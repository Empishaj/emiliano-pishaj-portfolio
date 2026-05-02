# QG-JAVA-039-01 — Statische Feature Flags über Konfiguration mit `application.yml`

## Dokumentstatus

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| Dokumenttyp | Java Quality Guideline / vertiefendes Kompendium-Kapitel | Statisches Feature-Flagging über Spring-Boot-Konfiguration | Spring Boot Externalized Configuration: https://docs.spring.io/spring-boot/reference/features/external-config.html |
| ID | QG-JAVA-039-01 | Unterkapitel zu Feature Flags | — |
| Titel | Statische Feature Flags über Konfiguration mit `application.yml` | `features.invoice-export.enabled=false` | — |
| Status | Accepted / verbindlicher Standard für statische Konfigurations-Flags in Java-/Spring-Boot-Services | Gilt für neue und überarbeitete Spring-Boot-Services | — |
| Sprache | Deutsch | Fachbegriffe wie `Feature Flag`, `application.yml`, `@ConditionalOnProperty` bleiben technisch benannt | — |
| Java-Baseline | Java 21+ | Records für `@ConfigurationProperties` sind erlaubt und empfohlen | Java Records: https://docs.oracle.com/en/java/javase/21/language/records.html |
| Spring-Baseline | Spring Boot 3.x | `application.yml`, Profile, `@ConfigurationProperties`, `@ConditionalOnProperty` | Spring Boot Reference: https://docs.spring.io/spring-boot/reference/ |
| Kategorie | DevOps / Release Management / Spring Boot Configuration / SaaS Platform Quality | Trennung von Deployment-Konfiguration und fachlicher Aktivierung | — |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, DevOps, QA, Security, Plattformverantwortliche | Entwicklung, Review, CI/CD, Betrieb | — |
| Verbindlichkeit | Neue statische Flags müssen zentral unter `features.*` liegen, typisiert gelesen, getestet und dokumentiert werden | `features.checkout.new-flow-enabled` | — |
| Nicht-Ziel | Dieses Dokument beschreibt keine dynamischen Rollouts, keine A/B-Test-Auswertung und keine Autorisierung | Dafür braucht es dynamische Flag-Systeme, Experiment-Plattformen und Berechtigungssysteme | OpenFeature: https://openfeature.dev/ |
| Prüfstatus | Fachlich validiert gegen Spring-Boot-Dokumentation zu externer Konfiguration, Profilen und `@ConditionalOnProperty` sowie OWASP Secrets Management | Stand: 2026-05-02 | OWASP Secrets Management: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie statische Feature Flags in Java-21-/Spring-Boot-3.x-Anwendungen sauber über Konfiguration umgesetzt werden. Der Schwerpunkt liegt auf `application.yml`, Profil-Dateien wie `application-local.yml` und `application-prod.yml`, Umgebungsvariablen, `@ConditionalOnProperty` und typisierten `@ConfigurationProperties`.

Statische Flags sind einfache, robuste Konfigurationsschalter. Sie entscheiden beim Start der Anwendung oder beim Aufbau des Spring `ApplicationContext`, ob eine Funktion, eine technische Variante, ein Adapter, eine Integration oder ein Codepfad aktiviert ist. Sie sind besonders geeignet für lokale Entwicklungsoptionen, environment-spezifische Integrationen, technische Migrationsschalter und einfache Startzeitentscheidungen.

Diese Richtlinie verhindert drei typische Fehler. Erstens: Feature-Flags werden als lose Strings im Fachcode verstreut. Zweitens: statische Konfigurationsflags werden fälschlich als dynamische Kill Switches oder Canary-Mechanismen verstanden. Drittens: Flags werden mit Sicherheits-, Mandanten- oder Berechtigungsentscheidungen verwechselt.

---

## 2. Kurzregel

Statische Feature Flags über `application.yml` dürfen verwendet werden, wenn die Entscheidung beim Start der Anwendung feststehen darf. Alle statischen Flags müssen zentral unter `features.*` liegen, sprechend benannt, typisiert über `@ConfigurationProperties` oder deklarativ über `@ConditionalOnProperty` gelesen, für aktivierten und deaktivierten Zustand getestet und mit einem klaren Zweck dokumentiert werden.

Statische Flags dürfen nicht als Ersatz für Autorisierung, Mandantentrennung, Entitlements, Secrets Management, Canary Releases, A/B-Tests oder schnelle produktive Kill Switches verwendet werden.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für Spring-Boot-Services, Batch-Anwendungen, Worker, interne Plattformmodule, Integrationsadapter und lokale Entwicklungsumgebungen, in denen Feature- oder Betriebsvarianten über Konfiguration gesteuert werden.

Sie gilt insbesondere für folgende Fälle:

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Lokale Entwicklung | Lokale Adapter, Mocks oder Stub-Implementierungen werden beim Start aktiviert | `features.mail.mock-sender-enabled=true` | Geeignet |
| Environment-spezifische Integration | Staging nutzt Sandbox-Provider, Produktion nutzt echten Provider | `features.payment.sandbox-enabled=true` | Geeignet |
| Bean-Auswahl | Genau eine Implementierung eines Interfaces soll im ApplicationContext existieren | `SmtpMailSender` oder `LoggingMailSender` | Geeignet |
| Technische Migration | Neuer technischer Pfad wird deployt, aber initial deaktiviert | `features.invoice-export.enabled=false` | Geeignet mit Ablaufdatum |
| Batch-/Worker-Verhalten | Ein optionaler Job oder Export wird pro Umgebung aktiviert | `features.cleanup-job.enabled=true` | Geeignet |
| Einfache Betriebsoption | Nicht zeitkritische Funktion kann beim nächsten Start deaktiviert werden | `features.expensive-report.enabled=false` | Eingeschränkt geeignet |

Diese Richtlinie gilt nicht als primärer Mechanismus für folgende Fälle:

| Aspekt | Details/Erklärung | Beispiel | Besserer Mechanismus |
|---|---|---|---|
| Canary Release | Prozentuale Aktivierung für Nutzer oder Requests | 5 % der Nutzer | Unleash, FF4J, OpenFeature, Gateway-/Traffic-Steuerung |
| A/B-Test | Variantensteuerung mit Auswertung, Stickiness und Metriken | Checkout-Variante A/B | Experiment-Plattform oder dynamisches Feature-Flag-System |
| Produktberechtigung | Kunde darf ein Modul nutzen oder nicht | Premium-Modul | Entitlement-/Permission-Modell |
| Mandantentrennung | Datenzugriff muss pro Tenant abgesichert sein | Tenant A darf nur Tenant-A-Daten sehen | Tenant-Isolation im Domain-, Query- und Security-Modell |
| Produktiver Kill Switch | Sofortiges Abschalten ohne Restart | Externer Payment-Provider fällt aus | Dynamisches Flag-System oder operative Control Plane |
| Secrets | Zugangsdaten, Tokens, API Keys | `payment.api-key` | Secret Store / Vault / Kubernetes Secret / Cloud Secret Manager |

---

## 4. Technischer Hintergrund

Spring Boot unterstützt externe Konfiguration, damit dieselbe Anwendung in unterschiedlichen Umgebungen mit unterschiedlichen Einstellungen betrieben werden kann. Zu den unterstützten Konfigurationsquellen gehören unter anderem Properties-Dateien, YAML-Dateien, Umgebungsvariablen und Command-Line-Argumente. Das ist die technische Grundlage dafür, statische Feature Flags über `application.yml` und Deployment-Konfiguration zu setzen.

Eine typische Flag-Struktur sieht so aus:

```yaml
features:
  invoice-export:
    enabled: false
  mail:
    mock-sender-enabled: true
  checkout:
    new-flow-enabled: false
```

Spring Boot kann diese Werte auf unterschiedliche Weise verwenden:

1. direkt über `@ConditionalOnProperty`, um Beans abhängig von Properties zu erzeugen,
2. über `@ConfigurationProperties`, um strukturierte Konfiguration in typisierte Java-Objekte zu binden,
3. über Profile wie `local`, `test`, `staging` und `prod`,
4. über Umgebungsvariablen oder Deployment-Werte,
5. über Testwerkzeuge wie `ApplicationContextRunner`.

`@ConditionalOnProperty` ist besonders wichtig für Bean-Level-Flags. Die Annotation prüft, ob eine Property in der Spring `Environment` vorhanden ist und einen bestimmten Wert besitzt. Über `havingValue` und `matchIfMissing` wird definiert, wann eine Bedingung erfüllt ist. Das geschieht beim Aufbau des ApplicationContext. Es ist deshalb eine Startzeitentscheidung, keine Live-Steuerung.

---

## 5. Verbindlicher Standard

### 5.1 Namespace

Alle statischen Feature Flags müssen unter dem Namespace `features.*` liegen.

```yaml
features:
  invoice-export:
    enabled: false
```

Nicht erlaubt sind lose Flags auf Root-Ebene:

```yaml
# ❌ Falsch
newCheckout: true
exportEnabled: false
useMockMail: true
```

Begründung: Ein einheitlicher Namespace macht Flags auffindbar, reviewbar und automatisierbar prüfbar.

### 5.2 Struktur

Für einfache Flags wird eine `enabled`-Eigenschaft verwendet:

```yaml
features:
  invoice-export:
    enabled: false
```

Für komplexere technische Varianten wird ein eigener Unterbaum genutzt:

```yaml
features:
  payment-provider:
    enabled: true
    provider: "sandbox"
    timeout: 2s
    retry-enabled: true
```

### 5.3 Zugriff im Code

In Fachlogik wird nicht direkt mit `Environment`, verstreutem `@Value` oder hartcodierten Property-Strings gearbeitet. Der Zugriff erfolgt bevorzugt über:

1. `@ConfigurationProperties` für typisierte Feature-Konfiguration,
2. `@ConditionalOnProperty` für Bean-Auswahl beim Start,
3. eine kleine Feature-Konfigurations- oder Router-Klasse, wenn mehrere Implementierungen orchestriert werden müssen.

### 5.4 Tests

Jedes statische Flag muss mindestens zwei relevante Testfälle haben:

1. Flag aktiviert,
2. Flag deaktiviert.

Bei Bean-Auswahl wird geprüft, welche Bean im Context existiert. Bei Verhalten innerhalb eines Services wird geprüft, welcher Codepfad aktiv ist.

### 5.5 Lifecycle

Temporäre Release- oder Migrationsflags müssen ein Entfernungskriterium besitzen. Dauerhafte technische Betriebsflags müssen als solche benannt und dokumentiert werden.

---

## 6. Gute Anwendung: statische Bean-Auswahl

Ein klassischer Fall ist die Auswahl zwischen echter Integration und lokaler Ersatzimplementierung.

```yaml
features:
  mail:
    mock-sender-enabled: false
```

```java
public interface MailSender {
    void send(MailMessage message);
}
```

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "features.mail",
        name = "mock-sender-enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class SmtpMailSender implements MailSender {

    @Override
    public void send(MailMessage message) {
        // echter SMTP-Versand
    }
}
```

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "features.mail",
        name = "mock-sender-enabled",
        havingValue = "true"
)
public class LoggingMailSender implements MailSender {

    @Override
    public void send(MailMessage message) {
        // lokaler Entwicklungsmodus: kein echter Versand
    }
}
```

Diese Lösung ist sauber, weil der Fachcode nur gegen `MailSender` arbeitet. Die Auswahl der Implementierung ist Konfiguration, nicht Businesslogik.

---

## 7. Gute Anwendung: typisierte Properties

Wenn ein Feature mehr als einen Boolean-Wert besitzt, wird `@ConfigurationProperties` verwendet.

```yaml
features:
  invoice-export:
    enabled: true
    max-batch-size: 500
    include-attachments: false
```

```java
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "features.invoice-export")
public record InvoiceExportProperties(
        boolean enabled,
        int maxBatchSize,
        boolean includeAttachments
) {
    public InvoiceExportProperties {
        if (maxBatchSize <= 0) {
            throw new IllegalArgumentException("maxBatchSize must be positive");
        }
    }
}
```

```java
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InvoiceExportProperties.class)
class FeatureConfiguration {
}
```

```java
import org.springframework.stereotype.Service;

@Service
public class InvoiceExportService {

    private final InvoiceExportProperties properties;

    public InvoiceExportService(InvoiceExportProperties properties) {
        this.properties = properties;
    }

    public ExportResult export(ExportCommand command) {
        if (!properties.enabled()) {
            return ExportResult.disabled("Invoice export is disabled by configuration.");
        }

        return runExport(
                command,
                properties.maxBatchSize(),
                properties.includeAttachments()
        );
    }

    private ExportResult runExport(
            ExportCommand command,
            int maxBatchSize,
            boolean includeAttachments
    ) {
        // Exportlogik
        return ExportResult.started();
    }
}
```

Diese Lösung ist besser als mehrere `@Value`-Felder, weil alle zusammengehörigen Einstellungen an einer Stelle gebunden, validiert und getestet werden können.

---

## 8. Gute Anwendung: Feature-Router statt verstreute `if`-Abfragen

Wenn ein Feature zwischen zwei Implementierungen routet, sollte die Entscheidung an einer kleinen Stelle konzentriert werden.

```yaml
features:
  checkout:
    new-flow-enabled: false
```

```java
@ConfigurationProperties(prefix = "features.checkout")
public record CheckoutFeatureProperties(
        boolean newFlowEnabled
) {}
```

```java
public interface CheckoutStrategy {
    CheckoutResult execute(CheckoutCommand command);
}
```

```java
import org.springframework.stereotype.Component;

@Component
public class CheckoutStrategyRouter {

    private final CheckoutFeatureProperties properties;
    private final CheckoutStrategy legacyCheckout;
    private final CheckoutStrategy newCheckout;

    public CheckoutStrategyRouter(
            CheckoutFeatureProperties properties,
            LegacyCheckoutStrategy legacyCheckout,
            NewCheckoutStrategy newCheckout
    ) {
        this.properties = properties;
        this.legacyCheckout = legacyCheckout;
        this.newCheckout = newCheckout;
    }

    public CheckoutStrategy select() {
        return properties.newFlowEnabled()
                ? newCheckout
                : legacyCheckout;
    }
}
```

```java
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final CheckoutStrategyRouter strategyRouter;

    public CheckoutService(CheckoutStrategyRouter strategyRouter) {
        this.strategyRouter = strategyRouter;
    }

    public CheckoutResult checkout(CheckoutCommand command) {
        return strategyRouter.select().execute(command);
    }
}
```

Der Vorteil: Das Feature Flag ist nicht in zehn Methoden verstreut. Nach Abschluss der Migration kann der Router einfach entfernt oder vereinfacht werden.

---

## 9. Schlechte Anwendung: `@Value` im Fachcode

```java
@Service
public class CheckoutService {

    @Value("${features.checkout.new-flow-enabled:false}")
    private boolean newFlowEnabled;

    public CheckoutResult checkout(CheckoutCommand command) {
        if (newFlowEnabled) {
            return runNewCheckout(command);
        }
        return runLegacyCheckout(command);
    }
}
```

Das ist für kleine Experimente verlockend, aber als Standard problematisch. Der Property-Name ist ein String im Fachcode. Der Flag-Zugriff ist schwer auffindbar. Tests müssen Spring-Property-Injektion nachbauen oder einen Spring Context starten. Außerdem wachsen solche Stellen schnell zu verteilten Kontrollflüssen.

Besser:

```java
@ConfigurationProperties(prefix = "features.checkout")
public record CheckoutFeatureProperties(boolean newFlowEnabled) {}
```

und Zugriff über Konstruktorinjektion oder Router.

---

## 10. Schlechte Anwendung: statisches Flag als Autorisierung

```java
if (features.adminToolsEnabled()) {
    tenantAdministration.deleteTenant(command.tenantId());
}
```

Das ist falsch. Das Feature Flag sagt nur, ob ein Funktionsbereich grundsätzlich aktiv ist. Es sagt nicht, ob der konkrete Nutzer die Aktion ausführen darf.

Korrekt:

```java
if (!authorizationService.canDeleteTenant(currentUser, command.tenantId())) {
    throw new AccessDeniedException("User is not allowed to delete tenant.");
}

if (!features.adminToolsEnabled()) {
    throw new FeatureDisabledException("Admin tools are disabled.");
}

tenantAdministration.deleteTenant(command.tenantId());
```

Autorisierung und Feature-Aktivierung sind zwei verschiedene Prüfungen.

---

## 11. Schlechte Anwendung: statisches Flag als produktiver Kill Switch

```yaml
features:
  recommendation-engine:
    enabled: false
```

Das kann ein Abschalter sein, aber nur mit Restart oder Redeploy. Wenn ein produktives Problem innerhalb von Sekunden oder wenigen Minuten entschärft werden muss, reicht ein statisches Flag oft nicht. Dann braucht es dynamische Steuerung über Feature-Flag-Systeme, Admin-Operationen, Traffic-Control, Gateway-Regeln oder operative Konfiguration.

Statische Flags dürfen als Kill Switch nur verwendet werden, wenn die Reaktionszeit eines Neustarts fachlich akzeptabel ist und der Betriebsprozess dafür dokumentiert ist.

---

## 12. Profile richtig einsetzen

Spring Profiles sind dafür gedacht, Teile der Anwendungskonfiguration für bestimmte Umgebungen zu trennen. Sie sind gut für `local`, `test`, `staging` und `prod`.

```yaml
# application.yml
features:
  mail:
    mock-sender-enabled: false
  invoice-export:
    enabled: false
```

```yaml
# application-local.yml
features:
  mail:
    mock-sender-enabled: true
  invoice-export:
    enabled: true
```

```yaml
# application-prod.yml
features:
  mail:
    mock-sender-enabled: false
  invoice-export:
    enabled: false
```

Profile dürfen nicht für Geschäftsvarianten missbraucht werden.

Nicht gut:

```yaml
# ❌ Falsch: Profile als Kundenmodell
spring:
  profiles:
    active: tenant-a
```

Mandanten-, Produkt- und Vertragslogik gehört in ein fachliches Entitlement- oder Permission-Modell, nicht in Spring-Profile.

---

## 13. Environment Overrides

In Container-, Cloud- oder CI/CD-Umgebungen werden YAML-Werte häufig durch Umgebungsvariablen überschrieben.

YAML:

```yaml
features:
  invoice-export:
    enabled: false
```

Umgebungsvariable:

```bash
FEATURES_INVOICE_EXPORT_ENABLED=true
```

Das ist für nicht-sensitive Konfigurationswerte zulässig. Secrets gehören nicht in Feature-Flag-Konfiguration.

Nicht erlaubt:

```yaml
features:
  payment:
    provider-api-key: "secret-value"
```

Richtig ist die Trennung:

```yaml
features:
  payment:
    external-provider-enabled: true
```

und Secret getrennt über Secret Store, Vault, Kubernetes Secret, Cloud Secret Manager oder eine vergleichbare Lösung.

---

## 14. Benennungskonvention

Feature Flags müssen sprechend und stabil benannt sein.

Gute Namen:

```yaml
features:
  checkout:
    new-flow-enabled: false
  invoice-export:
    enabled: true
  mail:
    mock-sender-enabled: false
  payment:
    sandbox-provider-enabled: true
```

Schlechte Namen:

```yaml
features:
  test: true
  new: false
  flag1: true
  enabled: true
  customer-feature: true
```

Regel: Ein Flag-Name muss Kontext, Funktion und Bedeutung erkennen lassen.

---

## 15. Default-Werte

Default-Werte sind Architekturentscheidungen. Sie müssen bewusst gesetzt werden.

Für neue, riskante oder experimentelle Funktionen gilt:

```java
@ConditionalOnProperty(
        prefix = "features.invoice-export",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
```

Wenn das Flag fehlt, ist die Funktion deaktiviert. Das ist der sichere Default für neue Funktionalität.

Für stabile Standardimplementierungen kann `matchIfMissing = true` sinnvoll sein:

```java
@ConditionalOnProperty(
        prefix = "features.mail",
        name = "mock-sender-enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class SmtpMailSender implements MailSender {
}
```

Diese Variante bedeutet: Wenn nichts anderes konfiguriert ist, nutze den normalen produktiven Pfad.

`matchIfMissing = true` darf nicht eingesetzt werden, wenn ein fehlender Konfigurationswert zu riskanter Aktivierung führen kann.

---

## 16. Security- und SaaS-Aspekte

### 16.1 Feature Flags sind keine Sicherheitsgrenze

Ein Feature Flag darf niemals die einzige Prüfung für eine sicherheitsrelevante Aktion sein. Es entscheidet über Aktivierung, nicht über Berechtigung.

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Autorisierung | Nutzerrechte müssen separat geprüft werden | `canDeleteTenant(...)` | Pflicht |
| Mandantentrennung | Tenant-Kontext darf nicht aus Flag-Konfiguration abgeleitet werden | `tenantId` aus Auth-Kontext | Pflicht |
| Sensitive Daten | Flags dürfen keine Secrets enthalten | API-Key, Token | Verboten |
| Logging | Flag-Werte dürfen keine vertraulichen Informationen enthalten | Kein Token in Flag-Namen | Pflicht |
| Ressourcenverbrauch | Aktivierte Features können Last erhöhen | Export, AI, Reporting | Limits nötig |
| Auditierbarkeit | Kritische Flag-Änderungen müssen nachvollziehbar sein | produktives Umschalten | Betriebskonzept nötig |

### 16.2 Ressourcenverbrauch

Ein statisch aktiviertes Feature kann die Last deutlich verändern. Beispiele sind Exporte, Recommender, PDF-Generierung, AI-Aufrufe, Hintergrundjobs oder externe API-Calls. Für solche Features müssen Timeouts, Rate Limits, Batch-Größen und Monitoring mitgedacht werden.

```yaml
features:
  invoice-export:
    enabled: true
    max-batch-size: 500
```

`max-batch-size` ist hier keine Dekoration. Es ist eine Schutzgrenze.

### 16.3 Mandantenfähigkeit

In SaaS-Systemen darf ein statisches Flag nicht darüber entscheiden, ob ein bestimmter Mandant fachlich Zugriff auf eine Funktion hat. Das gehört in ein Entitlement-Modell.

Falsch:

```yaml
features:
  tenant-acme-premium-dashboard-enabled: true
```

Besser:

```yaml
features:
  premium-dashboard:
    enabled: true
```

und zusätzlich im Fachmodell:

```java
if (!entitlements.hasAccess(tenantId, Feature.PREMIUM_DASHBOARD)) {
    throw new AccessDeniedException("Tenant has no access to premium dashboard.");
}
```

---

## 17. Teststrategie

### 17.1 Bean-Auswahl testen

Für `@ConditionalOnProperty`-Konfigurationen eignet sich `ApplicationContextRunner`.

```java
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MailSenderConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(MailSenderConfiguration.class);

    @Test
    void createsSmtpMailSender_whenMockSenderIsDisabled() {
        contextRunner
                .withPropertyValues("features.mail.mock-sender-enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(SmtpMailSender.class);
                    assertThat(context).doesNotHaveBean(LoggingMailSender.class);
                });
    }

    @Test
    void createsLoggingMailSender_whenMockSenderIsEnabled() {
        contextRunner
                .withPropertyValues("features.mail.mock-sender-enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingMailSender.class);
                    assertThat(context).doesNotHaveBean(SmtpMailSender.class);
                });
    }
}
```

### 17.2 Verhalten testen

Wenn ein Service sein Verhalten anhand von Properties ändert, wird die Properties-Klasse direkt instanziiert.

```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceExportServiceTest {

    @Test
    void export_returnsDisabledResult_whenFeatureIsDisabled() {
        var properties = new InvoiceExportProperties(false, 500, false);
        var service = new InvoiceExportService(properties);

        var result = service.export(new ExportCommand());

        assertThat(result.enabled()).isFalse();
        assertThat(result.reason()).contains("disabled");
    }

    @Test
    void export_startsExport_whenFeatureIsEnabled() {
        var properties = new InvoiceExportProperties(true, 500, false);
        var service = new InvoiceExportService(properties);

        var result = service.export(new ExportCommand());

        assertThat(result.enabled()).isTrue();
    }
}
```

### 17.3 Tests mit Profilen

Profilabhängige Konfigurationen dürfen getestet werden, wenn sie betriebsrelevant sind. Für einfache Unit Tests ist direkte Instanziierung meist besser als ein voller Spring Context.

---

## 18. Anti-Patterns

### 18.1 Flag-Wildwuchs

Viele alte Flags bleiben im Code, obwohl sie längst dauerhaft aktiviert oder deaktiviert sind.

```java
if (features.newCheckoutEnabled()) {
    return newCheckout.execute(command);
}
return legacyCheckout.execute(command);
```

Wenn `newCheckoutEnabled` seit Monaten überall aktiv ist, muss der Legacy-Pfad gelöscht werden.

### 18.2 Flag-Kombinationshölle

```java
if (a && !b || c && d || e) {
    // unklarer Pfad
}
```

Mehrere Flags in einer Bedingung sind ein Warnsignal. Die Logik muss in benannte Methoden, Router oder Strategien ausgelagert werden.

```java
if (checkoutFeatures.shouldUseNewCheckoutFor(command)) {
    return newCheckout.execute(command);
}
```

### 18.3 Feature Flags im Domain Model

```java
public class Order {

    public void cancel(FeatureProperties features) {
        if (features.newCancellationEnabled()) {
            // ...
        }
    }
}
```

Das Domain Model sollte nicht von Spring-Konfiguration abhängig sein. Feature-Routing gehört in Application Services oder Router, nicht in Entities oder Value Objects.

### 18.4 Statische Flags als Testdatenersatz

```yaml
features:
  always-return-test-user: true
```

Testhilfen gehören in Testkonfiguration, Test Fixtures oder lokale Mocks, nicht in produktive Feature-Konfiguration.

### 18.5 Flags ohne Entfernungskriterium

```yaml
features:
  new-checkout-flow:
    enabled: true
```

Wenn dieses Flag ein temporäres Release Flag ist, braucht es ein Entfernungskriterium. Sonst wird es technische Schuld.

---

## 19. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Zweck | Ist klar, warum das Flag existiert? | „Invoice Export initial deaktivieren“ | Pflicht |
| Typ | Ist es wirklich ein statisches Flag? | Startzeitentscheidung | Pflicht |
| Namespace | Liegt es unter `features.*`? | `features.invoice-export.enabled` | Pflicht |
| Name | Ist der Name sprechend und stabil? | `new-flow-enabled` | Pflicht |
| Zugriff | Wird `@ConfigurationProperties` oder `@ConditionalOnProperty` genutzt? | Kein verstreutes `@Value` | Pflicht |
| Default | Ist der Default sicher? | `enabled=false` für neues Feature | Pflicht |
| Tests | Gibt es Tests für aktiv und deaktiviert? | zwei Testfälle | Pflicht |
| Security | Ersetzt das Flag keine Autorisierung? | Admin-Aktion | Pflicht |
| SaaS | Versteckt das Flag keine Mandantenlogik? | kein `tenant-a-*` | Pflicht |
| Secrets | Enthält das Flag keine Secrets? | kein API-Key | Pflicht |
| Lifecycle | Gibt es Entfernungskriterium? | nach Vollaktivierung löschen | Pflicht für temporäre Flags |
| Monitoring | Ist Betriebswirkung beobachtbar? | Export-Last, Fehlerquote | Pflicht bei Last-/Ops-Features |

---

## 20. Automatisierbare Prüfungen

Folgende Prüfungen können über ArchUnit, Semgrep, Checkstyle, YAML-Linting oder CI-Skripte umgesetzt werden:

```text
- Keine @Value-Felder mit "features." in Service-Klassen.
- Keine Feature-Keys außerhalb von features.*.
- Keine Feature-Keys mit "password", "secret", "token", "api-key".
- Keine tenant-spezifischen Feature-Key-Namen.
- Jede @ConfigurationProperties-Klasse unter features.* muss Tests besitzen.
- Jede @ConditionalOnProperty-Nutzung muss havingValue explizit setzen.
- matchIfMissing=true muss im Review begründet werden.
- Temporäre Release Flags müssen ein Entfernungsticket oder Ablaufdatum besitzen.
```

Beispielhafte ArchUnit-Regel:

```java
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Value;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

class FeatureFlagArchitectureTest {

    @ArchTest
    static final ArchRule services_should_not_inject_feature_flags_with_value =
            noFields()
                    .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                    .should().beAnnotatedWith(Value.class)
                    .because("Feature Flags sollen in Services nicht über verstreutes @Value gelesen werden.");
}
```

---

## 21. Migration bestehender statischer Flags

Bestehende statische Flags werden nach folgendem Vorgehen bereinigt.

### Schritt 1: Inventar erstellen

Alle Properties suchen:

```bash
grep -R "features\." src/main src/test
grep -R "@Value" src/main/java
grep -R "enabled" src/main/resources/application*.yml
```

### Schritt 2: Namespace vereinheitlichen

Vorher:

```yaml
newCheckout: true
useMockMail: false
```

Nachher:

```yaml
features:
  checkout:
    new-flow-enabled: true
  mail:
    mock-sender-enabled: false
```

### Schritt 3: `@ConfigurationProperties` einführen

```java
@ConfigurationProperties(prefix = "features.checkout")
public record CheckoutFeatureProperties(boolean newFlowEnabled) {}
```

### Schritt 4: Fachcode entkoppeln

Vorher:

```java
@Value("${newCheckout:false}")
private boolean newCheckout;
```

Nachher:

```java
private final CheckoutFeatureProperties checkoutFeatures;
```

### Schritt 5: Tests ergänzen

Für jedes relevante Flag werden aktivierter und deaktivierter Zustand getestet.

### Schritt 6: Temporäre Flags entfernen

Wenn ein Release Flag vollständig aktiviert wurde, wird der alte Codepfad gelöscht, das Flag aus `application.yml` entfernt und die Tests werden bereinigt.

---

## 22. Entscheidungsbaum

```text
Ist die Entscheidung beim Start der Anwendung ausreichend?
├─ Nein → dynamisches Feature-Flag-System, OpenFeature, Unleash, FF4J oder operative Control Plane verwenden.
└─ Ja
   ├─ Geht es um Bean-Auswahl?
   │  └─ Ja → @ConditionalOnProperty verwenden.
   ├─ Geht es um mehrere zusammengehörige Werte?
   │  └─ Ja → @ConfigurationProperties verwenden.
   ├─ Geht es um Nutzer-, Tenant- oder Prozent-Rollout?
   │  └─ Ja → statisches Flag nicht verwenden.
   ├─ Geht es um Autorisierung?
   │  └─ Ja → Permission-/Entitlement-Modell verwenden.
   └─ Geht es um lokale/technische Umgebungskonfiguration?
      └─ Ja → statisches Flag über application.yml ist geeignet.
```

---

## 23. Definition of Done

Ein statisches Feature Flag erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Das Flag liegt unter `features.*`.
2. Der Name beschreibt Kontext und Zweck.
3. Das Flag wird nicht als Secret-Speicher verwendet.
4. Das Flag ersetzt keine Autorisierung und keine Mandantentrennung.
5. Der Zugriff erfolgt über `@ConfigurationProperties`, `@ConditionalOnProperty` oder eine zentrale Feature-Konfiguration.
6. Es gibt keine verstreuten direkten `@Value`-Zugriffe im Fachcode.
7. Der Default-Wert ist bewusst und sicher gewählt.
8. Aktivierter und deaktivierter Zustand sind getestet.
9. Temporäre Flags besitzen ein Entfernungskriterium.
10. Betriebsrelevante Flags haben Monitoring-, Timeout- oder Ressourcenüberlegungen.
11. `matchIfMissing=true` ist nur dort verwendet, wo ein fehlender Wert wirklich sicher ist.
12. Die Flag-Nutzung ist im Code Review nachvollziehbar.

---

## 24. Quellen

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| Externe Konfiguration | Spring Boot erlaubt Konfiguration über YAML, Properties, Umgebungsvariablen und Command-Line-Argumente. | `application.yml`, ENV Override | https://docs.spring.io/spring-boot/reference/features/external-config.html |
| Profile | Spring Profiles trennen Teile der Konfiguration nach Umgebung. | `local`, `test`, `staging`, `prod` | https://docs.spring.io/spring-boot/reference/features/profiles.html |
| `@ConditionalOnProperty` | Bedingte Bean-Registrierung abhängig von Property-Werten. | `havingValue`, `matchIfMissing` | https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/condition/ConditionalOnProperty.html |
| Application Properties | Spring Boot dokumentiert Properties in `application.properties` und `application.yaml`. | Common Application Properties | https://docs.spring.io/spring-boot/appendix/application-properties/index.html |
| Secrets Management | Secrets müssen zentral, kontrolliert und auditierbar verwaltet werden. | API-Key, Token, Passwort | https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html |
| OpenFeature | Vendor-neutrale API für dynamisches Feature Flagging. | dynamische Provider-Abstraktion | https://openfeature.dev/ |

---
