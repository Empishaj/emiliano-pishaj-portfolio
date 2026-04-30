# ADR-076 — Technology Radar: Stack-Entscheidungen & Technologie-Lifecycle

| Feld              | Wert                                              |
|-------------------|---------------------------------------------------|
| Status            | ✅ Akzeptiert                                     |
| Entscheider       | CTO / Architektur-Board                           |
| Datum             | 2024-01-01                                        |
| Review-Datum      | 2025-01-01 (jährlich)                             |
| Kategorie         | Governance / Technologie-Strategie                |
| Betroffene Teams  | Alle Engineering-Teams, Engineering-Manager       |

---

## Kontext & Treiber

Ohne explizite Technologiestrategie entstehen "Technologie-Zoos": jedes Team nutzt andere Frameworks, Libraries und Tools. Das führt zu:
- Wissenssilos: Experte kündigt, Know-how geht verloren
- Integrationsprobleme: inkompatible Technologien in gemeinsam genutzten Schichten
- Erhöhter Cognitive Load: Entwickler müssen N verschiedene Ökosysteme kennen
- Lizenzrisiken: ungeprüfte Open-Source-Lizenzen (GPL in kommerziellem Produkt)
- CVE-Blindfleck: unbekannte Abhängigkeiten werden nicht gescannt (→ ADR-057)

**Qualitätsziele unter Druck:** Wartbarkeit, Team-Effizienz, Security, Kosten

---

## Entscheidung

Der Engineering-Stack wird in vier Kategorien mit expliziten Lifecycle-Regeln geführt, analog zum ThoughtWorks Technology Radar. Neue Technologien im ADOPT-Ring werden standardisiert, HOLD-Ring-Technologien werden in neuen Projekten nicht verwendet.

**Aktueller Stack (Java 21 / Spring Boot 3.x Ökosystem):**

| Ring    | Technologie               | Kategorie     | Seit    | Notiz                                         |
|---------|---------------------------|---------------|---------|-----------------------------------------------|
| ADOPT   | Java 21 LTS               | Sprache       | Q1/2024 | Virtual Threads, Records, Pattern Matching    |
| ADOPT   | Spring Boot 3.x           | Framework     | Q1/2024 | Native Image Support, Observability           |
| ADOPT   | PostgreSQL 16             | Datenbank     | Q1/2024 | JSONB, Logical Replication, pg_partman        |
| ADOPT   | Kafka 3.x                 | Messaging     | Q1/2024 | KRaft (kein Zookeeper), Streams               |
| ADOPT   | Kubernetes 1.28+          | Infra         | Q1/2024 | Gateway API, Structured Auth                  |
| ADOPT   | Gradle 8.x + KTS          | Build         | Q1/2024 | Version Catalog, Configuration Cache          |
| ADOPT   | Testcontainers 1.19+      | Testing       | Q1/2024 | Reuse Mode, Cloud-Backend                     |
| TRIAL   | GraalVM Native Image      | Runtime       | Q2/2024 | Für Serverless/Lambda, noch nicht für alle    |
| TRIAL   | Spring Modulith           | Architektur   | Q2/2024 | Bewährt sich im aktuellen Projekt             |
| TRIAL   | Argo Rollouts             | CD            | Q2/2024 | Canary-Deployment-Strategie wird evaluiert    |
| ASSESS  | Virtual Threads + Loom    | Concurrency   | Q1/2024 | Im ADOPT wenn Pinning-Probleme gelöst         |
| ASSESS  | OpenTelemetry SDK         | Observability | Q2/2024 | Vendor-neutral Tracing, noch reifend          |
| HOLD    | Spring WebFlux / Reactor  | Framework     | seit ADR-004 | Virtual Threads ersetzen reaktiven Stack    |
| HOLD    | Lombok                    | Code-Gen      | seit ADR-001 | Records und moderne Java-Features ersetzen  |
| HOLD    | EJB / Legacy JEE          | Framework     | immer   | Nicht diskutierbar                            |

---

## Begründung

**Referenzen:**
- ThoughtWorks Technology Radar — Methodik der vier Ringe (Adopt/Trial/Assess/Hold) als bewährtes Framework für Technologie-Governance in Engineering-Organisationen.
- Martin Fowler, "Is High Quality Software Worth the Cost?" (2019) — technische Qualität und Technologie-Konsolidierung sind wirtschaftlich nachweisbar vorteilhaft.
- Neal Ford, Rebecca Parsons, "Fundamentals of Software Architecture" (2020), Kap. 3 — Architektur-Entscheidungen müssen explizit sein, implizite Entscheidungen sind die gefährlichsten.
- DORA State of DevOps 2023 — Teams mit geringer Technologie-Diversität haben 2,1× bessere Deployment-Stabilität.

---

## Alternativen & Warum sie abgelehnt wurden

| Alternative                    | Stärken                        | Schwächen                                   | Ablehnungsgrund                                        |
|-------------------------------|-------------------------------|---------------------------------------------|-------------------------------------------------------|
| Freie Technologiewahl pro Team | Maximale Autonomie             | Technologie-Zoo, Integrationsprobleme       | DORA: erhöhte Change-Failure-Rate bei hoher Diversität |
| Komplett standardisierter Stack | Maximale Konsistenz          | Innovation blockiert, keine Experimente     | Teams können nicht experimentieren → Talent-Abwanderung|
| Jährliche komplette Neubewertung | Immer aktuell              | Zu viel Instabilität, ständige Migrationen  | Migrations-Kosten überwiegen den Nutzen               |

---

## Trade-off-Matrix

| Qualitätsziel         | Technology Radar (gewählt) | Freie Wahl | Komplett standardisiert |
|-----------------------|---------------------------|------------|-------------------------|
| Innovation            | ⭐⭐⭐⭐                   | ⭐⭐⭐⭐⭐  | ⭐⭐                    |
| Konsistenz            | ⭐⭐⭐⭐                   | ⭐⭐        | ⭐⭐⭐⭐⭐               |
| Team-Autonomie        | ⭐⭐⭐⭐                   | ⭐⭐⭐⭐⭐  | ⭐⭐                    |
| Sicherheit (CVE)      | ⭐⭐⭐⭐⭐                  | ⭐⭐        | ⭐⭐⭐⭐⭐               |
| Onboarding-Geschw.    | ⭐⭐⭐⭐⭐                  | ⭐⭐        | ⭐⭐⭐⭐⭐               |

---

## Kosten-Nutzen-Analyse

**Initiale Kosten:**
- Radar-Erstellung und Review: 3 Personentage Architektur-Board
- Kommunikation an alle Teams: 1 Personentag
- HOLD-Technologien migrieren: variiert (Lombok-Migration: ~5 PT je nach Codebase-Größe)

**Laufende Kosten:**
- Quartalsweises Radar-Update: 4 × 4 Stunden Architektur-Board = 2 Personentage/Jahr
- CVE-Scanning (→ ADR-057) nur für Stack-Technologien: vollständig automatisiert

**Erwarteter Nutzen:**
- Onboarding neuer Entwickler: −40% Zeit durch konsistente Toolchain (messbar via Onboarding-Surveys)
- CVE-Response-Zeit: −80% durch bekannten, gescannten Stack
- Interview-Attraktion: +15% bei Kandidaten die modernen Java-Stack (Java 21, Virtual Threads) bevorzugen

**Break-Even:** 6 Monate — dann sind die Effizienzgewinne durch Konsistenz messbar.

---

## Konsequenzen

**Sofort:**
- `docs/tech-radar/` im Repository mit aktuellem Stand
- Renovate Bot (→ ADR-057) konfiguriert nur für Radar-ADOPT-Technologien
- Neue Services dürfen HOLD-Technologien nicht neu einführen — ArchUnit-Test (→ ADR-061)

**Mittelfristig:**
- TRIAL-Technologien werden nach 6 Monaten in ADOPT oder HOLD klassifiziert
- Lombok-Migration: schrittweise, kein Freeze anderer Features während Migration

**Risiken:**
- **Radar veraltet**: Wahrscheinlichkeit H ohne Prozess, M mit. Mitigation: festes Quartals-Review, Architektur-Board-Verantwortlichkeit.
- **Team-Widerstand gegen HOLD**: Wahrscheinlichkeit M, Auswirkung M. Mitigation: Begründung kommunizieren, Migrationspfad anbieten.

---

## Akzeptanzkriterien

- [ ] `docs/tech-radar/radar.md` existiert und ist im Git-Repository
- [ ] ArchUnit-Test: HOLD-Technologien tauchen in keinem neuen Modul auf
- [ ] Nächstes Radar-Review ist im Team-Kalender eingetragen
- [ ] Alle Teams kennen den aktuellen Radar-Stand (Team-Meetings dokumentiert)

---

## Verwandte ADRs

- [ADR-075](ADR-075-architektur-entscheidungsprozess.md) — übergeordneter Entscheidungsprozess
- [ADR-057](ADR-057-sbom-dependency-auditing.md) — CVE-Scanning für Stack-Technologien
- [ADR-061](ADR-061-fitness-functions.md) — ArchUnit für HOLD-Durchsetzung
