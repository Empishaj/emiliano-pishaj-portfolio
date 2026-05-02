# ADR-057 — SBOM, Dependency Auditing & Supply Chain Security

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Gradle 8.x · OWASP Dependency-Check          |
| Datum      | 2024-01-01                                        |
| Kategorie  | Security / DevOps                                 |

---

## Kontext & Problem

Log4Shell (CVE-2021-44228) hat gezeigt: eine verwundbare transitive Abhängigkeit kann in Minuten tausende Anwendungen kompromittieren. Software Supply Chain Security bedeutet: wissen was im eigenen Produkt steckt, wissen wann es verwundbar wird, und schnell handeln können. Ein **SBOM** (Software Bill of Materials) ist die Zutatenliste des eigenen Systems.

---

## Was ist ein SBOM?

```
SBOM = vollständige Liste aller Abhängigkeiten im System
  - Direkte Abhängigkeiten (was in build.gradle.kts steht)
  - Transitive Abhängigkeiten (was die Abhängigkeiten brauchen)
  - Mit: Name, Version, Lizenz, Herkunft, Hash

Warum:
  - Bei CVE-Meldung: sofort wissen ob eigene Anwendung betroffen
  - Compliance: DSGVO, BSI IT-Grundschutz verlangen Nachweisbarkeit
  - Lizenz-Compliance: GPL in kommerzieller Anwendung = rechtliches Problem
```

---

## SBOM generieren mit CycloneDX

```kotlin
// build.gradle.kts
plugins {
    id("org.cyclonedx.bom") version "1.8.2"
}

cyclonedxBom {
    includeConfigs.set(listOf("runtimeClasspath"))
    skipConfigs.set(listOf("testRuntimeClasspath"))
    outputFormat.set("json")  // oder "xml"
    outputName.set("bom")
    outputDir.set(file("build/reports"))
    includeMetadataResolution.set(true)
}

// ./gradlew cyclonedxBom → erzeugt build/reports/bom.json
// Format: CycloneDX (Standard für SBOMs, von OWASP)
```

```json
// bom.json (Auszug)
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.5",
  "version": 1,
  "metadata": {
    "timestamp": "2024-01-15T10:30:00Z",
    "component": {
      "type": "application",
      "name": "order-service",
      "version": "2.1.0"
    }
  },
  "components": [
    {
      "type": "library",
      "name": "spring-boot",
      "version": "3.3.0",
      "group": "org.springframework.boot",
      "purl": "pkg:maven/org.springframework.boot/spring-boot@3.3.0",
      "licenses": [{ "license": { "id": "Apache-2.0" } }],
      "hashes": [{ "alg": "SHA-256", "content": "abc123..." }]
    }
  ]
}
```

---

## CVE-Scanning mit OWASP Dependency-Check

```kotlin
// build.gradle.kts — bereits in ADR-036 erwähnt, hier vertieft
plugins {
    id("org.owasp.dependencycheck") version "9.0.9"
}

dependencyCheck {
    // NVD API Key für schnellere Updates (kostenlos bei nvd.nist.gov)
    nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""

    // Fehlschlag bei CVSS-Score >= 7 (High/Critical)
    failBuildOnCVSS = 7.0f

    // Ausgabeformate
    formats = listOf("HTML", "JSON", "JUNIT")

    // Falsch-Positive unterdrücken (mit Begründung!)
    suppressionFile = "dependency-check-suppressions.xml"

    // Datenbank lokal cachen (CI-Performance)
    data.directory = "${System.getenv("HOME")}/.dependency-check-data"
}
```

```xml
<!-- dependency-check-suppressions.xml -->
<!-- Jede Suppression braucht Begründung und Ablaufdatum -->
<suppressions>
    <suppress until="2024-06-01Z">
        <notes>
            CVE-2023-1234: Betrifft nur Feature X das wir nicht nutzen.
            Issue: https://github.com/org/repo/issues/456
        </notes>
        <packageUrl regex="true">^pkg:maven/com\.example/legacy\-lib@.*$</packageUrl>
        <cve>CVE-2023-1234</cve>
    </suppress>
</suppressions>
```

---

## Dependency-Update-Strategie mit Renovate Bot

```json
// renovate.json — automatische Dependency-Updates via PR
{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": ["config:base"],

  "schedule": ["every weekend"],

  "packageRules": [
    {
      "matchPackagePatterns": ["^org.springframework"],
      "groupName": "Spring Boot",
      "automerge": false    // Spring-Updates: manuelles Review
    },
    {
      "matchDepTypes": ["devDependencies"],
      "matchUpdateTypes": ["minor", "patch"],
      "automerge": true     // Dev-Dependencies: auto-merge wenn CI grün
    },
    {
      "matchUpdateTypes": ["major"],
      "addLabels": ["major-update"],
      "automerge": false    // Major-Updates: immer manuell
    }
  ],

  "vulnerabilityAlerts": {
    "enabled": true,        // CVE-Alerts sofort, außerhalb des Schedules
    "schedule": ["at any time"]
  }
}
```

---

## Lizenz-Compliance prüfen

```kotlin
// build.gradle.kts — Lizenz-Audit
plugins {
    id("com.github.jk1.dependency-license-report") version "2.7"
}

licenseReport {
    // Erlaubte Lizenzen (Liste anpassen je nach Lizenzpolitik)
    allowedLicensesFile = file("allowed-licenses.json")
    excludes = ["com.example:internal-lib"]
}
```

```json
// allowed-licenses.json
{
  "allowedLicenses": [
    { "moduleLicense": "Apache-2.0" },
    { "moduleLicense": "MIT" },
    { "moduleLicense": "BSD-2-Clause" },
    { "moduleLicense": "BSD-3-Clause" },
    { "moduleLicense": "EPL-2.0" }
  ]
  // NICHT erlaubt ohne Legal-Review:
  // GPL-2.0, GPL-3.0, AGPL — Copyleft!
  // LGPL: erlaubt wenn dynamisch gelinkt
}
```

---

## CI-Integration: vollständige Supply-Chain-Prüfung

```yaml
# .github/workflows/security.yml
name: Supply Chain Security

on:
  push:
    branches: [main]
  schedule:
    - cron: '0 6 * * 1'  # Montags 6 Uhr: wöchentlicher Full-Scan

jobs:
  dependency-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: OWASP Dependency Check
        run: ./gradlew dependencyCheckAnalyze
        env:
          NVD_API_KEY: ${{ secrets.NVD_API_KEY }}

      - name: Generate SBOM
        run: ./gradlew cyclonedxBom

      - name: License Check
        run: ./gradlew checkLicense

      - name: Upload SBOM
        uses: actions/upload-artifact@v4
        with:
          name: sbom-${{ github.sha }}
          path: build/reports/bom.json
          retention-days: 90  # SBOM für 90 Tage aufbewahren

      - name: SBOM to GitHub Dependency Graph
        uses: anchore/sbom-action@v0
        with:
          artifact-name: sbom.spdx.json
          format: spdx-json

  container-scan:
    runs-on: ubuntu-latest
    needs: [build-docker]
    steps:
      - name: Scan Container Image
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ghcr.io/${{ github.repository }}:${{ github.sha }}
          format: 'sarif'
          exit-code: '1'           # Fehlschlag bei Critical/High
          severity: 'CRITICAL,HIGH'
          output: 'trivy-results.sarif'

      - name: Upload Trivy Results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-results.sarif'
```

---

## Incident Response: CVE in Produktion

```markdown
## CVE Response Playbook

### Sofortmaßnahme (0-4 Stunden)
1. SBOM abfragen: `grep "log4j" build/reports/bom.json`
2. Betroffene Services identifizieren
3. CVSS Score und Exploitability prüfen (nvd.nist.gov)
4. Bei CVSS ≥ 9 (Critical): Sofort-Patch oder Mitigation

### Kurzfristig (1-3 Tage)
1. Renovate Bot / Dependabot PR reviewen und mergen
2. Falls kein Patch: Mitigation (WAF-Regel, Feature deaktivieren)
3. Suppression in dependency-check-suppressions.xml wenn kein Patch verfügbar

### Nachbereitung
1. Post-Mortem: Wie lange war die Vulnerability bekannt?
2. Prozess verbessern: Scanner-Frequency erhöhen?
```

---

## Konsequenzen

**Positiv:** Bei CVE sofort wissen ob betroffen. Lizenz-Compliance automatisch. Renovate Bot hält Dependencies aktuell ohne manuelle Arbeit. SBOM als nachweisbares Sicherheitsartefakt.

**Negativ:** NVD-Datenbank-Updates dauern (false negatives in den ersten 24h). Falsch-Positive erfordern Pflege der Suppressions. Renovate PRs müssen gereviewt werden — Aufwand.

---

## 💡 Guru-Tipps

- **SBOM in Deployment-Artefakt einbetten**: `docker build --label "sbom=$(cat bom.json)"` — Image und SBOM zusammen.
- **Dependency-Pinning vs. Ranges**: `"3.3.0"` statt `"3.3.+"` — reproduzierbare Builds, Renovate Bot übernimmt Updates.
- **NVD API Key kostenlos**: https://nvd.nist.gov/developers/request-an-api-key — dramatisch schnellere Scans.
- **GitHub Advanced Security**: integriertes Dependency-Scanning ohne eigene Infrastruktur.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP-Prinzipien der Anwendungssicherheit.
- [ADR-036](ADR-036-devops-cicd.md) — CVE-Scanning in der CI-Pipeline.
- [ADR-037](ADR-037-docker-container.md) — Container-Image-Scanning mit Trivy.
