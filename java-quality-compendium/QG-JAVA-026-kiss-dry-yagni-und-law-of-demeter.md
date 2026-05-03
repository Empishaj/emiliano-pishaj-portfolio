# QG-JAVA-026 — KISS, DRY, YAGNI und Law of Demeter

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-026 |
| Titel | KISS, DRY, YAGNI und Law of Demeter |
| Status | Accepted / verbindlicher Standard für Design- und Code-Reviews |
| Version | 1.0.0 |
| Datum | 2022-03-14 |
| Review-Datum | 2026-05-01 |
| Kategorie | Design-Prinzipien / Clean Code / Wartbarkeit |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, Architektur, QA |
| Java-Baseline | Java 21 |
| Framework-Kontext | Framework-unabhängig; anwendbar in Spring Boot, Jakarta EE, Wicket, Batch-, Worker- und Bibliothekscode |
| Geltungsbereich | Anwendungslogik, Domänenlogik, Services, Mapper, Controller, Tests, APIs, Konfiguration, Infrastrukturcode |
| Verbindlichkeit | Neue oder wesentlich geänderte Codebereiche müssen diese Prinzipien im Review berücksichtigen. Abweichungen sind zulässig, wenn sie Komplexität messbar reduzieren oder fachlich notwendig sind. |
| Technische Validierung | Gegen etablierte Literatur und Quellen zu DRY, YAGNI, Law of Demeter, AHA und Clean-Code-Design eingeordnet |
| Kurzentscheidung | Einfachheit, Wissenszentralisierung, Verzicht auf spekulative Erweiterung und geringe Kopplung sind verbindliche Qualitätsziele. Diese Prinzipien sind Diagnosewerkzeuge, keine mechanischen Dogmen. |

---

## 1. Zweck

Diese Richtlinie beschreibt vier zentrale Designprinzipien für wartbaren Java-Code:

1. **KISS**: Halte die Lösung so einfach wie möglich.
2. **DRY**: Dupliziere kein fachliches oder technisches Wissen.
3. **YAGNI**: Baue nichts, was heute nicht gebraucht wird.
4. **Law of Demeter**: Reduziere Kopplung, indem Objekte nicht durch fremde Objektstrukturen greifen.

Diese Prinzipien helfen, Code lesbar, änderbar, testbar und reviewbar zu halten. Sie sind besonders wichtig in wachsenden Java-Systemen, weil technische Komplexität selten abrupt entsteht. Sie entsteht schleichend: eine kleine Hilfsklasse hier, eine vorsorgliche Abstraktion dort, ein dritter `Manager`, eine duplizierte Geschäftsregel, eine Methodenkette über vier Objekte.

Der Zweck dieser Richtlinie ist nicht, jede Abstraktion zu verbieten. Der Zweck ist, Entwickler in die Lage zu versetzen, unnötige Komplexität früh zu erkennen und gezielt zu vermeiden.

---

## 2. Kurzregel

Verwende die einfachste Lösung, die das aktuelle Problem korrekt löst. Dupliziere kein Wissen, aber abstrahiere nicht zu früh. Baue keine Erweiterung, die heute keinen konkreten Anwendungsfall hat. Vermeide Code, der tief in fremde Objektstrukturen greift. Wenn eine Lösung nur durch viele generische Begriffe, indirekte Klassen, Konfigurationsmagie oder lange Methodenkette erklärbar ist, ist sie wahrscheinlich zu komplex.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Domänenmodelle
- Application Services
- Spring Services
- Controller
- Mapper
- Repository-Nutzung
- Konfiguration
- API-Modelle
- Utility-Code
- Tests und Test-Fixtures
- Feature-Flag-Code
- Validierung
- Security-Prüfungen
- SaaS-/Tenant-spezifische Logik
- Infrastruktur-Adapter

Sie gilt nicht als mechanisches Verbot von Abstraktionen. Interfaces, Strategien, generische Typen, Builder, Factories und Framework-Erweiterungen sind erlaubt, wenn sie ein reales Änderungs-, Test-, Sicherheits- oder Integrationsproblem lösen.

---

## 4. Technischer Hintergrund

### 4.1 Warum diese Prinzipien zusammengehören

KISS, DRY, YAGNI und Law of Demeter lösen unterschiedliche, aber verwandte Probleme.

| Prinzip | Schützt vor | Typisches Fehlbild | Gegenmittel |
|---|---|---|---|
| KISS | unnötiger Komplexität | generische Frameworks für einfache Aufgaben | einfachster korrekter Code |
| DRY | widersprüchlichem Wissen | Geschäftsregel an mehreren Stellen | Single Source of Truth |
| YAGNI | spekulativer Erweiterung | Interfaces, Strategien und Optionen ohne heutigen Bedarf | erst bauen, wenn gebraucht |
| Law of Demeter | struktureller Kopplung | `a.getB().getC().doSomething()` | Verhalten delegieren |

Diese Prinzipien stehen manchmal in Spannung. DRY kann zu früh angewendet falsche Abstraktionen erzeugen. YAGNI kann falsch verstanden zu kurzfristigem Copy-Paste führen. KISS kann zu simpel wirken, wenn notwendige Sicherheits- oder Betriebsanforderungen fehlen. Law of Demeter kann übertrieben zu vielen Weiterleitungs-Methoden führen.

Deshalb gilt: Die Prinzipien werden nicht mechanisch angewendet, sondern als Review-Diagnose genutzt.

---

## 5. Verbindlicher Standard

Code erfüllt diese Richtlinie, wenn er folgende Fragen positiv beantworten kann:

1. Kann ein erfahrener Java-Entwickler die Lösung ohne langes Kontextstudium verstehen?
2. Gibt es für jede Abstraktion einen aktuellen, konkreten Nutzen?
3. Ist fachliches Wissen genau an einer maßgeblichen Stelle modelliert?
4. Würde eine Änderung der Geschäftsregel nur an einer Stelle erfolgen?
5. Gibt es keine spekulativen Erweiterungspunkte ohne heutigen Nutzer?
6. Greift der Code nicht unnötig durch fremde Objektgraphen?
7. Sind Klassen- und Methodennamen konkret genug, um ihre Verantwortung zu verstehen?
8. Gibt es keine generischen Müllhalden wie `Manager`, `Processor`, `Handler`, `Util` ohne klare Bedeutung?
9. Werden Sicherheits-, Validierungs- und Tenant-Regeln nicht aus YAGNI-Gründen weggelassen?
10. Ist die Lösung einfacher zu testen als die überengineerte Alternative?

---

# Teil A — KISS: Keep It Simple

## 6. Bedeutung

KISS bedeutet: Die einfachste Lösung, die das Problem korrekt, sicher und wartbar löst, ist zu bevorzugen.

Einfach bedeutet nicht primitiv. Einfach bedeutet:

- wenige Konzepte
- klare Namen
- wenig Indirektion
- kleine Methoden
- konkrete Typen
- nachvollziehbarer Datenfluss
- einfache Tests
- keine unnötige Konfiguration
- keine spekulativen Erweiterungspunkte

Einfacher Code ist nicht der Code mit den wenigsten Zeilen. Einfacher Code ist der Code mit der geringsten notwendigen kognitiven Last.

---

## 7. Schlechte Anwendung: überengineerte Lösung

```java
public class ConfigurationRegistry {

    private static final Map<String, ConfigurationEntry> registry =
            new ConcurrentHashMap<>();

    private final ConfigurationValidator validator;
    private final ConfigurationSerializer serializer;
    private final ConfigurationContextResolver contextResolver;

    public <T> Optional<T> getConfiguration(
            String key,
            Class<T> type,
            ConfigurationContext context,
            ValidationStrategy strategy,
            ResolutionMode mode) {

        return Optional.ofNullable(registry.get(key))
                .filter(entry -> validator.validate(entry, strategy))
                .map(entry -> serializer.deserialize(entry.getValue(), type, context))
                .map(value -> contextResolver.resolve(value, context, mode));
    }
}
```

Wenn die Aufgabe nur lautet „eine kleine Liste aktivierter Feature-Namen aus der Konfiguration lesen“, ist diese Lösung zu groß. Sie führt neue Begriffe ein, die das eigentliche Problem nicht braucht: Registry, Entry, Serializer, Context, Strategy, ResolutionMode.

---

## 8. Gute Anwendung: einfache Konfiguration

```yaml
features:
  enabled:
    - ORDER_EXPORT
    - NEW_SEARCH
    - PAYMENT_RETRY
```

```java
@ConfigurationProperties(prefix = "features")
public record FeatureConfiguration(
        List<String> enabled
) {
    public FeatureConfiguration {
        enabled = List.copyOf(enabled);
    }

    public boolean isEnabled(String featureName) {
        return enabled.contains(featureName);
    }
}
```

Diese Lösung ist ausreichend, solange die Anforderungen einfach sind. Wenn später mandantenabhängige Aktivierung, Rollout-Prozente oder Auditierung benötigt werden, kann ein echtes Feature-Flag-System eingeführt werden. Nicht vorher.

---

## 9. KISS-Heuristiken

| Aspekt | Details/Erklärung | Beispiel | Bewertung |
|---|---|---|---|
| Verständlichkeit | Ein Entwickler versteht den Code in kurzer Zeit. | direkte Methode statt generischer Pipeline | Gut |
| Begriffe | Jeder Begriff hat fachliche Bedeutung. | `OrderCancellationService` | Gut |
| Indirektion | Jede Indirektion löst ein reales Problem. | Interface für externen Payment Provider | Gut |
| Klassenname | Der Name beschreibt eine konkrete Verantwortung. | `InvoiceNumberGenerator` | Gut |
| Generik | Generics vereinfachen Wiederverwendung, nicht Lesbarkeit um jeden Preis. | `Page<T>` | Gut |
| Manager-Klasse | `Manager` ohne fachliche Aussage ist Warnsignal. | `UserManager` | Prüfen |
| Framework im Kleinen | Mini-Framework für eine einzelne Funktion. | `ValidationEngine` für zwei Regeln | Schlecht |

---

## 10. KISS-Anti-Patterns

### 10.1 Interface mit genau einer spekulativen Implementierung

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

Wenn kein zweiter Formatter existiert, kein Test-Double nötig ist und keine externe Grenze modelliert wird, ist das Interface wahrscheinlich unnötig.

Besser:

```java
public final class UserNameFormatter {

    public String format(String firstName, String lastName) {
        return "%s %s".formatted(firstName, lastName).trim();
    }
}
```

Oder noch einfacher, wenn es nur eine Zeile an einer Stelle ist: direkt im fachlichen Objekt modellieren.

### 10.2 Abstraktion vor Verständnis

```java
public interface WorkflowStep<I, O, C extends Context> {
    O execute(I input, C context);
}
```

Wenn noch nicht klar ist, welche Workflows, Steps und Kontexte existieren, ist diese Abstraktion verfrüht.

### 10.3 Konfigurierbarkeit ohne Bedarf

```yaml
order:
  cancellation:
    validation:
      enabled: true
      strategy: STRICT
      max-days-after-creation: 14
      allow-admin-override: false
```

Wenn die Regel fachlich fest ist, muss sie nicht konfigurierbar gemacht werden. Konfiguration ist ebenfalls Code und muss getestet, dokumentiert und betrieben werden.

---

# Teil B — DRY: Don't Repeat Yourself

## 11. Bedeutung

DRY bedeutet nicht: „Kein Code darf ähnlich aussehen.“

DRY bedeutet: **Jedes Stück Wissen hat genau eine maßgebliche Darstellung im System.**

Wissen kann sein:

- eine Geschäftsregel
- eine Validierungsregel
- eine Berechnungsformel
- ein Security-Check
- eine Tenant-Isolationsregel
- ein Datenbankschema
- ein API-Vertrag
- ein Fehlerformat
- eine Mapping-Regel
- eine Konfigurationsbedeutung

Zwei Codeblöcke dürfen ähnlich aussehen, wenn sie verschiedenes Wissen darstellen. Zwei Codeblöcke dürfen nicht dieselbe fachliche Wahrheit unabhängig voneinander implementieren.

---

## 12. Schlechte Anwendung: duplizierte Geschäftsregel

```java
@RestController
class UserController {

    @PostMapping("/users")
    void create(@RequestBody CreateUserRequest request) {
        if (request.email() == null || !request.email().contains("@")) {
            throw new BadRequestException("Invalid email");
        }

        userService.create(request);
    }
}
```

```java
@Service
class UserService {

    void create(CreateUserRequest request) {
        if (request.email() == null || !request.email().contains("@")) {
            throw new ValidationException("Invalid email");
        }

        userRepository.save(UserEntity.from(request));
    }
}
```

```java
@Entity
class UserEntity {

    void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }

        this.email = email;
    }
}
```

Wenn sich die E-Mail-Regel ändert, müssen drei Stellen angepasst werden. Das ist DRY-Verletzung.

---

## 13. Gute Anwendung: fachlicher Typ als Single Source of Truth

```java
public record EmailAddress(String value) {

    private static final Pattern BASIC_EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmailAddress {
        Objects.requireNonNull(value, "email must not be null");

        value = value.trim().toLowerCase(Locale.ROOT);

        if (!BASIC_EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailAddressException(value);
        }
    }

    public String masked() {
        var at = value.indexOf('@');
        if (at <= 1) {
            return "***" + value.substring(at);
        }
        return value.substring(0, 1) + "***" + value.substring(at);
    }
}
```

```java
public record CreateUserRequest(
        @NotNull EmailAddress email,
        @NotBlank String name
) {}
```

Die Regel lebt im Typ. Controller, Service und Repository verwenden den Typ und duplizieren die Regel nicht.

---

## 14. DRY falsch angewendet: falsche Abstraktion

Zwei ähnliche Codeblöcke bedeuten nicht automatisch, dass sie dieselbe Abstraktion brauchen.

Schlecht:

```java
public class UniversalFormatter {

    public String format(Object value, FormatType type) {
        return switch (type) {
            case USER_DISPLAY_NAME -> formatUserDisplayName((User) value);
            case INVOICE_NUMBER -> formatInvoiceNumber((Invoice) value);
            case SHIPPING_LABEL -> formatShippingLabel((Order) value);
        };
    }
}
```

Hier wurden unterschiedliche fachliche Konzepte in eine technische Sammelklasse gezogen. Das reduziert nicht Wissen, sondern versteckt Unterschiede.

Besser:

```java
public final class UserDisplayName {
    public static String of(User user) {
        return "%s %s".formatted(user.firstName(), user.lastName()).trim();
    }
}
```

```java
public final class InvoiceNumberFormatter {
    public String format(InvoiceNumber number) {
        return "INV-%s".formatted(number.value());
    }
}
```

---

## 15. AHA als Korrektiv zu DRY

AHA steht für **Avoid Hasty Abstractions**. Es schützt vor dem Reflex, Ähnlichkeit sofort zu abstrahieren.

Praktische Regel:

```text
Einmal: Kein Muster.
Zweimal: Beobachten.
Dreimal: Prüfen, ob wirklich dasselbe Wissen vorliegt.
Erst dann abstrahieren.
```

Das bedeutet nicht, dass man immer drei Kopien erzeugen soll. Es bedeutet: Nicht jede zweite ähnliche Stelle ist automatisch eine stabile Abstraktion.

---

# Teil C — YAGNI: You Aren't Gonna Need It

## 16. Bedeutung

YAGNI bedeutet: Baue keine Fähigkeit, für die es heute keinen konkreten Bedarf gibt.

Nicht bauen:

- generische Batch-Funktion, wenn nur Einzelverarbeitung gebraucht wird
- Plugin-System, wenn es keine Plugins gibt
- Async-API, wenn alle Aufrufer synchron arbeiten
- mehrere Provider-Strategien, wenn es nur einen Provider gibt
- Konfigurierbarkeit, wenn die Regel fachlich fest ist
- abstrakte Basisklasse, wenn keine zweite Variante existiert
- Erweiterungspunkt „für später“, ohne Roadmap, Ticket oder Risiko

YAGNI ist kein Aufruf zu schlampigem Design. Es ist ein Schutz vor spekulativer Komplexität.

---

## 17. Schlechte Anwendung: spekulative Generalisierung

```java
public interface DataFetcher<T, ID, CTX, OPT> {

    Optional<T> fetch(ID id, CTX context, FetchOptions<OPT> options);

    CompletableFuture<T> fetchAsync(ID id, CTX context);

    List<T> fetchBatch(List<ID> ids, CTX context);

    Page<T> fetchPaged(Specification<T> specification, Pageable pageable, CTX context);
}
```

Wenn die aktuelle Aufgabe nur lautet „User per ID lesen“, ist diese Abstraktion unnötig.

Besser:

```java
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findById(Long id);
}
```

Wenn Batch, Paging oder Async später wirklich gebraucht werden, wird der Code dann erweitert.

---

## 18. YAGNI gilt nicht für Grundqualität

YAGNI darf nicht als Ausrede verwendet werden, um notwendige Qualitätsanforderungen wegzulassen.

Nicht unter YAGNI fallen:

| Aspekt | Warum trotzdem notwendig? | Beispiel |
|---|---|---|
| Input-Validierung | Ungültige Eingaben sind normaler Systemzustand. | `@Valid`, Value Objects |
| Autorisierung | Sicherheitsgrenze, nicht Komfortfunktion. | `@PreAuthorize`, Tenant-Check |
| Fehlerbehandlung | Fehler treten sicher auf. | sprechende Exceptions |
| Logging | Betrieb braucht Diagnosefähigkeit. | strukturiertes Logging |
| Metriken | Betrieb braucht Systemsignale. | Timer, Counter |
| Tests | Änderungen brauchen Absicherung. | Unit-/Integrationstests |
| Transaktionsgrenzen | Datenkonsistenz ist Kernanforderung. | `@Transactional` |
| Secrets Handling | Geheimnisse dürfen nicht im Code liegen. | Vault, Environment |
| Datenminimierung | Datenschutz und Angriffsfläche. | keine PII in Logs |

YAGNI sagt: Baue keine ungenutzte Fähigkeit. YAGNI sagt nicht: Spare an Sicherheit, Betrieb oder Korrektheit.

---

## 19. Gute Anwendung: Entwicklung in kleinen Schritten

Schritt 1:

```java
public class InvoiceService {

    public Invoice createInvoice(Order order) {
        return invoiceRepository.save(Invoice.from(order));
    }
}
```

Schritt 2, wenn wirklich ein zweiter Rechnungsweg kommt:

```java
public interface InvoiceCreationStrategy {
    boolean supports(Order order);
    Invoice create(Order order);
}
```

Schritt 3, wenn mehrere Varianten produktiv existieren:

```java
@Service
public class InvoiceService {

    private final List<InvoiceCreationStrategy> strategies;

    public Invoice createInvoice(Order order) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(order))
                .findFirst()
                .orElseThrow(() -> new NoInvoiceStrategyFoundException(order.type()))
                .create(order);
    }
}
```

Das ist YAGNI-kompatibel, weil die Abstraktion erst entsteht, wenn der konkrete Bedarf existiert.

---

# Teil D — Law of Demeter

## 20. Bedeutung

Die Law of Demeter wird oft als „Rede nur mit deinen direkten Freunden“ beschrieben. Eine Methode sollte nur mit Objekten arbeiten, die sie direkt kennt:

1. das eigene Objekt,
2. Methodenparameter,
3. selbst erzeugte Objekte,
4. direkte Felder,
5. stabile, explizit injizierte Abhängigkeiten.

Sie sollte nicht durch Objektketten laufen, um an interne Details fremder Objekte zu gelangen.

---

## 21. Schlechte Anwendung: Train Wreck

```java
public class OrderPrinter {

    public String format(Order order) {
        String city = order.getCustomer()
                .getAddress()
                .getCity()
                .getName();

        String zipCode = order.getCustomer()
                .getAddress()
                .getZipCode();

        return city + " " + zipCode;
    }
}
```

Der `OrderPrinter` kennt jetzt interne Struktur von `Order`, `Customer`, `Address` und `City`. Wenn sich diese Struktur ändert, bricht er.

---

## 22. Gute Anwendung: Verhalten delegieren

```java
public class Order {

    private final Customer customer;

    public ShippingAddress shippingAddress() {
        return customer.shippingAddress();
    }

    public String shippingCityName() {
        return shippingAddress().cityName();
    }

    public String shippingZipCode() {
        return shippingAddress().zipCode();
    }
}
```

```java
public class OrderPrinter {

    public String format(Order order) {
        return order.shippingCityName() + " " + order.shippingZipCode();
    }
}
```

Noch besser kann ein eigenes Wertobjekt genutzt werden:

```java
public record ShippingAddress(
        String street,
        String zipCode,
        String cityName,
        String countryCode
) {
    public String oneLine() {
        return "%s, %s %s, %s".formatted(street, zipCode, cityName, countryCode);
    }
}
```

```java
public class OrderPrinter {

    public String format(Order order) {
        return order.shippingAddress().oneLine();
    }
}
```

Jetzt kennt `OrderPrinter` nur noch `Order` und `ShippingAddress`, nicht die gesamte interne Objektstruktur.

---

## 23. Tell, Don't Ask

Die positive Formulierung lautet: Sage einem Objekt, was es tun soll, statt seine Daten herauszuziehen und außerhalb zu entscheiden.

Schlecht:

```java
if (order.getStatus() == OrderStatus.PENDING
        && order.getPayment().isAuthorized()
        && order.getCustomer().isActive()) {
    order.setStatus(OrderStatus.CONFIRMED);
}
```

Gut:

```java
order.confirm();
```

```java
public class Order {

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new OrderCannotBeConfirmedException(id, status);
        }

        if (!payment.isAuthorized()) {
            throw new OrderCannotBeConfirmedException(id, "Payment is not authorized");
        }

        if (!customer.isActive()) {
            throw new OrderCannotBeConfirmedException(id, "Customer is inactive");
        }

        this.status = OrderStatus.CONFIRMED;
    }
}
```

Die Regel lebt dort, wo der Zustand geschützt werden kann.

---

## 24. Wann Method Chains okay sind

Nicht jede Methodenkette ist automatisch falsch.

Akzeptabel:

```java
orders.stream()
        .filter(Order::isOpen)
        .map(Order::id)
        .toList();
```

Akzeptabel:

```java
builder.withName("Max")
        .withEmail("max@example.test")
        .build();
```

Akzeptabel:

```java
assertThat(order)
        .extracting(Order::status)
        .isEqualTo(OrderStatus.PENDING);
```

Problematisch:

```java
order.getCustomer().getAccount().getPlan().getDiscountRules().get(0).apply(order);
```

Die Faustregel lautet nicht „nie mehr als ein Punkt“. Die fachliche Regel lautet: **Greift der Code durch fremde Objektstruktur, statt eine stabile fachliche Methode zu nutzen?**

---

## 25. Security- und SaaS-Aspekte

### 25.1 YAGNI darf Security nicht verschieben

Falsch:

```java
// "Autorisierung bauen wir später, erstmal MVP"
orderRepository.findById(orderId);
```

Richtig:

```java
orderRepository.findByIdAndTenantId(orderId, tenantContext.tenantId())
        .orElseThrow(() -> new OrderNotFoundException(orderId));
```

Berechtigung, Tenant-Isolation und Input-Validierung sind keine „später vielleicht“-Features. Sie sind Grundbedingungen.

### 25.2 DRY für Security-Regeln

Wenn dieselbe Berechtigungsregel an mehreren Stellen dupliziert ist, entsteht Sicherheitsdrift.

Schlecht:

```java
if (user.role().equals("ADMIN") || order.userId().equals(user.id())) {
    // erlaubt
}
```

an fünf verschiedenen Stellen.

Besser:

```java
public class OrderAuthorizationService {

    public boolean canViewOrder(UserPrincipal user, Order order) {
        return user.isAdmin() || order.isOwnedBy(user.userId());
    }
}
```

Oder methodenbasiert:

```java
@PreAuthorize("@orderAuthorization.canView(#orderId, authentication)")
public OrderDetailResponse findById(OrderId orderId) {
    return orderQueryService.findById(orderId);
}
```

### 25.3 Law of Demeter für Tenant-Kontext

Schlecht:

```java
request.getUser().getCompany().getTenant().getId();
```

Besser:

```java
tenantContext.currentTenantId();
```

Tenant-Kontext ist eine Sicherheitsgrenze und muss stabil, explizit und zentral modelliert sein.

### 25.4 KISS bei Security

Sicherheitscode muss verständlich sein. Eine übermäßig generische Security-Engine, die niemand versteht, ist riskant.

Schlecht:

```java
permissionEvaluator.evaluate(subject, resource, action, context, strategy, mode, flags);
```

Besser:

```java
orderAuthorizationService.assertCanCancel(currentUser, order);
```

---

## 26. Test- und Review-Heuristiken

### 26.1 KISS-Test

```text
Kann ein Entwickler mit Projekterfahrung den Code in 30 Sekunden grob erklären?
Wenn nein: Warum nicht?
```

### 26.2 DRY-Test

```text
Wenn sich diese Geschäftsregel ändert, wie viele Dateien müssen angepasst werden?
Mehr als eine fachliche Quelle: DRY-Verletzung prüfen.
```

### 26.3 YAGNI-Test

```text
Gibt es heute einen konkreten produktiven oder im Sprint geplanten Anwendungsfall?
Nein: nicht bauen.
```

### 26.4 Law-of-Demeter-Test

```text
Greift der Code durch fremde Objektstrukturen?
Wenn ja: Fachliche Methode, Value Object oder Query-Modell prüfen.
```

### 26.5 Testaufwand als Designsignal

Wenn ein Test sehr viele Mocks, Builder, Kontexte und Flags braucht, ist das häufig kein Testproblem, sondern ein Designproblem.

---

## 27. Gute Anwendung: vor und nach Refactoring

### 27.1 Vorher

```java
public class OrderCancellationProcessor {

    public void process(Order order, User user) {
        if (order.getCustomer().getAccount().getTenant().getId().equals(user.getTenant().getId())
                && order.getStatus() == OrderStatus.PENDING
                && order.getPayment().getStatus() != PaymentStatus.CAPTURED
                && user.getPermissions().contains("ORDER_CANCEL")) {

            order.setStatus(OrderStatus.CANCELLED);
            order.getAuditTrail().add("Cancelled by " + user.getId());
        }
    }
}
```

Probleme:

- tiefe Objektketten
- Security-Regel im Prozessor
- Statusregel außerhalb von `Order`
- Audit-Logik direkt manipuliert
- String-basierte Permission
- keine klare Exception
- unklare Verantwortung

### 27.2 Nachher

```java
public class OrderCancellationService {

    private final OrderAuthorizationService authorizationService;

    public void cancel(Order order, UserPrincipal user) {
        authorizationService.assertCanCancel(user, order);

        order.cancelBy(user.userId());
    }
}
```

```java
public class Order {

    public void cancelBy(UserId cancelledBy) {
        if (status != OrderStatus.PENDING) {
            throw new OrderCannotBeCancelledException(id, status);
        }

        if (payment.isCaptured()) {
            throw new OrderCannotBeCancelledException(id, "Payment already captured");
        }

        this.status = OrderStatus.CANCELLED;
        this.auditTrail.recordCancellation(cancelledBy);
    }

    public boolean belongsTo(TenantId tenantId) {
        return this.tenantId.equals(tenantId);
    }
}
```

```java
public class OrderAuthorizationService {

    public void assertCanCancel(UserPrincipal user, Order order) {
        if (!order.belongsTo(user.tenantId())) {
            throw new AccessDeniedException("Order does not belong to tenant");
        }

        if (!user.hasPermission(Permission.ORDER_CANCEL)) {
            throw new AccessDeniedException("Missing permission ORDER_CANCEL");
        }
    }
}
```

Ergebnis:

- KISS: Service ist kurz und verständlich.
- DRY: Autorisierung lebt an einer Stelle.
- YAGNI: keine generische Permission-Engine nötig.
- Law of Demeter: keine tiefen Objektketten.
- Security: Tenant-Check ist explizit.

---

## 28. Anti-Patterns

### 28.1 `Manager`, `Processor`, `Handler` ohne Bedeutung

```java
public class UserManager {
    // Registrierung, Login, Reporting, Billing, Rollen, CSV, Mail...
}
```

Diese Namen sind erlaubt, wenn sie fachlich präzise sind. Sie sind problematisch, wenn sie nur Unklarheit verdecken.

### 28.2 Eine Utility-Klasse als Sammelbecken

```java
public final class OrderUtils {
    public static boolean canCancel(Order order) {}
    public static BigDecimal calculateTotal(Order order) {}
    public static String formatAddress(Order order) {}
}
```

Besser: Verhalten an `Order`, `Money`, `ShippingAddress`, `OrderAuthorizationService` verteilen.

### 28.3 DRY durch falsche Basisklasse

```java
public abstract class AbstractCrudService<T, ID> {
    public T create(T value) {}
    public T update(ID id, T value) {}
    public void delete(ID id) {}
}
```

Wenn die Fachregeln pro Aggregat verschieden sind, ist diese Basisklasse ein falscher gemeinsamer Nenner.

### 28.4 YAGNI-Verletzung durch leere Erweiterungspunkte

```java
public interface OrderCancellationPlugin {
    void beforeCancel(Order order);
    void afterCancel(Order order);
}
```

Wenn es keine Plugins gibt, ist diese Abstraktion unnötig.

### 28.5 Law-of-Demeter-Verletzung durch DTO-Durchgriffe

```java
customerDto.address().city().country().isoCode();
```

Auch bei DTOs kann Kopplung entstehen. Für API-Response-Modelle ist flache, use-case-orientierte Struktur oft besser.

---

## 29. Entscheidungsbaum

```text
1. Ist die Lösung für die aktuelle Anforderung verständlich?
   Nein → vereinfachen.

2. Gibt es dieselbe Geschäftsregel an mehreren Stellen?
   Ja → fachliche Quelle zentralisieren.

3. Gibt es für die Abstraktion heute einen konkreten Anwendungsfall?
   Nein → entfernen oder verschieben.

4. Greift Code durch fremde Objektketten?
   Ja → Methode, Value Object oder Query-Modell einführen.

5. Wird eine Sicherheits-, Validierungs- oder Tenant-Regel aus Einfachheitsgründen weggelassen?
   Ja → nicht zulässig.

6. Ist eine ähnliche Stelle wirklich dasselbe Wissen?
   Nein → keine gemeinsame Abstraktion erzwingen.
```

---

## 30. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| KISS | Ist die Lösung einfacher als die Alternative? | keine Mini-Frameworks | Pflicht |
| Begriffe | Haben Klassen konkrete fachliche Namen? | `OrderCancellationService` | Pflicht |
| Abstraktion | Hat jede Abstraktion heutigen Nutzen? | Interface für externe Grenze | Pflicht |
| DRY | Ist fachliches Wissen zentral? | `EmailAddress` statt drei Regexe | Pflicht |
| AHA | Wurde nicht zu früh abstrahiert? | zweite Kopie beobachtet | Pflicht |
| YAGNI | Gibt es einen aktuellen Anwendungsfall? | Ticket/Sprint/Produktbedarf | Pflicht |
| Security | Wurde YAGNI nicht gegen Security genutzt? | Tenant-Check vorhanden | Pflicht |
| Law of Demeter | Gibt es tiefe Objektketten? | `a.b().c().d()` | Prüfen |
| Tests | Sind Tests einfach aufzusetzen? | wenige Mocks | Qualitätsindikator |
| SaaS | Sind Tenant-Regeln explizit und zentral? | `TenantContext` | Pflicht |

---

## 31. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Klassen mit Namen *Manager, *Processor, *Handler, *Util, *Utils werden im Review markiert.
- Methoden mit sehr hoher zyklomatischer Komplexität werden markiert.
- Klassen mit mehr als definierter Anzahl Konstruktorparametern werden markiert.
- Pakete mit sehr vielen Utility-Klassen werden markiert.
- Direkte Verwendung von String-Permissions wird markiert.
- Lange Message Chains können über statische Analyse teilweise erkannt werden.
```

Beispielhafte ArchUnit-Regel:

```java
@ArchTest
static final ArchRule utility_classes_should_not_be_in_domain =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().haveSimpleNameEndingWith("Utils")
                .orShould().haveSimpleNameEndingWith("Util")
                .because("Domänenlogik soll in Domänentypen, Value Objects oder fachlichen Services leben.");
```

Beispielhafte Metriken:

| Metrik | Nutzen | Interpretation |
|---|---|---|
| Konstruktorparameter pro Klasse | SRP-/KISS-Signal | viele Abhängigkeiten prüfen |
| Methodenlänge | Lesbarkeit | lange Methoden refactoren |
| Zyklomatische Komplexität | Kontrollfluss-Komplexität | hohe Werte prüfen |
| Anzahl Utility-Klassen | OOP-/Kohäsionssignal | Sammelbecken prüfen |
| Kopplung zwischen Paketen | Law-of-Demeter-/Architektur-Signal | ungewollte Abhängigkeiten prüfen |

Automatisierung ersetzt kein Review. Sie markiert Verdachtsstellen.

---

## 32. Migration bestehender Codebereiche

Migration erfolgt schrittweise:

1. Komplexe Klassen identifizieren.
2. Verantwortlichkeiten auflisten.
3. Geschäftsregeln markieren.
4. Duplikate fachlichen Wissens finden.
5. Spekulative Abstraktionen entfernen.
6. Tiefe Objektketten durch fachliche Methoden ersetzen.
7. Security- und Tenant-Regeln zentralisieren.
8. Tests vereinfachen oder ergänzen.
9. Refactoring in kleinen Pull Requests durchführen.

Vorher:

```java
if (order.getCustomer().getAccount().getTenant().getId().equals(currentTenantId)) {
    // ...
}
```

Nachher:

```java
if (order.belongsTo(currentTenantId)) {
    // ...
}
```

Oder noch besser:

```java
orderAuthorizationService.assertBelongsToTenant(order, currentTenantId);
```

---

## 33. Ausnahmen

Ausnahmen sind zulässig, wenn:

- Framework-Code bestimmte Struktur verlangt,
- DTOs bewusst flach und datenorientiert bleiben,
- Builder-APIs absichtlich fluent sind,
- Testcode aus Lesbarkeitsgründen Wiederholung zulässt,
- Performance-kritischer Code direkte Datenstrukturen braucht,
- eine Abstraktion durch mehrere heutige Implementierungen gerechtfertigt ist,
- gesetzte Plattformstandards bestimmte Interfaces verlangen.

Ausnahmen müssen begründet werden. Besonders wichtig: Eine Ausnahme für KISS oder YAGNI darf keine Sicherheits- oder Mandantengrenze schwächen.

---

## 34. Definition of Done

Ein Codebereich erfüllt diese Richtlinie, wenn:

1. die Lösung ohne unnötige Indirektion verständlich ist,
2. jede Abstraktion einen aktuellen Nutzen hat,
3. fachliches Wissen nicht an mehreren Stellen dupliziert ist,
4. ähnliche Codebereiche nicht vorschnell in falsche Abstraktionen gezwungen wurden,
5. keine spekulativen Erweiterungspunkte ohne Bedarf existieren,
6. tiefe Objektketten vermieden oder fachlich gekapselt sind,
7. Sicherheits-, Validierungs- und Tenant-Regeln nicht unter YAGNI fallen,
8. Tests die Lösung einfach und direkt prüfen können,
9. Klassen konkrete Verantwortlichkeiten besitzen,
10. Review-Kommentare zu Komplexität, Duplikation und Kopplung geschlossen sind.

---

## 35. Quellen und weiterführende Literatur

- The Pragmatic Programmer — DRY-Auszug: https://media.pragprog.com/titles/tpp20/dry.pdf
- Martin Fowler — Yagni: https://martinfowler.com/bliki/Yagni.html
- Law of Demeter, Northeastern University: https://www.ccs.neu.edu/home/lieber/LoD.html
- Law of Demeter, General Formulation: https://www2.ccs.neu.edu/research/demeter/demeter-method/LawOfDemeter/general-formulation.html
- Kent C. Dodds — AHA Programming: https://kentcdodds.com/blog/aha-programming
- Sandi Metz — Practical Object-Oriented Design: als weiterführende Literatur zu Tell, Don't Ask, Kosten falscher Abstraktion und objektorientiertem Design
- Martin Fowler — Refactoring: als weiterführende Literatur für kleine, sichere Strukturverbesserungen
