# QG-JAVA-015 — Sicherheit: OWASP Top 10, Input-Validierung und Spring Security

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-015 |
| Titel | Sicherheit: OWASP Top 10, Input-Validierung und Spring Security |
| Status | Accepted / verbindlicher Mindeststandard für sichere Java-/Spring-Boot-Anwendungen |
| Sprache | Deutsch |
| Kategorie | Security / Secure Coding / Spring Security / OWASP |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Security 6.x, Jakarta Bean Validation 3.x |
| Zielgruppe | Java-Entwickler, Tech Leads, Security Engineers, Reviewer, QA Engineers, Product Engineers, Plattformteams |
| Gültigkeit | Gilt für REST APIs, Webanwendungen, Backend-Services, SaaS-Plattformen, interne APIs, Batch-Schnittstellen mit externen Eingaben und Integrationsadapter |
| Nicht-Ziel | Diese Richtlinie ersetzt keine Bedrohungsmodellierung, kein Penetration Testing, keine Datenschutz-Folgenabschätzung, kein vollständiges IAM-Konzept und keine organisationsweite Security Policy |
| Verbindlichkeit | Neue Java-/Spring-Boot-Funktionalität MUSS diese Richtlinie erfüllen. Abweichungen MÜSSEN im Pull Request begründet, risikobewertet und von Tech Lead oder Security Review akzeptiert werden. |
| Letzte fachliche Prüfung | 2026-04-09 |
| Quellenbasis | OWASP Top 10:2021, OWASP API Security Top 10:2023, OWASP Cheat Sheet Series, Spring Security Reference, Spring Framework ProblemDetail, Spring Boot Validation/Test-Dokumentation |

---

## 1. Zweck

Diese Richtlinie definiert den technischen Mindeststandard für sichere Java- und Spring-Boot-Anwendungen. Sie übersetzt zentrale OWASP-Risiken in konkrete Entwicklungsregeln: Input-Validierung, Autorisierung, sichere Fehlerbehandlung, Secrets, Logging, Passwortspeicherung, Spring Security, Dependency-Sicherheit, API-Schutz, Rate Limiting und Security-Tests.

Sicherheit ist keine Eigenschaft, die nachträglich an fertigen Code angeklebt wird. Sie entsteht aus vielen kleinen, konsequenten Entscheidungen: Welche Eingaben werden akzeptiert? Welche Felder darf ein Client setzen? Wo wird Autorisierung geprüft? Was landet in Logs? Welche Defaults sind sicher? Welche Abhängigkeiten sind aktuell? Welche Sicherheitsregeln sind automatisiert prüfbar?

Diese Richtlinie ist bewusst entwicklernah formuliert. Sie soll im Pull Request, beim Design neuer Endpunkte, beim Schreiben von Tests und beim Betrieb von Spring-Boot-Services direkt anwendbar sein.

---

## 2. Kurzregel

Vertraue niemals ungeprüften Eingaben. Prüfe jedes Objekt und jede Aktion gegen Authentifizierung, Autorisierung, Tenant-Kontext, Datenvalidierung, Ressourcenlimits und sichere Fehlerbehandlung. Nutze Spring Security konsequent mit deny-by-default, Methodenautorisierung, sicherer Passwortspeicherung, validierten DTOs, parametrierten Datenbankzugriffen, PII-freiem Logging und automatisierten Security-Gates.

**Sicherer Code entsteht nicht durch eine einzelne Annotation. Sicherer Code entsteht durch eine durchgängige Kette aus Designregel, Implementierung, Test, Review und Automatisierung.**

---

## 3. Geltungsbereich

Diese Richtlinie gilt für alle Codebereiche, in denen Daten aus nicht vollständig kontrollierten Quellen verarbeitet werden. Dazu gehören HTTP Requests, Message Broker, Datei-Uploads, Webhooks, Batch-Imports, CLI-Parameter, Datenbankinhalte aus externen Systemen, Third-Party-API-Antworten, Konfigurationen, Feature-Flags und Admin-Oberflächen.

Besonders betroffen sind:

- REST Controller
- GraphQL-Resolver
- Webhook-Handler
- Message Listener
- Import-Jobs
- Repository Queries
- Service-Methoden mit fachlichen Berechtigungen
- Authentifizierungs- und Autorisierungslogik
- Logging und Observability
- Fehlerbehandlung
- File Uploads
- externe HTTP-Clients
- Integration mit Payment-, Identity-, E-Mail-, Storage- oder KI-Diensten

Nicht jede Regel gilt in gleicher Tiefe für jede interne Methode. Sobald aber externe Eingaben, personenbezogene Daten, Mandantenbezug, Berechtigungen, Zahlungsdaten, Tokens oder operative Steuerung betroffen sind, gilt diese Richtlinie verbindlich.

---

## 4. Technischer Hintergrund

OWASP Top 10:2021 beschreibt die wichtigsten Webanwendungsrisiken, unter anderem Broken Access Control, Cryptographic Failures, Injection, Insecure Design, Security Misconfiguration, Vulnerable and Outdated Components, Identification and Authentication Failures, Software and Data Integrity Failures, Security Logging and Monitoring Failures und SSRF.

Für API-zentrierte SaaS-Plattformen ist zusätzlich OWASP API Security Top 10:2023 relevant. Dort stehen Risiken wie Broken Object Level Authorization, Broken Authentication, Broken Object Property Level Authorization, Unrestricted Resource Consumption, Broken Function Level Authorization, Unrestricted Access to Sensitive Business Flows, SSRF, Security Misconfiguration, Improper Inventory Management und Unsafe Consumption of APIs im Fokus.

Spring Security liefert starke technische Bausteine: Filter Chains, CSRF-Schutz, Security Headers, OAuth2 Resource Server, JWT-Unterstützung, methodenbasierte Autorisierung, Passwort-Encoder und Testunterstützung. Diese Bausteine müssen aber richtig eingesetzt werden. Ein falsch konfiguriertes `permitAll()`, eine fehlende Objektberechtigungsprüfung oder eine deaktivierte CSRF-Strategie an der falschen Stelle erzeugen echte Angriffsflächen.

---

## 5. OWASP-Mapping für Java-/Spring-Teams

| Aspekt | Details/Erklärung | Beispiel | Verbindlicher Standard |
|---|---|---|---|
| Broken Access Control | Nutzer können auf Objekte oder Funktionen zugreifen, die ihnen nicht gehören. | `GET /orders/42` ohne Owner-/Tenant-Prüfung | Jede Objekt-ID aus Request, Header oder Body MUSS gegen Nutzer, Rolle und Tenant geprüft werden. |
| Cryptographic Failures | Daten werden unverschlüsselt, mit schwachen Verfahren oder ohne Schlüsselmanagement verarbeitet. | Passwörter mit SHA-256, Tokens im Log, Secrets im Git | Passwörter mit PasswordEncoder, Secrets aus Secret Store, TLS, keine Tokens im Log. |
| Injection | Eingaben werden in SQL, JPQL, Shell, LDAP, JSONPath, SpEL oder Templates eingesetzt. | SQL-Konkatenation mit `query` | Parameter Binding, Allowlist, Encoding und keine Shell-Konkatenation. |
| Insecure Design | Sicherheitsanforderungen fehlen im Design. | Admin-Operation ohne Berechtigungskonzept | Security-Annahmen MÜSSEN vor Implementierung explizit sein. |
| Security Misconfiguration | Unsichere Defaults, offene Actuator-Endpunkte, Debug-Informationen. | `/actuator/env` öffentlich | Minimale Exposition, Profile trennen, Actuator schützen. |
| Vulnerable Components | Bibliotheken enthalten bekannte CVEs. | alte Jackson-/Logback-/Netty-Version | Dependency Scanning und regelmäßige Updates. |
| Authentication Failures | Login, Token, Sessions oder Passwortregeln sind schwach. | JWT ohne Ablaufzeit, unsicherer Passwortspeicher | OAuth2/OIDC/JWT sauber konfigurieren, PasswordEncoder verwenden. |
| Software/Data Integrity Failures | Build-, Dependency-, Plugin- oder Updatekette ist manipulierbar. | ungeprüfte Images, unfixierte Actions | Signaturen, Checksums, CI-Gates, SBOM und Image-Scanning. |
| Logging/Monitoring Failures | Angriffe werden nicht erkannt oder Logs enthalten sensitive Daten. | keine Audit-Events oder Passwort im Log | Sicherheitsereignisse loggen, sensitive Daten maskieren. |
| SSRF | Server ruft vom Nutzer kontrollierte URLs auf. | Import von `http://169.254.169.254/...` | URL-Allowlist, DNS/IP-Blocklisten, egress controls. |

---

## 6. Verbindlicher Standard

Jeder neue sicherheitsrelevante Code MUSS folgende Mindestregeln erfüllen:

1. Eingaben werden über typisierte DTOs modelliert, nicht über freie `Map<String,Object>`-Strukturen.
2. Bean Validation wird für syntaktische und einfache fachliche Grenzen genutzt.
3. Kritische fachliche Regeln werden zusätzlich im Service oder Domänenmodell geprüft.
4. Datenbankzugriffe verwenden Parameter Binding, keine String-Konkatenation.
5. Autorisierung wird auf Endpunkt- und Service-/Methodenebene geprüft.
6. Jede Objekt-ID aus dem Request wird gegen Owner, Berechtigung und Tenant geprüft.
7. Clients dürfen keine Rollen, Preise, Status, Tenant-IDs oder systemseitigen Felder setzen, wenn diese serverseitig bestimmt werden müssen.
8. Secrets werden nicht im Code, nicht in `application.yml`, nicht in Logs und nicht in Testfixtures gespeichert.
9. Passwörter werden ausschließlich mit einem geeigneten `PasswordEncoder` verarbeitet.
10. Fehlerantworten sind standardisiert und enthalten keine sensitiven Details.
11. Logs, Traces und Metriken enthalten keine Passwörter, Tokens, API Keys, vollständige personenbezogene Daten oder Session-Werte.
12. Security-Regeln werden getestet und in CI/CD mindestens teilweise automatisiert geprüft.

---

## 7. Input-Validierung: Niemals dem Client vertrauen

### 7.1 Schlechte Anwendung: unkontrollierte Eingaben direkt verarbeiten

```java
@PostMapping("/search")
public List<Product> search(@RequestParam String query,
                            @RequestParam int page) {
    String sql = "SELECT * FROM products WHERE name LIKE '%" + query + "%' LIMIT " + page;
    return jdbcTemplate.query(sql, productMapper);
}
```

Dieser Code ist aus mehreren Gründen gefährlich: `query` wird direkt in SQL eingesetzt, `page` wird semantisch falsch als Limit benutzt, es gibt keine Länge, keine erlaubten Zeichen, keine maximale Seitengröße und keine fachliche Kontrolle. Das ist kein API-Design-Fehler allein, sondern ein Sicherheitsfehler.

### 7.2 Gute Anwendung: typisiertes Request-DTO mit Bean Validation

```java
public record ProductSearchRequest(
        @NotBlank
        @Size(min = 2, max = 100)
        @Pattern(regexp = "^[\\p{L}\\p{N} ._\\-]+$", message = "query contains unsupported characters")
        String query,

        @Min(0)
        int page,

        @Min(1)
        @Max(100)
        int size
) {
}
```

```java
@GetMapping("/api/v1/products")
public Page<ProductSummaryResponse> search(@Valid ProductSearchRequest request) {
    return productService.search(request);
}
```

### 7.3 Gute Anwendung: Parameter Binding statt SQL-Konkatenation

```java
@Repository
public class ProductJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProductJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductEntity> searchByName(String query, int limit, int offset) {
        var sql = """
                SELECT id, name, price
                FROM products
                WHERE lower(name) LIKE lower(:query)
                ORDER BY name ASC
                LIMIT :limit OFFSET :offset
                """;

        var params = Map.of(
                "query", "%" + query + "%",
                "limit", limit,
                "offset", offset
        );

        return jdbc.query(sql, params, productMapper);
    }
}
```

Parameter Binding verhindert nicht jede Form fachlicher Fehlbedienung, aber es verhindert, dass Eingaben als SQL-Struktur interpretiert werden.

---

## 8. Mass Assignment und Broken Object Property Level Authorization

### 8.1 Schlechte Anwendung: freie Map oder Entity Binding

```java
@PostMapping("/users")
public void createUser(@RequestBody Map<String, Object> body) {
    String name = (String) body.get("name");
    String email = (String) body.get("email");
    String role = (String) body.get("role");

    userService.create(name, email, role);
}
```

Hier kann der Client potenziell Felder setzen, die niemals clientseitig steuerbar sein dürfen. Rollen, Preise, Rabatte, Freigabestatus, Tenant-IDs, Owner-IDs und Audit-Felder gehören nicht ungeprüft in Request-DTOs.

Noch gefährlicher ist direktes Binding auf Entities:

```java
@PostMapping("/users")
public UserEntity createUser(@RequestBody UserEntity entity) {
    return userRepository.save(entity);
}
```

Das koppelt API-Vertrag, Persistenzmodell und Sicherheitsgrenze. Es öffnet die Tür für übermäßige Datenfreigabe und Mass Assignment.

### 8.2 Gute Anwendung: explizite Request-DTOs

```java
public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 128) String password
) {
}
```

```java
@PostMapping("/api/v1/users")
@ResponseStatus(HttpStatus.CREATED)
public UserCreatedResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userRegistrationService.register(request);
}
```

Die Rolle wird serverseitig bestimmt:

```java
@Service
public class UserRegistrationService {

    @Transactional
    public UserCreatedResponse register(CreateUserRequest request) {
        var user = UserEntity.registerStandardUser(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        var saved = userRepository.save(user);
        return UserCreatedResponse.from(saved);
    }
}
```

---

## 9. Autorisierung: Authentifizierung ist nicht genug

Authentifizierung beantwortet: **Wer ist der Nutzer?**

Autorisierung beantwortet: **Darf dieser Nutzer diese konkrete Aktion an diesem konkreten Objekt ausführen?**

Viele Sicherheitslücken entstehen, weil Teams nach dem Login nur noch prüfen, ob irgendein Nutzer angemeldet ist. Das reicht nicht. Besonders in SaaS-Systemen muss jeder Zugriff auf tenantbezogene Daten objektbezogen geprüft werden.

### 9.1 Schlechte Anwendung: nur angemeldet reicht

```java
@GetMapping("/api/v1/orders/{id}")
public OrderDetailResponse findById(@PathVariable Long id) {
    return orderService.findById(id);
}
```

Wenn `orderService.findById(id)` nur nach ID lädt, kann ein Nutzer durch Ändern der ID fremde Bestellungen abrufen.

### 9.2 Gute Anwendung: Objekt- und Tenant-Prüfung

```java
@Service
public class OrderService {

    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.canReadOrder(#id, authentication)")
    @Transactional(readOnly = true)
    public OrderDetailResponse findById(OrderId id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return orderMapper.toDetail(order);
    }
}
```

Noch robuster ist eine tenantgebundene Repository-Abfrage:

```java
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByIdAndTenantId(Long id, TenantId tenantId);
}
```

```java
@Transactional(readOnly = true)
public OrderDetailResponse findById(OrderId id, TenantId tenantId) {
    var order = orderRepository.findByIdAndTenantId(id.value(), tenantId)
            .orElseThrow(() -> new OrderNotFoundException(id));
    return orderMapper.toDetail(order);
}
```

Bei tenantkritischen Daten ist die zweite Variante oft vorzuziehen, weil sie die Mandantengrenze bereits im Datenzugriff erzwingt.

---

## 10. Spring Security: Basiskonfiguration mit sicheren Defaults

### 10.1 Verbindliche Grundkonfiguration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**", "/actuator/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; object-src 'none'; frame-ancestors 'none'")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
```

Diese Konfiguration ist ein Ausgangspunkt, kein universelles Copy-Paste. CSRF darf nur dann deaktiviert werden, wenn die API wirklich stateless ist und keine browserseitige Cookie-Session als Authentifizierungsmechanismus nutzt. Für klassische Webanwendungen mit Login-Session ist CSRF-Schutz notwendig.

### 10.2 CSRF-Entscheidung

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Stateless API mit Bearer Token im Authorization Header | CSRF-Risiko ist anders gelagert, weil der Browser den Bearer Header nicht automatisch an fremde Seiten anhängt. | Mobile/API-Client mit OAuth2 Resource Server | CSRF kann deaktiviert werden, wenn keine Cookie-Auth genutzt wird. |
| Browser-App mit Session-Cookie | Browser sendet Cookies automatisch mit. | Server-side rendered Spring MVC App | CSRF MUSS aktiv bleiben. |
| SPA mit Cookie-basierter Authentifizierung | Cookie wird automatisch gesendet. | Angular/React mit Session-Cookie | CSRF-Schutz oder anderes belastbares Schutzkonzept erforderlich. |
| Interne Service-to-Service-Kommunikation | Kein Browserkontext. | mTLS/JWT zwischen Services | CSRF nicht relevant, AuthN/AuthZ aber weiterhin Pflicht. |

---

## 11. Methodenautorisierung: fachliche Grenzen im Service sichern

HTTP-Regeln reichen nicht aus. Viele Services werden nicht nur von Controllern aufgerufen, sondern auch von Message Listenern, Jobs oder anderen Services. Deshalb gehören kritische Berechtigungen zusätzlich auf Methodenebene oder in explizite Security-Services.

```java
@Service
public class TenantAdministrationService {

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public void suspendTenant(TenantId tenantId) {
        tenantRepository.suspend(tenantId);
    }

    @PreAuthorize("@tenantSecurity.canManageUsers(#tenantId, authentication)")
    public void inviteUser(TenantId tenantId, InviteUserCommand command) {
        tenantUserService.invite(tenantId, command);
    }
}
```

Regel: Sicherheitskritische Service-Methoden DÜRFEN NICHT allein darauf vertrauen, dass der Controller sie korrekt schützt.

---

## 12. Fehlerbehandlung: sicher, standardisiert und ohne Datenleck

### 12.1 Schlechte Anwendung: interne Details nach außen geben

```java
@ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public Map<String, Object> handle(Exception ex) {
    return Map.of(
            "error", ex.getClass().getName(),
            "message", ex.getMessage(),
            "stackTrace", Arrays.toString(ex.getStackTrace())
    );
}
```

Dieser Handler leakt interne Klassen, Stacktraces, Datenbankdetails und potenziell sensitive Werte.

### 12.2 Gute Anwendung: Problem Details

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setDetail("The request contains invalid fields.");
        problem.setProperty("errors", ex.getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", safeValidationMessage(error.getDefaultMessage())))
                .toList());
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied() {
        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("You are not allowed to perform this action.");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred.");
        return problem;
    }

    private String safeValidationMessage(String message) {
        return message == null ? "Invalid value" : message;
    }
}
```

Interne Details gehören in geschützte Logs, nicht in API-Antworten.

---

## 13. Secrets: Niemals im Code, niemals im Log, niemals als Default

### 13.1 Schlechte Anwendung

```java
@Value("${jwt.secret:mein-super-geheimer-schluessel-123}")
private String jwtSecret;

private static final String API_KEY = "sk-prod-abc123xyz789";

log.info("Connecting to DB with password: {}", dbPassword);
log.debug("JWT token: {}", token);
```

Hardcodierte Secrets landen in Git, Build-Artefakten, Logs, IDE-Indexen, Stacktraces oder Screenshots. Ein Default-Secret in `@Value` ist besonders gefährlich, weil es fehlende Konfiguration verdeckt.

### 13.2 Gute Anwendung

```yaml
jwt:
  issuer: "https://identity.example.com"
  audience: "order-service"
  # secret nicht hier speichern
```

```java
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String audience
) {
}
```

Secrets werden über Secret Store, Kubernetes Secrets, Vault, Cloud Secret Manager oder CI/CD-Secret-Mechanismen bereitgestellt. Sie dürfen nicht im Repository liegen.

Logging-Regel:

```java
log.info("User authenticated: userId={}, requestId={}", userId, requestId);

// Verboten:
// log.info("User authenticated with token={}", token);
// log.info("Login password={}", password);
```

---

## 14. Passwortspeicherung: PasswordEncoder statt eigener Kryptografie

Passwörter dürfen niemals im Klartext, mit Base64, MD5, SHA-1 oder einfachem SHA-256 gespeichert werden. Passwortspeicherung braucht langsame, adaptive Verfahren mit Salt und konfigurierbarem Aufwand.

### 14.1 Schlechte Anwendung

```java
String hash = DigestUtils.sha256Hex(password);
String encoded = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
```

Base64 ist Kodierung, kein Schutz. Schnelle Hashfunktionen sind für Passwortspeicherung nicht geeignet, weil sie Offline-Brute-Force-Angriffe begünstigen.

### 14.2 Gute Anwendung mit Spring Security

```java
@Configuration
public class PasswordConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

```java
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }
}
```

`DelegatingPasswordEncoder` ist migrationsfreundlich, weil gespeicherte Hashes mit Algorithmus-Präfixen arbeiten können. Für neue Systeme sind Argon2id, bcrypt oder PBKDF2 je nach Plattform, Compliance-Kontext und Betriebsanforderung zu bewerten. In Spring-Projekten ist `PasswordEncoder` der verbindliche Mechanismus, nicht eigene Kryptografie.

---

## 15. Output Encoding und XSS

Input-Validierung allein verhindert kein XSS. Daten, die in HTML, JavaScript, CSS, URLs oder JSON eingebettet werden, müssen im jeweiligen Ausgabekontext korrekt encodiert werden.

Schlecht:

```java
String html = """
        <h1>Hello %s</h1>
        """.formatted(userInput);
```

Wenn `userInput` HTML oder JavaScript enthält, kann daraus XSS entstehen.

Besser:

- Template Engine mit Auto-Escaping verwenden.
- Keine HTML-Fragmente per String-Konkatenation bauen.
- CSP setzen.
- Bei JSON APIs sicherstellen, dass Consumer korrekt rendern.
- Keine untrusted Daten in JavaScript-Kontext schreiben.

Spring Security kann Sicherheitsheader setzen, aber Header ersetzen nicht korrektes Output Encoding.

---

## 16. Logging und Observability: sicherheitsrelevant, aber datenarm

Logs müssen genug Kontext liefern, damit Sicherheitsvorfälle erkannt und analysiert werden können. Gleichzeitig dürfen sie keine sensiblen Daten enthalten.

### 16.1 Erlaubt

```java
log.info("Order created: orderId={}, tenantId={}, userId={}, requestId={}",
        orderId, tenantId, userId, requestId);
```

### 16.2 Verboten

```java
log.info("Login failed: email={}, password={}", email, password);
log.debug("Authorization header={}", authorizationHeader);
log.warn("Payment failed: cardNumber={}, cvv={}", cardNumber, cvv);
```

### 16.3 Regel für MDC

```java
MDC.put("requestId", requestId);
MDC.put("tenantId", tenantId.value());
MDC.put("userId", userId.value());

// Verboten:
// MDC.put("email", email);
// MDC.put("token", token);
// MDC.put("password", password);
```

Für personenbezogene Daten gelten die Regeln aus dem Data-Masking- und Pseudonymisierungsstandard des Kompendiums.

---

## 17. SSRF: ausgehende Requests absichern

Server-Side Request Forgery entsteht, wenn ein Server auf Basis von Nutzereingaben interne oder externe URLs abruft. Besonders gefährlich sind Metadaten-Endpunkte, interne Admin-APIs, Kubernetes-Service-Netze und Cloud-Provider-Metadatenadressen.

### 17.1 Schlechte Anwendung

```java
@PostMapping("/fetch-preview")
public String fetchPreview(@RequestParam URI url) {
    return restClient.get()
            .uri(url)
            .retrieve()
            .body(String.class);
}
```

### 17.2 Gute Anwendung: Allowlist und Egress-Kontrolle

```java
@Service
public class SafeUrlFetchService {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "images.example-cdn.com",
            "docs.example.com"
    );

    public String fetch(URI uri) {
        validateUri(uri);
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }

    private void validateUri(URI uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new ValidationException("Only https URLs are allowed.");
        }
        if (!ALLOWED_HOSTS.contains(uri.getHost())) {
            throw new ValidationException("Host is not allowed.");
        }
    }
}
```

Zusätzlich gehören ausgehende Netzwerkregeln, DNS-/IP-Prüfungen und Blocklisten für private/metadata IP-Bereiche in die Plattformschicht.

---

## 18. Rate Limiting und Ressourcenlimits

Sicherer Code begrenzt nicht nur Eingaben syntaktisch, sondern auch Ressourcenverbrauch. Ohne Limits können APIs durch große Payloads, tiefe JSON-Strukturen, teure Queries, massenhafte Login-Versuche oder unlimitierte Pagination angegriffen oder versehentlich überlastet werden.

Verbindliche Regeln:

- Listen-Endpunkte haben maximale `size`-Werte.
- Suchendpunkte haben minimale und maximale Query-Längen.
- Uploads haben Dateigröße, Dateityp und Scan-Regeln.
- Login-, Passwort-Reset- und MFA-Endpunkte haben Rate Limits.
- Teure Exporte und Reports laufen asynchron.
- Tenant-bezogene Quoten werden serverseitig geprüft.

```java
public record PageRequestDto(
        @Min(0) int page,
        @Min(1) @Max(100) int size
) {
}
```

---

## 19. Dependency Security und Build-Schutz

Abhängigkeiten sind Teil der Angriffsfläche. Veraltete Libraries, ungeprüfte Plugins und unsichere Container-Images können Sicherheitslücken einführen, ohne dass eigener Code geändert wurde.

Verbindliche Mindestregeln:

1. Dependency-Scanning in CI/CD.
2. Container-Image-Scanning für Runtime-Images.
3. Keine ungeprüften Snapshot-Abhängigkeiten in produktiven Services.
4. Keine Build-Plugins ohne Versionspinning.
5. Regelmäßige Updates von Spring Boot, Spring Security, Jackson, Netty/Tomcat, Logback und Datenbanktreibern.
6. SBOM-Erzeugung für produktive Artefakte, wenn organisatorisch vorgesehen.

Beispiel Maven OWASP Dependency-Check:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>12.1.1</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

Die konkrete Version muss projektweit zentral gepflegt werden.

---

## 20. Gute und falsche Anwendung im Überblick

| Aspekt | Details/Erklärung | Beispiel | Bewertung |
|---|---|---|---|
| Request Binding | Explizites DTO mit Validation | `CreateUserRequest` ohne `role` | Gut |
| Freie Map | Client kann beliebige Felder senden | `Map<String,Object>` | Schlecht |
| Entity als API | Persistenzmodell wird API-Vertrag | `@RequestBody UserEntity` | Schlecht |
| SQL | Parameter Binding | `WHERE name LIKE :query` | Gut |
| SQL-Konkatenation | Eingaben werden Code | `"..." + query` | Verboten |
| Authentifizierung | Token/JWT/OIDC sauber validiert | OAuth2 Resource Server | Gut |
| Autorisierung | Objekt, Tenant und Funktion prüfen | `findByIdAndTenantId` | Pflicht |
| Password Storage | `PasswordEncoder` | `DelegatingPasswordEncoder` | Pflicht |
| Logging | IDs statt Secrets | `userId`, `tenantId`, `requestId` | Gut |
| Token Logging | Auth Header im Log | `Bearer ...` | Verboten |
| CSRF | Kontextabhängig konfigurieren | Session-App: aktiv | Pflichtentscheidung |
| Actuator | Nur Health/Info öffentlich | `/actuator/health` | Gut |
| Actuator offen | Env/Beans/Heapdump öffentlich | `/actuator/env` | Verboten |

---

## 21. Security-Tests

Security-Regeln müssen testbar sein. Ein Service gilt nicht als sicher, nur weil eine Annotation im Code steht.

### 21.1 Controller-Security mit MockMvc

```java
@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;

    @Test
    void findById_returnsUnauthorized_whenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findById_returnsForbidden_whenUserHasNoPermission() throws Exception {
        when(orderService.findById(new OrderId(1L)))
                .thenThrow(new AccessDeniedException("forbidden"));

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isForbidden());
    }
}
```

### 21.2 Method Security testen

```java
@SpringBootTest
class OrderServiceMethodSecurityTest {

    @Autowired OrderService orderService;

    @Test
    @WithMockUser(roles = "USER")
    void cancelOrder_deniesAccess_whenUserIsNotOwner() {
        assertThatThrownBy(() -> orderService.cancelOrder(new OrderId(99L)))
                .isInstanceOf(AccessDeniedException.class);
    }
}
```

Tests müssen mindestens diese Fälle abdecken:

- anonym
- authentifiziert ohne Recht
- authentifiziert mit Recht
- falscher Tenant
- fremde Objekt-ID
- ungültige Eingabe
- gefährliche Eingabe
- fehlendes oder ungültiges Token
- Rate-Limit-/Quota-Fall, sofern relevant

---

## 22. Automatisierbare Prüfungen

| Aspekt | Details/Erklärung | Beispiel | Automatisierung |
|---|---|---|---|
| Dependency CVEs | Bekannte Schwachstellen in Libraries | Jackson, Netty, Logback | OWASP Dependency-Check, Snyk, Dependabot, Renovate |
| Secrets | Secrets im Repository | API Keys, Tokens | Gitleaks, TruffleHog, GitHub Secret Scanning |
| Container Images | Schwachstellen und Fehlkonfiguration | Runtime Image | Trivy, Grype |
| Static Analysis | Unsichere Code-Muster | SQL-Konkatenation | Semgrep, SpotBugs, SonarQube |
| Architekturregeln | Schicht- und Security-Regeln | Controller darf Entity nicht binden | ArchUnit |
| Tests | Security-Verhalten | 401/403, Tenant-Grenzen | JUnit, Spring Security Test |
| OpenAPI | Sicherheitsdefinitionen und Statuscodes | Bearer Auth, Problem Details | springdoc, OpenAPI Validator |

Beispiel Semgrep-Regel als Richtung:

```yaml
rules:
  - id: java-sql-string-concat
    message: "SQL darf nicht per String-Konkatenation mit Nutzereingaben gebaut werden."
    severity: ERROR
    languages: [java]
    pattern-either:
      - pattern: |
          $SQL = "..." + $INPUT;
      - pattern: |
          $JDBC.query("..." + $INPUT, ...);
```

---

## 23. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Eingaben | Gibt es typisierte Request-DTOs mit Validation? | `@Valid CreateUserRequest` | Pflicht |
| Client-Felder | Kann der Client Rollen, Preise, Tenant oder Status setzen? | `role` im Request | Verboten, wenn serverseitig bestimmt |
| SQL/Queries | Werden Parameter gebunden? | `:query`, `?` | Pflicht |
| Objektzugriff | Wird jede Objekt-ID gegen Nutzer/Tenant geprüft? | `/orders/{id}` | Pflicht |
| Method Security | Sind kritische Service-Methoden geschützt? | `@PreAuthorize` | Pflicht bei kritischen Aktionen |
| CSRF | Ist CSRF passend zum Auth-Modell entschieden? | stateless API vs Session | Pflichtentscheidung |
| Secrets | Sind keine Secrets in Code/YAML/Logs? | JWT Secret | Pflicht |
| Passwörter | Wird `PasswordEncoder` verwendet? | bcrypt/Delegating | Pflicht |
| Fehlerantworten | Keine Stacktraces, keine internen Details? | ProblemDetail | Pflicht |
| Logging | Keine Tokens, Passwörter, vollständige PII? | Authorization Header | Pflicht |
| Actuator | Sind nur erlaubte Endpunkte öffentlich? | health/info | Pflicht |
| Tests | Gibt es 401/403/Tenant/Validation-Tests? | MockMvc | Pflicht |
| Dependencies | Ist Dependency-/Image-Scanning aktiv? | CI | Pflicht |

---

## 24. Migration bestehender unsicherer Muster

Bestehende Codebasen werden in kontrollierten Schritten migriert.

### Schritt 1: Sichtbarkeit herstellen

Zuerst werden Risiken inventarisiert:

- Controller mit `Map<String,Object>`
- Controller mit Entity Binding
- SQL-Konkatenation
- fehlende `@Valid`-Annotationen
- Endpunkte ohne Security-Test
- `permitAll()`-Regeln
- offene Actuator-Endpunkte
- Secrets in Konfiguration
- Logs mit Tokens oder personenbezogenen Daten

### Schritt 2: API-Grenzen stabilisieren

Entities werden aus Request-/Response-Verträgen entfernt. Es werden explizite DTOs eingeführt. Kritische Felder wie `role`, `tenantId`, `ownerId`, `price`, `status`, `createdAt`, `approvedBy` werden serverseitig gesetzt.

### Schritt 3: Autorisierung nachziehen

Objektzugriffe werden tenant- und ownergebunden. Repository-Methoden werden, wo sinnvoll, um `TenantId` ergänzt. Service-Methoden bekommen explizite Berechtigungsprüfungen.

### Schritt 4: Tests ergänzen

Für jeden kritischen Endpunkt werden mindestens positive und negative Security-Tests ergänzt: anonym, falsche Rolle, falscher Tenant, fremde ID, erlaubter Zugriff.

### Schritt 5: Automatisierung aktivieren

Dependency-Scanning, Secret-Scanning, statische Analyse und Security-Test-Gates werden in CI/CD integriert.

---

## 25. Ausnahmen

Ausnahmen sind nur zulässig, wenn ein nachvollziehbarer technischer oder fachlicher Grund vorliegt. Beispiele:

- Ein internes Tool läuft in einer stark abgeschotteten Umgebung, verarbeitet keine sensitiven Daten und hat zusätzliche Netzwerkgrenzen.
- Ein Legacy-Endpunkt muss vorübergehend unterstützt werden, während Consumer migriert werden.
- Ein Framework erzwingt ein bestimmtes Binding-Modell, das durch zusätzliche Tests und Filter abgesichert wird.

Jede Ausnahme MUSS dokumentieren:

1. Welche Regel wird verletzt?
2. Warum ist die Abweichung notwendig?
3. Welche kompensierenden Kontrollen gibt es?
4. Wann wird die Ausnahme erneut geprüft?
5. Wer hat die Ausnahme akzeptiert?

---

## 26. Definition of Done

Eine neue Java-/Spring-Boot-Funktion erfüllt diese Richtlinie, wenn alle folgenden Punkte erfüllt sind:

1. Alle externen Eingaben laufen über typisierte DTOs oder explizite Parser.
2. Bean Validation ist aktiv und fachliche Invarianten werden zusätzlich geprüft.
3. Es gibt keine SQL-/JPQL-/Shell-/URL-Konkatenation mit ungeprüften Eingaben.
4. Objektzugriffe prüfen Nutzer, Rolle und Tenant-Kontext.
5. Kritische Service-Methoden sind durch Method Security oder explizite Security-Services geschützt.
6. Secrets liegen nicht im Code, nicht in YAML und nicht in Logs.
7. Passwörter werden nur mit `PasswordEncoder` verarbeitet.
8. Fehlerantworten sind standardisiert und enthalten keine internen Details.
9. Logs, Traces und Metriken enthalten keine Tokens, Passwörter, API Keys oder vollständigen sensitiven Daten.
10. Actuator-Endpunkte sind minimal exponiert und geschützt.
11. Dependency- und Secret-Scanning laufen in CI/CD.
12. Security-Tests decken mindestens anonymen Zugriff, falsche Rolle, falschen Tenant, fremde Objekt-ID und gültigen Zugriff ab.
13. Die Review-Checkliste wurde im Pull Request sichtbar berücksichtigt.

---

## 27. Quellen und weiterführende Literatur

- OWASP Top 10:2021 — https://owasp.org/Top10/2021/
- OWASP API Security Top 10:2023 — https://owasp.org/API-Security/editions/2023/en/0x11-t10/
- OWASP Input Validation Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
- OWASP SQL Injection Prevention Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html
- OWASP Injection Prevention Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Injection_Prevention_Cheat_Sheet.html
- OWASP Password Storage Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- OWASP Logging Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- Spring Security Reference: CSRF — https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
- Spring Security Reference: Method Security — https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
- Spring Security Reference: Password Storage — https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
- Spring Security Reference: Security HTTP Response Headers — https://docs.spring.io/spring-security/reference/servlet/exploits/headers.html
- Spring Framework Reference: Problem Details — https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html

