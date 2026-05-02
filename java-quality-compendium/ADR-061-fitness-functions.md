# ADR-061 — Architecture Fitness Functions: Architektur kontinuierlich messen

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · ArchUnit · JUnit 5           |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Qualitätssicherung  |

---

## Kontext & Problem

Architekturentscheidungen (ADRs) werden getroffen und dann schleichend untergraben — nicht aus böser Absicht, sondern weil niemand alles im Blick behält. "Wir hatten entschieden dass Domain kein Spring kennt" — drei Monate später: `@Autowired` in der Entity. **Architecture Fitness Functions** sind automatische Tests die Architektur-Eigenschaften kontinuierlich messen und bei Verletzung den Build brechen.

---

## Was ist eine Fitness Function?

```
Fitness Function = automatisch ausführbarer Test einer Architektur-Eigenschaft

Arten:
  Atomare Fitness Functions:   prüfen eine einzelne Eigenschaft
  Holistische Fitness Functions: prüfen systemweite Eigenschaften (Latenz, Fehlerrate)

Beispiele:
  "Domain-Klassen dürfen keine Spring-Annotationen haben" → ArchUnit
  "Alle Endpunkte antworten in < 500ms P95"              → Gatling (→ ADR-044)
  "Kein Circular Dependency zwischen Modulen"            → ArchUnit
  "Test-Coverage ≥ 80%"                                  → JaCoCo + CI Gate
  "Kein CVE mit CVSS ≥ 7 in Dependencies"                → OWASP DC (→ ADR-057)
```

---

## ArchUnit: Strukturelle Fitness Functions

```java
@AnalyzeClasses(packages = "com.example")
class ArchitectureFitnessFunctions {

    // ── ADR-031: Hexagonale Architektur ───────────────────────────
    @ArchTest
    static final ArchRule domain_kennt_kein_framework =
        noClasses()
            .that().resideInAPackage("com.example.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.jackson.."
            )
            .because("ADR-031: Domain-Schicht ist framework-unabhängig");

    // ── ADR-006: Schichtenarchitektur ──────────────────────────────
    @ArchTest
    static final ArchRule controller_kennt_kein_repository =
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat()
            .haveSimpleNameEndingWith("Repository")
            .because("ADR-006: Controller → Service → Repository");

    @ArchTest
    static final ArchRule services_kennen_keine_controller =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..")
            .because("Keine Abhängigkeit von Service zu Controller");

    // ── ADR-007: Kein Optional als Feldtyp ────────────────────────
    @ArchTest
    static final ArchRule kein_optional_als_feldtyp =
        noFields()
            .should().haveRawType(java.util.Optional.class)
            .because("ADR-007: Optional ist kein Feldtyp");

    // ── ADR-008: Keine Utils-Klassen ──────────────────────────────
    @ArchTest
    static final ArchRule keine_utility_klassen =
        noClasses()
            .should().haveSimpleNameEndingWith("Utils")
            .orShould().haveSimpleNameEndingWith("Helper")
            .orShould().haveSimpleNameEndingWith("Util")
            .because("ADR-008: Utility-Klassen sind ein Anti-Pattern");

    // ── ADR-025: DIP — Konstruktorinjektion statt Feld-Injection ─
    @ArchTest
    static final ArchRule keine_feld_injektion =
        noFields()
            .that().areDeclaredInClassesThat()
            .areAnnotatedWith(org.springframework.stereotype.Service.class)
            .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
            .because("ADR-025: Konstruktorinjektion statt @Autowired auf Feldern");

    // ── ADR-004: Kein @Async in Services (Virtual Threads) ────────
    @ArchTest
    static final ArchRule kein_async_in_services =
        noMethods()
            .that().areDeclaredInClassesThat()
            .areAnnotatedWith(org.springframework.stereotype.Service.class)
            .should().beAnnotatedWith(org.springframework.scheduling.annotation.Async.class)
            .because("ADR-004: Virtual Threads ersetzen @Async");

    // ── Keine Zirkularabhängigkeiten ───────────────────────────────
    @ArchTest
    static final ArchRule keine_zirkulaeren_abhaengigkeiten =
        slices().matching("com.example.(*)..").should().beFreeOfCycles()
            .because("Zirkuläre Abhängigkeiten machen Code unwartbar");

    // ── ADR-050: Modul-Abhängigkeiten ────────────────────────────
    @ArchTest
    static final ArchRule domain_abhaengigkeitsregel =
        classes().that().resideInAPackage("com.example.domain..")
            .should().onlyHaveDependentClassesThat()
            .resideInAnyPackage(
                "com.example.domain..",
                "com.example.application.."  // Nur application darf domain kennen
            )
            .because("ADR-050: Domain hat keine eingehenden Abhängigkeiten von Adaptern");

    // ── Testklassen-Konventionen ───────────────────────────────────
    @ArchTest
    static final ArchRule tests_muessen_test_im_namen_haben =
        classes()
            .that().resideInAPackage("..test..")
            .and().areAnnotatedWith(org.junit.jupiter.api.Test.class)
            .should().haveSimpleNameEndingWith("Test")
            .because("ADR-010: Testnamen-Konvention");

    // ── Exceptions: keine RuntimeException direkt werfen ──────────
    @ArchTest
    static final ArchRule keine_generischen_exceptions =
        noClasses()
            .should().callConstructor(RuntimeException.class, String.class)
            .because("ADR-015: Sprechende Domain-Exceptions statt RuntimeException");
}
```

---

## Metrische Fitness Functions in der Pipeline

```yaml
# .github/workflows/fitness-functions.yml
name: Architecture Fitness Functions

on: [push, pull_request]

jobs:
  structural-fitness:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: ArchUnit Tests
        run: ./gradlew architectureTest
        # Schlägt fehl wenn Architektur-Regel verletzt

  coverage-fitness:
    runs-on: ubuntu-latest
    steps:
      - name: Test Coverage
        run: ./gradlew jacocoTestCoverageVerification
        # build.gradle.kts: minimum = "0.80".toBigDecimal()

  dependency-fitness:
    runs-on: ubuntu-latest
    steps:
      - name: CVE Check
        run: ./gradlew dependencyCheckAnalyze
        # Schlägt fehl bei CVSS >= 7

  performance-fitness:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Load Test (P95 < 500ms)
        run: ./gradlew gatlingRun
        # Gatling Simulation mit SLO-Assertions (→ ADR-044)
```

---

## Fitness Function Dashboard: Architektur-Gesundheit sichtbar machen

```java
// Eigene Fitness Function: API-Response-Zeit messen
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApiPerformanceFitnessFunction {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void orderDetail_respondsWithin500ms() {
        var response = restTemplate.getForEntity("/api/v1/orders/1", OrderDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Test schlägt fehl wenn Antwort > 500ms
    }
}

// Modulith-Fitness-Function (→ ADR-056)
@Test
void modulithStruktur_istIntakt() {
    ApplicationModules.of(ECommerceApplication.class)
        .verify(); // Schlägt fehl bei Verletzung von Modulgrenzen
}
```

---

## Fitness Function Katalog (dieser Codebase)

```markdown
| Fitness Function                      | Tool        | Gate   | ADR   |
|---------------------------------------|-------------|--------|-------|
| Domain kennt kein Framework           | ArchUnit    | CI     | 031   |
| Kein @Autowired auf Feldern           | ArchUnit    | CI     | 025   |
| Keine Utils-Klassen                   | ArchUnit    | CI     | 008   |
| Kein Optional als Feldtyp             | ArchUnit    | CI     | 007   |
| Keine Zirkularabhängigkeiten          | ArchUnit    | CI     | —     |
| Test Coverage ≥ 80%                   | JaCoCo      | CI     | 045   |
| Mutation Score ≥ 80%                  | PIT         | Nightly| 045   |
| Kein CVE CVSS ≥ 7                     | OWASP DC    | CI     | 057   |
| Lizenz-Compliance                     | License-Rep.| CI     | 057   |
| P95 Latenz < 500ms                    | Gatling     | Staging| 044   |
| Error Rate < 1%                       | Prometheus  | Prod   | 054   |
| Modulith-Grenzen intakt               | Modulith    | CI     | 056   |
| Container CVE-Scan                    | Trivy       | CI     | 057   |
| JavaDoc auf public APIs               | Checkstyle  | CI     | 048   |
```

---

## Konsequenzen

**Positiv:** Architekturentscheidungen werden kontinuierlich durchgesetzt — nicht nur beim initialen Review. Neue Entwickler können nicht versehentlich Architektur verletzen. Architektur-Drift wird sofort sichtbar.

**Negativ:** Zu viele Fitness Functions → CI-Laufzeit steigt. Zu strenge Regeln → Entwickler finden Workarounds. Fitness Functions müssen selbst gewartet werden wenn Architektur sich weiterentwickelt.

---

## 💡 Guru-Tipps

- **Fitness Functions inkrementell einführen**: Start mit einer Regel, dann erweitern.
- **Violations dokumentieren wenn akzeptiert**: `@ArchIgnore` mit Begründung und Issue-Link.
- **Separate `architectureTest` Task**: trennt Architektur-Tests von Unit-Tests — beide in CI, aber getrennt filterbar.
- **"Evolutionary Architecture"** (Ford, Parsons): Fitness Functions sind das Kern-Werkzeug für evolvierbare Architektur.

---

## Verwandte ADRs

- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ADRs + Fitness Functions = ADR durchgesetzt.
- [ADR-036](ADR-036-devops-cicd.md) — Fitness Functions in der CI-Pipeline.
- [ADR-044](ADR-044-load-performance-testing.md) — Performance als Fitness Function.
