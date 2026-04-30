# ADR-014 — Parametrisierte Tests & Testdaten: Varianten ohne Duplizierung

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · JUnit 5.10+                            |
| Datum      | 2026-02-14                                 |
| Kategorie  | Testing / Parametrisierung                  |

---

## Kontext & Problem

Viele Tests prüfen dasselbe Verhalten mit verschiedenen Eingaben: Validierung akzeptiert gültige Emails und lehnt ungültige ab, Rabattberechnung liefert für verschiedene Kundentypen verschiedene Ergebnisse, Grenzwerte (0, -1, Integer.MAX_VALUE) werden korrekt behandelt. Ohne `@ParameterizedTest` entsteht Code-Duplikation: 10 Tests mit identischer Struktur, nur die Eingabewerte unterscheiden sich.

---

## Regel 1 — `@ValueSource` für einfache Einzelwert-Varianten

### Schlecht — duplizierte Tests für verschiedene Eingaben

```java
@Test void isValidEmail_returnsFalse_whenNoAtSign()       { assertThat(validator.isValid("maxexample.com")).isFalse(); }
@Test void isValidEmail_returnsFalse_whenEmptyString()    { assertThat(validator.isValid("")).isFalse(); }
@Test void isValidEmail_returnsFalse_whenOnlyAtSign()     { assertThat(validator.isValid("@")).isFalse(); }
@Test void isValidEmail_returnsFalse_whenDoubleAt()       { assertThat(validator.isValid("max@@example.com")).isFalse(); }
@Test void isValidEmail_returnsFalse_whenNoDomain()       { assertThat(validator.isValid("max@")).isFalse(); }
@Test void isValidEmail_returnsFalse_whenSpaces()         { assertThat(validator.isValid("max @example.com")).isFalse(); }
// Struktur identisch — nur der Eingabewert ändert sich. 6 Tests, 6 mal dieselbe Zeile.
```

### Gut — `@ParameterizedTest` mit `@ValueSource`

```java
@ParameterizedTest(name = "Email \"{0}\" sollte ungültig sein")
@ValueSource(strings = {
    "maxexample.com",        // kein @
    "",                      // leer
    "@",                     // nur @
    "max@@example.com",      // doppeltes @
    "max@",                  // keine Domain
    "max @example.com",      // Leerzeichen
    "max@.com",              // Punkt direkt nach @
    "@example.com"           // kein lokaler Teil
})
void isValidEmail_returnsFalse_forInvalidEmails(String invalidEmail) {
    assertThat(emailValidator.isValid(invalidEmail)).isFalse();
}

@ParameterizedTest(name = "Email \"{0}\" sollte gültig sein")
@ValueSource(strings = {
    "max@example.com",
    "max.mustermann@example.com",
    "max+tag@example.co.uk",
    "123@example.com"
})
void isValidEmail_returnsTrue_forValidEmails(String validEmail) {
    assertThat(emailValidator.isValid(validEmail)).isTrue();
}
```

---

## Regel 2 — `@CsvSource` für Eingabe-Ausgabe-Paare

Wenn Eingabe **und** erwartetes Ergebnis variieren.

### Schlecht — duplizierte Tests für Berechnung mit verschiedenen Werten

```java
@Test void calculateDiscount_returns20_forPremium()  { assertThat(service.calculate(PREMIUM, 100)).isEqualTo(20); }
@Test void calculateDiscount_returns10_forRegular()  { assertThat(service.calculate(REGULAR, 100)).isEqualTo(10); }
@Test void calculateDiscount_returns0_forNew()       { assertThat(service.calculate(NEW, 100)).isEqualTo(0); }
@Test void calculateDiscount_returns30_forVip100()   { assertThat(service.calculate(VIP, 100)).isEqualTo(30); }
@Test void calculateDiscount_returns60_forVip200()   { assertThat(service.calculate(VIP, 200)).isEqualTo(60); }
```

### Gut — `@CsvSource` für Tabellen-Testdaten

```java
@ParameterizedTest(name = "Kundentyp {0}, Betrag {1} → Rabatt {2}")
@CsvSource({
    // customerType, amount, expectedDiscount
    "PREMIUM, 100, 20",
    "PREMIUM, 200, 40",
    "REGULAR, 100, 10",
    "REGULAR,  50,  5",
    "NEW,     100,  0",
    "VIP,     100, 30",
    "VIP,     200, 60"
})
void calculateDiscount_returnsCorrectAmount(
        CustomerType type,
        BigDecimal amount,
        BigDecimal expectedDiscount) {

    var discount = discountService.calculate(type, amount);

    assertThat(discount)
        .as("Rabatt für %s bei Betrag %s", type, amount)
        .isEqualByComparingTo(expectedDiscount);
}
```

---

## Regel 3 — `@MethodSource` für komplexe Testdaten-Objekte

Wenn die Testdaten zu komplex für `@CsvSource`-Strings sind.

```java
// In der Testklasse: statische Methode liefert Stream<Arguments>
class OrderValidationTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("ungueltigeBestellungen")
    void validate_throwsException_forInvalidOrders(
            String scenarioName,
            CreateOrderCommand command,
            Class<? extends Exception> expectedExceptionType,
            String expectedMessageFragment) {

        assertThatThrownBy(() -> orderValidator.validate(command))
            .isInstanceOf(expectedExceptionType)
            .hasMessageContaining(expectedMessageFragment);
    }

    static Stream<Arguments> ungueltigeBestellungen() {
        return Stream.of(
            Arguments.of(
                "Null-UserId wirft IllegalArgumentException",
                new CreateOrderCommand(null, new ProductId("P1"), new Quantity(1)),
                IllegalArgumentException.class,
                "userId"
            ),
            Arguments.of(
                "Menge 0 wirft ValidationException",
                new CreateOrderCommand(new UserId(1L), new ProductId("P1"), new Quantity(0)),
                ValidationException.class,
                "quantity"
            ),
            Arguments.of(
                "Null-ProductId wirft IllegalArgumentException",
                new CreateOrderCommand(new UserId(1L), null, new Quantity(1)),
                IllegalArgumentException.class,
                "productId"
            )
        );
    }
}
```

---

## Regel 4 — `@EnumSource` für alle Enum-Werte

```java
// Prüfe: für JEDEN OrderStatus gibt es einen definierten Übergang oder eine Exception
@ParameterizedTest(name = "Status {0}: cancel() verhält sich korrekt")
@EnumSource(OrderStatus.class)
void cancel_hasDefinedBehaviorForAllStatuses(OrderStatus initialStatus) {
    var order = Order.withStatus(initialStatus);

    if (initialStatus == PENDING || initialStatus == PROCESSING) {
        assertThatNoException().isThrownBy(order::cancel);
        assertThat(order.status()).isEqualTo(CANCELLED);
    } else {
        assertThatThrownBy(order::cancel)
            .isInstanceOf(OrderCannotBeCancelledException.class);
    }
}

// Oder: nur bestimmte Enum-Werte einschließen / ausschließen
@ParameterizedTest
@EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED", "REFUNDED"})
void cancel_throwsException_forTerminalStatuses(OrderStatus terminalStatus) {
    var order = Order.withStatus(terminalStatus);

    assertThatThrownBy(order::cancel)
        .isInstanceOf(OrderCannotBeCancelledException.class);
}
```

---

## Regel 5 — Grenzwert-Tests: immer explizit

Grenzwerte sind die häufigsten Fehlerquellen. Sie werden immer explizit getestet — nie nur "irgendein gültiger Wert".

```java
@ParameterizedTest(name = "Menge {0} ist gültig")
@ValueSource(ints = {
    1,              // Minimum (untere Grenze)
    2,              // Minimum + 1
    99,             // Maximum - 1
    100             // Maximum (obere Grenze)
})
void quantity_isValid_forBoundaryValues(int value) {
    assertThatNoException()
        .isThrownBy(() -> new Quantity(value));
}

@ParameterizedTest(name = "Menge {0} ist ungültig")
@ValueSource(ints = {
    Integer.MIN_VALUE,  // Extremwert negativ
    -1,                 // unterhalb Minimum
    0,                  // genau die Grenze (ungültig)
    101,                // oberhalb Maximum
    Integer.MAX_VALUE   // Extremwert positiv
})
void quantity_throwsException_forInvalidValues(int invalidValue) {
    assertThatThrownBy(() -> new Quantity(invalidValue))
        .isInstanceOf(IllegalArgumentException.class);
}
```

---

## Regel 6 — `@NullSource` und `@NullAndEmptySource`

Null-Handling immer explizit testen — nicht nur als Kommentar "sollte auch mit null funktionieren".

```java
// Prüft: Methode wirft NullPointerException / IllegalArgumentException für null
@ParameterizedTest
@NullSource
void register_throwsException_whenNameIsNull(String nullName) {
    var command = new RegisterUserCommand(nullName, "max@example.com", "geheim");

    assertThatThrownBy(() -> userService.register(command))
        .isInstanceOf(IllegalArgumentException.class);
}

// Kombiniert: null UND leerer String UND leerzeichen-String
@ParameterizedTest(name = "Leerer/Null Name [{0}] wird abgelehnt")
@NullAndEmptySource
@ValueSource(strings = {"   ", "\t", "\n"})
void register_throwsException_whenNameIsBlankOrNull(String blankName) {
    var command = new RegisterUserCommand(blankName, "max@example.com", "geheim");

    assertThatThrownBy(() -> userService.register(command))
        .isInstanceOf(ValidationException.class);
}
```

---

## Fehler: Zu viel in einem parametrisierten Test

### Schlecht — parametrisierter Test mit Logik

```java
@ParameterizedTest
@CsvSource({"PREMIUM, true", "REGULAR, false", "NEW, false"})
void processOrder_behavesCorrectly(CustomerType type, boolean expectsDiscount) {
    var order = createOrderFor(type);

    var result = orderService.process(order);

    // Logik im Test — if/else verletzt die Testprinzipien (→ ADR-010)
    if (expectsDiscount) {
        assertThat(result.discountApplied()).isTrue();
        assertThat(result.total()).isLessThan(result.subtotal());
    } else {
        assertThat(result.discountApplied()).isFalse();
        assertThat(result.total()).isEqualTo(result.subtotal());
    }
}
```

### Gut — separate parametrisierte Tests für separate Aussagen

```java
@ParameterizedTest(name = "Kundentyp {0} erhält Rabatt")
@EnumSource(value = CustomerType.class, names = {"PREMIUM", "VIP"})
void processOrder_appliesDiscount_forPremiumCustomers(CustomerType type) {
    var order = createOrderFor(type);

    var result = orderService.process(order);

    assertThat(result.discountApplied()).isTrue();
    assertThat(result.total()).isLessThan(result.subtotal());
}

@ParameterizedTest(name = "Kundentyp {0} erhält keinen Rabatt")
@EnumSource(value = CustomerType.class, names = {"REGULAR", "NEW"})
void processOrder_appliesNoDiscount_forStandardCustomers(CustomerType type) {
    var order = createOrderFor(type);

    var result = orderService.process(order);

    assertThat(result.discountApplied()).isFalse();
    assertThat(result.total()).isEqualByComparingTo(result.subtotal());
}
```

---

## Überblick: Welche Annotation wann?

| Annotation          | Einsatz                                              | Beispiel                                         |
|---------------------|------------------------------------------------------|--------------------------------------------------|
| `@ValueSource`      | Ein Parameter, primitive Typen oder Strings          | Ungültige Emails, Grenzwerte                     |
| `@CsvSource`        | Mehrere Parameter, tabellarisch, einfache Typen      | Eingabe → Erwartetes Ergebnis                    |
| `@CsvFileSource`    | Viele Testfälle aus CSV-Datei im `resources/`        | Große Datensätze, business-seitig gepflegt       |
| `@MethodSource`     | Komplexe Objekte, berechnete Testdaten               | Domänenobjekte, Streams                          |
| `@EnumSource`       | Alle oder bestimmte Enum-Werte                       | Status-Maschinen, Typen-Variation                |
| `@NullSource`       | Null-Handling                                        | Pflichtfelder, @NonNull-Garantien                |
| `@NullAndEmptySource`| Null + leerer String                               | String-Validierung                               |
| `@ArgumentsSource`  | Externe, wiederverwendbare Testdaten-Quelle          | Datenbankgestützte Testdaten                     |

---

## Konsequenzen

**Positiv:**
- Neue Testfälle kosten nur eine Zeile (ein neuer Wert in der Source-Annotation).
- JUnit zeigt jeden parametrisierten Fall einzeln im Testbericht — Fehler sind punktgenau lokalisierbar.
- Grenzwerte, Null-Handling und Enum-Varianten sind systematisch und vollständig abgedeckt.

**Negativ:**
- `@CsvSource` mit komplexen Objekten ist unleserlich — dann `@MethodSource` verwenden.
- `@ParameterizedTest` nicht für semantisch verschiedene Szenarien missbrauchen — dann lieber `@Nested`.

---

## Tipps

- **`name`-Attribut immer setzen**: `@ParameterizedTest(name = "...")` macht den Testbericht lesbar. `{0}`, `{1}` sind die Parameterwerte.
- **`@CsvFileSource`** für Testdaten die Business-seitig gepflegt werden sollen — CSV-Datei unter `src/test/resources/`.
- **Konverter**: JUnit 5 konvertiert `String` zu `int`, `boolean`, `Enum` automatisch — für eigene Typen `@ConvertWith` oder impliziter Konstruktor mit einem `String`-Argument.
- **Kombinationen testen**: Mit `@MethodSource` können alle Kombinationen zweier Enum-Werte einfach via `Stream.of(CustomerType.values())` und `flatMap` erzeugt werden.
 