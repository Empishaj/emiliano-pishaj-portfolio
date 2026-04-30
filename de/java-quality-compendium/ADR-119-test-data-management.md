# ADR-119 — Test Data Management: Builder, Fixtures & Object Mothers

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / alle Entwickler                           |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Testing · Test-Qualität · Wartbarkeit                         |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| Abhängigkeiten    | ADR-096 (Test-Strategie), ADR-010-014 (JUnit/Testing)         |

---

## 1. Das Problem mit schlechten Testdaten

```java
// ❌ HÄUFIGSTES ANTI-PATTERN: Test-Objekte inline erstellen

@Test
void bestellung_wird_korrekt_berechnet() {
    // 15 Zeilen nur um ein Order-Objekt zu bauen...
    var customer = new Customer();
    customer.setId(1L);
    customer.setFirstName("Max");
    customer.setLastName("Mustermann");
    customer.setEmail("max@test.de");
    customer.setCountry("DE");

    var product = new Product();
    product.setId(100L);
    product.setName("T-Shirt");
    product.setPrice(new BigDecimal("19.99"));
    product.setVatRate(new BigDecimal("0.19"));

    var item = new OrderItem();
    item.setProduct(product);
    item.setQuantity(2);

    var order = new Order();
    order.setCustomer(customer);
    order.setItems(List.of(item));

    // ... und dann erst der eigentliche Test:
    assertThat(order.calculateTotal()).isEqualByComparingTo("47.58");
}

// PROBLEME:
// ① Wenn Customer ein neues Pflichtfeld bekommt → ALLE Tests brechen
// ② Irrelevante Details (E-Mail, Country) lenken vom Test-Fokus ab
// ③ Kein "was ist das Default-Customer für Tests?" → Duplikation
// ④ Test liest sich nicht wie eine Spezifikation
```

---

## 2. Entscheidung

Testdaten werden über drei Muster verwaltet:
- **Test-Builder** (Java Records + Builder): typsichere, fluent Objekt-Erstellung
- **Object Mother**: vordefinierte, benannte Testobjekte für häufige Szenarien
- **Fixtures**: persistente Testdaten via SQL/Flyway für Integrationstests

---

## 3. Test-Builder: Das wichtigste Muster

```java
// Testdaten-Builder: fluent, minimal, fokussiert

// SCHRITT 1: Builder-Klasse definieren (in src/test/java!)
public class OrderTestBuilder {

    // Sensible Defaults: alle Felder haben gültige Werte
    private OrderId    id         = new OrderId(999L);
    private CustomerId customerId = new CustomerId(1L);
    private OrderStatus status    = OrderStatus.CONFIRMED;
    private Money       total     = Money.of("49.99", EUR);
    private List<OrderItem> items = List.of(
        new OrderItem(new ProductId(100L), Quantity.of(1), Money.of("49.99", EUR))
    );
    private Instant     createdAt = Instant.now();

    // Nur die Felder überschreiben die für den Test relevant sind
    public OrderTestBuilder withId(Long id) {
        this.id = new OrderId(id);
        return this;
    }

    public OrderTestBuilder withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestBuilder withTotal(String amount) {
        this.total = Money.of(new BigDecimal(amount), EUR);
        return this;
    }

    public OrderTestBuilder withCustomerId(Long id) {
        this.customerId = new CustomerId(id);
        return this;
    }

    public OrderTestBuilder withItems(OrderItem... items) {
        this.items = List.of(items);
        return this;
    }

    public Order build() {
        return new Order(id, customerId, status, total, items, createdAt);
    }

    // Convenience: direkt als Record verfügbar
    public static OrderTestBuilder anOrder() {
        return new OrderTestBuilder();
    }
}

// SCHRITT 2: Im Test — präzise, lesbar, wartbar
@Test
void stornierung_ist_nicht_moeglich_wenn_bereits_versendet() {
    // Nur das Relevante angeben — alles andere: Defaults
    var order = anOrder()
        .withStatus(OrderStatus.SHIPPED)
        .build();

    assertThatThrownBy(() -> order.cancel("Kundenwunsch"))
        .isInstanceOf(OrderCannotBeCancelledException.class);
}

@Test
void rabatt_wird_korrekt_auf_gesamtbetrag_angewendet() {
    var order = anOrder()
        .withTotal("100.00")
        .build();

    var discounted = order.applyDiscount(new Discount(10));  // 10%

    assertThat(discounted.total()).isEqualByComparingTo("90.00");
}
// Kein Rauschen mehr. Test liest sich wie Spezifikation.
```

---

## 4. Object Mother: Benannte Testszenarien

```java
// Object Mother: vordefinierte, bedeutungsvolle Test-Objekte
// Jede Methode beschreibt ein klares Szenario

public class OrderFixtures {

    // Standard-Bestellung für einfache Tests
    public static Order confirmedOrder() {
        return anOrder()
            .withStatus(CONFIRMED)
            .withTotal("49.99")
            .build();
    }

    // Große Bestellung die Genehmigung braucht (Businessregel > 10.000 EUR)
    public static Order largeOrderRequiringApproval() {
        return anOrder()
            .withStatus(PENDING_APPROVAL)
            .withTotal("15000.00")
            .build();
    }

    // Bestellung die storniert werden kann
    public static Order cancellableOrder() {
        return anOrder()
            .withStatus(PENDING_PAYMENT)
            .build();
    }

    // Bestellung die NICHT storniert werden kann
    public static Order shippedOrder() {
        return anOrder()
            .withStatus(SHIPPED)
            .build();
    }
}

// Verwendung: test liest sich wie eine Geschichte
@Test
void große_bestellungen_erfordern_manuelle_genehmigung() {
    var order = largeOrderRequiringApproval();
    assertThat(order.requiresApproval()).isTrue();
}

@Test
void versendete_bestellung_kann_nicht_storniert_werden() {
    var order = shippedOrder();
    assertThatThrownBy(() -> order.cancel("Test"))
        .isInstanceOf(OrderCannotBeCancelledException.class);
}
```

---

## 5. Fixtures: Persistente Daten für Integrationstests

```sql
-- src/test/resources/db/fixtures/orders-test-data.sql
-- Wird via @Sql in Integrationstests geladen

-- Bekannte IDs: Tests können sich auf diese verlassen
INSERT INTO customers (id, email, first_name, country)
VALUES (1, 'confirmed@test.de', 'Max', 'DE'),
       (2, 'vip@test.de',       'Anna', 'AT');

INSERT INTO orders (id, customer_id, status, total_cents, created_at)
VALUES (100, 1, 'CONFIRMED',       4999,   NOW() - INTERVAL '1 day'),
       (101, 1, 'PENDING_PAYMENT', 9999,   NOW() - INTERVAL '2 hours'),
       (102, 2, 'SHIPPED',         15000,  NOW() - INTERVAL '3 days');
```

```java
// Integrations-Test mit Fixture-Daten
@SpringBootTest
@Transactional
class OrderRepositoryIntegrationTest {

    @Autowired OrderRepository repository;

    // Fixtures laden
    @BeforeEach
    @Sql("classpath:db/fixtures/orders-test-data.sql")
    void loadFixtures() {}

    @Test
    void findetBestellungenNachKunde() {
        // Bekannte ID aus Fixture-Datei
        var orders = repository.findByCustomerId(new CustomerId(1L));

        assertThat(orders).hasSize(2);
        assertThat(orders)
            .extracting(Order::status)
            .containsExactlyInAnyOrder(CONFIRMED, PENDING_PAYMENT);
    }
}
```

---

## 6. Faker: Realistische Zufalls-Testdaten

```kotlin
// build.gradle.kts
testImplementation("net.datafaker:datafaker:2.1.0")
```

```java
// Faker: realistische aber zufällige Testdaten
// Nützlich für Property-Based-Testing oder Load-Tests

public class CustomerTestBuilder {

    private static final Faker faker = new Faker(Locale.GERMAN);

    // Standard-Defaults (für normale Tests: deterministisch)
    private String email     = "test@example.com";
    private String firstName = "Max";
    private String lastName  = "Mustermann";

    // Faker-basiert: für Load-Tests und Property-Tests
    public static CustomerTestBuilder aRandomCustomer() {
        var builder = new CustomerTestBuilder();
        builder.email     = faker.internet().emailAddress();
        builder.firstName = faker.name().firstName();
        builder.lastName  = faker.name().lastName();
        return builder;
    }
}

// Load-Test mit realistischen Daten (nicht alle "Max Mustermann")
@Test
void systemVerarbeitet1000BestellungenKorrekt() {
    var orders = IntStream.range(0, 1000)
        .mapToObj(i -> anOrder()
            .withCustomerId(ThreadLocalRandom.current().nextLong(1, 10000))
            .withTotal(String.valueOf(ThreadLocalRandom.current().nextDouble(10, 1000)))
            .build())
        .toList();

    assertThat(orders).allMatch(o -> o.total().amount().compareTo(BigDecimal.ZERO) > 0);
}
```

---

## 7. Wo leben Testdaten-Klassen?

```
src/test/java/com/example/
└── testdata/                       ← Separates Package für Testdaten
    ├── builders/
    │   ├── OrderTestBuilder.java
    │   ├── CustomerTestBuilder.java
    │   └── ProductTestBuilder.java
    ├── fixtures/
    │   └── OrderFixtures.java      ← Object Mother
    └── TestDataFactory.java        ← Zentrale Fabrik (optional)

WICHTIG:
  Testdaten-Klassen leben in src/TEST/java — NICHT in src/main/java.
  Sie sind NICHT Teil des Produktionscodes.
  Kein @Component, kein Spring-Bean für Test-Builder.
```

---

## 8. Akzeptanzkriterien

- [ ] Kein Test erstellt Domain-Objekte ohne Builder oder Fixture (ArchUnit-Regel möglich)
- [ ] Alle Builder haben Defaults für alle Pflichtfelder
- [ ] Object-Mother-Methoden beschreiben ein fachliches Szenario (nicht technisch)
- [ ] Integrationstests nutzen SQL-Fixtures statt `save()` in `@BeforeEach`
- [ ] Test-Code-Coverage: Testdaten-Klassen müssen NICHT gecovered sein (Exclusion konfiguriert)

---

## Quellen & Referenzen

- **Joshua Kerievsky, "Refactoring to Patterns" (2004)** — Object Mother als Test-Pattern.
- **Gerard Meszaros, "xUnit Test Patterns" (2007), Kap. 26** — Test Data Builder; vollständige Klassifizierung von Test-Daten-Patterns.
- **Vladimir Khorikov, "Unit Testing" (2020), Kap. 4** — Wie Testdaten die Lesbarkeit von Tests beeinflussen.

---

## Verwandte ADRs

- [ADR-096](ADR-096-test-strategie-gesamtbild.md) — Test-Strategie-Gesamtbild
- [ADR-010](ADR-010-junit-grundlagen-struktur.md) — JUnit 5 Grundlagen
- [ADR-046](ADR-046-property-based-testing.md) — Property-Based Testing (Faker komplementär)
