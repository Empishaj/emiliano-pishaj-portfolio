# ADR-029 — Entwurfsmuster: Structural Patterns (Strukturmuster)

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 21                            |
| Datum      | 2024-01-01                    |
| Kategorie  | Design Patterns / GoF         |

---

## Adapter

**Wann**: Zwei inkompatible Interfaces zusammenbringen — "Steckdosen-Adapter für Code".

```java
// Legacy-Schnittstelle (kann nicht geändert werden)
public interface LegacyPaymentGateway {
    boolean processPayment(String accountNumber, double amount, String currencyCode);
}

// Neue Domänen-Schnittstelle
public interface PaymentGateway {
    PaymentResult charge(PaymentCommand command);
}

// Adapter: macht Legacy kompatibel mit neuer Schnittstelle
@Component
public class LegacyPaymentAdapter implements PaymentGateway {
    private final LegacyPaymentGateway legacy;

    @Override
    public PaymentResult charge(PaymentCommand command) {
        // Übersetzt neue → alte Schnittstelle
        boolean success = legacy.processPayment(
            command.accountId().value(),
            command.amount().value().doubleValue(),
            command.amount().currency().getCurrencyCode()
        );
        return success
            ? PaymentResult.success(UUID.randomUUID().toString())
            : PaymentResult.failure("Legacy gateway declined");
    }
}
// Domänencode kennt nur PaymentGateway — nie die Legacy-Schnittstelle
```

---

## Decorator

**Wann**: Verhalten zu einem Objekt hinzufügen ohne Vererbung und ohne die Klasse zu ändern.

```java
// Basis-Interface
public interface OrderRepository {
    Order findById(Long id);
    Order save(Order order);
}

// Basis-Implementierung
@Repository
public class JpaOrderRepository implements OrderRepository { ... }

// Decorator 1: Caching hinzufügen
public class CachingOrderRepository implements OrderRepository {
    private final OrderRepository delegate;
    private final Cache<Long, Order> cache;

    @Override
    public Order findById(Long id) {
        return cache.get(id, k -> delegate.findById(k)); // Cache-Decorator
    }

    @Override
    public Order save(Order order) {
        cache.invalidate(order.id());
        return delegate.save(order);
    }
}

// Decorator 2: Logging hinzufügen
public class LoggingOrderRepository implements OrderRepository {
    private final OrderRepository delegate;

    @Override
    public Order findById(Long id) {
        log.debug("Loading order {}", id);
        var order = delegate.findById(id);
        log.debug("Loaded order {} with status {}", id, order.status());
        return order;
    }

    @Override
    public Order save(Order order) {
        log.info("Saving order {}", order.id());
        return delegate.save(order);
    }
}

// Konfiguration: Decorators stacked
@Bean
public OrderRepository orderRepository(JpaOrderRepository jpa,
                                       Cache<Long, Order> cache) {
    return new LoggingOrderRepository(   // äußerster Decorator
        new CachingOrderRepository(jpa, cache)  // innerer Decorator
    );
}
// Spring: @Qualifier oder @Primary für die richtige Instanz
```

---

## Proxy

**Wann**: Einen Stellvertreter für ein Objekt bereitstellen — für Zugangskontrolle, Lazy Loading oder Remote-Zugriff.

```java
// Virtual Proxy: teures Objekt erst bei Bedarf laden
public class LazyReportProxy implements Report {
    private final Long reportId;
    private final ReportRepository repository;
    private Report loaded; // null bis zum ersten Zugriff

    @Override
    public String getContent() {
        if (loaded == null) {
            loaded = repository.loadFull(reportId); // Lazy Loading
        }
        return loaded.getContent();
    }
}

// Protection Proxy: Zugangskontrolle
public class SecuredOrderRepository implements OrderRepository {
    private final OrderRepository delegate;
    private final SecurityContext securityContext;

    @Override
    public Order findById(Long id) {
        var order = delegate.findById(id);
        if (!securityContext.currentUser().canAccess(order)) {
            throw new AccessDeniedException("Order " + id);
        }
        return order;
    }
}

// Spring AOP ist ein Proxy-Mechanismus: @Transactional, @Cacheable, @Secured
// → Spring erzeugt automatisch Proxy-Objekte für annotierte Beans
```

---

## Facade

**Wann**: Eine komplexe Subsystem-Schnittstelle vereinfachen.

```java
// Subsystem A: komplex
public class InventoryService     { public void reserve(List<OrderItem> items) { ... } }
public class PaymentProcessor     { public PaymentResult charge(Money amount) { ... } }
public class ShippingCalculator   { public Money calculate(Address to) { ... } }
public class NotificationDispatcher { public void notify(Order order) { ... } }
public class AuditLogger          { public void log(String event, Object data) { ... } }

// ❌ Ohne Facade: Aufrufer muss alle Subsysteme kennen und orchestrieren
public class OrderController {
    // 5 Abhängigkeiten — jede änderung am Checkout-Prozess betrifft den Controller
    private final InventoryService inventory;
    private final PaymentProcessor payment;
    private final ShippingCalculator shipping;
    private final NotificationDispatcher notifier;
    private final AuditLogger audit;
}

// ✅ Facade: vereinfachte Sicht auf den Checkout-Prozess
@Service
public class CheckoutFacade {
    // Alle Subsystem-Abhängigkeiten hier — Controller kennt nur die Facade

    public CheckoutResult checkout(CheckoutCommand command) {
        var shippingCost = shipping.calculate(command.shippingAddress());
        var total        = command.subtotal().add(shippingCost);
        var payment      = paymentProcessor.charge(total);

        if (payment.succeeded()) {
            inventory.reserve(command.items());
            var order = orderRepository.save(new Order(command, payment));
            notifier.notify(order);
            audit.log("ORDER_PLACED", order);
            return CheckoutResult.success(order.id());
        }
        return CheckoutResult.paymentFailed(payment.failureReason());
    }
}

@RestController
public class OrderController {
    private final CheckoutFacade checkout; // Nur eine Abhängigkeit!

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest req) {
        return mapper.toResponse(checkout.checkout(mapper.toCommand(req)));
    }
}
```

---

## Composite

**Wann**: Einzelne Objekte und Gruppen von Objekten einheitlich behandeln (Baumstruktur).

```java
// Interface: uniform für Einzelprodukt und Produktgruppe
public interface PriceComponent {
    Money totalPrice();
    String describe();
}

// Leaf: einfaches Produkt
public record Product(String name, Money price) implements PriceComponent {
    @Override public Money totalPrice() { return price; }
    @Override public String describe()  { return name + ": " + price; }
}

// Composite: Bündel von Produkten
public class ProductBundle implements PriceComponent {
    private final String name;
    private final List<PriceComponent> components = new ArrayList<>();

    public void add(PriceComponent component) { components.add(component); }

    @Override
    public Money totalPrice() {
        return components.stream()
            .map(PriceComponent::totalPrice)
            .reduce(Money.zero(), Money::add);
    }

    @Override
    public String describe() {
        return name + " [" + components.stream()
            .map(PriceComponent::describe)
            .collect(joining(", ")) + "]";
    }
}

// Aufruf: identisch für Einzelprodukt und Bundle
PriceComponent singleProduct = new Product("Java Book", Money.of("49.99", "EUR"));
PriceComponent bundle = new ProductBundle("Dev Kit");
// bundle.add(singleProduct); bundle.add(...)

Money total = bundle.totalPrice(); // Rekursiv für alle Ebenen
```

---

## Bridge

**Wann**: Abstraktion und Implementierung unabhängig variieren lassen.

```java
// Implementierungs-Interface (can vary independently)
public interface MessageSender {
    void send(String recipient, String content);
}

// Abstraktion (can vary independently)
public abstract class Notification {
    protected final MessageSender sender; // Bridge zur Implementierung

    public abstract void notifyUser(User user, String event);
}

// Konkrete Abstraktionen
public class OrderNotification extends Notification {
    public OrderNotification(MessageSender sender) { super(sender); }

    @Override
    public void notifyUser(User user, String event) {
        sender.send(user.contact(), "Order update: " + event);
    }
}

// Konkrete Implementierungen — unabhängig von Abstraktionen
@Component public class EmailSender implements MessageSender { ... }
@Component public class SmsSender   implements MessageSender { ... }

// Kombination zur Laufzeit: jede Abstraktion × jede Implementierung
var emailOrderNotification = new OrderNotification(emailSender);
var smsOrderNotification   = new OrderNotification(smsSender);
```

---

## Wann welches Strukturmuster?

| Problem                                               | Muster    |
|-------------------------------------------------------|-----------|
| Inkompatible Schnittstellen zusammenbringen           | Adapter   |
| Verhalten hinzufügen ohne Klasse zu ändern            | Decorator |
| Zugriffskontrolle / Lazy Loading / Remote             | Proxy     |
| Komplexes Subsystem vereinfachen                      | Facade    |
| Baumstruktur: Einzel + Gruppe einheitlich behandeln   | Composite |
| Abstraktion und Implementierung unabhängig variieren  | Bridge    |

---

## Tipps

- **Spring AOP ist Proxy/Decorator**: `@Transactional`, `@Cacheable`, `@Secured` sind Decorators die Spring automatisch als Proxies umsetzt.
- **Facade ≠ God Service**: Eine Facade delegiert — sie implementiert keine Business-Logik selbst.
- **Decorator ist die saubere Alternative zu Vererbung** wenn neue Verhaltensaspekte hinzugefügt werden.
- **Adapter als Anti-Corruption Layer** (→ ADR-023): Externe Systeme hinter einem Adapter kapseln — Domänencode nie direkt gegen externe APIs schreiben.
