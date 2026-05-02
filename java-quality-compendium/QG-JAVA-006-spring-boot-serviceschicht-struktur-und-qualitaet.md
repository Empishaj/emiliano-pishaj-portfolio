# QG-JAVA-006 — Spring-Boot-Serviceschicht: Struktur und Qualität

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-006 |
| Titel | Spring-Boot-Serviceschicht: Struktur und Qualität |
| Status | Accepted / verbindlicher Standard für neue und wesentlich geänderte Spring-Boot-Services |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, Spring Boot 3.2+, Spring Framework 6.x, SaaS-Plattformen, REST-/API-Backends, transaktionale Geschäftslogik |
| Java-Baseline | Java 21+ als Kompendium-Standard |
| Spring-Baseline | Spring Boot 3.2+ / Spring Framework 6.x als Mindestkontext; neuere Versionen sind zulässig, sofern die hier beschriebenen Architektur- und Transaktionsregeln weiterhin erfüllt werden |
| Kategorie | Spring Boot / Architektur / Service Design / Transaktionsgrenzen |
| Letzte Validierung | 2026-05-02 |
| Validierte Quellenbasis | Spring Framework Reference Documentation zu Dependency Injection, Transaktionen, Validation, Events und Error Responses; Spring Boot Reference Documentation zu Task Execution und Virtual Threads; Spring Data JPA Reference Documentation zu Projections; OWASP API Security Top 10 2023; OWASP Logging Cheat Sheet; RFC 9457 Problem Details for HTTP APIs |
| Technische Beispielvalidierung | Die reinen Java-21-Beispiele ohne Spring-Abhängigkeiten wurden mit `javac --release 21` syntaktisch geprüft. Spring-spezifische Beispiele sind referenzbasiert gegen die offizielle Spring-Dokumentation validiert und benötigen ein Spring-Boot-Projekt mit den entsprechenden Dependencies. |
| Verbindlichkeit | Diese Richtlinie gilt verbindlich für neue Services und für wesentlich geänderte bestehende Services. Abweichungen sind zulässig, wenn ein konkreter fachlicher, technischer oder architektonischer Grund im Pull Request nachvollziehbar dokumentiert wird. |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Services in Spring-Boot-Anwendungen strukturiert werden sollen, damit Geschäftslogik wartbar, testbar, sicher und transaktional nachvollziehbar bleibt.

Die Serviceschicht ist in vielen Spring-Boot-Systemen der Ort, an dem Use Cases koordiniert werden: Eingaben werden geprüft, fachliche Entscheidungen getroffen, Repositories aufgerufen, Transaktionen gesteuert, Domain-Objekte verändert und Events ausgelöst. Genau deshalb wird die Serviceschicht schnell zum Qualitätsrisiko, wenn sie unstrukturiert wächst.

Typische Fehler sind:

1. Services werden zu großen Alleskönnern.
2. Controller greifen direkt auf Repositories zu.
3. Entities werden als API-Response zurückgegeben.
4. Request-DTOs werden direkt als Persistenzmodell verwendet.
5. Transaktionen werden pauschal auf Klassenebene gesetzt.
6. externe Calls werden innerhalb langer Datenbanktransaktionen ausgeführt.
7. Events werden vor Commit ausgelöst und erzeugen inkonsistente Nebeneffekte.
8. `RuntimeException` wird als generischer Fehlercontainer missbraucht.
9. Tenant- und Berechtigungskontext werden nicht in der Serviceschicht abgesichert.
10. Unit-Tests benötigen unnötig den gesamten Spring-Kontext.

Ziel dieser Richtlinie ist nicht, Services künstlich klein oder akademisch rein zu machen. Ziel ist, dass jeder Service klar erkennbar beantwortet:

- Welchen Use Case führt er aus?
- Welche Eingabe akzeptiert er?
- Welche fachlichen Regeln prüft er?
- Welche Transaktionsgrenze gilt?
- Welche Daten verlassen die Schicht?
- Welche Nebeneffekte entstehen?
- Welche Fehler können auftreten?
- Welche Security- und Tenant-Regeln werden durchgesetzt?
- Wie kann der Service isoliert getestet werden?

Ein guter Spring-Boot-Service ist kein Sammelbecken für alles, was irgendwo hinmuss. Er ist eine klar geschnittene Anwendungskomponente, die fachliches Verhalten koordiniert, technische Details begrenzt und Systemgrenzen bewusst behandelt.

---

## 2. Kurzregel für Entwickler

Ein Spring-Boot-Service soll genau einen fachlich zusammenhängenden Use-Case-Bereich koordinieren, keine HTTP-Typen exponieren, keine Entities an API-Grenzen zurückgeben, Transaktionen bewusst auf öffentlichen Service-Methoden setzen und fachliche Fehler über sprechende Exception-Typen ausdrücken.

Controller dürfen Requests entgegennehmen und Responses ausliefern. Repositories dürfen Daten laden und speichern. Services verbinden beides durch fachliche Regeln, Transaktionsgrenzen, Autorisierung, Mapping und Use-Case-Orchestrierung.

Vermeide God Services. Vermeide generische `RuntimeException`. Vermeide direkte Entity-Exposition. Vermeide externe Calls innerhalb offener Datenbanktransaktionen. Vermeide `@Transactional` auf privaten Methoden. Vermeide Self-Invocation bei transaktionalen Methoden.

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

Ein Spring-Boot-Service MUSS folgende Regeln erfüllen:

1. Er MUSS einen klaren fachlichen Verantwortungsbereich haben.
2. Er MUSS seine Abhängigkeiten per Konstruktor erhalten.
3. Er MUSS fachliche Eingaben über Command-, Query- oder Request-DTOs entgegennehmen, wenn mehrere Eingabewerte fachlich zusammengehören.
4. Er DARF keine JPA-Entities direkt an Controller oder externe API-Grenzen zurückgeben.
5. Er MUSS API-Response-Daten über dedizierte Response-DTOs oder Projection-DTOs ausgeben.
6. Er MUSS Transaktionen bewusst auf den Service-Methoden setzen, die tatsächlich Datenbankarbeit koordinieren.
7. Schreibende Use Cases mit mehreren persistenzrelevanten Schritten MÜSSEN eine klare `@Transactional`-Grenze haben.
8. Lesende Use Cases SOLLTEN `@Transactional(readOnly = true)` verwenden, wenn Lazy Loading, konsistente Leseeinheiten oder Provider-Optimierungen relevant sind.
9. Fachliche Fehler MÜSSEN über sprechende Exception-Typen oder klar modellierte Result-Typen ausdrückbar sein.
10. Generische `RuntimeException`, `Exception` oder `IllegalStateException` DÜRFEN nicht als fachlicher Standardfehler verwendet werden.
11. Autorisierung und Tenant-Isolation MÜSSEN in oder unterhalb der Serviceschicht abgesichert werden, nicht ausschließlich im Controller.
12. Externe Nebeneffekte wie E-Mail, Billing, Webhooks oder Notifications DÜRFEN nicht unbedacht innerhalb einer offenen Datenbanktransaktion ausgeführt werden.
13. Kritische externe Nebeneffekte MÜSSEN robust über ein geeignetes Muster abgesichert werden, zum Beispiel Transactional Outbox, idempotente Jobs oder zuverlässig verarbeitete Events.
14. `@Transactional` DARF NICHT auf private Methoden gesetzt werden, weil proxybasierte Transaktionslogik dort nicht wie erwartet greift.
15. Transaktionale Methoden DÜRFEN NICHT über Self-Invocation innerhalb derselben Klasse aktiviert werden, wenn dadurch der Spring-Proxy umgangen wird.
16. Services MÜSSEN isoliert unit-testbar sein, sofern ihre Logik nicht bewusst Integrationsverhalten testet.
17. Logging in Services MUSS sensible Daten, Secrets, Tokens, Passwörter, personenbezogene Daten und Tenant-Grenzen beachten.
18. Methoden, die potenziell untrusted input verarbeiten, MÜSSEN validieren oder nur bereits validierte Command-Objekte akzeptieren.

### 3.2 Darf-nicht-Regeln

Ein Spring-Boot-Service DARF NICHT:

1. HTTP-spezifische Typen wie `HttpServletRequest`, `ResponseEntity`, `HttpStatus`, `MultipartFile` oder `Principal` ohne klare Ausnahme tief in die Geschäftslogik tragen.
2. Controller-Annotationen oder Web-Mapping-Logik enthalten.
3. direkt View-, Wicket- oder REST-spezifische Response-Formate als interne Geschäftsobjekte verwenden.
4. Entities als öffentliche API-Verträge zurückgeben.
5. Request-DTOs direkt als Entities speichern.
6. alle Abhängigkeiten einer fachlichen Domäne in einem einzigen God Service sammeln.
7. Feldinjektion mit `@Autowired` verwenden.
8. Transaktionen pauschal und unreflektiert auf Klassenebene setzen.
9. externe HTTP-Calls, E-Mail-Versand oder Billing-Calls ohne Timeout, Retry-Konzept und Fehlerstrategie ausführen.
10. `@Async` als Ersatz für ein klares Konsistenz- und Fehlerkonzept verwenden.
11. Virtual Threads als Freifahrtschein für unbegrenzte Parallelisierung behandeln.
12. Tenant-IDs aus Request-DTOs ungeprüft vertrauen.
13. technische Exceptions ungefiltert bis zur API-Grenze durchreichen.
14. Datenbank-Queries über Lazy Loading in Stream-, Mapper- oder Serialization-Schritten unkontrolliert auslösen.
15. Security-relevante Entscheidungen allein in Frontend, Controller oder Client verlagern.

### 3.3 Sollte-Regeln

Ein Spring-Boot-Service SOLLTE:

1. nach Use Case oder fachlicher Fähigkeit benannt werden, nicht nach einer technischen Tabelle.
2. kleine öffentliche Methoden mit klaren Command-/Query-Objekten anbieten.
3. Mapping zwischen Entity und DTO explizit oder über klar kontrollierte Mapper durchführen.
4. Events nur für echte Entkopplung verwenden, nicht um Kontrollfluss zu verstecken.
5. `@TransactionalEventListener` verwenden, wenn ein Event erst nach erfolgreichem Commit verarbeitet werden soll.
6. für kritische Integrationsevents ein Outbox-Muster statt reiner In-Memory-Events verwenden.
7. read-only Queries über DTO-Projections oder gezielte Fetch-Strategien modellieren, statt vollständige Aggregate unnötig zu laden.
8. Nebeneffekte idempotent gestalten.
9. keine übermäßig breite Service-Schnittstelle anbieten.
10. technische Framework-Abhängigkeiten am Rand halten.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Spring-Boot-Anwendungen, die Geschäftslogik in Serviceschichten abbilden. Sie gilt insbesondere für:

1. REST-Backends.
2. SaaS-Plattformen.
3. interne Service-APIs.
4. modulare Monolithen.
5. klassische mehrschichtige Spring-Boot-Anwendungen.
6. Anwendungen mit Spring MVC, Spring Data JPA, Bean Validation und transaktionaler Persistenz.
7. Anwendungen, die Events, asynchrone Verarbeitung oder externe Integrationen nutzen.
8. Anwendungen mit Mandantenfähigkeit.
9. Anwendungen mit auditierbaren Geschäftsprozessen.
10. Anwendungen, in denen Services fachliche Use Cases koordinieren.

Diese Richtlinie gilt nicht automatisch für:

1. reine technische Adapter ohne fachliche Logik.
2. reine Batch-Worker, sofern sie eine eigene Architekturregel besitzen.
3. kleine Prototypen ohne Produktionsanspruch.
4. Framework-Konfiguration.
5. reine Repository-Implementierungen.
6. reine Mapper-Klassen.
7. Event-Consumer, die bewusst als eigener Application Service geschnitten sind.
8. sehr einfache CRUD-Backoffice-Werkzeuge, sofern Security-, Tenant- und API-Regeln trotzdem erfüllt bleiben.

---

## 5. Begriffe

| Aspekt | Details/Erklärung | Beispiel |
|---|---|---|
| Service | Spring-Komponente, die einen fachlichen Use Case oder eine fachliche Fähigkeit koordiniert. | `UserRegistrationService`, `OrderCheckoutService` |
| Application Service | Service, der Eingaben entgegennimmt, Transaktionen steuert, Repositories nutzt und Domain-Operationen koordiniert. | `register(command)`, `createOrder(command)` |
| Domain Service | Fachlicher Service ohne direkten Framework-Fokus, wenn Logik nicht sinnvoll in Entity oder Value Object gehört. | `TaxCalculator`, `DiscountPolicy` |
| Repository | Abstraktion für Datenzugriff. | `UserRepository`, `OrderRepository` |
| Entity | Persistentes Objekt mit Identität und Lifecycle. | `UserEntity`, `OrderEntity` |
| DTO | Datentransferobjekt für Schicht- oder API-Grenzen. | `UserResponse`, `RegisterUserCommand` |
| Command | Eingabeobjekt für einen schreibenden Use Case. | `RegisterUserCommand` |
| Query | Eingabeobjekt oder Methode für einen lesenden Use Case. | `FindOrdersQuery` |
| Response DTO | Ausgabeobjekt für API oder Service-Grenze. | `UserCreatedResponse` |
| Projection | gezielte Auswahl von Daten für lesende Zwecke. | `OrderSummaryView` |
| Transaktionsgrenze | Bereich, in dem Datenbankoperationen atomar und konsistent ausgeführt werden sollen. | `@Transactional` auf `createOrder(...)` |
| Nebeneffekt | Aktion außerhalb der unmittelbaren Datenänderung. | E-Mail, Billing, Webhook, Notification |
| Tenant-Kontext | Informationen darüber, für welchen Mandanten eine Operation ausgeführt wird. | `TenantId`, Organisation, Account |
| Self-Invocation | Aufruf einer annotierten Methode aus derselben Klasse, wodurch Spring-Proxies umgangen werden können. | `this.saveInternal()` |
| Outbox | Muster, bei dem Integrationsevents transaktional in der Datenbank gespeichert und später zuverlässig ausgeliefert werden. | `outbox_event`-Tabelle |

---

## 6. Technischer Hintergrund

Spring-Boot-Anwendungen sind häufig schichtenorientiert aufgebaut: Controller nehmen Requests entgegen, Services koordinieren Use Cases, Repositories greifen auf Daten zu, Entities bilden Persistenzzustand ab, DTOs definieren Grenzen nach außen. Diese Struktur ist nicht deshalb sinnvoll, weil Schichten hübsch aussehen, sondern weil sie Verantwortlichkeiten trennen und Fehler lokal begrenzen.

Spring unterstützt Dependency Injection, indem Objekte ihre Abhängigkeiten über Konstruktoren, Factory-Methoden oder Properties definieren und der Container diese Abhängigkeiten beim Erzeugen der Beans bereitstellt. Für Services ist Konstruktorinjektion der bevorzugte Standard, weil Abhängigkeiten explizit, testbar und unveränderlich werden.

Spring-Transaktionen funktionieren in typischen Spring-Boot-Anwendungen proxybasiert. Das bedeutet: Die Transaktionslogik wird angewendet, wenn ein Aufruf über den Spring-Proxy läuft. Aufrufe innerhalb derselben Klasse umgehen diesen Proxy häufig. Deshalb sind Self-Invocation und `@Transactional` auf privaten Methoden klassische Fehlerquellen.

Spring dokumentiert außerdem, dass transaktionale Methoden bei proxybasierter Nutzung typischerweise über geeignete Sichtbarkeit aufgerufen werden müssen und dass nur externe Aufrufe über den Proxy interceptet werden. Seit Spring Framework 6.0 können bei class-based proxies auch protected oder package-visible Methoden transaktional sein; für klare Service-Standards bleiben öffentliche Service-Methoden trotzdem die robusteste und verständlichste Regel.

Für REST-APIs stellt Spring mit `ProblemDetail` und `ErrorResponse` Mechanismen bereit, um Fehlerantworten nach RFC 9457 auszugeben. Fachliche Exceptions sollen deshalb nicht beliebig bis zum Client durchfallen, sondern über `@ControllerAdvice` oder vergleichbare Fehler-Mapping-Mechanismen in stabile API-Fehler übersetzt werden.

---

## 7. Schichtenmodell und Abhängigkeitsregeln

### 7.1 Grundmodell

Die Standardstruktur lautet:

```text
Controller / API Adapter
        ↓
Application Service
        ↓
Domain / Policies / Entities / Value Objects
        ↓
Repository / Persistence Adapter
```

Das ist kein Dogma. Es ist eine Qualitätsregel gegen Kopplung, unkontrollierten Datenzugriff und zufällige Architektur.

### 7.2 Erlaubte und verbotene Abhängigkeiten

| Schicht | Darf abhängen von | Darf nicht abhängen von | Begründung |
|---|---|---|---|
| Controller | Service, Request-DTO, Response-DTO, Mapper am Rand | Repository, Entity als API-Vertrag, Datenbankdetails | Controller sollen HTTP übersetzen, nicht Geschäftslogik oder Persistenz steuern. |
| Service | Repository, Domain, Mapper, Policies, Event Publisher, Security-Kontext | Controller, `ResponseEntity`, `HttpServletRequest`, UI-Typen | Services sollen Use Cases koordinieren und nicht vom Web-Protokoll abhängig sein. |
| Domain | Value Objects, Domain Policies, fachliche Typen | Spring MVC, Controller, Repository, HTTP, technische Infrastruktur | Domain-Logik soll möglichst frameworkarm bleiben. |
| Repository | Entity, Projection, Datenzugriffsmechanismen | Controller, Service-Orchestrierung, HTTP | Repositories laden und speichern Daten, sie steuern keine Use Cases. |
| Entity | JPA-Annotationen, Value Objects, fachliche Methoden | Controller, Response-DTOs, externe API-Typen | Entities dürfen Persistenzmodell sein, aber nicht API-Vertrag. |
| Event Handler | Event, benötigter Port/Service, Outbox/Integration | zufälliger Zugriff auf alles | Handler sollen Nebeneffekte gezielt und testbar behandeln. |

### 7.3 Paketstruktur

Eine sinnvolle Paketstruktur kann so aussehen:

```text
com.example.user
├── api
│   ├── UserController.java
│   ├── RegisterUserRequest.java
│   └── UserResponse.java
├── application
│   ├── UserRegistrationService.java
│   ├── RegisterUserCommand.java
│   ├── UserCreatedResponse.java
│   └── UserRegisteredEvent.java
├── domain
│   ├── EmailAddress.java
│   ├── PasswordPolicy.java
│   └── UserStatus.java
├── persistence
│   ├── UserEntity.java
│   └── UserRepository.java
└── integration
    ├── WelcomeEmailHandler.java
    └── BillingTrialHandler.java
```

Diese Struktur trennt API-Vertrag, Use-Case-Logik, fachliche Typen, Persistenz und Integrationen. Dadurch werden Reviews einfacher, Abhängigkeiten sichtbarer und Tests gezielter.

---

## 8. Schlechtes Beispiel: God Service

```java
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final BillingService billingService;
    private final NotificationService notificationService;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void registerUser(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("bad name");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        emailService.sendWelcomeEmail(email, name);
        billingService.createFreeTrialSubscription(user.getId());
        notificationService.notifyAdmins("New user: " + email);
    }

    public List<User> getAllUsersWithOrders() {
        return userRepository.findAll().stream()
                .peek(user -> user.getOrders().size())
                .toList();
    }
}
```

### Warum ist das schlecht?

1. `@Transactional` auf Klassenebene versteckt, welche Methoden wirklich transaktional sein müssen.
2. `getUser(...)` gibt eine Entity zurück und koppelt API oder Aufrufer an das Persistenzmodell.
3. `RuntimeException` ist kein fachlich unterscheidbarer Fehler.
4. Registrierung, Passwortlogik, E-Mail, Billing und Admin-Notification liegen in einer Methode.
5. externe Nebeneffekte laufen innerhalb derselben Service-Methode und potenziell innerhalb derselben Transaktion.
6. Wenn E-Mail oder Billing fehlschlagen, ist unklar, ob die Benutzeranlage zurückgerollt werden soll.
7. Wenn die Transaktion zurückrollt, könnte eine E-Mail bereits versendet worden sein.
8. `getAllUsersWithOrders()` lädt möglicherweise unnötig viele Entities und provoziert N+1-Queries.
9. Die Methode ist schwer isoliert zu testen, weil zu viele Verantwortlichkeiten vermischt sind.
10. Es ist unklar, wo Autorisierung, Tenant-Grenze und Datenschutz geprüft werden.

---

## 9. Gute Anwendung: fokussierte Serviceschicht

### 9.1 Command und Response trennen

```java
public record RegisterUserCommand(
        String tenantId,
        String name,
        String email,
        String password
) {}

public record UserCreatedResponse(
        Long userId,
        String name,
        String email,
        Instant registeredAt
) {}
```

Für produktiven Spring-Code sollten Command-Objekte zusätzlich Bean-Validation-Annotationen verwenden:

```java
public record RegisterUserCommand(
        @NotBlank String tenantId,
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(min = 12, max = 128) String password
) {}
```

Wichtig: Ein `tenantId` aus einem Request-DTO darf in SaaS-Systemen nicht blind vertraut werden. Der Service muss den Tenant aus dem authentifizierten Kontext ableiten oder gegen diesen validieren.

### 9.2 Sprechende Exception-Hierarchie

Der folgende Punkt ist besonders wichtig: Ein Java `record` kann nicht von `RuntimeException` erben, weil Records implizit von `java.lang.Record` erben. Deshalb sind fachliche Exceptions keine Records.

Korrekt ist zum Beispiel eine sealed Exception-Hierarchie mit normalen Klassen:

```java
public sealed abstract class DomainException extends RuntimeException
        permits UserNotFoundException, EmailAlreadyExistsException, InvalidCommandException {

    protected DomainException(String message) {
        super(message);
    }
}

public final class UserNotFoundException extends DomainException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
        this.userId = userId;
    }

    public Long userId() {
        return userId;
    }
}

public final class EmailAlreadyExistsException extends DomainException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("Email already registered");
        this.email = email;
    }

    public String email() {
        return email;
    }
}

public final class InvalidCommandException extends DomainException {

    public InvalidCommandException(String message) {
        super(message);
    }
}
```

Beachte: Die Message von `EmailAlreadyExistsException` enthält bewusst nicht die vollständige E-Mail-Adresse. Je nach Kontext kann bereits eine E-Mail-Adresse personenbezogen und log-sensitiv sein. Für interne Debugging-Zwecke können strukturierte, geschützte Felder genutzt werden; Logs müssen trotzdem kontrolliert bleiben.

### 9.3 Fokussierter Application Service

```java
@Service
@Validated
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantAccessPolicy tenantAccessPolicy;

    public UserRegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            TenantAccessPolicy tenantAccessPolicy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    @Transactional
    public UserCreatedResponse register(@Valid RegisterUserCommand command) {
        tenantAccessPolicy.assertCurrentUserMayCreateUserFor(command.tenantId());

        if (userRepository.existsByTenantIdAndEmail(command.tenantId(), command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        var user = UserEntity.register(
                command.tenantId(),
                command.name(),
                command.email(),
                passwordEncoder.encode(command.password())
        );

        var saved = userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                saved.getTenantId(),
                saved.getId(),
                saved.getEmail()
        ));

        return new UserCreatedResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getCreatedAt()
        );
    }
}
```

Warum ist das besser?

1. Der Service hat einen klaren Use Case: Registrierung.
2. Abhängigkeiten sind explizit und testbar.
3. Eingabe und Ausgabe sind dedizierte DTOs.
4. Die Transaktionsgrenze liegt auf der öffentlichen Use-Case-Methode.
5. Tenant-Autorisierung wird in der Serviceschicht geprüft.
6. E-Mail und Billing werden nicht direkt in der Kernmethode ausgeführt.
7. Die Methode gibt keine Entity zurück.
8. Fachliche Fehler sind unterscheidbar.
9. Unit-Tests können Repository, Encoder, Event Publisher und Policy mocken.

---

## 10. Event-Verarbeitung und Nebeneffekte

### 10.1 Normale Events sind keine robuste Integration

Spring Application Events sind in-process Events. Sie sind hilfreich zur Entkopplung innerhalb einer Anwendung. Sie sind aber keine dauerhafte Queue, kein Message Broker und kein Garant für externe Zustellung.

Deshalb gilt:

1. Für einfache interne Nebeneffekte sind Spring Events geeignet.
2. Für E-Mail nach erfolgreicher Transaktion kann `@TransactionalEventListener` sinnvoll sein.
3. Für kritische externe Integrationen ist ein Outbox-Muster oder eine robuste Messaging-Lösung erforderlich.
4. Ein Event darf nicht verschleiern, dass eine fachliche Pflichtaktion zuverlässig erfolgen muss.

### 10.2 Event nach erfolgreichem Commit

```java
@Component
public class UserRegistrationEventHandler {

    private final EmailService emailService;
    private final BillingService billingService;

    public UserRegistrationEventHandler(
            EmailService emailService,
            BillingService billingService) {
        this.emailService = emailService;
        this.billingService = billingService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        emailService.sendWelcomeEmail(event.email());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createTrialSubscription(UserRegisteredEvent event) {
        billingService.createFreeTrialSubscription(event.tenantId(), event.userId());
    }
}
```

`@TransactionalEventListener` bindet Listener an den Transaktionsverlauf. Standardmäßig wird ein solcher Listener nach Commit ausgeführt. Das ist wichtig, wenn eine E-Mail erst versendet werden soll, nachdem die Datenbankänderung erfolgreich bestätigt wurde.

### 10.3 Kritische Nebeneffekte über Outbox

Für kritische Nebeneffekte reicht ein In-Memory-Event nicht. Beispiel: Wenn die Anlage eines Kunden zwingend ein Billing-System informieren muss, darf diese Information nicht verloren gehen, nur weil der Prozess nach Commit abstürzt.

In diesem Fall soll die Transaktion zusätzlich einen Outbox-Eintrag schreiben:

```java
@Transactional
public UserCreatedResponse register(@Valid RegisterUserCommand command) {
    // Benutzer validieren und speichern
    var saved = userRepository.save(user);

    outboxRepository.save(OutboxEvent.billingTrialRequested(
            saved.getTenantId(),
            saved.getId(),
            saved.getEmail()
    ));

    return mapper.toCreatedResponse(saved);
}
```

Ein separater Worker liest die Outbox, ruft das externe System auf, markiert Events als verarbeitet und behandelt Retry, Dead Letter und Idempotenz.

---

## 11. Transaktionsregeln

### 11.1 Method-Level statt pauschaler Klassenannotation

Pauschales `@Transactional` auf Klassenebene ist bequem, aber oft zu grob. Es verschleiert, welche Methoden wirklich eine Transaktion benötigen.

Bevorzugt:

```java
@Service
public class OrderService {

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> findOrderSummaries(String tenantId, Long userId) {
        return orderRepository.findSummariesByTenantIdAndUserId(tenantId, userId);
    }

    @Transactional
    public OrderCreatedResponse createOrder(CreateOrderCommand command) {
        // schreibender Use Case
    }

    public BigDecimal calculateTaxPreview(BigDecimal amount, String countryCode) {
        return taxCalculator.calculate(amount, countryCode);
    }
}
```

### 11.2 `readOnly = true` richtig verstehen

`@Transactional(readOnly = true)` ist kein Security-Mechanismus und kein Ersatz für Datenbankrechte. Es drückt die Absicht einer lesenden Transaktion aus und kann je nach Transaktionsmanager, JDBC-Treiber, JPA-Provider und Datenbank Optimierungen ermöglichen.

Richtig:

1. Für lesende Use Cases verwenden, wenn Datenbankzugriff stattfindet.
2. Nicht als Garantie verstehen, dass niemals geschrieben werden kann.
3. Nicht verwenden, wenn innerhalb der Methode persistente Änderungen erwartet werden.
4. Nicht verwenden, um fehlende fachliche Trennung zu kaschieren.

### 11.3 Keine privaten transaktionalen Methoden

Falsch:

```java
@Service
public class PaymentService {

    public void pay(PaymentCommand command) {
        validate(command);
        persistPayment(command);
    }

    @Transactional
    private void persistPayment(PaymentCommand command) {
        // wird bei proxybasierter AOP nicht so angewendet, wie viele Entwickler erwarten
    }
}
```

Richtig:

```java
@Service
public class PaymentService {

    @Transactional
    public void pay(PaymentCommand command) {
        validate(command);
        persistPayment(command);
    }

    private void persistPayment(PaymentCommand command) {
        // Teil derselben öffentlichen Transaktionsgrenze
    }
}
```

Oder bei bewusst getrennter Transaktion:

```java
@Service
public class PaymentPersistenceService {

    @Transactional
    public void persistPayment(PaymentCommand command) {
        // eigene öffentliche transaktionale Service-Methode
    }
}
```

### 11.4 Self-Invocation vermeiden

Falsch:

```java
@Service
public class InvoiceService {

    public void createInvoice(CreateInvoiceCommand command) {
        validate(command);
        this.persistInvoice(command);
    }

    @Transactional
    public void persistInvoice(CreateInvoiceCommand command) {
        // Aufruf über this umgeht typischerweise den Spring-Proxy
    }
}
```

Richtig ist ein externer Aufruf über eine andere Spring-Bean oder eine sauber gesetzte Transaktionsgrenze auf der äußeren Methode.

### 11.5 Externe Calls nicht innerhalb langer Transaktionen

Falsch:

```java
@Transactional
public void checkout(CheckoutCommand command) {
    var order = orderRepository.save(createOrder(command));
    paymentClient.charge(command.paymentDetails());
    shippingClient.createShipment(order.getId());
    order.markCompleted();
}
```

Warum kritisch?

1. Die Datenbanktransaktion bleibt während externer Netzwerkaufrufe offen.
2. Locks können länger gehalten werden.
3. Timeouts werden schwerer kontrollierbar.
4. Fehlersemantik wird unklar: Was passiert, wenn Zahlung erfolgreich, aber DB-Commit fehlschlägt?
5. Wiederholungen können doppelte externe Aktionen erzeugen.

Besser:

1. Zustand in der Datenbank speichern.
2. Outbox-Event erzeugen.
3. externe Verarbeitung asynchron und idempotent ausführen.
4. Statusübergänge explizit modellieren.

---

## 12. DTO-, Entity- und Mapping-Regeln

### 12.1 Entities nicht als API-Vertrag verwenden

Falsch:

```java
@GetMapping("/users/{id}")
public UserEntity getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

Warum ist das gefährlich?

1. Interne Datenbankstruktur wird öffentlicher Vertrag.
2. Neue Entity-Felder können versehentlich über die API sichtbar werden.
3. Lazy Loading kann während Serialisierung Datenbankzugriffe auslösen.
4. Bidirektionale Beziehungen können rekursive Serialisierung erzeugen.
5. Schreibbare Felder können zu Mass Assignment führen.
6. Berechtigungen auf Objekteigenschaften werden schwerer kontrollierbar.

Richtig:

```java
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return userQueryService.getUser(id);
}
```

```java
public record UserResponse(
        Long id,
        String name,
        String email,
        Instant registeredAt
) {}
```

### 12.2 Command und Response trennen

Falsch:

```java
public record UserDto(
        Long id,
        String name,
        String email,
        String password,
        boolean admin,
        Instant createdAt
) {}
```

Dieses DTO ist für alles und dadurch für nichts präzise. Es kann zu Overposting, ungewollter Datenexposition und unklarer Validierung führen.

Richtig:

```java
public record RegisterUserCommand(
        String name,
        String email,
        String password
) {}

public record UserResponse(
        Long id,
        String name,
        String email,
        Instant registeredAt
) {}

public record UpdateUserProfileCommand(
        String displayName,
        String preferredLanguage
) {}
```

### 12.3 Projections für lesende Use Cases nutzen

Wenn ein Use Case nur Zusammenfassungen braucht, soll nicht das vollständige Aggregat geladen werden.

```java
public record OrderSummaryDto(
        Long orderId,
        BigDecimal total,
        Instant createdAt,
        String status
) {}
```

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("""
            select new com.example.order.application.OrderSummaryDto(
                o.id,
                o.total,
                o.createdAt,
                o.status
            )
            from OrderEntity o
            where o.tenantId = :tenantId
              and o.userId = :userId
            order by o.createdAt desc
            """)
    List<OrderSummaryDto> findSummariesByTenantIdAndUserId(
            String tenantId,
            Long userId
    );
}
```

Projections reduzieren unnötige Datenlast und senken das Risiko, versehentlich sensitive Entity-Felder nach außen zu tragen.

---

## 13. Validierung und Fehlersemantik

### 13.1 Validierung am Rand und im Use Case

Request-Validierung im Controller ist notwendig, aber nicht ausreichend. Services können auch aus Jobs, Events, Tests, internen APIs oder anderen Services aufgerufen werden. Deshalb dürfen wichtige fachliche Invarianten nicht ausschließlich im Controller liegen.

Controller-Beispiel:

```java
@PostMapping("/users")
public ResponseEntity<UserCreatedResponse> register(
        @Valid @RequestBody RegisterUserRequest request) {
    var command = new RegisterUserCommand(
            currentTenantId(),
            request.name(),
            request.email(),
            request.password()
    );
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(userRegistrationService.register(command));
}
```

Service-Beispiel:

```java
@Service
@Validated
public class UserRegistrationService {

    @Transactional
    public UserCreatedResponse register(@Valid RegisterUserCommand command) {
        // zusätzlich fachliche Prüfung und Autorisierung
    }
}
```

Spring unterstützt Bean Validation in MVC und methodenbezogene Validierung über `@Validated`. Trotzdem ist Bean Validation nur ein Teil der Lösung. Fachliche Regeln wie „E-Mail im Tenant eindeutig“, „Nutzer darf diesen Tenant verwalten“ oder „Statusübergang ist erlaubt“ gehören in Service, Domain Policy oder Domain-Modell.

### 13.2 Fehler in stabile API-Antworten übersetzen

Fachliche Exceptions sollen nicht ungefiltert als Stacktrace oder generischer 500-Fehler beim Client landen.

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("User not found");
        problem.setDetail("The requested user does not exist or is not accessible.");
        problem.setProperty("userId", exception.userId());
        return problem;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Email already registered");
        problem.setDetail("A user with this email address already exists.");
        return problem;
    }
}
```

Wichtig: Fehlermeldungen sollen hilfreich, aber nicht informationspreisgebend sein. Bei Login, Registrierung, Account Recovery und sicherheitskritischen APIs muss genau geprüft werden, ob eine Antwort Benutzerexistenz, Tenant-Strukturen oder interne Regeln offenlegt.

---

## 14. Security- und SaaS-Aspekte

### 14.1 Serviceschicht als Sicherheitsgrenze

Die Serviceschicht ist keine reine Komfortschicht. Sie ist eine wichtige Sicherheitsgrenze. Controller können leicht umgangen werden, wenn derselbe Service später aus Batch-Jobs, Events, internen APIs, Tests oder Admin-Werkzeugen aufgerufen wird.

Deshalb gilt:

1. Autorisierung darf nicht ausschließlich im Controller liegen.
2. Tenant-Isolation muss in Service und Repository abgesichert werden.
3. Request-Felder wie `tenantId`, `userId`, `role`, `admin`, `price`, `discount`, `status` dürfen nicht blind vertraut werden.
4. Service-Methoden müssen klar trennen, welche Werte vom Client kommen und welche aus dem vertrauenswürdigen Kontext stammen.
5. Entity-Exposition ist ein Risiko für übermäßige Datenfreigabe.
6. generische Update-DTOs sind ein Risiko für Mass Assignment.

### 14.2 Broken Object Property Level Authorization vermeiden

OWASP API Security 2023 beschreibt Broken Object Property Level Authorization als Risiko, bei dem APIs Eigenschaften eines Objekts offenlegen oder ändern lassen, obwohl der Nutzer darauf keinen Zugriff haben sollte.

Schlechtes Beispiel:

```java
public record UpdateUserRequest(
        String name,
        String email,
        boolean admin,
        BigDecimal creditLimit,
        String tenantId
) {}
```

Wenn ein normaler Nutzer dieses DTO senden kann, besteht hohes Risiko. Er könnte versuchen, `admin`, `creditLimit` oder `tenantId` zu verändern.

Besser:

```java
public record UpdateOwnProfileCommand(
        String displayName,
        String preferredLanguage
) {}
```

Administrative Use Cases erhalten eigene Commands, eigene Services, eigene Berechtigungsprüfung und eigene Auditierung.

### 14.3 Tenant-Kontext nicht aus untrusted input ableiten

Falsch:

```java
@Transactional
public OrderCreatedResponse createOrder(CreateOrderCommand command) {
    // command.tenantId() kommt direkt aus dem Request
    return orderRepository.saveForTenant(command.tenantId(), command.toEntity());
}
```

Besser:

```java
@Transactional
public OrderCreatedResponse createOrder(CreateOrderCommand command) {
    var tenantId = tenantContext.currentTenantId();
    tenantAccessPolicy.assertMayCreateOrder(tenantId);

    var order = OrderEntity.create(
            tenantId,
            command.productId(),
            command.quantity()
    );

    return mapper.toCreatedResponse(orderRepository.save(order));
}
```

Der Tenant wird aus dem authentifizierten Kontext abgeleitet. Falls ein Request eine Tenant-ID enthält, muss sie gegen diesen Kontext geprüft werden.

### 14.4 Logging in Services

Services dürfen fachliche Ereignisse loggen, aber keine sensitiven Payloads unkontrolliert ausgeben.

Nicht loggen:

1. Passwörter.
2. Tokens.
3. API Keys.
4. Session-IDs.
5. vollständige Payment-Daten.
6. vollständige personenbezogene Profile ohne Zweckbindung.
7. komplette Request-DTOs aus Auth-, Identity-, Payment- oder Admin-Kontexten.
8. Cross-Tenant-Daten in gemeinsamen Log-Kontexten.

Besser:

```java
log.info("User registration completed: tenantId={}, userId={}", tenantId, userId);
```

Nicht:

```java
log.info("Register user command: {}", command);
```

Bei Records ist das besonders relevant, weil `toString()` automatisch alle Komponenten ausgeben kann.

---

## 15. Async, Virtual Threads und Ressourcensteuerung

### 15.1 `@Async` ist kein Konsistenzmodell

`@Async` verschiebt Arbeit in einen anderen Thread. Es beantwortet aber nicht automatisch:

1. Was passiert bei Fehlern?
2. Wer retryt?
3. Wie wird Idempotenz sichergestellt?
4. Wie wird Backpressure umgesetzt?
5. Welche Tenant-Grenzen gelten?
6. Wie wird überwacht?
7. Wie wird bei Shutdown sauber beendet?

Deshalb darf `@Async` nicht als Ersatz für ein robustes Integrations- oder Jobmodell verwendet werden.

### 15.2 Virtual Threads richtig einordnen

Spring Boot kann mit Java 21 Virtual Threads aktivieren. Wenn `spring.threads.virtual.enabled=true` gesetzt ist, konfiguriert Spring Boot unter anderem einen `AsyncTaskExecutor`, der Virtual Threads nutzt.

Das bedeutet aber nicht, dass unbegrenzte Nebenläufigkeit fachlich richtig ist. Auch mit Virtual Threads bleiben Datenbankverbindungen, externe APIs, Rate Limits, CPU, Speicher und Tenant-Quotas begrenzte Ressourcen.

Regel:

1. Virtual Threads erleichtern blockierenden I/O-Code.
2. Virtual Threads ersetzen kein Ressourcenlimit.
3. Virtual Threads ersetzen kein Timeout.
4. Virtual Threads ersetzen keine Idempotenz.
5. Virtual Threads ersetzen kein Outbox-Muster.
6. Virtual Threads ersetzen keine fachliche Transaktionsmodellierung.

### 15.3 Begrenzte Parallelität

Wenn ein Service viele parallele Tasks startet, muss er begrenzen, wie viele externe Ressourcen gleichzeitig belastet werden.

```java
private final Semaphore billingConcurrency = new Semaphore(20);

public void callBillingSafely(BillingCommand command) {
    if (!billingConcurrency.tryAcquire()) {
        throw new TooManyConcurrentBillingRequestsException();
    }
    try {
        billingClient.createSubscription(command);
    } finally {
        billingConcurrency.release();
    }
}
```

Dies ist nur ein einfaches Beispiel. In produktiven Systemen können Bulkheads, Rate Limiter, Resilience-Bibliotheken, Queueing oder Job-Systeme geeigneter sein.

---

## 16. Falsche Anwendung und Anti-Patterns

### 16.1 God Service

Ein Service mit 20 Abhängigkeiten und 50 öffentlichen Methoden ist fast immer ein Zeichen für fehlenden Schnitt.

Symptome:

1. Name endet auf generisches `Manager`, `Helper` oder `Service` ohne Use-Case-Klarheit.
2. Methoden betreffen Registrierung, Suche, Billing, E-Mail, Admin, Export und Reporting zugleich.
3. Tests müssen riesige Mock-Setups bauen.
4. jede Änderung erzeugt Merge-Konflikte.
5. neue Anforderungen werden einfach „noch schnell“ in denselben Service gebaut.

Korrektur:

1. nach Use Cases schneiden.
2. interne Policies auslagern.
3. Events oder Outbox für Nebeneffekte verwenden.
4. lesende und schreibende Services trennen, wenn die Modelle stark auseinanderlaufen.

### 16.2 Anämischer Weiterleitungsservice

Auch das Gegenteil ist schlecht: Ein Service, der nur Repository-Methoden eins zu eins weiterreicht.

```java
@Service
public class UserService {

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
}
```

Wenn ein Service keine fachliche Regel, keine Transaktionsgrenze, kein Mapping, keine Autorisierung und keinen Use Case ausdrückt, sollte geprüft werden, ob er überhaupt gebraucht wird.

### 16.3 Transaction Script ohne Fachsprache

```java
@Transactional
public void process(Long id) {
    var a = repoA.findById(id).orElseThrow();
    var b = repoB.findByA(a);
    if (b.getX() > 10) {
        b.setY("OK");
    }
    repoB.save(b);
}
```

Der Code funktioniert vielleicht, aber niemand erkennt den Use Case. Gute Services nutzen fachliche Namen:

```java
@Transactional
public ApprovalResponse approvePendingOrder(ApproveOrderCommand command) {
    // fachlich lesbarer Ablauf
}
```

### 16.4 Entity als Request-Body

```java
@PostMapping("/users")
public UserEntity create(@RequestBody UserEntity user) {
    return userRepository.save(user);
}
```

Das ist in produktiven APIs verboten. Es öffnet die Tür für Mass Assignment, fehlende Validierung und direkte Kopplung an das Persistenzmodell.

### 16.5 Event-Missbrauch als verdeckter Kontrollfluss

Events sind schlecht, wenn sie nur verwendet werden, damit niemand mehr sieht, was ein Use Case wirklich auslöst.

Ein Event ist geeignet, wenn mehrere unabhängige Reaktionen auf ein fachliches Ereignis möglich sind.

Ein Event ist ungeeignet, wenn die Hauptaktion fachlich zwingend von einem Schritt abhängt und dieser Schritt synchron, transaktional oder explizit fehlerbehandelt werden muss.

---

## 17. Framework- und Plattform-Kontext

### 17.1 Spring MVC

Controller sollen HTTP in Commands übersetzen und Responses ausliefern. Sie sollen nicht entscheiden, wie Datenbanktransaktionen, Billing, Tenant-Regeln oder komplexe Fachlogik funktionieren.

### 17.2 Spring Data JPA

Repositories sind für Datenzugriff zuständig. Sie dürfen gezielte Query-Methoden, Projections und Entity-Ladeoperationen anbieten. Sie sollen aber keine Use-Case-Orchestrierung enthalten.

Für lesende Use Cases sind DTO-Projections sinnvoll, wenn nicht das vollständige Aggregat benötigt wird.

### 17.3 Bean Validation

Bean Validation ist sinnvoll für strukturelle Eingaberegeln:

1. nicht leer.
2. Format.
3. Länge.
4. Wertebereiche.
5. verschachtelte Objektvalidierung.

Bean Validation ersetzt nicht:

1. Autorisierung.
2. Tenant-Prüfung.
3. Datenbank-Eindeutigkeit.
4. fachliche Statusübergänge.
5. Betrugs-/Missbrauchsregeln.
6. Cross-Field-Regeln, sofern sie besser als Domain Policy modelliert werden.

### 17.4 Wicket-Kontext

In Wicket-Anwendungen darf die Serviceschicht nicht von Wicket-Komponenten abhängen. Wicket-Seiten und Panels sollen Services aufrufen und DTOs anzeigen. Services sollen keine Wicket-Modelle, Komponenten oder Page-State-Objekte kennen.

Besonders wichtig:

1. keine Wicket-Komponenten in Service-Methodenparametern.
2. keine UI-spezifische Session-Logik in Services.
3. DTOs für View-Modelle bewusst klein halten.
4. Service-Ergebnisse nicht unkontrolliert im serialisierten Page State halten.

### 17.5 Modularer Monolith

Bei modularen Monolithen soll ein Service nicht direkt in die Persistenz eines anderen Moduls greifen. Stattdessen sind öffentliche Application Services, Domain Events oder klar definierte Ports zu verwenden.

---

## 18. Testing-Regeln

### 18.1 Unit-Tests ohne Spring-Kontext

Ein gut geschnittener Service kann in vielen Fällen ohne Spring-Kontext getestet werden.

```java
class UserRegistrationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final TenantAccessPolicy tenantAccessPolicy = mock(TenantAccessPolicy.class);

    private final UserRegistrationService service = new UserRegistrationService(
            userRepository,
            passwordEncoder,
            eventPublisher,
            tenantAccessPolicy
    );

    @Test
    void registerFailsWhenEmailAlreadyExists() {
        var command = new RegisterUserCommand(
                "tenant-1",
                "Max Mustermann",
                "max@example.org",
                "a-very-long-password"
        );

        when(userRepository.existsByTenantIdAndEmail("tenant-1", "max@example.org"))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> service.register(command));
    }
}
```

### 18.2 Integrationstests für Transaktionen

Transaktionsverhalten, Lazy Loading, JPA-Mapping, Datenbankconstraints, `@TransactionalEventListener` und echte Repository-Queries sollten mit Integrationstests geprüft werden.

Unit-Tests beweisen nicht, dass der Spring-Proxy, die Transaktion oder der JPA-Provider wie erwartet zusammenspielen.

### 18.3 Testfälle für Services

Für relevante Services sollen Tests mindestens prüfen:

1. erfolgreicher Use Case.
2. Validierungsfehler.
3. Autorisierungsfehler.
4. Tenant-Grenzverletzung.
5. fachlicher Konflikt.
6. Datenbank-Nichtfund.
7. Nebenwirkung wird ausgelöst oder Outbox-Eintrag wird geschrieben.
8. keine Entity wird nach außen exponiert.
9. idempotentes Verhalten bei Wiederholung, falls relevant.
10. Fehler-Mapping an der API-Grenze.

---

## 19. Review-Checkliste

Im Pull Request sind folgende Fragen zu prüfen:

| Aspekt | Details/Erklärung | Prüffrage |
|---|---|---|
| Verantwortlichkeit | Der Service hat einen klaren fachlichen Schnitt. | Hat der Service genau einen zusammenhängenden Use-Case-Bereich? |
| Größe | Große Services sind ein Warnsignal. | Hat der Service zu viele öffentliche Methoden oder Abhängigkeiten? |
| Konstruktorinjektion | Abhängigkeiten sind explizit. | Werden alle Dependencies über den Konstruktor injiziert? |
| DTO-Trennung | Eingabe, Ausgabe und Entity sind getrennt. | Gibt der Service Entities an Controller oder API-Grenzen zurück? |
| Command Design | Eingaben sind use-case-spezifisch. | Gibt es generische Alles-DTOs oder präzise Commands? |
| Transaktionen | Transaktionsgrenzen sind bewusst. | Ist `@Transactional` dort gesetzt, wo die fachliche Einheit liegt? |
| Private Methoden | Proxy-Grenzen sind bekannt. | Gibt es `@Transactional` auf privaten Methoden? |
| Self-Invocation | Spring-AOP wird nicht umgangen. | Ruft eine Methode derselben Klasse eine annotierte Methode über `this` auf? |
| Read-only | Lesende Use Cases sind gekennzeichnet. | Ist `readOnly = true` sinnvoll gesetzt? |
| Externe Calls | Transaktionen werden nicht unnötig lang gehalten. | Gibt es HTTP-, Mail-, Billing- oder Webhook-Calls innerhalb offener DB-Transaktionen? |
| Events | Events verschleiern keine Pflichtlogik. | Ist klar, ob der Event kritisch, optional, synchron oder asynchron ist? |
| Outbox | Kritische Integrationen sind robust. | Wird bei kritischen Nebeneffekten ein Outbox- oder vergleichbares Muster genutzt? |
| Fehlersemantik | Fehler sind fachlich unterscheidbar. | Werden sprechende Exceptions oder Result-Typen verwendet? |
| API-Fehler | Fehler werden stabil gemappt. | Gibt es `@ControllerAdvice` oder vergleichbares Mapping? |
| Security | Autorisierung liegt nicht nur im Controller. | Prüft der Service Berechtigungen und Tenant-Kontext? |
| Logging | Logs enthalten keine sensitiven Daten. | Werden Commands, DTOs oder Entities unkontrolliert geloggt? |
| Testing | Logik ist isoliert testbar. | Kann der Service ohne Spring-Kontext unit-getestet werden? |
| Projections | Lesende Use Cases laden nicht zu viel. | Wird nur das geladen, was der Use Case benötigt? |
| Framework-Kopplung | Services bleiben web- und ui-arm. | Kennt der Service HTTP-, Wicket- oder Controller-Typen? |

---

## 20. Automatisierbare Prüfungen

### 20.1 ArchUnit-Regeln

Geeignete Architekturregeln:

```java
@ArchTest
static final ArchRule controllers_should_not_access_repositories =
        noClasses()
                .that().resideInAPackage("..api..")
                .should().accessClassesThat().resideInAPackage("..persistence..");

@ArchTest
static final ArchRule services_should_not_depend_on_web =
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web..",
                        "jakarta.servlet.."
                );

@ArchTest
static final ArchRule repositories_should_not_depend_on_services =
        noClasses()
                .that().resideInAPackage("..persistence..")
                .should().dependOnClassesThat().resideInAPackage("..application..");
```

### 20.2 Static-Analysis-Regeln

Automatisierbar oder halbautomatisierbar sind unter anderem:

1. Feldinjektion mit `@Autowired` markieren.
2. `@Transactional` auf `private` Methoden markieren.
3. Controller-Methoden mit Entity-Rückgabetyp markieren.
4. `@RequestBody` auf Entity-Typen markieren.
5. generische `throw new RuntimeException(...)` in Services markieren.
6. Services mit zu vielen Abhängigkeiten markieren.
7. Methoden mit externen Client-Aufrufen innerhalb `@Transactional` markieren.
8. Log-Ausgaben kompletter Commands oder Entities markieren.
9. Zugriff aus Controller-Package auf Repository-Package blockieren.
10. Zugriff aus Service-Package auf Web-/Servlet-/Wicket-Typen blockieren.

### 20.3 CI-Gates

Für produktionsnahe Codebasen sollten folgende Gates existieren:

1. Unit-Tests für Service-Logik.
2. Integrationstests für relevante Transaktions- und Repository-Fälle.
3. ArchUnit-Regeln für Schichtengrenzen.
4. SAST für offensichtliche Injection-, Logging- und Security-Probleme.
5. Dependency-Scanning.
6. Testdatenprüfung gegen echte Secrets.
7. Coverage-Grenzen für kritische Services.
8. Mutation Testing für besonders geschäftskritische Logik, wenn Aufwand und Nutzen passen.

---

## 21. Migration bestehender Services

### 21.1 Schrittweise Migration

Bestehende God Services sollen nicht blind in viele kleine Klassen zerlegt werden. Migration erfolgt kontrolliert:

1. öffentliche Methoden inventarisieren.
2. Use Cases gruppieren.
3. transaktionale Methoden identifizieren.
4. externe Nebeneffekte identifizieren.
5. Entity-Rückgaben und Entity-Request-Bodies finden.
6. fachliche Exceptions einführen.
7. DTOs und Mapper einführen.
8. lesende und schreibende Use Cases trennen, wenn sinnvoll.
9. Tests vor Refactoring stabilisieren.
10. neue Services extrahieren.
11. alte Methoden deprecaten oder intern weiterleiten.
12. ArchUnit-Regeln schrittweise aktivieren.

### 21.2 Migration von Entity-Responses

Wenn Controller aktuell Entities zurückgeben:

1. API-Vertrag dokumentieren.
2. Response-DTO einführen.
3. Mapping explizit implementieren.
4. prüfen, welche Felder wirklich öffentlich sein dürfen.
5. Tests für Response-Struktur ergänzen.
6. Entity-Rückgabe entfernen.
7. Jackson-Annotationen auf Entities kritisch prüfen und reduzieren.

### 21.3 Migration von direkten Nebeneffekten

Wenn Services E-Mail, Billing oder Webhooks direkt innerhalb einer Transaktion ausführen:

1. fachliche Kritikalität bewerten.
2. entscheiden: synchroner Schritt, nach Commit Event oder Outbox.
3. Idempotenzschlüssel definieren.
4. Retry-Strategie definieren.
5. Fehlerstatus modellieren.
6. Monitoring und Alerting ergänzen.
7. Tests für Fehlerfälle schreiben.

---

## 22. Ausnahmen

Abweichungen von dieser Richtlinie sind zulässig, wenn sie bewusst und nachvollziehbar sind.

Mögliche Ausnahmen:

1. sehr kleine interne CRUD-Tools mit begrenztem Risiko.
2. technische Adapter ohne fachliche Logik.
3. Legacy-Code, der nur minimal geändert wird.
4. Performance-kritische Pfade mit bewusstem Sonderdesign.
5. Framework-Zwänge.
6. bewusst gewählte CQRS-, Hexagonal- oder modulare Architekturvarianten mit eigenem Standard.
7. einfache Admin-Werkzeuge, solange Security- und Tenant-Regeln erfüllt bleiben.

Nicht zulässig als Begründung:

1. „Das war schneller.“
2. „Das machen wir immer so.“
3. „Der Controller kann doch direkt das Repository aufrufen.“
4. „Die Entity hat schon alle Felder.“
5. „Wir loggen einfach das ganze Objekt.“
6. „Die Methode ist privat, aber `@Transactional` steht ja dran.“
7. „Virtual Threads lösen das schon.“

---

## 23. Definition of Done

Ein Spring-Boot-Service erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Der Service hat einen klaren fachlichen Verantwortungsbereich.
2. Abhängigkeiten werden per Konstruktor injiziert.
3. Es gibt keine Feldinjektion.
4. Controller greifen nicht direkt auf Repositories zu.
5. Services hängen nicht von HTTP-, Servlet-, Wicket- oder Controller-Typen ab.
6. Eingaben sind über präzise Commands oder Query-Parameter modelliert.
7. Ausgaben an API-Grenzen sind Response-DTOs oder Projections, keine Entities.
8. Request-DTOs werden nicht direkt als Entities gespeichert.
9. Transaktionsgrenzen sind auf öffentlichen Service-Methoden bewusst gesetzt.
10. Schreibende Use Cases haben eine klare atomare Einheit.
11. Lesende Use Cases verwenden `readOnly = true`, wenn Datenbankzugriff und Konsistenz-/Provider-Aspekte relevant sind.
12. Es gibt kein `@Transactional` auf privaten Methoden.
13. Es gibt keine transaktionale Self-Invocation, die den Spring-Proxy umgeht.
14. externe Nebeneffekte innerhalb von Transaktionen sind vermieden oder explizit begründet.
15. kritische Nebeneffekte sind über Outbox, robuste Jobs oder vergleichbare Mechanismen abgesichert.
16. fachliche Fehler sind sprechend modelliert.
17. API-Fehler werden stabil und ohne sensible Details gemappt.
18. Tenant- und Berechtigungsprüfungen sind in oder unterhalb der Serviceschicht abgesichert.
19. Logs enthalten keine sensitiven Payloads.
20. relevante Unit-Tests existieren ohne unnötigen Spring-Kontext.
21. relevante Integrationstests prüfen Transaktionen, Repositories und Event-Verhalten.
22. ArchUnit- oder vergleichbare Regeln sichern Schichtengrenzen, sofern im Projekt möglich.
23. Abweichungen sind im Pull Request begründet.

---

## 24. Entscheidungsbaum

```text
Soll eine neue Service-Methode entstehen?
├─ Koordiniert sie einen fachlichen Use Case?
│  ├─ Nein → Ist es wirklich ein Service oder eher Mapper/Policy/Repository?
│  └─ Ja
│     ├─ Greift sie auf die Datenbank zu?
│     │  ├─ Nein → keine Transaktion setzen, außer ein klarer Grund existiert.
│     │  └─ Ja
│     │     ├─ nur lesend?
│     │     │  ├─ Ja → @Transactional(readOnly = true) prüfen.
│     │     │  └─ Nein → @Transactional auf Use-Case-Methode setzen.
│     ├─ Gibt sie Daten an API oder Controller zurück?
│     │  ├─ Ja → Response-DTO oder Projection verwenden, keine Entity.
│     │  └─ Nein → interne Typen prüfen.
│     ├─ Enthält sie externe Nebeneffekte?
│     │  ├─ Ja → nach Commit, Outbox, Idempotenz und Retry prüfen.
│     │  └─ Nein → normale Service-Logik.
│     ├─ Enthält sie Tenant- oder Benutzerbezug?
│     │  ├─ Ja → Autorisierung und Tenant-Isolation im Service prüfen.
│     │  └─ Nein → trotzdem Datenzugriff prüfen.
│     ├─ Enthält sie fachliche Fehlerfälle?
│     │  ├─ Ja → sprechende Exception oder Result-Typ modellieren.
│     │  └─ Nein → keine generischen RuntimeExceptions einführen.
│     └─ Ist sie isoliert testbar?
│        ├─ Ja → Unit-Test schreiben.
│        └─ Nein → Schnitt, Abhängigkeiten und Framework-Kopplung prüfen.
```

---

## 25. Quellen und weiterführende Literatur

- Spring Framework Reference — Dependency Injection: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- Spring Framework Reference — Declarative Transaction Management with `@Transactional`: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- Spring Framework Reference — Transaction-bound Events: https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html
- Spring Framework Reference — Bean Validation: https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html
- Spring Framework Reference — Spring MVC Validation: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html
- Spring Framework Reference — Error Responses and `ProblemDetail`: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
- Spring Boot Reference — Task Execution and Scheduling / Virtual Threads: https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html
- Spring Data JPA Reference — Projections: https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html
- RFC 9457 — Problem Details for HTTP APIs: https://www.rfc-editor.org/rfc/rfc9457.html
- OWASP API Security Top 10 2023 — API3 Broken Object Property Level Authorization: https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/
- OWASP API Security Top 10 2023 — Unrestricted Resource Consumption: https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
