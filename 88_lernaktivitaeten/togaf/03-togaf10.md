# TOGAF-Coach-Playbook: Vulnerability Management, Security Patching und Resilienz

---

## Dokumentstatus

| Aspekt | Details/Erklärung | Beispiel | Literatur/Quelle |
|---|---|---|---|
| Zweck | Dieses Playbook erklärt Vulnerability Management aus Sicht eines TOGAF-orientierten Enterprise Architects. | Schwachstellen werden nicht nur technisch behoben, sondern als steuerbare Unternehmensfähigkeit behandelt. | TOGAF Standard, NIST SP 800-40 Rev. 4, NIST SP 800-61 Rev. 2, CISA KEV |
| Grundlage | Die operative Basis ist das bereitgestellte Vulnerability-Management-Dokument mit Prozessen zu Schwachstellen, Patching, Exceptions, Incidents, Changes und BC/DR. | CVSS-Fristen, Eskalationen, Patch-Prozess, Exception-Lifecycle. | Benutzerquelle: Vulnerability Management.txt |
| Zielgruppe | Enterprise Architects, Security Architects, Chapter Leads, Engineering Manager, Asset Owner, Product Owner, Betrieb, DevOps, Compliance. | Alle Rollen, die Schwachstellen nicht nur finden, sondern nachhaltig managen müssen. | TOGAF Architecture Governance |
| Sprachstil | Einfache Sprache, klare Handlungsanweisung, Coach-Haltung. | Nicht: „Implementiere ein risikobasiertes VM-Framework“, sondern: „Baue zuerst eine Liste deiner Assets.“ | Coaching-Ansatz |
| Ergebnis | Eine konkrete Anleitung, mit der du einen Vulnerability-Management-Prozess entwerfen, einführen, prüfen und verbessern kannst. | 30-60-90-Tage-Plan, Checklisten, Rollenmodell, Templates. | Enterprise Architecture Practice |

---

## Schnelle Start-Checkliste

1. Verstehe zuerst, welche Systeme, Anwendungen, Datenbanken, Cloud-Ressourcen und Endgeräte überhaupt existieren.
2. Ordne jedem Asset einen Owner zu, sonst wird jede Schwachstelle später herumgereicht.
3. Unterscheide klar zwischen Finding, Issue, Exception, Violation und Incident.
4. Bewerte Schwachstellen nicht nur nach CVSS, sondern auch nach Exposition, Asset-Kritikalität und Ausnutzbarkeit.
5. Behandle internet-exponierte Systeme strenger als interne Systeme.
6. Definiere Remediation-Fristen, bevor der erste kritische Fall auftaucht.
7. Lege fest, wer entscheidet, wenn ein Patch nicht sofort eingespielt werden kann.
8. Baue einen Exception-Prozess, der Ausnahmen sichtbar, zeitlich begrenzt und überprüfbar macht.
9. Verbinde Vulnerability Management mit Change Management, damit Patches nicht am Prozess scheitern.
10. Verbinde Vulnerability Management mit Incident Management, damit aktiv ausgenutzte Schwachstellen sofort eskalieren.
11. Miss nicht nur Anzahl der Schwachstellen, sondern Alter, Fristtreue, Wiederholungen und betroffene kritische Assets.
12. Nutze TOGAF, um den Prozess nicht als Tool-Projekt, sondern als Unternehmensfähigkeit zu bauen.
13. Dokumentiere die Zielarchitektur, den Ist-Zustand, die Lücke, die Roadmap und die Governance.
14. Trainiere Teams in einfacher Sprache, weil Sicherheit nur funktioniert, wenn Menschen wissen, was sie konkret tun müssen.
15. Prüfe regelmäßig, ob dein Prozess im Alltag wirklich funktioniert oder nur auf dem Papier sauber aussieht.

---

## Einordnung als Coach

Dieses Dokument ist bewusst groß. Es soll nicht nur eine kurze Beschreibung liefern, sondern eine vollständige Arbeitsanleitung. Du kannst es als Schulungsunterlage, Architekturleitfaden, Governance-Vorlage, Review-Basis oder Startpunkt für ein internes Security-Operating-Model verwenden.

Die TOGAF-Brille hilft dir dabei, operative Sicherheitsarbeit in eine stabile Unternehmensfähigkeit zu übersetzen. Das bedeutet: Wir betrachten nicht nur Scanner, Patches und Tickets. Wir betrachten Rollen, Prozesse, Daten, Anwendungen, Infrastruktur, Entscheidungen, Kennzahlen und Governance.

Der wichtigste Gedanke lautet: Vulnerability Management ist kein reines Security-Tool. Es ist eine dauerhafte Fähigkeit der Organisation, bekannte Schwächen rechtzeitig zu erkennen, zu bewerten, zu beheben und aus ihnen zu lernen.

Ein Enterprise Architect fragt deshalb nicht zuerst: „Welches Tool kaufen wir?“ Er fragt: „Welche Fähigkeit brauchen wir, welche Menschen tragen sie, welche Informationen benötigt sie, welche Systeme unterstützen sie und wie messen wir ihren Erfolg?“

Dieses Playbook folgt dieser Logik. Es beginnt einfach, geht dann Schritt für Schritt tiefer und führt dich am Ende zu einem umsetzbaren Operating Model.

---

## Grundbegriffe in einfacher Sprache

| Begriff | Einfache Erklärung | Beispiel |
|---|---|---|
| Vulnerability | Eine Schwachstelle. Das ist eine technische oder organisatorische Schwäche, die ausgenutzt werden kann. | Eine veraltete Log4j-Version in einer produktiven Anwendung. |
| Threat | Eine Bedrohung. Das ist ein mögliches Ereignis oder ein möglicher Akteur, der eine Schwachstelle ausnutzt. | Ein Angreifer scannt öffentlich erreichbare Server. |
| Exploit | Eine konkrete Methode, mit der eine Schwachstelle ausgenutzt wird. | Ein öffentlich verfügbarer Angriffscode für eine CVE. |
| CVE | Eine weltweit verwendete Kennung für bekannte Schwachstellen. | CVE-2021-44228 für Log4Shell. |
| CVSS | Ein Bewertungssystem, das die technische Schwere einer Schwachstelle auf einer Skala von 0.0 bis 10.0 beschreibt. | CVSS 9.8 bedeutet technisch sehr kritisch. |
| Finding | Eine gefundene Schwachstelle, die noch bewertet werden muss. | Der Scanner findet eine alte OpenSSL-Version. |
| Issue | Eine bestätigte Schwachstelle mit Handlungsbedarf. | Die alte OpenSSL-Version ist produktiv erreichbar und muss behoben werden. |
| Zero-Day | Eine Schwachstelle, für die es noch keinen offiziellen Patch gibt. | Ein Hersteller bestätigt die Lücke, liefert aber noch kein Update. |
| Patch | Eine Softwarekorrektur, mit der eine Schwachstelle behoben wird. | Ein Sicherheitsupdate des Betriebssystems. |
| Hotfix | Ein dringender Patch außerhalb des normalen Wartungsfensters. | Sofortiges Update wegen aktiver Ausnutzung. |
| Workaround | Eine vorübergehende Maßnahme, die das Risiko senkt, ohne die Ursache zu beheben. | Deaktivieren einer anfälligen Funktion. |
| Compensating Control | Eine Ersatzmaßnahme, wenn die eigentliche Anforderung noch nicht erfüllt werden kann. | Web Application Firewall vor einer nicht sofort patchbaren Anwendung. |
| Exception | Eine dokumentierte Ausnahme von einer Regel. | Ein System kann 60 Tage lang nicht gepatcht werden, weil ein Hersteller-Test fehlt. |
| Violation | Eine nicht genehmigte Abweichung. | Ein kritischer Server bleibt ungepatcht, ohne dokumentierte Ausnahme. |
| Residual Risk | Das Risiko, das nach Ersatzmaßnahmen übrig bleibt. | Trotz Segmentierung bleibt ein Restrisiko durch alte Software. |
| Asset Owner | Die fachlich verantwortliche Person für ein Asset. | Der Product Owner oder Systemverantwortliche einer Anwendung. |
| Asset Manager | Die Person, die operative Pflege, Koordination und Nachverfolgung für ein Asset übernimmt. | Technischer Systembetreuer. |
| CCSO | Eine zentrale Sicherheitsrolle oder Security-Leitung, die Vorgaben, Kontrolle und Eskalation verantwortet. | Chief Cyber Security Officer oder vergleichbare Rolle. |
| CAB | Change Advisory Board. Ein Gremium, das Änderungen bewertet und freigibt. | Patch für produktives Kernsystem wird im CAB geprüft. |
| RTO | Recovery Time Objective. Maximale tolerierbare Ausfallzeit. | Das System muss innerhalb von 4 Stunden wieder laufen. |
| RPO | Recovery Point Objective. Maximal tolerierbarer Datenverlust. | Es dürfen höchstens 15 Minuten Daten verloren gehen. |
| MTTR | Mean Time to Recover. Durchschnittliche Wiederherstellungszeit. | Im Schnitt werden Services nach 90 Minuten wiederhergestellt. |

---

## TOGAF-Denke: Was ein Enterprise Architect hier wirklich tut

| TOGAF-Baustein | Einfache Bedeutung | Coach-Hinweis |
|---|---|---|
| Capability | Du beschreibst Vulnerability Management als Fähigkeit der Organisation. Eine Fähigkeit besteht aus Menschen, Prozessen, Informationen und Technologie. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Value Stream | Du zeigst, wie ein Wert entsteht: Schwachstelle finden, bewerten, beheben, verifizieren und lernen. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Business Architecture | Du klärst Rollen, Verantwortlichkeiten, Entscheidungswege, Eskalationen und Governance. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Data Architecture | Du definierst, welche Daten benötigt werden: Asset-Daten, CVEs, Findings, Exceptions, Fristen, Evidenzen, Reports. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Application Architecture | Du definierst, welche Anwendungen den Prozess unterstützen: Scanner, Ticket-System, CMDB, SIEM, Patch-Tools, Reporting. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Technology Architecture | Du klärst Plattformen, Netzwerke, Cloud, Endpunkte, Container, Datenbanken, OT/ICS und EoL-Systeme. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Architecture Requirements | Du formulierst Anforderungen, die später prüfbar sind. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Gap Analysis | Du vergleichst Ist-Zustand und Zielzustand. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Roadmap | Du planst die Einführung in Wellen. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |
| Governance | Du sorgst dafür, dass Entscheidungen, Ausnahmen und Risiken sichtbar bleiben. | Frage immer: Wer tut was, auf welcher Datenbasis, mit welchem Entscheidungspunkt und welchem Nachweis? |

Als Coach sage ich dir: Wenn du diesen Prozess nur als Security-Prozess beschreibst, bleibt er oft in der Security-Abteilung hängen. Wenn du ihn als Enterprise Capability beschreibst, bekommt er Anschluss an Architektur, Betrieb, Produktentwicklung, Risiko, Budget und Management.

---

## TOGAF ADM auf Vulnerability Management angewendet

| ADM-Phase | Bedeutung | Anwendung im Thema |
|---|---|---|
| Preliminary Phase | Arbeitsweise, Prinzipien, Governance und Rollen festlegen. | Definiere Sicherheitsprinzipien, Asset-Verantwortung, Kritikalitätsmodell und Entscheidungsrechte. |
| Phase A: Architecture Vision | Zielbild formulieren. | Beschreibe, warum Vulnerability Management für Geschäft, Betrieb, Kundenvertrauen und Resilienz wichtig ist. |
| Phase B: Business Architecture | Rollen, Prozesse und Organisation gestalten. | Lege Asset Owner, Asset Manager, CCSO, CAB, Engineering, Betrieb und Management-Verantwortung fest. |
| Phase C: Information Systems Architectures | Daten- und Anwendungsarchitektur definieren. | Beschreibe CMDB, Scanner, Ticket-System, SCA, SIEM, Reporting und Datenflüsse. |
| Phase D: Technology Architecture | Technologie-Landschaft absichern. | Betrachte Netzwerk, Cloud, Container, Endpoints, Betriebssysteme, Datenbanken und OT/ICS. |
| Phase E: Opportunities and Solutions | Lösungsoptionen und Umsetzungsblöcke bilden. | Entscheide, was zuerst kommt: Asset-Inventar, Scanning, Priorisierung, Patch-Automatisierung, Reporting. |
| Phase F: Migration Planning | Roadmap planen. | Baue eine 30-60-90-Tage-Planung und danach Quartalswellen. |
| Phase G: Implementation Governance | Umsetzung steuern. | Prüfe, ob Teams wirklich nach Fristen, Templates, Eskalationen und Nachweisen arbeiten. |
| Phase H: Architecture Change Management | Änderungen dauerhaft kontrollieren. | Passe Prozess, Tools, Fristen und Kontrollpunkte an neue Risiken an. |
| Requirements Management | Anforderungen laufend verwalten. | Sammle Anforderungen aus Audit, Risiko, Produkt, Betrieb und Security und halte sie aktuell. |

Merke dir: ADM ist kein Papierprozess. ADM ist eine Denkstruktur. Sie zwingt dich, nicht direkt in Tools zu springen, sondern erst das System zu verstehen.

---

## Kapitel 1 — Zielbild: Was gutes Vulnerability Management leisten muss

Gutes Vulnerability Management sorgt dafür, dass bekannte Schwachstellen nicht zufällig, sondern systematisch behandelt werden.

Das Ziel ist nicht, jede Schwachstelle sofort perfekt zu beseitigen. Das Ziel ist, Risiken kontrolliert, nachvollziehbar und priorisiert zu reduzieren.

Ein gutes Zielbild sagt klar, was erkannt, bewertet, behoben, dokumentiert, eskaliert und berichtet wird.

### Coach-Kernaussage

Starte nicht mit Scanner-Auswahl. Starte mit der Frage, welches Risiko du kontrollieren willst.

### Leitprinzipien

1. Kein Asset ohne Owner.
2. Kein Finding ohne Bewertung.
3. Kein Issue ohne Frist.
4. Keine Fristüberschreitung ohne Eskalation.
5. Keine Ausnahme ohne Begründung.
6. Keine Begründung ohne Restrisiko.
7. Kein Restrisiko ohne verantwortliche Annahme.
8. Keine Behebung ohne Verifikation.
9. Kein Reporting ohne Trendanalyse.
10. Keine Verbesserung ohne Lessons Learned.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 2 — Asset-Basis: Ohne Inventar ist alles Zufall

Vulnerability Management steht und fällt mit dem Asset-Inventar.

Wenn du nicht weißt, welche Systeme existieren, kannst du nicht wissen, welche Schwachstellen relevant sind.

Ein Scanner findet technische Hinweise. Er ersetzt aber keine Verantwortung.

### Coach-Kernaussage

Baue zuerst eine einfache Liste. Perfektion entsteht später durch Pflege, nicht durch monatelanges Modellieren.

### Leitprinzipien

1. Jedes Asset hat eine eindeutige ID.
2. Jedes Asset hat einen fachlichen Owner.
3. Jedes Asset hat einen technischen Manager.
4. Jedes Asset hat eine Kritikalität.
5. Jedes Asset hat einen Standort oder eine logische Umgebung.
6. Jedes Asset hat eine Exposition.
7. Jedes Asset hat einen Lebenszyklusstatus.
8. Jedes Asset hat bekannte Abhängigkeiten.
9. Jedes Asset hat eine Patch-Strategie.
10. Jedes Asset hat eine Wiederherstellungsanforderung.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 3 — Identifikation: Wie Schwachstellen sichtbar werden

Identifikation bedeutet: Die Organisation erkennt mögliche Schwachstellen aus verschiedenen Quellen.

Automatisierte Scanner sind wichtig, aber nicht ausreichend.

Herstellermeldungen, Threat Intelligence, CERT-Meldungen, Penetrationstests und Code-Analysen liefern zusätzliche Hinweise.

### Coach-Kernaussage

Ein Scanner produziert Daten. Erst der Prozess macht daraus steuerbare Arbeit.

### Leitprinzipien

1. Nutze Netzwerk-Scanner für Infrastruktur.
2. Nutze SCA-Tools für Abhängigkeiten im Code.
3. Nutze Container-Scanning für Images.
4. Nutze Cloud-Security-Tools für Cloud-Konfigurationen.
5. Nutze Endpoint-Tools für Clients und Server.
6. Nutze Web-Application-Scanner für Webanwendungen.
7. Nutze Penetrationstests für tiefere Prüfung.
8. Nutze Hersteller-Advisories für produktnahe Hinweise.
9. Nutze CERT- und CISA-Meldungen für aktuelle Ausnutzung.
10. Nutze interne Meldewege für Beobachtungen aus Teams.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 4 — Bewertung: Warum CVSS allein nicht reicht

CVSS beschreibt die technische Schwere einer Schwachstelle.

Für Unternehmensentscheidungen brauchst du zusätzlich Kontext.

Ein CVSS-Score von 7.5 auf einem isolierten Testsystem ist anders zu behandeln als derselbe Score auf einem öffentlich erreichbaren Kundensystem.

### Coach-Kernaussage

Priorisierung ist kein Bauchgefühl. Priorisierung ist eine begründete Entscheidung mit nachvollziehbaren Kriterien.

### Leitprinzipien

1. Bewerte CVSS als technische Grundlage.
2. Bewerte Exposition separat.
3. Bewerte Asset-Kritikalität separat.
4. Prüfe, ob ein Exploit öffentlich verfügbar ist.
5. Prüfe, ob die Schwachstelle aktiv ausgenutzt wird.
6. Prüfe, ob sensible Daten betroffen sein können.
7. Prüfe, ob Authentifizierung erforderlich ist.
8. Prüfe, ob Benutzerinteraktion erforderlich ist.
9. Prüfe vorhandene Schutzmaßnahmen.
10. Dokumentiere die Entscheidung nachvollziehbar.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 5 — Priorisierung: Aus Findings werden handhabbare Issues

Ein Finding ist ein Hinweis. Ein Issue ist eine bestätigte Aufgabe.

Nicht jedes Finding ist gleich dringend.

Priorisierung schützt Teams davor, in einer unendlichen Liste technischer Meldungen zu ertrinken.

### Coach-Kernaussage

Ein gutes Issue erklärt nicht nur, was kaputt ist. Es erklärt, warum es wichtig ist und bis wann es erledigt sein muss.

### Leitprinzipien

1. Bestätige die technische Relevanz.
2. Entferne False Positives mit Nachweis.
3. Ordne Findings betroffenen Assets zu.
4. Gruppiere gleiche Ursachen.
5. Erstelle Issues mit klarer Beschreibung.
6. Vergib eine verantwortliche Rolle.
7. Setze eine Remediation-Frist.
8. Verknüpfe das Issue mit Risiko und Asset.
9. Lege Evidenzanforderungen fest.
10. Kommuniziere die Priorität verständlich.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 6 — Remediation: Beheben, entschärfen oder begründet ausnehmen

Remediation bedeutet, eine Schwachstelle wirksam zu behandeln.

Die beste Lösung ist meist ein Patch oder ein Update.

Manchmal sind Workarounds, Konfigurationen oder kompensierende Maßnahmen nötig.

### Coach-Kernaussage

Remediation ist nicht erledigt, wenn jemand sagt: „Wir haben gepatcht.“ Sie ist erledigt, wenn der Nachweis zeigt, dass die Schwachstelle nicht mehr wirksam ist.

### Leitprinzipien

1. Patch einspielen, wenn möglich.
2. Konfiguration härten, wenn die Schwachstelle durch Fehlkonfiguration entsteht.
3. Angreifbare Funktion deaktivieren, wenn sie nicht benötigt wird.
4. Zugriffe einschränken, wenn sofortiges Patchen nicht möglich ist.
5. Netzwerksegmentierung nutzen, wenn Systeme nicht schnell geändert werden können.
6. Monitoring erhöhen, wenn Restrisiko bleibt.
7. Rollback vorbereiten, wenn produktive Systeme betroffen sind.
8. Tests durchführen, bevor kritische Systeme geändert werden.
9. Exception beantragen, wenn Behebung fristgerecht nicht möglich ist.
10. Verifikation durchführen, bevor ein Issue geschlossen wird.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 7 — Security Patching: Patching als Wartungspflicht

Patching ist keine Sonderaktion. Patching ist vorbeugende Wartung.

Moderne Unternehmen müssen Patching planbar, automatisierbar und risikobasiert organisieren.

Je kritischer und exponierter ein System ist, desto kürzer muss die Reaktionszeit sein.

### Coach-Kernaussage

Patching darf nicht davon abhängen, ob gerade jemand Zeit hat. Patching braucht einen Rhythmus.

### Leitprinzipien

1. Überwache Hersteller-Advisories.
2. Überwache CVE-Datenbanken.
3. Überwache CISA KEV und vergleichbare Quellen.
4. Pflege Patch-Zyklen je Systemklasse.
5. Plane Wartungsfenster.
6. Teste Patches vor Produktion.
7. Automatisiere Standardfälle.
8. Dokumentiere Ausnahmen.
9. Erstelle Rollback-Pläne.
10. Berichte Patch-Compliance regelmäßig.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 8 — Exception Management: Ausnahmen sichtbar kontrollieren

Eine Exception ist keine Ausrede.

Eine Exception ist eine dokumentierte, bewertete und zeitlich begrenzte Abweichung.

Sie ist besser als eine versteckte Violation, weil sie sichtbar und überprüfbar bleibt.

### Coach-Kernaussage

Eine gute Ausnahme hat ein Ablaufdatum. Eine schlechte Ausnahme wird zur neuen Normalität.

### Leitprinzipien

1. Dokumentiere jede Ausnahme sofort.
2. Gib Asset, Owner und Grund an.
3. Beschreibe die betroffene Regel.
4. Bewerte Risiko vor Ersatzmaßnahmen.
5. Definiere kompensierende Maßnahmen.
6. Bewerte Restrisiko nach Ersatzmaßnahmen.
7. Lege ein Enddatum fest.
8. Plane die spätere Behebung.
9. Lasse die Ausnahme genehmigen.
10. Überprüfe Exceptions regelmäßig.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 9 — Incident Management: Wenn aus Schwachstelle ein Vorfall wird

Nicht jede Schwachstelle ist ein Incident.

Wenn eine Schwachstelle aktiv ausgenutzt wird oder Schaden entstanden ist, brauchst du Incident Response.

Der Übergang muss klar definiert sein.

### Coach-Kernaussage

In einem Incident zählt Klarheit mehr als Perfektion. Jeder muss wissen, wer entscheidet und was als Nächstes passiert.

### Leitprinzipien

1. Definiere Trigger für Incident-Eskalation.
2. Aktiviere ein Incident-Response-Team.
3. Sichere Beweise.
4. Isoliere betroffene Systeme.
5. Analysiere Ursache und Ausbreitung.
6. Entferne Angriffsartefakte.
7. Schließe die ausgenutzte Schwachstelle.
8. Stelle Systeme kontrolliert wieder her.
9. Kommuniziere abgestimmt.
10. Führe Lessons Learned durch.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 10 — Change Management: Sicherheit ohne kontrollierte Änderung funktioniert nicht

Viele Patches scheitern nicht technisch, sondern organisatorisch.

Change Management sorgt dafür, dass Änderungen geplant, bewertet, genehmigt, durchgeführt und überprüft werden.

Für kritische Schwachstellen muss es einen schnellen Notfallweg geben.

### Coach-Kernaussage

Ein guter Change-Prozess schützt Produktion und beschleunigt sichere Arbeit. Ein schlechter Change-Prozess blockiert beides.

### Leitprinzipien

1. Unterscheide Standard Change, Normal Change, Emergency Change und Major Change.
2. Definiere Vorabgenehmigungen für risikoarme Standardpatches.
3. Nutze CAB für relevante Produktionsänderungen.
4. Erlaube Emergency Changes bei kritischen Sicherheitsfällen.
5. Dokumentiere Notfalländerungen nachträglich.
6. Prüfe Rollback-Pläne.
7. Prüfe Testnachweise.
8. Prüfe Kommunikationsbedarf.
9. Verknüpfe Change mit Finding oder Issue.
10. Schließe Change erst nach Verifikation.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 11 — Business Continuity und Disaster Recovery: Resilienz einbauen

Vulnerability Management reduziert Eintrittswahrscheinlichkeit.

Business Continuity und Disaster Recovery reduzieren Auswirkung.

Beides gehört zusammen.

### Coach-Kernaussage

Ein Backup ist erst dann ein Backup, wenn du erfolgreich wiederhergestellt hast.

### Leitprinzipien

1. Definiere RTO je System.
2. Definiere RPO je System.
3. Ordne Backup-Strategien nach Kritikalität.
4. Nutze 3-2-1-1-0 als Grundregel.
5. Teste Wiederherstellung wirklich.
6. Dokumentiere Wiederherstellungszeiten.
7. Verbinde BC/DR mit Incident Response.
8. Prüfe Abhängigkeiten zwischen Systemen.
9. Berücksichtige Cloud, Datenbanken und Identitätsdienste.
10. Führe Übungen durch.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 12 — Reporting: Vom Zahlenfriedhof zur Steuerung

Reporting ist nicht dafür da, bunte Folien zu erzeugen.

Reporting soll zeigen, ob Risiko sinkt, Arbeit wirkt und Entscheidungen nötig sind.

Ein gutes Reporting unterscheidet operative, taktische und strategische Sicht.

### Coach-Kernaussage

Ein guter Report beantwortet nicht nur: Wie viele Schwachstellen haben wir? Er beantwortet: Wo müssen wir handeln?

### Leitprinzipien

1. Zeige offene Findings nach Kritikalität.
2. Zeige überfällige Issues.
3. Zeige Alter der Schwachstellen.
4. Zeige kritische Assets mit offenen Schwachstellen.
5. Zeige Patch-Compliance.
6. Zeige Exceptions und deren Ablaufdaten.
7. Zeige wiederkehrende Ursachen.
8. Zeige Trend pro Monat.
9. Zeige Top-Risiken für Management.
10. Zeige Entscheidungen, die benötigt werden.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 13 — Governance: Entscheidungen sauber machen

Governance bedeutet nicht Bürokratie.

Governance bedeutet klare Entscheidungsrechte, klare Kontrollpunkte und klare Nachweise.

Ohne Governance wird Vulnerability Management beliebig.

### Coach-Kernaussage

Gute Governance macht Arbeit leichter, weil niemand im Ernstfall raten muss.

### Leitprinzipien

1. Lege Verantwortlichkeiten schriftlich fest.
2. Definiere Entscheidungsrechte.
3. Definiere Eskalationswege.
4. Definiere Pflichtartefakte.
5. Definiere Review-Punkte.
6. Definiere Ausnahmegenehmigungen.
7. Definiere Reporting-Zyklen.
8. Definiere Auditfähigkeit.
9. Definiere Verbesserungsmechanismen.
10. Definiere Konsequenzen bei dauerhaftem Nicht-Handeln.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 14 — Architekturartefakte: Was du dokumentieren solltest

TOGAF-orientierte Arbeit braucht Artefakte.

Artefakte sind keine Dekoration. Sie machen Entscheidungen nachvollziehbar.

Für Vulnerability Management brauchst du wenige, aber klare Dokumente.

### Coach-Kernaussage

Dokumentiere so viel wie nötig und so wenig wie möglich. Aber alles, was Entscheidungen trägt, muss sichtbar sein.

### Leitprinzipien

1. Capability Map erstellen.
2. Value Stream beschreiben.
3. Rollenmodell dokumentieren.
4. Datenmodell skizzieren.
5. Tool-Landschaft erfassen.
6. Schnittstellen beschreiben.
7. Risiko- und Kontrollmodell erstellen.
8. Ist-Zustand beschreiben.
9. Zielzustand beschreiben.
10. Roadmap dokumentieren.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Kapitel 15 — Maturity Model: Reifegrad ehrlich bewerten

Reifegradmodelle helfen, den aktuellen Stand nüchtern zu sehen.

Sie sind kein Selbstzweck.

Sie zeigen, welche nächste Verbesserung wirklich sinnvoll ist.

### Coach-Kernaussage

Springe nicht von Level 1 direkt zu Level 5. Baue zuerst Verlässlichkeit auf.

### Leitprinzipien

1. Level 1: ad hoc und reaktiv.
2. Level 2: teilweise dokumentiert.
3. Level 3: standardisiert und wiederholbar.
4. Level 4: gemessen und gesteuert.
5. Level 5: kontinuierlich verbessert und automatisiert.
6. Bewerte Asset-Inventar separat.
7. Bewerte Scanning separat.
8. Bewerte Priorisierung separat.
9. Bewerte Remediation separat.
10. Bewerte Reporting separat.
11. Bewerte Governance separat.

### Umsetzung in einfacher Sprache

1. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
1. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
1. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
2. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
2. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
2. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
3. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
3. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
3. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
4. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
4. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
4. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
5. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
5. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
5. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
6. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
6. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
6. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
7. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
7. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
7. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
8. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
8. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
8. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
9. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
9. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
9. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.
10. Frage dich: Was muss hier konkret sichtbar, verantwortlich, messbar oder entscheidbar gemacht werden?
10. Prüfe danach: Gibt es dafür ein Artefakt, einen Owner, eine Frist und einen Nachweis?
10. Schließe den Schritt erst ab, wenn die nächste Rolle ohne Rückfrage weiterarbeiten kann.

### Typische Fehler

- Es gibt viele technische Meldungen, aber keine klare Verantwortung.
- Die Kritikalität wird rein technisch bewertet und der Geschäftskontext fehlt.
- Fristen existieren, werden aber nicht nachverfolgt.
- Ausnahmen werden mündlich akzeptiert und verschwinden später.
- Patches werden durchgeführt, aber nicht verifiziert.
- Reports zeigen Zahlen, aber keine Entscheidungen.
- Security, Betrieb und Entwicklung arbeiten nebeneinander statt miteinander.
- Die Architektur dokumentiert Systeme, aber keine operativen Sicherheitsfähigkeiten.
- Das Management sieht nur Ampeln, aber keine Ursachen.
- Lessons Learned werden geschrieben, aber nicht in Prozessänderungen übersetzt.

### Gute Praxis

- Jede Schwachstelle ist einem Asset zugeordnet.
- Jedes Asset hat einen Owner.
- Jedes Issue hat eine Frist.
- Jede Ausnahme hat ein Ablaufdatum.
- Jede Fristüberschreitung hat eine Eskalation.
- Jeder Patch hat einen Nachweis.
- Jede kritische Entscheidung wird dokumentiert.
- Jeder Report enthält Trend und Handlungsbedarf.
- Jeder Incident erzeugt eine Verbesserung.
- Jede Architekturänderung berücksichtigt Security-Auswirkungen.

---

## Capability Map für Vulnerability Management

| Capability-Domäne | Fähigkeit | Coach-Frage |
|---|---|---|
| Strategische Steuerung | Security-Prinzipien definieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Strategische Steuerung | Risikotoleranz formulieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Strategische Steuerung | Governance-Modell festlegen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Strategische Steuerung | Management-Reporting etablieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Strategische Steuerung | Budget und Ressourcen planen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Strategische Steuerung | Architektur-Roadmap pflegen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Asset Management | Asset-Inventar pflegen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Asset Management | Owner zuordnen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Asset Management | Kritikalität bewerten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Asset Management | Exposition dokumentieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Asset Management | Abhängigkeiten erfassen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Asset Management | Lebenszyklusstatus prüfen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Detection und Scanning | Netzwerk-Scanning betreiben | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Detection und Scanning | Endpoint-Schwachstellen erkennen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Detection und Scanning | Application-Scanning durchführen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Detection und Scanning | Dependency-Scanning nutzen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Detection und Scanning | Container-Images prüfen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Detection und Scanning | Cloud-Konfigurationen bewerten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Bewertung und Priorisierung | CVSS auswerten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Bewertung und Priorisierung | Exploitability prüfen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Bewertung und Priorisierung | KEV-Relevanz prüfen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Bewertung und Priorisierung | Business Impact bewerten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Bewertung und Priorisierung | False Positives ausschließen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Bewertung und Priorisierung | Issues klassifizieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Remediation und Patching | Patches planen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Remediation und Patching | Patches testen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Remediation und Patching | Patches deployen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Remediation und Patching | Workarounds umsetzen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Remediation und Patching | Kompensationsmaßnahmen dokumentieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Remediation und Patching | Verifikation durchführen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Exception Management | Exception erfassen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Exception Management | Risiko bewerten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Exception Management | Restrisiko dokumentieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Exception Management | Genehmigung einholen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Exception Management | Monitoring durchführen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Exception Management | Exception schließen oder erneuern | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Incident Response | Incident-Trigger definieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Incident Response | Triage durchführen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Incident Response | Containment steuern | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Incident Response | Eradication koordinieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Incident Response | Recovery begleiten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Incident Response | Lessons Learned umsetzen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| BC/DR | RTO definieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| BC/DR | RPO definieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| BC/DR | Backups prüfen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| BC/DR | Restore testen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| BC/DR | DR-Übungen planen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| BC/DR | Resilienz verbessern | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Reporting und Verbesserung | KPIs messen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Reporting und Verbesserung | Trends analysieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Reporting und Verbesserung | Root Causes identifizieren | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Reporting und Verbesserung | Maßnahmen planen | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Reporting und Verbesserung | Architekturänderungen ableiten | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |
| Reporting und Verbesserung | Reifegrad verbessern | Ist diese Fähigkeit benannt, verantwortet, gemessen und mit einem Prozess verbunden? |

---

## Rollenmodell und Verantwortlichkeiten

### Rollenbeschreibung

| Rolle | Hauptverantwortung | Coach-Hinweis |
|---|---|---|
| Enterprise Architect | Zielarchitektur, Capability Map, Roadmap, Architektur-Governance | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Security Architect | Security-Kontrollen, Risikoannahmen, technische Schutzmaßnahmen | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| CCSO | Security Governance, Eskalation, Richtlinien, Management-Reporting | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Asset Owner | Fachliche Verantwortung, Risikoannahme, Priorisierung im Kontext | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Asset Manager | Operative Nachverfolgung, Pflege, Koordination | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Engineering Lead | Umsetzung in Teams, technische Qualität, Remediation-Planung | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| DevOps / Platform Team | Automatisierung, Deployment, Patch-Pipelines, Infrastruktur | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Operations | Betrieb, Wartungsfenster, Systemstabilität, Monitoring | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Product Owner | Priorisierung im Produkt-Backlog, Business-Abwägung | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Change Manager | Change-Prozess, CAB, Freigaben, Notfalländerungen | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Incident Commander | Führung im Sicherheitsvorfall | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Legal / Compliance | Meldepflichten, regulatorische Bewertung, Nachweise | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| Management | Ressourcen, Zielkonflikte, Risikoentscheidungen | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |
| QA | Testnachweise, Verifikation, Regression, Abnahme | Diese Rolle muss wissen, welche Entscheidung sie treffen darf und welchen Nachweis sie liefern muss. |

### RACI-Matrix

| Aktivität | Responsible | Accountable | Consulted | Informed |
|---|---|---|---|---|
| Asset-Inventar pflegen | Asset Manager | Asset Owner | Enterprise Architect, Operations | CCSO |
| Asset-Kritikalität festlegen | Asset Owner | Management | Enterprise Architect, Security Architect | Engineering Lead |
| Scanner betreiben | Security Architect | CCSO | DevOps / Platform Team | Asset Owner |
| Findings bewerten | Security Architect | CCSO | Asset Manager, Engineering Lead | Asset Owner |
| Issues priorisieren | Asset Owner | Product Owner | Security Architect, Engineering Lead | Management |
| Remediation planen | Engineering Lead | Asset Owner | DevOps / Platform Team, Operations | CCSO |
| Patch testen | QA | Engineering Lead | Operations | Asset Owner |
| Patch deployen | DevOps / Platform Team | Operations | Change Manager, Engineering Lead | Asset Owner |
| Exception genehmigen | Asset Owner | CCSO | Security Architect, Legal / Compliance | Management |
| Incident eskalieren | Incident Commander | CCSO | Security Architect, Operations | Management |
| Change freigeben | Change Manager | CAB | Security Architect, Operations | Asset Owner |
| Reporting erstellen | CCSO | Management | Enterprise Architect, Security Architect | Alle relevanten Stakeholder |
| Risiko akzeptieren | Asset Owner | Management | CCSO, Legal / Compliance | Enterprise Architect |
| Lessons Learned umsetzen | Engineering Lead | Asset Owner | Security Architect, QA | Management |

---

## Informationsmodell: Welche Daten du brauchst

| Entität | Wichtige Felder | Warum wichtig |
|---|---|---|
| Asset | asset_id, name, typ, owner, manager, kritikalität, exposition, lebenszyklusstatus, standort | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Finding | finding_id, quelle, cve, cvss, asset_id, ersterkennung, status, raw_evidence | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Issue | issue_id, finding_id, priorität, frist, verantwortlicher, status, remediation_plan | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Exception | exception_id, asset_id, regel, grund, gültig_bis, kompensationsmaßnahmen, restrisiko | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Patch | patch_id, hersteller, version, betroffene_assets, teststatus, deployment_status | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Change | change_id, change_typ, wartungsfenster, rollback_plan, genehmigung, ergebnis | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Incident | incident_id, schweregrad, trigger, betroffene_assets, commander, status | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Evidence | evidence_id, typ, quelle, datum, prüfer, bezug | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Report | report_id, zeitraum, zielgruppe, kpis, entscheidungsbedarf | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |
| Risk | risk_id, szenario, wahrscheinlichkeit, auswirkung, kontrollen, restrisiko | Ohne diese Daten kann der Prozess nicht sauber gesteuert und geprüft werden. |

### Datenqualitätsregeln

- Asset-ID muss eindeutig sein.
- Owner darf nicht leer sein.
- Kritikalität muss aus einem definierten Modell stammen.
- Exposition muss mindestens zwischen internet-facing und intern unterscheiden.
- Finding muss eine Quelle haben.
- Issue muss eine Frist haben.
- Exception muss ein Ablaufdatum haben.
- Patch muss eine Zielversion oder Zielkonfiguration benennen.
- Change muss einen Rollback-Plan enthalten, wenn Produktion betroffen ist.
- Evidence muss prüfbar sein.
- Report muss einen Zeitraum haben.
- Risiko muss ein Szenario beschreiben.
- Restrisiko muss nach Kompensationsmaßnahmen bewertet werden.
- Statuswerte müssen standardisiert sein.
- Geschlossene Issues müssen einen Verifikationsnachweis haben.

---

## Vulnerability Management Prozess in einfacher Sprache

| Schritt | Was passiert | Coach-Hinweis |
|---|---|---|
| 1. Identifikation | Schwachstellen werden aus Scannern, Tests, Herstellerhinweisen, CERT-Meldungen und internen Meldungen gesammelt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 2. Kategorisierung | Findings werden technisch und fachlich bewertet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 3. Priorisierung | Aus relevanten Findings werden Issues mit Frist und Verantwortung. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 4. Zuweisung | Das Issue wird dem zuständigen Asset Manager oder Team zugeordnet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 5. Remediation | Die Schwachstelle wird behoben, entschärft oder als Exception behandelt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 6. Verifikation | Ein Re-Scan oder Nachweis bestätigt die Behebung. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 7. Reporting | Status, Trends und Risiken werden berichtet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 8. Verbesserung | Wiederkehrende Ursachen werden systematisch reduziert. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |

### Arbeitsanweisung

#### 1. Identifikation

- Ziel: Schwachstellen werden aus Scannern, Tests, Herstellerhinweisen, CERT-Meldungen und internen Meldungen gesammelt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 2. Kategorisierung

- Ziel: Findings werden technisch und fachlich bewertet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 3. Priorisierung

- Ziel: Aus relevanten Findings werden Issues mit Frist und Verantwortung.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 4. Zuweisung

- Ziel: Das Issue wird dem zuständigen Asset Manager oder Team zugeordnet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 5. Remediation

- Ziel: Die Schwachstelle wird behoben, entschärft oder als Exception behandelt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 6. Verifikation

- Ziel: Ein Re-Scan oder Nachweis bestätigt die Behebung.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 7. Reporting

- Ziel: Status, Trends und Risiken werden berichtet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 8. Verbesserung

- Ziel: Wiederkehrende Ursachen werden systematisch reduziert.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

---

## Patch Management Prozess in einfacher Sprache

| Schritt | Was passiert | Coach-Hinweis |
|---|---|---|
| 1. Monitoring | Hersteller, CVE-Quellen, CERT, CISA KEV und Threat Intelligence werden beobachtet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 2. Klassifizierung | Patches werden nach Dringlichkeit, Asset-Kritikalität und Exposition bewertet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 3. Test | Patches werden in geeigneter Umgebung getestet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 4. Freigabe | Der Change wird je nach Typ freigegeben. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 5. Deployment | Der Patch wird ausgerollt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 6. Verifikation | Systemfunktion und Schwachstellenstatus werden geprüft. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 7. Dokumentation | Nachweise werden abgelegt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 8. Reporting | Patch-Compliance und offene Risiken werden berichtet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |

### Arbeitsanweisung

#### 1. Monitoring

- Ziel: Hersteller, CVE-Quellen, CERT, CISA KEV und Threat Intelligence werden beobachtet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 2. Klassifizierung

- Ziel: Patches werden nach Dringlichkeit, Asset-Kritikalität und Exposition bewertet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 3. Test

- Ziel: Patches werden in geeigneter Umgebung getestet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 4. Freigabe

- Ziel: Der Change wird je nach Typ freigegeben.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 5. Deployment

- Ziel: Der Patch wird ausgerollt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 6. Verifikation

- Ziel: Systemfunktion und Schwachstellenstatus werden geprüft.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 7. Dokumentation

- Ziel: Nachweise werden abgelegt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 8. Reporting

- Ziel: Patch-Compliance und offene Risiken werden berichtet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

---

## Exception Management Prozess in einfacher Sprache

| Schritt | Was passiert | Coach-Hinweis |
|---|---|---|
| 1. Erkennen | Eine Abweichung wird erkannt oder absehbar. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 2. Dokumentieren | Asset, Owner, Regel, Grund und Zeitraum werden erfasst. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 3. Risikoanalyse | Risiko vor und nach Kompensation wird bewertet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 4. Qualitätssicherung | Security prüft Plausibilität und Angemessenheit. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 5. Genehmigung | Owner und ggf. CCSO genehmigen die Exception. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 6. Monitoring | Die Ausnahme wird überwacht. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 7. Erneuerung oder Schließung | Die Exception läuft aus, wird begründet verlängert oder geschlossen. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 8. Überführung | Langfristige Risiken gehen ins Risikoregister oder Schutzkonzept. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |

### Arbeitsanweisung

#### 1. Erkennen

- Ziel: Eine Abweichung wird erkannt oder absehbar.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 2. Dokumentieren

- Ziel: Asset, Owner, Regel, Grund und Zeitraum werden erfasst.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 3. Risikoanalyse

- Ziel: Risiko vor und nach Kompensation wird bewertet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 4. Qualitätssicherung

- Ziel: Security prüft Plausibilität und Angemessenheit.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 5. Genehmigung

- Ziel: Owner und ggf. CCSO genehmigen die Exception.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 6. Monitoring

- Ziel: Die Ausnahme wird überwacht.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 7. Erneuerung oder Schließung

- Ziel: Die Exception läuft aus, wird begründet verlängert oder geschlossen.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 8. Überführung

- Ziel: Langfristige Risiken gehen ins Risikoregister oder Schutzkonzept.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

---

## Incident Response Prozess in einfacher Sprache

| Schritt | Was passiert | Coach-Hinweis |
|---|---|---|
| 1. Preparation | Plan, Rollen, Tools und Übungen werden vorbereitet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 2. Identification | Vorfall wird erkannt, klassifiziert und initial dokumentiert. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 3. Containment | Ausbreitung wird verhindert. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 4. Eradication | Ursache und Angriffsreste werden beseitigt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 5. Recovery | Systeme werden kontrolliert wiederhergestellt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 6. Communication | Stakeholder werden abgestimmt informiert. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 7. Lessons Learned | Ursachen und Verbesserungen werden abgeleitet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 8. Architecture Feedback | Erkenntnisse fließen in Zielarchitektur und Roadmap ein. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |

### Arbeitsanweisung

#### 1. Preparation

- Ziel: Plan, Rollen, Tools und Übungen werden vorbereitet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 2. Identification

- Ziel: Vorfall wird erkannt, klassifiziert und initial dokumentiert.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 3. Containment

- Ziel: Ausbreitung wird verhindert.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 4. Eradication

- Ziel: Ursache und Angriffsreste werden beseitigt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 5. Recovery

- Ziel: Systeme werden kontrolliert wiederhergestellt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 6. Communication

- Ziel: Stakeholder werden abgestimmt informiert.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 7. Lessons Learned

- Ziel: Ursachen und Verbesserungen werden abgeleitet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 8. Architecture Feedback

- Ziel: Erkenntnisse fließen in Zielarchitektur und Roadmap ein.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

---

## Change Management Prozess in einfacher Sprache

| Schritt | Was passiert | Coach-Hinweis |
|---|---|---|
| 1. Change erfassen | Änderung wird beschrieben. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 2. Risiko bewerten | Auswirkung auf Sicherheit, Verfügbarkeit und Integrität wird geprüft. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 3. Testnachweis prüfen | Tests und Rollback werden bewertet. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 4. Freigabe durchführen | Standard, Normal, Emergency oder Major Change wird passend behandelt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 5. Umsetzung durchführen | Änderung wird kontrolliert umgesetzt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 6. Ergebnis prüfen | Funktion und Sicherheit werden geprüft. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 7. Dokumentieren | Nachweise und Ergebnis werden abgelegt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |
| 8. Lernen | Probleme werden in Prozessverbesserungen übersetzt. | Mache den Schritt so klar, dass eine andere Rolle ihn prüfen kann. |

### Arbeitsanweisung

#### 1. Change erfassen

- Ziel: Änderung wird beschrieben.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 2. Risiko bewerten

- Ziel: Auswirkung auf Sicherheit, Verfügbarkeit und Integrität wird geprüft.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 3. Testnachweis prüfen

- Ziel: Tests und Rollback werden bewertet.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 4. Freigabe durchführen

- Ziel: Standard, Normal, Emergency oder Major Change wird passend behandelt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 5. Umsetzung durchführen

- Ziel: Änderung wird kontrolliert umgesetzt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 6. Ergebnis prüfen

- Ziel: Funktion und Sicherheit werden geprüft.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 7. Dokumentieren

- Ziel: Nachweise und Ergebnis werden abgelegt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

#### 8. Lernen

- Ziel: Probleme werden in Prozessverbesserungen übersetzt.
- Eingang: Informationen, Tickets, Scan-Ergebnisse, Meldungen oder Entscheidungen.
- Ausgang: Ein dokumentierter, prüfbarer nächster Zustand.
- Owner: Die Rolle, die den Schritt operativ führt.
- Kontrolle: Ein Nachweis, der zeigt, dass der Schritt erledigt wurde.
- Fehlerbild: Der Schritt wird übersprungen oder nur mündlich erledigt.
- Coach-Frage: Was müsste ein Auditor, Architekt oder neuer Kollege sehen, um den Schritt zu verstehen?

---

## Priorisierungsmodell

### Basisklassen

| CVSS-Bereich | Kritikalität | Einfache Interpretation | Typische Reaktion |
|---|---|---|---|
| 0.0 | None | Keine erkennbare Auswirkung. | Dokumentieren oder ignorieren, je nach Prozess. |
| 0.1 – 3.9 | Low | Geringe technische Auswirkung. | In normalem Wartungszyklus behandeln. |
| 4.0 – 6.9 | Medium | Mittlere technische Auswirkung. | Planbar beheben und überwachen. |
| 7.0 – 8.9 | High | Hohe technische Auswirkung. | Priorisiert beheben. |
| 9.0 – 10.0 | Critical | Sehr hohe technische Auswirkung. | Sofort priorisieren und eng verfolgen. |

### Zusatzfaktoren

| Faktor | Frage | Wirkung |
|---|---|---|
| Internet-facing | Ist das System aus dem Internet erreichbar? | Ja erhöht Priorität deutlich. |
| KEV | Ist die Schwachstelle als aktiv ausgenutzt bekannt? | Ja kann Sofortmaßnahme auslösen. |
| Exploit verfügbar | Gibt es öffentlichen Angriffscode? | Ja erhöht Umsetzungsdruck. |
| Asset-Kritikalität | Wie wichtig ist das System für Geschäft oder Betrieb? | Höhere Kritikalität verkürzt Frist. |
| Datenklasse | Sind vertrauliche, personenbezogene oder geschäftskritische Daten betroffen? | Erhöht Auswirkung. |
| Authentifizierung | Muss ein Angreifer angemeldet sein? | Keine Authentifizierung erhöht Risiko. |
| User Interaction | Muss ein Benutzer aktiv etwas tun? | Keine Interaktion erhöht Risiko. |
| Kompensationsmaßnahmen | Gibt es bereits Schutzmaßnahmen? | Kann Risiko senken, ersetzt aber nicht automatisch die Behebung. |
| Ausbreitungspotenzial | Kann die Schwachstelle lateral movement ermöglichen? | Erhöht Incident-Nähe. |
| Betriebsabhängigkeit | Wie riskant ist ein Patch im Betrieb? | Beeinflusst Umsetzungsweg, nicht die Relevanz. |

### Beispiel für Prioritätsentscheidung

Ein CVSS-7.5-Finding auf einem isolierten internen Testsystem kann planbar behandelt werden.

Dasselbe CVSS-7.5-Finding auf einem internet-exponierten Produktivsystem mit öffentlichem Exploit kann wie ein kritisches Thema behandelt werden.

Die Regel lautet: CVSS ist der Anfang der Bewertung, nicht das Ende.

---

## Architektur-Anforderungen

| ID | Bereich | Anforderung | Prüfkriterium |
|---|---|---|---|
| REQ-01-01 | Asset Management | Die Organisation muss für Asset Management eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-02 | Asset Management | Die Organisation muss für Asset Management einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-03 | Asset Management | Die Organisation muss für Asset Management prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-04 | Asset Management | Die Organisation muss für Asset Management Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-05 | Asset Management | Die Organisation muss für Asset Management Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-06 | Asset Management | Die Organisation muss für Asset Management eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-07 | Asset Management | Die Organisation muss für Asset Management einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-08 | Asset Management | Die Organisation muss für Asset Management prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-09 | Asset Management | Die Organisation muss für Asset Management Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-01-10 | Asset Management | Die Organisation muss für Asset Management Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-01 | Detection | Die Organisation muss für Detection eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-02 | Detection | Die Organisation muss für Detection einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-03 | Detection | Die Organisation muss für Detection prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-04 | Detection | Die Organisation muss für Detection Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-05 | Detection | Die Organisation muss für Detection Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-06 | Detection | Die Organisation muss für Detection eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-07 | Detection | Die Organisation muss für Detection einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-08 | Detection | Die Organisation muss für Detection prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-09 | Detection | Die Organisation muss für Detection Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-02-10 | Detection | Die Organisation muss für Detection Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-01 | Priorisierung | Die Organisation muss für Priorisierung eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-02 | Priorisierung | Die Organisation muss für Priorisierung einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-03 | Priorisierung | Die Organisation muss für Priorisierung prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-04 | Priorisierung | Die Organisation muss für Priorisierung Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-05 | Priorisierung | Die Organisation muss für Priorisierung Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-06 | Priorisierung | Die Organisation muss für Priorisierung eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-07 | Priorisierung | Die Organisation muss für Priorisierung einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-08 | Priorisierung | Die Organisation muss für Priorisierung prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-09 | Priorisierung | Die Organisation muss für Priorisierung Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-03-10 | Priorisierung | Die Organisation muss für Priorisierung Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-01 | Remediation | Die Organisation muss für Remediation eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-02 | Remediation | Die Organisation muss für Remediation einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-03 | Remediation | Die Organisation muss für Remediation prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-04 | Remediation | Die Organisation muss für Remediation Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-05 | Remediation | Die Organisation muss für Remediation Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-06 | Remediation | Die Organisation muss für Remediation eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-07 | Remediation | Die Organisation muss für Remediation einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-08 | Remediation | Die Organisation muss für Remediation prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-09 | Remediation | Die Organisation muss für Remediation Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-04-10 | Remediation | Die Organisation muss für Remediation Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-01 | Patching | Die Organisation muss für Patching eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-02 | Patching | Die Organisation muss für Patching einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-03 | Patching | Die Organisation muss für Patching prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-04 | Patching | Die Organisation muss für Patching Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-05 | Patching | Die Organisation muss für Patching Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-06 | Patching | Die Organisation muss für Patching eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-07 | Patching | Die Organisation muss für Patching einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-08 | Patching | Die Organisation muss für Patching prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-09 | Patching | Die Organisation muss für Patching Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-05-10 | Patching | Die Organisation muss für Patching Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-01 | Exception Management | Die Organisation muss für Exception Management eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-02 | Exception Management | Die Organisation muss für Exception Management einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-03 | Exception Management | Die Organisation muss für Exception Management prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-04 | Exception Management | Die Organisation muss für Exception Management Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-05 | Exception Management | Die Organisation muss für Exception Management Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-06 | Exception Management | Die Organisation muss für Exception Management eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-07 | Exception Management | Die Organisation muss für Exception Management einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-08 | Exception Management | Die Organisation muss für Exception Management prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-09 | Exception Management | Die Organisation muss für Exception Management Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-06-10 | Exception Management | Die Organisation muss für Exception Management Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-01 | Incident Response | Die Organisation muss für Incident Response eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-02 | Incident Response | Die Organisation muss für Incident Response einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-03 | Incident Response | Die Organisation muss für Incident Response prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-04 | Incident Response | Die Organisation muss für Incident Response Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-05 | Incident Response | Die Organisation muss für Incident Response Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-06 | Incident Response | Die Organisation muss für Incident Response eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-07 | Incident Response | Die Organisation muss für Incident Response einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-08 | Incident Response | Die Organisation muss für Incident Response prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-09 | Incident Response | Die Organisation muss für Incident Response Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-07-10 | Incident Response | Die Organisation muss für Incident Response Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-01 | Change Management | Die Organisation muss für Change Management eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-02 | Change Management | Die Organisation muss für Change Management einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-03 | Change Management | Die Organisation muss für Change Management prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-04 | Change Management | Die Organisation muss für Change Management Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-05 | Change Management | Die Organisation muss für Change Management Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-06 | Change Management | Die Organisation muss für Change Management eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-07 | Change Management | Die Organisation muss für Change Management einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-08 | Change Management | Die Organisation muss für Change Management prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-09 | Change Management | Die Organisation muss für Change Management Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-08-10 | Change Management | Die Organisation muss für Change Management Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-01 | Reporting | Die Organisation muss für Reporting eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-02 | Reporting | Die Organisation muss für Reporting einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-03 | Reporting | Die Organisation muss für Reporting prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-04 | Reporting | Die Organisation muss für Reporting Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-05 | Reporting | Die Organisation muss für Reporting Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-06 | Reporting | Die Organisation muss für Reporting eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-07 | Reporting | Die Organisation muss für Reporting einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-08 | Reporting | Die Organisation muss für Reporting prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-09 | Reporting | Die Organisation muss für Reporting Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-09-10 | Reporting | Die Organisation muss für Reporting Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-01 | BC/DR | Die Organisation muss für BC/DR eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-02 | BC/DR | Die Organisation muss für BC/DR einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-03 | BC/DR | Die Organisation muss für BC/DR prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-04 | BC/DR | Die Organisation muss für BC/DR Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-05 | BC/DR | Die Organisation muss für BC/DR Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-06 | BC/DR | Die Organisation muss für BC/DR eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-07 | BC/DR | Die Organisation muss für BC/DR einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-08 | BC/DR | Die Organisation muss für BC/DR prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-09 | BC/DR | Die Organisation muss für BC/DR Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-10-10 | BC/DR | Die Organisation muss für BC/DR Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-01 | Governance | Die Organisation muss für Governance eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-02 | Governance | Die Organisation muss für Governance einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-03 | Governance | Die Organisation muss für Governance prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-04 | Governance | Die Organisation muss für Governance Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-05 | Governance | Die Organisation muss für Governance Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-06 | Governance | Die Organisation muss für Governance eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-07 | Governance | Die Organisation muss für Governance einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-08 | Governance | Die Organisation muss für Governance prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-09 | Governance | Die Organisation muss für Governance Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-11-10 | Governance | Die Organisation muss für Governance Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-01 | Tooling | Die Organisation muss für Tooling eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-02 | Tooling | Die Organisation muss für Tooling einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-03 | Tooling | Die Organisation muss für Tooling prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-04 | Tooling | Die Organisation muss für Tooling Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-05 | Tooling | Die Organisation muss für Tooling Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-06 | Tooling | Die Organisation muss für Tooling eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-07 | Tooling | Die Organisation muss für Tooling einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-08 | Tooling | Die Organisation muss für Tooling prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-09 | Tooling | Die Organisation muss für Tooling Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-12-10 | Tooling | Die Organisation muss für Tooling Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-01 | Datenqualität | Die Organisation muss für Datenqualität eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-02 | Datenqualität | Die Organisation muss für Datenqualität einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-03 | Datenqualität | Die Organisation muss für Datenqualität prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-04 | Datenqualität | Die Organisation muss für Datenqualität Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-05 | Datenqualität | Die Organisation muss für Datenqualität Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-06 | Datenqualität | Die Organisation muss für Datenqualität eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-07 | Datenqualität | Die Organisation muss für Datenqualität einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-08 | Datenqualität | Die Organisation muss für Datenqualität prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-09 | Datenqualität | Die Organisation muss für Datenqualität Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-13-10 | Datenqualität | Die Organisation muss für Datenqualität Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-01 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-02 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-03 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-04 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-05 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-06 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-07 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-08 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-09 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-14-10 | Auditfähigkeit | Die Organisation muss für Auditfähigkeit Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-01 | Schulung | Die Organisation muss für Schulung eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-02 | Schulung | Die Organisation muss für Schulung einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-03 | Schulung | Die Organisation muss für Schulung prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-04 | Schulung | Die Organisation muss für Schulung Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-05 | Schulung | Die Organisation muss für Schulung Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-06 | Schulung | Die Organisation muss für Schulung eine klare verantwortliche Rolle benennen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-07 | Schulung | Die Organisation muss für Schulung einen dokumentierten Prozessschritt definieren. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-08 | Schulung | Die Organisation muss für Schulung prüfbare Nachweise erzeugen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-09 | Schulung | Die Organisation muss für Schulung Kennzahlen oder Statusinformationen bereitstellen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |
| REQ-15-10 | Schulung | Die Organisation muss für Schulung Eskalations- oder Entscheidungsregeln festlegen. | Ist die Anforderung dokumentiert, einem Owner zugeordnet und im Alltag nachweisbar? |

---

## Kontrollkatalog

| Control-ID | Bereich | Kontrolle | Frequenz | Evidenz |
|---|---|---|---|---|
| CTRL-001 | Asset-Inventar | Alle produktiven Assets sind in einer zentralen Quelle erfasst. | Monatlich | CMDB-Export |
| CTRL-002 | Owner-Zuordnung | Jedes kritische Asset hat einen Asset Owner. | Monatlich | Asset-Liste mit Owner-Feld |
| CTRL-003 | Expositionsprüfung | Internet-facing Assets sind markiert. | Monatlich | Scan-Ergebnis oder Asset-Metadaten |
| CTRL-004 | Scanner-Abdeckung | Kritische Netzwerkbereiche werden regelmäßig gescannt. | Wöchentlich | Scan-Report |
| CTRL-005 | SCA-Abdeckung | Produktive Anwendungen werden auf abhängige Bibliotheken geprüft. | Bei Build und monatlich | SCA-Report |
| CTRL-006 | Container-Scanning | Produktive Container-Images werden geprüft. | Bei Build | Image-Scan-Report |
| CTRL-007 | KEV-Prüfung | CISA KEV oder vergleichbare Quelle wird für Priorisierung genutzt. | Täglich oder wöchentlich | KEV-Matching-Liste |
| CTRL-008 | Issue-Fristen | Issues haben Remediation-Fristen. | Laufend | Ticket-Auswertung |
| CTRL-009 | Überfällige Issues | Überfällige Issues werden eskaliert. | Wöchentlich | Eskalationslog |
| CTRL-010 | Patch-Test | Kritische Patches werden vor Produktion angemessen getestet. | Pro Change | Testnachweis |
| CTRL-011 | Rollback | Produktionschanges haben Rollback-Plan. | Pro Change | Change-Dokumentation |
| CTRL-012 | Verification | Geschlossene Issues haben Verifikationsnachweis. | Pro Issue | Re-Scan oder Nachweis |
| CTRL-013 | Exception-Ablauf | Exceptions haben Ablaufdatum. | Monatlich | Exception-Register |
| CTRL-014 | Restrisiko | Exceptions enthalten Restrisiko-Bewertung. | Pro Exception | Exception-Dokument |
| CTRL-015 | Incident-Trigger | Aktive Ausnutzung löst Incident-Prüfung aus. | Laufend | Incident-Ticket oder Triage-Vermerk |
| CTRL-016 | Backup-Test | Wiederherstellung wird getestet. | Monatlich | Restore-Test-Protokoll |
| CTRL-017 | Reporting | Management erhält Trendbericht. | Monatlich | Report |
| CTRL-018 | Lessons Learned | Major Incidents erzeugen Maßnahmen. | Pro Incident | Post-Incident-Review |
| CTRL-019 | Reifegradreview | Der VM-Prozess wird regelmäßig bewertet. | Quartalsweise | Maturity-Assessment |
| CTRL-020 | Architekturreview | Wesentliche Systemänderungen berücksichtigen Schwachstellen- und Patchfähigkeit. | Pro Architekturentscheidung | ADR oder Review-Protokoll |

---

## Coaching-Playbooks nach Rolle

### Playbook für Enterprise Architect

Deine Hauptaufgabe: Zielarchitektur, Capability Map, Roadmap, Architektur-Governance.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Security Architect

Deine Hauptaufgabe: Security-Kontrollen, Risikoannahmen, technische Schutzmaßnahmen.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für CCSO

Deine Hauptaufgabe: Security Governance, Eskalation, Richtlinien, Management-Reporting.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Asset Owner

Deine Hauptaufgabe: Fachliche Verantwortung, Risikoannahme, Priorisierung im Kontext.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Asset Manager

Deine Hauptaufgabe: Operative Nachverfolgung, Pflege, Koordination.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Engineering Lead

Deine Hauptaufgabe: Umsetzung in Teams, technische Qualität, Remediation-Planung.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für DevOps / Platform Team

Deine Hauptaufgabe: Automatisierung, Deployment, Patch-Pipelines, Infrastruktur.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Operations

Deine Hauptaufgabe: Betrieb, Wartungsfenster, Systemstabilität, Monitoring.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Product Owner

Deine Hauptaufgabe: Priorisierung im Produkt-Backlog, Business-Abwägung.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Change Manager

Deine Hauptaufgabe: Change-Prozess, CAB, Freigaben, Notfalländerungen.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Incident Commander

Deine Hauptaufgabe: Führung im Sicherheitsvorfall.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Legal / Compliance

Deine Hauptaufgabe: Meldepflichten, regulatorische Bewertung, Nachweise.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für Management

Deine Hauptaufgabe: Ressourcen, Zielkonflikte, Risikoentscheidungen.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

### Playbook für QA

Deine Hauptaufgabe: Testnachweise, Verifikation, Regression, Abnahme.

#### Was du jeden Monat prüfen solltest

- Welche offenen kritischen Risiken betreffen meinen Verantwortungsbereich?
- Welche Entscheidungen sind blockiert?
- Welche Fristen laufen in den nächsten 14 Tagen ab?
- Welche Exceptions laufen bald aus?
- Welche Systeme sind nicht sauber inventarisiert?
- Welche Reports zeigen Verschlechterung?
- Welche Ursachen treten wiederholt auf?
- Welche Architekturentscheidung kann das Problem dauerhaft reduzieren?
- Welche Teams brauchen Unterstützung?
- Welche Nachweise fehlen?

#### Gute Meeting-Sätze

- „Welches Asset ist betroffen und wer ist der Owner?“
- „Ist das ein Finding, ein Issue, eine Exception oder ein Incident?“
- „Welche Frist gilt und warum?“
- „Welche Evidenz brauchen wir zum Schließen?“
- „Was ist das Restrisiko, wenn wir nicht sofort patchen?“
- „Welche kompensierende Maßnahme senkt das Risiko bis zur Behebung?“
- „Welche Entscheidung wird heute benötigt?“
- „Was ist der nächste prüfbare Schritt?“

#### Fehler, die du vermeiden solltest

- Nur technische Details diskutieren und Verantwortung offenlassen.
- Risiko akzeptieren, ohne es aufzuschreiben.
- Fristen verschieben, ohne neue Begründung.
- Exceptions als Dauerzustand behandeln.
- Reporting als reine Statistik verstehen.

---

## 30-60-90-Tage-Umsetzungsplan

| Zeitraum | Ziel | Ergebnis |
|---|---|---|
| Tag 1 bis 30 — Klarheit schaffen | Scope definieren: Welche Organisationseinheiten, Systeme und Anwendungen sind enthalten? | Ein sichtbarer, steuerbarer Fortschritt statt abstrakter Absicht. |
| Tag 31 bis 60 — Prozess stabilisieren | Asset-Kritikalitätsmodell einführen. | Ein sichtbarer, steuerbarer Fortschritt statt abstrakter Absicht. |
| Tag 61 bis 90 — Steuerung beweisen | Coverage-Messung für Assets durchführen. | Ein sichtbarer, steuerbarer Fortschritt statt abstrakter Absicht. |

### Tag 1 bis 30 — Klarheit schaffen

1. Scope definieren: Welche Organisationseinheiten, Systeme und Anwendungen sind enthalten?
2. Vorhandenes Asset-Inventar sammeln.
3. Top-Assets identifizieren.
4. Owner-Lücken sichtbar machen.
5. Bestehende Scanner und Security-Tools erfassen.
6. Bestehende Patch-Prozesse sammeln.
7. Bestehende Change-Prozesse prüfen.
8. Bestehende Exception-Fälle suchen.
9. Erste kritische Risiken zusammenstellen.
10. Ein minimales Reporting aufbauen.
11. RACI-Entwurf erstellen.
12. Governance-Prinzipien formulieren.
13. Fristenmodell vorschlagen.
14. Eskalationsmodell vorschlagen.
15. Management-Entscheidung für Zielbild vorbereiten.

#### Abschlusskriterium

Der Zeitraum ist abgeschlossen, wenn die Ergebnisse dokumentiert, reviewt und in den nächsten Steuerungsschritt überführt wurden.

### Tag 31 bis 60 — Prozess stabilisieren

1. Asset-Kritikalitätsmodell einführen.
2. Finding-zu-Issue-Regel definieren.
3. Priorisierungsmodell einführen.
4. Ticket-Workflow definieren.
5. Patch-Workflow mit Change Management verbinden.
6. Exception-Template verbindlich machen.
7. Eskalationsstufen einführen.
8. Re-Scan-Regel definieren.
9. Management-Report monatlich etablieren.
10. Top-10-Risiken aktiv bearbeiten.
11. Tool-Schnittstellen prüfen.
12. KPIs definieren.
13. Teams schulen.
14. Erste Lessons Learned aus realen Fällen aufnehmen.
15. Roadmap für nächste Quartale erstellen.

#### Abschlusskriterium

Der Zeitraum ist abgeschlossen, wenn die Ergebnisse dokumentiert, reviewt und in den nächsten Steuerungsschritt überführt wurden.

### Tag 61 bis 90 — Steuerung beweisen

1. Coverage-Messung für Assets durchführen.
2. Scanner-Abdeckung verbessern.
3. SCA in relevante Build-Pipelines integrieren.
4. Patch-Compliance messen.
5. Überfällige Issues eskalieren.
6. Exceptions überprüfen.
7. KEV-Matching etablieren.
8. Critical- und High-Fristen aktiv steuern.
9. Management-Entscheidungen dokumentieren.
10. Auditfähige Evidence-Ablage definieren.
11. Architecture Governance Gate einführen.
12. Reifegrad-Assessment durchführen.
13. Roadmap anpassen.
14. Budget- und Ressourcenbedarf ableiten.
15. Nächste Verbesserungswelle starten.

#### Abschlusskriterium

Der Zeitraum ist abgeschlossen, wenn die Ergebnisse dokumentiert, reviewt und in den nächsten Steuerungsschritt überführt wurden.

---

## Templates

### Template: Asset-Steckbrief

```text
Asset-ID:
Asset-Name:
Asset-Typ:
Owner:
Asset Manager:
Business-Funktion:
Kritikalität:
Exposition:
Datenklasse:
Umgebung:
Abhängigkeiten:
Patch-Zyklus:
RTO:
RPO:
Letzte Prüfung:
Offene kritische Findings:
Offene Exceptions:
Nächster Review:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

### Template: Finding-Bewertung

```text
Finding-ID:
Quelle:
Asset-ID:
CVE:
CVSS:
Beschreibung:
Technischer Nachweis:
Exposition:
Exploit verfügbar:
KEV-Relevanz:
Business Impact:
False Positive geprüft:
Entscheidung:
Begründung:
Nächster Schritt:
Owner:
Frist:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

### Template: Issue-Beschreibung

```text
Issue-ID:
Finding-Referenz:
Betroffenes Asset:
Priorität:
Kritikalität:
Problem:
Risiko:
Erwartete Behebung:
Akzeptierter Nachweis:
Verantwortliches Team:
Frist:
Eskalationsdatum:
Status:
Verifikationsmethode:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

### Template: Exception-Antrag

```text
Exception-ID:
Asset:
Regelabweichung:
Grund:
Gültig von:
Gültig bis:
Risiko vor Kompensation:
Kompensationsmaßnahme 1:
Kompensationsmaßnahme 2:
Restrisiko:
Geplante Behebung:
Genehmiger:
CCSO-Prüfung:
Review-Datum:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

### Template: Patch-Change

```text
Change-ID:
Patch-ID:
Betroffene Assets:
Kritikalität:
Geplantes Wartungsfenster:
Testnachweis:
Rollback-Plan:
Kommunikationsbedarf:
Genehmigung:
Deployment-Verantwortlicher:
Startzeit:
Endzeit:
Ergebnis:
Verifikation:
Offene Folgeaufgaben:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

### Template: Incident-Triage

```text
Incident-ID:
Meldezeitpunkt:
Melder:
Betroffene Assets:
Schweregrad:
Auslöser:
Erste Beobachtung:
Vermutete Ursache:
Containment-Maßnahme:
Incident Commander:
Kommunikationsstatus:
Meldepflicht geprüft:
Nächster Statuszeitpunkt:
Aktuelle Entscheidung:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

### Template: Management-Report

```text
Berichtszeitraum:
Zusammenfassung:
Top-Risiken:
Offene Critical Issues:
Offene High Issues:
Überfällige Issues:
Patch-Compliance:
Exceptions:
Trendanalyse:
Entscheidungsbedarf:
Ressourcenbedarf:
Nächste Maßnahmen:
```

Coach-Hinweis: Nutze dieses Template nicht als starres Formular. Nutze es als Mindeststruktur, damit Entscheidungen nachvollziehbar bleiben.

---

## Architekturentscheidungen als ADR-Beispiele

### ADR-001 — Einführung eines zentralen Asset-Inventars

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Ohne Asset-Inventar kann kein risikobasiertes VM stattfinden. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-002 — Verbindliches Owner-Modell

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Jedes Asset benötigt fachliche und technische Verantwortung. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-003 — Einführung eines risikobasierten Priorisierungsmodells

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | CVSS wird um Exposition, KEV und Kritikalität ergänzt. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-004 — KEV-basierte Sofortpriorisierung

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Aktiv ausgenutzte Schwachstellen werden beschleunigt behandelt. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-005 — Exception Management mit Ablaufdatum

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Ausnahmen werden sichtbar, zeitlich begrenzt und überprüfbar. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-006 — Standard Changes für planbare Security Updates

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Wiederkehrende risikoarme Patches werden beschleunigt. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-007 — SCA in Build-Pipelines

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Abhängigkeiten werden frühzeitig geprüft. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-008 — Container-Image-Scanning

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Images werden vor Nutzung und regelmäßig geprüft. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-009 — Management-Reporting monatlich

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Risikoentwicklung wird regelmäßig steuerbar. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

### ADR-010 — Re-Scan als Schließbedingung

| Feld | Inhalt |
|---|---|
| Status | Vorschlag |
| Kontext | Issues werden erst nach Nachweis geschlossen. |
| Entscheidung | Die Organisation führt die beschriebene Praxis verbindlich ein. |
| Konsequenz | Rollen, Prozesse, Tools und Nachweise müssen angepasst werden. |
| Prüfpunkt | Die Entscheidung ist umgesetzt, wenn sie im Alltag messbar funktioniert. |

---

## Schulungsplan für Teams

| Modul | Thema | Lernziel | Übung |
|---|---|---|---|
| Modul 1 | Grundbegriffe | Finding, Issue, CVE, CVSS, Patch, Exception, Incident verstehen. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 2 | Rollen | Wer entscheidet, wer setzt um, wer akzeptiert Risiko? | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 3 | Asset-Verantwortung | Warum Owner-Zuordnung die Grundlage ist. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 4 | Priorisierung | Warum CVSS allein nicht reicht. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 5 | Patching | Wie planbares Security Patching funktioniert. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 6 | Exceptions | Wie man Abweichungen sauber dokumentiert. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 7 | Incident-Übergang | Wann aus Schwachstelle ein Vorfall wird. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 8 | Change-Verbindung | Wie Remediation und Change Management zusammenspielen. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 9 | Reporting | Welche Kennzahlen wirklich helfen. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |
| Modul 10 | Praxisübung | Ein reales Finding wird vom Scan bis zur Schließung durchgespielt. | Bearbeite ein Beispiel aus dem eigenen Arbeitskontext. |

### Modul 1: Grundbegriffe

Ziel: Finding, Issue, CVE, CVSS, Patch, Exception, Incident verstehen.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 2: Rollen

Ziel: Wer entscheidet, wer setzt um, wer akzeptiert Risiko?

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 3: Asset-Verantwortung

Ziel: Warum Owner-Zuordnung die Grundlage ist.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 4: Priorisierung

Ziel: Warum CVSS allein nicht reicht.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 5: Patching

Ziel: Wie planbares Security Patching funktioniert.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 6: Exceptions

Ziel: Wie man Abweichungen sauber dokumentiert.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 7: Incident-Übergang

Ziel: Wann aus Schwachstelle ein Vorfall wird.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 8: Change-Verbindung

Ziel: Wie Remediation und Change Management zusammenspielen.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 9: Reporting

Ziel: Welche Kennzahlen wirklich helfen.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

### Modul 10: Praxisübung

Ziel: Ein reales Finding wird vom Scan bis zur Schließung durchgespielt.

#### Ablauf

1. Begriff oder Prozess in einfacher Sprache erklären.
2. Ein reales oder realistisches Beispiel zeigen.
3. Rollen im Beispiel benennen.
4. Entscheidungspunkt identifizieren.
5. Nachweis definieren.
6. Fehlerbild diskutieren.
7. Gute Praxis formulieren.
8. Mini-Übung durchführen.
9. Ergebnis im Team reflektieren.
10. Nächsten Verbesserungsimpuls ableiten.

---

## Kennzahlen, die wirklich helfen

| Kennzahl | Frage | Nutzen | Vorsicht |
|---|---|---|---|
| Mean Time to Detect | Wie lange dauert es, bis eine Schwachstelle erkannt wird? | Zeigt Detection-Fähigkeit. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Mean Time to Triage | Wie lange dauert die Erstbewertung? | Zeigt Prozessklarheit. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Mean Time to Remediate | Wie lange dauert Behebung? | Zeigt operative Wirksamkeit. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Patch Compliance | Welcher Anteil der Systeme ist innerhalb der Frist gepatcht? | Zeigt Wartungsdisziplin. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Overdue Critical Issues | Wie viele kritische Issues sind überfällig? | Zeigt akuten Handlungsbedarf. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Exception Count | Wie viele aktive Exceptions gibt es? | Zeigt Abweichungsniveau. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Exception Aging | Wie alt sind aktive Exceptions? | Zeigt Dauerzustände. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Reopen Rate | Wie oft werden geschlossene Issues wieder geöffnet? | Zeigt Qualität der Behebung. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Scanner Coverage | Wie viele Assets werden geprüft? | Zeigt Transparenz. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Owner Coverage | Wie viele Assets haben Owner? | Zeigt Verantwortungsqualität. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| KEV Exposure | Wie viele Assets sind von aktiv ausgenutzten Schwachstellen betroffen? | Zeigt prioritäres Risiko. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Backup Restore Success | Wie oft gelingt Wiederherstellung im Test? | Zeigt Resilienz. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Incident Conversion Rate | Wie viele Schwachstellen werden zu Incidents? | Zeigt Eskalations- und Präventionslage. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Root Cause Recurrence | Welche Ursachen wiederholen sich? | Zeigt strukturelle Schwächen. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |
| Architecture Debt from Vulnerabilities | Welche Architekturprobleme erzeugen wiederholt Schwachstellen? | Zeigt strategischen Verbesserungsbedarf. | Nicht isoliert interpretieren, sondern im Kontext von Kritikalität und Trend lesen. |

Coach-Hinweis: Eine gute Kennzahl führt zu einer besseren Entscheidung. Eine schlechte Kennzahl erzeugt nur Druck ohne Richtung.

---

## Anti-Patterns und Korrekturen

| Anti-Pattern | Beschreibung | Korrektur |
|---|---|---|
| Scanner-Falle | Die Organisation glaubt, ein Scanner sei bereits Vulnerability Management. | Definiere Rollen, Fristen, Eskalation und Verifikation. |
| CVSS-Tunnelblick | Alle Entscheidungen folgen blind dem CVSS-Score. | Nutze Kontextfaktoren wie Exposition, KEV, Asset-Kritikalität und Business Impact. |
| Owner-Lücke | Findings haben keine echte verantwortliche Person. | Führe Owner-Pflicht im Asset-Inventar ein. |
| Ticket-Friedhof | Tickets werden erstellt, aber nicht aktiv gesteuert. | Führe wöchentliches Review überfälliger Issues ein. |
| Exception-Dauerzustand | Ausnahmen werden verlängert, ohne echte Begründung. | Setze harte Reviews und Management-Sichtbarkeit. |
| Change-Blockade | Patches scheitern an zu schwerfälligen Freigaben. | Definiere Standard Changes und Emergency Changes. |
| Report-Theater | Reports sehen professionell aus, führen aber zu keiner Entscheidung. | Jeder Report muss Entscheidungsbedarf zeigen. |
| Tool-Silo | Scanner, CMDB und Ticket-System sprechen nicht miteinander. | Definiere Datenflüsse und Integrationsanforderungen. |
| Security-Alleingang | Security bewertet alles, aber Umsetzung bleibt in Teams hängen. | Baue gemeinsames Operating Model mit Engineering und Betrieb. |
| Architekturblindheit | Wiederkehrende Schwachstellen werden gepatcht, aber Architekturursachen bleiben. | Leite Architekturmaßnahmen aus Root Causes ab. |

### Scanner-Falle

Problem: Die Organisation glaubt, ein Scanner sei bereits Vulnerability Management.

Korrektur: Definiere Rollen, Fristen, Eskalation und Verifikation.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### CVSS-Tunnelblick

Problem: Alle Entscheidungen folgen blind dem CVSS-Score.

Korrektur: Nutze Kontextfaktoren wie Exposition, KEV, Asset-Kritikalität und Business Impact.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Owner-Lücke

Problem: Findings haben keine echte verantwortliche Person.

Korrektur: Führe Owner-Pflicht im Asset-Inventar ein.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Ticket-Friedhof

Problem: Tickets werden erstellt, aber nicht aktiv gesteuert.

Korrektur: Führe wöchentliches Review überfälliger Issues ein.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Exception-Dauerzustand

Problem: Ausnahmen werden verlängert, ohne echte Begründung.

Korrektur: Setze harte Reviews und Management-Sichtbarkeit.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Change-Blockade

Problem: Patches scheitern an zu schwerfälligen Freigaben.

Korrektur: Definiere Standard Changes und Emergency Changes.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Report-Theater

Problem: Reports sehen professionell aus, führen aber zu keiner Entscheidung.

Korrektur: Jeder Report muss Entscheidungsbedarf zeigen.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Tool-Silo

Problem: Scanner, CMDB und Ticket-System sprechen nicht miteinander.

Korrektur: Definiere Datenflüsse und Integrationsanforderungen.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Security-Alleingang

Problem: Security bewertet alles, aber Umsetzung bleibt in Teams hängen.

Korrektur: Baue gemeinsames Operating Model mit Engineering und Betrieb.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

### Architekturblindheit

Problem: Wiederkehrende Schwachstellen werden gepatcht, aber Architekturursachen bleiben.

Korrektur: Leite Architekturmaßnahmen aus Root Causes ab.

Coach-Frage: Welche wiederholte Beobachtung zeigt, dass dieses Anti-Pattern bei uns existiert?

---

## Szenario-Playbooks

### Szenario: Kritische internet-exponierte Schwachstelle mit öffentlichem Exploit

1. Finding sofort als potenziell kritisch markieren.
2. Betroffenes Asset und Owner bestimmen.
3. Prüfen, ob KEV oder aktive Ausnutzung bekannt ist.
4. Incident-Triage durchführen.
5. Temporäre Schutzmaßnahme prüfen.
6. Emergency Change vorbereiten.
7. Patch oder Workaround testen.
8. Umsetzung im beschleunigten Verfahren durchführen.
9. Re-Scan oder technische Verifikation durchführen.
10. Management informieren.

Abschlusskriterium: Das Szenario ist abgeschlossen, wenn Risiko, Maßnahme, Nachweis und nächste Verbesserung dokumentiert sind.

### Szenario: High Finding auf internem Business-Critical-System

1. Finding validieren.
2. Business Impact bewerten.
3. Frist nach Kritikalität und Asset-Level setzen.
4. Team zuweisen.
5. Patch-Verfügbarkeit prüfen.
6. Change-Zyklus planen.
7. Regressionstest definieren.
8. Patch umsetzen.
9. Verifikation dokumentieren.
10. Trend im Reporting aufnehmen.

Abschlusskriterium: Das Szenario ist abgeschlossen, wenn Risiko, Maßnahme, Nachweis und nächste Verbesserung dokumentiert sind.

### Szenario: Patch nicht möglich wegen Herstellerabhängigkeit

1. Technischen Grund dokumentieren.
2. Herstellerstatement sichern.
3. Risiko vor Kompensation bewerten.
4. Kompensierende Maßnahmen definieren.
5. Exception beantragen.
6. CCSO-Prüfung durchführen.
7. Review-Datum setzen.
8. Migrations- oder Behebungsplan erstellen.
9. Monitoring erhöhen.
10. Exception regelmäßig prüfen.

Abschlusskriterium: Das Szenario ist abgeschlossen, wenn Risiko, Maßnahme, Nachweis und nächste Verbesserung dokumentiert sind.

### Szenario: EoL-System in produktiver Umgebung

1. EoL-Status bestätigen.
2. Asset-Kritikalität bewerten.
3. Exposition prüfen.
4. Schutzmaßnahmen sofort prüfen.
5. Segmentierung bewerten.
6. Monitoring anpassen.
7. Exception oder Risikoregister-Eintrag erstellen.
8. Migration planen.
9. Budgetentscheidung vorbereiten.
10. Management-Risiko sichtbar machen.

Abschlusskriterium: Das Szenario ist abgeschlossen, wenn Risiko, Maßnahme, Nachweis und nächste Verbesserung dokumentiert sind.

### Szenario: Wiederkehrende Dependency-Schwachstellen in Java-Anwendung

1. SCA-Ergebnisse analysieren.
2. Betroffene Bibliotheken gruppieren.
3. Build-Pipeline prüfen.
4. Update-Strategie definieren.
5. Renovate oder Dependabot prüfen.
6. Regressionstest-Abdeckung bewerten.
7. Release-Frequenz anpassen.
8. Team-Schulung durchführen.
9. Architecture Debt dokumentieren.
10. Monatlichen Trend messen.

Abschlusskriterium: Das Szenario ist abgeschlossen, wenn Risiko, Maßnahme, Nachweis und nächste Verbesserung dokumentiert sind.

### Szenario: Ransomware-Verdacht nach Ausnutzung einer Schwachstelle

1. Incident Commander aktivieren.
2. Betroffene Systeme isolieren.
3. Forensische Sicherung beginnen.
4. Kommunikationsplan aktivieren.
5. Backup-Status prüfen.
6. Root Cause eingrenzen.
7. Schwachstelle schließen.
8. Systeme aus sauberer Quelle wiederherstellen.
9. Meldepflichten prüfen.
10. Lessons Learned und Architekturmaßnahmen ableiten.

Abschlusskriterium: Das Szenario ist abgeschlossen, wenn Risiko, Maßnahme, Nachweis und nächste Verbesserung dokumentiert sind.

---

## Architecture Governance Gates

| Gate | Prüffrage | Ergebnis |
|---|---|---|
| Gate 1: Scope und Asset-Basis | Gibt es eine klare Asset-Liste und Verantwortlichkeit? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 2: Risiko- und Kritikalitätsmodell | Ist klar, wie Priorität entsteht? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 3: Prozess-Design | Sind Prozessschritte, Rollen und Nachweise definiert? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 4: Tool-Integration | Sind Datenflüsse zwischen Scanner, CMDB, Ticket und Reporting klar? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 5: Change-Integration | Ist Remediation mit Change Management verbunden? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 6: Exception-Governance | Sind Ausnahmen zeitlich begrenzt und genehmigt? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 7: Incident-Verbindung | Ist aktive Ausnutzung als Eskalation definiert? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 8: Reporting | Gibt es operative und Management-Sicht? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 9: Evidence | Sind Nachweise auditfähig abgelegt? | Freigabe, Nachbesserung oder Management-Entscheidung. |
| Gate 10: Continuous Improvement | Werden Lessons Learned in Architektur und Prozess übertragen? | Freigabe, Nachbesserung oder Management-Entscheidung. |

### Gate 1: Scope und Asset-Basis

Prüffrage: Gibt es eine klare Asset-Liste und Verantwortlichkeit?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 2: Risiko- und Kritikalitätsmodell

Prüffrage: Ist klar, wie Priorität entsteht?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 3: Prozess-Design

Prüffrage: Sind Prozessschritte, Rollen und Nachweise definiert?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 4: Tool-Integration

Prüffrage: Sind Datenflüsse zwischen Scanner, CMDB, Ticket und Reporting klar?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 5: Change-Integration

Prüffrage: Ist Remediation mit Change Management verbunden?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 6: Exception-Governance

Prüffrage: Sind Ausnahmen zeitlich begrenzt und genehmigt?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 7: Incident-Verbindung

Prüffrage: Ist aktive Ausnutzung als Eskalation definiert?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 8: Reporting

Prüffrage: Gibt es operative und Management-Sicht?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 9: Evidence

Prüffrage: Sind Nachweise auditfähig abgelegt?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

### Gate 10: Continuous Improvement

Prüffrage: Werden Lessons Learned in Architektur und Prozess übertragen?

#### Mindestnachweise

- Dokumentierter Prozess
- Benannte Owner
- Messbare Kennzahl
- Nachweisablage
- Eskalationsregel

---

## TOGAF Deliverables und passende Security-Artefakte

| TOGAF Deliverable | Security-Artefakt | Inhalt |
|---|---|---|
| Architecture Vision | Security Capability Vision | Warum VM als Unternehmensfähigkeit wichtig ist. |
| Business Architecture Definition | Rollen- und Prozessmodell | Wer entscheidet und wer arbeitet. |
| Data Architecture Definition | Informationsmodell | Welche Daten benötigt werden. |
| Application Architecture Definition | Tool-Landschaft | Welche Anwendungen den Prozess unterstützen. |
| Technology Architecture Definition | Technologieabdeckung | Welche Plattformen geprüft und gepatcht werden. |
| Architecture Requirements Specification | Anforderungskatalog | Welche Anforderungen verbindlich gelten. |
| Architecture Roadmap | VM-Roadmap | Welche Verbesserungen wann kommen. |
| Implementation Governance Model | Governance Gates | Wie Umsetzung kontrolliert wird. |
| Architecture Contract | Verbindliche Vereinbarung mit Teams | Welche Pflichten und Nachweise gelten. |
| Compliance Assessment | Review und Audit | Ob der Prozess funktioniert. |

---

## Große Umsetzungschecklisten

### Checkliste: Asset Management

- [ ] 01. Asset ist eindeutig identifiziert.
- [ ] 02. Asset hat einen fachlichen Owner.
- [ ] 03. Asset hat einen technischen Manager.
- [ ] 04. Asset hat eine Kritikalität.
- [ ] 05. Asset hat eine Exposition.
- [ ] 06. Asset hat eine Umgebung.
- [ ] 07. Asset hat eine Datenklasse.
- [ ] 08. Asset hat Abhängigkeiten.
- [ ] 09. Asset hat Lebenszyklusstatus.
- [ ] 10. Asset hat Patch-Zyklus.
- [ ] 11. Asset hat RTO.
- [ ] 12. Asset hat RPO.
- [ ] 13. Asset ist in CMDB oder Inventar.
- [ ] 14. Asset wird regelmäßig überprüft.
- [ ] 15. Asset ist einem Service zugeordnet.
- [ ] 16. Asset ist einem Team zugeordnet.
- [ ] 17. Asset hat Kontaktweg.
- [ ] 18. Asset hat Backup-Status.
- [ ] 19. Asset hat Monitoring-Status.
- [ ] 20. Asset hat letzte Review-Datum.

### Checkliste: Detection

- [ ] 01. Netzwerk-Scan ist geplant.
- [ ] 02. Endpoint-Scan ist geplant.
- [ ] 03. Cloud-Scan ist geplant.
- [ ] 04. Container-Scan ist geplant.
- [ ] 05. Dependency-Scan ist geplant.
- [ ] 06. Web-Application-Scan ist geplant.
- [ ] 07. Scanner-Coverage ist messbar.
- [ ] 08. Scan-Frequenz ist definiert.
- [ ] 09. Scan-Ausnahmen sind dokumentiert.
- [ ] 10. Scan-Ergebnisse werden zentral gesammelt.
- [ ] 11. False-Positive-Prozess ist definiert.
- [ ] 12. Manuelle Tests sind eingeplant.
- [ ] 13. Penetrationstest-Ergebnisse werden integriert.
- [ ] 14. Hersteller-Advisories werden überwacht.
- [ ] 15. CERT-Meldungen werden geprüft.
- [ ] 16. KEV-Liste wird geprüft.
- [ ] 17. Threat Intelligence wird berücksichtigt.
- [ ] 18. Interne Meldungen sind möglich.
- [ ] 19. Scan-Zugangsdaten sind gepflegt.
- [ ] 20. Scan-Risiken für Produktion sind bewertet.

### Checkliste: Priorisierung

- [ ] 01. CVSS ist erfasst.
- [ ] 02. Asset-Kritikalität ist erfasst.
- [ ] 03. Exposition ist erfasst.
- [ ] 04. Exploit-Verfügbarkeit ist geprüft.
- [ ] 05. KEV-Relevanz ist geprüft.
- [ ] 06. Business Impact ist bewertet.
- [ ] 07. Datenbetroffenheit ist bewertet.
- [ ] 08. Benutzerinteraktion ist bewertet.
- [ ] 09. Authentifizierungsbedarf ist bewertet.
- [ ] 10. Scope-Auswirkung ist bewertet.
- [ ] 11. Kompensationsmaßnahmen sind geprüft.
- [ ] 12. Priorität ist begründet.
- [ ] 13. Frist ist gesetzt.
- [ ] 14. Owner ist gesetzt.
- [ ] 15. Entscheidung ist dokumentiert.
- [ ] 16. False Positive ist belegt.
- [ ] 17. Grouping ist geprüft.
- [ ] 18. Duplikate sind bereinigt.
- [ ] 19. Management-Relevanz ist geprüft.
- [ ] 20. Incident-Trigger ist geprüft.

### Checkliste: Remediation

- [ ] 01. Patch-Verfügbarkeit ist geprüft.
- [ ] 02. Update-Pfad ist bekannt.
- [ ] 03. Konfigurationsfix ist geprüft.
- [ ] 04. Workaround ist geprüft.
- [ ] 05. Kompensationsmaßnahme ist geprüft.
- [ ] 06. Testplan ist erstellt.
- [ ] 07. Rollback-Plan ist erstellt.
- [ ] 08. Change-Typ ist bestimmt.
- [ ] 09. Wartungsfenster ist geplant.
- [ ] 10. Kommunikation ist geplant.
- [ ] 11. Deployment ist durchgeführt.
- [ ] 12. Funktionstest ist durchgeführt.
- [ ] 13. Security-Verifikation ist durchgeführt.
- [ ] 14. Re-Scan ist durchgeführt.
- [ ] 15. Nachweis ist abgelegt.
- [ ] 16. Issue ist geschlossen.
- [ ] 17. Lessons Learned ist geprüft.
- [ ] 18. Wiederholung ist analysiert.
- [ ] 19. Architekturmaßnahme ist bewertet.
- [ ] 20. Reporting ist aktualisiert.

### Checkliste: Exception Management

- [ ] 01. Exception-ID ist vergeben.
- [ ] 02. Asset ist benannt.
- [ ] 03. Owner ist benannt.
- [ ] 04. Betroffene Regel ist benannt.
- [ ] 05. Grund ist beschrieben.
- [ ] 06. Zeitraum ist definiert.
- [ ] 07. Risiko vor Kompensation ist bewertet.
- [ ] 08. Kompensationsmaßnahmen sind beschrieben.
- [ ] 09. Restrisiko ist bewertet.
- [ ] 10. Genehmiger ist benannt.
- [ ] 11. CCSO-Prüfung ist erfolgt.
- [ ] 12. Review-Datum ist gesetzt.
- [ ] 13. Behebungsplan ist vorhanden.
- [ ] 14. Monitoring ist definiert.
- [ ] 15. Exception ist im Register.
- [ ] 16. Ablauf wird überwacht.
- [ ] 17. Erneuerung ist begründet.
- [ ] 18. Schließung ist dokumentiert.
- [ ] 19. Risikoregister ist geprüft.
- [ ] 20. Management-Sicht ist vorhanden.

### Checkliste: Incident Management

- [ ] 01. Trigger ist definiert.
- [ ] 02. Schweregrad ist klassifiziert.
- [ ] 03. Incident Commander ist benannt.
- [ ] 04. Team ist aktiviert.
- [ ] 05. Kommunikationskanal ist eingerichtet.
- [ ] 06. Beweissicherung ist gestartet.
- [ ] 07. Containment ist geplant.
- [ ] 08. Containment ist durchgeführt.
- [ ] 09. Root Cause wird analysiert.
- [ ] 10. Eradication ist geplant.
- [ ] 11. Recovery ist geplant.
- [ ] 12. Backups sind geprüft.
- [ ] 13. Meldepflicht ist geprüft.
- [ ] 14. Kundenkommunikation ist geprüft.
- [ ] 15. Management ist informiert.
- [ ] 16. Statusrhythmus ist festgelegt.
- [ ] 17. Systemintegrität ist validiert.
- [ ] 18. Lessons Learned ist geplant.
- [ ] 19. Maßnahmen sind abgeleitet.
- [ ] 20. Architekturfeedback ist dokumentiert.

### Checkliste: Change Management

- [ ] 01. Change-Typ ist bestimmt.
- [ ] 02. Risiko ist bewertet.
- [ ] 03. Security-Auswirkung ist bewertet.
- [ ] 04. Verfügbarkeitsauswirkung ist bewertet.
- [ ] 05. Integritätsauswirkung ist bewertet.
- [ ] 06. Testnachweis ist vorhanden.
- [ ] 07. Rollback ist vorhanden.
- [ ] 08. Freigabe ist erteilt.
- [ ] 09. Wartungsfenster ist abgestimmt.
- [ ] 10. Kommunikation ist abgestimmt.
- [ ] 11. Deployment-Verantwortung ist klar.
- [ ] 12. Umsetzung ist dokumentiert.
- [ ] 13. Abweichungen sind dokumentiert.
- [ ] 14. Funktion ist geprüft.
- [ ] 15. Security ist geprüft.
- [ ] 16. Change ist geschlossen.
- [ ] 17. Folgeaufgaben sind erstellt.
- [ ] 18. Emergency Change ist nachdokumentiert.
- [ ] 19. CAB-Feedback ist berücksichtigt.
- [ ] 20. Standard-Change-Kandidat ist geprüft.

### Checkliste: BC/DR

- [ ] 01. RTO ist definiert.
- [ ] 02. RPO ist definiert.
- [ ] 03. Backup-Frequenz passt zur Kritikalität.
- [ ] 04. Backup ist geschützt.
- [ ] 05. Immutable oder Offline-Kopie ist geprüft.
- [ ] 06. Restore-Test ist geplant.
- [ ] 07. Restore-Test ist durchgeführt.
- [ ] 08. Restore-Zeit ist gemessen.
- [ ] 09. Datenverlust ist gemessen.
- [ ] 10. Abhängigkeiten sind dokumentiert.
- [ ] 11. DR-Runbook ist vorhanden.
- [ ] 12. Kommunikationsplan ist vorhanden.
- [ ] 13. Tabletop-Übung ist geplant.
- [ ] 14. Simulation ist geplant.
- [ ] 15. Full Interruption ist bewertet.
- [ ] 16. Monitoring nach Recovery ist geplant.
- [ ] 17. Systemintegrität wird geprüft.
- [ ] 18. Lessons Learned ist dokumentiert.
- [ ] 19. Verbesserungen sind geplant.
- [ ] 20. Management kennt Restrisiken.

---

## Coach-Karten für den Alltag

### Coach-Karte 001: Asset Owner finden

Situation: Du musst das Thema „Asset Owner finden“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 002: Finding validieren

Situation: Du musst das Thema „Finding validieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 003: False Positive belegen

Situation: Du musst das Thema „False Positive belegen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 004: Critical Issue priorisieren

Situation: Du musst das Thema „Critical Issue priorisieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 005: KEV-Meldung bewerten

Situation: Du musst das Thema „KEV-Meldung bewerten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 006: Patch-Fenster planen

Situation: Du musst das Thema „Patch-Fenster planen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 007: Emergency Change starten

Situation: Du musst das Thema „Emergency Change starten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 008: Exception ablehnen

Situation: Du musst das Thema „Exception ablehnen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 009: Exception genehmigen

Situation: Du musst das Thema „Exception genehmigen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 010: Restrisiko erklären

Situation: Du musst das Thema „Restrisiko erklären“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 011: CAB überzeugen

Situation: Du musst das Thema „CAB überzeugen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 012: Management informieren

Situation: Du musst das Thema „Management informieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 013: Team entlasten

Situation: Du musst das Thema „Team entlasten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 014: Scanner-Coverage erhöhen

Situation: Du musst das Thema „Scanner-Coverage erhöhen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 015: SCA einführen

Situation: Du musst das Thema „SCA einführen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 016: Container-Scanning einführen

Situation: Du musst das Thema „Container-Scanning einführen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 017: Cloud Finding bewerten

Situation: Du musst das Thema „Cloud Finding bewerten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 018: EoL-System behandeln

Situation: Du musst das Thema „EoL-System behandeln“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 019: OT-System behandeln

Situation: Du musst das Thema „OT-System behandeln“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 020: Incident-Triage starten

Situation: Du musst das Thema „Incident-Triage starten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 021: Backup-Test einfordern

Situation: Du musst das Thema „Backup-Test einfordern“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 022: Report verbessern

Situation: Du musst das Thema „Report verbessern“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 023: Metrik erklären

Situation: Du musst das Thema „Metrik erklären“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 024: Root Cause analysieren

Situation: Du musst das Thema „Root Cause analysieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 025: Architekturmaßnahme ableiten

Situation: Du musst das Thema „Architekturmaßnahme ableiten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 026: Roadmap erstellen

Situation: Du musst das Thema „Roadmap erstellen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 027: Policy schreiben

Situation: Du musst das Thema „Policy schreiben“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 028: Standard Change definieren

Situation: Du musst das Thema „Standard Change definieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 029: RACI klären

Situation: Du musst das Thema „RACI klären“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 030: Governance Gate durchführen

Situation: Du musst das Thema „Governance Gate durchführen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 031: Audit vorbereiten

Situation: Du musst das Thema „Audit vorbereiten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 032: Evidence prüfen

Situation: Du musst das Thema „Evidence prüfen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 033: Überfälliges Issue eskalieren

Situation: Du musst das Thema „Überfälliges Issue eskalieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 034: Wiederholungsfehler stoppen

Situation: Du musst das Thema „Wiederholungsfehler stoppen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 035: Team schulen

Situation: Du musst das Thema „Team schulen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 036: Product Owner einbinden

Situation: Du musst das Thema „Product Owner einbinden“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 037: DevOps einbinden

Situation: Du musst das Thema „DevOps einbinden“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 038: Operations einbinden

Situation: Du musst das Thema „Operations einbinden“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 039: Legal einbinden

Situation: Du musst das Thema „Legal einbinden“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 040: Kundenkommunikation vorbereiten

Situation: Du musst das Thema „Kundenkommunikation vorbereiten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 041: RTO prüfen

Situation: Du musst das Thema „RTO prüfen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 042: RPO prüfen

Situation: Du musst das Thema „RPO prüfen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 043: Patch-Automatisierung bewerten

Situation: Du musst das Thema „Patch-Automatisierung bewerten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 044: CMDB-Qualität prüfen

Situation: Du musst das Thema „CMDB-Qualität prüfen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 045: Tool-Integration planen

Situation: Du musst das Thema „Tool-Integration planen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 046: Dashboard entwerfen

Situation: Du musst das Thema „Dashboard entwerfen“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 047: Top-Risiko formulieren

Situation: Du musst das Thema „Top-Risiko formulieren“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 048: Management-Entscheidung vorbereiten

Situation: Du musst das Thema „Management-Entscheidung vorbereiten“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 049: Migrationsplan fordern

Situation: Du musst das Thema „Migrationsplan fordern“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

### Coach-Karte 050: Budgetbedarf erklären

Situation: Du musst das Thema „Budgetbedarf erklären“ in einem realen Unternehmen sauber voranbringen.

#### Vorgehen

1. Benenne zuerst das betroffene Asset oder den betroffenen Prozess.
2. Kläre, welche Rolle verantwortlich ist.
3. Prüfe, welche Entscheidung gebraucht wird.
4. Sammle die vorhandenen Nachweise.
5. Formuliere das Risiko in einfacher Sprache.
6. Beschreibe die nächste konkrete Handlung.
7. Setze eine Frist.
8. Lege fest, wie das Ergebnis geprüft wird.
9. Dokumentiere die Entscheidung.
10. Prüfe, ob daraus eine dauerhafte Architekturverbesserung entsteht.

#### Satz, den du nutzen kannst

> „Damit wir hier nicht nur diskutieren, klären wir jetzt Asset, Owner, Risiko, Frist und Nachweis.“

#### Woran du Erfolg erkennst

- Die nächste Rolle weiß, was zu tun ist.
- Die Entscheidung ist nachvollziehbar.
- Das Risiko ist nicht versteckt.
- Der Nachweis ist definiert.
- Das Thema taucht im Reporting korrekt auf.

---

## Operating Model

### Zweck

Das Operating Model beschreibt, wie Vulnerability Management im Alltag funktioniert.

### Grundsatz

Nicht Security allein ist verantwortlich, sondern die Organisation trägt Risiko gemeinsam.

### Takt

Findings werden regelmäßig bewertet, kritische Ereignisse sofort behandelt.

### Entscheidung

Risikoentscheidungen werden durch Owner und Management getroffen, nicht stillschweigend durch Nichtstun.

### Nachweis

Jede relevante Maßnahme erzeugt einen prüfbaren Nachweis.

### Verbesserung

Wiederkehrende Ursachen werden in Architektur-, Prozess- oder Automatisierungsmaßnahmen übersetzt.

### Regelkreis

1. Erkennen
2. Bewerten
3. Priorisieren
4. Beheben
5. Verifizieren
6. Berichten
7. Lernen
8. Architektur verbessern

Dieser Regelkreis ist das Herzstück. Wenn ein Schritt schwach ist, leidet der ganze Prozess.

---

## Kommunikationsbeispiele

### An Asset Owner bei Critical Issue

```text
Betreff: Kritische Schwachstelle auf deinem Asset — Entscheidung und Umsetzung erforderlich
Hallo [Name],
auf dem Asset [Asset] wurde eine kritische Schwachstelle identifiziert.
Die Schwachstelle ist relevant, weil [kurze Begründung].
Die empfohlene Maßnahme ist [Patch/Workaround].
Die Frist ist [Datum].
Bitte bestätige den Owner, den Umsetzungsplan und mögliche Blocker bis [Datum].
Wenn die Frist nicht eingehalten werden kann, ist eine Exception mit Restrisiko-Bewertung erforderlich.
```

### An Management bei Risikoentscheidung

```text
Betreff: Entscheidung erforderlich — Restrisiko bei [Asset/Service]
Für [Asset/Service] besteht ein relevantes Sicherheitsrisiko.
Die technische Behebung ist derzeit blockiert durch [Grund].
Bis zur Behebung sind folgende kompensierende Maßnahmen aktiv: [Maßnahmen].
Das verbleibende Risiko bewerten wir als [Level].
Wir benötigen eine Entscheidung zu [Option A/B/C].
```

### An Team nach erfolgreicher Remediation

```text
Betreff: Schwachstelle geschlossen — Nachweis liegt vor
Die Schwachstelle [ID] wurde behoben.
Der Nachweis liegt vor durch [Re-Scan/Test/Log].
Das Issue wird geschlossen.
Bitte prüft zusätzlich, ob die Ursache wiederkehrend ist und ob wir eine dauerhafte Verbesserung brauchen.
```

### An CAB für Emergency Change

```text
Betreff: Emergency Change wegen kritischer Sicherheitslücke
Es liegt eine kritische Schwachstelle auf [Asset] vor.
Die Dringlichkeit ergibt sich aus [CVSS/KEV/Exploit/Exposition].
Geplante Maßnahme: [Patch/Workaround].
Rollback: [Plan].
Teststatus: [Status].
Benötigte Freigabe: [Zeitpunkt].
```

---

## Praxisanhang: 100 konkrete Prüfimpulse

### Prüfimpuls 001

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 002

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 003

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 004

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 005

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 006

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 007

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 008

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 009

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 010

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 011

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 012

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 013

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 014

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 015

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 016

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 017

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 018

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 019

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 020

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 021

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 022

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 023

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 024

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 025

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 026

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 027

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 028

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 029

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 030

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 031

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 032

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 033

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 034

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 035

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 036

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 037

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 038

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 039

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 040

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 041

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 042

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 043

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 044

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 045

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 046

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 047

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 048

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 049

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 050

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 051

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 052

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 053

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 054

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 055

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 056

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 057

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 058

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 059

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 060

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 061

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 062

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 063

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 064

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 065

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 066

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 067

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 068

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 069

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 070

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 071

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 072

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 073

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 074

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 075

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 076

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 077

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 078

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 079

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 080

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 081

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 082

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 083

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 084

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 085

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 086

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 087

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 088

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 089

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 090

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 091

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 092

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 093

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 094

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 095

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 096

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 097

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 098

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 099

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

### Prüfimpuls 100

Frage: Kann ein neuer Kollege in fünf Minuten erkennen, wer für dieses Thema verantwortlich ist?

Prüfung: Suche nach Asset, Owner, Frist, Status und Nachweis.

Korrektur: Ergänze genau die Information, die fehlt, und mache sie im Prozess verpflichtend.

Coach-Hinweis: Kleine Lücken in der Dokumentation werden in kritischen Situationen zu großen Verzögerungen.

---

## Literatur und Orientierung

| Quelle | Relevanz | Nutzung im Playbook |
|---|---|---|
| The Open Group — TOGAF Standard | Grundlage für Enterprise Architecture, ADM, Governance und Architekturartefakte. | TOGAF-Brille für Capability, ADM, Roadmap und Governance. |
| NIST SP 800-40 Rev. 4 | Enterprise Patch Management als vorbeugende Wartung und Strategie. | Patching als dauerhafte Unternehmensfähigkeit. |
| NIST SP 800-61 Rev. 2 | Computer Security Incident Handling Guide. | Incident-Response-Struktur und Lessons Learned. |
| CISA KEV Catalog | Liste aktiv ausgenutzter bekannter Schwachstellen. | Priorisierung bei realer Ausnutzung. |
| FIRST CVSS v3.1 | Bewertungssystem für technische Schwere von Schwachstellen. | CVSS-Klassen und Bewertungslogik. |
| ISO/IEC 27001 und 27002 | Informationssicherheitsmanagement und Kontrollrahmen. | Governance-, Risiko- und Kontrollperspektive. |
| BSI IT-Grundschutz | Deutscher Rahmen für Informationssicherheit. | Orientierung für Prozesse, Schutzbedarf und Maßnahmen. |
| OWASP | Web Application Security Wissen und Tools. | Web-Scanning, Anwendungssicherheit und sichere Entwicklung. |

---

## Abschluss: Die wichtigste Führungslogik

Ein starkes Vulnerability Management entsteht nicht durch Angst vor Schwachstellen.
Es entsteht durch Klarheit.
Es entsteht durch Verantwortung.
Es entsteht durch gute Daten.
Es entsteht durch funktionierende Prozesse.
Es entsteht durch realistische Fristen.
Es entsteht durch sichtbare Ausnahmen.
Es entsteht durch saubere Nachweise.
Es entsteht durch kontinuierliches Lernen.
Und es entsteht durch Architekturarbeit, die operative Probleme nicht nur repariert, sondern strukturell reduziert.

Als TOGAF-orientierter Coach denkst du deshalb immer in drei Ebenen:

1. Operativ: Was muss heute getan werden?
2. Taktisch: Wie machen wir das wiederholbar und messbar?
3. Strategisch: Welche Architekturänderung verhindert, dass dieses Problem ständig zurückkommt?

Wenn du diese drei Ebenen sauber verbindest, wird Vulnerability Management von einer Ticketlast zu einer echten Unternehmensfähigkeit.