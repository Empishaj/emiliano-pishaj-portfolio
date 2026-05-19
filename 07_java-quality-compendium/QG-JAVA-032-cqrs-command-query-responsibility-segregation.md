# QG-JAVA-032 — CQRS: Command Query Responsibility Segregation richtig anwenden
 
---

## 1. Zweck

Diese Richtlinie erklärt, wie CQRS in Java- und Spring-Boot-Anwendungen sinnvoll eingesetzt wird.

CQRS steht für **Command Query Responsibility Segregation**. Übersetzt bedeutet das: **Trennung der Verantwortung für Befehle und Abfragen**.

Ein Command ist eine Operation, die Zustand verändert. Eine Query ist eine Operation, die Zustand liest. Die Grundidee ist einfach:

```text
Schreiben und Lesen haben unterschiedliche Ziele.
Deshalb müssen sie nicht zwangsläufig dasselbe Modell, dieselben DTOs, dieselben Repositories und dieselben Abfragen verwenden.
```

In vielen Anwendungen wird ein einziges Entity-Modell für alles benutzt:

- REST-Response,
- Datenbankpersistenz,
- Validierung,
- Businesslogik,
- Reporting,
- Listenansicht,
- Detailansicht,
- Export,
- Admin-Oberfläche.

Das funktioniert anfangs. Später entstehen typische Probleme:

- Listenansichten laden volle Entities.
- Detailansichten erzeugen N+1-Queries.
- Reporting aggregiert Daten im Java-Speicher.
- JPA-Entities wandern in Controller.
- Schreibmodelle werden für UI-Optimierung verbogen.
- Lesemodelle schleppen Geschäftsregeln mit.
- Services werden zu Mischklassen aus Command- und Query-Logik.
- Performance-Optimierungen gefährden Domänenkonsistenz.
- Transaktionsgrenzen werden unklar.

CQRS löst dieses Problem nicht durch Magie, sondern durch eine klare Trennung:

```text
Command-Seite: Konsistenz, Invarianten, Transaktionen, Domänenlogik.
Query-Seite: Performance, Projektionen, Denormalisierung, UI- und Reporting-Bedarf.
```

Diese Guideline zeigt die praktische Umsetzung im Alltag eines Java-Entwicklers: vom einfachen logischen CQRS in einem Spring-Boot-Service bis zu separaten Read-Models und eventbasierter Projektion.

---

## 2. Kurzregel

Trenne schreibende Use Cases von lesenden Use Cases, sobald beide unterschiedliche Anforderungen haben. Commands verändern Zustand und laufen über fachliche Services, Domänenmodelle, Validierung und Transaktionen. Queries verändern keinen Zustand und dürfen direkt auf optimierte DTO-Projektionen, Read-Repositories, Views oder native SQL-Abfragen zugreifen. Vollständiges CQRS mit separaten Datenbanken ist nur sinnvoll, wenn Performance, Skalierung, Reporting, Teamgrenzen oder Domänenkomplexität den zusätzlichen Aufwand rechtfertigen.

---

## 3. Warum CQRS im Java-Alltag wichtig ist

Ein Java-Entwickler arbeitet täglich mit zwei Arten von Anforderungen.

Die erste Anforderung lautet:

```text
Der Benutzer will etwas tun.
```

Beispiele:

- User registrieren,
- Bestellung aufgeben,
- Bestellung stornieren,
- Angebot veröffentlichen,
- Zahlung auslösen,
- Händler verifizieren,
- Passwort ändern.

Diese Anforderungen brauchen Schutz:

- Ist die Eingabe gültig?
- Darf der Benutzer das?
- Ist der Zustand korrekt?
- Wird die Transaktion konsistent abgeschlossen?
- Müssen Domain Events erzeugt werden?
- Müssen Invarianten geschützt werden?

Das ist die Command-Seite.

Die zweite Anforderung lautet:

```text
Der Benutzer will etwas sehen.
```

Beispiele:

- User-Liste anzeigen,
- Bestelldetails laden,
- Dashboard anzeigen,
- Umsätze aggregieren,
- Admin-Suche durchführen,
- Report exportieren,
- Merchant-Kachel anzeigen.

Diese Anforderungen brauchen andere Eigenschaften:

- schnell laden,
- wenige Queries,
- genau die Felder liefern, die die UI braucht,
- aggregierte Werte direkt berechnen,
- Paging und Sorting effizient unterstützen,
- keine volle Domain-Entity laden,
- keine Seiteneffekte erzeugen.

Das ist die Query-Seite.

CQRS hilft, diese zwei Welten nicht künstlich in eine Klasse und ein Modell zu pressen.

---

## 4. Grundlagen kurz erklärt

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| CQS | Command Query Separation. Methoden sollen entweder Zustand verändern oder Daten zurückgeben, aber nicht beides vermischen. | `cancelOrder()` vs. `findOrder()` |
| CQRS | Architekturmuster, das Schreib- und Leseverantwortung getrennt modelliert. | `OrderCommandService` und `OrderQueryService` |
| Command | Ausdruck einer Änderungsabsicht. | `RegisterUserCommand` |
| Query | Ausdruck einer Leseabsicht. | `FindUsersQuery` |
| Command Handler | Service/Methode, die einen Command verarbeitet. | `UserCommandService.handle(...)` |
| Query Handler | Service/Methode, die eine Query verarbeitet. | `UserQueryService.handle(...)` |
| Write Model | Modell für Schreiblogik, Invarianten und Transaktionen. | `User`, `Order` |
| Read Model | Modell für schnelle Lesefälle. | `UserSummaryDto`, `OrderDashboardView` |
| Projection | gezielte Abbildung von Daten in ein Lesemodell. | DTO direkt aus SQL/JPQL |
| Denormalisierung | Daten werden für Lesen bewusst redundant oder flacher gespeichert. | `order_count` direkt in View |
| Eventual Consistency | Lesemodell kann kurz hinter Schreibmodell zurückliegen. | Event-Projektion verzögert |
| Materialized View | vorberechnete Datenbanksicht für schnelle Reads. | Dashboard-View |
| Reporting Query | Aggregierende Abfrage für Analyse/Reports. | Umsatz pro Monat |
| Command DTO | Eingabeobjekt für einen Schreibfall. | `CreateOrderCommand` |
| Query DTO | Eingabeobjekt für einen Lesefall. | `FindOrdersQuery` |
| Response DTO | Ausgabeobjekt für API/UI. | `OrderSummaryResponse` |

---

## 5. CQS vs. CQRS

CQS und CQRS werden häufig verwechselt.

### CQS auf Methodenebene

CQS bedeutet:

```text
Eine Methode ist entweder Command oder Query.
```

Command:

```java
public void cancelOrder(OrderId orderId) {
    // Zustand ändern
}
```

Query:

```java
public OrderDetailDto findOrder(OrderId orderId) {
    // Zustand lesen
}
```

Warnsignal:

```java
public OrderDetailDto cancelAndReturnDetail(OrderId orderId) {
    // Zustand ändern UND große Lesesicht bauen
}
```

Das ist nicht automatisch verboten, aber es braucht eine bewusste Begründung. Häufig ist besser: Command ausführen, Location oder ID zurückgeben, anschließend über eine Query lesen.

### CQRS auf Architektur-/Serviceebene

CQRS geht weiter:

```text
Schreibmodell und Lesemodell dürfen getrennt sein.
```

Kleine CQRS-Form:

```text
OrderCommandService
OrderQueryService
OrderWriteRepository
OrderReadRepository
```

Große CQRS-Form:

```text
Command DB
Read DB
Event-Projektionen
Materialized Views
Elasticsearch / Redis / MongoDB
```

Wichtig: CQRS bedeutet nicht automatisch Event Sourcing. CQRS kann ohne Event Sourcing eingesetzt werden.

---

## 6. Die zentrale Linie

Die zentrale Linie lautet:

```text
Das Schreibmodell schützt Wahrheit.
Das Lesemodell liefert Sicht.
```

Das Schreibmodell beantwortet:

- Darf diese Änderung passieren?
- Ist der Zustand gültig?
- Welche Invarianten müssen gelten?
- Welche Transaktion ist nötig?
- Welches Event entsteht?
- Welche Sperre oder Versionierung ist nötig?

Das Lesemodell beantwortet:

- Welche Daten braucht der Client?
- In welchem Format?
- Wie schnell?
- Mit welchem Filter?
- Mit welchem Paging?
- Mit welcher Aggregation?
- Aus welcher Projektion?

Wenn ein Modell beide Aufgaben gleichzeitig erfüllen soll, entstehen Kompromisse.

---

# Teil A — Das Problem ohne CQRS

## 7. Ein Modell für alles

Schlecht:

```java
@Service
public class UserService {

    @Transactional
    public UserDto register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        var user = UserEntity.create(
                command.name(),
                command.email(),
                passwordEncoder.encode(command.password())
        );

        var saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserStatisticsDto getStatistics(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var orders = orderRepository.findByUserId(userId);

        var total = orders.stream()
                .map(OrderEntity::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserStatisticsDto(
                user.getName(),
                orders.size(),
                total
        );
    }
}
```

Was hier schiefläuft:

- `register()` ist ein Schreibfall, gibt aber ein volles DTO zurück.
- `findAll()` lädt volle Entities für eine Liste.
- `getStatistics()` aggregiert im Java-Speicher.
- `UserService` mischt Schreiblogik, Leselogik und Reporting.
- Query-Seite hängt an JPA-Entity-Struktur.
- Performance-Optimierungen gefährden Schreibmodell.
- Der Service wird schwer testbar und schwer reviewbar.

---

## 8. Typische Symptome

| Symptom | Details/Erklärung | CQRS-Signal |
|---|---|---|
| Listen laden langsam | volle Entities statt flache DTOs | Query-Seite braucht Projektionen |
| N+1-Probleme | Lazy Loading in Read-Use-Cases | Read Repository nötig |
| Reporting im Java-Speicher | Aggregation nach Entity-Laden | SQL-/View-Projektion |
| Controller geben Entities zurück | Persistenzmodell wird API-Modell | DTO-Trennung |
| Services werden riesig | Schreiben, Lesen, Reporting gemischt | Command/Query-Service trennen |
| `@Transactional` überall | Lesen und Schreiben nicht unterschieden | getrennte Transaktionsgrenzen |
| API braucht andere Felder als Domain | Domain wird UI-getrieben verändert | Read Model |
| Dashboard braucht Aggregation | normales Entity-Modell ungeeignet | denormalisiertes Read Model |
| Schreiblogik enthält UI-Mapping | Command gibt zu viel zurück | Command minimal halten |

---

# Teil B — CQRS Stufenmodell

## 9. Stufe 0: Kein CQRS, klassisches CRUD

Für einfache CRUD-Anwendungen ist vollständiges CQRS oft unnötig.

```java
@Service
public class CountryService {

    @Transactional(readOnly = true)
    public List<CountryDto> findAll() {
        return countryRepository.findAll().stream()
                .map(CountryDto::from)
                .toList();
    }

    @Transactional
    public CountryDto create(CreateCountryRequest request) {
        var country = countryRepository.save(CountryEntity.from(request));
        return CountryDto.from(country);
    }
}
```

Das ist okay, wenn:

- Domäne einfach ist,
- keine komplexen Invarianten existieren,
- Datenmenge klein ist,
- Listen schnell sind,
- UI keine besonderen Projektionen braucht,
- Reporting nicht relevant ist.

## 10. Stufe 1: Logische Trennung in Services

Diese Stufe ist der sinnvolle Standard für viele professionelle Spring-Boot-Anwendungen.

```text
UserCommandService
UserQueryService
```

Beide können dieselbe Datenbank nutzen. Die Trennung ist zunächst logisch.

## 11. Stufe 2: Getrennte Repositories und Projektionen

```text
UserWriteRepository → Entities/Aggregate
UserReadRepository  → DTO-Projektionen, Views, Native SQL
```

Die Datenbank ist noch dieselbe, aber Lesezugriffe sind optimiert.

## 12. Stufe 3: Separate Read-Models in derselben Datenbank

```text
users, orders, order_items → normale Write-Tabellen
user_dashboard_view       → materialisierte/denormalisierte Lesesicht
```

Synchronisation kann erfolgen durch:

- Datenbank-Views,
- Materialized Views,
- Trigger,
- scheduled refresh,
- application events,
- Outbox + Projection Worker.

## 13. Stufe 4: Separate Datenbanken

```text
Command DB: PostgreSQL / MySQL
Read DB: Elasticsearch / Redis / MongoDB / PostgreSQL-Read-Replica
```

Diese Stufe bringt Eventual Consistency und deutlich höhere Komplexität.

Sie ist sinnvoll bei:

- sehr hoher Leselast,
- komplexer Suche,
- Reporting/Analytics,
- getrennten Skalierungsanforderungen,
- polyglotten Read-Stores,
- echten Domänen-/Teamgrenzen.

Sie ist nicht sinnvoll, nur weil CQRS modern klingt.

---

# Teil C — Stufe 1 praktisch umsetzen

## 14. Command-Seite

### 14.1 Command als Intent

Ein Command beschreibt eine Änderungsabsicht.

```java
public record RegisterUserCommand(

        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {}
```

Result:

```java
public record RegisterUserResult(UserId userId) {}
```

### 14.2 Command Service

```java
@Service
public class UserCommandService {

    private final UserWriteRepository userWriteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    public UserCommandService(
            UserWriteRepository userWriteRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events) {
        this.userWriteRepository = userWriteRepository;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
    }

    @Transactional
    public RegisterUserResult handle(@Valid RegisterUserCommand command) {
        var email = new EmailAddress(command.email());

        if (userWriteRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        var user = User.register(
                UserId.generate(),
                command.name(),
                email,
                passwordEncoder.encode(command.password())
        );

        userWriteRepository.save(user);

        events.publishEvent(new UserRegisteredEvent(
                user.id(),
                Instant.now()
        ));

        return new RegisterUserResult(user.id());
    }
}
```

### 14.3 Warum Command minimal zurückgibt

Ein Command sollte meist nur zurückgeben:

- ID,
- Bestätigung,
- Version,
- Location,
- technische Referenz.

Nicht:

- vollständiges Detail-DTO,
- Dashboard-Daten,
- Reportingdaten,
- mehrere aggregierte Unterobjekte.

Begründung:

```text
Der Command verändert Zustand.
Die Query liest Sicht.
```

Wenn der Client nach dem Command Details braucht, ruft er die Detail-Query auf.

---

## 15. Query-Seite

### 15.1 Query als Leseabsicht

```java
public record FindUsersQuery(

        @Size(max = 100)
        String nameFilter,

        Pageable pageable
) {}
```

### 15.2 Query DTOs

```java
public record UserSummaryDto(
        Long id,
        String name,
        String email,
        int orderCount
) {}
```

```java
public record UserDetailDto(
        Long id,
        String name,
        String email,
        LocalDate registeredAt,
        int orderCount,
        BigDecimal totalSpent,
        List<String> recentOrderIds
) {}
```

### 15.3 Query Service

```java
@Service
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserReadRepository userReadRepository;

    public UserQueryService(UserReadRepository userReadRepository) {
        this.userReadRepository = userReadRepository;
    }

    public Page<UserSummaryDto> handle(FindUsersQuery query) {
        return userReadRepository.findSummaries(
                query.nameFilter(),
                query.pageable()
        );
    }

    public UserDetailDto findDetail(Long userId) {
        return userReadRepository.findDetailById(userId)
                .orElseThrow(() -> new UserNotFoundException(new UserId(userId)));
    }
}
```

### 15.4 Read Repository mit DTO-Projektion

```java
public interface UserReadRepository {

    @Query(\"\"\"
           SELECT new com.example.user.query.UserSummaryDto(
               u.id,
               u.name,
               u.email,
               COUNT(o.id)
           )
           FROM UserEntity u
           LEFT JOIN OrderEntity o ON o.userId = u.id
           WHERE (:nameFilter IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :nameFilter, '%')))
           GROUP BY u.id, u.name, u.email
           \"\"\")
    Page<UserSummaryDto> findSummaries(
            @Param("nameFilter") String nameFilter,
            Pageable pageable
    );

    @Query(value = \"\"\"
           SELECT
               u.id AS id,
               u.name AS name,
               u.email AS email,
               u.registered_at AS registeredAt,
               COUNT(o.id) AS orderCount,
               COALESCE(SUM(o.total), 0) AS totalSpent
           FROM users u
           LEFT JOIN orders o ON o.user_id = u.id
           WHERE u.id = :userId
           GROUP BY u.id, u.name, u.email, u.registered_at
           \"\"\", nativeQuery = true)
    Optional<UserDetailProjection> findDetailProjectionById(@Param("userId") Long userId);
}
```

Interface Projection:

```java
public interface UserDetailProjection {

    Long getId();

    String getName();

    String getEmail();

    LocalDate getRegisteredAt();

    int getOrderCount();

    BigDecimal getTotalSpent();
}
```

Mapper:

```java
public UserDetailDto toDto(UserDetailProjection projection, List<String> recentOrderIds) {
    return new UserDetailDto(
            projection.getId(),
            projection.getName(),
            projection.getEmail(),
            projection.getRegisteredAt(),
            projection.getOrderCount(),
            projection.getTotalSpent(),
            recentOrderIds
    );
}
```

---

# Teil D — Controller richtig strukturieren

## 16. Controller mit Command/Query-Trennung

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService queryService;
    private final UserApiMapper mapper;

    public UserController(
            UserCommandService commandService,
            UserQueryService queryService,
            UserApiMapper mapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterUserRequest request) {

        var result = commandService.handle(mapper.toCommand(request));
        var location = URI.create("/api/v1/users/" + result.userId().value());

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public Page<UserSummaryDto> findAll(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        return queryService.handle(new FindUsersQuery(name, pageable));
    }

    @GetMapping("/{id}")
    public UserDetailDto findDetail(@PathVariable Long id) {
        return queryService.findDetail(id);
    }
}
```

### 16.1 Request DTO getrennt vom Command

```java
public record RegisterUserRequest(
        String name,
        String email,
        String password
) {}
```

```java
@Component
public class UserApiMapper {

    public RegisterUserCommand toCommand(RegisterUserRequest request) {
        return new RegisterUserCommand(
                request.name(),
                request.email(),
                request.password()
        );
    }
}
```

Warum trennen?

- Request ist API-Vertrag.
- Command ist Anwendungsfall-Vertrag.
- API kann sich anders entwickeln als Application Layer.
- Tests werden klarer.
- Security-Felder bleiben kontrolliert.

---

# Teil E — Write Repository vs. Read Repository

## 17. Write Repository

Das Write Repository ist für Konsistenz und Persistenz des Schreibmodells zuständig.

```java
public interface UserWriteRepository {

    boolean existsByEmail(EmailAddress email);

    void save(User user);

    Optional<User> findById(UserId userId);
}
```

Wenn man direkt Spring Data JPA nutzt:

```java
@Repository
public interface UserJpaWriteRepository
        extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
}
```

Mit hexagonaler Architektur kann ein Adapter zwischen Domain Repository und JPA Repository liegen.

## 18. Read Repository

Das Read Repository optimiert Lesefälle.

```java
public interface UserReadRepository {

    Page<UserSummaryDto> findSummaries(String nameFilter, Pageable pageable);

    Optional<UserDetailDto> findDetailById(Long userId);

    List<UserExportRow> findExportRows(UserExportQuery query);
}
```

Read Repositories dürfen:

- DTO-Projektionen nutzen,
- native SQL nutzen,
- Views nutzen,
- Materialized Views nutzen,
- Aggregationen direkt in SQL ausführen,
- Read-only Transaktionen verwenden.

Read Repositories sollen nicht:

- Domain-Invarianten ändern,
- Entities für Schreiblogik zurückgeben,
- Lazy Loading für UI-Sichten erzwingen,
- Seiteneffekte haben.

---

# Teil F — Transaktionsgrenzen

## 19. Command-Transaktionen

Commands sind meist transaktional.

```java
@Transactional
public CancelOrderResult handle(CancelOrderCommand command) {
    var order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.cancel(command.reason());

    orderRepository.save(order);

    events.publishEvent(new OrderCancelledEvent(order.id(), Instant.now()));

    return new CancelOrderResult(order.id(), order.version());
}
```

Wichtig:

- Invarianten innerhalb der Transaktion prüfen.
- Änderungen atomar speichern.
- Optimistic Locking prüfen, wenn konkurrierende Änderungen möglich sind.
- Events nach Commit behandeln, wenn externe Nebenwirkungen folgen.

## 20. Query-Transaktionen

Queries sind read-only.

```java
@Transactional(readOnly = true)
public OrderDashboardDto dashboard(OrderDashboardQuery query) {
    return orderReadRepository.loadDashboard(query);
}
```

Vorteile:

- klare Absicht,
- weniger Dirty-Checking,
- bessere Lesbarkeit,
- mögliches Routing auf Read Replica,
- saubere Trennung im Review.

## 21. Query darf keinen Zustand verändern

Schlecht:

```java
@Transactional
public UserDetailDto findUserAndMarkAsSeen(Long userId) {
    var user = userRepository.findById(userId).orElseThrow();
    user.markSeen();
    return mapper.toDetail(user);
}
```

Besser:

```java
public UserDetailDto findUser(Long userId) {
    return userQueryService.findDetail(userId);
}

public void markUserAsSeen(Long userId) {
    userCommandService.handle(new MarkUserAsSeenCommand(userId));
}
```

Wenn das Markieren als gelesen fachlich zwingend mit dem Lesen verbunden ist, muss dies bewusst als Command modelliert werden.

---

# Teil G — Read Models und Projektionen

## 22. DTO-Projektion

Für Listenansichten sind DTO-Projektionen meist besser als Entities.

```java
public record OrderSummaryDto(
        Long id,
        String orderNumber,
        String customerName,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt
) {}
```

```java
@Query(\"\"\"
       SELECT new com.example.order.query.OrderSummaryDto(
           o.id,
           o.orderNumber,
           c.name,
           o.status,
           o.total,
           o.createdAt
       )
       FROM OrderEntity o
       JOIN CustomerEntity c ON c.id = o.customerId
       WHERE o.tenantId = :tenantId
       ORDER BY o.createdAt DESC
       \"\"\")
Page<OrderSummaryDto> findOrderSummaries(
        @Param("tenantId") String tenantId,
        Pageable pageable
);
```

## 23. Interface Projection

```java
public interface OrderSummaryProjection {

    Long getId();

    String getOrderNumber();

    String getCustomerName();

    String getStatus();

    BigDecimal getTotal();

    Instant getCreatedAt();
}
```

Interface-Projektionen sind praktisch, aber Records sind häufig klarer als API-interne DTOs.

## 24. Native SQL für komplexe Reads

Native SQL ist auf der Query-Seite erlaubt, wenn:

- JPQL unnötig kompliziert wäre,
- Aggregationen stark sind,
- Window Functions gebraucht werden,
- DB-spezifische Performance nötig ist,
- Query getestet ist,
- SQL Injection durch Parameterbindung verhindert wird.

```java
@Query(value = \"\"\"
       SELECT
           date_trunc('month', o.created_at) AS month,
           COUNT(*) AS order_count,
           SUM(o.total) AS revenue
       FROM orders o
       WHERE o.tenant_id = :tenantId
         AND o.created_at >= :from
         AND o.created_at < :to
       GROUP BY date_trunc('month', o.created_at)
       ORDER BY month
       \"\"\", nativeQuery = true)
List<MonthlyRevenueProjection> monthlyRevenue(
        @Param("tenantId") String tenantId,
        @Param("from") Instant from,
        @Param("to") Instant to
);
```

## 25. Materialized View

Für teure Dashboards kann eine vorberechnete Sicht sinnvoll sein.

```sql
CREATE MATERIALIZED VIEW merchant_dashboard_view AS
SELECT
    m.id AS merchant_id,
    COUNT(o.id) AS order_count,
    SUM(o.total) AS revenue,
    AVG(r.rating) AS average_rating
FROM merchants m
LEFT JOIN orders o ON o.merchant_id = m.id
LEFT JOIN ratings r ON r.merchant_id = m.id
GROUP BY m.id;
```

Repository:

```java
@Query(value = \"\"\"
       SELECT merchant_id, order_count, revenue, average_rating
       FROM merchant_dashboard_view
       WHERE merchant_id = :merchantId
       \"\"\", nativeQuery = true)
Optional<MerchantDashboardProjection> findDashboard(@Param("merchantId") Long merchantId);
```

Wichtig: Aktualisierungsstrategie definieren.

---

# Teil H — Eventual Consistency

## 26. Was bedeutet Eventual Consistency?

Wenn Read Model und Write Model getrennt sind, kann das Read Model kurz hinterherhinken.

Ablauf:

```text
1. Command speichert Bestellung.
2. Event wird publiziert.
3. Projection Worker aktualisiert Read Model.
4. Query sieht neue Bestellung erst nach kurzer Verzögerung.
```

Das ist Eventual Consistency.

## 27. Wann ist das akzeptabel?

Akzeptabel bei:

- Dashboards,
- Reporting,
- Suche,
- Analytics,
- Listenansichten,
- Benachrichtigungen,
- Statistiken.

Kritisch bei:

- Zahlungsstatus unmittelbar nach Zahlung,
- verfügbarem Lagerbestand,
- sicherheitskritischen Berechtigungen,
- Limits,
- Kontoständen,
- rechtlich verbindlichen Buchungen.

Regel:

```text
Für kritische Konsistenz: Command-Modell oder starke Konsistenz.
Für Performance-Sichten: Read Model mit akzeptierter Verzögerung.
```

## 28. UX-Hinweis

Nach einem Command kann der Client eine Location erhalten:

```http
201 Created
Location: /api/v1/orders/123
```

Wenn das Read Model verzögert ist, kann die Detail-Query kurz 404 liefern. Dafür braucht es eine definierte Strategie:

- Command gibt minimale Bestätigung,
- UI zeigt „wird verarbeitet“,
- Query hat Retry/Refresh,
- API dokumentiert Verzögerung,
- kritische Details werden direkt aus Write Model gelesen.

---

# Teil I — Events und Projektionen

## 29. Event als Grundlage für Read Model

```java
public record UserRegisteredEvent(
        UUID eventId,
        UserId userId,
        TenantId tenantId,
        String name,
        String email,
        Instant occurredAt
) {}
```

Projection Handler:

```java
@Component
public class UserReadModelProjector {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @EventListener
    public void on(UserRegisteredEvent event) {
        jdbcTemplate.update(\"\"\"
            INSERT INTO user_read_model (
                user_id,
                tenant_id,
                name,
                email,
                registered_at
            )
            VALUES (?, ?, ?, ?, ?)
            \"\"\",
            event.userId().value(),
            event.tenantId().value(),
            event.name(),
            event.email(),
            Timestamp.from(event.occurredAt())
        );
    }
}
```

Für verteilte Systeme: Outbox + Message Broker statt reinem In-Process Event.

## 30. Idempotente Projektion

Events können mehrfach verarbeitet werden. Projektionen müssen idempotent sein.

```sql
INSERT INTO user_read_model (user_id, tenant_id, name, email, registered_at)
VALUES (?, ?, ?, ?, ?)
ON CONFLICT (user_id) DO UPDATE
SET name = EXCLUDED.name,
    email = EXCLUDED.email;
```

## 31. Projection-Versionierung

Wenn sich ein Event ändert, muss das Read Model damit umgehen können.

Regeln:

- Event-Version dokumentieren.
- Neue Felder optional oder mit Default.
- Alte Events weiterhin verarbeiten können.
- Rebuild-Mechanismus für Read Models haben.
- Projection-Code testbar halten.

---

# Teil J — API-Design mit CQRS

## 32. Command Endpoint

Command Endpoint:

```http
POST /api/v1/users
```

Response:

```http
201 Created
Location: /api/v1/users/42
```

Body optional:

```json
{
  "userId": 42
}
```

Nicht ideal:

```json
{
  "id": 42,
  "name": "Max",
  "email": "max@example.com",
  "orderCount": 0,
  "dashboard": {}
}
```

Warum? Das ist Query-Verantwortung.

## 33. Query Endpoint

```http
GET /api/v1/users/42
GET /api/v1/users?name=Max&page=0&size=20
GET /api/v1/users/42/statistics
```

Query Endpoints:

- sind idempotent,
- verändern keinen Zustand,
- sind cachebarer,
- haben klare DTOs,
- dürfen read-optimiert sein.

## 34. Statuscodes

| Operation | Status | Details/Erklärung |
|---|---:|---|
| Command erstellt Ressource | 201 | mit Location |
| Command akzeptiert asynchron | 202 | Verarbeitung läuft |
| Command ohne Body erfolgreich | 204 | z. B. cancel |
| Query erfolgreich | 200 | DTO im Body |
| Query nicht gefunden | 404 | Ressource nicht sichtbar/vorhanden |
| Command-Konflikt | 409 | Zustand erlaubt Änderung nicht |
| Command fachlich ungültig | 422 | Regel verletzt |

---

# Teil K — Security- und SaaS-Aspekte

## 35. Tenant-Isolation

CQRS darf Tenant-Isolation nicht schwächen.

Jede Query braucht Tenant-Kontext:

```java
public record FindMerchantOffersQuery(
        TenantId tenantId,
        MerchantId merchantId,
        Pageable pageable
) {}
```

Repository:

```java
@Query(\"\"\"
       SELECT new com.example.offer.query.OfferSummaryDto(...)
       FROM OfferEntity o
       WHERE o.tenantId = :tenantId
         AND o.merchantId = :merchantId
       \"\"\")
Page<OfferSummaryDto> findSummaries(
        @Param("tenantId") String tenantId,
        @Param("merchantId") Long merchantId,
        Pageable pageable
);
```

Nie:

```java
findByMerchantId(merchantId)
```

wenn Tenant-Isolation erforderlich ist.

## 36. Read Models mit PII

Read Models enthalten oft denormalisierte Daten. Dadurch steigt das Datenschutz- und Security-Risiko.

Regeln:

- keine unnötigen personenbezogenen Daten in Read Models,
- Zugriffskontrolle auch auf Query-Seite,
- PII-Masking in Logs,
- Lösch-/Anonymisierungsprozess für Read Models,
- Rebuild-Prozess nach Datenschutzänderungen,
- keine sensitiven Felder in Suchindizes, wenn nicht zwingend nötig.

## 37. Authorization auf Query-Seite

Schlecht:

```java
public UserDetailDto findDetail(Long userId) {
    return userReadRepository.findDetailById(userId).orElseThrow();
}
```

Besser:

```java
public UserDetailDto findDetail(AccessScope scope, Long userId) {
    accessPolicy.assertCanReadUser(scope, userId);

    return userReadRepository.findDetailById(scope.tenantId(), userId)
            .orElseThrow(() -> new UserNotFoundException(new UserId(userId)));
}
```

Queries sind nicht automatisch harmlos. Lesen ist auch sicherheitsrelevant.

---

# Teil L — Tests

## 38. Command Service testen

```java
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    UserWriteRepository userWriteRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ApplicationEventPublisher events;

    @InjectMocks
    UserCommandService service;

    @Test
    void register_createsUser_whenEmailIsFree() {
        var command = new RegisterUserCommand(
                "Max Mustermann",
                "max@example.com",
                "Secret123"
        );

        when(userWriteRepository.existsByEmail(new EmailAddress("max@example.com")))
                .thenReturn(false);
        when(passwordEncoder.encode("Secret123"))
                .thenReturn("hashed");

        var result = service.handle(command);

        assertThat(result.userId()).isNotNull();
        verify(userWriteRepository).save(any(User.class));
        verify(events).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void register_throwsEmailAlreadyExists_whenEmailIsTaken() {
        var command = new RegisterUserCommand(
                "Max Mustermann",
                "max@example.com",
                "Secret123"
        );

        when(userWriteRepository.existsByEmail(new EmailAddress("max@example.com")))
                .thenReturn(true);

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userWriteRepository, never()).save(any());
    }
}
```

## 39. Query Repository testen

```java
@DataJpaTest
class UserReadRepositoryTest {

    @Autowired
    UserReadRepository repository;

    @Test
    @Sql("/test-data/users-with-orders.sql")
    void findSummaries_returnsFlatProjection_withoutLoadingFullDomain() {
        var result = repository.findSummaries(
                "Max",
                PageRequest.of(0, 20)
        );

        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.name()).contains("Max");
                    assertThat(user.orderCount()).isGreaterThanOrEqualTo(1);
                });
    }
}
```

## 40. Controller-Test für Command

```java
@WebMvcTest(UserController.class)
class UserControllerCommandTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserCommandService commandService;

    @MockBean
    UserQueryService queryService;

    @Test
    void register_returns201_andLocation() throws Exception {
        when(commandService.handle(any()))
                .thenReturn(new RegisterUserResult(new UserId(42L)));

        var body = \"\"\"
                {
                  "name": "Max Mustermann",
                  "email": "max@example.com",
                  "password": "Secret123"
                }
                \"\"";

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/42"));
    }
}
```

## 41. Controller-Test für Query

```java
@Test
void findDetail_returnsUserDetail() throws Exception {
    when(queryService.findDetail(42L))
            .thenReturn(new UserDetailDto(
                    42L,
                    "Max Mustermann",
                    "max@example.com",
                    LocalDate.of(2024, 1, 1),
                    3,
                    new BigDecimal("249.90"),
                    List.of("O-1", "O-2")
            ));

    mockMvc.perform(get("/api/v1/users/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.orderCount").value(3));
}
```

---

# Teil M — Typische Fehler

## 42. Fehler 1: CQRS als Overengineering

Schlecht:

```text
Für drei CRUD-Tabellen:
CommandBus, QueryBus, EventStore, ProjectionWorker, ReadDB, WriteDB.
```

Besser:

```text
Einfache CRUD-Anwendung: klassischer Service reicht.
Nichttriviale Domäne: Command/Query-Service logisch trennen.
```

## 43. Fehler 2: Command gibt volles DTO zurück

Schlecht:

```java
public UserDetailDto register(RegisterUserCommand command) {
    // ...
}
```

Besser:

```java
public RegisterUserResult register(RegisterUserCommand command) {
    // ...
}
```

## 44. Fehler 3: Query lädt Domain Aggregate

Schlecht:

```java
var orders = orderRepository.findAll();
return orders.stream().map(orderMapper::toSummary).toList();
```

Besser:

```java
return orderReadRepository.findSummaries(query);
```

## 45. Fehler 4: Query verändert Zustand

Schlecht:

```java
public OfferDto findAndIncrementViewCount(OfferId id) {
    // liest und schreibt
}
```

Besser:

```java
findOffer(id);
recordOfferViewed(id);
```

Oder bewusst als Command modellieren.

## 46. Fehler 5: Read Model ohne Security

Schlecht:

```java
dashboardRepository.findByMerchantId(merchantId);
```

Besser:

```java
dashboardRepository.findByTenantIdAndMerchantId(scope.tenantId(), merchantId);
```

## 47. Fehler 6: Eventual Consistency nicht kommuniziert

Problem:

Der Client erstellt eine Ressource und die Query findet sie sofort noch nicht.

Besser:

- 202/201 sauber nutzen,
- Location setzen,
- UI-Zustand „in Verarbeitung“,
- Read-Model-Verzögerung dokumentieren,
- kritische Reads aus Write Model bedienen.

## 48. Fehler 7: Read Model enthält zu viele PII-Daten

Schlecht:

```text
user_search_index enthält Name, E-Mail, Telefon, Adresse, IBAN.
```

Besser:

```text
Nur Felder speichern, die für Suche/Anzeige wirklich nötig sind.
```

## 49. Fehler 8: Kein Rebuild-Konzept

Wenn Read Models durch Events aufgebaut werden, muss es einen Rebuild geben.

Fragen:

- Wie wird ein Read Model neu aufgebaut?
- Welche Events werden replayed?
- Wie wird Downtime vermieden?
- Wie werden Versionen behandelt?

## 50. Fehler 9: Query-Seite nutzt `@Transactional` schreibend

Schlecht:

```java
@Transactional
public DashboardDto dashboard(...) {
    // read only
}
```

Besser:

```java
@Transactional(readOnly = true)
public DashboardDto dashboard(...) {
    // read only
}
```

## 51. Fehler 10: Write- und Read-DTO identisch

Wenn ein DTO gleichzeitig Request, Command, Entity-Mapping und Response ist, ist die Trennung nicht erreicht.

---

# Teil N — Schritt-für-Schritt-Anleitung

## 52. Neuen Use Case umsetzen

### Schritt 1: Use Case klassifizieren

```text
Ändert der Use Case Zustand?
Ja → Command.
Nein → Query.
```

### Schritt 2: Command oder Query benennen

Gute Namen:

```java
RegisterUserCommand
CancelOrderCommand
FindUsersQuery
LoadMerchantDashboardQuery
ExportOrdersQuery
```

Schlechte Namen:

```java
UserRequest
DataDto
ProcessInput
DoUserThing
```

### Schritt 3: Service wählen

```text
Command → UserCommandService
Query   → UserQueryService
```

### Schritt 4: Transaktion setzen

```java
@Transactional
```

für Command.

```java
@Transactional(readOnly = true)
```

für Query.

### Schritt 5: Modell wählen

Command:

```text
Domain / Aggregate / Entity / Value Objects
```

Query:

```text
DTO / Projection / View / Native SQL
```

### Schritt 6: Rückgabe bewusst begrenzen

Command:

```text
ID, Version, Status, Location
```

Query:

```text
genau die View-Daten
```

### Schritt 7: Tests schreiben

Command-Test:

- Invarianten,
- Events,
- Fehlerfälle.

Query-Test:

- Projektion,
- SQL,
- Paging,
- Security,
- keine N+1-Probleme.

---

# Teil O — Review-Checkliste

## 53. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Use Case klassifiziert | Command oder Query eindeutig? | `CancelOrderCommand` | Pflicht |
| Command verändert Zustand | Command-Seite mit Transaktion | `@Transactional` | Pflicht |
| Query verändert keinen Zustand | read-only | `@Transactional(readOnly = true)` | Pflicht |
| Command-Rückgabe minimal | ID/Version statt DetailDTO | `RegisterUserResult` | Pflicht |
| Query nutzt Projektion | kein volles Entity-Laden | `UserSummaryDto` | Pflicht bei Listen |
| Read Repository getrennt | Query-optimiert | `UserReadRepository` | Prüfen |
| Write Repository konsistent | Domain/Entity-orientiert | `UserWriteRepository` | Prüfen |
| Keine Entity im Controller | API-DTOs | `RegisterUserRequest` | Pflicht |
| Security auf Query | Tenant/Auth geprüft | `AccessScope` | Pflicht |
| Native SQL sicher | Parameterbindung | `:tenantId` | Pflicht |
| Paging begrenzt | Max Size | `@Max(100)` | Pflicht |
| Eventual Consistency | dokumentiert | Read Model Lag | Pflicht bei Stufe 3/4 |
| Read Model PII-arm | Datenminimierung | keine IBAN | Pflicht |
| Rebuild möglich | Projektionen wiederherstellbar | Replay/Refresh | Pflicht bei Projektionen |
| Tests vorhanden | Command + Query getrennt | Unit/JPA/Web | Pflicht |
| Kein Overengineering | Stufe passend gewählt | keine separate DB ohne Grund | Pflicht |

---

# Teil P — Automatisierbare Prüfungen

## 54. Mögliche Regeln

```text
- QueryServices dürfen keine WriteRepositories injizieren.
- CommandServices dürfen keine Query-DTOs zurückgeben.
- QueryServices sind readOnly transactional.
- Controller geben keine Entities zurück.
- ReadRepositories dürfen DTO-Projektionen verwenden.
- WriteRepositories dürfen nicht für Reporting-Queries missbraucht werden.
- Query-Methoden dürfen keine save/delete/update-Aufrufe enthalten.
- Native Queries müssen Parameterbindung nutzen.
```

### 54.1 ArchUnit-Beispiel

```java
@ArchTest
static final ArchRule query_services_should_not_depend_on_write_repositories =
        noClasses()
                .that().haveSimpleNameEndingWith("QueryService")
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("WriteRepository")
                .because("CQRS: Query-Seite darf nicht von Write-Repositories abhängen.");
```

```java
@ArchTest
static final ArchRule command_services_should_not_depend_on_read_repositories =
        noClasses()
                .that().haveSimpleNameEndingWith("CommandService")
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("ReadRepository")
                .because("CQRS: Command-Seite darf nicht von Query-optimierten Read-Repositories abhängen.");
```

---

# Teil Q — Migration bestehender Services

## 55. Schrittweise Migration

1. Service-Klassen mit vielen Lese- und Schreibmethoden identifizieren.
2. Methoden in Command und Query klassifizieren.
3. `XCommandService` und `XQueryService` einführen.
4. Schreibende Methoden in CommandService verschieben.
5. Lesende Methoden in QueryService verschieben.
6. Commands und Queries als eigene Records definieren.
7. Command-Rückgaben minimieren.
8. Query-DTOs pro UI-/API-Bedarf definieren.
9. Read Repository für Projektionen einführen.
10. Listenansichten von Entity-Mapping auf DTO-Projektion umstellen.
11. Reporting-Queries in Read Repository verschieben.
12. `@Transactional(readOnly = true)` für Query-Seite setzen.
13. Security/Tenant-Kontext auf Query-Seite prüfen.
14. N+1-Tests für kritische Queries ergänzen.
15. Bei Bedarf Read Models oder Views einführen.
16. Eventual-Consistency-Risiken dokumentieren.
17. Alte Mischmethoden entfernen.

---

# Teil R — Ausnahmen

## 56. Zulässige Ausnahmen

| Ausnahme | Bedingung |
|---|---|
| Kein CQRS | einfache CRUD-Funktion, geringe Komplexität |
| Command gibt DTO zurück | kleine interne API, bewusst entschieden, kein schweres Read Model |
| Query nutzt Entity | kleiner Einzelfall, keine N+1-/Performance-Risiken |
| Ein Repository | kleine Anwendung, keine getrennten Anforderungen |
| Native SQL | Query-Seite, parametrisiert, getestet |
| Separate Read DB | nur mit begründetem Skalierungs-/Such-/Reportingbedarf |
| Eventual Consistency | fachlich akzeptiert und dokumentiert |

Nicht zulässig:

- Entity direkt als öffentlicher Response-Vertrag,
- Query mit Seiteneffekt ohne Kennzeichnung als Command,
- freie native Query-Konkatenation,
- Read Model ohne Zugriffskontrolle,
- separate CQRS-Infrastruktur ohne fachlichen oder technischen Grund.

---

# Teil S — Definition of Done

## 57. Definition of Done

Ein CQRS-Codebereich erfüllt diese Richtlinie, wenn:

1. jeder Use Case als Command oder Query klassifiziert ist,
2. Command- und Query-Services logisch getrennt sind, sofern der Bereich nicht trivial ist,
3. Commands klare Intent-Records verwenden,
4. Queries klare Query-Records oder Parameterobjekte verwenden,
5. Command-Methoden Zustand verändern und transaktional sind,
6. Query-Methoden keinen Zustand verändern und read-only sind,
7. Command-Rückgaben minimal sind,
8. Query-Rückgaben auf konkrete DTOs/Projektionen gehen,
9. Listenansichten keine unnötigen vollständigen Entities laden,
10. Reporting nicht im Java-Speicher aggregiert, wenn SQL/View geeigneter ist,
11. Read Repositories für komplexe Sichten existieren,
12. Write Repositories nicht für Reporting missbraucht werden,
13. Tenant-/Security-Kontext auch auf Query-Seite geprüft wird,
14. native Queries parametrisiert sind,
15. Page Size und Filter validiert sind,
16. Eventual Consistency dokumentiert ist, falls Read Models asynchron sind,
17. Read Models keine unnötigen sensiblen Daten enthalten,
18. Projektionen getestet sind,
19. Command-Logik getestet ist,
20. Architekturregeln oder Reviews die Trennung absichern.

---

## 58. Quellen und weiterführende Literatur

- Microsoft Azure Architecture Center — CQRS Pattern: https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs
- Martin Fowler — CQRS: https://martinfowler.com/bliki/CQRS.html
- Martin Fowler — Command Query Separation: https://martinfowler.com/bliki/CommandQuerySeparation.html
- Confluent Developer — CQRS Pattern: https://developer.confluent.io/patterns/compositional-patterns/command-query-responsibility-segregation/
- Spring Data JPA Reference — Projections: https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html
- Spring Data JPA Reference — Query Methods: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- Spring Framework Reference — Transaction Management: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- Bertrand Meyer — Object-Oriented Software Construction
- Eric Evans — Domain-Driven Design: Tackling Complexity in the Heart of Software
- Vaughn Vernon — Implementing Domain-Driven Design
