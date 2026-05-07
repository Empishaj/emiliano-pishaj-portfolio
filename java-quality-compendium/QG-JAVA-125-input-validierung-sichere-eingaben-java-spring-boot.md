# QG-JAVA-125 — Input-Validierung: Sichere Eingaben in Java- und Spring-Boot-Anwendungen

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-030 |
| Titel | Input-Validierung: Sichere Eingaben in Java- und Spring-Boot-Anwendungen |
| Status | Accepted / verbindlicher Standard für Eingabevalidierung |
| Version | 1.0.0 |
| Datum | 2025-11-12 |
| Kategorie | Security / OWASP / API Design / Bean Validation |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Java-Baseline | Java 21 |
| Framework-Kontext | Spring Boot 3.x, Spring Framework 6.x, Jakarta Bean Validation, Hibernate Validator, Spring Data JPA |
| Geltungsbereich | HTTP Request Body, Query Parameter, Path Variables, Header, Kafka-/RabbitMQ-Nachrichten, Batch-/CSV-Importe, externe API-Payloads, Service-Methoden, Repository-Zugriffe |
| Verbindlichkeit | Jede Eingabe von außen wird validiert, normalisiert, typisiert und begrenzt. Keine Entity wird direkt als Request-Body verwendet. Datenbankzugriffe werden parametrisiert. Freitext wird nicht blocklist-basiert „gereinigt“, sondern über klare erlaubte Formate, Längen und Kontexte verarbeitet. |
| Technische Validierung | Gegen OWASP Input Validation Cheat Sheet, OWASP SQL Injection Prevention Cheat Sheet, Spring MVC Validation, Spring `@Validated` und Jakarta Bean Validation eingeordnet |
| Kurzentscheidung | Alle externen Eingaben werden an Systemgrenzen validiert. Zusätzlich werden relevante Service-Methoden mit Method Validation abgesichert. Datenbankzugriffe verwenden parametrisierte Queries. Allowlist-Validierung, Längenbegrenzung und starke Typen sind Standard. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Eingaben in Java- und Spring-Boot-Anwendungen sicher, nachvollziehbar und einheitlich validiert werden.

Input-Validierung bedeutet: Eine Anwendung prüft, ob eingehende Daten in Typ, Format, Länge, Wertebereich und fachlichem Kontext erlaubt sind, bevor sie weiterverarbeitet werden.

Das klingt einfach. In der Praxis ist es eine der wichtigsten Sicherheits- und Qualitätsgrenzen einer Anwendung. Viele schwere Fehler beginnen mit einem kleinen Satz:

```text
Der Client wird schon richtige Daten schicken.
```

Das ist falsch.

Jede Eingabe kann falsch, unvollständig, manipuliert, zu groß, zu lang, in falscher Kodierung, fachlich ungültig oder absichtlich bösartig sein. Das gilt nicht nur für öffentliche HTTP-APIs. Es gilt auch für interne Service-Aufrufe, Message-Queues, Scheduler, CSV-Importe, Webhooks, Admin-Oberflächen und Daten aus fremden Systemen.

Diese Guideline sorgt dafür, dass Entwickler im Daily Business wissen:

- Wo muss validiert werden?
- Welche Bean-Validation-Annotationen sind sinnvoll?
- Wann reicht `@Valid`?
- Wann braucht man `@Validated`?
- Warum sind DTOs Pflicht?
- Warum darf eine JPA-Entity nicht direkt als Request-Body genutzt werden?
- Wie verhindert man SQL-Injection?
- Wie validiert man Query-Parameter und Path-Variablen?
- Wie schützt man Message-Consumer?
- Wie behandelt man freie Texte?
- Wie testet man Validierung?
- Was gehört ins Review?

---

## 2. Kurzregel

Vertraue keiner Eingabe. Validiere an jeder Systemgrenze. Verwende separate Request-DTOs mit Jakarta Bean Validation. Aktiviere Validierung im Controller mit `@Valid`. Nutze Method Validation mit `@Validated`, wenn Service-Methoden direkt von mehreren Einstiegspunkten aufgerufen werden. Verwende Allowlist-Regeln, Längenbegrenzungen und starke Typen. Verwende keine JPA-Entity als Request-Body. Baue keine SQL-/JPQL-/Native-Queries per String-Konkatenation. Nutze parametrisierte Queries.

---

## 3. Warum Input-Validierung im Java-Alltag wichtig ist

Ein Java-Entwickler sieht Input-Validierungsprobleme oft erst spät. Der Code kompiliert. Der Endpoint antwortet. Der Happy-Path-Test ist grün.

Das Problem liegt in den Randfällen:

- `firstName = null` führt später zu `NullPointerException`.
- `email = "not-an-email"` landet in der Datenbank.
- `age = -999` wird akzeptiert.
- `quantity = -1` erzeugt eine Rückzahlung statt einer Zahlung.
- `pageSize = 1000000` belastet Datenbank und Speicher.
- `fileName = "../../etc/passwd"` ermöglicht Path-Traversal.
- `sort = "name desc; drop table users"` landet in einer dynamischen Query.
- `role = "ADMIN"` wird durch Mass Assignment gesetzt.
- `tenantId` wird manipuliert und führt zu Cross-Tenant-Datenzugriff.
- Eine Kafka-Nachricht kommt nicht vom erwarteten Producer oder enthält ungültige Werte.

Input-Validierung ist deshalb nicht nur Security. Sie ist auch Stabilität, Datenqualität, Fehlersuche, Betriebssicherheit und saubere Fachlogik.

---

## 4. Grundlagen kurz erklärt

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Input | Jede Eingabe, die von außerhalb des aktuellen Vertrauensbereichs kommt. | HTTP Body, Query-Parameter, Kafka Event |
| Validierung | Prüfung, ob Daten zulässig sind. | `@NotBlank`, `@Size`, `@Email` |
| Normalisierung | Eingabe in ein einheitliches Format bringen. | E-Mail trimmen und lowercase |
| Sanitization | Entfernen oder Umschreiben unerwünschter Inhalte. | HTML bereinigen |
| Allowlist | Nur explizit erlaubte Zeichen/Werte zulassen. | Enum, Regex, Wertebereich |
| Blocklist | Verbotene Muster suchen. | `<script>` blockieren |
| DTO | Data Transfer Object, eigenes Transportobjekt für API-Eingaben/Ausgaben. | `CreateUserRequest` |
| Bean Validation | Standard für deklarative Validierung per Annotationen. | Jakarta Validation |
| `@Valid` | Aktiviert verschachtelte Objektvalidierung an Parametern/Feldern. | `@Valid @RequestBody` |
| `@Validated` | Spring-Annotation für Method Validation und Gruppenvalidierung. | Service-Klasse validieren |
| Constraint | Validierungsregel. | `@Min(1)`, `@Pattern(...)` |
| Custom Validator | Eigene Validierungsannotation mit eigener Logik. | `@ValidDateRange` |
| SQL-Injection | Angreifer verändert Query-Logik über Eingabe. | `' OR '1'='1` |
| Mass Assignment | Client kann Felder setzen, die er nicht setzen darf. | `role=ADMIN` im Request |
| Defense in Depth | Mehrere Schutzschichten statt nur eine. | Controller + Service + DB |

---

## 5. Die zentrale Sicherheitslinie

Die zentrale Linie lautet:

```text
Eingaben werden an Grenzen geprüft.
Fachregeln werden im Fachmodell geschützt.
Datenbankzugriffe werden parametrisiert.
Ausgaben werden kontextbezogen kodiert.
```

Wichtig: Input-Validierung allein löst nicht alle Sicherheitsprobleme.

Input-Validierung schützt vor unzulässigen Eingaben. Sie ersetzt nicht:

- Zugriffskontrolle,
- Output-Encoding gegen XSS,
- parametrisierte Queries gegen SQL-Injection,
- CSRF-Schutz bei Browser-Flows,
- Authentifizierung,
- Autorisierung,
- Rate Limiting,
- Tenant-Isolation,
- PII-Masking,
- sichere Dateiablage,
- sichere Deserialisierung.

Ein Beispiel:

```java
@Pattern(regexp = "^[\\p{L} .'-]+$")
String name
```

Das hilft, Namen zu begrenzen. Wenn dieser Name später in HTML ausgegeben wird, ist trotzdem korrektes Output-Encoding nötig. Validierung und Encoding sind unterschiedliche Schutzmaßnahmen.

---

## 6. Anwendung im Daily Business eines Java-Entwicklers

Wenn du einen neuen Endpoint, Message Consumer, Import oder Service-Einstieg baust, arbeitest du nach dieser Reihenfolge.

### Schritt 1: Eingangsgrenze identifizieren

Frage:

```text
Wo kommt die Eingabe her?
```

Mögliche Grenzen:

- `@RestController`,
- `@KafkaListener`,
- `@RabbitListener`,
- Scheduler mit externen Parametern,
- CSV-/Excel-Import,
- Webhook,
- Admin UI,
- CLI,
- externe API,
- Datenbankfeld aus Fremdsystem.

### Schritt 2: Eigenes Request-DTO definieren

Keine JPA-Entity als Request-Body. Nur erlaubte Felder ins DTO.

```java
public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull @Min(18) @Max(120) Integer age
) {}
```

### Schritt 3: Typen stark machen

Nutze Enums, UUID, Integer, LocalDate, Value Objects statt freier Strings, wenn möglich.

```java
public enum SortDirection {
    ASC,
    DESC
}
```

### Schritt 4: Constraints setzen

Prüfe:

- Pflichtfeld,
- Länge,
- Format,
- Wertebereich,
- erlaubte Werte,
- verschachtelte Objekte,
- Collection-Größe,
- fachliche Beziehungen zwischen Feldern.

### Schritt 5: Validierung aktivieren

Im Controller:

```java
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    ...
}
```

Bei Query-Parametern:

```java
@Validated
@RestController
public class UserController {
    ...
}
```

### Schritt 6: Service-Grenze absichern, wenn nötig

Wenn der Service auch von Kafka, Scheduler oder anderen Services verwendet wird:

```java
@Service
@Validated
public class UserService {

    public UserResponse createUser(@Valid CreateUserRequest request) {
        ...
    }
}
```

### Schritt 7: Repository sicher halten

Keine String-Konkatenation. Nur parametrisierte Queries oder Spring-Data-Methoden.

### Schritt 8: Fehler zentral ausgeben

Validation Errors werden über `@RestControllerAdvice` einheitlich als Problem Detail oder standardisiertes Fehlerformat ausgegeben.

### Schritt 9: Tests schreiben

Mindestens:

- gültiger Request,
- fehlendes Pflichtfeld,
- zu langer Wert,
- ungültiges Format,
- Randwerte,
- Mass-Assignment-Test,
- SQL-Injection-Test für Query-Pfade,
- Message-Consumer mit ungültiger Payload.

### Schritt 10: Review-Checkliste anwenden

Kein PR mit neuer Eingabegrenze ohne Validierungsprüfung.

---

# Teil A — Systemgrenzen und Vertrauensmodell

## 7. Eingaben sind nicht nur HTTP

### 7.1 Typische Eingabequellen

| Quelle | Beispiel | Risiko |
|---|---|---|
| Request Body | JSON für User-Erstellung | Mass Assignment, ungültige Werte |
| Query Parameter | `?page=1000000` | DoS, langsame Queries |
| Path Variable | `/users/{id}` | ungültige IDs |
| Header | `X-Tenant-Id` | Tenant-Manipulation |
| Cookies | Sessiondaten | Manipulation |
| Kafka Message | UserCreatedEvent | kompromittierter Producer |
| RabbitMQ Message | ImportJob | ungültige Payload |
| Webhook | Payment Provider | gefälschte Events |
| CSV Upload | Produktimport | Formel-/CSV-Injection, große Dateien |
| Datei-Name | Download API | Path-Traversal |
| Externe API | ERP-Daten | fremdes Modell, falsche Annahmen |
| Datenbank | Legacy-Daten | historisch ungültige Daten |

### 7.2 Interne Services sind keine automatisch sichere Zone

In Microservices gilt:

```text
Intern ist nicht automatisch vertrauenswürdig.
```

Gründe:

- ein interner Service kann kompromittiert sein,
- ein Event kann aus alter Version stammen,
- ein Producer kann falsche Daten senden,
- ein Test-Tool kann Produktionsdaten beschädigen,
- ein Admin-Endpoint kann falsch benutzt werden,
- ein Tenant-Kontext kann fehlen.

Deshalb wird an jeder Systemgrenze validiert.

---

# Teil B — Bean Validation im Request-DTO

## 8. Request-DTO statt Entity

### 8.1 Schlechte Anwendung: Entity als Request Body

```java
@PostMapping("/users")
public UserEntity createUser(@RequestBody UserEntity user) {
    return userRepository.save(user);
}
```

Problem:

Der Client kann Felder setzen, die er nicht setzen darf:

```json
{
  "firstName": "Max",
  "email": "max@example.com",
  "role": "ADMIN",
  "active": true,
  "deleted": false,
  "tenantId": "another-tenant"
}
```

Das nennt man Mass Assignment.

### 8.2 Gute Anwendung: eigenes Request-DTO

```java
public record CreateUserRequest(

        @NotBlank(message = "Vorname ist Pflicht")
        @Size(min = 2, max = 50, message = "Vorname muss 2 bis 50 Zeichen haben")
        @Pattern(
                regexp = "^[\\p{L} '-]+$",
                message = "Vorname enthält ungültige Zeichen"
        )
        String firstName,

        @NotBlank(message = "Nachname ist Pflicht")
        @Size(min = 2, max = 50, message = "Nachname muss 2 bis 50 Zeichen haben")
        @Pattern(
                regexp = "^[\\p{L} '-]+$",
                message = "Nachname enthält ungültige Zeichen"
        )
        String lastName,

        @NotBlank(message = "E-Mail ist Pflicht")
        @Email(message = "Keine gültige E-Mail-Adresse")
        @Size(max = 255, message = "E-Mail darf höchstens 255 Zeichen haben")
        String email,

        @NotNull(message = "Alter ist Pflicht")
        @Min(value = 18, message = "Mindestalter ist 18")
        @Max(value = 120, message = "Alter darf höchstens 120 sein")
        Integer age
) {}
```

Controller:

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request) {

    var response = userService.createUser(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}
```

### 8.3 Warum Records gut passen

Records sind für Request-DTOs gut geeignet, weil sie:

- kompakt sind,
- unveränderliche Komponenten haben,
- automatisch `equals`, `hashCode` und `toString` bekommen,
- keine Setter haben,
- gut mit Jackson und Bean Validation zusammenarbeiten.

Trotzdem gilt: Ein Record mit Constraints wird nur validiert, wenn die Validierung am Einstiegspunkt aktiv ist.

---

## 9. Standard-Constraints

| Constraint | Bedeutung | Beispiel |
|---|---|---|
| `@NotNull` | Wert darf nicht `null` sein. | ID, Datum |
| `@NotBlank` | String darf nicht null, leer oder nur Leerzeichen sein. | Name |
| `@NotEmpty` | String/Collection darf nicht leer sein. | Liste |
| `@Size` | Länge oder Collection-Größe begrenzen. | max. 255 Zeichen |
| `@Email` | E-Mail-Format prüfen. | `email` |
| `@Min` | Mindestwert. | `page >= 0` |
| `@Max` | Maximalwert. | `size <= 100` |
| `@Positive` | Wert muss positiv sein. | Menge |
| `@PositiveOrZero` | Wert muss null oder positiv sein. | Seitenindex |
| `@Pattern` | Regex-Format prüfen. | erlaubte Zeichen |
| `@Past` | Datum muss in Vergangenheit liegen. | Geburtsdatum |
| `@Future` | Datum muss in Zukunft liegen. | Ablaufdatum |
| `@Valid` | verschachteltes Objekt validieren. | Adresse im Request |

### 9.1 Collections validieren

```java
public record CreateOrderRequest(

        @NotNull
        CustomerId customerId,

        @NotEmpty(message = "Bestellung braucht mindestens eine Position")
        @Size(max = 50, message = "Maximal 50 Positionen pro Bestellung")
        List<@Valid OrderItemRequest> items
) {}
```

```java
public record OrderItemRequest(

        @NotBlank
        String productId,

        @NotNull
        @Min(1)
        @Max(999)
        Integer quantity
) {}
```

Wichtig: `@Valid` auf der Liste sorgt dafür, dass die Elemente validiert werden.

---

## 10. `@Valid` im Controller

### 10.1 Request Body

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    ...
}
```

Ohne `@Valid` werden Constraints auf dem Request-DTO bei diesem Einstieg nicht automatisch geprüft.

### 10.2 Query Parameter und Path Variable

Für Validierung einzelner Controller-Methodenparameter wird in Spring typischerweise Method Validation genutzt. Dazu wird der Controller mit `@Validated` annotiert.

```java
@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping
    public List<UserResponse> searchUsers(
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 100)
            String query,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size) {

        return userService.search(query, page, size);
    }

    @GetMapping("/{id}")
    public UserResponse findById(
            @PathVariable
            @NotNull
            UUID id) {

        return userService.findById(id);
    }
}
```

### 10.3 Header validieren

```java
@GetMapping("/tenant-data")
public TenantDataResponse tenantData(
        @RequestHeader("X-Tenant-Id")
        @NotBlank
        @Size(max = 64)
        String tenantId) {

    return tenantDataService.findForTenant(new TenantId(tenantId));
}
```

Bei Tenant-Kontext ist Validierung allein nicht genug. Zusätzlich ist Autorisierung nötig: Der aktuelle User muss auf diesen Tenant zugreifen dürfen.

---

# Teil C — Service-Layer-Validierung

## 11. Warum auch Services validieren?

Controller sind nicht der einzige Einstieg.

Ein Service kann aufgerufen werden von:

- REST Controller,
- Kafka Listener,
- Batch Job,
- Scheduler,
- CLI,
- Integrationstest,
- anderem Application Service,
- interner Facade.

Wenn nur der Controller validiert, kann ungültige Eingabe über andere Wege durchrutschen.

### 11.1 Gute Anwendung

```java
@Service
@Validated
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(@Valid CreateUserRequest request) {
        var email = EmailAddress.of(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        var user = User.create(
                request.firstName(),
                request.lastName(),
                email,
                request.age()
        );

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse findById(@NotNull UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

### 11.2 Wichtig zu `@Validated`

`@Validated` auf Klassenebene aktiviert in Spring Method Validation für diese Bean. Methodenaufrufe müssen über den Spring-Proxy laufen. Interne Self-Invocation kann Validierung umgehen, ähnlich wie bei `@Transactional`.

Schlecht:

```java
@Service
@Validated
public class UserService {

    public void outer(CreateUserRequest request) {
        inner(request); // interner Aufruf, Proxy kann umgangen werden
    }

    public void inner(@Valid CreateUserRequest request) {
        ...
    }
}
```

Besser:

- Validierung am öffentlichen Einstieg,
- fachliche Validierung zusätzlich im Domänenmodell,
- separate Bean, wenn Method Validation bewusst zwischen Methoden greifen soll.

---

# Teil D — Message Consumer und Events

## 12. Kafka-/RabbitMQ-Nachrichten validieren

### 12.1 Problem

Message-Queues werden oft als intern betrachtet. Das ist gefährlich. Nachrichten können:

- aus alter Version stammen,
- manuell erzeugt worden sein,
- falsches Schema haben,
- vom falschen Producer kommen,
- Tenant-Kontext verlieren,
- fachlich ungültige Werte enthalten.

### 12.2 Gute Anwendung

```java
public record UserCreatedEvent(

        @NotNull
        UUID eventId,

        @NotBlank
        @Size(max = 64)
        String tenantId,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotNull
        Instant occurredAt
) {}
```

```java
@Component
@Validated
public class UserEventConsumer {

    private final UserService userService;

    @KafkaListener(topics = "users.created")
    public void handleUserCreated(@Valid UserCreatedEvent event) {
        userService.handleUserCreated(event);
    }
}
```

### 12.3 Fehlerbehandlung bei ungültigen Messages

Ungültige Messages dürfen nicht endlos retried werden, wenn sie fachlich dauerhaft ungültig sind.

Regel:

```text
Technischer Fehler → Retry.
Fachlich ungültige Nachricht → Dead Letter Queue oder Quarantine mit Diagnose.
```

Beispiel:

```java
try {
    validator.validate(event);
    userService.handle(event);
} catch (ConstraintViolationException ex) {
    deadLetterPublisher.publishInvalidMessage(event, ex);
}
```

---

# Teil E — Allowlist statt Blocklist

## 13. Allowlist-Prinzip

OWASP empfiehlt für Input Validation bevorzugt eine positive Validierung: Erlaubte Werte, erlaubte Formate, erlaubte Längen und erlaubte Bereiche definieren.

### 13.1 Schlechte Anwendung: Blocklist

```java
if (description.contains("<script>")
        || description.contains("javascript:")
        || description.contains("onerror=")) {
    throw new ValidationException("Ungültige Eingabe");
}
```

Problem:

Blocklists sind unvollständig. Angreifer finden andere Schreibweisen, Encodings oder Umgehungen.

### 13.2 Gute Anwendung: Allowlist

```java
public record ProductDescriptionRequest(

        @NotBlank
        @Size(max = 500)
        @Pattern(
                regexp = "^[\\p{L}\\p{N} .,!?()'\"\\-:;/€%\\n\\r]+$",
                message = "Beschreibung enthält nicht erlaubte Zeichen"
        )
        String description
) {}
```

### 13.3 Vorsicht bei Freitext

Nicht jeder Freitext kann sinnvoll streng per Regex eingeschränkt werden. Produktbeschreibungen, Support-Nachrichten oder Kommentare brauchen oft mehr Zeichen.

Dann gilt:

- Länge begrenzen,
- HTML nicht direkt zulassen,
- Markdown nur mit sicherem Renderer,
- Output-Encoding beim Anzeigen,
- ggf. Sanitizer für HTML,
- keine Speicherung von aktiven Skripten,
- Content Security Policy prüfen,
- Audit und Moderationslogik bei öffentlichen Inhalten.

Input Validation ersetzt kein Output-Encoding.

---

# Teil F — SQL-Injection verhindern

## 14. Problem: String-Konkatenation in Queries

Schlecht:

```java
@Repository
public class UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public List<UserEntity> findByName(String name) {
        return entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.name = '" + name + "'",
                UserEntity.class
        ).getResultList();
    }
}
```

Angreifereingabe:

```text
' OR '1'='1
```

Die Query-Logik wird verändert.

### 14.1 Gute Anwendung: Spring Data Repository Method

```java
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByLastNameAndActiveTrue(String lastName);
}
```

### 14.2 Gute Anwendung: JPQL mit Named Parameters

```java
@Query("""
       SELECT u
       FROM UserEntity u
       WHERE u.lastName = :lastName
         AND u.active = true
       """)
List<UserEntity> findActiveByLastName(@Param("lastName") String lastName);
```

### 14.3 Gute Anwendung: Criteria API für dynamische Queries

```java
public List<UserEntity> search(UserSearchCriteria criteria) {
    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(UserEntity.class);
    var root = query.from(UserEntity.class);

    var predicates = new ArrayList<Predicate>();

    if (criteria.lastName() != null) {
        predicates.add(cb.equal(root.get("lastName"), criteria.lastName()));
    }

    if (criteria.active() != null) {
        predicates.add(cb.equal(root.get("active"), criteria.active()));
    }

    query.where(predicates.toArray(Predicate[]::new));

    return entityManager.createQuery(query).getResultList();
}
```

### 14.4 Sort-Felder nicht frei übernehmen

Schlecht:

```java
String jpql = "SELECT u FROM UserEntity u ORDER BY " + sort;
```

Besser:

```java
private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
        "name", "lastName",
        "createdAt", "createdAt",
        "email", "email"
);

public Sort sortBy(String requestedField, Sort.Direction direction) {
    var property = ALLOWED_SORT_FIELDS.get(requestedField);

    if (property == null) {
        throw new InvalidSortFieldException(requestedField);
    }

    return Sort.by(direction, property);
}
```

---

# Teil G — Path Traversal und Dateieingaben

## 15. Dateinamen validieren

Schlecht:

```java
@GetMapping("/files/{fileName}")
public ResponseEntity<Resource> download(@PathVariable String fileName) {
    var path = Path.of("/data/uploads/" + fileName);
    ...
}
```

Angriff:

```text
../../../../etc/passwd
```

Besser:

```java
private static final Pattern SAFE_FILE_NAME =
        Pattern.compile("^[a-zA-Z0-9._-]{1,120}$");

public Path resolveSafeUploadPath(String fileName) {
    if (!SAFE_FILE_NAME.matcher(fileName).matches()) {
        throw new InvalidFileNameException(fileName);
    }

    var basePath = Path.of("/data/uploads").toAbsolutePath().normalize();
    var resolved = basePath.resolve(fileName).normalize();

    if (!resolved.startsWith(basePath)) {
        throw new PathTraversalAttemptException(fileName);
    }

    return resolved;
}
```

Regeln:

- Dateiname allowlisten,
- Pfad normalisieren,
- prüfen, dass Ziel im erlaubten Basisverzeichnis liegt,
- keine absoluten Pfade vom Client übernehmen,
- MIME-Type nicht blind vertrauen,
- Dateigröße begrenzen,
- Uploads außerhalb des Webroots speichern.

---

# Teil H — Fachliche Validierung

## 16. Bean Validation vs. Fachlogik

Bean Validation ist gut für technische und einfache fachliche Regeln:

- Pflichtfeld,
- Länge,
- Format,
- Wertebereich,
- Collection-Größe,
- Datumsbeziehung.

Komplexe Fachregeln gehören oft in Domain-Objekte oder Policies.

### 16.1 Beispiel: Bean Validation

```java
public record CreateOfferRequest(

        @NotBlank
        @Size(max = 120)
        String title,

        @NotNull
        @FutureOrPresent
        LocalDate validFrom,

        @NotNull
        LocalDate validTo,

        @NotNull
        @Positive
        BigDecimal discountedPrice
) {}
```

### 16.2 Beispiel: Domain-Regel

```java
public class Offer {

    public void publish() {
        if (!merchant.isVerified()) {
            throw new OfferPublicationException("merchant is not verified");
        }

        if (discountedPrice.isGreaterThanOrEqualTo(originalPrice)) {
            throw new OfferPublicationException("discounted price must be lower than original price");
        }

        this.status = OfferStatus.PUBLISHED;
    }
}
```

Nicht jede Regel gehört als Annotation an ein DTO. Viele Regeln brauchen Domänenwissen.

---

## 17. Custom Validator

### 17.1 Wann sinnvoll?

Custom Validatoren sind sinnvoll, wenn:

- mehrere Felder gemeinsam geprüft werden,
- Regel an mehreren DTOs wiederverwendet wird,
- Regel deklarativ sichtbar sein soll,
- Validierungsfehler sauber in Standardformat passen soll.

### 17.2 Beispiel: Date Range

Annotation:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {

    String message() default "Enddatum muss nach Startdatum liegen";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

DTO:

```java
@ValidDateRange
public record DateRangeRequest(

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {}
```

Validator:

```java
public class ValidDateRangeValidator
        implements ConstraintValidator<ValidDateRange, DateRangeRequest> {

    @Override
    public boolean isValid(
            DateRangeRequest request,
            ConstraintValidatorContext context) {

        if (request == null) {
            return true;
        }

        if (request.startDate() == null || request.endDate() == null) {
            return true;
        }

        return !request.endDate().isBefore(request.startDate());
    }
}
```

Wichtig: `null`-Prüfungen bleiben bei `@NotNull`. Der Custom Validator prüft nur die Beziehung.

### 17.3 Bessere Fehlermeldung auf konkretem Feld

```java
public class ValidDateRangeValidator
        implements ConstraintValidator<ValidDateRange, DateRangeRequest> {

    @Override
    public boolean isValid(DateRangeRequest request, ConstraintValidatorContext context) {
        if (request == null || request.startDate() == null || request.endDate() == null) {
            return true;
        }

        if (!request.endDate().isBefore(request.startDate())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("endDate muss nach startDate liegen")
                .addPropertyNode("endDate")
                .addConstraintViolation();

        return false;
    }
}
```

---

# Teil I — Fehlerantworten

## 18. Zentraler GlobalExceptionHandler

Validation Errors müssen einheitlich und für Clients verständlich sein.

```java
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBodyValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));

        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.requireNonNullElse(error.getDefaultMessage(), "invalid"),
                        "rejectedValue", safeRejectedValue(error.getRejectedValue())
                ))
                .toList();

        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Constraint violation");
        problem.setInstance(URI.create(request.getRequestURI()));

        var errors = ex.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "path", violation.getPropertyPath().toString(),
                        "message", violation.getMessage()
                ))
                .toList();

        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    private Object safeRejectedValue(Object value) {
        if (value == null) {
            return null;
        }

        var text = value.toString();

        if (text.length() > 100) {
            return text.substring(0, 100) + "...";
        }

        return text;
    }
}
```

### 18.1 Keine sensitiven Werte in Fehlerantworten

Nicht zurückgeben:

- Passwörter,
- Tokens,
- API Keys,
- Kreditkartennummern,
- IBAN,
- interne SQL-Fehler,
- Stacktraces,
- vollständige Payloads.

Fehlermeldungen sollen hilfreich, aber nicht informationsgefährdend sein.

---

# Teil J — Normalisierung

## 19. Normalisierung vor fachlicher Verwendung

Normalisierung bedeutet: Eingabe wird in ein einheitliches Format gebracht.

Beispiele:

```java
public record EmailAddress(String value) {

    public EmailAddress {
        Objects.requireNonNull(value);
        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.length() > 255) {
            throw new InvalidEmailException(value);
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException(value);
        }
    }
}
```

Regeln:

- Trimmen bei Feldern, wo Leerzeichen keine Bedeutung haben.
- Lowercase bei E-Mails oder Codes, wenn fachlich korrekt.
- Unicode-Normalisierung prüfen, wenn Sicherheitsrelevant.
- Telefonnummern in ein kanonisches Format überführen.
- Währungscodes als `Currency` validieren.
- Freitext nicht blind verändern, wenn Zeichen bedeutungstragend sind.

---

# Teil K — Grenzwerte und DoS-Schutz

## 20. Warum Längen und Größen wichtig sind

Fehlende Größenbegrenzung kann zu Denial of Service führen. Ein Request mit sehr großen Strings, Listen oder Page Sizes kann Speicher, CPU oder Datenbank belasten.

### 20.1 DTO-Grenzen

```java
public record SearchRequest(

        @NotBlank
        @Size(min = 2, max = 100)
        String query,

        @Min(0)
        int page,

        @Min(1)
        @Max(100)
        int size,

        @Size(max = 20)
        List<@Size(max = 50) String> filters
) {}
```

### 20.2 Upload-Grenzen

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 12MB
```

### 20.3 Pagination immer begrenzen

Schlecht:

```java
@GetMapping("/orders")
public List<OrderResponse> findAll(@RequestParam int size) {
    return orderService.findAll(size);
}
```

Besser:

```java
int boundedSize = Math.min(Math.max(size, 1), 100);
```

Oder direkt per Constraint:

```java
@RequestParam(defaultValue = "20")
@Min(1)
@Max(100)
int size
```

---

# Teil L — Tests

## 21. Controller-Validierung testen

```java
@WebMvcTest(UserController.class)
class UserControllerValidationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    void createUser_returns400_whenEmailIsInvalid() throws Exception {
        var request = """
                {
                  "firstName": "Max",
                  "lastName": "Mustermann",
                  "email": "not-an-email",
                  "age": 30
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createUser_returns400_whenFirstNameIsMissing() throws Exception {
        var request = """
                {
                  "lastName": "Mustermann",
                  "email": "max@example.com",
                  "age": 30
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
```

### 21.1 Test deckt vergessenes `@Valid` auf

Wenn `@Valid` im Controller fehlt, werden diese Tests unerwartet erfolgreich mit 201/200 statt 400. Genau deshalb sind Controller-Slice-Tests Pflicht.

---

## 22. Custom Validator testen

```java
class ValidDateRangeValidatorTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void validates_whenEndDateIsAfterStartDate() {
        var request = new DateRangeRequest(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejects_whenEndDateIsBeforeStartDate() {
        var request = new DateRangeRequest(
                LocalDate.of(2025, 1, 31),
                LocalDate.of(2025, 1, 1)
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Enddatum muss nach Startdatum liegen");
    }
}
```

---

## 23. Repository-Sicherheit testen

### 23.1 SQL-Injection-Test

```java
@DataJpaTest
class UserRepositoryInjectionTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void findByLastName_doesNotReturnAllUsers_forSqlInjectionPayload() {
        userRepository.save(new UserEntity("Max", "Mustermann", "max@example.com"));
        userRepository.save(new UserEntity("Anna", "Meyer", "anna@example.com"));

        var result = userRepository.findActiveByLastName("' OR '1'='1");

        assertThat(result).isEmpty();
    }
}
```

Dieser Test ersetzt keine statische Analyse, zeigt aber, dass die Query parametrisiert genutzt wird.

---

## 24. Service-Validation testen

```java
@SpringBootTest
class UserServiceValidationTest {

    @Autowired
    UserService userService;

    @Test
    void createUser_throwsConstraintViolation_whenRequestIsInvalid() {
        var invalidRequest = new CreateUserRequest(
                "",
                "Mustermann",
                "not-an-email",
                -1
        );

        assertThatThrownBy(() -> userService.createUser(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
```

Wichtig: Der Service muss über Spring injiziert werden, damit Method Validation über Proxy greifen kann.

---

# Teil M — Typische Fehler

## 25. Fehler 1: `@Valid` vergessen

Schlecht:

```java
@PostMapping("/users")
public UserResponse createUser(@RequestBody CreateUserRequest request) {
    return userService.createUser(request);
}
```

Besser:

```java
@PostMapping("/users")
public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userService.createUser(request);
}
```

## 26. Fehler 2: Nur Controller validieren

Schlecht:

```java
@KafkaListener(topics = "users")
public void handle(CreateUserRequest request) {
    userService.createUser(request);
}
```

Besser:

```java
@KafkaListener(topics = "users")
public void handle(@Valid CreateUserRequest request) {
    userService.createUser(request);
}
```

Zusätzlich Service mit `@Validated`.

## 27. Fehler 3: Blocklist statt Allowlist

Schlecht:

```java
if (input.contains("<script>")) {
    throw new ValidationException("invalid");
}
```

Besser:

```java
@Size(max = 500)
@Pattern(regexp = "^[\\p{L}\\p{N} .,!?-]+$")
String description
```

Oder bei echtem Freitext: Länge begrenzen und Ausgabe korrekt encoden.

## 28. Fehler 4: Entity als Request Body

Schlecht:

```java
@PostMapping("/users")
public UserEntity createUser(@RequestBody UserEntity entity) {
    return userRepository.save(entity);
}
```

Besser:

```java
@PostMapping("/users")
public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userService.createUser(request);
}
```

## 29. Fehler 5: Dynamische Query per String

Schlecht:

```java
"SELECT u FROM UserEntity u WHERE u.email = '" + email + "'"
```

Besser:

```java
@Query("SELECT u FROM UserEntity u WHERE u.email = :email")
Optional<UserEntity> findByEmail(@Param("email") String email);
```

## 30. Fehler 6: Freie Sort-Felder

Schlecht:

```java
Sort.by(request.sortBy())
```

wenn `sortBy` ungeprüft vom Client kommt.

Besser:

```java
var property = allowedSortFields.get(request.sortBy());
if (property == null) {
    throw new InvalidSortFieldException(request.sortBy());
}
return Sort.by(direction, property);
```

## 31. Fehler 7: Keine maximale Länge

Schlecht:

```java
public record CommentRequest(String text) {}
```

Besser:

```java
public record CommentRequest(
        @NotBlank
        @Size(max = 2000)
        String text
) {}
```

## 32. Fehler 8: Tenant aus Request blind übernehmen

Schlecht:

```java
public List<OrderResponse> orders(@RequestHeader("X-Tenant-Id") String tenantId) {
    return orderService.findOrders(new TenantId(tenantId));
}
```

Besser:

```java
public List<OrderResponse> orders(Authentication authentication) {
    var accessScope = accessScopeResolver.from(authentication);
    return orderService.findOrders(accessScope);
}
```

Tenant-Auswahl ist Autorisierung, nicht nur Validierung.

---

# Teil N — Review-Checkliste

## 33. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Request DTO | Wird ein eigenes DTO genutzt? | `CreateUserRequest` | Pflicht |
| Keine Entity | Keine JPA-Entity als Request-Body | kein `@RequestBody UserEntity` | Pflicht |
| `@Valid` | Request Body validiert? | `@Valid @RequestBody` | Pflicht |
| Controller-Parameter | Query/Path/Header validiert? | `@Min`, `@Size` | Pflicht |
| `@Validated` | Method Validation nötig? | Service, Controller Parameter | Prüfen |
| Pflichtfelder | `@NotNull`, `@NotBlank` gesetzt? | Name, E-Mail | Pflicht |
| Längen | Maximalwerte gesetzt? | max 255 | Pflicht |
| Wertebereich | Min/Max gesetzt? | `size <= 100` | Pflicht |
| Format | Regex/Email/Enum genutzt? | `@Email` | Pflicht |
| Allowlist | Blocklist vermieden? | erlaubte Zeichen | Pflicht |
| Mass Assignment | Nur erlaubte Felder im DTO? | kein `role` | Pflicht |
| SQL-Injection | Keine Query-Konkatenation? | named params | Pflicht |
| Sortierung | Sort-Felder allowlisted? | `allowedSortFields` | Pflicht |
| Datei | Pfade normalisiert? | `startsWith(basePath)` | Pflicht bei Dateien |
| Messages | Kafka/Rabbit Payload validiert? | `@Valid` | Pflicht |
| Fehlerantwort | Zentral und sicher? | ProblemDetail | Pflicht |
| Sensitive Werte | Keine Secrets in Fehlern? | kein Token | Pflicht |
| Tests | Ungültige Eingaben getestet? | 400 Tests | Pflicht |
| Tenant | Tenant nicht blind übernommen? | AccessScope | Pflicht bei SaaS |

---

# Teil O — Automatisierbare Prüfungen

## 34. Mögliche Regeln

```text
- Keine @RequestBody Entity-Klassen.
- Jeder @RequestBody-Parameter muss @Valid tragen.
- Controller mit validierten @RequestParam/@PathVariable nutzen @Validated.
- Keine createQuery-Aufrufe mit String-Konkatenation.
- Keine nativen Queries aus ungeprüften Strings.
- Keine Controller-Methoden mit Map<String, Object> als Request Body ohne begründete Ausnahme.
- Keine Page Size ohne @Max.
- Keine Dateioperationen mit ungeprüften Path-Variablen.
- Keine DTOs ohne @Size auf freien Strings.
```

### 34.1 ArchUnit-Beispiel: keine Entity als RequestBody

```java
@ArchTest
static final ArchRule controllers_should_not_accept_entities_as_request_body =
        noMethods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
                .should().haveRawParameterTypes(UserEntity.class)
                .because("JPA-Entities dürfen nicht direkt als Request-Body verwendet werden.");
```

Diese konkrete Regel muss im Projekt generischer über Package-Konventionen gebaut werden, zum Beispiel `..entity..`.

### 34.2 Semgrep-/Search-Regeln

Suchen nach gefährlichen Mustern:

```text
createQuery(".*" + ...)
@Query(nativeQuery = true) mit String-Aufbau
@RequestBody .*Entity
@RequestBody Map<String, Object>
@RequestParam int size ohne @Max
```

---

# Teil P — Migration bestehender Endpoints

## 35. Schrittweise Migration

1. Alle Controller-Methoden mit `@RequestBody` inventarisieren.
2. Prüfen, ob Entities direkt verwendet werden.
3. Pro Endpoint eigenes Request-DTO einführen.
4. Pflichtfelder und Längen ergänzen.
5. Query-/Path-Parameter validieren.
6. Controller mit `@Validated` versehen, wenn Parameterconstraints genutzt werden.
7. Service-Methoden mit `@Validated` absichern, wenn mehrere Einstiegspunkte existieren.
8. Manuelle Validierung durch Bean Validation ersetzen, wo sinnvoll.
9. Komplexe Regeln in Custom Validator oder Domain verschieben.
10. Repository-Queries auf Parameterisierung prüfen.
11. Freie Sort-/Filter-Felder allowlisten.
12. Fehlerantworten vereinheitlichen.
13. Tests für ungültige Eingaben ergänzen.
14. Security Review für Mass Assignment, Tenant und Dateioperationen durchführen.

---

# Teil Q — Ausnahmen

## 36. Zulässige Ausnahmen

Ausnahmen sind möglich, aber müssen im Review begründet werden.

| Ausnahme | Bedingung |
|---|---|
| `Map<String, Object>` als Request | nur für bewusst dynamische Payloads, mit Schema-/Whitelist-Prüfung |
| Keine `@Pattern` auf Freitext | zulässig, wenn Länge begrenzt und Output-Encoding garantiert ist |
| Service ohne `@Validated` | zulässig, wenn nur validierte Controller-Aufrufe existieren und keine weiteren Einstiege |
| Native Query | zulässig, wenn parametrisierte Query und keine String-Konkatenation |
| Entity-naher Import | nur intern, mit expliziter Mapping-/Validierungsschicht |
| Sehr flexible Filter | nur mit allowlisted Feldern und typisierten Operatoren |

Nicht zulässig:

- Entity direkt als öffentlicher Request-Body,
- SQL-/JPQL-Konkatenation mit Userinput,
- unlimitierte Listen/Page Sizes,
- freie Datei-Pfade aus Requests,
- Tenant-Kontext ohne Autorisierung,
- Validierung nur als Kommentar.

---

# Teil R — Definition of Done

## 37. Definition of Done

Ein Endpoint, Consumer oder Import erfüllt diese Richtlinie, wenn:

1. alle externen Eingaben identifiziert sind,
2. eigene Request-/Message-DTOs verwendet werden,
3. keine JPA-Entity direkt als Request-Body akzeptiert wird,
4. Pflichtfelder validiert sind,
5. String-Längen begrenzt sind,
6. Zahlenbereiche begrenzt sind,
7. Collections begrenzt sind,
8. Formate validiert sind,
9. verschachtelte DTOs mit `@Valid` validiert werden,
10. Query-/Path-/Header-Parameter Constraints besitzen,
11. Method Validation dort aktiv ist, wo Services mehrere Einstiegspunkte haben,
12. Datenbankzugriffe parametrisiert sind,
13. Sort-/Filter-Felder allowlisted sind,
14. Dateien und Pfade normalisiert und begrenzt sind,
15. Tenant-Kontext nicht blind aus Eingaben übernommen wird,
16. Fehlerantworten zentral und sicher sind,
17. Tests ungültige Eingaben prüfen,
18. Custom Validatoren Unit-Tests haben,
19. Security-relevante Randfälle geprüft sind,
20. keine Validierungsregel nur manuell und inkonsistent in einzelnen Endpoints existiert.

---

## 38. Quellen und weiterführende Literatur

- OWASP Cheat Sheet Series — Input Validation Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
- OWASP Cheat Sheet Series — SQL Injection Prevention Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html
- OWASP Cheat Sheet Series — Query Parameterization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Query_Parameterization_Cheat_Sheet.html
- OWASP Cheat Sheet Series — REST Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html
- Spring Framework Reference — Validation in Spring MVC: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html
- Spring Framework Javadoc — `@Validated`: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/validation/annotation/Validated.html
- Jakarta Bean Validation Specification: https://jakarta.ee/specifications/bean-validation/
- Jakarta Bean Validation Tutorial — Built-in Constraints: https://jakarta.ee/learn/docs/jakartaee-tutorial/current/beanvalidation/bean-validation/bean-validation.html
- Hibernate Validator Reference Guide: https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/
- Spring Data JPA Reference — Query Methods and `@Query`: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
