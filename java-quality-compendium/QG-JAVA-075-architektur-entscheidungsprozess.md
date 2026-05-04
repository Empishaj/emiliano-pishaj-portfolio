# QG-JAVA-075 — Architektur-Entscheidungsprozess

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-075 |
| Titel | Architektur-Entscheidungsprozess |
| Status | Accepted / verbindlicher Governance-Standard für Architekturentscheidungen |
| Version | 1.0.0 |
| Datum | 2023-11-06 |
| Kategorie | Governance / Architektur / Entscheidungsprozess / Engineering Operating Model |
| Zielgruppe | CTO, Architektur-Board, Tech Leads, Java Chapter Leads, Entwickler, QA, Security, DevOps, Product Owner |
| Java-Baseline | Java 21 |
| Framework-Kontext | Framework-unabhängig; anwendbar auf Java, Spring Boot, SaaS-Plattformen, Microservices, Datenplattformen und Infrastrukturentscheidungen |
| Geltungsbereich | Architekturentscheidungen, technische Standards, teamübergreifende Schnittstellen, Security-Modelle, Persistenzentscheidungen, Messaging, API-Standards, Betriebsarchitektur, Plattformentscheidungen |
| Verbindlichkeit | Architekturentscheidungen mit relevanter Auswirkung müssen nach diesem Prozess vorbereitet, reviewed, dokumentiert und später überprüft werden. Nicht jede technische Kleinentscheidung benötigt ein ADR. |
| Technische Validierung | Gegen ADR-Konzept nach Michael Nygard, Thoughtworks Technology Radar, GitHub Branch Protection/CODEOWNERS und moderne Architecture-Governance-Praktiken eingeordnet |
| Kurzentscheidung | Architekturentscheidungen werden nicht stillschweigend im Code, in Meetings oder in Köpfen getroffen, sondern bei definierter Tragweite über einen leichten, reviewbaren und versionierten Entscheidungsprozess dokumentiert. |

---

## 1. Zweck

Diese Richtlinie definiert den Prozess, nach dem Architekturentscheidungen in Java- und SaaS-Systemen getroffen, dokumentiert, reviewed, umgesetzt und später überprüft werden.

Der Zweck ist nicht, Engineering durch Bürokratie zu bremsen. Der Zweck ist, technische Entscheidungen nachvollziehbar, überprüfbar und teamübergreifend konsistent zu machen.

Ohne definierten Entscheidungsprozess entstehen typische Schäden:

- Entscheidungen werden mündlich getroffen und später vergessen.
- Teams kopieren Technologien, weil große Unternehmen sie nutzen, nicht weil sie zum eigenen Problem passen.
- Diskussionen werden nach Monaten wiederholt, weil die ursprüngliche Begründung fehlt.
- Neue Entwickler sehen nur das Ergebnis, aber nicht die Gründe.
- Architektur driftet über Teams auseinander.
- Security-, Performance-, Kosten- oder Betriebsentscheidungen werden lokal getroffen, obwohl sie systemweite Wirkung haben.
- Gute Entscheidungen werden nach Personalwechsel zurückgedreht, weil niemand mehr erklären kann, warum sie getroffen wurden.
- Schlechte Entscheidungen bleiben zu lange bestehen, weil kein Review-Datum existiert.

Ein guter Entscheidungsprozess macht technische Führung skalierbar. Er verhindert, dass jede Entscheidung beim CTO landet, ohne Architekturqualität dem Zufall zu überlassen.

---

## 2. Kurzregel

Eine Architekturentscheidung wird dokumentiert, wenn sie mehrere Teams betrifft, schwer rückgängig zu machen ist, bestehende Standards verändert, Security, Performance, Kosten, Datenmodell, API-Verträge, Betriebsmodell oder Plattformarchitektur beeinflusst oder einem bestehenden Standard widerspricht. Die Entscheidung wird leichtgewichtig als ADR oder vergleichbares Entscheidungsdokument beschrieben, im Repository versioniert, über Pull Request reviewed, mit Review-Datum versehen und bei Bedarf durch automatisierte Regeln abgesichert.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für Entscheidungen zu:

- Programmiersprachen und Runtime-Versionen,
- Java- und Spring-Boot-Baselines,
- Architekturmustern,
- Modul- und Servicegrenzen,
- API-Design und Versionierung,
- Datenbanktechnologie und Schema-Strategien,
- JPA-/Persistenzstandards,
- Kafka-, Messaging- und Event-Strategien,
- Security-Modellen,
- Authentifizierung und Autorisierung,
- Mandantenfähigkeit,
- Observability,
- Deployment- und Runtime-Modell,
- Container- und Kubernetes-Standards,
- CI/CD-Gates,
- Teststrategie,
- technische Schulden mit Systemwirkung,
- Framework- und Bibliotheksentscheidungen mit langfristiger Bindung,
- Abweichungen von bestehenden Quality Guidelines.

Nicht jede lokale Implementierungsentscheidung braucht ein ADR. Ein Methodenname, eine kleine Hilfsklasse oder eine lokale Refactoring-Entscheidung wird im Code Review geklärt, nicht im Architekturprozess.

---

## 4. Technischer Hintergrund

Architecture Decision Records wurden durch Michael Nygards Artikel „Documenting Architecture Decisions“ bekannt. Der Kern ist leichtgewichtig: Eine Entscheidung wird mit Kontext, Entscheidung und Konsequenzen dokumentiert. Der Wert liegt nicht nur im Ergebnis, sondern in der Begründung und in den später sichtbaren Trade-offs.

Thoughtworks beschreibt Lightweight Architecture Decision Records als Technik, um wichtige Architekturentscheidungen samt Kontext und Konsequenzen zu erfassen, und empfiehlt, sie in Source Control statt in einem Wiki abzulegen. Dadurch bleiben sie näher am Code, versionierbar und reviewbar.

GitHub unterstützt die technische Durchsetzung eines solchen Prozesses über Pull Requests, Protected Branches, Required Reviews, Status Checks und CODEOWNERS. Diese Mechanismen machen aus Architektur-Governance keinen losgelösten Papierprozess, sondern einen Bestandteil des normalen Engineering-Flows.

---

## 5. Entscheidungsprinzipien

### 5.1 Entscheidungen sind billig zu treffen, aber teuer zu vergessen

Viele Architekturentscheidungen wirken über Jahre. Der größte Schaden entsteht nicht dadurch, dass eine Entscheidung diskutiert wird, sondern dadurch, dass sie später ohne Kontext wiederholt, missverstanden oder unbewusst gebrochen wird.

### 5.2 Der Prozess muss leichtgewichtig bleiben

Ein ADR ist kein 40-seitiges Lösungsdesign. Es beschreibt die relevante Entscheidung, den Kontext, die Alternativen, die Konsequenzen und den Umsetzungsrahmen.

### 5.3 Entscheidungskompetenz bleibt bei Teams, aber nicht ohne Beratung

Teams sollen Entscheidungen treffen können. Sie müssen aber betroffene Teams und relevante Fachexperten einbeziehen, wenn eine Entscheidung über lokale Teamgrenzen hinaus wirkt.

### 5.4 Nicht jede Entscheidung braucht ein ADR

Zu viele ADRs machen den Prozess unbrauchbar. Zu wenige ADRs machen Architekturwissen unsichtbar. Diese Richtlinie definiert deshalb klare Schwellenwerte.

### 5.5 Entscheidungen müssen überprüfbar sein

Ein ADR ohne Umsetzungsnachweis, Review-Datum oder Verankerung im Code wird leicht zum Dokumentenfriedhof. Strukturelle Entscheidungen sollen durch Tests, ArchUnit, CI-Gates oder Review-Checklisten abgesichert werden, soweit sinnvoll.

---

## 6. Wann ein ADR erforderlich ist

Ein ADR ist erforderlich, wenn mindestens zwei der folgenden Kriterien zutreffen:

| Kriterium | Details/Erklärung | Beispiel |
|---|---|---|
| Teamübergreifende Wirkung | Mehr als ein Team ist betroffen. | gemeinsamer API-Standard |
| Schwer rückgängig | Entscheidung erzeugt starke Bindung. | Datenbankwahl, Messaging-Plattform |
| Security-relevant | Sicherheitsmodell, Auth, Secrets, Datenzugriff betroffen. | OAuth2 Resource Server |
| Datenmodell-relevant | Persistenz, Migration, Retention, Mandantenfähigkeit betroffen. | Multi-Tenant-Schema |
| API-/Contract-relevant | Externe oder interne Consumer betroffen. | REST-Versionierung |
| Betriebsrelevant | Observability, Deployment, Skalierung, Resilienz betroffen. | Kubernetes Readiness |
| Kostenrelevant | Erhebliche laufende Kosten oder Vendor-Bindung. | Managed Kafka |
| Performance-relevant | Latenz, Durchsatz oder Skalierung betroffen. | Cache-Strategie |
| Standardänderung | Bestehende Guideline oder ADR wird geändert. | Wechsel von WebFlux zu Virtual Threads |
| Abweichung | Bewusste Ausnahme von Standard. | Service nutzt MongoDB statt PostgreSQL |
| Langfristige Bindung | Entscheidung wirkt voraussichtlich über mehrere Releases. | Framework-Wahl |
| Audit-/Nachweispflicht | Entscheidung muss später begründbar sein. | Logging personenbezogener Daten vermeiden |

Ein ADR ist nicht erforderlich für:

| Entscheidung | Details/Erklärung |
|---|---|
| lokale Implementierungsdetails | private Methoden, kleine Klassenextraktionen |
| triviale Refactorings | keine Architekturwirkung |
| reine Formatierung | keine Verhaltensänderung |
| kleine Testergänzungen | keine Systementscheidung |
| eindeutige Anwendung bestehender Guidelines | kein neuer Entscheidungsbedarf |
| kleine Bugfixes | sofern keine Architekturfolge entsteht |

---

## 7. Entscheidungsarten

Nicht jede dokumentierte Entscheidung ist gleich groß. Das Dokumentationsformat muss zur Tragweite passen.

| Entscheidungsart | Details/Erklärung | Format |
|---|---|---|
| Lokale Designentscheidung | Betrifft nur eine Klasse oder ein kleines Modul. | Code Review, Kommentar nur bei Bedarf |
| Teamentscheidung | Betrifft ein Team oder einen Service. | Mini-ADR oder Technical Decision Note |
| Architekturentscheidung | Betrifft mehrere Module, Teams oder langlebige Standards. | ADR |
| Plattformentscheidung | Betrifft viele Services oder zentrale Infrastruktur. | ADR + Architektur-Board Review |
| Sicherheitsentscheidung | Betrifft Schutzmodell oder Datenzugriff. | ADR + Security Review |
| Ausnahmeentscheidung | Weicht von bestehendem Standard ab. | Ausnahme-ADR oder Deviation Note |
| Ablösung | Ersetzt eine bestehende Entscheidung. | Superseding ADR |

---

## 8. Rollen und Verantwortlichkeiten

| Rolle | Verantwortlichkeiten |
|---|---|
| Autor | Bereitet Kontext, Alternativen, Entscheidung und Konsequenzen vor. |
| Tech Lead | Prüft technische Tragfähigkeit, Umsetzbarkeit, Komplexität und Teamwirkung. |
| Architektur-Board | Prüft systemweite Konsistenz, Standards, langfristige Folgen und Konflikte. |
| Security | Prüft Auth, Datenzugriff, Secrets, Bedrohungen, Logging, Compliance-Risiken. |
| QA/Test | Prüft Testbarkeit, Qualitätsnachweise, Regression und Abnahmekriterien. |
| DevOps/SRE | Prüft Betrieb, Observability, Deployment, Kosten, Resilienz und Rollback. |
| Product Owner | Prüft fachlichen Nutzen, Scope, Roadmap-Bezug und Migrationsrisiken. |
| Betroffene Teams | Liefern Feedback zu Auswirkungen, Abhängigkeiten und Umsetzbarkeit. |
| Entscheider | Akzeptiert, lehnt ab oder fordert Überarbeitung. Je nach Tragweite Team, Tech Lead, Architektur-Board oder CTO. |

---

## 9. ADR-Lebenszyklus

Empfohlener Lebenszyklus:

```text
Draft
  ↓
Proposed
  ↓
Review
  ↓
Accepted / Rejected / Deferred
  ↓
Implementation
  ↓
Confirmed / Superseded / Deprecated
```

### 9.1 Statusdefinitionen

| Status | Bedeutung |
|---|---|
| Draft | Früher Entwurf, noch nicht reviewbereit. |
| Proposed | Entscheidungsvorschlag ist reviewbereit. |
| Accepted | Entscheidung ist angenommen und soll umgesetzt werden. |
| Rejected | Entscheidung wurde abgelehnt; Begründung bleibt dokumentiert. |
| Deferred | Entscheidung wird vertagt, weil Kontext oder Daten fehlen. |
| Implemented | Entscheidung wurde technisch umgesetzt. |
| Confirmed | Entscheidung wurde beim Review bestätigt. |
| Superseded | Entscheidung wurde durch ein neueres ADR ersetzt. |
| Deprecated | Entscheidung gilt noch für Bestand, soll aber nicht mehr neu verwendet werden. |

### 9.2 Prozessfluss

```text
1. Entscheidungsschwelle prüfen.
2. Draft erstellen.
3. Betroffene Teams und Fachexperten identifizieren.
4. Proposed-PR eröffnen.
5. Review innerhalb definierter Frist.
6. Entscheidung akzeptieren, ablehnen oder vertagen.
7. Umsetzung über getrennte PRs.
8. Umsetzung im ADR referenzieren.
9. Review-Datum setzen.
10. Nach 6–12 Monaten überprüfen.
```

---

## 10. Review-SLA

Empfohlene Fristen:

| Entscheidungstyp | Review-Frist | Bemerkung |
|---|---|---|
| Teaminterne ADR | 3 Arbeitstage | Tech Lead + betroffene Reviewer |
| Teamübergreifende ADR | 5 Arbeitstage | betroffene Teams einbeziehen |
| Security-relevante ADR | 5–10 Arbeitstage | abhängig von Risiko |
| Plattformentscheidung | 10 Arbeitstage | Architektur-Board |
| Hotfix-Entscheidung | beschleunigt, aber dokumentiert | nachträgliche Bestätigung nötig |
| Deferred | nächster definierter Termin | fehlende Daten beschaffen |

Eine Frist ist kein Automatismus für Zustimmung. Sie verhindert Entscheidungsstau.

---

## 11. ADR-Template

Empfohlenes Template:

```markdown
# ADR-XXX — <Titel>

## Dokumentstatus

| Feld | Wert |
|---|---|
| Status | Proposed / Accepted / Rejected / Superseded |
| Datum | YYYY-MM-DD |
| Review-Datum | YYYY-MM-DD |
| Entscheider | Team / Tech Lead / Architektur-Board / CTO |
| Betroffene Teams | ... |
| Kategorie | ... |
| Ersetzt | ADR-... |
| Ersetzt durch | ADR-... |

## Kontext

Welche Situation, welches Problem oder welcher Druck führt zu dieser Entscheidung?

## Entscheidung

Was wird entschieden?

## Alternativen

Welche Optionen wurden betrachtet?

## Begründung

Warum wurde diese Option gewählt?

## Konsequenzen

Welche positiven und negativen Folgen entstehen?

## Umsetzung

Welche Arbeitspakete, Migrationen, Tests oder Gates sind nötig?

## Akzeptanzkriterien

Wann gilt die Entscheidung als umgesetzt?

## Review

Wann und anhand welcher Signale wird geprüft, ob die Entscheidung weiterhin gilt?

## Quellen

Welche Dokumente, Standards, Messwerte oder Referenzen stützen die Entscheidung?
```

---

## 12. Was in ein gutes ADR gehört

Ein gutes ADR beantwortet:

1. Welches Problem wird gelöst?
2. Warum ist das jetzt relevant?
3. Welche Alternativen wurden geprüft?
4. Warum wurde die gewählte Option bevorzugt?
5. Welche Nachteile werden bewusst akzeptiert?
6. Welche Systeme und Teams sind betroffen?
7. Welche Security-, Performance-, Kosten- und Betriebsfolgen entstehen?
8. Welche Tests oder technischen Gates sichern die Entscheidung ab?
9. Wie wird migriert?
10. Wann wird überprüft, ob die Entscheidung noch gilt?

---

## 13. Was nicht in ein ADR gehört

Nicht geeignet:

- lange Implementierungsdetails,
- vollständige Spezifikationen,
- große Design-Dokumente,
- Code-Dumps,
- Ticketlisten ohne Entscheidung,
- Meeting-Protokolle,
- Marketingargumente,
- unbelegte Technologiebegeisterung,
- „Wir machen das, weil Unternehmen X das auch macht“,
- „Das war schon immer so“.

Ein ADR dokumentiert eine Entscheidung, nicht jede Diskussion.

---

## 14. Bewertungskriterien für Entscheidungen

Jede relevante Architekturentscheidung wird entlang folgender Qualitätsziele bewertet:

| Qualitätsziel | Prüffrage |
|---|---|
| Korrektheit | Löst die Entscheidung das tatsächliche Problem? |
| Wartbarkeit | Wird der Code langfristig verständlicher oder stabiler? |
| Änderbarkeit | Wie leicht kann die Entscheidung später angepasst werden? |
| Testbarkeit | Können Auswirkungen automatisiert geprüft werden? |
| Sicherheit | Verbessert oder gefährdet die Entscheidung Schutzmechanismen? |
| Performance | Welche Latenz-, Durchsatz- oder Skalierungseffekte entstehen? |
| Betrieb | Ist die Lösung beobachtbar, deploybar und supportbar? |
| Kosten | Welche laufenden und einmaligen Kosten entstehen? |
| Teamfähigkeit | Können Teams die Lösung verstehen und betreiben? |
| Standardkonformität | Passt die Entscheidung zu bestehenden Guidelines? |
| Migrationsfähigkeit | Gibt es einen realistischen Übergangspfad? |

---

## 15. Alternativenanalyse

Jedes ADR muss mindestens zwei ernsthafte Optionen oder eine begründete „do nothing“-Option enthalten.

Beispiel:

| Alternative | Stärken | Schwächen | Entscheidung |
|---|---|---|---|
| PostgreSQL JSONB | flexibel, bekannt, transaktional | weniger spezialisiert als Dokumentdatenbank | gewählt |
| MongoDB | dokumentorientiert, flexible Struktur | neue Betriebs- und Konsistenzkomplexität | abgelehnt |
| Separate Tabellen | stark normalisiert, gute Constraints | hoher Migrationsaufwand | abgelehnt |
| Status quo | kein Aufwand | Problem bleibt bestehen | abgelehnt |

---

## 16. Entscheidungsbefugnis

Nicht jede Entscheidung braucht denselben Entscheider.

| Tragweite | Entscheider | Review |
|---|---|---|
| Lokale Modulentscheidung | verantwortlicher Entwickler + Reviewer | Code Review |
| Serviceinterne Architekturentscheidung | Tech Lead | Team Review |
| Teamübergreifende Entscheidung | betroffene Tech Leads | Cross-Team Review |
| Plattformstandard | Architektur-Board | Plattform-/Security-/DevOps-Review |
| Security-Modell | Security + Architektur-Board | Security Review erforderlich |
| Strategische Technologieentscheidung | CTO / Architektur-Board | Wirtschaftlichkeits- und Risikoanalyse |
| Abweichung von Standard | Standard-Owner | Deviation Review |

Ziel ist dezentrale Entscheidung mit klarer Einbindung relevanter Expertise, nicht CTO-Bottleneck.

---

## 17. CODEOWNERS und Branch Protection

ADR-Änderungen müssen technisch geschützt sein.

Beispiel `CODEOWNERS`:

```text
/docs/adr/**                  @architecture-board @java-chapter-leads
/docs/quality/**              @java-chapter-leads
/docs/security/**             @security-team @architecture-board
/src/main/resources/db/**     @database-team
/src/main/avro/**             @platform-team @architecture-board
```

Empfohlene Branch Protection:

```yaml
main:
  require_pull_request_reviews: true
  required_approving_review_count: 1
  dismiss_stale_reviews: true
  require_code_owner_reviews: true
  require_status_checks_to_pass: true
  required_status_checks:
    - build
    - test
    - architecture-rules
    - markdown-lint
```

Für ADRs gilt:

- Änderung nur per Pull Request.
- Architektur-Codeowner muss reviewen.
- Relevante Fachexperten müssen einbezogen werden.
- Veraltete Approvals werden bei Änderungen invalidiert.
- ADR-Dateien werden im Repository versioniert.

---

## 18. Automatisierte Durchsetzung

Ein ADR soll, wenn möglich, durch technische Regeln abgesichert werden.

Beispiele:

| ADR-Typ | Mögliche Durchsetzung |
|---|---|
| Schichtenarchitektur | ArchUnit |
| Hexagonal Architecture | Paketabhängigkeitsregeln |
| Keine JPA in Domain | ArchUnit |
| REST API Format | OpenAPI Linting |
| Kafka Schema | Schema Registry Compatibility Check |
| Keine Secrets | Secret Scanning |
| Keine PII in Logs | statische Analyse + Review |
| Docker Non-Root | Container Scan / Dockerfile Lint |
| Teststrategie | Build-Profile / Test-Tags |
| Code Review Pflicht | Branch Protection |

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule domain_must_not_depend_on_spring =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("Diese Architekturentscheidung hält die Domäne frameworkfrei.");
```

---

## 19. ADR-Index

Jedes Repository mit ADRs muss einen Index führen.

Beispiel:

```markdown
# Architecture Decision Records

| ADR | Titel | Status | Datum | Review-Datum | Owner |
|---|---|---|---|---|---|
| ADR-001 | Java 21 als Runtime-Baseline | Accepted | 2023-11-06 | 2026-05-01 | Java Chapter |
| ADR-002 | PostgreSQL als Standarddatenbank | Accepted | 2024-02-12 | 2026-02-12 | Plattform |
| ADR-003 | Kafka Events mit Schema Registry | Accepted | 2024-03-04 | 2025-09-04 | Architektur |
```

Der Index macht sichtbar:

- welche Entscheidungen existieren,
- welche noch gültig sind,
- welche überfällig für Review sind,
- welche ersetzt wurden,
- wem die Entscheidung gehört.

---

## 20. Review bestehender ADRs

Jedes ADR erhält ein Review-Datum.

Review-Fragen:

1. Ist die Entscheidung noch gültig?
2. Haben sich Anforderungen geändert?
3. Haben sich Kosten geändert?
4. Gibt es neue Sicherheitsrisiken?
5. Gibt es bessere technische Optionen?
6. Wurde die Entscheidung umgesetzt?
7. Wird sie im Code eingehalten?
8. Gibt es Verstöße oder Ausnahmen?
9. Muss ein neues ADR das alte ersetzen?
10. Muss die Entscheidung als deprecated markiert werden?

Mögliche Review-Ergebnisse:

| Ergebnis | Bedeutung |
|---|---|
| Confirmed | Entscheidung gilt weiter. |
| Superseded | Neues ADR ersetzt diese Entscheidung. |
| Deprecated | Nicht mehr für neue Arbeit verwenden. |
| Rejected after review | Entscheidung wird aktiv zurückgenommen. |
| Needs migration | Entscheidung ist richtig, aber Umsetzung unvollständig. |

---

## 21. Kosten-Nutzen-Betrachtung

### 21.1 Typische Kosten

| Kostenart | Details/Erklärung |
|---|---|
| Erstellung | 1–4 Stunden für normale ADRs |
| Review | 30–120 Minuten je nach Tragweite |
| Abstimmung | Betroffene Teams müssen eingebunden werden |
| Pflege | Review-Daten und Status aktuell halten |
| Automatisierung | ArchUnit, CI-Gates oder Linting aufsetzen |
| Migration | Bestehende Entscheidungen nachdokumentieren |

### 21.2 Typischer Nutzen

| Nutzen | Details/Erklärung |
|---|---|
| Nachvollziehbarkeit | Neue Teammitglieder verstehen Entscheidungen schneller. |
| Weniger Wiederholungsdiskussionen | Alte Debatten müssen nicht neu geführt werden. |
| Konsistenz | Teams arbeiten nach denselben Architekturgrundlagen. |
| Bessere Reviews | Reviewer können auf dokumentierte Entscheidungen verweisen. |
| Onboarding | Architekturwissen ist sichtbar statt personengebunden. |
| Auditierbarkeit | Security-, Daten- und Betriebsentscheidungen sind belegbar. |
| Weniger Architekturdrift | Abweichungen werden früher sichtbar. |
| Schnellere Entscheidungen | Klare Schwellenwerte verhindern Eskalationsstau. |

### 21.3 Korrektur zu Metriken

Konkrete pauschale Aussagen wie „dokumentierte Architekturstandards erzeugen exakt X-fache Deployment-Frequenz“ dürfen nicht ohne belastbaren Nachweis in ADRs stehen. DORA-Metriken wie Deployment Frequency, Lead Time, Change Failure Rate und Failed Deployment Recovery Time sind nützlich, um Software Delivery Performance zu messen. Sie ersetzen aber keine direkte Evidenz für eine spezifische ADR-Prozesswirkung.

Für dieses Kompendium gilt deshalb: Nutzenannahmen zu ADRs werden als Hypothesen oder qualitative erwartete Wirkungen formuliert, nicht als harte Statistik, wenn keine direkt passende Quelle vorliegt.

---

## 22. Risiken und Gegenmaßnahmen

| Risiko | Wahrscheinlichkeit | Auswirkung | Gegenmaßnahme |
|---|---:|---:|---|
| Prozess wird zu schwer | Mittel | Hoch | klare Schwellenwerte, kurze Templates |
| ADR-Friedhof | Hoch | Hoch | Review-Datum, Index, Owner |
| Entscheidungen werden nicht umgesetzt | Mittel | Hoch | Akzeptanzkriterien, Umsetzungstickets |
| ADRs werden nicht gelesen | Mittel | Mittel | Verlinkung in PRs, Code, Guidelines |
| CTO-Bottleneck | Mittel | Hoch | dezentrale Entscheidungsbefugnis |
| Zu viele ADRs | Mittel | Mittel | Entscheidungsschwellen prüfen |
| Zu wenige ADRs | Mittel | Hoch | Review-Checkliste und PR-Gate |
| Konflikte zwischen ADRs | Mittel | Hoch | ADR-Index und Superseded-Mechanismus |
| Fehlende Durchsetzung | Hoch | Hoch | ArchUnit, CI, CODEOWNERS |
| Veraltete Entscheidungen | Hoch | Mittel | Review-Zyklus |

---

## 23. Gute Anwendung

### 23.1 Beispiel: Entscheidung braucht ADR

Ein Team möchte von relationaler Persistenz auf MongoDB wechseln.

ADR erforderlich, weil:

- Datenmodell betroffen,
- schwer rückgängig,
- Betrieb betroffen,
- Backup/Restore betroffen,
- Query-Modell betroffen,
- Teamgrenzen möglich,
- Skills und Kosten betroffen,
- bestehende Standards betroffen.

### 23.2 Beispiel: Entscheidung braucht kein ADR

Ein Entwickler extrahiert eine private Methode aus einer langen Service-Methode.

Kein ADR erforderlich, weil:

- lokale Änderung,
- leicht rückgängig,
- keine Teamwirkung,
- keine Plattformwirkung,
- kein Architekturstandard betroffen.

### 23.3 Beispiel: Mini-ADR ausreichend

Ein Team entscheidet, in einem Service `MapStruct` statt manuelle Mapper zu verwenden, weil viele DTO-Mapper entstehen.

Mini-ADR reicht, wenn:

- nur ein Service betroffen,
- keine Plattformvorgabe entsteht,
- keine Security-/Kostenwirkung,
- keine starke teamübergreifende Bindung.

---

## 24. Schlechte Anwendung

### 24.1 Mündliche Architekturentscheidung

```text
"Wir hatten damals entschieden, dass alle Events JSON bleiben."
```

Problem: Niemand kennt Kontext, Alternativen, Konsequenzen oder Review-Datum.

### 24.2 Wiki ohne Codebezug

```text
Confluence-Seite von 2021 beschreibt Architektur, Code macht inzwischen etwas anderes.
```

Problem: Dokumentation driftet vom Repository weg.

### 24.3 ADR ohne Entscheidung

```markdown
# Kafka Gedanken

Wir könnten Kafka nutzen. Es gibt Vorteile und Nachteile.
```

Problem: Kein Beschluss, keine Konsequenz, kein Review.

### 24.4 ADR ohne Alternativen

```markdown
Wir nehmen Technologie X.
```

Problem: Keine nachvollziehbare Abwägung.

### 24.5 ADR ohne Umsetzung

```markdown
Accepted: Domain darf nicht von Spring abhängen.
```

Aber keine ArchUnit-Regel, kein Review-Gate, keine Migration.

Problem: Entscheidung wird nicht wirksam.

---

## 25. Zusammenhang mit Quality Guidelines

Dieses Dokument regelt den Entscheidungsprozess. Quality Guidelines regeln verbindliche Standards.

Beispiel:

| Artefakt | Zweck |
|---|---|
| ADR | Warum wurde eine Entscheidung getroffen? |
| Quality Guideline | Wie wird die Entscheidung korrekt angewendet? |
| ArchUnit-Test | Wird die Entscheidung eingehalten? |
| PR-Template | Wird die Entscheidung im Review berücksichtigt? |
| README/Index | Wo finde ich relevante Entscheidungen? |

Beispiel:

```text
ADR: Wir nutzen Hexagonal Architecture für domänenkritische Services.
QG: Wie setzen wir Ports, Adapter, Mapper und Tests konkret um?
ArchUnit: Domain darf nicht von Spring/JPA abhängen.
PR Review: Neue Domain-Klassen werden gegen diese Regel geprüft.
```

---

## 26. Entscheidungsprozess im Pull Request

### 26.1 ADR-PR

Ein ADR-PR enthält:

- ADR-Datei,
- aktualisierten ADR-Index,
- betroffene Links,
- gegebenenfalls Prototyp oder Spike-Ergebnis,
- Review-Checkliste,
- Akzeptanzkriterien,
- geplante Umsetzungsschritte.

### 26.2 Implementierungs-PR

Ein Implementierungs-PR enthält:

- Link auf ADR,
- Umsetzung eines Teilpakets,
- Tests,
- Migrationsschritte,
- falls möglich technische Durchsetzung.

ADR-PR und Implementierungs-PR dürfen getrennt sein. Bei kleineren Entscheidungen können sie kombiniert werden, wenn Reviewbarkeit erhalten bleibt.

---

## 27. Review-Checkliste

| Aspekt | Details/Erklärung | Entscheidung |
|---|---|---|
| Entscheidungsschwelle | Ist ein ADR wirklich nötig oder bewusst nicht nötig? | Prüfen |
| Kontext | Ist das Problem klar beschrieben? | Pflicht |
| Entscheidung | Ist der Beschluss eindeutig? | Pflicht |
| Alternativen | Wurden realistische Optionen betrachtet? | Pflicht |
| Begründung | Sind Trade-offs sichtbar? | Pflicht |
| Konsequenzen | Sind positive und negative Folgen benannt? | Pflicht |
| Betroffene Teams | Sind relevante Teams einbezogen? | Pflicht |
| Security | Sind Risiken und Schutzmaßnahmen bewertet? | Pflicht bei Relevanz |
| Kosten | Sind laufende Kosten und Bindungen betrachtet? | Pflicht bei Relevanz |
| Betrieb | Sind Observability, Rollback, Skalierung betrachtet? | Pflicht bei Relevanz |
| Tests | Sind Akzeptanzkriterien und Testnachweise definiert? | Pflicht |
| Automatisierung | Können Regeln technisch abgesichert werden? | Prüfen |
| Review-Datum | Ist ein realistisches Review-Datum gesetzt? | Pflicht |
| Status | Ist der ADR-Status korrekt? | Pflicht |
| Index | Ist der ADR-Index aktualisiert? | Pflicht |

---

## 28. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Jede ADR-Datei muss Status, Datum und Review-Datum enthalten.
- Jede ADR-Datei muss Kontext, Entscheidung und Konsequenzen enthalten.
- Accepted ADRs müssen im ADR-Index stehen.
- Superseded ADRs müssen auf ersetzendes ADR verweisen.
- ADR-Dateinamen müssen einer Namenskonvention folgen.
- ADR-PRs benötigen CODEOWNER Review.
- Structural ADRs müssen mindestens ein technisches Gate oder eine Begründung haben, warum keins möglich ist.
- ADRs mit Review-Datum in der Vergangenheit werden im CI oder Dashboard markiert.
```

Beispielhafte Script-Prüfung:

```bash
#!/usr/bin/env bash
set -euo pipefail

missing=0

for file in docs/adr/ADR-*.md; do
  grep -q "## Kontext" "$file" || { echo "$file: Kontext fehlt"; missing=1; }
  grep -q "## Entscheidung" "$file" || { echo "$file: Entscheidung fehlt"; missing=1; }
  grep -q "## Konsequenzen" "$file" || { echo "$file: Konsequenzen fehlen"; missing=1; }
  grep -q "Review-Datum" "$file" || { echo "$file: Review-Datum fehlt"; missing=1; }
done

exit "$missing"
```

---

## 29. Migration bestehender Entscheidungen

Bestehende undokumentierte Entscheidungen werden nicht rückwirkend perfekt rekonstruiert. Sie werden pragmatisch nacherfasst.

Priorität:

1. Security-Entscheidungen,
2. Datenbank- und Datenmodellentscheidungen,
3. API- und Event-Verträge,
4. Plattform- und Deploymententscheidungen,
5. Architektur- und Modulgrenzen,
6. Test- und CI/CD-Standards,
7. Framework- und Library-Standards.

Migrationsformat:

```markdown
# ADR-XXX — Nachdokumentierte Entscheidung: <Titel>

## Kontext

Diese Entscheidung wurde vor Einführung des ADR-Prozesses getroffen.
Der ursprüngliche Kontext ist teilweise rekonstruiert.

## Entscheidung

...

## Bekannte Unsicherheiten

...

## Review-Datum

...
```

---

## 30. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Produktions-Hotfix sofort notwendig ist,
- eine Entscheidung rein lokal und leicht rückgängig ist,
- ein Prototyp bewusst nicht produktionsnah ist,
- die Entscheidung bereits durch eine bestehende Guideline vollständig abgedeckt ist,
- ein externer regulatorischer oder vertraglicher Rahmen keine echte Wahl lässt,
- eine technische Entscheidung vorläufig bewusst als Spike markiert ist.

Auch bei Ausnahmen gilt: Sobald aus einem Spike produktiver Code wird, wird die Entscheidung bewertet und bei Bedarf dokumentiert.

---

## 31. Definition of Done

Der Architektur-Entscheidungsprozess ist erfüllt, wenn:

1. Entscheidungsschwellen klar angewendet wurden,
2. relevante Entscheidungen als ADR dokumentiert sind,
3. ADRs im Repository versioniert sind,
4. ADRs per Pull Request reviewed wurden,
5. CODEOWNERS für ADR-Pfade aktiv ist,
6. Branch Protection relevante Reviews und Checks erzwingt,
7. jedes ADR Status, Datum und Review-Datum enthält,
8. Alternativen und Konsequenzen dokumentiert sind,
9. betroffene Teams einbezogen wurden,
10. Akzeptanzkriterien definiert sind,
11. Umsetzung in PRs auf das ADR verweist,
12. strukturelle Entscheidungen soweit möglich technisch abgesichert sind,
13. ADR-Index aktuell ist,
14. veraltete ADRs als Confirmed, Deprecated oder Superseded markiert werden.

---

## 32. Quellen und weiterführende Literatur

- Michael Nygard — Documenting Architecture Decisions: https://www.cognitect.com/blog/2011/11/15/documenting-architecture-decisions
- ADR GitHub Organization — Architectural Decision Records: https://adr.github.io/
- Martin Fowler — Architecture Decision Record: https://martinfowler.com/bliki/ArchitectureDecisionRecord.html
- Thoughtworks Technology Radar — Lightweight Architecture Decision Records: https://www.thoughtworks.com/radar/techniques/lightweight-architecture-decision-records
- Thoughtworks Technology Radar — Lightweight approach to RFCs: https://www.thoughtworks.com/radar/techniques/lightweight-approach-to-rfcs
- Thoughtworks Technology Radar — Architecture advice process: https://www.thoughtworks.com/radar/techniques/architecture-advice-process
- GitHub Docs — Protected Branches: https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches
- GitHub Docs — CODEOWNERS: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners
- Google Cloud / DORA — 2023 State of DevOps Report: https://services.google.com/fh/files/misc/2023_final_report_sodr.pdf
