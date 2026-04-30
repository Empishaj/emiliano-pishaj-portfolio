# ADR-049 — Code Review & Pull Request Prozess

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21                                |
| Datum      | 2024-01-01                        |
| Kategorie  | Team-Kultur / Prozess             |

---

## Kontext & Problem

Code Review ist das wichtigste Qualitätswerkzeug das ein Team hat — wichtiger als jedes automatische Tool, weil nur ein Mensch Designentscheidungen, Namensgebung und Businesslogik-Korrektheit beurteilen kann. Schlecht durchgeführte Reviews sind entweder bedeutungslos (Rubber-Stamp) oder toxisch (persönliche Kritik statt Code-Kritik).

---

## Regel 1 — PR-Größe: Klein und fokussiert

```
❌ 1 PR mit 50 geänderten Dateien und 3 verschiedenen Features
✅ 3 PRs: je 1 Feature, < 400 geänderte Zeilen

Faustregel:
- < 200 Zeilen:  reviewbar in 15 Minuten — optimal
- 200–400 Zeilen: reviewbar in 30 Minuten
- > 400 Zeilen:  aufteilen! Reviewer werden oberflächlich.

Ausnahmen: Refactoring-PRs können größer sein wenn KEINE Verhaltensänderung.
```

---

## Regel 2 — PR-Beschreibung: Kontext, nicht Zusammenfassung

```markdown
<!-- ❌ Schlechte PR-Beschreibung -->
# Fix order calculation

Fixed the bug.

---

<!-- ✅ Gute PR-Beschreibung -->
# feat(orders): Steuerberechnung nach österreichischem Umsatzsteuergesetz

## Problem
Österreichische Bestellungen wurden bisher mit deutschem MwSt-Satz (19%)
berechnet. Für AT gilt 20% (§ 10 UStG).

## Lösung
- `TaxCalculationStrategy`-Interface eingeführt (→ ADR-025 OCP)
- `AustrianTaxStrategy` und `GermanTaxStrategy` implementiert
- Land aus Lieferadresse bestimmt die Strategie (via Spring @Qualifier)

## Wie testen
1. Bestellung mit AT-Lieferadresse aufgeben
2. Total-Betrag prüfen: Netto × 1.20 (nicht × 1.19)

## Checklist
- [x] Unit-Tests für beide Strategien
- [x] Integrationstest mit AT-Adresse
- [x] JavaDoc auf TaxCalculationStrategy
- [x] ADR-025 Querverweise gesetzt

## Verbundene Issues
Closes #456
```

---

## Regel 3 — Review-Fokus: Was wird geprüft?

```
Priorität 1 — Korrektheit (muss)
✓ Ist die Logik korrekt?
✓ Werden alle Fehlerfälle behandelt?
✓ Race Conditions, Thread Safety (→ ADR-033)?
✓ Sicherheit: SQL Injection, Auth (→ ADR-015)?
✓ Korrekte Transaktionsgrenzen (→ ADR-006)?

Priorität 2 — Design (muss)
✓ SOLID-Verletzungen (→ ADR-025)?
✓ Richtige Abstraktionsebene?
✓ Code Smells (→ ADR-027)?
✓ Testbarkeit: ist alles testbar?
✓ Namensgebung: versteht man sofort was gemeint ist?

Priorität 3 — Vollständigkeit (muss)
✓ Tests vorhanden und sinnvoll?
✓ Fehlerfälle getestet?
✓ JavaDoc für public APIs (→ ADR-048)?
✓ ADR-Referenzen wenn Architekturentscheidung getroffen (→ ADR-009)?

Priorität 4 — Stil (sollte)
✓ Formatierung konsistent (automatisch via Formatter)?
✓ Keine unnötigen Kommentare / Dead Code?
```

---

## Regel 4 — Review-Kommentare: konstruktiv und präzise

```markdown
<!-- ❌ Schlechter Kommentar: persönlich, unspezifisch -->
Das ist schlecht. Bitte nochmal machen.

<!-- ❌ Schlechter Kommentar: Frage ohne Vorschlag -->
Warum machst du das so?

<!-- ✅ Guter Kommentar: erklärt Problem + Vorschlag + Begründung -->
Hier entsteht ein N+1-Problem (→ ADR-016): für jede Bestellung wird
die Customer-Entity separat geladen. Vorschlag:

```java
// Statt:
orders.stream().map(o -> o.customer().name())

// Besser: JOIN FETCH in der Query
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer WHERE ...")
```

Das reduziert 100 Queries auf 1 bei 100 Bestellungen.
```

```markdown
<!-- Prefix-Konvention für Dringlichkeit -->
[blocker]  Diese Änderung ist zwingend — PR kann nicht gemerged werden.
[major]    Sollte geändert werden — hohes Risiko oder schlechtes Design.
[minor]    Verbesserungsvorschlag — nicht blockierend, Autors Entscheidung.
[nit]      Kleinigkeit (Formatierung, Benennung) — rein informativ.
[question] Frage zum Verständnis — kein Action Item.
[praise]   Positives Feedback — auch Lob gehört in Reviews!
```

---

## Regel 5 — Review-Checkliste als PR-Template

```markdown
<!-- .github/PULL_REQUEST_TEMPLATE.md -->
## Beschreibung
<!-- Was ändert dieser PR? Warum? -->

## Typ der Änderung
- [ ] Bug Fix
- [ ] Feature
- [ ] Refactoring (kein Verhaltensänderung)
- [ ] Performance-Verbesserung
- [ ] Dokumentation

## Reviewer-Checkliste
- [ ] Korrektheit: Logik korrekt, alle Fehlerfälle behandelt
- [ ] Tests: Unit-Tests, Integrationstests wo sinnvoll
- [ ] Sicherheit: Keine Injection, Auth-Checks korrekt
- [ ] Performance: Keine N+1, kein unnötiger DB-Zugriff
- [ ] Design: SOLID, keine offensichtlichen Code Smells
- [ ] Breaking Changes: API rückwärtskompatibel oder neue Version
- [ ] Dokumentation: JavaDoc für neue public APIs

## Verbundene Issues
Closes #
```

---

## Regel 6 — Branch Protection Regeln (GitHub)

```yaml
# Erzwungen via GitHub Branch Protection:
# Settings → Branches → Protect matching branches

require-pull-request-reviews:
  required-approving-review-count: 1    # Mindestens 1 Approval
  dismiss-stale-reviews: true           # Neuer Commit invalidiert alte Approvals
  require-code-owner-reviews: true      # CODEOWNERS müssen reviewen

require-status-checks-to-pass:          # Alle CI-Checks müssen grün sein
  - build
  - test
  - code-quality
  - security-scan

restrictions:
  push-access:                          # Direkter Push auf main verboten
    - [] # niemand darf direkt pushen
```

---

## Konsequenzen

**Positiv:** Kleine PRs werden schnell reviewed — weniger Kontext-Wechsel. Konstruktive Kommentare verbessern die Codequalität ohne das Team zu demotivieren. Branch Protection verhindert unreviewed Code in main.

**Negativ:** Review-Prozess verlangsamt Deployment wenn PRs groß werden — deshalb: Disziplin bei PR-Größe. Reviewer-Kapazität ist ein Engpass — Review-Rotation einplanen.

---

## 💡 Guru-Tipps

- **Boy Scout Rule im Review**: "War es besser oder schlechter als vor diesem PR?" — das ist die Messlatte.
- **Synchrones Code Review** für komplexe Änderungen: 30 Minuten paired Review erspart asynchrones Ping-Pong.
- **`CODEOWNERS`-Datei**: definiert wer bei Änderungen an bestimmten Pfaden automatisch als Reviewer zugewiesen wird.
- **Lob explizit machen**: `[praise] Elegante Lösung mit dem Strategy Pattern!` — gutes Design soll positiv verstärkt werden.

---

## Verwandte ADRs

- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ADR-Referenzen sind Review-Pflicht.
- [ADR-036](ADR-036-devops-cicd.md) — CI-Pipeline muss grün sein bevor Merge erlaubt.
