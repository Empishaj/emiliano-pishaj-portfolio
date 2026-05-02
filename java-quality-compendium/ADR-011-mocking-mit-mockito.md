# ADR-011 — Mocking mit Mockito: Richtig einsetzen, Fallen vermeiden

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · JUnit 5.10+ · Mockito 5.x              |
| Datum      | 2025-02-08                                  |
| Kategorie  | Testing / Mocking                           |

---

## Kontext & Problem

Mocking ist das mächtigste Werkzeug im Unit-Test-Arsenal — und das am häufigsten missbrauchte. Zu viel Mocking führt zu Tests, die nur die Mockito-Konfiguration prüfen, nicht das Verhalten. Zu wenig führt zu schwerfälligen Integrationstests für einfache Unit-Szenarien. Dieses ADR definiert wann, wie und wie nicht gemockt wird.

---

## Grundregel: Was wird gemockt — und was nicht?

| Abhängigkeit                  | Vorgehen            | Begründung                                         |
|-------------------------------|---------------------|----------------------------------------------------|
| Repository / Datenbankzugriff | ✅ Mocken            | Kein Test-DB-Aufwand für Unit-Tests                |
| Externer HTTP-Service         | ✅ Mocken            | Netzwerk ist nicht deterministisch                 |
| Email- / Notification-Service | ✅ Mocken            | Kein echter Versand im Test                        |
| Domänenlogik (eigene Klasse)  | ❌ Nicht mocken      | Echtes Objekt verwenden — das ist was getestet wird |
| Wert-Objekte / Records        | ❌ Nicht mocken      | Trivial zu instanzieren, kein Grund für Mock       |
| `java.time.Clock`             | ✅ Faken / Mocken    | Deterministisches Datum im Test                    |
| Statische Methoden            | ⚠ Letztes Mittel    | Zeigt oft Design-Problem (→ ADR-008)               |

---

## Fehler 1 — Zu viel Mocking: Die Implementierung wird gespiegelt

### Schlecht — Test prüft nur die Mockito-Konfiguration

```java
@Test
void register_savesUserAndReturnsDto() {
    // Arrange: Mocks für alles
    var command = new RegisterUserCommand("Max", "max@example.com", "geheim");
    var encodedPassword = "encoded_geheim";
    var savedUser = new UserEntity(1L, "Max", "max@example.com", encodedPassword);
    var expectedDto = new UserCreatedResponse(1L, "Max", "max@example.com");

    when(passwordEncoder.encode("geheim")).thenReturn(encodedPassword);
    when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
    when(userRepository.save(any())).thenReturn(savedUser);
    when(userMapper.toDto(savedUser)).thenReturn(expectedDto); // ← Mock spiegelt Implementierung!

    // Act
    var result = userService.register(command);

    // Assert: prüft nur ob gemockte Werte korrekt durchgereicht wurden
    assertThat(result.name()).isEqualTo("Max");
    verify(passwordEncoder).encode("geheim");
    verify(userRepository).existsByEmail("max@example.com");
    verify(userRepository).save(any());
    verify(userMapper).toDto(savedUser);
}
```

**Warum ist das schlecht?**
- `when(userMapper.toDto(savedUser)).thenReturn(expectedDto)`: Der Mapper wird gemockt und gibt das zurück, was wir erwarten — der Test kann nie scheitern, egal was der echte Mapper tut.
- Jede Refactoring-Schritt an der Implementierung bricht den Test — nicht weil das Verhalten falsch ist, sondern weil ein `verify()` fehlt.
- Der Test ist eine 1:1-Kopie der Implementierung. Er testet nichts.
- 5 `when()`-Aufrufe für einen einzigen Testfall: zu viel Setup ist ein Design-Signal.

### Gut — nur echte Grenzen mocken, Mapper echt verwenden

```java
@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock UserRepository   userRepository;
    @Mock PasswordEncoder  passwordEncoder;
    @Mock ApplicationEventPublisher eventPublisher;

    // Echter Mapper — kein Mock. Sein Verhalten ist Teil des Tests.
    UserMapper userMapper = new UserMapper();

    @InjectMocks UserRegistrationService userService;

    @Test
    void register_returnsCorrectDto_onSuccess() {
        // Arrange — nur echte Grenzen werden konfiguriert
        var command = new RegisterUserCommand("Max", "max@example.com", "geheim");
        when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
        when(passwordEncoder.encode("geheim")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, UserEntity.class);
            return entity.withId(1L); // Simuliert generierte ID
        });

        // Act
        var result = userService.register(command);

        // Assert — prüft das echte Mapping-Ergebnis
        assertThat(result.name()).isEqualTo("Max");
        assertThat(result.email()).isEqualTo("max@example.com");
        assertThat(result.id()).isEqualTo(1L);
    }
}
```

---

## Fehler 2 — `verify()` als primäre Assertion

### Schlecht — Test prüft nur ob etwas aufgerufen wurde

```java
@Test
void findById_callsRepositoryWithCorrectId() {
    when(userRepository.findById(42L))
        .thenReturn(Optional.of(new UserEntity(42L, "Max", "max@example.com")));

    userService.findById(42L);

    // Nur verify — kein assertThat über das Ergebnis!
    verify(userRepository).findById(42L);
}
```

**Warum ist das schlecht?**
- `verify()` prüft Implementierungsdetails (wie wird etwas erreicht), nicht Verhalten (was wird erreicht).
- Der Test ist grün, auch wenn `findById()` das falsche Ergebnis zurückliefert.
- Wenn die Implementierung refactored wird und `findById()` intern den Cache nutzt, bricht der Test — obwohl das Verhalten identisch ist.

### Gut — Ergebnis prüfen, `verify()` nur für Nebeneffekte

```java
@Test
void findById_returnsCorrectUserDto_whenUserExists() {
    // Arrange
    when(userRepository.findById(42L))
        .thenReturn(Optional.of(new UserEntity(42L, "Max", "max@example.com")));

    // Act
    var result = userService.findById(42L);

    // Assert — das ERGEBNIS, nicht der Aufruf
    assertThat(result.id()).isEqualTo(42L);
    assertThat(result.name()).isEqualTo("Max");
    // verify() weglassen — der Test schlägt ohnehin fehl wenn das Repository nicht aufgerufen wird
}

// verify() ist richtig für Nebeneffekte ohne Rückgabewert:
@Test
void register_publishesUserRegisteredEvent_onSuccess() {
    var command = new RegisterUserCommand("Max", "max@example.com", "geheim");
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, UserEntity.class).withId(1L));

    userService.register(command);

    // verify() ist hier richtig: publishEvent hat keinen Rückgabewert der geprüft werden kann
    verify(eventPublisher).publishEvent(
        argThat(event -> event instanceof UserRegisteredEvent e
            && e.email().equals("max@example.com"))
    );
}
```

---

## Fehler 3 — `any()` als Flucht vor präzisen Assertions

### Schlecht — `any()` überall, kein echter Prüfwert

```java
@Test
void createOrder_savesOrder() {
    when(orderRepository.save(any())).thenReturn(any()); // any() als Rückgabewert?!
    when(userRepository.findById(any())).thenReturn(Optional.of(any())); // Sinnlos

    orderService.createOrder(new CreateOrderCommand(1L, "P1", 3));

    verify(orderRepository).save(any()); // Prüft: wurde irgendwas gespeichert. Mehr nicht.
}
```

**Warum ist das schlecht?**
- `any()` als Rückgabe von `thenReturn()` ist in Mockito 5 verboten und wirft eine Exception — aber der Impuls dahinter ist das Problem.
- `verify(repo).save(any())` prüft nur dass die Methode aufgerufen wurde — nicht ob das richtige Objekt übergeben wurde.
- `any()` wo ein konkreter Wert stehen sollte, bedeutet: der Entwickler weiß nicht was er testen soll.

### Gut — präzise Matcher, konkrete Werte

```java
@Test
void createOrder_savesOrderWithCorrectItems() {
    // Arrange
    var command = new CreateOrderCommand(new UserId(1L), new ProductId("P1"), new Quantity(3));
    var user = new UserEntity(1L, "Max", "max@example.com");
    when(userRepository.findById(new UserId(1L))).thenReturn(Optional.of(user));
    when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    orderService.createOrder(command);

    // Assert — präziser ArgumentCaptor statt any()
    var captor = ArgumentCaptor.forClass(OrderEntity.class);
    verify(orderRepository).save(captor.capture());

    var savedOrder = captor.getValue();
    assertThat(savedOrder.userId()).isEqualTo(new UserId(1L));
    assertThat(savedOrder.items()).hasSize(1);
    assertThat(savedOrder.items().get(0).productId()).isEqualTo(new ProductId("P1"));
    assertThat(savedOrder.items().get(0).quantity().value()).isEqualTo(3);
}
```

---

## Fehler 4 — Mocking von Typen die man nicht besitzt

### Schlecht — Mock von JDK- oder Framework-Typen

```java
@Test
void processRequest_extractsHeader() {
    // Mock von HttpServletRequest — komplexes Framework-Objekt
    var request = mock(HttpServletRequest.class);
    when(request.getHeader("X-User-Id")).thenReturn("42");
    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn("application/json");

    var result = requestProcessor.process(request);

    assertThat(result.userId()).isEqualTo(42L);
}
```

**Warum ist das schlecht?**
- `HttpServletRequest` hat dutzende Methoden — jede unmockierte Methode gibt `null` zurück und erzeugt subtile Fehler.
- Tests koppeln sich an Framework-Internas — Änderungen an `HttpServletRequest` brechen Tests.
- Spring MVC Test (`MockMvc`) existiert genau dafür — Framework-Objekte durch echte Test-Infrastruktur ersetzen.

### Gut — Framework-Objekte durch Test-Infrastruktur ersetzen

```java
// Für Controller-Tests: MockMvc statt Mock(HttpServletRequest)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  UserRegistrationService userService;

    @Test
    void register_returns201_onSuccess() throws Exception {
        when(userService.register(any())).thenReturn(
            new UserCreatedResponse(1L, "Max", "max@example.com", Instant.now()));

        mockMvc.perform(post("/api/users")
                .contentType(APPLICATION_JSON)
                .content("""
                    { "name": "Max", "email": "max@example.com", "password": "geheim123" }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.email").value("max@example.com"));
    }
}
```

---

## Fehler 5 — Mockito in `@BeforeEach` aufwändig konfigurieren

### Schlecht — `@BeforeEach` als globaler Mock-Konfigurations-Dump

```java
class UserServiceTest {

    @BeforeEach
    void setUp() {
        // Alle Mocks für alle Tests vorkonfiguriert — auch wenn ein Test sie nicht braucht
        when(userRepository.findById(any())).thenReturn(Optional.of(defaultUser));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(emailService.send(any(), any())).thenReturn(true);
        when(clock.instant()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void findById_throwsException_whenUserNotFound() {
        // Muss das globale Mock überschreiben — Reihenfolge ist wichtig!
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
            .isInstanceOf(UserNotFoundException.class);
    }
}
```

**Warum ist das schlecht?**
- Jeder Test trägt implizites Wissen über den `@BeforeEach`-Block — schwer verständlich ohne beides gleichzeitig zu lesen.
- Überschreiben von `@BeforeEach`-Mocks ist fehleranfällig und reihenfolgeabhängig.
- Tests, die nur einen Mock brauchen, schleppen fünf unbenutzte Konfigurationen mit.

### Gut — Mocks lokal im Test, `@BeforeEach` nur für das System-under-Test

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository  userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock Clock           clock;

    @InjectMocks UserRegistrationService userService;

    // @BeforeEach nur für SUT-Setup — KEINE Mock-Konfiguration
    @BeforeEach
    void setUp() {
        // Nur wenn wirklich alle Tests dasselbe Setup brauchen — sonst weglassen
    }

    @Test
    void findById_returnsUser_whenExists() {
        // Lokal: nur was dieser Test braucht
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(new UserEntity(1L, "Max", "max@example.com")));

        var result = userService.findById(1L);

        assertThat(result.name()).isEqualTo("Max");
    }

    @Test
    void findById_throwsException_whenNotFound() {
        // Lokal: eigene Konfiguration, kein Überschreiben
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
            .isInstanceOf(UserNotFoundException.class);
    }
}
```

---

## Konsequenzen

**Positiv:**
- Tests scheitern wenn das Verhalten falsch ist — nicht wenn die Implementierung umstrukturiert wird.
- `ArgumentCaptor` macht geprüfte Interaktionen explizit und präzise.
- Wenige Mocks pro Test = einfach verständlich ohne Kontext-Laden.

**Negativ:**
- Sorgfältige Entscheidung nötig: was mocken, was nicht. Erfordert Design-Verständnis.

---

## Tipps

- **Faustregel**: Mehr als 3 `when()`-Aufrufe in einem Test? Prüfe ob der Service zu viele Abhängigkeiten hat.
- **`@MockBean` nur im Spring-Context**: In reinen Unit-Tests `@Mock` von Mockito verwenden — kein Spring-Context nötig.
- **`lenient().when()`**: Für den seltenen Fall, dass ein Mock in manchen Tests nicht aufgerufen wird und Mockito sonst `UnnecessaryStubbingException` wirft.
- **Mockito Strict Stubs (Standard in 5.x)**: Unbenutzte Stubs werden als Fehler gemeldet — das ist gut, nicht lästig. Nutze es als Feedback.
 