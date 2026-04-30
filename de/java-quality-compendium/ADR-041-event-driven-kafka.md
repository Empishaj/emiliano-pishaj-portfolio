# ADR-041 — Event-Driven Architecture & Kafka

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x · Spring Kafka · Avro        |
| Datum      | 2024-09-29                                        |
| Kategorie  | Architektur / Messaging                           |

---

## Kontext & Problem

Synchrone REST-Aufrufe zwischen Services erzeugen enge Kopplung: wenn Service B langsam ist, ist Service A langsam. Wenn B ausfällt, fällt A aus. Event-Driven Architecture entkoppelt Services temporal und strukturell — Producer weiß nicht wer konsumiert, Consumer weiß nicht wer produziert.

---

## Grundprinzip

```
Synchron (eng gekoppelt):
OrderService ──[HTTP POST]──→ InventoryService  ← Was wenn Inventory down ist?
OrderService ──[HTTP POST]──→ NotificationService ← Was wenn Notification langsam ist?

Asynchron (lose gekoppelt):
OrderService ──[OrderPlacedEvent]──→ Kafka Topic
                                          │
                                          ├──→ InventoryService   (konsumiert)
                                          ├──→ NotificationService (konsumiert)
                                          └──→ AnalyticsService    (konsumiert)
OrderService wartet auf niemanden — fire and forget
```

---

## Kafka Producer: Events publizieren

```java
// Event-Schema: Avro oder Record
public record OrderPlacedEvent(
    String  orderId,
    String  customerId,
    List<OrderItemEvent> items,
    String  currency,
    long    totalCents,
    String  occurredAt  // ISO-8601
) {
    public static OrderPlacedEvent from(Order order) {
        return new OrderPlacedEvent(
            order.id().value().toString(),
            order.customerId().value().toString(),
            order.items().stream().map(OrderItemEvent::from).toList(),
            order.total().currency().getCurrencyCode(),
            order.total().toCents(),
            Instant.now().toString()
        );
    }
}

// Producer-Konfiguration
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, OrderPlacedEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
            ProducerConfig.ACKS_CONFIG, "all",          // Alle Replicas bestätigen
            ProducerConfig.RETRIES_CONFIG, 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true  // Kein Duplikat bei Retry
        ));
    }

    @Bean
    public KafkaTemplate<String, OrderPlacedEvent> orderEventTemplate(
            ProducerFactory<String, OrderPlacedEvent> factory) {
        return new KafkaTemplate<>(factory);
    }
}

// Event publizieren: transaktional (Outbox Pattern — → ADR-042)
@Service
public class OrderService {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public void placeOrder(CreateOrderCommand cmd) {
        var order = createAndSaveOrder(cmd);

        // Event mit Order-ID als Key: selbe Order immer in dieselbe Partition
        // → Reihenfolge für dieselbe Order garantiert
        kafkaTemplate.send("orders.placed", order.id().toString(),
            OrderPlacedEvent.from(order));
    }
}
```

---

## Kafka Consumer: Events konsumieren

```java
// Consumer-Konfiguration
// application.yml:
// spring:
//   kafka:
//     consumer:
//       group-id: inventory-service
//       auto-offset-reset: earliest
//       enable-auto-commit: false    # Manueller Commit — kein Datenverlust

@Component
public class InventoryEventConsumer {

    @KafkaListener(
        topics = "orders.placed",
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderPlaced(
            @Payload OrderPlacedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {
        try {
            log.info("Processing order: {}", event.orderId());
            inventoryService.reserve(event.orderId(), event.items());

            ack.acknowledge(); // Erst nach erfolgreicher Verarbeitung bestätigen!
        } catch (InsufficientStockException e) {
            // Fachlicher Fehler: Dead Letter Topic
            log.warn("Insufficient stock for order {}: {}", event.orderId(), e.getMessage());
            ack.acknowledge(); // Trotzdem bestätigen — Retry würde nichts ändern
            // Dead Letter Event publizieren
            deadLetterPublisher.publish("orders.placed.dlq", event, e.getMessage());
        } catch (Exception e) {
            // Technischer Fehler: NICHT bestätigen → Retry
            log.error("Failed to process order {}, will retry", event.orderId(), e);
            // ack.nack(Duration.ofSeconds(10)); // Retry nach 10s
        }
    }
}
```

---

## Idempotenz: Events können mehrfach ankommen

```java
// ❌ Schlecht: nicht-idempotente Verarbeitung
@KafkaListener(topics = "orders.placed")
public void onOrderPlaced(OrderPlacedEvent event) {
    // Was wenn dieser Event zweimal ankommt? Doppelte Reservierung!
    inventory.reduce(event.items());
}

// ✅ Gut: Idempotenz durch Verarbeitungs-Tabelle
@KafkaListener(topics = "orders.placed")
@Transactional
public void onOrderPlaced(OrderPlacedEvent event, Acknowledgment ack) {
    // Bereits verarbeitet?
    if (processedEventRepository.exists(event.orderId())) {
        log.info("Skipping duplicate event for order {}", event.orderId());
        ack.acknowledge();
        return;
    }

    inventory.reserve(event.orderId(), event.items());

    // Als verarbeitet markieren — atomar mit der Verarbeitung
    processedEventRepository.markProcessed(event.orderId());
    ack.acknowledge();
}
```

---

## Dead Letter Queue (DLQ)

```java
// application.yml — automatische DLQ-Konfiguration
// spring:
//   kafka:
//     listener:
//       ack-mode: MANUAL
//       missing-topics-fatal: false

@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
    var recoverer = new DeadLetterPublishingRecoverer(template,
        // DLQ-Topic: original-topic.DLT
        (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
    );

    return new DefaultErrorHandler(recoverer,
        // Retry-Konfiguration: 3 Versuche mit exponentialem Backoff
        new FixedBackOff(1000L, 3));
}
```

---

## Schema Registry: Event-Kontrakte

```java
// Avro-Schema für starke Typisierung und Backward-Compatibility
// src/main/avro/OrderPlacedEvent.avsc
{
  "type": "record",
  "name": "OrderPlacedEvent",
  "namespace": "com.example.events",
  "fields": [
    {"name": "orderId",    "type": "string"},
    {"name": "customerId", "type": "string"},
    {"name": "totalCents", "type": "long"},
    // Neues Feld mit Default — backward-compatible!
    {"name": "currency", "type": "string", "default": "EUR"}
  ]
}
// Schema Registry prüft: ist die neue Version kompatibel mit der alten?
// Inkompatible Änderungen (Feld ohne Default entfernen) werden abgelehnt
```

---

## Konsequenzen

**Positiv:** Services sind temporal entkoppelt — Ausfall eines Consumers stoppt Producer nicht. Kafka ist persistent — Events können replay werden. Consumer-Groups ermöglichen horizontale Skalierung.

**Negativ:** Eventual Consistency: Consumer verarbeitet Events verzögert. Debugging schwieriger als bei synchronen Calls. Idempotenz muss bewusst implementiert werden.

---

## Tipps

- **Partition Key = Aggregat-ID**: Selbe Order immer in dieselbe Partition → Reihenfolge garantiert.
- **`enable-auto-commit: false`** immer — automatischer Commit riskiert Datenverlust bei Fehler.
- **Outbox Pattern** (→ ADR-042) für transaktionale Konsistenz zwischen DB und Kafka.
- **Consumer-Group-ID** ist das "wer hat diesen Event schon gesehen" — jeder Service hat seine eigene.
