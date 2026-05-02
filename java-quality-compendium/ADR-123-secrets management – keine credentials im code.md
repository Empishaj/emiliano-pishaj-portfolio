## Kontext

Microservices brauchen Zugangsdaten: Datenbankpasswörter, API-Keys,
Zertifikate, Signing-Keys. Die naheliegende Lösung ist, diese direkt
in `application.yml` oder im Java-Code zu schreiben.

Das ist eine der häufigsten und folgenreichsten Sicherheitslücken
überhaupt – und sie passiert auch erfahrenen Teams.

---

## Das Problem in der Praxis

###  So sieht es schlecht aus

```yaml
# application.yml – liegt im Git-Repository
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/myapp
    username: admin
    password: SuperGeheim123!      # 💀 Im Klartext im Repo

payment:
  stripe-key: sk_live_abc123xyz    # 💀 Produktions-Key öffentlich sichtbar
  webhook-secret: whsec_xyz987
```

```java
// ❌ Noch schlimmer: Hardcoded im Java-Code
@Service
public class PaymentService {

    // 💀 Steht jetzt für immer in der Git-History
    private static final String API_KEY = "sk_live_abc123xyz";

    // 💀 Default-Wert als Fallback – startet auch ohne echtes Secret
    @Value("${db.password:admin123}")
    private String dbPassword;
}
```

**Was hier schiefläuft:**

- Secrets in der Git-History sind **permanent** – auch nach `git rm`
  noch mit `git log -S "password"` auffindbar
- Automatisierte Bots scannen GitHub/GitLab innerhalb von **Sekunden**
  nach neuen Commits
- `@Value("${secret:defaultValue}")` ist tückisch: wenn der echte
  Wert fehlt, startet die App mit dem unsicheren Default – **kein
  Fehler, keine Warnung**
- Alle Entwickler sehen Produktions-Credentials → Insider-Threat,
  versehentliche Nutzung

**Das echte Risiko:**

```bash
# Ein Angreifer braucht nur das:
git log --all -S "password" --source
git show <commit-hash>:src/main/resources/application.yml
# Fertig. Auch wenn der Commit "gelöscht" wurde.
```

---

## Die Lösung

### So macht man es richtig

**Schritt 1: Konfiguration über Umgebungsvariablen (Mindeststandard)**

```yaml
# application.yml – sicher im Repository
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:myapp}
    username: ${DB_USER}          # Kein Default → App startet nicht ohne Wert
    password: ${DB_PASSWORD}      # Kein Default → Fail-fast by design

payment:
  stripe-key: ${PAYMENT_STRIPE_KEY}
  webhook-secret: ${PAYMENT_WEBHOOK_SECRET}
```

```java
// ✅ Typsichere Konfiguration ohne Defaults für Secrets
@ConfigurationProperties(prefix = "payment")
@Validated
public record PaymentProperties(
    @NotBlank String stripeKey,
    @NotBlank String webhookSecret
) {}

// Schlägt beim Start fehl wenn Werte fehlen – gewollt!
```

**Schritt 2: HashiCorp Vault für Produktion**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  cloud:
    vault:
      host: vault.internal.company.com
      port: 8200
      scheme: https
      authentication: KUBERNETES   # Kein statisches Token in K8s!
      kubernetes:
        role: payment-service
      kv:
        enabled: true
        backend: secret
        default-context: payment-service
```

**Schritt 3: Lokale Entwicklung sauber halten**

```bash
# .env.local – MUSS in .gitignore stehen
DB_PASSWORD=local-only-password
PAYMENT_STRIPE_KEY=sk_test_...    # Nur Test-Keys lokal!
PAYMENT_WEBHOOK_SECRET=whsec_test_...
```

```bash
# .gitignore
.env
.env.local
.env.*.local
**/application-local.yml
**/application-secrets.yml
```

**Schritt 4: Pre-Commit-Hook als Sicherheitsnetz**

```bash
# .git/hooks/pre-commit (oder via detect-secrets)
pip install detect-secrets
detect-secrets scan > .secrets.baseline
detect-secrets audit .secrets.baseline
```

---

## Tipps

**Pitfall #1 – Die 4-Minuten-Regel:**
AWS hat ein eigenes System das Secrets in Public-Repos erkennt und
den Account-Inhaber benachrichtigt. Aber automatisierte Bots sind
schneller: Zwischen Push und erstem Missbrauch vergehen oft
**unter 4 Minuten**. Prävention schlägt Reaktion immer.

**Pitfall #2 – `@Value` mit Default ist eine Zeitbombe:**
```java
// Sieht harmlos aus, ist es nicht
@Value("${jwt.secret:myDefaultSecret}")
private String jwtSecret;
// Wenn JWT_SECRET nicht gesetzt ist, läuft die App mit
// "myDefaultSecret" – und niemand merkt es sofort.
// Tokens werden akzeptiert, Logs zeigen keinen Fehler.
```

**Pitfall #3 – Kubernetes Secrets sind Base64, nicht verschlüsselt:**
```bash
# Jeder mit kubectl-Zugriff kann das:
kubectl get secret myapp-secrets -o jsonpath='{.data.DB_PASSWORD}' | base64 -d
# → SuperGeheim123!
```
Für echte Verschlüsselung: **Sealed Secrets** oder **External Secrets
Operator** mit Vault-Backend verwenden.

**War Story:** Ein Team rotierte einen Datenbankpasswort-Secret in
Vault – aber die App nutzte noch den gecachten Wert aus dem
Application-Context. Erst nach einem Neustart funktionierte es.
Lösung: `spring.cloud.vault.config.lifecycle.enabled=true` für
automatische Secret-Rotation ohne Neustart.

**Profi-Tipp – Secret Scanning im CI/CD:**
```yaml
# .github/workflows/security.yml
- name: Secret Scanning
  uses: trufflesecurity/trufflehog@main
  with:
    path: ./
    base: ${{ github.event.repository.default_branch }}
    head: HEAD
```

---

### Takeaway

- Secrets niemals im Repository
- Fail-fast: App startet nicht ohne korrekte Konfiguration
- Secret-Rotation ohne Redeployment möglich (mit Vault)
- Audit-Trail für alle Secret-Zugriffe
- Vault als zusätzliche Infrastruktur-Komponente
- Lokales Setup braucht einmalige Einrichtung pro Entwickler

---

## Checkliste

- [ ] Kein `@Value("${secret:defaultValue}")` für sicherheitsrelevante Werte
- [ ] `application.yml` enthält keine Klartext-Credentials
- [ ] `.env`-Dateien stehen in `.gitignore`
- [ ] Pre-Commit-Hook oder CI-Secret-Scanning aktiv
- [ ] Bestehende Git-History mit `truffleHog` gescannt
- [ ] Kubernetes Secrets via Sealed Secrets oder External Secrets verschlüsselt
- [ ] Secret-Rotation-Prozess dokumentiert und getestet
