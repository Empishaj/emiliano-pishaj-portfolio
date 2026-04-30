# ADR-017 — Observability: Strukturiertes Logging, Metriken & Tracing

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Boot 3.x · Micrometer · OpenTelemetry|
| Datum      | 2024-01-01                                        |
| Kategorie  | Observability / Operations                        |

---

## Kontext & Problem

Ein System das man nicht beobachten kann, kann man nicht betreiben. Observability besteht aus drei Säulen: **Logs** (was ist passiert), **Metriken** (wie verhält sich das System), **Traces** (wie fließt ein Request durch das System). Schlechtes Logging macht Incidents unlösbar. Fehlende Metriken machen Kapazitätsplanung zum Raten.

---

## Säule 1 — Strukturiertes Logging: JSON statt Freitext

### ❌ Schlecht — Freitext-Logging

```java
// Nicht durchsuchbar, nicht parsebar, keine Struktur
log.info("User " + userId + " hat Bestellung " + orderId + " aufgegeben für " + total + " EUR");
log.error("Fehler!!!");  // Kein Kontext, keine Exception
log.debug("Processing: " + order.toString()); // toString() kann sensitiv sein!

// Exception ohne Stack-Trace
try {
    process(order);
} catch (Exception e) {
    log.error("Fehler beim Verarbeiten: " + e.getMessage()); // Stack-Trace verloren!
}
```

### ✅ Gut — strukturiertes Logging mit SLF4J + MDC

```java
// Immer: SLF4J-Interface, niemals konkrete Logger-Implementierung
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

// Strukturierte Parameter statt String-Konkatenation
// → Logback/Logstash kann diese als separate JSON-Felder ausgeben
log.info("Order placed successfully",
    kv("userId",   userId),
    kv("orderId",  orderId),
    kv("total",    total),
    kv("currency", "EUR")
);

// MDC: Request-Kontext für alle Logs des Request-Threads
// → Alle Logs eines Requests haben dieselbe traceId
try {
    MDC.put("requestId", requestId.toString());
    MDC.put("userId",    userId.toString());

    processOrder(order);

} finally {
    MDC.clear(); // Pflicht: MDC nach Request-Ende leeren (Thread-Pool!)
}

// Exception korrekt loggen: Exception als letztes Argument — gibt Stack-Trace
try {
    paymentGateway.charge(order);
} catch (PaymentException e) {
    log.error("Payment failed for order {}", orderId, e); // ← e als letztes Argument!
    throw new OrderPaymentException(orderId, e);
}

// Log-Level korrekt verwenden:
log.error("..."); // Systemfehler, Intervention nötig
log.warn("...");  // Unerwarteter Zustand, System funktioniert noch
log.info("...");  // Business-Event (Bestellung, Registrierung, Zahlung)
log.debug("..."); // Technischer Kontext für Diagnose (nur in Entwicklung aktiv)
log.trace("..."); // Extrem detailliert (nie in Produktion)
```

### Logback-Konfiguration für JSON in Produktion:

```xml
<!-- logback-spring.xml -->
<springProfile name="prod">
    <appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- Automatisch: timestamp, level, logger, message, MDC-Felder als JSON -->
            <includeMdcKeyName>requestId</includeMdcKeyName>
            <includeMdcKeyName>userId</includeMdcKeyName>
            <includeMdcKeyName>traceId</includeMdcKeyName>
        </encoder>
    </appender>
    <root level="INFO"><appender-ref ref="JSON_STDOUT"/></root>
</springProfile>
```

---

## Säule 2 — Metriken mit Micrometer

```java
// Micrometer ist die Abstraktion — Prometheus, Datadog, CloudWatch als Backend
@Service
public class OrderService {

    private final MeterRegistry meterRegistry;

    // Counter: monoton steigende Zähler (Bestellungen, Fehler, Logins)
    private final Counter ordersPlacedCounter;
    private final Counter paymentFailuresCounter;

    // Timer: Dauer von Operationen
    private final Timer orderProcessingTimer;

    // Gauge: aktueller Wert (Queue-Länge, offene Verbindungen)
    private final AtomicInteger pendingOrdersGauge;

    public OrderService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.ordersPlacedCounter = Counter.builder("orders.placed")
            .description("Anzahl aufgegebener Bestellungen")
            .tag("env", "production")
            .register(meterRegistry);

        this.paymentFailuresCounter = Counter.builder("orders.payment.failures")
            .description("Fehlgeschlagene Zahlungen")
            .register(meterRegistry);

        this.orderProcessingTimer = Timer.builder("orders.processing.duration")
            .description("Verarbeitungsdauer pro Bestellung")
            .publishPercentiles(0.5, 0.95, 0.99) // P50, P95, P99
            .register(meterRegistry);

        this.pendingOrdersGauge = meterRegistry.gauge(
            "orders.pending.count", new AtomicInteger(0));
    }

    @Transactional
    public OrderCreatedResponse placeOrder(CreateOrderCommand command) {
        return orderProcessingTimer.record(() -> {
            var result = doPlaceOrder(command);
            ordersPlacedCounter.increment();
            pendingOrdersGauge.incrementAndGet();
            return result;
        });
    }
}

// Oder deklarativ mit @Timed (einfacher, aber weniger Kontrolle)
@Timed(value = "orders.processing.duration", percentiles = {0.5, 0.95, 0.99})
public OrderCreatedResponse placeOrder(CreateOrderCommand command) { ... }
```

---

## Säule 3 — Distributed Tracing mit OpenTelemetry

```java
// Spring Boot 3.x + Micrometer Tracing + OpenTelemetry = automatisches Tracing
// Konfiguration in application.yml:
// management:
//   tracing:
//     sampling:
//       probability: 1.0  # 100% in Entwicklung, 0.1 (10%) in Produktion
//   otlp:
//     tracing:
//       endpoint: http://jaeger:4318/v1/traces

// Spring injiziert TraceId/SpanId automatisch in MDC → alle Logs haben traceId!
// Jeder REST-Call, JPA-Query, Kafka-Message wird automatisch instrumentiert.

// Manueller Span für wichtige Business-Operationen:
@Service
public class PaymentService {

    private final Tracer tracer; // Micrometer Tracer

    public PaymentResult processPayment(PaymentCommand command) {
        // Eigener Span mit Business-Kontext
        var span = tracer.nextSpan()
            .name("payment.process")
            .tag("payment.provider", command.provider().name())
            .tag("payment.amount",   command.amount().toString())
            .start();

        try (var ignored = tracer.withSpan(span)) {
            return paymentGateway.charge(command);
        } catch (PaymentException e) {
            span.tag("error", "true");
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

---

## Actuator: Health & Info Endpoints

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized  # Nie "always" in Produktion!
  info:
    git:
      mode: full  # Git-Commit-Info im /actuator/info
```

```java
// Eigener HealthIndicator für externe Abhängigkeiten
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            var status = paymentGateway.ping();
            return Health.up()
                .withDetail("provider", "Stripe")
                .withDetail("responseTime", status.latencyMs() + "ms")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## Konsequenzen

**Positiv:** JSON-Logs sind in Elasticsearch/Loki direkt durchsuchbar. TraceId im Log macht es trivial, alle Logs eines einzelnen Requests zu finden. Metriken ermöglichen SLA-Monitoring und Alerting.

**Negativ:** `MDC.clear()` in `finally`-Block ist Pflicht — vergessen führt zu verseuchten Thread-Pool-Kontexten. Zu viele Metriken mit hoher Kardinalität (z. B. userId als Tag) können Prometheus überlasten.

---

## 💡 Guru-Tipps

- **Logging-Format in Tests**: In Tests `logback-test.xml` mit einfachem Format — JSON ist nur für Produktion.
- **Nie `System.out.println()`** — immer SLF4J. `println` erscheint in keinem Log-Aggregator.
- **Sensitive Daten im Log**: Passwörter, Tokens, Kreditkartennummern dürfen nie geloggt werden — `@ToString.Exclude` (Lombok) oder manuelles `toString()`.
- **Metriken-Namenskonvention**: `substantiv.verb.einheit` — z. B. `http.requests.duration`, `db.queries.count`, `cache.hits.ratio`.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — Keine Secrets im Log.
- [ADR-006](ADR-006-spring-boot-serviceschicht.md) — MDC-Kontext in der Service-Schicht.
