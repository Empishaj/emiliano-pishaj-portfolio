# QG-JAVA-016 — Datenbank & JPA: N+1, Fetch-Strategien und Transaktionsgrenzen

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-016 |
| Titel | Datenbank & JPA: N+1, Fetch-Strategien und Transaktionsgrenzen |
| Status | Accepted / verbindlicher Standard für neue und wesentlich geänderte Persistenzzugriffe |
| Version | 1.0 |
| Datum | 2026-05-02 |
| Review-Zyklus | Jährlich oder bei Wechsel von Spring Boot, Spring Data JPA, Hibernate oder Datenbankversion |
| Kategorie | Persistenz / Performance / Transaktionsdesign |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Data JPA 3.x, Hibernate ORM 6.x, Jakarta Persistence 3.x |
| Zielgruppe | Java-Entwickler, Backend-Entwickler, Tech Leads, Reviewer, QA, Performance-Verantwortliche, Architektur |
| Verbindlichkeit | Diese Richtlinie gilt für alle neuen Repository-, Service- und Persistenzzugriffe. Abweichungen sind im Pull Request fachlich zu begründen und mit Messung oder Test abzusichern. |
| Technische Validierung | Geprüft gegen Spring Framework Transaktionsdokumentation, Spring Data JPA Repository-/Projection-/EntityGraph-Dokumentation, Hibernate ORM 6.x Dokumentation und Jakarta Persistence Spezifikation. |
| Primäres Qualitätsziel | Datenbankzugriffe sollen bewusst, messbar, transaktional korrekt, tenant-sicher und performance-stabil umgesetzt werden. |

## 1. Zweck

Diese Richtlinie beschreibt, wie Java-/Spring-Boot-Anwendungen mit Spring Data JPA und Hibernate Datenbankzugriffe so gestalten, dass typische Produktionsprobleme vermieden werden: N+1-Queries, unkontrolliertes Lazy Loading, zu breite Transaktionen, Entity-Leaks in API-Schichten, Bulk-Operationen in Schleifen, unklare Fetch-Strategien und fehlende Query-Messbarkeit.

JPA ist keine Entschuldigung, SQL nicht mehr zu verstehen. JPA abstrahiert Objekt-Relationales Mapping, aber es entfernt nicht die Kosten von Datenbankzugriffen. Jede Entity-Beziehung, jede Projection, jede Transaktionsgrenze und jede Collection-Initialisierung hat technische Wirkung auf SQL, Locks, Speicher, Latenz und Skalierung.

## 2. Kurzregel

Repository-Methoden müssen genau die Daten laden, die der konkrete Use Case benötigt. Standard ist `LAZY` für Beziehungen, explizite Fetch-Strategien pro Use Case, DTO-Projections für lesende Listen, Transaktionen in der Service-Schicht, keine Entities an Controller/API-Grenzen und messbare Query-Anzahl in Tests für kritische Zugriffe. Lazy Loading außerhalb der Service-Transaktion ist verboten.

## 3. Geltungsbereich

Diese Richtlinie gilt für Spring Boot Services mit Spring Data JPA, Hibernate-basierte Persistenzzugriffe, JPA-Entities, Repository-Methoden, JPQL- und Native-Queries, EntityGraphs, DTO-Projections, Bulk-Updates, Transaktionsgrenzen in Services und Integrationstests für Persistenzzugriffe.

Sie gilt besonders für SaaS-Systeme mit Mandanten-, Nutzer- oder Rollenbezug. In solchen Systemen ist Datenzugriff immer auch eine Sicherheitsfrage: Eine Query ohne Tenant-Kontext ist nicht nur langsam oder unsauber, sondern potenziell gefährlich.

Diese Richtlinie gilt nicht für reine In-Memory-Repositories, nicht-persistente Test-Doubles und bewusst ausgewählte alternative Persistenzansätze wie jOOQ, MyBatis oder reines JDBC. Die fachlichen Prinzipien zu Query-Messbarkeit, Transaktionsgrenzen und Tenant-Sicherheit gelten dennoch sinngemäß.

## 4. Technischer Hintergrund

JPA verwaltet Entities in einem Persistence Context. Innerhalb dieses Kontextes sind Entities „managed“. Änderungen an managed Entities werden durch Dirty Checking erkannt und beim Flush beziehungsweise Commit in SQL-Statements übersetzt. Lazy Associations werden bei Zugriff geladen, sofern noch ein geeigneter Persistence Context verfügbar ist. Genau daraus entstehen viele Produktionsfehler: Der Code sieht nach normaler Objekt-Navigation aus, erzeugt aber unbemerkt Datenbankabfragen.

Das N+1-Problem entsteht typischerweise, wenn zunächst eine Liste von N Elternobjekten geladen wird und anschließend pro Elternobjekt eine lazy Beziehung nachgeladen wird. Aus einer fachlich einfachen Operation werden dann 1 + N Datenbankabfragen. Bei 20 Datensätzen fällt das oft nicht auf. Bei 2.000 Datensätzen wird daraus ein Latenz-, Last- und Stabilitätsproblem.

Fetch-Strategien sind deshalb kein Entity-Detail, sondern eine Use-Case-Entscheidung. Die Entity beschreibt die Beziehung. Der konkrete Use Case entscheidet, welche Beziehungen geladen werden müssen.

## 5. Verbindlicher Standard

Für neue Persistenzzugriffe gelten folgende verbindliche Regeln:

1. Beziehungen werden standardmäßig explizit als `LAZY` modelliert, sofern kein sehr guter Grund für `EAGER` dokumentiert ist.
2. API-Controller geben keine JPA-Entities zurück.
3. Mapping von Entity zu DTO findet innerhalb der transaktionalen Service-Methode statt.
4. Listen- und Suchendpunkte verwenden bevorzugt DTO-Projections oder gezielte Query-DTOs.
5. `JOIN FETCH` wird gezielt für konkrete Detail-Use-Cases eingesetzt, nicht pauschal.
6. Mehrere To-Many-Collections dürfen nicht blind in einer Query gefetched werden.
7. Bulk-Operationen werden nicht als Schleife aus `findById` plus Entity-Änderung umgesetzt.
8. Schreibende Use Cases laufen in einer klaren `@Transactional`-Methode auf Service-Ebene.
9. Lesende Use Cases verwenden `@Transactional(readOnly = true)`, wenn sie mehrere Lazy-/Mapping-Schritte oder konsistente Leseeinheiten benötigen.
10. Kritische Repository-Methoden müssen in Tests gegen Query-Anzahl, Fetch-Verhalten oder Ergebnisform abgesichert werden.
11. Mandanten- und Berechtigungsfilter müssen Teil der Query oder des Service-Kontrakts sein, nicht nachträgliche Filterung im Speicher.
12. `spring.jpa.open-in-view` wird für APIs deaktiviert, damit Lazy-Loading-Fehler früh sichtbar werden.

Empfohlene Basis-Konfiguration für API-Services:

```yaml
spring:
  jpa:
    open-in-view: false
    properties:
      hibernate:
        generate_statistics: false
```

Für Tests kann Statistik gezielt aktiviert werden:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
        session:
          events:
            log:
              LOG_QUERIES_SLOWER_THAN_MS: 25
```

## 6. Gute Anwendung: DTO-Projection für Listen

Listenendpunkte benötigen selten vollständige Aggregate. Häufig reicht ein gezieltes DTO.

```java
public record OrderSummaryDto(
    Long id,
    OrderStatus status,
    Instant createdAt,
    BigDecimal total,
    long itemCount
) {}
```

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("""
        select new com.example.order.api.OrderSummaryDto(
            o.id,
            o.status,
            o.createdAt,
            coalesce(sum(i.unitPrice * i.quantity), 0),
            count(i.id)
        )
        from OrderEntity o
        left join o.items i
        where o.tenantId = :tenantId
          and o.userId = :userId
        group by o.id, o.status, o.createdAt
        order by o.createdAt desc
        """)
    List<OrderSummaryDto> findSummariesByUserId(
            @Param("tenantId") TenantId tenantId,
            @Param("userId") UserId userId);
}
```

Warum ist das gut?

- Es werden nur benötigte Felder geladen.
- Es entsteht kein Lazy Loading in der API-Schicht.
- Mandant und Nutzer sind Teil der Query.
- Aggregation findet in der Datenbank statt.
- Das Ergebnis ist direkt API- beziehungsweise servicefähig.
- Die Query macht die Datenanforderung explizit.

## 7. Falsche Anwendung: N+1 durch Lazy Loading in Streams

```java
@Transactional(readOnly = true)
public List<OrderSummaryDto> findAllOrders(TenantId tenantId) {
    var orders = orderRepository.findByTenantId(tenantId);

    return orders.stream()
            .map(order -> new OrderSummaryDto(
                    order.id(),
                    order.status(),
                    order.createdAt(),
                    order.total(),
                    order.items().size()
            ))
            .toList();
}
```

Dieses Beispiel sieht harmlos aus. Es lädt aber zunächst alle Orders und löst anschließend pro Order den Zugriff auf `items` aus. Bei 500 Orders entstehen potenziell 501 Queries.

Besser ist eine Projection-Query oder eine gezielte Fetch-Query, abhängig vom Use Case.

## 8. Gute Anwendung: Detailansicht mit `JOIN FETCH`

Für eine Detailansicht kann `JOIN FETCH` passend sein, wenn ein Aggregat mit seinen benötigten Bestandteilen geladen werden soll.

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("""
        select distinct o
        from OrderEntity o
        left join fetch o.items i
        left join fetch i.product p
        where o.id = :orderId
          and o.tenantId = :tenantId
        """)
    Optional<OrderEntity> findDetailById(
            @Param("tenantId") TenantId tenantId,
            @Param("orderId") OrderId orderId);
}
```

```java
@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderQueryService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional(readOnly = true)
    public OrderDetailDto findDetail(TenantId tenantId, OrderId orderId) {
        return orderRepository.findDetailById(tenantId, orderId)
                .map(orderMapper::toDetailDto)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
```

Die Entity verlässt die Service-Schicht nicht. Das Mapping geschieht innerhalb der Transaktion. Der Controller erhält ein DTO.

## 9. Gute Anwendung: `@EntityGraph` für definierte Fetch-Pläne

`@EntityGraph` eignet sich, wenn für eine Repository-Methode ein bestimmter Fetch-Plan wiederverwendbar und deklarativ ausgedrückt werden soll.

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<OrderEntity> findByTenantIdAndId(TenantId tenantId, OrderId id);
}
```

`@EntityGraph` ist besonders nützlich, wenn die Query selbst durch Spring Data abgeleitet werden kann, aber bestimmte Beziehungen geladen werden sollen.

Nicht gut ist, EntityGraphs als pauschalen Ersatz für Query-Design zu verwenden. Auch ein EntityGraph kann zu Overfetching, großen Resultsets und schwer nachvollziehbarem SQL führen.

## 10. FetchType-Regel

### 10.1 To-Many-Beziehungen

To-Many-Beziehungen werden nicht eager geladen.

```java
@OneToMany(
    mappedBy = "order",
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<OrderItemEntity> items = new ArrayList<>();
```

Diese Regel gilt für `@OneToMany` und `@ManyToMany`.

`@ManyToMany` ist besonders kritisch. In fachlichen Domänen sollte häufig eine explizite Join-Entity modelliert werden, weil die Beziehung selbst fachliche Attribute haben kann.

### 10.2 To-One-Beziehungen

To-One-Beziehungen werden im Code explizit als `LAZY` markiert, obwohl JPA für `@ManyToOne` und `@OneToOne` historisch andere Defaults kennt.

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "customer_id", nullable = false)
private CustomerEntity customer;
```

Wichtig: `LAZY` bei To-One-Beziehungen ist in JPA eine Anweisung beziehungsweise ein Hinweis, dessen tatsächliches Verhalten vom Provider, Proxy-Fähigkeit und Mapping abhängt. In Hibernate funktioniert es in vielen Standardfällen über Proxies; bei finalen Klassen, finalen Methoden, bestimmten Bytecode-Enhancement-Konstellationen oder Spezialfällen muss das konkrete Verhalten geprüft werden.

### 10.3 EAGER ist Ausnahme, nicht Standard

`EAGER` darf nur verwendet werden, wenn alle folgenden Bedingungen erfüllt sind:

1. Die Beziehung ist klein und fachlich immer erforderlich.
2. Die Beziehung erzeugt keine großen Objektgraphen.
3. Die Query-Auswirkung ist bekannt.
4. Es gibt einen Test oder eine Messung.
5. Der Pull Request begründet die Entscheidung.

Schlecht:

```java
@OneToMany(fetch = FetchType.EAGER)
private List<OrderItemEntity> items;
```

Dieses Mapping lädt Items immer mit, auch wenn der Use Case sie nicht benötigt.

## 11. Transaktionsgrenzen

Transaktionen gehören in die Service-Schicht, nicht in Controller und nicht in Repository-Hilfslogik.

Schlecht:

```java
@RestController
@Transactional
public class OrderController {
}
```

Warum ist das schlecht?

- Die Transaktion umfasst HTTP-Verarbeitung, Controller-Logik und Serialisierung.
- Lazy Loading kann versehentlich während JSON-Serialisierung passieren.
- Die Transaktionsdauer wird unnötig verlängert.
- Datenbankverbindungen bleiben länger gebunden.
- Fachliche Transaktionsgrenzen sind nicht mehr sichtbar.

Gut:

```java
@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher events;

    public OrderCommandService(OrderRepository orderRepository,
                               ApplicationEventPublisher events) {
        this.orderRepository = orderRepository;
        this.events = events;
    }

    @Transactional
    public void cancel(TenantId tenantId, OrderId orderId, CurrentUser currentUser) {
        var order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.assertCanBeCancelledBy(currentUser);
        order.cancel();

        events.publishEvent(new OrderCancelledEvent(tenantId, orderId));
    }
}
```

Hinweis: Wenn externe Nebenwirkungen erst nach erfolgreichem Commit passieren dürfen, wird `@TransactionalEventListener(phase = AFTER_COMMIT)` oder ein Transactional-Outbox-Pattern verwendet. Normale Events innerhalb einer Transaktion sind keine Garantie für externe Konsistenz.

## 12. `@Transactional(readOnly = true)`

`@Transactional(readOnly = true)` ist für lesende Service-Methoden sinnvoll. Es dokumentiert die Absicht und kann je nach Transaktionsmanager, Datenbank und JPA-Provider Optimierungen ermöglichen.

```java
@Transactional(readOnly = true)
public OrderDetailDto findDetail(TenantId tenantId, OrderId orderId) {
    return orderRepository.findDetailById(tenantId, orderId)
            .map(orderMapper::toDetailDto)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
}
```

Wichtig: `readOnly = true` ist kein Sicherheitsmechanismus. Es ersetzt keine Rechteprüfung, keine Tenant-Prüfung und keine Datenbankberechtigung. Es garantiert auch nicht in jedem technischen Setup, dass niemals geschrieben werden kann. Es ist primär eine Transaktions- und Optimierungsabsicht.

## 13. Self-Invocation und private transaktionale Methoden

Spring-Transaktionen werden typischerweise über Proxies angewendet. Deshalb werden nur Aufrufe abgefangen, die von außen über den Proxy kommen. Ein interner Methodenaufruf innerhalb derselben Klasse umgeht den Proxy.

Schlecht:

```java
@Service
public class OrderService {

    public void process(OrderId id) {
        updateOrder(id); // interner Aufruf: Transaktionsproxy wird nicht genutzt
    }

    @Transactional
    void updateOrder(OrderId id) {
        // vermeintlich transaktional
    }
}
```

Besser:

```java
@Service
public class OrderProcessingService {

    private final OrderUpdateService orderUpdateService;

    public OrderProcessingService(OrderUpdateService orderUpdateService) {
        this.orderUpdateService = orderUpdateService;
    }

    public void process(OrderId id) {
        orderUpdateService.updateOrder(id);
    }
}

@Service
public class OrderUpdateService {

    @Transactional
    public void updateOrder(OrderId id) {
        // tatsächlich über Spring-Proxy aufgerufen
    }
}
```

## 14. Open Session in View

Für API-Services wird `spring.jpa.open-in-view=false` gesetzt.

```yaml
spring:
  jpa:
    open-in-view: false
```

Open Session in View hält den Persistence Context über die Service-Schicht hinaus offen. Dadurch verschwinden `LazyInitializationException`s scheinbar, aber die eigentliche Ursache bleibt bestehen: Der Service hat nicht klar definiert, welche Daten für den Use Case gebraucht werden. In APIs führt das häufig zu Lazy Loading während Serialisierung, unerwarteten Queries und schwer messbarer Performance.

Die richtige Lösung ist nicht OSIV, sondern sauberes Query-Design und DTO-Mapping innerhalb der Transaktion.

## 15. Bulk-Operationen

Bulk-Operationen dürfen nicht als Schleife aus Entity-Laden und Entity-Ändern umgesetzt werden, wenn eine set-basierte Query möglich ist.

Schlecht:

```java
@Transactional
public void deactivateUsers(TenantId tenantId, List<UserId> userIds) {
    for (var userId : userIds) {
        var user = userRepository.findByTenantIdAndId(tenantId, userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.deactivate();
    }
}
```

Bei 10.000 Nutzern entstehen 10.000 Reads plus 10.000 Updates oder Flush-Operationen.

Besser:

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UserEntity u
        set u.active = false
        where u.tenantId = :tenantId
          and u.id in :userIds
        """)
    int deactivateUsers(
            @Param("tenantId") TenantId tenantId,
            @Param("userIds") Collection<UserId> userIds);
}
```

```java
@Transactional
public int deactivateUsers(TenantId tenantId, Collection<UserId> userIds) {
    return userRepository.deactivateUsers(tenantId, userIds);
}
```

Wichtig: JPQL-Bulk-Operationen umgehen Entity-Lifecycle-Callbacks, Dirty Checking einzelner Entities und den bereits geladenen Persistence Context. Nach Bulk-Updates muss bewusst mit `clearAutomatically`, `flushAutomatically` oder manueller EntityManager-Steuerung gearbeitet werden.

## 16. Batch Processing

Große Datenmengen werden in Batches verarbeitet. Das gilt besonders für Migrationen, Reprocessing, Imports und Massenänderungen.

```java
@Transactional
public void processInBatches(TenantId tenantId, List<OrderId> allIds) {
    for (var batch : partition(allIds, 500)) {
        orderRepository.markForRecalculation(tenantId, batch);
    }
}

private static <T> List<List<T>> partition(List<T> values, int size) {
    var result = new ArrayList<List<T>>();
    for (int i = 0; i < values.size(); i += size) {
        result.add(values.subList(i, Math.min(i + size, values.size())));
    }
    return result;
}
```

Batch-Größen werden nicht geraten. Sie werden gemessen. Eine gute Startgröße liegt häufig zwischen 100 und 1.000, hängt aber von Datenbank, Indizes, Lock-Verhalten, Netzwerk, Row-Größe und Transaktionsdauer ab.

## 17. Pagination und Fetch Joins

Collection Fetch Joins und Pagination sind gefährlich. Wenn eine Query eine To-Many-Collection fetched und gleichzeitig paginiert, kann die Datenbank auf Zeilenebene paginieren, während fachlich auf Parent-Objektebene paginiert werden soll. Das führt zu falschen Seiten, Duplikaten oder Speicherproblemen.

Schlecht:

```java
@Query("""
    select o
    from OrderEntity o
    left join fetch o.items
    where o.tenantId = :tenantId
    """)
Page<OrderEntity> findPageWithItems(TenantId tenantId, Pageable pageable);
```

Besser: zweistufig laden.

```java
@Query("""
    select o.id
    from OrderEntity o
    where o.tenantId = :tenantId
    order by o.createdAt desc
    """)
Page<OrderId> findPageIds(TenantId tenantId, Pageable pageable);
```

```java
@Query("""
    select distinct o
    from OrderEntity o
    left join fetch o.items
    where o.tenantId = :tenantId
      and o.id in :ids
    """)
List<OrderEntity> findByIdsWithItems(TenantId tenantId, Collection<OrderId> ids);
```

Oder für Listenansichten: DTO-Projection verwenden.

## 18. Mehrere Collections laden

Mehrere To-Many-Collections in einer Query zu fetchen ist ein häufiger Fehler. Es kann zu kartesischen Produkten, Duplikaten, stark aufgeblähten Resultsets oder Hibernate-spezifischen Exceptions führen.

Schlecht:

```java
@Query("""
    select distinct o
    from OrderEntity o
    left join fetch o.items
    left join fetch o.comments
    where o.id = :id
    """)
Optional<OrderEntity> findWithItemsAndComments(OrderId id);
```

Besser:

- Prüfen, ob beide Collections wirklich gleichzeitig benötigt werden.
- Eine Detail-DTO-Query bauen.
- Collections separat laden.
- Aggregatgrenzen überdenken.
- Bei read-only Views gezielt native SQL oder jOOQ prüfen.
- Keine riesigen Objektgraphen für einfache API-Antworten materialisieren.

## 19. `@BatchSize` und Default Batch Fetching

`@BatchSize` oder `hibernate.default_batch_fetch_size` können N+1-Probleme entschärfen, aber nicht sauber lösen. Aus 1 + N Queries werden dann weniger Queries, aber das Query-Verhalten bleibt indirekt.

Beispiel:

```java
@BatchSize(size = 50)
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
private List<OrderItemEntity> items = new ArrayList<>();
```

Diese Technik ist sinnvoll als Optimierung für Lazy-Zugriffe, die bewusst akzeptiert werden. Sie ersetzt keine Use-Case-spezifische Query.

## 20. Interface-Projections und SpEL-Fallen

Spring Data JPA Interface-Projections sind nützlich, wenn nur skalare Felder benötigt werden.

```java
public interface OrderListRow {
    Long getId();
    OrderStatus getStatus();
    Instant getCreatedAt();
}
```

Gefährlich sind Projections, die über SpEL oder Getter indirekt Associations dereferenzieren.

```java
public interface OrderSummaryProjection {

    Long getId();

    @Value("#{target.items.size()}")
    int getItemCount();
}
```

Das kann wieder Lazy Loading und N+1 auslösen. Für Aggregationen wie Item-Anzahl ist eine Query mit `count(...)` vorzuziehen.

## 21. Entity Exposure vermeiden

JPA-Entities dürfen nicht als API-Response zurückgegeben werden.

Schlecht:

```java
@GetMapping("/{id}")
public OrderEntity findById(@PathVariable Long id) {
    return orderRepository.findById(id).orElseThrow();
}
```

Warum ist das schlecht?

- Lazy Loading kann während JSON-Serialisierung ausgelöst werden.
- Interne Felder werden potenziell exponiert.
- Bidirektionale Beziehungen können Endlosrekursion verursachen.
- API-Vertrag koppelt sich an Datenbankmodell.
- Sensitive Daten können versehentlich sichtbar werden.
- Mass Assignment und Object Property Exposure werden begünstigt.

Gut:

```java
@GetMapping("/{id}")
public OrderDetailResponse findById(@PathVariable Long id) {
    return orderQueryService.findDetail(currentTenantId(), new OrderId(id));
}
```

## 22. Tenant-Isolation und Security

In SaaS-Systemen muss der Tenant-Kontext Teil des Datenzugriffs sein. Es ist nicht ausreichend, Daten erst nach dem Laden im Speicher zu filtern.

Schlecht:

```java
public OrderDetailDto findById(OrderId orderId) {
    var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.tenantId().equals(currentTenant())) {
        throw new AccessDeniedException("Wrong tenant");
    }

    return mapper.toDto(order);
}
```

Besser:

```java
public OrderDetailDto findById(TenantId tenantId, OrderId orderId) {
    return orderRepository.findByTenantIdAndId(tenantId, orderId)
            .map(mapper::toDetailDto)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
}
```

Noch besser: Repository-Methoden werden grundsätzlich tenantgebunden benannt und implementiert.

```java
Optional<OrderEntity> findByTenantIdAndId(TenantId tenantId, OrderId id);

boolean existsByTenantIdAndId(TenantId tenantId, OrderId id);

@Query("""
    select o
    from OrderEntity o
    where o.tenantId = :tenantId
      and o.status = :status
    """)
List<OrderEntity> findByTenantIdAndStatus(TenantId tenantId, OrderStatus status);
```

Tenant-Isolation gehört in Query-Kontrakte, Tests und Reviews.

## 23. Optimistic Locking

Für konkurrierende Schreibzugriffe auf dieselbe Entity wird `@Version` verwendet.

```java
@Entity
public class OrderEntity {

    @Id
    private Long id;

    @Version
    private long version;

    private OrderStatus status;

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id);
        }
        status = OrderStatus.CANCELLED;
    }
}
```

Optimistic Locking verhindert Lost Updates, wenn zwei Transaktionen denselben Zustand ändern wollen. Der konkrete Fehler muss in der Anwendung sauber behandelt werden, zum Beispiel mit `409 Conflict` in der API.

## 24. Pessimistic Locking

Pessimistic Locking ist eine bewusste Ausnahme für kritische Bereiche, in denen konkurrierende Änderungen nicht nur erkannt, sondern aktiv serialisiert werden sollen.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    select o
    from OrderEntity o
    where o.tenantId = :tenantId
      and o.id = :orderId
    """)
Optional<OrderEntity> findForUpdate(TenantId tenantId, OrderId orderId);
```

Pessimistic Locks dürfen nur verwendet werden, wenn Lock-Dauer, Deadlock-Risiko, Timeout und Datenbankverhalten verstanden sind.

## 25. Query-Messung in Tests

Für kritische Repository-Methoden wird Query-Verhalten getestet.

```java
@DataJpaTest
class OrderRepositoryQueryCountTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findDetailById_executesExpectedNumberOfQueries() {
        var sessionFactory = entityManager
                .getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class);

        var statistics = sessionFactory.getStatistics();
        statistics.clear();

        orderRepository.findDetailById(new TenantId("tenant-1"), new OrderId(1L));

        assertThat(statistics.getQueryExecutionCount())
                .as("Detail-Query darf kein N+1 auslösen")
                .isLessThanOrEqualTo(2);
    }
}
```

Wichtig: Query-Count-Tests müssen robust sein. Nicht jede Hibernate-Version erzeugt exakt dieselbe Anzahl SQL-Statements in allen Fällen. Für kritische Pfade kann „höchstens N Queries“ sinnvoller sein als „exakt 1 Query“.

## 26. Langsame Queries sichtbar machen

In Test- und Staging-Umgebungen werden langsame Queries sichtbar gemacht.

```yaml
spring:
  jpa:
    properties:
      hibernate:
        session:
          events:
            log:
              LOG_QUERIES_SLOWER_THAN_MS: 50
```

Zusätzlich sollten Datenbank-seitige Slow-Query-Logs, APM, OpenTelemetry-Traces oder JDBC-Proxy-Tools genutzt werden. JPA-Statistiken ersetzen keine echte Produktionsbeobachtung.

## 27. Repository-Design

Repository-Methoden müssen fachlich lesbar und begrenzt sein.

Gut:

```java
Optional<OrderEntity> findByTenantIdAndId(TenantId tenantId, OrderId id);

boolean existsByTenantIdAndCustomerIdAndId(
        TenantId tenantId,
        CustomerId customerId,
        OrderId id);
```

Schlecht:

```java
List<OrderEntity> findAll();

Optional<OrderEntity> findById(Long id);

List<OrderEntity> findByStatus(OrderStatus status);
```

In Multi-Tenant-Systemen sind ungebundene Repository-Methoden ein Sicherheitsrisiko. Methoden ohne Tenant-Kontext sind nur erlaubt für technische Stammdaten, globale Konfiguration oder bewusst mandantenfreie Tabellen.

## 28. Native Queries

Native Queries sind erlaubt, wenn JPQL, Criteria oder Spring Data Projections den Use Case nicht sauber ausdrücken oder die Datenbank gezielt genutzt werden soll.

Native Queries müssen:

- parametergebunden sein,
- keine String-Konkatenation mit Nutzereingaben enthalten,
- testbar sein,
- Indizes berücksichtigen,
- Migrationen berücksichtigen,
- kein DB-vendor-spezifisches Verhalten ohne Begründung einführen,
- bei API-nahen Ergebnissen DTOs statt Entities liefern.

Schlecht:

```java
String sql = "select * from orders where tenant_id = '" + tenantId + "'";
```

Gut:

```java
@Query(value = """
    select o.id, o.status, o.created_at
    from orders o
    where o.tenant_id = :tenantId
      and o.status = :status
    order by o.created_at desc
    """, nativeQuery = true)
List<OrderListRow> findRows(
        @Param("tenantId") String tenantId,
        @Param("status") String status);
```

## 29. Anti-Patterns

### 29.1 `findAll()` im Service

```java
var orders = orderRepository.findAll();
```

`findAll()` ist nur für kleine Referenzdaten oder Tests erlaubt. In Produktivcode braucht es Filter, Pagination, Tenant-Kontext und Sortierung.

### 29.2 Entity als API-DTO

```java
return userRepository.findById(id).orElseThrow();
```

Nicht erlaubt.

### 29.3 EAGER als „schneller Fix“

```java
@ManyToOne(fetch = FetchType.EAGER)
private CustomerEntity customer;
```

Nicht erlaubt ohne Begründung und Messung.

### 29.4 Mapping außerhalb der Transaktion

```java
var order = orderService.findEntity(id);
return mapper.toDto(order); // Lazy Loading möglich
```

Nicht erlaubt.

### 29.5 Query im Loop

```java
for (var id : ids) {
    repository.findById(id);
}
```

Nicht erlaubt, wenn `findAllById`, Batch Query oder Bulk-Operation möglich ist.

### 29.6 Filterung im Speicher

```java
repository.findAll().stream()
        .filter(o -> o.tenantId().equals(tenantId))
        .toList();
```

Nicht erlaubt. Filter gehören in die Query.

### 29.7 Lazy Loading über JSON

```java
@Entity
public class OrderEntity {
    @OneToMany
    private List<OrderItemEntity> items;
}
```

Wenn diese Entity direkt serialisiert wird, entsteht unkontrolliertes Lazy Loading oder Endlosrekursion.

## 30. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Use Case | Lädt die Query genau die Daten, die benötigt werden? | Liste vs Detailansicht | Pflicht |
| Tenant | Ist `tenantId` Teil der Query? | `findByTenantIdAndId` | Pflicht in SaaS |
| Entity Exposure | Verlässt eine Entity die Service-Schicht? | Controller gibt Entity zurück | Nicht erlaubt |
| Fetch | Sind Beziehungen explizit `LAZY`? | `@ManyToOne(fetch = LAZY)` | Standard |
| N+1 | Gibt es Stream-Zugriffe auf Lazy Collections? | `order.items().size()` | Prüfen |
| Projection | Wird für Listen DTO/Projection genutzt? | `OrderSummaryDto` | Bevorzugt |
| Transaktion | Liegt `@Transactional` auf Service-Ebene? | public Service-Methode | Pflicht |
| OSIV | Ist `spring.jpa.open-in-view=false` gesetzt? | API-Service | Pflicht |
| Bulk | Werden Massenänderungen set-basiert ausgeführt? | `@Modifying update` | Bevorzugt |
| Pagination | Wird nicht mit Collection Fetch Join paginiert? | `Page + join fetch items` | Vermeiden |
| Locking | Gibt es Schutz vor Lost Updates? | `@Version` | Bei Schreibkonflikten |
| Tests | Gibt es Query-/Repository-Tests für kritische Pfade? | Query Count | Pflicht bei Risiko |

## 31. Automatisierbare Prüfungen

Mögliche ArchUnit-/CI-Regeln:

```text
- Controller dürfen nicht von Entity-Klassen abhängen.
- DTO-Packages dürfen nicht von Entity-Packages abhängen.
- Repository-Methoden in Tenant-Kontexten müssen tenantId im Namen oder in @Query enthalten.
- Keine @Transactional-Annotation auf Controller-Klassen.
- Keine @Transactional-Annotation auf private Methoden.
- Keine FetchType.EAGER ohne explizite Ausnahmeannotation.
- Keine findAll()-Nutzung in Service-Klassen ohne Pageable oder dokumentierte Ausnahme.
- spring.jpa.open-in-view=false muss in produktiven Profilen gesetzt sein.
```

Beispiel:

```java
@ArchTest
static final ArchRule controller_should_not_depend_on_entities =
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..entity..")
                .because("Controller dürfen keine JPA-Entities als API-Vertrag verwenden.");
```

## 32. Migration bestehender JPA-Zugriffe

Bestehender Code wird schrittweise bereinigt:

1. Repository-Methoden und Controller-Endpunkte erfassen.
2. Endpunkte nach Liste, Detail, Command und Batch kategorisieren.
3. `findAll()`-Nutzungen identifizieren.
4. Entity-Rückgaben aus Controller entfernen.
5. DTOs und Mapper einführen.
6. N+1-Risiken mit Tests oder SQL-Logs sichtbar machen.
7. Kritische Listen auf DTO-Projections umstellen.
8. Detailzugriffe mit `JOIN FETCH` oder `@EntityGraph` definieren.
9. OSIV deaktivieren.
10. LazyInitializationExceptions als Designfeedback behandeln, nicht durch EAGER verstecken.
11. Bulk-Operationen aus Schleifen in set-basierte Queries überführen.
12. Tenant-Filter in Repository-Kontrakte aufnehmen.
13. Query-Anzahl und Performance in Tests/Staging prüfen.

## 33. Ausnahmen

Eine Abweichung von dieser Richtlinie ist erlaubt, wenn alle folgenden Bedingungen erfüllt sind:

- Der Use Case ist klar beschrieben.
- Die Datenmenge ist begrenzt oder gemessen.
- Die Abweichung ist im Pull Request begründet.
- Es gibt einen Test oder eine Performance-Messung.
- Security- und Tenant-Aspekte sind geprüft.
- Die Abweichung erzeugt keine versteckte Kopplung zur API-Schicht.

Beispiele für mögliche Ausnahmen:

- Kleine Referenztabelle mit wenigen Zeilen.
- Read-only Admin-Export mit bewusstem Streaming.
- Technische Batch-Migration außerhalb des normalen Request-Flows.
- Native Query für datenbankspezifische Funktion.
- EAGER für eine extrem kleine, fachlich untrennbare To-One-Beziehung mit Messung.

## 34. Entscheidungshilfe

| Frage | Wenn ja | Wenn nein |
|---|---|---|
| Wird eine Liste für eine API angezeigt? | DTO-Projection verwenden | Detail-Use-Case prüfen |
| Wird ein vollständiges Aggregat fachlich benötigt? | `JOIN FETCH` oder `@EntityGraph` prüfen | Projection bevorzugen |
| Wird eine To-Many-Collection gebraucht? | gezielt fetch oder aggregieren | nicht laden |
| Wird paginiert? | keine Collection-Fetch-Joins | Detail-Query möglich |
| Werden viele Zeilen geändert? | Bulk-Query oder Batch | normale Entity-Transaktion möglich |
| Gibt es parallele Schreibzugriffe? | `@Version` oder Locking prüfen | einfache Transaktion reicht |
| Ist Tenant-Kontext relevant? | Query muss tenantgebunden sein | Ausnahme begründen |
| Wird eine Entity an Controller gegeben? | Refactoring nötig | Standard erfüllt |

## 35. Definition of Done

Ein Persistenzzugriff erfüllt diese Richtlinie, wenn:

1. Der Use Case klar zwischen Liste, Detail, Command oder Batch eingeordnet ist.
2. Die Query nur die benötigten Daten lädt.
3. Keine JPA-Entity die Service-Schicht in Richtung Controller/API verlässt.
4. Beziehungen standardmäßig `LAZY` sind oder eine begründete Ausnahme besitzen.
5. N+1-Risiken geprüft und bei kritischen Pfaden getestet sind.
6. Transaktionen auf Service-Ebene liegen.
7. Mapping von Entities zu DTOs innerhalb der Transaktion erfolgt.
8. `spring.jpa.open-in-view=false` für API-Services gesetzt ist.
9. Tenant- und Berechtigungsgrenzen Teil der Query oder des Service-Kontrakts sind.
10. Bulk-Operationen nicht unnötig in Schleifen ausgeführt werden.
11. Pagination, Sorting und Filtering datenbankseitig erfolgen.
12. Schreibkonflikte bei relevanten Entities durch `@Version` oder bewusste Locking-Strategie behandelt werden.
13. Tests oder Messungen das Query-Verhalten für kritische Fälle absichern.

## 36. Quellen und weiterführende Literatur

- Spring Framework Reference Documentation — Declarative Transaction Management und `@Transactional`
- Spring Data JPA Reference Documentation — Repositories, Projections, `@EntityGraph`, `@Modifying`
- Hibernate ORM 6.x User Guide — Fetching, Batch Fetching, Statistics, Persistence Context
- Jakarta Persistence 3.1 Specification — Entity Mapping, FetchType, Locking, AttributeConverter
- OWASP API Security Top 10 — objektbezogene Autorisierung und API-Datenzugriffe
- Vlad Mihalcea / High-Performance Java Persistence — weiterführende praktische Hibernate-Performance-Literatur
- Martin Fowler — Patterns of Enterprise Application Architecture, Repository, Unit of Work, Transaction Script
