# QG-JAVA-039 — Feature Flags: Deployment von Release entkoppeln

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-039 |
| Titel | Feature Flags: Deployment von Release entkoppeln |
| Status | Accepted / verbindlicher Standard für neue und wesentlich überarbeitete Spring-Boot-Services |
| Sprache | Deutsch |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x; optional Unleash, FF4J oder OpenFeature-kompatibler Provider |
| Kategorie | DevOps / Release Management / Betriebssteuerung / SaaS-Plattformqualität |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, DevOps Engineers, Platform Engineers, Security Reviewer, Product Owner |
| Verbindlichkeit | Feature Flags MÜSSEN bewusst typisiert, zentral ausgewertet, getestet, überwacht und entfernt werden. Flag-Abfragen DÜRFEN NICHT ungeordnet im Business-Code verstreut werden. Feature Flags DÜRFEN Autorisierung, Mandantenprüfung oder Sicherheitskontrollen NICHT ersetzen. |
| Qualitätsziel | Deployment und Release sollen entkoppelt werden, ohne Codequalität, Nachvollziehbarkeit, Mandantentrennung, Testbarkeit oder Betriebssicherheit zu verschlechtern. |
| Prüfstatus | Inhalt fachlich gegen Spring-Boot-`@ConditionalOnProperty`-Dokumentation, Unleash-Java-SDK-/Feature-Flag-Dokumentation, OpenFeature, OWASP API Security und OWASP Logging Cheat Sheet validiert. |
| Letzte fachliche Prüfung | 2026-05-02 |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Feature Flags in Java- und Spring-Boot-Systemen eingesetzt werden, damit Deployment und Release sauber voneinander getrennt werden können.

Ohne Feature Flags bedeutet ein Deployment häufig automatisch: neue Funktionalität ist für Nutzer sichtbar und wirksam. Dadurch werden Deployments riskanter, größere Änderungen werden länger zurückgehalten, Feature Branches wachsen, Integrationskonflikte nehmen zu und Rollbacks werden organisatorisch schwerer.

Mit Feature Flags kann Code bereits produktiv deployed werden, ohne sofort für alle Nutzer aktiviert zu sein. Ein Feature kann zunächst deaktiviert bleiben, für interne Nutzer aktiviert werden, schrittweise für Nutzergruppen freigeschaltet werden oder bei Problemen sofort wieder deaktiviert werden. Dadurch werden kleine, häufige Deployments, Trunk-Based Development, Canary Releases und Kill-Switches praktikabler.

Diese Richtlinie macht jedoch auch klar: Feature Flags sind kein kostenloser Qualitätsgewinn. Jeder Flag erzeugt zusätzliche Pfade im Code, zusätzliche Testkombinationen, zusätzliche Betriebszustände und zusätzliche Verantwortung. Ein Feature Flag ohne Besitzer, Ablaufdatum, Monitoring und Entfernungspfad ist technische Schuld.

---

## 2. Kurzregel für Entwickler

Verwende Feature Flags, wenn Deployment und Aktivierung einer Funktion bewusst getrennt werden müssen. Jede Flag-Abfrage MUSS über einen zentralen `FeatureFlagService` oder eine vergleichbare Abstraktion erfolgen. Jeder Flag MUSS einen Typ, einen Besitzer, einen sicheren Default, einen erwarteten Lebenszyklus und eine Teststrategie besitzen.

Feature Flags DÜRFEN NICHT verwendet werden, um fehlendes Design, fehlende Autorisierung, fehlende Mandantentrennung oder fehlende Konfigurationsdisziplin zu verstecken.

Kurzform:

```text
Feature Flags steuern Aktivierung.
Feature Flags ersetzen keine Berechtigung.
Feature Flags ersetzen keine Mandantentrennung.
Feature Flags ersetzen keine Tests.
Feature Flags müssen wieder entfernt werden.
```

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Spring-Boot-Services,
- Java-Backend-Services,
- SaaS-Plattformen mit Mandantenfähigkeit,
- API-Features,
- UI-sichtbare Backend-Funktionen,
- schrittweise Rollouts,
- Canary Releases,
- Kill-Switches,
- Betriebsflags,
- Migrationsflags,
- Experiment- und Variantensteuerung,
- Produkt- und Entitlement-nahe Freischaltungen.

Diese Richtlinie gilt nicht als vollständige Anleitung für:

- wissenschaftlich saubere A/B-Test-Auswertung,
- Produktanalyse und Statistik,
- vollständige Experimentation-Plattformen,
- Kubernetes-Traffic-Splitting,
- Blue-Green-Deployment als Infrastrukturverfahren,
- vollständige Berechtigungsmodelle,
- Lizenzmanagement,
- Mandantenabrechnung.

Diese Themen können Feature Flags nutzen, benötigen aber jeweils zusätzliche Architektur- und Governance-Regeln.

---

## 4. Technischer Hintergrund

Ein Feature Flag ist eine kontrollierbare Entscheidung im Code, mit der ein bestimmter Codepfad aktiviert, deaktiviert oder variiert werden kann, ohne den Anwendungscode neu auszuliefern.

Die wichtigste Unterscheidung lautet:

```text
Deployment = Code wird in eine Umgebung ausgeliefert.
Release    = Funktionalität wird für Nutzer oder Systeme wirksam aktiviert.
```

Feature Flags entkoppeln diese beiden Vorgänge. Dadurch kann ein Team häufiger deployen und trotzdem kontrollieren, wann eine Funktion sichtbar oder wirksam wird.

Technisch gibt es unterschiedliche Stufen:

1. **statische Flags** über Konfiguration, zum Beispiel `application.yml`,
2. **dynamische Flags** über einen zentralen Feature-Flag-Provider,
3. **kontextabhängige Flags** nach Nutzer, Mandant, Rolle, Segment, Umgebung oder Prozentrollout,
4. **Variantenflags** für Experimente oder alternative Implementierungen,
5. **Betriebsflags** für Kill-Switches, Lastschutz oder Degradation.

Spring Boot `@ConditionalOnProperty` ist gut für statische Bean-Entscheidungen beim Anwendungsstart. Es ist nicht geeignet, wenn ein Feature während des laufenden Betriebs dynamisch aktiviert oder deaktiviert werden soll.

Ein System wie Unleash oder FF4J ist geeignet, wenn Flags zur Laufzeit geändert, überwacht, segmentiert und mit unterschiedlichen Strategien pro Umgebung gesteuert werden sollen. OpenFeature kann als vendor-neutrale Abstraktion dienen, wenn Provider-Austauschbarkeit ein wichtiges Architekturziel ist.

---

## 5. Feature-Flag-Typen

Nicht alle Feature Flags sind gleich. Diese Unterscheidung ist verbindlich, weil Typ, Lebensdauer, Testumfang und Risiko unterschiedlich sind.

| Flag-Typ | Zweck | Typische Lebensdauer | Beispiel | Qualitätsregel |
|---|---|---:|---|---|
| Release Flag | Unfertige oder neue Funktionalität kontrolliert freigeben | kurz | neuer Checkout Flow | MUSS nach vollständiger Aktivierung entfernt werden |
| Experiment Flag | Varianten für A/B- oder multivariate Tests ausspielen | kurz | Suchranking A gegen B | MUSS Messkonzept, Zielmetrik und Auswertungszeitraum haben |
| Operational Flag | Technische Implementierung, Migration oder Betriebsverhalten steuern | kurz bis mittel | neuer Suchindex, neuer Payment Provider | MUSS mit Monitoring und Rollback-Plan verbunden sein |
| Kill Switch | Problematische Funktion im Betrieb schnell deaktivieren | dauerhaft möglich | AI-Recommendations abschalten bei Kostenanstieg | MUSS klar dokumentiert, überwacht und schnell bedienbar sein |
| Permission / Entitlement Flag | Produktzugang nach Paket, Rolle oder Mandant steuern | längerfristig | Premium-Funktion für Enterprise-Mandanten | DARF Autorisierung nicht ersetzen; MUSS mit Berechtigungssystem zusammenspielen |
| Migration Flag | Daten-, API- oder Infrastrukturwechsel schrittweise steuern | kurz bis mittel | neuer Read-Model-Pfad | MUSS Abschlusskriterien und Entferndatum haben |
| Developer Flag | Lokale oder interne Entwicklerfunktion steuern | sehr kurz | Debug-Ansicht, interne Testfunktion | DARF nie ungeprüft produktiv aktiv sein |

Unleash verwendet ebenfalls Flag-Typen mit erwarteter Lebensdauer, zum Beispiel Release, Experiment, Operational, Kill switch und Permission. Diese Einordnung ist nützlich, weil alte Flags automatisch als potenziell veraltet markiert werden können.

---

## 6. Verbindlicher Standard

Für neue Feature Flags gelten folgende Regeln:

1. Jeder Feature Flag MUSS einen stabilen, sprechenden Namen haben.
2. Jeder Feature Flag MUSS einem Typ zugeordnet sein.
3. Jeder Feature Flag MUSS einen fachlichen oder technischen Besitzer haben.
4. Jeder temporäre Feature Flag MUSS ein geplantes Entferndatum oder ein klares Entfernungskriterium besitzen.
5. Jede Flag-Auswertung MUSS über eine zentrale Abstraktion erfolgen.
6. Flag-Namen DÜRFEN NICHT als freie Strings im gesamten Code verstreut werden.
7. Der sichere Default MUSS bewusst festgelegt werden.
8. Beide Pfade, aktiviert und deaktiviert, MÜSSEN getestet werden, solange beide produktiv möglich sind.
9. Kritische Flags MÜSSEN beobachtbar sein: Metrik, Log, Audit-Ereignis oder Dashboard.
10. Feature Flags DÜRFEN keine Berechtigungsprüfung ersetzen.
11. Feature Flags DÜRFEN keine Mandantentrennung ersetzen.
12. Vollständig aktivierte Release Flags MÜSSEN entfernt werden.

---

## 7. Naming-Standard

Feature-Flag-Namen müssen stabil, klein geschrieben, technisch eindeutig und produktübergreifend verständlich sein.

Empfohlenes Format:

```text
<domain>.<capability>.<purpose>
```

Beispiele:

```text
checkout.new-flow.enabled
checkout.discount-v2.enabled
recommendation.ai.enabled
search.opensearch-read-path.enabled
payment.provider-stripe-v2.enabled
order.kill-switch.external-tax-calculation
```

Nicht erlaubt:

```text
newFeature
flag1
testFlag
maxFeature
checkoutNew
useNewThing
```

Ein Flag-Name SOLL nicht den Sprint, das Ticket oder den Entwicklernamen enthalten. Ticket- und Sprint-Informationen gehören in die Flag-Metadaten, nicht in den technischen Flag-Namen.

---

## 8. Gute Anwendung: statische Flags mit Spring Boot

Statische Flags eignen sich für Entscheidungen, die beim Start der Anwendung getroffen werden und während der Laufzeit nicht dynamisch wechseln müssen.

### 8.1 Konfiguration in `application.yml`

```yaml
features:
  recommendations:
    ai-enabled: false
  checkout:
    new-flow-enabled: false
```

### 8.2 Bean-Auswahl mit `@ConditionalOnProperty`

```java
@Component
@ConditionalOnProperty(
    name = "features.recommendations.ai-enabled",
    havingValue = "true"
)
public class AiRecommendationService implements RecommendationService {
    @Override
    public List<Recommendation> recommendFor(UserId userId) {
        // AI-basierte Empfehlungen
        return List.of();
    }
}

@Component
@ConditionalOnProperty(
    name = "features.recommendations.ai-enabled",
    havingValue = "false",
    matchIfMissing = true
)
public class BasicRecommendationService implements RecommendationService {
    @Override
    public List<Recommendation> recommendFor(UserId userId) {
        // stabile Standardempfehlungen
        return List.of();
    }
}
```

Diese Variante ist gut, wenn die Entscheidung pro Deployment-Umgebung stabil ist. Sie ist nicht gut, wenn Product Owner, SRE oder Support im laufenden Betrieb umschalten müssen.

### 8.3 Typischer Einsatzzweck

Geeignet:

- alternative Infrastruktur-Beans,
- aktivierte/deaktivierte Integrationen pro Umgebung,
- lokale Entwicklungsfeatures,
- technische Migrationspfade beim Start,
- optionale Scheduler,
- optionale Adapter.

Nicht geeignet:

- Canary Rollout,
- Nutzersegmentierung,
- A/B-Test,
- Kill Switch während Incident,
- dynamisches Abschalten bei Last,
- Mandanten- oder Nutzer-spezifische Aktivierung.

---

## 9. Gute Anwendung: zentraler FeatureFlagService

Flag-Abfragen dürfen nicht ungeordnet im Code stehen. Der Anwendungscode soll nicht direkt von Unleash, FF4J oder einem anderen Provider abhängen. Stattdessen wird eine zentrale Abstraktion verwendet.

### 9.1 Feature-Flag-Enum

```java
public enum FeatureFlag {

    CHECKOUT_NEW_FLOW("checkout.new-flow.enabled", FlagType.RELEASE),
    CHECKOUT_DISCOUNT_V2("checkout.discount-v2.enabled", FlagType.RELEASE),
    AI_RECOMMENDATIONS("recommendation.ai.enabled", FlagType.KILL_SWITCH),
    OPENSEARCH_READ_PATH("search.opensearch-read-path.enabled", FlagType.MIGRATION);

    private final String key;
    private final FlagType type;

    FeatureFlag(String key, FlagType type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return key;
    }

    public FlagType type() {
        return type;
    }
}
```

```java
public enum FlagType {
    RELEASE,
    EXPERIMENT,
    OPERATIONAL,
    KILL_SWITCH,
    PERMISSION,
    MIGRATION,
    DEVELOPER
}
```

### 9.2 Kontextobjekt

```java
public record FeatureContext(
    UserId userId,
    TenantId tenantId,
    String environment,
    Set<String> roles
) {
    public FeatureContext {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        roles = Set.copyOf(roles == null ? Set.of() : roles);
    }
}
```

Der Mandant darf niemals aus einem ungeprüften Request-Feld übernommen werden. `tenantId` MUSS aus dem authentifizierten Sicherheitskontext, aus dem geprüften Routing-Kontext oder aus einer serverseitig validierten Mandantenauflösung stammen.

### 9.3 Zentrale Abstraktion

```java
public interface FeatureFlagService {

    boolean isEnabled(FeatureFlag flag, FeatureContext context);

    default boolean isDisabled(FeatureFlag flag, FeatureContext context) {
        return !isEnabled(flag, context);
    }
}
```

### 9.4 Provider-Implementierung

```java
@Service
public class UnleashFeatureFlagService implements FeatureFlagService {

    private final Unleash unleash;

    public UnleashFeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }

    @Override
    public boolean isEnabled(FeatureFlag flag, FeatureContext context) {
        var unleashContext = UnleashContext.builder()
            .userId(context.userId() == null ? null : context.userId().value().toString())
            .addProperty("tenantId", context.tenantId().value().toString())
            .addProperty("environment", context.environment())
            .build();

        return unleash.isEnabled(flag.key(), unleashContext, safeDefaultFor(flag));
    }

    private boolean safeDefaultFor(FeatureFlag flag) {
        return switch (flag.type()) {
            case RELEASE, EXPERIMENT, MIGRATION, DEVELOPER -> false;
            case OPERATIONAL, KILL_SWITCH -> false;
            case PERMISSION -> false;
        };
    }
}
```

Der Default `false` ist für neue Funktionen in der Regel der sicherste Zustand. Bei besonderen Betriebsflags kann ein anderer Default fachlich richtig sein, muss aber explizit dokumentiert werden.

---

## 10. Unleash-Konfiguration für Spring Boot

Eine typische Unleash-Konfiguration liest URL und Token aus Konfiguration oder Secret Management. Der API Token darf nicht im Quellcode stehen.

```yaml
feature-flags:
  unleash:
    api-url: ${UNLEASH_API_URL}
    api-token: ${UNLEASH_API_TOKEN}
    app-name: order-service
    instance-id: ${HOSTNAME:local-dev}
```

```java
@Configuration
public class UnleashClientConfiguration {

    @Bean
    Unleash unleash(FeatureFlagProperties properties) {
        var config = UnleashConfig.builder()
            .appName(properties.unleash().appName())
            .instanceId(properties.unleash().instanceId())
            .unleashAPI(properties.unleash().apiUrl())
            .apiKey(properties.unleash().apiToken())
            .synchronousFetchOnInitialisation(true)
            .build();

        return new DefaultUnleash(config);
    }
}
```

Für produktive Systeme gilt:

- Token kommen aus Secret Management, nicht aus Git.
- Client-Tokens haben nur die minimal nötigen Rechte.
- Development-, Staging- und Production-Umgebungen verwenden getrennte Tokens.
- Flag-Konfigurationen werden pro Umgebung kontrolliert.
- Änderungen an produktionskritischen Flags sind auditierbar.

---

## 11. Gute Anwendung: Strategy Pattern statt verstreuter if-Abfragen

### Schlecht: Flag-Check mitten in der Geschäftslogik

```java
@Service
public class CheckoutService {

    private final FeatureFlagService features;

    public CheckoutResult checkout(CheckoutCommand command) {
        if (features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, command.context())) {
            // neuer Checkout
            // weitere verschachtelte Bedingungen
            // Sonderfälle
        } else {
            // alter Checkout
            // weitere verschachtelte Bedingungen
        }

        if (features.isEnabled(FeatureFlag.CHECKOUT_DISCOUNT_V2, command.context())) {
            // neuer Rabattpfad
        }

        // Nach wenigen Wochen ist nicht mehr klar, welche Kombinationen möglich sind.
        return CheckoutResult.success();
    }
}
```

### Gut: Flag entscheidet über Strategie, nicht über Detailchaos

```java
public interface CheckoutStrategy {
    CheckoutResult execute(CheckoutCommand command);
}

@Service
public class LegacyCheckoutStrategy implements CheckoutStrategy {
    @Override
    public CheckoutResult execute(CheckoutCommand command) {
        return CheckoutResult.success();
    }
}

@Service
public class NewCheckoutStrategy implements CheckoutStrategy {
    @Override
    public CheckoutResult execute(CheckoutCommand command) {
        return CheckoutResult.success();
    }
}
```

```java
@Service
public class CheckoutService {

    private final CheckoutStrategy legacyCheckout;
    private final CheckoutStrategy newCheckout;
    private final FeatureFlagService features;

    public CheckoutService(
            LegacyCheckoutStrategy legacyCheckout,
            NewCheckoutStrategy newCheckout,
            FeatureFlagService features) {
        this.legacyCheckout = legacyCheckout;
        this.newCheckout = newCheckout;
        this.features = features;
    }

    public CheckoutResult checkout(CheckoutCommand command) {
        var strategy = features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, command.context())
            ? newCheckout
            : legacyCheckout;

        return strategy.execute(command);
    }
}
```

Das ist testbar, lesbar und später leicht zu entfernen. Nach vollständigem Rollout wird die alte Strategie gelöscht, der Flag entfernt und der Service vereinfacht.

---

## 12. Gute Anwendung: Kill Switch mit kontrollierter Degradation

Ein Kill Switch ist kein normaler Release Flag. Er ist ein bewusstes Betriebsinstrument, um eine Funktion bei Fehlern, Kostenexplosion, Latenzproblemen oder externen Störungen schnell abzuschalten.

```java
@Service
public class RecommendationFacade {

    private final FeatureFlagService features;
    private final AiRecommendationClient aiClient;
    private final BasicRecommendationService fallback;

    public RecommendationFacade(
            FeatureFlagService features,
            AiRecommendationClient aiClient,
            BasicRecommendationService fallback) {
        this.features = features;
        this.aiClient = aiClient;
        this.fallback = fallback;
    }

    public List<Recommendation> recommendFor(UserId userId, FeatureContext context) {
        if (features.isDisabled(FeatureFlag.AI_RECOMMENDATIONS, context)) {
            return fallback.recommendFor(userId);
        }

        try {
            return aiClient.recommendFor(userId);
        } catch (AiProviderUnavailableException ex) {
            return fallback.recommendFor(userId);
        }
    }
}
```

Wichtig: Der Fallback muss fachlich zulässig sein. Ein Kill Switch darf keine Sicherheitskontrolle abschalten und darf keine inkonsistenten Datenzustände erzeugen.

---

## 13. Falsche Anwendung und Anti-Patterns

### 13.1 Flag-Abfragen überall im Code

```java
if (unleash.isEnabled("new-checkout-flow")) {
    // ...
}
```

Problem:

- freie Strings sind refactoring-unsicher,
- der Provider ist überall gekoppelt,
- Flag-Nutzung ist schwer auffindbar,
- Entfernung wird teuer,
- Tests werden unübersichtlich.

Korrektur:

```java
features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, context)
```

### 13.2 Feature Flag als Berechtigung

```java
if (features.isEnabled(FeatureFlag.ADMIN_DASHBOARD, context)) {
    return adminDataService.loadAllData();
}
```

Das ist gefährlich, wenn keine echte Autorisierung erfolgt. Feature Flags können steuern, ob eine Funktion sichtbar oder aktiv ist. Sie dürfen aber nicht allein entscheiden, ob ein Nutzer auf geschützte Daten zugreifen darf.

Korrektur:

```java
authorization.requirePermission(userId, Permission.ADMIN_DASHBOARD_READ);

if (features.isEnabled(FeatureFlag.ADMIN_DASHBOARD, context)) {
    return adminDataService.loadPermittedDataFor(userId);
}

return DashboardResponse.notAvailable();
```

### 13.3 Client-side Flag als Schutz für Backend-Funktion

Ein im Browser deaktivierter Button schützt keine API. Angreifer können Requests direkt senden.

Falsch:

```text
Button im Frontend versteckt = Funktion ist geschützt.
```

Richtig:

```text
Frontend-Flag verbessert UX.
Backend prüft Berechtigung und Mandantentrennung immer selbst.
```

### 13.4 Dauerhafte Release Flags

Release Flags sind temporär. Wenn ein Feature vollständig ausgerollt ist und der alte Pfad nicht mehr benötigt wird, muss der Flag entfernt werden.

Falsch:

```java
// Seit 18 Monaten im Code
if (features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, context)) {
    return newCheckout.execute(command);
}
return legacyCheckout.execute(command);
```

Richtig:

```java
return newCheckout.execute(command);
```

### 13.5 Flag-Kombinationshölle

```java
if (flagA && !flagB || flagC && user.isPremium() && !tenant.isMigrated()) {
    // Niemand kann sicher sagen, welcher Zustand hier gemeint ist.
}
```

Korrektur:

- Flag-Entscheidung in benannte Methode auslagern,
- Kombinationen reduzieren,
- Strategien oder Entscheidungsobjekte verwenden,
- kritische Kombinationen explizit testen.

```java
boolean shouldUseNewCheckout(CheckoutCommand command) {
    return features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, command.context())
        && command.tenant().isMigrated();
}
```

### 13.6 Feature Flag ohne Monitoring

Ein Flag, der produktives Verhalten ändert, aber keine Metriken erzeugt, ist schwer steuerbar.

Mindestens erforderlich:

- Aktivierungsstatus pro Umgebung,
- Fehlerquote pro Pfad,
- Latenz pro Pfad,
- Nutzungsvolumen pro Pfad,
- Kostenwirkung bei externen Providern,
- Rückfallrate bei Fallbacks.

### 13.7 Feature Flag ohne Test beider Pfade

Solange beide Pfade produktiv möglich sind, müssen beide Pfade getestet werden.

```java
@Test
void checkout_usesNewFlow_whenFlagIsEnabled() {
    when(features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, context)).thenReturn(true);

    checkoutService.checkout(command);

    verify(newCheckout).execute(command);
    verifyNoInteractions(legacyCheckout);
}

@Test
void checkout_usesLegacyFlow_whenFlagIsDisabled() {
    when(features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, context)).thenReturn(false);

    checkoutService.checkout(command);

    verify(legacyCheckout).execute(command);
    verifyNoInteractions(newCheckout);
}
```

---

## 14. Security- und SaaS-Relevanz

Feature Flags beeinflussen produktives Verhalten. Deshalb sind sie sicherheits- und betriebsrelevant.

### 14.1 Feature Flags ersetzen keine Autorisierung

Ein Permission-Flag kann Produktzugang steuern, aber es ist keine vollständige Berechtigungsprüfung. Autorisierung muss weiterhin serverseitig durch ein geprüftes Permission-, Rollen- oder Policy-Modell erfolgen.

Pflichtregel:

```text
Erst Berechtigung prüfen, dann Feature-Flag auswerten.
```

### 14.2 Mandantentrennung darf nicht vom Flag abhängen

In SaaS-Systemen darf ein Feature Flag niemals dazu führen, dass Daten eines falschen Mandanten gelesen, geschrieben oder angezeigt werden.

Falsch:

```java
var tenantId = request.tenantId();
var context = new FeatureContext(userId, tenantId, environment, roles);
```

Richtig:

```java
var tenantId = tenantContext.currentTenantId();
var context = new FeatureContext(userId, tenantId, environment, roles);
```

Der Mandant kommt aus einem geprüften serverseitigen Kontext, nicht aus einem ungeprüften Request-Feld.

### 14.3 API Tokens und SDK Keys sind Secrets

Provider-Tokens für Unleash, FF4J oder andere Systeme dürfen nicht in Git, Logs, Stacktraces oder Testdaten stehen.

Pflichtregeln:

- Tokens kommen aus Secret Management oder sicherer Runtime-Konfiguration.
- Production Tokens sind getrennt von Development Tokens.
- Tokens haben minimale Rechte.
- Token-Rotation ist möglich.
- Flag-Konfigurationsänderungen sind auditierbar.

### 14.4 Feature Flags können Ressourcenverbrauch erhöhen

Ein falsch aktivierter Flag kann teure externe Calls, große Queries, KI-Kosten, Mailversand, SMS-Versand oder zusätzliche CPU-/Speicherlast verursachen.

Deshalb braucht jeder Betriebs- oder Release-Flag für ressourcenintensive Funktionalität:

- Rate Limits,
- Timeouts,
- Circuit Breaker,
- Bulkheads,
- Kostenmetriken,
- Tenant-Limits,
- Fallback-Verhalten.

### 14.5 Logs dürfen keine sensiblen Kontextdaten enthalten

Flag-Entscheidungen dürfen protokolliert werden, aber nicht mit Passwörtern, Tokens, personenbezogenen Detaildaten oder vollständigen Payloads.

Gut:

```text
feature=checkout.new-flow.enabled enabled=true tenantHash=4f9a userSegment=premium environment=prod
```

Schlecht:

```text
feature=admin.enabled enabled=true email=max@example.com jwt=eyJhbGciOi...
```

---

## 15. Teststrategie

Feature Flags erhöhen die Zahl möglicher Ausführungspfade. Deshalb braucht jeder Flag eine explizite Teststrategie.

### 15.1 Unit Tests

Unit Tests prüfen die lokale Entscheidung und beide Pfade.

```java
@Test
void checkout_usesNewStrategy_whenFlagIsEnabled() {
    when(features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, context)).thenReturn(true);

    checkoutService.checkout(command);

    verify(newCheckout).execute(command);
    verifyNoInteractions(legacyCheckout);
}
```

### 15.2 Integrationstests

Integrationstests prüfen, dass Konfiguration, Provider, Spring Context und relevante Endpunkte korrekt zusammenspielen.

Beispiel für statische Properties:

```java
@SpringBootTest(properties = "features.recommendations.ai-enabled=false")
class RecommendationConfigurationTest {

    @Autowired RecommendationService recommendationService;

    @Test
    void usesBasicRecommendationService_whenAiFeatureIsDisabled() {
        assertThat(recommendationService)
            .isInstanceOf(BasicRecommendationService.class);
    }
}
```

### 15.3 Security Tests

Security Tests prüfen, dass deaktivierte UI- oder Feature-Pfade keine Berechtigungsprüfung umgehen.

Mindestens testen:

- Nutzer ohne Berechtigung erhält keinen Zugriff, auch wenn Flag aktiv ist.
- Nutzer mit Berechtigung erhält keinen Zugriff auf fremden Mandanten, auch wenn Flag aktiv ist.
- Deaktivierter Flag liefert kontrollierte Antwort, nicht Stacktrace oder undefinierten Zustand.
- Kill Switch deaktiviert Funktion, aber nicht Security-Kontrollen.

### 15.4 Produktionsnahe Tests

Für Canary- oder Migration-Flags braucht es produktionsnahe Validierung:

- Metriken vor Aktivierung,
- Aktivierung für interne Nutzer,
- Aktivierung für kleinen Prozentsatz,
- Vergleich alter/neuer Pfad,
- Rollback-Probe,
- Abschlussentscheidung.

---

## 16. Observability und Betrieb

Feature Flags müssen im Betrieb sichtbar sein.

Für kritische Flags SOLLEN folgende Signale verfügbar sein:

| Signal | Zweck | Beispiel |
|---|---|---|
| Flag-Status pro Umgebung | Verstehen, was aktiv ist | `checkout.new-flow.enabled=true in prod` |
| Aktivierungsrate | Rollout-Status erkennen | 5%, 25%, 50%, 100% |
| Fehlerquote pro Pfad | Neue Funktion bewerten | HTTP 5xx neuer Checkout vs. alter Checkout |
| Latenz pro Pfad | Performance vergleichen | p95/p99 |
| Business-Metrik | Wirkung bewerten | Conversion, Abbruchrate, Bestellabschluss |
| Kostenmetrik | Externe Kosten kontrollieren | KI-Provider-Kosten pro Mandant |
| Audit-Log | Änderungen nachvollziehen | Wer hat welchen Flag wann geändert? |

Feature-Flag-Änderungen in Produktion sind Konfigurationsänderungen mit Produkt- und Betriebswirkung. Sie müssen nachvollziehbar sein.

---

## 17. Lifecycle-Regeln

Jeder Flag muss einen Lebenszyklus besitzen.

### 17.1 Mindestmetadaten

```yaml
name: checkout.new-flow.enabled
type: release
owner: team-checkout
created: 2026-05-02
remove-by: 2026-06-15
default: false
fallback: legacy-checkout
jira: CHECKOUT-1234
risk: medium
monitoring: dashboard/checkout-new-flow
```

### 17.2 Lebenszyklus

```text
Define → Develop → Rollout → Full Activation → Cleanup → Removed
```

### 17.3 Cleanup-Regel

Release-, Experiment- und Migration-Flags sind temporär. Nach Abschluss müssen entfernt werden:

1. Flag-Check im Code,
2. alter Codepfad,
3. Tests für alten Pfad,
4. Flag-Konfiguration im Provider,
5. Dokumentation und Dashboards,
6. nicht mehr benötigte Metriken.

### 17.4 Stale-Flag-Regel

Ein Flag gilt als stale, wenn mindestens eine Bedingung erfüllt ist:

- erwartetes Entferndatum überschritten,
- Feature ist seit mindestens zwei Sprints vollständig aktiviert,
- alter Pfad wird produktiv nicht mehr genutzt,
- Owner ist unbekannt,
- keine Metriken zeigen Nutzung,
- Flag-Name ist im Provider archiviert, aber im Code noch vorhanden.

Stale Flags müssen im Review oder in der Pipeline sichtbar werden.

---

## 18. Automatisierbare Prüfungen

Feature-Flag-Qualität kann teilweise automatisiert werden.

### 18.1 Keine freien Flag-Strings

Semgrep, Checkstyle oder ArchUnit können direkte Provider-Abfragen oder String-Flags suchen.

Beispiele für verbotene Muster:

```java
unleash.isEnabled("...")
ff4j.check("...")
featureManager.isActive("...")
```

Erlaubt ist nur die zentrale Abstraktion:

```java
features.isEnabled(FeatureFlag.CHECKOUT_NEW_FLOW, context)
```

### 18.2 Stale-Flag-Suche

Die Pipeline SOLL prüfen, ob im Code noch Flags verwendet werden, die im Provider als stale, archived oder removed markiert sind.

### 18.3 Mindestmetadaten

Für neue Flags SOLLTE automatisiert geprüft werden:

- Owner vorhanden,
- Typ vorhanden,
- Beschreibung vorhanden,
- erwartete Lebensdauer vorhanden,
- Default definiert,
- Umgebungskonfiguration vorhanden.

### 18.4 Testabdeckung beider Pfade

Vollständig automatisierbar ist das schwer. Im Review MUSS aber geprüft werden, ob beide produktiv möglichen Pfade getestet sind.

---

## 19. Migration bestehender Feature Flags

Bestehende ungeordnete Feature Flags werden schrittweise bereinigt.

### Schritt 1: Inventarisieren

Alle Flag-Keys im Code suchen:

```bash
grep -R "isEnabled(\|check(\|feature" src/main src/test
```

### Schritt 2: Klassifizieren

Jeden Flag einem Typ zuordnen:

- Release,
- Experiment,
- Operational,
- Kill Switch,
- Permission,
- Migration,
- Developer.

### Schritt 3: Owner und Status klären

Für jeden Flag klären:

- Wer besitzt den Flag?
- Ist er noch aktiv?
- Ist er vollständig ausgerollt?
- Ist der alte Pfad noch nötig?
- Gibt es Tests für beide Pfade?
- Gibt es Monitoring?

### Schritt 4: Zentralisieren

Freie Strings in `FeatureFlag`-Enum oder Registry überführen.

### Schritt 5: Entfernen

Vollständig ausgerollte temporäre Flags entfernen. Nicht nur deaktivieren. Entfernen.

---

## 20. Review-Checkliste

Bei jedem Pull Request mit Feature Flags müssen folgende Fragen beantwortet werden:

- Hat der Flag einen sprechenden Namen?
- Ist der Flag-Typ klar?
- Ist der Besitzer klar?
- Ist der sichere Default bewusst gewählt?
- Gibt es ein Entferndatum oder Abschlusskriterium?
- Wird der Flag über eine zentrale Abstraktion ausgewertet?
- Werden keine freien Flag-Strings im Business-Code verwendet?
- Sind beide Pfade getestet?
- Ist der deaktivierte Pfad fachlich sicher?
- Ist der aktivierte Pfad berechtigt und mandantensicher?
- Ersetzt der Flag keine Autorisierung?
- Kommt `tenantId` aus einem geprüften Kontext?
- Sind Monitoring und Rollback-Verhalten definiert?
- Werden Secrets oder Tokens nicht geloggt?
- Ist klar, wann der Flag wieder entfernt wird?

---

## 21. Ausnahmen

Ausnahmen sind zulässig, müssen aber begründet werden.

Zulässige Ausnahmen:

- sehr einfache lokale Developer-Flags,
- testinterne Flags,
- einmalige Migrationsskripte,
- statische Konfigurationsentscheidungen ohne Laufzeitumschaltung,
- externe Libraries, die eigene Flag-Mechanismen erzwingen.

Nicht zulässige Ausnahmen:

- Feature Flag statt Berechtigungsprüfung,
- Feature Flag statt Mandantenprüfung,
- produktive Flags ohne Owner,
- Release Flags ohne Entfernungspfad,
- direkte Provider-Kopplung in vielen Services,
- sensible Tokens im Code,
- nicht getestete aktivierte/deaktivierte Pfade.

---

## 22. Definition of Done

Ein Feature Flag erfüllt diese Richtlinie, wenn alle folgenden Kriterien erfüllt sind:

1. Der Flag hat einen stabilen, sprechenden Namen.
2. Der Flag ist typisiert.
3. Der Flag hat einen Besitzer.
4. Der Flag hat einen sicheren Default.
5. Temporäre Flags haben ein Entferndatum oder ein klares Abschlusskriterium.
6. Die Auswertung erfolgt zentral über `FeatureFlagService` oder eine gleichwertige Abstraktion.
7. Im Business-Code stehen keine freien Flag-Strings.
8. Beide produktiv möglichen Pfade sind getestet.
9. Der Flag ersetzt keine Berechtigungs- oder Mandantenprüfung.
10. Provider-Tokens werden sicher verwaltet.
11. Kritische Flags sind beobachtbar.
12. Vollständig ausgerollte Release Flags werden entfernt.

---

## 23. Quellen und weiterführende Literatur

- Spring Boot Documentation: `@ConditionalOnProperty`
- Unleash Documentation: Java SDK, Feature Flags, Activation Strategies, Feature Flag Lifecycle
- FF4J Documentation: Feature Flipping for Java
- OpenFeature Specification: vendor-neutrale Feature-Flag-Abstraktion
- Martin Fowler / Pete Hodgson: Feature Toggles
- OWASP API Security 2023: Unrestricted Resource Consumption
- OWASP Logging Cheat Sheet
- OWASP Secrets Management Cheat Sheet
