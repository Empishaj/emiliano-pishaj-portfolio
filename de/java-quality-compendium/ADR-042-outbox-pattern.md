# ADR-042 — Transactional Outbox Pattern

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Debezium   |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Daten               |

---

## Kontext & Problem

Ein klassisches Dual-Write-Problem: Service A speichert eine Bestellung in der DB und publiziert dann ein Kafka-Event. Was wenn das Event nach dem DB-Commit verloren geht (Kafka down, App-Crash)? DB ist inkonsistent mit dem Event-Log. Das Outbox Pattern löst dieses Problem durch atomares Schreiben.

---

## Das Problem: Dual Write ohne Garantie

```java
// ❌ Schlecht: Zwei Schreibvorgänge ohne atomare Garantie
@Transactional
public void placeOrder(CreateOrderCommand cmd) {
    var order = orderRepository.save(new Order(cmd)); // ① DB-Write erfolgreich
    kafkaTemplate.send("orders.placed", event);       // ② Kafka-Write: kann fehlschlagen!
    // App-Crash nach ①, vor ②: Order in DB, kein Kafka-Event → Inkonsistenz
    // Kafka-Timeout: Event nie gesendet, aber DB-Transaktion committed
}
```

---

## Outbox Pattern: atomar schreiben

```java
// ✅ Outbox: beide Schreibvorgänge in EINER Transaktion
@Transactional
public void placeOrder(CreateOrderCommand cmd) {
    var order = orderRepository.save(new Order(cmd));

    // Statt direkt Kafka → Event in Outbox-Tabelle schreiben (SAME Transaktion!)
    outboxRepository.save(new OutboxEvent(
        UUID.randomUUID(),
        "OrderPlacedEvent",
        order.id().toString(),
        objectMapper.writeValueAsString(OrderPlacedEvent.from(order)),
        Instant.now()
    ));
    // Beide in EINER DB-Transaktion → entweder beide oder keins
    // Kein Kafka-Fehler kann hier die Konsistenz brechen
}
```

---

## Outbox-Tabelle

```sql
-- V10__create_outbox_table.sql
CREATE TABLE outbox_events (
    id            UUID         PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,  -- z.B. "Order"
    aggregate_id   VARCHAR(255) NOT NULL,  -- z.B. Order-ID
    event_type     VARCHAR(255) NOT NULL,  -- z.B. "OrderPlacedEvent"
    payload        JSONB        NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ,            -- NULL = noch nicht publiziert
    retry_count    INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(occurred_at)
    WHERE published_at IS NULL;
```

---

## Option A: Polling Publisher (einfach, ohne Debezium)

```java
@Component
public class OutboxPublisher {

    @Scheduled(fixedDelay = 1000) // Jede Sekunde prüfen
    @Transactional
    public void publishPendingEvents() {
        var pending = outboxRepository.findUnpublished(Pageable.ofSize(100));

        pending.forEach(event -> {
            try {
                kafkaTemplate.send(
                    toTopicName(event.eventType()),
                    event.aggregateId(),
                    event.payload()
                ).get(5, SECONDS); // Sync warten auf Bestätigung

                event.markPublished();
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.id(), e);
                event.incrementRetry();
                outboxRepository.save(event);
                // Nach 5 Retries: in DLQ verschieben
            }
        });
    }
}
```

---

## Option B: CDC mit Debezium (robuster, kein Polling)

```yaml
# Debezium PostgreSQL Connector: überwacht DB-WAL (Write-Ahead Log)
# Jede INSERT in outbox_events → automatisch an Kafka gesendet
# Kein Polling, kein Delay, kein verlorenes Event

# debezium-connector.json
{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "database.hostname": "postgres",
  "database.dbname": "orderdb",
  "table.include.list": "public.outbox_events",
  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
  "transforms.outbox.table.field.event.type": "event_type",
  "transforms.outbox.route.by.field": "aggregate_type"
}
```

---

## Konsequenzen

**Positiv:** Atomare Garantie: DB und Kafka immer konsistent. Kein verlorenes Event bei App-Crash oder Kafka-Ausfall. Natürliches At-Least-Once-Delivery → Consumer muss idempotent sein (→ ADR-041).

**Negativ:** Zusätzliche Tabelle. Outbox muss regelmäßig bereinigt werden (published_at älter als N Tage löschen). Polling-Ansatz: minimale Latenz durch Polling-Intervall.

---

## 💡 Guru-Tipps

- **Outbox + Idempotenz**: Outbox garantiert At-Least-Once → Consumer muss Duplikate erkennen (→ ADR-041).
- **Debezium bevorzugen** gegenüber Polling: CDC ist reaktiv, kein Polling-Delay, kein DB-Lock.
- **Outbox-Bereinigung**: `DELETE FROM outbox_events WHERE published_at < NOW() - INTERVAL '7 days'` — regelmäßig ausführen.

---

## Verwandte ADRs

- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka als Transport.
- [ADR-034](ADR-034-db-migrations-flyway.md) — Flyway für Outbox-Tabelle.
