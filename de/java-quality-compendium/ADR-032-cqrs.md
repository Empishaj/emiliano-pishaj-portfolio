# ADR-032 — CQRS: Command Query Responsibility Segregation

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Daten               |

---

## Kontext & Problem

In typischen Anwendungen konkurrieren Schreib- und Lesezugriffe um dasselbe Modell. Das Schreibmodell (Command) braucht Konsistenz, Validierung und Transaktionen. Das Lesemodell (Query) braucht Performance, Denormalisierung und flache Projektionen. Diese Anforderungen sind fundamental verschieden. CQRS trennt sie explizit.

---

## Das CQRS-Prinzip

```
Ohne CQRS:
UserService.findById()    → UserEntity (schwer für Abfragen mit JOINs)
UserService.register()    → UserEntity (gut für Validierung)
Beide benutzen dasselbe Modell → Kompromisse überall

Mit CQRS:
Commands → Schreibmodell → Konsistenz, Invarianten, Transaktionen
Queries  → Lesemodell   → Performance, Projektionen, Denormalisierung
```

---

## ❌ Schlecht — ein Modell für alles

```java
@Service
public class UserService {
    // Schreiben: braucht Validierung, Transaktionen, Domain-Objekte
    @Transactional
    public UserDto register(RegisterCommand cmd) { ... }

    // Lesen: braucht JOINs, Pagination, flache DTOs
    // Aber: muss UserEntity laden + N+1-Problem + unnötige Felder
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(userMapper::toDto); // Lädt volle Entity für simples Listing
    }

    // Reporting: braucht aggregierte Daten aus mehreren Tabellen
    // Mit einer Entity nicht sauber darstellbar
    public UserStatisticsDto getStatistics(Long userId) {
        var user   = userRepository.findById(userId).orElseThrow();
        var orders = orderRepository.findByUserId(userId); // Separate Query
        var total  = orders.stream()
            .mapToDouble(o -> o.total().doubleValue()).sum(); // Im Speicher aggregiert
        return new UserStatisticsDto(user.name(), orders.size(), total);
    }
}
```

---

## ✅ Gut — Command- und Query-Seite getrennt

### Command-Seite: Domänenlogik, Transaktionen, Konsistenz

```java
// Command: Schreib-Intent als Value Object
public record RegisterUserCommand(
    @NotBlank String name,
    @Email    String email,
    @NotBlank String password
) {}

public record RegisterUserResult(UserId userId) {}

// Command Handler: verarbeitet Commands, persistiert Domain-Objekte
@Service
public class UserCommandService {

    @Transactional
    public RegisterUserResult handle(RegisterUserCommand command) {
        if (userWriteRepository.existsByEmail(command.email()))
            throw new EmailAlreadyExistsException(command.email());

        var user = User.register(
            UserId.generate(),
            command.name(),
            new Email(command.email()),
            passwordEncoder.encode(command.password())
        );

        userWriteRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.id()));
        return new RegisterUserResult(user.id());
    }
}
```

### Query-Seite: optimierte Leseprojektionen, kein Domain-Overhead

```java
// Query: Lese-Intent klar benannt
public record FindUsersQuery(
    String  nameFilter,
    Pageable pageable
) {}

// Query-DTOs: flach, genau was die UI braucht — keine Domain-Entities
public record UserSummaryDto(Long id, String name, String email, int orderCount) {}

public record UserDetailDto(
    Long id, String name, String email,
    String avatarUrl, LocalDate registeredAt,
    int orderCount, BigDecimal totalSpent,
    List<String> recentOrderIds
) {}

// Query Service: direkte SQL-Projektionen, kein N+1, kein Domain-Overhead
@Service
@Transactional(readOnly = true)
public class UserQueryService {

    // Direkte Projektion auf DTO — kein Laden der vollen Entity
    public Page<UserSummaryDto> handle(FindUsersQuery query) {
        return userReadRepository.findSummaries(query.nameFilter(), query.pageable());
    }

    // Denormalisierte Sicht mit aggregierten Daten in einer Query
    public UserDetailDto findDetail(Long userId) {
        return userReadRepository.findDetailById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
}

// Read Repository: optimierte Queries, native SQL wenn nötig
public interface UserReadRepository {

    @Query("""
        SELECT new com.example.dto.UserSummaryDto(
            u.id, u.name, u.email, COUNT(o)
        )
        FROM User u
        LEFT JOIN Order o ON o.userId = u.id
        WHERE (:nameFilter IS NULL OR u.name LIKE %:nameFilter%)
        GROUP BY u.id, u.name, u.email
        """)
    Page<UserSummaryDto> findSummaries(String nameFilter, Pageable pageable);

    // Native SQL für komplexe Reporting-Queries
    @Query(value = """
        SELECT u.id, u.name, u.email,
               COUNT(o.id)       AS order_count,
               SUM(o.total)      AS total_spent,
               u.registered_at
        FROM users u
        LEFT JOIN orders o ON o.user_id = u.id
        WHERE u.id = :userId
        GROUP BY u.id
        """, nativeQuery = true)
    Optional<UserDetailDto> findDetailById(Long userId);
}
```

### Controller: klare Trennung im API-Layer

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService   queryService;

    // Command Endpoint: schreibend, gibt nur ID zurück
    @PostMapping
    @ResponseStatus(CREATED)
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        var result   = commandService.handle(mapper.toCommand(request));
        var location = URI.create("/api/v1/users/" + result.userId().value());
        return ResponseEntity.created(location).build();
    }

    // Query Endpoints: lesend, flache DTOs
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

---

## CQRS Skalierungsstufen

```
Stufe 1 (dieser ADR): Logische Trennung
→ Command- und Query-Services getrennt
→ Optimierte Read-Repositories
→ Ein Datenbankschema

Stufe 2: Separate Read-Models in derselben DB
→ Materialized Views / Query-optimierte Tabellen
→ Command-Seite: normalisiert, konsistent
→ Query-Seite: denormalisiert, performant
→ Synchronisation durch Events

Stufe 3: Separate Datenbanken (mit Event Sourcing → ADR-033)
→ Command DB: Write-optimiert (PostgreSQL)
→ Query DB: Read-optimiert (Elasticsearch, Redis, MongoDB)
→ Eventual Consistency durch Event-Projektion
```

---

## Konsequenzen

**Positiv:** Query-Seite kann unabhängig optimiert werden ohne Command-Seite zu berühren. Read-Models können für verschiedene UI-Bedürfnisse separat optimiert werden. Klare Trennung von Schreib- und Lese-Komplexität.

**Negativ:** Mehr Klassen und Interfaces. Bei Stufe 3 (separate DBs): Eventual Consistency muss akzeptiert werden. Für einfache CRUD-Anwendungen ist CQRS Overengineering.

---

## 💡 Guru-Tipps

- **Stufe 1 immer**, Stufen 2/3 nur wenn Performance es erfordert.
- **Command gibt minimal zurück**: Nur ID oder Confirmation — kein volles DTO. Wer das vollständige Objekt braucht, macht eine Query.
- **CQS auf Methoden-Ebene** (Command Query Separation, Bertrand Meyer): Methoden die etwas tun (Commands) geben nichts zurück. Methoden die etwas zurückgeben (Queries) verändern keinen Zustand.

---

## Verwandte ADRs

- [ADR-031](ADR-031-hexagonal-architecture.md) — Hexagonal Architecture als strukturelle Basis.
- [ADR-033](ADR-033-event-sourcing.md) — Event Sourcing als Ergänzung für Stufe 3.
- [ADR-016](ADR-016-datenbank-jpa-n-plus-eins.md) — Read-Projektionen lösen N+1-Probleme.
