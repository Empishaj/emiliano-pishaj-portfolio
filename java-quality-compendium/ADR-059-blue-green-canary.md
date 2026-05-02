# ADR-059 — Blue/Green Deployment, Canary Releases & Rollback

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Boot 3.x · Kubernetes · Argo Rollouts |
| Datum      | 2024-01-01                                        |
| Kategorie  | DevOps / Release Management                       |

---

## Kontext & Problem

Rolling Deployments (→ ADR-038) deployen Schritt für Schritt. Blue/Green und Canary sind zwei Strategien für risikoärmere Releases wenn Fehlerrate bei neuer Version sofort gemessen werden soll. Jede Strategie passt zu anderen Risikoprofilen.

---

## Deployment-Strategien im Vergleich

```
Rolling Update (Standard K8s):
→ Pods werden nacheinander ersetzt
→ Kurze Zeit: alte und neue Version gleichzeitig aktiv
→ Rollback: langsam (neue Version muss wieder zurückgerollt werden)
→ Gut für: normale Updates ohne Breaking Changes

Blue/Green:
→ Zwei identische Produktionsumgebungen (blue = aktuell, green = neu)
→ Traffic wechselt sofort: 0% → 100%
→ Rollback: sofort (Traffic zurückschalten)
→ Gut für: Breaking Changes, Datenbankmigrationen die rückwärtskompatibel sind
→ Kosten: doppelte Infrastruktur während Deployment

Canary:
→ Neue Version bekommt schrittweise Traffic: 5% → 25% → 50% → 100%
→ Fehler treffen nur kleinen Teil der User
→ Rollback: Traffic auf 0% für neue Version
→ Gut für: Risikoreiche Features, A/B-Tests
→ Braucht: Metriken-basierte Promotion (Error Rate, Latency)
```

---

## Blue/Green mit Kubernetes

```yaml
# Blue Deployment (aktuell aktiv)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service-blue
  labels:
    app: order-service
    slot: blue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
      slot: blue
  template:
    metadata:
      labels:
        app: order-service
        slot: blue
        version: "1.0.0"
    spec:
      containers:
        - name: order-service
          image: ghcr.io/example/order-service:sha-abc123
---
# Green Deployment (neue Version, noch kein Traffic)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service-green
  labels:
    slot: green
spec:
  replicas: 3
  template:
    metadata:
      labels:
        app: order-service
        slot: green
        version: "2.0.0"
    spec:
      containers:
        - name: order-service
          image: ghcr.io/example/order-service:sha-def456
---
# Service: Traffic-Steuerung durch Selector-Wechsel
apiVersion: v1
kind: Service
metadata:
  name: order-service
spec:
  selector:
    app: order-service
    slot: blue    # ← hier von blue auf green umschalten
  ports:
    - port: 80
      targetPort: 8080
```

```bash
# Blue → Green umschalten (sofortiger Traffic-Wechsel)
kubectl patch service order-service \
  -p '{"spec":{"selector":{"slot":"green"}}}'

# Smoke-Test der neuen Version
./scripts/smoke-test.sh https://order-service.example.com

# Rollback: sofort zurück zu blue
kubectl patch service order-service \
  -p '{"spec":{"selector":{"slot":"blue"}}}'
```

---

## Canary mit Argo Rollouts

```yaml
# Argo Rollouts: schrittweise Traffic-Migration mit automatischer Analyse
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: order-service
spec:
  replicas: 10
  strategy:
    canary:
      steps:
        - setWeight: 5          # 5% Canary Traffic
        - pause: {duration: 10m}  # 10 Minuten warten und Metriken prüfen
        - setWeight: 25
        - pause: {duration: 10m}
        - setWeight: 50
        - pause: {duration: 10m}
        - setWeight: 100        # Vollständiger Rollout
      analysis:
        templates:
          - templateName: order-service-analysis
        startingStep: 1
        args:
          - name: service-name
            value: order-service-canary

---
# AnalysisTemplate: automatische Rollback-Entscheidung
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata:
  name: order-service-analysis
spec:
  metrics:
    - name: error-rate
      interval: 2m
      successCondition: result[0] < 0.01   # < 1% Fehlerrate
      failureLimit: 3
      provider:
        prometheus:
          address: http://prometheus:9090
          query: |
            sum(rate(http_requests_total{
              service="{{args.service-name}}",
              status=~"5.."
            }[2m]))
            /
            sum(rate(http_requests_total{
              service="{{args.service-name}}"
            }[2m]))

    - name: p95-latency
      interval: 2m
      successCondition: result[0] < 0.5    # < 500ms P95
      failureLimit: 3
      provider:
        prometheus:
          address: http://prometheus:9090
          query: |
            histogram_quantile(0.95,
              sum(rate(http_request_duration_seconds_bucket{
                service="{{args.service-name}}"
              }[2m])) by (le)
            )
```

---

## Datenbankmigrationen und Blue/Green

```
Problem: Blue/Green erfordert dass alte und neue Version
         zeitweise dieselbe DB nutzen.

Lösung: Expand/Contract Pattern (3 Deployment-Schritte)

Schritt 1: EXPAND — rückwärtskompatible Migration (neue Spalte nullable)
  V15__add_priority_column.sql:
  ALTER TABLE orders ADD COLUMN priority INTEGER;  -- nullable!
  → Alte Version (blue): ignoriert priority
  → Neue Version (green): setzt priority

Schritt 2: DEPLOY — nach vollständigem Wechsel auf neue Version
  → Beide Versionen schreiben priority

Schritt 3: CONTRACT — alte Version nicht mehr im Einsatz
  V16__make_priority_not_null.sql:
  UPDATE orders SET priority = 0 WHERE priority IS NULL;
  ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
  → Erst jetzt: Constraint setzen

Breaking Schema Change → niemals direkt mit Blue/Green!
```

---

## Feature Flags als Ergänzung (→ ADR-039)

```java
// Canary-Release und Feature Flags zusammen:
// 5% der User bekommen neue Version + neues Feature
// 95% der User: alte Version + Feature deaktiviert

@Service
public class CheckoutService {
    private final FeatureService features;

    public CheckoutResult checkout(CheckoutCommand cmd) {
        if (features.isEnabledForUser("new-checkout-flow", cmd.userId())) {
            return newCheckout.execute(cmd);    // Neue Version
        }
        return legacyCheckout.execute(cmd);     // Alte Version
    }
}
// Rollout ohne Deployment: nur Flag-Prozentsatz erhöhen
```

---

## GitHub Actions: Blue/Green Pipeline

```yaml
deploy-production:
  runs-on: ubuntu-latest
  environment: production
  steps:
    - name: Deploy Green Slot
      run: |
        kubectl set image deployment/order-service-green \
          order-service=${{ env.IMAGE }}:${{ github.sha }}
        kubectl rollout status deployment/order-service-green

    - name: Smoke Test Green
      run: ./scripts/smoke-test.sh https://green.order-service.internal

    - name: Switch Traffic to Green
      run: |
        kubectl patch service order-service \
          -p '{"spec":{"selector":{"slot":"green"}}}'

    - name: Wait and Verify
      run: |
        sleep 120  # 2 Minuten Traffic auf Green
        ./scripts/verify-error-rate.sh   # Error Rate < 1%?

    - name: Update Blue Slot (für nächstes Deployment)
      run: |
        kubectl set image deployment/order-service-blue \
          order-service=${{ env.IMAGE }}:${{ github.sha }}

    # Automatischer Rollback wenn verify schlägt fehl
    - name: Rollback on Failure
      if: failure()
      run: |
        kubectl patch service order-service \
          -p '{"spec":{"selector":{"slot":"blue"}}}'
        echo "::error::Deployment failed — rolled back to blue"
```

---

## Konsequenzen

**Positiv:** Blue/Green: sofortiger Rollback ohne Pod-Restart. Canary: Fehler treffen nur kleinen User-Anteil, automatische Rollback-Entscheidung via Prometheus. Expand/Contract: Zero-Downtime trotz Schema-Änderungen.

**Negativ:** Blue/Green: doppelte Infrastruktur-Kosten während Deployment. Canary mit Argo Rollouts: zusätzliche Infrastruktur-Komplexität. Expand/Contract: drei Deployments statt einem für Breaking Schema Changes.

---

## 💡 Guru-Tipps

- **Smoke Tests immer**: kein Traffic-Switch ohne Smoke-Test der neuen Version.
- **Rollback-Playbook**: dokumentieren wie rollback ausgeführt wird — im Incident kein Zeit zum Nachdenken.
- **Canary nicht zu klein starten**: 1% ist für Fehlerrate-Messung oft zu wenig Daten — 5% ist Minimum.
- **Session Sticky**: bei Blue/Green sicherstellen dass User-Sessions nicht zwischen Blue und Green wechseln.

---

## Verwandte ADRs

- [ADR-036](ADR-036-devops-cicd.md) — CI/CD Pipeline als Basis.
- [ADR-038](ADR-038-kubernetes.md) — Kubernetes-Deployment als Grundlage.
- [ADR-039](ADR-039-feature-flags.md) — Feature Flags ergänzen Canary Releases.
- [ADR-034](ADR-034-db-migrations-flyway.md) — Expand/Contract für DB-Migrationen.
