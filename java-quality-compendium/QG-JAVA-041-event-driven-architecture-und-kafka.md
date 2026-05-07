# QG-JAVA-041 — Event-Driven Architecture und Kafka

## Dokumentstatus

| Aspekt | Details/Erklärung |
| --- | --- |
| Dokumenttyp | Java Quality Guideline / Architektur- und Integrationsstandard |
| ID | QG-JAVA-041 |
| Titel | Event-Driven Architecture und Kafka |
| Status | Accepted / verbindlicher Standard für neue eventbasierte Integrationen |
| Version | 2.0 |
| Datum | 2026-05-01|
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.4+, Spring Kafka 3.3+, Micrometer 1.13+, OpenTelemetry, Jakarta Validation 3.0 |
| Messaging-Baseline | Apache Kafka 3.7+ (KRaft-Modus), Confluent Schema Registry 7.6+, Avro 1.11+ |
| Testing-Baseline | Testcontainers 1.20+ (`apache/kafka:3.7.0` als Default-Image), Pact JVM 4.6+ für Messaging Contracts |
| Kategorie | Architektur · Messaging · Integration · Resilienz · DevSecOps |
| Zielgruppe | Java-Entwickler, Tech Leads, Architektur, QA, Security, DevOps, Plattform-Team |
| Geltungsbereich | Event-Produktion, Event-Konsum, Kafka-Topics, Event-Schemas, Consumer-Groups, Retry-Strategien, Dead Letter Topics, Idempotenz, Outbox, Saga, Observability, Tenant-Isolation und Security |
| Verbindlichkeit | Kafka DARF nur für fachlich oder technisch sinnvolle asynchrone Integration eingesetzt werden. Events MÜSSEN versioniert, beobachtbar, idempotent verarbeitbar und sicher betrieben werden. Abweichungen sind im Pull Request nachvollziehbar zu begründen. |
| Technischer Prüfstatus | Code-Beispiele sind gegen Spring Kafka 3.3, Apache Kafka 3.7, Confluent Schema Registry 7.6 und Pact JVM 4.6 referenzbasiert validiert; alle DB-Beispiele setzen PostgreSQL 16+ mit `TIMESTAMPTZ` voraus |
| Schwester-Guidelines | QG-JAVA-006 (Spring-Boot-Serviceschicht), QG-JAVA-008 (Objektorientierung), QG-JAVA-019 (Contract Testing — Sektion 14 Messaging Contracts), QG-JAVA-039-01 (Statische Feature Flags), QG-JAVA-126 (Docker) |
| Wesentliche Änderungen ggü. v1.0 | Spring Kafka 3.3+ statt 3.1.9 · `@RetryableTopic` als modernes Retry-Pattern · Transactional Producer / Exactly-Once-Semantics · KRaft statt Zookeeper · Native Tracing mit `observation-enabled` (kein manuelles Header-Setting mehr) · `processed_events`-Tabelle mit Tenant-Isolation, `TIMESTAMPTZ`, Partitionierung und Retention · Acknowledge nach Transaction-Commit (klassischer Bug-Fix) · Outbox-Dispatcher mit `SKIP LOCKED` und exponentiellem Backoff · Debezium CDC als bevorzugte Outbox-Lösung · Saga-Pattern für verteilte Transaktionen · Tenant-Context-Pattern beim Consumer · JSON Schema und Protobuf als Alternativen · Schema Registry Authentication · Subject-Namensstrategie (TopicNameStrategy vs. RecordNameStrategy) · Cross-References zu QG-JAVA-019 v2 (Messaging Contracts) und QG-JAVA-006 v2 (Tenant-Context) · strukturell an QG-JAVA-006 v2 angeglichen (TOC, Lese-Anleitung, MUSS/DARF/SOLLTE getrennt, Begriffsglossar als eigene Sektion, RFC-2119-Großschreibung) |
| Nicht-Ziel | Diese Richtlinie ist kein Ersatz für gutes API-Design, klare Servicegrenzen oder vollständige Domain-Driven-Design-Schulung. Sie behandelt keine Kafka-Cluster-Operations, keine Performance-Tuning-Details und kein Streaming-Analytics-Setup. |

---

## Inhaltsverzeichnis

1. [Zweck dieser Richtlinie](#1-zweck-dieser-richtlinie)
2. [Kurzregel für Entwickler](#2-kurzregel-für-entwickler)
3. [Verbindlicher Standard](#3-verbindlicher-standard)
4. [Geltungsbereich](#4-geltungsbereich)
5. [Begriffe](#5-begriffe)
6. [Technischer Hintergrund](#6-technischer-hintergrund)
7. [Wann Event-Driven Architecture sinnvoll ist](#7-wann-event-driven-architecture-sinnvoll-ist)
8. [Ereignistypen: Domain Event, Command, Integration Event](#8-ereignistypen-domain-event-command-integration-event)
9. [Topic-Naming und Schema-Subject-Strategie](#9-topic-naming-und-schema-subject-strategie)
10. [Event Envelope und Payload-Design](#10-event-envelope-und-payload-design)
11. [Producer-Konfiguration](#11-producer-konfiguration)
12. [Transactional Producer und Exactly-Once-Semantics](#12-transactional-producer-und-exactly-once-semantics)
13. [Partition Key und Reihenfolge](#13-partition-key-und-reihenfolge)
14. [Outbox-Pattern](#14-outbox-pattern)
15. [Debezium CDC als Outbox-Implementation](#15-debezium-cdc-als-outbox-implementation)
16. [Consumer-Konfiguration](#16-consumer-konfiguration)
17. [Consumer-Verarbeitung mit korrekter Acknowledge-Semantik](#17-consumer-verarbeitung-mit-korrekter-acknowledge-semantik)
18. [Idempotenz und `processed_events`-Tabelle](#18-idempotenz-und-processed_events-tabelle)
19. [Fehlerbehandlung mit `@RetryableTopic`](#19-fehlerbehandlung-mit-retryabletopic)
20. [Dead Letter Topics und Recovery](#20-dead-letter-topics-und-recovery)
21. [Schema Registry, Avro, JSON Schema und Protobuf](#21-schema-registry-avro-json-schema-und-protobuf)
22. [Event-Versionierung und Kompatibilität](#22-event-versionierung-und-kompatibilität)
23. [Saga-Pattern für verteilte Transaktionen](#23-saga-pattern-für-verteilte-transaktionen)
24. [Eventual Consistency](#24-eventual-consistency)
25. [Observability und natives Tracing](#25-observability-und-natives-tracing)
26. [Security- und SaaS-Aspekte](#26-security--und-saas-aspekte)
27. [Tenant-Context im Consumer](#27-tenant-context-im-consumer)
28. [Datenschutz und Retention](#28-datenschutz-und-retention)
29. [Testing](#29-testing)
30. [Anti-Patterns](#30-anti-patterns)
31. [CI/CD-Standard](#31-cicd-standard)
32. [Werkzeuglandschaft: Spring Kafka, Kafka Streams, Spring Cloud Stream](#32-werkzeuglandschaft-spring-kafka-kafka-streams-spring-cloud-stream)
33. [Migration bestehender Kafka-Integrationen](#33-migration-bestehender-kafka-integrationen)
34. [Review-Checkliste](#34-review-checkliste)
35. [Automatisierbare Prüfungen](#35-automatisierbare-prüfungen)
36. [Ausnahmen](#36-ausnahmen)
37. [Definition of Done](#37-definition-of-done)
38. [Quellen und weiterführende Literatur](#38-quellen-und-weiterführende-literatur)

### Wie liest du dieses Dokument

Dieses Dokument ist mit ca. 130 KB ein Nachschlagewerk, kein Lesebuch. Vier Lesepfade werden empfohlen:

**Wenn du neu im Team bist:** Starte mit Sektion 2 (Kurzregel), lies dann Sektion 1 (Zweck), Sektion 7 (Wann sinnvoll) und Sektion 8 (Ereignistypen). Damit hast du die mentale Karte für die Frage, ob Kafka überhaupt das richtige Werkzeug ist. Danach Sektion 14 (Outbox) und Sektion 18 (Idempotenz) — die zwei härtesten Production-Lehren.

**Wenn du im Code-Review bist:** Springe direkt zu Sektion 34 (Review-Checkliste). Jede Zeile hat einen Anker zur Detail-Sektion mit der Begründung. Bei Verstoß gegen eine MUSS-Regel: Sektion 3.1.

**Wenn du etwas Spezifisches suchst:** Inhaltsverzeichnis oben. Häufigste Punkte: Anti-Patterns → Sektion 30. `@RetryableTopic` → Sektion 19. Outbox → Sektion 14. Saga → Sektion 23. Tracing → Sektion 25. Tenant → Sektion 27.

**Wenn du eine Architektur-Entscheidung treffen musst:** Sektion 7 (ist EDA sinnvoll?), dann Sektion 32 (welches Werkzeug?), dann Sektion 14/15 (Outbox- vs. CDC-Strategie). Wer schon dabei ist, sollte auch Sektion 23 lesen — Saga ist häufig der eigentliche Architektur-Treiber.

---

## 1. Zweck dieser Richtlinie

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
- Datenschutzrisiken wandern in Topics, Logs, DLTs, Backups und Traces.

Der Zweck dieser Guideline ist deshalb nicht „Kafka überall". Der Zweck ist, Kafka **dort robust einzusetzen, wo asynchrone Ereignisse fachlich sinnvoll sind** — und gleichzeitig die operativen, sicherheits- und compliance-relevanten Folgen sauber zu adressieren.

---

## 2. Kurzregel für Entwickler

Kafka-Events DÜRFEN nur fachlich stabile Ereignisse oder technisch klar definierte Integrationssignale transportieren. Jedes Event braucht einen Owner, ein Schema, eine Versionierungsstrategie, einen Partition Key, eine Idempotenzstrategie, eine Fehlerstrategie, Observability und Sicherheitsprüfung.

Producer DÜRFEN kritische fachliche Zustandsänderungen nicht direkt in derselben Transaktion „nebenbei" nach Kafka senden, wenn Datenbank und Kafka konsistent bleiben müssen. Dafür ist ein Outbox-Pattern (Sektion 14) oder Debezium CDC (Sektion 15) erforderlich.

Consumer MÜSSEN idempotent verarbeiten. Acknowledge MUSS **nach** dem Transaction-Commit erfolgen, nicht innerhalb derselben Methode wie die fachliche Verarbeitung. Retry MUSS über `@RetryableTopic` (Sektion 19) oder `DefaultErrorHandler` mit DLT erfolgen — niemals über Endlosschleifen oder schweigendes Verschlucken.

Tenant-Kontext MUSS im Event-Envelope explizit transportiert werden und beim Consumer sicher in den Thread-Kontext gesetzt werden (Sektion 27). Ohne diese Disziplin gibt es keine sichere Multi-Tenant-Verarbeitung.

---

## 3. Verbindlicher Standard

### 3.1 MUSS-Regeln

Ein eventbasierter Service MUSS folgende Regeln erfüllen:

1. Jedes Topic MUSS einen Owner haben (Team oder Person).
2. Jedes Event MUSS ein Schema haben — Avro, JSON Schema oder Protobuf.
3. Schemas MÜSSEN in einer Schema Registry zentral abgelegt werden.
4. Topic-Namen MÜSSEN dem Schema `<domain>.<event-name>.v<major-version>` folgen.
5. Producer MÜSSEN `acks=all` und `enable.idempotence=true` setzen.
6. Producer MÜSSEN Send-Ergebnisse beobachten — `whenComplete` oder `CompletableFuture`-Handling.
7. Partition Keys MÜSSEN fachlich gewählt sein (Aggregat-ID), nicht zufällig.
8. Bei kritischen DB-und-Kafka-Flüssen MUSS Outbox (Sektion 14) oder Debezium CDC (Sektion 15) verwendet werden.
9. Consumer MÜSSEN idempotent sein — über `processed_events`-Tabelle (Sektion 18) oder gleichwertigen Mechanismus.
10. Consumer-`Acknowledge` MUSS nach Transaction-Commit erfolgen, nicht innerhalb der `@Transactional`-Methode (Sektion 17).
11. `enable-auto-commit` MUSS auf `false` gesetzt sein.
12. Retry-Strategie MUSS definiert sein — `@RetryableTopic` (Sektion 19) oder `DefaultErrorHandler` mit DLT.
13. Dead Letter Topics MÜSSEN Owner, Monitoring und Runbook haben.
14. Schema-Kompatibilität MUSS in CI geprüft werden.
15. Breaking Changes MÜSSEN über neue Major-Version-Topics erfolgen (`v1` → `v2`).
16. Tenant-Kontext MUSS bei SaaS-Events im Event-Envelope enthalten sein.
17. Tenant-Context MUSS beim Consumer in den Thread-Kontext gesetzt werden (Sektion 27, mit Cross-Reference auf QG-JAVA-006 v2 Sektion 14.6).
18. Tracing MUSS über Micrometer Observation aktiviert sein (`observation-enabled: true`).
19. Personenbezogene Daten DÜRFEN NICHT in Event-Payloads geloggt werden.
20. Schema Registry MUSS authentifizierten Zugriff haben (Basic Auth, OAuth oder mTLS).
21. Kafka-Cluster MUSS authentifiziert und autorisiert sein (SASL/SSL, ACLs).
22. `processed_events`-Tabelle MUSS Tenant-Isolation haben, `TIMESTAMPTZ` verwenden und eine Retention-Strategie besitzen (Partitionierung oder Cleanup-Job).
23. Outbox-Dispatcher MUSS `SELECT FOR UPDATE SKIP LOCKED` oder gleichwertigen Mechanismus verwenden.
24. Outbox-Events MÜSSEN exponentielles Backoff bei Fehlern haben.
25. Bei verteilten Transaktionen MUSS Saga-Pattern (Sektion 23) angewendet werden — keine Two-Phase-Commits über Kafka.

### 3.2 DARF-NICHT-Regeln

Ein eventbasierter Service DARF NICHT:

1. Kafka als Ersatz für klare Servicegrenzen oder gutes API-Design verwenden.
2. JPA-Entities oder API-Response-DTOs als Event-Modelle wiederverwenden.
3. Vollständige Event-Payloads auf INFO-Level loggen.
4. Sensitive Daten (Passwörter, Tokens, IBANs, vollständige E-Mail-Adressen) in Events oder Headern transportieren.
5. `kafkaTemplate.send(...)` ohne Error-Handling als Standard verwenden.
6. Random-UUIDs als Partition Keys verwenden, wenn Reihenfolge wichtig ist.
7. `@KafkaListener` mit `@Transactional` und `ack.acknowledge()` in derselben Methode kombinieren.
8. Auto-Commit aktivieren, wenn fachliche Verarbeitung mit DB-Transaktionen verbunden ist.
9. Endlose Retry-Schleifen ohne Backoff und DLT betreiben.
10. Schemas im gleichen Topic ohne Major-Version inkompatibel ändern (Feld umbenennen, Pflichtfeld ohne Default hinzufügen, Semantik ändern).
11. Tenant-IDs aus ungeprüften Request-Feldern in Event-Envelopes übernehmen.
12. Manuelles Setzen von Trace-Headern (Anti-Pattern seit Spring Boot 3.0 — siehe Sektion 25).
13. Kafka als synchroner Request/Reply-Mechanismus verwenden, wenn der Aufrufer sofortiges Ergebnis braucht.
14. DLT ohne Owner, Monitoring oder Runbook betreiben.
15. Zookeeper-basierte Kafka-Cluster für Neuinstallationen (Kafka 4.0 entfernt Zookeeper komplett).
16. `confluentinc/cp-kafka` mit `latest`-Tag in Tests verwenden.
17. Schema-Subject-Namensstrategien wechseln, ohne Kompatibilität zu prüfen.

### 3.3 SOLLTE-Regeln

Ein eventbasierter Service SOLLTE:

1. `@RetryableTopic` für nicht-blockierendes Retry verwenden statt `DefaultErrorHandler` mit blockierendem `BackOff`.
2. Transactional Producer (`transaction-id-prefix`) für Read-Process-Write-Patterns innerhalb Kafka.
3. Debezium CDC als Outbox-Implementation für hohe Last und niedrige Latenz erwägen.
4. JSON Schema bei Mixed-Stack-Setups (Java + Node.js + Python) als Schema-Format wählen.
5. Protobuf bei hohem Durchsatz und Cross-Language-Setups erwägen.
6. Avro als Default für Spring-Boot-zentrierte Setups.
7. Apache Kafka 3.7+ in KRaft-Modus für Neuinstallationen.
8. Slice-Tests (`@Import`, gezielte `@SpringBootTest(classes=...)`) statt voll geladener Spring-Kontexte für Kafka-Integrationstests.
9. Messaging Contracts mit Pact JVM (siehe QG-JAVA-019 v2 Sektion 14) für Cross-Service-Verträge.
10. Saga-Patterns choreographiert (Events) statt orchestriert (Saga-Service) — Choreographie skaliert besser.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für:

1. Kafka Producer in Spring-Boot-Services.
2. Kafka Consumer (`@KafkaListener`).
3. Spring Kafka Listener-Container und Error-Handler.
4. Event-Schemas mit Avro, JSON Schema oder Protobuf.
5. Schema Registry (Confluent Schema Registry, Apicurio Registry).
6. Consumer Groups und Partition-Strategien.
7. Dead Letter Topics, Retry Topics, `@RetryableTopic`.
8. Transactional Outbox und Debezium CDC.
9. Saga-Pattern (choreographiert und orchestriert).
10. Idempotenz in Consumer-Verarbeitung.
11. Event-Versionierung und Schema-Migration.
12. Observability mit Micrometer und OpenTelemetry.
13. Security-Aspekte: Kafka-ACLs, Schema-Registry-Auth, Datenminimierung.
14. SaaS-/Tenant-bezogene Events und Tenant-Context-Propagation.
15. Integrationstests mit Kafka/Testcontainers.
16. CI/CD-Verifikation von Event-Schemas.

Diese Richtlinie gilt nicht als vollständiger Standard für:

1. Reine In-Memory-Domain-Events innerhalb eines Monolithen, sofern diese nicht über Kafka publiziert werden. Sobald ein Event serviceübergreifend verteilt wird, gilt diese Richtlinie.
2. Kafka-Cluster-Operations (Broker-Tuning, Replication-Strategien, Storage-Sizing).
3. Streaming-Analytics-Setups mit Kafka Streams oder ksqlDB.
4. Zwischenspeicher-Patterns (Kafka als Cache).
5. Migrationen von RabbitMQ, ActiveMQ oder anderen Brokern zu Kafka.
6. Synchrone APIs (siehe QG-JAVA-006 v2 für Service-Layer und QG-JAVA-019 v2 für Contract Testing).

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Event | Maschinenlesbare Beschreibung einer fachlichen Tatsache in der Vergangenheit | `OrderPlaced`, `PaymentCaptured` |
| Domain Event | Fachliches Ereignis, das innerhalb einer Domain stattgefunden hat | `OrderPlaced` |
| Integration Event | Externe, stabile Repräsentation eines fachlichen Ereignisses | öffentliches `OrderPlaced.v1`-Schema |
| Command | Aufforderung an ein anderes System, etwas zu tun | `ReserveInventory` |
| Topic | Logischer Stream in Kafka, in dem Events gespeichert werden | `orders.placed.v1` |
| Partition | Teil eines Topics; Reihenfolge ist nur pro Partition garantiert | `orders.placed.v1-0` bis `-9` |
| Partition Key | Wert, der bestimmt, in welche Partition ein Record geschrieben wird | `orderId` |
| Consumer Group | Gruppe von Consumer-Instanzen, die ein Topic kollektiv verarbeiten | `inventory-service` |
| Offset | Position eines Records innerhalb einer Partition | `0`, `1`, `2`, ... |
| Acknowledge / Commit | Bestätigung, dass ein Record verarbeitet wurde | `ack.acknowledge()` |
| Schema Registry | Zentrale Ablage für Event-Schemas mit Versionierung und Kompatibilitätsprüfung | Confluent Schema Registry |
| Avro | Binäres Schema-Format mit Schema Registry Integration | `OrderPlacedEvent.avsc` |
| Subject | Schema-Identifier in der Schema Registry | `orders.placed.v1-value` |
| Dead Letter Topic (DLT) | Topic für Events, die nach Retry-Limit nicht verarbeitet werden konnten | `orders.placed.v1.DLT` |
| Outbox | Lokale DB-Tabelle, die Events vor Publish persistiert | `outbox_events` |
| Debezium / CDC | Change Data Capture aus dem PostgreSQL Write-Ahead-Log | Debezium PostgreSQL Connector |
| Saga | Sequenz von Schritten, die fachliche Konsistenz über Service-Grenzen hinweg sichern | Order-Saga |
| Choreographie | Saga ohne zentralen Orchestrator — Services reagieren auf Events | `OrderPlaced` → `PaymentCaptured` → `InventoryReserved` |
| Orchestrierung | Saga mit zentralem Saga-Service, der Schritte koordiniert | `OrderSagaService` |
| Idempotenz | Mehrfache Ausführung erzeugt dasselbe Ergebnis | Consumer prüft `event_id` |
| `processed_events`-Tabelle | DB-Tabelle, die verarbeitete `event_id`s speichert | mit `tenant_id` und `event_id` als Primary Key |
| Transactional Producer | Kafka-Producer mit Transaktions-API für Exactly-Once-Semantics | `transaction-id-prefix` |
| `@RetryableTopic` | Spring-Kafka-Annotation für nicht-blockierendes Retry mit eigenen Retry-Topics | seit Spring Kafka 2.7+ |
| KRaft | Kafka Raft Metadata mode — ersetzt Zookeeper seit Kafka 3.5 | Default in Kafka 4.0+ |
| Event Envelope | Technischer Umschlag um den Event-Payload mit Metadaten | `eventId`, `correlationId`, `tenantId` |
| Correlation ID | Identifier, der mehrere Events eines Geschäftsflusses verbindet | aus Request-Trace übernommen |
| Causation ID | Identifier des Events, das das aktuelle Event ausgelöst hat | `eventId` des Vorgänger-Events |
| Tenant Context | Server-validierter Mandanten-Kontext im aktuellen Thread | `TenantContext.currentTenantId()` |

---

## 6. Technischer Hintergrund

Kafka ist ein verteilter Commit-Log-basierter Event-Streaming-Broker. Producer schreiben Records in Topics. Topics sind in Partitionen aufgeteilt. Consumer lesen Records aus Partitionen. Consumer Groups steuern, welche Instanz eines Services welche Partitionen verarbeitet. Kafka speichert Events für eine definierte Retention-Zeit, wodurch Consumer später lesen, erneut lesen oder nach Ausfällen aufholen können.

**Wichtig ist:** Kafka entkoppelt Services nicht vollständig von Verantwortung. Es entfernt direkte synchrone Laufzeitkopplung, erzeugt aber neue Vertrags-, Schema-, Betriebs- und Konsistenzverantwortung.

Ein Kafka-Event ist ein **langlebiger Vertrag**. Sobald es veröffentlicht wird, können unbekannte Consumer darauf reagieren. Deshalb ist ein Event nicht einfach „ein DTO auf einem Topic", sondern ein öffentlicher Integrationsvertrag.

### 6.1 KRaft-Modus statt Zookeeper

Apache Kafka 3.5+ verwendet **KRaft (Kafka Raft Metadata mode)** als Default. Kafka 4.0 (Februar 2025) entfernt Zookeeper komplett. Vorteile von KRaft:

- Kein separates Zookeeper-Cluster mehr.
- Geringere Latenz für Metadaten-Operationen.
- Vereinfachtes Bootstrapping.
- Bessere Skalierung auf Millionen von Partitionen.

Für Neuinstallationen MUSS KRaft verwendet werden. Bestehende Zookeeper-Cluster können über `kafka-storage.sh` migriert werden — das ist Operations-Aufgabe und nicht Teil dieser Guideline.

### 6.2 Spring Kafka 3.3+ als Baseline

Spring Boot 3.4 bringt Spring Kafka 3.3+. Wichtige Features gegenüber 3.1.x:

- **`@RetryableTopic`** ist ausgereifter und für viele Use Cases bevorzugt gegenüber `DefaultErrorHandler`.
- **Native Tracing-Integration** mit Micrometer Observation ist First-Class.
- **`ContainerProperties.AckMode.RECORD_FILTERED`** und neue Async-Listener-Patterns.
- **Verbesserte Transactional-Producer-API**.

Diese Guideline geht von Spring Kafka 3.3+ aus.

### 6.3 Event als Vertrag, nicht als DTO

Ein Event ist ein langlebiger Vertrag zwischen Producer und allen aktuellen oder zukünftigen Consumern. Das hat drei Konsequenzen:

1. **Schema-Versionierung ist Pflicht.** Breaking Changes ohne neue Major-Version sind ein Vertrags-Bruch.
2. **Schema-Registry ist Pflicht.** Ohne Registry können Producer inkompatible Events veröffentlichen, die erst zur Laufzeit beim Consumer brechen.
3. **Datenminimierung ist Pflicht.** Was im Event landet, landet in DLTs, Backups, Replay-Prozessen, Logs und manchmal Analytics-Systemen.

---

## 7. Wann Event-Driven Architecture sinnvoll ist

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
| --- | --- | --- | --- |
| Fachliches Ereignis | Etwas ist geschehen und andere Systeme dürfen reagieren | `OrderPlaced`, `PaymentCaptured` | **Geeignet** |
| Temporal Entkopplung | Producer soll nicht auf Consumer warten | Notification nach Bestellung | **Geeignet** |
| Fan-out | Mehrere Consumer reagieren unabhängig | Inventory, Analytics, Mail | **Geeignet** |
| Eventual Consistency akzeptabel | Sofortige Antwort hängt nicht vom Consumer ab | Analytics-Aktualisierung | **Geeignet** |
| Auditierbarer Ereignisstrom | Historie ist fachlich relevant | Statusänderungen | **Geeignet** |
| Saga über mehrere Services | Verteilte fachliche Transaktion, Eventual Consistency | Order → Payment → Inventory | **Geeignet, mit Sektion 23** |
| Synchrones Ergebnis nötig | Aufrufer braucht sofortige Antwort des Zielsystems | Kreditkartenautorisierung im Checkout | **REST/RPC prüfen** |
| Starke Transaktion über Systeme | Alles muss atomar gleichzeitig committed werden | Buchung + externe Zahlung | **Saga-Pattern (Sektion 23)** |
| Simple CRUD | Keine relevanten Folgereaktionen | Admin-Lookup-Tabelle | **Kafka meist unnötig** |
| Request/Reply über Kafka | Quasi-synchroner RPC über Topics | „warte auf Antworttopic" | **Anti-Pattern, REST verwenden** |

---

## 8. Ereignistypen: Domain Event, Command, Integration Event

### 8.1 Domain Event

Ein Domain Event beschreibt eine fachliche Tatsache in der Vergangenheit.

```
OrderPlaced
PaymentCaptured
CustomerRegistered
SubscriptionCancelled
```

**Regel:** Der Name steht in der Vergangenheitsform oder beschreibt einen bereits eingetretenen Zustand. Ein Event ist keine Aufforderung.

### 8.2 Command

Ein Command fordert ein anderes System auf, etwas zu tun.

```
ReserveInventory
SendInvoiceEmail
StartShipment
```

Commands über Kafka sind möglich, aber sie erzeugen stärkere Kopplung als Domain Events. Sie müssen besonders sorgfältig hinsichtlich Ownership, Retry, Idempotenz und Fehlerantwort modelliert werden.

**Regel:** Wenn ein Command-Pattern über Kafka modelliert wird, MUSS klar dokumentiert sein, wer es konsumieren darf, wie Fehler zurückgemeldet werden und wer für Idempotenz verantwortlich ist.

### 8.3 Integration Event

Ein Integration Event ist eine externe, stabile Repräsentation eines fachlichen Ereignisses.

Das interne Domain Event kann anders aussehen als das veröffentlichte Integration Event. Das ist oft sinnvoll, weil externe Verträge stabiler sein müssen als interne Modelle.

**Pattern:**

```java
// Internes Domain Event (kann sich ändern)
public class OrderPlaced {
    private final Order order;
    private final List<DomainEvent> sideEffects;
    // ...
}

// Externes Integration Event (stabiler Vertrag)
public record OrderPlacedPayload(
    String orderId,
    String customerId,
    List<OrderItemPayload> items,
    String currency,
    long totalCents,
    Instant placedAt
) {}
```

Die Übersetzung passiert in einem **Event-Mapper** im Application Service oder Outbox-Prozessor.

---

## 9. Topic-Naming und Schema-Subject-Strategie

### 9.1 Topic-Naming

Topic-Namen MÜSSEN stabil, sprechend und versionierbar sein.

**Empfohlenes Schema:**

```
<domain>.<event-name>.v<major-version>
```

**Beispiele:**

```
orders.placed.v1
orders.cancelled.v1
payments.captured.v1
customers.registered.v1
inventory.reservation-failed.v1
```

**Nicht empfohlen:**

```
topic1
events
order
new-order
test
prod-orders
```

**Regeln:**

1. Keine Umgebung im Topic-Namen, wenn Cluster/Namespace die Umgebung bereits trennt.
2. Major-Version nur bei inkompatiblen Änderungen erhöhen.
3. Topic beschreibt Fachlichkeit, nicht Producer-Klassenname.
4. DLT-/Retry-Topics folgen einer festen Konvention (siehe Sektion 19.2).
5. Temporäre Topics MÜSSEN ein Ablaufdatum haben.

### 9.2 Schema-Subject-Namensstrategie

Confluent Schema Registry bietet drei Subject-Namen-Strategien — die Wahl ist eine wichtige Architekturentscheidung:

| Strategie | Subject | Anwendung |
|---|---|---|
| **TopicNameStrategy** (Default) | `<topic-name>-value` | Ein Schema pro Topic. Standard für die meisten Use Cases. |
| **RecordNameStrategy** | `<record-namespace>.<record-name>` | Ein Schema pro Event-Typ. Multiple Event-Typen pro Topic möglich (Event-Sourcing-artig). |
| **TopicRecordNameStrategy** | `<topic>-<record-namespace>.<record-name>` | Kombination — multiple Event-Typen pro Topic, aber pro Topic separat versioniert. |

**Konfiguration:**

```yaml
spring:
  kafka:
    producer:
      properties:
        # Default — ein Schema pro Topic
        value.subject.name.strategy: io.confluent.kafka.serializers.subject.TopicNameStrategy

        # Alternative — multiple Event-Typen pro Topic
        # value.subject.name.strategy: io.confluent.kafka.serializers.subject.RecordNameStrategy
```

**Regel:** Diese Guideline empfiehlt **TopicNameStrategy** als Default. Wenn ein Team „alle Order-Events in einem Topic" wollte (Event Sourcing), MUSS das im Architecture Board explizit beschlossen und dokumentiert sein, weil RecordNameStrategy weitreichende Konsequenzen für Consumer-Filterung, Retention und Replay hat.

---

## 10. Event Envelope und Payload-Design

### 10.1 Event Envelope

Ein Event SOLLTE einen stabilen technischen Umschlag besitzen. Der Umschlag erleichtert Korrelation, Idempotenz, Versionierung und Betrieb.

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
) {
    public EventEnvelope {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
    }
}
```

**Regeln für Envelope-Felder:**

| Feld | Bedeutung | Pflicht |
| --- | --- | --- |
| `eventId` | Global eindeutig. Wird für Idempotenz genutzt. | MUSS |
| `eventType` | Beschreibt den fachlichen Typ. | MUSS |
| `eventVersion` | Beschreibt die Schemakompatibilität. | MUSS |
| `occurredAt` | Zeitpunkt des fachlichen Ereignisses. | MUSS |
| `producer` | Identifiziert den veröffentlichenden Service. | MUSS |
| `correlationId` | Verbindet Ereignisse eines Geschäftsflusses. | SOLLTE |
| `causationId` | Verweist auf das auslösende Event. | KANN |
| `tenantId` | Bei SaaS-Systemen Pflicht, sofern Eventdaten tenantbezogen sind. | MUSS bei SaaS |
| `payload` | Enthält die fachlichen Daten. | MUSS |

**Verboten in Envelope-Feldern:**

- E-Mail-Adressen
- Vollständige Namen
- Tokens, API-Keys
- IBAN, Kreditkartennummern
- Geheime IDs aus externen Systemen

### 10.2 Payload: fachlich stabil und minimal

```java
public record OrderPlacedPayload(
        String orderId,
        String customerId,
        List<OrderItemPayload> items,
        String currency,
        long totalCents
) {}

public record OrderItemPayload(
        String productId,
        int quantity,
        long unitPriceCents
) {}
```

**Regeln:**

1. Payload enthält nur Daten, die Consumer wirklich brauchen.
2. Keine internen Entity-Strukturen veröffentlichen.
3. Keine JPA-Entities als Event-Modelle verwenden.
4. Keine API-Response-DTOs als Event-Modelle wiederverwenden.
5. Geldbeträge nicht als `double` übertragen — `long` (Cents) oder `BigDecimal` mit Skalenangabe.
6. Zeitpunkte als ISO-8601-String oder logischer Typ im Schema modellieren.
7. IDs eindeutig und stabil halten.
8. Personenbezogene Daten minimieren oder pseudonymisieren.

### 10.3 Schlechtes Payload-Design

```java
// ❌ Anti-Pattern
public record OrderPlacedEvent(
    OrderEntity order,                 // JPA-Entity!
    Customer customer,                 // mit E-Mail, Telefonnummer
    String passwordHash,               // sollte gar nicht existieren
    double totalEur                    // Floating-Point für Geld!
) {}
```

**Probleme:** JPA-Entity bringt Lazy-Loading-Risiken, vollständige Kunden-Daten verstoßen gegen Datenminimierung, `passwordHash` darf nirgends auftauchen, `double` für Geld führt zu Rundungsfehlern.

---

## 11. Producer-Konfiguration

### 11.1 Wichtige Producer-Einstellungen

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      properties:
        schema.registry.url: ${SCHEMA_REGISTRY_URL}
        basic.auth.credentials.source: USER_INFO
        basic.auth.user.info: ${SCHEMA_REGISTRY_USER}:${SCHEMA_REGISTRY_PASSWORD}
        # Reliability
        acks: all
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        # Timeouts
        delivery.timeout.ms: 120000
        request.timeout.ms: 30000
        # Performance
        linger.ms: 5
        compression.type: zstd
      # Tracing — Pflicht
      observation-enabled: true
```

**Regeln:**

- `acks=all` für zuverlässige Bestätigung.
- `enable.idempotence=true` aktiviert Idempotent Producer (verhindert Duplikate bei Retry).
- Producer-Fehler beobachten und nicht ignorieren.
- Timeouts bewusst setzen.
- Compression bewusst wählen (`zstd` ist guter Default — gutes Verhältnis von CPU zu Compression-Ratio).
- Keine Secrets im YAML, sondern über Secret Management oder Environment.
- `observation-enabled: true` aktiviert Micrometer-Tracing (siehe Sektion 25).

### 11.2 Schlecht: blindes `fire and forget`

```java
// ❌ Anti-Pattern: ignoriert Send-Ergebnis
kafkaTemplate.send("orders.placed.v1", event);
```

**Problem:** Der Code ignoriert, ob der Send-Vorgang erfolgreich war. Für nicht-kritische Telemetrie kann das akzeptabel sein. Für fachliche Integration ist es zu schwach.

### 11.3 Gut: Send-Ergebnis beobachten

```java
@Service
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, EventEnvelope<OrderPlacedPayload>> kafkaTemplate;
    private final EventPublishMetrics metrics;

    public OrderEventPublisher(
            KafkaTemplate<String, EventEnvelope<OrderPlacedPayload>> kafkaTemplate,
            EventPublishMetrics metrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
    }

    public CompletableFuture<SendResult<String, EventEnvelope<OrderPlacedPayload>>>
            publishOrderPlaced(EventEnvelope<OrderPlacedPayload> event) {

        var future = kafkaTemplate.send(
                "orders.placed.v1",
                event.payload().orderId(),
                event
        );

        return future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event: eventId={}, topic={}",
                        event.eventId(), "orders.placed.v1", ex);
                metrics.incrementPublishFailure("orders.placed.v1");
                return;
            }

            log.info("Published event: eventId={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            metrics.incrementPublishSuccess("orders.placed.v1");
        });
    }
}
```

**Wichtig:** Wenn die Veröffentlichung fachlich kritisch ist (DB+Kafka müssen konsistent bleiben), reicht auch das nicht. Dann ist Outbox (Sektion 14) oder Debezium CDC (Sektion 15) zu verwenden.

---

## 12. Transactional Producer und Exactly-Once-Semantics

Für Read-Process-Write-Patterns innerhalb Kafka — ein Service liest ein Event, transformiert, schreibt ein neues Event — bietet Kafka **Transactional Producer** mit Exactly-Once-Semantics.

### 12.1 Konfiguration

```yaml
spring:
  kafka:
    producer:
      transaction-id-prefix: "fulfillment-service-tx-"      # ✅ aktiviert Transactions
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
    consumer:
      properties:
        isolation.level: read_committed                     # ✅ liest nur committed Records
```

### 12.2 Read-Process-Write Pattern

```java
@Configuration
public class KafkaTransactionConfiguration {

    @Bean
    public KafkaTransactionManager<String, Object> kafkaTransactionManager(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            transactionalKafkaListenerContainerFactory(
                    ConsumerFactory<String, Object> consumerFactory,
                    KafkaTransactionManager<String, Object> txManager) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setKafkaAwareTransactionManager(txManager);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }
}
```

```java
@Service
public class FulfillmentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FulfillmentRepository fulfillmentRepository;

    @KafkaListener(
        topics = "orders.placed.v1",
        groupId = "fulfillment-service",
        containerFactory = "transactionalKafkaListenerContainerFactory"
    )
    @Transactional("kafkaTransactionManager")
    public void onOrderPlaced(EventEnvelope<OrderPlacedPayload> envelope) {

        // 1. Verarbeitung
        var fulfillment = fulfillmentRepository.create(envelope.payload());

        // 2. Folge-Event publizieren — innerhalb derselben Kafka-Transaction
        var fulfillmentEvent = EventEnvelope.from(fulfillment);
        kafkaTemplate.send(
            "fulfillment.created.v1",
            fulfillment.id(),
            fulfillmentEvent
        );

        // Beide Operationen sind atomar:
        // - Consumer-Offset wird committed
        // - Folge-Event wird publiziert
        // Wenn entweder fehlschlägt, wird beides rolled back.
    }
}
```

### 12.3 Was Transactional Producer nicht löst

**Wichtig:** Transactional Producer garantiert nur Atomarität **innerhalb** Kafka. **Externe Systeme (PostgreSQL, MongoDB) sind nicht in der Kafka-Transaction.** Wenn der Service in eine externe DB schreibt UND ein Kafka-Event publiziert, ist Outbox-Pattern (Sektion 14) immer noch erforderlich.

| Scenario | Lösung |
|---|---|
| Read Kafka → Process → Write Kafka | Transactional Producer |
| Read Kafka → Process → Write DB | Idempotente Verarbeitung mit `processed_events` (Sektion 18) |
| Process → Write DB → Write Kafka | Outbox-Pattern (Sektion 14) oder Debezium CDC (Sektion 15) |
| Saga über mehrere Services | Saga-Pattern (Sektion 23) |

---

## 13. Partition Key und Reihenfolge

Der Key bestimmt, in welche Partition ein Record geschrieben wird. Reihenfolge ist in Kafka **nur innerhalb einer Partition** garantiert.

### 13.1 Regel

```
Partition Key = fachliche Aggregat-ID
```

### 13.2 Beispiele

| Event | Empfohlener Key | Warum |
| --- | --- | --- |
| `OrderPlaced` | `orderId` | Reihenfolge pro Bestellung |
| `OrderCancelled` | `orderId` | Statusereignisse derselben Bestellung geordnet |
| `PaymentCaptured` | `paymentId` oder `orderId` | je nach fachlichem Ordering |
| `CustomerUpdated` | `customerId` | Reihenfolge pro Kunde |
| `TenantConfigurationChanged` | `tenantId` | Reihenfolge pro Tenant |

### 13.3 Anti-Pattern

```java
// ❌ Zerstört Reihenfolge
kafkaTemplate.send(topic, UUID.randomUUID().toString(), event);

// ❌ Nicht-deterministischer Key
kafkaTemplate.send(topic, Instant.now().toString(), event);
```

### 13.4 Consumer-Parallelität

- Mehr Consumer als Partitionen bringen für ein Topic keine zusätzliche Parallelität.
- Mehr Partitionen erhöhen Betriebs- und Rebalancing-Komplexität.
- Reihenfolge über mehrere Aggregate hinweg ist nicht garantiert.
- Globale Reihenfolge ist bei Kafka in der Regel kein realistisches Designziel.

---

## 14. Outbox-Pattern

### 14.1 Problem

Wenn ein Service in einer Datenbank speichert und anschließend ein Kafka-Event sendet, entstehen zwei unterschiedliche Transaktionssysteme.

```java
// ❌ Anti-Pattern: zwei separate Transaktionen
@Transactional
public void placeOrder(CreateOrderCommand command) {
    var order = orderRepository.save(Order.from(command));

    kafkaTemplate.send("orders.placed.v1",
                      order.id().value().toString(),
                      OrderPlacedEvent.from(order));
}
```

**Fehlerfälle:**

- Datenbank-Commit erfolgreich, Kafka-Send schlägt fehl → Order ist in DB, Event fehlt.
- Kafka-Send erfolgreich, Datenbank-Transaktion rollt zurück → Event ist publiziert, Order existiert nicht.
- Anwendung crasht zwischen DB und Kafka.
- Event wird doppelt gesendet.
- Consumer sieht Ereignis, zu dem die Datenbank noch nicht konsistent ist.

### 14.2 Gute Anwendung: Transactional Outbox

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void placeOrder(CreateOrderCommand command) {
        var order = orderRepository.save(Order.from(command));

        var event = OutboxEvent.create(
                UUID.randomUUID(),
                "orders.placed.v1",
                order.id().value().toString(),
                serialize(OrderPlacedPayload.from(order)),
                Instant.now()
        );

        outboxRepository.save(event);
    }

    private byte[] serialize(Object payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(e);
        }
    }
}
```

### 14.3 Outbox-Tabelle

```sql
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    payload BYTEA NOT NULL,
    headers JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',         -- PENDING, PUBLISHED, FAILED
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ
);

-- Index für Outbox-Dispatcher
CREATE INDEX idx_outbox_pending ON outbox_events (status, next_retry_at)
    WHERE status = 'PENDING';

-- Index für Cleanup
CREATE INDEX idx_outbox_published_at ON outbox_events (published_at)
    WHERE status = 'PUBLISHED';
```

### 14.4 Outbox-Dispatcher mit `SKIP LOCKED` und Backoff

```java
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay:PT1S}")
    @Transactional
    public void publishPendingEvents() {
        var events = outboxRepository.findNextBatchSkipLocked(100);

        for (var event : events) {
            publishWithBackoff(event);
        }
    }

    private void publishWithBackoff(OutboxEvent event) {
        try {
            var record = new ProducerRecord<>(
                event.topic(),
                event.aggregateId(),
                event.payload()
            );
            // Headers übertragen, falls vorhanden
            event.headers().forEach((k, v) ->
                record.headers().add(k, v.getBytes(StandardCharsets.UTF_8)));

            kafkaTemplate.send(record).get(30, TimeUnit.SECONDS);
            outboxRepository.markPublished(event.id());
        } catch (Exception ex) {
            outboxRepository.markFailed(event.id(), ex.getMessage(), nextRetryAt(event));
            log.warn("Failed to publish outbox event: id={}, retryCount={}",
                event.id(), event.retryCount() + 1, ex);
        }
    }

    private Instant nextRetryAt(OutboxEvent event) {
        // Exponentielles Backoff: 1min, 2min, 4min, 8min, 16min, 32min, 64min (max 1h)
        var backoffSeconds = Math.min(60L * (1L << Math.min(event.retryCount(), 6)), 3600);
        return Instant.now().plusSeconds(backoffSeconds);
    }
}
```

```java
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    // ✅ SKIP LOCKED für Multi-Pod-Deployments
    @Query(value = """
        SELECT * FROM outbox_events
        WHERE status = 'PENDING'
          AND (next_retry_at IS NULL OR next_retry_at <= NOW())
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> findNextBatchSkipLocked(@Param("batchSize") int batchSize);
}
```

**Warum `SKIP LOCKED`:** Bei mehreren Service-Pods greifen alle Scheduler gleichzeitig auf die Tabelle zu. Ohne `SKIP LOCKED` blockieren sich die Pods gegenseitig oder verarbeiten dieselben Events doppelt. Mit `SKIP LOCKED` nimmt sich jeder Pod seine Events, ohne die anderen zu blockieren.

### 14.5 Outbox-Cleanup

```sql
-- ✅ Cleanup-Job (täglich): behält 7 Tage
DELETE FROM outbox_events
WHERE status = 'PUBLISHED'
  AND published_at < NOW() - INTERVAL '7 days';
```

Bei sehr hohem Durchsatz: PostgreSQL-Partitionierung nach `created_at` (analog zu `processed_events` in Sektion 18.4).

---

## 15. Debezium CDC als Outbox-Implementation

Debezium ist ein **Change Data Capture (CDC)** Tool, das direkt aus dem PostgreSQL Write-Ahead-Log (WAL) liest. Es ist die **State-of-the-Art-Lösung für Outbox** in produktionsreifen Setups.

### 15.1 Vorteile gegenüber Polling-basiertem Outbox-Dispatcher

| Aspekt | Polling-Dispatcher | Debezium CDC |
|---|---|---|
| Latenz | Sekunden (Polling-Intervall) | Millisekunden |
| DB-Last | Polling-Queries | Lesen aus WAL (kaum Last) |
| Ordering | Nach `created_at` | Nach WAL-Reihenfolge |
| Race Conditions | Durch `SKIP LOCKED` gelöst | Inhärent vermieden |
| Operations | Anwendungs-Scheduler | Separate Debezium-Instanz (Kafka Connect) |
| Komplexität | Niedriger Setup | Höherer Setup, niedrigere Anwendungs-Komplexität |

### 15.2 Setup mit Debezium Outbox Event Router

Debezium hat einen **eingebauten Outbox Event Router**, der direkt das `EventEnvelope`-Pattern unterstützt.

**Outbox-Tabelle (Debezium-konform):**

```sql
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    tenant_id VARCHAR(36),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

**Debezium-Connector-Konfiguration:**

```json
{
  "name": "outbox-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "${DEBEZIUM_DB_USER}",
    "database.password": "${DEBEZIUM_DB_PASSWORD}",
    "database.dbname": "orders",
    "table.include.list": "public.outbox_events",
    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.route.topic.replacement": "${routedByValue}",
    "transforms.outbox.route.by.field": "aggregate_type",
    "transforms.outbox.table.fields.additional.placement": "tenant_id:header,type:header",
    "transforms.outbox.table.field.event.id": "id",
    "transforms.outbox.table.field.event.key": "aggregate_id",
    "transforms.outbox.table.field.event.payload": "payload"
  }
}
```

**Service-Code:**

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void placeOrder(CreateOrderCommand command) {
        var order = orderRepository.save(Order.from(command));

        // ✅ Debezium liest die Outbox-Tabelle automatisch — kein Dispatcher nötig
        outboxRepository.save(OutboxEvent.builder()
            .id(UUID.randomUUID())
            .aggregateType("orders.placed.v1")           // → wird Topic-Name
            .aggregateId(order.id().value().toString())  // → wird Partition Key
            .type("OrderPlaced")                          // → wird Header
            .tenantId(tenantContext.currentTenantId())
            .payload(serialize(OrderPlacedPayload.from(order)))
            .build());
    }
}
```

### 15.3 Wann Debezium, wann Polling-Dispatcher?

**Debezium CDC, wenn:**

- Hoher Durchsatz (>100 Events/Sekunde).
- Niedrige Latenz erforderlich.
- Operations-Team kann Kafka Connect betreiben.
- Mehrere Services nutzen denselben Outbox-Mechanismus.

**Polling-Dispatcher, wenn:**

- Niedrigerer Durchsatz.
- Kein Kafka Connect verfügbar.
- Einfaches Setup gewünscht.
- Service-spezifische Outbox-Logik (z. B. komplexe Routing-Regeln).

---

## 16. Consumer-Konfiguration

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    consumer:
      group-id: inventory-service
      auto-offset-reset: earliest
      enable-auto-commit: false                    # ✅ Pflicht
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      properties:
        schema.registry.url: ${SCHEMA_REGISTRY_URL}
        basic.auth.credentials.source: USER_INFO
        basic.auth.user.info: ${SCHEMA_REGISTRY_USER}:${SCHEMA_REGISTRY_PASSWORD}
        specific.avro.reader: true
        isolation.level: read_committed            # ✅ bei Transactional Producer
    listener:
      ack-mode: manual
      observation-enabled: true                    # ✅ aktiviert Tracing
```

**Regeln:**

1. `group-id` ist bewusst und servicebezogen zu wählen.
2. `enable-auto-commit=false` für kontrollierte Verarbeitung.
3. `ack-mode: manual` — Acknowledge erfolgt explizit nach erfolgreicher Verarbeitung.
4. Fehlerstrategie zentral konfigurieren (Sektion 19).
5. Consumer MUSS idempotent sein (Sektion 18).
6. Consumer Lag MUSS beobachtet werden.
7. Deserialisierungsfehler MÜSSEN in eine definierte Fehlerstrategie laufen.
8. Keine endlose Retry-Schleife ohne Backoff und DLT.
9. `observation-enabled: true` aktiviert Micrometer-Tracing (siehe Sektion 25).

---

## 17. Consumer-Verarbeitung mit korrekter Acknowledge-Semantik

Das ist die **kritischste Sektion** für korrekte Implementierung. Ein subtiler Fehler in der Acknowledge-Semantik führt zu Event-Verlust oder doppelter Verarbeitung.

### 17.1 Anti-Pattern: `@Transactional` und `ack.acknowledge()` in derselben Methode

```java
// ❌ Anti-Pattern: Acknowledge VOR Transaction-Commit
@KafkaListener(topics = "orders.placed.v1", groupId = "inventory-service")
@Transactional
public void onOrderPlaced(EventEnvelope<OrderPlacedPayload> envelope, Acknowledgment ack) {
    inventoryService.reserve(...);
    processedEventRepository.markProcessed(envelope.eventId());

    ack.acknowledge();   // ⚠️ Acknowledge vor Transaction-Commit!
}
```

**Was hier subtil falsch ist:** `ack.acknowledge()` **innerhalb** der `@Transactional`-Methode bedeutet, dass der Acknowledge vor dem Transaction-Commit aufgerufen wird. Wenn die Transaktion danach in der Commit-Phase fehlschlägt (z. B. Constraint Violation, DB-Disconnect), ist der Offset bereits committed — das Event geht verloren.

### 17.2 Korrekte Implementierung: Acknowledge NACH Transaction-Commit

```java
// ✅ Korrekt: zwei getrennte Methoden
@Component
public class InventoryOrderEventConsumer {

    private final InventoryProcessingService processingService;
    private final TenantContext tenantContext;

    @KafkaListener(topics = "orders.placed.v1", groupId = "inventory-service")
    public void onOrderPlaced(
            EventEnvelope<OrderPlacedPayload> envelope,
            Acknowledgment ack) {

        // ✅ Tenant-Context im aktuellen Thread setzen (siehe Sektion 27)
        try (var scope = tenantContext.setForCurrentThread(envelope.tenantId())) {
            // ✅ Service-Aufruf mit @Transactional — Tx wird committed beim Return
            processingService.processWithIdempotency(envelope);
        }

        // ✅ Acknowledge NACH erfolgreichem Service-Aufruf (Tx committed)
        ack.acknowledge();
    }
}
```

```java
@Service
public class InventoryProcessingService {

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void processWithIdempotency(EventEnvelope<OrderPlacedPayload> envelope) {
        if (processedEventRepository.existsByTenantIdAndEventId(
                envelope.tenantId(), envelope.eventId())) {
            return;   // bereits verarbeitet — idempotent
        }

        inventoryService.reserve(
            new OrderId(UUID.fromString(envelope.payload().orderId())),
            envelope.payload().items()
        );

        processedEventRepository.markProcessed(
            envelope.tenantId(),
            envelope.eventId(),
            envelope.eventType(),
            envelope.occurredAt()
        );

        // Transaction wird beim Methoden-Return committed
    }
}
```

**Warum die Trennung in zwei Methoden:**

- Der Listener hat KEIN `@Transactional` — `ack.acknowledge()` läuft außerhalb jeder Transaction.
- Der Service hat `@Transactional` — die Transaction wird beim Methoden-Return committed.
- `ack.acknowledge()` wird erst **nach** dem Return aufgerufen, also nach dem Commit.
- Wenn die Service-Methode wirft, kommt der Listener nicht zum `ack.acknowledge()` — der Event wird beim nächsten Poll erneut zugestellt.

### 17.3 Alternative: `KafkaTransactionManager` für Kafka-DB-Atomarität

Wenn die fachliche Verarbeitung sowohl eine DB-Schreibung **als auch** ein Kafka-Publish benötigt, ist `ChainedKafkaTransactionManager` (deprecated seit Spring Kafka 2.7) die historische Lösung. **Modern bevorzugt:** Outbox-Pattern (Sektion 14) — die robusteste Trennung.

---

## 18. Idempotenz und `processed_events`-Tabelle

Kafka-Consumer MÜSSEN davon ausgehen, dass Events mehrfach verarbeitet werden können.

**Ursachen für mehrfache Zustellung:**

- Retry nach technischem Fehler.
- Consumer-Neustart vor Commit.
- Rebalancing.
- Erneutes Abspielen alter Events (Replay).
- Manuelles Zurücksetzen von Offsets.
- Producer-Wiederholung (auch bei `enable.idempotence=true` möglich nach Cluster-Wechsel).
- Outbox-Re-Publish.
- DLT-Reprocessing.

### 18.1 Anti-Pattern: keine Idempotenz

```java
// ❌ Anti-Pattern
@KafkaListener(topics = "orders.placed.v1")
public void onOrderPlaced(OrderPlacedEvent event) {
    inventory.reduce(event.items());   // bei Doppel-Zustellung wird Bestand doppelt reduziert
}
```

### 18.2 Gute Anwendung: `processed_events`-Tabelle

**Tabelle mit Tenant-Isolation, `TIMESTAMPTZ` und Partitionierung:**

```sql
CREATE TABLE processed_events (
    tenant_id VARCHAR(36) NOT NULL,
    event_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (tenant_id, event_id, processed_at)
) PARTITION BY RANGE (processed_at);

-- Monatliche Partitionen
CREATE TABLE processed_events_2026_05 PARTITION OF processed_events
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE processed_events_2026_06 PARTITION OF processed_events
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

-- Index für Lookups (innerhalb jeder Partition)
CREATE INDEX idx_processed_events_lookup
    ON processed_events (tenant_id, event_id);
```

**Warum diese Form:**

| Element | Begründung |
|---|---|
| `tenant_id` als Teil des Primary Keys | Defense-in-Depth gegen Tenant-Übergriffe |
| `TIMESTAMPTZ` | korrektes Verhalten bei verteilten Consumern in verschiedenen Zeitzonen |
| Partitionierung nach `processed_at` | Cleanup wird trivial — alte Partitionen droppen statt rows deleten |
| `processed_at` im Primary Key | erforderlich für PostgreSQL-Partitionierung |

### 18.3 Idempotenz-Service

```java
@Service
public class ProcessedEventService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean markIfNotProcessed(String tenantId, UUID eventId, String eventType) {
        try {
            jdbcTemplate.update("""
                INSERT INTO processed_events(tenant_id, event_id, event_type, processed_at)
                VALUES (?, ?, ?, NOW())
                """, tenantId, eventId, eventType);
            return true;
        } catch (DuplicateKeyException alreadyProcessed) {
            return false;
        }
    }
}
```

```java
@Service
public class InventoryProcessingService {

    private final ProcessedEventService processedEventService;
    private final InventoryService inventoryService;

    @Transactional
    public void processWithIdempotency(EventEnvelope<OrderPlacedPayload> envelope) {
        var marked = processedEventService.markIfNotProcessed(
            envelope.tenantId(),
            envelope.eventId(),
            envelope.eventType()
        );

        if (!marked) {
            return;   // bereits verarbeitet
        }

        inventoryService.reserve(envelope.payload());
    }
}
```

Diese Variante nutzt die Datenbank-Unique-Constraint als **atomare Idempotenzsicherung**. Der `INSERT` ist atomar — er gelingt entweder (markiert das Event als verarbeitet) oder schlägt mit `DuplicateKeyException` fehl (Event war bereits verarbeitet).

### 18.4 Cleanup-Strategie

**Mit Partitionierung (bevorzugt):**

```sql
-- Alte Partition droppen (Datenmenge wird in Sekunden entfernt)
DROP TABLE processed_events_2025_05;
```

**Ohne Partitionierung:**

```sql
-- Cleanup-Job (täglich, behält 90 Tage)
DELETE FROM processed_events
WHERE processed_at < NOW() - INTERVAL '90 days';
```

Bei einem produktiven System mit 1.000 Events/Sekunde wächst die Tabelle auf ~30 Milliarden Zeilen pro Jahr. Ohne Cleanup ist das ein Production-Killer — Partitionierung macht das trivial handhabbar.

---

## 19. Fehlerbehandlung mit `@RetryableTopic`

Spring Kafka 2.7+ bietet **`@RetryableTopic`** — das **bevorzugte Pattern** für nicht-blockierendes Retry. Im Gegensatz zu `DefaultErrorHandler` mit `BackOff` blockiert `@RetryableTopic` nicht den Consumer-Thread.

### 19.1 Konzept: nicht-blockierendes Retry mit eigenen Topics

```
Hauptthema:               orders.placed.v1
Retry-Topic 0:            orders.placed.v1-retry-0      (nach 1 Sekunde)
Retry-Topic 1:            orders.placed.v1-retry-1      (nach 2 Sekunden)
Retry-Topic 2:            orders.placed.v1-retry-2      (nach 4 Sekunden)
Dead Letter Topic:        orders.placed.v1-dlt
```

Wenn die Verarbeitung im Hauptthema fehlschlägt, wird das Event in `retry-0` geschrieben. Ein anderer Listener konsumiert `retry-0` mit Verzögerung. Wenn das auch fehlschlägt, geht es nach `retry-1` usw. Andere Events im Hauptthema können in der Zwischenzeit weiter verarbeitet werden.

### 19.2 `@RetryableTopic` Beispiel

```java
@Component
public class InventoryOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryOrderEventConsumer.class);

    private final InventoryProcessingService processingService;
    private final TenantContext tenantContext;

    @RetryableTopic(
        attempts = "4",                                                  // 1 + 3 Retries
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 60000),
        autoCreateTopics = "false",                                      // Topics existieren bereits
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        include = { TransientException.class, IOException.class },        // ← retry-fähig
        exclude = {
            IllegalArgumentException.class,
            org.apache.kafka.common.errors.SerializationException.class
        }                                                                 // ← nicht retry-fähig (sofort DLT)
    )
    @KafkaListener(topics = "orders.placed.v1", groupId = "inventory-service")
    public void onOrderPlaced(
            EventEnvelope<OrderPlacedPayload> envelope,
            Acknowledgment ack) {

        try (var scope = tenantContext.setForCurrentThread(envelope.tenantId())) {
            processingService.processWithIdempotency(envelope);
        }

        ack.acknowledge();
    }

    @DltHandler
    public void handleDlt(
            EventEnvelope<OrderPlacedPayload> envelope,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exception,
            @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic) {

        log.error("Event went to DLT after retry exhaustion: " +
                  "eventId={}, originalTopic={}, tenantId={}, exception={}",
                envelope.eventId(),
                originalTopic,
                envelope.tenantId(),
                exception);

        // Optional: Alert auslösen, in Tracking-System schreiben, etc.
    }
}
```

### 19.3 `@RetryableTopic` vs. `DefaultErrorHandler` — wann was?

| Aspekt | `@RetryableTopic` | `DefaultErrorHandler` |
|---|---|---|
| **Blockierend** | Nein — Retry läuft in eigenen Topics | Ja — `BackOff` blockiert Consumer-Thread |
| **Sichtbarkeit** | Retry-State ist in Kafka sichtbar | Retry-State nur in Anwendung |
| **Skalierung** | Retry-Topics können andere Consumer-Counts haben | Skaliert mit Hauptthema |
| **Reihenfolge pro Partition** | Wird gebrochen (Events in Retry-Topic) | Bleibt erhalten |
| **DLT-Integration** | Eingebaut über `@DltHandler` | Über `DeadLetterPublishingRecoverer` |
| **Audit** | Jeder Retry-Versuch ist als separates Event sichtbar | Nur in Logs |

**Empfehlung:**

- **`@RetryableTopic`** für die meisten Use Cases — nicht-blockierend, gute Sichtbarkeit, skaliert besser.
- **`DefaultErrorHandler`** nur, wenn strikte Reihenfolge pro Partition zwingend ist (selten der Fall).

### 19.4 `DefaultErrorHandler` als Alternative

```java
@Configuration
public class KafkaErrorHandlingConfiguration {

    @Bean
    public DefaultErrorHandler defaultErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        var recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, exception) -> new TopicPartition(
                record.topic() + ".DLT",
                record.partition()
            )
        );

        var backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxInterval(60_000L);
        backOff.setMaxElapsedTime(120_000L);

        var errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            org.apache.kafka.common.errors.SerializationException.class
        );

        return errorHandler;
    }
}
```

---

## 20. Dead Letter Topics und Recovery

### 20.1 DLT-Regeln

1. Jedes DLT MUSS einen Owner haben (Team oder Person).
2. Jedes DLT MUSS Monitoring haben (Alarmierung bei Menge, Alter, kritischem Eventtyp).
3. Jedes DLT MUSS ein Runbook haben — wer macht was, wenn DLT-Nachrichten auflaufen?
4. DLT-Nachrichten DÜRFEN NICHT ignoriert werden.
5. Reprocessing-Mechanismus MUSS definiert sein.
6. DLT-Topic enthält keine unnötigen sensitiven Daten.
7. DLT-Retention ist bewusst gesetzt (länger als Hauptthema, damit Analyse möglich ist).

### 20.2 Fehlerarten und Strategien

| Fehlerart | Beispiel | Strategie |
|---|---|---|
| Temporärer technischer Fehler | DB kurz nicht erreichbar | Retry mit Backoff |
| Dauerhafter technischer Fehler | Schema kann nicht deserialisiert werden | Sofort DLT, Alarm |
| Fachlicher erwartbarer Fehler | Bestand reicht nicht | Fachliches Folgeevent oder DLT je nach Prozess |
| Poison Message | Event verletzt Contract dauerhaft | DLT, Analyse, Fix |
| Downstream nicht verfügbar | externer Provider down | Retry, Circuit Breaker, DLT nach Limit |

### 20.3 DLT-Reprocessing

```java
@RestController
@RequestMapping("/admin/dlt")
public class DltReprocessingController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerFactory<String, Object> consumerFactory;

    @PostMapping("/reprocess")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReprocessingReport> reprocessDlt(
            @RequestParam String dltTopic,
            @RequestParam String targetTopic,
            @RequestParam(defaultValue = "100") int maxRecords) {

        var consumer = consumerFactory.createConsumer("dlt-reprocessor", "");
        consumer.subscribe(List.of(dltTopic));

        var processed = 0;
        var records = consumer.poll(Duration.ofSeconds(10));

        for (var record : records) {
            if (processed >= maxRecords) break;

            kafkaTemplate.send(targetTopic, record.key(), record.value());
            processed++;
        }

        consumer.commitSync();
        consumer.close();

        return ResponseEntity.ok(new ReprocessingReport(dltTopic, targetTopic, processed));
    }
}
```

**Wichtig:** Reprocessing MUSS authentifiziert und authorisiert sein. DLT-Reprocessing ohne Berechtigungsprüfung ist eine Sicherheitslücke (vergleiche QG-JAVA-006 v2 Sektion 14).

---

## 21. Schema Registry, Avro, JSON Schema und Protobuf

### 21.1 Warum Schema Registry?

Event-Schemas sind Verträge zwischen Teams. Ohne Schema Registry können Producer inkompatible Events veröffentlichen und Consumer brechen erst zur Laufzeit.

Schema Registry ermöglicht:

- Zentrale Schemaablage.
- Kompatibilitätsprüfung (BACKWARD, FORWARD, FULL).
- Versionierung pro Subject.
- Codegenerierung.
- Kontrollierte Evolution.

### 21.2 Avro (Default für Spring-Boot-zentrierte Setups)

```json
{
  "type": "record",
  "name": "OrderPlacedEvent",
  "namespace": "com.example.events.orders.v1",
  "fields": [
    { "name": "eventId", "type": { "type": "string", "logicalType": "uuid" } },
    { "name": "orderId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "currency", "type": "string", "default": "EUR" },
    { "name": "totalCents", "type": "long" },
    { "name": "occurredAt", "type": { "type": "long", "logicalType": "timestamp-millis" } },
    { "name": "tenantId", "type": "string" }
  ]
}
```

**Vorteile:** Binär (kompakt), Schema Registry Integration, Codegenerierung.
**Nachteile:** Nicht human-readable, Schema Registry Pflicht.

### 21.3 JSON Schema (für Mixed-Stack-Setups)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "OrderPlacedEvent",
  "required": ["eventId", "orderId", "customerId", "totalCents", "tenantId"],
  "properties": {
    "eventId": { "type": "string", "format": "uuid" },
    "orderId": { "type": "string" },
    "customerId": { "type": "string" },
    "currency": { "type": "string", "default": "EUR" },
    "totalCents": { "type": "integer", "minimum": 0 },
    "occurredAt": { "type": "string", "format": "date-time" },
    "tenantId": { "type": "string" }
  }
}
```

**Vorteile:** Human-readable, einfach zu debuggen, Cross-Language (Java + Node.js + Python).
**Nachteile:** Größere Payloads, weniger strikte Typ-Garantien als Avro/Protobuf.

### 21.4 Protobuf (für hohen Durchsatz und Cross-Language)

```protobuf
syntax = "proto3";

package com.example.events.orders.v1;

import "google/protobuf/timestamp.proto";

message OrderPlacedEvent {
  string event_id = 1;
  string order_id = 2;
  string customer_id = 3;
  string currency = 4;
  int64 total_cents = 5;
  google.protobuf.Timestamp occurred_at = 6;
  string tenant_id = 7;
}
```

**Vorteile:** Hoher Durchsatz, kompakt, Cross-Language, gRPC-Integration.
**Nachteile:** Schema separat (`.proto`-Files), Tooling komplexer, weniger Schema Registry Integration als Avro.

### 21.5 Auswahl-Matrix

| Format | Wann | Trade-offs |
|---|---|---|
| **Avro** | Spring-Boot-zentriert, viele Consumer, Schema Registry vorhanden | Binär (nicht human-readable), Schema Registry Pflicht |
| **JSON Schema** | Mixed Stack (Java + Node.js + Python), Debugging wichtig | Größere Payloads, weniger strikte Typ-Garantien |
| **Protobuf** | Hoher Durchsatz, Cross-Language, gRPC-Integration | Schema separat, Tooling komplexer |

### 21.6 Schema Registry Authentication

Schema Registry MUSS authentifizierten Zugriff haben.

```yaml
spring:
  kafka:
    properties:
      schema.registry.url: ${SCHEMA_REGISTRY_URL}

      # Option 1: Basic Auth
      basic.auth.credentials.source: USER_INFO
      basic.auth.user.info: ${SCHEMA_REGISTRY_USER}:${SCHEMA_REGISTRY_PASSWORD}

      # Option 2: OAuth Bearer Token
      # schema.registry.bearer.auth.credentials.source: STATIC_TOKEN
      # schema.registry.bearer.auth.token: ${SCHEMA_REGISTRY_TOKEN}

      # Option 3: mTLS
      # schema.registry.ssl.keystore.location: /etc/secrets/keystore.jks
      # schema.registry.ssl.keystore.password: ${KEYSTORE_PASSWORD}
```

Ohne Authentifizierung kann ein kompromittierter Service unkontrolliert Schemas registrieren oder modifizieren.

---

## 22. Event-Versionierung und Kompatibilität

### 22.1 Kompatibilitätsregeln

**Backward-kompatible Änderungen:**

- Optionales Feld mit Default hinzufügen.
- Neues Feld mit sinnvollem Default hinzufügen.
- Dokumentation ergänzen.
- Zusätzliche Enum-Werte (mit Vorsicht — Consumer müssen unbekannte Werte tolerieren).

**Breaking Changes:**

- Feld entfernen.
- Feld umbenennen.
- Feldtyp ändern.
- Pflichtfeld ohne Default hinzufügen.
- Semantik eines Feldes ändern.
- Topic-Bedeutung ändern.
- Event als Command umdeuten.

### 22.2 Versionierungs-Regel

```
Kompatible Änderung   → gleiches Topic, neue Schema-Version.
Inkompatible Änderung → neues Major-Version-Topic.
```

**Beispiel:**

```
orders.placed.v1     (alte Version, läuft weiter)
orders.placed.v2     (neue Version, breaking change)
```

### 22.3 Migration

1. Producer publiziert optional beide Versionen (`v1` und `v2`).
2. Consumer migrieren auf `v2`.
3. Monitoring prüft, ob `v1` noch konsumiert wird.
4. `v1` wird deprecated (Kommunikation an alle Consumer-Teams).
5. `v1` wird nach kommunizierter Frist abgeschaltet.

**Keine stillen Breaking Changes.**

---

## 23. Saga-Pattern für verteilte Transaktionen

Wenn eine fachliche Operation mehrere Services betrifft (z. B. Order → Payment → Inventory), gibt es **keine verteilte ACID-Transaktion** über Kafka. Stattdessen wird das **Saga-Pattern** verwendet.

### 23.1 Choreographierte Saga (bevorzugt)

Jeder Service reagiert auf Events der anderen Services, ohne zentralen Orchestrator.

```
1. OrderService     → OrderPlaced event
2. PaymentService   liest OrderPlaced → versucht Zahlung
                    → PaymentCaptured oder PaymentFailed
3. InventoryService liest PaymentCaptured → reserviert Bestand
                    → InventoryReserved oder InventoryReservationFailed
4. OrderService     liest finale Events → setzt Order-Status
                    → OrderConfirmed oder OrderCancelled
```

**Beispiel: Kompensation bei `PaymentFailed`:**

```java
@KafkaListener(topics = "payments.failed.v1", groupId = "order-service")
public void onPaymentFailed(
        EventEnvelope<PaymentFailedPayload> envelope,
        Acknowledgment ack) {

    try (var scope = tenantContext.setForCurrentThread(envelope.tenantId())) {
        orderSagaService.compensate(envelope);
    }

    ack.acknowledge();
}

@Service
public class OrderSagaService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void compensate(EventEnvelope<PaymentFailedPayload> envelope) {
        var orderId = new OrderId(UUID.fromString(envelope.payload().orderId()));

        // Idempotent: nur cancel wenn nicht bereits gecancelled
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.isAlreadyCancelled()) {
            return;
        }

        order.cancel("PAYMENT_FAILED", envelope.payload().reason());
        orderRepository.save(order);

        // Folge-Event publizieren (via Outbox)
        outboxRepository.save(OutboxEvent.create(
            UUID.randomUUID(),
            "orders.cancelled.v1",
            orderId.value().toString(),
            serialize(OrderCancelledPayload.from(order, "PAYMENT_FAILED")),
            Instant.now()
        ));
    }
}
```

**Vorteile:** Skaliert gut, keine zentrale Single-Point-of-Failure, einfach zu testen.
**Nachteile:** Saga-Logik ist über mehrere Services verteilt, schwerer zu nachzuvollziehen.

### 23.2 Orchestrierte Saga

Ein zentraler Saga-Service koordiniert alle Schritte und ruft bei Fehler Kompensations-Operationen auf.

```java
@Service
public class OrderSagaOrchestrator {

    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository;

    @Transactional
    public void execute(OrderSaga saga) {
        try {
            // Schritt 1: Payment
            var paymentResult = paymentClient.capture(saga.paymentRequest());
            saga.recordPaymentCaptured(paymentResult);

            // Schritt 2: Inventory
            var inventoryResult = inventoryClient.reserve(saga.inventoryRequest());
            saga.recordInventoryReserved(inventoryResult);

            // Schritt 3: Order Confirmation
            saga.confirm();

        } catch (PaymentFailedException ex) {
            // Kompensation
            saga.recordPaymentFailed(ex);

        } catch (InventoryReservationFailedException ex) {
            // Kompensation: Payment refunden
            paymentClient.refund(saga.paymentResult());
            saga.recordInventoryFailed(ex);
        }

        orderRepository.save(saga.toOrder());
    }
}
```

**Vorteile:** Saga-Logik zentral, leichter zu nachzuvollziehen.
**Nachteile:** Single-Point-of-Failure, Saga-Service muss Kafka-Verbindung zu allen Teilnehmern haben.

### 23.3 Wann was?

| Saga-Typ | Wann |
|---|---|
| **Choreographiert** | Locker gekoppelte Services, viele Saga-Typen, Standardfall. |
| **Orchestriert** | Wenige zentrale Sagas, komplexe Compensation-Logik, Audit-Pflicht zentral. |

Diese Guideline empfiehlt **Choreographie als Default**, weil sie besser skaliert und der Spirit von EDA-Architekturen ist.

---

## 24. Eventual Consistency

Event-Driven Architecture bedeutet häufig: Daten sind nicht sofort überall konsistent.

**Beispiel-Flow:**

1. Order Service veröffentlicht `OrderPlaced`.
2. Inventory Service reserviert Bestand einige Sekunden später.
3. Notification Service sendet E-Mail später.
4. Analytics Service aktualisiert Reporting später.

### 24.1 Daraus folgen Regeln

- UI MUSS Zwischenzustände darstellen können.
- Fachprozesse brauchen Statusmodelle.
- Timeouts und Kompensationen müssen definiert sein.
- Monitoring muss Verzögerungen sichtbar machen.
- Nutzer dürfen nicht falsche Sofortkonsistenzversprechen erhalten.

**Beispiel:**

```java
// ✅ Korrekt: Zwischenstatus zeigen
public enum OrderStatus {
    DRAFT,
    PAYMENT_PENDING,
    PAYMENT_CAPTURED,
    INVENTORY_RESERVED,
    CONFIRMED,
    CANCELLED
}

// statt sofort:
// OrderStatus = CONFIRMED   // ← falsch, wenn Inventory asynchron reserviert wird
```

### 24.2 Read Models und CQRS

Bei stark unterschiedlichen Konsistenz-Anforderungen kann **CQRS (Command Query Responsibility Segregation)** sinnvoll sein:

- **Command Side:** schreibt Events.
- **Query Side:** baut Read Models aus Events auf, optimiert für Lesen.

Read Models sind eventual consistent gegenüber dem Source-of-Truth — das ist explizit modelliert.

---

## 25. Observability und natives Tracing

### 25.1 Pflichtsignale

| Signal | Zweck |
|---|---|
| Publish-Erfolgsrate | Producer-Health |
| Publish-Fehlerrate | Producer-Probleme erkennen |
| Consumer-Verarbeitungsdauer | Performance-Bewertung |
| Consumer-Fehlerrate | Consumer-Probleme erkennen |
| Consumer Lag | Aufholzustand der Consumer |
| DLT-Rate | Failure-Rate erkennen |
| Retry-Anzahl | Retry-Druck erkennen |
| Deserialisierungsfehler | Schema-Probleme |
| Schema-Fehler | Vertrags-Verletzungen |
| Verarbeitete Events pro Topic | Volumen-Tracking |
| Zeit zwischen `occurredAt` und Verarbeitung | End-to-End-Latenz |
| Correlation IDs in Logs/Traces | Cross-Service-Debugging |

### 25.2 Natives Tracing mit Micrometer Observation

**Manuelle Header-Setzung ist Anti-Pattern seit Spring Boot 3.0.** Stattdessen wird **Micrometer Observation** verwendet — Spring Kafka propagiert automatisch W3C Trace Context Headers (`traceparent`, `tracestate`) und erzeugt Spans für Producer und Consumer.

**Konfiguration:**

```yaml
spring:
  kafka:
    producer:
      observation-enabled: true
    listener:
      observation-enabled: true
```

```java
@Bean
public KafkaTemplate<String, Object> kafkaTemplate(
        ProducerFactory<String, Object> producerFactory) {
    var template = new KafkaTemplate<>(producerFactory);
    template.setObservationEnabled(true);   // ✅ aktiviert Tracing
    return template;
}
```

```java
@KafkaListener(
    topics = "orders.placed.v1",
    groupId = "inventory-service",
    containerFactory = "kafkaListenerContainerFactory"      // mit observationEnabled=true
)
public void onOrderPlaced(EventEnvelope<OrderPlacedPayload> envelope, Acknowledgment ack) {
    // Trace Context ist automatisch im MDC verfügbar
    log.info("Processing event: eventId={}", envelope.eventId());
    // → Log enthält automatisch traceId und spanId
    // → Spans werden an OpenTelemetry-Collector exportiert
}
```

### 25.3 Logging-Regeln

```java
// ✅ Korrekt: nur Metadaten loggen
log.info("Event consumed: eventId={}, eventType={}, topic={}, partition={}, offset={}, tenantHash={}",
        envelope.eventId(),
        envelope.eventType(),
        topic,
        partition,
        offset,
        hashTenantId(envelope.tenantId()));   // ✅ Hash, kein Klartext

// ❌ Anti-Pattern: vollständiger Payload
log.info("Event payload={}", envelope.payload());
// → kann personenbezogene Daten, Geld-Beträge, interne IDs enthalten
```

### 25.4 Correlation IDs

`correlationId` und `causationId` MÜSSEN im Event-Envelope mitgereicht werden (Sektion 10.1). Spring Kafka propagiert diese **automatisch** zwischen Services über die Trace-Context-Headers — kein manuelles Header-Setting erforderlich.

---

## 26. Security- und SaaS-Aspekte

### 26.1 Keine sensitiven Daten in Events

Events landen in Topics, Logs, Replay-Prozessen, DLTs, Backups, lokalen Debug-Tools und teilweise Analytics-Systemen. Deshalb gilt **Datenminimierung**.

```java
// ❌ Anti-Pattern
public record CustomerRegisteredEvent(
        String email,
        String fullName,
        String passwordHash,       // sollte NIRGENDS sein
        String phone,
        String iban
) {}

// ✅ Datenminimal
public record CustomerRegisteredEvent(
        String customerId,
        String tenantId,
        Instant occurredAt
) {}
```

Consumer, die Details benötigen, können diese über **berechtigte APIs** oder interne Read Models abrufen.

### 26.2 Tenant-Isolation in Events

Bei SaaS-Systemen MUSS jedes tenantbezogene Event den Tenant-Kontext enthalten oder eindeutig aus dem Key ableitbar machen.

```java
public record TenantScopedEvent<T>(
        UUID eventId,
        String tenantId,
        T payload
) {}
```

Consumer MÜSSEN Tenant-Kontext prüfen und DÜRFEN nicht tenantübergreifend aggregieren, wenn das fachlich oder regulatorisch nicht erlaubt ist.

### 26.3 Kafka-ACLs und Least Privilege

Kafka-ACLs MÜSSEN Least Privilege umsetzen:

- Producer darf nur auf eigene Topics schreiben.
- Consumer darf nur relevante Topics lesen.
- Admin-Rechte sind stark begrenzt.
- Schema Registry Zugriff ist geschützt (Sektion 21.6).
- DLT-Zugriff ist beschränkt auf Operations-Team.
- Secrets liegen nicht in `application.yml`.

```bash
# Beispiel: Producer-ACL
kafka-acls --bootstrap-server kafka:9092 \
    --add \
    --allow-principal User:order-service \
    --producer \
    --topic orders.placed.v1

# Beispiel: Consumer-ACL
kafka-acls --bootstrap-server kafka:9092 \
    --add \
    --allow-principal User:inventory-service \
    --consumer \
    --topic orders.placed.v1 \
    --group inventory-service
```

### 26.4 Authentication: SASL/SSL

```yaml
spring:
  kafka:
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: SCRAM-SHA-512
      sasl.jaas.config: |
        org.apache.kafka.common.security.scram.ScramLoginModule required
        username="${KAFKA_USERNAME}"
        password="${KAFKA_PASSWORD}";
      ssl.truststore.location: /etc/secrets/truststore.jks
      ssl.truststore.password: ${TRUSTSTORE_PASSWORD}
```

---

## 27. Tenant-Context im Consumer

Tenant-Isolation hört nicht beim Event-Envelope auf — sie MUSS beim Consumer in den Thread-Kontext propagiert werden, damit alle nachfolgenden DB-Operationen, Service-Aufrufe und Logs den richtigen Tenant kennen.

**Cross-Reference:** Diese Sektion baut auf QG-JAVA-006 v2 Sektion 14.6 (Tenant-Context-Pattern) auf.

### 27.1 Tenant-Context-Setzung beim Consumer

```java
@KafkaListener(topics = "orders.placed.v1", groupId = "inventory-service")
public void onOrderPlaced(
        EventEnvelope<OrderPlacedPayload> envelope,
        Acknowledgment ack) {

    // ✅ Tenant-Context im aktuellen Thread setzen
    try (var scope = tenantContext.setForCurrentThread(envelope.tenantId())) {
        processingService.processWithIdempotency(envelope);
        ack.acknowledge();
    }
    // ✅ TenantContext wird automatisch aufgeräumt
}
```

```java
@Service
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public TenantScope setForCurrentThread(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        CURRENT_TENANT.set(tenantId);
        return () -> CURRENT_TENANT.remove();
    }

    public String currentTenantId() {
        var tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new TenantContextNotSetException();
        }
        return tenantId;
    }

    public interface TenantScope extends AutoCloseable {
        @Override
        void close();   // keine Exception
    }
}
```

### 27.2 Tenant-Filtering bei Topic-Lesen

In Multi-Tenant-Setups kann ein Service Events von vielen Tenants verarbeiten. Wenn der Service nur für bestimmte Tenants läuft:

```java
@KafkaListener(topics = "orders.placed.v1", groupId = "inventory-service-eu")
public void onOrderPlaced(EventEnvelope<OrderPlacedPayload> envelope, Acknowledgment ack) {
    // ✅ Frühe Filterung
    if (!authorizedTenants.isEuTenant(envelope.tenantId())) {
        ack.acknowledge();   // Event nicht für uns — committen und ignorieren
        return;
    }

    try (var scope = tenantContext.setForCurrentThread(envelope.tenantId())) {
        processingService.processWithIdempotency(envelope);
    }

    ack.acknowledge();
}
```

Bei großen Tenants: separate Topics pro Tenant-Gruppe (`orders.placed.v1.eu`, `orders.placed.v1.us`), separate Consumer Groups.

### 27.3 Tenant-Context in `processed_events` und Outbox

Beide Tabellen MÜSSEN `tenant_id` als Teil des Primary Keys haben (siehe Sektion 18.2 und 14.3). Das ist Defense-in-Depth gegen Tenant-Übergriffe — auch wenn `eventId` global eindeutig sein sollte, schützt die Tenant-Spalte gegen Implementierungsfehler.

---

## 28. Datenschutz und Retention

Kafka-Topics haben Retention. Dadurch bleiben Events bewusst für eine Zeit gespeichert. Das ist betrieblich nützlich, kann aber Datenschutzrisiken erzeugen.

### 28.1 Regeln

1. Personenbezogene Daten in Events vermeiden.
2. Retention pro Topic fachlich begründen.
3. DLT-Retention begrenzen (typisch 30 Tage, nicht unbegrenzt).
4. Kompakte technische IDs statt Klardaten verwenden.
5. Löschkonzepte bei personenbezogenen Events definieren.
6. Backups und Mirror Topics berücksichtigen.
7. Reprocessing darf keine gelöschten personenbezogenen Daten wiederherstellen.

### 28.2 Wenn personenbezogene Daten zwingend in Events müssen

Brauchen das Event:

- Datenschutzbewertung (DSFA bei DSGVO-relevanten Daten).
- Retention-Entscheidung (kürzer als bei nicht-personenbezogenen Events).
- Zugriffsbegrenzung (ACLs auf Topic-Ebene).
- Pseudonymisierung wo möglich.
- Recht-auf-Löschung-Konzept (z. B. Tombstone-Events bei Compacted Topics).

```java
// ✅ Tombstone für GDPR Right-to-be-Forgotten
public void deleteCustomerData(String customerId) {
    // Tombstone in Compacted Topic
    kafkaTemplate.send("customers.profile.v1", customerId, null);

    // Compaction entfernt alle vorherigen Events mit demselben Key
}
```

---

## 29. Testing

### 29.1 Unit-Test des Producers

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

### 29.2 Consumer-Test mit Idempotenz

```java
@ExtendWith(MockitoExtension.class)
class InventoryProcessingServiceTest {

    @Mock InventoryService inventoryService;
    @Mock ProcessedEventService processedEventService;

    @InjectMocks InventoryProcessingService processingService;

    @Test
    void process_skipsEvent_whenAlreadyProcessed() {
        var envelope = validOrderPlacedEnvelope();

        when(processedEventService.markIfNotProcessed(
            envelope.tenantId(), envelope.eventId(), envelope.eventType()
        )).thenReturn(false);

        processingService.processWithIdempotency(envelope);

        verifyNoInteractions(inventoryService);
    }

    @Test
    void process_reservesInventory_whenNotProcessed() {
        var envelope = validOrderPlacedEnvelope();

        when(processedEventService.markIfNotProcessed(
            envelope.tenantId(), envelope.eventId(), envelope.eventType()
        )).thenReturn(true);

        processingService.processWithIdempotency(envelope);

        verify(inventoryService).reserve(any(), any());
    }
}
```

### 29.3 Integrationstest mit Kafka Testcontainers (KRaft)

```java
@SpringBootTest(classes = {
    KafkaTestConfiguration.class,
    OrderEventConsumer.class,
    InventoryProcessingService.class
})    // ✅ Slice statt voll @SpringBootTest
@Testcontainers
class OrderKafkaIntegrationTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("apache/kafka:3.7.0")     // ✅ KRaft-Modus
    );

    @Autowired
    KafkaTemplate<String, EventEnvelope<OrderPlacedPayload>> kafkaTemplate;

    @Autowired
    InventoryRepository inventoryRepository;

    @Test
    void consumerProcessesOrderPlacedEvent() {
        var event = validOrderPlacedEvent();

        kafkaTemplate.send("orders.placed.v1", event.payload().orderId(), event);

        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() ->
                assertThat(inventoryRepository.hasReservation(event.payload().orderId()))
                    .isTrue()
            );
    }
}
```

### 29.4 Messaging Contract Test mit Pact

**Cross-Reference:** Diese Sektion verbindet sich mit QG-JAVA-019 v2 Sektion 14 (Messaging Contracts). Siehe dort für vertiefende Details.

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "order-events", providerType = ProviderType.ASYNCH)
class BillingServiceConsumerTest {

    @Pact(consumer = "billing-service")
    MessagePact orderCreatedEvent(MessagePactBuilder builder) {
        return builder
            .given("a new order is placed in tenant tenant-test")
            .expectsToReceive("OrderCreatedEvent")
            .withMetadata(Map.of(
                "contentType", "application/json",
                "tenantId", "tenant-test"
            ))
            .withContent(newJsonBody(body -> {
                body.uuid("eventId");
                body.stringType("eventType", "OrderCreatedEvent");
                body.integerType("orderId");
                body.stringType("status", "CREATED");
                body.decimalType("amount", new BigDecimal("99.99"));
                body.stringType("currency", "EUR");
                body.timestamp("createdAt");
            }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "orderCreatedEvent")
    void consumesOrderCreatedEvent_createsBillingEntry(List<Message> messages) {
        var rawMessage = messages.get(0).contentsAsString();
        var event = orderEventDeserializer.deserialize(rawMessage);

        billingService.handle(event);

        assertThat(billingRepository.findByOrderId(event.orderId()))
            .isPresent();
    }
}
```

### 29.5 Schema-Kompatibilitätstest

```java
@Test
void orderPlacedSchemaIsBackwardCompatible() {
    var schemaRegistry = new MockSchemaRegistryClient();

    // v1 registrieren
    schemaRegistry.register("orders.placed.v1-value", new AvroSchema(orderPlacedV1Schema));

    // v2 muss BACKWARD-kompatibel sein
    var compatibility = schemaRegistry.testCompatibility(
        "orders.placed.v1-value",
        new AvroSchema(orderPlacedV2Schema)
    );

    assertThat(compatibility).isTrue();
}
```

---

## 30. Anti-Patterns

### 30.1 Kafka als Ersatz für klare Servicegrenzen

Wenn die Fachgrenzen unklar sind, macht Kafka sie nicht klarer. Es verteilt nur die Unklarheit.

### 30.2 Event = komplette Entity

```java
// ❌ Anti-Pattern
public record OrderPlacedEvent(OrderEntity order) {}
```

JPA-Entities sind keine Event-Verträge. Lazy Loading, Hibernate-Proxies, internes Schema — alles davon ist Risiko.

### 30.3 Payload vollständig loggen

```java
// ❌ Anti-Pattern
log.info("Received event {}", event);
```

**Gefahr:** PII, Secrets, große Logs, Kosten. Nur Metadaten loggen (Sektion 25.3).

### 30.4 Keine Idempotenz

Ein Consumer, der doppelte Events nicht verträgt, ist produktiv riskant. Siehe Sektion 18.

### 30.5 Auto-Commit ohne Verarbeitungsbewusstsein

```yaml
spring.kafka.consumer.enable-auto-commit: true   # ❌ Anti-Pattern bei DB-Operationen
```

Automatisches Commit kann Offsets bestätigen, bevor fachliche Verarbeitung erfolgreich abgeschlossen ist.

### 30.6 Keine DLT-Strategie

Poison Messages blockieren Verarbeitung oder verschwinden unkontrolliert. Siehe Sektion 20.

### 30.7 Breaking Schema Change im gleichen Topic

Feld entfernen oder umbenennen ohne Versionierung bricht Consumer. Siehe Sektion 22.

### 30.8 Random Partition Key

```java
// ❌ Zerstört Reihenfolge pro Aggregat
kafkaTemplate.send(topic, UUID.randomUUID().toString(), event);
```

### 30.9 Kafka für synchrone Entscheidung

Wenn der Nutzer **jetzt** wissen muss, ob Zahlung autorisiert wurde, ist Kafka oft nicht der richtige primäre Mechanismus.

### 30.10 DLT ohne Owner

Ein DLT ohne Monitoring ist nur ein **stilles Fehlergrab**.

### 30.11 `@KafkaListener` mit `@Transactional` und `ack.acknowledge()` in derselben Methode

Klassischer Bug — Acknowledge vor Transaction-Commit. Event kann verloren gehen. Siehe Sektion 17.1.

### 30.12 Manuelles Header-Setting für Tracing

Anti-Pattern seit Spring Boot 3.0. `observation-enabled: true` macht das automatisch. Siehe Sektion 25.2.

### 30.13 `processed_events`-Tabelle ohne Tenant-Isolation und Retention

```sql
-- ❌ Production-Killer nach 6 Monaten
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(200),
    processed_at TIMESTAMP   -- ohne Time Zone!
);
```

Korrekte Form siehe Sektion 18.2.

### 30.14 `DefaultErrorHandler` mit langem Backoff in Reihenfolge-kritischen Topics

Blockiert den Consumer-Thread. `@RetryableTopic` ist die richtige Lösung (Sektion 19).

### 30.15 Tenant-IDs aus Request-Feldern in Event-Envelopes

```java
// ❌ Privilege Escalation
var event = new EventEnvelope<>(eventId, ..., request.tenantId(), payload);
```

`tenantId` MUSS aus geprüftem `TenantContext` stammen. Siehe Sektion 27.

---

## 31. CI/CD-Standard

### 31.1 Pflicht-Gates

1. Unit-Tests für Producer/Consumer-Logik.
2. Integrationstest mit Kafka oder Testcontainers für kritische Flows.
3. Schema-Kompatibilitätsprüfung in CI.
4. Keine neuen Events ohne Owner.
5. Keine `latest`-Images in Kafka-Testcontainers (`apache/kafka:3.7.0` oder versionierter Tag).
6. Keine sensiblen Felder im Schema ohne Freigabe.
7. DLT/Retry-Konfiguration geprüft.
8. Observability-Metriken vorhanden.
9. Security-Konfiguration für Kafka-Zugriff geprüft.
10. Outbox-Test für kritische Producer.
11. Messaging Contract Tests (Pact) für Cross-Service-Verträge.

### 31.2 GitHub Actions Beispiel

```yaml
name: Kafka Integration

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Unit Tests
        run: ./gradlew test

      - name: Integration Tests with Kafka
        run: ./gradlew integrationTest

      - name: Schema Compatibility Check
        run: ./gradlew checkSchemaCompatibility

      - name: Pact Consumer Tests
        run: ./gradlew pactTest

      - name: Publish Pacts to Broker
        if: github.ref == 'refs/heads/main'
        env:
          PACT_BROKER_URL: ${{ secrets.PACT_BROKER_URL }}
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
        run: ./gradlew pactPublish
```

### 31.3 Schema-Kompatibilitätsprüfung

```groovy
// build.gradle
plugins {
    id 'com.github.imflog.kafka-schema-registry-gradle-plugin' version '1.13.0'
}

schemaRegistry {
    url = 'http://schema-registry:8081'

    config {
        subject('orders.placed.v1-value', 'BACKWARD')
    }

    register {
        subject('orders.placed.v1-value', 'src/main/avro/OrderPlacedEvent.avsc', 'AVRO')
    }
}

tasks.named('check') {
    dependsOn 'testSchemasTask'
}
```

---

## 32. Werkzeuglandschaft: Spring Kafka, Kafka Streams, Spring Cloud Stream

### 32.1 Auswahl-Matrix

| Werkzeug | Wann |
|---|---|
| **Spring Kafka** | Direkter Producer/Consumer, einzelne Events, fachliche Verarbeitung. Diese Guideline ist primär auf Spring Kafka fokussiert. |
| **Kafka Streams** | Stream-Processing, Joins zwischen Topics, Windowed Aggregations, Stateful Pipelines. |
| **Spring Cloud Stream** | Abstraktion über Kafka (und RabbitMQ, Pulsar etc.), schnelle Integration, weniger Kontrolle. |

### 32.2 Spring Kafka (Default)

```java
@KafkaListener(topics = "orders.placed.v1")
public void onOrderPlaced(EventEnvelope<OrderPlacedPayload> envelope) {
    // direkte Event-Verarbeitung
}
```

### 32.3 Kafka Streams

Für Stream-Processing-Pipelines:

```java
@Bean
public KStream<String, OrderEvent> orderEventStream(StreamsBuilder builder) {
    var orders = builder.stream("orders.placed.v1");
    var payments = builder.stream("payments.captured.v1");

    return orders.join(
        payments,
        (order, payment) -> new OrderWithPayment(order, payment),
        JoinWindows.of(Duration.ofMinutes(5))
    );
}
```

### 32.4 Spring Cloud Stream

Für höhere Abstraktion:

```java
@Bean
public Function<EventEnvelope<OrderPlacedPayload>, EventEnvelope<FulfillmentRequestedPayload>>
        processOrder() {
    return order -> {
        // Verarbeitung
        return EventEnvelope.from(...);
    };
}
```

```yaml
spring:
  cloud:
    stream:
      bindings:
        processOrder-in-0:
          destination: orders.placed.v1
        processOrder-out-0:
          destination: fulfillment.requested.v1
```

**Diese Guideline empfiehlt Spring Kafka als Default**, weil es mehr Kontrolle und bessere Integration mit den hier dokumentierten Patterns bietet.

---

## 33. Migration bestehender Kafka-Integrationen

Migration erfolgt in Schritten:

1. Alle Topics inventarisieren.
2. Producer und Consumer je Topic identifizieren.
3. Owner pro Topic festlegen.
4. Schemas erfassen oder einführen.
5. Topic-Naming vereinheitlichen (`<domain>.<event-name>.v<major>`).
6. Partition Key prüfen (fachliche Aggregat-ID).
7. Idempotenz in Consumer ergänzen (`processed_events`-Tabelle mit Tenant-Isolation).
8. `@RetryableTopic` oder `DefaultErrorHandler` mit DLT ergänzen.
9. Outbox für kritische Producer ergänzen, ggf. auf Debezium CDC migrieren.
10. PII/Secrets in Events prüfen und entfernen.
11. Observability-Metriken ergänzen, `observation-enabled` aktivieren.
12. Manuelles Tracing-Header-Setting durch native Tracing ersetzen.
13. `@KafkaListener` mit `@Transactional` und `ack.acknowledge()` durch zwei-Methoden-Pattern ersetzen.
14. Tenant-Context-Setzung beim Consumer ergänzen.
15. Integrationstests mit Kafka/Testcontainers (KRaft) ergänzen.
16. Messaging Contract Tests mit Pact (siehe QG-JAVA-019 v2) für kritische Cross-Service-Verträge.
17. Schema-Kompatibilitätsprüfung in CI aktivieren.
18. Kafka-Cluster auf KRaft migrieren (falls noch Zookeeper).
19. Alte inkompatible Topics geordnet ausphasen.

---

## 34. Review-Checkliste

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| Notwendigkeit | Ist Kafka laut Sektion 7 das richtige Werkzeug? | §7 |
| Event-Typ | Domain Event oder Command sauber unterschieden? | §8 |
| Topic-Name | Folgt `<domain>.<event-name>.v<major>`? | §9.1 |
| Subject-Strategie | TopicNameStrategy bewusst gewählt? | §9.2 |
| Owner | Topic und Schema haben Owner? | §3.1.1 |
| Schema | Schema in Schema Registry, Auth aktiviert? | §3.1.2-3, §21.6 |
| Schema-Format | Avro/JSON Schema/Protobuf bewusst gewählt? | §21.5 |
| Envelope | EventEnvelope mit Pflichtfeldern? | §10.1 |
| Payload | Datenminimal, keine PII, kein `double` für Geld? | §10.2 |
| Producer-Config | `acks=all`, `enable.idempotence=true`, `observation-enabled`? | §11.1 |
| Send-Behandlung | `whenComplete` mit Logging und Metriken? | §11.3 |
| Partition Key | Fachliche Aggregat-ID? | §13 |
| Outbox | Bei DB+Kafka kritischer Konsistenz? | §14 |
| Outbox-Dispatcher | `SKIP LOCKED` und exponentielles Backoff? | §14.4 |
| CDC | Debezium für hohe Last erwogen? | §15 |
| Transactional Producer | Bei Read-Process-Write Pattern? | §12 |
| Consumer-Config | `enable-auto-commit=false`, `ack-mode: manual`? | §16 |
| Acknowledge-Semantik | Acknowledge NACH Tx-Commit (zwei Methoden)? | §17 |
| Idempotenz | `processed_events` mit Tenant-Isolation, `TIMESTAMPTZ`, Partitionierung? | §18 |
| Retry | `@RetryableTopic` mit `include`/`exclude`? | §19 |
| DLT | Owner, Monitoring, Runbook, Reprocessing definiert? | §20 |
| Tracing | `observation-enabled` (kein manuelles Header-Setting)? | §25.2 |
| Logging | Nur Metadaten, keine Payloads, Tenant-Hash? | §25.3 |
| Tenant-Context | `setForCurrentThread` beim Consumer? | §27.1 |
| Saga | Bei verteilten Transaktionen, choreographiert? | §23 |
| Eventual Consistency | UI/Status-Modell zeigt Zwischenzustände? | §24 |
| Sicherheit | ACLs, SASL/SSL, kein PII? | §26 |
| Testing | Unit, Integration, Pact Messaging, Schema? | §29 |
| KRaft | Apache Kafka 3.7+ in KRaft-Modus? | §6.1 |
| Schwester-Guidelines | QG-JAVA-006 v2 (Tenant), QG-JAVA-019 v2 (Pact) konsultiert? | — |

---

## 35. Automatisierbare Prüfungen

### 35.1 ArchUnit-Regeln

```java
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.kafka.annotation.KafkaListener;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class KafkaArchitectureTest {

    // ✅ Kafka-Listener nicht in Domain-Code
    @ArchTest
    static final ArchRule kafka_listeners_should_not_be_in_domain =
        noMethods()
            .that().areAnnotatedWith(KafkaListener.class)
            .should().beDeclaredInClassesThat().resideInAPackage("..domain..")
            .because("Kafka ist Infrastruktur und gehört in Adapter oder Messaging-Pakete.");

    // ✅ Outbox-Repository nur im Adapter-Layer
    @ArchTest
    static final ArchRule outbox_repository_only_in_adapter =
        classes()
            .that().haveNameMatching(".*OutboxRepository.*")
            .should().resideInAPackage("..adapter..")
            .orShould().resideInAPackage("..messaging..");

    // ✅ Listener müssen Acknowledgment-Parameter haben (manuelle Acknowledgment)
    @ArchTest
    static final ArchRule listeners_must_accept_acknowledgment =
        methods()
            .that().areAnnotatedWith(KafkaListener.class)
            .should().haveRawParameterTypes()
            .as("@KafkaListener-Methoden müssen Acknowledgment-Parameter akzeptieren");
}
```

### 35.2 Static-Analysis-Regeln (Semgrep)

```yaml
# semgrep-rules.yml
rules:
  - id: kafka-no-fixed-port-tests
    message: "KafkaContainer ohne versionierten Tag verwenden"
    severity: ERROR
    languages: [java]
    pattern-regex: 'KafkaContainer.*"latest"'

  - id: kafka-no-payload-logging
    message: "Vollständige Event-Payloads dürfen nicht geloggt werden"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: log.info("...{}", $EVENT)
      - metavariable-pattern:
          metavariable: $EVENT
          pattern-regex: '.*[Ee]vent|.*[Ee]nvelope|.*[Pp]ayload'

  - id: kafka-no-acknowledge-in-transactional
    message: "ack.acknowledge() darf nicht innerhalb @Transactional sein"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: |
          @Transactional
          ...
          public void $METHOD(..., Acknowledgment $ACK) {
            ...
            $ACK.acknowledge();
          }

  - id: kafka-no-random-partition-key
    message: "Random UUIDs als Partition Key zerstören Reihenfolge"
    severity: ERROR
    languages: [java]
    pattern-regex: 'kafkaTemplate.send\([^,]+,\s*UUID.randomUUID\(\)'

  - id: kafka-no-fire-and-forget
    message: "kafkaTemplate.send() ohne whenComplete ist Anti-Pattern"
    severity: WARNING
    languages: [java]
    patterns:
      - pattern: $TEMPLATE.send($TOPIC, $KEY, $VALUE);
      - pattern-not-inside: $TEMPLATE.send($TOPIC, $KEY, $VALUE).whenComplete(...)
      - pattern-not-inside: $TEMPLATE.send($TOPIC, $KEY, $VALUE).get(...)

  - id: schema-no-sensitive-fields
    message: "Schema enthält verbotenes sensibles Feld"
    severity: ERROR
    languages: [json]
    pattern-regex: '"(password|secret|token|api_key|apikey|authorization|iban)":'
```

### 35.3 CI-Gates

1. ArchUnit-Regeln in jedem Test-Run.
2. Semgrep-Regeln für Pull Requests.
3. Schema-Kompatibilitätsprüfung gegen Schema Registry.
4. Pact-Consumer-Tests in jeder Pipeline.
5. Pact-Provider-Verifikation (siehe QG-JAVA-019 v2 Sektion 11).
6. Coverage für Producer- und Consumer-Code.
7. Integrationstest mit Kafka Testcontainers für kritische Flows.

---

## 36. Ausnahmen

Ausnahmen sind zulässig, wenn:

1. Ein Event nur intern und temporär in einer Entwicklungsumgebung genutzt wird.
2. Ein Topic bewusst technische Telemetrie statt Fachereignisse transportiert.
3. Ein Consumer bewusst nicht idempotent sein muss, weil nachweislich keine Wiederholung schaden kann (selten zutreffend).
4. Ein Schema-Registry-System im Legacy-Cluster noch nicht vorhanden ist (mit Migrationsplan).
5. Ein sehr kleines internes System vorübergehend JSON ohne Registry nutzt.
6. Ein synchroner Use Case bewusst nicht auf Kafka migriert wird.
7. Eine Bestandsanwendung Spring Kafka 3.1.x verwendet, ohne sofortiges Upgrade.

Ausnahmen MÜSSEN dokumentiert werden:

- **Was weicht ab?**
- **Warum ist die Abweichung notwendig?**
- **Welche Risiken entstehen?**
- **Wie wird das Risiko begrenzt?** (Compensating Controls)
- **Wann wird die Ausnahme erneut geprüft?** (Review-Termin)
- **Wer hat die Ausnahme freigegeben?** (Architecture-Approval)

Bei produktiven fachlichen Events sind Owner, Idempotenz, Monitoring und Datenschutzprüfung **nicht optional**.

---

## 37. Definition of Done

Ein Kafka-basierter Event-Flow erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Das Event ist fachlich klar benannt (`<domain>.<event-name>.v<major-version>`).
2. Ein Owner für Topic und Schema ist benannt.
3. Ein versioniertes Schema existiert in der Schema Registry.
4. Schema Registry hat authentifizierten Zugriff (Basic Auth, OAuth oder mTLS).
5. Schema-Subject-Strategie (TopicNameStrategy als Default) ist bewusst gewählt.
6. Schema-Kompatibilität wird in CI geprüft.
7. Producer-Konfiguration ist zuverlässig (`acks=all`, `enable.idempotence=true`, `observation-enabled=true`).
8. Producer-Send wird mit `whenComplete` beobachtet — Logs und Metriken.
9. Ein fachlich korrekter Partition Key (Aggregat-ID) wird verwendet.
10. Kritische DB+Kafka-Flüsse sind über Outbox-Pattern oder Debezium CDC abgesichert.
11. Outbox-Dispatcher verwendet `SKIP LOCKED` und exponentielles Backoff.
12. Bei Read-Process-Write innerhalb Kafka: Transactional Producer wird verwendet.
13. Consumer sind idempotent über `processed_events`-Tabelle mit Tenant-Isolation, `TIMESTAMPTZ` und Partitionierung.
14. Consumer-`Acknowledge` erfolgt nach Transaction-Commit (zwei-Methoden-Pattern).
15. `enable-auto-commit=false` und `ack-mode: manual` sind gesetzt.
16. Retry erfolgt über `@RetryableTopic` (bevorzugt) oder `DefaultErrorHandler` mit DLT.
17. DLT hat Owner, Monitoring, Runbook und Reprocessing-Mechanismus.
18. Bei verteilten Transaktionen wird Saga-Pattern verwendet (choreographiert bevorzugt).
19. Tracing erfolgt über `observation-enabled: true` (kein manuelles Header-Setting).
20. Logs enthalten nur Metadaten, keine vollständigen Payloads, Tenant-IDs werden gehasht.
21. Tenant-Kontext ist im Event-Envelope enthalten und beim Consumer in den Thread-Kontext gesetzt (Cross-Reference: QG-JAVA-006 v2 Sektion 14.6).
22. Kafka-Cluster läuft in KRaft-Modus (Apache Kafka 3.7+).
23. Kafka-Authentication (SASL/SSL) und Kafka-ACLs sind konfiguriert.
24. Tests decken Producer, Consumer (mit Idempotenz), Integration mit Testcontainers, Schema-Kompatibilität und Messaging Contracts (Pact, siehe QG-JAVA-019 v2) ab.
25. Datenschutz und Retention sind bewertet.
26. Pull-Request-Review berücksichtigt die Checkliste aus Sektion 34.

---

## 38. Quellen und weiterführende Literatur

### Apache Kafka

* Apache Kafka Producer Configs: <https://kafka.apache.org/documentation/#producerconfigs>
* Apache Kafka Consumer Configs: <https://kafka.apache.org/documentation/#consumerconfigs>
* Apache Kafka KRaft mode: <https://kafka.apache.org/documentation/#kraft>
* Kafka Transactions: <https://kafka.apache.org/documentation/#transactions>

### Spring Kafka

* Spring Kafka Reference (aktuelle Version): <https://docs.spring.io/spring-kafka/reference/>
* Spring Kafka Error Handling: <https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html>
* Spring Kafka `@RetryableTopic`: <https://docs.spring.io/spring-kafka/reference/kafka/retrytopic.html>
* Spring Kafka Transactions: <https://docs.spring.io/spring-kafka/reference/kafka/transactions.html>
* Spring Kafka Observation: <https://docs.spring.io/spring-kafka/reference/kafka/micrometer.html>

### Schema Registry und Schemas

* Confluent Schema Registry — Schema Evolution and Compatibility: <https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html>
* Confluent Pattern — Schema Compatibility: <https://developer.confluent.io/patterns/event-stream/schema-compatibility/>
* Apache Avro Specification: <https://avro.apache.org/docs/current/specification/>
* Protocol Buffers (Protobuf): <https://protobuf.dev/>
* JSON Schema: <https://json-schema.org/>

### Outbox und CDC

* Chris Richardson — Transactional Outbox Pattern: <https://microservices.io/patterns/data/transactional-outbox.html>
* Debezium Outbox Event Router: <https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html>
* Debezium PostgreSQL Connector: <https://debezium.io/documentation/reference/stable/connectors/postgresql.html>

### Saga-Pattern

* Chris Richardson — Saga Pattern: <https://microservices.io/patterns/data/saga.html>
* Caitie McCaffrey — Distributed Sagas: <https://www.youtube.com/watch?v=0UTOLRTwOX0>

### Architektur und Konzepte

* Martin Fowler — What do you mean by „Event-Driven"?: <https://martinfowler.com/articles/201701-event-driven.html>
* Martin Fowler — CQRS: <https://martinfowler.com/bliki/CQRS.html>

### Security und Datenschutz

* OWASP Logging Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html>
* OWASP Secrets Management Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html>
* OWASP API Security Top 10: <https://owasp.org/API-Security/>

### Testing

* Testcontainers — Kafka Module: <https://java.testcontainers.org/modules/kafka/>
* Pact JVM — Message Pact: <https://docs.pact.io/getting_started/how_pact_works#non-http-testing-message-pact>

---

*Diese Richtlinie ersetzt die Erstfassung vom 2024-02-16 (v1.0). Für inhaltliche Querverweise siehe QG-JAVA-006 v2 (Spring-Boot-Serviceschicht), insbesondere Sektion 14.6 (Tenant-Context-Pattern). Für Messaging Contracts siehe QG-JAVA-019 v2 (Contract Testing) Sektion 14. Für Domänenmodellierung siehe QG-JAVA-008 (Objektorientierung). Für Feature Flags zur Producer-/Consumer-Steuerung siehe QG-JAVA-039-01 v2 (Statische Feature Flags). Für Containerisierung der Services siehe QG-JAVA-126 v2 (Docker).*
