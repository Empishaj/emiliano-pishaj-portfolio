# ADR-080 — GitLab CI/CD Pipeline mit DevSecOps: Sicherheit als First-Class-Citizen

| Feld              | Wert                                                        |
|-------------------|-------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                               |
| Entscheider       | CTO / Head of Engineering / CISO                            |
| Datum             | 2024-01-01                                                  |
| Review-Datum      | 2025-01-01 (jährlich, plus nach jedem Major-CVE-Incident)   |
| Kategorie         | DevOps / Security / CI/CD                                   |
| Betroffene Teams  | Alle Engineering-Teams, DevOps, Security, Compliance        |
| Abhängigkeiten    | ADR-036 (CI/CD), ADR-037 (Docker), ADR-038 (Kubernetes), ADR-057 (SBOM), ADR-015 (Security) |

---

## 1. Kontext & Treiber

### 1.1 Das Problem mit "Security am Ende"

Der klassische Entwicklungsprozess behandelt Sicherheit als Phase nach der Entwicklung: Code schreiben, testen, dann einen Security-Scan am Ende der Pipeline. Das Resultat ist strukturell dysfunktional:

```
Klassisches Modell ("Security at the End"):

  Code → Build → Test → Security-Scan → Deploy
                              ↑
                     Fehler werden hier gefunden,
                     aber der Fix muss am Anfang passieren.
                     Kosten: 10–100× höher als frühe Entdeckung.
                     Zeitverlust: Blocker kurz vor Release.
                     Frustration: Entwickler sehen Security als Feind.
```

**Konkrete Schmerzpunkte im Status Quo:**
- Sicherheitslücken werden kurz vor Release entdeckt → Deployment-Freeze, Druck, schlechte Fixes
- Kein Überblick über transitive Abhängigkeiten und deren CVEs (Log4Shell war für viele Teams eine Überraschung)
- Secrets landen in der Git-History (durchschnittlich 1 Leak pro 1.000 Commits laut GitGuardian State of Secrets 2023)
- Container-Images mit bekannten Vulnerabilities in Produktion (Durchschnitt: 1.237 CVEs pro Image laut Anchore Software Composition Analysis Report 2023)
- Compliance-Nachweise (ISO 27001, SOC 2, BSI IT-Grundschutz) erfordern Dokumentation die manuell und aufwändig ist

### 1.2 Der DevSecOps-Ansatz

**DevSecOps** ("Security as Code") integriert Sicherheitsprüfungen als automatisierte, nicht-blockierbare (für niedrige Schwellwerte) oder blockierende (für kritische Findings) Schritte in jeden Pipeline-Durchlauf.

```
DevSecOps-Modell:

  Commit → [SAST] → [Secret-Scan] → Build → [SCA/SBOM] →
  → [Container-Scan] → Test → [DAST] → [Compliance-Check] → Deploy
       ↑                  ↑              ↑           ↑
   Sekunden           Minuten         Minuten     Minuten
   Feedback           Feedback        Feedback    Feedback

Prinzip: Shift Left — Fehler so früh wie möglich finden.
```

### 1.3 Warum GitLab?

GitLab wurde als CI/CD-Plattform gewählt weil es als einzige Plattform alle DevSecOps-Werkzeuge nativ integriert (SAST, DAST, Secret Detection, Container Scanning, License Compliance) — ohne externe Tool-Integrationen und separate Lizenz-Kosten. Die Alternative (GitHub Actions + externe Security-Tools) erzeugt erheblichen Integrations- und Pflege-Aufwand.

**Referenz:** Gartner Magic Quadrant for Application Security Testing 2023 — GitLab in Leader-Position für integrierte DevSecOps-Plattformen.

---

## 2. Entscheidung

Alle Projekte verwenden eine standardisierte GitLab CI/CD-Pipeline mit integrierten DevSecOps-Stages. Die Pipeline ist in drei Schichten strukturiert:

1. **Shift-Left-Security**: SAST und Secret Detection laufen bei jedem Commit, Ergebnisse blockieren den MR bei Critical/High-Findings
2. **Build-Time-Security**: SBOM-Generierung, SCA und Container-Scanning laufen bei jedem Build
3. **Deploy-Time-Security**: DAST und Compliance-Checks laufen gegen Staging vor jedem Production-Deployment

**Nicht verhandelbar:**
- Kein Merge bei Secret-Detection-Findings (Severity: Critical) — keine Ausnahmen
- Kein Production-Deployment bei CVSS ≥ 9.0 (Critical CVE) ohne CISO-Approval
- Alle Security-Findings landen im GitLab Security Dashboard — zentrale Sichtbarkeit für Security-Team

---

## 3. Pipeline-Architektur im Detail

### 3.1 Vollständige `.gitlab-ci.yml`

```yaml
# .gitlab-ci.yml
# Einbinden des Standard-Templates aller Projekte
include:
  # GitLab Security Templates (integriert, keine externe Dependency)
  - template: Security/SAST.gitlab-ci.yml
  - template: Security/Secret-Detection.gitlab-ci.yml
  - template: Security/Dependency-Scanning.gitlab-ci.yml
  - template: Security/Container-Scanning.gitlab-ci.yml
  - template: Security/DAST.gitlab-ci.yml
  - template: Security/License-Scanning.gitlab-ci.yml
  # Unternehmens-Standard-Konfiguration aus eigenem Template-Repo
  - project: 'platform/ci-templates'
    ref: main
    file: '/templates/java-spring-boot.yml'

# ── Globale Konfiguration ──────────────────────────────────────────────────
variables:
  # Java-Konfiguration
  JAVA_VERSION: "21"
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.caching=true"

  # Docker/Container
  DOCKER_DRIVER:     overlay2
  DOCKER_TLS_CERTDIR: "/certs"
  CONTAINER_IMAGE:   "${CI_REGISTRY_IMAGE}:${CI_COMMIT_SHA}"

  # Security-Konfiguration
  SAST_EXCLUDED_PATHS: "spec,test,tests,tmp"
  DS_EXCLUDED_PATHS:   "node_modules,.cache"
  CS_SEVERITY_THRESHOLD: "HIGH"       # Container-Scan: Block bei HIGH+
  DAST_WEBSITE:         "https://staging.example.com"
  SECRET_DETECTION_EXCLUDED_PATHS: "src/test"

  # SBOM
  CYCLONEDX_SCHEMA_VERSION: "1.5"

# ── Stages: Reihenfolge ist Sicherheits-Philosophie ──────────────────────
stages:
  - validate          # Syntaxprüfung, Formatierung — schnell, immer
  - security-early    # SAST, Secret Detection — Shift Left
  - build             # Kompilieren, Packaging
  - test              # Unit, Integration, Mutation
  - security-build    # SCA, SBOM, Container-Scan
  - quality           # Code-Qualität, Coverage, ArchUnit
  - deploy-staging    # Deploy auf Staging
  - security-runtime  # DAST gegen Staging
  - compliance        # License-Check, Policy-Check
  - deploy-production # Nur nach allen Gates

# ── Stage: validate ───────────────────────────────────────────────────────
validate:checkstyle:
  stage: validate
  image: eclipse-temurin:21-jdk-alpine
  script:
    - ./gradlew checkstyleMain checkstyleTest --no-daemon
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'
  cache:
    key: gradle-${CI_COMMIT_REF_SLUG}
    paths: [.gradle/]

validate:commit-message:
  stage: validate
  image: python:3.12-alpine
  script:
    # Conventional Commits prüfen (→ ADR-064)
    - pip install commitizen --quiet
    - cz check --commit-msg-file "${CI_COMMIT_MESSAGE}"
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'

# ── Stage: security-early — SHIFT LEFT ───────────────────────────────────
# SAST: Statische Code-Analyse auf Schwachstellen-Pattern
# GitLab verwendet SpotBugs für Java mit Find-Security-Bugs-Plugin
sast:
  stage: security-early
  variables:
    SAST_JAVA_VERSION: "21"
    FIND_SECURITY_BUGS_LEVEL: 1    # 1 = niedrigste Schwelle
  artifacts:
    reports:
      sast: gl-sast-report.json   # → GitLab Security Dashboard
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'

# Secret Detection: verhindert dass API-Keys, Passwörter, Tokens in Git landen
secret_detection:
  stage: security-early
  variables:
    SECRET_DETECTION_HISTORIC_SCAN: "false"  # Nur neue Commits
  artifacts:
    reports:
      secret_detection: gl-secret-detection-report.json
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'
  # WICHTIG: Secret-Detection blockiert MR bei ANY finding (konfiguriert in Security Policy)

# Eigene Secret-Prüfung: zusätzlich zu GitLab's eingebautem Scanner
security:detect-secrets:
  stage: security-early
  image: python:3.12-slim
  script:
    - pip install detect-secrets --quiet
    # Prüft gegen Baseline — neue Secrets werden gefunden
    - detect-secrets scan --baseline .secrets.baseline
    - detect-secrets audit .secrets.baseline
  artifacts:
    paths: [.secrets.baseline]
    expire_in: 1 week
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'

# ── Stage: build ─────────────────────────────────────────────────────────
build:compile:
  stage: build
  image: eclipse-temurin:21-jdk
  script:
    - ./gradlew assemble --no-daemon
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 day
  cache:
    key: gradle-${CI_COMMIT_REF_SLUG}
    paths: [.gradle/, build/]

build:docker:
  stage: build
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    # Multi-Stage Build (→ ADR-037)
    - |
      docker build \
        --build-arg JAR_FILE=build/libs/*.jar \
        --cache-from $CI_REGISTRY_IMAGE:latest \
        --label "org.opencontainers.image.revision=${CI_COMMIT_SHA}" \
        --label "org.opencontainers.image.created=$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        --label "org.opencontainers.image.source=${CI_PROJECT_URL}" \
        -t $CONTAINER_IMAGE \
        -t $CI_REGISTRY_IMAGE:latest \
        .
    - docker push $CONTAINER_IMAGE
    - docker push $CI_REGISTRY_IMAGE:latest
  needs: [build:compile]

# ── Stage: test ──────────────────────────────────────────────────────────
test:unit:
  stage: test
  image: eclipse-temurin:21-jdk
  services:
    # Testcontainers benötigt Docker-Socket
    - docker:24-dind
  variables:
    DOCKER_HOST: "tcp://docker:2376"
    TESTCONTAINERS_RYUK_DISABLED: "true"
  script:
    - ./gradlew test --no-daemon
  artifacts:
    when: always
    reports:
      junit: build/test-results/test/**/*.xml
    paths:
      - build/reports/tests/
      - build/reports/jacoco/
    expire_in: 1 week
  coverage: '/Total.*?([0-9]{1,3})%/'

test:integration:
  stage: test
  image: eclipse-temurin:21-jdk
  services:
    - docker:24-dind
  variables:
    DOCKER_HOST: "tcp://docker:2376"
  script:
    - ./gradlew integrationTest --no-daemon
  artifacts:
    when: always
    reports:
      junit: build/test-results/integrationTest/**/*.xml

test:architecture:
  stage: test
  image: eclipse-temurin:21-jdk
  script:
    # ArchUnit + Spring Modulith Verify (→ ADR-061, ADR-079)
    - ./gradlew architectureTest --no-daemon
  artifacts:
    when: always
    reports:
      junit: build/test-results/architectureTest/**/*.xml

# ── Stage: security-build ────────────────────────────────────────────────
# Software Composition Analysis: Abhängigkeiten auf bekannte CVEs prüfen
dependency_scanning:
  stage: security-build
  variables:
    DS_JAVA_VERSION: "21"
    DS_GRADLE_VERSION: "8"
  artifacts:
    reports:
      dependency_scanning: gl-dependency-scanning-report.json
  needs: [build:compile]

# SBOM generieren — maschinenlesbare Zutatenliste (→ ADR-057)
security:sbom:
  stage: security-build
  image: eclipse-temurin:21-jdk
  script:
    - ./gradlew cyclonedxBom --no-daemon
    # SBOM als GitLab-Artifact hochladen → in Merge Request sichtbar
    - cp build/reports/bom.json gl-sbom.json
  artifacts:
    reports:
      cyclonedx: gl-sbom.json
    paths: [build/reports/bom.json]
    expire_in: 90 days   # SBOM für 90 Tage für Audit-Trail aufbewahren
  needs: [build:compile]

# OWASP Dependency-Check: zweiter Scan mit NVD-Datenbank
security:owasp-dc:
  stage: security-build
  image: eclipse-temurin:21-jdk
  script:
    - ./gradlew dependencyCheckAnalyze --no-daemon
  after_script:
    # JUnit-Report für GitLab-Integration
    - |
      python3 << 'EOF'
      import json, xml.etree.ElementTree as ET
      # Dependency-Check JSON → JUnit-Report Konvertierung
      # (Skript im separaten File für Lesbarkeit)
      EOF
  artifacts:
    when: always
    paths:
      - build/reports/dependency-check-report.html
      - build/reports/dependency-check-report.json
    expire_in: 30 days
  cache:
    key: owasp-dc-data
    paths: [~/.dependency-check-data/]   # NVD-Datenbank cachen
  variables:
    NVD_API_KEY: $NVD_API_KEY   # Aus GitLab CI/CD Variables (masked!)

# Container-Scan: bekannte CVEs im Docker-Image prüfen
container_scanning:
  stage: security-build
  variables:
    CS_IMAGE: $CONTAINER_IMAGE
    CS_SEVERITY_THRESHOLD: "HIGH"
    CS_DOCKERFILE_PATH: "Dockerfile"
  artifacts:
    reports:
      container_scanning: gl-container-scanning-report.json
  needs: [build:docker]

# Trivy: zusätzlicher Container-Scan (zweite Meinung)
security:trivy:
  stage: security-build
  image:
    name: aquasec/trivy:latest
    entrypoint: [""]
  script:
    - trivy image
        --exit-code 1
        --severity CRITICAL
        --ignore-unfixed
        --format sarif
        --output trivy-results.sarif
        $CONTAINER_IMAGE
  artifacts:
    reports:
      sast: trivy-results.sarif    # Erscheint im Security Dashboard
    when: always
  needs: [build:docker]
  allow_failure: false             # CRITICAL CVEs = Pipeline schlägt fehl

# ── Stage: quality ────────────────────────────────────────────────────────
quality:sonarqube:
  stage: quality
  image: eclipse-temurin:21-jdk
  script:
    - ./gradlew sonar
        -Dsonar.projectKey=${CI_PROJECT_NAMESPACE}_${CI_PROJECT_NAME}
        -Dsonar.host.url=$SONAR_HOST_URL
        -Dsonar.token=$SONAR_TOKEN
        -Dsonar.pullrequest.key=${CI_MERGE_REQUEST_IID}
        -Dsonar.pullrequest.branch=${CI_MERGE_REQUEST_SOURCE_BRANCH_NAME}
        -Dsonar.pullrequest.base=${CI_MERGE_REQUEST_TARGET_BRANCH_NAME}
        --no-daemon
    # Quality Gate prüfen
    - |
      STATUS=$(curl -s -u $SONAR_TOKEN: \
        "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=${CI_PROJECT_NAMESPACE}_${CI_PROJECT_NAME}" \
        | jq -r '.projectStatus.status')
      if [ "$STATUS" != "OK" ]; then
        echo "SonarQube Quality Gate FAILED: $STATUS"
        exit 1
      fi
  needs: [test:unit, test:integration]
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'

quality:coverage-gate:
  stage: quality
  image: eclipse-temurin:21-jdk
  script:
    # Coverage-Prüfung: < 80% = Fehler (→ ADR-045)
    - ./gradlew jacocoTestCoverageVerification --no-daemon
  needs: [test:unit]

quality:mutation-test:
  stage: quality
  image: eclipse-temurin:21-jdk
  script:
    # Mutation Testing (→ ADR-045) — läuft nur auf Main/Release-Branches (langsam)
    - ./gradlew pitest --no-daemon
  artifacts:
    paths: [build/reports/pitest/]
    expire_in: 1 week
  rules:
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'
    - if: '$CI_COMMIT_BRANCH =~ /^release\/.*/'
  allow_failure: false

# ── Stage: deploy-staging ─────────────────────────────────────────────────
deploy:staging:
  stage: deploy-staging
  image: bitnami/kubectl:latest
  environment:
    name: staging
    url: https://staging.example.com
  script:
    - kubectl config use-context $KUBE_CONTEXT_STAGING
    - |
      kubectl set image deployment/order-service \
        order-service=$CONTAINER_IMAGE \
        --namespace staging
    - kubectl rollout status deployment/order-service --namespace staging --timeout=5m
    # Smoke-Test nach Deployment
    - |
      for i in $(seq 1 10); do
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
          https://staging.example.com/actuator/health)
        if [ "$HTTP_CODE" = "200" ]; then
          echo "Staging health check passed"
          break
        fi
        echo "Attempt $i: HTTP $HTTP_CODE, retrying..."
        sleep 10
      done
  needs:
    - build:docker
    - test:unit
    - test:integration
    - test:architecture
    - security:trivy    # Container-Scan muss grün sein vor Staging-Deploy
  rules:
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'
    - if: '$CI_COMMIT_BRANCH =~ /^release\/.*/'

# ── Stage: security-runtime (DAST) ────────────────────────────────────────
# DAST: Dynamic Application Security Testing gegen laufende Staging-Instanz
dast:
  stage: security-runtime
  variables:
    DAST_WEBSITE:           "https://staging.example.com"
    DAST_FULL_SCAN_ENABLED: "false"   # Full Scan nur wöchentlich (langsam!)
    DAST_API_SPECIFICATION: "https://staging.example.com/v3/api-docs"
    # OpenAPI-Spec für intelligente DAST-Crawling
    DAST_API_PROFILE:       "Quick"
  artifacts:
    reports:
      dast: gl-dast-report.json
  needs: [deploy:staging]
  rules:
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'

# Wöchentlicher vollständiger DAST-Scan (cron-gesteuert)
dast:full:
  extends: dast
  variables:
    DAST_FULL_SCAN_ENABLED: "true"
  rules:
    - if: '$CI_PIPELINE_SOURCE == "schedule"'
      when: always

# OWASP ZAP: zweiter DAST-Scanner (zweite Meinung)
security:zap:
  stage: security-runtime
  image: owasp/zap2docker-stable:latest
  script:
    - |
      zap-baseline.py \
        -t https://staging.example.com \
        -r zap-report.html \
        -x zap-report.xml \
        -l WARN \
        --auto
  artifacts:
    when: always
    paths: [zap-report.html, zap-report.xml]
    expire_in: 30 days
  needs: [deploy:staging]
  allow_failure: true   # ZAP-Baseline: Warnungen blockieren nicht, kritische schon
  rules:
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'

# ── Stage: compliance ─────────────────────────────────────────────────────
# License Compliance: keine GPL in kommerziellem Produkt
license_scanning:
  stage: compliance
  artifacts:
    reports:
      license_scanning: gl-license-scanning-report.json

compliance:license-gate:
  stage: compliance
  image: eclipse-temurin:21-jdk
  script:
    # Eigene Lizenz-Policy (→ ADR-057)
    - ./gradlew checkLicense --no-daemon
    # Erlaubte Lizenzen: Apache-2.0, MIT, BSD-2/3, EPL-2.0
    # Verbotene: GPL-2.0, GPL-3.0, AGPL (ohne Legal-Review)
  needs: [build:compile]

compliance:policy-check:
  stage: compliance
  image: openpolicyagent/opa:latest-static
  script:
    # OPA: Sicherheitsrichtlinien als Code prüfen
    - |
      opa eval \
        --data policy/security-policy.rego \
        --input gl-sast-report.json \
        --format pretty \
        'data.security.allow'
    # Policy: keine Medium-Findings auf API-Authentifizierung-Code
  needs:
    - job: sast
      artifacts: true
  rules:
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'

# ── Stage: deploy-production ──────────────────────────────────────────────
deploy:production:
  stage: deploy-production
  image: bitnami/kubectl:latest
  environment:
    name: production
    url: https://example.com
  # Manueller Approval-Gate + automatische Sicherheits-Vorbedingungen
  when: manual
  script:
    - kubectl config use-context $KUBE_CONTEXT_PROD
    # Canary Deployment via Argo Rollouts (→ ADR-059)
    - |
      kubectl argo rollouts set image order-service \
        order-service=$CONTAINER_IMAGE \
        --namespace production
    # Warte auf Canary-Promotion (automatisch via Argo Rollouts Analyse)
    - |
      kubectl argo rollouts status order-service \
        --namespace production \
        --timeout 30m
  needs:
    - deploy:staging
    - dast
    - compliance:license-gate
    - compliance:policy-check
    - quality:sonarqube
  rules:
    - if: '$CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH'
    - if: '$CI_COMMIT_BRANCH =~ /^release\/.*/'
```

### 3.2 GitLab Security Policy: Automatische Blockierung

```yaml
# .gitlab/security-policies/policy.yml
# GitLab Security Policy: konfiguriert wann Findings den MR blockieren

scan_result_policies:
  - name: "Block Critical Secrets"
    description: "Jeder Fund eines Secrets blockiert den Merge — keine Ausnahmen"
    enabled: true
    rules:
      - type: scan_finding
        branches: ["*"]
        scanners: [secret_detection]
        vulnerabilities_allowed: 0    # Kein einziger Secret-Fund erlaubt
        severity_levels: [critical, high, medium, low, info, unknown]
        vulnerability_states: [detected]
    actions:
      - type: require_approval
        approvals_required: 2
        user_approvers: [ciso, security-team-lead]

  - name: "Block Critical SAST Findings"
    description: "Critical/High SAST-Findings erfordern Security-Review"
    enabled: true
    rules:
      - type: scan_finding
        branches: ["main", "release/*"]
        scanners: [sast]
        vulnerabilities_allowed: 0
        severity_levels: [critical, high]
        vulnerability_states: [detected, confirmed]
    actions:
      - type: require_approval
        approvals_required: 1
        user_approvers: [security-team-lead]

  - name: "Block Critical CVEs in Dependencies"
    description: "CVSS >= 9.0 erfordert CISO-Approval"
    enabled: true
    rules:
      - type: scan_finding
        branches: ["main"]
        scanners: [dependency_scanning, container_scanning]
        vulnerabilities_allowed: 0
        severity_levels: [critical]
        vulnerability_states: [detected]
    actions:
      - type: require_approval
        approvals_required: 1
        user_approvers: [ciso]
```

---

## 4. Security-Dashboard: Zentraler Überblick

```yaml
# GitLab Security Dashboard: automatisch befüllt durch alle Security-Jobs
# Konfiguration: Group-Level Security Dashboard aktivieren

# Eigenes Security-Reporting-Job für Audit-Trail
security:weekly-report:
  stage: compliance
  image: python:3.12-slim
  script:
    - pip install requests jinja2 --quiet
    - python3 scripts/generate-security-report.py
  artifacts:
    paths: [security-report.pdf]
    expire_in: 1 year   # Compliance: 1 Jahr aufbewahren
  rules:
    - if: '$CI_PIPELINE_SOURCE == "schedule"'
      when: always
```

```python
# scripts/generate-security-report.py
# Wöchentlicher Security-Report: alle offenen Findings, Trends, SLAs
import requests
from datetime import datetime, timedelta

class SecurityReportGenerator:
    """
    Generiert wöchentlichen Security-Report aus GitLab Security Dashboard API.
    Verwendet für:
    - Management-Reporting
    - ISO-27001-Compliance-Nachweis
    - SLA-Tracking: Critical-Findings müssen innerhalb 24h adressiert sein
    """

    SEVERITY_SLA = {
        "critical": timedelta(hours=24),
        "high":     timedelta(days=7),
        "medium":   timedelta(days=30),
        "low":      timedelta(days=90),
    }

    def collect_findings(self):
        resp = requests.get(
            f"{GITLAB_URL}/api/v4/projects/{PROJECT_ID}/vulnerabilities",
            headers={"PRIVATE-TOKEN": GITLAB_TOKEN},
            params={"state": "detected", "per_page": 100}
        )
        return resp.json()

    def check_sla_breaches(self, findings):
        breaches = []
        now = datetime.utcnow()
        for f in findings:
            severity   = f["severity"].lower()
            detected   = datetime.fromisoformat(f["detected_at"])
            sla        = self.SEVERITY_SLA.get(severity, timedelta(days=90))
            if now - detected > sla:
                breaches.append({
                    "id":         f["id"],
                    "severity":   severity,
                    "title":      f["title"],
                    "overdue_by": str(now - detected - sla)
                })
        return breaches
```

---

## 5. Secrets-Management in der Pipeline

```yaml
# GitLab CI/CD Variables — Kategorien und Schutzregeln

# MASKED: Wert wird in Job-Logs durch *** ersetzt
# PROTECTED: Nur auf protected Branches verfügbar
# FILE: Als Datei im Job verfügbar (für Zertifikate, Kubeconfig)

# Sichere Variablen-Kategorien:
#
# Kategorie A — Credentials (MASKED + PROTECTED):
#   $SONAR_TOKEN           → SonarQube API Token
#   $NVD_API_KEY           → NIST NVD API Key für Dependency-Check
#   $CI_REGISTRY_PASSWORD  → Docker Registry (automatisch von GitLab)
#   $KUBE_CONTEXT_PROD     → Kubernetes Production Context
#
# Kategorie B — Infrastruktur (PROTECTED, nicht notwendig MASKED):
#   $SONAR_HOST_URL        → https://sonar.internal
#   $DAST_WEBSITE          → https://staging.example.com
#
# Kategorie C — Feature-Flags (ungeschützt, öffentlich):
#   $SKIP_DAST             → "true" in Entwicklungsbranches
#   $MUTATION_ENABLED      → "false" in Feature-Branches

# NIEMALS in CI-Variablen:
# - Produktions-Datenbank-Passwörter (→ Vault, ADR-069)
# - Private TLS-Keys (→ Kubernetes Secrets, ADR-038)
# - Kunden-Daten irgendeiner Art
```

```yaml
# .gitlab/ci/secrets-scanning.yml
# Zusätzliche pre-commit Hook-Konfiguration als Ergänzung zum Pipeline-Scan
# (Erster Schutz: Entwickler-Maschine; Zweiter Schutz: Pipeline)

# detect-secrets Baseline — in Git committed
# .secrets.baseline wird bei jedem MR geprüft
# Neue Secrets = Pipeline schlägt fehl
```

---

## 6. Pipeline-Visualisierung

```
Commit / MR öffnen
│
├─ [validate]          ~2 Min
│   ├── checkstyle ✓
│   └── commit-message ✓
│
├─ [security-early]    ~5 Min   ← SHIFT LEFT
│   ├── SAST (SpotBugs + Find-Security-Bugs)
│   │     Critical/High → MR blockiert, Security-Review erforderlich
│   └── Secret Detection
│         Jeder Fund → MR blockiert, CISO-Approval erforderlich
│
├─ [build]             ~4 Min
│   ├── compile (Gradle)
│   └── docker build & push
│
├─ [test]              ~12 Min
│   ├── unit tests (mit Testcontainers)
│   ├── integration tests
│   └── architecture tests (ArchUnit + Modulith Verify)
│
├─ [security-build]    ~8 Min
│   ├── Dependency Scanning (GitLab native)
│   ├── OWASP Dependency-Check (CVSS ≥ 7 → fail)
│   ├── SBOM generieren (CycloneDX → GitLab Artifact)
│   ├── Container Scanning (GitLab native)
│   └── Trivy Scan (CRITICAL → fail)
│
├─ [quality]           ~8 Min
│   ├── SonarQube + Quality Gate
│   ├── Coverage Gate (≥ 80%)
│   └── Mutation Testing (nur main/release)
│
├─ [deploy-staging]    ~5 Min   (nur main/release)
│   └── kubectl rollout → Staging
│       Smoke-Test /actuator/health
│
├─ [security-runtime]  ~10 Min  (nur main/release)
│   ├── DAST (GitLab native, gegen Staging)
│   └── OWASP ZAP Baseline Scan
│
├─ [compliance]        ~3 Min
│   ├── License Scanning
│   ├── License Gate (kein GPL/AGPL)
│   └── OPA Policy Check
│
└─ [deploy-production] MANUELL  (nach allen Gates)
    └── Argo Rollouts Canary (5% → 25% → 50% → 100%)

GESAMT: ~57 Minuten (main branch, vollständige Pipeline)
        ~27 Minuten (MR, ohne Staging/DAST/Production)
```

---

## 7. Branching-Strategie & Pipeline-Regeln

```
Branch           Pipeline-Umfang                    Schutzstufe
─────────────────────────────────────────────────────────────────
main             Vollständig (alle Stages)           Protected, kein Direct-Push
release/*        Vollständig + Hotfix-Bereitschaft   Protected
develop          Ohne DAST, ohne Production-Deploy   Protected
feature/*        validate + security-early + build + test
fix/*            validate + security-early + build + test
hotfix/*         Vollständig + expedited approval    Protected

MR-Regeln (erzwungen via GitLab MR Settings):
  ✓ Mindestens 1 Approval von Code-Reviewer
  ✓ Mindestens 1 Approval von Security-Team (wenn Security-Findings vorhanden)
  ✓ Alle Pipeline-Stages grün (außer explizit allow_failure: true)
  ✓ Keine ungelösten Diskussions-Threads
  ✓ MR-Branch muss up-to-date mit Target-Branch sein
```

---

## 8. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| **GitHub Actions + externe Security-Tools** | Größtes Ökosystem, bekannt | Jedes Security-Tool separate Integration, Lizenz-Kosten, Wartungsaufwand | GitLab integriert SAST/DAST/Secret-Detection/Container-Scanning nativ — kein Integrations-Overhead |
| **Jenkins + Plugin-Ökosystem** | Maximum Flexibilität, On-Premise | Wartungsaufwand enorm, Security-Plugins veralten schnell, keine native MR-Integration | DORA: Teams mit modernen CI/CD-Plattformen deployen 4× häufiger; Jenkins-Wartung frisst Engineering-Kapazität |
| **Security-Scans nur wöchentlich (Batch)** | Schnellere tägliche Pipeline | Findings werden spät entdeckt, Kontext ist weg, Fix-Kosten 10× höher | NIST SSDLC: jede Stunde die ein Security-Finding später entdeckt wird, erhöht die Fix-Kosten um 10% |
| **Kein DAST (nur SAST)** | Deutlich schnellere Pipeline | SAST findet nur ca. 30% der Runtime-Vulnerabilities (OWASP) | OWASP DAST-Studie: DAST findet 50–70% der Injection-Vulnerabilities die SAST übersieht |
| **Separates Security-Team führt Scans durch** | Klare Verantwortlichkeit | Engpass, kein Shift Left, Entwickler sehen Security als fremdes Problem | DevSecOps Manifesto: Security ist Verantwortung aller — nicht nur des Security-Teams |

---

## 9. Trade-off-Matrix

| Qualitätsziel | GitLab + DevSecOps (gewählt) | GitHub Actions + extern | Jenkins | Nur SAST, kein DAST |
|---|---|---|---|---|
| Security-Abdeckung | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| Pipeline-Geschwindigkeit | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| Wartungsaufwand | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| Compliance-Nachweise | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| Developer Experience | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Kosten | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Shift-Left-Fähigkeit | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |

---

## 10. Kosten-Nutzen-Analyse

### 10.1 Kosten

```
Initiale Einrichtung:
  Pipeline-Konfiguration (.gitlab-ci.yml, Policies):     5 Personentage
  Security-Policy-Definition (OPA, GitLab Policies):     3 Personentage
  SonarQube-Integration + Quality-Gate-Konfiguration:    2 Personentage
  Team-Schulung (DevSecOps-Mindset, Tool-Nutzung):       8 Personentage (4h × alle Devs)
  GESAMT INITIAL:                                        ~18 Personentage
  KOSTEN (80 EUR/h × 8h):                               ~11.520 EUR

Laufende Kosten:
  GitLab Ultimate Lizenz (DevSecOps-Features):           ~29 EUR/User/Monat
  Für 20 User:                                           580 EUR/Monat
  SonarQube Developer Edition:                           ~150 EUR/Monat
  GitLab Runner (Self-Hosted, 4 vCPU 8 GB):             ~60 EUR/Monat (Cloud-VM)
  GESAMT LAUFEND:                                        ~790 EUR/Monat

Pipeline-Overhead:
  Mehrzeit pro Pipeline (Security-Jobs):                 +20–30 Minuten
  Bei 20 Commits/Tag × 30 Min:                          10h/Tag Machine-Zeit
  Kein Entwickler-Wartezeit da parallel                  0 EUR/Monat (asynchrone Ausführung)
```

### 10.2 Nutzen (quantifiziert)

```
Nutzen 1: Frühe Fehlerentdeckung (Shift Left)
  IBM-Studie: Bug im Design gefunden = 1× Kosten
  Bug in Produktion gefunden = 100× Kosten
  Annahme: 5 Security-Bugs/Monat werden früher gefunden
  Einsparung: 5 × (2h Prod-Fix vs. 20 Min CI-Fix) × 80 EUR = 3.067 EUR/Monat

Nutzen 2: Kein Secret-Leak in Git-History
  GitGuardian Report 2023: Durchschnittliche Kosten eines API-Key-Leaks: 11.000 EUR
  (Credential-Rotation, Incident-Response, potenzielle Datenverletzung)
  Präventionsrate: 95% (Secret Detection fängt fast alle ab)
  Erwarteter Nutzen: 11.000 × 0.95 × P(Leak/Monat) = ~440 EUR/Monat
  (Bei P(Leak) = 5%/Monat ohne Secret-Detection)

Nutzen 3: Compliance-Nachweise automatisiert
  ISO 27001 / SOC 2 Audit-Vorbereitung ohne DevSecOps:  40–80 Stunden manuell
  Mit automatisierten Security-Reports (Pipeline-Artifacts): 4–8 Stunden
  Einsparung pro Audit: 36h × 80 EUR = 2.880 EUR
  Bei 2 Audits/Jahr: 480 EUR/Monat

Nutzen 4: Kein Log4Shell-Typ-Incident
  Average Cost of a Data Breach 2023 (IBM): 4,45 Mio EUR
  Präventionsrate durch SCA: ~80% bekannter CVEs werden vor Deploy erkannt
  Erwarteter Nutzen (versicherungsmathematisch):
  4.450.000 × 0,80 × P(Breach/Jahr) / 12 Monate
  Bei P(Breach) = 1% ohne SCA: 2.967 EUR/Monat

GESAMT MONATLICHER NUTZEN:         ~6.954 EUR/Monat
LAUFENDE KOSTEN:                      ~790 EUR/Monat
NETTO MONATLICHER VORTEIL:          ~6.164 EUR/Monat

BREAK-EVEN INITIALE KOSTEN:
  11.520 EUR ÷ 6.164 EUR/Monat = 1,9 Monate
```

---

## 11. Konsequenzen

### 11.1 Sofort (Woche 1–2)
- `.gitlab-ci.yml` mit allen Security-Stages einrichten
- GitLab Security Policies konfigurieren (Secret Detection blockiert MR sofort)
- Team-Workshop: DevSecOps-Mindset, wie man Security-Dashboard liest
- `NVD_API_KEY` als geschützte CI/CD-Variable hinterlegen
- SonarQube Quality Gate definieren (mindestens: 0 neue Critical-Bugs, 80% Coverage)

### 11.2 Mittelfristig (1–3 Monate)
- OPA-Policies definieren: welche Findings sind für welchen Code-Bereich nicht akzeptabel
- Security-Dashboard-Review in Sprint-Retrospektive einbauen: "welche neuen Findings gibt es?"
- DAST-Baseline erstellen: bekannte False-Positives in ZAP-Konfiguration ausschließen
- Wöchentlichen Security-Report automatisieren (management-facing)

### 11.3 Langfristig (3–12 Monate)
- Alle SLA-Breaches auf null: Critical = 24h, High = 7d, Medium = 30d
- Security-Score im Team-Dashboard sichtbar machen (Gamification)
- Jährliches Penetration-Testing als Ergänzung zu automatischem DAST

### 11.4 Risiken

| Risiko | Wahrscheinlichkeit | Auswirkung | Mitigation |
|---|---|---|---|
| Pipeline zu langsam → Entwickler umgehen sie | M | H | Security-Jobs parallel; Shift-Left macht die kritischen Jobs schnell; Developer Experience monitoren |
| False Positives führen zu "Alert Fatigue" | H | M | SAST-Tuning, Whitelist bekannter FP, OPA-Policies; wöchentliches Review |
| Secret Detection übersieht Secrets in Binary-Artifacts | M | H | Zweite Scan-Ebene (detect-secrets); Pre-commit Hooks als erster Schutz |
| DAST erzeugt Last auf Staging (unerwünschte Side-Effects) | L | M | DAST in separatem Staging-Namespace; Read-Only-Scan-Modus für kritische Endpunkte |
| GitLab Ultimate Lizenzkosten steigen | L | M | Self-Hosted GitLab als Fallback; Open-Source-Alternativen (Trivy, OWASP DC) als Backup-Scans |

---

## 12. Quellen & Referenzen

- **NIST SSDLC (Secure Software Development Lifecycle), NIST SP 800-218 (2022)** — Shift-Left-Prinzip: Kosten der Fehlerentdeckung steigen exponentiell mit jeder Phase; Security-by-Default.
- **OWASP DevSecOps Guideline (2023)** — Integration von Security-Scans in CI/CD; DAST vs. SAST Coverage-Metriken.
- **IBM Cost of a Data Breach Report 2023** — Durchschnittliche Breach-Kosten: 4,45 Mio. USD; Einsparung durch DevSecOps: 1,68 Mio. USD durchschnittlich.
- **GitGuardian "State of Secrets Sprawl" 2023** — 1 Secret-Leak pro 1.000 Commits; durchschnittliche Entdeckungszeit ohne automatisierte Scans: 27 Tage.
- **DORA State of DevOps 2023** — High-Performer deployen 973× häufiger als Low-Performer; Security-Integration korreliert positiv mit Deployment-Frequenz.
- **Gartner Magic Quadrant for Application Security Testing 2023** — GitLab in Leader-Position für integrierte DevSecOps-Plattformen.
- **Anchore "Software Composition Analysis in Modern DevOps" (2023)** — Durchschnittlich 1.237 CVEs pro Container-Image ohne SCA-Integration.
- **Gene Kim, Jez Humble, Patrick Debois, "The DevOps Handbook" (2016), Kap. 21** — Security als geteilte Verantwortung aller Engineering-Teams; "Security as Code".
- **Shannon Lietz, "DevSecOps Manifesto" (2015)** — Prinzipien der Security-Integration in DevOps-Kultur.
- **CVE-2021-44228 (Log4Shell) Post-Mortem** — Systemische Ursache: keine SCA-Scans, keine SBOM, kein Überblick über transitive Abhängigkeiten.

---

## 13. Akzeptanzkriterien

- [ ] `.gitlab-ci.yml` mit allen 8 Security-Stages existiert und ist in CI aktiv
- [ ] Secret-Detection-Policy ist aktiv: jeder Fund blockiert den MR ohne Ausnahme
- [ ] SBOM wird für jeden Build generiert und 90 Tage aufbewahrt (Audit-Trail)
- [ ] SonarQube Quality Gate ist definiert und blockiert MR bei Verletzung
- [ ] OWASP Dependency-Check schlägt fehl bei CVSS ≥ 7.0 (konfiguriert in build.gradle.kts)
- [ ] Trivy schlägt fehl bei CRITICAL CVE im Container-Image vor Staging-Deploy
- [ ] GitLab Security Dashboard zeigt alle Findings zentral — Security-Team hat Zugriff
- [ ] Wöchentlicher Security-Report wird automatisch generiert (Schedule-Pipeline)
- [ ] SLA-Tracking ist aktiv: Critical-Findings werden innerhalb 24h adressiert (gemessen via GitLab-API)
- [ ] Team-Schulung dokumentiert: alle Entwickler kennen Security-Dashboard und Finding-Interpretation

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP Top 10 als Inhalt der SAST-Regeln
- [ADR-036](ADR-036-devops-cicd.md) — Basis-Pipeline (GitHub Actions als Vergleich)
- [ADR-037](ADR-037-docker-container.md) — Container-Image das gescannt wird
- [ADR-038](ADR-038-kubernetes.md) — Deployment-Ziel nach bestandenen Security-Gates
- [ADR-040](ADR-040-oauth2-oidc-jwt.md) — DAST testet auch Auth-Endpoints
- [ADR-057](ADR-057-sbom-dependency-auditing.md) — SBOM-Standard (CycloneDX) der in Pipeline erzeugt wird
- [ADR-059](ADR-059-blue-green-canary.md) — Canary-Deployment nach bestandenen DAST-Tests
- [ADR-064](ADR-064-changelog-versionierung.md) — Conventional Commits werden in Pipeline validiert
- [ADR-069](ADR-069-configuration-management.md) — Vault-Secrets werden als masked CI/CD-Variablen übergeben
