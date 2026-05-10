# TOGAF® 10 — Die vollständige Lernanleitung
## Vom Anfänger zum Profi: Alles was du wissen musst, um TOGAF 10 konkret anzuwenden

---

# KAPITEL 1 — Was ist TOGAF 10 und warum brauche ich es?

---

## 1.1 Das Problem ohne TOGAF

Stell dir vor, du baust ein Haus.
Ohne Architekt, ohne Plan, ohne Blaupause.

Jeder Handwerker macht einfach, was er für richtig hält.
Der Elektriker legt Kabel, wo er will.
Der Klempner baut Rohre ein, ohne auf die Wände zu achten.
Der Maurer mauert Wände, die später wieder eingerissen werden müssen.

Das Ergebnis: Chaos, Kosten, Verzögerungen.

**Genau das passiert in Unternehmen ohne Enterprise Architecture.**

Jede Abteilung kauft eigene IT-Systeme.
Systeme können nicht miteinander kommunizieren.
Daten existieren dreifach — und sind trotzdem nicht konsistent.
Projekte scheitern, weil niemand das große Bild sieht.

---

## 1.2 Was TOGAF löst

TOGAF ist der "Architektenplan" für Unternehmen.

**TOGAF steht für:**
> **T**he **O**pen **G**roup **A**rchitecture **F**ramework

Es ist ein **Framework** — also ein strukturierter Rahmen —
der dir sagt:
- **Wie** du eine Unternehmensarchitektur entwickelst
- **Was** du dabei produzieren sollst
- **Wer** dabei welche Rolle spielt
- **Warum** du bestimmte Entscheidungen triffst

---

## 1.3 TOGAF 10 — Was ist neu gegenüber Version 9?

TOGAF 10 wurde 2022 veröffentlicht.
Die wichtigsten Neuerungen:

```
╔══════════════════════════════════════════════════════════════╗
║           TOGAF 9 vs. TOGAF 10 — DIE UNTERSCHIEDE          ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  TOGAF 9 (alt)              TOGAF 10 (neu)                  ║
║  ─────────────────────────  ──────────────────────────────  ║
║  Ein einziges dickes Buch   6 separate Dokumente            ║
║                             (modularer Aufbau)              ║
║                                                              ║
║  Fokus auf IT               Stärkerer Business-Fokus        ║
║                                                              ║
║  Wenig Agilität             Agile Integration               ║
║                             (Sprints, iterativ)             ║
║                                                              ║
║  Statische Methode          Flexible Anpassung              ║
║                             an verschiedene Stile           ║
║                                                              ║
║  Business Architecture      Business Architecture           ║
║  wenig ausgeprägt           stark ausgebaut                 ║
║                             (eigene Zertifizierung!)        ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 1.4 Die 6 Dokumente des TOGAF 10 Standards

TOGAF 10 ist in 6 eigenständige Dokumente aufgeteilt:

```
📚 TOGAF 10 STANDARD — DOKUMENTENSTRUKTUR

┌─────────────────────────────────────────────────────────────┐
│  1. Introduction and Core Concepts                         │
│     → Was ist TOGAF? Grundbegriffe, Konzepte               │
├─────────────────────────────────────────────────────────────┤
│  2. Architecture Development Method (ADM)                  │
│     → Der Prozess: Wie entwickle ich eine Architektur?     │
├─────────────────────────────────────────────────────────────┤
│  3. ADM Techniques                                          │
│     → Werkzeuge und Methoden für die ADM-Phasen            │
├─────────────────────────────────────────────────────────────┤
│  4. Applying the ADM                                        │
│     → Wie passe ich den ADM an meinen Kontext an?          │
├─────────────────────────────────────────────────────────────┤
│  5. Architecture Content                                    │
│     → Was produziere ich? Deliverables, Artifacts          │
├─────────────────────────────────────────────────────────────┤
│  6. EA Capability and Governance                            │
│     → Wie baue ich eine EA-Funktion im Unternehmen auf?    │
└─────────────────────────────────────────────────────────────┘
```

---

## 1.5 Wer braucht TOGAF?

TOGAF ist relevant für:

| Rolle | Warum TOGAF relevant ist |
|-------|--------------------------|
| Enterprise Architect | Kernwerkzeug für die tägliche Arbeit |
| Business Architect | Business Architecture nach TOGAF 10 |
| IT-Architekt | Technische Architektur im TOGAF-Kontext |
| CIO / CTO | Strategische Steuerung der IT |
| Projektmanager | Verständnis der Architekturvorgaben |
| Business Analyst | Anforderungen im EA-Kontext |
| Berater | Kundenberatung zu EA-Themen |

---

## 1.6 Was TOGAF NICHT ist

Wichtig zu verstehen — TOGAF ist:

```
❌ KEIN fertiges Produkt, das man einfach installiert
❌ KEINE Garantie für Projekterfolg
❌ KEIN starres Regelwerk, das man sklavisch befolgt
❌ KEIN Ersatz für gesunden Menschenverstand
❌ KEIN reines IT-Framework (es ist ein EA-Framework!)

✅ TOGAF IST ein flexibler Rahmen
✅ TOGAF IST anpassbar an jede Organisation
✅ TOGAF IST eine gemeinsame Sprache für Architekten
✅ TOGAF IST ein iterativer Prozess
✅ TOGAF IST eine Methode für strukturiertes Denken
```

---

# KAPITEL 2 — Die Grundbegriffe 

---

## 2.1 Enterprise — Was ist ein "Unternehmen" im TOGAF-Sinne?

**TOGAF-Definition:**
> Ein "Enterprise" ist jede Sammlung von Organisationen,
> die gemeinsame Ziele verfolgen.

Das kann sein:
- Ein ganzes Unternehmen (z.B. Siemens AG)
- Eine Abteilung (z.B. IT-Abteilung)
- Eine Behörde (z.B. das BAMF)
- Ein Verbund aus mehreren Organisationen
- Eine Lieferkette mit Partnern und Lieferanten

**Wichtig:** Enterprise = nicht zwingend "privates Unternehmen"!
Auch Behörden, NGOs, Universitäten können ein "Enterprise" sein.

---

## 2.2 Architecture — Was ist "Architektur" im TOGAF-Sinne?

Das Wort "Architektur" hat in TOGAF zwei Bedeutungen:

**Bedeutung 1: Das Ergebnis (das "Was")**
> Die formale Beschreibung eines Systems oder einer Komponente,
> einschließlich ihrer Beziehungen zur Umgebung.

**Bedeutung 2: Der Prozess (das "Wie")**
> Die Methode, mit der eine solche Beschreibung erstellt wird.

**Einfach erklärt:**
Architektur ist sowohl der Bauplan (das Dokument)
als auch die Tätigkeit des Planens (der Prozess).

---

## 2.3 Enterprise Architecture — Das Gesamtbild

**TOGAF-Definition:**
> Enterprise Architecture ist eine kohärente Beschreibung
> der Struktur und des Verhaltens eines Unternehmens,
> die Business, Information, Anwendungen und Technologie
> in Beziehung zueinander setzt.

**Einfach erklärt:**
EA ist der Masterplan eines Unternehmens.
Er zeigt, wie alles zusammenhängt:
- Geschäftsprozesse
- Informationen und Daten
- Anwendungen und Systeme
- Technische Infrastruktur

---

## 2.4 Stakeholder — Wer sind die Beteiligten?

**Definition:**
> Ein Stakeholder ist eine Person oder Gruppe,
> die ein Interesse an einem System oder einer Architektur hat.

**Arten von Stakeholdern:**

```
STAKEHOLDER-TYPEN

INTERN:
├── Geschäftsführung / C-Level (CEO, CIO, CFO)
│   → Interesse: Strategische Ausrichtung, Kosten, Risiko
├── Fachbereiche (HR, Finance, Operations)
│   → Interesse: Funktionalität, Prozesse, Effizienz
├── IT-Abteilung
│   → Interesse: Technische Machbarkeit, Standards
└── Mitarbeiter
    → Interesse: Benutzerfreundlichkeit, Arbeitsabläufe

EXTERN:
├── Kunden
│   → Interesse: Service-Qualität, Datenschutz
├── Lieferanten / Partner
│   → Interesse: Schnittstellen, Integration
├── Regulatoren / Behörden
│   → Interesse: Compliance, Gesetze
└── Investoren
    → Interesse: Rendite, Risikomanagement
```

---

## 2.5 Concern — Was beschäftigt die Stakeholder?

**Definition:**
> Ein Concern ist ein spezifisches Interesse oder eine Sorge,
> die ein Stakeholder in Bezug auf die Architektur hat.

**Beispiele:**

| Stakeholder | Concern (Sorge / Interesse) |
|-------------|----------------------------|
| CEO | "Wie hilft die neue Architektur, Kosten zu senken?" |
| CIO | "Wie sicher sind unsere Daten?" |
| Fachbereich | "Kann ich meinen Prozess weiterhin so durchführen?" |
| Regulator | "Erfüllen wir alle gesetzlichen Anforderungen?" |
| Mitarbeiter | "Muss ich alles neu lernen?" |

**Warum ist das wichtig?**
Als Architekt musst du die Concerns aller Stakeholder kennen
und in deiner Architektur adressieren.

---

## 2.6 Architecture View & Viewpoint

Diese zwei Begriffe werden oft verwechselt.

**Viewpoint (Standpunkt):**
> Die Definition, welche Aspekte einer Architektur
> für einen bestimmten Stakeholder relevant sind.

**View (Ansicht):**
> Die konkrete Darstellung der Architektur
> aus einem bestimmten Blickwinkel.

**Analogie:**
Stell dir ein Gebäude vor.
- Der **Viewpoint** des Elektriker ist: "Zeig mir alle Leitungen."
- Die **View** ist: der konkrete Elektroplan des Gebäudes.

**Beispiel in TOGAF:**

```
VIEWPOINT: "Sicherheitsarchitektur"
→ Relevant für: CISO, Compliance-Abteilung, Regulatoren
→ Zeigt: Sicherheitszonen, Zugriffsrechte, Verschlüsselung

VIEW: Das konkrete Sicherheitsarchitektur-Diagramm
→ Enthält: Firewall-Positionen, VPN-Verbindungen,
           Authentifizierungssysteme
```

---

## 2.7 Baseline vs. Target Architecture

Zwei der wichtigsten Begriffe im ADM:

**Baseline Architecture (IST-Zustand):**
> Die aktuelle Architektur — wie das Unternehmen heute aufgestellt ist.

**Target Architecture (SOLL-Zustand):**
> Die zukünftige Architektur — wie das Unternehmen
> nach der Transformation aussehen soll.

**Die Lücke dazwischen = Gap**
Diese Lücke zu analysieren und zu schließen ist
eine der Kernaufgaben des Enterprise Architekten.

```
BASELINE ──────────────────────────────► TARGET
(IST)         GAP ANALYSIS              (SOLL)
              ↕ Was fehlt?
              ↕ Was muss geändert werden?
              ↕ Was kann bleiben?
```

---

## 2.8 Architecture Principle — Grundsätze der Architektur

**Definition:**
> Ein Architecture Principle ist eine allgemeine Regel oder Leitlinie,
> die die Nutzung und den Einsatz von Ressourcen und Assets
> im gesamten Unternehmen regelt.

**Einfach erklärt:**
Prinzipien sind die "Spielregeln" der Architektur.
Sie gelten für alle Entscheidungen.

**Beispiele für Architecture Principles:**

```
PRINZIP 1: "Data is an Asset"
→ Bedeutung: Daten werden wie wertvolle Unternehmensressourcen
  behandelt und entsprechend geschützt und verwaltet.

PRINZIP 2: "Technology Independence"
→ Bedeutung: Anwendungen sind unabhängig von spezifischer
  Technologie entwickelt. Technologiewechsel sind möglich.

PRINZIP 3: "Single Source of Truth"
→ Bedeutung: Jede Information hat genau eine autoritative Quelle.
  Keine Datenduplizierung ohne Synchronisation.

PRINZIP 4: "Security by Design"
→ Bedeutung: Sicherheit wird von Anfang an eingebaut,
  nicht nachträglich hinzugefügt.
```

Jedes Prinzip hat in TOGAF eine standardisierte Struktur:

```
STRUKTUR EINES TOGAF PRINCIPLES:

Name:          Kurzer, prägnanter Name
Statement:     Was das Prinzip aussagt (1-2 Sätze)
Rationale:     Warum dieses Prinzip wichtig ist
Implications:  Was das Prinzip für die Praxis bedeutet
```

---

# KAPITEL 3 — Die vier Architekturdomänen {#kapitel-3}

---

## 3.1 Überblick: BDAT

TOGAF strukturiert Enterprise Architecture in vier Domänen.
Das Akronym ist **BDAT**:

```
╔══════════════════════════════════════════════════════════════╗
║              DIE VIER ARCHITEKTURDOMÄNEN                    ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  B — BUSINESS ARCHITECTURE                                  ║
║      "Wie funktioniert das Unternehmen?"                    ║
║      → Prozesse, Fähigkeiten, Wertströme,                  ║
║        Organisationsstruktur, Geschäftsmodell               ║
║                                                              ║
║  D — DATA ARCHITECTURE                                      ║
║      "Welche Daten brauchen wir?"                           ║
║      → Datenmodelle, Datenflüsse, Datenhaltung,            ║
║        Datenqualität, Master Data Management                ║
║                                                              ║
║  A — APPLICATION ARCHITECTURE                               ║
║      "Welche Anwendungen unterstützen uns?"                 ║
║      → Anwendungslandschaft, Schnittstellen,               ║
║        Anwendungsintegration, APIs                          ║
║                                                              ║
║  T — TECHNOLOGY ARCHITECTURE                                ║
║      "Auf welcher Infrastruktur läuft alles?"               ║
║      → Server, Netzwerke, Cloud, Middleware,               ║
║        Betriebssysteme, Datenbanken                         ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 3.2 Business Architecture — Die Geschäftsarchitektur

Die Business Architecture beschreibt das Unternehmen
aus einer geschäftlichen Perspektive.

**Was sie enthält:**

```
BUSINESS ARCHITECTURE INHALTE:

├── Business Model
│   → Wie schafft das Unternehmen Wert?
│
├── Business Capabilities
│   → Was kann das Unternehmen?
│   → Beispiel: "Kreditvergabe", "Schadenabwicklung"
│
├── Value Streams
│   → Wie fließt Wert durch das Unternehmen?
│   → Beispiel: "Kunde stellt Antrag → Prüfung → Entscheidung"
│
├── Business Processes
│   → Wie werden Aufgaben durchgeführt?
│
├── Organization Structure
│   → Wer macht was? Welche Einheiten gibt es?
│
├── Roles & Actors
│   → Welche Rollen gibt es? Wer interagiert mit dem System?
│
└── Business Services
    → Welche Services bietet das Unternehmen an?
```

**Konkretes Beispiel — Bank:**

```
BUSINESS ARCHITECTURE EINER BANK

Business Capability: "Kreditvergabe"
├── Sub-Capability: Kreditantrag entgegennehmen
├── Sub-Capability: Bonität prüfen
├── Sub-Capability: Kreditentscheidung treffen
├── Sub-Capability: Kreditvertrag erstellen
└── Sub-Capability: Kredit auszahlen

Value Stream: "Kredit vergeben"
Trigger: Kunde stellt Kreditantrag
Value Item: Ausgezahlter Kredit + Kundenbeziehung

Stage 1: Antrag aufnehmen
Stage 2: Unterlagen prüfen
Stage 3: Bonitätsprüfung
Stage 4: Entscheidung
Stage 5: Vertragsabschluss
Stage 6: Auszahlung
```

---

## 3.3 Data Architecture — Die Datenarchitektur

Die Data Architecture beschreibt,
wie Daten im Unternehmen strukturiert, gespeichert
und genutzt werden.

**Was sie enthält:**

```
DATA ARCHITECTURE INHALTE:

├── Datenmodelle (konzeptuell, logisch, physisch)
│   → Welche Entitäten gibt es? Wie hängen sie zusammen?
│
├── Datenflüsse
│   → Woher kommen Daten? Wohin fließen sie?
│
├── Data Governance
│   → Wer ist verantwortlich für welche Daten?
│
├── Master Data Management
│   → Welche Daten sind "die Wahrheit"?
│
├── Datenqualität
│   → Wie sichern wir Vollständigkeit und Korrektheit?
│
└── Datenschutz & Compliance
    → DSGVO, Aufbewahrungsfristen, Zugriffsrechte
```

**Konkretes Beispiel — Bank:**

```
DATA ARCHITECTURE EINER BANK

Kernentitäten:
├── Kunde (KundenID, Name, Adresse, Bonität)
├── Konto (KontoNr, Typ, Saldo, Inhaber)
├── Kredit (KreditID, Betrag, Laufzeit, Zinssatz)
├── Transaktion (TransaktionsID, Betrag, Datum, Von, Nach)
└── Produkt (ProduktID, Typ, Konditionen)

Master Data: Kundendaten
→ Einzige Quelle: CRM-System
→ Alle anderen Systeme lesen von dort

Datenfluss:
Online-Banking → API → Core-Banking-System → DWH → Reporting
```

---

## 3.4 Application Architecture — Die Anwendungsarchitektur

Die Application Architecture beschreibt
die Anwendungslandschaft des Unternehmens.

**Was sie enthält:**

```
APPLICATION ARCHITECTURE INHALTE:

├── Anwendungslandschaft (Application Portfolio)
│   → Welche Anwendungen gibt es?
│
├── Anwendungsinteraktionen
│   → Welche Anwendungen kommunizieren miteinander?
│
├── APIs & Schnittstellen
│   → Wie tauschen Anwendungen Daten aus?
│
├── Anwendungslebenszyklus
│   → Welche Anwendungen werden abgelöst?
│   → Welche werden neu eingeführt?
│
└── Anwendungsintegration
    → ESB, Middleware, Microservices, SOA
```

**Konkretes Beispiel — Bank:**

```
APPLICATION LANDSCAPE EINER BANK

FRONTEND:
├── Online-Banking Portal (Web)
├── Mobile Banking App (iOS/Android)
└── Filialsystem (Teller-Workstation)

MIDDLEWARE:
├── API Gateway
├── ESB (Enterprise Service Bus)
└── Identity & Access Management

BACKEND:
├── Core-Banking-System (z.B. Temenos, SAP Banking)
├── CRM-System (Kundendaten)
├── Kreditvergabesystem
├── Compliance & Reporting System
└── Data Warehouse

EXTERNE SYSTEME:
├── SCHUFA (Bonitätsprüfung)
├── SWIFT (Internationale Überweisungen)
└── Bundesbank (Meldewesen)
```

---

## 3.5 Technology Architecture — Die Technologiearchitektur

Die Technology Architecture beschreibt
die technische Infrastruktur, auf der alles läuft.

**Was sie enthält:**

```
TECHNOLOGY ARCHITECTURE INHALTE:

├── Server & Computing
│   → On-Premise, Cloud, Hybrid
│
├── Netzwerkarchitektur
│   → LAN, WAN, VPN, DMZ, Firewall
│
├── Middleware
│   → Betriebssysteme, Datenbanken, Application Server
│
├── Cloud-Architektur
│   → IaaS, PaaS, SaaS
│   → AWS, Azure, Google Cloud
│
├── Sicherheitsarchitektur
│   → Verschlüsselung, Authentifizierung, Monitoring
│
└── Disaster Recovery & Business Continuity
    → Backup, Failover, RTO, RPO
```

---

## 3.6 Wie die vier Domänen zusammenhängen

Die vier Domänen sind nicht unabhängig.
Sie bauen aufeinander auf:

```
ZUSAMMENHANG DER VIER DOMÄNEN

BUSINESS ARCHITECTURE
"Wir wollen Kredite digital vergeben"
         │
         │ definiert Anforderungen für
         ▼
DATA ARCHITECTURE
"Wir brauchen: Kundendaten, Bonitätsdaten, Kreditdaten"
         │
         │ definiert Anforderungen für
         ▼
APPLICATION ARCHITECTURE
"Wir brauchen: CRM, Kreditvergabesystem, SCHUFA-Anbindung"
         │
         │ definiert Anforderungen für
         ▼
TECHNOLOGY ARCHITECTURE
"Wir brauchen: Server, Netzwerk, Sicherheit, Cloud"

→ Änderungen in der Business Architecture
  haben Auswirkungen auf alle anderen Domänen!
```

---

# KAPITEL 4 — Der ADM: Das Herzstück von TOGAF 10

---

## 4.1 Was ist der ADM?

**ADM = Architecture Development Method**

Der ADM ist der Kernprozess von TOGAF.
Er beschreibt, wie du Schritt für Schritt
eine Enterprise Architecture entwickelst.

**Wichtige Eigenschaften des ADM:**

```
╔══════════════════════════════════════════════════════════════╗
║              EIGENSCHAFTEN DES ADM                          ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  ITERATIV                                                    ║
║  → Du durchläufst den ADM mehrfach                         ║
║  → Jeder Zyklus verfeinert die Architektur                 ║
║                                                              ║
║  FLEXIBEL                                                    ║
║  → Du kannst Phasen überspringen oder anpassen             ║
║  → Du kannst den ADM mit Agile kombinieren                 ║
║                                                              ║
║  VOLLSTÄNDIG                                                 ║
║  → Vom ersten Gedanken bis zur Umsetzung                   ║
║  → Von der Vision bis zur Governance                       ║
║                                                              ║
║  GENERISCH                                                   ║
║  → Gilt für jede Branche                                   ║
║  → Gilt für jede Unternehmensgröße                         ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 4.2 Die ADM-Phasen im Überblick

```
                    ┌─────────────────┐
                    │  PRELIMINARY    │
                    │  PHASE          │
                    │  "Vorbereitung" │
                    └────────┬────────┘
                             │
              ┌──────────────▼──────────────┐
              │                             │
    ┌─────────▼──────────┐     ┌────────────▼────────┐
    │   PHASE H           │     │   PHASE A           │
    │   Architecture      │     │   Architecture      │
    │   Change Mgmt       │     │   Vision            │
    └─────────┬──────────┘     └────────────┬────────┘
              │                             │
    ┌─────────▼──────────┐     ┌────────────▼────────┐
    │   PHASE G           │     │   PHASE B           │
    │   Implementation    │     │   Business          │
    │   Governance        │     │   Architecture      │
    └─────────┬──────────┘     └────────────┬────────┘
              │                             │
    ┌─────────▼──────────┐     ┌────────────▼────────┐
    │   PHASE F           │     │   PHASE C           │
    │   Migration         │     │   Information       │
    │   Planning          │     │   Systems Arch.     │
    └─────────┬──────────┘     └────────────┬────────┘
              │                             │
    ┌─────────▼──────────┐     ┌────────────▼────────┐
    │   PHASE E           │◄────┤   PHASE D           │
    │   Opportunities     │     │   Technology        │
    │   & Solutions       │     │   Architecture      │
    └─────────────────────┘     └─────────────────────┘
                    ▲
                    │
         ┌──────────┴──────────┐
         │  REQUIREMENTS       │
         │  MANAGEMENT         │
         │  (zentral, immer    │
         │   aktiv)            │
         └─────────────────────┘
```

---

## 4.3 Jede Phase hat dieselbe Grundstruktur

Jede ADM-Phase folgt demselben Muster:

```
STRUKTUR JEDER ADM-PHASE:

1. OBJECTIVES (Ziele)
   → Was soll diese Phase erreichen?

2. INPUTS (Eingaben)
   → Was brauche ich, um diese Phase zu starten?
   → Welche Dokumente und Informationen?

3. STEPS (Schritte)
   → Was tue ich in dieser Phase?
   → In welcher Reihenfolge?

4. OUTPUTS (Ausgaben)
   → Was produziere ich in dieser Phase?
   → Welche Deliverables entstehen?
```

---

## 4.4 Iteration im ADM

Der ADM ist kein linearer Prozess.
Er ist iterativ — du kannst und sollst zurückgehen.

**Drei Arten von Iteration:**

```
ITERATION 1: INNERHALB EINER PHASE
→ Du arbeitest eine Phase mehrfach durch,
  bis das Ergebnis gut genug ist.

ITERATION 2: ZWISCHEN PHASEN
→ Du gehst von Phase C zurück zu Phase B,
  weil du neue Erkenntnisse gewonnen hast.

ITERATION 3: ZWISCHEN ADM-ZYKLEN
→ Du startest einen neuen ADM-Zyklus,
  weil sich die Anforderungen geändert haben.
```

---

# KAPITEL 5 — Preliminary Phase 

---

## 5.1 Was ist die Preliminary Phase?

Die Preliminary Phase ist die **Vorbereitung** vor dem eigentlichen ADM.

Sie beantwortet die Frage:
> "Wie müssen wir TOGAF für unser Unternehmen anpassen,
> bevor wir anfangen?"

**Analogie:**
Bevor ein Koch kocht, richtet er seine Küche ein.
Er schärft die Messer, legt die Zutaten bereit,
und stellt sicher, dass alles funktioniert.
Das ist die Preliminary Phase.

---

## 5.2 Was passiert in der Preliminary Phase?

```
PRELIMINARY PHASE — AKTIVITÄTEN

1. TOGAF ANPASSEN (Tailoring)
   → Welche ADM-Phasen sind für uns relevant?
   → Welche können wir überspringen?
   → Wie nennen wir unsere Deliverables?

2. ARCHITECTURE PRINCIPLES DEFINIEREN
   → Welche Grundsätze gelten für alle Architekturentscheidungen?
   → Beispiel: "Cloud-First", "API-First", "Security by Design"

3. ARCHITECTURE CAPABILITY AUFBAUEN
   → Wer sind unsere Architekten?
   → Welche Rollen gibt es?
   → Welche Tools nutzen wir?

4. ARCHITECTURE GOVERNANCE ETABLIEREN
   → Wie werden Architekturentscheidungen getroffen?
   → Wer hat das letzte Wort?
   → Wie werden Abweichungen behandelt?

5. ARCHITECTURE FRAMEWORK AUSWÄHLEN
   → Nutzen wir TOGAF pur?
   → Kombinieren wir mit anderen Frameworks?
   → Welche Modellierungssprache? (ArchiMate, UML, BPMN)
```

---

## 5.3 Konkrete Outputs der Preliminary Phase

```
OUTPUTS DER PRELIMINARY PHASE:

├── Architecture Principles Dokument
│   → Liste aller Architecture Principles mit Begründung
│
├── Tailored Architecture Framework
│   → Angepasster ADM für das Unternehmen
│
├── Architecture Governance Framework
│   → Prozesse und Strukturen für EA-Governance
│
├── Architecture Repository (initial)
│   → Erste Struktur des Architecture Repository
│
└── Request for Architecture Work (optional)
    → Formaler Auftrag für die Architekturarbeit
```

---

## 5.4 Fallbeispiel: Preliminary Phase beim BAMF

**Situation:**
Das Bundesamt für Migration und Flüchtlinge (BAMF)
möchte seine IT-Landschaft modernisieren.
Bevor der ADM startet, muss die Preliminary Phase durchgeführt werden.

```
PRELIMINARY PHASE — BAMF

SCHRITT 1: TOGAF ANPASSEN
→ Relevante Phasen: Alle (A bis H)
→ Besonderheit: Öffentlicher Sektor — strenge Compliance
→ Zusätzliche Frameworks: BSI-Grundschutz, DSGVO, OZG

SCHRITT 2: ARCHITECTURE PRINCIPLES
→ Prinzip 1: "Datenschutz by Design"
  Statement: Alle Systeme werden mit Datenschutz
  als Kernprinzip entwickelt.
  Rationale: DSGVO-Compliance, Schutz sensibler Asylantragsdaten
  Implications: Privacy Impact Assessment für jedes Projekt

→ Prinzip 2: "Interoperabilität"
  Statement: Alle Systeme müssen mit Behörden-IT
  interoperabel sein.
  Rationale: Datenaustausch mit Ausländerbehörden,
  Verwaltungsgerichten, EUAA
  Implications: Standardisierte APIs, XÖV-Standards

→ Prinzip 3: "Cloud-Kompatibilität"
  Statement: Neue Systeme müssen cloud-fähig sein.
  Rationale: Skalierbarkeit bei Antragspeaks
  Implications: BSI C5-Zertifizierung für Cloud-Anbieter

SCHRITT 3: ARCHITECTURE CAPABILITY
→ Chief Architect: 1 Person (Gesamtverantwortung)
→ Business Architects: 3 Personen (Fachprozesse)
→ IT Architects: 4 Personen (Systeme, Daten, Technologie)
→ Tool: Sparx Enterprise Architect

SCHRITT 4: GOVERNANCE
→ Architecture Board: CIO + Fachbereichsleiter + Chief Architect
→ Entscheidungsfrequenz: Monatlich
→ Eskalationsweg: Architecture Board → CIO → BMI
```

---

# KAPITEL 6 — Phase A: Architecture Vision

---

## 6.1 Was ist Phase A?

Phase A ist der **Start** jedes ADM-Zyklus.

Sie beantwortet die Fragen:
- **Warum** machen wir das?
- **Was** wollen wir erreichen?
- **Wer** ist beteiligt?
- **Wie weit** geht unser Scope?

**Analogie:**
Bevor ein Architekt einen Bauplan zeichnet,
spricht er mit dem Bauherrn.
Was soll das Gebäude leisten? Wie groß? Für wen?
Das ist Phase A.

---

## 6.2 Die wichtigsten Aktivitäten in Phase A

```
PHASE A — AKTIVITÄTEN

1. STAKEHOLDER IDENTIFIZIEREN
   → Wer ist betroffen?
   → Wer hat Einfluss?
   → Wer muss eingebunden werden?

2. CONCERNS VERSTEHEN
   → Was beschäftigt die Stakeholder?
   → Welche Sorgen haben sie?
   → Welche Erwartungen haben sie?

3. SCOPE DEFINIEREN
   → Welche Teile des Unternehmens sind betroffen?
   → Welche Domänen? (B, D, A, T)
   → Welcher Zeitraum?

4. BUSINESS SCENARIO ENTWICKELN
   → Was ist das Problem?
   → Was ist das Ziel?
   → Welche Akteure sind beteiligt?

5. ARCHITECTURE VISION ERSTELLEN
   → High-Level-Beschreibung der Zielarchitektur
   → Noch keine Details — nur das große Bild

6. STATEMENT OF ARCHITECTURE WORK
   → Formaler Auftrag für die Architekturarbeit
   → Scope, Zeitplan, Ressourcen, Deliverables
```

---

## 6.3 Das Business Scenario — Detaillierte Erklärung

Das Business Scenario ist ein zentrales Werkzeug in Phase A.

**Was ist ein Business Scenario?**
> Ein Business Scenario beschreibt ein Geschäftsproblem
> oder eine Geschäftsmöglichkeit, die durch die Architektur
> adressiert werden soll.

**Aufbau eines Business Scenarios:**

```
BUSINESS SCENARIO STRUKTUR

1. PROBLEM / DRIVER
   → Was ist das Problem oder die Chance?
   → Warum handeln wir jetzt?

2. ENVIRONMENT
   → In welchem Kontext findet das statt?
   → Welche Rahmenbedingungen gelten?

3. OBJECTIVES
   → Was wollen wir konkret erreichen?
   → Messbare Ziele (SMART)

4. HUMAN ACTORS
   → Welche Menschen sind beteiligt?
   → Welche Rollen spielen sie?

5. COMPUTER ACTORS
   → Welche IT-Systeme sind beteiligt?
   → Welche Systeme müssen neu gebaut werden?

6. ROLES & RESPONSIBILITIES
   → Wer ist für was verantwortlich?
```

---

## 6.4 Fallbeispiel: Phase A beim BAMF

```
PHASE A — BAMF: "Digitales Asylverfahren 2026"

STAKEHOLDER:
├── BMI (Bundesministerium des Innern) — Auftraggeber
├── BAMF-Präsident — Sponsor
├── Asylbewerber — Endnutzer
├── BAMF-Entscheider — Hauptnutzer
├── Ausländerbehörden — Datenlieferanten
├── Verwaltungsgerichte — Empfänger von Bescheiden
├── Bundesbeauftragter für Datenschutz — Compliance
└── Bundestag — Politische Kontrolle

CONCERNS:
├── BMI: "Verfahren müssen schneller werden"
├── BAMF-Präsident: "Wir brauchen mehr Personal oder bessere Tools"
├── Asylbewerber: "Ich will wissen, wo mein Antrag steht"
├── Entscheider: "Das System muss einfacher zu bedienen sein"
├── Datenschutzbeauftragter: "Keine Datenpannen!"
└── Verwaltungsgericht: "Bescheide müssen rechtssicher sein"

SCOPE:
├── Domänen: Business + Data + Application (Prio)
│            Technology (sekundär)
├── Zeitraum: 3 Jahre (2024-2026)
├── Geografisch: Alle BAMF-Standorte in Deutschland
└── Ausgeschlossen: Integrationskurse (separates Projekt)

ARCHITECTURE VISION:
"Das BAMF der Zukunft bearbeitet Asylanträge vollständig digital,
trifft Entscheidungen in unter 6 Monaten und tauscht Daten
nahtlos mit allen relevanten Behörden aus — sicher,
datenschutzkonform und bürgerfreundlich."

BUSINESS SCENARIO:
Problem: Ø 8+ Monate Verfahrensdauer, 200.000 offene Fälle,
         papierbasierte Prozesse
Ziel: Ø <6 Monate, volldigitale Akte, Systemintegration
Akteure: Antragsteller, Entscheider, Dolmetscher,
         MARiS-System, EURODAC, E-Akte (neu)
```

---

## 6.5 Der Scope-Rahmen: Was gehört dazu?

Scope zu definieren bedeutet, vier Dimensionen festzulegen:

```
SCOPE-DIMENSIONEN

1. BREADTH (Breite)
   → Welche Teile des Unternehmens sind betroffen?
   → Beispiel: Nur Asylverfahren, nicht Integrationskurse

2. DEPTH (Tiefe)
   → Wie detailliert wird die Architektur?
   → Beispiel: Konzeptuell (grob) oder physisch (detailliert)?

3. TIME PERIOD (Zeitraum)
   → Welcher Zeitraum wird betrachtet?
   → Beispiel: 3-Jahres-Horizont (2024-2026)

4. ARCHITECTURE DOMAINS (Domänen)
   → Welche der vier Domänen werden bearbeitet?
   → Beispiel: B + D + A (nicht T in dieser Phase)
```

---

# KAPITEL 7 — Phase B: Business Architecture

---

## 7.1 Was ist Phase B?

Phase B entwickelt die **Business Architecture**.

Sie beantwortet die Fragen:
- Wie funktioniert das Unternehmen heute? (Baseline)
- Wie soll es in Zukunft funktionieren? (Target)
- Was muss sich ändern? (Gap)

**Warum ist Phase B so wichtig?**
Die Business Architecture ist das Fundament.
Alle anderen Domänen (D, A, T) bauen darauf auf.
Wenn Phase B falsch ist, ist alles andere falsch.

---

## 7.2 Aktivitäten in Phase B

```
PHASE B — AKTIVITÄTEN

1. BASELINE BUSINESS ARCHITECTURE ENTWICKELN
   → Wie funktioniert das Unternehmen heute?
   → Welche Prozesse, Capabilities, Strukturen gibt es?

2. TARGET BUSINESS ARCHITECTURE ENTWICKELN
   → Wie soll das Unternehmen in Zukunft funktionieren?
   → Welche neuen Capabilities werden benötigt?

3. GAP ANALYSIS DURCHFÜHREN
   → Was fehlt? Was muss geändert werden?
   → Was kann bleiben?

4. STAKEHOLDER CONCERNS ADRESSIEREN
   → Welche Views brauchen welche Stakeholder?

5. ARCHITECTURE DEFINITION DOCUMENT ERSTELLEN
   → Dokumentation der Business Architecture
```

---

## 7.3 Business Capabilities — Detaillierte Erklärung

**Was ist eine Business Capability?**
> Eine Business Capability ist eine bestimmte Fähigkeit,
> die ein Unternehmen besitzt oder braucht,
> um einen bestimmten Zweck zu erfüllen.

**Wichtige Eigenschaften:**
- Eine Capability beschreibt das **WAS**, nicht das **WIE**
- Sie ist stabil — Prozesse ändern sich, Capabilities selten
- Sie ist unabhängig von Organisationsstruktur und Technologie

**Beispiel:**
```
CAPABILITY: "Kreditwürdigkeitsprüfung"

WAS sie bedeutet:
Die Fähigkeit, die Bonität eines Kreditnehmers zu beurteilen.

WAS sie NICHT bedeutet:
→ Nicht: "Wir nutzen SCHUFA" (das ist Technologie)
→ Nicht: "Die Risikomanagement-Abteilung macht das" (das ist Org)
→ Nicht: "Wir prüfen Einkommensnachweise" (das ist Prozess)

Die Capability existiert unabhängig davon,
WER sie ausführt und WIE sie ausgeführt wird.
```

---

## 7.4 Capability Map erstellen — Schritt für Schritt

**Schritt 1: Capabilities sammeln**

Brainstorming mit Fachexperten:
"Was muss unser Unternehmen können?"

**Schritt 2: Gruppieren**

Capabilities in logische Gruppen einteilen:
- Kernaufgaben (Mission Capabilities)
- Steuerung (Governance Capabilities)
- Unterstützung (Enabling Capabilities)

**Schritt 3: Hierarchie bilden**

Capabilities haben Ebenen:
- Level 1: Grob (z.B. "Kundenmanagement")
- Level 2: Mittel (z.B. "Kundenakquise", "Kundenbindung")
- Level 3: Detailliert (z.B. "Lead-Generierung", "Onboarding")

**Schritt 4: Heat Map erstellen**

Jede Capability bewerten:
- Wie gut ist sie heute? (Performance)
- Wie wichtig ist sie für die Zukunft? (Priorität)

```
HEAT MAP LEGENDE:
🔴 ROT    = Schwach + Hohe Priorität → Sofort investieren!
🟡 GELB   = Mittel + Mittlere Priorität → Beobachten
🟢 GRÜN   = Stark + Niedrige Priorität → Beibehalten
⚪ GRAU   = Nicht relevant → Auslagern oder einstellen
```

---

## 7.5 Value Streams — Detaillierte Erklärung

**Was ist ein Value Stream?**
> Ein Value Stream ist eine End-to-End-Sammlung von Aktivitäten,
> die ein Gesamtergebnis für einen Kunden, Stakeholder
> oder Endnutzer erzeugt.

**Wichtige Begriffe:**

```
VALUE STREAM BEGRIFFE:

STAKEHOLDER:
→ Für wen wird der Wert erzeugt?
→ Beispiel: Kreditnehmer, Asylbewerber, Patient

TRIGGERING EVENT:
→ Was löst den Value Stream aus?
→ Beispiel: Kreditantrag, Asylantrag, Krankenhausaufnahme

VALUE ITEM:
→ Was ist das Ergebnis des Value Streams?
→ Was bekommt der Stakeholder am Ende?
→ Beispiel: Ausgezahlter Kredit, Schutzstatus, Behandlung

STAGE:
→ Ein Abschnitt des Value Streams
→ Jede Stage erzeugt einen Teilwert
→ Jede Stage hat einen Eintritt und einen Austritt

VALUE STAGE EXIT CRITERIA:
→ Wann ist eine Stage abgeschlossen?
→ Was muss erfüllt sein, um zur nächsten Stage zu gehen?
```

---

## 7.6 Value Stream Mapping — Schritt für Schritt

**Schritt 1: Stakeholder identifizieren**
Für wen wird der Wert erzeugt?

**Schritt 2: Triggering Event identifizieren**
Was löst den Prozess aus?

**Schritt 3: Value Item definieren**
Was ist das Endergebnis?

**Schritt 4: Stages definieren**
Welche Schritte gibt es von Trigger bis Value Item?

**Schritt 5: Capabilities mappen**
Welche Capabilities werden in jeder Stage benötigt?

**Schritt 6: Schwachstellen identifizieren**
Wo gibt es Engpässe? Wo fehlen Capabilities?

---

## 7.7 Vollständiges Fallbeispiel: Value Stream "Kredit vergeben"

```
VALUE STREAM: "Kredit vergeben"
STAKEHOLDER: Privatkunde
TRIGGER: Kreditantrag wird gestellt
VALUE ITEM: Ausgezahlter Kredit + Bestätigung

┌──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
│ Stage 1  │ Stage 2  │ Stage 3  │ Stage 4  │ Stage 5  │ Stage 6  │
│          │          │          │          │          │          │
│ Antrag   │ Unterlagen│ Bonitäts-│ Kredit-  │ Vertrag  │ Kredit   │
│ aufnehmen│ prüfen   │ prüfung  │ entscheid│ abschl.  │ auszahl. │
│          │          │          │          │          │          │
│ VALUE:   │ VALUE:   │ VALUE:   │ VALUE:   │ VALUE:   │ VALUE:   │
│ Antrag   │ Vollstän-│ Risiko   │ Ja/Nein  │ Rechts-  │ Geld auf │
│ erfasst  │ digkeit  │ bekannt  │ Entsch.  │ sicherh. │ dem Konto│
│          │ bestätigt│          │          │          │          │
└────┬─────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┘
     │          │          │          │          │          │
CAPABILITIES:
├─S1─┤ Antragsmgmt, Kundenmgmt, Dokumentenmgmt
├─S2─┤ Dokumentenprüfung, Compliance-Prüfung
├─S3─┤ Bonitätsprüfung, Risikomanagement, SCHUFA-Anbindung
├─S4─┤ Kreditentscheidung, Qualitätssicherung
├─S5─┤ Vertragserstellung, Rechtsprüfung
└─S6─┤ Zahlungsabwicklung, Kontomanagement

SCHWACHSTELLEN (IST-ZUSTAND):
🔴 Stage 2: Manuelle Prüfung dauert 3-5 Tage
🔴 Stage 3: SCHUFA-Abfrage nicht automatisiert
🟡 Stage 5: Vertrag noch in Papierform

ZIEL (TARGET):
✅ Stage 2: Automatische Vollständigkeitsprüfung → 1 Stunde
✅ Stage 3: Automatische SCHUFA-Abfrage → Echtzeit
✅ Stage 5: Digitale Unterschrift → 1 Tag
```

---

## 7.8 Information Mapping

**Was ist Information Mapping?**
> Information Mapping zeigt, welche Informationen
> zwischen Akteuren fließen — unabhängig von IT-Systemen.

**Wichtig:** Information Mapping ist NICHT dasselbe wie
ein Datenmodell oder ein Systemdiagramm!
Es zeigt den logischen Informationsfluss,
nicht die technische Implementierung.

**Beispiel — Kreditvergabe:**

```
INFORMATION MAP — KREDITVERGABE

EXTERNE QUELLEN          BANK-INTERN              EMPFÄNGER
────────────────         ──────────────           ──────────

Kunde ─────────────────► Antragsdaten ───────────► Sachbearbeiter
(Antrag, Nachweise)      │
                         ▼
SCHUFA ─────────────────► Bonitätsdaten ──────────► Kreditentscheider
(Score, Negativmerkmale) │
                         ▼
Bundesbank ─────────────► Regulatorische ──────────► Compliance
(Meldepflichten)         Daten                       Abteilung
                         │
                         ▼
                         Kreditakte ────────────────► Buchaltung
                         │                            Controlling
                         ▼
                         Kreditvertrag ─────────────► Kunde
                         │
                         ▼
                         Meldedaten ────────────────► Bundesbank
                                                      (Reporting)
```

---

## 7.9 Organization Mapping

**Was ist Organization Mapping?**
> Organization Mapping zeigt die Beziehungen,
> Abhängigkeiten und Informationsflüsse
> zwischen Organisationseinheiten.

**Wichtiger Unterschied:**

```
ORGANIGRAMM vs. ORGANIZATION MAP

ORGANIGRAMM:                    ORGANIZATION MAP:
"Wer ist wem unterstellt?"      "Wer braucht was von wem?"

CEO                             Vertrieb ──Leads──► Marketing
├── CFO                         Vertrieb ──Aufträge──► Produktion
├── CIO                         Produktion ──Lieferstatus──► Logistik
└── COO                         Logistik ──Rechnung──► Finance
    ├── Produktion
    ├── Logistik
    └── Vertrieb

→ Zeigt Hierarchie                → Zeigt Abhängigkeiten
→ Statisch                        → Dynamisch
→ Für HR                          → Für Architekten
```

---

# 📌 KAPITEL 8 — Phase C: Information Systems Architecture

---

## 8.1 Was ist Phase C?

Phase C entwickelt die **Information Systems Architecture**.
Sie besteht aus zwei Teilen:

```
PHASE C = DATA ARCHITECTURE + APPLICATION ARCHITECTURE

PART 1: DATA ARCHITECTURE
→ Welche Daten brauchen wir?
→ Wie sind sie strukturiert?
→ Wo werden sie gespeichert?
→ Wie fließen sie?

PART 2: APPLICATION ARCHITECTURE
→ Welche Anwendungen brauchen wir?
→ Wie interagieren sie?
→ Welche Schnittstellen gibt es?
→ Was wird abgelöst? Was bleibt?
```

---

## 8.2 Data Architecture — Schritt für Schritt

**Schritt 1: Datenentitäten identifizieren**

Aus der Business Architecture ableiten:
Welche Informationen werden in den Value Streams benötigt?

**Schritt 2: Konzeptuelles Datenmodell erstellen**

Entitäten und ihre Beziehungen darstellen.
Noch keine technischen Details!

**Schritt 3: Logisches Datenmodell erstellen**

Attribute hinzufügen.
Beziehungen präzisieren (1:1, 1:n, n:m).

**Schritt 4: Datenflüsse modellieren**

Woher kommen die Daten?
Wohin fließen sie?
Wer darf sie lesen? Wer darf sie ändern?

**Schritt 5: Data Governance definieren**

Wer ist Data Owner?
Welche Qualitätsstandards gelten?
Wie lange werden Daten aufbewahrt?

---

## 8.3 Fallbeispiel: Data Architecture beim BAMF

```
DATA ARCHITECTURE — BAMF

SCHRITT 1: KERNENTITÄTEN
├── Antragsteller (PersonID, Name, Geburtsdatum, Nationalität)
├── Asylantrag (AntragID, Datum, Status, Antragsteller)
├── Anhörungsprotokoll (ProtokollID, Datum, Inhalt, Antrag)
├── Entscheidung (EntscheidungID, Typ, Datum, Begründung)
├── Bescheid (BescheidID, Datum, Zustellungsdatum)
└── Dolmetscher (DolmetscherID, Sprachen, Verfügbarkeit)

SCHRITT 2: BEZIEHUNGEN
Antragsteller ──1:n──► Asylantrag
Asylantrag ──1:n──► Anhörungsprotokoll
Asylantrag ──1:1──► Entscheidung
Entscheidung ──1:1──► Bescheid
Anhörungsprotokoll ──n:1──► Dolmetscher

SCHRITT 3: DATENFLÜSSE
EURODAC ──Fingerabdrücke──► BAMF-System
Ausländerbehörde ──Meldedaten──► BAMF-System
BAMF-System ──Bescheid──► Antragsteller
BAMF-System ──Statistiken──► BMI
BAMF-System ──Falldaten──► Verwaltungsgericht

SCHRITT 4: DATA GOVERNANCE
Data Owner Antragstellerdaten: Referat "Registrierung"
Data Owner Entscheidungsdaten: Referat "Entscheidung"
Aufbewahrungsfrist: 10 Jahre nach Abschluss
Zugriff: Nur autorisierte Sachbearbeiter (Rollenkonzept)
Datenschutz: DSGVO Art. 9 (besondere Kategorien)
```

---

## 8.4 Application Architecture — Schritt für Schritt

**Schritt 1: Anwendungslandschaft erfassen (Baseline)**

Welche Anwendungen gibt es heute?
Was machen sie? Wie alt sind sie?

**Schritt 2: Anwendungslandschaft bewerten**

Welche Anwendungen sind fit für die Zukunft?
Welche müssen abgelöst werden?

**Schritt 3: Target Application Architecture definieren**

Welche Anwendungen brauchen wir in Zukunft?
Welche neuen Anwendungen werden eingeführt?

**Schritt 4: Integrationsarchitektur definieren**

Wie kommunizieren die Anwendungen?
APIs? Messaging? Dateiübertragung?

**Schritt 5: Transition planen**

In welcher Reihenfolge werden Anwendungen abgelöst?

---

## 8.5 Fallbeispiel: Application Architecture beim BAMF

```
APPLICATION ARCHITECTURE — BAMF

BASELINE (IST-ZUSTAND):
├── MARiS (Fallmanagementsystem) — VERALTET 🔴
│   → Alter: 15+ Jahre, keine Weiterentwicklung
│   → Problem: Keine API, keine Mobilfähigkeit
├── EURODAC-Client — VERALTET 🔴
│   → Manueller Abgleich, keine Automatisierung
├── Textverarbeitungssystem (MS Word) — MANUELL 🟡
│   → Bescheide werden manuell erstellt
├── E-Mail-System — STANDARD 🟢
│   → Interne Kommunikation
└── Statistiksystem — AUSREICHEND 🟡
    → Reporting für BMI

TARGET (SOLL-ZUSTAND):
├── Neues Fallmanagementsystem (FMS 2.0) ← NEU
│   → Webbasiert, API-first, mobil
│   → Integriert: Dokumentenmgmt, Workflow
├── EURODAC-Anbindung (automatisiert) ← MODERNISIERT
│   → Echtzeit-Abgleich via API
├── E-Akte-System ← NEU
│   → Volldigitale Akte, revisionssicher
├── Bescheidgenerator ← NEU
│   → Automatische Bescheiderstellung aus Templates
├── Bürgerportal ← NEU
│   → Online-Antragstellung, Statusabfrage
└── Statistik & BI ← MODERNISIERT
    → Echtzeit-Dashboard für Management

INTEGRATIONSARCHITEKTUR:
Bürgerportal ──API──► FMS 2.0
FMS 2.0 ──API──► EURODAC
FMS 2.0 ──API──► E-Akte
FMS 2.0 ──API──► Bescheidgenerator
FMS 2.0 ──API──► Ausländerbehörden-System
FMS 2.0 ──ETL──► BI-System
```

---

# KAPITEL 9 — Phase D: Technology Architecture {#kapitel-9}

---

## 9.1 Was ist Phase D?

Phase D entwickelt die **Technology Architecture**.

Sie beantwortet die Fragen:
- Auf welcher Infrastruktur laufen unsere Anwendungen?
- Welche Technologien nutzen wir?
- Wie sichern wir Verfügbarkeit und Sicherheit?

---

## 9.2 Aktivitäten in Phase D

```
PHASE D — AKTIVITÄTEN

1. BASELINE TECHNOLOGY ARCHITECTURE
   → Welche Infrastruktur haben wir heute?
   → Server, Netzwerke, Cloud, Middleware

2. TARGET TECHNOLOGY ARCHITECTURE
   → Welche Infrastruktur brauchen wir?
   → On-Premise, Cloud, Hybrid?

3. TECHNOLOGIESTANDARDS DEFINIEREN
   → Welche Technologien sind erlaubt?
   → Welche sind verboten?

4. SICHERHEITSARCHITEKTUR
   → Wie schützen wir unsere Systeme?
   → Firewall, Verschlüsselung, IAM

5. GAP ANALYSIS
   → Was fehlt? Was muss beschafft werden?
```

---

## 9.3 Fallbeispiel: Technology Architecture beim BAMF

```
TECHNOLOGY ARCHITECTURE — BAMF

BASELINE (IST):
├── On-Premise Rechenzentrum (Nürnberg)
│   → Veraltete Hardware (10+ Jahre)
│   → Kein Redundanzkonzept
├── Netzwerk: TESTA-ng (Behördennetz)
├── Betriebssystem: Windows Server 2012 (End of Life!)
└── Datenbank: Oracle 11g (End of Life!)

TARGET (SOLL):
├── Hybrid Cloud
│   → Bundescloud (ITDZ Berlin) für unkritische Systeme
│   → On-Premise für hochsensible Asylantragsdaten
│   → BSI C5-zertifizierte Cloud-Anbieter
├── Netzwerk: TESTA-ng + Zero Trust Architecture
├── Betriebssystem: Windows Server 2022 / Linux
├── Datenbank: PostgreSQL (Open Source, DSGVO-konform)
├── Container: Kubernetes (für Skalierbarkeit)
└── Sicherheit:
    ├── Multi-Faktor-Authentifizierung (MFA)
    ├── End-to-End-Verschlüsselung
    ├── SIEM (Security Information & Event Management)
    └── BSI-Grundschutz Compliance

TECHNOLOGIESTANDARDS:
✅ Erlaubt: Open-Source-Datenbanken, REST-APIs, HTTPS
✅ Erlaubt: BSI-zertifizierte Cloud-Dienste
❌ Verboten: Proprietäre Formate ohne Exportmöglichkeit
❌ Verboten: Nicht-EU-Cloud-Anbieter für Asylantragsdaten
```

---

# KAPITEL 10 — Phase E: Opportunities & Solutions

---

## 10.1 Was ist Phase E?

Phase E ist der Übergang von der Architektur zur Umsetzung.

Sie beantwortet die Fragen:
- Wie setzen wir die Zielarchitektur um?
- Welche Projekte brauchen wir?
- In welcher Reihenfolge?
- Make or Buy? Eigenbau oder Standardsoftware?

---

## 10.2 Aktivitäten in Phase E

```
PHASE E — AKTIVITÄTEN

1. GAPS PRIORISIEREN
   → Welche Lücken sind am kritischsten?
   → Was hat den größten Business-Impact?

2. LÖSUNGSOPTIONEN BEWERTEN
   → Make: Eigenbau
   → Buy: Standardsoftware kaufen
   → Reuse: Vorhandenes wiederverwenden
   → Outsource: Auslagern

3. WORK PACKAGES DEFINIEREN
   → Welche Arbeitspakete gibt es?
   → Welche Abhängigkeiten bestehen?

4. ARCHITECTURE ROADMAP ERSTELLEN
   → Zeitplan für die Umsetzung
   → Meilensteine und Deliverables

5. TRANSITION ARCHITECTURES DEFINIEREN
   → Zwischenzustände auf dem Weg zur Zielarchitektur
```

---

## 10.3 Make or Buy — Entscheidungsrahmen

```
MAKE OR BUY ENTSCHEIDUNG

MAKE (Eigenbau):
✅ Wenn: Unique Business Requirements
✅ Wenn: Kein passendes Produkt am Markt
✅ Wenn: Strategischer Wettbewerbsvorteil
❌ Wenn: Hohe Kosten nicht gerechtfertigt
❌ Wenn: Standardanforderungen vorhanden

BUY (Standardsoftware):
✅ Wenn: Standardprozesse (HR, Finance, CRM)
✅ Wenn: Schnelle Implementierung nötig
✅ Wenn: Geringere Wartungskosten
❌ Wenn: Zu viel Customizing nötig
❌ Wenn: Vendor Lock-in inakzeptabel

REUSE (Wiederverwenden):
✅ Wenn: Vorhandene Systeme passen (mit Anpassung)
✅ Wenn: Investitionsschutz wichtig
❌ Wenn: Technologische Schulden zu hoch

OUTSOURCE (Auslagern):
✅ Wenn: Nicht-Kernkompetenz
✅ Wenn: Spezialisiertes Know-how extern vorhanden
❌ Wenn: Sicherheitskritische Daten involviert
❌ Wenn: Regulatorische Einschränkungen
```

---

## 10.4 Architecture Roadmap — Fallbeispiel BAMF

```
ARCHITECTURE ROADMAP — BAMF (2024-2026)

PHASE 1 (2024 Q1-Q2): FUNDAMENT
├── Projekt 1: Infrastruktur modernisieren
│   → Neue Server, Netzwerk, Sicherheit
│   → Dauer: 6 Monate, Budget: 2 Mio. €
└── Projekt 2: EURODAC-Anbindung automatisieren
    → Echtzeit-API-Anbindung
    → Dauer: 4 Monate, Budget: 500.000 €

PHASE 2 (2024 Q3 - 2025 Q2): KERNSYSTEME
├── Projekt 3: Neues Fallmanagementsystem (FMS 2.0)
│   → Ablösung von MARiS
│   → Dauer: 12 Monate, Budget: 8 Mio. €
└── Projekt 4: E-Akte einführen
    → Volldigitale Akte
    → Dauer: 8 Monate, Budget: 3 Mio. €

PHASE 3 (2025 Q3 - 2026 Q2): DIGITALISIERUNG
├── Projekt 5: Bürgerportal
│   → Online-Antragstellung
│   → Dauer: 6 Monate, Budget: 2 Mio. €
└── Projekt 6: Bescheidgenerator
    → Automatische Bescheiderstellung
    → Dauer: 4 Monate, Budget: 1 Mio. €

PHASE 4 (2026 Q3-Q4): OPTIMIERUNG
└── Projekt 7: BI & Analytics
    → Echtzeit-Reporting
    → Dauer: 4 Monate, Budget: 1 Mio. €

GESAMTBUDGET: ~17,5 Mio. €
GESAMTDAUER: 3 Jahre
```

---

# KAPITEL 11 — Phase F: Migration Planning

---

## 11.1 Was ist Phase F?

Phase F erstellt den detaillierten **Migrationsplan**.

Sie beantwortet die Fragen:
- Wie kommen wir vom IST zum SOLL?
- In welcher Reihenfolge migrieren wir?
- Wie minimieren wir Risiken während der Migration?

---

## 11.2 Transition Architectures

Ein wichtiges Konzept in Phase F:

**Was sind Transition Architectures?**
> Transition Architectures sind Zwischenzustände
> auf dem Weg von der Baseline zur Target Architecture.

**Warum brauchen wir sie?**
Man kann nicht von heute auf morgen alles ändern.
Es braucht Zwischenschritte, die stabil und funktionsfähig sind.

```
TRANSITION ARCHITECTURES — BEISPIEL BAMF

BASELINE (2024):
MARiS (alt) + Papierakte + Manuelle EURODAC-Abfrage

TRANSITION 1 (Ende 2024):
MARiS (alt) + Digitale Akte (neu) + Automatische EURODAC-Abfrage
→ Erste Digitalisierung, MARiS noch im Einsatz

TRANSITION 2 (Mitte 2025):
FMS 2.0 (neu) + Digitale Akte + EURODAC-API
→ MARiS abgelöst, neues System im Einsatz

TARGET (Ende 2026):
FMS 2.0 + E-Akte + EURODAC-API + Bürgerportal + Bescheidgenerator
→ Volldigitales BAMF
```

---

## 11.3 Migrationsrisiken managen

```
RISIKOMANAGEMENT IN PHASE F

RISIKO 1: Datenverlust bei Migration
Wahrscheinlichkeit: Mittel
Impact: Hoch
Maßnahme: Vollständiges Backup vor Migration,
           Parallelbetriebs-Phase, Rollback-Plan

RISIKO 2: Mitarbeiter nicht geschult
Wahrscheinlichkeit: Hoch
Impact: Mittel
Maßnahme: Schulungsplan 3 Monate vor Go-Live,
           Key-User-Konzept, Helpdesk

RISIKO 3: Systemausfall während Migration
Wahrscheinlichkeit: Niedrig
Impact: Sehr hoch
Maßnahme: Migration außerhalb Geschäftszeiten,
           Fallback-Szenario definiert,
           Kommunikationsplan vorbereitet

RISIKO 4: Datenschutzverletzung
Wahrscheinlichkeit: Niedrig
Impact: Sehr hoch
Maßnahme: DSGVO-Folgenabschätzung,
           Datenschutzbeauftragten einbinden,
           Verschlüsselung während Migration
```

---

# 📌 KAPITEL 12 — Phase G: Implementation Governance

---

## 12.1 Was ist Phase G?

Phase G stellt sicher, dass die Umsetzung
**konform** zur Architektur erfolgt.

Sie beantwortet die Fragen:
- Setzen die Projekte die Architektur korrekt um?
- Gibt es Abweichungen? Sind sie akzeptabel?
- Wie werden Architekturentscheidungen dokumentiert?

---

## 12.2 Architecture Compliance

**Was ist Architecture Compliance?**
> Architecture Compliance prüft, ob ein Projekt
> die definierten Architekturstandards und -prinzipien einhält.

**Compliance-Level:**

```
COMPLIANCE-LEVEL

CONFORMANT (Konform):
→ Das Projekt erfüllt alle Architekturvorgaben.
→ Keine Abweichungen.

COMPLIANT (Entsprechend):
→ Das Projekt erfüllt die wesentlichen Vorgaben.
→ Kleinere Abweichungen sind dokumentiert und genehmigt.

NON-CONFORMANT (Nicht konform):
→ Das Projekt weicht von Architekturvorgaben ab.
→ Eskalation zum Architecture Board erforderlich.

EXEMPT (Ausgenommen):
→ Das Projekt ist von bestimmten Vorgaben ausgenommen.
→ Formale Ausnahmegenehmigung liegt vor.
```

---

## 12.3 Architecture Compliance Review

Ein Architecture Compliance Review ist eine formale Prüfung:

```
COMPLIANCE REVIEW PROZESS

1. TRIGGER
   → Projekt beantragt Compliance Review
   → Oder: Architecture Board initiiert Review

2. VORBEREITUNG
   → Projektunterlagen sammeln
   → Architekturvorgaben zusammenstellen
   → Review-Team zusammenstellen

3. REVIEW DURCHFÜHREN
   → Projektarchitektur gegen Vorgaben prüfen
   → Abweichungen identifizieren
   → Begründungen anhören

4. ERGEBNIS DOKUMENTIEREN
   → Compliance-Status festhalten
   → Abweichungen dokumentieren
   → Empfehlungen formulieren

5. ENTSCHEIDUNG
   → Architecture Board entscheidet
   → Konform: Projekt kann fortfahren
   → Nicht konform: Projekt muss angepasst werden
```

---

# KAPITEL 13 — Phase H: Architecture Change Management

---

## 13.1 Was ist Phase H?

Phase H verwaltet **Änderungen** an der Architektur.

Sie beantwortet die Fragen:
- Was hat sich geändert? (Intern oder extern)
- Müssen wir die Architektur anpassen?
- Wann starten wir einen neuen ADM-Zyklus?

---

## 13.2 Arten von Architekturänderungen

```
ÄNDERUNGSTYPEN

TYP 1: SIMPLIFICATION CHANGE
→ Kleine Änderung, kein neuer ADM-Zyklus nötig
→ Beispiel: Neue Version einer Anwendung
→ Prozess: Dokumentieren + Architecture Board informieren

TYP 2: INCREMENTAL CHANGE
→ Mittlere Änderung, Teile des ADM werden wiederholt
→ Beispiel: Neue Geschäftsanforderung
→ Prozess: Betroffene ADM-Phasen wiederholen

TYP 3: RE-ARCHITECTING CHANGE
→ Große Änderung, neuer ADM-Zyklus erforderlich
→ Beispiel: Neue Digitalstrategie, Fusion, Gesetzesänderung
→ Prozess: ADM von Phase A neu starten
```

---

## 13.3 Trigger für Architecture Change Management

```
CHANGE TRIGGER — BEISPIELE

INTERN:
├── Neue Geschäftsstrategie
├── Fusion oder Akquisition
├── Neue Produkte oder Services
├── Reorganisation
└── Technologische Innovation

EXTERN:
├── Neue Gesetze oder Regulierungen (z.B. DSGVO, NIS2)
├── Marktveränderungen
├── Neue Technologien (z.B. KI, Blockchain)
├── Neue Wettbewerber
└── Wirtschaftliche Veränderungen
```

---

# KAPITEL 14 — Requirements Management

---

## 14.1 Was ist Requirements Management?

Requirements Management ist **keine Phase** — es ist ein **kontinuierlicher Prozess**.

Er läuft durch alle ADM-Phasen hindurch.

**Was er tut:**
- Anforderungen sammeln
- Anforderungen priorisieren
- Anforderungen verfolgen
- Anforderungen bei Änderungen aktualisieren

---

## 14.2 Anforderungstypen in TOGAF

```
ANFORDERUNGSTYPEN

BUSINESS REQUIREMENTS:
→ Was das Unternehmen braucht
→ Beispiel: "Verfahrensdauer unter 6 Monate"

ARCHITECTURE REQUIREMENTS:
→ Was die Architektur leisten muss
→ Beispiel: "System muss 10.000 gleichzeitige Nutzer unterstützen"

FUNCTIONAL REQUIREMENTS:
→ Was das System tun muss
→ Beispiel: "Nutzer kann Antragsstatus online abfragen"

NON-FUNCTIONAL REQUIREMENTS:
→ Wie gut das System es tun muss
→ Beispiel: "Antwortzeit < 2 Sekunden, Verfügbarkeit 99,9%"

CONSTRAINTS:
→ Einschränkungen, die nicht verhandelbar sind
→ Beispiel: "Muss DSGVO-konform sein", "Budget max. 5 Mio. €"
```

---

# KAPITEL 15 — Architecture Principles

---

## 15.1 Warum sind Principles so wichtig?

Architecture Principles sind die **Leitplanken** aller Architekturentscheidungen.

Ohne Principles:
- Jeder Architekt entscheidet nach eigenem Ermessen
- Inkonsistente Architektur
- Konflikte zwischen Projekten

Mit Principles:
- Einheitliche Entscheidungsgrundlage
- Konsistente Architektur
- Schnellere Entscheidungen

---

## 15.2 Vollständiges Beispiel: Architecture Principles Katalog

```
ARCHITECTURE PRINCIPLES KATALOG — BEISPIELUNTERNEHMEN

PRINZIP 1: PRIMACY OF PRINCIPLES
Name: Vorrang der Prinzipien
Statement: Diese Prinzipien gelten für alle IT-Entscheidungen.
           Ausnahmen erfordern formale Genehmigung.
Rationale: Konsistenz und Steuerbarkeit der Architektur.
Implications: Architecture Board prüft alle Ausnahmen.

PRINZIP 2: MAXIMIZE BENEFIT TO THE ENTERPRISE
Name: Maximaler Unternehmensnutzen
Statement: Architekturentscheidungen maximieren den Nutzen
           für das gesamte Unternehmen, nicht einzelne Bereiche.
Rationale: Verhindert Silo-Denken und lokale Optimierung.
Implications: Kosten-Nutzen-Analyse für alle Projekte.

PRINZIP 3: INFORMATION MANAGEMENT IS EVERYBODY'S BUSINESS
Name: Informationsmanagement ist Chefsache
Statement: Alle Mitarbeiter sind verantwortlich für
           die Qualität der Daten, die sie verwalten.
Rationale: Datenqualität ist Grundlage aller Entscheidungen.
Implications: Schulungen, Data Stewards, Qualitätsmessungen.

PRINZIP 4: BUSINESS CONTINUITY
Name: Geschäftskontinuität
Statement: Unternehmensbetrieb muss auch bei IT-Ausfällen
           aufrechterhalten werden.
Rationale: IT-Ausfälle dürfen nicht das Geschäft stoppen.
Implications: Backup-Systeme, Failover, Notfallpläne.

PRINZIP 5: COMMON USE APPLICATIONS
Name: Gemeinsame Nutzung von Anwendungen
Statement: Anwendungen werden unternehmensweit geteilt,
           nicht für einzelne Bereiche entwickelt.
Rationale: Kosteneffizienz, Konsistenz, Wartbarkeit.
Implications: Anwendungskatalog, Genehmigungsprozess.

PRINZIP 6: SERVICE ORIENTATION
Name: Serviceorientierung
Statement: Funktionalität wird als Services bereitgestellt,
           die von anderen Systemen genutzt werden können.
Rationale: Flexibilität, Wiederverwendbarkeit, Integration.
Implications: API-First-Design, Service-Katalog.

PRINZIP 7: COMPLIANCE WITH LAW
Name: Gesetzeskonformität
Statement: Alle IT-Systeme müssen geltendes Recht einhalten.
Rationale: Rechtssicherheit, Vermeidung von Bußgeldern.
Implications: Legal Review für alle neuen Systeme.

PRINZIP 8: IT RESPONSIBILITY
Name: IT-Verantwortung
Statement: Die IT-Abteilung ist verantwortlich für
           den sicheren und effizienten Betrieb aller Systeme.
Rationale: Klare Verantwortlichkeiten.
Implications: SLAs, Monitoring, Incident Management.

PRINZIP 9: PROTECTION OF INTELLECTUAL PROPERTY
Name: Schutz des geistigen Eigentums
Statement: Geistiges Eigentum wird durch geeignete Maßnahmen
           geschützt.
Rationale: Wettbewerbsvorteil, rechtliche Anforderungen.
Implications: Zugriffskontrollen, DRM, Vertragsklauseln.

PRINZIP 10: DATA IS AN ASSET
Name: Daten sind ein Vermögenswert
Statement: Daten werden wie wertvolle Unternehmensressourcen
           behandelt.
Rationale: Daten sind die Grundlage für Entscheidungen.
Implications: Data Governance, Datenqualitätsprogramm.
```

---

# KAPITEL 16 — Deliverables, Artifacts & Building Blocks

---

## 16.1 Die drei Kategorien

TOGAF unterscheidet drei Arten von Arbeitsergebnissen:

```
╔══════════════════════════════════════════════════════════════╗
║         DELIVERABLES vs. ARTIFACTS vs. BUILDING BLOCKS      ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  DELIVERABLE (Lieferergebnis):                              ║
║  → Formal vereinbart und genehmigt                         ║
║  → Von Stakeholdern unterzeichnet                          ║
║  → Beispiel: Architecture Definition Document              ║
║  → Beispiel: Statement of Architecture Work                ║
║                                                              ║
║  ARTIFACT (Artefakt):                                       ║
║  → Beschreibt einen Aspekt der Architektur                 ║
║  → Teil eines Deliverables                                 ║
║  → Drei Typen:                                             ║
║    - Catalog (Liste): z.B. Anwendungskatalog               ║
║    - Matrix (Tabelle): z.B. Anwendungs-Daten-Matrix        ║
║    - Diagram (Bild): z.B. Prozessdiagramm                  ║
║                                                              ║
║  BUILDING BLOCK (Baustein):                                 ║
║  → Wiederverwendbare Komponente                            ║
║  → Zwei Typen:                                             ║
║    - ABB: Architecture Building Block (konzeptuell)        ║
║    - SBB: Solution Building Block (konkret)                ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 16.2 Wichtige Deliverables pro Phase

```
DELIVERABLES PRO ADM-PHASE

PRELIMINARY:
→ Architecture Principles Document
→ Tailored Architecture Framework

PHASE A:
→ Architecture Vision Document
→ Statement of Architecture Work
→ Business Scenario

PHASE B:
→ Business Architecture Definition Document
→ Business Architecture Roadmap (Draft)
→ Gap Analysis Results

PHASE C:
→ Data Architecture Definition Document
→ Application Architecture Definition Document
→ Architecture Roadmap (Updated)

PHASE D:
→ Technology Architecture Definition Document
→ Architecture Roadmap (Updated)

PHASE E:
→ Architecture Roadmap (Final)
→ Transition Architecture Documents
→ Implementation and Migration Plan (Draft)

PHASE F:
→ Implementation and Migration Plan (Final)
→ Finalized Architecture Roadmap

PHASE G:
→ Architecture Compliance Assessments
→ Architecture Contract

PHASE H:
→ Architecture Updates
→ Change Requests
```

---

## 16.3 Artifacts — Kataloge, Matrizen, Diagramme

**Kataloge (Catalogs):**
Listen von Dingen.

```
BEISPIELE FÜR KATALOGE:
├── Principles Catalog (Liste aller Prinzipien)
├── Business Service/Function Catalog
├── Application Portfolio Catalog
├── Technology Standards Catalog
└── Requirements Catalog
```

**Matrizen (Matrices):**
Zeigen Beziehungen zwischen zwei Dingen.

```
BEISPIELE FÜR MATRIZEN:
├── Business Interaction Matrix
│   (Welche Org-Einheiten interagieren miteinander?)
├── Actor/Role Matrix
│   (Welche Akteure haben welche Rollen?)
├── Application/Function Matrix
│   (Welche Anwendung unterstützt welche Funktion?)
└── Data Entity/Business Function Matrix
    (Welche Daten werden in welcher Funktion genutzt?)
```

**Diagramme (Diagrams):**
Visuelle Darstellungen.

```
BEISPIELE FÜR DIAGRAMME:
├── Business Footprint Diagram
│   (Überblick: Treiber, Ziele, Capabilities, Org-Einheiten)
├── Value Chain Diagram
│   (Wertschöpfungskette)
├── Application Communication Diagram
│   (Welche Anwendungen kommunizieren miteinander?)
├── Technology Architecture Diagram
│   (Infrastruktur-Übersicht)
└── Architecture Roadmap
    (Zeitplan der Transformation)
```

---

## 16.4 Building Blocks — ABB vs. SBB

```
ABB vs. SBB — DER UNTERSCHIED

ABB (Architecture Building Block):
→ Konzeptuell, technologieneutral
→ Beschreibt WHAT (was gebraucht wird)
→ Beispiel: "Authentifizierungsservice"
→ Beispiel: "Dokumentenmanagementsystem"

SBB (Solution Building Block):
→ Konkret, technologiespezifisch
→ Beschreibt HOW (wie es realisiert wird)
→ Beispiel: "Microsoft Active Directory" (realisiert Authentifizierung)
→ Beispiel: "SharePoint" (realisiert Dokumentenmgmt)

BEZIEHUNG:
ABB "Authentifizierungsservice"
    └── wird realisiert durch ──► SBB "Microsoft Active Directory"
                                  SBB "Keycloak (Open Source)"
                                  SBB "AWS Cognito"

→ Der ABB bleibt stabil.
→ Der SBB kann sich ändern (Technologiewechsel).
```

---

# KAPITEL 17 — Enterprise Continuum & Architecture Repository 

---

## 17.1 Was ist der Enterprise Continuum?

Der Enterprise Continuum ist ein **Klassifizierungssystem**
für Architektur-Assets.

Er ordnet Assets von **generisch** bis **spezifisch**:

```
ENTERPRISE CONTINUUM

GENERISCH ◄─────────────────────────────────────────► SPEZIFISCH

Foundation          Common          Industry        Organization
Architecture        Systems         Specific        Specific
                    Architecture    Architecture    Architecture

Beispiel:
"Sicherheits-       "IT-Sicherheits- "Bank-Sicherheits- "Meine Bank-
 konzept            standards"        architektur"       Sicherheits-
 allgemein"                                              architektur"

→ Je weiter rechts, desto spezifischer für dein Unternehmen.
→ Du nutzt generische Assets als Ausgangspunkt
  und passt sie an deine Bedürfnisse an.
```

---

## 17.2 Architecture Repository — Der Wissensspeicher

Das Architecture Repository ist der **zentrale Speicher**
für alle Architektur-Assets.

```
ARCHITECTURE REPOSITORY — STRUKTUR

┌─────────────────────────────────────────────────────────────┐
│                   ARCHITECTURE REPOSITORY                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ARCHITECTURE METAMODEL                                     │
│  → Wie ist das Repository strukturiert?                    │
│  → Welche Typen von Entitäten gibt es?                     │
│                                                             │
│  ARCHITECTURE CAPABILITY                                    │
│  → Governance-Parameter des Repository                     │
│                                                             │
│  ARCHITECTURE LANDSCAPE                                     │
│  → Aktuelle Architektur des Unternehmens                   │
│  → Strategic Architecture (grob)                           │
│  → Segment Architecture (mittel)                           │
│  → Capability Architecture (detailliert)                   │
│                                                             │
│  STANDARDS LIBRARY                                          │
│  → Technologiestandards                                    │
│  → Produktstandards                                        │
│  → Compliance-Standards                                    │
│                                                             │
│  REFERENCE LIBRARY                                          │
│  → Templates, Patterns, Guidelines                         │
│  → Wiederverwendbare Bausteine                             │
│                                                             │
│  GOVERNANCE REPOSITORY                                      │
│  → Entscheidungen des Architecture Board                   │
│  → Compliance-Berichte                                     │
│  → Ausnahmegenehmigungen                                   │
│                                                             │
│  ARCHITECTURE REQUIREMENTS REPOSITORY                       │
│  → Alle genehmigten Anforderungen                         │
│                                                             │
│  SOLUTIONS LANDSCAPE                                        │
│  → Konkrete Lösungen (SBBs) im Einsatz                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

# KAPITEL 18 — Architecture Views & Viewpoints 

---

## 18.1 Warum brauchen wir verschiedene Views?

Ein Unternehmen ist komplex.
Kein einzelnes Diagramm kann alles zeigen.

Verschiedene Stakeholder brauchen verschiedene Perspektiven:

```
STAKEHOLDER UND IHRE VIEWS

CEO:
→ Braucht: Strategische Übersicht, Business-Impact
→ View: Business Footprint Diagram, Roadmap

CIO:
→ Braucht: IT-Landschaft, Systemintegration
→ View: Application Landscape, Technology Architecture

CISO:
→ Braucht: Sicherheitsarchitektur, Risiken
→ View: Security Architecture Diagram

Fachbereichsleiter:
→ Braucht: Prozesse, Capabilities
→ View: Value Stream, Business Process Diagram

Entwickler:
→ Braucht: Technische Details, APIs
→ View: Application Communication Diagram, API-Katalog
```

---

## 18.2 Viewpoint definieren — Schritt für Schritt

```
VIEWPOINT DEFINITION

1. STAKEHOLDER IDENTIFIZIEREN
   → Für wen ist dieser Viewpoint?

2. CONCERNS IDENTIFIZIEREN
   → Was interessiert diesen Stakeholder?

3. MODELL-TYPEN FESTLEGEN
   → Welche Diagramme, Kataloge, Matrizen?

4. NOTATION FESTLEGEN
   → ArchiMate? UML? BPMN? Eigene Notation?

5. BEISPIEL-VIEW ERSTELLEN
   → Muster-Darstellung für diesen Viewpoint
```

---

# KAPITEL 19 — Stakeholder Management 

---

## 19.1 Stakeholder-Analyse

Stakeholder-Management ist eine der wichtigsten
Fähigkeiten eines Enterprise Architekten.

**Stakeholder-Matrix:**

```
STAKEHOLDER-MATRIX (Einfluss vs. Interesse)

HOHER EINFLUSS
│
│  MANAGE CLOSELY          KEEP SATISFIED
│  (Eng einbinden)         (Zufrieden halten)
│  → Regelmäßige Meetings  → Regelmäßige Updates
│  → Entscheidungen        → Keine Überraschungen
│    abstimmen
│
├────────────────────────────────────────────────
│
│  MONITOR                 KEEP INFORMED
│  (Beobachten)            (Informieren)
│  → Gelegentliche Updates → Newsletter, Reports
│  → Bei Bedarf einbinden  → Kein aktiver Einbezug
│
NIEDRIGER EINFLUSS
         NIEDRIGES INTERESSE    HOHES INTERESSE
```

---

## 19.2 Stakeholder-Kommunikation

```
KOMMUNIKATIONSPLAN — BEISPIEL

STAKEHOLDER: CEO
Interesse: Strategischer Nutzen, Kosten
Kommunikationsformat: Executive Summary (1 Seite)
Frequenz: Monatlich
Medium: Persönliches Meeting + Bericht

STAKEHOLDER: CIO
Interesse: IT-Architektur, Risiken, Standards
Kommunikationsformat: Technischer Bericht
Frequenz: Wöchentlich
Medium: Architecture Board Meeting

STAKEHOLDER: Fachbereichsleiter
Interesse: Prozesse, Auswirkungen auf Arbeitsweise
Kommunikationsformat: Workshop, Präsentation
Frequenz: Bei Meilensteinen
Medium: Workshop + Präsentation

STAKEHOLDER: Mitarbeiter
Interesse: Auswirkungen auf den Arbeitsalltag
Kommunikationsformat: FAQ, Schulungen
Frequenz: Bei Go-Live
Medium: Intranet, Schulungen, Townhall
```

---

# KAPITEL 20 — Gap Analysis 

---

## 20.1 Was ist Gap Analysis?

**Gap Analysis** = Lückenanalyse

Sie vergleicht die Baseline Architecture (IST)
mit der Target Architecture (SOLL)
und identifiziert die Lücken (GAPS).

```
GAP ANALYSIS FRAMEWORK

BASELINE                    TARGET
(Was haben wir heute?)      (Was brauchen wir?)
        │                           │
        └───────────► GAP ◄─────────┘
                   (Was fehlt?)

GAP-TYPEN:
├── FEHLT KOMPLETT → Muss neu gebaut/beschafft werden
├── VORHANDEN, ABER UNZUREICHEND → Muss verbessert werden
├── VORHANDEN, ABER NICHT GENUTZT → Muss aktiviert werden
└── VORHANDEN, ABER NICHT MEHR NÖTIG → Kann abgeschaltet werden
```

---

## 20.2 Gap Analysis Tabelle — Beispiel

```
GAP ANALYSIS — BAMF APPLICATION ARCHITECTURE

CAPABILITY          BASELINE        TARGET          GAP
─────────────────────────────────────────────────────────────
Fallmanagement      MARiS (alt)     FMS 2.0 (neu)   ERSETZEN
                    Veraltet        Modern, API-fähig

Digitale Akte       Papierakte      E-Akte-System   NEU BAUEN
                    Nicht vorhanden Volldigital

EURODAC-Anbindung   Manuell         Automatisch     MODERNISIEREN
                    Batch-Abfrage   Echtzeit-API

Bürgerportal        Nicht vorhanden Online-Portal   NEU BAUEN
                                    Antragstellung

Bescheidgenerator   MS Word manuell Automatisiert   NEU BAUEN
                                    Template-basiert

Statistik/BI        Excel-Reports   Echtzeit-BI     MODERNISIEREN
                    Monatlich       Dashboard

E-Mail-System       MS Exchange     MS Exchange     KEIN GAP
                    Ausreichend     Ausreichend
```

---

# KAPITEL 21 — Business Capabilities & Value Streams 

---

## 21.1 Capability vs. Process vs. Function

Diese drei Begriffe werden oft verwechselt:

```
CAPABILITY vs. PROCESS vs. FUNCTION

CAPABILITY (Fähigkeit):
→ WAS das Unternehmen kann
→ Stabil, technologieneutral
→ Beispiel: "Kreditwürdigkeitsprüfung"

PROCESS (Prozess):
→ WIE etwas getan wird
→ Schritte, Reihenfolge, Regeln
→ Beispiel: "Kreditantrag prüfen:
   1. Antrag entgegennehmen
   2. Unterlagen prüfen
   3. SCHUFA abfragen..."

FUNCTION (Funktion):
→ WAS eine Organisationseinheit tut
→ Gebunden an Abteilung/Team
→ Beispiel: "Kreditabteilung prüft Bonität"

ZUSAMMENHANG:
Eine CAPABILITY wird durch PROCESSES realisiert,
die von einer FUNCTION (Abteilung) ausgeführt werden,
unterstützt durch Anwendungen und Technologie.
```

---

## 21.2 Capability Assessment — Detaillierte Methode

```
CAPABILITY ASSESSMENT METHODE

SCHRITT 1: CAPABILITY INVENTAR
→ Alle Capabilities auflisten

SCHRITT 2: BEWERTUNGSKRITERIEN FESTLEGEN
→ Performance (Wie gut ist die Capability heute?)
   Skala: 1 (sehr schlecht) bis 5 (exzellent)
→ Strategische Bedeutung (Wie wichtig ist sie?)
   Skala: 1 (unwichtig) bis 5 (kritisch)

SCHRITT 3: BEWERTUNG DURCHFÜHREN
→ Mit Fachexperten und Stakeholdern
→ Workshops, Interviews, Datenanalyse

SCHRITT 4: HEAT MAP ERSTELLEN
→ Performance vs. Strategische Bedeutung

SCHRITT 5: INVESTITIONSENTSCHEIDUNGEN ABLEITEN
→ Niedrige Performance + Hohe Bedeutung = Investieren!
→ Hohe Performance + Niedrige Bedeutung = Auslagern?

BEISPIEL:
Capability               Perf.  Strat.  Entscheidung
──────────────────────────────────────────────────────
Fallmanagement           2/5    5/5     🔴 Sofort investieren
Identitätsfeststellung   3/5    5/5     🟡 Verbessern
Integrationskurse        4/5    3/5     🟢 Beibehalten
Statistik/Reporting      3/5    2/5     ⚪ Optimieren/Auslagern
```

---

# KAPITEL 22 — Business Scenarios 

---

## 22.1 Business Scenarios im Detail

Business Scenarios sind ein zentrales Werkzeug in Phase A.

**Warum sind sie wichtig?**
Sie helfen dabei:
- Das Problem klar zu beschreiben
- Stakeholder zu alignen
- Anforderungen zu strukturieren
- Die Architecture Vision zu begründen

---

## 22.2 Business Scenario entwickeln — Schritt für Schritt

```
BUSINESS SCENARIO ENTWICKLUNG

SCHRITT 1: PROBLEM IDENTIFIZIEREN
→ Was ist das Problem oder die Chance?
→ Warum handeln wir jetzt?
→ Was passiert, wenn wir NICHT handeln?

SCHRITT 2: ENVIRONMENT BESCHREIBEN
→ Welche Rahmenbedingungen gelten?
→ Rechtlich, technisch, finanziell, politisch

SCHRITT 3: OBJECTIVES DEFINIEREN (SMART)
→ Spezifisch: Was genau wollen wir erreichen?
→ Messbar: Wie messen wir Erfolg?
→ Erreichbar: Ist es realistisch?
→ Relevant: Warum ist es wichtig?
→ Terminiert: Bis wann?

SCHRITT 4: AKTEURE IDENTIFIZIEREN
→ Human Actors: Welche Menschen sind beteiligt?
→ Computer Actors: Welche Systeme sind beteiligt?

SCHRITT 5: ROLLEN & VERANTWORTLICHKEITEN
→ Wer macht was?
→ Wer entscheidet?
→ Wer liefert?

SCHRITT 6: SZENARIEN DURCHSPIELEN
→ Wie sieht der Ablauf heute aus? (IST)
→ Wie soll er in Zukunft aussehen? (SOLL)
→ Was muss sich ändern?
```

---

## 22.3 Vollständiges Business Scenario — E-Commerce Unternehmen

```
BUSINESS SCENARIO: "Omnichannel-Transformation"

UNTERNEHMEN: RetailMax GmbH (fiktiv)
             Einzelhandel, 500 Filialen, 10.000 Mitarbeiter

PROBLEM:
→ Online-Umsatz stagniert bei 15% (Branche: 35%)
→ Kunden können online bestellen, aber nicht in Filiale
  zurückgeben (fehlende Integration)
→ Lagerbestand nicht in Echtzeit sichtbar
→ Kundendaten in 4 verschiedenen Systemen (Silos)
→ Wettbewerber bieten nahtloses Omnichannel-Erlebnis

ENVIRONMENT:
→ Rechtlich: DSGVO (Kundendaten), PCI-DSS (Zahlungen)
→ Technisch: Legacy-ERP (SAP R/3, 15 Jahre alt)
→ Finanziell: Budget 5 Mio. € über 2 Jahre
→ Markt: Wettbewerber Amazon, Zalando wachsen stark

OBJECTIVES:
→ Online-Anteil auf 30% steigern (bis Ende 2025)
→ Click & Collect in allen 500 Filialen (bis Q2 2025)
→ Einheitliches Kundenprofil (Single Customer View) bis Q4 2024
→ Echtzeit-Lagerbestand für Kunden sichtbar bis Q1 2025
→ Rückgabe online bestellter Ware in Filiale bis Q2 2025

HUMAN ACTORS:
├── Kunde (Online + Filiale)
├── Filialverkäufer
├── Online-Shop-Manager
├── Logistik-Mitarbeiter
├── IT-Abteilung
└── Geschäftsführung

COMPUTER ACTORS (IST):
├── SAP R/3 (ERP) — veraltet
├── Online-Shop-System (Magento) — isoliert
├── Filial-Kassensystem (NCR) — isoliert
├── CRM-System (Salesforce) — isoliert
└── Lagerverwaltungssystem (WMS) — isoliert

COMPUTER ACTORS (SOLL):
├── SAP S/4HANA (modernes ERP)
├── Unified Commerce Platform
├── Single Customer Data Platform
├── Echtzeit-Lagerbestandssystem
└── Omnichannel-API-Layer

ROLLEN:
Projektverantwortung: CIO
Business Sponsor: CMO
Architektur: Enterprise Architect (Lead)
Umsetzung: IT-Projekte + externe Systemintegratoren
```

---

# KAPITEL 23 — TOGAF mit anderen Frameworks 

---

## 23.1 TOGAF + Agile

TOGAF wird oft als "zu schwerfällig" für agile Umgebungen kritisiert.
Das stimmt nicht — wenn man es richtig macht.

```
TOGAF + AGILE — KOMBINATION

TOGAF LIEFERT:
→ Strategischen Rahmen (Warum? Was?)
→ Architecture Principles (Leitplanken)
→ Architecture Roadmap (Richtung)
→ Governance (Kontrolle)

AGILE LIEFERT:
→ Iterative Umsetzung (Wie? Wann?)
→ Schnelle Lieferung (Sprints)
→ Flexibilität bei Details
→ Kundenfeedback

KOMBINATION:
→ TOGAF definiert die "Architektur-Leitplanken"
→ Agile-Teams arbeiten innerhalb dieser Leitplanken
→ Architecture Reviews bei Sprint Reviews
→ Architektur wird iterativ verfeinert

TOGAF 10 SERIES GUIDE: "Applying the ADM Using Agile Sprints"
→ Offizieller Guide für TOGAF + Agile Kombination
```

---

## 23.2 TOGAF + ITIL

```
TOGAF + ITIL — KOMBINATION

TOGAF:                          ITIL:
→ Architektur entwickeln        → IT-Services betreiben
→ Strategisch                   → Operativ
→ Zukunftsorientiert            → Gegenwartsorientiert

ZUSAMMENSPIEL:
→ TOGAF definiert die Ziel-Architektur
→ ITIL definiert, wie die Services betrieben werden
→ TOGAF Phase G (Governance) nutzt ITIL-Prozesse
→ ITIL Change Management → TOGAF Phase H

BEISPIEL:
TOGAF definiert: "Wir brauchen einen API-Gateway"
ITIL definiert:  "So wird der API-Gateway betrieben,
                  gewartet und bei Ausfall wiederhergestellt"
```

---

## 23.3 TOGAF + ArchiMate

```
TOGAF + ARCHIMATE — DIE PERFEKTE KOMBINATION

TOGAF:                          ArchiMate:
→ Methode (WIE vorgehen?)       → Sprache (WIE darstellen?)
→ Prozess                       → Notation
→ Framework                     → Modellierungssprache

ArchiMate SCHICHTEN:
├── Motivation Layer
│   → Warum? (Treiber, Ziele, Prinzipien)
│   → Entspricht: TOGAF Preliminary + Phase A
│
├── Strategy Layer
│   → Capabilities, Value Streams
│   → Entspricht: TOGAF Phase B (grob)
│
├── Business Layer
│   → Prozesse, Funktionen, Rollen
│   → Entspricht: TOGAF Phase B (detailliert)
│
├── Application Layer
│   → Anwendungen, Daten
│   → Entspricht: TOGAF Phase C
│
└── Technology Layer
    → Infrastruktur
    → Entspricht: TOGAF Phase D

→ ArchiMate ist die empfohlene Modellierungssprache für TOGAF!
```

---

# KAPITEL 24 — Vollständiges Fallbeispiel: Digitale Transformation einer Behörde

---

## 24.1 Ausgangssituation

```
FALLBEISPIEL: STADTWERKE MUSTERSTADT (fiktiv)
Branche: Kommunaler Energieversorger
Größe: 800 Mitarbeiter, 150.000 Kunden
Problem: Digitalisierungsdruck, veraltete IT, Kundenverluste

AUSGANGSSITUATION:
├── Kundenverwaltung: SAP IS-U (15 Jahre alt)
├── Kundenportal: Nicht vorhanden
├── Zählerablesung: Manuell (Ablesedienst)
├── Rechnungsstellung: Papier (100%)
├── Kundenservice: Nur telefonisch und persönlich
└── Smart Meter: Nicht eingeführt

PROBLEME:
├── Kundenverluste: 5% p.a. an Wettbewerber
├── Hohe Prozesskosten (manuelle Ablesung: 2 Mio. €/Jahr)
├── Kundenzufriedenheit: 2,8/5 (Branchenschnitt: 3,8)
├── Regulatorisch: Smart Meter Pflicht ab 2025 (MessEG)
└── Mitarbeiterfluktuation: IT-Fachkräfte verlassen das Unternehmen
```

---

## 24.2 Preliminary Phase

```
PRELIMINARY PHASE — STADTWERKE MUSTERSTADT

ARCHITECTURE PRINCIPLES:
1. Kundenzentriertheit
   Statement: Alle Architekturentscheidungen priorisieren
              den Nutzen für den Kunden.
   Implications: Customer Journey Mapping vor jedem Projekt

2. Cloud-First
   Statement: Neue Systeme werden bevorzugt als Cloud-Dienste
              beschafft oder betrieben.
   Implications: Cloud-Anbieter müssen ISO 27001 zertifiziert sein

3. API-First
   Statement: Alle Systeme stellen ihre Funktionalität
              über APIs bereit.
   Implications: API-Design-Guidelines, API-Katalog

4. Datenschutz by Design
   Statement: Datenschutz wird von Anfang an eingebaut.
   Implications: DSGVO-Folgenabschätzung für alle Projekte

ARCHITECTURE TEAM:
├── Chief Architect: 1 (Vollzeit)
├── Business Architect: 1 (Teilzeit)
└── IT Architect: 2 (Vollzeit)

TOOLS:
→ Modellierung: Archi (kostenlos, ArchiMate)
→ Repository: Confluence (Atlassian)
→ Governance: Jira (Entscheidungsprotokoll)
```

---

## 24.3 Phase A — Architecture Vision

```
PHASE A — STADTWERKE MUSTERSTADT

STAKEHOLDER:
├── Geschäftsführer (Sponsor) — Interesse: Kosten, Wachstum
├── IT-Leiter — Interesse: Technologie, Sicherheit
├── Vertriebsleiter — Interesse: Kundenzufriedenheit, Umsatz
├── Betriebsleiter — Interesse: Prozesseffizienz
├── Betriebsrat — Interesse: Mitarbeiter, Datenschutz
├── Kunden — Interesse: Einfache Bedienung, Transparenz
└── Stadtrat — Interesse: Daseinsvorsorge, Kosten

ARCHITECTURE VISION:
"Die Stadtwerke Musterstadt werden bis 2026 zum
digitalen Vorreiter unter kommunalen Energieversorgern.
Kunden verwalten ihre Verträge vollständig digital,
Smart Meter ermöglichen Echtzeit-Transparenz,
und unsere Prozesse sind zu 80% automatisiert."

BUSINESS SCENARIO:
Problem: Kundenverluste, hohe Kosten, regulatorischer Druck
Ziel: Digitales Kundenerlebnis, Prozessautomatisierung
Trigger: Strategieentscheidung der Geschäftsführung
Value Item: Zufriedene Kunden, effiziente Prozesse

SCOPE:
├── Domänen: B + D + A + T (alle vier)
├── Zeitraum: 3 Jahre (2024-2026)
├── Geografisch: Versorgungsgebiet Musterstadt
└── Ausgeschlossen: Netzinfrastruktur (separates Projekt)
```

---

## 24.4 Phase B — Business Architecture

```
PHASE B — STADTWERKE MUSTERSTADT

CAPABILITY MAP:

KERNAUFGABEN:
├── Energieversorgung
│   ├── Stromerzeugung/-beschaffung
│   ├── Netzmanagement
│   └── Zählerablesung 🔴 (manuell, teuer)
├── Kundenmanagement
│   ├── Vertragsmanagement 🔴 (papierbasiert)
│   ├── Kundenkommunikation 🔴 (nur Telefon)
│   ├── Beschwerdemanagement 🟡
│   └── Kundenportal ❌ (nicht vorhanden)
└── Abrechnung
    ├── Rechnungsstellung 🔴 (100% Papier)
    ├── Zahlungsabwicklung 🟡
    └── Mahnwesen 🟡

STEUERUNG:
├── Regulatorisches Reporting 🟢
├── Datenschutz 🟡
└── Qualitätsmanagement 🟡

VALUE STREAM: "Energie liefern und abrechnen"
Trigger: Neuer Kunde schließt Vertrag ab
Value Item: Zuverlässige Energie + korrekte Rechnung

Stage 1: Vertragsabschluss (🔴 Nur persönlich/telefonisch)
Stage 2: Zählerinstallation (🟡 Manuell, 2-4 Wochen)
Stage 3: Energielieferung (🟢 Funktioniert gut)
Stage 4: Ablesung (🔴 Manuell, 1x jährlich)
Stage 5: Abrechnung (🔴 Papierrechnung, 4 Wochen)
Stage 6: Zahlung (🟡 Überweisung/SEPA)
Stage 7: Kundenservice (🔴 Nur Telefon, lange Wartezeiten)
```

---

## 24.5 Phase C — Information Systems Architecture

```
PHASE C — STADTWERKE MUSTERSTADT

DATA ARCHITECTURE:
Kernentitäten:
├── Kunde (KundenNr, Name, Adresse, Kontakt)
├── Vertrag (VertragsNr, Tarif, Laufzeit, Status)
├── Zähler (ZählerNr, Typ, Standort, Smart/Analog)
├── Ablesung (AblesungID, Datum, Wert, Methode)
├── Rechnung (RechnungsNr, Betrag, Zeitraum, Status)
└── Zahlung (ZahlungsID, Betrag, Datum, Methode)

Master Data: Kundendaten → SAP IS-U (Quelle der Wahrheit)

APPLICATION ARCHITECTURE (SOLL):
├── Kundenportal (NEU)
│   → Self-Service: Vertrag, Rechnung, Zählerstand
│   → Mobile App
├── Smart Meter Management System (NEU)
│   → Echtzeit-Datenerfassung
│   → Automatische Ablesung
├── CRM-System (MODERNISIERT)
│   → 360°-Kundensicht
│   → Omnichannel-Kommunikation
├── SAP S/4HANA Utilities (UPGRADE von IS-U)
│   → Modernes ERP für Energieversorger
├── E-Rechnungssystem (NEU)
│   → Digitale Rechnung (PDF, E-Mail)
│   → Papier nur auf Wunsch
└── API-Gateway (NEU)
    → Zentrale Schnittstelle für alle Systeme
```

---

## 24.6 Phase D — Technology Architecture

```
PHASE D — STADTWERKE MUSTERSTADT

TARGET TECHNOLOGY ARCHITECTURE:

CLOUD (Azure — ISO 27001, DSGVO-konform):
├── Kundenportal (Azure App Service)
├── CRM (Salesforce — SaaS)
├── E-Rechnungssystem (SaaS)
└── API-Gateway (Azure API Management)

ON-PREMISE (Rechenzentrum Musterstadt):
├── SAP S/4HANA (kritische Geschäftsdaten)
├── Smart Meter Management System
└── Backup & Disaster Recovery

NETZWERK:
├── Internet: 10 Gbit/s redundant
├── Smart Meter: LoRaWAN + LTE (Übertragungsprotokoll)
└── Intern: VLAN-Segmentierung

SICHERHEIT:
├── MFA für alle Mitarbeiter
├── Zero Trust Architecture
├── SIEM (Splunk)
└── Penetrationstests (jährlich)
```

---

## 24.7 Phase E — Opportunities & Solutions

```
PHASE E — STADTWERKE MUSTERSTADT

ARCHITECTURE ROADMAP:

Q1 2024: FUNDAMENT
├── API-Gateway einführen
└── SAP IS-U → S/4HANA Upgrade starten

Q2-Q3 2024: KUNDENDATEN
├── CRM-System einführen (Salesforce)
└── Single Customer View aufbauen

Q4 2024: DIGITALE RECHNUNG
└── E-Rechnungssystem einführen
    → Ziel: 50% digitale Rechnungen bis Ende 2024

Q1-Q2 2025: KUNDENPORTAL
└── Kundenportal launchen
    → Self-Service: Vertrag, Rechnung, Zählerstand

Q3-Q4 2025: SMART METER
└── Smart Meter Rollout (gesetzliche Pflicht)
    → Automatische Ablesung

Q1-Q2 2026: OPTIMIERUNG
└── KI-gestützte Tarifempfehlungen
└── Predictive Maintenance

BUDGET:
├── 2024: 2,5 Mio. €
├── 2025: 3,0 Mio. €
└── 2026: 1,5 Mio. €
GESAMT: 7,0 Mio. €

ERWARTETER ROI:
├── Einsparung Ablesung: 2,0 Mio. €/Jahr
├── Einsparung Papierrechnung: 0,3 Mio. €/Jahr
├── Kundenwachstum (5% → +2%): 1,5 Mio. €/Jahr
└── Break-Even: Ende 2027
```

---

# KAPITEL 25 — Vollständiges Fallbeispiel: E-Commerce 

---

## 25.1 Ausgangssituation

```
FALLBEISPIEL: FASHIONWORLD AG (fiktiv)
Branche: Online-Fashion-Handel
Größe: 200 Mitarbeiter, 500.000 Kunden
Umsatz: 80 Mio. € (Online: 100%)

AUSGANGSSITUATION:
├── Shop: Shopify (Monolith)
├── Lager: Eigenes WMS (veraltet)
├── CRM: HubSpot
├── ERP: Sage (klein, nicht skalierbar)
├── Zahlungen: Stripe + PayPal
└── Versand: DHL-Integration (manuell)

PROBLEME:
├── Wachstum begrenzt durch Shopify-Limits
├── Keine internationale Expansion möglich
│   (Mehrwährung, Mehrsprache fehlt)
├── Lager-WMS nicht mit Shop integriert
│   → Lagerbestand oft falsch im Shop
├── Retouren: Manueller Prozess, 5 Tage Bearbeitungszeit
└── Personalisierung: Nicht vorhanden
    → Alle Kunden sehen denselben Shop
```

---

## 25.2 ADM-Durchlauf kompakt

```
FASHIONWORLD — ADM KOMPAKT

PRELIMINARY:
→ Principles: Customer First, API-First, Cloud-Native
→ Team: 1 EA (extern), 1 IT-Architekt (intern)

PHASE A:
→ Vision: "Europas führende digitale Fashion-Plattform bis 2026"
→ Scope: Alle 4 Domänen, 2 Jahre, Europa-Expansion
→ Business Scenario:
   Problem: Wachstumslimits, schlechte Integration
   Ziel: Headless Commerce, Echtzeit-Lager, Personalisierung

PHASE B:
→ Capabilities:
   🔴 Internationalisierung (fehlt komplett)
   🔴 Echtzeit-Lagerbestand (fehlt)
   🔴 Personalisierung (fehlt)
   🟡 Retourenmanagement (vorhanden, aber langsam)
   🟢 Zahlungsabwicklung (gut)

→ Value Stream "Kauf abwickeln":
   Stage 1: Produkt entdecken (🔴 keine Personalisierung)
   Stage 2: Produkt auswählen (🟡 Lagerbestand unzuverlässig)
   Stage 3: Kaufen (🟢 gut)
   Stage 4: Lieferung (🟡 manuelle DHL-Integration)
   Stage 5: Retoure (🔴 5 Tage, manuell)

PHASE C:
→ Application Architecture (SOLL):
   ├── Headless Commerce Platform (Commercetools)
   ├── PIM (Akeneo — Produktdaten)
   ├── OMS (Order Management System)
   ├── WMS (modernisiert, API-fähig)
   ├── CDP (Customer Data Platform)
   ├── Personalisierungsengine (Algolia)
   └── Retourenportal (Selbstentwickelt)

PHASE D:
→ AWS (Cloud-Native)
→ Microservices-Architektur
→ Kubernetes für Skalierung

PHASE E:
→ Roadmap:
   Q1 2024: Headless Commerce Plattform
   Q2 2024: PIM + Echtzeit-Lager
   Q3 2024: Internationalisierung (DE, AT, CH, NL)
   Q4 2024: CDP + Personalisierung
   Q1 2025: Retourenportal
   Q2 2025: Expansion FR, IT, ES
```

---

# KAPITEL 26 — Häufige Fehler und wie du sie vermeidest 

---

## 26.1 Die 10 häufigsten TOGAF-Fehler

```
FEHLER 1: TOGAF ZU DOGMATISCH ANWENDEN
Problem: Alle Phasen sklavisch befolgen, auch wenn unnötig
Lösung: TOGAF ist ein Framework, kein Regelwerk.
        Passe es an deinen Kontext an!

FEHLER 2: ARCHITEKTUR OHNE STAKEHOLDER-EINBINDUNG
Problem: Architekt entwickelt Architektur im Elfenbeinturm
Lösung: Stakeholder von Anfang an einbinden.
        Architecture Vision gemeinsam entwickeln.

FEHLER 3: ZU VIEL DOKUMENTATION, ZU WENIG WERT
Problem: Hunderte Seiten Dokumentation, die niemand liest
Lösung: Fokus auf die Deliverables, die echten Wert liefern.
        "Just enough architecture"

FEHLER 4: BASELINE ARCHITECTURE NICHT KENNEN
Problem: Zielarchitektur ohne Kenntnis des IST-Zustands
Lösung: Immer zuerst die Baseline dokumentieren.
        Gap Analysis ist nur möglich, wenn du den IST kennst.

FEHLER 5: SCOPE CREEP
Problem: Scope wächst unkontrolliert
Lösung: Scope in Phase A klar definieren und dokumentieren.
        Änderungen formal genehmigen lassen.

FEHLER 6: KEINE GOVERNANCE
Problem: Architektur wird definiert, aber nicht durchgesetzt
Lösung: Architecture Board einrichten.
        Compliance Reviews durchführen.
        Konsequenzen bei Abweichungen definieren.

FEHLER 7: TECHNOLOGIE VOR BUSINESS
Problem: IT-Architekten starten mit Technologie,
         ohne Business Architecture zu verstehen
Lösung: Immer mit Phase B (Business) beginnen!
        Technologie folgt dem Business, nicht umgekehrt.

FEHLER 8: EINMALIGES PROJEKT STATT KONTINUIERLICHER PROZESS
Problem: TOGAF wird als einmaliges Projekt gesehen
Lösung: EA ist ein kontinuierlicher Prozess.
        Phase H (Change Management) ist genauso wichtig wie Phase A.

FEHLER 9: FEHLENDE KOMMUNIKATION
Problem: Architektur wird nicht kommuniziert
Lösung: Kommunikationsplan erstellen.
        Verschiedene Views für verschiedene Stakeholder.

FEHLER 10: KEINE MESSUNG DES ERFOLGS
Problem: Niemand weiß, ob die Architektur erfolgreich war
Lösung: KPIs definieren (in Phase A!).
        Regelmäßig messen und berichten.
```

---

## 26.2 Anti-Patterns in der Enterprise Architecture

```
ANTI-PATTERN 1: "THE IVORY TOWER ARCHITECT"
Beschreibung: Der Architekt sitzt im Büro und entwickelt
              perfekte Architekturen, die niemand umsetzt.
Symptome: Projekte ignorieren die Architektur,
          Architektur ist nicht praxistauglich.
Lösung: Architekten müssen in Projekten mitarbeiten.
        "Embedded Architect" in agilen Teams.

ANTI-PATTERN 2: "ARCHITECTURE FOR ARCHITECTURE'S SAKE"
Beschreibung: Architektur wird um ihrer selbst willen gemacht,
              nicht um Business-Probleme zu lösen.
Symptome: Niemand fragt nach der Architektur,
          kein messbarer Business-Nutzen.
Lösung: Jede Architekturaktivität muss einen Business-Treiber haben.

ANTI-PATTERN 3: "BIG BANG ARCHITECTURE"
Beschreibung: Alles auf einmal ändern.
Symptome: Projekte scheitern, weil sie zu groß sind.
Lösung: Inkrementelle Transformation.
        Transition Architectures nutzen.

ANTI-PATTERN 4: "VENDOR-DRIVEN ARCHITECTURE"
Beschreibung: Technologieentscheidungen werden von
              Herstellern getroffen, nicht von Architekten.
Symptome: Vendor Lock-in, hohe Kosten, schlechte Integration.
Lösung: Architecture Principles zuerst,
        dann Technologieauswahl.
```

---

# KAPITEL 27 — Prüfungsvorbereitung & Lernstrategie 

---

## 27.1 TOGAF Zertifizierungen im Überblick

```
TOGAF ZERTIFIZIERUNGSPORTFOLIO

LEVEL 1: TOGAF FOUNDATION
→ Grundkenntnisse TOGAF
→ Multiple Choice, 40 Fragen
→ 55 Minuten
→ Bestehensnote: 55%

LEVEL 2: TOGAF PRACTITIONER
→ Anwendung von TOGAF
→ Szenario-basiert, 8 Fragen (Komplex)
→ 90 Minuten
→ Bestehensnote: 60%

COMBINED: TOGAF FOUNDATION + PRACTITIONER
→ Beide Level in einer Prüfung
→ 80 Fragen gesamt
→ 150 Minuten

SPEZIALISIERUNG: TOGAF BUSINESS ARCHITECTURE FOUNDATION
→ Fokus auf Business Architecture
→ 40 Fragen
→ 60 Minuten
→ Bestehensnote: 55%
```

---

## 27.2 Lernstrategie für TOGAF Foundation

```
LERNPLAN — 4 WOCHEN

WOCHE 1: GRUNDLAGEN
├── Tag 1-2: Was ist TOGAF? Warum? Struktur
├── Tag 3-4: Die vier Domänen (BDAT)
├── Tag 5-6: ADM-Überblick (alle Phasen)
└── Tag 7: Wiederholung + Übungsfragen

WOCHE 2: ADM IM DETAIL
├── Tag 1-2: Preliminary + Phase A
├── Tag 3-4: Phase B + C
├── Tag 5-6: Phase D + E + F
└── Tag 7: Phase G + H + Requirements Management

WOCHE 3: KONZEPTE & BEGRIFFE
├── Tag 1-2: Deliverables, Artifacts, Building Blocks
├── Tag 3-4: Enterprise Continuum + Repository
├── Tag 5-6: Architecture Principles + Governance
└── Tag 7: Stakeholder Management + Views

WOCHE 4: PRÜFUNGSVORBEREITUNG
├── Tag 1-3: Übungsprüfungen (mehrfach)
├── Tag 4-5: Schwachstellen aufarbeiten
├── Tag 6: Letzte Wiederholung
└── Tag 7: Prüfung!
```

---

## 27.3 Die wichtigsten Prüfungsthemen

```
PRÜFUNGSRELEVANTE THEMEN (TOGAF FOUNDATION)

SEHR HÄUFIG:
├── ADM-Phasen und ihre Ziele
├── Inputs und Outputs der Phasen
├── Deliverables (welche Phase, welches Dokument?)
├── Architecture Principles (Struktur, Beispiele)
├── Stakeholder Management
└── Gap Analysis

HÄUFIG:
├── Enterprise Continuum
├── Architecture Repository (Komponenten)
├── Building Blocks (ABB vs. SBB)
├── Views und Viewpoints
└── Architecture Governance

GELEGENTLICH:
├── TOGAF mit anderen Frameworks
├── Architecture Styles
├── Risk Management
└── Communications Management
```

---

## 27.4 Merkhilfen und Eselsbrücken

```
MERKHILFEN FÜR TOGAF

ADM-PHASEN MERKEN:
"Profis Arbeiten Besonders Clever — Denn Echte Fachleute
 Gewinnen Häufig Richtig"
P = Preliminary
A = Architecture Vision
B = Business Architecture
C = Information Systems Architecture
D = Technology Architecture
E = Opportunities & Solutions
F = Migration Planning
G = Implementation Governance
H = Architecture Change Management
R = Requirements Management (zentral)

VIER DOMÄNEN (BDAT):
"Bunte Daten Auf Technologie"
B = Business
D = Data
A = Application
T = Technology

ARTIFACT-TYPEN:
"CDM" = Catalog, Diagram, Matrix

BUILDING BLOCK TYPEN:
"Architekten Schaffen Lösungen"
ABB = Architecture Building Block
SBB = Solution Building Block
```

---

## 27.5 Übungsfragen mit Antworten

```
ÜBUNGSFRAGE 1:
Was ist der Hauptzweck der Preliminary Phase?

A) Die Architecture Vision zu entwickeln
B) Das TOGAF Framework für das Unternehmen anzupassen
C) Die Gap Analysis durchzuführen
D) Den Migrationsplan zu erstellen

ANTWORT: B
Erklärung: Die Preliminary Phase bereitet das Unternehmen
auf die Nutzung von TOGAF vor. Das beinhaltet die Anpassung
des Frameworks, die Definition von Architecture Principles
und den Aufbau der Architecture Capability.

───────────────────────────────────────────────────────────

ÜBUNGSFRAGE 2:
In welcher Phase wird das Statement of Architecture Work erstellt?

A) Preliminary Phase
B) Phase A
C) Phase B
D) Phase E

ANTWORT: B
Erklärung: Das Statement of Architecture Work ist ein
Deliverable von Phase A (Architecture Vision). Es definiert
den Scope, die Ressourcen und den Zeitplan der Architekturarbeit.

───────────────────────────────────────────────────────────

ÜBUNGSFRAGE 3:
Was ist der Unterschied zwischen einem ABB und einem SBB?

A) ABBs sind für Business, SBBs für Technologie
B) ABBs sind konzeptuell, SBBs sind konkret/technologiespezifisch
C) ABBs werden in Phase A erstellt, SBBs in Phase E
D) Es gibt keinen Unterschied

ANTWORT: B
Erklärung: Architecture Building Blocks (ABBs) beschreiben
benötigte Fähigkeiten auf konzeptuellem Level (technologieneutral).
Solution Building Blocks (SBBs) sind konkrete, technologie-
spezifische Implementierungen, die ABBs realisieren.

───────────────────────────────────────────────────────────

ÜBUNGSFRAGE 4:
Welche Aussage über Architecture Principles ist KORREKT?

A) Sie werden nur in Phase B verwendet
B) Sie sind optional und können weggelassen werden
C) Sie gelten für alle Architekturentscheidungen im Unternehmen
D) Sie werden vom Architecture Board täglich neu definiert

ANTWORT: C
Erklärung: Architecture Principles sind allgemeine Regeln
und Leitlinien, die für alle Architekturentscheidungen gelten.
Sie werden in der Preliminary Phase definiert und sind die
Grundlage aller nachfolgenden Architekturarbeit.

───────────────────────────────────────────────────────────

ÜBUNGSFRAGE 5:
Was ist der Hauptunterschied zwischen einem Deliverable
und einem Artifact?

A) Deliverables sind größer als Artifacts
B) Deliverables sind formal vereinbart und genehmigt,
   Artifacts sind Bestandteile von Deliverables
C) Artifacts werden von Stakeholdern genehmigt,
   Deliverables nicht
D) Es gibt keinen Unterschied

ANTWORT: B
Erklärung: Ein Deliverable ist ein formal vereinbartes
Arbeitsergebnis, das von Stakeholdern genehmigt wird.
Ein Artifact ist ein Bestandteil eines Deliverables und
beschreibt einen Aspekt der Architektur (als Catalog,
Matrix oder Diagram).
```

---

## 27.6 Abschließende Lernempfehlungen

```
DEINE LERNSTRATEGIE — ZUSAMMENFASSUNG

1. VERSTEHEN VOR AUSWENDIGLERNEN
   → TOGAF macht Sinn, wenn du die Logik verstehst.
   → Warum gibt es Phase B vor Phase C?
   → Warum ist Requirements Management zentral?

2. MIT BEISPIELEN LERNEN
   → Jedes Konzept mit einem echten Beispiel verknüpfen.
   → Nutze die Fallbeispiele in dieser Anleitung.

3. REGELMÄSSIG ÜBEN
   → Täglich 30 Minuten ist besser als einmal 5 Stunden.
   → Übungsfragen nach jedem Kapitel.

4. VERBINDUNGEN HERSTELLEN
   → Wie hängen Phase A und Phase B zusammen?
   → Wie hängen ABBs und SBBs zusammen?
   → Wie hängen Principles und Governance zusammen?

5. PRAXISBEZUG HERSTELLEN
   → Wende TOGAF-Konzepte auf dein eigenes Unternehmen an.
   → Erstelle eine Capability Map deines Arbeitgebers.
   → Identifiziere Value Streams in deinem Alltag.

6. COMMUNITY NUTZEN
   → The Open Group Community
   → LinkedIn TOGAF-Gruppen
   → Study Groups mit anderen Lernenden

```

---

> **📚 Quellenhinweis:**
> Diese Anleitung basiert auf dem TOGAF® Standard, 10th Edition,
> veröffentlicht von The Open Group (2022), sowie dem
> TOGAF® Business Architecture Foundation Study Guide.
> TOGAF® ist ein eingetragenes Warenzeichen von The Open Group.


