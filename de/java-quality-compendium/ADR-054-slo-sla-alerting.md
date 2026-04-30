# ADR-054 — SLO/SLA, Alerting & On-Call-Strategie

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Prometheus · Grafana · AlertManager          |
| Datum      | 2024-01-01                                        |
| Kategorie  | Observability / Operations                        |

---

## Kontext & Problem

Ohne definierte SLOs weiß niemand wann der Service "gut genug" läuft und wann er gecrasht ist. Ohne Alerting bemerkt man Probleme wenn Kunden sich beschweren. Ohne On-Call-Strategie wacht um 3 Uhr der falsche Mensch auf. Dieses ADR definiert den Rahmen für messbare Verlässlichkeit.

---

## SLI, SLO, SLA — Definitionen

```
SLI (Service Level Indicator): Was wird gemessen?
  → "P95-Latenz der /api/v1/orders Endpunkte in den letzten 5 Minuten"
  → "Error-Rate (5xx) über alle Endpunkte"
  → "Availability: Prozentsatz erfolgreicher Requests"

SLO (Service Level Objective): Internes Ziel
  → P95-Latenz < 500ms (99% der Zeitfenster pro Woche)
  → Error-Rate < 0.5% (über rollendes 30-Tage-Fenster)
  → Availability ≥ 99.9% pro Monat (erlaubt 43 Minuten Ausfall)

SLA (Service Level Agreement): Externe Vereinbarung mit Konsequenzen
  → Kundenseitig vereinbart, oft mit Gutschriften bei Verletzung
  → SLO ist das interne Ziel — sollte strenger als SLA sein (Buffer)
```

---

## SLOs definieren: Error Budget

```
Availability SLO: 99.9% pro Monat
→ Erlaubtes Ausfallbudget: 0.1% von 30 Tagen = 43.8 Minuten

Wenn Error Budget zu 50% aufgebraucht:
→ Feature-Freeze bis Budget sich erholt
→ Fokus auf Reliability-Verbesserungen

Wenn Error Budget zu 100% aufgebraucht:
→ Incident Review verpflichtend
→ Keine neuen Features bis Ursache behoben

Error Budget Metrik in Prometheus:
1 - (sum(rate(http_requests_total{status=~"5.."}[30d]))
   / sum(rate(http_requests_total[30d])))
```

---

## Prometheus Alerting Regeln

```yaml
# prometheus/alerts.yml
groups:
  - name: order-service-slos
    rules:

      # ── Availability Alert ─────────────────────────────────────────
      - alert: HighErrorRate
        expr: |
          sum(rate(http_requests_total{
            job="order-service",
            status=~"5.."
          }[5m]))
          /
          sum(rate(http_requests_total{job="order-service"}[5m]))
          > 0.01
        for: 2m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "Error rate > 1% for order-service"
          description: "Error rate is {{ $value | humanizePercentage }} (SLO: < 0.5%)"
          runbook: "https://wiki.example.com/runbooks/high-error-rate"

      # ── Latency Alert (P95) ────────────────────────────────────────
      - alert: HighP95Latency
        expr: |
          histogram_quantile(0.95,
            sum(rate(http_request_duration_seconds_bucket{
              job="order-service"
            }[5m])) by (le)
          ) > 0.5
        for: 5m
        labels:
          severity: warning
          team: backend
        annotations:
          summary: "P95 latency > 500ms for order-service"
          description: "P95 latency is {{ $value | humanizeDuration }}"
          runbook: "https://wiki.example.com/runbooks/high-latency"

      # ── Availability (Black Box) ──────────────────────────────────
      - alert: ServiceDown
        expr: up{job="order-service"} == 0
        for: 1m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "Order service is down"
          runbook: "https://wiki.example.com/runbooks/service-down"

      # ── Error Budget Burn Rate ─────────────────────────────────────
      # Fast burn: kritisch wenn in 1h so viel verbrannt wie in 6h erlaubt
      - alert: ErrorBudgetBurnRateCritical
        expr: |
          (
            sum(rate(http_requests_total{status=~"5.."}[1h]))
            / sum(rate(http_requests_total[1h]))
          )
          / (1 - 0.999)  # SLO = 99.9%
          > 14.4          # 14.4× = 1-Stunden-Burn für 6h-Budget
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Error budget burning too fast"
```

---

## AlertManager: Routing und Eskalation

```yaml
# alertmanager/alertmanager.yml
route:
  receiver: default
  group_by: [alertname, team]
  group_wait:      30s
  group_interval:  5m
  repeat_interval: 4h

  routes:
    # Kritische Alerts → PagerDuty (weckt On-Call)
    - match:
        severity: critical
      receiver: pagerduty-critical
      continue: true  # Auch an Slack senden

    # Warnungen → Slack (kein Wake-Up)
    - match:
        severity: warning
      receiver: slack-warnings

receivers:
  - name: pagerduty-critical
    pagerduty_configs:
      - routing_key: $PAGERDUTY_KEY
        description: "{{ .GroupLabels.alertname }}: {{ .CommonAnnotations.summary }}"

  - name: slack-warnings
    slack_configs:
      - api_url: $SLACK_WEBHOOK
        channel: "#backend-alerts"
        text: |
          *{{ .GroupLabels.alertname }}*
          {{ .CommonAnnotations.summary }}
          Runbook: {{ .CommonAnnotations.runbook }}

  - name: default
    slack_configs:
      - api_url: $SLACK_WEBHOOK
        channel: "#backend-monitoring"

inhibit_rules:
  # Wenn Service down: Latency-Alert unterdrücken (logisch: macht keinen Sinn)
  - source_match:
      alertname: ServiceDown
    target_match_re:
      alertname: (HighP95Latency|HighErrorRate)
    equal: [job]
```

---

## Grafana Dashboard: SLO-Übersicht

```json
// Grafana Dashboard JSON (Auszug)
{
  "panels": [
    {
      "title": "Error Rate (SLO: < 0.5%)",
      "type": "stat",
      "targets": [{
        "expr": "sum(rate(http_requests_total{status=~'5..'}[5m])) / sum(rate(http_requests_total[5m]))"
      }],
      "thresholds": {
        "steps": [
          {"value": 0,     "color": "green"},
          {"value": 0.005, "color": "yellow"},  // SLO-Grenze
          {"value": 0.01,  "color": "red"}
        ]
      }
    },
    {
      "title": "P95 Latency (SLO: < 500ms)",
      "type": "gauge",
      "targets": [{
        "expr": "histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket[5m])) by (le))"
      }]
    },
    {
      "title": "Error Budget (30 Tage)",
      "type": "stat",
      "targets": [{
        "expr": "(1 - (sum(rate(http_requests_total{status=~'5..'}[30d])) / sum(rate(http_requests_total[30d])))) / (1 - 0.999) * 100"
      }],
      "unit": "percent"
    }
  ]
}
```

---

## On-Call-Strategie

```markdown
## Severity-Definitionen

P0 — Critital: Service komplett down, Datenverlust möglich
  → Sofort wecken (PagerDuty), Eskalation nach 15 Min wenn kein ACK

P1 — High: Signifikante Fehlerrate (> 5%), SLO-Verletzung
  → Wecken im Bereitschaftsdienst, Response innerhalb 30 Min

P2 — Medium: Degraded Performance (SLO-Warnung), nicht kritisch
  → Slack-Benachrichtigung, Reaktion während Geschäftszeiten

P3 — Low: Informativ, Trend-Warnung
  → Ticket erstellen, nächste Woche adressieren

## Runbooks: Pflicht für jeden Alert

Jeder Prometheus-Alert MUSS ein Runbook-Link haben:
1. Was bedeutet dieser Alert?
2. Was sind mögliche Ursachen?
3. Diagnose-Schritte (Commands, Dashboard-Links)
4. Lösungsmaßnahmen
5. Eskalationspfad
```

---

## Spring Boot: SLO-Histogramme für Prometheus

```java
// application.yml — SLO-Buckets für Latenz-Histogramm
management:
  metrics:
    distribution:
      slo:
        http.server.requests: 100ms, 250ms, 500ms, 1000ms, 2500ms
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

---

## Konsequenzen

**Positiv:** SLOs machen "gut genug" messbar. Error Budget gibt Teams Autonomie — solange Budget vorhanden, können Features geliefert werden. Runbooks reduzieren Mean-Time-to-Recovery (MTTR).

**Negativ:** SLOs müssen kalibriert werden — zu streng führt zu Alert-Fatigue, zu locker zu verpassten Incidents. On-Call ist emotionaler Aufwand — faire Rotation und Kompensation nötig.

---

## 💡 Guru-Tipps

- **Alert auf Symptome, nicht Ursachen**: `ErrorRateHigh` statt `DatabaseConnectionPoolFull` — ersteres ist für User sichtbar.
- **Silence-Windows** in AlertManager für geplante Wartungsfenster.
- **Post-Mortem ohne Schuld**: Jedes P0/P1-Incident hat ein blameless Post-Mortem — Systemproblem, nie Menschenproblem.
- **Amixr / PagerDuty** für Eskalations-Ketten und On-Call-Rotation.

---

## Verwandte ADRs

- [ADR-017](ADR-017-observability-logging-tracing.md) — Metriken als Grundlage für SLIs.
- [ADR-044](ADR-044-load-performance-testing.md) — Load Tests validieren SLOs vorab.
