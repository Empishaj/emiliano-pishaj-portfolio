# ADR-038 — Kubernetes: Health Probes, Resource Limits & Secrets

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Spring Boot 3.x · K8s 1.28+ |
| Datum      | 2026-03-15                        |
| Kategorie  | DevOps / Kubernetes               |

---

## Deployment-Manifest (vollständig kommentiert)

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  labels:
    app: order-service
    version: "1.0.0"
spec:
  replicas: 3                  # Minimum 2 für Hochverfügbarkeit
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1              # 1 extra Pod beim Update
      maxUnavailable: 0        # Kein Ausfall während Update (Zero-Downtime)
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      # Security: kein Root
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001

      # Affinity: Pods auf verschiedene Nodes verteilen
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                topologyKey: kubernetes.io/hostname
                labelSelector:
                  matchLabels:
                    app: order-service

      containers:
        - name: order-service
          image: ghcr.io/example/order-service:abc1234  # Pin auf Git-SHA!
          imagePullPolicy: IfNotPresent

          ports:
            - containerPort: 8080

          # ── Resource Limits: IMMER setzen ──────────────────────────
          resources:
            requests:                 # Minimum garantiert (Scheduling-Basis)
              memory: "256Mi"
              cpu: "100m"             # 0.1 CPU-Kerne
            limits:                   # Maximum (OOM-Kill bei Überschreitung)
              memory: "512Mi"
              cpu: "500m"             # 0.5 CPU-Kerne
          # Ohne limits: ein Pod kann den Node leerlaufen

          # ── Health Probes ──────────────────────────────────────────
          startupProbe:              # Verhindert Kills während langer Startup
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            failureThreshold: 30     # 30 × 10s = 5 Minuten Startup-Toleranz
            periodSeconds: 10

          livenessProbe:             # Ist der Pod noch am Leben?
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 0   # startupProbe übernimmt initiale Wartezeit
            periodSeconds: 10
            failureThreshold: 3      # 3 Fehler → Pod neu starten

          readinessProbe:            # Ist der Pod bereit für Traffic?
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            periodSeconds: 5
            failureThreshold: 3      # 3 Fehler → Pod aus Load Balancer entfernen

          # ── Umgebungsvariablen ─────────────────────────────────────
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "production"
            - name: SERVER_PORT
              value: "8080"
            # JVM: Container-Limits respektieren
            - name: JAVA_TOOL_OPTIONS
              value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

          # ── Secrets: NIEMALS als env-Value hardcodieren ─────────────
          envFrom:
            - secretRef:
                name: order-service-secrets   # Kubernetes Secret

          # Read-Only Filesystem (Security)
          securityContext:
            readOnlyRootFilesystem: true
            allowPrivilegeEscalation: false
            capabilities:
              drop: ["ALL"]

          volumeMounts:
            - mountPath: /tmp      # Spring braucht /tmp für temporäre Dateien
              name: tmp-volume

      volumes:
        - name: tmp-volume
          emptyDir: {}
```

---

## Spring Boot Actuator für Health Probes konfigurieren

```yaml
# application.yml
management:
  endpoint:
    health:
      probes:
        enabled: true           # Aktiviert /actuator/health/liveness + readiness
      show-details: never       # Kein internes Detail in Produktion
      group:
        liveness:
          include: livenessState, diskSpace
        readiness:
          include: readinessState, db, redis  # Readiness prüft Abhängigkeiten!
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
```

```java
// Graceful Shutdown: Spring wartet auf laufende Requests
// application.yml:
// server:
//   shutdown: graceful
// spring:
//   lifecycle:
//     timeout-per-shutdown-phase: 30s

// Kubernetes preStop Hook für Zero-Downtime:
// lifecycle:
//   preStop:
//     exec:
//       command: ["/bin/sh", "-c", "sleep 15"]
// → 15s warten damit K8s Service Endpoints aktualisiert
```

---

## Secrets Management

```yaml
# ❌ NIEMALS: Secrets als Klartext im Manifest
env:
  - name: DB_PASSWORD
    value: "mein-super-geheimes-passwort"   # Im Git! Katastrophe!

# ✅ Kubernetes Secret
apiVersion: v1
kind: Secret
metadata:
  name: order-service-secrets
type: Opaque
data:
  # Base64-encodiert (kein Schutz! nur Encoding)
  # Echte Lösung: External Secrets Operator + Vault/AWS Secrets Manager
  SPRING_DATASOURCE_PASSWORD: <base64-encoded>
  JWT_SECRET: <base64-encoded>
```

```yaml
# ✅✅ Besser: External Secrets Operator + HashiCorp Vault
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: order-service-secrets
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-backend
    kind: SecretStore
  target:
    name: order-service-secrets
  data:
    - secretKey: SPRING_DATASOURCE_PASSWORD
      remoteRef:
        key: secret/order-service
        property: db-password
```

---

## HorizontalPodAutoscaler

```yaml
# Automatische Skalierung basierend auf CPU/Memory
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 2    # Mindestens 2 für HA
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70    # Skalierung bei 70% CPU
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

---

## Konsequenzen

**Positiv:** Resource Limits verhindern dass ein Pod den Node destabilisiert. Health Probes ermöglichen Zero-Downtime-Deployments und automatische Selbstheilung. RollingUpdate mit `maxUnavailable: 0` — kein Ausfall während Deploy.

**Negativ:** Resource-Limits falsch gesetzt → OOM-Kill oder Throttling. Health Probes zu aggressiv → unnötige Pod-Restarts. Startupprobe notwendig damit liveness nicht zu früh feuert.

---

## Tipps

- **Limits ≠ Requests**: Requests sind das Minimum (Scheduling). Limits sind das Maximum (OOM). Limits sollte ~2× Requests sein.
- **Nie `latest` als Image-Tag** in Kubernetes — erzeugt nicht-deterministische Deployments.
- **`kubectl rollout undo deployment/order-service`** für sofortigen Rollback.
- **PodDisruptionBudget**: `minAvailable: 1` verhindert dass Kubernetes zu viele Pods gleichzeitig beendet.
 