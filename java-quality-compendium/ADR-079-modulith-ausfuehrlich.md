# ADR-079 — Modularer Monolith (Modulith): Architektur, Grenzen & Migrationsstrategie

| Feld              | Wert                                              |
|-------------------|---------------------------------------------------|
| Status            | ✅ Akzeptiert                                     |
| Entscheider       | CTO / Architektur-Board                           |
| Datum             | 2024-01-01                                        |
| Review-Datum      | 2025-07-01 (6 Monate — Treiber prüfen)            |
| Kategorie         | Architektur-Strategie / Deployment-Modell         |
| Betroffene Teams  | Alle Engineering-Teams, DevOps, Product           |
| Supersedes        | — (ersetzt keine bestehende Entscheidung)         |
| Abhängigkeiten    | ADR-023 (DDD), ADR-031 (Hexagonal), ADR-032 (CQRS), ADR-041 (Kafka), ADR-056 (Spring Modulith) |

---

## 1. Kontext & Treiber

### 1.1 Die Ausgangssituation

Wir befinden uns an einem Entscheidungspunkt den jedes wachsende Engineering-Team kennt: das bestehende System ist zu groß für einen reinen Monolithen und noch zu klein für den vollen Microservice-Overhead. Die falsche Entscheidung in dieser Phase ist teuer — in beide Richtungen.

**Teamgröße zum Entscheidungszeitpunkt:** 8–25 Entwickler, 3–5 Domänenteams  
**Systemalter:** 2–4 Jahre  
**Deployment-Frequenz:** 2–5 Deployments/Woche  
**Incident-Rate:** 2–4 P2/P3 Incidents/Monat durch Deployment-Abhängigkeiten

### 1.2 Konkrete Schmerzpunkte des Status Quo

Der aktuelle "Monolith ohne Grenzen" (Big Ball of Mud) erzeugt folgende messbare Probleme:

```
Problem 1: Deployment-Kopplung
  Symptom: Team A will Feature X deployen, blockiert weil Team B
           gerade einen Bug in derselben Codebasis hat.
  Häufigkeit: 2–3× pro Woche
  Kosten: ~4 Stunden Verzögerung × 3 Entwickler = 12h/Woche = ~50h/Monat

Problem 2: Unbeabsichtigte Kopplung
  Symptom: Änderung in OrderService bricht UserService-Tests.
  Ursache: Direkte Import-Abhängigkeiten ohne Schichtgrenzen.
  Häufigkeit: ~6 Build-Breaks/Woche die kein Modul-Experte erwartet hat

Problem 3: Test-Laufzeit
  Symptom: Vollständige Test-Suite dauert 18 Minuten.
  Ursache: Kein isoliertes Testen einzelner Module möglich.
  Kosten: 18 Min × 20 Commits/Tag × 5 Entwickler = 30 Stunden verlorene Zeit/Woche

Problem 4: Onboarding-Komplexität
  Symptom: Neue Entwickler brauchen 6–8 Wochen um selbstständig Features zu liefern.
  Ursache: Keine definierten Modul-APIs, globales Kontextwissen notwendig.
```

### 1.3 Warum NICHT sofort Microservices?

Die Microservice-Option wurde im Architektur-Board diskutiert und aus folgenden Gründen zurückgestellt:

**Team-Maturity-Argument:** Laut DORA State of DevOps 2019/2023 verbessern Microservices die Delivery-Performance nur bei Teams die bereits hohe Deployment-Reife haben (Elite-Performer). Low- und Medium-Performer werden durch Microservices schlechter — die verteilte Komplexität übersteigt den Nutzen.

**Conway's Law:** Unsere aktuelle Team-Kommunikationsstruktur hat noch keine scharfen Grenzen. Microservice-Grenzen die gegen die Kommunikationsstruktur laufen, erzeugen Distributed Monoliths — das schlimmste beider Welten.

**Konkrete Kosten:** Eine vollständige Microservice-Migration bei ~200 KLOC würde nach internen Schätzungen 12–18 Monate Migrationsaufwand bedeuten bei gleichzeitig reduzierter Feature-Velocity. Die Business-Kosten rechtfertigen das nicht.

---

## 2. Entscheidung

Wir migrieren den bestehenden Monolithen schrittweise zu einem **Modulith**: einem einzelnen Deployment-Artefakt mit klar durchgesetzten, maschinell verifizierbaren Modul-Grenzen.

**Die Kerneigenschaften des Modulith:**
- Ein Deployment-Artefakt (ein JAR/Container)
- Mehrere logische Module mit expliziten, vertragsbasierten APIs
- Modul-interne Implementierungen sind für andere Module unsichtbar
- Modul-übergreifende Kommunikation ausschließlich über definierte Ports und Events
- Datenbank-Schemas sind pro Modul getrennt (kein Schema-Sharing)
- Vollständige maschinelle Verifikation der Grenzen durch ArchUnit und Spring Modulith Verify

**Was ein Modulith ausdrücklich NICHT ist:**
- Ein Monolith mit besserer Package-Struktur aber ohne durchgesetzte Grenzen
- Ein Stepping-Stone-Architektur die explizit auf Microservices vorbereitet (das ist ein Nebenprodukt, kein Ziel)
- Eine Lösung für Teams die bereits Microservice-Maturity haben

---

## 3. Detaillierte Architektur

### 3.1 Modul-Taxonomie und Abhängigkeitsregeln

```
Erlaubte Abhängigkeitsrichtungen (Pfeile = darf abhängen von):

  ┌─────────────────────────────────────────────────────────────┐
  │                    Bootstrap / Application                   │
  │     (assembliert alle Module, kein eigener Business-Code)   │
  └──────────┬──────────┬──────────┬──────────┬────────────────┘
             │          │          │          │
             ▼          ▼          ▼          ▼
    ┌────────────┐ ┌─────────┐ ┌────────┐ ┌──────────────┐
    │   Orders   │ │Inventory│ │Payment │ │Notification  │
    │  (Modul)   │ │ (Modul) │ │(Modul) │ │  (Modul)     │
    └─────┬──────┘ └────┬────┘ └───┬────┘ └──────────────┘
          │              │          │          ▲
          └──────────────┴──────────┘          │
                         │                     │ (Events)
                         ▼                     │
               ┌──────────────────┐            │
               │  Shared Kernel   │            │
               │ (Value Objects,  │────────────┘
               │  Domain Events,  │
               │  Common Ports)   │
               └──────────────────┘

VERBOTEN (erzwingt ArchUnit):
  ✗ Orders → Payment (direkte Abhängigkeit auf andere Domänen)
  ✗ Notification → Orders.internal (interne Implementierung)
  ✗ Shared Kernel → Orders (Shared Kernel kennt keine Domänen)
```

### 3.2 Paketstruktur-Konvention

```
com.example.
│
├── bootstrap/                          ← Nur: Spring Boot App, Konfiguration
│   ├── ECommerceApplication.java
│   └── config/
│       ├── SecurityConfig.java
│       └── AsyncConfig.java
│
├── orders/                             ← Domänen-Modul "Orders"
│   │
│   ├── api/                            ← Öffentliche API dieses Moduls
│   │   ├── OrderApi.java               ← Interface: was andere Module aufrufen dürfen
│   │   ├── dto/
│   │   │   ├── CreateOrderCommand.java ← Eingehende Commands
│   │   │   └── OrderSummaryDto.java    ← Ausgehende Daten (keine Entity!)
│   │   └── events/
│   │       ├── OrderPlacedEvent.java   ← Ausgehende Domain Events
│   │       └── OrderCancelledEvent.java
│   │
│   ├── internal/                       ← Private Implementierung — UNSICHTBAR nach außen
│   │   ├── domain/
│   │   │   ├── Order.java              ← Aggregate Root (→ ADR-023)
│   │   │   ├── OrderItem.java          ← Value Object
│   │   │   └── OrderStatus.java        ← Enum
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   └── PlaceOrderUseCase.java
│   │   │   └── out/
│   │   │       └── OrderPersistencePort.java
│   │   ├── service/
│   │   │   └── OrderDomainService.java
│   │   ├── adapter/
│   │   │   ├── rest/
│   │   │   │   └── OrderController.java
│   │   │   └── persistence/
│   │   │       ├── OrderJpaRepository.java
│   │   │       └── OrderEntity.java     ← JPA Entity gehört ins internal!
│   │   └── OrdersModuleConfiguration.java
│   │
│   └── package-info.java               ← Spring Modulith @ApplicationModule
│
├── inventory/                          ← Domänen-Modul "Inventory"
│   ├── api/
│   │   ├── InventoryApi.java
│   │   └── events/
│   │       └── StockReservedEvent.java
│   └── internal/
│       └── ...
│
├── shared-kernel/                      ← Geteilte Primitive (kein Business-Code!)
│   ├── domain/
│   │   ├── Money.java                  ← Value Object
│   │   ├── OrderId.java                ← Typed ID
│   │   └── UserId.java
│   └── events/
│       └── DomainEvent.java            ← Basis-Interface für alle Events
│
└── notification/                       ← Modul ohne eigene Domain-API
    └── internal/
        ├── NotificationEventHandler.java
        └── EmailService.java
```

### 3.3 Spring Modulith Konfiguration

```java
// orders/package-info.java
/**
 * Orders-Modul: Kerndomäne Bestellverwaltung.
 *
 * Öffentliche API: {@link com.example.orders.api.OrderApi}
 * Publizierte Events: {@link com.example.orders.api.events.OrderPlacedEvent}
 *
 * @see <a href="../../docs/adr/ADR-079-modulith.md">ADR-079: Modulith</a>
 */
@ApplicationModule(
    displayName = "Orders",
    // Nur inventory ist als direkter Aufruf erlaubt (für Bestandsprüfung)
    // Alle anderen: nur über Events
    allowedDependencies = {
        "inventory :: api",
        "shared-kernel"
    }
)
package com.example.orders;

import org.springframework.modulith.ApplicationModule;
```

```java
// inventory/package-info.java
@ApplicationModule(
    displayName = "Inventory",
    allowedDependencies = { "shared-kernel" }
    // inventory darf NICHT auf orders zugreifen → kein Kreise
)
package com.example.inventory;
```

```java
// notification/package-info.java
@ApplicationModule(
    displayName = "Notification",
    allowedDependencies = { "shared-kernel" }
    // Notification kennt keine anderen Module — reagiert nur auf Events
)
package com.example.notification;
```

---

## 4. Modul-Kommunikation: Die drei Patterns

### Pattern A — Synchroner Aufruf über öffentliche API (selten)

Nur wenn sofortige Antwort zwingend erforderlich ist und Eventual Consistency nicht akzeptabel.

```java
// ❌ SCHLECHT: direkte Abhängigkeit auf interne Klasse
// (Spring Modulith und ArchUnit verbieten das)
import com.example.inventory.internal.domain.Stock; // VERBOTEN

@Service
public class OrderDomainService {
    // Direkter Import auf internes Inventory-Objekt → hartes Coupling
    private final StockRepository stockRepository; // VERBOTEN
}

// ✅ GUT: Abhängigkeit auf die öffentliche API des Inventory-Moduls
import com.example.inventory.api.InventoryApi; // NUR api-Package!
import com.example.inventory.api.dto.StockCheckResult;

@Service
public class OrderDomainService {

    private final InventoryApi inventoryApi; // Interface aus dem api-Package

    public void placeOrder(PlaceOrderCommand command) {
        // Synchroner Aufruf — nur für Verfügbarkeits-Check nötig
        var stockResult = inventoryApi.checkAvailability(
            command.items().stream()
                .map(i -> new StockCheckRequest(i.productId(), i.quantity()))
                .toList()
        );

        if (!stockResult.allAvailable()) {
            throw new InsufficientStockException(stockResult.unavailableItems());
        }

        // Erst jetzt: Order anlegen
        var order = Order.place(command);
        orderPersistencePort.save(order);

        // Event publizieren → Inventory reagiert asynchron auf Reservierung
        eventPublisher.publishEvent(new OrderPlacedEvent(
            order.id(),
            command.items(),
            order.total()
        ));
    }
}
```

### Pattern B — Asynchrone Event-Kommunikation (bevorzugt)

```java
// Orders-Modul publiziert ein Event
// Inventory-Modul reagiert darauf — ohne direktes Wissen voneinander

// Im orders-Modul:
public record OrderPlacedEvent(
    OrderId    orderId,
    UserId     customerId,
    List<OrderItemData> items,
    Money      total,
    Instant    occurredAt
) implements DomainEvent {
    public OrderPlacedEvent {
        Objects.requireNonNull(orderId,     "orderId");
        Objects.requireNonNull(customerId,  "customerId");
        Objects.requireNonNull(items,       "items");
        Objects.requireNonNull(total,       "total");
        Objects.requireNonNull(occurredAt,  "occurredAt");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty");
    }
}

// Im orders-Modul: Event publizieren
@Service
class OrderDomainService {

    private final ApplicationEventPublisher events;

    @Transactional
    public OrderCreatedResult placeOrder(PlaceOrderCommand command) {
        var order = Order.place(command);
        orderPersistencePort.save(order);

        // Spring Modulith: Event wird erst nach DB-Commit gefeuert
        // → Kein inkonsistenter Zustand wenn Event-Handler fehlschlägt
        events.publishEvent(OrderPlacedEvent.from(order));

        return new OrderCreatedResult(order.id(), order.total());
    }
}

// Im inventory-Modul: Event konsumieren
// (Inventory kennt OrderDomainService NICHT — nur das Event aus shared-kernel/api)
@Component
class InventoryEventHandler {

    private final StockReservationService reservationService;

    // @ApplicationModuleListener = transaktionaler Event-Listener mit Retry
    // → Spring Modulith verarbeitet das Event in eigener Transaktion
    // → Bei Fehler: Retry, dann Dead Letter (konfigurierbar)
    @ApplicationModuleListener
    void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Reserving stock for order {}", event.orderId());

        try {
            reservationService.reserve(
                event.orderId(),
                event.items().stream()
                    .map(i -> new ReservationRequest(i.productId(), i.quantity()))
                    .toList()
            );
        } catch (InsufficientStockException e) {
            // Kompensation: Order informieren dass Bestand fehlt
            events.publishEvent(new StockReservationFailedEvent(
                event.orderId(),
                e.unavailableItems()
            ));
        }
    }
}
```

### Pattern C — Modul-interne Transaktionsgrenzen

```java
// Wichtig: @Transactional darf NICHT Modulgrenzen überschreiten
// Ein @Transactional-Block = ein Modul

// ❌ SCHLECHT: Transaktion überspannt zwei Module (unmöglich bei Microservices,
//             aber im Modulith verlockend und falsch)
@Transactional
public void placeOrderAndReserveStock(PlaceOrderCommand cmd) {
    var order = orderService.create(cmd);       // Orders-Modul
    inventoryService.reserve(order.items());    // Inventory-Modul
    // Problem: wenn inventoryService.reserve() fehlschlägt,
    // wird orderService.create() zurückgerollt — das klingt gut,
    // aber es macht den Modulith zu einem "Distributed Monolith"
    // sobald wir ein Modul extrahieren wollen!
}

// ✅ GUT: Jedes Modul hat seine eigene Transaktionsgrenze
// Orders-Modul: eigene Transaktion
@Transactional
public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
    var order = Order.place(cmd);
    orderPersistencePort.save(order);          // Nur Orders-DB
    events.publishEvent(OrderPlacedEvent.from(order));
    return new OrderCreatedResult(order.id());
    // Transaktion endet hier → Event wird gefeuert
}

// Inventory-Modul: eigene Transaktion (ausgelöst durch Event)
@ApplicationModuleListener  // = eigene Transaktion
void onOrderPlaced(OrderPlacedEvent event) {
    reservationService.reserve(event.orderId(), event.items()); // Nur Inventory-DB
}
// → Beide Transaktionen sind unabhängig → sofort Microservice-fähig
```

---

## 5. Datenbank-Isolation: Schema-per-Modul

### 5.1 Warum Schema-Trennung im Modulith?

```
Ohne Schema-Trennung: JOIN über Modulgrenzen → unmögliche Service-Extraktion später
Mit Schema-Trennung:  Jedes Modul hat eigene Tabellen → Service-Extraktion in 1–2 Wochen

PostgreSQL Schemas (nicht zu verwechseln mit Datenbanken!):
- orders.*      → Tabellen des Orders-Moduls
- inventory.*   → Tabellen des Inventory-Moduls
- notification.*→ Tabellen des Notification-Moduls
- public.*      → Nur: Spring-Modulith-Event-Log, Flyway-History, ShedLock

Alle Schemas in EINER PostgreSQL-Instanz → ein DB-Connection-Pool
```

### 5.2 Flyway: Migrations-Trennung

```sql
-- src/main/resources/db/migration/orders/V1__create_orders_schema.sql
CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE orders.orders (
    id           BIGSERIAL    PRIMARY KEY,
    customer_id  BIGINT       NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    currency     VARCHAR(3)   NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE orders.order_items (
    id         BIGSERIAL    PRIMARY KEY,
    order_id   BIGINT       NOT NULL REFERENCES orders.orders(id),
    product_id VARCHAR(50)  NOT NULL,
    quantity   INT          NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(15,2) NOT NULL
);

CREATE INDEX idx_orders_customer ON orders.orders(customer_id);
CREATE INDEX idx_orders_status   ON orders.orders(status)
    WHERE status NOT IN ('DELIVERED', 'CANCELLED');
```

```sql
-- src/main/resources/db/migration/inventory/V1__create_inventory_schema.sql
CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.stock (
    product_id    VARCHAR(50) PRIMARY KEY,
    available     INT         NOT NULL CHECK (available >= 0),
    reserved      INT         NOT NULL CHECK (reserved >= 0),
    reorder_point INT         NOT NULL DEFAULT 10,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version       BIGINT      NOT NULL DEFAULT 0   -- Optimistic Locking (→ ADR-033)
);

-- KEIN FOREIGN KEY auf orders.orders! → Schema-Isolation
-- inventory.stock.product_id ist keine FK auf orders.order_items.product_id
```

### 5.3 Hibernate: Schema-Routing

```java
// application.yml: mehrere Schemas für JPA konfigurieren
// spring.jpa.properties.hibernate.default_schema=orders
// → reicht nicht, wenn mehrere Module unterschiedliche Schemas brauchen

// Lösung: @Table mit schema-Attribut in jeder Entity
@Entity
@Table(schema = "orders", name = "orders")
public class OrderEntity {
    // ...
}

@Entity
@Table(schema = "inventory", name = "stock")
public class StockEntity {
    // ...
}

// Flyway: Locations pro Modul konfigurieren
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy migrationStrategy() {
        return flyway -> {
            // Reihenfolge wichtig: shared zuerst, dann Module
            runMigrations(flyway, "classpath:db/migration/shared");
            runMigrations(flyway, "classpath:db/migration/orders");
            runMigrations(flyway, "classpath:db/migration/inventory");
            runMigrations(flyway, "classpath:db/migration/notification");
        };
    }
}
```

---

## 6. Maschinelle Verifikation: Die Modulith-Wächter

### 6.1 Spring Modulith Verify Test

```java
// src/test/java/com/example/ModulithStructureTest.java
// Dieser Test läuft in CI und schlägt fehl bei jeder Grenzver­letzung

@SpringBootTest
class ModulithStructureTest {

    ApplicationModules modules =
        ApplicationModules.of(ECommerceApplication.class);

    @Test
    void verifyModularStructure() {
        // Prüft:
        // ✓ Keine Abhängigkeiten auf internal-Packages anderer Module
        // ✓ Keine Zirkular-Abhängigkeiten zwischen Modulen
        // ✓ allowedDependencies werden eingehalten
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        // Generiert PlantUML-Diagramme der Modulstruktur
        // → docs/architecture/modules/ wird automatisch aktualisiert
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeAggregatingDocument();
    }

    @Test
    void printModuleOverview() {
        // Zeigt im Test-Output: welche Module existieren, welche Abhängigkeiten
        modules.forEach(System.out::println);
    }
}
```

### 6.2 ArchUnit: Zusätzliche Regeln

```java
// src/test/java/com/example/ModulithArchitectureRules.java
@AnalyzeClasses(packages = "com.example")
class ModulithArchitectureRules {

    // ① Kein direkter DB-Zugriff über Modulgrenzen
    @ArchTest
    static final ArchRule kein_schema_crossing =
        noClasses()
            .that().resideInAPackage("com.example.orders..")
            .should().dependOnClassesThat()
            .areAnnotatedWith(javax.persistence.Table.class)
            .and().resideOutsideOfPackage("com.example.orders..")
            .because("ADR-079: Schema-Isolation — kein Cross-Schema-Zugriff");

    // ② Nur api-Packages sind von außen sichtbar
    @ArchTest
    static final ArchRule nur_api_ist_sichtbar =
        noClasses()
            .that().resideOutsideOfPackage("com.example.orders..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.example.orders.internal..")
            .because("ADR-079: internal-Package ist modulprivat");

    // ③ Events müssen unveränderlich sein (Records oder @Immutable)
    @ArchTest
    static final ArchRule events_sind_records =
        classes()
            .that().resideInAPackage("..events..")
            .and().haveSimpleNameEndingWith("Event")
            .should().beRecords()
            .because("ADR-079: Domain Events sind unveränderliche Fakten — Records erzwingen das");

    // ④ Kein @Transactional in Event-Handlern (→ @ApplicationModuleListener verwenden)
    @ArchTest
    static final ArchRule event_handler_kein_transactional =
        noMethods()
            .that().areAnnotatedWith(ApplicationModuleListener.class)
            .should().beAnnotatedWith(Transactional.class)
            .because("ADR-079: @ApplicationModuleListener hat eigene Transaktionssemantik");

    // ⑤ Shared Kernel darf keine Domänen-Abhängigkeiten haben
    @ArchTest
    static final ArchRule shared_kernel_ist_clean =
        noClasses()
            .that().resideInAPackage("com.example.shared..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.example.orders..",
                "com.example.inventory..",
                "com.example.payment..",
                "com.example.notification.."
            )
            .because("ADR-079: Shared Kernel kennt keine Domänen — nur primitive Value Objects");
}
```

### 6.3 Modul-spezifische Slice-Tests

```java
// Test NUR das Orders-Modul — Inventory und Payment werden gemockt
// → isolierte, schnelle Tests (→ ADR-020)

@ApplicationModuleTest   // Spring Modulith: lädt NUR das Orders-Modul
class OrdersModuleIntegrationTest {

    @Autowired OrderApi     orderApi;   // Die öffentliche API
    @MockBean  InventoryApi inventoryApi; // Nachbar-Modul: gemockt

    @Test
    void placeOrder_publishesOrderPlacedEvent() {
        // Arrange
        when(inventoryApi.checkAvailability(any())).thenReturn(StockCheckResult.allAvailable());

        var command = new PlaceOrderCommand(
            new UserId(42L),
            List.of(new OrderItemRequest(new ProductId("P1"), new Quantity(2))),
            testAddress()
        );

        // Act + Assert: Event wurde publiziert
        assertThat(orderApi.place(command))
            .satisfies(result -> {
                assertThat(result.orderId()).isNotNull();
                assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            });
        // Spring Modulith prüft automatisch: wurde OrderPlacedEvent gefeuert?
    }

    @Test
    @Scenario  // Spring Modulith: vollständiger Event-Flow-Test
    void placeOrder_triggersInventoryReservation(
            PublishedEvents events) {
        var command = new PlaceOrderCommand(...);
        orderApi.place(command);

        assertThat(events)
            .contains(OrderPlacedEvent.class)
            .matching(e -> e.items().size() == 1);
    }
}

// Inventory-Modul in Isolation
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.STANDALONE)
class InventoryModuleTest {

    @Autowired StockApi stockApi;

    @Test
    void reserve_reducesAvailableStock() {
        // Testet nur Inventory-Logik — kein Orders-Modul geladen
    }
}
```

---

## 7. Alternativenanalyse

### 7.1 Alternativen & Warum sie abgelehnt wurden

| Alternative                           | Stärken                                          | Schwächen                                                  | Ablehnungsgrund                                                              |
|---------------------------------------|--------------------------------------------------|------------------------------------------------------------|------------------------------------------------------------------------------|
| **Microservices sofort**              | Maximale Deployment-Unabhängigkeit               | 12–18 Monate Migration, verteilte Komplexität, 5×-Overhead | DORA: schlechtere Ergebnisse ohne Reife; Conway's Law nicht erfüllt          |
| **Monolith ohne Grenzen (Status Quo)**| Minimaler Aufwand                               | Alle beschriebenen Schmerzpunkte bleiben                   | Nicht skalierbar; Deployment-Blockaden kosten 50h/Monat                      |
| **Package-by-Layer**                  | Bekannte Struktur (Controller/Service/Repository)| Keine Domain-Isolation, alle Klassen sehen alle anderen   | Löst das Kopplung-Problem nicht; trotzdem Big Ball of Mud                    |
| **Package-by-Feature ohne Enforcement**| Logische Gruppierung                           | Ohne Enforcement: Imports über Grenzen unkontrolliert      | Grenzen ohne Enforcement sind wirkungslos — Erfahrung aus Praxis             |
| **Vertical Slicing (Feature-Teams)**  | Maximale Kohäsion pro Feature                    | Kein Domain-Modell, kein Aggregat-Schutz                   | Kollidiert mit DDD (→ ADR-023); aggregatübergreifende Konsistenz schwierig   |

### 7.2 Trade-off-Matrix

| Qualitätsziel              | Modulith          | Microservices sofort | Monolith ohne Grenzen | Package-by-Layer |
|----------------------------|-------------------|----------------------|-----------------------|------------------|
| Deployment-Unabhängigkeit  | ⭐⭐⭐⭐             | ⭐⭐⭐⭐⭐               | ⭐⭐                    | ⭐⭐              |
| Modulgrenzen durchgesetzt  | ⭐⭐⭐⭐⭐            | ⭐⭐⭐⭐⭐               | ⭐                     | ⭐⭐              |
| Operativer Aufwand         | ⭐⭐⭐⭐⭐            | ⭐⭐                   | ⭐⭐⭐⭐⭐                | ⭐⭐⭐⭐⭐          |
| Testbarkeit (isoliert)     | ⭐⭐⭐⭐             | ⭐⭐⭐⭐                 | ⭐⭐                    | ⭐⭐              |
| Migrationsfähigkeit        | ⭐⭐⭐⭐⭐            | ⭐⭐⭐                  | ⭐⭐                    | ⭐⭐              |
| Onboarding-Geschwindigkeit | ⭐⭐⭐⭐             | ⭐⭐                   | ⭐⭐                    | ⭐⭐⭐             |
| Build-Zeit (isoliert)      | ⭐⭐⭐⭐             | ⭐⭐⭐⭐⭐               | ⭐⭐                    | ⭐⭐              |
| Initiale Implementierung   | ⭐⭐⭐               | ⭐⭐                   | ⭐⭐⭐⭐⭐                | ⭐⭐⭐⭐⭐          |

---

## 8. Kosten-Nutzen-Analyse

### 8.1 Initiale Migrationskosten

```
Phase 1: Paket-Struktur einführen (ohne Enforcement)
  → Aufwand: 3–5 Personentage
  → Alle bestehenden Klassen in neue Paketstruktur verschieben
  → Kein Behavior-Change, nur Refactoring

Phase 2: Spring Modulith + ArchUnit einführen
  → Aufwand: 2–3 Personentage
  → Zuerst: nur Dokumentation (modules.verify() still failing = OK)
  → Dann: systematisch Verletzungen beheben

Phase 3: Verletzungen beheben (paralleles Entwickler-Refactoring)
  → Aufwand: 10–30 Personentage (je nach Kopplungsgrad der Codebasis)
  → Verteilt über 4–8 Wochen, parallel zu laufender Entwicklung
  → Empfehlung: Boy Scout Rule — bei jedem PR eine Verletzung beheben

Phase 4: Schema-Trennung einführen
  → Aufwand: 5–10 Personentage
  → Flyway-Migration für Schema-Erstellung
  → Hibernate-Konfiguration anpassen

GESAMT: 20–50 Personentage, verteilt über 8–12 Wochen
Kosten bei 80 EUR/h Senior-Entwickler: 12.800–32.000 EUR
```

### 8.2 Nutzen (quantifiziert)

```
Nutzen 1: Deployment-Blockaden eliminiert
  Aktuell:    50h/Monat = 4.000 EUR/Monat (2 × Senior-Dev-Stunde)
  Nach Modul: geschätzt -70% = 15h/Monat = 1.200 EUR/Monat
  Einsparung: 2.800 EUR/Monat

Nutzen 2: Test-Laufzeit reduziert (isolierte Modul-Tests)
  Aktuell:    18 Minuten Full Suite × 20 Commits/Tag × 5 Devs = 30h/Woche
  Nach Modul: Modul-Tests 3 Min, Full Suite nur bei Main-Branch
              Geschätzt -75% = 7.5h/Woche = 30h/Monat = 2.400 EUR/Monat
  Einsparung: 1.800 EUR/Monat

Nutzen 3: Onboarding-Zeit verkürzt
  Aktuell:    8 Wochen bis selbstständige Produktivität
  Nach Modul: geschätzt 5 Wochen (klare Grenzen, weniger globales Kontextwissen)
  Einsparung: 3 Wochen × 40h × 80 EUR/h = 9.600 EUR pro Neueinstellung

Nutzen 4: Reduktion unbeabsichtigter Regressions
  Aktuell:    6 Build-Breaks/Woche durch Cross-Module-Kopplung
  Nach Modul: 0 (ArchUnit verhindert das Commitment)
  Einsparung: 6 × 2h Debug × 80 EUR = 960 EUR/Woche = 3.840 EUR/Monat

TOTALER MONATLICHER NUTZEN: ~10.040 EUR/Monat

BREAK-EVEN: 12.800–32.000 EUR ÷ 10.040 EUR/Monat = 1,3–3,2 Monate
```

### 8.3 Betriebskosten (laufend)

```
Kein zusätzlicher Infrastruktur-Aufwand (ein Deployment = ein Container)
Wartungsaufwand: ~10% einer Engineering-Stelle für Architektur-Guardianship
→ ADR-Review, ArchUnit-Erweiterungen, Neue-Modul-Onboarding
Geschätzte Kosten: 6.400 EUR/Monat (80% einer Stelle × 80 EUR/h × 160h)

NETTO MONATLICHER VORTEIL: 10.040 - 6.400 = 3.640 EUR/Monat (nach Break-Even)
```

---

## 9. Migrationsstrategie: Vom Big Ball of Mud zum Modulith

### Phase 1: Inventory (Woche 1–2)
```
Ziel: Struktur ohne Enforcement einführen
1. Paket-Hierarchie erstellen: orders/, inventory/, shared-kernel/
2. Klassen verschieben (IntelliJ Refactor → Move)
3. Spring Modulith einbinden, verify() im Test (aber failing = OK)
4. Kein Business-Code ändern
```

### Phase 2: Grenzen sichtbar machen (Woche 3–4)
```
Ziel: alle Verletzungen kennen und priorisieren
1. modules.verify() als Test einschalten (expected to fail)
2. Verletzungs-Report: welche Klassen überqueren Grenzen?
3. Priorisieren nach Häufigkeit und Risiko
4. Plan erstellen: welche Verletzung in welchem Sprint
```

### Phase 3: Systematisches Refactoring (Woche 5–12)
```
Ziel: alle Verletzungen beheben, Test wird grün
1. Pro Sprint: 3–5 Verletzungen beheben
2. Muster für häufige Verletzungen:
   - Direkte Entity-Zugriffe → DTO + Mapper
   - Cross-Repository → Event oder API-Aufruf
   - Shared Services → in Shared Kernel oder duplizieren (DRY ist hier falsch!)
3. modules.verify() wird grün → Test in CI = Gate
```

### Phase 4: Schema-Trennung (Woche 10–14)
```
Ziel: Datenbank-Isolation
1. Schemas erstellen: orders, inventory, etc.
2. Flyway-Migrationen: bestehende Tabellen in neue Schemas verschieben
   (PostgreSQL: kein ALTER TABLE, sondern: ALTER TABLE SET SCHEMA)
3. JPA @Table(schema = "...") anpassen
4. Foreign Keys über Schema-Grenzen: LÖSCHEN (!)
   → Ersatz: Applikations-Konsistenz durch Events
```

### Phase 5: Event-basierte Kommunikation (Woche 12–16)
```
Ziel: synchrone Cross-Modul-Aufrufe durch Events ersetzen
1. @ApplicationModuleListener einführen
2. Transaktionsgrenzen prüfen: ein Modul = eine Transaktion
3. Saga-Pattern für mehrstufige Abläufe (→ ADR-065)
```

---

## 10. Service-Extraktion: Wenn und Wie

### 10.1 Trigger-Kriterien (aus ADR-077)

```
Ein Modul wird als Service extrahiert wenn ≥ 2 dieser Trigger erfüllt sind:

□ Team-Blockierung: Teams können sich beim Deploy nicht koordinieren
□ Ressourcen-Ungleichgewicht: Modul braucht 5× mehr CPU/RAM als Rest
□ Technologie-Divergenz: Modul muss andere Sprache/Runtime nutzen
□ Regulatorische Isolation: Compliance erfordert physische Datentrennung
□ Team-Größe: > 10 Personen in einem Modul

Aktuelle Messung: _______________
Nächste Messung: _______________  (Review-Datum dieses ADR)
```

### 10.2 Extraktions-Playbook

Wenn die Trigger erfüllt sind:

```
Schritt 1: Strangler Fig vorbereiten (1–2 Wochen)
  → Modul-API vollständig dokumentieren (OpenAPI → ADR-066)
  → Contract Tests einrichten (Pact → ADR-019)
  → Feature Flag für Traffic-Split erstellen (→ ADR-039)

Schritt 2: Service-Skelett aufsetzen (1 Woche)
  → Eigenes Repository, eigene CI/CD-Pipeline (→ ADR-036)
  → Eigener Kubernetes-Namespace (→ ADR-038)
  → Kafka-Topics als Kommunikationskanal (→ ADR-041)
  → Outbox Pattern für transaktionale Konsistenz (→ ADR-042)

Schritt 3: Datenmigration (2–4 Wochen)
  → Schema in eigene Datenbank migrieren
  → pg_dump + pg_restore oder Replikation während Migration
  → Dual-Write-Phase: Modulith schreibt in beide, Service liest aus eigenem Schema

Schritt 4: Traffic-Migration (2 Wochen)
  → Canary: 5% → 25% → 50% → 100% (→ ADR-059)
  → Contract Tests: beide grün halten
  → Rollback-Mechanismus bereit

Schritt 5: Modulith bereinigen (1 Woche)
  → Modul aus Modulith entfernen
  → Durch API-Client ersetzen (REST oder gRPC → ADR-067)
  → ArchUnit: kein direkter DB-Zugriff mehr auf altes Schema

GESAMT: 7–11 Wochen pro Modul-Extraktion (realistisch, nicht optimistisch)
```

---

## 11. Konsequenzen

### 11.1 Sofort (Woche 1–4)
- Spring Modulith in `build.gradle.kts` hinzufügen
- `ModulithStructureTest` anlegen — initial failing, Ziel: grün in 12 Wochen
- ArchUnit-Regeln (Abschnitt 6.2) als Pflicht-Gate in CI einrichten
- Paket-Struktur einführen ohne Behavior-Change

### 11.2 Mittelfristig (3–6 Monate)
- Alle Verletzungen behoben: `ModulithStructureTest` ist dauerhaft grün
- Schema-Trennung vollständig — kein Cross-Schema-Join im Produktionscode
- Alle Cross-Modul-Transaktionen aufgelöst — Eventual Consistency eingeführt
- Modul-Test-Zeiten: < 3 Minuten pro Modul

### 11.3 Langfristig (6–18 Monate)
- Trigger-Messung zeigt: Modul X erfüllt Extractions-Kriterien → Service-Extraktion
- Architektur-Dokument (→ ADR-047) zeigt aktuellen Stand
- Jährliches Review: sind die Grenzen noch richtig gezogen?

### 11.4 Risiken

| Risiko | Wahrscheinlichkeit | Auswirkung | Mitigation |
|--------|--------------------|------------|------------|
| Team ignoriert Grenzen nach Einführung | M | H | ArchUnit-CI-Gate — Commit geht nicht durch |
| Falsch gezogene Modul-Grenzen (Bounded Context falsch) | M | M | Modul-Grenzen können im Modulith günstiger korrigiert werden als Service-Grenzen |
| Eventual Consistency wird unterschätzt | H | H | Training: Team muss Saga-Pattern verstehen; Monitoring der Event-Processing-Zeiten |
| Extraction-Trigger nie gemessen | H | M | Review-Datum dieses ADR in Kalender eingetragen |

---

## 12. Quellen & Referenzen

- **Sam Newman, "Building Microservices" (2022), 2. Auflage, Kap. 1 und 20** — "Monolith First"; Strangler Fig Pattern für schrittweise Migration.
- **Martin Fowler, "Monolith First" (2015)** — Empirische Grundlage für die Reihenfolge: erst verstehen, dann aufteilen.
- **Melvin Conway, "How Do Committees Invent?" (1968)** — Conway's Law: Architektur folgt Kommunikationsstruktur.
- **Zhamak Dehghani, "Data Mesh" (2022), Kap. 3** — Domain-Ownership auch im Monolithen; Schema-per-Domain.
- **Oliver Drotbohm, Spring Modulith (2023)** — Referenz-Dokumentation und Rational für Modulith-Konzept in Spring.
- **DORA State of DevOps 2019, 2023** — Empirische Daten: Microservices verbessern Elite-Performer, verschlechtern Low-Performer.
- **Neal Ford, Rebecca Parsons, Mark Richards, "Software Architecture: The Hard Parts" (2022), Kap. 4** — Komponentenaufteilung und Daten-Disaggregation-Strategien.
- **Eric Evans, "Domain-Driven Design" (2003), Kap. 4** — Bounded Context als natürliche Modulgrenze.
- **Vaughn Vernon, "Implementing Domain-Driven Design" (2013), Kap. 3** — Bounded Context Integration Patterns.

---

## 13. Akzeptanzkriterien

- [ ] `ModulithStructureTest.verifyModularStructure()` ist dauerhaft grün in CI
- [ ] Alle ArchUnit-Regeln (Abschnitt 6.2) sind grün — kein Bypass via `@ArchIgnore` ohne dokumentierten Grund
- [ ] Modul-Test-Laufzeit für jedes Modul < 3 Minuten (gemessen in CI)
- [ ] Kein Cross-Schema-JOIN im Produktionscode (Flyway-Migration-Validierung)
- [ ] Kein `@Transactional` der Modulgrenzen überspannt (ArchUnit-Regel aktiv)
- [ ] Onboarding-Dokumentation für neue Module: Checkliste in `docs/modules/new-module.md`
- [ ] Trigger-Kriterien für Service-Extraktion sind im Review-Datum dieses ADR messbar dokumentiert

---

## Verwandte ADRs

- **Supersedes:** — (ersetzt keine bestehende Entscheidung)
- [ADR-023](ADR-023-domain-driven-design.md) — DDD: Bounded Contexts bestimmen Modul-Grenzen
- [ADR-031](ADR-031-hexagonal-architecture.md) — Hexagonal Architecture innerhalb jedes Moduls
- [ADR-032](ADR-032-cqrs.md) — CQRS pro Modul: Command- und Query-Seite getrennt
- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka für externe Event-Kommunikation wenn Modul extrahiert
- [ADR-042](ADR-042-outbox-pattern.md) — Outbox für transaktionale Event-Publizierung
- [ADR-050](ADR-050-multi-module-gradle.md) — Multi-Module-Gradle als Build-seitiger Modulith
- [ADR-061](ADR-061-fitness-functions.md) — ArchUnit als maschinelle Enforcement-Strategie
- [ADR-065](ADR-065-saga-pattern.md) — Saga für Modul-übergreifende Transaktionen
- [ADR-077](ADR-077-microservices-vs-monolith.md) — übergeordneter Entscheidungsrahmen
