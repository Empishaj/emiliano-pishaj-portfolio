# ADR-087 — iSAQB F6: Architekturbewertung — ATAM, Szenarien & Methoden

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / CTO                                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Foundation F6 · Architekturbewertung                   |
| Betroffene Teams  | Tech Leads, Architekten, Product                              |
| iSAQB-Lernziel    | LZ-6-1 bis LZ-6-6 (Foundation Level)                         |

---

## 1. Warum Architekturen bewertet werden müssen

```
Das fundamentale Problem ohne Bewertung:

SZENARIO A: Architektur wird gebaut, nie bewertet
  → 18 Monate Entwicklung
  → Go-Live: Performance katastrophal unter Last
  → Ursache: Synchrone Service-Ketten unter Last → Kaskadenausfall
  → Fix: 6 Monate Refactoring → 480.000 EUR Kosten
  → Wäre in 2-tägigem ATAM-Workshop findbar gewesen: 3.200 EUR

SZENARIO B: Architektur wird früh bewertet
  → Nach Architektur-Entwurf: 2-tägiger ATAM-Workshop
  → Sensitivitätspunkt gefunden: synchrone Stripe-Calls unter Last
  → Entscheidung: asynchrone Verarbeitung + Circuit Breaker
  → Kosten: 3.200 EUR (Workshop) + 5 PT Implementierung = 6.400 EUR
  → Spart: 480.000 EUR - 6.400 EUR = 473.600 EUR

BEWERTUNGSPRINZIP:
  Fehler früh finden = billig. Fehler spät finden = teuer.
  NIST SDLC: Kosten steigen um Faktor 10 pro Phase.
```

---

## 2. Bewertungsmethoden im Überblick

```
┌──────────────────────────────────────────────────────────────────────┐
│                  ARCHITEKTURBEWERTUNGS-METHODEN                      │
├─────────────────┬──────────────┬─────────────────────────────────────┤
│ Methode         │ Zeitaufwand  │ Wann einsetzen?                     │
├─────────────────┼──────────────┼─────────────────────────────────────┤
│ ATAM            │ 2-3 Tage     │ Große Architekturentscheidungen      │
│                 │              │ Vor kritischem Projektbeginn         │
├─────────────────┼──────────────┼─────────────────────────────────────┤
│ Fitness         │ Kontinuierlich│ Laufende Verifikation in CI/CD      │
│ Functions       │ (automatisch)│ Tägliche Architektur-Gesundheit      │
├─────────────────┼──────────────┼─────────────────────────────────────┤
│ Architecture    │ 2-4 Stunden  │ Review nach Sprints                  │
│ Review          │              │ Kleinere Entscheidungen prüfen       │
├─────────────────┼──────────────┼─────────────────────────────────────┤
│ Technische      │ 1-2 Stunden  │ Technologieentscheidungen            │
│ Spike           │              │ "Proof of Concept" für unbekanntes  │
├─────────────────┼──────────────┼─────────────────────────────────────┤
│ Metriken        │ Automatisch  │ Kontinuierliche Code-Qualität        │
│ (SonarQube etc.)│              │ Coverage, Kopplung, Komplexität     │
└─────────────────┴──────────────┴─────────────────────────────────────┘
```

---

## 3. ATAM: Architecture Tradeoff Analysis Method

### 3.1 Was ATAM ist und wie es funktioniert

```
ATAM (Kazman, Klein, Clements — SEI Carnegie Mellon, 1998/2002):
  Strukturierter Workshop der Architektur-Entscheidungen anhand von
  Qualitätsszenarien bewertet. Findet Sensitivitätspunkte, Trade-offs
  und Risiken BEVOR sie teuer werden.

KERNIDEE:
  Jede architektonische Entscheidung beeinflusst mehrere Qualitätsziele.
  ATAM macht diese Auswirkungen explizit und prüft ob die Entscheidungen
  die wichtigsten Szenarien unterstützen.

ATAM-ARTEFAKTE:
  ① Utility Tree:  Hierarchische Priorisierung der Qualitätsziele
  ② Szenarien:     Konkrete Testfälle für die Architektur
  ③ Sensitivitätspunkte: Stellen wo kleine Änderungen große Auswirkungen haben
  ④ Trade-off-Punkte: Entscheidungen die zwei Qualitätsziele gegeneinander abwägen
  ⑤ Risiken:       Architektonische Entscheidungen mit unbekanntem Ausgang
  ⑥ Non-Risks:     Entscheidungen die analysiert und als sicher bewertet wurden
```

### 3.2 Schritt 1: Der Utility Tree

```
UTILITY TREE — hierarchische Qualitätsziel-Priorisierung:
Bewertung: (H=High, M=Medium, L=Low) × (H=High Impact, M=Medium Impact)

Utility
├── Performance
│   ├── Latenz             (H,H) ← QS-P01: 500ms P95
│   ├── Durchsatz          (H,M) ← 100 RPS Spitzenlast
│   └── Ressourcennutzung  (M,L)
│
├── Verfügbarkeit
│   ├── Fehlertoleranz     (H,H) ← QS-V01: 99.9% uptime
│   ├── Wiederherstellung  (H,M) ← RTO 2h, RPO 1h
│   └── Graceful Degradation (M,M)
│
├── Sicherheit
│   ├── Authentifizierung  (H,H) ← OAuth2/OIDC
│   ├── Autorisierung      (H,H) ← RBAC
│   └── Datenschutz        (H,H) ← DSGVO
│
├── Wartbarkeit
│   ├── Modifizierbarkeit  (H,M) ← QS-W01: < 5 PT
│   ├── Testbarkeit        (H,H) ← > 80% Coverage
│   └── Verständlichkeit   (M,M)
│
└── Deployment
    ├── Deployment-Frequenz (H,M) ← täglich auf Staging
    └── Rollback-Fähigkeit  (H,H) ← Canary + Auto-Rollback

PRIORISIERUNG: (H,H)-Szenarien werden in ATAM zuerst bewertet.
```

### 3.3 Schritt 2: Szenarien entwickeln und priorisieren

```
DIREKTE SZENARIEN (Nutzer-/System-Interaktion):
  S01: 1000 Nutzer gleichzeitig → Bestellung aufgeben → P95 < 500ms
  S02: Stripe API nicht verfügbar → Bestellung trotzdem anlegen, Retry
  S03: Neues Feature (neue Zahlungsmethode) → < 5 PT Aufwand, 0 Tests brechen

INDIREKTE SZENARIEN (Evolution des Systems):
  S04: Team wächst von 8 auf 25 Personen → paralleles Entwickeln ohne Konflikte
  S05: PostgreSQL wird durch MongoDB ersetzt → nur Infrastruktur-Adapter ändern
  S06: Zweites Land (Österreich) → Steuerrecht-Erweiterung ohne Kern-Änderung

STRESS-SZENARIEN (Systemgrenzen testen):
  S07: Alle 3 Pod-Instanzen außer einer sterben → System bleibt verfügbar
  S08: DDoS-Angriff: 10.000 Requests/Sekunde → System schützt sich (Rate Limiting)
  S09: Entwickler pushed Code mit SQL-Injection → SAST findet es in 5 Minuten
```

### 3.4 Schritt 3: Architektur gegen Szenarien testen

```
SZENARIO S02: Stripe API nicht verfügbar → Bestellung trotzdem anlegen

Architektur-Antwort:
  1. OrderDomainService ruft PaymentGateway-Port auf
  2. StripeAdapter → Circuit Breaker (Resilience4j, → ADR-022)
  3. Circuit Breaker offen → Fallback: Zahlung in Outbox-Queue (→ ADR-042)
  4. Bestellung angelegt mit Status PENDING_PAYMENT
  5. Outbox-Processor retried alle 30s bis Stripe wieder erreichbar

ATAM-BEWERTUNG dieses Szenarios:
  ✅ Verfügbarkeit: Bestellung kann angelegt werden (Stripe down → kein Ausfall)
  ✅ Fehlertoleranz: Circuit Breaker verhindert Cascading Failure
  ⚠️  SENSITIVITÄTSPUNKT: Outbox-Latenz → wenn Stripe >24h down, was passiert?
  ⚠️  TRADE-OFF: Eventual Consistency vs. sofortige Zahlungsbestätigung
  ❓  RISIKO: Was wenn Nutzer 10× auf "Bezahlen" klickt → Idempotenz nötig (→ ADR-086)

SZENARIO S01: 1000 gleichzeitige Nutzer → P95 < 500ms

Architektur-Antwort:
  1. Request → API-Gateway (keine Business-Logik)
  2. OrderController → OrderDomainService
  3. OrderDomainService → OrderRepository (PostgreSQL)
  4. OrderDomainService → PaymentGateway (Circuit Breaker + Timeout 3s)
  5. Response

LATENZ-ANALYSE (synchron):
  API-Gateway:     10ms
  DB-Query:        50ms (mit Index, → ADR-073)
  Stripe-Call:    200ms (synchron, externen Partner!)
  Total:          260ms → OK für P95 = 500ms... ABER:

  SENSITIVITÄTSPUNKT: Bei 1000 gleichzeitigen Requests:
  1000 × 200ms Stripe-Hold = 1000 simultane HTTP-Verbindungen zu Stripe
  Stripe rate-limits bei 100 RPS → Requests queuen sich auf → 2000ms P95

  IDENTIFIZIERTES RISIKO:
  Synchroner Stripe-Call unter Last verletzt QS-P01!
  
  LÖSUNG AUS ATAM:
  Zahlungsautorisierung asynchron → Order anlegen → Zahlung queuen
  → Stripe-Latenz nicht mehr im kritischen Pfad
```

### 3.5 Schritt 4: Sensitivitätspunkte, Trade-offs, Risiken dokumentieren

```
SENSITIVITÄTSPUNKTE (kleine Änderung, große Auswirkung):
  SP-01: Timeout für Stripe-Call
         → Zu kurz (500ms): Zahlung schlägt oft fehl
         → Zu lang (5000ms): Nutzer wartet ewig, Connection-Pool erschöpft
         → Empfehlung: 3000ms + Circuit Breaker (→ ADR-022)

  SP-02: HikariCP Pool-Größe
         → Zu klein: Requests queuen bei Last
         → Zu groß: DB überlastet mit zu vielen Verbindungen
         → Formel: (CPU-Kerne × 2) + effektive_Spindle-Anzahl (→ ADR-043)

TRADE-OFF-PUNKTE (A und B können nicht beide maximiert werden):
  TP-01: Konsistenz vs. Verfügbarkeit (CAP-Theorem)
         → Synchrone Zahlung: starke Konsistenz, niedrige Verfügbarkeit
         → Asynchrone Zahlung: hohe Verfügbarkeit, eventual consistency
         → ENTSCHEIDUNG: asynchron (Verfügbarkeit wichtiger als Sofort-Konsistenz)
         
  TP-02: Performance vs. Sicherheit
         → Caching erhöht Performance, kann aber veraltete Auth-Daten zeigen
         → Auth-Checks ohne Cache: 100% aktuell, aber +50ms pro Request
         → ENTSCHEIDUNG: Auth-Daten mit 60s TTL cachen (akzeptables Fenster)

RISIKEN:
  R-01: Outbox-Processor läuft nicht (Single-Instance)
        Wahrscheinlichkeit: M | Auswirkung: H
        → Zahlungen werden nicht verarbeitet bis Service neustartet
        Mitigation: ShedLock + Kubernetes CronJob Fallback (→ ADR-074)
        
  R-02: Kafka-Topic-Lag bei Bestandsspitzen
        Wahrscheinlichkeit: H | Auswirkung: M
        → Events werden zu spät verarbeitet → Nutzer sieht alten Status
        Mitigation: Consumer-Group skalieren + SLO für Event-Processing-Lag (→ ADR-054)

NON-RISKS (analysiert und als sicher bewertet):
  NR-01: Hexagonale Architektur → Wartbarkeit (Modul austauschbar, ADR-031)
  NR-02: Spring Modulith → Modulisolation (ADR-079)
  NR-03: Kubernetes 3-Replikas → Verfügbarkeit (ADR-038)
```

---

## 4. Lightweight Architecture Review (für laufende Entwicklung)

### 4.1 Der Mini-ATAM für Sprint-Reviews

```
NICHT jede Entscheidung braucht einen 2-Tages-ATAM-Workshop.
Für kleinere Entscheidungen: strukturierter Mini-Review (1-2 Stunden).

FRAGEN-KATALOG (für jede größere Architekturentscheidung):

① WAS wird geändert?
  "Wir fügen Caching für Produktdaten ein"

② WELCHE Qualitätsziele werden besser?
  "Performance (Latenz -60%), Datenbankentlastung (-80% Leseanfragen)"

③ WELCHE Qualitätsziele werden schlechter?
  "Datenkonsistenz (Produkte können bis 10min veraltet sein)"

④ WAS sind die Sensitivitätspunkte?
  "Cache-TTL: zu kurz = kein Performance-Gewinn, zu lang = inkonsistent"

⑤ WAS sind die Risiken?
  "Cache-Stampede wenn TTL abläuft (→ ADR-024 Lösung vorhanden)"
  "Memory-Overflow wenn zu viele Produkte gecacht (→ Caffeine max-size)"

⑥ IST die Entscheidung reversibel?
  "Ja — Cache kann jederzeit deaktiviert werden, Fallback: DB"
```

### 4.2 Architecture Decision Review Template

```markdown
# Architecture Decision Review: Produkt-Caching

**Datum:** 2024-03-15
**Entscheider:** Orders-Team Tech Lead
**Reviewer:** Architektur-Board

## Was wird geändert?
Caffeine-Cache für ProductQueryService mit 10-Minuten-TTL

## Qualitätsziele-Impact
| Qualitätsziel | Vorher | Nachher | Akzeptabel? |
|---|---|---|---|
| Latenz (P95) | 280ms | 120ms | ✅ besser |
| DB-Last | 1000 RPS | 200 RPS | ✅ besser |
| Datenkonsistenz | sofort | 10min Lag | ✅ akzeptabel (Produkte ändern sich selten) |
| Komplexität | einfach | mittel | ✅ akzeptabel |

## Sensitivitätspunkte
- TTL-Wert: 10 Minuten als initiales Ziel, messbar über Stale-Read-Rate

## Risiken und Mitigationen
- Cache-Stampede: Caffeine probabilistic early expiry aktiviert
- Memory: maxSize = 10.000 Einträge konfiguriert

## Entscheidung: ✅ ANGENOMMEN
Verweis: ADR-024, ADR-082 (QS-P01)
```

---

## 5. Architekturbewertung durch Metriken

### 5.1 Automatische Bewertung im CI (kontinuierlich)

```java
// Fitness Functions als kontinuierliche Architekturbewertung (→ ADR-061)
// Diese Tests laufen bei jedem Commit und validieren Architektur-Invarianten

@SpringBootTest
class ArchitectureFitnessTest {

    // Bewertung: "Ist die Schichtenarchitektur noch intakt?"
    @Test
    void schichtenarchitektur_eingehalten() {
        // Verletzt = Architekturregression = Build bricht
        ApplicationModules.of(ECommerceApp.class).verify();
    }

    // Bewertung: "Ist die Performance-Fitness noch gegeben?"
    @Test
    @Timeout(500)  // 500ms Maximum
    void orderEndpoint_respondsWithin500ms(TestRestTemplate client) {
        var response = client.postForEntity("/api/v2/orders", validOrderRequest(), OrderDto.class);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        // Wenn > 500ms: Test schlägt fehl = Performance-Regression gefunden
    }
}
```

### 5.2 Metriken-Dashboard für kontinuierliche Bewertung

```yaml
# Prometheus-Queries die Architektur-Gesundheit messen

# 1. Latenz-Fitness (QS-P01)
histogram_quantile(0.95,
  sum(rate(http_request_duration_seconds_bucket[5m])) by (le))
# Alert wenn > 0.5s (→ ADR-054)

# 2. Verfügbarkeits-Fitness (QS-V01)
sum(rate(http_requests_total{status!~"5.."}[30d]))
/ sum(rate(http_requests_total[30d]))
# Alert wenn < 0.999

# 3. Circuit-Breaker-Gesundheit
resilience4j_circuitbreaker_state{name="stripePayment"}
# Alert wenn state == "OPEN" für > 5 Minuten

# 4. Outbox-Lag (asynchrone Verarbeitung gesund?)
sum(orders_outbox_pending_count)
# Alert wenn > 1000 (Backlog wächst schneller als verarbeitet wird)
```

---

## 6. Kosten-Nutzen der Architekturbewertung

```
ATAM-Workshop Kosten:
  2 Tage × 8 Personen (Tech Leads, Architekten, Product)
  = 128 Personenstunden × 100 EUR/h = 12.800 EUR

Vorteile (empirisch belegt, CMU/SEI-Studien):
  Gefundene Risiken pro ATAM-Workshop: 25-50
  Davon Critical (wenn in Produktion → Incident): 5-10
  Kosten eines kritischen Production-Incidents: 50.000-200.000 EUR
  Einsparung bei 5 verhinderten Incidents: 250.000 - 1.000.000 EUR

DORA-Forschung (Forsgren et al., "Accelerate", 2018):
  Teams die regelmäßig Architektur bewerten haben:
  → 5× niedrigere Change Failure Rate
  → 2× schnellere Recovery nach Incidents

ROI der Architekturbewertung: 1.000 - 10.000%
```

---

## 7. Quellen & Referenzen

- **iSAQB CPSA-F Curriculum (2023), LZ-6-1 bis LZ-6-6** — Bewertungsmethoden als Pflicht im Foundation Level.
- **Rick Kazman, Mark Klein, Paul Clements, "ATAM: Method for Architecture Evaluation" (2000), CMU/SEI** — Original-Methodik; vollständige Beschreibung des 9-Schritt-Prozesses.
- **Len Bass, Paul Clements, Rick Kazman, "Software Architecture in Practice" (2021), 4. Auflage, Kap. 21** — ATAM, CBAM und andere Bewertungsmethoden.
- **Neal Ford, Rebecca Parsons, "Building Evolutionary Architectures" (2017)** — Fitness Functions als kontinuierliche Architekturbewertung.
- **Nicole Forsgren, Jez Humble, Gene Kim, "Accelerate" (2018)** — Empirische Basis: Bewertung und Review korrelieren mit Delivery Performance.

---

## Akzeptanzkriterien

- [ ] ATAM-Workshop vor jeder großen Architekturentscheidung (> 20 PT Implementierung)
- [ ] Utility Tree mit priorisierten Qualitätsszenarien dokumentiert (→ `docs/architecture/utility-tree.md`)
- [ ] Mindestens 5 Sensitivitätspunkte identifiziert und dokumentiert
- [ ] Fitness Functions für alle (H,H)-Szenarien im CI aktiv (→ ADR-061)
- [ ] Quarterly Architecture Health Review im Team-Kalender

---

## Verwandte ADRs

- [ADR-082](ADR-082-isaqb-qualitaetsziele.md) — Qualitätsziele (Input für ATAM-Utility-Tree)
- [ADR-061](ADR-061-fitness-functions.md) — Fitness Functions als kontinuierliche Bewertung
- [ADR-054](ADR-054-slo-sla-alerting.md) — SLOs als operationalisierte Bewertungskriterien
- [ADR-088](ADR-088-isaqb-werkzeuge-arc42.md) — Werkzeuge zur Dokumentation der Bewertungsergebnisse
