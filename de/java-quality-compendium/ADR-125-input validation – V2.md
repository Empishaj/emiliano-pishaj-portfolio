
## Kontext

Jede Eingabe die von außen kommt – HTTP-Request-Body, Query-Parameter,
Path-Variablen, Nachrichten aus Message-Queues – ist potenziell bösartig.
Fehlende oder unvollständige Validierung ist die Grundlage für
SQL-Injection, XSS, Path-Traversal und Business-Logic-Bypässe.

In Microservices ist das besonders relevant: Auch interne Services
können kompromittiert werden. **Vertraue keiner Eingabe, egal woher.**

---

## Das Problem in der Praxis

### ❌ So sieht es schlecht aus

```java
// ❌ Keine Validierung – alles wird akzeptiert
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @RequestBody CreateUserRequest request) {

    // Was wenn firstName null ist? NullPointerException.
    // Was wenn email "not-an-email" ist? Landet in der DB.
    // Was wenn age = -999 ist? Kein Problem scheinbar.
    return ResponseEntity.ok(userService.createUser(request));
}
```

```java
// ❌ Manuelle Validierung – unvollständig und inkonsistent
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @RequestBody CreateUserRequest request) {

    if (request.firstName() == null || request.firstName().isEmpty()) {
        throw new RuntimeException("firstName required");
    }
    // email wird vergessen zu prüfen
    // age-Grenzen werden vergessen
    // Jeder Endpoint macht es anders
    return ResponseEntity.ok(userService.createUser(request));
}
```

```java
// ❌ SQL-Injection durch String-Konkatenation
@Repository
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    public List<User> findByName(String name) {
        // 💀 Klassische SQL-Injection
        return em.createQuery(
            "SELECT u FROM User u WHERE u.name = '" + name + "'",
            User.class
        ).getResultList();
        // Input: ' OR '1'='1  → gibt alle User zurück
        // Input: '; DROP TABLE users; --  → Katastrophe
    }
}
```

**Was hier schiefläuft:**

- Kein `@Valid` → Bean Validation Annotations werden ignoriert
- Manuelle Validierung ist fehleranfällig und inkonsistent
- String-Konkatenation in Queries → SQL-Injection
- Keine Längenbegrenzung → Speicherüberlauf, DoS möglich

---

## Die Lösung

### ✅ So macht man es richtig

**Schritt 1: Bean Validation konsequent nutzen**

```java
// ✅ Request-Record mit vollständiger Validierung
public record CreateUserRequest(

    @NotBlank(message = "Vorname ist Pflicht")
    @Size(min = 2, max = 50, message = "Vorname: 2-50 Zeichen")
    @Pattern(regexp = "^[\\p{L} '-]+$",
             message = "Vorname enthält ungültige Zeichen")
    String firstName,

    @NotBlank(message = "Nachname ist Pflicht")
    @Size(min = 2, max = 50, message = "Nachname: 2-50 Zeichen")
    String lastName,

    @NotBlank(message = "E-Mail ist Pflicht")
    @Email(message = "Keine gültige E-Mail-Adresse")
    @Size(max = 255)
    String email,

    @NotNull(message = "Alter ist Pflicht")
    @Min(value = 18, message = "Mindestalter: 18 Jahre")
    @Max(value = 120, message = "Ungültiges Alter")
    Integer age
) {}
```

```java
// ✅ @Valid aktiviert die Validierung – ohne das passiert nichts!
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userService.createUser(request));
}

// ✅ Auch Query-Parameter validieren
@GetMapping("/users")
public List<UserResponse> searchUsers(
        @RequestParam
        @NotBlank
        @Size(min = 2, max = 100)
        String query,

        @RequestParam(defaultValue = "0")
        @Min(0) int page,

        @RequestParam(defaultValue = "20")
        @Min(1) @Max(100) int size) {
    return userService.search(query, page, size);
}
```

**Schritt 2: @Validated für Service-Layer**

```java
// ✅ Validierung auch im Service – Defense in Depth
@Service
@Validated  // Aktiviert Validierung auf Methoden-Ebene
@RequiredArgsConstructor
public class UserService {

    public UserResponse createUser(@Valid CreateUserRequest request) {
        // Auch wenn der Controller @Valid vergisst:
        // hier wird nochmal validiert
    }

    // Parameter direkt validieren
    public UserResponse getUser(@NotNull UUID id) {
        return userRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
```

**Schritt 3: Sichere Datenbankzugriffe**

```java
// ✅ Spring Data JPA – niemals String-Konkatenation
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Automatisch parametrisiert – kein SQL-Injection möglich
    Optional<UserEntity> findByEmail(String email);

    // JPQL mit Named Parameters
    @Query("SELECT u FROM UserEntity u WHERE u.lastName = :lastName " +
           "AND u.active = true")
    List<UserEntity> findActiveByLastName(@Param("lastName") String lastName);
}
```

**Schritt 4: Custom Validator für komplexe Regeln**

```java
// Eigene Annotation
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "Enddatum muss nach Startdatum liegen";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator-Implementierung
public class ValidDateRangeValidator
        implements ConstraintValidator<ValidDateRange, DateRangeRequest> {

    @Override
    public boolean isValid(DateRangeRequest request,
                           ConstraintValidatorContext context) {
        if (request.startDate() == null || request.endDate() == null) {
            return true; // @NotNull kümmert sich darum
        }
        return request.endDate().isAfter(request.startDate());
    }
}

// Verwendung
@ValidDateRange
public record DateRangeRequest(
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
```

---

## Tipps

**Pitfall #1 – `@Valid` vergessen:**
Bean Validation Annotations auf dem Record/DTO sind wirkungslos ohne
`@Valid` am Controller-Parameter. Es gibt keinen Compiler-Fehler,
keinen Runtime-Fehler beim Start – die Validierung wird einfach still
ignoriert. Integration-Tests die den vollen Request-Stack testen
decken das auf.

**Pitfall #2 – Nur den Controller validieren:**
```java
// Service wird auch direkt aufgerufen (z.B. aus Scheduler, Message Consumer)
// Ohne @Validated am Service kommt ungültige Eingabe durch
@KafkaListener(topics = "user-events")
public void handleUserEvent(CreateUserRequest request) {
    userService.createUser(request);  // Kein @Valid hier → kein Schutz
}
```
Lösung: `@Validated` + `@Valid` auf Service-Methoden.

**Pitfall #3 – Allowlist statt Blocklist:**
```java
// ❌ Blocklist – man vergisst immer etwas
if (input.contains("<script>") || input.contains("javascript:")) {
    throw new ValidationException("Ungültige Eingabe");
}

// ✅ Allowlist – nur explizit Erlaubtes kommt durch
@Pattern(regexp = "^[a-zA-Z0-9äöüÄÖÜß .,!?-]{1,500}$")
String description
```

**Pitfall #4 – Mass Assignment:**
```java
// ❌ Entity direkt als Request-Body – Angreifer kann alle Felder setzen
@PostMapping("/users")
public User createUser(@RequestBody UserEntity user) {
    // Angreifer setzt user.role = "ADMIN", user.active = true, etc.
    return userRepository.save(user);
}

// ✅ Immer separates DTO – nur explizit erlaubte Felder
@PostMapping("/users")
public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    // Nur firstName, lastName, email, age kommen rein
}
```

**War Story:** Ein E-Commerce-Service hatte keine Validierung auf
dem `quantity`-Parameter beim Warenkorb. Ein Nutzer setzte
`quantity = -1` und bekam Geld zurück statt zu bezahlen.
Der Fehler war im Code in 2 Minuten gefunden – aber der Schaden
war bereits entstanden.

---

## Entscheidung

**Bean Validation** (`@Valid`, `@Validated`) auf allen Eingaben,
auf Controller- und Service-Layer. Allowlists statt Blocklists.
Spring Data JPA für alle Datenbankzugriffe – keine String-Konkatenation.

### Konsequenzen

-  Systematischer Schutz gegen Injection-Angriffe
- ✅ Einheitliche Fehlermeldungen durch GlobalExceptionHandler
- ✅ Validierungslogik deklarativ und lesbar
- ✅ Defense in Depth durch mehrschichtige Validierung
- ⚠️ Custom Validators brauchen eigene Tests
- ⚠️ Regex-Patterns für internationale Zeichen sorgfältig testen

---

## Checkliste für den Alltag

- [ ] Alle `@RequestBody`-Parameter haben `@Valid`
- [ ] Alle `@RequestParam`/`@PathVariable` haben Constraints
- [ ] Service-Klassen haben `@Validated` wo nötig
- [ ] Keine String-Konkatenation in Datenbankabfragen
- [ ] Keine Entity-Klassen direkt als Request-Body
- [ ] Custom Validators haben Unit-Tests
- [ ] Maximale Feldlängen überall definiert (DoS-Schutz)