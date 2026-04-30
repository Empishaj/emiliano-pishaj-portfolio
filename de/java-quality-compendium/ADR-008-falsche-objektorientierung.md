# ADR-008 — Falsche Objektorientierung: OOP richtig verstehen und anwenden

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21                                |
| Datum      | 2024-03-03                        |
| Kategorie  | OOP / Design Principles           |

---

## Kontext & Problem

Java ist eine objektorientierte Sprache — aber "objektorientiert" wird häufig auf "ich benutze Klassen" reduziert. Die eigentlichen Prinzipien — Kapselung, Kohäsion, Verhalten beim Objekt — werden ignoriert. Das Resultat sind Klassen, die wie Datenbehälter mit statischen Hilfsfunktionen aussehen, statt wie echte Objekte mit Identität und Verantwortung.

Dieses ADR benennt die häufigsten OOP-Fehlmuster in Java-Projekten und zeigt die korrekte Umsetzung.

---

## Fehler 1 — Anämisches Domänenmodell (Anemic Domain Model)

Das verbreitetste OOP-Anti-Pattern in Java: Klassen als reine Datenbehälter, alle Logik in Service-Klassen ausgelagert.

### Schlecht — Anämische Entity, fette Service-Klasse

```java
// Die Entity ist ein dummer Datenbehälter — nur Getter/Setter, null Logik
@Entity
public class Order {
    private Long id;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal total;

    // Nur Getter und Setter — kein Verhalten, keine Validierung
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotal(BigDecimal total)    { this.total = total; }
    // ... 20 weitere Getter/Setter
}

// Alle Logik lebt im Service — die Entity weiß nichts über sich selbst
@Service
public class OrderService {

    public void cancelOrder(Order order) {
        // Logik die zur Order gehört, lebt im Service
        if (order.getStatus() == DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }
        if (order.getStatus() == CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        order.setStatus(CANCELLED);
        order.setTotal(BigDecimal.ZERO);
        // ... weitere Zustandsänderungen, die NUR zur Order gehören
    }

    public BigDecimal calculateTotal(Order order) {
        // Auch das gehört zur Order, nicht zum Service
        return order.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

**Warum ist das schlecht?**
- Die Business-Regeln ("Delivered Orders können nicht storniert werden") sind vom Objekt getrennt, das diese Regel kennen sollte.
- Jeder Service kann `setStatus()` aufrufen und ungültige Zustände erzeugen — keine Invarianten geschützt.
- Kapselung verletzt: interne Struktur (items, total) nach außen exponiert, jeder kann sie ändern.
- Verhalten ist nicht auffindbar — in welchem der 20 Services steckt die Stornierungslogik?

### Gut — Reiches Domänenmodell: Verhalten beim Objekt

```java
@Entity
public class Order {

    @Id
    private Long id;

    @OneToMany(cascade = ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    @Enumerated(STRING)
    private OrderStatus status = PENDING;

    // Verhalten gehört zur Order — sie schützt ihre eigenen Invarianten
    public void cancel() {
        if (status == DELIVERED) {
            throw new OrderCannotBeCancelledException(id, "Order already delivered");
        }
        if (status == CANCELLED) {
            throw new OrderCannotBeCancelledException(id, "Order already cancelled");
        }
        this.status = CANCELLED;
        // Weitere konsistente Zustandsänderungen hier — niemals von außen
    }

    public void addItem(Product product, int quantity) {
        if (status != PENDING) {
            throw new OrderModificationException(id, "Only pending orders can be modified");
        }
        items.add(new OrderItem(product, quantity));
    }

    // Berechnetes Attribut — kein Setter, kein Service nötig
    public BigDecimal total() {
        return items.stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Kein setStatus() von außen — Zustandsübergänge nur durch Methoden mit Namen
    public OrderStatus status() { return status; }
}

// Der Service koordiniert nur noch — keine Logik
@Service
public class OrderService {
    public void cancelOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel(); // Logik lebt in der Order
        orderRepository.save(order);
    }
}
```

---

## Fehler 2 — Utility-Klassen als OOP-Ersatz

### Schlecht — statische Hilfsmethoden-Friedhöfe

```java
// Eine Klasse die "alles" kann — Friedhof für statische Methoden
public class UserUtils {
    public static boolean isValidEmail(String email) { ... }
    public static String formatName(String first, String last) { ... }
    public static boolean isAdult(LocalDate birthDate) { ... }
    public static String maskEmail(String email) { ... }
    public static boolean hasPermission(User user, String action) { ... }
    // 40 weitere statische Methoden...
}

// Verwendung: kein Objekt, kein Kontext, keine Kohäsion
if (UserUtils.isValidEmail(email) && UserUtils.isAdult(birthDate)) {
    UserUtils.hasPermission(user, "CREATE_ORDER");
}
```

**Warum ist das schlecht?**
- `UserUtils`, `StringHelper`, `DateUtil` sind Code-Müllhalden ohne echte Verantwortung.
- Statische Methoden sind nicht mockbar — Tests werden zu Integrationstests.
- "Utils" im Klassenname ist ein Warnsignal: die Klasse hat keine kohärente Identität.
- Keine Wiederverwendung möglich — alle Methoden sind funktional unverbunden.

### Gut — Verhalten gehört zum Wert-Objekt

```java
// Email ist ein Wert-Objekt, das sich selbst validiert und Verhalten hat
public record Email(String value) {

    public Email {
        Objects.requireNonNull(value);
        if (!value.contains("@") || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException(value);
        }
        value = value.trim().toLowerCase(); // Normalisierung im Konstruktor
    }

    public String masked() {
        int atIndex = value.indexOf('@');
        return value.substring(0, 1) + "***" + value.substring(atIndex);
    }
}

// Age-Logik gehört zum Konzept "Alter", nicht zu einem Utils
public record Age(LocalDate birthDate) {

    public boolean isAdult() {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }

    public int years() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}

// Verwendung: ausdrucksstark, typsicher, testbar
var email = new Email("user@example.com");
var age   = new Age(LocalDate.of(1990, 5, 15));

if (age.isAdult()) {
    System.out.println("Kontakt: " + email.masked());
}
```

---

## Fehler 3 — Vererbung statt Komposition

### Schlecht — Vererbung für Code-Wiederverwendung

```java
// Vererbung um Methoden zu "erben" — konzeptuell falsch
public class EmailNotification extends BaseNotification {
    // Erbt send(), log(), retry() aus BaseNotification
    // Aber: ist eine EmailNotification wirklich eine BaseNotification?
    // Oder nutzt sie nur ihre Infrastruktur?
}

public class SmsNotification extends BaseNotification {
    @Override
    public void send() {
        super.send(); // Fragile Base Class Problem
        // ...
    }
}
```

**Warum ist das schlecht?**
- Vererbung ist die stärkste Kopplung in OOP — Änderungen in `BaseNotification` brechen alle Subklassen.
- "Fragile Base Class": jede Änderung der Basisklasse kann Subklassen unbeabsichtigt kaputt machen.
- Tiefe Vererbungshierarchien (Base → Abstract → Concrete → Override) sind kaum nachvollziehbar.

### Gut — Komposition: Verhalten zusammensetzen

```java
// Interfaces definieren Fähigkeiten — keine Implementierung vererbt
interface NotificationSender {
    void send(Notification notification);
}

interface NotificationLogger {
    void log(Notification notification, Result result);
}

// Komposition: jede Klasse hat ihre Abhängigkeiten — keine Vererbung
public class EmailNotificationSender implements NotificationSender {

    private final EmailClient      emailClient;
    private final NotificationLogger logger;  // ← Komposition statt Vererbung

    public EmailNotificationSender(EmailClient emailClient,
                                   NotificationLogger logger) {
        this.emailClient = emailClient;
        this.logger      = logger;
    }

    @Override
    public void send(Notification notification) {
        var result = emailClient.send(notification.recipient(), notification.body());
        logger.log(notification, result); // Verhalten wiederverwendet durch Komposition
    }
}

// Jede Klasse ist unabhängig testbar, austauschbar, ersetzbar
```

---

## Fehler 4 — God Class: Eine Klasse kennt alles

### Schlecht — eine Klasse mit zu vielen Verantwortlichkeiten

```java
// UserManager macht alles: Auth, Profil, Einstellungen, Billing, Notifications...
public class UserManager {
    public User register(String name, String email, String password) { ... }
    public User login(String email, String password) { ... }
    public void logout(String sessionId) { ... }
    public void updateProfile(Long userId, String name, String bio) { ... }
    public void changePassword(Long userId, String oldPw, String newPw) { ... }
    public void updateNotificationSettings(Long userId, boolean email, boolean sms) { ... }
    public void upgradeToPremium(Long userId, String paymentToken) { ... }
    public void cancelSubscription(Long userId) { ... }
    public void sendVerificationEmail(Long userId) { ... }
    // ... 30 weitere Methoden
}
```

### Gut — Single Responsibility: je eine Klasse, je eine Verantwortung

```java
// Jeder Service hat eine klar benannte, begrenzte Verantwortung
@Service public class UserRegistrationService  { ... } // Nur: Registration
@Service public class UserAuthenticationService { ... } // Nur: Login/Logout/Session
@Service public class UserProfileService        { ... } // Nur: Profil & Einstellungen
@Service public class SubscriptionService       { ... } // Nur: Billing & Plan
@Service public class UserVerificationService   { ... } // Nur: E-Mail-Verifikation

// Faustregel: Wenn "und" im Klassenname steht oder im Beschreibungssatz nötig ist,
// hat die Klasse zu viele Verantwortlichkeiten.
```

---

## Fehler 5 — Primitive Obsession

### Schlecht — alles ist String oder int

```java
// Alles ist primitiv — kein Typ, kein Schutz, keine Dokumentation
public void createOrder(Long userId, String productId, int quantity, String currency, BigDecimal price) {
    // Können userId und productId verwechselt werden? Ja!
    // Ist quantity 0 oder negativ gültig? Unbekannt.
    // Ist "EUR" ein gültiger currency-Wert? Weiß niemand.
}

// Aufruf — verwechslungsanfällig
createOrder(productId, userId, -1, "EURO", BigDecimal.valueOf(-10)); // Kompiliert, ist Quatsch
```

### Gut — Domänentypen statt Primitive

```java
// Jeder Wert hat seinen eigenen Typ — falsche Belegung schlägt beim Compiler an
public record UserId(Long value) {
    public UserId { if (value <= 0) throw new IllegalArgumentException("UserId must be positive"); }
}

public record ProductId(String value) {
    public ProductId { Objects.requireNonNull(value); }
}

public record Quantity(int value) {
    public Quantity { if (value <= 0) throw new IllegalArgumentException("Quantity must be positive"); }
}

public record Money(BigDecimal amount, Currency currency) {
    public Money { if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount cannot be negative"); }
}

// Aufruf — typsicher, selbstdokumentierend, verwechslungssicher
public void createOrder(UserId userId, ProductId productId, Quantity quantity, Money price) { ... }

// Compiler verhindert Verwechslung:
createOrder(new ProductId("p1"), new UserId(42L), ...); // ← Compile-Fehler ✓
```

---

## Konsequenzen

**Positiv:**
- Echte Kapselung: Invarianten werden von den Objekten selbst geschützt — kein "vergessener" Validierungsaufruf im Service.
- Testbarkeit: Objekte mit Verhalten können isoliert getestet werden.
- Auffindbarkeit: Logik lebt dort, wo sie hingehört — im Objekt.

**Negativ:**
- Erfordert einen kulturellen Wandel im Team: weg von "Data + Utils", hin zu "Objekte mit Verhalten".
- JPA-Entities mit reichem Modell erfordern etwas mehr Sorgfalt bei Lazy Loading und Transaktionsgrenzen.

---

## Tipps

- **Test als Indikator**: Muss ein Test 5 Mocks aufsetzen, um eine Methode zu testen? Das Objekt hat zu viele Abhängigkeiten — Single Responsibility verletzt.
- **"Tell, don't ask"**: Objekte sollen Dinge tun, nicht ihre Daten rausgeben damit jemand anderes entscheidet. `order.cancel()` statt `if (order.getStatus() == PENDING) order.setStatus(CANCELLED)`.
- **Komposition über Vererbung**: Wann ist Vererbung richtig? Wenn ein echtes "ist ein"-Verhältnis besteht und die Liskov Substitution gilt. Sonst: Interface + Komposition.
- **Primitive Obsession Test**: Kann ein `Long` als `userId` an eine Methode übergeben werden, die `productId` erwartet? Dann fehlt ein Domänentyp.
 