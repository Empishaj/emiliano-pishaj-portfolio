# ADR-023 — Domain-Driven Design: Aggregate, Bounded Context & Ubiquitous Language

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x                             |
| Datum      | 2026-02-21                                        |
| Kategorie  | Architektur / Domain Design                       |

---

## Kontext & Problem

Ohne klare Domänenmodellierung wächst Software zur "Big Ball of Mud": alles hängt mit allem zusammen, niemand versteht die Grenzen, Änderungen haben unvorhersehbare Auswirkungen. Domain-Driven Design (DDD) bietet ein Vokabular und Muster für strukturierte Domänenmodellierung.

---

## Konzept 1 — Ubiquitous Language: Eine Sprache für Code und Gespräch

### Schlecht — technische Namen statt Domänensprache

```java
// Code spricht andere Sprache als die Domäne
public class DataRecord {         // Was ist ein "DataRecord"?
    private String str1;          // Keine Ahnung was das ist
    private int num1;
    private boolean flag;
}

public class UserDataProcessor {  // "Processor" ist technisch, nicht fachlich
    public void processUserData(DataRecord rec) { ... }
}

// Im Meeting sagt der Product Owner "Bestellung" —
// Im Code steht DataRecord, OrderEntry, PurchaseObject, BuyRequest...
```

### Gut — Code spiegelt die Domänensprache

```java
// Code klingt wie das fachliche Gespräch
public class Order {              // "Bestellung" → Order
    private Money totalAmount;    // "Gesamtbetrag" → totalAmount
    private int itemCount;        // "Artikelanzahl" → itemCount
    private boolean isPaid;       // "bezahlt" → isPaid
}

// Methoden sprechen die Sprache des Domänenexperten:
order.place();          // "Bestellung aufgeben"
order.cancel();         // "Bestellung stornieren"
order.confirm();        // "Bestellung bestätigen"
order.ship();           // "Bestellung versenden"
// Kein: order.setStatus(4), order.processState(), order.updateData()
```

---

## Konzept 2 — Aggregate: Konsistenzgrenzen

Ein Aggregate ist eine Gruppe von Objekten, die immer **konsistent** sein müssen. Das Aggregate-Root kontrolliert alle Änderungen.

### Schlecht — kein Aggregate, direkte Manipulation

```java
// Direkte Manipulation von internen Objekten — Konsistenz unkontrollierbar
@Service
public class OrderItemService {

    public void addItem(Long orderId, Long productId, int quantity) {
        var order = orderRepository.findById(orderId).orElseThrow();
        var item  = new OrderItem(productId, quantity);

        // Direkt ins interne List-Feld — Order weiß nichts davon!
        order.getItems().add(item); // ← Umgeht alle Validierungen der Order

        // Order-Invariante verletzt: vielleicht war die Bestellung bereits bestätigt?
        // Vielleicht überschreitet die Menge das Limit?
        // Der Service weiß es nicht — die Order wusste es, aber wurde umgangen.
        orderItemRepository.save(item); // Items direkt gespeichert ohne Order
    }
}
```

### Gut — Aggregate-Root kontrolliert alle Änderungen

```java
// Order ist das Aggregate-Root — alle Änderungen gehen durch sie
@Entity
public class Order {  // ← Aggregate-Root

    @Id private OrderId id;

    @OneToMany(cascade = ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>(); // Interna: kapselt

    private OrderStatus status = PENDING;
    private int maxItems = 10; // Geschäftsregel: max 10 Artikel

    // Einziger Einstiegspunkt zum Hinzufügen — Invarianten werden hier geschützt
    public OrderItem addItem(ProductId productId, Quantity quantity) {
        if (status != PENDING) {
            throw new OrderModificationException(id, status);
        }
        if (items.size() >= maxItems) {
            throw new OrderItemLimitExceededException(id, maxItems);
        }
        var item = new OrderItem(productId, quantity);
        items.add(item);
        return item;
        // Kein direkter Zugriff auf items.add() von außen!
    }

    // Lesen: nur unveränderliche Kopie nach außen
    public List<OrderItem> items() {
        return Collections.unmodifiableList(items);
    }
}

// Repository nur für Aggregate-Roots — KEIN OrderItemRepository!
public interface OrderRepository extends JpaRepository<Order, OrderId> { }
// OrderItem lebt und stirbt mit seiner Order — kein eigenes Repository nötig
```

---

## Konzept 3 — Bounded Context: Grenzen ziehen

Verschiedene Teile des Systems haben verschiedene Modelle desselben Konzepts — und das ist richtig so.

### Schlecht — ein User-Objekt für alle Kontexte

```java
// Ein monolithisches User-Objekt das alles kann
@Entity
public class User {
    // Identity-Kontext:
    private String email;
    private String passwordHash;
    private String mfaSecret;
    private List<Role> roles;

    // Profil-Kontext:
    private String displayName;
    private String bio;
    private String avatarUrl;

    // Billing-Kontext:
    private String stripeCustomerId;
    private SubscriptionTier subscriptionTier;
    private LocalDate subscriptionExpiry;

    // Notification-Kontext:
    private boolean emailNotifications;
    private boolean smsNotifications;
    private String phoneNumber;

    // 50 weitere Felder aus 8 verschiedenen Kontexten...
}
// Jeder Service modifiziert dieses Objekt — niemand versteht mehr was was ist
```

### Gut — jeder Bounded Context hat sein eigenes Modell

```java
// Identity Context: nur was für Authentication nötig ist
package com.example.identity;

@Entity
public class UserAccount {
    private UserId     id;
    private Email      email;
    private String     passwordHash;
    private List<Role> roles;
    private boolean    mfaEnabled;
}

// Order Context: User ist nur eine Referenz-ID — kein voller User
package com.example.ordering;

@Entity
public class Order {
    private OrderId orderId;
    private UserId  customerId; // ← nur die ID, kein User-Objekt!
    // Der Order-Kontext braucht kein Passwort, kein Avatar, kein Stripe-ID
}

// Notification Context: eigenes, fokussiertes Modell
package com.example.notification;

public record NotificationPreferences(
    UserId  userId,
    boolean emailEnabled,
    boolean smsEnabled,
    String  phoneNumber
) {}
```

```
Bounded Context Map:

┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│   Identity      │     │    Ordering      │     │    Notification    │
│   Context       │     │    Context       │     │    Context         │
│                 │     │                  │     │                    │
│ UserAccount     │─ID─▶│ Order            │─ID─▶│ NotificationPref   │
│ Role            │     │ OrderItem        │     │ NotificationEvent  │
│ Credential      │     │ Payment          │     │                    │
└─────────────────┘     └──────────────────┘     └────────────────────┘
        ↑ Anti-Corruption Layer (ACL) wenn nötig
```

---

## Konzept 4 — Domain Events: Kontexte entkoppeln

```java
// Statt direktem Service-Aufruf: Domain Event als Integrationskanal
@Entity
public class Order {

    // Domainlogik registriert Events — keine direkte Abhängigkeit auf andere Kontexte
    @DomainEvents
    Collection<Object> domainEvents() {
        return List.of(new OrderPlacedEvent(id, customerId, total(), Instant.now()));
    }

    @AfterDomainEventPublication
    void clearEvents() { /* Events nach Veröffentlichung löschen */ }
}

// Notification-Kontext reagiert auf Events — unabhängig, austauschbar
@Component
class OrderPlacedNotificationHandler {

    @EventListener
    @Async
    void onOrderPlaced(OrderPlacedEvent event) {
        // Notification-Kontext hat kein direktes Wissen über Order-Interna
        notificationService.sendOrderConfirmation(event.customerId(), event.orderId());
    }
}
```

---

## Konzept 5 — Value Objects statt Primitive (→ ADR-008)

```java
// Geldbetrag ist kein BigDecimal — er hat Währung und Validierung
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.scale() > 2) throw new IllegalArgumentException("Max 2 Dezimalstellen");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Kein negativer Betrag");
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) throw new CurrencyMismatchException(currency, other.currency);
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }
}
```

---

## Konsequenzen

**Positiv:** Bounded Contexts erlauben unabhängige Entwicklung und Deployment. Aggregate-Roots garantieren Konsistenz ohne verteilte Transaktionen. Domain Events entkoppeln Kontexte lose und ermöglichen Event Sourcing.

**Negativ:** DDD erfordert tiefes Domänenverständnis — funktioniert nicht ohne enge Zusammenarbeit mit Domänenexperten. Overhead bei kleinen, einfachen Domänen.

---

## Tipps

- **Event Storming**: Workshop-Format um Bounded Contexts und Domain Events gemeinsam mit Domänenexperten zu identifizieren.
- **Repository pro Aggregate-Root**: Nie ein Repository für interne Aggregate-Objekte — das ist ein Zeichen für fehlende Kapselung.
- **Anti-Corruption Layer (ACL)**: Wenn Kontext A Daten aus Kontext B braucht, niemals direkt das Modell von B übernehmen — Übersetzer-Schicht dazwischen.
- **Starte nicht mit DDD**: Erst wenn die Domäne komplex genug ist. Für einfache CRUD-Anwendungen ist DDD Overengineering.
