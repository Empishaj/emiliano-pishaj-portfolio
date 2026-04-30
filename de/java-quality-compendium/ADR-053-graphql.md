# ADR-053 — GraphQL: Wann, wie und wie nicht

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Spring Boot 3.x · Spring for GraphQL         |
| Datum      | 2024-01-01                                        |
| Kategorie  | API Design                                        |

---

## Kontext & Problem

GraphQL ist nicht immer die bessere REST-Alternative — es ist eine andere Trade-off-Entscheidung. Wähle GraphQL wenn Clients flexibel verschiedene Datenmengen brauchen (mobile vs. desktop, verschiedene Teams konsumieren dieselbe API). Wähle REST wenn du ein klar definiertes, stabiles API mit wenigen Consumer-Typen hast.

---

## Wann GraphQL, wann REST?

```
GraphQL bevorzugen wenn:
✓ Verschiedene Clients brauchen verschiedene Datenmengen (Overfetching-Problem)
✓ Viele Frontend-Teams konsumieren dieselbe Backend-API
✓ Stark vernetzte Daten mit komplexen Beziehungen (Graph-Struktur)
✓ BFF (Backend-for-Frontend)-Pattern

REST bevorzugen wenn:
✓ Einfache CRUD-Operationen
✓ Wenige, gut definierte Consumer
✓ HTTP-Caching wichtig (GraphQL-POST-Requests nicht cachebar)
✓ Bekanntes API-Design ohne GraphQL-Lernkurve
✓ File-Uploads, Streaming
```

---

## Schema-First Design

```graphql
# src/main/resources/graphql/schema.graphqls
# Schema First: Schema definiert den Vertrag — Code folgt

type Query {
    order(id: ID!): Order
    orders(filter: OrderFilter, page: Int = 0, size: Int = 20): OrderPage!
    user(id: ID!): User
}

type Mutation {
    placeOrder(input: PlaceOrderInput!): PlaceOrderResult!
    cancelOrder(orderId: ID!): CancelOrderResult!
}

type Subscription {
    orderStatusChanged(orderId: ID!): OrderStatusEvent!
}

type Order {
    id:           ID!
    status:       OrderStatus!
    items:        [OrderItem!]!
    total:        Money!
    customer:     User!           # Kann lazy geladen werden (DataLoader!)
    createdAt:    DateTime!
}

type OrderItem {
    product:   Product!
    quantity:  Int!
    subtotal:  Money!
}

type Money {
    amount:   Float!
    currency: String!
}

enum OrderStatus {
    PENDING
    CONFIRMED
    SHIPPED
    DELIVERED
    CANCELLED
}

input PlaceOrderInput {
    items:           [OrderItemInput!]!
    shippingAddress: AddressInput!
}

type PlaceOrderResult {
    orderId: ID!
    total:   Money!
}

scalar DateTime
```

---

## Spring for GraphQL Implementierung

```java
// @Controller für GraphQL (nicht @RestController)
@Controller
public class OrderGraphQlController {

    private final PlaceOrderUseCase placeOrder;
    private final UserQueryService  userQuery;
    private final OrderQueryService orderQuery;

    // Query Handler: @QueryMapping = Methoden-Name = Schema-Field-Name
    @QueryMapping
    public OrderDto order(@Argument Long id) {
        return orderQuery.findById(id);
    }

    @QueryMapping
    public Connection<OrderSummaryDto> orders(
            @Argument OrderFilter filter,
            @Argument int page,
            @Argument int size) {
        return orderQuery.findAll(filter, PageRequest.of(page, size));
    }

    // Mutation Handler
    @MutationMapping
    public PlaceOrderResult placeOrder(@Argument PlaceOrderInput input,
                                        @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(Long.parseLong(jwt.getSubject()));
        return placeOrder.handle(mapper.toCommand(input, userId));
    }

    // SchemaField für verschachtelte Typen: customer im Order
    // WICHTIG: DataLoader verhindert N+1!
    @SchemaMapping(typeName = "Order", field = "customer")
    public CompletableFuture<UserDto> customer(
            OrderDto order,
            DataLoader<Long, UserDto> userLoader) {
        // DataLoader batcht alle customer-Anfragen einer Query zusammen
        return userLoader.load(order.customerId());
    }
}
```

---

## Das N+1-Problem in GraphQL: DataLoader

```java
// ❌ OHNE DataLoader: N+1-Problem
// Query: { orders { customer { name } } }
// → 1 Query für alle Orders
// → N Queries für jeden Customer (1 pro Order)

@SchemaMapping(typeName = "Order", field = "customer")
public UserDto customer(OrderDto order) {
    return userService.findById(order.customerId()); // N Queries!
}

// ✅ MIT DataLoader: alle Customer in einer Batch-Query
@Bean
public DataLoaderRegistry dataLoaderRegistry(UserQueryService userQuery) {
    var registry = new DataLoaderRegistry();

    // BatchLoader: wird einmal pro Request mit allen IDs aufgerufen
    registry.register("users", DataLoader.newMappedDataLoader(
        (Set<Long> userIds) -> {
            // Eine Query für alle IDs statt N Queries
            var users = userQuery.findByIds(userIds);
            return CompletableFuture.completedFuture(
                users.stream().collect(toMap(UserDto::id, identity()))
            );
        }
    ));

    return registry;
}

// Verwendung im Controller:
@SchemaMapping(typeName = "Order", field = "customer")
public CompletableFuture<UserDto> customer(
        OrderDto order,
        @Argument DataLoader<Long, UserDto> userLoader) {
    return userLoader.load(order.customerId()); // Gebatcht!
}
```

---

## Fehlerbehandlung

```java
// GraphQL Errors folgen der Spezifikation: kein HTTP 4xx/5xx
// → Fehler im "errors"-Array der Response

@ControllerAdvice
public class GraphQlExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public GraphQLError handleNotFound(OrderNotFoundException ex) {
        return GraphQLError.newError()
            .errorType(ErrorType.NOT_FOUND)
            .message(ex.getMessage())
            .extensions(Map.of("orderId", ex.orderId()))
            .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public GraphQLError handleUnauthorized(AccessDeniedException ex) {
        return GraphQLError.newError()
            .errorType(ErrorType.FORBIDDEN)
            .message("Access denied")
            .build();
        // Kein Detail: kein Information Leak!
    }
}
```

---

## Query-Tiefe und Komplexität begrenzen

```java
// Schutz gegen Denial-of-Service durch tief verschachtelte Queries:
// { orders { items { product { category { products { items { ... } } } } } } }

@Configuration
public class GraphQlConfig {

    @Bean
    public GraphQlSource graphQlSource(/* ... */) {
        return GraphQlSource.schemaResourceBuilder()
            .schemaResources(/* ... */)
            .configureRuntimeWiring(builder -> builder
                .instrumentation(new MaxQueryDepthInstrumentation(10))  // Max 10 Ebenen
                .instrumentation(new MaxQueryComplexityInstrumentation(100)) // Max 100 Felder
            )
            .build();
    }
}
```

---

## Konsequenzen

**Positiv:** Clients fragen nur die Daten ab die sie brauchen — kein Overfetching. Schema ist der explizite Vertrag (Contract Testing → ADR-019 adaptierbar). Subscriptions für Echtzeit-Updates.

**Negativ:** DataLoader obligatorisch sonst N+1. HTTP-Caching funktioniert nicht für Queries (POST). Komplexere Fehlermeldungen als bei REST. Schema-Versionierung erfordert Disziplin.

---

## 💡 Guru-Tipps

- **Persisted Queries**: Client sendet Query-Hash statt Query-String → kleinere Requests, besser cachebar.
- **`@DgsComponent`** (Netflix DGS) als Alternative zu Spring for GraphQL — mehr Annotations, ähnliche Konzepte.
- **Apollo Sandbox** für lokale Schema-Erkundung: besser als GraphiQL.
- **Schema Stitching mit Subgraphs**: Federation (Apollo) wenn mehrere Teams eigene GraphQL-Services haben.

---

## Verwandte ADRs

- [ADR-021](ADR-021-rest-api-design.md) — REST vs. GraphQL: bewusste Entscheidung.
- [ADR-016](ADR-016-datenbank-jpa-n-plus-eins.md) — DataLoader löst N+1 wie JOIN FETCH.
- [ADR-019](ADR-019-contract-testing.md) — Schema als Contract.
