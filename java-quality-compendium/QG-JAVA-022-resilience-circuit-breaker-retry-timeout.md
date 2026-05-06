# QG-JAVA-022 — Resilience: Circuit Breaker, Retry, Timeout und Bulkhead

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-022 |
| Titel | Resilience: Circuit Breaker, Retry, Timeout und Bulkhead |
| Status | Accepted / verbindlicher Standard für resiliente externe Aufrufe |
| Version | 1.0.0 |
| Datum | 2024-09-23 |
| Review-Datum | 2026-05-01 |
| Kategorie | Resilience / Architektur / Betrieb / Verteilte Systeme |
| Zielgruppe | Java-Entwickler, Tech Leads, Architektur, QA, DevOps, SRE, Security |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Resilience4j 2.x, Micrometer, OpenTelemetry |
| Geltungsbereich | HTTP-Clients, REST-Clients, Datenbankzugriffe, Messaging-Adapter, externe Provider, interne Service-zu-Service-Aufrufe, SaaS-Plattformintegrationen |
| Verbindlichkeit | Jeder externe oder potenziell langsame Aufruf muss Timeout, Fehlerstrategie und Beobachtbarkeit besitzen. Retries und Circuit Breaker werden nur bewusst und getestet eingesetzt. |
| Technische Validierung | Gegen Resilience4j Spring-Boot-Konfiguration, Spring `RestClient`/`ClientHttpRequestFactory`, Micrometer-Metriken und gängige Distributed-Systems-Resilience-Patterns validiert |
| Kurzentscheidung | Externe Aufrufe dürfen nicht unbegrenzt warten, nicht unkontrolliert wiederholt werden und nicht ganze Services blockieren. Timeout, Retry, Circuit Breaker, Bulkhead, Fallback, Idempotenz und Metriken bilden zusammen den Mindeststandard. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Java- und Spring-Boot-Services externe Abhängigkeiten resilient aufrufen.

In verteilten Systemen sind Fehler kein Ausnahmefall. Netzwerke sind unzuverlässig, Downstream-Services werden langsam, Datenbanken haben Lock-Situationen, DNS kann verzögern, Provider antworten mit 429 oder 503, einzelne Instanzen starten neu, Zertifikate laufen ab und Wartungsfenster passieren. Ein Service, der so tut, als seien externe Aufrufe immer schnell und erfolgreich, ist nicht produktionsreif.

Resilience bedeutet nicht, jeden Fehler zu verstecken. Resilience bedeutet, Fehler kontrolliert zu begrenzen, schnell sichtbar zu machen und das System in einem definierten Zustand zu halten.

Diese Guideline beantwortet konkret:

- Wann ist ein Timeout Pflicht?
- Wann darf ein Retry stattfinden?
- Wann ist ein Retry gefährlich?
- Wann hilft ein Circuit Breaker?
- Wann braucht ein Service einen Bulkhead?
- Wie werden Fallbacks fachlich sauber gestaltet?
- Wie werden Resilience-Mechanismen getestet?
- Welche Metriken müssen sichtbar sein?
- Welche Anti-Patterns führen zu Kaskadenausfällen?

---

## 2. Kurzregel

Jeder externe Aufruf bekommt ein Timeout. Retries erfolgen nur für transiente technische Fehler, mit begrenzter Anzahl, Backoff und Jitter. Fachliche Ablehnungen werden nicht wiederholt. Circuit Breaker schützen Downstream-Services und eigene Threads vor wiederholtem Fail-Slow-Verhalten. Bulkheads begrenzen gleichzeitige Zugriffe auf riskante Ressourcen. Fallbacks müssen fachlich korrekt sein und dürfen keine stillen Datenfehler erzeugen. Jede Resilience-Konfiguration wird über Metriken, Logs und Tests überprüfbar gemacht.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- REST-Aufrufe zu internen Services,
- REST-/HTTP-Aufrufe zu externen Providern,
- Payment-, Identity-, Notification-, Mail-, SMS- und ERP-Adapter,
- Datenbankqueries mit potenziell hoher Laufzeit,
- Kafka-/Messaging-Verarbeitung mit Downstream-Aufrufen,
- Dateisystem-/Object-Storage-Zugriffe,
- Cache- und Redis-Zugriffe,
- SaaS-Mandantenintegrationen,
- Scheduler und Batch-Jobs mit externen Abhängigkeiten,
- alle produktionsrelevanten IO-Operationen.

Nicht betroffen sind rein lokale CPU-Berechnungen ohne IO. Für CPU-intensive Verarbeitung gelten andere Regeln, zum Beispiel begrenzte Executor, Backpressure, Queue-Limits und Lasttests.

---

## 4. Technischer Hintergrund

Resilience4j ist eine modulare Fault-Tolerance-Bibliothek für Java. Sie bietet unter anderem Circuit Breaker, Retry, Rate Limiter, Bulkhead, Thread Pool Bulkhead und TimeLimiter. Für Spring Boot können Instanzen dieser Module in der `application.yml` konfiguriert und über Annotationen oder programmgesteuerte Dekoration verwendet werden.

Spring Frameworks `RestClient` ist ein synchroner HTTP-Client. Die tatsächlichen Timeouts werden über die darunterliegende `ClientHttpRequestFactory` oder HTTP-Client-Implementierung konfiguriert. Spring `SimpleClientHttpRequestFactory` unterstützt Connect- und Read-Timeouts als `Duration`. Ein Timeout von `0` bedeutet dort ein unendliches Timeout und ist für produktive externe Aufrufe grundsätzlich nicht zulässig.

Micrometer kann Resilience4j-Metriken veröffentlichen. In Spring Cloud Circuit Breaker mit Resilience4j werden Metriken automatisch konfiguriert, wenn passende Dependencies wie Spring Boot Actuator und `resilience4j-micrometer` im Classpath vorhanden sind.

---

## 5. Resilience ist kein Ersatz für gutes Design

Resilience-Muster retten kein falsches Schnittstellendesign.

| Problem | Resilience-Muster hilft? | Richtige Einordnung |
|---|---|---|
| Externer Service ist kurz überlastet | Ja, Retry mit Backoff kann helfen | transienter Fehler |
| Payment wird fachlich abgelehnt | Nein | keine Wiederholung |
| API ist grundsätzlich zu langsam | Teilweise | Performance- und Architekturproblem |
| Service ruft fünf Downstreams synchron im Request auf | Teilweise | Orchestrierungs- und Kopplungsproblem |
| Nicht-idempotente Operation wird wiederholt | Nein, gefährlich | Idempotency-Key oder kein Retry |
| Datenbankquery ist schlecht indiziert | Timeout begrenzt Schaden | Query/Index reparieren |
| Downstream ist dauerhaft kaputt | Circuit Breaker schützt | Incident/Operational Problem |
| Fallback liefert falsche Daten | Nein, erzeugt Fachfehler | Fallback-Design klären |

---

## 6. Regel 1: Timeout ist Pflicht

### 6.1 Problem

Ohne Timeout kann ein externer Aufruf unbegrenzt blockieren. Das ist eine der häufigsten Ursachen für Kaskadenausfälle.

Schlecht:

```java
RestClient paymentClient = RestClient.builder()
        .baseUrl("https://payment-service")
        .build();
```

Problem: Die Timeout-Semantik hängt von Defaults der darunterliegenden Implementierung ab. Defaults sind selten ein belastbarer Produktionsstandard.

Schlecht:

```java
RestTemplate restTemplate = new RestTemplate();
```

Problem: Ohne explizite Request Factory sind Timeouts nicht als fachliche Architekturentscheidung sichtbar.

Schlecht:

```java
@Query("SELECT o FROM OrderEntity o JOIN o.items i WHERE i.status = :status")
List<OrderEntity> findExpensiveOrders(OrderStatus status);
```

Problem: Eine teure Query kann lange laufen, Locks halten und Threads blockieren.

### 6.2 Gute Anwendung: `RestClient` mit expliziten Timeouts

```java
@Configuration
class PaymentClientConfiguration {

    @Bean
    RestClient paymentRestClient(RestClient.Builder builder) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return builder
                .baseUrl("https://payment-service")
                .requestFactory(requestFactory)
                .build();
    }
}
```

### 6.3 Gute Anwendung: Apache HttpClient mit Connection Pool

Für produktive Services ist häufig ein gepoolter HTTP-Client sinnvoll.

```java
@Configuration
class ExternalHttpClientConfiguration {

    @Bean
    RestClient externalRestClient(RestClient.Builder builder) {
        var requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setConnectTimeout(Timeout.ofSeconds(2))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build();

        var httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();

        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return builder
                .requestFactory(requestFactory)
                .baseUrl("https://external-provider.example")
                .build();
    }
}
```

Wichtig: Timeouts und Connection-Pool-Grenzen gehören zusammen. Ein Timeout ohne Pool-Grenze schützt nicht vor Ressourcenerschöpfung.

### 6.4 Gute Anwendung: JPA Query Timeout

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @QueryHints(@QueryHint(
            name = "jakarta.persistence.query.timeout",
            value = "3000"
    ))
    @Query("""
            SELECT o
            FROM OrderEntity o
            JOIN FETCH o.items
            WHERE o.customerId = :customerId
            """)
    List<OrderEntity> findWithItemsByCustomerId(CustomerId customerId);
}
```

Globale Konfiguration:

```yaml
spring:
  jpa:
    properties:
      jakarta.persistence.query.timeout: 3000
```

### 6.5 Timeout-Arten

| Timeout-Art | Details/Erklärung | Typischer Wert |
|---|---|---:|
| Connect Timeout | Zeit zum Aufbau der TCP-/TLS-Verbindung | 1–3 Sekunden |
| Read/Response Timeout | Zeit bis Antwortdaten gelesen werden | 2–10 Sekunden |
| Connection Request Timeout | Zeit auf freie Verbindung aus Pool | 100 ms–1 Sekunde |
| Query Timeout | maximale DB-Query-Laufzeit | 1–10 Sekunden |
| Overall Operation Timeout | gesamte fachliche Operation | Use-Case-spezifisch |
| TimeLimiter | begrenzt asynchrone Operationen | abhängig von SLA |

Timeout-Werte sind keine Dogmen. Sie müssen aus SLO, Downstream-SLA, Nutzererwartung und Betriebsrealität abgeleitet werden.

---

## 7. Regel 2: Retry nur für transiente Fehler

### 7.1 Problem

Retries können helfen, wenn Fehler kurzzeitig sind. Falsch eingesetzt verschlimmern sie Ausfälle.

Schlecht:

```java
for (int i = 0; i < 3; i++) {
    try {
        return paymentGateway.charge(command);
    } catch (Exception ignored) {
        Thread.sleep(100);
    }
}
throw new PaymentFailedException(command.orderId());
```

Probleme:

- retryt alle Exceptions,
- retryt fachliche Fehler,
- kein Backoff,
- kein Jitter,
- keine Metrik,
- keine klare Obergrenze für Gesamtdauer,
- verschluckt Kontext,
- kann Downstream weiter überlasten.

### 7.2 Gute Anwendung: Resilience4j Retry

```yaml
resilience4j:
  retry:
    instances:
      paymentProvider:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        randomized-wait-factor: 0.3
        retry-exceptions:
          - java.net.SocketTimeoutException
          - org.springframework.web.client.ResourceAccessException
          - org.springframework.web.client.HttpServerErrorException$ServiceUnavailable
        ignore-exceptions:
          - com.example.payment.PaymentDeclinedException
          - com.example.payment.InsufficientFundsException
          - com.example.payment.InvalidCardException
```

```java
@Service
public class PaymentProviderAdapter {

    private final PaymentProviderClient client;

    public PaymentProviderAdapter(PaymentProviderClient client) {
        this.client = client;
    }

    @Retry(name = "paymentProvider", fallbackMethod = "paymentFallback")
    public PaymentResult charge(PaymentCommand command) {
        return client.charge(command);
    }

    private PaymentResult paymentFallback(PaymentCommand command, Exception exception) {
        return PaymentResult.technicalFailure(
                command.orderId(),
                "Payment provider temporarily unavailable"
        );
    }
}
```

### 7.3 Welche Fehler dürfen retryt werden?

| Fehler | Retry? | Begründung |
|---|---:|---|
| Connect Timeout | Ja | oft transient |
| Read Timeout | Ja, begrenzt | kann transient sein |
| HTTP 429 | Ja, mit Backoff und Rate-Limit-Respekt | Provider signalisiert Überlast |
| HTTP 500 | Ja, begrenzt | möglicherweise transient |
| HTTP 502/503/504 | Ja | typisches Gateway-/Überlastsignal |
| HTTP 400 | Nein | Request ist falsch |
| HTTP 401/403 | Nein | Auth/Authz-Problem |
| HTTP 404 | Normalerweise nein | Ressource fehlt; kontextabhängig |
| Payment Declined | Nein | fachliche Ablehnung |
| Validation Error | Nein | wiederholt denselben Fehler |
| Optimistic Locking | Kontextabhängig | kann bei kontrollierter Wiederholung sinnvoll sein |
| Deadlock | Ja, begrenzt | DB-transient, aber Ursache beobachten |

### 7.4 Idempotenz ist Pflicht bei Retry

Ein Retry darf keine doppelten Nebenwirkungen erzeugen.

Schlecht:

```java
@Retry(name = "paymentProvider")
public PaymentResult charge(PaymentCommand command) {
    return paymentProvider.charge(command);
}
```

Wenn `charge` nicht idempotent ist, kann dieselbe Zahlung mehrfach ausgelöst werden.

Besser:

```java
public record PaymentCommand(
        OrderId orderId,
        Money amount,
        IdempotencyKey idempotencyKey
) {}
```

```java
@Retry(name = "paymentProvider")
public PaymentResult charge(PaymentCommand command) {
    return paymentProvider.charge(
            command.orderId(),
            command.amount(),
            command.idempotencyKey()
    );
}
```

Regel:

```text
Automatischer Retry nur, wenn Operation idempotent ist oder durch Idempotency-Key idempotent gemacht wurde.
```

---

## 8. Regel 3: Circuit Breaker schützt vor Fail-Slow und Kaskaden

### 8.1 Problem

Wenn ein Downstream-Service langsam oder kaputt ist, sollen nicht alle Requests weiterhin lange auf denselben Fehler laufen. Ein Circuit Breaker öffnet nach definierten Fehlersignalen und liefert schnell einen Fallback oder Fehler zurück.

### 8.2 Circuit-Breaker-Zustände

```text
CLOSED
  ├─ normale Aufrufe laufen durch
  ├─ Fehler und langsame Calls werden gemessen
  └─ Schwelle überschritten → OPEN

OPEN
  ├─ Aufrufe werden nicht mehr an Downstream gesendet
  ├─ CallNotPermittedException / Fallback
  └─ Wartezeit abgelaufen → HALF_OPEN

HALF_OPEN
  ├─ wenige Probeaufrufe erlaubt
  ├─ Probe erfolgreich → CLOSED
  └─ Probe fehlerhaft → OPEN
```

### 8.3 Gute Konfiguration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentProvider:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50
        slow-call-duration-threshold: 3s
        slow-call-rate-threshold: 80
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        record-exceptions:
          - java.io.IOException
          - java.net.SocketTimeoutException
          - org.springframework.web.client.ResourceAccessException
          - org.springframework.web.client.HttpServerErrorException
        ignore-exceptions:
          - com.example.payment.PaymentDeclinedException
```

### 8.4 Anwendung

```java
@Service
public class PaymentProviderAdapter {

    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "paymentCircuitFallback")
    public PaymentResult charge(PaymentCommand command) {
        return paymentProviderClient.charge(command);
    }

    private PaymentResult paymentCircuitFallback(
            PaymentCommand command,
            CallNotPermittedException exception) {

        return PaymentResult.providerUnavailable(command.orderId());
    }

    private PaymentResult paymentCircuitFallback(
            PaymentCommand command,
            Exception exception) {

        return PaymentResult.technicalFailure(command.orderId(), "Payment call failed");
    }
}
```

### 8.5 Wann ist ein Circuit Breaker sinnvoll?

| Situation | Circuit Breaker? | Details/Erklärung |
|---|---:|---|
| externer HTTP-Provider | Ja | schützt Threads und Provider |
| interne Service-zu-Service-Aufrufe | Ja | verhindert Kaskaden |
| Datenbankzugriff | Vorsicht | oft besser Pool-/Query-Timeout und DB-Resilienz |
| Cache-Zugriff | Ja, häufig sinnvoll | Fallback auf DB oder degraded mode |
| lokaler CPU-Code | Nein | kein Downstream |
| fachliche Validierung | Nein | kein transienter Fehler |
| Kafka Consumer Downstream-Aufruf | Ja | verhindert blockierende Consumer |

---

## 9. Regel 4: Bulkhead begrenzt Ressourcenverbrauch

### 9.1 Problem

Ein langsamer Downstream darf nicht alle Threads, Verbindungen oder Worker eines Services blockieren.

Bulkheads isolieren Ressourcen. Das Konzept entspricht wasserdichten Schotten in einem Schiff: Ein Schaden in einem Bereich soll nicht das gesamte System fluten.

### 9.2 Semaphore Bulkhead

```yaml
resilience4j:
  bulkhead:
    instances:
      paymentProvider:
        max-concurrent-calls: 10
        max-wait-duration: 100ms
```

```java
@Bulkhead(name = "paymentProvider", type = Bulkhead.Type.SEMAPHORE)
public PaymentResult charge(PaymentCommand command) {
    return paymentProviderClient.charge(command);
}
```

### 9.3 Thread Pool Bulkhead

```yaml
resilience4j:
  thread-pool-bulkhead:
    instances:
      reportExport:
        core-thread-pool-size: 4
        max-thread-pool-size: 8
        queue-capacity: 50
```

Thread Pool Bulkheads sind sinnvoll, wenn blockierende oder lange laufende Aufgaben bewusst in separate Ressourcen ausgelagert werden sollen. Bei Java 21 und Virtual Threads ist die Entscheidung differenziert zu treffen: Virtual Threads reduzieren Thread-Kosten, lösen aber nicht automatisch Downstream-Überlast, Connection-Pool-Limits oder Provider-Rate-Limits. Ein Bulkhead bleibt deshalb auch mit Virtual Threads relevant.

### 9.4 Bulkhead-Werte ableiten

| Einflussgröße | Details/Erklärung |
|---|---|
| Downstream-SLA | Wie viele parallele Calls verträgt der Provider? |
| eigener Thread-/Connection-Pool | Wie viele Ressourcen stehen zur Verfügung? |
| Request-Rate | Wie viele Calls entstehen pro Nutzerrequest? |
| Timeout | Lange Timeouts brauchen niedrigere Parallelitätsgrenzen. |
| Business-Priorität | Payment ist kritischer als Newsletter-Versand. |
| Fallback-Fähigkeit | Degradierbare Features können niedriger begrenzt werden. |

---

## 10. Regel 5: TimeLimiter für asynchrone Operationen

### 10.1 Einsatzgebiet

`TimeLimiter` begrenzt die Laufzeit asynchroner Operationen, typischerweise `CompletionStage`/`CompletableFuture`.

```yaml
resilience4j:
  timelimiter:
    instances:
      paymentProvider:
        timeout-duration: 5s
        cancel-running-future: true
```

```java
@TimeLimiter(name = "paymentProvider", fallbackMethod = "paymentTimeoutFallback")
public CompletableFuture<PaymentResult> chargeAsync(PaymentCommand command) {
    return CompletableFuture.supplyAsync(() -> paymentProviderClient.charge(command));
}
```

### 10.2 Wichtige Klarstellung

Ein TimeLimiter ersetzt kein HTTP-Client-Timeout. Wenn der darunterliegende HTTP-Client keinen Timeout hat, kann die tatsächliche IO-Operation weiterlaufen oder Ressourcen halten. Deshalb gilt:

```text
HTTP Timeout zuerst konfigurieren. TimeLimiter zusätzlich für asynchrone Gesamtbegrenzung verwenden.
```

---

## 11. Kombinationsregeln

### 11.1 Reihenfolge verstehen

Die Reihenfolge der Resilience-Mechanismen beeinflusst Verhalten und Metriken.

Empfohlene Denklogik:

```text
1. Bulkhead begrenzt Ressourcen.
2. Timeout begrenzt Wartezeit.
3. Retry wiederholt nur geeignete Fehler.
4. Circuit Breaker verhindert wiederholte Aufrufe bei bekannt schlechtem Zustand.
5. Fallback definiert fachlich akzeptables Ersatzverhalten.
```

Bei Annotationen wird die tatsächliche Reihenfolge über Spring AOP und Resilience4j-Integration bestimmt. Deshalb gilt: Kritische Kombinationen müssen getestet und bei Bedarf programmatisch dekoriert werden.

### 11.2 Programmgesteuerte Dekoration für Klarheit

```java
public PaymentResult charge(PaymentCommand command) {
    Supplier<PaymentResult> supplier = () -> paymentProviderClient.charge(command);

    Supplier<PaymentResult> decorated = Decorators.ofSupplier(supplier)
            .withBulkhead(bulkheadRegistry.bulkhead("paymentProvider"))
            .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker("paymentProvider"))
            .withRetry(retryRegistry.retry("paymentProvider"))
            .withFallback(this::isRecoverable, throwable -> fallback(command, throwable))
            .decorate();

    return decorated.get();
}
```

Bei komplexen oder sicherheitskritischen Resilience-Flows ist programmatische Dekoration oft verständlicher als mehrere Annotationen übereinander.

---

## 12. Fallback-Design

### 12.1 Fallback ist Fachentscheidung

Ein Fallback darf nicht einfach irgendeinen Wert zurückgeben. Er muss fachlich korrekt sein.

Schlecht:

```java
private PaymentResult fallback(PaymentCommand command, Exception exception) {
    return PaymentResult.success("fallback");
}
```

Das wäre fachlich falsch und gefährlich.

Besser:

```java
private PaymentResult fallback(PaymentCommand command, Exception exception) {
    return PaymentResult.pendingProviderConfirmation(command.orderId());
}
```

Oder:

```java
private PaymentResult fallback(PaymentCommand command, Exception exception) {
    throw new PaymentTemporarilyUnavailableException(command.orderId(), exception);
}
```

### 12.2 Fallback-Typen

| Fallback-Typ | Details/Erklärung | Beispiel |
|---|---|---|
| Fail Fast | Fehler schnell und sauber zurückgeben | Payment nicht verfügbar |
| Queue for Later | Verarbeitung asynchron nachholen | E-Mail, Notification |
| Cached Response | letzte bekannte Daten liefern | Produktkatalog, Konfiguration |
| Degraded Result | reduzierte Funktion anbieten | Empfehlungen ausblenden |
| Default Value | nur bei fachlich harmlosen Daten | leere optionale Liste |
| Manual Review | Prozess in manuellen Status setzen | Zahlung unklar |

### 12.3 Verbotene Fallbacks

Nicht erlaubt:

- Erfolg vortäuschen,
- Sicherheitsprüfung überspringen,
- falsche Berechtigungen annehmen,
- Zahlungsstatus als erfolgreich setzen, wenn Provider nicht erreichbar ist,
- personenbezogene Daten aus unsicherem Cache liefern,
- still leere Ergebnisse liefern, wenn Fachlogik dadurch falsch wird.

---

## 13. Datenbank-Resilience

Datenbanken brauchen eigene Resilience-Regeln.

### 13.1 Timeouts

- Query Timeout setzen.
- Transaction Timeout prüfen.
- Connection Pool Timeout setzen.
- Lock Timeout für kritische DBs prüfen.

### 13.2 Connection Pool

Beispiel Hikari:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      connection-timeout: 1000
      validation-timeout: 1000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 13.3 Retry bei Datenbanken

DB-Retry ist gefährlich, wenn eine Transaktion Nebenwirkungen hat. Retry ist nur kontrolliert zulässig, zum Beispiel bei:

- Deadlocks,
- transienten Verbindungsfehlern,
- Optimistic-Locking-Konflikten mit klarer Wiederholungsstrategie.

Nicht blind wiederholen:

- nicht-idempotente Writes,
- lange Transaktionen,
- unbekannte SQL Exceptions,
- fachliche Constraint-Verletzungen.

---

## 14. Kafka- und Messaging-Resilience

Bei Kafka Consumer-Flows gelten besondere Regeln:

- Consumer muss idempotent sein.
- Downstream-Aufrufe aus Consumer müssen Timeout/Bulkhead/Circuit Breaker haben.
- Technische Fehler werden mit Backoff retryt.
- Poison Messages gehen ins DLT.
- Fachliche Fehler werden als fachliches Folgeevent oder kontrollierter Fehler behandelt.
- Consumer Lag wird überwacht.

Schlecht:

```java
@KafkaListener(topics = "orders.placed.v1")
public void onOrderPlaced(OrderPlacedEvent event) {
    paymentClient.capture(event.paymentId());
}
```

Besser:

```java
@KafkaListener(topics = "orders.placed.v1")
@Transactional
public void onOrderPlaced(EventEnvelope<OrderPlacedPayload> event, Acknowledgment ack) {
    if (processedEventRepository.existsByEventId(event.eventId())) {
        ack.acknowledge();
        return;
    }

    paymentReservationService.reserveWithResilience(event.payload());
    processedEventRepository.markProcessed(event.eventId());
    ack.acknowledge();
}
```

---

## 15. SaaS- und Tenant-Aspekte

Resilience darf Tenant-Isolation nicht verletzen.

### 15.1 Tenant-spezifische Circuit Breaker?

Nicht jeder Tenant braucht eigene Circuit-Breaker-Instanzen. Aber bei großen B2B-Tenants oder tenant-spezifischen externen Integrationen kann Isolation sinnvoll sein.

Beispiel:

```text
paymentProvider-tenant-a
paymentProvider-tenant-b
```

Vorsicht: Zu viele dynamische Circuit-Breaker-Namen können Metrik-Kardinalität explodieren lassen.

### 15.2 Tenant-spezifische Fallbacks

Fallbacks dürfen keine Daten tenantübergreifend liefern.

Schlecht:

```java
return cachedCatalog.get(productId);
```

Besser:

```java
return cachedCatalog.get(tenantId, productId);
```

### 15.3 Security

Bei Auth-/Authorization-Services gilt:

- keine unsicheren „allow by default“-Fallbacks,
- keine Rollen aus Cache verwenden, wenn der Cache nicht tenant- und freshness-sicher ist,
- bei unklarem Auth-Zustand sicher fehlschlagen,
- Fallback darf nicht zu Privilege Escalation führen.

---

## 16. Observability und Metriken

Resilience ohne Metriken ist blind.

Pflichtsignale:

| Signal | Details/Erklärung |
|---|---|
| Circuit Breaker State | CLOSED, OPEN, HALF_OPEN |
| Failure Rate | Fehlerrate je Instanz |
| Slow Call Rate | Anteil langsamer Calls |
| Retry Attempts | Anzahl Wiederholungen |
| Retry Exhausted | aufgebrauchte Retries |
| Timeout Count | Timeouts je Downstream |
| Bulkhead Rejections | abgewiesene Aufrufe wegen Limit |
| Fallback Count | wie oft Fallback aktiv ist |
| Downstream Latency | P50/P95/P99 |
| Error Classification | technisch vs fachlich |
| Tenant-/Provider-Segment | kontrolliert und ohne hohe Kardinalität |

Beispiel Logging:

```java
log.warn("Payment provider unavailable: orderId={}, provider={}, circuitState={}",
        command.orderId(),
        "stripe",
        circuitBreaker.getState());
```

Nicht loggen:

```java
log.warn("Payment command failed: {}", command);
```

Payment-Kommandos können sensitive Daten enthalten.

---

## 17. Konfiguration zentralisieren

Resilience-Konfiguration gehört nicht verstreut in Klassen.

Gut:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentProvider:
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      paymentProvider:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        randomized-wait-factor: 0.3
  bulkhead:
    instances:
      paymentProvider:
        max-concurrent-calls: 10
        max-wait-duration: 100ms
```

Noch besser: Standardprofile für Integrationsarten definieren.

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default-external-http:
        sliding-window-size: 20
        failure-rate-threshold: 50
        slow-call-duration-threshold: 3s
      critical-payment:
        sliding-window-size: 50
        failure-rate-threshold: 30
        wait-duration-in-open-state: 15s
```

---

## 18. Tests

### 18.1 Circuit Breaker offen

```java
@Test
void charge_returnsProviderUnavailable_whenCircuitIsOpen() {
    var circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentProvider");
    circuitBreaker.transitionToOpenState();

    var result = paymentService.charge(validPaymentCommand());

    assertThat(result.status()).isEqualTo(PaymentStatus.PROVIDER_UNAVAILABLE);
    verifyNoInteractions(paymentProviderClient);
}
```

### 18.2 Retry bei transientem Fehler

```java
@Test
void charge_retriesTimeout_andSucceedsOnThirdAttempt() {
    when(paymentProviderClient.charge(any()))
            .thenThrow(new ResourceAccessException("timeout"))
            .thenThrow(new ResourceAccessException("timeout"))
            .thenReturn(PaymentResult.success("tx-123"));

    var result = paymentService.charge(validPaymentCommand());

    assertThat(result.transactionId()).isEqualTo("tx-123");
    verify(paymentProviderClient, times(3)).charge(any());
}
```

### 18.3 Fachlicher Fehler wird nicht retryt

```java
@Test
void charge_doesNotRetry_whenPaymentIsDeclined() {
    when(paymentProviderClient.charge(any()))
            .thenThrow(new PaymentDeclinedException("card declined"));

    assertThatThrownBy(() -> paymentService.charge(validPaymentCommand()))
            .isInstanceOf(PaymentDeclinedException.class);

    verify(paymentProviderClient, times(1)).charge(any());
}
```

### 18.4 Bulkhead begrenzt parallele Aufrufe

```java
@Test
void charge_rejectsCalls_whenBulkheadIsFull() {
    var bulkhead = bulkheadRegistry.bulkhead("paymentProvider");

    bulkhead.acquirePermission();
    bulkhead.acquirePermission();

    assertThatThrownBy(() -> paymentService.charge(validPaymentCommand()))
            .isInstanceOf(BulkheadFullException.class);
}
```

### 18.5 Timeout mit WireMock oder MockWebServer

Integrationstest:

```java
@Test
void clientTimesOut_whenProviderDoesNotRespondFastEnough() {
    wireMockServer.stubFor(post("/payments")
            .willReturn(aResponse()
                    .withFixedDelay(10_000)
                    .withStatus(200)));

    assertThatThrownBy(() -> paymentProviderClient.charge(validPaymentCommand()))
            .isInstanceOf(PaymentProviderTimeoutException.class);
}
```

---

## 19. Chaos Engineering

Resilience-Konfiguration muss nicht nur im Unit-Test, sondern auch unter realistischeren Störungen geprüft werden.

Mögliche Experimente:

- Payment Provider antwortet 5 Sekunden verzögert.
- Provider liefert 50% HTTP 503.
- DNS-Auflösung scheitert.
- Datenbankquery läuft langsam.
- Redis ist nicht erreichbar.
- Kafka Consumer Downstream ist down.
- Externer Provider liefert 429.

Experiment-Template:

```markdown
## Hypothese
Wenn der Payment Provider für 60 Sekunden HTTP 503 liefert,
öffnet der Circuit Breaker innerhalb von 20 Calls und der Service gibt kontrolliert
`PROVIDER_UNAVAILABLE` zurück.

## Steady State
- 5xx Rate < 1%
- Payment Fallback Rate < 1%
- Circuit Breaker CLOSED

## Experiment
- 60 Sekunden 503 simulieren
- Requests mit realistischer Rate senden
- Circuit State beobachten

## Erwartung
- Circuit Breaker öffnet
- keine Thread-Pool-Erschöpfung
- Fehler sind in Metriken sichtbar
- Recovery nach Provider-Erholung
```

---

## 20. Anti-Patterns

### 20.1 Kein Timeout

Jeder externe Aufruf ohne Timeout ist ein Produktionsrisiko.

### 20.2 Retry auf alles

```java
catch (Exception e) { retry(); }
```

Falsch, weil fachliche und technische Fehler vermischt werden.

### 20.3 Retry ohne Jitter

Viele Instanzen wiederholen gleichzeitig und erzeugen Thundering Herd.

### 20.4 Circuit Breaker ohne Metrik

Ein Circuit Breaker, dessen Zustand niemand sieht, hilft im Incident nur begrenzt.

### 20.5 Fallback auf Erfolg

Provider nicht erreichbar ≠ Operation erfolgreich.

### 20.6 Zu lange Timeouts

Ein 60-Sekunden-Timeout in einem synchronen Nutzerrequest ist oft nur ein langsamer Ausfall.

### 20.7 Annotation Stack ohne Verständnis

```java
@Retry
@CircuitBreaker
@TimeLimiter
@Bulkhead
```

ohne Test und ohne Reihenfolgenverständnis ist gefährlich.

### 20.8 Retry nicht-idempotenter Writes

Kann doppelte Zahlungen, doppelte Bestellungen oder doppelte E-Mails erzeugen.

### 20.9 Fallback ohne Fachentscheidung

Technischer Default ohne Produkt-/Fachentscheidung führt zu falschem Verhalten.

### 20.10 Resilience als Pflaster für schlechte Query

Timeouts begrenzen Schaden. Sie ersetzen keine Indexe, keine Query-Optimierung und kein Kapazitätsmanagement.

---

## 21. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Timeout | Hat jeder externe Call Connect-/Read-/Operation-Timeout? | RestClient Timeout | Pflicht |
| Retry | Werden nur transiente Fehler retryt? | 503, Timeout | Pflicht |
| Backoff/Jitter | Gibt es Backoff und Jitter? | exponential + randomized | Pflicht |
| Idempotenz | Ist Retry für Writes sicher? | Idempotency-Key | Pflicht |
| Circuit Breaker | Ist Fail-Fast für instabile Downstreams konfiguriert? | paymentProvider | Pflicht bei kritischen Downstreams |
| Bulkhead | Sind parallele Calls begrenzt? | max 10 Payment Calls | Pflicht bei riskanten Downstreams |
| Fallback | Ist Fallback fachlich korrekt? | pending statt success | Pflicht |
| Fehlerklassifikation | fachliche vs technische Fehler getrennt? | PaymentDeclined ignorieren | Pflicht |
| Metriken | Sind CB/Retry/Timeout sichtbar? | Micrometer/Grafana | Pflicht |
| Logging | Keine sensitiven Daten im Log? | orderId statt Payload | Pflicht |
| Tests | Gibt es Tests für Timeout, Retry, Open Circuit, Fallback? | Unit/Integration | Pflicht |
| SaaS | Tenant-Isolation auch im Fallback? | tenantbezogener Cache | Pflicht bei SaaS |
| Security | Auth-Fallback fail-safe? | deny statt allow | Pflicht |
| Konfiguration | zentral in YAML/Properties? | Resilience4j instances | Soll |
| Chaos | Kritische Flows experimentell geprüft? | Provider 503 | Soll |

---

## 22. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- HTTP-Client-Beans müssen explizite Timeouts konfigurieren.
- Externe Adapter dürfen nicht ohne Resilience-Konfiguration produktiv verwendet werden.
- Retry-Konfiguration muss max-attempts begrenzen.
- Retry-Konfiguration darf fachliche Exceptions nicht wiederholen.
- Payment-/Write-Operationen mit Retry müssen Idempotency-Key verwenden.
- Resilience4j-Metriken müssen im Actuator/Micrometer sichtbar sein.
- Keine Fallback-Methode darf fachlichen Erfolg vortäuschen.
- Controller dürfen keine externen Provider direkt aufrufen.
```

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule controllers_should_not_call_external_clients =
        noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter.out..")
                .because("Externe Aufrufe müssen über Use Cases und resiliente Adapter laufen.");
```

---

## 23. Migration bestehender Aufrufe

Migration erfolgt schrittweise:

1. Alle externen Aufrufe inventarisieren.
2. Kritikalität bewerten: Payment, Auth, DB, Mail, Analytics, Provider.
3. Timeouts ergänzen.
4. Fehlerklassifikation definieren.
5. Retry nur bei transienten Fehlern ergänzen.
6. Idempotenz für retrybare Writes sicherstellen.
7. Circuit Breaker für kritische Downstreams ergänzen.
8. Bulkhead bei ressourcenintensiven Abhängigkeiten ergänzen.
9. Fallback fachlich abstimmen.
10. Metriken und Logs ergänzen.
11. Tests ergänzen.
12. Chaos-/Staging-Experiment durchführen.
13. Werte nach Messdaten justieren.

Nicht alles gleichzeitig umbauen. Zuerst die Abhängigkeiten mit höchstem Risiko.

---

## 24. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Aufruf rein lokal und nicht blockierend ist,
- ein technischer Client selbst nachweislich Timeouts erzwingt,
- ein Provider fachlich keine Wiederholung erlaubt,
- ein synchroner Aufruf bewusst ohne Fallback fehlschlagen soll,
- Legacy-Code noch nicht migriert ist,
- ein Spike nicht produktiv betrieben wird.

Auch bei Ausnahmen gilt: Kein produktiver externer Aufruf ohne Timeout.

---

## 25. Definition of Done

Ein externer Aufruf erfüllt diese Richtlinie, wenn:

1. Connect Timeout gesetzt ist,
2. Read/Response Timeout gesetzt ist,
3. Operation Timeout oder TimeLimiter geprüft wurde,
4. Retry nur für transiente Fehler erfolgt,
5. Retry begrenzt ist,
6. Backoff und Jitter verwendet werden,
7. fachliche Fehler nicht retryt werden,
8. nicht-idempotente Operationen nicht automatisch wiederholt werden,
9. bei retrybaren Writes ein Idempotency-Key oder äquivalenter Schutz existiert,
10. Circuit Breaker für kritische Downstreams konfiguriert ist,
11. Bulkhead bei ressourcenintensiven Downstreams gesetzt ist,
12. Fallback fachlich korrekt ist,
13. Metriken für Circuit Breaker, Retry, Timeout und Bulkhead sichtbar sind,
14. Logs strukturiert und frei von sensitiven Daten sind,
15. Tests für Erfolg, Timeout, Retry, fachlichen Fehler, Open Circuit und Fallback existieren,
16. SaaS-/Tenant-Isolation im Fehler- und Fallbackfall erhalten bleibt,
17. Werte dokumentiert und anhand von SLO/Last/Downstream-Verhalten begründet sind.

---

## 26. Quellen und weiterführende Literatur

- Resilience4j Documentation — Getting Started Spring Boot: https://resilience4j.readme.io/docs/getting-started-3
- Resilience4j Documentation — Retry: https://resilience4j.readme.io/docs/retry
- Resilience4j Documentation — CircuitBreaker: https://resilience4j.readme.io/docs/circuitbreaker
- Resilience4j Documentation — Bulkhead: https://resilience4j.readme.io/docs/bulkhead
- Resilience4j Documentation — TimeLimiter: https://resilience4j.readme.io/docs/timeout
- Resilience4j Documentation — Micrometer: https://resilience4j.readme.io/docs/micrometer
- Spring Framework Javadoc — SimpleClientHttpRequestFactory: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/client/SimpleClientHttpRequestFactory.html
- Spring Framework Reference — REST Clients: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
- Spring Cloud Circuit Breaker — Resilience4j Metrics: https://docs.spring.io/spring-cloud-circuitbreaker/reference/spring-cloud-circuitbreaker-resilience4j/collecting-metrics.html
- Michael T. Nygard — Release It!, Design and Deploy Production-Ready Software
- Martin Fowler — Circuit Breaker: https://martinfowler.com/bliki/CircuitBreaker.html
- AWS Architecture Blog — Exponential Backoff and Jitter: https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/
