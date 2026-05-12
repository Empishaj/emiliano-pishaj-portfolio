# QG-JAVA-072 — Chaos Engineering: Resilienz durch kontrollierte Experimente

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-072 |
| Titel | Chaos Engineering: Resilienz durch kontrollierte Experimente |
| Status | Accepted / verbindlicher Standard für kontrollierte Resilienzexperimente in Java-/Spring-Boot-Systemen |
| Sprache | Deutsch |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Boot Actuator, Micrometer, OpenTelemetry; optional Chaos Monkey for Spring Boot 4.x, Resilience4j, Kubernetes |
| Kategorie | Testing / Resilience / Observability / Betriebssicherheit / SaaS-Plattformqualität |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA Engineers, SRE/DevOps Engineers, Platform Engineers, Security Reviewer, Product Owner, Incident Manager |
| Verbindlichkeit | Chaos-Experimente DÜRFEN nur mit Hypothese, Steady-State-Definition, begrenztem Blast Radius, Rollback-/Stop-Mechanismus, Observability, Verantwortlichen und dokumentiertem Ergebnis durchgeführt werden. Zufälliges „Kaputtmachen“ von Systemen ist verboten. |
| Qualitätsziel | Resilienzmechanismen wie Timeouts, Retries, Circuit Breaker, Bulkheads, Rate Limits, Fallbacks, Degradation und Recovery-Verhalten sollen überprüfbar werden, bevor echte Ausfälle sie unter Druck testen. |
| Prüfstatus | Inhalt fachlich gegen Principles of Chaos Engineering, Chaos Monkey for Spring Boot Reference Guide, Spring Boot Actuator, Resilience4j, OpenTelemetry Collector und OWASP-nahe Betriebs-/Logging-Grundsätze validiert. |
| Letzte fachliche Prüfung | 2026-05-03 |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Chaos Engineering in Java- und Spring-Boot-Systemen kontrolliert, nachvollziehbar und sicher angewendet wird. Ziel ist nicht, Systeme wahllos zu beschädigen. Ziel ist, gezielte Experimente zu nutzen, um reale Ausfallszenarien unter kontrollierten Bedingungen zu prüfen.

Resilience-Patterns im Code sind nur eine Annahme, solange sie nicht unter Störung beobachtet wurden. Ein Circuit Breaker, der nie durch tatsächliche Fehler geöffnet wurde, ein Timeout, der nie gegen echte Latenz geprüft wurde, und ein Fallback, der nie unter Last ausgeführt wurde, sind keine belastbaren Schutzmechanismen. Sie sind zunächst nur Implementierungen.

Chaos Engineering macht diese Implementierungen beobachtbar. Es prüft, ob das System trotz realistischer Störung in einem akzeptablen Zustand bleibt, ob Monitoring rechtzeitig anschlägt, ob Runbooks funktionieren, ob Recovery eintritt und ob Teams die richtigen Signale erkennen.

---

## 2. Kurzregel für Entwickler

Chaos Engineering ist ein kontrolliertes Experiment mit Hypothese, Messpunkten und begrenztem Risiko. Jedes Experiment MUSS vorab definieren, welcher normale Zustand erwartet wird, welche Störung eingeführt wird, welche Metriken beobachtet werden, wann das Experiment gestoppt wird und welches Verhalten als Erfolg oder Fehlschlag gilt.

Kurzform:

```text
Keine Hypothese → kein Chaos-Experiment.
Keine Metriken → kein Chaos-Experiment.
Kein Stop-Mechanismus → kein Chaos-Experiment.
Kein begrenzter Blast Radius → kein Chaos-Experiment.
Keine Dokumentation → kein Lerneffekt.
```

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Spring-Boot-Services,
- Java-Backend-Services,
- SaaS-Plattformen mit Mandantenfähigkeit,
- Microservices,
- APIs,
- Worker,
- Event-Consumer,
- Batch-Prozesse,
- Integrationen zu Datenbanken,
- Integrationen zu externen HTTP-Services,
- Messaging-Systeme,
- Payment-, Identity-, Notification-, Search- und Reporting-Module,
- Staging-, Pre-Production- und kontrollierte Production-Experimente.

Diese Richtlinie gilt nicht als vollständige Anleitung für:

- Penetration Testing,
- Lasttests als eigenständige Disziplin,
- Disaster-Recovery-Planung im gesamten Unternehmen,
- vollständige Incident-Response-Prozesse,
- Cloud-Provider-Ausfallübungen auf Organisationsebene,
- Sicherheitsangriffe gegen produktive Systeme.

Diese Themen können mit Chaos Engineering verbunden werden, benötigen aber zusätzliche Verfahren, Freigaben und Schutzmaßnahmen.

---

## 4. Technischer Hintergrund

Chaos Engineering basiert auf der Idee, kontrollierte Störungen einzuführen, um die Fähigkeit eines Systems zur Stabilität, Degradation und Erholung zu prüfen. Die bekannten Principles of Chaos Engineering nennen unter anderem die Definition eines Steady State, die Variation realer Ereignisse, Experimente in produktionsnahen Bedingungen, Automatisierung und die Begrenzung des Blast Radius.

Der zentrale Unterschied zu Random Testing lautet: Chaos Engineering beginnt nicht mit einer zufälligen Störung, sondern mit einer Hypothese.

Beispiel:

```text
Hypothese:
Wenn der Payment Provider für 60 Sekunden nicht erreichbar ist,
dann öffnet der Circuit Breaker innerhalb von 10 Sekunden,
neue Bestellungen wechseln in den Status PAYMENT_PENDING,
die API bleibt verfügbar,
und die Error Rate des Order Service bleibt unter 1 %.
```

Diese Hypothese ist prüfbar. Sie enthält Störung, erwartetes Verhalten, Zeitgrenzen und Metriken.

---

## 5. Verbindlicher Standard

Chaos-Experimente MÜSSEN nach einem dokumentierten Experimentmodell durchgeführt werden. Mindestens folgende Elemente sind verpflichtend:

1. Hypothese,
2. Zielsystem und Scope,
3. Umgebung,
4. Steady-State-Metriken,
5. Störung,
6. Blast Radius,
7. Startbedingung,
8. Stopbedingung,
9. Rollback- oder Disable-Mechanismus,
10. Verantwortliche Person,
11. Beobachtende Person,
12. Kommunikationskanal,
13. Ergebnisdokumentation,
14. Maßnahmenliste nach dem Experiment.

Produktionsnahe oder produktive Experimente DÜRFEN nur durchgeführt werden, wenn Monitoring, Alerting, Runbook, Abbruchmechanismus und Verantwortlichkeiten vorab geprüft wurden.

---

## 6. Begriffe

| Aspekt | Details/Erklärung | Beispiel | Qualitätsregel |
|---|---|---|---|
| Steady State | Messbarer Normalzustand des Systems aus Nutzer- oder Systemperspektive | Error Rate < 0,5 %, P95 < 500 ms | MUSS vor dem Experiment definiert sein |
| Hypothese | Erwartung, wie das System unter Störung reagieren soll | Circuit Breaker öffnet nach 3 Fehlern | MUSS prüfbar sein |
| Variable / Assault | Eingeführte Störung | Latenz, Exception, Service-Ausfall | MUSS realistisch sein |
| Blast Radius | Begrenzter Wirkungsbereich des Experiments | ein Service, ein Tenant, 1 % Traffic | MUSS klein beginnen |
| Abort Condition | Bedingung für sofortigen Abbruch | Error Rate > 5 % für 2 Minuten | MUSS vorab feststehen |
| Recovery | Rückkehr zum Steady State | P95 wieder < 500 ms | MUSS gemessen werden |
| GameDay | Geplante Teamübung mit realistischem Szenario | Payment-Ausfall im Staging | MUSS dokumentiert werden |

---

## 7. Voraussetzungen

Chaos Engineering darf nicht eingeführt werden, bevor die folgenden Voraussetzungen erfüllt sind.

| Voraussetzung | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Observability | Metriken, Logs und Traces müssen vorhanden und nutzbar sein | Error Rate, P95, Circuit-Breaker-State | Pflicht |
| SLO-/Steady-State-Definition | Es muss klar sein, was „normal“ bedeutet | Availability, Latenz, Fehlerrate | Pflicht |
| Rollback / Stop | Experiment muss sofort beendet werden können | Chaos Monkey disable, NetworkPolicy zurücksetzen | Pflicht |
| Runbook | Team muss wissen, was im Fehlerfall zu tun ist | Link im Experimentdokument | Pflicht |
| Verantwortliche | Experiment braucht Owner und Beobachter | Tech Lead + SRE | Pflicht |
| Kommunikationskanal | Alle Beteiligten müssen erreichbar sein | Incident-/GameDay-Channel | Pflicht |
| Zeitfenster | Experiment nur in geeignetem Fenster | Kernarbeitszeit, Team verfügbar | Pflicht |
| Datenklassifikation | Keine sensiblen Nutzerdaten in Chaos-Logs | keine E-Mail im Experimentlog | Pflicht |

Ohne diese Voraussetzungen wird kein Chaos-Experiment durchgeführt.

---

## 8. Gute Anwendung: Experiment statt Zufallstest

### 8.1 Experiment-Template

```markdown
# Chaos-Experiment: Payment Provider nicht erreichbar

## Hypothese
Wenn der Payment Provider für 60 Sekunden nicht erreichbar ist,
dann bleibt der Order Service verfügbar,
der Circuit Breaker öffnet innerhalb von 10 Sekunden,
und neue Bestellungen werden im Status PAYMENT_PENDING gespeichert.

## Umgebung
Staging.

## Betroffene Systeme
- order-service
- payment-adapter
- payment-provider-sandbox

## Steady State vor Start
- Order API Error Rate < 0,5 %
- P95 Latenz < 500 ms
- Circuit Breaker `paymentProvider` ist CLOSED
- Queue Backlog < 100 Events

## Störung
Payment Provider wird per NetworkPolicy für 60 Sekunden blockiert.

## Blast Radius
Nur Staging, nur Testtenant `tenant-chaos-001`, nur synthetische Testdaten.

## Beobachtete Metriken
- HTTP 5xx Rate
- P95/P99 Latenz
- Circuit-Breaker-State
- Fallback-Aufrufe
- Queue Backlog
- Recovery-Zeit

## Stopbedingungen
- 5xx Rate > 5 % für 2 Minuten
- P95 > 3 Sekunden für 2 Minuten
- Queue Backlog > 10.000
- manuelle Abbruchentscheidung durch Experiment Owner

## Erwartetes Verhalten
- Circuit Breaker öffnet innerhalb von 10 Sekunden
- API liefert kontrollierte fachliche Antwort
- keine ungefangenen Exceptions im Controller
- kein vollständiger Service-Ausfall
- System kehrt nach Störung innerhalb von 2 Minuten zum Steady State zurück

## Ergebnis
[Nach Durchführung ausfüllen]

## Maßnahmen
[Nach Durchführung ausfüllen]
```

### 8.2 Warum diese Struktur gut ist

Diese Struktur trennt Erwartung, Durchführung und Ergebnis. Dadurch wird Chaos Engineering zu einem lernenden Verfahren. Das Team kann später nachvollziehen, ob Resilience-Mechanismen tatsächlich funktioniert haben oder ob nur zufällig kein Fehler sichtbar wurde.

---

## 9. Schlechte Anwendung: Chaos ohne Hypothese

```text
Wir aktivieren einfach mal Chaos Monkey und schauen, was passiert.
```

Das ist kein Chaos Engineering. Das ist unkontrolliertes Stören.

Warum ist das schlecht?

- Es gibt keinen definierten Erfolgszustand.
- Es gibt keine klare Abbruchbedingung.
- Es ist unklar, welche Metriken relevant sind.
- Es ist unklar, wer entscheidet, wann gestoppt wird.
- Es erzeugt Angst vor dem Verfahren statt Vertrauen in das System.
- Es produziert Daten, aber keine belastbare Erkenntnis.

Korrekt ist:

```text
Wir prüfen gezielt, ob der Order Service bei 2 Sekunden Payment-Latenz
innerhalb definierter SLO-Grenzen bleibt und ob der Timeout korrekt greift.
```

---

## 10. Chaos Monkey for Spring Boot

Chaos Monkey for Spring Boot ist ein Werkzeug, um in Spring-Boot-Anwendungen kontrolliert Latenz, Exceptions, CPU-Last, Speicherlast oder Anwendungskill-Szenarien auszulösen. Die aktuelle Referenzdokumentation beschreibt Version 4.0.0 und zeigt, dass Chaos Monkey entweder als reguläre Dependency oder als externe Dependency eingebunden werden kann.

### 10.1 Dependency

```kotlin
// build.gradle.kts
implementation("de.codecentric:chaos-monkey-spring-boot:4.0.0")
```

Für Maven:

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>chaos-monkey-spring-boot</artifactId>
    <version>4.0.0</version>
</dependency>
```

Die Version MUSS gegen die eingesetzte Spring-Boot-Version geprüft werden. Chaos Monkey for Spring Boot weist selbst darauf hin, dass jede Version für eine bestimmte Spring-Boot-Version gebaut ist und eine falsche Kombination brechen kann.

### 10.2 Aktivierung nur über separates Profil

```yaml
spring:
  profiles:
    active: chaos-monkey

chaos:
  monkey:
    enabled: true
    watcher:
      service: true
    assaults:
      latencyActive: true
      latencyRangeStart: 1000
      latencyRangeEnd: 3000
      exceptionsActive: false
      killApplicationActive: false
```

In produktionsnahen Umgebungen MUSS Chaos Monkey über ein separates Profil, eine separate Deployment-Variante oder eine ausdrücklich kontrollierte Runtime-Konfiguration aktiviert werden. Es darf nicht versehentlich in `prod` aktiv sein.

### 10.3 Watchers

Chaos Monkey for Spring Boot arbeitet mit Watchers. Diese beobachten Spring-Komponenten und können Angriffe auf öffentliche Methoden anwenden.

Typische Watchers:

```yaml
chaos:
  monkey:
    watcher:
      controller: false
      restController: true
      service: true
      repository: false
      component: false
      restTemplate: false
      webClient: false
      actuatorHealth: false
```

Für erste Experimente SOLLTE mit `service: true` begonnen werden. Controller- und Repository-Watchers erhöhen den Blast Radius und dürfen nur bewusst aktiviert werden.

### 10.4 Assaults

```yaml
chaos:
  monkey:
    assaults:
      level: 3
      latencyActive: true
      latencyRangeStart: 1000
      latencyRangeEnd: 3000
      exceptionsActive: false
      killApplicationActive: false
      memoryActive: false
      cpuActive: false
```

`level` steuert, wie häufig Angriffe ausgelöst werden. Niedrige Werte müssen im Experimentplan erklärt werden. Für erste Experimente gilt: geringe Intensität, kurzer Zeitraum, kleiner Scope.

### 10.5 Actuator-Endpunkte absichern

Chaos Monkey bietet Actuator-Endpunkte wie:

```text
GET  /actuator/chaosmonkey
GET  /actuator/chaosmonkey/status
POST /actuator/chaosmonkey/enable
POST /actuator/chaosmonkey/disable
GET  /actuator/chaosmonkey/watchers
POST /actuator/chaosmonkey/watchers
GET  /actuator/chaosmonkey/assaults
POST /actuator/chaosmonkey/assaults
POST /actuator/chaosmonkey/assaults/runtime/attack
```

Diese Endpunkte sind sicherheitskritisch. Sie dürfen niemals öffentlich erreichbar sein. Zugriff ist nur über interne Netze, starke Authentifizierung, rollenbasierte Berechtigung und Audit Logging zulässig.

---

## 11. Sichere Beispielkonfiguration für Staging

```yaml
spring:
  config:
    activate:
      on-profile: chaos-monkey

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,chaosmonkey
  endpoint:
    chaosmonkey:
      enabled: true

chaos:
  monkey:
    enabled: true
    watcher:
      service: true
      repository: false
      restController: false
      component: false
      restTemplate: false
      webClient: false
      actuatorHealth: false
    assaults:
      level: 5
      latencyActive: true
      latencyRangeStart: 500
      latencyRangeEnd: 1500
      exceptionsActive: false
      killApplicationActive: false
      memoryActive: false
      cpuActive: false
```

Diese Konfiguration eignet sich für erste Staging-Experimente, weil sie nur Service-Methoden betrifft, Latenz statt harter Fehler injiziert und keine App-Kill-, Speicher- oder CPU-Angriffe aktiviert.

---

## 12. Resilience-Patterns, die durch Chaos geprüft werden

| Pattern | Was wird geprüft? | Typische Störung | Erfolgskriterium |
|---|---|---|---|
| Timeout | Bricht der Client rechtzeitig ab? | externe API antwortet langsam | Anfrage hängt nicht unendlich |
| Retry | Wird nur sinnvoll wiederholt? | transienter 503 | begrenzte Wiederholung, kein Retry-Sturm |
| Circuit Breaker | Öffnet der Schutzmechanismus rechtzeitig? | wiederholte Fehler | State wechselt OPEN / HALF_OPEN / CLOSED korrekt |
| Bulkhead | Wird Ressourcenverbrauch isoliert? | Payment hängt | andere Funktionen bleiben verfügbar |
| Rate Limiter | Wird Überlast begrenzt? | Traffic-Spike | kontrollierte Ablehnung statt Kollaps |
| Fallback | Gibt es fachlich sinnvolle Degradation? | Provider down | Pending-Status, Cache, Ersatzantwort |
| Idempotenz | Sind Wiederholungen ungefährlich? | Retry nach Timeout | keine Doppelbuchung, keine Dubletten |
| Queue Recovery | Erholt sich asynchrone Verarbeitung? | Consumer-Ausfall | Backlog wird abgebaut |

Resilience4j unterstützt unter anderem Circuit Breaker, Retry, RateLimiter, Bulkhead und TimeLimiter. In Spring-Boot-Anwendungen können diese Instanzen über `application.yml` konfiguriert werden.

---

## 13. Beispiel: Payment-Latenz mit Circuit Breaker prüfen

### 13.1 Resilience4j-Konfiguration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentProvider:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
  timelimiter:
    instances:
      paymentProvider:
        timeoutDuration: 2s
        cancelRunningFuture: true
```

### 13.2 Erwartetes Verhalten

```text
Wenn der Payment Provider länger als 2 Sekunden antwortet,
dann greift der TimeLimiter.
Wenn mehrere Aufrufe fehlschlagen,
dann öffnet der Circuit Breaker.
Der Order Service speichert die Bestellung im Status PAYMENT_PENDING.
```

### 13.3 Prüfpunkte

```text
- P95 Latenz Order API
- Anzahl TimeLimiter-Fehler
- Circuit-Breaker-State
- Anzahl Pending Orders
- keine doppelten Payment-Versuche
- keine Klartext-PII in Logs oder Traces
```

---

## 14. Observability-Standard

Chaos Engineering ohne Observability ist blind. Mindestens folgende Signale sind erforderlich:

| Signal | Details/Erklärung | Beispiel | Pflicht? |
|---|---|---|---|
| RED-Metriken | Rate, Errors, Duration | HTTP Rate, 5xx, P95 | Ja |
| USE-Metriken | Utilization, Saturation, Errors | CPU, Memory, Thread Pools, Connection Pools | Ja |
| Resilience-Metriken | Zustand von Circuit Breakern, Retries, Bulkheads | `resilience4j_circuitbreaker_state` | Ja |
| Business-Metriken | Fachlicher Schaden oder Degradation | Orders pending, failed payments | Ja |
| Logs | Ereignisse mit Korrelation, aber ohne sensible Daten | correlationId, tenantId | Ja |
| Traces | Request-Pfade und Latenzquellen | OTel spans | Ja |
| Alerts | Schwellenwerte für Abbruch und Recovery | Error Rate > 5 % | Ja |

OpenTelemetry Collector kann Attribute in Telemetriedaten hinzufügen, ändern oder löschen. Dadurch können sensible Attribute vor der Weiterleitung entfernt werden.

Beispiel:

```yaml
processors:
  attributes/redact-chaos-sensitive-data:
    actions:
      - key: http.request.header.authorization
        action: delete
      - key: http.request.header.cookie
        action: delete
      - key: user.email
        action: delete
      - key: customer.iban
        action: delete
```

---

## 15. SaaS- und Security-Aspekte

Chaos Engineering in SaaS-Systemen muss mandantenfähig gedacht werden. Ein Experiment darf nicht unbegrenzt alle Mandanten, alle Nutzer oder alle Regionen betreffen.

### 15.1 Tenant-spezifischer Blast Radius

Erste Experimente werden nur mit synthetischen Testmandanten durchgeführt:

```text
tenant-chaos-001
user-chaos-001
order-chaos-001
```

Produktive Kundendaten werden nicht für Experimente verwendet, wenn ein synthetisches Szenario ausreicht.

### 15.2 Feature Flags für Chaos-Aktivierung

Chaos-Aktivierung darf nicht hart im Code stehen. Für kontrollierte Experimente kann ein statisches oder dynamisches Betriebsflag eingesetzt werden:

```yaml
operations:
  chaos-experiments:
    enabled: false
    allowed-tenant: tenant-chaos-001
```

Dieses Flag ersetzt nicht die Zugriffskontrolle auf Chaos-Endpunkte. Es ist nur eine zusätzliche Betriebsbremse.

### 15.3 Actuator-Endpunkte

Chaos-Endpunkte sind administrative Funktionen. Sie müssen geschützt werden durch:

- interne Netzgrenzen,
- Authentifizierung,
- Rollenprüfung,
- Audit Logging,
- keine Exposition über öffentliche Ingress-Regeln,
- keine Aktivierung in Standard-Produktionsprofilen.

### 15.4 Logs und Traces

Chaos-Experimente dürfen keine personenbezogenen Daten in Logs oder Traces schreiben. Experiment-Logs müssen technische IDs, Correlation IDs und synthetische Testdaten verwenden.

Schlecht:

```java
log.warn("CHAOS: delaying payment for user {}", user.email());
```

Gut:

```java
log.warn("CHAOS: delaying payment", kv("tenantId", tenantId), kv("correlationId", correlationId));
```

### 15.5 Ressourcenerschöpfung

CPU-, Speicher-, Verbindungs- und Netzwerkexperimente können Noisy-Neighbor-Effekte erzeugen. In Multi-Tenant-Systemen müssen Ressourcenlimits, Pod-Limits, Bulkheads, Rate Limits und Queue-Grenzen vor solchen Experimenten geprüft werden.

---

## 16. Schlechte Anwendung und Anti-Patterns

### 16.1 Chaos in Produktion ohne Staging-Erfahrung

```text
Wir starten direkt mit Production, weil nur Production realistisch ist.
```

Falsch. Die Principles of Chaos Engineering bevorzugen produktionsnahe beziehungsweise produktive Bedingungen, weil Systeme dort anders reagieren können. Daraus folgt aber nicht, dass unreife Teams ohne Vorbereitung direkt Production stören dürfen. Der Reifegrad entscheidet über die Umgebung.

### 16.2 Keine Stopbedingung

```text
Wir lassen das Experiment 30 Minuten laufen und schauen danach.
```

Falsch. Jedes Experiment braucht harte Stopbedingungen.

### 16.3 Zu großer Blast Radius

```text
Alle Services, alle Mandanten, alle Regionen.
```

Falsch. Experimente beginnen klein.

### 16.4 Chaos als Ersatz für Tests

```text
Wir brauchen keine Integrationstests, wir machen Chaos Engineering.
```

Falsch. Chaos Engineering ergänzt Unit Tests, Integrationstests, Contract Tests, Lasttests und Security Tests. Es ersetzt sie nicht.

### 16.5 Kill Application als erstes Experiment

```yaml
chaos:
  monkey:
    assaults:
      killApplicationActive: true
```

Falsch als Einstieg. Harte Experimente kommen erst, wenn Latenz-, Exception- und Provider-Ausfall-Szenarien beherrscht werden.

### 16.6 Experimente ohne Maßnahmen

```text
Experiment fehlgeschlagen. Nächste Woche wiederholen.
```

Falsch. Jedes fehlgeschlagene Experiment muss Maßnahmen erzeugen oder bewusst als akzeptiertes Restrisiko dokumentiert werden.

---

## 17. Experimentbibliothek

### 17.1 Empfohlene Reihenfolge

| Stufe | Experiment | Umgebung | Risiko | Ziel |
|---:|---|---|---|---|
| 1 | Latenz im abhängigen Service | Staging | niedrig | Timeouts prüfen |
| 2 | kontrollierte Exceptions | Staging | niedrig bis mittel | Error Handling prüfen |
| 3 | externer Provider nicht erreichbar | Staging | mittel | Circuit Breaker prüfen |
| 4 | Queue Consumer gestoppt | Staging | mittel | Backpressure und Recovery prüfen |
| 5 | Datenbank-Latenz | Staging | mittel | Connection Pool und Timeouts prüfen |
| 6 | Pod-Restart | Staging / Canary | mittel | Kubernetes Readiness prüfen |
| 7 | zonaler Ausfall | Pre-Production / Production kontrolliert | hoch | Failover prüfen |

### 17.2 Payment Provider nicht erreichbar

```text
Störung:
Payment Sandbox wird per NetworkPolicy blockiert.

Erwartung:
Order API bleibt verfügbar.
Bestellungen werden als PAYMENT_PENDING gespeichert.
Circuit Breaker öffnet.
Keine Doppelbuchung.
```

### 17.3 Datenbank langsam

```text
Störung:
Repository- oder Service-Methoden erhalten 1–3 Sekunden Latenz.

Erwartung:
Timeouts greifen.
Connection Pool wird nicht erschöpft.
API liefert kontrollierte Fehler oder Fallbacks.
```

### 17.4 Notification-Service fällt aus

```text
Störung:
Mail-/Push-Provider wirft Exceptions.

Erwartung:
Kerntransaktion bleibt konsistent.
Notification wird retryfähig persistiert.
Keine fachliche Operation scheitert nur wegen optionaler Benachrichtigung.
```

### 17.5 Search-Index nicht verfügbar

```text
Störung:
Search-Adapter nicht erreichbar.

Erwartung:
Schreibpfad bleibt konsistent.
Reindex-Event wird später verarbeitet.
Nutzer erhält degradierte, aber kontrollierte Antwort.
```

---

## 18. Pipeline und regelmäßige Experimente

Chaos Engineering kann teilautomatisiert werden. Vollautomatisierung darf jedoch erst erfolgen, wenn Experimente mehrfach manuell erfolgreich und sicher durchgeführt wurden.

Beispiel für eine Staging-Pipeline:

```yaml
name: Weekly Chaos Engineering

on:
  schedule:
    - cron: '0 9 * * 3'

jobs:
  chaos-staging:
    runs-on: ubuntu-latest
    environment: staging

    steps:
      - name: Verify steady state
        run: ./scripts/verify-steady-state.sh

      - name: Enable payment latency assault
        run: |
          curl -X POST "$CHAOS_ENDPOINT/actuator/chaosmonkey/assaults" \
            -H "Authorization: Bearer $CHAOS_TOKEN" \
            -H "Content-Type: application/json" \
            -d '{
              "level": 5,
              "latencyActive": true,
              "latencyRangeStart": 2000,
              "latencyRangeEnd": 5000,
              "exceptionsActive": false,
              "killApplicationActive": false
            }'

      - name: Run observation window
        run: sleep 300

      - name: Disable chaos
        if: always()
        run: |
          curl -X POST "$CHAOS_ENDPOINT/actuator/chaosmonkey/disable" \
            -H "Authorization: Bearer $CHAOS_TOKEN"

      - name: Verify recovery
        run: ./scripts/verify-steady-state.sh

      - name: Generate report
        if: always()
        run: ./scripts/generate-chaos-report.sh
```

Wichtig: `if: always()` ist beim Disable-Schritt Pflicht, damit Chaos auch bei vorherigem Pipelinefehler beendet wird.

---

## 19. GameDay-Standard

Ein GameDay ist ein geplantes Teamformat für größere Resilienzexperimente.

### 19.1 Agenda

```text
Dauer: 2 Stunden

1. Vorbereitung — 30 Minuten
   - Hypothese lesen
   - Dashboards öffnen
   - Runbooks prüfen
   - Verantwortlichkeiten klären
   - Stopbedingungen bestätigen

2. Experiment — 60 Minuten
   - Baseline messen
   - Störung einführen
   - System beobachten
   - Teamreaktion dokumentieren
   - Experiment stoppen oder auslaufen lassen

3. Auswertung — 30 Minuten
   - Was hat funktioniert?
   - Was war unerwartet?
   - Welche Signale fehlten?
   - Welche Runbooks waren unklar?
   - Welche Maßnahmen entstehen?
```

### 19.2 Rollen

| Rolle | Verantwortung |
|---|---|
| Experiment Owner | entscheidet Start, Stop und Scope |
| Driver | führt technische Schritte aus |
| Observer | beobachtet Dashboards und Alerts |
| Scribe | dokumentiert Timeline, Beobachtungen und Maßnahmen |
| Service Owner | bewertet fachliche Auswirkungen |
| Incident Lead | übernimmt, wenn Experiment außer Kontrolle gerät |

---

## 20. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Hypothese | Ist das erwartete Verhalten klar prüfbar? | Circuit Breaker öffnet < 10s | Pflicht |
| Steady State | Sind Normalmetriken definiert? | P95 < 500ms, Error Rate < 0,5% | Pflicht |
| Scope | Ist das Zielsystem klar begrenzt? | order-service, payment-adapter | Pflicht |
| Umgebung | Ist die Umgebung geeignet? | Staging vor Production | Pflicht |
| Blast Radius | Ist der Wirkungsbereich klein genug? | ein Tenant, 1 % Traffic | Pflicht |
| Stopbedingung | Gibt es harte Abbruchkriterien? | Error Rate > 5 % | Pflicht |
| Disable | Kann Chaos sicher beendet werden? | `/actuator/chaosmonkey/disable` | Pflicht |
| Observability | Sind Dashboards und Alerts bereit? | Grafana, Prometheus, Traces | Pflicht |
| Security | Sind Endpunkte geschützt? | kein öffentlicher Actuator | Pflicht |
| Daten | Werden synthetische oder minimale Daten genutzt? | Testtenant | Pflicht |
| Ergebnis | Wird das Experiment dokumentiert? | Report mit Maßnahmen | Pflicht |
| Maßnahmen | Gibt es Owner und Deadline? | Ticket mit Verantwortlichem | Pflicht |

---

## 21. Automatisierbare Prüfungen

Mögliche Automatisierungen:

```text
- Build schlägt fehl, wenn Chaos Monkey Dependency in produktiven Profilen aktiv ist.
- CI prüft, dass chaos-monkey nur in allowlisted Environments aktiviert werden kann.
- Kubernetes Policy verhindert öffentliche Exposition von /actuator/chaosmonkey.
- Deployment prüft, ob Chaos-Endpunkte hinter Authentifizierung liegen.
- Pipeline verifiziert Steady State vor Experimentstart.
- Pipeline deaktiviert Chaos immer im finalen Cleanup-Schritt.
- Alert prüft, ob chaos.monkey.* Metriken außerhalb erlaubter Zeitfenster auftreten.
- OTel Collector entfernt sensible Attribute aus Chaos-bezogenen Traces.
```

Beispielhafte Policy-Idee:

```text
Wenn SPRING_PROFILES_ACTIVE den Wert chaos-monkey enthält,
dann muss ENVIRONMENT in [staging, preprod, chaos-lab] liegen.
```

---

## 22. Migration und Einführung

Chaos Engineering wird schrittweise eingeführt.

### 22.1 Phase 1 — Vorbereitung

- Kritische User Journeys identifizieren.
- SLOs oder Steady-State-Metriken definieren.
- Dashboards und Alerts prüfen.
- Resilience-Patterns dokumentieren.
- Runbooks ergänzen.

### 22.2 Phase 2 — Staging-Experimente

- Latenzexperimente für nicht-kritische Abhängigkeiten.
- Exception-Experimente für externe Adapter.
- Provider-Ausfall im Staging.
- Ergebnisberichte erstellen.

### 22.3 Phase 3 — Automatisierung

- wiederkehrende Experimente in Staging,
- standardisierte Reports,
- automatische Steady-State-Prüfung,
- automatische Cleanup-Schritte.

### 22.4 Phase 4 — Kontrollierte Production-Experimente

Production-Experimente dürfen erst nach nachweislich erfolgreichen Staging-Experimenten erfolgen. Sie beginnen mit kleinstmöglichem Blast Radius und synthetischem oder internem Traffic.

---

## 23. Ausnahmen

Chaos Engineering darf ausgesetzt werden, wenn:

- Observability fehlt,
- ein aktueller Incident läuft,
- kritische Releases aktiv stabilisiert werden,
- kein Owner verfügbar ist,
- Stopmechanismus unklar ist,
- Sicherheitsfreigaben fehlen,
- das Experiment echte Kundendaten unnötig gefährden würde,
- das Team die Auswirkungen nicht beobachten kann.

Eine Ausnahme muss dokumentiert werden, damit fehlende Voraussetzungen nicht dauerhaft unsichtbar bleiben.

---

## 24. Definition of Done

Ein Chaos-Experiment erfüllt diese Richtlinie, wenn es eine prüfbare Hypothese besitzt, der Steady State vorab definiert wurde, die Störung realistisch und begrenzt ist, der Blast Radius klein genug ist, Stopbedingungen und Rollback-Mechanismus existieren, Observability verfügbar ist, Security- und SaaS-Risiken geprüft wurden, das Experiment dokumentiert wurde und aus dem Ergebnis entweder bestätigte Resilienz oder konkrete Verbesserungsmaßnahmen abgeleitet wurden.

Ein Chaos-Engineering-Setup erfüllt diese Richtlinie, wenn Chaos-Aktivierung nicht versehentlich in Produktion erfolgen kann, Actuator- oder Steuerendpunkte geschützt sind, Metriken und Traces verfügbar sind, GameDays wiederholbar geplant werden können und Experimente nicht als Ersatz für andere Testarten missverstanden werden.

---

## 25. Quellen und weiterführende Literatur

| Quelle | Relevanz |
|---|---|
| Principles of Chaos Engineering | Grundprinzipien: Steady State, reale Ereignisse, Production-Nähe, Automatisierung, Blast Radius |
| Chaos Monkey for Spring Boot Reference Guide | Konfiguration, Dependency, Profilaktivierung, Actuator-Endpunkte, Watchers, Assaults, Metriken |
| Spring Boot Actuator Documentation | Management-Endpunkte, Health, Metrics, Exposition und Zugriffsschutz |
| Resilience4j Documentation | Circuit Breaker, Retry, Rate Limiter, Bulkhead, TimeLimiter und Spring-Boot-Konfiguration |
| OpenTelemetry Collector Documentation | Entfernen, Aktualisieren und Anreichern von Telemetrieattributen |
| OWASP Logging Cheat Sheet | Schutz sensibler Daten in Logs und sicherheitsrelevanten Ereignissen |

