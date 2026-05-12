# ADR-092 — iSAQB Advanced: Cloud-Native Architecture Patterns

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Architektur-Board / Head of Platform                    |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Advanced · Cloud-Native                                 |
| Betroffene Teams  | Alle Engineering-Teams, Platform-Team                         |
| iSAQB-Lernziel    | Advanced Level · Cloud-Native Patterns                        |

---

## 1. Was Cloud-Native wirklich bedeutet

```
CLOUD-NATIVE ≠ "läuft in der Cloud"
CLOUD-NATIVE = "nutzt Cloud-Eigenschaften: Elastizität, Managed Services, 
                Automatisierung, Resilienz durch Infrastruktur"

CNCF-Definition (Cloud Native Computing Foundation):
  "Cloud native technologies empower organizations to build and run scalable
   applications in modern, dynamic environments such as public, private, and
   hybrid clouds. Containers, service meshes, microservices, immutable
   infrastructure, and declarative APIs exemplify this approach."

DIE FÜNF SÄULEN (12-Factor App + Erweiterungen):
  ① Containerisierung: portable, reproduzierbare Deployment-Einheiten
  ② Deklarative Konfiguration: Infrastruktur als Code (→ ADR-060)
  ③ Observability by Design: Logs, Metriken, Traces von Anfang an
  ④ Horizontal Scaling: stateless Services, Scale-Out statt Scale-Up
  ⑤ Resilience Patterns: Circuit Breaker, Retry, Graceful Degradation
```

---

## 2. The Twelve-Factor App (Heroku, 2012)

```
Die 12 Faktoren sind die Grundlage aller Cloud-Native-Architekturen.
Jeder Faktor vermeidet ein konkretes Problem in verteilten Systemen.

FAKTOR 1: CODEBASE — Eine Codebase, viele Deployments
  Problem: Code-Duplikation zwischen Staging und Production
  Lösung: Ein Git-Repository, verschiedene Konfigurationen (ENV-Vars)
  Konkret: main-Branch → Staging; Tag v2.3.0 → Production
  Verletzung: "Ich mache schnell einen Fix direkt in Production"

FAKTOR 2: DEPENDENCIES — Explizit deklariert und isoliert
  Problem: "Auf meinem Rechner läuft's"
  Lösung: build.gradle.kts mit fixen Versionen, kein System-Install
  Konkret: keine system-weiten mvn oder npm install
  Verletzung: "Ich habe curl manuell auf dem Server installiert"

FAKTOR 3: CONFIG — Konfiguration in der Umgebung
  Problem: Secrets in der Codebasis (git blame = Sicherheitslücke)
  Lösung: ENV-Variablen oder Vault (→ ADR-069)
  Konkret: SPRING_DATASOURCE_URL als ENV, nicht in application.yml
  Verletzung: "DB-Passwort in application-production.yml"

FAKTOR 4: BACKING SERVICES — Als angehängte Ressourcen behandeln
  Problem: Lokale DB kann nicht durch Remote-DB ersetzt werden ohne Code-Änderung
  Lösung: DB, Cache, Queue über URL/ENV konfiguriert, austauschbar
  Konkret: jdbc:postgresql://localhost:5432/db und jdbc:postgresql://rds:5432/db identisch

FAKTOR 5: BUILD, RELEASE, RUN — Strikt getrennte Phasen
  Problem: "Ich patche schnell auf dem Production-Server"
  Lösung: Build-Phase, Release (Build+Config), Run-Phase streng getrennt
  Konkret: Docker-Image = immutables Artefakt; Config per ENV

FAKTOR 6: PROCESSES — Zustandslose Prozesse
  Problem: Session im Memory einer Instanz → Load Balancer bricht Session
  Lösung: Kein Zustand im Prozess; State in externem Store (Redis, DB)
  Konkret: Spring Session mit Redis-Backend statt In-Memory-Session
  Verletzung: "Unsere Warenkörbe sind im App-Server-Speicher"

FAKTOR 7: PORT BINDING — Services über Port-Binding exportieren
  Problem: Service nur in bestimmten App-Servern deploybar
  Lösung: Spring Boot embedded Tomcat — selbstständige HTTP-Bindung
  Konkret: java -jar app.jar → läuft sofort, kein Tomcat-Install

FAKTOR 8: CONCURRENCY — Horizontal durch Process-Model skalieren
  Problem: Vertikale Skalierung (mehr RAM, mehr CPU) begrenzt und teuer
  Lösung: Mehrere zustandslose Instanzen → Kubernetes HPA
  Konkret: CPU > 70% → automatisch neue Pod-Instanz

FAKTOR 9: DISPOSABILITY — Schnell starten, graceful shutdown
  Problem: Pod-Neustart → 30 Sekunden Downtime
  Lösung: GraalVM Native (→ ADR-071) oder optimiertes JVM-Startup
  Konkret: Graceful Shutdown (→ server.shutdown: graceful in Spring Boot)
  Kubernetes: SIGTERM → 30s Graceful Period → SIGKILL

FAKTOR 10: DEV/PROD PARITY — Entwicklung und Produktion ähnlich halten
  Problem: "In Dev: H2, in Prod: PostgreSQL" → Bugs nur in Prod
  Lösung: Testcontainers (→ ADR-018) — echtes PostgreSQL auch in Tests

FAKTOR 11: LOGS — Als Event-Stream behandeln
  Problem: Log-Dateien auf Server → verloren bei Pod-Neustart
  Lösung: stdout → Kubernetes → Loki/ELK Aggregation
  Konkret: kein log.file.name in application.yml; nur stdout

FAKTOR 12: ADMIN PROCESSES — Admin-Tasks als Einmal-Prozesse
  Problem: "Ich rufe direkt die DB-Konsole auf dem Production-Server"
  Lösung: kubectl run --rm -it admin-job → ausführen → automatisch beenden
  Konkret: Flyway-Migration als Init-Container (→ ADR-034)
```

---

## 3. Cloud-Native Deployment-Pattern

### 3.1 Sidecar Pattern

```yaml
# Sidecar: Zusatzfunktionalität ohne Code-Änderung im Haupt-Container
# Typisch: Logging-Aggregation, Security-Proxy, Config-Sync

# Kubernetes Pod mit Sidecar
spec:
  containers:
    # Haupt-Container: Anwendung
    - name: order-service
      image: ghcr.io/example/order-service:sha-abc123
      ports:
        - containerPort: 8080

    # Sidecar 1: Istio-Envoy-Proxy (mTLS, Circuit Breaker, Tracing)
    # → wird automatisch von Istio injiziert, kein Code-Änderung
    - name: istio-proxy
      image: istio/proxyv2:latest

    # Sidecar 2: OpenTelemetry-Collector
    - name: otel-collector
      image: otel/opentelemetry-collector:latest
      args: ["--config=/etc/otel/config.yaml"]
      volumeMounts:
        - name: otel-config
          mountPath: /etc/otel

    # Sidecar 3: Vault-Agent (Secret-Rotation → ADR-069)
    - name: vault-agent
      image: vault:latest
      command: ["vault", "agent", "-config=/vault/config/agent.hcl"]
```

### 3.2 Ambassador Pattern (API-Gateway als Proxy)

```yaml
# Ambassador: ausgehende Netzwerkkommunikation durch Proxy
# Kapselt: Retry, Circuit Breaking, mTLS, Service Discovery

# Statt: OrderService → direkter HTTP-Call zu InventoryService
# Mit Ambassador: OrderService → localhost:8081 → Ambassador → InventoryService

# OrderService sieht nur: http://localhost:8081/inventory
# Ambassador übernimmt: Service Discovery, Retry, Timeout, mTLS

# Kong oder Envoy als Ambassador:
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: order-ambassador
  annotations:
    konghq.com/plugins: rate-limiting, jwt-auth, request-transformer
spec:
  rules:
    - http:
        paths:
          - path: /api/v2/orders
            backend:
              service:
                name: order-service
                port:
                  number: 8080
```

### 3.3 Init Container Pattern

```yaml
# Init Container: Vorbereitungsaufgaben vor dem Haupt-Container
# Klassischer Use-Case: Flyway-Datenbank-Migration

spec:
  initContainers:
    # DB-Migration VOR Start des Services
    - name: db-migrate
      image: ghcr.io/example/order-service:sha-abc123
      command: ["java", "-jar", "app.jar", "--spring.batch.job.enabled=false"]
      env:
        - name: FLYWAY_MIGRATE_ON_START
          value: "true"
        - name: SERVER_PORT
          value: "-1"  # Kein HTTP-Server, nur Migration

    # Warten bis externe Services bereit
    - name: wait-for-kafka
      image: busybox
      command: ['sh', '-c',
        'until nc -z kafka:9092; do echo waiting; sleep 2; done']

  containers:
    - name: order-service
      # Startet ERST wenn alle initContainers erfolgreich
```

---

## 4. Kubernetes-spezifische Patterns

### 4.1 Operator Pattern: Custom Resources

```java
// Kubernetes Operator: benutzerdefinierte Ressourcen und Automatisierung
// Wenn Kubernetes-native Ressourcen nicht ausreichen

// Custom Resource Definition (CRD):
// kubectl apply -f order-service-crd.yaml
// Danach: kubectl get orderservice → wie kubectl get deployment

// CRD-Definition:
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: orderservices.example.com
spec:
  group: example.com
  names:
    kind: OrderService
    plural: orderservices
  versions:
    - name: v1
      schema:
        openAPIV3Schema:
          properties:
            spec:
              properties:
                replicas: {type: integer}
                canaryWeight: {type: integer}  # Canary-Prozentsatz

// Java Operator (Quarkus Operator SDK oder Java Operator SDK):
@ControllerConfiguration
public class OrderServiceOperator
        implements Reconciler<OrderService> {

    @Override
    public UpdateControl<OrderService> reconcile(
            OrderService resource, Context context) {
        // Desired State aus OrderService-Spec
        var desired = resource.getSpec().getReplicas();

        // Actual State aus Kubernetes
        var deployment = kubernetesClient
            .apps().deployments()
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(resource.getMetadata().getName())
            .get();

        if (deployment.getSpec().getReplicas() != desired) {
            // Reconcile: Deployment auf desired-State bringen
            deployment.getSpec().setReplicas(desired);
            kubernetesClient.apps().deployments().replace(deployment);
        }

        return UpdateControl.noUpdate();
    }
}
```

### 4.2 Horizontal Pod Autoscaler

```yaml
# HPA: automatische Skalierung basierend auf Metriken
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 2    # Minimum für Verfügbarkeit (→ ADR-082 QS-V01)
  maxReplicas: 20   # Maximum für Kostenbegrenzung

  metrics:
    # CPU-basierte Skalierung
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70    # Scale wenn CPU > 70%

    # Kafka-Lag-basierte Skalierung (KEDA)
    - type: External
      external:
        metric:
          name: kafka_consumer_lag
          selector:
            matchLabels:
              topic: orders.placed
              consumerGroup: fulfillment
        target:
          type: AverageValue
          averageValue: "1000"      # Scale wenn Lag > 1000 Messages
```

---

## 5. Service Mesh: Istio als Cloud-Native Querschnittskonzept

```yaml
# Istio übernimmt als Infrastruktur-Schicht:
# mTLS, Retry, Circuit Breaking, Tracing, Traffic Management

# VirtualService: Traffic-Splitting für Canary
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service
spec:
  http:
    - match:
        - headers:
            x-canary: {exact: "true"}  # Canary-User
      route:
        - destination:
            host: order-service-v2
            port: {number: 80}
          weight: 100
    - route:               # Standard-Traffic
        - destination:
            host: order-service-v1
            port: {number: 80}
          weight: 95
        - destination:
            host: order-service-v2
            port: {number: 80}
          weight: 5         # 5% Canary (→ ADR-059)

# DestinationRule: Circuit Breaker auf Infrastruktur-Ebene
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: payment-service
spec:
  host: payment-service
  trafficPolicy:
    outlierDetection:
      consecutive5xxErrors: 5      # 5 Fehler → Ejection
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50       # Max 50% der Instanzen ejected
```

---

## 6. GitOps: Deklarative Infrastruktur

```yaml
# GitOps: Git ist die Single Source of Truth für Infrastruktur
# ArgoCD: reconciliert Kubernetes-Cluster mit Git-Repository

# Git-Repository-Struktur (GitOps):
# k8s/
#   production/
#     order-service/
#       deployment.yaml
#       service.yaml
#       hpa.yaml
#   staging/
#     order-service/
#       deployment.yaml

# ArgoCD Application:
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: order-service-production
spec:
  source:
    repoURL: https://github.com/example/k8s-config
    targetRevision: main
    path: k8s/production/order-service

  destination:
    server: https://kubernetes.default.svc
    namespace: production

  syncPolicy:
    automated:
      prune: true       # Gelöschte Ressourcen werden auch im Cluster gelöscht
      selfHeal: true    # Manuelle Cluster-Änderungen werden überschrieben!
    syncOptions:
      - CreateNamespace=true

# ERGEBNIS:
# PR in k8s-config-Repository → Code-Review → Merge → ArgoCD erkennt
# → automatisches Deployment → vollständig auditierbar in Git
```

---

## 7. Cloud-Native Security (Supply Chain Security → ADR-057)

```yaml
# Policy Enforcement mit OPA (Open Policy Agent) / Gatekeeper

# Verhindert Deployments ohne Security-Labels
apiVersion: templates.gatekeeper.sh/v1beta1
kind: ConstraintTemplate
metadata:
  name: requiresecuritylabels
spec:
  targets:
    - target: admission.k8s.gatekeeper.sh
      rego: |
        package requiresecuritylabels
        violation[{"msg": msg}] {
          not input.review.object.metadata.labels["security-scan"]
          msg := "Deployments müssen security-scan Label haben"
        }
        violation[{"msg": msg}] {
          not input.review.object.metadata.labels["sbom-generated"]
          msg := "Deployments müssen sbom-generated Label haben"
        }
```

---

## 8. Quellen & Referenzen

- **Adam Wiggins, "The Twelve-Factor App" (2012)** — Heroku-Methodik; Grundlage aller Cloud-Native-Prinzipien.
- **CNCF, "Cloud Native Definition" (2018)** — offizielle Definition von "cloud native".
- **Bilgin Ibryam & Roland Huß, "Kubernetes Patterns" (2019), O'Reilly** — Sidecar, Ambassador, Init Container als Kubernetes-native Patterns.
- **Chris Richardson, "Microservices Patterns" (2018), Manning** — Cloud-Native-Deployment-Patterns für Microservices.
- **Weaveworks, "GitOps: Operations by Pull Request" (2017)** — Ursprung des GitOps-Begriffs; Git als Single Source of Truth.
- **Lydia Leong, Gartner, "Cloud-Native Infrastructure" (2020)** — Enterprise-Perspektive auf Cloud-Native-Adoption.

---

## Akzeptanzkriterien

- [ ] Alle Services sind 12-Factor-konform (keine lokalen State-Exceptions)
- [ ] GitOps via ArgoCD: alle Produktions-Deployments über Git-PR
- [ ] HPA konfiguriert für alle Production-Services (min 2, max konfiguriert)
- [ ] Keine `docker-compose up` für Production — nur Kubernetes
- [ ] Policy-Check (OPA/Gatekeeper): Deployments ohne Security-Labels werden blockiert

---

## Verwandte ADRs

- [ADR-037](ADR-037-docker-container.md) — Container-Best-Practices (12-Factor Faktor 7)
- [ADR-038](ADR-038-kubernetes.md) — Kubernetes als Cloud-Native-Plattform
- [ADR-060](ADR-060-infrastructure-as-code.md) — IaC (12-Factor Faktor 3)
- [ADR-069](ADR-069-configuration-management.md) — Config (12-Factor Faktor 3)
- [ADR-093](ADR-093-isaqb-living-documentation.md) — Living Documentation (Expert-Level)
