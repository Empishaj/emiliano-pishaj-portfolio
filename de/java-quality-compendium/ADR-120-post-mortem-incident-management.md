# ADR-120 — Post-Mortem & Incident Management: Aus Fehlern lernen

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Datum             | 2024-01-01                                                    |
| Kategorie         | Betrieb · Incident Management · Lernkultur                    |


---

## 1. Warum Post-Mortems systemisch wichtig sind

```
OHNE POST-MORTEM-KULTUR:
  Incident passiert → Team behebt es → weiter wie bisher
  Dieselbe Ursache tritt 3 Monate später wieder auf
  Ursache: Der Lerneffekt wurde nicht institutionalisiert

MIT POST-MORTEM-KULTUR (Google SRE, Amazon):
  Incident → Behebung → Post-Mortem → Systemic Fix
  Dieselbe Ursache tritt nie wieder auf (oder viel seltener)
  
AMAZON PRIME DAY 2018:
  Mehrere Incidents, alle mit Post-Mortems.
  Direktes Ergebnis: 99.999% Verfügbarkeit bei den nächsten Prime Days.

SCHLÜSSELPRINZIP: Blameless Post-Mortems
  NICHT: "Wer hat Fehler gemacht?"
  SONDERN: "Was in unserem System hat diesen Fehler ermöglicht?"
  
  Menschen machen Fehler — Systeme können Fehler verhindern oder abschwächen.
  Ein Entwickler der einen Config-Fehler macht ist nicht "schuld".
  Das System das Config-Fehler ohne Review in Produktion erlaubt ist schuld.
```

---

## 2. Entscheidung

Jeder Incident mit Severity 1 oder 2 erhält ein schriftliches Post-Mortem
innerhalb von 5 Werktagen. Post-Mortems sind blameless, zeitgebunden und
erzeugen konkrete, verfolgte Action Items. Alle Post-Mortems sind öffentlich
im Engineering-Wiki zugänglich.

---

## 3. Severity-Klassifizierung

```
SEV-1 (Kritisch — sofortiger Eingriff):
  → Service vollständig nicht verfügbar
  → Produktionsdaten verloren oder korrumpiert
  → Security-Breach
  → SLA verletzt (> 1h Downtime)
  REAKTION: sofort, alle Hände an Deck
  POST-MORTEM: zwingend, innerhalb 3 Werktage

SEV-2 (Hoch — dringende Behebung):
  → Kritische Funktion degraded (Zahlungen, Login)
  → Mehrere Kunden betroffen
  → SLO-Error-Budget > 50% aufgebraucht
  REAKTION: innerhalb 30 Minuten
  POST-MORTEM: zwingend, innerhalb 5 Werktage

SEV-3 (Mittel — normale Priorität):
  → Einzelne Funktion beeinträchtigt
  → Wenige Kunden betroffen
  → Performance-Degradation
  REAKTION: innerhalb 4 Stunden
  POST-MORTEM: empfohlen (Team-Entscheidung)

SEV-4 (Niedrig):
  → Kosmetische Probleme
  → Keine Kundenauswirkung
  POST-MORTEM: optional
```

---

## 4. Incident-Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│                    INCIDENT LIFECYCLE                            │
├──────────┬──────────┬──────────┬──────────┬────────────────────┤
│ DETECT   │RESPOND   │MITIGATE  │RESOLVE   │ POST-MORTEM        │
├──────────┼──────────┼──────────┼──────────┼────────────────────┤
│ Alert    │Incident  │Rollback/ │Root Cause│ Schreiben:         │
│ ausgelöst│ Commander│Workaround│ behoben  │ 5 Werktage         │
│          │benennen  │          │          │                    │
│          │          │Status-   │Customers │ Review-Meeting:    │
│          │Slack:    │page      │ inform.  │ 7 Werktage         │
│          │#incidents│update    │          │                    │
│          │          │          │          │ Action Items:      │
│          │          │          │          │ Jira-Tickets       │
└──────────┴──────────┴──────────┴──────────┴────────────────────┘
   ←MTTR gemessen→                          ←Systemic Fix→
```

---

## 5. Das Post-Mortem-Template

```markdown
# Post-Mortem: [Kurzer Incident-Titel]

**Datum:**          [Datum des Incidents]
**Severity:**       SEV-[1/2]
**Dauer:**          [Beginn] bis [Ende] ([X Stunden Y Minuten])
**Verfasser:**      [Name, Team]
**Review-Datum:**   [Datum des Review-Meetings]

---

## Zusammenfassung
[2-3 Sätze: Was ist passiert, welche Auswirkung hatte es, wie wurde es behoben.]

---

## Impact
| Metrik               | Wert                              |
|----------------------|-----------------------------------|
| Betroffene Nutzer    | ~X.XXX                            |
| Service-Downtime     | X Stunden Y Minuten               |
| Fehlgeschlagene Requests | ~XXX.XXX                       |
| Umsatzauswirkung     | ~XX.XXX EUR (geschätzt)           |
| SLO-Error-Budget     | X% aufgebraucht                   |

---

## Timeline

| Zeit (UTC)  | Ereignis                                                    |
|-------------|-------------------------------------------------------------|
| 14:32       | Alert ausgelöst: "P95-Latenz > 2s" (→ ADR-054)            |
| 14:35       | On-Call kontaktiert, Incident-Kanal erstellt                |
| 14:38       | Incident Commander: [Name]                                  |
| 14:42       | Erste Diagnose: Erhöhte Fehlerrate im Payment-Service       |
| 14:55       | Ursache identifiziert: Deadlock in DB-Migrations-Script     |
| 15:02       | Workaround: Transaction abgebrochen, altes Deployment       |
| 15:10       | Service wieder normal → Incident mitigated                  |
| 17:30       | Root-Cause-Fix deployed (Migration korrigiert)              |
| 18:00       | Incident geschlossen                                        |

---

## Ursachenanalyse (5-Why)

**Symptom:** Payment-Service war 38 Minuten nicht erreichbar.

**Why 1:** Alle DB-Connections im Pool erschöpft.
**Why 2:** Alle Connections warteten auf einen Lock.
**Why 3:** Eine lang laufende DB-Migration hielt einen Table-Lock.
**Why 4:** Migration wurde ohne Lock-Timeout deployed.
**Why 5:** Unser Deployment-Prozess prüft nicht ob Migrations Lock-Timeouts haben.

**Root Cause:** Fehlende Lock-Timeout-Prüfung in Flyway-Migrations.

---

## Was gut funktioniert hat
- Alert wurde innerhalb 3 Minuten ausgelöst (Ziel: < 5 Minuten) ✅
- Rollback dauerte 8 Minuten (Ziel: < 15 Minuten) ✅
- On-Call-Kommunikation über Status-Page war zeitnah ✅
- OTel-Traces zeigten sofort wo der Bottleneck war (→ ADR-102) ✅

---

## Was verbessert werden kann
- Keine Prüfung auf Lock-Timeouts in Migrations vor Deployment
- DB-Migration und App-Deployment in derselben Pipeline — zu eng gekoppelt
- Status-Page-Update kam 15 Minuten nach Beginn (Ziel: < 5 Minuten)

---

## Action Items

| # | Beschreibung | Owner | Priorität | Deadline | Ticket |
|---|---|---|---|---|---|
| 1 | Flyway-Migration-Linter: warnt bei fehlendem Lock-Timeout | Platform-Team | P1 | 2 Wochen | PLAT-234 |
| 2 | DB-Migration als separater Step vor App-Deployment | Platform-Team | P1 | 1 Monat | PLAT-235 |
| 3 | Status-Page-Automatisierung: Alert → automatischer Incident | Platform-Team | P2 | 6 Wochen | PLAT-236 |
| 4 | Runbook für "DB-Lock-Incidents" erstellen | Orders-Team | P2 | 2 Wochen | ORDERS-789 |

---

## Lessons Learned
[Was lernen wir allgemein aus diesem Incident?
Nicht spezifisch für dieses System — was gilt für alle unsere Services?]

DB-Migrations die Table-Locks halten können sind ein systematisches Risiko.
→ Gilt für alle Teams: ADR-098 (Zero-Downtime Migrations) konsequent anwenden.
```

---

## 6. Blameless Post-Mortem: Die richtige Sprache

```
SCHULDZUWEISUNG (vermeiden):
  ❌ "Max hat vergessen den Lock-Timeout zu setzen."
  ❌ "Das Team hat nicht ausreichend getestet."
  ❌ "Die Deployment-Pipeline wurde falsch konfiguriert."

SYSTEMISCHES DENKEN (verwenden):
  ✅ "Unser Deployment-Prozess ermöglicht es, Lock-Timeouts zu vergessen."
  ✅ "Unsere Test-Suite erkennt keine lang-laufenden Lock-Situationen."
  ✅ "Die Pipeline-Konfiguration macht Lock-Timeout-Checks nicht obligatorisch."

WARUM DER UNTERSCHIED WICHTIG IST:
  Menschen machen unter Druck Fehler — immer.
  Schuld zuweisen ändert nichts am System.
  Systeme die Fehler unmöglich machen, sind die Lösung.
  
  Blameless-Kultur führt zu:
  → Ehrlichere Incident-Meldungen (kein Verstecken von Problemen)
  → Tiefere Root-Cause-Analysen (niemand muss sich schützen)
  → Schnellere Behebung (Fokus auf Lösung, nicht Schuldfrage)
```

---

## 7. Action-Item-Tracking

```
ACTION ITEMS sind wertlos ohne Tracking.

REGEL: Jedes Action Item braucht:
  ① Konkreten Owner (Person, nicht Team)
  ② Deadline (Datum, nicht "soon" oder "eventually")
  ③ Jira-Ticket (mit Post-Mortem-Link)
  ④ Priorität (P1/P2/P3)

FOLLOW-UP:
  Wöchentliches Engineering-Meeting: Action-Items-Review
  Monatlich: Post-Mortem-Trends analysieren
    → Welche Root-Causes wiederholen sich?
    → Welche Action Items sind systematisch überfällig?

METRIKEN:
  post_mortem.action_items.open         (sollte sinken über Zeit)
  post_mortem.action_items.overdue      (Alert wenn > 5)
  incidents.recurring_root_cause_rate   (sollte sinken)
```

---

## 8. Incident-Commander-Rolle

```
INCIDENT COMMANDER (IC):
  Koordiniert die Incident-Response — macht NICHT selbst die Debugging-Arbeit.
  
  Verantwortlichkeiten:
  ① Kommunikation: Updates in #incidents und Status-Page
  ② Delegation: "Max, schau in die DB-Metriken. Anna, prüf die Kafka-Consumer"
  ③ Timeline pflegen: protokolliert alle Schritte live
  ④ Eskalation: wann wird CTO informiert?
  ⑤ Entscheidungen: Rollback ja/nein, Workaround ja/nein
  
  IC ist NICHT:
  → der Entwickler der den Fehler "verursacht" hat
  → derjenige der am meisten debuggt
  
  IC-Rotation: alle Senior Engineers rotieren durch die IC-Rolle.
  Ziel: breite Incident-Response-Kompetenz im Team.
```

---


## Quellen & Referenzen

- **Google SRE Book, "Postmortem Culture: Learning from Failure" (2016)** — Kapitel 15; Blameless-Kultur und Lernende Organisationen.
- **John Allspaw, "Blameless PostMortems and a Just Culture" (2012), Etsy Blog** — Seminal Blog Post der Blameless-Post-Mortem-Bewegung.
- **Gene Kim, Patrick Debois, "The DevOps Handbook" (2016), Teil IV** — Feedback-Loops und kontinuierliches Lernen.
- **Sidney Dekker, "The Field Guide to Understanding Human Error" (2014)** — Systemisches Denken statt Schuldzuweisung.
 