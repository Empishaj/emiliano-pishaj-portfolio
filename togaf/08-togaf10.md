# TOGAF Migration Planning Coach-Playbook
## Migration Planning, Transition Architectures, Work Packages, Roadmaps, Risiken und Governance

> Dieses Playbook macht Migration Planning praktisch. Es ist für Einsteiger geschrieben, aber so aufgebaut, dass auch erfahrene Enterprise Architects, Solution Architects, Projektleiter, Product Owner, Engineering Manager und Governance-Gremien damit arbeiten können.

> Ziel: Du sollst nach dem Lesen nicht nur wissen, was Migration Planning bedeutet. Du sollst echte Migrationen planen, erklären, steuern, prüfen und verbessern können.


## Kurzcheckliste: Woran du ein gutes Migration Planning erkennst
- [ ] Die Baseline Architecture ist ehrlich und ausreichend vollständig beschrieben.
- [ ] Die Target Architecture ist konkret, messbar und von Stakeholdern genehmigt.
- [ ] Die Gap Analysis zeigt klar, was neu, verändert, behalten oder abgeschaltet wird.
- [ ] Jeder relevante Gap ist einem Work Package zugeordnet.
- [ ] Jedes Work Package hat Zweck, Scope, Nutzen, Aufwand, Risiko, Abhängigkeiten und Verantwortliche.
- [ ] Die Work Packages sind priorisiert und logisch sequenziert.
- [ ] Es gibt stabile Transition Architectures als Zwischenzustände.
- [ ] Für jeden Zwischenzustand ist Interoperabilität geplant.
- [ ] Für jeden kritischen Schritt gibt es Rollback, Testplan und Go/No-Go-Kriterien.
- [ ] Risiken werden nicht versteckt, sondern sichtbar bewertet und aktiv gesteuert.
- [ ] Der Implementierungs- und Migrationsplan ist für Management und Umsetzungsteams verständlich.
- [ ] Architecture Governance begleitet die Umsetzung, ohne sie unnötig zu lähmen.
- [ ] Änderungen während der Migration laufen über klare Change-Regeln.
- [ ] Erfolg wird über KPIs, Capability Increments und Business Outcome gemessen.
- [ ] Nach jeder Welle werden Lessons Learned dokumentiert und in die nächste Welle übernommen.

## 0. Wie du dieses Playbook benutzt

Dieses Playbook ist nicht als Lesebuch gedacht, sondern als Arbeitsbuch. Du kannst es von vorne nach hinten lesen, aber im Alltag wirst du einzelne Abschnitte gezielt verwenden. Wenn du eine Migration vorbereitest, beginne mit Baseline, Target und Gap Analysis. Wenn du mitten in der Umsetzung bist, arbeite mit Work Packages, Transition Architectures, Interoperabilitätsmatrix, Risiko-Register und Governance-Checklisten.

Als Coach würde ich dir sagen: Eine Migration ist kein heroischer Sprung. Eine Migration ist eine kontrollierte Abfolge von belastbaren Zwischenschritten. Wer nur das Ziel beschreibt, hat noch keinen Plan. Wer nur Aufgaben beschreibt, hat noch keine Architektur. Migration Planning verbindet beides.

Die einfachste Leitfrage lautet:

> Wie kommen wir vom heutigen Zustand zum Zielzustand, ohne den laufenden Betrieb, die Daten, die Menschen und die Kontrolle zu verlieren?

## 1. Migration Planning in einem Satz

Migration Planning ist die strukturierte Planung des Weges von der heutigen Architektur zur Zielarchitektur. Dabei werden Arbeitspakete, Zwischenzustände, Abhängigkeiten, Risiken, Kosten, Verantwortlichkeiten und Kontrollpunkte so geordnet, dass die Veränderung umsetzbar, steuerbar und prüfbar wird.

In TOGAF gehört Migration Planning vor allem in Phase F des ADM. Es baut aber stark auf Phase E auf und wirkt direkt in Phase G hinein. Phase E erkennt Chancen und Lösungsoptionen. Phase F macht daraus einen belastbaren Plan. Phase G achtet darauf, dass die Umsetzung der Architektur folgt. Phase H sorgt dafür, dass Änderungen am Plan kontrolliert behandelt werden.

## 2. Die einfache Landkarte: Von Baseline zu Target

Jede Migration beginnt mit zwei Zuständen.

| Aspekt | Details/Erklärung | Beispiel | Umsetzungshinweis |
|---|---|---|---|
| Baseline Architecture | Der heutige Zustand. So arbeiten Systeme, Prozesse, Daten, Menschen und Technologien heute wirklich. | Java-8-Monolith auf On-Premise-Servern mit Oracle-Datenbank. | Nicht beschönigen. Die Baseline ist kein Wunschbild. |
| Target Architecture | Der gewünschte Zielzustand. So soll die Architektur nach der Migration aussehen. | Java-21-Service-Landschaft auf Kubernetes mit PostgreSQL, API-Gateway und Monitoring. | Messbar machen: Verfügbarkeit, Kosten, Time-to-Deploy, Sicherheitsniveau. |
| Gap | Der Unterschied zwischen Baseline und Target. | Oracle wird durch PostgreSQL ersetzt. | Jeder Gap braucht Entscheidung: behalten, verändern, neu bauen oder abschalten. |
| Work Package | Ein umsetzbares Arbeitspaket zur Schließung eines oder mehrerer Gaps. | Datenmigration Oracle zu PostgreSQL. | Nicht zu groß schneiden. 1–3 Monate sind oft gut. |
| Transition Architecture | Ein stabiler Zwischenzustand auf dem Weg zum Ziel. | API-Gateway eingeführt, Monolith läuft aber noch. | Jeder Zwischenzustand muss betreibbar sein. |
| Roadmap | Zeitliche Reihenfolge der Veränderung. | Q1 Infrastruktur, Q2 Datenmigration, Q3 Cutover. | Abhängigkeiten vor Wunschterminen. |

## 3. Warum Migrationen scheitern
| Aspekt | Details/Erklärung | Beispiel | Umsetzungshinweis |
|---|---|---|---|
| Unvollständige Baseline | Das Team kennt nicht alle Systeme, Schnittstellen, Jobs, Datenbanken, Batch-Prozesse oder manuellen Workarounds. | Während der Migration taucht ein unbekannter Datenexport auf. | Vor dem Planen Inventar erzwingen. Keine Migration ohne Architektur- und Systeminventar. |
| Unklare Target Architecture | Das Ziel lautet nur Cloud, modern oder schneller. Das reicht nicht. | Das Team weiß nicht, welche Plattform verbindlich ist. | Target Architecture mit messbaren Qualitätszielen formulieren. |
| Projektplan ohne Architekturplan | Es gibt Termine und Aufgaben, aber keine logischen Architekturzustände. | Meilensteine sind geplant, aber Zwischenzustände nicht betreibbar. | Transition Architectures definieren und mit Work Packages verbinden. |
| Interoperabilität vergessen | Alte und neue Systeme können während der Migration nicht sauber zusammenarbeiten. | Neues CRM spricht nicht mit altem ERP. | Für jeden Zwischenzustand Schnittstellen, Adapter, Datenflüsse und Verantwortliche festlegen. |
| Rollback fehlt | Wenn etwas schiefgeht, kann niemand sauber zurück. | Go-Live scheitert, aber die alte Datenbank ist bereits verändert. | Für jeden kritischen Schritt Rollback-Plan testen. |
| Daten unterschätzt | Datenqualität, Datenmenge, Datenformat und Datenschutz werden zu spät erkannt. | Datenmigration dauert dreimal so lange wie geplant. | Datenmigration als eigenes Work Package behandeln. |
| Menschen vergessen | Technik ist fertig, aber Nutzer, Betrieb und Support sind nicht bereit. | Support kennt die neue Fehlerdiagnose nicht. | Change, Training, Support und Kommunikation in die Roadmap aufnehmen. |
| Zu wenig Governance | Teams verändern Scope, Technologie oder Reihenfolge ohne transparente Entscheidung. | Ein Team ersetzt heimlich eine Plattformkomponente. | Architecture Board, Change Control und Entscheidungspunkte einführen. |
| Zu viel Governance | Jede Kleinigkeit muss in ein Gremium. Die Migration wird langsam und verliert Momentum. | Kleine technische Aufgaben hängen tagelang in Freigaben. | Nur relevante Architekturabweichungen eskalieren. Operative Umsetzung nicht blockieren. |
| Kein messbarer Nutzen | Nach Monaten Arbeit ist unklar, was besser wurde. | Eine Welle ist abgeschlossen, aber niemand sieht Business Value. | Capability Increments und KPIs pro Transition definieren. |

## 4. Migration Planning im TOGAF ADM

Migration Planning ist kein isolierter Termin. Es ist Teil eines Architekturzyklus. Die wichtigsten ADM-Phasen im Zusammenhang sind:

| ADM-Phase | Rolle im Migration Planning | Typische Outputs | Coaching-Hinweis |
|---|---|---|---|
| Preliminary Phase | Legt fest, wie Architekturarbeit grundsätzlich betrieben wird. | Architecture Capability, Governance, Principles, Repository. | Ohne saubere Spielregeln wird Migration später politisch und technisch chaotisch. |
| Phase A – Architecture Vision | Klärt Zweck, Scope, Stakeholder und groben Nutzen. | Architecture Vision, Statement of Architecture Work. | Hier entscheidet sich, ob die Migration ein echtes Business-Problem löst. |
| Phase B – Business Architecture | Beschreibt Geschäftsprozesse, Fähigkeiten, Organisation und Value Streams. | Business Baseline, Business Target, Business Gaps. | Technische Migrationen scheitern oft an Geschäftsprozessen, nicht an Technik. |
| Phase C – Information Systems Architecture | Beschreibt Daten- und Anwendungsebene. | Data Architecture, Application Architecture, System-Gaps. | Hier entstehen meist die kritischen Schnittstellen- und Datenrisiken. |
| Phase D – Technology Architecture | Beschreibt Infrastruktur, Plattformen, Netzwerke und technische Standards. | Technology Architecture, Infrastruktur-Gaps. | Hier wird sichtbar, ob Zielarchitektur betreibbar ist. |
| Phase E – Opportunities & Solutions | Bündelt Gaps zu möglichen Lösungen und ersten Work Packages. | Solution Building Blocks, erste Roadmap, Work Packages. | Phase E sagt: Was könnten wir tun? |
| Phase F – Migration Planning | Macht aus den Optionen einen belastbaren Plan. | Implementation and Migration Plan, Transition Architectures, priorisierte Roadmap. | Phase F sagt: In welcher Reihenfolge tun wir es wirklich? |
| Phase G – Implementation Governance | Überwacht die Umsetzung gegen Architekturentscheidungen. | Architecture Contracts, Compliance Reviews. | Gute Governance verhindert Abweichungen, ohne Umsetzung zu ersticken. |
| Phase H – Architecture Change Management | Behandelt Änderungen an Architektur, Scope und Roadmap. | Change Requests, aktualisierte Roadmap. | Keine Migration überlebt unverändert. Der Umgang mit Änderung ist entscheidend. |

## 5. Die sieben Kernbausteine
| Baustein | Details/Erklärung | Leitfrage | Ergebnis |
|---|---|---|---|
| Baseline Architecture | Der aktuelle Zustand. | Was haben wir heute wirklich? | Systeme, Daten, Prozesse, Schnittstellen, Infrastruktur, Organisation, Verträge. |
| Target Architecture | Der gewünschte Zielzustand. | Wie soll es danach funktionieren? | Zielsysteme, Zielprozesse, Zieltechnologien, Qualitätsziele, Betrieb. |
| Gap Analysis | Vergleich zwischen Ist und Soll. | Was fehlt, was bleibt, was ändert sich, was muss weg? | Gap-Matrix, Gap-Steckbriefe, Bewertung. |
| Work Packages | Umsetzbare Bündel von Arbeit. | Welche Arbeit schließt welche Gaps? | Scope, Aufwand, Kosten, Nutzen, Risiko, Abhängigkeiten. |
| Transition Architectures | Stabile Zwischenzustände. | Wie sieht der Zustand nach Welle 1, 2, 3 aus? | Architekturzustand, KPIs, Interoperabilität, Betriebsfähigkeit. |
| Architecture Roadmap | Zeitliche und logische Reihenfolge. | Was passiert wann und warum? | Sequenz, Meilensteine, Entscheidungsgrenzen. |
| Implementation and Migration Plan | Der Gesamtplan. | Wie wird die Migration umgesetzt und gesteuert? | Zeit, Kosten, Risiken, Governance, Kommunikation, Testing, Rollback. |

## 6. Schritt 1: Baseline dokumentieren

Die Baseline ist der ehrliche Blick auf die Realität. Sie darf unbequem sein. Sie darf Lücken zeigen. Sie darf technische Schulden sichtbar machen. Genau dafür ist sie da.

Als Coach würde ich dich hier bremsen: Starte nicht mit der Roadmap, solange du die Baseline nicht kennst. Sonst planst du auf Vermutungen.

### 6.1 Baseline-Inventar
#### 6.1.1 Applikationen
- [ ] Applikationen: Name erfassen
- [ ] Applikationen: Version erfassen
- [ ] Applikationen: Zweck erfassen
- [ ] Applikationen: Business Owner erfassen
- [ ] Applikationen: Technical Owner erfassen
- [ ] Applikationen: Nutzergruppen erfassen
- [ ] Applikationen: Kritikalität erfassen
- [ ] Applikationen: Technologiestack erfassen
- [ ] Applikationen: Betriebsmodell erfassen
- [ ] Applikationen: Lebenszyklusstatus erfassen

#### 6.1.2 Daten
- [ ] Daten: Datenbanken erfassen
- [ ] Daten: Datenmodelle erfassen
- [ ] Daten: Datenvolumen erfassen
- [ ] Daten: Datenqualität erfassen
- [ ] Daten: Datenklassifikation erfassen
- [ ] Daten: Datenschutzbezug erfassen
- [ ] Daten: Archivierung erfassen
- [ ] Daten: Retention erfassen
- [ ] Daten: Datenflüsse erfassen

#### 6.1.3 Schnittstellen
- [ ] Schnittstellen: Quelle erfassen
- [ ] Schnittstellen: Ziel erfassen
- [ ] Schnittstellen: Protokoll erfassen
- [ ] Schnittstellen: Format erfassen
- [ ] Schnittstellen: Frequenz erfassen
- [ ] Schnittstellen: SLA erfassen
- [ ] Schnittstellen: Fehlerbehandlung erfassen
- [ ] Schnittstellen: Monitoring erfassen
- [ ] Schnittstellen: Owner erfassen

#### 6.1.4 Infrastruktur
- [ ] Infrastruktur: Server erfassen
- [ ] Infrastruktur: Container erfassen
- [ ] Infrastruktur: VMs erfassen
- [ ] Infrastruktur: Cloud Services erfassen
- [ ] Infrastruktur: Netzwerkzonen erfassen
- [ ] Infrastruktur: Firewalls erfassen
- [ ] Infrastruktur: Load Balancer erfassen
- [ ] Infrastruktur: Storage erfassen
- [ ] Infrastruktur: Backup erfassen

#### 6.1.5 Security
- [ ] Security: Identity erfassen
- [ ] Security: Berechtigungen erfassen
- [ ] Security: Verschlüsselung erfassen
- [ ] Security: Logging erfassen
- [ ] Security: Schwachstellen erfassen
- [ ] Security: Secrets erfassen
- [ ] Security: Compliance-Anforderungen erfassen

#### 6.1.6 Organisation
- [ ] Organisation: Teams erfassen
- [ ] Organisation: Rollen erfassen
- [ ] Organisation: Dienstleister erfassen
- [ ] Organisation: Supportmodell erfassen
- [ ] Organisation: Betriebsprozesse erfassen
- [ ] Organisation: Wissensträger erfassen
- [ ] Organisation: Eskalationswege erfassen

#### 6.1.7 Finanzen
- [ ] Finanzen: Betriebskosten erfassen
- [ ] Finanzen: Lizenzen erfassen
- [ ] Finanzen: Wartungsverträge erfassen
- [ ] Finanzen: Cloud-Kosten erfassen
- [ ] Finanzen: Personalkosten erfassen
- [ ] Finanzen: Einsparpotenziale erfassen

### 6.2 Baseline-Steckbrief
```markdown
# System-Steckbrief

| Feld | Inhalt |
|---|---|
| System-ID | SYS-001 |
| Systemname | Beispielsystem |
| Zweck | Welche Geschäftsfähigkeit unterstützt das System? |
| Business Owner | Name / Rolle |
| Technical Owner | Name / Rolle |
| Kritikalität | Critical / High / Medium / Low |
| Nutzer | Rollen, Anzahl, Standorte |
| Technologie | Sprache, Framework, Datenbank, Plattform |
| Betrieb | On-Premise / Cloud / Hybrid |
| Schnittstellen | Eingehend und ausgehend |
| Daten | Datenarten, Volumen, Klassifikation |
| Security | Authentifizierung, Autorisierung, Logging, Schwachstellen |
| Kosten | Betrieb, Lizenz, Wartung |
| Probleme | Bekannte Risiken, EoL, Performance, technische Schulden |
| Migrationsrelevanz | Muss migriert / ersetzt / behalten / abgeschaltet werden |
```

## 7. Schritt 2: Target Architecture definieren

Die Target Architecture ist kein Slogan. Sie ist ein überprüfbarer Zielzustand. Sie muss so konkret sein, dass daraus Work Packages entstehen können.

### 7.1 Qualitätsfragen für Target Architecture
- [ ] Welche Geschäftsziele unterstützt die Target Architecture konkret?
- [ ] Welche Business Capabilities werden verbessert oder neu geschaffen?
- [ ] Welche Systeme werden ersetzt, modernisiert, konsolidiert oder abgeschaltet?
- [ ] Welche Datenstrukturen werden verändert?
- [ ] Welche Schnittstellen werden verändert oder neu aufgebaut?
- [ ] Welche Technologien sind Zielstandard?
- [ ] Welche Technologien sind ausdrücklich nicht mehr erlaubt?
- [ ] Welche Sicherheitsanforderungen gelten?
- [ ] Welche Compliance-Anforderungen müssen erfüllt werden?
- [ ] Welche Betriebskennzahlen gelten im Ziel?
- [ ] Welche Kosten sollen sinken?
- [ ] Welche Time-to-Market-Verbesserung wird erwartet?
- [ ] Welche organisatorischen Änderungen sind notwendig?
- [ ] Welche Skills braucht das Team im Zielbetrieb?
- [ ] Welche Architekturprinzipien müssen eingehalten werden?

### 7.2 Target-Steckbrief
```markdown
# Target Architecture Steckbrief

| Feld | Inhalt |
|---|---|
| Target-ID | TAR-001 |
| Name | Zielarchitektur für ... |
| Zieltermin | Datum / Quartal |
| Business-Zweck | Warum wird dieser Zielzustand gebraucht? |
| Hauptnutzen | Kosten, Geschwindigkeit, Sicherheit, Skalierung, Qualität |
| Zielsysteme | Welche Systeme existieren im Ziel? |
| Zieltechnologien | Welche Plattformen, Datenbanken, Frameworks? |
| Zielprozesse | Welche Prozesse ändern sich? |
| Ziel-Datenflüsse | Wie fließen Daten im Ziel? |
| Ziel-Schnittstellen | APIs, Events, Batch, Files, Streaming |
| Security-Zielbild | IAM, Verschlüsselung, Logging, Monitoring |
| Betriebsmodell | Wer betreibt was? Wie wird supportet? |
| KPIs | Verfügbarkeit, Kosten, Durchlaufzeit, MTTR, Deployment-Frequenz |
| Nicht-Ziele | Was wird bewusst nicht verändert? |
| Annahmen | Welche Annahmen liegen zugrunde? |
| Risiken | Welche Zielrisiken sind bereits bekannt? |
```

## 8. Schritt 3: Gap Analysis durchführen

Die Gap Analysis beantwortet die einfache Frage: Was unterscheidet den heutigen Zustand vom Zielzustand?

Ein häufiger Fehler ist, nur neue Dinge zu betrachten. Das ist zu wenig. Du musst auch betrachten, was wegfällt, was bleibt und was versehentlich vergessen wurde.

### 8.1 Gap-Kategorien
| Gap-Kategorie | Bedeutung | Beispiel | Architekturaktion |
|---|---|---|---|
| Retained | Vorhanden in Baseline und Target. | Ein CRM bleibt bestehen. | Schützen und weiter betreiben. |
| Added | Nicht in Baseline, aber in Target benötigt. | API-Gateway wird neu eingeführt. | Neu aufbauen, kaufen oder integrieren. |
| Eliminated | In Baseline vorhanden, aber in Target nicht mehr benötigt. | Legacy-Server wird abgeschaltet. | Dekommissionierung planen. |
| Transformed | In beiden vorhanden, aber verändert. | Java 8 wird auf Java 21 gehoben. | Migration, Refactoring oder Upgrade planen. |
| Accidentally Eliminated | In Baseline vorhanden und weiterhin nötig, aber in Target vergessen. | Regulatorisches Reporting fehlt im Zielbild. | Sofort klären und Target korrigieren. |

### 8.2 Gap-Matrix-Template
```markdown
| Element | Baseline | Target | Gap-Kategorie | Business Impact | Technischer Impact | Priorität | Verantwortlich |
|---|---|---|---|---|---|---|---|
| System A | Java 8 Monolith | Java 21 Service | Transformed | Hoch | Hoch | 1 | Architekturteam |
| Datenbank B | Oracle 12c | PostgreSQL | Transformed | Mittel | Hoch | 2 | Data Team |
| Reporting C | Vorhanden | Fehlt | Accidentally Eliminated | Kritisch | Mittel | 0 | Business Owner |
```

### 8.3 Gap-Steckbrief
```markdown
# Gap-Steckbrief

| Feld | Inhalt |
|---|---|
| Gap-ID | GAP-001 |
| Name | Kurzname |
| Beschreibung | Was ist der Unterschied zwischen Baseline und Target? |
| Kategorie | Added / Eliminated / Transformed / Retained / Accidentally Eliminated |
| Betroffene Domäne | Business / Data / Application / Technology / Security |
| Business Impact | Was passiert geschäftlich, wenn der Gap nicht geschlossen wird? |
| Technischer Impact | Welche Systeme, Daten, Schnittstellen sind betroffen? |
| Risiko | Initial Risk und mögliche Auswirkungen |
| Aufwand | Grobe Aufwandsschätzung |
| Abhängigkeiten | Welche anderen Gaps hängen daran? |
| Empfohlenes Work Package | WP-ID oder Vorschlag |
| Entscheidung | Umsetzen / verschieben / ablehnen / Target korrigieren |
```

## 9. Schritt 4: Work Packages schneiden

Ein Work Package ist ein steuerbares Arbeitspaket. Es ist größer als eine einzelne Aufgabe, aber kleiner als ein unkontrollierbares Großprojekt. Gute Work Packages sind die Bausteine einer Migration.

### 9.1 Gute Work Packages haben klare Grenzen
- Ein Work Package hat einen klaren Zweck.
- Ein Work Package ist in verständlicher Sprache benannt.
- Ein Work Package schließt einen oder mehrere Gaps.
- Ein Work Package hat eine realistische Dauer.
- Ein Work Package hat messbaren Nutzen.
- Ein Work Package hat klare Abhängigkeiten.
- Ein Work Package hat einen verantwortlichen Owner.
- Ein Work Package hat erkennbare Risiken.
- Ein Work Package hat Test- und Abnahmekriterien.
- Ein Work Package hat einen Rollback- oder Recovery-Ansatz.

### 9.2 Work-Package-Template
```markdown
# Work Package

| Feld | Inhalt |
|---|---|
| WP-ID | ... |
| Name | ... |
| Zweck | ... |
| Beschreibung | ... |
| Geschlossene Gaps | ... |
| Scope | ... |
| Out of Scope | ... |
| Business Value | ... |
| Capability Increment | ... |
| Aufwand | ... |
| Kosten | ... |
| Abhängigkeiten | ... |
| Risiken | ... |
| Testkriterien | ... |
| Abnahmekriterien | ... |
| Rollback | ... |
| Owner | ... |
| Status | ... |
```

### 9.3 Beispiel: Work Package Datenmigration
```markdown
# WP-006 – Datenmigration Oracle zu PostgreSQL

| Feld | Inhalt |
|---|---|
| Zweck | Ablösung der Oracle-Datenbank durch PostgreSQL im Zielbetrieb. |
| Geschlossene Gaps | GAP-003 Datenbankwechsel, GAP-009 Lizenzkostenreduktion |
| Business Value | Reduktion laufender Lizenzkosten, bessere Cloud-Betriebsfähigkeit. |
| Capability Increment | Datenbank kann automatisiert gesichert, skaliert und überwacht werden. |
| Aufwand | 10 Wochen, 2 Entwickler, 1 DBA, 1 Tester. |
| Kosten | 90.000 Euro einmalig, laufend 24.000 Euro pro Jahr. |
| Risiken | Datenverlust, Mapping-Fehler, Performance-Unterschiede. |
| Tests | Testmigration, Datenabgleich, Performance-Test, Restore-Test. |
| Rollback | Oracle bleibt bis zur Abnahme read/write-fähig, PostgreSQL wird zurückgesetzt. |
```

## 10. Schritt 5: Priorisierung und Sequenzierung

Priorisierung sagt, was wichtig ist. Sequenzierung sagt, was zuerst kommen muss. Beides ist nicht dasselbe.

Ein Work Package kann wichtig sein, aber trotzdem später kommen, weil es eine Voraussetzung braucht. Ein anderes kann weniger wichtig sein, aber früh kommen, weil es mehrere spätere Schritte ermöglicht.

### 10.1 Priorisierungskriterien
| Kriterium | Details/Erklärung | Typische Frage | Wirkung auf Reihenfolge |
|---|---|---|---|
| Abhängigkeit | Ein Work Package braucht ein anderes vorher. | Was muss vorher fertig sein? | Sehr stark. |
| Business Value | Das Work Package liefert sichtbaren Geschäftsnutzen. | Was bringt früh Wert? | Stark. |
| Risiko | Das Work Package reduziert oder erzeugt Risiko. | Was müssen wir früh beweisen? | Stark. |
| Dringlichkeit | Regulatorisch, technisch oder vertraglich getrieben. | Was hat Fristdruck? | Sehr stark. |
| Aufwand | Dauer und Ressourcenbedarf. | Was passt in die nächste Welle? | Mittel. |
| Komplexität | Technische und organisatorische Schwierigkeit. | Was braucht mehr Vorbereitung? | Mittel. |
| Lernwert | Das Work Package erzeugt Wissen für spätere Schritte. | Wovon lernen wir am meisten? | Mittel. |
| Stakeholder-Sichtbarkeit | Ergebnis ist für Management oder Nutzer sichtbar. | Was schafft Vertrauen? | Mittel. |

### 10.2 Scoring-Modell
```markdown
| WP-ID | Business Value 1-5 | Risiko-Reduktion 1-5 | Dringlichkeit 1-5 | Aufwand 1-5 umgekehrt | Abhängigkeit frei? | Score |
|---|---:|---:|---:|---:|---|---:|
| WP-001 | 4 | 5 | 5 | 4 | Ja | 18 |
| WP-002 | 3 | 4 | 4 | 5 | Ja | 16 |
| WP-006 | 5 | 4 | 3 | 2 | Nein | 14 |
```

Wichtig: Dieses Scoring ist eine Entscheidungshilfe, kein Automat. Architektururteil bleibt notwendig.

### 10.3 Abhängigkeitsgraph
```text
WP-001 Architekturgrundlage
    ├── WP-002 Netzwerk und Landing Zone
    │       ├── WP-005 Security Baseline
    │       └── WP-006 Datenmigration
    │              └── WP-009 Cutover
    ├── WP-003 CI/CD Pipeline
    │       └── WP-007 Applikationsmodernisierung
    │              └── WP-009 Cutover
    └── WP-004 Monitoring
            └── WP-008 Betriebsübergabe

WP-009 Cutover
    ├── WP-010 Dekommissionierung Legacy
    └── WP-011 Lessons Learned und Architekturupdate
```

## 11. Schritt 6: Transition Architectures definieren

Eine Transition Architecture ist ein stabiler Zwischenzustand. Stabil bedeutet: Der Zustand kann betrieben, überwacht, supportet und erklärt werden. Er ist kein halbfertiger Unfall.

Ein häufiger Fehler ist, Transition Architectures nur als Zeitpunkte zu sehen. Das ist falsch. Eine Transition Architecture ist ein Architekturzustand mit Struktur, Qualität, Fähigkeiten und Grenzen.

### 11.1 Kriterien für gute Transition Architectures
- [ ] Der Zustand ist technisch lauffähig.
- [ ] Der Zustand ist fachlich nutzbar.
- [ ] Der Zustand hat definierte Schnittstellen.
- [ ] Der Zustand hat definierte Betriebsverantwortung.
- [ ] Der Zustand hat definierte Security-Kontrollen.
- [ ] Der Zustand hat messbare KPIs.
- [ ] Der Zustand hat klare Abnahmekriterien.
- [ ] Der Zustand kann überwacht werden.
- [ ] Der Zustand hat einen Rollback- oder Recovery-Ansatz.
- [ ] Der Zustand liefert einen echten Capability Increment.

### 11.2 Transition-Architecture-Template
```markdown
# Transition Architecture

| Feld | Inhalt |
|---|---|
| Transition-ID | ... |
| Name | ... |
| Zieltermin | ... |
| Zweck | ... |
| Enthaltene Work Packages | ... |
| Neue Fähigkeiten | ... |
| Weiterhin alte Komponenten | ... |
| Neue Komponenten | ... |
| Schnittstellen | ... |
| Datenzustand | ... |
| Betrieb | ... |
| Security | ... |
| KPIs | ... |
| Risiken | ... |
| Abnahmekriterien | ... |
| Rollback | ... |
```

### 11.3 Beispiel: Drei Transition Architectures
#### TA-001 – Fundament
Cloud-Landing-Zone, CI/CD, Monitoring und Security-Baseline sind vorhanden. Altsystem läuft weiter. Neue Plattform ist bereit für erste Migrationen.

| Aspekt | Details/Erklärung |
|---|---|
| Transition | TA-001 – Fundament |
| Zweck | Cloud-Landing-Zone, CI/CD, Monitoring und Security-Baseline sind vorhanden. Altsystem läuft weiter. Neue Plattform ist bereit für erste Migrationen. |
| Abnahme | Technische und fachliche Kriterien erfüllt, Monitoring aktiv, Owner bestätigt. |
| Risiko | Restabhängigkeiten sichtbar im Risiko-Register. |
| Nächster Schritt | Vorbereitung der folgenden Migrationswelle. |

#### TA-002 – Parallelbetrieb
Erste Funktionen laufen auf neuer Plattform. Daten werden synchronisiert. API-Gateway vermittelt zwischen alter und neuer Welt.

| Aspekt | Details/Erklärung |
|---|---|
| Transition | TA-002 – Parallelbetrieb |
| Zweck | Erste Funktionen laufen auf neuer Plattform. Daten werden synchronisiert. API-Gateway vermittelt zwischen alter und neuer Welt. |
| Abnahme | Technische und fachliche Kriterien erfüllt, Monitoring aktiv, Owner bestätigt. |
| Risiko | Restabhängigkeiten sichtbar im Risiko-Register. |
| Nächster Schritt | Vorbereitung der folgenden Migrationswelle. |

#### TA-003 – Zielnaher Zustand
Kernfunktionen sind migriert. Legacy-System läuft nur noch für Archiv und Rückfall. Cutover wird vorbereitet.

| Aspekt | Details/Erklärung |
|---|---|
| Transition | TA-003 – Zielnaher Zustand |
| Zweck | Kernfunktionen sind migriert. Legacy-System läuft nur noch für Archiv und Rückfall. Cutover wird vorbereitet. |
| Abnahme | Technische und fachliche Kriterien erfüllt, Monitoring aktiv, Owner bestätigt. |
| Risiko | Restabhängigkeiten sichtbar im Risiko-Register. |
| Nächster Schritt | Vorbereitung der folgenden Migrationswelle. |

## 12. Schritt 7: Interoperabilität sicherstellen

Interoperabilität bedeutet: Systeme, Daten, Prozesse und Organisationseinheiten können während der Migration zusammenarbeiten. Gerade Zwischenzustände brauchen Interoperabilität, weil alte und neue Welt gleichzeitig existieren.

### 12.1 Die vier Ebenen der Interoperabilität
| Ebene | Details/Erklärung | Beispiel |
|---|---|---|
| Business-Interoperabilität | Geschäftsprozesse funktionieren über alte und neue Systeme hinweg. | Auftrag wird im neuen Portal erfasst, aber im alten ERP fakturiert. |
| Daten-Interoperabilität | Daten können korrekt übertragen, synchronisiert und interpretiert werden. | Kundendaten werden zwischen CRM alt und CRM neu synchronisiert. |
| Applikations-Interoperabilität | Anwendungen können miteinander kommunizieren. | API-Gateway übersetzt neue REST-Aufrufe auf alte SOAP-Schnittstelle. |
| Technologie-Interoperabilität | Netzwerk, Identity, Security und Plattformen erlauben Kommunikation. | Cloud-Service kann sicher auf On-Premise-Datenbank zugreifen. |

### 12.2 Interoperabilitätsmatrix
```markdown
| Transition | Quelle | Ziel | Daten/Service | Mechanismus | Owner | Risiko | Test |
|---|---|---|---|---|---|---|---|
| TA-001 | Neue API | Legacy Backend | Auftrag lesen | Adapter REST zu SOAP | Integration Team | Mittel | Integrationstest |
| TA-002 | Neues CRM | Altes ERP | Kundendaten | Event Sync | Data Team | Hoch | Datenabgleich |
| TA-003 | Altes Archiv | Neues Reporting | Historische Daten | Read-only Connector | BI Team | Niedrig | Smoke Test |
```

### 12.3 Typische Interoperabilitätsmuster
| Muster | Details/Erklärung | Wann sinnvoll |
|---|---|---|
| API Gateway | Vermittelt zwischen Konsumenten und Services. | Wenn alte und neue Services gleichzeitig erreichbar sein müssen. |
| Adapter | Übersetzt Protokolle, Formate oder Semantik. | Wenn Legacy SOAP und neue REST-Welt verbunden werden müssen. |
| Event Bridge | Leitet Events zwischen Systemen weiter. | Wenn Systeme lose gekoppelt werden sollen. |
| Database Replication | Synchronisiert Datenbanken. | Wenn paralleler Betrieb mit Datenabgleich nötig ist. |
| Dual Write | Schreibt gleichzeitig in zwei Systeme. | Nur vorsichtig nutzen, da Konsistenz schwierig ist. |
| Read-only Legacy | Altsystem bleibt lesend verfügbar. | Für Archiv, Audit und Rückfall. |
| Feature Flags | Aktiviert Funktionen schrittweise. | Für risikoarme Rollouts. |
| Blue-Green Deployment | Zwei Umgebungen, Umschaltung per Routing. | Für schnelle Rollbacks. |
| Canary Release | Neue Version für kleinen Nutzeranteil. | Wenn Verhalten in Produktion vorsichtig validiert werden soll. |
| Shadow Mode | Neues System läuft mit, beeinflusst aber noch nicht den Betrieb. | Zum Vergleich alter und neuer Logik. |

## 13. Schritt 8: Implementierungs- und Migrationsplan erstellen

Der Implementierungs- und Migrationsplan ist das zentrale Ergebnis von Phase F. Er ist kein reiner Projektplan. Er ist ein Architektur-, Umsetzungs-, Risiko-, Finanz-, Governance- und Kommunikationsplan in einem.

### 13.1 Inhaltsstruktur
| Kapitel | Details/Erklärung |
|---|---|
| Executive Summary | Was wird migriert, warum, bis wann, mit welchem Nutzen und Risiko? |
| Scope und Nicht-Scope | Was gehört zur Migration und was ausdrücklich nicht? |
| Baseline Summary | Kurze Beschreibung des Ist-Zustands. |
| Target Summary | Kurze Beschreibung des Zielzustands. |
| Gap Summary | Wichtigste Unterschiede und daraus abgeleitete Handlungsbedarfe. |
| Work Package Katalog | Alle Arbeitspakete mit Aufwand, Kosten, Nutzen, Risiken und Owner. |
| Transition Architectures | Alle Zwischenzustände mit KPIs und Abnahmekriterien. |
| Roadmap | Zeitliche Reihenfolge und Wellenplanung. |
| Interoperabilitätsplan | Alte und neue Welt in jedem Zwischenzustand. |
| Test- und Cutover-Plan | Wie wird getestet, freigegeben und umgeschaltet? |
| Rollback- und Recovery-Plan | Wie wird bei Problemen zurückgekehrt oder stabilisiert? |
| Risikomanagement | Risiken, Mitigationsmaßnahmen, Residual Risk. |
| Finanzplan | Budget, CapEx, OpEx, Einsparungen, ROI. |
| Ressourcenplan | Teams, Skills, Dienstleister, Verfügbarkeit. |
| Governance | Gremien, Entscheidungsrechte, Eskalation, Reporting. |
| Kommunikationsplan | Wer wird wann wie informiert? |
| Change Management | Training, Support, Akzeptanz, Prozessänderungen. |
| Abnahme | Definition of Done und Erfolgskriterien. |

### 13.2 Roadmap-Template
```markdown
| Welle | Zeitraum | Transition | Work Packages | Capability Increment | Go/No-Go-Kriterium |
|---|---|---|---|---|---|
| 1 | Q1 | TA-001 Fundament | WP-001, WP-002, WP-003 | Plattform betriebsbereit | Security Baseline erfüllt |
| 2 | Q2 | TA-002 Parallelbetrieb | WP-004, WP-005, WP-006 | Erste Funktionen auf Zielplattform | Datenabgleich > 99,9% |
| 3 | Q3 | TA-003 Cutover Ready | WP-007, WP-008 | Kernsystem zielnah | Performance und Regression grün |
| 4 | Q4 | Target | WP-009, WP-010 | Legacy abgeschaltet | Business-Abnahme erteilt |
```

## 14. Schritt 9: Risikomanagement für Migrationen

Migration ohne Risikomanagement ist Hoffnung. Hoffnung ist kein Steuerungsinstrument.

### 14.1 Typische Migrationsrisiken
| Risiko | Details/Erklärung | Gegenmaßnahme |
|---|---|---|
| Datenverlust | Daten gehen bei Migration oder Cutover verloren. | Backup, Testmigration, Validierung, Read-only Freeze. |
| Dateninkonsistenz | Altes und neues System zeigen unterschiedliche Werte. | Synchronisation, Abgleichreports, klare System-of-Record-Regel. |
| Downtime | Systeme sind länger nicht verfügbar als erlaubt. | Wartungsfenster, Blue-Green, Rollback, Lasttests. |
| Performance-Regressions | Neues System ist langsamer. | Performance-Baseline, Lasttest, Tuning vor Go-Live. |
| Schnittstellenbruch | Abhängige Systeme funktionieren nicht mehr. | Interoperabilitätsmatrix, Contract Tests, API-Kompatibilität. |
| Security-Lücke | Neue Plattform ist unsicher konfiguriert. | Security-by-Design, IaC-Scanning, PenTest, Hardening. |
| Compliance-Verstoß | Datenverarbeitung verletzt Anforderungen. | DSGVO-Prüfung, Datenschutzfolgeabschätzung, Audit-Trail. |
| Skill-Lücke | Team hat nicht die nötigen Kenntnisse. | Training, Pairing, externe Unterstützung, Runbooks. |
| Vendor Lock-in | Zielarchitektur erzeugt zu starke Anbieterabhängigkeit. | Exit-Strategie, Standards, Datenexport, Vertragsprüfung. |
| Scope Creep | Migration wächst unkontrolliert. | Change Control, Scope Board, Nicht-Ziele dokumentieren. |
| Budgetüberschreitung | Aufwand wurde unterschätzt. | Puffer, Earned Value, regelmäßige Forecasts. |
| Akzeptanzproblem | Nutzer umgehen neue Lösung. | Change Management, Pilotgruppen, Schulung, Feedbackzyklen. |

### 14.2 Risiko-Register-Template
```markdown
| Risiko-ID | Beschreibung | Wahrscheinlichkeit | Auswirkung | Initial Risk | Maßnahme | Owner | Residual Risk | Status |
|---|---|---|---|---|---|---|---|---|
| R-001 | Datenverlust bei Cutover | Mittel | Hoch | Hoch | Backup, Testmigration, Abgleich | Data Lead | Niedrig | Offen |
| R-002 | Performance zu niedrig | Mittel | Mittel | Mittel | Lasttest, Tuning | Tech Lead | Niedrig | In Arbeit |
```

### 14.3 Go/No-Go-Entscheidung
- [ ] Alle kritischen Tests sind grün.
- [ ] Alle kritischen Risiken haben akzeptiertes Residual Risk.
- [ ] Rollback-Plan ist getestet.
- [ ] Support-Team ist bereit.
- [ ] Monitoring ist aktiv.
- [ ] Business Owner hat Abnahme erteilt.
- [ ] Security hat Freigabe erteilt.
- [ ] Datenvalidierung ist erfolgreich.
- [ ] Kommunikation an Nutzer ist erfolgt.
- [ ] Entscheidung wurde dokumentiert.

## 15. Schritt 10: Governance und Monitoring

Architecture Governance sorgt dafür, dass die Umsetzung nicht vom Zielbild wegdriftet. Sie ist ein Schutzmechanismus für die Investition.

### 15.1 Governance-Struktur
| Gremium/Rolle | Aufgabe | Frequenz | Entscheidungsrechte |
|---|---|---|---|
| Migration Steering Committee | Budget, Scope, Prioritäten, strategische Risiken. | Monatlich oder bei Eskalation. | Go/No-Go, Budget, Scope-Änderung. |
| Architecture Board | Architekturkonformität, Abweichungen, Prinzipien. | Zweiwöchentlich oder nach Bedarf. | Architekturfreigabe, Ausnahmegenehmigung. |
| Program Board | Operative Steuerung der Work Packages. | Wöchentlich. | Reihenfolge, Ressourcen, Blocker. |
| Security Review | Sicherheitsanforderungen und Residual Risks. | Pro Transition und vor Go-Live. | Security-Freigabe oder Blocker. |
| Data Governance Board | Datenqualität, Datenmodell, System of Record. | Pro Datenmigration. | Datenfreigabe, Datenabnahme. |
| Change Advisory Board | Produktionsänderungen, Cutover, Wartungsfenster. | Nach Change-Kalender. | Change-Freigabe. |

### 15.2 KPI-Dashboard
```markdown
| KPI | Zielwert | Aktuell | Trend | Kommentar |
|---|---:|---:|---|---|
| Abgeschlossene Work Packages | 8/12 | 6/12 | ↗ | Zwei Pakete verzögert |
| Erreichte Transition Architectures | 2/3 | 1/3 | → | TA-002 in Abnahme |
| Budgetverbrauch | <= 60% | 58% | ↗ | Im Rahmen |
| Kritische offene Risiken | 0 | 1 | ↘ | Datenrisiko in Klärung |
| Testabdeckung kritischer Prozesse | > 95% | 91% | ↗ | Regression läuft |
| Schnittstellen erfolgreich getestet | 100% | 87% | ↗ | Drei Tests offen |
| Datenvalidierung | > 99,9% | 99,7% | ↗ | Mapping-Korrektur offen |
| Go-Live Readiness | 100% | 74% | ↗ | Support-Schulung offen |
```

## 16. Rollen und Verantwortlichkeiten

Migrationen scheitern oft nicht, weil Menschen unfähig sind, sondern weil Verantwortlichkeiten unklar sind. Deshalb braucht jedes Migration Planning ein klares Rollenmodell.

| Rolle | Details/Erklärung | Hauptbeitrag |
|---|---|---|
| Executive Sponsor | Sichert Mandat, Budget und Management-Rückhalt. | Entscheidet bei strategischen Konflikten. |
| Enterprise Architect | Hält Zielbild, Roadmap, Transition Architectures und Governance zusammen. | Verhindert Architekturdrift. |
| Solution Architect | Gestaltet konkrete Lösung innerhalb eines Work Packages. | Übersetzt Zielarchitektur in umsetzbare Lösung. |
| Business Owner | Verantwortet Geschäftsnutzen und fachliche Abnahme. | Entscheidet, ob Ergebnis wertvoll ist. |
| Product Owner | Priorisiert fachliche Anforderungen in Umsetzung. | Verbindet Business und Delivery. |
| Program Manager | Steuert Zeit, Budget, Abhängigkeiten und Fortschritt. | Sorgt für operative Lieferfähigkeit. |
| Technical Lead | Führt technische Umsetzung. | Sichert technische Qualität. |
| Data Lead | Verantwortet Datenmigration, Datenqualität, Datenmodell. | Verhindert Datenchaos. |
| Security Architect | Prüft Security-Anforderungen und Risiken. | Sichert Security-by-Design. |
| Operations Lead | Sichert Betrieb, Monitoring, Support, Runbooks. | Verhindert Betriebsblindheit. |
| QA/Test Lead | Verantwortet Teststrategie und Abnahmequalität. | Sorgt für Nachweisbarkeit. |
| Change Manager | Plant Kommunikation, Training und Akzeptanz. | Verhindert organisatorisches Scheitern. |

### 16.1 RACI-Beispiel
```markdown
| Aktivität | Sponsor | Enterprise Architect | Program Manager | Business Owner | Tech Lead | Data Lead | Security | Ops |
|---|---|---|---|---|---|---|---|---|
| Target freigeben | A | R | C | A | C | C | C | C |
| Gap Analysis | I | A/R | C | C | R | R | C | C |
| Work Packages definieren | I | A | R | C | R | R | C | C |
| Roadmap erstellen | A | R | R | C | C | C | C | C |
| Datenmigration planen | I | C | C | C | C | A/R | C | C |
| Security Review | I | C | I | I | C | C | A/R | C |
| Go/No-Go | A | R | R | A | C | C | C | C |
| Betrieb übernehmen | I | C | C | C | C | C | C | A/R |
```

## 17. Architekturartefakte im Repository

Migration Planning erzeugt viele Artefakte. Sie gehören nicht in private Ordner. Sie gehören ins Architecture Repository.

| Artefakt | Details/Erklärung | Zeitpunkt |
|---|---|---|
| Baseline Inventory | Systeme, Daten, Schnittstellen, Infrastruktur. | Vor Gap Analysis. |
| Target Architecture Description | Zielbild und Ziel-KPIs. | Vor Work Packages. |
| Gap Matrix | Unterschiede zwischen Baseline und Target. | Nach Phase B/C/D. |
| Gap Steckbriefe | Details kritischer Gaps. | Für Priorisierung. |
| Work Package Catalog | Alle Arbeitspakete. | Für Roadmap. |
| Transition Architecture Descriptions | Zwischenzustände. | Für Governance. |
| Interoperability Matrix | Kommunikation in Zwischenzuständen. | Vor Umsetzung. |
| Risk Register | Initial und Residual Risks. | Laufend. |
| Implementation and Migration Plan | Gesamtplan. | Phase F Abschluss. |
| Architecture Contracts | Vereinbarung zwischen Architektur und Umsetzung. | Phase G. |
| Compliance Review Reports | Prüfung gegen Architektur. | Laufend. |
| Change Requests | Änderungen an Zielbild oder Roadmap. | Phase H. |

## 18. Workshop-Designs

Gute Migration Planning Workshops sind keine PowerPoint-Veranstaltungen. Sie erzeugen Entscheidungen, Artefakte und Klarheit.

### 18.1 Baseline Discovery Workshop
**Ziel:** Ist-Zustand erfassen

**Agenda:**
- Systemliste validieren
- Schnittstellen identifizieren
- Owner klären
- Datenquellen prüfen
- Risiken sammeln

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

### 18.2 Target Alignment Workshop
**Ziel:** Zielbild abstimmen

**Agenda:**
- Business-Ziele prüfen
- Ziel-KPIs festlegen
- Nicht-Ziele klären
- Architekturprinzipien prüfen
- Stakeholder-Abnahme vorbereiten

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

### 18.3 Gap Analysis Workshop
**Ziel:** Gaps identifizieren

**Agenda:**
- Baseline vs. Target vergleichen
- Gaps klassifizieren
- Accidental Eliminations finden
- Impact bewerten
- Prioritäten setzen

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

### 18.4 Work Package Workshop
**Ziel:** Gaps in Arbeitspakete überführen

**Agenda:**
- Gaps bündeln
- Scope schneiden
- Abhängigkeiten klären
- Owner benennen
- Aufwand grob schätzen

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

### 18.5 Roadmap Workshop
**Ziel:** Sequenz und Wellen planen

**Agenda:**
- Abhängigkeitsgraph erstellen
- Wellen definieren
- Transition Architectures benennen
- Go/No-Go-Punkte setzen
- Management-Version vorbereiten

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

### 18.6 Risk Workshop
**Ziel:** Migrationsrisiken bewerten

**Agenda:**
- Risiken sammeln
- Initial Risk bewerten
- Maßnahmen definieren
- Residual Risk akzeptieren
- Owner zuweisen

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

### 18.7 Cutover Readiness Workshop
**Ziel:** Go-Live vorbereiten

**Agenda:**
- Checklisten prüfen
- Rollback testen
- Support vorbereiten
- Kommunikation prüfen
- Go/No-Go entscheiden

**Output:** Dokumentierte Entscheidungen, offene Punkte, Verantwortliche und aktualisierte Artefakte.

## 19. Praxisbeispiel: ERP-Migration

Dieses Beispiel zeigt, wie die Elemente zusammenspielen.

- **Ausgangslage:** SAP R/3 On-Premise, viele Eigenentwicklungen, hohe Betriebskosten, EoL-Risiken.
- **Ziel:** SAP S/4HANA Hybrid mit standardisierten Prozessen, API-Integration und reduziertem Customizing.
- **Haupt-Gaps:** Datenmodell, Custom Code, Schnittstellen, Betriebsmodell, Rollen, Reporting.
- **Work Packages:** Infrastruktur, Datenbereinigung, Custom-Code-Analyse, Schnittstellenmodernisierung, Pilot, Cutover, Dekommissionierung.
- **Transition 1:** S/4HANA Sandbox und Datenanalyse abgeschlossen.
- **Transition 2:** Finance und Einkauf laufen im neuen System, Vertrieb noch alt.
- **Transition 3:** Vollständiger Parallelbetrieb, Altsystem read-only.
- **Target:** S/4HANA produktiv, R/3 abgeschaltet, Archiv verfügbar.
- **Hauptrisiko:** Datenqualität und Prozessakzeptanz.
- **Gegenmaßnahmen:** Testmigrationen, Key-User-Netzwerk, Prozessschulungen, Cutover-Rehearsal.

## 20. Praxisbeispiel: Cloud-Migration
- **Ausgangslage:** Mehrere On-Premise-Anwendungen mit manuellen Deployments und schwacher Skalierung.
- **Ziel:** Cloud-native Plattform mit automatisiertem Deployment, Monitoring und Security Baseline.
- **Haupt-Gaps:** Netzwerk, Identity, IaC, Containerisierung, Logging, Kostenkontrolle.
- **Work Packages:** Landing Zone, IAM, Netzwerk, CI/CD, Observability, Pilot-App, Migrationswellen.
- **Transition 1:** Landing Zone steht, Pilot-App deploybar.
- **Transition 2:** Drei Anwendungen laufen in Cloud, Legacy-Datenbank bleibt on-prem.
- **Transition 3:** Datenbanken migriert, Betriebsmodell umgestellt.
- **Target:** Standardisierte Cloud-Plattform mit Self-Service-Deployment.
- **Hauptrisiko:** Kostenkontrolle und Security Misconfiguration.
- **Gegenmaßnahmen:** Cloud Governance, Budgets, Tagging, Policy-as-Code, Security Scans.

## 21. Praxisbeispiel: Monolith zu modularer Architektur
- **Ausgangslage:** Großer Java-Monolith, langsame Releases, starke Kopplung, wenige Tests.
- **Ziel:** Modularer Monolith oder Services mit klaren Domänengrenzen und CI/CD.
- **Haupt-Gaps:** Domänenmodell, Testbarkeit, Releasefähigkeit, Datenzugriff, Schnittstellen.
- **Work Packages:** Modulgrenzen, Testharness, API-Schicht, Datenzugriff entkoppeln, Strangler-Fig, Release-Pipeline.
- **Transition 1:** Modulgrenzen dokumentiert, neue Tests und Architekturregeln aktiv.
- **Transition 2:** Erste Funktionen laufen über neue API-Schicht.
- **Transition 3:** Kritische Domäne entkoppelt, alte UI nutzt neue API.
- **Target:** Klare Module, schnellere Releases, reduzierte Kopplung.
- **Hauptrisiko:** Zu frühe Microservice-Zerlegung.
- **Gegenmaßnahmen:** Erst Modularisierung, dann gezielte Extraktion.

## 22. Templates für den Alltag
### 22.1 Decision Log
```markdown
# Decision Log

## Entscheidungs-ID
- ...

## Datum
- ...

## Entscheidung
- ...

## Alternativen
- ...

## Begründung
- ...

## Betroffene Work Packages
- ...

## Risiken
- ...

## Owner
- ...

```

### 22.2 Architecture Contract
```markdown
# Architecture Contract

## Ziel
- ...

## Geltungsbereich
- ...

## Architekturprinzipien
- ...

## Verbindliche Standards
- ...

## Akzeptierte Abweichungen
- ...

## Reviewpunkte
- ...

## Abnahmekriterien
- ...

```

### 22.3 Cutover Plan
```markdown
# Cutover Plan

## Zeitfenster
- ...

## Vorbedingungen
- ...

## Schritte
- ...

## Verantwortliche
- ...

## Kommunikation
- ...

## Smoke Tests
- ...

## Rollback-Punkt
- ...

## Go/No-Go
- ...

```

### 22.4 Rollback Plan
```markdown
# Rollback Plan

## Auslöser
- ...

## Entscheider
- ...

## Technische Schritte
- ...

## Datenrücksetzung
- ...

## Kommunikation
- ...

## Zeitlimit
- ...

## Nachprüfung
- ...

```

### 22.5 Runbook
```markdown
# Runbook

## System
- ...

## Start/Stop
- ...

## Monitoring
- ...

## Alarme
- ...

## Standardfehler
- ...

## Recovery
- ...

## Eskalation
- ...

## Kontakte
- ...

```

### 22.6 Lessons Learned
```markdown
# Lessons Learned

## Was war geplant?
- ...

## Was ist passiert?
- ...

## Was hat funktioniert?
- ...

## Was hat nicht funktioniert?
- ...

## Was ändern wir?
- ...

## Owner
- ...

## Termin
- ...

```

## 23. 30-60-90-Tage-Plan für Migration Planning

### 23.1 Erste 30 Tage: Verstehen und stabilisieren
- [ ] Mandat klären: Wer will die Migration und warum?
- [ ] Stakeholder identifizieren.
- [ ] Scope und Nicht-Scope dokumentieren.
- [ ] Baseline-Inventar starten.
- [ ] Kritische Systeme und Schnittstellen priorisieren.
- [ ] Bestehende Architekturartefakte einsammeln.
- [ ] Business-Ziele in messbare Zielgrößen übersetzen.
- [ ] Top-10-Risiken sammeln.
- [ ] Governance-Struktur vorschlagen.
- [ ] Ersten Plan für Workshops erstellen.

### 23.2 Tage 31 bis 60: Strukturieren und entscheiden
- [ ] Baseline validieren.
- [ ] Target Architecture schärfen.
- [ ] Gap Analysis durchführen.
- [ ] Gaps bewerten und priorisieren.
- [ ] Work Packages schneiden.
- [ ] Abhängigkeiten modellieren.
- [ ] Transition Architectures entwerfen.
- [ ] Interoperabilitätsbedarf identifizieren.
- [ ] Risikoregister aufbauen.
- [ ] Erste Roadmap-Version erstellen.

### 23.3 Tage 61 bis 90: Plan finalisieren und Governance starten
- [ ] Work Packages mit Ownern und Aufwand versehen.
- [ ] Roadmap mit Stakeholdern abstimmen.
- [ ] Transition Architectures finalisieren.
- [ ] Test-, Cutover- und Rollback-Struktur definieren.
- [ ] Finanzplan und Budgetpuffer einarbeiten.
- [ ] Governance-Meetings starten.
- [ ] Architecture Contract vorbereiten.
- [ ] Go/No-Go-Kriterien je Welle definieren.
- [ ] Management Summary erstellen.
- [ ] Phase G Übergabe vorbereiten.

## 24. Coach-Fragen für Führungskräfte
1. Welches Business-Problem löst diese Migration wirklich?
2. Was passiert, wenn wir die Migration nicht durchführen?
3. Welche Kosten verursacht der Status quo?
4. Welche Risiken entstehen durch den Status quo?
5. Welche Risiken erzeugt die Migration selbst?
6. Welche Entscheidungen müssen auf Managementebene getroffen werden?
7. Welche Teile der Migration sind nicht verhandelbar?
8. Welche Teile der Migration sind Wunsch, aber kein Muss?
9. Welche Ziel-KPIs sind wirklich geschäftsrelevant?
10. Welche Zwischenzustände können wir dem Management erklären?
11. Welche Abhängigkeiten können den Termin gefährden?
12. Welche Skills fehlen im Team?
13. Welche Dienstleisterabhängigkeiten bestehen?
14. Welche Verträge oder Lizenzen beeinflussen die Roadmap?
15. Welche regulatorischen Anforderungen müssen berücksichtigt werden?
16. Welche Sicherheitsanforderungen dürfen nicht verschoben werden?
17. Welche Nutzergruppen sind am stärksten betroffen?
18. Welche Kommunikationsfehler dürfen wir nicht machen?
19. Wann stoppen wir einen Go-Live?
20. Wer darf einen Go-Live stoppen?
21. Welche Daten dürfen niemals verloren gehen?
22. Welche Schnittstelle ist am kritischsten?
23. Welche Annahme ist am gefährlichsten?
24. Was ist unser Plan B?
25. Wann ist die Migration wirklich fertig?

## 25. Coach-Fragen für Architekten
1. Ist die Baseline ausreichend belegt oder nur angenommen?
2. Ist die Target Architecture präzise genug für Umsetzung?
3. Welche Gaps wurden möglicherweise übersehen?
4. Welche Baseline-Komponente wurde versehentlich aus der Target entfernt?
5. Sind Work Packages logisch geschnitten?
6. Ist jedes Work Package einem Capability Increment zugeordnet?
7. Ist die Roadmap durch Abhängigkeiten oder durch Wunschtermine getrieben?
8. Kann jede Transition Architecture betrieben werden?
9. Sind alte und neue Welt in jeder Transition interoperabel?
10. Sind Datenflüsse in jedem Zwischenzustand klar?
11. Gibt es einen System of Record pro Datenobjekt?
12. Gibt es temporäre technische Schulden in Transition States?
13. Sind diese temporären Schulden mit Ablaufdatum versehen?
14. Sind Architecture Principles verletzt?
15. Falls ja: Ist die Abweichung genehmigt?
16. Sind Security Controls in jedem Zustand aktiv?
17. Ist Logging während der Migration ausreichend?
18. Sind Deployment- und Rollback-Mechanismen getestet?
19. Gibt es messbare Abnahmekriterien?
20. Kann ein Auditor die Entscheidungskette nachvollziehen?

## 26. Coach-Fragen für Engineering Teams
1. Was genau bauen wir in diesem Work Package?
2. Welchen Gap schließen wir damit?
3. Was gehört ausdrücklich nicht dazu?
4. Welche Abhängigkeiten blockieren uns?
5. Welche Schnittstellen müssen stabil bleiben?
6. Welche Daten werden verändert?
7. Welche Tests beweisen, dass die Migration funktioniert?
8. Wie erkennen wir früh, dass etwas schiefgeht?
9. Wie rollen wir zurück?
10. Wer entscheidet bei Problemen?
11. Welche Metriken beobachten wir während des Rollouts?
12. Welche Feature Flags brauchen wir?
13. Welche Runbooks müssen wir schreiben?
14. Welche Alarme müssen eingerichtet sein?
15. Was muss der Support wissen?
16. Was müssen Nutzer wissen?
17. Welche technischen Schulden nehmen wir temporär auf?
18. Wann entfernen wir temporäre Adapter?
19. Welche Dokumentation aktualisieren wir?
20. Was lernen wir für die nächste Welle?

## 27. Definition of Done für Migration Planning
- [ ] Baseline Architecture ist dokumentiert und validiert.
- [ ] Target Architecture ist dokumentiert und genehmigt.
- [ ] Gap Analysis ist vollständig genug für Planung.
- [ ] Alle kritischen Gaps haben Entscheidung und Owner.
- [ ] Work Package Katalog ist erstellt.
- [ ] Work Packages sind priorisiert und sequenziert.
- [ ] Abhängigkeiten sind sichtbar.
- [ ] Transition Architectures sind definiert.
- [ ] Interoperabilitätsplan ist erstellt.
- [ ] Risikoregister ist erstellt.
- [ ] Teststrategie ist definiert.
- [ ] Cutover-Strategie ist definiert.
- [ ] Rollback-Strategie ist definiert.
- [ ] Finanzplan ist erstellt.
- [ ] Ressourcenplan ist erstellt.
- [ ] Governance-Struktur ist aktiv.
- [ ] Kommunikationsplan ist vorbereitet.
- [ ] Management hat den Plan genehmigt.
- [ ] Phase G kann starten.
- [ ] Architecture Repository ist aktualisiert.

## 28. Häufige Fehler und Korrekturen
| Fehler | Warum problematisch | Korrektur |
|---|---|---|
| Migration mit Modernisierung verwechseln | Nicht jede Migration muss alles verbessern. Manchmal ist Stabilisierung wichtiger. | Ziel klar trennen: Lift, Shift, Transform, Replace. |
| Alles in eine Welle packen | Große Wellen erhöhen Risiko. | In Transition Architectures schneiden. |
| Datenmigration als Nebenaufgabe behandeln | Daten sind meist der kritischste Teil. | Eigenes Work Package mit Data Lead. |
| Keine Nicht-Ziele definieren | Scope wächst unkontrolliert. | Out-of-Scope-Liste pflegen. |
| Keine temporären Zustände planen | Die Realität besteht während Migration aus Mischzuständen. | Jeden Zwischenzustand entwerfen. |
| Altsystem zu früh abschalten | Versteckte Abhängigkeiten brechen. | Read-only-Phase und Monitoring einplanen. |
| Dekommissionierung vergessen | Kosten und Risiken bleiben. | Legacy-Abschaltung als eigenes Work Package. |
| Support zu spät einbinden | Betrieb ist unvorbereitet. | Ops ab Baseline einbeziehen. |
| Security erst vor Go-Live prüfen | Späte Findings sind teuer. | Security Gates pro Transition. |
| Kein Lernen zwischen Wellen | Fehler wiederholen sich. | Lessons Learned verpflichtend machen. |

## 29. Entscheidungsregeln
1. Abhängigkeiten schlagen Wunschtermine.
2. Datenintegrität schlägt Geschwindigkeit.
3. Betriebsfähigkeit schlägt Feature-Umfang.
4. Rollback-Fähigkeit schlägt Optimismus.
5. Klarer Scope schlägt maximale Ambition.
6. Kleine stabile Zwischenzustände schlagen große unsichere Sprünge.
7. Messbare Ziel-KPIs schlagen vage Modernisierung.
8. Explizite Risiken schlagen stille Annahmen.
9. Dokumentierte Abweichungen schlagen versteckte Umgehungen.
10. Lernende Roadmap schlägt starren Plan.

## 30. Mini-Glossar
| Begriff | Erklärung |
|---|---|
| Baseline Architecture | Der dokumentierte Ist-Zustand der Architektur. |
| Target Architecture | Der dokumentierte Zielzustand der Architektur. |
| Gap | Unterschied zwischen Ist- und Zielzustand. |
| Work Package | Arbeitspaket zur Umsetzung eines Teils der Veränderung. |
| Transition Architecture | Stabiler Zwischenzustand zwischen Baseline und Target. |
| Capability Increment | Messbarer Fähigkeitszuwachs durch Umsetzung. |
| Roadmap | Zeitlich und logisch geordneter Veränderungsplan. |
| Cutover | Der Umschaltmoment von alt nach neu. |
| Rollback | Rückkehr zu einem vorherigen stabilen Zustand. |
| Interoperabilität | Fähigkeit unterschiedlicher Systeme oder Bereiche, zusammenzuarbeiten. |
| Architecture Governance | Regeln, Rollen und Prüfungen zur Einhaltung der Architektur. |
| Architecture Contract | Verbindliche Vereinbarung zwischen Architektur und Umsetzung. |
| Residual Risk | Restrisiko nach Gegenmaßnahmen. |
| Go/No-Go | Formale Entscheidung, ob ein kritischer Schritt gestartet wird. |
| Dekommissionierung | Kontrollierte Abschaltung alter Systeme. |

## 31. 365 Coach-Karten für Migration Planning

Die folgenden Coach-Karten sind bewusst kurz. Sie eignen sich für Workshops, Daily-Impulse, Review-Fragen, Schulungen und als Reflexionsmaterial.

### Coach-Karte 001: Baseline

**Merksatz:** Beschreibe, was wirklich existiert, nicht was offiziell existieren sollte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 002: Baseline

**Merksatz:** Suche nach Schattenprozessen, Excel-Dateien und manuellen Workarounds.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 003: Baseline

**Merksatz:** Eine unbekannte Schnittstelle kann eine ganze Migration kippen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 004: Baseline

**Merksatz:** Inventar ohne Owner ist nur eine Liste, keine Architekturgrundlage.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 005: Baseline

**Merksatz:** Kritikalität muss fachlich und technisch bewertet werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 006: Baseline

**Merksatz:** Alte Systeme haben oft versteckte Geschäftsfunktionen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 007: Baseline

**Merksatz:** Sprich mit Betrieb, nicht nur mit Projektteams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 008: Baseline

**Merksatz:** Prüfe Logs, Jobs, Batch-Läufe und Cronjobs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 009: Baseline

**Merksatz:** Datenqualität ist Teil der Baseline.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 010: Baseline

**Merksatz:** Kosten des Status quo machen Migration entscheidbar.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 011: Target

**Merksatz:** Ein Ziel ohne KPI ist ein Wunsch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 012: Target

**Merksatz:** Target Architecture muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 013: Target

**Merksatz:** Nicht-Ziele schützen die Roadmap.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 014: Target

**Merksatz:** Zieltechnologien brauchen Skills und Betriebskonzept.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 015: Target

**Merksatz:** Sicherheit gehört in das Zielbild, nicht ans Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 016: Target

**Merksatz:** Target muss Geschäftsziele sichtbar unterstützen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 017: Target

**Merksatz:** Je unklarer das Ziel, desto politischer die Migration.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 018: Target

**Merksatz:** Ein Zielbild darf ambitioniert sein, aber nicht neblig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 019: Target

**Merksatz:** Zielarchitektur ohne Datenarchitektur ist unvollständig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 020: Target

**Merksatz:** Target entscheidet, welche Gaps relevant sind.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 021: Gap Analysis

**Merksatz:** Gaps sind keine Schuldzuweisung, sondern Arbeitsmaterial.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 022: Gap Analysis

**Merksatz:** Ein Gap ohne Entscheidung bleibt Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 023: Gap Analysis

**Merksatz:** Accidentally Eliminated ist die gefährlichste Kategorie.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 024: Gap Analysis

**Merksatz:** Nicht alles, was alt ist, muss weg.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 025: Gap Analysis

**Merksatz:** Nicht alles, was neu ist, ist wertvoll.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 026: Gap Analysis

**Merksatz:** Jeder Gap braucht Impact-Bewertung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 027: Gap Analysis

**Merksatz:** Gaps müssen in Arbeit übersetzt werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 028: Gap Analysis

**Merksatz:** Eine Gap-Matrix schafft gemeinsame Sprache.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 029: Gap Analysis

**Merksatz:** Gaps ohne Owner verschwinden nicht.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 030: Gap Analysis

**Merksatz:** Business Impact schlägt technische Eleganz.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 031: Work Packages

**Merksatz:** Ein Work Package braucht ein klares Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 032: Work Packages

**Merksatz:** Zu große Work Packages verstecken Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 033: Work Packages

**Merksatz:** Zu kleine Work Packages erzeugen Steuerungsaufwand.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 034: Work Packages

**Merksatz:** Ein Work Package ohne Nutzen ist verdächtig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 035: Work Packages

**Merksatz:** Jedes Work Package braucht Abhängigkeiten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 036: Work Packages

**Merksatz:** Ein Owner ist keine Mailingliste.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 037: Work Packages

**Merksatz:** Rollback gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 038: Work Packages

**Merksatz:** Tests gehören ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 039: Work Packages

**Merksatz:** Security gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 040: Work Packages

**Merksatz:** Doku gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 041: Roadmap

**Merksatz:** Roadmaps werden aus Abhängigkeiten gebaut, nicht aus Wunschdaten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 042: Roadmap

**Merksatz:** Früher sichtbarer Nutzen schafft Vertrauen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 043: Roadmap

**Merksatz:** Eine Roadmap braucht Entscheidungspunkte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 044: Roadmap

**Merksatz:** Eine Welle muss in sich Sinn ergeben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 045: Roadmap

**Merksatz:** Puffer ist kein Luxus, sondern Risikobehandlung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 046: Roadmap

**Merksatz:** Nicht jede Parallelisierung spart Zeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 047: Roadmap

**Merksatz:** Sequenzierung ist Architekturarbeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 048: Roadmap

**Merksatz:** Ein kritischer Pfad muss sichtbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 049: Roadmap

**Merksatz:** Roadmaps müssen lernfähig bleiben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 050: Roadmap

**Merksatz:** Management braucht eine andere Sicht als Teams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 051: Transition

**Merksatz:** Ein Zwischenzustand muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 052: Transition

**Merksatz:** Halbfertig ist keine Transition Architecture.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 053: Transition

**Merksatz:** Jede Transition braucht KPIs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 054: Transition

**Merksatz:** Jede Transition braucht Abnahme.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 055: Transition

**Merksatz:** Jede Transition braucht Support.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 056: Transition

**Merksatz:** Jede Transition braucht Security-Kontrollen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 057: Transition

**Merksatz:** Jede Transition braucht Datenregeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 058: Transition

**Merksatz:** Jede Transition braucht Interoperabilität.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 059: Transition

**Merksatz:** Jede Transition braucht Rollback oder Recovery.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 060: Transition

**Merksatz:** Jede Transition muss erklärbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 061: Interoperability

**Merksatz:** Alte und neue Welt müssen miteinander sprechen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 062: Interoperability

**Merksatz:** Adapter sind temporär, aber brauchen Owner.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 063: Interoperability

**Merksatz:** Temporär ohne Ablaufdatum wird dauerhaft.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 064: Interoperability

**Merksatz:** Daten-Synchronisation ist kein Detail.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 065: Interoperability

**Merksatz:** System of Record muss pro Datenobjekt klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 066: Interoperability

**Merksatz:** Schnittstellen brauchen Contract Tests.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 067: Interoperability

**Merksatz:** API-Gateways können Migration entkoppeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 068: Interoperability

**Merksatz:** Dual Write ist gefährlich und braucht Kontrolle.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 069: Interoperability

**Merksatz:** Read-only Legacy kann Risiko reduzieren.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 070: Interoperability

**Merksatz:** Interoperabilität muss pro Transition geprüft werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 071: Risk

**Merksatz:** Risiken werden nicht kleiner, wenn man sie nicht aufschreibt.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 072: Risk

**Merksatz:** Initial Risk und Residual Risk trennen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 073: Risk

**Merksatz:** Jede Maßnahme muss einem Risiko zugeordnet sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 074: Risk

**Merksatz:** Ein akzeptiertes Risiko braucht Entscheider.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 075: Risk

**Merksatz:** Go-Live-Risiken brauchen Go/No-Go-Kriterien.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 076: Risk

**Merksatz:** Datenrisiken sind oft geschäftskritisch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 077: Risk

**Merksatz:** Security-Risiken entstehen auch durch Übergangszustände.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 078: Risk

**Merksatz:** Skill-Risiken sind echte Projektrisiken.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 079: Risk

**Merksatz:** Budgetrisiken brauchen Forecasts.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 080: Risk

**Merksatz:** Scope-Risiken brauchen Change Control.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 081: Governance

**Merksatz:** Governance schützt Ziel und Investition.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 082: Governance

**Merksatz:** Zu wenig Governance erzeugt Drift.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 083: Governance

**Merksatz:** Zu viel Governance erzeugt Umgehung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 084: Governance

**Merksatz:** Entscheidungsrechte müssen vor Konflikten klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 085: Governance

**Merksatz:** Architecture Board prüft relevante Abweichungen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 086: Governance

**Merksatz:** Nicht jede operative Frage braucht ein Gremium.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 087: Governance

**Merksatz:** Architecture Contracts schaffen Verbindlichkeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 088: Governance

**Merksatz:** Compliance Reviews brauchen klare Kriterien.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 089: Governance

**Merksatz:** Eskalationswege müssen bekannt sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 090: Governance

**Merksatz:** Governance muss Umsetzung ermöglichen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 091: Cutover

**Merksatz:** Cutover ist kein einzelner Termin, sondern ein Ablauf.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 092: Cutover

**Merksatz:** Cutover braucht Generalprobe.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 093: Cutover

**Merksatz:** Smoke Tests müssen vorab definiert sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 094: Cutover

**Merksatz:** Rollback-Zeitfenster muss bekannt sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 095: Cutover

**Merksatz:** Kommunikation entscheidet über Ruhe im Go-Live.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 096: Cutover

**Merksatz:** Support muss vor Cutover bereit sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 097: Cutover

**Merksatz:** Daten-Freeze muss geplant werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 098: Cutover

**Merksatz:** Entscheider müssen erreichbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 099: Cutover

**Merksatz:** Monitoring muss live sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 100: Cutover

**Merksatz:** No-Go ist eine professionelle Option.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 101: Baseline

**Merksatz:** Beschreibe, was wirklich existiert, nicht was offiziell existieren sollte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 102: Baseline

**Merksatz:** Suche nach Schattenprozessen, Excel-Dateien und manuellen Workarounds.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 103: Baseline

**Merksatz:** Eine unbekannte Schnittstelle kann eine ganze Migration kippen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 104: Baseline

**Merksatz:** Inventar ohne Owner ist nur eine Liste, keine Architekturgrundlage.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 105: Baseline

**Merksatz:** Kritikalität muss fachlich und technisch bewertet werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 106: Baseline

**Merksatz:** Alte Systeme haben oft versteckte Geschäftsfunktionen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 107: Baseline

**Merksatz:** Sprich mit Betrieb, nicht nur mit Projektteams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 108: Baseline

**Merksatz:** Prüfe Logs, Jobs, Batch-Läufe und Cronjobs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 109: Baseline

**Merksatz:** Datenqualität ist Teil der Baseline.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 110: Baseline

**Merksatz:** Kosten des Status quo machen Migration entscheidbar.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 111: Target

**Merksatz:** Ein Ziel ohne KPI ist ein Wunsch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 112: Target

**Merksatz:** Target Architecture muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 113: Target

**Merksatz:** Nicht-Ziele schützen die Roadmap.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 114: Target

**Merksatz:** Zieltechnologien brauchen Skills und Betriebskonzept.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 115: Target

**Merksatz:** Sicherheit gehört in das Zielbild, nicht ans Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 116: Target

**Merksatz:** Target muss Geschäftsziele sichtbar unterstützen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 117: Target

**Merksatz:** Je unklarer das Ziel, desto politischer die Migration.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 118: Target

**Merksatz:** Ein Zielbild darf ambitioniert sein, aber nicht neblig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 119: Target

**Merksatz:** Zielarchitektur ohne Datenarchitektur ist unvollständig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 120: Target

**Merksatz:** Target entscheidet, welche Gaps relevant sind.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 121: Gap Analysis

**Merksatz:** Gaps sind keine Schuldzuweisung, sondern Arbeitsmaterial.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 122: Gap Analysis

**Merksatz:** Ein Gap ohne Entscheidung bleibt Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 123: Gap Analysis

**Merksatz:** Accidentally Eliminated ist die gefährlichste Kategorie.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 124: Gap Analysis

**Merksatz:** Nicht alles, was alt ist, muss weg.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 125: Gap Analysis

**Merksatz:** Nicht alles, was neu ist, ist wertvoll.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 126: Gap Analysis

**Merksatz:** Jeder Gap braucht Impact-Bewertung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 127: Gap Analysis

**Merksatz:** Gaps müssen in Arbeit übersetzt werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 128: Gap Analysis

**Merksatz:** Eine Gap-Matrix schafft gemeinsame Sprache.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 129: Gap Analysis

**Merksatz:** Gaps ohne Owner verschwinden nicht.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 130: Gap Analysis

**Merksatz:** Business Impact schlägt technische Eleganz.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 131: Work Packages

**Merksatz:** Ein Work Package braucht ein klares Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 132: Work Packages

**Merksatz:** Zu große Work Packages verstecken Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 133: Work Packages

**Merksatz:** Zu kleine Work Packages erzeugen Steuerungsaufwand.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 134: Work Packages

**Merksatz:** Ein Work Package ohne Nutzen ist verdächtig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 135: Work Packages

**Merksatz:** Jedes Work Package braucht Abhängigkeiten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 136: Work Packages

**Merksatz:** Ein Owner ist keine Mailingliste.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 137: Work Packages

**Merksatz:** Rollback gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 138: Work Packages

**Merksatz:** Tests gehören ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 139: Work Packages

**Merksatz:** Security gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 140: Work Packages

**Merksatz:** Doku gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 141: Roadmap

**Merksatz:** Roadmaps werden aus Abhängigkeiten gebaut, nicht aus Wunschdaten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 142: Roadmap

**Merksatz:** Früher sichtbarer Nutzen schafft Vertrauen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 143: Roadmap

**Merksatz:** Eine Roadmap braucht Entscheidungspunkte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 144: Roadmap

**Merksatz:** Eine Welle muss in sich Sinn ergeben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 145: Roadmap

**Merksatz:** Puffer ist kein Luxus, sondern Risikobehandlung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 146: Roadmap

**Merksatz:** Nicht jede Parallelisierung spart Zeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 147: Roadmap

**Merksatz:** Sequenzierung ist Architekturarbeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 148: Roadmap

**Merksatz:** Ein kritischer Pfad muss sichtbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 149: Roadmap

**Merksatz:** Roadmaps müssen lernfähig bleiben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 150: Roadmap

**Merksatz:** Management braucht eine andere Sicht als Teams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 151: Transition

**Merksatz:** Ein Zwischenzustand muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 152: Transition

**Merksatz:** Halbfertig ist keine Transition Architecture.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 153: Transition

**Merksatz:** Jede Transition braucht KPIs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 154: Transition

**Merksatz:** Jede Transition braucht Abnahme.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 155: Transition

**Merksatz:** Jede Transition braucht Support.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 156: Transition

**Merksatz:** Jede Transition braucht Security-Kontrollen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 157: Transition

**Merksatz:** Jede Transition braucht Datenregeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 158: Transition

**Merksatz:** Jede Transition braucht Interoperabilität.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 159: Transition

**Merksatz:** Jede Transition braucht Rollback oder Recovery.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 160: Transition

**Merksatz:** Jede Transition muss erklärbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 161: Interoperability

**Merksatz:** Alte und neue Welt müssen miteinander sprechen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 162: Interoperability

**Merksatz:** Adapter sind temporär, aber brauchen Owner.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 163: Interoperability

**Merksatz:** Temporär ohne Ablaufdatum wird dauerhaft.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 164: Interoperability

**Merksatz:** Daten-Synchronisation ist kein Detail.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 165: Interoperability

**Merksatz:** System of Record muss pro Datenobjekt klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 166: Interoperability

**Merksatz:** Schnittstellen brauchen Contract Tests.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 167: Interoperability

**Merksatz:** API-Gateways können Migration entkoppeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 168: Interoperability

**Merksatz:** Dual Write ist gefährlich und braucht Kontrolle.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 169: Interoperability

**Merksatz:** Read-only Legacy kann Risiko reduzieren.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 170: Interoperability

**Merksatz:** Interoperabilität muss pro Transition geprüft werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 171: Risk

**Merksatz:** Risiken werden nicht kleiner, wenn man sie nicht aufschreibt.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 172: Risk

**Merksatz:** Initial Risk und Residual Risk trennen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 173: Risk

**Merksatz:** Jede Maßnahme muss einem Risiko zugeordnet sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 174: Risk

**Merksatz:** Ein akzeptiertes Risiko braucht Entscheider.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 175: Risk

**Merksatz:** Go-Live-Risiken brauchen Go/No-Go-Kriterien.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 176: Risk

**Merksatz:** Datenrisiken sind oft geschäftskritisch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 177: Risk

**Merksatz:** Security-Risiken entstehen auch durch Übergangszustände.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 178: Risk

**Merksatz:** Skill-Risiken sind echte Projektrisiken.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 179: Risk

**Merksatz:** Budgetrisiken brauchen Forecasts.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 180: Risk

**Merksatz:** Scope-Risiken brauchen Change Control.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 181: Governance

**Merksatz:** Governance schützt Ziel und Investition.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 182: Governance

**Merksatz:** Zu wenig Governance erzeugt Drift.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 183: Governance

**Merksatz:** Zu viel Governance erzeugt Umgehung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 184: Governance

**Merksatz:** Entscheidungsrechte müssen vor Konflikten klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 185: Governance

**Merksatz:** Architecture Board prüft relevante Abweichungen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 186: Governance

**Merksatz:** Nicht jede operative Frage braucht ein Gremium.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 187: Governance

**Merksatz:** Architecture Contracts schaffen Verbindlichkeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 188: Governance

**Merksatz:** Compliance Reviews brauchen klare Kriterien.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 189: Governance

**Merksatz:** Eskalationswege müssen bekannt sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 190: Governance

**Merksatz:** Governance muss Umsetzung ermöglichen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 191: Cutover

**Merksatz:** Cutover ist kein einzelner Termin, sondern ein Ablauf.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 192: Cutover

**Merksatz:** Cutover braucht Generalprobe.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 193: Cutover

**Merksatz:** Smoke Tests müssen vorab definiert sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 194: Cutover

**Merksatz:** Rollback-Zeitfenster muss bekannt sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 195: Cutover

**Merksatz:** Kommunikation entscheidet über Ruhe im Go-Live.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 196: Cutover

**Merksatz:** Support muss vor Cutover bereit sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 197: Cutover

**Merksatz:** Daten-Freeze muss geplant werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 198: Cutover

**Merksatz:** Entscheider müssen erreichbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 199: Cutover

**Merksatz:** Monitoring muss live sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 200: Cutover

**Merksatz:** No-Go ist eine professionelle Option.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 201: Baseline

**Merksatz:** Beschreibe, was wirklich existiert, nicht was offiziell existieren sollte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 202: Baseline

**Merksatz:** Suche nach Schattenprozessen, Excel-Dateien und manuellen Workarounds.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 203: Baseline

**Merksatz:** Eine unbekannte Schnittstelle kann eine ganze Migration kippen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 204: Baseline

**Merksatz:** Inventar ohne Owner ist nur eine Liste, keine Architekturgrundlage.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 205: Baseline

**Merksatz:** Kritikalität muss fachlich und technisch bewertet werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 206: Baseline

**Merksatz:** Alte Systeme haben oft versteckte Geschäftsfunktionen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 207: Baseline

**Merksatz:** Sprich mit Betrieb, nicht nur mit Projektteams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 208: Baseline

**Merksatz:** Prüfe Logs, Jobs, Batch-Läufe und Cronjobs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 209: Baseline

**Merksatz:** Datenqualität ist Teil der Baseline.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 210: Baseline

**Merksatz:** Kosten des Status quo machen Migration entscheidbar.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 211: Target

**Merksatz:** Ein Ziel ohne KPI ist ein Wunsch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 212: Target

**Merksatz:** Target Architecture muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 213: Target

**Merksatz:** Nicht-Ziele schützen die Roadmap.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 214: Target

**Merksatz:** Zieltechnologien brauchen Skills und Betriebskonzept.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 215: Target

**Merksatz:** Sicherheit gehört in das Zielbild, nicht ans Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 216: Target

**Merksatz:** Target muss Geschäftsziele sichtbar unterstützen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 217: Target

**Merksatz:** Je unklarer das Ziel, desto politischer die Migration.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 218: Target

**Merksatz:** Ein Zielbild darf ambitioniert sein, aber nicht neblig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 219: Target

**Merksatz:** Zielarchitektur ohne Datenarchitektur ist unvollständig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 220: Target

**Merksatz:** Target entscheidet, welche Gaps relevant sind.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 221: Gap Analysis

**Merksatz:** Gaps sind keine Schuldzuweisung, sondern Arbeitsmaterial.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 222: Gap Analysis

**Merksatz:** Ein Gap ohne Entscheidung bleibt Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 223: Gap Analysis

**Merksatz:** Accidentally Eliminated ist die gefährlichste Kategorie.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 224: Gap Analysis

**Merksatz:** Nicht alles, was alt ist, muss weg.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 225: Gap Analysis

**Merksatz:** Nicht alles, was neu ist, ist wertvoll.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 226: Gap Analysis

**Merksatz:** Jeder Gap braucht Impact-Bewertung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 227: Gap Analysis

**Merksatz:** Gaps müssen in Arbeit übersetzt werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 228: Gap Analysis

**Merksatz:** Eine Gap-Matrix schafft gemeinsame Sprache.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 229: Gap Analysis

**Merksatz:** Gaps ohne Owner verschwinden nicht.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 230: Gap Analysis

**Merksatz:** Business Impact schlägt technische Eleganz.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 231: Work Packages

**Merksatz:** Ein Work Package braucht ein klares Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 232: Work Packages

**Merksatz:** Zu große Work Packages verstecken Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 233: Work Packages

**Merksatz:** Zu kleine Work Packages erzeugen Steuerungsaufwand.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 234: Work Packages

**Merksatz:** Ein Work Package ohne Nutzen ist verdächtig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 235: Work Packages

**Merksatz:** Jedes Work Package braucht Abhängigkeiten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 236: Work Packages

**Merksatz:** Ein Owner ist keine Mailingliste.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 237: Work Packages

**Merksatz:** Rollback gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 238: Work Packages

**Merksatz:** Tests gehören ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 239: Work Packages

**Merksatz:** Security gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 240: Work Packages

**Merksatz:** Doku gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 241: Roadmap

**Merksatz:** Roadmaps werden aus Abhängigkeiten gebaut, nicht aus Wunschdaten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 242: Roadmap

**Merksatz:** Früher sichtbarer Nutzen schafft Vertrauen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 243: Roadmap

**Merksatz:** Eine Roadmap braucht Entscheidungspunkte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 244: Roadmap

**Merksatz:** Eine Welle muss in sich Sinn ergeben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 245: Roadmap

**Merksatz:** Puffer ist kein Luxus, sondern Risikobehandlung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 246: Roadmap

**Merksatz:** Nicht jede Parallelisierung spart Zeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 247: Roadmap

**Merksatz:** Sequenzierung ist Architekturarbeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 248: Roadmap

**Merksatz:** Ein kritischer Pfad muss sichtbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 249: Roadmap

**Merksatz:** Roadmaps müssen lernfähig bleiben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 250: Roadmap

**Merksatz:** Management braucht eine andere Sicht als Teams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 251: Transition

**Merksatz:** Ein Zwischenzustand muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 252: Transition

**Merksatz:** Halbfertig ist keine Transition Architecture.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 253: Transition

**Merksatz:** Jede Transition braucht KPIs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 254: Transition

**Merksatz:** Jede Transition braucht Abnahme.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 255: Transition

**Merksatz:** Jede Transition braucht Support.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 256: Transition

**Merksatz:** Jede Transition braucht Security-Kontrollen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 257: Transition

**Merksatz:** Jede Transition braucht Datenregeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 258: Transition

**Merksatz:** Jede Transition braucht Interoperabilität.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 259: Transition

**Merksatz:** Jede Transition braucht Rollback oder Recovery.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 260: Transition

**Merksatz:** Jede Transition muss erklärbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 261: Interoperability

**Merksatz:** Alte und neue Welt müssen miteinander sprechen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 262: Interoperability

**Merksatz:** Adapter sind temporär, aber brauchen Owner.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 263: Interoperability

**Merksatz:** Temporär ohne Ablaufdatum wird dauerhaft.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 264: Interoperability

**Merksatz:** Daten-Synchronisation ist kein Detail.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 265: Interoperability

**Merksatz:** System of Record muss pro Datenobjekt klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 266: Interoperability

**Merksatz:** Schnittstellen brauchen Contract Tests.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 267: Interoperability

**Merksatz:** API-Gateways können Migration entkoppeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 268: Interoperability

**Merksatz:** Dual Write ist gefährlich und braucht Kontrolle.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 269: Interoperability

**Merksatz:** Read-only Legacy kann Risiko reduzieren.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 270: Interoperability

**Merksatz:** Interoperabilität muss pro Transition geprüft werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 271: Risk

**Merksatz:** Risiken werden nicht kleiner, wenn man sie nicht aufschreibt.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 272: Risk

**Merksatz:** Initial Risk und Residual Risk trennen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 273: Risk

**Merksatz:** Jede Maßnahme muss einem Risiko zugeordnet sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 274: Risk

**Merksatz:** Ein akzeptiertes Risiko braucht Entscheider.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 275: Risk

**Merksatz:** Go-Live-Risiken brauchen Go/No-Go-Kriterien.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 276: Risk

**Merksatz:** Datenrisiken sind oft geschäftskritisch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 277: Risk

**Merksatz:** Security-Risiken entstehen auch durch Übergangszustände.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 278: Risk

**Merksatz:** Skill-Risiken sind echte Projektrisiken.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 279: Risk

**Merksatz:** Budgetrisiken brauchen Forecasts.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 280: Risk

**Merksatz:** Scope-Risiken brauchen Change Control.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 281: Governance

**Merksatz:** Governance schützt Ziel und Investition.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 282: Governance

**Merksatz:** Zu wenig Governance erzeugt Drift.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 283: Governance

**Merksatz:** Zu viel Governance erzeugt Umgehung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 284: Governance

**Merksatz:** Entscheidungsrechte müssen vor Konflikten klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 285: Governance

**Merksatz:** Architecture Board prüft relevante Abweichungen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 286: Governance

**Merksatz:** Nicht jede operative Frage braucht ein Gremium.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 287: Governance

**Merksatz:** Architecture Contracts schaffen Verbindlichkeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 288: Governance

**Merksatz:** Compliance Reviews brauchen klare Kriterien.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 289: Governance

**Merksatz:** Eskalationswege müssen bekannt sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 290: Governance

**Merksatz:** Governance muss Umsetzung ermöglichen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 291: Cutover

**Merksatz:** Cutover ist kein einzelner Termin, sondern ein Ablauf.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 292: Cutover

**Merksatz:** Cutover braucht Generalprobe.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 293: Cutover

**Merksatz:** Smoke Tests müssen vorab definiert sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 294: Cutover

**Merksatz:** Rollback-Zeitfenster muss bekannt sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 295: Cutover

**Merksatz:** Kommunikation entscheidet über Ruhe im Go-Live.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 296: Cutover

**Merksatz:** Support muss vor Cutover bereit sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 297: Cutover

**Merksatz:** Daten-Freeze muss geplant werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 298: Cutover

**Merksatz:** Entscheider müssen erreichbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 299: Cutover

**Merksatz:** Monitoring muss live sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 300: Cutover

**Merksatz:** No-Go ist eine professionelle Option.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 301: Baseline

**Merksatz:** Beschreibe, was wirklich existiert, nicht was offiziell existieren sollte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 302: Baseline

**Merksatz:** Suche nach Schattenprozessen, Excel-Dateien und manuellen Workarounds.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 303: Baseline

**Merksatz:** Eine unbekannte Schnittstelle kann eine ganze Migration kippen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 304: Baseline

**Merksatz:** Inventar ohne Owner ist nur eine Liste, keine Architekturgrundlage.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 305: Baseline

**Merksatz:** Kritikalität muss fachlich und technisch bewertet werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 306: Baseline

**Merksatz:** Alte Systeme haben oft versteckte Geschäftsfunktionen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 307: Baseline

**Merksatz:** Sprich mit Betrieb, nicht nur mit Projektteams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 308: Baseline

**Merksatz:** Prüfe Logs, Jobs, Batch-Läufe und Cronjobs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 309: Baseline

**Merksatz:** Datenqualität ist Teil der Baseline.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 310: Baseline

**Merksatz:** Kosten des Status quo machen Migration entscheidbar.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 311: Target

**Merksatz:** Ein Ziel ohne KPI ist ein Wunsch.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 312: Target

**Merksatz:** Target Architecture muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 313: Target

**Merksatz:** Nicht-Ziele schützen die Roadmap.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 314: Target

**Merksatz:** Zieltechnologien brauchen Skills und Betriebskonzept.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 315: Target

**Merksatz:** Sicherheit gehört in das Zielbild, nicht ans Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 316: Target

**Merksatz:** Target muss Geschäftsziele sichtbar unterstützen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 317: Target

**Merksatz:** Je unklarer das Ziel, desto politischer die Migration.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 318: Target

**Merksatz:** Ein Zielbild darf ambitioniert sein, aber nicht neblig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 319: Target

**Merksatz:** Zielarchitektur ohne Datenarchitektur ist unvollständig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 320: Target

**Merksatz:** Target entscheidet, welche Gaps relevant sind.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 321: Gap Analysis

**Merksatz:** Gaps sind keine Schuldzuweisung, sondern Arbeitsmaterial.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 322: Gap Analysis

**Merksatz:** Ein Gap ohne Entscheidung bleibt Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 323: Gap Analysis

**Merksatz:** Accidentally Eliminated ist die gefährlichste Kategorie.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 324: Gap Analysis

**Merksatz:** Nicht alles, was alt ist, muss weg.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 325: Gap Analysis

**Merksatz:** Nicht alles, was neu ist, ist wertvoll.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 326: Gap Analysis

**Merksatz:** Jeder Gap braucht Impact-Bewertung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 327: Gap Analysis

**Merksatz:** Gaps müssen in Arbeit übersetzt werden.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 328: Gap Analysis

**Merksatz:** Eine Gap-Matrix schafft gemeinsame Sprache.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 329: Gap Analysis

**Merksatz:** Gaps ohne Owner verschwinden nicht.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 330: Gap Analysis

**Merksatz:** Business Impact schlägt technische Eleganz.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 331: Work Packages

**Merksatz:** Ein Work Package braucht ein klares Ende.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 332: Work Packages

**Merksatz:** Zu große Work Packages verstecken Risiko.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 333: Work Packages

**Merksatz:** Zu kleine Work Packages erzeugen Steuerungsaufwand.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 334: Work Packages

**Merksatz:** Ein Work Package ohne Nutzen ist verdächtig.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 335: Work Packages

**Merksatz:** Jedes Work Package braucht Abhängigkeiten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 336: Work Packages

**Merksatz:** Ein Owner ist keine Mailingliste.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 337: Work Packages

**Merksatz:** Rollback gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 338: Work Packages

**Merksatz:** Tests gehören ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 339: Work Packages

**Merksatz:** Security gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 340: Work Packages

**Merksatz:** Doku gehört ins Work Package.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 341: Roadmap

**Merksatz:** Roadmaps werden aus Abhängigkeiten gebaut, nicht aus Wunschdaten.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 342: Roadmap

**Merksatz:** Früher sichtbarer Nutzen schafft Vertrauen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 343: Roadmap

**Merksatz:** Eine Roadmap braucht Entscheidungspunkte.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 344: Roadmap

**Merksatz:** Eine Welle muss in sich Sinn ergeben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 345: Roadmap

**Merksatz:** Puffer ist kein Luxus, sondern Risikobehandlung.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 346: Roadmap

**Merksatz:** Nicht jede Parallelisierung spart Zeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 347: Roadmap

**Merksatz:** Sequenzierung ist Architekturarbeit.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 348: Roadmap

**Merksatz:** Ein kritischer Pfad muss sichtbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 349: Roadmap

**Merksatz:** Roadmaps müssen lernfähig bleiben.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 350: Roadmap

**Merksatz:** Management braucht eine andere Sicht als Teams.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 351: Transition

**Merksatz:** Ein Zwischenzustand muss betreibbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 352: Transition

**Merksatz:** Halbfertig ist keine Transition Architecture.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 353: Transition

**Merksatz:** Jede Transition braucht KPIs.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 354: Transition

**Merksatz:** Jede Transition braucht Abnahme.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 355: Transition

**Merksatz:** Jede Transition braucht Support.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 356: Transition

**Merksatz:** Jede Transition braucht Security-Kontrollen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 357: Transition

**Merksatz:** Jede Transition braucht Datenregeln.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 358: Transition

**Merksatz:** Jede Transition braucht Interoperabilität.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 359: Transition

**Merksatz:** Jede Transition braucht Rollback oder Recovery.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 360: Transition

**Merksatz:** Jede Transition muss erklärbar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 361: Interoperability

**Merksatz:** Alte und neue Welt müssen miteinander sprechen.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 362: Interoperability

**Merksatz:** Adapter sind temporär, aber brauchen Owner.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 363: Interoperability

**Merksatz:** Temporär ohne Ablaufdatum wird dauerhaft.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 364: Interoperability

**Merksatz:** Daten-Synchronisation ist kein Detail.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

### Coach-Karte 365: Interoperability

**Merksatz:** System of Record muss pro Datenobjekt klar sein.

**Coach-Frage:** Was bedeutet dieser Satz konkret für deine aktuelle Migration?

**Umsetzung:** Notiere eine konkrete Entscheidung, einen offenen Punkt oder eine nächste Aktion.

## 32. 500 Praxisfragen für Migration Planning Workshops

1. **Baseline:** Prüfe: Welche Systeme existieren heute wirklich?
2. **Baseline:** Prüfe: Welche Systeme sind nicht offiziell dokumentiert?
3. **Baseline:** Prüfe: Welche Schnittstellen sind kritisch?
4. **Baseline:** Prüfe: Welche Batch-Prozesse laufen nachts?
5. **Baseline:** Prüfe: Welche Datenbanken sind führend?
6. **Baseline:** Prüfe: Welche manuellen Workarounds gibt es?
7. **Baseline:** Prüfe: Welche Systeme sind End-of-Life?
8. **Baseline:** Prüfe: Welche Lizenzen laufen aus?
9. **Baseline:** Prüfe: Welche Systeme haben keine Owner?
10. **Baseline:** Prüfe: Welche Systeme dürfen nicht ausfallen?
11. **Target:** Prüfe: Welche Zielsysteme sind gesetzt?
12. **Target:** Prüfe: Welche Zieltechnologien sind verbindlich?
13. **Target:** Prüfe: Welche Architekturprinzipien gelten?
14. **Target:** Prüfe: Welche Qualitätseigenschaften werden verbessert?
15. **Target:** Prüfe: Welche Kosten sollen reduziert werden?
16. **Target:** Prüfe: Welche Prozesse werden verändert?
17. **Target:** Prüfe: Welche Datenflüsse entstehen neu?
18. **Target:** Prüfe: Welche Systeme werden abgeschaltet?
19. **Target:** Prüfe: Welche Systeme bleiben bewusst erhalten?
20. **Target:** Prüfe: Welche Nicht-Ziele gelten?
21. **Gap:** Prüfe: Was existiert heute und im Ziel nicht mehr?
22. **Gap:** Prüfe: Was existiert im Ziel, aber heute noch nicht?
23. **Gap:** Prüfe: Was bleibt, muss aber verändert werden?
24. **Gap:** Prüfe: Was wurde im Zielbild vergessen?
25. **Gap:** Prüfe: Welche Gaps sind kritisch?
26. **Gap:** Prüfe: Welche Gaps sind regulatorisch relevant?
27. **Gap:** Prüfe: Welche Gaps betreffen Daten?
28. **Gap:** Prüfe: Welche Gaps betreffen Security?
29. **Gap:** Prüfe: Welche Gaps betreffen Betrieb?
30. **Gap:** Prüfe: Welche Gaps betreffen Nutzer?
31. **Work Packages:** Prüfe: Welche Gaps können zusammen umgesetzt werden?
32. **Work Packages:** Prüfe: Welche Gaps müssen getrennt bleiben?
33. **Work Packages:** Prüfe: Welches Work Package liefert frühen Nutzen?
34. **Work Packages:** Prüfe: Welches Work Package reduziert Risiko?
35. **Work Packages:** Prüfe: Welches Work Package hat die meisten Abhängigkeiten?
36. **Work Packages:** Prüfe: Wer ist Owner?
37. **Work Packages:** Prüfe: Welche Skills werden gebraucht?
38. **Work Packages:** Prüfe: Welche Tests sind nötig?
39. **Work Packages:** Prüfe: Was ist out of scope?
40. **Work Packages:** Prüfe: Wann ist das Work Package fertig?
41. **Roadmap:** Prüfe: Was muss zuerst passieren?
42. **Roadmap:** Prüfe: Was kann parallel laufen?
43. **Roadmap:** Prüfe: Was darf nicht parallel laufen?
44. **Roadmap:** Prüfe: Wo liegt der kritische Pfad?
45. **Roadmap:** Prüfe: Welche Welle liefert sichtbaren Nutzen?
46. **Roadmap:** Prüfe: Welche Welle ist am riskantesten?
47. **Roadmap:** Prüfe: Welche Entscheidungspunkte brauchen wir?
48. **Roadmap:** Prüfe: Welche Puffer brauchen wir?
49. **Roadmap:** Prüfe: Welche Abhängigkeit ist ungeklärt?
50. **Roadmap:** Prüfe: Welche Termine sind extern fix?
51. **Transition:** Prüfe: Wie sieht Zwischenzustand eins aus?
52. **Transition:** Prüfe: Ist der Zwischenzustand betreibbar?
53. **Transition:** Prüfe: Welche alten Komponenten bleiben aktiv?
54. **Transition:** Prüfe: Welche neuen Komponenten sind aktiv?
55. **Transition:** Prüfe: Welche Daten liegen wo?
56. **Transition:** Prüfe: Wer supportet den Zustand?
57. **Transition:** Prüfe: Welche KPIs gelten?
58. **Transition:** Prüfe: Welche Risiken bleiben?
59. **Transition:** Prüfe: Was ist der Rollback?
60. **Transition:** Prüfe: Wann gilt die Transition als erreicht?
61. **Interoperability:** Prüfe: Welche Systeme müssen während der Migration sprechen?
62. **Interoperability:** Prüfe: Welche Protokolle ändern sich?
63. **Interoperability:** Prüfe: Welche Adapter brauchen wir?
64. **Interoperability:** Prüfe: Welche Daten werden synchronisiert?
65. **Interoperability:** Prüfe: Welche Schnittstellen müssen abwärtskompatibel bleiben?
66. **Interoperability:** Prüfe: Welche temporären Lösungen entstehen?
67. **Interoperability:** Prüfe: Wann werden temporäre Lösungen entfernt?
68. **Interoperability:** Prüfe: Wer testet die Schnittstellen?
69. **Interoperability:** Prüfe: Wie erkennen wir Synchronisationsfehler?
70. **Interoperability:** Prüfe: Welche Integrationen sind kritisch?
71. **Risk:** Prüfe: Was kann die Migration stoppen?
72. **Risk:** Prüfe: Wo droht Datenverlust?
73. **Risk:** Prüfe: Wo droht Downtime?
74. **Risk:** Prüfe: Wo droht Compliance-Verstoß?
75. **Risk:** Prüfe: Wo fehlen Skills?
76. **Risk:** Prüfe: Wo ist Budget unsicher?
77. **Risk:** Prüfe: Welche Annahme ist riskant?
78. **Risk:** Prüfe: Was ist Initial Risk?
79. **Risk:** Prüfe: Was ist Residual Risk?
80. **Risk:** Prüfe: Wer akzeptiert das Restrisiko?
81. **Governance:** Prüfe: Wer entscheidet über Scope?
82. **Governance:** Prüfe: Wer entscheidet über Architekturabweichungen?
83. **Governance:** Prüfe: Wer entscheidet über Go/No-Go?
84. **Governance:** Prüfe: Wer darf Rollback auslösen?
85. **Governance:** Prüfe: Welche Reports braucht Management?
86. **Governance:** Prüfe: Welche Reports brauchen Teams?
87. **Governance:** Prüfe: Welche Reviewpunkte sind verpflichtend?
88. **Governance:** Prüfe: Welche Abweichungen sind genehmigungspflichtig?
89. **Governance:** Prüfe: Wie wird Change dokumentiert?
90. **Governance:** Prüfe: Wie wird das Repository aktualisiert?
91. **Cutover:** Prüfe: Wann beginnt der Cutover?
92. **Cutover:** Prüfe: Wann endet das Rückfallfenster?
93. **Cutover:** Prüfe: Welche Vorbedingungen müssen erfüllt sein?
94. **Cutover:** Prüfe: Welche Schritte sind sequenziell?
95. **Cutover:** Prüfe: Wer führt welchen Schritt aus?
96. **Cutover:** Prüfe: Welche Smoke Tests gibt es?
97. **Cutover:** Prüfe: Wann wird zurückgerollt?
98. **Cutover:** Prüfe: Wer informiert Nutzer?
99. **Cutover:** Prüfe: Wer informiert Management?
100. **Cutover:** Prüfe: Was passiert nach Go-Live?
101. **Baseline:** Kläre im Workshop: Welche Systeme existieren heute wirklich?
102. **Baseline:** Kläre im Workshop: Welche Systeme sind nicht offiziell dokumentiert?
103. **Baseline:** Kläre im Workshop: Welche Schnittstellen sind kritisch?
104. **Baseline:** Kläre im Workshop: Welche Batch-Prozesse laufen nachts?
105. **Baseline:** Kläre im Workshop: Welche Datenbanken sind führend?
106. **Baseline:** Kläre im Workshop: Welche manuellen Workarounds gibt es?
107. **Baseline:** Kläre im Workshop: Welche Systeme sind End-of-Life?
108. **Baseline:** Kläre im Workshop: Welche Lizenzen laufen aus?
109. **Baseline:** Kläre im Workshop: Welche Systeme haben keine Owner?
110. **Baseline:** Kläre im Workshop: Welche Systeme dürfen nicht ausfallen?
111. **Target:** Kläre im Workshop: Welche Zielsysteme sind gesetzt?
112. **Target:** Kläre im Workshop: Welche Zieltechnologien sind verbindlich?
113. **Target:** Kläre im Workshop: Welche Architekturprinzipien gelten?
114. **Target:** Kläre im Workshop: Welche Qualitätseigenschaften werden verbessert?
115. **Target:** Kläre im Workshop: Welche Kosten sollen reduziert werden?
116. **Target:** Kläre im Workshop: Welche Prozesse werden verändert?
117. **Target:** Kläre im Workshop: Welche Datenflüsse entstehen neu?
118. **Target:** Kläre im Workshop: Welche Systeme werden abgeschaltet?
119. **Target:** Kläre im Workshop: Welche Systeme bleiben bewusst erhalten?
120. **Target:** Kläre im Workshop: Welche Nicht-Ziele gelten?
121. **Gap:** Kläre im Workshop: Was existiert heute und im Ziel nicht mehr?
122. **Gap:** Kläre im Workshop: Was existiert im Ziel, aber heute noch nicht?
123. **Gap:** Kläre im Workshop: Was bleibt, muss aber verändert werden?
124. **Gap:** Kläre im Workshop: Was wurde im Zielbild vergessen?
125. **Gap:** Kläre im Workshop: Welche Gaps sind kritisch?
126. **Gap:** Kläre im Workshop: Welche Gaps sind regulatorisch relevant?
127. **Gap:** Kläre im Workshop: Welche Gaps betreffen Daten?
128. **Gap:** Kläre im Workshop: Welche Gaps betreffen Security?
129. **Gap:** Kläre im Workshop: Welche Gaps betreffen Betrieb?
130. **Gap:** Kläre im Workshop: Welche Gaps betreffen Nutzer?
131. **Work Packages:** Kläre im Workshop: Welche Gaps können zusammen umgesetzt werden?
132. **Work Packages:** Kläre im Workshop: Welche Gaps müssen getrennt bleiben?
133. **Work Packages:** Kläre im Workshop: Welches Work Package liefert frühen Nutzen?
134. **Work Packages:** Kläre im Workshop: Welches Work Package reduziert Risiko?
135. **Work Packages:** Kläre im Workshop: Welches Work Package hat die meisten Abhängigkeiten?
136. **Work Packages:** Kläre im Workshop: Wer ist Owner?
137. **Work Packages:** Kläre im Workshop: Welche Skills werden gebraucht?
138. **Work Packages:** Kläre im Workshop: Welche Tests sind nötig?
139. **Work Packages:** Kläre im Workshop: Was ist out of scope?
140. **Work Packages:** Kläre im Workshop: Wann ist das Work Package fertig?
141. **Roadmap:** Kläre im Workshop: Was muss zuerst passieren?
142. **Roadmap:** Kläre im Workshop: Was kann parallel laufen?
143. **Roadmap:** Kläre im Workshop: Was darf nicht parallel laufen?
144. **Roadmap:** Kläre im Workshop: Wo liegt der kritische Pfad?
145. **Roadmap:** Kläre im Workshop: Welche Welle liefert sichtbaren Nutzen?
146. **Roadmap:** Kläre im Workshop: Welche Welle ist am riskantesten?
147. **Roadmap:** Kläre im Workshop: Welche Entscheidungspunkte brauchen wir?
148. **Roadmap:** Kläre im Workshop: Welche Puffer brauchen wir?
149. **Roadmap:** Kläre im Workshop: Welche Abhängigkeit ist ungeklärt?
150. **Roadmap:** Kläre im Workshop: Welche Termine sind extern fix?
151. **Transition:** Kläre im Workshop: Wie sieht Zwischenzustand eins aus?
152. **Transition:** Kläre im Workshop: Ist der Zwischenzustand betreibbar?
153. **Transition:** Kläre im Workshop: Welche alten Komponenten bleiben aktiv?
154. **Transition:** Kläre im Workshop: Welche neuen Komponenten sind aktiv?
155. **Transition:** Kläre im Workshop: Welche Daten liegen wo?
156. **Transition:** Kläre im Workshop: Wer supportet den Zustand?
157. **Transition:** Kläre im Workshop: Welche KPIs gelten?
158. **Transition:** Kläre im Workshop: Welche Risiken bleiben?
159. **Transition:** Kläre im Workshop: Was ist der Rollback?
160. **Transition:** Kläre im Workshop: Wann gilt die Transition als erreicht?
161. **Interoperability:** Kläre im Workshop: Welche Systeme müssen während der Migration sprechen?
162. **Interoperability:** Kläre im Workshop: Welche Protokolle ändern sich?
163. **Interoperability:** Kläre im Workshop: Welche Adapter brauchen wir?
164. **Interoperability:** Kläre im Workshop: Welche Daten werden synchronisiert?
165. **Interoperability:** Kläre im Workshop: Welche Schnittstellen müssen abwärtskompatibel bleiben?
166. **Interoperability:** Kläre im Workshop: Welche temporären Lösungen entstehen?
167. **Interoperability:** Kläre im Workshop: Wann werden temporäre Lösungen entfernt?
168. **Interoperability:** Kläre im Workshop: Wer testet die Schnittstellen?
169. **Interoperability:** Kläre im Workshop: Wie erkennen wir Synchronisationsfehler?
170. **Interoperability:** Kläre im Workshop: Welche Integrationen sind kritisch?
171. **Risk:** Kläre im Workshop: Was kann die Migration stoppen?
172. **Risk:** Kläre im Workshop: Wo droht Datenverlust?
173. **Risk:** Kläre im Workshop: Wo droht Downtime?
174. **Risk:** Kläre im Workshop: Wo droht Compliance-Verstoß?
175. **Risk:** Kläre im Workshop: Wo fehlen Skills?
176. **Risk:** Kläre im Workshop: Wo ist Budget unsicher?
177. **Risk:** Kläre im Workshop: Welche Annahme ist riskant?
178. **Risk:** Kläre im Workshop: Was ist Initial Risk?
179. **Risk:** Kläre im Workshop: Was ist Residual Risk?
180. **Risk:** Kläre im Workshop: Wer akzeptiert das Restrisiko?
181. **Governance:** Kläre im Workshop: Wer entscheidet über Scope?
182. **Governance:** Kläre im Workshop: Wer entscheidet über Architekturabweichungen?
183. **Governance:** Kläre im Workshop: Wer entscheidet über Go/No-Go?
184. **Governance:** Kläre im Workshop: Wer darf Rollback auslösen?
185. **Governance:** Kläre im Workshop: Welche Reports braucht Management?
186. **Governance:** Kläre im Workshop: Welche Reports brauchen Teams?
187. **Governance:** Kläre im Workshop: Welche Reviewpunkte sind verpflichtend?
188. **Governance:** Kläre im Workshop: Welche Abweichungen sind genehmigungspflichtig?
189. **Governance:** Kläre im Workshop: Wie wird Change dokumentiert?
190. **Governance:** Kläre im Workshop: Wie wird das Repository aktualisiert?
191. **Cutover:** Kläre im Workshop: Wann beginnt der Cutover?
192. **Cutover:** Kläre im Workshop: Wann endet das Rückfallfenster?
193. **Cutover:** Kläre im Workshop: Welche Vorbedingungen müssen erfüllt sein?
194. **Cutover:** Kläre im Workshop: Welche Schritte sind sequenziell?
195. **Cutover:** Kläre im Workshop: Wer führt welchen Schritt aus?
196. **Cutover:** Kläre im Workshop: Welche Smoke Tests gibt es?
197. **Cutover:** Kläre im Workshop: Wann wird zurückgerollt?
198. **Cutover:** Kläre im Workshop: Wer informiert Nutzer?
199. **Cutover:** Kläre im Workshop: Wer informiert Management?
200. **Cutover:** Kläre im Workshop: Was passiert nach Go-Live?
201. **Baseline:** Validiere mit Ownern: Welche Systeme existieren heute wirklich?
202. **Baseline:** Validiere mit Ownern: Welche Systeme sind nicht offiziell dokumentiert?
203. **Baseline:** Validiere mit Ownern: Welche Schnittstellen sind kritisch?
204. **Baseline:** Validiere mit Ownern: Welche Batch-Prozesse laufen nachts?
205. **Baseline:** Validiere mit Ownern: Welche Datenbanken sind führend?
206. **Baseline:** Validiere mit Ownern: Welche manuellen Workarounds gibt es?
207. **Baseline:** Validiere mit Ownern: Welche Systeme sind End-of-Life?
208. **Baseline:** Validiere mit Ownern: Welche Lizenzen laufen aus?
209. **Baseline:** Validiere mit Ownern: Welche Systeme haben keine Owner?
210. **Baseline:** Validiere mit Ownern: Welche Systeme dürfen nicht ausfallen?
211. **Target:** Validiere mit Ownern: Welche Zielsysteme sind gesetzt?
212. **Target:** Validiere mit Ownern: Welche Zieltechnologien sind verbindlich?
213. **Target:** Validiere mit Ownern: Welche Architekturprinzipien gelten?
214. **Target:** Validiere mit Ownern: Welche Qualitätseigenschaften werden verbessert?
215. **Target:** Validiere mit Ownern: Welche Kosten sollen reduziert werden?
216. **Target:** Validiere mit Ownern: Welche Prozesse werden verändert?
217. **Target:** Validiere mit Ownern: Welche Datenflüsse entstehen neu?
218. **Target:** Validiere mit Ownern: Welche Systeme werden abgeschaltet?
219. **Target:** Validiere mit Ownern: Welche Systeme bleiben bewusst erhalten?
220. **Target:** Validiere mit Ownern: Welche Nicht-Ziele gelten?
221. **Gap:** Validiere mit Ownern: Was existiert heute und im Ziel nicht mehr?
222. **Gap:** Validiere mit Ownern: Was existiert im Ziel, aber heute noch nicht?
223. **Gap:** Validiere mit Ownern: Was bleibt, muss aber verändert werden?
224. **Gap:** Validiere mit Ownern: Was wurde im Zielbild vergessen?
225. **Gap:** Validiere mit Ownern: Welche Gaps sind kritisch?
226. **Gap:** Validiere mit Ownern: Welche Gaps sind regulatorisch relevant?
227. **Gap:** Validiere mit Ownern: Welche Gaps betreffen Daten?
228. **Gap:** Validiere mit Ownern: Welche Gaps betreffen Security?
229. **Gap:** Validiere mit Ownern: Welche Gaps betreffen Betrieb?
230. **Gap:** Validiere mit Ownern: Welche Gaps betreffen Nutzer?
231. **Work Packages:** Validiere mit Ownern: Welche Gaps können zusammen umgesetzt werden?
232. **Work Packages:** Validiere mit Ownern: Welche Gaps müssen getrennt bleiben?
233. **Work Packages:** Validiere mit Ownern: Welches Work Package liefert frühen Nutzen?
234. **Work Packages:** Validiere mit Ownern: Welches Work Package reduziert Risiko?
235. **Work Packages:** Validiere mit Ownern: Welches Work Package hat die meisten Abhängigkeiten?
236. **Work Packages:** Validiere mit Ownern: Wer ist Owner?
237. **Work Packages:** Validiere mit Ownern: Welche Skills werden gebraucht?
238. **Work Packages:** Validiere mit Ownern: Welche Tests sind nötig?
239. **Work Packages:** Validiere mit Ownern: Was ist out of scope?
240. **Work Packages:** Validiere mit Ownern: Wann ist das Work Package fertig?
241. **Roadmap:** Validiere mit Ownern: Was muss zuerst passieren?
242. **Roadmap:** Validiere mit Ownern: Was kann parallel laufen?
243. **Roadmap:** Validiere mit Ownern: Was darf nicht parallel laufen?
244. **Roadmap:** Validiere mit Ownern: Wo liegt der kritische Pfad?
245. **Roadmap:** Validiere mit Ownern: Welche Welle liefert sichtbaren Nutzen?
246. **Roadmap:** Validiere mit Ownern: Welche Welle ist am riskantesten?
247. **Roadmap:** Validiere mit Ownern: Welche Entscheidungspunkte brauchen wir?
248. **Roadmap:** Validiere mit Ownern: Welche Puffer brauchen wir?
249. **Roadmap:** Validiere mit Ownern: Welche Abhängigkeit ist ungeklärt?
250. **Roadmap:** Validiere mit Ownern: Welche Termine sind extern fix?
251. **Transition:** Validiere mit Ownern: Wie sieht Zwischenzustand eins aus?
252. **Transition:** Validiere mit Ownern: Ist der Zwischenzustand betreibbar?
253. **Transition:** Validiere mit Ownern: Welche alten Komponenten bleiben aktiv?
254. **Transition:** Validiere mit Ownern: Welche neuen Komponenten sind aktiv?
255. **Transition:** Validiere mit Ownern: Welche Daten liegen wo?
256. **Transition:** Validiere mit Ownern: Wer supportet den Zustand?
257. **Transition:** Validiere mit Ownern: Welche KPIs gelten?
258. **Transition:** Validiere mit Ownern: Welche Risiken bleiben?
259. **Transition:** Validiere mit Ownern: Was ist der Rollback?
260. **Transition:** Validiere mit Ownern: Wann gilt die Transition als erreicht?
261. **Interoperability:** Validiere mit Ownern: Welche Systeme müssen während der Migration sprechen?
262. **Interoperability:** Validiere mit Ownern: Welche Protokolle ändern sich?
263. **Interoperability:** Validiere mit Ownern: Welche Adapter brauchen wir?
264. **Interoperability:** Validiere mit Ownern: Welche Daten werden synchronisiert?
265. **Interoperability:** Validiere mit Ownern: Welche Schnittstellen müssen abwärtskompatibel bleiben?
266. **Interoperability:** Validiere mit Ownern: Welche temporären Lösungen entstehen?
267. **Interoperability:** Validiere mit Ownern: Wann werden temporäre Lösungen entfernt?
268. **Interoperability:** Validiere mit Ownern: Wer testet die Schnittstellen?
269. **Interoperability:** Validiere mit Ownern: Wie erkennen wir Synchronisationsfehler?
270. **Interoperability:** Validiere mit Ownern: Welche Integrationen sind kritisch?
271. **Risk:** Validiere mit Ownern: Was kann die Migration stoppen?
272. **Risk:** Validiere mit Ownern: Wo droht Datenverlust?
273. **Risk:** Validiere mit Ownern: Wo droht Downtime?
274. **Risk:** Validiere mit Ownern: Wo droht Compliance-Verstoß?
275. **Risk:** Validiere mit Ownern: Wo fehlen Skills?
276. **Risk:** Validiere mit Ownern: Wo ist Budget unsicher?
277. **Risk:** Validiere mit Ownern: Welche Annahme ist riskant?
278. **Risk:** Validiere mit Ownern: Was ist Initial Risk?
279. **Risk:** Validiere mit Ownern: Was ist Residual Risk?
280. **Risk:** Validiere mit Ownern: Wer akzeptiert das Restrisiko?
281. **Governance:** Validiere mit Ownern: Wer entscheidet über Scope?
282. **Governance:** Validiere mit Ownern: Wer entscheidet über Architekturabweichungen?
283. **Governance:** Validiere mit Ownern: Wer entscheidet über Go/No-Go?
284. **Governance:** Validiere mit Ownern: Wer darf Rollback auslösen?
285. **Governance:** Validiere mit Ownern: Welche Reports braucht Management?
286. **Governance:** Validiere mit Ownern: Welche Reports brauchen Teams?
287. **Governance:** Validiere mit Ownern: Welche Reviewpunkte sind verpflichtend?
288. **Governance:** Validiere mit Ownern: Welche Abweichungen sind genehmigungspflichtig?
289. **Governance:** Validiere mit Ownern: Wie wird Change dokumentiert?
290. **Governance:** Validiere mit Ownern: Wie wird das Repository aktualisiert?
291. **Cutover:** Validiere mit Ownern: Wann beginnt der Cutover?
292. **Cutover:** Validiere mit Ownern: Wann endet das Rückfallfenster?
293. **Cutover:** Validiere mit Ownern: Welche Vorbedingungen müssen erfüllt sein?
294. **Cutover:** Validiere mit Ownern: Welche Schritte sind sequenziell?
295. **Cutover:** Validiere mit Ownern: Wer führt welchen Schritt aus?
296. **Cutover:** Validiere mit Ownern: Welche Smoke Tests gibt es?
297. **Cutover:** Validiere mit Ownern: Wann wird zurückgerollt?
298. **Cutover:** Validiere mit Ownern: Wer informiert Nutzer?
299. **Cutover:** Validiere mit Ownern: Wer informiert Management?
300. **Cutover:** Validiere mit Ownern: Was passiert nach Go-Live?
301. **Baseline:** Dokumentiere: Welche Systeme existieren heute wirklich?
302. **Baseline:** Dokumentiere: Welche Systeme sind nicht offiziell dokumentiert?
303. **Baseline:** Dokumentiere: Welche Schnittstellen sind kritisch?
304. **Baseline:** Dokumentiere: Welche Batch-Prozesse laufen nachts?
305. **Baseline:** Dokumentiere: Welche Datenbanken sind führend?
306. **Baseline:** Dokumentiere: Welche manuellen Workarounds gibt es?
307. **Baseline:** Dokumentiere: Welche Systeme sind End-of-Life?
308. **Baseline:** Dokumentiere: Welche Lizenzen laufen aus?
309. **Baseline:** Dokumentiere: Welche Systeme haben keine Owner?
310. **Baseline:** Dokumentiere: Welche Systeme dürfen nicht ausfallen?
311. **Target:** Dokumentiere: Welche Zielsysteme sind gesetzt?
312. **Target:** Dokumentiere: Welche Zieltechnologien sind verbindlich?
313. **Target:** Dokumentiere: Welche Architekturprinzipien gelten?
314. **Target:** Dokumentiere: Welche Qualitätseigenschaften werden verbessert?
315. **Target:** Dokumentiere: Welche Kosten sollen reduziert werden?
316. **Target:** Dokumentiere: Welche Prozesse werden verändert?
317. **Target:** Dokumentiere: Welche Datenflüsse entstehen neu?
318. **Target:** Dokumentiere: Welche Systeme werden abgeschaltet?
319. **Target:** Dokumentiere: Welche Systeme bleiben bewusst erhalten?
320. **Target:** Dokumentiere: Welche Nicht-Ziele gelten?
321. **Gap:** Dokumentiere: Was existiert heute und im Ziel nicht mehr?
322. **Gap:** Dokumentiere: Was existiert im Ziel, aber heute noch nicht?
323. **Gap:** Dokumentiere: Was bleibt, muss aber verändert werden?
324. **Gap:** Dokumentiere: Was wurde im Zielbild vergessen?
325. **Gap:** Dokumentiere: Welche Gaps sind kritisch?
326. **Gap:** Dokumentiere: Welche Gaps sind regulatorisch relevant?
327. **Gap:** Dokumentiere: Welche Gaps betreffen Daten?
328. **Gap:** Dokumentiere: Welche Gaps betreffen Security?
329. **Gap:** Dokumentiere: Welche Gaps betreffen Betrieb?
330. **Gap:** Dokumentiere: Welche Gaps betreffen Nutzer?
331. **Work Packages:** Dokumentiere: Welche Gaps können zusammen umgesetzt werden?
332. **Work Packages:** Dokumentiere: Welche Gaps müssen getrennt bleiben?
333. **Work Packages:** Dokumentiere: Welches Work Package liefert frühen Nutzen?
334. **Work Packages:** Dokumentiere: Welches Work Package reduziert Risiko?
335. **Work Packages:** Dokumentiere: Welches Work Package hat die meisten Abhängigkeiten?
336. **Work Packages:** Dokumentiere: Wer ist Owner?
337. **Work Packages:** Dokumentiere: Welche Skills werden gebraucht?
338. **Work Packages:** Dokumentiere: Welche Tests sind nötig?
339. **Work Packages:** Dokumentiere: Was ist out of scope?
340. **Work Packages:** Dokumentiere: Wann ist das Work Package fertig?
341. **Roadmap:** Dokumentiere: Was muss zuerst passieren?
342. **Roadmap:** Dokumentiere: Was kann parallel laufen?
343. **Roadmap:** Dokumentiere: Was darf nicht parallel laufen?
344. **Roadmap:** Dokumentiere: Wo liegt der kritische Pfad?
345. **Roadmap:** Dokumentiere: Welche Welle liefert sichtbaren Nutzen?
346. **Roadmap:** Dokumentiere: Welche Welle ist am riskantesten?
347. **Roadmap:** Dokumentiere: Welche Entscheidungspunkte brauchen wir?
348. **Roadmap:** Dokumentiere: Welche Puffer brauchen wir?
349. **Roadmap:** Dokumentiere: Welche Abhängigkeit ist ungeklärt?
350. **Roadmap:** Dokumentiere: Welche Termine sind extern fix?
351. **Transition:** Dokumentiere: Wie sieht Zwischenzustand eins aus?
352. **Transition:** Dokumentiere: Ist der Zwischenzustand betreibbar?
353. **Transition:** Dokumentiere: Welche alten Komponenten bleiben aktiv?
354. **Transition:** Dokumentiere: Welche neuen Komponenten sind aktiv?
355. **Transition:** Dokumentiere: Welche Daten liegen wo?
356. **Transition:** Dokumentiere: Wer supportet den Zustand?
357. **Transition:** Dokumentiere: Welche KPIs gelten?
358. **Transition:** Dokumentiere: Welche Risiken bleiben?
359. **Transition:** Dokumentiere: Was ist der Rollback?
360. **Transition:** Dokumentiere: Wann gilt die Transition als erreicht?
361. **Interoperability:** Dokumentiere: Welche Systeme müssen während der Migration sprechen?
362. **Interoperability:** Dokumentiere: Welche Protokolle ändern sich?
363. **Interoperability:** Dokumentiere: Welche Adapter brauchen wir?
364. **Interoperability:** Dokumentiere: Welche Daten werden synchronisiert?
365. **Interoperability:** Dokumentiere: Welche Schnittstellen müssen abwärtskompatibel bleiben?
366. **Interoperability:** Dokumentiere: Welche temporären Lösungen entstehen?
367. **Interoperability:** Dokumentiere: Wann werden temporäre Lösungen entfernt?
368. **Interoperability:** Dokumentiere: Wer testet die Schnittstellen?
369. **Interoperability:** Dokumentiere: Wie erkennen wir Synchronisationsfehler?
370. **Interoperability:** Dokumentiere: Welche Integrationen sind kritisch?
371. **Risk:** Dokumentiere: Was kann die Migration stoppen?
372. **Risk:** Dokumentiere: Wo droht Datenverlust?
373. **Risk:** Dokumentiere: Wo droht Downtime?
374. **Risk:** Dokumentiere: Wo droht Compliance-Verstoß?
375. **Risk:** Dokumentiere: Wo fehlen Skills?
376. **Risk:** Dokumentiere: Wo ist Budget unsicher?
377. **Risk:** Dokumentiere: Welche Annahme ist riskant?
378. **Risk:** Dokumentiere: Was ist Initial Risk?
379. **Risk:** Dokumentiere: Was ist Residual Risk?
380. **Risk:** Dokumentiere: Wer akzeptiert das Restrisiko?
381. **Governance:** Dokumentiere: Wer entscheidet über Scope?
382. **Governance:** Dokumentiere: Wer entscheidet über Architekturabweichungen?
383. **Governance:** Dokumentiere: Wer entscheidet über Go/No-Go?
384. **Governance:** Dokumentiere: Wer darf Rollback auslösen?
385. **Governance:** Dokumentiere: Welche Reports braucht Management?
386. **Governance:** Dokumentiere: Welche Reports brauchen Teams?
387. **Governance:** Dokumentiere: Welche Reviewpunkte sind verpflichtend?
388. **Governance:** Dokumentiere: Welche Abweichungen sind genehmigungspflichtig?
389. **Governance:** Dokumentiere: Wie wird Change dokumentiert?
390. **Governance:** Dokumentiere: Wie wird das Repository aktualisiert?
391. **Cutover:** Dokumentiere: Wann beginnt der Cutover?
392. **Cutover:** Dokumentiere: Wann endet das Rückfallfenster?
393. **Cutover:** Dokumentiere: Welche Vorbedingungen müssen erfüllt sein?
394. **Cutover:** Dokumentiere: Welche Schritte sind sequenziell?
395. **Cutover:** Dokumentiere: Wer führt welchen Schritt aus?
396. **Cutover:** Dokumentiere: Welche Smoke Tests gibt es?
397. **Cutover:** Dokumentiere: Wann wird zurückgerollt?
398. **Cutover:** Dokumentiere: Wer informiert Nutzer?
399. **Cutover:** Dokumentiere: Wer informiert Management?
400. **Cutover:** Dokumentiere: Was passiert nach Go-Live?
401. **Baseline:** Entscheide: Welche Systeme existieren heute wirklich?
402. **Baseline:** Entscheide: Welche Systeme sind nicht offiziell dokumentiert?
403. **Baseline:** Entscheide: Welche Schnittstellen sind kritisch?
404. **Baseline:** Entscheide: Welche Batch-Prozesse laufen nachts?
405. **Baseline:** Entscheide: Welche Datenbanken sind führend?
406. **Baseline:** Entscheide: Welche manuellen Workarounds gibt es?
407. **Baseline:** Entscheide: Welche Systeme sind End-of-Life?
408. **Baseline:** Entscheide: Welche Lizenzen laufen aus?
409. **Baseline:** Entscheide: Welche Systeme haben keine Owner?
410. **Baseline:** Entscheide: Welche Systeme dürfen nicht ausfallen?
411. **Target:** Entscheide: Welche Zielsysteme sind gesetzt?
412. **Target:** Entscheide: Welche Zieltechnologien sind verbindlich?
413. **Target:** Entscheide: Welche Architekturprinzipien gelten?
414. **Target:** Entscheide: Welche Qualitätseigenschaften werden verbessert?
415. **Target:** Entscheide: Welche Kosten sollen reduziert werden?
416. **Target:** Entscheide: Welche Prozesse werden verändert?
417. **Target:** Entscheide: Welche Datenflüsse entstehen neu?
418. **Target:** Entscheide: Welche Systeme werden abgeschaltet?
419. **Target:** Entscheide: Welche Systeme bleiben bewusst erhalten?
420. **Target:** Entscheide: Welche Nicht-Ziele gelten?
421. **Gap:** Entscheide: Was existiert heute und im Ziel nicht mehr?
422. **Gap:** Entscheide: Was existiert im Ziel, aber heute noch nicht?
423. **Gap:** Entscheide: Was bleibt, muss aber verändert werden?
424. **Gap:** Entscheide: Was wurde im Zielbild vergessen?
425. **Gap:** Entscheide: Welche Gaps sind kritisch?
426. **Gap:** Entscheide: Welche Gaps sind regulatorisch relevant?
427. **Gap:** Entscheide: Welche Gaps betreffen Daten?
428. **Gap:** Entscheide: Welche Gaps betreffen Security?
429. **Gap:** Entscheide: Welche Gaps betreffen Betrieb?
430. **Gap:** Entscheide: Welche Gaps betreffen Nutzer?
431. **Work Packages:** Entscheide: Welche Gaps können zusammen umgesetzt werden?
432. **Work Packages:** Entscheide: Welche Gaps müssen getrennt bleiben?
433. **Work Packages:** Entscheide: Welches Work Package liefert frühen Nutzen?
434. **Work Packages:** Entscheide: Welches Work Package reduziert Risiko?
435. **Work Packages:** Entscheide: Welches Work Package hat die meisten Abhängigkeiten?
436. **Work Packages:** Entscheide: Wer ist Owner?
437. **Work Packages:** Entscheide: Welche Skills werden gebraucht?
438. **Work Packages:** Entscheide: Welche Tests sind nötig?
439. **Work Packages:** Entscheide: Was ist out of scope?
440. **Work Packages:** Entscheide: Wann ist das Work Package fertig?
441. **Roadmap:** Entscheide: Was muss zuerst passieren?
442. **Roadmap:** Entscheide: Was kann parallel laufen?
443. **Roadmap:** Entscheide: Was darf nicht parallel laufen?
444. **Roadmap:** Entscheide: Wo liegt der kritische Pfad?
445. **Roadmap:** Entscheide: Welche Welle liefert sichtbaren Nutzen?
446. **Roadmap:** Entscheide: Welche Welle ist am riskantesten?
447. **Roadmap:** Entscheide: Welche Entscheidungspunkte brauchen wir?
448. **Roadmap:** Entscheide: Welche Puffer brauchen wir?
449. **Roadmap:** Entscheide: Welche Abhängigkeit ist ungeklärt?
450. **Roadmap:** Entscheide: Welche Termine sind extern fix?
451. **Transition:** Entscheide: Wie sieht Zwischenzustand eins aus?
452. **Transition:** Entscheide: Ist der Zwischenzustand betreibbar?
453. **Transition:** Entscheide: Welche alten Komponenten bleiben aktiv?
454. **Transition:** Entscheide: Welche neuen Komponenten sind aktiv?
455. **Transition:** Entscheide: Welche Daten liegen wo?
456. **Transition:** Entscheide: Wer supportet den Zustand?
457. **Transition:** Entscheide: Welche KPIs gelten?
458. **Transition:** Entscheide: Welche Risiken bleiben?
459. **Transition:** Entscheide: Was ist der Rollback?
460. **Transition:** Entscheide: Wann gilt die Transition als erreicht?
461. **Interoperability:** Entscheide: Welche Systeme müssen während der Migration sprechen?
462. **Interoperability:** Entscheide: Welche Protokolle ändern sich?
463. **Interoperability:** Entscheide: Welche Adapter brauchen wir?
464. **Interoperability:** Entscheide: Welche Daten werden synchronisiert?
465. **Interoperability:** Entscheide: Welche Schnittstellen müssen abwärtskompatibel bleiben?
466. **Interoperability:** Entscheide: Welche temporären Lösungen entstehen?
467. **Interoperability:** Entscheide: Wann werden temporäre Lösungen entfernt?
468. **Interoperability:** Entscheide: Wer testet die Schnittstellen?
469. **Interoperability:** Entscheide: Wie erkennen wir Synchronisationsfehler?
470. **Interoperability:** Entscheide: Welche Integrationen sind kritisch?
471. **Risk:** Entscheide: Was kann die Migration stoppen?
472. **Risk:** Entscheide: Wo droht Datenverlust?
473. **Risk:** Entscheide: Wo droht Downtime?
474. **Risk:** Entscheide: Wo droht Compliance-Verstoß?
475. **Risk:** Entscheide: Wo fehlen Skills?
476. **Risk:** Entscheide: Wo ist Budget unsicher?
477. **Risk:** Entscheide: Welche Annahme ist riskant?
478. **Risk:** Entscheide: Was ist Initial Risk?
479. **Risk:** Entscheide: Was ist Residual Risk?
480. **Risk:** Entscheide: Wer akzeptiert das Restrisiko?
481. **Governance:** Entscheide: Wer entscheidet über Scope?
482. **Governance:** Entscheide: Wer entscheidet über Architekturabweichungen?
483. **Governance:** Entscheide: Wer entscheidet über Go/No-Go?
484. **Governance:** Entscheide: Wer darf Rollback auslösen?
485. **Governance:** Entscheide: Welche Reports braucht Management?
486. **Governance:** Entscheide: Welche Reports brauchen Teams?
487. **Governance:** Entscheide: Welche Reviewpunkte sind verpflichtend?
488. **Governance:** Entscheide: Welche Abweichungen sind genehmigungspflichtig?
489. **Governance:** Entscheide: Wie wird Change dokumentiert?
490. **Governance:** Entscheide: Wie wird das Repository aktualisiert?
491. **Cutover:** Entscheide: Wann beginnt der Cutover?
492. **Cutover:** Entscheide: Wann endet das Rückfallfenster?
493. **Cutover:** Entscheide: Welche Vorbedingungen müssen erfüllt sein?
494. **Cutover:** Entscheide: Welche Schritte sind sequenziell?
495. **Cutover:** Entscheide: Wer führt welchen Schritt aus?
496. **Cutover:** Entscheide: Welche Smoke Tests gibt es?
497. **Cutover:** Entscheide: Wann wird zurückgerollt?
498. **Cutover:** Entscheide: Wer informiert Nutzer?
499. **Cutover:** Entscheide: Wer informiert Management?
500. **Cutover:** Entscheide: Was passiert nach Go-Live?
## 33. Abschluss

Migration Planning ist ein Führungsinstrument für Veränderung. Es bringt Architektur, Umsetzung, Risiken, Finanzen, Betrieb und Menschen in eine gemeinsame Ordnung. Ein guter Migrationsplan ersetzt nicht die Realität, aber er macht sie steuerbar.

Der wichtigste Satz zum Schluss:

> Eine Migration ist erst dann gut geplant, wenn jeder kritische Schritt einen Zweck, einen Owner, einen Test, einen Rückweg und einen messbaren Nutzen hat.
