# ADR-102 — OpenTelemetry: Vollständiges Setup für Traces, Metrics & Logs

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / Platform-Team                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Observability · Tracing · Monitoring                          |
| Betroffene Teams  | Alle Engineering-Teams, Platform-Team                         |
| Abhängigkeiten    | ADR-017 (Observability), ADR-054 (SLO), ADR-038 (Kubernetes)  |

---

## 1. Kontext & Treiber

### 1.1 Das Problem ohne verteiltes Tracing

```
INCIDENT: Bestellung schlägt fehl. P95-Latenz: 4.2 Sekunden. SLO verletzt.

OHNE OpenTelemetry:
  Entwickler öffnet Logs von Order-Service:
  "2024-03-15 10:30:01.234 ERROR Order failed"
  Keine weiteren Details.

  Fragen die nicht beantwortet werden können:
  - Welcher der 3 downstream-Calls hat die 4.2s verursacht?
  - War es Stripe, Inventory oder die eigene DB?
  - Hat dieser spezifische Request eine andere Code-Route genommen?
  - War es dieser Nutzer, diese Region, dieses Produkt?
  → Mean Time to Diagnose: 2-3 Stunden

MIT OpenTelemetry:
  Entwickler öffnet Jaeger/Grafana Tempo:
  Sucht nach traceId aus dem Error-Log.
  Sieht sofort: Request-Waterfall über 4 Services.
  Order-Service: 200ms → Inventory: 150ms → Payment (Stripe): 3.8s ← HIER!
  → Diagnose: 5 Minuten
```

### 1.2 OpenTelemetry vs. andere Lösungen

```
VENDOR-SPEZIFISCH (Datadog, New Relic, Dynatrace):
  → Einfaches Setup
  → Vendor-Lock-in: wenn Preis steigt oder Vendor wechselt → Alles neu
  → Kosten: 50-200 EUR/Host/Monat

ZIPKIN (ältere Alternative):
  → Nur Tracing (kein Metrics, kein Logs)
  → Nicht der heutige Standard

OPENTELEMETRY (OTEL):
  → CNCF-Standard: vendor-neutral
  → Traces + Metrics + Logs (die "drei Säulen")
  → Einmal instrumentieren, beliebig exportieren (Jaeger, Grafana, Datadog)
  → Kostenlos — nur Infrastruktur für Backends kostet
  ✅ GEWÄHLT
```

---

## 2. Entscheidung

Alle Services verwenden den OpenTelemetry Java Agent (Zero-Code-Instrumentation) ergänzt durch manuelle Instrumentierung für Business-Events. Export erfolgt über OTLP an einen OTel-Collector. Sampling: 10% in Produktion, 100% für Errors und langsame Requests.

---

## 3. Die drei Säulen der Observability

```
TRACES:  "Was ist wann passiert?" — Zeitlinie eines einzelnen Requests
METRICS: "Wie verhält sich das System insgesamt?" — Aggregierte Zahlen
LOGS:    "Was wurde ausgegeben?" — Textuelle Events

OpenTelemetry verbindet alle drei:
  Jedes Log hat die traceId des aktuellen Requests → direkte Verknüpfung
  Jede Metric hat Exemplare mit traceIds → von Metrik zum Trace
  → "Meine P95-Latenz ist 800ms [klick auf Exemplar] → hier ist der Trace"
```

---

## 4. Setup: OpenTelemetry Java Agent

### 4.1 Zero-Code-Instrumentation (empfohlener Start)

```dockerfile
# Dockerfile: OTEL Java Agent einbinden
FROM eclipse-temurin:21-jre-alpine AS runtime

# OTEL Java Agent herunterladen
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/\
    download/v2.1.0/opentelemetry-javaagent.jar /otel/agent.jar

COPY build/libs/app.jar /app/app.jar

ENTRYPOINT ["java",
    # OTEL Agent als JVM-Agent anhängen
    "-javaagent:/otel/agent.jar",

    # Service-Name für Tracing-UI
    "-Dotel.service.name=${SERVICE_NAME}",

    # Exporter: OTLP (OpenTelemetry Protocol) an Collector
    "-Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_ENDPOINT}",
    "-Dotel.exporter.otlp.protocol=grpc",

    # Was wird exportiert?
    "-Dotel.traces.exporter=otlp",
    "-Dotel.metrics.exporter=otlp",
    "-Dotel.logs.exporter=otlp",

    # Propagation: TraceContext (W3C Standard) + Baggage
    "-Dotel.propagators=tracecontext,baggage",

    # Sampling (Details im nächsten Abschnitt)
    "-Dotel.traces.sampler=parentbased_traceidratio",
    "-Dotel.traces.sampler.arg=0.1",  # 10% in Produktion

    "-jar", "/app/app.jar"]
```

### 4.2 Kubernetes: OTEL Operator (besser als Dockerfile)

```yaml
# Mit OTEL Operator: kein Dockerfile ändern nötig!
# Operator injiziert Agent automatisch per Annotation

apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  template:
    metadata:
      annotations:
        # Diese Annotation triggert die automatische Agent-Injection
        instrumentation.opentelemetry.io/inject-java: "true"
    spec:
      containers:
        - name: order-service
          image: ghcr.io/example/order-service:latest
          # KEIN -javaagent nötig — Operator macht das!

---
# OTEL Instrumentation-Konfiguration (ClusterWeit)
apiVersion: opentelemetry.io/v1alpha1
kind: Instrumentation
metadata:
  name: java-instrumentation
spec:
  exporter:
    endpoint: http://otel-collector:4317
  propagators:
    - tracecontext
    - baggage
  sampler:
    type: parentbased_traceidratio
    argument: "0.1"
  java:
    image: ghcr.io/open-telemetry/opentelemetry-operator/autoinstrumentation-java:2.1.0
```

---

## 5. OTel Collector: Das zentrale Routing-Element

```yaml
# otel-collector-config.yaml
receivers:
  # Empfängt Telemetrie von Services (OTLP über gRPC + HTTP)
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  # Batch: sammelt Daten und sendet gebündelt → Performance
  batch:
    timeout: 10s
    send_batch_size: 1000

  # Memory-Limiter: verhindert OOM des Collectors
  memory_limiter:
    limit_mib: 1500
    spike_limit_mib: 400
    check_interval: 5s

  # Attribute hinzufügen: Umgebung, Region
  attributes:
    actions:
      - key: deployment.environment
        value: production
        action: upsert
      - key: k8s.cluster.name
        value: eu-west-prod
        action: upsert

  # Sensitive Daten entfernen (DSGVO! → ADR-035)
  attributes/redact:
    actions:
      - key: http.request.header.authorization
        action: delete    # Authorization-Header nicht in Traces!
      - key: user.email
        action: delete    # PII entfernen

exporters:
  # Traces → Grafana Tempo (Open Source)
  otlp/tempo:
    endpoint: http://tempo:4317

  # Metrics → Prometheus (über OTLP-Receiver)
  prometheusremotewrite:
    endpoint: http://prometheus:9090/api/v1/write

  # Logs → Grafana Loki
  loki:
    endpoint: http://loki:3100/loki/api/v1/push

  # Debug (nur in Dev):
  debug:
    verbosity: detailed

service:
  pipelines:
    traces:
      receivers:  [otlp]
      processors: [memory_limiter, batch, attributes, attributes/redact]
      exporters:  [otlp/tempo]

    metrics:
      receivers:  [otlp]
      processors: [memory_limiter, batch, attributes]
      exporters:  [prometheusremotewrite]

    logs:
      receivers:  [otlp]
      processors: [memory_limiter, batch, attributes, attributes/redact]
      exporters:  [loki]
```

---

## 6. Sampling-Strategie

```java
// PROBLEM: 1000 RPS = 86.400.000 Traces/Tag
// Alle speichern = teuer und unnötig (die meisten sind identisch)

// SAMPLING-STRATEGIE:
// 10% Random-Sample = 8.640.000 Traces/Tag → genug für Statistiken
// 100% für Errors = alle Fehler werden gespeichert
// 100% für langsame Requests (> 2s) = alle Performance-Probleme

// Implementierung: Composite Sampler

@Bean
public Sampler compositeSampler() {
    return new CompositeSampler(
        // Immer samplen wenn: Error
        new AlwaysSampleOnError(),
        // Immer samplen wenn: langsam (Custom Span-Attribute)
        new AlwaysSampleWhenAttributePresent("request.slow"),
        // Sonst: 10% Ratio
        Sampler.traceIdRatioBased(0.10)
    );
}

// Custom Sampler für "langsame Requests"
public class AlwaysSampleOnError implements Sampler {
    @Override
    public SamplingResult shouldSample(
            Context parentContext, String traceId, String name,
            SpanKind spanKind, Attributes attributes, List<LinkData> links) {

        // HTTP-Status >= 500 → immer samplen
        var statusCode = attributes.get(AttributeKey.longKey("http.response.status_code"));
        if (statusCode != null && statusCode >= 500) {
            return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE);
        }
        return SamplingResult.create(SamplingDecision.DROP);
    }
}
```

---

## 7. Manuelle Instrumentierung für Business-Events

```java
// Automatische Instrumentierung durch Agent:
// ✅ HTTP-Requests (Latenz, Status-Codes)
// ✅ JDBC-Queries (SQL, Dauer)
// ✅ Kafka Producer/Consumer
// ✅ Spring MVC, WebFlux
// ✅ HikariCP Connection Pool

// Manuell instrumentieren für:
// Business-Events die fachlich relevant sind

@Service
public class OrderDomainService {

    private static final Tracer TRACER = GlobalOpenTelemetry
        .getTracer("com.example.orders", "2.3.0");

    private static final Meter METER = GlobalOpenTelemetry
        .getMeter("com.example.orders");

    // Business-Metrik: wie viele Bestellungen pro Minute?
    private final LongCounter orderCounter = METER
        .counterBuilder("orders.placed.total")
        .setDescription("Total number of orders placed")
        .setUnit("orders")
        .build();

    // Business-Histogramm: Bestellwert-Verteilung
    private final DoubleHistogram orderValueHistogram = METER
        .histogramBuilder("orders.placed.value.eur")
        .setDescription("Order value distribution in EUR")
        .setUnit("EUR")
        .setExplicitBucketBoundariesAdvice(
            List.of(10.0, 25.0, 50.0, 100.0, 250.0, 500.0, 1000.0))
        .build();

    public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
        // Manueller Span für Business-Operation
        var span = TRACER.spanBuilder("order.place")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("customer.id", cmd.customerId().value())
            .setAttribute("order.item.count", cmd.items().size())
            .startSpan();

        try (var scope = span.makeCurrent()) {
            var order = Order.place(cmd);
            orderRepository.save(order);

            // Business-Attribute auf Span setzen (für Suche in Jaeger)
            span.setAttribute("order.id", order.id().value());
            span.setAttribute("order.total.eur",
                order.total().amount().doubleValue());
            span.setAttribute("order.currency", order.total().currency().getCode());

            // Metriken aktualisieren
            orderCounter.add(1,
                Attributes.builder()
                    .put("currency", order.total().currency().getCode())
                    .put("country", cmd.shippingAddress().country())
                    .build());

            orderValueHistogram.record(
                order.total().amount().doubleValue(),
                Attributes.of(
                    AttributeKey.stringKey("currency"),
                    order.total().currency().getCode()));

            span.setStatus(StatusCode.OK);
            return OrderCreatedResult.from(order);

        } catch (Exception e) {
            // Exception auf Span setzen → erscheint in Jaeger mit Stack-Trace
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

---

## 8. Logs mit TraceID verknüpfen

```xml
<!-- logback-spring.xml: TraceID automatisch in Logs einbetten -->
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <!-- OpenTelemetry MDC-Keys automatisch eingebunden -->
      <!-- OTel Agent setzt automatisch: trace_id, span_id, trace_flags in MDC -->
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
```

```
ERGEBNIS im Log (JSON):
{
  "timestamp": "2024-03-15T10:30:01.234Z",
  "level": "INFO",
  "message": "Order placed successfully",
  "orderId": "ord-123",
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",  ← OTEL setzt das automatisch
  "span_id": "00f067aa0ba902b7",                   ← OTEL setzt das automatisch
  "service.name": "order-service"
}

→ In Grafana: Log → klick auf trace_id → direkt zum Trace in Tempo!
```

---

## 9. Grafana-Dashboard: Die drei Säulen verbunden

```
GOLDEN SIGNALS in Grafana (aus OTel-Daten):

Panel 1: Request Rate
  → rate(http_server_duration_count{service_name="order-service"}[5m])

Panel 2: Error Rate
  → rate(http_server_duration_count{http_response_status_code=~"5.."}[5m])
    / rate(http_server_duration_count[5m])

Panel 3: Latency (P95)
  → histogram_quantile(0.95,
      rate(http_server_duration_bucket{service_name="order-service"}[5m]))

Panel 4: Trace-Suche (Tempo-Datasource)
  → Suche nach traceId, Service, Fehler, Latenz-Range

Panel 5: Service Map
  → Automatisch generiert aus Span-Beziehungen
  → Zeigt: Order → Inventory → DB + Order → Stripe
```

---

## 10. Akzeptanzkriterien

- [ ] Alle Services senden Traces, Metrics, Logs an OTel Collector
- [ ] TraceID erscheint in allen Logs (automatisch durch OTEL Agent + Logback-Config)
- [ ] Sampling: 10% Random + 100% Errors + 100% Slow Requests (> 2s) konfiguriert
- [ ] Business-Metriken (`orders.placed.total`, `orders.placed.value.eur`) in Grafana sichtbar
- [ ] PII (Authorization-Header, E-Mails) wird im Collector entfernt (DSGVO)
- [ ] Service Map zeigt alle Downstream-Abhängigkeiten korrekt
- [ ] MTTR-Verbesserung messbar: Incident-Diagnose < 15 Minuten (vorher > 2h)

---

## Quellen & Referenzen

- **OpenTelemetry Documentation** — opentelemetry.io, vollständige Referenz.
- **CNCF, "Distributed Tracing Landscape"** — Vendor-Vergleich und Standardisierung.
- **Brendan Gregg, "Systems Performance" (2020), Kap. 2** — Observability-Grundlagen: USE und RED Methoden.
- **Cindy Sridharan, "Distributed Systems Observability" (2018), O'Reilly** — Die drei Säulen: Logs, Metrics, Traces.

---

## Verwandte ADRs

- [ADR-017](ADR-017-observability-logging-tracing.md) — Observability-Konzepte
- [ADR-054](ADR-054-slo-sla-alerting.md) — SLOs basieren auf OTel-Metriken
- [ADR-086](ADR-086-isaqb-querschnittskonzepte.md) — Correlation-IDs
