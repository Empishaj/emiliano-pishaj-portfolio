# ADR-097 — Dead Letter Queue & Poison Pills: Kafka-Fehlerbehandlung

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Messaging · Fehlerbehandlung · Resilience                     |
| Betroffene Teams  | Alle Teams die Kafka-Events konsumieren                       |
| Abhängigkeiten    | ADR-041 (Kafka), ADR-042 (Outbox), ADR-022 (Resilience)       |

---

## 1. Kontext & Treiber

### 1.1 Das Gift-Pille-Problem

```
SITUATION: Kafka Consumer verarbeitet Events in einer Partition sequentiell.

Normale Verarbeitung:
  Event 1 → ✅ verarbeitet → Offset committed
  Event 2 → ✅ verarbeitet → Offset committed
  Event 3 → ❌ FEHLER! Consumer wirft Exception
            Kafka: "nicht committed" → Consumer versucht Event 3 erneut
  Event 3 → ❌ FEHLER! (immer noch)
  Event 3 → ❌ FEHLER! (immer noch)
  ... für immer

ERGEBNIS: Die Partition ist blockiert.
          Event 4, 5, 6, ... werden NIEMALS verarbeitet.
          Ein einziges fehlerhaftes Event stoppt alle nachfolgenden Events.
          Das nennt sich "Poison Pill" — das vergiftete Event.

KONKRETES BEISPIEL:
  OrderPlacedEvent mit corrupted JSON kommt rein.
  Fulfillment-Service kann es nicht deserialisieren.
  → Jeder Retry schlägt fehl.
  → Alle neuen Bestellungen werden nicht mehr versandt.
  → Stunden später: Ops-Team bemerkt Backlog → manueller Eingriff nötig.
```

### 1.2 Die Fehlerklassen

```
FEHLERKLASSE 1: Transiente Fehler (temporär, behebbar durch Retry)
  Ursache: DB kurz nicht verfügbar, externe API Timeout, Netzwerk-Blip
  Strategie: Retry mit Exponential Backoff (→ ADR-022)
  Max Retries: 3-5 Versuche
  
FEHLERKLASSE 2: Poison Pills (permanent, nicht durch Retry behebbar)
  Ursache: Korruptes Event-Format, Schema-Verletzung, Business-Constraint nie erfüllbar
  Strategie: In Dead Letter Topic verschieben, Consumer-Progression fortsetzen
  Max Retries: 0-1 (sofort in DLQ)

FEHLERKLASSE 3: Business-Fehler (erwartet, kein technischer Fehler)
  Ursache: Bestand reicht nicht, User nicht mehr aktiv
  Strategie: Kompensations-Event publizieren (→ ADR-065 Saga)
  Nicht: in DLQ werfen (das ist erwartetes Verhalten!)
```

---

## 2. Entscheidung

Alle Kafka-Consumer implementieren eine dreistufige Fehlerbehandlung: Exponential Backoff für transiente Fehler, automatische Verschiebung in ein Dead Letter Topic nach erschöpften Retries. Dead Letter Topics werden aktiv überwacht und fehlerhaft verarbeitete Events werden nach Ursachenbehebung re-prozessiert.

---

## 3. Implementierung: Spring Kafka Fehlerbehandlung

### 3.1 ❌ Schlecht — keine Fehlerbehandlung

```java
@KafkaListener(topics = "orders.placed")
public void onOrderPlaced(OrderPlacedEvent event) {
    fulfillmentService.prepare(event.orderId(), event.items());
    // Wenn prepare() wirft: Kafka retried unendlich.
    // Eine Exception = Partition blockiert = alle nachfolgenden Events gestoppt.
}
```

### 3.2 ✅ Gut — vollständige Fehlerbehandlungs-Konfiguration

```java
// Konfiguration: Retry + Dead Letter Topic
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, byte[]> kafkaTemplate) {

        // Dead Letter Topic Publisher
        var deadLetterPublisher = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            // Routing: fehlerhaftes Event → .DLT Topic
            (record, ex) -> {
                var dlqTopic = record.topic() + ".DLT";
                var partition = record.partition();
                return new TopicPartition(dlqTopic, partition);
            }
        );

        // Retry-Strategie: Exponential Backoff
        var backoff = ExponentialBackOffWithMaxRetries(5); // Max 5 Versuche
        backoff.setInitialInterval(500);   // 500ms Initial
        backoff.setMultiplier(2.0);        // ×2 bei jedem Retry
        backoff.setMaxInterval(30_000);    // Max 30s zwischen Retries

        var handler = new DefaultErrorHandler(deadLetterPublisher, backoff);

        // SOFORT in DLQ (kein Retry) für nicht-transiente Fehler:
        handler.addNotRetryableExceptions(
            DeserializationException.class,   // Korruptes Format → sofort DLQ
            SchemaViolationException.class,   // Schema-Mismatch → sofort DLQ
            NullPointerException.class        // Bug im Code → sofort DLQ (nicht retrien!)
        );

        // Retry für transiente Fehler:
        handler.addRetryableExceptions(
            DataAccessException.class,        // DB kurz weg
            ResourceAccessException.class     // HTTP Timeout
        );

        return handler;
    }
}

// Consumer mit fehlertoleranter Konfiguration
@KafkaListener(
    topics = "orders.placed",
    groupId = "fulfillment-service",
    containerFactory = "kafkaListenerContainerFactory"
)
@Transactional
public void onOrderPlaced(
        @Payload OrderPlacedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.OFFSET) long offset) {

    log.info("Processing OrderPlacedEvent",
        kv("orderId", event.orderId()),
        kv("topic", topic),
        kv("offset", offset));

    try {
        fulfillmentService.prepare(event.orderId(), event.items());

        log.info("OrderPlacedEvent processed successfully",
            kv("orderId", event.orderId()));

    } catch (OrderAlreadyFulfilledException e) {
        // BUSINESS-FEHLER: nicht in DLQ — das ist erwartetes Verhalten bei Duplikat
        log.warn("Order already fulfilled, skipping (idempotent)",
            kv("orderId", event.orderId()));
        // KEIN throw → Offset wird committed, nächstes Event wird verarbeitet

    } catch (Exception e) {
        log.error("Failed to process OrderPlacedEvent",
            kv("orderId", event.orderId()),
            kv("error", e.getMessage()));
        throw e; // Re-throw → ErrorHandler entscheidet: Retry oder DLQ
    }
}
```

### 3.3 Dead Letter Topic Namenskonvention

```
Convention: <source-topic>.DLT

Beispiele:
  orders.placed           → orders.placed.DLT
  orders.cancelled        → orders.cancelled.DLT
  inventory.reserved      → inventory.reserved.DLT

Konfiguration:
  Retention:    7 Tage (lang genug für Debugging + Re-Processing)
  Partitions:   gleich wie Source-Topic
  Replicas:     3 (wie alle kritischen Topics)

Header die automatisch hinzugefügt werden (Spring Kafka):
  kafka_dlt-original-topic:     orders.placed
  kafka_dlt-original-partition: 2
  kafka_dlt-original-offset:    12345
  kafka_dlt-exception-message:  "Cannot deserialize..."
  kafka_dlt-exception-stacktrace: "..."
  kafka_dlt-exception-cause:    "JsonParseException"
```

---

## 4. DLQ-Monitoring: Alerting bei Backlog

```yaml
# Prometheus Alert: DLQ wächst → sofortige Benachrichtigung
- alert: DeadLetterQueueGrowing
  expr: |
    sum(kafka_consumer_group_lag{
      topic=~".*\\.DLT",
      consumergroup="dlq-monitor"
    }) > 0
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Dead Letter Queue hat unverarbeitete Events"
    description: "{{ $labels.topic }}: {{ $value }} Events in DLQ"
    runbook: "https://wiki.example.com/runbooks/dlq-processing"

- alert: DeadLetterQueueCritical
  expr: |
    sum(kafka_consumer_group_lag{topic=~".*\\.DLT"}) > 100
  for: 15m
  labels:
    severity: critical
  annotations:
    summary: "DLQ kritisch angewachsen — manuelle Intervention nötig"
```

---

## 5. DLQ-Dashboard: Admin-Interface für Re-Processing

```java
// Admin-Endpoint: fehlerhaft verarbeitete Events einsehen und re-prozessieren
@RestController
@RequestMapping("/admin/dlq")
@PreAuthorize("hasRole('ADMIN')")
public class DeadLetterQueueController {

    private final KafkaConsumer<String, byte[]> dlqConsumer;
    private final KafkaProducer<String, byte[]> replayProducer;

    // Alle Events in einer DLQ einsehen
    @GetMapping("/{topic}")
    public List<DlqEventDto> listDlqEvents(
            @PathVariable String topic,
            @RequestParam(defaultValue = "100") int limit) {

        var dlqTopic = topic + ".DLT";
        dlqConsumer.assign(getPartitions(dlqTopic));
        dlqConsumer.seekToBeginning(getPartitions(dlqTopic));

        var events = new ArrayList<DlqEventDto>();
        var records = dlqConsumer.poll(Duration.ofSeconds(5));

        for (var record : records) {
            if (events.size() >= limit) break;
            events.add(new DlqEventDto(
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp(),
                extractHeader(record, "kafka_dlt-exception-message"),
                extractHeader(record, "kafka_dlt-original-topic"),
                new String(record.value(), StandardCharsets.UTF_8)
            ));
        }
        return events;
    }

    // Einzelnes Event re-prozessieren
    @PostMapping("/{topic}/replay/{partition}/{offset}")
    public ResponseEntity<Void> replayEvent(
            @PathVariable String topic,
            @PathVariable int partition,
            @PathVariable long offset) {

        var dlqEvent = fetchEvent(topic + ".DLT", partition, offset);

        // Zurück in das Original-Topic senden
        var originalTopic = extractHeader(dlqEvent, "kafka_dlt-original-topic");
        replayProducer.send(new ProducerRecord<>(
            originalTopic,
            dlqEvent.key(),
            dlqEvent.value()
        ));

        log.info("Replayed DLQ event",
            kv("topic", originalTopic),
            kv("partition", partition),
            kv("offset", offset));

        return ResponseEntity.accepted().build();
    }

    // Alle Events eines Topics re-prozessieren (nach Bugfix)
    @PostMapping("/{topic}/replay-all")
    public ResponseEntity<Map<String, Object>> replayAll(
            @PathVariable String topic) {

        var replayed = 0;
        var dlqTopic = topic + ".DLT";
        var originalTopic = topic;

        // Alle DLQ-Events zurück in Original-Topic
        for (var record : fetchAllDlqEvents(dlqTopic)) {
            replayProducer.send(new ProducerRecord<>(
                originalTopic, record.key(), record.value()));
            replayed++;
        }

        return ResponseEntity.ok(Map.of(
            "replayed", replayed,
            "sourceTopic", dlqTopic,
            "targetTopic", originalTopic
        ));
    }
}
```

---

## 6. Idempotenz: Voraussetzung für Re-Processing

```java
// Jeder Consumer MUSS idempotent sein — DLQ-Events werden re-prozessiert!
// Dasselbe Event kann mehrfach ankommen.

@Component
public class FulfillmentEventHandler {

    @KafkaListener(topics = "orders.placed")
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        // Idempotenz: bereits verarbeitete Events überspringen
        if (shipmentRepository.existsByOrderId(event.orderId())) {
            log.info("Shipment already exists for order, skipping (idempotent)",
                kv("orderId", event.orderId()));
            return;  // KEIN Fehler, KEIN Exception — einfach überspringen
        }

        // Verarbeitung
        var shipment = Shipment.prepareFor(event.orderId(), event.items());
        shipmentRepository.save(shipment);
    }
}

// Idempotenz-Schlüssel Tabelle (Alternative zu If-Exists-Check):
@Entity
@Table(name = "processed_events",
    uniqueConstraints = @UniqueConstraint(columnNames = {"topic", "partition", "offset_value"}))
public class ProcessedEvent {
    private String topic;
    private int partition;
    private long offsetValue;  // @Column(name="offset_value") wegen SQL-Keyword
    private Instant processedAt;
}
```

---

## 7. Schemafehler: Deserialisierungs-DLQ

```java
// Spezialfall: Event kann gar nicht deserialisiert werden
// → ErrorHandlingDeserializer als Wrapper

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> consumerFactory() {
        var props = Map.of(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,

            // ErrorHandlingDeserializer: fängt DeserializationException
            // und liefert null zurück statt Exception zu werfen
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class,

            // Echter Deserializer (in ErrorHandlingDeserializer gewickelt)
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class.getName()
        );
        return new DefaultKafkaConsumerFactory<>(props);
    }
}

@KafkaListener(topics = "orders.placed")
public void onOrderPlaced(
        @Payload(required = false) OrderPlacedEvent event,  // required=false!
        ConsumerRecord<String, ?> record) {

    if (event == null) {
        // Deserialisierung fehlgeschlagen → in DLQ
        log.error("Cannot deserialize event, sending to DLQ",
            kv("topic", record.topic()),
            kv("rawValue", new String((byte[]) record.value())));
        // ErrorHandler schreibt automatisch in .DLT
        throw new DeserializationException("Cannot deserialize", (byte[]) record.value(), false, null);
    }

    // Normale Verarbeitung
    fulfillmentService.prepare(event.orderId(), event.items());
}
```

---

## 8. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| Fehler ignorieren (log + skip) | Einfach | Datenverlust! Consumer weiß nie ob verarbeitet | Unakzeptabel für Bestell-Events |
| Endlos-Retry | Kein Datenverlust | Partition blockiert, kein Fortschritt | Poison Pill blockiert alle nachfolgenden Events |
| Synchrones Error-Handling im Consumer | Flexibel | Komplexer Code, kein Retry-Tracking | Spring Kafka bietet das transparenter |
| Eigene DLQ in PostgreSQL | Kein Kafka-Overhead | Zwei Infrastrukturen für dasselbe Problem | Kafka ist bereits vorhanden |

---

## 9. Trade-off-Matrix

| Qualitätsziel | Retry + DLQ (gewählt) | Endlos-Retry | Ignorieren |
|---|---|---|---|
| Datensicherheit | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐ |
| Consumer-Fortschritt | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐⭐⭐⭐ |
| Observability | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |
| Implementierungskomplexität | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Re-Processing-Fähigkeit | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |

---

## 10. Akzeptanzkriterien

- [ ] Jeder Kafka-Consumer hat `DefaultErrorHandler` mit Exponential Backoff konfiguriert
- [ ] Jedes Topic hat ein korrespondierendes `.DLT` Topic erstellt
- [ ] Prometheus-Alert bei DLQ-Backlog > 0 (Warning) und > 100 (Critical)
- [ ] Admin-Endpoint `/admin/dlq` für Re-Processing vorhanden und gesichert
- [ ] Alle Consumer sind idempotent (Tests beweisen das: doppeltes Event → keine Seiteneffekte)
- [ ] `DeserializationException` landet sofort in DLQ (kein Retry)
- [ ] Runbook dokumentiert: wie wird ein DLQ-Event analysiert und re-prozessiert?

---

## Quellen & Referenzen

- **Confluent Documentation, "Kafka Error Handling and Dead Letter Topics"** — offizielle Referenz.
- **Spring Kafka Documentation, "DefaultErrorHandler"** — aktuelle Spring Kafka API.
- **Jay Kreps, Neha Narkhede, Jun Rao, "Kafka: A Distributed Messaging System for Log Processing" (2011)** — Kafka-Grundlagen: Partition, Offset, Consumer Groups.
- **Chris Richardson, "Microservices Patterns" (2018), Kap. 3** — Message-basierte Kommunikation und Fehlerbehandlung.

---

## Verwandte ADRs

- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka-Grundkonfiguration
- [ADR-042](ADR-042-outbox-pattern.md) — Outbox als Producer-seitige Garantie
- [ADR-022](ADR-022-resilience-circuit-breaker.md) — Retry & Circuit Breaker
- [ADR-065](ADR-065-saga-pattern.md) — Kompensation als Alternative zu DLQ
- [ADR-095](ADR-095-asyncapi-specification.md) — Events mit AsyncAPI dokumentiert
