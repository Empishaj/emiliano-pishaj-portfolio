# ADR-121 — Avro Schema Registry: Event-Schema-Evolution sicher managen

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Messaging · Schema-Management · Kompatibilität                |
| Betroffene Teams  | Alle Teams die Kafka-Events produzieren oder konsumieren      |
| Abhängigkeiten    | ADR-041 (Kafka), ADR-095 (AsyncAPI), ADR-097 (DLQ)           |

---

## 1. Das Problem ohne Schema Registry

```
OHNE SCHEMA REGISTRY:
  Producer serialisiert OrderPlacedEvent als JSON.
  Consumer deserialisiiert OrderPlacedEvent als Java-Klasse.
  
  PROBLEM: Schema-Änderungen sind unsichtbar!
  
  Producer-Team: "Wir haben 'totalCents' in 'amountCents' umbenannt."
  Consumer-Team: (weiß nichts davon)
  Consumer: DeserializationException bei nächstem Kafka-Message
  → Produktion down bis Consumer manuell aktualisiert

MIT SCHEMA REGISTRY:
  Schemas sind zentral registriert und versioniert.
  Kompatibilitätsprüfung verhindert inkompatible Änderungen.
  Consumer findet Schema per Message-ID automatisch.
  Breaking Change ohne Migration → Registry blockiert den Deploy.
```

---

## 2. Entscheidung

Confluent Schema Registry (oder Redpanda Schema Registry als Alternative)
wird für alle Kafka-Event-Schemas verwendet. Alle Schemas sind Avro (
binär, kompakt, stark typisiert). Kompatibilitätsmodus: BACKWARD_TRANSITIVE.

---

## 3. Avro Schema definieren

```json
// src/main/avro/OrderPlacedEvent.avsc
// Avro-Schema-Datei (JSON-Format)
{
  "type": "record",
  "name": "OrderPlacedEvent",
  "namespace": "com.example.orders.events.v1",
  "doc": "Wird publiziert wenn eine Bestellung erfolgreich angelegt wurde.",
  "fields": [
    {
      "name": "orderId",
      "type": "string",
      "doc": "UUID der Bestellung"
    },
    {
      "name": "customerId",
      "type": "string"
    },
    {
      "name": "occurredAt",
      "type": {
        "type": "long",
        "logicalType": "timestamp-millis"
      },
      "doc": "Zeitpunkt der Bestellanlage in UTC"
    },
    {
      "name": "items",
      "type": {
        "type": "array",
        "items": {
          "type": "record",
          "name": "OrderItemData",
          "fields": [
            {"name": "productId",   "type": "string"},
            {"name": "quantity",    "type": "int"},
            {"name": "unitPriceCents", "type": "long"}
          ]
        }
      }
    },
    {
      "name": "totalCents",
      "type": "long",
      "doc": "Gesamtbetrag in Cent (kein Float! Keine Rundungsfehler)"
    },
    {
      "name": "currency",
      "type": "string",
      "doc": "ISO 4217 Währungscode (z.B. 'EUR')"
    }
  ]
}
```

---

## 4. Avro Code-Generierung

```kotlin
// build.gradle.kts
plugins {
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

dependencies {
    implementation("org.apache.avro:avro:1.11.3")
    implementation("io.confluent:kafka-avro-serializer:7.6.0")
}

avro {
    // Avro-Schemas aus .avsc-Dateien → Java-Klassen generieren
    fieldVisibility.set("PRIVATE")      // private Felder (kein public!)
    isGettersReturnOptional.set(true)   // Optional<T> statt null
    isCreateSetters.set(false)          // Immutable (keine Setter)
}
```

```bash
# Generierte Java-Klasse (automatisch, nicht manuell bearbeiten!):
# build/generated/main/avro/com/example/orders/events/v1/OrderPlacedEvent.java
```

---

## 5. Producer: Schema bei Registry registrieren

```java
@Configuration
public class KafkaAvroProducerConfig {

    @Value("${kafka.schema-registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public ProducerFactory<String, SpecificRecord> producerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);

        // Avro-Serializer: registriert Schema bei Registry beim ersten Send
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            KafkaAvroSerializer.class);

        // Schema Registry URL
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
            schemaRegistryUrl);

        // Kompatibilitätsprüfung aktivieren
        props.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, false);
        // false: Schema muss VOR dem Producer-Deploy registriert sein
        // (verhindert versehentliches Registrieren inkompatibler Schemas)

        return new DefaultKafkaProducerFactory<>(props);
    }
}

// Producer: sendet OrderPlacedEvent — Schema-ID wird automatisch in Message eingebettet
@Service
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publishOrderPlaced(Order order) {
        // Avro-generiertes Objekt
        var event = OrderPlacedEvent.newBuilder()
            .setOrderId(order.id().value())
            .setCustomerId(order.customerId().value())
            .setOccurredAt(order.createdAt().toEpochMilli())
            .setTotalCents(order.total().toMinorUnit())
            .setCurrency(order.total().currency().getCurrencyCode())
            .setItems(order.items().stream()
                .map(this::toAvroItem)
                .toList())
            .build();

        // Kafka-Message enthält im Header: Schema-ID (4 Bytes + Magic Byte)
        // Consumer findet Schema automatisch über Schema-ID
        kafkaTemplate.send("orders.placed", order.id().value(), event);
    }
}
```

---

## 6. Kompatibilitätsmodi: Welcher für uns?

```
NONE:
  Jede Schema-Änderung erlaubt.
  Nicht für Produktion geeignet.

BACKWARD (default):
  Neues Schema kann ÄLTERE Messages lesen.
  Consumer kann aktualisiert werden BEVOR Producer.
  Erlaubt: optionale Felder hinzufügen, Felder mit Default entfernen.

FORWARD:
  Älteres Schema kann NEUERE Messages lesen.
  Producer kann aktualisiert werden BEVOR Consumer.

FULL:
  Sowohl BACKWARD als auch FORWARD.
  Restriktivster Modus.

BACKWARD_TRANSITIVE (unsere Wahl):
  Neues Schema kann ALLE historischen Messages lesen.
  Sicherste Option für Kafka (Messages bleiben lange in Topics).
  Consumer kann immer noch alte Messages verarbeiten.
```

```bash
# Kompatibilitätsmodus für Subject setzen
curl -X PUT \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "BACKWARD_TRANSITIVE"}' \
  http://schema-registry:8081/config/orders.placed-value
```

---

## 7. Schema-Evolution: Richtige und falsche Änderungen

```json
// VERSION 1 (original):
{"name": "totalCents", "type": "long"}

// ✅ VERSION 2 BACKWARD-KOMPATIBEL: optionales Feld hinzufügen
{"name": "discountCents", "type": ["null", "long"], "default": null}
// null als Union-Type + null-Default → alte Consumer ignorieren das Feld

// ❌ VERSION 2 NICHT KOMPATIBEL: Pflichtfeld ohne Default hinzufügen
{"name": "taxCents", "type": "long"}
// → Alte Messages haben kein taxCents → DeserializationException

// ❌ VERSION 2 NICHT KOMPATIBEL: Feld umbenennen
// totalCents entfernen, amountCents hinzufügen
// → Alte Messages haben totalCents, neues Schema kennt es nicht

// ✅ MIGRATION-STRATEGIE bei Breaking Change:
// Schritt 1: Neues Topic "orders.placed.v2" erstellen
// Schritt 2: Beide Topics befüllen (dual-write)
// Schritt 3: Consumer migrieren zu v2
// Schritt 4: v1 deprecaten
// (→ ADR-110 API Deprecation, gleiche Strategie für Events)
```

---

## 8. CI/CD: Schema-Kompatibilität prüfen

```yaml
# .gitlab-ci.yml
schema:validate:
  stage: validate
  image: confluentinc/cp-schema-registry:latest
  script:
    - |
      # Schema gegen Registry auf Kompatibilität prüfen
      # BEVOR der Service deployed wird
      for schema_file in src/main/avro/*.avsc; do
        subject=$(basename "$schema_file" .avsc)
        echo "Prüfe Schema: $subject"

        response=$(curl -s -o /dev/null -w "%{http_code}" \
          -X POST \
          -H "Content-Type: application/vnd.schemaregistry.v1+json" \
          --data "{\"schema\": $(jq -c . < $schema_file | jq -Rs .)}" \
          "$SCHEMA_REGISTRY_URL/compatibility/subjects/${subject}-value/versions/latest")

        if [ "$response" != "200" ]; then
          echo "FEHLER: Schema $subject ist inkompatibel! Response: $response"
          exit 1
        fi
        echo "Schema $subject: kompatibel ✅"
      done
```

---

## 9. Consumer: Schema automatisch laden

```java
@Configuration
public class KafkaAvroConsumerConfig {

    @Bean
    public ConsumerFactory<String, SpecificRecord> consumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Avro-Deserializer: lädt Schema automatisch von Registry
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
            schemaRegistryUrl);

        // Specific Record: deserialize direkt in generierte Klasse
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(props);
    }
}

@KafkaListener(topics = "orders.placed")
public void onOrderPlaced(OrderPlacedEvent event) {
    // event ist ein typsicheres Avro-Objekt
    // Schema-Mismatch → DeserializationException → DLQ (→ ADR-097)
    log.info("Order received: {}", event.getOrderId());
    fulfillmentService.prepare(event);
}
```

---

## 10. Schema Registry UI und Monitoring

```yaml
# Schema Registry UI deployen (Confluent Control Center oder Kafdrop)
# Zeigt: alle Schemas, Versionen, Kompatibilität, Consumer-Groups

# Prometheus-Metriken der Schema Registry
- alert: SchemaRegistryDown
  expr: up{job="schema-registry"} == 0
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Schema Registry nicht erreichbar"
    description: "Alle Avro-Producer und Consumer scheitern bei neuen Connections"
```

---

## 11. Akzeptanzkriterien

- [ ] Alle Kafka-Event-Schemas als `.avsc`-Dateien im Repository
- [ ] `AUTO_REGISTER_SCHEMAS=false`: Schemas müssen vor Deploy registriert sein
- [ ] CI prüft Kompatibilität bevor Deploy (Schema Compatibility Check)
- [ ] Kompatibilitätsmodus: `BACKWARD_TRANSITIVE` für alle Topics
- [ ] Breaking-Change-Migration-Prozess dokumentiert (neues Topic, dual-write, deprecate)
- [ ] Schema Registry in Kubernetes mit Prometheus-Monitoring

---

## Quellen & Referenzen

- **Confluent Documentation, "Schema Registry"** — vollständige Referenz. docs.confluent.io
- **Martin Kleppmann, "Designing Data-Intensive Applications" (2017), Kap. 4** — Avro, Thrift, Protobuf Schema-Evolution.
- **Neha Narkhede, Gwen Shapira, Todd Palino, "Kafka: The Definitive Guide" (2022)** — Schema Registry und Avro-Integration.

---

## Verwandte ADRs

- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka-Grundkonfiguration
- [ADR-095](ADR-095-asyncapi-specification.md) — AsyncAPI als Gesamtdokumentation
- [ADR-097](ADR-097-dead-letter-queue.md) — DLQ für Schema-Fehler
- [ADR-110](ADR-110-api-deprecation.md) — Breaking-Change-Prozess (auch für Events)
