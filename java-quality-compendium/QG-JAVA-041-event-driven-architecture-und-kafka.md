# QG-JAVA-041 — Event-Driven Architecture und Kafka

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-041 |
| Titel | Event-Driven Architecture und Kafka |
| Status | Accepted / verbindlicher Standard für neue eventbasierte Integrationen |
| Version | 1.0.0 |
| Datum | 2024-02-16 |
| Review-Datum | 2026-05-01 |
| Kategorie | Architektur / Messaging / Integration / Resilienz |
| Zielgruppe | Java-Entwickler, Tech Leads, Architektur, QA, Security, DevOps, Plattform-Team |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Kafka 3.x, Micrometer, OpenTelemetry |
| Messaging-Baseline | Apache Kafka, Schema Registry, Avro oder vergleichbarer Schema-Mechanismus |
| Geltungsbereich | Event-Produktion, Event-Konsum, Kafka-Topics, Event-Schemas, Consumer-Groups, Retry, Dead Letter Topics, Idempotenz, Outbox, Observability und Security |
| Verbindlichkeit | Kafka darf nur für fachlich oder technisch sinnvolle asynchrone Integration eingesetzt werden. Events müssen versioniert, beobachtbar, idempotent verarbeitbar und sicher betrieben werden. |
| Technische Validierung | Gegen Apache Kafka Producer-Konfiguration, Spring Kafka Error Handling, Confluent Schema Registry, OWASP Logging und Secrets Management validiert |
| Kurzentscheidung | Event-Driven Architecture entkoppelt Services zeitlich und strukturell, erfordert aber explizite Standards für Schema, Zustellung, Idempotenz, Fehlerbehandlung, Observability, Datenschutz und Betrieb. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Event-Driven Architecture mit Kafka in Java- und Spring-Boot-Systemen sauber eingesetzt wird.

Event-Driven Architecture ist kein Ersatz für gutes API-Design und kein pauschales Mittel gegen schlechte Servicegrenzen. Sie ist ein Integrationsstil, bei dem Services fachliche Ereignisse veröffentlichen und andere Services darauf reagieren können. Dadurch können Systeme zeitlich entkoppelt, unabhängiger deploybar und resilienter gegen langsame Consumer werden.

Gleichzeitig verschiebt Event-Driven Architecture Komplexität:

- Fehler treten später und asynchron auf.
- Konsistenz ist oft nur eventual.
- Ereignisse können mehrfach ankommen.
- Reihenfolge gilt nur innerhalb einer Partition.
- Consumer können zurückliegen.
- Schemaänderungen können viele Teams betreffen.
- Debugging braucht Observability.
- Datenschutzrisiken wandern in Topics, Logs und Traces.

Der Zweck dieser Guideline ist deshalb nicht „Kafka überall“. Der Zweck ist, Kafka dort robust einzusetzen, wo asynchrone Ereignisse fachlich sinnvoll sind.

---

## 2. Kurzregel

Kafka-Events dürfen nur fachlich stabile Ereignisse oder technisch klar definierte Integrationssignale transportieren. Jedes Event braucht einen Owner, ein Schema, eine Versionierungsstrategie, einen Partition Key, eine Idempotenzstrategie, eine Fehlerstrategie, Observability und Sicherheitsprüfung. Producer dürfen kritische fachliche Zustandsänderungen nicht direkt in derselben Transaktion „nebenbei“ nach Kafka senden, wenn Datenbank und Kafka konsistent bleiben müssen; dafür ist ein Outbox-Pattern oder eine äquivalente Konsistenzstrategie erforderlich.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Kafka Producer
- Kafka Consumer
- Spring Kafka Listener
- Event-Schemas mit Avro, JSON Schema oder Protobuf
- Schema Registry
- Consumer Groups
- Dead Letter Topics
- Retry Topics
- Transactional Outbox
- Idempotenz in Consumer-Verarbeitung
- Event-Versionierung
- Event-Observability
- Event-Security
- SaaS-/Tenant-bezogene Events
- Integrationstests mit Kafka/Testcontainers
- CI/CD-Verifikation von Event-Schemas

Diese Richtlinie gilt nicht für reine In-Memory-Domain-Events innerhalb eines Monolithen, sofern diese nicht über Kafka oder andere Broker publiziert werden. Sobald ein Event serviceübergreifend verteilt wird, gilt diese Richtlinie.

---

## 4. Technischer Hintergrund

Kafka ist ein verteilter Commit-Log-basierter Event-Streaming-Broker. Producer schreiben Records in Topics. Topics sind in Partitionen aufgeteilt. Consumer lesen Records aus Partitionen. Consumer Groups steuern, welche Instanz eines Services welche Partitionen verarbeitet. Kafka speichert Events für eine definierte Retention-Zeit, wodurch Consumer später lesen, erneut lesen oder nach Ausfällen aufholen können.

Wichtig ist: Kafka entkoppelt Services nicht vollständig von Verantwortung. Es entfernt direkte synchrone Laufzeitkopplung, erzeugt aber neue Vertrags-, Schema-, Betriebs- und Konsistenzverantwortung.

Ein Kafka-Event ist ein langlebiger Vertrag. Sobald es veröffentlicht wird, können unbekannte Consumer darauf reagieren. Deshalb ist ein Event nicht einfach „ein DTO auf einem Topic“, sondern ein öffentlicher Integrationsvertrag.

---

## 5. Wann Event-Driven Architecture sinnvoll ist

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Fachliches Ereignis | Etwas ist geschehen und andere Systeme dürfen reagieren. | `OrderPlaced`, `PaymentCaptured` | Geeignet |
| Temporal Entkopplung | Producer soll nicht auf Consumer warten. | Notification nach Bestellung | Geeignet |
| Fan-out | Mehrere Consumer reagieren unabhängig. | Inventory, Analytics, Mail | Geeignet |
| Eventual Consistency akzeptabel | Sofortige Antwort hängt nicht vom Consumer ab. | Analytics-Aktualisierung | Geeignet |
| Auditierbarer Ereignisstrom | Historie ist fachlich relevant. | Statusänderungen | Geeignet |
| Synchrones Ergebnis nötig | Aufrufer braucht sofortige Antwort des Zielsystems. | Kreditkartenautorisierung im Checkout | REST/RPC prüfen |
| Starke Transaktion über Systeme | Alles muss atomar gleichzeitig committed werden. | Buchung + externe Zahlung | Spezialdesign nötig |
| Simple CRUD | Keine relevanten Folgereaktionen. | Admin-Lookup-Tabelle | Kafka meist unnötig |
| Request/Reply über Kafka | Quasi-synchroner RPC über Topics. | „warte auf Antworttopic“ | Kritisch prüfen |

---

## 6. Ereignistypen

### 6.1 Domain Event

Ein Domain Event beschreibt eine fachliche Tatsache in der Vergangenheit.

```text
OrderPlaced
PaymentCaptured
CustomerRegistered
SubscriptionCancelled
```

Regel: Der Name steht in der Vergangenheitsform oder beschreibt einen bereits eingetretenen Zustand. Ein Event ist keine Aufforderung.

### 6.2 Command

Ein Command fordert ein anderes System auf, etwas zu tun.

```text
ReserveInventory
SendInvoiceEmail
StartShipment
```

Commands über Kafka sind möglich, aber sie erzeugen stärkere Kopplung als Domain Events. Sie müssen besonders sorgfältig hinsichtlich Ownership, Retry, Idempotenz und Fehlerantwort modelliert werden.

### 6.3 Integration Event

Ein Integration Event ist eine externe, stabile Repräsentation eines fachlichen Ereignisses.

Das interne Domain Event kann anders aussehen als das veröffentlichte Integration Event. Das ist oft sinnvoll, weil externe Verträge stabiler sein müssen als interne Modelle.

---

## 7. Topic-Naming

Topic-Namen müssen stabil, sprechend und versionierbar sein.

Empfohlenes Schema:

```text
<domain>.<event-name>.v<major-version>
```

Beispiele:

```text
orders.placed.v1
orders.cancelled.v1
payments.captured.v1
customers.registered.v1
inventory.reservation-failed.v1
```

Nicht empfohlen:

```text
topic1
events
order
new-order
test
prod-orders
```

Regeln:

1. Keine Umgebung im Topic-Namen, wenn Cluster/Namespace die Umgebung bereits trennt.
2. Major-Version nur bei inkompatiblen Änderungen erhöhen.
3. Topic beschreibt Fachlichkeit, nicht Producer-Klassenname.
4. DLQ-/DLT-Topics folgen einer festen Konvention.
5. Temporäre Topics müssen ein Ablaufdatum haben.

---

## 8. Event Envelope

Ein Event sollte einen stabilen technischen Umschlag besitzen. Der Umschlag erleichtert Korrelation, Idempotenz, Versionierung und Betrieb.

```java
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String producer,
        String correlationId,
        String causationId,
        String tenantId,
        T payload
) {}
```

Regeln:

- `eventId` ist global eindeutig und wird für Idempotenz genutzt.
- `eventType` beschreibt den fachlichen Typ.
- `eventVersion` beschreibt die Schemakompatibilität.
- `occurredAt` ist der Zeitpunkt des fachlichen Ereignisses.
- `producer` identifiziert den veröffentlichenden Service.
- `correlationId` verbindet Ereignisse eines Geschäftsflusses.
- `causationId` verweist optional auf das auslösende Event.
- `tenantId` ist bei SaaS-Systemen Pflicht, sofern Eventdaten tenantbezogen sind.
- `payload` enthält die fachlichen Daten.

Keine sensitiven Daten in Envelope-Feldern. Keine E-Mail, keine Namen, keine Tokens, keine IBAN.

---

## 9. Event Payload: fachlich stabil und minimal

Beispiel:

```java
public record OrderPlacedPayload(
        String orderId,
        String customerId,
        List<OrderItemPayload> items,
        String currency,
        long totalCents
) {}
```

```java
public record OrderItemPayload(
        String productId,
        int quantity,
        long unitPriceCents
) {}
```

Regeln:

1. Payload enthält nur Daten, die Consumer wirklich brauchen.
2. Keine internen Entity-Strukturen veröffentlichen.
3. Keine JPA-Entities als Event-Modelle verwenden.
4. Keine API-Response-DTOs als Event-Modelle wiederverwenden.
5. Geldbeträge nicht als `double` übertragen.
6. Zeitpunkte als ISO-8601 oder logischer Typ im Schema modellieren.
7. IDs eindeutig und stabil halten.
8. Personenbezogene Daten minimieren oder pseudonymisieren.

---

## 10. Producer-Konfiguration

### 10.1 Wichtige Producer-Einstellungen

```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      properties:
        schema.registry.url: ${SCHEMA_REGISTRY_URL}
        acks: all
        enable.idempotence: true
        delivery.timeout.ms: 120000
        request.timeout.ms: 30000
        linger.ms: 5
        compression.type: zstd
```

Wichtige Regeln:

- `acks=all` für zuverlässige Bestätigung.
- Idempotent Producer aktiviert lassen beziehungsweise bewusst aktivieren.
- Producer-Fehler beobachten und nicht ignorieren.
- Timeouts bewusst setzen.
- Compression bewusst wählen.
- Keine Secrets im YAML, sondern über Secret Management oder Environment bereitstellen.

Apache Kafka dokumentiert, dass Idempotenz standardmäßig aktiviert ist, sofern keine widersprüchlichen Konfigurationen gesetzt sind; explizit aktivierte Idempotenz verlangt unter anderem `acks=all` und passende Retry-/In-Flight-Konfiguration. Diese Details sind wichtig, weil falsch kombinierte Producer-Optionen Zuverlässigkeit reduzieren können.

### 10.2 Nicht ausreichend: blindes `fire and forget`

Schlecht:

```java
kafkaTemplate.send("orders.placed.v1", event);
```

Problem: Der Code ignoriert, ob der Send-Vorgang erfolgreich war. Für nicht-kritische Telemetrie kann das akzeptabel sein. Für fachliche Integration ist es zu schwach.

Besser:

```java
public void publishOrderPlaced(EventEnvelope<OrderPlacedPayload> event) {
    kafkaTemplate.send("orders.placed.v1", event.payload().orderId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event: eventId={}, topic={}",
                            event.eventId(),
                            "orders.placed.v1",
                            ex);
                    metrics.incrementPublishFailure("orders.placed.v1");
                    return;
                }

                log.info("Published event: eventId={}, topic={}, partition={}, offset={}",
                        event.eventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            });
}
```

Wenn die Veröffentlichung fachlich kritisch ist, reicht auch das nicht zwingend. Dann ist das Outbox-Pattern zu verwenden.

---

## 11. Partition Key

Der Key bestimmt, in welche Partition ein Record geschrieben wird. Reihenfolge ist in Kafka nur innerhalb einer Partition garantiert.

Regel:

```text
Partition Key = fachliche Aggregat-ID
```

Beispiele:

| Event | Empfohlener Key | Warum |
|---|---|---|
| `OrderPlaced` | `orderId` | Reihenfolge pro Bestellung |
| `OrderCancelled` | `orderId` | Statusereignisse derselben Bestellung geordnet |
| `PaymentCaptured` | `paymentId` oder `orderId` | je nach fachlichem Ordering |
| `CustomerUpdated` | `customerId` | Reihenfolge pro Kunde |
| `TenantConfigurationChanged` | `tenantId` | Reihenfolge pro Tenant |

Nicht empfohlen:

```java
kafkaTemplate.send(topic, UUID.randomUUID().toString(), event);
```

Das zerstört fachliche Reihenfolge.

---

## 12. Outbox Pattern

### 12.1 Problem

Wenn ein Service in einer Datenbank speichert und anschließend ein Kafka-Event sendet, entstehen zwei unterschiedliche Transaktionssysteme.

Schlecht:

```java
@Transactional
public void placeOrder(CreateOrderCommand command) {
    var order = orderRepository.save(Order.from(command));

    kafkaTemplate.send("orders.placed.v1", order.id().value().toString(), OrderPlacedEvent.from(order));
}
```

Fehlerfälle:

- Datenbank-Commit erfolgreich, Kafka-Send schlägt fehl.
- Kafka-Send erfolgreich, Datenbank-Transaktion rollt zurück.
- Anwendung crasht zwischen DB und Kafka.
- Event wird doppelt gesendet.
- Consumer sieht Ereignis, zu dem die Datenbank noch nicht konsistent ist.

### 12.2 Gute Anwendung: Transactional Outbox

```java
@Transactional
public void placeOrder(CreateOrderCommand command) {
    var order = orderRepository.save(Order.from(command));

    var event = OutboxEvent.create(
            UUID.randomUUID(),
            "orders.placed.v1",
            order.id().value().toString(),
            OrderPlacedPayload.from(order),
            Instant.now()
    );

    outboxRepository.save(event);
}
```

Ein separater Publisher liest die Outbox-Tabelle und publiziert nach Kafka:

```java
@Component
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay:PT1S}")
    public void publishPendingEvents() {
        var events = outboxRepository.findNextBatch(100);

        for (var event : events) {
            kafkaTemplate.send(event.topic(), event.aggregateId(), event.payload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            outboxRepository.markPublished(event.id());
                        } else {
                            outboxRepository.markFailed(event.id(), ex.getMessage());
                        }
                    });
        }
    }
}
```

Für hohe Anforderungen ist ein bewährter Outbox-Mechanismus wie Debezium-basierte CDC oder ein robuster interner Outbox-Dispatcher zu prüfen.

---

## 13. Consumer-Konfiguration

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      group-id: inventory-service
      auto-offset-reset: earliest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      properties:
        schema.registry.url: ${SCHEMA_REGISTRY_URL}
        specific.avro.reader: true
    listener:
      ack-mode: manual
      observation-enabled: true
```

Regeln:

1. `group-id` ist bewusst und servicebezogen zu wählen.
2. `enable-auto-commit=false` für kontrollierte Verarbeitung.
3. Ack erst nach erfolgreicher Verarbeitung.
4. Fehlerstrategie zentral konfigurieren.
5. Consumer muss idempotent sein.
6. Consumer Lag muss beobachtet werden.
7. Deserialisierungsfehler müssen in eine definierte Fehlerstrategie laufen.
8. Keine endlose Retry-Schleife ohne Backoff und DLQ.

---

## 14. Consumer: gute Verarbeitung

```java
@Component
public class InventoryOrderEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryOrderEventConsumer.class);

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(
            topics = "orders.placed.v1",
            groupId = "inventory-service"
    )
    @Transactional
    public void onOrderPlaced(
            @Payload EventEnvelope<OrderPlacedPayload> envelope,
            Acknowledgment acknowledgment) {

        if (processedEventRepository.existsByEventId(envelope.eventId())) {
            log.info("Skipping duplicate event: eventId={}, eventType={}",
                    envelope.eventId(),
                    envelope.eventType());
            acknowledgment.acknowledge();
            return;
        }

        inventoryService.reserve(
                new OrderId(UUID.fromString(envelope.payload().orderId())),
                envelope.payload().items()
        );

        processedEventRepository.markProcessed(
                envelope.eventId(),
                envelope.eventType(),
                envelope.occurredAt()
        );

        acknowledgment.acknowledge();
    }
}
```

Wichtig: Markierung als verarbeitet und fachliche Verarbeitung müssen in derselben lokalen Transaktion erfolgen, sofern eine Datenbank beteiligt ist.

---

## 15. Idempotenz

Kafka-Consumer müssen davon ausgehen, dass Events mehrfach verarbeitet werden können.

Ursachen:

- Retry nach technischem Fehler
- Consumer-Neustart vor Commit
- Rebalancing
- erneutes Abspielen alter Events
- manuelles Zurücksetzen von Offsets
- Producer-Wiederholung
- Outbox-Re-Publish
- DLT-Reprocessing

### 15.1 Schlechte Anwendung

```java
@KafkaListener(topics = "orders.placed.v1")
public void onOrderPlaced(OrderPlacedEvent event) {
    inventory.reduce(event.items());
}
```

Wenn das Event zweimal ankommt, wird Bestand doppelt reduziert.

### 15.2 Gute Anwendung: Processed-Event-Tabelle

```sql
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(200) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);
```

```java
@Transactional
public boolean markIfNotProcessed(UUID eventId, String eventType) {
    try {
        jdbcTemplate.update("""
                INSERT INTO processed_events(event_id, event_type, processed_at)
                VALUES (?, ?, now())
                """, eventId, eventType);
        return true;
    } catch (DuplicateKeyException alreadyProcessed) {
        return false;
    }
}
```

```java
@Transactional
public void handle(EventEnvelope<OrderPlacedPayload> event) {
    if (!processedEventService.markIfNotProcessed(event.eventId(), event.eventType())) {
        return;
    }

    inventoryService.reserve(...);
}
```

Diese Variante nutzt die Datenbank-Unique-Constraint als atomare Idempotenzsicherung.

---

## 16. Fehlerbehandlung, Retry und Dead Letter Topic

### 16.1 Fehlerarten

| Fehlerart | Beispiel | Strategie |
|---|---|---|
| Temporärer technischer Fehler | Datenbank kurz nicht erreichbar | Retry mit Backoff |
| Dauerhafter technischer Fehler | Schema kann nicht deserialisiert werden | DLT + Alarm |
| Fachlicher erwartbarer Fehler | Bestand reicht nicht | Fachliches Folgeevent oder DLT je nach Prozess |
| Poison Message | Event verletzt Contract dauerhaft | DLT, Analyse, Fix |
| Downstream nicht verfügbar | externer Provider down | Retry, Circuit Breaker, DLT nach Limit |

### 16.2 Spring Kafka DefaultErrorHandler

```java
@Configuration
class KafkaErrorHandlingConfiguration {

    @Bean
    DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        var backOff = new FixedBackOff(1_000L, 3L);

        var errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                org.apache.kafka.common.errors.SerializationException.class
        );

        return errorHandler;
    }
}
```

Spring Kafka stellt mit `DefaultErrorHandler` und `DeadLetterPublishingRecoverer` zentrale Mechanismen bereit, um fehlerhafte Records nach definierten Retry-Versuchen in Dead-Letter-Topics zu publizieren.

### 16.3 Regeln für DLT

1. Jedes DLT hat einen Owner.
2. Jedes DLT hat Monitoring.
3. Jedes DLT hat ein Runbook.
4. DLT-Nachrichten werden nicht ignoriert.
5. Reprocessing ist definiert.
6. DLT enthält keine unnötigen sensitiven Daten.
7. Alarmierung erfolgt bei Menge, Alter oder kritischem Eventtyp.
8. DLT-Retention ist bewusst gesetzt.

---

## 17. Schema Registry und Avro

### 17.1 Warum Schema Registry?

Event-Schemas sind Verträge zwischen Teams. Ohne Schema Registry können Producer inkompatible Events veröffentlichen und Consumer brechen erst zur Laufzeit.

Schema Registry ermöglicht:

- zentrale Schemaablage,
- Kompatibilitätsprüfung,
- Versionierung,
- Codegenerierung,
- kontrollierte Evolution.

### 17.2 Beispiel: Avro-Schema

```json
{
  "type": "record",
  "name": "OrderPlacedEvent",
  "namespace": "com.example.events.orders.v1",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "orderId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "currency", "type": "string", "default": "EUR" },
    { "name": "totalCents", "type": "long" },
    { "name": "occurredAt", "type": "string" }
  ]
}
```

### 17.3 Kompatibilitätsregeln

Backward-kompatible Änderungen:

- optionales Feld mit Default hinzufügen,
- neues Feld mit sinnvollem Default hinzufügen,
- Dokumentation ergänzen,
- zusätzliche Enum-Werte nur mit Consumer-Kompatibilitätsprüfung.

Breaking Changes:

- Feld entfernen,
- Feld umbenennen,
- Feldtyp ändern,
- Pflichtfeld ohne Default hinzufügen,
- Semantik eines Feldes ändern,
- Topic-Bedeutung ändern,
- Event als Command umdeuten.

Confluent dokumentiert `BACKWARD` als Standard-Kompatibilitätsmodus für Schema Registry. Für Kafka ist das relevant, weil Consumer ältere Nachrichten aus Topics oder Changelogs lesen können müssen.

---

## 18. Event-Versionierung

Empfohlene Regel:

```text
Kompatible Änderung → gleiches Topic, neue Schema-Version.
Inkompatible Änderung → neues Major-Version-Topic.
```

Beispiel:

```text
orders.placed.v1
orders.placed.v2
```

Migration:

1. Producer publiziert optional beide Versionen.
2. Consumer migrieren auf v2.
3. Monitoring prüft, ob v1 noch konsumiert wird.
4. v1 wird deprecated.
5. v1 wird nach kommunizierter Frist abgeschaltet.

Keine stillen Breaking Changes.

---

## 19. Ordering und Parallelität

Kafka garantiert Reihenfolge nur innerhalb einer Partition. Deshalb ist der Partition Key entscheidend.

Wenn alle Events einer Bestellung geordnet verarbeitet werden müssen:

```java
kafkaTemplate.send("orders.events.v1", orderId.toString(), event);
```

Wenn `orderId` nicht als Key verwendet wird, können Events derselben Bestellung in verschiedenen Partitionen landen und ungeordnet verarbeitet werden.

Consumer-Parallelität erhöht Durchsatz, aber nicht ohne Grenzen:

- Mehr Consumer als Partitionen bringen für ein Topic keine zusätzliche Parallelität.
- Mehr Partitionen erhöhen Betriebs- und Rebalancing-Komplexität.
- Reihenfolge über mehrere Aggregate hinweg ist nicht garantiert.
- Globale Reihenfolge ist bei Kafka in der Regel kein realistisches Designziel.

---

## 20. Eventual Consistency

Event-Driven Architecture bedeutet häufig: Daten sind nicht sofort überall konsistent.

Beispiel:

1. Order Service veröffentlicht `OrderPlaced`.
2. Inventory Service reserviert Bestand einige Sekunden später.
3. Notification Service sendet E-Mail später.
4. Analytics Service aktualisiert Reporting später.

Daraus folgen Regeln:

- UI muss Zwischenzustände darstellen können.
- Fachprozesse brauchen Statusmodelle.
- Timeouts und Kompensationen müssen definiert sein.
- Monitoring muss Verzögerungen sichtbar machen.
- Nutzer dürfen nicht falsche Sofortkonsistenzversprechen erhalten.

Beispiel:

```text
OrderStatus = PENDING_INVENTORY_RESERVATION
```

statt sofort:

```text
OrderStatus = CONFIRMED
```

wenn Inventory asynchron reserviert wird.

---

## 21. Observability

Jeder Producer und Consumer muss beobachtbar sein.

Pflichtsignale:

- Publish-Erfolgsrate
- Publish-Fehlerrate
- Consumer-Verarbeitungsdauer
- Consumer-Fehlerrate
- Consumer Lag
- DLT-Rate
- Retry-Anzahl
- Deserialisierungsfehler
- Schema-Fehler
- Verarbeitete Events pro Topic
- Zeit zwischen `occurredAt` und Verarbeitung
- Correlation IDs in Logs/Traces

Beispiel:

```java
log.info("Event consumed: eventId={}, eventType={}, topic={}, partition={}, offset={}",
        envelope.eventId(),
        envelope.eventType(),
        topic,
        partition,
        offset);
```

Nicht loggen:

```java
log.info("Event payload={}", envelope.payload());
```

Payloads können personenbezogene oder geschäftskritische Daten enthalten.

---

## 22. Tracing

Bei Event-Flows muss die Korrelation über Servicegrenzen erhalten bleiben. Dazu werden Trace- und Correlation-Informationen im Header oder Envelope weitergereicht.

Beispiel Header:

```java
var record = new ProducerRecord<String, EventEnvelope<OrderPlacedPayload>>(
        "orders.placed.v1",
        event.payload().orderId(),
        event
);

record.headers().add("correlationId", event.correlationId().getBytes(StandardCharsets.UTF_8));
record.headers().add("eventId", event.eventId().toString().getBytes(StandardCharsets.UTF_8));

kafkaTemplate.send(record);
```

Regeln:

- Keine PII in Headers.
- Keine Secrets in Headers.
- Correlation IDs stabil weiterreichen.
- Trace-Kontext nicht manuell fälschen.
- OpenTelemetry/Micrometer Observation verwenden, wenn verfügbar.

---

## 23. Security- und SaaS-Aspekte

### 23.1 Keine sensitiven Daten in Events

Events landen in Topics, Logs, Replay-Prozessen, DLTs, Backups, lokalen Debug-Tools und teilweise Analytics-Systemen. Deshalb gilt Datenminimierung.

Nicht erlaubt:

```java
public record CustomerRegisteredEvent(
        String email,
        String fullName,
        String passwordHash,
        String phone,
        String iban
) {}
```

Besser:

```java
public record CustomerRegisteredEvent(
        String customerId,
        String tenantId,
        Instant occurredAt
) {}
```

Consumer, die Details benötigen, können diese über berechtigte APIs oder interne read models abrufen.

### 23.2 Tenant-Isolation

Bei SaaS-Systemen muss jedes tenantbezogene Event den Tenant-Kontext enthalten oder eindeutig aus dem Key ableitbar machen.

```java
public record TenantScopedEvent<T>(
        UUID eventId,
        String tenantId,
        T payload
) {}
```

Consumer müssen Tenant-Kontext prüfen und dürfen nicht tenantübergreifend aggregieren, wenn das fachlich oder regulatorisch nicht erlaubt ist.

### 23.3 Zugriff auf Topics

Kafka-ACLs müssen least privilege umsetzen:

- Producer darf nur auf eigene Topics schreiben.
- Consumer darf nur relevante Topics lesen.
- Admin-Rechte sind stark begrenzt.
- Schema Registry Zugriff ist geschützt.
- DLT-Zugriff ist beschränkt.
- Secrets liegen nicht in `application.yml`.

### 23.4 Logging

OWASP warnt vor sensitiven Informationen in Logs. Für Kafka gilt das besonders, weil Event-Payloads häufig große Datenobjekte enthalten. Logs dürfen deshalb Event-Metadaten enthalten, aber nicht pauschal vollständige Payloads.

---

## 24. Datenschutz und Retention

Kafka-Topics haben Retention. Dadurch bleiben Events bewusst für eine Zeit gespeichert. Das ist betrieblich nützlich, kann aber Datenschutzrisiken erzeugen.

Regeln:

1. Personenbezogene Daten in Events vermeiden.
2. Retention pro Topic fachlich begründen.
3. DLT-Retention begrenzen.
4. Kompakte technische IDs statt Klardaten verwenden.
5. Löschkonzepte bei personenbezogenen Events definieren.
6. Backups und Mirror Topics berücksichtigen.
7. Reprocessing darf keine gelöschten personenbezogenen Daten wiederherstellen.

Wenn personenbezogene Daten zwingend in Events müssen, braucht das Event eine Datenschutzbewertung, Retention-Entscheidung und Zugriffsbegrenzung.

---

## 25. Testing

### 25.1 Unit-Test des Producers

```java
@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    KafkaTemplate<String, EventEnvelope<OrderPlacedPayload>> kafkaTemplate;

    @Test
    void publish_usesOrderIdAsKey() {
        var publisher = new OrderEventPublisher(kafkaTemplate);
        var event = validOrderPlacedEvent();

        publisher.publish(event);

        verify(kafkaTemplate).send(
                eq("orders.placed.v1"),
                eq(event.payload().orderId()),
                eq(event)
        );
    }
}
```

### 25.2 Consumer-Test mit Idempotenz

```java
@Test
void consume_skipsEvent_whenAlreadyProcessed() {
    when(processedEventRepository.existsByEventId(event.eventId())).thenReturn(true);

    consumer.onOrderPlaced(event, acknowledgment);

    verifyNoInteractions(inventoryService);
    verify(acknowledgment).acknowledge();
}
```

### 25.3 Integrationstest mit Kafka Testcontainers

```java
@SpringBootTest
@Testcontainers
class OrderKafkaIntegrationTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Autowired
    KafkaTemplate<String, EventEnvelope<OrderPlacedPayload>> kafkaTemplate;

    @Test
    void consumerProcessesOrderPlacedEvent() {
        var event = validOrderPlacedEvent();

        kafkaTemplate.send("orders.placed.v1", event.payload().orderId(), event);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        assertThat(inventoryRepository.hasReservation(event.payload().orderId()))
                                .isTrue()
                );
    }
}
```

### 25.4 Contract-/Schema-Test

Schemas müssen im Build geprüft werden:

- Schema kann generiert werden.
- Schema ist kompatibel.
- Event enthält Pflichtmetadaten.
- Kein verbotenes Feld wie `password`, `token`, `iban`.
- Consumer kann alte Beispiel-Events lesen.

---

## 26. CI/CD-Gates

Pflicht-Gates für eventbasierte Services:

1. Unit-Tests für Producer/Consumer-Logik.
2. Integrationstest mit Kafka oder Testcontainers für kritische Flows.
3. Schema-Kompatibilitätsprüfung.
4. Keine neuen Events ohne Owner.
5. Keine `latest`-Images in Kafka-Testcontainers.
6. Keine sensiblen Felder im Schema ohne Freigabe.
7. DLT/Retry-Konfiguration geprüft.
8. Observability-Metriken vorhanden.
9. Security-Konfiguration für Kafka-Zugriff geprüft.
10. Outbox-Test für kritische Producer.

---

## 27. Anti-Patterns

### 27.1 Kafka als Ersatz für klare Servicegrenzen

Wenn die Fachgrenzen unklar sind, macht Kafka sie nicht klarer. Es verteilt nur die Unklarheit.

### 27.2 Event = komplette Entity

```java
public record OrderPlacedEvent(OrderEntity order) {}
```

JPA-Entities sind keine Event-Verträge.

### 27.3 Payload vollständig loggen

```java
log.info("Received event {}", event);
```

Gefahr: PII, Secrets, große Logs, Kosten.

### 27.4 Keine Idempotenz

Ein Consumer, der doppelte Events nicht verträgt, ist produktiv riskant.

### 27.5 Auto-Commit ohne Verarbeitungsbewusstsein

Automatisches Commit kann Offsets bestätigen, bevor fachliche Verarbeitung erfolgreich abgeschlossen ist.

### 27.6 Keine DLT-Strategie

Poison Messages blockieren Verarbeitung oder verschwinden unkontrolliert.

### 27.7 Breaking Schema Change im gleichen Topic

Feld entfernen oder umbenennen ohne Versionierung bricht Consumer.

### 27.8 Random Partition Key

Zerstört Reihenfolge pro Aggregat.

### 27.9 Kafka für synchrone Entscheidung

Wenn der Nutzer jetzt wissen muss, ob Zahlung autorisiert wurde, ist Kafka oft nicht der richtige primäre Mechanismus.

### 27.10 DLT ohne Owner

Ein DLT ohne Monitoring ist nur ein stilles Fehlergrab.

---

## 28. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Event-Typ | Ist es ein fachliches Ereignis oder ein Command? | `OrderPlaced` | Pflicht |
| Topic-Name | Ist der Name stabil und versioniert? | `orders.placed.v1` | Pflicht |
| Owner | Gibt es Producer- und Topic-Owner? | Team Order | Pflicht |
| Schema | Gibt es Avro/JSON Schema/Protobuf? | Schema Registry | Pflicht |
| Kompatibilität | Sind Änderungen kompatibel? | Feld mit Default | Pflicht |
| Key | Ist der Partition Key fachlich korrekt? | `orderId` | Pflicht |
| Idempotenz | Kann Consumer Events mehrfach verarbeiten? | `processed_events` | Pflicht |
| Outbox | Ist DB+Kafka-Konsistenz geregelt? | Outbox-Tabelle | Pflicht bei kritischen Events |
| Retry | Gibt es Backoff und Grenze? | 3 Versuche | Pflicht |
| DLT | Gibt es DLT, Monitoring und Runbook? | `orders.placed.v1.DLT` | Pflicht |
| Observability | Gibt es Metriken, Logs, Trace-Korrelation? | consumer lag, eventId | Pflicht |
| Security | Keine PII/Secrets im Event? | technische IDs | Pflicht |
| Tenant | Ist Tenant-Kontext enthalten? | `tenantId` | Pflicht bei SaaS |
| Tests | Gibt es Producer-, Consumer-, Schema- und Integrationstests? | Testcontainers | Pflicht |

---

## 29. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Topic-Namen müssen Schema <domain>.<event>.v<major> erfüllen.
- Event-Schemas dürfen keine Felder password, token, secret, authorization enthalten.
- Events müssen eventId, eventType, occurredAt enthalten.
- Producer-Properties müssen acks=all und idempotence nicht aktiv widersprechen.
- Consumer dürfen nicht ohne Fehlerhandler produktiv laufen.
- DLT-Konfiguration muss für produktive Listener vorhanden sein.
- Keine Kafka-Payload-Logs auf INFO.
- Integrationstests mit Kafka müssen @Tag("integration") tragen.
- Schema-Kompatibilitätsprüfung läuft in CI.
```

Beispielhafte ArchUnit-Regel:

```java
@ArchTest
static final ArchRule kafka_listeners_should_not_be_in_domain =
        noMethods()
                .that().areAnnotatedWith(KafkaListener.class)
                .should().beDeclaredInClassesThat()
                .resideInAPackage("..domain..")
                .because("Kafka ist Infrastruktur und gehört in Adapter oder Messaging-Pakete.");
```

---

## 30. Migration bestehender Kafka-Integrationen

Migration erfolgt in Schritten:

1. Alle Topics inventarisieren.
2. Producer und Consumer je Topic identifizieren.
3. Owner pro Topic festlegen.
4. Schemas erfassen oder einführen.
5. Topic-Naming vereinheitlichen.
6. Partition Key prüfen.
7. Idempotenz in Consumer ergänzen.
8. Error Handler und DLT ergänzen.
9. Outbox für kritische Producer ergänzen.
10. PII/Secrets in Events prüfen und entfernen.
11. Observability-Metriken ergänzen.
12. Integrationstests mit Kafka/Testcontainers ergänzen.
13. Schema-Kompatibilitätsprüfung in CI aktivieren.
14. Alte inkompatible Topics geordnet ausphasen.

---

## 31. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Event nur intern und temporär in einer Entwicklungsumgebung genutzt wird,
- ein Topic bewusst technische Telemetrie statt Fachereignisse transportiert,
- ein Consumer bewusst nicht idempotent sein muss, weil nachweislich keine Wiederholung schaden kann,
- ein Schema-Registry-System im Legacy-Cluster noch nicht vorhanden ist,
- ein sehr kleines internes System vorübergehend JSON ohne Registry nutzt,
- ein synchroner Use Case bewusst nicht auf Kafka migriert wird.

Ausnahmen müssen begründet werden. Bei produktiven fachlichen Events sind Owner, Idempotenz, Monitoring und Datenschutzprüfung nicht optional.

---

## 32. Definition of Done

Ein Kafka-basierter Event-Flow erfüllt diese Richtlinie, wenn:

1. das Event fachlich klar benannt ist,
2. ein stabiler Topic-Name existiert,
3. ein Owner für Topic und Schema benannt ist,
4. ein versioniertes Schema existiert,
5. die Schema-Kompatibilität in CI geprüft wird,
6. Producer-Konfiguration zuverlässig ist,
7. ein fachlich korrekter Partition Key verwendet wird,
8. kritische DB+Kafka-Flüsse über Outbox oder gleichwertige Strategie abgesichert sind,
9. Consumer idempotent sind,
10. Retry und DLT definiert sind,
11. DLT überwacht und reprocessbar ist,
12. Consumer Lag und Fehler beobachtet werden,
13. Logs keine vollständigen Payloads oder sensitiven Daten enthalten,
14. Tenant-Kontext bei SaaS-Events berücksichtigt ist,
15. Tests Producer, Consumer, Schema und Integration abdecken,
16. Datenschutz und Retention bewertet sind.

---

## 33. Quellen und weiterführende Literatur

- Apache Kafka Producer Configs: https://kafka.apache.org/41/configuration/producer-configs/
- Spring Kafka Reference — Error Handling: https://docs.spring.io/spring-kafka/docs/3.1.9/reference/kafka/annotation-error-handling.html
- Spring Kafka Reference Documentation: https://docs.spring.io/spring-kafka/reference/
- Confluent Schema Registry — Schema Evolution and Compatibility: https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html
- Confluent Pattern — Schema Compatibility: https://developer.confluent.io/patterns/event-stream/schema-compatibility/
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- Martin Fowler — What do you mean by “Event-Driven”?: https://martinfowler.com/articles/201701-event-driven.html
- Chris Richardson — Transactional Outbox Pattern: https://microservices.io/patterns/data/transactional-outbox.html
