# QG-JAVA-025 — SOLID: Die fünf Prinzipien der objektorientierten Gestaltung

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-025 |
| Titel | SOLID: Die fünf Prinzipien der objektorientierten Gestaltung |
| Status | Akzeptiert / verbindlicher Qualitätsstandard für objektorientiertes Design in Java-Code |
| Version | 1.0.0 |
| Datum | 2026-05-02 |
| Review-Zyklus | Jährlich oder bei grundlegenden Änderungen an Architektur-, Test- oder Coding-Standards |
| Java-Baseline | Java 21+ |
| Technologiekontext | Java 21, Spring Boot 3.x, JUnit 5, AssertJ, Mockito, ArchUnit |
| Kategorie | Design-Prinzipien / Objektorientierung / Wartbarkeit / Testbarkeit |
| Zielgruppe | Java-Entwickler, Tech Leads, Architekten, Reviewer, QA Engineers |
| Verbindlichkeit | SOLID wird als Diagnose- und Gestaltungswerkzeug verwendet. Abweichungen sind erlaubt, wenn Einfachheit, fachliche Klarheit oder YAGNI stärker wiegen und die Entscheidung im Review nachvollziehbar ist. |
| Qualitätsziel | Code soll kohärent, testbar, erweiterbar, ersetzbar und verständlich sein, ohne durch spekulative Abstraktion unnötig komplex zu werden. |
| Prüfstatus | Fachlich validiert gegen etablierte Literatur zu SOLID, Liskov Substitution, Spring Dependency Injection und YAGNI. |
| Automatisierbarkeit | Teilweise über ArchUnit, Checkstyle, PMD, SonarQube, Dependency-Analysen und Review-Checklisten prüfbar. |
| Sicherheitsrelevanz | Mittel bis hoch. Schlechte Objektstruktur führt häufig zu verstreuter Autorisierung, unklaren Verantwortlichkeiten, unsicheren Abhängigkeiten, schwer testbaren Fehlerpfaden und fehlender Mandantentrennung. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie die fünf SOLID-Prinzipien in Java-21-Codebasen praktisch angewendet werden. SOLID ist kein Dogma und kein mechanisches Regelwerk. SOLID ist ein Diagnoseinstrument für häufige Designprobleme: zu große Klassen, unkontrollierte Änderungen, falsche Vererbung, zu breite Schnittstellen und harte Kopplung an konkrete Implementierungen.

Das Ziel dieser Richtlinie ist nicht, möglichst viele Interfaces, Klassen oder Patterns zu erzeugen. Das Ziel ist, Code so zu strukturieren, dass fachliche Regeln auffindbar bleiben, Änderungen lokal begrenzt sind, Tests ohne unnötigen Infrastrukturaufwand möglich sind und neue Anforderungen nicht regelmäßig alte, funktionierende Logik beschädigen.

---

## 2. Kurzregel

Verwende SOLID als Prüffrage im Design und im Code Review. Eine Klasse soll eine klare Verantwortung haben, Erweiterungen sollen möglichst ohne riskante Änderungen an bestehender Logik möglich sein, Subtypen müssen sich korrekt als Basistyp verwenden lassen, Schnittstellen sollen klein und nutzerorientiert sein, und fachlich zentrale Module sollen nicht direkt von technischen Details abhängen.

SOLID darf nicht spekulativ angewendet werden. Eine Abstraktion ist nur dann gerechtfertigt, wenn sie ein reales Änderungs-, Test-, Integrations- oder Domänenproblem löst.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Domänenklassen
- Application Services
- Spring Services
- Ports und Adapter
- Strategien und Policies
- Validatoren
- Mapper
- Integrationsclients
- Repository-Schnittstellen
- Test-Doubles
- Modulgrenzen
- Security- und Tenant-spezifische Kontrolllogik

Diese Richtlinie gilt nicht als Aufforderung, für jede Klasse ein Interface zu erzeugen oder jede einfache Methode in ein Pattern zu überführen. Einfache CRUD-Flüsse, kleine Hilfsfunktionen und stabile technische Adapter dürfen bewusst einfach bleiben.

---

## 4. Technischer Hintergrund

SOLID steht für fünf Prinzipien objektorientierter Gestaltung:

| Prinzip | Name | Kernfrage |
|---|---|---|
| S | Single Responsibility Principle | Hat diese Klasse genau einen klaren Grund, sich zu ändern? |
| O | Open/Closed Principle | Kann neues Verhalten ergänzt werden, ohne stabile Logik riskant zu verändern? |
| L | Liskov Substitution Principle | Kann jeder Subtyp überall dort verwendet werden, wo der Basistyp erwartet wird? |
| I | Interface Segregation Principle | Hängt ein Client nur von den Methoden ab, die er wirklich braucht? |
| D | Dependency Inversion Principle | Hängen fachlich zentrale Module von Abstraktionen statt von technischen Details ab? |

SOLID ist besonders relevant in langlebigen Java-Systemen, weil dort Änderungen nicht isoliert auftreten. Eine neue Zahlungsart, ein neuer Mandant, eine neue Berechtigungsregel, eine neue externe Schnittstelle oder ein neues Reporting-Format verändert oft mehrere Stellen gleichzeitig. SOLID hilft dabei, solche Änderungsachsen sichtbar zu machen und den Code entlang echter Verantwortlichkeiten zu schneiden.

---

## 5. Verbindlicher Standard

Für neue produktive Java-Klassen gilt:

1. Eine Klasse muss eine klar benennbare Verantwortung haben.
2. Öffentliche Methoden müssen fachliches Verhalten ausdrücken, nicht nur interne Daten freigeben.
3. Vererbung darf nur bei echtem substituierbarem Verhalten eingesetzt werden.
4. Komposition ist gegenüber Implementierungsvererbung zu bevorzugen.
5. Abhängigkeiten werden per Konstruktor injiziert.
6. Fachliche Services instanziieren technische Abhängigkeiten nicht selbst.
7. Interfaces werden dort verwendet, wo mehrere Implementierungen, Test-Doubles, externe Grenzen oder stabile Ports existieren.
8. Interfaces werden nicht mechanisch für jede Klasse erzeugt.
9. Breite Interfaces werden in kleinere, clientnahe Rollen getrennt.
10. Jede Abstraktion muss einen erkennbaren Zweck haben.

---

# Teil A — Die fünf SOLID-Prinzipien

---

## 6. S — Single Responsibility Principle

### 6.1 Regel

Eine Klasse soll genau eine fachlich oder technisch klar benennbare Verantwortung haben. Praktisch bedeutet das: Eine Klasse soll nur aus einem zusammenhängenden Grund geändert werden müssen.

Die Prüffrage lautet:

> Kann ich die Verantwortung dieser Klasse in einem Satz erklären, ohne „und“, „sowie“, „außerdem“ oder eine lange Aufzählung zu brauchen?

Wenn die Antwort nein ist, ist die Klasse wahrscheinlich zu breit.

---

### 6.2 Schlechte Anwendung: UserService als Sammelstelle

```java
@Service
public class UserService {

    public void register(RegisterCommand command) {
        // Validierung
        // Passwort-Hashing
        // User speichern
        // E-Mail versenden
        // Billing Trial anlegen
        // Admins informieren
        // Audit Log schreiben
    }

    public void sendWelcomeEmail(User user) {
        // SMTP-Details
    }

    public String generateCsvReport(List<User> users) {
        // CSV-Formatierung
    }

    public void logUserAction(User user, String action) {
        // Logging-Format
    }

    public void upgradeSubscription(Long userId, String paymentToken) {
        // Payment-Provider-Integration
    }
}
```

Diese Klasse hat mehrere Änderungsgründe: Registrierung, E-Mail, Reporting, Logging, Billing und Payment. Jede Änderung an einem dieser Themen kann dieselbe Klasse verändern. Dadurch steigt das Risiko, dass ein harmloses Refactoring versehentlich andere Funktionen beschädigt.

---

### 6.3 Gute Anwendung: Verantwortlichkeiten trennen

```java
@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final ApplicationEventPublisher events;

    public UserRegistrationService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.events = events;
    }

    @Transactional
    public UserCreatedResponse register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        var user = User.register(
                command.name(),
                command.email(),
                passwordHasher.hash(command.password())
        );

        var saved = userRepository.save(user);
        events.publishEvent(new UserRegisteredEvent(saved.id(), saved.email()));

        return UserCreatedResponse.from(saved);
    }
}
```

```java
@Service
public class WelcomeEmailService {

    private final MailSender mailSender;

    public WelcomeEmailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(UserRegisteredEvent event) {
        mailSender.send(MailMessage.welcome(event.email()));
    }
}
```

```java
@Service
public class UserReportService {

    public CsvDocument createUserReport(List<UserReportRow> rows) {
        return CsvDocument.fromRows(rows);
    }
}
```

Jetzt hat jede Klasse einen klareren Änderungsgrund. Das bedeutet nicht, dass jede Methode eine eigene Klasse braucht. Es bedeutet, dass unterschiedliche Änderungsachsen getrennt werden.

---

### 6.4 SRP-Heuristiken

| Signal | Bedeutung | Mögliche Korrektur |
|---|---|---|
| Klassenname enthält `Manager`, `Helper`, `Processor`, `Handler` ohne Fachbezug | Verantwortung ist wahrscheinlich unklar | Fachlichen Namen wählen oder Klasse aufteilen |
| Mehr als 4–5 Konstruktorabhängigkeiten | Klasse orchestriert wahrscheinlich zu viel | Verantwortlichkeiten schneiden |
| Test braucht viele Mocks | Klasse hat zu viele externe Gründe | Ports, Subservices oder Domain-Objekte extrahieren |
| Methoden betreffen unterschiedliche Fachbereiche | Mehrere Änderungsgründe | Services nach Use Cases trennen |
| Eine Änderung an E-Mail-Template verändert UserService | Falsche Verantwortlichkeit | E-Mail-Service oder Event-Listener extrahieren |

---

### 6.5 SRP und Security

SRP ist sicherheitsrelevant. Wenn Autorisierung, Validierung, Logging, Fachlogik und Persistenz unsauber vermischt sind, wird schwer erkennbar, ob eine Berechtigungsprüfung wirklich vor jeder kritischen Aktion erfolgt.

Schlecht:

```java
@Service
public class TenantAdminService {

    public void deleteUser(Long tenantId, Long userId, User currentUser) {
        var user = userRepository.findById(userId).orElseThrow();

        // Berechtigungsprüfung zwischen Fach- und Persistenzlogik versteckt
        if (!currentUser.isAdmin()) {
            throw new AccessDeniedException("Not allowed");
        }

        userRepository.delete(user);
        auditLogger.log("Deleted user " + user.email());
    }
}
```

Besser:

```java
@Service
public class TenantUserDeletionService {

    private final TenantAuthorizationService authorization;
    private final TenantUserRepository users;
    private final AuditLog auditLog;

    public void deleteUser(DeleteTenantUserCommand command) {
        authorization.requireCanDeleteUser(command.actor(), command.tenantId());

        var user = users.findByTenantIdAndUserId(command.tenantId(), command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        users.delete(user);
        auditLog.userDeleted(command.tenantId(), command.userId(), command.actor().id());
    }
}
```

Die Verantwortung ist klarer: Der Service orchestriert den Use Case. Die Autorisierung ist explizit. Das Audit-Log enthält keine unnötige E-Mail-Adresse.

---

## 7. O — Open/Closed Principle

### 7.1 Regel

Code soll offen für Erweiterung, aber geschlossen für riskante Modifikation sein. Das bedeutet nicht, dass bestehender Code nie verändert werden darf. Es bedeutet: Wenn ein Bereich regelmäßig durch neue Varianten erweitert wird, soll die Struktur so gestaltet sein, dass neue Varianten möglichst durch Ergänzung statt durch Änderung stabiler Entscheidungslogik eingeführt werden.

---

### 7.2 Schlechte Anwendung: Rabattlogik als `if`-Kaskade

```java
public BigDecimal calculateDiscount(Order order, CustomerType type) {
    if (type == CustomerType.PREMIUM) {
        return order.total().multiply(new BigDecimal("0.20"));
    }
    if (type == CustomerType.SEASONAL) {
        return order.total().multiply(new BigDecimal("0.10"));
    }
    if (type == CustomerType.LOYALTY) {
        return order.total().multiply(new BigDecimal("0.15"));
    }
    if (type == CustomerType.EMPLOYEE) {
        return order.total().multiply(new BigDecimal("0.30"));
    }
    return BigDecimal.ZERO;
}
```

Bei jeder neuen Rabattart muss die Methode geändert werden. Das ist akzeptabel, wenn es nur zwei stabile Fälle gibt. Es ist problematisch, wenn Rabatte ein aktiver Erweiterungspunkt sind.

---

### 7.3 Gute Anwendung: Strategie für echte Variabilität

```java
public interface DiscountPolicy {

    boolean appliesTo(Order order, Customer customer);

    BigDecimal calculate(Order order, Customer customer);
}
```

```java
@Component
public class PremiumCustomerDiscountPolicy implements DiscountPolicy {

    @Override
    public boolean appliesTo(Order order, Customer customer) {
        return customer.type() == CustomerType.PREMIUM;
    }

    @Override
    public BigDecimal calculate(Order order, Customer customer) {
        return order.total().multiply(new BigDecimal("0.20"));
    }
}
```

```java
@Component
public class EmployeeDiscountPolicy implements DiscountPolicy {

    @Override
    public boolean appliesTo(Order order, Customer customer) {
        return customer.isEmployee();
    }

    @Override
    public BigDecimal calculate(Order order, Customer customer) {
        return order.total().multiply(new BigDecimal("0.30"));
    }
}
```

```java
@Service
public class DiscountService {

    private final List<DiscountPolicy> policies;

    public DiscountService(List<DiscountPolicy> policies) {
        this.policies = List.copyOf(policies);
    }

    public BigDecimal calculateDiscount(Order order, Customer customer) {
        return policies.stream()
                .filter(policy -> policy.appliesTo(order, customer))
                .map(policy -> policy.calculate(order, customer))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

Neue Rabattarten können durch neue Klassen ergänzt werden. Die zentrale Berechnung bleibt stabil.

---

### 7.4 OCP mit `sealed` und `switch`

Nicht jede Erweiterbarkeit braucht eine offene Strategie. Wenn alle Varianten fachlich geschlossen sind, ist ein `sealed interface` mit exhaustivem `switch` oft besser.

```java
public sealed interface PaymentResult
        permits PaymentSucceeded, PaymentRejected, PaymentPending {
}

public record PaymentSucceeded(String transactionId) implements PaymentResult {}
public record PaymentRejected(String reason) implements PaymentResult {}
public record PaymentPending(Instant since) implements PaymentResult {}
```

```java
public String describe(PaymentResult result) {
    return switch (result) {
        case PaymentSucceeded s -> "Zahlung gebucht: " + s.transactionId();
        case PaymentRejected r -> "Zahlung abgelehnt: " + r.reason();
        case PaymentPending p -> "Zahlung ausstehend seit: " + p.since();
    };
}
```

Das ist **nicht** offen für beliebige Erweiterung. Genau das ist hier richtig, weil die Typfamilie geschlossen sein soll. SOLID bedeutet nicht, jede Hierarchie offen zu machen.

---

### 7.5 OCP-Heuristiken

| Frage | Entscheidung |
|---|---|
| Kommen regelmäßig neue Varianten hinzu? | Strategie, Policy oder Plugin-Struktur prüfen |
| Ist die Variantenmenge fachlich abgeschlossen? | `sealed interface` und exhaustiver `switch` prüfen |
| Gibt es nur zwei stabile Fälle? | Ein einfacher `if` kann besser sein |
| Wird ein Pattern nur für einen hypothetischen zweiten Fall gebaut? | YAGNI prüfen |
| Werden bestehende Tests bei jeder neuen Variante angepasst? | OCP-Verletzung wahrscheinlich |

---

### 7.6 OCP und SaaS

In SaaS-Systemen entstehen Varianten häufig durch Tarife, Mandanten, Regionen, Integrationen oder Feature Flags. OCP hilft, diese Varianten zu strukturieren.

Schlecht:

```java
if (tenantId.equals("tenant-a")) {
    // Sonderfall
} else if (tenantId.equals("tenant-b")) {
    // anderer Sonderfall
} else if (featureFlags.isEnabled("new-pricing")) {
    // weiterer Sonderfall
}
```

Besser:

```java
public interface PricingPolicy {
    boolean appliesTo(TenantContext tenant);
    Price calculate(PriceCalculationCommand command);
}
```

Mandantenlogik darf aber nicht blind in Codevarianten zerfallen. Wenn es um kommerzielle Freischaltungen geht, gehört die Entscheidung häufig in ein Entitlement-Modell, nicht in technische `if`-Kaskaden.

---

## 8. L — Liskov Substitution Principle

### 8.1 Regel

Ein Subtyp muss überall dort verwendet werden können, wo der Basistyp erwartet wird, ohne dass das Programmverhalten bricht. Es reicht nicht, dass der Code kompiliert. Entscheidend ist semantische Ersetzbarkeit.

Die Prüffrage lautet:

> Würden alle Tests, die für den Basistyp geschrieben wurden, auch für jeden Subtyp bestehen?

---

### 8.2 Schlechte Anwendung: Rectangle und Square

```java
public class Rectangle {

    protected int width;
    protected int height;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int area() {
        return width * height;
    }
}
```

```java
public class Square extends Rectangle {

    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;
    }

    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height;
    }
}
```

```java
void verifyRectangleBehavior(Rectangle rectangle) {
    rectangle.setWidth(5);
    rectangle.setHeight(4);

    assert rectangle.area() == 20;
}
```

Ein `Square` ist mathematisch ein Spezialfall eines Rechtecks. Verhaltenstechnisch ist es hier aber kein ersetzbarer Subtyp, weil Setter andere Seiteneffekte haben.

---

### 8.3 Gute Anwendung: Gemeinsames Verhalten statt falscher Vererbung

```java
public interface Shape {
    int area();
}
```

```java
public record Rectangle(int width, int height) implements Shape {

    public Rectangle {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
    }

    @Override
    public int area() {
        return width * height;
    }
}
```

```java
public record Square(int side) implements Shape {

    public Square {
        if (side <= 0) {
            throw new IllegalArgumentException("side must be positive");
        }
    }

    @Override
    public int area() {
        return side * side;
    }
}
```

Beide Typen teilen nur das Verhalten, das wirklich gemeinsam ist: `area()`.

---

### 8.4 Typische LSP-Verletzungen in Java

#### 8.4.1 Subklasse wirft unerwartet `UnsupportedOperationException`

```java
public interface Exporter {
    void export(Report report);
}
```

```java
public class PdfExporter implements Exporter {
    @Override
    public void export(Report report) {
        // PDF erzeugen
    }
}
```

```java
public class DisabledExporter implements Exporter {
    @Override
    public void export(Report report) {
        throw new UnsupportedOperationException("Export disabled");
    }
}
```

Wenn `DisabledExporter` überall dort eingesetzt wird, wo `Exporter` erwartet wird, bricht das Verhalten. Besser ist ein separater Typ oder ein explizites Ergebnis:

```java
public sealed interface ExportResult permits Exported, ExportDisabled, ExportFailed {
}

public record Exported(String fileName) implements ExportResult {}
public record ExportDisabled(String reason) implements ExportResult {}
public record ExportFailed(String reason) implements ExportResult {}
```

#### 8.4.2 Methode verschärft Vorbedingungen

```java
public interface PaymentProcessor {
    PaymentResult pay(PaymentCommand command);
}
```

Eine Implementierung, die nur bestimmte Länder akzeptiert, obwohl das Interface allgemein wirkt, verschärft die Vorbedingungen.

Besser:

```java
public interface PaymentProcessor {
    boolean supports(PaymentCommand command);
    PaymentResult pay(PaymentCommand command);
}
```

Oder die Auswahl wird über einen Router abgebildet:

```java
@Service
public class PaymentProcessorRouter {

    private final List<PaymentProcessor> processors;

    public PaymentProcessor selectFor(PaymentCommand command) {
        return processors.stream()
                .filter(processor -> processor.supports(command))
                .findFirst()
                .orElseThrow(() -> new UnsupportedPaymentMethodException(command.method()));
    }
}
```

---

### 8.5 LSP-Prüfkriterien

| Aspekt | Prüffrage |
|---|---|
| Vorbedingungen | Verlangt der Subtyp mehr als der Basistyp verspricht? |
| Nachbedingungen | Liefert der Subtyp weniger als der Basistyp verspricht? |
| Exceptions | Wirft der Subtyp unerwartete Exceptions für normale Basistyp-Nutzung? |
| Seiteneffekte | Führt der Subtyp zusätzliche Seiteneffekte ein? |
| Invarianten | Bricht der Subtyp Annahmen, die für den Basistyp gelten? |

---

## 9. I — Interface Segregation Principle

### 9.1 Regel

Clients sollen nicht von Methoden abhängen, die sie nicht nutzen. Interfaces sollen nach Rollen und Nutzung geschnitten werden, nicht nach Datenbanktabellen oder technischen Sammelbegriffen.

---

### 9.2 Schlechte Anwendung: fettes Repository-Interface

```java
public interface UserRepository {

    Optional<User> findById(UserId id);

    List<User> findAll();

    User save(User user);

    void delete(UserId id);

    List<User> findByEmailDomain(String domain);

    UserStatistics generateStatistics();

    void bulkImport(List<User> users);

    void anonymize(UserId id);

    void lock(UserId id);

    void unlock(UserId id);
}
```

Jeder Client sieht alles: Lesen, Schreiben, Admin, Statistik, Import, Datenschutz, Sperrung. Dadurch kann ein Service versehentlich Methoden nutzen, die er fachlich nicht nutzen sollte.

---

### 9.3 Gute Anwendung: rollenorientierte Schnittstellen

```java
public interface UserReader {
    Optional<User> findById(UserId id);
}
```

```java
public interface UserWriter {
    User save(User user);
}
```

```java
public interface UserAdministration {
    void lock(UserId id);
    void unlock(UserId id);
}
```

```java
public interface UserAnonymizer {
    void anonymize(UserId id);
}
```

```java
@Service
public class UserProfileService {

    private final UserReader users;

    public UserProfileService(UserReader users) {
        this.users = users;
    }

    public UserProfile getProfile(UserId id) {
        return users.findById(id)
                .map(UserProfile::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

Der `UserProfileService` kann nicht löschen, sperren oder anonymisieren, weil seine Abhängigkeit diese Methoden gar nicht anbietet.

---

### 9.4 ISP und Spring Data

Spring Data Repository Interfaces sind oft technisch breit. Das ist nicht automatisch falsch, aber fachlich gefährlich, wenn sie unkontrolliert in Application Services injiziert werden.

Besser ist eine fachliche Port-Schnittstelle:

```java
public interface FindUserByIdPort {
    Optional<User> findById(UserId id);
}
```

```java
@Repository
class JpaUserRepositoryAdapter implements FindUserByIdPort, SaveUserPort {

    private final SpringDataUserRepository repository;
    private final UserMapper mapper;

    @Override
    public Optional<User> findById(UserId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }
}
```

So bleiben fachliche Services unabhängig von der technischen Breite des Spring-Data-Repositories.

---

### 9.5 ISP und Security

ISP kann Berechtigungsgrenzen im Code unterstützen. Wenn ein Service nur eine Lese-Schnittstelle bekommt, kann er technisch nicht versehentlich schreiben.

Schlecht:

```java
@Service
public class UserLookupService {

    private final UserRepository repository;

    public void lookup(UserId id) {
        var user = repository.findById(id).orElseThrow();

        // versehentlich möglich, obwohl der Service nur lesen sollte
        repository.delete(id);
    }
}
```

Besser:

```java
@Service
public class UserLookupService {

    private final UserReader users;

    public UserLookupService(UserReader users) {
        this.users = users;
    }

    public User lookup(UserId id) {
        return users.findById(id).orElseThrow();
    }
}
```

---

### 9.6 ISP-Grenze: Nicht jede Methode braucht ein eigenes Interface

Zu viele Mikro-Interfaces können die Codebasis ebenfalls verschlechtern. ISP bedeutet nicht, pro Methode automatisch ein Interface zu erzeugen. Eine Schnittstelle soll eine kohärente Rolle beschreiben.

Gute Rollennamen:

- `UserReader`
- `UserWriter`
- `PaymentGateway`
- `MailSender`
- `TenantAuthorization`
- `InvoiceExporter`

Schlechte Rollennamen:

- `UserFindByIdInterface`
- `UserSaveInterface`
- `DoUserThing`
- `AbstractUserProcessor`
- `IUserService`

---

## 10. D — Dependency Inversion Principle

### 10.1 Regel

Fachlich zentrale Module sollen nicht direkt von technischen Details abhängen. High-Level-Module hängen von Abstraktionen ab; technische Details implementieren diese Abstraktionen.

Dependency Inversion ist ein Designprinzip. Dependency Injection ist ein Mechanismus, um dieses Prinzip umzusetzen. Spring kann dabei helfen, ersetzt aber nicht die Designentscheidung.

---

### 10.2 Schlechte Anwendung: direkte Instanziierung technischer Details

```java
public class UserRegistrationService {

    private final MySqlUserRepository userRepository = new MySqlUserRepository();
    private final SmtpMailSender mailSender = new SmtpMailSender("smtp.example.com");
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void register(RegisterUserCommand command) {
        // ...
    }
}
```

Der Service ist direkt an MySQL, SMTP und eine konkrete Encoder-Implementierung gekoppelt. Ein Unit Test ohne Datenbank und SMTP wird schwer. Ein Wechsel des Mail-Providers verändert fachliche Logik.

---

### 10.3 Gute Anwendung: Ports und Konstruktorinjektion

```java
public interface UserRegistrationRepository {
    boolean existsByEmail(Email email);
    User save(User user);
}
```

```java
public interface PasswordHasher {
    PasswordHash hash(RawPassword password);
}
```

```java
public interface UserRegisteredEventPublisher {
    void publish(UserRegisteredEvent event);
}
```

```java
@Service
public class UserRegistrationService {

    private final UserRegistrationRepository users;
    private final PasswordHasher passwordHasher;
    private final UserRegisteredEventPublisher events;

    public UserRegistrationService(
            UserRegistrationRepository users,
            PasswordHasher passwordHasher,
            UserRegisteredEventPublisher events) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.events = events;
    }

    @Transactional
    public UserCreatedResponse register(RegisterUserCommand command) {
        var email = new Email(command.email());

        if (users.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        var user = User.register(
                new DisplayName(command.name()),
                email,
                passwordHasher.hash(new RawPassword(command.password()))
        );

        var saved = users.save(user);
        events.publish(new UserRegisteredEvent(saved.id(), saved.email()));

        return UserCreatedResponse.from(saved);
    }
}
```

Technische Adapter implementieren die Ports:

```java
@Repository
class JpaUserRegistrationRepository implements UserRegistrationRepository {

    private final SpringDataUserRepository repository;
    private final UserMapper mapper;

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmailHash(email.searchHash());
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }
}
```

---

### 10.4 DIP mit Spring

In Spring-Anwendungen wird Konstruktorinjektion als Standard verwendet. Feldinjektion ist für produktiven Code nicht erlaubt.

Schlecht:

```java
@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private MailSender mailSender;
}
```

Besser:

```java
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final MailSender mailSender;

    public InvoiceService(InvoiceRepository invoiceRepository, MailSender mailSender) {
        this.invoiceRepository = invoiceRepository;
        this.mailSender = mailSender;
    }
}
```

Vorteile:

- Abhängigkeiten sind sichtbar.
- Tests können ohne Spring Context instanziieren.
- Zirkuläre Abhängigkeiten fallen früher auf.
- Felder können `final` sein.
- Die Klasse beschreibt ihre benötigten Kollaboratoren explizit.

---

### 10.5 DIP-Grenze: Nicht jede konkrete Klasse braucht ein Interface

Dieses Anti-Pattern ist in Java-Projekten weit verbreitet:

```java
public interface UserMapper {
    UserDto toDto(User user);
}
```

```java
@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserDto toDto(User user) {
        return new UserDto(user.id(), user.name(), user.email());
    }
}
```

Wenn es nur eine Implementierung gibt, keine externe Grenze, kein Test-Double, keine Austauschbarkeit und keine stabile Port-Grenze, ist ein Interface oft nur zusätzlicher Lärm.

Besser kann eine finale Klasse sein:

```java
@Component
public final class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(user.id(), user.name(), user.email());
    }
}
```

DIP bedeutet: **Abstrahiere dort, wo Abhängigkeiten instabil, extern, schwer testbar oder fachlich austauschbar sind.** Nicht: „Erzeuge für jede Klasse ein Interface.“

---

# Teil B — SOLID im Java-/Spring-Alltag

---

## 11. Zusammenspiel der Prinzipien

SOLID-Prinzipien wirken selten isoliert. Häufig treten sie gemeinsam auf.

| Symptom | Betroffene Prinzipien | Beispiel |
|---|---|---|
| Service mit zehn Abhängigkeiten | SRP, DIP | Zu viele Verantwortlichkeiten und externe Details |
| Neue Variante braucht Änderung alter Methode | OCP | Rabatt-, Versand- oder Payment-Logik |
| Subklasse wirft `UnsupportedOperationException` | LSP, ISP | Interface zu breit oder Vererbung falsch |
| Service hängt von Spring-Data-Repository mit allen Methoden ab | ISP, DIP | Fachlicher Use Case sieht technische Details |
| Interface pro Klasse ohne zweiten Implementierer | DIP falsch angewendet | Spekulative Abstraktion |

---

## 12. SOLID und Domain Design

SOLID unterstützt gutes Domain Design, ersetzt es aber nicht. Ein reiches Domänenmodell profitiert von SRP, weil Invarianten dort leben, wo der Zustand verwaltet wird.

Schlecht:

```java
@Service
public class OrderService {

    public void cancel(Order order) {
        if (order.status() == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(order.id());
        }
        order.setStatus(OrderStatus.CANCELLED);
    }
}
```

Besser:

```java
public class Order {

    private OrderStatus status;

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id());
        }
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

Der Service koordiniert. Das Objekt schützt seine Invarianten.

---

## 13. SOLID und Testing

Gute Testbarkeit ist ein starker Indikator für gutes Design. Wenn ein Unit Test nur mit vielen Mocks, Spring Context, Testdatenbank und komplexem Setup möglich ist, ist häufig ein SOLID-Problem sichtbar.

| Testproblem | Wahrscheinliches Designproblem |
|---|---|
| Mehr als fünf Mocks für eine Service-Methode | SRP verletzt |
| Test prüft viele Implementierungsdetails | OCP/DIP schwach |
| Subtypen brauchen Spezialtests für Basistyp-Verhalten | LSP verletzt |
| Mock muss Methoden stubben, die der Test nicht braucht | ISP verletzt |
| Test braucht echte Infrastruktur für einfache Logik | DIP verletzt |

---

## 14. SOLID und Security

SOLID ist kein Security-Framework. Trotzdem hat schlechtes Design direkte Sicherheitsfolgen.

### 14.1 SRP: Autorisierung muss sichtbar sein

Wenn Autorisierung in großen Services versteckt ist, werden Pfade leicht vergessen.

```java
authorization.requireCanAccessTenant(actor, tenantId);
```

Diese Prüfung muss früh, sichtbar und testbar sein.

### 14.2 OCP: Sicherheitsregeln erweiterbar machen

Neue Rollen, Pläne oder Mandantenregeln dürfen nicht in verstreuten `if`-Blöcken wachsen.

```java
public interface AccessPolicy {
    boolean appliesTo(AccessRequest request);
    AccessDecision decide(AccessRequest request);
}
```

### 14.3 LSP: Subtypen dürfen Sicherheitsversprechen nicht schwächen

Wenn ein Interface sagt `requiresAuthorization()`, darf ein Subtyp diese Logik nicht umgehen.

### 14.4 ISP: Schreibrechte nicht unnötig exponieren

Ein Lese-Service sollte keine Schreibschnittstelle injiziert bekommen.

### 14.5 DIP: Security-Adapter austauschbar halten

Fachliche Use Cases sollten nicht direkt von einem konkreten JWT-, OAuth- oder IAM-Client abhängen, sondern von einer fachlichen Abstraktion wie `CurrentUserProvider` oder `AuthorizationService`.

---

## 15. SOLID und SaaS-Plattformen

In SaaS-Systemen sind SOLID-Verletzungen oft teuer, weil eine Änderung nicht nur einen Nutzer, sondern Mandanten, Tarife, Rollen, Regionen und Integrationen betrifft.

Typische SaaS-Anwendungsfälle:

| SaaS-Thema | SOLID-Anwendung |
|---|---|
| Mandantentrennung | SRP für Tenant-Kontext, ISP für tenantgebundene Repository-Ports |
| Entitlements | OCP über Policy- oder Rule-Objekte |
| Feature Flags | SRP für Flag-Routing, OCP für Varianten |
| Integrationen | DIP über Gateway-Ports |
| Rollen und Rechte | OCP und ISP über Access Policies |
| Auditing | SRP: Audit nicht mit beliebigem Logging vermischen |
| DSGVO | SRP für Löschung, Anonymisierung, Pseudonymisierung |

---

# Teil C — Anti-Patterns

---

## 16. SOLID-Theater

SOLID-Theater entsteht, wenn Code äußerlich nach Architektur aussieht, aber keine echte Komplexität reduziert.

```java
public interface UserNameFormatter {
    String format(String firstName, String lastName);
}

@Component
public class DefaultUserNameFormatter implements UserNameFormatter {
    @Override
    public String format(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
```

Wenn es keine echte Variante, keine externe Grenze und keinen Testnutzen gibt, ist das vermutlich unnötig.

Besser:

```java
public final class UserName {

    private final String value;

    private UserName(String value) {
        this.value = value;
    }

    public static UserName of(String firstName, String lastName) {
        return new UserName((firstName + " " + lastName).trim());
    }

    public String value() {
        return value;
    }
}
```

Oder sogar einfach:

```java
String displayName = "%s %s".formatted(firstName, lastName).trim();
```

Die einfachste Lösung ist korrekt, solange sie die reale Komplexität nicht versteckt.

---

## 17. Interface für jede Klasse

Ein Interface ist kein Qualitätsnachweis. Es ist eine Abstraktion. Eine schlechte Abstraktion ist schlechter als keine Abstraktion.

Verwende ein Interface, wenn mindestens einer dieser Gründe gilt:

- Es gibt mehrere Implementierungen.
- Es gibt eine externe technische Grenze.
- Es gibt ein Test-Double mit echtem Nutzen.
- Das fachliche Modul soll von Infrastruktur getrennt werden.
- Die Abstraktion bildet einen stabilen Port.
- Varianten werden über Dependency Injection ausgewählt.

Vermeide ein Interface, wenn:

- Es nur eine triviale Implementierung gibt.
- Das Interface exakt dieselben Methoden wie die Klasse hat.
- Der Name nur ein `I` oder `Impl`-Paar erzeugt.
- Die Abstraktion nur „für später“ existiert.

---

## 18. Vererbung für Code-Wiederverwendung

Schlecht:

```java
public abstract class BaseService {

    protected void logStart(String operation) {
        // ...
    }

    protected void logEnd(String operation) {
        // ...
    }

    protected void validateTenant(TenantId tenantId) {
        // ...
    }
}
```

```java
public class OrderService extends BaseService {
}
```

Problem: Jede Subklasse hängt an internem Verhalten der Basisklasse. Änderungen in `BaseService` können viele Services brechen.

Besser:

```java
@Component
public class TenantValidator {
    public void requireValid(TenantId tenantId) {
        // ...
    }
}
```

```java
@Service
public class OrderService {

    private final TenantValidator tenantValidator;

    public OrderService(TenantValidator tenantValidator) {
        this.tenantValidator = tenantValidator;
    }
}
```

Komposition ist sichtbarer, testbarer und weniger fragil.

---

## 19. Zu frühe Strategie-Pattern

Schlecht:

```java
public interface GreetingStrategy {
    String greet(String name);
}

@Component
public class DefaultGreetingStrategy implements GreetingStrategy {
    public String greet(String name) {
        return "Hallo " + name;
    }
}
```

Wenn es nur genau eine Begrüßung gibt, ist das überzogen.

Besser:

```java
public String greet(String name) {
    return "Hallo " + name;
}
```

Wenn später echte Varianten entstehen, kann refaktoriert werden.

---

## 20. God Service mit SOLID-Vokabular

Eine Klasse wird nicht besser, weil sie nach außen ein Interface hat.

```java
public interface UserManagementUseCase {
    void register(...);
    void login(...);
    void logout(...);
    void updateProfile(...);
    void deleteUser(...);
    void exportCsv(...);
    void anonymize(...);
}
```

Das Interface kaschiert nur, dass die Verantwortung zu groß ist.

---

# Teil D — Review, Automatisierung und Migration

---

## 21. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| SRP | Hat die Klasse genau eine klar benennbare Verantwortung? | `UserRegistrationService` | Muss erfüllt sein |
| SRP | Braucht die Beschreibung der Klasse ein „und“? | Registrierung und Reporting | Aufteilen prüfen |
| OCP | Gibt es eine erkennbare Variantenachse? | Rabatte, Payment Provider | Strategie/Policy prüfen |
| OCP | Ist eine Abstraktion nur spekulativ? | Interface ohne zweite Implementierung | Entfernen prüfen |
| LSP | Sind Subtypen wirklich ersetzbar? | `Square extends Rectangle` | Vermeiden |
| LSP | Wirft ein Subtyp `UnsupportedOperationException` für normale Nutzung? | deaktivierter Exporter | Design prüfen |
| ISP | Hängt ein Service von Methoden ab, die er nicht braucht? | fettes Repository | Interface schneiden |
| DIP | Instanziiert ein Service technische Klassen selbst? | `new SmtpClient()` | Konstruktorinjektion |
| DIP | Gibt es Interfaces ohne echten Grund? | `FooService` / `FooServiceImpl` | Vereinfachen |
| Testing | Ist der Testaufwand ungewöhnlich hoch? | viele Mocks | Design prüfen |
| Security | Sind Autorisierung und Tenant-Kontext sichtbar? | `requireCanAccessTenant` | Pflicht |
| SaaS | Sind Mandantenvarianten verstreut? | `if tenant-a` | Policy/Entitlement prüfen |

---

## 22. Automatisierbare Prüfungen

SOLID kann nicht vollständig automatisiert werden. Einige Symptome lassen sich aber prüfen.

### 22.1 Keine Feldinjektion

```java
@ArchTest
static final ArchRule no_field_injection =
        noFields()
                .should().beAnnotatedWith(Autowired.class)
                .because("Abhängigkeiten müssen über Konstruktoren sichtbar gemacht werden.");
```

### 22.2 Controller dürfen nicht direkt auf Repositories zugreifen

```java
@ArchTest
static final ArchRule controller_should_not_access_repositories =
        noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                .because("Controller koordinieren HTTP, fachliche Logik liegt in Services.");
```

### 22.3 Keine `Utils`-Friedhöfe

```java
@ArchTest
static final ArchRule no_utils_classes =
        noClasses()
                .should().haveSimpleNameEndingWith("Utils")
                .orShould().haveSimpleNameEndingWith("Helper")
                .because("Utils/Helper-Klassen sind häufig ein Zeichen unklarer Verantwortung.");
```

Diese Regel braucht Ausnahmen, etwa für technische Test-Fixtures oder sehr kleine, stabile Funktionen. Sie darf nicht blind angewendet werden.

### 22.4 Services nicht zu groß werden lassen

Tools wie SonarQube, PMD oder Checkstyle können Symptome melden:

```text
- zu viele Methoden
- zu viele Parameter
- zu hohe zyklomatische Komplexität
- zu viele Abhängigkeiten
- zu lange Klassen
- zu tiefe Vererbung
```

Diese Metriken sind Hinweise, keine automatischen Urteile.

---

## 23. Migration bestehender Codebasen

Bestehender Code wird nicht in einem großen Umbau „SOLID gemacht“. Migration erfolgt schrittweise entlang realer Änderungsstellen.

### 23.1 Vorgehen

1. Änderungsbereich identifizieren.
2. Bestehendes Verhalten mit Tests absichern.
3. Verantwortlichkeiten sichtbar machen.
4. Große Klasse entlang Use Cases schneiden.
5. Externe Abhängigkeiten als Ports herausziehen.
6. Vererbung durch Komposition ersetzen, wenn Verhalten nicht substituierbar ist.
7. Interfaces entfernen, wenn sie keinen Zweck haben.
8. Review-Checkliste anwenden.
9. Architekturregel ergänzen, wenn das Problem wiederkehrend ist.

### 23.2 Beispiel: God Service schrittweise schneiden

Vorher:

```java
public class UserService {
    public void register(...) {}
    public void login(...) {}
    public void sendMail(...) {}
    public void exportCsv(...) {}
    public void anonymize(...) {}
}
```

Nachher:

```java
public class UserRegistrationService {}
public class UserAuthenticationService {}
public class UserNotificationService {}
public class UserExportService {}
public class UserAnonymizationService {}
```

Wichtig: Nicht mechanisch schneiden. Der Schnitt muss fachliche Gründe haben.

---

## 24. Ausnahmen

Abweichungen von SOLID-Prinzipien sind zulässig, wenn die einfachere Lösung klarer, stabiler und angemessener ist.

Zulässige Ausnahmen:

- Kleine, private Hilfsmethoden innerhalb einer Klasse.
- Einfache CRUD-Flüsse ohne erkennbare Variantenachse.
- Technische Adapter mit stabiler, kleiner Verantwortung.
- Einfache Mapper ohne externe Grenze.
- Klassen mit bewusstem Framework-Zuschnitt.
- Prototypen, sofern sie nicht ungeprüft in produktiven Code übergehen.

Nicht zulässig:

- God Services in produktiver Fachlogik.
- Direkte technische Instanziierung in zentralen Use Cases.
- Vererbung nur zur Code-Wiederverwendung.
- Interfaces ohne Zweck in großem Stil.
- Versteckte Autorisierung in Nebenpfaden.
- Mandantenlogik über verstreute `if`-Kaskaden.

---

## 25. Entscheidungshilfe

| Situation | Empfehlung |
|---|---|
| Eine Klasse hat viele fachlich unabhängige Methoden | SRP prüfen, Klasse schneiden |
| Neue Varianten werden regelmäßig ergänzt | OCP über Strategie, Policy oder Konfiguration |
| Varianten sind fachlich geschlossen | `sealed interface` und exhaustiver `switch` |
| Subtyp muss Methoden deaktivieren | LSP/ISP prüfen, Interface schneiden |
| Client braucht nur Lesen, bekommt aber Schreiben | ISP anwenden |
| Service erzeugt `new SmtpClient()` | DIP anwenden |
| Interface existiert nur für eine Klasse | Zweck prüfen, eventuell entfernen |
| Test braucht viele Mocks | SRP/DIP prüfen |
| Code wird durch SOLID deutlich komplizierter | YAGNI prüfen |

---

## 26. Definition of Done

Eine Java-Klasse erfüllt diese Richtlinie, wenn ihre Verantwortung klar benennbar ist, ihre Abhängigkeiten per Konstruktor sichtbar sind, sie keine technischen Details ohne Grund selbst instanziiert, ihre Schnittstellen nur benötigtes Verhalten exponieren, ihre Subtypen substituierbar sind, erkennbare Variantenachsen angemessen modelliert sind, Security- und Tenant-Regeln sichtbar und testbar bleiben und keine spekulativen Abstraktionen ohne realen Nutzen eingeführt wurden.

Für Pull Requests gilt: Eine Änderung ist SOLID-konform, wenn sie die Verständlichkeit, Änderbarkeit oder Testbarkeit verbessert, ohne unnötige Klassen-, Interface- oder Pattern-Komplexität einzuführen.

---

## 27. Quellen und weiterführende Literatur

| Quelle | Relevanz |
|---|---|
| Robert C. Martin — Design Principles and Design Patterns / SOLID-Prinzipien | Ursprung und klassische Einordnung der Prinzipien |
| Barbara Liskov — Data Abstraction and Hierarchy | Grundlage für Subtyping und Liskov Substitution |
| Barbara Liskov, Jeannette Wing — Behavioral Subtyping | Präzisierung substituierbaren Verhaltens |
| Martin Fowler — YAGNI | Gegenpol zu spekulativer Abstraktion |
| Spring Framework Documentation — Dependency Injection | Technische Umsetzung von Dependency Injection in Spring |
| Joshua Bloch — Effective Java | Praktische Java-Designregeln zu Klassen, Interfaces und Komposition |
| Eric Evans — Domain-Driven Design | Fachliche Modellierung, Entities, Value Objects und Services |
| Robert C. Martin — Clean Architecture | Abhängigkeiten, Grenzen, Use Cases und Dependency Rule |
| JUnit 5, Mockito, AssertJ, ArchUnit Dokumentation | Prüfbarkeit von Designentscheidungen im Test- und Review-Kontext |

---

## 28. Merksatz

SOLID ist kein Ziel an sich. SOLID ist ein Werkzeug, um Code so zu schneiden, dass echte Änderungen weniger gefährlich werden. Gute Objektorientierung entsteht nicht durch viele Klassen, sondern durch klare Verantwortung, sichtbare Grenzen, ersetzbare Abhängigkeiten und fachlich verständliches Verhalten.
