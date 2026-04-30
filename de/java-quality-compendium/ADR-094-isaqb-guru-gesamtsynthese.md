# ADR-094 — iSAQB Guru-Level: Gesamtsynthese — Die Einheit aller Prinzipien

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO                                                           |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2026-01-01 (jährlich)                                         |
| Kategorie         | iSAQB Expert/Guru · Gesamtsynthese                            |
| Betroffene Teams  | Alle Engineering-Teams — strategisches Fundament              |
| iSAQB-Abdeckung   | Foundation F1–F7 + Advanced + Expert vollständig              |

---

## 1. Die Gesamtschau: Wie alle Prinzipien zusammenwirken

```
Auf Guru-Niveau sieht man nicht mehr einzelne Prinzipien.
Man sieht die VERBINDUNGEN zwischen ihnen.

Das ist der Unterschied zwischen Wissen und Weisheit:
  Wissen:  "Conway's Law sagt, Architektur spiegelt Org-Struktur."
  Weisheit: "Also muss ich die Organisation ändern, bevor ich
             die Architektur ändern kann, was bedeutet, dass
             ich zuerst die Qualitätsziele explizit machen muss,
             damit Teams verstehen warum die Grenzen so sind,
             was das Onboarding verbessert, was die MTTR senkt,
             was das Error Budget schont, was Features ermöglicht."

Das ist ein einziger Gedankenstrang — nicht sechs separate Themen.
```

---

## 2. Das Meta-Modell: Wie alle ADRs zusammenhängen

```
STRATEGISCHE EBENE (Was wollen wir erreichen?):
  ADR-082: Qualitätsziele     → definiert den ZIELZUSTAND
  ADR-075: Entscheidungsprozess → regelt WIE wir entscheiden
  ADR-085: Sozio-technische Systeme → sichert dass ORGANISATION und ARCHITEKTUR aligned sind
  ADR-077: Microservices vs. Modulith → strategische Deployment-Entscheidung

ARCHITEKTURELLE EBENE (Wie strukturieren wir?):
  ADR-081: Was ist Architektur  → fundamentale Sprache
  ADR-083: Architekturmuster   → Strukturmuster für Qualitätsziele
  ADR-084: Entwurfsprinzipien  → Leitplanken für jede Entscheidung
  ADR-086: Querschnittskonzepte → systemweite Konsistenz
  ADR-079: Modulith            → konkrete Strukturentscheidung

TAKTISCHE EBENE (Wie implementieren wir?):
  ADR-001-074: Java/Spring Best Practices
  ADR-023: DDD Taktisch
  ADR-089: DDD Strategisch (Context Mapping)
  ADR-031: Hexagonale Architektur
  ADR-032: CQRS

EVOLUTIONS-EBENE (Wie bleibt es gesund?):
  ADR-087: Architekturbewertung (ATAM)
  ADR-090: Evolutionary Architecture
  ADR-061: Fitness Functions
  ADR-093: Living Documentation
  ADR-054: SLOs als Qualitätsmesssung

  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │  Strategisch  │────▶│ Architekturell│────▶│  Taktisch   │
  │  (Warum)      │◀────│  (Wie)        │     │  (Was/Code) │
  └──────────────┘     └──────────────┘     └──────────────┘
         ▲                    ▲                    ▲
         │                    │                    │
         └────────────────────┴────────────────────┘
                         Evolution
                     (Fitness Functions,
                      ATAM, Living Docs)
```

---

## 3. Die sieben Weisheiten des Architekten auf Guru-Niveau

### Weisheit 1: Architektur ist Kommunikation, nicht Diagramme

```
ANFÄNGER denkt: "Ich muss ein gutes Diagramm zeichnen."
EXPERTE denkt:  "Ich muss die richtigen Entscheidungen treffen."
GURU denkt:     "Ich muss sicherstellen, dass alle die richtigen
                 Entscheidungen verstehen — mit oder ohne Diagramme."

KONSEQUENZ:
  → ADR-Prozess (ADR-075) wichtiger als schöne C4-Diagramme
  → Team API (ADR-085) wichtiger als Architektur-Wiki
  → Fitness Functions (ADR-061) wichtiger als Architektur-Reviews
  
  Das beste Architektur-Diagramm ist wertlos wenn der Entwickler
  beim nächsten PR die Grenze verletzt weil er das Diagramm
  nicht gesehen hat. ArchUnit verhindert das automatisch.
```

### Weisheit 2: Qualitätsziele sind die eigentlichen Architektur-Entscheidungen

```
ANFÄNGER: "Wir nutzen Microservices." (Technologie-Entscheidung)
EXPERTE:  "Wir nutzen Microservices wegen Deployment-Autonomie." (Begründung)
GURU:     "Deployment-Autonomie ist wichtig weil Deployment-Frequenz
           unser primäres DORA-Ziel ist, was korreliert mit Customer-Value-
           Liefergeschwindigkeit, was unser strategisches Differenzierungs-
           merkmal ist."

KONSEQUENZ:
  → Qualitätsszenarien ZUERST (ADR-082)
  → Dann: welches Muster erfüllt das Szenario? (ADR-083)
  → Dann: wie wird das Muster implementiert? (ADR-001-074)
  
  Wenn die Reihenfolge umgekehrt wird, entsteht "Cargo-Cult-Architektur":
  Muster kopieren ohne zu verstehen warum.
```

### Weisheit 3: Conway's Law ist keine Empfehlung — es ist Physik

```
ANFÄNGER ignoriert Conway's Law.
EXPERTE kennt Conway's Law und handelt danach.
GURU versteht: Conway's Law ist unausweichlich wie Gravitation.

Die Frage ist nicht "befolgen wir Conway's Law?" sondern
"lassen wir Conway's Law uns zufällig formen oder bewusst?"

KONSEQUENZ:
  → Inverse Conway Maneuver (ADR-085) ist kein optionaler Best-Practice
  → Es ist die einzige Möglichkeit Architektur zu steuern
  → Teams reorganisieren ZUERST, dann Architektur ändern — nie umgekehrt
```

### Weisheit 4: Die teuerste Architektur-Entscheidung ist die implizite

```
ANFÄNGER: "Wir müssen nicht alles dokumentieren."
EXPERTE:  "Wir dokumentieren wichtige Entscheidungen als ADRs."
GURU:     "Jede undokumentierte Entscheidung kostet Geld — entweder
           durch Wiederholung der Diskussion oder durch falsche
           Implementierung die davon ausgeht, die Entscheidung
           wäre anders gefallen."

IBM-Studie: Kommunikationsaufwand für nicht-dokumentierte Entscheidungen
kostet durchschnittlich 4.2 Stunden pro Woche pro Entwickler.
Bei 25 Entwicklern: 105 Stunden/Woche = 2.625 Stunden/Monat = 210.000 EUR/Jahr

KONSEQUENZ:
  → ADR-Prozess (ADR-075) ist nicht Overhead — er ersetzt teurere Alternativen
  → Jede Entscheidung die zweimal diskutiert wird hätte dokumentiert werden sollen
```

### Weisheit 5: Architektur optimiert — du entscheidest was

```
ANFÄNGER: "Gute Architektur ist X."
EXPERTE:  "Gute Architektur erfüllt Qualitätsziele Y."
GURU:     "Gute Architektur optimiert bewusst auf Y und akzeptiert
           bewusst die Kosten für Z."

JEDE Architektur hat Trade-offs. Es gibt keine perfekte Architektur.
Es gibt nur gut-begründete Entscheidungen im Kontext der Qualitätsziele.

MICROSERVICES optimieren auf: Team-Autonomie, Deployment-Unabhängigkeit
KOSTEN: verteilte Komplexität, Netzwerk-Overhead, Operational-Burden

MODULITH optimiert auf: Entwickler-Produktivität, einfacher Betrieb
KOSTEN: gemeinsames Deployment, horizontale Skalierung nur gesamt

BEIDE können "gut" sein — abhängig vom Kontext.
Ein Guru kann für beide argumentieren. Ein Anfänger hat eine Religion.
```

### Weisheit 6: Die beste Architektur ist die einfachste die funktioniert

```
YAGNI auf Architektur-Ebene:
  → Nicht: "wir könnten mal Microservices brauchen"
  → Ja:    "wir haben jetzt dieses konkrete Problem"

EVOLUTIONÄRES DESIGN (ADR-090):
  Architektur kann sich ändern — wenn Fitness Functions die Änderung
  sicher machen. Daher: heute die einfachste Lösung, morgen evolving.

STRANGLER FIG (ADR-090):
  Wenn Architektur geändert werden muss: inkrementell, nicht Big Bang.
  Ein Big Bang Rewrite scheitert statistisch. (Joel Spolsky, 2000)

KONSEQUENZ:
  → Modulith jetzt (ADR-079)
  → Service-Extraktion wenn konkreter Trigger erfüllt (ADR-077)
  → Reactive wenn konkreter Use-Case es verlangt (ADR-091)
  → GraalVM wenn konkreter Vorteil messbar (ADR-071)
```

### Weisheit 7: Architektur ist ein sozialer Prozess

```
TECHNISCHE WAHRHEIT:
  Die technisch beste Architektur die nicht vom Team
  akzeptiert und verstanden wird, ist schlechter als
  eine sub-optimale Architektur die das Team besitzt und pflegt.

SOZIALE WAHRHEIT:
  → Architecture Guild > einzelner Architekt
  → ADR-Reviews > Architecture Decisions ex Cathedra
  → Fitness Functions > manuelle Code-Reviews
  → Team Topologies > Org-Chart-Optimierung

"The architect's job isn't to make all the decisions. It's to
create conditions where good decisions emerge naturally."
— Michael Nygard
```

---

## 4. Der vollständige iSAQB-Lehrplan: Übersicht aller ADRs

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    iSAQB-KOMPENDIUM VOLLSTÄNDIG                              │
├──────────────────────────────────┬──────────────────────────────────────────┤
│ FOUNDATION LEVEL (F1-F7)         │ ADR                                      │
├──────────────────────────────────┼──────────────────────────────────────────┤
│ F1: Was ist Softwarearchitektur? │ ADR-081                                  │
│ F2: Qualitätsziele & ISO 25010   │ ADR-082                                  │
│ F3: Architekturmuster & -stile   │ ADR-083                                  │
│ F4: Entwurfsprinzipien           │ ADR-084 + ADR-025 + ADR-026              │
│ F5: Querschnittskonzepte         │ ADR-086                                  │
│ F6: Architekturbewertung (ATAM)  │ ADR-087                                  │
│ F7: Werkzeuge (arc42, C4, UML)   │ ADR-088                                  │
├──────────────────────────────────┼──────────────────────────────────────────┤
│ ADVANCED LEVEL                   │ ADR                                      │
├──────────────────────────────────┼──────────────────────────────────────────┤
│ Sozio-technische Systeme         │ ADR-085                                  │
│ Strategic DDD & Context Mapping  │ ADR-089 + ADR-023                        │
│ Evolutionary Architecture        │ ADR-090                                  │
│ Reactive Architecture            │ ADR-091                                  │
│ Cloud-Native Architecture        │ ADR-092                                  │
├──────────────────────────────────┼──────────────────────────────────────────┤
│ EXPERT / GURU LEVEL              │ ADR                                      │
├──────────────────────────────────┼──────────────────────────────────────────┤
│ Living Documentation             │ ADR-093                                  │
│ Gesamtsynthese                   │ ADR-094 (dieser ADR)                     │
│ Architekturprozess (Meta)        │ ADR-075                                  │
│ Technology Radar                 │ ADR-076                                  │
│ Microservices-Entscheidungsrahmen│ ADR-077                                  │
│ Datenstrategie                   │ ADR-078                                  │
│ Modulith (ausführlich)           │ ADR-079                                  │
│ GitLab CI/CD & DevSecOps         │ ADR-080                                  │
└──────────────────────────────────┴──────────────────────────────────────────┘
```

---

## 5. Architekt-Reifegrad-Modell

```
LEVEL 1 — IMPLEMENTIERER (Code schreiben):
  Kennt: Java, Spring, Unit Tests
  Sieht: Klassen, Methoden
  Entscheidungsebene: "Wie implementiere ich X?"

LEVEL 2 — DESIGNER (Design Patterns):
  Kennt: GoF-Patterns, SOLID, Clean Code
  Sieht: Klassen-Beziehungen, Module
  Entscheidungsebene: "Welches Muster löst dieses Problem?"

LEVEL 3 — ARCHITEKT (System-Design):
  Kennt: Architekturmuster, DDD, Qualitätsziele
  Sieht: System-Grenzen, Schichten, Services
  Entscheidungsebene: "Welche Architektur erfüllt die Qualitätsziele?"

LEVEL 4 — SENIOR-ARCHITEKT (Trade-offs):
  Kennt: ATAM, Conway's Law, Team Topologies
  Sieht: Organisations-Architektur-Alignment
  Entscheidungsebene: "Wie passen Architektur und Organisation zusammen?"

LEVEL 5 — GURU (Systemdenken):
  Kennt: Alles oben + technologische Trends + Business-Kontext
  Sieht: Wie Architektur-Entscheidungen Business-Outcomes treiben
  Entscheidungsebene: "Welche Architektur-Strategie ermöglicht die Business-Strategie?"

SELBSTTEST (Level 5):
  Kannst du erklären WIE die heutige Architektur-Entscheidung
  die MTTR in 6 Monaten senken wird, was das Error-Budget schont,
  was Team-Autonomie ermöglicht, was Deployment-Frequenz erhöht,
  was schnellere Feature-Lieferung ermöglicht,
  was Customer-Retention verbessert,
  was Revenue steigert?
  
  Wenn ja: Level 5.
  Wenn nein: weitere Ebenen fehlen noch.
```

---

## 6. Das Architektur-Manifest dieses Kompendiums

```
WIR GLAUBEN:

  Architektur ist ein kontinuierlicher Prozess, kein Einmal-Artefakt.
  
  Qualitätsziele sind wichtiger als Technologie-Präferenzen.
  
  Conway's Law ist Physik — ignorieren kostet mehr als respektieren.
  
  Die beste Architektur ist die einfachste die funktioniert
  und sich kontrolliert weiterentwickeln kann.
  
  Explizite Entscheidungen (ADRs) sind billiger als implizite.
  
  Maschinell prüfbare Architektur (ArchUnit) ist besser als
  dokumentierte Architektur die niemand kennt.
  
  Teams die ihre Domäne besitzen sind produktiver als Teams
  die auf andere warten.
  
  Messbare Qualität (SLOs, DORA) ist besser als subjektive Qualität.
  
  Dokumentation die aus Code generiert wird kann nicht veralten.

WIR ENTSCHEIDEN:
  Immer mit expliziter Begründung.
  Immer mit dokumentierten Alternativen.
  Immer mit Review-Datum.
  Immer mit messbaren Akzeptanzkriterien.
```

---

## 7. Das vollständige ADR-Kompendium: Themen-Index

```
JAVA 21 SPRACHE:              ADR-001–005
SPRING BOOT ARCHITEKTUR:      ADR-006
CODE-QUALITÄT:                ADR-007–009
JUNIT & TESTING:              ADR-010–014
SECURITY:                     ADR-015, ADR-040
DATENBANK:                    ADR-016, ADR-034, ADR-073
OBSERVABILITY:                ADR-017, ADR-054
INTEGRATION & CONTRACT TESTS: ADR-018–020
API DESIGN:                   ADR-021, ADR-053, ADR-066, ADR-067
RESILIENCE:                   ADR-022
DDD TAKTISCH:                 ADR-023
CACHING:                      ADR-024
DESIGN-PRINZIPIEN:            ADR-025–027, ADR-052, ADR-084
ENTWURFSMUSTER (GOF):         ADR-028–030
HEXAGONALE ARCHITEKTUR:       ADR-031
CQRS:                         ADR-032
CONCURRENCY:                  ADR-033
PRIVACY/GDPR:                 ADR-035
CI/CD:                        ADR-036, ADR-080
CONTAINER & KUBERNETES:       ADR-037–038
FEATURE FLAGS:                ADR-039
MESSAGING/KAFKA:              ADR-041, ADR-042
PERFORMANCE & JVM:            ADR-043–044
TESTING ADVANCED:             ADR-045–046
DOKUMENTATION:                ADR-047–048, ADR-088, ADR-093
CODE REVIEW & PROZESS:        ADR-049–051
PERSISTENZ & MIGRATION:       ADR-016, ADR-034
GRAPHQL:                      ADR-053
EVENT SOURCING:                ADR-055
MODULITH:                     ADR-056, ADR-079
SBOM & SUPPLY CHAIN:          ADR-057
MULTI-TENANCY:                ADR-058
DEPLOYMENT-STRATEGIEN:        ADR-059
INFRASTRUCTURE AS CODE:       ADR-060
FITNESS FUNCTIONS:            ADR-061, ADR-090
RATE LIMITING:                ADR-062
BACKUP & DR:                  ADR-063
VERSIONIERUNG:                ADR-064
SAGA:                         ADR-065
GRPC:                         ADR-067
SPRING BATCH:                 ADR-068
CONFIGURATION:                ADR-069
ZERO TRUST:                   ADR-070
GRAALVM:                      ADR-071
CHAOS ENGINEERING:            ADR-072
SCHEDULED JOBS:               ADR-074
GOVERNANCE & PROZESS:         ADR-075–076
MICROSERVICES VS. MODULITH:   ADR-077
DATENSTRATEGIE:               ADR-078
SOZIO-TECHNISCH:              ADR-085
QUERSCHNITTSKONZEPTE:         ADR-086
ARCHITEKTURBEWERTUNG:         ADR-087
CONTEXT MAPPING:              ADR-089
EVOLUTIONARY ARCHITECTURE:    ADR-090
REACTIVE ARCHITECTURE:        ADR-091
CLOUD-NATIVE:                 ADR-092
iSAQB FOUNDATION F1-F7:       ADR-081–088
iSAQB ADVANCED:               ADR-089–093
iSAQB GURU:                   ADR-094

TOTAL: 94 Architecture Decision Records
```

---

## 8. Abschlussbewertung: Was ein Guru anders macht

```
EIN GURU wird gefragt: "Was ist bessere Architektur: Microservices oder Monolith?"

EIN ANFÄNGER antwortet: "Microservices wegen Skalierbarkeit."

EIN EXPERTE antwortet: "Kommt drauf an. Bei großen Teams: Microservices.
                        Bei kleinen Teams: Modulith."

EIN GURU antwortet: "Diese Frage hat keine sinnvolle Antwort ohne Kontext.
                     Zeig mir:
                     ① Deine Qualitätsziele (→ ADR-082)
                     ② Deine Team-Struktur (→ ADR-085)
                     ③ Deine aktuellen Schmerzen (→ gemessen mit DORA)
                     ④ Deine mittelfristige Team-Wachstumsprognose
                     
                     Dann sage ich dir nicht nur welche Architektur besser ist,
                     sondern auch: welchen Schritt jetzt, welchen in 6 Monaten,
                     welche Risiken dabei, und wie wir messen ob wir richtig liegen."

Das IST der Unterschied zwischen Wissen und Weisheit.
```

---

## 9. Quellen: Das Gesamtwerk hinter diesem Kompendium

**Grundlegende Werke:**
- Eric Evans, "Domain-Driven Design" (2003)
- Martin Fowler, "Patterns of Enterprise Application Architecture" (2002)
- Robert C. Martin, "Clean Architecture" (2017)
- Fred Brooks, "The Mythical Man-Month" (1975)

**Architektur-Methodik:**
- Len Bass, Paul Clements, Rick Kazman, "Software Architecture in Practice" (2021)
- Neal Ford, Rebecca Parsons, "Building Evolutionary Architectures" (2017)
- Gregor Hohpe, "The Software Architect Elevator" (2020)
- Frank Buschmann et al., "Pattern-Oriented Software Architecture" (1996)

**Organisation & Teams:**
- Matthew Skelton & Manuel Pais, "Team Topologies" (2019)
- Nicole Forsgren, Jez Humble, Gene Kim, "Accelerate" (2018)
- Melvin Conway, "How Do Committees Invent?" (1968)

**Cloud & DevOps:**
- Sam Newman, "Building Microservices" (2022)
- Gene Kim et al., "The DevOps Handbook" (2016)
- Brendan Burns et al., "Kubernetes: Up and Running" (2022)

**Java & Spring:**
- Venkat Subramaniam, "Pragmatic Concurrency with Java" (2023)
- iSAQB, "CPSA-F Curriculum" (2023)
- Spring Documentation (aktuelle Version)

---

## Akzeptanzkriterien

- [ ] Alle 94 ADRs sind dokumentiert und haben Review-Datum
- [ ] Architektur-Board trifft sich quartalsweise (→ ADR-090)
- [ ] DORA-Metriken werden gemessen und sind sichtbar (→ ADR-054)
- [ ] Neuer Tech Lead kann nach 1 Woche eigenständig Architektur-Entscheidungen begründen
- [ ] Conway-Alignment-Score wird monatlich gemessen (→ ADR-085)
- [ ] Fitness-Functions-Suite läuft grün bei jedem Commit

---

## Verwandte ADRs

Dieser ADR ist die Synthese aller vorherigen ADRs.
Startpunkt für Einsteiger: ADR-081 (Was ist Softwarearchitektur?)
Startpunkt für Teams: ADR-075 (Entscheidungsprozess) + ADR-085 (Conway's Law)
Startpunkt für CTO-Level: ADR-077 (Microservices/Modulith) + ADR-078 (Datenstrategie)
