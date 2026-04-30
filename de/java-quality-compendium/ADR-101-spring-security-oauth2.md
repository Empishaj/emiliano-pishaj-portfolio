# ADR-101 — Spring Security: OAuth2 Resource Server vollständig konfiguriert

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / Security-Team                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Security · Spring Security · OAuth2                           |
| Betroffene Teams  | Alle Teams die HTTP-Endpunkte absichern                       |
| Abhängigkeiten    | ADR-015 (OWASP), ADR-040 (OAuth2/OIDC), ADR-086 (Security AOP)|

---

## 1. Kontext & Treiber

### 1.1 Das Problem ohne klare Spring-Security-Konfiguration

```
ADR-040 erklärt OAuth2-Konzepte: was JWT ist, was OIDC ist, wie Flows funktionieren.
Was fehlt: die konkrete Spring-Security-Implementierung.

IN DER PRAXIS entstehen ohne klare Vorgaben:
  Team A: @PreAuthorize("isAuthenticated()") überall
  Team B: HttpSecurity-Config pro Service verschieden
  Team C: JWT manuell geparst statt Spring Security genutzt
  Team D: Security komplett ausgeschaltet ("für Tests")
  
ERGEBNIS:
  → Jeder Service hat andere Security-Regeln
  → Kein einheitliches Fehlerverhalten (manche: 401, manche: 403, manche: 500)
  → Security-Reviews schwierig weil kein Standard
  → Fehler bei manueller JWT-Verarbeitung (keine Signatur-Prüfung!)
```

---

## 2. Entscheidung

Alle HTTP-Services nutzen Spring Security als OAuth2 Resource Server. JWT-Validierung erfolgt ausschließlich über Spring Security (niemals manuell). Autorisierung folgt dem Defense-in-Depth-Prinzip: mehrere Ebenen, keine einzelne Schwachstelle bricht alles.

---

## 3. Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    // Kein extra JWT-Library nötig — Spring Boot bringt Nimbus JOSE mit
}
```

---

## 4. Die vollständige Security-Konfiguration

### 4.1 HttpSecurity — der Kern

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // @PreAuthorize aktivieren
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // ── CSRF ────────────────────────────────────────────────────────
            // CSRF-Schutz für REST-APIs deaktivieren:
            // Warum? REST-APIs nutzen JWT (kein Session-Cookie) →
            //        CSRF-Angriff funktioniert nur bei Cookie-basierter Auth.
            // JWT im Authorization-Header ist CSRF-immun.
            .csrf(csrf -> csrf.disable())

            // ── SESSION ─────────────────────────────────────────────────────
            // Keine Server-Side-Session: jeder Request ist standalone mit JWT.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── CORS ─────────────────────────────────────────────────────────
            // CORS-Konfiguration (→ Details in ADR-117, hier nur Aktivierung)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── AUTORISIERUNG ────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Öffentliche Endpunkte — KEIN Token nötig:
                .requestMatchers(
                    "/actuator/health/**",    // Health-Checks (für K8s Probes!)
                    "/actuator/info",         // App-Infos (kein Secret)
                    "/v3/api-docs/**",        // OpenAPI-Dokumentation
                    "/swagger-ui/**"          // Swagger UI
                ).permitAll()

                // Prometheus-Metrics: nur aus K8s-Namespace erreichbar
                // (NetworkPolicy sorgt dafür — nicht durch Security!)
                .requestMatchers("/actuator/prometheus").permitAll()

                // Admin-Endpunkte: nur ADMIN-Rolle
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Alle anderen: authentifiziert
                .anyRequest().authenticated()
            )

            // ── OAUTH2 RESOURCE SERVER ───────────────────────────────────────
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                // Fehler-Handler: was passiert wenn Token fehlt/ungültig ist?
                .authenticationEntryPoint(problemDetailAuthEntryPoint())
                .accessDeniedHandler(problemDetailAccessDeniedHandler())
            )

            // ── SECURITY HEADERS ──────────────────────────────────────────────
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.disable()) // CSP macht das besser
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
            )

            .build();
    }

    // ── JWT DECODER ───────────────────────────────────────────────────────────
    @Bean
    public JwtDecoder jwtDecoder() {
        // Variante A: JWKS-URL (Public Keys von Keycloak/Auth-Server)
        // Spring Security holt automatisch die Public Keys und validiert die Signatur
        var decoder = NimbusJwtDecoder
            .withJwkSetUri("https://auth.example.com/realms/myapp/protocol/openid-connect/certs")
            .build();

        // Zusätzliche Validierungen ÜBER die Standard-Validierung hinaus:
        var validators = new ArrayList<>(JwtValidators.createDefaultWithIssuer(
            "https://auth.example.com/realms/myapp"  // issuer muss übereinstimmen!
        ).getValidators());

        // Custom: Audience prüfen (Token muss für UNSEREN Service ausgestellt sein)
        validators.add(new JwtClaimValidator<List<String>>(
            JwtClaimNames.AUD,
            aud -> aud != null && aud.contains("order-service")
        ));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }

    // Variante B: Symmetric Key (für Tests/Entwicklung)
    @Bean
    @Profile("test")
    public JwtDecoder testJwtDecoder() {
        // Test-Decoder mit festem Secret (NIEMALS in Produktion!)
        return NimbusJwtDecoder
            .withSecretKey(new SecretKeySpec(
                "test-secret-key-32-characters!!".getBytes(),
                "HmacSHA256"))
            .build();
    }

    // ── JWT → AUTHENTICATION CONVERTER ───────────────────────────────────────
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();

        // Keycloak-spezifisch: Rollen aus "realm_access.roles" extrahieren
        // (Standard Spring: "scope" oder "authorities")
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = new ArrayList<GrantedAuthority>();

            // Keycloak Realm-Rollen
            var realmAccess = (Map<String, Object>) jwt.getClaim("realm_access");
            if (realmAccess != null) {
                var roles = (List<String>) realmAccess.get("roles");
                if (roles != null) {
                    roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .forEach(authorities::add);
                }
            }

            // Scopes aus "scope"-Claim
            var scope = jwt.getClaimAsString("scope");
            if (scope != null) {
                Arrays.stream(scope.split(" "))
                    .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                    .forEach(authorities::add);
            }

            return authorities;
        });

        // Principal-Name: sub-Claim als Nutzer-ID
        converter.setPrincipalClaimName("sub");
        return converter;
    }

    // ── FEHLER-HANDLING: RFC 9457 Problem Details ─────────────────────────────
    @Bean
    public AuthenticationEntryPoint problemDetailAuthEntryPoint() {
        return (request, response, ex) -> {
            // 401: Token fehlt oder ist ungültig
            response.setStatus(401);
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                {
                  "type": "https://example.com/problems/unauthorized",
                  "title": "Unauthorized",
                  "status": 401,
                  "detail": "Authentication required"
                }
                """);
            // KEINE Details über warum der Token ungültig ist → kein Info-Leak!
        };
    }

    @Bean
    public AccessDeniedHandler problemDetailAccessDeniedHandler() {
        return (request, response, ex) -> {
            // 403: Token gültig, aber keine Berechtigung
            response.setStatus(403);
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                {
                  "type": "https://example.com/problems/forbidden",
                  "title": "Forbidden",
                  "status": 403,
                  "detail": "Insufficient permissions"
                }
                """);
        };
    }
}
```

### 4.2 application.yml Konfiguration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Issuer-URI: Spring Security lädt automatisch die JWKS-URL daraus
          issuer-uri: https://auth.example.com/realms/myapp
          # Alternativ: direkte JWKS-URL angeben
          # jwk-set-uri: https://auth.example.com/realms/myapp/protocol/openid-connect/certs
```

---

## 5. Method-Level Security: @PreAuthorize

### 5.1 Die Autorisierungs-Ebenen

```
DEFENSE IN DEPTH — mehrere Ebenen:

Ebene 1: HttpSecurity (Web-Layer)
  → Grobe Filterung: /admin/** nur für ADMIN
  → Vorteil: zentral, schnell, kein Code in Services

Ebene 2: @PreAuthorize (Method-Layer)
  → Feine Filterung: nur eigene Bestellungen sehen
  → Vorteil: nah an der Business-Logik, testbar

BEIDE zusammen: wenn Ebene 1 versagt, fängt Ebene 2 auf.
```

```java
@RestController
@RequestMapping("/api/v2/orders")
public class OrderController {

    // ① Einfache Rollen-Prüfung
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<OrderSummaryDto> listOrders(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        // Nur eigene Bestellungen — außer ADMIN sieht alle
        var userId = jwt.getSubject();
        if (jwt.getClaimAsStringList("roles").contains("ADMIN")) {
            return orderService.findAll(pageable);
        }
        return orderService.findByUser(userId, pageable);
    }

    // ② Eigene Ressource oder Admin
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOwner(#orderId, authentication)")
    public OrderDetailDto getOrder(@PathVariable String orderId) {
        return orderService.findById(orderId);
    }

    // ③ Scope-basierte Prüfung (für M2M / Service-Accounts)
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_orders:write')")
    public void cancelOrder(@PathVariable String orderId) {
        orderService.cancel(orderId);
    }

    // ④ Komplex: SpEL-Ausdruck mit eigenem Service
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("""
        hasRole('ADMIN') or (
            hasRole('SUPPORT') and
            @orderSecurityService.isAssignedTo(#orderId, authentication.name)
        )
        """)
    public void updateStatus(@PathVariable String orderId,
                              @RequestBody UpdateStatusRequest req) {
        orderService.updateStatus(orderId, req.newStatus());
    }
}

// Security-Service: Autorisierungs-Logik HIER (nicht in Domain!)
@Service("orderSecurityService")
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    public boolean isOwner(String orderId, Authentication auth) {
        var jwt = (Jwt) auth.getPrincipal();
        var userId = jwt.getSubject();
        return orderRepository.existsByIdAndCustomerId(orderId, userId);
    }

    public boolean isAssignedTo(String orderId, String username) {
        return orderRepository.findAssignedSupportAgent(orderId)
            .map(agent -> agent.username().equals(username))
            .orElse(false);
    }
}
```

---

## 6. JWT-Claims im Controller verwenden

```java
// Wie man an JWT-Daten kommt:

@PostMapping
public ResponseEntity<OrderCreatedResponse> createOrder(
        @Valid @RequestBody PlaceOrderRequest request,

        // Variante 1: Vollständiges JWT-Objekt
        @AuthenticationPrincipal Jwt jwt) {

    // Alle Standard-Claims:
    var userId     = jwt.getSubject();                      // "sub"
    var email      = jwt.getClaimAsString("email");
    var name       = jwt.getClaimAsString("name");
    var tenantId   = jwt.getClaimAsString("tenant_id");     // Custom Claim
    var roles      = jwt.getClaimAsStringList("realm_access.roles");
    var issuedAt   = jwt.getIssuedAt();                     // iat
    var expiresAt  = jwt.getExpiresAt();                    // exp

    var command = new PlaceOrderCommand(
        new UserId(userId),
        new TenantId(tenantId),
        request.items(),
        request.shippingAddress()
    );
    return ResponseEntity.status(201).body(orderService.place(command));
}

// Variante 2: Nur was man braucht
@GetMapping("/me")
public UserProfileDto getMyProfile(Authentication authentication) {
    var jwt = (Jwt) authentication.getPrincipal();
    return new UserProfileDto(jwt.getSubject(), jwt.getClaimAsString("email"));
}

// Variante 3: SecurityContextHolder (wenn kein Parameter möglich)
public String getCurrentUserId() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .filter(auth -> auth.getPrincipal() instanceof Jwt)
        .map(auth -> ((Jwt) auth.getPrincipal()).getSubject())
        .orElseThrow(() -> new IllegalStateException("No authenticated user"));
}
```

---

## 7. Security-Tests

```java
// Test-Helfer: JWT simulieren ohne echten Auth-Server

@AutoConfigureMockMvc
@SpringBootTest
class OrderControllerSecurityTest {

    @Autowired MockMvc mvc;

    // ① Ohne Token → 401
    @Test
    void ohneToken_gibt401() throws Exception {
        mvc.perform(get("/api/v2/orders"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType("application/problem+json"))
            .andExpect(jsonPath("$.status").value(401));
    }

    // ② Mit gültigem User-Token → 200
    @Test
    @WithMockUser(roles = "USER")    // Spring Security Test-Helper
    void mitUserToken_gibtEigeneBestellungen() throws Exception {
        mvc.perform(get("/api/v2/orders"))
            .andExpect(status().isOk());
    }

    // ③ Mit JWT (für Claims-Tests)
    @Test
    void mitJwt_verwendetSubAlsUserId() throws Exception {
        mvc.perform(get("/api/v2/orders/ord-123")
                .with(jwt()                           // Spring Security MockMvc JWT
                    .jwt(builder -> builder
                        .subject("user-42")
                        .claim("tenant_id", "tenant-abc")
                        .claim("realm_access",
                            Map.of("roles", List.of("USER"))))))
            .andExpect(status().isOk());
    }

    // ④ Falsche Rolle → 403
    @Test
    void adminEndpointMitUserRolle_gibt403() throws Exception {
        mvc.perform(delete("/admin/users/1")
                .with(jwt().jwt(b -> b.claim("realm_access",
                    Map.of("roles", List.of("USER"))))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }
}

// dependencies für Tests:
// testImplementation("org.springframework.security:spring-security-test")
```

---

## 8. Service-to-Service Kommunikation (M2M)

```java
// Wenn Service A Service B aufruft: kein User-Token, sondern Client-Credentials

@Configuration
public class ServiceTokenConfig {

    // OAuth2 Client Credentials Flow für ausgehende Service-Calls
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrations,
            OAuth2AuthorizedClientRepository authorizedClients) {

        var manager = new DefaultOAuth2AuthorizedClientManager(
            clientRegistrations, authorizedClients);

        manager.setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()    // Client Credentials Flow
                .build());

        return manager;
    }

    // RestClient mit automatischem Token-Management
    @Bean
    public RestClient inventoryClient(OAuth2AuthorizedClientManager clientManager) {
        var interceptor = new OAuth2ClientHttpRequestInterceptor(clientManager);
        interceptor.setClientRegistrationIdResolver(
            request -> "inventory-service");  // Registration aus application.yml

        return RestClient.builder()
            .baseUrl("https://inventory.example.com")
            .requestInterceptor(interceptor)  // Token automatisch hinzugefügt
            .build();
    }
}
```

```yaml
# application.yml: OAuth2 Client-Registrierung für M2M
spring:
  security:
    oauth2:
      client:
        registration:
          inventory-service:
            provider: keycloak
            client-id: order-service
            client-secret: ${INVENTORY_CLIENT_SECRET}  # Aus Vault
            authorization-grant-type: client_credentials
            scope: inventory:read
        provider:
          keycloak:
            issuer-uri: https://auth.example.com/realms/myapp
```

---

## 9. Akzeptanzkriterien

- [ ] Alle HTTP-Endpunkte sind abgesichert (keine unbewachten Routen außer explizit definiert)
- [ ] JWT wird ausschließlich durch Spring Security validiert (kein manuelles Parsen)
- [ ] Fehlerantworten sind RFC 9457 Problem-Details: 401 bei fehlendem Token, 403 bei fehlender Berechtigung
- [ ] Security-Tests für: ohne Token, falsches Token, falsche Rolle, eigene Ressource, fremde Ressource
- [ ] ArchUnit: kein manueller JWT-Parse-Code außerhalb von `SecurityConfig`

---

## Quellen & Referenzen

- **Spring Security Reference Documentation, "OAuth2 Resource Server"** — aktuelle Implementierungsreferenz.
- **RFC 6750, "OAuth 2.0 Bearer Token Usage"** — Authorization-Header-Standard.
- **RFC 7519, "JSON Web Token (JWT)"** — JWT-Spezifikation mit Claims.
- **OWASP, "REST Security Cheat Sheet"** — Stateless, Defense-in-Depth.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP als Sicherheits-Rahmen
- [ADR-040](ADR-040-oauth2-oidc-jwt.md) — OAuth2/OIDC-Konzepte
- [ADR-086](ADR-086-isaqb-querschnittskonzepte.md) — Security als Querschnittskonzept
