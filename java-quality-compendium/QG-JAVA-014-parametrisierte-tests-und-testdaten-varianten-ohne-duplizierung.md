# QG-JAVA-014 — Parametrisierte Tests und Testdaten: Varianten ohne Duplizierung

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-014 |
| Titel | Parametrisierte Tests und Testdaten: Varianten ohne Duplizierung |
| Status | Accepted / verbindlicher Standard für wiederholbare Testvarianten in neuen Java-Testklassen |
| Sprache | Deutsch |
| Kategorie | Testing / Parametrisierung / Testdaten-Design |
| Java-Baseline | Java 21 |
| Test-Baseline | JUnit Jupiter 5.10+; AssertJ als bevorzugte Assertion-Bibliothek gemäß Projektstandard |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, Test Automation Engineers |
| Gültigkeit | Gilt für Unit Tests, Domain Tests, Service Tests, Validator Tests und begrenzte Spring-Integrationstests |
| Nicht-Ziel | Diese Richtlinie ersetzt keine Property-Based Tests, keine Lasttests, keine Contract Tests und keine fachliche Teststrategie |
| Verbindlichkeit | Parametrisierte Tests MÜSSEN verwendet werden, wenn dieselbe Testlogik mit mehreren Eingabewerten oder erwarteten Ergebnissen ausgeführt wird und die Szenarien semantisch gleichartig sind |
| Letzte fachliche Prüfung | 2026-05-03 |
| Quellenbasis | JUnit User Guide 5.10.5, JUnit User Guide 6.0.3, AssertJ-Projektstandard, projektinterne Java-Testqualitätsregeln |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie parametrisierte Tests in JUnit 5 eingesetzt werden, um Varianten systematisch zu prüfen, ohne Testcode zu duplizieren. Sie legt fest, wann `@ParameterizedTest`, `@ValueSource`, `@CsvSource`, `@MethodSource`, `@EnumSource`, `@NullSource`, `@NullAndEmptySource`, `@CsvFileSource` und `@ArgumentsSource` verwendet werden sollen.

Das Ziel ist nicht, möglichst viele Testfälle in eine Methode zu pressen. Das Ziel ist, **gleichartige Testvarianten sauber, lesbar, isoliert und aussagekräftig** abzubilden. Ein parametrisierter Test ist gut, wenn jeder einzelne Testfall im Testbericht verständlich erscheint und bei Fehlern sofort erkennbar ist, welche Eingabe, welche Bedingung und welche Erwartung verletzt wurde.

---

## 2. Kurzregel

Verwende parametrisierte Tests, wenn dieselbe fachliche Aussage mit mehreren Eingaben geprüft wird. Verwende keinen parametrisierten Test, wenn die Szenarien unterschiedliche fachliche Bedeutungen, unterschiedliche Arrange-Logik oder unterschiedliche Assertions haben. In diesem Fall werden getrennte Testmethoden oder `@Nested`-Gruppen verwendet.

**Ein guter parametrisierter Test prüft eine Aussage über viele Varianten. Ein schlechter parametrisierter Test versteckt viele Aussagen in einer Methode.**

---

## 3. Geltungsbereich

Diese Richtlinie gilt besonders für:

- Validierung von Eingabewerten
- Grenzwerttests
- Enum-Varianten
- Statusübergänge
- einfache Eingabe-Ausgabe-Tabellen
- Fehlertypen bei ähnlichen invaliden Kommandos
- Währungs-, Mengen-, Datums- und Formatregeln
- Berechtigungsvarianten mit gleichem Erwartungsmuster
- Mandanten-/Tenant-Grenzfälle mit gleicher Teststruktur
- Parser-, Formatter-, Mapper- und Konvertertests

Diese Richtlinie gilt nicht automatisch für:

- semantisch unterschiedliche Szenarien
- Tests mit stark unterschiedlichem Setup
- Tests mit stark unterschiedlichen Assertions
- komplexe Workflows über viele Schritte
- End-to-End-Szenarien
- Contract Tests
- Tests, die durch Parametrisierung schwerer verständlich werden

---

## 4. Technischer Hintergrund

JUnit Jupiter unterstützt parametrisierte Tests mit `@ParameterizedTest`. Ein parametrisierter Test sieht aus wie ein normaler Test, wird aber mehrfach mit unterschiedlichen Argumenten ausgeführt. JUnit verlangt dafür mindestens eine Argumentquelle, zum Beispiel `@ValueSource`, `@CsvSource`, `@MethodSource` oder `@EnumSource`.

JUnit führt jede Invocation separat aus und stellt sie in IDE, Build-Report oder Console-Ausgabe einzeln dar. Genau deshalb ist das `name`-Attribut wichtig. Ohne sprechenden Namen sieht man im Fehlerfall nur abstrakte Invocation-Indizes. Mit sprechendem Namen wird aus einem parametrisierten Test eine lesbare Variantenspezifikation.

Für JUnit 5.10+ muss das Modul `junit-jupiter-params` verfügbar sein. In vielen Spring-Boot-Projekten ist das über `spring-boot-starter-test` bereits indirekt enthalten. Bei expliziter JUnit-Konfiguration muss die Abhängigkeit bewusst ergänzt werden.

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
}
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 5. Verbindlicher Standard

Ein parametrisierter Test wird verwendet, wenn alle folgenden Bedingungen erfüllt sind:

1. Alle Varianten prüfen dieselbe fachliche Aussage.
2. Die Arrange-Logik ist gleich oder nur datengetrieben unterschiedlich.
3. Die Act-Phase ist identisch.
4. Die Assert-Phase ist gleich strukturiert.
5. Jeder Testfall ist über den Testnamen eindeutig identifizierbar.
6. Die Testdaten sind klein genug, um in der gewählten Quelle lesbar zu bleiben.
7. Es gibt keine `if/else`-Verzweigung in der Assertion-Logik.

Ein parametrisierter Test darf nicht verwendet werden, wenn die Testmethode dadurch zu einer Mini-Test-Engine wird.

---

## 6. Entscheidungsregel: Welche Quelle wann?

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| `@ValueSource` | Ein einzelner Parameter mit einfachen Literalwerten | ungültige E-Mails, Mengenwerte, IDs | Verwenden bei einfachen Einzelwerten |
| `@NullSource` | Ein einzelner `null`-Wert | Pflichtfeld darf nicht null sein | Verwenden für explizites Null-Handling |
| `@EmptySource` | Leere Strings, Collections, Maps oder Arrays | leerer Name, leere Tags | Verwenden für leere Eingaben |
| `@NullAndEmptySource` | Kombination aus `null` und leer | Name, Beschreibung, Suchtext | Bevorzugt für String-Pflichtfelder |
| `@EnumSource` | Alle oder ausgewählte Enum-Werte | Statusübergänge, Kundentypen | Verwenden für vollständige Enum-Abdeckung |
| `@CsvSource` | Mehrere einfache Parameter in Tabellenform | Eingabe → erwartetes Ergebnis | Verwenden für kleine, lesbare Tabellen |
| `@CsvFileSource` | Viele einfache Testfälle aus Datei | große Regelmatrix | Verwenden, wenn Daten größer oder fachlich gepflegt sind |
| `@MethodSource` | Komplexe Objekte oder berechnete Testdaten | Commands, Entities, Exceptions | Standard für komplexe Testdaten |
| `@ArgumentsSource` | Wiederverwendbare externe Argumentquelle | Generator für viele Domänenfälle | Nur bei wiederverwendbarer Speziallogik |

---

## 7. Gute Anwendung: `@ValueSource` für einfache Einzelwerte

### Schlechte Anwendung

```java
@Test
void isValidEmail_returnsFalse_whenNoAtSign() {
    assertThat(emailValidator.isValid("maxexample.com")).isFalse();
}

@Test
void isValidEmail_returnsFalse_whenEmptyString() {
    assertThat(emailValidator.isValid("")).isFalse();
}

@Test
void isValidEmail_returnsFalse_whenDoubleAt() {
    assertThat(emailValidator.isValid("max@@example.com")).isFalse();
}
```

Diese Tests sind nicht fachlich falsch, aber strukturell dupliziert. Die fachliche Aussage ist immer dieselbe: Die E-Mail-Adresse ist ungültig.

### Gute Anwendung

```java
@ParameterizedTest(name = "E-Mail-Adresse [{0}] wird als ungültig abgelehnt")
@ValueSource(strings = {
        "maxexample.com",
        "",
        "@",
        "max@@example.com",
        "max@",
        "max @example.com",
        "max@.com",
        "@example.com"
})
void isValidEmail_returnsFalse_forInvalidEmails(String invalidEmail) {
    assertThat(emailValidator.isValid(invalidEmail)).isFalse();
}
```

Der Test ist kompakt, jede Invocation erscheint einzeln im Testbericht, und neue Varianten kosten nur eine zusätzliche Zeile.

---

## 8. Gute Anwendung: gültige und ungültige Varianten trennen

Gültige und ungültige Eingaben dürfen nicht in denselben Test gepackt werden, wenn dadurch ein Boolean-Parameter die Assertion steuert.

### Schlecht

```java
@ParameterizedTest
@CsvSource({
        "max@example.com, true",
        "maxexample.com, false",
        "max@, false"
})
void isValidEmail_returnsExpectedResult(String email, boolean expected) {
    assertThat(emailValidator.isValid(email)).isEqualTo(expected);
}
```

Dieser Test ist zwar kurz, aber fachlich schwächer. Der Boolean `expected` verschleiert die Testaussage. Der Testname sagt nicht, ob gerade Akzeptanz oder Ablehnung geprüft wird.

### Gut

```java
@ParameterizedTest(name = "gültige E-Mail-Adresse [{0}] wird akzeptiert")
@ValueSource(strings = {
        "max@example.com",
        "max.mustermann@example.com",
        "max+tag@example.co.uk",
        "123@example.com"
})
void isValidEmail_returnsTrue_forValidEmails(String validEmail) {
    assertThat(emailValidator.isValid(validEmail)).isTrue();
}

@ParameterizedTest(name = "ungültige E-Mail-Adresse [{0}] wird abgelehnt")
@ValueSource(strings = {
        "maxexample.com",
        "max@@example.com",
        "max@",
        "max @example.com"
})
void isValidEmail_returnsFalse_forInvalidEmails(String invalidEmail) {
    assertThat(emailValidator.isValid(invalidEmail)).isFalse();
}
```

Die Trennung macht die Fachaussage klarer und verhindert versteckte Testlogik.

---

## 9. Gute Anwendung: `@CsvSource` für Eingabe-Ausgabe-Paare

`@CsvSource` ist geeignet, wenn mehrere einfache Parameter gemeinsam variieren. Typische Fälle sind Berechnungen, Mapping-Regeln, Klassifikationen oder Grenzwerttabellen.

```java
@ParameterizedTest(name = "Kundentyp {0}, Betrag {1} → Rabatt {2}")
@CsvSource({
        "PREMIUM, 100.00, 20.00",
        "PREMIUM, 200.00, 40.00",
        "REGULAR, 100.00, 10.00",
        "REGULAR,  50.00,  5.00",
        "NEW,     100.00,  0.00",
        "VIP,     100.00, 30.00",
        "VIP,     200.00, 60.00"
})
void calculateDiscount_returnsCorrectAmount(
        CustomerType customerType,
        BigDecimal amount,
        BigDecimal expectedDiscount) {

    var discount = discountService.calculate(customerType, amount);

    assertThat(discount)
            .as("Rabatt für Kundentyp %s bei Betrag %s", customerType, amount)
            .isEqualByComparingTo(expectedDiscount);
}
```

Wichtig: `@CsvSource` ist gut für einfache Tabellen. Wenn die Werte komplex werden, ist `@MethodSource` besser.

---

## 10. `@CsvSource` mit Text Blocks

Für größere CSV-Tabellen ist das `textBlock`-Attribut deutlich lesbarer als viele einzelne Strings.

```java
@ParameterizedTest(name = "[{index}] {0}: Betrag {1} → Rabatt {2}")
@CsvSource(useHeadersInDisplayName = true, textBlock = """
        CUSTOMER_TYPE, AMOUNT, EXPECTED_DISCOUNT
        PREMIUM,      100.00, 20.00
        PREMIUM,      200.00, 40.00
        REGULAR,      100.00, 10.00
        NEW,          100.00,  0.00
        VIP,          200.00, 60.00
        """)
void calculateDiscount_returnsCorrectAmount_withTextBlock(
        CustomerType customerType,
        BigDecimal amount,
        BigDecimal expectedDiscount) {

    var discount = discountService.calculate(customerType, amount);

    assertThat(discount).isEqualByComparingTo(expectedDiscount);
}
```

Diese Variante passt gut zu Java 21 und zur Projektregel für Text Blocks bei mehrzeiligen strukturierten Inhalten.

---

## 11. Wichtige CSV-Fallen

`@CsvSource` hat spezielle Regeln für leere und `null`-Werte. Eine leere, nicht quotierte Spalte wird als `null` interpretiert. Eine quotierte leere Spalte `''` wird als leerer String interpretiert.

```java
@ParameterizedTest(name = "Eingabe [{0}] wird normalisiert zu [{1}]")
@CsvSource(value = {
        "' Max ', Max",
        "'', ''",
        "NIL, NIL"
}, nullValues = "NIL")
void normalize_handlesEmptyAndNullValues(String input, String expected) {
    assertThat(normalizer.normalize(input)).isEqualTo(expected);
}
```

Für produktive Testqualität gilt: Wenn `null` und leerer String fachlich unterschiedlich sind, müssen sie im Test auch sichtbar unterschiedlich modelliert werden.

---

## 12. Gute Anwendung: `@MethodSource` für komplexe Testdaten

Sobald Testdaten aus Objekten, Exceptions, Commands oder mehreren fachlichen Typen bestehen, ist `@MethodSource` der Standard.

```java
@ParameterizedTest(name = "{0}")
@MethodSource("ungueltigeBestellungen")
void validate_throwsException_forInvalidOrders(
        String scenarioName,
        CreateOrderCommand command,
        Class<? extends Throwable> expectedExceptionType,
        String expectedMessageFragment) {

    assertThatThrownBy(() -> orderValidator.validate(command))
            .isInstanceOf(expectedExceptionType)
            .hasMessageContaining(expectedMessageFragment);
}

static Stream<Arguments> ungueltigeBestellungen() {
    return Stream.of(
            Arguments.of(
                    "Null-UserId wird abgelehnt",
                    new CreateOrderCommand(null, new ProductId("P1"), new Quantity(1)),
                    IllegalArgumentException.class,
                    "userId"
            ),
            Arguments.of(
                    "Null-ProductId wird abgelehnt",
                    new CreateOrderCommand(new UserId(1L), null, new Quantity(1)),
                    IllegalArgumentException.class,
                    "productId"
            ),
            Arguments.of(
                    "Menge 0 wird abgelehnt",
                    new CreateOrderCommand(new UserId(1L), new ProductId("P1"), new Quantity(0)),
                    ValidationException.class,
                    "quantity"
            )
    );
}
```

Die erste Spalte `scenarioName` ist nicht nur Dekoration. Sie macht den fehlgeschlagenen Testfall im Report lesbar.

---

## 13. `@MethodSource`: lokale und externe Quellen

Eine lokale Factory-Methode muss bei normalem `PER_METHOD`-Lifecycle statisch sein. Nicht-statische Factory-Methoden sind möglich, wenn die Testklasse `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` verwendet. Externe Factory-Methoden müssen statisch sein.

```java
@ParameterizedTest(name = "{0}")
@MethodSource("validCommands")
void validate_acceptsValidCommands(String scenario, CreateOrderCommand command) {
    assertThatNoException().isThrownBy(() -> orderValidator.validate(command));
}

static Stream<Arguments> validCommands() {
    return Stream.of(
            Arguments.of("ein Produkt mit Menge 1", validCommand(new Quantity(1))),
            Arguments.of("ein Produkt mit Menge 100", validCommand(new Quantity(100)))
    );
}
```

Für mehrfach genutzte Testdatenquellen ist eine eigene Test-Fixture-Klasse erlaubt:

```java
@ParameterizedTest(name = "{0}")
@MethodSource("com.example.testdata.OrderArguments#invalidOrderCommands")
void validate_rejectsInvalidCommands(String scenario, CreateOrderCommand command) {
    assertThatThrownBy(() -> orderValidator.validate(command))
            .isInstanceOf(RuntimeException.class);
}
```

Externe Quellen dürfen nicht zu unübersichtlichen globalen Testdatenfriedhöfen werden. Sie sind sinnvoll, wenn dieselbe Testdatenmatrix von mehreren Testklassen verwendet wird.

---

## 14. Gute Anwendung: `@EnumSource` für vollständige Status- und Typabdeckung

`@EnumSource` ist besonders wertvoll für State Machines. Wenn ein Enum erweitert wird, werden Tests über alle Werte automatisch angepasst und können fehlende Behandlung sichtbar machen.

```java
@ParameterizedTest(name = "Status {0}: cancel() hat definiertes Verhalten")
@EnumSource(OrderStatus.class)
void cancel_hasDefinedBehaviorForAllStatuses(OrderStatus initialStatus) {
    var order = Order.withStatus(initialStatus);

    switch (initialStatus) {
        case PENDING, PROCESSING -> {
            assertThatNoException().isThrownBy(order::cancel);
            assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        }
        case SHIPPED, DELIVERED, CANCELLED ->
                assertThatThrownBy(order::cancel)
                        .isInstanceOf(OrderCannotBeCancelledException.class)
                        .hasMessageContaining(initialStatus.name());
    }
}
```

Diese Form ist für vollständige Statusabdeckung akzeptabel. Wenn die `switch`-Logik im Test zu komplex wird, sind getrennte `@EnumSource`-Tests für erlaubte und verbotene Gruppen besser.

```java
@ParameterizedTest(name = "Status {0} kann storniert werden")
@EnumSource(value = OrderStatus.class, names = {"PENDING", "PROCESSING"})
void cancel_setsStatusToCancelled_forCancellableStatuses(OrderStatus status) {
    var order = Order.withStatus(status);

    order.cancel();

    assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
}

@ParameterizedTest(name = "Status {0} kann nicht storniert werden")
@EnumSource(value = OrderStatus.class, names = {"SHIPPED", "DELIVERED", "CANCELLED"})
void cancel_throwsException_forTerminalStatuses(OrderStatus status) {
    var order = Order.withStatus(status);

    assertThatThrownBy(order::cancel)
            .isInstanceOf(OrderCannotBeCancelledException.class);
}
```

---

## 15. Grenzwerttests sind Pflicht

Grenzwerte sind nicht „nice to have“. Sie sind in Validierung, Mengen, Preisen, Zeiträumen, Seitenlimits, Batchgrößen und API-Pagination regelmäßig die Fehlerquelle.

```java
@ParameterizedTest(name = "Menge {0} ist gültig")
@ValueSource(ints = {
        1,
        2,
        99,
        100
})
void quantity_acceptsValidBoundaryValues(int value) {
    assertThatNoException().isThrownBy(() -> new Quantity(value));
}

@ParameterizedTest(name = "Menge {0} ist ungültig")
@ValueSource(ints = {
        Integer.MIN_VALUE,
        -1,
        0,
        101,
        Integer.MAX_VALUE
})
void quantity_rejectsInvalidBoundaryValues(int invalidValue) {
    assertThatThrownBy(() -> new Quantity(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("quantity");
}
```

Verbindliche Grenzwertregel:

- Minimum minus 1 wird getestet.
- Minimum wird getestet.
- Minimum plus 1 wird getestet, wenn fachlich relevant.
- Maximum minus 1 wird getestet, wenn fachlich relevant.
- Maximum wird getestet.
- Maximum plus 1 wird getestet.
- Extremwerte wie `Integer.MIN_VALUE` und `Integer.MAX_VALUE` werden getestet, wenn numerische Überläufe relevant sein können.

---

## 16. Null, leer und blank explizit testen

Null-Handling darf nicht implizit bleiben. Für String-Pflichtfelder wird grundsätzlich `@NullAndEmptySource` plus blanke Werte über `@ValueSource` verwendet.

```java
@ParameterizedTest(name = "Name [{0}] wird als ungültig abgelehnt")
@NullAndEmptySource
@ValueSource(strings = {" ", "   ", "\t", "\n"})
void register_rejectsBlankOrNullName(String invalidName) {
    var command = new RegisterUserCommand(invalidName, "max@example.com", "geheim123");

    assertThatThrownBy(() -> userService.register(command))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("name");
}
```

`@NullSource` darf nicht für primitive Parameter verwendet werden, weil primitive Typen kein `null` aufnehmen können.

---

## 17. `@CsvFileSource` für große Tabellen

Wenn eine Testmatrix fachlich groß wird oder von QA, Fachbereich oder Testautomation gepflegt werden soll, kann `@CsvFileSource` verwendet werden.

```text
# src/test/resources/discount-rules.csv
customerType,amount,expectedDiscount
PREMIUM,100.00,20.00
PREMIUM,200.00,40.00
REGULAR,100.00,10.00
NEW,100.00,0.00
VIP,200.00,60.00
```

```java
@ParameterizedTest(name = "[{index}] {0}, Betrag {1} → Rabatt {2}")
@CsvFileSource(resources = "/discount-rules.csv", numLinesToSkip = 1)
void calculateDiscount_usesConfiguredRules(
        CustomerType customerType,
        BigDecimal amount,
        BigDecimal expectedDiscount) {

    var discount = discountService.calculate(customerType, amount);

    assertThat(discount).isEqualByComparingTo(expectedDiscount);
}
```

CSV-Dateien sind nur dann sinnvoll, wenn sie gut gepflegt werden. Eine große CSV-Datei ohne sprechende Szenarienamen wird schnell genauso unlesbar wie kopierte Testmethoden.

---

## 18. `@ArgumentsSource` für wiederverwendbare Spezialquellen

`@ArgumentsSource` ist nicht der Default. Es ist geeignet, wenn eine Argumentquelle mehrfach genutzt wird oder eine eigene Datenbereitstellungsklasse fachlich sinnvoll ist.

```java
class BoundaryQuantityArguments implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(1, true),
                Arguments.of(100, true),
                Arguments.of(0, false),
                Arguments.of(101, false)
        );
    }
}

@ParameterizedTest(name = "Menge {0}: gültig={1}")
@ArgumentsSource(BoundaryQuantityArguments.class)
void quantity_hasExpectedValidity(int value, boolean valid) {
    if (valid) {
        assertThatNoException().isThrownBy(() -> new Quantity(value));
    } else {
        assertThatThrownBy(() -> new Quantity(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

Dieses Beispiel zeigt bewusst auch die Grenze: Wenn der Boolean zu einer `if/else`-Assertion führt, sind zwei getrennte parametrisierte Tests oft klarer. `@ArgumentsSource` sollte deshalb nicht als Ausrede für komplexe Testlogik verwendet werden.

---

## 19. Schlechte Anwendung: parametrisierter Test mit Entscheidungslogik

```java
@ParameterizedTest
@CsvSource({
        "PREMIUM, true",
        "REGULAR, false",
        "NEW, false"
})
void processOrder_behavesCorrectly(CustomerType type, boolean expectsDiscount) {
    var order = createOrderFor(type);

    var result = orderService.process(order);

    if (expectsDiscount) {
        assertThat(result.discountApplied()).isTrue();
        assertThat(result.total()).isLessThan(result.subtotal());
    } else {
        assertThat(result.discountApplied()).isFalse();
        assertThat(result.total()).isEqualByComparingTo(result.subtotal());
    }
}
```

Dieser Test ist schlecht, weil er zwei fachliche Aussagen in eine Methode mischt. Die Assertion-Logik hängt von einem Parameter ab. Das macht den Test schwerer lesbar und erhöht die Gefahr, dass Fehler im Test selbst liegen.

### Besser

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

## 20. Security- und SaaS-Aspekte

Parametrisierte Tests sind besonders wertvoll für Security- und SaaS-Grenzen, weil dieselbe Sicherheitsregel gegen viele Varianten geprüft werden kann.

### Tenant-Isolation

```java
@ParameterizedTest(name = "User aus Tenant {0} darf Bestellung aus Tenant {1} nicht lesen")
@CsvSource({
        "tenant-a, tenant-b",
        "tenant-b, tenant-a",
        "tenant-a, tenant-c"
})
void findOrder_rejectsCrossTenantAccess(String userTenant, String orderTenant) {
    var user = authenticatedUserForTenant(new TenantId(userTenant));
    var orderId = existingOrderForTenant(new TenantId(orderTenant));

    assertThatThrownBy(() -> orderService.findOrder(user, orderId))
            .isInstanceOf(AccessDeniedException.class);
}
```

### Rollen- und Berechtigungsvarianten

```java
@ParameterizedTest(name = "Rolle {0} darf keine Admin-Aktion ausführen")
@EnumSource(value = UserRole.class, names = {"CUSTOMER", "SUPPORT_READONLY", "MERCHANT"})
void deleteTenant_rejectsNonAdminRoles(UserRole role) {
    var user = userWithRole(role);

    assertThatThrownBy(() -> tenantAdminService.deleteTenant(user, new TenantId("t-1")))
            .isInstanceOf(AccessDeniedException.class);
}
```

Security-Tests dürfen aber nicht nur positive Fälle prüfen. Für Berechtigungen, Tenant-Isolation, Objektzugriff und Input Validation sind negative Varianten Pflicht.

---

## 21. Testdatenqualität

Parametrisierte Tests sind nur so gut wie ihre Daten. Gute Testdaten sind klein, fachlich benannt, realistisch genug und frei von sensiblen echten Daten.

Verbindliche Regeln:

- Keine echten personenbezogenen Daten in Testdaten.
- Keine echten E-Mails, IBANs, Tokens, API Keys oder Telefonnummern.
- Keine Produktionsdaten in `@CsvFileSource`-Dateien.
- Szenarienamen müssen fachlich lesbar sein.
- Testdaten sollen Grenzwerte und typische Fehlerfälle enthalten.
- Große Testdatenmengen müssen fachlich gepflegt und versioniert werden.

Gute synthetische Daten:

```java
Arguments.of("gültige synthetische E-Mail", "max@example.test", true)
Arguments.of("leerer lokaler Teil", "@example.test", false)
Arguments.of("Tenant A gegen Tenant B", "tenant-a", "tenant-b")
```

Schlechte Testdaten:

```java
Arguments.of("echte private E-Mail", "vorname.nachname@echte-firma.de", true)
Arguments.of("echter Token", "eyJhbGciOi...", true)
Arguments.of("echte IBAN", "DE...", true)
```

---

## 22. Parametrisierung und `@Nested`

Parametrisierte Tests und `@Nested` ergänzen sich. `@Nested` strukturiert den Kontext, `@ParameterizedTest` strukturiert Varianten innerhalb dieses Kontextes.

```java
class OrderTest {

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @ParameterizedTest(name = "Status {0} kann storniert werden")
        @EnumSource(value = OrderStatus.class, names = {"PENDING", "PROCESSING"})
        void cancelsOrder_forCancellableStatuses(OrderStatus status) {
            var order = Order.withStatus(status);

            order.cancel();

            assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        }

        @ParameterizedTest(name = "Status {0} kann nicht storniert werden")
        @EnumSource(value = OrderStatus.class, names = {"SHIPPED", "DELIVERED", "CANCELLED"})
        void rejectsCancellation_forTerminalStatuses(OrderStatus status) {
            var order = Order.withStatus(status);

            assertThatThrownBy(order::cancel)
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }
    }
}
```

---

## 23. Parametrisierung und Mockito

Parametrisierte Tests mit Mockito sind erlaubt, aber vorsichtig einzusetzen. Jeder Testlauf muss isoliert bleiben. Stub-Konfiguration gehört in die Testmethode, nicht in eine globale Matrix, die schwer verständlich ist.

```java
@ParameterizedTest(name = "Repository liefert {0} → Exception={1}")
@CsvSource({
        "true, false",
        "false, true"
})
void findById_handlesRepositoryResult(boolean userExists, boolean expectsException) {
    when(userRepository.findById(1L))
            .thenReturn(userExists
                    ? Optional.of(new UserEntity(1L, "Max", "max@example.test"))
                    : Optional.empty());

    if (expectsException) {
        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(UserNotFoundException.class);
    } else {
        assertThat(userService.findById(1L).name()).isEqualTo("Max");
    }
}
```

Dieser Test ist zu clever. Besser sind zwei getrennte Tests, weil Rückgabe und Exception zwei unterschiedliche Verhalten sind.

```java
@Test
void findById_returnsUser_whenUserExists() {
    when(userRepository.findById(1L))
            .thenReturn(Optional.of(new UserEntity(1L, "Max", "max@example.test")));

    var result = userService.findById(1L);

    assertThat(result.name()).isEqualTo("Max");
}

@Test
void findById_throwsException_whenUserDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(1L))
            .isInstanceOf(UserNotFoundException.class);
}
```

---

## 24. Anti-Patterns

### 24.1 Ein parametrisierter Test für mehrere Fachregeln

Wenn Testname, Assertion und Erwartung nur noch durch mehrere Parameter erklärbar sind, ist der Test zu breit.

### 24.2 `boolean expected` als Steuerparameter

Ein erwarteter Boolean ist nicht immer falsch, aber oft ein Zeichen dafür, dass zwei semantische Gruppen getrennt werden sollten.

### 24.3 `if/else` in Assertions

Assertions dürfen nicht abhängig vom Testparameter grundlegend verschiedene Pfade ausführen.

### 24.4 Riesige `@CsvSource`-Tabellen im Code

Wenn die Tabelle länger als etwa 15 bis 20 Zeilen wird, ist `@CsvFileSource` oder `@MethodSource` zu prüfen.

### 24.5 Komplexe Objekte in CSV quetschen

Wenn CSV-Spalten JSON, kommaseparierte Listen oder verschachtelte Werte enthalten, ist `@MethodSource` die bessere Wahl.

### 24.6 Zufallsdaten ohne deterministischen Seed

Parametrisierte Tests müssen reproduzierbar sein. Zufällige Testdaten ohne festen Seed sind nicht erlaubt.

### 24.7 Produktionsdaten als Testdaten

Produktionsdaten dürfen nicht als parametrisierte Testdaten verwendet werden. Auch pseudonymisierte Daten müssen hinsichtlich Re-Identifizierungsrisiko geprüft werden.

---

## 25. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Eine Aussage | Prüft der parametrisierte Test eine fachliche Aussage? | „ungültige E-Mails werden abgelehnt“ | Pflicht |
| Keine Testlogik | Gibt es `if/else` in der Assertion? | Boolean steuert Assertions | Vermeiden |
| Sprechender Name | Ist `@ParameterizedTest(name = ...)` gesetzt? | `{0}` oder Szenarioname | Pflicht |
| Passende Quelle | Ist die gewählte Source passend? | `@MethodSource` statt CSV für Objekte | Pflicht |
| Grenzwerte | Sind untere/obere Grenzen enthalten? | 0, 1, max, max+1 | Pflicht bei Validierung |
| Null/Leer | Sind `null`, leer und blank explizit getestet? | `@NullAndEmptySource` | Pflicht bei Strings |
| Enum-Abdeckung | Werden alle relevanten Enum-Werte abgedeckt? | `@EnumSource` | Pflicht bei Statuslogik |
| Lesbarkeit | Bleibt der Testbericht verständlich? | Invocation-Namen | Pflicht |
| Testdaten | Sind Testdaten synthetisch und frei von Secrets? | `example.test` | Pflicht |
| Wartbarkeit | Kann ein neuer Testfall einfach ergänzt werden? | neue `Arguments.of(...)`-Zeile | Soll |
| Isolation | Sind alle Invocations unabhängig? | kein geteilter mutable State | Pflicht |
| SaaS-Sicherheit | Sind Tenant-/Rollenvarianten negativ getestet? | Cross-Tenant Access | Pflicht bei Zugriffskontrolle |

---

## 26. Automatisierbare Prüfungen

Mögliche automatisierbare Regeln:

```text
- Parametrisierte Tests müssen ein name-Attribut besitzen.
- Keine JUnit-Assertions in parametrisierten Tests, wenn AssertJ verfügbar ist.
- Keine @CsvSource-Tabellen mit mehr als N Zeilen ohne Begründung.
- Keine echten Domains, Tokens oder IBANs in Testdaten.
- Keine @TestMethodOrder in Testklassen mit parametrisierten Tests.
- Keine zufälligen Testdaten ohne festen Seed.
- Keine produktionsnahen Secrets in src/test/resources.
```

Beispielhafte ArchUnit-/Checkstyle-Prüfung kann projektspezifisch ergänzt werden. Für viele dieser Regeln ist Semgrep oder ein eigener statischer Check pragmatischer als ArchUnit, weil Annotation-Attribute und Testdatenstrings geprüft werden müssen.

---

## 27. Migration bestehender Tests

Bestehende duplizierte Tests werden schrittweise migriert.

1. Testmethoden mit gleicher Struktur identifizieren.
2. Fachliche Aussage formulieren.
3. Varianten in `@ValueSource`, `@CsvSource` oder `@MethodSource` überführen.
4. `@ParameterizedTest(name = ...)` ergänzen.
5. Assertions auf AssertJ vereinheitlichen.
6. Null-, Leer-, Blank- und Grenzwerte ergänzen.
7. Alte duplizierte Tests entfernen.
8. Testbericht einmal bewusst mit fehlerhaftem Wert prüfen: Ist die Fehlermeldung verständlich?

Vorher:

```java
@Test void quantity_rejectsZero() { assertThatThrownBy(() -> new Quantity(0)).isInstanceOf(IllegalArgumentException.class); }
@Test void quantity_rejectsMinusOne() { assertThatThrownBy(() -> new Quantity(-1)).isInstanceOf(IllegalArgumentException.class); }
@Test void quantity_rejects101() { assertThatThrownBy(() -> new Quantity(101)).isInstanceOf(IllegalArgumentException.class); }
```

Nachher:

```java
@ParameterizedTest(name = "Menge {0} ist ungültig")
@ValueSource(ints = {0, -1, 101})
void quantity_rejectsInvalidValues(int invalidValue) {
    assertThatThrownBy(() -> new Quantity(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
}
```

---

## 28. Ausnahmen

Ein normaler `@Test` ist besser als ein parametrisierter Test, wenn:

- nur ein Szenario existiert,
- das Szenario fachlich besonders wichtig ist,
- die Testdaten stark erklärungsbedürftig sind,
- das Setup je Variante stark unterschiedlich ist,
- die Assertions je Variante fachlich unterschiedlich sind,
- der Testbericht durch Parametrisierung schlechter lesbar wird,
- der Test als Spezifikation eines einzelnen Sonderfalls dienen soll.

Eine Parametrisierung ist kein Qualitätsziel an sich. Lesbarkeit bleibt wichtiger als Kürze.

---

## 29. Definition of Done

Ein parametrisierter Test erfüllt diese Richtlinie, wenn er genau eine fachliche Aussage über mehrere Varianten prüft, eine passende Argumentquelle verwendet, einen sprechenden Invocation-Namen besitzt, keine fachliche Verzweigungslogik in den Assertions enthält, Null-, Leer-, Blank- und Grenzwerte bei Validierungen berücksichtigt, synthetische und sichere Testdaten verwendet, jede Invocation isoliert ausführbar ist und bei Fehlschlag ohne Debugging erkennbar ist, welche Variante warum fehlgeschlagen ist.

---

## 30. Quellen und weiterführende Literatur

| Quelle | Relevanz |
|---|---|
| JUnit 5.10.5 User Guide — Parameterized Tests | Beschreibt `@ParameterizedTest`, notwendige Argumentquellen, `junit-jupiter-params`, `@ValueSource`, Null-/Empty-Sources, `@EnumSource`, `@MethodSource`, `@CsvSource`, `@CsvFileSource`, Argument Conversion und Display Names |
| JUnit 6.0.3 User Guide | Aktuelle JUnit-Dokumentation; bestätigt den Stand der JUnit-Plattform und neue Dokumentationsstruktur |
| Projektstandard AssertJ | Assertions in parametrisierten Tests sollen aussagekräftig und fluent formuliert werden |
| Projektstandard Testklassen-Design | `@Nested` und Parametrisierung werden kombiniert, aber nicht gegeneinander ausgespielt |

