# ADR-062 — Rate Limiting & API Throttling

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Bucket4j   |
| Datum      | 2024-01-01                        |
| Kategorie  | Security / Performance            |

---

## Kontext & Problem

Ohne Rate Limiting kann ein einzelner Client — absichtlich oder durch Bug — die gesamte API zum Absturz bringen: Brute-Force auf Login-Endpunkte, DDoS durch Scraping, fehlerhafte Clients in Endlosschleife. Rate Limiting schützt die API und macht sie fair für alle Nutzer.

---

## Rate Limiting Strategien

```
Fixed Window:    100 Requests pro Minute — einfach, aber Burst am Fenster-Wechsel möglich
Sliding Window:  100 Requests in den letzten 60 Sekunden — glatter
Token Bucket:    Bucket mit N Tokens, pro Request 1 Token, Refill mit Rate R
                 → erlaubt kontrollierten Burst, glättet über Zeit
Leaky Bucket:    Request-Queue mit fester Ausgaberate — sehr glatt, keine Bursts

Empfehlung: Token Bucket (Bucket4j) — flexibel, gut verständlich
```

---

## Bucket4j: Token-Bucket-Implementierung

```kotlin
// build.gradle.kts
implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:8.7.0")
implementation("com.github.vladimir-bukhtoyarov:bucket4j-redis:8.7.0")
```

```java
// Rate Limit Konfiguration
@Configuration
public class RateLimitConfig {

    // In-Memory für Single-Instance (Development)
    @Bean
    @Profile("!production")
    public RateLimitService inMemoryRateLimitService() {
        return new InMemoryRateLimitService();
    }

    // Redis-backed für Multi-Instance (Production) → konsistent über alle Pods
    @Bean
    @Profile("production")
    public RateLimitService redisRateLimitService(RedisClient redisClient) {
        var proxyManager = Bucket4jRedis.casBasedBuilder(redisClient)
            .build();
        return new RedisRateLimitService(proxyManager);
    }
}

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Globales API-Limit: 100 Requests/Minute pro IP
    private Bucket getOrCreateBucket(String key, BandwidthLimit limit) {
        return buckets.computeIfAbsent(key, k ->
            Bucket.builder()
                .addLimit(limit)
                .build()
        );
    }

    public boolean tryConsume(String clientKey, BandwidthLimit limit) {
        return getOrCreateBucket(clientKey, limit).tryConsume(1);
    }

    public RateLimitInfo getRateLimitInfo(String clientKey, BandwidthLimit limit) {
        var bucket = getOrCreateBucket(clientKey, limit);
        var probe = bucket.estimateAbilityToConsume(1);
        return new RateLimitInfo(
            probe.isCanBeConsumed(),
            probe.getRemainingTokens(),
            probe.getNanosToWaitForRefill() / 1_000_000_000L  // Sekunden bis Refill
        );
    }
}
```

---

## Rate Limit Filter: verschiedene Limits per Endpunkt

```java
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    // Limits: anpassen nach Endpunkt-Sensitivität
    private static final BandwidthLimit STANDARD_LIMIT = Bandwidth.classic(
        100, Refill.greedy(100, Duration.ofMinutes(1))   // 100/Minute
    );

    private static final BandwidthLimit LOGIN_LIMIT = Bandwidth.classic(
        5, Refill.greedy(5, Duration.ofMinutes(1))       // 5/Minute — Brute-Force-Schutz
    );

    private static final BandwidthLimit SEARCH_LIMIT = Bandwidth.classic(
        30, Refill.greedy(30, Duration.ofMinutes(1))     // 30/Minute — teurer Endpunkt
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse res,
                                     FilterChain chain) throws IOException, ServletException {
        var limit = selectLimit(req.getRequestURI());
        var clientKey = extractClientKey(req);

        var info = rateLimitService.getRateLimitInfo(clientKey, limit);

        // Standard-Headers (RFC 6585 / IETF Draft)
        res.setHeader("X-RateLimit-Limit",     String.valueOf(limit.getCapacity()));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(info.remaining()));
        res.setHeader("X-RateLimit-Reset",     String.valueOf(info.secondsUntilRefill()));

        if (!info.canConsume()) {
            res.setStatus(429);  // Too Many Requests
            res.setHeader("Retry-After", String.valueOf(info.secondsUntilRefill()));
            res.setContentType("application/problem+json");
            res.getWriter().write("""
                {
                  "type": "https://example.com/problems/rate-limit-exceeded",
                  "title": "Too Many Requests",
                  "status": 429,
                  "detail": "Rate limit exceeded. Retry after %d seconds."
                }
                """.formatted(info.secondsUntilRefill()));
            return;
        }

        chain.doFilter(req, res);
    }

    private BandwidthLimit selectLimit(String uri) {
        if (uri.contains("/auth/login"))    return LOGIN_LIMIT;
        if (uri.contains("/search"))        return SEARCH_LIMIT;
        return STANDARD_LIMIT;
    }

    private String extractClientKey(HttpServletRequest req) {
        // Authentifizierte User: nach User-ID (besser als IP)
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt)
            return "user:" + jwt.getSubject();

        // Unauthentifizierte: nach IP (Proxy-Header beachten!)
        return "ip:" + getClientIp(req);
    }

    private String getClientIp(HttpServletRequest req) {
        // X-Forwarded-For wenn hinter Load Balancer/Proxy
        var forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0].trim();  // Erste IP = Original-Client
        return req.getRemoteAddr();
    }
}
```

---

## Deklaratives Rate Limiting mit Annotation

```java
// Eigene Annotation für Method-Level Rate Limiting
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int requests() default 100;
    int perSeconds() default 60;
    String key() default "global"; // "ip", "user", "global"
}

@Aspect
@Component
public class RateLimitAspect {

    @Around("@annotation(rateLimit)")
    public Object applyRateLimit(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        var key = buildKey(rateLimit, pjp);
        var limit = Bandwidth.classic(
            rateLimit.requests(),
            Refill.greedy(rateLimit.requests(), Duration.ofSeconds(rateLimit.perSeconds()))
        );

        if (!rateLimitService.tryConsume(key, limit))
            throw new RateLimitExceededException(rateLimit.requests(), rateLimit.perSeconds());

        return pjp.proceed();
    }
}

// Verwendung
@RestController
public class OrderController {

    @PostMapping("/orders")
    @RateLimit(requests = 10, perSeconds = 60, key = "user")  // 10 Orders/Minute/User
    public ResponseEntity<OrderCreatedResponse> createOrder(...) { ... }

    @GetMapping("/orders")
    @RateLimit(requests = 100, perSeconds = 60, key = "user")
    public Page<OrderSummaryDto> findAll(...) { ... }
}
```

---

## Spring Cloud Gateway: Rate Limiting am Gateway

```yaml
# application.yml — Spring Cloud Gateway mit Redis Rate Limiter
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10    # 10 Tokens/Sekunde
                redis-rate-limiter.burstCapacity: 20    # Max 20 gleichzeitig
                redis-rate-limiter.requestedTokens: 1
                key-resolver: "#{@userKeyResolver}"

        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 2     # 2/Sekunde — Login
                redis-rate-limiter.burstCapacity: 5
```

---

## Monitoring: Rate Limit Metriken

```java
// Micrometer: Rate-Limit-Verletzungen messen (→ ADR-017)
private final Counter rateLimitExceeded = Counter.builder("api.rate_limit.exceeded")
    .tag("endpoint", "orders")
    .register(meterRegistry);

// Alert wenn Rate-Limit-Verletzungen > 10/Minute:
// → Möglicher Angriff oder fehlerhafte Client-Implementierung
```

---

## Konsequenzen

**Positiv:** Schutz vor Brute-Force auf sensible Endpunkte. Fair-Use: kein einzelner Client monopolisiert die API. Standard-Headers (`X-RateLimit-*`) ermöglichen Clients intelligentes Backoff.

**Negativ:** Redis-Abhängigkeit für Multi-Instance-Deployment. Zu aggressives Limit frustriert legitime User. IP-basiertes Limiting kann bei NAT/Proxy viele User treffen.

---

## 💡 Guru-Tipps

- **Unterschiedliche Limits** für authenticated vs. anonymous: Auth-User verdienen höhere Limits.
- **`Retry-After`-Header**: RFC 7231 Standard — Clients können intelligent warten statt weiter hammern.
- **Exponential Backoff im Client**: wenn Rate Limit getroffen, nicht sofort retry.
- **Whitelist** für interne Services und Monitoring: Health-Check-Endpunkte vom Limit ausnehmen.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP: Rate Limiting als Schutz gegen Brute-Force.
- [ADR-022](ADR-022-resilience-circuit-breaker.md) — Bulkhead schützt intern, Rate Limit extern.
- [ADR-017](ADR-017-observability-logging-tracing.md) — Rate-Limit-Verletzungen monitoren.
