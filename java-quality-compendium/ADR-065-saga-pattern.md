# ADR-065 — Saga Pattern: Verteilte Transaktionen ohne 2PC

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Kafka      |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Verteilte Systeme   |

---

## Kontext & Problem

In einer Microservice-Architektur spannt eine Geschäftstransaktion oft mehrere Services und deren Datenbanken. Ein klassisches Two-Phase-Commit (2PC) ist in verteilten Systemen unpraktisch: es erzeugt enge Kopplung, ist fehleranfällig und skaliert schlecht. Das **Saga Pattern** löst verteilte Transaktionen durch eine Sequenz von lokalen Transaktionen mit Kompensations-Aktionen.

---

## Das Problem: verteilte Transaktion ohne Saga

```
Bestellung aufgeben:
1. OrderService:     Order anlegen        → DB Commit ✓
2. InventoryService: Bestand reservieren  → DB Commit ✓
3. PaymentService:   Zahlung verarbeiten  → FEHLER!

Problem: Schritt 1+2 sind committed, Schritt 3 schlägt fehl.
Klassische Lösung: Rollback von Schritt 1+2 → braucht 2PC → enge Kopplung.
```

---

## Saga: zwei Varianten

```
Choreography-based Saga:
→ Jeder Service publiziert Events und reagiert auf Events anderer Services
→ Kein zentraler Koordinator
→ Gut für: einfache Flows, kleine Teams
→ Problem: schwer zu debuggen, Saga-Flow ist implizit

Orchestration-based Saga:
→ Zentraler Saga-Orchestrator koordiniert alle Schritte
→ Expliziter, nachvollziehbarer Flow
→ Gut für: komplexe Flows, explizite Fehlerbehandlung
→ Problem: Orchestrator ist ein zusätzlicher Service
```

---

## Choreography-based Saga

```java
// Schritt 1: OrderService — Order anlegen und Event publizieren
@Service
public class OrderService {

    @Transactional
    public void placeOrder(PlaceOrderCommand cmd) {
        var order = new Order(cmd);
        order.setStatus(PENDING_PAYMENT);
        orderRepository.save(order);

        // Event publiziert → Inventory reagiert darauf
        eventPublisher.publishEvent(new OrderPlacedEvent(
            order.id(), order.customerId(), order.items(), order.total()));
    }

    // Kompensation: wenn Zahlung fehlschlägt
    @EventListener
    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        var order = orderRepository.findById(event.orderId()).orElseThrow();
        order.cancel("Payment failed: " + event.reason());
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCancelledEvent(order.id()));
    }
}

// Schritt 2: InventoryService — Bestand reservieren
@Component
public class InventoryEventHandler {

    @EventListener
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        try {
            inventoryService.reserve(event.orderId(), event.items());
            // Erfolg → Payment wird getriggert
            eventPublisher.publishEvent(
                new InventoryReservedEvent(event.orderId(), event.customerId(), event.total()));
        } catch (InsufficientStockException e) {
            // Kompensation: Order muss storniert werden
            eventPublisher.publishEvent(
                new InventoryReservationFailedEvent(event.orderId(), e.getMessage()));
        }
    }

    // Kompensation: wenn Zahlung fehlschlägt → Reservierung freigeben
    @EventListener
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        inventoryService.release(event.orderId());
    }
}

// Schritt 3: PaymentService — Zahlung verarbeiten
@Component
public class PaymentEventHandler {

    @EventListener
    @Transactional
    public void onInventoryReserved(InventoryReservedEvent event) {
        try {
            paymentService.charge(event.customerId(), event.total());
            eventPublisher.publishEvent(new PaymentSucceededEvent(event.orderId()));
        } catch (PaymentException e) {
            // Kompensation ausgelöst
            eventPublisher.publishEvent(
                new PaymentFailedEvent(event.orderId(), e.getMessage()));
        }
    }
}
```

---

## Orchestration-based Saga

```java
// Saga Orchestrator: expliziter Workflow-Controller
@Service
public class PlaceOrderSaga {

    // Saga-State: welcher Schritt ist wie ausgegangen?
    @Entity
    @Table(name = "order_sagas")
    public static class OrderSagaState {
        @Id private String orderId;
        private SagaStep  currentStep;
        private SagaStatus status;
        private String     failureReason;
        // Kompensations-Tracking: welche Schritte müssen rückgängig gemacht werden?
        private boolean inventoryReserved;
        private boolean paymentCharged;
    }

    @Transactional
    public void start(PlaceOrderCommand cmd) {
        // Saga starten
        var saga = new OrderSagaState(cmd.orderId(), SagaStep.CREATE_ORDER, RUNNING);
        sagaRepository.save(saga);

        try {
            // Schritt 1: Order anlegen
            orderService.create(cmd);
            saga.setCurrentStep(SagaStep.RESERVE_INVENTORY);

            // Schritt 2: Bestand reservieren
            inventoryService.reserve(cmd.orderId(), cmd.items());
            saga.setInventoryReserved(true);
            saga.setCurrentStep(SagaStep.PROCESS_PAYMENT);

            // Schritt 3: Zahlung
            paymentService.charge(cmd.customerId(), cmd.total());
            saga.setPaymentCharged(true);

            // Erfolg
            orderService.confirm(cmd.orderId());
            saga.setStatus(COMPLETED);

        } catch (Exception e) {
            saga.setFailureReason(e.getMessage());
            saga.setStatus(COMPENSATING);
            compensate(saga);
        }

        sagaRepository.save(saga);
    }

    // Kompensation: rückwärts durch erfolgreiche Schritte
    private void compensate(OrderSagaState saga) {
        if (saga.isPaymentCharged()) {
            silently(() -> paymentService.refund(saga.orderId()));
        }
        if (saga.isInventoryReserved()) {
            silently(() -> inventoryService.release(saga.orderId()));
        }
        silently(() -> orderService.cancel(saga.orderId(), saga.failureReason()));
        saga.setStatus(COMPENSATED);
    }

    private void silently(Runnable action) {
        try { action.run(); }
        catch (Exception e) { log.error("Compensation step failed", e); }
        // Kompensationen müssen idempotent und robust sein!
    }
}
```

---

## Saga-Zustand in der Datenbank

```sql
-- V20__create_order_sagas.sql
CREATE TABLE order_sagas (
    order_id           VARCHAR(36)  PRIMARY KEY,
    current_step       VARCHAR(50)  NOT NULL,
    status             VARCHAR(20)  NOT NULL,  -- RUNNING, COMPLETED, COMPENSATING, COMPENSATED, FAILED
    inventory_reserved BOOLEAN      NOT NULL DEFAULT FALSE,
    payment_charged    BOOLEAN      NOT NULL DEFAULT FALSE,
    failure_reason     TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Stuck Sagas: Sagas die seit > 30 Minuten laufen
CREATE INDEX idx_sagas_stuck ON order_sagas(created_at)
    WHERE status IN ('RUNNING', 'COMPENSATING');
```

---

## Stuck Saga Recovery

```java
// Stuck Sagas: automatische Wiederaufnahme
@Scheduled(fixedDelay = 60_000)
@Transactional
public void recoverStuckSagas() {
    var cutoff = Instant.now().minus(30, ChronoUnit.MINUTES);
    var stuckSagas = sagaRepository.findStuckSagas(cutoff);

    stuckSagas.forEach(saga -> {
        log.warn("Found stuck saga: {} at step {}", saga.orderId(), saga.currentStep());
        try {
            resume(saga);  // Wiederaufnahme am letzten bekannten Schritt
        } catch (Exception e) {
            log.error("Cannot recover saga {}", saga.orderId(), e);
            saga.setStatus(FAILED);  // Manuelle Intervention nötig
            alerting.notify("Manual intervention required for saga: " + saga.orderId());
        }
    });
}
```

---

## Konsequenzen

**Positiv:** Keine 2PC-Abhängigkeit. Jeder Service hat seine eigene Datenbank. Kompensations-Aktionen machen partielle Fehler rückgängig. Orchestration-Saga macht den Flow explizit und nachvollziehbar.

**Negativ:** Eventual Consistency — während der Saga ist das System inkonsistent. Kompensations-Aktionen sind schwieriger als Rollbacks (idempotent, robust). Stuck Sagas müssen überwacht und behandelt werden.

---

## 💡 Guru-Tipps

- **Kompensationen immer idempotent**: können mehrfach aufgerufen werden ohne Nebenwirkungen.
- **Saga-ID = Correlation-ID**: durch alle Logs für End-to-End-Traceability.
- **Choreography für einfache Flows** (3-4 Schritte), **Orchestration für komplexe Flows** (5+ Schritte).
- **Axon Framework** unterstützt Saga-Orchestration nativ mit `@Saga`, `@StartSaga`, `@EndSaga`.

---

## Verwandte ADRs

- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka als Event-Transport für Choreography-Saga.
- [ADR-042](ADR-042-outbox-pattern.md) — Outbox für transaktionale Event-Publizierung in der Saga.
- [ADR-055](ADR-055-event-sourcing.md) — Event Sourcing + Saga = natürliche Kombination.
