# ADR-043 — JVM Tuning, GC-Strategie & HikariCP

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2024-01-01                        |
| Kategorie  | Performance / Operations          |

---

## JVM-Flags für Produktion

```bash
# Empfohlene JVM-Flags für Spring Boot in Kubernetes (→ ADR-038)
JAVA_TOOL_OPTIONS="\
  # Container-Limits respektieren (Standard ab Java 11)
  -XX:+UseContainerSupport \
  # 75% des Container-RAM als Max-Heap
  -XX:MaxRAMPercentage=75.0 \
  # G1GC: Standard ab Java 9, gut für 4GB+ Heap
  -XX:+UseG1GC \
  # GC-Pause-Ziel: 200ms (Anpassen nach Anwendung)
  -XX:MaxGCPauseMillis=200 \
  # GC-Logs für Diagnose (rotierend)
  -Xlog:gc*:file=/tmp/gc-%t.log:time,uptime,level:filecount=5,filesize=20m \
  # Heap-Dump bei OOM für Post-Mortem-Analyse
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  # Schnellerer Zufallsgenerator (Container: /dev/random kann blockieren)
  -Djava.security.egd=file:/dev/./urandom \
  # Native Memory Tracking (für Off-Heap-Diagnose)
  -XX:NativeMemoryTracking=summary"
```

---

## GC-Auswahl nach Anwendungstyp

```
G1GC (Standard, Java 9+):
  → Web-Services, REST-APIs, Spring Boot
  → Heap: 1–32 GB
  → Pause-Ziel: 100–500ms
  → -XX:+UseG1GC -XX:MaxGCPauseMillis=200

ZGC (Java 15+ produktiv):
  → Sehr niedrige Latenzen (<10ms Pausen)
  → Heap: 8 MB – 16 TB
  → Für: Low-Latency-Services, Real-Time
  → -XX:+UseZGC

Shenandoah GC:
  → Ähnlich ZGC, concurrent compaction
  → OpenJDK (nicht Oracle JDK)
  → -XX:+UseShenandoahGC

Serial GC:
  → Single-Thread, minimal Overhead
  → Nur für: Batch-Jobs, kleine CLI-Tools
  → -XX:+UseSerialGC
```

---

## HikariCP: Connection Pool korrekt konfigurieren

```yaml
# application.yml
spring:
  datasource:
    hikari:
      # Maximale Pool-Größe: Formel: minimumIdle ≤ maximumPoolSize
      # Faustregel: Anzahl CPU-Kerne × 2 + Anzahl Spindeln
      # Für Kubernetes mit 2 vCPU: 10 ist oft gut
      maximum-pool-size: 10

      # Minimum bereit gehaltene Verbindungen
      minimum-idle: 5

      # Wie lange auf eine Verbindung warten?
      connection-timeout: 3000   # 3 Sekunden — danach Exception

      # Wie lange eine Verbindung maximal im Pool bleiben?
      max-lifetime: 600000       # 10 Minuten (< DB-Timeout!)

      # Wie lange eine idle Connection gehalten wird
      idle-timeout: 300000       # 5 Minuten

      # Verbindung testen bei Abholung aus Pool
      connection-test-query: SELECT 1

      # Pool-Name für Metriken
      pool-name: OrderServicePool

      # Metriken an Micrometer (→ ADR-017)
      register-mbeans: true
```

---

## Connection-Pool-Sizing Formel

```
Formel nach "HikariCP Wiki: About Pool Sizing":
pool_size = Tn × (Cm - 1) + 1

Tn = max. Anzahl paralleler Threads die DB-Verbindungen nutzen
Cm = max. Anzahl gleichzeitiger DB-Verbindungen pro Query-Pfad

Beispiel: Spring Boot, 10 Request-Threads, 1 DB-Verbindung pro Request
pool_size = 10 × (1 - 1) + 1 = 1? → Nein, Minimum: Tn/4 bis Tn/2

Pragmatische Faustregel:
- 1–2 vCPU Container:   pool-size = 5–10
- 4 vCPU Container:     pool-size = 15–20
- Messen: Wenn connection-timeout auftreten → zu klein
- Messen: Wenn idle > 80% → zu groß
```

---

## Off-Heap Memory: Metaspace und Direct Memory

```bash
# Metaspace: Klassen-Metadaten (kann wachsen ohne Limit → OOM)
-XX:MetaspaceSize=256m        # Initial-Größe
-XX:MaxMetaspaceSize=512m     # Maximal-Größe (IMMER setzen!)

# Direct Memory: NIO ByteBuffers, Netty (Spring WebFlux)
-XX:MaxDirectMemorySize=256m  # Maximal-Größe für Off-Heap-NIO

# Native Memory Tracking prüfen:
jcmd <PID> VM.native_memory summary
# Zeigt: Java Heap, Class, Thread, Code, GC, Internal, Other
```

---

## Diagnose: Was wenn die App zu viel Speicher verbraucht?

```bash
# 1. Heap-Analyse
jmap -histo:live <PID> | head -30          # Top-30 Objekte nach Instanzen
jcmd <PID> GC.heap_info                    # Heap-Statistik
jstat -gcutil <PID> 1000 10               # GC-Statistik, 10 Messungen à 1s

# 2. Heap-Dump erstellen und analysieren
jcmd <PID> GC.heap_dump /tmp/heap.hprof
# Analysieren mit Eclipse Memory Analyzer (MAT) oder IntelliJ Profiler

# 3. Thread-Dump (Deadlock-Diagnose → ADR-033)
jcmd <PID> Thread.print

# 4. Java Flight Recorder (JFR) — geringe Overhead (<2%)
jcmd <PID> JFR.start duration=60s filename=/tmp/recording.jfr
# Analysieren mit JDK Mission Control
```

---

## Konsequenzen

**Positiv:** Korrekte GC-Konfiguration reduziert GC-Pausen messbar. HikariCP mit korrekter Pool-Größe verhindert Connection-Timeouts unter Last. `HeapDumpOnOutOfMemoryError` ermöglicht Post-Mortem-Analyse.

**Negativ:** JVM-Flags müssen per Anwendung kalibriert werden — keine universelle Konfiguration. Connection-Pool zu groß: DB überlastet. Zu klein: Timeout.

---

## Tipps

- **`MaxRAMPercentage` statt `-Xmx`** in Containern: passt sich automatisch an Container-Limits an.
- **`max-lifetime` < DB-Connection-Timeout**: wenn HikariCP länger als DB-Timeout hält, sind Connections beim nächsten Versuch tot.
- **Metriken überwachen**: `hikaricp.connections.pending` > 0 → Pool zu klein. `hikaricp.connections.idle` > 80% → Pool zu groß.
 