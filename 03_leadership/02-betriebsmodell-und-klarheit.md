# 02 – Betriebsmodell und Klarheit

## Einordnung

Dieses Dokument beschreibt, warum ich in Engineering-Organisationen zuerst auf das Betriebsmodell schaue.

Mit Betriebsmodell meine ich nicht ein Organigramm und nicht eine Prozessfolie. Ich meine die reale Art, wie Arbeit im Alltag funktioniert: Wer entscheidet? Wer ist verantwortlich? Wer braucht welchen Kontext? Wo entstehen Abhängigkeiten? Wo wird Qualität gesichert? Wie werden Risiken sichtbar? Wie lernen Teams aus Problemen?

Für mich ist das Betriebsmodell der Ort, an dem Führung, Technik, Zusammenarbeit und Delivery zusammenkommen.

Wenn das Betriebsmodell unklar ist, wird selbst ein starkes Team langsamer. 
Wenn es klar ist, können Menschen besser entscheiden, Verantwortung übernehmen und sauber liefern.

---

## Mein Grundsatz

Ich schaue nicht zuerst auf einzelne Mitarbeiter. Ich schaue zuerst auf das System, in dem diese Menschen arbeiten.

Viele Probleme wirken auf den ersten Blick wie individuelle Schwächen. 
In der Praxis entstehen sie aber oft durch unklare Rollen, schlechte Schnittstellen, fehlenden Kontext, zu viele Übergaben oder widersprüchliche Prioritäten.

Darum frage ich bei Problemen nicht nur:

> Wer hat das verursacht?

Sondern zuerst:

> Welche Bedingungen haben dieses Problem wahrscheinlich gemacht?

Diese Frage hilft mir, wirksamer zu führen. 
Mir ist wichtig bei Herausforderungen mit allen Menschen zu sprechen um die verschiedenen Perspektiven und Dimensionen zu verstehen.
Manchmal bilateral, aber meistens in einem Post-Mortem Kontext.

---

## Was ein Betriebsmodell für mich bedeutet

Ein gutes Betriebsmodell beschreibt, wie Engineering-Arbeit wirklich funktioniert.

Es beantwortet einfache, aber wichtige Fragen:

- Welche Teams gibt es?
- Welche Verantwortung hat jedes Team?
- Welche Entscheidungen dürfen Teams selbst treffen?
- Welche Entscheidungen müssen abgestimmt werden?
- Wo liegen fachliche und technische Schnittstellen?
- Wie werden Architekturentscheidungen getroffen?
- Wie wird Qualität gesichert?
- Wie gehen wir mit Incidents um?
- Wie werden Risiken sichtbar?
- Wie wird Wissen dokumentiert?
- Wie wird Zusammenarbeit mit Product, QA, DevOps, Security und Management organisiert?

Wenn diese Fragen nicht klar beantwortet sind, entsteht im Alltag Reibung.
Ich merke das insbesondere wenn Senior-Devs mich etwas Fragen, was sie mit Leichtigkeit auf einer Confluence-Seite hätten lesen können. 
Sie fragen aber, weil eine solche Seite in der Regel nicht exisitiert.

Reibung zeigt sich dann im Alltag durch lange Abstimmungen, zu viele Rückfragen, unklare Prioritäten, späte Eskalationen und langsame Entscheidungen.
Oder leider auch oftmals darin, dass der Senior-Dev gezwungenermaßen over-engineert weil er nur vermuttet was die UserStory bedeuet anstatt konkrete
Ansprechperson zu kennen und ihm eine 10 Minuten Klärungsmeeting zu halten. Er investiert stattdessen 10 Stunden in die falsche Richtung. 
 
---

## Warum Klarheit so wichtig ist

Klarheit ist für mich eine Führungsaufgabe.

Menschen können nur dann gut und effizient arbeiten, wenn sie verstehen:

- was wichtig ist
- was von ihnen erwartet wird
- welche Verantwortung sie haben
- welche Entscheidungen sie selbst treffen dürfen
- wann sie andere einbeziehen müssen
- woran gute Qualität erkannt wird
- wie Probleme eskaliert werden
- wie Arbeit priorisiert wird

Wenn Klarheit fehlt, werden Menschen vorsichtig. Sie sichern sich stärker ab. Sie fragen mehr nach. Sie eskalieren früher. Sie warten länger auf Entscheidungen.

Das ist selten fehlende Motivation. Es ist oft eine normale Reaktion auf ein unklar aufgebautes System.

---

## Was mich in meiner Praxis geprägt hat

### 1. Verteilte Engineering-Teams brauchen mehr explizite Klarheit

In meiner aktuellen Rolle führe ich ein Java-Engineering-Chapter mit rund 22 Engineers über Deutschland sowie Nearshore-Teams in Portugal und Polen.

In einem verteilten Setup funktioniert vieles nicht mehr über Zuruf. Entscheidungen, Schnittstellen und Erwartungen müssen bewusster formuliert werden.

Nearshore-Zusammenarbeit braucht klare Strukturen:

- sauberes Onboarding
- verständliche Service-Dokumentation
- klare Review-Routinen
- gemeinsame Qualitätsstandards
- nachvollziehbare Architekturentscheidungen
- feste Austauschformate
- klare Eskalationswege

Wenn diese Dinge fehlen, entstehen Missverständnisse. Nicht, weil Menschen nicht wollen. Sondern weil Kontext fehlt.

Darum ist Klarheit in verteilten Teams ein absolutes Muss. Sie ist eine Voraussetzung für gute Zusammenarbeit.

---

### 2. Plattformarbeit braucht klare Schnittstellen

Beim Aufbau und Betrieb komplexer Plattformen habe ich gelernt: Plattformarbeit scheitert selten an einem einzigen großen Problem. Sie wird oft durch viele kleine Unklarheiten schwer und träge.

Typische Fragen sind:

- Wem gehört welcher Service?
- Wer darf eine Schnittstelle ändern?
- Wer muss bei Breaking Changes informiert werden?
- Wer trägt Verantwortung im Betrieb?
- Was passiert bei Incidents?
- Wo stehen bekannte Risiken?
- Wo wird technischer Kontext dokumentiert?

Wenn diese Fragen nicht klar sind, entstehen Abhängigkeiten und Wartezeiten.

Teamgrenzen und Interaktionsmuster haben einen großen Einfluss auf die Leistungsfähigkeit von Softwareorganisationen.
Sie reduzieren teuere Abstimmungskosten. 

---

### 3. Delivery Governance braucht ein klares Betriebsmodell

DDORA-Kennzahlen, CI/CD-Quality-Gates und Delivery-Transparenz funktionieren nur dann gut, wenn klar ist, wie Entscheidungen getroffen werden.
Kennzahlen allein verbessern nichts, außer das man halt weiß das es nicht gut läuft.
SSie helfen erst dann, wenn Teams gemeinsam verstehen:

-- Was zeigt diese Kennzahl?
-- Welche Ursache vermuten wir?
-- Welche Maßnahme leiten wir ab?
-- Wer entscheidet darüber?
-- Wann prüfen wir, ob es besser wurde?

EEine Metrik ohne Entscheidungsroutine ist nur Reporting. 
Eine Metrik mit guter Entscheidungsroutine wird zu einem Lerninstrument und zu einem Spiegel für das Team.
 
---

### 4. Betrieb braucht klare Verantwortung

AAus meiner Arbeit mit geschäftskritischen Billing-Systemen habe ich gelernt: Ein System ist nicht fertig, wenn es entwickelt wurde. Es muss im Betrieb verstanden, beobachtet und unterstützt werden können.

Für den Betrieb braucht es klare Antworten:

- Wer reagiert bei einem Fehler?
- Welche Logs sind wichtig?
- Welche Use Cases sind kritisch?
- Welche Abhängigkeiten müssen geprüft werden?
- Wie wird ein Incident eingeordnet?
- Wo steht das Playbook?
- Wer entscheidet über Rollback oder Hotfix?

Ohne diese Klarheit wird Betrieb unnötig riskant.

Deshalb halte ich Playbooks, Service Deep Dives und ADRs für sehr wichtig. Sie machen Wissen nicht nur sichtbar, sondern nutzbar.

---

## Wie ich Klarheit herstelle

Ich versuche Klarheit nicht durch lange Prozesse zu erzwingen. Ich arbeite lieber mit einfachen, wiederholbaren Mechanismen.

### Rollen klären

Ich achte darauf, dass Teams wissen, wer wofür verantwortlich ist.

Dabei geht es nicht nur um Titel. Es geht um echte Verantwortung im Alltag.

Eine Rolle ist erst dann klar, wenn die Person versteht:

- welche Entscheidungen zur Rolle gehören
- welche Erwartungen damit verbunden sind
- welche Schnittstellen wichtig sind
- welche Grenzen es gibt
- wann eskaliert werden soll

### Entscheidungsräume definieren

Ownership ohne Entscheidungsraum funktioniert nicht.

Wenn ein Team Verantwortung tragen soll, muss klar sein, was es selbst entscheiden darf. Gleichzeitig muss klar sein, welche Entscheidungen größere Abstimmung brauchen.

Ich unterscheide gerne zwischen:

- Entscheidungen, die ein Team selbst treffen kann
- Entscheidungen, die mit anderen Teams abgestimmt werden müssen
- Entscheidungen, die Architektur oder Management einbeziehen müssen
- Entscheidungen, die wegen Risiko, Kosten oder Compliance besonders sorgfältig getroffen werden müssen

Diese Unterscheidung verhindert unnötige Eskalation und zu lockere Entscheidungen.

### Schnittstellen sichtbar machen

Schnittstellen sind oft die Stellen, an denen Reibung entsteht.

Darum schaue ich genau auf Übergaben zwischen Teams, Rollen und Systemen.

Typische Prüffragen sind:

- Welche Informationen gehen bei Übergaben verloren?
- Wo warten Teams regelmäßig aufeinander?
- Welche Schnittstellen sind technisch oder organisatorisch zu eng gekoppelt?
- Welche Teams müssen zu oft gemeinsam entscheiden?
- Wo fehlen klare API- oder Service-Verantwortlichkeiten?

Wenn Schnittstellen klarer werden, wird Zusammenarbeit ruhiger.

### Standards definieren

Standards helfen Teams, weniger über Grundsatzfragen zu diskutieren.

Gute Standards beantworten wiederkehrende Fragen:

- Wie schreiben wir APIs?
- Wie dokumentieren wir Architekturentscheidungen?
- Welche Tests erwarten wir?
- Welche Security-Prüfungen sind Pflicht?
- Was muss vor einem Deployment erfüllt sein?
- Welche Informationen gehören in ein Playbook?
- Wie sieht ein Service Deep Dive aus?

Standards dürfen nicht zu schwer sein. Sie müssen im Alltag nutzbar bleiben.

### Routinen schaffen

Klarheit entsteht nicht durch ein einmaliges Dokument. Klarheit entsteht durch wiederholte Routinen.

Beispiele:

- regelmäßige 1:1s
- Team-Retrospektiven
- Architektur-Reviews
- Service Deep Dives
- DORA-Reviews
- Incident Reviews
- Chapter-Formate
- Onboarding-Routinen
- Code-Quality-Formate

Routinen schaffen Verlässlichkeit. Sie machen sichtbar, was sonst zufällig bleibt.

---

## Was ich bewusst vermeide

Ich vermeide Betriebsmodelle, die nur auf Papier funktionieren.

Ein Modell ist für mich nur dann gut, wenn es im Alltag hilft.

Ich vermeide auch Prozesse, die Verantwortung verdecken. Wenn niemand mehr weiß, wer entscheidet, ist der Prozess zu schwer.

Ich vermeide außerdem unklare Begriffe. Wörter wie Ownership, Empowerment, DevOps oder Governance müssen praktisch übersetzt werden. Sonst wirken sie modern, helfen aber nicht.

Für mich zählt:

- Was bedeutet das konkret?
- Wer handelt?
- Wer entscheidet?
- Wer trägt Verantwortung?
- Wie wird es im Alltag sichtbar?

---

## Typische Warnsignale für fehlende Klarheit

Diese Muster nehme ich ernst:

- Entscheidungen dauern lange, obwohl das Thema klein ist.
- Teams warten regelmäßig auf andere Teams.
- Risiken werden spät oder weich formuliert.
- Niemand fühlt sich wirklich verantwortlich.
- Alles muss über dieselben wenigen Personen laufen.
- Dokumentation existiert, wird aber nicht genutzt.
- Incidents führen immer wieder zu denselben Fragen.
- Neue Mitarbeitende brauchen lange, um Services zu verstehen.
- Product, Engineering und QA haben unterschiedliche Vorstellungen von „fertig“.
- Kennzahlen werden berichtet, aber führen zu keiner Entscheidung.

Wenn ich solche Muster sehe, suche ich nicht nach Schuld. Ich suche nach der Unklarheit im System.

---

## Methoden und Bücher, die mein Denken beeinflussen

### Team Topologies

*Team Topologies* hilft mir, Teamgrenzen und Interaktionsmuster bewusst zu betrachten. Die wichtigste Erkenntnis für meine Arbeit ist: Teamstrukturen sind keine reine Organisationsfrage. Sie beeinflussen direkt, wie Software gebaut, betrieben und verändert wird.

### High Output Management

Andy Grove beschreibt Management als Hebelwirkung. Für mich bedeutet das: Führung sollte Mechanismen schaffen, die vielen Menschen helfen. Ein gutes Betriebsmodell ist ein solcher Mechanismus.

### Accelerate

*Accelerate* zeigt, dass leistungsfähige Softwareorganisationen Geschwindigkeit und Stabilität gemeinsam betrachten. Das bestätigt meinen Ansatz, Delivery nicht nur über Output, sondern über Flow, Qualität und Risiko zu steuern.

### The DevOps Handbook

Das *DevOps Handbook* prägt mein Verständnis von Flow, Feedback und kontinuierlichem Lernen. Ein gutes Betriebsmodell muss diese drei Dinge unterstützen.

### ADRs und Service Deep Dives

ADRs und Service Deep Dives sind für mich praktische Methoden, um technische Entscheidungen und Service-Wissen im System zu halten. Sie verbinden Architektur, Betrieb, Onboarding und Qualität.

---

## Meine Prinzipien für Betriebsmodell und Klarheit

### Klarheit muss im Alltag funktionieren

Ein Dokument allein reicht nicht. Klarheit zeigt sich daran, ob Menschen im Alltag bessere Entscheidungen treffen können.

### Verantwortung braucht Kontext

Menschen können nur dann gute Verantwortung übernehmen, wenn sie den Kontext kennen.

### Schnittstellen sind Führungsarbeit

Schlechte Schnittstellen erzeugen Reibung. Gute Führung macht Schnittstellen sichtbar und verbessert sie.

### Standards sollen entlasten

Gute Standards reduzieren unnötige Diskussionen. Schlechte Standards erzeugen Bürokratie.

### Kennzahlen brauchen Entscheidungen

Eine Kennzahl ist erst dann nützlich, wenn daraus Lernen oder Handeln entsteht.

### Wissen muss auffindbar sein

Wissen, das nur in Köpfen existiert, macht Organisationen abhängig. Wissen im System macht Teams robuster.

---

## KurzprinzipW
wWWW
Ein gutes Betriebsmodell macht Arbeit klarer, Entscheidungen schneller und Verantwortung tragfähiger.
