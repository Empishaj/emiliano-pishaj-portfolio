# ADR-015 — Sicherheit: OWASP Top 10, Input-Validierung & Spring Security

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x · Spring Security 6.x        |
| Datum      | 2025-12-26                                        |
| Kategorie  | Security                                          |

---

## Kontext & Problem

Sicherheit ist kein Feature das am Ende hinzugefügt wird — sie ist eine Eigenschaft die von Anfang an im Design steckt. Die OWASP Top 10 beschreiben die häufigsten und kritischsten Sicherheitslücken in Webanwendungen. Dieses ADR definiert verbindliche Regeln gegen die Top-Angriffsvektoren in Java/Spring-Anwendungen.

---

## Regel 1 — Input-Validierung: Niemals dem Client vertrauen

### Schlecht — unkontrollierte Eingaben direkt verarbeitet

```java
@PostMapping("/search")
public List<Product> search(@RequestParam String query,
                            @RequestParam int page) {
    // query direkt in SQL — SQL Injection!
    String sql = "SELECT * FROM products WHERE name LIKE '%" + query + "%' LIMIT " + page;
    return jdbcTemplate.query(sql, productMapper);
}

@PostMapping("/users")
public void createUser(@RequestBody Map<String, Object> body) {
    // Kein Schema, kein Limit, keine Typprüfung
    String name  = (String) body.get("name");
    String role  = (String) body.get("role");  // User kann sich selbst ADMIN geben!
    userService.create(name, role);
}
```

### Gut — Validierung auf allen Ebenen

```java
// ① Request-DTO mit Bean Validation — Compiler + Runtime-Schutz
public record CreateUserRequest(
    @NotBlank @Size(min = 2, max = 100)                      String name,
    @NotBlank @Email                                          String email,
    @NotBlank @Size(min = 8, max = 72) @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9]).+$",
        message = "Passwort braucht Großbuchstaben und Ziffer") String password
    // KEIN role-Feld — Rolle wird serverseitig gesetzt, nie vom Client
) {}

// ② Controller: @Valid aktiviert Bean Validation
@PostMapping("/users")
@ResponseStatus(CREATED)
public UserCreatedResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userService.register(request);
}

// ③ Datenbankzugriff: immer Prepared Statements / Paramter-Binding
@Query("SELECT p FROM Product p WHERE p.name LIKE :query")
List<Product> searchByName(@Param("query") String query); // Parameter gebunden, nie konkateniert

// ④ Globaler Validation-Fehler-Handler
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var detail = ProblemDetail.forStatus(BAD_REQUEST);
        detail.setTitle("Validation Failed");
        detail.setProperty("errors", ex.getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
            .toList());
        return detail;
    }
}
```

---

## Regel 2 — Secrets: Niemals im Code, niemals im Log

### Schlecht — Secrets hardcodiert oder geloggt

```java
// Hardcodierter Secret — landet in Git!
@Value("${jwt.secret:mein-super-geheimer-schluessel-123}")
private String jwtSecret;

// API-Key im Code
private static final String API_KEY = "sk-prod-abc123xyz789";

// Secret im Log
log.info("Connecting to DB with password: {}", dbPassword);
log.debug("JWT Token: {}", token); // Token im Log = Session-Hijacking möglich
```

### Gut — Secrets aus Umgebung, nie geloggt

```java
// application.yml: nur Referenz auf Umgebungsvariable
// jwt:
//   secret: ${JWT_SECRET}   ← aus Umgebungsvariable oder Vault

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    @NotBlank String secret,
    Duration expiration
) {}

// Logging: sensitive Felder maskieren
@JsonIgnore  // Nie in JSON-Serialisierung
private final String password;

// Custom ToString für Entities mit sensitiven Daten
@Override
public String toString() {
    return "User[id=%d, email=%s]".formatted(id, email);
    // password wird NICHT geloggt
}

// MDC für Request-Kontext — niemals User-Credentials
MDC.put("userId", userId.toString());     // OK
MDC.put("requestId", requestId);          // OK
// MDC.put("password", password);         // NIEMALS
```

---

## Regel 3 — Spring Security: Defense in Depth

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Aktiviert @PreAuthorize auf Methoden-Ebene
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // CSRF: aktiviert für Browser-Clients, deaktiviert für stateless APIs
            .csrf(csrf -> csrf.disable()) // Nur für reine REST-APIs mit JWT
            // Security Headers
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)         // Clickjacking
                .xssProtection(XssProtectionConfig::disable)                       // CSP übernimmt das
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            )
            // Session: stateless für APIs
            .sessionManagement(session ->
                session.sessionCreationPolicy(STATELESS))
            // Autorisierung: explizit, kein "permit all" als Default
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()  // Alles andere: Authentifizierung Pflicht
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}

// Methoden-Level-Autorisierung: feinkörnige Kontrolle
@Service
public class OrderService {

    // Nur der Order-Eigentümer oder Admin darf stornieren
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOwner(#orderId, authentication)")
    public void cancelOrder(Long orderId) { ... }

    // Rückgabewert filtern: User sieht nur eigene Daten
    @PostFilter("filterObject.userId == authentication.principal.id or hasRole('ADMIN')")
    public List<Order> findAll() { ... }
}
```

---

## Regel 4 — Passwörter: BCrypt, kein MD5/SHA

```java
// ❌ NIEMALS: MD5, SHA-1, SHA-256 für Passwörter, Base64, plain text
String hashed = DigestUtils.md5Hex(password);    // Kryptographisch gebrochen!
String encoded = Base64.encode(password);         // Das ist KEIN Hashing!

// ✅ BCrypt mit Kostenfaktor — langsam by design (verhindert Brute-Force)
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Kostenfaktor 12: ~250ms pro Hash
}

// Verwendung
String hashed = passwordEncoder.encode(rawPassword);
boolean matches = passwordEncoder.matches(rawPassword, storedHash);
```

---

## Konsequenzen

**Positiv:** SQL-Injection durch konsequentes Parameter-Binding unmöglich. Secrets in Git-History sind der häufigste Angriff — Umgebungsvariablen verhindern das. `@EnableMethodSecurity` ermöglicht granulare Autorisierung ohne Controller-Logik.

**Negativ:** `@Valid` auf jedem Controller-Parameter ist Pflicht — wird es vergessen, ist die Validierung wirkungslos. Regelmäßige Dependency-Updates nötig (CVE-Monitoring).

---

## Tipps

- **OWASP Dependency-Check** im Build einbinden: `plugins { id 'org.owasp.dependencycheck' }` — CVEs in Abhängigkeiten werden automatisch gefunden.
- **Rate Limiting**: Spring Boot + Bucket4j für API-Rate-Limiting gegen Brute-Force.
- **Security-Tests schreiben**: `@WithMockUser`, `@WithAnonymousUser` in Tests — Sicherheitsregeln sind testbar.
- **Kein `@Secured("ROLE_ADMIN")` verwenden** — `@PreAuthorize` mit SpEL ist mächtiger und konsistenter.