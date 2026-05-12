# ADR-100 — Helm Charts & Kustomize: Kubernetes-Konfiguration ohne Copy-Paste

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Head of Platform                                        |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | DevOps · Kubernetes · Infrastruktur                           |
| Betroffene Teams  | Platform-Team, alle Entwicklungsteams                         |
| Abhängigkeiten    | ADR-038 (Kubernetes), ADR-060 (IaC), ADR-085 (Conway/Platform)|

---

## 1. Kontext & Treiber

### 1.1 Das Problem mit rohen Kubernetes-Manifesten

```
SITUATION: 3 Umgebungen (dev, staging, production), 8 Services

Ohne Templating:
  k8s/
    dev/
      order-service/deployment.yaml      ← identisch bis auf image-Tag
      order-service/service.yaml         ← identisch
      order-service/configmap.yaml       ← fast identisch
    staging/
      order-service/deployment.yaml      ← Copy-Paste von dev, Replicas: 2
      order-service/service.yaml         ← identisch zu dev
      order-service/configmap.yaml       ← fast identisch
    production/
      order-service/deployment.yaml      ← Copy-Paste von staging, Replicas: 5
      order-service/service.yaml         ← identisch zu staging
      order-service/configmap.yaml       ← fast identisch

RESULTAT:
  24 YAML-Dateien für 8 Services × 3 Umgebungen
  Security-Fix in deployment.yaml? → 24 Dateien manuell ändern
  → Wer vergisst 3 davon → Security-Lücke in Produktion
  → Conway's Law: Teams die Kubernetes-Manifeste manuell pflegen
    verbringen 20-30% der Zeit damit statt Features zu liefern
```

### 1.2 Zwei Ansätze: Helm und Kustomize

```
HELM (Templating + Paket-Manager):
  → Go-Templates in YAML + values.yaml
  → Komplexe Logik (if/else, loops, named templates)
  → Versionierte Pakete (Charts) mit Lifecycle
  → Chart-Repository: externe Charts wiederverwendbar (nginx, postgresql)
  → Gut für: wiederverwendbare Pakete, externe Abhängigkeiten

KUSTOMIZE (Overlay-System, in kubectl eingebaut):
  → Kein Templating — nur Patches/Overlays
  → Base-Manifest + Environment-spezifische Overrides
  → Kein neues Tooling (in kubectl 1.14+ eingebaut)
  → Gut für: eigene Anwendungen, klar strukturierte Umgebungsunterschiede

UNSERE ENTSCHEIDUNG:
  → Kustomize: für eigene Services (dev/staging/prod Overlays)
  → Helm: für externe Abhängigkeiten (PostgreSQL, Kafka, Prometheus)
  → Kein entweder/oder — beide haben ihre Stärken
```

---

## 2. Entscheidung

Eigene Service-Deployments verwenden **Kustomize** mit Base-Manifesten und Umgebungs-Overlays. Externe Abhängigkeiten (Datenbanken, Messaging, Monitoring) werden als **Helm-Charts** installiert. ArgoCD (→ ADR-092) übernimmt GitOps-basiertes Deployment beider Arten.

---

## 3. Kustomize: Eigene Services

### 3.1 Projektstruktur

```
k8s/
├── base/                               ← Gemeinsame Basis (alle Environments)
│   ├── order-service/
│   │   ├── kustomization.yaml
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   ├── serviceaccount.yaml
│   │   └── networkpolicy.yaml
│   └── inventory-service/
│       └── ...
│
└── overlays/                           ← Umgebungs-spezifische Overrides
    ├── dev/
    │   ├── kustomization.yaml
    │   └── patches/
    │       ├── order-service-replicas.yaml   ← 1 Replika
    │       └── order-service-resources.yaml  ← kleine Ressourcen
    ├── staging/
    │   ├── kustomization.yaml
    │   └── patches/
    │       ├── order-service-replicas.yaml   ← 2 Replikas
    │       └── order-service-hpa.yaml        ← HPA aktiv
    └── production/
        ├── kustomization.yaml
        └── patches/
            ├── order-service-replicas.yaml   ← 5 Replikas
            ├── order-service-resources.yaml  ← mehr RAM/CPU
            ├── order-service-hpa.yaml        ← aggressiver HPA
            └── order-service-pdb.yaml        ← PodDisruptionBudget
```

### 3.2 Base-Manifeste

```yaml
# k8s/base/order-service/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml
  - service.yaml
  - serviceaccount.yaml
  - networkpolicy.yaml

commonLabels:           # In ALLE Ressourcen eingefügt
  app: order-service
  team: orders-team
  managed-by: kustomize

commonAnnotations:
  contact: orders-team@example.com
```

```yaml
# k8s/base/order-service/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  # Replicas: in Base nicht gesetzt → Overlays definieren das
  selector:
    matchLabels:
      app: order-service
  template:
    spec:
      serviceAccountName: order-service
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 2000
      containers:
        - name: order-service
          # Image ohne Tag! → Overlays setzen das
          image: ghcr.io/example/order-service
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: kubernetes
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 15
          resources: {}  # Overlays setzen das
```

### 3.3 Production-Overlay

```yaml
# k8s/overlays/production/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
  - ../../base/order-service

# Image-Tag aus CI setzen (GitOps: Tag im Git-Repository committen)
images:
  - name: ghcr.io/example/order-service
    newTag: sha-abc1234f  # Wird von CI aktualisiert

# Namespace
namespace: production

patches:
  - path: patches/replicas.yaml
  - path: patches/resources.yaml
  - path: patches/hpa.yaml
  - path: patches/pdb.yaml

# Production-spezifische Secrets (External Secrets Operator)
resources:
  - externalsecret-db.yaml
  - externalsecret-stripe.yaml
```

```yaml
# k8s/overlays/production/patches/resources.yaml
# Patch: Ressourcen für Production
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  template:
    spec:
      containers:
        - name: order-service
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
```

```yaml
# k8s/overlays/production/patches/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

```yaml
# k8s/overlays/production/patches/pdb.yaml
# PodDisruptionBudget: mindestens 2 Pods immer verfügbar
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: order-service-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: order-service
```

### 3.4 Kustomize deployen

```bash
# Preview was deployed wird:
kubectl kustomize k8s/overlays/production

# Deployen:
kubectl apply -k k8s/overlays/production

# Diff: was ändert sich?
kubectl diff -k k8s/overlays/production
```

---

## 4. Helm: Externe Abhängigkeiten

### 4.1 PostgreSQL via Helm

```bash
# Helm-Repository hinzufügen
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# PostgreSQL für Produktion installieren
helm install order-db bitnami/postgresql \
  --namespace production \
  --version "13.2.0" \        # Immer eine fixe Version!
  --values helm/values/postgresql-production.yaml
```

```yaml
# helm/values/postgresql-production.yaml
auth:
  postgresPassword: ""          # Aus Vault via ExternalSecretsOperator
  username: orderservice
  password: ""                  # Aus Vault
  database: orderdb
  existingSecret: order-db-credentials  # K8s Secret (von Vault befüllt)

primary:
  resources:
    requests:
      memory: 512Mi
      cpu: 250m
    limits:
      memory: 2Gi
      cpu: 2000m

  persistence:
    size: 50Gi
    storageClass: ssd

  extendedConfiguration: |
    max_connections = 200
    shared_buffers = 512MB
    effective_cache_size = 1536MB

backup:
  enabled: true
  cronjob:
    schedule: "0 2 * * *"  # 2 Uhr täglich

metrics:
  enabled: true              # PostgreSQL Exporter für Prometheus
```

### 4.2 Kafka via Helm

```yaml
# helm/values/kafka-production.yaml
replicaCount: 3

kraft:
  enabled: true             # KRaft: kein Zookeeper nötig

persistence:
  enabled: true
  size: 100Gi

resources:
  requests:
    memory: 2Gi
    cpu: 1000m
  limits:
    memory: 4Gi
    cpu: 2000m

metrics:
  kafka:
    enabled: true           # JMX Exporter
  jmx:
    enabled: true

provisioning:
  enabled: true
  topics:
    - name: orders.placed
      partitions: 12
      replicationFactor: 3
      config:
        retention.ms: "604800000"  # 7 Tage
    - name: orders.placed.DLT
      partitions: 12
      replicationFactor: 3
```

---

## 5. Helm vs. Kustomize: Wann was

```
KUSTOMIZE VERWENDEN wenn:
  ✓ Eigene Anwendungen (Source im eigenen Repository)
  ✓ Klare Umgebungsunterschiede (dev/staging/prod)
  ✓ Kein komplexe Logik nötig
  ✓ Kein externes Chart-Sharing nötig
  ✓ Team kennt Kubernetes, wenig Helm-Erfahrung

HELM VERWENDEN wenn:
  ✓ Externe Abhängigkeiten (PostgreSQL, Kafka, Prometheus)
  ✓ Chart wird mit anderen Teams/Firmen geteilt
  ✓ Komplexe Parametrisierung mit if/else-Logik
  ✓ Versioned Release-Lifecycle (helm upgrade, helm rollback)
  ✓ Template-Wiederverwendung über Services hinweg (Company-Chart)

KOMBINATION (empfohlen):
  Kustomize (eigene Services) + Helm (externe Services)
  GitOps via ArgoCD: beide Typen werden aus Git deployed
```

---

## 6. Unternehmens-Helm-Chart für Standard-Services

```yaml
# charts/java-spring-service/Chart.yaml
# Eigenes Unternehmens-Chart für alle Java-Spring-Services
apiVersion: v2
name: java-spring-service
description: Standard Chart für Java 21 / Spring Boot 3.x Services
version: "3.2.0"
appVersion: "latest"
```

```yaml
# charts/java-spring-service/values.yaml
# Defaults die alle Services übernehmen
image:
  repository: ""
  tag: "latest"
  pullPolicy: IfNotPresent

replicaCount: 2

resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"

springProfile: kubernetes

healthcheck:
  readiness: /actuator/health/readiness
  liveness:  /actuator/health/liveness

observability:
  prometheus: true      # Prometheus-Scraping aktiviert
  javaagent:  true      # OpenTelemetry Java Agent

security:
  runAsNonRoot: true
  runAsUser: 1000
  readOnlyRootFilesystem: true
```

```yaml
# Verwendung in einem Service:
# order-service/helm/values-production.yaml
image:
  repository: ghcr.io/example/order-service
  tag: "sha-abc1234"

replicaCount: 5

resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"

springProfile: "kubernetes,production"

env:
  - name: SPRING_DATASOURCE_URL
    value: jdbc:postgresql://order-db:5432/orderdb
```

---

## 7. GitOps-Integration: Kustomize + Helm via ArgoCD

```yaml
# ArgoCD Application für eigenen Service (Kustomize)
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: order-service-production
spec:
  source:
    repoURL: https://github.com/example/k8s-config
    targetRevision: main
    path: k8s/overlays/production
    # Kustomize wird automatisch erkannt

  destination:
    server: https://kubernetes.default.svc
    namespace: production

  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

```yaml
# ArgoCD Application für externe Abhängigkeit (Helm)
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: kafka-production
spec:
  source:
    repoURL: https://charts.bitnami.com/bitnami
    chart: kafka
    targetRevision: "26.8.0"  # Fixe Version!
    helm:
      valueFiles:
        - helm/values/kafka-production.yaml
      releaseName: kafka

  destination:
    server: https://kubernetes.default.svc
    namespace: production
```

---

## 8. Akzeptanzkriterien

- [ ] Kein rohes `kubectl apply -f` in CI/CD — nur `kubectl apply -k` oder Helm
- [ ] Alle Umgebungen haben eigene Overlays (dev/staging/production)
- [ ] Image-Tags in kustomization.yaml werden automatisch von CI aktualisiert
- [ ] Externe Abhängigkeiten (PostgreSQL, Kafka) über Helm mit fixen Chart-Versionen
- [ ] `kubectl diff -k` läuft vor jedem Deployment in CI (kein blindes Apply)
- [ ] Platform-Team hat Unternehmens-Helm-Chart für Standard-Services

---

## Quellen & Referenzen

- **Kubernetes Documentation, "Kustomize"** — offizielle Referenz.
- **Helm Documentation, "Helm Charts"** — offizielle Chart-Referenz.
- **Weaveworks, "GitOps" (2017)** — Git als Single Source of Truth für Kubernetes.
- **Bilgin Ibryam & Roland Huß, "Kubernetes Patterns" (2019)** — Configuration Resource Pattern.
- **Matthew Skelton & Manuel Pais, "Team Topologies" (2019)** — Platform-Team als Enabler für Self-Service.

---

## Verwandte ADRs

- [ADR-038](ADR-038-kubernetes.md) — Kubernetes-Grundlagen
- [ADR-059](ADR-059-blue-green-canary.md) — Deployment-Strategien mit Kustomize
- [ADR-060](ADR-060-infrastructure-as-code.md) — IaC-Prinzipien
- [ADR-092](ADR-092-isaqb-cloud-native.md) — GitOps mit ArgoCD
- [ADR-101](ADR-101-spring-security-oauth2.md) — Spring Security (nächster ADR)
