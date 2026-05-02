# ADR-055 — Event Sourcing

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Axon 4.x  |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Daten               |

---

## Kontext & Problem

In klassischen CRUD-Systemen wird der aktuelle Zustand gespeichert — die Geschichte ist verloren. Event Sourcing dreht das um: **Ereignisse** werden gespeichert, der aktuelle Zustand wird daraus rekonstruiert. Das ermöglicht vollständige Auditierbarkeit, Zeitreisen in vergangene Zustände und natürliche Integration mit CQRS (→ ADR-032).

---

## Wann Event Sourcing, wann nicht?

```
Event Sourcing sinnvoll wenn:
✓ Vollständige Auditierbarkeit Pflicht (Finanzen, Medizin, Compliance)
✓ "Wie war der Zustand am 15. März?" muss beantwortbar sein
✓ Komplexe Domäne mit DDD (→ ADR-023) und CQRS (→ ADR-032)
✓ Temporal Queries: "Zeig mir alle Preisänderungen des letzten Jahres"

Event Sourcing vermeiden wenn:
✗ Einfache CRUD-Applikation
✗ Team ohne ES-Erfahrung unter Zeitdruck
✗ Reporting dominiert (dann: CQRS ohne ES reicht)
✗ Häufige Schema-Änderungen (Event-Schema-Migration ist komplex)
```

---

## Das Kernkonzept

```
Traditionell:          Event Sourcing:
orders Tabelle:        order_events Tabelle:
┌──────────────────┐   ┌─────────────────────────────────────┐
│ id │ status │ .. │   │ id │ aggregate_id │ type       │ .. │
│ 1  │ SHIPPED │   │   │ 1  │ order-1      │ OrderPlaced│ .. │
└──────────────────┘   │ 2  │ order-1      │ ItemAdded  │ .. │
                       │ 3  │ order-1      │ Confirmed  │ .. │
Aktueller Zustand      │ 4  │ order-1      │ Shipped    │ .. │
                       └─────────────────────────────────────┘
                       Zustand = Replay aller Events
```

---

## ❌ Schlecht — klassisches CRUD (Zustand überschrieben)

```java
@Transactional
public void shipOrder(Long orderId) {
    var order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus(SHIPPED);        // Vorheriger Zustand verloren!
    order.setShippedAt(Instant.now());
    orderRepository.save(order);
    // Wann wurde bestellt? Wer hat bestätigt? Unbekannt.
}
```

---

## ✅ Gut — Event Sourcing mit Axon Framework

### Events definieren

```java
// Events: unveränderliche Fakten über was passiert ist
// Namenskonvention: Vergangenheitsform — was IST passiert
public record OrderPlacedEvent(
    String  orderId,
    String  customerId,
    List<OrderItemData> items,
    String  currency,
    long    totalCents,
    Instant occurredAt
) {}

public record OrderItemAddedEvent(
    String orderId,
    String productId,
    int    quantity,
    long   priceCents
) {}

public record OrderConfirmedEvent(
    String  orderId,
    String  confirmedBy,
    Instant confirmedAt
) {}

public record OrderShippedEvent(
    String  orderId,
    String  trackingNumber,
    Instant shippedAt
) {}

public record OrderCancelledEvent(
    String  orderId,
    String  reason,
    Instant cancelledAt
) {}
```

### Aggregate — Command Handler + Event Sourcing Handler

```java
@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private OrderStatus       status;
    private List<OrderItem>   items = new ArrayList<>();
    private String            customerId;

    // Pflicht: leerer Konstruktor für Axon-Rehydration
    public OrderAggregate() {}

    // ── Command Handlers: Entscheidungen + Event publizieren ─────────
    @CommandHandler
    public OrderAggregate(PlaceOrderCommand cmd) {
        // Validierung: Invarianten prüfen
        if (cmd.items().isEmpty())
            throw new InvalidOrderException("Order must have at least one item");

        // Event publizieren — kein direktes State-Update!
        AggregateLifecycle.apply(new OrderPlacedEvent(
            cmd.orderId(),
            cmd.customerId(),
            cmd.items(),
            cmd.currency(),
            calculateTotal(cmd.items()),
            Instant.now()
        ));
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand cmd) {
        if (status != OrderStatus.PENDING)
            throw new InvalidOrderStateException("Only PENDING orders can be confirmed");

        AggregateLifecycle.apply(new OrderConfirmedEvent(
            orderId, cmd.confirmedBy(), Instant.now()));
    }

    @CommandHandler
    public void handle(CancelOrderCommand cmd) {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED)
            throw new OrderCannotBeCancelledException(orderId, status);

        AggregateLifecycle.apply(new OrderCancelledEvent(
            orderId, cmd.reason(), Instant.now()));
    }

    // ── Event Sourcing Handlers: State rekonstruieren ─────────────────
    // Diese Methoden werden beim Replay aufgerufen — KEIN Side-Effect!
    @EventSourcingHandler
    public void on(OrderPlacedEvent event) {
        this.orderId    = event.orderId();
        this.customerId = event.customerId();
        this.status     = OrderStatus.PENDING;
        this.items      = toOrderItems(event.items());
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.status = OrderStatus.CONFIRMED;
    }

    @EventSourcingHandler
    public void on(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
    }
}
```

### Command Gateway — Commands senden

```java
@Service
public class OrderCommandService {

    private final CommandGateway commandGateway;

    public String placeOrder(PlaceOrderCommand command) {
        return commandGateway.sendAndWait(command);
        // Axon sendet an Aggregate → Command Handler → Event → EventStore
    }

    public void confirmOrder(String orderId, String confirmedBy) {
        commandGateway.sendAndWait(
            new ConfirmOrderCommand(orderId, confirmedBy));
    }
}
```

### Projection — Read Model aufbauen (CQRS Query-Seite)

```java
// Projection: Events konsumieren und Read-Model aufbauen
@Component
@ProcessingGroup("order-summary-projection")
public class OrderSummaryProjection {

    private final OrderSummaryRepository repository;

    @EventHandler
    public void on(OrderPlacedEvent event) {
        repository.save(new OrderSummary(
            event.orderId(),
            event.customerId(),
            OrderStatus.PENDING,
            event.totalCents(),
            event.occurredAt()
        ));
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        repository.findById(event.orderId())
            .ifPresent(summary -> {
                summary.setStatus(OrderStatus.CONFIRMED);
                repository.save(summary);
            });
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        repository.findById(event.orderId())
            .ifPresent(summary -> {
                summary.setStatus(OrderStatus.CANCELLED);
                repository.save(summary);
            });
    }
}
```

### Event Store Konfiguration

```yaml
# application.yml — Axon mit PostgreSQL Event Store
axon:
  eventhandling:
    processors:
      order-summary-projection:
        mode: tracking    # Persistent tracking — überleben Neustart
        thread-count: 1

server:
  axoniq:
    axonserver:
      servers: localhost:8124  # Axon Server für Event Store
      # Alternativ: JPA-basierter Event Store ohne Axon Server
```

---

## Zeitreise: Zustand zu einem vergangenen Zeitpunkt

```java
// Event Sourcing erlaubt Zeitreisen — unmöglich mit CRUD
@Service
public class OrderTimelineService {

    private final EventStore eventStore;

    // "Wie sah Bestellung 123 am 15. März aus?"
    public OrderAggregate reconstructAt(String orderId, Instant pointInTime) {
        var events = eventStore.readEvents(orderId)
            .asStream()
            .filter(e -> e.getTimestamp().isBefore(pointInTime))
            .collect(toList());

        // Aggregate aus historischen Events rekonstruieren
        return AggregateFactory.reconstruct(OrderAggregate.class, events);
    }
}
```

---

## Snapshots: Performance bei langen Event-Streams

```java
// Bei 10.000 Events pro Aggregate: Replay dauert lang
// Lösung: Snapshot alle N Events

axon:
  aggregate:
    order:
      snapshot-trigger-definition: eventCountSnapshotTriggerDefinition
      event-count-threshold: 500  # Snapshot alle 500 Events
```

---

## Konsequenzen

**Positiv:** Vollständige Audit-History per Definition — kein nachträgliches Logging nötig. Zeitreisen in vergangene Zustände. Natürliche Basis für CQRS. Event-Replay ermöglicht neue Projektionen ohne Datenmigration.

**Negativ:** Deutlich höhere Komplexität als CRUD. Event-Schema-Evolution schwierig — Events sind unveränderlich. Eventual Consistency zwischen Command- und Query-Seite. Team braucht ES-Erfahrung.

---

## 💡 Guru-Tipps

- **Upcasting** für Event-Schema-Migration: altes Format → neues Format beim Laden.
- **Dead Events**: Events die kein Handler mehr hat trotzdem im Store lassen — Replay-Fähigkeit erhalten.
- **Idempotente Event Handler**: Events können mehrfach geliefert werden — Handler müssen das tolerieren.
- **Separate Event Store und Message Bus**: Axon Server ist beides — für eigene Implementierung: PostgreSQL + Kafka trennen.

---

## Verwandte ADRs

- [ADR-032](ADR-032-cqrs.md) — CQRS als natürliche Ergänzung zu Event Sourcing.
- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka als Event-Transport.
- [ADR-042](ADR-042-outbox-pattern.md) — Outbox als Alternative wenn kein ES gewünscht.
