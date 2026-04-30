# ADR-117 — JFR & Async-Profiler: Produktions-Performance verstehen

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Observability · Performance · JVM                             |
| Betroffene Teams  | Alle Backend-Teams, SRE                                       |
| Abhängigkeiten    | ADR-043 (JVM-Tuning), ADR-102 (OTel), ADR-054 (SLO)          |

---

## 1. Wann Profiling nötig ist

```
METRIKEN (Prometheus/OTel → ADR-102): zeigen WAS langsam ist
  "P95-Latenz ist 800ms" — aber WARUM?

PROFILING: zeigt WARUM etwas langsam ist
  "800ms: davon 650ms in JSON-Serialisierung, 150ms in DB-Query"
  → Zielgerichtete Optimierung statt Raten

ZWEI TOOLS:
  JFR (Java Flight Recorder): eingebaut in JVM, Overhead < 1%
  Async-Profiler: sampling-basiert, tiefere Stack-Traces, CPU-Profile
```

---

## 2. Entscheidung

JFR wird dauerhaft in Produktion aktiviert (Overhead < 1%). Async-Profiler
wird für tiefe Performance-Analysen bei Incidents eingesetzt (temporär).
Beide erzeugen Flame-Graphs für visuelle Analyse.

---

## 3. JFR: Dauerhaftes Profiling in Produktion

### 3.1 Aktivierung

```dockerfile
# Dockerfile: JFR in Produktion aktivieren
ENTRYPOINT ["java",
    # JFR immer aktiv, kontinuierliche Aufzeichnung
    "-XX:+FlightRecorder",
    "-XX:StartFlightRecording=name=continuous,duration=0,maxage=1h,maxsize=500m,filename=/tmp/jfr/",

    # Bei OutOfMemoryError: Dump erzeugen
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:HeapDumpPath=/tmp/heapdumps/",

    "-jar", "/app/app.jar"]
```

```yaml
# Kubernetes: JFR-Verzeichnis als emptyDir mounten
spec:
  containers:
    - name: order-service
      volumeMounts:
        - name: jfr-storage
          mountPath: /tmp/jfr
        - name: heap-dumps
          mountPath: /tmp/heapdumps
  volumes:
    - name: jfr-storage
      emptyDir:
        medium: Memory   # RAM-basiert: keine Disk-I/O
        sizeLimit: 500Mi
    - name: heap-dumps
      emptyDir:
        sizeLimit: 2Gi
```

### 3.2 JFR-Daten aus laufendem Prozess laden

```bash
# JFR-Recording aus laufendem Pod holen

# 1. Pod identifizieren
kubectl get pods -n production | grep order-service

# 2. JFR-Dump erzeugen (last 60 seconds)
kubectl exec order-service-pod -- \
    jcmd 1 JFR.dump \
    name=incident-$(date +%Y%m%d-%H%M%S) \
    filename=/tmp/jfr/incident.jfr \
    duration=60s

# 3. Datei herunterladen
kubectl cp order-service-pod:/tmp/jfr/incident.jfr ./incident.jfr

# 4. In JDK Mission Control öffnen
jmc incident.jfr
```

---

## 4. Async-Profiler: CPU und Memory Flame-Graphs

```bash
# Async-Profiler im laufenden Container nutzen
# (temporär bei Incidents, nicht dauerhaft)

# 1. Profiler in Container kopieren
kubectl cp async-profiler-3.0-linux-x64.tar.gz \
    order-service-pod:/tmp/

# 2. CPU-Profiling für 30 Sekunden
kubectl exec -it order-service-pod -- bash -c "
    cd /tmp && tar xf async-profiler-*.tar.gz
    ./async-profiler/bin/asprof \
        -d 30 \
        -f /tmp/flamegraph.html \
        --web \
        1
"

# 3. Flame-Graph herunterladen
kubectl cp order-service-pod:/tmp/flamegraph.html ./flamegraph.html
open flamegraph.html
```

### 4.1 Flame-Graph lesen

```
WAS IST EIN FLAME-GRAPH:
  Y-Achse: Call-Stack (oben = die eigentliche Arbeit)
  X-Achse: Zeit (Breite = wie viel % der Zeit dort verbracht)
  
  BREITE = WICHTIGKEIT
  Breite eines Blocks = % der Gesamtlaufzeit die in dieser Methode verbracht wird
  
  Suche nach FLACHEN, BREITEN Blöcken:
  → Das sind die Bottlenecks
  
TYPISCHE BEFUNDE:
  "JacksonSerializer.serialize" nimmt 60% der Zeit
  → JSON-Serialisierung optimieren: weniger Felder, Binary-Format
  
  "HikariPool.getConnection" nimmt 30% der Zeit
  → Connection-Pool zu klein (→ ADR-043 HikariCP)
  
  "StringConcatenation.append" nimmt 20% der Zeit
  → StringBuilder statt String-Konkatenation in Hotpath
```

---

## 5. Spring Boot Actuator: JFR-Integration

```java
// Actuator-Endpoint: JFR-Recording über HTTP starten/stoppen
// Nützlich für automatisierte Performance-Tests

@RestController
@RequestMapping("/actuator/jfr")
@PreAuthorize("hasRole('ADMIN')")
public class JfrController {

    @PostMapping("/start")
    public ResponseEntity<String> startRecording(
            @RequestParam(defaultValue = "60") int durationSeconds) {

        var recordingName = "incident-" + Instant.now().toEpochMilli();
        var cmd = "JFR.start name=" + recordingName +
                  " duration=" + durationSeconds + "s" +
                  " filename=/tmp/jfr/" + recordingName + ".jfr";

        // jcmd an den eigenen Prozess senden
        var pid = ProcessHandle.current().pid();
        runJcmd(pid, cmd);

        return ResponseEntity.ok("Recording gestartet: " + recordingName);
    }
}
```

---

## 6. Kontinuierliches Profiling mit Pyroscope

```yaml
# Kubernetes: Pyroscope Sidecar für kontinuierliches Profiling
spec:
  containers:
    - name: order-service
      # ...
      env:
        - name: PYROSCOPE_SERVER_ADDRESS
          value: http://pyroscope:4040
        - name: PYROSCOPE_APPLICATION_NAME
          value: order-service
        - name: PYROSCOPE_PROFILING_INTERVAL
          value: "10s"

    # Pyroscope Agent als Sidecar (oder Java Agent)
    - name: pyroscope-agent
      image: grafana/pyroscope:latest
      args: ["--config.file=/etc/pyroscope/config.yaml"]
```

---

## 7. Konkrete Optimierungs-Workflows

```
WORKFLOW: "P95-Latenz gestiegen von 200ms auf 800ms"

SCHRITT 1: OTel-Traces (→ ADR-102) anschauen
  → Welcher Span nimmt die meiste Zeit?
  → Ergebnis: "order.serialize" ist 600ms

SCHRITT 2: JFR-Recording aus dem Zeitraum holen
  → Welche Methoden wurden aufgerufen?
  → Ergebnis: ObjectMapper.writeValueAsString() erscheint sehr häufig

SCHRITT 3: Async-Profiler Flame-Graph für 60 Sekunden
  → Wo genau in der Serialisierung ist es langsam?
  → Ergebnis: Java-Reflection in Jackson sehr häufig

SCHRITT 4: Fix identifizieren
  → Jackson-Mixin oder @JsonProperty statt Reflection
  → Oder: kleinere DTOs serialisieren

SCHRITT 5: Ergebnis messen
  → JFR vor und nach der Änderung vergleichen
  → OTel-Metrik zeigt: P95 zurück auf 180ms ✅
```

---

## 8. Akzeptanzkriterien

- [ ] JFR mit `maxage=1h` in allen Produktions-Pods aktiv (JVM-Flags in Dockerfile)
- [ ] Async-Profiler-Binaries in Standard-Container-Image vorhanden
- [ ] Flame-Graph-Analyse ist im Incident-Runbook dokumentiert
- [ ] Performance-Regressions-Test: Profiling-Ergebnisse vor/nach Releases verglichen

---

## Quellen & Referenzen

- **Erik Duveblad, "Continuous Profiling of Java" (2021)** — JFR in Produktion.
- **Brendan Gregg, "Systems Performance" (2020), Kap. 6** — Flame-Graphs: Interpretation und Erstellung.
- **Aleksey Shipilev, JVM Performance Resources** — Tiefe JVM-Optimierung.
- **async-profiler GitHub** — github.com/async-profiler/async-profiler

---

## Verwandte ADRs

- [ADR-043](ADR-043-jvm-tuning-hikaricp.md) — JVM-Grundkonfiguration
- [ADR-102](ADR-102-opentelemetry-vollstaendig.md) — OTel: Traces zeigen WAS langsam ist
- [ADR-054](ADR-054-slo-sla-alerting.md) — SLO: Performance-Schwellwerte
