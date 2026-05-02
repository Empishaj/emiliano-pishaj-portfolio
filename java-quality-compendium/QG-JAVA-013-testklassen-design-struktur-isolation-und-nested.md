# QG-JAVA-013 — Testklassen-Design: Struktur, Isolation und `@Nested`

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-013 |
| Titel | Testklassen-Design: Struktur, Isolation und `@Nested` |
| Status | Accepted / verbindlicher Standard für neue und wesentlich überarbeitete JUnit-5-Testklassen |
| Version | 1.0.0 |
| Datum der fachlichen Prüfung | 2026-05-03 |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, Test Engineers, Architektur |
| Technologiekontext | Java 21, JUnit Jupiter 5.10+, AssertJ, Mockito, Spring Boot Test 3.x |
| Kategorie | Testing / Testarchitektur / Testlesbarkeit |
| Verbindlichkeit | Neue Testklassen müssen diese Richtlinie erfüllen. Bestehende Testklassen werden bei fachlicher Änderung, Refactoring oder Flaky-Test-Bereinigung schrittweise angepasst. |
| Kurzfassung | Testklassen müssen Verhalten auffindbar, isoliert und lesbar dokumentieren. `@Nested` wird genutzt, wenn eine Testklasse mehrere Methoden, Zustände oder Szenariogruppen enthält. |
| Technische Validierung | Gegen JUnit-5-User-Guide, Spring-Boot-Test-Dokumentation und bestehende Java-21-Testpraxis geprüft. |
| Qualitätsziel | Tests sollen beim Lesen erklären, welches Verhalten gilt, und beim Scheitern sofort zeigen, welches Szenario gebrochen ist. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie JUnit-5-Testklassen in Java-Projekten strukturiert werden. Ziel ist nicht nur, dass Tests grün sind. Ziel ist, dass Tests als ausführbare Spezifikation wirken: Ein Entwickler muss in einer Testklasse schnell erkennen können, welche Methode, welcher Zustand oder welches Szenario geprüft wird.

Unstrukturierte Testklassen werden mit der Zeit schwer wartbar. Eine Klasse wie `UserServiceTest` beginnt oft mit fünf Testmethoden und endet nach einigen Monaten mit vierzig Methoden ohne erkennbare Ordnung. Fehlerfälle, Happy Paths, Validierung, Events, Security-Pfade und Randfälle liegen dann flach nebeneinander. Das führt zu schlechter Auffindbarkeit, doppeltem Setup, unklaren Testdaten und hohem Review-Aufwand.

JUnit 5 bietet mit `@Nested`, `@DisplayName`, Lifecycle-Methoden und Tags wirksame Mittel, um Testklassen lesbar und wartbar zu strukturieren. Diese Richtlinie legt verbindlich fest, wann und wie diese Mittel eingesetzt werden.

---

## 2. Kurzregel

Eine Testklasse muss nach beobachtbarem Verhalten strukturiert sein. Bei mehr als etwa acht bis zehn Testmethoden wird die Klasse mit `@Nested` nach Methode, Zustand oder Szenario gruppiert. Jeder Test bleibt isoliert, verwendet klar erkennbare Testdaten und prüft genau ein fachliches Szenario. Gemeinsamer veränderlicher Zustand, Testreihenfolgen und globale Mock-Konfigurationen sind zu vermeiden.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Unit Tests mit JUnit Jupiter.
- Service Tests.
- Domain Tests.
- Mapper Tests.
- Controller Slice Tests.
- Spring Boot Integrationstests.
- Tests für Value Objects, Records, Sealed Types und fachliche Zustandsmaschinen.
- Testklassen mit Mockito, AssertJ und Spring Boot Test.

Diese Richtlinie gilt nicht als vollständiger Ersatz für separate Richtlinien zu:

- Assertions mit AssertJ.
- Mockito und Test Doubles.
- Parametrisierten Tests.
- Integrationstests mit Testcontainers.
- Contract Testing.
- Performance- und Lasttests.

---

## 4. Technischer Hintergrund

JUnit Jupiter unterstützt verschachtelte Testklassen über `@Nested`. Eine `@Nested`-Klasse ist eine nicht-statische innere Testklasse, mit der Tests hierarchisch gruppiert werden können. Der JUnit-User-Guide beschreibt `@Nested` genau für solche hierarchischen Teststrukturen. Außerdem unterstützt JUnit Jupiter `@DisplayName`, Lifecycle-Methoden wie `@BeforeEach`, `@BeforeAll`, `@AfterEach`, `@AfterAll`, Tags und konfigurierbare Testinstanz-Lebenszyklen. Siehe JUnit User Guide: <https://docs.junit.org/5.10.2/user-guide/index.html>.

Standardmäßig verwendet JUnit Jupiter den Testinstanz-Lifecycle `PER_METHOD`. Das bedeutet: Für jede Testmethode wird eine neue Instanz der Testklasse erzeugt. Dieser Default ist gut, weil er Isolation fördert. `PER_CLASS` ist möglich, muss aber bewusst eingesetzt werden, weil dann derselbe Testklassenzustand über mehrere Tests hinweg bestehen kann. Der JUnit-User-Guide beschreibt `PER_METHOD` als Default und `PER_CLASS` als Alternative für besondere Fälle.

Ein wichtiger Java-21-Kontext: Seit Java 16 dürfen `@BeforeAll`- und `@AfterAll`-Methoden in `@Nested`-Klassen auch statisch deklariert werden. Nicht-statische `@BeforeAll`- und `@AfterAll`-Methoden benötigen weiterhin `@TestInstance(PER_CLASS)`. Diese Unterscheidung ist wichtig, weil ältere Beispiele im Internet oft pauschal sagen, dass `@BeforeAll` in `@Nested` nur mit `PER_CLASS` funktioniert. Für Java 21 ist das zu grob.

---

## 5. Verbindlicher Standard

Neue JUnit-5-Testklassen müssen die folgenden Grundregeln erfüllen:

1. Die Testklasse ist nach beobachtbarem Verhalten strukturiert.
2. Eine Testmethode prüft ein fachliches Szenario.
3. Testmethoden verwenden sprechende Namen im Muster `methodName_expectedBehavior_whenCondition`.
4. `@DisplayName` wird verwendet, wenn die IDE-/CI-Ausgabe dadurch lesbarer wird.
5. `@Nested` wird verwendet, wenn Tests nach Methode, Zustand oder Szenario gruppiert werden müssen.
6. Gemeinsamer veränderlicher Zustand zwischen Tests ist verboten.
7. `@TestMethodOrder` ist für normale Unit Tests verboten.
8. `@BeforeEach` bereitet Testdaten vor, enthält aber keine Assertions.
9. `@BeforeAll` ist nur für teure Ressourcen erlaubt, nicht für fachliches Standardszenario-Setup.
10. `@TestInstance(PER_CLASS)` ist eine begründete Ausnahme, kein Default.
11. Testdaten werden über Factory-Methoden, Builder oder Test Fixtures erzeugt, nicht durch duplizierte Inline-Blöcke.
12. Test-Fixtures liegen im Test-Source-Tree, nicht in `main`.
13. Tests dürfen keine echten personenbezogenen Produktionsdaten enthalten.
14. Sicherheits-, Mandanten- und Berechtigungsszenarien müssen explizit testbar und auffindbar sein.

---

## 6. Grundprinzip: Testklasse als ausführbare Spezifikation

Eine gute Testklasse beantwortet beim Lesen vier Fragen:

1. Welche fachliche Einheit wird getestet?
2. Welche Methode, welcher Zustand oder welches Szenario wird geprüft?
3. Welche Bedingung wird aufgebaut?
4. Welches Verhalten wird erwartet?

Eine schlechte Testklasse zwingt den Leser dazu, Methodennamen, Setup, Mocks und Assertions zusammenzusuchen. Das ist kein kleines Stilproblem. Es erhöht die Wahrscheinlichkeit, dass Tests falsch erweitert, falsch gelöscht oder bei Refactorings falsch verstanden werden.

---

## 7. `@Nested` nach Methoden strukturieren

### 7.1 Schlechte Anwendung: flache Testklasse ohne Ordnung

```java
class UserServiceTest {

    @Test void findById_returnsUser() {}
    @Test void findById_throwsException() {}
    @Test void register_savesUser() {}
    @Test void register_throwsOnDuplicate() {}
    @Test void register_sendsEmail() {}
    @Test void register_throwsOnInvalidName() {}
    @Test void register_throwsOnInvalidEmail() {}
    @Test void updateProfile_updatesName() {}
    @Test void updateProfile_throwsIfNotFound() {}
    @Test void changePassword_updatesPassword() {}
    @Test void changePassword_throwsWhenOldPasswordWrong() {}
}
```

Diese Struktur ist anfangs erträglich, wird aber schnell unübersichtlich. Tests für unterschiedliche Methoden, Fehlerfälle und Nebeneffekte stehen nebeneinander. Die Klasse hat keine erkennbare innere Ordnung.

### 7.2 Gute Anwendung: Gruppierung nach getesteter Methode

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("gibt UserDto zurück, wenn der User existiert")
        void returnsUser_whenUserExists() {
            // Arrange
            when(userRepository.findById(1L))
                    .thenReturn(Optional.of(new UserEntity(1L, "Max", "max@example.com")));

            // Act
            var result = userService.findById(1L);

            // Assert
            assertThat(result.name()).isEqualTo("Max");
            assertThat(result.email()).isEqualTo("max@example.com");
        }

        @Test
        @DisplayName("wirft UserNotFoundException, wenn die ID unbekannt ist")
        void throwsUserNotFoundException_whenIdIsUnknown() {
            // Arrange
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.findById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("legt einen neuen User an, wenn die E-Mail frei ist")
        void createsUser_whenEmailIsAvailable() {
            // Arrange
            var command = validRegistrationCommand();
            when(userRepository.existsByEmail(command.email())).thenReturn(false);
            when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0, UserEntity.class).withId(1L));

            // Act
            var result = userService.register(command);

            // Assert
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("max@example.com");
        }

        @Test
        @DisplayName("wirft EmailAlreadyExistsException, wenn die E-Mail bereits verwendet wird")
        void throwsEmailAlreadyExistsException_whenEmailIsTaken() {
            // Arrange
            var command = validRegistrationCommand();
            when(userRepository.existsByEmail(command.email())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> userService.register(command))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("max@example.com");
        }
    }

    private static RegisterUserCommand validRegistrationCommand() {
        return new RegisterUserCommand("Max Mustermann", "max@example.com", "secret123");
    }
}
```

Diese Struktur hat mehrere Vorteile: Tests sind auffindbar, die IDE-Ausgabe wird lesbar, Setup kann gruppenspezifisch gehalten werden und neue Tests werden an der richtigen Stelle ergänzt.

---

## 8. `@Nested` nach Zustand strukturieren

Bei Domänenobjekten mit klaren Zuständen ist die Gruppierung nach Zustand oft besser als die Gruppierung nach Methode.

Typische Beispiele:

- Bestellung: `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.
- Zahlung: `OPEN`, `AUTHORIZED`, `CAPTURED`, `FAILED`, `REFUNDED`.
- Konto: `ACTIVE`, `LOCKED`, `SUSPENDED`.
- Ticket: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.

### Gute Anwendung: Zustandsbasierte Teststruktur

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Nested
    @DisplayName("Bestellung im Status PENDING")
    class WhenPending {

        @Test
        @DisplayName("kann storniert werden")
        void canBeCancelled() {
            // Arrange
            var order = OrderTestBuilder.anOrder().pending().build();

            // Act
            order.cancel();

            // Assert
            assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("kann neue Positionen aufnehmen")
        void canAddItems() {
            // Arrange
            var order = OrderTestBuilder.anOrder().pending().build();

            // Act
            order.addItem(new ProductId("P1"), new Quantity(2));

            // Assert
            assertThat(order.items()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Bestellung im Status DELIVERED")
    class WhenDelivered {

        @Test
        @DisplayName("kann nicht storniert werden")
        void cannotBeCancelled() {
            // Arrange
            var order = OrderTestBuilder.anOrder().delivered().build();

            // Act & Assert
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(OrderCannotBeCancelledException.class)
                    .hasMessageContaining("DELIVERED");
        }

        @Test
        @DisplayName("kann keine neuen Positionen aufnehmen")
        void cannotAddItems() {
            // Arrange
            var order = OrderTestBuilder.anOrder().delivered().build();

            // Act & Assert
            assertThatThrownBy(() -> order.addItem(new ProductId("P2"), new Quantity(1)))
                    .isInstanceOf(OrderModificationException.class);
        }
    }
}
```

Diese Struktur dokumentiert die Zustandsmaschine des Objekts. Der Leser erkennt nicht nur einzelne Methoden, sondern das zulässige Verhalten pro Zustand.

---

## 9. Gruppierung nach Szenario

Nicht jede Testklasse lässt sich sinnvoll nach Methode oder Zustand gruppieren. Bei komplexeren Use Cases kann eine Szenariostruktur besser sein.

Beispiele:

- `WhenUserIsAnonymous`
- `WhenUserIsAuthenticated`
- `WhenUserIsAdmin`
- `WhenTenantIsActive`
- `WhenTenantIsSuspended`
- `WhenPaymentProviderIsUnavailable`
- `WhenFeatureFlagIsDisabled`

### Beispiel: SaaS-Szenarien

```java
class TenantAccessServiceTest {

    @Nested
    @DisplayName("wenn der Mandant aktiv ist")
    class WhenTenantIsActive {

        @Test
        @DisplayName("erlaubt Zugriff für berechtigten Benutzer")
        void allowsAccess_whenUserBelongsToTenant() {
            // Arrange
            var tenantId = new TenantId("tenant-a");
            var user = userOfTenant(tenantId);

            // Act
            var result = accessService.canAccess(user, tenantId);

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("wenn der Mandant gesperrt ist")
    class WhenTenantIsSuspended {

        @Test
        @DisplayName("verweigert Zugriff auch für berechtigten Benutzer")
        void deniesAccess_whenTenantIsSuspended() {
            // Arrange
            var tenantId = new TenantId("tenant-a");
            var user = userOfTenant(tenantId);
            suspendTenant(tenantId);

            // Act
            var result = accessService.canAccess(user, tenantId);

            // Assert
            assertThat(result).isFalse();
        }
    }
}
```

Für SaaS-Anwendungen ist diese Struktur besonders wertvoll, weil Security- und Tenant-Szenarien sichtbar bleiben.

---

## 10. Tiefe von `@Nested`-Hierarchien

`@Nested` verbessert Lesbarkeit, kann aber selbst missbraucht werden. Zu tiefe Hierarchien erzeugen Navigationsaufwand und verstecken Setup über mehrere Ebenen.

### Regel

- Eine Ebene `@Nested` ist oft sinnvoll.
- Zwei Ebenen sind erlaubt, wenn sie Methode und Untergruppe sauber trennen.
- Drei Ebenen sind die Obergrenze und müssen im Review begründet werden.
- Mehr als drei Ebenen sind nicht erlaubt; dann muss die Testklasse aufgeteilt werden.

### Gute zweistufige Struktur

```java
@Nested
@DisplayName("register()")
class Register {

    @Nested
    @DisplayName("Validierung")
    class Validation {

        @Test
        @DisplayName("wirft Exception bei leerem Namen")
        void throwsException_whenNameIsBlank() {
            // ...
        }

        @Test
        @DisplayName("wirft Exception bei ungültiger E-Mail")
        void throwsException_whenEmailIsInvalid() {
            // ...
        }
    }

    @Nested
    @DisplayName("Events")
    class Events {

        @Test
        @DisplayName("publiziert UserRegisteredEvent nach erfolgreicher Registrierung")
        void publishesEvent_onSuccess() {
            // ...
        }
    }
}
```

### Schlechte überstrukturierte Variante

```java
@Nested
class Register {
    @Nested
    class WhenInputIsValid {
        @Nested
        class AndEmailIsFree {
            @Nested
            class AndPasswordIsStrong {
                @Test
                void createsUser() {}
            }
        }
    }
}
```

Diese Struktur ist zu tief. Der Test sollte flacher formuliert oder in separate Testklassen aufgeteilt werden.

---

## 11. Test-Fixtures: Testdaten sauber bereitstellen

Testdaten sind Teil der Testqualität. Schlechte Testdaten machen Tests schwer lesbar, schwer wartbar und fehleranfällig.

### 11.1 Schlechte Anwendung: Inline-Testdaten in jedem Test

```java
@Test
void register_sendsEmail_onSuccess() {
    var user = new UserEntity();
    user.setId(1L);
    user.setName("Max Mustermann");
    user.setEmail("max@example.com");
    user.setCreatedAt(Instant.now());
    user.setActive(true);
    user.setRole(UserRole.USER);

    when(userRepository.save(any())).thenReturn(user);

    userService.register(new RegisterUserCommand("Max", "max@example.com", "secret123"));

    verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
}
```

Das Setup dominiert den Test. Der eigentliche fachliche Zweck geht unter.

### 11.2 Gute Anwendung: private Factory-Methoden

```java
private static UserEntity activeUser() {
    return new UserEntity(1L, "Max Mustermann", "max@example.com", UserRole.USER, true);
}

private static RegisterUserCommand validRegistrationCommand() {
    return new RegisterUserCommand("Max Mustermann", "max@example.com", "secret123");
}
```

Verwendung:

```java
@Test
void register_publishesUserRegisteredEvent_onSuccess() {
    // Arrange
    when(userRepository.existsByEmail("max@example.com")).thenReturn(false);
    when(userRepository.save(any())).thenReturn(activeUser());

    // Act
    userService.register(validRegistrationCommand());

    // Assert
    verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
}
```

### 11.3 Gute Anwendung: Test-Builder für komplexe Domänenobjekte

```java
public final class OrderTestBuilder {

    private UUID id = UUID.randomUUID();
    private UserId userId = new UserId(1L);
    private OrderStatus status = OrderStatus.PENDING;
    private final List<OrderItem> items = new ArrayList<>();

    private OrderTestBuilder() {
    }

    public static OrderTestBuilder anOrder() {
        return new OrderTestBuilder();
    }

    public OrderTestBuilder forUser(long userId) {
        this.userId = new UserId(userId);
        return this;
    }

    public OrderTestBuilder pending() {
        this.status = OrderStatus.PENDING;
        return this;
    }

    public OrderTestBuilder delivered() {
        this.status = OrderStatus.DELIVERED;
        return this;
    }

    public OrderTestBuilder withItem(String productId, int quantity) {
        this.items.add(new OrderItem(new ProductId(productId), new Quantity(quantity)));
        return this;
    }

    public Order build() {
        return Order.reconstitute(id, userId, status, List.copyOf(items));
    }
}
```

Verwendung:

```java
var order = OrderTestBuilder.anOrder()
        .forUser(42L)
        .delivered()
        .withItem("P1", 3)
        .build();
```

Ein Test-Builder ist gut, wenn er die fachliche Absicht lesbarer macht. Er ist schlecht, wenn er selbst komplexe Business-Logik enthält oder ungültige Zustände versteckt.

---

## 12. `@BeforeEach`: lokal, klein und transparent

`@BeforeEach` darf Setup vorbereiten. Es darf keine Assertions enthalten und keine fachlich wichtigen Bedingungen verstecken, die nur für einzelne Tests gelten.

### Schlechte Anwendung: globaler Setup-Dump

```java
@BeforeEach
void setUp() {
    when(userRepository.findById(any())).thenReturn(Optional.of(defaultUser()));
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("encoded");
    when(eventPublisher.publishEvent(any())).thenReturn(null);
    when(clock.instant()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
}
```

Problem: Jeder Test muss diesen Block kennen, auch wenn er nur eine einzige Konfiguration benötigt. Außerdem müssen Tests Sonderfälle durch Überschreiben erzeugen.

### Gute Anwendung: gruppenspezifisches Setup in `@Nested`

```java
@Nested
@DisplayName("register()")
class Register {

    private RegisterUserCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterUserCommand("Max", "max@example.com", "secret123");
    }

    @Test
    void returnsDto_whenEmailIsAvailable() {
        when(userRepository.existsByEmail(command.email())).thenReturn(false);

        var result = userService.register(command);

        assertThat(result.email()).isEqualTo("max@example.com");
    }
}
```

Hier bereitet `@BeforeEach` nur die gemeinsam benötigte Testdatenbasis der Gruppe vor. Mocks bleiben möglichst lokal im Test.

---

## 13. `@BeforeAll` und teure Ressourcen

`@BeforeAll` ist für teure Ressourcen vorgesehen, die nicht pro Test neu erzeugt werden sollen. Beispiele:

- Testcontainer.
- Embedded Broker.
- einmalige Schema-Initialisierung.
- teure Dateisystem- oder Zertifikatsvorbereitung.

`@BeforeAll` ist nicht für fachliches Standardszenario-Setup gedacht.

### Beispiel: Integrationstest mit teurer Ressource

```java
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    }

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        // ...
    }
}
```

Auch wenn eine Ressource nur einmal gestartet wird, muss der fachliche Zustand vor jedem Test bereinigt werden. Sonst entstehen Reihenfolgeabhängigkeiten.

---

## 14. `@TestInstance(PER_CLASS)` bewusst einsetzen

`@TestInstance(PER_CLASS)` erzeugt nur eine Testinstanz pro Testklasse. Dadurch sind nicht-statische `@BeforeAll`-Methoden möglich. Gleichzeitig steigt das Risiko, dass Felder zwischen Tests Zustände behalten.

### Zulässige Anwendung

```java
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExpensiveResourceTest {

    private final SomeExpensiveResource resource = new SomeExpensiveResource();

    @BeforeAll
    void initializeOnce() {
        resource.initialize();
    }

    @BeforeEach
    void resetBeforeEachTest() {
        resource.reset();
    }
}
```

### Regel

`PER_CLASS` darf nur verwendet werden, wenn einer der folgenden Gründe vorliegt:

- Nicht-statisches `@BeforeAll` ist fachlich oder technisch notwendig.
- Eine Extension oder Testressource verlangt es.
- Eine teure Ressource soll bewusst geteilt werden.
- Die Isolation wird durch `@BeforeEach` oder Transaktionsrollback nachweislich wiederhergestellt.

`PER_CLASS` darf nicht verwendet werden, um bequem Zustand zwischen Tests zu teilen.

---

## 15. Testisolation und Reihenfolge

Tests müssen unabhängig voneinander ausführbar sein. Ein Test darf nicht davon abhängen, dass ein anderer Test vorher lief.

### Verbotenes Muster

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceTest {

    private static Order order = new Order(new UserId(1L));

    @Test
    @Order(1)
    void addItem() {
        order.addItem(new ProductId("P1"), new Quantity(1));
    }

    @Test
    @Order(2)
    void cancelOrder() {
        order.cancel();
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }
}
```

Das ist kein Unit-Test-Design, sondern ein verstecktes Szenario-Skript. Wenn ein fachlicher Prozess in Schritten getestet werden soll, gehört er in einen einzelnen Integrationstest oder in explizite State-Transition-Tests mit frischem Setup.

### Erlaubte Ausnahme

`@TestMethodOrder` darf nur in sehr wenigen Fällen verwendet werden, zum Beispiel für bewusst sequenzielle technische Demonstrationstests oder externe Systemtests, bei denen die Reihenfolge Teil des Testszenarios ist. In normalen Unit Tests ist es verboten.

---

## 16. Wann Testklasse aufteilen statt `@Nested`?

`@Nested` ist kein Ersatz für sinnvolle Klassengrenzen. Wenn eine Testklasse trotz `@Nested` zu groß wird, ist meist auch das getestete Objekt zu breit.

Eine Testklasse sollte aufgeteilt werden, wenn:

- sie mehr als etwa 300 bis 500 Zeilen hat,
- sie mehr als drei `@Nested`-Hauptgruppen enthält,
- die Setup-Logik pro Gruppe völlig verschieden ist,
- unterschiedliche technische Testarten vermischt werden,
- Unit Tests und Spring-Integrationstests in derselben Klasse stehen,
- ein Service mehrere Use Cases enthält, die eigentlich separate Services sein sollten.

Beispiele für sinnvolle Aufteilung:

```text
UserRegistrationServiceTest
UserAuthenticationServiceTest
UserProfileServiceTest
UserPasswordChangeServiceTest
```

Statt:

```text
UserServiceTest
```

---

## 17. `@DisplayName`: lesbare Testausgabe

`@DisplayName` ist kein Ersatz für gute Methodennamen. Methodennamen bleiben technisch stabil und grepbar. `@DisplayName` verbessert die Lesbarkeit in IDE und CI.

### Standard

```java
@Test
@DisplayName("wirft UserNotFoundException, wenn die ID unbekannt ist")
void findById_throwsUserNotFoundException_whenIdIsUnknown() {
    // ...
}
```

Für `@Nested`-Klassen ist `@DisplayName` besonders wertvoll:

```java
@Nested
@DisplayName("findById()")
class FindById {
}
```

### Regel

- Methodennamen bleiben im technischen Format.
- `@DisplayName` darf in deutscher Sprache formuliert werden.
- `@DisplayName` muss fachliches Verhalten beschreiben, nicht Implementierungsdetails.
- Emojis, Witze und instabile Begriffe sind in Testnamen und Display Names nicht erlaubt.

---

## 18. `@Tag`: Testkategorien steuerbar machen

JUnit Tags dürfen verwendet werden, um Testkategorien im Build steuerbar zu machen.

Empfohlene Tags:

```java
@Tag("unit")
@Tag("integration")
@Tag("contract")
@Tag("slow")
@Tag("security")
```

Tags sind sinnvoll, wenn Build-Pipelines unterschiedliche Testmengen ausführen. Sie dürfen nicht dazu missbraucht werden, instabile Tests dauerhaft aus dem normalen Build auszuschließen.

### Regel

Ein Test mit `@Tag("slow")` braucht einen fachlichen Grund. Ein Test mit `@Tag("security")` muss ein sicherheitsrelevantes Verhalten prüfen. Ein Test mit `@Tag("integration")` darf externe oder containerisierte Infrastruktur nutzen. Ein Test mit `@Tag("unit")` darf keinen Spring ApplicationContext starten.

---

## 19. Spring Boot Testklassen

Spring-Boot-Testklassen müssen klar zwischen Unit Test, Slice Test und Integrationstest unterscheiden.

### Unit Test

```java
@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {
    // Kein Spring Context
}
```

### MVC Slice Test

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    // Nur Web-Schicht
}
```

### Integrationstest

```java
@SpringBootTest
class UserRegistrationIntegrationTest {
    // Voller ApplicationContext
}
```

### Regel

`@SpringBootTest` ist nicht der Default. Es wird nur verwendet, wenn der Spring ApplicationContext tatsächlich Teil des Tests ist. Für reine Domain-, Mapper- und Service-Tests wird kein Spring Context gestartet.

Spring Boot beschreibt `@SpringBootTest` als Testunterstützung, die einen ApplicationContext über Spring Boot lädt. Das ist wertvoll für Integration, aber unnötig schwer für reine Unit Tests. Siehe Spring Boot Testing Documentation: <https://docs.spring.io/spring-boot/reference/testing/index.html>.

---

## 20. Security- und SaaS-Aspekte

Testklassen in SaaS-Systemen müssen Sicherheits- und Mandantengrenzen sichtbar machen. Es reicht nicht, nur Happy Paths zu testen.

### Pflichtszenarien bei SaaS-relevanten Services

- Zugriff mit korrektem Tenant.
- Zugriff mit falschem Tenant.
- Zugriff ohne Berechtigung.
- Zugriff mit deaktiviertem Feature.
- Zugriff mit gesperrtem Tenant.
- Zugriff mit gelöschtem oder anonymisiertem User.
- Fehlerpfade ohne sensitive Daten in Exception Messages.
- Logs ohne Klartext-PII, soweit im Test überprüfbar.

### Beispiel

```java
@Nested
@DisplayName("Mandantentrennung")
class TenantIsolation {

    @Test
    @DisplayName("verweigert Zugriff auf Bestellung eines anderen Mandanten")
    void deniesAccess_whenOrderBelongsToDifferentTenant() {
        // Arrange
        var currentTenant = new TenantId("tenant-a");
        var foreignTenant = new TenantId("tenant-b");
        var orderId = new OrderId("order-1");

        when(orderRepository.findTenantIdByOrderId(orderId)).thenReturn(foreignTenant);

        // Act & Assert
        assertThatThrownBy(() -> orderService.loadOrder(currentTenant, orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageNotContaining("tenant-b");
    }
}
```

Sicherheitsrelevante Tests müssen so benannt und gruppiert sein, dass sie im Review sichtbar sind. Eine Untergruppe `Security`, `Authorization`, `TenantIsolation` oder `DataProtection` ist ausdrücklich erwünscht.

---

## 21. Anti-Patterns

### 21.1 Testklasse als Methodenfriedhof

```java
class UserServiceTest {
    @Test void test1() {}
    @Test void test2() {}
    @Test void test3() {}
    // 50 weitere Tests ohne Struktur
}
```

Problem: Verhalten ist nicht auffindbar.

### 21.2 Globaler veränderlicher Zustand

```java
static Order order = new Order(new UserId(1L));
```

Problem: Tests beeinflussen sich gegenseitig.

### 21.3 `@BeforeEach` mit verstecktem Fachverhalten

```java
@BeforeEach
void setUp() {
    order = OrderTestBuilder.anOrder()
            .paid()
            .shipped()
            .delivered()
            .build();
}
```

Problem: Ein Leser erkennt im Test selbst nicht, welcher Zustand relevant ist.

### 21.4 Zu tiefe Nested-Struktur

```java
@Nested class A {
    @Nested class B {
        @Nested class C {
            @Nested class D {
            }
        }
    }
}
```

Problem: Struktur wird zum Navigationshindernis.

### 21.5 Unit- und Integrationstests in einer Klasse

```java
@SpringBootTest
class UserServiceTest {

    @Test
    void pureCalculation_returnsTax() {}

    @Test
    void repositoryPersistsUser() {}
}
```

Problem: Schnelle Unit Tests werden unnötig langsam, und Testarten sind nicht klar getrennt.

### 21.6 Tests mit echten personenbezogenen Daten

```java
var email = "real.customer@company.com";
```

Problem: Personenbezogene Daten gehören nicht in Testcode. Verwende `example.com`, synthetische Daten oder nicht-personenbezogene Dummy-Werte.

---

## 22. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Struktur | Ist die Testklasse nach Methode, Zustand oder Szenario gruppiert? | `@Nested class Register` | Pflicht bei größeren Testklassen |
| Lesbarkeit | Erklärt der Testname Verhalten und Bedingung? | `findById_returnsUser_whenUserExists` | Pflicht |
| Display Name | Verbessert `@DisplayName` die IDE-/CI-Ausgabe? | „wirft Exception bei unbekannter ID“ | Empfohlen |
| Isolation | Gibt es keinen geteilten veränderlichen Zustand? | keine mutable `static` Fixture | Pflicht |
| Reihenfolge | Wird `@TestMethodOrder` vermieden? | keine Testreihenfolge | Pflicht |
| Setup | Ist `@BeforeEach` klein und transparent? | nur gemeinsames Minimalsetup | Pflicht |
| Testdaten | Werden Builder oder Factory-Methoden sinnvoll genutzt? | `validRegistrationCommand()` | Empfohlen |
| Nested-Tiefe | Maximal drei Ebenen? | Methode → Untergruppe | Pflicht |
| Testart | Sind Unit, Slice und Integration getrennt? | kein unnötiges `@SpringBootTest` | Pflicht |
| Security | Sind Berechtigungs- und Tenant-Fälle sichtbar gruppiert? | `@Nested class TenantIsolation` | Pflicht bei SaaS-Code |
| Datenschutz | Werden keine echten personenbezogenen Daten verwendet? | `max@example.com` | Pflicht |
| Wartbarkeit | Ist die Klasse noch klein genug oder sollte sie geteilt werden? | mehrere Services getrennt testen | Review-Entscheidung |

---

## 23. Automatisierbare Prüfungen

Nicht jede Teststruktur lässt sich perfekt automatisieren. Einige Regeln können aber in CI/CD unterstützt werden.

### Mögliche Regeln

```text
- Keine @TestMethodOrder in Unit-Testpaketen.
- Keine mutable static Felder in Testklassen.
- Keine @SpringBootTest in Klassen mit Namen *UnitTest.
- Keine echten Domains außerhalb erlaubter Testdomains wie example.com.
- Keine Testdaten mit produktiven E-Mail-Domains.
- Keine @Disabled-Tests ohne Ticketreferenz und Ablaufdatum.
- Keine Testklasse über definierte maximale Zeilenzahl ohne Begründung.
- Keine Nested-Tiefe größer als 3.
```

### Beispielhafte ArchUnit-Regel

```java
@AnalyzeClasses(packages = "com.example")
class TestArchitectureRules {

    @ArchTest
    static final ArchRule unit_tests_should_not_use_spring_boot_test =
            noClasses()
                    .that().haveSimpleNameEndingWith("UnitTest")
                    .should().beAnnotatedWith(SpringBootTest.class)
                    .because("Unit Tests dürfen keinen Spring ApplicationContext starten.");

    @ArchTest
    static final ArchRule tests_should_not_use_test_method_order =
            noClasses()
                    .that().resideInAPackage("..")
                    .should().beAnnotatedWith(TestMethodOrder.class)
                    .because("Tests müssen unabhängig von Ausführungsreihenfolge sein.");
}
```

Diese Regeln sind Beispiele und müssen an Paketstruktur, Namenskonventionen und Build-Setup des Projekts angepasst werden.

---

## 24. Migration bestehender Testklassen

Bestehende große Testklassen werden schrittweise migriert.

### Schritt 1: Methoden gruppieren

Zuerst werden Testmethoden nach Methode, Zustand oder Szenario sortiert.

### Schritt 2: `@Nested` einführen

Danach werden logische Gruppen in `@Nested`-Klassen überführt.

### Schritt 3: Setup entflechten

Globales `@BeforeEach` wird reduziert. Gruppenspezifisches Setup wandert in die jeweilige `@Nested`-Klasse.

### Schritt 4: Testdaten extrahieren

Wiederholte Inline-Testdaten werden durch private Factory-Methoden oder Test-Builder ersetzt.

### Schritt 5: Isolation prüfen

Mutable `static` Felder, Reihenfolgen und implizite Abhängigkeiten werden entfernt.

### Schritt 6: Testarten trennen

Unit Tests, Slice Tests und Integrationstests werden in separate Klassen verschoben.

### Schritt 7: Security-Gruppen ergänzen

Sicherheits-, Tenant- und Berechtigungsszenarien werden sichtbar gruppiert.

---

## 25. Ausnahmen

Ausnahmen sind zulässig, aber begründungspflichtig.

### Zulässige Ausnahmen

- Sehr kleine Testklassen mit weniger als acht Testmethoden brauchen kein `@Nested`.
- `@TestMethodOrder` ist in expliziten sequenziellen Systemtests erlaubt.
- `@TestInstance(PER_CLASS)` ist erlaubt, wenn eine teure Ressource bewusst geteilt wird und Isolation gesichert ist.
- `@BeforeAll` darf für teure technische Ressourcen genutzt werden.
- Mehrere Assertions sind erlaubt, wenn sie ein gemeinsames Ergebnis beschreiben.

### Nicht zulässige Ausnahmen

- Testreihenfolge als Ersatz für sauberes Setup.
- Produktive personenbezogene Daten im Testcode.
- Dauerhaft deaktivierte Tests ohne Ticket und Ablaufdatum.
- Gemeinsamer mutable Zustand zwischen Unit Tests.
- `@SpringBootTest` als bequemer Default für einfache Unit Tests.

---

## 26. Entscheidungshilfe

| Frage | Wenn ja | Wenn nein |
|---|---|---|
| Hat die Testklasse mehr als acht bis zehn Testmethoden? | `@Nested` einführen oder Klasse teilen | Flache Struktur kann bleiben |
| Testet die Klasse mehrere Methoden? | Nach Methode gruppieren | Andere Struktur prüfen |
| Hat das Objekt eine Zustandsmaschine? | Nach Zustand gruppieren | Nach Methode oder Szenario gruppieren |
| Gibt es Security-/Tenant-Fälle? | Eigene `@Nested`-Gruppe verwenden | Nicht nötig |
| Ist Setup pro Gruppe verschieden? | Setup in jeweilige `@Nested`-Klasse verschieben | Gemeinsames minimales Setup möglich |
| Wird Spring Context wirklich benötigt? | Slice oder Integrationstest | Unit Test ohne Spring |
| Werden Tests nur mit Reihenfolge grün? | Designfehler beheben | Gut |
| Ist `PER_CLASS` nötig? | Isolation explizit wiederherstellen | Default `PER_METHOD` verwenden |

---

## 27. Definition of Done

Eine Testklasse erfüllt diese Richtlinie, wenn sie nach Verhalten strukturiert ist, bei größerem Umfang `@Nested` verwendet, sprechende Testnamen und gegebenenfalls `@DisplayName` nutzt, jeden Test isoliert ausführt, keine Reihenfolgeabhängigkeit besitzt, Testdaten über klare Factory-Methoden oder Builder erzeugt, keine echten personenbezogenen Daten enthält, Unit-/Slice-/Integrationstests sauber trennt und sicherheitsrelevante SaaS-Szenarien sichtbar prüft.

---

## 28. Quellen und weiterführende Literatur

- JUnit 5 User Guide — Test Classes, `@Nested`, Test Instance Lifecycle, Tags, Display Names: <https://docs.junit.org/5.10.2/user-guide/index.html>
- JUnit 5 User Guide — aktuelle Dokumentation: <https://docs.junit.org/current/user-guide/>
- Spring Boot Reference Documentation — Testing: <https://docs.spring.io/spring-boot/reference/testing/index.html>
- AssertJ Dokumentation: <https://assertj.github.io/doc/>
- Mockito JUnit Jupiter Extension: <https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/org.mockito.junit.jupiter/org/mockito/junit/jupiter/MockitoExtension.html>
- Testcontainers Dokumentation: <https://java.testcontainers.org/>
