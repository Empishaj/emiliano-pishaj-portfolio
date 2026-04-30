# ADR-021 — REST API Design & Versionierung

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Boot 3.x                             |
| Datum      | 2024-01-01                                        |
| Kategorie  | API Design                                        |

---

## Kontext & Problem

Eine REST-API ist ein öffentlicher Vertrag. Schlechtes API-Design ist schwer zu versionieren, schwer zu dokumentieren und erzeugt Reibung bei jedem Consumer. Konsistenz ist das wichtigste Qualitätsmerkmal einer API — ein Entwickler der einen Endpoint kennt, soll den nächsten erraten können.

---

## Regel 1 — URL-Konventionen: Ressourcen, keine Aktionen

### ❌ Schlecht — RPC-Style mit Verben in URLs

```
POST /api/createOrder
GET  /api/getOrderById?id=1
POST /api/cancelOrder?orderId=1
POST /api/updateUserEmail
GET  /api/getAllActiveUsers
POST /api/doPayment
```

### ✅ Gut — REST-Ressourcen mit HTTP-Verben

```
POST   /api/v1/orders                    → Bestellung erstellen
GET    /api/v1/orders/{id}               → Bestellung abrufen
GET    /api/v1/orders?status=PENDING     → Bestellungen filtern
PATCH  /api/v1/orders/{id}               → Bestellung teilweise aktualisieren
DELETE /api/v1/orders/{id}               → Bestellung löschen (soft delete)

POST   /api/v1/orders/{id}/cancellations → Stornierung als Sub-Ressource
POST   /api/v1/orders/{id}/payments      → Zahlung als Sub-Ressource

GET    /api/v1/users/{id}/orders         → Bestellungen eines Users (Relation)
```

---

## Regel 2 — HTTP-Status-Codes: semantisch korrekt

### ❌ Schlecht — alles 200, Fehler im Body

```java
// Anti-Pattern: HTTP 200 für alles, Fehler im Body versteckt
@PostMapping("/orders")
public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest req) {
    if (req.productId() == null) {
        return ResponseEntity.ok(Map.of("success", false, "error", "productId required"));
    }
    var order = orderService.create(req);
    return ResponseEntity.ok(Map.of("success", true, "data", order));
}
```

### ✅ Gut — HTTP-Status-Codes als primäres Fehler-Signal

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    // 201 Created + Location Header für neue Ressourcen
    @PostMapping
    public ResponseEntity<OrderCreatedResponse> createOrder(
            @Valid @RequestBody CreateOrderCommand command) {
        var created = orderService.create(command);
        var location = URI.create("/api/v1/orders/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // 200 OK für erfolgreiche Abfragen
    @GetMapping("/{id}")
    public OrderDetailResponse findById(@PathVariable Long id) {
        return orderService.findById(id); // 404 kommt aus GlobalExceptionHandler
    }

    // 204 No Content für erfolgreiche Aktionen ohne Rückgabe
    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        orderService.cancel(id);
    }

    // 202 Accepted für asynchrone Operationen
    @PostMapping("/{id}/shipments")
    @ResponseStatus(ACCEPTED)
    public void initiateShipment(@PathVariable Long id) {
        shippingService.initiateAsync(id); // Asynchron — kein sofortiges Ergebnis
    }
}
```

```java
// Globaler Exception-Handler mit RFC 9457 Problem Details (Spring 6 nativ!)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleNotFound(OrderNotFoundException ex, HttpServletRequest req) {
        var problem = ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
        problem.setTitle("Order Not Found");
        problem.setInstance(URI.create(req.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    public ProblemDetail handleConflict(OrderCannotBeCancelledException ex) {
        var problem = ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
        problem.setTitle("Order State Conflict");
        problem.setProperty("currentStatus", ex.currentStatus());
        return problem;
    }

    // 400 für Validation-Fehler (→ ADR-015)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", ex.getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
            .toList());
        return problem;
    }
}
```

---

## Regel 3 — Versionierung: URL-Pfad ist der Standard

```java
// Option A (bevorzugt): Versionsnummer im URL-Pfad — sichtbar, cachebar, einfach
@RequestMapping("/api/v1/orders")
public class OrderControllerV1 { ... }

@RequestMapping("/api/v2/orders")
public class OrderControllerV2 { ... } // Neue Version bei breaking changes

// Option B: Accept-Header-Versionierung
@GetMapping(value = "/orders/{id}", produces = "application/vnd.company.order.v2+json")
public OrderDetailV2Response findById(@PathVariable Long id) { ... }

// ❌ Nicht: Query-Parameter-Versionierung
// GET /api/orders/1?version=2  → schwer zu cachen, unkonventionell
```

### Was ist ein Breaking Change?

```
Breaking (neue Version nötig):     Non-Breaking (keine neue Version nötig):
- Feld entfernen                   - Neues optionales Feld hinzufügen
- Feld umbenennen                  - Neuen optionalen Endpoint hinzufügen
- Typ ändern (String → Integer)    - Fehlermeldung präzisieren
- HTTP-Methode ändern              - Performance-Verbesserungen
- Pflichtfeld hinzufügen           - Neue HTTP-Header
- Status-Code ändern
```

---

## Regel 4 — Pagination, Filtering & Sorting

```java
// Einheitliches Pagination-Interface für alle Listen-Endpunkte
@GetMapping
public Page<OrderSummaryResponse> findAll(
        @RequestParam(defaultValue = "0")   int page,
        @RequestParam(defaultValue = "20")  int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort,
        @RequestParam(required = false)     OrderStatus status,
        @RequestParam(required = false)     Instant from,
        @RequestParam(required = false)     Instant to) {

    var pageable = PageRequest.of(page, Math.min(size, 100), // Max 100 pro Seite
        Sort.by(parseSortParam(sort)));

    return orderService.findAll(status, from, to, pageable)
        .map(orderMapper::toSummary);
}
```

```json
// Response: konsistente Pagination-Struktur
{
  "content": [ ... ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 142,
    "totalPages": 8
  },
  "_links": {
    "self":  { "href": "/api/v1/orders?page=0&size=20" },
    "next":  { "href": "/api/v1/orders?page=1&size=20" },
    "last":  { "href": "/api/v1/orders?page=7&size=20" }
  }
}
```

---

## Regel 5 — Idempotenz bei kritischen Operationen

```java
// Idempotency Key für Zahlungen — verhindert Doppelabbuchung bei Retry
@PostMapping("/{id}/payments")
public ResponseEntity<PaymentResponse> pay(
        @PathVariable Long id,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody PaymentRequest request) {

    // Bereits verarbeiteter Request? Gecachtes Ergebnis zurückgeben
    return idempotencyService.getOrProcess(idempotencyKey,
        () -> paymentService.process(id, request));
}
```

---

## Konsequenzen

**Positiv:** Konsistente URLs, Status-Codes und Error-Responses machen die API für Consumer vorhersagbar. RFC 9457 Problem Details sind ein Standard — Consumer können maschinell verarbeiten.

**Negativ:** Versionierungsstrategie muss früh festgelegt werden — nachträgliche Einführung ist aufwändig. Mehrere aktive API-Versionen erhöhen den Wartungsaufwand.

---

## 💡 Guru-Tipps

- **OpenAPI/Swagger** mit `springdoc-openapi` — automatisch generiert, immer aktuell.
- **Immer `application/json` und `application/problem+json`** im `produces`/`consumes` angeben.
- **HATEOAS** (Spring HATEOAS) für Hypermedia-Links wenn Consumer von URL-Struktur entkoppelt werden sollen.
- **Keine sensitiven Daten in URLs** (Passwörter, Tokens) — landen in Logs und Browser-History.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — Security auf API-Ebene.
- [ADR-019](ADR-019-contract-testing.md) — Contract Tests sichern API-Kompatibilität.
- [ADR-020](ADR-020-springboottest-slice-tests.md) — `@WebMvcTest` für Controller-Tests.
