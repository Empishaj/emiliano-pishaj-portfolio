# TOGAF ADM – Alle Phasen im Detail

> **Quelle:** TOGAF® Standard, 10th Edition – Architecture Development Method (ADM)
> Das ADM ist ein **iterativer Prozess**. Es gibt keine starre Reihenfolge. Es ist kein Wasserfall-Modell. Es ist ein **Referenzmodell**, das beschreibt, was getan werden muss, um eine Enterprise Architecture aufzubauen und zu pflegen.

---

## Überblick: Alle ADM-Phasen

| Phase | Name | Kernfrage |
|---|---|---|
| **Preliminary** | Vorbereitung | Sind wir bereit für Architekturarbeit? |
| **A** | Architecture Vision | Was wollen wir erreichen? |
| **B** | Business Architecture | Wie sieht unser Geschäft aus? |
| **C** | Information Systems Architectures | Welche Daten und Apps brauchen wir? |
| **D** | Technology Architecture | Auf welcher Technik bauen wir auf? |
| **E** | Opportunities & Solutions | Welche Lösungen gibt es? |
| **F** | Migration Planning | Wie kommen wir von A nach B? |
| **G** | Implementation Governance | Wird alles richtig umgesetzt? |
| **H** | Architecture Change Management | Wie steuern wir Änderungen? |
| **RM** | Requirements Management | Wie verwalten wir Anforderungen laufend? |

---

---

# Preliminary Phase – Vorbereitung

---

## Was ist das Ziel dieser Phase?

Die Preliminary Phase ist der **Startpunkt vor dem eigentlichen ADM-Zyklus**. Hier wird alles vorbereitet, was gebraucht wird, damit Architekturarbeit überhaupt funktionieren kann. Man baut sozusagen das Fundament, bevor man das Haus baut.

Ohne diese Phase fehlen dem Unternehmen die Werkzeuge, Rollen, Regeln und Prozesse, die für konsistente Architekturarbeit notwendig sind.

**Einfach gesagt:** Bevor man anfängt, Architektur zu entwickeln, muss man sicherstellen, dass das Unternehmen dafür bereit ist.

---

## Was wird in dieser Phase gemacht?

### 1. Den organisatorischen Kontext verstehen

Zuerst muss man verstehen, in welchem Umfeld die Architekturarbeit stattfindet:

- Welche **Unternehmensbereiche** sind betroffen?
- Was sind die **Geschäftsziele und Treiber**, die Architekturarbeit notwendig machen?
- Welche **bestehenden Frameworks, Methoden und Governance-Strukturen** gibt es bereits?
- Wie ist das Unternehmen strukturiert? (Enterprise Operating Model)
- Gibt es bereits eine Architecture Capability, oder muss sie von Grund auf aufgebaut werden?

### 2. Sponsoren und Stakeholder gewinnen

Architekturarbeit braucht **Unterstützung von der Führungsebene**. Ohne diese Unterstützung hat Architekturarbeit keinen Einfluss.

- Führungskräfte (z. B. CIO, CTO, CEO) als Sponsoren gewinnen
- Alle relevanten Stakeholder identifizieren
- Rollen und Verantwortlichkeiten für die Architekturarbeit festlegen
- Klären, wer Entscheidungen trifft und wer informiert werden muss

### 3. Das TOGAF-Framework anpassen (Tailoring)

TOGAF ist ein **generisches Framework** – es passt nicht für jedes Unternehmen 1:1. Deshalb muss es angepasst werden:

- Auswahl und Anpassung des **Architecture Content Frameworks**
  - Beispiele: Zachman Framework, DoDAF, NAF oder das TOGAF-eigene Framework
- Definition des **Architecture Metamodells**: Welche Konzepte, Entitäten und Beziehungen werden verwendet?
- Festlegen, welche ADM-Phasen relevant sind und wie sie angepasst werden
- Integration mit anderen Frameworks wie ITIL, COBIT, PMBOK, PRINCE2 oder Agile-Methoden

### 4. Architecture Principles definieren

Architecture Principles sind **allgemeine Regeln und Leitlinien**, die dauerhaft gelten und die gesamte Architekturarbeit steuern.

Sie:
- Spiegeln den **Konsens im Unternehmen** wider
- Leiten sich aus übergeordneten Enterprise Principles ab
- Definieren, wie Ressourcen und Assets eingesetzt werden
- Müssen klar mit **Geschäftszielen** verknüpft sein

**Beispiele für typische Architecture Principles:**

| Principle | Bedeutung |
|---|---|
| Primacy of Principles | Alle Entscheidungen folgen den Principles |
| Technology Independence | Keine Abhängigkeit von einzelnen Herstellern |
| Interoperability | Systeme müssen miteinander kommunizieren können |
| Data is an Asset | Daten gehören dem Unternehmen und müssen gepflegt werden |
| Reuse before Buy before Build | Erst vorhandenes nutzen, dann kaufen, dann selbst bauen |

Jedes Principle besteht aus:
- **Name**: kurzer, einprägsamer Titel
- **Statement**: Was gilt?
- **Rationale**: Warum gilt das?
- **Implications**: Was bedeutet das in der Praxis?

### 5. Architecture Governance Framework aufbauen

Governance ist das **Rückgrat** jeder nachhaltigen Architekturarbeit. Ohne Governance werden Architekturentscheidungen ignoriert oder umgangen.

In dieser Phase wird aufgebaut:

- **Architecture Board**: Das zentrale Gremium, das Architekturentscheidungen trifft und überwacht
- **Governance Framework**: Regeln, Prozesse und Verantwortlichkeiten für die Architekturarbeit
- **Architecture Repository**: Die zentrale Ablage für alle Architektur-Artefakte

Das Architecture Repository besteht aus folgenden Teilen:

| Komponente | Inhalt |
|---|---|
| Architecture Metamodel | Das angepasste Framework und Metamodell |
| Architecture Landscape | Aktueller Zustand der Unternehmensarchitektur |
| Standards Library | Standards, mit denen neue Architekturen übereinstimmen müssen |
| Reference Library | Vorlagen, Muster und Referenzmaterial |
| Governance Repository | Protokolle aller Governance-Aktivitäten |
| Architecture Requirements Repository | Alle genehmigten Architekturanforderungen |
| Solutions Landscape | Übersicht über geplante und eingesetzte Lösungen |

### 6. Architecture Capability als operative Einheit aufbauen

Eine erfolgreiche Enterprise Architecture Praxis muss wie ein **operatives Geschäft** geführt werden. Das bedeutet:

- **Financial Management**: Budgets und Kosten der Architekturarbeit planen
- **Performance Management**: Den Wert der Architekturarbeit messen
- **Service Management**: Architekturdienstleistungen bereitstellen
- **Resource Management**: Fähigkeiten und Kompetenzen der Architekten sicherstellen
- **Communications & Stakeholder Management**: Regelmäßige Kommunikation mit allen Beteiligten
- **Quality Management**: Qualität der Architektur-Artefakte sicherstellen
- **Risk & Opportunity Management**: Risiken frühzeitig erkennen und steuern
- **Configuration Management**: Versionen und Änderungen an Artefakten verwalten

---

## Was kommt rein? (Inputs)

- Geschäftsstrategie, Geschäftsziele und Treiber
- Bestehende Governance-Strukturen und -prozesse
- Bestehende Architektur-Frameworks und -methoden
- Organisationsstruktur und Unternehmenskultur
- Bestehende IT-Strategie und IT-Governance
- Regulatorische Anforderungen

---

## Was kommt raus? (Outputs / Deliverables)

- **Angepasstes TOGAF-Framework** (Tailored Architecture Framework)
- **Architecture Principles** (dokumentiert und genehmigt)
- **Architecture Governance Framework** (inkl. Architecture Board)
- **Architecture Repository** (Grundstruktur aufgebaut)
- **Architecture Capability** (Rollen, Verantwortlichkeiten, Prozesse definiert)
- **Request for Architecture Work** (als Input für Phase A)

---

## Häufige Fehler

- Die Phase wird übersprungen oder zu wenig ernst genommen → spätere Phasen sind inkonsistent
- Principles werden ohne echten Konsens definiert → werden später ignoriert
- Governance wird nur auf dem Papier etabliert, aber nicht gelebt
- Das Framework wird zu stark angepasst → verliert den Mehrwert des Standards

---

---

# Phase A – Architecture Vision

---

## Was ist das Ziel dieser Phase?

Phase A ist die **erste Phase des eigentlichen ADM-Zyklus**. Hier wird festgelegt, was die Architekturentwicklungsinitiative erreichen soll. Es werden die relevanten Stakeholder identifiziert, eine Architecture Vision erstellt und die formale Genehmigung eingeholt, um mit der Arbeit fortzufahren.

**Einfach gesagt:** Wir klären, was wir tun wollen, wer beteiligt ist, und holen uns das OK, um loszulegen.

---

## Was wird in dieser Phase gemacht?

### 1. Die Initiative starten

Ausgangspunkt ist der **Request for Architecture Work** – ein formales Dokument, das beschreibt, warum Architekturarbeit notwendig ist.

Typische Auslöser:
- Neue Geschäftsstrategie oder -ziele
- Regulatorische Anforderungen (z. B. neue Datenschutzgesetze)
- Technologischer Wandel (z. B. Cloud-Migration)
- Fusionen, Übernahmen oder Restrukturierungen
- Erkannte Probleme in der aktuellen Architektur

### 2. Stakeholder identifizieren und managen

Stakeholder sind alle Personen oder Gruppen, die von der Architektur betroffen sind oder Einfluss auf sie haben.

- Alle relevanten Stakeholder identifizieren: intern (Management, IT, Business Units) und extern (Kunden, Partner, Regulatoren)
- **Stakeholder-Concerns** analysieren: Was sind ihre Anliegen, Erwartungen und Bedenken?
- **Stakeholder-Management-Plan** entwickeln: Wie werden sie eingebunden und informiert?
- Sicherstellen, dass alle Concerns in der Architecture Vision berücksichtigt werden

### 3. Scope der Architektur definieren

Der Scope legt fest, was die Architekturarbeit umfasst und was nicht.

**Breadth (Breite):** Welche Teile des Unternehmens sind betroffen?
- Gesamtes Unternehmen
- Eine Business Unit
- Ein spezifisches Programm oder Projekt

**Depth (Tiefe):** Wie detailliert wird gearbeitet?
- Contextual (Warum?)
- Conceptual (Was?)
- Logical (Wie?)
- Physical (Womit?)

**Time Horizon:** Welcher Zeithorizont wird betrachtet?
- Kurzfristig (0–1 Jahr)
- Mittelfristig (1–3 Jahre)
- Langfristig (3–5+ Jahre)

**Architecture Domains:** Welche Bereiche sind relevant?
- Business Architecture
- Data Architecture
- Application Architecture
- Technology Architecture

### 4. Architecture Vision erstellen

Die Architecture Vision ist ein **High-Level-Überblick** über die Zielarchitektur. Sie ist kein technisches Dokument – sie ist ein **Kommunikationsmittel** für alle Stakeholder.

Die Architecture Vision:
- Beschreibt den **zukünftigen Zustand** des Unternehmens aus Architektursicht
- Zeigt, wie die Zielarchitektur die **Geschäftsziele** unterstützt
- Identifiziert die wichtigsten **Chancen und Risiken**
- Ist verständlich für Nicht-Techniker (Management, Business)
- Wird oft durch **Business Scenarios** entwickelt und validiert

### 5. Business Scenarios entwickeln

Business Scenarios sind ein Werkzeug, um Geschäftsanforderungen zu verstehen und zu dokumentieren. Sie helfen dabei:
- Geschäftliche Anforderungen aus **Nutzerperspektive** zu beschreiben
- Die Architektur aus **Geschäftsperspektive** zu validieren
- Stakeholder-Concerns in konkrete Architekturanforderungen zu übersetzen

Ein Business Scenario beschreibt typischerweise:
- Den **Geschäftskontext**: Was ist die Situation?
- Die **Akteure**: Wer ist beteiligt?
- Den **gewünschten Zustand**: Was soll erreicht werden?
- Die **Anforderungen**: Was muss die Architektur leisten?

### 6. Statement of Architecture Work erstellen

Das **Statement of Architecture Work** ist das formale Dokument, das die Architekturarbeit definiert. Es ist sozusagen der **Vertrag** zwischen dem Architekturteam und den Sponsoren.

Es enthält:
- Den **Scope** der Architekturarbeit
- Die **Ziele und Ergebnisse**
- Die **Ressourcen, Zeitpläne und Verantwortlichkeiten**
- Die **Akzeptanzkriterien** für die Architekturarbeit
- Die **Risiken und Annahmen**

### 7. Formale Genehmigung einholen

- Präsentation der Architecture Vision und des Statement of Architecture Work vor dem **Architecture Board** und den Sponsoren
- Formale Genehmigung, mit der Architekturentwicklung fortzufahren
- Dokumentation der Genehmigung im **Governance Repository**

---

## Was kommt rein? (Inputs)

- Request for Architecture Work
- Architecture Principles (aus Preliminary Phase)
- Bestehende Architecture Repository-Inhalte
- Geschäftsstrategie, -ziele und -treiber
- Bestehende Architektur-Dokumentation (Baseline)

---

## Was kommt raus? (Outputs / Deliverables)

- **Architecture Vision** (genehmigt)
- **Statement of Architecture Work** (genehmigt)
- **Stakeholder Map** und Stakeholder-Management-Plan
- **Architecture Definition Document** (erste Version, High-Level)
- **Architecture Requirements Specification** (erste Version)
- **Business Scenarios** (falls entwickelt)
- **Kommunikationsplan**

---

## Häufige Fehler

- Vision ist zu vage oder zu technisch → verliert Stakeholder
- Scope wird nicht klar definiert → führt zu Scope Creep in späteren Phasen
- Stakeholder werden nicht ausreichend eingebunden → fehlende Akzeptanz
- Statement of Architecture Work wird nicht formal genehmigt → fehlende Verbindlichkeit

---

---

# Phase B – Business Architecture

---

## Was ist das Ziel dieser Phase?

Phase B entwickelt die **Business Architecture**. Sie beschreibt den **aktuellen Zustand (Baseline)** und den **Zielzustand (Target)** der Geschäftsarchitektur und identifiziert die **Lücken (Gaps)** zwischen beiden.

**Einfach gesagt:** Wir schauen uns an, wie das Geschäft heute funktioniert, wie es in Zukunft funktionieren soll, und was fehlt, um dahin zu kommen.

---

## Was wird in dieser Phase gemacht?

### 1. Baseline Business Architecture beschreiben

Zuerst dokumentieren wir den **aktuellen Zustand** des Geschäfts:

- Bestehende **Geschäftsprozesse** analysieren und dokumentieren
- **Organisationsstrukturen** erfassen
- **Geschäftsfähigkeiten (Business Capabilities)** inventarisieren
- **Geschäftsservices** identifizieren
- Schwachstellen, Redundanzen und Ineffizienzen im aktuellen Zustand erkennen
- Bestehende Dokumentation aus dem Architecture Repository nutzen

### 2. Target Business Architecture entwickeln

Die Zielarchitektur beschreibt den **gewünschten zukünftigen Zustand** des Geschäfts:

**Business Capabilities:**
- Was kann das Unternehmen tun?
- Beispiele: Kundenmanagement, Produktentwicklung, Lieferkettenmanagement, Finanzberichterstattung
- Capabilities sind **stabil** – sie ändern sich nicht, wenn sich Prozesse oder Technologien ändern
- Capabilities werden in **Capability Maps** visualisiert

**Value Streams:**
- Wie wird Wert für Stakeholder erzeugt?
- End-to-End-Prozesse, die Wert liefern (z. B. von der Bestellung bis zur Lieferung)
- Verbinden Capabilities mit konkreten Geschäftsergebnissen

**Business Processes:**
- Wie werden Aktivitäten ausgeführt?
- Detaillierte Beschreibung von Abläufen (z. B. mit BPMN)

**Organisationsstruktur:**
- Wie ist das Unternehmen organisiert?
- Wer ist für was verantwortlich?
- Visualisiert in **Organization Maps**

**Business Functions:**
- Welche Funktionen erfüllt das Unternehmen? (z. B. HR, Finance, Marketing)

**Business Services:**
- Welche Services bietet das Unternehmen intern und extern an?

**Information Flows:**
- Welche Informationen fließen zwischen Einheiten?
- Wer braucht welche Information, von wem, wann?

**Business Roles & Actors:**
- Wer führt welche Aktivitäten aus?
- Welche Rollen gibt es im Unternehmen?

### 3. Business Modeling Techniken einsetzen

Verschiedene Modellierungstechniken werden eingesetzt:

| Technik | Zweck |
|---|---|
| Business Model Canvas | Beschreibt das gesamte Geschäftsmodell |
| Capability Maps | Visualisieren Geschäftsfähigkeiten und ihre Reife |
| Value Stream Maps | Zeigen den Wertefluss durch das Unternehmen |
| Organization Maps | Zeigen Organisationsstrukturen und Verantwortlichkeiten |
| Process Models (BPMN) | Beschreiben Geschäftsprozesse im Detail |
| Business Motivation Model (BMM) | Verbindet Ziele, Strategien und Taktiken |

### 4. Gap-Analyse durchführen

Die Gap-Analyse vergleicht systematisch **Baseline** und **Target** Architecture:

- Welche Fähigkeiten fehlen?
- Welche Prozesse müssen geändert werden?
- Welche Strukturen müssen angepasst werden?
- Wie schwerwiegend ist jede Lücke?
- Welche Lücken haben den größten Einfluss auf die Geschäftsziele?

Die Ergebnisse der Gap-Analyse sind wichtige Inputs für Phase E (Opportunities & Solutions).

### 5. Interoperabilitätsanforderungen definieren

- Identifikation der **Informations- und Serviceaustausche** zwischen Geschäftseinheiten
- Definition von Interoperabilitätsanforderungen auf Geschäftsebene
- Diese werden in Phase C (Data Architecture) weiter verfeinert

### 6. Architekturanforderungen aktualisieren

- Neue oder geänderte Anforderungen aus Phase B werden im **Requirements Management**-Prozess erfasst
- Rückkopplung zu Phase A, falls die Vision angepasst werden muss

---

## Was kommt rein? (Inputs)

- Architecture Vision (aus Phase A)
- Statement of Architecture Work
- Architecture Principles
- Bestehende Geschäftsdokumentation (Prozesse, Organigramme, Strategiepapiere)
- Architecture Repository (bestehende Business Architecture-Artefakte)

---

## Was kommt raus? (Outputs / Deliverables)

- **Architecture Definition Document** (Business Architecture-Abschnitt, Baseline + Target)
- **Architecture Requirements Specification** (aktualisiert)
- **Gap-Analyse** (Business Architecture)
- **Business Architecture-Artefakte**: Capability Maps, Value Stream Maps, Process Models, Organization Maps
- **Architecture Roadmap** (erste Beiträge)
- **Aktualisiertes Architecture Repository**

---

## Häufige Fehler

- Business Architecture wird zu IT-lastig → verliert den Geschäftsfokus
- Baseline wird nicht ausreichend dokumentiert → Gap-Analyse ist unvollständig
- Business Capabilities werden mit Business Processes verwechselt
- Stakeholder aus dem Business werden nicht ausreichend eingebunden

---

---

# Phase C – Information Systems Architectures

---

## Was ist das Ziel dieser Phase?

Phase C entwickelt die **Information Systems Architectures** – also die **Data Architecture** und die **Application Architecture**. Diese beschreiben, welche Daten und Anwendungen das Unternehmen braucht, um seine Geschäftsziele zu erreichen.

**Einfach gesagt:** Wir klären, welche Daten wir haben und brauchen, und welche Anwendungen diese Daten verarbeiten.

Phase C hat zwei Teile:
1. **Data Architecture** – Welche Daten gibt es, und wie fließen sie?
2. **Application Architecture** – Welche Anwendungen gibt es, und wie hängen sie zusammen?

---

## Teil 1: Data Architecture

### 1. Baseline Data Architecture beschreiben

- Dokumentation der **aktuellen Datenwelt**: Datenquellen, Datenmodelle, Datenflüsse, Datenspeicher
- Identifikation von Datenqualitätsproblemen, Redundanzen und Datensilos
- Analyse bestehender Datenstandards und Data Governance

### 2. Target Data Architecture entwickeln

**Logisches Datenmodell:**
- Welche Datenentitäten existieren? (z. B. Kunde, Produkt, Bestellung, Lieferant)
- Wie hängen diese Entitäten zusammen?
- Welche Attribute hat jede Entität?

**Datenflüsse:**
- Wie fließen Daten zwischen Systemen und Prozessen?
- Wer erzeugt Daten? Wer konsumiert sie?
- Welche Transformationen finden statt?

**Data Governance:**
- Wer ist für welche Daten verantwortlich? (Data Owner)
- Wie werden Datenqualität und -konsistenz sichergestellt?
- Welche Datenrichtlinien gelten?

**Datenstandards:**
- Welche Formate, Definitionen und Klassifikationen gelten?
- Wie werden Daten einheitlich benannt und beschrieben?

**Master Data Management (MDM):**
- Wie werden Stammdaten (Kunden, Produkte, Lieferanten) konsistent verwaltet?
- Welches System ist die "Single Source of Truth" für welche Daten?

**Datensicherheit und Datenschutz:**
- Welche Daten sind sensibel oder personenbezogen?
- Wie werden sie geschützt? (Verschlüsselung, Zugriffsrechte, Anonymisierung)
- Welche regulatorischen Anforderungen gelten? (z. B. DSGVO)

**Information Exchange:**
- Wie werden Informationen zwischen Systemen und Partnern ausgetauscht?
- Welche Austauschformate werden verwendet? (z. B. JSON, XML, EDI)

---

## Teil 2: Application Architecture

### 1. Baseline Application Architecture beschreiben

- **Application Portfolio**: Inventar aller bestehenden Anwendungen
- Dokumentation von Anwendungsbeziehungen, -abhängigkeiten und -integrationen
- Identifikation von veralteten Systemen (Legacy), Redundanzen und Lücken
- Bewertung der aktuellen Anwendungslandschaft gegen die Geschäftsanforderungen

### 2. Target Application Architecture entwickeln

**Application Portfolio:**
- Welche Anwendungen werden benötigt?
- Welche bestehenden Anwendungen bleiben, werden abgelöst oder konsolidiert?
- Welche neuen Anwendungen werden eingeführt?

**Application Services:**
- Welche Services stellen Anwendungen bereit?
- Wie werden diese Services von anderen Anwendungen oder Nutzern konsumiert?

**Application Interactions:**
- Wie kommunizieren Anwendungen miteinander?
- Welche Integrationsmuster werden verwendet? (APIs, Messaging, Events, Batch)
- Welche Daten werden ausgetauscht?

**Application Components:**
- Wie sind Anwendungen intern strukturiert?
- Welche Komponenten hat eine Anwendung?

**Klassifikation von Software (wichtig!):**

Ein häufig diskutiertes Thema ist die Abgrenzung zwischen Phase C und Phase D:

| Kategorie | Phase | Beispiele |
|---|---|---|
| Informationssystem-Software | Phase C | ERP, CRM, HRM, E-Commerce-Plattform |
| Technologie-Software | Phase D | Betriebssysteme, Middleware, Datenbankmanagementsysteme, Monitoring-Tools |

Die Empfehlung: Jede Software im Einzelfall bewerten und als Team entscheiden, wo sie eingeordnet wird.

**Make-Buy-Outsource-Entscheidungen:**
- Werden Anwendungen selbst entwickelt (Make)?
- Werden sie zugekauft (Buy – COTS)?
- Werden sie ausgelagert (Outsource)?

### 3. Gap-Analyse durchführen

- Vergleich von Baseline und Target für Data und Application Architecture
- Identifikation fehlender Anwendungen, Datenmodelle oder Integrationen
- Priorisierung der Gaps als Input für Phase E

### 4. Sicherheitsanforderungen berücksichtigen

- Identifikation von Sicherheitsanforderungen auf Daten- und Applikationsebene
- Sicherstellung, dass Datenschutz und Compliance-Anforderungen berücksichtigt werden

---

## Was kommt rein? (Inputs)

- Architecture Vision (Phase A)
- Business Architecture (Phase B)
- Bestehende Anwendungs- und Datendokumentation
- Architecture Repository (bestehende IS-Artefakte)
- Architekturprinzipien (Daten- und Anwendungsprinzipien)

---

## Was kommt raus? (Outputs / Deliverables)

- **Architecture Definition Document** (Data + Application Architecture, Baseline + Target)
- **Architecture Requirements Specification** (aktualisiert)
- **Gap-Analyse** (Data + Application Architecture)
- **Data Architecture-Artefakte**: Datenmodelle, Datenflussdiagramme, Data Entity/Business Function Matrix
- **Application Architecture-Artefakte**: Application Portfolio, Application Interaction Diagrams, Application/Function Matrix
- **Architecture Roadmap** (Beiträge)
- **Aktualisiertes Architecture Repository**

---

## Häufige Fehler

- Data und Application Architecture werden nicht klar voneinander getrennt
- Klassifikation von Software (Phase C vs. Phase D) ist unklar → Konsistenzprobleme
- Legacy-Systeme werden nicht ausreichend berücksichtigt
- Sicherheits- und Datenschutzanforderungen werden nachgelagert behandelt

---

---

# Phase D – Technology Architecture

---

## Was ist das Ziel dieser Phase?

Phase D entwickelt die **Technology Architecture**. Sie beschreibt die **technologische Infrastruktur** – Hardware, Software-Infrastruktur, Netzwerke, Plattformen und Standards – die benötigt wird, um die Zielarchitektur technisch zu realisieren.

**Einfach gesagt:** Wir klären, auf welcher technologischen Grundlage unsere Anwendungen und Daten laufen sollen.

---

## Was wird in dieser Phase gemacht?

### 1. Baseline Technology Architecture beschreiben

- Inventar der **aktuellen Technologielandschaft**:
  - Server (physisch und virtuell)
  - Netzwerke (LAN, WAN, VPN)
  - Cloud-Dienste (IaaS, PaaS, SaaS)
  - Middleware (Message Broker, ESB, API-Gateways)
  - Betriebssysteme
  - Datenbanken
  - Monitoring- und Management-Tools
- Dokumentation von Technologiestandards und -richtlinien
- Identifikation veralteter Technologien (End-of-Life), Sicherheitslücken und Kapazitätsengpässe

### 2. Target Technology Architecture entwickeln

**Compute:**
- Welche Server-Infrastruktur wird benötigt?
- Physische Server, virtuelle Maschinen, Container, Serverless?
- On-Premises, Cloud (Public, Private, Hybrid, Multi-Cloud)?

**Storage:**
- Welche Speicherlösungen werden benötigt?
- Relationale Datenbanken, NoSQL, Data Warehouses, Object Storage?

**Networking:**
- Welche Netzwerkinfrastruktur wird benötigt?
- LAN, WAN, VPN, Software-Defined Networking (SDN)?
- API-Gateways, Load Balancer, CDN?

**Security:**
- Firewalls, Intrusion Detection/Prevention Systems (IDS/IPS)
- Identity & Access Management (IAM)
- Verschlüsselung (at rest, in transit)
- Security Information and Event Management (SIEM)

**Middleware:**
- Message Broker (z. B. Apache Kafka, RabbitMQ)
- Enterprise Service Bus (ESB)
- API-Management-Plattformen

**Betriebssysteme und Laufzeitumgebungen:**
- Welche Betriebssysteme werden eingesetzt?
- Container-Orchestrierung (z. B. Kubernetes)?
- Laufzeitumgebungen (z. B. JVM, .NET Runtime)?

**Monitoring und Observability:**
- Wie wird der Betrieb überwacht?
- Logging, Monitoring, Tracing, Alerting?

**Cloud-Strategie:**
- Public Cloud (z. B. AWS, Azure, Google Cloud)
- Private Cloud (On-Premises)
- Hybrid Cloud (Kombination)
- Multi-Cloud (mehrere Anbieter)

**Plattformarchitektur:**
- Microservices vs. Monolith?
- Container-basiert (Docker, Kubernetes)?
- Serverless?
- Event-Driven Architecture?

### 3. Technologieprinzipien anwenden

- Anwendung der in der Preliminary Phase definierten Technologieprinzipien
- **Technologieunabhängigkeit**: Vermeidung von Vendor Lock-in
- **Nachhaltigkeit**: Energieeffizienz und Umweltverträglichkeit berücksichtigen
- **Standardisierung**: Einheitliche Technologiestandards im Unternehmen

### 4. Sicherheitsarchitektur auf Technologieebene

- Definition von Sicherheitsmaßnahmen auf Infrastrukturebene
- Identity & Access Management (IAM)
- Network Security Architecture
- Data Encryption (at rest, in transit)
- Disaster Recovery und Business Continuity Planning

### 5. Interoperabilität auf Technologieebene

- Welche technischen Mechanismen ermöglichen den Informationsaustausch?
- APIs (REST, GraphQL, gRPC)
- Messaging-Protokolle (AMQP, MQTT)
- Datenaustauschformate (JSON, XML, Protobuf)
- Netzwerkprotokolle (HTTP/S, TCP/IP)

### 6. Gap-Analyse durchführen

- Vergleich von Baseline und Target Technology Architecture
- Identifikation fehlender Technologiekomponenten, veralteter Systeme und Kapazitätslücken
- Priorisierung als Input für Phase E

---

## Was kommt rein? (Inputs)

- Architecture Vision (Phase A)
- Business Architecture (Phase B)
- Information Systems Architectures (Phase C)
- Bestehende Technologiedokumentation
- Architecture Repository (Technologiestandards, Reference Library)
- Technologieprinzipien

---

## Was kommt raus? (Outputs / Deliverables)

- **Architecture Definition Document** (Technology Architecture, Baseline + Target)
- **Architecture Requirements Specification** (aktualisiert)
- **Gap-Analyse** (Technology Architecture)
- **Technology Architecture-Artefakte**:
  - Technology Standards Catalog
  - Technology Portfolio Catalog
  - Environments and Locations Diagram
  - Platform Decomposition Diagram
- **Architecture Roadmap** (Beiträge)
- **Aktualisiertes Architecture Repository**

---

## Häufige Fehler

- Technology Architecture wird zu früh zu detailliert (zu viel "wie" statt "was")
- Technologieentscheidungen werden ohne Berücksichtigung der Geschäftsanforderungen getroffen
- Sicherheitsanforderungen werden nachgelagert behandelt
- Vendor Lock-in wird nicht ausreichend berücksichtigt

---

---

# Phase E – Opportunities & Solutions

---

## Was ist das Ziel dieser Phase?

Phase E ist der **Übergang von der Architekturentwicklung zur Implementierungsplanung**. Hier werden Lösungsoptionen für die in den Phasen B, C und D identifizierten Gaps entwickelt und bewertet. Außerdem beginnt die Entwicklung der **Architecture Roadmap** und des **Implementation and Migration Plans**.

**Einfach gesagt:** Wir schauen uns an, welche Lösungen es gibt, um die Lücken zu schließen, und beginnen zu planen, wie und wann wir diese umsetzen.

---

## Was wird in dieser Phase gemacht?

### 1. Implementierungsbereitschaft bewerten

Bevor Lösungen geplant werden, muss bewertet werden, ob das Unternehmen bereit ist:

- **Organisatorische Bereitschaft**: Ist die Organisation bereit für Veränderungen?
- **Kulturelle Bereitschaft**: Wie veränderungsbereit ist die Unternehmenskultur?
- **Ressourcen**: Stehen Budget, Personal und Technologie zur Verfügung?
- **Risiken**: Welche Risiken bestehen bei der Implementierung?
- **Zeitdruck**: Gibt es externe Deadlines (z. B. regulatorische Fristen)?

### 2. Work Packages identifizieren

Ein **Work Package** ist eine Menge von Aktionen, die definiert werden, um ein oder mehrere Geschäftsziele zu erreichen.

Work Packages:
- Können Teil eines Projekts sein
- Können ein vollständiges Projekt darstellen
- Können Teil eines Programms sein
- Werden aus den Gap-Analysen der Phasen B, C und D abgeleitet

Für jedes Work Package wird definiert:
- Was soll erreicht werden?
- Welche Ressourcen werden benötigt?
- Welche Abhängigkeiten bestehen?
- Welche Risiken gibt es?
- Wie hoch ist der erwartete Nutzen?

### 3. Lösungsoptionen identifizieren und bewerten

Für jedes identifizierte Gap werden Lösungsoptionen entwickelt:

| Option | Bedeutung | Beispiel |
|---|---|---|
| **Reuse** | Bestehende Lösung wiederverwenden | Bestehendes ERP-Modul aktivieren |
| **Buy** | Fertige Lösung kaufen (COTS) | SAP, Salesforce, ServiceNow |
| **Make** | Selbst entwickeln | Eigenentwicklung einer App |
| **Outsource** | An externen Dienstleister auslagern | Cloud-Service, Managed Service |

Lösungen werden bewertet nach:
- **Kosten und Nutzen** (ROI)
- **Risiko**
- **Strategischer Fit** (passt es zur Unternehmensstrategie?)
- **Technischer Machbarkeit**
- **Abhängigkeiten** zu anderen Lösungen
- **Time-to-Value** (wie schnell kann Nutzen realisiert werden?)

### 4. Transition Architectures definieren

Da die Zielarchitektur oft nicht in einem Schritt erreicht werden kann, werden **Transition Architectures** definiert:

- Formale Beschreibungen des Architekturzustands an **architektonisch bedeutsamen Zeitpunkten**
- Zeigen die schrittweise Evolution von der Baseline zur Target Architecture
- Jede Transition Architecture stellt einen **stabilen, betriebsfähigen Zustand** dar
- Dienen als Meilensteine auf dem Weg zur Zielarchitektur

**Beispiel:**
- Baseline: Monolithisches ERP-System
- Transition Architecture 1: ERP + neue Microservices für Kundenverwaltung
- Transition Architecture 2: ERP + vollständige Microservices-Schicht
- Target Architecture: Cloud-native Microservices-Plattform

### 5. Architecture Roadmap entwickeln

Die **Architecture Roadmap** ist ein abstrahierter Plan für Business- oder Technologieveränderungen:

- Zeigt, welche Work Packages wann umgesetzt werden
- Berücksichtigt Abhängigkeiten zwischen Work Packages
- Verbindet Architekturziele mit konkreten Implementierungsschritten
- Operiert typischerweise über mehrere Jahre und Disziplinen hinweg
- Zeigt die Transition Architectures als Meilensteine

### 6. Implementation and Migration Plan (erste Version)

- Erste Version des Implementierungs- und Migrationsplans
- Wird in Phase F weiter verfeinert und finalisiert

---

## Was kommt rein? (Inputs)

- Architecture Definition Document (Phasen B, C, D)
- Gap-Analysen (Phasen B, C, D)
- Architecture Requirements Specification
- Architecture Vision (Phase A)
- Architecture Repository (Solutions Continuum, Reference Library)
- Bestehende Projektportfolios und Programme

---

## Was kommt raus? (Outputs / Deliverables)

- **Architecture Roadmap** (erste vollständige Version)
- **Transition Architecture(s)** (definiert und dokumentiert)
- **Work Package Portfolio** (identifiziert und priorisiert)
- **Implementation and Migration Plan** (erste Version)
- **Architecture Definition Document** (aktualisiert)
- **Architecture Requirements Specification** (aktualisiert)

---

## Häufige Fehler

- Work Packages werden nicht ausreichend priorisiert → Ressourcenkonflikte
- Transition Architectures werden nicht definiert → Implementierung verliert Orientierung
- Lösungsoptionen werden ohne ausreichende Bewertung ausgewählt
- Abhängigkeiten zwischen Work Packages werden übersehen

---

---

# Phase F – Migration Planning

---

## Was ist das Ziel dieser Phase?

Phase F **finalisiert den Implementierungs- und Migrationsplan**. Sie stellt sicher, dass der Plan realistisch, finanzierbar und mit den Unternehmenszielen abgestimmt ist. Außerdem wird festgelegt, wie das Unternehmen konkret von der Baseline Architecture zur Target Architecture gelangt.

**Einfach gesagt:** Wir machen den Migrationsplan fertig – mit konkreten Zeitplänen, Ressourcen, Kosten und Abhängigkeiten.

---

## Was wird in dieser Phase gemacht?

### 1. Implementation and Migration Plan finalisieren

- Detaillierung der in Phase E entwickelten ersten Version
- Festlegung von **Zeitplänen, Meilensteinen und Abhängigkeiten**
- Zuweisung von **Ressourcen** (Budget, Personal, Technologie)
- Priorisierung von Work Packages nach:
  - Geschäftlichem Nutzen
  - Risiko
  - Abhängigkeiten
  - Ressourcenverfügbarkeit
  - Strategischer Bedeutung

### 2. Kosten-Nutzen-Analyse durchführen

- Bewertung des **Return on Investment (ROI)** für jedes Work Package
- Analyse, wie sich der ROI über die Zeit verändert
- Berücksichtigung von:
  - **Direkten Kosten**: Implementierung, Lizenzen, Training, Hardware
  - **Indirekten Kosten**: Betrieb, Wartung, Risikomanagement
  - **Nutzen**: Effizienzgewinne, Kosteneinsparungen, Umsatzsteigerungen, Risikoreduktion

### 3. Interoperabilität logisch implementieren

- Die in den Phasen A–D definierten Interoperabilitätsanforderungen werden in Phase F **logisch implementiert**
- Konkrete Migrationspfade für Daten, Anwendungen und Technologien werden definiert
- Datenmigrationsstrategie festlegen: Wie werden bestehende Daten in neue Systeme überführt?

### 4. Transition Architectures in den Plan integrieren

- Die in Phase E definierten Transition Architectures werden als **Meilensteine** in den Migrationsplan integriert
- Sicherstellung, dass jede Transition Architecture einen stabilen, betriebsfähigen Zustand darstellt
- Für jede Transition Architecture wird definiert:
  - Wann wird sie erreicht?
  - Was muss dafür umgesetzt werden?
  - Welche Ressourcen werden benötigt?
  - Welche Risiken bestehen?

### 5. Abstimmung mit Projektportfolio und Programm-Management

- Sicherstellung, dass der Migrationsplan mit dem **bestehenden Projektportfolio** abgestimmt ist
- Integration mit **Programm-Management-Prozessen** (z. B. PMBOK, PRINCE2, MSP)
- Identifikation von Abhängigkeiten zu laufenden Projekten
- Vermeidung von Ressourcenkonflikten

### 6. Governance-Anforderungen für die Implementierung definieren

- Festlegung, welche **Governance-Mechanismen** während der Implementierung gelten
- Definition von **Architecture Compliance Reviews** (werden in Phase G durchgeführt)
- Vorbereitung der Übergabe an Phase G

### 7. Kommunikation und Change Management planen

- Wie werden Stakeholder über den Migrationsplan informiert?
- Wie wird das Change Management organisiert?
- Welche Trainings und Schulungen sind notwendig?

---

## Was kommt rein? (Inputs)

- Architecture Roadmap (Phase E)
- Transition Architectures (Phase E)
- Work Package Portfolio (Phase E)
- Architecture Definition Document
- Architecture Requirements Specification
- Bestehende Projektportfolios, Budgetpläne

---

## Was kommt raus? (Outputs / Deliverables)

- **Implementation and Migration Plan** (finalisiert und genehmigt)
- **Architecture Roadmap** (aktualisiert)
- **Finanzierungsplan** und Kosten-Nutzen-Analyse
- **Architecture Definition Document** (aktualisiert)
- **Architecture Requirements Specification** (aktualisiert)
- **Aktualisiertes Architecture Repository**

---

## Häufige Fehler

- Migrationsplan ist zu optimistisch → unterschätzt Komplexität und Risiken
- Abhängigkeiten zu laufenden Projekten werden nicht berücksichtigt
- Kosten-Nutzen-Analyse wird oberflächlich durchgeführt
- Governance-Anforderungen für die Implementierung werden nicht definiert
- Change Management wird vergessen

---

---

# Phase G – Implementation Governance

---

## Was ist das Ziel dieser Phase?

Phase G stellt die **architektonische Aufsicht über die Implementierung** sicher. Sie gewährleistet, dass die Implementierungsprojekte die definierte Zielarchitektur einhalten und dass Abweichungen erkannt, bewertet und gesteuert werden.

**Einfach gesagt:** Wir schauen zu, dass die Implementierung so läuft, wie die Architektur es vorschreibt – und greifen ein, wenn das nicht der Fall ist.

---

## Was wird in dieser Phase gemacht?

### 1. Architecture Contracts erstellen und verwalten

**Architecture Contracts** sind formale Vereinbarungen zwischen dem Architekturteam und den Implementierungsteams (und ggf. externen Lieferanten).

Sie definieren:
- Die **architektonischen Anforderungen** an die Implementierung
- **Compliance-Kriterien** und Qualitätsstandards
- Verantwortlichkeiten und Eskalationswege
- Akzeptanzkriterien für Lieferergebnisse
- Konsequenzen bei Nicht-Einhaltung

Architecture Contracts stellen sicher, dass alle Beteiligten wissen, was von ihnen erwartet wird.

### 2. Architecture Compliance Reviews durchführen

- Regelmäßige Überprüfung der Implementierungsprojekte auf **Konformität mit der Zielarchitektur**
- Bewertung von Projektlieferergebnissen gegen:
  - Architecture Definition Document
  - Architecture Requirements Specification
  - Architecture Principles
  - Standards Library
- Dokumentation der Compliance-Ergebnisse im **Governance Repository**
- Eskalation von Abweichungen an das Architecture Board

**Mögliche Ergebnisse eines Compliance Reviews:**

| Ergebnis | Bedeutung |
|---|---|
| Compliant | Vollständig konform mit der Architektur |
| Conditionally Compliant | Konform mit kleinen Abweichungen, die akzeptiert werden |
| Non-Compliant | Nicht konform, Korrekturmaßnahmen erforderlich |
| Exempt | Ausnahme genehmigt (Waiver) |

### 3. Implementierungsprojekte begleiten und beraten

- Architekten agieren als **Berater und Mentoren** für Implementierungsteams
- Beantwortung architektonischer Fragen während der Implementierung
- Sicherstellung, dass Architekturentscheidungen korrekt umgesetzt werden
- Unterstützung bei der Lösung architektonischer Konflikte

### 4. Architecture Board-Aktivitäten unterstützen

- Regelmäßige Berichterstattung an das **Architecture Board**
- Eskalation von Compliance-Problemen und Risiken
- Einholung von Genehmigungen für architektonische Ausnahmen (Waivers)
- Dokumentation aller Governance-Aktivitäten

### 5. Änderungsanfragen bewerten

- Während der Implementierung entstehen oft **Änderungsanfragen**
- Diese werden bewertet nach:
  - Auswirkungen auf die Zielarchitektur
  - Auswirkungen auf andere Work Packages
  - Kosten und Risiken
- Genehmigung oder Ablehnung durch das Architecture Board
- Bei größeren Änderungen: Rückkopplung zu Phase H oder sogar neuer ADM-Zyklus

### 6. Risikomanagement während der Implementierung

- Kontinuierliche Überwachung von Implementierungsrisiken
- Zwei Risikoebenen werden unterschieden:
  - **Initial Risk**: Risiko vor der Implementierung von Maßnahmen
  - **Residual Risk**: Risiko nach der Implementierung von Maßnahmen
- Risikomanagement-Prozess:
  1. Risikoklassifikation
  2. Risikoidentifikation
  3. Initiale Risikobewertung
  4. Risikomaßnahmen und Restrisikobewertung
  5. Risikomonitoring

### 7. Abnahme und Übergabe

- Formale Abnahme von Implementierungsergebnissen
- Überprüfung, ob die Akzeptanzkriterien aus den Architecture Contracts erfüllt sind
- Übergabe der implementierten Lösungen in den Betrieb
- Aktualisierung des Architecture Repository mit den neuen Ist-Zuständen

---

## Was kommt rein? (Inputs)

- Implementation and Migration Plan (Phase F)
- Architecture Roadmap (Phase F)
- Architecture Definition Document
- Architecture Requirements Specification
- Architecture Contracts
- Projektlieferergebnisse der Implementierungsteams

---

## Was kommt raus? (Outputs / Deliverables)

- **Architecture Compliance Reviews** (Berichte und Ergebnisse)
- **Architecture Contracts** (erstellt und verwaltet)
- **Governance Repository** (aktualisiert)
- **Architecture Definition Document** (aktualisiert mit Ist-Zuständen)
- **Architecture Repository** (aktualisiert)
- **Änderungsanfragen** (bewertet und entschieden)
- **Lessons Learned** (Erkenntnisse aus der Implementierung)

---

## Häufige Fehler

- Compliance Reviews werden zu selten oder gar nicht durchgeführt
- Architecture Contracts sind zu vage → keine klaren Akzeptanzkriterien
- Architekten sind zu weit von der Implementierung entfernt → Probleme werden zu spät erkannt
- Änderungsanfragen werden ohne architektonische Bewertung genehmigt

---

---

# Phase H – Architecture Change Management

---

## Was ist das Ziel dieser Phase?

Phase H etabliert **Prozesse und Verfahren**, um Änderungen an der Architektur zu steuern. Sie stellt sicher, dass die Architektur aktuell und relevant bleibt, und entscheidet, wann ein neuer ADM-Zyklus gestartet werden muss.

**Einfach gesagt:** Wir beobachten, was sich im Unternehmen und in der Welt ändert, und entscheiden, ob und wie wir die Architektur anpassen müssen.

---

## Was wird in dieser Phase gemacht?

### 1. Änderungsauslöser (Triggers) überwachen

Änderungen an der Architektur können durch viele Faktoren ausgelöst werden:

**Interne Trigger:**
- Neue Geschäftsstrategie oder -ziele
- Neue Geschäftsanforderungen oder -prozesse
- Technologische Innovationen im Unternehmen
- Erkannte Probleme in der aktuellen Architektur
- Ergebnisse aus Phase G (Compliance Reviews)

**Externe Trigger:**
- Neue regulatorische Anforderungen (z. B. neue Datenschutzgesetze)
- Technologischer Wandel (z. B. neue Cloud-Technologien, KI)
- Marktveränderungen (z. B. neue Wettbewerber, neue Kundenbedürfnisse)
- Fusionen, Übernahmen oder Partnerschaften
- Wirtschaftliche Veränderungen

### 2. Änderungsanfragen (Change Requests) verwalten

- Alle Änderungsanfragen werden formal erfasst und bewertet
- Bewertungskriterien:
  - Wie groß ist die Auswirkung auf die bestehende Architektur?
  - Wie dringend ist die Änderung?
  - Welche Kosten und Risiken entstehen?
  - Welche Stakeholder sind betroffen?

### 3. Art der Änderung bestimmen

TOGAF unterscheidet verschiedene Arten von Änderungen:

| Art der Änderung | Beschreibung | Reaktion |
|---|---|---|
| **Simplification Change** | Kleine, vereinfachende Änderung | Kann ohne neuen ADM-Zyklus umgesetzt werden |
| **Incremental Change** | Moderate Änderung, die die Architektur erweitert | Kann durch einen Teil-ADM-Zyklus adressiert werden |
| **Re-Architecting Change** | Fundamentale Änderung, die die Architektur grundlegend verändert | Neuer vollständiger ADM-Zyklus erforderlich |

### 4. Entscheidung: Neuer ADM-Zyklus oder nicht?

- Kleine Änderungen: Direkt in die bestehende Architektur integrieren
- Mittlere Änderungen: Bestimmte ADM-Phasen erneut durchlaufen
- Große Änderungen: Neuen vollständigen ADM-Zyklus starten (zurück zu Phase A)

### 5. Architecture Repository aktuell halten

- Alle genehmigten Änderungen werden im **Architecture Repository** dokumentiert
- Die **Architecture Landscape** wird aktualisiert
- Die **Standards Library** wird bei Bedarf aktualisiert
- Neue Erkenntnisse werden in der **Reference Library** gespeichert

### 6. Wert der Architektur überwachen und messen

- Kontinuierliche Überwachung, ob die Architektur den erwarteten Wert liefert
- Messung von:
  - Geschäftlichem Nutzen (z. B. Kosteneinsparungen, Effizienzgewinne)
  - Technischer Qualität (z. B. Systemverfügbarkeit, Performance)
  - Compliance (z. B. Einhaltung von Standards und Regulierungen)
- Berichterstattung an das Architecture Board und die Sponsoren

### 7. Architecture Governance aufrechterhalten

- Sicherstellung, dass das **Architecture Board** aktiv bleibt
- Regelmäßige Reviews der Architecture Principles
- Überprüfung, ob das Architecture Framework noch passt
- Bei Bedarf: Rückkopplung zur Preliminary Phase

---

## Was kommt rein? (Inputs)

- Ergebnisse aus Phase G (Compliance Reviews, Änderungsanfragen)
- Externe und interne Trigger für Architekturänderungen
- Architecture Repository (aktueller Zustand)
- Architecture Definition Document
- Architecture Roadmap

---

## Was kommt raus? (Outputs / Deliverables)

- **Aktualisiertes Architecture Repository**
- **Aktualisierte Architecture Landscape**
- **Entschiedene Änderungsanfragen** (genehmigt oder abgelehnt)
- **Neuer Request for Architecture Work** (falls neuer ADM-Zyklus gestartet wird)
- **Governance Repository** (aktualisiert)
- **Wertberichte** (Value Realization Reports)

---

## Häufige Fehler

- Änderungsanfragen werden nicht formal erfasst → unkontrollierte Änderungen
- Die Architektur wird nicht regelmäßig überprüft → veraltet schnell
- Kleine Änderungen werden ignoriert → häufen sich zu großen Problemen an
- Der Wert der Architektur wird nicht gemessen → fehlende Rechtfertigung für Architekturarbeit

---

---

# Requirements Management – Anforderungsmanagement

---

## Was ist das Ziel?

Das **Requirements Management** ist keine eigenständige Phase mit einem Anfang und Ende – es ist ein **kontinuierlicher Prozess**, der **während des gesamten ADM-Zyklus** läuft. Es stellt sicher, dass alle Architekturanforderungen erfasst, verwaltet und in die richtigen Phasen eingespeist werden.

**Einfach gesagt:** Anforderungen entstehen ständig – in jeder Phase, von jedem Stakeholder. Das Requirements Management sorgt dafür, dass keine Anforderung verloren geht und alle Anforderungen berücksichtigt werden.

---

## Was wird gemacht?

### 1. Anforderungen erfassen

- Anforderungen können aus jeder ADM-Phase entstehen
- Anforderungen kommen von Stakeholdern, aus Gap-Analysen, aus Compliance Reviews, aus externen Triggern
- Alle Anforderungen werden im **Architecture Requirements Repository** gespeichert

### 2. Anforderungen klassifizieren und priorisieren

- Anforderungen werden klassifiziert nach:
  - **Typ**: Funktional, nicht-funktional, Constraint, Annahme
  - **Quelle**: Stakeholder, regulatorisch, technisch
  - **Priorität**: Muss, Soll, Kann
- Priorisierung nach Geschäftswert und Dringlichkeit

### 3. Anforderungen in die richtigen Phasen einleiten

- Anforderungen werden in die ADM-Phasen eingespeist, in denen sie relevant sind
- Änderungen an Anforderungen werden an die betroffenen Phasen kommuniziert
- Sicherstellung, dass alle Anforderungen in der Architektur berücksichtigt werden

### 4. Anforderungen verfolgen und verwalten

- Kontinuierliche Verfolgung des Status jeder Anforderung
- Sicherstellung, dass Anforderungen vollständig, konsistent und widerspruchsfrei sind
- Verwaltung von Änderungen an Anforderungen (Versionierung)
- Rückverfolgbarkeit: Welche Anforderung führt zu welcher Architekturentscheidung?

### 5. Anforderungen genehmigen

- Neue und geänderte Anforderungen werden durch das **Architecture Board** genehmigt
- Nur genehmigte Anforderungen werden in die Architekturarbeit aufgenommen
- Abgelehnte Anforderungen werden dokumentiert und begründet

---

## Warum ist Requirements Management so wichtig?

Das Requirements Management ist das **Bindeglied zwischen allen ADM-Phasen**. Ohne es:
- Gehen Anforderungen verloren
- Entstehen Widersprüche zwischen Phasen
- Werden Stakeholder-Concerns nicht berücksichtigt
- Ist die Architektur nicht nachvollziehbar

Es stellt sicher, dass die Architektur immer auf den **tatsächlichen Bedürfnissen** des Unternehmens basiert – und nicht auf Annahmen oder veralteten Informationen.

---

## Was kommt rein? (Inputs)

- Anforderungen aus allen ADM-Phasen
- Stakeholder-Concerns
- Ergebnisse aus Gap-Analysen
- Ergebnisse aus Compliance Reviews
- Externe und interne Trigger

---

## Was kommt raus? (Outputs)

- **Architecture Requirements Repository** (laufend aktualisiert)
- **Priorisierte Anforderungsliste**
- **Genehmigte Anforderungen** (als Input für alle Phasen)
- **Änderungsanfragen** (als Input für Phase H)

---

---

# Zusammenfassung: Das ADM als Ganzes

---

## Die wichtigsten Prinzipien des ADM

### 1. Iterativität

Das ADM ist **nicht linear**. Es ist ein iterativer Prozess:
- Innerhalb einer Phase kann man zurückgehen und Ergebnisse anpassen
- Zwischen Phasen gibt es Rückkopplungen
- Der gesamte Zyklus kann mehrfach durchlaufen werden

### 2. Flexibilität

Das ADM **schreibt keine starre Reihenfolge vor**. Es ist kein Wasserfall-Modell. Die Phasen können:
- In unterschiedlicher Reihenfolge durchlaufen werden
- Parallel durchgeführt werden
- Übersprungen werden (wenn nicht relevant)
- Angepasst werden (Tailoring)

### 3. Anpassbarkeit

Das ADM ist ein **generisches Framework** und muss an die spezifischen Bedürfnisse des Unternehmens angepasst werden:
- Integration mit anderen Frameworks (ITIL, COBIT, PMBOK, Agile)
- Anpassung des Content Frameworks
- Anpassung der Phasen und Deliverables

### 4. Governance

Governance ist **zentral** für das ADM:
- Das Architecture Board überwacht alle Architekturaktivitäten
- Architecture Contracts stellen Compliance sicher
- Das Governance Repository dokumentiert alle Entscheidungen

### 5. Stakeholder-Orientierung

Das ADM ist **stakeholder-orientiert**:
- Stakeholder-Concerns werden in jeder Phase berücksichtigt
- Die Architecture Vision kommuniziert die Architektur verständlich
- Stakeholder werden aktiv eingebunden

---

## Der Zusammenhang zwischen den Phasen

```
Preliminary Phase
      ↓
Phase A: Architecture Vision
      ↓
Phase B: Business Architecture
      ↓
Phase C: Information Systems Architectures
      ↓
Phase D: Technology Architecture
      ↓
Phase E: Opportunities & Solutions
      ↓
Phase F: Migration Planning
      ↓
Phase G: Implementation Governance
      ↓
Phase H: Architecture Change Management
      ↓
(Neuer ADM-Zyklus oder zurück zu einer früheren Phase)

← Requirements Management läuft durch alle Phasen →
```

---

## Was macht das ADM besonders?

| Merkmal | Beschreibung |
|---|---|
| **Iterativ** | Phasen können wiederholt und angepasst werden |
| **Flexibel** | Keine starre Reihenfolge, kein Wasserfall |
| **Anpassbar** | Kann an jedes Unternehmen angepasst werden |
| **Governance-orientiert** | Starke Betonung von Kontrolle und Überwachung |
| **Stakeholder-orientiert** | Alle Stakeholder-Concerns werden berücksichtigt |
| **Ganzheitlich** | Deckt Business, Data, Application und Technology ab |
| **Wiederverwendbar** | Fördert die Wiederverwendung bestehender Architektur-Assets |

---

> **Quelle:** TOGAF® Standard, 10th Edition, The Open Group
> Dieses Dokument basiert auf den Inhalten des TOGAF® Standard, 10th Edition – Introduction and Core Concepts sowie dem TOGAF Business Architecture Foundation Study Guide.
