# ADR-006 — Spring Boot Serviceschicht: Struktur & Qualität

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Spring Boot 3.2+             |
| Datum      | 2025-08-22                        |
| Kategorie  | Spring Boot / Architektur         |

---

## Kontext & Problem

Die Serviceschicht ist das Herzstück jeder Spring-Boot-Anwendung — hier lebt die Business-Logik. Häufige Probleme: aufgeblähte Services mit gemischten Verantwortlichkeiten, direkte Abhängigkeiten auf Framework-Typen, fehlende Trennung zwischen Eingangs- und Ausgangs-DTOs, und schleichende Transaktionsgrenzen-Fehler.

Dieses ADR definiert verbindliche Regeln für eine wartbare, testbare Serviceschicht.

---

## Schlechtes Beispiel — God Service

```java
@Service
@Transactional
public class UserService {

    // Direkte Entity-Exposition nach außen — Kopplung!
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found")); // Zu generisch
    }

    // Alles in einem Service: Emails, Rechnungen, Notifications...
    public void registerUser(String name, String email, String password) {
        // Validierung gemischt mit Business-Logik gemischt mit Infrastruktur
        if (name == null || name.isBlank()) throw new RuntimeException("bad name");
        if (userRepository.existsByEmail(email)) throw new RuntimeException("exists");

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Email-Logik direkt im Service — schwer zu testen, schwer zu ersetzen
        emailService.sendWelcomeEmail(email, name);
        billingService.createFreeTrialSubscription(user.getId());
        notificationService.notifyAdmins("New user: " + email);
    }

    // Direkter DB-Zugriff ohne Transaktionsüberlegung — N+1 Problem vorprogrammiert
    public List<User> getAllUsersWithOrders() {
        return userRepository.findAll().stream()
            .peek(u -> u.getOrders().size()) // Lazy Loading außerhalb Transaktion → LazyInitializationException
            .collect(Collectors.toList());
    }
}
```

**Warum ist das schlecht?**
- `RuntimeException` als Fehlertyp ist nicht unterscheidbar — kein sauberes Fehlerhandling im Controller möglich.
- Entity direkt als Rückgabetyp: Änderungen am Datenbankschema brechen sofort die API.
- Ein Service macht zu viel: Registration, Billing, Notification — Single Responsibility Principle verletzt.
- `@Transactional` auf Klassenebene schließt alle Methoden ein, auch solche ohne DB-Zugriff.
- N+1-Problem durch Lazy Loading außerhalb der Transaktion.

---

## Gutes Beispiel — strukturierte Serviceschicht

### 1. Dedizierte Exception-Hierarchie

```java
// Sprechende, behandelbare Exceptions
public sealed interface DomainException
    permits UserNotFoundException, EmailAlreadyExistsException, ValidationException {}

public record UserNotFoundException(Long userId)
    implements DomainException, RuntimeException {
    public UserNotFoundException {
        super("User not found: " + userId);
    }
}

public record EmailAlreadyExistsException(String email)
    implements DomainException, RuntimeException {
    public EmailAlreadyExistsException {
        super("Email already registered: " + email);
    }
}
```

### 2. Klare DTO-Trennung (Command / Response)

```java
// Eingehend: Command-Objekt (was der Aufrufer sendet)
public record RegisterUserCommand(
    @NotBlank String name,
    @Email    String email,
    @Size(min = 8) String password
) {}

// Ausgehend: Response-Objekt (was der Service zurückgibt)
// Niemals die JPA-Entity direkt exponieren!
public record UserCreatedResponse(
    Long   userId,
    String name,
    String email,
    Instant registeredAt
) {}
```

### 3. Schlanker, fokussierter Service

```java
@Service
public class UserRegistrationService {

    private final UserRepository      userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // Konstruktor-Injektion — kein @Autowired auf Feldern
    public UserRegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher  = eventPublisher;
    }

    @Transactional
    public UserCreatedResponse register(RegisterUserCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        var user = new UserEntity(
            command.name(),
            command.email(),
            passwordEncoder.encode(command.password())
        );

        var saved = userRepository.save(user);

        // Event statt direktem Service-Aufruf → lose Kopplung
        // Email, Billing, Notification reagieren auf das Event — unabhängig voneinander
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail()));

        return new UserCreatedResponse(
            saved.getId(),
            saved.getName(),
            saved.getEmail(),
            saved.getCreatedAt()
        );
    }
}
```

### 4. Event-Handler für Nebeneffekte

```java
@Component
public class UserRegistrationEventHandler {

    private final EmailService        emailService;
    private final BillingService      billingService;
    private final NotificationService notificationService;

    // Jeder Nebeneffekt ist ein eigener Listener — unabhängig testbar und austauschbar
    @EventListener
    @Async // Läuft außerhalb der ursprünglichen Transaktion
    public void onUserRegistered(UserRegisteredEvent event) {
        emailService.sendWelcomeEmail(event.email());
    }

    @EventListener
    @Async
    public void createFreeTrial(UserRegisteredEvent event) {
        billingService.createFreeTrialSubscription(event.userId());
    }
}
```

### 5. Korrekte `@Transactional`-Verwendung

```java
@Service
public class OrderService {

    // Lesende Operationen: readOnly=true → Performance-Optimierung
    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getOrderSummaries(Long userId) {
        // JOIN FETCH verhindert N+1 Problem
        return orderRepository.findSummariesByUserId(userId);
    }

    // Schreibende Operationen: Standard-@Transactional
    @Transactional
    public OrderCreatedResponse createOrder(CreateOrderCommand command) {
        // ...
    }

    // Kein @Transactional für reine Berechnungen ohne DB-Zugriff
    public BigDecimal calculateTax(BigDecimal amount, String country) {
        return taxCalculator.calculate(amount, country);
    }
}
```

---

## Architekturregeln

| Schicht       | Darf abhängen von     | Darf NICHT abhängen von |
|---------------|-----------------------|--------------------------|
| Controller    | Service, Request-DTOs | Entity, Repository       |
| Service       | Repository, Domain    | Controller, HTTP-Typen   |
| Repository    | Entity                | Service, Controller      |
| Domain/Entity | —                     | Spring, JPA Annotations* |

\* Ausnahme: `@Entity`, `@Id` etc. sind akzeptiert in der Entity-Klasse selbst.

---

## Konsequenzen

**Positiv:**
- Klare Schichtentrennung macht jeden Service isoliert testbar — kein Spring-Context nötig für Unit-Tests.
- Events entkoppeln Nebeneffekte von der Kernlogik — neue Aktionen bei Registration? Einfach neuen Listener hinzufügen.
- `readOnly = true` verbessert Datenbankperformance messbar (kein Dirty-Checking, Read-Replicas möglich).
- Sprechende Exceptions ermöglichen sauberes Fehlerhandling im `@ControllerAdvice`.

**Negativ / Einschränkungen:**
- Mehr Klassen als im "God Service"-Ansatz — initialer Aufwand höher.
- Async-Events sind transaktional entkoppelt — bei Fehler im Event-Handler wird die Haupt-Transaktion nicht zurückgerollt. Für kritische Nebeneffekte: Transactional Outbox Pattern verwenden.

---

## Tipps

- **`@Autowired` auf Feldern vermeiden** — Konstruktorinjektion macht Abhängigkeiten explizit und erlaubt echte Unit-Tests ohne Spring-Context.
- **Niemals `@Transactional` auf `private` Methoden** — Spring AOP kann private Methoden nicht abfangen, die Annotation wird ignoriert.
- **Self-Invocation vermeiden**: Ein `@Transactional`-Aufruf innerhalb derselben Klasse umgeht den Proxy — in eine separate `@Service`-Klasse auslagern.
- **`@TransactionalEventListener`** für Events, die erst nach erfolgreichem Commit gefeuert werden sollen (z. B. Email erst nach DB-Commit senden).
- Mit **Virtual Threads** (ADR-004) kann `@Async` in vielen Fällen entfallen — blockierende Calls skalieren von Haus aus.
 