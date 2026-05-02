# ADR-072 — Chaos Engineering: Resilienz durch kontrollierte Experimente

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Chaos Monkey |
| Datum      | 2024-01-01                        |
| Kategorie  | Testing / Resilience              |

---

## Kontext & Problem

Resilience-Patterns (→ ADR-022) sind im Code vorhanden — aber funktionieren sie auch wirklich unter echten Ausfallbedingungen? Chaos Engineering ist die Disziplin, Fehler absichtlich in Produktionssysteme einzuführen, um Schwachstellen zu finden bevor echte Ausfälle sie aufdecken. "Hope is not a strategy."

---

## Chaos Engineering Prinzipien (nach Netflix)

```
1. Steady State Hypothesis:
   Definiere was "normal" ist (Error Rate < 0.5%, P95 < 500ms)

2. Introduce Variables:
   Simuliere realistische Ausfälle (Server-Crash, Netzwerk-Latenz, Disk full)

3. Run Experiments in Production (oder Staging):
   Kleine kontrollierte Experimente, nicht große Katastrophen

4. Automate Experiments:
   Regelmäßig, nicht nur einmalig

5. Minimize Blast Radius:
   Beginne klein (1% Traffic), eskaliere vorsichtig
```

---

## Chaos Monkey for Spring Boot

```kotlin
// build.gradle.kts
implementation("de.codecentric:chaos-monkey-spring-boot:3.1.0")
```

```yaml
# application.yml — Chaos Monkey Konfiguration
chaos:
  monkey:
    enabled: true   # Nur in Chaos-Experiment-Umgebung!
    watcher:
      controller:   true
      restController: true
      service:      true
      repository:   true
    assaults:
      level: 3        # 1 von 3 Requests betroffen
      latencyActive:  true
      latencyRangeStart: 1000   # 1 Sekunde min
      latencyRangeEnd:   3000   # 3 Sekunden max
      exceptionsActive: false   # Exceptions separat konfiguriert
      killApplicationActive: false
```

```java
// Programmatische Konfiguration: Chaos per REST-API steuern
// POST /actuator/chaosmonkey/assaults
{
  "level": 5,
  "latencyActive": true,
  "latencyRangeStart": 2000,
  "latencyRangeEnd": 5000,
  "exceptionsActive": false
}

// Chaos stoppen:
// POST /actuator/chaosmonkey/disable
```

---

## Strukturierte Chaos-Experimente

```markdown
## Experiment Template

### Hypothese
Wenn der Payment Service für 30 Sekunden nicht erreichbar ist,
zeigt die Order-Platzierung eine benutzerfreundliche Fehlermeldung
und der Circuit Breaker öffnet sich nach 3 fehlgeschlagenen Versuchen.

### Erwarteter Zustand (Steady State)
- Error Rate: < 0.5%
- P95 Latenz: < 500ms
- Circuit Breaker: CLOSED

### Durchführung
1. Baseline messen: Error Rate, Latenz, Circuit Breaker Status
2. Chaos einführen: Payment Service unavailable (NetworkPolicy blocken)
3. Beobachten: Wie verhält sich das System?
4. Steady State wiederherstellen: NetworkPolicy zurücksetzen
5. Erholung messen: Wie lange bis Steady State zurück?

### Erwartetes Verhalten
- Circuit Breaker öffnet nach 3 Fehlern (< 10 Sekunden)
- Fallback-Methode aktiv: Order in Pending-Zahlungs-Status
- Error Rate steigt kurz, sinkt dann durch Fallback
- Kein vollständiger Service-Ausfall

### Tatsächliches Ergebnis
[Ergebnis des Experiments dokumentieren]

### Erkenntnisse & Maßnahmen
[Was wurde gelernt? Was muss verbessert werden?]
```

---

## Chaos-Experiment-Bibliothek

```java
// Eigene Chaos-Experimente für spezifische Ausfallszenarien
@Component
@ConditionalOnProperty("chaos.experiments.enabled")
public class ChaosExperimentRunner {

    // Experiment 1: Datenbank-Verbindung unterbrechen
    public void runDatabaseFailureExperiment(Duration duration) {
        log.warn("CHAOS: Starting database failure experiment for {}", duration);
        chaosAgent.blockDatabaseConnections(duration);

        Mono.delay(duration).subscribe(__ -> {
            chaosAgent.restoreDatabaseConnections();
            log.warn("CHAOS: Database connections restored");
            verifyRecovery();
        });
    }

    // Experiment 2: Speicher-Druck erzeugen
    public void runMemoryPressureExperiment() {
        log.warn("CHAOS: Starting memory pressure experiment");
        var hog = new byte[100 * 1024 * 1024]; // 100MB belegen
        Arrays.fill(hog, (byte) 1); // Verhindern dass GC es wegräumt

        Mono.delay(Duration.ofMinutes(5)).subscribe(__ -> {
            hog[0] = 0; // Reference freigeben
            log.warn("CHAOS: Memory pressure released");
        });
    }

    // Experiment 3: CPU-Spike
    public void runCpuSpikeExperiment(Duration duration) {
        log.warn("CHAOS: Starting CPU spike experiment for {}", duration);
        var end = Instant.now().plus(duration);
        // CPU belasten bis Experiment endet
        while (Instant.now().isBefore(end)) {
            Math.sqrt(Math.random()); // CPU-Arbeit
        }
        log.warn("CHAOS: CPU spike ended");
    }

    private void verifyRecovery() {
        // Prüfen ob Steady State wiederhergestellt
        var metrics = metricsCollector.getCurrentMetrics();
        if (metrics.errorRate() > 0.01) {
            alerting.notify("CHAOS: System did not recover! Error rate: " + metrics.errorRate());
        }
    }
}
```

---

## Chaos Engineering in der Pipeline

```yaml
# .github/workflows/chaos.yml — wöchentliche Chaos-Experimente
name: Weekly Chaos Engineering

on:
  schedule:
    - cron: '0 10 * * 3'  # Mittwochs 10 Uhr (Kernarbeitszeit, Team bereit)

jobs:
  chaos-experiments:
    runs-on: ubuntu-latest
    environment: staging

    steps:
      - name: Verify Steady State
        run: ./scripts/verify-steady-state.sh
        # Schlägt fehl wenn Staging schon nicht healthy ist

      - name: Run Payment Service Latency Experiment
        run: |
          # 2 Sekunden Latenz für Payment Service, 5 Minuten lang
          kubectl exec -n staging deploy/payment-service -- \
            curl -X POST localhost:8080/actuator/chaosmonkey/assaults \
            -d '{"latencyActive":true,"latencyRangeStart":2000,"level":5}'

          sleep 300  # 5 Minuten warten

          # Chaos beenden
          kubectl exec -n staging deploy/payment-service -- \
            curl -X POST localhost:8080/actuator/chaosmonkey/disable

      - name: Verify Recovery
        run: |
          sleep 60  # 1 Minute Recovery-Zeit
          ./scripts/verify-steady-state.sh

      - name: Generate Chaos Report
        run: ./scripts/generate-chaos-report.sh
```

---

## GameDay: großes Chaos-Experiment mit dem Team

```markdown
## GameDay Agenda (2 Stunden)

### Vorbereitung (30 Min)
- Alle relevanten Dashboards öffnen
- Runbooks griffbereit
- On-Call-Kommunikation aktiv
- Blast Radius begrenzen: nur Staging oder 5% Production Traffic

### Experiment (60 Min)
- Chaos-Szenario: "Was passiert wenn das Rechenzentrum Frankfurt ausfällt?"
- Beobachten: zeigt das Team echtes Resilience-Verhalten?
- Dokumentieren: was ist unerwartet passiert?

### Retrospektive (30 Min)
- Was hat wie erwartet funktioniert?
- Was war überraschend?
- Welche Verbesserungen sind nötig?
- Action Items mit Verantwortlichen und Deadline
```

---

## Konsequenzen

**Positiv:** Schwachstellen werden in kontrollierten Experimenten gefunden, nicht im echten Ausfall. Teams werden confident im Umgang mit Ausfällen. Resilience-Patterns werden verifiziert statt nur angenommen.

**Negativ:** Erfordert reife Observability (→ ADR-017) — ohne gute Metriken ist Chaos sinnlos. Chaos in Production erfordert klares Rollback-Protokoll. Kultureller Widerstand: "Warum brechen wir unsere eigene Infrastruktur?"

---

## 💡 Guru-Tipps

- **Klein anfangen**: zuerst Chaos in Staging, dann erst Production.
- **Immer Rollback-Mechanismus** bereit haben bevor Experiment startet.
- **GameDay als Team-Event**: Chaos Engineering ist nicht nur für Ops — Entwickler müssen teilnehmen.
- **Chaos Engineering ≠ Random Testing**: jedes Experiment hat eine klare Hypothese und Messpunkt.

---

## Verwandte ADRs

- [ADR-022](ADR-022-resilience-circuit-breaker.md) — Circuit Breaker als getestetes Resilience-Pattern.
- [ADR-017](ADR-017-observability-logging-tracing.md) — Observability ist Voraussetzung für Chaos Engineering.
- [ADR-054](ADR-054-slo-sla-alerting.md) — SLOs als Steady State Definition.
