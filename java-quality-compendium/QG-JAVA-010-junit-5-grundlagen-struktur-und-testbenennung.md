# QG-JAVA-010 — JUnit 5: Grundlagen, Struktur und Testbenennung

## Dokumentstatus

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| Dokumenttyp | Java Quality Guideline | Verbindliche Qualitätsrichtlinie für Tests | JUnit User Guide, Spring Boot Testing Reference |
| ID | QG-JAVA-010 | Fortlaufende Nummer im Java-Qualitätskompendium | Interne Kompendiumsstruktur |
| Titel | JUnit 5: Grundlagen, Struktur und Testbenennung | Standard für lesbare, wartbare und aussagekräftige Unit- und Komponententests | JUnit Jupiter Programming Model |
| Status | Akzeptiert | Für neue Tests verbindlicher Standard | Interner Qualitätsstandard |
| Sprache | Deutsch | Englische Fachbegriffe bleiben dort erhalten, wo sie API- oder Framework-Begriffe sind | Projektvorgabe |
| Java-Baseline | Java 21 | Produktions- und Testcode werden mit Java 21 gedacht | Oracle/OpenJDK Java 21 |
| Test-Baseline | JUnit Jupiter / JUnit 5.10+ als Projektstandard; Regeln sind weitgehend auf neuere JUnit-Jupiter-Versionen übertragbar | `@Test`, `@BeforeEach`, `@Nested`, `@DisplayName`, `@ParameterizedTest` | https://docs.junit.org/5.10.5/user-guide/ |
| Spring-Baseline | Spring Boot Test 3.x | `spring-boot-starter-test`, Test Slices, `@SpringBootTest` | https://docs.spring.io/spring-boot/reference/testing/index.html |
| Kategorie | Testing / Clean Code / Wartbarkeit | Teststruktur, Testnamen, Testisolation, Assertions | JUnit, AssertJ, Mockito, Spring Boot Test |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, Test Automation Engineers | Personen, die Tests schreiben, reviewen oder Teststandards verantworten | Interner Qualitätsstandard |
| Verbindlichkeit | Neue Tests MÜSSEN diese Richtlinie einhalten. Bestehende Tests SOLLTEN bei fachlicher Änderung schrittweise angepasst werden. | Refactoring alter Tests bei Änderung desselben Codes | Interner Qualitätsstandard |
| Prüfstatus | Fachlich gegen JUnit- und Spring-Boot-Dokumentation validiert. JUnit-/AssertJ-/Mockito-Beispiele sind Musterbeispiele und benötigen die jeweiligen Testabhängigkeiten im Projekt. | `spring-boot-starter-test` bringt JUnit Jupiter, AssertJ, Hamcrest und weitere Testbibliotheken mit. | https://docs.spring.io/spring-boot/reference/testing/index.html |
| Letzte fachliche Prüfung | 2026-05-02 | Prüfung gegen verfügbare JUnit-5.10.5- und Spring-Boot-Testdokumentation | Aktueller Kompendiumsstand |

## 1. Zweck

Diese Richtlinie definiert den verbindlichen Grundstandard für JUnit-Tests in Java-21- und Spring-Boot-3.x-Projekten. Ein guter Test dokumentiert das erwartete Verhalten, prüft die Korrektheit und zeigt beim Scheitern möglichst direkt, welches Verhalten gebrochen wurde. Schlechte Tests sind nicht nur lästig; sie erzeugen falsche Sicherheit, verlängern Reviews, verschleiern Designprobleme und machen Refactoring riskant.

Der Zweck dieser Richtlinie ist nicht, jede fortgeschrittene JUnit-Funktion zu erklären. Der Zweck ist, eine gemeinsame Test-Grammatik festzulegen: klare Struktur, klare Namen, ein Szenario pro Test, keine versteckte Testlogik, keine Reihenfolge-Abhängigkeiten und verständliche Assertions.

## 2. Kurzregel für Entwickler

Jeder JUnit-Test MUSS ein klar benanntes Verhalten prüfen, nach Arrange–Act–Assert strukturiert sein und unabhängig von allen anderen Tests laufen. Ein Test SOLL beim Lesen erklären, welches Verhalten unter welcher Bedingung erwartet wird. Ein Test DARF keine versteckte Geschäftslogik, keine Reihenfolge-Abhängigkeit, keinen geteilten veränderlichen Zustand und keine unklare Assertion enthalten.

## 3. Geltungsbereich

Diese Richtlinie gilt für Unit Tests, Komponententests, Service-Tests, Domain-Tests, Controller-Slice-Tests und Spring-Boot-Tests, sofern sie mit JUnit Jupiter geschrieben werden. Sie gilt für Tests mit JUnit Assertions, AssertJ, Mockito und Spring Boot Test.

Sie gilt nicht als vollständiger Standard für Integrationstests mit Datenbanken, Testcontainers, Contract Tests, Performance Tests, Security Tests oder End-to-End-Tests. Für diese Testarten gelten zusätzliche Richtlinien. Die hier beschriebenen Grundregeln bleiben jedoch auch dort nützlich: klare Testnamen, klare Struktur, Isolation, reproduzierbare Daten und verständliche Fehlermeldungen.

## 4. Technischer Hintergrund

JUnit Jupiter ist das Programmier- und Erweiterungsmodell für moderne JUnit-Tests. Die JUnit Platform stellt die Grundlage zur Ausführung von Tests auf der JVM bereit. JUnit Jupiter bietet Annotationen wie `@Test`, `@BeforeEach`, `@AfterEach`, `@Nested`, `@DisplayName`, `@Tag`, `@Timeout` und `@ExtendWith`.

JUnit erzeugt im Standard-Lifecycle `PER_METHOD` für jede Testmethode eine neue Instanz der Testklasse. Dieses Verhalten reduziert unerwartete Seiteneffekte durch veränderlichen Instanzzustand. Wird `@TestInstance(Lifecycle.PER_CLASS)` genutzt, teilen sich alle Testmethoden dieselbe Testinstanz; das kann sinnvoll sein, muss aber bewusst entschieden werden und erhöht das Risiko von Zustandskopplung.

Spring Boot stellt über `spring-boot-starter-test` zentrale Testabhängigkeiten bereit, darunter JUnit Jupiter, AssertJ, Hamcrest und weitere Testbibliotheken. `@SpringBootTest` lädt einen Spring ApplicationContext und ist deshalb mächtiger, aber auch deutlich schwerer als ein reiner Unit Test. Für schnelle Unit Tests SOLL kein Spring Context gestartet werden, wenn einfache Konstruktorinjektion und Mocks ausreichen.

## 5. Verbindlicher Standard

Ein Test erfüllt den Standard dieser Richtlinie, wenn er die folgenden Bedingungen erfüllt: Er hat einen sprechenden Namen, prüft ein klar abgegrenztes Szenario, trennt Vorbereitung, Ausführung und Prüfung sichtbar, verwendet präzise Assertions, ist unabhängig von anderen Tests, enthält keine unnötige Logik und erzeugt bei Fehlschlag eine verständliche Diagnose.

Tests MÜSSEN nicht künstlich akademisch wirken. Sie müssen lesbar, schnell, robust und zuverlässig sein. Ein Entwickler muss aus dem Testnamen und den Assertions erkennen können, welches Verhalten das System garantiert.

## 6. Regel 1 — Arrange, Act, Assert als Grundstruktur

Jeder Test MUSS in drei Phasen gedacht werden: Vorbereitung, Ausführung und Prüfung. Diese Struktur wird als Arrange–Act–Assert bezeichnet. Die Phasen SOLLEN durch Leerzeilen getrennt werden. Kommentare wie `// Arrange`, `// Act` und `// Assert` sind erlaubt und in komplexeren Tests empfohlen. In sehr kleinen Tests dürfen die Kommentare entfallen, wenn die Struktur trotzdem eindeutig ist.

### 6.1 Schlechte Anwendung: mehrere Szenarien in einem Test

```java
@Test
void test1() {
    userService.setRepository(userRepository);
    when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "Max")));
    UserDto result = userService.findById(1L);
    assertEquals("Max", result.name());
    verify(userRepository).findById(1L);

    when(userRepository.findById(2L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.findById(2L));
}
```

Dieser Test ist schlecht, weil er zwei verschiedene Verhaltensfälle prüft: „User existiert“ und „User existiert nicht“. Wenn der Test scheitert, muss der Entwickler erst lesen und debuggen, welcher Teil gebrochen ist. Außerdem ist der Name `test1` wertlos.

### 6.2 Gute Anwendung: ein Szenario pro Test

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

Im zweiten Beispiel sind Act und Assert zusammengezogen, weil eine Exception geprüft wird. Das ist zulässig, sofern der erwartete Fehlerzustand klar bleibt.

## 7. Regel 2 — Testnamen beschreiben Verhalten, nicht Implementierung

Ein Testname MUSS beschreiben, welches beobachtbare Verhalten unter welcher Bedingung erwartet wird. Der Testname DARF nicht nur die getestete Methode oder ein internes Implementierungsdetail nennen.

Das bevorzugte Namensschema lautet:

```text
methodName_expectedBehavior_whenCondition()
```

Dieses Schema ist kein Selbstzweck. Es zwingt den Entwickler, über Verhalten und Bedingung nachzudenken. Wenn ein Test nicht sinnvoll in diesem Schema benannt werden kann, ist häufig unklar, was der Test eigentlich prüft.

### 7.1 Schlechte Testnamen

```java
@Test void test() {}
@Test void testFindUser() {}
@Test void userServiceTest() {}
@Test void shouldWork() {}
@Test void findById_callsRepository() {}
@Test void testFindByIdWithNullThrowsNPE() {}
```

Diese Namen sind schwach. Sie dokumentieren kein klares Verhalten. Besonders `findById_callsRepository()` ist gefährlich: Der Test prüft ein Implementierungsdetail statt eines beobachtbaren Ergebnisses. Wenn ein Service später über Cache, Projection oder andere Infrastruktur lädt, kann das Verhalten korrekt bleiben, obwohl der Test unnötig bricht.

### 7.2 Gute Testnamen

```java
@Test void findById_returnsUser_whenUserExists() {}
@Test void findById_throwsUserNotFoundException_whenIdIsUnknown() {}
@Test void register_throwsEmailAlreadyExistsException_whenEmailIsTaken() {}
@Test void calculateTotal_returnsZero_whenCartIsEmpty() {}
@Test void calculateTotal_appliesDiscount_whenUserIsPremiumMember() {}
@Test void cancel_throwsOrderCannotBeCancelledException_whenOrderIsDelivered() {}
```

### 7.3 `@DisplayName` für fachlich lesbare Testausgaben

`@DisplayName` DARF verwendet werden, wenn Testreports oder fachlich lesbare Ausgaben verbessert werden sollen. Der Methodenname MUSS trotzdem verständlich bleiben.

```java
@Test
@DisplayName("Bestellung kann nicht storniert werden, wenn sie bereits ausgeliefert wurde")
void cancel_throwsOrderCannotBeCancelledException_whenOrderIsDelivered() {
    // Arrange
    var order = deliveredOrder();

    // Act & Assert
    assertThatThrownBy(order::cancel)
        .isInstanceOf(OrderCannotBeCancelledException.class);
}
```

## 8. Regel 3 — Ein Test prüft ein Szenario

Ein Test MUSS ein fachlich zusammenhängendes Szenario prüfen. Mehrere Assertions sind erlaubt, wenn sie gemeinsam ein einziges Ergebnis beschreiben. Mehrere Assertions sind nicht erlaubt, wenn sie verschiedene Verhalten, Nebeneffekte oder unabhängige Verantwortlichkeiten prüfen.

### 8.1 Schlechte Anwendung: zu viele unabhängige Erwartungen

```java
@Test
void register_createsUserAndSendsEmailAndReturnsDtoWithCorrectFields() {
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    var result = userService.register(command);

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

Dieser Test prüft DTO-Mapping, Persistenz, E-Mail-Nebeneffekt und Statuslogik gleichzeitig. Das wirkt effizient, erzeugt aber schlechte Diagnose. Bei Fehlschlag ist nicht klar, ob die Registrierung fachlich falsch ist, das Mapping gebrochen ist, der Mock falsch konfiguriert ist oder ein Nebeneffekt nicht ausgelöst wurde.

### 8.2 Gute Anwendung: ein Fokus pro Test

```java
@Test
void register_returnsCorrectUserDto_onSuccess() {
    // Arrange
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    // Act
    var result = userService.register(command);

    // Assert
    assertThat(result)
        .extracting(UserCreatedResponse::name, UserCreatedResponse::email)
        .containsExactly("Max", "max@example.com");
}

@Test
void register_sendsWelcomeEmail_onSuccess() {
    // Arrange
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    // Act
    userService.register(command);

    // Assert
    verify(emailService).sendWelcomeEmail("max@example.com");
}

@Test
void register_persistsNewUser_onSuccess() {
    // Arrange
    var command = new RegisterUserCommand("Max", "max@example.com", "secret123");

    // Act
    userService.register(command);

    // Assert
    verify(userRepository).save(argThat(user -> user.email().equals("max@example.com")));
}
```

Diese Tests sind länger als ein einzelner Sammeltest, aber sie sind deutlich leichter zu verstehen, zu reparieren und zu refactoren.

## 9. Regel 4 — Keine unnötige Logik im Test

Tests SOLLEN möglichst deklarativ sein. Ein Test DARF keine komplexen `if`-/`else`-Ketten, keine fachlichen Berechnungen und keine versteckte Erwartungslogik enthalten. Eine einfache Schleife kann in Ausnahmefällen legitim sein, etwa bei technischen Wiederholungen. Für mehrere fachliche Eingabefälle SOLLEN parametrisierte Tests verwendet werden.

### 9.1 Schlechte Anwendung: Testlogik spiegelt Produktionslogik

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

        if (customer instanceof PremiumCustomer) {
            assertThat(discount).isGreaterThan(0.15);
        } else {
            assertThat(discount).isGreaterThanOrEqualTo(0.0);
        }
    }
}
```

Dieser Test enthält eigene Entscheidungslogik. Damit wird die Erwartung unklar. Außerdem prüft er nur unpräzise Bereiche statt konkrete fachliche Werte.

### 9.2 Gute Anwendung: explizite Fälle

```java
@Test
void calculateDiscount_returns20Percent_forPremiumCustomer() {
    // Arrange
    var customer = new PremiumCustomer("A");

    // Act
    var discount = discountService.calculate(customer);

    // Assert
    assertThat(discount).isEqualTo(0.20);
}

@Test
void calculateDiscount_returns5Percent_forRegularCustomer() {
    // Arrange
    var customer = new RegularCustomer("B");

    // Act
    var discount = discountService.calculate(customer);

    // Assert
    assertThat(discount).isEqualTo(0.05);
}
```

### 9.3 Gute Anwendung: parametrisierter Test für gleichartige Fälle

```java
@ParameterizedTest(name = "{0} erhält {1} Rabatt")
@MethodSource("discountCases")
void calculateDiscount_returnsExpectedRate_forCustomerType(Customer customer, double expectedRate) {
    // Act
    var discount = discountService.calculate(customer);

    // Assert
    assertThat(discount).isEqualTo(expectedRate);
}

static Stream<Arguments> discountCases() {
    return Stream.of(
        Arguments.of(new PremiumCustomer("A"), 0.20),
        Arguments.of(new RegularCustomer("B"), 0.05),
        Arguments.of(new NewCustomer("C"), 0.00)
    );
}
```

Parametrisierte Tests sind sinnvoll, wenn dieselbe Regel mit mehreren Eingabewerten geprüft wird. Sie sind nicht sinnvoll, wenn jeder Fall eigene Vorbereitung, eigene Assertions oder eigene fachliche Erklärung benötigt.

## 10. Regel 5 — Test-Isolation: kein geteilter veränderlicher Zustand

Jeder Test MUSS unabhängig ausführbar sein. Tests DÜRFEN nicht davon abhängen, dass ein anderer Test vorher oder nachher läuft. `@TestMethodOrder` DARF nicht verwendet werden, um fachliche Abhängigkeiten zwischen Tests zu erzwingen.

### 10.1 Schlechte Anwendung: statischer veränderlicher Zustand

```java
class OrderServiceTest {

    static Order testOrder = new Order(new UserId(1L));

    @Test
    void addItem_increasesItemCount() {
        testOrder.addItem(new Product("P1"), new Quantity(2));

        assertThat(testOrder.items()).hasSize(1);
    }

    @Test
    void cancel_setsStatusToCancelled() {
        testOrder.cancel();

        assertThat(testOrder.status()).isEqualTo(OrderStatus.CANCELLED);
    }
}
```

Dieser Test ist anfällig für Reihenfolge-Probleme. Sobald ein Test den gemeinsamen Zustand verändert, wird der nächste Test vom vorherigen Test beeinflusst.

### 10.2 Gute Anwendung: frische Testdaten pro Test

```java
class OrderServiceTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(new UserId(1L));
    }

    @Test
    void addItem_increasesItemCount() {
        // Act
        order.addItem(new Product("P1"), new Quantity(2));

        // Assert
        assertThat(order.items()).hasSize(1);
    }

    @Test
    void cancel_setsStatusToCancelled() {
        // Act
        order.cancel();

        // Assert
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }
}
```

JUnit Jupiter nutzt standardmäßig `PER_METHOD`; trotzdem ist `@BeforeEach` oft sinnvoll, um pro Test explizit frische Objekte bereitzustellen. `@BeforeEach` DARF vorbereiten, aber keine Assertions enthalten.

## 11. Regel 6 — Assertions müssen Diagnose liefern

Assertions MÜSSEN so formuliert sein, dass ein Fehlschlag verständlich ist. `assertTrue(...)` und `assertFalse(...)` sind nur erlaubt, wenn ihre Bedeutung unmittelbar erkennbar ist oder eine klare Fehlermeldung ergänzt wird. Fluent Assertions mit AssertJ SOLLEN bevorzugt werden, weil sie erwartete und tatsächliche Werte lesbarer darstellen können.

### 11.1 Schlechte Anwendung: unklare boolesche Assertion

```java
assertTrue(result.size() > 0);
assertTrue(user.name().startsWith("M"));
assertTrue(order.total().compareTo(BigDecimal.ZERO) > 0);
```

Diese Assertions sagen beim Fehlschlag wenig. Der Leser muss selbst rekonstruieren, was erwartet wurde.

### 11.2 Gute Anwendung: sprechende Assertions

```java
assertThat(result).isNotEmpty();
assertThat(user.name()).startsWith("M");
assertThat(order.total()).isPositive();
```

### 11.3 Gute Anwendung: Assertion mit fachlichem Kontext

```java
assertThat(order.status())
    .as("Eine erfolgreich stornierte Bestellung muss den Status CANCELLED haben")
    .isEqualTo(OrderStatus.CANCELLED);
```

Assertions SOLLEN nicht nur technisch korrekt sein. Sie sollen beim Scheitern den fachlichen Fehler sichtbar machen.

## 12. Regel 7 — Mocks sparsam und zielgerichtet einsetzen

Mocks sind ein Werkzeug, kein Qualitätsmerkmal. Ein Unit Test DARF Mocks verwenden, wenn externe Abhängigkeiten isoliert werden sollen. Ein Test SOLLTE nicht mehr Mocks verwenden als notwendig. Wenn ein Test viele Mocks braucht, ist das häufig ein Hinweis auf zu hohe Kopplung oder eine zu große Klasse.

### 12.1 Gute Anwendung mit MockitoExtension

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_returnsUser_whenUserExists() {
        // Arrange
        var user = new User(1L, "Max");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        var result = userService.findById(1L);

        // Assert
        assertThat(result.name()).isEqualTo("Max");
    }
}
```

`verify(...)` SOLL nur genutzt werden, wenn der beobachtbare Effekt tatsächlich die Interaktion ist, etwa bei Ports, Gateways, Event Publishern oder Benachrichtigungskomponenten. Es SOLL nicht jede interne Repository-Interaktion überprüft werden, wenn das eigentliche Ergebnis bereits fachlich geprüft wird.

## 13. Regel 8 — Spring Context nur starten, wenn er gebraucht wird

Ein Unit Test SOLL keinen Spring ApplicationContext starten. `@SpringBootTest` ist für Tests gedacht, die Spring Boot Features, Konfiguration, Auto-Configuration oder echte Integration über mehrere Beans benötigen. Für Service- oder Domain-Logik ist ein schneller Test mit Konstruktorinjektion, echten Objekten und gezielten Mocks meist besser.

### 13.1 Schlechte Anwendung: schwerer Context für einfache Logik

```java
@SpringBootTest
class PriceCalculatorTest {

    @Autowired
    private PriceCalculator priceCalculator;

    @Test
    void calculateTotal_returnsSumOfItems() {
        var total = priceCalculator.calculateTotal(List.of(
            new Item("A", BigDecimal.TEN),
            new Item("B", BigDecimal.ONE)
        ));

        assertThat(total).isEqualByComparingTo("11.00");
    }
}
```

Dieser Test lädt unnötig den Spring Context für reine Berechnungslogik.

### 13.2 Gute Anwendung: reiner Unit Test

```java
class PriceCalculatorTest {

    private final PriceCalculator priceCalculator = new PriceCalculator();

    @Test
    void calculateTotal_returnsSumOfItems() {
        // Arrange
        var items = List.of(
            new Item("A", BigDecimal.TEN),
            new Item("B", BigDecimal.ONE)
        );

        // Act
        var total = priceCalculator.calculateTotal(items);

        // Assert
        assertThat(total).isEqualByComparingTo("11.00");
    }
}
```

### 13.3 Gute Anwendung: Spring Boot Test, wenn Integration relevant ist

```java
@SpringBootTest
class UserRegistrationIntegrationTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Test
    void register_persistsUserAndPublishesEvent_whenCommandIsValid() {
        // Integration über Spring-Konfiguration, Transaktion und Event-Verarbeitung ist hier Teil des Tests.
    }
}
```

Bei Controller-, Repository- oder JSON-Slice-Tests SOLLEN passende Spring-Boot-Test-Slices verwendet werden, wenn sie das Ziel präziser und schneller prüfen als ein vollständiger `@SpringBootTest`.

## 14. Regel 9 — Zeit, Zufall und externe Ressourcen kontrollieren

Tests MÜSSEN deterministisch sein. Sie DÜRFEN nicht davon abhängen, welche Uhrzeit gerade ist, welche Zufallszahl erzeugt wird, ob ein externer Dienst erreichbar ist oder wie schnell ein Thread geplant wird.

### 14.1 Schlechte Anwendung: `Instant.now()` direkt im Testziel

```java
@Test
void token_isExpired_whenExpirationIsInPast() {
    var token = new Token(Instant.now().minusSeconds(1));

    assertThat(token.isExpired()).isTrue();
}
```

Dieser Test kann instabil werden, wenn Zeit im Produktionscode und Testcode unterschiedlich gelesen wird.

### 14.2 Gute Anwendung: `Clock` injizieren

```java
@Test
void token_isExpired_whenExpirationIsBeforeCurrentTime() {
    // Arrange
    var fixedClock = Clock.fixed(Instant.parse("2026-05-02T10:00:00Z"), ZoneOffset.UTC);
    var token = new Token(Instant.parse("2026-05-02T09:59:59Z"), fixedClock);

    // Act
    var expired = token.isExpired();

    // Assert
    assertThat(expired).isTrue();
}
```

Für Zufall SOLL ein kontrollierbarer Generator oder fester Seed genutzt werden. Für externe Ressourcen SOLLEN Test Doubles, lokale Testcontainer oder klar isolierte Integrationsumgebungen verwendet werden.

## 15. Regel 10 — Keine sensiblen Daten in Tests

Testcode DARF keine echten Passwörter, Tokens, privaten Kundendaten, Produktionsdaten, API Keys oder personenbezogene Echtdaten enthalten. Testdaten SOLLEN künstlich, minimal und fachlich aussagekräftig sein.

Schlecht:

```java
var command = new LoginCommand("real.customer@example.com", "RealPasswordFromTicket123!");
```

Gut:

```java
var command = new LoginCommand("user@example.test", "valid-Test-Password-123");
```

Für SaaS-Systeme MÜSSEN Tests Mandantenkontext und Berechtigungsgrenzen explizit prüfen, wenn die getestete Logik tenant-relevant ist. Ein Test, der nur den Happy Path eines Mandanten prüft, reicht nicht aus, wenn die Methode Daten über Tenant-Grenzen hinweg laden könnte.

## 16. Gute Gesamtstruktur einer Testklasse

Eine Testklasse SOLLTE konsistent aufgebaut sein. Imports und Annotationen folgen den üblichen Projektregeln. Testdaten werden über Builder, Fixtures oder Factory-Methoden erstellt, aber nicht so stark abstrahiert, dass der Testfall unlesbar wird.

```java
@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void register_returnsCreatedUser_whenEmailIsAvailable() {
        // Arrange
        var command = validRegisterUserCommand();
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(command.password())).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> persistedUser(invocation.getArgument(0)));

        // Act
        var result = userRegistrationService.register(command);

        // Assert
        assertThat(result)
            .extracting(UserCreatedResponse::name, UserCreatedResponse::email)
            .containsExactly("Max Mustermann", "max@example.test");
    }

    @Test
    void register_throwsEmailAlreadyExistsException_whenEmailIsTaken() {
        // Arrange
        var command = validRegisterUserCommand();
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userRegistrationService.register(command))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining(command.email());
    }

    private static RegisterUserCommand validRegisterUserCommand() {
        return new RegisterUserCommand("Max Mustermann", "max@example.test", "valid-Test-Password-123");
    }
}
```

## 17. Anti-Patterns

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| `test1`, `shouldWork`, `happyPath` | Namen ohne Verhalten erzeugen schlechte Diagnose | `@Test void test1()` | JUnit User Guide: `@Test`, `@DisplayName` |
| Mehrere Szenarien pro Test | Ein Testbruch ist nicht eindeutig zuordenbar | Existenzfall und Fehlerfall im selben Test | Testdesign-Grundsatz: ein Szenario pro Test |
| Überall `@SpringBootTest` | Langsame, schwere Tests für einfache Logik | Context-Start für reine Berechnung | Spring Boot Testing Reference |
| Geteilter statischer Zustand | Erzeugt Reihenfolge-Abhängigkeit und Flakiness | `static Order testOrder` | JUnit PER_METHOD Lifecycle |
| `@TestMethodOrder` für fachliche Reihenfolge | Versteckt Kopplung zwischen Tests | Login-Test muss vor Checkout-Test laufen | JUnit unterstützt Method Order, aber isolierte Tests sind Qualitätsstandard |
| Unklare `assertTrue`-Prüfungen | Schlechte Fehlermeldungen und wenig fachlicher Kontext | `assertTrue(result.size() > 0)` | AssertJ-Dokumentation |
| Testlogik mit `if`/`else` | Der Test reproduziert Produktionslogik | `if (customer instanceof PremiumCustomer)` | Testdesign-Grundsatz: erwartete Werte explizit machen |
| Blindes `verify` | Test koppelt sich an Implementierung statt Verhalten | `verify(repository).findById(id)` trotz Ergebnisprüfung | Mockito als Werkzeug, nicht als Testziel |
| `Thread.sleep` | Zeitabhängige Tests werden instabil und langsam | `Thread.sleep(1000)` | JUnit `@Timeout`, deterministisches Design |
| Echte Secrets im Testcode | Sicherheits- und Compliance-Risiko | Produktions-Token in Testdaten | OWASP Logging/Sensitive Data Grundsätze |

## 18. Security- und SaaS-Aspekte

Tests müssen sicherheitsrelevante Regeln sichtbar machen. In SaaS-Systemen betrifft das besonders Mandantentrennung, Berechtigungen, Eingabevalidierung, Fehlersemantik und sensible Daten.

Ein Service-Test für eine tenant-relevante Methode SOLLTE nicht nur prüfen, dass ein User seine eigenen Daten sieht. Er MUSS auch prüfen, dass Daten eines anderen Mandanten nicht zurückgegeben oder verändert werden können, sofern die getestete Methode Zugriffskontrolle oder Datenfilterung verantwortet.

Beispiel:

```java
@Test
void findOrder_throwsAccessDenied_whenOrderBelongsToDifferentTenant() {
    // Arrange
    var currentTenant = new TenantId("tenant-a");
    var foreignOrder = orderOfTenant("tenant-b");
    when(orderRepository.findById(foreignOrder.id())).thenReturn(Optional.of(foreignOrder));

    // Act & Assert
    assertThatThrownBy(() -> orderService.findOrder(currentTenant, foreignOrder.id()))
        .isInstanceOf(AccessDeniedException.class);
}
```

Tests DÜRFEN sensible Inhalte nicht unnötig in Assertion-Meldungen, Logs oder Snapshot-Dateien schreiben. Fehlerausgaben werden häufig in CI-Systemen gespeichert. Deshalb sind künstliche Testdaten und maskierte Werte Pflicht.

## 19. Automatisierbare Prüfungen

Ein Teil dieser Richtlinie kann automatisiert geprüft werden. Nicht alles lässt sich maschinell bewerten, aber Automatisierung reduziert Review-Aufwand und verhindert offensichtliche Rückfälle.

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| Testnamenskonvention | Testmethoden sollen dem Schema `method_expected_whenCondition` folgen | Regex-Prüfung in Checkstyle oder ArchUnit | Interner Qualitätsstandard |
| Verbot von `@TestMethodOrder` | Testreihenfolge darf nicht zur fachlichen Voraussetzung werden | ArchUnit-Regel gegen `@TestMethodOrder` | JUnit User Guide |
| Verbot von `Thread.sleep` | Zeitabhängige Tests werden langsam und instabil | Semgrep/Checkstyle gegen `Thread.sleep` in `src/test` | JUnit `@Timeout`, deterministische Tests |
| Begrenzung von `@SpringBootTest` | Full-Context-Tests sollen bewusst eingesetzt werden | Review-Gate oder ArchUnit für Testpackages | Spring Boot Testing Reference |
| Keine echten Domains/Secrets | Testdaten sollen künstlich sein | Secret Scanning in CI | OWASP Logging/Sensitive Data Grundsätze |
| Keine leeren Testmethoden | Tests ohne Assertion oder erwartete Exception sind nutzlos | Mutation Testing, PIT, IDE Inspection | Testing Best Practices |
| Assertions prüfen | `assertTrue` mit komplexem Ausdruck markieren | PMD/Sonar-Regel | AssertJ als bevorzugte Assertion-Bibliothek |

Beispielhafte ArchUnit-Idee:

```java
@AnalyzeClasses(packages = "com.example")
class TestArchitectureRules {

    @ArchTest
    static final ArchRule tests_sollen_nicht_testmethodorder_verwenden =
        noClasses()
            .that().resideInAPackage("..")
            .should().beAnnotatedWith(TestMethodOrder.class)
            .because("Tests müssen unabhängig sein und dürfen keine fachliche Ausführungsreihenfolge benötigen.");
}
```

## 20. Migration bestehender Tests

Bestehende Tests müssen nicht in einem großen Schritt umgeschrieben werden. Migration erfolgt opportunistisch, wenn der betroffene Produktionscode oder die Testklasse ohnehin geändert wird.

Empfohlene Reihenfolge:

1. Testnamen auf Verhalten umstellen.
2. Mehrere Szenarien in einzelne Tests aufteilen.
3. Arrange–Act–Assert sichtbar trennen.
4. Unklare Assertions durch sprechende AssertJ-Assertions ersetzen.
5. Geteilten Zustand entfernen.
6. Unnötige `@SpringBootTest`-Tests in Unit Tests oder passende Slices überführen.
7. Zeit, Zufall und externe Ressourcen kontrollierbar machen.
8. Security- und Tenant-Negativfälle ergänzen.

Eine Migration ist erfolgreich, wenn die Tests schneller, klarer, unabhängiger und beim Fehlschlag aussagekräftiger sind.

## 21. Ausnahmen

Ausnahmen sind zulässig, müssen aber begründet sein.

`@TestInstance(Lifecycle.PER_CLASS)` darf verwendet werden, wenn teure Testressourcen bewusst einmal pro Klasse aufgebaut werden und der Zustand zwischen Tests zuverlässig zurückgesetzt wird. `@TestMethodOrder` darf nur in seltenen technischen Demonstrations- oder Migrationsfällen verwendet werden, nicht als fachliche Kopplung. `@SpringBootTest` darf verwendet werden, wenn die Spring-Boot-Konfiguration selbst Teil des getesteten Verhaltens ist. Mehrere Assertions in einem Test sind zulässig, wenn sie gemeinsam ein Ergebnis beschreiben.

## 22. Review-Checkliste

- Beschreibt der Testname ein beobachtbares Verhalten?
- Ist die Bedingung im Namen klar erkennbar?
- Prüft der Test genau ein Szenario?
- Sind Arrange, Act und Assert sichtbar getrennt?
- Sind mehrere Assertions fachlich ein gemeinsames Ergebnis?
- Gibt es unnötige Testlogik mit `if`, `else`, Schleifen oder Berechnungen?
- Ist der Test unabhängig von anderen Tests ausführbar?
- Gibt es geteilten veränderlichen Zustand?
- Wird `@SpringBootTest` wirklich benötigt?
- Sind Mocks sparsam und zielgerichtet eingesetzt?
- Prüfen `verify(...)`-Aufrufe Verhalten oder nur Implementierungsdetails?
- Sind Assertions aussagekräftig?
- Gibt es echte Secrets, echte Kundendaten oder Produktionswerte im Test?
- Sind Zeit und Zufall kontrollierbar?
- Werden relevante Fehlerfälle geprüft?
- Werden tenant- und berechtigungsrelevante Negativfälle geprüft?
- Ist der Test schnell genug für den vorgesehenen Testtyp?

## 23. Definition of Done

Ein Test erfüllt diese Richtlinie, wenn alle folgenden Punkte erfüllt sind:

1. Der Testname beschreibt Methode, erwartetes Verhalten und Bedingung.
2. Der Test prüft ein klar abgegrenztes Szenario.
3. Arrange, Act und Assert sind erkennbar getrennt.
4. Assertions sind präzise und liefern verständliche Fehldiagnosen.
5. Der Test enthält keine unnötige eigene Logik.
6. Der Test ist unabhängig von anderen Tests ausführbar.
7. Es gibt keinen geteilten veränderlichen Zustand zwischen Tests.
8. Der Test startet keinen Spring Context ohne fachlichen oder technischen Grund.
9. Mocks prüfen relevante Grenzen und keine zufälligen Implementierungsdetails.
10. Zeit, Zufall und externe Abhängigkeiten sind kontrolliert.
11. Testdaten enthalten keine echten Secrets oder produktiven personenbezogenen Daten.
12. Sicherheits- und Mandantenfälle werden dort geprüft, wo die getestete Logik dafür verantwortlich ist.

## 24. Quellen und weiterführende Literatur

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| JUnit Jupiter User Guide | Offizielle Dokumentation zu Annotationen, Lifecycle, Nested Tests, Extension Model und Teststruktur | `@Test`, `@BeforeEach`, `@Nested`, `@DisplayName`, `@ExtendWith` | https://docs.junit.org/5.10.5/user-guide/ |
| JUnit 6 Überblick | Aktueller JUnit-Stand zeigt die Weiterentwicklung der Plattform; viele Jupiter-Grundregeln bleiben übertragbar | Java-17-Runtime-Anforderung in JUnit 6 | https://docs.junit.org/6.0.3/overview.html |
| Spring Boot Testing Reference | Offizielle Spring-Boot-Testdokumentation | `spring-boot-starter-test`, `@SpringBootTest`, Test Slices | https://docs.spring.io/spring-boot/reference/testing/index.html |
| Spring Boot Applications Testing | Details zu `@SpringBootTest`, Web-Umgebungen und Testkontext | `MOCK`, `RANDOM_PORT`, Rollback-Grenzen bei Webserver-Tests | https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html |
| AssertJ | Fluent Assertions und lesbarere Assertion-Ausgaben | `assertThat(actual).isEqualTo(expected)` | https://assertj.github.io/doc/ |
| Mockito Jupiter | Integration von Mockito in JUnit Jupiter | `@ExtendWith(MockitoExtension.class)` | https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/org/mockito/junit/jupiter/MockitoExtension.html |
| OWASP Logging Cheat Sheet | Relevanz von sensiblen Informationen in Logs und Testausgaben | Keine Secrets in CI-Logs | https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html |
