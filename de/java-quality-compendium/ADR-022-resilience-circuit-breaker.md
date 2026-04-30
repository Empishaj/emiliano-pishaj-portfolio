# ADR-022 — Resilience: Circuit Breaker, Retry & Timeout

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x · Resilience4j 2.x          |
| Datum      | 2024-09-23                                       |
| Kategorie  | Resilience / Architektur                          |

---

## Kontext & Problem

In verteilten Systemen scheitern externe Aufrufe. Ein Netzwerk-Timeout, ein überlasteter Downstream-Service, ein kurzer Datenbankausfall — all das ist nicht die Ausnahme, sondern der Normalfall. Ohne Resilience-Muster kaskadieren Fehler: ein langsamer Payment-Service hält Threads, die Threads erschöpfen den Thread-Pool, der gesamte Service fällt aus. Fail fast, recover gracefully.

---

## Regel 1 — Timeout: Immer, ohne Ausnahme

### Schlecht — kein Timeout

```java
// RestTemplate ohne Timeout — ein hängender Service blockiert den Thread für immer
RestTemplate restTemplate = new RestTemplate();

// WebClient ohne Timeout — reactive, aber trotzdem blockiert ohne Timeout
WebClient.builder()
    .baseUrl("https://payment-service")
    .build();

// Direkte JDBC ohne Query-Timeout
@Query("SELECT * FROM orders WHERE ...")  // Kann Minuten laufen!
List<Order> findAll();
```

### Gut — überall Timeouts konfigurieren

```java
// RestClient (Spring Boot 3.2+) mit Timeout
@Bean
public RestClient restClient() {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(2));  // TCP-Verbindung aufbauen
    factory.setReadTimeout(Duration.ofSeconds(5));     // Antwort lesen

    return RestClient.builder()
        .requestFactory(factory)
        .baseUrl("https://payment-service")
        .build();
}

// JPA Query-Timeout
@QueryHints(@QueryHint(name = HINT_SPEC_QUERY_TIMEOUT, value = "3000")) // 3 Sekunden
@Query("SELECT o FROM Order o WHERE ...")
List<Order> findExpensiveQuery();

// Global JPA Timeout
// spring.jpa.properties.jakarta.persistence.query.timeout=3000
```

---

## Regel 2 — Retry: Mit Backoff, nicht naiv

### Schlecht — naive Retry-Schleife

```java
// Ohne Backoff: bei 100 Instanzen feuern alle gleichzeitig → Thundering Herd
for (int i = 0; i < 3; i++) {
    try {
        return paymentGateway.charge(payment);
    } catch (Exception e) {
        // Sofort retry — überlastet den bereits überlasteten Service!
        Thread.sleep(100); // Festes Intervall, kein Jitter
    }
}
throw new PaymentException("Retry exhausted");
```

### Gut — Resilience4j Retry mit exponentialem Backoff

```java
// application.yml
// resilience4j:
//   retry:
//     instances:
//       paymentService:
//         max-attempts: 3
//         wait-duration: 500ms
//         enable-exponential-backoff: true
//         exponential-backoff-multiplier: 2   # 500ms, 1000ms, 2000ms
//         randomized-wait-factor: 0.3         # Jitter ±30% gegen Thundering Herd
//         retry-exceptions:
//           - java.net.SocketTimeoutException
//           - org.springframework.web.client.ResourceAccessException
//         ignore-exceptions:
//           - com.example.PaymentDeclinedException  # Fachliche Fehler NICHT retrien!

@Service
public class PaymentService {

    private final RetryRegistry retryRegistry;

    @Retry(name = "paymentService", fallbackMethod = "paymentFallback")
    public PaymentResult charge(PaymentCommand command) {
        return paymentGateway.charge(command);
    }

    // Fallback: aufgerufen wenn alle Retries erschöpft sind
    private PaymentResult paymentFallback(PaymentCommand command, Exception ex) {
        log.warn("Payment service unavailable, queuing for async retry", ex);
        paymentQueue.enqueue(command); // Async-Fallback statt direkter Fehler
        return PaymentResult.queued(command.orderId());
    }
}
```

---

## Regel 3 — Circuit Breaker: Fail Fast statt Fail Slow

```java
// application.yml
// resilience4j:
//   circuit-breaker:
//     instances:
//       paymentService:
//         sliding-window-type: COUNT_BASED
//         sliding-window-size: 10          # Letzte 10 Calls beobachten
//         failure-rate-threshold: 50       # Öffnen ab 50% Fehlerrate
//         wait-duration-in-open-state: 30s # 30s warten bevor HALF_OPEN
//         permitted-calls-in-half-open-state: 3  # 3 Probe-Calls
//         slow-call-duration-threshold: 3s  # Alles > 3s gilt als "slow"
//         slow-call-rate-threshold: 80      # Öffnen ab 80% slow calls

@Service
public class PaymentService {

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService") // Reihenfolge: Retry innerhalb CircuitBreaker
    @TimeLimiter(name = "paymentService") // Timeout
    public CompletableFuture<PaymentResult> chargeAsync(PaymentCommand command) {
        return CompletableFuture.supplyAsync(() -> paymentGateway.charge(command));
    }

    private CompletableFuture<PaymentResult> paymentFallback(
            PaymentCommand command, CallNotPermittedException ex) {
        // Circuit ist offen — sofort Fallback ohne den Service zu belasten
        log.warn("Circuit OPEN for payment service — failing fast for order {}",
            command.orderId());
        return CompletableFuture.completedFuture(PaymentResult.circuitOpen());
    }
}
```

```
Circuit Breaker Zustandsmaschine:

CLOSED ──(50% Fehler)──→ OPEN ──(30s)──→ HALF_OPEN ──(Erfolg)──→ CLOSED
  ↑                                              │
  └──────────────────(Fehler)────────────────────┘
```

---

## Regel 4 — Bulkhead: Ressourcen isolieren

```java
// Bulkhead: Limiting concurrent calls — verhindert dass ein langsamer Service
// alle Threads aufbraucht und andere Services mitreißt
// resilience4j:
//   bulkhead:
//     instances:
//       paymentService:
//         max-concurrent-calls: 10  # Maximal 10 gleichzeitige Payment-Calls
//         max-wait-duration: 100ms  # 100ms warten auf freien Slot

@Bulkhead(name = "paymentService", type = Bulkhead.Type.SEMAPHORE)
public PaymentResult charge(PaymentCommand command) {
    return paymentGateway.charge(command); // Max 10 gleichzeitig
}
```

---

## Regel 5 — Resilience kombinieren: Dekorator-Reihenfolge

```java
// Korrekte Reihenfolge der Resilience-Dekoratoren (außen → innen):
// TimeLimiter → CircuitBreaker → Retry → Bulkhead → eigentlicher Call

// Deklarativ via Annotationen (Spring AOP, Reihenfolge beachten):
@TimeLimiter(name = "service")    // 1. Äußerster: Gesamttimeout
@CircuitBreaker(name = "service") // 2. Fail fast wenn offen
@Retry(name = "service")          // 3. Retry innerhalb des Circuits
@Bulkhead(name = "service")       // 4. Innerster: Concurrency-Limit
public CompletableFuture<Result> call() { ... }
```

---

## Resilience im Test prüfen

```java
@Test
void charge_returnsFallback_whenCircuitIsOpen() {
    // Circuit manuell öffnen
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentService");
    circuitBreaker.transitionToOpenState();

    var result = paymentService.charge(new PaymentCommand(...));

    assertThat(result.status()).isEqualTo(CIRCUIT_OPEN);
    assertThat(result.retryScheduled()).isTrue();
}

@Test
void charge_retriesOnTimeout_andSucceedsOnThirdAttempt() {
    // Erste zwei Calls: Timeout, dritter: Erfolg
    when(paymentGateway.charge(any()))
        .thenThrow(new SocketTimeoutException("timeout"))
        .thenThrow(new SocketTimeoutException("timeout"))
        .thenReturn(PaymentResult.success("tx-123"));

    var result = paymentService.charge(new PaymentCommand(...));

    assertThat(result.transactionId()).isEqualTo("tx-123");
    verify(paymentGateway, times(3)).charge(any());
}
```

---

## Konsequenzen

**Positiv:** Circuit Breaker verhindert Kaskadenausfälle. Retry mit Backoff und Jitter verhindert Thundering-Herd-Effekte. Fallbacks ermöglichen degradierten Betrieb statt vollständigem Ausfall.

**Negativ:** Mehr Konfigurationsparameter die verstanden und getestet werden müssen. Falsch konfiguriertes Retry kann Downstream-Services überlasten (kein Jitter, zu kurze Intervalle).

---

## Tipps

- **Idempotenz bei Retry**: Nur idempotente Operationen dürfen automatisch retryed werden (GET, DELETE, idempotente POST mit Idempotency-Key — → ADR-021).
- **Fachliche Fehler nicht retrien**: `PaymentDeclinedException` ist kein transienter Fehler — Retry ändert nichts, nervt nur den Upstream.
- **Resilience4j Metrics** sind automatisch in Micrometer integriert — Circuit-Breaker-Status ist in Grafana sichtbar (→ ADR-017).
- **Chaos Engineering**: Resilience-Verhalten mit Chaos Monkey for Spring Boot testen.
