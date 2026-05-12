# ADR-091 — iSAQB Advanced: Reactive Architecture — Wann und Warum

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert (mit Einschränkungen)                           |
| Entscheider       | CTO / Architektur-Board                                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Advanced · Reactive Architecture                        |
| Betroffene Teams  | Tech Leads, alle Backend-Entwickler                           |
| iSAQB-Lernziel    | Advanced Level · Reactive Systems                             |

---

## 1. Das Problem das Reactive Architecture löst (und nur das)

```
KONTEXT: Traditioneller Thread-per-Request-Ansatz

HTTP-Request kommt an
→ Thread aus Thread-Pool wird zugewiesen (z.B. 200 Threads total)
→ Thread bearbeitet Request:
    DB-Query ausführen → WARTEN (100ms) → Thread blockiert!
    Stripe-API aufrufen → WARTEN (200ms) → Thread blockiert!
    Kafka-Message senden → WARTEN (10ms) → Thread blockiert!
→ Thread-Pool bei 200 gleichzeitigen Requests: ERSCHÖPFT
→ 201. Request: "Connection refused" oder lange Queue

PROBLEM: Threads warten 80-90% ihrer Zeit auf I/O.
         Thread kostet ~1MB Stack-Memory.
         200 Threads = 200MB nur für Thread-Stacks.
         1000 gleichzeitige Connections? → 1GB Stack-Speicher.

ZWEI LÖSUNGSANSÄTZE:
  ① Reactive (WebFlux, Project Reactor): Non-Blocking I/O, Event-Loop
     → kein Thread blockiert, wenige Threads, hoher Durchsatz
  ② Virtual Threads (Java 21 Loom → ADR-004): Blockierend aber billig
     → viele Threads, blockieren aber kostet fast nichts
     
UNSERE ENTSCHEIDUNG: Virtual Threads first (→ ADR-004)
  Reactive nur wo wirklich nötig (Streaming, Backpressure).
```

---

## 2. Das Reactive Manifesto (2014)

```
Jonas Bonér et al., "The Reactive Manifesto" (2014):
Reactive Systems haben vier Eigenschaften:

RESPONSIVE (Antwortbereit):
  System antwortet konsistent schnell
  Auch unter Last und Teilausfällen
  Nicht nur "funktioniert" — sondern messbar schnell

RESILIENT (Widerstandsfähig):
  System bleibt funktionsfähig bei Fehlern
  Fehler werden isoliert → kein Kaskadenausfall
  Recovery ohne menschliches Eingreifen

ELASTIC (Elastisch):
  System skaliert unter Last automatisch
  Scale-Up und Scale-Down (nicht nur Scale-Up!)
  Keine festen Ressourcengrenzen

MESSAGE-DRIVEN (Nachrichtenbasiert):
  Asynchrone Nachrichten als Kommunikationsprinzip
  → Zeitliche Entkopplung (Sender wartet nicht)
  → Ortliche Entkopplung (Empfänger-Standort egal)
  → Load-Leveling durch Message-Queues

WICHTIG: Dies beschreibt Architektur-Eigenschaften, nicht einen Tech-Stack!
  Kafka (→ ADR-041) = Message-Driven Reactive
  Kubernetes Auto-Scaling = Elastic Reactive
  Circuit Breaker (→ ADR-022) = Resilient Reactive
  SLOs (→ ADR-054) = Responsive Reactive
```

---

## 3. Reactive Programming: Project Reactor / WebFlux

### 3.1 Wann Reactive Programming sinnvoll ist

```
JA, Reactive Programming (WebFlux) wenn:
  ① Streaming: kontinuierlicher Datenstrom (Echtzeit-Logs, Market-Data)
  ② Sehr hohe Concurrency: > 10.000 gleichzeitige Connections
  ③ Server-Sent Events / WebSocket als Kern-Feature
  ④ Backpressure nötig: Consumer kann Tempo des Producers kontrollieren

NEIN, Virtual Threads reichen wenn:
  ① Standard Web-API mit normaler Last (< 5.000 RPS pro Instance)
  ② Team hat keine Reactor-Erfahrung
  ③ Blocking Libraries verwendet werden (JDBC, JPA, bestehende SDKs)
  ④ Codebase-Komplexität soll minimal bleiben

UNSERE ENTSCHEIDUNG (ADR-004):
  Primary: Virtual Threads (Java 21) für alle Standard-Services
  Exception: Reactive (WebFlux) nur für Echtzeit-Streaming-Use-Cases
```

### 3.2 Reactive Konzepte: Mono und Flux

```java
// Project Reactor: die zwei Kerntypen

// Mono<T>: 0 oder 1 Element (async Optional)
Mono<Order> findById(OrderId id);

// Flux<T>: 0 bis N Elemente (async Stream)
Flux<OrderSummary> findAll(Pageable page);

// ❌ SCHLECHT — Blocking-Code in Reactive-Kontext (Thread-Blocking!)
@GetMapping("/orders/{id}")
public Mono<OrderDto> findOrder(@PathVariable Long id) {
    // FEHLER: JPA-Call blockiert den Reactor-Event-Loop-Thread!
    var order = orderRepository.findById(id); // BLOCKING! NIEMALS in Reactor!
    return Mono.just(orderMapper.toDto(order.orElseThrow()));
}

// ✅ GUT — Non-Blocking mit R2DBC (Reactive DB-Treiber)
@GetMapping("/orders/{id}")
public Mono<OrderDto> findOrder(@PathVariable Long id) {
    return r2dbcOrderRepository.findById(id)    // Non-Blocking!
        .map(orderMapper::toDto)
        .switchIfEmpty(Mono.error(
            new OrderNotFoundException(new OrderId(id))));
}

// ✅ GUT — WebFlux für Echtzeit-Streaming (Server-Sent Events)
@GetMapping(value = "/orders/{id}/status", produces = TEXT_EVENT_STREAM_VALUE)
public Flux<OrderStatusEvent> streamOrderStatus(@PathVariable Long id) {
    return orderEventService.statusStream(new OrderId(id))
        .takeUntil(event -> event.status().isFinal())  // Beendet wenn Final-Status
        .timeout(Duration.ofMinutes(30));               // Max 30 Minuten
}
```

### 3.3 Backpressure: Das Alleinstellungsmerkmal von Reactive

```java
// Backpressure: Consumer kontrolliert das Tempo des Producers

// OHNE Backpressure (klassisch):
// Producer sendet 10.000 Events/Sekunde
// Consumer verarbeitet 1.000 Events/Sekunde
// → Buffer läuft voll → OutOfMemoryError → System crasht

// MIT Backpressure (Reactive):
Flux.fromIterable(getAllOrders())                    // 500.000 Orders
    .onBackpressureBuffer(1000)                       // Max 1000 im Buffer
    .publishOn(Schedulers.boundedElastic())           // Thread-Pool für CPU-Arbeit
    .flatMap(order -> processOrder(order), 10)        // Max 10 parallel
    .subscribe(
        result  -> log.info("Processed: {}", result),
        error   -> log.error("Error", error),
        ()      -> log.info("All orders processed")
    );
// Consumer kontrolliert durch flatMap-Parallelität den Verarbeitungsdruck

// ANWENDUNGSFALL: Monatliche Abrechnung für 500.000 User
// Mit Spring Batch (→ ADR-068): einfacher, bewährter
// Mit Reactor: flexibler für dynamische Parallelisierung
// EMPFEHLUNG: Spring Batch (außer spezifische Streaming-Anforderung)
```

---

## 4. Reactive Architecture ohne Reactive Programming

### 4.1 Die wichtige Erkenntnis

```
Reactive Architecture (System-Ebene) ≠ Reactive Programming (Code-Ebene)

Du kannst ein Reactive System mit imperativem Code bauen:
  
REACTIVE ARCHITEKTUR-EIGENSCHAFTEN durch:
  Responsive:   → SLOs, Latenz-Monitoring (→ ADR-054)
  Resilient:    → Circuit Breaker, Retry, Bulkhead (→ ADR-022)
  Elastic:      → Kubernetes Auto-Scaling (→ ADR-038)
  Message-Driven: → Kafka Events (→ ADR-041)

ALLES MIT VIRTUELLE THREADS + SPRING BOOT — kein WebFlux nötig!

Der häufigste Fehler: "Wir brauchen WebFlux für Reactive Architecture."
Die Wahrheit: Reactive Architecture ist ein Architektur-Muster,
              kein Programming-Modell.
```

### 4.2 Reactive Architecture mit imperativem Code

```java
// Reactive Architektur mit Virtual Threads (Java 21, kein WebFlux)

@Configuration
public class ReactiveArchitectureConfig {

    // ELASTIC: Auto-Scaling durch Kubernetes konfiguriert (→ ADR-038)

    // RESILIENT: Circuit Breaker (→ ADR-022)
    @Bean
    public CircuitBreaker stripeCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("stripe",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build()
        );
    }

    // MESSAGE-DRIVEN: Kafka (→ ADR-041)
    @KafkaListener(topics = "orders.placed", groupId = "fulfillment")
    public void onOrderPlaced(OrderPlacedEvent event) {
        fulfillmentService.prepare(event.orderId(), event.items());
        // Imperativ! Aber Message-Driven! → Reactive Architecture
    }

    // RESPONSIVE: Gemessen durch Prometheus (→ ADR-054)
}
```

---

## 5. Event Sourcing als Reactive Pattern (→ ADR-055)

```
Event Sourcing + Reactive = natürliche Kombination:

1. Nutzer-Aktion → Command → Domain Event publiziert
2. Event-Store speichert Event (unveränderlich)
3. Projections konsumieren Events ASYNCHRON
4. Read Models werden aktualisiert

BACKPRESSURE im Event-Sourcing-Kontext:
  Wenn Read-Model-Aufbau zu langsam:
  → Consumer-Group kann bewusst "hinterherhinken"
  → System bleibt funktionsfähig (Eventual Consistency)
  → Kein Datenverlust durch Message-Persistenz (Kafka-Retention)

REACTIVE STREAMS für Event-Replay:
  Flux<DomainEvent> replayEvents(AggregateId id) {
      return eventStore.loadEvents(id)
          .publishOn(Schedulers.boundedElastic());
  }
```

---

## 6. Wann welches Modell?

```
ENTSCHEIDUNGSMATRIX:

Szenario                           | Empfehlung
-----------------------------------|------------------------------------------
Standard Web-API (CRUD)           | Virtual Threads + Spring MVC
Hohe Concurrency (< 10.000 RPS)   | Virtual Threads + Spring MVC
Server-Sent Events                 | Spring WebFlux (Reactor)
WebSocket-Server                   | Spring WebFlux (Reactor)
Echtzeit-Streaming (Market Data)   | Spring WebFlux + Project Reactor
Batch-Processing                   | Spring Batch (→ ADR-068)
Event-Driven System                | Kafka + Virtual Threads (→ ADR-041)
Microservice-Kommunikation         | gRPC oder REST (synchron) /
                                   | Kafka (asynchron)
Reaktive Systemarchitektur        | Kafka + Circuit Breaker + K8s
                                   | (kein WebFlux nötig!)
```

---

## 7. Quellen & Referenzen

- **Jonas Bonér et al., "The Reactive Manifesto" (2014)** — Vier Eigenschaften: Responsive, Resilient, Elastic, Message-Driven.
- **Josh Long & Stéphane Nicoll, "Reactive Spring" (2020)** — Praxisbuch für Spring WebFlux und Project Reactor.
- **Roland Kuhn, Brian Hanafee, Jamie Allen, "Reactive Design Patterns" (2017)** — Muster für reaktive System-Architektur auf System-Ebene.
- **Ben Christensen, "Reactive Programming in the Netflix API" (2013)** — Ursprung von RxJava; Motivation aus der Praxis.
- **Ron Pressler (Oracle), "JEP 444: Virtual Threads" (2023)** — Virtual Threads als Alternative zu Reactive für I/O-Bound-Workloads.

---

## Akzeptanzkriterien

- [ ] Entscheidung dokumentiert wann WebFlux, wann Virtual Threads (dieses ADR)
- [ ] Alle neuen Services: Virtual Threads by Default (→ ADR-004)
- [ ] WebFlux nur für explizit genehmigte Streaming-Use-Cases
- [ ] Reactive Architecture-Eigenschaften über K8s + Kafka + Circuit Breaker implementiert (nicht WebFlux)
- [ ] Team-Schulung: Reactive Architecture ≠ Reactive Programming

---

## Verwandte ADRs

- [ADR-004](ADR-004-virtual-threads.md) — Virtual Threads als bevorzugter Ansatz
- [ADR-022](ADR-022-resilience-circuit-breaker.md) — Resilience (Reactive-Eigenschaft)
- [ADR-041](ADR-041-event-driven-kafka.md) — Message-Driven (Reactive-Eigenschaft)
- [ADR-038](ADR-038-kubernetes.md) — Elastic (Reactive-Eigenschaft)
- [ADR-092](ADR-092-isaqb-cloud-native.md) — Cloud-Native Architecture (nächste Ebene)
