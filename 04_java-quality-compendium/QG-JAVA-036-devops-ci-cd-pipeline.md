# QG-JAVA-036 — DevOps: CI/CD Pipeline für Java-Services

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-036 |
| Titel | DevOps: CI/CD Pipeline für Java-Services |
| Status | Accepted / verbindlicher Standard für Build-, Test-, Security- und Deployment-Pipelines |
| Version | 1.0.0 |
| Datum | 2025-03-27 |
| Kategorie | DevOps / CI/CD / Software Supply Chain / Delivery Governance |
| Zielgruppe | Java-Entwickler, Tech Leads, DevOps, SRE, Security, QA, Plattform-Team, Engineering Management |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Gradle oder Maven, Docker/OCI Images |
| Plattform-Kontext | GitHub Actions, GitLab CI/CD oder vergleichbare Pipeline-Systeme |
| Geltungsbereich | Build, Test, Code Quality, Security Scans, Container Build, SBOM, Provenance, Deployment, Smoke Tests, Rollback und Release Gates |
| Verbindlichkeit | Jede produktive Änderung muss automatisiert gebaut, getestet, geprüft, versioniert und nachvollziehbar deploybar sein. Direkte manuelle Produktionsänderungen außerhalb definierter Release-/Hotfix-Prozesse sind nicht zulässig. |
| Technische Validierung | Gegen GitHub Actions, GitLab CI/CD, Docker Buildx/BuildKit, OWASP Dependency-Check, GitHub Artifact Attestations und moderne Supply-Chain-Security-Praktiken eingeordnet |
| Kurzentscheidung | CI/CD ist ein verbindlicher Qualitäts- und Sicherheitsmechanismus. Jeder Pull Request wird automatisch validiert. Jedes Deployment ist einem Commit, einem Build, einem Artefakt, einem Image Digest und einer Pipeline-Ausführung zuordenbar. |

---

## 1. Zweck

Diese Richtlinie definiert, wie CI/CD-Pipelines für Java-21- und Spring-Boot-Services aufgebaut werden.

Eine Pipeline ist nicht nur ein Skript, das `gradlew build` ausführt. Eine professionelle Pipeline ist ein technischer Kontrollpunkt für Qualität, Sicherheit, Reproduzierbarkeit und Betriebsfähigkeit. Sie beantwortet vor jedem Merge und vor jedem Deployment die Fragen:

- Kompiliert der Code?
- Sind Unit-Tests grün?
- Sind Integrationstests grün?
- Werden Architekturregeln eingehalten?
- Sind bekannte verwundbare Abhängigkeiten erkannt?
- Wurden Secrets versehentlich eingecheckt?
- Ist das Container-Image baubar?
- Ist das Image klein, sicher und non-root?
- Gibt es SBOM und Build-Provenance?
- Ist das Artefakt eindeutig einem Commit zuordenbar?
- Kann auf Staging deployed werden?
- Funktioniert der Service nach Deployment?
- Kann im Fehlerfall zurückgerollt werden?

Ohne Pipeline ist Qualität abhängig von manueller Disziplin. Mit Pipeline wird Qualität zumindest teilweise reproduzierbar, prüfbar und teamübergreifend erzwingbar.

---

## 2. Kurzregel

Jeder Pull Request durchläuft automatisierte Checks für Build, Unit-Tests, Codequalität, Architekturregeln, Security und relevante Integrationstests. Jeder Merge in den Hauptbranch erzeugt ein unveränderliches Artefakt mit Git-SHA, SBOM und möglichst Provenance. Deployments verwenden unveränderliche Image-Tags oder Digests, niemals `latest` als produktive Referenz. Staging wird automatisch deployed; Produktion erhält je nach Risiko ein definiertes Approval Gate, Health Check, Smoke Test und Rollback-Mechanismus.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Java-Services,
- Spring-Boot-Anwendungen,
- Worker,
- Batch-Anwendungen,
- REST APIs,
- Kafka Consumer/Producer Services,
- Container Images,
- Helm Charts,
- Kubernetes Manifeste,
- GitHub Actions Workflows,
- GitLab CI/CD Pipelines,
- Gradle-/Maven-Builds,
- Testcontainers-Integrationstests,
- Security- und Dependency-Scans,
- Container-Scans,
- Deployments nach Staging und Produktion.

Sie gilt nicht für einmalige lokale Skripte oder experimentelle Spikes, solange diese nicht produktiv betrieben oder in produktiven Repositories dauerhaft genutzt werden.

---

## 4. Grundprinzipien

| Prinzip | Details/Erklärung | Konsequenz |
|---|---|---|
| Pipeline as Code | Pipeline liegt versioniert im Repository. | Änderungen an Pipeline werden reviewed. |
| Fail Fast | Schnelle Checks laufen zuerst. | Entwickler erhalten frühes Feedback. |
| Reproducible Build | Ein Artefakt ist aus Commit und Pipeline nachvollziehbar. | Debugging und Audit werden möglich. |
| Immutable Artifact | Build-Artefakt wird nicht nachträglich verändert. | Promotion statt Neubuild pro Umgebung. |
| Least Privilege | Jobs erhalten nur nötige Berechtigungen. | Geringere Angriffswirkung bei kompromittierten Jobs. |
| Secrets geschützt | Secrets liegen nicht im Repository. | Zugriff über Secrets/OIDC/Vault. |
| Quality Gates | Merge und Deployment hängen an definierten Gates. | Qualität wird nicht freiwillig. |
| Observability nach Deploy | Deployment endet nicht mit `kubectl apply`. | Health, Smoke, Logs, Metriken prüfen. |
| Rollback-fähig | Jede Produktionseinspielung ist zurücknehmbar. | Risiko wird kontrollierbar. |
| Traceability | Commit → Build → Image → Deploy → Runtime ist verfolgbar. | Incident-Analyse wird möglich. |

---

## 5. Zielpipeline

Empfohlener Ablauf:

```text
Pull Request
  ├─ Lint / Format / Markdown
  ├─ Compile
  ├─ Unit Tests
  ├─ Architecture Tests
  ├─ Code Quality
  ├─ Dependency Scan
  ├─ Secret Scan
  ├─ Integration Tests
  ├─ Contract Tests
  └─ PR Quality Gate

Main Branch
  ├─ Build Artifact
  ├─ Build Container Image
  ├─ Generate SBOM
  ├─ Generate Provenance / Attestation
  ├─ Container Scan
  ├─ Push Image by Git SHA
  ├─ Deploy Staging
  ├─ Smoke Tests
  ├─ Optional Manual Approval
  ├─ Deploy Production
  ├─ Health Check
  └─ Deployment Verification
```

Nicht jede Pipeline muss alle Schritte immer ausführen. Aber jedes relevante Risiko muss abgedeckt werden. Ein Service ohne Kafka braucht keinen Kafka-Contract-Test. Ein Service mit Docker-Image braucht Container-Scan und Image-Traceability.

---

## 6. Was CI und CD bedeuten

### 6.1 Continuous Integration

CI bedeutet: Änderungen werden regelmäßig integriert und automatisch validiert. Für Pull Requests heißt das:

- Build läuft automatisch,
- Tests laufen automatisch,
- statische Prüfungen laufen automatisch,
- Security Checks laufen automatisch,
- Architekturregeln laufen automatisch,
- Status Checks blockieren Merge bei Fehlern.

CI ist nicht optional. Ein PR mit roter CI wird nicht gemerged.

### 6.2 Continuous Delivery

Continuous Delivery bedeutet: Der Code ist nach erfolgreicher Pipeline jederzeit deploybar. Ein manuelles Freigabegate kann existieren, aber das Artefakt ist vorbereitet, getestet und releasefähig.

### 6.3 Continuous Deployment

Continuous Deployment bedeutet: Jede erfolgreiche Änderung wird automatisch bis Produktion deployed. Das ist nicht für jedes regulatorische oder organisatorische Umfeld erforderlich. Entscheidend ist, dass der Prozess bewusst gewählt wird und nicht durch manuelle Unsicherheit entsteht.

---

## 7. Branch-Strategie

Empfohlener Standard:

```text
main
 ├─ immer deploybar
 ├─ geschützt
 ├─ kein direkter Push
 └─ Merge nur über PR mit grünen Checks

feature/*
 ├─ kurzlebig
 ├─ PR gegen main
 └─ regelmäßig rebasen oder updaten

release/*
 ├─ nur wenn Release-Branches organisatorisch nötig sind
 └─ klare Patch-Regeln

hotfix/*
 ├─ Incident-bezogen
 ├─ kleiner Scope
 └─ nachträgliche Tests/Review falls beschleunigt
```

Lang laufende Feature Branches sind zu vermeiden, weil sie Integration verzögern und Merge-Risiko erhöhen. Feature Flags sind für unfertige Funktionalität in `main` der bevorzugte Mechanismus.

---

## 8. GitHub Actions Referenzpipeline

### 8.1 Workflow-Grundgerüst

```yaml
name: ci-cd

on:
  pull_request:
    branches: [ main ]
  push:
    branches: [ main ]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

env:
  JAVA_VERSION: "21"
  REGISTRY: ghcr.io
  IMAGE_NAME: ghcr.io/${{ github.repository }}

permissions:
  contents: read

jobs:
  build-test:
    name: Build and unit tests
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ env.JAVA_VERSION }}
          cache: gradle

      - name: Validate Gradle Wrapper
        uses: gradle/actions/wrapper-validation@v4

      - name: Compile
        run: ./gradlew compileJava compileTestJava --no-daemon

      - name: Unit tests
        run: ./gradlew test --no-daemon

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-reports
          path: build/reports/tests/test/
```

Wichtig:

- `permissions` sind minimal.
- `concurrency` verhindert unnötige parallele Runs auf denselben Branches.
- Wrapper Validation schützt gegen manipulierte Gradle Wrapper.
- Java-Version ist zentral.
- Cache wird über `setup-java` aktiviert.
- Testberichte werden auch bei Fehlern hochgeladen.

### 8.2 Codequalität und Architekturregeln

```yaml
  quality:
    name: Code quality
    runs-on: ubuntu-latest
    needs: build-test
    timeout-minutes: 15

    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ env.JAVA_VERSION }}
          cache: gradle

      - name: Checkstyle
        run: ./gradlew checkstyleMain checkstyleTest --no-daemon

      - name: SpotBugs
        run: ./gradlew spotbugsMain spotbugsTest --no-daemon

      - name: ArchUnit
        run: ./gradlew archTest --no-daemon

      - name: JaCoCo coverage
        run: ./gradlew jacocoTestReport jacocoTestCoverageVerification --no-daemon
```

Regel: Architekturtests sind kein Nice-to-have, wenn Architekturentscheidungen im Projekt verbindlich sind.

### 8.3 Security Checks

```yaml
  security:
    name: Security checks
    runs-on: ubuntu-latest
    needs: build-test
    timeout-minutes: 20

    permissions:
      contents: read
      security-events: write

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ env.JAVA_VERSION }}
          cache: gradle

      - name: OWASP Dependency-Check
        run: ./gradlew dependencyCheckAnalyze --no-daemon

      - name: Upload dependency-check report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: dependency-check-report
          path: build/reports/dependency-check-report.html
```

OWASP Dependency-Check identifiziert bekannte verwundbare Komponenten über Projektabhängigkeiten. Wichtig: Der Build muss so konfiguriert sein, dass relevante CVSS-Schwellen tatsächlich fehlschlagen. Die Standardeinstellung `failBuildOnCVSS = 11` führt faktisch nicht zum Fehlschlag, weil CVSS-Scores von 0 bis 10 reichen.

### 8.4 Integrationstests mit Testcontainers

```yaml
  integration-test:
    name: Integration tests
    runs-on: ubuntu-latest
    needs: [ quality, security ]
    timeout-minutes: 25

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ env.JAVA_VERSION }}
          cache: gradle

      - name: Integration tests
        run: ./gradlew integrationTest --no-daemon
```

Für produktionsnahe Integrationstests wird bevorzugt Testcontainers verwendet. GitHub Actions Runner stellen Docker grundsätzlich bereit; dennoch müssen Container-Laufzeiten und Image Pulls beobachtet werden.

Nicht bevorzugt:

```yaml
services:
  postgres:
    image: postgres:latest
```

Besser ist Testcontainers im Testcode, weil Tests ihre Abhängigkeiten selbst deklarieren und lokal wie in CI gleich laufen. GitHub-Service-Container können sinnvoll sein, wenn ein Team diesen Ansatz bewusst standardisiert.

### 8.5 Docker Build, SBOM und Provenance

```yaml
  container:
    name: Build container image
    runs-on: ubuntu-latest
    needs: integration-test
    if: github.ref == 'refs/heads/main'

    permissions:
      contents: read
      packages: write
      id-token: write
      attestations: write

    outputs:
      image-digest: ${{ steps.build.outputs.digest }}

    steps:
      - uses: actions/checkout@v4

      - name: Login to registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Setup Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Build and push image
        id: build
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: |
            ${{ env.IMAGE_NAME }}:${{ github.sha }}
          labels: |
            org.opencontainers.image.source=${{ github.server_url }}/${{ github.repository }}
            org.opencontainers.image.revision=${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
          sbom: true
          provenance: true

      - name: Generate artifact attestation
        uses: actions/attest-build-provenance@v2
        with:
          subject-name: ${{ env.IMAGE_NAME }}
          subject-digest: ${{ steps.build.outputs.digest }}
          push-to-registry: true
```

Regeln:

- Produktive Images werden mit Git-SHA getaggt.
- `latest` darf nicht als produktive Referenz verwendet werden.
- Image Digest wird gespeichert.
- SBOM und Provenance werden erzeugt, wenn Plattform und Tooling es unterstützen.
- GitHub Artifact Attestations erzeugen Build-Provenance für Artefakte wie Container Images.
- Artifact Attestations sind kein Beweis, dass ein Artefakt sicher ist; sie verbinden Artefakt, Source und Build-Informationen. Sicherheitsbewertung bleibt zusätzlich notwendig.

### 8.6 Container Scan

```yaml
  container-scan:
    name: Container vulnerability scan
    runs-on: ubuntu-latest
    needs: container
    if: github.ref == 'refs/heads/main'

    steps:
      - name: Scan image
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.IMAGE_NAME }}:${{ github.sha }}
          format: table
          exit-code: "1"
          severity: CRITICAL,HIGH
```

Für Organisationen mit strenger Reproduzierbarkeit sind Actions auf Commit-SHA zu pinnen statt nur auf Major Tags. Das reduziert Supply-Chain-Risiken durch veränderte Action-Versionen.

---

## 9. Deployment nach Staging und Produktion

### 9.1 Staging Deployment

```yaml
  deploy-staging:
    name: Deploy staging
    runs-on: ubuntu-latest
    needs: [ container, container-scan ]
    environment: staging
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Deploy image digest to staging
        run: |
          ./scripts/deploy.sh \
            --environment staging \
            --image "${IMAGE_NAME}@${IMAGE_DIGEST}"
        env:
          IMAGE_NAME: ${{ env.IMAGE_NAME }}
          IMAGE_DIGEST: ${{ needs.container.outputs.image-digest }}

      - name: Smoke test staging
        run: ./scripts/smoke-test.sh https://staging.example.com
```

### 9.2 Production Deployment mit Environment Approval

```yaml
  deploy-production:
    name: Deploy production
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment:
      name: production
      url: https://example.com
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Deploy image digest to production
        run: |
          ./scripts/deploy.sh \
            --environment production \
            --image "${IMAGE_NAME}@${IMAGE_DIGEST}"
        env:
          IMAGE_NAME: ${{ env.IMAGE_NAME }}
          IMAGE_DIGEST: ${{ needs.container.outputs.image-digest }}

      - name: Health check
        run: ./scripts/health-check.sh https://example.com/actuator/health

      - name: Smoke test production
        run: ./scripts/smoke-test.sh https://example.com
```

GitHub Environments können Deployment Gates wie Required Reviewers, Wartezeiten und umgebungsspezifische Secrets nutzen. Produktionsfreigaben sollen nicht in selbst gebauten Shell-Prompts versteckt werden, sondern über Plattformmechanismen nachvollziehbar sein.

---

## 10. GitLab CI/CD Referenzstruktur

GitLab CI/CD nutzt `.gitlab-ci.yml`, Stages, Jobs, `needs`, Rules, Protected Branches und CI/CD Variables.

Beispiel:

```yaml
stages:
  - validate
  - test
  - security
  - package
  - deploy

variables:
  JAVA_VERSION: "21"
  IMAGE_NAME: "$CI_REGISTRY_IMAGE"

default:
  image: eclipse-temurin:21-jdk
  cache:
    key: "$CI_COMMIT_REF_SLUG"
    paths:
      - .gradle/caches/
      - .gradle/wrapper/

build:
  stage: validate
  script:
    - ./gradlew compileJava compileTestJava --no-daemon

unit-test:
  stage: test
  script:
    - ./gradlew test --no-daemon
  artifacts:
    when: always
    reports:
      junit: build/test-results/test/*.xml
    paths:
      - build/reports/tests/test/

quality:
  stage: test
  script:
    - ./gradlew checkstyleMain spotbugsMain archTest jacocoTestCoverageVerification --no-daemon

dependency-check:
  stage: security
  script:
    - ./gradlew dependencyCheckAnalyze --no-daemon
  artifacts:
    when: always
    paths:
      - build/reports/dependency-check-report.html

integration-test:
  stage: test
  script:
    - ./gradlew integrationTest --no-daemon

container:
  stage: package
  image: docker:27
  services:
    - docker:27-dind
  variables:
    DOCKER_TLS_CERTDIR: "/certs"
  script:
    - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
    - docker build -t "$IMAGE_NAME:$CI_COMMIT_SHA" .
    - docker push "$IMAGE_NAME:$CI_COMMIT_SHA"
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'

deploy-staging:
  stage: deploy
  script:
    - ./scripts/deploy.sh --environment staging --image "$IMAGE_NAME:$CI_COMMIT_SHA"
    - ./scripts/smoke-test.sh https://staging.example.com
  environment:
    name: staging
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'

deploy-production:
  stage: deploy
  script:
    - ./scripts/deploy.sh --environment production --image "$IMAGE_NAME:$CI_COMMIT_SHA"
    - ./scripts/health-check.sh https://example.com/actuator/health
  environment:
    name: production
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
      when: manual
```

GitLab stellt vordefinierte CI/CD-Variablen wie `CI_COMMIT_SHA`, `CI_REGISTRY_IMAGE` und viele weitere bereit. Geschützte Variablen können auf geschützte Branches oder Tags begrenzt werden, damit Deployment-Credentials nicht in beliebigen Pipelines verfügbar sind.

---

## 11. Gradle-Konfiguration

### 11.1 Test Source Sets

```kotlin
plugins {
    java
    jacoco
    checkstyle
    id("com.github.spotbugs") version "6.0.26"
    id("org.owasp.dependencycheck") version "12.2.2"
    id("org.sonarqube") version "6.0.1.5171"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

configurations {
    named("integrationTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    named("integrationTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform {
        includeTags("integration")
    }
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}
```

### 11.2 JaCoCo

```kotlin
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

Coverage-Schwellen sind Qualitätsindikatoren, aber keine alleinige Aussage über Testqualität. Kritische Geschäftslogik kann 80% Coverage haben und trotzdem schlecht getestet sein. Deshalb werden Coverage Gates mit Review, Mutation Testing oder gezielten Randfalltests ergänzt, wenn nötig.

### 11.3 OWASP Dependency-Check

```kotlin
dependencyCheck {
    failBuildOnCVSS = 7.0f
    formats = listOf("HTML", "JSON", "JUNIT")
    suppressionFile = "config/dependency-check/suppressions.xml"
}
```

Suppressions müssen begründet, datiert und regelmäßig überprüft werden. Eine Suppression ohne Ablaufdatum ist technische Schuld.

---

## 12. Maven-Alternative

Für Maven-Projekte gelten dieselben Phasen.

Beispiel:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.owasp</groupId>
            <artifactId>dependency-check-maven</artifactId>
            <version>12.1.8</version>
            <configuration>
                <failBuildOnCVSS>7</failBuildOnCVSS>
                <formats>
                    <format>HTML</format>
                    <format>JSON</format>
                </formats>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.12</version>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.5.2</version>
        </plugin>
    </plugins>
</build>
```

Gradle oder Maven ist eine Teamentscheidung. Die Pipeline muss beide sauber unterstützen, wenn beide im Unternehmen existieren.

---

## 13. Security- und Supply-Chain-Regeln

### 13.1 Least Privilege für Pipeline-Berechtigungen

GitHub Actions:

```yaml
permissions:
  contents: read
```

Nur Jobs, die Packages schreiben, bekommen:

```yaml
permissions:
  contents: read
  packages: write
  id-token: write
  attestations: write
```

Regel: Keine globalen Schreibrechte für alle Jobs.

### 13.2 OIDC statt Long-Lived Cloud Secrets

Bevorzugt:

```text
GitHub/GitLab OIDC → Cloud Provider Role → temporäre Credentials
```

Nicht bevorzugt:

```text
statischer AWS_ACCESS_KEY_ID + AWS_SECRET_ACCESS_KEY in Repository Secrets
```

Wenn statische Secrets unvermeidbar sind, müssen sie rotiert, begrenzt, geschützt und überwacht werden.

### 13.3 Keine Secrets im Workflow

Nicht erlaubt:

```yaml
env:
  DB_PASSWORD: super-secret-password
```

Erlaubt:

```yaml
env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
```

Noch besser für Cloud Deployments: OIDC und rollenbasierte temporäre Credentials.

### 13.4 Pinning von Actions

Für normale Projekte ist Major-Version-Pinning oft pragmatisch:

```yaml
uses: actions/checkout@v4
```

Für hohe Supply-Chain-Anforderungen ist SHA-Pinning zu bevorzugen:

```yaml
uses: actions/checkout@<commit-sha>
```

Die Organisation muss entscheiden, welcher Standard gilt.

### 13.5 SBOM

Ein SBOM beschreibt, welche Komponenten im Artefakt enthalten sind. Es ist kein Security-Scan, sondern eine Inventarliste. Es hilft bei CVE-Bewertung, Incident Response und Nachweispflichten.

### 13.6 Provenance / Attestation

Build Provenance beschreibt, wie und wo ein Artefakt gebaut wurde. GitHub Artifact Attestations können kryptographisch signierte Aussagen über Build-Herkunft erzeugen. Attestations beweisen nicht automatisch Sicherheit, aber sie verbessern Nachvollziehbarkeit und Integrität der Lieferkette.

---

## 14. Docker- und Image-Regeln

Pflichtregeln:

1. Multi-Stage Build.
2. Non-root Runtime User.
3. Kleines Runtime Image.
4. Kein JDK im Runtime Image, wenn JRE reicht.
5. Keine Secrets im Image.
6. Kein `latest` als Produktionsreferenz.
7. Image Tag mit Git-SHA.
8. Deployment bevorzugt über Digest.
9. Container Scan vor Produktion.
10. SBOM/Provenance erzeugen, wenn möglich.
11. OCI Labels setzen.

Beispiel Tags:

```text
ghcr.io/org/service:4f2c9f8a0e5b...
ghcr.io/org/service@sha256:...
```

Nicht:

```text
ghcr.io/org/service:latest
```

`latest` kann zusätzlich für lokale Entwicklung oder Demo-Umgebungen erzeugt werden, darf aber nicht als produktive Deployment-Quelle dienen.

---

## 15. Deployment-Strategien

| Strategie | Details/Erklärung | Einsatz |
|---|---|---|
| Rolling Deployment | Instanzen werden schrittweise ersetzt. | Standard für stateless Services |
| Blue/Green | Neue Umgebung parallel, Umschalten bei Erfolg. | Höhere Sicherheit bei kritischen Releases |
| Canary | kleiner Traffic-Anteil zuerst. | Risikoarme schrittweise Aktivierung |
| Feature Flag | Deploy von Release entkoppeln. | Unfertige oder riskante Features |
| Manual Gate | Menschliche Freigabe vor Produktion. | Regulierte oder kritische Systeme |
| Auto Production Deploy | Vollautomatisch bis Produktion. | Reife Teams mit guten Gates |

Deployment-Strategie muss zur Kritikalität passen. Ein manuelles Gate ersetzt keine Tests. Automatisierung ersetzt keine Risikobewertung.

---

## 16. Smoke Tests und Health Checks

### 16.1 Health Check

```bash
#!/usr/bin/env bash
set -euo pipefail

url="${1:?Health URL required}"

for attempt in {1..30}; do
  if curl -fsS "$url" > /tmp/health.json; then
    echo "Health check passed"
    exit 0
  fi

  echo "Health check failed, attempt $attempt"
  sleep 5
done

echo "Health check failed after retries"
exit 1
```

### 16.2 Smoke Test

Smoke Tests prüfen minimalen fachlichen Betrieb:

- Service antwortet.
- Auth funktioniert.
- kritischer Read-Endpunkt funktioniert.
- kritischer Write-Endpunkt funktioniert nur in Test-/Staging-Kontext.
- Datenbankverbindung funktioniert.
- externe Kernabhängigkeit ist erreichbar oder Fallback sichtbar.

Smoke Tests dürfen nicht zur vollständigen E2E-Suite werden. Sie sollen schnell zeigen, ob Deployment grundsätzlich funktioniert.

---

## 17. Rollback

Jedes Deployment braucht Rollback-Strategie.

Möglichkeiten:

- vorheriges Image Digest redeployen,
- Helm Rollback,
- Kubernetes Rollout Undo,
- Blue/Green Rückschaltung,
- Feature Flag deaktivieren,
- Traffic auf alte Version zurückschalten,
- Datenbankmigration rückwärtskompatibel gestalten.

Regel: Rollback darf nicht erst im Incident erfunden werden.

Beispiel:

```bash
kubectl rollout undo deployment/app -n production
kubectl rollout status deployment/app -n production
```

Bei Datenbankmigrationen ist Rollback schwieriger. Deshalb müssen Migrationen vorwärts- und rückwärtskompatibel geplant werden.

---

## 18. Datenbankmigrationen in CI/CD

Regeln:

1. Migrationen werden im Build geprüft.
2. Migrationen laufen gegen Testcontainers-Datenbank.
3. Migrationen sind idempotent, soweit möglich.
4. Breaking Migrationen werden in Expand/Contract-Schritten umgesetzt.
5. Anwendungsversionen müssen während Rolling Deployment kompatibel bleiben.
6. Große Datenänderungen laufen nicht unkontrolliert im Startup.
7. Rollback- oder Forward-Fix-Strategie ist dokumentiert.

Expand/Contract:

```text
1. Neue nullable Spalte hinzufügen.
2. Anwendung schreibt alte und neue Struktur.
3. Backfill durchführen.
4. Anwendung liest neue Struktur.
5. Alte Spalte entfernen, wenn sicher.
```

---

## 19. Release-Traceability

Jedes Deployment muss beantworten können:

| Frage | Erwartete Antwort |
|---|---|
| Welcher Commit läuft? | Git-SHA |
| Welches Image läuft? | Image Digest |
| Welche Pipeline hat gebaut? | Workflow Run ID |
| Wer hat freigegeben? | Environment Approval / Audit Log |
| Welche Tests liefen? | Testberichte |
| Welche Dependencies enthalten? | SBOM |
| Wie wurde gebaut? | Provenance / Attestation |
| Welche Konfiguration? | Environment/Helm values |
| Wann deployed? | Deployment Timestamp |
| Wie rollbacken? | vorheriges Digest / Helm Revision |

Diese Informationen müssen im Incident schnell verfügbar sein.

---

## 20. Observability in der Pipeline

Eine Pipeline endet nicht beim Deployment. Nach Deployment müssen Signale beobachtet werden:

- HTTP 5xx Rate,
- Latenz,
- Error Logs,
- JVM Memory,
- CPU,
- Restart Count,
- Readiness/Liveness,
- Kafka Consumer Lag,
- Datenbankverbindungen,
- SLO/Error Budget,
- Business Smoke Signal.

Optional kann die Pipeline nach Deployment automatisiert prüfen:

```bash
./scripts/check-metrics.sh \
  --service order-service \
  --window 5m \
  --max-error-rate 1
```

Wenn Metriken nach Deployment rot werden, muss die Pipeline oder der Release-Prozess ein Rollback oder Stop-the-line-Signal auslösen.

---

## 21. Performance der Pipeline

Langsame Pipelines werden umgangen. Deshalb muss Geschwindigkeit aktiv gestaltet werden.

Maßnahmen:

- Unit-Tests zuerst.
- Integrationstests nur nach erfolgreichen schnellen Checks.
- Build Cache nutzen.
- Dependency Cache nutzen.
- Docker Layer Cache nutzen.
- Parallelisierung verwenden.
- Jobs mit `needs` gezielt verbinden.
- Große Tests taggen.
- Flaky Tests reparieren, nicht ignorieren.
- Testcontainers-Images vorwärmen, wenn nötig.
- Nicht bei jedem PR unnötig Container-Images pushen.

Zielwerte sind teamspezifisch. Als Richtwert sollte ein normaler PR innerhalb weniger Minuten erstes Feedback geben.

---

## 22. Anti-Patterns

### 22.1 Nur lokal testen

```text
"Bei mir läuft es."
```

Nicht akzeptabel. CI ist die Referenz.

### 22.2 `latest` in Produktion

```yaml
image: ghcr.io/org/service:latest
```

Nicht nachvollziehbar und nicht reproduzierbar.

### 22.3 Secrets im Workflow

```yaml
password: my-prod-password
```

Kritischer Verstoß.

### 22.4 Build einmal pro Umgebung neu

```text
Build für Staging, anderer Build für Produktion.
```

Problem: Produktion erhält nicht exakt das getestete Artefakt.

Besser: einmal bauen, Artefakt promoten.

### 22.5 Security Scan nur nachts

Security muss PRs blockieren können, wenn kritische Risiken eingeführt werden. Nightly Scans ergänzen PR-Scans, ersetzen sie aber nicht.

### 22.6 Manuelle Deployments ohne Audit

```bash
ssh prod
java -jar app.jar
```

Nicht zulässig für produktive Services.

### 22.7 Rote Tests ignorieren

Flaky Tests sind Produktivitäts- und Vertrauensproblem. Sie werden repariert oder isoliert, nicht dauerhaft ignoriert.

### 22.8 Zu viele manuelle Gates

Wenn jedes Deployment mehrere manuelle Freigaben braucht, ist die Pipeline keine Delivery Pipeline, sondern ein teilautomatisierter Antrag.

### 22.9 CI mit zu breiten Berechtigungen

Jeder Job mit Schreibrechten erhöht Risiko.

### 22.10 Keine Rollback-Strategie

Ein Deployment ohne Rollback ist ein Produktionsrisiko.

---

## 23. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Pipeline as Code | Liegt Pipeline im Repository? | `.github/workflows` / `.gitlab-ci.yml` | Pflicht |
| Branch Protection | Merge nur mit grüner Pipeline? | Required checks | Pflicht |
| Java-Version | Ist Java 21 eindeutig gesetzt? | toolchain/setup-java | Pflicht |
| Build | Kompiliert reproduzierbar? | Gradle/Maven Wrapper | Pflicht |
| Tests | Unit- und Integrationstests getrennt? | `test` / `integrationTest` | Pflicht |
| Architekturregeln | ArchUnit oder vergleichbar? | `archTest` | Pflicht bei strukturellen Regeln |
| Security | Dependency-/Secret-/Container-Scan? | OWASP, Trivy | Pflicht |
| Secrets | Keine Secrets im Repo oder YAML? | `${{ secrets.X }}` / OIDC | Pflicht |
| Image | Git-SHA-Tag und Digest? | `service:${{ github.sha }}` | Pflicht |
| Kein `latest` prod | Produktion nutzt kein `latest`? | Digest Deployment | Pflicht |
| SBOM | SBOM erzeugt? | Buildx `sbom: true` | Soll |
| Provenance | Attestation erzeugt? | GitHub Attestations | Soll |
| Staging | Automatisches Staging Deployment? | nach main | Soll |
| Production Gate | Freigabe passend zur Kritikalität? | environment approval | Pflicht bei Prod |
| Health Check | Nach Deploy geprüft? | `/actuator/health` | Pflicht |
| Rollback | Rollback dokumentiert und getestet? | Helm/kubectl/flag | Pflicht |
| Observability | Deploy-Signale sichtbar? | Logs/Metriken/Trace | Pflicht |

---

## 24. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- main ist protected.
- required status checks sind aktiv.
- direkte Pushes auf main sind verboten.
- Workflow-Dateien werden von CODEOWNERS reviewed.
- Dockerfiles werden gescannt.
- Kein Deployment manifest referenziert :latest.
- Gradle Wrapper wird validiert.
- Dependency-Check schlägt bei CVSS >= 7 fehl.
- Secret Scan läuft auf PRs.
- Container Scan schlägt bei HIGH/CRITICAL fehl.
- Image bekommt OCI Labels.
- Production Deployments benötigen Environment Approval.
- SBOM/Attestation wird bei main-Build erzeugt.
```

Beispiel einfacher Check gegen `latest`:

```bash
#!/usr/bin/env bash
set -euo pipefail

if grep -R "image: .*:latest" k8s helm charts .github .gitlab-ci.yml; then
  echo "Production manifests must not use :latest"
  exit 1
fi
```

---

## 25. Migration bestehender Pipelines

Migration erfolgt schrittweise:

1. Ist-Zustand der Pipeline dokumentieren.
2. Branch Protection aktivieren.
3. Build und Unit-Tests als Pflichtcheck setzen.
4. Integrationstests ergänzen.
5. Security-Scan ergänzen.
6. Container Build standardisieren.
7. Image Tagging auf Git-SHA umstellen.
8. `latest` aus produktiven Manifests entfernen.
9. Container-Scan ergänzen.
10. SBOM/Provenance ergänzen.
11. Staging Deployment automatisieren.
12. Smoke Tests ergänzen.
13. Production Gate definieren.
14. Rollback dokumentieren.
15. Pipeline-Laufzeit optimieren.

Nicht alles muss in einem PR passieren. Pipeline-Härtung sollte in kleinen, überprüfbaren Schritten erfolgen.

---

## 26. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Incident-Hotfix beschleunigt deployed werden muss,
- ein Legacy-Service noch nicht containerisiert ist,
- eine externe Plattform bestimmte Deployment-Schritte erzwingt,
- ein Security-Scan temporär wegen falscher Positives blockiert,
- ein Test wegen Infrastrukturproblem vorübergehend deaktiviert werden muss.

Ausnahmen benötigen:

1. Begründung,
2. verantwortliche Person,
3. Ablaufdatum,
4. Nacharbeitsticket,
5. Risikoakzeptanz,
6. soweit möglich Ersatzkontrolle.

Eine Ausnahme ohne Ablaufdatum wird technische Schuld.

---

## 27. Definition of Done

Eine CI/CD-Pipeline erfüllt diese Richtlinie, wenn:

1. Pipeline as Code im Repository liegt,
2. Pull Requests automatisch validiert werden,
3. Hauptbranches geschützt sind,
4. Build, Unit-Tests und relevante Integrationstests Pflichtchecks sind,
5. Codequalität und Architekturregeln automatisiert geprüft werden,
6. Dependency-, Secret- und Container-Scans laufen,
7. Java 21 als Toolchain gesetzt ist,
8. Artefakte unveränderlich und einem Git-SHA zugeordnet sind,
9. Container Images mit Git-SHA und Digest deployt werden,
10. Produktion nicht `latest` verwendet,
11. SBOM und möglichst Provenance erzeugt werden,
12. Staging automatisch deployed und getestet wird,
13. Produktion über definiertes Gate deployed wird,
14. Health Checks und Smoke Tests nach Deployment laufen,
15. Rollback dokumentiert und realistisch ausführbar ist,
16. Secrets geschützt und Berechtigungen minimal sind,
17. Deployment-Traceability gegeben ist,
18. Pipeline-Laufzeit für Entwickler akzeptabel bleibt.

---

## 28. Quellen und weiterführende Literatur

- GitHub Docs — GitHub Actions: https://docs.github.com/en/actions
- GitHub Docs — Artifact Attestations: https://docs.github.com/en/actions/concepts/security/artifact-attestations
- GitHub Docs — Using artifact attestations to establish provenance: https://docs.github.com/actions/security-for-github-actions/using-artifact-attestations/using-artifact-attestations-to-establish-provenance-for-builds
- GitHub Docs — Deployment environments: https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment
- GitLab Docs — CI/CD Pipelines: https://docs.gitlab.com/ci/pipelines/
- GitLab Docs — CI/CD Variables: https://docs.gitlab.com/ci/variables/
- GitLab Docs — Protected branches: https://docs.gitlab.com/user/project/repository/branches/protected/
- Docker Docs — Build and push with GitHub Actions: https://docs.docker.com/build/ci/github-actions/
- Docker Docs — GitHub Actions cache backend: https://docs.docker.com/build/cache/backends/gha/
- Docker Docs — SBOM and provenance attestations with GitHub Actions: https://docs.docker.com/build/ci/github-actions/attestations/
- OWASP Dependency-Check: https://owasp.org/www-project-dependency-check/
- OWASP Dependency-Check Gradle Configuration: https://dependency-check.github.io/DependencyCheck/dependency-check-gradle/configuration.html
- SLSA — Supply-chain Levels for Software Artifacts: https://slsa.dev/
- OpenSSF Scorecard: https://github.com/ossf/scorecard
