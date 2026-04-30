# ADR-090 — iSAQB Advanced: Evolutionary Architecture & kontinuierliche Fitness

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Architektur-Board                                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Advanced · Evolutionary Architecture                    |
| Betroffene Teams  | Alle Engineering-Teams, Tech Leads                            |
| iSAQB-Lernziel    | Advanced Level · Evolutionary Design                          |

---

## 1. Das Problem mit "Design Upfront"

```
BIG DESIGN UPFRONT (BDUF):
  Annahme: Wir kennen alle Anforderungen am Anfang.
  Realität: Anforderungen ändern sich in jedem Monat.
  Konsequenz: Architektur ist nach 12 Monaten an der Realität vorbei.

KEIN DESIGN (emergent ohne Steuerung):
  Annahme: Guter Code entsteht durch Refactoring.
  Realität: Ohne Leitplanken entstehen Schicht-Verletzungen, God Classes, Spaghetti.
  Konsequenz: "Big Ball of Mud" — teuerste Architektur.

EVOLUTIONARY ARCHITECTURE (Neal Ford, Rebecca Parsons, 2017):
  Annahme: Architektur muss kontrolliert geändert werden können.
  Prinzip: Fitness Functions messen kontinuierlich ob Architektur noch gilt.
  Konsequenz: Architektur kann auf Anforderungsänderungen reagieren
              OHNE zusammenzubrechen.

KERNAUSSAGE:
  "An evolutionary architecture supports guided, incremental change
   across multiple dimensions as a first principle."
  — Ford, Parsons, "Building Evolutionary Architectures" (2017)
```

---

## 2. Fitness Functions: Architektur kontinuierlich messen

### 2.1 Die drei Dimensionen von Fitness Functions

```
ATOMARE FITNESS FUNCTIONS (einzelne Eigenschaft):
  → Prüfen EINE Architektur-Invariante
  → Laufen in Sekunden bis Minuten
  → Beispiel: "Domain darf kein Spring kennen"

HOLISTISCHE FITNESS FUNCTIONS (systemweite Eigenschaft):
  → Prüfen Emergenz-Eigenschaften (entstehen aus Zusammenspiel)
  → Laufen länger (Last-Tests, E2E-Tests)
  → Beispiel: "System erfüllt P95 < 500ms unter Last"

GETRIGGERTE FITNESS FUNCTIONS (bei Ereignissen):
  → Laufen bei spezifischen Trigger-Ereignissen
  → Beispiel: "Bei jedem Deployment: Smoke-Test"
  → Beispiel: "Bei jedem Tag: Security-Scan"

KONTINUIERLICHE FITNESS FUNCTIONS (laufend):
  → Laufen dauerhaft in Produktion
  → Beispiel: Prometheus-Alert wenn Latenz steigt
  → Kein manuelles Trigger nötig
```

### 2.2 Fitness Functions für verschiedene Architektur-Dimensionen

```java
// DIMENSION 1: Strukturelle Fitness (ArchUnit)
@AnalyzeClasses(packages = "com.example")
class StructuralFitnessFunctions {

    // Fitness Function: Schichtenarchitektur
    @ArchTest
    static final ArchRule layer_fitness =
        layeredArchitecture()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Adapter").definedBy("..adapter..")
            .whereLayer("Domain").mayNotAccessAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Adapter").mayOnlyAccessLayers("Application", "Domain");

    // Fitness Function: Kein Circular Dependency
    @ArchTest
    static final ArchRule no_cycles =
        slices().matching("com.example.(*)..")
            .should().beFreeOfCycles();

    // Fitness Function: Methoden-Komplexität
    @ArchTest
    static final ArchRule low_complexity =
        methods().should(haveCyclomaticComplexityLessThan(10))
            .because("Cognitive load ADR-084: Complexity <= 10");
}

// DIMENSION 2: Performance-Fitness (Gatling)
class PerformanceFitnessFunction extends Simulation {
    // P95 < 500ms unter 100 RPS (QS-P01 → ADR-082)
    setUp(orderScenario.injectOpen(constantUsersPerSec(100).during(60)))
        .assertions(
            global().responseTime().percentile(95).lt(500),
            global().failedRequests().percent().lt(1.0)
        );
}

// DIMENSION 3: Security-Fitness (SAST in CI)
// Läuft bei jedem Commit automatisch → ADR-080

// DIMENSION 4: Coverage-Fitness (JaCoCo Gate)
jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "0.80".toBigDecimal() } }
    }
}
```

---

## 3. Inkrementelle Architektur-Änderungen

### 3.1 Das Strangler Fig Pattern (Martin Fowler, 2004)

```
IDEE: Wie ein Würgefeigenbaum (Strangler Fig) der einen Baum umwächst:
  → Neuer Code wächst neben altem Code
  → Schrittweise Migration: neuer Code übernimmt Funktion für Funktion
  → Alter Code wird "gewürgt" (entfernt) wenn neue Version vollständig
  → Kein Big Bang Rewrite (der riskanteste und teuerste Ansatz)

ANWENDUNGSFALL: Monolith → Modulith Migration (→ ADR-079)
```

```java
// PHASE 1: Strangler Fig — Routing-Schicht einführen
// Neuer Code und alter Code koexistieren

@RestController
public class OrderController {

    private final LegacyOrderService legacyService;  // Alter Code
    private final NewOrderDomainService newService;  // Neuer Code

    @Value("${feature.new-order-service.enabled:false}")
    private boolean useNewOrderService;              // Feature Flag!

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody PlaceOrderRequest req) {
        if (useNewOrderService) {
            // Neuer Service: Hexagonal, DDD, testbar
            return ResponseEntity.status(201)
                .body(newService.place(req.toCommand()));
        } else {
            // Alter Service: bewährt, aber schwer zu ändern
            return ResponseEntity.status(201)
                .body(legacyService.createOrder(req));
        }
    }
}

// PHASE 2: Canary-Release → 5% auf neuen Service
// PHASE 3: 50% → Metriken vergleichen
// PHASE 4: 100% → Legacy-Code entfernen
// PHASE 5: Feature-Flag-Code entfernen
```

### 3.2 Branch by Abstraction (Martin Fowler)

```java
// Wenn Strangler Fig nicht möglich (z.B. tief eingebettete Komponente):
// Branch by Abstraction macht die Änderung ohne Long-Lived-Branch

// SCHRITT 1: Abstraktions-Layer vor dem alten Code
public interface CacheProvider {
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, Duration ttl);
}

// SCHRITT 2: Alter Code (Caffeine) hinter Interface verstecken
@Primary
@ConditionalOnProperty("cache.provider", havingValue = "caffeine")
class CaffeineCacheProvider implements CacheProvider { /* ... */ }

// SCHRITT 3: Neuer Code (Redis) als Alternative
@ConditionalOnProperty("cache.provider", havingValue = "redis")
class RedisCacheProvider implements CacheProvider { /* ... */ }

// SCHRITT 4: Feature-Flag steuert welche Implementierung aktiv
# application.yml:
# cache.provider: caffeine  → jetzt
# cache.provider: redis     → nach Migration

// SCHRITT 5: Alte Caffeine-Implementierung entfernen wenn Redis bewährt
// SCHRITT 6: Interface bleibt (Abstraktions-Wert erhalten)
```

---

## 4. Technische Schulden bewusst managen

### 4.1 Schulden-Kategorien und Rückzahlungsstrategien

```
RUCKSACK-METAPHER (Ward Cunningham, 1992 — Original-Begriff):
  "Technical Debt" = bewusst aufgenommener Kredit für schnellere Lieferung
  Ward meinte ursprünglich: BEWUSSTE Schulden die man BEABSICHTIGT zurückzuzahlen
  NICHT: schlechten Code der aus Unwissenheit entsteht (das ist Unwissenheit, kein Kredit)

VIER SCHULDEN-ARTEN (Martin Fowler):
  
  Rücksichtslos-Bewusst:
    "Wir haben keine Zeit für gutes Design"
    → Bewusste Entscheidung, Code ist bekanntermaßen schlecht
    → Wird oft nie zurückgezahlt
    
  Rücksichtslos-Unbewusst:
    "Was ist ein Design Pattern?"
    → Keine Entscheidung — Unwissenheit
    → Größte Schuldenmasse in der Industrie
    
  Vorsichtig-Bewusst:
    "Wir deployen jetzt und refactoren danach"
    → Vernünftig wenn: Deadline real, Rückzahlung geplant
    → Erfordert: Ticket, Priorität, Datum
    
  Vorsichtig-Unbewusst:
    "Jetzt sehen wir wie wir es hätten machen sollen"
    → Entsteht durch Lernen — unvermeidlich
    → Gesunde Schulden: Team lernt und verbessert sich

EMPFEHLUNG:
  Nur Vorsichtig-Bewusste Schulden sind akzeptabel.
  → Immer dokumentieren (ADR mit Status "Temporary")
  → Immer Ticket mit Priorität anlegen
  → Immer Rückzahlungsplan
```

### 4.2 Schulden messen und priorisieren

```java
// Technische-Schulden-Annotation für explizite Dokumentation
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface TechnicalDebt {
    String issue();       // "PROJ-1234"
    String reason();      // Warum diese Schuld?
    String plan();        // Wie wird sie zurückgezahlt?
    String dueDate();     // "2024-Q2"
    Severity severity();  // HIGH, MEDIUM, LOW

    enum Severity { HIGH, MEDIUM, LOW }
}

// Verwendung:
@TechnicalDebt(
    issue   = "ORDERS-456",
    reason  = "Sprint-Deadline: OrderService.findAll() lädt alle Orders ohne Pagination",
    plan    = "Cursor-based Pagination implementieren wenn OrderCount > 10.000",
    dueDate = "2024-Q2",
    severity = TechnicalDebt.Severity.HIGH
)
public List<Order> findAll() {
    return orderRepository.findAll(); // TODO-Ticket: ORDERS-456
}

// ArchUnit: Warnung bei überfälligen Schulden
@ArchTest
static final ArchRule keine_ueberfaelligen_schulden =
    noClasses()
        .should(beAnnotatedWithOverdueDebt())
        .because("Technische Schulden mit abgelaufenem Datum müssen adressiert werden");
```

---

## 5. Architektur-Entscheidungen revidieren

### 5.1 Wann ADRs superseded werden

```
ADRs sind nicht ewig gültig. Sie werden superseded wenn:
  ① Kontext sich geändert hat (Team gewachsen → Microservices sinnvoll)
  ② Technologie nicht mehr gilt (Lombok → Records, Java 21)
  ③ Neue Information verfügbar (Failure Modes erst in Produktion sichtbar)
  ④ Qualitätsziele sich geändert haben

WICHTIG: Supersediertes ADR NICHT löschen — es zeigt die Evolution!

Beispiel:
  ADR-004-alt: "Reactive Programming mit WebFlux" [Status: Superseded]
  ADR-004: "Virtual Threads (Project Loom) statt Reactor" [Status: Active]
  → Warum wurde gewechselt? Beide ADRs zusammen erzählen die Geschichte.
```

### 5.2 Architektur-Review-Prozess (quartalsweise)

```markdown
# Quarterly Architecture Review — Template

## Datum: Q1 2025

## Metriken-Überprüfung
| Fitness Function | Ziel | Aktuell | Trend |
|---|---|---|---|
| P95-Latenz | < 500ms | 340ms | ✅ stabil |
| Deployment-Frequenz | täglich | 2×/Woche | ⚠️ gesunken |
| Change-Failure-Rate | < 5% | 8% | ❌ gestiegen |
| MTTR | < 1h | 3h | ❌ gestiegen |

## Architektur-Drift erkennen
- Gibt es Module die Conway's Law widersprechen? (→ ADR-085)
- Gibt es ADRs die durch Team-Wachstum nicht mehr passen?
- Welche technischen Schulden sind überfällig?

## Entscheidungen für nächstes Quartal
- [ ] ADR-077 Review: Extraktions-Trigger für Orders-Modul prüfen
- [ ] ADR-043 Review: HikariCP Pool-Größe nach Lasttest anpassen
- [ ] ADR-039 Review: Feature-Flags die > 6 Monate alt sind aufräumen
```

---

## 6. Die drei Evolutionsdimensionen

```
FORD & PARSONS, "Building Evolutionary Architectures" — drei Achsen:

TECHNISCHE PARTITION:
  Wie ist Code technisch organisiert? (Schichten, Module, Services)
  Fitness: ArchUnit, Modulith Verify
  Evolution: Schichtenarch → Hexagonal → Modulith → (optional) Microservices

DOMÄNEN-PARTITION:
  Wie ist Code nach fachlichen Grenzen organisiert?
  Fitness: Context Map, Team Topologies (→ ADR-085, ADR-089)
  Evolution: CRUD → DDD-Tactical → DDD-Strategic → Event Sourcing

DEPLOYMENT-PARTITION:
  Wie wird Code deployed und betrieben?
  Fitness: DORA-Metriken, Deployment-Frequenz
  Evolution: Manuell → CI/CD → Canary → GitOps

Diese drei Dimensionen können unabhängig voneinander evolvieren.
Häufiger Fehler: Deployment-Evolution (Microservices) ohne Domänen-Evolution.
```

---

## 7. Quellen & Referenzen

- **Neal Ford, Rebecca Parsons, "Building Evolutionary Architectures" (2017), O'Reilly** — Das Standardwerk; Fitness Functions, drei Evolutionsdimensionen, Strangler Fig.
- **Martin Fowler, "Strangler Fig Application" (2004)** — Pattern für inkrementelle Legacy-Migration.
- **Martin Fowler, "Branch by Abstraction" (2014)** — Technik für Änderungen ohne Long-Lived-Branches.
- **Ward Cunningham, "The WyCash Portfolio Management System" (1992)** — Originalformulierung von Technical Debt.
- **Martin Fowler, "Technical Debt Quadrant" (2009)** — Vier Schulden-Typen Klassifikation.
- **DORA Research Program, "Accelerate" (2018)** — Metriken für evolvierbare Architekturen.

---

## Akzeptanzkriterien

- [ ] Fitness-Functions-Suite läuft bei jedem Commit (strukturell + Coverage)
- [ ] Quarterly Architecture Review ist im Team-Kalender
- [ ] Alle `@TechnicalDebt`-Annotationen mit `dueDate` in der Vergangenheit haben aktive Tickets
- [ ] Strangler-Fig-Phasen für aktive Migrationen dokumentiert
- [ ] ADR-Status-Review: alle ADRs älter als 12 Monate auf "Superseded" oder "Confirmed" gesetzt

---

## Verwandte ADRs

- [ADR-061](ADR-061-fitness-functions.md) — ArchUnit-Katalog für strukturelle Fitness
- [ADR-087](ADR-087-isaqb-architekturbewertung.md) — ATAM für nicht-kontinuierliche Bewertung
- [ADR-051](ADR-051-definition-of-done.md) — Technical Debt im DoD berücksichtigt
- [ADR-091](ADR-091-isaqb-reactive-architecture.md) — Reactive Architecture als Evolutions-Option
