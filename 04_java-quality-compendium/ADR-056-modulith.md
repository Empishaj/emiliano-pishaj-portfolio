# ADR-056 — Modulith: Modularer Monolith als Microservice-Alternative

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Boot 3.x · Spring Modulith 1.x        |
| Datum      | 2024-01-01                                        |
| Kategorie  | Architektur                                       |

---

## Kontext & Problem

Microservices lösen Probleme von sehr großen Teams mit sehr großen Systemen. Für die meisten Projekte bringen sie mehr Komplexität als Nutzen: verteilte Transaktionen, Netzwerk-Latenz, Deployment-Overhead, Observability-Aufwand. Der **Modulith** ist ein Monolith mit klar durchgesetzten Modulgrenzen — die Vorteile von Microservices ohne den operativen Overhead.

---

## Monolith vs. Modulith vs. Microservices

```
Monolith (Big Ball of Mud):
→ Alles in einem Deployment, keine Grenzen
→ Einfach zu deployen, unmöglich zu warten

Modulith:
→ Alles in einem Deployment, klar durchgesetzte Modulgrenzen
→ Module kommunizieren nur über definierte APIs
→ Intern: direkte Methodenaufrufe; Async via Interne Events
→ Kein Netzwerk-Overhead, kein verteiltes Commit-Problem

Microservices:
→ Separate Deployments, Netzwerkkommunikation
→ Sinnvoll ab: > 50 Entwickler, > 10 unabhängige Teams
→ Overhead: Service Mesh, Distributed Tracing, Contract Testing, ...

Empfehlung: Starte als Modulith. Extrahiere Services wenn konkrete Probleme auftreten.
```

---

## Spring Modulith: Modulgrenzen erzwingen

```
src/main/java/com/example/
├── ECommerceApplication.java
│
├── order/                          ← Modul "order"
│   ├── OrderModule.java            ← Öffentliche API des Moduls (optional)
│   ├── Order.java                  ← Internes Domänenobjekt
│   ├── OrderService.java           ← Interne Service-Logik
│   ├── OrderRepository.java        ← Internes Repository
│   └── internal/                   ← Explizit intern (nicht von anderen Modulen nutzbar)
│       └── OrderCalculator.java
│
├── inventory/                      ← Modul "inventory"
│   ├── InventoryService.java       ← Öffentliche API
│   └── internal/
│       └── StockCalculator.java
│
└── notification/                   ← Modul "notification"
    └── NotificationService.java
```

---

## Spring Modulith Konfiguration

```java
// @SpringBootApplication ist der Anker — alle Subpakete sind Module
@SpringBootApplication
public class ECommerceApplication { ... }

// Explizite Modul-API-Beschreibung (optional aber empfohlen)
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = "inventory"  // Nur inventory darf referenziert werden
)
package com.example.order;
```

```kotlin
// build.gradle.kts
implementation("org.springframework.experimental:spring-modulith-starter-core:1.1.0")
implementation("org.springframework.experimental:spring-modulith-events-api:1.1.0")
testImplementation("org.springframework.experimental:spring-modulith-starter-test:1.1.0")
```

---

## Modul-zu-Modul Kommunikation

```java
// ❌ Schlecht: direkte Abhängigkeit auf interne Klasse eines anderen Moduls
@Service
public class OrderService {
    // Direkte Abhängigkeit auf INTERNE Klasse von inventory — verletzt Grenze!
    private final com.example.inventory.internal.StockCalculator calculator;
}

// ✅ Gut: über öffentliche API kommunizieren
@Service
public class OrderService {
    // Nur öffentliche API von inventory nutzen
    private final InventoryService inventoryService;

    public void placeOrder(PlaceOrderCommand cmd) {
        // Synchroner Aufruf — beide im selben Prozess, kein Netzwerk
        inventoryService.reserve(cmd.items());
        var order = createAndSaveOrder(cmd);
    }
}

// ✅ Noch besser: Async via Spring Modulith Events (lose Kopplung)
@Service
public class OrderService {

    private final ApplicationEventPublisher events;

    @Transactional
    public void placeOrder(PlaceOrderCommand cmd) {
        var order = createAndSaveOrder(cmd);

        // Event publizieren — inventory reagiert asynchron
        // Transaktional: Event wird nur publiziert wenn Transaktion committed
        events.publishEvent(new OrderPlacedEvent(order.id(), order.items()));
    }
}

// Inventory-Modul: reagiert auf Event — kennt OrderService nicht
@ApplicationModuleListener  // Spring Modulith: transaktionaler Event-Listener
public class InventoryEventHandler {

    @EventListener
    void on(OrderPlacedEvent event) {
        inventoryService.reserve(event.orderId(), event.items());
    }
}
```

---

## Modul-Tests: Slice-Test pro Modul

```java
// Spring Modulith: Test-Slice nur für ein Modul
@ApplicationModuleTest   // Lädt nur das "order"-Modul und seine Abhängigkeiten
class OrderModuleTest {

    @Autowired OrderService orderService;
    @MockBean  InventoryService inventoryService; // Nachbar-Modul gemockt

    @Test
    void placeOrder_reservesInventory() {
        orderService.placeOrder(validCommand());

        verify(inventoryService).reserve(any(), any());
    }
}

// Modulstruktur-Test: erzwingt Modulgrenzen maschinell
@Test
void verifyModularStructure() {
    ApplicationModules.of(ECommerceApplication.class).verify();
    // Schlägt fehl wenn:
    // - Modul A auf interne Klasse von Modul B zugreift
    // - Zirkuläre Abhängigkeiten zwischen Modulen existieren
    // - Nicht-erlaubte Abhängigkeiten vorhanden sind
}
```

---

## Dokumentation: Moduldiagramm automatisch generieren

```java
// Spring Modulith generiert C4-kompatible Moduldiagramme
@Test
void createModulithDocumentation() {
    var modules = ApplicationModules.of(ECommerceApplication.class);

    // PlantUML-Diagramm aller Module und ihrer Abhängigkeiten
    new Documenter(modules)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml();
    // Output: target/spring-modulith-docs/
}
```

---

## Migrations-Pfad: Modulith → Microservices

```
Wenn ein Modul extrahiert werden soll:
1. Modul kommuniziert bereits nur über Events → kein direkter Aufruf
2. Modul hat eigenes DB-Schema (Schema per Modul → DB per Service)
3. Event-Kommunikation auf Kafka umstellen (→ ADR-041)
4. Modul als separater Service deployen
5. Im Monolith: Modul durch Client ersetzen der den Service aufruft

Spring Modulith macht diesen Pfad explizit planbar.
```

---

## Konsequenzen

**Positiv:** Einfacheres Deployment als Microservices (ein Artefakt). Keine verteilten Transaktionen. Modulgrenzen maschinell prüfbar. Migrationsweg zu Microservices bleibt offen.

**Negativ:** Vertikale Skalierung einzelner Module unmöglich (alles skaliert zusammen). Bei sehr großen Teams: Merge-Konflikte. Wenn wirklich verschiedene Technologien pro Modul nötig: nicht möglich.

---

## 💡 Guru-Tipps

- **Schema-per-Modul**: Jedes Modul hat sein eigenes DB-Schema — `order.*`, `inventory.*`. Erleichtert spätere Extraktion.
- **`@ApplicationModuleListener`** statt `@EventListener` für transaktionale Event-Verarbeitung im Modulith.
- **Strangler Fig Pattern**: Monolith bleibt bestehen, neues Verhalten wird als Microservice daneben gebaut.
- **"Monolith first"** (Martin Fowler): Starte als Modulith. Extrahiere Services wenn es konkrete Skalierungs- oder Team-Autonomie-Probleme gibt — nicht spekulativ.

---

## Verwandte ADRs

- [ADR-023](ADR-023-domain-driven-design.md) — Bounded Contexts als Modulgrundlage.
- [ADR-031](ADR-031-hexagonal-architecture.md) — Hexagonal Architecture pro Modul.
- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka wenn Modul zu eigenem Service wird.
