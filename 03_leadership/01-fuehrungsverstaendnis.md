# 01 – Führungsverständnis

## Einordnung

Dieses Dokument beschreibt, wie ich über Führung in Engineering-Organisationen denke.

Es soll zeigen, wie ich arbeite, worauf ich achte und welche Erfahrungen mein Führungsbild geprägt haben. Meine Sicht auf Führung kommt nicht aus Theorie allein. Sie ist in der Praxis entstanden: aus Softwareentwicklung, Betrieb, Plattformarbeit, Delivery-Verantwortung, technischer Führung und der disziplinarischen Führung von Engineering-Teams.

Ich schreibe bewusst einfach. Führung muss im Alltag verständlich bleiben. Wenn ein Führungsprinzip nur auf einer Folie gut klingt, aber im Team nicht hilft, ist es für mich nicht tragfähig.

---

## Mein Führungsverständnis in einem Satz

Ich führe so, dass Teams klarer, selbstständiger und wirksamer werden.

Mein Ziel ist nicht, jede Entscheidung selbst zu treffen. Mein Ziel ist, ein Umfeld zu schaffen, in dem gute Entscheidungen dort entstehen, wo der beste Kontext liegt.

Dafür brauchen Teams Klarheit, Vertrauen, gute technische Leitplanken, Feedback, Entwicklung und ein Betriebsmodell, das Verantwortung wirklich tragen kann.

---

## Was mich geprägt hat

### 1. Führung bedeutet, Menschen und Systeme zusammenzudenken

In meiner aktuellen Rolle führe ich ein Java-Engineering-Chapter mit rund 22 Engineers über Deutschland sowie Nearshore-Teams in Portugal und Polen.

Diese Erfahrung hat mein Führungsverständnis stark geprägt. Bei dieser Größe reicht persönliche Nähe allein nicht aus. Führung braucht verlässliche Routinen: 1:1s, Feedback, Entwicklungspläne, Performance Reviews, Hiring, Onboarding und klare Erwartungen.

Gleichzeitig darf Führung nicht nur Prozess sein. Menschen brauchen Orientierung, Vertrauen, ehrliches Feedback und einen Rahmen, in dem sie gute Arbeit leisten können.

Darum verbinde ich zwei Dinge:

- Menschen ernst nehmen.
- Das System verbessern, in dem sie arbeiten.

Für mich ist das der Kern moderner Engineering-Führung.

---

### 2. Führung braucht Klarheit

In komplexen Plattformumgebungen habe ich gelernt: Viele Probleme entstehen nicht durch fehlenden Einsatz, sondern durch fehlende Klarheit.

Unklare Rollen, unklare Schnittstellen und unklare Entscheidungen kosten Teams viel Kraft. Deshalb schaue ich zuerst auf das Betriebsmodell:

- Wer entscheidet was?
- Wer trägt welche Verantwortung?
- Wo entstehen Wartezeiten?
- Wo fehlen Informationen?
- Welche Regeln sind im Alltag wirklich wirksam?
- Welche Abhängigkeiten machen Teams langsamer?
- Wo wird Verantwortung erwartet, aber kein Entscheidungsraum gegeben?

Das Buch *Team Topologies* von Matthew Skelton und Manuel Pais hat mir geholfen, Teamgrenzen, Schnittstellen und Kommunikationswege noch bewusster zu betrachten. Gute Teamstrukturen sind kein Organigramm-Thema. Sie bestimmen sehr konkret, wie schnell und wie sauber Softwareorganisationen arbeiten können.

Wenn Teams ständig aufeinander warten, ist das für mich kein individuelles Problem einzelner Personen. Dann schaue ich zuerst auf Schnittstellen, Entscheidungswege und Verantwortlichkeiten.

---

### 3. Lieferfähigkeit und Qualität gehören zusammen

Beim Aufbau und der Führung des TEAQ-Plattformteams mit 8 Engineers wurde für mich besonders sichtbar, wie wichtig transparente Delivery-Steuerung ist.

Ein Team kann viel arbeiten und trotzdem schwer steuerbar sein, wenn niemand klar sieht, wo Arbeit hängen bleibt, wo Qualität leidet oder wo Risiken entstehen. Deshalb sind Kennzahlen wie DORA-Metriken für mich wertvoll. Nicht, um Menschen zu kontrollieren. Sondern um Flow, Qualität und Stabilität besser zu verstehen.

Das Buch *Accelerate* hat diese Sicht für mich bestätigt: Leistungsfähige Softwareorganisationen betrachten Geschwindigkeit und Stabilität gemeinsam.

Für mich heißt das praktisch:

- schneller liefern, ohne leichtsinnig zu werden
- Risiken früher sichtbar machen
- Qualität nicht erst am Ende prüfen
- Teams nicht über Bauchgefühl steuern
- Kennzahlen als Lerninstrument nutzen, nicht als Druckmittel

Delivery Governance ist für mich deshalb kein Reporting-Thema. Sie ist ein Mittel, um Teams handlungsfähiger zu machen.

---

### 4. Führung braucht technisches Verständnis

Ich komme aus Java Engineering, Billing-Systemen, Plattformarbeit, Microservices, CI/CD und Betrieb. Diese technische Basis ist für meine Führung wichtig.

Ich muss nicht jede Ecke des Codes kennen. Aber ich muss technische Entscheidungen verstehen, Risiken erkennen und gute Fragen stellen können.

Aus meiner fast zehnjährigen Erfahrung mit geschäftskritischen Billing-Systemen habe ich gelernt: Software ist nicht fertig, wenn Code gemerged wurde. Sie muss im Betrieb funktionieren. Sie muss stabil laufen. Sie muss nachvollziehbar bleiben.

Deshalb sind mir folgende Punkte wichtig:

- Wartbarkeit
- Betriebssicherheit
- klare Architekturentscheidungen
- automatisierte Qualität
- sichere Entwicklung
- nachvollziehbare Verantwortung
- verständliche Dokumentation nahe am Code

Ich lasse technische Entscheidungen nicht im Raum hängen. Wenn ein Team eine wichtige Architekturentscheidung trifft, soll diese Entscheidung nachvollziehbar dokumentiert werden. Dafür nutze ich ADRs direkt im Repository. Eine ADR erklärt, welches Problem gelöst wurde, welche Optionen betrachtet wurden und warum eine Entscheidung getroffen wurde.

Das ist kein bürokratischer Selbstzweck. Es hilft bei Wartung, Onboarding, Reviews und späteren Änderungen.

Zusätzlich halte ich Service Deep Dives für ein starkes Format. Jeder wichtige Service sollte verständlich beschreiben:

- warum er existiert
- welche fachliche Aufgabe er erfüllt
- welche Inputs und Outputs er hat
- wo er in der Wertschöpfungskette steht
- welche Abhängigkeiten bestehen
- was passiert, wenn der Service ausfällt
- welche Risiken und offenen Punkte bekannt sind

Solche Deep Dives machen Wissen teilbar. Sie helfen bei Kapazitätsverschiebungen, Onboarding, Hackathons, Architekturarbeit und Betrieb. Der genaue Effekt hängt vom Kontext ab, aber der Nutzen ist in der Praxis deutlich spürbar: Teams können sich schneller orientieren und Services schneller verstehen.

Ein Service sollte aus meiner Sicht nur dann produktiv betrieben werden, wenn auch klar ist, wie man ihn betreibt. Dazu gehören Playbooks für kritische Use Cases, bekannte Fehlerbilder und wichtige Betriebsabläufe.

---

### 5. Am Ende geht es um Menschen

Als Engineering Manager und Java Tech Lead habe ich ein internationales Plattformteam aufgebaut und geführt. Dort wurde besonders deutlich: Gute Teams entstehen nicht automatisch.

Menschen brauchen Feedback, Entwicklung, Verantwortung und Schutz vor unnötiger Reibung.

Ich habe gelernt, dass sich Führung je nach Reifegrad des Teams verändert. Am Anfang ist oft mehr direkte Orientierung nötig. Später geht es stärker darum, Entscheidungsräume zu öffnen, Menschen zu entwickeln und Rahmenbedingungen zu verbessern.

Führung kann sehr technische Arbeit sein. Sie kann aber auch sehr menschlich sein. Manchmal geht es um Architektur, Delivery und Priorisierung. Manchmal geht es darum, zuzuhören, Unsicherheit einzuordnen oder jemanden durch eine schwierige Phase zu begleiten. Und manchmal gehört auch dazu, klar zu sagen, dass Erwartungen nicht erfüllt werden oder dass sich Wege trennen müssen.

Camille Fournier beschreibt in *The Manager’s Path* sehr gut, wie sich technische Führung über Rollen hinweg verändert. Genau diese Entwicklung erkenne ich in meiner eigenen Laufbahn: vom Entwickler über technische Verantwortung hin zur Entwicklung von Menschen und Organisationen.

Für mich heißt das:

- Ich gebe Feedback früh und konkret.
- Ich entwickle Menschen nicht nur im Jahresgespräch.
- Ich mache Erwartungen sichtbar.
- Ich helfe Menschen, den nächsten Entwicklungsschritt zu erkennen.
- Ich spreche schwierige Themen klar an, aber respektvoll.
- Ich schütze Teams, wenn Druck von außen unnötig wird.
- Ich fordere Teams, wenn Verantwortung nicht sauber übernommen wird.

---

### 6. Führung schafft ein Betriebsmodell, nicht nur Meetings

Gemeinsam mit der Entwicklungsleitung habe ich an einem Engineering Operating Model gearbeitet: Rollen, Teamstrukturen, Schnittstellen und Governance-Routinen.

Das ist für mich ein zentraler Teil moderner Engineering-Führung.

Ein gutes Betriebsmodell beantwortet einfache, aber wichtige Fragen:

- Wie arbeiten Teams zusammen?
- Wie treffen wir Entscheidungen?
- Wie erkennen wir Risiken früh?
- Wie sichern wir Qualität?
- Wie lernen wir aus Problemen?
- Wie vermeiden wir unnötige Abhängigkeiten?
- Wie wird Verantwortung sichtbar?
- Wie bleibt Wissen im System?

Für mich bedeutet das: Führung sollte Mechanismen bauen, die vielen Menschen helfen, bessere Arbeit zu leisten.
Ein gutes Betriebsmodell ist genau so ein Mechanismus. Es macht gute Arbeit wahrscheinlicher, ohne jedes Detail zentral steuern zu müssen.

---

### 7. Führung macht Wissen nutzbar

Ich unterrichte nebenberuflich Java und KI-Automation an einer öffentlichen Bildungseinrichtung in Ostfriesland. Diese Lehrtätigkeit passt gut zu meinem Führungsverständnis.

Gute Führung bedeutet auch, komplexe Themen verständlich zu machen. Das gilt für Java, KI, Architektur, Delivery-Kennzahlen und Security.

Ich möchte nicht, dass Wissen nur bei einzelnen starken Personen bleibt. Wissen muss im System ankommen: in Standards, Reviews, Dokumentation, Service Deep Dives, ADRs und gemeinsamen Routinen.

Wenn Wissen nur in Köpfen bleibt, wird eine Organisation abhängig von einzelnen Personen. Wenn Wissen im System landet, wird ein Team robuster.

Das ist für mich auch im Kontext von KI wichtig. KI-Werkzeuge werden besser nutzbar, wenn fachlicher Kontext, Architekturentscheidungen und technische Regeln sauber dokumentiert sind. Ohne Kontext entsteht schnell formal richtiger, aber fachlich schwacher Output.

---

## Meine wichtigsten Führungsprinzipien

### Klarheit vor Tempo

Wenn Ziele, Rollen oder Prioritäten unklar sind, wird Geschwindigkeit teuer. Ich schaffe zuerst Klarheit, damit Teams sicherer und schneller handeln können.

Tempo ohne Klarheit führt oft zu Nach(t)arbeit.

---

### Ownership braucht Entscheidungsraum

Ich gebe Verantwortung nicht nur als Aufgabe weiter. Ich achte darauf, dass Menschen auch den passenden Entscheidungsraum und Kontext bekommen.

Verantwortung ohne Entscheidungsraum ist keine echte Ownership. Sie ist nur zusätzlicher Druck.

---

### Feedback gehört in den Alltag

Feedback darf keine Überraschung im Jahresgespräch sein. Ich gebe Feedback möglichst zeitnah, konkret und handlungsorientiert.

Gleichzeitig sollen meine Mitarbeitenden mir jederzeit Feedback geben können. Führung funktioniert nicht nur von oben nach unten. Gute Führung braucht Rückmeldung aus dem System.

---

### Qualität ist Führungsaufgabe

Qualität entsteht nicht nur durch gute Entwicklerinnen und Entwickler. Qualität entsteht durch Standards, Reviews, Tests, Automatisierung, Architekturentscheidungen und Zeit für saubere Arbeit.

Wenn Qualität immer nur eingefordert, aber nicht ermöglicht wird, entsteht Frust.

---

### Sicherheit gehört von Anfang an dazu

Secure SDLC, DevSecOps und OWASP-orientierte Praktiken sind für mich Teil guter Softwareentwicklung. Sicherheit darf nicht erst am Ende geprüft werden.

Sicherheit muss in Architektur, Entwicklung, Review, CI/CD und Betrieb mitgedacht werden.

---

### Lernen muss im System ankommen

Wenn ein Team etwas lernt, sollte dieses Wissen nicht verloren gehen. Es muss in Arbeitsweisen, Dokumentation, Standards und Entscheidungen sichtbar werden.

Nur dann wird nicht nur eine Person besser, sondern das ganze System.

---

### Führung soll Abhängigkeit reduzieren

Ich sehe mich als erfolgreich, wenn mein Team mich im Tagesgeschäft weniger braucht.

Nicht, weil Führung unwichtig wird. Sondern weil das Team handlungsfähiger geworden ist.

Ein starkes Team braucht weiterhin Führung. Aber es braucht weniger operative Abhängigkeit.

---

## Bücher und Methoden, die mein Denken beeinflussen

Diese Bücher und Ansätze haben meine Sicht auf Engineering-Führung, Organisation und Delivery geprägt:

- Camille Fournier: *The Manager’s Path* – Entwicklung von Engineering-Führung, People Management und technischer Führung.
- Andy Grove: *High Output Management* – Führung über Hebelwirkung, Routinen, 1:1s und Management-Systeme.
- Matthew Skelton / Manuel Pais: *Team Topologies* – Teamstrukturen, Kommunikationswege und Schnittstellen als Grundlage guter Softwareorganisationen.
- Nicole Forsgren / Jez Humble / Gene Kim: *Accelerate* – DORA-Metriken, Flow, Stabilität und Leistungsfähigkeit von Softwareorganisationen.
- Gene Kim / Jez Humble / Patrick Debois / John Willis: *The DevOps Handbook* – Flow, Feedback, kontinuierliches Lernen und Verantwortung in DevOps-Organisationen.
- Michael D. Watkins: *The First 90 Days* – strukturiertes Ankommen, Stakeholder-Verständnis und frühe Wirksamkeit in neuen Führungsrollen.
- Architecture Decision Records (ADRs) – nachvollziehbare Dokumentation wichtiger technischer Entscheidungen.
- Service Deep Dives – systematische Dokumentation von Zweck, Betrieb, Risiken und Abhängigkeiten eines Services.

---

## Kurzprinzip

Gute Engineering-Führung macht Menschen stärker, Entscheidungen klarer und Systeme verlässlicher.