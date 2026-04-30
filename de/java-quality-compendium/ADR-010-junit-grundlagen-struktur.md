# ADR-010 — JUnit 5: Grundlagen, Struktur & Testbenennung

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · JUnit 5.10+ · Spring Boot Test 3.x     |
| Datum      | 2026-07-03                                  |
| Kategorie  | Testing / Clean Code                        |

---

## Kontext & Problem

Ein Test hat drei Aufgaben: er dokumentiert das erwartete Verhalten, er prüft die Korrektheit, und er zeigt beim Scheitern sofort *was* falsch ist. Schlechte Tests erfüllen keine dieser Aufgaben — sie sind grün wenn alles gut ist, aber beim Scheitern sagt der Fehler nichts, beim Lesen versteht man nicht was getestet wird, und die Struktur zwingt zum Debuggen des Tests selbst.

Dieses ADR definiert die verbindliche Grundstruktur für alle JUnit-5-Tests im Projekt.

---

## Regel 1 — Das AAA-Muster: Arrange, Act, Assert

Jeder Test folgt exakt drei Phasen. Sie sind immer durch eine Leerzeile getrennt und optional mit Kommentar markiert.

### Schlecht — alles vermischt, keine Struktur

```java
@Test
void test1() {
    userService.setRepository(mockRepo);
    when(mockRepo.findById(1L)).thenReturn(Optional.of(new User(1L, "Max")));
    UserDto result = userService.findById(1L);
    assertEquals("Max", result.name());
    verify(mockRepo).findById(1L);
    when(mockRepo.findById(2L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.findById(2L));
}
```

**Warum ist das schlecht?**
- Zwei verschiedene Szenarien in einem Test — bei Fehlschlag ist unklar welches Szenario versagt hat.
- Kein AAA-Trennung — Arrange, Act und Assert sind verwoben und schwer zu lesen.
- `test1` als Name sagt nichts über das erwartete Verhalten.
- Zwei `when()`-Aufrufe für zwei verschiedene Fälle: das sind zwei Tests.

### Gut — sauber getrennte AAA-Struktur

```java
@Test
void findById_returnsUser_whenUserExists() {
    // Arrange
    var expected = new User(1L, "Max Mustermann");
    when(userRepository.findById(1L)).thenReturn(Optional.of(expected));

    // Act
    var result = userService.findById(1L);

    // Assert
    assertThat(result.name()).isEqualTo("Max Mustermann");
}

@Test
void findById_throwsUserNotFoundException_whenUserDoesNotExist() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("99");
}
```

---

## Regel 2 — Testnamen: Verhalten, nicht Implementierung

Ein Testname beschreibt das **beobachtbare Verhalten** — was passiert, unter welcher Bedingung. Nicht was intern aufgerufen wird.

### Namensschema

```
methodName_expectedBehavior_whenCondition()
```

### Schlechte Testnamen

```java
@Test void test()                          {} // Sagt gar nichts
@Test void testFindUser()                  {} // Was wird geprüft?
@Test void userServiceTest()               {} // Kein Verhalten
@Test void shouldWork()                    {} // Bedeutungslos
@Test void findById_callsRepository()      {} // Implementierungsdetail, kein Verhalten
@Test void testFindByIdWithNullThrowsNPE() {} // Zu lang, falsches Format
```

### Gute Testnamen

```java
@Test void findById_returnsUser_whenUserExists()
@Test void findById_throwsUserNotFoundException_whenIdIsUnknown()
@Test void register_throwsEmailAlreadyExistsException_whenEmailIsTaken()
@Test void calculateTotal_returnsZero_whenCartIsEmpty()
@Test void calculateTotal_appliesDiscount_whenUserIsPremiumMember()
@Test void cancel_throwsOrderCannotBeCancelledException_whenOrderIsDelivered()
```

### Mit `@DisplayName` für komplexe Szenarien:

```java
@Test
@DisplayName("Bestellung kann nicht storniert werden, wenn sie bereits ausgeliefert wurde")
void cancel_throwsException_whenAlreadyDelivered() {
    // ...
}

// Oder mit @Nested und @DisplayName für lesbare Testhierarchien (→ ADR-013)
```

---

## Regel 3 — Ein Test, ein Szenario, eine Assert-Aussage

Ein Test prüft **eine** Sache. Mehrere Assertions sind erlaubt, wenn sie zusammen **ein** Ergebnis beschreiben. Sie sind verboten, wenn sie verschiedene Verhaltensaspekte prüfen.

### Schlecht — zu viele unabhängige Assertions

```java
@Test
void register_createsUserAndSendsEmailAndReturnsDtoWithCorrectFields() {
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    var result = userService.register(command);

    // 8 unabhängige Assertions — welche scheitert bei Fehler?
    assertThat(result).isNotNull();
    assertThat(result.id()).isNotNull();
    assertThat(result.name()).isEqualTo("Max");
    assertThat(result.email()).isEqualTo("max@example.com");
    verify(emailService).sendWelcomeEmail(any());
    verify(userRepository).save(any());
    assertThat(result.registeredAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(result.active()).isTrue();
}
```

### Gut — ein Fokus pro Test, Assertions beschreiben ein Ergebnis

```java
@Test
void register_returnsCorrectUserDto_onSuccess() {
    // Arrange
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    // Act
    var result = userService.register(command);

    // Assert — alle Assertions beschreiben EINEN Sachverhalt: das zurückgegebene DTO
    assertThat(result)
        .extracting(UserCreatedResponse::name, UserCreatedResponse::email)
        .containsExactly("Max", "max@example.com");
}

@Test
void register_sendsWelcomeEmail_onSuccess() {
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    userService.register(command);

    verify(emailService).sendWelcomeEmail("max@example.com");
}

@Test
void register_persistsNewUser_onSuccess() {
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    userService.register(command);

    verify(userRepository).save(argThat(u -> u.email().equals("max@example.com")));
}
```

---

## Regel 4 — Keine Logik im Test

Tests dürfen keine if/else, Schleifen oder komplexe Berechnungen enthalten. Logik im Test bedeutet: der Test selbst muss getestet werden.

### Schlecht — Logik im Test

```java
@Test
void calculateDiscount_appliesCorrectRate() {
    List<Customer> customers = List.of(
        new PremiumCustomer("A"),
        new RegularCustomer("B"),
        new NewCustomer("C")
    );

    for (Customer customer : customers) {
        double discount = discountService.calculate(customer);
        // Logik im Test — was ist die erwartete Rate? Woher weiß man das ohne die Implementierung?
        if (customer instanceof PremiumCustomer) {
            assertTrue(discount > 0.15);
        } else {
            assertTrue(discount >= 0.0);
        }
    }
}
```

### Gut — explizite, logikfreie Tests (→ ADR-014 für parametrisierte Tests)

```java
@Test
void calculateDiscount_returns20Percent_forPremiumCustomer() {
    var customer = new PremiumCustomer("A");

    var discount = discountService.calculate(customer);

    assertThat(discount).isEqualTo(0.20);
}

@Test
void calculateDiscount_returns5Percent_forRegularCustomer() {
    var customer = new RegularCustomer("B");

    var discount = discountService.calculate(customer);

    assertThat(discount).isEqualTo(0.05);
}
```

---

## Regel 5 — Test-Isolation: kein gemeinsamer Zustand

Jeder Test läuft in Isolation. Geteilter Zustand zwischen Tests führt zu flaky Tests (mal grün, mal rot) und Reihenfolge-Abhängigkeiten.

### Schlecht — geteilter veränderlicher Zustand

```java
class OrderServiceTest {
    // ❌ Statisch und veränderlich — alle Tests teilen dieses Objekt
    static Order testOrder = new Order();

    @Test
    void addItem_increasesItemCount() {
        testOrder.addItem(new Product("P1"), 2);
        assertThat(testOrder.items()).hasSize(1); // Funktioniert nur wenn zuerst ausgeführt
    }

    @Test
    void cancel_setsStatusToCancelled() {
        testOrder.cancel(); // testOrder könnte bereits Items haben vom vorherigen Test!
        assertThat(testOrder.status()).isEqualTo(CANCELLED);
    }
}
```

### Gut — frisches Objekt für jeden Test

```java
class OrderServiceTest {

    // ✅ Für jeden Test neu erzeugt durch JUnit 5 Lifecycle (Default: PER_METHOD)
    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(new UserId(1L)); // Frisches Objekt, kein geteilter Zustand
    }

    @Test
    void addItem_increasesItemCount() {
        order.addItem(new Product("P1"), new Quantity(2));

        assertThat(order.items()).hasSize(1);
    }

    @Test
    void cancel_setsStatusToCancelled() {
        order.cancel();

        assertThat(order.status()).isEqualTo(CANCELLED);
    }
}
```

---

## Konsequenzen

**Positiv:**
- Grüne Tests sind vertrauenswürdig — keine flaky Tests durch geteilten Zustand.
- Fehlschlagende Tests zeigen sofort: welche Methode, welches Szenario, welche Bedingung.
- Testklassen sind selbstdokumentierend — kein Lesen der Implementierung nötig um zu verstehen was getestet wird.

**Negativ:**
- Mehr Testmethoden für separate Szenarien — initialer Mehraufwand.
- Strikte Benennung erfordert Disziplin im Team — Code-Review-Pflicht.

---

## Tipps

- **`@TestMethodOrder` vermeiden**: wenn Tests eine Reihenfolge brauchen, sind sie nicht isoliert.
- **`@BeforeEach` für Setup, nie für Assertions**: `@BeforeEach` bereitet vor — niemals prüfen.
- **"Arrange" aufwendig?** Das ist ein Zeichen, dass das System-under-Test zu viele Abhängigkeiten hat — kein Test-Problem, sondern ein Design-Problem.
- **Lieber zu viele als zu wenige Tests**: ein Test pro Randfall, pro Happy-Path, pro Fehlerzustand.
 