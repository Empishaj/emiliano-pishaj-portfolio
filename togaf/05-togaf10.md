# TOGAF Gap Analysis

## Schnelle Start-Checkliste

1. Ist der Scope der Gap Analysis klar?
2. Ist die Baseline Architecture ausreichend beschrieben?
3. Ist die Target Architecture ausreichend beschrieben?
4. Sind die Architekturdomänen klar getrennt?
5. Sind die Stakeholder bekannt?
6. Ist die Granularität der Analyse festgelegt?
7. Ist entschieden, ob Business, Data, Application und Technology getrennt bewertet werden?
8. Gibt es eine gemeinsame Begriffsliste?
9. Gibt es eine vorbereitete Gap-Matrix?
10. Ist klar, wie Retained, Added, Eliminated und Accidentally Eliminated verwendet werden?
11. Ist klar, wie Gaps priorisiert werden?
12. Ist klar, wie Risiken aus Gaps abgeleitet werden?
13. Ist klar, wer die Ergebnisse genehmigt?
14. Ist klar, wie Ergebnisse an Phase E und Phase F übergeben werden?
15. Ist klar, wer nach dem Workshop die Dokumentation pflegt?


## Die Grundidee

Eine Gap Analysis vergleicht zwei Zustände.

Der erste Zustand ist heute.

Der zweite Zustand ist morgen.

Heute ist die Baseline.

Morgen ist das Target.

Der Abstand dazwischen ist der Gap.

Der Gap zeigt dir, was verändert werden muss.

Manchmal musst du etwas neu einführen.

Manchmal musst du etwas behalten.

Manchmal musst du etwas abschalten.

Manchmal hast du in der Zielarchitektur etwas vergessen, das du weiterhin brauchst.

Genau diese letzte Kategorie ist besonders gefährlich.

Sie führt oft zu teuren Überraschungen.

Die Gap Analysis verhindert solche Überraschungen.

Sie zwingt dich, sauber zu vergleichen.

Sie macht Architektur umsetzbar.

Ohne Gap Analysis bleibt Architektur oft ein schönes Bild.

Mit Gap Analysis wird daraus ein belastbarer Änderungsplan.


## Was du als Coach zuerst verstehen musst

Als Coach erklärst du Gap Analysis nicht als Tabelle.

Du erklärst sie als Denkprozess.

Menschen verstehen Tabellen erst, wenn sie den Zweck verstanden haben.

Der Zweck ist nicht Dokumentation um der Dokumentation willen.

Der Zweck ist Entscheidungsvorbereitung.

Der Zweck ist Umsetzungsfähigkeit.

Der Zweck ist Risikotransparenz.

Der Zweck ist Priorisierung.

Der Zweck ist saubere Migration.

Wenn du nur fragst: Was fehlt?, bekommst du oft oberflächliche Antworten.

Wenn du fragst: Was passiert, wenn wir dieses Element verlieren?, bekommst du bessere Antworten.

Wenn du fragst: Welche Zielbausteine haben heute keine Grundlage?, bekommst du bessere Planung.

Wenn du fragst: Welche heutigen Bausteine werden morgen nicht mehr gebraucht?, bekommst du eine Abschaltliste.

Wenn du fragst: Welche Abhängigkeiten blockieren die Veränderung?, bekommst du eine Roadmap-Grundlage.


## Begriffswörterbuch

| Begriff | Einfache Erklärung | Coach-Hinweis |
| --- | --- | --- |
| Baseline Architecture | Der heutige Zustand. | Nicht schönreden. Baseline ist Realität. |
| Target Architecture | Der gewünschte zukünftige Zustand. | Nicht nur Wunschbild. Target muss erreichbar sein. |
| Gap | Der Unterschied zwischen heute und morgen. | Ein Gap ist zuerst neutral. |
| Retained | Bleibt erhalten. | Trotzdem absichern, damit es nicht beschädigt wird. |
| Added | Muss neu entstehen. | Wird später oft ein Projekt, Epic oder Arbeitspaket. |
| Eliminated | Wird bewusst entfernt. | Braucht Dekommissionierung und Übergangsplanung. |
| Accidentally Eliminated | Wurde versehentlich nicht im Ziel berücksichtigt. | Sofort prüfen, weil hier echte Schäden entstehen können. |
| Gap-Matrix | Tabelle für den systematischen Vergleich. | Die Matrix ist Werkzeug, nicht Selbstzweck. |
| Impact | Auswirkung eines Gaps. | Ohne Impact keine gute Priorisierung. |
| Urgency | Zeitliche Dringlichkeit. | Dringend ist nicht immer wichtig, aber oft kritisch. |
| Risk | Mögliches Problem aus einem Gap. | Jeder Gap kann ein Risiko erzeugen. |
| Roadmap | Geordneter Veränderungsplan. | Gaps liefern Material für die Roadmap. |
| Work Package | Umsetzbares Arbeitspaket. | Phase E und F brauchen solche Pakete. |


## TOGAF-Einordnung

Die Gap Analysis gehört zu den wichtigsten Arbeitstechniken im ADM.

ADM bedeutet Architecture Development Method.

Sie wird nicht nur einmal verwendet.

Sie wird in mehreren Phasen gebraucht.

Besonders wichtig ist sie in Phase B, Phase C und Phase D.

Phase B betrachtet die Business Architecture.

Phase C betrachtet Data Architecture und Application Architecture.

Phase D betrachtet Technology Architecture.

Die Ergebnisse gehen danach in Phase E.

Phase E heißt Opportunities and Solutions.

Dort werden Lösungsoptionen und Arbeitspakete abgeleitet.

Danach gehen die Ergebnisse in Phase F.

Phase F heißt Migration Planning.

Dort wird geplant, in welcher Reihenfolge die Veränderungen umgesetzt werden.

Damit ist die Gap Analysis eine Brücke.

Sie verbindet Architekturdenken mit Umsetzungsplanung.

| ADM-Phase | Domäne | Typische Gap-Frage | Output |
| --- | --- | --- | --- |
| Phase B | Business Architecture | Welche Geschäftsprozesse, Fähigkeiten oder Rollen fehlen? | Business-Gap-Liste |
| Phase C Data | Data Architecture | Welche Datenobjekte, Datenflüsse oder Qualitätsregeln fehlen? | Data-Gap-Liste |
| Phase C Application | Application Architecture | Welche Anwendungen, Schnittstellen oder Funktionen fehlen? | Application-Gap-Liste |
| Phase D | Technology Architecture | Welche Plattformen, Services oder Infrastrukturbausteine fehlen? | Technology-Gap-Liste |
| Phase E | Opportunities and Solutions | Welche Lösungen schließen mehrere Gaps sinnvoll zusammen? | Solution Building Blocks und Work Packages |
| Phase F | Migration Planning | In welcher Reihenfolge schließen wir die Gaps? | Migration Roadmap |


## Die vier Gap-Kategorien ausführlich


### Retained

Ein Element ist heute vorhanden und wird auch im Zielzustand gebraucht.

Coach-Hinweis: Retained klingt passiv, ist aber nicht immer ohne Arbeit. Manchmal muss ein vorhandenes Element stabilisiert, modernisiert oder geschützt werden.

Typische Aktionen:

- Weiter nutzen
- Abhängigkeiten prüfen
- Betriebsfähigkeit sichern
- Dokumentation aktualisieren


### Added

Ein Element existiert heute nicht, wird aber im Zielzustand benötigt.
Coach-Hinweis: Added ist meistens der Ursprung neuer Arbeit. Daraus entstehen häufig Projekte, Epics, Beschaffungsvorhaben oder Plattformaufgaben.

Typische Aktionen:

- Anforderung beschreiben
- Kosten grob schätzen
- Owner bestimmen
- Umsetzungsoptionen prüfen


### Eliminated

Ein Element existiert heute, wird aber im Zielzustand nicht mehr benötigt.

Hinweis: Eliminated bedeutet nicht einfach löschen. Abschalten ist ein Projekt mit Daten, Nutzern, Verträgen, Risiken und Kommunikation.

Typische Aktionen:

- Dekommissionierungsplan erstellen
- Abhängigkeiten lösen
- Daten migrieren oder archivieren
- Nutzer informieren


### Accidentally Eliminated

Ein Element existiert heute und wird weiter gebraucht, fehlt aber im Zielzustand.

Coach-Hinweis: Das ist die Warnlampe der Gap Analysis. Hier musst du sofort stoppen und prüfen, ob die Target Architecture falsch oder unvollständig ist.

Typische Aktionen:

- Target Architecture korrigieren
- Stakeholder informieren
- Auswirkung bewerten
- Entscheidung dokumentieren


## Schritt-für-Schritt-Prozess


### Schritt 1: Scope klären

Lege fest, welche Domäne, welcher Bereich, welches Produkt, welche Organisation und welcher Zeitraum betrachtet werden.

Fragen:

- Was gehört in den Scope?
- Was gehört ausdrücklich nicht in den Scope?
- Wer entscheidet über Scope-Änderungen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Scope klären


### Schritt 2: Baseline sammeln

Erfasse den heutigen Zustand mit Interviews, Dokumenten, Systemlisten, Prozesslandkarten und vorhandenen Architekturmodellen.

Coach-Fragen:

- Welche Quellen beschreiben die heutige Realität?
- Wo gibt es unsichere Annahmen?
- Welche Systeme oder Prozesse sind kritisch?

Typischer Output:

- Dokumentiertes Ergebnis zu: Baseline sammeln


### Schritt 3: Baseline validieren

Prüfe mit Fachexperten, ob die Beschreibung der Realität stimmt.

Coach-Fragen:

- Wer kennt die Realität am besten?
- Welche Aussage ist belegt?
- Welche Aussage ist nur Vermutung?

Typischer Output:

- Dokumentiertes Ergebnis zu: Baseline validieren


### Schritt 4: Target sammeln

Erfasse die Zielarchitektur aus Vision, Anforderungen, Zielbildern und Architekturentscheidungen.

Coach-Fragen:

- Welche Zielbilder sind genehmigt?
- Welche Anforderungen treiben das Target?
- Welche Prinzipien müssen eingehalten werden?

Typischer Output:

- Dokumentiertes Ergebnis zu: Target sammeln


### Schritt 5: Target validieren

Prüfe, ob die Zielarchitektur vollständig, verständlich und widerspruchsfrei ist.

Coach-Fragen:

- Welche Zielbestandteile fehlen möglicherweise?
- Sind alle Stakeholder-Bedarfe abgedeckt?
- Ist das Target in der gegebenen Zeit erreichbar?

Typischer Output:

- Dokumentiertes Ergebnis zu: Target validieren


### Schritt 6: Vergleichsebene festlegen

Entscheide, ob du Capabilities, Prozesse, Anwendungen, Datenobjekte, Schnittstellen oder Technologien vergleichst.

Coach-Fragen:

- Welche Ebene ist für die Entscheidung nützlich?
- Ist der Detailgrad für Management und Umsetzung passend?
- Brauchen wir mehrere Ebenen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Vergleichsebene festlegen


### Schritt 7: Gap-Matrix aufbauen

Erstelle eine Tabelle mit Baseline-Elementen und Target-Elementen.

Coach-Fragen:

- Ist die Matrix lesbar?
- Sind die Begriffe eindeutig?
- Kann ein Dritter die Matrix verstehen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Gap-Matrix aufbauen


### Schritt 8: Elemente klassifizieren

Ordne jedes Element Retained, Added, Eliminated oder Accidentally Eliminated zu.

Coach-Fragen:

- Was bleibt?
- Was kommt neu?
- Was fällt weg?
- Was wurde versehentlich vergessen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Elemente klassifizieren


### Schritt 9: Gaps beschreiben

Formuliere jeden Gap als klare Aussage in einem Satz.

Fragen:

- Ist der Gap in einem Satz verständlich?
- Ist er beobachtbar?
- Ist er ohne Lösungsvorwegnahme formuliert?

Typischer Output:

- Dokumentiertes Ergebnis zu: Gaps beschreiben


### Schritt 10: Auswirkung bewerten

Bewerte Business Impact, technischen Impact, Kostenwirkung, Sicherheitswirkung und Migrationswirkung.

Coach-Fragen:

- Was passiert, wenn wir den Gap nicht schließen?
- Wer ist betroffen?
- Welche Ziele sind gefährdet?

Typischer Output:

- Dokumentiertes Ergebnis zu: Auswirkung bewerten


### Schritt 11: Dringlichkeit bewerten

Lege fest, wann der Gap spätestens bearbeitet werden muss.

Coach-Fragen:

- Bis wann muss das gelöst sein?
- Was blockiert andere Arbeiten?
- Gibt es regulatorische oder Vertragsfristen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Dringlichkeit bewerten


### Schritt 12: Priorität berechnen

Kombiniere Impact und Dringlichkeit zu einer Priorität.

Coach-Fragen:

- Welche Gaps sind kritisch?
- Welche Gaps bringen hohen Nutzen?
- Welche Gaps können warten?

Typischer Output:

- Dokumentiertes Ergebnis zu: Priorität berechnen


### Schritt 13: Risiken ableiten

Formuliere aus kritischen Gaps konkrete Risiken.

Coach-Fragen:

- Welches Risiko entsteht?
- Wie hoch ist das Anfangsrisiko?
- Welches Restrisiko bleibt nach Maßnahmen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Risiken ableiten


### Schritt 14: Maßnahmen ableiten

Entscheide, ob der Gap durch Projekt, Änderung, Beschaffung, Prozessanpassung oder Abschaltung geschlossen wird.

Coach-Fragen:

- Welche Maßnahme passt am besten?
- Kann ein Gap durch mehrere Maßnahmen geschlossen werden?
- Gibt es eine einfache Lösung?

Typischer Output:

- Dokumentiertes Ergebnis zu: Maßnahmen ableiten


### Schritt 15: Owner bestimmen

Jeder Gap braucht eine verantwortliche Person oder Rolle.

Coach-Fragen:

- Wer verantwortet die Entscheidung?
- Wer verantwortet die Umsetzung?
- Wer muss informiert werden?

Typischer Output:

- Dokumentiertes Ergebnis zu: Owner bestimmen


### Schritt 16: Review durchführen

Lasse die Ergebnisse durch Architecture Board, Fachbereich und Umsetzungsteam prüfen.

Coach-Fragen:

- Wer muss zustimmen?
- Wer kann fachlich widersprechen?
- Welche Entscheidung ist offen?

Typischer Output:

- Dokumentiertes Ergebnis zu: Review durchführen


### Schritt 17: Repository aktualisieren

Speichere Matrix, Entscheidungen, Gaps, Risiken und Maßnahmen im Architecture Repository.

Coach-Fragen:

- Wo wird das Ergebnis gespeichert?
- Wer pflegt es?
- Wie wird die Version gesichert?

Typischer Output:

- Dokumentiertes Ergebnis zu: Repository aktualisieren


### Schritt 18: Übergabe an Phase E/F

Gib priorisierte Gaps an Opportunities and Solutions und Migration Planning weiter.

Coach-Fragen:

- Was braucht Phase E?
- Was braucht Phase F?
- Welche Informationen fehlen für die Roadmap?

Typischer Output:

- Dokumentiertes Ergebnis zu: Übergabe an Phase E/F


## Gap-Matrix-Vorlage

Die folgende Vorlage kannst du direkt in Markdown, Excel, Confluence oder ein EA-Tool übertragen.

| Baseline-Element | Target-Element | Kategorie | Gap-Beschreibung | Impact | Dringlichkeit | Priorität | Owner | Nächste Aktion |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Kundenportal alt | Kundenportal neu | Retained/Changed | Portal bleibt, muss aber modernisiert werden. | Hoch | Kurzfristig | Hoch | Application Owner | Modernisierungsumfang klären |
| Manueller Report | Automatisches Reporting | Eliminated | Manueller Report wird durch automatisiertes Reporting ersetzt. | Mittel | Mittelfristig | Mittel | Business Owner | Datenquelle prüfen |
| Keine API-Gateway-Lösung | API Gateway | Added | API Gateway wird für Zielarchitektur benötigt. | Hoch | Kurzfristig | Hoch | Technology Architect | Lösungsoptionen prüfen |
| Compliance Export | Fehlt im Target | Accidentally Eliminated | Pflichtreporting wurde im Zielbild vergessen. | Kritisch | Sofort | Sehr hoch | Enterprise Architect | Target korrigieren |


## Bewertungsmodell für Gaps

Ein Gap ohne Bewertung ist nur eine Beobachtung.

Ein bewerteter Gap wird zur Entscheidungsgrundlage.

| Bewertungskriterium | Frage | Skala | Beispiel |
| --- | --- | --- | --- |
| Business Impact | Wie stark ist das Geschäft betroffen? | 1 bis 5 | 5 bei Umsatz- oder Compliance-Auswirkung |
| Technical Impact | Wie stark ist die technische Landschaft betroffen? | 1 bis 5 | 5 bei zentraler Plattform oder Kernintegration |
| Security Impact | Entsteht ein Sicherheitsrisiko? | 1 bis 5 | 5 bei hoher Exposition oder sensiblen Daten |
| Data Impact | Sind wichtige Daten betroffen? | 1 bis 5 | 5 bei Stammdaten oder regulatorischen Daten |
| Migration Impact | Blockiert der Gap die Migration? | 1 bis 5 | 5 bei kritischer Abhängigkeit |
| Urgency | Wie schnell muss gehandelt werden? | 1 bis 5 | 5 bei sofortigem Bedarf |


Eine einfache Formel für Priorität kann so aussehen:

```text
Priorität = höchster Impact-Wert + Dringlichkeit + Blocker-Zuschlag
```

Der Blocker-Zuschlag ist 0, 1 oder 2.

0 bedeutet: Der Gap blockiert nichts.

1 bedeutet: Der Gap blockiert einzelne Arbeitspakete.

2 bedeutet: Der Gap blockiert die Zielarchitektur oder einen Go-Live.


## Prioritätsklassen

| Klasse | Bedeutung | Entscheidung | Beispiel |
| --- | --- | --- | --- |
| P1 | Sofort kritisch | Muss vor weiteren Schritten behandelt werden. | Pflichtfunktion fehlt im Target. |
| P2 | Hoch | Muss in die nächste Roadmap-Welle. | Zentrale Integration fehlt. |
| P3 | Mittel | Einplanen, aber nicht sofort. | Reporting-Komfortfunktion fehlt. |
| P4 | Niedrig | Später prüfen oder bündeln. | Nice-to-have-Funktion. |


## Rollenmodell

| Rolle | Aufgabe in der Gap Analysis | Typische Fragen |
| --- | --- | --- |
| Enterprise Architect | Führt die Methode, sichert Konsistenz und Governance. | Passt der Gap zur Zielarchitektur? |
| Business Architect | Bewertet Prozesse, Fähigkeiten und Organisation. | Welche Business Capability ist betroffen? |
| Data Architect | Bewertet Datenobjekte, Datenqualität und Datenflüsse. | Welche Daten fehlen oder verändern sich? |
| Application Architect | Bewertet Anwendungen, Schnittstellen und Funktionen. | Welche Applikation muss neu, geändert oder abgeschaltet werden? |
| Technology Architect | Bewertet Plattformen, Infrastruktur und Technologiebausteine. | Welche technische Grundlage fehlt? |
| Security Architect | Bewertet Risiken, Kontrollen und Schutzbedarf. | Welches Risiko entsteht aus dem Gap? |
| Product Owner | Bewertet Nutzen, Priorität und fachlichen Bedarf. | Welcher Wert entsteht durch das Schließen des Gaps? |
| Project Manager | Bewertet Aufwand, Abhängigkeit und Zeitplan. | Wann kann der Gap realistisch geschlossen werden? |
| Architecture Board | Genehmigt Ergebnisse und Ausnahmen. | Ist die Entscheidung tragfähig? |


## RACI-Vorlage

| Aktivität | Enterprise Architect | Domain Architect | Business Owner | Security Architect | Project Manager | Architecture Board |
| --- | --- | --- | --- | --- | --- | --- |
| Scope festlegen | A/R | C | C | C | C | I |
| Baseline erfassen | A | R | C | C | I | I |
| Target validieren | A | R | C | C | I | C |
| Gap-Matrix erstellen | A/R | R | C | C | I | I |
| Gap bewerten | A | R | C | R | C | I |
| Risiken ableiten | A | C | C | R | I | I |
| Maßnahmen ableiten | A | R | C | C | R | I |
| Ergebnis genehmigen | R | C | C | C | I | A |

Legende: R = Responsible, A = Accountable, C = Consulted, I = Informed.


## Workshop-Design


### Workshop 1: Scope und Baseline

Ziel ist ein gemeinsames Bild der heutigen Realität.

Agenda:

- Einstieg und Zielklärung
- Gemeinsame Begriffsklärung
- Arbeit an der Matrix oder am relevanten Ausschnitt
- Prüfung offener Fragen
- Entscheidung über nächste Schritte
- Dokumentation der offenen Punkte

Coach-Satz:

> Wir suchen heute keine perfekte Antwort, sondern eine belastbare gemeinsame Sicht.


### Workshop 2: Target Review

Ziel ist ein gemeinsames Verständnis des Zielbildes.

Agenda:

- Einstieg und Zielklärung
- Gemeinsame Begriffsklärung
- Arbeit an der Matrix oder am relevanten Ausschnitt
- Prüfung offener Fragen
- Entscheidung über nächste Schritte
- Dokumentation der offenen Punkte

Coach-Satz:

> Wir suchen heute keine perfekte Antwort, sondern eine belastbare gemeinsame Sicht.


### Workshop 3: Gap Discovery

Ziel ist die erste vollständige Gap-Liste.

Agenda:

- Einstieg und Zielklärung
- Gemeinsame Begriffsklärung
- Arbeit an der Matrix oder am relevanten Ausschnitt
- Prüfung offener Fragen
- Entscheidung über nächste Schritte
- Dokumentation der offenen Punkte

Coach-Satz:

> Wir suchen heute keine perfekte Antwort, sondern eine belastbare gemeinsame Sicht.


### Workshop 4: Gap Bewertung

Ziel ist Priorisierung nach Impact und Dringlichkeit.

Agenda:

- Einstieg und Zielklärung
- Gemeinsame Begriffsklärung
- Arbeit an der Matrix oder am relevanten Ausschnitt
- Prüfung offener Fragen
- Entscheidung über nächste Schritte
- Dokumentation der offenen Punkte

Coach-Satz:

> Wir suchen heute keine perfekte Antwort, sondern eine belastbare gemeinsame Sicht.


### Workshop 5: Risiken und Maßnahmen

Ziel ist die Übersetzung in Risiken, Maßnahmen und Owner.

Agenda:

- Einstieg und Zielklärung
- Gemeinsame Begriffsklärung
- Arbeit an der Matrix oder am relevanten Ausschnitt
- Prüfung offener Fragen
- Entscheidung über nächste Schritte
- Dokumentation der offenen Punkte

Coach-Satz:

> Wir suchen heute keine perfekte Antwort, sondern eine belastbare gemeinsame Sicht.


### Workshop 6: Board Review

Ziel ist Entscheidung und Freigabe.

Agenda:

- Einstieg und Zielklärung
- Gemeinsame Begriffsklärung
- Arbeit an der Matrix oder am relevanten Ausschnitt
- Prüfung offener Fragen
- Entscheidung über nächste Schritte
- Dokumentation der offenen Punkte

Coach-Satz:

> Wir suchen heute keine perfekte Antwort, sondern eine belastbare gemeinsame Sicht.


## Domänenspezifische Gap-Fragen


### Business Architecture

- Welche Business Capabilities fehlen im Zielbild?
- Welche heutigen Prozesse werden künftig nicht mehr gebraucht?
- Welche Rollen verändern sich?
- Welche Organisationseinheiten sind betroffen?
- Welche manuellen Tätigkeiten sollen automatisiert werden?
- Welche Wertströme werden verändert?
- Welche Business-Regeln fehlen im Target?
- Welche Kundenerlebnisse ändern sich?
- Welche Partnerprozesse sind betroffen?
- Welche fachlichen Risiken entstehen?


### Data Architecture

- Welche Datenobjekte fehlen im Target?
- Welche Datenquellen werden abgeschaltet?
- Welche Daten müssen migriert werden?
- Welche Datenqualitätsregeln fehlen?
- Welche Datenflüsse verändern sich?
- Welche Verantwortlichkeiten für Daten fehlen?
- Welche Stammdaten sind betroffen?
- Welche Reports verlieren ihre Grundlage?
- Welche Aufbewahrungsregeln sind relevant?
- Welche Datenklassifizierung ist nötig?


### Application Architecture

- Welche Anwendungen bleiben?
- Welche Anwendungen werden ersetzt?
- Welche Anwendungen fehlen?
- Welche Schnittstellen fehlen?
- Welche Funktionen werden doppelt gebaut?
- Welche Legacy-Abhängigkeiten bleiben bestehen?
- Welche APIs müssen neu entstehen?
- Welche Benutzergruppen sind betroffen?
- Welche Integrationen blockieren die Migration?
- Welche Anwendung ist Owner eines fachlichen Objekts?


### Technology Architecture

- Welche Plattformen fehlen?
- Welche Infrastruktur ist End-of-Life?
- Welche Cloud-Services werden benötigt?
- Welche Netzwerkanforderungen ändern sich?
- Welche Betriebsfähigkeiten fehlen?
- Welche Monitoring-Fähigkeiten fehlen?
- Welche Sicherheitskontrollen fehlen?
- Welche Performance-Anforderungen werden nicht erfüllt?
- Welche Deployment-Fähigkeiten fehlen?
- Welche Standards werden verletzt?


### Security and Risk

- Welche Schutzmaßnahmen fehlen im Target?
- Welche heutigen Kontrollen werden versehentlich entfernt?
- Welche Risiken entstehen durch neue Komponenten?
- Welche regulatorischen Anforderungen sind betroffen?
- Welche Identitäts- und Zugriffsregeln fehlen?
- Welche Logging-Anforderungen fehlen?
- Welche Audit-Anforderungen fehlen?
- Welche Segmentation-Anforderungen fehlen?
- Welche Schwachstellen werden durch den Gap sichtbar?
- Welche Ausnahme müsste genehmigt werden?


## Typische Gap-Muster

| Muster | Beschreibung | Coach-Aktion |
| --- | --- | --- |
| Capability Gap | Eine Fähigkeit fehlt im Ziel oder wird heute nicht ausreichend unterstützt. | Business Capability Map prüfen. |
| Process Gap | Ein Prozess ist im Ziel anders vorgesehen als heute. | Prozessmodell und Verantwortlichkeiten prüfen. |
| Application Gap | Eine Anwendung fehlt, bleibt zu lange bestehen oder wird falsch ersetzt. | Application Portfolio prüfen. |
| Data Gap | Daten fehlen, sind schlecht, werden doppelt geführt oder falsch migriert. | Datenmodell und Datenflüsse prüfen. |
| Integration Gap | Systeme können nicht sauber miteinander sprechen. | Schnittstellenlandkarte prüfen. |
| Technology Gap | Plattform oder Infrastruktur fehlt für das Zielbild. | Technologieportfolio prüfen. |
| Security Gap | Schutzmaßnahme, Kontrolle oder Nachweis fehlt. | Security Controls und Risikoanalyse prüfen. |
| Operating Model Gap | Rollen, Prozesse oder Betriebsfähigkeit fehlen. | Betriebsmodell prüfen. |
| Governance Gap | Entscheidungs- oder Kontrollprozess fehlt. | Governance Framework prüfen. |
| Migration Gap | Der Weg von heute nach morgen ist nicht machbar oder nicht geplant. | Roadmap-Abhängigkeiten prüfen. |


## Beispiele aus der Praxis

| Beispiel | Situation | Kategorie | Aktion |
| --- | --- | --- | --- |
| Legacy-CRM wird abgelöst | Das alte CRM existiert heute. Im Target gibt es ein neues CRM. Ein Reporting-Export wurde vergessen. | Accidentally Eliminated | Target korrigieren und Reporting-Anforderung aufnehmen. |
| API Gateway fehlt | Mehrere Zielanwendungen brauchen API Management. Heute gibt es kein API Gateway. | Added | Lösungsoptionen bewerten und Platform Work Package erstellen. |
| Manuelle Freigabe entfällt | Ein manueller Freigabeprozess wird durch automatisierten Workflow ersetzt. | Eliminated | Prozessabschaltung planen und Rollen ändern. |
| Datenbank bleibt | Die Kundendatenbank bleibt im Zielbild erhalten. | Retained | Betriebsfähigkeit, Backup und Schnittstellen prüfen. |
| Monitoring fehlt | Neue Cloud-Komponenten sind geplant, aber Monitoring ist nicht beschrieben. | Added | Observability-Anforderung ergänzen. |
| Zugriffsmodell unklar | Target beschreibt Anwendungen, aber keine Berechtigungsstruktur. | Added | Identity- und Access-Anforderungen aufnehmen. |


## Vom Gap zum Work Package

Ein häufiger Fehler ist, Gaps nur zu dokumentieren.

Dokumentation allein verändert nichts.

Jeder relevante Gap muss in eine Entscheidung oder eine Arbeit übersetzt werden.

In TOGAF passiert diese Übersetzung vor allem in Phase E und Phase F.

Phase E sucht passende Lösungsbausteine.

Phase F plant die Reihenfolge.

Du kannst dir das als Pipeline vorstellen.

```text
Gap -> Auswirkung -> Risiko -> Maßnahme -> Work Package -> Roadmap -> Umsetzung -> Nachweis
```

| Stufe | Frage | Ergebnis |
| --- | --- | --- |
| Gap | Was fehlt oder verändert sich? | Klare Gap-Beschreibung |
| Auswirkung | Warum ist das relevant? | Impact-Bewertung |
| Risiko | Was kann schiefgehen? | Risk Statement |
| Maßnahme | Was tun wir dagegen? | Option oder Entscheidung |
| Work Package | Wie wird es umgesetzt? | Umsetzungspaket |
| Roadmap | Wann wird es umgesetzt? | Zeitliche Einordnung |
| Nachweis | Wie zeigen wir, dass es erledigt ist? | Akzeptanzkriterien |


## Template: Gap-Steckbrief

```text
Gap-ID: [ausfüllen]
Titel: [ausfüllen]
Domäne: [ausfüllen]
Baseline-Element: [ausfüllen]
Target-Element: [ausfüllen]
Kategorie: [ausfüllen]
Beschreibung: [ausfüllen]
Business Impact: [ausfüllen]
Technical Impact: [ausfüllen]
Security Impact: [ausfüllen]
Data Impact: [ausfüllen]
Migration Impact: [ausfüllen]
Dringlichkeit: [ausfüllen]
Priorität: [ausfüllen]
Abhängigkeiten: [ausfüllen]
Risiko: [ausfüllen]
Empfohlene Maßnahme: [ausfüllen]
Owner: [ausfüllen]
Entscheidungsstatus: [ausfüllen]
Nächster Review: [ausfüllen]
Nachweis für Schließung: [ausfüllen]
```


## Template: Risk Statement aus einem Gap

```text
Wenn [Gap nicht geschlossen wird],
dann kann [unerwünschtes Ereignis] eintreten,
was zu [Auswirkung auf Business, Technik, Sicherheit oder Compliance] führt.
Daher wird [Maßnahme] benötigt,
und [Owner] verantwortet die weitere Bearbeitung bis [Datum/Meilenstein].
```


## Template: Executive Summary

```text
Ziel der Analyse:
[Kurze Beschreibung des betrachteten Architektur-Scopes]

Wichtigste Erkenntnisse:
1. [Erkenntnis]
2. [Erkenntnis]
3. [Erkenntnis]

Kritische Gaps:
- [Gap-ID] [Titel] [Auswirkung]

Entscheidungsbedarf:
- [Welche Entscheidung braucht das Board?]

Empfohlene nächste Schritte:
- [Schritt]
- [Schritt]
- [Schritt]
```


## Qualitätskriterien einer guten Gap Analysis

- Sie hat einen klaren Scope.
- Sie trennt Ist und Soll sauber.
- Sie verwendet einheitliche Begriffe.
- Sie ist nicht nur technisch.
- Sie betrachtet Business, Daten, Anwendungen, Technologie und Betrieb.
- Sie macht Annahmen sichtbar.
- Sie markiert Unsicherheiten.
- Sie unterscheidet bewusste Abschaltung von versehentlichem Verlust.
- Sie bewertet Auswirkung und Dringlichkeit.
- Sie benennt Owner.
- Sie erzeugt umsetzbare Folgearbeit.
- Sie wird versioniert.
- Sie wird im Architecture Repository abgelegt.
- Sie wird durch Governance geprüft.
- Sie wird nach Änderungen aktualisiert.


## Anti-Patterns

| Anti-Pattern | Beschreibung | Korrektur |
| --- | --- | --- |
| Die Wunschliste | Alles, was jemand haben möchte, wird als Gap erfasst. | Nur aufnehmen, was aus Baseline-Target-Vergleich oder Anforderungen ableitbar ist. |
| Die Technikfalle | Nur Anwendungen und Infrastruktur werden betrachtet. | Business, Daten, Betrieb und Governance einbeziehen. |
| Die Excel-Wand | Die Matrix wird riesig und niemand versteht sie. | Management- und Detailversion trennen. |
| Der blinde Fleck | Accidentally Eliminated wird nicht geprüft. | Jedes Baseline-Element aktiv gegen Target prüfen. |
| Der Verantwortungsnebel | Gaps haben keine Owner. | Jeder relevante Gap braucht Owner und nächsten Schritt. |
| Der Entscheidungsstau | Gaps werden gesammelt, aber nicht entschieden. | Regelmäßiges Board Review einplanen. |
| Der perfekte Stillstand | Man wartet auf vollständige Daten. | Mit ausreichend guter Baseline starten und Unsicherheiten markieren. |
| Der Roadmap-Bruch | Gap Analysis endet vor Phase E. | Übergabeformat für Work Packages erzwingen. |


## 30-60-90-Tage-Plan


### Tag 1 bis 30: Grundlagen schaffen

- Scope klären
- Stakeholder identifizieren
- Baseline-Quellen sammeln
- Target-Dokumente sammeln
- Begriffe festlegen
- Matrix-Vorlage erstellen
- Ersten Workshop planen


### Tag 31 bis 60: Gaps sichtbar machen

- Baseline validieren
- Target validieren
- Gap-Matrix befüllen
- Accidental Eliminations prüfen
- Erste Impact-Bewertung durchführen
- Risiken formulieren
- Owner vorschlagen


### Tag 61 bis 90: In Umsetzung übersetzen

- Prioritäten finalisieren
- Maßnahmen ableiten
- Work Packages vorbereiten
- Board Review durchführen
- Repository aktualisieren
- Roadmap-Input liefern
- Lessons Learned dokumentieren


## Ein vollständiges Mini-Beispiel

Ausgangslage: Ein Unternehmen möchte sein altes Händlerportal modernisieren.

Baseline:

- Altes Portal auf Legacy-Stack
- Manuelle Angebotsfreigabe
- CSV-Export für Controlling
- Keine zentrale API-Schicht
- Monitoring nur auf Serverebene
- Benutzerrechte direkt in der Anwendung gepflegt

Target:

- Neues Portal mit moderner Webarchitektur
- Automatisierter Freigabeworkflow
- Zentrale API-Schicht
- Application Monitoring
- Zentraler Identity Provider

Erkannte Gaps:

| Gap-ID | Gap | Kategorie | Bewertung | Aktion |
| --- | --- | --- | --- | --- |
| G-001 | Legacy-Portal wird ersetzt. | Eliminated | Hoch | Ablöseplan und Datenmigration erstellen |
| G-002 | Automatisierter Workflow fehlt heute. | Added | Hoch | Workflow-Lösung definieren |
| G-003 | CSV-Export fehlt im Target. | Accidentally Eliminated | Kritisch | Target sofort korrigieren |
| G-004 | API-Schicht fehlt heute. | Added | Hoch | API-Gateway und Governance definieren |
| G-005 | Servermonitoring reicht für Target nicht. | Added | Mittel | Application Monitoring ergänzen |
| G-006 | Berechtigungen werden künftig zentral verwaltet. | Eliminated/Added | Hoch | Identity-Migration planen |


## Coach-Sprache für schwierige Situationen

| Situation | Coach-Antwort |
| --- | --- |
| Wenn jemand sagt: Das wissen wir doch alles. | Dann antworte: Gut, dann machen wir es sichtbar, damit andere es auch nutzen können. |
| Wenn jemand sagt: Dafür haben wir keine Zeit. | Dann antworte: Genau deshalb brauchen wir die Analyse, damit wir keine Zeit in falsche Arbeit stecken. |
| Wenn jemand sagt: Die Zielarchitektur ist doch schon fertig. | Dann antworte: Fertig ist sie erst, wenn geprüft ist, was im Vergleich zur Realität fehlt. |
| Wenn jemand sagt: Das ist zu detailliert. | Dann antworte: Dann wählen wir zwei Ebenen: Managementsicht und Umsetzungssicht. |
| Wenn jemand sagt: Das ist nur Dokumentation. | Dann antworte: Nein, das ist Entscheidungsvorbereitung. |
| Wenn jemand sagt: Das ist Sache der IT. | Dann antworte: Gaps entstehen oft im Zusammenspiel aus Business, Daten, Anwendungen und Betrieb. |


## Review-Fragen für das Architecture Board

- Ist der Scope der Analyse genehmigt?
- Sind Baseline und Target ausreichend belastbar?
- Sind kritische Gaps vollständig sichtbar?
- Wurden Accidentally Eliminated-Elemente geprüft?
- Sind Risiken aus Gaps sauber formuliert?
- Sind Prioritäten nachvollziehbar?
- Sind Owner benannt?
- Sind Maßnahmen realistisch?
- Sind Abhängigkeiten sichtbar?
- Ist die Übergabe an Phase E und Phase F möglich?
- Gibt es offene Entscheidungen?
- Gibt es Ausnahmen, die genehmigt werden müssen?


## Definition of Done

- Die Baseline ist dokumentiert und validiert.
- Das Target ist dokumentiert und validiert.
- Die Gap-Matrix ist vollständig für den vereinbarten Scope.
- Alle Gaps sind kategorisiert.
- Accidentally Eliminated wurde gezielt geprüft.
- Alle kritischen und hohen Gaps haben Impact-Bewertung.
- Alle kritischen und hohen Gaps haben Owner.
- Risiken sind formuliert und ins Risikoregister übergeben.
- Maßnahmen sind abgeleitet.
- Work-Package-Kandidaten sind benannt.
- Architecture Board hat Ergebnisse geprüft.
- Architecture Repository ist aktualisiert.
- Phase E und Phase F haben verwendbare Inputs erhalten.


## Coach-Karten für tägliche Anwendung

Die folgenden Coach-Karten sind kurze Lerneinheiten.

Du kannst sie für Training, Review, Workshop-Vorbereitung oder Selbststudium nutzen.

### Coach-Karte 001: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 002: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 003: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 004: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 005: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 006: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 007: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 008: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 009: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 010: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 011: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 012: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 013: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 014: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 015: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 016: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 017: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 018: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 019: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 020: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 021: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 022: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 023: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 024: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 025: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 026: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 027: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 028: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 029: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 030: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 031: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 032: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 033: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 034: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 035: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 036: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 037: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 038: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 039: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 040: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 041: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 042: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 043: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 044: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 045: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 046: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 047: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 048: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 049: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 050: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 051: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 052: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 053: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 054: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 055: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 056: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 057: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 058: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 059: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 060: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 061: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 062: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 063: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 064: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 065: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 066: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 067: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 068: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 069: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 070: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 071: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 072: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 073: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 074: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 075: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 076: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 077: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 078: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 079: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 080: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 081: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 082: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 083: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 084: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 085: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 086: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 087: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 088: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 089: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 090: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 091: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 092: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 093: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 094: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 095: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 096: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 097: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 098: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 099: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 100: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 101: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 102: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 103: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 104: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 105: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 106: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 107: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 108: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 109: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 110: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 111: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 112: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 113: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 114: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 115: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 116: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 117: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 118: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 119: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 120: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 121: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 122: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 123: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 124: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 125: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 126: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 127: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 128: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 129: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 130: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 131: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 132: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 133: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 134: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 135: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 136: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 137: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 138: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 139: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 140: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 141: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 142: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 143: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 144: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 145: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 146: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 147: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 148: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 149: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 150: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 151: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 152: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 153: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 154: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 155: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 156: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 157: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 158: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 159: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 160: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 161: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 162: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 163: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 164: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 165: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 166: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 167: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 168: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 169: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 170: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 171: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 172: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 173: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 174: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 175: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 176: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 177: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 178: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 179: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 180: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 181: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 182: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 183: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 184: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 185: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 186: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 187: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 188: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 189: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 190: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 191: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 192: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 193: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 194: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 195: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 196: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 197: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 198: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 199: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 200: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 201: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 202: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 203: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 204: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 205: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 206: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 207: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 208: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 209: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 210: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 211: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 212: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 213: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 214: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 215: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 216: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 217: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 218: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 219: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 220: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 221: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 222: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 223: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 224: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 225: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 226: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 227: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 228: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 229: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 230: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 231: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 232: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 233: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 234: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 235: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 236: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 237: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 238: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 239: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 240: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 241: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 242: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 243: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 244: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 245: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 246: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 247: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 248: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 249: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 250: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 251: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 252: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 253: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 254: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 255: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 256: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 257: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 258: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 259: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 260: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 261: Scope

Kernaussage: Ein klarer Scope verhindert Streit über Nebenthemen.

Leitfrage: Was betrachten wir bewusst nicht?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Scope
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 262: Baseline

Kernaussage: Die Baseline ist die Realität, nicht die Wunschbeschreibung.

Leitfrage: Wer kann bestätigen, dass diese Beschreibung stimmt?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Baseline
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 263: Target

Kernaussage: Das Target muss erreichbar, prüfbar und verständlich sein.

Leitfrage: Welche Annahme steckt im Zielbild?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Target
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 264: Gap

Kernaussage: Ein Gap ist eine Aussage über Unterschied, nicht über Schuld.

Leitfrage: Was genau ist anders zwischen heute und morgen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Gap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 265: Retained

Kernaussage: Behalten bedeutet nicht automatisch unverändert lassen.

Leitfrage: Muss das Element stabilisiert oder angepasst werden?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Retained
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 266: Added

Kernaussage: Ein Added Gap braucht Aufwand, Budget und Verantwortlichkeit.

Leitfrage: Wer baut oder beschafft dieses Element?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Added
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 267: Eliminated

Kernaussage: Abschalten braucht Planung.

Leitfrage: Welche Nutzer, Daten und Verträge hängen daran?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 268: Accidentally Eliminated

Kernaussage: Diese Kategorie schützt vor teuren Verlusten.

Leitfrage: Wurde dieses Element absichtlich oder versehentlich vergessen?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Accidentally Eliminated
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 269: Impact

Kernaussage: Impact erklärt, warum ein Gap wichtig ist.

Leitfrage: Was passiert, wenn wir nichts tun?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Impact
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 270: Urgency

Kernaussage: Dringlichkeit erklärt, wann gehandelt werden muss.

Leitfrage: Welcher Termin oder Meilenstein erzeugt Druck?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Urgency
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 271: Risk

Kernaussage: Ein Gap kann ein Risiko erzeugen.

Leitfrage: Welches unerwünschte Ereignis kann eintreten?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Risk
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 272: Owner

Kernaussage: Ohne Owner bleibt ein Gap liegen.

Leitfrage: Wer entscheidet und wer setzt um?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Owner
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 273: Matrix

Kernaussage: Die Matrix macht Vergleiche sichtbar.

Leitfrage: Ist die Tabelle für Dritte verständlich?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Matrix
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 274: Priorisierung

Kernaussage: Nicht jeder Gap ist gleich wichtig.

Leitfrage: Was blockiert die Zielarchitektur?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Priorisierung
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 275: Roadmap

Kernaussage: Gaps liefern Material für die Roadmap.

Leitfrage: Welche Reihenfolge ist logisch?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Roadmap
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 276: Governance

Kernaussage: Governance macht Ergebnisse verbindlich.

Leitfrage: Wer genehmigt diese Einordnung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Governance
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 277: Repository

Kernaussage: Ein Ergebnis ohne Ablage verschwindet.

Leitfrage: Wo wird die Entscheidung versioniert?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Repository
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 278: Stakeholder

Kernaussage: Betroffene erkennen Gaps oft früher als Architekten.

Leitfrage: Wer lebt täglich mit diesem Prozess?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Stakeholder
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 279: Abhängigkeit

Kernaussage: Ein Gap hängt selten allein.

Leitfrage: Welcher andere Gap ist davon abhängig?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Abhängigkeit
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.

### Coach-Karte 280: Nachweis

Kernaussage: Geschlossen ist ein Gap erst mit Beleg.

Leitfrage: Woran erkennen wir die Schließung?

So wendest du es an:

- Stelle die Leitfrage im Workshop laut und sichtbar.
- Schreibe die Antwort als kurzen, prüfbaren Satz auf.
- Markiere Annahmen, wenn keine sichere Information vorliegt.
- Verknüpfe die Antwort mit einem Owner oder einer offenen Entscheidung.
- Prüfe später, ob daraus ein Gap, ein Risiko oder ein Arbeitspaket entsteht.

Mini-Beispiel:

- Thema: Nachweis
- Beobachtung: Die Antwort auf die Leitfrage ist noch nicht sauber belegt.
- Nächste Aktion: Klärung mit Fachbereich oder Architekturrolle einplanen.


## Anhang A: Vollständige Prüfliste für Baseline

1. Geschäftsziele erfassen und Quelle dokumentieren.
2. Geschäftsprozesse erfassen und Quelle dokumentieren.
3. Business Capabilities erfassen und Quelle dokumentieren.
4. Organisationseinheiten erfassen und Quelle dokumentieren.
5. Rollen erfassen und Quelle dokumentieren.
6. Verantwortlichkeiten erfassen und Quelle dokumentieren.
7. Produkte erfassen und Quelle dokumentieren.
8. Kundengruppen erfassen und Quelle dokumentieren.
9. Partner erfassen und Quelle dokumentieren.
10. Lieferanten erfassen und Quelle dokumentieren.
11. Standorte erfassen und Quelle dokumentieren.
12. Regulatorische Anforderungen erfassen und Quelle dokumentieren.
13. Anwendungen erfassen und Quelle dokumentieren.
14. Schnittstellen erfassen und Quelle dokumentieren.
15. Datenobjekte erfassen und Quelle dokumentieren.
16. Datenflüsse erfassen und Quelle dokumentieren.
17. Reports erfassen und Quelle dokumentieren.
18. Datenqualität erfassen und Quelle dokumentieren.
19. Technologien erfassen und Quelle dokumentieren.
20. Plattformen erfassen und Quelle dokumentieren.
21. Infrastruktur erfassen und Quelle dokumentieren.
22. Netzwerke erfassen und Quelle dokumentieren.
23. Cloud-Services erfassen und Quelle dokumentieren.
24. Security Controls erfassen und Quelle dokumentieren.
25. Betriebsprozesse erfassen und Quelle dokumentieren.
26. Monitoring erfassen und Quelle dokumentieren.
27. Backup erfassen und Quelle dokumentieren.
28. Deployment erfassen und Quelle dokumentieren.
29. Supportmodell erfassen und Quelle dokumentieren.
30. Verträge erfassen und Quelle dokumentieren.


## Anhang B: Vollständige Prüfliste für Target

1. Zielbild geprüft.
2. Architecture Vision berücksichtigt.
3. Stakeholder-Anforderungen berücksichtigt.
4. Architecture Principles geprüft.
5. Business-Ziele abgebildet.
6. Capability-Zielbild beschrieben.
7. Prozess-Zielbild beschrieben.
8. Application-Zielbild beschrieben.
9. Daten-Zielbild beschrieben.
10. Technologie-Zielbild beschrieben.
11. Security-Zielbild beschrieben.
12. Operating Model beschrieben.
13. Governance-Anforderungen beschrieben.
14. Migrationsannahmen dokumentiert.
15. Nicht-Ziele dokumentiert.
16. Abhängigkeiten dokumentiert.
17. Entscheidungen versioniert.
18. Offene Punkte markiert.
19. Risiken sichtbar.
20. Review abgeschlossen.


## Anhang C: Fragenkatalog für Interviews

1. Welche heutige Lösung darf auf keinen Fall verloren gehen?
2. Welche heutige Lösung bereitet die meisten Probleme?
3. Welche Zielanforderung ist noch unklar?
4. Welche Schnittstelle ist kritisch?
5. Welche Daten sind besonders wichtig?
6. Welche manuelle Arbeit soll entfallen?
7. Welche regulatorische Anforderung muss eingehalten werden?
8. Welche Abhängigkeit wird oft übersehen?
9. Welche Anwendung hat keinen klaren Owner?
10. Welche Funktion wird doppelt angeboten?
11. Welche technische Plattform ist veraltet?
12. Welche Betriebsfähigkeit fehlt heute?
13. Welche Änderung kann nicht ohne Fachbereich entschieden werden?
14. Welche Abschaltung wäre riskant?
15. Welche Zielkomponente hat noch keine Finanzierung?
16. Welche Entscheidung blockiert die Roadmap?


## Anhang D: Beispielhafte Jira-Übersetzung

| Gap-ID | Jira-Typ | Titel | Akzeptanzkriterien |
| --- | --- | --- | --- |
| G-003 | Epic | Compliance Export in Zielarchitektur aufnehmen | Export ist im Target dokumentiert; Owner bestätigt Bedarf; Roadmap-Einordnung vorhanden. |
| G-004 | Epic | API Gateway einführen | Gateway-Option bewertet; Zielarchitektur aktualisiert; Betriebsmodell beschrieben. |
| G-005 | Story | Application Monitoring für neues Portal definieren | Metriken, Logs und Alerts sind beschrieben und reviewt. |
| G-006 | Epic | Identity-Migration vorbereiten | Rollenmodell, Mapping und Migrationsplan liegen vor. |


## Schlussbild

Eine gute Gap Analysis ist kein Pflichtformular.

Sie ist ein Führungsinstrument für Veränderung.

Sie zeigt, was bleibt.

Sie zeigt, was neu entstehen muss.

Sie zeigt, was wegfällt.

Sie zeigt, was versehentlich vergessen wurde.

Sie schützt vor blinden Flecken.

Sie macht Risiken sichtbar.

Sie bereitet Roadmaps vor.

Sie verbindet Architektur mit Umsetzung.

Wenn du nur ein Bild der Zielarchitektur hast, hast du noch keinen Plan.

Wenn du die Gaps kennst, bewertest und in Arbeit übersetzt, beginnt echte Steuerung.
