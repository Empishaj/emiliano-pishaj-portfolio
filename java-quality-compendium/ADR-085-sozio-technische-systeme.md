# ADR-085 — Sozio-technische Systeme: Wenn Organisationsstruktur zur Architektur wird

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / VP Engineering / Architektur-Board                      |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-07-01 (6 Monate — Team-Strukturen können sich ändern)    |
| Kategorie         | Architektur-Strategie · Organisationsdesign · Conway's Law    |
| Betroffene Teams  | Engineering-Leadership, alle Team-Leads, HR, Product          |
| Abhängigkeiten    | ADR-077 (Microservices vs. Monolith), ADR-079 (Modulith), ADR-023 (DDD) |

---

## 1. Die erschreckende Wahrheit: Deine Organisation IST deine Architektur

### 1.1 Das Phänomen das jedes wachsende Tech-Unternehmen kennt

```
Situation in Woche 1:
  3 Entwickler, ein Monolith, volle Kontrolle, schnelle Releases.

Situation in Monat 18:
  25 Entwickler, 5 Teams, derselbe Monolith.
  Jeder Deploy braucht Koordination mit allen Teams.
  Feature dauert 6 Wochen statt 2 — wegen Abstimmung, nicht Entwicklung.
  Architekt sagt: "Wir brauchen Microservices."
  
Drei Monate nach Microservice-Migration:
  Service A ruft Service B ruft Service C.
  Team Payments und Team Orders blockieren sich gegenseitig bei jedem Sprint.
  Deployment von Service A bricht Service B.
  Man hat Microservices — aber exakt dieselben Abstimmungsprobleme.

Was ist passiert?
  Die Architektur wurde geändert. Die Organisation nicht.
  Das nennt sich Conway's Law — und es wurde ignoriert.
```

### 1.2 Conway's Law: Die meistzitierte und meistignorierte Erkenntnis

Melvin Conway formulierte 1968 eine Beobachtung die heute durch Jahrzehnte empirischer Forschung bestätigt ist:

> "Any organization that designs a system (defined broadly) will produce a design whose structure is a copy of the organization's communication structure."
> — Melvin E. Conway, "How Do Committees Invent?", 1968

**Was das konkret bedeutet:**

```
THESE: Die Architektur DEINES Systems spiegelt die Kommunikationsstruktur
       DEINER Organisation — ob du willst oder nicht.

NICHT: "Die Architektur sollte die Org-Struktur spiegeln."
       Das ist eine Beschreibung, keine Empfehlung.

SONDERN: Wenn du Architektur ändern willst, musst du ZUERST die
          Kommunikationsstruktur ändern. Andernfalls wird die Architektur
          in die alte Form zurückgezogen — immer.
```

---

## 2. Konkrete Beispiele: Conway's Law in der Praxis

### Beispiel A: Das Backend-Frontend-Split-Desaster

```
ORGANISATION:
  Team 1: "Backend-Team"  (8 Personen, Java, alle Services)
  Team 2: "Frontend-Team" (6 Personen, React, alle UIs)

RESULTIERENDE ARCHITEKTUR (Conway's Law):
  Backend: ein riesiger Monolith-Service mit "allem"
  Frontend: eine React-SPA die "alles" konsumiert
  
  ┌──────────────────────────────────────────────────────┐
  │                  Frontend (React SPA)                │
  │              Team Frontend (6 Personen)              │
  └──────────────────────┬───────────────────────────────┘
                         │ Riesige, unstrukturierte API
  ┌──────────────────────▼───────────────────────────────┐
  │                 Backend Monolith                     │
  │  Orders + Payments + Users + Products + Inventory + │
  │  Notifications + Analytics + ... (Team Backend, 8P) │
  └──────────────────────────────────────────────────────┘

KONSEQUENZEN:
  ① API hat 147 Endpunkte, manche für Orders, manche für UI-Spezifika
  ② Jede Frontend-Änderung braucht ein Backend-Ticket (Blockade!)
  ③ API-Vertrag ist das Battle-Field zwischen zwei Teams
  ④ Backend-Team "kennt" Frontend-Anforderungen nie rechtzeitig
  ⑤ Jeder Sprint: "Die API gibt uns nicht was wir brauchen"
```

```
RICHTIGE ORGANISATION (Team-Topologies):
  Team 1: "Orders-Team" (Full-Stack, 4-6 Personen)
          → Besitzt: Order-Service (Backend) + Order-UI-Komponenten
  Team 2: "Payments-Team" (Full-Stack, 4-6 Personen)
          → Besitzt: Payment-Service (Backend) + Payment-UI
  Team 3: "Platform-Team" (Enabling-Team)
          → Shared: Auth, Design-System, CI/CD-Templates

RESULTIERENDE ARCHITEKTUR:
  ┌─────────────────┐   ┌─────────────────┐
  │  Orders-Team    │   │  Payments-Team   │
  │ ┌─────────────┐ │   │ ┌─────────────┐ │
  │ │ Order-UI    │ │   │ │ Payment-UI  │ │
  │ │ (React)     │ │   │ │ (React)     │ │
  │ └──────┬──────┘ │   │ └──────┬──────┘ │
  │        │        │   │        │        │
  │ ┌──────▼──────┐ │   │ ┌──────▼──────┐ │
  │ │Order-Service│ │   │ │Pay-Service  │ │
  │ │ (Java)      │ │   │ │ (Java)      │ │
  │ └─────────────┘ │   │ └─────────────┘ │
  └─────────────────┘   └─────────────────┘

KONSEQUENZEN:
  ① Orders-Team deployed Orders-UI und Orders-Service unabhängig
  ② Payments-Team deployed Payments ohne Abstimmung mit Orders
  ③ Keine API-Verhandlungen — jedes Team besitzt seine API
  ④ Feature "neue Zahlungsmethode": nur Payments-Team involviert
```

---

### Beispiel B: Der geografisch verteilte Teams-Monolith-Trap

```
ORGANISATION:
  Team Berlin:  5 Entwickler (Zeitzone UTC+1)
  Team Lissabon: 4 Entwickler (Zeitzone UTC)
  Team Bangalore: 6 Entwickler (Zeitzone UTC+5:30)

KONSEQUENZ NACH CONWAY:
  3 Timezones, 3h+ Überschneidungszeit → wenig synchrone Kommunikation
  → Jedes Team entwickelt seinen eigenen "Teil"
  → 3 Codebereiche entstehen organisch (auch wenn niemand das geplant hat)
  → Interfaces zwischen den Bereichen: E-Mail, Jira-Tickets, KEINE Echtzeitabstimmung

  Berlin-Code: gut für deutsche Marktspezifika, komisch für globale Features
  Bangalore-Code: schnell, viele Features, aber mit Berlin inkompatibel
  Lissabon-Code: gründlich, aber 3 Tage hinter weil Abfragen nach Bangalore warten

WAS OHNE BEWUSSTSEIN PASSIERT:
  "Wir haben einen Monolithen mit 3 de-facto-getrennten Bereichen
   ohne explizite API, mit impliziten Abhängigkeiten, koordiniert per E-Mail."
  Das ist das Schlimmste beider Welten.

MIT CONWAY-BEWUSSTSEIN (Inverse Conway Maneuver):
  ① Bounded Contexts nach DDD definieren (→ ADR-023)
  ② Module explizit nach Teams aufteilen: Berlin → DE-Compliance,
     Bangalore → Core-Ordering, Lissabon → Payment-Integration
  ③ API-Verträge explizit (OpenAPI → ADR-066), nicht per E-Mail
  ④ Spring Modulith erzwingt die Grenzen maschinell (→ ADR-079)
  → Die geografische Verteilung wird zu einem Vorteil, nicht einem Problem
```

---

### Beispiel C: Das Database-Team Anti-Pattern

```
ORGANISATION (häufig in Enterprise-Unternehmen):
  "DBA-Team": 3 spezialisierte Datenbankadministratoren
  → Alle Datenbankänderungen gehen durch dieses Team
  → Ticket erstellen → 2 Wochen Wartezeit → Migration

CONWAY-RESULTAT:
  ① Keine autonomen Teams möglich (alle warten auf DBA)
  ② Schema-Änderungen werden aufgeschoben → technische Schulden
  ③ Teams bauen Workarounds (JSON in VARCHAR speichern statt Schema-Change)
  ④ Deployment-Pipeline blockiert auf DBA-Approval-Gate
  ⑤ Entwickler kennen DB-Konzepte nicht → Antipatterns unkontrolliert

  Messbar: Durchschnittliche Zeit von Schema-Change-Idee bis Produktion: 3 Wochen

ALTERNATIVE (dezentrale DB-Ownership):
  Jedes Produkt-Team besitzt sein eigenes Schema
  (Schema-per-Modul → ADR-079, Section 5)
  
  DBA-Team wird zum "Enabling Team" (Team-Topologies):
  → Berät Teams in DB-Design
  → Definiert Standards (Index-Konventionen, Migration-Templates)
  → Reviewed kritische Änderungen (Production-Hotfixes)
  → GENEHMIGT NICHT mehr jeden Change
  
  Messbar nach 6 Monaten: Schema-Change-Zeit: 2 Tage statt 3 Wochen
  Messung: Deployment-Frequenz × 10 gestiegen (DORA)
```

---

## 3. Team Topologies: Das Framework für bewusstes Organisationsdesign

Matthew Skelton & Manuel Pais, "Team Topologies" (2019), definieren vier Team-Typen und drei Interaktionsmodi:

### 3.1 Die vier Team-Typen

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        VIER TEAM-TYPEN                                  │
├─────────────────┬───────────────────────────────────────────────────────┤
│ Stream-Aligned  │ Besitzt einen Wertstrom von Ende-zu-Ende               │
│ Team            │ Beispiel: "Orders-Team" (Frontend + Backend + DB)      │
│                 │ Ziel: unabhängiges, schnelles Delivery                 │
│                 │ Charakteristik: Full-Stack, autonomes Deployment       │
├─────────────────┼───────────────────────────────────────────────────────┤
│ Platform Team   │ Baut die interne Plattform für andere Teams            │
│                 │ Beispiel: "DevOps-Platform" (K8s, CI/CD, Observability)│
│                 │ Ziel: kognitive Last der Stream-Teams reduzieren       │
│                 │ Charakteristik: "X-as-a-Service" für andere Teams      │
├─────────────────┼───────────────────────────────────────────────────────┤
│ Enabling Team   │ Hilft anderen Teams Fähigkeiten aufzubauen            │
│                 │ Beispiel: "Security Champions", "Architecture Guild"   │
│                 │ Ziel: Wissenstransfer, kein dauerhafter Engpass        │
│                 │ Charakteristik: temporär, löst sich nach Aufgabe auf   │
├─────────────────┼───────────────────────────────────────────────────────┤
│ Complicated     │ Tiefes Spezialwissen das andere Teams brauchen        │
│ Subsystem Team  │ Beispiel: "ML-Pipeline-Team", "Crypto-Bibliothek"      │
│                 │ Ziel: Komplexität verbergen hinter einfacher API       │
│                 │ Charakteristik: klein, sehr spezialisiert              │
└─────────────────┴───────────────────────────────────────────────────────┘
```

### 3.2 Die drei Interaktionsmodi

```
KOLLABORATION (Collaboration):
  Zwei Teams arbeiten eng zusammen für eine begrenzte Zeit.
  WANN: Neues Terrain erkunden, gemeinsames Interface definieren.
  WARNSIGNAL: Dauert länger als 2-3 Sprints → Abhängigkeit aufbrechen!

  Beispiel: Orders-Team und Payments-Team definieren gemeinsam
  den Zahlungs-API-Vertrag. Danach arbeiten beide unabhängig.

X-AS-A-SERVICE (Consuming a Service):
  Team A konsumiert Service von Team B ohne Abstimmung.
  WANN: Stabile, gut dokumentierte API vorhanden.
  ZIEL: Zero-Touch-Interaction — Team A braucht kein Meeting mit B.

  Beispiel: Orders-Team konsumiert "Notification-Service" von
  Platform-Team via definiertem API — kein gemeinsames Meeting.

FACILITATION (Facilitating):
  Enabling Team hilft anderem Team, löst aber kein Problem für es.
  WANN: Wissenslücke die geschlossen werden soll (temporär).
  ZIEL: Enabling Team macht sich obsolet.

  Beispiel: Architecture-Guild-Mitglied arbeitet 2 Sprints im
  Payments-Team um Event-Sourcing zu implementieren.
  Danach: Payments-Team kann es selbst.
```

---

## 4. Kognitiver Load: Die unsichtbare Grenze

### 4.1 Warum kognitiver Load Architektur begrenzt

```
JOHN SWELLER, "Cognitive Load Theory" (1988):
  Das menschliche Arbeitsgedächtnis kann gleichzeitig etwa 7 (±2) "Chunks"
  verarbeiten. Jede zusätzliche Komplexität über diese Grenze verdrängt
  etwas anderes — und führt zu Fehlern.

ÜBERTRAGEN AUF SOFTWARE-TEAMS:
  Ein Team kann nur so viele Systeme/Domänen "in the head halten"
  bevor Qualität, Geschwindigkeit und Motivation sinken.

ARTEN DES KOGNITIVEN LOADS (Team Topologies):
  
  Intrinsic Load:   die inhärente Komplexität der Domäne
                    (Steuerrecht, Finanzregulierung, ML-Algorithmen)
                    → Kann nicht reduziert werden — ist die echte Arbeit
  
  Extraneous Load:  Komplexität durch schlechtes System-Design
                    (unklare APIs, fehlende Dokumentation, fragile Tests)
                    → KANN reduziert werden — ist Architektur-Aufgabe
  
  Germane Load:     Lernen, Kompetenzaufbau
                    → Soll vorhanden sein aber nicht alle Kapazität fressen

ZIEL: Intrinsic Load maximieren, Extraneous Load eliminieren.
```

### 4.2 Konkretes Beispiel: Zu hoher kognitiver Load

```
SITUATION: "Orders-Team" ist verantwortlich für:
  ① Order-Management (Kerndomäne)
  ② User-Authentication (komplexes OAuth2/OIDC)
  ③ PDF-Rechnung-Generierung (komplexe Bibliothek, PDF-Spec)
  ④ Datenbank-Administration (Flyway, Query-Tuning)
  ⑤ Kubernetes-Deployment-Konfiguration
  ⑥ Monitoring-Dashboard-Pflege
  ⑦ On-Call-Rotation für alle 6 Bereiche

MESSUNG des kognitiven Loads:
  → Sprintreview-Frage: "Was habt ihr diese Woche gemacht?"
  → Antwort: "Montag: Kubernetes-OutOfMemory debuggt. Dienstag: PDF-Library-Update.
    Mittwoch: Feature. Donnerstag: Monitoring alert zu viel false positives.
    Freitag: DB-Migration geschrieben."
  
  Ergebnis: 4 von 5 Tagen nicht an der Kerndomäne gearbeitet.
  Extraneous Load dominiert. Team ist erschöpft und demotiviert.

LÖSUNG: Kognitive Last durch Teamstrukturen und Plattformen reduzieren

  Platform Team übernimmt:
    ✓ Kubernetes-Deployment (Template-basiert, self-service)
    ✓ Monitoring-Grundkonfiguration (automatisch für jeden Service)
    ✓ On-Call für Infrastruktur

  Enabling Team übernimmt (temporär):
    ✓ Auth-Bibliothek einrichten → danach konsumiert Orders-Team nur die API

  Complicated Subsystem Team übernimmt:
    ✓ PDF-Service (intern genutzte API)

  Orders-Team danach:
    → 80% Zeit: Bestelldomäne
    → 20% Zeit: Infrastruktur (Flyway-Migrations, eigene Kubernetes-Manifests)
    → Kognitive Last: reduziert, fokussiert, motiviert
```

---

## 5. Der Inverse Conway Maneuver

### 5.1 Was der Inverse Conway Maneuver ist

```
NORMAL (reaktiv): Organisation beeinflusst Architektur (unbewusst)
  Team-Struktur entsteht → Architektur folgt → Architektur ist zufällig

INVERS (aktiv): Gewünschte Architektur definiert die Organisationsstruktur
  Ziel-Architektur definieren → Teams NACH Architektur ausrichten →
  Kommunikationsstruktur spiegelt Systemstruktur bewusst wider

RUTH MALAN & DANA BREDEMEYER (2008):
  "If the architecture of the system and the architecture of the organization
  are at odds, the architecture of the organization wins."
  
  → Die Organisation gewinnt IMMER. Also organisiere ZUERST richtig.
```

### 5.2 Schritt-für-Schritt: Inverse Conway Maneuver durchführen

```
SCHRITT 1: Ziel-Architektur skizzieren (Top-Down)

  Wir wollen:
    □ Order-Domäne unabhängig deploybar
    □ Payment-Domäne unabhängig deploybar
    □ Gemeinsame Plattform (Auth, Observability)
    □ Billing-Domäne (Rechnungsstellung) eigenständig

SCHRITT 2: Team-Grenzen an Domänen-Grenzen ausrichten

  VORHER:
    Team "Backend"   → Code für Orders + Payments + Billing (alles)
    Team "Frontend"  → UI für Orders + Payments + Billing (alles)

  NACHHER:
    Team "Orders"    → Orders-Backend + Orders-UI (Full-Stack)
    Team "Payments"  → Payments-Backend + Payments-UI (Full-Stack)
    Team "Billing"   → Billing-Backend (keine eigene UI zunächst)
    Team "Platform"  → Auth, K8s-Abstraction, Observability, CI/CD-Templates

SCHRITT 3: API-Verträge vor der Migration definieren
  (nicht danach — sonst entstehen organisch schlechte Grenzen)
  
  Orders ↔ Payments: welche Events fließen?
  Orders ↔ Billing: welche Daten braucht Billing von Orders?
  Orders ↔ Platform: welche Services konsumiert Orders?
  
  → OpenAPI + AsyncAPI Spezifikation vor erster Zeile Code (→ ADR-066)

SCHRITT 4: Codebase schrittweise aufteilen (Strangler Fig)
  
  Woche 1-4:  Paket-Grenzen im Monolith ziehen (Spring Modulith → ADR-079)
  Monat 2-3:  Modul-Tests isoliert (→ ADR-020)
  Monat 4-6:  Schema-Trennung (Schema-per-Modul → ADR-079)
  Monat 7+:   Service-Extraktion wenn Trigger erfüllt (→ ADR-077)

SCHRITT 5: Kommunikationsstrukturen institutionalisieren
  
  Teams sprechen über APIs, nicht über Code
  → Kein "kannst du kurz in unsere Service-Klasse schauen?"
  → Stattdessen: "Unser Contract-Test schlägt fehl (→ ADR-019)"
```

---

## 6. Dunbar's Number: Die biologische Grenze von Teams

### 6.1 Warum Team-Größe Architektur beeinflusst

```
ROBIN DUNBAR, "Neocortex Size as a Constraint on Group Size" (1992):
  Menschen können stabile soziale Beziehungen zu etwa 150 Personen halten.
  
  DUNBAR'S LAYERS:
    5  Personen: intimste Gruppe (eng zusammenarbeiten, vertrauen)
   15  Personen: enge Zusammenarbeit möglich
   50  Personen: locker koordiniert
  150  Personen: bekannt, aber keine Vertrauensbasis
  500+:          anonyme Organisation

JEFF BEZOS' "PIZZA RULE":
  "Wenn zwei Pizzen nicht reichen um ein Team zu sättigen, ist das Team zu groß."
  → Etwa 5-8 Personen

CONWAY-KONSEQUENZ:
  5-8 Personen können über eine Codebase mit etwa X KLOC sprechen.
  Wenn die Codebase größer wird als was 5-8 Personen "im Kopf halten" können
  → kognitive Überlastung → Qualität sinkt → Conway-Effekt: 
    Jeder baut "seinen Teil" ohne Gesamtbild
```

### 6.2 Konkrete Beispiele für Dunbar-Verletzungen

```
BEISPIEL: 12 Personen, ein Team, ein Service

  Probleme die auftreten:
  ① Code-Review: 11 potenzielle Reviewer → Review-Stau oder Rubber-Stamp
  ② Dailys dauern 30+ Minuten → alle hören 80% der Zeit zu
  ③ Jeder "besitzt" einen Teil → de-facto 3-4 Sub-Teams ohne Struktur
  ④ Deployments: alle 12 müssen koordiniert werden
  ⑤ Meetings: 12 Personen × Koordinations-Overhead = Produktivitätsverlust

  CONWAY-RESULTAT: Der Code zerfällt organisch in 3 Teile
  (weil 3 informelle Sub-Gruppen entstehen), aber ohne Grenzen.
  
  LÖSUNG: 12 Personen → 2 Teams à 5-6 Personen, explizite Domänen-Grenze
  → Team "Order-Core" (Bestellprozess)
  → Team "Order-Fulfillment" (Versand, Lager)
  → Kommunikation über klar definierte API (nicht über Code-Kopplung)
```

---

## 7. Technische Schulden als sozio-technisches Symptom

### 7.1 Woher technische Schulden wirklich kommen

```
GÄNGIGE ERKLÄRUNG: "Entwickler haben schlechten Code geschrieben."
SOZIO-TECHNISCHE WAHRHEIT: "Organisationsstrukturen haben schlechten Code erzwungen."

MECHANISMUS:
  ① Teams werden nach Technologie aufgeteilt (Backend/Frontend/DB/DevOps)
  ② Jede Feature-Implementierung braucht alle Teams
  ③ Teams koordinieren per Ticket und Meeting
  ④ Koordination ist langsam → Zeitdruck → Shortcuts → technische Schulden
  ⑤ Technische Schulden erhöhen Koordinationsbedarf → Teufelskreis

EMPIRISCHE EVIDENZ:
  Microsoft Research, "Organizational Volatility and Its Effects on
  Software Defects" (2009): Die Anzahl der Personen die an einer Datei
  gearbeitet haben ist ein besserer Prädiktor für Bugs als technische Metriken.
  → Je mehr Teams an einem Code-Bereich, desto mehr Bugs.
```

### 7.2 Konkrete Messung des sozio-technischen Zustands

```bash
#!/bin/bash
# scripts/socio-technical-analysis.sh
# Analysiert welche Code-Bereiche von vielen Teams angefasst werden
# (hohe Team-Kopplung = potenzielle Schulden-Quelle)

echo "=== Sozio-Technische Analyse: Code-Ownership ==="

# Für jede Java-Datei: wie viele verschiedene Autoren haben sie bearbeitet?
git log --pretty=format:"%ae" -- "*.java" | sort | uniq | wc -l

echo ""
echo "=== Dateien mit hoher Team-Rotation (> 5 verschiedene Autoren) ==="
git log --name-only --pretty=format:"%ae" -- "*.java" |
  awk 'NF > 0 { if ($0 ~ /@/) author = $0; else files[author][$0]++ }
       END { for (f in files) for (a in files[f]) count[f]++ }
       END { for (f in count) if (count[f] > 5) print count[f], f }' |
  sort -rn | head -20

# Diese Dateien sind Kandidaten für:
# a) Aufnahme in ein klar besitzendes Team (Ownership)
# b) Extraktion in ein eigenes Modul (Grenzen ziehen)
# c) Erhöhte Code-Review-Sorgfalt
```

```java
// Konkrete Code-Signatur hoher Team-Kopplung:

// ❌ Warnsignal: Klasse mit vielen verschiedenen "Eigentümern"
// git log --follow -p src/main/java/com/example/OrderService.java | grep "^Author:" | sort -u
// zeigt: 12 verschiedene Autoren in 6 Monaten
// → Jeder hat "seinen" Teil hinzugefügt ohne Gesamtbild
// → Klasse hat 847 Zeilen, 23 Methoden, 8 Abhängigkeiten

// ✅ Lösung: explizite Ownership + Grenzziehung
// Im Team-Onboarding-Dokument:
// "OrderService GEHÖRT dem Orders-Team.
//  PRs die OrderService.java ändern brauchen Approval von Orders-Team-Lead.
//  CODEOWNERS-Datei erzwingt das automatisch."

// .github/CODEOWNERS oder .gitlab/CODEOWNERS:
// /src/main/java/com/example/orders/ @orders-team
// /src/main/java/com/example/payments/ @payments-team
// /src/main/java/com/example/platform/ @platform-team
```

---

## 8. Team API: Der explizite Kommunikationsvertrag

### 8.1 Was eine Team API ist

```
IDEE (Team Topologies, Skelton & Pais):
  Jedes Team definiert explizit wie andere Teams mit ihm interagieren.
  Nicht nur der technische API-Vertrag — der soziale Vertrag.

BESTANDTEILE EINER TEAM API:
  ① Code-Ownership: welche Repositories/Module gehören dem Team?
  ② Services: welche Dienste bietet das Team an (mit SLAs)?
  ③ APIs/Contracts: welche technischen Interfaces?
  ④ Working Agreements: wie erreicht man das Team? (Ticket, Slack, Meeting?)
  ⑤ Office Hours: wann ist das Team verfügbar für Fragen?
  ⑥ Ownership-Grenzen: was ist IN-SCOPE, was NICHT?
```

### 8.2 Konkrete Team API (Markdown-Dokument)

```markdown
# Team API: Orders Team

**Team:** Orders Team  
**Mission:** Wir ermöglichen Kunden das reibungslose Aufgeben, Verfolgen
             und Stornieren von Bestellungen.

---

## Was wir besitzen (Ownership)
- `/src/orders/` — Order-Service (Backend)
- `/frontend/src/orders/` — Order-UI-Komponenten
- Kafka-Topics: `orders.placed`, `orders.confirmed`, `orders.cancelled`
- PostgreSQL-Schema: `orders.*`
- Kubernetes-Namespace: `orders-*`

## Was wir anbieten (unsere API)
| Service | SLA | Kontakt bei Problemen |
|---|---|---|
| REST API `/api/v2/orders` | P95 < 500ms, 99.9% uptime | PagerDuty: orders-oncall |
| Kafka: `orders.placed` Event | <100ms nach DB-Commit | Slack: #orders-team |
| Order-Suche (intern) | P95 < 200ms | Jira: ORDERS-Board |

## Wie ihr mit uns interagiert
| Anfrage | Kanal | SLA für Antwort |
|---|---|---|
| Bug in unserem Service | Jira ORDERS-Board | 1 Werktag |
| API-Änderungswunsch | RFC via Github Issue | 3 Werktage |
| Dringende Produktion | Slack #orders-team + PagerDuty | 15 Minuten |
| Fachliche Frage | Slack #orders-team | 4 Stunden |

## Was WIR NICHT machen (Out of Scope)
- Zahlungsabwicklung (→ Payments Team)
- Produktkatalog (→ Products Team)
- User-Authentifizierung (→ Platform Team)
- Lagerhaltung (→ Inventory Team)

## Office Hours
- Montag 14-15 Uhr: offene Sprechstunde für andere Teams
- Kein Meeting vor 10 Uhr (Deep Work Time)

## Working Agreements (intern)
- Code Review: ≤ 4 Stunden Response Time
- Deployment: jeden Dienstag und Donnerstag, 10 Uhr
- Technische Schulden: 20% der Sprint-Kapazität reserviert
```

---

## 9. Messung: Sind Organisation und Architektur aligned?

### 9.1 DORA-Metriken als sozio-technischer Gesundheitscheck

```
DORA (DevOps Research and Assessment) misst vier Metriken die sowohl
technische als auch organisatorische Gesundheit reflektieren:

DEPLOYMENT FREQUENCY:
  Elite:  Mehrfach täglich
  High:   Täglich bis wöchentlich
  Medium: Wöchentlich bis monatlich
  Low:    Monatlich bis halb-jährlich
  
  SOZIO-TECHNISCHE INTERPRETATION:
  Niedrige Frequenz = Koordinationsaufwand zu hoch
  → Teams blockieren sich → zu starke Kopplung → Conway-Problem

LEAD TIME FOR CHANGES (von Commit zu Produktion):
  Elite:  < 1 Stunde
  High:   1 Tag bis 1 Woche
  Medium: 1 Woche bis 1 Monat
  Low:    > 1 Monat
  
  SOZIO-TECHNISCHE INTERPRETATION:
  Hohe Lead Time = viele Approvals, viele Teams involviert
  → Approval-Gates folgen Organigramm (→ Conway)

CHANGE FAILURE RATE:
  Elite:  0-5%
  High:   5-10%
  Medium: 10-15%
  Low:    > 15%
  
  SOZIO-TECHNISCHE INTERPRETATION:
  Hohe Failure Rate = Teams verstehen Downstream-Abhängigkeiten nicht
  → Codebase-Kopplung nicht bekannt → implizite Architektur

MEAN TIME TO RECOVER (MTTR):
  Elite:  < 1 Stunde
  High:   < 1 Tag
  Medium: 1 Tag bis 1 Woche
  Low:    > 1 Woche
  
  SOZIO-TECHNISCHE INTERPRETATION:
  Hohe MTTR = niemand fühlt sich verantwortlich ODER
              zu viele Teams für Recovery nötig (→ fehlende Ownership)
```

### 9.2 Team-Kopplung messen (Conway-Alignment-Score)

```python
# scripts/conway-alignment.py
# Misst: Wie gut sind Teamgrenzen und Code-Grenzen aligned?

import subprocess
from collections import defaultdict

def get_file_authors(file_path, months=6):
    """Alle Autoren einer Datei in den letzten N Monaten."""
    result = subprocess.run(
        ['git', 'log', '--since', f'{months} months ago',
         '--pretty=format:%ae', '--', file_path],
        capture_output=True, text=True
    )
    return set(result.stdout.strip().split('\n'))

def get_team_for_author(author_email, team_mapping):
    """Gibt das Team eines Autors zurück."""
    for domain, team in team_mapping.items():
        if domain in author_email:
            return team
    return "unknown"

# Team-Mapping: E-Mail-Domain → Team
TEAMS = {
    'orders':   'orders-team',
    'payments': 'payments-team',
    'platform': 'platform-team',
}

# Analysiere alle Java-Dateien
import os
coupling_violations = []

for root, dirs, files in os.walk('src/main/java'):
    for file in files:
        if not file.endswith('.java'):
            continue
        path = os.path.join(root, file)
        authors = get_file_authors(path)
        teams = {get_team_for_author(a, TEAMS) for a in authors if a}

        if len(teams) > 1:  # Mehrere Teams haben diese Datei bearbeitet
            coupling_violations.append({
                'file': path,
                'teams': teams,
                'severity': len(teams)
            })

# Report
print(f"Conway-Alignment-Violations: {len(coupling_violations)}")
print(f"(Dateien die von mehreren Teams bearbeitet wurden)")
for v in sorted(coupling_violations, key=lambda x: -x['severity'])[:10]:
    print(f"  {v['severity']} Teams: {v['file']}")
    print(f"    Teams: {', '.join(v['teams'])}")
```

---

## 10. Antipatterns: Organisatorische Schulden

```
ANTIPATTERN 1: "Shared Services Team" als Bottleneck
  Symptom: Ein Team (z.B. "Platform") wird für ALLES zuständig
           → alle anderen Teams warten auf dieses Team
  Ursache: Angst vor Duplikation, Wunsch nach Standardisierung
  Conway-Konsequenz: Alle Systeme sind gekoppelt über das Platform-Team
  Lösung: Platform-Team bietet Self-Service an (Golden Path, Templates)
          → andere Teams können OHNE Ticket deployen, monitoren, skalieren

ANTIPATTERN 2: Matrix-Organisation
  Symptom: Entwickler haben zwei Vorgesetzte (Fach-Manager + Projekt-Manager)
  Ursache: Ressourcenoptimierung, Skill-Pooling
  Conway-Konsequenz: Loyalität unklar → Code ohne klare Ownership
  Lösung: Primäre Zugehörigkeit zu Produkt-Team, Sharing durch Rotation/Enabling

ANTIPATTERN 3: Feature-Teams ohne Domänen-Ownership
  Symptom: Teams werden per Feature gebildet ("Login-Team", "Checkout-Team")
           die sich nach Feature-Abschluss auflösen
  Ursache: Projekt-Denken statt Produkt-Denken
  Conway-Konsequenz: Kein nachhaltiges Ownership → kein "Wir" für einen Bereich
  Lösung: Langlebige Teams mit dauerhafter Domänen-Ownership

ANTIPATTERN 4: Zu große Batch-Releases koordiniert durch Release-Manager
  Symptom: 1× pro Monat Release, 3 Teams müssen koordinieren
  Ursache: Enge Kopplung zwischen Services/Teams
  Conway-Konsequenz: Release-Koordination IST die Architektur (implizit)
  Lösung: Unabhängige Deploy-Fähigkeit durch Domänen-Trennung → ADR-059

ANTIPATTERN 5: Architekt als Einzelperson vs. Architektur als Praxis
  Symptom: "Der Architekt" trifft alle Entscheidungen allein
  Ursache: Hierarchisches Denken, Angst vor "schlechten Entscheidungen"
  Conway-Konsequenz: Bottleneck → langsame Entscheidungen → Teams warten
  Lösung: Architektur-Gremium (Guilds, RFC-Prozess → ADR-075)
          Teams treffen Entscheidungen selbst, kommunizieren via ADRs
```

---

## 11. Alternativen & Trade-offs

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| Technologie-basierte Teams (Backend/Frontend/DB) | Expertise-Bündelung, leichte Ressourcenplanung | Jedes Feature braucht alle Teams, hohe Koordination | Conway: erzeugt geschichtete Architektur, kein autonomes Delivery |
| Feature-Teams (temporär per Feature) | Flexibel, fokussiert | Kein Ownership, technische Schulden wachsen | Kein "Wer kümmert sich danach?" → Verfall |
| Vollständig autonome Microservice-Teams | Maximale Autonomie | Ohne Plattform: jedes Team baut alles selbst | Kognitiver Load explodiert; DORA: skaliert erst ab 50+ Devs |
| Gilde/Chapter (Matrix) | Wissensaustausch erhalten | Zwei Vorgesetzte, Loyalitätskonflikte | Conway: unklare Ownership → implizite Architektur |

---

## 12. Trade-off-Matrix

| Qualitätsziel | Domänen-Teams (gewählt) | Tech-Teams | Feature-Teams |
|---|---|---|---|
| Deployment-Autonomie | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| Kognitiver Load | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Technische Exzellenz | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| Team-Stabilität | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐ |
| Feature-Velocity | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| Koordinations-Aufwand | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |

---

## 13. Kosten-Nutzen-Analyse

```
KOSTEN DER REORGANISATION (Inverse Conway Maneuver):
  Organisationsdesign-Workshop:             3 Tage × 10 Personen = 30 PT
  Team-Neubildung (Kommunikation, Friction): ~4 Wochen reduzierte Velocity
  Codebase-Restrukturierung (Modulith):     20-50 PT (→ ADR-079)
  Team-API-Dokumente schreiben:             1 PT/Team × 4 Teams = 4 PT
  GESAMT:                                   ~60-80 PT = 38.400-51.200 EUR

KOSTEN DES STATUS QUO (jährlich, messbar):
  Koordinations-Overhead (Meetings):        2h/Entwickler/Woche × 25 Devs × 50 Wochen
                                            = 2.500h/Jahr = 200.000 EUR
  Feature-Delay durch Team-Abhängigkeiten:  ∅ 1 Sprint Delay × 20 Features/Jahr
                                            = 1.600h/Jahr = 128.000 EUR
  Onboarding-Verlängerung (keine Ownership):+2 Wochen × 5 Neueinstellungen/Jahr
                                            = 400h = 32.000 EUR
  GESAMT STATUS QUO KOSTEN:                ~360.000 EUR/Jahr

BREAK-EVEN: 51.200 EUR ÷ 360.000 EUR/Jahr = 7 Wochen
```

---

## 14. Akzeptanzkriterien

- [ ] Jedes Team hat eine Team-API (`docs/teams/<name>/README.md`) mit Ownership, Services, Interaktionsregeln
- [ ] CODEOWNERS-Datei existiert und mapped Verzeichnisse auf Teams (PR-Approval erzwungen)
- [ ] DORA-Metriken werden gemessen und sind sichtbar: Deployment-Frequency, Lead Time, CFR, MTTR
- [ ] Conway-Alignment-Score: < 10% der Java-Dateien haben mehr als ein Team als Autor (letzte 3 Monate)
- [ ] Kognitive-Last-Assessment: 80% der Team-Mitglieder können Kernverantwortung in 1 Satz beschreiben
- [ ] Keine "Dependency Queue" länger als 3 Werktage (andere Teams auf unser Team wartend)
- [ ] Deployment-Unabhängigkeit: jedes Stream-Aligned-Team kann ohne andere Teams deployen

---

## 15. Quellen & Referenzen

- **Melvin E. Conway, "How Do Committees Invent?" Datamation Magazine, April 1968** — Originalformulierung von Conway's Law; die meistzitierte, meistignorierte Erkenntnis in Software-Engineering.
- **Matthew Skelton & Manuel Pais, "Team Topologies" (2019)** — vier Team-Typen, drei Interaktionsmodi; das praktischste Buch zur Organisationsgestaltung für Software-Teams.
- **Robin Dunbar, "Neocortex Size as a Constraint on Group Size in Primates" (1992)** — empirische Grundlage für Team-Größenbeschränkungen.
- **Ruth Malan & Dana Bredemeyer, "Less Is More With Minimalist Architecture" (2002), IEEE Software** — "Architecture of the organization wins" — frühe Formulierung des Inverse Conway Maneuver.
- **Nicole Forsgren, Jez Humble, Gene Kim, "Accelerate" (2018)** — empirische Basis der DORA-Metriken; Organisationskultur als stärkster Prädiktor für Software-Delivery-Performance.
- **Microsoft Research, "Organizational Volatility and Its Effects on Software Defects" (2009), FSE** — empirischer Nachweis: Anzahl der Autoren pro Datei ist stärkster Defekt-Prädiktor.
- **Eric Evans, "Domain-Driven Design" (2003), Kap. 14-16** — Bounded Contexts als natürliche Team-Grenzen; Strategic Design.
- **Fred Brooks, "The Mythical Man-Month" (1975/1995), Kap. 7-8** — "Communication overhead scales as n(n-1)/2" — mathematische Grundlage für Team-Größenbeschränkungen.
- **Allan Kelly, "Continuous Digital" (2018)** — Produkt-orientierte Teamstruktur vs. Projekt-Denken.
- **John Sweller, "Cognitive Load Theory" (1988), Cognitive Science** — theoretische Grundlage für kognitive Last in Teams.

---

## Verwandte ADRs

- **Supersedes:** —
- [ADR-023](ADR-023-domain-driven-design.md) — DDD-Bounded-Contexts als Grundlage für Team-Grenzen
- [ADR-075](ADR-075-architektur-entscheidungsprozess.md) — ADR-Prozess als sozio-technisches Werkzeug
- [ADR-077](ADR-077-microservices-vs-monolith.md) — Entscheidungsrahmen der Conway's Law berücksichtigt
- [ADR-079](ADR-079-modulith-ausfuehrlich.md) — Technische Umsetzung der Team-Grenzen
- [ADR-054](ADR-054-slo-sla-alerting.md) — DORA-Metriken als Messinstrument
