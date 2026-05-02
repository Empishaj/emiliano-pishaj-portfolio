# ADR-093 — iSAQB Expert/Guru: Living Documentation — Architektur die sich selbst beschreibt

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Architektur-Board                                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Expert · Living Documentation · Docs-as-Code            |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| iSAQB-Lernziel    | Expert Level · Continuous Architecture Documentation          |

---

## 1. Das unlösbar scheinende Dokumentationsproblem

```
ERFAHRUNG IN JEDEM PROJEKT:

Monat 1: Architektur dokumentiert. Sorgfältig, vollständig.
Monat 3: 20% der Dokumentation veraltet.
Monat 6: 50% veraltet. Niemand liest sie mehr.
Monat 12: "Wir haben eine Dokumentation, aber bitte nicht daran orientieren."
Monat 18: Neue Entwickler fragen Kollegen, nicht die Dokumentation.

WURZEL-URSACHE:
  Dokumentation und Code sind entkoppelt.
  Code ändert sich täglich.
  Dokumentation ändert sich quartalsweise (wenn überhaupt).
  → Divergenz ist unvermeidlich.

CYRILLE MARTRAIRE'S LÖSUNG (Living Documentation, 2019):
  "Documentation should be generated from the code itself,
   or extracted from it, so that it cannot become outdated."
  
  Wenn Dokumentation AUS dem Code generiert wird:
  Code ändert sich → Dokumentation ändert sich AUTOMATISCH.
  → Keine Divergenz möglich.
```

---

## 2. Living Documentation: Die vier Quellen

```
QUELLE 1: Code selbst als Dokumentation
  → Klassen- und Methodennamen sind Dokumentation
  → Typsystem kommuniziert Invarianten
  → Tests beschreiben erwartetes Verhalten

QUELLE 2: Annotations und Metadaten
  → @ADR-Referenzen im Code (→ ADR-009)
  → @BusinessRule("RULE-42") auf Geschäftslogik
  → JavaDoc als strukturierte Dokumentation (→ ADR-048)

QUELLE 3: Tests als ausführbare Dokumentation
  → BDD-Tests (Given/When/Then) beschreiben Verhalten in Sprache
  → @DisplayName-Tests lesen sich wie Spezifikation
  → Serenity BDD generiert HTML-Report aus Tests

QUELLE 4: Infrastruktur als Dokumentation
  → Kubernetes-Manifeste beschreiben Deployment
  → Terraform beschreibt Infrastruktur (→ ADR-060)
  → OpenAPI-Schema beschreibt API (→ ADR-066)
  → Avro/Protobuf beschreibt Events
```

---

## 3. Code als Dokumentation: Expressives Design

### 3.1 Naming als Primärdokumentation

```java
// ❌ SCHLECHT — Code erklärt nicht was er tut
public class OSvc {
    public Ord proc(Map<String, Object> d) {
        var o = new Ord();
        o.setS("PEND");
        // Was ist "d"? Was ist "PEND"? Was macht "proc"?
    }
}

// ✅ GUT — Code ist selbsterklärend
public class OrderDomainService {
    public Order placeOrder(PlaceOrderCommand command) {
        var order = Order.withStatus(OrderStatus.PENDING_PAYMENT)
            .forCustomer(command.customerId())
            .withItems(command.items())
            .build();
        // Jeder Begriff stammt aus der Ubiquitous Language (→ ADR-023)
        // Kein Kommentar nötig — der Code beschreibt sich selbst
    }
}

// Value Objects als Typ-Dokumentation
public record Money(BigDecimal amount, Currency currency) {
    // Der Typ "Money" kommuniziert: das ist kein primitiver Betrag
    // sondern ein Betrag MIT Währung — immer zusammen
}

public record OrderId(Long value) {
    // "OrderId" kommuniziert: das ist nicht irgendeine Long — es ist ein Order-Identifier
    // Compiler verhindert: userId statt orderId zu übergeben
}
```

### 3.2 Tests als ausführbare Spezifikation

```java
// BDD-Stil: Tests beschreiben Verhalten in Domänensprache
@ExtendWith(MockitoExtension.class)
class OrderPlacementSpec {

    @Nested
    @DisplayName("When a customer places an order")
    class WhenPlacingOrder {

        @Test
        @DisplayName("then the order is created with PENDING_PAYMENT status")
        void orderCreatedWithPendingPaymentStatus() {
            // Given
            var command = PlaceOrderCommand.builder()
                .customerId(new CustomerId(42L))
                .items(List.of(new OrderItem(PRODUCT_A, quantity(2))))
                .build();

            // When
            var result = orderService.placeOrder(command);

            // Then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        }

        @Test
        @DisplayName("then an OrderPlacedEvent is published")
        void orderPlacedEventIsPublished() { /* ... */ }

        @Nested
        @DisplayName("with insufficient stock")
        class WithInsufficientStock {

            @Test
            @DisplayName("then an InsufficientStockException is thrown")
            void exceptionThrownWhenStockInsufficient() { /* ... */ }
        }
    }
}

// Maven Surefire generiert Testreport:
// "When a customer places an order"
//   ✅ then the order is created with PENDING_PAYMENT status
//   ✅ then an OrderPlacedEvent is published
//   "with insufficient stock"
//     ✅ then an InsufficientStockException is thrown
//
// DIESER Report IS die Spezifikation — immer aktuell!
```

---

## 4. Annotations als strukturierte Metadaten

### 4.1 Business-Rules als Code-Annotations

```java
// Custom-Annotation: Business-Regel direkt am Code
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessRule {
    String id();           // "RULE-42"
    String description();  // Lesbare Beschreibung
    String[] references(); // Verweis auf Anforderungs-Dokument, ADR
}

// Verwendung:
@BusinessRule(
    id          = "RULE-42",
    description = "Bestellungen über 10.000 EUR benötigen manuelle Freigabe",
    references  = {"https://wiki.example.com/rules/RULE-42", "ADR-023"}
)
public boolean requiresManualApproval(Order order) {
    return order.total().amount().compareTo(new BigDecimal("10000")) > 0;
}

@BusinessRule(
    id          = "RULE-07",
    description = "Österreichischer Steuersatz: 20% (§ 10 UStG AT)",
    references  = {"https://www.wko.at/steuern/umsatzsteuer"}
)
public TaxRate vatRateFor(Country country) {
    return switch (country) {
        case AT  -> TaxRate.of("0.20");
        case DE  -> TaxRate.of("0.19");
        default  -> TaxRate.of("0.20");  // EU-Standardsatz
    };
}
```

### 4.2 Automatische Dokumentationsgenerierung aus Annotations

```java
// Generator: liest alle @BusinessRule-Annotationen und erstellt Dokumentation
@Component
public class BusinessRuleDocumentationGenerator {

    public void generateDocumentation(String outputPath) throws IOException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(BusinessRule.class));

        var rules = scanner.findCandidateComponents("com.example")
            .stream()
            .flatMap(bd -> extractRulesFrom(bd.getBeanClassName()))
            .sorted(Comparator.comparing(r -> r.id()))
            .toList();

        // Markdown-Tabelle generieren
        var markdown = new StringBuilder();
        markdown.append("# Business Rules Dokumentation\n\n");
        markdown.append("*Automatisch generiert aus dem Quellcode.*\n\n");
        markdown.append("| ID | Beschreibung | Referenzen |\n");
        markdown.append("|---|---|---|\n");

        for (var rule : rules) {
            markdown.append("| %s | %s | %s |\n".formatted(
                rule.id(),
                rule.description(),
                String.join(", ", rule.references())
            ));
        }

        Files.writeString(Path.of(outputPath, "business-rules.md"),
            markdown.toString());

        System.out.println("Generated " + rules.size() + " business rules");
    }
}

// CI-Pipeline: Dokumentation bei jedem Build regenerieren
// docs/business-rules.md ist IMMER aktuell
```

---

## 5. Architektur als ausführbarer Code (Structurizr)

### 5.1 Architektur aus Code extrahieren

```java
// Statt: Architektur manuell in Structurizr DSL pflegen
// Besser: Architektur aus Annotations + Spring-Kontext extrahieren

@SpringBootApplication
@ArchitectureDocumentation(
    systemName = "E-Commerce Platform",
    description = "Bestellverwaltungssystem"
)
public class ECommerceApplication { }

// Auf jedem Service:
@Service
@ArchitectureComponent(
    name         = "OrderDomainService",
    description  = "Bestelllogik und Lifecycle-Management",
    technology   = "Plain Java, no Framework",
    adrs         = {"ADR-031", "ADR-023"},
    team         = "orders-team"
)
public class OrderDomainService { }

// Generator: baut Structurizr-Workspace aus Annotations
@Component
public class StructurizrExporter {

    public Workspace exportToStructurizr(ApplicationContext ctx) {
        var workspace = new Workspace("E-Commerce", "Living Architecture");
        var model = workspace.getModel();
        var system = model.addSoftwareSystem("E-Commerce Platform");

        // Spring Context analysieren
        ctx.getBeansWithAnnotation(ArchitectureComponent.class)
            .forEach((beanName, bean) -> {
                var annotation = bean.getClass()
                    .getAnnotation(ArchitectureComponent.class);
                var component = system.addContainer(annotation.name());
                component.setDescription(annotation.description());
                component.setTechnology(annotation.technology());
                // Beziehungen aus @Autowired-Feldern ableiten
            });

        return workspace;
    }
}
```

---

## 6. API-Dokumentation als Living Documentation

### 6.1 OpenAPI aus Code (Spring Doc)

```java
// Spring Doc: OpenAPI-Spezifikation AUTOMATISCH aus Controller-Annotations
@RestController
@RequestMapping("/api/v2/orders")
@Tag(name = "Orders", description = "Bestellverwaltung — Erstellen, Verfolgen, Stornieren")
public class OrderController {

    @PostMapping
    @Operation(
        summary     = "Bestellung aufgeben",
        description = "Legt eine neue Bestellung an. Idempotenz via Idempotency-Key-Header.",
        responses   = {
            @ApiResponse(responseCode = "201", description = "Bestellung erfolgreich angelegt",
                content = @Content(schema = @Schema(implementation = OrderCreatedResponse.class))),
            @ApiResponse(responseCode = "422", description = "Fachlicher Fehler (z.B. kein Bestand)",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    public ResponseEntity<OrderCreatedResponse> placeOrder(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Bestelldetails mit Artikeln und Lieferadresse")
            @Valid PlaceOrderRequest request,

            @RequestHeader(name = "Idempotency-Key", required = false)
            @Parameter(description = "UUID für idempotente Anfragen (24h TTL)")
            String idempotencyKey) {
        // ...
    }
}

// CI-Pipeline: OpenAPI-Spec generieren
// ./gradlew generateOpenApiDocs → docs/api/openapi.json
// Automatisch veröffentlicht auf API-Portal
// IMMER synchron mit dem Code
```

---

## 7. ADR-Index als Living Documentation

### 7.1 ADR-Verknüpfungen im Code

```java
// @ADR-Annotation: verknüpft Code mit Entscheidung
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ADR {
    String[] value();  // ADR-Nummern: {"ADR-031", "ADR-023"}
    String reason() default "";
}

// Verwendung:
@ADR({"ADR-031"})                          // Diese Klasse implementiert ADR-031
public interface PaymentGateway { }

@ADR(value = {"ADR-022"}, reason = "Circuit Breaker für Stripe-Calls")
public PaymentResult charge(Money amount) { }

@ADR({"ADR-073"})
@Index(name = "idx_orders_user_status")    // Index wegen Performance (ADR-073)
private OrderStatus status;
```

```java
// Generator: ADR-Code-Traceability-Matrix erstellen
@Component
public class AdrTraceabilityGenerator {

    public void generateTraceabilityMatrix(String outputPath) {
        var matrix = new TreeMap<String, List<String>>();

        // Alle Klassen scannen die @ADR-Annotation haben
        var scanner = createClassPathScanner();
        scanner.findCandidateComponents("com.example").forEach(bd -> {
            var clazz = loadClass(bd.getBeanClassName());
            var adrAnnotation = clazz.getAnnotation(ADR.class);
            if (adrAnnotation != null) {
                for (var adr : adrAnnotation.value()) {
                    matrix.computeIfAbsent(adr, k -> new ArrayList<>())
                        .add(clazz.getSimpleName());
                }
            }
        });

        // Markdown-Tabelle generieren
        var md = new StringBuilder("# ADR-Traceability-Matrix\n\n");
        md.append("Welche Klassen implementieren welche ADRs?\n\n");
        matrix.forEach((adr, classes) -> {
            md.append("## ").append(adr).append("\n");
            classes.forEach(c -> md.append("- `").append(c).append("`\n"));
        });

        Files.writeString(Path.of(outputPath, "adr-traceability.md"),
            md.toString());
    }
}
```

---

## 8. Dashboard: Architektur-Gesundheit auf einen Blick

```python
# scripts/architecture-health-dashboard.py
# Generiert täglichen Architektur-Gesundheitsbericht

import subprocess
import json
from dataclasses import dataclass

@dataclass
class HealthMetric:
    name: str
    value: str
    status: str  # GREEN, YELLOW, RED
    threshold: str

def collect_metrics() -> list[HealthMetric]:
    return [
        # Code-Qualität
        HealthMetric("Coverage",
            get_coverage_from_jacoco(),
            GREEN_IF_ABOVE(0.80), "≥ 80%"),

        # Architektur-Constraints
        HealthMetric("ArchUnit Violations",
            get_archunit_violations(),
            GREEN_IF_ZERO(), "= 0"),

        # Technische Schulden
        HealthMetric("Overdue Tech Debt",
            count_overdue_debt_annotations(),
            GREEN_IF_ZERO(), "= 0"),

        # Dokumentation
        HealthMetric("ADRs without Review Date",
            count_adrs_without_review(),
            GREEN_IF_ZERO(), "= 0"),

        # Conway-Alignment
        HealthMetric("Cross-Team Files (last 30d)",
            get_conway_violations(),
            GREEN_IF_BELOW(10), "< 10%"),
    ]

def generate_dashboard(metrics, output_path):
    md = "# Architecture Health Dashboard\n\n"
    md += f"*Generated: {datetime.now().isoformat()}*\n\n"
    md += "| Metrik | Wert | Status | Schwellwert |\n"
    md += "|--------|------|--------|-------------|\n"
    for m in metrics:
        emoji = "✅" if m.status == "GREEN" else "⚠️" if m.status == "YELLOW" else "❌"
        md += f"| {m.name} | {m.value} | {emoji} | {m.threshold} |\n"
    Path(output_path).write_text(md)
```

---

## 9. Das Living-Documentation-System im Überblick

```
Kontinuierlich generierte Dokumentation in docs/:

docs/
├── architecture/
│   ├── README.md                    ← arc42 (teilweise auto-generiert)
│   ├── diagrams/                    ← PlantUML (versioniert, CI-rendered)
│   ├── adr/                         ← ADRs (manuell, reviewt)
│   ├── adr-traceability.md         ← AUTO aus @ADR-Annotations
│   └── health-dashboard.md         ← AUTO täglich generiert
├── api/
│   └── openapi.json                 ← AUTO aus Spring Doc Annotations
├── events/
│   └── asyncapi.yaml               ← AUTO aus Avro-Schemas
├── business-rules.md                ← AUTO aus @BusinessRule-Annotations
├── quality-scenarios.yml            ← MANUELL (→ ADR-082)
└── teams/                           ← MANUELL (Team-APIs → ADR-085)

Generierungs-Pipeline (täglich + bei Commit):
  CI → generateDocs → commit to docs-branch → GitHub Pages
```

---

## 10. Quellen & Referenzen

- **Cyrille Martraire, "Living Documentation" (2019), Addison-Wesley** — Das Standardwerk; Klassifikation der Dokumentationsquellen, Patterns für automatische Generierung.
- **Dan North, "Introducing BDD" (2006)** — Behavior-Driven Development; Tests als ausführbare Spezifikation.
- **Simon Brown, "Visualise, Document and Explore Your Software Architecture" (2023)** — Structurizr als Living Architecture Toolkit.
- **Michael Nygard, "Documenting Architecture Decisions" (2011)** — ADRs als dauerhafte Dokumentation von Entscheidungen.
- **Kent Beck, "Test-Driven Development: By Example" (2002)** — Tests als primäre Entwicklungs- und Dokumentationsartefakte.
- **Martin Fowler, "Catalog of Patterns of Enterprise Application Architecture"** — Muster die durch ihre Namen dokumentieren.

---

## Akzeptanzkriterien

- [ ] Business-Rules-Dokumentation wird automatisch aus `@BusinessRule`-Annotations generiert
- [ ] OpenAPI-Spec in CI generiert und auf API-Portal veröffentlicht
- [ ] ADR-Traceability-Matrix wird automatisch aus `@ADR`-Annotations generiert
- [ ] Architecture Health Dashboard wird täglich generiert
- [ ] Test-Displaynames beschreiben Verhalten in Domänensprache (kein "test1", "shouldWork")
- [ ] Neue Entwickler können Architektur aus Code + generierter Doku verstehen (gemessen)

---

## Verwandte ADRs

- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ADRs im Code verankern
- [ADR-048](ADR-048-javadoc-standards.md) — JavaDoc als strukturierte Dokumentation
- [ADR-066](ADR-066-api-first-openapi.md) — OpenAPI als Living API Documentation
- [ADR-088](ADR-088-isaqb-werkzeuge-arc42.md) — arc42 als Dokumentations-Template
- [ADR-094](ADR-094-isaqb-guru-gesamtsynthese.md) — Guru-Level: Gesamtsynthese
