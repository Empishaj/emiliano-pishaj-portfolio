# ADR-096 — Test-Strategie: Das Gesamtbild — Pyramide, Trophäe, Kosten

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / alle Tech Leads                           |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Testing-Strategie · Qualitätssicherung                        |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| Abhängigkeiten    | ADR-010–020 (alle Test-ADRs), ADR-051 (DoD), ADR-061 (Fitness)|

---

## 1. Kontext & Treiber

### 1.1 Das Problem ohne Teststrategie

```
IN PROJEKTEN OHNE EXPLIZITE TEST-STRATEGIE:

Team A: "Wir schreiben nur Unit-Tests, Integration ist Aufwand."
→ 85% Unit-Tests, alles grün, Produktion: Konfiguration fehlt → 503

Team B: "Wir vertrauen nur E2E-Tests, die testen wirklich."
→ 5% Unit, 95% E2E, Suite dauert 45 Minuten, flaky 30% der Zeit
→ Entwickler pushen ohne Tests lokal — zu langsam

Team C: "Wir mocken alles weg."
→ 100% Unit-Tests mit Mocks, Coverage 95%
→ Produktion: die nicht-gemockte Wirklichkeit bricht alles

ERGEBNIS OHNE STRATEGIE:
  → Jedes Team entscheidet selbst → kein konsistentes Qualitätsniveau
  → False Confidence: hohe Coverage ≠ echte Sicherheit
  → Feedback-Loop zu lang oder zu flaky → Tests werden ignoriert
```

### 1.2 Was eine Test-Strategie leisten muss

```
Eine gute Test-Strategie definiert:
  ① WAS auf welcher Ebene getestet wird (Verantwortlichkeiten)
  ② WIEVIEL jede Ebene kostet (Zeit, Wartung)
  ③ WANN Tests laufen (lokal, CI, täglich)
  ④ WIE viele Tests auf welcher Ebene erwartet werden
  ⑤ WAS tatsächliche Qualität misst (nicht nur Coverage)
```

---

## 2. Die Test-Pyramide: Das Original-Modell (Mike Cohn, 2009)

```
                        ▲
                       /E\
                      / E \
                     / 2  \
                    /  E  \           E2E-Tests
                   /   T   \          • Selten, langsam, teuer
                  /─────────\         • Confidence: hoch, aber fragil
                 / Integration\
                /   Tests      \      Integrations-Tests
               /────────────────\     • Mittel, Minuten, stabil
              /    Unit Tests    \
             /____________________\   Unit-Tests
                                       • Viele, schnell, billig, isoliert

PROPORTIONEN (original):
  Unit:        70%
  Integration: 20%
  E2E:         10%

REALITÄTS-CHECK:
  Diese Proportionen gelten für klassische Schichtenarchitektur.
  Bei DDD/Hexagonal/Modulith verschiebt sich das.
```

---

## 3. Die Test-Trophäe (Kent C. Dodds, 2019): Moderneres Modell

```
         ┌─────────────────┐
         │   E2E / System  │   10%
         │      Tests      │   (kritische User-Journeys)
         ├─────────────────┤
         │   Integration   │   40%   ← SCHWERPUNKT
         │     Tests       │   (Slices, DB, Messaging)
         ├─────────────────┤
         │   Unit Tests    │   40%
         │  (Domain-Logik) │   (reine Business-Regeln)
         ├─────────────────┤
         │ Static Analysis │   10%
         │ (ArchUnit etc.) │   (immer, fast kostenlos)
         └─────────────────┘

WARUM DIE TROPHÄE FÜR UNSER SYSTEM BESSER PASST:
  Integration-Tests mit Testcontainers (→ ADR-018) sind:
  → Schnell genug (Sekunden bis wenige Minuten)
  → Realistisch (echte DB, echte Kafka)
  → Stabil (kein Mocking von Infrastruktur)
  
  Sie testen was Unit-Tests nicht können:
  → SQL-Queries (Indizes, Constraints)
  → Spring-Konfiguration (Security, Transaktionen)
  → JPA-Mapping
```

---

## 4. Test-Ebenen: Was wird wo getestet

### 4.1 Ebene 1 — Statische Analyse (immer, 0 Sekunden)

```java
// LÄUFT: bei jedem Commit, in < 1 Sekunde
// PRÜFT: Architektur-Invarianten, Code-Stil, Typsicherheit

// ① ArchUnit (→ ADR-061): strukturelle Regeln
@ArchTest
static final ArchRule domain_kennt_kein_framework = /* ... */;

// ② Checkstyle: Code-Stil
// ③ SpotBugs/SAST: bekannte Bug-Patterns
// ④ Compiler-Warnungen als Fehler: -Xlint:unchecked
// ⑤ Spring Modulith Verify (→ ADR-079): Modul-Grenzen

// KOSTEN: ~5 Sekunden Build-Zeit
// WERT: verhindert Architektur-Regression bei jedem Commit
```

### 4.2 Ebene 2 — Unit-Tests (Domain-Logik)

```java
// LÄUFT: lokal (immer), CI (immer), in < 30 Sekunden
// PRÜFT: Business-Logik, Algorithmen, Domain-Regeln
// MOCKT: Ports/Interfaces (nicht konkrete Infrastruktur)

// ✅ GUT: Unit-Test für Domain-Logik
class OrderTest {

    @Test
    void total_summiertAlleItemSubtotals() {
        var order = Order.builder()
            .item(new OrderItem(PRODUCT_A, Quantity.of(2), Money.of("10.00", EUR)))
            .item(new OrderItem(PRODUCT_B, Quantity.of(1), Money.of("15.00", EUR)))
            .build();

        assertThat(order.total()).isEqualTo(Money.of("35.00", EUR));
        // Kein Mock, kein Spring — reines Java, in < 1ms
    }

    @Test
    void cancel_wirftException_wennBereitsVersendet() {
        var order = Order.inStatus(SHIPPED);

        assertThatThrownBy(() -> order.cancel("Kundenwunsch"))
            .isInstanceOf(OrderCannotBeCancelledException.class)
            .hasMessageContaining("SHIPPED");
        // Domain-Regel: kein Storno nach Versand
    }
}

// ❌ SCHLECHT: Unit-Test der Infrastruktur testet (sinnlos)
class OrderServiceTest {
    @Mock OrderRepository repo;        // Mock!
    @Mock PaymentGateway gateway;      // Mock!
    @Mock EventPublisher publisher;    // Mock!

    @Test
    void placeOrder_callsSaveOnRepository() {
        orderService.placeOrder(cmd);
        verify(repo).save(any(Order.class));  // Testet nur: "wurde save() aufgerufen"
        // KEIN echter Business-Wert — testet Implementierungsdetail, nicht Verhalten
    }
}
// Dieser Test bricht bei JEDEM Refactoring, schützt vor NICHTS.
// Domain-Service-Tests: Integration besser als Unit (mit echter DB).
```

**Was Unit-Tests testen SOLLEN:**
```
✅ Domain-Aggregates (Order, Payment, User)
✅ Value Objects (Money, Quantity, Email)
✅ Domain Services (TaxCalculator, PricingStrategy)
✅ Algorithmen (Sortierung, Berechnungsformeln)
✅ Zustandsübergänge (OrderStatus-Machine)

❌ Was Unit-Tests NICHT testen sollen:
❌ Spring-Konfiguration
❌ SQL-Queries
❌ JSON-Serialisierung
❌ HTTP-Status-Codes
❌ Transaktionsgrenzen
→ Für all das: Integration/Slice-Tests
```

### 4.3 Ebene 3 — Slice-Tests / Integration-Tests (der Schwerpunkt)

```java
// LÄUFT: lokal (optional), CI (immer), in 1–5 Minuten
// PRÜFT: Adapter-Integration (DB, HTTP, Messaging), Spring-Kontext
// NUTZT: Testcontainers (echte DB, echter Kafka → ADR-018)

// ① @DataJpaTest: NUR JPA, echte DB (→ ADR-020)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Testcontainers
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired OrderJpaRepository repository;

    @Test
    void findByUserId_findetNurBestellungenDesNutzers() {
        var user1Orders = List.of(
            saveOrder("user-1", PENDING),
            saveOrder("user-1", CONFIRMED)
        );
        saveOrder("user-2", PENDING);  // Anderer User — darf nicht auftauchen

        var result = repository.findByUserId("user-1", PageRequest.of(0, 10));

        assertThat(result.getContent())
            .hasSize(2)
            .extracting(OrderEntity::getUserId)
            .containsOnly("user-1");
    }

    @Test
    void speichern_ohneUserId_wirftConstraintViolation() {
        var entity = new OrderEntity();
        entity.setUserId(null);  // NOT NULL Constraint in DB!

        assertThatThrownBy(() -> repository.saveAndFlush(entity))
            .isInstanceOf(ConstraintViolationException.class);
        // Testet echte DB-Constraints — Unit-Test könnte das nicht!
    }
}

// ② @WebMvcTest: NUR Controller-Layer (→ ADR-020)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mvc;
    @MockBean  PlaceOrderUseCase useCase;  // Service gemockt — wir testen nur HTTP-Layer

    @Test
    void placeOrder_gibtLocation_HeaderZurueck() throws Exception {
        when(useCase.execute(any())).thenReturn(new OrderCreatedResult("ord-123", Money.of("50.00", EUR)));

        mvc.perform(post("/api/v2/orders")
                .contentType(APPLICATION_JSON)
                .content(validOrderJson()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/orders/ord-123")))
            .andExpect(jsonPath("$.orderId").value("ord-123"));
    }

    @Test
    void placeOrder_ohneItems_gibt422Zurueck() throws Exception {
        mvc.perform(post("/api/v2/orders")
                .contentType(APPLICATION_JSON)
                .content("{\"items\": []}"))  // Leere Items-Liste
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.violations").isArray());
        // Testet: Bean Validation, GlobalExceptionHandler (→ ADR-086)
    }
}

// ③ @SpringBootTest: Vollständiger Kontext für kritische Flows (→ ADR-020)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class OrderPlacementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = /* ... */;
    @Container
    static KafkaContainer kafka = /* ... */;

    @Autowired TestRestTemplate http;

    @Test
    void vollständigerBestellflow_vonHTTPBisKafka() {
        // HTTP-Request
        var response = http.postForEntity("/api/v2/orders", validOrderRequest(), OrderDto.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        var orderId = response.getBody().orderId();

        // DB: Order wirklich angelegt?
        await().atMost(5, SECONDS).untilAsserted(() ->
            assertThat(orderRepository.findById(orderId)).isPresent());

        // Kafka: Event publiziert?
        await().atMost(10, SECONDS).untilAsserted(() ->
            assertThat(kafkaConsumer.poll(Duration.ofSeconds(1))
                .records("orders.placed"))
                .anyMatch(r -> r.key().equals(orderId)));
    }
}
```

### 4.4 Ebene 4 — Contract-Tests (zwischen Services)

```java
// LÄUFT: CI, bei Vertrags-Änderungen
// PRÜFT: API-Verträge zwischen Consumer und Producer (→ ADR-019)
// GARANTIERT: Consumer wird nicht durch Producer-Änderung gebrochen

// Consumer definiert Erwartung (Pact)
@ExtendWith(PactConsumerTestExt.class)
class FulfillmentConsumesOrderEventsTest {

    @Pact(consumer = "fulfillment-service", provider = "order-service")
    MessagePact orderPlacedEvent(MessagePactBuilder builder) {
        return builder
            .expectsToReceive("an OrderPlacedEvent")
            .withMetadata(Map.of("contentType", "application/json"))
            .withContent(new PactDslJsonBody()
                .stringType("orderId")
                .stringType("customerId")
                .minArrayLike("items", 1, item ->
                    item.stringType("productId")
                        .integerType("quantity")))
            .toPact();
    }
}
```

### 4.5 Ebene 5 — E2E-Tests (kritische User-Journeys)

```java
// LÄUFT: CI (auf Staging), täglich oder bei Releases
// PRÜFT: End-to-End User-Journey durch alle Services
// TOOLS: Playwright (Frontend), REST-Assured (API)

// NUR die wichtigsten Happy Paths!
// E2E ist teuer — 3-5 kritische Journeys, nicht alle Features

@Test
@DisplayName("Kritische User-Journey: Bestellung aufgeben und bezahlen")
void kritischeJourney_BestellungUndZahlung() {
    // Given: eingeloggter Nutzer
    var token = authService.loginAs("test@example.com", "password");

    // When: Bestellung aufgeben
    var orderResponse = RestAssured
        .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(validOrderPayload())
        .when()
            .post("/api/v2/orders")
        .then()
            .statusCode(201)
            .extract().as(OrderCreatedDto.class);

    // Then: Bestellung existiert und hat korrekten Status
    await().atMost(30, SECONDS).untilAsserted(() ->
        RestAssured.given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/v2/orders/" + orderResponse.orderId())
        .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED")));
}
```

---

## 5. Kosten-Vergleich: Was Tests kosten

```
TEST-TYP        SCHREIBEN   AUSFÜHREN   WARTUNG    FEEDBACK   KONFIDENZ
────────────────────────────────────────────────────────────────────────────
Static Analysis  gering      <5s         keine      sofort     mittel
Unit (Domain)    mittel      <1ms/Test   gering     sofort     hoch (für Logik)
Slice (@Data...) mittel      2-10s       mittel     1 Min      hoch (für Layer)
Integration      hoch        30s-2min    mittel     3-5 Min    sehr hoch
Contract         mittel      30s         gering     5 Min      sehr hoch (für API)
E2E              sehr hoch   5-30 Min    sehr hoch  20-60 Min  hoch (fragil!)

OPTIMALE VERTEILUNG (our system):
  Static Analysis: 100% der Klassen (ArchUnit, Checkstyle)
  Unit (Domain):   ~200-400 Tests pro Service
  Slice:           ~100-200 Tests pro Service
  Integration:     ~20-50 Tests pro Service (kritische Flows)
  Contract:        1-3 Contracts pro Producer
  E2E:             3-5 kritische User-Journeys
```

---

## 6. Testbarkeit als Design-Kriterium

```java
// THESE: Wenn Code schwer zu testen ist, ist das Design-Problem, kein Test-Problem.

// ❌ SCHLECHT — schwer testbar (zu viele Abhängigkeiten, kein Interface)
@Service
public class OrderService {
    @Autowired private JdbcTemplate jdbc;        // konkret, schwer mockbar
    @Autowired private StripeClient stripe;      // extern, teuer
    @Autowired private EmailService email;       // Seiteneffekt
    @Autowired private AuditLogger audit;        // Querschnitt

    public void place(PlaceOrderCmd cmd) {
        // alles zusammen — nicht isolierbar
    }
}
// Test braucht: JDBC-Verbindung, Stripe-Mock, Email-Mock, Audit-Mock
// → Integration-Test wird zum Unit-Test-Ersatz

// ✅ GUT — testfreundliches Design (Domain isoliert, Ports klar)
@Service
public class OrderDomainService {
    // Nur Ports (Interfaces) — einfach mockbar oder In-Memory-Implementierung
    private final OrderRepository repository;    // Interface
    private final PaymentGateway  gateway;       // Interface
    private final EventPublisher  events;        // Interface

    // Domain-Logik ISOLIERT testbar:
    // → kein DB, kein Stripe, kein Email nötig
    // → 3 einfache Mocks oder In-Memory-Implementierungen
}
```

---

## 7. Qualitäts-Metriken: Was wirklich zählt

```
SCHLECHTE METRIKEN (führen zu falschen Anreizen):
  ❌ "Code-Coverage > 80%"
     → Developers schreiben Tests die Coverage erhöhen ohne zu prüfen
     → Triviale Getter/Setter mit 100% Coverage, komplexe Logik ungetestad

  ❌ "Anzahl Tests > 1000"
     → Menge sagt nichts über Qualität

GUTE METRIKEN:
  ✅ Mutation Score > 80% (→ ADR-045)
     → Misst ob Tests tatsächlich Fehler finden
     
  ✅ Test-Feedback-Zeit < 5 Minuten (lokale Suite)
     → Wenn zu langsam: Tests werden übersprungen
     
  ✅ Flaky-Test-Rate < 1%
     → Instabile Tests werden ignoriert → kein Vertrauen
     
  ✅ Anzahl Production-Bugs die Tests nicht gefunden haben
     → Die ehrlichste Metrik: was hat unser Test-System übersehen?
     
  ✅ Mean Time to Red (MTR): wie schnell zeigt CI einen Fehler?
     → < 10 Minuten ist das Ziel
```

---

## 8. Test-Pyramide in unserem Stack

```
UNSER JAVA 21 / SPRING BOOT / MODULITH STACK:

Statische Analyse  ─────────── ArchUnit, Checkstyle, SpotBugs, Modulith Verify
                                ↑ Läuft bei jedem Compile

Unit Tests         ─────────── JUnit 5 + AssertJ + Mockito
(Domain-Logik)                  ↑ Reine Java-Objekte, kein Spring
                                ↑ ADR-010 bis ADR-014, ADR-052 (Immutability)

Slice Tests        ─────────── @DataJpaTest, @WebMvcTest, @JsonTest
(Adapter-Layer)                 ↑ Ein Layer, minimaler Spring-Context
                                ↑ ADR-020

Integration Tests  ─────────── @SpringBootTest + Testcontainers
(Kritische Flows)               ↑ Echter PostgreSQL, echte Kafka
                                ↑ ADR-018, ADR-020

Contract Tests     ─────────── Pact Consumer-Driven Contracts
(API-Verträge)                  ↑ ADR-019

Modul-Tests        ─────────── @ApplicationModuleTest (Spring Modulith)
(Modulith-Grenzen)              ↑ ADR-079

E2E Tests          ─────────── REST-Assured gegen Staging
(User-Journeys)                 ↑ Nur kritische Happy Paths

Performance        ─────────── Gatling (→ ADR-044) gegen Staging
                                ↑ Nur auf main-Branch

Mutation Testing   ─────────── PIT (→ ADR-045) nightly
                                ↑ Validiert Test-Qualität
```

---

## 9. Akzeptanzkriterien

- [ ] Test-Suite läuft lokal in < 3 Minuten (Unit + Slices)
- [ ] CI-Pipeline: Unit + Integration < 10 Minuten
- [ ] Mutation Score > 80% für Domain-Pakete (gemessen wöchentlich)
- [ ] Flaky-Test-Rate < 1% (gemessen über 30 Pipeline-Runs)
- [ ] E2E-Suite: ≤ 5 kritische User-Journeys, läuft in < 20 Minuten
- [ ] Kein neues Feature ohne Slice-Test für den Controller und Domain-Unit-Test
- [ ] Definition of Done (→ ADR-051) enthält Test-Anforderungen für jede Ebene

---

## Quellen & Referenzen

- **Mike Cohn, "Succeeding with Agile" (2009)** — Ursprung der Test-Pyramide.
- **Kent C. Dodds, "The Testing Trophy" (2019)** — Modernisierte Verteilung; Integration-Tests als Schwerpunkt.
- **Martin Fowler, "Test Pyramid" (2012)** — Formalisierung und Industriestandard-Erklärung.
- **Vladimir Khorikov, "Unit Testing: Principles, Practices, and Patterns" (2020)** — Was Unit-Tests testen SOLLEN (Domain-Logik, nicht Implementierung).
- **Gojko Adzic, "Specification by Example" (2011)** — Tests als lebende Dokumentation.

---

## Verwandte ADRs

- [ADR-010–014](ADR-010-junit-grundlagen-struktur.md) — Unit-Test-Implementierung
- [ADR-018–020](ADR-018-integrationstests-testcontainers.md) — Integration/Slice-Tests
- [ADR-045](ADR-045-mutation-testing-pit.md) — Mutation Testing als Qualitätsmetrik
- [ADR-051](ADR-051-definition-of-done.md) — DoD integriert diese Test-Strategie
