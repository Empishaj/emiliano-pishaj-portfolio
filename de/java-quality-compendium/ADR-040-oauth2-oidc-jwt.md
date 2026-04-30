# ADR-040 — OAuth2, OIDC & JWT Best Practices

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Security 6.x · Spring Boot 3.x        |
| Datum      | 2024-01-01                                        |
| Kategorie  | Security / Authentication                         |

---

## Kontext & Problem

Selbstgebaute Authentifizierungssysteme sind die größte Sicherheitslücke. OAuth2 + OpenID Connect (OIDC) sind der Industriestandard. JWT ist das Token-Format. Alle drei werden häufig falsch eingesetzt: Tokens zu lang gültig, zu viel im JWT, kein Refresh-Token-Rotation, Key-Expiry vergessen.

---

## Grundmodell: OAuth2 Resource Server

```
Client (Browser/App)
  │
  ├─[1: Auth Request]──→ Authorization Server (Keycloak / Auth0 / Cognito)
  │                          │
  │◄─[2: Access Token + Refresh Token]────────────────────────────────────
  │
  ├─[3: API Request + Bearer Token]──→ Resource Server (unser Spring Boot Service)
                                           │
                                           ├─[4: Token validieren (JWK Set)]
                                           │     ↗ Authorization Server (JWK-Endpunkt)
                                           │
                                           └─[5: Response]──→ Client
```

---

## Spring Boot Resource Server Konfiguration

```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # JWK Set URI: öffentliche Keys vom Authorization Server
          jwk-set-uri: https://auth.example.com/realms/myapp/protocol/openid-connect/certs
          # Alternativ: issuer-uri lässt Spring die Konfiguration autodiscovern
          issuer-uri: https://auth.example.com/realms/myapp
```

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())          // Stateless: kein CSRF nötig
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");         // Claim-Name in JWT
        converter.setAuthorityPrefix("ROLE_");              // Spring Security Prefix

        var authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(converter);
        return authConverter;
    }
}
```

---

## JWT Best Practices

```java
// ❌ Schlecht: zu viel im JWT-Payload
// JWTs sind Base64-encoded — sichtbar für jeden der den Token hat!
{
  "sub": "user-123",
  "email": "max@example.com",
  "roles": ["ADMIN"],
  "address": { "street": "...", "city": "..." },   // PII im Token — falsch
  "creditCardLast4": "1234",                        // Sensitive Daten — falsch
  "permissions": ["READ_ALL", "WRITE_ALL", ...],    // 50 Permissions → Token zu groß
  "exp": 1893456000    // Jahr 2030 — viel zu lang gültig
}

// ✅ Gut: minimaler JWT-Payload
{
  "sub": "user-123",                // Subject: User-ID
  "iss": "https://auth.example.com", // Issuer
  "aud": "order-service",            // Audience: welcher Service
  "roles": ["ADMIN"],               // Nur Rollen
  "exp": 1700000900,                // Ablauf: 15 Minuten
  "iat": 1700000000,                // Ausgestellt: jetzt
  "jti": "a1b2c3d4"                // JWT ID: für Revocation
}
// Alles andere (Name, Email, Adresse): per API-Aufruf nachladen wenn gebraucht
```

---

## Token-Expiry und Refresh

```java
// ❌ Schlecht: langer Access-Token
// exp = 24h — kompromittierter Token gültig für 24 Stunden
// exp = keine Ablaufzeit — ewig gültig: katastrophal

// ✅ Gut: kurzer Access-Token + Refresh-Token-Rotation
// Access Token:  15 Minuten (kurzes Fenster bei Kompromittierung)
// Refresh Token: 7 Tage, aber: Rotation bei jedem Refresh

// Refresh Token Rotation: jedes Mal wenn der Refresh Token verwendet wird,
// wird ein neuer ausgestellt und der alte invalidiert.
// → Gestohlener Refresh Token wird erkannt wenn Original-Besitzer ihn nutzt.

// Keycloak Konfiguration:
// access-token-lifespan: 15m
// refresh-token-max-reuse: 0  (= Rotation aktiviert)
// sso-session-max-lifespan: 7d
```

---

## JWT validieren: alle Claims prüfen

```java
@Component
public class JwtValidator {

    private final JWKSet jwkSet; // Public Keys vom Auth-Server

    public Claims validate(String token) {
        var jwt = SignedJWT.parse(token);

        // ① Signatur prüfen
        var verifier = new RSASSAVerifier((RSAPublicKey) getPublicKey(jwt.getHeader().getKeyID()));
        if (!jwt.verify(verifier))
            throw new InvalidTokenException("Invalid signature");

        var claims = jwt.getJWTClaimsSet();

        // ② Ablauf prüfen
        if (claims.getExpirationTime().before(new Date()))
            throw new TokenExpiredException("Token expired");

        // ③ Issuer prüfen
        if (!"https://auth.example.com/realms/myapp".equals(claims.getIssuer()))
            throw new InvalidTokenException("Invalid issuer");

        // ④ Audience prüfen (verhindert Token-Missbrauch zwischen Services)
        if (!claims.getAudience().contains("order-service"))
            throw new InvalidTokenException("Token not intended for this service");

        return claims;
    }
}
// Spring Security macht das automatisch wenn richtig konfiguriert
// Diese Klasse: nur zur Illustration der Prüfschritte
```

---

## Methoden-Level-Autorisierung

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    // Nur ADMIN oder Eigentümer darf Bestellung sehen
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id, authentication)")
    public OrderDetailDto findById(@PathVariable Long id) { ... }

    // Nur ADMIN darf alle Bestellungen sehen
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSummaryDto> findAll(Pageable pageable) { ... }

    // User kann nur eigene Bestellungen aufgeben
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createOrder(
            @Valid @RequestBody CreateOrderCommand cmd,
            @AuthenticationPrincipal Jwt jwt) {
        // UserId aus JWT extrahieren — nie aus Request-Body
        var userId = new UserId(Long.parseLong(jwt.getSubject()));
        // ...
    }
}

@Component("orderSecurity")
public class OrderSecurityService {
    public boolean isOwner(Long orderId, Authentication auth) {
        var jwt = (Jwt) auth.getPrincipal();
        var userId = Long.parseLong(jwt.getSubject());
        return orderRepository.findById(orderId)
            .map(o -> o.userId().value().equals(userId))
            .orElse(false);
    }
}
```

---

## Konsequenzen

**Positiv:** Standard OAuth2/OIDC: kein selbstgebautes Auth-System. JWK-basierte Validierung: kein Secret teilen zwischen Services. Kurze Token-Lebensdauer + Rotation: kompromittierte Tokens schnell ungültig.

**Negativ:** JWK-Endpunkt muss erreichbar sein für Schlüssel-Rotation. Token-Revocation (vor Ablauf) bei Standard-JWT schwierig — Lösung: kurze Lebensdauer + Revocation-Liste.

---

## 💡 Guru-Tipps

- **Audience-Claim pflichtweise prüfen** (`aud`): verhindert Token-Leakage zwischen Services.
- **`@AuthenticationPrincipal Jwt`** in Controllern statt `SecurityContextHolder.getContext()` — typensicher und testbar.
- **Key-Rotation**: Authorization Server rotiert JWKs regelmäßig — Spring Security aktualisiert JWK-Set automatisch.
- **Nie JWT im LocalStorage** (XSS-Angriff). Bevorzugt: HttpOnly Cookie mit Secure-Flag.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP-Prinzipien für die gesamte Security-Schicht.
- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ArchUnit: Controller dürfen JWT direkt lesen.
