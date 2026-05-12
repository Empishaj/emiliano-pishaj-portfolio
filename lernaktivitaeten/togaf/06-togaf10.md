# TOGAF Business Scenarios

---

## Inhaltsverzeichnis

1. [Was ist ein Business Scenario überhaupt?](#1-was-ist-ein-business-scenario-überhaupt)
2. [Warum brauchen wir Business Scenarios?](#2-warum-brauchen-wir-business-scenarios)
3. [Die 5 Kernelemente eines Business Scenarios](#3-die-5-kernelemente-eines-business-scenarios)
4. [Wo im ADM werden Business Scenarios eingesetzt?](#4-wo-im-adm-werden-business-scenarios-eingesetzt)
5. [Schritt-für-Schritt: Ein Business Scenario entwickeln](#5-schritt-für-schritt-ein-business-scenario-entwickeln)
   - [Schritt 1 – Das Problem identifizieren](#-schritt-1--das-problem-identifizieren)
   - [Schritt 2 – Die Umgebung beschreiben](#-schritt-2--die-umgebung-beschreiben)
   - [Schritt 3 – Akteure identifizieren](#-schritt-3--akteure-identifizieren)
   - [Schritt 4 – Gewünschte Ergebnisse definieren](#-schritt-4--gewünschte-ergebnisse-definieren)
   - [Schritt 5 – Anforderungen ableiten](#-schritt-5--anforderungen-ableiten)
   - [Schritt 6 – Das Scenario validieren und verfeinern](#-schritt-6--das-scenario-validieren-und-verfeinern)
6. [Das vollständige Business Scenario Template](#6-das-vollständige-business-scenario-template)
7. [Vollständiges Beispiel: Von Anfang bis Ende](#7-vollständiges-beispiel-von-anfang-bis-ende)
8. [Business Scenarios vs. Use Cases – Was ist der Unterschied?](#8-business-scenarios-vs-use-cases--was-ist-der-unterschied)
9. [Häufige Fehler – und wie du sie vermeidest](#9-häufige-fehler--und-wie-du-sie-vermeidest)
10. [Checkliste: Ist dein Business Scenario vollständig?](#10-checkliste-ist-dein-business-scenario-vollständig)
11. [Zusammenfassung in 5 Sätzen](#11-zusammenfassung-in-5-sätzen)

---

## 1. Was ist ein Business Scenario überhaupt?

Stell dir vor, du bist Drehbuchautor.

Du schreibst keine technischen Spezifikationen. Du schreibst eine
**Geschichte** – eine Geschichte darüber, wie das Unternehmen in
Zukunft arbeiten soll, wer dabei mitmacht, was schief läuft, wenn
nichts geändert wird, und wie die Welt aussieht, wenn alles klappt.

Genau das ist ein **Business Scenario**.

Ein Business Scenario ist eine Technik aus dem TOGAF-Framework, die
verwendet wird, um:

- **Geschäftsprobleme** zu beschreiben und zu verstehen
- **Stakeholder-Anforderungen** zu identifizieren und zu strukturieren
- **Architekturanforderungen** aus echten Geschäftssituationen abzuleiten
- **Konsens** zwischen verschiedenen Interessengruppen herzustellen

> **Merksatz:**
> Ein Business Scenario ist keine technische Spezifikation.
> Es ist eine **strukturierte Geschichte** über ein Geschäftsproblem
> und die gewünschte Lösung – aus der Perspektive der Menschen,
> die davon betroffen sind.

In der TOGAF-Welt wird ein Business Scenario als eine Methode
beschrieben, die hilft, die **Architekturanforderungen** zu
identifizieren und zu priorisieren, die notwendig sind, um ein
bestimmtes Geschäftsproblem zu lösen oder eine Geschäftschance
zu nutzen.

Es ist kein Wunschzettel. Es ist kein Lastenheft. Es ist eine
**strukturierte Beschreibung der Realität** – mit klaren Aussagen
darüber, was heute nicht funktioniert und was morgen besser
sein soll.

---

## 2. Warum brauchen wir Business Scenarios?

Ohne Business Scenarios passiert folgendes in der Praxis:

**Szenario ohne Business Scenario:**

```
CIO: "Wir brauchen eine neue Plattform."
Architekt: "Welche Anforderungen hat sie?"
CIO: "Muss schneller sein. Und moderner."
Architekt: "Wie schnell? Was bedeutet modern?"
CIO: "Ihr seid doch die Experten!"
Architekt: *erstellt eine Architektur, die niemand wollte*
```

Das Ergebnis: Millionen Euro investiert, niemand ist zufrieden,
die Architektur löst nicht das eigentliche Problem.

**Szenario mit Business Scenario:**

```
Alle Beteiligten sitzen zusammen.
Das Problem wird klar beschrieben.
Die betroffenen Personen werden identifiziert.
Das gewünschte Ergebnis wird konkret definiert.
Anforderungen entstehen aus echten Bedürfnissen.
Die Architektur löst das richtige Problem.
```

Business Scenarios haben konkrete Vorteile:

| Vorteil | Was das bedeutet |
|---------|-----------------|
| **Gemeinsames Verständnis** | Alle Beteiligten sprechen über dasselbe Problem |
| **Klare Anforderungen** | Anforderungen entstehen aus echten Geschäftssituationen |
| **Stakeholder-Einbindung** | Betroffene werden aktiv einbezogen |
| **Priorisierung** | Wichtige Probleme werden von unwichtigen getrennt |
| **Konsens** | Alle stimmen dem Problem und der gewünschten Lösung zu |
| **Messbarkeit** | Erfolg kann an konkreten Ergebnissen gemessen werden |
| **Kommunikation** | Komplexe Sachverhalte werden verständlich erklärt |

Laut TOGAF Standard ist die Rolle des Architekten unter anderem,
die Anforderungen der Stakeholder zu identifizieren und zu verfeinern,
Architektur-Views zu entwickeln, die zeigen, wie Anforderungen
adressiert werden, und Trade-offs zwischen konkurrierenden Anforderungen
zu zeigen. Business Scenarios sind das **wichtigste Werkzeug**
für genau diese Aufgabe.

---

## 3. Die 5 Kernelemente eines Business Scenarios

Jedes Business Scenario besteht aus fünf unverzichtbaren Elementen.
Fehlt eines davon, ist das Scenario unvollständig.

### Element 1: 🔴 Das Problem (The Problem)

**Was ist das Geschäftsproblem oder die Geschäftschance?**

Das Problem ist der Ausgangspunkt. Es beschreibt, was heute
nicht funktioniert, was eine Chance darstellt oder was sich
verändern muss.

Ein gutes Problem ist:
- **Konkret** – nicht „wir haben IT-Probleme", sondern „unsere
  Auftragsbearbeitung dauert 5 Tage, obwohl Kunden 24h erwarten"
- **Geschäftlich** – nicht technisch formuliert
- **Messbar** – mit klaren Auswirkungen auf das Geschäft
- **Relevant** – verknüpft mit Unternehmenszielen

---

### Element 2: 🟡 Die Umgebung (The Environment)

**In welchem Kontext findet das Problem statt?**

Die Umgebung beschreibt den Rahmen, in dem das Problem existiert:
- Welche Geschäftsprozesse sind betroffen?
- Welche Systeme sind involviert?
- Welche externen Faktoren spielen eine Rolle?
- Welche regulatorischen Anforderungen gibt es?
- Wie sieht die aktuelle Organisationsstruktur aus?

---

### Element 3: 🟢 Die Akteure (The Actors)

**Wer ist betroffen? Wer handelt? Wer entscheidet?**

Akteure sind alle Personen, Organisationen oder Systeme, die
mit dem Problem interagieren. TOGAF definiert einen Akteur als
eine Person, Organisation oder ein System, das eine oder mehrere
Rollen hat und Aktivitäten initiiert oder mit ihnen interagiert.

Es gibt zwei Arten von Akteuren:
- **Menschliche Akteure:** Personen und Rollen (z.B. Kundenberater,
  Vertriebsleiter, IT-Administrator)
- **Systemische Akteure:** IT-Systeme und Anwendungen, die eine
  Rolle spielen (z.B. CRM-System, ERP-System, Webportal)

---

### Element 4: 🔵 Das gewünschte Ergebnis (The Desired Outcome)

**Wie sieht Erfolg aus?**

Das gewünschte Ergebnis beschreibt den Zustand, der erreicht
werden soll, wenn das Problem gelöst ist. Es ist die Antwort
auf die Frage: „Woran erkennen wir, dass wir erfolgreich waren?"

Ein gutes Ergebnis ist:
- **Messbar** – mit konkreten Kennzahlen
- **Realistisch** – erreichbar im gegebenen Rahmen
- **Zeitgebunden** – mit einem klaren Zeitrahmen
- **Geschäftlich relevant** – verknüpft mit Unternehmenszielen

---

### Element 5: 🟠 Die Anforderungen (The Requirements)

**Was muss die Architektur leisten, um das Ergebnis zu erreichen?**

Die Anforderungen sind das direkte Ergebnis des Business Scenarios.
Sie beschreiben, was die Architektur – die Geschäftsprozesse,
Systeme, Daten und Technologien – leisten muss, damit das
gewünschte Ergebnis erreicht wird.

Anforderungen aus Business Scenarios sind:
- **Geschäftlich begründet** – nicht technisch motiviert
- **Priorisiert** – nach Wichtigkeit geordnet
- **Nachvollziehbar** – direkt aus dem Problem und Ergebnis ableitbar

---

## 4. Wo im ADM werden Business Scenarios eingesetzt?

Business Scenarios sind kein Werkzeug für eine einzige Phase.
Sie werden im gesamten ADM eingesetzt – aber am intensivsten
in den frühen Phasen:

### Phase A: Architecture Vision (Haupteinsatzgebiet)

In Phase A ist das Business Scenario das **wichtigste Werkzeug**.
Hier wird die Architecture Vision entwickelt – und dafür muss
der Architekt verstehen:

```
→ Was ist das Geschäftsproblem, das gelöst werden soll?
→ Wer sind die Stakeholder und was sind ihre Anforderungen?
→ Was ist das gewünschte Ergebnis der Architekturarbeit?
→ Welche Anforderungen muss die Architektur erfüllen?
```

Das Business Scenario liefert genau diese Antworten. Es ist
die Grundlage für die Architecture Vision und den Statement
of Architecture Work.

Laut TOGAF Standard beschreibt Phase A die Erstellung der
Architecture Vision, die Identifikation der Stakeholder und
die Einholung der Genehmigung für die Architekturarbeit.
Das Business Scenario ist der Mechanismus, mit dem all das
strukturiert und dokumentiert wird.

---

### Phase B: Business Architecture

In Phase B werden Business Scenarios genutzt, um:
```
→ Geschäftsprozesse zu verstehen und zu modellieren
→ Business Capabilities zu identifizieren
→ Organisatorische Anforderungen zu erfassen
→ Informations- und Serviceaustausche zu definieren
```

Laut TOGAF Standard werden in Phase B die Informations- und
Serviceaustausche in Geschäftsbegriffen weiter definiert –
Business Scenarios helfen dabei, diese Austausche aus der
Perspektive der betroffenen Akteure zu verstehen.

---

### 💻 Phase C & D: Information Systems & Technology

In diesen Phasen können Business Scenarios genutzt werden, um:
```
→ Technische Anforderungen aus Geschäftssituationen abzuleiten
→ Systemanforderungen zu validieren
→ Sicherzustellen, dass technische Lösungen das Geschäftsproblem lösen
```

---

### Requirements Management (Kontinuierlich)

Business Scenarios sind auch ein wichtiges Werkzeug für das
kontinuierliche Requirements Management im ADM. Wenn sich
Anforderungen ändern, kann das Business Scenario aktualisiert
werden, um die neuen Anforderungen zu reflektieren.

---

## 5. Schritt-für-Schritt: Ein Business Scenario entwickeln

Jetzt wird es praktisch. Hier sind die **6 konkreten Schritte**,
die du brauchst, um ein vollständiges Business Scenario zu entwickeln.

---

### 🔴 Schritt 1 – Das Problem identifizieren

**Was du tust:**
Du identifizierst und beschreibst das Geschäftsproblem oder
die Geschäftschance, die das Business Scenario adressiert.

**Warum das wichtig ist:**
Ohne ein klar definiertes Problem gibt es keine Grundlage für
die Architekturarbeit. Viele Architekturprojekte scheitern, weil
das eigentliche Problem nie klar formuliert wurde.

**Wie du das Problem findest:**

```
Fragen, die du stellen solltest:

🔍 "Was funktioniert heute nicht?"
   → Welche Prozesse sind ineffizient, fehlerhaft oder zu langsam?

🔍 "Was kostet uns das?"
   → Welche finanziellen, operativen oder strategischen Auswirkungen hat das Problem?

🔍 "Was passiert, wenn wir nichts tun?"
   → Was sind die Konsequenzen des Status quo?

🔍 "Welche Geschäftsziele werden dadurch gefährdet?"
   → Wie hängt das Problem mit der Unternehmensstrategie zusammen?

🔍 "Wer leidet am meisten darunter?"
   → Welche Personen oder Abteilungen sind am stärksten betroffen?
```

**Tipps für eine gute Problembeschreibung:**

```
❌ Schlecht: "Unsere IT ist veraltet."
✅ Gut:      "Unsere Auftragsbearbeitung dauert durchschnittlich
              5 Werktage. Kunden erwarten eine Bestätigung innerhalb
              von 2 Stunden. Das führt zu einem Kundenverlust von
              ca. 15% pro Jahr und Umsatzeinbußen von 2,3 Mio. €."

❌ Schlecht: "Wir brauchen eine neue App."
✅ Gut:      "Unsere Außendienstmitarbeiter haben keinen mobilen
              Zugriff auf Kundendaten. Sie müssen nach jedem
              Kundengespräch ins Büro fahren, um Berichte zu
              erstellen. Das kostet pro Mitarbeiter 2 Stunden
              täglich – bei 50 Mitarbeitern sind das 100 verlorene
              Arbeitsstunden pro Tag."
```

**Output von Schritt 1:**
Eine klare, präzise und geschäftlich formulierte Problembeschreibung
in 3–5 Sätzen.

---

### 🟡 Schritt 2 – Die Umgebung beschreiben

**Was du tust:**
Du beschreibst den Kontext, in dem das Problem existiert.
Das ist der „Schauplatz" deines Business Scenarios.

**Was zur Umgebung gehört:**

```
📌 Geschäftlicher Kontext:
   → In welcher Branche operiert das Unternehmen?
   → Welche Marktbedingungen sind relevant?
   → Welche regulatorischen Anforderungen gibt es?
   → Welche strategischen Ziele verfolgt das Unternehmen?

📌 Organisatorischer Kontext:
   → Welche Abteilungen sind betroffen?
   → Wie ist die Entscheidungsstruktur?
   → Welche Partnerschaften oder Lieferketten sind relevant?

📌 Technischer Kontext:
   → Welche Systeme sind heute im Einsatz?
   → Welche Integrationen existieren?
   → Welche technischen Einschränkungen gibt es?

📌 Externer Kontext:
   → Welche Kunden, Partner oder Lieferanten sind betroffen?
   → Welche Markttrends sind relevant?
   → Welche Wettbewerbssituation besteht?
```

**Praktischer Tipp:**
Die Umgebung muss nicht erschöpfend sein. Beschreibe nur, was
für das Verständnis des Problems und der Lösung relevant ist.
Zu viel Detail macht das Scenario unübersichtlich.

**Output von Schritt 2:**
Eine strukturierte Beschreibung des Kontexts – maximal 1–2 Seiten.

---

### 🟢 Schritt 3 – Akteure identifizieren

**Was du tust:**
Du identifizierst alle Personen, Rollen, Organisationen und
Systeme, die in das Business Scenario involviert sind.

**Warum das wichtig ist:**
Anforderungen entstehen immer aus den Bedürfnissen von Menschen.
Wenn du nicht weißt, wer betroffen ist, kannst du keine
vollständigen Anforderungen ableiten.

**Die zwei Arten von Akteuren:**

**a) Menschliche Akteure (Human Actors):**

```
Format für jeden menschlichen Akteur:
┌─────────────────────────────────────────────────────────┐
│ AKTEUR: [Name der Rolle]                                │
│ BESCHREIBUNG: [Wer ist diese Person? Was macht sie?]    │
│ INTERESSE: [Warum ist diese Person betroffen?]          │
│ BEDÜRFNIS: [Was braucht diese Person?]                  │
│ SCHMERZ: [Was frustriert diese Person heute?]           │
└─────────────────────────────────────────────────────────┘
```

**Beispiel:**
```
┌─────────────────────────────────────────────────────────┐
│ AKTEUR: Kundenberater (Außendienst)                     │
│ BESCHREIBUNG: Besucht täglich 5–8 Kunden vor Ort.       │
│               Verwaltet ca. 120 Kundenbeziehungen.      │
│ INTERESSE: Direkt betroffen durch fehlenden             │
│            mobilen Datenzugriff.                        │
│ BEDÜRFNIS: Zugriff auf Kundendaten, Bestellhistorie     │
│            und Angebote vom Smartphone aus.             │
│ SCHMERZ: Muss nach jedem Besuch ins Büro fahren,        │
│          um Berichte zu erstellen. Verliert 2h täglich. │
└─────────────────────────────────────────────────────────┘
```

**b) Systemische Akteure (System Actors):**

```
Format für jeden systemischen Akteur:
┌─────────────────────────────────────────────────────────┐
│ SYSTEM: [Name des Systems]                              │
│ FUNKTION: [Was macht dieses System?]                    │
│ ROLLE IM SCENARIO: [Wie ist es in das Problem           │
│                     involviert?]                        │
│ EINSCHRÄNKUNG: [Welche Grenzen hat es heute?]           │
└─────────────────────────────────────────────────────────┘
```

**Beispiel:**
```
┌─────────────────────────────────────────────────────────┐
│ SYSTEM: CRM-System (Salesforce)                         │
│ FUNKTION: Verwaltung aller Kundendaten und              │
│           Verkaufsaktivitäten.                          │
│ ROLLE IM SCENARIO: Enthält alle Kundendaten, die        │
│                    der Außendienst benötigt.            │
│ EINSCHRÄNKUNG: Nur über Desktop-Browser zugänglich.     │
│                Keine mobile App vorhanden.              │
└─────────────────────────────────────────────────────────┘
```

**Stakeholder-Map erstellen:**

Erstelle eine einfache Stakeholder-Map, die zeigt, wie die
Akteure miteinander in Beziehung stehen:

```
                    ┌─────────────────┐
                    │   Vertriebsleiter│
                    │   (Entscheider)  │
                    └────────┬────────┘
                             │ leitet
              ┌──────────────┴──────────────┐
              │                             │
    ┌─────────▼──────────┐       ┌─────────▼──────────┐
    │   Kundenberater    │       │   Innendienst       │
    │   (Außendienst)    │◄─────►│   (Support)         │
    └─────────┬──────────┘       └─────────┬──────────┘
              │ nutzt                       │ nutzt
    ┌─────────▼──────────┐       ┌─────────▼──────────┐
    │   CRM-System       │       │   ERP-System        │
    │   (Salesforce)     │◄─────►│   (SAP)             │
    └────────────────────┘       └────────────────────┘
```

**Output von Schritt 3:**
Eine vollständige Liste aller Akteure mit ihren Rollen,
Interessen, Bedürfnissen und Schmerzen.

---

### 🔵 Schritt 4 – Gewünschte Ergebnisse definieren

**Was du tust:**
Du definierst, wie Erfolg aussieht. Was soll sich konkret
verbessern, wenn das Problem gelöst ist?

**Warum das wichtig ist:**
Ohne klare Erfolgskriterien kann niemand beurteilen, ob die
Architektur das Problem wirklich gelöst hat. Viele Projekte
gelten als „abgeschlossen", obwohl das eigentliche Problem
weiterhin besteht.

**Das SMART-Prinzip für gewünschte Ergebnisse:**

| Buchstabe | Bedeutung | Beispiel |
|-----------|-----------|---------|
| **S** – Spezifisch | Konkret und klar formuliert | "Auftragsbestätigung innerhalb von 2 Stunden" |
| **M** – Messbar | Mit Kennzahlen messbar | "Reduktion der Bearbeitungszeit von 5 Tagen auf 4 Stunden" |
| **A** – Achievable | Realistisch erreichbar | "Basierend auf Benchmarks der Branche" |
| **R** – Relevant | Verknüpft mit Unternehmenszielen | "Unterstützt das Ziel: Kundenzufriedenheit +20%" |
| **T** – Terminiert | Mit klarem Zeitrahmen | "Bis Ende Q3 2026" |

**Ergebnisse aus Akteurs-Perspektive formulieren:**

Für jeden Akteur solltest du definieren, was sich für ihn
konkret verbessert:

```
Akteur: Kundenberater (Außendienst)
Gewünschtes Ergebnis:
→ Kann Kundendaten in Echtzeit vom Smartphone abrufen
→ Erstellt Besuchsberichte direkt nach dem Kundengespräch
→ Spart 2 Stunden täglich (= 10h/Woche)
→ Kann täglich 2 zusätzliche Kundenbesuche durchführen

Akteur: Vertriebsleiter
Gewünschtes Ergebnis:
→ Echtzeit-Überblick über alle Außendienstaktivitäten
→ Aktuelle Verkaufsdaten ohne Verzögerung
→ Bessere Planung durch vollständige Datenlage

Akteur: Unternehmen (gesamt)
Gewünschtes Ergebnis:
→ Kundenzufriedenheit steigt um 20%
→ Umsatz pro Außendienstmitarbeiter steigt um 15%
→ Kundenverlustrate sinkt von 15% auf unter 5%
```

**Output von Schritt 4:**
Eine Liste konkreter, messbarer Ergebnisse – pro Akteur und
für das Unternehmen insgesamt.

---

### 🟠 Schritt 5 – Anforderungen ableiten

**Was du tust:**
Du leitest aus dem Problem, der Umgebung, den Akteuren und
den gewünschten Ergebnissen konkrete Architekturanforderungen ab.

**Warum das wichtig ist:**
Die Anforderungen sind das **direkte Ergebnis** des Business
Scenarios. Sie fließen in die Architekturarbeit ein und
bestimmen, was die Architektur leisten muss.

**Arten von Anforderungen:**

```
📋 Geschäftliche Anforderungen (Business Requirements):
   → Was muss das Unternehmen tun können?
   Beispiel: "Das Unternehmen muss Auftragsbestätigungen
              innerhalb von 2 Stunden versenden können."

📋 Funktionale Anforderungen (Functional Requirements):
   → Was muss das System tun können?
   Beispiel: "Das CRM-System muss über eine mobile App
              zugänglich sein."

📋 Nicht-funktionale Anforderungen (Non-Functional Requirements):
   → Wie muss das System es tun?
   Beispiel: "Die mobile App muss auch offline funktionieren
              (bei schlechter Netzverbindung im Außendienst)."

📋 Einschränkungen (Constraints):
   → Was darf nicht verändert werden?
   Beispiel: "Das bestehende SAP-ERP-System darf nicht
              ausgetauscht werden."
```

**Anforderungen priorisieren:**

Nutze die MoSCoW-Methode für die Priorisierung:

| Priorität | Bedeutung | Beispiel |
|-----------|-----------|---------|
| **M** – Must Have | Absolut notwendig, ohne das geht nichts | Mobile CRM-App |
| **S** – Should Have | Wichtig, aber kurzfristig verzichtbar | Offline-Funktionalität |
| **C** – Could Have | Wünschenswert, wenn Zeit und Budget es erlauben | KI-gestützte Empfehlungen |
| **W** – Won't Have | Bewusst ausgeschlossen (für jetzt) | Vollständige ERP-Integration in Phase 1 |

**Output von Schritt 5:**
Eine priorisierte Liste von Architekturanforderungen, die direkt
aus dem Business Scenario abgeleitet sind.

---

### 🟣 Schritt 6 – Das Scenario validieren und verfeinern

**Was du tust:**
Du überprüfst das Business Scenario mit allen relevanten
Stakeholdern und verfeinerst es basierend auf ihrem Feedback.

**Warum das wichtig ist:**
Ein Business Scenario, das nur vom Architekten erstellt wurde,
spiegelt oft nicht die Realität wider. Stakeholder haben
wichtige Perspektiven, die du alleine nicht kennst.

**Validierungsworkshop durchführen:**

```
Agenda für einen 2-stündigen Validierungsworkshop:

00:00 – 00:15  Begrüßung und Ziel des Workshops
00:15 – 00:30  Präsentation des Business Scenarios
00:30 – 01:00  Diskussion: Ist das Problem korrekt beschrieben?
               → Haben wir etwas vergessen?
               → Ist die Umgebung vollständig?
               → Sind alle Akteure identifiziert?
01:00 – 01:30  Diskussion: Sind die gewünschten Ergebnisse richtig?
               → Sind die Ergebnisse realistisch?
               → Sind die Anforderungen vollständig?
               → Gibt es widersprüchliche Anforderungen?
01:30 – 01:50  Priorisierung der Anforderungen (gemeinsam)
01:50 – 02:00  Zusammenfassung und nächste Schritte
```

**Iterativer Prozess:**

Ein Business Scenario wird selten beim ersten Versuch perfekt.
Plane mindestens 2–3 Iterationen ein:

```
Iteration 1: Erster Entwurf (Architekt alleine oder mit kleinem Team)
     ↓
Iteration 2: Validierung mit Fachexperten und Betroffenen
     ↓
Iteration 3: Validierung mit Entscheidungsträgern und Genehmigung
     ↓
Finales Business Scenario: Genehmigt und bereit für die Architekturarbeit
```

**Output von Schritt 6:**
Ein von allen relevanten Stakeholdern genehmigtes Business Scenario –
bereit als Input für Phase A (Architecture Vision) und Phase B
(Business Architecture).

---

## 6. Das vollständige Business Scenario Template

Hier ist ein vollständiges Template, das du direkt verwenden kannst:

```
╔══════════════════════════════════════════════════════════════╗
║           TOGAF BUSINESS SCENARIO TEMPLATE                  ║
╚══════════════════════════════════════════════════════════════╝

METADATEN
─────────────────────────────────────────────────────────────
Titel:              [Kurzer, prägnanter Name des Scenarios]
Version:            [z.B. 1.0, 1.1, 2.0]
Datum:              [Erstellungsdatum]
Erstellt von:       [Name des Architekten / Teams]
Status:             [Entwurf / In Review / Genehmigt]
Genehmigt von:      [Name des Genehmigenden]
Genehmigungsdatum:  [Datum der Genehmigung]

1. PROBLEMBESCHREIBUNG
─────────────────────────────────────────────────────────────
1.1 Das Geschäftsproblem:
    [Beschreibe das Problem in 3–5 klaren, geschäftlich
     formulierten Sätzen. Was funktioniert nicht?
     Was sind die Auswirkungen?]

1.2 Geschäftliche Auswirkungen:
    → Finanzielle Auswirkung:  [z.B. Umsatzverlust X €/Jahr]
    → Operative Auswirkung:    [z.B. X Stunden Mehraufwand/Tag]
    → Strategische Auswirkung: [z.B. Wettbewerbsnachteil]
    → Risiko bei Nichthandeln: [Was passiert, wenn nichts getan wird?]

1.3 Verknüpfung mit Unternehmenszielen:
    → Unternehmensziel 1: [z.B. Kundenzufriedenheit steigern]
    → Unternehmensziel 2: [z.B. Digitale Transformation]

2. UMGEBUNG
─────────────────────────────────────────────────────────────
2.1 Geschäftlicher Kontext:
    [Branche, Marktbedingungen, strategische Situation]

2.2 Organisatorischer Kontext:
    [Betroffene Abteilungen, Entscheidungsstrukturen]

2.3 Technischer Kontext:
    [Aktuelle Systeme, Integrationen, Einschränkungen]

2.4 Externer Kontext:
    [Kunden, Partner, Regulierung, Markttrends]

3. AKTEURE
─────────────────────────────────────────────────────────────
3.1 Menschliche Akteure:

    Akteur 1:
    → Rolle:         [z.B. Kundenberater Außendienst]
    → Beschreibung:  [Wer ist diese Person?]
    → Interesse:     [Warum ist sie betroffen?]
    → Bedürfnis:     [Was braucht sie?]
    → Schmerz:       [Was frustriert sie heute?]

    Akteur 2:
    → Rolle:         [...]
    → Beschreibung:  [...]
    → Interesse:     [...]
    → Bedürfnis:     [...]
    → Schmerz:       [...]

    [Weitere Akteure nach Bedarf]

3.2 Systemische Akteure:

    System 1:
    → Name:              [z.B. CRM-System (Salesforce)]
    → Funktion:          [Was macht dieses System?]
    → Rolle im Scenario: [Wie ist es involviert?]
    → Einschränkung:     [Welche Grenzen hat es?]

    [Weitere Systeme nach Bedarf]

4. GEWÜNSCHTE ERGEBNISSE
─────────────────────────────────────────────────────────────
4.1 Ergebnisse für das Unternehmen:
    → [Messbares Ergebnis 1 mit Kennzahl und Zeitrahmen]
    → [Messbares Ergebnis 2 mit Kennzahl und Zeitrahmen]
    → [Messbares Ergebnis 3 mit Kennzahl und Zeitrahmen]

4.2 Ergebnisse pro Akteur:

    Akteur 1 ([Rolle]):
    → [Was verbessert sich konkret für diese Person?]
    → [Messbare Verbesserung]

    Akteur 2 ([Rolle]):
    → [Was verbessert sich konkret für diese Person?]
    → [Messbare Verbesserung]

5. ANFORDERUNGEN
─────────────────────────────────────────────────────────────
5.1 Geschäftliche Anforderungen:
    [M] BR-01: [Muss-Anforderung]
    [M] BR-02: [Muss-Anforderung]
    [S] BR-03: [Sollte-Anforderung]
    [C] BR-04: [Könnte-Anforderung]

5.2 Funktionale Anforderungen:
    [M] FR-01: [Muss-Anforderung]
    [M] FR-02: [Muss-Anforderung]
    [S] FR-03: [Sollte-Anforderung]

5.3 Nicht-funktionale Anforderungen:
    [M] NFR-01: [Muss-Anforderung, z.B. Performance, Sicherheit]
    [S] NFR-02: [Sollte-Anforderung]

5.4 Einschränkungen:
    CON-01: [Was darf nicht verändert werden?]
    CON-02: [Welche Rahmenbedingungen gelten?]

6. VALIDIERUNG
─────────────────────────────────────────────────────────────
Validiert durch:    [Namen der Stakeholder]
Validierungsdatum:  [Datum]
Offene Punkte:      [Was muss noch geklärt werden?]
Nächste Schritte:   [Was passiert als nächstes?]
```

---

## 7. Vollständiges Beispiel: Von Anfang bis Ende

Hier ist ein vollständiges, realistisches Business Scenario
für ein mittelständisches Unternehmen:

```
╔══════════════════════════════════════════════════════════════╗
║           TOGAF BUSINESS SCENARIO                           ║
║           Mobiler Außendienst – MusterGmbH                  ║
╚══════════════════════════════════════════════════════════════╝

METADATEN
─────────────────────────────────────────────────────────────
Titel:              Mobiler Datenzugriff für den Außendienst
Version:            1.2
Datum:              Mai 2026
Erstellt von:       EA-Team, MusterGmbH
Status:             Genehmigt
Genehmigt von:      CIO, Vertriebsleiter
Genehmigungsdatum:  15. Mai 2026

1. PROBLEMBESCHREIBUNG
─────────────────────────────────────────────────────────────
1.1 Das Geschäftsproblem:
    Die 50 Außendienstmitarbeiter der MusterGmbH haben keinen
    mobilen Zugriff auf das CRM-System. Nach jedem Kundengespräch
    müssen sie ins Büro fahren, um Besuchsberichte zu erstellen
    und Kundendaten zu aktualisieren. Dies führt zu einem
    Zeitverlust von durchschnittlich 2 Stunden pro Mitarbeiter
    und Tag sowie zu veralteten Kundendaten im System.

1.2 Geschäftliche Auswirkungen:
    → Finanzielle Auswirkung:  100 verlorene Arbeitsstunden/Tag
                                = ca. 1,8 Mio. € Produktivitätsverlust/Jahr
    → Operative Auswirkung:    Kundendaten sind 24–48h veraltet
    → Strategische Auswirkung: Wettbewerber mit mobilen Lösungen
                                gewinnen Marktanteile
    → Risiko bei Nichthandeln: Weitere 10% Marktanteilsverlust
                                in den nächsten 2 Jahren

1.3 Verknüpfung mit Unternehmenszielen:
    → Unternehmensziel 1: Kundenzufriedenheit um 20% steigern
    → Unternehmensziel 2: Digitale Transformation des Vertriebs
    → Unternehmensziel 3: Umsatz um 10% steigern

2. UMGEBUNG
─────────────────────────────────────────────────────────────
2.1 Geschäftlicher Kontext:
    MusterGmbH ist ein B2B-Großhändler mit 500 Mitarbeitern.
    Der Außendienst ist der wichtigste Vertriebskanal (70% des
    Umsatzes). Wettbewerber haben bereits mobile Lösungen
    eingeführt und gewinnen Kunden.

2.2 Organisatorischer Kontext:
    → Vertriebsabteilung: 50 Außendienstmitarbeiter, 10 Innendienst
    → Entscheidung liegt beim Vertriebsleiter und CIO
    → IT-Abteilung: 15 Mitarbeiter, verantwortlich für Umsetzung

2.3 Technischer Kontext:
    → CRM: Salesforce (nur Desktop-Browser, keine mobile App)
    → ERP: SAP S/4HANA (vollständig integriert mit CRM)
    → Geräte: Außendienst nutzt iPhones (privat und dienstlich)
    → Netzwerk: Außendienst oft in Gebieten mit schlechtem Empfang

2.4 Externer Kontext:
    → 800 B2B-Kunden in Deutschland und Österreich
    → Regulierung: DSGVO (Kundendaten auf mobilen Geräten)
    → Markttrend: Mobile-First im B2B-Vertrieb

3. AKTEURE
─────────────────────────────────────────────────────────────
3.1 Menschliche Akteure:

    Akteur 1:
    → Rolle:         Kundenberater (Außendienst)
    → Beschreibung:  Besucht täglich 5–8 Kunden vor Ort.
                     Verwaltet ca. 120 Kundenbeziehungen.
    → Interesse:     Direkt betroffen durch fehlenden Zugriff.
    → Bedürfnis:     Kundendaten, Bestellhistorie und Angebote
                     vom Smartphone aus abrufen und aktualisieren.
    → Schmerz:       2h täglich Bürofahrt für Berichtserstellung.
                     Oft veraltete Daten beim Kundengespräch.

    Akteur 2:
    → Rolle:         Vertriebsleiter
    → Beschreibung:  Verantwortlich für 50 Außendienstmitarbeiter
                     und die Vertriebsstrategie.
    → Interesse:     Benötigt aktuelle Verkaufsdaten für Planung.
    → Bedürfnis:     Echtzeit-Überblick über Außendienstaktivitäten.
    → Schmerz:       Entscheidungen basieren auf 24–48h alten Daten.

    Akteur 3:
    → Rolle:         IT-Administrator
    → Beschreibung:  Verantwortlich für CRM und mobile Geräte.
    → Interesse:     Muss Sicherheit und DSGVO-Konformität sichern.
    → Bedürfnis:     Sichere, verwaltbare mobile Lösung.
    → Schmerz:       Keine Mobile Device Management (MDM)-Lösung.

3.2 Systemische Akteure:

    System 1:
    → Name:              CRM-System (Salesforce)
    → Funktion:          Verwaltung aller Kundendaten und Aktivitäten.
    → Rolle im Scenario: Enthält alle Daten, die der Außendienst
                         benötigt, ist aber nicht mobil zugänglich.
    → Einschränkung:     Nur über Desktop-Browser zugänglich.

    System 2:
    → Name:              ERP-System (SAP S/4HANA)
    → Funktion:          Auftragsbearbeitung, Lagerbestand, Finanzen.
    → Rolle im Scenario: Liefert Bestellhistorie und Verfügbarkeiten.
    → Einschränkung:     Keine direkte mobile Schnittstelle.

4. GEWÜNSCHTE ERGEBNISSE
─────────────────────────────────────────────────────────────
4.1 Ergebnisse für das Unternehmen:
    → Produktivitätssteigerung: 100h/Tag → 20h/Tag Büroaufwand
      (Einsparung: 1,4 Mio. €/Jahr) – bis Q4 2026
    → Kundenzufriedenheit: +20% (gemessen per NPS) – bis Q2 2027
    → Umsatz pro Außendienstmitarbeiter: +15% – bis Q4 2026
    → Datenaktualität: Kundendaten max. 1h alt (statt 24–48h)

4.2 Ergebnisse pro Akteur:

    Kundenberater (Außendienst):
    → Spart 2h täglich (keine Bürofahrt mehr nötig)
    → Kann 2 zusätzliche Kundenbesuche pro Tag durchführen
    → Hat beim Kundengespräch immer aktuelle Daten
    → Erstellt Berichte direkt nach dem Gespräch (5 Min.)

    Vertriebsleiter:
    → Echtzeit-Dashboard mit allen Außendienstaktivitäten
    → Tagesaktuelle Verkaufszahlen für bessere Planung
    → Kann Ressourcen effizienter einsetzen

    IT-Administrator:
    → Zentrale Verwaltung aller mobilen Geräte via MDM
    → DSGVO-konforme Datenverschlüsselung auf Geräten
    → Remote-Wipe bei Geräteverlust möglich

5. ANFORDERUNGEN
─────────────────────────────────────────────────────────────
5.1 Geschäftliche Anforderungen:
    [M] BR-01: Außendienstmitarbeiter müssen Kundendaten
               mobil abrufen und aktualisieren können.
    [M] BR-02: Besuchsberichte müssen direkt nach dem
               Kundengespräch mobil erstellt werden können.
    [S] BR-03: Offline-Funktionalität bei schlechtem Empfang.
    [C] BR-04: KI-gestützte Empfehlungen für Cross-Selling.

5.2 Funktionale Anforderungen:
    [M] FR-01: Mobile App für Salesforce CRM (iOS).
    [M] FR-02: Synchronisation mit SAP ERP (Bestellhistorie).
    [M] FR-03: Digitale Besuchsberichte mit Unterschrift.
    [S] FR-04: Offline-Datenzugriff (letzte 30 Tage).
    [S] FR-05: Push-Benachrichtigungen für wichtige Updates.

5.3 Nicht-funktionale Anforderungen:
    [M] NFR-01: DSGVO-konforme Datenverschlüsselung.
    [M] NFR-02: Mobile Device Management (MDM) Integration.
    [M] NFR-03: Ladezeit unter 3 Sekunden (auch bei 4G).
    [S] NFR-04: Verfügbarkeit 99,5% (Werktage 6–22 Uhr).

5.4 Einschränkungen:
    CON-01: SAP S/4HANA darf nicht ausgetauscht werden.
    CON-02: Nur iOS-Geräte (keine Android-Unterstützung).
    CON-03: Budget: max. 250.000 € für Phase 1.
    CON-04: Go-Live bis spätestens 01.12.2026.

6. VALIDIERUNG
─────────────────────────────────────────────────────────────
Validiert durch:    Vertriebsleiter, CIO, 3 Außendienstmitarbeiter,
                    IT-Administrator
Validierungsdatum:  10. Mai 2026
Offene Punkte:      → DSGVO-Prüfung durch Datenschutzbeauftragten
                    → Budget-Freigabe durch CFO ausstehend
Nächste Schritte:   → Architecture Vision (Phase A) starten
                    → Statement of Architecture Work erstellen
                    → Projekt-Kickoff mit IT-Team planen
```

---

## 8. Business Scenarios vs. Use Cases – Was ist der Unterschied?

Diese Frage kommt immer wieder. Hier ist die klare Antwort:

| Merkmal | Business Scenario | Use Case |
|---------|------------------|---------|
| **Fokus** | Geschäftsproblem und gewünschtes Ergebnis | Systemfunktionalität |
| **Perspektive** | Geschäftlich / strategisch | Technisch / funktional |
| **Detailgrad** | Hoch-level, konzeptuell | Detailliert, spezifisch |
| **Akteure** | Menschen UND Systeme | Primär Benutzer und Systeme |
| **Ziel** | Anforderungen ableiten | Systemverhalten beschreiben |
| **Wann** | Frühe Phasen (A, B) | Spätere Phasen (C, D) |
| **Ergebnis** | Architekturanforderungen | Systemspezifikationen |

> 💡 **Merksatz:**
> Ein Business Scenario sagt: *„Was muss das Unternehmen erreichen?"*
> Ein Use Case sagt: *„Wie verhält sich das System, wenn ein Benutzer
> eine bestimmte Aktion ausführt?"*
>
> Business Scenarios kommen **vor** Use Cases. Sie liefern den
> Kontext, aus dem Use Cases entstehen.

---

## 9. Häufige Fehler – und wie du sie vermeidest

### ❌ Fehler 1: Das Problem technisch formulieren

**Was passiert:** „Wir brauchen eine neue API für das CRM-System."
**Folge:** Das eigentliche Geschäftsproblem bleibt unklar.
**Lösung:** Formuliere das Problem immer aus Geschäftsperspektive.
Frage: „Was ist die geschäftliche Auswirkung?"

---

### ❌ Fehler 2: Akteure vergessen

**Was passiert:** Das Scenario beschreibt nur Systeme, nicht Menschen.
**Folge:** Wichtige Anforderungen werden übersehen.
**Lösung:** Identifiziere systematisch alle betroffenen Personen
und Rollen. Frage: „Wer ist von diesem Problem betroffen?"

---

### ❌ Fehler 3: Gewünschte Ergebnisse nicht messbar

**Was passiert:** „Wir wollen bessere Kundenzufriedenheit."
**Folge:** Niemand kann beurteilen, ob das Projekt erfolgreich war.
**Lösung:** Nutze das SMART-Prinzip. Jedes Ergebnis braucht
eine Kennzahl und einen Zeitrahmen.

---

### ❌ Fehler 4: Kein Stakeholder-Involvement

**Was passiert:** Der Architekt erstellt das Scenario alleine.
**Folge:** Das Scenario spiegelt nicht die Realität wider.
Wichtige Perspektiven fehlen.
**Lösung:** Führe Workshops mit allen betroffenen Stakeholdern
durch. Das Business Scenario ist ein kollaboratives Dokument.

---

### ❌ Fehler 5: Zu viele Scenarios auf einmal

**Was passiert:** Das Team erstellt 20 Business Scenarios gleichzeitig.
**Folge:** Kein Scenario wird wirklich gut ausgearbeitet.
**Lösung:** Fokussiere dich auf 2–3 kritische Scenarios pro
ADM-Zyklus. Qualität vor Quantität.

---

### ❌ Fehler 6: Das Scenario nicht aktualisieren

**Was passiert:** Das Business Scenario wird einmal erstellt
und dann nie wieder angeschaut.
**Folge:** Das Scenario veraltet, Anforderungen ändern sich,
aber das Scenario bleibt statisch.
**Lösung:** Behandle das Business Scenario als lebendes Dokument.
Aktualisiere es, wenn sich Anforderungen oder der Kontext ändern.

---

## 10. Checkliste: Ist dein Business Scenario vollständig?

```
METADATEN
[ ] Titel ist klar und prägnant
[ ] Version und Datum sind angegeben
[ ] Status ist klar (Entwurf / In Review / Genehmigt)
[ ] Verantwortlicher ist benannt

PROBLEMBESCHREIBUNG
[ ] Das Problem ist klar und geschäftlich formuliert
[ ] Die finanziellen Auswirkungen sind quantifiziert
[ ] Die operativen Auswirkungen sind beschrieben
[ ] Das Risiko bei Nichthandeln ist benannt
[ ] Die Verknüpfung mit Unternehmenszielen ist hergestellt

UMGEBUNG
[ ] Der geschäftliche Kontext ist beschrieben
[ ] Der organisatorische Kontext ist beschrieben
[ ] Der technische Kontext ist beschrieben
[ ] Externe Faktoren sind berücksichtigt

AKTEURE
[ ] Alle menschlichen Akteure sind identifiziert
[ ] Für jeden Akteur: Rolle, Interesse, Bedürfnis, Schmerz
[ ] Alle systemischen Akteure sind identifiziert
[ ] Für jedes System: Funktion, Rolle, Einschränkung
[ ] Eine Stakeholder-Map ist erstellt

GEWÜNSCHTE ERGEBNISSE
[ ] Ergebnisse für das Unternehmen sind SMART formuliert
[ ] Ergebnisse für jeden Akteur sind definiert
[ ] Alle Ergebnisse sind messbar (mit Kennzahlen)
[ ] Alle Ergebnisse haben einen Zeitrahmen

ANFORDERUNGEN
[ ] Geschäftliche Anforderungen sind abgeleitet
[ ] Funktionale Anforderungen sind abgeleitet
[ ] Nicht-funktionale Anforderungen sind abgeleitet
[ ] Einschränkungen sind dokumentiert
[ ] Alle Anforderungen sind mit MoSCoW priorisiert
[ ] Anforderungen sind direkt aus dem Problem ableitbar

VALIDIERUNG
[ ] Das Scenario wurde mit Stakeholdern validiert
[ ] Feedback wurde eingearbeitet
[ ] Offene Punkte sind dokumentiert
[ ] Das Scenario ist genehmigt
[ ] Nächste Schritte sind definiert

QUALITÄT
[ ] Das Scenario ist in einfacher, verständlicher Sprache
[ ] Kein unnötiges Fachjargon
[ ] Das Scenario ist konsistent (keine Widersprüche)
[ ] Das Scenario ist vollständig (alle 5 Elemente vorhanden)
[ ] Das Scenario ist bereit als Input für Phase A 🚀
```

---

## 11. Zusammenfassung in 5 Sätzen

1. **Ein Business Scenario ist eine strukturierte Geschichte**
   über ein Geschäftsproblem – mit klaren Akteuren, einem
   definierten Kontext und messbaren gewünschten Ergebnissen.

2. **Es besteht aus 5 Kernelementen:** Problem, Umgebung,
   Akteure, gewünschte Ergebnisse und Anforderungen – fehlt
   eines davon, ist das Scenario unvollständig.

3. **Business Scenarios werden hauptsächlich in Phase A**
   (Architecture Vision) eingesetzt, um Stakeholder-Anforderungen
   zu identifizieren und die Grundlage für die Architekturarbeit
   zu schaffen.

4. **Der Entwicklungsprozess ist iterativ** – plane mindestens
   2–3 Runden mit Stakeholder-Feedback ein, bevor das Scenario
   genehmigt wird.

5. **Ohne Business Scenarios baut man Architekturen ins Blaue** –
   mit Business Scenarios baut man Architekturen, die echte
   Geschäftsprobleme lösen und echten Mehrwert schaffen.

---

## 📚 Weiterführende Quellen

| Quelle | Inhalt |
|--------|--------|
| **TOGAF Standard, 10th Edition – ADM Techniques** | Offizielle Beschreibung der Business Scenario Methode |
| **TOGAF Series Guide: Business Scenarios (G176)** | Detaillierter Leitfaden für Business Scenarios |
| **TOGAF Standard – Architecture Development Method** | Phase A und B im Detail |
| **TOGAF Business Architecture Foundation Study Guide** | Kapitel 12: Business Scenarios |
| **www.opengroup.org/library/g176** | Offizieller Business Scenarios Guide |
| **www.opengroup.org/togaf** | Offizielle TOGAF-Ressourcen |

---

> 📝 **Dieses Dokument basiert auf dem TOGAF® Standard, 10th Edition**
> und dem TOGAF® Business Architecture Foundation Study Guide
> von The Open Group. TOGAF® ist ein eingetragenes Warenzeichen
> von The Open Group.
>
> Erstellt als Hands-On-Einführung für Einsteiger.
> Für den internen Gebrauch bestimmt.

---

*Ende des Dokuments – Viel Erfolg mit deinen Business Scenarios! 🎯*