# ADR-070 — Zero Trust Security & Service Mesh

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Istio      |
| Datum      | 2024-01-01                        |
| Kategorie  | Security / Architektur            |

---

## Kontext & Problem

"Vertraue dem internen Netzwerk" ist ein überholtes Sicherheitsmodell. Wenn ein Service kompromittiert wird, kann er im internen Netzwerk frei kommunizieren. **Zero Trust** bedeutet: Vertraue niemandem — weder extern noch intern. Jede Kommunikation wird authentifiziert und autorisiert. Ein **Service Mesh** (Istio, Linkerd) setzt dieses Modell auf Infrastruktur-Ebene durch — ohne Codeänderungen.

---

## Zero Trust Prinzipien

```
Niemals vertrauen, immer verifizieren:
→ Jede Service-zu-Service-Kommunikation wird authentifiziert (mTLS)
→ Jeder Service braucht explizite Autorisierung für jeden Endpunkt
→ Minimale Berechtigungen: Service A darf nur was es braucht

Assume Breach:
→ Design als ob Angreifer bereits im Netzwerk ist
→ Lateral Movement verhindern durch Microsegmentierung
→ Vollständige Audit-Logs aller Kommunikation
```

---

## mTLS mit Istio: Service Mesh

```yaml
# Istio: mTLS zwischen allen Services im Cluster
# kein Code-Änderung in Spring Boot nötig — Sidecar-Proxy übernimmt

# PeerAuthentication: mTLS Pflicht für den ganzen Namespace
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: production
spec:
  mtls:
    mode: STRICT  # Nur mTLS — kein Plain HTTP zwischen Services
```

```yaml
# AuthorizationPolicy: wer darf wen kontaktieren?
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: order-service-policy
  namespace: production
spec:
  selector:
    matchLabels:
      app: order-service

  rules:
    # Nur api-gateway darf den order-service aufrufen
    - from:
        - source:
            principals:
              - "cluster.local/ns/production/sa/api-gateway"
      to:
        - operation:
            methods: ["POST", "GET", "PATCH"]
            paths: ["/api/v*/*"]

    # Monitoring darf Metrics-Endpunkt abfragen
    - from:
        - source:
            namespaces: ["monitoring"]
      to:
        - operation:
            paths: ["/actuator/prometheus"]
```

---

## Networkpolicies: Kubernetes-Netzwerksegmentierung

```yaml
# Ohne Service Mesh: Kubernetes NetworkPolicy als minimaler Schutz
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: order-service-netpol
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: order-service

  policyTypes:
    - Ingress
    - Egress

  ingress:
    # Nur vom API-Gateway eingehender Traffic erlaubt
    - from:
        - podSelector:
            matchLabels:
              app: api-gateway
      ports:
        - port: 8080

    # Von Prometheus (Monitoring Namespace)
    - from:
        - namespaceSelector:
            matchLabels:
              name: monitoring
      ports:
        - port: 8080

  egress:
    # Nur zur eigenen Datenbank
    - to:
        - podSelector:
            matchLabels:
              app: order-db
      ports:
        - port: 5432

    # Zu Kafka
    - to:
        - podSelector:
            matchLabels:
              app: kafka
      ports:
        - port: 9092

    # DNS (Pflicht für K8s Service Discovery)
    - to:
        - namespaceSelector: {}
      ports:
        - port: 53
          protocol: UDP
```

---

## Service Account Tokens: Minimale Kubernetes-Berechtigungen

```yaml
# RBAC: minimale Berechtigungen für jeden Service
apiVersion: v1
kind: ServiceAccount
metadata:
  name: order-service
  namespace: production
---
# Role: nur was der Service wirklich braucht
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: order-service-role
rules:
  - apiGroups: [""]
    resources: ["configmaps"]
    resourceNames: ["order-service-config"]
    verbs: ["get", "watch"]
  # NICHT: wildcards wie verbs: ["*"] resources: ["*"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: order-service-rolebinding
subjects:
  - kind: ServiceAccount
    name: order-service
roleRef:
  kind: Role
  name: order-service-role
  apiGroup: rbac.authorization.k8s.io
```

---

## Spring Boot: Service-Identität prüfen

```java
// Auch wenn mTLS durch Istio übernommen wird:
// Anwendungsebene kann Service-Identität zusätzlich prüfen

@Component
public class ServiceIdentityValidator {

    // Istio injiziert Service-Identität als Header
    private static final String SPIFFE_HEADER = "x-forwarded-client-cert";

    public void validateCallerIdentity(HttpServletRequest request,
                                        String allowedSpiffeId) {
        var cert = request.getHeader(SPIFFE_HEADER);
        if (cert == null || !cert.contains("URI=spiffe://" + allowedSpiffeId)) {
            throw new AccessDeniedException(
                "Request from unauthorized service identity");
        }
    }
}

// SPIFFE/SPIRE: Service Identity für Zero Trust
// spiffe://cluster.local/ns/production/sa/api-gateway
```

---

## Secrets: Rotation ohne Downtime (Zero Trust für Credentials)

```yaml
# Vault Agent Sidecar: dynamische Credentials, kein statisches Secret
# In jedem Pod: Vault Agent als Sidecar-Container
spec:
  serviceAccountName: order-service
  initContainers:
    - name: vault-init
      image: vault:latest
      command: ["vault", "agent", "-config=/vault/config/agent.hcl"]
  containers:
    - name: order-service
      # App liest Credentials aus /vault/secrets/ — Vault Agent rotiert automatisch
      volumeMounts:
        - name: vault-secrets
          mountPath: /vault/secrets
```

---

## Konsequenzen

**Positiv:** Kompromittierter Service kann nicht auf andere Services zugreifen (Lateral Movement geblockt). mTLS verschlüsselt und authentifiziert alle interne Kommunikation. RBAC minimiert Auswirkung eines kompromittierten Service Accounts.

**Negativ:** Istio erhöht Latenz (Sidecar-Proxy, ~1-5ms pro Hop). Service Mesh ist komplexe Infrastruktur. Istio-Lernkurve für Operations-Team. NetworkPolicies bei vielen Services aufwändig zu pflegen.

---

## 💡 Guru-Tipps

- **Linkerd als leichtere Alternative zu Istio**: einfacher zu betreiben, weniger Features.
- **`PERMISSIVE` → `STRICT` mTLS schrittweise**: erst PERMISSIVE (erlaubt beides), dann STRICT nach Validierung.
- **Audit Logging**: Istio-Access-Logs an zentrales Log-System (→ ADR-017) — vollständige Kommunikationshistorie.
- **OPA (Open Policy Agent)**: Policy-as-Code für komplexe Autorisierungsregeln im Service Mesh.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP-Prinzipien auf Netzwerk-Ebene.
- [ADR-040](ADR-040-oauth2-oidc-jwt.md) — JWT für User-Identität, mTLS für Service-Identität.
- [ADR-038](ADR-038-kubernetes.md) — Kubernetes als Basis für Service Mesh.
- [ADR-069](ADR-069-configuration-management.md) — Vault für dynamische Credentials.
