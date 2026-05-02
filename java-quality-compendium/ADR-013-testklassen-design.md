# ADR-013 — Testklassen-Design: Struktur, Isolation & `@Nested`

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · JUnit 5.10+                            |
| Datum      | 2024-05-22                                  |
| Kategorie  | Testing / Testklassen-Architektur           |

---

## Kontext & Problem

Wächst eine Service-Klasse, wächst ihre Testklasse mit. Nach einiger Zeit hat `UserServiceTest` 40 Methoden, ohne erkennbare Struktur. Wer sucht Tests für `register()`? Wer für den Fehlerfall bei `findById()`? `@Nested`-Klassen in JUnit 5 geben der Testdatei eine lesbare Hierarchie — sie gruppieren Tests nach Methode, Zustand oder Szenario.

---

## Regel 1 — Testklassen mit `@Nested` nach Methoden strukturieren

### Schlecht — flache Testklasse, unstrukturierte 40 Methoden

```java
class UserServiceTest {
    @Test void findById_returnsUser()            { ... }
    @Test void findById_throwsException()        { ... }
    @Test void register_savesUser()              { ... }
    @Test void register_throwsOnDuplicate()      { ... }
    @Test void register_sendsEmail()             { ... }
    @Test void register_throwsOnInvalidName()    { ... }
    @Test void register_throwsOnInvalidEmail()   { ... }
    @Test void updateProfile_updatesName()       { ... }
    @Test void updateProfile_throwsIfNotFound()  { ... }
    // ... 30 weitere, alphabetisch gemischt
}
```

### Gut — `@Nested` nach Methoden gruppiert

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository  userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks UserRegistrationService userService;

    // ─── findById() ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("gibt UserDto zurück wenn User existiert")
        void returnsUser_whenExists() {
            when(userRepository.findById(1L))
                .thenReturn(Optional.of(new UserEntity(1L, "Max", "max@example.com")));

            var result = userService.findById(1L);

            assertThat(result.name()).isEqualTo("Max");
        }

        @Test
        @DisplayName("wirft UserNotFoundException wenn ID unbekannt")
        void throwsException_whenNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    // ─── register() ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("register()")
    class Register {

        private RegisterUserCommand validCommand;

        @BeforeEach
        void setUp() {
            // Setup nur für register()-Tests — nicht für alle Tests der Klasse
            validCommand = new RegisterUserCommand("Max", "max@example.com", "geheim123");
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, UserEntity.class).withId(1L));
        }

        @Test
        @DisplayName("gibt korrektes DTO zurück bei erfolgreicher Registrierung")
        void returnsCorrectDto_onSuccess() {
            var result = userService.register(validCommand);

            assertThat(result)
                .extracting(UserCreatedResponse::name, UserCreatedResponse::email)
                .containsExactly("Max", "max@example.com");
        }

        @Test
        @DisplayName("publiziert UserRegisteredEvent nach erfolgreicher Registrierung")
        void publishesEvent_onSuccess() {
            userService.register(validCommand);

            verify(eventPublisher).publishEvent(
                argThat(e -> e instanceof UserRegisteredEvent evt
                    && evt.email().equals("max@example.com"))
            );
        }

        @Test
        @DisplayName("wirft EmailAlreadyExistsException wenn Email bereits vergeben")
        void throwsException_whenEmailTaken() {
            when(userRepository.existsByEmail("max@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(validCommand))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("max@example.com");
        }

        // ── Validierungsfehler als eigene verschachtelte Gruppe ────────────────
        @Nested
        @DisplayName("Validierung")
        class Validation {

            @Test
            @DisplayName("wirft ValidationException bei leerem Namen")
            void throwsException_whenNameIsBlank() {
                var command = new RegisterUserCommand("", "max@example.com", "geheim123");

                assertThatThrownBy(() -> userService.register(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("name");
            }

            @Test
            @DisplayName("wirft ValidationException bei ungültiger Email")
            void throwsException_whenEmailIsInvalid() {
                var command = new RegisterUserCommand("Max", "kein-email", "geheim123");

                assertThatThrownBy(() -> userService.register(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("email");
            }
        }
    }
}
```

**JUnit 5 Ausgabe in der IDE:**
```
UserServiceTest
├── findById()
│   ├── ✅ gibt UserDto zurück wenn User existiert
│   └── ✅ wirft UserNotFoundException wenn ID unbekannt
└── register()
    ├── ✅ gibt korrektes DTO zurück bei erfolgreicher Registrierung
    ├── ✅ publiziert UserRegisteredEvent nach erfolgreicher Registrierung
    ├── ✅ wirft EmailAlreadyExistsException wenn Email bereits vergeben
    └── Validierung
        ├── ✅ wirft ValidationException bei leerem Namen
        └── ✅ wirft ValidationException bei ungültiger Email
```

---

## Regel 2 — Testklassen nach Zustand gruppieren (State-based Testing)

Wenn eine Klasse unterschiedliche Zustände hat (Order: PENDING, PROCESSING, DELIVERED), nach Zustand statt nach Methode gruppieren.

```java
class OrderTest {

    // ─── Neuen Bestellung (PENDING) ───────────────────────────────────────────
    @Nested
    @DisplayName("Bestellung im Status PENDING")
    class WhenPending {

        private Order order;

        @BeforeEach
        void setUp() {
            order = new Order(new UserId(1L)); // Immer PENDING nach Erstellung
        }

        @Test
        @DisplayName("kann storniert werden")
        void canBeCancelled() {
            assertThatNoException().isThrownBy(order::cancel);
            assertThat(order.status()).isEqualTo(CANCELLED);
        }

        @Test
        @DisplayName("kann Items hinzufügen")
        void canAddItems() {
            order.addItem(new Product("P1"), new Quantity(2));

            assertThat(order.items()).hasSize(1);
        }

        @Test
        @DisplayName("kann bestätigt werden")
        void canBeConfirmed() {
            assertThatNoException().isThrownBy(order::confirm);
            assertThat(order.status()).isEqualTo(PROCESSING);
        }
    }

    // ─── Ausgelieferte Bestellung (DELIVERED) ────────────────────────────────
    @Nested
    @DisplayName("Bestellung im Status DELIVERED")
    class WhenDelivered {

        private Order order;

        @BeforeEach
        void setUp() {
            order = new Order(new UserId(1L));
            order.confirm();  // PENDING → PROCESSING
            order.ship();     // PROCESSING → SHIPPED
            order.deliver();  // SHIPPED → DELIVERED
        }

        @Test
        @DisplayName("kann NICHT storniert werden")
        void cannotBeCancelled() {
            assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCannotBeCancelledException.class)
                .hasMessageContaining("DELIVERED");
        }

        @Test
        @DisplayName("kann KEINE Items mehr hinzufügen")
        void cannotAddItems() {
            assertThatThrownBy(() -> order.addItem(new Product("P2"), new Quantity(1)))
                .isInstanceOf(OrderModificationException.class);
        }
    }
}
```

---

## Regel 3 — Test-Fixtures: Testdaten sauber bereitstellen

Testdaten nicht inline erzeugen — Builder oder Factory-Methoden nutzen.

### Schlecht — Testdaten-Erstellung überall inline

```java
@Test
void register_sendsEmail() {
    // Komplexes Setup inline — dupliziert in 10 anderen Tests
    var user = new UserEntity();
    user.setId(1L);
    user.setName("Max Mustermann");
    user.setEmail("max@example.com");
    user.setCreatedAt(Instant.now());
    user.setActive(true);
    user.setRole(UserRole.USER);
    when(userRepository.save(any())).thenReturn(user);
    // ...
}
```

### Gut — statische Factory-Methoden oder Test-Builder

```java
// In der Testklasse oder einer gemeinsamen TestFixtures-Klasse
class UserServiceTest {

    // Statische Factory-Methode — benennt das Szenario
    private static UserEntity activeUser() {
        return new UserEntity(1L, "Max Mustermann", "max@example.com");
    }

    private static UserEntity userWithId(Long id) {
        return new UserEntity(id, "User " + id, "user" + id + "@example.com");
    }

    private static RegisterUserCommand validRegistrationCommand() {
        return new RegisterUserCommand("Max", "max@example.com", "geheim123");
    }

    @Test
    void register_sendsEmail_onSuccess() {
        when(userRepository.save(any())).thenReturn(activeUser()); // Lesbar, kein Setup-Rauschen

        userService.register(validRegistrationCommand());

        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }
}

// Für komplexere Domänenobjekte: Test-Builder
class OrderTestBuilder {

    private UserId userId  = new UserId(1L);
    private OrderStatus status = PENDING;
    private List<OrderItem> items = new ArrayList<>();

    public static OrderTestBuilder anOrder() { return new OrderTestBuilder(); }

    public OrderTestBuilder forUser(Long userId) {
        this.userId = new UserId(userId);
        return this;
    }

    public OrderTestBuilder withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestBuilder withItem(String productId, int quantity) {
        items.add(new OrderItem(new ProductId(productId), new Quantity(quantity)));
        return this;
    }

    public Order build() {
        return Order.reconstitute(UUID.randomUUID(), userId, status, items);
    }
}

// Verwendung — liest sich wie eine Spezifikation
var deliveredOrder = anOrder()
    .forUser(42L)
    .withStatus(DELIVERED)
    .withItem("P1", 3)
    .build();
```

---

## Regel 4 — `@BeforeEach` vs. `@BeforeAll`

```java
class DatabaseIntegrationTest {

    // @BeforeAll: einmalig für alle Tests der Klasse — nur für teure Ressourcen
    // Muss static sein (außer bei @TestInstance(PER_CLASS))
    @BeforeAll
    static void startDatabase() {
        // Testcontainer starten — teuer, einmal reicht
        postgres.start();
    }

    // @AfterAll: Ressourcen freigeben
    @AfterAll
    static void stopDatabase() {
        postgres.stop();
    }

    // @BeforeEach: vor JEDEM Test — für Datenbankbereinigung
    @BeforeEach
    void cleanDatabase() {
        // Tabellen leeren, Testtransaktionen zurückrollen
        jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    }

    // @AfterEach: für Aufräumarbeiten nach jedem Test
    @AfterEach
    void verifyNoUnwantedInteractions() {
        // Optional: Mockito strict stubs prüfen (passiert automatisch mit MockitoExtension)
    }
}
```

---

## Regel 5 — `@TestInstance(PER_CLASS)` bewusst einsetzen

```java
// Standard: PER_METHOD — JUnit erstellt für jeden Test eine neue Instanz
// Gut: volle Isolation, @BeforeEach und @AfterEach funktionieren wie erwartet.

// PER_CLASS: eine Instanz für alle Tests der Klasse
// Wann sinnvoll: wenn @BeforeAll / @AfterAll nicht-statisch sein muss
// (z. B. bei @Nested-Klassen oder wenn Extensions nicht-statische Felder brauchen)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExpensiveSetupTest {

    private final SomeExpensiveResource resource = new SomeExpensiveResource();

    @BeforeAll // Kann nicht-statisch sein bei PER_CLASS
    void setUp() {
        resource.initialize(); // Einmalig
    }

    @AfterAll
    void tearDown() {
        resource.shutdown();
    }

    @BeforeEach
    void resetState() {
        resource.reset(); // Zustand zurücksetzen — Isolation sicherstellen!
    }

    @Test void test1() { ... }
    @Test void test2() { ... }
}
```

---

## Konsequenzen

**Positiv:**
- `@Nested` gibt der IDE und dem CI eine lesbare Testhierarchie — Fehler sind sofort lokalisierbar.
- `@BeforeEach` in `@Nested`-Klassen gilt nur für Tests dieser Gruppe — kein globaler Setup-Overhead.
- Test-Builder machen Testdaten-Erstellung wiederholt benutzbar und lesbar.

**Negativ:**
- Tiefe `@Nested`-Hierarchien (mehr als 3 Ebenen) werden unübersichtlich — Grenze beachten.
- `@TestInstance(PER_CLASS)` erfordert bewusstes Zurücksetzen des Zustands in `@BeforeEach`.

---

## Tipps

- **Faustregel für `@Nested`**: Wenn eine Testklasse mehr als 8–10 Testmethoden hat, ist es Zeit für `@Nested`.
- **Testhierarchie = lebende Dokumentation**: Die `@DisplayName`-Texte in `@Nested`-Klassen bilden zusammen eine lesbare Spezifikation des Verhaltens.
- **Test-Builder als `internal` Klasse**: Builder für Testdaten gehören in den `test/`-Source-Tree, nie in `main/`.
- **`@Tag` für Test-Kategorien**: `@Tag("slow")`, `@Tag("integration")` — im Build-Tool selektiv ausführen.
 