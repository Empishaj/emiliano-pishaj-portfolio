# ADR-116 — Distributed Locking: Exklusive Ausführung in verteilten Systemen

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Datum             | 2024-01-01                                                    |
| Kategorie         | Concurrency · Verteilte Systeme · Scheduled Jobs              |

---

## 1. Das Problem: Mehrere Instanzen, ein Job

```
SZENARIO: Monatliche Rechnung für alle Kunden generieren.
  
OHNE Distributed Locking:
  3 Kubernetes-Pods laufen gleichzeitig.
  Um 02:00 Uhr startet @Scheduled auf allen 3 Pods.
  → 3 Instanzen starten den Billing-Job gleichzeitig.
  → Kunden bekommen 3 Rechnungen statt einer.
  → Datenbankduplikate, Buchhaltungsfehler, Kundenbeschwerden.

MIT Distributed Locking:
  Nur die Instanz die den Lock erhält, führt den Job aus.
  Die anderen 2 überspringen ihn.
  → Eine Rechnung pro Kunde. Korrekt.

WEITERE USE CASES:
  ① Scheduled Jobs (→ ADR-074): monatliche Reports, tägliche Imports
  ② Idempotente Operationen die nur einmal ausgeführt werden dürfen
  ③ Ressourcen-Reservierung (Lager, Sitzplätze, Termine)
  ④ Cache-Warming: nur ein Pod wärmt den Cache auf
```

---

## 2. Entscheidung

**ShedLock** für Scheduled Jobs (einfach, DB-basiert, kein zusätzlicher Stack).
**Redisson** für programmatische Locks (Redis-basiert, flexibel, niedrige Latenz).
Kein selbstgebautes Locking — bekannte Libraries verwenden.

---

## 3. ShedLock: Für Scheduled Jobs

```kotlin
// build.gradle.kts
dependencies {
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.10.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.10.0")
}
```

```sql
-- Flyway Migration: ShedLock-Tabelle erstellen
-- V30__create_shedlock_table.sql
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMPTZ  NOT NULL,
    locked_at  TIMESTAMPTZ  NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

```java
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")  // Max Lock: 30 Minuten
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()           // DB-Zeit statt Server-Zeit (konsistenter)
                .build()
        );
    }
}

@Service
@Slf4j
public class MonthlyBillingJob {

    @Scheduled(cron = "0 0 2 1 * ?")   // 1. jeden Monats, 02:00 Uhr
    @SchedulerLock(
        name               = "monthlyBilling",
        lockAtLeastFor     = "PT5M",    // Mindestens 5 Minuten halten (verhindert gleichzeitige Starts)
        lockAtMostFor      = "PT4H"     // Maximal 4 Stunden (Timeout-Schutz bei Crash)
    )
    public void runMonthlyBilling() {
        log.info("Starte monatliche Abrechnung...");
        billingService.generateAllInvoices();
        log.info("Monatliche Abrechnung abgeschlossen.");
    }
}
```

### 3.1 Wie ShedLock funktioniert

```
Ablauf:
  02:00 Uhr: Pod A versucht Lock zu setzen (INSERT in shedlock-Tabelle)
  02:00 Uhr: Pod B versucht Lock zu setzen (INSERT → scheitert, PRIMARY KEY bereits da)
  02:00 Uhr: Pod C versucht Lock zu setzen (INSERT → scheitert)
  
  Pod A führt Job aus.
  Pod B und C überspringen den Job silently.
  
  Nach lockAtMostFor (4h): Lock wird automatisch freigegeben
  (schützt vor: Pod A crasht während Job → Lock bleibt ewig)
```

---

## 4. Redisson: Programmatische Locks

```kotlin
// Für Locks außerhalb von Scheduled Jobs
implementation("org.redisson:redisson-spring-boot-starter:3.27.0")
```

```java
@Service
public class InventoryReservationService {

    private final RedissonClient redisson;

    public ReservationResult reserve(ProductId productId, int quantity) {
        // Lock-Name: pro Produkt ein eigener Lock (feingranular!)
        var lockName = "inventory:product:" + productId.value();
        var lock     = redisson.getLock(lockName);

        // Versuche Lock zu erhalten: maximal 10 Sekunden warten
        boolean locked;
        try {
            locked = lock.tryLock(
                10, TimeUnit.SECONDS,   // Wartezeit auf Lock
                30, TimeUnit.SECONDS    // Lock-Dauer (Auto-Release nach 30s)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException(productId);
        }

        if (!locked) {
            // Anderer Thread/Pod hat den Lock — Retry oder Fehler
            throw new InventoryLockTimeoutException(productId,
                "Produkt wird gerade von einer anderen Transaktion reserviert");
        }

        try {
            // Kritischer Abschnitt: nur ein Thread gleichzeitig
            return doReserve(productId, quantity);
        } finally {
            // IMMER freigeben — auch bei Exception!
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

### 4.1 Fair Lock für Fairness

```java
// Fair Lock: Requests werden in FIFO-Reihenfolge bedient
// Verhindert Starvation bei hoher Konkurrenz
var fairLock = redisson.getFairLock("payment:process:" + orderId);

// ReadWriteLock: Multiple Reads oder ein Write gleichzeitig
var rwLock = redisson.getReadWriteLock("catalog:products");

try (var readLock = rwLock.readLock()) {
    readLock.lock();
    // Viele Threads können gleichzeitig lesen
    return productRepository.findAll();
}

try (var writeLock = rwLock.writeLock()) {
    writeLock.lock();
    // Nur ein Thread schreibt — alle anderen warten
    productRepository.save(updatedProduct);
}
```

---

## 5. Wann welches Tool?

```
SHEDLOCK:
  ✅ Scheduled Jobs (@Scheduled)
  ✅ Kein Redis vorhanden
  ✅ Einfache "nur einmal ausführen"-Semantik
  ❌ Nicht für feinkörnige Locks mit kurzer Dauer (DB-Overhead)

REDISSON (Redis):
  ✅ Programmatische Locks im Anwendungscode
  ✅ Kurze Lock-Dauer (Millisekunden bis Sekunden)
  ✅ Fair Locks, ReadWrite Locks, Semaphores
  ✅ Redis bereits im Stack (für Caching etc.)
  ❌ Zusätzliche Redis-Infrastruktur nötig

POSTGRESQL ADVISORY LOCKS:
  ✅ Transaktions-scoped Locks
  ✅ Kein zusätzlicher Stack
  ✅ Integriert mit DB-Transaktionen
  Beispiel: SELECT pg_try_advisory_lock(12345)
```

---

## 6. Anti-Patterns

```java
// ❌ SCHLECHT: Lock-Dauer zu kurz → Lock wird freigegeben während Job läuft
@SchedulerLock(name = "bigJob", lockAtMostFor = "PT1M")
// Job dauert 45 Minuten → Lock wird nach 1 Minute freigegeben
// → Zweite Instanz startet → Job läuft doppelt!

// ✅ GUT: Lock-Dauer großzügig + Monitoring für Jobs die zu lang dauern
@SchedulerLock(name = "bigJob", lockAtMostFor = "PT5H")

// ❌ SCHLECHT: Lock nie freigeben bei Exception
var lock = redisson.getLock("resource");
lock.lock();
doSomethingThatMightThrow();  // Exception → lock.unlock() wird nie aufgerufen!
// Lock hängt bis lockAtMostFor abläuft

// ✅ GUT: try/finally garantiert Freigabe
lock.lock();
try {
    doSomethingThatMightThrow();
} finally {
    if (lock.isHeldByCurrentThread()) lock.unlock();
}
```

---

## 7. Monitoring

```java
@Scheduled(fixedDelay = 60_000)
public void monitorStaleLocks() {
    // ShedLock: Locks die länger als erwartet gehalten werden
    var staleLocks = jdbcTemplate.queryForList("""
        SELECT name, locked_by, locked_at, lock_until
        FROM shedlock
        WHERE locked_at < NOW() - INTERVAL '1 hour'
        """);

    staleLocks.forEach(lock ->
        log.warn("Möglicher staler Lock: {}", lock));
}
```
 

---

## Quellen & Referenzen

- **ShedLock Documentation** — github.com/lukas-krecan/ShedLock
- **Redisson Documentation** — redisson.org
- **Martin Kleppmann, "Designing Data-Intensive Applications" (2017), Kap. 8** — Verteilte Locks, Fencing Tokens, Probleme mit Clock-Drift.
- **Mike Acton, "CppCon" (2014)** — Grundprinzipien für Concurrency in verteilten Systemen.
 