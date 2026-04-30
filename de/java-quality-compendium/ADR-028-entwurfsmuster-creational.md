# ADR-028 — Entwurfsmuster: Creational Patterns (Erzeugungsmuster)

| Feld       | Wert                          |
|------------|-------------------------------|
| Status     | ✅ Akzeptiert                 |
| Java       | 21                            |
| Datum      | 2024-01-01                    |
| Kategorie  | Design Patterns / GoF         |

---

## Kontext & Problem

Erzeugungsmuster lösen das Problem "Wie erzeuge ich Objekte?" auf eine Art die flexibel, testbar und entkoppelt ist. Sie trennen die Entscheidung was erzeugt wird von der Entscheidung wie und wo es erzeugt wird.

---

## Factory Method

**Wann**: Eine Klasse soll Objekte erzeugen, aber die konkrete Klasse erst zur Laufzeit entscheiden.

### ❌ Schlecht — direkte Instanzierung mit Switch

```java
public class NotificationSender {
    public void send(String type, String message) {
        // Neuer Typ → diese Methode muss geändert werden (verletzt OCP)
        switch (type) {
            case "EMAIL" -> new EmailNotification(message).send();
            case "SMS"   -> new SmsNotification(message).send();
            case "PUSH"  -> new PushNotification(message).send();
        }
    }
}
```

### ✅ Gut — Factory Method

```java
public interface Notification {
    void send(String message);
}

public abstract class NotificationFactory {
    // Factory Method — Subklassen entscheiden was erzeugt wird
    public abstract Notification create(NotificationChannel channel);

    public void sendTo(NotificationChannel channel, String message) {
        create(channel).send(message); // Nutzt Factory Method
    }
}

@Component
public class SpringNotificationFactory extends NotificationFactory {
    private final Map<NotificationChannel, Notification> providers;

    @Override
    public Notification create(NotificationChannel channel) {
        return providers.getOrDefault(channel, new NoOpNotification());
    }
}
```

---

## Builder

**Wann**: Ein Objekt hat viele optionale Parameter oder einen komplexen Konstruktionsprozess.

### ❌ Schlecht — Teleskop-Konstruktoren

```java
// Welche Parameter sind was? In welcher Reihenfolge?
new Order(userId, productId, quantity, shippingAddress,
          billingAddress, couponCode, priority, giftMessage);
// 8 Parameter — unleserlich, fehleranfällig
```

### ✅ Gut — Builder Pattern

```java
// In Java 21: oft Records mit Builder-Methoden oder Lombok @Builder
// Hier: expliziter Builder für komplexe Konstruktion mit Validierung

public final class Order {
    private final UserId        userId;
    private final List<OrderItem> items;
    private final Address       shippingAddress;
    private final Address       billingAddress;   // optional
    private final String        couponCode;        // optional
    private final boolean       isGift;

    private Order(Builder builder) { /* alle Felder aus Builder */ }

    public static Builder forUser(UserId userId) {
        return new Builder(userId);
    }

    public static class Builder {
        private final UserId userId;
        private final List<OrderItem> items = new ArrayList<>();
        private Address shippingAddress;
        private Address billingAddress;
        private String  couponCode;
        private boolean isGift = false;

        private Builder(UserId userId) { this.userId = Objects.requireNonNull(userId); }

        public Builder addItem(ProductId product, Quantity qty) {
            items.add(new OrderItem(product, qty)); return this;
        }
        public Builder shippingTo(Address address)  { this.shippingAddress = address; return this; }
        public Builder billingTo(Address address)   { this.billingAddress  = address; return this; }
        public Builder withCoupon(String code)      { this.couponCode      = code;    return this; }
        public Builder asGift()                     { this.isGift          = true;    return this; }

        public Order build() {
            Objects.requireNonNull(shippingAddress, "shippingAddress required");
            if (items.isEmpty()) throw new IllegalStateException("Order needs at least one item");
            return new Order(this);
        }
    }
}

// Aufruf: selbstdokumentierend
var order = Order.forUser(userId)
    .shippingTo(homeAddress)
    .addItem(bookId,  Quantity.of(2))
    .addItem(penId,   Quantity.of(1))
    .withCoupon("SAVE10")
    .asGift()
    .build();
```

---

## Singleton

**Wann**: Genau eine Instanz einer Klasse soll existieren (z. B. Konfiguration, Registry).

### ❌ Schlecht — klassisches Singleton mit statischer Methode

```java
// Probleme: nicht testbar, globaler Zustand, Thread-Safety fraglich
public class ConfigurationManager {
    private static ConfigurationManager instance;

    private ConfigurationManager() { }

    public static ConfigurationManager getInstance() {
        if (instance == null) {                // ❌ Race Condition in Multi-Threading!
            instance = new ConfigurationManager();
        }
        return instance;
    }
}
```

### ✅ Gut — Spring Bean ist das moderne Singleton

```java
// Spring verwaltet Lebenszeit — kein eigener Singleton-Code
@Configuration
public class AppConfig {

    @Bean // Default-Scope = singleton — einmal erzeugt, überall geteilt
    public ExchangeRateProvider exchangeRateProvider() {
        return new CachingExchangeRateProvider(/* ... */);
    }
}

// Wo gebraucht: Konstruktorinjektion statt getInstance()
@Service
public class PaymentService {
    private final ExchangeRateProvider rates;
    // Spring injiziert die eine Instanz — testbar, austauschbar
}
```

---

## Prototype

**Wann**: Neue Objekte durch Kopieren bestehender erzeugen — billiger als Neuerzeugung.

```java
// In Java 21: Records haben "Wither"-Pattern als modernen Prototype
public record OrderTemplate(
    Address defaultShipping,
    List<OrderItem> standardItems,
    String channel
) {
    // Prototype: Kopie mit geändertem Feld
    public OrderTemplate withChannel(String newChannel) {
        return new OrderTemplate(defaultShipping, standardItems, newChannel);
    }

    public OrderTemplate withShipping(Address newAddress) {
        return new OrderTemplate(newAddress, standardItems, channel);
    }
}

// Erzeugen durch Klonen statt von Grund auf neu
var webTemplate   = baseTemplate.withChannel("WEB");
var mobileTemplate = baseTemplate.withChannel("MOBILE");
```

---

## Abstract Factory

**Wann**: Familien zusammengehöriger Objekte erzeugen, ohne ihre konkreten Klassen zu nennen.

```java
// Abstrakte Factory: erzeugt zusammengehörige Produkte
public interface PaymentProviderFactory {
    PaymentGateway createGateway();
    PaymentValidator createValidator();
    PaymentLogger createLogger();
}

// Konkrete Factory: Stripe-Familie
@Component("stripe")
public class StripeProviderFactory implements PaymentProviderFactory {
    @Override public PaymentGateway   createGateway()   { return new StripeGateway(config); }
    @Override public PaymentValidator createValidator() { return new StripeValidator(); }
    @Override public PaymentLogger    createLogger()    { return new StripeAuditLogger(); }
}

// Konkrete Factory: PayPal-Familie
@Component("paypal")
public class PayPalProviderFactory implements PaymentProviderFactory {
    @Override public PaymentGateway   createGateway()   { return new PayPalGateway(config); }
    @Override public PaymentValidator createValidator() { return new PayPalValidator(); }
    @Override public PaymentLogger    createLogger()    { return new PayPalLogger(); }
}

// Aufruf: Factory aus Konfiguration wählen — nie konkrete Klassen direkt
@Service
public class PaymentService {
    private final PaymentGateway   gateway;
    private final PaymentValidator validator;

    public PaymentService(@Qualifier("${payment.provider}") PaymentProviderFactory factory) {
        this.gateway   = factory.createGateway();
        this.validator = factory.createValidator();
    }
}
```

---

## Wann welches Muster?

| Problem                                          | Muster           |
|--------------------------------------------------|------------------|
| Unterklasse entscheidet welches Objekt erzeugt wird | Factory Method |
| Komplex konfigurierbare Objekte                  | Builder          |
| Genau eine Instanz global                        | Singleton → Spring Bean |
| Objekte aus Vorlage erzeugen                     | Prototype → Record-Wither |
| Familien zusammengehöriger Objekte               | Abstract Factory |

---

## 💡 Guru-Tipps

- **Builder in Java 21**: Für einfachere Fälle reichen Records mit `withX()`-Methoden. Builder explizit nur wenn Validierung im `build()`-Schritt nötig ist.
- **Singleton ist nicht böse** — globaler veränderlicher Zustand ist böse. Ein zustandsloser Service als Singleton ist völlig korrekt.
- **Factory Method vs. Abstract Factory**: Factory Method = ein Produkt, eine Entscheidung. Abstract Factory = eine Familie von Produkten, eine kohärente Gruppe.
- **Lombok `@Builder`**: Spart Boilerplate, aber: keine Build-Zeit-Validierung, keine benannten Zwischenschritte.

---

## Verwandte ADRs

- [ADR-025](ADR-025-solid-prinzipien.md) — OCP: Erzeugungsmuster setzen OCP praktisch um.
- [ADR-029](ADR-029-entwurfsmuster-structural.md) — Strukturmuster für die erzeugten Objekte.
