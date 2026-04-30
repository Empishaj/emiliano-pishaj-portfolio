# ADR-036 — DevOps: CI/CD Pipeline

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · GitHub Actions / GitLab CI   |
| Datum      | 2024-01-01                        |
| Kategorie  | DevOps / CI/CD                    |

---

## Kontext & Problem

Code der nicht automatisch gebaut, getestet und deployed wird, ist nicht produktionsreif. Eine CI/CD-Pipeline ist kein Luxus — sie ist die minimale Qualitätssicherung. Jeder Push muss automatisch validiert werden. Jede Produktionsänderung muss nachvollziehbar und rollback-fähig sein.

---

## Die Pipeline-Phasen

```
Push → Lint/Format → Build → Unit Tests → Integration Tests
     → Code Quality → Security Scan → Docker Build → Deploy Staging
     → Smoke Tests → [Manual Gate] → Deploy Production → Health Check
```

---

## GitHub Actions Pipeline

```yaml
# .github/workflows/pipeline.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  JAVA_VERSION: '21'
  IMAGE_NAME: ghcr.io/${{ github.repository }}

jobs:
  # ── Phase 1: Build & Unit Tests ────────────────────────────────
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: gradle

      - name: Build
        run: ./gradlew build -x test

      - name: Unit Tests
        run: ./gradlew test
        # JUnit-Ergebnisse publizieren
      - uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Unit Test Results
          path: build/test-results/**/*.xml
          reporter: java-junit

      - name: Code Coverage
        run: ./gradlew jacocoTestReport jacocoTestCoverageVerification
        # Schlägt fehl wenn Coverage unter Schwellwert (build.gradle konfiguriert)

  # ── Phase 2: Code-Qualität ─────────────────────────────────────
  code-quality:
    runs-on: ubuntu-latest
    needs: build-and-test
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # SonarQube braucht volle Git-History

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: gradle

      - name: ArchUnit Tests
        run: ./gradlew archTest  # ADR-009: Architekturregeln automatisch prüfen

      - name: SonarQube Analysis
        run: ./gradlew sonar
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}

      - name: Checkstyle
        run: ./gradlew checkstyleMain checkstyleTest

  # ── Phase 3: Sicherheits-Scan ───────────────────────────────────
  security:
    runs-on: ubuntu-latest
    needs: build-and-test
    steps:
      - uses: actions/checkout@v4

      - name: OWASP Dependency Check
        run: ./gradlew dependencyCheckAnalyze
        # Schlägt fehl bei CVSS-Score ≥ 7 (konfigurierbar)

      - name: Upload Dependency Check Report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: dependency-check-report
          path: build/reports/dependency-check-report.html

  # ── Phase 4: Integrationstests ──────────────────────────────────
  integration-tests:
    runs-on: ubuntu-latest
    needs: [code-quality, security]
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: gradle

      - name: Integration Tests
        run: ./gradlew integrationTest
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
          SPRING_DATASOURCE_PASSWORD: test

  # ── Phase 5: Docker Build & Push ────────────────────────────────
  docker:
    runs-on: ubuntu-latest
    needs: integration-tests
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Docker Build & Push
        uses: docker/build-push-action@v5
        with:
          push: true
          tags: |
            ${{ env.IMAGE_NAME }}:${{ github.sha }}
            ${{ env.IMAGE_NAME }}:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max

  # ── Phase 6: Deploy ─────────────────────────────────────────────
  deploy-staging:
    runs-on: ubuntu-latest
    needs: docker
    environment: staging
    steps:
      - name: Deploy to Staging
        run: |
          kubectl set image deployment/app \
            app=${{ env.IMAGE_NAME }}:${{ github.sha }}
        env:
          KUBECONFIG: ${{ secrets.KUBECONFIG_STAGING }}

      - name: Smoke Tests
        run: ./scripts/smoke-test.sh https://staging.example.com

  deploy-production:
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment:
      name: production
      url: https://example.com
    # Manual Approval Gate
    steps:
      - name: Deploy to Production
        run: |
          kubectl set image deployment/app \
            app=${{ env.IMAGE_NAME }}:${{ github.sha }}
        env:
          KUBECONFIG: ${{ secrets.KUBECONFIG_PROD }}

      - name: Health Check
        run: |
          for i in {1..10}; do
            curl -f https://example.com/actuator/health && break
            sleep 10
          done
```

---

## Gradle Build-Konfiguration

```kotlin
// build.gradle.kts
plugins {
    id("org.springframework.boot") version "3.3.0"
    id("jacoco")
    id("org.sonarqube") version "5.0.0.4638"
    id("org.owasp.dependencycheck") version "9.0.0"
    id("checkstyle")
}

// Test-Aufgaben trennen
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
}

// Coverage-Schwellwert
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% Line Coverage Minimum
            }
        }
    }
}

// SonarQube Konfiguration
sonar {
    properties {
        property("sonar.projectKey", "com.example:my-project")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.qualitygate.wait", "true")
    }
}

// OWASP: Fehlschlag bei CVSS ≥ 7
dependencyCheck {
    failBuildOnCVSS = 7.0f
    formats = listOf("HTML", "JSON")
}
```

---

## Konsequenzen

**Positiv:** Kein manueller Deploy, keine manuelle Qualitätsprüfung. Jeder PR ist vollständig validiert bevor er merged wird. Git-SHA als Image-Tag macht jedes Deployment zu einem nachvollziehbaren Commit.

**Negativ:** Pipeline muss gepflegt werden. Integration-Tests verlängern die Pipeline-Zeit. Quality Gates müssen anfangs kalibriert werden.

---

## Tipps

- **Fail Fast**: Schnelle Tests (Unit, Lint) zuerst — teure Tests (Integration, Security) nur wenn schnelle bestanden.
- **Pipeline as Code**: `pipeline.yml` im Repository — versioniert, reviewbar, reproduzierbar.
- **Branch Protection**: `main` darf nur via PR mit bestandener Pipeline merged werden. Kein direkter Push auf `main`.
- **Secrets nie in Pipeline-Dateien**: `${{ secrets.NAME }}` aus GitHub Secrets / GitLab CI Variables.
 