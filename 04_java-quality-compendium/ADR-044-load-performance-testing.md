# ADR-044 — Load & Performance Testing mit Gatling

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · Gatling 3.x · Spring Boot 3.x          |
| Datum      | 2024-01-01                                  |
| Kategorie  | Testing / Performance                       |

---

## Kontext & Problem

Unit-Tests und Integrationstests zeigen dass der Code korrekt ist. Load-Tests zeigen ob er schnell genug ist. Ohne Load-Tests wird Performance erst in Produktion unter Last entdeckt. Gatling ist das Java/Scala-Ökosystem-Standard für Load-Tests.

---

## Grundkonzepte: Was wird gemessen?

```
Latenz:          Wie lange dauert eine Anfrage? (P50, P95, P99)
Throughput:      Wie viele Anfragen pro Sekunde?
Error Rate:      Wie viele Anfragen schlagen fehl?
Resource Usage:  CPU, Memory, DB-Connections unter Last

SLO-Beispiele (Service Level Objectives):
- P95-Latenz < 500ms bei 100 RPS
- P99-Latenz < 1000ms
- Error Rate < 0.1%
- 0 DB-Connection-Timeouts bis 200 RPS
```

---

## Gatling-Simulation (Java DSL)

```java
// src/test/java/com/example/performance/OrderApiSimulation.java
public class OrderApiSimulation extends Simulation {

    // ── HTTP-Protokoll ───────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .header("Authorization", "Bearer " + loadTestToken());

    // ── Szenarien ─────────────────────────────────────────────────
    private final ScenarioBuilder browseCatalog = scenario("Browse Catalog")
        .exec(
            http("Get Products")
                .get("/api/v1/products?page=0&size=20")
                .check(status().is(200))
                .check(jsonPath("$.content").exists())
                .check(responseTimeInMillis().lte(500)) // SLO: < 500ms
        );

    private final ScenarioBuilder placeOrder = scenario("Place Order")
        .exec(
            http("Create Order")
                .post("/api/v1/orders")
                .body(StringBody("""
                    {
                      "items": [{"productId": "P1", "quantity": 2}],
                      "shippingAddress": {
                        "street": "Teststraße 1",
                        "city": "Berlin",
                        "zipCode": "10115"
                      }
                    }
                    """))
                .check(status().is(201))
                .check(header("Location").exists())
                .check(responseTimeInMillis().lte(1000)) // SLO: < 1000ms
        );

    // ── Last-Profile ───────────────────────────────────────────────
    {
        // Load-Test: normale Last
        setUp(
            browseCatalog.injectOpen(
                rampUsers(50).during(30),        // Rampe: 0→50 in 30s
                constantUsersPerSec(50).during(60) // Konstant: 50 RPS für 60s
            ),
            placeOrder.injectOpen(
                rampUsers(20).during(30),
                constantUsersPerSec(20).during(60)
            )
        )
        .protocols(httpProtocol)
        .assertions(
            // Globale SLOs — schlägt die Pipeline fehl wenn verletzt
            global().responseTime().percentile(95).lte(500),
            global().responseTime().percentile(99).lte(1000),
            global().failedRequests().percent().lte(1.0), // < 1% Fehlerrate
            forAll().failedRequests().percent().lte(5.0)
        );
    }
}
```

---

## Verschiedene Last-Profile

```java
// 1. Smoke Test: minimale Last, prüft ob überhaupt funktioniert
setUp(scenario.injectOpen(atOnceUsers(1)));

// 2. Load Test: normale erwartete Last
setUp(scenario.injectOpen(
    rampUsers(100).during(Duration.ofMinutes(2)),
    constantUsersPerSec(50).during(Duration.ofMinutes(5))
));

// 3. Stress Test: Last bis zum Bruch — wo ist der Knackpunkt?
setUp(scenario.injectOpen(
    stressPeakUsers(500).during(Duration.ofSeconds(20))
));

// 4. Spike Test: plötzlicher Lastanstieg (Flash Sale, virales Event)
setUp(scenario.injectOpen(
    constantUsersPerSec(10).during(Duration.ofMinutes(5)),  // Normal
    atOnceUsers(200),                                         // Spike!
    constantUsersPerSec(10).during(Duration.ofMinutes(5))   // Normal
));

// 5. Soak Test: normale Last über lange Zeit (Memory-Leaks entdecken)
setUp(scenario.injectOpen(
    constantUsersPerSec(30).during(Duration.ofHours(1))
));
```

---

## Gradle Integration

```kotlin
// build.gradle.kts
plugins {
    id("io.gatling.gradle") version "3.10.3"
}

gatling {
    jvmArgs = listOf(
        "-server",
        "-Xmx1g",
        "-XX:+UseG1GC"
    )
}

tasks.register("performanceTest") {
    group = "verification"
    description = "Runs performance tests against staging"
    dependsOn("gatlingRun")
    // Nur auf Staging-Umgebung ausführen, nicht lokal
}
```

---

## Performance-Baseline: Messung vor und nach Änderung

```java
// Anmerkungen im Code wenn Performance-relevante Änderung gemacht wird
// ADR-044: Performance-Baseline vor Refactoring: P95=120ms bei 50 RPS
// ADR-044: Nach Query-Optimierung (ADR-016): P95=45ms bei 50 RPS (-63%)

// JMH Micro-Benchmarks für einzelne Methoden
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class OrderTotalBenchmark {

    private Order order;

    @Setup
    public void setup() {
        order = createOrderWith1000Items();
    }

    @Benchmark
    public Money calculateTotal() {
        return order.total(); // Wie schnell ist die Total-Berechnung?
    }
}
// Ausführen: ./gradlew jmh
```

---

## Konsequenzen

**Positiv:** Performance-Regression wird vor Produktion erkannt. SLO-Verletzungen schlagen die Pipeline fehl. Soak-Tests entdecken Memory-Leaks.

**Negativ:** Load-Tests brauchen eine separate Umgebung (Staging) — nicht gegen Produktion. Setup von realistischen Testdaten aufwändig.

---

## Tipps

- **Immer P95 und P99 messen**, nicht nur Durchschnitt — Durchschnitt versteckt Ausreißer.
- **Isolierte Umgebung**: Load-Tests gegen Staging, niemals Produktion.
- **Datenbank-State beachten**: Load-Test mit leerem Cache ergibt andere Ergebnisse als warmer Cache.
- **Bottleneck identifizieren**: Gatling zeigt Symptome. Ursache: JFR (JVM), EXPLAIN ANALYZE (DB), Jaeger (Traces).
