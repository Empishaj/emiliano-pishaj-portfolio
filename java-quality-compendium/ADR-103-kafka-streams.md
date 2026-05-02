# ADR-103 — Kafka Streams: Echtzeit-Datenstromverarbeitung

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Messaging · Stream-Processing · Echtzeit                      |
| Betroffene Teams  | Backend-Teams mit Aggregations-/Analyseanforderungen          |
| Abhängigkeiten    | ADR-041 (Kafka), ADR-032 (CQRS), ADR-095 (AsyncAPI)           |

---

## 1. Kontext & Treiber

### 1.1 Wann reicht Kafka Consumer nicht mehr?

```
KAFKA CONSUMER (ADR-041) — gut für:
  ✅ Einzelne Events verarbeiten (Order → Fulfillment vorbereiten)
  ✅ Einfache Zustandslosigkeit (jedes Event unabhängig)
  ✅ Seiteneffekte auslösen (E-Mail senden, DB schreiben)

KAFKA STREAMS — nötig für:
  ✅ Events AGGREGIEREN (Umsatz der letzten 5 Minuten)
  ✅ Events JOINEN (Bestellung + Kundendaten aus zwei Topics zusammenführen)
  ✅ Events FILTERN und TRANSFORMIEREN in neue Topics (ETL)
  ✅ ZEITFENSTER-Analysen (Bestellungen pro Stunde)
  ✅ STATEFUL Processing (Bestellhistorie pro Kunde aufbauen)

KONKRETES BEISPIEL:
  "Zeige Echtzeit-Dashboard: Umsatz letzte 5 Minuten, aufgeteilt nach Land"
  
  Mit Kafka Consumer: unmöglich ohne externe DB-Aggregation
  Mit Kafka Streams: direktes Aggregieren im Stream, Output in Topic
```

### 1.2 Kafka Streams vs. Apache Flink vs. Spark Streaming

```
KAFKA STREAMS:
  → Java-Library (kein separater Cluster!)
  → Läuft IN der Anwendung als embedded Library
  → Gut für: mittelkomplexe Stream-Processing in Kafka-Ökosystemen
  → Nicht gut für: komplexe ML-Pipelines, sehr große Zustände (TB)

APACHE FLINK:
  → Eigener Cluster (operativer Aufwand!)
  → Sehr leistungsfähig, komplexe Stateful-Operationen
  → Gut für: komplexe Event-Processing, große Zustände
  → Overkill für die meisten Anwendungsfälle

APACHE SPARK STREAMING:
  → Micro-Batch (nicht echtes Echtzeit-Streaming)
  → Gut für: Batch-nahe Analysen, Data Lake Integration

ENTSCHEIDUNG: Kafka Streams für unsere Anwendungsfälle
  → Keine zusätzliche Infrastruktur
  → Java-nativ (passt in Spring Boot)
  → Ausreichend für Aggregationen und Joins
```

---

## 2. Entscheidung

Kafka Streams wird für alle Anwendungsfälle verwendet die State über mehrere Events hinweg aufbauen oder mehrere Topics zusammenführen. Simple Event-Konsumierung (kein State, kein Join) bleibt bei Spring Kafka `@KafkaListener`.

---

## 3. Kafka Streams Grundkonzepte

```
TOPOLOGIE:
  Kafka Streams verarbeitet Events durch eine Topologie:
  Source-Topic → Processors (filter, map, aggregate) → Sink-Topic

STREAM (KStream):
  Endloser Datenstrom von Events (einer Event = eine Zeile)
  Beispiel: orders.placed Topic → jede Bestellung ist ein Event

TABLE (KTable):
  Aggregierter Zustand, der sich mit der Zeit ändert
  Beispiel: Aktueller Umsatz pro Stunde (immer aktuell, nicht historisch)

STATE STORE:
  Lokale RocksDB-Datenbank für Zustand (aggregierte Werte)
  Automatisch in Kafka-Topics repliziert (Fault-Tolerance)

WINDOWS:
  Zeitfenster für Aggregationen:
  Tumbling: feste Fenster (0-5min, 5-10min, ...)
  Hopping:  überlappende Fenster (0-5min, 1-6min, 2-7min, ...)
  Session:  nutzerbasiert (Events innerhalb X Minuten Inaktivität)
```

---

## 4. Implementierung: Spring Boot + Kafka Streams

### 4.1 Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")

    // JSON-Serialisierung für Streams
    implementation("io.confluent:kafka-streams-avro-serde:7.5.0")
    // Oder: eigene JSON-Serializer
}
```

```yaml
# application.yml
spring:
  kafka:
    streams:
      application-id: order-analytics    # Eindeutige App-ID
      bootstrap-servers: kafka:9092
      properties:
        default.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
        default.value.serde: org.springframework.kafka.support.serializer.JsonSerde
        commit.interval.ms: 1000         # Offset-Commit-Intervall
        num.stream.threads: 2            # Parallele Verarbeitungs-Threads
        replication.factor: 3            # Für interne Topics
```

### 4.2 Beispiel 1: Umsatz-Aggregation (5-Minuten-Fenster)

```java
// Use Case: "Umsatz der letzten 5 Minuten aufgeteilt nach Land"

@Configuration
@EnableKafkaStreams
public class RevenueAggregationStream {

    private static final Duration WINDOW_SIZE = Duration.ofMinutes(5);

    @Bean
    public KStream<String, OrderPlacedEvent> revenueAggregationTopology(
            StreamsBuilder builder) {

        // Serdes (Serializer/Deserializer) für unsere Typen
        var orderSerde   = new JsonSerde<>(OrderPlacedEvent.class);
        var revenueSerde = new JsonSerde<>(RevenueAggregate.class);

        // ── SOURCE: Events aus orders.placed lesen ────────────────────────
        KStream<String, OrderPlacedEvent> orderStream = builder.stream(
            "orders.placed",
            Consumed.with(Serdes.String(), orderSerde)
        );

        // ── TRANSFORM: Umsatz aggregieren pro Land, pro 5-Minuten-Fenster ─
        orderStream
            // Nur bestätigte Bestellungen (kein Pending)
            .filter((key, event) -> event.status().equals("CONFIRMED"))

            // Key ändern: von orderId → countryCode
            // Warum? Aggregation passiert PER KEY
            .selectKey((orderId, event) ->
                event.shippingAddress().countryCode())

            // Gruppieren nach neuem Key (countryCode)
            .groupByKey(Grouped.with(Serdes.String(), orderSerde))

            // Zeitfenster: 5-Minuten-Tumbling-Window
            .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))

            // Aggregieren: Umsatz summieren
            .aggregate(
                // Initialzustand
                () -> new RevenueAggregate(0L, 0),

                // Aggregations-Funktion: für jedes neue Event
                (countryCode, event, aggregate) -> new RevenueAggregate(
                    aggregate.totalCents() + event.totalCents(),
                    aggregate.orderCount() + 1
                ),

                // State-Store für Fault-Tolerance
                Materialized.<String, RevenueAggregate, WindowStore<Bytes, byte[]>>as(
                    "revenue-by-country-store")
                    .withValueSerde(revenueSerde)
            )

            // KTable → KStream (für Output in Topic)
            .toStream()

            // Key anreichern mit Zeitfenster-Info
            .map((windowedKey, aggregate) -> KeyValue.pair(
                windowedKey.key() + "@" + windowedKey.window().startTime(),
                new RevenueWindow(
                    windowedKey.key(),             // country
                    windowedKey.window().startTime(),
                    windowedKey.window().endTime(),
                    aggregate.totalCents(),
                    aggregate.orderCount()
                )
            ))

            // ── SINK: Ergebnisse in Output-Topic schreiben ─────────────────
            .to("orders.revenue.by-country-5min",
                Produced.with(Serdes.String(),
                    new JsonSerde<>(RevenueWindow.class)));

        return orderStream;
    }
}

// Aggregat-Typen (müssen serialisierbar sein!)
public record RevenueAggregate(long totalCents, int orderCount) {}

public record RevenueWindow(
    String  country,
    Instant windowStart,
    Instant windowEnd,
    long    totalCents,
    int     orderCount
) {}
```

### 4.3 Beispiel 2: Stream-Stream-Join (Bestellungen + Zahlungen)

```java
// Use Case: "Bestellungen mit ihrer Zahlungsbestätigung anreichern"
// Problem: OrderPlacedEvent und PaymentConfirmedEvent kommen in verschiedenen Topics

@Bean
public KStream<String, EnrichedOrderEvent> orderPaymentJoinTopology(
        StreamsBuilder builder) {

    var orderSerde   = new JsonSerde<>(OrderPlacedEvent.class);
    var paymentSerde = new JsonSerde<>(PaymentConfirmedEvent.class);
    var enrichedSerde = new JsonSerde<>(EnrichedOrderEvent.class);

    KStream<String, OrderPlacedEvent> orders = builder.stream(
        "orders.placed", Consumed.with(Serdes.String(), orderSerde));

    KStream<String, PaymentConfirmedEvent> payments = builder.stream(
        "payments.confirmed", Consumed.with(Serdes.String(), paymentSerde));

    // Stream-Stream-Join: beide Streams per orderId joinen
    // Zeitfenster: Payment muss innerhalb 10 Minuten nach Order kommen
    return orders.join(
        payments,

        // Join-Funktion: was passiert bei Match?
        (order, payment) -> new EnrichedOrderEvent(
            order.orderId(),
            order.customerId(),
            order.totalCents(),
            payment.stripePaymentId(),    // Aus Payment angereichert
            payment.confirmedAt()
        ),

        // Join-Fenster: bis zu 10 Minuten Toleranz
        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(10)),

        StreamJoined.with(Serdes.String(), orderSerde, paymentSerde)
    )
    .to("orders.enriched",
        Produced.with(Serdes.String(), enrichedSerde));
}
```

### 4.4 Beispiel 3: KTable für aktuelle Bestellzustände

```java
// Use Case: "Aktueller Status jeder Bestellung"
// Wie bei einem Materialized View — immer der neueste Stand

@Bean
public KTable<String, OrderStatusView> orderStatusTable(StreamsBuilder builder) {

    // Alle Status-Änderungen kommen hier rein
    KStream<String, OrderStatusChangedEvent> statusEvents = builder.stream(
        "orders.status-changed",
        Consumed.with(Serdes.String(), new JsonSerde<>(OrderStatusChangedEvent.class))
    );

    // KTable: neuester Wert pro Key (orderId)
    // Bei neuem Event mit gleichem Key: Wert wird ÜBERSCHRIEBEN (kein Aggregieren)
    return statusEvents
        .mapValues(event -> new OrderStatusView(
            event.orderId(),
            event.newStatus(),
            event.updatedAt()
        ))
        .toTable(
            Materialized.<String, OrderStatusView, KeyValueStore<Bytes, byte[]>>as(
                "order-status-store")  // Name des State-Stores
                .withValueSerde(new JsonSerde<>(OrderStatusView.class))
        );
}

// Interaktive Queries: State-Store direkt abfragen
@Service
public class OrderStatusQueryService {

    @Autowired
    private KafkaStreams kafkaStreams;

    public Optional<OrderStatusView> getCurrentStatus(String orderId) {
        // State-Store direkt abfragen — kein DB-Aufruf!
        var store = kafkaStreams.store(
            StoreQueryParameters.fromNameAndType(
                "order-status-store",
                QueryableStoreTypes.keyValueStore()));

        return Optional.ofNullable(store.get(orderId));
    }
}
```

---

## 5. Fehlerbehandlung in Streams

```java
@Configuration
public class StreamsErrorHandlingConfig {

    @Bean
    public StreamsBuilderFactoryBeanCustomizer streamsCustomizer() {
        return factoryBean -> {
            var props = new HashMap<String, Object>();

            // Deserialisierungsfehler: in DLQ statt Crash
            props.put(
                StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndContinueExceptionHandler.class    // Loggen + weitermachen
                // Alternative: LogAndFailExceptionHandler → Stream stoppt bei Fehler
            );

            // Produktion-Fehler: in DLQ
            props.put(
                StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                DefaultProductionExceptionHandler.class
            );

            factoryBean.setStreamsConfiguration(props);
        };
    }
}

// Eigener Exception-Handler für Business-Logik-Fehler
public class OrderStreamExceptionHandler
        implements DeserializationExceptionHandler {

    @Override
    public DeserializationHandlerResponse handle(
            ProcessorContext context,
            ConsumerRecord<byte[], byte[]> record,
            Exception exception) {

        log.error("Cannot deserialize event, sending to DLQ",
            kv("topic", record.topic()),
            kv("offset", record.offset()),
            kv("exception", exception.getMessage()));

        // Event in DLQ-Topic schreiben (→ ADR-097)
        dlqProducer.send(new ProducerRecord<>(
            record.topic() + ".DLT",
            record.key(),
            record.value()
        ));

        return DeserializationHandlerResponse.CONTINUE; // Stream läuft weiter
    }
}
```

---

## 6. Testing

```java
// TopologyTestDriver: Unit-Testing ohne echten Kafka-Cluster!
class RevenueAggregationStreamTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, OrderPlacedEvent> inputTopic;
    private TestOutputTopic<String, RevenueWindow> outputTopic;

    @BeforeEach
    void setup() {
        var builder = new StreamsBuilder();
        var stream  = new RevenueAggregationStream();
        stream.revenueAggregationTopology(builder);    // Topologie aufbauen

        var topology = builder.build();
        var props    = testStreamProperties();

        testDriver  = new TopologyTestDriver(topology, props);
        inputTopic  = testDriver.createInputTopic("orders.placed",
            new StringSerializer(), new JsonSerializer<>());
        outputTopic = testDriver.createOutputTopic("orders.revenue.by-country-5min",
            new StringDeserializer(), new JsonDeserializer<>(RevenueWindow.class));
    }

    @Test
    void aggregiertUmsatzPro5MinutenUndLand() {
        // Arrange: 3 Bestellungen aus DE in 5 Minuten
        var now = Instant.now();
        inputTopic.pipeInput("ord-1", confirmedOrder("DE", 1000), now);
        inputTopic.pipeInput("ord-2", confirmedOrder("DE", 2000), now.plusSeconds(60));
        inputTopic.pipeInput("ord-3", confirmedOrder("AT", 500),  now.plusSeconds(90));

        // Zeit voranschreiben damit Fenster schließt
        testDriver.advanceWallClockTime(Duration.ofMinutes(6));

        // Assert: DE-Aggregat korrekt
        var deRevenue = outputTopic.readValuesToList().stream()
            .filter(w -> w.country().equals("DE"))
            .findFirst().orElseThrow();

        assertThat(deRevenue.totalCents()).isEqualTo(3000);
        assertThat(deRevenue.orderCount()).isEqualTo(2);
    }

    @AfterEach
    void tearDown() { testDriver.close(); }
}
```

---

## 7. Wann Kafka Streams, wann @KafkaListener?

```
VERWENDE @KafkaListener (→ ADR-041) wenn:
  ✅ Einzelnes Event verarbeiten (stateless)
  ✅ Seiteneffekt auslösen (E-Mail, DB-Write, HTTP-Call)
  ✅ Einfacher Consumer ohne Aggregation
  Beispiel: OrderPlacedEvent → Fulfillment vorbereiten

VERWENDE Kafka Streams wenn:
  ✅ Aggregation über viele Events (Umsatz, Zählung)
  ✅ Join zweier Topics nötig
  ✅ Zeitfenster-Analyse (letzte 5 Minuten)
  ✅ Stateful Processing (Customer-Order-History)
  ✅ ETL-Pipeline (transformieren + filtern + neues Topic)
  Beispiel: Umsatz-Dashboard, Fraud-Detection, Recommendation-Engine
```

---

## 8. Akzeptanzkriterien

- [ ] Alle Kafka-Streams-Topologien haben Unit-Tests mit `TopologyTestDriver`
- [ ] State Stores haben Monitoring: Größe, Latency (über JMX/OTel)
- [ ] Deserialisierungsfehler landen in `.DLT`-Topic (→ ADR-097)
- [ ] `application-id` ist eindeutig pro Service-Instanz-Gruppe
- [ ] Replication-Factor für interne Topics = 3 (wie alle Produktions-Topics)

---

## Quellen & Referenzen

- **Apache Kafka Documentation, "Streams Developer Guide"** — offizielle Referenz.
- **Matthias J. Sax, "Kafka Streams in Action" (2021), Manning** — Praxisbuch mit vollständigen Beispielen.
- **Confluent, "Kafka Streams Interactive Queries"** — State-Store-Queries.

---

## Verwandte ADRs

- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka-Grundkonfiguration
- [ADR-032](ADR-032-cqrs.md) — CQRS: KTable als Read-Model
- [ADR-097](ADR-097-dead-letter-queue.md) — Fehlerbehandlung in Streams
