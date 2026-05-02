# Java Quality Compendium

Eine kuratierte Sammlung dessen, was ich über Java-Code-Qualität gelernt habe — und was ich davon für tragfähig halte.

## Worum es hier geht

Code-Qualität ist für mich kein Stilthema und keine reine Geschmacksfrage. Sie entscheidet darüber, ob ein System morgen noch veränderbar ist, ob neue Teammitglieder sich darin zurechtfinden und ob Engineering-Arbeit auf Dauer Freude macht oder zur Last wird.

Dieses Compendium sammelt Muster, Anti-Muster, kleine Codebeispiele und ausgearbeitete Begründungen — also nicht nur „so macht man es", sondern auch „warum genau so und nicht anders". Im Zentrum steht modernes Java (21+), aber die Prinzipien dahinter gelten weiter.

## Warum ich das pflege

Ich führe als Chapter Lead Java-Engineers in mehreren Ländern. In meiner Arbeit ist saubere Code-Qualität nichts, was sich von selbst durchsetzt — sie braucht eine gemeinsame Sprache, an der man sich orientieren kann. Genau das ist hier der Versuch: eine kompakte, ehrliche Referenz, die im Alltag tatsächlich nützlich ist.

Das Compendium ist gleichzeitig:

- **Nachschlagewerk** — wenn ich oder jemand aus meinem Umfeld eine konkrete Frage hat
- **Lerngrundlage** — für Engineers, die ihren Stil schärfen wollen
- **Referenzpunkt** für Code-Reviews und Architektur-Entscheidungen
- **Lebendes Dokument** — es wächst mit dem, was ich in der Praxis sehe und lerne

## Wie es organisiert ist

Jeder Eintrag folgt derselben Logik: erst das Problem oder die typische Stelle, an der es schiefgeht. Dann ein Beispiel, das zeigt, wie es *nicht* sein sollte — mit ehrlicher Begründung, warum dieses Muster im Alltag teuer wird. Danach die saubere Variante, ebenfalls mit Begründung. Wo es passt, kommen am Ende noch Hinweise zu Tools, Tests oder verwandten Themen.

Diese Struktur ist mir wichtig, weil reines „Best Practice" ohne Begründung selten überzeugt. Engineers wollen verstehen, warum etwas eine bessere Idee ist. Nur so wird aus einer Vorgabe eine echte Überzeugung.

## Verbindung zum übrigen Portfolio

Dieses Compendium steht nicht für sich. Es ergänzt:

- die [Code Quality ADRs](./java-quality-compendium/) — dort dokumentiere ich die größeren Entscheidungen *zwischen* Mustern
- die [Working Principles](./working-principles.md) — dort beschreibe ich, wie ich grundsätzlich auf Engineering-Arbeit schaue
- die [Working Papers](./working-papers/) — dort denke ich systemisch über Engineering-Organisationen nach

Die Reihenfolge ist bewusst: hier geht es um *Code*, in den ADRs um *Entscheidungen*, in den Principles um *Haltung*, in den Papers um *Systeme*. Vier Ebenen, die im Alltag zusammenwirken.

## Ein Hinweis vorab

Das hier ist meine Sicht — geprägt von der Praxis, nicht von einer Lehrbuchposition. An manchen Stellen wirst du bewusste Vereinfachungen finden, an anderen Stellen Meinungen, denen man widersprechen kann. Das ist beabsichtigt. Ein Compendium soll nicht die letzte Wahrheit liefern, sondern einen klaren Bezugspunkt — von dem aus du diskutieren, abweichen oder weiterdenken kannst.

---

*Stand: April 2026 — wird laufend erweitert.*