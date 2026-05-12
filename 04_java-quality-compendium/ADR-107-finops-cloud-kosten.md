# ADR-107 — FinOps: Cloud-Kosten verstehen, messen und optimieren

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Head of Engineering / Controlling                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01 (quartalsweise Cost Review!)                       |
| Kategorie         | FinOps · Cloud-Kosten · Kubernetes · Infrastruktur            |
| Betroffene Teams  | Alle Teams, Platform-Team, Finance                            |
| Abhängigkeiten    | ADR-038 (K8s), ADR-060 (IaC), ADR-092 (Cloud-Native)         |

---

## 1. Kontext & Treiber

### 1.1 Das Cloud-Kosten-Problem

```
TYPISCHE SITUATION nach 2 Jahren Cloud-Betrieb:

Monat 1: 2.000 EUR/Monat (MVP, 3 Services)
Monat 12: 8.000 EUR/Monat (10 Services, Team wächst)
Monat 24: 28.000 EUR/Monat (20 Services)

CFO fragt: "Warum 28.000 EUR? Was genau kostet wieviel?"
Antwort: "Wir... wissen es nicht genau."

HÄUFIGSTE KOSTENTREIBER (die niemand sieht):
  ① Überprovisionierte VMs: Requests = 4 CPU, Usage = 0.2 CPU
  ② Nicht abgebaute Test-Umgebungen: läuft seit 6 Monaten ohne Nutzung
  ③ Ungenutzte Snapshots/Disks: niemand hat sie gelöscht
  ④ Unoptimierte Datentransfer-Kosten: egress zwischen Regionen
  ⑤ Managed Services Oversizing: RDS r5.4xlarge für 10 RPS
  ⑥ Keine Spot-Instances für Batch-Jobs
```

### 1.2 FinOps: Cloud Financial Operations

```
FINOPS = Disziplin die Cloud-Finanzmanagement, Engineering und Business verbindet.

FinOps Foundation Prinzipien:
  ① SICHTBARKEIT: jeder sieht seine Kosten
  ② ACCOUNTABILITY: Teams verantworten ihre Kosten
  ③ OPTIMIERUNG: kontinuierliche Verbesserung
  ④ KOLLABORATION: Engineering + Finance + Business gemeinsam

NICHT: Finance erklärt Engineering was zu tun ist.
NICHT: Engineering ignoriert Kosten.
SONDERN: Teams treffen bewusste Kosten/Nutzen-Entscheidungen.
```

---

## 2. Entscheidung

Alle Cloud-Ressourcen werden mit Team-Tags versehen (Cost Attribution). Monatliche Cost-Review-Meetings pro Team. Kubernetes-Ressourcen werden durch VPA (Vertical Pod Autoscaler) und Karpenter kontinuierlich optimiert. Entwicklungs-Umgebungen werden automatisch außerhalb der Geschäftszeiten skaliert.

---

## 3. Kosten sichtbar machen: Tagging-Strategie

### 3.1 Pflicht-Tags für alle Cloud-Ressourcen

```hcl
# Terraform: Pflicht-Tags auf ALLEN Ressourcen (→ ADR-060)
locals {
  required_tags = {
    # PFLICHT — ohne diese Tags: Ressource wird in Audit gefunden und gemahnt
    team        = var.team_name        # "orders-team"
    service     = var.service_name     # "order-service"
    environment = var.environment      # "production", "staging", "dev"
    cost-center = var.cost_center      # "engineering-backend"
    managed-by  = "terraform"

    # Für Kostenzuordnung zu Geschäftsbereichen:
    product     = var.product          # "e-commerce", "analytics"
    owner       = var.team_email       # "orders-team@example.com"
  }
}

# Auf jeder Ressource:
resource "google_compute_instance" "order_service" {
  labels = local.required_tags
}

resource "google_sql_database_instance" "order_db" {
  labels = local.required_tags
}
```

```yaml
# Kubernetes: Labels auf allen Pods (→ ADR-100 Kustomize)
# k8s/base/order-service/deployment.yaml
spec:
  template:
    metadata:
      labels:
        app: order-service
        team: orders-team              # Für Cost Attribution
        cost-center: engineering       # Für Billing
        environment: production
```

### 3.2 Kosten-Dashboard

```yaml
# Grafana-Dashboard: Kosten pro Team (aus Cloud-Billing API)
# Oder: Cloud-nativer Cost Explorer (AWS), Cloud Billing (GCP), Cost Management (Azure)

# Kubecost: Kubernetes-spezifische Kosten per Pod/Namespace/Label
# Deployment via Helm:
helm install kubecost kubecost/cost-analyzer \
  --namespace kubecost \
  --set kubecostToken=$KUBECOST_TOKEN \
  --set global.grafana.enabled=true

# Kubecost zeigt:
# - Kosten pro Namespace
# - Kosten pro Deployment
# - Idle-Kosten (überprovisionierte Ressourcen)
# - Efficiency Score (Utilization vs. Request)
```

---

## 4. Kubernetes-Ressourcen optimieren

### 4.1 Das Über-Provisionierungs-Problem

```yaml
# ❌ HÄUFIGER FEHLER: Requests zu großzügig gesetzt
containers:
  - name: order-service
    resources:
      requests:
        cpu: "2000m"      # 2 CPUs requested
        memory: "2Gi"     # 2 GB requested
      limits:
        cpu: "4000m"
        memory: "4Gi"

# TATSÄCHLICHE NUTZUNG (Prometheus):
# CPU: 150m (7.5% von requested!)
# Memory: 400Mi (20% von requested!)
# 
# Kosten: bezahlt für 2 CPUs, nutzt 0.15 CPUs
# Verschwendung: 92.5% der CPU-Kosten!
```

```yaml
# ✅ RICHTIG: Vertical Pod Autoscaler (VPA) für automatische Empfehlungen
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: order-service-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  updatePolicy:
    updateMode: "Off"    # Nur Empfehlungen, kein Auto-Update (erst manuell prüfen!)
    # updateMode: "Auto" → VPA ändert Requests automatisch (in Prod vorsichtig!)

# Nach einigen Tagen: VPA-Empfehlung lesen:
# kubectl describe vpa order-service-vpa
# 
# Output:
# Container Recommendations:
#   Container Name: order-service
#   Lower Bound:
#     cpu:    85m
#     memory: 280Mi
#   Target:
#     cpu:    150m       ← statt 2000m!
#     memory: 420Mi      ← statt 2048Mi!
#   Upper Bound:
#     cpu:    300m
#     memory: 600Mi
```

### 4.2 Karpenter: Node-Kosten optimieren

```yaml
# Karpenter (AWS EKS): intelligentes Node-Management
# → Provisioned nur was benötigt wird
# → Spot-Instances für nicht-kritische Workloads

apiVersion: karpenter.sh/v1beta1
kind: NodePool
metadata:
  name: default
spec:
  template:
    spec:
      requirements:
        # Spot zuerst (80% günstiger als On-Demand!)
        - key: karpenter.sh/capacity-type
          operator: In
          values: ["spot", "on-demand"]   # Spot bevorzugt

        - key: kubernetes.io/arch
          operator: In
          values: ["amd64", "arm64"]

  disruption:
    consolidationPolicy: WhenUnderutilized   # Idle Nodes entfernen
    consolidateAfter: 30s

---
# Workloads: kritisch auf On-Demand, Batch auf Spot
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  template:
    spec:
      nodeSelector:
        karpenter.sh/capacity-type: on-demand  # KRITISCH → On-Demand

---
apiVersion: batch/v1
kind: Job
metadata:
  name: monthly-billing-job
spec:
  template:
    spec:
      nodeSelector:
        karpenter.sh/capacity-type: spot       # BATCH → Spot (80% günstiger)
      tolerations:
        - key: "karpenter.sh/capacity-type"
          value: "spot"
          effect: NoSchedule
```

### 4.3 Entwicklungsumgebungen: Automatisches Herunterfahren

```yaml
# Dev- und Staging-Umgebungen außerhalb Geschäftszeiten skalieren
# Einsparung: 65% (von 24h auf 8h pro Werktag)

# CronJob: Skaliert alle Dev-Deployments auf 0 um 20 Uhr
apiVersion: batch/v1
kind: CronJob
metadata:
  name: scale-down-dev
spec:
  schedule: "0 20 * * MON-FRI"   # 20 Uhr Werktags
  jobTemplate:
    spec:
      template:
        spec:
          serviceAccountName: scaler
          containers:
            - name: kubectl
              image: bitnami/kubectl:latest
              command:
                - /bin/sh
                - -c
                - |
                  # Alle Deployments im dev-Namespace auf 0 skalieren
                  kubectl scale deployments --all --replicas=0 -n dev
                  kubectl scale deployments --all --replicas=0 -n staging-dev
                  echo "Dev-Umgebungen heruntergefahren um $(date)"
---
# CronJob: Hochfahren 8 Uhr morgens
apiVersion: batch/v1
kind: CronJob
metadata:
  name: scale-up-dev
spec:
  schedule: "0 8 * * MON-FRI"    # 8 Uhr Werktags
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: kubectl
              image: bitnami/kubectl:latest
              command:
                - /bin/sh
                - -c
                - |
                  kubectl scale deployments --all --replicas=1 -n dev
                  echo "Dev-Umgebungen hochgefahren um $(date)"
```

---

## 5. Datenbank-Kosten optimieren

```
HÄUFIGE DB-Kostenfallen:

① Ständig laufende Dev-DBs:
   Lösung: RDS stoppen wenn nicht genutzt (AWS: Stop/Start, kostet 0 wenn gestoppt)

② Oversized Instances:
   Lösung: CloudWatch CPU-Monitoring → Downsize wenn < 20% Auslastung
   RDS db.r5.xlarge (0,48 USD/h) → db.r5.large (0,24 USD/h) = -50%

③ Multi-AZ für Dev (nicht nötig):
   Lösung: Multi-AZ nur für Production
   Dev: Single-AZ (50% günstiger)

④ Übermäßige Snapshots:
   Lösung: Retention Policy (7 Tage für Dev, 30 Tage für Prod)
   Automatische Löschung alter Snapshots via Terraform Lifecycle

⑤ Ungenutzte Read Replicas:
   Lösung: Read Replicas nur wenn tatsächlich genutzt + überwacht
```

```hcl
# Terraform: Kosten-optimierte RDS-Konfiguration
resource "aws_db_instance" "order_db" {
  identifier = "order-db-${var.environment}"

  # Production: Multi-AZ, große Instance
  multi_az            = var.environment == "production"
  instance_class      = var.environment == "production" ? "db.r5.xlarge" : "db.t3.medium"
  allocated_storage   = var.environment == "production" ? 100 : 20

  # Dev: automatisch stoppen wenn nicht in Verwendung
  # (AWS stoppt RDS nach 7 Tagen Inaktivität automatisch)

  # Backup: kurze Retention für Dev
  backup_retention_period = var.environment == "production" ? 7 : 1

  tags = local.required_tags
}
```

---

## 6. Cost Review: Monatlicher Prozess

```markdown
## Monatlicher Cost Review — Template

**Datum:** [MONAT YYYY]
**Teilnehmer:** Team Leads, CTO, Finance

### Budget vs. Ist
| Team | Budget | Ist | Abweichung |
|---|---|---|---|
| Orders-Team | 1.500 EUR | 1.820 EUR | +21% ❌ |
| Platform | 3.000 EUR | 2.850 EUR | -5% ✅ |

### Top 3 Kostentreiber diesen Monat
1. Orders-Team: RDS Multi-AZ auch in Staging (+320 EUR) → Aktion: deaktivieren
2. Platform: Kafka Cluster Upsize → erwartet (+200 EUR)
3. Analytics: Ungenutzte EC2-Instances seit 3 Monaten (+150 EUR) → terminieren

### Effizienz-Metriken (Kubecost)
| Service | CPU Utilization | Memory Utilization | Effizienz-Score |
|---|---|---|---|
| order-service | 35% | 62% | 48% ⚠️ → VPA-Empfehlung umsetzen |
| inventory-service | 78% | 71% | 75% ✅ |

### Aktionen nächsten Monat
- [ ] VPA-Empfehlungen für order-service umsetzen: -200 EUR/Monat erwartet
- [ ] Analytics EC2 terminieren: -150 EUR/Monat
- [ ] Staging RDS auf Single-AZ: -120 EUR/Monat

**Erwartete Einsparung:** 470 EUR/Monat = 5.640 EUR/Jahr
```

---

## 7. Cost-per-Feature: Engineering Economics

```
FORTGESCHRITTENES FINOPS: Kosten pro Business-Metric messen

Beispiel: "Was kostet eine Bestellung zu verarbeiten?"

BERECHNUNG:
  Gesamtkosten Order-Service: 1.820 EUR/Monat
  Bestellungen/Monat: 45.000
  → Kosten pro Bestellung: 0,04 EUR (4 Cent)

NUTZEN: 
  → Entscheidungsbasis für Optimierungen
  → "Wenn wir Latenz halbieren, kostet das X EUR mehr — lohnt es sich?"
  → Jira-Ticket "Performance-Optimierung" kann jetzt ROI berechnet werden

IMPLEMENTIERUNG in Grafana:
  Panel: "Cost per Order"
  Formel: monthly_infrastructure_cost / monthly_order_count
```

---

## 8. Akzeptanzkriterien

- [ ] Alle Cloud-Ressourcen haben Pflicht-Tags (Terraform tfsec-Check)
- [ ] Kubecost deployed und zeigt Kosten pro Team/Namespace
- [ ] Dev-Umgebungen fahren außerhalb Geschäftszeiten automatisch herunter
- [ ] Monatlicher Cost Review im Kalender aller Team Leads
- [ ] VPA deployed für alle Produktions-Services (im Empfehlungs-Modus)
- [ ] Budget-Alerts: bei > 20% Überschreitung → E-Mail an Team Lead + CTO

---

## Quellen & Referenzen

- **FinOps Foundation, "Cloud Financial Management Framework" (2023)** — offizielle FinOps-Methodik.
- **Corey Quinn, "The Duckbill Group Cloud Costs"** — Praxisorientierte Cloud-Optimierung.
- **Kubernetes Documentation, "Resource Management for Pods and Containers"** — Requests, Limits, VPA.
- **AWS Well-Architected Framework, "Cost Optimization Pillar"** — AWS-spezifische Best Practices.

---

## Verwandte ADRs

- [ADR-038](ADR-038-kubernetes.md) — Kubernetes-Ressourcen-Konfiguration
- [ADR-060](ADR-060-infrastructure-as-code.md) — Terraform mit Kosten-Tags
- [ADR-100](ADR-100-helm-kustomize.md) — Kustomize für Umgebungs-Ressourcen
