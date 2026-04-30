# ADR-074 — Scheduled Jobs: Best Practices & Pitfalls

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Quartz    |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Operations          |

---

## Kontext & Problem

`@Scheduled` in Spring Boot ist bequem — und gefährlich in Multi-Instance-Deployments. Wenn drei Pod-Instanzen laufen, laufen drei Scheduled Jobs gleichzeitig. Doppelte Emails, doppelte Abrechnungen, Race Conditions. Dieses ADR regelt wann und wie Scheduled Jobs richtig implementiert werden.

---

## Das Problem: @Scheduled in Multi-Instance-Deployment

```java
// ❌ Schlecht: läuft auf JEDER Pod-Instanz!
@Service
public class DataRetentionJob {

    @Scheduled(cron = "0 0 2 * * *")  // 2 Uhr nachts
    public void deleteExpiredData() {
        // Bei 3 Pods: dieser Code läuft 3× gleichzeitig!
        // Doppeltes Löschen ist harmlos — aber:
        // Doppeltes Email-Senden ist problematisch
        // Doppeltes Abrechnen ist katastrophal
        int deleted = dataRepository.deleteExpiredRecords();
        emailService.sendRetentionReport(deleted);  // 3× gesendet!
    }
}
```

---

## Lösung 1: ShedLock — Distributed Lock für @Scheduled

```kotlin
// build.gradle.kts
implementation("net.javacrumbs.shedlock:shedlock-spring:5.13.0")
implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.13.0")
```

```sql
-- V30__create_shedlock_table.sql
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()  // DB-Zeit statt System-Zeit — konsistent in verteilten Systemen
                .build()
        );
    }
}

@Service
public class DataRetentionJob {

    @Scheduled(cron = "0 0 2 * * *")
    @SchedulerLock(
        name = "dataRetentionJob",
        lockAtLeastFor = "5m",     // Lock min 5 Min halten (verhindert Doppelstart)
        lockAtMostFor  = "23h"     // Lock max 23h (verhindert ewigen Lock bei Crash)
    )
    public void deleteExpiredData() {
        // Nur eine Instanz führt das aus — andere überspringen
        log.info("Running data retention job on {}", InetAddress.getLocalHost().getHostName());
        int deleted = dataRepository.deleteExpiredRecords();
        emailService.sendRetentionReport(deleted);
    }
}
```

---

## Lösung 2: Kubernetes CronJob — Job außerhalb der Applikation

```yaml
# Für reine Batch-Jobs die keine Applikationslogik direkt brauchen:
# Kubernetes CronJob ist oft die bessere Wahl
apiVersion: batch/v1
kind: CronJob
metadata:
  name: data-retention-job
spec:
  schedule: "0 2 * * *"   # 2 Uhr nachts (UTC)
  concurrencyPolicy: Forbid  # Neuer Job startet nicht wenn vorheriger noch läuft
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 5

  jobTemplate:
    spec:
      backoffLimit: 3   # 3 Versuche bei Fehler
      activeDeadlineSeconds: 3600  # Max 1 Stunde

      template:
        spec:
          restartPolicy: OnFailure
          containers:
            - name: data-retention
              image: ghcr.io/example/order-service:latest
              command: ["java", "-jar", "app.jar", "--spring.profiles.active=batch"]
              args: ["--job=dataRetention", "--month=2024-01"]
              env:
                - name: SPRING_BATCH_JOB_ENABLED
                  value: "true"
```

---

## @Scheduled Best Practices

```java
@Service
public class ScheduledJobService {

    // ① Immer: Fehlerbehandlung und Logging
    @Scheduled(cron = "0 */15 * * * *")  // Alle 15 Minuten
    @SchedulerLock(name = "cacheRefreshJob", lockAtMostFor = "14m")
    public void refreshCache() {
        var start = Instant.now();
        try {
            log.info("Starting cache refresh job");
            cacheService.refreshAll();
            var duration = Duration.between(start, Instant.now());
            log.info("Cache refresh completed in {}ms", duration.toMillis());
            meterRegistry.timer("job.cache.refresh.duration").record(duration);
        } catch (Exception e) {
            log.error("Cache refresh job failed", e);
            meterRegistry.counter("job.cache.refresh.errors").increment();
            // NICHT rethrow: @Scheduled fängt Exceptions, aber explizites Logging ist wichtig
        }
    }

    // ② Idempotenz: Job kann mehrfach laufen ohne Schaden
    @Scheduled(fixedDelay = 60_000)  // 60 Sekunden nach Ende des letzten Runs
    @SchedulerLock(name = "outboxProcessorJob", lockAtMostFor = "55s")
    public void processOutbox() {
        // Outbox-Verarbeitung ist natürlich idempotent (→ ADR-042)
        outboxPublisher.publishPendingEvents();
    }

    // ③ fixedDelay statt fixedRate für variable Laufzeiten
    // fixedRate: startet N ms nach Start des vorherigen Runs (auch wenn noch läuft!)
    // fixedDelay: startet N ms nach ENDE des vorherigen Runs (sicherer!)
    @Scheduled(fixedDelay = 30_000, initialDelay = 60_000)
    public void processQueue() { ... }

    // ④ Cron-Ausdrücke kommentieren (6 Felder: Sek Min Std Tag Mon Wochentag)
    @Scheduled(cron = "0 0 3 * * MON")  // Montags 3:00 Uhr
    public void weeklyReport() { ... }

    @Scheduled(cron = "0 30 8-17 * * MON-FRI")  // Werktags 8:30-17:30 alle 30 Min
    public void businessHoursSync() { ... }
}
```

---

## Monitoring: Scheduled Jobs überwachen

```java
// Prometheus: Job-Laufzeiten und Fehlerraten
@Component
public class JobMetricsCollector {

    @EventListener
    public void onScheduledJobStart(ScheduledJobStartEvent event) {
        meterRegistry.counter("scheduled.job.runs",
            "job", event.jobName()).increment();
    }

    @EventListener
    public void onScheduledJobComplete(ScheduledJobCompleteEvent event) {
        meterRegistry.timer("scheduled.job.duration",
            "job", event.jobName())
            .record(event.duration());
    }

    @EventListener
    public void onScheduledJobFailed(ScheduledJobFailedEvent event) {
        meterRegistry.counter("scheduled.job.failures",
            "job", event.jobName()).increment();
    }
}
```

```yaml
# Prometheus Alert: Job hat zu lange nicht gelaufen
- alert: ScheduledJobMissed
  expr: time() - scheduled_job_last_success_timestamp{job="dataRetentionJob"} > 90000
  # Alert wenn Job seit > 25 Stunden nicht erfolgreich war (Soll: täglich)
  annotations:
    summary: "Scheduled job {{ $labels.job }} has not run for 25+ hours"
```

---

## Job-Abhängigkeiten: Reihenfolge sicherstellen

```java
// Jobs die voneinander abhängen: explizite Orchestrierung statt Timing
@Service
public class MonthlyClosingOrchestrator {

    // Statt drei separate @Scheduled die zufällig in der falschen Reihenfolge laufen:
    @Scheduled(cron = "0 0 1 1 * *")  // 1. jeden Monats um 1 Uhr
    @SchedulerLock(name = "monthlyClosing", lockAtMostFor = "6h")
    public void runMonthlyClosing() {
        // Explizite Reihenfolge mit Fehlerbehandlung
        log.info("Starting monthly closing");

        try {
            step1RecalculateInvoices();   // Schritt 1: Rechnungen berechnen
            step2GenerateReports();        // Schritt 2: Berichte
            step3SendNotifications();      // Schritt 3: Benachrichtigungen
            step4ArchiveData();            // Schritt 4: Archivierung

            log.info("Monthly closing completed successfully");
        } catch (Exception e) {
            log.error("Monthly closing failed at step, manual intervention required", e);
            alerting.notifyOnCall("Monthly closing failed! Check logs.");
        }
    }
}
```

---

## Konsequenzen

**Positiv:** ShedLock verhindert Doppelausführung in Multi-Instance-Deployments einfach und zuverlässig. K8s CronJob ist robuster für Jobs ohne Applikationskontext. Monitoring zeigt fehlende oder fehlgeschlagene Jobs sofort.

**Negativ:** ShedLock braucht eine DB-Tabelle. K8s CronJob: Applikation muss als Job-Modus startbar sein. `fixedRate` ist riskant bei langen Job-Laufzeiten — `fixedDelay` ist sicherer.

---

## 💡 Guru-Tipps

- **`lockAtMostFor` = Job-Timeout × 1.1**: verhindert ewigen Lock wenn Pod crasht.
- **`lockAtLeastFor` = Scheduling-Intervall × 0.9**: verhindert Doppelstart bei schnellen Jobs.
- **Cron-Ausdrücke testen**: `crontab.guru` für Lesbarkeit, `CronExpression.parse()` im Test.
- **Nie `@Scheduled` für kritische Geschäftsprozesse** (Abrechnung, DSGVO-Löschung): immer ShedLock oder K8s CronJob mit `concurrencyPolicy: Forbid`.

---

## Verwandte ADRs

- [ADR-068](ADR-068-spring-batch.md) — Spring Batch für komplexe Batch-Jobs.
- [ADR-038](ADR-038-kubernetes.md) — K8s CronJob als Alternative.
- [ADR-017](ADR-017-observability-logging-tracing.md) — Job-Metriken monitoren.
