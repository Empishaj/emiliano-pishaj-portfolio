# ADR-039 — Feature Flags: Deployment von Release entkoppeln

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Unleash/FF4J|
| Datum      | 2024-01-01                        |
| Kategorie  | DevOps / Release Management       |

---

## Kontext & Problem

Ohne Feature Flags ist Deploy = Release. Jeder Deploy ist riskant. Mit Feature Flags ist Deploy ≠ Release: Code kann deployed werden, aber deaktiviert — und dann schrittweise aktiviert werden. Trunk-Based Development wird möglich. A/B-Tests werden trivial.

---

## Anwendungsfälle

```
1. Trunk-Based Development: Unfertige Features in main ohne Aktivierung
2. Canary Release: Feature für 5% der User aktivieren, bei Fehler sofort deaktivieren
3. A/B-Testing: Feature-Varianten für verschiedene Nutzergruppen
4. Kill Switch: Problematisches Feature sofort deaktivieren ohne Deploy
5. Operational Flags: Feature bei Lastspitzen deaktivieren
```

---

## Spring Boot: Einfache Feature Flags mit @ConditionalOnProperty

```yaml
# application.yml
features:
  new-checkout-flow: false   # Deaktiviert
  ai-recommendations: true   # Aktiviert
  experimental-search: false
```

```java
// Für Bean-Level-Flags: Feature-Bean nur wenn aktiviert
@Component
@ConditionalOnProperty(name = "features.ai-recommendations", havingValue = "true")
public class AiRecommendationService implements RecommendationService { ... }

@Component
@ConditionalOnProperty(name = "features.ai-recommendations",
                       havingValue = "false", matchIfMissing = true)
public class BasicRecommendationService implements RecommendationService { ... }
```

---

## Unleash: Professionelles Feature-Flag-System

```java
// build.gradle.kts
// implementation("io.getunleash:unleash-client-java:9.0.0")

@Configuration
public class UnleashConfig {

    @Bean
    public Unleash unleash() {
        return UnleashConfig.builder()
            .appName("order-service")
            .instanceId(InetAddress.getLocalHost().getHostName())
            .unleashAPI("https://unleash.example.com/api/")
            .customHttpHeader("Authorization", "Bearer " + apiKey)
            .fetchTogglesInterval(15) // Alle 15 Sekunden aktualisieren
            .build()
            .unleash();
    }
}

// Feature Flag Service: zentraler Einstiegspunkt
@Service
public class FeatureService {
    private final Unleash unleash;

    // Einfacher Boolean-Check
    public boolean isEnabled(String featureName) {
        return unleash.isEnabled(featureName);
    }

    // Kontext-abhängig (User-spezifisch)
    public boolean isEnabledForUser(String featureName, UserId userId) {
        var context = UnleashContext.builder()
            .userId(userId.value().toString())
            .build();
        return unleash.isEnabled(featureName, context);
    }

    // Mit Default-Wert (Safety Net)
    public boolean isEnabled(String featureName, boolean defaultValue) {
        return unleash.isEnabled(featureName, defaultValue);
    }
}
```

---

## Feature Flags im Code: sauber und testbar

```java
// ❌ Schlecht: Flag-Check direkt im Business-Code verstreut
@Service
public class CheckoutService {
    public CheckoutResult checkout(CheckoutCommand cmd) {
        if (unleash.isEnabled("new-checkout-flow")) {
            // Neuer Checkout
        } else {
            // Alter Checkout
        }
        if (unleash.isEnabled("apply-new-discount")) {
            // ...
        }
        // Feature-Flags überall verstreut — schwer zu entfernen
    }
}

// ✅ Gut: Strategy Pattern + Feature Flag als Routing
@Service
public class CheckoutService {
    private final CheckoutStrategy   legacyCheckout;
    private final CheckoutStrategy   newCheckout;
    private final FeatureService     features;

    public CheckoutResult checkout(CheckoutCommand cmd) {
        var strategy = features.isEnabledForUser("new-checkout-flow", cmd.userId())
            ? newCheckout
            : legacyCheckout;
        return strategy.execute(cmd);
    }
}

// Feature Flag im Test mocken
@Test
void checkout_usesNewFlow_whenFlagEnabled() {
    when(features.isEnabledForUser("new-checkout-flow", userId)).thenReturn(true);

    checkoutService.checkout(command);

    verify(newCheckout).execute(command);
    verifyNoInteractions(legacyCheckout);
}
```

---

## Feature Flag Lifecycle: geboren und gestorben

```java
// Feature Flags akkumulieren sich → technische Schulden
// Jede Flag hat ein "Verfallsdatum" als TODO-Kommentar

// ADR-039: Flags die älter als 2 Sprints sind müssen entfernt werden
@Service
public class CheckoutService {

    // TODO(ADR-039): Flag 'new-checkout-flow' nach Sprint 47 entfernen
    // Erstellt: 2024-01-15, Geplante Entfernung: 2024-02-15
    // Vollständig aktiviert → verbleibende Arbeit: legacyCheckout entfernen
    public CheckoutResult checkout(CheckoutCommand cmd) {
        if (features.isEnabled("new-checkout-flow")) {
            return newCheckout.execute(cmd);
        }
        return legacyCheckout.execute(cmd); // ← Nach Entfernung der Flag löschen
    }
}
```

---

## Konsequenzen

**Positiv:** Trunk-Based Development ohne lange Feature-Branches. Kill Switch ohne Deploy. Canary Releases ohne Kubernetes-Traffic-Routing-Komplexität.

**Negativ:** Flags sind technische Schulden — müssen nach Vollaktivierung entfernt werden. Zu viele Flags machen Code unlesbar. Flags benötigen eigenen Test-Aufwand (both enabled & disabled).

---

## 💡 Guru-Tipps

- **Maximale Flags gleichzeitig**: 5–10 aktive Flags pro Service. Mehr → Komplexität explodiert.
- **Flag-Typen trennen**: Release-Flags (temporär) von Ops-Flags (permanent, z. B. Kill-Switch) trennen.
- **Monitoring**: Welche Flags sind aktiv? Unleash-Dashboard oder eigene Metrik.

---

## Verwandte ADRs

- [ADR-036](ADR-036-devops-cicd.md) — CI/CD + Feature Flags = sicheres Deployment.
- [ADR-022](ADR-022-resilience-circuit-breaker.md) — Feature Flag als Software-Circuit-Breaker.
