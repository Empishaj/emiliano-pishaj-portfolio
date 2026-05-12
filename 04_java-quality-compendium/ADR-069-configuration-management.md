# ADR-069 — Configuration Management: Vault & Spring Cloud Config

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · HashiCorp Vault |
| Datum      | 2024-01-01                        |
| Kategorie  | Security / Operations             |

---

## Kontext & Problem

Konfiguration hat drei Kategorien: unkritische Werte (Timeouts, Feature Flags), umgebungsabhängige Werte (URLs), und Secrets (Passwörter, API-Keys). Secrets in application.yml, in Umgebungsvariablen die geloggt werden, oder in Git-History — das ist die häufigste Ursache für Security-Incidents. Dieses ADR trennt die Konfigurationsebenen und regelt Secrets-Management.

---

## Konfigurationshierarchie

```
Priorität (höher überschreibt niedriger):
1. Vault/External Secrets    ← Secrets: niemals in anderen Quellen
2. Kubernetes ConfigMaps     ← Umgebungsspezifische Werte
3. application-{profile}.yml ← Profil-spezifische Defaults
4. application.yml           ← Anwendungs-Defaults
```

---

## application.yml: nur unkritische Defaults

```yaml
# ✅ Was in application.yml gehört: unkritische Defaults, kein Secret

server:
  port: 8080
  shutdown: graceful

spring:
  application:
    name: order-service
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false

management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus

# Feature Flags: in externem Config-System (nicht hier)
# Timeouts: hier OK, aber nicht in Produktion überschreiben wenn nicht nötig
resilience4j:
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 500ms

# ❌ NIEMALS hier:
# spring.datasource.password: ...
# jwt.secret: ...
# stripe.api-key: ...
```

---

## HashiCorp Vault: Secrets zentral verwalten

```kotlin
// build.gradle.kts
implementation("org.springframework.cloud:spring-cloud-starter-vault-config:4.1.0")
```

```yaml
# bootstrap.yml (wird vor application.yml geladen)
spring:
  cloud:
    vault:
      host: vault.internal
      port: 8200
      scheme: https
      authentication: KUBERNETES      # In K8s: Service Account Token
      kubernetes:
        role: order-service
        service-account-token-file: /var/run/secrets/kubernetes.io/serviceaccount/token

      # Vault-Pfade wo Secrets liegen
      kv:
        enabled: true
        backend: secret
        default-context: order-service   # secret/data/order-service
        profiles:
          path: production               # secret/data/order-service/production
```

```bash
# Vault: Secrets anlegen (einmalig, von Ops-Team)
vault kv put secret/order-service \
  spring.datasource.password="super-secret-db-pw" \
  jwt.secret="random-256-bit-key" \
  stripe.api-key="sk_prod_..."

vault kv put secret/order-service/production \
  spring.datasource.url="jdbc:postgresql://prod-db:5432/orderdb"
```

```java
// Spring Boot: Vault-Secrets automatisch als Properties verfügbar
@ConfigurationProperties(prefix = "spring.datasource")
public record DatabaseConfig(
    String url,
    String username,
    @NonNull String password  // Kommt aus Vault — niemals geloggt
) {}

@ConfigurationProperties(prefix = "stripe")
public record StripeConfig(
    @NonNull String apiKey
) {}
```

---

## Vault Dynamic Secrets: DB-Credentials on-the-fly

```yaml
# Vault generiert DB-Credentials dynamisch — kein statisches Passwort!
spring:
  cloud:
    vault:
      database:
        enabled: true
        role: order-service         # Vault Database Role
        backend: database
        # Vault erstellt temporären DB-User mit TTL=1h
        # Spring Boot erneuert automatisch vor Ablauf
```

```bash
# Vault Database Engine konfigurieren
vault write database/config/orderdb \
  plugin_name=postgresql-database-plugin \
  connection_url="postgresql://{{username}}:{{password}}@postgres:5432/orderdb" \
  allowed_roles="order-service"

vault write database/roles/order-service \
  db_name=orderdb \
  creation_statements="CREATE ROLE '{{name}}' WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}';" \
  default_ttl="1h" \
  max_ttl="24h"
# → Jede App-Instanz hat eigene temporäre DB-Credentials mit TTL
# → Leak eines Credentials ist nach 1h wertlos
```

---

## Spring Cloud Config: Zentralisierte Non-Secret-Konfiguration

```yaml
# Für Umgebungs-Konfiguration ohne Vault (Non-Secrets):
# application.yml auf Spring Cloud Config Server

spring:
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: true   # App startet nicht wenn Config-Server nicht erreichbar
      retry:
        max-attempts: 6
```

```yaml
# config-server Git-Repository:
# order-service-production.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db.internal:5432/orderdb
    # password: aus Vault (nicht hier!)

resilience4j:
  retry:
    instances:
      paymentService:
        max-attempts: 5  # Prod: mehr Retries als Default

logging:
  level:
    com.example: INFO   # Prod: kein DEBUG
```

---

## Konfigurationsvalidierung beim Start

```java
// @ConfigurationProperties + Bean Validation:
// App startet nicht wenn Konfiguration invalide
@ConfigurationProperties(prefix = "order-service")
@Validated
public record OrderServiceConfig(
    @NotNull  Duration  paymentTimeout,
    @Positive int       maxItemsPerOrder,
    @NotBlank String    supportEmail,
    @Min(1)   int       maxRetries
) {}

// @Value für einfache Werte — aber @ConfigurationProperties bevorzugen
@Value("${order-service.max-items-per-order:10}")
private int maxItems; // Default: 10 wenn nicht konfiguriert
```

---

## Sensitive Konfiguration niemals loggen

```java
// Spring Boot loggt beim Start alle Properties — Secrets maskieren!
// application.yml:
// spring:
//   cloud:
//     config:
//       hide-sensitive-properties: true

// Eigene Masking-Konfiguration
@Bean
public SanitizingFunction customSanitizer() {
    return data -> {
        var keyName = data.getKey().toLowerCase();
        if (keyName.contains("password") || keyName.contains("secret")
                || keyName.contains("key") || keyName.contains("token")) {
            return data.withValue("******");
        }
        return data;
    };
}
```

---

## Secrets Rotation ohne Downtime

```java
// Spring Cloud Vault: automatische Secret-Rotation
// Wenn Vault-Secret sich ändert → Spring Boot lädt es beim nächsten Refresh

@RefreshScope  // Bean wird bei /actuator/refresh neu erstellt
@Service
public class StripePaymentGateway {

    @Value("${stripe.api-key}")
    private String apiKey;  // Wird bei Rotation automatisch aktualisiert
}

// Rotation triggern (z.B. nach manuellem Secret-Update):
// POST /actuator/refresh
// → Alle @RefreshScope Beans werden neu erstellt
// → Neue Credentials ohne Neustart
```

---

## Konsequenzen

**Positiv:** Secrets niemals in Dateisystem oder Git. Dynamic Secrets: Credentials ablaufen automatisch. Zentrale Konfiguration: Änderungen wirken sofort ohne Deployment. Audit-Trail: Vault loggt jeden Secret-Zugriff.

**Negativ:** Vault ist kritische Infrastruktur — muss hochverfügbar sein. Bootstrap-Problem: App braucht Vault-Credentials um Vault zu erreichen (K8s Service Account löst das). Spring Cloud Config Server ist zusätzlicher Service.

---

## 💡 Guru-Tipps

- **Vault Agent Sidecar**: in Kubernetes — Vault Agent Container schreibt Secrets als Dateien, App liest Dateien. Keine direkte Vault-Abhängigkeit der App.
- **External Secrets Operator** (→ ADR-038): synchronisiert Vault-Secrets in K8s Secrets automatisch.
- **Niemals `@Value` für Secrets**: stattdessen `@ConfigurationProperties` — typsicher, validierbar.
- **`spring.config.import`**: moderne Art um externe Konfigurationsquellen einzubinden (Spring Boot 2.4+).

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — Secrets nie hardcoden.
- [ADR-038](ADR-038-kubernetes.md) — K8s Secrets und External Secrets Operator.
- [ADR-057](ADR-057-sbom-dependency-auditing.md) — Vault API-Keys für SBOM-Tools.
