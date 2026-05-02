# QG-JAVA-011 — Mocking mit Mockito richtig einsetzen und Fallen vermeiden

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-011 |
| Titel | Mocking mit Mockito richtig einsetzen und Fallen vermeiden |
| Status | Accepted / verbindlicher Standard für neue und wesentlich überarbeitete Java-Tests |
| Sprache | Deutsch |
| Java-Baseline | Java 21 |
| Test-Baseline | JUnit Jupiter 5.10+; Mockito 5.x; AssertJ; Spring Boot Test 3.x |
| Spring-Hinweis | Für reine Unit-Tests wird Mockito ohne Spring-Kontext verwendet. Für Spring-Kontexttests ist ab Spring Framework 6.2 / Spring Boot 3.4+ `@MockitoBean` zu bevorzugen; `@MockBean` ist in Spring Boot 3.4 deprecated und zur Entfernung in Spring Boot 4.0 vorgesehen. |
| Kategorie | Testing / Mocking / Clean Code |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, Security Reviewer, Architektur |
| Verbindlichkeit | Mockito darf verwendet werden, wenn echte Systemgrenzen, Nebeneffekte oder nicht-deterministische Abhängigkeiten isoliert werden müssen. Mockito darf nicht verwendet werden, um triviale Domänenobjekte, Value Objects, Records, Collections oder die zu testende Logik selbst künstlich zu ersetzen. |
| Qualitätsziel | Tests sollen beobachtbares Verhalten prüfen, nicht die interne Mockito-Konfiguration spiegeln. |
| Prüfstatus | Inhalt fachlich gegen JUnit-Jupiter-, Mockito- und Spring-Test-Dokumentation validiert; Codebeispiele mit externen Testbibliotheken benötigen die jeweiligen Projektabhängigkeiten. |
| Letzte fachliche Prüfung | 2026-05-02 |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wann Mockito in Java-Tests eingesetzt werden soll, wann Mocking schadet und wie Mockito-Tests so geschrieben werden, dass sie Verhalten prüfen statt Implementierungsdetails nachzubauen.

Mocking ist kein Selbstzweck. Ein Mock ist ein Test-Double für eine Abhängigkeit, deren echtes Verhalten im jeweiligen Test nicht relevant, nicht deterministisch, teuer, langsam, gefährlich oder außerhalb der Prozessgrenze liegt. Falsch eingesetztes Mocking erzeugt dagegen Tests, die grün bleiben, obwohl das System falsch arbeitet, oder rot werden, obwohl nur intern refaktoriert wurde.

Das Ziel ist nicht „mehr Mockito“. Das Ziel ist **verlässliche, schnelle, verständliche und refactoring-stabile Tests**.

---

## 2. Kurzregel für Entwickler

Mocke nur echte Grenzen: Datenbankzugriff, externe HTTP-Clients, E-Mail-, Payment-, Messaging-, Storage-, Clock- und andere Infrastrukturabhängigkeiten. Verwende echte Objekte für Domänenlogik, Value Objects, Records, Mapper ohne Infrastruktur, Collections und das System under Test.

Prüfe primär das Ergebnis und das beobachtbare Verhalten. Verwende `verify()` nur für echte Nebeneffekte, bei denen kein sinnvoller Rückgabewert geprüft werden kann.

Wenn ein Test viele `when(...)`-Konfigurationen, viele `verify(...)`-Aufrufe oder tiefe Mock-Ketten benötigt, ist das ein Designsignal: Das System under Test hat wahrscheinlich zu viele Abhängigkeiten, zu viele Verantwortlichkeiten oder eine ungünstige Schnittstelle.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Unit-Tests mit JUnit Jupiter und Mockito,
- Spring-Service-Tests ohne Spring ApplicationContext,
- Spring MVC Slice Tests mit gemockten Service-Abhängigkeiten,
- Tests von Use-Case-Services, Mappern, Validatoren und Domänenlogik,
- Tests, die externe Systeme, Datenbanken, Message Broker oder Zeitabhängigkeiten isolieren müssen.

Diese Richtlinie gilt nicht als vollständige Anleitung für:

- Contract Tests,
- End-to-End-Tests,
- Performance-Tests,
- Testcontainers-Integrationstests,
- Consumer-Driven Contract Testing,
- Security-Penetrationstests.

Diese Testarten dürfen Mockito punktuell verwenden, haben aber eigene Qualitätsregeln.

---

## 4. Technischer Hintergrund

Mockito erzeugt Test-Doubles, deren Verhalten im Test gezielt konfiguriert und überprüft werden kann. In JUnit Jupiter wird dafür typischerweise `MockitoExtension` verwendet. Diese Extension initialisiert Mockito-Mocks und behandelt striktes Stubbing.

Ein Mockito-Test besteht häufig aus drei Elementen:

1. `@Mock` für ersetzte Abhängigkeiten,
2. ein echtes System under Test,
3. Stubbing mit `when(...).thenReturn(...)` oder `thenAnswer(...)`, wenn die Abhängigkeit im Szenario ein bestimmtes Verhalten zeigen muss.

Mockito ist stark, weil es Isolation ermöglicht. Mockito ist gefährlich, wenn es dazu führt, dass Tests nicht mehr das Verhalten des Systems prüfen, sondern nur noch bestätigen, dass die Implementierung intern genau die erwarteten Methoden in genau der erwarteten Reihenfolge aufruft.

---

## 5. Verbindlicher Standard

### 5.1 Standard für reine Unit-Tests

Für reine Unit-Tests wird kein Spring ApplicationContext geladen. Es gilt:

```java
@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    UserMapper userMapper = new UserMapper();

    UserRegistrationService userService;

    @BeforeEach
    void setUp() {
        userService = new UserRegistrationService(
            userRepository,
            passwordEncoder,
            eventPublisher,
            userMapper
        );
    }
}
```

`@InjectMocks` darf verwendet werden, wenn die Konstruktion einfach und eindeutig ist. Bei komplexeren Services ist explizite Konstruktion im Test vorzuziehen, weil sie die Abhängigkeiten sichtbar macht.

### 5.2 Standard für Spring-Kontexttests

Für Tests mit Spring ApplicationContext gilt:

- In Spring Boot 3.2/3.3 ist `@MockBean` verbreitet und technisch weiterhin nutzbar.
- Ab Spring Boot 3.4 ist `@MockBean` deprecated und für Entfernung in Spring Boot 4.0 vorgesehen.
- Für aktuelle Codebasen mit Spring Framework 6.2+ soll `@MockitoBean` verwendet werden, wenn ein Bean im Testkontext durch einen Mockito-Mock ersetzt werden muss.

Beispiel:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserRegistrationService userRegistrationService;

    @Test
    void register_returnsCreated_whenCommandIsValid() throws Exception {
        when(userRegistrationService.register(any()))
            .thenReturn(new UserCreatedResponse(1L, "Max", "max@example.com"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Max",
                      "email": "max@example.com",
                      "password": "geheim123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.email").value("max@example.com"));
    }
}
```

### 5.3 Standard für Verhalten statt Implementierung

Ein Test MUSS primär prüfen, was sichtbar herauskommt oder welcher fachliche Nebeneffekt entsteht. Er SOLLTE nicht prüfen, welche internen Zwischenschritte zufällig aufgerufen wurden.

---

## 6. Was wird gemockt — und was nicht?

| Abhängigkeit | Vorgehen | Details/Erklärung | Beispiel |
|---|---|---|---|
| Repository / Datenbankzugriff | Mocken im Unit-Test | Der Unit-Test soll keine echte Datenbank benötigen. | `@Mock UserRepository` |
| Externer HTTP-Service | Mocken oder Fake verwenden | Netzwerk ist langsam, nicht deterministisch und nicht vollständig kontrollierbar. | `@Mock PaymentClient` |
| E-Mail-/Notification-Service | Mocken | Der Test darf keine echten Nachrichten versenden. | `verify(emailService).send(...)` |
| Message Publisher | Mocken oder Test-Broker nutzen | Unit-Test prüft Publikation, Integrationstest prüft Broker-Verhalten. | `@Mock EventPublisher` |
| `Clock` | Fake oder feste `Clock` verwenden | Zeit muss deterministisch sein. | `Clock.fixed(...)` |
| UUID-/ID-Generator | Fake verwenden | Zufall muss kontrollierbar sein. | `new FixedIdGenerator(...)` |
| Domänenlogik | Nicht mocken | Genau dieses Verhalten soll getestet werden. | `Order.cancel()` echt ausführen |
| Value Objects / Records | Nicht mocken | Einfach instanzieren; Mocking verschlechtert Lesbarkeit. | `new Email("a@b.de")` |
| Mapper ohne Infrastruktur | Eher nicht mocken | Mapping ist Teil des beobachtbaren Ergebnisses. | `new UserMapper()` |
| Collections | Nicht mocken | Echte Collections sind trivial und robuster. | `List.of(...)` |
| JDK-Werte wie `String`, `BigDecimal`, `Instant` | Nicht mocken | Werte sind deterministisch und billig. | `Instant.parse(...)` |
| Framework-Objekte wie `HttpServletRequest` | Meist nicht direkt mocken | Besser Spring-Testinfrastruktur verwenden. | `MockMvc` statt `mock(HttpServletRequest.class)` |
| Statische Methoden | Letztes Mittel | Häufig Designsignal; besser Abhängigkeit extrahieren. | `IdGenerator` statt `UUID.randomUUID()` direkt |
| Final Classes | Nur bewusst | Mockito 5 kann vieles, aber Design bleibt wichtiger als Machbarkeit. | Nur bei echter Grenze |

---

## 7. Gute Anwendung

### 7.1 Nur echte Grenzen mocken

```java
@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    UserMapper userMapper = new UserMapper();

    UserRegistrationService userService;

    @BeforeEach
    void setUp() {
        userService = new UserRegistrationService(
            userRepository,
            passwordEncoder,
            eventPublisher,
            userMapper
        );
    }

    @Test
    void register_returnsCreatedUser_whenEmailIsAvailable() {
        // Arrange
        var command = new RegisterUserCommand("Max", "max@example.com", "geheim123");

        when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, UserEntity.class);
            return user.withId(1L);
        });

        // Act
        var result = userService.register(command);

        // Assert
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Max");
        assertThat(result.email()).isEqualTo("max@example.com");
    }
}
```

Dieser Test mockt Repository, Encoder und Event Publisher, verwendet aber einen echten Mapper. Dadurch prüft der Test nicht nur, ob gemockte Werte durchgereicht werden, sondern ob das sichtbare Ergebnis fachlich stimmt.

### 7.2 `verify()` für echte Nebeneffekte

```java
@Test
void register_publishesUserRegisteredEvent_onSuccess() {
    // Arrange
    var command = new RegisterUserCommand("Max", "max@example.com", "geheim123");

    when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
    when(passwordEncoder.encode("geheim123")).thenReturn("encoded-password");
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation ->
        invocation.getArgument(0, UserEntity.class).withId(1L)
    );

    // Act
    userService.register(command);

    // Assert
    verify(eventPublisher).publishEvent(argThat(event ->
        event instanceof UserRegisteredEvent userRegistered
            && userRegistered.userId().equals(1L)
            && userRegistered.email().equals("max@example.com")
    ));
}
```

`verify()` ist hier sinnvoll, weil das Publizieren eines Events ein beobachtbarer Nebeneffekt ist und keinen Rückgabewert liefert.

### 7.3 `ArgumentCaptor` für präzise Prüfung gespeicherter Objekte

```java
@Test
void createOrder_savesOrderWithCorrectTenantAndItems() {
    // Arrange
    var command = new CreateOrderCommand(
        new TenantId("tenant-a"),
        new UserId(1L),
        new ProductId("P1"),
        new Quantity(3)
    );

    when(userRepository.findById(new TenantId("tenant-a"), new UserId(1L)))
        .thenReturn(Optional.of(new UserEntity(new TenantId("tenant-a"), new UserId(1L))));
    when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    orderService.createOrder(command);

    // Assert
    var captor = ArgumentCaptor.forClass(OrderEntity.class);
    verify(orderRepository).save(captor.capture());

    var savedOrder = captor.getValue();
    assertThat(savedOrder.tenantId()).isEqualTo(new TenantId("tenant-a"));
    assertThat(savedOrder.userId()).isEqualTo(new UserId(1L));
    assertThat(savedOrder.items()).hasSize(1);
    assertThat(savedOrder.items().getFirst().productId()).isEqualTo(new ProductId("P1"));
    assertThat(savedOrder.items().getFirst().quantity()).isEqualTo(new Quantity(3));
}
```

`ArgumentCaptor` ist sinnvoll, wenn eine Methode keinen fachlichen Rückgabewert liefert, aber ein übergebenes Objekt präzise geprüft werden muss.

---

## 8. Falsche Anwendung und Anti-Patterns

### 8.1 Anti-Pattern: Die Implementierung wird im Test gespiegelt

```java
@Test
void register_savesUserAndReturnsDto_bad() {
    var command = new RegisterUserCommand("Max", "max@example.com", "geheim123");
    var savedUser = new UserEntity(1L, "Max", "max@example.com", "encoded");
    var expectedDto = new UserCreatedResponse(1L, "Max", "max@example.com");

    when(passwordEncoder.encode("geheim123")).thenReturn("encoded");
    when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
    when(userRepository.save(any())).thenReturn(savedUser);
    when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

    var result = userService.register(command);

    assertThat(result).isEqualTo(expectedDto);
    verify(passwordEncoder).encode("geheim123");
    verify(userRepository).existsByEmail("max@example.com");
    verify(userRepository).save(any());
    verify(userMapper).toDto(savedUser);
}
```

Problem: Der Test beweist fast nur, dass Mockito so konfiguriert wurde, wie die Implementierung gerade aussieht. Wenn der Mapper kaputt ist, merkt der Test es nicht. Wenn die Implementierung intern refaktoriert wird, kann der Test brechen, obwohl das Verhalten gleich bleibt.

Korrektur: Mapper echt verwenden oder separat testen. Interaktionen nur prüfen, wenn sie fachlich relevante Nebeneffekte sind.

### 8.2 Anti-Pattern: `verify()` als primäre Assertion

```java
@Test
void findById_callsRepository_bad() {
    when(userRepository.findById(42L))
        .thenReturn(Optional.of(new UserEntity(42L, "Max", "max@example.com")));

    userService.findById(42L);

    verify(userRepository).findById(42L);
}
```

Problem: Der Test prüft nicht, ob das Ergebnis korrekt ist. Er prüft nur einen internen Weg.

Besser:

```java
@Test
void findById_returnsUser_whenUserExists() {
    // Arrange
    when(userRepository.findById(42L))
        .thenReturn(Optional.of(new UserEntity(42L, "Max", "max@example.com")));

    // Act
    var result = userService.findById(42L);

    // Assert
    assertThat(result.id()).isEqualTo(42L);
    assertThat(result.name()).isEqualTo("Max");
    assertThat(result.email()).isEqualTo("max@example.com");
}
```

### 8.3 Anti-Pattern: `any()` als Flucht vor Präzision

```java
@Test
void createOrder_savesSomething_bad() {
    when(orderRepository.save(any())).thenReturn(any());

    orderService.createOrder(new CreateOrderCommand(1L, "P1", 3));

    verify(orderRepository).save(any());
}
```

Problem: `any()` beschreibt keine Erwartung. Der Test sagt nur: „Irgendetwas wurde irgendwie gespeichert.“ Zusätzlich ist `any()` als Rückgabewert fachlich falsch; Matcher gehören in Stubbing- oder Verification-Aufrufe, nicht als echte Rückgabewerte.

Besser: konkrete Werte, typisierte Matcher oder `ArgumentCaptor` verwenden.

```java
verify(orderRepository).save(argThat(order ->
    order.userId().equals(new UserId(1L))
        && order.items().size() == 1
        && order.items().getFirst().productId().equals(new ProductId("P1"))
));
```

### 8.4 Anti-Pattern: Framework-Objekte direkt mocken

```java
@Test
void processRequest_extractsHeader_bad() {
    var request = mock(HttpServletRequest.class);
    when(request.getHeader("X-User-Id")).thenReturn("42");
    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn("application/json");

    var result = requestProcessor.process(request);

    assertThat(result.userId()).isEqualTo(42L);
}
```

Problem: Komplexe Framework-Typen haben viele Methoden, versteckte Verträge und Lifecycle-Regeln. Ein handgebauter Mock bildet das Framework oft falsch ab.

Besser: Für Spring MVC Controller `MockMvc` verwenden. Für Servlet-nahe Tests können Spring-Test-Objekte wie `MockHttpServletRequest` sinnvoller sein als Mockito-Mocks.

### 8.5 Anti-Pattern: Globales Stubbing in `@BeforeEach`

```java
@BeforeEach
void setUp() {
    when(userRepository.findById(any())).thenReturn(Optional.of(defaultUser));
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(passwordEncoder.encode(any())).thenReturn("encoded");
    when(clock.instant()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
}
```

Problem: Jeder Test enthält verborgenes Setup. Tests überschreiben implizite Stubs, unbenutzte Stubs werden gemeldet, und die Lesbarkeit sinkt.

Standard: `@BeforeEach` darf das System under Test konstruieren. Mock-Verhalten wird lokal in dem Test konfiguriert, der es benötigt.

### 8.6 Anti-Pattern: Deep Stubs

```java
@Mock(answer = Answers.RETURNS_DEEP_STUBS)
OrderClient orderClient;

@Test
void loadsTotal_bad() {
    when(orderClient.getOrder("1").getCustomer().getAccount().getBalance())
        .thenReturn(BigDecimal.TEN);

    var total = service.loadBalance("1");

    assertThat(total).isEqualTo(BigDecimal.TEN);
}
```

Problem: Deep Stubs stabilisieren eine schlechte Schnittstelle. Die Testkonfiguration verrät eine zu tiefe Kopplung an Objektketten.

Besser: Schnittstelle flacher machen oder einen Fake für den externen Client verwenden.

### 8.7 Anti-Pattern: Spies für schlechtes Design

```java
@Test
void calculate_usesRealMethodButMocksPart_bad() {
    var service = spy(new PriceCalculationService());
    doReturn(BigDecimal.TEN).when(service).loadBasePrice(any());

    var result = service.calculate("P1");

    assertThat(result).isPositive();
}
```

Problem: Ein Spy ruft echte Methoden auf, außer sie werden überschrieben. Das kann Tests überraschend machen. Häufig zeigt ein Spy, dass eine externe Abhängigkeit nicht sauber extrahiert wurde.

Besser: `PriceRepository` oder `PriceProvider` als Abhängigkeit injizieren und mocken.

### 8.8 Anti-Pattern: Statische Methoden mocken, statt Design zu verbessern

Statisches Mocking ist möglich, aber soll in neuem Produktivcode die Ausnahme bleiben. Wenn Zeit, UUIDs, Zufall, Umgebungsvariablen oder externe Infrastruktur statisch aufgerufen werden, ist meistens ein injizierbares Interface besser.

Schlecht:

```java
var id = UUID.randomUUID();
var now = Instant.now();
```

Besser:

```java
public final class OrderService {

    private final Clock clock;
    private final IdGenerator idGenerator;

    public OrderService(Clock clock, IdGenerator idGenerator) {
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    public Order createOrder(CreateOrderCommand command) {
        return new Order(
            idGenerator.nextId(),
            Instant.now(clock),
            command.userId()
        );
    }
}
```

---

## 9. Mockito-Regeln im Detail

### 9.1 `@Mock` statt `@MockitoBean` für Unit-Tests

`@Mock` gehört zu Mockito und braucht keinen Spring-Kontext. Es ist der Standard für reine Unit-Tests.

```java
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    PriceRepository priceRepository;

    PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService(priceRepository);
    }
}
```

`@MockitoBean` gehört in Spring-Kontexttests, wenn ein Bean im ApplicationContext ersetzt werden muss.

### 9.2 `@InjectMocks` bewusst verwenden

`@InjectMocks` kann Testcode verkürzen, verbirgt aber Konstruktion und Abhängigkeiten. Es ist akzeptabel, wenn die Klasse einen klaren Konstruktor und wenige Abhängigkeiten hat.

Bevorzugt bei kritischen Services:

```java
@BeforeEach
void setUp() {
    userService = new UserService(
        userRepository,
        passwordEncoder,
        eventPublisher,
        userMapper
    );
}
```

### 9.3 `when(...).thenReturn(...)` nur lokal und szenariospezifisch

Jeder Test soll nur das konfigurieren, was für sein Szenario nötig ist.

Gut:

```java
@Test
void findById_throwsException_whenUserDoesNotExist() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class);
}
```

### 9.4 `thenAnswer(...)` verwenden, wenn eine Abhängigkeit eingehende Argumente verarbeitet

```java
when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
    var user = invocation.getArgument(0, UserEntity.class);
    return user.withId(1L);
});
```

Das ist besser als ein komplett vorgefertigtes Objekt, wenn der Test prüfen soll, ob Eingaben korrekt in das gespeicherte Objekt übernommen werden.

### 9.5 `verifyNoMoreInteractions(...)` sparsam einsetzen

`verifyNoMoreInteractions(...)` ist stark, aber spröde. Es ist sinnvoll, wenn keine weiteren Nebeneffekte auftreten dürfen, zum Beispiel bei Security- oder Payment-Flows.

```java
@Test
void charge_doesNotCallPaymentProvider_whenUserIsNotAuthorized() {
    when(permissionService.canCharge(userId)).thenReturn(false);

    assertThatThrownBy(() -> paymentService.charge(userId, amount))
        .isInstanceOf(AccessDeniedException.class);

    verifyNoInteractions(paymentProvider);
}
```

In normalen Tests sollte `verifyNoMoreInteractions(...)` nicht reflexartig verwendet werden, weil es harmlose interne Refactorings unnötig brechen kann.

---

## 10. Security- und SaaS-Relevanz

Mockito-Tests können Security stärken oder verschleiern. Falsch gemockte Tests erzeugen besonders in SaaS-Systemen gefährliche blinde Flecken.

### 10.1 Tenant-Isolation darf nicht wegmockt werden

Schlecht:

```java
when(orderRepository.findById(any())).thenReturn(Optional.of(orderFromAnotherTenant));
```

Problem: Der Test prüft nicht, ob der Tenant-Kontext korrekt verwendet wird.

Besser:

```java
when(orderRepository.findByTenantIdAndId(new TenantId("tenant-a"), new OrderId(42L)))
    .thenReturn(Optional.of(order));

var result = orderService.findById(new TenantId("tenant-a"), new OrderId(42L));

assertThat(result.orderId()).isEqualTo(new OrderId(42L));
verify(orderRepository).findByTenantIdAndId(new TenantId("tenant-a"), new OrderId(42L));
```

Für SaaS-Systeme gilt: Repository- und Service-Tests müssen Tenant-Parameter explizit sichtbar machen. `any()` ist bei Tenant-, User-, Permission- und Ownership-Parametern nur in begründeten Ausnahmefällen erlaubt.

### 10.2 Negative Security-Pfade testen

```java
@Test
void export_doesNotReadData_whenPermissionIsMissing() {
    when(permissionService.canExport(userId, tenantId)).thenReturn(false);

    assertThatThrownBy(() -> exportService.exportOrders(userId, tenantId))
        .isInstanceOf(AccessDeniedException.class);

    verifyNoInteractions(orderRepository);
    verifyNoInteractions(storageClient);
}
```

Dieser Test prüft nicht nur, dass eine Exception geworfen wird, sondern auch, dass danach keine Daten gelesen und keine Dateien erzeugt werden.

### 10.3 Keine echten Secrets, Tokens oder produktiven Endpunkte in Tests

Mocks dürfen niemals echte API Keys, Passwörter, Tokens oder produktive URLs enthalten. Testdaten müssen eindeutig künstlich sein.

Schlecht:

```java
when(paymentClient.charge(any())).thenReturn(new PaymentResult("live_token_..."));
```

Besser:

```java
when(paymentClient.charge(any())).thenReturn(new PaymentResult("test-token-not-real"));
```

### 10.4 Mocks sind kein Ersatz für Integrationstests

Ein Mockito-Test kann beweisen, dass dein Code eine Client-Methode mit bestimmten Argumenten aufruft. Er beweist nicht, dass:

- der HTTP-Vertrag korrekt ist,
- JSON-Serialisierung passt,
- Authentifizierung gegen das externe System funktioniert,
- Retry- und Timeout-Verhalten in echter Infrastruktur funktionieren,
- Datenbank-Constraints korrekt greifen,
- Transaktionen wirklich committen oder rollbacken.

Für diese Punkte braucht es Integrationstests, Contract Tests oder Testcontainers.

---

## 11. Spring-Test-Kontext

### 11.1 Reiner Unit-Test

Verwende Mockito direkt:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    UserService userService;
}
```

Kein `@SpringBootTest`, kein ApplicationContext, kein `@MockitoBean`.

### 11.2 MVC-Slice-Test

Verwende Spring-Testinfrastruktur und ersetze nur Service-Grenzen:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserRegistrationService userRegistrationService;
}
```

### 11.3 Voller Spring-Kontext

`@SpringBootTest` nur verwenden, wenn der Test wirklich Spring-Konfiguration, Bean-Wiring, Profile, Properties oder Integration über mehrere Beans prüfen soll.

Wenn in einem `@SpringBootTest` viele Beans gemockt werden müssen, ist das ein Warnsignal. Entweder ist der Test zu breit, oder das System ist zu stark gekoppelt.

---

## 12. Review-Checkliste

| Aspekt | Prüffrage | Erwartung |
|---|---|---|
| Testziel | Prüft der Test beobachtbares Verhalten? | Ergebnis oder relevanter Nebeneffekt ist sichtbar geprüft. |
| Mock-Anzahl | Gibt es mehr als drei Mocks? | Design kritisch prüfen. |
| Stubbing | Gibt es mehr als drei `when(...)`-Aufrufe? | Setup reduzieren oder SUT-Zuschnitt prüfen. |
| Mapper | Wird ein einfacher Mapper gemockt? | Echten Mapper verwenden oder separat testen. |
| Value Objects | Werden Records, Value Objects oder Collections gemockt? | Echte Instanzen verwenden. |
| `verify()` | Ist `verify()` die einzige Assertion? | Ergebnisassertion ergänzen oder Testzweck ändern. |
| `any()` | Wird `any()` für fachlich wichtige Werte genutzt? | Konkrete Werte oder Captor verwenden. |
| Tenant | Werden Tenant-, User-, Ownership- oder Permission-Parameter präzise geprüft? | Keine unkontrollierten `any()`-Matcher. |
| Security | Gibt es negative Tests für fehlende Berechtigungen? | Keine nachgelagerten Repository-/Client-Aufrufe. |
| Spring | Wird für einen Unit-Test ein Spring-Kontext geladen? | In der Regel entfernen. |
| `@BeforeEach` | Enthält `@BeforeEach` globales Stubbing? | Stubbing lokal in Tests verschieben. |
| Strictness | Werden unnötige Stubs unterdrückt? | Ursache beheben; `lenient()` nur begründet. |
| Spies | Wird ein Spy verwendet? | Design kritisch prüfen. |
| Static Mocking | Werden statische Methoden gemockt? | Abhängigkeit extrahieren, wenn möglich. |
| Testdaten | Enthalten Tests echte Secrets oder produktive URLs? | Sofort entfernen. |

---

## 13. Automatisierbare Prüfungen

### 13.1 ArchUnit-Regeln

Beispiele für mögliche Projektregeln:

```java
@AnalyzeClasses(packages = "com.example")
class TestArchitectureRules {

    @ArchTest
    static final ArchRule keine_springboot_tests_fuer_reine_domain_tests =
        noClasses()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Test")
            .should().beAnnotatedWith(SpringBootTest.class)
            .because("Domain-Tests sollen ohne Spring ApplicationContext laufen.");

    @ArchTest
    static final ArchRule keine_mockito_beans_in_unit_tests =
        noFields()
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("UnitTest")
            .should().beAnnotatedWith(MockitoBean.class)
            .because("@MockitoBean ist für Spring-Kontexttests, nicht für reine Unit-Tests.");
}
```

### 13.2 Build- und Review-Regeln

Mögliche automatisierbare Prüfungen:

- Tests mit `@SpringBootTest` müssen begründet werden.
- `@MockBean` wird in Spring Boot 3.4+ als veraltet markiert und soll nicht neu eingeführt werden.
- Statisches Mocking benötigt Review-Freigabe.
- `lenient()` benötigt Kommentar mit Begründung.
- Testklassen mit mehr als einer bestimmten Anzahl Mocks werden im Review markiert.
- Produktive URLs oder offensichtliche Secrets in Tests werden per Secret-Scanning blockiert.

---

## 14. Migration bestehender Mockito-Tests

Bestehende Tests werden nicht mechanisch umgeschrieben. Migration erfolgt risikobasiert.

### Schritt 1: Testzweck erkennen

Vor jeder Änderung klären:

- Prüft der Test Verhalten?
- Prüft er nur interne Aufrufe?
- Ist der Test flaky?
- Bricht er häufig bei Refactorings?
- Laden Unit-Tests unnötig Spring-Kontext?

### Schritt 2: Überflüssige Mocks entfernen

Records, Value Objects, Mapper ohne Infrastruktur, Domänenobjekte und Collections werden durch echte Instanzen ersetzt.

### Schritt 3: Ergebnisassertionen ergänzen

`verify()`-Only-Tests werden auf Ergebnisprüfung umgestellt.

### Schritt 4: `@BeforeEach`-Stubbing reduzieren

Globales Stubbing wird lokal in die Tests verschoben.

### Schritt 5: Spring-Testannotation prüfen

- Reiner Unit-Test: `@ExtendWith(MockitoExtension.class)`.
- MVC-Test: `@WebMvcTest` plus `@MockitoBean` für Service-Grenzen.
- Integrationstest: `@SpringBootTest` nur mit klarer Begründung.

### Schritt 6: Security- und Tenant-Pfade ergänzen

Bei SaaS-Code müssen negative Berechtigungs-, Tenant- und Ownership-Fälle ergänzt werden.

---

## 15. Ausnahmen

Abweichungen sind zulässig, wenn sie fachlich begründet sind.

Zulässige Ausnahmen:

- `lenient()` bei gemeinsam genutzten Test-Fixtures, wenn lokale Stubs unverhältnismäßig viel Duplikation erzeugen.
- Static Mocking für Legacy-Code, wenn eine sofortige Designänderung zu riskant ist.
- Spy für schwer migrierbare Legacy-Komponenten, wenn ein klarer Refactoring-Pfad dokumentiert ist.
- `@SpringBootTest` mit Mocks, wenn bewusst Spring-Konfiguration plus einzelne externe Grenzen getestet werden.

Nicht zulässige Ausnahmen:

- Mockito verwenden, um Domänenlogik zu ersetzen.
- `any()` für Tenant-, Permission-, Ownership- oder Security-Parameter ohne Begründung.
- Echte Secrets in Tests.
- Leere oder bedeutungslose Assertions.
- Tests, die nur Mockito-Konfiguration bestätigen.

---

## 16. Definition of Done

Ein Mockito-Test erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Der Test prüft ein klar benanntes Verhalten oder einen klaren Nebeneffekt.
2. Das System under Test ist echt und wird nicht gemockt.
3. Nur echte Grenzen oder nicht-deterministische Abhängigkeiten werden gemockt.
4. Records, Value Objects, Collections und einfache Mapper werden nicht gemockt.
5. Stubbing ist lokal und szenariospezifisch.
6. Das Ergebnis wird mit AssertJ/JUnit-Assertions geprüft.
7. `verify()` wird nur für fachlich relevante Nebeneffekte verwendet.
8. `any()` wird nicht für fachlich kritische Werte verwendet.
9. Tenant-, User-, Permission- und Ownership-Parameter werden präzise geprüft.
10. Es werden keine echten Secrets, Tokens oder produktiven Endpunkte verwendet.
11. Reine Unit-Tests laden keinen Spring ApplicationContext.
12. `@MockitoBean` oder `@MockBean` wird nur in Spring-Kontexttests verwendet.
13. `lenient()`, Spies und statisches Mocking sind begründet.
14. Der Test ist isoliert, deterministisch und unabhängig von Ausführungsreihenfolge.

---

## 17. Entscheidungsbaum

```text
Soll eine Abhängigkeit im Test ersetzt werden?
|
|-- Ist es das System under Test?
|   |-- Ja  -> Nicht mocken.
|   |-- Nein -> weiter.
|
|-- Ist es ein Value Object, Record, DTO, Collection oder einfacher Mapper?
|   |-- Ja  -> Echte Instanz verwenden.
|   |-- Nein -> weiter.
|
|-- Ist es eine externe Grenze, Datenbank, Netzwerk, Mail, Payment, Storage, Clock oder ID-Generator?
|   |-- Ja  -> Mock, Fake oder Test-Double verwenden.
|   |-- Nein -> weiter.
|
|-- Ist es komplexe eigene Domänenlogik?
|   |-- Ja  -> Echte Instanz verwenden und separat testen.
|   |-- Nein -> weiter.
|
|-- Braucht der Test Spring-Konfiguration?
|   |-- Ja  -> Spring-Test mit @MockitoBean für ersetzte Beans.
|   |-- Nein -> Reiner Mockito-Test mit @ExtendWith(MockitoExtension.class).
```

---

## 18. Quellen und weiterführende Literatur

- Mockito JUnit Jupiter Extension — offizielle Javadoc-Dokumentation.
- Mockito Core API — offizielle Mockito-Dokumentation.
- Mockito ArgumentCaptor — offizielle Javadoc-Dokumentation.
- Mockito ArgumentMatchers — offizielle Javadoc-Dokumentation.
- JUnit 5 User Guide — JUnit Jupiter Programmier- und Extension-Modell.
- Spring Framework Reference — `@MockitoBean` und `@MockitoSpyBean`.
- Spring Boot API — Deprecation von `@MockBean` seit Spring Boot 3.4 zugunsten von `@MockitoBean`.
- OWASP Logging Cheat Sheet — sensible Informationen in Logs und Testdaten vermeiden.
- OWASP API Security 2023 — Ressourcen-, Berechtigungs- und Objektzugriffsrisiken in APIs.
