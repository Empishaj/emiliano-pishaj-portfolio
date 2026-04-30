# ADR-046 — Property-Based Testing mit jqwik

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Status     | ✅ Akzeptiert                               |
| Java       | 21 · JUnit 5 · jqwik 1.8+                  |
| Datum      | 2024-01-01                                  |
| Kategorie  | Testing / Testmethodik                      |

---

## Kontext & Problem

Beispiel-basierte Tests prüfen konkrete Fälle: `isAdult(18) == true`. Property-Based Tests beschreiben **Eigenschaften** die für alle Inputs gelten müssen: "Für jeden Wert ≥ 18 ist isAdult() true." Der Test-Framework generiert hunderte zufällige Eingaben und sucht nach Gegenbeispielen. Das findet Fehler die kein Entwickler manuell hätte spezifiziert.

---

## jqwik: Property-Based Testing in Java

```kotlin
// build.gradle.kts
testImplementation("net.jqwik:jqwik:1.8.3")
testImplementation("net.jqwik:jqwik-kotlin:1.8.3") // optional
```

---

## Einfache Properties

```java
import net.jqwik.api.*;

class MoneyPropertyTest {

    // ❌ Beispiel-basierter Test: nur 3 Fälle
    @Test
    void add_returnsSumOfAmounts() {
        assertThat(Money.of("10", "EUR").add(Money.of("5", "EUR")))
            .isEqualTo(Money.of("15", "EUR"));
    }

    // ✅ Property-basiert: jqwik generiert 1000+ Zufallswerte
    @Property(tries = 1000)
    void add_isCommutative(
            @ForAll @BigRange(min = "0", max = "999999") BigDecimal a,
            @ForAll @BigRange(min = "0", max = "999999") BigDecimal b) {
        // Eigenschaft: Addition ist kommutativ
        var moneyA = new Money(a, Currency.EUR);
        var moneyB = new Money(b, Currency.EUR);

        assertThat(moneyA.add(moneyB)).isEqualTo(moneyB.add(moneyA));
    }

    @Property
    void add_isAssociative(
            @ForAll @BigRange(min = "0", max = "9999") BigDecimal a,
            @ForAll @BigRange(min = "0", max = "9999") BigDecimal b,
            @ForAll @BigRange(min = "0", max = "9999") BigDecimal c) {
        // Eigenschaft: (a+b)+c == a+(b+c)
        var ma = new Money(a, Currency.EUR);
        var mb = new Money(b, Currency.EUR);
        var mc = new Money(c, Currency.EUR);

        assertThat(ma.add(mb).add(mc)).isEqualTo(ma.add(mb.add(mc)));
    }

    @Property
    void add_withZero_returnsOriginal(@ForAll @BigRange(min = "0") BigDecimal amount) {
        // Eigenschaft: a + 0 = a
        var money = new Money(amount, Currency.EUR);
        assertThat(money.add(Money.zero(Currency.EUR))).isEqualTo(money);
    }
}
```

---

## Domain-spezifische Generatoren

```java
class OrderPropertyTest {

    // Eigener Arbitrary-Provider: erzeugt valide Order-Objekte
    @Provide
    Arbitrary<Order> validOrders() {
        var userIds = Arbitraries.longs().between(1, 10000)
            .map(UserId::new);

        var quantities = Arbitraries.integers().between(1, 100)
            .map(Quantity::new);

        var productIds = Arbitraries.strings().alpha().ofLength(8)
            .map(ProductId::new);

        var items = Combinators.combine(productIds, quantities)
            .as(OrderItem::new)
            .list().ofMinSize(1).ofMaxSize(10);

        return Combinators.combine(userIds, items)
            .as(Order::new);
    }

    @Property
    void total_isAlwaysPositive(@ForAll("validOrders") Order order) {
        // Eigenschaft: Total ist immer > 0 wenn Items vorhanden
        assertThat(order.total().amount()).isPositive();
    }

    @Property
    void total_equalsItemSum(@ForAll("validOrders") Order order) {
        // Eigenschaft: Total == Summe der Item-Preise
        var expectedTotal = order.items().stream()
            .map(OrderItem::subtotal)
            .reduce(Money.zero(EUR), Money::add);

        assertThat(order.total()).isEqualTo(expectedTotal);
    }

    @Property
    void serialization_roundTrip(@ForAll("validOrders") Order order) {
        // Eigenschaft: Serialisierung → Deserialisierung = Original
        var json        = objectMapper.writeValueAsString(order);
        var deserialized = objectMapper.readValue(json, Order.class);

        assertThat(deserialized).isEqualTo(order);
    }
}
```

---

## Shrinking: minimales Gegenbeispiel finden

```java
// jqwik shrinkst automatisch wenn ein Fehler gefunden wird
// Beispiel: Fehler in add() für sehr große Zahlen

@Property
void add_neverOverflows(
        @ForAll @LongRange(min = 0, max = Long.MAX_VALUE / 2) long a,
        @ForAll @LongRange(min = 0, max = Long.MAX_VALUE / 2) long b) {

    var result = a + b; // Bug: Overflow bei großen Zahlen

    assertThat(result).isGreaterThanOrEqualTo(a);
}

// jqwik findet den Fehler und shrinkst zum minimalen Gegenbeispiel:
// a = 4611686018427387904 (Long.MAX_VALUE/2)
// b = 4611686018427387904 (Long.MAX_VALUE/2)
// Klares minimales Beispiel statt zufälliger Riesenzahl
```

---

## Stateful Property Tests

```java
// Zustandsbasierte Tests: Sequenzen von Aktionen prüfen
class OrderStateMachineTest {

    @Property
    void stateTransitions_areConsistent(
            @ForAll List<@From("orderActions") String> actions) {
        var order = new Order(new UserId(1L));

        for (var action : actions) {
            switch (action) {
                case "addItem"  -> safeAddItem(order);
                case "confirm"  -> safeConfirm(order);
                case "cancel"   -> safeCancel(order);
            }
            // Invariante: Status immer in gültigem Zustand
            assertThat(order.status()).isIn(
                OrderStatus.PENDING, CONFIRMED, CANCELLED);
        }
    }

    @Provide
    Arbitrary<String> orderActions() {
        return Arbitraries.of("addItem", "confirm", "cancel");
    }
}
```

---

## Wann Property-Based Testing, wann Beispiel-basiert?

```
Beispiel-basiert (JUnit @Test):
→ Spezifisches Verhalten mit bekanntem Input/Output
→ Happy Path und bekannte Fehlerfälle
→ Lesbarkeit wichtiger als Vollständigkeit

Property-Based (jqwik @Property):
→ Mathematische Eigenschaften (Kommutativität, Assoziativität)
→ Invarianten die immer gelten müssen
→ Serialisierung/Deserialisierung Round-Trips
→ Fuzzing: Robustheit gegen beliebige Inputs
→ Zustandsmaschinen: beliebige Aktionssequenzen
```

---

## Konsequenzen

**Positiv:** Findet Bugs die kein Entwickler als Testfall spezifiziert hätte. Shrinking liefert minimale reproduzierbare Gegenbeispiele. Deckt Grenzwert-Probleme systematisch auf.

**Negativ:** Schwieriger zu schreiben als Beispiel-Tests — erfordert Denken in Eigenschaften statt Beispielen. Nicht für alle Testfälle sinnvoll. Laufzeit höher durch viele generierte Inputs.

---

## 💡 Guru-Tipps

- **Starte mit Round-Trip-Properties**: Serialisierung, Parsing, Encoding/Decoding — einfach zu formulieren, findet viele Bugs.
- **Seed fixieren** für reproduzierbare CI-Fehler: `@Property(seed = 42L)` — reproduziert exakt denselben Zufalls-Stream.
- **`@Report(Reporting.GENERATED)` für Debugging**: zeigt alle generierten Werte.
- **Property-Tests ergänzen Beispiel-Tests** — sie ersetzen sie nicht. Beide haben ihre Stärken.

---

## Verwandte ADRs

- [ADR-014](ADR-014-parametrisierte-tests.md) — Parametrisierte Tests für bekannte Varianten.
- [ADR-045](ADR-045-mutation-testing-pit.md) — Mutation Testing + Property-Based = maximale Abdeckung.
