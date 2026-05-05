# QG-JAVA-017 — Observability: Strukturiertes Logging, Metriken und Tracing

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-017 |
| Titel | Observability: Strukturiertes Logging, Metriken und Tracing |
| Status | Accepted / verbindlicher Standard für neue und wesentlich geänderte Java-/Spring-Boot-Services |
| Version | 1.0.0 |
| Datum | 2023-02-04 |
| Kategorie | Observability / Operations / DevSecOps |
| Zielgruppe | Java-Entwickler, Tech Leads, SRE/Operations, QA, Security, Architektur, Plattform-Team |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Framework 6.x, Micrometer, Micrometer Tracing, OpenTelemetry |
| Geltungsbereich | Alle produktiven Java-Services, APIs, Worker, Batch-Prozesse und Integrationsadapter |
| Verbindlichkeit | Neue Services müssen diese Richtlinie erfüllen. Bestehende Services müssen bei relevanten Änderungen schrittweise migriert werden. Abweichungen sind im Pull Request fachlich und technisch zu begründen. |
| Technische Validierung | Gegen Spring Boot Actuator/Tracing, Micrometer Observation, OpenTelemetry Collector/Security und OWASP Logging validiert |
| Kurzentscheidung | Jeder Service muss strukturierte Logs, aussagekräftige Metriken, verteilte Traces, sichere Actuator-Konfiguration und PII-/Secret-freie Telemetrie bereitstellen. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Java- und Spring-Boot-Services so instrumentiert werden, dass sie im Betrieb beobachtbar, analysierbar und sicher diagnostizierbar sind.

Ein Service gilt nicht als produktionsreif, wenn er nur „funktioniert“. Er muss im Fehlerfall erklären können, **was passiert ist**, **wo es passiert ist**, **welcher Request betroffen war**, **welche Abhängigkeit beteiligt war**, **wie stark der Nutzer betroffen ist** und **ob sich das System wieder erholt**.

Observability besteht in dieser Richtlinie aus drei zentralen Signalen:

1. **Logs** beschreiben relevante Ereignisse und Fehler im zeitlichen Ablauf.
2. **Metriken** beschreiben aggregiertes Systemverhalten über die Zeit.
3. **Traces** beschreiben den Weg eines Requests durch Services, Datenbanken, Queues und externe Systeme.

Diese drei Signale müssen korreliert werden können. Ein Log ohne `traceId`, eine Metrik ohne Service-Kontext oder ein Trace mit sensiblen Daten ist unvollständig oder riskant.

---

## 2. Kurzregel

Jeder produktive Java-Service MUSS strukturierte JSON-Logs, technische und fachliche Metriken, verteiltes Tracing, sichere Actuator-Endpunkte und PII-/Secret-freie Telemetrie bereitstellen. Logs, Metriken und Traces MÜSSEN über `traceId`, `spanId`, `service.name`, Umgebung und Request-Kontext korrelierbar sein. Personenbezogene Daten, Tokens, Passwörter, API-Keys, Session-Werte und vollständige Payloads DÜRFEN NICHT ungeprüft in Logs, Spans, Metriken oder Actuator-Ausgaben gelangen.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- REST-APIs
- Spring-Boot-Services
- interne Plattformservices
- Worker und Batch-Jobs
- Messaging-Consumer und Producer
- Integrationsadapter zu externen Systemen
- Services mit Datenbankzugriff
- Services mit personenbezogenen Daten
- Services mit Mandantenfähigkeit
- Services mit SLO-, SLA- oder Incident-Relevanz

Diese Richtlinie gilt nicht für reine lokale Wegwerf-Prototypen ohne produktive Nutzung. Sobald ein Service in CI/CD, Staging oder Produktion läuft, gilt diese Richtlinie.

---

## 4. Technischer Hintergrund

Spring Boot 3 integriert Observability über Micrometer Observation, Micrometer Metrics und Micrometer Tracing. Die Micrometer Observation API verfolgt den Ansatz, Code einmal zu instrumentieren und daraus je nach Konfiguration Metriken, Traces und weitere Signale abzuleiten.

Spring Boot Actuator stellt Endpunkte für Health, Metrics, Info und Prometheus bereit. Spring Boot Actuator bietet außerdem Auto-Konfiguration für Micrometer Tracing, das als Fassade für Tracing-Systeme dient. OpenTelemetry dient als vendor-neutraler Standard für Telemetriedaten und kann über Collector-Komponenten Daten empfangen, filtern, transformieren und an Backend-Systeme wie Jaeger, Tempo, Grafana, Datadog, Elastic oder andere Systeme weiterleiten.

Observability ist kein Ersatz für Tests, keine Ausrede für unsauberen Code und kein Freibrief für das Sammeln beliebiger Daten. Die Telemetrie muss zweckgebunden, datensparsam, sicher und betrieblich nützlich sein.

---

## 5. Verbindlicher Standard

Jeder Service MUSS mindestens folgende Fähigkeiten bereitstellen:

1. Strukturierte JSON-Logs in produktiven Umgebungen.
2. Korrelationsfelder in Logs: `traceId`, `spanId`, `requestId`, `service.name`, `environment`.
3. Keine sensitiven Daten in Logs, Traces, Metriken oder Health Details.
4. Metriken für HTTP-Anfragen, Latenzen, Fehler, Datenbankzugriffe, externe Abhängigkeiten und fachliche Kernereignisse.
5. Tracing für eingehende Requests, ausgehende HTTP-Calls, Datenbankzugriffe und Messaging-Flüsse.
6. Sichere Actuator-Konfiguration mit begrenzter Endpunktfreigabe.
7. Health Checks für Liveness und Readiness.
8. Alerting auf Symptome und SLO-Verletzungen, nicht auf jedes einzelne Log-Event.
9. Dokumentierte Dashboards und Runbooks für produktionskritische Services.
10. Tests oder Review-Nachweise für Logging-, Metrik- und Trace-Konventionen.

---

## 6. Säule 1: Strukturiertes Logging

### 6.1 Warum strukturiertes Logging?

Freitext-Logs sind für Menschen kurzfristig lesbar, aber für Maschinen schlecht auswertbar. In produktiven Systemen müssen Logs nach Feldern gefiltert, gruppiert und korreliert werden können. JSON-Logs erlauben Suchabfragen wie:

```text
service.name = "order-service"
AND level = "ERROR"
AND traceId = "..."
AND orderId = "..."
```

Ein Logeintrag muss deshalb nicht nur eine Nachricht enthalten, sondern strukturierte Kontextfelder.

### 6.2 Schlechte Anwendung: Freitext, Konkatenation, verlorener Stacktrace

```java
// ❌ Nicht strukturiert, teuer bei deaktiviertem Log-Level, nicht sauber parsebar
log.info("User " + userId + " hat Bestellung " + orderId + " aufgegeben für " + total + " EUR");

// ❌ Kein Kontext
log.error("Fehler!!!");

// ❌ Potenziell sensitive Daten über toString()
log.debug("Processing order: " + order);

// ❌ Exception-Stacktrace geht verloren
try {
    paymentGateway.charge(order);
} catch (PaymentException e) {
    log.error("Fehler beim Bezahlen: " + e.getMessage());
}
```

### 6.3 Gute Anwendung: Parameterisierte Logs und Exception als letztes Argument

```java
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

public void placeOrder(OrderId orderId, UserId userId, Money total) {
    log.info("Order placed successfully: orderId={}, userId={}, amount={}, currency={}",
            orderId.value(),
            userId.value(),
            total.amount(),
            total.currency());
}
```

```java
try {
    paymentGateway.charge(order);
} catch (PaymentException e) {
    log.error("Payment failed for orderId={}", order.id().value(), e);
    throw new OrderPaymentException(order.id(), e);
}
```

Die Exception MUSS als letztes Argument übergeben werden. Nur dann schreibt SLF4J/Logback den vollständigen Stacktrace.

### 6.4 Strukturierte Argumente

Für Logback mit Logstash Encoder können strukturierte Schlüssel-Wert-Paare genutzt werden:

```java
import static net.logstash.logback.argument.StructuredArguments.kv;

log.info("Order placed successfully",
        kv("orderId", orderId.value()),
        kv("userId", userId.value()),
        kv("amount", total.amount()),
        kv("currency", total.currency()));
```

### 6.5 Produktionsformat: JSON

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="prod">
        <appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
                <includeMdcKeyName>requestId</includeMdcKeyName>
                <includeMdcKeyName>tenantId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="JSON_STDOUT"/>
        </root>
    </springProfile>

    <springProfile name="local,test">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] %logger{36} traceId=%X{traceId:-} - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

### 6.6 Log-Level-Regeln

| Level | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| ERROR | Systemfehler, Datenverlust, fehlgeschlagene kritische Operation oder Zustand mit Interventionsbedarf | Payment Provider nicht erreichbar und kein Fallback möglich | Verwenden |
| WARN | Unerwarteter Zustand, degradierter Betrieb, Fallback aktiv, aber Service läuft weiter | Retry erschöpft, Fallback genutzt | Verwenden |
| INFO | Fachlich oder betrieblich relevantes Ereignis | Bestellung erstellt, Zahlung bestätigt, Import beendet | Sparsam verwenden |
| DEBUG | Diagnoseinformationen für Entwicklung oder gezielte Fehleranalyse | Query-Parameter nach Validierung, technischer Routingpfad | In Produktion nur temporär |
| TRACE | Sehr detaillierte Diagnose | Einzelne interne Schritte | In Produktion grundsätzlich vermeiden |

### 6.7 Keine sensitiven Daten im Log

Nicht erlaubt:

```java
log.info("Login with password={}", password);
log.debug("Authorization header={}", authorizationHeader);
log.info("JWT token={}", token);
log.info("Customer iban={}", iban);
log.info("Request body={}", rawBody);
```

Erlaubt:

```java
log.info("Login attempt for userId={}, result={}", userId.value(), result);
log.warn("Payment failed for orderId={}, provider={}, reasonCode={}",
        orderId.value(),
        provider.name(),
        failureCode);
```

### 6.8 MDC und Kontext

MDC darf genutzt werden, um Request-Kontext automatisch an alle Logeinträge zu binden.

```java
@Component
public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            MDC.put("requestId", resolveOrCreateRequestId(request));
            MDC.put("tenantId", resolveTenantIdIfAvailable(request));
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

`MDC.clear()` im `finally`-Block ist Pflicht, weil Thread-Wiederverwendung sonst Kontext in spätere Requests verschleppen kann. Bei Virtual Threads ist die Gefahr geringer als bei klassischen Thread Pools, aber die Regel bleibt korrekt und robust.

---

## 7. Säule 2: Metriken mit Micrometer

### 7.1 Zweck von Metriken

Metriken beantworten nicht „was ist in Request 123 passiert“, sondern „wie verhält sich das System insgesamt“. Sie sind Grundlage für Alerting, SLOs, Kapazitätsplanung und Trendanalyse.

Typische Metriken:

- Request Rate
- Error Rate
- Latenzverteilungen
- Datenbankverbindungsnutzung
- Queue-Länge
- Cache-Hit-Rate
- externe Provider-Fehler
- fachliche Ereigniszähler
- Importdauer
- Anzahl verarbeiteter Nachrichten

### 7.2 Counter

Counter zählen monoton steigende Ereignisse.

```java
@Service
public class OrderMetrics {

    private final Counter ordersPlaced;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersPlaced = Counter.builder("orders.placed.total")
                .description("Anzahl erfolgreich aufgegebener Bestellungen")
                .tag("service", "order-service")
                .register(registry);
    }

    public void recordOrderPlaced() {
        ordersPlaced.increment();
    }
}
```

### 7.3 Timer

Timer messen Dauer und Anzahl von Operationen.

```java
@Service
public class OrderService {

    private final Timer orderCreationTimer;

    public OrderService(MeterRegistry registry) {
        this.orderCreationTimer = Timer.builder("orders.creation.duration")
                .description("Dauer der Bestellungserstellung")
                .publishPercentileHistogram()
                .register(registry);
    }

    public OrderCreatedResponse create(CreateOrderCommand command) {
        return orderCreationTimer.record(() -> doCreate(command));
    }
}
```

Für Prometheus sind Histogramme meist besser als nur lokal berechnete Percentiles, weil Percentiles sonst nicht sauber über Instanzen aggregiert werden können.

### 7.4 Gauge

Gauges messen aktuelle Werte.

```java
@Component
public class ImportQueueMetrics {

    private final ImportQueue queue;

    public ImportQueueMetrics(MeterRegistry registry, ImportQueue queue) {
        this.queue = queue;

        Gauge.builder("imports.queue.size", queue, ImportQueue::size)
                .description("Aktuelle Länge der Import-Queue")
                .register(registry);
    }
}
```

Gauges dürfen nicht auf kurzlebige Objekte zeigen, die durch Garbage Collection verschwinden oder instabil werden.

### 7.5 Keine hohe Kardinalität

Nicht erlaubt:

```java
Counter.builder("orders.created")
        .tag("userId", userId.value().toString())
        .tag("orderId", orderId.value().toString())
        .register(registry);
```

Das erzeugt potenziell Millionen Zeitreihen. Hohe Kardinalität kann Monitoring-Systeme überlasten.

Erlaubt:

```java
Counter.builder("orders.created")
        .tag("tenantTier", tenant.tier().name())
        .tag("country", tenant.countryCode())
        .register(registry);
```

Auch diese Tags müssen begrenzt und kontrolliert sein.

### 7.6 Metrik-Namenskonvention

| Metriktyp | Namensschema | Beispiel |
|---|---|---|
| Counter | `<domain>.<event>.total` | `orders.created.total` |
| Timer | `<domain>.<operation>.duration` | `payments.authorization.duration` |
| Gauge | `<domain>.<resource>.current` oder konkrete Einheit | `imports.queue.size` |
| Fehler | `<domain>.<operation>.errors.total` | `payments.authorization.errors.total` |

Einheiten müssen eindeutig sein. Dauer wird über Timer gemessen, nicht als freier Zahlenwert ohne Einheit.

---

## 8. Säule 3: Distributed Tracing mit OpenTelemetry

### 8.1 Zweck von Tracing

Tracing zeigt, wie ein einzelner Request durch ein verteiltes System fließt. Ein Trace besteht aus Spans. Jeder Span beschreibt einen Abschnitt, zum Beispiel HTTP-Eingang, Service-Methode, Datenbankzugriff, HTTP-Call zu einem Provider oder Kafka-Verarbeitung.

Tracing beantwortet Fragen wie:

- Welcher Service hat die Latenz verursacht?
- Welche externe Abhängigkeit war langsam?
- Wo ist ein Fehler entstanden?
- Wurden Retry, Timeout oder Circuit Breaker ausgelöst?
- Welche Logs gehören zu diesem Trace?

### 8.2 Spring Boot Konfiguration

```yaml
management:
  tracing:
    sampling:
      probability: 0.1
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
```

Für lokale Entwicklung kann `probability: 1.0` sinnvoll sein. In Produktion ist Sampling bewusst zu wählen. Kritische Services können höhere Sampling-Raten haben; hochfrequentierte Services benötigen Kosten- und Datenschutzbewertung.

### 8.3 Manuelle Spans nur für relevante fachliche Operationen

```java
@Service
public class PaymentService {

    private final io.micrometer.tracing.Tracer tracer;
    private final PaymentGateway paymentGateway;

    public PaymentService(io.micrometer.tracing.Tracer tracer,
                          PaymentGateway paymentGateway) {
        this.tracer = tracer;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult authorize(PaymentCommand command) {
        var span = tracer.nextSpan()
                .name("payment.authorize")
                .tag("payment.provider", command.provider().name())
                .tag("payment.currency", command.amount().currency().getCurrencyCode())
                .start();

        try (var ignored = tracer.withSpan(span)) {
            return paymentGateway.authorize(command);
        } catch (PaymentException e) {
            span.tag("error", "true");
            span.tag("error.type", e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

Nicht erlaubt:

```java
span.tag("user.email", email);
span.tag("payment.cardNumber", cardNumber);
span.tag("authorization.header", authorizationHeader);
span.tag("request.body", rawBody);
```

### 8.4 OpenTelemetry Collector: Sensitive Attribute entfernen

```yaml
processors:
  attributes/drop-sensitive:
    actions:
      - key: http.request.header.authorization
        action: delete
      - key: http.request.header.cookie
        action: delete
      - key: user.email
        action: delete
      - key: customer.iban
        action: delete
      - key: customer.phone
        action: delete
```

Der Collector ist ein geeigneter Ort, um Telemetrie vor der Weiterleitung an Backends zu bereinigen. Das ersetzt aber nicht die Pflicht, sensitive Daten bereits im Anwendungscode zu vermeiden.

---

## 9. Actuator: Health, Metrics und sichere Endpunkte

### 9.1 Grundkonfiguration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

In Produktion dürfen nicht pauschal alle Actuator-Endpunkte exponiert werden.

Nicht erlaubt:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

### 9.2 Security für Actuator

```java
@Configuration
public class ActuatorSecurityConfig {

    @Bean
    SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("OPS"))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
```

Health und Info können je nach Infrastruktur öffentlich oder intern erreichbar sein. Metrics, Prometheus, Env, Beans, Configprops, Loggers und Thread Dump müssen geschützt werden.

### 9.3 Custom HealthIndicator

```java
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    private final PaymentGateway paymentGateway;

    public PaymentGatewayHealthIndicator(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    public Health health() {
        try {
            var status = paymentGateway.ping();

            return Health.up()
                    .withDetail("provider", "payment-gateway")
                    .withDetail("latencyMs", status.latencyMs())
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("provider", "payment-gateway")
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}
```

Fehlermeldungen in Health Details dürfen keine Secrets, Tokens, vollständigen URLs mit Credentials oder personenbezogene Daten enthalten.

---

## 10. Security- und SaaS-Aspekte

### 10.1 Keine personenbezogenen Daten in Telemetrie

Telemetrie ist oft für mehr Personen zugänglich als produktive Datenbanken. Deshalb sind Logs, Traces und Metriken besonders kritisch.

Nicht erlaubt:

- E-Mail-Adressen in Logs
- Passwörter in Logs
- Tokens in Logs oder Traces
- IBAN/Kreditkartendaten in Logs
- vollständige Request-/Response-Bodies
- personenbezogene IDs als Metrik-Tags
- Authorization Header in Spans
- Cookies in Traces
- tenantübergreifende Diagnose ohne Zugriffskontrolle

### 10.2 Tenant-Kontext

In SaaS-Systemen ist `tenantId` betrieblich nützlich, aber sicherheitskritisch. Es darf nur eine interne, nicht direkt personenbezogene und nicht erratbare Tenant-Referenz verwendet werden.

Erlaubt:

```java
MDC.put("tenantId", tenantContext.technicalTenantId());
```

Nicht erlaubt:

```java
MDC.put("tenantName", tenant.legalName());
MDC.put("customerEmail", user.email());
```

### 10.3 Observability darf keine neue Angriffsfläche schaffen

Actuator-Endpunkte, Dashboards, Logsysteme und Trace-Systeme müssen geschützt werden. Wer Zugriff auf Logs und Traces hat, kann häufig Geschäftsprozesse, Systemgrenzen, interne IDs, Fehlermuster und Abhängigkeitsstrukturen sehen.

Deshalb gilt:

- Actuator-Endpunkte sind minimal zu exponieren.
- Debug-Logs sind in Produktion nicht dauerhaft aktiv.
- Log-Level-Änderungen zur Laufzeit müssen auditierbar sein.
- Trace-Sampling darf nicht unkontrolliert personenbezogene Daten erfassen.
- Dashboards dürfen keine geheimen Werte anzeigen.
- Log- und Trace-Zugriffe müssen rollenbasiert gesteuert werden.

---

## 11. Fehlerbehandlung und Observability

Fehler müssen so geloggt werden, dass Betrieb und Entwicklung handlungsfähig sind.

Schlecht:

```java
catch (Exception e) {
    log.error("Error: {}", e.getMessage());
    throw e;
}
```

Gut:

```java
catch (PaymentProviderTimeoutException e) {
    log.warn("Payment provider timeout: orderId={}, provider={}, timeoutMs={}",
            orderId.value(),
            provider.name(),
            timeout.toMillis(),
            e);

    metrics.recordPaymentTimeout(provider);
    throw new PaymentUnavailableException(orderId, e);
}
```

Jeder produktionsrelevante Fehler braucht:

- technischen Kontext
- fachlichen Kontext ohne sensitive Daten
- Exception Stacktrace
- Metrik oder vorhandenes technisches Signal
- Korrelation über Trace

---

## 12. Alerting und SLOs

Alerting darf nicht auf beliebige einzelne Logs reagieren. Gute Alerts basieren auf Nutzerwirkung und Systemzustand.

Gute Alert-Kandidaten:

- Error Rate über Schwelle
- P95/P99-Latenz über Schwelle
- Circuit Breaker offen
- Queue-Länge wächst dauerhaft
- Consumer Lag wächst
- Datenbankverbindungen erschöpft
- fehlgeschlagene Login-Versuche steigen auffällig
- 5xx-Anteil steigt
- externe Abhängigkeit antwortet nicht
- Readiness dauerhaft DOWN

Schlechte Alert-Kandidaten:

- jedes einzelne `ERROR`-Log
- Debug-Log-Muster
- sporadische Einzel-Time-Outs ohne Nutzerwirkung
- technische Metrik ohne Schwelle und Kontext

Beispielhafte SLO-orientierte Formulierung:

```text
99,5 % aller Checkout-Requests sollen innerhalb von 800 ms erfolgreich beantwortet werden.
Alert: Wenn die 5-Minuten-Error-Rate > 1 % liegt oder P95 > 800 ms für 10 Minuten.
```

---

## 13. Gute Anwendung: vollständiger Service-Ausschnitt

```java
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final MeterRegistry meterRegistry;
    private final Timer checkoutTimer;
    private final Counter checkoutFailures;
    private final PaymentGateway paymentGateway;

    public CheckoutService(MeterRegistry meterRegistry,
                           PaymentGateway paymentGateway) {
        this.meterRegistry = meterRegistry;
        this.paymentGateway = paymentGateway;

        this.checkoutTimer = Timer.builder("checkout.processing.duration")
                .description("Dauer der Checkout-Verarbeitung")
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.checkoutFailures = Counter.builder("checkout.failures.total")
                .description("Anzahl fehlgeschlagener Checkouts")
                .register(meterRegistry);
    }

    public CheckoutResult checkout(CheckoutCommand command) {
        return checkoutTimer.record(() -> {
            try {
                log.info("Checkout started: orderId={}, tenantId={}",
                        command.orderId().value(),
                        command.tenantId().value());

                var result = paymentGateway.authorize(command.payment());

                log.info("Checkout completed: orderId={}, result={}",
                        command.orderId().value(),
                        result.status());

                return CheckoutResult.success(result);

            } catch (PaymentException e) {
                checkoutFailures.increment();

                log.warn("Checkout failed: orderId={}, failureType={}",
                        command.orderId().value(),
                        e.getClass().getSimpleName(),
                        e);

                return CheckoutResult.failed(command.orderId());
            }
        });
    }
}
```

---

## 14. Anti-Patterns

### 14.1 `System.out.println`

```java
System.out.println("Order created");
```

Problem: Kein Level, kein Logger, keine Korrelation, keine strukturierte Ausgabe, keine zentrale Steuerung.

### 14.2 Logging von vollständigen Objekten

```java
log.info("User={}", user);
```

Problem: `toString()` kann sensitive Felder enthalten und ändert sich unkontrolliert.

### 14.3 User-ID als Metrik-Tag

```java
Counter.builder("login.success")
        .tag("userId", userId.toString())
        .register(registry);
```

Problem: Hohe Kardinalität und potenziell personenbezogene Telemetrie.

### 14.4 Actuator komplett offen

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

Problem: Interne Konfiguration, Beans, Metriken oder Diagnosedaten können sichtbar werden.

### 14.5 Trace-Spans mit Payloads

```java
span.tag("request.body", requestBody);
```

Problem: Sensitive Daten gelangen in Trace-Systeme und werden schwer löschbar.

### 14.6 Logs ohne Exception

```java
log.error("Could not create order: {}", e.getMessage());
```

Problem: Stacktrace fehlt, Ursachenanalyse wird unnötig schwer.

### 14.7 Zu viele Business-Events als INFO

```java
log.info("Validated field A");
log.info("Calculated price");
log.info("Mapped DTO");
```

Problem: Lograuschen, höhere Kosten, weniger Signal.

---

## 15. Tests und Qualitätssicherung

### 15.1 Log-Verhalten testen

Logging wird nicht für jede Methode getestet. Getestet werden nur kritische Anforderungen:

- sensitive Daten erscheinen nicht im Log
- Audit-Events werden erzeugt
- Fehler enthalten Kontext
- Exception wird korrekt übergeben
- MDC wird geleert

Beispiel mit einem Test-Appender:

```java
@Test
void login_doesNotLogPassword() {
    var appender = new InMemoryLogAppender();
    appender.attachTo(LoginService.class);

    loginService.login(new LoginCommand("max@example.com", "secret-password"));

    assertThat(appender.messages())
            .noneMatch(message -> message.contains("secret-password"));
}
```

### 15.2 Metriken testen

```java
@Test
void checkout_incrementsFailureCounter_whenPaymentFails() {
    var registry = new SimpleMeterRegistry();
    var service = new CheckoutService(registry, failingPaymentGateway());

    service.checkout(validCommand());

    assertThat(registry.counter("checkout.failures.total").count())
            .isEqualTo(1.0);
}
```

### 15.3 Actuator-Konfiguration prüfen

```java
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorSecurityTest {

    @Autowired MockMvc mockMvc;

    @Test
    void metricsEndpoint_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpoint_isAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
```

---

## 16. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Logging | Werden SLF4J und strukturierte Felder verwendet? | `orderId`, `traceId` | Pflicht |
| Stacktrace | Wird die Exception als letztes Logargument übergeben? | `log.error("...", e)` | Pflicht |
| Sensitive Daten | Werden Tokens, Passwörter, E-Mails, IBANs vermieden? | kein `requestBody` | Pflicht |
| MDC | Wird Kontext gesetzt und sauber gelöscht? | `finally { MDC.clear(); }` | Pflicht bei manuellem MDC |
| Metriken | Gibt es Metriken für kritische Operationen? | Timer, Counter | Pflicht |
| Kardinalität | Werden keine IDs als Tags verwendet? | keine `userId`-Tags | Pflicht |
| Tracing | Sind relevante externe Calls sichtbar? | HTTP, DB, Messaging | Pflicht |
| Actuator | Sind Endpunkte minimal und geschützt? | health/info offen, metrics geschützt | Pflicht |
| Health | Gibt es Liveness/Readiness? | Kubernetes Probes | Pflicht für Deployments |
| Alerts | Basieren Alerts auf Symptomen/SLOs? | Error Rate, P95 | Pflicht |
| SaaS | Ist Tenant-Kontext sicher und nicht personenbezogen? | technische Tenant-ID | Pflicht |
| Tests | Gibt es Nachweise für kritisches Logging/Metrics/Security? | MockMvc, MeterRegistry | Pflicht |

---

## 17. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Keine Verwendung von System.out.println in main-Code.
- Keine Logger-Konkretionen außerhalb von SLF4J.
- Keine log-Aufrufe mit Variablennamen password, token, secret, authorization.
- Keine Metrik-Tags mit userId, orderId, email, sessionId.
- Actuator exposure darf in Produktion nicht "*" enthalten.
- management.endpoint.health.show-details darf in Produktion nicht "always" sein.
- Services müssen service.name und environment setzen.
- Docker/Kubernetes Deployment muss OTEL_SERVICE_NAME oder spring.application.name setzen.
```

Beispiel mit ArchUnit:

```java
@ArchTest
static final ArchRule no_system_out_in_production_code =
        noClasses()
                .that().resideOutsideOfPackage("..test..")
                .should().accessClassesThat().belongToAnyOf(System.class)
                .because("Produktiver Code muss SLF4J Logging verwenden, nicht System.out.");
```

---

## 18. Migration bestehender Services

Bestehende Services werden in sechs Schritten migriert:

1. `spring.application.name` setzen.
2. JSON-Logging für produktive Profile einführen.
3. `traceId` und `spanId` in Logs sichtbar machen.
4. Actuator-Endpunkte auf `health`, `info`, `metrics`, `prometheus` begrenzen.
5. Metriken für kritische Use Cases ergänzen.
6. OpenTelemetry Export über Collector konfigurieren.
7. Sensitive Logging prüfen und entfernen.
8. Dashboards und Alerts für Kernflüsse ergänzen.

Migration darf inkrementell erfolgen, aber jeder Schritt muss produktiv validiert werden.

---

## 19. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein sehr kleiner interner Batch-Prozess keine HTTP-Oberfläche besitzt
- ein Service nur in lokaler Entwicklung läuft
- ein bestehender Legacy-Service noch nicht vollständig migriert werden kann
- ein Backend-System aktuell keine OTLP-Exporte unterstützt
- regulatorische Vorgaben bestimmte Telemetrie einschränken

Auch bei Ausnahmen gilt: Keine sensitiven Daten in Logs, keine offenen Actuator-Endpunkte und keine unkontrollierte hohe Metrik-Kardinalität.

---

## 20. Definition of Done

Ein Service erfüllt diese Richtlinie, wenn:

1. produktive Logs strukturiert und maschinenlesbar sind,
2. Logs `traceId`, `spanId`, `service.name` und Umgebungskontext enthalten,
3. keine sensitiven Daten in Logs, Metriken, Traces oder Health Details erscheinen,
4. HTTP-, Datenbank-, Messaging- und externe Calls beobachtbar sind,
5. fachlich kritische Operationen Timer oder Counter besitzen,
6. Metrik-Tags keine hohe Kardinalität erzeugen,
7. Traces an einen OpenTelemetry Collector oder ein kompatibles Backend exportiert werden,
8. Actuator-Endpunkte minimal exponiert und geschützt sind,
9. Liveness- und Readiness-Signale vorhanden sind,
10. Dashboards und Alerts für zentrale Use Cases existieren,
11. kritische Logging-/Metric-/Actuator-Regeln getestet oder im Review nachgewiesen sind.

---

## 21. Quellen und weiterführende Literatur

- Spring Boot Reference Documentation — Actuator, Metrics, Tracing: https://docs.spring.io/spring-boot/reference/actuator/
- Spring Boot Tracing Documentation: https://docs.spring.io/spring-boot/reference/actuator/tracing.html
- Spring Blog — Observability with Spring Boot 3: https://spring.io/blog/2022/10/12/observability-with-spring-boot-3
- Micrometer Documentation: https://docs.micrometer.io/
- OpenTelemetry Documentation: https://opentelemetry.io/docs/
- OpenTelemetry — Handling Sensitive Data: https://opentelemetry.io/docs/security/handling-sensitive-data/
- OpenTelemetry Collector — Transforming Telemetry: https://opentelemetry.io/docs/collector/transforming-telemetry/
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
