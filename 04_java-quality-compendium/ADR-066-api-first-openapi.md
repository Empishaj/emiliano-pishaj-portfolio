# ADR-066 — API-First Design & OpenAPI

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · OpenAPI 3.1|
| Datum      | 2024-01-01                        |
| Kategorie  | API Design / Dokumentation        |

---

## Kontext & Problem

"Code-First" generiert OpenAPI aus Annotations — die Dokumentation ist immer hinter dem Code. Teams entwickeln parallel und merken erst bei Integration dass ihre Contracts nicht kompatibel sind. **API-First** dreht das um: das OpenAPI-Schema ist der Vertrag, Code wird daraus generiert. Schema-Änderung → Review → Code-Generierung → Implementierung.

---

## API-First Workflow

```
Traditionell (Code-First):
Code schreiben → Annotations hinzufügen → OpenAPI generieren → Docs veröffentlichen
Problem: API-Design-Entscheidungen werden spät im Prozess sichtbar

API-First:
OpenAPI-Schema entwerfen → Review im Team → Client/Server-Code generieren
→ Implementierung → Stabile, reviewbare API-Contracts von Anfang an
```

---

## OpenAPI-Schema (api-first)

```yaml
# src/main/resources/api/order-api.yml
openapi: "3.1.0"
info:
  title: Order Service API
  version: "2.3.0"
  description: |
    API für Bestellverwaltung. Alle Änderungen folgen Semantic Versioning.
    Breaking Changes werden in der MAJOR-Version kommuniziert.
  contact:
    name: Backend Team
    email: backend@example.com
  license:
    name: Apache 2.0

servers:
  - url: https://api.example.com/api/v2
    description: Production
  - url: https://staging-api.example.com/api/v2
    description: Staging

tags:
  - name: Orders
    description: Bestellungsverwaltung
  - name: OrderItems
    description: Artikel innerhalb einer Bestellung

paths:
  /orders:
    post:
      summary: Bestellung aufgeben
      operationId: placeOrder
      tags: [Orders]
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PlaceOrderRequest'
            examples:
              standard:
                summary: Standard-Bestellung
                value:
                  items:
                    - productId: "PROD-001"
                      quantity: 2
                  shippingAddress:
                    street: "Hauptstraße 1"
                    city: "Berlin"
                    zipCode: "10115"
                    country: "DE"
      responses:
        '201':
          description: Bestellung erfolgreich aufgegeben
          headers:
            Location:
              description: URL der neuen Bestellung
              schema:
                type: string
                format: uri
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderCreatedResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '422':
          $ref: '#/components/responses/BusinessError'

    get:
      summary: Bestellungen abfragen
      operationId: listOrders
      tags: [Orders]
      parameters:
        - name: status
          in: query
          schema:
            $ref: '#/components/schemas/OrderStatus'
        - name: page
          in: query
          schema:
            type: integer
            minimum: 0
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            minimum: 1
            maximum: 100
            default: 20
      responses:
        '200':
          description: Paginierte Bestellliste
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderPage'

  /orders/{orderId}:
    get:
      summary: Bestellung abrufen
      operationId: getOrder
      tags: [Orders]
      parameters:
        - name: orderId
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderDetail'
        '404':
          $ref: '#/components/responses/NotFound'

components:
  schemas:
    PlaceOrderRequest:
      type: object
      required: [items, shippingAddress]
      properties:
        items:
          type: array
          minItems: 1
          items:
            $ref: '#/components/schemas/OrderItemRequest'
        shippingAddress:
          $ref: '#/components/schemas/Address'
        couponCode:
          type: string
          maxLength: 20
          example: "SAVE10"

    OrderItemRequest:
      type: object
      required: [productId, quantity]
      properties:
        productId:
          type: string
          pattern: '^PROD-[0-9]{3,}$'
          example: "PROD-001"
        quantity:
          type: integer
          minimum: 1
          maximum: 100

    Address:
      type: object
      required: [street, city, zipCode, country]
      properties:
        street:
          type: string
          minLength: 1
          maxLength: 200
        city:
          type: string
        zipCode:
          type: string
          pattern: '^[0-9]{5}$'
        country:
          type: string
          pattern: '^[A-Z]{2}$'
          description: ISO 3166-1 alpha-2

    OrderStatus:
      type: string
      enum: [PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED]

    Money:
      type: object
      required: [amount, currency]
      properties:
        amount:
          type: number
          format: decimal
          example: 49.99
        currency:
          type: string
          pattern: '^[A-Z]{3}$'
          example: "EUR"

    OrderCreatedResponse:
      type: object
      required: [orderId, total]
      properties:
        orderId:
          type: integer
          format: int64
        total:
          $ref: '#/components/schemas/Money'

    OrderPage:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/OrderSummary'
        page:
          $ref: '#/components/schemas/PageInfo'

    PageInfo:
      type: object
      properties:
        number:         { type: integer }
        size:           { type: integer }
        totalElements:  { type: integer, format: int64 }
        totalPages:     { type: integer }

  responses:
    ValidationError:
      description: Validierungsfehler (RFC 9457)
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'

    NotFound:
      description: Ressource nicht gefunden
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'

    Unauthorized:
      description: Authentifizierung erforderlich

    BusinessError:
      description: Fachlicher Fehler (z.B. unzureichender Bestand)
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'

    ProblemDetail:
      type: object
      properties:
        type:    { type: string, format: uri }
        title:   { type: string }
        status:  { type: integer }
        detail:  { type: string }

  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

---

## Code-Generierung aus OpenAPI-Schema

```kotlin
// build.gradle.kts — OpenAPI Generator Plugin
plugins {
    id("org.openapi.generator") version "7.4.0"
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/api/order-api.yml")
    outputDir.set("$buildDir/generated")

    configOptions.set(mapOf(
        "interfaceOnly"           to "true",   // Nur Interface, nicht Impl
        "useSpringBoot3"          to "true",
        "useTags"                 to "true",
        "dateLibrary"             to "java8",
        "useJakartaEe"            to "true",
        "generateSupportingFiles" to "false",
        "skipDefaultInterface"    to "true"
    ))

    // Typzuordnung: OpenAPI-Typen → eigene Domänentypen
    typeMappings.set(mapOf(
        "Money" to "com.example.domain.Money"
    ))
}

// Generierter Code wird beim Kompilieren einbezogen
sourceSets {
    main {
        java.srcDirs("$buildDir/generated/src/main/java")
    }
}
```

```java
// Generiertes Interface — Controller implementiert es
// (niemals manuell editieren!)
public interface OrdersApi {

    @PostMapping("/orders")
    ResponseEntity<OrderCreatedResponse> placeOrder(
        @Valid @RequestBody PlaceOrderRequest request);

    @GetMapping("/orders")
    ResponseEntity<OrderPage> listOrders(
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size);
}

// Controller implementiert generiertes Interface
@RestController
public class OrderController implements OrdersApi {

    @Override
    public ResponseEntity<OrderCreatedResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        var result = orderService.place(mapper.toCommand(request));
        return ResponseEntity
            .created(URI.create("/api/v2/orders/" + result.orderId()))
            .body(mapper.toResponse(result));
    }
}
```

---

## Schema-Validierung in CI

```yaml
# .github/workflows/api-lint.yml
- name: Lint OpenAPI Schema
  uses: stoplightio/spectral-action@v0.8
  with:
    file_glob: 'src/main/resources/api/**/*.yml'
    # Regeln: keine undokumented Endpunkte, alle Felder mit Beispielen, etc.

- name: Validate Schema Breaking Changes
  uses: oasdiff/oasdiff-action@main
  with:
    base: 'https://raw.githubusercontent.com/org/repo/main/src/main/resources/api/order-api.yml'
    revision: 'src/main/resources/api/order-api.yml'
    # Schlägt fehl bei Breaking Changes ohne MAJOR-Version-Increment
```

---

## Konsequenzen

**Positiv:** API-Contract ist reviewbar bevor Implementierung beginnt. Client-Code kann parallel entwickelt werden. Breaking Changes werden durch Schema-Diff-Tool automatisch erkannt. Dokumentation ist immer aktuell — sie IST der Code.

**Negativ:** Initiales Setup-Overhead. Generierter Code muss mit eigenem Code koexistieren — Mapping nötig. Schema-Evolution mit Backward-Compatibility erfordert Disziplin.

---

## 💡 Guru-Tipps

- **`$ref` konsequent nutzen**: alle Schemas zentral in `components/schemas` — kein Inline-Schema.
- **`examples` für jeden Endpunkt**: Swagger UI und Postman generieren Testcases daraus.
- **Spectral Rules**: eigene Linting-Regeln für Naming-Conventions, Pflicht-Felder, etc.
- **Mock Server aus Schema**: `prism mock order-api.yml` — Contract Testing ohne Server.

---

## Verwandte ADRs

- [ADR-021](ADR-021-rest-api-design.md) — REST-Konventionen die das Schema befolgt.
- [ADR-019](ADR-019-contract-testing.md) — OpenAPI-Schema als Contract für Pact.
- [ADR-048](ADR-048-javadoc-standards.md) — Dokumentation: Schema + JavaDoc ergänzen sich.
