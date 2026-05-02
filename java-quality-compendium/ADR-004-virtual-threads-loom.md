# ADR-004 — Virtual Threads für hochperformante Konkurrenz

| Feld       | Wert                          |
|------------|-------------------------------|
| Status     | ✅ Akzeptiert                 |
| Java       | 21 · JEP 444 · Project Loom   |
| Datum      | 2024-01-01                    |
| Kategorie  | Concurrency / Performance     |

---

## Kontext & Problem

Platform Threads sind teuer: jeder belegt 1–2 MB Stack-Speicher und ist direkt an einen OS-Thread gekoppelt. Bei I/O-Operationen (Datenbankabfragen, HTTP-Calls, Dateizugriffe) blockiert der Thread — und damit ein wertvoller OS-Thread — komplett, auch wenn er dabei nichts tut.

Das führte zur Verbreitung reaktiver Frameworks (WebFlux, Quarkus Mutiny), die mit non-blocking I/O und Callback-Ketten arbeiten. Das Ergebnis: komplexer, schwer debuggbarer, schwer testbarer Code.

**Virtual Threads (Project Loom)** lösen das Problem an der Wurzel: sie sind leichtgewichtig (wenige KB), werden von der JVM verwaltet, und werden beim Blockieren vom OS-Thread entkoppelt — sodass andere Virtual Threads weiterlaufen können.

---

## ❌ Schlechtes Beispiel — reaktiver Code (WebFlux)

```java
// Reaktiver Stack — funktional korrekt, aber schwer verständlich
@GetMapping("/orders/{id}")
public Mono<OrderDto> getOrder(@PathVariable String id) {
    return orderRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException(id)))
        .flatMap(order ->
            userRepository.findById(order.userId())
                .map(user -> new OrderDto(order, user))
        )
        .subscribeOn(Schedulers.boundedElastic());
}
```

**Warum ist das schlecht?**
- Callback-Ketten sind schwer zu lesen und zu debuggen — Stack-Traces enthalten reaktive Framework-Frames, nicht den eigentlichen Aufrufort.
- Jeder Entwickler muss das reaktive Programmiermodell verstehen, bevor er produktiv wird.
- Fehlerbehandlung über `onErrorResume`, `onErrorMap` etc. ist unintuitive.
- Testing erfordert `StepVerifier` statt normalem `assertThat`.

---

## ✅ Gutes Beispiel — Virtual Threads mit Spring Boot 3.2+

```java
// Aktivierung in application.properties:
// spring.threads.virtual.enabled=true
// Das ist alles — Spring Boot konfiguriert den Rest automatisch.

@GetMapping("/orders/{id}")
public OrderDto getOrder(@PathVariable String id) {

    // Normaler, imperatiever, blockierender Code.
    // Der Virtual Thread gibt den OS-Thread frei, während er auf die DB wartet.
    var order = orderRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(id));

    var user = userRepository
        .findById(order.userId())
        .orElseThrow();

    return new OrderDto(order, user);
}
// Skaliert auf 100.000+ gleichzeitige Anfragen — ohne reaktiven Stack.
```

### Eigene Virtual Threads erstellen:

```java
// ❌ Teurer Platform Thread Pool — limitiert auf 200 gleichzeitige Tasks
ExecutorService platform = Executors.newFixedThreadPool(200);
// 200 Threads = ~400 MB RAM-Overhead. Bei 201 Anfragen: Stau.

// ✅ Virtual Thread Executor — unbegrenzt skalierbar
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var futureA = executor.submit(() -> fetchUserFromDatabase(userId));
    var futureB = executor.submit(() -> fetchOrdersFromDatabase(userId));

    var user   = futureA.get();
    var orders = futureB.get();

    return new UserWithOrdersDto(user, orders);
}
// try-with-resources wartet automatisch auf Abschluss aller Tasks → Structured Concurrency
```

### Structured Concurrency (Preview in Java 21):

```java
// Parallele Aufrufe mit einheitlichem Fehlerhandling
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

    var userTask   = scope.fork(() -> fetchUser(userId));
    var ordersTask = scope.fork(() -> fetchOrders(userId));

    scope.join();           // Wartet auf beide
    scope.throwIfFailed();  // Propagiert den ersten Fehler

    return new UserWithOrdersDto(
        userTask.get(),
        ordersTask.get()
    );
}
```

---

## Begründung

Virtual Threads trennen das Konzept der **Parallelität** (viele gleichzeitige Tasks) von der **Ressource** (OS-Threads). Die JVM parkt einen Virtual Thread, sobald er auf I/O wartet, und legt ihn zurück auf einen OS-Thread, sobald die I/O abgeschlossen ist. Für den Entwickler sieht der Code aus wie normaler blockierender Code — die JVM übernimmt das Scheduling.

Das Thread-per-Request-Modell, das für synchronen Code intuitiv und einfach ist, wird damit wieder skalierbar.

---

## Konsequenzen

**Positiv:**
- Imperatiever Code statt reaktiver Ketten — drastisch einfacher zu lesen, zu schreiben, zu debuggen und zu testen.
- Stack-Traces sind wieder lesbar und zeigen den echten Aufrufpfad.
- Kein Umlernen auf reaktive APIs nötig — bestehende JDBC-, JPA-, RestTemplate-Code funktioniert unverändert.
- Spring Boot 3.2+: ein einziger Property-Eintrag aktiviert Virtual Threads global.

**Negativ / Einschränkungen:**
- **Pinning**: Wenn ein Virtual Thread einen `synchronized`-Block betritt und dabei blockiert, bleibt der zugrundeliegende OS-Thread blockiert. Diagnose: `-Djdk.tracePinnedThreads=full`.
- Virtual Threads sind **nicht** geeignet für CPU-intensive Tasks — dort weiterhin Platform Threads oder `ForkJoinPool` nutzen.
- Thread-locals funktionieren, aber bei Millionen Virtual Threads können sie zu hohem Speicherverbrauch führen. Alternative: `ScopedValues` (JEP 446, Preview).

---

## 💡 Guru-Tipps

- **`synchronized` ersetzen**: Überall wo blockierende I/O in `synchronized`-Blöcken stattfindet, auf `ReentrantLock` umstellen, um Pinning zu vermeiden.
- **Thread-Local prüfen**: Große `ThreadLocal`-Objekte (z. B. Connection-Pools, caches) können bei Virtual Threads zum Problem werden — auf `ScopedValues` migrieren.
- **Nicht übertreiben**: `Executors.newVirtualThreadPerTaskExecutor()` erstellt für jeden Task einen neuen Virtual Thread — das ist gewollt und kein Anti-Pattern.
- **Monitoring**: Virtual Threads erscheinen in JFR (Java Flight Recorder) und in Thread-Dumps — aber es können Millionen sein, also Filter verwenden.

---

## Verwandte ADRs

- [ADR-006](ADR-006-spring-boot-serviceschicht.md) — Wie Virtual Threads die Spring Boot Serviceschicht vereinfachen.
