# ADR-083 — iSAQB F3: Architekturmuster & Architekturstile

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Foundation · Architekturmuster                          |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| iSAQB-Lernziel    | LZ-3-1 bis LZ-3-8 (Foundation Level)                         |

---

## 1. Architekturmuster vs. Entwurfsmuster: Der Unterschied

```
ENTWURFSMUSTER (Design Patterns, GoF → ADR-028-030):
  → Betrifft: Klassen, Objekte, ihre Beziehungen
  → Maßstab: einige Klassen, ein Modul
  → Beispiele: Strategy, Observer, Factory, Decorator
  → Entscheidungsebene: Wie implementiere ich X?

ARCHITEKTURMUSTER (Architectural Patterns):
  → Betrifft: Systemstruktur, Komponenten-Topologie
  → Maßstab: das gesamte System oder große Teile davon
  → Beispiele: Schichten, Microservices, Event-Driven, Pipes-and-Filters
  → Entscheidungsebene: Wie organisiere ich das gesamte System?

ARCHITEKTURSTILE (Architectural Styles):
  → Noch abstrakter als Architekturmuster
  → Beschreibt Grundprinzipien: "wie kommunizieren Komponenten?"
  → Beispiele: REST, SOAP, Client-Server, Peer-to-Peer
  → Entscheidungsebene: Welches grundlegende Kommunikationsparadigma?
```

**Warum ist der Unterschied wichtig?**
Wer Entwurfsmuster für Architekturentscheidungen einsetzt (oder umgekehrt), löst das Problem auf der falschen Ebene. Ein Strategy-Pattern löst kein Skalierungsproblem. Eine Microservice-Entscheidung löst kein Klassen-Design-Problem.

---

## 2. Die sieben wichtigsten Architekturmuster (iSAQB Lehrplan)

### 2.1 Muster 1: Schichtenarchitektur (Layers)

**Grundprinzip:** Komponenten werden in horizontale Schichten gruppiert. Jede Schicht darf nur auf die direkt darunter liegende zugreifen.

```
         ┌──────────────────────────────────────────┐
         │           Präsentationsschicht           │  ← HTTP, REST, GraphQL
         │  (Controller, Views, API-Endpunkte)      │
         └─────────────────┬────────────────────────┘
                           │ darf zugreifen auf ↓
         ┌─────────────────▼────────────────────────┐
         │           Anwendungsschicht              │  ← Use Cases, Orchestrierung
         │  (Services, Application-Logik)           │
         └─────────────────┬────────────────────────┘
                           │ darf zugreifen auf ↓
         ┌─────────────────▼────────────────────────┐
         │             Domänenschicht               │  ← Business-Regeln
         │  (Entities, Value Objects, Domain Logic) │
         └─────────────────┬────────────────────────┘
                           │ darf zugreifen auf ↓
         ┌─────────────────▼────────────────────────┐
         │         Infrastrukturschicht             │  ← Datenbank, externe APIs
         │  (Repositories, Gateways, Messaging)     │
         └──────────────────────────────────────────┘
```

```java
// ❌ SCHICHT-VERLETZUNG — Präsentation greift direkt auf Infrastruktur zu
@RestController
public class OrderController {
    @Autowired
    private JdbcTemplate jdbc;   // Infrastruktur direkt in Präsentation!

    @GetMapping("/orders/{id}")
    public Order findOrder(@PathVariable Long id) {
        return jdbc.queryForObject(   // SQL in Controller = katastrophal für Wartbarkeit
            "SELECT * FROM orders WHERE id = ?",
            new OrderRowMapper(), id);
    }
}
// Problem: Controller kennt Datenbankstruktur. Wenn Tabelle umbenannt wird,
// bricht die API. Wenn DB gewechselt wird, muss Controller geändert werden.
// Kein Test ohne echte DB möglich.

// ✅ SCHICHTEN KORREKT — jede Schicht kennt nur ihre direkte Nachbarin
@RestController                          // Präsentationsschicht
public class OrderController {
    private final OrderQueryService orderService;  // → Anwendungsschicht

    @GetMapping("/orders/{id}")
    public OrderDetailDto findOrder(@PathVariable Long id) {
        return orderService.findById(new OrderId(id));   // DTO, keine Entity
    }
}

@Service                                 // Anwendungsschicht
public class OrderQueryService {
    private final OrderRepository repository;   // → Port (Domänenschicht)

    public OrderDetailDto findById(OrderId id) {
        return repository.findById(id)
            .map(orderMapper::toDetailDto)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
}

// Repository-Interface in Domänenschicht, Implementierung in Infrastruktur
public interface OrderRepository {       // Domänenschicht: Interface (Port)
    Optional<Order> findById(OrderId id);
}

@Repository                              // Infrastrukturschicht: JPA-Adapter
public class JpaOrderRepository implements OrderRepository {
    public Optional<Order> findById(OrderId id) { ... }
}
```

**Qualitätsziele:**
- ✅ Wartbarkeit: Schichten sind austauschbar (DB wechseln → nur Infrastruktur)
- ✅ Testbarkeit: jede Schicht isoliert testbar
- ⚠️ Performance: jeder Aufruf durchläuft alle Schichten (konfigurierbar)

**ArchUnit-Regel für Schichten:**

```java
@ArchTest
static final ArchRule schichtenregel =
    layeredArchitecture()
        .consideringAllDependencies()
        .layer("Presentation").definedBy("..adapter.rest..")
        .layer("Application") .definedBy("..application..")
        .layer("Domain")      .definedBy("..domain..")
        .layer("Infrastructure").definedBy("..adapter.persistence..")
        .whereLayer("Presentation") .mayOnlyAccessLayers("Application")
        .whereLayer("Application")  .mayOnlyAccessLayers("Domain")
        .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain")
        .because("ADR-083: Schichtenarchitektur — nur abwärts");
```

---

### 2.2 Muster 2: Ports & Adapters (Hexagonale Architektur) → ADR-031

**Warum ist das mehr als nur Schichten?**

```
Schichten-Problem: Domäne hängt von Infrastruktur ab
  Domäne → Repository (JPA-Interface) → JPA-Klassen
  → Domäne kennt JPA! Kein Wechsel ohne Domänenänderungen.

Hexagonal löst das durch Dependency Inversion:
  Domäne definiert PORT (Interface): "Ich brauche etwas das Order speichern kann"
  Infrastruktur liefert ADAPTER: "JpaOrderRepository implementiert diesen Port"
  → Domäne kennt KEIN JPA, KEIN Spring, KEIN Framework
  → Domäne ist komplett isoliert und in Millisekunden testbar

              Externe Welt           Domäne            Externe Welt
             (Treibend)                               (Getrieben)
         ┌───────────────┐       ┌──────────┐       ┌───────────────┐
         │ HTTP-Request  │──────▶│          │──────▶│ PostgreSQL    │
         │ REST Controller│      │  DOMAIN  │       │ (JPA Adapter) │
         ├───────────────┤       │          │       ├───────────────┤
         │ Kafka Consumer│──────▶│  LOGIK   │──────▶│ Kafka Producer│
         │ (Event-driven)│       │          │       │ (Event Adapter│
         ├───────────────┤       │          │       ├───────────────┤
         │ CLI-Command   │──────▶│          │──────▶│ Stripe API    │
         └───────────────┘       └──────────┘       └───────────────┘
           Inbound Adapters      Ports (Interfaces)  Outbound Adapters
```

---

### 2.3 Muster 3: Event-Driven Architecture (EDA)

**Grundprinzip:** Komponenten kommunizieren durch Ereignisse. Keine direkte Abhängigkeit zwischen Sender und Empfänger.

```
DIREKTE KOMMUNIKATION (Probleme):
  OrderService ──[REST-Call]──▶ InventoryService
  OrderService ──[REST-Call]──▶ NotificationService
  OrderService ──[REST-Call]──▶ BillingService

  Problem: OrderService kennt alle anderen Services
  Problem: Ausfall eines Services = Ausfall des OrderService
  Problem: Neue Services brauchen Änderung am OrderService

EVENT-DRIVEN (Lösung):
  OrderService ──[OrderPlacedEvent]──▶ Kafka-Topic
                                            │
                          ┌─────────────────┼─────────────────┐
                          ▼                 ▼                 ▼
                  InventoryService  NotificationService  BillingService

  Vorteil: OrderService kennt niemanden außer Kafka
  Vorteil: Neuer Service? Einfach Event konsumieren — kein Change am OrderService
  Vorteil: Ausfall eines Consumers stoppt OrderService nicht
```

```java
// ❌ SCHLECHT — Direkte Kopplung (Publisher kennt alle Subscriber)
@Service
public class OrderService {
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final BillingService billingService;
    // 3 direkte Abhängigkeiten — neue Funktion = Änderung hier

    public void placeOrder(PlaceOrderCommand cmd) {
        var order = createOrder(cmd);
        inventoryService.reserve(order.items());    // synchron, koppelt Latenz
        notificationService.sendConfirmation(order); // synchron
        billingService.createInvoice(order);          // synchron
        // Wenn billing down: Bestellung schlägt fehl!
    }
}

// ✅ GUT — Event-Driven (Publisher kennt niemanden)
@Service
public class OrderService {
    private final ApplicationEventPublisher events;
    // Eine Abhängigkeit: den Event-Bus (Kafka/Spring Events)

    @Transactional
    public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
        var order = createAndSaveOrder(cmd);
        events.publishEvent(OrderPlacedEvent.from(order));
        return OrderCreatedResult.from(order);
        // Bestellung erfolgreich — egal ob billing gerade down ist!
    }
}

// Subscriber sind vollständig unabhängig
@ApplicationModuleListener // → ADR-079 Spring Modulith
void onOrderPlaced(OrderPlacedEvent event) {
    inventoryService.reserve(event.orderId(), event.items());
}
```

**Qualitätsziele:**
- ✅ Verfügbarkeit: Ausfall eines Consumers stoppt den Publisher nicht
- ✅ Skalierbarkeit: Consumer-Gruppen skalieren unabhängig
- ✅ Erweiterbarkeit: neue Consumer ohne Änderung am Producer
- ⚠️ Konsistenz: Eventual Consistency statt starker Konsistenz
- ⚠️ Komplexität: Debugging von Event-Flows ist schwieriger

---

### 2.4 Muster 4: Pipes and Filters

**Grundprinzip:** Daten fließen durch eine Kette von unabhängigen Transformationsschritten (Filter).

```
Input ──▶ [Filter 1] ──▶ [Filter 2] ──▶ [Filter 3] ──▶ Output
         Validierung   Transformation  Anreicherung

Java 8 Streams sind das bekannteste Beispiel:
```

```java
// ❌ SCHLECHT — Monolithische Verarbeitungslogik
public List<OrderSummary> processOrders(List<RawOrder> rawOrders) {
    List<OrderSummary> result = new ArrayList<>();
    for (RawOrder raw : rawOrders) {
        // Validierung, Transformation, Filterung, Anreicherung — alles zusammen
        if (raw.amount() > 0 && raw.customerId() != null) {
            var customer = customerRepo.findById(raw.customerId());
            if (customer.isPresent() && customer.get().isActive()) {
                var product = productRepo.findById(raw.productId());
                if (product.isPresent()) {
                    result.add(new OrderSummary(
                        raw.id(),
                        customer.get().name(),
                        product.get().name(),
                        raw.amount() * product.get().price()
                    ));
                }
            }
        }
    }
    return result;
}
// Problem: alles in einer Methode, kein Testen einzelner Schritte möglich

// ✅ GUT — Pipes and Filters (Stream-Ansatz)
public List<OrderSummary> processOrders(List<RawOrder> rawOrders) {
    return rawOrders.stream()
        .filter(this::isValidOrder)           // Filter 1: Validierung
        .filter(this::hasActiveCustomer)      // Filter 2: Kundenstatus
        .map(this::enrichWithProduct)          // Transformation: Produktdaten
        .map(this::calculateTotal)             // Transformation: Berechnung
        .collect(toList());
}
// Jeder Schritt ist:
// ① Isoliert testbar (jede Methode einzeln testen)
// ② Leicht austauschbar (neuer Filter? einfach einfügen)
// ③ Leicht verständlich (Sequenz liest sich wie Beschreibung)
```

**Qualitätsziele:**
- ✅ Wartbarkeit: jeder Filter ist unabhängig modifizierbar
- ✅ Wiederverwendbarkeit: Filter in anderen Pipes nutzbar
- ✅ Testbarkeit: jeden Filter isoliert testen
- ⚠️ Performance: bei großen Datenmengen stream()-Overhead beachten

---

### 2.5 Muster 5: Repository (Datenzugriffsabstraktion)

```java
// ❌ SCHLECHT — Datenbankzugriff überall im Code verstreut
@Service
public class OrderService {
    @Autowired JdbcTemplate jdbc;

    public Order findOrder(Long id) {
        return jdbc.queryForObject("SELECT * FROM orders WHERE id = ?", ..., id);
        // Jeder Service kennt das SQL-Schema
        // Schema ändert sich → alle Services müssen geändert werden
    }
}

// ✅ GUT — Repository-Muster kapselt Datenzugriff
public interface OrderRepository {              // Domain-Interface
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomer(CustomerId id);
    void save(Order order);
}

@Repository                                     // Infrastruktur-Adapter
class JpaOrderRepository implements OrderRepository {
    // SQL/JPA-Details hier — Domain-Service kennt kein SQL
    public Optional<Order> findById(OrderId id) { ... }
}

// Test: InMemoryRepository statt JPA
class InMemoryOrderRepository implements OrderRepository {
    private final Map<OrderId, Order> store = new HashMap<>();
    public Optional<Order> findById(OrderId id) { return Optional.ofNullable(store.get(id)); }
}
// → Domänenservice-Tests laufen in Millisekunden ohne DB
```

---

### 2.6 Muster 6: CQRS (Command Query Responsibility Segregation) → ADR-032

```
LESEND und SCHREIBEND haben fundamental verschiedene Anforderungen:

SCHREIBEN:
  → Braucht: Validierung, Transaktionen, Domänenregeln
  → Datenmodell: normalisiert, konsistent
  → Optimiert für: Integrität

LESEN:
  → Braucht: Performance, flache Projektionen, Paginierung
  → Datenmodell: denormalisiert, query-optimiert
  → Optimiert für: Geschwindigkeit

CQRS trennt beides:
  Command-Seite: create/update/delete → Domänenmodell → normalisierte DB
  Query-Seite:   read → optimierte Projektionen → denormalisierte Sichten

Warum wichtig für Qualitätsziele?
  → QS-P01 (Latenz ≤ 500ms): Lese-Optimierung ohne Schreib-Kompromisse
  → QS-W01 (Wartbarkeit): Read-Model unabhängig vom Write-Model änderbar
```

---

### 2.7 Muster 7: Microservices vs. Modulith → ADR-077

```
MICROSERVICES:
  Deployment-Einheit:  separate Services (je ein Container)
  Kommunikation:       Netzwerk (REST, gRPC, Kafka)
  Datenhaltung:        Database-per-Service
  Treiber:             Team-Autonomie, unabhängige Skalierung
  Kosten:              Netzwerk-Overhead, verteilte Komplexität

MODULITH (→ ADR-079):
  Deployment-Einheit:  ein Artefakt, mehrere logische Module
  Kommunikation:       direkte Methodenaufrufe + interne Events
  Datenhaltung:        Schema-per-Modul in einer DB
  Treiber:             Team-Größe < 25, einfacher Betrieb
  Kosten:              Migrations-Komplexität bei sehr großem Wachstum

WAHL-KRITERIUM: Conway's Law (→ ADR-077)
  Architektur folgt Kommunikationsstruktur des Teams.
  Team mit 8 Personen → Modulith
  Team mit 50+ Personen in 5+ autonomen Teams → Microservices
```

---

## 3. Architekturstile: Die Kommunikationsebene

```
REQUEST-RESPONSE (synchron):
  Komponente A wartet auf Antwort von B
  Beispiele: REST, gRPC, GraphQL
  Wann: Sofortige Antwort für Benutzer nötig
  Problem: Latenz koppelt sich (wenn B langsam, wird A langsam)

EVENT-DRIVEN (asynchron):
  Komponente A sendet Event, wartet nicht
  Beispiele: Kafka, AMQP, Spring Events
  Wann: Entkopplung von Komponenten, hohe Skalierung
  Problem: Eventual Consistency, komplexes Debugging

SHARED DATABASE:
  Komponenten teilen eine Datenbank direkt
  Wann: Selten sinnvoll, nur für eng verwandte Module
  Problem: Koppelung durch Schema, kein unabhängiges Deployment

SHARED NOTHING:
  Jede Komponente hat eigene Ressourcen
  Wann: Maximale Isolation nötig (Compliance, Mandantenfähigkeit)
  Problem: Komplexe Datenkonsistenz (→ ADR-065 Saga)
```

---

## 4. Muster-Auswahl anhand von Qualitätszielen

```
Qualitätsziel-zu-Muster-Mapping:

WENN: Performance ist kritisch (QS-P01)
DANN: CQRS (Lese-Optimierung), Caching, asynchrone Verarbeitung

WENN: Verfügbarkeit ist kritisch (QS-V01)
DANN: Event-Driven, Circuit Breaker, Redundanz

WENN: Wartbarkeit ist kritisch (QS-W01)
DANN: Hexagonale Architektur, Modulith, klare Schichten

WENN: Sicherheit ist kritisch (QS-S01)
DANN: Ports & Adapters (Security-Adapter), Zero Trust (→ ADR-070)

WENN: Team-Autonomie > Konsistenz
DANN: Microservices, Database-per-Service

WENN: Einfachheit > Autonomie (und Team < 25)
DANN: Modulith, Shared Database mit Schema-Trennung
```

---

## 5. Anti-Patterns: Falsch eingesetzte Architekturmuster

```
ANTI-PATTERN 1: Distributed Monolith
  Was: Services die wie Microservices aussehen, aber synchron voneinander abhängen
  Symptom: Service A kann nicht ohne Service B deployed werden
  Ursache: Microservices eingeführt ohne Team-Autonomie oder klare Grenzen
  Lösung: Entweder wirklich entkoppeln (Events, eigene DBs) oder Modulith

ANTI-PATTERN 2: Anemic Domain Model in Schichtenarchitektur
  Was: Schichten vorhanden, aber Domain-Schicht nur Datenbehälter ohne Logik
  Symptom: Alle Business-Logik im Service-Layer
  Ursache: Schichten als Namenskonvention, nicht als Designprinzip
  Lösung: Rich Domain Model (→ ADR-008 OOP, → ADR-023 DDD)

ANTI-PATTERN 3: Leaky Abstraction
  Was: Interna einer Schicht sickern in andere Schichten
  Symptom: Controller kennt SQL, Service kennt HTTP-Status-Codes
  Ursache: Fehlende Interface-Definition an Schichtgrenzen
  Lösung: Strikte Interface-Trennung (→ ADR-031 Ports)

ANTI-PATTERN 4: God Service
  Was: Ein Service macht alles in einem Microservice-System
  Symptom: 80% der API-Calls gehen an denselben Service
  Ursache: Bounded Contexts nicht korrekt identifiziert (→ ADR-023 DDD)
  Lösung: Domain-getriebene Service-Abgrenzung nach Bounded Contexts
```

---

## 6. Validierung: Richtiges Muster für das richtige Problem

```
CHECKLISTE vor Muster-Auswahl:

□ Welche Qualitätsziele muss dieses Muster erfüllen? (→ ADR-082)
□ Welche Qualitätsziele opfert dieses Muster? (Trade-offs sind unvermeidlich)
□ Sind die Treiber für dieses Muster in unserem Kontext vorhanden?
  (Event-Driven braucht Eventual-Consistency-Toleranz)
□ Hat das Team die nötige Erfahrung?
  (Kafka-basierte EDA ohne Kafka-Erfahrung = operativer Alptraum)
□ Gibt es ein ArchUnit-Test das die Struktur durchsetzt?
  (ungeprüfte Architektur = implizite Architektur nach 3 Monaten)
□ Gibt es ein konkretes Qualitätsszenario das dieses Muster löst?
  (kein Muster ohne Szenario = Overengineering)
```

---

## 7. Quellen & Referenzen

- **iSAQB CPSA-F Curriculum (2023), LZ-3-1 bis LZ-3-8** — Architekturmuster als Kern des Foundation-Lehrplans.
- **Frank Buschmann et al., "Pattern-Oriented Software Architecture" Vol. 1 (1996)** — Pipes and Filters, Layers, Broker als klassische Architekturmuster.
- **Eric Evans, "Domain-Driven Design" (2003)** — Repository, Aggregate als domänengetriebene Architekturmuster.
- **Gregor Hohpe, Bobby Woolf, "Enterprise Integration Patterns" (2003)** — Event-Driven Architecture, Message Channels.
- **Sam Newman, "Building Microservices" (2022)** — Microservices als Architekturmuster; Abgrenzung zu Modulith.
- **Martin Fowler, "Patterns of Enterprise Application Architecture" (2002)** — Repository, Service Layer, CQRS-Grundlagen.

---

## Akzeptanzkriterien

- [ ] Für jedes verwendete Architekturmuster existiert eine ADR-Referenz mit Begründung
- [ ] ArchUnit-Regeln prüfen das gewählte Muster maschinell (→ ADR-061)
- [ ] Jedes Muster ist mit dem Qualitätsziel verknüpft das es adressiert (→ ADR-082)
- [ ] Anti-Patterns sind durch ArchUnit-Regeln explizit verboten
- [ ] Onboarding-Dokument erklärt welches Muster wo und warum eingesetzt wird

---

## Verwandte ADRs

- [ADR-082](ADR-082-isaqb-qualitaetsziele.md) — Qualitätsziele (Voraussetzung)
- [ADR-031](ADR-031-hexagonal-architecture.md) — Ports & Adapters (Muster 2) vollständig
- [ADR-032](ADR-032-cqrs.md) — CQRS (Muster 6) vollständig
- [ADR-041](ADR-041-event-driven-kafka.md) — Event-Driven Architecture (Muster 3) vollständig
- [ADR-079](ADR-079-modulith-ausfuehrlich.md) — Modulith (Muster 7) vollständig
- [ADR-084](ADR-084-isaqb-entwurfsprinzipien.md) — Entwurfsprinzipien (iSAQB F4)
