# ADR-068 — Spring Batch & Asynchrone Massenverarbeitung

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Batch 5.x             |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Batch Processing    |

---

## Kontext & Problem

Nicht jede Operation ist synchron und interaktiv. Monatsabschlüsse, Datenmigrationen, Massenimporte, Berichte aus Millionen Zeilen — das sind Batch-Jobs. Falsch implementiert: Speicherleck (alles in RAM laden), kein Restart bei Fehler, keine Fortschrittsanzeige, kein Monitoring. Spring Batch löst diese Probleme mit einem bewährten, konfigurierbaren Framework.

---

## Spring Batch Grundkonzepte

```
Job          = der gesamte Batch-Prozess
Step         = eine Phase im Job (mehrere Steps pro Job möglich)
ItemReader   = liest Daten (DB, CSV, API, ...)
ItemProcessor= transformiert/validiert ein Item
ItemWriter   = schreibt Ergebnisse (DB, Datei, API, ...)
Chunk        = N Items werden zusammen gelesen, verarbeitet und geschrieben
               → Kein alles-auf-einmal in RAM
JobRepository= speichert Ausführungshistorie (Restart, Monitoring)
```

---

## Beispiel: Monatsabrechnungs-Job

```java
// Ziel: für 500.000 User die monatliche Rechnung berechnen und versenden

@Configuration
@EnableBatchProcessing
public class MonthlyBillingJobConfig {

    // ── Reader: User aus DB in Chunks lesen ────────────────────────
    @Bean
    public JpaPagingItemReader<UserEntity> activeUserReader(
            EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<UserEntity>()
            .name("activeUserReader")
            .entityManagerFactory(emf)
            .queryString("""
                SELECT u FROM UserEntity u
                WHERE u.subscriptionStatus = 'ACTIVE'
                ORDER BY u.id
                """)
            .pageSize(100)   // 100 pro DB-Query
            .build();
    }

    // ── Processor: Rechnung berechnen ──────────────────────────────
    @Bean
    public ItemProcessor<UserEntity, MonthlyInvoice> invoiceProcessor(
            BillingCalculator calculator) {
        return user -> {
            try {
                return calculator.calculateFor(user, YearMonth.now().minusMonths(1));
            } catch (BillingException e) {
                log.warn("Could not calculate invoice for user {}: {}", user.id(), e.getMessage());
                return null; // null → Item wird übersprungen (nicht an Writer weitergegeben)
            }
        };
    }

    // ── Writer: Rechnungen speichern und Email senden ──────────────
    @Bean
    public ItemWriter<MonthlyInvoice> invoiceWriter(
            InvoiceRepository invoiceRepo,
            EmailService emailService) {
        return chunk -> {
            // Chunk als Batch speichern — eine Transaktion pro Chunk!
            invoiceRepo.saveAll(chunk.getItems());

            // Emails senden (außerhalb Transaktion — kein Rollback bei Email-Fehler)
            chunk.getItems().forEach(invoice ->
                emailService.sendInvoice(invoice.userId(), invoice));
        };
    }

    // ── Step: Reader + Processor + Writer konfigurieren ───────────
    @Bean
    public Step monthlyBillingStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            JpaPagingItemReader<UserEntity> reader,
            ItemProcessor<UserEntity, MonthlyInvoice> processor,
            ItemWriter<MonthlyInvoice> writer) {
        return new StepBuilder("monthlyBillingStep", jobRepository)
            .<UserEntity, MonthlyInvoice>chunk(100, txManager)  // 100 Items pro Transaktion
            .reader(reader)
            .processor(processor)
            .writer(writer)
            // Fehlerbehandlung: 3 Versuche, dann überspringen
            .faultTolerant()
                .retryLimit(3)
                .retry(TemporaryServiceException.class)
                .skipLimit(10)           // Max 10 Items überspringen
                .skip(BillingException.class)
            // Listener für Monitoring
            .listener(new StepProgressListener())
            .build();
    }

    // ── Job: Steps orchestrieren ────────────────────────────────────
    @Bean
    public Job monthlyBillingJob(
            JobRepository jobRepository,
            Step monthlyBillingStep) {
        return new JobBuilder("monthlyBillingJob", jobRepository)
            .incrementer(new RunIdIncrementer())  // Jeder Run hat eindeutige ID
            .start(monthlyBillingStep)
            // Bei Fehler: ab letztem Checkpoint weitermachen
            .preventRestart()   // oder: kein preventRestart() für Restart-Unterstützung
            .listener(new JobProgressListener())
            .build();
    }
}
```

---

## Job starten: Scheduled + On-Demand

```java
// Scheduled: monatlich am 1. um 2 Uhr
@Component
public class MonthlyBillingScheduler {

    private final JobLauncher jobLauncher;
    private final Job         monthlyBillingJob;

    @Scheduled(cron = "0 0 2 1 * *")  // 1. jeden Monats um 2:00
    public void runMonthlyBilling() {
        var params = new JobParametersBuilder()
            .addString("billingMonth", YearMonth.now().minusMonths(1).toString())
            .addLong("timestamp", System.currentTimeMillis())  // Unique per Run
            .toJobParameters();

        try {
            var execution = jobLauncher.run(monthlyBillingJob, params);
            log.info("Monthly billing job started: {}", execution.getId());
        } catch (JobExecutionException e) {
            log.error("Failed to start monthly billing job", e);
            alerting.notify("Monthly billing job failed to start!");
        }
    }
}

// On-Demand: REST-Endpoint für manuelle Auslösung
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class BatchJobController {

    @PostMapping("/admin/jobs/monthly-billing")
    public ResponseEntity<Long> triggerBilling(@RequestParam String month) {
        var params = new JobParametersBuilder()
            .addString("billingMonth", month)
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        var execution = jobLauncher.run(monthlyBillingJob, params);
        return ResponseEntity.accepted().body(execution.getId());
    }

    @GetMapping("/admin/jobs/monthly-billing/{executionId}")
    public JobExecution getJobStatus(@PathVariable Long executionId) {
        return jobExplorer.getJobExecution(executionId);
    }
}
```

---

## Partitionierung: parallele Verarbeitung

```java
// Partitionierung: Job auf mehrere Threads verteilen
@Bean
public Step partitionedBillingStep(
        JobRepository jobRepository,
        Step monthlyBillingStep) {

    return new StepBuilder("partitionedBillingStep", jobRepository)
        .partitioner("monthlyBillingStep", partitioner())
        .step(monthlyBillingStep)
        .taskExecutor(taskExecutor())         // Parallele Ausführung
        .gridSize(4)                          // 4 Partitionen (4 Threads)
        .build();
}

@Bean
public Partitioner partitioner() {
    // Teile User-IDs in 4 Ranges auf
    return gridSize -> {
        var min = userRepository.findMinId();
        var max = userRepository.findMaxId();
        var range = (max - min) / gridSize;

        return IntStream.range(0, gridSize)
            .boxed()
            .collect(toMap(
                i -> "partition" + i,
                i -> {
                    var ctx = new ExecutionContext();
                    ctx.putLong("minId", min + i * range);
                    ctx.putLong("maxId", i == gridSize - 1 ? max : min + (i + 1) * range - 1);
                    return ctx;
                }
            ));
    };
}
```

---

## Monitoring: Job-Ausführungshistorie

```yaml
# Spring Batch speichert Ausführungshistorie in DB (Flyway-Migration nötig)
spring:
  batch:
    jdbc:
      initialize-schema: never    # Flyway übernimmt Schema-Management
    job:
      enabled: false              # Kein Auto-Start beim Startup
```

```java
// Eigene Metriken für Spring Batch (→ ADR-017)
@Component
public class StepProgressListener implements StepExecutionListener {

    private final MeterRegistry meterRegistry;

    @Override
    public void afterStep(StepExecution stepExecution) {
        meterRegistry.gauge("batch.step.read.count",
            stepExecution.getReadCount());
        meterRegistry.gauge("batch.step.write.count",
            stepExecution.getWriteCount());
        meterRegistry.gauge("batch.step.skip.count",
            stepExecution.getSkipCount());
    }
}
```

---

## Konsequenzen

**Positiv:** Chunk-Processing verhindert OOM bei großen Datenmengen. JobRepository ermöglicht Restart ab letztem Checkpoint nach Fehler. Skip/Retry-Mechanismus behandelt einzelne fehlerhafte Items ohne den gesamten Job abzubrechen.

**Negativ:** Spring Batch benötigt eigene Tabellen in der DB (Job-Metadata). Initiales Setup-Aufwand für Reader/Processor/Writer. Parallelisierung erfordert thread-sichere Reader und Writer.

---

## 💡 Guru-Tipps

- **Chunk-Größe kalibrieren**: zu klein = viele kleine Transaktionen (langsam), zu groß = Memory-Druck und langes Rollback.
- **`CompositeItemWriter`**: mehrere Writer kombinieren (DB + Email + Audit-Log).
- **`ClassifierCompositeItemWriter`**: verschiedene Items an verschiedene Writer routen.
- **Virtual Threads** (→ ADR-004): `taskExecutor(Executors.newVirtualThreadPerTaskExecutor())` für Partitionierung.

---

## Verwandte ADRs

- [ADR-006](ADR-006-spring-boot-serviceschicht.md) — Transaktionsgrenzen in Batch-Steps.
- [ADR-016](ADR-016-datenbank-jpa-n-plus-eins.md) — JpaPagingItemReader vermeidet N+1 im Batch.
- [ADR-017](ADR-017-observability-logging-tracing.md) — Batch-Metriken monitoren.
