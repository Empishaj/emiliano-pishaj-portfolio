# ADR-064 — Changelog, Release Notes & Semantische Versionierung

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Gradle 8.x                   |
| Datum      | 2024-01-01                        |
| Kategorie  | Dokumentation / DevOps            |

---

## Kontext & Problem

Ohne Changelog weiß niemand was sich zwischen Versionen geändert hat. Consumer einer API sehen `2.1.0 → 2.2.0` und wissen nicht ob sie upgraden können. Entwickler können nach einem Incident nicht rekonstruieren was deployed wurde. Semantische Versionierung und strukturierte Changelogs lösen diese Probleme.

---

## Semantische Versionierung (SemVer)

```
Format: MAJOR.MINOR.PATCH

MAJOR: Breaking Change — API-Konsumenten müssen Code anpassen
  → Endpoint entfernt, Parameter-Typ geändert, Auth-Anforderung verschärft
  → Beispiel: 1.5.3 → 2.0.0

MINOR: Neue rückwärtskompatible Funktionalität
  → Neuer optionaler Endpoint, neues optionales Feld in Response
  → Beispiel: 1.5.3 → 1.6.0

PATCH: Bug Fix, Sicherheits-Patch, Performance
  → Internes Verhalten korrigiert, kein API-Änderung
  → Beispiel: 1.5.3 → 1.5.4

Pre-Release:  1.0.0-alpha.1, 1.0.0-rc.1
Build-Meta:   1.0.0+build.123 (ignoriert bei Versionierung)
```

---

## CHANGELOG.md Format (Keep a Changelog)

```markdown
# Changelog

Alle wichtigen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.1.0/)
und dieses Projekt folgt [Semantic Versioning](https://semver.org/lang/de/).

---

## [Unreleased]
### Added
- Neue Such-API für Produkte mit Elasticsearch-Backend (#234)

### Changed  
- Verbesserte Fehlerbehandlung bei Zahlungsfehlern — detailliertere Fehlermeldungen

---

## [2.3.0] — 2024-03-15

### Added
- `POST /api/v2/orders/{id}/returns` — Rückgabe-Endpunkt (#287)
- Support für österreichischen MwSt-Satz (20%) bei AT-Lieferadressen (#301)
- Prometheus-Metriken für Checkout-Dauer (Histogram)

### Changed
- `GET /api/v1/orders` — Pagination-Parameter: `page` statt `offset` (rückwärtskompatibel)
- Verbesserte P95-Latenz der Produkt-Suche: 450ms → 180ms durch Elasticsearch-Caching

### Fixed
- #289: Bestellstatus wurde bei gleichzeitigen Updates inkorrekt gesetzt (Race Condition)
- #295: UTF-8-Sonderzeichen in Produktnamen führten zu 500-Fehler

### Security
- CVE-2024-1234: Spring Security auf 6.2.2 aktualisiert

---

## [2.2.0] — 2024-02-01

### Added
- Virtual Thread Support (Spring Boot 3.2, Java 21)
- Canary Deployment via Argo Rollouts (#256)

### Deprecated
- `GET /api/v1/products/search?q=` — wird in v3.0.0 entfernt; `POST /api/v2/products/search` verwenden

### Security
- Passwort-Reset-Flow: zusätzliche Rate-Limitation (5 Versuche/Stunde)

---

## [2.0.0] — 2024-01-15  ← Breaking Change

### ⚠️ Breaking Changes
- `GET /api/v1/orders` gibt jetzt Objekt `{ content: [...], page: {...} }` statt Array
- Auth: OAuth2/OIDC ist jetzt Pflicht — API-Key-Auth entfernt
- `OrderDto.customerId` umbenannt zu `OrderDto.userId` (Feldname geändert)

### Migration Guide
Von 1.x auf 2.0 migrieren:
1. OAuth2-Token statt API-Key verwenden → [Auth Docs](https://docs.example.com/auth)
2. Pagination-Wrapper im Response-Parsing berücksichtigen
3. `customerId` → `userId` umbenennen in Client-Code

[...]
```

---

## Conventional Commits: automatischer Changelog

```
Commit-Format: <type>(<scope>): <description>

Types:
  feat:     Neue Funktion → MINOR-Increment
  fix:      Bug Fix → PATCH-Increment
  breaking: Breaking Change → MAJOR-Increment (auch: feat! fix!)
  docs:     Dokumentation
  refactor: Refactoring ohne Verhalten-Änderung
  perf:     Performance-Verbesserung
  test:     Tests hinzugefügt
  ci:       CI/CD-Änderungen
  chore:    Build, Abhängigkeiten

Beispiele:
  feat(orders): add return endpoint for order items
  fix(checkout): prevent race condition on concurrent order updates
  feat!: replace API key auth with OAuth2/OIDC
  fix(security): update Spring Security to 6.2.2 (CVE-2024-1234)
  docs(api): add migration guide for 2.0.0 breaking changes
```

```yaml
# .github/workflows/release.yml — automatischer Release
name: Release

on:
  push:
    branches: [main]

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      # semantic-release: liest Commits, berechnet Version, generiert Changelog
      - name: Semantic Release
        uses: semantic-release/semantic-release@v23
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          branches: [main]
          plugins:
            - "@semantic-release/commit-analyzer"
            - "@semantic-release/release-notes-generator"
            - "@semantic-release/changelog"
            - "@semantic-release/github"
```

---

## Gradle Version Management

```kotlin
// build.gradle.kts — Version aus Git-Tag
version = System.getenv("RELEASE_VERSION")
    ?: runCatching { "git describe --tags --abbrev=0".run() }.getOrDefault("0.0.0-SNAPSHOT")

// Oder: Git-Tag als Versionierungsquelle
plugins {
    id("com.palantir.git-version") version "3.0.0"
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
// Ergibt: "1.2.3" wenn auf Tag, "1.2.3-3-g1234abc" wenn 3 Commits nach Tag
```

---

## /actuator/info: Version im Healthcheck

```yaml
# application.yml
info:
  app:
    name:    "@project.name@"
    version: "@project.version@"
    description: "@project.description@"
  build:
    time: "@maven.build.timestamp@"
  git:
    commit: "@git.commit.id.abbrev@"
    branch: "@git.branch@"
```

```json
// GET /actuator/info
{
  "app": {
    "name": "order-service",
    "version": "2.3.0",
    "description": "Order Management Service"
  },
  "build": {
    "time": "2024-03-15T10:30:00Z"
  },
  "git": {
    "commit": "abc1234",
    "branch": "main"
  }
}
// → Sofort sichtbar welche Version in Produktion läuft
```

---

## Konsequenzen

**Positiv:** Semantic Release automatisiert Versionierung und Changelog vollständig. Conventional Commits machen jeden Commit zum Changelog-Eintrag. `/actuator/info` macht deployed Version jederzeit prüfbar.

**Negativ:** Conventional Commit-Disziplin im Team nötig — ein schlechter Commit-Message blockiert automatische Version-Berechnung nicht, verschlechtert aber den Changelog. Breaking Changes müssen bewusst als `feat!` oder `BREAKING CHANGE:` markiert werden.

---

## 💡 Guru-Tipps

- **Commitlint**: prüft Commit-Messages gegen Conventional Commits Format im pre-commit Hook.
- **`[Unreleased]`-Block** immer pflegen: alle Änderungen seit letztem Release dokumentieren.
- **Changelog ist für Menschen**: Release Notes für Entwickler, vereinfachter Blog-Post für Endnutzer.
- **Git-Tags signieren**: `git tag -s v2.3.0` — kryptographisch verifizierbare Releases.

---

## Verwandte ADRs

- [ADR-036](ADR-036-devops-cicd.md) — Semantic Release in der CI-Pipeline.
- [ADR-021](ADR-021-rest-api-design.md) — SemVer bestimmt API-Versionierung.
