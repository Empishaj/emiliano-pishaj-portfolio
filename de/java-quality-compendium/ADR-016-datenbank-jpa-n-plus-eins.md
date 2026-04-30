# ADR-016 — Datenbank & JPA: N+1, Fetch-Strategien & Transaktionsgrenzen

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Boot 3.x · Spring Data JPA / Hibernate 6 |
| Datum      | 2024-01-01                                        |
| Kategorie  | Persistenz / Performance                          |

---

## Kontext & Problem

JPA abstrahiert SQL — aber falsch eingesetzt produziert es hunderte von SQL-Queries wo eine reichen würde. Das N+1-Problem ist der häufigste Performance-Killer in JPA-Anwendungen, oft erst in Produktion entdeckt. Dieses ADR definiert verbindliche Regeln für Fetch-Strategien, Transaktionsgrenzen und Query-Design.

---

## Regel 1 — Das N+1-Problem: erkennen und verhindern

### ❌ Schlecht — N+1 durch Lazy Loading

```java
// Entity mit Lazy-Collection (JPA-Standard)
@Entity
public class Order {
    @OneToMany(fetch = FetchType.LAZY) // Standard: LAZY
    private List<OrderItem> items;
}

// Im Service: 1 Query für Orders, dann N Queries für Items
@Transactional(readOnly = true)
public List<OrderSummaryDto> findAll() {
    var orders = orderRepository.findAll(); // Query 1: SELECT * FROM orders → N Rows

    return orders.stream()
        .map(order -> new OrderSummaryDto(
            order.id(),
            order.items().size() // ← Query 2..N+1: SELECT * FROM order_items WHERE order_id=?
        ))
        .toList();
}
// Ergebnis: 1 + N SQL-Queries. Bei 1000 Orders = 1001 Queries. Katastrophe.
```

### ✅ Gut — JOIN FETCH oder Projection

```java
// Option A: JOIN FETCH — lädt alles in einer Query
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.userId = :userId")
List<Order> findWithItemsByUserId(@Param("userId") Long userId);

// Option B: Entity Graph — deklarativ, kein JPQL nötig
@EntityGraph(attributePaths = {"items", "items.product"})
List<Order> findByUserId(Long userId);

// Option C (bevorzugt für Read-Only): Projection direkt auf DTO
// Kein Entity-Overhead, kein Lazy-Loading-Problem
@Query("""
    SELECT new com.example.dto.OrderSummaryDto(
        o.id, o.status, COUNT(i), SUM(i.price)
    )
    FROM Order o
    LEFT JOIN o.items i
    WHERE o.userId = :userId
    GROUP BY o.id, o.status
    """)
List<OrderSummaryDto> findSummariesByUserId(@Param("userId") Long userId);

// Option D: Spring Data Projection Interface
interface OrderSummary {
    Long getId();
    OrderStatus getStatus();

    @Value("#{target.items.size()}")
    int getItemCount();
}
List<OrderSummary> findByUserId(Long userId);
```

---

## Regel 2 — FetchType: LAZY ist immer der Default

```java
// ❌ EAGER ist fast immer falsch — lädt immer alles, auch wenn es nicht gebraucht wird
@OneToMany(fetch = FetchType.EAGER) // Jede Order-Query lädt auch alle Items!
private List<OrderItem> items;

@ManyToOne(fetch = FetchType.EAGER) // Standard für @ManyToOne — explizit auf LAZY setzen!
private User user;

// ✅ Immer LAZY, gezielt nachladen wenn nötig
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
private List<OrderItem> items;

@ManyToOne(fetch = FetchType.LAZY) // Explizit! @ManyToOne ist EAGER by default.
@JoinColumn(name = "user_id")
private User user;
```

---

## Regel 3 — Transaktionsgrenzen: klar, minimal, korrekt

### ❌ Schlecht — zu breite oder fehlende Transaktionsgrenzen

```java
// ❌ @Transactional auf dem Controller — viel zu breit
@RestController
@Transactional // Transaktion umfasst HTTP-Parsing, Serialisierung, Business-Logik — falsch!
public class OrderController { ... }

// ❌ Lazy Loading außerhalb der Transaktion
@Transactional
public Order findById(Long id) {
    return orderRepository.findById(id).orElseThrow();
} // Transaktion endet hier!

// Im Controller:
var order = orderService.findById(id);
order.items().size(); // ← LazyInitializationException! Keine Transaktion mehr aktiv.

// ❌ Kein @Transactional bei schreibenden Operationen
public void cancelOrder(Long id) { // Vergessenes @Transactional!
    var order = orderRepository.findById(id).orElseThrow();
    order.cancel();
    // orderRepository.save(order) auch vergessen — Dirty-Checking funktioniert nicht ohne Transaktion
}
```

### ✅ Gut — Transaktionen in der Service-Schicht, korrekt dimensioniert

```java
@Service
public class OrderService {

    // Lesend: readOnly=true — Dirty-Checking deaktiviert, Read-Replicas möglich
    @Transactional(readOnly = true)
    public OrderDetailDto findById(Long id) {
        // JOIN FETCH innerhalb der Transaktion — kein LazyInitializationException
        return orderRepository.findWithItemsById(id)
            .map(orderMapper::toDetailDto) // Mapping innerhalb der Transaktion
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
    // Gibt DTO zurück — keine Entity nach außen, kein Lazy-Loading-Problem

    // Schreibend: Standard-@Transactional mit korrekten Rollback-Regeln
    @Transactional(rollbackFor = Exception.class) // Auch checked Exceptions rollen zurück
    public void cancelOrder(Long orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        order.cancel(); // Dirty-Checking erkennt Änderung automatisch
        // Kein explizites save() nötig — Hibernate trackt die Änderung
        eventPublisher.publishEvent(new OrderCancelledEvent(orderId));
    }

    // Isolation Level explizit setzen wenn nötig
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void processPayment(Long orderId, Payment payment) { ... }
}
```

---

## Regel 4 — Bulk-Operationen: niemals in Schleifen

### ❌ Schlecht — Update in Schleife

```java
// N Updates statt einem — bei 10.000 Zeilen = 10.000 Queries
@Transactional
public void deactivateInactiveUsers(List<Long> userIds) {
    userIds.forEach(id -> {
        var user = userRepository.findById(id).orElseThrow(); // Query 1
        user.deactivate();                                     // Dirty-Check Query 2
    });
}
```

### ✅ Gut — Bulk-Operation mit einer Query

```java
// Eine Query für alles
@Modifying
@Query("UPDATE UserEntity u SET u.active = false WHERE u.id IN :ids")
int deactivateUsers(@Param("ids") List<Long> ids);

// Für große Mengen: Batch-Processing
@Transactional
public void deactivateInBatches(List<Long> allIds) {
    Lists.partition(allIds, 500) // Guava oder eigene Aufteilung
        .forEach(batch -> userRepository.deactivateUsers(batch));
}
```

---

## Regel 5 — Hibernate-Statistik im Test aktivieren

```java
// application-test.yml
// spring:
//   jpa:
//     properties:
//       hibernate:
//         generate_statistics: true
//         session.events.log.LOG_QUERIES_SLOWER_THAN_MS: 10

// Im Test: Query-Anzahl prüfen
@DataJpaTest
class OrderRepositoryTest {

    @Autowired OrderRepository orderRepository;
    @Autowired EntityManager em;

    @Test
    void findWithItems_executesExactlyOneQuery() {
        var stats = em.getEntityManagerFactory()
            .unwrap(SessionFactory.class)
            .getStatistics();
        stats.clear();

        orderRepository.findWithItemsByUserId(1L);

        // Sicherstellen: nur 1 Query, kein N+1
        assertThat(stats.getQueryExecutionCount())
            .as("Erwarte genau 1 Query (JOIN FETCH), nicht N+1")
            .isEqualTo(1);
    }
}
```

---

## Konsequenzen

**Positiv:** JOIN FETCH und Projektionen eliminieren N+1 vollständig. `readOnly=true` verbessert Durchsatz messbar. Bulk-Operationen reduzieren Netzwerk-Round-Trips um Größenordnungen.

**Negativ:** JOIN FETCH kann bei mehreren Collections zu kartesischem Produkt führen — dann `@EntityGraph` oder separate Queries verwenden. Projektions-DTOs erfordern mehr Klassen.

---

## 💡 Guru-Tipps

- **Hibernate `show_sql`** in Entwicklung aktivieren: sofort sehen was JPA tut.
- **`@BatchSize(size = 25)`** als pragmatische Zwischenlösung für Lazy-Collections.
- **`@Transactional` auf `private` Methoden**: wirkungslos! Spring AOP kann private Methoden nicht intercepten.
- **Optimistic Locking** mit `@Version` für konkurrente Schreibzugriffe — verhindert Lost-Update-Anomalien.

---

## Verwandte ADRs

- [ADR-006](ADR-006-spring-boot-serviceschicht.md) — Transaktionsgrenzen in der Service-Schicht.
- [ADR-025](ADR-025-spring-boot-slice-tests.md) — `@DataJpaTest` für Repository-Tests.
