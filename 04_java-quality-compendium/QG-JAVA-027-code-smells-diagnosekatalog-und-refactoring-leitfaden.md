# QG-JAVA-027 — Code Smells: Diagnosekatalog und Refactoring-Leitfaden

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-027 |
| Titel | Code Smells: Diagnosekatalog und Refactoring-Leitfaden |
| Status | Accepted / verbindlicher Standard für Code Reviews und Refactoring-Entscheidungen |
| Version | 1.0.0 |
| Datum | 2022-11-29 |
| Kategorie | Code Quality / Refactoring / Wartbarkeit |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Architektur |
| Java-Baseline | Java 21 |
| Framework-Kontext | Framework-unabhängig; Beispiele mit Java 21, Spring Boot 3.x, JPA und SaaS-Kontext |
| Geltungsbereich | Produktivcode, Tests, Services, Controller, Domain-Modelle, DTOs, Mapper, Repositories, Adapter, Konfiguration und Infrastrukturcode |
| Verbindlichkeit | Code Smells müssen im Review erkannt, bewertet und bei relevanter Änderungsnähe beseitigt oder dokumentiert akzeptiert werden. Nicht jeder Smell blockiert einen PR, aber kumulierte Smells erhöhen Refactoring-Priorität. |
| Technische Validierung | Gegen Fowler/Beck-Code-Smells, Refactoring.Guru, SonarQube-Issue-Klassifikation und SpotBugs-Java-Analyse eingeordnet |
| Kurzentscheidung | Code Smells sind Diagnosehinweise für Wartbarkeits-, Änderbarkeits-, Testbarkeits- oder Kopplungsprobleme. Sie sind keine automatischen Bugs, aber sie zeigen Stellen, an denen zukünftige Änderungen teuer, riskant oder unklar werden. |

---

## 1. Zweck

Diese Richtlinie beschreibt die wichtigsten Code Smells in Java-Systemen und zeigt konkrete Gegenmaßnahmen.

Ein Code Smell ist kein automatischer Fehler. Der Code kann funktionieren und trotzdem riechen. Der Smell sagt: „Hier wird zukünftige Änderung wahrscheinlich schmerzhaft.“ Genau deshalb sind Code Smells für professionelle Java-Qualität so wichtig. Sie helfen, Risiken zu erkennen, bevor aus Wartbarkeitsschäden echte Produktionsfehler werden.

Diese Guideline dient als Diagnosekatalog für Code Reviews, Refactoring-Planung, technische Schulden, Architektur-Reviews, Pair Programming, Qualitätsgates, Onboarding, Chapter-Standards, Pull-Request-Kommentare und gezielte Modernisierung alter Java-Codebasen.

Der wichtigste Punkt: Ein Smell ist kein Anlass für einen Big-Bang-Rewrite. Ein Smell ist ein Hinweis für kontrolliertes, testgestütztes Refactoring.

---

## 2. Kurzregel

Wenn Code schwer zu lesen, schwer zu ändern, schwer zu testen oder schwer sicher zu betreiben ist, liegt meist ein Code Smell vor. Smells werden nicht nach Geschmack bewertet, sondern nach Risiko: Korrektheit, Änderbarkeit, Testbarkeit, Security, Performance, Betrieb und Teamverständnis. Einzelne kleine Smells können akzeptabel sein. Mehrere Smells an derselben Stelle sind ein klares Refactoring-Signal.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für Domänenlogik, Application Services, Spring Services, Controller, Repository-Nutzung, JPA-Entities, Mapper, DTOs, Value Objects, Tests, Konfigurationsklassen, Kafka Consumer/Producer, REST Clients, Docker-/CI-bezogenen Java-Code, Security- und Authorization-Code sowie SaaS-/Tenant-bezogene Logik.

Sie gilt nicht als mechanischer Zwang, jeden auffälligen Code sofort umzubauen. Priorität entsteht aus Änderungsnähe, Risiko, Häufung und fachlicher Bedeutung.

---

## 4. Technischer Hintergrund

Der Begriff „Code Smell“ wurde besonders durch Kent Beck und Martin Fowler im Kontext von Refactoring bekannt. Fowler beschreibt Code Smells als Hinweise auf tieferliegende Designprobleme, die durch Refactoring verbessert werden können. Viele klassische Smells wie Long Method, Long Parameter List, Divergent Change, Shotgun Surgery, Feature Envy, Data Clumps, Primitive Obsession, Switch Statements, Speculative Generality und Message Chains sind heute weiterhin relevant.

SonarQube klassifiziert Code Smells als Wartbarkeitsprobleme, die Code verwirrend oder schwer wartbar machen können. SpotBugs analysiert Java-Bytecode statisch und erkennt zahlreiche Bug Patterns und schlechte Praktiken. Beide Werkzeuge sind hilfreich, ersetzen aber nicht das fachliche Review. Tools finden Symptome. Menschen bewerten Kontext.

---

## 5. Verbindlicher Standard

Für neue oder wesentlich geänderte Codebereiche gilt:

1. Methoden sollen kurz, klar und zielgerichtet sein.
2. Klassen sollen eine erkennbare Verantwortung haben.
3. Parameterlisten sollen lesbar und fachlich gebündelt sein.
4. Fachliche Daten, die zusammengehören, sollen als Typ modelliert werden.
5. Fachliche Regeln sollen nicht an vielen Stellen dupliziert werden.
6. Technische Abstraktionen brauchen einen aktuellen Nutzen.
7. Dead Code wird gelöscht.
8. Kommentare erklären Gründe, nicht offensichtliche Schritte.
9. Security- und Tenant-Regeln dürfen nicht in String-, Boolean- oder Copy-Paste-Logik zerfallen.
10. Switch-/`instanceof`-Kaskaden werden durch Polymorphismus, Strategy, sealed Types oder Pattern Matching geprüft.
11. Code Smells werden im Review mit Risiko und Gegenmaßnahme kommentiert.
12. Refactoring erfolgt schrittweise und testgestützt.

---

## 6. Bewertungsprinzip: Smell ist nicht automatisch Bug

Ein Smell ist ein Indikator. Er wird bewertet nach:

| Bewertungskriterium | Details/Erklärung | Beispiel |
|---|---|---|
| Änderungsnähe | Muss dieser Code bald geändert werden? | aktives Feature |
| Kritikalität | Betrifft der Code Zahlungen, Security, Kundendaten oder Buchung? | Payment-Service |
| Häufung | Treten mehrere Smells zusammen auf? | Long Method + Feature Envy + Primitive Obsession |
| Testabdeckung | Gibt es Schutz durch Tests? | fehlende Randfalltests |
| Kopplung | Wie viele Stellen brechen bei Änderung? | Shotgun Surgery |
| Verständlichkeit | Kann ein Entwickler den Code schnell erklären? | kryptische Namen |
| Betriebsrisiko | Erschwert der Code Diagnose oder Rollback? | unstrukturiertes Logging |
| Security-Risiko | Kann der Smell Schutzmechanismen schwächen? | String-Rollen, fehlende Tenant-Typen |

Ein kleiner Smell in stabilem, selten geändertem Code kann niedrige Priorität haben. Ein Smell in sicherheitskritischer, oft geänderter Fachlogik ist refactoringpflichtig.

---

# Teil A — Bloaters: Zu viel Code, zu große Strukturen

## 7. Smell: Long Method

### 7.1 Symptom

Eine Methode macht zu viele Dinge. Sie enthält mehrere Blöcke, viele Kontrollstrukturen, gemischte Abstraktionsebenen oder ist nur mit Kommentaren verständlich.

Richtwerte:

| Methode | Bewertung |
|---|---|
| 1–10 Zeilen | meistens gut |
| 10–20 Zeilen | prüfen |
| 20–40 Zeilen | häufig refactoringwürdig |
| über 40 Zeilen | fast immer kritisch |
| viele verschachtelte Blöcke | kritisch, auch bei weniger Zeilen |

Zeilenzahl ist ein Signal, kein Gesetz. Eine 25-Zeilen-Methode mit klarem linearem Ablauf kann akzeptabel sein. Eine 12-Zeilen-Methode mit drei verschachtelten Bedingungen kann schlecht sein.

### 7.2 Schlechte Anwendung

```java
public OrderResult processOrder(CreateOrderCommand command) {
    if (command.userId() == null) {
        throw new IllegalArgumentException("userId required");
    }
    if (command.items() == null || command.items().isEmpty()) {
        throw new IllegalArgumentException("items required");
    }
    for (var item : command.items()) {
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
    if (!user.isActive()) {
        throw new UserInactiveException(command.userId());
    }

    var subtotal = BigDecimal.ZERO;
    for (var item : command.items()) {
        var product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        subtotal = subtotal.add(product.price().multiply(BigDecimal.valueOf(item.quantity())));
    }

    var discount = discountService.calculate(user, subtotal);
    var tax = taxService.calculate(user.country(), subtotal.subtract(discount));
    var total = subtotal.subtract(discount).add(tax);

    var order = new OrderEntity();
    order.setUserId(user.id());
    order.setStatus(OrderStatus.PENDING);
    order.setSubtotal(subtotal);
    order.setDiscount(discount);
    order.setTax(tax);
    order.setTotal(total);
    orderRepository.save(order);

    emailService.sendOrderConfirmation(user.email(), order.id());
    inventoryService.reserve(order.id(), command.items());
    eventPublisher.publishEvent(new OrderCreatedEvent(order.id()));

    return new OrderResult(order.id(), total);
}
```

Probleme:

- Validierung, Laden, Berechnung, Persistenz und Nebeneffekte sind vermischt.
- Es gibt mehrere Gründe, die Methode zu ändern.
- Die Methode ist schwer isoliert zu testen.
- Fehlerfälle sind schwer gezielt zu prüfen.
- Review muss zu viel Kontext auf einmal halten.

### 7.3 Gute Anwendung

```java
public OrderResult processOrder(CreateOrderCommand command) {
    validate(command);

    var user = loadActiveUser(command.userId());
    var pricedOrder = priceOrder(command.items(), user);
    var order = persistOrder(user, pricedOrder);

    publishOrderCreated(order, user);

    return new OrderResult(order.id(), pricedOrder.total());
}
```

```java
private User loadActiveUser(UserId userId) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

    if (!user.isActive()) {
        throw new UserInactiveException(userId);
    }

    return user;
}
```

Jeder Schritt hat einen Namen. Das Review kann fachliche Schritte erkennen.

### 7.4 Gegenmaßnahmen

| Problem | Refactoring |
|---|---|
| mehrere Blöcke | Extract Method |
| verschiedene Verantwortlichkeiten | Extract Class |
| viele Bedingungen | Guard Clauses / Strategy / Specification |
| Validierung vermischt | Value Object / Validator |
| Persistenz vermischt | Repository/Port |
| Nebeneffekte vermischt | Event/Outbox/Handler |
| schwer testbar | Use Case aufteilen |

---

## 8. Smell: Large Class / God Class

### 8.1 Symptom

Eine Klasse hat zu viele Felder, Methoden, Abhängigkeiten oder Verantwortlichkeiten.

Warnsignale:

- mehr als 7–10 Felder,
- mehr als 5 Konstruktorabhängigkeiten,
- viele Methoden mit unterschiedlichen Themen,
- Klassenname endet auf `Manager`, `Processor`, `Handler`, `Service` ohne klare Fachlichkeit,
- Tests benötigen viele Mocks,
- Änderungen aus unterschiedlichen Gründen landen immer in derselben Klasse.

### 8.2 Schlechte Anwendung

```java
@Service
public class UserManager {

    public User register(RegisterCommand command) { ... }

    public User login(LoginCommand command) { ... }

    public void logout(String sessionId) { ... }

    public void sendWelcomeEmail(User user) { ... }

    public String exportUsersAsCsv() { ... }

    public void upgradeSubscription(UserId userId) { ... }

    public void changePassword(ChangePasswordCommand command) { ... }

    public void deleteUser(UserId userId) { ... }

    public void anonymizeUser(UserId userId) { ... }
}
```

Diese Klasse ändert sich bei Registrierung, Login, E-Mail, Reporting, Billing, Passwort, Löschung und Datenschutzanforderungen.

### 8.3 Gute Anwendung

```java
@Service
public class UserRegistrationService {
    public UserCreatedResponse register(RegisterUserCommand command) { ... }
}
```

```java
@Service
public class UserAuthenticationService {
    public LoginResult login(LoginCommand command) { ... }
}
```

```java
@Service
public class UserDataDeletionService {
    public void anonymize(UserId userId) { ... }
}
```

```java
@Service
public class UserExportService {
    public CsvDocument exportUsers(UserExportFilter filter) { ... }
}
```

### 8.4 Gegenmaßnahmen

- Extract Class
- Extract Service
- Extract Value Object
- Extract Policy
- Extract Strategy
- Modulgrenzen prüfen
- Testaufwand als Designsignal nutzen

---

## 9. Smell: Long Parameter List

### 9.1 Symptom

Eine Methode hat zu viele Parameter. Aufrufe sind schwer lesbar und fehleranfällig.

Schlecht:

```java
public User createUser(
        String firstName,
        String lastName,
        String email,
        String password,
        String role,
        boolean active,
        LocalDate birthDate) {
    ...
}
```

Aufruf:

```java
createUser("Max", "Muster", "max@example.test", "secret", "ADMIN", true,
        LocalDate.of(1990, 1, 1));
```

Was bedeutet `true`? Ist `"ADMIN"` gültig? Wurde E-Mail normalisiert?

### 9.2 Gute Anwendung

```java
public record CreateUserCommand(
        @NotBlank String firstName,
        @NotBlank String lastName,
        EmailAddress email,
        RawPassword password,
        UserRole role,
        boolean active,
        LocalDate birthDate
) {}
```

```java
public User createUser(CreateUserCommand command) {
    ...
}
```

Noch besser: Prüfen, ob `active` oder `role` überhaupt vom Aufrufer kommen darf. Bei SaaS- und Security-Kontext ist Mass Assignment ein Risiko.

### 9.3 Gegenmaßnahmen

| Symptom | Refactoring |
|---|---|
| viele zusammengehörige Werte | Introduce Parameter Object |
| fachliches Konzept versteckt | Extract Value Object |
| Boolean Parameter | Replace Parameter with Explicit Methods |
| IDs verwechslungsanfällig | Strong Types |
| Optionale Parameter | Builder oder separate Commands |
| Security-Felder im Request | serverseitig setzen |

---

## 10. Smell: Data Clumps

### 10.1 Symptom

Dieselben Felder treten immer gemeinsam auf.

Schlecht:

```java
void createOrder(String street, String zipCode, String city, String countryCode) { ... }

void validateAddress(String street, String zipCode, String city, String countryCode) { ... }

String formatAddress(String street, String zipCode, String city, String countryCode) { ... }
```

### 10.2 Gute Anwendung

```java
public record Address(
        @NotBlank String street,
        @NotBlank String zipCode,
        @NotBlank String city,
        @NotBlank String countryCode
) {
    public Address {
        countryCode = countryCode.toUpperCase(Locale.ROOT);
    }

    public String oneLine() {
        return "%s, %s %s, %s".formatted(street, zipCode, city, countryCode);
    }
}
```

```java
void createOrder(Address shippingAddress) { ... }
```

### 10.3 Gegenmaßnahmen

- Extract Value Object
- Fachliche Validierung in Record-Konstruktor
- Gemeinsames Verhalten in den Typ verschieben
- API-DTO und Domain-Value-Object bewusst trennen

---

## 11. Smell: Primitive Obsession

### 11.1 Symptom

Fachlich unterschiedliche Werte werden als `String`, `Long`, `int`, `boolean` oder `BigDecimal` ohne Kontext modelliert.

Schlecht:

```java
void transfer(String fromAccountId, String toAccountId, BigDecimal amount, String currency) {
    ...
}
```

Probleme:

- IDs können vertauscht werden.
- Betrag kann negativ sein.
- Währung kann ungültig sein.
- Betrag und Währung können getrennt werden.
- Validierung verteilt sich.

### 11.2 Gute Anwendung

```java
public record AccountId(String value) {
    public AccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("accountId must not be blank");
        }
    }
}
```

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }
}
```

```java
void transfer(AccountId from, AccountId to, Money amount) {
    ...
}
```

### 11.3 Security-/SaaS-Variante

Schlecht:

```java
Optional<Order> findById(OrderId orderId);
```

Besser bei Mandantenfähigkeit:

```java
Optional<Order> findByIdAndTenantId(OrderId orderId, TenantId tenantId);
```

Oder:

```java
Optional<Order> find(OrderId orderId, AccessScope scope);
```

`TenantId`, `UserId`, `OrderId`, `Permission` und `Role` sind keine beliebigen Strings. Sie sind Sicherheits- und Domänentypen.

---

# Teil B — Change Preventers: Änderung wird teuer

## 12. Smell: Divergent Change

### 12.1 Symptom

Eine Klasse wird aus vielen unterschiedlichen Gründen geändert.

Schlecht:

```java
public class InvoiceService {

    public Invoice createInvoice(Order order) { ... }

    public String renderInvoicePdf(Invoice invoice) { ... }

    public void sendInvoiceEmail(Invoice invoice) { ... }

    public void exportInvoiceCsv(LocalDate from, LocalDate to) { ... }

    public void archiveInvoice(Invoice invoice) { ... }
}
```

Diese Klasse ändert sich bei Rechnungslogik, PDF-Layout, E-Mail-Template, CSV-Format und Archivierungsstrategie.

### 12.2 Gute Anwendung

```java
public class InvoiceCreationService { ... }

public class InvoicePdfRenderer { ... }

public class InvoiceEmailSender { ... }

public class InvoiceCsvExporter { ... }

public class InvoiceArchiveService { ... }
```

### 12.3 Gegenmaßnahmen

- Single Responsibility Principle anwenden
- Extract Class
- Use Case Services trennen
- technische Adapter trennen
- Reporting von Fachlogik trennen
- Tests pro Verantwortung strukturieren

---

## 13. Smell: Shotgun Surgery

### 13.1 Symptom

Eine einzelne fachliche Änderung erfordert kleine Anpassungen an vielen Klassen.

Schlecht:

```java
void createUser(String email) { validateEmail(email); ... }

void updateEmail(String email) { validateEmail(email); ... }

void sendNotification(String email) { validateEmail(email); ... }

void inviteUser(String email) { validateEmail(email); ... }
```

Wenn E-Mail-Normalisierung geändert wird, müssen viele Stellen angepasst werden.

### 13.2 Gute Anwendung

```java
public record EmailAddress(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmailAddress {
        Objects.requireNonNull(value);
        value = value.trim().toLowerCase(Locale.ROOT);

        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidEmailAddressException(value);
        }
    }
}
```

Änderung an Validierung oder Normalisierung passiert an einer Stelle.

### 13.3 Gegenmaßnahmen

- Extract Value Object
- Move Method
- Centralize Rule
- Domain Service/Policy einführen
- DRY auf Wissen anwenden
- Tests auf fachliche Regel konzentrieren

---

## 14. Smell: Parallel Inheritance Hierarchies

### 14.1 Symptom

Für jede neue Klasse in einer Hierarchie muss eine entsprechende Klasse in einer anderen Hierarchie ergänzt werden.

Schlecht:

```java
abstract class PaymentMethod {}
class CreditCardPayment extends PaymentMethod {}
class PayPalPayment extends PaymentMethod {}

abstract class PaymentValidator {}
class CreditCardPaymentValidator extends PaymentValidator {}
class PayPalPaymentValidator extends PaymentValidator {}

abstract class PaymentMapper {}
class CreditCardPaymentMapper extends PaymentMapper {}
class PayPalPaymentMapper extends PaymentMapper {}
```

Neue Zahlungsart bedeutet: drei Klassenfamilien erweitern.

### 14.2 Gute Anwendung

Verhalten näher an die Variante bringen oder Strategie gezielt bündeln:

```java
sealed interface PaymentMethod
        permits CreditCardPayment, PayPalPayment {

    PaymentValidationResult validate();

    PaymentProviderRequest toProviderRequest();
}
```

Oder:

```java
interface PaymentMethodHandler<T extends PaymentMethod> {
    boolean supports(PaymentMethod method);
    PaymentValidationResult validate(T method);
    PaymentProviderRequest map(T method);
}
```

### 14.3 Gegenmaßnahmen

- Move Method
- Replace Inheritance with Composition
- Strategy Pattern
- Sealed Types mit Pattern Matching
- Handler nur einführen, wenn mehrere Varianten real existieren

---

## 15. Smell: Repeated Switches / `instanceof`-Kaskaden

### 15.1 Symptom

An mehreren Stellen wird über denselben Typ geswitcht.

Schlecht:

```java
BigDecimal discountFor(Customer customer) {
    if (customer instanceof PremiumCustomer) {
        return new BigDecimal("0.20");
    }
    if (customer instanceof RegularCustomer) {
        return new BigDecimal("0.05");
    }
    if (customer instanceof NewCustomer) {
        return BigDecimal.ZERO;
    }
    throw new IllegalStateException("Unknown customer type");
}
```

Wenn ein neuer Kundentyp kommt, müssen alle Kaskaden gefunden werden.

### 15.2 Gute Anwendung: Polymorphismus

```java
sealed interface Customer permits PremiumCustomer, RegularCustomer, NewCustomer {
    BigDecimal discountRate();
}
```

```java
record PremiumCustomer(CustomerId id) implements Customer {
    @Override
    public BigDecimal discountRate() {
        return new BigDecimal("0.20");
    }
}
```

### 15.3 Gute Anwendung: Pattern Matching mit sealed Types

```java
BigDecimal discountFor(Customer customer) {
    return switch (customer) {
        case PremiumCustomer ignored -> new BigDecimal("0.20");
        case RegularCustomer ignored -> new BigDecimal("0.05");
        case NewCustomer ignored -> BigDecimal.ZERO;
    };
}
```

### 15.4 Gegenmaßnahmen

- Polymorphismus
- Strategy Pattern
- sealed interface + switch expression
- Visitor nur bei stabiler Typmenge und vielen Operationen
- EnumMap bei einfachen Zuordnungen
- kein `default`, wenn sealed Exhaustiveness genutzt werden kann

---

# Teil C — Couplers: Zu starke Kopplung

## 16. Smell: Feature Envy

### 16.1 Symptom

Eine Methode interessiert sich mehr für Daten einer anderen Klasse als für die eigene Klasse.

Schlecht:

```java
public class OrderPrinter {

    public String format(Order order) {
        return order.customer().name()
                + ", "
                + order.customer().shippingAddress().street()
                + ", "
                + order.customer().shippingAddress().zipCode()
                + " "
                + order.customer().shippingAddress().city();
    }
}
```

`OrderPrinter` kennt zu viel über `Order`, `Customer` und `Address`.

### 16.2 Gute Anwendung

```java
public class Order {

    public String formattedShippingAddress() {
        return customer.formattedShippingAddress();
    }
}
```

```java
public class Customer {

    public String formattedShippingAddress() {
        return shippingAddress.oneLine();
    }
}
```

```java
public record Address(String street, String zipCode, String city) {

    public String oneLine() {
        return "%s, %s %s".formatted(street, zipCode, city);
    }
}
```

### 16.3 Gegenmaßnahmen

- Move Method
- Extract Value Object
- Tell, Don't Ask
- Law of Demeter
- Fachliches Verhalten zum Fachobjekt verschieben

---

## 17. Smell: Message Chains / Train Wreck

### 17.1 Symptom

Code greift über mehrere Objekte hinweg auf interne Strukturen zu.

Schlecht:

```java
String tenantId = request.getUser()
        .getCompany()
        .getTenant()
        .getId();
```

### 17.2 Gute Anwendung

```java
TenantId tenantId = tenantContext.currentTenantId();
```

Oder:

```java
TenantId tenantId = request.tenantId();
```

### 17.3 Gegenmaßnahmen

- Law of Demeter
- Delegationsmethode
- fachliches Query-Modell
- Value Object
- Context-Objekt
- technische Sicherheitsgrenzen explizit modellieren

### 17.4 Wichtige Klarstellung

Nicht jede Methodenkette ist schlecht.

Akzeptabel:

```java
orders.stream()
        .filter(Order::isOpen)
        .map(Order::id)
        .toList();
```

Problematisch:

```java
order.getCustomer().getAccount().getTenant().getPermissions().contains("ADMIN");
```

Entscheidend ist nicht die Anzahl der Punkte, sondern das Durchgreifen in fremde Objektstruktur.

---

## 18. Smell: Inappropriate Intimacy

### 18.1 Symptom

Klassen kennen zu viele interne Details voneinander.

Schlecht:

```java
public class InvoiceService {

    public void applyDiscount(Order order) {
        order.getCustomer().getInternalFlags().put("discountApplied", true);
        order.getPricingDetails().setManualOverride(true);
    }
}
```

### 18.2 Gute Anwendung

```java
public class Order {

    public void applyDiscount(Discount discount) {
        pricing.apply(discount);
        auditTrail.recordDiscountApplied(discount);
    }
}
```

### 18.3 Gegenmaßnahmen

- Kapselung wiederherstellen
- Move Method
- Hide Delegate
- Extract Aggregate Boundary
- Domain Events
- Package-private statt public APIs
- öffentliche Oberfläche verkleinern

---

## 19. Smell: Middle Man

### 19.1 Symptom

Eine Klasse leitet fast nur weiter und hat keine eigene Verantwortung.

Schlecht:

```java
public class UserFacade {

    private final UserService userService;

    public User findById(UserId id) {
        return userService.findById(id);
    }

    public User save(User user) {
        return userService.save(user);
    }

    public void delete(UserId id) {
        userService.delete(id);
    }
}
```

### 19.2 Wann Delegation trotzdem sinnvoll ist

Delegation ist legitim, wenn sie eine Grenze schützt:

- API-Facade,
- Anti-Corruption Layer,
- Security Boundary,
- Transaction Boundary,
- Adapter in Hexagonal Architecture,
- Remote Client Wrapper,
- Rate-Limiting-/Retry-/Circuit-Breaker-Wrapper.

### 19.3 Gegenmaßnahmen

- Inline Class
- Verantwortung hinzufügen oder Klasse entfernen
- Facade nur bei echter Grenze behalten
- Paketabhängigkeiten prüfen

---

# Teil D — Dispensables: Überflüssiger Code

## 20. Smell: Dead Code

### 20.1 Symptom

Code wird nicht mehr genutzt.

Schlecht:

```java
private List<User> getAllUsersLegacy() {
    return jdbcTemplate.query("SELECT * FROM legacy_users", mapper);
}
```

Oder:

```java
public Order process(Order order, boolean legacyMode) {
    return process(order);
}
```

`legacyMode` wird nie gelesen.

### 20.2 Gute Anwendung

Dead Code wird gelöscht.

```text
Git merkt sich die Historie.
```

Wenn historischer Kontext wichtig ist, gehört er in ADR, Ticket, Commit oder Dokumentation, nicht als ungenutzter Code in die Anwendung.

### 20.3 Gegenmaßnahmen

- Delete Method
- Delete Class
- Delete Parameter
- Feature Flag Lifecycle prüfen
- statische Analyse verwenden
- Tests anpassen

---

## 21. Smell: Speculative Generality

### 21.1 Symptom

Code unterstützt hypothetische Anwendungsfälle, die aktuell nicht existieren.

Schlecht:

```java
public interface OrderStorage<T extends Serializable, ID, C extends StorageContext> {

    CompletableFuture<T> storeAsync(T entity, ID id, C context, StorageOptions options);

    Optional<T> load(ID id, C context);

    Page<T> search(Specification<T> specification, Pageable pageable, C context);
}
```

Wenn es nur eine MySQL-Implementierung gibt, ist das spekulativ.

### 21.2 Gute Anwendung

```java
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
```

Erst wenn eine zweite echte Storage-Variante existiert oder ein Port in Hexagonal Architecture fachlich nötig ist, wird abstrahiert.

### 21.3 Gegenmaßnahmen

- YAGNI anwenden
- Inline Class
- Collapse Hierarchy
- Remove Unused Parameter
- Remove Unused Interface
- Abstraktion erst bei realem zweiten Anwendungsfall

---

## 22. Smell: Comments as Deodorant

### 22.1 Symptom

Kommentare überdecken unverständlichen Code.

Schlecht:

```java
// Berechne den Preis nach Formel: Basis * (1 - d) * m + t
double p = b * (1 - d) * m + t;
```

Besser:

```java
Money price = basePrice
        .applyDiscount(discountRate)
        .multiply(multiplier)
        .add(tax);
```

Schlecht:

```java
// Prüfe, ob User aktiv und erwachsen ist
if (user.active() && Period.between(user.dateOfBirth(), clock.today()).getYears() >= 18) {
    ...
}
```

Besser:

```java
if (user.isActiveAdult(clock)) {
    ...
}
```

### 22.2 Wann Kommentare gut sind

Gute Kommentare erklären:

- warum eine scheinbar ungewöhnliche Lösung gewählt wurde,
- welche fachliche Einschränkung gilt,
- welcher Standard eingehalten wird,
- warum eine Alternative nicht genutzt wurde,
- welcher ADR-/QG-Kontext relevant ist.

Beispiel:

```java
// QG-JAVA-004: ReentrantLock statt synchronized, weil diese Methode
// blockierende I/O unter Virtual Threads ausführen kann.
private final ReentrantLock lock = new ReentrantLock();
```

### 22.3 Gegenmaßnahmen

- Rename Variable
- Rename Method
- Extract Method
- Introduce Explaining Variable
- Kommentar durch sprechenden Code ersetzen
- Begründungskommentare behalten

---

## 23. Smell: Lazy Class

### 23.1 Symptom

Eine Klasse existiert, hat aber zu wenig Verantwortung.

Schlecht:

```java
public class UserName {

    private final String value;

    public UserName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
```

Wenn keine Validierung, kein Verhalten und kein fachlicher Schutz existiert, ist der Typ eventuell nur Ballast.

### 23.2 Wann kleine Klassen trotzdem richtig sind

Kleine Klassen sind sinnvoll, wenn sie:

- fachliche Typen schützen,
- Validierung kapseln,
- Security-Grenzen ausdrücken,
- IDs unverwechselbar machen,
- API-/Domain-Grenzen klären.

Beispiel:

```java
public record TenantId(String value) {
    public TenantId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
    }
}
```

### 23.3 Gegenmaßnahmen

- Inline Class, wenn kein Nutzen
- Verhalten hinzufügen, wenn fachlich sinnvoll
- Typ behalten, wenn er Verwechslung oder Security-Fehler verhindert

---

## 24. Smell: Duplicated Code

### 24.1 Symptom

Gleiche Logik existiert mehrfach.

Schlecht:

```java
BigDecimal calculateGermanTax(BigDecimal net) {
    return net.multiply(new BigDecimal("0.19"));
}
```

```java
BigDecimal calculateTaxForInvoice(BigDecimal net) {
    return net.multiply(new BigDecimal("0.19"));
}
```

```java
BigDecimal calculateTaxForOrder(BigDecimal net) {
    return net.multiply(new BigDecimal("0.19"));
}
```

### 24.2 Gute Anwendung

```java
public final class GermanVat {

    private static final BigDecimal STANDARD_RATE = new BigDecimal("0.19");

    public Money calculateFor(Money net) {
        return net.multiply(STANDARD_RATE);
    }
}
```

### 24.3 DRY-Korrektur

Nicht jede Ähnlichkeit ist Duplikation. Wenn zwei Codebereiche zufällig gleich aussehen, aber unterschiedliches Wissen repräsentieren, darf man sie getrennt lassen.

Bevor abstrahiert wird:

```text
Ist dieselbe fachliche Regel dupliziert?
Oder sehen nur zwei unterschiedliche Regeln aktuell ähnlich aus?
```

---

# Teil E — Objektmodell-Smells

## 25. Smell: Data Class / Anemic Object

### 25.1 Symptom

Eine Klasse enthält nur Daten, Getter und Setter, aber keine fachlichen Invarianten.

Schlecht:

```java
public class Order {

    private OrderStatus status;
    private List<OrderItem> items;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
```

Jeder Service kann beliebige Zustände setzen.

### 25.2 Gute Anwendung

```java
public class Order {

    private OrderStatus status;
    private final List<OrderItem> items = new ArrayList<>();

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void addItem(Product product, Quantity quantity) {
        if (status != OrderStatus.PENDING) {
            throw new OrderModificationException(status);
        }
        items.add(new OrderItem(product.id(), quantity, product.price()));
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }
}
```

### 25.3 Gegenmaßnahmen

- Encapsulate Field
- Remove Setter
- Move Method
- Rich Domain Model
- Value Objects
- Zustandsübergänge benennen

---

## 26. Smell: Refused Bequest

### 26.1 Symptom

Eine Subklasse erbt Verhalten, das sie nicht sinnvoll unterstützen kann.

Schlecht:

```java
public class FileDocument {

    public void print() { ... }

    public void saveToDisk() { ... }
}
```

```java
public class CloudDocument extends FileDocument {

    @Override
    public void saveToDisk() {
        throw new UnsupportedOperationException("Cloud documents cannot be saved locally");
    }
}
```

### 26.2 Gute Anwendung

```java
public interface Printable {
    void print();
}
```

```java
public interface LocallyPersistable {
    void saveToDisk();
}
```

```java
public class CloudDocument implements Printable {
    @Override
    public void print() { ... }
}
```

### 26.3 Gegenmaßnahmen

- Replace Inheritance with Composition
- Extract Interface
- Interface Segregation
- Liskov Substitution prüfen
- Vererbung nur bei echtem substituierbarem Verhältnis

---

## 27. Smell: Temporary Field

### 27.1 Symptom

Felder werden nur für bestimmte Methoden oder Zwischenzustände gesetzt.

Schlecht:

```java
public class PriceCalculator {

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;

    public Money calculate(Order order) {
        subtotal = calculateSubtotal(order);
        discount = calculateDiscount(order);
        tax = calculateTax(order);
        return Money.eur(subtotal.subtract(discount).add(tax));
    }
}
```

Dieser Zustand gehört nicht zur Klasse. Er gehört zur Berechnung.

### 27.2 Gute Anwendung

```java
public Money calculate(Order order) {
    var subtotal = calculateSubtotal(order);
    var discount = calculateDiscount(order, subtotal);
    var tax = calculateTax(order, subtotal.subtract(discount));

    return Money.eur(subtotal.subtract(discount).add(tax));
}
```

Oder:

```java
public record PriceBreakdown(
        Money subtotal,
        Money discount,
        Money tax,
        Money total
) {}
```

### 27.3 Gegenmaßnahmen

- lokale Variable statt Feld
- Extract Class für Berechnungsergebnis
- Methode stateless halten
- Thread Safety verbessern

---

# Teil F — Java-/Spring-spezifische Smells

## 28. Smell: Fat Controller

### 28.1 Symptom

Controller enthält Fachlogik, Persistenzzugriff oder externe Aufrufe.

Schlecht:

```java
@RestController
class OrderController {

    private final OrderRepository orderRepository;

    @PostMapping("/orders/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        var order = orderRepository.findById(id).orElseThrow();

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```

### 28.2 Gute Anwendung

```java
@RestController
class OrderController {

    private final CancelOrderUseCase cancelOrderUseCase;

    @PostMapping("/orders/{id}/cancellations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        cancelOrderUseCase.cancel(new CancelOrderCommand(new OrderId(id)));
    }
}
```

### 28.3 Gegenmaßnahmen

- Use Case Service
- Application Service
- Request/Response Mapping
- Controller nur als Adapter
- Security und Validation klar trennen

---

## 29. Smell: Repository Leakage

### 29.1 Symptom

Controller oder Domain-Objekte greifen direkt auf Repositories zu.

Schlecht:

```java
@RestController
class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users/{id}")
    UserEntity find(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

Probleme:

- Entity wird API-Vertrag.
- Service-Schicht wird umgangen.
- Security- und Tenant-Regeln fehlen leicht.
- Lazy Loading kann in JSON-Serialisierung auslösen.

### 29.2 Gute Anwendung

```java
@GetMapping("/users/{id}")
UserResponse find(@PathVariable Long id) {
    return userQueryService.findById(new UserId(id));
}
```

### 29.3 Gegenmaßnahmen

- Service-/Use-Case-Schicht verwenden
- DTOs statt Entities
- Repository nur im Persistence/Application Layer
- ArchUnit-Regel

---

## 30. Smell: Transaction Boundary Smell

### 30.1 Symptom

Transaktionen sind zu breit, fehlen oder liegen an falscher Stelle.

Schlecht:

```java
@RestController
@Transactional
class OrderController {
}
```

Schlecht:

```java
private void cancelOrder(Order order) {
    // @Transactional hier wäre wirkungslos, wenn private Methode annotiert wäre.
}
```

Gut:

```java
@Service
class CancelOrderService {

    @Transactional
    public void cancel(OrderId orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();
    }
}
```

### 30.2 Gegenmaßnahmen

- Transaktion auf Service-/Application-Use-Case
- keine Transaktion auf Controller
- keine private `@Transactional`
- Self-Invocation vermeiden
- readOnly für lesende Use Cases
- Integrationstest für Dirty Checking und Lazy Loading

---

## 31. Smell: Annotation-Driven Mystery

### 31.1 Symptom

Verhalten entsteht durch viele Annotationen, aber ist im Code kaum nachvollziehbar.

Schlecht:

```java
@Service
@Transactional
@Async
@Cacheable
@Retryable
@Timed
public class PaymentService {
    ...
}
```

Problem: Transaktion, Async, Cache, Retry und Metriken beeinflussen einander. Der Kontrollfluss wird unklar.

### 31.2 Gute Anwendung

Annotationen bewusst und eng einsetzen:

```java
@Service
public class PaymentService {

    @Timed("payment.capture.duration")
    @Transactional
    public PaymentResult capture(PaymentCommand command) {
        ...
    }
}
```

Retry eher in technischem Adapter:

```java
@Component
class PaymentProviderClient {
    @Retryable(maxAttempts = 3)
    PaymentProviderResponse capture(PaymentProviderRequest request) {
        ...
    }
}
```

### 31.3 Gegenmaßnahmen

- Annotationen reduzieren
- Verantwortlichkeiten trennen
- technische Resilience in Adapter
- fachliche Transaktion im Use Case
- Tests für Verhalten der Annotationen
- Dokumentation bei ungewöhnlicher Kombination

---

## 32. Smell: Magic Security Strings

### 32.1 Symptom

Rollen, Berechtigungen oder Scopes werden als freie Strings verteilt.

Schlecht:

```java
@PreAuthorize("hasAuthority('ORDER_CANCEL')")
public void cancelOrder(Long orderId) { ... }
```

an vielen Stellen.

Oder:

```java
if (user.permissions().contains("ADMIN")) {
    ...
}
```

### 32.2 Gute Anwendung

```java
public enum Permission {
    ORDER_CANCEL,
    ORDER_VIEW,
    ORDER_REFUND
}
```

```java
public final class Authorities {
    public static final String ORDER_CANCEL = "ORDER_CANCEL";

    private Authorities() {}
}
```

```java
@PreAuthorize("hasAuthority(T(com.example.security.Authorities).ORDER_CANCEL)")
public void cancelOrder(OrderId orderId) { ... }
```

Noch besser: fachlicher Authorization Service.

```java
orderAuthorizationService.assertCanCancel(currentUser, order);
```

### 32.3 Gegenmaßnahmen

- Permission enum oder Konstanten
- Authorization Service
- Tests für Berechtigungen
- keine freien Rollenstrings in Fachlogik
- keine Rollen aus Client-Request übernehmen

---

## 33. Smell: Tenant Leakage

### 33.1 Symptom

Tenant-Kontext wird vergessen, implizit gelesen oder inkonsistent weitergereicht.

Schlecht:

```java
Optional<Order> findById(OrderId orderId);
```

in SaaS-Kontext, obwohl Orders tenantgebunden sind.

Schlecht:

```java
TenantId tenantId = request.getHeader("X-Tenant");
orderRepository.findById(orderId);
```

### 33.2 Gute Anwendung

```java
Optional<Order> findByIdAndTenantId(OrderId orderId, TenantId tenantId);
```

```java
public record AccessScope(
        TenantId tenantId,
        UserId userId,
        Set<Permission> permissions
) {}
```

```java
orderQueryService.findById(orderId, accessScope);
```

### 33.3 Gegenmaßnahmen

- TenantId als starker Typ
- AccessScope statt loser Header
- Repository-Methoden mit Tenant-Kontext
- Integrationstest für Cross-Tenant-Zugriff
- ArchUnit/Review-Gate für tenantgebundene Queries

---

## 34. Smell: Log-and-Throw

### 34.1 Symptom

Exception wird geloggt und direkt weitergeworfen. Dadurch entstehen doppelte Logs.

Schlecht:

```java
try {
    paymentClient.charge(command);
} catch (PaymentException e) {
    log.error("Payment failed", e);
    throw e;
}
```

Wenn der globale Handler ebenfalls loggt, erscheint derselbe Fehler mehrfach.

### 34.2 Gute Anwendung

Loggen dort, wo Kontext entsteht oder wo Fehler final behandelt wird.

```java
try {
    paymentClient.charge(command);
} catch (PaymentProviderException e) {
    throw new PaymentFailedException(command.orderId(), e);
}
```

Globaler Handler:

```java
@ExceptionHandler(PaymentFailedException.class)
ProblemDetail handle(PaymentFailedException ex) {
    log.warn("Payment failed: orderId={}", ex.orderId(), ex);
    ...
}
```

### 34.3 Gegenmaßnahmen

- klare Logging-Verantwortung
- Domain Exception wrappen
- kein doppeltes Logging
- keine sensitiven Daten in Exception Message
- strukturierte Logs

---

## 35. Smell: Test Smells

### 35.1 Symptom

Tests sind schwer verständlich, langsam oder prüfen Implementierungsdetails statt Verhalten.

Warnsignale:

- Testname `test1`,
- mehrere Szenarien in einem Test,
- viel Logik im Test,
- viele Mocks,
- `@SpringBootTest` für Unit-Test,
- `verify()` als einzige Assertion,
- unklare Testdaten,
- Reihenfolgeabhängigkeit,
- flaky Tests.

### 35.2 Schlechte Anwendung

```java
@Test
void test1() {
    when(repo.findById(1L)).thenReturn(Optional.of(user));
    var result = service.findById(1L);
    verify(repo).findById(1L);
}
```

### 35.3 Gute Anwendung

```java
@Test
void findById_returnsUser_whenUserExists() {
    when(repo.findById(new UserId(1L))).thenReturn(Optional.of(user));

    var result = service.findById(new UserId(1L));

    assertThat(result.name()).isEqualTo("Max");
}
```

### 35.4 Gegenmaßnahmen

- AAA-Struktur
- aussagekräftige Namen
- AssertJ
- weniger Mocks
- Unit-Test statt Spring Context
- Testdaten-Builder
- `@Nested` für Struktur
- parametrisierte Tests für Varianten

---

## 36. Refactoring-Entscheidungsbaum

```text
Code Smell erkannt?
├── Methode/Klasse zu groß?
│   ├── Long Method → Extract Method
│   ├── Large Class → Extract Class
│   └── God Class → Verantwortlichkeiten trennen
│
├── Parameter/Daten schlecht modelliert?
│   ├── Long Parameter List → Parameter Object
│   ├── Data Clumps → Value Object
│   └── Primitive Obsession → Domänentyp
│
├── Änderung wird teuer?
│   ├── Divergent Change → SRP / Extract Class
│   ├── Shotgun Surgery → Regel zentralisieren
│   └── Parallel Hierarchy → Komposition / Strategy
│
├── Kopplung zu stark?
│   ├── Feature Envy → Move Method
│   ├── Message Chain → Law of Demeter
│   ├── Inappropriate Intimacy → Kapselung stärken
│   └── Middle Man → Inline oder echte Grenze klären
│
├── Code ist überflüssig?
│   ├── Dead Code → Löschen
│   ├── Speculative Generality → YAGNI
│   ├── Lazy Class → Inline oder Nutzen stärken
│   └── Comments as Deodorant → Rename / Extract Method
│
└── Java-/Spring-Spezifikum?
    ├── Fat Controller → Use Case Service
    ├── Repository Leakage → Service/DTO
    ├── Transaction Boundary Smell → Service-Transaktion
    ├── Magic Security Strings → Permission-Typen
    └── Tenant Leakage → TenantId/AccessScope
```

---

## 37. Priorisierung von Refactoring

Nicht jeder Smell wird sofort behoben. Priorisierung erfolgt nach Risiko.

| Priorität | Kriterien | Vorgehen |
|---|---|---|
| P0 | Security-Risiko, Datenverlust, Cross-Tenant-Zugriff | sofort beheben |
| P1 | aktiver Code, viele Änderungen, mehrere Smells | im aktuellen PR beheben oder separater PR |
| P2 | wartungsrelevant, aber nicht kritisch | Backlog-Ticket |
| P3 | stabiler Legacy-Code ohne Änderungsdruck | beobachten |
| P4 | rein geschmackliche Präferenz | nicht erzwingen |

Boy-Scout-Regel:

```text
Wenn du Code ohnehin anfasst, hinterlasse ihn ein wenig sauberer.
```

Aber: Refactoring darf Feature-PRs nicht unkontrolliert aufblasen. Größere Refactorings gehören in eigene PRs.

---

## 38. Automatisierung

Tools können Smells finden oder Hinweise liefern:

| Tool | Erkennt gut | Erkennt schlecht |
|---|---|---|
| SonarQube | Code Smells, Komplexität, Duplications, Maintainability | fachlichen Kontext |
| SpotBugs | Java Bug Patterns, Bad Practices | Architekturabsicht |
| Checkstyle | Format, Naming, einfache Regeln | Designqualität |
| PMD | Stil- und Komplexitätsregeln | fachliche Korrektheit |
| ArchUnit | Architekturabhängigkeiten | lokale Lesbarkeit |
| Error Prone | Java-Fehlermuster | Domänenmodell |
| IDE Inspections | Dead Code, ungenutzte Parameter, Simplification | Systemrisiko |

Automatisierung ist Pflichtunterstützung, aber kein Ersatz für Review.

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule controllers_should_not_depend_on_repositories =
        noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("Repository")
                .because("Controller dürfen keine Persistenzdetails kennen.");
```

Beispiel Sonar-/Quality-Gate-Prinzip:

```text
Neue Codebereiche dürfen keine neuen blocker/critical Maintainability Issues einführen,
sofern keine begründete Ausnahme dokumentiert ist.
```

---

## 39. Review-Kommentare mit Smell-Bezug

Gute Review-Kommentare benennen Smell, Risiko und Gegenmaßnahme.

```text
[major] Long Method: `processOrder` validiert, lädt Daten, berechnet Preise,
persistiert und publiziert Events. Bitte mindestens Preisberechnung und Persistenz
extrahieren. Das macht Fehlerfälle isoliert testbar.
```

```text
[blocker] Tenant Leakage: `findById(orderId)` lädt ohne Tenant-Filter.
In einem SaaS-Service kann dadurch Cross-Tenant-Zugriff entstehen.
Bitte `findByIdAndTenantId(orderId, tenantId)` oder `AccessScope` verwenden.
```

```text
[minor] Comments as Deodorant: Der Kommentar erklärt die Bedingung.
Ein Methodenname wie `user.isActiveAdult(clock)` würde den Kommentar überflüssig machen.
```

```text
[praise] Sehr gut: `EmailAddress` kapselt Validierung und Normalisierung.
Damit wird Shotgun Surgery bei E-Mail-Regeln vermieden.
```

---

## 40. Security- und SaaS-Aspekte

Code Smells sind nicht nur Wartbarkeitsthemen. In SaaS- und Security-Kontexten können sie Schutzmechanismen schwächen.

| Smell | Security-/SaaS-Risiko | Gegenmaßnahme |
|---|---|---|
| Primitive Obsession | `String tenantId`, `String role` werden verwechselt oder ungeprüft | `TenantId`, `Role`, `Permission` |
| Shotgun Surgery | Berechtigungsregel an vielen Stellen driftet auseinander | Authorization Service |
| Feature Envy | Service greift tief in User/Account/Tenant-Strukturen | AccessScope |
| Long Method | Security-Check geht zwischen Fachlogik unter | Guard Clauses / Policy |
| Magic Strings | falsche Rollen-/Scope-Strings | Konstanten, Enum, Tests |
| Fat Controller | Auth/Validation/Fachlogik vermischt | Controller als Adapter |
| Comments as Deodorant | Kommentar behauptet Security, Code erzwingt sie nicht | Test + klare Methode |
| Dead Code | alte unsichere Pfade bleiben erreichbar | löschen |
| Speculative Generality | unnötige Extension Points erhöhen Angriffsfläche | YAGNI |
| Log-and-Throw | sensitive Daten mehrfach geloggt | zentrales Logging |

---

## 41. Migration bestehender Codebereiche

Migration erfolgt schrittweise:

1. Hotspots identifizieren.
2. Smells katalogisieren.
3. Tests absichern.
4. kleine Refactorings durchführen.
5. fachliche Typen einführen.
6. Controller und Services trennen.
7. Tenant-/Security-Regeln zentralisieren.
8. Dead Code löschen.
9. ArchUnit-/CI-Regeln ergänzen.
10. größere Umbauten über separate PRs planen.

Nicht empfohlen:

```text
"Wir bauen das Modul komplett neu."
```

Besser:

```text
Wir refactoren entlang aktueller Änderungen und schützen kritische Regeln zuerst.
```

---

## 42. Anti-Patterns im Umgang mit Smells

### 42.1 Smell als persönlicher Vorwurf

Falsch:

```text
Du schreibst schlechten Code.
```

Richtig:

```text
Diese Methode hat Long-Method- und Feature-Envy-Symptome.
Das macht die nächste Änderung riskant. Vorschlag: Extract Method + Move Method.
```

### 42.2 Toolausgabe blind akzeptieren

Nicht jeder Sonar-Hinweis ist fachlich relevant. Nicht jeder Tool-Fund muss sofort blockieren. Bewertung durch Kontext bleibt nötig.

### 42.3 Smells ignorieren, weil Tests grün sind

Tests beweisen nicht automatisch gute Wartbarkeit. Grüner Code kann stark riechen.

### 42.4 Refactoring ohne Tests

Refactoring ohne Testschutz ist riskant. Zuerst Charakterisierungstests oder gezielte Unit-Tests ergänzen.

### 42.5 Big-Bang-Rewrite

Große Rewrites erzeugen neue Risiken. Refactoring ist kontrollierte Verbesserung in kleinen Schritten.

---

## 43. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Methodenlänge | Ist die Methode klar und fokussiert? | keine 60-Zeilen-Use-Case-Methode | Prüfen |
| Klassenverantwortung | Hat die Klasse einen Grund zur Änderung? | kein `UserManager` für alles | Pflicht |
| Parameter | Sind Parameter fachlich gebündelt? | Command/Value Object | Pflicht |
| Primitive | Werden Domänentypen genutzt? | `OrderId`, `TenantId`, `Money` | Pflicht bei Fachwerten |
| Duplikation | Ist Wissen zentral? | `EmailAddress` | Pflicht |
| Abstraktion | Gibt es realen Bedarf? | keine spekulativen Interfaces | Pflicht |
| Kopplung | Gibt es tiefe Objektketten? | kein `a.b().c().d()` | Prüfen |
| Controller | Enthält Controller keine Fachlogik? | Use Case Service | Pflicht |
| Repository | Verlässt Entity nicht die API-Grenze? | DTO Mapping | Pflicht |
| Transaction | Liegt Transaktion an richtiger Stelle? | Service Layer | Pflicht |
| Security | Keine String-Rollen, keine Tenant-Leaks? | Permission/TenantId | Pflicht |
| Tests | Sind Tests lesbar und fokussiert? | AAA, AssertJ | Pflicht |
| Dead Code | Wurde ungenutzter Code gelöscht? | keine Legacy-Methoden | Pflicht |
| Kommentare | Erklären Kommentare Gründe? | keine Deodorant-Kommentare | Prüfen |
| Automatisierung | Gibt es passende Tool-Regeln? | Sonar, SpotBugs, ArchUnit | Soll |

---

## 44. Definition of Done

Ein geänderter Codebereich erfüllt diese Richtlinie, wenn:

1. keine neuen kritischen Code Smells eingeführt wurden,
2. bestehende relevante Smells in berührtem Code geprüft wurden,
3. Long Methods auf verständliche Schritte reduziert wurden,
4. fachliche Werte als Domänentypen modelliert sind, wo sinnvoll,
5. zusammengehörige Daten als Value Objects oder Commands gebündelt sind,
6. keine unnötigen spekulativen Abstraktionen eingeführt wurden,
7. Dead Code entfernt wurde,
8. Controller keine Fachlogik enthalten,
9. Repositories nicht direkt aus API-Schichten genutzt werden,
10. Transaktionsgrenzen bewusst gesetzt sind,
11. Security- und Tenant-Regeln nicht dupliziert oder implizit sind,
12. Tests die refactorierten Bereiche absichern,
13. Tool-Hinweise bewertet und relevante Findings behoben wurden,
14. offene Smells mit Risiko und Nacharbeit dokumentiert sind, wenn sie nicht sofort behoben werden.

---

## 45. Quellen und weiterführende Literatur

- Martin Fowler — Refactoring, insbesondere Kapitel „Bad Smells in Code“
- Martin Fowler — Refactoring Catalog: https://refactoring.com/catalog/
- Refactoring.Guru — Code Smells: https://refactoring.guru/refactoring/smells
- Refactoring.Guru — Long Method: https://refactoring.guru/smells/long-method
- Refactoring.Guru — Feature Envy: https://refactoring.guru/smells/feature-envy
- Refactoring.Guru — Shotgun Surgery: https://refactoring.guru/smells/shotgun-surgery
- Refactoring.Guru — Speculative Generality: https://refactoring.guru/smells/speculative-generality
- SonarQube Documentation — Issues and Code Smells: https://docs.sonarsource.com/sonarqube-server/user-guide/issues/
- SpotBugs Documentation — Bug Descriptions: https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html
- Joshua Kerievsky — Refactoring to Patterns
- Michael Feathers — Working Effectively with Legacy Code
