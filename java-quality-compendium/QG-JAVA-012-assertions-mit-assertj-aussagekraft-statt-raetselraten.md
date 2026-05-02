# QG-JAVA-012 — Assertions mit AssertJ: Aussagekraft statt Rätselraten

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-012 |
| Titel | Assertions mit AssertJ: Aussagekraft statt Rätselraten |
| Status | Accepted / verbindlicher Standard für neue und wesentlich überarbeitete Java-Tests |
| Sprache | Deutsch |
| Java-Baseline | Java 21 |
| Test-Baseline | JUnit Jupiter 5.10+; AssertJ Core 3.25+; Spring Boot Test 3.x |
| Kategorie | Testing / Assertions / Clean Code |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, Security Reviewer |
| Verbindlichkeit | Für fachliche Assertions in neuen Tests MUSS AssertJ verwendet werden. JUnit-Assertions wie `assertEquals`, `assertTrue`, `assertFalse`, `assertNull`, `assertNotNull`, `assertThrows` und `fail` DÜRFEN in neuem Projektcode nicht verwendet werden, sofern AssertJ eine gleichwertige oder bessere Ausdrucksform bietet. |
| Qualitätsziel | Fehlgeschlagene Tests sollen ohne Debugging erklären, welches Verhalten verletzt wurde, welcher Wert erwartet war, welcher Wert tatsächlich vorlag und in welchem fachlichen Kontext der Fehler entstanden ist. |
| Prüfstatus | Inhalt fachlich gegen AssertJ-Core-, JUnit-Jupiter- und Spring-Boot-Test-Dokumentation validiert; Codebeispiele benötigen die jeweiligen Testabhängigkeiten im Projekt. |
| Letzte fachliche Prüfung | 2026-05-02 |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Assertions in Java-Tests mit AssertJ geschrieben werden, damit Tests nicht nur grün oder rot sind, sondern im Fehlerfall verständlich erklären, was fachlich verletzt wurde.

Eine Assertion ist die wichtigste Kommunikationsstelle eines Tests. Sie ist der Satz, den der Test im Fehlerfall an den nächsten Entwickler sendet. Schlechte Assertions liefern nur technische Wahrheitswerte wie `expected true but was false`. Gute Assertions zeigen den fachlichen Kontext, den erwarteten Zustand, den tatsächlichen Zustand und möglichst den betroffenen Wert.

Das Ziel ist nicht, eine Bibliothek aus Geschmacksgründen zu bevorzugen. Das Ziel ist **aussagekräftige, wartbare, schnelle und reviewbare Tests**.

---

## 2. Kurzregel für Entwickler

Verwende in Java-Tests standardmäßig AssertJ:

```java
import static org.assertj.core.api.Assertions.*;
```

Schreibe Assertions so, dass sie das beobachtbare Verhalten ausdrücken. Nutze `assertThat(...)`, `assertThatThrownBy(...)`, `assertThatExceptionOfType(...)`, `assertSoftly(...)`, `extracting(...)`, `tuple(...)`, `satisfies(...)`, `usingRecursiveComparison()` und eigene Assertions, wenn dadurch die Fehlermeldung verständlicher wird.

JUnit-Assertions sind in neuem Code nicht erlaubt, wenn AssertJ eine bessere Alternative bietet. Das gilt insbesondere für:

```java
assertEquals(...)
assertTrue(...)
assertFalse(...)
assertNull(...)
assertNotNull(...)
assertThrows(...)
fail(...)
```

Eine Assertion SOLL nicht nur technisch korrekt sein. Sie SOLL für einen Menschen sofort verständlich sein.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Unit-Tests mit JUnit Jupiter,
- Service-Tests,
- Domänen- und Value-Object-Tests,
- Mapper- und Validator-Tests,
- Controller-Slice-Tests,
- Integrationstests mit Spring Boot Test,
- Tests mit Mockito,
- Tests mit Testcontainers,
- Security- und Berechtigungsprüfungen auf Testebene.

Diese Richtlinie gilt nicht als vollständige Anleitung für:

- Property-Based Testing,
- Performance-Tests,
- Lasttests,
- Penetrationstests,
- Contract-Testing-Frameworks,
- Snapshot-Testframeworks.

Auch dort können AssertJ-Assertions eingesetzt werden; die jeweilige Testart besitzt aber zusätzliche Regeln.

---

## 4. Technischer Hintergrund

AssertJ ist eine Fluent-Assertion-Bibliothek für Java. Statt Assertions als isolierte Funktionsaufrufe zu schreiben, werden erwartete Eigenschaften in einer lesbaren Kette ausgedrückt:

```java
assertThat(user.email())
    .as("E-Mail-Adresse des neu registrierten Benutzers")
    .isEqualTo("max@example.com");
```

JUnit Jupiter liefert eigene Assertions mit. Diese sind technisch nutzbar, aber häufig weniger ausdrucksstark. Besonders bei Collections, Exceptions, `Optional`, Zeitwerten, Objektvergleichen und fachlichen Zuständen sind AssertJ-Assertions in der Regel lesbarer und präziser.

Wichtig ist: AssertJ macht eine schlechte Assertion nicht automatisch gut. Eine Assertion wie `assertThat(result.isActive()).isTrue()` ist nur begrenzt besser als `assertTrue(result.isActive())`, wenn kein fachlicher Kontext ergänzt wird. Der Qualitätsgewinn entsteht durch passende AssertJ-APIs, aussagekräftige Beschreibungen, präzise Objektvergleiche und gut benannte Testfälle.

---

## 5. Verbindlicher Standard

### 5.1 AssertJ ist Standard

In neuen und wesentlich überarbeiteten Tests MÜSSEN fachliche Prüfungen mit AssertJ formuliert werden.

Erlaubt:

```java
assertThat(result.id()).isEqualTo(42L);
assertThat(users).extracting(UserDto::email).contains("max@example.com");
assertThatThrownBy(() -> service.findById(99L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessageContaining("99");
```

Nicht erlaubt:

```java
assertEquals(42L, result.id());
assertTrue(users.stream().anyMatch(u -> u.email().equals("max@example.com")));
assertThrows(UserNotFoundException.class, () -> service.findById(99L));
```

### 5.2 Assertions prüfen Verhalten, nicht Implementierung

Eine Assertion MUSS das beobachtbare Ergebnis des Tests prüfen. Sie SOLL nicht die interne Implementierung spiegeln.

Schlecht:

```java
assertThat(repositoryWasCalled).isTrue();
```

Besser:

```java
assertThat(result)
    .extracting(UserDto::id, UserDto::name, UserDto::email)
    .containsExactly(42L, "Max Mustermann", "max@example.com");
```

### 5.3 Fehlermeldung ist Teil der Qualität

Bei fachlich kritischen Assertions MUSS der Kontext mit `as(...)` ergänzt werden, wenn die Standardfehlermeldung nicht ausreichend wäre.

```java
assertThat(result.status())
    .as("Bestellung %s muss nach bestätigter Zahlung den Status PAID haben", orderId)
    .isEqualTo(OrderStatus.PAID);
```

### 5.4 Mehrere Assertions sind erlaubt, wenn sie ein Ergebnis beschreiben

Mehrere Assertions in einem Test sind erlaubt, wenn sie gemeinsam denselben fachlichen Zustand beschreiben.

Erlaubt:

```java
assertSoftly(softly -> {
    softly.assertThat(response.id()).as("Benutzer-ID").isEqualTo(1L);
    softly.assertThat(response.name()).as("Anzeigename").isEqualTo("Max");
    softly.assertThat(response.email()).as("E-Mail-Adresse").isEqualTo("max@example.com");
});
```

Nicht erlaubt ist ein Test, der mehrere unabhängige Verhaltensweisen gleichzeitig prüft, etwa Rückgabeobjekt, E-Mail-Versand, Datenbankpersistenz und Audit-Log in einer einzigen Testmethode.

---

## 6. Gute Anwendung

### 6.1 Einfache Werte aussagekräftig prüfen

```java
@Test
void findById_returnsUser_whenUserExists() {
    // Arrange
    var user = new UserDto(42L, "Max Mustermann", "max@example.com");

    // Act
    var result = user;

    // Assert
    assertThat(result.id())
        .as("Benutzer-ID des gefundenen Benutzers")
        .isEqualTo(42L);

    assertThat(result.name())
        .as("Anzeigename des gefundenen Benutzers")
        .isEqualTo("Max Mustermann");
}
```

### 6.2 Mehrere Felder eines Objekts mit `extracting(...)` prüfen

```java
@Test
void register_returnsCreatedUserResponse_onSuccess() {
    // Arrange
    var response = new UserCreatedResponse(1L, "Max", "max@example.com", true);

    // Act & Assert
    assertThat(response)
        .extracting(
            UserCreatedResponse::id,
            UserCreatedResponse::name,
            UserCreatedResponse::email,
            UserCreatedResponse::active
        )
        .containsExactly(1L, "Max", "max@example.com", true);
}
```

Diese Form ist sinnvoll, wenn alle geprüften Felder gemeinsam denselben erwarteten Zustand beschreiben.

### 6.3 Soft Assertions für zusammengehörige Zustände

```java
@Test
void register_returnsCompleteUserResponse_onSuccess() {
    // Arrange
    var response = new UserCreatedResponse(1L, "Max", "max@example.com", true);

    // Assert
    assertSoftly(softly -> {
        softly.assertThat(response.id())
            .as("ID")
            .isEqualTo(1L);
        softly.assertThat(response.name())
            .as("Name")
            .isEqualTo("Max");
        softly.assertThat(response.email())
            .as("E-Mail")
            .isEqualTo("max@example.com");
        softly.assertThat(response.active())
            .as("Aktivierungsstatus")
            .isTrue();
    });
}
```

Soft Assertions sind sinnvoll, wenn ein Objekt mehrere zusammengehörige Eigenschaften hat und der Entwickler im Fehlerfall alle Abweichungen auf einmal sehen soll.

### 6.4 Collections präzise prüfen

```java
@Test
void findPendingOrders_returnsOnlyPendingOrders_sortedByCreatedAt() {
    // Arrange
    var orders = List.of(
        new OrderSummary(1L, OrderStatus.PENDING, new BigDecimal("19.99")),
        new OrderSummary(2L, OrderStatus.PENDING, new BigDecimal("29.99")),
        new OrderSummary(3L, OrderStatus.PENDING, new BigDecimal("39.99"))
    );

    // Assert
    assertThat(orders)
        .hasSize(3)
        .allMatch(order -> order.status() == OrderStatus.PENDING)
        .extracting(OrderSummary::id)
        .containsExactly(1L, 2L, 3L);
}
```

Wenn Reihenfolge fachlich relevant ist, MUSS `containsExactly(...)` verwendet werden. Wenn Reihenfolge nicht relevant ist, SOLLTE `containsExactlyInAnyOrder(...)` verwendet werden.

```java
assertThat(orders)
    .extracting(OrderSummary::id)
    .containsExactlyInAnyOrder(3L, 1L, 2L);
```

### 6.5 Tupel für Objektlisten verwenden

```java
@Test
void findOrders_returnsExpectedStatusAndTotals() {
    // Arrange
    var orders = List.of(
        new OrderSummary(1L, OrderStatus.PENDING, new BigDecimal("19.99")),
        new OrderSummary(2L, OrderStatus.PAID, new BigDecimal("29.99"))
    );

    // Assert
    assertThat(orders)
        .extracting(OrderSummary::status, OrderSummary::total)
        .containsExactly(
            tuple(OrderStatus.PENDING, new BigDecimal("19.99")),
            tuple(OrderStatus.PAID, new BigDecimal("29.99"))
        );
}
```

### 6.6 Exceptions präzise prüfen

```java
@Test
void findById_throwsUserNotFoundException_whenIdIsUnknown() {
    // Act & Assert
    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("99")
        .hasMessageNotContaining("password")
        .hasNoCause();
}
```

Für konkrete Exception-Typen ist diese Variante oft lesbarer:

```java
assertThatExceptionOfType(UserNotFoundException.class)
    .isThrownBy(() -> userService.findById(99L))
    .withMessageContaining("99")
    .withNoCause();
```

Wenn Felder einer eigenen Exception geprüft werden sollen, ist `catchThrowable(...)` häufig klarer als Reflection-basierte Extraktion:

```java
@Test
void findById_throwsExceptionWithUserId_whenIdIsUnknown() {
    // Act
    var thrown = catchThrowable(() -> userService.findById(99L));

    // Assert
    assertThat(thrown)
        .isInstanceOf(UserNotFoundException.class);

    var exception = (UserNotFoundException) thrown;
    assertThat(exception.userId())
        .as("User-ID in fachlicher Exception")
        .isEqualTo(99L);
}
```

### 6.7 Optional ohne `get()` prüfen

```java
@Test
void findMiddleName_returnsValue_whenMiddleNameExists() {
    // Arrange
    Optional<String> result = Optional.of("Hans");

    // Assert
    assertThat(result)
        .isPresent()
        .contains("Hans");
}
```

```java
@Test
void findMiddleName_returnsEmpty_whenMiddleNameDoesNotExist() {
    // Arrange
    Optional<String> result = Optional.empty();

    // Assert
    assertThat(result).isEmpty();
}
```

Bei Optional-Werten mit Objekten:

```java
assertThat(result)
    .isPresent()
    .hasValueSatisfying(user ->
        assertThat(user.email()).isEqualTo("max@example.com")
    );
```

### 6.8 BigDecimal, Geld und Rundung prüfen

Für Geldbeträge SOLLTE kein `double` verwendet werden. In Tests sind exakte `BigDecimal`-Werte mit String-Konstruktor zu bevorzugen.

```java
@Test
void calculateTotal_returnsExpectedMoneyAmount() {
    // Arrange
    var total = new BigDecimal("19.99");

    // Assert
    assertThat(total).isEqualByComparingTo(new BigDecimal("19.99"));
}
```

Wenn bewusst Toleranz zulässig ist:

```java
assertThat(total)
    .isCloseTo(new BigDecimal("19.99"), within(new BigDecimal("0.01")));
```

### 6.9 Zeit deterministisch prüfen

Zeitabhängige Tests DÜRFEN nicht gegen `Instant.now()` im Assertion-Ausdruck laufen, wenn dadurch Flakiness entstehen kann. Nutze eine feste `Clock`.

```java
@Test
void createToken_setsExpirationToOneHourAfterCreation() {
    // Arrange
    var now = Instant.parse("2026-05-02T10:00:00Z");
    var expiresAt = now.plus(Duration.ofHours(1));

    // Assert
    assertThat(expiresAt)
        .as("Ablaufzeitpunkt des Tokens")
        .isEqualTo(Instant.parse("2026-05-02T11:00:00Z"));
}
```

Für zeitliche Bereiche:

```java
assertThat(result.createdAt())
    .isAfterOrEqualTo(Instant.parse("2026-05-02T10:00:00Z"))
    .isBefore(Instant.parse("2026-05-02T10:01:00Z"));
```

### 6.10 Rekursive Vergleiche für DTOs und Testdaten

`usingRecursiveComparison()` ist sinnvoll für DTOs, Records und verschachtelte Antwortobjekte, wenn ein kompletter Zustand verglichen werden soll.

```java
@Test
void getProfile_returnsExpectedProfile() {
    // Arrange
    var expected = new UserProfileResponse(
        new UserId(42L),
        "Max",
        "max@example.com",
        List.of("ADMIN", "USER")
    );

    var actual = new UserProfileResponse(
        new UserId(42L),
        "Max",
        "max@example.com",
        List.of("ADMIN", "USER")
    );

    // Assert
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
}
```

Diese Methode darf nicht gedankenlos verwendet werden. Wenn einzelne Felder absichtlich ignoriert werden, MUSS der Grund im Test sichtbar sein.

```java
assertThat(actual)
    .usingRecursiveComparison()
    .ignoringFields("technicalId", "createdAt")
    .isEqualTo(expected);
```

---

## 7. Falsche Anwendung und Anti-Patterns

### 7.1 JUnit-Assertions in neuem Code

Schlecht:

```java
assertTrue(result.active());
assertEquals(42L, result.id());
assertFalse(users.isEmpty());
assertNull(result.middleName());
```

Warum ist das schlecht?

- Die Fehlermeldung enthält oft zu wenig fachlichen Kontext.
- Assertions sind weniger gut verkettbar.
- Collections, Exceptions und Optionals werden häufig umständlich geprüft.
- Der Teststil wird im Projekt uneinheitlich.

Besser:

```java
assertThat(result.active())
    .as("Benutzer muss nach E-Mail-Verifikation aktiv sein")
    .isTrue();

assertThat(result.id())
    .as("Benutzer-ID")
    .isEqualTo(42L);

assertThat(users)
    .as("Liste gefundener Benutzer")
    .isNotEmpty();

assertThat(result.middleName()).isNull();
```

### 7.2 `assertThat(boolean).isTrue()` ohne Kontext

Schlecht:

```java
assertThat(order.canBeCancelled()).isTrue();
```

Das ist formal AssertJ, aber fachlich schwach. Die Fehlermeldung sagt nur, dass ein Boolean falsch war.

Besser:

```java
assertThat(order.canBeCancelled())
    .as("Bestellung %s im Status %s muss stornierbar sein", order.id(), order.status())
    .isTrue();
```

Noch besser, wenn dieses Muster häufig vorkommt: eigene Assertion.

```java
OrderAssert.assertThat(order)
    .isCancellable();
```

### 7.3 Manuelle Stream-Assertions statt Collection-API

Schlecht:

```java
assertThat(users.stream().anyMatch(user -> user.email().equals("max@example.com"))).isTrue();
```

Besser:

```java
assertThat(users)
    .extracting(UserDto::email)
    .contains("max@example.com");
```

### 7.4 Index-Zugriffe ohne fachliche Reihenfolge

Schlecht:

```java
assertThat(users.get(0).email()).isEqualTo("max@example.com");
```

Das ist nur korrekt, wenn die Reihenfolge Teil des fachlichen Vertrags ist.

Besser, wenn Reihenfolge egal ist:

```java
assertThat(users)
    .extracting(UserDto::email)
    .contains("max@example.com");
```

Besser, wenn Reihenfolge relevant ist:

```java
assertThat(users)
    .extracting(UserDto::email)
    .containsExactly("anna@example.com", "max@example.com");
```

### 7.5 Zu breite rekursive Vergleiche

Schlecht:

```java
assertThat(actual)
    .usingRecursiveComparison()
    .isEqualTo(expected);
```

Diese Assertion kann gut sein. Sie ist aber schlecht, wenn sie ohne Verständnis genutzt wird und bei Fehlern eine große Objektstruktur ausgibt, die niemand schnell lesen kann.

Besser:

```java
assertThat(actual)
    .as("Öffentlich sichtbare Felder des UserProfileResponse")
    .usingRecursiveComparison()
    .ignoringFields("internalVersion", "createdAt")
    .isEqualTo(expected);
```

Oder bewusst fokussiert:

```java
assertThat(actual)
    .extracting(UserProfileResponse::id, UserProfileResponse::email)
    .containsExactly(new UserId(42L), "max@example.com");
```

### 7.6 `withFailMessage(...)` als Ersatz für gute Assertion

Schlecht:

```java
assertThat(result.status())
    .withFailMessage("Status falsch")
    .isEqualTo(OrderStatus.PAID);
```

Besser:

```java
assertThat(result.status())
    .as("Bestellung %s muss nach Zahlungsbestätigung PAID sein", orderId)
    .isEqualTo(OrderStatus.PAID);
```

`as(...)` beschreibt den Kontext. `withFailMessage(...)` ersetzt die Standardmeldung vollständig und kann dadurch wichtige Details verlieren. `withFailMessage(...)` ist nur sinnvoll, wenn die Standardmeldung bewusst vollständig ersetzt werden soll.

### 7.7 Exception-Message zu exakt prüfen

Schlecht:

```java
assertThatThrownBy(() -> service.findById(99L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessage("User not found: 99");
```

Das koppelt den Test an eine konkrete Textformulierung. Besser ist häufig:

```java
assertThatThrownBy(() -> service.findById(99L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessageContaining("99");
```

Für fachlich stabile Fehlercodes ist ein eigenes Feld besser als ein Message-Vergleich:

```java
var thrown = catchThrowable(() -> service.findById(99L));

assertThat(thrown).isInstanceOf(UserNotFoundException.class);
assertThat(((UserNotFoundException) thrown).errorCode())
    .isEqualTo("USER_NOT_FOUND");
```

---

## 8. Security- und SaaS-Relevanz

Assertions sind ein zentraler Bestandteil sicherer Softwareentwicklung, weil sie Security-Anforderungen ausführbar machen. Ein Test, der nur den Happy Path prüft, schützt keine Mandantengrenzen, keine Berechtigungslogik und keine Datenminimierung.

### 8.1 Sensible Daten dürfen nicht in Fehlerausgaben erwartet werden

Exception- und Response-Tests MÜSSEN prüfen, dass Passwörter, Tokens, Secrets und interne technische Details nicht versehentlich ausgegeben werden, wenn der getestete Pfad solche Daten berühren könnte.

```java
assertThatThrownBy(() -> loginService.login(command))
    .isInstanceOf(AuthenticationFailedException.class)
    .hasMessageNotContaining("password")
    .hasMessageNotContaining("token")
    .hasMessageNotContaining(command.password());
```

### 8.2 Tenant-Isolation präzise prüfen

In SaaS-Systemen DÜRFEN Assertions nicht nur prüfen, dass „irgendein Objekt“ zurückkommt. Sie MÜSSEN prüfen, dass das Objekt zum richtigen Mandanten gehört.

Schlecht:

```java
assertThat(result).isNotNull();
```

Besser:

```java
assertThat(result)
    .extracting(OrderDto::tenantId, OrderDto::orderId)
    .containsExactly(new TenantId("tenant-a"), new OrderId("order-123"));
```

### 8.3 Berechtigungsprüfungen negativ testen

Security-Tests MÜSSEN nicht nur erlaubte Zugriffe prüfen, sondern auch verweigerte Zugriffe.

```java
assertThatThrownBy(() -> orderService.getOrder(otherTenantUser, orderId))
    .isInstanceOf(AccessDeniedException.class)
    .hasMessageNotContaining("internal")
    .hasMessageNotContaining("sql")
    .hasMessageNotContaining("token");
```

### 8.4 API-Responses auf Datenminimierung prüfen

```java
assertThat(response)
    .extracting(UserResponse::id, UserResponse::email, UserResponse::displayName)
    .containsExactly(new UserId(42L), "max@example.com", "Max");

assertThat(response.toString())
    .doesNotContain("password")
    .doesNotContain("secret")
    .doesNotContain("apiKey");
```

Diese `toString()`-Prüfung ist kein Ersatz für sauberes Logging-Design, kann aber bei sicherheitskritischen DTOs als zusätzlicher Schutz sinnvoll sein.

---

## 9. Framework- und Plattform-Kontext

### 9.1 Spring Boot Test

Spring Boot Test bringt über den üblichen Test-Starter typischerweise JUnit Jupiter, AssertJ, Mockito und weitere Testbibliotheken mit. Wenn Spring Boot Dependency Management verwendet wird, SOLLTE die von Spring Boot verwaltete Version nicht ohne konkreten Grund überschrieben werden.

### 9.2 Mockito

Mockito und AssertJ erfüllen unterschiedliche Aufgaben:

| Werkzeug | Aufgabe | Beispiel |
|---|---|---|
| Mockito | Abhängigkeiten ersetzen und Interaktionen bei Nebeneffekten prüfen | `when(repository.findById(...))`, `verify(eventPublisher).publishEvent(...)` |
| AssertJ | Ergebnisse, Exceptions, Collections und Objektzustände prüfen | `assertThat(result).isEqualTo(...)` |

Mockito-`verify(...)` ersetzt keine fachliche Assertion. Wenn ein Rückgabewert vorhanden ist, SOLL dieser mit AssertJ geprüft werden.

### 9.3 JSON, HTTP und API-Tests

In MVC-Tests können Spring- oder JSONPath-Assertions verwendet werden, wenn sie die HTTP-Ebene ausdrücken. Sobald Objekte im Testcode vorliegen, gilt AssertJ als Standard.

Beispiel für HTTP-Ebene:

```java
mockMvc.perform(get("/api/users/42"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.email").value("max@example.com"));
```

Beispiel für Objekt-Ebene:

```java
assertThat(response.email()).isEqualTo("max@example.com");
```

### 9.4 Records und DTOs

Für Records ist `isEqualTo(...)` häufig ausreichend, weil Records `equals(...)` komponentenbasiert erzeugen.

```java
assertThat(actual)
    .isEqualTo(new UserDto(42L, "Max", "max@example.com"));
```

Bei komplexeren DTOs ist `usingRecursiveComparison()` oder `extracting(...)` oft lesbarer.

---

## 10. Designregeln für gute Assertions

| Regel | Details/Erklärung | Beispiel |
|---|---|---|
| Verhalten prüfen | Die Assertion beschreibt das beobachtbare Ergebnis. | `assertThat(response.status()).isEqualTo(PAID)` |
| Kontext ergänzen | Kritische Assertions erhalten `as(...)`. | `.as("Order %s nach Payment", orderId)` |
| Boolean vermeiden oder beschreiben | Reine Boolean-Assertions brauchen Kontext. | `assertThat(canCancel).as("...").isTrue()` |
| Collections mit Collection-API prüfen | Keine manuellen Stream-Booleschen Ausdrücke. | `.extracting(User::email).contains(...)` |
| Reihenfolge bewusst machen | `containsExactly` nur bei fachlicher Reihenfolge. | Sortierte Ergebnisse |
| Exceptions fachlich prüfen | Typ, relevante Message-Teile, Fehlercode, Cause. | `assertThatExceptionOfType(...)` |
| Optionals ohne `get()` prüfen | `contains`, `isPresent`, `hasValueSatisfying`. | `assertThat(optional).contains(value)` |
| Zeit deterministisch prüfen | Keine instabilen Now-Vergleiche. | `Clock.fixed(...)` |
| Rekursive Vergleiche bewusst einsetzen | Nicht als pauschale Abkürzung für alles. | `.usingRecursiveComparison()` |
| Eigene Assertions ab Wiederholung | Ab etwa drittem gleichen Muster lohnt Custom Assertion. | `OrderAssert.assertThat(order).hasStatus(PAID)` |

---

## 11. Eigene Assertions für Domänentypen

Wenn dieselben fachlichen Prüfungen mehrfach auftreten, SOLLTE eine eigene AssertJ-Assertion geschrieben werden. Das verbessert Lesbarkeit, Wiederverwendung und Fehlermeldungen.

```java
import org.assertj.core.api.AbstractAssert;

public final class OrderAssert extends AbstractAssert<OrderAssert, Order> {

    private OrderAssert(Order actual) {
        super(actual, OrderAssert.class);
    }

    public static OrderAssert assertThat(Order actual) {
        return new OrderAssert(actual);
    }

    public OrderAssert hasStatus(OrderStatus expected) {
        isNotNull();
        if (actual.status() != expected) {
            failWithMessage(
                "Expected order <%s> to have status <%s> but was <%s>",
                actual.id(), expected, actual.status()
            );
        }
        return this;
    }

    public OrderAssert isOwnedBy(UserId expectedUserId) {
        isNotNull();
        if (!actual.userId().equals(expectedUserId)) {
            failWithMessage(
                "Expected order <%s> to be owned by <%s> but was owned by <%s>",
                actual.id(), expectedUserId, actual.userId()
            );
        }
        return this;
    }
}
```

Verwendung:

```java
OrderAssert.assertThat(order)
    .hasStatus(OrderStatus.PAID)
    .isOwnedBy(new UserId(42L));
```

Custom Assertions sind besonders sinnvoll für:

- fachliche Statusübergänge,
- Berechtigungs- und Ownership-Regeln,
- Geld-/Steuer-/Rabattlogik,
- API-Response-Strukturen,
- Domänenaggregate mit mehreren Invarianten.

---

## 12. Review-Checkliste

Ein Pull Request erfüllt diese Richtlinie nur, wenn die folgenden Fragen zufriedenstellend beantwortet werden können:

| Aspekt | Prüffrage | Erwartung |
|---|---|---|
| AssertJ-Standard | Werden fachliche Assertions mit AssertJ geschrieben? | Keine neuen JUnit-Assertions im Testcode |
| Aussagekraft | Würde die Fehlermeldung ohne Debugging helfen? | Kontext über `as(...)` oder passende Assertion-API |
| Verhalten | Prüft der Test beobachtbares Verhalten? | Keine reine Implementierungsprüfung |
| Boolean-Assertions | Haben `isTrue()`/`isFalse()` fachlichen Kontext? | `as(...)` oder eigene Assertion |
| Collections | Werden Collections mit AssertJ-Collection-API geprüft? | Kein unnötiger Stream-Boolean |
| Exceptions | Werden Typ, Message-Anteile und sensible Daten geprüft? | Keine bloße „irgendeine Exception“-Prüfung |
| Optional | Wird `Optional.get()` vermieden? | `contains`, `isEmpty`, `hasValueSatisfying` |
| Zeit | Sind Zeitprüfungen deterministisch? | Feste `Clock` oder stabile Zeitfenster |
| SaaS | Werden Tenant, Owner und Permission-Kontext geprüft? | Keine reinen `isNotNull()`-Assertions |
| Security | Wird geprüft, dass Secrets nicht ausgegeben werden? | `doesNotContain(...)` bei kritischen Pfaden |
| Wiederholung | Wäre eine eigene Assertion sinnvoll? | Ab wiederholtem Muster Custom Assertion prüfen |

---

## 13. Automatisierbare Prüfungen

### 13.1 Verbot von JUnit-Assertions per ArchUnit

```java
@AnalyzeClasses(packages = "com.example")
class AssertionArchitectureTest {

    @ArchTest
    static final ArchRule tests_sollen_nicht_von_junit_assertions_abhaengen =
        noClasses()
            .that().resideInAPackage("..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("org.junit.jupiter.api.Assertions")
            .because("QG-JAVA-012: Fachliche Assertions werden mit AssertJ geschrieben.");
}
```

Diese Regel ist bewusst streng. Falls einzelne JUnit-APIs technisch notwendig sind, müssen sie explizit begründet und enger gefiltert werden.

### 13.2 Verbot per Checkstyle oder Forbidden APIs

Mögliche Prüfungen:

```text
org.junit.jupiter.api.Assertions.assertEquals
org.junit.jupiter.api.Assertions.assertTrue
org.junit.jupiter.api.Assertions.assertFalse
org.junit.jupiter.api.Assertions.assertNull
org.junit.jupiter.api.Assertions.assertNotNull
org.junit.jupiter.api.Assertions.assertThrows
org.junit.jupiter.api.Assertions.fail
```

### 13.3 Semgrep-Regeln

Beispielhafte Prüfidee:

```yaml
rules:
  - id: java-junit-assertions-vermeiden
    pattern-either:
      - pattern: assertEquals(...)
      - pattern: assertTrue(...)
      - pattern: assertFalse(...)
      - pattern: assertNull(...)
      - pattern: assertNotNull(...)
      - pattern: assertThrows(...)
    message: "QG-JAVA-012: Verwende AssertJ statt JUnit-Assertions."
    languages: [java]
    severity: WARNING
```

### 13.4 IDE-Inspections

Im Team SOLLTEN IDE-Templates und Inspections so konfiguriert werden, dass:

- `org.assertj.core.api.Assertions.*` als statischer Standardimport vorgeschlagen wird,
- JUnit-Assertions nicht automatisch importiert werden,
- `assertThat(...).isTrue()` ohne `as(...)` bei kritischen Tests im Review auffällt,
- wiederholte Assertion-Blöcke in Custom Assertions überführt werden.

---

## 14. Migration bestehender Tests

Bestehende Tests werden nicht blind mechanisch migriert. Die Migration erfolgt in drei Stufen:

### 14.1 Mechanische Umstellung

```java
assertEquals(expected, actual);
```

wird zu:

```java
assertThat(actual).isEqualTo(expected);
```

```java
assertTrue(condition);
```

wird nicht automatisch nur zu:

```java
assertThat(condition).isTrue();
```

sondern fachlich geprüft:

```java
assertThat(order.canBeCancelled())
    .as("Bestellung %s im Status %s muss stornierbar sein", order.id(), order.status())
    .isTrue();
```

### 14.2 Semantische Verbesserung

Manuelle Stream-Prüfungen werden in Collection-Assertions überführt.

Vorher:

```java
assertTrue(users.stream().anyMatch(u -> u.email().equals("max@example.com")));
```

Nachher:

```java
assertThat(users)
    .extracting(UserDto::email)
    .contains("max@example.com");
```

### 14.3 Fachliche Stabilisierung

Tests mit vielen unabhängigen Assertions werden getrennt oder mit Soft Assertions strukturiert. Tests mit unscharfen Assertions wie `isNotNull()` werden fachlich präzisiert.

Vorher:

```java
assertNotNull(response);
```

Nachher:

```java
assertThat(response)
    .extracting(UserResponse::id, UserResponse::email)
    .containsExactly(new UserId(42L), "max@example.com");
```

---

## 15. Ausnahmen

Ausnahmen sind selten, aber möglich.

JUnit-Assertions DÜRFEN verwendet werden, wenn:

- ein Framework-Beispiel oder eine externe Test-API dies zwingend vorgibt,
- eine Migrationsphase bewusst noch nicht abgeschlossen ist,
- eine technische Spezial-API von JUnit benötigt wird, die keine sinnvolle AssertJ-Entsprechung hat,
- ein Alt-Test nur minimal angepasst wird und eine vollständige Migration nicht Teil des aktuellen Changes ist.

Ausnahmen MÜSSEN im Pull Request begründet werden. Neue Tests SOLLEN keine neuen JUnit-Assertions einführen.

---

## 16. Definition of Done

Ein Test erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Fachliche Assertions werden mit AssertJ geschrieben.
2. Der Test verwendet keine neuen JUnit-Assertions, sofern AssertJ eine geeignete Alternative bietet.
3. Die Assertion prüft beobachtbares Verhalten und nicht nur technische Implementierungsdetails.
4. Kritische Assertions besitzen ausreichenden fachlichen Kontext über `as(...)` oder eigene Assertions.
5. Collections werden mit AssertJ-Collection-API geprüft.
6. Exceptions werden mit Typ, relevanten Message-Anteilen, Cause oder fachlichen Feldern geprüft.
7. Optionals werden ohne `get()` geprüft.
8. Zeit- und Zufallswerte werden deterministisch geprüft.
9. SaaS-Kontexte wie Tenant, Owner und Berechtigungen werden explizit geprüft, wenn sie fachlich relevant sind.
10. Sensible Daten werden in Exception-, Response- und Logging-nahen Tests bewusst ausgeschlossen.
11. Wiederkehrende fachliche Assertion-Muster werden absehbar in Custom Assertions überführt.
12. Die Fehlermeldung eines scheiternden Tests hilft einem Entwickler, die Ursache ohne Debugging einzugrenzen.

---

## 17. Quellen und weiterführende Literatur

- AssertJ — Fluent assertions for Java: https://assertj.github.io/
- AssertJ Core API — Assertions: https://javadoc.io/doc/org.assertj/assertj-core/latest/org/assertj/core/api/Assertions.html
- AssertJ Core API — SoftAssertions: https://javadoc.io/doc/org.assertj/assertj-core/latest/org/assertj/core/api/SoftAssertions.html
- AssertJ Core API — RecursiveComparisonAssert: https://javadoc.io/doc/org.assertj/assertj-core/latest/org/assertj/core/api/RecursiveComparisonAssert.html
- JUnit 5 User Guide: https://docs.junit.org/5.10.2/user-guide/index.html
- Spring Boot Testing Reference: https://docs.spring.io/spring-boot/reference/testing/index.html
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP API Security 2023 — Broken Object Property Level Authorization: https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/

---

## 18. Merksatz

Eine gute Assertion ist kein technischer Wahrheitswert. Eine gute Assertion ist eine präzise fachliche Aussage darüber, welches Verhalten das System zeigen muss und warum der tatsächliche Zustand davon abweicht.
