# ADR-081 — iSAQB F1: Was ist Softwarearchitektur? Grundbegriffe & Aufgaben

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / alle Senior-Entwickler                    |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Foundation · Grundbegriffe                              |
| Betroffene Teams  | Alle Engineering-Teams, Tech Leads                            |
| iSAQB-Lernziel    | LZ-1-1 bis LZ-1-6 (Foundation Level)                         |

---

## 1. Warum dieser ADR existiert

Ohne gemeinsames Verständnis was Softwarearchitektur ist, entstehen drei konkrete Probleme:

```
Problem A: Architektur = Technologiewahl
  Symptom: "Wir haben uns für React entschieden — das ist unsere Architektur."
  Wahrheit: Technologiewahl ist ein ERGEBNIS architektonischer Entscheidungen, nicht die Architektur selbst.
  Konsequenz: Qualitätsziele werden nie explizit formuliert → niemand weiß woran Erfolg gemessen wird.

Problem B: Architektur = Diagramm
  Symptom: Einmal ein Diagramm zeichnen, fertig.
  Wahrheit: Architektur ist ein kontinuierlicher Entscheidungsprozess, kein Einmal-Artefakt.
  Konsequenz: Diagramm veraltet → misleading documentation → Neuentwickler treffen falsche Entscheidungen.

Problem C: Architektur = Aufgabe einer Person
  Symptom: "Der Architekt macht das schon."
  Wahrheit: Jeder Entwickler trifft täglich Architekturentscheidungen (welche Abstraktion, welche Grenze).
  Konsequenz: Inkonsistente Codebasis, weil 90% der Entscheidungen ohne Architektur-Bewusstsein fallen.
```

---

## 2. Was Softwarearchitektur IST: Die iSAQB-Definition

### 2.1 Offizielle Definition (iSAQB Glossar)

> **Softwarearchitektur** ist die grundlegende Organisation eines Systems, verkörpert in seinen Komponenten, deren Beziehungen zueinander und zur Umgebung sowie die Prinzipien, die den Entwurf und die Evolution des Systems leiten.

Diese Definition hat vier präzise Bestandteile die alle verstanden sein müssen:

```
① Komponenten
   → Die Bausteine: Module, Services, Klassen, Bibliotheken
   → Jede Komponente hat eine klar definierte Verantwortung
   → Beispiel: "OrderService ist verantwortlich für den Bestelllebenszyklus"

② Beziehungen zwischen Komponenten
   → Wer ruft wen auf? Wer kennt wen? Wer hängt von wem ab?
   → Richtung der Abhängigkeiten ist architektonisch entscheidend
   → Beispiel: Controller → Service → Repository (nie andersherum)

③ Beziehung zur Umgebung
   → Externe Systeme, Datenbanken, APIs, Benutzer
   → Systemgrenzen: Was gehört zum System, was nicht?
   → Beispiel: "Stripe ist extern, unsere Zahlungslogik intern"

④ Prinzipien die Entwurf und Evolution leiten
   → Das ist das Wichtigste: Prinzipien überdauern Einzelentscheidungen
   → Beispiel: "Domänenklassen kennen kein Framework" (→ ADR-031)
   → Ohne Prinzipien ist jede Entscheidung ein Einzelfall ohne Lerneffekt
```

### 2.2 Was Architektur NICHT ist

```
Architektur ≠ Technologieliste
  Falsch: "Unsere Architektur: Java, Spring, PostgreSQL, Kafka"
  Richtig: Diese Technologien wurden gewählt um bestimmte Qualitätsziele zu erfüllen

Architektur ≠ Einmalige Entscheidung
  Falsch: "Wir haben die Architektur in Woche 1 festgelegt."
  Richtig: Architektur entwickelt sich kontinuierlich — Evolutionary Architecture (→ ADR-061)

Architektur ≠ Nur Makro-Ebene
  Falsch: "Architektur betrifft nur Services und Deployment."
  Richtig: Auch Klassen-Design, Paket-Struktur und API-Kontrakte sind Architektur

Architektur ≠ Implementierungsdetail
  Falsch: "Ob wir ArrayList oder LinkedList nutzen ist Architektur."
  Richtig: Entscheidungen die schwer rückgängig zu machen sind (hohe Änderungskosten)
```

---

## 3. Die vier Tätigkeiten des Software-Architekten (iSAQB)

### 3.1 Tätigkeitsmodell

```
                    ┌─────────────────────────────────────┐
                    │         SOFTWARE-ARCHITEKT           │
                    └──────────────┬──────────────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼                    ▼
    ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
    │ ① KLÄREN        │ │ ② ENTWERFEN      │ │ ③ BEWERTEN       │ │ ④ KOMMUNIZIEREN  │
    │                  │ │                  │ │                  │ │                  │
    │ Anforderungen    │ │ Strukturen       │ │ Eignung der      │ │ Stakeholder      │
    │ verstehen        │ │ entwickeln       │ │ Architektur      │ │ informieren      │
    │                  │ │                  │ │ prüfen           │ │                  │
    │ Qualitätsziele   │ │ Muster anwenden  │ │                  │ │ Dokumentieren    │
    │ festlegen        │ │                  │ │ Risiken          │ │                  │
    │                  │ │ Technologien     │ │ identifizieren   │ │ Szenarios        │
    │ Randbedingungen  │ │ auswählen        │ │                  │ │ beschreiben      │
    │ erheben          │ │                  │ │                  │ │                  │
    └──────────────────┘ └──────────────────┘ └──────────────────┘ └──────────────────┘
```

### 3.2 Tätigkeit ① KLÄREN: Anforderungen verstehen

Die häufigste Fehlerquelle: Falsche Anforderungen werden perfekt umgesetzt.

```java
// ❌ Schlecht: Architektur ohne geklärte Anforderungen
// Entwickler A baut: Synchrones REST-API
// Entwickler B baut: Asynchrones Event-System
// Entwickler C baut: Direkten DB-Zugriff
// → Inkonsistenz, weil niemand gefragt hat: "Wie kritisch ist Konsistenz?"
//    "Wie viele gleichzeitige Nutzer?" "Wann ist 'langsam' ein Problem?"

// ✅ Gut: Qualitätsziele explizit klären (→ ADR-082)
// Bevor irgendein Code entsteht, werden Szenarien formuliert:
// "Ein Nutzer gibt eine Bestellung auf. Das System muss innerhalb 500ms
//  antworten (P95). Bei 1000 gleichzeitigen Nutzern."
// DIESER Satz bestimmt die Architektur — nicht die Technologie-Präferenz.
```

### 3.3 Tätigkeit ② ENTWERFEN: Strukturen entwickeln

Architekturelles Entwerfen ist nicht Implementieren. Es ist Abwägen.

```
Architektonische Entscheidungen haben drei Eigenschaften:
  ① Schwer rückgängig zu machen (hohe Änderungskosten)
  ② Beeinflussen viele andere Entscheidungen
  ③ Haben messbare Auswirkungen auf Qualitätsziele

Beispiele architektonischer Entscheidungen:
  ✓ Synchron vs. asynchrone Kommunikation zwischen Services
  ✓ Shared Database vs. Database-per-Service
  ✓ Monolith vs. Microservices (→ ADR-077)
  ✓ SQL vs. NoSQL für primäre Datenhaltung

Beispiele KEINE architektonischen Entscheidungen:
  ✗ Welche Bibliothek für JSON-Serialisierung
  ✗ Ob man Optional.orElseThrow() oder ifPresent() nutzt
  ✗ Wie eine einzelne Methode intern implementiert wird
```

### 3.4 Tätigkeit ③ BEWERTEN: Eignung prüfen

Architekturen müssen bewertet werden — BEVOR sie in Produktion sind.

```
Bewertungsmethoden (→ ADR-088 für Details):
  ATAM  = Architecture Tradeoff Analysis Method
         → Szenarien durchspielen, Sensitivitätspunkte finden
  CBAM  = Cost-Benefit Analysis Method
         → Kosten und Nutzen architektonischer Entscheidungen quantifizieren
  Fitness Functions (→ ADR-061)
         → Automatisierte, kontinuierliche Architektur-Bewertung

Wann bewerten?
  ① Bei großen Architekturentscheidungen (vor Umsetzung)
  ② Periodisch (jedes Quartal: "Stimmt die Architektur noch?")
  ③ Nach Incidents ("Was in der Architektur hat versagt?")
```

### 3.5 Tätigkeit ④ KOMMUNIZIEREN: Stakeholder informieren

Die am meisten unterschätzte Architektentätigkeit.

```
Stakeholder haben unterschiedliche Interessen und Sichtweisen:

Management:
  → Will: Kosten, Risiken, Time-to-Market
  → Braucht: "Die Architektur reduziert das Deployment-Risiko um 70%"
  → Nicht: "Wir nutzen Hexagonal Architecture mit DDD"

Entwickler:
  → Will: Klare Entscheidungen, Begründungen
  → Braucht: ADRs (dieses Kompendium!), Architekturdiagramme, Beispielcode
  → Nicht: Abstrakte Prinzipien ohne Bezug zum Code

Betrieb/DevOps:
  → Will: Deployability, Überwachbarkeit, Recoverability
  → Braucht: "Service X hat diese Health-Endpoints, diese Metriken"
  → Nicht: Domänenmodell-Diagramme

Für jede Zielgruppe braucht es eine andere Sicht auf dieselbe Architektur.
```

---

## 4. Architektonische Strukturen: Baustein-, Laufzeit- und Verteilungssicht

### 4.1 Die drei fundamentalen Architektursichten (iSAQB + arc42)

```
BAUSSTEINSICHT (statisch):
  → "Was gibt es?"
  → Zeigt: Komponenten, Module, Klassen, Pakete
  → Beschreibt: Zuständigkeiten, Schnittstellen
  → Werkzeug: Komponentendiagramm, Paket-Diagramm
  → Frage: "Welche Module existieren und wofür sind sie zuständig?"

LAUFZEITSICHT (dynamisch):
  → "Was passiert zur Laufzeit?"
  → Zeigt: Objektinstanzen, Nachrichtenfluss, Prozesse
  → Beschreibt: Ablaufszenarien, Kommunikation
  → Werkzeug: Sequenzdiagramm, Aktivitätsdiagramm
  → Frage: "Wie läuft eine Bestellung durch das System?"

VERTEILUNGSSICHT (Deployment):
  → "Wo läuft es?"
  → Zeigt: Server, Container, Netzwerke, Prozesse
  → Beschreibt: Hardware, Infrastruktur, Deployment-Einheiten
  → Werkzeug: Deployment-Diagramm, C4 Level 4
  → Frage: "Auf welchem Server läuft was, wie kommunizieren sie?"
```

### 4.2 Warum alle drei Sichten benötigt werden

```java
// Beispiel: Eine Entscheidung — drei verschiedene Sichten zeigen verschiedene Aspekte

// BAUSSTEINSICHT: OrderService hat diese Abhängigkeiten
public class OrderDomainService {        // Komponente
    private final OrderRepository repo;  // Abhängigkeit (→ Port)
    private final PaymentGateway payment;// Abhängigkeit (→ Port)
}
// Sicht zeigt: "OrderDomainService hängt von Ports ab, nicht Adaptern" (→ ADR-031)

// LAUFZEITSICHT: Was passiert wenn eine Bestellung aufgegeben wird?
// Client → OrderController → OrderDomainService → OrderRepository → DB
//                                               → PaymentGateway → Stripe API
//                                               → EventPublisher → Kafka
// Sicht zeigt: "Stripe-Aufruf ist synchron — Latenz-Bottleneck!"

// VERTEILUNGSSICHT: Wo läuft was?
// Kubernetes-Cluster:
//   Pod: order-service (3 Replikas) → PostgreSQL (managed) → Kafka (managed)
//                                  ↘ Stripe API (extern, kein SLA)
// Sicht zeigt: "Stripe ist Single-Point-of-Failure, braucht Circuit Breaker" (→ ADR-022)
```

---

## 5. Explizite vs. Implizite Architektur

### 5.1 Das Problem impliziter Architektur

```
Implizite Architektur:
  → Entsteht ohne bewusste Entscheidungen
  → Jeder Entwickler löst Probleme "pragmatisch"
  → Nach 2 Jahren: "Big Ball of Mud" (→ ADR-027 Code Smells)

Symptome impliziter Architektur:
  ✗ "Warum ist das so gemacht?" → "Keine Ahnung, war schon immer so"
  ✗ Jeder Service hat eine andere Struktur
  ✗ Niemand kennt alle Abhängigkeiten
  ✗ Änderungen brechen unerwartet andere Stellen
  ✗ Onboarding dauert 3 Monate weil kein Plan existiert
```

### 5.2 Explizite Architektur als Antwort

```
Explizite Architektur bedeutet:
  ① Entscheidungen sind dokumentiert (ADRs — dieses Kompendium)
  ② Prinzipien sind bekannt und werden durchgesetzt (ArchUnit → ADR-061)
  ③ Grenzen sind klar und maschinell prüfbar (Spring Modulith → ADR-079)
  ④ Neue Teammitglieder finden alles in docs/adr/

Validierung: Kann ein neuer Entwickler nach einem Tag Onboarding folgende
Fragen beantworten?
  □ "Welche Schichten gibt es?"
  □ "Was darf von was abhängen?"
  □ "Wo liegt Business-Logik?"
  □ "Warum nutzen wir keine direkte DB-Verbindung aus dem Controller?"

Wenn NEIN: Die Architektur ist implizit.
Wenn JA: Die Architektur ist explizit.
```

---

## 6. Architektur und Agilität: Kein Widerspruch

### 6.1 Das falsche Dilemma

```
Falsche Annahme: "Agil = kein Design upfront"
Richtig:         "Agil = gerade genug Design, kontinuierlich angepasst"

Das Agile Manifest sagt:
  "Responding to change OVER following a plan"
  → OVER, nicht INSTEAD OF
  → Pläne und Architektur existieren, werden aber flexibel angepasst
```

### 6.2 "Emergent Architecture" vs. "Intentional Architecture"

```
Emergent Architecture (rein): Architektur entsteht aus Code
  → Ergebnis: oft inkonsistent, zufällig, schwer wartbar
  → Richtiger Einsatz: für sehr kleine, unwichtige Teile

Intentional Architecture (rein): Architektur wird vollständig vorab geplant
  → Ergebnis: "Ivory Tower Architecture" die an der Realität vorbeigeht
  → Richtiger Einsatz: für unveränderliche Kernprinzipien

Hybridansatz (richtig): Kernprinzipien intentional, Details emergent
  → Intentional: Hexagonal Architecture, DDD-Grenzen, Security-Modell
  → Emergent: Konkrete Klassen-Struktur, API-Details, Datenbankschema-Feinheiten

Referenz: Neal Ford, Rebecca Parsons, "Building Evolutionary Architectures" (2017)
          → Fitness Functions als Mechanismus für kontinuierliche Architektur-Prüfung
```

---

## 7. Validierung: Wie wissen wir ob wir Architektur richtig verstehen?

### 7.1 Selbsttest für Entwickler und Architekten

```
FRAGE 1: Ist das eine Architekturentscheidung?
"Wir nutzen Spring Security statt selbst gebauter Auth."

ANTWORT: Ja — schwer rückgängig zu machen, beeinflusst viele Aspekte.
SCHLUSSFOLGERUNG: ADR schreiben (→ ADR-040 OAuth2).

FRAGE 2: Ist das eine Architekturentscheidung?
"Wir nutzen String.format() statt String.concat()."

ANTWORT: Nein — lokale, leicht änderbare Implementierungsentscheidung.
SCHLUSSFOLGERUNG: Code-Review-Kommentar, kein ADR.

FRAGE 3: Woran erkennt man ob eine Architektur gut ist?
ANTWORT (iSAQB): An der Erfüllung der Qualitätsziele (→ ADR-082)
→ Nicht: "Sieht gut aus im Diagramm"
→ Ja:    "P95-Latenz < 500ms bei 100 RPS, bestätigt durch Lasttest (→ ADR-044)"
```

### 7.2 Die "Fitness" einer Architektur

```
Eine Architektur ist gut wenn:
  ① Sie die Qualitätsziele erfüllt (messbar!)
  ② Sie Änderungen ermöglicht ohne unverhältnismäßige Kosten
  ③ Sie von den Stakeholdern verstanden wird
  ④ Sie maschinell prüfbar ist (Fitness Functions → ADR-061)
  ⑤ Sie die Entwickler produktiv macht (keine Architektur die sich gegen den Fluss arbeitet)

Metriken für Architektur-Güte:
  → Deployment-Frequenz (DORA) — gute Architektur ermöglicht häufige Releases
  → Mean Time to Recovery (MTTR) — gute Architektur macht Fehler schnell behebbar
  → Change Failure Rate — schlechte Architektur führt zu mehr Regressions
  → Onboarding-Zeit — explizite Architektur reduziert Einarbeitungszeit
```

---

## 8. Quellen & Referenzen

- **iSAQB CPSA-F Curriculum (2023), Lernziele LZ-1-1 bis LZ-1-6** — offizielle Grundlage aller Begriffe in diesem ADR.
- **IEEE 1471:2000 / ISO/IEC/IEEE 42010:2011** — internationaler Standard für Architekturbeschreibung; definiert "stakeholder", "concern", "viewpoint", "view".
- **Martin Fowler, "Patterns of Enterprise Application Architecture" (2002), Introduction** — "Architecture is the set of decisions that are hard to change."
- **Grady Booch et al., "Object-Oriented Analysis and Design" (1994)** — erste formale Definition von Softwarearchitektur als Sammlung von Entwurfsentscheidungen.
- **Neal Ford, Rebecca Parsons, "Building Evolutionary Architectures" (2017)** — Fitness Functions als Mechanismus für kontinuierliche Architekturprüfung.
- **arc42 Template (Peter Hruschka, Gernot Starke)** — deutschsprachiger Standard für Architekturdokumentation; direkt verwendbar.

---

## 9. Akzeptanzkriterien

- [ ] Alle Senior-Entwickler können den Unterschied zwischen Architektur und Implementierungsdetail erklären
- [ ] Es existiert eine messbare Definition von "Qualitätsziel" für dieses Projekt (→ ADR-082)
- [ ] ADR-Prozess ist etabliert und wird aktiv genutzt (→ ADR-075)
- [ ] Neue Entwickler können nach 1 Tag Onboarding die grundlegenden Architekturentscheidungen benennen
- [ ] `docs/architecture/README.md` erklärt die drei Architektursichten (Baustein, Laufzeit, Verteilung)

---

## Verwandte ADRs

- [ADR-075](ADR-075-architektur-entscheidungsprozess.md) — Der Entscheidungsprozess
- [ADR-082](ADR-082-isaqb-qualitaetsziele.md) — Qualitätsziele (iSAQB F2) — direkte Fortsetzung
- [ADR-047](ADR-047-c4-architekturdokumentation.md) — C4 als Dokumentationsstandard
- [ADR-061](ADR-061-fitness-functions.md) — Architektur-Fitness-Functions
- [ADR-031](ADR-031-hexagonal-architecture.md) — Ein konkretes Architekturmuster (iSAQB F3)
