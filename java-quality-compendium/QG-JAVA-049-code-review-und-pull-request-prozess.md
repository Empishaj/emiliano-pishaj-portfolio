# QG-JAVA-049 — Code Review und Pull-Request-Prozess

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-049 |
| Titel | Code Review und Pull-Request-Prozess |
| Status | Accepted / verbindlicher Standard für Pull Requests und Code Reviews |
| Version | 1.0.0 |
| Datum | 2024-04-23 |
| Review-Datum | 2026-05-01 |
| Kategorie | Engineering-Prozess / Qualitätssicherung / Teamarbeit |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur, Engineering Management |
| Java-Baseline | Java 21 |
| Plattform-Kontext | GitHub, GitLab oder vergleichbare Pull-/Merge-Request-Plattformen |
| Geltungsbereich | Alle Codeänderungen an produktiven Services, Tests, Build-Konfiguration, Infrastruktur-Code, API-Verträgen, Datenbankmigrationen und sicherheitsrelevanten Artefakten |
| Verbindlichkeit | Jede produktive Änderung erfolgt über Pull Request oder Merge Request. Direkte Pushes auf geschützte Hauptbranches sind nicht zulässig. |
| Technische Validierung | Gegen GitHub Protected Branches, Pull Request Reviews, CODEOWNERS, Google Engineering Review-Prinzipien und OWASP Secure Code Review eingeordnet |
| Kurzentscheidung | Code Review ist ein verbindlicher Qualitäts-, Lern- und Risikokontrollprozess. Ein Review prüft Korrektheit, Sicherheit, Design, Tests, Wartbarkeit, Betriebsfähigkeit und Verständlichkeit. |

---

## 1. Zweck

Diese Richtlinie definiert, wie Pull Requests und Code Reviews in Java-Projekten durchgeführt werden.

Code Review ist nicht nur ein formales Approval. Es ist eines der wichtigsten Qualitätswerkzeuge eines Engineering-Teams, weil automatische Tools zwar Formatierung, Tests, Linting, Coverage und bekannte Schwachstellen prüfen können, aber nicht zuverlässig erkennen, ob eine fachliche Regel korrekt verstanden wurde, ob eine Abstraktion angemessen ist, ob eine Änderung spätere Wartung erschwert oder ob ein Sicherheitsmodell wirklich sauber umgesetzt ist.

Ein gutes Review schützt vor:

- fachlichen Fehlern,
- Sicherheitslücken,
- regressionsanfälligem Design,
- unklarer Namensgebung,
- fehlenden Tests,
- Performance-Problemen,
- inkonsistenten API-Verträgen,
- unkontrollierten Datenbankänderungen,
- Architekturdrift,
- unwartbarer Komplexität,
- stillen Betriebsrisiken.

Ein schlechtes Review ist entweder ein Rubber Stamp oder eine persönliche Auseinandersetzung. Beides ist schädlich. Diese Richtlinie macht Reviews konkret, prüfbar und konstruktiv.

---

## 2. Kurzregel

Jede nicht-triviale Änderung wird über einen kleinen, fokussierten Pull Request eingebracht. Der Pull Request erklärt Problem, Lösung, Tests, Risiken und betroffene Qualitätsrichtlinien. Reviewer prüfen zuerst Korrektheit, Sicherheit, Design, Tests und Betriebsfähigkeit; Stilfragen werden automatisiert oder nachrangig behandelt. Kommentare sind präzise, begründet und mit Schweregrad markiert. Merges erfolgen nur nach erfolgreicher CI, erforderlichen Approvals und Einhaltung der Branch-Protection-Regeln.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Java-Produktionscode,
- Tests,
- Datenbankmigrationen,
- Build-Dateien,
- Dockerfiles,
- Kubernetes-/Helm-/Terraform-Dateien,
- GitHub-/GitLab-CI-Konfiguration,
- API-Verträge,
- Kafka-/Event-Schemas,
- Security-Konfiguration,
- Observability-Konfiguration,
- Feature-Flag-Konfiguration,
- Architektur- und Qualitätsrichtlinien.

Ausgenommen sind nur rein redaktionelle Änderungen ohne technische Wirkung, zum Beispiel Tippfehler in nicht-verbindlichen Dokumenten. Sobald eine Änderung Verhalten, Build, Deployment, Security, Datenmodell oder Schnittstellen betrifft, gilt diese Richtlinie.

---

## 4. Technischer Hintergrund

Pull Requests und Merge Requests sind technische Kontrollpunkte vor dem Merge in den Hauptbranch. Plattformen wie GitHub unterstützen dafür Review-Approvals, required status checks, CODEOWNERS, protected branches und Regeln, die direkte Pushes oder Merges ohne erfüllte Bedingungen verhindern.

GitHub dokumentiert Branch Protection Rules als Mechanismus, um bestimmte Workflows auf Branches zu erzwingen, etwa Pull-Request-Reviews, Status Checks oder Einschränkungen für Pushes. CODEOWNERS erlaubt, Verantwortliche für Pfade zu definieren, die automatisch als Reviewer angefordert werden können. Diese Mechanismen ersetzen kein gutes Review, aber sie verhindern, dass Qualitätsregeln nur freiwillige Absicht bleiben.

Der Prozess ist damit ein Engineering-Control: Er verbindet menschliche Bewertung mit automatisierten Gates.

---

## 5. Verbindlicher Standard

Für jeden Pull Request gelten folgende Mindestregeln:

1. Der Pull Request ist klein und fokussiert.
2. Der Pull Request beschreibt Problem, Lösung, Testnachweis und Risiken.
3. Jeder Pull Request hat mindestens einen qualifizierten Reviewer.
4. Sicherheits-, Architektur- oder Datenbankänderungen benötigen passende Fachexpertise.
5. CI muss erfolgreich sein.
6. Tests müssen angepasst oder ergänzt werden, wenn Verhalten geändert wird.
7. Direkter Push auf `main`, `master`, `develop` oder geschützte Release-Branches ist verboten.
8. Reviewer-Kommentare werden mit Schweregrad markiert.
9. Blockierende Kommentare müssen vor Merge gelöst werden.
10. Rubber-Stamp-Reviews sind nicht zulässig.
11. Persönliche Kritik ist nicht zulässig; bewertet wird Code, Design und Risiko.
12. Relevante Quality Guidelines werden im Review referenziert.
13. Größere technische Entscheidungen brauchen eine dokumentierte Entscheidung oder ein separates Architekturartefakt.
14. Merges trotz roter CI sind nur als dokumentierte Ausnahme mit Verantwortlichem erlaubt.

---

## 6. Pull-Request-Größe

### 6.1 Grundsatz

Ein Pull Request muss so klein sein, dass ein Reviewer ihn konzentriert und vollständig prüfen kann.

Richtwerte:

| PR-Größe | Details/Erklärung | Bewertung |
|---|---|---|
| bis 200 geänderte Zeilen | Sehr gut reviewbar, ideal für normale Änderungen | Zielgröße |
| 200–400 geänderte Zeilen | Noch reviewbar, wenn fokussiert und gut beschrieben | Akzeptabel |
| 400–800 geänderte Zeilen | Nur mit klarer Begründung; Aufteilung prüfen | Kritisch |
| über 800 geänderte Zeilen | Normalerweise aufzuteilen | Ausnahme |
| große generierte Dateien | Separat kennzeichnen oder getrennt einbringen | Nicht mit Fachlogik mischen |
| reine Formatierung | Separater PR ohne Verhaltensänderung | Erlaubt |
| Refactoring ohne Verhalten | Separater PR, Tests müssen grün bleiben | Erlaubt |

Die Zeilenzahl ist keine starre Regel. Entscheidend ist Reviewbarkeit. Ein PR mit 250 Zeilen schwieriger Geschäftslogik kann zu groß sein. Ein PR mit 900 Zeilen generierter Avro-Klassen kann unkritisch sein, wenn die Quelle separat geprüft wird.

### 6.2 Schlechte Anwendung

```text
Ein PR enthält:
- neues Checkout-Feature,
- Datenbankmigration,
- Refactoring der Tax-Engine,
- neues Kafka-Event,
- Formatierung von 40 Dateien,
- Dependency-Upgrade,
- Bugfix für Login.
```

Dieser PR ist nicht sinnvoll reviewbar. Reviewer können fachliche, technische und sicherheitsrelevante Risiken nicht sauber trennen.

### 6.3 Gute Anwendung

```text
PR 1: Refactoring ohne Verhalten — TaxCalculationStrategy extrahieren.
PR 2: Österreichische Steuerstrategie ergänzen.
PR 3: Datenbankmigration für Steuerland ergänzen.
PR 4: API-Feld dokumentieren und OpenAPI aktualisieren.
PR 5: Feature Flag aktivierbar machen.
```

Jeder PR hat ein klares Reviewziel.

---

## 7. Pull-Request-Beschreibung

### 7.1 Schlechte Beschreibung

```markdown
# Fix order calculation

Fixed bug.
```

Diese Beschreibung hilft nicht. Reviewer müssen den Kontext aus Code, Ticket und Annahmen rekonstruieren.

### 7.2 Gute Beschreibung

```markdown
# feat(orders): Steuerberechnung für österreichische Lieferadressen

## Problem

Bestellungen mit österreichischer Lieferadresse wurden bisher mit dem deutschen
Standardsteuersatz berechnet. Für AT-Bestellungen muss der österreichische
Standardsteuersatz angewendet werden.

## Lösung

- `TaxCalculationStrategy` eingeführt.
- `GermanTaxStrategy` und `AustrianTaxStrategy` implementiert.
- Strategieauswahl erfolgt über Lieferland.
- Bestehende DE-Logik bleibt unverändert.
- Tests für DE und AT ergänzt.

## Technische Auswirkungen

- Keine Datenbankmigration.
- Keine API-Änderung.
- Keine Änderung an bestehenden Events.
- Keine neue externe Abhängigkeit.

## Wie getestet

- Unit-Tests für beide Strategien.
- Service-Test für Strategieauswahl.
- Regressionstest für bestehende DE-Bestellungen.

## Risiken

- Falsche Steuerlogik hätte fachliche Auswirkung auf Rechnungsbeträge.
- Rundung wurde explizit mit `BigDecimal` geprüft.

## Verbundene Issues

Closes #456
```

### 7.3 Pflichtbestandteile

Jeder nicht-triviale PR enthält:

| Abschnitt | Details/Erklärung |
|---|---|
| Problem | Warum ist die Änderung nötig? |
| Lösung | Was wurde geändert? |
| Testnachweis | Welche Tests wurden ausgeführt oder ergänzt? |
| Risiken | Was kann brechen? |
| Auswirkungen | API, DB, Security, Performance, Betrieb, Observability |
| Rollback | Wie kann Änderung zurückgenommen werden, wenn relevant? |
| Issues | Ticket-/Issue-Referenz |
| Screenshots/Logs | Bei UI, API oder Betriebsverhalten, wenn sinnvoll |

---

## 8. Pull-Request-Template

Empfohlenes Template:

```markdown
## Beschreibung

<!-- Was ändert dieser PR und warum? -->

## Typ der Änderung

- [ ] Bugfix
- [ ] Feature
- [ ] Refactoring ohne Verhaltensänderung
- [ ] Performance-Verbesserung
- [ ] Security-Fix
- [ ] Datenbankmigration
- [ ] API-/Contract-Änderung
- [ ] Dokumentation
- [ ] Build-/CI-/Deployment-Änderung

## Testnachweis

- [ ] Unit-Tests ergänzt/angepasst
- [ ] Integrationstests ergänzt/angepasst
- [ ] Security-Tests ergänzt/angepasst
- [ ] Contract-Tests ergänzt/angepasst
- [ ] Manuell getestet
- [ ] Nicht erforderlich, Begründung:

## Qualitätsprüfung

- [ ] Korrektheit geprüft
- [ ] Fehlerfälle geprüft
- [ ] Security geprüft
- [ ] Performance geprüft
- [ ] Logging/Metriken/Tracing geprüft
- [ ] API-/Event-Kompatibilität geprüft
- [ ] Datenbankmigration geprüft
- [ ] Keine sensiblen Daten in Logs/Events/Fehlermeldungen

## Risiken und Rollback

<!-- Was ist das Hauptrisiko? Wie kann zurückgerollt werden? -->

## Verbundene Issues

Closes #
```

---

## 9. Review-Fokus

Reviewer prüfen in Prioritäten. Nicht alle Kommentare sind gleich wichtig.

### 9.1 Priorität 1: Korrektheit

Fragen:

- Ist die fachliche Logik korrekt?
- Sind Randfälle behandelt?
- Sind Fehlerfälle behandelt?
- Sind Null-, Leer-, Grenz- und Mehrfachfälle geprüft?
- Ist die Transaktion korrekt?
- Ist Nebenläufigkeit berücksichtigt?
- Sind Zeit, Zeitzonen und Rundung korrekt?
- Gibt es Datenverlust?
- Gibt es Race Conditions?
- Gibt es idempotente Verarbeitung, wenn nötig?

### 9.2 Priorität 2: Security

Fragen:

- Gibt es Input Validation?
- Gibt es Autorisierung auf Objekt- und Tenant-Ebene?
- Gibt es Mass-Assignment-Risiko?
- Gibt es SQL-/NoSQL-/Command-Injection?
- Werden Secrets geschützt?
- Werden personenbezogene Daten minimiert?
- Werden Tokens, Passwörter, IBANs oder E-Mails geloggt?
- Sind Actuator- oder Admin-Endpunkte geschützt?
- Sind Kafka-Events, Logs und Traces frei von sensitiven Daten?
- Ist Dependency-/CVE-Risiko geprüft?

### 9.3 Priorität 3: Design

Fragen:

- Hat die Klasse eine klare Verantwortung?
- Ist die Abstraktion gerechtfertigt?
- Ist der Code testbar?
- Sind Ports, Adapter, Services und Domain-Objekte sauber getrennt?
- Ist SOLID sinnvoll angewendet?
- Wurde YAGNI beachtet?
- Wurde DRY korrekt angewendet oder entstand eine falsche Abstraktion?
- Gibt es unnötige Kopplung?
- Sind Namen fachlich präzise?

### 9.4 Priorität 4: Tests

Fragen:

- Gibt es Tests für Happy Path?
- Gibt es Tests für Fehlerfälle?
- Gibt es Tests für Randfälle?
- Sind Unit-Tests ohne unnötigen Spring-Kontext geschrieben?
- Gibt es Integrationstests bei DB, Kafka, Redis oder externen Adaptern?
- Sind Contract-Tests betroffen?
- Sind Tests deterministisch?
- Werden keine echten personenbezogenen Daten genutzt?
- Sind Tests lesbar und wartbar?

### 9.5 Priorität 5: Betrieb

Fragen:

- Gibt es sinnvolles Logging?
- Gibt es Metriken für kritische Operationen?
- Sind Traces/Korrelation betroffen?
- Gibt es Rollback- oder Migrationsstrategie?
- Sind Feature Flags nötig?
- Ist die Änderung beobachtbar?
- Gibt es Runbook-Anpassungen?
- Wird ein Alert oder Dashboard benötigt?

### 9.6 Priorität 6: Stil

Stilfragen werden nachrangig behandelt und möglichst automatisiert.

Beispiele:

- Formatierung,
- Import-Reihenfolge,
- Whitespace,
- einfache Umbenennungen,
- kleine Ausdrucksverbesserungen.

Stil darf kein Ersatz für fachliches Review werden.

---

## 10. Review-Kommentare

### 10.1 Schlechte Kommentare

```text
Das ist schlecht.
```

```text
Warum so?
```

```text
Bitte sauber machen.
```

```text
Gefällt mir nicht.
```

Diese Kommentare sind nicht hilfreich, weil sie kein überprüfbares Problem und keinen Lösungsvorschlag enthalten.

### 10.2 Gute Kommentare

```text
[major] Hier entsteht ein N+1-Problem: Für jede Bestellung wird die Customer-Entity
separat geladen. Bitte einen Join-Fetch oder eine DTO-Projection verwenden.
Bei 100 Bestellungen erzeugt diese Stelle sonst 101 Queries.
```

```text
[blocker] Der Endpunkt prüft aktuell nur Authentifizierung, aber keine Objektberechtigung.
Ein Nutzer könnte durch Ändern der orderId fremde Bestellungen abrufen.
Bitte Tenant- und Ownership-Prüfung ergänzen.
```

```text
[minor] Der Methodenname `process` ist sehr allgemein. `reserveInventoryForOrder`
würde den fachlichen Zweck klarer ausdrücken.
```

```text
[question] Ist garantiert, dass dieses Event nur einmal verarbeitet wird?
Falls nicht, brauchen wir hier eine Idempotenzprüfung über eventId.
```

```text
[praise] Sehr gut: Die neue Steuerlogik ist über Strategien erweitert, ohne die
bestehende DE-Logik zu verändern. Die Tests machen den Unterschied klar.
```

---

## 11. Kommentar-Schweregrade

| Präfix | Bedeutung | Muss vor Merge gelöst werden? |
|---|---|---|
| `[blocker]` | Kritischer Fehler, Security-Risiko, Datenverlust, Build-/Testbruch, falsche Fachlogik | Ja |
| `[major]` | Hohes Risiko, schlechtes Design, fehlender Test, Performanceproblem | In der Regel ja |
| `[minor]` | Verbesserung mit begrenztem Risiko | Entscheidung Autor/Team |
| `[nit]` | Kleinigkeit, Lesbarkeit, Benennung, Stil | Nein, wenn nicht vereinbart |
| `[question]` | Verständnisfrage, kein automatisches Action Item | Klären |
| `[suggestion]` | Konkreter Verbesserungsvorschlag | Autor entscheidet, sofern nicht major/blocker |
| `[praise]` | Positives Feedback | Nein |

Regel: Ein Kommentar ohne Schweregrad ist mindestens als `[question]` oder `[minor]` zu verstehen. Für blockierende Kommentare muss der Reviewer das Präfix explizit setzen.

---

## 12. Reviewer-Verantwortung

Reviewer sind nicht nur Gatekeeper. Sie übernehmen Mitverantwortung für Qualität.

Reviewer müssen:

1. den PR-Kontext lesen,
2. CI-Ergebnis prüfen,
3. fachliche Risiken prüfen,
4. Security-Risiken prüfen,
5. Testabdeckung bewerten,
6. Architekturkonformität prüfen,
7. Kommentare präzise formulieren,
8. wichtige Änderungen erneut prüfen,
9. Approvals nicht leichtfertig geben,
10. bei Unsicherheit synchrones Gespräch vorschlagen.

Reviewer müssen nicht:

- jeden Formatierungsfehler manuell suchen,
- persönliche Vorlieben durchsetzen,
- eine alternative Komplettlösung schreiben,
- jeden PR allein absichern,
- Autorenschaft übernehmen.

---

## 13. Autor-Verantwortung

Der Autor eines PRs ist verantwortlich dafür, den Review einfach zu machen.

Autoren müssen:

1. kleine PRs erstellen,
2. Kontext liefern,
3. Tests ergänzen,
4. CI vor Review grün bekommen,
5. selbst vorreviewen,
6. offensichtliche Formatierungsfehler entfernen,
7. Risiken benennen,
8. auf Kommentare reagieren,
9. Änderungen nach Review sauber pushen,
10. bei Unklarheiten aktiv Klärung suchen.

Vor dem Review sollte der Autor die Diff selbst lesen. Viele Fehler werden durch ein eigenes Vorreview gefunden.

---

## 14. Approval-Regeln

### 14.1 Mindeststandard

```text
Normale Änderung: mindestens 1 Approval.
Kritische Änderung: mindestens 2 Approvals oder Code Owner Approval.
Security-relevante Änderung: Security-/Tech-Lead-Review erforderlich.
Datenbankmigration: Reviewer mit Persistenz-/DB-Kontext erforderlich.
API-/Event-Breaking-Change: Architektur-/Consumer-Review erforderlich.
```

### 14.2 Wann ein Approval nicht ausreichend ist

Ein Approval ist nicht ausreichend, wenn:

- CI rot ist,
- blocker-Kommentare offen sind,
- Tests fehlen,
- Security-Fragen offen sind,
- Branch Protection nicht erfüllt ist,
- Reviewer nicht den relevanten Codebereich beurteilen kann,
- ein großer PR nur oberflächlich geprüft wurde.

### 14.3 Stale Reviews

Wenn nach einem Approval relevante Änderungen gepusht werden, muss das Approval erneuert werden. Plattformen wie GitHub unterstützen das Verwerfen alter Approvals durch Branch-Protection-Einstellungen.

---

## 15. Branch Protection

Für Hauptbranches gelten verbindliche Schutzregeln.

Beispielhafte Konfiguration:

```yaml
protected_branches:
  main:
    require_pull_request_reviews: true
    required_approving_review_count: 1
    dismiss_stale_reviews: true
    require_code_owner_reviews: true
    require_status_checks_to_pass: true
    required_status_checks:
      - build
      - test
      - integration-test
      - code-quality
      - security-scan
    restrict_pushes: true
    allow_force_pushes: false
    allow_deletions: false
```

Pflichtregeln:

- Kein direkter Push auf `main`.
- Keine Force Pushes auf geschützte Branches.
- Keine Branch-Löschung ohne Admin-Prozess.
- Required checks müssen grün sein.
- Required reviews müssen erfüllt sein.
- CODEOWNERS müssen bei relevanten Pfaden eingebunden werden.
- Stale Reviews werden bei neuen Commits invalidiert.

---

## 16. CODEOWNERS

CODEOWNERS definiert, wer für bestimmte Pfade reviewpflichtig ist.

Beispiel:

```text
# Security-sensitive configuration
/src/main/java/**/security/**        @team-security @lead-architecture

# Database migrations
/src/main/resources/db/migration/**  @team-database @lead-backend

# Kafka schemas
/src/main/avro/**                    @team-platform @team-architecture

# CI/CD
/.github/workflows/**                @team-devops

# Quality Guidelines
/docs/quality/**                     @java-chapter-leads
```

Regeln:

1. CODEOWNERS darf nicht zu breit sein.
2. CODEOWNERS muss gepflegt werden.
3. Fachliche Ownership muss real sein.
4. CODEOWNERS-Review ersetzt nicht allgemeines Peer Review.
5. Kritische Pfade brauchen mindestens einen verantwortlichen Owner.

---

## 17. CI-Gates

Ein Pull Request darf nur gemerged werden, wenn alle relevanten Gates grün sind.

Pflicht-Gates für Java-Services:

| Gate | Details/Erklärung |
|---|---|
| Build | Kompiliert mit Java 21 |
| Unit Tests | Schnelle Tests ohne externe Infrastruktur |
| Integration Tests | Testcontainers oder definierte Testumgebung |
| Static Analysis | Checkstyle, SpotBugs, PMD, Sonar oder vergleichbar |
| Formatting | Automatischer Formatter |
| Dependency Scan | CVE-/Dependency-Check |
| Secret Scan | Keine Secrets im Code |
| Container Scan | Trivy oder vergleichbar bei Docker-Images |
| Contract Tests | Pact/Spring Cloud Contract bei API-/Event-Contracts |
| Migration Check | Flyway/Liquibase bei DB-Änderungen |
| Architecture Tests | ArchUnit bei Architekturregeln |

Nicht jeder PR muss jedes Gate ausführen, aber jedes betroffene Risiko muss durch ein passendes Gate abgedeckt sein.

---

## 18. Security Review

Security Review ist nicht nur Aufgabe eines Security-Teams. Jeder Reviewer prüft Basissicherheit.

Pflichtfragen:

- Kann ein Nutzer fremde Daten abrufen oder ändern?
- Wird Tenant-Isolation eingehalten?
- Wird Input validiert?
- Gibt es neue externe Calls?
- Werden Secrets eingeführt?
- Werden sensitive Daten geloggt?
- Werden neue Dependencies eingeführt?
- Gibt es neue Actuator-/Admin-/Debug-Endpunkte?
- Werden Berechtigungen serverseitig geprüft?
- Gibt es SSRF-, Injection-, XSS-, Path-Traversal- oder Deserialization-Risiken?
- Sind Kafka-Events, Logs und Traces datensparsam?

Bei Security-Fixes gilt: Details dürfen im PR nicht unnötig ausgenutzt oder öffentlich dokumentiert werden, wenn das Repository nicht vollständig intern ist.

---

## 19. Datenbank- und Migrations-Review

Datenbankänderungen brauchen besondere Aufmerksamkeit.

Prüfen:

- Ist Migration vorwärtskompatibel?
- Ist Rollback möglich oder dokumentiert?
- Gibt es Locks auf großen Tabellen?
- Gibt es Default-Werte?
- Gibt es Backfill-Strategie?
- Gibt es Indexe?
- Sind Constraints korrekt?
- Sind Nullability-Änderungen sicher?
- Sind Datenmengen berücksichtigt?
- Ist die Anwendung während Rolling Deployment kompatibel?

Schlecht:

```sql
ALTER TABLE orders ADD COLUMN status VARCHAR(30) NOT NULL;
```

Wenn die Tabelle Daten enthält, kann das fehlschlagen oder lange locken.

Besser:

```sql
ALTER TABLE orders ADD COLUMN status VARCHAR(30);
UPDATE orders SET status = 'PENDING' WHERE status IS NULL;
ALTER TABLE orders ALTER COLUMN status SET NOT NULL;
```

Bei großen Tabellen ist auch das in Batches oder mit Online-Migration zu prüfen.

---

## 20. API- und Contract-Review

Bei API-Änderungen prüfen:

- Ist die Änderung rückwärtskompatibel?
- Wurde OpenAPI angepasst?
- Wurden Contract-Tests angepasst?
- Gibt es Consumer?
- Braucht es neue Version?
- Sind Fehlerformate konsistent?
- Sind Status-Codes korrekt?
- Gibt es Idempotenzanforderungen?
- Enthält die API sensitive Daten?
- Werden Objektberechtigungen geprüft?

Breaking Changes dürfen nicht still in bestehende Versionen gemerged werden.

---

## 21. Event- und Kafka-Review

Bei Event-Änderungen prüfen:

- Ist Topic-Name stabil?
- Ist Schema versioniert?
- Ist Änderung kompatibel?
- Gibt es Defaults für neue Felder?
- Ist Partition Key korrekt?
- Sind Consumer idempotent?
- Gibt es DLT-Strategie?
- Sind PII/Secrets ausgeschlossen?
- Ist Tenant-Kontext enthalten?
- Gibt es Schema-Kompatibilitätsprüfung in CI?

---

## 22. Review bei Refactoring

Refactoring-PRs müssen besonders sauber getrennt werden.

Regeln:

1. Keine Verhaltensänderung im Refactoring-PR.
2. Tests müssen vorher und nachher grün sein.
3. Wenn Verhalten geändert wird, separater PR.
4. Große automatische Formatierung getrennt von fachlicher Änderung.
5. Vorher/Nachher-Risiko in PR-Beschreibung angeben.

Gute PR-Beschreibung:

```markdown
## Ziel

Extraktion von `TaxCalculationStrategy`, ohne Verhalten zu ändern.

## Nachweis

- Bestehende Tests unverändert grün.
- Keine API-Änderung.
- Keine DB-Änderung.
- Folge-PR ergänzt AT-Steuerlogik.
```

---

## 23. Review-Timing und Teamregeln

Empfohlene Teamregeln:

- PRs werden innerhalb eines Arbeitstags erstgesichtet.
- Kleine PRs werden bevorzugt schnell reviewed.
- Große PRs werden aufgeteilt oder synchron reviewed.
- Blockierende Kommentare werden priorisiert geklärt.
- Reviewer-Kapazität wird geplant.
- Reviews sind Teil der Arbeit, nicht Nebenbeschäftigung.
- Niemand merged eigene PRs ohne erforderliches Review.
- Hotfixes haben beschleunigten, aber nicht reviewfreien Prozess.

Für komplexe Änderungen ist ein synchrones Review oft besser als langes asynchrones Ping-Pong.

---

## 24. Hotfix-Prozess

Hotfixes dürfen schneller laufen, aber nicht ohne Kontrolle.

Minimalstandard:

1. Issue oder Incident-Referenz.
2. Kleiner PR.
3. Mindestens ein qualifizierter Reviewer.
4. Relevante Tests oder begründete Testausnahme.
5. CI soweit möglich grün.
6. Nachgelagerter Review, wenn unter Incident-Druck gemerged wurde.
7. Retrospektive bei kritischem Produktionsfehler.
8. Nacharbeitsticket für technische Schulden.

Kein Hotfix darf absichtlich Security-, Daten- oder Compliance-Risiken ignorieren.

---

## 25. Anti-Patterns

### 25.1 Rubber Stamp

```text
LGTM
```

ohne sichtbare Prüfung bei komplexem PR.

Problem: Qualitätskontrolle existiert nur formal.

### 25.2 Review als persönlicher Angriff

```text
Du hast das falsch gemacht.
```

Besser:

```text
[major] Diese Methode mischt Validierung und Persistenz. Dadurch wird der Fehlerfall
schwer testbar. Bitte Validierung in ein Value Object oder einen Validator ziehen.
```

### 25.3 Stilkommentare statt Risikoprüfung

Ein Review mit zehn Whitespace-Kommentaren und keiner Prüfung von Security, Korrektheit oder Tests verfehlt den Zweck.

### 25.4 Riesen-PR

Zu große PRs führen zu oberflächlichen Reviews.

### 25.5 Nachträgliches Reinschieben

Nach Approval werden größere Änderungen gepusht und sofort gemerged. Das ist nicht zulässig.

### 25.6 CI rot, trotzdem Merge

Nur mit dokumentierter Ausnahme und Verantwortlichem zulässig. Normalfall: verboten.

### 25.7 Tests entfernen statt reparieren

Flaky oder rote Tests dürfen nicht einfach gelöscht werden, ohne Ursache und Ersatz zu klären.

### 25.8 Review-Kommentare ignorieren

Offene major/blocker-Kommentare dürfen nicht ungelöst bleiben.

---

## 26. Gute Anwendung: Review-Kommentar mit Guideline-Bezug

```text
[blocker] Dieser Controller gibt `OrderEntity` direkt zurück.
Das verletzt unsere API- und JPA-Guidelines: Entities dürfen die Service-/API-Grenze
nicht verlassen. Bitte ein `OrderResponse`-DTO verwenden und Mapping im Adapter halten.

Risiko:
- interne Felder werden exponiert,
- Lazy Loading kann im JSON-Serializer auslösen,
- API-Vertrag koppelt an DB-Modell.
```

```text
[major] Der neue Kafka-Consumer ist nicht idempotent.
Wenn dasselbe Event nach Rebalance oder Retry erneut verarbeitet wird, wird der Bestand
doppelt reserviert. Bitte `eventId` speichern oder eine fachlich idempotente Operation
verwenden.
```

```text
[praise] Die PR-Beschreibung ist sehr gut: Problem, Lösung, Tests und Rollback sind
klar. Dadurch ist das Review deutlich einfacher.
```

---

## 27. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| PR-Größe | Ist der PR klein und fokussiert? | < 400 Zeilen | Pflicht |
| Beschreibung | Sind Problem, Lösung, Tests, Risiken klar? | PR-Template ausgefüllt | Pflicht |
| Korrektheit | Ist Fachlogik korrekt? | Randfälle geprüft | Pflicht |
| Tests | Gibt es passende Tests? | Unit, Slice, Integration | Pflicht |
| Security | Sind Auth, Input, Secrets, PII geprüft? | keine Objektberechtigungs-Lücke | Pflicht |
| Design | Ist die Abstraktion angemessen? | kein Overengineering | Pflicht |
| Performance | Keine N+1, keine unnötigen Schleifen, keine teuren Queries? | Projection statt Lazy Load | Pflicht |
| Datenbank | Migration sicher und rollbar? | Backfill-Strategie | Pflicht bei DB |
| API | Rückwärtskompatibilität geprüft? | OpenAPI/Contract | Pflicht bei API |
| Events | Schema, Key, Idempotenz geprüft? | Avro + DLT | Pflicht bei Kafka |
| Observability | Logging/Metriken/Tracing angepasst? | neue kritische Operation sichtbar | Pflicht |
| CI | Alle relevanten Checks grün? | build/test/security | Pflicht |
| Kommentare | Sind blocker/major gelöst? | keine offenen Blocker | Pflicht |
| Ownership | Sind CODEOWNERS beteiligt? | Security/DB/API | Pflicht bei relevanten Pfaden |

---

## 28. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Main Branch ist geschützt.
- Required Reviews sind aktiviert.
- Stale Reviews werden invalidiert.
- Required Status Checks sind aktiviert.
- CODEOWNERS ist vorhanden.
- PR-Template ist vorhanden.
- Direkte Pushes auf main sind verboten.
- Force Pushes auf main sind verboten.
- Security-Scan läuft in PRs.
- Secret-Scan läuft in PRs.
- Tests laufen vor Merge.
- Große PRs werden markiert.
- PRs mit DB-Migration benötigen DB-Codeowner.
- PRs mit security-Pfad benötigen Security-Codeowner.
```

Beispiel `CODEOWNERS`:

```text
/src/main/java/**/security/**        @security-team @java-chapter-leads
/src/main/resources/db/migration/**  @database-team @java-chapter-leads
/src/main/avro/**                    @platform-team @architecture-board
/.github/workflows/**                @devops-team
```

---

## 29. Migration bestehender Review-Prozesse

Migration erfolgt in Schritten:

1. PR-Template einführen.
2. Branch Protection für `main` aktivieren.
3. Required CI Checks definieren.
4. CODEOWNERS für kritische Pfade ergänzen.
5. Review-Schweregrade einführen.
6. Teamregel für PR-Größe vereinbaren.
7. Security- und Datenbank-Reviewregeln ergänzen.
8. Große Alt-PRs vermeiden und neue Arbeit in kleine PRs schneiden.
9. Review-Kultur mit Beispielen trainieren.
10. Metriken beobachten: PR-Durchlaufzeit, Review-Latenz, Defekte nach Merge.

---

## 30. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein kritischer Produktions-Hotfix sofort nötig ist,
- ein generierter Codeblock sehr groß ist,
- ein reines Formatierungs-Refactoring viele Dateien betrifft,
- eine Repository-Migration technisch große Diffs erzeugt,
- ein externer Merge aus Upstream-Quelle notwendig ist.

Ausnahmen müssen dokumentiert werden. Auch bei Ausnahmen gilt: Kein bewusster Merge mit bekannten Security-, Datenverlust- oder Compliance-Risiken ohne explizite Verantwortungsübernahme.

---

## 31. Definition of Done

Ein Pull Request erfüllt diese Richtlinie, wenn:

1. der PR klein und fokussiert ist,
2. die Beschreibung Problem, Lösung, Tests und Risiken erklärt,
3. alle relevanten Tests grün sind,
4. CI erfolgreich ist,
5. mindestens die erforderlichen Reviews erfolgt sind,
6. CODEOWNERS bei kritischen Pfaden eingebunden wurden,
7. blocker- und major-Kommentare geschlossen sind,
8. Security-, Performance-, Datenbank-, API- und Event-Risiken geprüft wurden, soweit betroffen,
9. keine sensiblen Daten in Logs, Events oder Fehlermeldungen eingeführt wurden,
10. Branch Protection erfüllt ist,
11. der Merge keine offenen, ungeklärten Qualitätsrisiken enthält.

---

## 32. Quellen und weiterführende Literatur

- GitHub Docs — About protected branches: https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches
- GitHub Docs — About pull request reviews: https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/reviewing-changes-in-pull-requests/about-pull-request-reviews
- GitHub Docs — About code owners: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners
- GitHub Docs — About status checks: https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks
- Google Engineering Practices — How to do a code review: https://google.github.io/eng-practices/review/reviewer/
- Google Engineering Practices — The CL author’s guide: https://google.github.io/eng-practices/review/developer/
- OWASP Code Review Guide: https://owasp.org/www-project-code-review-guide/
- OWASP Secure Coding Practices Quick Reference Guide: https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/
