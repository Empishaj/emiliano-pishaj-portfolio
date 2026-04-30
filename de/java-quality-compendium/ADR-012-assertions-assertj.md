# ADR-012 — Assertions mit AssertJ: Aussagekraft statt Rätselraten

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · JUnit 5.10+ · AssertJ 3.25+            |
| Datum      | 2023-12-13                                  |
| Kategorie  | Testing / Assertions                        |

---

## Kontext & Problem

Eine Assertion ist die einzige Kommunikation eines Tests mit dem Entwickler im Fehlerfall. Schlechte Assertions sagen: `expected: true but was: false`. Gute Assertions sagen: `expected User with name "Max" but found User with name "null" — check if email validation resets the name field`. JUnit `assertTrue` / `assertEquals` sind primitiv. AssertJ ist der Standard.

**Verbindlich: Im gesamten Projekt wird ausschließlich AssertJ verwendet. JUnit-Assertions (`assertEquals`, `assertTrue` etc.) sind verboten.**

---

## Regel 1 — `assertThat()` statt `assertEquals()` / `assertTrue()`

### Schlecht — JUnit-Assertions: informationsarm

```java
// Was bedeutet "expected: <true> but was: <false>"?
assertTrue(result.isActive());

// Was bedeutet "expected: <42> but was: <0>"? Welche ID? Warum 0?
assertEquals(42L, result.getId());

// Welches Element fehlt? Welche hatte es?
assertTrue(result.contains("Max"));

// Fehlermeldung bei Fehler: "expected: <true> but was: <false>"
assertFalse(users.isEmpty());

// expected: <null> but was: <"Max">
assertNull(result.getMiddleName());
```

### Gut — AssertJ: selbsterklärende Fehlermeldungen

```java
// Fehlermeldung: "Expected User to be active, but it was inactive"
assertThat(result.isActive()).isTrue();

// Fehlermeldung: "expected: 42L but was: 0L (for field 'id')"
assertThat(result.id()).isEqualTo(42L);

// Fehlermeldung: "Expecting list to contain 'Max' but could not find it in ['Anna', 'Bernd']"
assertThat(result).contains("Max");

// Fehlermeldung: "Expecting empty list but found 3 elements: [User(...), ...]"
assertThat(users).isNotEmpty();

// Fehlermeldung: "Expected middleName to be null but was 'Hans'"
assertThat(result.middleName()).isNull();
```

---

## Regel 2 — Fluent Chaining für zusammengehörige Assertions

### Schlecht — separate assertThat()-Aufrufe für ein Objekt

```java
assertThat(user.id()).isEqualTo(1L);
assertThat(user.name()).isEqualTo("Max");
assertThat(user.email()).isEqualTo("max@example.com");
assertThat(user.active()).isTrue();
// Welcher Aspekt scheitert? Erst nach dem ersten Fehler sieht man es.
```

### ✅ Gut — Soft Assertions: alle Fehler auf einmal sehen

```java
// SoftAssertions: alle Assertions werden ausgeführt, alle Fehler gemeldet
assertSoftly(softly -> {
    softly.assertThat(user.id()).isEqualTo(1L);
    softly.assertThat(user.name()).isEqualTo("Max");
    softly.assertThat(user.email()).isEqualTo("max@example.com");
    softly.assertThat(user.active()).isTrue();
});
// Fehlermeldung listet ALLE fehlgeschlagenen Assertions auf
```

### Oder: `extracting()` für prägnante Multi-Field-Prüfung

```java
// Prüft mehrere Felder in einem Ausdruck
assertThat(user)
    .extracting(User::name, User::email, User::active)
    .containsExactly("Max", "max@example.com", true);
```

---

## Regel 3 — Collections präzise prüfen

### Schlecht — manuelle Größenprüfung und Index-Zugriff

```java
assertEquals(3, orders.size());
assertTrue(orders.get(0).status().equals(PENDING));
assertFalse(orders.isEmpty());
assertTrue(orders.stream().anyMatch(o -> o.total().compareTo(BigDecimal.ZERO) > 0));
```

### Gut — AssertJ Collection-API

```java
assertThat(orders)
    .hasSize(3)
    .isNotEmpty()
    .allMatch(o -> o.status() == PENDING)          // Alle Elemente erfüllen Bedingung
    .anyMatch(o -> o.total().signum() > 0)          // Mindestens ein Element
    .noneMatch(o -> o.status() == CANCELLED)        // Kein Element
    .extracting(Order::status)                      // Wert aus jedem Element extrahieren
    .containsOnly(PENDING);                         // Nur dieser Wert kommt vor

// Exakte Reihenfolge prüfen
assertThat(orders)
    .extracting(Order::id)
    .containsExactly(1L, 2L, 3L);

// Reihenfolge egal
assertThat(orders)
    .extracting(Order::id)
    .containsExactlyInAnyOrder(3L, 1L, 2L);

// Auf bestimmte Felder eines Objekts prüfen
assertThat(orders)
    .extracting(Order::status, Order::total)
    .contains(tuple(PENDING, new BigDecimal("99.00")));
```

---

## Regel 4 — Exceptions präzise prüfen

### Schlecht — `@Test(expected=...)` und manuelles try/catch

```java
// JUnit 4 Stil — Exception-Message wird nicht geprüft
@Test(expected = UserNotFoundException.class)
void findById_throwsException() {
    userService.findById(99L);
}

// Manuelles try/catch — ausführlich und fehleranfällig
@Test
void findById_throwsException() {
    try {
        userService.findById(99L);
        fail("Expected UserNotFoundException");
    } catch (UserNotFoundException e) {
        assertEquals("User not found: 99", e.getMessage());
    }
}
```

### Gut — `assertThatThrownBy()` mit vollständiger Exception-Prüfung

```java
@Test
void findById_throwsUserNotFoundException_whenIdIsUnknown() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert — vollständige Exception-Prüfung in einem Ausdruck
    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("99")
        .hasMessageNotContaining("password") // Kein sensitives Datum in Exception-Messages!
        .extracting("userId")                // Eigenes Feld der Exception prüfen
        .isEqualTo(99L);
}

// Alternative: assertThatExceptionOfType für typsichere API
assertThatExceptionOfType(UserNotFoundException.class)
    .isThrownBy(() -> userService.findById(99L))
    .withMessageContaining("User not found")
    .withNoCause();

// Prüfen dass KEINE Exception geworfen wird
assertThatNoException().isThrownBy(() -> userService.findById(1L));
```

---

## Regel 5 — Fehlermeldungen mit `as()` oder `withFailMessage()` anreichern

### Schlecht — kontextlose Fehlermeldung

```java
assertThat(result.status()).isEqualTo(APPROVED);
// Fehlermeldung bei Fehler: "expected: APPROVED but was: PENDING"
// Welcher User? Welcher Auftrag? Warum sollte er APPROVED sein?
```

### Gut — Kontext in der Fehlermeldung

```java
assertThat(result.status())
    .as("Order #%d should be APPROVED after payment confirmation", order.id())
    .isEqualTo(APPROVED);
// Fehlermeldung: "[Order #42 should be APPROVED after payment confirmation]
//                expected: APPROVED but was: PENDING"

// Für komplexe Objekte: Beschreibung des erwarteten Zustands
assertThat(user.active())
    .as("User with email '%s' should be active after email verification", user.email())
    .isTrue();
```

---

## Regel 6 — Optional, Dates, Zahlen: typsichere Assertions

### Optional

```java
// ❌ Schlecht
assertTrue(result.isPresent());
assertEquals("Max", result.get().name()); // NoSuchElementException wenn leer!

// ✅ Gut
assertThat(result)
    .isPresent()
    .hasValueSatisfying(user -> assertThat(user.name()).isEqualTo("Max"));

// Oder direkt:
assertThat(result).contains(new User(1L, "Max", "max@example.com"));
assertThat(emptyResult).isEmpty();
```

### Zahlen & Dezimalwerte

```java
// ❌ Schlecht — Floating-Point-Vergleich ohne Toleranz ist unsicher
assertEquals(19.99, result.total(), 0.001); // JUnit 4 Stil

// ✅ Gut — AssertJ mit Offset für Dezimalzahlen
assertThat(result.total())
    .isCloseTo(new BigDecimal("19.99"), within(new BigDecimal("0.01")));

// Ganzzahlbereiche
assertThat(result.quantity())
    .isPositive()
    .isGreaterThan(0)
    .isLessThanOrEqualTo(100);
```

### Datum & Zeit

```java
// ❌ Schlecht — System.currentTimeMillis() ist nicht deterministisch
assertTrue(result.createdAt().toEpochMilli() <= System.currentTimeMillis());

// ✅ Gut — AssertJ Temporal-Assertions
assertThat(result.createdAt())
    .isAfter(Instant.parse("2024-01-01T00:00:00Z"))
    .isBefore(Instant.now().plusSeconds(5));

// Mit fixer Clock (→ ADR-011: Clock mocken)
assertThat(result.createdAt()).isEqualTo(fixedClock.instant());
```

---

## Regel 7 — Eigene Assertions für Domänentypen

Wenn `extracting(...).isEqualTo(...)` zu ausführlich wird, schreibe eine eigene AssertJ-Assertion für häufig geprüfte Domänentypen.

```java
// Eigene Assertion für Order
public class OrderAssert extends AbstractAssert<OrderAssert, Order> {

    public OrderAssert(Order actual) {
        super(actual, OrderAssert.class);
    }

    public static OrderAssert assertThat(Order order) {
        return new OrderAssert(order);
    }

    public OrderAssert hasStatus(OrderStatus expected) {
        isNotNull();
        if (actual.status() != expected) {
            failWithMessage(
                "Expected order #%d to have status <%s> but was <%s>",
                actual.id(), expected, actual.status()
            );
        }
        return this;
    }

    public OrderAssert hasItemCount(int expected) {
        isNotNull();
        assertThat(actual.items()).hasSize(expected);
        return this;
    }

    public OrderAssert isOwnedBy(UserId userId) {
        isNotNull();
        if (!actual.userId().equals(userId)) {
            failWithMessage("Expected order to be owned by %s but was owned by %s",
                userId, actual.userId());
        }
        return this;
    }
}

// Verwendung — lesbar wie Prosa
OrderAssert.assertThat(order)
    .hasStatus(PENDING)
    .hasItemCount(3)
    .isOwnedBy(new UserId(42L));
```

---

## Konsequenzen

**Positiv:**
- Fehlermeldungen erklären was falsch ist — kein Debugging des Tests nötig.
- Fluent API ist selbstdokumentierend — Tests lesen sich wie Prosa.
- Soft Assertions zeigen alle Fehler auf einmal statt nach erstem Fehler abzubrechen.

**Negativ:**
- AssertJ-API ist groß — einmalige Einarbeitungszeit. IDE-Autovervollständigung hilft.
- Custom Assertions bedeuten initiale Investition — lohnt sich ab dem dritten Test desselben Typs.

---

## Tipps

- **`import static org.assertj.core.api.Assertions.*`** — einmal statisch importieren, überall verfügbar.
- **Fehlermeldung vor dem Schreiben lesen**: Führe den Test einmal bewusst mit falschem Wert aus. Ist die Meldung verständlich? Wenn nicht: `as()` hinzufügen.
- **`usingRecursiveComparison()`**: Für tiefe Objektvergleiche ohne `equals()` — perfekt für DTOs die kein `equals()` überschreiben (müssen):
  ```java
  assertThat(result).usingRecursiveComparison().isEqualTo(expected);
  ```
- **`satisfies()`** für komplexe Bedingungen die kein Standard-Matcher abdeckt:
  ```java
  assertThat(order).satisfies(o -> {
      assertThat(o.total()).isGreaterThan(BigDecimal.ZERO);
      assertThat(o.items()).isNotEmpty();
  });
  ```
 