# QG-JAVA-039-01 — Statische Feature Flags über Konfiguration mit `application.yml`

## Dokumentstatus

| Aspekt | Details/Erklärung |
| --- | --- |
| Dokumenttyp | Java Quality Guideline / vertiefendes Kompendium-Kapitel |
| ID | QG-JAVA-039-01 |
| Titel | Statische Feature Flags über Konfiguration mit `application.yml` |
| Status | Accepted / verbindlicher Standard für statische Konfigurations-Flags in Java-/Spring-Boot-Services |
| Version | 2.0 |
| Datum | 2026-05-03 (überarbeitet von 1.0 vom 2026-05-02) |
| Sprache | Deutsch |
| Java-Baseline | Java 21+ |
| Spring-Baseline | Spring Boot 3.4+, Spring Framework 6.x, Hibernate Validator 8 |
| Kategorie | DevOps · Release Management · Spring Boot Configuration · SaaS Platform Quality |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, DevOps, QA, Security, Plattformverantwortliche |
| Verbindlichkeit | Verbindlich für neue und überarbeitete Spring-Boot-Services. Abweichungen sind im Pull Request nachvollziehbar zu begründen. |
| Technischer Prüfstatus | Code-Beispiele sind gegen Spring Boot 3.4 und Hibernate Validator 8 referenzbasiert validiert; alle `@ConfigurationProperties`-Beispiele verwenden Records mit konstruktor-basierter Bindung |
| Schwester-Guidelines | QG-JAVA-006 (Spring-Boot-Serviceschicht), QG-JAVA-008 (Objektorientierung), QG-JAVA-019 (Contract Testing), QG-JAVA-126 (Docker) |
| Nicht-Ziel | Diese Richtlinie beschreibt keine dynamischen Rollouts, A/B-Test-Auswertung, Autorisierung, Mandantentrennung, Entitlements oder Secrets Management |
| Wesentliche Änderungen ggü. v1.0 | `@DurationUnit`/`@DataSizeUnit` für Duration/DataSize-Properties · Bean Validation als Standard-Validierungspfad · Lifecycle-Klärung von `@ConditionalOnProperty` (einmalige Auswertung) · `ApplicationContextRunner`-Tests für echte Validierung · Multi-Varianten-Pattern (Enum/String) statt nur Boolean · positive Boolean-Naming als Konvention · `@ConfigurationPropertiesScan` als moderne Alternative · Profil-Aktivierungsstrategien und Property-Auflösungsreihenfolge · Environment-Variable-Mapping mit Underscore-Verhalten · Abgrenzung zu `@Profile` und `@ConditionalOnExpression` · operationalisiertes Entfernungskriterium · Cross-References zu QG-JAVA-006 v2 · strukturell an QG-JAVA-006 v2 angeglichen (TOC, Lese-Anleitung, MUSS/DARF/SOLLTE getrennt, Begriffsglossar als eigene Sektion, RFC-2119-Großschreibung) |

---

## Inhaltsverzeichnis

1. [Zweck dieser Richtlinie](#1-zweck-dieser-richtlinie)
2. [Kurzregel für Entwickler](#2-kurzregel-für-entwickler)
3. [Verbindlicher Standard](#3-verbindlicher-standard)
4. [Geltungsbereich](#4-geltungsbereich)
5. [Begriffe](#5-begriffe)
6. [Technischer Hintergrund](#6-technischer-hintergrund)
7. [Lifecycle: Wann wird ein Flag ausgewertet?](#7-lifecycle-wann-wird-ein-flag-ausgewertet)
8. [Gute Anwendung: Bean-Auswahl mit Boolean-Flag](#8-gute-anwendung-bean-auswahl-mit-boolean-flag)
9. [Gute Anwendung: Multi-Varianten-Pattern](#9-gute-anwendung-multi-varianten-pattern)
10. [Gute Anwendung: typisierte Properties mit Bean Validation](#10-gute-anwendung-typisierte-properties-mit-bean-validation)
11. [Gute Anwendung: Feature-Router](#11-gute-anwendung-feature-router)
12. [Duration und DataSize richtig binden](#12-duration-und-datasize-richtig-binden)
13. [Anti-Patterns](#13-anti-patterns)
14. [Profile und Property-Auflösungsreihenfolge](#14-profile-und-property-auflösungsreihenfolge)
15. [Environment Overrides und Variable Mapping](#15-environment-overrides-und-variable-mapping)
16. [Benennungskonvention](#16-benennungskonvention)
17. [Default-Werte und sicheres Verhalten](#17-default-werte-und-sicheres-verhalten)
18. [Abgrenzung zu `@Profile` und `@ConditionalOnExpression`](#18-abgrenzung-zu-profile-und-conditionalonexpression)
19. [Security- und SaaS-Aspekte](#19-security--und-saas-aspekte)
20. [Teststrategie](#20-teststrategie)
21. [Lifecycle-Management und Entfernungskriterium](#21-lifecycle-management-und-entfernungskriterium)
22. [Migration bestehender Flags](#22-migration-bestehender-flags)
23. [Review-Checkliste](#23-review-checkliste)
24. [Automatisierbare Prüfungen](#24-automatisierbare-prüfungen)
25. [Ausnahmen](#25-ausnahmen)
26. [Definition of Done](#26-definition-of-done)
27. [Entscheidungsbaum](#27-entscheidungsbaum)
28. [Quellen und weiterführende Literatur](#28-quellen-und-weiterführende-literatur)

### Wie liest du dieses Dokument

Dieses Dokument ist mit ca. 95 KB ein Nachschlagewerk, kein Lesebuch. Drei Lesepfade werden empfohlen:

**Wenn du neu im Team bist:** Starte mit Sektion 2 (Kurzregel), lies dann Sektion 1 (Zweck), Sektion 4 (Geltungsbereich) und Sektion 8–11 (gute Anwendung mit Beispielen). Damit hast du die mentale Karte für 80 % aller Flag-Reviews.

**Wenn du im Code-Review bist:** Springe direkt zu Sektion 23 (Review-Checkliste). Jede Zeile hat einen Anker zur Detail-Sektion mit der Begründung. Bei Verstoß gegen eine MUSS-Regel: Sektion 3.1.

**Wenn du etwas Spezifisches suchst:** Inhaltsverzeichnis oben. Häufigste Punkte: Anti-Patterns → Sektion 13. Lifecycle → Sektion 7. Validierung → Sektion 10. Tests → Sektion 20. Security → Sektion 19.

**Wenn du eine Tool-Entscheidung treffen musst:** Sektion 27 (Entscheidungsbaum) — sieben Fragen, klare Antworten, ob ein statisches Flag das richtige Werkzeug ist.

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie statische Feature Flags in Java-21-/Spring-Boot-3.4-Anwendungen sauber über Konfiguration umgesetzt werden. Der Schwerpunkt liegt auf `application.yml`, Profil-Dateien wie `application-local.yml` und `application-prod.yml`, Umgebungsvariablen, `@ConditionalOnProperty` und typisierten `@ConfigurationProperties`.

Statische Flags sind einfache, robuste Konfigurationsschalter. Sie entscheiden **beim Aufbau des Spring `ApplicationContext`** — also einmalig beim Start der Anwendung — ob eine Funktion, eine technische Variante, ein Adapter, eine Integration oder ein Codepfad aktiviert ist. Sie sind besonders geeignet für lokale Entwicklungsoptionen, environment-spezifische Integrationen, technische Migrationsschalter und einfache Startzeitentscheidungen.

Diese Richtlinie verhindert vier typische Fehler:

1. Feature-Flags werden als lose Strings im Fachcode verstreut.
2. Statische Konfigurationsflags werden fälschlich als dynamische Kill Switches oder Canary-Mechanismen verstanden.
3. Flags werden mit Sicherheits-, Mandanten- oder Berechtigungsentscheidungen verwechselt.
4. Flag-Konfiguration enthält subtile Bugs durch falsche Typ-Bindung (z. B. `Duration` ohne `@DurationUnit`).

Ziel ist nicht, Feature Flags künstlich zu komplexifizieren. Ziel ist, dass jedes Flag klar erkennbar beantwortet:

- Welche Entscheidung trifft es?
- Wann wird sie getroffen — beim Start oder zur Laufzeit?
- Wer setzt sie — Default, Profil, Environment, Override?
- Was ist der sichere Default, wenn die Property fehlt?
- Wann wird das Flag wieder entfernt?

---

## 2. Kurzregel für Entwickler

Statische Feature Flags über `application.yml` DÜRFEN verwendet werden, wenn die Entscheidung beim Start der Anwendung feststehen darf. Alle statischen Flags MÜSSEN zentral unter `features.*` liegen, sprechend benannt, typisiert über `@ConfigurationProperties` (mit Bean Validation) oder deklarativ über `@ConditionalOnProperty` gelesen, für aktivierten und deaktivierten Zustand getestet und mit einem klaren Zweck dokumentiert werden.

Boolean-Flag-Namen MÜSSEN positiv formuliert sein (`use-mock-sender`, nicht `mock-sender-disabled`). Bei drei oder mehr Varianten MUSS ein Enum-/String-Pattern verwendet werden, kein Boolean. `Duration`-Properties MÜSSEN `@DurationUnit` setzen, damit fehlende Einheiten nicht stillschweigend als Millisekunden interpretiert werden. Validierung erfolgt über Bean-Validation-Annotationen (`@Validated`, `@Positive`, `@NotBlank`).

Statische Flags DÜRFEN NICHT als Ersatz für Autorisierung, Mandantentrennung, Entitlements, Secrets Management, Canary Releases, A/B-Tests oder schnelle produktive Kill Switches verwendet werden. `@ConditionalOnProperty` greift einmalig beim Start — auch mit Spring Cloud Config Refresh oder Actuator `/refresh` bleibt die Bean-Topologie unverändert.

---

## 3. Verbindlicher Standard

### 3.1 MUSS-Regeln

Ein statisches Feature Flag MUSS folgende Regeln erfüllen:

1. Alle statischen Flags MÜSSEN unter dem Namespace `features.*` liegen.
2. Flag-Namen MÜSSEN sprechend sein und Kontext, Funktion und Bedeutung erkennen lassen.
3. Boolean-Flag-Namen MÜSSEN positiv formuliert sein (`use-mock-sender`, nicht `mock-sender-disabled`).
4. Bei drei oder mehr Varianten MUSS ein Enum-/String-Pattern verwendet werden statt mehrerer Booleans.
5. `Duration`-Properties MÜSSEN `@DurationUnit` setzen.
6. `DataSize`-Properties MÜSSEN `@DataSizeUnit` setzen.
7. `@ConfigurationProperties`-Records MÜSSEN Bean-Validation-Annotationen (`@Validated`, `@Positive`, `@NotBlank`) für strukturelle Constraints verwenden.
8. Komplexe oder kontextabhängige Constraints MÜSSEN im Compact Constructor des Records validiert werden.
9. Aktivierter und deaktivierter Zustand jedes Flags MÜSSEN getestet sein.
10. Tests MÜSSEN die Spring-Validierung über `ApplicationContextRunner` mit `withPropertyValues` prüfen, nicht nur direkte Konstruktor-Aufrufe.
11. Default-Werte MÜSSEN bewusst gewählt sein. `matchIfMissing=true` MUSS im Review begründet werden.
12. Für neue, riskante oder experimentelle Funktionen MUSS der Default `enabled=false` sein.
13. Temporäre Release- oder Migrationsflags MÜSSEN ein Entfernungskriterium besitzen (Annotation, Kommentar-Schema oder Ticket-Verweis).
14. Flag-Konfiguration MUSS reproduzierbar versioniert sein (Git-versioniertes `application.yml`).
15. Sensitive Werte (Tokens, API-Keys, Passwörter) DÜRFEN NICHT in Flag-Konfiguration stehen — Secrets gehören in einen Secret Store.
16. Autorisierungs- und Mandantenprüfungen MÜSSEN unabhängig vom Flag erfolgen (siehe QG-JAVA-006 v2 Sektion 14).

### 3.2 DARF-NICHT-Regeln

Ein statisches Feature Flag DARF NICHT:

1. Lose außerhalb von `features.*` als Root-Property abgelegt werden.
2. Mit `@Value` im Fachcode gelesen werden, wenn es um nicht-triviale Entscheidungen geht.
3. Als einzige Prüfung für eine sicherheitsrelevante Aktion fungieren.
4. Tenant-spezifisch im Property-Namen kodiert werden (`features.tenant-acme-premium-enabled` ist verboten).
5. Secrets, Tokens, API-Keys, Passwörter oder personenbezogene Daten enthalten.
6. Als produktiver Sofort-Kill-Switch verwendet werden, wenn die Reaktionszeit eines Neustarts fachlich nicht akzeptabel ist.
7. Negative Booleans als Standardform haben (`disabled`, `legacy-mode-enabled`).
8. In `@ConditionalOnExpression` mit komplexen Boolean-Kombinationen mehrerer Properties verschachtelt werden.
9. Im Domain Model als Parameter durchgereicht werden — Domain-Klassen DÜRFEN NICHT von Spring-Konfiguration abhängen.
10. Als Testdatenersatz in produktiver Konfiguration verwendet werden.
11. Ohne Entfernungskriterium dauerhaft als „temporäres" Migrationsflag bestehen bleiben.

### 3.3 SOLLTE-Regeln

Ein statisches Feature Flag SOLLTE:

1. Mit `@ConfigurationPropertiesScan` zentral aktiviert werden statt mit individuellem `@EnableConfigurationProperties`.
2. Bei Bean-Auswahl in einer dedizierten `@Configuration`-Klasse mit `@Bean`-Methoden statt direkter `@Component`-Annotation gepflegt werden, sobald es drei oder mehr Varianten gibt.
3. Bei Multi-Varianten als Java-Enum modelliert werden, nicht als String-Konstante.
4. Bei Routing-Entscheidungen über einen dedizierten Router-Service zentralisiert werden.
5. Mit Konstruktor-Injektion in einer `@Configuration`-Klasse erzwungen werden, um Fail-Fast-Validierung beim Start zu erreichen.
6. Bei betriebsrelevanter Wirkung Monitoring, Timeouts und Ressourcenüberlegungen mitführen.
7. Bei jedem Pull Request das Lifecycle-Stadium (temporär/dauerhaft) im Kommentar oder Annotation angeben.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Spring-Boot-Services, Batch-Anwendungen, Worker, interne Plattformmodule, Integrationsadapter und lokale Entwicklungsumgebungen, in denen Feature- oder Betriebsvarianten über Konfiguration gesteuert werden.

### 4.1 Geeignete Anwendungsfälle

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
| --- | --- | --- | --- |
| Lokale Entwicklung | Lokale Adapter, Mocks oder Stub-Implementierungen werden beim Start aktiviert | `features.mail.use-mock-sender=true` | **Geeignet** |
| Environment-spezifische Integration | Staging nutzt Sandbox-Provider, Produktion nutzt echten Provider | `features.payment.provider=sandbox` | **Geeignet** |
| Bean-Auswahl | Genau eine Implementierung eines Interfaces soll im ApplicationContext existieren | `SmtpMailSender` oder `LoggingMailSender` | **Geeignet** |
| Technische Migration | Neuer technischer Pfad wird deployt, aber initial deaktiviert | `features.invoice-export.enabled=false` | **Geeignet mit Ablaufdatum** |
| Batch-/Worker-Verhalten | Ein optionaler Job oder Export wird pro Umgebung aktiviert | `features.cleanup-job.enabled=true` | **Geeignet** |
| Einfache Betriebsoption | Nicht zeitkritische Funktion kann beim nächsten Start deaktiviert werden | `features.expensive-report.enabled=false` | **Eingeschränkt geeignet** |
| Multi-Varianten-Auswahl | Eine von drei oder mehr Implementierungen wird beim Start ausgewählt | `features.mail.sender-type=ses` | **Geeignet** |

### 4.2 Nicht geeignete Anwendungsfälle

Diese Richtlinie gilt **nicht** als primärer Mechanismus für folgende Fälle:

| Aspekt | Details/Erklärung | Beispiel | Besserer Mechanismus |
| --- | --- | --- | --- |
| Canary Release | Prozentuale Aktivierung für Nutzer oder Requests | 5 % der Nutzer | Unleash, FF4J, OpenFeature, Gateway-/Traffic-Steuerung |
| A/B-Test | Variantensteuerung mit Auswertung, Stickiness und Metriken | Checkout-Variante A/B | Experiment-Plattform oder dynamisches Feature-Flag-System |
| Produktberechtigung | Kunde darf ein Modul nutzen oder nicht | Premium-Modul | Entitlement-/Permission-Modell |
| Mandantentrennung | Datenzugriff muss pro Tenant abgesichert sein | Tenant A darf nur Tenant-A-Daten sehen | Tenant-Isolation im Domain-, Query- und Security-Modell (siehe QG-JAVA-006 v2 Sektion 14) |
| Produktiver Kill Switch | Sofortiges Abschalten ohne Restart | Externer Payment-Provider fällt aus | Dynamisches Flag-System oder operative Control Plane |
| Secrets | Zugangsdaten, Tokens, API Keys | `payment.api-key` | Secret Store / Vault / Kubernetes Secret / Cloud Secret Manager |
| Live-Reaktion auf Property-Änderung | Property soll sich zur Laufzeit ändern und Verhalten anpassen | Throttle-Limit anpassen | `@RefreshScope` (Spring Cloud) oder dynamisches Feature-Flag-System |
| Kombination mehrerer Properties | Komplexe Boolean-Logik aus mehreren Flags | `a && b && !c` | Domain-Logik in Service mit dediziertem Router |

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Feature Flag | Konfigurations-Schalter, der das Verhalten der Anwendung beeinflusst | `features.invoice-export.enabled` |
| Statisches Flag | Wird beim Start einmalig ausgewertet | `@ConditionalOnProperty`-Bean |
| Dynamisches Flag | Wird zur Laufzeit ausgewertet, kann live geändert werden | OpenFeature, Unleash |
| `@ConfigurationProperties` | Spring-Annotation, die Properties typisiert in Java-Objekte bindet | `@ConfigurationProperties(prefix="features.invoice-export")` |
| `@ConditionalOnProperty` | Spring-Boot-Annotation, die Bean-Erzeugung an Property-Werte koppelt | `@ConditionalOnProperty(prefix="features.mail", name="use-mock-sender")` |
| `@ConfigurationPropertiesScan` | Aktiviert automatisches Scannen aller `@ConfigurationProperties` in einem Package | `@ConfigurationPropertiesScan("com.example.features")` |
| `@DurationUnit` | Spring-Boot-Annotation, die die Default-Einheit für `Duration`-Werte festlegt | `@DurationUnit(ChronoUnit.SECONDS)` |
| `@DataSizeUnit` | Spring-Boot-Annotation, die die Default-Einheit für `DataSize`-Werte festlegt | `@DataSizeUnit(DataUnit.MEGABYTES)` |
| Compact Constructor | Java-Record-Konstruktor ohne explizite Parameter, validiert Komponentenwerte | `public Properties { if (...) throw ... }` |
| Bean Validation | Jakarta-Bean-Validation-API für deklarative Validierung | `@Validated`, `@Positive`, `@NotBlank` |
| Profile | Spring-Mechanismus zur Trennung von Konfiguration nach Umgebung | `local`, `staging`, `prod` |
| Profile Group | Spring-Boot-Feature, das mehrere Profile als logische Gruppe aktiviert | `production: prod, cloud, metrics` |
| Property-Auflösungsreihenfolge | Reihenfolge, in der Spring Boot Konfigurationsquellen auswertet | Command-Line > ENV > YAML > Default |
| Relaxed Binding | Spring Boot's Mechanismus, mehrere Property-Schreibweisen zu akzeptieren | `kebab-case`, `camelCase`, `SCREAMING_SNAKE_CASE` |
| Feature Router | Service, der die Flag-Entscheidung an einer Stelle konzentriert | `CheckoutStrategyRouter` |
| Entitlement | Berechtigung eines Tenants oder Nutzers für ein fachliches Feature | `entitlements.hasAccess(tenantId, Feature.PREMIUM)` |
| Kill Switch | Mechanismus zum sofortigen Abschalten einer Funktion | dynamisch, nicht statisch |
| Fail Fast at Startup | Anwendung scheitert beim Start, wenn Konfiguration ungültig ist | erzwungene Konstruktor-Bindung in `@Configuration` |

---

## 6. Technischer Hintergrund

Spring Boot unterstützt externe Konfiguration, damit dieselbe Anwendung in unterschiedlichen Umgebungen mit unterschiedlichen Einstellungen betrieben werden kann. Zu den unterstützten Konfigurationsquellen gehören Properties-Dateien, YAML-Dateien, Umgebungsvariablen und Command-Line-Argumente. Das ist die technische Grundlage dafür, statische Feature Flags über `application.yml` und Deployment-Konfiguration zu setzen.

**Eine typische Flag-Struktur sieht so aus:**

```yaml
features:
  invoice-export:
    enabled: false
  mail:
    use-mock-sender: true
  checkout:
    new-flow-enabled: false
```

Spring Boot kann diese Werte auf unterschiedliche Weise verwenden:

1. Direkt über `@ConditionalOnProperty`, um Beans abhängig von Properties zu erzeugen.
2. Über `@ConfigurationProperties`, um strukturierte Konfiguration in typisierte Java-Objekte zu binden.
3. Über Profile wie `local`, `test`, `staging` und `prod`.
4. Über Umgebungsvariablen oder Deployment-Werte.
5. Über Testwerkzeuge wie `ApplicationContextRunner`.

`@ConditionalOnProperty` ist besonders wichtig für Bean-Level-Flags. Die Annotation prüft, ob eine Property in der Spring `Environment` vorhanden ist und einen bestimmten Wert besitzt. Über `havingValue` und `matchIfMissing` wird definiert, wann eine Bedingung erfüllt ist. **Das geschieht beim Aufbau des ApplicationContext** — siehe Sektion 7 für die genaue Lifecycle-Erklärung.

`@ConfigurationProperties` mit Records nutzt seit Spring Boot 3.x **konstruktor-basierte Bindung**. Das ist die idiomatische Form. Spring Boot erkennt Records automatisch und braucht kein zusätzliches `@ConstructorBinding`. Bei nicht-Record-Klassen mit einem öffentlichen Konstruktor passiert dasselbe automatisch; bei mehreren Konstruktoren MUSS `@ConstructorBinding` explizit gesetzt werden.

**Bean Validation auf `@ConfigurationProperties`** ist die idiomatische Form für strukturelle Constraints. Mit `@Validated` auf der Klasse aktiviert Spring Boot Hibernate Validator. Annotationen wie `@Positive`, `@NotBlank`, `@Min`, `@Max`, `@Pattern` werden beim Binding ausgewertet. Bei Verstoß wirft Spring Boot beim Start einen `BindException` mit allen Verstößen — nicht nur dem ersten. Das ist deutlich besser als imperative Validierung im Compact Constructor, weil:

- Engineers sofort alle Probleme auf einmal sehen, nicht nur das erste.
- Annotationen deklarativ und sofort lesbar sind.
- Standard-Constraints (`@Email`, `@Min`, `@Pattern`) wiederverwendbar sind.

Compact Constructor bleibt für komplexe oder kontextabhängige Validierungen die richtige Wahl — z. B. „wenn `enabled=true`, dann muss `provider` gesetzt sein". Beide Mechanismen sind valide und ergänzen sich.

---

## 7. Lifecycle: Wann wird ein Flag ausgewertet?

Das ist der wichtigste konzeptionelle Punkt — und der häufigste Fehler in Engineering-Teams.

### 7.1 `@ConditionalOnProperty` greift einmalig beim Start

`@ConditionalOnProperty` wird beim Aufbau des `ApplicationContext` ausgewertet. Nach dem Start ist die Bean-Topologie eingefroren. Wenn jemand zur Laufzeit die Property ändert — über Spring Cloud Config Refresh, Actuator `/refresh`-Endpoint, Konfigurations-Watcher, externe Property-Quelle — passiert **gar nichts**: die Beans bleiben wie sie sind.

```java
// Application Start: features.mail.use-mock-sender=false
// → SmtpMailSender wird im Context registriert
// → LoggingMailSender wird NICHT registriert

// Zur Laufzeit: features.mail.use-mock-sender=true (via Spring Cloud Config Refresh)
// → Property-Wert ändert sich
// → ABER: SmtpMailSender bleibt im Context
// → ABER: LoggingMailSender wird NICHT nachträglich registriert
// → ABER: MailSender-Bean ist immer noch SmtpMailSender
```

Das ist ein häufiges Missverständnis. „Wir haben Spring Cloud Config, also sind unsere Flags dynamisch!" — Falsch. Properties können dynamisch sein, aber `@ConditionalOnProperty` ist statisch.

### 7.2 `@ConfigurationProperties` ist auch statisch — meistens

`@ConfigurationProperties` ohne `@RefreshScope` wird einmal beim Start gebunden. Die Werte sind dann für die gesamte Anwendungs-Lebenszeit fest. Auch hier hilft Spring Cloud Config Refresh nichts.

**Mit `@RefreshScope`** kann sich das Verhalten ändern:

```java
@RefreshScope
@ConfigurationProperties(prefix = "features.throttle")
public class ThrottleProperties {
    private int maxRequestsPerMinute;
    // ...
}
```

`@RefreshScope` führt dazu, dass die Bean nach einem Actuator-`/refresh` neu erzeugt wird. Wer die Bean injiziert, bekommt automatisch eine Proxy-Variante, die zur Laufzeit den neuen Wert liefert.

**Aber:** `@RefreshScope` funktioniert nur für `@ConfigurationProperties`, nicht für `@ConditionalOnProperty`. Bean-Selektion bleibt statisch. Das ist eine wichtige Trennung.

### 7.3 Wann ist welche Form richtig?

| Anforderung | Mechanismus |
|---|---|
| Bean-Auswahl beim Start, danach statisch | `@ConditionalOnProperty` |
| Konfigurationswerte beim Start binden, danach statisch | `@ConfigurationProperties` ohne `@RefreshScope` |
| Konfigurationswerte zur Laufzeit nachladbar | `@ConfigurationProperties` mit `@RefreshScope` (Spring Cloud erforderlich) |
| Bean-Topologie zur Laufzeit ändern | nicht über Spring Boot Standard — dynamisches Flag-System verwenden |
| Live-Aktivierung/-Deaktivierung von Funktionen | OpenFeature, Unleash, FF4J |

### 7.4 Was bedeutet das für Reviews?

Wenn ein Reviewer ein `@ConditionalOnProperty` sieht, MUSS er sich fragen: „Ist die Entscheidung beim Start ausreichend?" Wenn ja — passt. Wenn nein — falsches Werkzeug.

Wenn ein Reviewer `@ConfigurationProperties` mit `@RefreshScope` sieht, MUSS er sich fragen: „Wird die Bean wirklich injiziert oder zur Laufzeit referenziert? Sind alle Konsumenten Refresh-fähig?" `@RefreshScope` bringt subtile Probleme mit Caching, Connection-Pools und State-Management mit sich.

---

## 8. Gute Anwendung: Bean-Auswahl mit Boolean-Flag

Ein klassischer Fall ist die Auswahl zwischen echter Integration und lokaler Ersatzimplementierung **bei genau zwei Varianten**.

### 8.1 Konfiguration

```yaml
features:
  mail:
    use-mock-sender: false       # ✅ positive Boolean-Naming
```

### 8.2 Interface

```java
public interface MailSender {
    void send(MailMessage message);
}
```

### 8.3 Implementierungen mit `@ConditionalOnProperty`

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "features.mail",
        name = "use-mock-sender",
        havingValue = "false",
        matchIfMissing = true        // ✅ Default: produktiver SmtpMailSender
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
        name = "use-mock-sender",
        havingValue = "true"
        // matchIfMissing fehlt → Default ist false
)
public class LoggingMailSender implements MailSender {

    @Override
    public void send(MailMessage message) {
        // lokaler Entwicklungsmodus: kein echter Versand
    }
}
```

### 8.4 Warum ist das gut?

- Der Fachcode arbeitet nur gegen `MailSender`, nicht gegen konkrete Implementierungen.
- Die Auswahl ist Konfiguration, nicht Businesslogik.
- `matchIfMissing=true` bedeutet: ohne Konfiguration gewinnt der produktive Pfad — sicherer Default.
- Beide Varianten sind mutual exclusive über die Property gesteuert.

### 8.5 Wann reicht dieses Pattern nicht?

Bei drei oder mehr Varianten (siehe Sektion 9). Boolean skaliert nicht — wer einen dritten `MailSender` für AWS SES hinzufügen will, kommt mit einem Boolean nicht mehr aus. Dann ist das Multi-Varianten-Pattern aus Sektion 9 die richtige Form.

---

## 9. Gute Anwendung: Multi-Varianten-Pattern

Bei drei oder mehr Implementierungen MUSS ein Enum-/String-basiertes Pattern verwendet werden, kein Boolean.

### 9.1 Konfiguration

```yaml
features:
  mail:
    sender-type: smtp        # smtp, logging, ses, sendgrid
```

### 9.2 Konfigurations-Klasse als Single Source of Truth

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailSenderConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "features.mail",
            name = "sender-type",
            havingValue = "smtp",
            matchIfMissing = true        // ✅ Default
    )
    public MailSender smtpMailSender() {
        return new SmtpMailSender();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "features.mail",
            name = "sender-type",
            havingValue = "logging"
    )
    public MailSender loggingMailSender() {
        return new LoggingMailSender();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "features.mail",
            name = "sender-type",
            havingValue = "ses"
    )
    public MailSender sesMailSender(SesProperties props) {
        return new SesMailSender(props);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "features.mail",
            name = "sender-type",
            havingValue = "sendgrid"
    )
    public MailSender sendgridMailSender(SendgridProperties props) {
        return new SendgridMailSender(props);
    }
}
```

### 9.3 Warum ist das besser als Boolean?

- **Erweiterbar:** neue Variante = neue `@Bean`-Methode mit neuem `havingValue`. Keine Logik-Umstellung.
- **Mutual Exclusivität:** über `havingValue` explizit gesteuert. Nur eine Bean wird im Context registriert.
- **Lesbar:** `sender-type: ses` sagt sofort, was aktiv ist. `use-mock-sender: false` und `use-ses: true` und `use-sendgrid: false` ist mehrdeutig.
- **Konsistent:** alle Varianten leben in einer `@Configuration`-Klasse, nicht verstreut über `@Component`-annotierte Klassen.

### 9.4 Validierung des Variant-Werts

Wenn nur bestimmte Werte erlaubt sind, sollte das auch validiert werden:

```java
@ConfigurationProperties(prefix = "features.mail")
@Validated
public record MailFeatureProperties(
        @Pattern(regexp = "^(smtp|logging|ses|sendgrid)$",
                 message = "sender-type must be one of: smtp, logging, ses, sendgrid")
        String senderType
) {}
```

Oder noch besser: ein Java-Enum:

```java
public enum MailSenderType {
    SMTP, LOGGING, SES, SENDGRID
}

@ConfigurationProperties(prefix = "features.mail")
@Validated
public record MailFeatureProperties(
        @NotNull MailSenderType senderType
) {}
```

Spring Boot mappt YAML-String automatisch auf den Enum-Wert (case-insensitive). Damit ist Type-Safety im Code garantiert und unsinnige Werte (`sender-type: foo`) werden beim Start abgelehnt.

---

## 10. Gute Anwendung: typisierte Properties mit Bean Validation

Wenn ein Feature mehr als einen Boolean-Wert besitzt, MUSS `@ConfigurationProperties` verwendet werden — mit Bean-Validation-Annotationen für strukturelle Constraints.

### 10.1 Konfiguration

```yaml
features:
  invoice-export:
    enabled: true
    max-batch-size: 500
    include-attachments: false
    export-format: PDF
```

### 10.2 Properties-Record mit Bean Validation

```java
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "features.invoice-export")
@Validated                                              // ✅ aktiviert Bean Validation
public record InvoiceExportProperties(
        boolean enabled,
        @Positive @Min(1) int maxBatchSize,             // ✅ deklarativ
        boolean includeAttachments,
        @NotNull ExportFormat exportFormat
) {
    // ✅ Compact Constructor für komplexe Constraints
    public InvoiceExportProperties {
        if (enabled && maxBatchSize > 10_000) {
            throw new IllegalArgumentException(
                "maxBatchSize > 10000 ist nur in Test-Profilen zulässig"
            );
        }
    }
}

public enum ExportFormat {
    PDF, CSV, XLSX, JSON
}
```

### 10.3 Aktivierung mit `@ConfigurationPropertiesScan`

```java
@SpringBootApplication
@ConfigurationPropertiesScan("com.example.features")    // ✅ moderne Form
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`@ConfigurationPropertiesScan` registriert automatisch alle `@ConfigurationProperties`-annotierten Klassen im Package. Kein einzelnes `@EnableConfigurationProperties` pro Klasse mehr nötig.

### 10.4 Verwendung im Service

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
                properties.includeAttachments(),
                properties.exportFormat()
        );
    }

    private ExportResult runExport(
            ExportCommand command,
            int maxBatchSize,
            boolean includeAttachments,
            ExportFormat format
    ) {
        return ExportResult.started();
    }
}
```

### 10.5 Fail-Fast at Startup erzwingen

Standardmäßig validiert Spring Boot `@ConfigurationProperties` erst bei Bean-Erzeugung. Wenn die Bean Lazy ist, kommt der Validierungs-Fehler erst zur Laufzeit. Für sicherheitsrelevante Properties ist das zu spät.

```java
@Configuration
public class FeatureConfiguration {

    private final InvoiceExportProperties props;

    // ✅ Konstruktor erzwingt Bindung und Validierung beim Start
    public FeatureConfiguration(InvoiceExportProperties props) {
        this.props = props;
    }
}
```

Oder global über `spring.config.activate.on-cloud-platform` und ähnliche Mechanismen. Wer Sicherheits-Properties hat, sollte sie über eine `@Configuration`-Klasse mit Konstruktor-Injektion in den Startup-Pfad zwingen.

### 10.6 Warum besser als `@Value` oder mehrere lose Properties?

- **Typsicher:** Compiler erkennt Tippfehler beim Property-Zugriff.
- **Atomic:** alle zusammengehörigen Werte werden auf einmal validiert und gebunden.
- **Testbar:** Properties können direkt instanziiert werden (mit Vorsicht — siehe Sektion 20).
- **Refactoring-sicher:** IDE kann Property-Namen umbenennen.
- **Dokumentationsfähig:** `@ConfigurationProperties` wird von Spring Boot Configuration Metadata erkannt → IDE-Autocomplete in `application.yml`.

---

## 11. Gute Anwendung: Feature-Router

Wenn ein Feature zwischen zwei oder mehr Implementierungen routet, sollte die Entscheidung an einer kleinen, dedizierten Stelle konzentriert werden. Das verhindert verstreute `if`-Abfragen im Fachcode.

### 11.1 Konfiguration

```yaml
features:
  checkout:
    new-flow-enabled: false
```

### 11.2 Properties

```java
@ConfigurationProperties(prefix = "features.checkout")
@Validated
public record CheckoutFeatureProperties(
        boolean newFlowEnabled
) {}
```

### 11.3 Strategy-Interface und Implementierungen

```java
public interface CheckoutStrategy {
    CheckoutResult execute(CheckoutCommand command);
}

@Component
public class LegacyCheckoutStrategy implements CheckoutStrategy {
    @Override
    public CheckoutResult execute(CheckoutCommand command) {
        // alte Implementierung
        return CheckoutResult.success();
    }
}

@Component
public class NewCheckoutStrategy implements CheckoutStrategy {
    @Override
    public CheckoutResult execute(CheckoutCommand command) {
        // neue Implementierung
        return CheckoutResult.success();
    }
}
```

### 11.4 Router

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

### 11.5 Service

```java
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

### 11.6 Trade-off: Router-Pattern vs. Bean-Pattern

**Router-Pattern (oben gezeigt):**

- ✅ Beide Strategien immer im Context — einfach zu testen, beide Pfade verfügbar.
- ✅ Logik des Routings explizit sichtbar.
- ❌ Beide Strategien werden initialisiert (auch wenn nur eine genutzt wird) — bei teurer Initialisierung problematisch.

**Bean-Pattern (Alternative):**

```java
@Configuration
public class CheckoutStrategyConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "features.checkout",
                           name = "new-flow-enabled",
                           havingValue = "true")
    public CheckoutStrategy newCheckoutStrategy() {
        return new NewCheckoutStrategy();
    }

    @Bean
    @ConditionalOnProperty(prefix = "features.checkout",
                           name = "new-flow-enabled",
                           havingValue = "false",
                           matchIfMissing = true)
    public CheckoutStrategy legacyCheckoutStrategy() {
        return new LegacyCheckoutStrategy();
    }
}

@Service
public class CheckoutService {

    private final CheckoutStrategy strategy;       // ✅ nur eine Bean injected

    public CheckoutService(CheckoutStrategy strategy) {
        this.strategy = strategy;
    }
}
```

- ✅ Nur eine Strategy-Bean im Context — effizienter, sauberer.
- ✅ Keine `if`-Logik nötig.
- ❌ Tests brauchen `ApplicationContextRunner` für Bean-Selection-Tests.
- ❌ Nicht beide Pfade gleichzeitig testbar in einer JVM-Instanz.

**Empfehlung:**

- **Router-Pattern** bei einfachen Strategien mit billiger Initialisierung und wenn beide Pfade in Tests gleichzeitig validiert werden müssen.
- **Bean-Pattern** bei teuren Initialisierungen (DB-Connections, Cache-Setup, externe Provider) oder wenn nur ein Pfad zur Laufzeit existieren soll.

Beide Patterns sind valide. Die Wahl ist bewusst zu treffen.

---

## 12. Duration und DataSize richtig binden

`Duration` und `DataSize` sind häufige Property-Typen in Feature-Konfiguration. Beide haben subtile Fallstricke ohne explizite Default-Einheit.

### 12.1 Das Problem

```yaml
features:
  payment-provider:
    timeout: 2          # ⚠️ ohne Einheit → wird als 2 ms interpretiert!
```

Ohne `@DurationUnit` wird `2` als 2 Millisekunden gelesen — das ist ein Production-Bug, der „funktioniert in Tests, scheitert in Produktion" bedeutet.

### 12.2 Lösung: `@DurationUnit`

```java
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "features.payment-provider")
@Validated
public record PaymentProviderProperties(
        boolean enabled,
        @NotBlank String provider,
        @DurationUnit(ChronoUnit.SECONDS) Duration timeout,    // ✅ explizite Default-Einheit
        boolean retryEnabled
) {
    public PaymentProviderProperties {
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }
}
```

Jetzt funktioniert:

| YAML-Wert | Interpretation |
|---|---|
| `timeout: 2` | 2 Sekunden (Default-Einheit) |
| `timeout: 2s` | 2 Sekunden (explizit) |
| `timeout: 500ms` | 500 Millisekunden |
| `timeout: 1m` | 1 Minute |
| `timeout: 1h` | 1 Stunde |

### 12.3 Lösung: `@DataSizeUnit`

Analog für `DataSize`:

```java
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

@ConfigurationProperties(prefix = "features.upload")
@Validated
public record UploadProperties(
        boolean enabled,
        @DataSizeUnit(DataUnit.MEGABYTES) DataSize maxFileSize    // ✅ Default: MB
) {}
```

| YAML-Wert | Interpretation |
|---|---|
| `max-file-size: 10` | 10 MB (Default-Einheit) |
| `max-file-size: 10MB` | 10 MB (explizit) |
| `max-file-size: 500KB` | 500 KB |
| `max-file-size: 1GB` | 1 GB |

### 12.4 Pflicht-Test für Duration/DataSize

```java
@Test
void timeoutWithoutUnit_isInterpretedAsSeconds() {
    new ApplicationContextRunner()
        .withUserConfiguration(PaymentProviderConfiguration.class)
        .withPropertyValues(
            "features.payment-provider.enabled=true",
            "features.payment-provider.provider=stripe",
            "features.payment-provider.timeout=5"   // ⚠️ ohne Einheit
        )
        .run(context -> {
            var props = context.getBean(PaymentProviderProperties.class);
            assertThat(props.timeout()).isEqualTo(Duration.ofSeconds(5));
        });
}
```

---

## 13. Anti-Patterns

### 13.1 `@Value` im Fachcode

```java
// ❌ Anti-Pattern
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

Probleme:

- Property-Name ist ein String im Fachcode — kein Refactoring-Schutz.
- Flag-Zugriff ist über die Codebase verstreut, schwer auffindbar.
- Tests müssen Spring-Property-Injektion nachbauen oder einen Spring Context starten.
- Solche Stellen wachsen schnell zu verteilten Kontrollflüssen.

**Besser:** `@ConfigurationProperties` (siehe Sektion 10) und Konstruktor-Injektion oder Router (siehe Sektion 11).

### 13.2 Negative Booleans

```yaml
# ❌ Anti-Pattern: doppelte Negation im Code
features:
  legacy-flow:
    disabled: false
  mail:
    mock-sender-disabled: false
```

Im Code:

```java
if (!properties.disabled()) { ... }                         // doppelte Negation
if (Boolean.FALSE.equals(properties.disabled())) { ... }    // NPE-Falle bei Boolean-Wrapper
```

**Besser:**

```yaml
# ✅ Positive Naming
features:
  new-flow:
    enabled: true
  mail:
    use-mock-sender: false
```

### 13.3 Statisches Flag als Autorisierung

```java
// ❌ Anti-Pattern
if (features.adminToolsEnabled()) {
    tenantAdministration.deleteTenant(command.tenantId());
}
```

Das Feature Flag sagt nur, ob ein Funktionsbereich grundsätzlich aktiv ist. Es sagt **nicht**, ob der konkrete Nutzer die Aktion ausführen darf.

**Besser:**

```java
// ✅ Trennung von Aktivierung und Autorisierung
if (!features.adminToolsEnabled()) {
    throw new FeatureDisabledException("Admin tools are disabled.");
}

if (!authorizationService.canDeleteTenant(currentUser, command.tenantId())) {
    throw new AccessDeniedException("User is not allowed to delete tenant.");
}

tenantAdministration.deleteTenant(command.tenantId());
```

Siehe QG-JAVA-006 v2 Sektion 14 für vertiefende Diskussion zu Tenant- und Berechtigungsprüfungen in der Service-Schicht.

### 13.4 Statisches Flag als produktiver Kill Switch

```yaml
# ⚠️ Riskant ohne Restart-Mechanismus
features:
  recommendation-engine:
    enabled: false
```

Wenn ein produktives Problem innerhalb von Sekunden oder wenigen Minuten entschärft werden muss, reicht ein statisches Flag oft nicht. Dann braucht es **dynamische Steuerung** über Feature-Flag-Systeme (OpenFeature, Unleash, FF4J), Admin-Operationen, Traffic-Control oder Gateway-Regeln.

Statische Flags DÜRFEN als Kill Switch nur verwendet werden, wenn die Reaktionszeit eines Neustarts (mehrere Minuten in Container-Setups, abhängig von Probes und Rolling-Update-Strategien) fachlich akzeptabel ist und der Betriebsprozess dafür dokumentiert ist.

### 13.5 Tenant-spezifischer Flag-Name

```yaml
# ❌ Anti-Pattern
features:
  tenant-acme-premium-dashboard-enabled: true
  tenant-globex-export-enabled: false
```

Mandanten-, Produkt- und Vertragslogik gehört in ein **fachliches Entitlement- oder Permission-Modell**, nicht in Spring-Konfiguration.

**Besser:**

```yaml
features:
  premium-dashboard:
    enabled: true
```

und im Fachmodell (siehe QG-JAVA-006 v2 Sektion 14):

```java
if (!entitlements.hasAccess(tenantId, Feature.PREMIUM_DASHBOARD)) {
    throw new AccessDeniedException("Tenant has no access to premium dashboard.");
}
```

### 13.6 Flag-Wildwuchs

```java
// ❌ Anti-Pattern: alte Flags bleiben für immer
if (features.newCheckoutEnabled()) {
    return newCheckout.execute(command);
}
return legacyCheckout.execute(command);
```

Wenn `newCheckoutEnabled` seit Monaten überall aktiv ist, MUSS der Legacy-Pfad gelöscht werden. Siehe Sektion 21 für Lifecycle-Management.

### 13.7 Flag-Kombinationshölle mit `@ConditionalOnExpression`

```java
// ❌ Anti-Pattern
@Component
@ConditionalOnExpression(
    "${features.checkout.new-flow-enabled:false} and " +
    "${features.experimental:false} and " +
    "!${features.maintenance-mode:false}"
)
public class ExperimentalCheckout implements CheckoutStrategy { ... }
```

Mehrere Properties in einer Bedingung sind ein Warnsignal. Die Logik ist schwer zu verstehen, schwer zu testen, und meistens ein Symptom dafür, dass die fachliche Domäne nicht richtig modelliert ist.

**Besser:**

```java
@Component
public class ExperimentalCheckoutGuard {

    private final CheckoutFeatureProperties checkoutFeatures;
    private final ExperimentalFeatureProperties experimentalFeatures;
    private final MaintenanceProperties maintenanceProps;

    // ... Konstruktor

    public boolean shouldUseExperimentalCheckout(CheckoutCommand command) {
        if (maintenanceProps.enabled()) {
            return false;
        }
        if (!experimentalFeatures.enabled()) {
            return false;
        }
        return checkoutFeatures.newFlowEnabled();
    }
}
```

Die Logik lebt in benannten Methoden, ist testbar und sprechend.

### 13.8 Feature Flags im Domain Model

```java
// ❌ Anti-Pattern
public class Order {

    public void cancel(FeatureProperties features) {
        if (features.newCancellationEnabled()) {
            // ...
        }
    }
}
```

Domain-Klassen DÜRFEN NICHT von Spring-Konfiguration abhängen. Feature-Routing gehört in Application Services oder Router, nicht in Entities oder Value Objects. Siehe QG-JAVA-008 für vertiefende Diskussion zur Domänen-Modellierung.

**Besser:**

```java
@Service
public class OrderCancellationService {

    private final OrderCancellationFeatureProperties features;

    public void cancel(Order order, CancellationCommand command) {
        if (features.newCancellationEnabled()) {
            order.cancelWithCompensation(command);
        } else {
            order.cancel(command);
        }
    }
}
```

### 13.9 Flags als Testdatenersatz in produktiver Konfiguration

```yaml
# ❌ Anti-Pattern
features:
  always-return-test-user: true
  skip-payment-verification: true
```

Testhilfen gehören in Testkonfiguration, Test Fixtures oder lokale Mocks, nicht in produktive Feature-Konfiguration. Wenn solche Flags existieren und versehentlich in Production aktiv geschaltet werden, sind die Folgen schwerwiegend.

**Besser:** Test-spezifische Konfiguration in `application-test.yml`, Test-Container, dedizierte Mock-Komponenten in einem Test-Profile.

### 13.10 Flags ohne Entfernungskriterium

```yaml
# ⚠️ Wird zu technischer Schuld
features:
  new-checkout-flow:
    enabled: true
```

Wenn dieses Flag ein temporäres Release Flag ist, MUSS es ein Entfernungskriterium besitzen. Sonst wird es technische Schuld.

**Besser:** Ablauf-Kommentar oder dedizierte Annotation (siehe Sektion 21).

---

## 14. Profile und Property-Auflösungsreihenfolge

### 14.1 Profile-Dateien

Spring Profiles sind dafür gedacht, Teile der Anwendungskonfiguration für bestimmte Umgebungen zu trennen. Sie sind gut für `local`, `test`, `staging` und `prod`.

**Default-Konfiguration:**

```yaml
# application.yml
features:
  mail:
    use-mock-sender: false
  invoice-export:
    enabled: false
```

**Lokale Entwicklung:**

```yaml
# application-local.yml
features:
  mail:
    use-mock-sender: true
  invoice-export:
    enabled: true
```

**Produktion:**

```yaml
# application-prod.yml
features:
  mail:
    use-mock-sender: false
  invoice-export:
    enabled: false
```

### 14.2 Profil-Aktivierung

Profile können auf vier Wegen aktiviert werden:

| Methode | Beispiel | Anwendung |
|---|---|---|
| `application.yml` | `spring.profiles.active: prod` | Default für Deployment |
| Environment-Variable | `SPRING_PROFILES_ACTIVE=prod` | Container, Cloud |
| Command-Line | `--spring.profiles.active=prod` | CLI-Start |
| Java-System-Property | `-Dspring.profiles.active=prod` | JVM-Args |

### 14.3 Profil-Gruppen (Spring Boot 2.4+)

Bei komplexen Setups können mehrere Profile als logische Gruppe aktiviert werden:

```yaml
# application.yml
spring:
  profiles:
    group:
      production:
        - prod
        - cloud
        - metrics
      local:
        - local
        - mock-services
      ci:
        - test
        - in-memory-db
```

Aktivierung mit `--spring.profiles.active=production` aktiviert dann automatisch `prod`, `cloud` und `metrics`. Das ist Standard in modernen Setups.

### 14.4 Property-Auflösungsreihenfolge

Spring Boot wertet Konfigurationsquellen in fester Reihenfolge aus. Die spätere Quelle gewinnt:

| Reihenfolge | Quelle |
|---|---|
| 1 (höchste) | Command-Line-Argumente (`--key=value`) |
| 2 | `SPRING_APPLICATION_JSON` Env-Variable |
| 3 | Java-System-Properties (`-Dkey=value`) |
| 4 | OS-Environment-Variablen (`KEY=value`) |
| 5 | Profile-spezifische `application-{profile}.yml` |
| 6 | `application.yml` |
| 7 | `@PropertySource`-Annotationen |
| 8 (niedrigste) | Default-Werte im Code |

**Konsequenz:** Eine Environment-Variable `FEATURES_INVOICE_EXPORT_ENABLED=true` überschreibt **immer** den YAML-Wert, egal aus welchem Profile.

### 14.5 Profile-Anti-Pattern: Geschäftsvarianten

```yaml
# ❌ Anti-Pattern: Profile als Kundenmodell
spring:
  profiles:
    active: tenant-a
```

Mandanten-, Produkt- und Vertragslogik gehört in ein fachliches Entitlement- oder Permission-Modell, nicht in Spring-Profile.

---

## 15. Environment Overrides und Variable Mapping

In Container-, Cloud- oder CI/CD-Umgebungen werden YAML-Werte häufig durch Umgebungsvariablen überschrieben.

### 15.1 Standard-Mapping

YAML:

```yaml
features:
  invoice-export:
    enabled: false
```

Environment-Variable:

```bash
FEATURES_INVOICE_EXPORT_ENABLED=true
```

Spring Boot's **Relaxed Binding** akzeptiert mehrere Schreibweisen:

| Property-Format | Akzeptierte Environment-Variable(n) |
|---|---|
| `features.invoice-export.enabled` | `FEATURES_INVOICEEXPORT_ENABLED` oder `FEATURES_INVOICE_EXPORT_ENABLED` |
| `features.payment-provider.timeout` | `FEATURES_PAYMENTPROVIDER_TIMEOUT` oder `FEATURES_PAYMENT_PROVIDER_TIMEOUT` |
| `features.checkout.new-flow-enabled` | `FEATURES_CHECKOUT_NEWFLOWENABLED` oder `FEATURES_CHECKOUT_NEW_FLOW_ENABLED` |

**Wichtig:** Bindestriche im Property-Namen werden zu Unterstrichen oder entfernt. Beide Formen funktionieren — aber nicht `FEATURES_PAYMENT-PROVIDER_TIMEOUT` (mit Bindestrichen).

### 15.2 Listen und Maps in Environment-Variablen

```yaml
features:
  enabled-providers:
    - paypal
    - stripe
    - adyen
```

Environment-Variable:

```bash
FEATURES_ENABLEDPROVIDERS_0=paypal
FEATURES_ENABLEDPROVIDERS_1=stripe
FEATURES_ENABLEDPROVIDERS_2=adyen
```

Maps:

```yaml
features:
  provider-config:
    paypal:
      timeout: 5s
    stripe:
      timeout: 3s
```

Environment-Variable:

```bash
FEATURES_PROVIDERCONFIG_PAYPAL_TIMEOUT=5s
FEATURES_PROVIDERCONFIG_STRIPE_TIMEOUT=3s
```

Das Mapping ist nicht intuitiv und in CI/CD-Setups eine häufige Fehlerquelle. Wer Listen oder Maps über Environment-Variablen überschreibt, sollte das in Tests verifizieren.

### 15.3 Secret-Trennung

Secrets gehören nicht in Feature-Flag-Konfiguration:

```yaml
# ❌ Verboten
features:
  payment:
    provider-api-key: "secret-value"
```

Richtig ist die Trennung:

```yaml
# ✅ Feature-Flag und Secret getrennt
features:
  payment:
    external-provider-enabled: true
    provider: stripe

# Secret separat über Vault, Kubernetes Secret oder Cloud Secret Manager:
spring:
  config:
    import: vault://secret/payment
```

Siehe OWASP Secrets Management Cheat Sheet für vertiefende Diskussion.

---

## 16. Benennungskonvention

Feature Flags MÜSSEN sprechend, stabil und positiv benannt sein.

### 16.1 Gute Namen

```yaml
features:
  checkout:
    new-flow-enabled: false
    use-experimental-pricing: false
  invoice-export:
    enabled: true
  mail:
    sender-type: smtp
    use-mock-sender: false
  payment:
    provider: stripe
    use-sandbox: true
```

### 16.2 Schlechte Namen

```yaml
# ❌ Anti-Patterns
features:
  test: true                                # nichtssagend
  new: false                                # generisch
  flag1: true                               # technisch
  enabled: true                             # ohne Kontext
  customer-feature: true                    # mehrdeutig
  legacy-checkout-disabled: false           # negative Boolean → doppelte Negation
  tenant-acme-premium-enabled: true         # tenant-spezifisch im Namen
```

### 16.3 Konvention im Detail

| Aspekt | Regel | Beispiel |
|---|---|---|
| Namespace | Immer unter `features.*` | `features.invoice-export.enabled` |
| Schreibweise | `kebab-case` in YAML | `new-flow-enabled` |
| Boolean-Form | Positive Formulierung | `use-mock-sender`, nicht `mock-sender-disabled` |
| Multi-Variant | String oder Enum, kein Boolean | `sender-type: smtp` |
| Suffix `-enabled` | Bei reinen On/Off-Flags | `features.invoice-export.enabled` |
| Suffix `use-` | Bei Mock/Test-Schaltern | `features.mail.use-mock-sender` |
| Verwendung von Substantiven | Funktion oder Bereich nennen | `checkout.new-flow-enabled`, nicht `new-checkout` |
| Stabilität | Einmal benannt, möglichst nicht umbenennen | Migrationen sind teuer |

---

## 17. Default-Werte und sicheres Verhalten

Default-Werte sind Architekturentscheidungen. Sie MÜSSEN bewusst gesetzt werden.

### 17.1 Sicherer Default für neue Funktionalität

Für neue, riskante oder experimentelle Funktionen MUSS der Default `enabled=false` sein:

```java
@ConditionalOnProperty(
        prefix = "features.invoice-export",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false        // ✅ Default: deaktiviert
)
```

Wenn das Flag fehlt, ist die Funktion deaktiviert. Das ist der sichere Default für neue Funktionalität.

### 17.2 Sicherer Default für stabile Standardimplementierungen

Für stabile Standardimplementierungen kann `matchIfMissing = true` sinnvoll sein:

```java
@ConditionalOnProperty(
        prefix = "features.mail",
        name = "use-mock-sender",
        havingValue = "false",
        matchIfMissing = true        // ✅ Default: produktiver SmtpMailSender
)
public class SmtpMailSender implements MailSender { ... }
```

Diese Variante bedeutet: Wenn nichts anderes konfiguriert ist, nutze den normalen produktiven Pfad.

### 17.3 Wann `matchIfMissing=true` verboten ist

`matchIfMissing=true` DARF NICHT eingesetzt werden, wenn ein fehlender Konfigurationswert zu **riskanter Aktivierung** führen kann:

```java
// ❌ Gefährlich
@ConditionalOnProperty(
        prefix = "features.experimental-engine",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true        // ⚠️ aktiviert Experimental ohne explizite Konfiguration!
)
```

Wenn ein Engineer vergisst, `features.experimental-engine.enabled=false` zu setzen, ist das Feature aktiv. Das ist Fail-Open statt Fail-Closed.

### 17.4 Defaults im Properties-Record

Defaults können auch direkt im Record gesetzt werden:

```java
@ConfigurationProperties(prefix = "features.invoice-export")
@Validated
public record InvoiceExportProperties(
        boolean enabled,
        @Positive int maxBatchSize,
        boolean includeAttachments,
        @NotNull ExportFormat exportFormat
) {
    // ✅ Defaults via static factory
    public static InvoiceExportProperties defaults() {
        return new InvoiceExportProperties(false, 100, false, ExportFormat.PDF);
    }
}
```

Spring Boot füllt fehlende Werte mit Default-Werten von Java (false, 0, null) — das ist oft nicht das gewünschte Verhalten. Explizite Defaults im YAML oder über Validation-Constraints (`@Min`, `@NotNull`) sind robuster.

---

## 18. Abgrenzung zu `@Profile` und `@ConditionalOnExpression`

`@ConditionalOnProperty` ist nicht der einzige Mechanismus für statisches Flagging. Drei verwandte Mechanismen werden häufig verwechselt.

### 18.1 `@Profile`

```java
@Component
@Profile("local")
public class LoggingMailSender implements MailSender { ... }
```

| Wann `@Profile` | Wann `@ConditionalOnProperty` |
|---|---|
| Aktivierung an einer **Umgebung** hängt (`local`, `staging`, `prod`) | Aktivierung an einer **fachlichen Funktion** hängt |
| Bündelung verwandter Konfiguration | Einzelne Feature-Entscheidung |
| Top-Level-Schalter | Feingranulare Steuerung |

**Beispiel für `@Profile`:**

```java
@Component
@Profile("!production")
public class TestDataInitializer { ... }      // nur außerhalb Production
```

**Beispiel für `@ConditionalOnProperty`:**

```java
@Component
@ConditionalOnProperty(prefix = "features.mail", name = "use-mock-sender", havingValue = "true")
public class LoggingMailSender implements MailSender { ... }
```

`@Profile` ist syntaktischer Zucker für `@ConditionalOnExpression` mit String-Matching auf `spring.profiles.active`. Beide sind valide, haben aber unterschiedliche semantische Konnotationen.

### 18.2 `@ConditionalOnExpression`

```java
// ⚠️ Vorsicht: meistens Anti-Pattern
@Component
@ConditionalOnExpression(
    "${features.checkout.new-flow-enabled:false} and ${features.experimental:false}"
)
public class ExperimentalCheckout implements CheckoutStrategy { ... }
```

`@ConditionalOnExpression` erlaubt SpEL-Ausdrücke und damit beliebig komplexe Boolean-Logik. Das ist mächtig, aber meistens ein Anti-Pattern (siehe Sektion 13.7) — die Logik ist schwer zu verstehen, schwer zu testen, und meistens ein Zeichen für unsaubere Modellierung.

**Wann `@ConditionalOnExpression` legitim ist:**

- Einzelne kombinierte Bedingung, die fachlich zusammengehört.
- Property muss ein bestimmtes Format haben (`@ConditionalOnExpression("'${env}' matches '^prod-.+'")`).
- Vergleich von Property-Werten (`@ConditionalOnExpression("'${region}' == 'eu-west-1'")`).

**Wann nicht:**

- Mehr als zwei Properties kombiniert.
- Verschachtelte Boolean-Logik mit `and`/`or`/`!`.
- Conditions, die nicht in zwei Sätzen erklärt werden können.

### 18.3 Auswahlmatrix

| Anforderung | Mechanismus |
|---|---|
| Bean-Auswahl an einer einzelnen Boolean-Property | `@ConditionalOnProperty` |
| Bean-Auswahl an Multi-Variant-String | `@ConditionalOnProperty` mit `havingValue` |
| Bean nur in bestimmten Umgebungen aktiv | `@Profile` |
| Bean an komplexe Property-Kombination | `@ConditionalOnExpression` (sparsam!) |
| Bean an existierende andere Bean | `@ConditionalOnBean` / `@ConditionalOnMissingBean` |
| Bean an Klassenpfad-Verfügbarkeit | `@ConditionalOnClass` |

---

## 19. Security- und SaaS-Aspekte

### 19.1 Feature Flags sind keine Sicherheitsgrenze

Ein Feature Flag DARF NIEMALS die einzige Prüfung für eine sicherheitsrelevante Aktion sein. Es entscheidet über Aktivierung, nicht über Berechtigung.

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
| --- | --- | --- | --- |
| Autorisierung | Nutzerrechte MÜSSEN separat geprüft werden | `canDeleteTenant(...)` | Pflicht |
| Mandantentrennung | Tenant-Kontext DARF NICHT aus Flag-Konfiguration abgeleitet werden | `tenantId` aus Auth-Kontext | Pflicht |
| Sensitive Daten | Flags DÜRFEN keine Secrets enthalten | API-Key, Token | Verboten |
| Logging | Flag-Werte DÜRFEN keine vertraulichen Informationen enthalten | Kein Token in Flag-Namen | Pflicht |
| Ressourcenverbrauch | Aktivierte Features können Last erhöhen | Export, AI, Reporting | Limits nötig |
| Auditierbarkeit | Kritische Flag-Änderungen MÜSSEN nachvollziehbar sein | produktives Umschalten | Betriebskonzept nötig |

### 19.2 Ressourcenverbrauch

Ein statisch aktiviertes Feature kann die Last deutlich verändern. Beispiele sind Exporte, Recommender, PDF-Generierung, AI-Aufrufe, Hintergrundjobs oder externe API-Calls. Für solche Features MÜSSEN Timeouts, Rate Limits, Batch-Größen und Monitoring mitgedacht werden.

```yaml
features:
  invoice-export:
    enabled: true
    max-batch-size: 500
    timeout: 30s
```

`max-batch-size` und `timeout` sind hier keine Dekoration. Sie sind Schutzgrenzen.

### 19.3 Mandantenfähigkeit

In SaaS-Systemen DARF ein statisches Flag nicht darüber entscheiden, ob ein bestimmter Mandant fachlich Zugriff auf eine Funktion hat. Das gehört in ein **Entitlement-Modell**.

**Falsch:**

```yaml
features:
  tenant-acme-premium-dashboard-enabled: true
```

**Besser:**

```yaml
features:
  premium-dashboard:
    enabled: true
```

und im Fachmodell (Cross-Reference auf QG-JAVA-006 v2 Sektion 14):

```java
import com.example.tenant.TenantContext;
import com.example.entitlement.EntitlementService;

@Service
public class PremiumDashboardService {

    private final PremiumDashboardFeatureProperties features;
    private final TenantContext tenantContext;
    private final EntitlementService entitlements;

    public PremiumDashboardService(
            PremiumDashboardFeatureProperties features,
            TenantContext tenantContext,
            EntitlementService entitlements
    ) {
        this.features = features;
        this.tenantContext = tenantContext;
        this.entitlements = entitlements;
    }

    public DashboardData loadDashboard() {
        // 1. globale Aktivierung
        if (!features.enabled()) {
            throw new FeatureDisabledException();
        }

        // 2. Tenant-Berechtigung
        var tenantId = tenantContext.currentTenantId();
        if (!entitlements.hasAccess(tenantId, Feature.PREMIUM_DASHBOARD)) {
            throw new AccessDeniedException("Tenant has no access to premium dashboard.");
        }

        return loadDashboardData();
    }
}
```

Drei Ebenen der Prüfung:

1. **Statisches Flag:** ist die Funktion in dieser Anwendung global aktiviert?
2. **Tenant-Kontext:** in welchem Tenant arbeiten wir? (siehe QG-JAVA-006 v2 Sektion 14.6)
3. **Entitlement:** hat dieser Tenant die Berechtigung?

Alle drei sind unabhängig. Keine ersetzt die anderen.

---

## 20. Teststrategie

### 20.1 Bean-Auswahl testen

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
    void createsSmtpMailSender_whenUseMockSenderIsFalse() {
        contextRunner
                .withPropertyValues("features.mail.use-mock-sender=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(SmtpMailSender.class);
                    assertThat(context).doesNotHaveBean(LoggingMailSender.class);
                });
    }

    @Test
    void createsLoggingMailSender_whenUseMockSenderIsTrue() {
        contextRunner
                .withPropertyValues("features.mail.use-mock-sender=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingMailSender.class);
                    assertThat(context).doesNotHaveBean(SmtpMailSender.class);
                });
    }

    @Test
    void usesDefaultSmtpMailSender_whenPropertyIsMissing() {
        contextRunner
                .run(context -> {
                    // matchIfMissing=true → SmtpMailSender ist Default
                    assertThat(context).hasSingleBean(SmtpMailSender.class);
                });
    }
}
```

### 20.2 Properties mit Validation testen

`new InvoiceExportProperties(false, -1, false, ...)` umgeht die Spring-Validierung. Der korrekte Test verwendet `ApplicationContextRunner`:

```java
class InvoiceExportPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(FeatureConfiguration.class);

    @Test
    void rejectsNegativeBatchSize() {
        contextRunner
                .withPropertyValues(
                        "features.invoice-export.enabled=true",
                        "features.invoice-export.max-batch-size=-1",
                        "features.invoice-export.export-format=PDF"
                )
                .run(context -> {
                    // ✅ Application-Start scheitert
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
                });
    }

    @Test
    void acceptsValidConfiguration() {
        contextRunner
                .withPropertyValues(
                        "features.invoice-export.enabled=true",
                        "features.invoice-export.max-batch-size=500",
                        "features.invoice-export.export-format=PDF"
                )
                .run(context -> {
                    var props = context.getBean(InvoiceExportProperties.class);
                    assertThat(props.maxBatchSize()).isEqualTo(500);
                    assertThat(props.exportFormat()).isEqualTo(ExportFormat.PDF);
                });
    }

    @Test
    void rejectsInvalidExportFormat() {
        contextRunner
                .withPropertyValues(
                        "features.invoice-export.enabled=true",
                        "features.invoice-export.max-batch-size=500",
                        "features.invoice-export.export-format=INVALID"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }
}
```

### 20.3 Duration und DataSize testen

```java
@Test
void timeoutWithoutUnit_isInterpretedAsSeconds() {
    contextRunner
            .withPropertyValues(
                    "features.payment-provider.enabled=true",
                    "features.payment-provider.provider=stripe",
                    "features.payment-provider.timeout=5"
            )
            .run(context -> {
                var props = context.getBean(PaymentProviderProperties.class);
                assertThat(props.timeout()).isEqualTo(Duration.ofSeconds(5));
            });
}

@Test
void timeoutWithExplicitMillis_isInterpretedAsMillis() {
    contextRunner
            .withPropertyValues(
                    "features.payment-provider.enabled=true",
                    "features.payment-provider.provider=stripe",
                    "features.payment-provider.timeout=500ms"
            )
            .run(context -> {
                var props = context.getBean(PaymentProviderProperties.class);
                assertThat(props.timeout()).isEqualTo(Duration.ofMillis(500));
            });
}
```

### 20.4 Verhalten testen

Wenn ein Service sein Verhalten anhand von Properties ändert, kann die Properties-Klasse direkt instanziiert werden — solange keine Bean-Validation getestet werden soll:

```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceExportServiceTest {

    @Test
    void export_returnsDisabledResult_whenFeatureIsDisabled() {
        // ⚠️ Nur wenn Bean-Validation nicht relevant ist
        var properties = new InvoiceExportProperties(false, 500, false, ExportFormat.PDF);
        var service = new InvoiceExportService(properties);

        var result = service.export(new ExportCommand());

        assertThat(result.enabled()).isFalse();
        assertThat(result.reason()).contains("disabled");
    }

    @Test
    void export_startsExport_whenFeatureIsEnabled() {
        var properties = new InvoiceExportProperties(true, 500, false, ExportFormat.PDF);
        var service = new InvoiceExportService(properties);

        var result = service.export(new ExportCommand());

        assertThat(result.enabled()).isTrue();
    }
}
```

### 20.5 Test-Konventionen

| Aspekt | Test-Werkzeug |
|---|---|
| Bean-Auswahl mit `@ConditionalOnProperty` | `ApplicationContextRunner` mit `withPropertyValues` |
| Properties mit Bean Validation | `ApplicationContextRunner` mit `hasFailed()`-Assertions |
| Duration/DataSize-Bindung | `ApplicationContextRunner` mit verschiedenen Format-Werten |
| Service-Verhalten bei Flag-Zuständen | direkter Konstruktor-Aufruf, isolierte Unit-Tests |
| Profil-spezifische Konfiguration | `@SpringBootTest(properties = "spring.profiles.active=...")` |
| End-to-End mit allen Flags | volles `@SpringBootTest` (sparsam einsetzen) |

---

## 21. Lifecycle-Management und Entfernungskriterium

Temporäre Release- oder Migrationsflags MÜSSEN ein Entfernungskriterium besitzen. Sonst werden sie zu technischer Schuld.

### 21.1 Flag-Lifecycle-Stadien

| Stadium | Beschreibung | Beispiel |
|---|---|---|
| **Temporär — Migration** | Wird während Migration verwendet, danach entfernt | `features.checkout.new-flow-enabled` |
| **Temporär — Release** | Schaltet neue Funktion ein, nach Stabilisierung entfernt | `features.invoice-export.enabled` |
| **Dauerhaft — Environment-Konfiguration** | Bleibt bestehen, unterscheidet Umgebungen | `features.mail.use-mock-sender` |
| **Dauerhaft — Betriebsoption** | Bleibt bestehen, ermöglicht Betriebsanpassung | `features.expensive-report.enabled` |

### 21.2 Annotation für Flag-Metadaten

Eine projekteigene Annotation kann das Lifecycle-Stadium am Code sichtbar machen:

```java
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface FeatureFlag {
    Lifecycle lifecycle();
    String introducedDate();        // ISO-8601: 2026-04-15
    String expectedRemovalDate() default "";
    String ticket() default "";
    String owner() default "";

    enum Lifecycle {
        TEMPORARY_MIGRATION,
        TEMPORARY_RELEASE,
        PERMANENT_ENVIRONMENT,
        PERMANENT_OPERATIONAL
    }
}
```

Verwendung:

```java
@FeatureFlag(
    lifecycle = Lifecycle.TEMPORARY_MIGRATION,
    introducedDate = "2026-04-15",
    expectedRemovalDate = "2026-06-15",
    ticket = "ENG-1234",
    owner = "team-billing"
)
@ConfigurationProperties(prefix = "features.invoice-export")
@Validated
public record InvoiceExportProperties(...) {}
```

### 21.3 YAML-Kommentar-Schema

Alternativ als Konvention im `application.yml`:

```yaml
features:
  invoice-export:
    # @flag-type: temporary-release
    # @flag-introduced: 2026-04-15
    # @flag-expires: 2026-06-15
    # @flag-owner: team-billing
    # @flag-ticket: ENG-1234
    enabled: false
```

### 21.4 CI-Check für abgelaufene Flags

```bash
#!/bin/bash
# check-expired-flags.sh
# Warnt bei Flags älter als 90 Tage ohne Removal-Date

NOW=$(date +%s)
THRESHOLD=$((NOW - 90 * 86400))

grep -B1 "@flag-introduced" application*.yml | \
    awk '/@flag-introduced/ {
        date=$3;
        cmd="date -d \"" date "\" +%s";
        cmd | getline introduced;
        close(cmd);
        if (introduced < '"$THRESHOLD"') {
            print "WARNING: Flag introduced > 90 days ago:", $0
        }
    }'
```

In CI als Gate:

```yaml
- name: Check expired feature flags
  run: ./check-expired-flags.sh
```

### 21.5 Pull-Request-Template

```markdown
## Feature Flag Checklist

- [ ] Flag liegt unter `features.*`?
- [ ] Boolean-Name ist positiv formuliert?
- [ ] Bei 3+ Varianten: Enum/String statt Boolean?
- [ ] Bei `Duration`-Properties: `@DurationUnit` gesetzt?
- [ ] Bean-Validation-Annotationen für strukturelle Constraints?
- [ ] Tests für aktiviert UND deaktiviert?
- [ ] `matchIfMissing=true` begründet?
- [ ] Lifecycle-Stadium dokumentiert?
- [ ] Wenn temporär: Ablauf-Datum und Ticket angegeben?
```

### 21.6 Periodische Reviews

Alle 3 Monate sollten Flag-Reviews durchgeführt werden:

1. Welche Flags sind dauerhaft auf einem Wert? → entfernen.
2. Welche Flags sind seit > 6 Monaten temporär? → eskalieren.
3. Welche Flags haben keinen Owner mehr? → Verantwortlichkeit klären.
4. Welche Flags werden nicht mehr referenziert? → Tote Flags entfernen.

---

## 22. Migration bestehender Flags

Bestehende statische Flags werden nach folgendem Vorgehen bereinigt.

### 22.1 Schritt 1: Inventar erstellen

Alle Properties suchen:

```bash
grep -R "features\." src/main src/test
grep -R "@Value" src/main/java
grep -R "@ConditionalOnProperty" src/main/java
grep -R "enabled" src/main/resources/application*.yml
```

### 22.2 Schritt 2: Namespace vereinheitlichen

**Vorher:**

```yaml
newCheckout: true
useMockMail: false
exportEnabled: true
```

**Nachher:**

```yaml
features:
  checkout:
    new-flow-enabled: true
  mail:
    use-mock-sender: false
  invoice-export:
    enabled: true
```

### 22.3 Schritt 3: Negative Booleans positiv umformulieren

**Vorher:**

```yaml
features:
  legacy-flow:
    disabled: false
```

**Nachher:**

```yaml
features:
  new-flow:
    enabled: true
```

### 22.4 Schritt 4: `@ConfigurationProperties` einführen

**Vorher:**

```java
@Value("${newCheckout:false}")
private boolean newCheckout;
```

**Nachher:**

```java
@ConfigurationProperties(prefix = "features.checkout")
@Validated
public record CheckoutFeatureProperties(boolean newFlowEnabled) {}

@Service
public class CheckoutService {
    private final CheckoutFeatureProperties checkoutFeatures;
    // Konstruktor-Injektion
}
```

### 22.5 Schritt 5: Multi-Boolean zu Multi-Variant umstellen

**Vorher:**

```yaml
features:
  mail:
    use-mock-sender: false
    use-ses: false
    use-sendgrid: true
```

```java
if (props.useSes()) { ... }
else if (props.useSendgrid()) { ... }
else if (props.useMockSender()) { ... }
else { ... }
```

**Nachher:**

```yaml
features:
  mail:
    sender-type: sendgrid
```

```java
@Bean
@ConditionalOnProperty(prefix = "features.mail", name = "sender-type", havingValue = "sendgrid")
public MailSender sendgridMailSender() { ... }
```

### 22.6 Schritt 6: `Duration` und `DataSize` korrekt binden

**Vorher:**

```yaml
features:
  payment:
    timeout: 2000     # ⚠️ ms? Sekunden? Unklar!
```

```java
private long timeoutMs;       // ⚠️ String → long, fehleranfällig
```

**Nachher:**

```yaml
features:
  payment:
    timeout: 2s       # ✅ explizit
```

```java
@DurationUnit(ChronoUnit.SECONDS) Duration timeout;
```

### 22.7 Schritt 7: Bean Validation einführen

**Vorher:**

```java
public record Properties(int maxBatchSize) {
    public Properties {
        if (maxBatchSize <= 0) throw new IllegalArgumentException();
    }
}
```

**Nachher:**

```java
@Validated
public record Properties(@Positive int maxBatchSize) {}
```

### 22.8 Schritt 8: Tests ergänzen

Für jedes relevante Flag werden aktivierter und deaktivierter Zustand getestet — mit `ApplicationContextRunner`, nicht nur direkter Konstruktion.

### 22.9 Schritt 9: Lifecycle dokumentieren

Annotation oder YAML-Kommentar-Schema (Sektion 21.2 / 21.3) ergänzen.

### 22.10 Schritt 10: Temporäre Flags entfernen

Wenn ein Release Flag vollständig aktiviert wurde, wird der alte Codepfad gelöscht, das Flag aus `application.yml` entfernt und die Tests werden bereinigt.

---

## 23. Review-Checkliste

Im Pull Request sind folgende Fragen zu prüfen. Jede Zeile verweist auf die Detail-Sektion mit der Begründung.

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| Zweck | Ist klar, warum das Flag existiert? | §1 |
| Typ | Ist es wirklich ein statisches Flag? | §7 |
| Namespace | Liegt es unter `features.*`? | §3.1.1 |
| Name | Ist der Name sprechend und stabil? | §16 |
| Boolean-Naming | Ist die Boolean positiv formuliert? | §3.1.3, §13.2 |
| Multi-Variant | Bei 3+ Optionen: Enum/String statt Boolean? | §3.1.4, §9 |
| `@DurationUnit` | Bei Duration-Properties gesetzt? | §3.1.5, §12 |
| `@DataSizeUnit` | Bei DataSize-Properties gesetzt? | §3.1.6, §12 |
| Validation | Bean-Validation-Annotationen für Constraints? | §3.1.7, §10 |
| Compact Constructor | Komplexe Constraints im Constructor? | §3.1.8, §10.2 |
| Zugriff | Wird `@ConfigurationProperties` oder `@ConditionalOnProperty` genutzt? | §10, §13.1 |
| Default | Ist der Default sicher? | §3.1.11, §17 |
| `matchIfMissing` | Ist `matchIfMissing=true` begründet? | §3.1.11, §17.3 |
| Tests | Gibt es Tests für aktiv und deaktiviert? | §3.1.9, §20 |
| Test-Werkzeug | `ApplicationContextRunner` für Validation-Tests? | §3.1.10, §20.2 |
| Lifecycle | Ist Lifecycle-Stadium dokumentiert? | §3.1.13, §21 |
| Entfernungskriterium | Bei temporären Flags: Ablauf-Datum gesetzt? | §3.1.13, §21 |
| Security | Ersetzt das Flag keine Autorisierung? | §3.1.16, §13.3 |
| SaaS | Versteckt das Flag keine Mandantenlogik? | §13.5, §19.3 |
| Tenant | Tenant-spezifische Logik im Entitlement-Modell? | §19.3 |
| Secrets | Enthält das Flag keine Secrets? | §3.1.15, §15.3 |
| Monitoring | Ist Betriebswirkung beobachtbar? | §19.2 |
| Cross-Reference | QG-JAVA-006 v2 für Service-Layer-Themen konsultiert? | §19.3 |

---

## 24. Automatisierbare Prüfungen

### 24.1 ArchUnit-Regeln

```java
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Value;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class FeatureFlagArchitectureTest {

    // ✅ Services dürfen keine @Value für Feature Flags verwenden
    @ArchTest
    static final ArchRule services_should_not_inject_feature_flags_with_value =
        noFields()
            .that().areDeclaredInClassesThat().resideInAPackage("..service..")
            .should().beAnnotatedWith(Value.class)
            .because("Feature Flags sollen über @ConfigurationProperties gelesen werden, " +
                     "nicht über verstreutes @Value.");

    // ✅ @ConfigurationProperties für Feature Flags müssen @Validated haben
    @ArchTest
    static final ArchRule feature_properties_must_be_validated =
        classes()
            .that().areAnnotatedWith("org.springframework.boot.context.properties.ConfigurationProperties")
            .and().haveNameMatching(".*FeatureProperties$")
            .should().beAnnotatedWith("org.springframework.validation.annotation.Validated");

    // ✅ Domain-Klassen dürfen nicht von FeatureProperties abhängen
    @ArchTest
    static final ArchRule domain_should_not_depend_on_feature_properties =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().haveNameMatching(".*FeatureProperties$");
}
```

### 24.2 Semgrep-Regeln

```yaml
# semgrep-rules.yml
rules:
  - id: feature-flag-not-in-features-namespace
    message: "Feature Flag muss unter `features.*` liegen"
    severity: ERROR
    languages: [yaml]
    pattern-regex: '^(?!features:)(checkout|mail|payment|invoice).*enabled:'

  - id: feature-flag-negative-boolean
    message: "Negative Boolean-Naming vermeiden — `enabled` statt `disabled`"
    severity: WARNING
    languages: [yaml]
    pattern-regex: '\w+-disabled:|disabled:\s*(true|false)'

  - id: feature-flag-tenant-specific
    message: "Tenant-spezifische Flag-Namen sind verboten — Entitlement-Modell verwenden"
    severity: ERROR
    languages: [yaml]
    pattern-regex: 'features:.*tenant-[a-z]+-'

  - id: feature-flag-secret-in-name
    message: "Flag-Name enthält Secret-Indikator — Secrets gehören in Secret Store"
    severity: ERROR
    languages: [yaml]
    pattern-regex: 'features:.*(password|secret|token|api-key|apikey)'

  - id: configuration-properties-without-validated
    message: "@ConfigurationProperties für Features sollte @Validated haben"
    severity: WARNING
    languages: [java]
    patterns:
      - pattern: |
          @ConfigurationProperties(prefix = "features.$X")
          $TYPE $NAME(...)
      - pattern-not-inside: |
          @Validated
          @ConfigurationProperties(prefix = "features.$X")
          $TYPE $NAME(...)

  - id: duration-without-unit
    message: "Duration-Property ohne @DurationUnit kann zu falscher Interpretation führen"
    severity: WARNING
    languages: [java]
    patterns:
      - pattern: Duration $NAME
      - pattern-not-inside: |
          @DurationUnit($UNIT) Duration $NAME
```

### 24.3 CI-Gates

Für produktionsnahe Codebasen sollten folgende Gates existieren:

1. ArchUnit-Regeln laufen in jedem Test-Run.
2. Semgrep-Regeln laufen in CI für Pull Requests.
3. `check-expired-flags.sh` blockt Build bei Flags älter als 90 Tage ohne Removal-Date.
4. Coverage für Properties-Records und `@ConditionalOnProperty`-Beans wird gemessen.
5. Spring Boot Configuration Metadata (`META-INF/spring-configuration-metadata.json`) wird im Build erzeugt und versioniert.

---

## 25. Ausnahmen

Abweichungen von dieser Richtlinie sind zulässig, wenn sie bewusst und nachvollziehbar sind.

Mögliche Ausnahmen:

1. Sehr kleine interne Tools mit minimaler Konfiguration.
2. Legacy-Code, der nur minimal geändert wird.
3. Externe Library mit eigenem Konfigurations-Modell.
4. Performance-kritische Pfade, in denen ein Routing-Pattern zu teuer wäre.
5. Bestehende `@Value`-basierte Konfiguration während einer Migration.
6. Bewusst gewählte CQRS-, Hexagonal- oder modulare Architekturvarianten mit eigenem Standard.

Nicht zulässig als Begründung:

1. „Das war schneller."
2. „Das machen wir immer so."
3. „Bean-Validation ist zu streng."
4. „Wir testen das später."
5. „`@Value` ist doch einfacher."
6. „Negative Booleans sind doch klar."
7. „`Duration` funktioniert auch ohne `@DurationUnit`."
8. „Tenant-Logik in Flag-Namen ist pragmatisch."

---

## 26. Definition of Done

Ein statisches Feature Flag erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Das Flag liegt unter `features.*`.
2. Der Name beschreibt Kontext und Zweck.
3. Boolean-Namen sind positiv formuliert.
4. Bei 3+ Varianten ist ein Enum/String-Pattern verwendet, kein Boolean.
5. `Duration`-Properties haben `@DurationUnit`.
6. `DataSize`-Properties haben `@DataSizeUnit`.
7. `@ConfigurationProperties`-Records sind mit `@Validated` annotiert.
8. Strukturelle Constraints sind über Bean-Validation-Annotationen abgebildet.
9. Komplexe Constraints sind im Compact Constructor des Records validiert.
10. Der Zugriff erfolgt über `@ConfigurationProperties` oder `@ConditionalOnProperty`, nicht über verstreutes `@Value` im Fachcode.
11. Aktivierter und deaktivierter Zustand sind getestet.
12. Validation-Tests verwenden `ApplicationContextRunner` mit `withPropertyValues`.
13. Default-Wert ist bewusst gewählt und sicher.
14. `matchIfMissing=true` ist nur dort eingesetzt, wo ein fehlender Wert wirklich sicher ist.
15. Lifecycle-Stadium ist dokumentiert (Annotation oder YAML-Kommentar-Schema).
16. Temporäre Flags besitzen ein Entfernungskriterium mit Datum.
17. Das Flag ersetzt keine Autorisierung und keine Mandantentrennung.
18. Tenant-Logik ist im Entitlement-Modell, nicht im Flag-Namen.
19. Das Flag enthält keine Secrets.
20. Betriebsrelevante Flags haben Monitoring-, Timeout- oder Ressourcenüberlegungen.
21. Pull-Request-Review berücksichtigt die Checkliste aus Sektion 23.
22. Cross-References zu QG-JAVA-006 v2 für Service-Layer-Themen sind beachtet.

---

## 27. Entscheidungsbaum

```
Ist die Entscheidung beim Start der Anwendung ausreichend?
├─ Nein → dynamisches Feature-Flag-System (OpenFeature, Unleash, FF4J) oder operative Control Plane verwenden.
└─ Ja
   ├─ Soll der Wert zur Laufzeit nachladbar sein?
   │  ├─ Ja → @ConfigurationProperties mit @RefreshScope (Spring Cloud erforderlich).
   │  └─ Nein → weiter unten.
   ├─ Geht es um Bean-Auswahl?
   │  ├─ Zwei Varianten?
   │  │  └─ Boolean + @ConditionalOnProperty (Sektion 8).
   │  └─ Drei oder mehr Varianten?
   │     └─ Enum/String + @ConfigurationProperties + @Bean-Methoden (Sektion 9).
   ├─ Geht es um mehrere zusammengehörige Werte?
   │  └─ @ConfigurationProperties Record mit Bean Validation (Sektion 10).
   ├─ Geht es um Duration oder DataSize?
   │  └─ @DurationUnit / @DataSizeUnit explizit setzen (Sektion 12).
   ├─ Geht es um Routing zwischen Implementierungen?
   │  ├─ Beide immer initialisierbar?
   │  │  └─ Router-Pattern (Sektion 11).
   │  └─ Nur eine soll initialisiert werden?
   │     └─ Bean-Pattern mit @ConditionalOnProperty (Sektion 11.6).
   ├─ Geht es um Nutzer-, Tenant- oder Prozent-Rollout?
   │  └─ Statisches Flag NICHT verwenden — dynamisches System.
   ├─ Geht es um Autorisierung?
   │  └─ Permission-/Entitlement-Modell (siehe QG-JAVA-006 v2 Sektion 14).
   ├─ Geht es um Mandantentrennung?
   │  └─ Tenant-Context-Pattern (siehe QG-JAVA-006 v2 Sektion 14.6).
   ├─ Sind Secrets im Spiel?
   │  └─ Vault, Kubernetes Secrets, Cloud Secret Manager — niemals in Flag-Konfiguration.
   ├─ Komplexe Boolean-Kombination mehrerer Properties?
   │  └─ Domänen-Logik in dediziertem Service-Klass-Methode (NICHT @ConditionalOnExpression).
   └─ Sonst:
      └─ statisches Flag über application.yml ist geeignet.
```

---

## 28. Quellen und weiterführende Literatur

* Spring Boot Reference — Externalized Configuration: <https://docs.spring.io/spring-boot/reference/features/external-config.html>
* Spring Boot Reference — `@ConfigurationProperties`: <https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties>
* Spring Boot Reference — Profiles: <https://docs.spring.io/spring-boot/reference/features/profiles.html>
* Spring Boot Reference — Profile Groups: <https://docs.spring.io/spring-boot/reference/features/profiles.html#features.profiles.groups>
* Spring Boot Reference — `@ConditionalOnProperty`: <https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/condition/ConditionalOnProperty.html>
* Spring Boot Reference — `@ConfigurationPropertiesScan`: <https://docs.spring.io/spring-boot/api/java/org/springframework/boot/context/properties/ConfigurationPropertiesScan.html>
* Spring Boot Reference — Conversion of Durations: <https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.durations>
* Spring Boot Reference — Conversion of Data Sizes: <https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.data-sizes>
* Spring Boot Reference — Validation: <https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.validation>
* Spring Boot Reference — Relaxed Binding: <https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding>
* Spring Boot Reference — Property Resolution Order: <https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config>
* Spring Cloud Config — `@RefreshScope`: <https://docs.spring.io/spring-cloud-config/docs/current/reference/html/>
* Java Records — JEP 395: <https://openjdk.org/jeps/395>
* Jakarta Bean Validation 3.0: <https://jakarta.ee/specifications/bean-validation/3.0/>
* Hibernate Validator 8 Reference: <https://docs.jboss.org/hibernate/validator/8.0/reference/en-US/html_single/>
* OpenFeature — Vendor-neutral Feature Flag API: <https://openfeature.dev/>
* Unleash — Open Source Feature Management: <https://www.getunleash.io/>
* FF4J — Feature Flipping for Java: <https://ff4j.github.io/>
* Martin Fowler — Feature Toggles: <https://martinfowler.com/articles/feature-toggles.html>
* OWASP Secrets Management Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html>

---

*Diese Richtlinie ersetzt die Erstfassung vom 2026-05-02 (v1.0). Für inhaltliche Querverweise auf Service-Layer-Themen siehe QG-JAVA-006 v2 (Spring-Boot-Serviceschicht), insbesondere Sektion 14 (Security- und SaaS-Aspekte) und Sektion 14.6 (Tenant-Context-Pattern). Für Domänenmodellierung siehe QG-JAVA-008 (Objektorientierung). Für API-Verträge containerisierter Services siehe QG-JAVA-019 v2 (Contract Testing) und QG-JAVA-126 v2 (Docker).*
