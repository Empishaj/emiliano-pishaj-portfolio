# ADR-082 — iSAQB F2: Qualitätsziele, ISO 25010 & Qualitätsszenarien

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Architektur-Board / Product Owner                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Foundation · Qualität                                   |
| Betroffene Teams  | Alle Teams, Product, Management                               |
| iSAQB-Lernziel    | LZ-2-1 bis LZ-2-7 (Foundation Level)                         |

---

## 1. Warum Qualitätsziele der Kern der Architektur sind

### 1.1 Das fundamentale Missverständnis

```
Falsche Reihenfolge (wie es meistens läuft):
  1. Technologie wählen ("Wir nehmen Microservices")
  2. System bauen
  3. Feststellen: "Performance ist katastrophal" oder "Wartung kostet Vermögen"
  4. Refactoring — zu spät und teuer

Richtige Reihenfolge (iSAQB):
  1. Qualitätsziele explizit definieren ("P95-Latenz < 200ms, Availability 99.9%")
  2. Architekturoptionen bewerten: welche erfüllt die Ziele?
  3. Entscheidung mit Begründung treffen
  4. Kontinuierlich messen ob Ziele noch erfüllt werden
```

### 1.2 Qualitätsziele sind nicht optional

Jede Architektur optimiert — bewusst oder unbewusst — auf bestimmte Qualitätsziele. Wer keine Ziele definiert, bekommt zufällige Optimierung:

```
Ohne explizite Qualitätsziele passiert Folgendes:
  → Entwickler A optimiert auf Performance (weil er das interessant findet)
  → Entwickler B optimiert auf Wartbarkeit (weil er Code-Qualität schätzt)
  → Entwickler C optimiert auf Features (weil PM-Druck existiert)
  → Ergebnis: inkonsistentes System das kein Ziel gut erfüllt

Mit expliziten Qualitätszielen:
  → Alle kennen: "P95-Latenz vor Features, Sicherheit vor Komfort"
  → Architekturentscheidungen haben eine Leitlinie
  → Konflikte können rational gelöst werden: "Dieses Cache erhöht Performance,
    aber verschlechtert Konsistenz — welches Ziel hat Priorität laut ADR-082?"
```

---

## 2. ISO/IEC 25010: Das Qualitätsmodell

### 2.1 Die acht Qualitätsmerkmale

```
ISO/IEC 25010 definiert acht Top-Level-Qualitätsmerkmale für Software-Produkte:

┌─────────────────────────────────────────────────────────────────────────────┐
│                         ISO/IEC 25010 Qualitätsmodell                       │
├─────────────────┬───────────────────────────────────────────────────────────┤
│ Merkmal         │ Untermerkmale (Auswahl)                                    │
├─────────────────┼───────────────────────────────────────────────────────────┤
│ Funktionalität  │ Vollständigkeit, Korrektheit, Angemessenheit               │
│ Zuverlässigkeit │ Fehlertoleranz, Wiederherstellbarkeit, Verfügbarkeit       │
│ Performance     │ Zeitverhalten, Ressourcennutzung, Kapazität                │
│ Benutzbarkeit   │ Erlernbarkeit, Bedienbarkeit, Fehlertoleranz für User      │
│ Sicherheit      │ Vertraulichkeit, Integrität, Authentizität, Nicht-Abstreit.│
│ Kompatibilität  │ Koexistenz, Interoperabilität                              │
│ Wartbarkeit     │ Analysierbarkeit, Modifizierbarkeit, Testbarkeit, Modularit│
│ Übertragbarkeit │ Anpassbarkeit, Installierbarkeit, Ersetzbarkeit            │
└─────────────────┴───────────────────────────────────────────────────────────┘
```

### 2.2 Für Java/Spring-Architektur relevante Merkmale im Detail

```
PERFORMANCE (Zeitverhalten):
  Systemantwortzeit bei normaler Last:         P50, P95, P99 Latenz
  Systemantwortzeit bei Spitzenlast:           Durchsatz (RPS)
  Ressourcenverbrauch:                         CPU, RAM, Disk I/O

  Warum kritisch für Architektur:
    N+1-Problem (→ ADR-016) = Performance-Antipattern
    Fehlender Index (→ ADR-073) = Latenz-Problem
    Falsches Caching (→ ADR-024) = Resource-Problem

WARTBARKEIT (Modifizierbarkeit):
  Wie schnell kann ein neues Feature eingebaut werden?
  Wie sicher können Änderungen gemacht werden?
  Wie gut kann Code verstanden werden?

  Warum kritisch für Architektur:
    Falsche OOP (→ ADR-008) = niedrige Modifizierbarkeit
    Fehlende Tests (→ ADR-010) = unsichere Änderungen
    God Classes (→ ADR-027) = schlechte Analysierbarkeit

SICHERHEIT:
  Vertraulichkeit: Daten nur für Berechtigte zugänglich
  Integrität: Daten nicht unbemerkt veränderbar
  Verfügbarkeit: System trotz Angriffe verfügbar

  Warum kritisch für Architektur:
    Fehlende Input-Validierung (→ ADR-015) = Integritätsproblem
    Fehlende Auth (→ ADR-040) = Vertraulichkeitsproblem
    Kein Rate Limiting (→ ADR-062) = Verfügbarkeitsproblem

ZUVERLÄSSIGKEIT (Fehlertoleranz):
  Wie verhalten sich sich bei Teilausfällen?
  Wie schnell erholt sich das System?

  Warum kritisch für Architektur:
    Kein Circuit Breaker (→ ADR-022) = Kaskadenausfall
    Kein Backup (→ ADR-063) = fehlende Wiederherstellbarkeit
```

---

## 3. Qualitätsszenarien: Das wichtigste Werkzeug

### 3.1 Warum Qualitätsszenarien, nicht vage Wünsche

```
❌ SCHLECHT — Vage Qualitätswünsche (nutzlos für Architektur):
  "Das System soll schnell sein."
  "Das System soll sicher sein."
  "Das System soll gut wartbar sein."

  Warum nutzlos? Diese Aussagen sind:
    → Nicht messbar (wann ist "schnell" schnell genug?)
    → Nicht prüfbar (kein Test kann "gut wartbar" verifizieren)
    → Nicht konflikttlösend ("sicher" und "benutzbar" widersprechen sich — was gewinnt?)

✅ GUT — Qualitätsszenarien (operationalisiert, messbar):
  "Wenn 500 Nutzer gleichzeitig eine Bestellung aufgeben,
   antwortet das System in 95% der Fälle innerhalb 500ms."

  Anatomy dieses Szenarios:
    Stimulus:        "500 Nutzer gleichzeitig eine Bestellung aufgeben"
    Umgebung:        "normaler Betrieb" (impliziert)
    System:          "das Bestellsystem"
    Antwort:         "antwortet"
    Messbare Antwort:"95% innerhalb 500ms"
```

### 3.2 Das Szenario-Template (iSAQB)

```
Qualitätsszenario-Template:
  ┌─────────────────────────────────────────────────────┐
  │ Quelle:     Wer/was verursacht den Stimulus?        │
  │ Stimulus:   Was passiert?                           │
  │ Umgebung:   In welchem Systemzustand?               │
  │ Artefakt:   Was wird beeinflusst?                   │
  │ Antwort:    Wie reagiert das System?                │
  │ Messbarkeit:Wie wird die Antwort gemessen?          │
  └─────────────────────────────────────────────────────┘
```

### 3.3 Konkrete Qualitätsszenarien für unser System

```yaml
# docs/architecture/quality-scenarios.yml
# Diese Datei ist die Grundlage ALLER Architekturentscheidungen

qualityScenarios:

  # ── PERFORMANCE ────────────────────────────────────────────────────
  - id: QS-P01
    merkmal: Performance
    prioritaet: HOCH
    szenario:
      quelle: "Registrierter Nutzer über Web-Browser"
      stimulus: "Gibt eine Bestellung mit 5 Artikeln auf"
      umgebung: "Normalbetrieb, 100 gleichzeitige Nutzer"
      artefakt: "Order-Service, Payment-Service"
      antwort: "Bestellung wird angelegt, Bestätigung angezeigt"
      messbarkeit: "P95-Latenz ≤ 500ms, P99-Latenz ≤ 1000ms"
    architekturelleKonsequenz:
      - "Synchrone Zahlungsverarbeitung nötig (kein 'Fire and Forget')"
      - "Datenbankindizes auf orders.user_id, orders.created_at (→ ADR-073)"
      - "Circuit Breaker für Payment-Service (→ ADR-022)"

  - id: QS-P02
    merkmal: Performance
    prioritaet: HOCH
    szenario:
      quelle: "System intern (Batch)"
      stimulus: "Monatsabrechnung für 500.000 User"
      umgebung: "Nachtbetrieb, 2:00 Uhr"
      artefakt: "Billing-Service"
      antwort: "Alle Rechnungen berechnet und versendet"
      messbarkeit: "Gesamtdauer ≤ 4 Stunden"
    architekturelleKonsequenz:
      - "Spring Batch mit Partitionierung nötig (→ ADR-068)"
      - "Kein synchrones HTTP — asynchrone Verarbeitung"
      - "Separate DB-Connection-Pool für Batch"

  # ── VERFÜGBARKEIT ──────────────────────────────────────────────────
  - id: QS-V01
    merkmal: Zuverlässigkeit/Verfügbarkeit
    prioritaet: SEHR_HOCH
    szenario:
      quelle: "Infrastruktur"
      stimulus: "Eine von drei Pod-Instanzen fällt aus"
      umgebung: "Produktionsbetrieb, Spitzenlast"
      artefakt: "Order-Service"
      antwort: "System bleibt verfügbar, kein Datenverlust"
      messbarkeit: "Availability ≥ 99.9% (max. 8.7h Ausfall/Jahr)"
    architekturelleKonsequenz:
      - "Minimum 3 Replikas (→ ADR-038 Kubernetes)"
      - "PodDisruptionBudget: minAvailable: 2"
      - "Liveness- und Readiness-Probes konfiguriert (→ ADR-038)"

  - id: QS-V02
    merkmal: Zuverlässigkeit/Fehlertoleranz
    prioritaet: HOCH
    szenario:
      quelle: "Externer Payment-Provider (Stripe)"
      stimulus: "Stripe-API antwortet nicht innerhalb 5 Sekunden"
      umgebung: "Normalbetrieb"
      artefakt: "Order-Service → Stripe-Integration"
      antwort: "Bestellung wird trotzdem angelegt, Zahlung asynchron"
      messbarkeit: "Nutzer wartet max. 1s zusätzlich, erhält Bestätigung"
    architekturelleKonsequenz:
      - "Circuit Breaker mit Fallback (→ ADR-022)"
      - "Outbox Pattern für Zahlungs-Retry (→ ADR-042)"
      - "Timeout 3s für Stripe-Calls (→ ADR-022)"

  # ── WARTBARKEIT ────────────────────────────────────────────────────
  - id: QS-W01
    merkmal: Wartbarkeit/Modifizierbarkeit
    prioritaet: HOCH
    szenario:
      quelle: "Entwickler"
      stimulus: "Neue Zahlungsmethode (PayPal) soll integriert werden"
      umgebung: "Aktive Entwicklung"
      artefakt: "Payment-Modul"
      antwort: "Neue Zahlungsmethode integriert ohne bestehende zu ändern"
      messbarkeit: "Aufwand ≤ 5 Personentage, 0 bestehende Tests brechen"
    architekturelleKonsequenz:
      - "Strategy Pattern für Zahlungsmethoden (→ ADR-030)"
      - "PaymentGateway als Port (→ ADR-031 Hexagonal)"
      - "OCP einhalten: neuer Adapter, kein Ändern bestehender (→ ADR-025)"

  # ── SICHERHEIT ─────────────────────────────────────────────────────
  - id: QS-S01
    merkmal: Sicherheit/Vertraulichkeit
    prioritaet: SEHR_HOCH
    szenario:
      quelle: "Angreifer (extern)"
      stimulus: "SQL-Injection-Angriff auf Produktsuche"
      umgebung: "Normalbetrieb"
      artefakt: "Produkt-API, Datenbank"
      antwort: "Angriff wird abgewehrt, kein Datenleak"
      messbarkeit: "0 erfolgreiche Injections in Penetration Test"
    architekturelleKonsequenz:
      - "Ausschließlich Prepared Statements / JPA (→ ADR-016)"
      - "Input-Validierung auf allen Ebenen (→ ADR-015)"
      - "SAST in CI-Pipeline (→ ADR-080)"
```

---

## 4. Qualitätsziele priorisieren: Der Konflikt ist die Regel

### 4.1 Qualitätsziele widersprechen sich immer

```
Die häufigsten Konflikte in der Praxis:

PERFORMANCE vs. SICHERHEIT:
  Performance will: cachen, viel im Memory halten, schnell sein
  Sicherheit will: validieren, verschlüsseln, begrenzen
  → Kompromiss: Caching mit TTL (frische Daten), aber Credentials nie gecacht (→ ADR-024)

PERFORMANCE vs. WARTBARKEIT:
  Performance will: optimierte SQL-Queries, denormalisierte Daten
  Wartbarkeit will: klare Domänenmodelle, normalisierte Daten
  → Kompromiss: Normalisiertes Schreibmodell, denormalisierte Lese-Projektionen (CQRS → ADR-032)

VERFÜGBARKEIT vs. KONSISTENZ (CAP-Theorem):
  Verfügbarkeit will: immer antworten, auch bei Teilausfällen
  Konsistenz will: niemals veraltete Daten zeigen
  → Kompromiss: Eventual Consistency für Lesepfade, starke Konsistenz für Schreibpfade

SICHERHEIT vs. BENUTZBARKEIT:
  Sicherheit will: 2FA, starke Passwörter, kurze Sessions
  Benutzbarkeit will: einfach, schnell, nicht nervend
  → Kompromiss: Risk-Based Auth (normales Login leicht, kritische Aktionen 2FA)
```

### 4.2 Priorisierungstabelle: Dokumentierte Konfliktergebnisse

```
# docs/architecture/quality-priorities.md

PRIORISIERUNG (höher = wichtiger bei Konflikt):

Stufe 1 — NICHT VERHANDELBAR:
  ① Datensicherheit (DSGVO, Kundendaten)
  ② Korrektheit der Finanzdaten (kein Geld verlieren)
  ③ Grundverfügbarkeit (SLA 99.9%)

Stufe 2 — HOCH:
  ④ Performance (P95 < 500ms)
  ⑤ Wartbarkeit (< 5 PT für Standard-Feature)

Stufe 3 — MITTEL:
  ⑥ Benutzbarkeit
  ⑦ Skalierbarkeit (jenseits aktueller Last)

Stufe 4 — NIEDRIG (wenn keine Konflikte):
  ⑧ Übertragbarkeit
  ⑨ Optimale Ressourcennutzung

KONFLIKTLÖSUNG: Wenn Qualitätsziel A und B konfligieren,
gewinnt dasjenige mit der niedrigeren Stufe-Nummer.
Ausnahmen brauchen explizites ADR.
```

---

## 5. Qualitätsbewusstes Design: Architektur-Taktiken

### 5.1 Taktiken für Performance

```
Taktik: Caching (→ ADR-024)
  Qualitätsziel: Zeitverhalten (Latenz reduzieren)
  Wie: Häufig benötigte Daten im Speicher halten
  Kosten: Komplexität, mögliche Inkonsistenz
  Wann einsetzen: Daten lesen > schreiben, Toleranz für leicht veraltete Daten

Taktik: Asynchrone Verarbeitung (→ ADR-041 Kafka)
  Qualitätsziel: Zeitverhalten (Antwortzeit entkoppeln von Verarbeitungszeit)
  Wie: Sofort antworten, Verarbeitung im Hintergrund
  Kosten: Eventual Consistency, Komplexität im Fehlerfall
  Wann einsetzen: Verarbeitungszeit > akzeptable Antwortzeit

Taktik: Datenbankindizes (→ ADR-073)
  Qualitätsziel: Zeitverhalten (Query-Latenz)
  Wie: Vorberechnete Sucheinstiegspunkte
  Kosten: Disk-Space, langsamere Schreiboperationen
  Wann einsetzen: Queries auf großen Tabellen mit selektiven WHERE-Klauseln
```

### 5.2 Taktiken für Wartbarkeit

```
Taktik: Modularisierung (→ ADR-079 Modulith)
  Qualitätsziel: Modifizierbarkeit, Testbarkeit
  Wie: Klare Grenzen zwischen Verantwortlichkeiten
  Kosten: Initiales Design-Investment
  Messbar: Isolierte Module-Tests < 3 Min (→ ADR-020)

Taktik: Abstraktion durch Interfaces/Ports (→ ADR-031)
  Qualitätsziel: Modifizierbarkeit (Implementierung austauschbar)
  Wie: Abhängigkeiten auf Abstraktionen, nicht Implementierungen
  Kosten: Mehr Klassen, mehr Indirektion
  Messbar: Neue DB-Implementierung: < 1 Tag Aufwand

Taktik: Automatisierte Tests (→ ADR-010 bis ADR-014)
  Qualitätsziel: Testbarkeit, Änderungssicherheit
  Wie: Tests auf allen Ebenen (Unit, Integration, E2E)
  Kosten: Test-Schreiben, Test-Wartung
  Messbar: Coverage ≥ 80%, Mutation Score ≥ 80% (→ ADR-045)
```

### 5.3 Taktiken für Verfügbarkeit

```
Taktik: Redundanz (→ ADR-038 Kubernetes Replikas)
  Qualitätsziel: Availability
  Wie: Mehrere Instanzen, kein Single Point of Failure
  Kosten: Infrastrukturkosten × Replikationsfaktor
  Messbar: Availability ≥ 99.9% nach Deployment

Taktik: Circuit Breaker (→ ADR-022)
  Qualitätsziel: Fehlertoleranz (verhindert Kaskadenausfall)
  Wie: Automatischer "Aus-Schalter" für fehlerhafte Abhängigkeiten
  Kosten: Komplexität, Fallback muss implementiert werden
  Messbar: Bei Abhängigkeitsausfall: System antwortet immer noch (Fallback)

Taktik: Graceful Degradation
  Qualitätsziel: Availability (reduzierter, aber existenter Service)
  Wie: Kernfunktionen ohne optionale Abhängigkeiten weiter betreiben
  Kosten: Zusätzliche Fallback-Implementierungen
  Messbar: Produktsuche funktioniert auch wenn Recommendation-Service down
```

---

## 6. Qualitätssicherung durch Messung

### 6.1 Qualitätsziele sind nur so gut wie ihre Messbarkeit

```java
// Beispiel: QS-P01 in der Pipeline automatisch verifiziert (→ ADR-044 Gatling)
class QualityScenarioVerification extends Simulation {

    val orderScenario = scenario("QS-P01: Order Placement")
        .exec(
            http("Place Order")
                .post("/api/v1/orders")
                .body(StringBody(orderPayload))
                .check(status().is(201))
                .check(responseTimeInMillis().lte(500))  // P95 ≤ 500ms
        )

    setUp(
        orderScenario.injectOpen(
            rampUsers(100).during(30),          // 100 gleichzeitige User
            constantUsersPerSec(100).during(60)  // Normalbetrieb-Last
        )
    )
    .assertions(
        global().responseTime().percentile(95).lte(500),   // QS-P01: P95 ≤ 500ms
        global().responseTime().percentile(99).lte(1000),  // QS-P01: P99 ≤ 1000ms
        global().failedRequests().percent().lte(1.0)        // Max 1% Fehlerrate
    )
}
```

### 6.2 Qualitätsziele im SLO-Framework (→ ADR-054)

```yaml
# Qualitätsziele als SLOs operationalisiert

slos:
  - name: "QS-P01: Order Placement Latency"
    sli: "histogram_quantile(0.95, http_request_duration_seconds{endpoint='/api/v1/orders', method='POST'})"
    slo: "< 0.5"  # 500ms
    window: "30d"
    errorBudget: "0.1%"  # 99.9% der Requests müssen < 500ms sein

  - name: "QS-V01: Order Service Availability"
    sli: "sum(rate(http_requests_total{service='order-service', status!~'5..'}[5m])) / sum(rate(http_requests_total{service='order-service'}[5m]))"
    slo: "> 0.999"
    window: "30d"
```

---

## 7. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| Keine expliziten Qualitätsziele | Kein initialer Aufwand | Zufällige Optimierung, keine Entscheidungsgrundlage | Führt beweislich zu Big-Ball-of-Mud (Fowler, 2002) |
| Nur informelle Beschreibungen | Schnell aufzuschreiben | Nicht messbar, nicht durchsetzbar | Kann nicht in SLO überführt werden (→ ADR-054) |
| ISO 25010 vollständig umsetzen | Vollständige Abdeckung | Zu viele Merkmale für ein Projekt, Overengineering | 3-5 priorisierte Szenarien pro Merkmal reichen |

---

## 8. Trade-off-Matrix

| Qualitätsziel | Explicit Scenarios + ISO25010 | Vage Wünsche | Keine Ziele |
|---|---|---|---|
| Entscheidungsgrundlage | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |
| Messbarkeit | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |
| Konfliktlösung | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |
| Initialer Aufwand | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Onboarding neuer Devs | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |

---

## 9. Kosten-Nutzen-Analyse

```
Initiale Kosten:
  Qualitätsziele-Workshop (alle Stakeholder): 1 Tag = ~8 PT gesamt
  Qualitätsszenarien formalisieren:           2 PT
  SLOs in Monitoring überführen:              3 PT
  GESAMT:                                    ~13 PT = ~8.300 EUR

Laufende Kosten:
  Quartals-Review der Szenarien:              0.5 PT/Quartal = 2 PT/Jahr

Nutzen:
  Falsche Architekturentscheidungen vermieden: eine verhinderte 50-PT-Migration = 32.000 EUR
  Klarheit bei Priorisierungskonflikten:      2h/Woche weniger Diskussion = ~3.300 EUR/Jahr
  Besseres Onboarding (klare Qualitätsziele): -1 Woche/Neueinstellung = ~3.200 EUR

Break-Even: nach der ersten verhinderten fehlgeleiteten Architekturentscheidung — typisch: 3-6 Monate
```

---

## 10. Akzeptanzkriterien

- [ ] `docs/architecture/quality-scenarios.yml` mit mindestens 8 Szenarien existiert
- [ ] Jedes Qualitätsszenario hat eine messbare Antwort (kein "soll schnell sein")
- [ ] Priorisierungstabelle dokumentiert welches Ziel bei Konflikten gewinnt
- [ ] Mindestens 3 Qualitätsszenarien sind als SLOs in Prometheus konfiguriert (→ ADR-054)
- [ ] Lasttest (→ ADR-044) verifiziert QS-P01 automatisch in der Staging-Pipeline

---

## Quellen & Referenzen

- **ISO/IEC 25010:2023** — offizielles Qualitätsmodell; acht Qualitätsmerkmale mit Untermerkmalen.
- **iSAQB CPSA-F Curriculum (2023), LZ-2-1 bis LZ-2-7** — Qualitätsziele als Basis aller Architekturentscheidungen.
- **Len Bass, Paul Clements, Rick Kazman, "Software Architecture in Practice" (2021), 4. Auflage, Kap. 4** — Quality Attribute Scenarios als Methode; ATAM-Grundlage.
- **Martin Fowler, "Patterns of Enterprise Application Architecture" (2002)** — Konflikt zwischen Performance und Wartbarkeit als fundamentales Architekturproblem.
- **DORA State of DevOps 2023** — empirische Messung: Teams mit expliziten Qualitätszielen haben 2× bessere MTTR.

---

## Verwandte ADRs

- [ADR-081](ADR-081-isaqb-grundlagen-softwarearchitektur.md) — Was ist Softwarearchitektur (Voraussetzung)
- [ADR-083](ADR-083-isaqb-architekturmuster.md) — Architekturmuster zur Erfüllung von Qualitätszielen
- [ADR-054](ADR-054-slo-sla-alerting.md) — SLOs als operationalisierte Qualitätsziele
- [ADR-044](ADR-044-load-performance-testing.md) — Lasttest zur Verifikation von Performance-Szenarien
- [ADR-061](ADR-061-fitness-functions.md) — Automatisierte Qualitätsprüfung
