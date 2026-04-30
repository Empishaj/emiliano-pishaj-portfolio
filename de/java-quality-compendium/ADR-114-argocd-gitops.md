# ADR-114 — ArgoCD & GitOps: Git als Single Source of Truth für Kubernetes

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Head of Platform                                        |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | DevOps · GitOps · Kubernetes · Deployment                     |
| Betroffene Teams  | Platform-Team, alle Engineering-Teams                         |
| Abhängigkeiten    | ADR-038 (K8s), ADR-060 (IaC), ADR-100 (Helm/Kustomize)       |

---

## 1. Das Problem mit imperativen Deployments

```
IMPERATIV (ohne GitOps):
  kubectl apply -f deployment.yaml   ← manuell, im Terminal
  helm upgrade myapp ./chart          ← CI/CD-Script, einmalig

PROBLEME:
  ① Wer hat wann was deployed? → nur in CI-Logs, schwer nachverfolgbar
  ② Manuelle Cluster-Änderungen (kubectl patch, kubectl scale) → Git kennt sie nicht
  ③ Cluster-Zustand und Git weichen ab (Drift) → niemand merkt es
  ④ Disaster Recovery: Cluster neu aufbauen = alle Deployments neu ausführen
  ⑤ Rollback: welcher Commit war das letzte gute Deployment?

GITOPS (deklarativ):
  Git enthält gewünschten Cluster-Zustand
  ArgoCD gleicht Cluster kontinuierlich mit Git ab
  
  Wer was deployed: git log (vollständig, auditierbar)
  Manuelle Änderungen: ArgoCD überschreibt sie automatisch
  Cluster-Drift: ArgoCD meldet und korrigiert es
  Disaster Recovery: neuer Cluster + ArgoCD = identischer Zustand
  Rollback: git revert → ArgoCD deployed vorherigen Stand
```

---

## 2. Entscheidung

ArgoCD ist der GitOps-Controller. Alle Kubernetes-Ressourcen werden ausschließlich
über Git-PRs geändert — nie per `kubectl` direkt in Produktion. ArgoCD
überwacht Git und gleicht den Cluster-Zustand automatisch ab.

---

## 3. Repository-Struktur

```
k8s-config/                        ← Separates GitOps-Repository
├── apps/                          ← ArgoCD Application-Definitionen
│   ├── staging/
│   │   ├── order-service.yaml
│   │   └── payments-service.yaml
│   └── production/
│       ├── order-service.yaml
│       └── payments-service.yaml
│
├── services/                      ← Kustomize-Manifeste (→ ADR-100)
│   ├── order-service/
│   │   ├── base/
│   │   └── overlays/
│   │       ├── staging/
│   │       └── production/
│   └── payments-service/
│       └── ...
│
└── platform/                      ← Cluster-weite Infrastruktur
    ├── cert-manager/
    ├── istio/
    └── monitoring/
```

---

## 4. ArgoCD Application Definition

```yaml
# apps/production/order-service.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: order-service-production
  namespace: argocd
  annotations:
    # Wer hat diese Application erstellt?
    app.kubernetes.io/managed-by: "orders-team"
spec:
  project: production   # ArgoCD Project für Zugriffssteuerung

  source:
    repoURL:        https://github.com/example/k8s-config.git
    targetRevision: main            # Branch
    path:           services/order-service/overlays/production

  destination:
    server:    https://kubernetes.default.svc  # Lokaler Cluster
    namespace: production

  syncPolicy:
    automated:
      prune:     true    # Ressourcen die in Git gelöscht wurden, aus Cluster entfernen
      selfHeal:  true    # Manuelle Cluster-Änderungen überschreiben (Drift-Korrektur)
      allowEmpty: false  # Nie auf leeres Verzeichnis synken (Sicherheit!)

    syncOptions:
      - CreateNamespace=true
      - PrunePropagationPolicy=foreground
      - RespectIgnoreDifferences=true

    # Retry bei transientem Fehler
    retry:
      limit: 3
      backoff:
        duration:    5s
        factor:      2
        maxDuration: 3m

  # Was ArgoCD bei Vergleich ignorieren soll (automatisch gesetzte Felder)
  ignoreDifferences:
    - group: apps
      kind:  Deployment
      jsonPointers:
        - /spec/replicas   # HPA ändert replicas → soll nicht als Drift gelten
```

---

## 5. CI/CD-Integration: Image-Tag aktualisieren

```yaml
# .gitlab-ci.yml: nach Build/Push → k8s-config-Repo aktualisieren

deploy:update-image-tag:
  stage: deploy
  image: alpine/git:latest
  before_script:
    - git config --global user.email "ci@example.com"
    - git config --global user.name "GitLab CI"
    - git clone https://oauth2:${GITOPS_TOKEN}@github.com/example/k8s-config.git
    - cd k8s-config
  script:
    # kustomize edit: Image-Tag in der richtigen Overlay-Datei aktualisieren
    - |
      cd services/order-service/overlays/staging
      kustomize edit set image \
        ghcr.io/example/order-service=ghcr.io/example/order-service:$CI_COMMIT_SHA

    # Commit und Push → ArgoCD erkennt Änderung und deployed
    - |
      git add .
      git commit -m "ci: update order-service to $CI_COMMIT_SHORT_SHA

      Pipeline: $CI_PIPELINE_URL
      Build: $CI_JOB_URL"
    - git push origin main

  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
```

---

## 6. ArgoCD Projects: Zugriffssteuerung

```yaml
# ArgoCD Project: begrenzt welche Teams welche Cluster/Namespaces deployen dürfen
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: production
  namespace: argocd
spec:
  description: "Production-Deployments — nur autorisierte Teams"

  # Erlaubte Source-Repositories
  sourceRepos:
    - https://github.com/example/k8s-config.git

  # Erlaubte Ziel-Cluster und Namespaces
  destinations:
    - server:    https://kubernetes.default.svc
      namespace: production
    - server:    https://kubernetes.default.svc
      namespace: production-*   # Production-Subnamespaces

  # Verbotene Cluster-Ressourcen (Sicherheit!)
  clusterResourceBlacklist:
    - group: ""
      kind:  Namespace    # Namespaces dürfen nicht über Apps erstellt werden
    - group: rbac.authorization.k8s.io
      kind:  ClusterRole  # Keine Cluster-Rollen über App-Sync

  # Sync-Windows: Deployments nur zu bestimmten Zeiten
  syncWindows:
    - kind:      allow
      schedule:  "0 8 * * MON-FRI"   # Mo-Fr 8 Uhr bis 20 Uhr
      duration:  12h
      applications: ["*"]
    - kind:      deny
      schedule:  "0 20 * * *"        # Kein Deploy nach 20 Uhr
      duration:  12h
      manualSync: false              # Auch manuelle Syncs blockieren
```

---

## 7. Multi-Cluster mit ArgoCD

```yaml
# App-of-Apps Pattern: eine Application die alle anderen managed
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: app-of-apps-staging
  namespace: argocd
spec:
  source:
    repoURL: https://github.com/example/k8s-config.git
    path: apps/staging    # Verzeichnis mit allen Application-Definitionen
    targetRevision: main

  destination:
    server: https://kubernetes.default.svc
    namespace: argocd

  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

---

## 8. Rollback via Git

```bash
# Rollback ist ein git revert — kein kubectl rollout undo!

# 1. Letzten guten Commit finden
git log --oneline k8s-config/services/order-service/overlays/production/

# 2. Revert erstellen
git revert abc1234 --no-edit

# 3. Push → ArgoCD deployed automatisch vorherigen Stand
git push origin main

# ArgoCD zeigt im UI: "Syncing" → "Synced"
# Rollback vollständig auditierbar in Git-History
```

---

## 9. Monitoring und Alerting

```yaml
# Prometheus-Alert: Application out of sync
- alert: ArgoCDAppOutOfSync
  expr: argocd_app_info{sync_status="OutOfSync"} == 1
  for: 10m
  annotations:
    summary: "ArgoCD: {{ $labels.name }} ist out of sync"
    description: "Cluster-Zustand weicht von Git ab seit 10 Minuten"

- alert: ArgoCDAppDegraded
  expr: argocd_app_info{health_status="Degraded"} == 1
  for: 5m
  annotations:
    summary: "ArgoCD: {{ $labels.name }} ist degraded"
```

---

## 10. Akzeptanzkriterien

- [ ] Kein direktes `kubectl apply` oder `helm upgrade` in Produktion (Policy enforced)
- [ ] Alle Services haben ArgoCD Application-Definitionen
- [ ] `selfHeal: true` aktiviert: manuelle Cluster-Änderungen werden innerhalb 3 Minuten korrigiert
- [ ] Rollback via `git revert` dauert < 5 Minuten end-to-end
- [ ] Sync-Windows: kein Deployment nach 20 Uhr (configurable)
- [ ] ArgoCD-Dashboard zeigt alle Services als "Synced + Healthy"

---

## Quellen & Referenzen

- **Weaveworks, "GitOps: Operations by Pull Request" (2017)** — Ursprung des GitOps-Begriffs.
- **ArgoCD Documentation** — vollständige Referenz. argo-cd.readthedocs.io
- **Christian Hernandez, "GitOps and Kubernetes" (2021), Manning** — ArgoCD-Muster in der Praxis.

---

## Verwandte ADRs

- [ADR-038](ADR-038-kubernetes.md) — Kubernetes-Grundlagen
- [ADR-100](ADR-100-helm-kustomize.md) — Kustomize-Manifeste die ArgoCD deployt
- [ADR-092](ADR-092-isaqb-cloud-native.md) — GitOps als Cloud-Native-Prinzip
