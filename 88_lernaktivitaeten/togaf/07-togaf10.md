# TOGAF: Supply Chain Security, OT/ICS, Awareness, Compliance und Security KPIs

 

## Start-Checkliste

1. Verstehe zuerst, welche Lieferketten dein Unternehmen wirklich hat.
2. Trenne Software-Lieferkette, Hardware-Lieferkette und Dienstleister-Lieferkette.
3. Ordne jeden Lieferanten nach Kritikalität, Zugang, Daten und Abhängigkeit ein.
4. Baue SBOM nicht als Dokumentationsspielerei, sondern als Betriebsfähigkeit auf.
5. Setze SLSA, Signierung und Provenance schrittweise ein.
6. Behandle OT/ICS anders als klassische IT, weil Verfügbarkeit und Sicherheit dort anders gewichtet werden.
7. Nutze IEC 62443, Zonen und Conduits als Architektur-Sprache für industrielle Systeme.
8. Verankere Awareness nicht als jährliche Pflichtfolie, sondern als messbares Lernsystem.
9. Baue Compliance nicht als Audit-Stress auf, sondern als dauerhaftes Kontrollsystem.
10. Nutze KPIs nur, wenn daraus Entscheidungen, Eskalationen oder Verbesserungen entstehen.
11. Verbinde alle Maßnahmen mit TOGAF: Capability, Governance, Repository, Roadmap und Requirements.
12. Dokumentiere jede wichtige Ausnahme mit Risiko, Ablaufdatum, Kompensationsmaßnahme und Owner.

## Wie du dieses Playbook liest

Dieses Playbook ist wie ein Coach aufgebaut. Es erklärt nicht nur Begriffe, sondern zeigt dir, wie du arbeitest. Du bekommst klare Schritte, Beispiele, Vorlagen, Kontrollfragen und Umsetzungspläne.

Der Schwerpunkt liegt auf der Architektur-Sicht. Das bedeutet: Wir schauen nicht nur auf einzelne Tools. Wir fragen, welche Fähigkeit das Unternehmen aufbauen muss, welche Rollen beteiligt sind, welche Entscheidungen zu treffen sind und wie die Umsetzung dauerhaft gesteuert wird.

TOGAF hilft dabei, weil es Architekturarbeit in Phasen, Artefakte, Governance und Repository-Strukturen übersetzt. Supply Chain Security wird damit kein loses Security-Thema, sondern ein Teil der Enterprise Architecture.

Die Sprache bleibt bewusst einfach. Komplexität wird nicht versteckt, sondern sauber sortiert. Das Ziel ist, dass du das Thema erklären, einführen, prüfen und in echte Arbeitspakete übersetzen kannst.

## Annahmen und Grenzen

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Annahme | Dieses Playbook geht davon aus, dass ein Unternehmen Software entwickelt oder betreibt und dabei externe Bibliotheken, Container, Cloud-Dienste, Dienstleister oder Hardware-Komponenten nutzt. | Wenn ein Unternehmen nur interne Standardsoftware nutzt, wird der Supply-Chain-Teil schlanker, aber nicht überflüssig. |
| Annahme | Das Unternehmen arbeitet mit Architektur-Governance oder möchte diese einführen. | Ohne Governance bleiben SBOM, SLSA, Lieferantenbewertung und Compliance häufig unverbindlich. |
| Grenze | Dieses Playbook ersetzt keine Rechtsberatung und kein Audit durch eine akkreditierte Stelle. | Für Verträge, DSGVO, NIS2, DORA, CRA oder ISO-Zertifizierung müssen zuständige Fachrollen einbezogen werden. |
| Grenze | Tools werden als Beispiele genannt, nicht als Kaufempfehlung. | Die richtige Tool-Auswahl hängt von Bestand, Budget, Cloud-Strategie, Reifegrad und regulatorischem Kontext ab. |

## Executive Summary

Supply Chain Security bedeutet: Du schützt nicht nur deinen eigenen Code, deine eigenen Systeme und deine eigenen Mitarbeitenden. Du schützt auch alles, was von außen in dein Unternehmen hineinkommt oder Einfluss auf deine Systeme hat.

Das betrifft Open-Source-Bibliotheken, Container-Images, Build-Pipelines, Paket-Registries, Cloud-Dienste, Firmware, Hardware, externe Dienstleister, Subunternehmer und Wartungszugänge.

Der Fehler vieler Organisationen ist, dass sie Supply Chain Security erst beim Incident ernst nehmen. Dann ist es zu spät. Gute Architektur sorgt dafür, dass Risiken vorher sichtbar werden.

TOGAF hilft, weil es aus einzelnen Kontrollen eine steuerbare Fähigkeit macht: Rollen, Prinzipien, Standards, Prozesse, Roadmaps, Entscheidungsrechte, Repository und kontinuierliche Verbesserung.

Ein reifes Unternehmen kann jederzeit beantworten: Welche Komponenten nutzen wir? Welche Lieferanten sind kritisch? Welche Schwachstellen betreffen uns? Welche Builds sind vertrauenswürdig? Welche Zugriffe haben Dritte? Welche Ausnahmen laufen bald ab? Welche Kontrollen wirken wirklich?

## Teil 1: Grundverständnis

### Was bedeutet Supply Chain Security?

Supply Chain Security schützt die Lieferkette eines Unternehmens. Eine Lieferkette ist hier nicht nur der Transport von Waren. In der IT ist die Lieferkette alles, was in deine Produkte, Systeme, Datenflüsse und Betriebsprozesse hineinwirkt.

Bei Software beginnt die Lieferkette oft mit einer kleinen Abhängigkeit. Ein Entwickler fügt eine Bibliothek hinzu. Diese Bibliothek nutzt wieder andere Bibliotheken. Daraus entsteht ein Abhängigkeitsnetz. Wenn dort eine Komponente kompromittiert ist, kann dein System betroffen sein.

Bei Hardware geht es um Geräte, Firmware, Chips, Netzwerkkomponenten und Produktionswege. Ein Gerät kann technisch korrekt aussehen und trotzdem unsichere Firmware oder manipulierte Komponenten enthalten.

Bei Dienstleistern geht es um Menschen, Zugänge, Prozesse, Subunternehmer und Daten. Ein Dienstleister kann sicher wirken, aber über schwache interne Prozesse ein Einfallstor werden.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Software-Lieferkette | Risiken entstehen durch Open-Source-Bibliotheken, Paketmanager, Build-Systeme, CI/CD, Container-Images und Veröffentlichungsprozesse. | Kompromittierte Bibliothek, Typosquatting, Dependency Confusion, manipuliertes Build-Artefakt. |
| Hardware-Lieferkette | Risiken entstehen durch Geräte, Firmware, Komponenten, Transport, Austauschgeräte und Wartungsprozesse. | Gefälschte Netzwerkkomponente, unsichere Firmware, nicht geprüfter Ersatzteil-Lieferant. |
| Dienstleister-Lieferkette | Risiken entstehen durch externe Zugriffe, Datenweitergabe, Supportzugänge, Subunternehmer und Insolvenz kritischer Partner. | Wartungsdienstleister mit VPN-Zugang, Cloud-Anbieter mit Unterauftragnehmern. |
| OT/ICS-Lieferkette | Risiken entstehen durch lange Lebenszyklen, Herstellerabhängigkeiten, schwer patchbare Systeme und hohe Verfügbarkeitsanforderungen. | PLC-Firmware, SCADA-Server, Remote-Zugang eines Anlagenbauers. |

### Warum TOGAF hier nützlich ist

TOGAF zwingt dich, ein Thema nicht nur technisch zu betrachten. Du musst Business-Ziele, Informationsflüsse, Anwendungen, Technologie, Governance und Migration zusammenführen.

Supply Chain Security ist genau so ein Querschnittsthema. Es gehört nicht nur der Security-Abteilung. Es betrifft Einkauf, Legal, Architektur, Engineering, Betrieb, Produktmanagement, Compliance und Führung.

Mit TOGAF kannst du daraus eine Architecture Capability bauen. Das ist die Fähigkeit des Unternehmens, Lieferkettenrisiken systematisch zu erkennen, zu bewerten, zu steuern und dauerhaft zu verbessern.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Preliminary Phase | Hier werden Architekturprinzipien, Governance, Rollen und Repository-Strukturen festgelegt. | Prinzip: Keine produktive Software ohne bekannte Komponentenliste. |
| Phase A – Architecture Vision | Hier wird begründet, warum Supply Chain Security geschäftlich notwendig ist. | Vision: Kritische Produkte müssen nachvollziehbar, patchbar und auditierbar sein. |
| Phase B – Business Architecture | Hier werden Prozesse, Rollen, Lieferantenklassifikation und Verantwortlichkeiten beschrieben. | Supplier Due Diligence wird Teil des Beschaffungsprozesses. |
| Phase C – Data/Application Architecture | Hier werden SBOM, Abhängigkeiten, Artefakte, Repositories und Anwendungen betrachtet. | Alle produktiven Services erzeugen SBOMs im Build. |
| Phase D – Technology Architecture | Hier werden CI/CD, Signierung, Registry, Scanner, MDM, OT-Zonen und Monitoring festgelegt. | Container-Images werden signiert und vor Deployment geprüft. |
| Phase E – Opportunities & Solutions | Hier werden Lösungsbausteine und Arbeitspakete gebildet. | Einführung von SCA-Scanner, Cosign, SBOM-Repository und Lieferantenportal. |
| Phase F – Migration Planning | Hier wird die Roadmap priorisiert. | Zuerst kritische Internet-facing Systeme, danach interne Services. |
| Phase G – Implementation Governance | Hier wird geprüft, ob Projekte die Architektur einhalten. | Architecture Compliance Review vor Go-Live. |
| Phase H – Architecture Change Management | Hier wird die Fähigkeit laufend verbessert. | Neue Regulierung oder neuer Angriffstyp löst Architecture Change aus. |

## Teil 2: Architekturprinzipien für Supply Chain Security

| Aspekt | Details/Erklärung | Beispiel | Konsequenz |
| --- | --- | --- | --- |
| P-01 | Transparenz vor Vertrauen | Wir vertrauen Lieferketten nicht blind. Wir machen Komponenten, Lieferanten, Artefakte, Zugriffe und Risiken sichtbar. | Jedes produktive System braucht eine bekannte Komponentenliste und einen verantwortlichen Owner. |
| P-02 | Kein Build ohne Nachweis | Ein Build-Artefakt muss nachvollziehbar sein. Wir müssen wissen, aus welchem Code, mit welcher Pipeline und mit welchen Abhängigkeiten es entstanden ist. | Provenance wird für kritische Artefakte verpflichtend. |
| P-03 | Signieren vor Ausliefern | Artefakte, Container und Releases müssen gegen Manipulation geschützt werden. | Container-Images werden vor Deployment signiert und verifiziert. |
| P-04 | Kritikalität steuert Aufwand | Nicht jeder Lieferant und jede Komponente wird gleich behandelt. Kritische Abhängigkeiten erhalten strengere Prüfungen. | Remote-Zugriff auf OT-Systeme erfordert stärkere Kontrollen als ein Newsletter-Dienst. |
| P-05 | Ausnahmen haben Ablaufdatum | Jede Abweichung braucht Owner, Begründung, Restrisiko, Kompensationsmaßnahme und Enddatum. | Ein ungepatchtes EoL-System darf nicht ohne Exception weiterlaufen. |
| P-06 | Security ist Teil des Lieferprozesses | Kontrollen gehören in Einkauf, Entwicklung, Build, Deployment und Betrieb, nicht erst in ein späteres Audit. | SCA-Scan läuft in CI/CD und blockiert kritische Findings. |
| P-07 | OT schützt Verfügbarkeit und Sicherheit gemeinsam | In OT-Umgebungen werden Maßnahmen so geplant, dass Sicherheitsgewinn und Betriebsstabilität zusammenpassen. | Patch wird zuerst in Testumgebung geprüft und in Wartungsfenstern eingespielt. |
| P-08 | Awareness ist messbar | Schulung ist nur dann wirksam, wenn Verhalten und Wirkung gemessen werden. | Phishing-Melderate, Klickrate und Trainingsabschluss werden monatlich bewertet. |
| P-09 | Compliance braucht Evidenz | Eine Kontrolle zählt nur, wenn sie nachweisbar ist. | Audit-Evidence wird im Governance Repository abgelegt. |
| P-10 | Repository vor Einzelwissen | Architekturwissen darf nicht nur in Köpfen liegen. | Standards, Lieferantenbewertungen, SBOMs, Exceptions und Reviews liegen zentral auffindbar. |

## Teil 3: Capability Map

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Supplier Risk Management | Lieferanten erfassen, klassifizieren, bewerten und überwachen. | Supplier Register, Kritikalitätsmodell, Due-Diligence-Fragebogen. |
| Software Composition Management | Software-Komponenten identifizieren, bewerten und überwachen. | SBOM, SCA, Dependency Policy, Vulnerability Mapping. |
| Build Integrity Management | Build-Prozesse nachvollziehbar, reproduzierbar und gegen Manipulation geschützt gestalten. | SLSA-Roadmap, Provenance, gehärtete Runner, Review-Regeln. |
| Artifact Trust Management | Artefakte signieren, speichern, prüfen und nur geprüfte Artefakte deployen. | Cosign, private Registry, Admission Control. |
| Third-Party Access Management | Zugriffe von Dienstleistern auf interne Systeme steuern. | PAM, JIT Access, MFA, Session Recording. |
| OT Supply Chain Protection | OT-Lieferketten, Fernzugriff, Firmware, Zonen und Wartungsprozesse schützen. | IEC 62443, Zones and Conduits, Jump Server. |
| Awareness and Behavior Management | Mitarbeitende und Führungskräfte gezielt befähigen. | Rollenbasierte Schulungen, Phishing-Simulationen, Champion-Netzwerk. |
| Compliance Evidence Management | Kontrollen, Nachweise und Auditfähigkeit aufbauen. | Evidence Library, Kontrollmapping, Audit-Kalender. |
| Security Metrics Management | KPIs definieren, messen, steuern und verbessern. | Executive Dashboard, Trend-Analyse, Eskalationen. |
| Exception and Residual Risk Management | Abweichungen kontrolliert zulassen und überwachen. | Exception-Template, Risikoannahme, Ablaufdatum. |

### Capability-Reifegrade

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Level 1 – Ad hoc | Maßnahmen existieren zufällig. Wissen liegt in einzelnen Köpfen. Es gibt keine klare Verantwortung. | Ein Team nutzt einen Scanner, ein anderes nicht. |
| Level 2 – Wiederholbar | Einige Prozesse sind definiert, aber nicht durchgängig. Es gibt erste Standards und Templates. | Kritische Lieferanten werden bewertet, aber nicht regelmäßig. |
| Level 3 – Gesteuert | Prozesse, Rollen, Governance und KPIs sind definiert. Kontrollen sind verbindlich. | SBOM wird für alle produktiven Services erzeugt. |
| Level 4 – Messbar | Die Organisation misst Wirkung, Trends, SLA-Erfüllung und Kontrollqualität. | Patch Compliance und Supplier Risk Score werden monatlich berichtet. |
| Level 5 – Optimierend | Die Organisation verbessert sich kontinuierlich und reagiert schnell auf neue Risiken. | Neue Bedrohungen lösen automatisiert Priorisierung und Roadmap-Anpassung aus. |

## Teil 4: Rollenmodell und Verantwortlichkeiten

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Chief Information Security Officer / CCSO | Gesamtverantwortung für Sicherheitssteuerung, Risikobehandlung und Eskalation. | Genehmigt kritische Ausnahmen und berichtet an Management. |
| Enterprise Architect | Ordnet Supply Chain Security in Zielarchitektur, Standards, Roadmap und Governance ein. | Verbindet SBOM, SLSA, Lieferantenmanagement und Compliance. |
| Security Architect | Definiert Sicherheitskontrollen, Referenzarchitekturen und technische Mindeststandards. | Entwirft Signierungs- und Verifikationsarchitektur. |
| Supplier Manager / Einkauf | Steuert Lieferantenbewertung, Vertragspflichten und Nachweise. | Fordert ISO-27001-Nachweis oder Sicherheitsfragebogen an. |
| Legal / Datenschutz | Prüft Verträge, DPA, Datenschutzpflichten und Meldepflichten. | Bewertet Subunternehmer-Regelungen. |
| Engineering Lead | Setzt Vorgaben in Entwicklung und CI/CD um. | Sorgt für SCA, Lockfiles, Review-Regeln und sichere Repositories. |
| DevSecOps Engineer | Automatisiert Kontrollen in Pipelines, Deployment und Monitoring. | Implementiert SBOM-Generierung und Image-Signierung. |
| Product Owner | Priorisiert Security-Arbeit im Produktkontext. | Plant technische Schulden und Remediation in Backlogs. |
| OT Engineer | Bewertet industrielle Anlagen, Wartungsfenster und Betriebsrisiken. | Plant sichere Fernwartung mit Anlagenverantwortlichen. |
| Compliance Manager | Mappt Anforderungen auf Kontrollen und Evidenz. | Pflegt Audit-Plan und Kontrollnachweise. |
| Awareness Lead | Plant Schulungen, Kampagnen und Simulationen. | Führt Phishing-Simulationen und Lernmaßnahmen durch. |
| Service Owner | Verantwortet ein System oder einen Service über den Lebenszyklus. | Hält SBOM, Patches, Exceptions und Lieferanten aktuell. |

### RACI für zentrale Aktivitäten

| Aspekt | Responsible | Accountable | Consulted | Informed |
| --- | --- | --- | --- | --- |
| Lieferant klassifizieren | Supplier Manager | CISO / Legal | Security Architect, EA | Engineering, Service Owner |
| Sicherheitsfragebogen prüfen | Supplier Manager | CISO / Legal | Security Architect, EA | Engineering, Service Owner |
| Vertragliche Sicherheitsanforderungen festlegen | Supplier Manager | CISO / Legal | Security Architect, EA | Engineering, Service Owner |
| SBOM erzeugen | Engineering Lead / DevSecOps | Service Owner | Security Architect, EA | CISO, Compliance |
| SBOM im Repository ablegen | Engineering Lead / DevSecOps | Service Owner | Security Architect, EA | CISO, Compliance |
| Kritische Dependency patchen | Engineering Lead / DevSecOps | Service Owner | Security Architect, EA | CISO, Compliance |
| Build-Provenance erzeugen | Engineering Lead / DevSecOps | Service Owner | Security Architect, EA | CISO, Compliance |
| Container-Image signieren | Engineering Lead / DevSecOps | Service Owner | Security Architect, EA | CISO, Compliance |
| Deployment-Verifikation erzwingen | Engineering Lead / DevSecOps | Service Owner | Security Architect, EA | CISO, Compliance |
| OT-Fernzugriff genehmigen | OT Engineer | OT Asset Owner | Security Architect, CISO | EA, Compliance |
| Exception genehmigen | Service Owner | CISO / Asset Owner | EA, Security Architect, Compliance | Management |
| Awareness-Kampagne durchführen | Awareness Lead | CISO | HR, Führungskräfte | Alle Mitarbeitenden |
| Compliance-Kontrolle nachweisen | Compliance Manager | CISO | EA, Security Architect, Service Owner | Management |
| Executive Dashboard berichten | Compliance Manager | CISO | EA, Security Architect, Service Owner | Management |

## Teil 5: Lieferanten-Sicherheitsbewertung

Lieferanten-Sicherheitsbewertung beginnt nicht mit einem Fragebogen. Sie beginnt mit einer einfachen Frage: Wie stark kann dieser Lieferant unserem Geschäft schaden, wenn bei ihm etwas schiefgeht?

Diese Frage muss vor dem Einkauf, vor dem Vertrag, vor dem technischen Zugang und vor produktiver Nutzung beantwortet werden.

Ein guter Bewertungsprozess ist risikobasiert. Er prüft kritische Lieferanten streng und einfache Lieferanten schlank. Dadurch bleibt der Prozess akzeptiert und wirksam.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Kritikalität hoch | Lieferant ist geschäftskritisch, verarbeitet sensible Daten oder hat privilegierten Zugriff. | Cloud-Plattform, Zahlungsdienstleister, OT-Wartungsdienstleister. |
| Kritikalität mittel | Lieferant unterstützt wichtige Prozesse, aber Ausfall oder Kompromittierung wäre begrenzt beherrschbar. | HR-Tool, Monitoring-Dienst, Marketing-Plattform. |
| Kritikalität niedrig | Lieferant hat keine kritischen Daten, keinen produktiven Zugriff und geringe Geschäftsabhängigkeit. | Öffentliches Newsletter-Tool ohne sensible Daten. |

### Bewertungsprozess Schritt für Schritt

| Aspekt | Schritt | Details/Erklärung |
| --- | --- | --- |
| 1 | Lieferant erfassen | Name, Leistung, Owner, Vertrag, Datenarten, Systeme, Zugang und Subunternehmer dokumentieren. |
| 2 | Kritikalität bestimmen | Bewerte Daten, Zugriff, Geschäftsauswirkung, Austauschbarkeit und regulatorische Relevanz. |
| 3 | Due Diligence durchführen | Fragebogen, Zertifikate, Pentest-Berichte, Kontrollnachweise oder Audit prüfen. |
| 4 | Risiko bewerten | Initiales Risiko bestimmen und mit vorhandenen Kontrollen vergleichen. |
| 5 | Vertraglich absichern | Sicherheitsanforderungen, Meldepflichten, Audit-Rechte, Subunternehmer und Exit regeln. |
| 6 | Onboarding steuern | Technische Zugänge, MFA, Rollen, Logging, Datenflüsse und Ansprechpartner einrichten. |
| 7 | Laufend überwachen | Zertifikate erneuern, Incidents prüfen, Risiko neu bewerten, Änderungen erfassen. |
| 8 | Offboarding durchführen | Zugänge entziehen, Datenrückgabe oder Löschung prüfen, Nachweise sichern. |

### Lieferanten-Scorecard

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Datenzugriff | Welche Daten sieht oder verarbeitet der Lieferant? | 0 = keine, 5 = hoch sensible oder umfangreiche personenbezogene Daten. |
| Systemzugriff | Welche technischen Zugänge hat der Lieferant? | 0 = kein Zugriff, 5 = privilegierter produktiver Zugriff. |
| Geschäftsabhängigkeit | Wie kritisch ist der Lieferant für den Geschäftsbetrieb? | 0 = leicht ersetzbar, 5 = Ausfall stoppt Kernprozess. |
| Subunternehmer | Wie transparent und kontrolliert sind Unterauftragnehmer? | 0 = keine, 5 = viele oder unklare Subunternehmer. |
| Sicherheitsnachweise | Welche Zertifizierungen oder Reports liegen vor? | 0 = starke Nachweise, 5 = keine verwertbaren Nachweise. |
| Incident-Reife | Wie gut meldet und behandelt der Lieferant Vorfälle? | 0 = klare SLAs und Nachweise, 5 = unklare Prozesse. |
| Exit-Fähigkeit | Wie leicht kann das Unternehmen den Lieferanten wechseln? | 0 = leicht, 5 = hoher Lock-in. |

### Fragen für Lieferantenbewertung

- Welche konkrete Leistung erbringt der Lieferant?
- Welcher interne Owner ist fachlich verantwortlich?
- Welcher technische Owner ist verantwortlich?
- Welche Daten werden verarbeitet?
- Werden personenbezogene Daten verarbeitet?
- Werden vertrauliche Unternehmensdaten verarbeitet?
- Hat der Lieferant Netzwerkzugang?
- Hat der Lieferant administrativen Zugang?
- Hat der Lieferant Zugriff auf Produktionssysteme?
- Hat der Lieferant Zugriff auf OT-Systeme?
- Kann der Lieferant Daten exportieren?
- Welche Subunternehmer werden eingesetzt?
- Wo findet Verarbeitung statt?
- Welche Zertifizierungen liegen vor?
- Wann laufen Zertifizierungen ab?
- Gibt es einen aktuellen Pentest-Bericht?
- Gibt es eine Incident-Meldepflicht?
- Welche Meldefrist gilt?
- Gibt es Audit-Rechte?
- Wie wird Vertragsende technisch abgewickelt?
- Wie werden Zugänge entzogen?
- Wie wird Datenlöschung nachgewiesen?
- Was passiert bei Insolvenz?
- Welche Alternativen gibt es?
- Welche Exit-Kosten entstehen?
- Welche Risiken bleiben nach allen Kontrollen übrig?

## Teil 6: SBOM als Architektur-Fähigkeit

SBOM bedeutet Software Bill of Materials. Es ist eine Stückliste für Software. Sie zeigt, welche Komponenten, Bibliotheken, Versionen und Abhängigkeiten in einer Software enthalten sind.

Der Wert einer SBOM zeigt sich im Ernstfall. Wenn eine kritische Schwachstelle veröffentlicht wird, musst du schnell wissen: Sind wir betroffen? Wo? In welcher Version? In welchem Produkt? Bei welchem Kunden?

Eine SBOM ist nicht nur ein Dokument. Sie ist Teil eines Prozesses. Sie muss erzeugt, gespeichert, ausgewertet, aktualisiert, mit Schwachstellen abgeglichen und im Betrieb genutzt werden.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Format | SPDX und CycloneDX sind verbreitete Formate. CycloneDX wird häufig im Security-Kontext genutzt. | JSON-SBOM pro Build-Artefakt. |
| Inhalt | Name, Version, Hersteller, Lizenz, Hash, Abhängigkeiten, Erzeugungsdatum und Tool. | log4j-core 2.x mit SHA-256 und Lizenzangabe. |
| Erzeugung | SBOM sollte automatisiert in der Build-Pipeline entstehen. | Maven-Build erzeugt CycloneDX-Datei. |
| Ablage | SBOM muss zentral auffindbar sein und zum Artefakt passen. | SBOM wird im Artefakt-Repository oder Security-Repository abgelegt. |
| Nutzung | SBOM wird mit CVE-Daten, Lizenzregeln und Kundenanforderungen abgeglichen. | Log4Shell-Abfrage über alle Produkte. |
| Governance | Es braucht Pflicht, Owner, Aktualität, Qualität und Review. | Kein Release ohne SBOM für kritische Services. |

### SBOM-Einführung Schritt für Schritt

| Aspekt | Schritt | Details/Erklärung |
| --- | --- | --- |
| Phase 1 | Pilot auswählen | Wähle 2 bis 3 kritische Services, möglichst unterschiedliche Tech-Stacks. |
| Phase 2 | Toolchain festlegen | Entscheide Format, Generator, Ablageort und Schwachstellenabgleich. |
| Phase 3 | Pipeline integrieren | SBOM wird automatisch bei jedem Release erzeugt. |
| Phase 4 | Qualität prüfen | Prüfe Vollständigkeit, Versionsangaben, Komponentenbeziehungen und Wiederholbarkeit. |
| Phase 5 | Repository anbinden | SBOMs werden zentral gespeichert, versioniert und auffindbar. |
| Phase 6 | Vulnerability Matching | SBOM wird automatisch gegen CVE- und Advisory-Daten geprüft. |
| Phase 7 | Release Gate setzen | Für kritische Produkte wird SBOM vor Release verpflichtend. |
| Phase 8 | Reporting aufbauen | Management sieht Abdeckung, kritische Komponenten und Reaktionszeit. |

### SBOM-Qualitätskriterien

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Vollständigkeit | Alle direkten und transitiven Abhängigkeiten sind enthalten. | Nicht nur pom.xml, sondern effektiver Dependency Tree. |
| Eindeutigkeit | Komponenten sind eindeutig identifizierbar. | Package URL oder CPE, wenn sinnvoll. |
| Versionstreue | Die SBOM beschreibt exakt das gebaute Artefakt. | Nicht Wunschstand, sondern Build-Ergebnis. |
| Reproduzierbarkeit | Wiederholter Build erzeugt vergleichbare Metadaten. | Gleiche Commit-Version, gleiche Abhängigkeiten. |
| Aktualität | SBOM ist zum Release-Zeitpunkt erzeugt. | Keine manuell gepflegte Altdatei. |
| Verknüpfbarkeit | SBOM ist mit Artefakt, Version, Repository und Service verknüpft. | Service A Version 1.4.2 hat SBOM X. |
| Auswertbarkeit | Security-Tools können die SBOM maschinell lesen. | JSON/XML im Standardformat. |

## Teil 7: Secure Software Supply Chain

Eine sichere Software-Lieferkette schützt den Weg von der Idee bis zum produktiven Artefakt. Sie betrachtet Code, Abhängigkeiten, Entwicklerzugänge, Repositories, Build-Systeme, Artefakte, Signaturen und Deployments.

Der zentrale Gedanke ist: Ein Artefakt soll nicht einfach deshalb vertrauenswürdig sein, weil es im richtigen Ordner liegt. Es soll nachweisbar aus dem richtigen Code, mit der richtigen Pipeline und den richtigen Kontrollen entstanden sein.

Hier helfen SLSA, Signierung, Provenance, in-toto, private Repositories, Lockfiles, Review-Regeln und Deployment-Gates.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| SLSA Level 1 | Build-Prozess ist dokumentiert. Erste Provenance ist vorhanden. | Build erzeugt nachvollziehbare Metadaten. |
| SLSA Level 2 | Versionskontrolle und authentifizierte Provenance werden genutzt. | Builds sind mit Repository und Commit verknüpft. |
| SLSA Level 3 | Build-Umgebung ist gehärtet und Manipulation ist deutlich schwerer. | Isolierte Runner, kontrollierte Identitäten, Schutz vor Manipulation. |
| SLSA Level 4 | Strenge Anforderungen wie zwei Personen Review und hermetische Builds. | Kritische Komponenten mit höchster Vertrauensstufe. |

### Secure Build Pipeline als Referenzablauf

1. Entwickler erstellt Änderung in einem Branch.
2. Änderung wird durch mindestens eine weitere Person geprüft.
3. Pipeline startet nur aus vertrauenswürdigem Repository.
4. Abhängigkeiten werden aus zugelassenen Quellen geladen.
5. Lockfiles oder vergleichbare Mechanismen fixieren Versionen.
6. SCA-Scan prüft bekannte Schwachstellen.
7. SAST prüft Quellcode auf typische Fehler.
8. Secrets-Scan verhindert versehentliche Zugangsdaten im Code.
9. Tests prüfen Funktion, Sicherheit und Regression.
10. Build erzeugt Artefakt.
11. SBOM wird automatisch erzeugt.
12. Provenance wird erzeugt.
13. Artefakt wird signiert.
14. Artefakt wird in ein geschütztes Repository geladen.
15. Deployment prüft Signatur und Policy.
16. Monitoring prüft Laufzeit und neue Schwachstellen.

### Paketmanager-Sicherheit

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Maven / Java | Nutze private Repositories, Dependency-Check oder SCA, Version-Pinning, kontrollierte Mirrors und klare Update-Regeln. | Keine zufälligen Snapshot-Abhängigkeiten in Produktion. |
| npm / JavaScript | Nutze Lockfiles, Registry-Konfiguration, Audit, Paket-Reputation und eingeschränkte Publish-Rechte. | Schutz vor Typosquatting und Dependency Confusion. |
| pip / Python | Nutze Hash-Verifikation, private Mirrors, pip-audit und klare Anforderungen an requirements-Dateien. | Keine unfixierten Wildcard-Versionen für kritische Services. |
| NuGet / .NET | Nutze packages.lock.json, private Feeds, NuGet Audit und Signaturprüfung, soweit passend. | Keine ungeprüften externen Feeds in CI. |
| Container | Nutze minimalistische Base Images, regelmäßigen Rebuild, Trivy/Grype, Signierung und Admission Controls. | Kein Deployment unsignierter Images. |

### Policy: Was im Engineering verbindlich sein sollte

- Jede produktive Anwendung hat einen technischen Owner.
- Jede produktive Anwendung hat ein bekanntes Repository.
- Jede produktive Anwendung nutzt definierte Paketquellen.
- Jede produktive Anwendung erzeugt bei Release eine SBOM.
- Kritische und hohe Schwachstellen blockieren Releases nach definierter Policy.
- Jede Exception braucht Ablaufdatum und Owner.
- Produktive Artefakte werden aus CI/CD erzeugt, nicht lokal von Entwicklergeräten.
- Produktive Artefakte werden signiert.
- Deployments prüfen Signatur und Herkunft.
- Build-Systeme haben minimale Rechte.
- Secrets dürfen nicht im Repository liegen.
- Zugriffe auf Paketregistries werden protokolliert.
- Kritische Änderungen benötigen Review.
- CI/CD-Runner werden regelmäßig aktualisiert.
- Abhängigkeiten werden regelmäßig gepflegt, nicht nur im Notfall.

## Teil 8: OT/ICS/SCADA-Sicherheit

OT steht für Operational Technology. Dazu gehören industrielle Steuerungen, Anlagen, Maschinen, Sensoren, SCADA-Systeme und Produktionsnetze.

OT ist nicht einfach IT mit älterer Hardware. OT hat andere Prioritäten. Ein Patch, der in der IT normal ist, kann in der Produktion einen Anlagenstillstand verursachen. Deshalb braucht OT eigene Architekturentscheidungen.

IEC 62443 ist eine wichtige Normenreihe für industrielle Cybersicherheit. Besonders wichtig sind Security Levels sowie das Denken in Zonen und Conduits.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Security Level 1 | Schutz vor unbeabsichtigten oder einfachen Verstößen. | Grundschutz für wenig kritische Zonen. |
| Security Level 2 | Schutz vor absichtlichen Angriffen mit einfachen Mitteln. | Typisch für viele industrielle Umgebungen als Mindestziel. |
| Security Level 3 | Schutz vor Angriffen mit anspruchsvolleren Mitteln. | Für kritische Produktionsbereiche oder hohe Schadenswirkung. |
| Security Level 4 | Schutz vor sehr starken Angreifern mit hoher Kompetenz und Ressourcen. | Für besonders kritische Infrastrukturen. |

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Zone | Gruppe von Assets mit vergleichbarem Schutzbedarf. | Produktionszelle, SCADA-Zone, Engineering-Workstation-Zone. |
| Conduit | Kontrollierter Kommunikationspfad zwischen Zonen. | Firewall-Regel zwischen IT und OT-DMZ. |
| Purdue Model | Referenz zur Strukturierung von IT/OT-Ebenen. | Trennung zwischen Unternehmens-IT, DMZ, Steuerung und Feldebene. |
| Data Diode | Unidirektionaler Datenfluss, wenn Rückkanal vermieden werden muss. | Produktionsdaten gehen nach außen, Befehle nicht zurück. |
| Jump Server | Kontrollierter Zugangspunkt für Wartung oder Administration. | MFA, Session Recording und Freigabeprozess. |
| Application Whitelisting | Nur zugelassene Programme dürfen laufen. | Für stabile OT-Workstations oft besser als klassische Blacklist. |

### OT-Fernzugriff als Architekturentscheidung

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Minimale Exposition | Nur notwendige Systeme dürfen erreichbar sein. | Kein direkter VPN-Zugriff in die Steuerungszone. |
| Starke Authentifizierung | MFA ist Pflicht für externe und privilegierte Zugriffe. | Techniker nutzt MFA vor Jump Server. |
| Zeitliche Begrenzung | Zugang gilt nur für ein freigegebenes Wartungsfenster. | Zugriff aktiv von 10:00 bis 12:00 Uhr. |
| Genehmigung | Jeder Zugriff braucht dokumentierte Freigabe. | Asset Owner genehmigt Wartungszugriff. |
| Protokollierung | Sitzungen werden aufgezeichnet oder detailliert geloggt. | Session Recording auf Jump Server. |
| Sofortige Deaktivierung | Nach Abschluss wird Zugang entzogen. | JIT-Zugriff statt dauerhaftes Konto. |

### OT-Checkliste für Architekten

- Welche Anlagen sind im Scope?
- Welche Systeme sind sicherheits- oder produktionskritisch?
- Welche Zonen existieren heute?
- Welche Kommunikationspfade existieren zwischen IT und OT?
- Welche Dienstleister haben Zugriff?
- Welche Fernwartungszugänge existieren?
- Welche Systeme sind EoL?
- Welche Systeme können nicht gepatcht werden?
- Welche Kompensationsmaßnahmen existieren?
- Welche Backups sind getestet?
- Welche Wiederanlaufzeit ist akzeptabel?
- Welche Notfallprozesse sind geübt?
- Welche Logs werden gesammelt?
- Welche OT-spezifischen Detection-Regeln existieren?
- Welche Produktionsrisiken entstehen durch Sicherheitsmaßnahmen?

## Teil 9: Security Awareness und Training

Awareness ist nicht der Versuch, Menschen perfekt zu machen. Awareness ist der Aufbau eines besseren Sicherheitsverhaltens im Alltag.

Ein gutes Programm unterscheidet Zielgruppen. Entwickler brauchen andere Inhalte als Führungskräfte. Admins brauchen andere Inhalte als normale Anwender. OT-Personal braucht andere Inhalte als Sales.

Awareness muss freundlich, klar, wiederholbar und messbar sein. Bestrafung erzeugt Schweigen. Lernen erzeugt bessere Meldungen.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Alle Mitarbeitenden | Basiswissen zu Phishing, Passwörtern, MFA, Daten, Homeoffice und Meldewegen. | Jährliches Basistraining plus kurze Lernimpulse. |
| Entwickler | Secure Coding, Dependency Risks, Secrets, SBOM, Reviews und CI/CD-Sicherheit. | Quartalsweise technische Sessions. |
| Admins | Privilegierte Zugriffe, PAM, Logging, Hardening, Incident Response. | Praxisübungen und Zugriffskontrollen. |
| Führungskräfte | Risikoverständnis, Vorbildverhalten, Entscheidungswege und Meldekultur. | Executive Briefing und Tabletop-Übungen. |
| OT-Personal | Fernwartung, USB-Risiken, Anlagenverfügbarkeit, Incident-Wege. | Schichttaugliche Kurztrainings. |
| Einkauf / Supplier Management | Lieferantenrisiken, Sicherheitsfragen, Vertragsklauseln und Nachweise. | Checklisten und Due-Diligence-Training. |

### Phishing-Simulation als Lernsystem

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Planung | Simulationen werden regelmäßig geplant und nach Schwierigkeitsgrad gesteigert. | Quartalsweise Kampagne. |
| Durchführung | Keine echte Gefährdung, sondern sichere Lernumgebung. | Klick führt auf Aufklärungsseite. |
| Messung | Klickrate, Melderate, Wiederholer und Trend werden gemessen. | Ziel: niedrige Klickrate, hohe Melderate. |
| Nachbereitung | Direktes, freundliches Lernen statt Bloßstellung. | Kurzes Micro-Learning nach Klick. |
| Verbesserung | Ergebnisse verbessern Inhalte und Kommunikation. | Mehr Training bei Spear-Phishing-Schwächen. |

## Teil 10: Compliance und Audit

Compliance bedeutet nicht, dass ein Unternehmen Papier produziert. Compliance bedeutet, dass Anforderungen verstanden, auf Kontrollen übersetzt, umgesetzt, gemessen und nachgewiesen werden.

Ein gutes Compliance-System beantwortet drei Fragen: Was gilt für uns? Welche Kontrollen erfüllen das? Wo liegt der Nachweis?

TOGAF hilft, weil Anforderungen in Architecture Requirements, Standards Library, Governance Repository und Architecture Roadmap übersetzt werden können.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| DSGVO / GDPR | Schutz personenbezogener Daten, Betroffenenrechte, Datenpannen, Verträge und Nachweise. | DPA mit Auftragsverarbeitern, 72h-Meldebewertung. |
| NIS2 | Risikomanagement, Incident-Meldung, Business Continuity, Supply Chain Security und Schulung. | 24h Frühwarnung und 72h Meldung für relevante Vorfälle. |
| ISO/IEC 27001 | ISMS-Anforderungen, Risikomanagement, Kontrollen und Zertifizierung. | Scope, Statement of Applicability, interne Audits. |
| CRA | Security-Anforderungen für Produkte mit digitalen Elementen. | Schwachstellenmanagement und Transparenz über Komponenten. |
| DORA | Digitale Resilienz im Finanzsektor. | ICT-Risikomanagement und Drittparteienkontrolle. |
| TISAX | Informationssicherheit in der Automobilindustrie. | Supplier Assessment und Nachweispflichten. |
| PCI DSS | Schutz von Zahlungskartendaten. | Netzwerksegmentierung, Logging, Verschlüsselung. |

### Compliance-Mapping als Arbeitsmethode

| Aspekt | Schritt | Details/Erklärung |
| --- | --- | --- |
| 1 | Anforderung erfassen | Regel, Vertrag, Standard oder Kundenvorgabe eindeutig dokumentieren. |
| 2 | Kontrolle definieren | Beschreiben, welche Maßnahme die Anforderung erfüllt. |
| 3 | Owner benennen | Eine Rolle ist für Umsetzung und Nachweis verantwortlich. |
| 4 | Evidenz festlegen | Definieren, welcher Nachweis ausreichend ist. |
| 5 | Frequenz bestimmen | Kontrolle ist einmalig, monatlich, quartalsweise oder jährlich nachzuweisen. |
| 6 | Tool oder Ablage bestimmen | Nachweise liegen im Governance Repository. |
| 7 | Gap bewerten | Fehlt die Kontrolle, entsteht ein Gap mit Priorität. |
| 8 | Roadmap ableiten | Gaps werden in Maßnahmen und Arbeitspakete übersetzt. |

## Teil 11: KPIs und Security Dashboard

KPIs sind nur dann sinnvoll, wenn sie zu Entscheidungen führen. Eine Zahl ohne Konsequenz ist Dekoration.

Gute Security-KPIs zeigen Zustand, Trend, Risiko und Handlungsbedarf. Sie sind verständlich genug für Management und konkret genug für Umsetzungsteams.

TOGAF betrachtet KPIs als Teil von Performance Management der Architecture Capability. Es geht nicht darum, Teams zu beschämen. Es geht darum, das System steuerbar zu machen.

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Patch Compliance Rate | Anteil der Systeme, die innerhalb der Frist gepatcht wurden. | Ziel: >95 Prozent für kritische Patches. |
| Mean Time to Remediate | Durchschnittliche Zeit bis zur Behebung einer Schwachstelle. | Nach Kritikalität getrennt messen. |
| Overdue Findings Rate | Anteil überfälliger Findings. | Ziel: unter 5 Prozent. |
| SBOM Coverage | Anteil produktiver Services mit aktueller SBOM. | Ziel: 100 Prozent für kritische Services. |
| Signed Artifact Rate | Anteil produktiver Artefakte mit gültiger Signatur. | Ziel: steigende Abdeckung bis Pflicht. |
| Supplier Review Coverage | Anteil kritischer Lieferanten mit aktueller Bewertung. | Ziel: 100 Prozent. |
| Exception Aging | Alter aktiver Ausnahmen. | Keine kritische Exception ohne Review. |
| Phishing Click Rate | Anteil der Klicks in Simulationen. | Trend muss sinken. |
| Phishing Report Rate | Anteil korrekt gemeldeter Simulationen. | Trend muss steigen. |
| Access Review Completion | Anteil abgeschlossener Zugriffsreviews. | Ziel: 100 Prozent für privilegierte Konten. |
| OT Remote Access Sessions | Anzahl und Dauer externer OT-Zugriffe. | Jede Sitzung mit Genehmigung und Logging. |
| Compliance Evidence Completeness | Anteil vollständiger Nachweise pro Kontrollbereich. | Audit-Vorbereitung ohne Panik. |

### Executive Dashboard Aufbau

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Gesamtrisiko | Ampelstatus mit Begründung und Trend. | Mittel, weil kritische Lieferantenreviews überfällig sind. |
| Top 5 Risiken | Die wichtigsten Risiken mit Owner und nächstem Schritt. | EoL-OT-Systeme, fehlende MFA, ungeklärte Subunternehmer. |
| Kontrollabdeckung | Abdeckung bei SBOM, Signierung, Supplier Reviews, Patch Compliance. | SBOM 62 Prozent, Signierung 35 Prozent. |
| Ausnahmen | Anzahl, Kritikalität, Alter und Ablaufdatum. | 3 kritische Exceptions laufen in 14 Tagen ab. |
| Incidents und Findings | Neue, geschlossene, überfällige und wiederkehrende Fälle. | Overdue Findings sinken um 12 Prozent. |
| Entscheidungsbedarf | Konkrete Management-Entscheidungen. | Budget für SBOM-Repository und OT-Jump-Server. |

## Teil 12: TOGAF-Artefakte

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Architecture Principle | Grundregel für Architekturentscheidungen. | Keine produktive Software ohne bekannte Komponentenliste. |
| Architecture Requirement | Konkrete Anforderung an die Zielarchitektur. | Jeder kritische Service muss SBOM im CI/CD erzeugen. |
| Architecture Decision Record | Dokumentierte Architekturentscheidung. | Wir nutzen CycloneDX als primäres SBOM-Format. |
| Standard | Verbindliche technische oder organisatorische Regel. | Container-Images müssen signiert werden. |
| Reference Architecture | Wiederverwendbares Zielbild. | Secure Build Pipeline mit Signierung und SBOM. |
| Risk Register Entry | Dokumentiertes Risiko mit Behandlung. | Unbewertete Subunternehmer bei kritischem Anbieter. |
| Exception Record | Dokumentierte Abweichung mit Ablaufdatum. | EoL-System bis Migration mit Netzwerkisolation. |
| Compliance Evidence | Nachweis für Kontrolle. | Audit-Log, Report, Screenshot, Vertragsanlage. |
| Roadmap Item | Umsetzungsbaustein in der Migrationsplanung. | Q3: Einführung private Package Registry. |
| Capability Assessment | Bewertung des Reifegrads. | SLSA aktuell Level 1, Ziel Level 3 für kritische Services. |

### Architecture Repository Struktur

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Standards Library | Technische Standards, Policy, Mindestanforderungen. | Dependency Policy, SBOM Standard, Supplier Security Standard. |
| Governance Repository | Reviews, Entscheidungen, Exceptions, Compliance-Nachweise. | Architecture Compliance Review für Produkt A. |
| Architecture Requirements Repository | Alle Architektur- und Sicherheitsanforderungen. | REQ-SC-017: Signaturprüfung vor Deployment. |
| Solutions Landscape | Lösungen, Tools und Plattformen. | SCA-Tool, SBOM-Repository, PAM-System. |
| Architecture Landscape | Ist- und Zielarchitektur. | Aktuelle CI/CD-Landschaft und Zielpipeline. |
| Reference Library | Templates, Checklisten, Beispiele. | Supplier Assessment Template, RACI, Workshop-Agenda. |

## Teil 13: 30-60-90-Tage-Plan

| Aspekt | Ziel | Details/Erklärung |
| --- | --- | --- |
| Tag 1-30 | Transparenz schaffen | Scope definieren, kritische Lieferanten erfassen, Top-Services identifizieren, erste SBOM-Piloten starten, OT-Fernzugriffe inventarisieren. |
| Tag 31-60 | Standards und Governance aufsetzen | Prinzipien beschließen, RACI festlegen, Supplier-Klassifikation verbindlich machen, SBOM-Standard definieren, erste Pipeline-Kontrollen aktivieren. |
| Tag 61-90 | Pilot in Betrieb bringen | SBOM für kritische Services, SCA-Gates, Signierungspilot, Supplier Reviews für kritische Lieferanten, Dashboard v1. |
| Tag 91-180 | Skalieren | Abdeckung erhöhen, Exception Management einführen, Compliance-Mapping, OT-Remote-Access-Zielarchitektur umsetzen. |
| Tag 181-365 | Optimieren | SLSA-Reife erhöhen, automatisierte Verifikation, regelmäßige Lieferantenreviews, Awareness-System, Executive Dashboard und Auditfähigkeit stabilisieren. |

## Teil 14: Konkrete Templates

### Template: Supplier Security Assessment

```text
Lieferant: [ausfüllen]
Interner Owner: [ausfüllen]
Leistung: [ausfüllen]
Kritikalität: [ausfüllen]
Datenkategorien: [ausfüllen]
Systemzugänge: [ausfüllen]
Produktionszugriff: [ausfüllen]
OT-Zugriff: [ausfüllen]
Subunternehmer: [ausfüllen]
Zertifizierungen: [ausfüllen]
Letzter Sicherheitsnachweis: [ausfüllen]
Incident-Meldefrist: [ausfüllen]
Audit-Rechte: [ausfüllen]
Exit-Regelung: [ausfüllen]
Initiales Risiko: [ausfüllen]
Kompensationsmaßnahmen: [ausfüllen]
Restrisiko: [ausfüllen]
Nächster Review: [ausfüllen]
Entscheidung: [ausfüllen]
```

### Template: SBOM Policy

```text
Scope: [ausfüllen]
Pflicht für Produktgruppen: [ausfüllen]
Format: [ausfüllen]
Erzeugungszeitpunkt: [ausfüllen]
Ablageort: [ausfüllen]
Verknüpfung mit Artefakt: [ausfüllen]
Vulnerability Matching: [ausfüllen]
Release-Gate: [ausfüllen]
Exception-Prozess: [ausfüllen]
Owner: [ausfüllen]
Reporting: [ausfüllen]
```

### Template: SLSA Roadmap

```text
Aktueller Reifegrad: [ausfüllen]
Ziel-Reifegrad: [ausfüllen]
Kritische Artefakte: [ausfüllen]
Build-System: [ausfüllen]
Repository-Schutz: [ausfüllen]
Review-Regel: [ausfüllen]
Provenance-Erzeugung: [ausfüllen]
Signierung: [ausfüllen]
Deployment-Verifikation: [ausfüllen]
Offene Gaps: [ausfüllen]
Roadmap: [ausfüllen]
```

### Template: OT Remote Access Request

```text
Anlage: [ausfüllen]
Zone: [ausfüllen]
System: [ausfüllen]
Dienstleister: [ausfüllen]
Grund des Zugriffs: [ausfüllen]
Wartungsfenster: [ausfüllen]
Genehmigender Asset Owner: [ausfüllen]
MFA bestätigt: [ausfüllen]
Session Recording aktiv: [ausfüllen]
Firewall-Regel: [ausfüllen]
Notfallkontakt: [ausfüllen]
Deaktivierungszeitpunkt: [ausfüllen]
Nachweis: [ausfüllen]
```

### Template: Exception Record

```text
Exception-ID: [ausfüllen]
Betroffener Standard: [ausfüllen]
Betroffenes System: [ausfüllen]
Owner: [ausfüllen]
Grund: [ausfüllen]
Initiales Risiko: [ausfüllen]
Kompensationsmaßnahme: [ausfüllen]
Restrisiko: [ausfüllen]
Gültig von: [ausfüllen]
Gültig bis: [ausfüllen]
Review-Termin: [ausfüllen]
Genehmigung: [ausfüllen]
```

### Template: Compliance Evidence Record

```text
Kontrolle: [ausfüllen]
Framework: [ausfüllen]
Anforderung: [ausfüllen]
Owner: [ausfüllen]
Nachweistyp: [ausfüllen]
Nachweisort: [ausfüllen]
Prüffrequenz: [ausfüllen]
Letzte Prüfung: [ausfüllen]
Ergebnis: [ausfüllen]
Offene Maßnahmen: [ausfüllen]
```

## Teil 15: Workshop-Designs

### Workshop 1: Supply Chain Scope

- Ziel ist ein gemeinsames Bild aller Lieferketten.
- Teilnehmer: EA, Security, Einkauf, Legal, Engineering, Betrieb.
- Output: Scope, Top-Risiken, priorisierte Domänen.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

### Workshop 2: Kritische Lieferanten

- Ziel ist die Klassifikation der wichtigsten Lieferanten.
- Teilnehmer: Einkauf, Service Owner, CISO, Legal.
- Output: Supplier Register, Kritikalität, Review-Plan.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

### Workshop 3: SBOM und SCA

- Ziel ist ein umsetzbarer Pipeline-Pilot.
- Teilnehmer: Engineering, DevSecOps, Security Architect.
- Output: Toolchain, Pilotservices, Gate-Regeln.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

### Workshop 4: Secure Build

- Ziel ist ein Zielbild für vertrauenswürdige Artefakte.
- Teilnehmer: DevSecOps, Platform Team, EA, Security.
- Output: SLSA-Ziel, Signierung, Provenance, Roadmap.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

### Workshop 5: OT-Fernzugriff

- Ziel ist sichere Wartung ohne unnötige Exposition.
- Teilnehmer: OT, IT, Security, Anlagenverantwortliche, Dienstleistersteuerung.
- Output: Zielarchitektur, Prozesse, Controls.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

### Workshop 6: Compliance Mapping

- Ziel ist die Übersetzung von Anforderungen in Kontrollen.
- Teilnehmer: Compliance, Security, EA, Service Owner.
- Output: Kontrollmatrix, Evidence Plan, Gaps.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

### Workshop 7: KPI Dashboard

- Ziel ist ein steuerbares Management-Dashboard.
- Teilnehmer: CISO, EA, Compliance, Management, Service Owner.
- Output: KPI-Set, Datenquellen, Reporting-Frequenz.
- Agenda:
  - 00:00–00:10 Ziel und Scope klären
  - 00:10–00:30 Ist-Zustand sammeln
  - 00:30–01:00 Risiken und Abhängigkeiten bewerten
  - 01:00–01:30 Zielbild und Mindeststandard formulieren
  - 01:30–01:50 Maßnahmen und Owner festlegen
  - 01:50–02:00 Entscheidungen und nächste Schritte sichern

## Teil 16: Fehlerbilder und Korrekturen

| Aspekt | Details/Erklärung | Korrektur |
| --- | --- | --- |
| SBOM wird manuell gepflegt | Die Liste ist schnell veraltet und nicht vertrauenswürdig. | SBOM automatisiert im Build erzeugen. |
| Alle Lieferanten werden gleich geprüft | Der Prozess wird zu langsam oder zu oberflächlich. | Risikobasierte Kritikalitätsklassen nutzen. |
| Signierung ohne Verifikation | Artefakte sind signiert, aber niemand prüft es im Deployment. | Admission Control oder Deployment-Gate einführen. |
| Scanner ohne Owner | Findings entstehen, aber niemand behebt sie. | Service Owner und SLAs verbindlich machen. |
| OT wie IT behandeln | Patches oder Scans gefährden Produktion. | OT-spezifische Prozesse, Tests und Wartungsfenster nutzen. |
| Awareness als Pflichtübung | Mitarbeitende klicken durch Folien ohne Verhaltensänderung. | Kurze, wiederholte, rollenbasierte Lerneinheiten. |
| Compliance nur vor Audit | Kurzfristige Hektik und schwache Nachweise. | Kontinuierliches Evidence Management. |
| KPIs ohne Entscheidung | Dashboard sieht gut aus, aber nichts ändert sich. | Jeder KPI braucht Schwelle, Owner und Eskalation. |
| Exceptions ohne Ablaufdatum | Abweichungen werden zum Dauerzustand. | Ablaufdatum, Review und Restrisiko verpflichtend machen. |
| Dienstleisterzugänge bleiben aktiv | Ehemalige Partner behalten Zugriff. | Offboarding-Kontrolle und Access Reviews etablieren. |

## Teil 17: Umsetzung in Jira oder Backlog

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Epic | Supply Chain Security Foundation | Bündelt Governance, Standards, Rollen und Repository. |
| Story | Als Service Owner möchte ich eine SBOM pro Release erzeugen, damit betroffene Komponenten schnell identifiziert werden können. | Akzeptanz: SBOM wird automatisch erzeugt und zentral abgelegt. |
| Story | Als DevSecOps Engineer möchte ich Container-Images signieren, damit nur vertrauenswürdige Artefakte deployt werden. | Akzeptanz: Signaturprüfung blockiert unsignierte Images. |
| Story | Als Supplier Manager möchte ich Lieferanten klassifizieren, damit kritische Anbieter stärker geprüft werden. | Akzeptanz: Top-20-Lieferanten haben Klassifikation und Owner. |
| Story | Als Compliance Manager möchte ich Kontrollen auf Evidenz mappen, damit Audits vorbereitet sind. | Akzeptanz: Jede Kontrolle hat Owner, Nachweis und Frequenz. |
| Task | CycloneDX-Plugin in Maven-Pipeline integrieren. | SBOM-Datei wird im Build-Artefakt abgelegt. |
| Task | Supplier Assessment Template in Repository veröffentlichen. | Template ist versioniert und freigegeben. |
| Task | OT-Fernzugriffe inventarisieren. | Liste enthält Dienstleister, Systeme, Owner und Zugriffspfad. |
| Risk | Unklare Subunternehmer bei kritischem Dienstleister. | Risiko wird bewertet und Vertrag nachgeschärft. |
| Decision | SBOM-Format für kritische Services festlegen. | ADR dokumentiert Format und Ausnahmen. |

## Teil 18: Mini-Lexikon

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| SBOM | Software Bill of Materials. Eine strukturierte Liste der Software-Komponenten. |  |
| SCA | Software Composition Analysis. Prüfung von Abhängigkeiten auf Schwachstellen und Lizenzen. |  |
| SLSA | Framework zur Verbesserung der Vertrauenswürdigkeit von Software-Artefakten. |  |
| Provenance | Nachweis, wie ein Artefakt entstanden ist. |  |
| Cosign | Werkzeug zur Signierung und Prüfung von Artefakten, besonders Container-Images. |  |
| in-toto | Framework zur Verifikation von Schritten in der Software-Lieferkette. |  |
| Typosquatting | Angriff mit ähnlich geschriebenen Paketnamen. |  |
| Dependency Confusion | Angriff, bei dem interne Paketnamen über externe Registries ausgenutzt werden. |  |
| OT | Operational Technology, also industrielle Betriebs- und Steuerungstechnik. |  |
| ICS | Industrial Control Systems, industrielle Steuerungssysteme. |  |
| SCADA | Systeme zur Überwachung und Steuerung industrieller Prozesse. |  |
| IEC 62443 | Normenreihe für industrielle Cybersicherheit. |  |
| Zone | Gruppe von Assets mit ähnlichem Schutzbedarf. |  |
| Conduit | Kontrollierter Kommunikationsweg zwischen Zonen. |  |
| DPA | Datenverarbeitungsvertrag mit Auftragsverarbeitern. |  |
| Exception | Dokumentierte und genehmigte Abweichung von einer Regel. |  |
| Residual Risk | Restrisiko nach Maßnahmen. |  |
| MTTR | Durchschnittliche Zeit bis Behebung oder Wiederherstellung, je nach Kontext. |  |
| MTTD | Durchschnittliche Zeit bis Erkennung. |  |
| KPI | Kennzahl zur Steuerung. |  |

## Teil 19: 500 Praxisfragen für Assessments und Workshops

### Strategie und Scope

1. Heute prüfen: Welche Produkte, Services oder Systeme sind geschäftskritisch?
2. Heute prüfen: Welche Lieferketten beeinflussen diese Produkte direkt?
3. Heute prüfen: Welche Lieferketten beeinflussen diese Produkte indirekt?
4. Heute prüfen: Welche regulatorischen Anforderungen sind im Scope?
5. Heute prüfen: Welche Kundenanforderungen gibt es zur Lieferkettensicherheit?
6. Heute prüfen: Welche Architekturprinzipien gelten bereits?
7. Heute prüfen: Welche Prinzipien fehlen?
8. Heute prüfen: Welche Entscheidungen wurden bisher nicht dokumentiert?
9. Heute prüfen: Wo entstehen heute die größten Intransparenzen?
10. Heute prüfen: Welche Risiken akzeptiert das Management bewusst?
11. Im Review fragen: Welche Produkte, Services oder Systeme sind geschäftskritisch?
12. Im Review fragen: Welche Lieferketten beeinflussen diese Produkte direkt?
13. Im Review fragen: Welche Lieferketten beeinflussen diese Produkte indirekt?
14. Im Review fragen: Welche regulatorischen Anforderungen sind im Scope?
15. Im Review fragen: Welche Kundenanforderungen gibt es zur Lieferkettensicherheit?
16. Im Review fragen: Welche Architekturprinzipien gelten bereits?
17. Im Review fragen: Welche Prinzipien fehlen?
18. Im Review fragen: Welche Entscheidungen wurden bisher nicht dokumentiert?
19. Im Review fragen: Wo entstehen heute die größten Intransparenzen?
20. Im Review fragen: Welche Risiken akzeptiert das Management bewusst?
21. Im Audit belegen: Welche Produkte, Services oder Systeme sind geschäftskritisch?
22. Im Audit belegen: Welche Lieferketten beeinflussen diese Produkte direkt?
23. Im Audit belegen: Welche Lieferketten beeinflussen diese Produkte indirekt?
24. Im Audit belegen: Welche regulatorischen Anforderungen sind im Scope?
25. Im Audit belegen: Welche Kundenanforderungen gibt es zur Lieferkettensicherheit?
26. Im Audit belegen: Welche Architekturprinzipien gelten bereits?
27. Im Audit belegen: Welche Prinzipien fehlen?
28. Im Audit belegen: Welche Entscheidungen wurden bisher nicht dokumentiert?
29. Im Audit belegen: Wo entstehen heute die größten Intransparenzen?
30. Im Audit belegen: Welche Risiken akzeptiert das Management bewusst?
31. Im Zielbild klären: Welche Produkte, Services oder Systeme sind geschäftskritisch?
32. Im Zielbild klären: Welche Lieferketten beeinflussen diese Produkte direkt?
33. Im Zielbild klären: Welche Lieferketten beeinflussen diese Produkte indirekt?
34. Im Zielbild klären: Welche regulatorischen Anforderungen sind im Scope?
35. Im Zielbild klären: Welche Kundenanforderungen gibt es zur Lieferkettensicherheit?
36. Im Zielbild klären: Welche Architekturprinzipien gelten bereits?
37. Im Zielbild klären: Welche Prinzipien fehlen?
38. Im Zielbild klären: Welche Entscheidungen wurden bisher nicht dokumentiert?
39. Im Zielbild klären: Wo entstehen heute die größten Intransparenzen?
40. Im Zielbild klären: Welche Risiken akzeptiert das Management bewusst?
41. In der Roadmap entscheiden: Welche Produkte, Services oder Systeme sind geschäftskritisch?
42. In der Roadmap entscheiden: Welche Lieferketten beeinflussen diese Produkte direkt?
43. In der Roadmap entscheiden: Welche Lieferketten beeinflussen diese Produkte indirekt?
44. In der Roadmap entscheiden: Welche regulatorischen Anforderungen sind im Scope?
45. In der Roadmap entscheiden: Welche Kundenanforderungen gibt es zur Lieferkettensicherheit?
46. In der Roadmap entscheiden: Welche Architekturprinzipien gelten bereits?
47. In der Roadmap entscheiden: Welche Prinzipien fehlen?
48. In der Roadmap entscheiden: Welche Entscheidungen wurden bisher nicht dokumentiert?
49. In der Roadmap entscheiden: Wo entstehen heute die größten Intransparenzen?
50. In der Roadmap entscheiden: Welche Risiken akzeptiert das Management bewusst?
### Lieferanten

51. Heute prüfen: Welche Lieferanten haben Zugriff auf Produktionssysteme?
52. Heute prüfen: Welche Lieferanten verarbeiten personenbezogene Daten?
53. Heute prüfen: Welche Lieferanten sind schwer ersetzbar?
54. Heute prüfen: Welche Lieferanten nutzen Subunternehmer?
55. Heute prüfen: Welche Zertifizierungen liegen vor?
56. Heute prüfen: Welche Lieferanten sind überfällig für eine Neubewertung?
57. Heute prüfen: Welche Verträge enthalten Incident-Meldepflichten?
58. Heute prüfen: Welche Verträge enthalten Audit-Rechte?
59. Heute prüfen: Welche Lieferanten haben OT-Zugang?
60. Heute prüfen: Welche Lieferanten sind Teil kritischer Geschäftsprozesse?
61. Im Review fragen: Welche Lieferanten haben Zugriff auf Produktionssysteme?
62. Im Review fragen: Welche Lieferanten verarbeiten personenbezogene Daten?
63. Im Review fragen: Welche Lieferanten sind schwer ersetzbar?
64. Im Review fragen: Welche Lieferanten nutzen Subunternehmer?
65. Im Review fragen: Welche Zertifizierungen liegen vor?
66. Im Review fragen: Welche Lieferanten sind überfällig für eine Neubewertung?
67. Im Review fragen: Welche Verträge enthalten Incident-Meldepflichten?
68. Im Review fragen: Welche Verträge enthalten Audit-Rechte?
69. Im Review fragen: Welche Lieferanten haben OT-Zugang?
70. Im Review fragen: Welche Lieferanten sind Teil kritischer Geschäftsprozesse?
71. Im Audit belegen: Welche Lieferanten haben Zugriff auf Produktionssysteme?
72. Im Audit belegen: Welche Lieferanten verarbeiten personenbezogene Daten?
73. Im Audit belegen: Welche Lieferanten sind schwer ersetzbar?
74. Im Audit belegen: Welche Lieferanten nutzen Subunternehmer?
75. Im Audit belegen: Welche Zertifizierungen liegen vor?
76. Im Audit belegen: Welche Lieferanten sind überfällig für eine Neubewertung?
77. Im Audit belegen: Welche Verträge enthalten Incident-Meldepflichten?
78. Im Audit belegen: Welche Verträge enthalten Audit-Rechte?
79. Im Audit belegen: Welche Lieferanten haben OT-Zugang?
80. Im Audit belegen: Welche Lieferanten sind Teil kritischer Geschäftsprozesse?
81. Im Zielbild klären: Welche Lieferanten haben Zugriff auf Produktionssysteme?
82. Im Zielbild klären: Welche Lieferanten verarbeiten personenbezogene Daten?
83. Im Zielbild klären: Welche Lieferanten sind schwer ersetzbar?
84. Im Zielbild klären: Welche Lieferanten nutzen Subunternehmer?
85. Im Zielbild klären: Welche Zertifizierungen liegen vor?
86. Im Zielbild klären: Welche Lieferanten sind überfällig für eine Neubewertung?
87. Im Zielbild klären: Welche Verträge enthalten Incident-Meldepflichten?
88. Im Zielbild klären: Welche Verträge enthalten Audit-Rechte?
89. Im Zielbild klären: Welche Lieferanten haben OT-Zugang?
90. Im Zielbild klären: Welche Lieferanten sind Teil kritischer Geschäftsprozesse?
91. In der Roadmap entscheiden: Welche Lieferanten haben Zugriff auf Produktionssysteme?
92. In der Roadmap entscheiden: Welche Lieferanten verarbeiten personenbezogene Daten?
93. In der Roadmap entscheiden: Welche Lieferanten sind schwer ersetzbar?
94. In der Roadmap entscheiden: Welche Lieferanten nutzen Subunternehmer?
95. In der Roadmap entscheiden: Welche Zertifizierungen liegen vor?
96. In der Roadmap entscheiden: Welche Lieferanten sind überfällig für eine Neubewertung?
97. In der Roadmap entscheiden: Welche Verträge enthalten Incident-Meldepflichten?
98. In der Roadmap entscheiden: Welche Verträge enthalten Audit-Rechte?
99. In der Roadmap entscheiden: Welche Lieferanten haben OT-Zugang?
100. In der Roadmap entscheiden: Welche Lieferanten sind Teil kritischer Geschäftsprozesse?
### Software-Komponenten

101. Heute prüfen: Welche Services haben keine aktuelle SBOM?
102. Heute prüfen: Welche Services nutzen Open-Source-Komponenten?
103. Heute prüfen: Welche Komponenten sind EoL?
104. Heute prüfen: Welche Abhängigkeiten sind nicht gepinnt?
105. Heute prüfen: Welche Paketquellen sind erlaubt?
106. Heute prüfen: Welche Paketquellen werden tatsächlich genutzt?
107. Heute prüfen: Welche Teams nutzen private Registries?
108. Heute prüfen: Welche Teams nutzen öffentliche Registries direkt?
109. Heute prüfen: Welche Schwachstellen sind überfällig?
110. Heute prüfen: Welche Libraries sind mehrfach in unterschiedlichen Versionen im Einsatz?
111. Im Review fragen: Welche Services haben keine aktuelle SBOM?
112. Im Review fragen: Welche Services nutzen Open-Source-Komponenten?
113. Im Review fragen: Welche Komponenten sind EoL?
114. Im Review fragen: Welche Abhängigkeiten sind nicht gepinnt?
115. Im Review fragen: Welche Paketquellen sind erlaubt?
116. Im Review fragen: Welche Paketquellen werden tatsächlich genutzt?
117. Im Review fragen: Welche Teams nutzen private Registries?
118. Im Review fragen: Welche Teams nutzen öffentliche Registries direkt?
119. Im Review fragen: Welche Schwachstellen sind überfällig?
120. Im Review fragen: Welche Libraries sind mehrfach in unterschiedlichen Versionen im Einsatz?
121. Im Audit belegen: Welche Services haben keine aktuelle SBOM?
122. Im Audit belegen: Welche Services nutzen Open-Source-Komponenten?
123. Im Audit belegen: Welche Komponenten sind EoL?
124. Im Audit belegen: Welche Abhängigkeiten sind nicht gepinnt?
125. Im Audit belegen: Welche Paketquellen sind erlaubt?
126. Im Audit belegen: Welche Paketquellen werden tatsächlich genutzt?
127. Im Audit belegen: Welche Teams nutzen private Registries?
128. Im Audit belegen: Welche Teams nutzen öffentliche Registries direkt?
129. Im Audit belegen: Welche Schwachstellen sind überfällig?
130. Im Audit belegen: Welche Libraries sind mehrfach in unterschiedlichen Versionen im Einsatz?
131. Im Zielbild klären: Welche Services haben keine aktuelle SBOM?
132. Im Zielbild klären: Welche Services nutzen Open-Source-Komponenten?
133. Im Zielbild klären: Welche Komponenten sind EoL?
134. Im Zielbild klären: Welche Abhängigkeiten sind nicht gepinnt?
135. Im Zielbild klären: Welche Paketquellen sind erlaubt?
136. Im Zielbild klären: Welche Paketquellen werden tatsächlich genutzt?
137. Im Zielbild klären: Welche Teams nutzen private Registries?
138. Im Zielbild klären: Welche Teams nutzen öffentliche Registries direkt?
139. Im Zielbild klären: Welche Schwachstellen sind überfällig?
140. Im Zielbild klären: Welche Libraries sind mehrfach in unterschiedlichen Versionen im Einsatz?
141. In der Roadmap entscheiden: Welche Services haben keine aktuelle SBOM?
142. In der Roadmap entscheiden: Welche Services nutzen Open-Source-Komponenten?
143. In der Roadmap entscheiden: Welche Komponenten sind EoL?
144. In der Roadmap entscheiden: Welche Abhängigkeiten sind nicht gepinnt?
145. In der Roadmap entscheiden: Welche Paketquellen sind erlaubt?
146. In der Roadmap entscheiden: Welche Paketquellen werden tatsächlich genutzt?
147. In der Roadmap entscheiden: Welche Teams nutzen private Registries?
148. In der Roadmap entscheiden: Welche Teams nutzen öffentliche Registries direkt?
149. In der Roadmap entscheiden: Welche Schwachstellen sind überfällig?
150. In der Roadmap entscheiden: Welche Libraries sind mehrfach in unterschiedlichen Versionen im Einsatz?
### Build und CI/CD

151. Heute prüfen: Welche Pipelines dürfen produktive Artefakte erzeugen?
152. Heute prüfen: Welche Runner sind gehärtet?
153. Heute prüfen: Wer darf Pipeline-Konfigurationen ändern?
154. Heute prüfen: Wer darf Secrets verwalten?
155. Heute prüfen: Wer darf Releases auslösen?
156. Heute prüfen: Wer prüft Build-Provenance?
157. Heute prüfen: Wer prüft Signaturen?
158. Heute prüfen: Welche Artefakte werden lokal gebaut?
159. Heute prüfen: Welche Builds sind reproduzierbar?
160. Heute prüfen: Welche Build-Schritte sind nicht nachvollziehbar?
161. Im Review fragen: Welche Pipelines dürfen produktive Artefakte erzeugen?
162. Im Review fragen: Welche Runner sind gehärtet?
163. Im Review fragen: Wer darf Pipeline-Konfigurationen ändern?
164. Im Review fragen: Wer darf Secrets verwalten?
165. Im Review fragen: Wer darf Releases auslösen?
166. Im Review fragen: Wer prüft Build-Provenance?
167. Im Review fragen: Wer prüft Signaturen?
168. Im Review fragen: Welche Artefakte werden lokal gebaut?
169. Im Review fragen: Welche Builds sind reproduzierbar?
170. Im Review fragen: Welche Build-Schritte sind nicht nachvollziehbar?
171. Im Audit belegen: Welche Pipelines dürfen produktive Artefakte erzeugen?
172. Im Audit belegen: Welche Runner sind gehärtet?
173. Im Audit belegen: Wer darf Pipeline-Konfigurationen ändern?
174. Im Audit belegen: Wer darf Secrets verwalten?
175. Im Audit belegen: Wer darf Releases auslösen?
176. Im Audit belegen: Wer prüft Build-Provenance?
177. Im Audit belegen: Wer prüft Signaturen?
178. Im Audit belegen: Welche Artefakte werden lokal gebaut?
179. Im Audit belegen: Welche Builds sind reproduzierbar?
180. Im Audit belegen: Welche Build-Schritte sind nicht nachvollziehbar?
181. Im Zielbild klären: Welche Pipelines dürfen produktive Artefakte erzeugen?
182. Im Zielbild klären: Welche Runner sind gehärtet?
183. Im Zielbild klären: Wer darf Pipeline-Konfigurationen ändern?
184. Im Zielbild klären: Wer darf Secrets verwalten?
185. Im Zielbild klären: Wer darf Releases auslösen?
186. Im Zielbild klären: Wer prüft Build-Provenance?
187. Im Zielbild klären: Wer prüft Signaturen?
188. Im Zielbild klären: Welche Artefakte werden lokal gebaut?
189. Im Zielbild klären: Welche Builds sind reproduzierbar?
190. Im Zielbild klären: Welche Build-Schritte sind nicht nachvollziehbar?
191. In der Roadmap entscheiden: Welche Pipelines dürfen produktive Artefakte erzeugen?
192. In der Roadmap entscheiden: Welche Runner sind gehärtet?
193. In der Roadmap entscheiden: Wer darf Pipeline-Konfigurationen ändern?
194. In der Roadmap entscheiden: Wer darf Secrets verwalten?
195. In der Roadmap entscheiden: Wer darf Releases auslösen?
196. In der Roadmap entscheiden: Wer prüft Build-Provenance?
197. In der Roadmap entscheiden: Wer prüft Signaturen?
198. In der Roadmap entscheiden: Welche Artefakte werden lokal gebaut?
199. In der Roadmap entscheiden: Welche Builds sind reproduzierbar?
200. In der Roadmap entscheiden: Welche Build-Schritte sind nicht nachvollziehbar?
### OT/ICS

201. Heute prüfen: Welche OT-Zonen existieren?
202. Heute prüfen: Welche Conduits existieren?
203. Heute prüfen: Welche Anlagen haben Fernwartung?
204. Heute prüfen: Welche Dienstleister nutzen Fernwartung?
205. Heute prüfen: Welche Systeme sind nicht patchbar?
206. Heute prüfen: Welche Kompensationsmaßnahmen existieren?
207. Heute prüfen: Welche Backups wurden getestet?
208. Heute prüfen: Welche Wiederanlaufpläne existieren?
209. Heute prüfen: Welche Anlagen haben externe Netzwerkverbindungen?
210. Heute prüfen: Welche USB-Regeln gelten in der Produktion?
211. Im Review fragen: Welche OT-Zonen existieren?
212. Im Review fragen: Welche Conduits existieren?
213. Im Review fragen: Welche Anlagen haben Fernwartung?
214. Im Review fragen: Welche Dienstleister nutzen Fernwartung?
215. Im Review fragen: Welche Systeme sind nicht patchbar?
216. Im Review fragen: Welche Kompensationsmaßnahmen existieren?
217. Im Review fragen: Welche Backups wurden getestet?
218. Im Review fragen: Welche Wiederanlaufpläne existieren?
219. Im Review fragen: Welche Anlagen haben externe Netzwerkverbindungen?
220. Im Review fragen: Welche USB-Regeln gelten in der Produktion?
221. Im Audit belegen: Welche OT-Zonen existieren?
222. Im Audit belegen: Welche Conduits existieren?
223. Im Audit belegen: Welche Anlagen haben Fernwartung?
224. Im Audit belegen: Welche Dienstleister nutzen Fernwartung?
225. Im Audit belegen: Welche Systeme sind nicht patchbar?
226. Im Audit belegen: Welche Kompensationsmaßnahmen existieren?
227. Im Audit belegen: Welche Backups wurden getestet?
228. Im Audit belegen: Welche Wiederanlaufpläne existieren?
229. Im Audit belegen: Welche Anlagen haben externe Netzwerkverbindungen?
230. Im Audit belegen: Welche USB-Regeln gelten in der Produktion?
231. Im Zielbild klären: Welche OT-Zonen existieren?
232. Im Zielbild klären: Welche Conduits existieren?
233. Im Zielbild klären: Welche Anlagen haben Fernwartung?
234. Im Zielbild klären: Welche Dienstleister nutzen Fernwartung?
235. Im Zielbild klären: Welche Systeme sind nicht patchbar?
236. Im Zielbild klären: Welche Kompensationsmaßnahmen existieren?
237. Im Zielbild klären: Welche Backups wurden getestet?
238. Im Zielbild klären: Welche Wiederanlaufpläne existieren?
239. Im Zielbild klären: Welche Anlagen haben externe Netzwerkverbindungen?
240. Im Zielbild klären: Welche USB-Regeln gelten in der Produktion?
241. In der Roadmap entscheiden: Welche OT-Zonen existieren?
242. In der Roadmap entscheiden: Welche Conduits existieren?
243. In der Roadmap entscheiden: Welche Anlagen haben Fernwartung?
244. In der Roadmap entscheiden: Welche Dienstleister nutzen Fernwartung?
245. In der Roadmap entscheiden: Welche Systeme sind nicht patchbar?
246. In der Roadmap entscheiden: Welche Kompensationsmaßnahmen existieren?
247. In der Roadmap entscheiden: Welche Backups wurden getestet?
248. In der Roadmap entscheiden: Welche Wiederanlaufpläne existieren?
249. In der Roadmap entscheiden: Welche Anlagen haben externe Netzwerkverbindungen?
250. In der Roadmap entscheiden: Welche USB-Regeln gelten in der Produktion?
### Awareness

251. Heute prüfen: Welche Zielgruppen brauchen welche Trainings?
252. Heute prüfen: Wie hoch ist die Trainingsabschlussquote?
253. Heute prüfen: Wie hoch ist die Phishing-Klickrate?
254. Heute prüfen: Wie hoch ist die Melderate?
255. Heute prüfen: Welche Abteilungen brauchen zusätzliche Unterstützung?
256. Heute prüfen: Welche Themen werden häufig falsch verstanden?
257. Heute prüfen: Welche Führungskräfte benötigen Briefings?
258. Heute prüfen: Welche Lernformate funktionieren am besten?
259. Heute prüfen: Wie wird sicheres Verhalten anerkannt?
260. Heute prüfen: Wie werden Vorfälle ohne Angst gemeldet?
261. Im Review fragen: Welche Zielgruppen brauchen welche Trainings?
262. Im Review fragen: Wie hoch ist die Trainingsabschlussquote?
263. Im Review fragen: Wie hoch ist die Phishing-Klickrate?
264. Im Review fragen: Wie hoch ist die Melderate?
265. Im Review fragen: Welche Abteilungen brauchen zusätzliche Unterstützung?
266. Im Review fragen: Welche Themen werden häufig falsch verstanden?
267. Im Review fragen: Welche Führungskräfte benötigen Briefings?
268. Im Review fragen: Welche Lernformate funktionieren am besten?
269. Im Review fragen: Wie wird sicheres Verhalten anerkannt?
270. Im Review fragen: Wie werden Vorfälle ohne Angst gemeldet?
271. Im Audit belegen: Welche Zielgruppen brauchen welche Trainings?
272. Im Audit belegen: Wie hoch ist die Trainingsabschlussquote?
273. Im Audit belegen: Wie hoch ist die Phishing-Klickrate?
274. Im Audit belegen: Wie hoch ist die Melderate?
275. Im Audit belegen: Welche Abteilungen brauchen zusätzliche Unterstützung?
276. Im Audit belegen: Welche Themen werden häufig falsch verstanden?
277. Im Audit belegen: Welche Führungskräfte benötigen Briefings?
278. Im Audit belegen: Welche Lernformate funktionieren am besten?
279. Im Audit belegen: Wie wird sicheres Verhalten anerkannt?
280. Im Audit belegen: Wie werden Vorfälle ohne Angst gemeldet?
281. Im Zielbild klären: Welche Zielgruppen brauchen welche Trainings?
282. Im Zielbild klären: Wie hoch ist die Trainingsabschlussquote?
283. Im Zielbild klären: Wie hoch ist die Phishing-Klickrate?
284. Im Zielbild klären: Wie hoch ist die Melderate?
285. Im Zielbild klären: Welche Abteilungen brauchen zusätzliche Unterstützung?
286. Im Zielbild klären: Welche Themen werden häufig falsch verstanden?
287. Im Zielbild klären: Welche Führungskräfte benötigen Briefings?
288. Im Zielbild klären: Welche Lernformate funktionieren am besten?
289. Im Zielbild klären: Wie wird sicheres Verhalten anerkannt?
290. Im Zielbild klären: Wie werden Vorfälle ohne Angst gemeldet?
291. In der Roadmap entscheiden: Welche Zielgruppen brauchen welche Trainings?
292. In der Roadmap entscheiden: Wie hoch ist die Trainingsabschlussquote?
293. In der Roadmap entscheiden: Wie hoch ist die Phishing-Klickrate?
294. In der Roadmap entscheiden: Wie hoch ist die Melderate?
295. In der Roadmap entscheiden: Welche Abteilungen brauchen zusätzliche Unterstützung?
296. In der Roadmap entscheiden: Welche Themen werden häufig falsch verstanden?
297. In der Roadmap entscheiden: Welche Führungskräfte benötigen Briefings?
298. In der Roadmap entscheiden: Welche Lernformate funktionieren am besten?
299. In der Roadmap entscheiden: Wie wird sicheres Verhalten anerkannt?
300. In der Roadmap entscheiden: Wie werden Vorfälle ohne Angst gemeldet?
### Compliance und Audit

301. Heute prüfen: Welche Standards gelten für das Unternehmen?
302. Heute prüfen: Welche Kontrollen decken welche Anforderungen ab?
303. Heute prüfen: Welche Nachweise fehlen?
304. Heute prüfen: Welche Nachweise sind veraltet?
305. Heute prüfen: Welche Kontrollen sind nicht operationalisiert?
306. Heute prüfen: Welche Audit-Findings sind offen?
307. Heute prüfen: Welche regulatorischen Änderungen kommen auf uns zu?
308. Heute prüfen: Welche Kunden verlangen zusätzliche Nachweise?
309. Heute prüfen: Welche Ausnahme gefährdet Auditfähigkeit?
310. Heute prüfen: Welche Kontrollen können automatisiert werden?
311. Im Review fragen: Welche Standards gelten für das Unternehmen?
312. Im Review fragen: Welche Kontrollen decken welche Anforderungen ab?
313. Im Review fragen: Welche Nachweise fehlen?
314. Im Review fragen: Welche Nachweise sind veraltet?
315. Im Review fragen: Welche Kontrollen sind nicht operationalisiert?
316. Im Review fragen: Welche Audit-Findings sind offen?
317. Im Review fragen: Welche regulatorischen Änderungen kommen auf uns zu?
318. Im Review fragen: Welche Kunden verlangen zusätzliche Nachweise?
319. Im Review fragen: Welche Ausnahme gefährdet Auditfähigkeit?
320. Im Review fragen: Welche Kontrollen können automatisiert werden?
321. Im Audit belegen: Welche Standards gelten für das Unternehmen?
322. Im Audit belegen: Welche Kontrollen decken welche Anforderungen ab?
323. Im Audit belegen: Welche Nachweise fehlen?
324. Im Audit belegen: Welche Nachweise sind veraltet?
325. Im Audit belegen: Welche Kontrollen sind nicht operationalisiert?
326. Im Audit belegen: Welche Audit-Findings sind offen?
327. Im Audit belegen: Welche regulatorischen Änderungen kommen auf uns zu?
328. Im Audit belegen: Welche Kunden verlangen zusätzliche Nachweise?
329. Im Audit belegen: Welche Ausnahme gefährdet Auditfähigkeit?
330. Im Audit belegen: Welche Kontrollen können automatisiert werden?
331. Im Zielbild klären: Welche Standards gelten für das Unternehmen?
332. Im Zielbild klären: Welche Kontrollen decken welche Anforderungen ab?
333. Im Zielbild klären: Welche Nachweise fehlen?
334. Im Zielbild klären: Welche Nachweise sind veraltet?
335. Im Zielbild klären: Welche Kontrollen sind nicht operationalisiert?
336. Im Zielbild klären: Welche Audit-Findings sind offen?
337. Im Zielbild klären: Welche regulatorischen Änderungen kommen auf uns zu?
338. Im Zielbild klären: Welche Kunden verlangen zusätzliche Nachweise?
339. Im Zielbild klären: Welche Ausnahme gefährdet Auditfähigkeit?
340. Im Zielbild klären: Welche Kontrollen können automatisiert werden?
341. In der Roadmap entscheiden: Welche Standards gelten für das Unternehmen?
342. In der Roadmap entscheiden: Welche Kontrollen decken welche Anforderungen ab?
343. In der Roadmap entscheiden: Welche Nachweise fehlen?
344. In der Roadmap entscheiden: Welche Nachweise sind veraltet?
345. In der Roadmap entscheiden: Welche Kontrollen sind nicht operationalisiert?
346. In der Roadmap entscheiden: Welche Audit-Findings sind offen?
347. In der Roadmap entscheiden: Welche regulatorischen Änderungen kommen auf uns zu?
348. In der Roadmap entscheiden: Welche Kunden verlangen zusätzliche Nachweise?
349. In der Roadmap entscheiden: Welche Ausnahme gefährdet Auditfähigkeit?
350. In der Roadmap entscheiden: Welche Kontrollen können automatisiert werden?
### KPIs

351. Heute prüfen: Welche KPIs steuern echte Entscheidungen?
352. Heute prüfen: Welche KPIs sind reine Dekoration?
353. Heute prüfen: Welche KPI-Datenquellen sind zuverlässig?
354. Heute prüfen: Welche KPIs brauchen Schwellenwerte?
355. Heute prüfen: Welche KPIs brauchen Eskalationsregeln?
356. Heute prüfen: Welche KPIs zeigen Trend statt Momentaufnahme?
357. Heute prüfen: Welche Kennzahlen sind für Management verständlich?
358. Heute prüfen: Welche Kennzahlen sind für Teams handlungsfähig?
359. Heute prüfen: Welche Metriken fehlen für OT?
360. Heute prüfen: Welche Metriken fehlen für Lieferanten?
361. Im Review fragen: Welche KPIs steuern echte Entscheidungen?
362. Im Review fragen: Welche KPIs sind reine Dekoration?
363. Im Review fragen: Welche KPI-Datenquellen sind zuverlässig?
364. Im Review fragen: Welche KPIs brauchen Schwellenwerte?
365. Im Review fragen: Welche KPIs brauchen Eskalationsregeln?
366. Im Review fragen: Welche KPIs zeigen Trend statt Momentaufnahme?
367. Im Review fragen: Welche Kennzahlen sind für Management verständlich?
368. Im Review fragen: Welche Kennzahlen sind für Teams handlungsfähig?
369. Im Review fragen: Welche Metriken fehlen für OT?
370. Im Review fragen: Welche Metriken fehlen für Lieferanten?
371. Im Audit belegen: Welche KPIs steuern echte Entscheidungen?
372. Im Audit belegen: Welche KPIs sind reine Dekoration?
373. Im Audit belegen: Welche KPI-Datenquellen sind zuverlässig?
374. Im Audit belegen: Welche KPIs brauchen Schwellenwerte?
375. Im Audit belegen: Welche KPIs brauchen Eskalationsregeln?
376. Im Audit belegen: Welche KPIs zeigen Trend statt Momentaufnahme?
377. Im Audit belegen: Welche Kennzahlen sind für Management verständlich?
378. Im Audit belegen: Welche Kennzahlen sind für Teams handlungsfähig?
379. Im Audit belegen: Welche Metriken fehlen für OT?
380. Im Audit belegen: Welche Metriken fehlen für Lieferanten?
381. Im Zielbild klären: Welche KPIs steuern echte Entscheidungen?
382. Im Zielbild klären: Welche KPIs sind reine Dekoration?
383. Im Zielbild klären: Welche KPI-Datenquellen sind zuverlässig?
384. Im Zielbild klären: Welche KPIs brauchen Schwellenwerte?
385. Im Zielbild klären: Welche KPIs brauchen Eskalationsregeln?
386. Im Zielbild klären: Welche KPIs zeigen Trend statt Momentaufnahme?
387. Im Zielbild klären: Welche Kennzahlen sind für Management verständlich?
388. Im Zielbild klären: Welche Kennzahlen sind für Teams handlungsfähig?
389. Im Zielbild klären: Welche Metriken fehlen für OT?
390. Im Zielbild klären: Welche Metriken fehlen für Lieferanten?
391. In der Roadmap entscheiden: Welche KPIs steuern echte Entscheidungen?
392. In der Roadmap entscheiden: Welche KPIs sind reine Dekoration?
393. In der Roadmap entscheiden: Welche KPI-Datenquellen sind zuverlässig?
394. In der Roadmap entscheiden: Welche KPIs brauchen Schwellenwerte?
395. In der Roadmap entscheiden: Welche KPIs brauchen Eskalationsregeln?
396. In der Roadmap entscheiden: Welche KPIs zeigen Trend statt Momentaufnahme?
397. In der Roadmap entscheiden: Welche Kennzahlen sind für Management verständlich?
398. In der Roadmap entscheiden: Welche Kennzahlen sind für Teams handlungsfähig?
399. In der Roadmap entscheiden: Welche Metriken fehlen für OT?
400. In der Roadmap entscheiden: Welche Metriken fehlen für Lieferanten?

## Teil 20: Coach-Karten für tägliche Anwendung

### Coach-Karte 001: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 002: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 003: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 004: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 005: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 006: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 007: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 008: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 009: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 010: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 011: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 012: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 013: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 014: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 015: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 016: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 017: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 018: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 019: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 020: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 021: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 022: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 023: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 024: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 025: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 026: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 027: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 028: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 029: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 030: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 031: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 032: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 033: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 034: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 035: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 036: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 037: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 038: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 039: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 040: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 041: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 042: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 043: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 044: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 045: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 046: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 047: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 048: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 049: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 050: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 051: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 052: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 053: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 054: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 055: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 056: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 057: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 058: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 059: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 060: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 061: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 062: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 063: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 064: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 065: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 066: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 067: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 068: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 069: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 070: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 071: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 072: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 073: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 074: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 075: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 076: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 077: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 078: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 079: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 080: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 081: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 082: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 083: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 084: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 085: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 086: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 087: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 088: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 089: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 090: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 091: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 092: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 093: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 094: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 095: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 096: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 097: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 098: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 099: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 100: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 101: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 102: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 103: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 104: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 105: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 106: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 107: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 108: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 109: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 110: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 111: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 112: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 113: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 114: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 115: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 116: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 117: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 118: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 119: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 120: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 121: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 122: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 123: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 124: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 125: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 126: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 127: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 128: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 129: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 130: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 131: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 132: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 133: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 134: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 135: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 136: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 137: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 138: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 139: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 140: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 141: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 142: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 143: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 144: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 145: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 146: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 147: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 148: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 149: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 150: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 151: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 152: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 153: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 154: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 155: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 156: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 157: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 158: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 159: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 160: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 161: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 162: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 163: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 164: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 165: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 166: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 167: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 168: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 169: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 170: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 171: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 172: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 173: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 174: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 175: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 176: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 177: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 178: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 179: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 180: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 181: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 182: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 183: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 184: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 185: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 186: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 187: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 188: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 189: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 190: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 191: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 192: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 193: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 194: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 195: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 196: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 197: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 198: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 199: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 200: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 201: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 202: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 203: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 204: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 205: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 206: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 207: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 208: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 209: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 210: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 211: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 212: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 213: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 214: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 215: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 216: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 217: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 218: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 219: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 220: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 221: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 222: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 223: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 224: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 225: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 226: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 227: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 228: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 229: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 230: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 231: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 232: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 233: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 234: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 235: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 236: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 237: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 238: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 239: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 240: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 241: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 242: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 243: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 244: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 245: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 246: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 247: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 248: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 249: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 250: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 251: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 252: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 253: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 254: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 255: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 256: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 257: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 258: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 259: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 260: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 261: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 262: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 263: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 264: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 265: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 266: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 267: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 268: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 269: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 270: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 271: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 272: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 273: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 274: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 275: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 276: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 277: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 278: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 279: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 280: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 281: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 282: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 283: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 284: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 285: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 286: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 287: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 288: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 289: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 290: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 291: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 292: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 293: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 294: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 295: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 296: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 297: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 298: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 299: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 300: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 301: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 302: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 303: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 304: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 305: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 306: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 307: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 308: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 309: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 310: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 311: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 312: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 313: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 314: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 315: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 316: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 317: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 318: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 319: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 320: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 321: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 322: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 323: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 324: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 325: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 326: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 327: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 328: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 329: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 330: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 331: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 332: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 333: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 334: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 335: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 336: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 337: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 338: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 339: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 340: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 341: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 342: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 343: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 344: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 345: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 346: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 347: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 348: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 349: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 350: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 351: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 352: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 353: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 354: OT-Zone

- Fokus: OT-Zone
- Aufgabe: Prüfe heute eine OT-Zone auf klare Grenzen und Kommunikationswege.
- Merksatz: Zonen machen Schutzbedarf sichtbar.
- Beispiel: Ohne Zonen bleibt OT-Sicherheit ein Netz aus Einzelfällen.
- Ergebnis: Ergebnis: Zone oder Conduit dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 355: Fernzugriff

- Fokus: Fernzugriff
- Aufgabe: Prüfe heute einen externen Fernzugriff.
- Merksatz: Dauerhafte Zugänge sind ein Risiko.
- Beispiel: Guter Fernzugriff ist genehmigt, zeitlich begrenzt, protokolliert und abschaltbar.
- Ergebnis: Ergebnis: Zugriff bewertet.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 356: Exception

- Fokus: Exception
- Aufgabe: Prüfe heute eine aktive Ausnahme.
- Merksatz: Eine Ausnahme ohne Ablaufdatum ist keine Ausnahme, sondern ein neuer Standard.
- Beispiel: Restrisiko muss sichtbar und akzeptiert sein.
- Ergebnis: Ergebnis: Ablaufdatum und Owner bestätigt.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 357: Awareness

- Fokus: Awareness
- Aufgabe: Prüfe heute, ob ein Training ein konkretes Verhalten verbessert.
- Merksatz: Awareness ist nicht Folienkonsum.
- Beispiel: Gute Awareness erhöht Melden, Verstehen und sichere Routine.
- Ergebnis: Ergebnis: Lernziel geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 358: Compliance Evidence

- Fokus: Compliance Evidence
- Aufgabe: Prüfe heute einen Kontrollnachweis.
- Merksatz: Was nicht nachweisbar ist, wird im Audit schwach.
- Beispiel: Gute Evidenz ist aktuell, auffindbar und eindeutig einer Kontrolle zugeordnet.
- Ergebnis: Ergebnis: Evidence Record aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 359: KPI

- Fokus: KPI
- Aufgabe: Prüfe heute eine Kennzahl auf Entscheidungsnutzen.
- Merksatz: Eine Zahl ohne Konsequenz ist Dekoration.
- Beispiel: Jeder KPI braucht Owner, Schwelle und nächste Aktion.
- Ergebnis: Ergebnis: KPI geschärft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 360: Architecture Repository

- Fokus: Architecture Repository
- Aufgabe: Prüfe heute, ob ein wichtiges Artefakt zentral auffindbar ist.
- Merksatz: Repository schlägt Einzelwissen.
- Beispiel: Architekturwissen muss versioniert, auffindbar und verwendbar sein.
- Ergebnis: Ergebnis: Ablage verbessert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 361: Lieferantenkritikalität

- Fokus: Lieferantenkritikalität
- Aufgabe: Prüfe heute einen Lieferanten danach, welchen Schaden ein Ausfall oder eine Kompromittierung verursachen würde.
- Merksatz: Kritikalität entsteht aus Daten, Zugriff, Abhängigkeit und Geschäftsfolgen.
- Beispiel: Ein Lieferant ohne Datenzugriff kann trotzdem kritisch sein, wenn er einen Kernprozess betreibt.
- Ergebnis: Ergebnis: Kritikalitätsklasse dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 362: SBOM

- Fokus: SBOM
- Aufgabe: Prüfe heute, ob ein produktiver Service eine aktuelle SBOM hat.
- Merksatz: Eine SBOM muss zum konkreten Release passen.
- Beispiel: Eine alte SBOM ist im Ernstfall gefährlich, weil sie falsche Sicherheit erzeugt.
- Ergebnis: Ergebnis: SBOM-Abdeckung aktualisiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 363: Signierung

- Fokus: Signierung
- Aufgabe: Prüfe heute, ob ein Artefakt signiert und beim Deployment geprüft wird.
- Merksatz: Signierung ohne Prüfung ist unvollständig.
- Beispiel: Das Deployment-Gate ist der Ort, an dem Vertrauen technisch erzwungen wird.
- Ergebnis: Ergebnis: Gap für unsignierte Artefakte dokumentiert.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 364: Provenance

- Fokus: Provenance
- Aufgabe: Prüfe heute, ob nachvollziehbar ist, aus welchem Commit ein Artefakt entstanden ist.
- Merksatz: Provenance verbindet Code, Build und Artefakt.
- Beispiel: Ohne Provenance bleibt unklar, ob ein Artefakt wirklich aus der erwarteten Quelle stammt.
- Ergebnis: Ergebnis: Build-Nachweis geprüft.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

### Coach-Karte 365: Dependency Policy

- Fokus: Dependency Policy
- Aufgabe: Prüfe heute, ob ein Team klare Regeln für Abhängigkeiten hat.
- Merksatz: Abhängigkeiten sind Architekturentscheidungen im Kleinen.
- Beispiel: Ungepflegte Dependencies werden später zu Sicherheits- und Wartungsproblemen.
- Ergebnis: Ergebnis: Policy-Lücke erfasst.
- TOGAF-Bezug: Prüfe, ob daraus ein Requirement, Standard, Risiko, Decision Record oder Roadmap Item entsteht.
- Führungsfrage: Wer besitzt das Thema wirklich und wer kann entscheiden?
- Review-Frage: Ist das Ergebnis belegbar oder nur behauptet?

## Teil 21: Definition of Done

| Aspekt | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Supplier Security | Kritische Lieferanten sind klassifiziert, bewertet, vertraglich abgesichert und im Monitoring. | Supplier Register vollständig für Top-Lieferanten. |
| SBOM | Kritische Services erzeugen SBOMs automatisch und legen sie zentral ab. | SBOM pro Release im Repository. |
| Secure Build | Builds sind nachvollziehbar, geschützt und erzeugen signierte Artefakte. | Provenance und Signatur vorhanden. |
| Deployment Trust | Deployment prüft Policy, Signatur und Herkunft. | Unsignierte Images werden blockiert. |
| OT Remote Access | Fernzugriffe sind genehmigt, zeitlich begrenzt, protokolliert und deaktivierbar. | Jump Server mit MFA und Recording. |
| Awareness | Zielgruppen haben passende Inhalte und Wirkung wird gemessen. | Phishing-Melderate steigt. |
| Compliance | Anforderungen sind Kontrollen zugeordnet und Evidenz ist aktuell. | Evidence Library vollständig. |
| KPIs | Dashboard zeigt Risiken, Trends, Owner und Entscheidungen. | Management sieht Handlungsbedarf. |
| Exception Management | Ausnahmen haben Owner, Risiko, Kompensation, Ablaufdatum und Review. | Keine stille Dauerabweichung. |
| TOGAF Governance | Standards, Reviews, Roadmap und Repository sind aktiv genutzt. | Architecture Board entscheidet auf Basis von Evidenz. |

## Abschluss

Supply Chain Security ist kein Tool-Projekt. Es ist eine Architektur- und Führungsaufgabe.

Die technische Seite ist wichtig: SBOM, SCA, Signierung, Provenance, SLSA, sichere Registries und Deployment-Gates. Aber ohne Rollen, Governance, Lieferantenprozesse, Compliance-Evidenz und KPIs bleibt die Wirkung begrenzt.

Der beste Start ist nicht Perfektion. Der beste Start ist Transparenz: Welche Lieferketten haben wir? Welche sind kritisch? Wo fehlen Nachweise? Wo fehlen Owner? Wo fehlen technische Kontrollen?

Danach entsteht Schritt für Schritt eine belastbare Fähigkeit. Genau dafür ist TOGAF hilfreich: Es macht aus vielen Einzelmaßnahmen eine steuerbare Enterprise-Architecture-Fähigkeit.
