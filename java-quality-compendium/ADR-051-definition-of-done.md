# ADR-051 — Definition of Done & Technische Schulden

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21                                |
| Datum      | 2024-01-01                        |
| Kategorie  | Team-Kultur / Prozess             |

---

## Kontext & Problem

"Fertig" ohne Definition ist bedeutungslos. Ein Feature das funktioniert aber keine Tests hat, keine JavaDoc, keine Sicherheits-Prüfung und unbegrenzten Speicher verbraucht — ist es "fertig"? Die Definition of Done macht explizit was "fertig" bedeutet. Technische Schulden entstehen wenn bekannte Abkürzungen bewusst oder unbewusst gemacht werden.

---

## Definition of Done (DoD) — Checkliste

```markdown
## Code-Qualität
- [ ] Code Review abgeschlossen, alle [blocker] und [major] Kommentare adressiert
- [ ] Keine neuen SonarQube-Findings (Bug, Vulnerability, Security Hotspot)
- [ ] Keine neuen Code-Smells die gegen ADR-027 verstoßen
- [ ] SOLID-Prinzipien nicht verletzt (→ ADR-025)
- [ ] Formatierung: `./gradlew checkstyleMain` ohne Fehler

## Tests
- [ ] Unit-Tests für alle neuen/geänderten Klassen (→ ADR-010)
- [ ] Grenzwerte getestet (→ ADR-014)
- [ ] Fehlerfälle getestet (alle @throws-Szenarien)
- [ ] Integrations-/Slice-Tests für neue Endpunkte (→ ADR-020)
- [ ] Code-Coverage ≥ 80% für neuen Code
- [ ] Mutation Score ≥ 80% (→ ADR-045, bei kritischer Logik)
- [ ] Alle bestehenden Tests grün

## Sicherheit
- [ ] Input-Validierung für alle neuen Endpunkte (→ ADR-015)
- [ ] Kein PII im Log (→ ADR-017, ADR-035)
- [ ] Keine neuen OWASP-Dependency-Check Findings (CVSS ≥ 7)
- [ ] Auth-Checks auf neuen Endpunkten (@PreAuthorize)

## Persistenz
- [ ] DB-Änderungen als Flyway-Migration (→ ADR-034)
- [ ] Keine N+1-Queries (→ ADR-016)
- [ ] Transaktionsgrenzen korrekt (→ ADR-006)

## Observability
- [ ] Relevante Business-Events geloggt (→ ADR-017)
- [ ] Metriken für neue Operationen (Counter/Timer)
- [ ] Fehler mit vollständigem Stack-Trace geloggt

## Dokumentation
- [ ] JavaDoc für neue public APIs (→ ADR-048)
- [ ] ADR erstellt/aktualisiert wenn Architekturentscheidung getroffen (→ ADR-009)
- [ ] CHANGELOG.md aktualisiert
- [ ] C4-Diagramm aktualisiert wenn neue Komponente/Abhängigkeit (→ ADR-047)

## Deployment
- [ ] Feature Flag wenn nötig (→ ADR-039)
- [ ] Zero-Downtime-Migration sichergestellt
- [ ] Konfiguration in application.yml dokumentiert
- [ ] CI/CD Pipeline grün (→ ADR-036)
```

---

## Technische Schulden: transparent machen

```java
// ❌ Schlecht: Schulden versteckt im Code
public List<Order> findAll() {
    return orderRepository.findAll(); // TODO: Pagination fehlt noch
    // Niemand sieht dieses TODO in der Übersicht
}

// ✅ Gut: Schulden mit ADR-Referenz und Issue-Link dokumentiert
// TECH_DEBT(#234, ADR-016): Diese Methode lädt alle Orders ohne Pagination.
// Bei > 10.000 Orders führt das zu OOM.
// Geplante Behebung: Sprint 48.
// Workaround: Produktionsdatenbank hat aktuell max 5.000 Orders.
public List<Order> findAll() {
    return orderRepository.findAll();
}
```

---

## Technische Schulden kategorisieren

```
Kategorie A: Sicherheitsrisiko — sofort beheben (kein Sprint-Planning nötig)
  → SQL Injection, fehlende Auth, CVE in Dependency

Kategorie B: Datenintegrität — in aktuellem oder nächstem Sprint
  → Fehlende Transaktion, Race Condition, N+1

Kategorie C: Wartbarkeit — priorisiert im Backlog
  → Fehlende Tests, schlechte Benennung, God Class
  → SonarQube Technical Debt Report als Grundlage

Kategorie D: Performance — bei Bedarf
  → Langsame Queries die aktuell nicht schmerzen
  → Dokumentiert mit Schwellwert: "Beheben wenn > 1s P95"
```

---

## Tech-Debt-Board im Team

```markdown
# Tech Debt Register (Confluence / GitHub Issues mit Label tech-debt)

| ID   | Kategorie | Beschreibung                          | Aufwand | Priorität | Sprint |
|------|-----------|---------------------------------------|---------|-----------|--------|
| #234 | B         | OrderService: Pagination fehlt        | 3 SP    | Hoch      | 48     |
| #189 | C         | UserService: God Class aufteilen      | 8 SP    | Mittel    | 50     |
| #301 | D         | Produkt-Suche: ElasticSearch statt DB | 13 SP   | Niedrig   | —      |

Regel: 20% der Sprint-Kapazität für Tech-Debt reservieren.
```

---

## Boy Scout Rule als Teamregel

```
"Hinterlasse den Code sauberer als du ihn vorgefunden hast."

Konkret:
- Jeder PR verbessert mindestens einen kleinen Aspekt des umgebenden Codes
- Nicht-funktionale Verbesserungen (Umbenennen, Test hinzufügen) sind willkommen
- Kein Sprinklen von TODO-Kommentaren ohne Issue-Nummer
- Kein "das war schon so" als Begründung

Aber: kein "Opportunistic Refactoring" der alle Review-Kapazität bindet.
Große Refactorings → eigener PR mit eigenem Review.
```

---

## Konsequenzen

**Positiv:** DoD verhindert "90% fertig für immer". Tech-Debt-Register macht Schulden sichtbar und planbar statt versteckt und unkontrolliert wachsend. Boy Scout Rule hält die Codebase langsam aber stetig sauber.

**Negativ:** DoD-Checkliste kann als bürokratisch empfunden werden — Team-Buy-In nötig. 20% Tech-Debt-Kapazität muss gegenüber Product Management kommuniziert werden.

---

## Tipps

- **DoD sichtbar machen**: Checkliste als PR-Template (→ ADR-049) — nicht im Kopf.
- **"Definition of Ready"** ergänzt DoD: Was muss ein Ticket haben bevor es in den Sprint kommt? (Akzeptanzkriterien, Designs, ADR-Referenzen).
- **SonarQube Quality Gate** als technischer DoD-Enforcer: PR kann nicht gemerged werden wenn Quality Gate rot.
- **Tech Debt ist kein Fehler** — manchmal die richtige Entscheidung. Aber: bewusst, dokumentiert, mit Rückzahlungsplan.
 