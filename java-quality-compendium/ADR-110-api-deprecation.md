# ADR-110 — API Deprecation & Breaking-Change-Strategie

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | API Design · Versionierung · Consumer-Schutz                  |
| Betroffene Teams  | Alle Teams die öffentliche oder interne APIs bereitstellen    |
| Abhängigkeiten    | ADR-021 (REST), ADR-064 (Versioning), ADR-066 (OpenAPI)       |

---

## 1. Kontext & Treiber

### 1.1 Das Problem ohne Deprecation-Strategie

```
SZENARIO: Orders-Team möchte /api/v1/orders umbenennen und
          das Antwortformat ändern.

OHNE STRATEGIE:
  Tag 1:  Orders-Team ändert einfach das Response-Format
  Tag 2:  Fulfillment-Service bricht (erwartet altes Format)
  Tag 2:  Mobile App bricht (erwartet altes Format)
  Tag 2:  3 externe Partner-Integrationen brechen
  Tag 3:  Incident, alle Teams debuggen gleichzeitig
  Woche 2: Alle Consumer haben mühsam migriert
  Kosten: 40-80 PT Debugging + Migration quer durch alle Teams

MIT STRATEGIE:
  Woche 1:  Neue API-Version /api/v2/orders bereitstellen
            Deprecation-Header auf v1 setzen
  Monat 1:  Alle Consumer benachrichtigt (Slack, E-Mail, Changelog)
  Monat 3:  Monitor: welche Consumer nutzen v1 noch?
  Monat 6:  Letzte Consumer migriert (messbar!)
  Monat 7:  v1 abschalten — null Überraschungen
  Kosten: 5 PT (neue Version bauen) + koordinierte Migration
```

### 1.2 Was ein Breaking Change ist

```
BREAKING CHANGE = jede Änderung die vorhandenen Consumer-Code
                  ohne Anpassung zum Fehlschlagen bringt.

BREAKING (erfordert neue Version):
  ❌ Endpunkt-URL ändern oder entfernen
  ❌ HTTP-Method ändern (POST → PUT)
  ❌ Pflichtfeld aus Request entfernen
  ❌ Pflichtfeld zur Response hinzufügen das Consumer lesen müssen
  ❌ Feld umbenennen
  ❌ Typ ändern (string → int)
  ❌ Enum-Werte entfernen
  ❌ Fehlercodes oder HTTP-Status ändern
  ❌ Authentifizierungsmethode ändern

NON-BREAKING (sicher, keine neue Version):
  ✅ Optionales Feld zur Response hinzufügen
  ✅ Neuen Endpunkt hinzufügen
  ✅ Neue optionale Query-Parameter hinzufügen
  ✅ Enum-Wert hinzufügen (wenn Consumer unbekannte Werte ignorieren)
  ✅ Performance-Verbesserungen
  ✅ Dokumentation verbessern
```

---

## 2. Entscheidung

Breaking Changes werden durch parallele Versionen und einen strukturierten
Sunset-Prozess gehandhabt. Alte Versionen werden mindestens **6 Monate** nach
Deprecation-Ankündigung aktiv gehalten. Nutzung wird laufend gemessen.

---

## 3. URL-Versionierungsstrategie

```java
// URL-Pfad-Versionierung (unsere Wahl)
// Vorteil: sichtbar, cachebar, einfach zu verstehen
// Nachteil: URL-Proliferation bei vielen Versionen

@RestController
@RequestMapping("/api/v2/orders")   // v2 = neue Hauptversion
public class OrderControllerV2 { }

@RestController
@RequestMapping("/api/v1/orders")   // v1 = deprecated, noch aktiv
@Deprecated
public class OrderControllerV1 { }
```

---

## 4. Deprecation-Header

```java
// Deprecated-Endpunkte geben Standard-HTTP-Header zurück
// RFC 8594: "Sunset" Header für geplante Abschaltung
// Ietf-Draft: "Deprecation" Header

@RestController
@RequestMapping("/api/v1/orders")
public class OrderControllerV1 {

    // Diese Konstante zentralisiert das Sunset-Datum — einmal ändern, überall wirkt
    private static final String SUNSET_DATE =
        "Sun, 01 Dec 2024 00:00:00 GMT";  // RFC 7231 Date-Format

    private static final String DEPRECATION_DATE =
        "Mon, 01 Jun 2024 00:00:00 GMT";

    @GetMapping
    public ResponseEntity<List<OrderDtoV1>> listOrders() {
        var orders = orderService.findAll();
        return ResponseEntity.ok()
            // Sunset: wann wird dieser Endpunkt abgeschaltet?
            .header("Sunset", SUNSET_DATE)
            // Deprecation: wann wurde er deprecated?
            .header("Deprecation", DEPRECATION_DATE)
            // Link zur neuen Version
            .header("Link", "</api/v2/orders>; rel=\"successor-version\"")
            // Deprecation-Hinweis im Body-Link (alternativ)
            .header("Link",
                "<https://docs.example.com/api/migration-v1-v2>; rel=\"deprecation\"")
            .body(orders.stream().map(orderMapperV1::toDto).toList());
    }
}

// Spring-Filter: Deprecation-Header für ALLE v1-Anfragen setzen
// (damit kein Endpunkt vergessen wird)
@Component
public class ApiDeprecationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse res,
                                     FilterChain chain)
            throws ServletException, IOException {

        chain.doFilter(req, res);

        // Nach der Verarbeitung: Header für v1-Anfragen setzen
        if (req.getRequestURI().startsWith("/api/v1/")) {
            res.setHeader("Sunset",      "Sun, 01 Dec 2024 00:00:00 GMT");
            res.setHeader("Deprecation", "Mon, 01 Jun 2024 00:00:00 GMT");
            res.setHeader("Link",        "</api/v2" +
                req.getRequestURI().substring(7) + ">; rel=\"successor-version\"");
        }
    }
}
```

---

## 5. Consumer-Nutzung messen

```java
// Wissen welche Consumer v1 noch nutzen → gezieltes Outreach

@Component
public class ApiVersionMetrics {

    private final MeterRegistry meterRegistry;

    @Bean
    public FilterRegistrationBean<ApiVersionTracker> versionTracker() {
        var bean = new FilterRegistrationBean<>(new ApiVersionTracker(meterRegistry));
        bean.addUrlPatterns("/api/*");
        return bean;
    }
}

@Component
public class ApiVersionTracker extends OncePerRequestFilter {

    private final MeterRegistry metrics;
    private final Counter v1Counter;

    public ApiVersionTracker(MeterRegistry metrics) {
        this.metrics   = metrics;
        this.v1Counter = Counter.builder("api.version.usage")
            .tag("version", "v1")
            .description("Requests to deprecated v1 API")
            .register(metrics);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse res,
                                     FilterChain chain)
            throws ServletException, IOException {

        if (req.getRequestURI().startsWith("/api/v1/")) {
            // Metrik: welche Version, welcher Client
            Counter.builder("api.version.usage")
                .tag("version", "v1")
                .tag("client", extractClientId(req))  // aus JWT oder API-Key
                .tag("endpoint", extractEndpoint(req))
                .register(metrics)
                .increment();
        }
        chain.doFilter(req, res);
    }

    private String extractClientId(HttpServletRequest req) {
        // Aus Authorization-Header oder X-Client-ID Header
        return Optional.ofNullable(req.getHeader("X-Client-ID"))
            .orElse("unknown");
    }
}
```

```yaml
# Prometheus-Alert: v1 wird noch aktiv genutzt (vor Sunset-Datum warnen)
- alert: DeprecatedApiStillUsed
  expr: |
    sum(rate(api_version_usage_total{version="v1"}[1h])) > 0
  for: 1h
  annotations:
    summary: "Deprecated API v1 wird noch verwendet"
    description: "{{ $value }} Requests/Stunde an v1. Sunset: 01 Dez 2024"
    runbook: "https://wiki.example.com/runbooks/api-migration"
```

---

## 6. Der Deprecation-Prozess (Timeline)

```
PHASE 1 — ANNOUNCE (Monat 0):
  □ Neue Version v2 bereitstellen (parallel zu v1)
  □ OpenAPI: v1-Endpunkte mit x-deprecated: true markieren
  □ Deprecation-Header auf v1 aktivieren
  □ Changelog-Eintrag mit Migration-Guide
  □ Alle bekannten Consumer benachrichtigen (Slack, E-Mail)
  □ Jira-Tickets für Consumer-Teams erstellen

PHASE 2 — MIGRATION (Monat 1-5):
  □ Wöchentliches Monitoring: wer nutzt v1 noch?
  □ Aktives Outreach bei Teams die nicht migriert haben
  □ Migration-Support anbieten (Office Hours)
  □ Nutzungs-Dashboard zeigt Fortschritt

PHASE 3 — SUNSET WARNING (Monat 5):
  □ 30 Tage vor Abschaltung: erneute Benachrichtigung aller Consumer
  □ v1-Nutzung muss Richtung 0 gehen (Metrik überwachen)
  □ Rote Linie: 0 Requests in den letzten 7 Tagen vor Abschaltung

PHASE 4 — SUNSET (Monat 6):
  □ v1-Endpunkte geben 410 Gone zurück (nicht 404!)
  □ Response enthält Link zu v2 und Migration-Doku
  □ Nach 3 weiteren Monaten: v1-Code entfernen
```

---

## 7. 410 Gone statt Einfach-Löschen

```java
// NICHT: Endpunkt einfach entfernen → 404 (verwirrend für Consumer)
// SONDERN: 410 Gone mit hilfreicher Fehlermeldung

@RestController
@RequestMapping("/api/v1/orders")
public class OrderControllerV1Sunset {

    // Nach Sunset-Datum: nur noch 410 zurückgeben
    @RequestMapping("/**")
    public ResponseEntity<ProblemDetail> sunset() {
        var problem = ProblemDetail.forStatus(HttpStatus.GONE);
        problem.setTitle("API Version 1 has been discontinued");
        problem.setDetail(
            "This API version was deprecated on 2024-06-01 and " +
            "discontinued on 2024-12-01. Please migrate to v2.");
        problem.setType(URI.create("https://docs.example.com/api/v2"));
        problem.setProperty("migrationGuide",
            "https://docs.example.com/api/migration-v1-v2");
        problem.setProperty("newEndpoint", "/api/v2/orders");

        return ResponseEntity.status(HttpStatus.GONE)
            .header("Link",
                "</api/v2/orders>; rel=\"successor-version\"")
            .body(problem);
    }
}
```

---

## 8. OpenAPI-Dokumentation für Deprecation

```yaml
# openapi.yaml — deprecated Endpunkte dokumentieren
paths:
  /api/v1/orders:
    get:
      summary: List orders (DEPRECATED)
      deprecated: true           # ← OpenAPI standard: zeigt "Deprecated" Badge
      description: |
        **⚠️ DEPRECATED** — Diese Version wird am 01.12.2024 abgeschaltet.
        Bitte zu `/api/v2/orders` migrieren.
        Migration-Guide: https://docs.example.com/api/migration-v1-v2
      x-sunset: "2024-12-01"    # Custom Extension: Sunset-Datum maschinell lesbar
      x-migration-guide: "https://docs.example.com/api/migration-v1-v2"
```

---

## 9. Akzeptanzkriterien

- [ ] Alle v1-Endpunkte geben `Sunset` und `Deprecation` Header zurück
- [ ] Prometheus-Metrik `api.version.usage{version="v1"}` ist vorhanden
- [ ] Alert bei v1-Nutzung kurz vor Sunset-Datum aktiv
- [ ] Nach Sunset: alle v1-Endpunkte geben `410 Gone` mit Migration-Link
- [ ] Migration-Guide in Confluence/Backstage veröffentlicht
- [ ] OpenAPI: alle deprecated Endpunkte mit `deprecated: true` markiert

---

## Quellen & Referenzen

- **RFC 8594, "The Sunset HTTP Header Field" (2019)** — Standard für API-Abschaltungs-Ankündigung.
- **Ietf Draft, "Deprecation Header" (2021)** — Ergänzender Header zu Sunset.
- **Stripe API Versioning** — Best Practice für API-Versionierung und Rückwärtskompatibilität.
- **Phil Sturgeon, "Build APIs You Won't Hate" (2014)** — Breaking Changes und Versionierungsstrategien.

---

## Verwandte ADRs

- [ADR-021](ADR-021-rest-api-design.md) — REST-Grundprinzipien
- [ADR-066](ADR-066-api-first-openapi.md) — OpenAPI-Dokumentation
- [ADR-064](ADR-064-changelog-versionierung.md) — Semantische Versionierung
