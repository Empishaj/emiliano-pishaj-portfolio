# QG-JAVA-008 — Falsche Objektorientierung: OOP richtig verstehen und anwenden

## Dokumentstatus

| Feld | Wert |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-008 |
| Titel | Falsche Objektorientierung: OOP richtig verstehen und anwenden |
| Status | Accepted / verbindlicher Standard für objektorientiertes Design in Java-Codebasen |
| Version | 1.0.0 |
| Datum | 2026-05-02 |
| Kategorie | Objektorientiertes Design / Domain Design / Code Quality |
| Java-Baseline | Java 21+ |
| Spring-Kontext | Spring Boot 3.x, sofern Services, Komponenten und JPA-Entities betroffen sind |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Verbindlichkeit | Neue Domänenlogik MUSS so modelliert werden, dass fachliche Invarianten, Zustandsübergänge und Verhaltensregeln nicht beliebig von außen umgangen werden können. Abweichungen sind im Pull Request nachvollziehbar zu begründen. |
| Technische Validierung | Reine Java-21-Beispiele wurden syntaktisch gegen Java 21 geprüft. Framework-Beispiele mit Spring/JPA-Annotationen sind konzeptionelle Beispiele und müssen im Projekt gegen konkrete Dependencies geprüft werden. |
| Quellenbasis | Martin Fowler: Anemic Domain Model; Oracle Java 21 Dokumentation zu Records und Sealed Classes; Jakarta Persistence 3.1; OWASP Mass Assignment / Web Security Testing Guide; OWASP API Security |
| Qualitätsziel | Code soll fachliche Absicht ausdrücken, Invarianten schützen, Testbarkeit verbessern, versehentliche Zustandsverletzungen verhindern und sicherheitsrelevante Daten- und Objektgrenzen sichtbar machen. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie objektorientiertes Design in Java fachlich korrekt, wartbar und prüfbar eingesetzt wird. Sie richtet sich gegen typische Fehlmuster: anämische Domänenmodelle, Utility-Klassen als Ersatz für Modellierung, Vererbung zur bloßen Code-Wiederverwendung, God Classes, primitive Typen für fachlich unterschiedliche Werte und öffentliche Setter, die gültige Zustände unterlaufen.

Objektorientierung bedeutet in dieser Richtlinie nicht: „Wir verwenden Klassen.“ Objektorientierung bedeutet: Fachliche Konzepte werden als Typen modelliert, schützen ihre Invarianten und stellen Verhalten über klare Methoden bereit. Ein Objekt soll nicht nur Daten tragen, sondern dort Verhalten besitzen, wo dieses Verhalten zur Identität, zum Zustand oder zur fachlichen Regel des Objekts gehört.

Diese Richtlinie ist kein Aufruf, jede Anwendung künstlich mit Domänenobjekten zu überladen. Für einfache CRUD-Flüsse können einfache Datenmodelle und Application Services ausreichen. Sobald jedoch fachliche Regeln, Zustandsübergänge, Berechtigungen, Geldbeträge, Mengen, Statusautomaten, Mandantenkontext oder sicherheitsrelevante Entscheidungen betroffen sind, MUSS das Modell expliziter und stärker gekapselt werden.

---

## 2. Kurzregel für Entwickler

Modelliere fachliche Regeln dort, wo sie hingehören: Invarianten und Zustandsübergänge gehören an das Domänenobjekt oder in einen klar benannten Domain Service; reine Ablaufkoordination gehört in den Application Service. Vermeide öffentliche Setter, untypisierte Primitive, God Classes, Utility-Friedhöfe und `instanceof`-Kaskaden, wenn sie fachliches Verhalten nur außerhalb der eigentlichen Domänentypen verstecken.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Domänenobjekte mit fachlichen Invarianten
- JPA-Entities mit relevantem Verhalten
- Value Objects
- Services mit Business-Logik
- Status- und Zustandsübergänge
- Geld-, Mengen-, Identitäts- und Berechtigungslogik
- SaaS-Logik mit Tenant-, Account-, Plan-, User- oder Rollenbezug
- API- und Command-Verarbeitung, sofern daraus Domänenzustand entsteht

Diese Richtlinie gilt nicht als pauschale Pflicht für:

- triviale CRUD-Administrationsmasken ohne relevante Fachlogik
- reine DTOs
- einfache Read Models und Projections
- technische Adapter, Mapper und Infrastrukturkomponenten
- temporäre Testfixtures
- bewusst funktionale oder pipeline-orientierte Datenverarbeitung, sofern sie klar, testbar und sicher bleibt

---

## 4. Technischer Hintergrund

Java unterstützt mehrere Modellierungsformen: Klassen, Interfaces, Records, Enums, Sealed Classes und seit Java 21 ausgereifte Pattern-Matching-Mechanismen. Diese Sprachmittel ersetzen Objektorientierung nicht, sondern erweitern die Möglichkeiten, fachliche Konzepte präzise auszudrücken.

Records eignen sich sehr gut für transparente, unveränderliche Datenaggregate, insbesondere DTOs und Value Objects. Sie sind jedoch bewusst keine klassischen Entities mit Identität, Lifecycle und kontrollierten Zustandsübergängen. Oracle beschreibt Records als shallowly immutable Carrier eines festen Wertesets.

Sealed Classes und Sealed Interfaces eignen sich für geschlossene Typfamilien, etwa Ergebnisvarianten, Statusmodelle oder erlaubte Domänenzustände. Oracle beschreibt Sealed Types als Möglichkeit, explizit festzulegen, welche Klassen eine Klasse erweitern oder ein Interface implementieren dürfen.

JPA-Entities besitzen zusätzliche technische Rahmenbedingungen. Jakarta Persistence verlangt unter anderem einen public oder protected No-Arg-Konstruktor und erlaubt keine finalen Entity-Klassen. Deshalb ist ein sauberes Domänenmodell mit JPA möglich, benötigt aber Disziplin bei Konstruktoren, Kapselung, Lazy Loading und Transaktionsgrenzen.

---

## 5. Verbindlicher Standard

### 5.1 Fachliche Invarianten müssen geschützt werden

Ein Objekt mit fachlichen Regeln DARF NICHT nur aus öffentlichen Settern bestehen. Zustandsänderungen MÜSSEN über benannte Methoden erfolgen, die gültige Übergänge erzwingen.

Schlecht:

```java
order.setStatus(OrderStatus.CANCELLED);
order.setTotal(BigDecimal.ZERO);
```

Gut:

```java
order.cancel();
```

Der zweite Aufruf ist fachlich stärker, weil er ausdrückt, was passiert. Die Methode `cancel()` kann prüfen, ob die Bestellung überhaupt storniert werden darf.

### 5.2 Verhalten gehört zum fachlich verantwortlichen Typ

Wenn eine Regel ausschließlich zu einem fachlichen Konzept gehört, SOLLTE sie dort modelliert werden. Ein `Order`-Objekt sollte seine Stornierbarkeit kennen. Ein `Money`-Objekt sollte Beträge und Währung schützen. Eine `Quantity` sollte verhindern, dass negative Mengen entstehen.

### 5.3 Application Services orchestrieren, sie ersetzen nicht das Modell

Ein Spring Service DARF Use Cases koordinieren: Laden, Berechtigung prüfen, Domänenmethode aufrufen, speichern, Event publizieren. Er SOLLTE jedoch nicht alle fachlichen Details selbst implementieren, wenn dadurch Domänenobjekte zu passiven Datenbehältern werden.

### 5.4 Utility-Klassen sind zu begründen

Statische Hilfsklassen mit Namen wie `UserUtils`, `CommonUtils`, `StringHelper`, `DateUtil` oder `ObjectHelper` sind Warnsignale. Sie sind nur zulässig, wenn sie eine kleine, kohärente, technische Hilfsfunktion kapseln und kein fachliches Verhalten aus Domänentypen herausziehen.

### 5.5 Primitive Obsession ist zu vermeiden

Fachlich unterschiedliche Werte SOLLTEN eigene Typen erhalten, wenn Verwechslungen, Validierungsregeln oder Sicherheitsfolgen relevant sind. `Long userId` und `Long productId` sind für den Compiler identisch. `UserId` und `ProductId` sind es nicht.

---

## 6. Abgrenzung: reiches Domänenmodell, Transaction Script und CRUD

Ein reiches Domänenmodell ist nicht für jeden Code automatisch besser. Es ist dann besonders wertvoll, wenn:

- Zustände nur in bestimmten Reihenfolgen erlaubt sind
- Regeln an mehreren Stellen wiederkehren
- ungültige Zustände teuer oder gefährlich sind
- Geld, Mengen, Rechte oder Mandantengrenzen betroffen sind
- eine Änderung mehrere Felder konsistent anpassen muss
- die fachliche Sprache im Code sichtbar werden soll

Ein einfacher Transaction-Script-Stil kann ausreichen, wenn:

- der Use Case sehr einfach ist
- keine relevante Fachlogik existiert
- Daten nur validiert, gespeichert und gelesen werden
- die Anwendung vor allem ein technisches Administrationswerkzeug ist
- das Modell bewusst als Read Model oder Projection verwendet wird

Diese Richtlinie verbietet einfache Lösungen nicht. Sie verbietet, komplexe Fachlogik in schwach benannten Services, Utility-Klassen und Setter-Sequenzen zu verstecken.

---

## 7. Fehlmuster 1 — Anämisches Domänenmodell

Ein anämisches Domänenmodell sieht auf den ersten Blick fachlich aus, enthält aber fast kein Verhalten. Es besitzt Klassen mit Domänennamen, Felder, Getter und Setter. Die eigentliche Logik liegt in Services, Utility-Klassen oder Controllern.

### 7.1 Schlechte Anwendung

```java
public class Order {

    private Long id;
    private List<OrderItem> items = new ArrayList<>();
    private OrderStatus status;
    private BigDecimal total;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
```

```java
public class OrderService {

    public void cancelOrder(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setTotal(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### 7.2 Warum das schlecht ist

Die `Order` schützt ihre eigenen Regeln nicht. Jeder Code kann den Status ändern, ohne die erlaubten Zustandsübergänge zu beachten. Dadurch werden ungültige Zustände möglich, etwa eine bereits ausgelieferte Bestellung mit Status `CANCELLED`.

Außerdem wird Verhalten schwer auffindbar. Wer wissen möchte, wann eine Bestellung storniert werden darf, muss Services durchsuchen. Das Modell selbst gibt keine Antwort.

### 7.3 Gute Anwendung

```java
public class Order {

    private Long id;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;

    protected Order() {
        // Für JPA
    }

    public Order(List<OrderItem> initialItems) {
        if (initialItems == null || initialItems.isEmpty()) {
            throw new IllegalArgumentException("Eine Bestellung benötigt mindestens eine Position.");
        }
        this.items.addAll(initialItems);
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id, "Bereits ausgelieferte Bestellungen können nicht storniert werden.");
        }

        if (status == OrderStatus.CANCELLED) {
            throw new OrderCannotBeCancelledException(id, "Die Bestellung ist bereits storniert.");
        }

        this.status = OrderStatus.CANCELLED;
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) {
            throw new OrderModificationException(id, "Nur offene Bestellungen dürfen verändert werden.");
        }
        this.items.add(Objects.requireNonNull(item));
    }

    public BigDecimal total() {
        return items.stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }
}
```

```java
public class OrderApplicationService {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void cancelOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();

        orderRepository.save(order);
    }
}
```

### 7.4 Qualitätsregel

Wenn eine Methode erst Daten aus einem Objekt liest, dann außerhalb entscheidet und anschließend über Setter denselben Objektzustand verändert, ist das ein starkes Signal für fehlendes Verhalten am Objekt.

Schlecht:

```java
if (order.getStatus() == OrderStatus.PENDING) {
    order.setStatus(OrderStatus.CANCELLED);
}
```

Gut:

```java
order.cancel();
```

---

## 8. Fehlmuster 2 — Utility-Klassen als Ersatz für Modellierung

Utility-Klassen sind häufig ein Symptom dafür, dass ein fachliches Konzept keinen eigenen Typ bekommen hat.

### 8.1 Schlechte Anwendung

```java
public final class UserUtils {

    private UserUtils() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    public static String maskEmail(String email) {
        int at = email.indexOf("@");
        return email.substring(0, 1) + "***" + email.substring(at);
    }

    public static boolean isAdult(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}
```

### 8.2 Warum das schlecht ist

`UserUtils` hat keine klare Verantwortung. E-Mail-Validierung, Maskierung und Altersberechnung sind verschiedene fachliche Konzepte. Außerdem bleiben Werte weiterhin ungeschützt: Nach der Validierung ist `String email` immer noch irgendein String.

### 8.3 Gute Anwendung mit Value Objects

```java
public record Email(String value) {

    public Email {
        Objects.requireNonNull(value, "email must not be null");
        value = value.trim().toLowerCase(Locale.ROOT);

        if (!value.contains("@")) {
            throw new InvalidEmailException(value);
        }
    }

    public String masked() {
        int at = value.indexOf('@');
        return value.substring(0, 1) + "***" + value.substring(at);
    }
}
```

```java
public record BirthDate(LocalDate value) {

    public BirthDate {
        Objects.requireNonNull(value, "birth date must not be null");
        if (value.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Geburtsdatum darf nicht in der Zukunft liegen.");
        }
    }

    public int ageInYears(Clock clock) {
        return Period.between(value, LocalDate.now(clock)).getYears();
    }

    public boolean isAdult(Clock clock) {
        return ageInYears(clock) >= 18;
    }
}
```

### 8.4 Qualitätsregel

Wenn eine statische Utility-Methode regelmäßig denselben primitiven Werttyp entgegennimmt und fachliche Regeln darauf anwendet, SOLLTE geprüft werden, ob ein Value Object fehlt.

---

## 9. Fehlmuster 3 — Vererbung zur Code-Wiederverwendung

Vererbung ist eine starke Kopplung. Sie ist geeignet, wenn ein echtes substituierbares „ist ein“-Verhältnis besteht. Sie ist ungeeignet, wenn nur Implementierung wiederverwendet werden soll.

### 9.1 Schlechte Anwendung

```java
public abstract class BaseNotification {

    public void send(Notification notification) {
        log(notification);
        doSend(notification);
        retryIfNeeded(notification);
    }

    protected abstract void doSend(Notification notification);

    private void log(Notification notification) {
        // Logging
    }

    private void retryIfNeeded(Notification notification) {
        // Retry
    }
}
```

```java
public class EmailNotification extends BaseNotification {

    @Override
    protected void doSend(Notification notification) {
        // E-Mail versenden
    }
}
```

### 9.2 Warum das schlecht ist

Subklassen sind vom Ablauf der Basisklasse abhängig. Eine Änderung in `BaseNotification` kann alle Subklassen beeinflussen. Dieses Problem ist als fragile base class bekannt. Außerdem wird Verhalten versteckt geerbt, statt explizit komponiert.

### 9.3 Gute Anwendung mit Komposition

```java
public interface NotificationSender {
    SendResult send(Notification notification);
}
```

```java
public interface NotificationLogger {
    void log(Notification notification, SendResult result);
}
```

```java
public class EmailNotificationSender implements NotificationSender {

    private final EmailClient emailClient;
    private final NotificationLogger logger;

    public EmailNotificationSender(EmailClient emailClient, NotificationLogger logger) {
        this.emailClient = emailClient;
        this.logger = logger;
    }

    @Override
    public SendResult send(Notification notification) {
        var result = emailClient.send(notification.recipient(), notification.body());
        logger.log(notification, result);
        return result;
    }
}
```

### 9.4 Qualitätsregel

Vererbung DARF NICHT nur verwendet werden, um Methoden oder Felder wiederzuverwenden. Wenn eine Klasse lediglich Verhalten nutzt, SOLLTE Komposition verwendet werden. Wenn eine Klasse wirklich eine spezialisierte Form eines Typs ist, kann Vererbung sinnvoll sein. Bei geschlossenen Typfamilien SOLLTEN Sealed Interfaces geprüft werden.

---

## 10. Fehlmuster 4 — God Class und God Service

God Classes bündeln zu viele Verantwortlichkeiten. In Spring-Anwendungen treten sie oft als `UserManager`, `OrderService`, `AdminService`, `CommonService` oder `PlatformService` auf.

### 10.1 Schlechte Anwendung

```java
public class UserManager {

    public User register(String name, String email, String password) {
        throw new UnsupportedOperationException();
    }

    public User login(String email, String password) {
        throw new UnsupportedOperationException();
    }

    public void updateProfile(Long userId, String name, String bio) {
        throw new UnsupportedOperationException();
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        throw new UnsupportedOperationException();
    }

    public void upgradeToPremium(Long userId, String paymentToken) {
        throw new UnsupportedOperationException();
    }

    public void sendVerificationEmail(Long userId) {
        throw new UnsupportedOperationException();
    }
}
```

### 10.2 Warum das schlecht ist

Die Klasse hat keine klare Grenze. Änderungen an Registrierung, Authentifizierung, Profil, Payment und Verifikation landen im gleichen Typ. Dadurch steigen Kopplung, Testaufwand, Merge-Konflikte und Risiko unbeabsichtigter Seiteneffekte.

### 10.3 Gute Anwendung

```java
public class UserRegistrationService {
    public UserId register(RegisterUserCommand command) {
        throw new UnsupportedOperationException();
    }
}
```

```java
public class UserAuthenticationService {
    public SessionToken login(LoginCommand command) {
        throw new UnsupportedOperationException();
    }
}
```

```java
public class UserProfileService {
    public void updateProfile(UpdateUserProfileCommand command) {
        throw new UnsupportedOperationException();
    }
}
```

```java
public class SubscriptionService {
    public void upgradeToPremium(UpgradeSubscriptionCommand command) {
        throw new UnsupportedOperationException();
    }
}
```

### 10.4 Qualitätsregel

Wenn die Beschreibung einer Klasse mehrere „und“ enthält, ist die Klasse wahrscheinlich zu groß. Wenn ein Test für eine Methode viele fachlich unabhängige Mocks benötigt, ist die Verantwortung wahrscheinlich falsch geschnitten.

---

## 11. Fehlmuster 5 — Primitive Obsession

Primitive Obsession bedeutet, dass fachlich unterschiedliche Konzepte als `String`, `Long`, `int`, `BigDecimal` oder `boolean` modelliert werden, obwohl eigene Typen Verwechslungen und ungültige Zustände verhindern könnten.

### 11.1 Schlechte Anwendung

```java
public void createOrder(
        Long userId,
        Long productId,
        int quantity,
        BigDecimal amount,
        String currency
) {
    // ...
}
```

Dieser Code lässt viele ungültige Aufrufe zu. `userId` und `productId` können vertauscht werden. `quantity` kann negativ sein. `currency` kann `"EURO"`, `"eur"`, `""` oder `null` sein. Der Compiler schützt nichts davon.

### 11.2 Gute Anwendung

```java
public record UserId(Long value) {

    public UserId {
        Objects.requireNonNull(value, "userId must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}
```

```java
public record ProductId(Long value) {

    public ProductId {
        Objects.requireNonNull(value, "productId must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("productId must be positive");
        }
    }
}
```

```java
public record Quantity(int value) {

    public Quantity {
        if (value <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
```

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new IllegalArgumentException("amount has too many fraction digits for currency");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }
}
```

```java
public void createOrder(
        UserId userId,
        ProductId productId,
        Quantity quantity,
        Money price
) {
    // ...
}
```

### 11.3 Qualitätsregel

Eigene Domänentypen SOLLTEN verwendet werden, wenn mindestens eine der folgenden Bedingungen erfüllt ist:

- Der Wert hat eigene Validierungsregeln.
- Der Wert kann leicht mit einem anderen Wert desselben technischen Typs verwechselt werden.
- Der Wert trägt sicherheitsrelevante Bedeutung.
- Der Wert erscheint in öffentlichen APIs, Events oder Persistenzgrenzen.
- Der Wert ist Teil von Geld-, Mengen-, Rollen-, Tenant- oder Berechtigungslogik.

---

## 12. Fehlmuster 6 — Getter/Setter als Scheinkapselung

Getter und Setter werden häufig als Kapselung missverstanden. Kapselung bedeutet jedoch nicht, dass Felder private sind und über Methoden trotzdem beliebig verändert werden können. Kapselung bedeutet, dass ein Objekt seinen gültigen Zustand schützt.

### 12.1 Schlechte Anwendung

```java
public class Account {

    private AccountStatus status;
    private BigDecimal balance;

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
```

### 12.2 Gute Anwendung

```java
public class Account {

    private AccountStatus status = AccountStatus.ACTIVE;
    private BigDecimal balance = BigDecimal.ZERO;

    public void close() {
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountCannotBeClosedException("Konto kann nur mit Saldo 0 geschlossen werden.");
        }
        this.status = AccountStatus.CLOSED;
    }

    public void credit(Money amount) {
        requireActive();
        this.balance = this.balance.add(amount.amount());
    }

    public void debit(Money amount) {
        requireActive();

        if (balance.compareTo(amount.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        this.balance = this.balance.subtract(amount.amount());
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
    }
}
```

### 12.3 Qualitätsregel

Öffentliche Setter für fachlich relevante Felder sind grundsätzlich zu vermeiden. Stattdessen MÜSSEN fachlich benannte Methoden verwendet werden.

---

## 13. Fehlmuster 7 — `instanceof` als Ersatz für Polymorphismus

`instanceof` ist nicht falsch. Pattern Matching und Sealed Classes machen Typprüfung in Java deutlich besser. Trotzdem ist eine `instanceof`-Kaskade oft ein Signal, dass Verhalten nicht dort modelliert wurde, wo es hingehört.

### 13.1 Schlechte Anwendung

```java
public BigDecimal discountFor(Customer customer, BigDecimal amount) {
    if (customer instanceof PremiumCustomer) {
        return amount.multiply(new BigDecimal("0.20"));
    }

    if (customer instanceof RegularCustomer) {
        return amount.multiply(new BigDecimal("0.05"));
    }

    if (customer instanceof NewCustomer) {
        return BigDecimal.ZERO;
    }

    throw new IllegalStateException("Unbekannter Kundentyp.");
}
```

### 13.2 Gute Anwendung mit Polymorphismus

```java
public sealed interface Customer
        permits PremiumCustomer, RegularCustomer, NewCustomer {

    BigDecimal discountFor(BigDecimal amount);
}
```

```java
public record PremiumCustomer(String id) implements Customer {

    @Override
    public BigDecimal discountFor(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.20"));
    }
}
```

```java
public record RegularCustomer(String id) implements Customer {

    @Override
    public BigDecimal discountFor(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.05"));
    }
}
```

```java
public record NewCustomer(String id) implements Customer {

    @Override
    public BigDecimal discountFor(BigDecimal amount) {
        return BigDecimal.ZERO;
    }
}
```

```java
public BigDecimal discountFor(Customer customer, BigDecimal amount) {
    return customer.discountFor(amount);
}
```

### 13.3 Wann `switch` trotzdem richtig ist

Bei geschlossenen Ergebnis- oder Zustandsmodellen kann ein exhaustiver `switch` über ein Sealed Interface richtig sein, insbesondere wenn das Verhalten bewusst außerhalb des Typs liegen soll, zum Beispiel bei API-Mapping, Darstellung, Logging-Klassifikation oder technischer Übersetzung.

```java
public String labelFor(PaymentResult result) {
    return switch (result) {
        case PaymentSucceeded succeeded -> "Erfolgreich";
        case PaymentFailed failed -> "Fehlgeschlagen";
        case PaymentPending pending -> "Ausstehend";
    };
}
```

### 13.4 Qualitätsregel

Wenn dieselbe Typentscheidung an mehreren Stellen im Code wiederholt wird, SOLLTE geprüft werden, ob Verhalten in den Typ selbst gehört oder eine Sealed-Hierarchie mit zentralem Mapping sinnvoller ist.

---

## 14. Domain Services vs. Application Services

Nicht jede Regel gehört zwingend in eine Entity. Manchmal betrifft eine fachliche Regel mehrere Aggregate oder externe fachliche Konzepte. Dann ist ein Domain Service sinnvoll.

### 14.1 Application Service

Ein Application Service koordiniert einen Use Case.

```java
public class TransferApplicationService {

    private final AccountRepository accountRepository;
    private final TransferPolicy transferPolicy;

    public TransferApplicationService(
            AccountRepository accountRepository,
            TransferPolicy transferPolicy
    ) {
        this.accountRepository = accountRepository;
        this.transferPolicy = transferPolicy;
    }

    public void transfer(TransferCommand command) {
        var source = accountRepository.findById(command.sourceAccountId())
            .orElseThrow();

        var target = accountRepository.findById(command.targetAccountId())
            .orElseThrow();

        transferPolicy.verifyAllowed(source, target, command.amount());

        source.debit(command.amount());
        target.credit(command.amount());

        accountRepository.save(source);
        accountRepository.save(target);
    }
}
```

### 14.2 Domain Service

Ein Domain Service kapselt eine fachliche Regel, die nicht natürlich zu genau einem Objekt gehört.

```java
public class TransferPolicy {

    public void verifyAllowed(Account source, Account target, Money amount) {
        if (source.sameAs(target)) {
            throw new TransferNotAllowedException("Quelle und Ziel dürfen nicht identisch sein.");
        }

        if (!source.canDebit(amount)) {
            throw new TransferNotAllowedException("Quellkonto hat nicht genug Deckung.");
        }
    }
}
```

### 14.3 Qualitätsregel

Ein Service ist problematisch, wenn er interne Zustände fremder Objekte ausliest, Entscheidungen trifft und anschließend mehrere Setter aufruft. Ein Service ist gut geschnitten, wenn er Objekte lädt, fachlich benannte Methoden aufruft und technische Abläufe koordiniert.

---

## 15. JPA und reiches Domänenmodell

JPA verhindert kein reiches Domänenmodell, macht aber bestimmte Kompromisse notwendig.

### 15.1 Zulässige Kompromisse

JPA-Entities benötigen einen public oder protected No-Arg-Konstruktor. Dieser Konstruktor kann für Framework-Zwecke geschützt sein. Fachliche Konstruktoren und Factory-Methoden können zusätzlich existieren.

```java
@Entity
public class OrderEntity {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    protected OrderEntity() {
        // Für JPA
    }

    public OrderEntity(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Eine Bestellung benötigt mindestens eine Position.");
        }
        // Initialisierung
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id, "Bereits ausgeliefert.");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

### 15.2 Nicht zulässige Ausrede

JPA ist keine Rechtfertigung für öffentliche Setter auf alle Felder. JPA benötigt Zugriff für Persistenz, aber die Fachlogik muss trotzdem Zustandsübergänge schützen.

### 15.3 Qualitätsregel

JPA-Entities mit Fachlogik SOLLTEN:

- protected No-Arg-Konstruktor verwenden
- öffentliche Setter für kritische Felder vermeiden
- Zustandsübergänge über fachliche Methoden abbilden
- Collections nicht direkt veränderbar herausgeben
- Lazy Loading nicht durch beliebige Domänenmethoden unkontrolliert auslösen
- keine Controller-, HTTP- oder DTO-Abhängigkeiten enthalten

---

## 16. Security- und SaaS-Auswirkungen

Falsche Objektorientierung ist kein reines Stilproblem. Sie kann direkte Sicherheits- und Plattformfolgen haben.

### 16.1 Mass Assignment

Wenn API-Requests direkt auf Entities gebunden werden, können Angreifer versuchen, Felder zu setzen, die nie durch den Client änderbar sein sollten.

Schlecht:

```java
@PostMapping("/users")
public void updateUser(@RequestBody UserEntity user) {
    userRepository.save(user);
}
```

Besser:

```java
public record UpdateUserProfileCommand(
    String displayName,
    String bio
) {}
```

```java
@PostMapping("/users/{id}")
public void updateUser(
        @PathVariable Long id,
        @RequestBody UpdateUserProfileCommand command
) {
    userProfileService.updateProfile(id, command);
}
```

Der Command enthält nur die Felder, die wirklich durch den Client geändert werden dürfen.

### 16.2 Tenant-Isolation

In SaaS-Plattformen darf ein Objekt seine Mandantengrenze nicht nur als zufälliges Feld besitzen, das überall gesetzt werden kann.

Schlecht:

```java
order.setTenantId(request.tenantId());
```

Besser:

```java
order.verifyBelongsTo(currentTenant);
order.cancelBy(currentTenant, actor);
```

### 16.3 Berechtigungen

Objektorientierung ersetzt keine Autorisierung. Sie kann aber verhindern, dass Zustandsänderungen an Berechtigungsprüfungen vorbeilaufen.

Besser:

```java
order.cancelBy(actor);
```

statt:

```java
if (actor.canCancel(order)) {
    order.setStatus(OrderStatus.CANCELLED);
}
```

Die Methode `cancelBy` kann zusätzlich prüfen, ob der Actor fachlich berechtigt ist. Ob die Autorisierung in der Entity, einer Policy oder einem Domain Service liegt, hängt vom Modell ab. Wichtig ist: Die Regel darf nicht beliebig über Setter umgangen werden.

### 16.4 Sensitive Data Exposure

Getter, `toString()`, Records und Mapper dürfen sensible Felder nicht unkontrolliert nach außen tragen. Ein gutes Objektmodell trennt interne Zustände, API-Responses und Logging-Repräsentationen.

---

## 17. Framework- und Plattform-Kontext

### 17.1 Spring Boot

Spring Services SOLLTEN Use Cases koordinieren. Sie DÜRFEN fachliche Policies aufrufen, Repositories verwenden und Transaktionen steuern. Sie SOLLTEN jedoch nicht alle fachlichen Regeln enthalten, wenn dadurch Domänenobjekte ihre Verantwortung verlieren.

### 17.2 Records

Records sind hervorragend für Value Objects und DTOs, wenn keine JPA-Entity-Anforderungen bestehen. Für JPA-Entities sind Records in dieser Richtlinie nicht vorgesehen.

### 17.3 Sealed Classes

Sealed Interfaces eignen sich für geschlossene Domänenvarianten, beispielsweise Payment Results, Statusübergänge oder fachliche Ergebnisobjekte.

### 17.4 MapStruct und Mapper

Mapper sind sinnvoll für DTO-Entity-Transformationen. Sie dürfen jedoch keine versteckte Business-Logik enthalten. Ein Mapper übersetzt Strukturen. Er entscheidet nicht über Berechtigungen, Statusübergänge oder fachliche Gültigkeit.

### 17.5 Reflection

Reflection ist in Frameworks üblich. Im Anwendungscode ist Reflection kein Ersatz für klare Schnittstellen, Value Objects oder Mapper. Direkter Zugriff auf private Felder im Business-Code ist grundsätzlich zu vermeiden.

---

## 18. Designregeln

### 18.1 Regel: Verhalten gehört zur Verantwortung

Eine Methode gehört zu dem Typ, dessen fachlichen Zustand sie verändert oder dessen Invariante sie schützt.

### 18.2 Regel: Keine öffentlichen Setter für kritische Felder

Felder wie `status`, `role`, `tenantId`, `price`, `amount`, `balance`, `ownerId`, `plan`, `permissions`, `deleted`, `verified`, `active` oder `locked` DÜRFEN NICHT beliebig per Setter veränderbar sein.

### 18.3 Regel: Value Objects für wertvolle Werte

E-Mail-Adressen, Geldbeträge, Mengen, Tenant-IDs, User-IDs, Product-IDs, Rollennamen und externe Referenzen SOLLTEN eigene Typen erhalten, wenn Regeln oder Verwechslungsgefahr bestehen.

### 18.4 Regel: Komposition vor Vererbung

Vererbung benötigt eine echte fachliche Substituierbarkeit. Für Wiederverwendung von Verhalten ist Komposition der Standard.

### 18.5 Regel: Keine Utility-Müllhalden

Utility-Klassen müssen klein, technisch und kohärent sein. Fachliche Hilfsmethoden gehören in Value Objects, Domain Services oder fachlich benannte Komponenten.

### 18.6 Regel: Services nicht mit Domänenobjekten verwechseln

Ein Service ist kein Ersatz für ein Modell. Ein Service orchestriert. Ein Domänenobjekt schützt Zustand. Eine Policy entscheidet fachlich. Ein Repository lädt und speichert.

---

## 19. Entscheidungsbaum

```text
Hat der Typ fachliche Invarianten?
├─ Nein
│  ├─ Ist es ein DTO oder Read Model?
│  │  └─ Record oder einfache Klasse verwenden.
│  └─ Ist es technische Infrastruktur?
│     └─ Klare Komponente oder Adapter verwenden.
└─ Ja
   ├─ Hat der Typ Identität und Lifecycle?
   │  └─ Entity oder Aggregate Root mit Verhalten modellieren.
   ├─ Ist es ein unveränderlicher Wert?
   │  └─ Value Object verwenden, häufig als Record.
   ├─ Betrifft die Regel mehrere Aggregate?
   │  └─ Domain Service oder Policy verwenden.
   └─ Gibt es eine geschlossene Menge von Varianten?
      └─ Sealed Interface / Sealed Class prüfen.
```

---

## 20. Gute und falsche Anwendung im Überblick

| Aspekt | Falsche Anwendung | Gute Anwendung | Beispiel |
|---|---|---|---|
| Zustandsänderung | `setStatus(CANCELLED)` | `cancel()` | Bestellung schützt ihre Übergänge selbst |
| Geldbetrag | `BigDecimal amount, String currency` | `Money amount` | Währung und Betrag werden gemeinsam validiert |
| Mandant | `setTenantId(...)` | `verifyBelongsTo(tenant)` oder tenantgebundene Factory | Tenant-Isolation wird nicht überschrieben |
| Verhalten | `OrderUtils.calculateTotal(order)` | `order.total()` | Verhalten liegt am fachlichen Objekt |
| Wiederverwendung | abstrakte Basisklasse | Komposition über Interfaces | Sender + Logger + RetryPolicy |
| API-Binding | `@RequestBody Entity` | Command DTO | Schutz gegen Mass Assignment |
| Statusmodell | offenes Interface + `instanceof` überall | Sealed Type oder Polymorphismus | Vollständigkeit wird sichtbar |
| Service | God Service | Use-Case-Service + Domainmodell | Registrierung, Profil, Billing getrennt |

---

## 21. Review-Checkliste

Ein Pull Request erfüllt diese Richtlinie nur, wenn folgende Fragen sauber beantwortet werden können:

- Hat jede Klasse eine klar benannte Verantwortung?
- Enthält eine Klasse mehrere unabhängige Themenbereiche?
- Gibt es öffentliche Setter für fachlich kritische Felder?
- Werden Zustandsübergänge über fachlich benannte Methoden modelliert?
- Gibt es Utility-Klassen, die eigentlich Value Objects oder Domain Services sein sollten?
- Werden primitive Typen für fachlich unterschiedliche Werte verwendet?
- Können `userId`, `productId`, `tenantId`, `accountId` oder ähnliche Werte verwechselt werden?
- Werden Entities direkt als API-Request oder API-Response verwendet?
- Werden interne Collections veränderbar herausgegeben?
- Enthält ein Service Logik, die klar zu einem Domänenobjekt gehört?
- Enthält ein Mapper fachliche Entscheidungen?
- Wird Vererbung nur für Code-Wiederverwendung verwendet?
- Gibt es `instanceof`-Kaskaden, die Polymorphismus oder Sealed Types ersetzen sollten?
- Ist bei JPA-Entities ein protected No-Arg-Konstruktor vorhanden?
- Sind JPA-Kompromisse sichtbar, aber die Fachregeln trotzdem geschützt?
- Ist Tenant-Isolation explizit modelliert?
- Sind sensible Felder von API-Responses, Logs und `toString()` getrennt?
- Können die wichtigsten Regeln ohne Spring Context als Unit Tests geprüft werden?

---

## 22. Automatisierbare Prüfungen

Nicht alle OOP-Regeln lassen sich vollständig automatisieren. Dennoch SOLLTEN folgende Prüfungen eingesetzt oder geprüft werden:

### 22.1 ArchUnit

Mögliche Regeln:

```java
// Beispielidee, projektbezogen anzupassen:
// Controller dürfen nicht direkt auf Repositories zugreifen.
// Controller dürfen keine Entities zurückgeben.
// Services dürfen nicht von Controllern abhängen.
// Domain-Objekte dürfen nicht von Web-Typen abhängen.
```

### 22.2 Checkstyle / PMD / SonarQube

Mögliche Prüfziele:

- Klassen mit zu vielen Methoden
- Klassen mit zu vielen Feldern
- hohe zyklomatische Komplexität
- Utility-Klassen mit vielen fachlich gemischten Methoden
- öffentliche Setter in Domain-Packages
- leere Catch-Blöcke
- übermäßige `instanceof`-Nutzung
- direkte Verwendung von Entities in Controller-Signaturen

### 22.3 Semgrep

Mögliche projektbezogene Suchmuster:

```yaml
rules:
  - id: no-entity-request-body
    message: "Entities dürfen nicht direkt als @RequestBody verwendet werden."
    severity: ERROR
    patterns:
      - pattern: |
          public $RET $METHOD(@RequestBody $ENTITY $ARG, ...) { ... }
```

Diese Regel ist nur eine Skizze und muss an eure Paketnamen und Entity-Konventionen angepasst werden.

---

## 23. Migration und Refactoring

### 23.1 Vorgehen bei anämischen Entities

1. Kritische Fachregeln identifizieren.
2. Setter-Nutzung suchen.
3. Tests für aktuelles Verhalten ergänzen.
4. Fachlich benannte Methoden am Objekt einführen.
5. Aufrufer schrittweise von Setter-Sequenzen auf Methoden umstellen.
6. Setter für kritische Felder entfernen oder Sichtbarkeit reduzieren.
7. DTOs und API-Verträge von Entities trennen.
8. Regressionstests ausführen.
9. ArchUnit- oder Review-Regel ergänzen.

### 23.2 Vorgehen bei God Services

1. Methoden thematisch gruppieren.
2. Use Cases identifizieren.
3. Fachliche Regeln von technischer Orchestrierung trennen.
4. Kleine Application Services extrahieren.
5. Value Objects für wiederkehrende Primitive einführen.
6. Policies oder Domain Services für objektübergreifende Regeln extrahieren.
7. Tests umstellen: erst Charakterisierungstests, dann gezielte Unit Tests.

### 23.3 Vorgehen bei Primitive Obsession

1. Werte mit Validierungslogik identifizieren.
2. Eigene Typen einführen.
3. API-/DTO-Grenzen bewusst mappen.
4. Persistenzmapping prüfen.
5. Verwechslungstests und Validierungstests ergänzen.

---

## 24. Ausnahmen

Abweichungen sind zulässig, wenn sie begründet sind.

Akzeptable Ausnahmen:

- reine CRUD-Verwaltung ohne fachliche Zustandsregeln
- technische DTOs und Read Models
- Performancekritische Hot Paths nach Messung
- Framework-Anforderungen
- Legacy-Code während kontrollierter Migration
- bewusst funktionaler Code, wenn er klar, testbar und sicher bleibt

Nicht akzeptable Ausnahmen:

- „Das war schneller.“
- „Das haben wir immer so gemacht.“
- „Setter sind einfacher.“
- „Der Service kennt die Regel ja.“
- „Die Entity ist nur eine Tabelle.“
- „Das prüfen wir im Controller.“

---

## 25. Definition of Done

Code erfüllt diese Richtlinie, wenn:

- fachliche Invarianten nicht über öffentliche Setter umgangen werden können
- Zustandsübergänge über fachlich benannte Methoden erfolgen
- Services Use Cases orchestrieren, aber nicht unnötig das Domänenmodell ersetzen
- Value Objects für relevante fachliche Werte verwendet werden
- Primitive Obsession bei sicherheits- oder fachkritischen Werten vermieden wird
- Utility-Klassen nicht als Ablageort für Fachlogik dienen
- Vererbung fachlich begründet ist oder durch Komposition ersetzt wurde
- Entities nicht direkt als API-Requests oder API-Responses verwendet werden
- Tenant-, Rollen-, Status-, Geld- und Mengenregeln explizit modelliert sind
- sensible Daten nicht unkontrolliert durch Getter, Mapper oder `toString()` offengelegt werden
- die wichtigsten Fachregeln durch Unit Tests ohne Spring Context prüfbar sind
- Pull-Request-Review die Checkliste dieser Richtlinie berücksichtigt

---

## 26. Quellen und weiterführende Literatur

- Martin Fowler: Anemic Domain Model  
  https://martinfowler.com/bliki/AnemicDomainModel.html

- Oracle Java SE 21: Record Classes  
  https://docs.oracle.com/en/java/javase/21/language/records.html

- Oracle Java SE 21: Sealed Classes and Interfaces  
  https://docs.oracle.com/en/java/javase/21/language/sealed-classes-and-interfaces.html

- Oracle Java SE 21 API: `java.lang.Record`  
  https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html

- Jakarta Persistence 3.1 Specification, Entity Class  
  https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html

- OWASP Cheat Sheet Series: Mass Assignment Cheat Sheet  
  https://cheatsheetseries.owasp.org/cheatsheets/Mass_Assignment_Cheat_Sheet.html

- OWASP Web Security Testing Guide: Testing for Mass Assignment  
  https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/07-Input_Validation_Testing/20-Testing_for_Mass_Assignment

- OWASP API Security 2019: API6 Mass Assignment  
  https://owasp.org/API-Security/editions/2019/en/0xa6-mass-assignment/

- Eric Evans: Domain-Driven Design: Tackling Complexity in the Heart of Software. Addison-Wesley, 2003.

- Martin Fowler: Patterns of Enterprise Application Architecture. Addison-Wesley, 2002.

- Joshua Bloch: Effective Java. 3rd Edition. Addison-Wesley, 2018.
