# ADR-047 — Architekturdokumentation mit dem C4-Modell

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Structurizr · PlantUML       |
| Datum      | 2024-01-01                        |
| Kategorie  | Dokumentation / Architektur       |

---

## Kontext & Problem

Architekturdokumentation veraltet sofort wenn sie manuell gepflegt wird. Diagramme ohne klares Abstraktionsniveau verwirren mehr als sie helfen. Das C4-Modell (Simon Brown) löst beides: vier definierte Abstraktionsebenen, und mit Structurizr kann die Architektur als Code gepflegt werden — versioniert, reviewbar, automatisch gerendert.

---

## Die vier C4-Ebenen

```
Level 1: System Context  — Wer nutzt das System? Welche externen Systeme?
Level 2: Container       — Welche deployable Einheiten? (Services, DBs, Queue)
Level 3: Component       — Welche Komponenten in einem Container?
Level 4: Code            — Klassen, Interfaces (nur bei Bedarf, meist UML)
```

---

## Structurizr DSL: Architektur als Code

```kotlin
// workspace.dsl (Structurizr DSL)
workspace "E-Commerce Plattform" "Architektur nach C4-Modell" {

    model {
        // ── Level 1: Personen und externe Systeme ─────────────────
        customer = person "Kunde" "Kauft Produkte online"
        admin    = person "Administrator" "Verwaltet Produkte und Bestellungen"

        emailProvider = softwareSystem "SendGrid" "E-Mail-Versand" {
            tags "External"
        }
        paymentGateway = softwareSystem "Stripe" "Zahlungsabwicklung" {
            tags "External"
        }

        // ── Unser System ───────────────────────────────────────────
        ecommerce = softwareSystem "E-Commerce Plattform" "Kernplattform" {

            // ── Level 2: Container ────────────────────────────────
            webApp = container "Web Application" {
                description "React SPA"
                technology  "TypeScript, React 18"
                tags        "Frontend"
            }

            apiGateway = container "API Gateway" {
                description "Routing, Rate Limiting, Auth-Proxy"
                technology  "Spring Cloud Gateway"
            }

            orderService = container "Order Service" {
                description "Bestellungen, Checkout, Zahlungen"
                technology  "Java 21, Spring Boot 3.x"

                // ── Level 3: Komponenten ──────────────────────────
                orderController = component "OrderController" {
                    description "REST-Endpunkte für Bestellungen"
                    technology  "Spring MVC @RestController"
                }
                orderDomainService = component "OrderDomainService" {
                    description "Business-Logik (Hexagonal Port)"
                    technology  "Plain Java — kein Framework"
                }
                orderRepository = component "OrderJpaRepository" {
                    description "Persistenz-Adapter"
                    technology  "Spring Data JPA"
                }
                stripeAdapter = component "StripePaymentAdapter" {
                    description "Zahlungs-Adapter (Outbound Port)"
                    technology  "Stripe Java SDK"
                }
            }

            productService = container "Product Service" {
                description "Produktkatalog, Suche, Bestände"
                technology  "Java 21, Spring Boot 3.x"
            }

            notificationService = container "Notification Service" {
                description "Email- und Push-Benachrichtigungen"
                technology  "Java 21, Spring Boot 3.x"
            }

            orderDb = container "Order Database" {
                description "Bestellungen und Zahlungen"
                technology  "PostgreSQL 16"
                tags        "Database"
            }

            kafka = container "Event Bus" {
                description "Async Kommunikation zwischen Services"
                technology  "Apache Kafka 3.x"
                tags        "Queue"
            }

            authServer = container "Authorization Server" {
                description "OAuth2/OIDC"
                technology  "Keycloak 23"
                tags        "Security"
            }
        }

        // ── Beziehungen Level 1 ───────────────────────────────────
        customer     -> ecommerce        "Kauft, verwaltet Konto"
        admin        -> ecommerce        "Verwaltet Produkte"
        ecommerce    -> emailProvider    "Sendet Emails via HTTPS"
        ecommerce    -> paymentGateway   "Verarbeitet Zahlungen via HTTPS"

        // ── Beziehungen Level 2 ───────────────────────────────────
        customer     -> webApp           "HTTPS"
        webApp       -> apiGateway       "REST/HTTPS"
        apiGateway   -> orderService     "REST/HTTPS"
        apiGateway   -> productService   "REST/HTTPS"
        apiGateway   -> authServer       "Validiert Tokens"
        orderService -> orderDb          "Liest/Schreibt" "JDBC"
        orderService -> kafka            "Publiziert Events"
        notificationService -> kafka     "Konsumiert Events"
        notificationService -> emailProvider "HTTPS"
        orderService -> paymentGateway   "Verarbeitet Zahlung" "HTTPS"

        // ── Beziehungen Level 3 (Order Service) ───────────────────
        orderController     -> orderDomainService "Ruft auf"
        orderDomainService  -> orderRepository    "Persistiert via Port"
        orderDomainService  -> stripeAdapter      "Zahlt via Port"
        orderRepository     -> orderDb            "SQL"
        stripeAdapter       -> paymentGateway     "HTTPS"
    }

    // ── Views ─────────────────────────────────────────────────────
    views {
        systemContext ecommerce "SystemContext" {
            include *
            autoLayout lr
        }

        container ecommerce "Containers" {
            include *
            autoLayout lr
        }

        component orderService "OrderServiceComponents" {
            include *
            autoLayout tb
        }

        // Deployment-Diagramm
        deployment ecommerce "Production" "ProductionDeployment" {
            include *
            autoLayout lr
        }

        // Stile
        styles {
            element "Person"    { shape Person;   background "#08427b"; color "#ffffff" }
            element "External"  { background "#999999"; color "#ffffff" }
            element "Database"  { shape Cylinder }
            element "Queue"     { shape Pipe }
            element "Frontend"  { background "#85bbf0" }
            element "Security"  { background "#f0a500"; color "#000000" }
        }
    }
}
```

---

## Structurizr in der CI-Pipeline rendern

```yaml
# .github/workflows/docs.yml
- name: Render Architecture Diagrams
  run: |
    docker run --rm -v $(pwd):/workspace \
      structurizr/cli:latest \
      export -workspace workspace.dsl \
             -format plantuml \
             -output docs/architecture/

- name: Publish to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: docs/architecture
```

---

## Alternative: PlantUML für einfache Diagramme

```plantuml
@startuml OrderService-Component
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Component.puml

title Komponenten: Order Service

Container_Boundary(orderService, "Order Service") {
    Component(ctrl,   "OrderController",     "Spring MVC",    "REST API")
    Component(svc,    "OrderDomainService",  "Plain Java",    "Business-Logik")
    Component(repo,   "OrderJpaRepository",  "Spring Data",   "Persistenz-Adapter")
    Component(stripe, "StripeAdapter",       "Stripe SDK",    "Zahlungs-Adapter")
}

ContainerDb(db,    "Order DB",     "PostgreSQL")
System_Ext(stripe_ext, "Stripe",  "Zahlungsabwicklung")

Rel(ctrl,   svc,    "Ruft auf")
Rel(svc,    repo,   "via Port")
Rel(svc,    stripe, "via Port")
Rel(repo,   db,     "JDBC")
Rel(stripe, stripe_ext, "HTTPS")

@enduml
```

---

## ADR-Verknüpfung in Diagrammen

```kotlin
// In Structurizr DSL: ADRs als Eigenschaften
orderDomainService = component "OrderDomainService" {
    description "Business-Logik Kern"
    properties {
        "adr" "ADR-031 (Hexagonal Architecture), ADR-023 (DDD)"
        "owner" "Team Backend"
        "status" "Active"
    }
}
```

---

## Konsequenzen

**Positiv:** Architektur als Code = versioniert, reviewbar, nie veraltet (wenn Disziplin vorhanden). Vier klare Ebenen lösen "zu abstrakt / zu detailliert"-Problem. Structurizr kann Diagramme automatisch generieren.

**Negativ:** Initiale Einarbeitung in DSL-Syntax. Diagramme müssen bei Architekturänderungen aktiv aktualisiert werden — automatische Synchronisierung nur mit Werkzeugunterstützung.

---

## 💡 Guru-Tipps

- **Nicht alle Level immer zeigen**: Level 1 für Stakeholder, Level 2 für Dev-Teams, Level 3 nur für komplexe Services.
- **"Living Architecture"**: `workspace.dsl` ins Repository, Rendering in CI → immer aktuell in GitHub Pages.
- **Entscheidungen ≠ Beschreibung**: C4 beschreibt was ist. ADRs (→ ADR-009) erklären warum.
- **Structurizr Lite** für lokale Entwicklung: `docker run -p 8080:8080 structurizr/lite`.

---

## Verwandte ADRs

- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ADRs im Code — C4 als Übersichtsdokumentation.
- [ADR-031](ADR-031-hexagonal-architecture.md) — Hexagonal Architecture im C4-Komponentendiagramm sichtbar.
