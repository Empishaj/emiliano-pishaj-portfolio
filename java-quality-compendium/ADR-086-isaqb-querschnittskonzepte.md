# ADR-086 — iSAQB F5: Querschnittskonzepte — Was alle Schichten berührt

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Foundation F5 · Querschnittskonzepte                   |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| iSAQB-Lernziel    | LZ-5-1 bis LZ-5-7 (Foundation Level)                         |

---

## 1. Was sind Querschnittskonzepte?

```
FACHLICHE ANFORDERUNGEN (vertical slice):
  "Ein Nutzer kann eine Bestellung aufgeben"
  → betrifft: Order-Modul, Order-API, Order-DB
  → klar abgegrenzte Verantwortlichkeit

QUERSCHNITTSKONZEPTE (cross-cutting concerns):
  "Alle Aktionen werden geloggt."
  "Alle Endpunkte sind abgesichert."
  "Alle Fehler werden einheitlich behandelt."
  → betrifft: JEDEN Service, JEDE Schicht, JEDES Modul
  → Lösung an einem Ort, Wirkung überall

Das PROBLEM ohne bewusste Querschnitts-Strategie:
  Logging in OrderService: log.info("Order created");
  Logging in PaymentService: System.out.println("Payment charged");
  Logging in UserService: logger.debug("User found: " + user);
  → 3 verschiedene Strategien, 3 verschiedene Formate
  → Monitoring unmöglich, Fehlerdiagnose dauert Stunden
```

---

## 2. Querschnittskonzept: Logging & Observability

### 2.1 Logging-Strategie (einheitlich, strukturiert)

```java
// ❌ SCHLECHT — unstrukturiertes Logging, verschiedene Formate
public class OrderService {
    public void placeOrder(PlaceOrderCommand cmd) {
        System.out.println("Order placed for user: " + cmd.userId()); // stdout
        log.debug("Processing: " + cmd);                               // DEBUG (zu viel!)
        // Kein Trace-Kontext → bei 100 parallelen Anfragen nicht zuordenbar
        // Kein strukturiertes Format → kein grep, kein Kibana-Filter
    }
}

// ✅ GUT — strukturiertes Logging mit Kontext
@Service
@Slf4j
public class OrderDomainService {

    @Autowired
    private MeterRegistry meterRegistry;

    public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
        // Strukturierter Log-Kontext (MDC = Mapped Diagnostic Context)
        // Trace-ID kommt automatisch aus OpenTelemetry (→ ADR-017)
        try {
            log.info("Placing order",                           // Event-Name
                kv("userId",   cmd.userId().value()),           // strukturiert
                kv("itemCount", cmd.items().size()),
                kv("totalEur",  cmd.estimatedTotal().amount())
            );

            var order = Order.place(cmd);
            orderRepository.save(order);

            // Business-Event loggen (nicht: technisches Event)
            log.info("Order placed successfully",
                kv("orderId",  order.id().value()),
                kv("status",   order.status()),
                kv("durationMs", /* elapsed */0)
            );

            // Metrik für SLO-Monitoring (→ ADR-054)
            meterRegistry.counter("orders.placed",
                "currency", cmd.currency()).increment();

            return OrderCreatedResult.from(order);

        } catch (InsufficientStockException e) {
            // Business-Fehler: WARN (erwartet, aber relevant)
            log.warn("Order placement failed: insufficient stock",
                kv("userId",    cmd.userId().value()),
                kv("productId", e.productId()),
                kv("requested", e.requested()),
                kv("available", e.available())
            );
            meterRegistry.counter("orders.failed", "reason", "stock").increment();
            throw e;
        }
    }
}
```

### 2.2 Log-Level-Strategie (verbindlich für alle Teams)

```yaml
# application.yml — Log-Level-Konvention für alle Services

logging:
  level:
    root: WARN                              # Standard: nur Warnings

    # Unsere Anwendungs-Pakete
    com.example:                   INFO     # Business-Events
    com.example.domain:            INFO     # Domänen-Entscheidungen
    com.example.adapter.rest:      INFO     # HTTP-Requests (komprimiert)
    com.example.adapter.persistence: WARN  # DB: nur Probleme

    # Frameworks (sehr geschwätzig → reduzieren)
    org.springframework:           WARN
    org.hibernate.SQL:             DEBUG    # Nur für lokale Entwicklung!
    org.hibernate.type:            TRACE    # NIEMALS in Produktion!

  # Format: JSON für Produktion (maschinenlesbar → Kibana/Loki)
  # Format: Pretty-Print für lokale Entwicklung
  pattern:
    console: "%d{HH:mm:ss} %-5level [%X{traceId}] %logger{36} - %msg%n"
```

```
LOG-LEVEL-BEDEUTUNG (verbindliche Konvention):

ERROR: Unbehandelte Exception, System-Fehler, Datenverlust-Risiko
       → Alert wird ausgelöst, On-Call wird geweckt
       → NICHT: validierungsfehler, business-exceptions

WARN:  Erwarteter Fehler mit Auswirkung (kein Bestand, Zahlung abgelehnt)
       → Ticket wird erstellt, wird bei Review angeschaut
       → NICHT: "deprecated API used" → ist INFO

INFO:  Business-Ereignis (Order placed, User registered, Payment succeeded)
       → Das ist die primäre Audit-Trail-Ebene
       → NICHT: technische Details (SQL, HTTP-Headers)

DEBUG: Technische Details für Fehleranalyse
       → NUR in Entwicklung und Staging, NIEMALS in Produktion dauerhaft

TRACE: Maximum-Verbose (Framework-Internals)
       → Nur temporär, nie dauerhaft aktiviert
```

---

## 3. Querschnittskonzept: Fehlerbehandlung

### 3.1 Die drei Fehlerklassen

```
TECHNISCHER FEHLER (Infrastructure Error):
  Ursache: Netzwerk, DB-Verbindung, Disk voll
  Behandlung: Retry, Circuit Breaker, Fallback
  Log-Level: ERROR
  HTTP-Status: 503 Service Unavailable

FACHLICHER FEHLER (Domain Error):
  Ursache: Geschäftsregel verletzt (kein Bestand, ungültige IBAN)
  Behandlung: Nutzer informieren, nicht retrien
  Log-Level: WARN
  HTTP-Status: 422 Unprocessable Entity

PROGRAMMIERFEHLER (Programming Error):
  Ursache: NullPointerException, ArrayIndexOutOfBounds
  Behandlung: Bugfix, kein Retry
  Log-Level: ERROR
  HTTP-Status: 500 Internal Server Error
```

### 3.2 Einheitliche Fehlerbehandlung als Querschnittskonzept

```java
// ❌ SCHLECHT — Fehlerbehandlung in jedem Controller anders
@RestController
public class OrderController {
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        try {
            return ResponseEntity.ok(orderService.create(req));
        } catch (Exception e) {
            // Was für ein Status? 400? 500? Welches Format?
            return ResponseEntity.status(500).body(e.getMessage());
            // getMessage() kann interne Details leaken!
        }
    }
}

// ✅ GUT — Zentraler Exception-Handler als Querschnittskonzept
// Gilt für ALLE Controller des gesamten Systems

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Fachliche Fehler → 422 + strukturiertes Problem-Detail (RFC 9457)
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(
            DomainException ex, HttpServletRequest req) {
        log.warn("Domain rule violated: {}", ex.getMessage(),
            kv("exceptionType", ex.getClass().getSimpleName()));

        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("https://docs.example.com/errors/" + ex.errorCode()));
        problem.setTitle(ex.title());
        problem.setProperty("errorCode", ex.errorCode());
        return problem;
    }

    // Validierungsfehler → 400 mit Feldliste
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
            .toList();

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setProperty("violations", fields);
        return problem;
    }

    // Technische Fehler → 503 OHNE interne Details (Security!)
    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleTechnicalError(
            DataAccessException ex, HttpServletRequest req) {
        // Interne Details NICHT an Client: SQL-Fehlermeldungen können Schema verraten
        var traceId = MDC.get("traceId");
        log.error("Database error", ex, kv("traceId", traceId));

        var problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Service temporarily unavailable");
        problem.setDetail("Please try again. Reference: " + traceId);
        // traceId für Support, kein Stack-Trace für Angreifer
        return problem;
    }

    // Fallback für alles andere
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please contact support.");
    }
}
```

### 3.3 Domain-Exception-Hierarchie

```java
// Basis-Klasse für alle fachlichen Fehler
public abstract class DomainException extends RuntimeException {
    public abstract String errorCode();
    public abstract String title();

    protected DomainException(String message) { super(message); }
}

// Konkrete Domain-Fehler
public class InsufficientStockException extends DomainException {
    private final ProductId productId;
    private final int requested, available;

    @Override public String errorCode() { return "INSUFFICIENT_STOCK"; }
    @Override public String title()     { return "Insufficient Stock"; }
}

public class OrderNotFoundException extends DomainException {
    private final OrderId orderId;

    @Override public String errorCode() { return "ORDER_NOT_FOUND"; }
    @Override public String title()     { return "Order Not Found"; }
}

// Technische Fehler (nicht DomainException)
public class PaymentGatewayException extends RuntimeException {
    // → wird zu 503, nicht 422
}
```

---

## 4. Querschnittskonzept: Sicherheit als AOP

### 4.1 Security nicht in Business-Logik einbetten

```java
// ❌ SCHLECHT — Security-Checks in der Domäne (verletzt SoC → ADR-084)
public class OrderDomainService {
    public void cancelOrder(OrderId id, String userToken) {
        // Security-Logik IN der Domäne
        var claims = jwtParser.parseClaims(userToken);     // JWT-Library!
        if (!claims.get("role").equals("ADMIN")
                && !claims.get("sub").equals(order.userId())) {
            throw new AccessDeniedException("Not authorized");
        }
        // ...
    }
}
// Problem: Domäne kennt JWT-Format, JWT-Library, Auth-Konzepte
// Änderung des Auth-Systems → Domäne muss sich ändern

// ✅ GUT — Security als Querschnittskonzept (AOP + Spring Security)
@Service
public class OrderDomainService {
    // KEIN Security-Code hier — die Domäne ist vollständig naiv
    public void cancelOrder(OrderId id) {
        // Nur Business-Logik
    }
}

// Security-Aspekt (Querschnittskonzept durch AOP):
@RestController
public class OrderController {
    @DeleteMapping("/orders/{id}")
    @PreAuthorize("""
        hasRole('ADMIN') or
        @orderSecurityService.isOrderOwner(#id, authentication.name)
        """)
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(new OrderId(id));
        // Wenn @PreAuthorize schlägt fehl: AccessDeniedException → 403
        // Keine Security-Logik in der Domäne nötig
    }
}

@Service("orderSecurityService")
public class OrderSecurityService {
    // Security-Entscheidungslogik NUR hier — nicht in Domäne
    public boolean isOrderOwner(Long orderId, String username) {
        return orderRepository.existsByIdAndCustomerEmail(orderId, username);
    }
}
```

---

## 5. Querschnittskonzept: Transaktionsmanagement

### 5.1 Transaktionsgrenzen als Querschnittskonzept

```java
// ❌ SCHLECHT — Transaktionen zu weit ausgedehnt (oder fehlend)
@Service
public class OrderService {
    // Kein @Transactional → halb gespeicherte Daten bei Fehler!
    public void placeOrder(PlaceOrderCommand cmd) {
        orderRepo.save(newOrder);       // Commit A
        inventoryService.reserve(items);// Commit B — aber was wenn das hier fehlschlägt?
        // Order ist in DB, Bestand nicht reduziert → inkonsistenter Zustand
    }
}

// ❌ AUCH SCHLECHT — Transaktion zu weit, externe Calls eingeschlossen
@Transactional
public void placeOrder(PlaceOrderCommand cmd) {
    orderRepo.save(newOrder);
    stripeClient.charge(amount);        // Externe API in Transaktion!
    // Stripe-Call dauert 2000ms → DB-Verbindung blockiert für 2000ms
    // Bei 100 parallelen Requests: Connection-Pool erschöpft
}

// ✅ GUT — Transaktionsgrenzen präzise definiert
@Service
public class OrderDomainService {

    @Transactional    // ← Nur DB-Operationen
    public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
        var order = Order.place(cmd);
        orderRepository.save(order);
        // Event in Outbox speichern (transaktional!) → ADR-042
        outboxRepository.save(OrderPlacedOutboxEvent.from(order));
        return OrderCreatedResult.from(order);
        // Transaktion endet hier — Stripe wird ASYNCHRON über Outbox aufgerufen
    }
}
```

### 5.2 @Transactional Propagation-Typen (wann was)

```java
// Die wichtigsten Propagation-Typen erklärt:

@Transactional(propagation = REQUIRED)  // Standard
// → Nimmt vorhandene Transaktion oder startet neue
// → 90% aller Fälle

@Transactional(propagation = REQUIRES_NEW)
// → Immer neue Transaktion, pausiert vorhandene
// → Wann: Audit-Logging das auch bei Rollback erhalten bleiben soll
public void logAuditEvent(AuditEvent event) {
    auditRepo.save(event);
    // Auch wenn äußere Transaktion rolled back → dieser Log bleibt!
}

@Transactional(propagation = SUPPORTS)
// → Transaktion falls vorhanden, sonst ohne
// → Wann: reine Lesemethoden die sowohl mit als auch ohne Transaktion OK sind

@Transactional(readOnly = true)
// → Optimierungshinweis an DB: keine dirty-reads nötig
// → Hibernate deaktiviert dirty-checking → schneller
// → Immer für reine Lesemethoden verwenden!
public OrderDetailDto findById(OrderId id) { ... }
```

---

## 6. Querschnittskonzept: Caching als systemweite Strategie

```java
// ❌ SCHLECHT — Caching ad-hoc, inkonsistent
public class ProductService {
    private final Map<Long, Product> cache = new HashMap<>(); // lokaler In-Memory-Cache
    // → kein Expiry, kein Cluster-Support, kein Monitoring
}

public class UserService {
    // Kein Caching → jeder Aufruf geht zur DB
}

// ✅ GUT — einheitliche Caching-Strategie (→ ADR-024)
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        var config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(/* ... */)
            .serializeValuesWith(/* ... */);

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withCacheConfiguration("products", config.entryTtl(Duration.ofHours(1)))
            .withCacheConfiguration("userSessions", config.entryTtl(Duration.ofMinutes(30)))
            .build();
    }
}

@Service
public class ProductQueryService {
    @Cacheable(value = "products", key = "#id",
               unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductDto findById(ProductId id) {
        return productRepository.findById(id)
            .map(productMapper::toDto)
            .orElse(null);
    }

    @CacheEvict(value = "products", key = "#product.id")
    public void updateProduct(Product product) {
        productRepository.save(product);
        // Cache-Eintrag wird nach Save gelöscht → nächster Read holt frische Daten
    }
}
```

---

## 7. Querschnittskonzept: Idempotenz

### 7.1 Warum Idempotenz ein Querschnittskonzept ist

```
Idempotenz: Eine Operation kann mehrfach ausgeführt werden ohne
            unterschiedliche Ergebnisse zu produzieren.

WARUM JEDER SERVICE Idempotenz braucht:
  ① Netzwerkfehler → Client sendet Request erneut (Retry → ADR-022)
  ② Kafka verarbeitet Events mindestens einmal (→ ADR-041)
  ③ Outbox-Events werden bei Neustart erneut gefeuert (→ ADR-042)
  ④ Load Balancer leitet bei Timeout zur nächsten Instanz um
  
  Ohne Idempotenz: Bestellungen werden doppelt angelegt, Zahlungen zweifach gebucht.
```

```java
// ❌ SCHLECHT — keine Idempotenz-Behandlung
@PostMapping("/orders")
public OrderCreatedResponse placeOrder(@RequestBody PlaceOrderRequest req) {
    return orderService.create(req);
    // Client retried wegen Timeout → doppelte Bestellung!
}

// ✅ GUT — Idempotenz durch Idempotency-Key
@PostMapping("/orders")
public OrderCreatedResponse placeOrder(
        @RequestBody PlaceOrderRequest req,
        @RequestHeader("Idempotency-Key") String idempotencyKey) {

    // Bereits verarbeiteten Request zurückgeben
    return idempotencyStore.get(idempotencyKey)
        .map(cached -> (OrderCreatedResponse) cached)
        .orElseGet(() -> {
            var result = orderService.create(req);
            // Ergebnis speichern (24h TTL)
            idempotencyStore.save(idempotencyKey, result, Duration.ofHours(24));
            return result;
        });
}

// Für DB-Operationen: UNIQUE Constraint als Sicherheitsnetz
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
public class Order {
    @Column(name = "idempotency_key")
    private String idempotencyKey;
    // INSERT bei Duplikat → DB wirft ConstraintViolationException → 409 Conflict
}
```

---

## 8. Querschnittskonzept: Internationalisierung (i18n)

```java
// ❌ SCHLECHT — Texte hardcodiert
throw new DomainException("Order cannot be cancelled because it was already shipped");
// → Deutsch? Englisch? Übersetzung? Unmöglich.

// ✅ GUT — i18n durch MessageSource
public class OrderCannotBeCancelledException extends DomainException {
    private final OrderStatus currentStatus;

    @Override
    public String errorCode() { return "ORDER_CANNOT_BE_CANCELLED"; }

    // Kein Text in der Exception — nur Codes und Parameter
    public OrderStatus currentStatus() { return currentStatus; }
}

// messages.properties (Englisch, Standard)
# error.ORDER_CANNOT_BE_CANCELLED=Order cannot be cancelled (current status: {0})
# field.orders.quantity.positive=Quantity must be positive

// messages_de.properties (Deutsch)
# error.ORDER_CANNOT_BE_CANCELLED=Bestellung kann nicht storniert werden (aktueller Status: {0})

// Im GlobalExceptionHandler:
@ExceptionHandler(OrderCannotBeCancelledException.class)
public ProblemDetail handle(OrderCannotBeCancelledException ex,
                             Locale locale) {
    var msg = messageSource.getMessage(
        "error." + ex.errorCode(),
        new Object[]{ex.currentStatus()},
        locale  // aus Accept-Language Header
    );
    return ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, msg);
}
```

---

## 9. Querschnittskonzept: Konfigurationsmanagement

```java
// ❌ SCHLECHT — Konfiguration verstreut, teilweise hardcodiert
@Service
public class OrderService {
    private static final int MAX_ITEMS = 50;          // Hardcodiert!
    private static final String CURRENCY = "EUR";     // Hardcodiert!

    @Value("${stripe.key}")
    private String stripeKey;                          // Secret in Properties!
}

// ✅ GUT — typsichere, validierte Konfiguration (→ ADR-069)
@ConfigurationProperties(prefix = "order")
@Validated
public record OrderProperties(
    @Min(1) @Max(500)
    int maxItemsPerOrder,          // aus application.yml, validiert

    @NotNull
    Currency defaultCurrency,

    @Positive
    Duration sessionTimeout        // Duration direkt — kein String-Parsing
) {}

// application.yml
// order:
//   max-items-per-order: 50
//   default-currency: EUR
//   session-timeout: PT30M
//
// Secrets NICHT hier → Vault (→ ADR-069)
```

---

## 10. Querschnittskonzept: Correlation IDs & Distributed Tracing

```java
// Jeder eingehende Request bekommt eine Correlation-ID
// Diese ID reist durch alle Services und Logs

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Von außen mitgegebene ID übernehmen ODER neue erstellen
        var correlationId = Optional.ofNullable(req.getHeader(HEADER))
            .filter(id -> id.matches("[a-zA-Z0-9-]{8,64}"))  // Validierung!
            .orElse(UUID.randomUUID().toString());

        // In MDC für alle Logs dieses Threads
        MDC.put("correlationId", correlationId);
        MDC.put("requestPath", req.getRequestURI());
        MDC.put("requestMethod", req.getMethod());

        // An Response-Header anhängen (für Client-Debugging)
        res.setHeader(HEADER, correlationId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();  // ThreadLocal IMMER aufräumen!
        }
    }
}

// Beim Upstream-Call an andere Services weitergeben:
@Bean
public RestClient.Builder restClientBuilder() {
    return RestClient.builder()
        .requestInterceptor((req, body, execution) -> {
            // Correlation-ID an ausgehende Requests weitergeben
            req.getHeaders().set("X-Correlation-ID",
                MDC.get("correlationId"));
            return execution.execute(req, body);
        });
}
```

---

## 11. Architekturelle Umsetzung: Querschnittskonzepte modellieren

```
DREI MUSTER FÜR QUERSCHNITTSKONZEPTE:

MUSTER 1: Shared Library
  → Bibliothek die alle Services nutzen
  → Gut für: Logging-Setup, Exception-Typen, Common-DTOs
  → Problem: Abhängigkeits-Kopplung, Update-Koordination

MUSTER 2: Aspect-Oriented Programming (AOP)
  → Aspekte weben sich in bestehenden Code ein
  → Gut für: Logging, Security-Checks, Transaktionen
  → Problem: "Magie" die schwer zu debuggen ist

MUSTER 3: Service Mesh / Sidecar
  → Infrastruktur übernimmt Querschnittskonzepte
  → Gut für: mTLS, Retries, Tracing, Circuit Breaking (→ ADR-070 Istio)
  → Problem: Kubernetes-Abhängigkeit, Operational Overhead

UNSER ANSATZ (Kombination):
  Logging:           AOP + Structured-Logging-Library
  Security:          Spring Security (Framework-Integration)
  Fehlerbehandlung:  @RestControllerAdvice (AOP via Framework)
  Transaktionen:     Spring @Transactional (AOP via Framework)
  Caching:           Spring Cache + Redis (Framework + Infrastruktur)
  Tracing:           OpenTelemetry + Sidecar (Service Mesh Aspekt)
  Circuit Breaking:  Resilience4j (Library) + Istio (Service Mesh)
```

---

## 12. Validierung: Sind Querschnittskonzepte konsistent umgesetzt?

```bash
# Prüfen ob alle Services den zentralen Exception-Handler verwenden
grep -r "GlobalExceptionHandler" src/ --include="*.java" | wc -l
# Erwartet: genau 1 (in der Bootstrap-Konfiguration)

# Prüfen ob ResponseEntity direkt in Controllern benutzt wird (Anti-Pattern)
grep -r "ResponseEntity.status(500)" src/ --include="*.java"
# Erwartet: 0 Treffer (zentraler Handler übernimmt das)

# Prüfen ob System.out.println im Code existiert
grep -r "System.out.println" src/ --include="*.java"
# Erwartet: 0 Treffer
```

```java
// ArchUnit-Regeln für Querschnittskonzepte:
@ArchTest
static final ArchRule kein_system_out =
    noClasses().should()
        .callMethod(System.class, "out")
        .because("ADR-086: Nur SLF4J-Logger, kein System.out");

@ArchTest
static final ArchRule controller_kein_direkte_exception_behandlung =
    noMethods()
        .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
        .should().declareThrowableOfType(Exception.class)
        .because("ADR-086: GlobalExceptionHandler übernimmt alle Exceptions");
```

---

## 13. Quellen & Referenzen

- **iSAQB CPSA-F Curriculum (2023), LZ-5-1 bis LZ-5-7** — Querschnittskonzepte als Pflichtthema im Foundation Level.
- **Gregor Kiczales et al., "Aspect-Oriented Programming" (1997), ECOOP** — Originalarbeit zu AOP als Mechanismus für cross-cutting concerns.
- **Michael Nygard, "Release It!" (2018), 2. Auflage** — Timeouts, Circuit Breaker, Bulkheads als Produktions-Stabilitätsmuster.
- **RFC 9457: "Problem Details for HTTP APIs" (2023)** — Standard für strukturierte Fehlerantworten.
- **OpenTelemetry Specification (2023)** — Standard für Traces, Metrics, Logs.

---

## Akzeptanzkriterien

- [ ] Alle Services nutzen denselben strukturierten Log-Format (JSON in Produktion)
- [ ] Genau ein `@RestControllerAdvice` für das gesamte System — kein lokales Exception-Handling in Controllern
- [ ] Keine `System.out.println` im Produktionscode (ArchUnit-Test grün)
- [ ] Idempotency-Keys auf allen POST-Endpunkten für Zahlungen und Bestellungen
- [ ] Correlation-ID in allen Logs und HTTP-Responses
- [ ] `@Transactional(readOnly = true)` auf allen reinen Lesemethoden

---

## Verwandte ADRs

- [ADR-084](ADR-084-isaqb-entwurfsprinzipien.md) — Separation of Concerns als Basis
- [ADR-017](ADR-017-observability-logging-tracing.md) — Observability-Implementierung
- [ADR-015](ADR-015-sicherheit-owasp.md) — Security als Querschnittskonzept
- [ADR-024](ADR-024-caching-strategie.md) — Caching-Strategie
- [ADR-087](ADR-087-isaqb-architekturbewertung.md) — Architekturbewertung (iSAQB F6)
