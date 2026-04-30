# ADR-095 — AsyncAPI: Events dokumentieren wie OpenAPI REST dokumentiert

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | API Design · Event-Driven · Dokumentation                     |
| Betroffene Teams  | Alle Teams die Kafka-Events produzieren oder konsumieren      |
| Abhängigkeiten    | ADR-041 (Kafka), ADR-066 (OpenAPI), ADR-089 (Context Mapping) |

---

## 1. Kontext & Treiber

### 1.1 Das Problem: Event-Verträge existieren nur im Kopf

```
SITUATION IN JEDEM WACHSENDEN EVENT-DRIVEN SYSTEM:

Team Orders produziert: OrderPlacedEvent
Team Fulfillment konsumiert: OrderPlacedEvent

Frage von Fulfillment an Orders: "Welche Felder hat das Event?"
Antwort: "Schau in die Klasse OrderPlacedEvent.java"
Follow-up: "In welchem Repository?"
Antwort: "Im orders-service Repository"
Follow-up: "Welcher Branch? Was ist die aktuelle Version?"
...

3 Meetings später haben beide Teams ein gemeinsames Verständnis.
3 Monate später: Orders ändert ein Feld → Fulfillment bricht lautlos.

WURZEL-URSACHE:
  REST-APIs haben OpenAPI (→ ADR-066) — entdeckbar, versioniert, validierbar.
  Kafka-Events haben... nichts. Sie existieren nur als Java-Klassen.
  
  Das Ergebnis: Events sind undokumentierte, implizite Contracts.
  Änderungen werden nicht kommuniziert.
  Consumer-Breaking-Changes werden nicht erkannt bis Produktion.
```

### 1.2 Warum AsyncAPI und nicht nur Avro/Protobuf?

```
AVRO/PROTOBUF (Schema für Serialisierung):
  ✓ Definiert das Datenformat eines Events
  ✗ Beschreibt NICHT: auf welchem Topic? Welche Partitionierung?
  ✗ Beschreibt NICHT: wer publiziert, wer konsumiert?
  ✗ Beschreibt NICHT: Semantik (wann wird es publiziert, warum?)

ASYNCAPI (Gesamtdokumentation des Event-Contracts):
  ✓ Welches Topic/Channel?
  ✓ Welcher Payload (Referenz auf Avro/JSON-Schema)?
  ✓ Wer ist Publisher, wer Consumer?
  ✓ Semantik: wann, warum, Geschäftsbedeutung
  ✓ Fehlerbehandlung: was passiert bei ungültigem Event?
  
→ AsyncAPI + Avro = vollständiger Event-Contract
```

---

## 2. Entscheidung

Alle Kafka-Topics die zwischen Bounded Contexts kommunizieren werden mit AsyncAPI 3.0 dokumentiert. Die Spezifikation lebt im Quellcode-Repository des publizierenden Services und wird in CI validiert und veröffentlicht. Avro-Schemas dienen als Payload-Definition, AsyncAPI als Gesamt-Vertrag.

---

## 3. AsyncAPI in der Praxis

### 3.1 Vollständige AsyncAPI-Spezifikation

```yaml
# src/main/resources/asyncapi/orders-events.yaml
asyncapi: "3.0.0"

info:
  title: Order Service Events
  version: "2.3.0"
  description: |
    Events die vom Order Service publiziert werden.
    Alle Consumer müssen sich an den Contract halten.
    Breaking Changes werden in der MAJOR-Version kommuniziert.
  contact:
    name: Orders Team
    email: orders-team@example.com
  license:
    name: Internal

defaultContentType: application/avro

servers:
  production:
    host: kafka.example.com:9092
    protocol: kafka
    description: Production Kafka Cluster
    security:
      - saslScram: []
  staging:
    host: kafka-staging.example.com:9092
    protocol: kafka

channels:
  # ── Channel 1: Order wurde aufgegeben ─────────────────────────────
  orders/placed:
    address: orders.placed
    description: |
      Wird publiziert wenn eine Bestellung erfolgreich angelegt wurde.
      Consumer: Inventory (Bestand reservieren), Notification (Bestätigungs-E-Mail),
                Finance (Rechnung vorbereiten), Fulfillment (Versand vorbereiten).
    messages:
      OrderPlacedEvent:
        $ref: '#/components/messages/OrderPlacedEvent'
    bindings:
      kafka:
        topic: orders.placed
        partitions: 12
        replicas: 3
        topicConfiguration:
          retention.ms: 604800000  # 7 Tage
          cleanup.policy: delete
        # Partitionierungs-Key: customerId → gleicher Kunde, gleiche Partition → Reihenfolge
        key:
          type: string
          description: "customerId — garantiert Reihenfolge pro Kunde"

  orders/cancelled:
    address: orders.cancelled
    description: |
      Wird publiziert wenn eine Bestellung storniert wird.
      Consumer MÜSSEN Kompensations-Aktionen durchführen (Bestand freigeben, Erstattung).
      Idempotenz-Pflicht: Consumer müssen doppelte Events tolerieren.
    messages:
      OrderCancelledEvent:
        $ref: '#/components/messages/OrderCancelledEvent'

  orders/status-changed:
    address: orders.status-changed
    description: |
      Wird bei jedem Statuswechsel publiziert.
      Consumer: Notification Service für Statusbenachrichtigungen.
    messages:
      OrderStatusChangedEvent:
        $ref: '#/components/messages/OrderStatusChangedEvent'

operations:
  publishOrderPlaced:
    action: send
    channel:
      $ref: '#/channels/orders~1placed'
    title: Order wurde aufgegeben
    summary: Order Service publiziert dieses Event nach erfolgreicher Bestellanlage
    description: |
      Wird in derselben Transaktion wie die Bestellanlage in die Outbox geschrieben
      (→ Transactional Outbox Pattern) und nach DB-Commit an Kafka gesendet.
      Garantie: exactly-once per Outbox-Semantik (nicht Kafka-native).
    bindings:
      kafka:
        groupId:
          type: string
          enum: [order-service]

components:
  messages:
    OrderPlacedEvent:
      name: OrderPlacedEvent
      title: Bestellung aufgegeben
      summary: Eine neue Bestellung wurde erfolgreich angelegt
      contentType: application/avro
      headers:
        type: object
        properties:
          correlationId:
            type: string
            format: uuid
            description: Tracing-Correlation-ID (→ ADR-086)
          eventVersion:
            type: string
            example: "1"
      payload:
        $ref: '#/components/schemas/OrderPlacedPayload'
      examples:
        - name: StandardBestellung
          summary: Normale Bestellung mit zwei Artikeln
          payload:
            orderId: "ord-12345678"
            customerId: "cust-87654321"
            occurredAt: "2024-03-15T10:30:00Z"
            items:
              - productId: "PROD-001"
                quantity: 2
                unitPriceCents: 2999
            totalCents: 5998
            currency: "EUR"
            shippingAddress:
              street: "Hauptstraße 1"
              city: "Berlin"
              zipCode: "10115"
              countryCode: "DE"

    OrderCancelledEvent:
      name: OrderCancelledEvent
      payload:
        $ref: '#/components/schemas/OrderCancelledPayload'

  schemas:
    OrderPlacedPayload:
      type: object
      required: [orderId, customerId, occurredAt, items, totalCents, currency]
      properties:
        orderId:
          type: string
          pattern: '^ord-[a-f0-9]{8,}$'
          description: Eindeutige Bestell-ID
        customerId:
          type: string
          description: Eindeutige Kunden-ID
        occurredAt:
          type: string
          format: date-time
          description: Zeitpunkt der Bestellanlage (UTC, ISO 8601)
        items:
          type: array
          minItems: 1
          items:
            $ref: '#/components/schemas/OrderItemData'
        totalCents:
          type: integer
          minimum: 1
          description: Gesamtbetrag in Cent (keine Fließkomma-Arithmetik!)
        currency:
          type: string
          pattern: '^[A-Z]{3}$'
          description: ISO 4217 Währungscode
        shippingAddress:
          $ref: '#/components/schemas/AddressData'

    OrderItemData:
      type: object
      required: [productId, quantity, unitPriceCents]
      properties:
        productId:
          type: string
        quantity:
          type: integer
          minimum: 1
        unitPriceCents:
          type: integer
          minimum: 0

    AddressData:
      type: object
      required: [street, city, zipCode, countryCode]
      properties:
        street:       { type: string }
        city:         { type: string }
        zipCode:      { type: string }
        countryCode:  { type: string, pattern: '^[A-Z]{2}$' }

    OrderCancelledPayload:
      type: object
      required: [orderId, customerId, reason, occurredAt]
      properties:
        orderId:     { type: string }
        customerId:  { type: string }
        reason:      { type: string, enum: [CUSTOMER_REQUEST, PAYMENT_FAILED, OUT_OF_STOCK] }
        occurredAt:  { type: string, format: date-time }
        refundCents: { type: integer, minimum: 0 }

  securitySchemes:
    saslScram:
      type: scramSha256
      description: SASL/SCRAM-SHA-256 Authentifizierung
```

### 3.2 Java-Code synchron zur AsyncAPI-Spec halten

```java
// Events müssen mit der AsyncAPI-Spec übereinstimmen
// Annotations schaffen die Verbindung

@AsyncAPISchema("OrderPlacedPayload")   // Verknüpft mit AsyncAPI-Komponente
public record OrderPlacedEvent(
    @NotBlank String  orderId,
    @NotBlank String  customerId,
    @NotNull  Instant occurredAt,
    @NotEmpty List<OrderItemData> items,
    @Positive long    totalCents,
    @Pattern(regexp = "^[A-Z]{3}$")
              String  currency,
    @NotNull  AddressData shippingAddress
) implements DomainEvent {

    // Validierung: Event-Invarianten zur Build-Zeit prüfen
    public OrderPlacedEvent {
        Objects.requireNonNull(orderId,     "orderId");
        Objects.requireNonNull(customerId,  "customerId");
        Objects.requireNonNull(occurredAt,  "occurredAt");
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("items must not be empty");
        if (totalCents <= 0)
            throw new IllegalArgumentException("totalCents must be positive");
    }
}
```

### 3.3 Consumer: Contract gegen Spec prüfen

```java
// Consumer-seitige Schema-Validierung (verhindert Silent Breaking Changes)
@Component
public class OrderPlacedEventValidator {

    private final Schema avroSchema;

    public OrderPlacedEventValidator() throws IOException {
        // Schema aus dem zentralen Schema-Registry laden (→ ADR-041)
        this.avroSchema = SchemaRegistryClient
            .getSchema("orders.placed-value", /* latest version */ -1);
    }

    public void validate(ConsumerRecord<String, byte[]> record) {
        try {
            avroDecoder.decode(record.value(), avroSchema);
        } catch (AvroDecodeException e) {
            // Schema-Verletzung → Dead Letter Queue (→ ADR-097)
            throw new EventSchemaViolationException(
                "OrderPlacedEvent violates schema v" + avroSchema.getVersion(), e);
        }
    }
}
```

---

## 4. Schema-Evolution: Regeln für Breaking vs. Non-Breaking

```
BACKWARD-COMPATIBLE (Consumer kann alte UND neue Events lesen):
  ✅ Neues optionales Feld hinzufügen (mit Default-Wert)
  ✅ Enum-Wert hinzufügen (wenn Consumer unbekannte Werte toleriert)
  ✅ Feld-Beschreibung ändern (nur Doku)

FORWARD-COMPATIBLE (Producer kann alte UND neue Consumer bedienen):
  ✅ Optionales Feld entfernen
  ✅ Default-Wert eines Feldes ändern

BREAKING CHANGE (MAJOR-Version erforderlich!):
  ❌ Pflichtfeld hinzufügen
  ❌ Feld umbenennen oder Typ ändern
  ❌ Feld entfernen das Consumer nutzen
  ❌ Enum-Wert entfernen

MIGRATION BEI BREAKING CHANGE:
  1. Neues Topic: orders.placed.v2
  2. Parallel-Phase: Producer schreibt in v1 UND v2
  3. Consumer migrieren auf v2
  4. v1 Topic deprecaten (Header: x-deprecated: true)
  5. v1 nach Übergangsfrist abschalten (max. 6 Monate)
```

### 4.1 Schema-Kompatibilität in CI erzwingen

```yaml
# .gitlab-ci.yml — Schema Registry Compatibility Check
schema:compatibility-check:
  stage: validate
  image: confluentinc/cp-schema-registry:latest
  script:
    # Prüfe ob neue Schema-Version kompatibel mit Registry ist
    - |
      curl -s -X POST \
        -H "Content-Type: application/vnd.schemaregistry.v1+json" \
        --data "{\"schema\": $(cat src/main/avro/OrderPlacedEvent.avsc | jq -c . | jq -Rs .)}" \
        http://schema-registry:8081/compatibility/subjects/orders.placed-value/versions/latest \
      | jq -e '.is_compatible == true'
    # Schlägt fehl wenn inkompatibel → Breaking Change nicht unbemerkt
```

---

## 5. AsyncAPI-Dokumentation veröffentlichen

```yaml
# CI: AsyncAPI-Docs generieren und deployen
docs:asyncapi:
  stage: docs
  image: asyncapi/generator:latest
  script:
    # HTML-Dokumentation generieren
    - ag src/main/resources/asyncapi/orders-events.yaml @asyncapi/html-template
        --output docs/asyncapi --force-write

    # Markdown für arc42-Integration
    - ag src/main/resources/asyncapi/orders-events.yaml @asyncapi/markdown-template
        --output docs/asyncapi/README.md --force-write

  artifacts:
    paths: [docs/asyncapi/]
    expire_in: 1 week

  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
```

---

## 6. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| Nur Avro-Schema | Typsicherheit, Schema Registry | Kein Topic-Kontext, keine Semantik | Events ohne Kontext = nicht selbstdokumentierend |
| Nur JavaDoc | Entwickler-vertraut | Nicht maschinenlesbar, veraltet sofort | Kein Tooling (kein Generator, kein Validator) |
| CloudEvents Standard | Interoperabel, herstellerneutral | Weniger Detail für Kafka-spezifische Konfig | Für internes System überspezifiziert |
| Confluence-Seiten | Flexibel | Entkoppelt vom Code, veraltet sofort | Dasselbe Problem wie ohne Dokumentation |

---

## 7. Trade-off-Matrix

| Qualitätsziel | AsyncAPI + Avro | Nur Avro | Kein Contract |
|---|---|---|---|
| Dokumentationsqualität | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐ |
| Maschinenlesbarkeit | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐ |
| Breaking-Change-Erkennung | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐ |
| Wartungsaufwand | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Onboarding neuer Consumer | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐ |

---

## 8. Kosten-Nutzen-Analyse

```
Initiale Kosten:
  AsyncAPI-Specs für bestehende Topics schreiben: 2–4 PT (je nach Anzahl Topics)
  CI-Integration (Schema Compatibility Check):    1 PT
  Tooling Setup (Generator, Portal):              1 PT
  Team-Schulung:                                  0.5 PT/Person
  GESAMT: ~10 PT = 6.400 EUR

Laufende Kosten:
  AsyncAPI bei Event-Änderung aktualisieren: 1–2h pro Change
  (erzwungen durch CI-Validierung — kein Vergessen möglich)

Nutzen:
  Debugging-Zeit bei Consumer-Problemen: -80% (Contract ist klar)
  Onboarding neuer Consumer-Teams: -3 Tage pro Team
  Breaking-Change-Incidents verhindert: 1 verhindert = ~8.000 EUR

Break-Even: nach dem ersten verhinderten Breaking-Change-Incident
```

---

## 9. Akzeptanzkriterien

- [ ] Alle Topics die Bounded-Context-Grenzen überschreiten haben eine AsyncAPI-Spezifikation
- [ ] Schema-Kompatibilitäts-Check läuft in CI und blockiert inkompatible Änderungen
- [ ] AsyncAPI-Dokumentation wird automatisch generiert und auf API-Portal veröffentlicht
- [ ] Jede Event-Klasse hat `@AsyncAPISchema`-Annotation die auf die Spec verweist
- [ ] Event-Versionierungsregeln sind im Team bekannt und dokumentiert

---

## Quellen & Referenzen

- **AsyncAPI Initiative, "AsyncAPI Specification 3.0" (2024)** — offizielle Spezifikation; asyncapi.com.
- **Confluent, "Schema Registry Documentation"** — Avro-Schema-Evolution und Kompatibilitätsmodi.
- **Gregor Hohpe & Bobby Woolf, "Enterprise Integration Patterns" (2003), Kap. 3** — Message-Kanal-Dokumentation als Contract.
- **Zhamak Dehghani, "Data Mesh" (2022)** — Events als Produkte mit expliziten Contracts.

---

## Verwandte ADRs

- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka als Event-Transport
- [ADR-066](ADR-066-api-first-openapi.md) — OpenAPI als Vorbild (REST-Äquivalent)
- [ADR-089](ADR-089-isaqb-advanced-context-mapping.md) — Published Language als DDD-Konzept
- [ADR-097](ADR-097-dead-letter-queue.md) — DLQ für fehlerhaft Events
