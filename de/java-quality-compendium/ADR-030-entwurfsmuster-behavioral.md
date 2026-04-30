# ADR-030 — Entwurfsmuster: Behavioral Patterns (Verhaltensmuster)

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 21                            |
| Datum      | 2024-01-01                    |
| Kategorie  | Design Patterns / GoF         |

---

## Strategy

**Wann**: Algorithmen austauschbar machen — zur Laufzeit zwischen verschiedenen Implementierungen wählen.

```java
// ❌ Algorithmus hardcodiert mit if-else
public class SortService {
    public <T> List<T> sort(List<T> items, String strategy) {
        return switch (strategy) {
            case "QUICK"  -> quickSort(items);
            case "MERGE"  -> mergeSort(items);
            case "BUBBLE" -> bubbleSort(items);
            default -> throw new IllegalArgumentException(strategy);
        };
    }
}

// ✅ Strategy Pattern
@FunctionalInterface
public interface SortStrategy<T> {
    List<T> sort(List<T> items);
}

@Service
public class SortService {
    private final Map<String, SortStrategy<Object>> strategies;

    public <T> List<T> sort(List<T> items, String strategyName) {
        return (List<T>) strategies
            .getOrDefault(strategyName, Collections::sort)
            .sort(new ArrayList<>(items));
    }
}

// In Java 21: Lambdas als Strategien
SortStrategy<Integer> ascending  = items -> items.stream().sorted().toList();
SortStrategy<Integer> descending = items -> items.stream().sorted(Comparator.reverseOrder()).toList();

// Praxisbeispiel: Discount-Strategien (→ ADR-025 OCP)
public interface DiscountStrategy {
    Money apply(Money price, Customer customer);
    boolean isApplicable(Customer customer);
}
```

---

## Observer / Event

**Wann**: Wenn eine Zustandsänderung mehrere abhängige Objekte benachrichtigen soll, ohne sie direkt zu kennen.

```java
// ❌ Direkte Aufrufe — enge Kopplung, schwer erweiterbar
public class OrderService {
    public void placeOrder(CreateOrderCommand cmd) {
        var order = createAndSaveOrder(cmd);
        emailService.sendConfirmation(order);    // direkte Kopplung
        inventoryService.reserve(order);          // direkte Kopplung
        loyaltyService.addPoints(order);          // direkte Kopplung
        analyticsService.track(order);            // direkte Kopplung
        // Neuer Observer? → diese Methode ändern (verletzt OCP)
    }
}

// ✅ Observer via Spring Events — lose Kopplung
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;

    public void placeOrder(CreateOrderCommand cmd) {
        var order = createAndSaveOrder(cmd);
        eventPublisher.publishEvent(new OrderPlacedEvent(order)); // einmal publizieren
        // Neuer Observer? Neuen @EventListener hinzufügen — OrderService unverändert
    }
}

// Observer: unabhängig, unbekannt für OrderService
@Component
public class OrderConfirmationObserver {
    @EventListener
    @Async
    public void onOrderPlaced(OrderPlacedEvent event) {
        emailService.sendConfirmation(event.order());
    }
}

@Component
public class InventoryObserver {
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        inventoryService.reserve(event.order().items());
    }
}
```

---

## Command

**Wann**: Eine Anfrage als Objekt kapseln — für Undo/Redo, Logging, Queuing.

```java
// Command-Interface
public interface OrderCommand {
    OrderCommandResult execute();
    void undo(); // Optional: für Undo-Unterstützung
}

// Konkretes Command
public class CancelOrderCommand implements OrderCommand {
    private final Long       orderId;
    private final OrderRepository repository;
    private OrderStatus previousStatus; // Für Undo gespeichert

    @Override
    public OrderCommandResult execute() {
        var order = repository.findById(orderId).orElseThrow();
        previousStatus = order.status();
        order.cancel();
        repository.save(order);
        return OrderCommandResult.success(orderId);
    }

    @Override
    public void undo() {
        // Zustand wiederherstellen
        var order = repository.findById(orderId).orElseThrow();
        order.restoreStatus(previousStatus);
        repository.save(order);
    }
}

// Command Handler / Invoker
@Service
public class OrderCommandHandler {
    private final Deque<OrderCommand> history = new ArrayDeque<>();

    public OrderCommandResult execute(OrderCommand command) {
        var result = command.execute();
        if (result.succeeded()) history.push(command);
        return result;
    }

    public void undoLast() {
        if (!history.isEmpty()) history.pop().undo();
    }
}
```

---

## Chain of Responsibility

**Wann**: Anfragen durch eine Kette von Handlern schicken, jeder kann sie bearbeiten oder weiterleiten.

```java
// Handler-Interface
public abstract class OrderValidationHandler {
    private OrderValidationHandler next;

    public OrderValidationHandler then(OrderValidationHandler next) {
        this.next = next;
        return next;
    }

    public final ValidationResult validate(CreateOrderCommand cmd) {
        var result = doValidate(cmd);
        if (!result.isValid()) return result;
        return next != null ? next.validate(cmd) : ValidationResult.valid();
    }

    protected abstract ValidationResult doValidate(CreateOrderCommand cmd);
}

// Handler-Implementierungen
public class UserExistsValidator    extends OrderValidationHandler { ... }
public class StockAvailableValidator extends OrderValidationHandler { ... }
public class CreditCheckValidator   extends OrderValidationHandler { ... }
public class FraudDetectionValidator extends OrderValidationHandler { ... }

// Kette aufbauen
var chain = new UserExistsValidator();
chain.then(new StockAvailableValidator())
     .then(new CreditCheckValidator())
     .then(new FraudDetectionValidator());

var result = chain.validate(command);

// In Spring: Servlet Filter und Spring Security Filter Chain sind Chain of Responsibility
```

---

## State

**Wann**: Das Verhalten eines Objekts hängt von seinem Zustand ab und ändert sich mit ihm.

```java
// ❌ State-Maschine als if-else Kaskade
public class Order {
    private String status = "PENDING";

    public void confirm() {
        if ("PENDING".equals(status))    status = "CONFIRMED";
        else if ("CONFIRMED".equals(status)) throw new IllegalStateException("already confirmed");
        else throw new IllegalStateException("cannot confirm from " + status);
    }
    // Bei 5 Zuständen × 5 Aktionen = 25 if-else Zweige
}

// ✅ State Pattern mit Sealed Interface (→ ADR-002)
sealed interface OrderState permits PendingState, ConfirmedState, ShippedState, CancelledState {
    OrderState confirm();
    OrderState ship();
    OrderState cancel();
}

record PendingState() implements OrderState {
    @Override public OrderState confirm()  { return new ConfirmedState(); }
    @Override public OrderState ship()     { throw new InvalidTransitionException("PENDING→SHIPPED"); }
    @Override public OrderState cancel()   { return new CancelledState(); }
}

record ConfirmedState() implements OrderState {
    @Override public OrderState confirm()  { throw new InvalidTransitionException("already CONFIRMED"); }
    @Override public OrderState ship()     { return new ShippedState(); }
    @Override public OrderState cancel()   { return new CancelledState(); }
}

// Order delegiert an State-Objekt
public class Order {
    private OrderState state = new PendingState();

    public void confirm() { state = state.confirm(); }
    public void ship()    { state = state.ship();    }
    public void cancel()  { state = state.cancel();  }
}
```

---

## Template Method

**Wann**: Den Skelett-Algorithmus in einer Basisklasse definieren, Schritte in Subklassen überschreiben.

```java
// Abstrakte Basis: definiert den Algorithmus-Ablauf
public abstract class ReportGenerator {

    // Template Method — final: Algorithmus nicht überschreibbar
    public final Report generate(ReportRequest request) {
        var data      = fetchData(request);        // ① Hook
        var processed = processData(data);          // ② Hook
        var formatted = formatReport(processed);    // ③ Hook
        auditLog(request, formatted);               // ④ immer gleich
        return formatted;
    }

    protected abstract List<DataRow> fetchData(ReportRequest request);
    protected abstract List<DataRow> processData(List<DataRow> raw);
    protected abstract Report        formatReport(List<DataRow> data);

    // Hook mit Default — kann überschrieben werden, muss aber nicht
    protected void auditLog(ReportRequest req, Report report) {
        log.info("Report generated: {}", req.type());
    }
}

// Konkrete Implementierungen überschreiben nur die Hooks
public class SalesCsvReport extends ReportGenerator {
    @Override protected List<DataRow> fetchData(ReportRequest r)  { return salesRepo.findAll(r); }
    @Override protected List<DataRow> processData(List<DataRow> d) { return aggregate(d); }
    @Override protected Report        formatReport(List<DataRow> d) { return toCsv(d); }
}

public class SalesPdfReport extends ReportGenerator {
    @Override protected List<DataRow> fetchData(ReportRequest r)  { return salesRepo.findAll(r); }
    @Override protected List<DataRow> processData(List<DataRow> d) { return aggregate(d); }
    @Override protected Report        formatReport(List<DataRow> d) { return toPdf(d); }
}
```

---

## Iterator (modern: Java Stream / Iterable)

```java
// Java bietet Iterator nativ — nicht neu implementieren
// Stream ist der moderne Iterator
orderRepository.findAll()
    .stream()
    .filter(o -> o.status() == PENDING)
    .map(orderMapper::toDto)
    .toList();

// Eigener Iterator nur für komplexe Traversals (z. B. Baum-Traversal)
public class OrderTreeIterator implements Iterator<Order> {
    private final Deque<Order> stack = new ArrayDeque<>();

    public OrderTreeIterator(Order root) { stack.push(root); }

    @Override public boolean hasNext() { return !stack.isEmpty(); }

    @Override public Order next() {
        var order = stack.pop();
        order.subOrders().forEach(stack::push);
        return order;
    }
}
```

---

## Wann welches Verhaltensmuster?

| Problem                                              | Muster               |
|------------------------------------------------------|----------------------|
| Algorithmus zur Laufzeit austauschen                 | Strategy             |
| Zustandsänderungen an Abhängige melden               | Observer / Event     |
| Anfrage als Objekt kapseln (Undo, Queue)             | Command              |
| Anfrage durch Handler-Kette schicken                 | Chain of Responsibility |
| Verhalten abhängig vom Zustand                       | State                |
| Algorithmus-Skeleton fix, Schritte variabel          | Template Method      |
| Kollektions-Traversal kapseln                        | Iterator             |

---

## Tipps

- **Strategy mit Lambdas**: In Java 21 sind `@FunctionalInterface`-Strategien oft eleganter als Klassen.
- **State mit Sealed Interface**: Das natürlichste Java-21-Pattern für State-Maschinen — der Compiler prüft Vollständigkeit.
- **Chain of Responsibility in Spring**: Servlet Filter, Spring Security Filter, Interceptors — alles CoR.
- **Template Method kritisch sehen**: Vererbung für Algorithmus-Varianten — oft besser durch Strategy ersetzbar (Komposition > Vererbung, → ADR-008).
