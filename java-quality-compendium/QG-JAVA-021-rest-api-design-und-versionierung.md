# QG-JAVA-021 — REST API Design und Versionierung

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-021 |
| Titel | REST API Design und Versionierung |
| Status | Accepted / verbindlicher Standard für neue und geänderte HTTP-APIs |
| Sprache | Deutsch |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Framework 6.x |
| Kategorie | API Design, Schnittstellenqualität, Security, SaaS-Plattformen |
| Zielgruppe | Java-Entwickler, Backend-Entwickler, Tech Leads, API-Reviewer, QA, Security, Architektur, Product Owner mit API-Verantwortung |
| Verbindlichkeit | Diese Richtlinie gilt für alle neuen REST-/HTTP-APIs und für Breaking Changes an bestehenden APIs. Abweichungen müssen im Pull Request fachlich begründet und im API-Review akzeptiert werden. |
| Prüfstatus | Fachlich validiert gegen RFC 9110, RFC 9457, Spring Framework `ProblemDetail`, OpenAPI 3.1, OWASP API Security 2023 und Spring Data Pagination-Konzepte. |
| Letzte fachliche Prüfung | 2025-12-03 |
| Review-Zyklus | Mindestens jährlich oder bei größeren Änderungen an Spring Boot, OpenAPI, Security-Vorgaben oder API-Governance. |

---

## 1. Zweck

Eine REST-API ist ein technischer Vertrag zwischen Provider und Consumer. Sie ist nicht nur eine Sammlung von Controller-Methoden, sondern ein dauerhaftes Integrationsversprechen. Eine schlecht gestaltete API erzeugt Reibung bei jedem Consumer, erschwert Versionierung, macht Fehlerbehandlung uneinheitlich, schwächt Security-Reviews und erhöht die Kosten jeder Änderung.

Diese Richtlinie definiert verbindliche Regeln für REST-/HTTP-APIs in Java- und Spring-Boot-Systemen. Ziel ist eine API-Landschaft, die konsistent, sicher, dokumentierbar, testbar, versionierbar und für Consumer vorhersagbar ist.

Eine gute API hat vier Eigenschaften. Erstens ist sie **ressourcenorientiert**, nicht methodenorientiert. Zweitens nutzt sie **HTTP-Semantik** korrekt. Drittens trennt sie **interne Domänenmodelle** von externen Verträgen. Viertens macht sie **Breaking Changes** sichtbar, planbar und testbar.

---

## 2. Kurzregel

REST-APIs werden als stabile Ressourcenverträge modelliert. URLs beschreiben Ressourcen, HTTP-Methoden beschreiben Operationen, Status-Codes beschreiben Ergebnisarten, Fehlerantworten folgen RFC 9457 Problem Details, Listenendpunkte haben begrenzte Pagination, kritische Schreiboperationen sind idempotent abgesichert, und Breaking Changes erfordern eine neue API-Version.

Controller dürfen keine JPA-Entities exponieren, keine ungeprüften Request-Objekte direkt auf Entities binden, keine sensitiven Daten in URLs transportieren und keine fachlichen Fehler als HTTP `200 OK` mit Fehlerbody verstecken.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- externe REST-/HTTP-APIs,
- interne Service-to-Service-HTTP-APIs,
- Spring-Boot-Controller,
- API-Gateways und Backend-for-Frontend-Schnittstellen,
- JSON-basierte Request-/Response-Verträge,
- OpenAPI-Dokumentation,
- API-Versionierung,
- API-Fehlerbehandlung,
- Pagination, Filtering und Sorting,
- idempotente Schreiboperationen,
- SaaS- und mandantenfähige API-Endpunkte.

Diese Richtlinie gilt nicht unmittelbar für:

- GraphQL-Schemas,
- gRPC-/Protobuf-Schnittstellen,
- Kafka-/Event-Verträge,
- interne Java-Methodensignaturen,
- Batch-Dateiformate,
- WebSocket-Protokolle,
- reine HTML-/Server-Side-Rendered-Endpunkte.

Die Prinzipien zu Vertrag, Versionierung, Fehlersemantik und Security gelten jedoch sinngemäß auch dort.

---

## 4. Technischer Hintergrund

HTTP definiert Methoden, Status-Codes, Header und Semantik. RFC 9110 beschreibt unter anderem die Bedeutung von Status-Codes wie `200 OK`, `201 Created`, `202 Accepted` und `204 No Content`. Diese Semantik muss von APIs genutzt werden, damit Consumer, Proxies, Gateways, Monitoring, Caches und Tests korrekt interpretieren können, was passiert ist.

RFC 9457 definiert Problem Details for HTTP APIs. Das Format liefert maschinenlesbare Fehlerantworten und ersetzt RFC 7807. Spring Framework unterstützt dieses Modell über `ProblemDetail` und verwandte Fehlerabstraktionen.

OpenAPI 3.1 ist der Standard zur maschinenlesbaren Beschreibung von HTTP-APIs. Eine API ist erst dann wirklich produktionsreif, wenn ihre Requests, Responses, Fehlerfälle, Status-Codes, Sicherheitsanforderungen und Versionen dokumentiert und testbar sind.

OWASP API Security 2023 zeigt, dass APIs besonders anfällig für fehlerhafte Objektberechtigungen, übermäßige Datenfreigabe, fehlende Ressourcenbegrenzung, unsichere Business-Flows und fehlerhafte Autorisierung sind. REST-Design ist deshalb immer auch Security-Design.

---

## 5. Verbindlicher Standard

Für neue APIs gelten folgende verbindliche Grundregeln:

1. URLs beschreiben Ressourcen, keine Aktionen.
2. HTTP-Methoden werden semantisch korrekt verwendet.
3. Fehler werden nicht in `200 OK` versteckt.
4. Fehlerantworten folgen RFC 9457 Problem Details.
5. API-Verträge verwenden Request-/Response-DTOs, keine JPA-Entities.
6. Listenendpunkte haben Pagination mit maximaler Seitengröße.
7. Sorting- und Filterparameter werden allowlist-basiert validiert.
8. Breaking Changes führen zu einer neuen API-Version.
9. Kritische `POST`-/`PATCH`-Operationen unterstützen Idempotenz, wenn Wiederholungen realistisch sind.
10. Sensitive Daten dürfen nicht in URLs, Query-Parametern oder Log-freundlichen Headern transportiert werden.
11. Jeder Endpunkt muss Authentifizierung, Autorisierung und Tenant-Kontext explizit berücksichtigen.
12. Jede produktive API wird über OpenAPI dokumentiert und über Controller-, Contract- oder Integrationstests abgesichert.

---

## 6. URL-Konventionen: Ressourcen statt Aktionen

### 6.1 Schlechtes Beispiel: RPC-Stil in URLs

```http
POST /api/createOrder
GET  /api/getOrderById?id=1
POST /api/cancelOrder?orderId=1
POST /api/updateUserEmail
GET  /api/getAllActiveUsers
POST /api/doPayment
```

Diese URLs sind problematisch, weil sie Operationen in Pfadnamen kodieren. Das widerspricht der Ressourcensemantik und führt zu uneinheitlichen APIs. Consumer müssen jede URL auswendig lernen, statt Muster wiederzuerkennen.

### 6.2 Gutes Beispiel: Ressourcenorientierte URLs

```http
POST   /api/v1/orders
GET    /api/v1/orders/{orderId}
GET    /api/v1/orders?status=PENDING
PATCH  /api/v1/orders/{orderId}
DELETE /api/v1/orders/{orderId}

POST   /api/v1/orders/{orderId}/cancellations
POST   /api/v1/orders/{orderId}/payments
GET    /api/v1/users/{userId}/orders
```

Die URL benennt die Ressource. Die HTTP-Methode beschreibt, was mit der Ressource geschieht.

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Ressource | Substantiv, fachliches Objekt oder Sammlung | `/orders`, `/users/{id}/orders` | Verwenden |
| Aktion | Verb im Pfad | `/createOrder`, `/doPayment` | Vermeiden |
| Sub-Ressource | Fachliches Ereignis oder abhängige Ressource | `/orders/{id}/payments` | Verwenden |
| Query-Parameter | Filter, Suche, Pagination, Sorting | `?status=PENDING&page=0&size=20` | Verwenden |
| Sensitives Datum | Passwort, Token, E-Mail, IBAN | `?email=max@example.com` | Nicht verwenden |

---

## 7. HTTP-Methoden korrekt verwenden

| Methode | Bedeutung | Typische Verwendung | Wichtige Eigenschaft |
|---|---|---|---|
| `GET` | Ressource lesen | `GET /orders/{id}` | Safe, darf keinen Zustand ändern |
| `POST` | Neue Ressource oder nicht-idempotente Operation | `POST /orders`, `POST /payments` | Nicht automatisch idempotent |
| `PUT` | Ressource vollständig ersetzen | `PUT /orders/{id}` | Idempotent bei gleicher Payload |
| `PATCH` | Ressource teilweise ändern | `PATCH /orders/{id}` | Kann idempotent gestaltet werden, muss aber bewusst geprüft werden |
| `DELETE` | Ressource löschen oder deaktivieren | `DELETE /orders/{id}` | Idempotent gestalten |

### 7.1 Schlechtes Beispiel: Operation als `GET`

```http
GET /api/v1/orders/42/cancel
```

Das ist falsch, weil `GET` safe sein muss. Ein `GET` darf keine Stornierung auslösen. Browser, Caches, Crawler oder Monitoring-Systeme können `GET`-URLs automatisch abrufen.

### 7.2 Gutes Beispiel: fachliche Sub-Ressource

```http
POST /api/v1/orders/42/cancellations
```

Die Stornierung wird als Ressource oder Ereignis modelliert. Dadurch bleibt die HTTP-Semantik korrekt.

---

## 8. HTTP-Status-Codes

### 8.1 Grundregel

HTTP-Status-Codes sind das primäre maschinenlesbare Ergebnis-Signal. Der Body ergänzt Details, ersetzt aber nicht den Status-Code.

### 8.2 Schlechtes Beispiel: alles `200 OK`

```java
@PostMapping("/orders")
public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
    if (request.productId() == null) {
        return ResponseEntity.ok(Map.of(
                "success", false,
                "error", "productId required"
        ));
    }

    var order = orderService.create(request);
    return ResponseEntity.ok(Map.of(
            "success", true,
            "data", order
    ));
}
```

Das ist schlecht, weil Consumer den Fehler nicht über HTTP erkennen können. Monitoring, API-Gateways, Retries und Clients sehen eine erfolgreiche Antwort.

### 8.3 Gutes Beispiel: Status-Codes semantisch korrekt

```java
@RestController
@RequestMapping("/api/v1/orders")
class OrderController {

    private final OrderApplicationService orderService;

    OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrderCreatedResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        var created = orderService.create(request);
        var location = URI.create("/api/v1/orders/" + created.id());

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping(value = "/{orderId}", produces = "application/json")
    public OrderDetailResponse findById(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long orderId) {
        orderService.delete(orderId);
    }

    @PostMapping("/{orderId}/shipments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void initiateShipment(@PathVariable Long orderId) {
        orderService.initiateShipment(orderId);
    }
}
```

### 8.4 Status-Code-Matrix

| Status | Verwendung | Beispiel | Hinweise |
|---|---|---|---|
| `200 OK` | Erfolgreiche Abfrage oder Operation mit Response-Body | `GET /orders/42` | Standard für erfolgreiche Reads |
| `201 Created` | Neue Ressource erzeugt | `POST /orders` | Mit `Location`-Header |
| `202 Accepted` | Auftrag angenommen, Verarbeitung läuft asynchron | `POST /shipments` | Ergebnis später abrufbar machen |
| `204 No Content` | Erfolgreich, aber kein Body | `DELETE /orders/42` | Kein Response-Body |
| `400 Bad Request` | Syntaktisch oder formal ungültiger Request | ungültiges JSON, Bean Validation | Client muss Request korrigieren |
| `401 Unauthorized` | Nicht authentifiziert | fehlendes/ungültiges Token | Name ist historisch; meint Authentifizierung |
| `403 Forbidden` | Authentifiziert, aber nicht berechtigt | falsche Rolle, falscher Tenant | Berechtigungsproblem |
| `404 Not Found` | Ressource nicht gefunden oder bewusst verborgen | unbekannte ID | Bei BOLA manchmal bewusst statt 403 |
| `409 Conflict` | Fachlicher Zustandskonflikt | Bestellung bereits ausgeliefert | Nicht mit Validation vermischen |
| `412 Precondition Failed` | Bedingung wie ETag passt nicht | Optimistic Locking | Für konkurrierende Updates |
| `422 Unprocessable Content` | Fachlich nicht verarbeitbarer Inhalt | semantisch ungültiger Payload | Nur verwenden, wenn im Team standardisiert |
| `429 Too Many Requests` | Rate Limit überschritten | API-Missbrauch, Tenant-Limit | Mit `Retry-After`, wenn möglich |
| `500 Internal Server Error` | Unerwarteter Fehler | Bug, nicht behandelter Fehler | Keine internen Details ausgeben |
| `503 Service Unavailable` | Temporär nicht verfügbar | Downstream nicht erreichbar | Mit Retry-Semantik prüfen |

---

## 9. Fehlerantworten mit RFC 9457 Problem Details

### 9.1 Grundregel

Fehlerantworten verwenden `application/problem+json` und folgen RFC 9457. Sie enthalten mindestens:

- `type`,
- `title`,
- `status`,
- `detail`,
- `instance`.

Zusätzliche Felder sind erlaubt, müssen aber stabil, dokumentiert und frei von sensiblen Daten sein.

### 9.2 Globaler Exception Handler

```java
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(
            OrderNotFoundException exception,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Order was not found."
        );
        problem.setTitle("Order Not Found");
        problem.setType(URI.create("https://api.example.com/problems/order-not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("orderId", exception.orderId());

        return problem;
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    public ProblemDetail handleOrderCannotBeCancelled(
            OrderCannotBeCancelledException exception,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Order cannot be cancelled in its current state."
        );
        problem.setTitle("Order State Conflict");
        problem.setType(URI.create("https://api.example.com/problems/order-state-conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("currentStatus", exception.currentStatus().name());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        var errors = exception.getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", safeValidationMessage(error.getDefaultMessage())
                ))
                .toList();

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setDetail("Request validation failed.");
        problem.setType(URI.create("https://api.example.com/problems/validation-failed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", errors);

        return problem;
    }

    private String safeValidationMessage(String message) {
        return message == null ? "Invalid value." : message;
    }
}
```

### 9.3 Schlechtes Beispiel: interne Details in Fehlerantworten

```json
{
  "error": "SQLIntegrityConstraintViolationException",
  "message": "Duplicate entry 'max@example.com' for key users.email_unique",
  "trace": "org.hibernate.exception.ConstraintViolationException..."
}
```

Das ist gefährlich, weil interne Implementierungsdetails, SQL-Strukturen und personenbezogene Daten offengelegt werden können.

### 9.4 Gutes Beispiel

```json
{
  "type": "https://api.example.com/problems/email-already-registered",
  "title": "Email Already Registered",
  "status": 409,
  "detail": "The provided email address is already registered.",
  "instance": "/api/v1/users",
  "code": "USER_EMAIL_ALREADY_REGISTERED"
}
```

---

## 10. Versionierung

### 10.1 Standard: Version im URL-Pfad

Für produktive APIs wird die Hauptversion im URL-Pfad verwendet:

```http
/api/v1/orders
/api/v2/orders
```

Das ist sichtbar, einfach zu testen, gut in Logs und API-Gateways erkennbar und für Consumer leicht verständlich.

```java
@RestController
@RequestMapping("/api/v1/orders")
class OrderControllerV1 {
}

@RestController
@RequestMapping("/api/v2/orders")
class OrderControllerV2 {
}
```

### 10.2 Alternative: Media-Type-Versionierung

Media-Type-Versionierung kann sinnvoll sein, wenn Pfade stabil bleiben sollen und Consumer bewusst über `Accept` verhandeln.

```java
@GetMapping(
        value = "/api/orders/{orderId}",
        produces = "application/vnd.example.order.v2+json"
)
public OrderDetailV2Response findById(@PathVariable Long orderId) {
    return orderService.findByIdV2(orderId);
}
```

Diese Variante ist mächtiger, aber schwerer zu verstehen, zu testen und zu debuggen. Sie ist nicht Standard für dieses Kompendium.

### 10.3 Nicht verwenden: Query-Parameter-Versionierung

```http
GET /api/orders/42?version=2
```

Diese Variante ist für produktive APIs nicht der Standard. Sie ist weniger sichtbar, schwerer sauber zu cachen und vermischt Versionierung mit Filterparametern.

---

## 11. Was ist ein Breaking Change?

| Änderung | Breaking? | Begründung |
|---|---:|---|
| Feld entfernen | Ja | Consumer können darauf angewiesen sein |
| Feld umbenennen | Ja | JSON-Pfad ändert sich |
| Feldtyp ändern | Ja | `String` zu `Integer` bricht Parser |
| Pflichtfeld im Request hinzufügen | Ja | Alte Consumer senden es nicht |
| HTTP-Methode ändern | Ja | Client-Code bricht |
| Status-Code-Semantik ändern | Ja | Fehlerbehandlung bricht |
| Response-Struktur grundlegend ändern | Ja | Deserialisierung bricht |
| Authentifizierungsverfahren ändern | Ja | Consumer kann nicht mehr zugreifen |
| Neues optionales Response-Feld hinzufügen | Nein, meist | Robuste Consumer ignorieren unbekannte Felder |
| Neuer optionaler Query-Parameter | Nein | Alte Consumer unverändert |
| Neuer Endpoint | Nein | Bestehende Consumer nicht betroffen |
| Fehlermeldung sprachlich verbessern | Meist nein | Solange `type`/`code` stabil bleiben |
| Performance verbessern | Nein | Vertrag bleibt gleich |

### 11.1 Pflichtregel

Breaking Changes dürfen nicht stillschweigend in derselben API-Version ausgeliefert werden. Sie erfordern entweder:

- eine neue API-Hauptversion,
- eine explizite Migrationsphase,
- Consumer-Abstimmung,
- Contract Tests,
- Deprecation-Kommunikation,
- und ein dokumentiertes Abschaltdatum der alten Version.

---

## 12. Request- und Response-DTOs

### 12.1 Schlechtes Beispiel: Entity direkt exponieren

```java
@GetMapping("/{id}")
public UserEntity findById(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}
```

Das ist verboten, weil die Persistenzstruktur zum API-Vertrag wird. Interne Felder, Lazy-Loading-Strukturen, JPA-Annotationen und sensible Daten können versehentlich nach außen gelangen.

### 12.2 Gutes Beispiel: expliziter Response-Vertrag

```java
public record UserResponse(
        Long id,
        String displayName,
        String email
) {
}

@GetMapping("/{id}")
public UserResponse findById(@PathVariable Long id) {
    return userService.findById(id);
}
```

### 12.3 Request-DTOs mit Validation

```java
public record CreateOrderRequest(
        @NotNull Long productId,
        @Min(1) int quantity,
        @NotNull Long deliveryAddressId
) {
}
```

Request-DTOs dürfen nur Felder enthalten, die der Consumer setzen darf. Das verhindert Mass Assignment und übermäßige Objektmanipulation.

---

## 13. Pagination, Filtering und Sorting

### 13.1 Grundregel

Jeder Listenendpunkt muss Pagination unterstützen. Unbegrenzte Listen sind verboten.

```java
@GetMapping(produces = "application/json")
public PageResponse<OrderSummaryResponse> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort,
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to) {

    var pageable = PageRequest.of(
            normalizePage(page),
            normalizeSize(size),
            parseAllowedSort(sort)
    );

    var result = orderService.findAll(status, from, to, pageable);

    return PageResponse.from(result.map(orderMapper::toSummary));
}

private int normalizePage(int page) {
    return Math.max(page, 0);
}

private int normalizeSize(int size) {
    return Math.min(Math.max(size, 1), 100);
}
```

### 13.2 Stabile PageResponse statt Spring-Page direkt exponieren

Spring `Page<T>` kann intern nützlich sein. Als externer API-Vertrag ist ein eigener Response-Typ stabiler.

```java
public record PageResponse<T>(
        List<T> content,
        PageMetadata page
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                new PageMetadata(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}

public record PageMetadata(
        int number,
        int size,
        long totalElements,
        int totalPages
) {
}
```

### 13.3 Sorting nur per Allowlist

```java
private Sort parseAllowedSort(String sort) {
    var parts = sort.split(",");
    var property = parts[0];
    var direction = parts.length > 1 ? parts[1] : "asc";

    var allowed = Map.of(
            "createdAt", "createdAt",
            "total", "totalAmount",
            "status", "status"
    );

    if (!allowed.containsKey(property)) {
        throw new InvalidSortParameterException(property);
    }

    var sortDirection = "desc".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    return Sort.by(sortDirection, allowed.get(property));
}
```

### 13.4 Anti-Pattern: beliebige Sortfelder durchreichen

```java
Sort.by(requestedSortField);
```

Das ist riskant, weil interne Feldnamen exponiert und unerwartete Query-Pläne erzeugt werden können. Sortierfelder werden allowlist-basiert übersetzt.

---

## 14. Filtering

Filterparameter müssen fachlich benannt, typisiert und begrenzt sein.

Gute Beispiele:

```http
GET /api/v1/orders?status=PENDING
GET /api/v1/orders?from=2026-01-01T00:00:00Z&to=2026-01-31T23:59:59Z
GET /api/v1/orders?customerId=42&page=0&size=20
```

Schlechte Beispiele:

```http
GET /api/v1/orders?where=status='PENDING'
GET /api/v1/orders?sql=select * from orders
GET /api/v1/orders?filter={"internalField":"value"}
```

Filter dürfen keine interne Query-Sprache exponieren, sofern kein bewusst entworfenes, validiertes Query-Modell existiert.

---

## 15. Idempotenz bei kritischen Operationen

### 15.1 Problem

Netzwerke sind unzuverlässig. Consumer können Timeouts erleben und Requests wiederholen, obwohl der Provider die Operation bereits verarbeitet hat. Bei Zahlungen, Bestellungen, Gutscheinverwendung, Buchungen oder Kontobewegungen kann das zu Doppelverarbeitung führen.

### 15.2 Standard

Kritische nicht-idempotente Operationen unterstützen einen `Idempotency-Key`.

```java
@PostMapping(value = "/{orderId}/payments", consumes = "application/json", produces = "application/json")
public ResponseEntity<PaymentResponse> pay(
        @PathVariable Long orderId,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody PaymentRequest request) {

    var response = idempotencyService.getOrProcess(
            idempotencyKey,
            request.fingerprint(),
            () -> paymentService.process(orderId, request)
    );

    return ResponseEntity.ok(response);
}
```

### 15.3 Anforderungen an Idempotency Keys

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Pflicht bei kritischen Operationen | Zahlung, Bestellung, Buchung, Gutschein | `POST /payments` | Pflicht |
| Geltungsbereich | Key gilt pro Nutzer/Tenant und Operation | `tenantId + userId + key` | Pflicht |
| Fingerprint | Gleicher Key mit anderer Payload muss abgelehnt werden | Hash der normalisierten Payload | Pflicht |
| Ablaufzeit | Keys müssen ablaufen | 24–72 Stunden je nach Domäne | Pflicht |
| Speicherung | Ergebnis oder Verarbeitungsstatus persistieren | `idempotency_records` | Pflicht |
| Sicherheit | Key ist kein Secret, aber nicht erratbar | UUID/ULID | Empfehlung |

### 15.4 Wichtige Einordnung

`Idempotency-Key` ist ein weit verbreitetes Muster und wird in der IETF-HTTPAPI-Arbeitsgruppe als Header-Feld spezifiziert. Die Spezifikation ist als Draft/Arbeitsdokument zu behandeln; für interne Standards kann das Muster trotzdem verbindlich gesetzt werden, wenn Verhalten und Gültigkeit sauber dokumentiert sind.

---

## 16. Security- und SaaS-Aspekte

### 16.1 Broken Object Level Authorization vermeiden

Jeder Endpunkt, der eine ID aus dem Request nutzt, muss prüfen, ob der aktuelle Nutzer beziehungsweise Tenant auf diese Ressource zugreifen darf.

```java
@GetMapping("/{orderId}")
public OrderDetailResponse findById(
        @AuthenticationPrincipal CurrentUser currentUser,
        @PathVariable Long orderId) {

    return orderService.findByIdForUser(currentUser.userId(), currentUser.tenantId(), orderId);
}
```

Im Service:

```java
@Transactional(readOnly = true)
public OrderDetailResponse findByIdForUser(UserId userId, TenantId tenantId, Long orderId) {
    var order = orderRepository.findByIdAndTenantId(orderId, tenantId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    authorizationService.assertCanReadOrder(userId, order);

    return orderMapper.toDetail(order);
}
```

### 16.2 Keine sensitiven Daten in URLs

Verboten:

```http
GET /api/v1/users?email=max@example.com
GET /api/v1/reset-password?token=abc123
GET /api/v1/accounts/DE89370400440532013000
```

URLs landen häufig in Logs, Browser-History, Proxy-Logs, Monitoring und Referrer-Kontexten. Sensitive Daten werden in Bodies, sicheren Token-Flows oder serverseitigen Suchindizes behandelt.

### 16.3 Keine übermäßige Datenfreigabe

Response-DTOs dürfen nur Felder enthalten, die der Consumer wirklich braucht.

Schlecht:

```java
public record UserResponse(
        Long id,
        String email,
        String passwordHash,
        String internalRiskScore,
        boolean admin
) {
}
```

Gut:

```java
public record UserProfileResponse(
        Long id,
        String displayName,
        String email
) {
}
```

### 16.4 Ressourcenbegrenzung

APIs müssen Ressourcenverbrauch begrenzen:

- maximale Seitengröße,
- maximale Payload-Größe,
- Timeouts,
- Rate Limits,
- Tenant-Quotas,
- maximale Exportgröße,
- Schutz gegen teure Filterkombinationen,
- Schutz gegen unbounded Includes.

---

## 17. OpenAPI-Dokumentation

Jede produktive API muss eine OpenAPI-Spezifikation bereitstellen oder daraus generiert werden.

Mindestinhalt:

- Pfade und HTTP-Methoden,
- Request- und Response-Schemas,
- Status-Codes,
- Problem-Details-Fehlerantworten,
- Authentifizierungsverfahren,
- Beispielrequests,
- Pagination-Parameter,
- Rate-Limit-Header, sofern vorhanden,
- Version,
- Deprecation-Hinweise.

Beispiel für Controller-Anreicherung:

```java
@Operation(summary = "Bestellung anhand ihrer ID abrufen")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bestellung gefunden"),
        @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden"),
        @ApiResponse(responseCode = "403", description = "Kein Zugriff auf diese Bestellung")
})
@GetMapping("/{orderId}")
public OrderDetailResponse findById(@PathVariable Long orderId) {
    return orderService.findById(orderId);
}
```

Annotationen ersetzen keine gute API-Modellierung. Sie dokumentieren nur das, was fachlich sauber entworfen wurde.

---

## 18. Deprecation und Version Lifecycle

API-Versionen brauchen einen Lebenszyklus.

| Phase | Bedeutung | Pflichtmaßnahme |
|---|---|---|
| Active | Version ist regulär nutzbar | Tests, Dokumentation, Support |
| Deprecated | Version soll migriert werden | Deprecation Header, Doku, Migrationsleitfaden |
| Sunset | Abschaltung geplant | Abschaltdatum, Consumer-Kommunikation |
| Removed | Version entfernt | Gateway-Regel und Dokumentation aktualisieren |

Empfohlene Header für abgekündigte APIs:

```http
Deprecation: true
Sunset: Wed, 31 Dec 2026 23:59:59 GMT
Link: <https://api.example.com/docs/migration/v1-to-v2>; rel="deprecation"
```

---

## 19. Gute Controller-Struktur

Controller sind HTTP-Adapter. Sie validieren den HTTP-Vertrag, delegieren an Application Services und mappen das Ergebnis in Response-DTOs. Sie enthalten keine Geschäftslogik.

```java
@RestController
@RequestMapping("/api/v1/orders")
class OrderController {

    private final OrderApplicationService orderService;

    OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrderCreatedResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {

        var command = new CreateOrderCommand(
                currentUser.tenantId(),
                currentUser.userId(),
                request.productId(),
                request.quantity(),
                request.deliveryAddressId()
        );

        var created = orderService.create(command);
        var location = URI.create("/api/v1/orders/" + created.id());

        return ResponseEntity.created(location).body(created);
    }
}
```

Wichtig: Tenant- und User-Kontext werden nicht aus dem Request-Body akzeptiert, sondern aus dem authentifizierten Sicherheitskontext abgeleitet.

---

## 20. Anti-Patterns

### 20.1 RPC-URLs

```http
POST /api/doSomething
```

Problem: Keine Ressourcensemantik, schwer konsistent zu erweitern.

### 20.2 Fehler als `200 OK`

```json
{
  "success": false,
  "error": "Not found"
}
```

Problem: HTTP-Semantik, Monitoring und Client-Fehlerbehandlung werden beschädigt.

### 20.3 Entities als Response

```java
public UserEntity getUser() { ... }
```

Problem: Persistenzmodell wird API-Vertrag, Sensitive Data Exposure möglich.

### 20.4 Unbegrenzte Listen

```http
GET /api/v1/orders/all
```

Problem: Ressourcenerschöpfung, schlechte Performance, DoS-Risiko.

### 20.5 Freies Sortieren nach internen Feldern

```http
GET /api/v1/users?sort=passwordHash,desc
```

Problem: interne Felder und Query-Struktur werden exponiert.

### 20.6 Sensitive Daten in URL

```http
GET /api/v1/users?email=max@example.com
```

Problem: URLs werden breit geloggt und weitergereicht.

### 20.7 Mandant aus Request-Body übernehmen

```json
{
  "tenantId": "tenant-b",
  "productId": 123
}
```

Problem: Tenant-Spoofing. Tenant-Kontext kommt aus Authentifizierung, nicht aus Consumer-Input.

---

## 21. Tests

### 21.1 Controller-Test mit MockMvc

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean OrderApplicationService orderService;

    @Test
    void create_returns201AndLocation_whenRequestIsValid() throws Exception {
        var response = new OrderCreatedResponse(42L, "PENDING");
        when(orderService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 100,
                                  "quantity": 2,
                                  "deliveryAddressId": 55
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/42"))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void create_returns400_whenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": null,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }
}
```

### 21.2 Contract Tests

Für Service-to-Service-APIs müssen relevante Consumer-Erwartungen über Contract Tests abgesichert werden, insbesondere bei unabhängigen Teams oder unabhängig deploybaren Services.

### 21.3 Security Tests

Mindestens folgende Fälle müssen getestet werden:

- Zugriff auf fremde Ressource ist nicht möglich,
- Zugriff ohne Authentifizierung liefert `401`,
- Zugriff mit falscher Rolle liefert `403` oder bewusst `404`,
- Pagination begrenzt `size`,
- ungültiges Sorting wird abgelehnt,
- Request-Body kann keine nicht erlaubten Felder setzen,
- Fehlerantwort enthält keine Stacktraces oder sensitiven Daten.

---

## 22. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| URL | Ressource statt Aktion? | `/orders/{id}` | Pflicht |
| Version | Version im Pfad vorhanden? | `/api/v1` | Pflicht für produktive APIs |
| Methode | HTTP-Methode semantisch korrekt? | `POST /payments` | Pflicht |
| Status | Kein Fehler als `200 OK`? | `404`, `409`, `400` | Pflicht |
| Fehlerformat | RFC 9457 Problem Details? | `application/problem+json` | Pflicht |
| DTOs | Keine Entities im API-Vertrag? | `OrderResponse` | Pflicht |
| Validation | Request-DTO validiert? | `@Valid`, Bean Validation | Pflicht |
| Authorization | Objektberechtigung geprüft? | `findByIdAndTenantId` | Pflicht |
| Tenant | Tenant nicht aus Body übernommen? | Security Context | Pflicht |
| Pagination | Listen begrenzt? | `size <= 100` | Pflicht |
| Sorting | Allowlist verwendet? | `createdAt`, `status` | Pflicht |
| Sensitive Data | Keine Secrets/PII in URL oder Fehlern? | keine E-Mail in Query | Pflicht |
| Versionierung | Breaking Change erkannt? | Feld entfernt | Pflicht |
| OpenAPI | Dokumentation aktuell? | Schemas/Responses | Pflicht |
| Tests | Controller-/Contract-/Security-Tests vorhanden? | MockMvc, Pact | Pflicht |

---

## 23. Automatisierbare Prüfungen

Mögliche automatisierte Checks:

```text
- Keine Controller-Methoden dürfen JPA-Entities zurückgeben.
- Keine Controller dürfen direkt auf Repositories zugreifen.
- Alle Controller-Pfade müssen mit /api/v{number}/ beginnen.
- Keine Pfade mit Verben wie create, update, delete, getAll, doSomething.
- Listenendpunkte müssen page/size oder cursor unterstützen.
- Maximale Seitengröße muss begrenzt werden.
- OpenAPI-Spezifikation muss im CI validiert werden.
- Breaking Changes müssen über OpenAPI-Diff erkannt werden.
- ProblemDetail muss für definierte Domain-Exceptions verwendet werden.
```

Beispielrichtung mit ArchUnit:

```java
@ArchTest
static final ArchRule controller_should_not_return_entities = methods()
        .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
        .should().notHaveRawReturnType(UserEntity.class)
        .because("Controller dürfen keine Persistenz-Entities als API-Vertrag exponieren.");
```

Für OpenAPI-Diffs können Werkzeuge wie `openapi-diff` oder API-Governance-Checks in der CI eingesetzt werden.

---

## 24. Migration bestehender APIs

Bestehende uneinheitliche APIs werden nicht unkontrolliert umgebaut. Migration erfolgt in Schritten:

1. Bestehende API dokumentieren.
2. Consumer identifizieren.
3. Fehlerformat standardisieren, soweit non-breaking möglich.
4. DTOs einführen, ohne bestehende Felder sofort zu entfernen.
5. Pagination und Größenlimits ergänzen.
6. OpenAPI-Spezifikation erzeugen.
7. Contract Tests für kritische Consumer ergänzen.
8. Breaking Changes in `/api/v2` ausliefern.
9. Alte Version deprecaten.
10. Abschaltdatum kommunizieren.

---

## 25. Ausnahmen

Abweichungen sind möglich, aber begründungspflichtig.

| Ausnahme | Bedingung | Dokumentation |
|---|---|---|
| Externe Standard-API verlangt anderes Design | z. B. Drittanbieter-Kompatibilität | API-Review |
| Legacy-API kann nicht sofort versioniert werden | Consumer-Abhängigkeiten | Migrationsplan |
| Kein `/api/v1` bei rein interner Actuator-/Health-API | Plattformkonvention | Architekturregel |
| Media-Type-Versionierung | bewusstes API-Gateway-/Consumer-Modell | API-Design-Entscheidung |
| Cursor Pagination statt Page Pagination | große oder zeitlich sortierte Datenmengen | API-Dokumentation |

---

## 26. Definition of Done

Eine REST-API erfüllt diese Richtlinie, wenn sie ressourcenorientierte URLs nutzt, HTTP-Methoden und Status-Codes korrekt verwendet, Fehler über RFC 9457 Problem Details ausgibt, Request- und Response-DTOs statt Entities verwendet, Eingaben validiert, Objektberechtigungen und Tenant-Grenzen prüft, Listen begrenzt paginiert, Sorting allowlist-basiert behandelt, sensitive Daten aus URLs und Fehlerantworten fernhält, Breaking Changes versioniert, OpenAPI-Dokumentation bereitstellt und durch Controller-, Security- und bei Service-to-Service-Kommunikation Contract Tests abgesichert ist.

---

## 27. Quellen und weiterführende Literatur

- RFC 9110 — HTTP Semantics: https://www.rfc-editor.org/rfc/rfc9110.html
- RFC 9457 — Problem Details for HTTP APIs: https://www.rfc-editor.org/rfc/rfc9457.html
- Spring Framework Reference — Error Responses and ProblemDetail: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
- Spring Framework API — `ProblemDetail`: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ProblemDetail.html
- OpenAPI Specification 3.1.1: https://spec.openapis.org/oas/v3.1.1.html
- Spring Data REST — Paging and Sorting: https://docs.spring.io/spring-data/rest/reference/paging-and-sorting.html
- OWASP API Security Top 10 2023: https://owasp.org/API-Security/editions/2023/en/0x11-t10/
- OWASP API4:2023 — Unrestricted Resource Consumption: https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/
- IETF HTTPAPI Draft — Idempotency-Key HTTP Header Field: https://datatracker.ietf.org/doc/draft-ietf-httpapi-idempotency-key-header/
