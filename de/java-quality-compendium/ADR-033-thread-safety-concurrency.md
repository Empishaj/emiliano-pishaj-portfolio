# ADR-033 — Concurrency: Thread Safety, Deadlocks & Race Conditions

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Status     | ✅ Akzeptiert                               |
| Java       | 21 · Virtual Threads · java.util.concurrent |
| Datum      | 2024-01-01                                  |
| Kategorie  | Concurrency / Korrektheit                   |

---

## Kontext & Problem

Concurrency-Bugs sind die schwierigsten Bugs: sie treten nicht-deterministisch auf, reproduzieren sich selten in Entwicklung, und fallen erst unter Last in Produktion auf. Virtual Threads (→ ADR-004) lösen das Skalierungsproblem — aber nicht das Korrektheitsproblem. Shared mutable state bleibt gefährlich.

---

## Regel 1 — Shared Mutable State ist der Feind

### ❌ Schlecht — nicht thread-sicherer Zähler

```java
@Service
public class OrderStatisticsService {

    // ❌ Shared mutable state — Race Condition!
    private int orderCount = 0;
    private Map<String, Integer> countByStatus = new HashMap<>(); // nicht thread-sicher!

    public void recordOrder(Order order) {
        orderCount++;                         // Race Condition: nicht atomar!
        countByStatus.merge(order.status().name(), 1, Integer::sum); // nicht thread-sicher!
    }

    public int getOrderCount() { return orderCount; }
}
// Unter Last: orderCount hat falschen Wert. Unsichtbar im Entwickler-Test.
```

### ✅ Gut — threadsichere Alternativen

```java
@Service
public class OrderStatisticsService {

    // Option A: AtomicInteger für einfache Zähler
    private final AtomicInteger orderCount = new AtomicInteger(0);

    // Option B: ConcurrentHashMap für Maps
    private final ConcurrentHashMap<String, LongAdder> countByStatus = new ConcurrentHashMap<>();

    public void recordOrder(Order order) {
        orderCount.incrementAndGet(); // Atomar — kein Race Condition

        countByStatus
            .computeIfAbsent(order.status().name(), k -> new LongAdder())
            .increment(); // LongAdder: performanter als AtomicLong unter Contention
    }

    public int getOrderCount() { return orderCount.get(); }

    // Option C (bevorzugt): Micrometer-Metriken statt eigene Zähler (→ ADR-017)
    // meterRegistry.counter("orders.total").increment();
}
```

---

## Regel 2 — Deadlock: zirkuläres Warten verhindern

### ❌ Schlecht — Deadlock durch inkonsequente Lock-Reihenfolge

```java
// Thread A: sperrt accountA, wartet auf accountB
// Thread B: sperrt accountB, wartet auf accountA → Deadlock!
public class BankTransferService {
    public void transfer(Account from, Account to, Money amount) {
        synchronized (from) {          // Thread A sperrt Account-1
            synchronized (to) {        // Thread A wartet auf Account-2
                from.debit(amount);    // Thread B hat Account-2 gesperrt
                to.credit(amount);     // und wartet auf Account-1 → DEADLOCK
            }
        }
    }
}
```

### ✅ Gut — konsistente Lock-Reihenfolge

```java
public class BankTransferService {
    public void transfer(Account from, Account to, Money amount) {
        // Immer in aufsteigender ID-Reihenfolge sperren — konsistent, kein Deadlock
        var first  = from.id().compareTo(to.id()) < 0 ? from : to;
        var second = from.id().compareTo(to.id()) < 0 ? to   : from;

        synchronized (first) {
            synchronized (second) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    }
}

// Noch besser: ReentrantLock mit Timeout (→ kein ewiges Warten)
public void transfer(Account from, Account to, Money amount)
        throws InterruptedException {
    var firstLock  = getLock(from.id().compareTo(to.id()) < 0 ? from : to);
    var secondLock = getLock(from.id().compareTo(to.id()) < 0 ? to   : from);

    if (firstLock.tryLock(500, MILLISECONDS)) {
        try {
            if (secondLock.tryLock(500, MILLISECONDS)) {
                try {
                    from.debit(amount);
                    to.credit(amount);
                } finally { secondLock.unlock(); }
            } else {
                throw new TransferTimeoutException("Could not acquire lock on target account");
            }
        } finally { firstLock.unlock(); }
    } else {
        throw new TransferTimeoutException("Could not acquire lock on source account");
    }
}
```

---

## Regel 3 — Race Condition: Check-then-Act atomar machen

### ❌ Schlecht — nicht-atomares Check-then-Act

```java
@Service
public class InventoryService {

    @Transactional
    public void reserve(ProductId productId, int quantity) {
        var stock = stockRepository.findByProductId(productId);

        // CHECK: genug Bestand?
        if (stock.available() >= quantity) {  // ← zwischen Check und Act
            // ACT: Bestand reduzieren       //    kann ein anderer Thread
            stock.reduce(quantity);           //    denselben Bestand lesen!
            stockRepository.save(stock);
        }
        // Race Condition: zwei Threads lesen stock=5, beide reservieren 3 → stock=-1
    }
}
```

### ✅ Gut — atomares Update oder Optimistic Locking

```java
// Option A: Atomares SQL-Update (kein Read-Modify-Write)
@Modifying
@Query("""
    UPDATE Stock s
    SET s.available = s.available - :quantity
    WHERE s.productId = :productId
      AND s.available >= :quantity
    """)
int tryReserve(@Param("productId") ProductId id, @Param("quantity") int quantity);

// Aufruf: 0 rows updated = nicht genug Bestand
int updated = stockRepository.tryReserve(productId, quantity);
if (updated == 0) throw new InsufficientStockException(productId);

// Option B: Optimistic Locking mit @Version
@Entity
public class Stock {
    @Version
    private Long version; // JPA inkrementiert automatisch bei jedem Save

    public void reduce(int quantity) {
        if (available < quantity) throw new InsufficientStockException();
        this.available -= quantity;
    }
}
// Gleichzeitige Änderung → OptimisticLockException → Retry oder Fehler
```

---

## Regel 4 — Visibility: volatile und happens-before

```java
// ❌ Sichtbarkeitsproblem: Feld-Update nicht garantiert sichtbar für andere Threads
public class FeatureToggle {
    private boolean featureEnabled = false; // JVM kann in Register cachen!

    public void enable()  { featureEnabled = true; }
    public boolean isEnabled() { return featureEnabled; } // Kann alten Wert lesen!
}

// ✅ volatile: garantiert Sichtbarkeit über Thread-Grenzen
public class FeatureToggle {
    private volatile boolean featureEnabled = false;

    public void enable()           { featureEnabled = true;  }
    public void disable()          { featureEnabled = false; }
    public boolean isEnabled()     { return featureEnabled;  }
}

// Für compound actions: AtomicBoolean
private final AtomicBoolean featureEnabled = new AtomicBoolean(false);
featureEnabled.compareAndSet(false, true); // Atomar: nur wenn false → auf true setzen
```

---

## Regel 5 — Immutability als ultimativer Schutz

```java
// Immutable Objekte sind per Definition thread-sicher — kein Lock nötig
public record ExchangeRate(
    Currency from,
    Currency to,
    BigDecimal rate,
    Instant   validAt
) {
    // Kein Setter, kein Zustandswechsel — immer sicher zu teilen
    public Money convert(Money amount) {
        return new Money(amount.value().multiply(rate), to);
    }
}

// Immutable Collections
private final List<String> allowedCurrencies =
    List.of("EUR", "USD", "GBP"); // Unmodifiable — thread-sicher
```

---

## Regel 6 — Virtual Threads: Pinning vermeiden (→ ADR-004)

```java
// ❌ synchronized mit blockierendem I/O: Virtual Thread Pinning
public synchronized void processWithIo() {
    database.query(); // Blockiert OS-Thread während synchronized!
}

// ✅ ReentrantLock statt synchronized bei I/O in Virtual Thread-Kontext
private final ReentrantLock lock = new ReentrantLock();

public void processWithIo() {
    lock.lock();
    try {
        database.query(); // Virtual Thread gibt OS-Thread frei beim Warten
    } finally {
        lock.unlock();
    }
}
```

---

## Diagnose-Tools

```bash
# Deadlock-Erkennung: JVM thread dump
jstack <PID>          # Zeigt "Found one Java-level deadlock"

# Virtual Thread Pinning diagnostizieren
-Djdk.tracePinnedThreads=full  # JVM-Flag: loggt jeden Pinning-Vorfall

# Race Conditions finden: ThreadSanitizer (GraalVM Native)
# Oder: Helgrind (Valgrind) für JVM-Tests

# Java Flight Recorder: Contention-Analyse
jcmd <PID> JFR.start settings=profile
jcmd <PID> JFR.stop filename=recording.jfr
# Analysieren mit JDK Mission Control: Lock Contention View
```

---

## Konsequenzen

**Positiv:** Thread-sichere Klassen durch Immutability und korrekte Synchronisation sind deterministisch testbar. Atomar SQL-Updates eliminieren Read-Modify-Write-Races elegant.

**Negativ:** Lock-basierte Synchronisation reduziert Parallelität. Optimistic Locking erfordert Retry-Logik für konfliktreiche Szenarien.

---

## 💡 Guru-Tipps

- **Immutability first**: Wenn ein Objekt nach Erzeugung nicht geändert werden muss — Record verwenden. Thread-safety für lau.
- **Prefer higher-level concurrency**: `ExecutorService`, `CompletableFuture`, `StructuredTaskScope` statt manuellem `Thread`-Handling.
- **ConcurrentHashMap statt synchronized HashMap**: `Collections.synchronizedMap()` ist grob-granular. `ConcurrentHashMap` ist fein-granular und performanter.
- **`synchronized` auf `this` vermeiden**: Interne Methoden können Lock von außen sehen. Privates Lock-Objekt verwenden: `private final Object lock = new Object();`

---

## Verwandte ADRs

- [ADR-004](ADR-004-virtual-threads-loom.md) — Virtual Threads und Pinning.
- [ADR-016](ADR-016-datenbank-jpa-n-plus-eins.md) — Optimistic Locking mit JPA `@Version`.
