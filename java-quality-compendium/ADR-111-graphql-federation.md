# ADR-111 — GraphQL Federation: Ein Graph aus vielen Services

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | API Design · GraphQL · Microservices                          |
| Betroffene Teams  | Teams die GraphQL-APIs anbieten                               |
| Abhängigkeiten    | ADR-053 (GraphQL), ADR-085 (Team Topologies)                  |

---

## 1. Kontext & Treiber

### 1.1 Das Problem: GraphQL in Microservice-Architekturen

```
OHNE FEDERATION:
  Jeder Microservice hat seinen eigenen GraphQL-Endpunkt.
  
  Frontend fragt:
    /orders-service/graphql   → Bestellungen
    /users-service/graphql    → Kundendaten
    /products-service/graphql → Produktdetails

  Problem 1: Frontend macht 3 separate Requests für eine View
  Problem 2: Jedes Team baut seinen eigenen GraphQL-Server
  Problem 3: Kein einheitliches Typsystem — "Order.customer" gibt es 3×

MIT FEDERATION (Apollo Federation v2 / Hive Federation):
  Ein einheitlicher Graph-Endpunkt (/graphql)
  Dahinter: mehrere Subgraphs (je Service)
  Frontend fragt nur einmal — Router verteilt intern

  query {
    order(id: "ord-123") {    # → orders-service
      id
      total
      customer {              # → users-service (Type-Extension!)
        name
        email
      }
      items {
        product {             # → products-service (Type-Extension!)
          name
          price
        }
      }
    }
  }
```

---

## 2. Entscheidung

GraphQL Federation wird eingesetzt wenn mehr als zwei Teams denselben
zusammengesetzten Graphen anbieten müssen. Jedes Team besitzt seinen Subgraph.
Der Router (Apollo Router oder Hive) aggregiert sie.

---

## 3. Subgraph-Architektur

```
              ┌──────────────────────────────────┐
              │         GraphQL Router            │
              │   (Apollo Router / Hive)          │
              │   POST /graphql                   │
              └───┬──────────────┬───────────────┘
                  │              │               │
          ┌───────▼──┐   ┌──────▼──┐   ┌────────▼──┐
          │ Orders   │   │ Users   │   │ Products  │
          │ Subgraph │   │ Subgraph│   │ Subgraph  │
          └──────────┘   └─────────┘   └───────────┘
            Team: Orders   Team:Users   Team: Products
```

### 3.1 Subgraph definieren: Orders-Service

```java
// build.gradle.kts
dependencies {
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter:8.4.0")
    implementation("com.netflix.graphql.dgs:graphql-dgs-federation:8.4.0")
}
```

```graphql
# src/main/resources/schema/orders.graphqls

# @key: macht Order zu einer Entity die andere Subgraphs erweitern können
type Order @key(fields: "id") {
  id:         ID!
  status:     OrderStatus!
  totalCents: Int!
  currency:   String!
  createdAt:  String!
  # customer ist in users-service definiert — hier nur Referenz
  customer:   Customer!
  items:      [OrderItem!]!
}

# Stub: Customer kommt aus users-service, wir definieren nur was wir brauchen
type Customer @key(fields: "id") @extends {
  id: ID! @external   # Existiert im users-service, hier nur referenziert
}

type OrderItem {
  quantity: Int!
  product: Product!
}

type Product @key(fields: "id") @extends {
  id: ID! @external
}

enum OrderStatus {
  PENDING_PAYMENT
  CONFIRMED
  SHIPPED
  DELIVERED
  CANCELLED
}

type Query {
  order(id: ID!):          Order
  ordersByCustomer(customerId: ID!): [Order!]!
}

type Mutation {
  placeOrder(input: PlaceOrderInput!): Order!
  cancelOrder(id: ID!):               Order!
}

input PlaceOrderInput {
  customerId: String!
  items:      [OrderItemInput!]!
}

input OrderItemInput {
  productId: String!
  quantity:  Int!
}
```

```java
// Orders-Subgraph: DataFetcher
@DgsComponent
public class OrderDataFetcher {

    @SpringBean
    private OrderQueryService orderService;

    @DgsQuery
    public Order order(@InputArgument String id) {
        return orderService.findById(new OrderId(id))
            .map(orderFederationMapper::toGraphQL)
            .orElse(null);
    }

    // Federation: Resolver für @key — wenn Router Customer-Objekte braucht
    @DgsEntityFetcher(name = "Order")
    public Order resolveOrder(Map<String, Object> values) {
        var id = (String) values.get("id");
        return orderService.findById(new OrderId(id))
            .map(orderFederationMapper::toGraphQL)
            .orElse(null);
    }
}
```

### 3.2 Users-Service: Customer erweitern

```graphql
# src/main/resources/schema/users.graphqls

# Primäre Definition von Customer
type Customer @key(fields: "id") {
  id:    ID!
  name:  String!
  email: String!
  phone: String
}

# Customer um Order-Kontext erweitern
extend type Customer @key(fields: "id") {
  orders: [Order!]!     # Hinzugefügt von users-service
}

type Query {
  customer(id: ID!): Customer
  me:                Customer
}
```

```java
@DgsComponent
public class CustomerOrdersFetcher {

    @SpringBean
    private OrderQueryService orderService;

    // Federation: wenn Router Customer + orders braucht
    @DgsEntityFetcher(name = "Customer")
    public Customer resolveCustomer(Map<String, Object> values) {
        var customerId = (String) values.get("id");
        // Nur ID zurückgeben — orders wird lazy geladen
        return Customer.builder().id(customerId).build();
    }

    @DgsData(parentType = "Customer", field = "orders")
    public List<Order> customerOrders(DgsDataFetchingEnvironment env) {
        var customer = env.getSource();
        return orderService.findByCustomerId(new CustomerId(customer.getId()))
            .stream()
            .map(orderMapper::toGraphQL)
            .toList();
    }
}
```

---

## 4. Router-Konfiguration (Apollo Router)

```yaml
# router.yaml
federation_version: "2"

supergraph:
  # Supergraph-Schema aus Subgraph-SDL generieren
  # In CI: via Rover CLI zusammenbauen
  path: /etc/router/supergraph.graphql

subgraphs:
  orders:
    routing_url: http://orders-service:8080/graphql
  users:
    routing_url: http://users-service:8080/graphql
  products:
    routing_url: http://products-service:8080/graphql

# Query-Planung: Router optimiert automatisch
# Parallele Subgraph-Abfragen wenn möglich
traffic_shaping:
  router:
    global_rate_limit:
      capacity: 1000
      interval: 1s
  all:
    timeout: 10s

# CORS
cors:
  allow_any_origin: false
  origins:
    - https://app.example.com

# Authentication: JWT weitergeben an Subgraphs
authentication:
  router:
    jwt:
      jwks:
        url: https://auth.example.com/.well-known/jwks.json
```

### 4.1 Supergraph-Schema in CI generieren

```yaml
# .gitlab-ci.yml
schema:compose:
  stage: validate
  image: node:20-alpine
  before_script:
    - npm install -g @apollo/rover
  script:
    # Rover: holt SDL von allen Subgraphs und validiert
    - |
      rover supergraph compose \
        --config rover-config.yaml \
        --output supergraph.graphql

    # Schema gegen Registry validieren (Breaking Changes?)
    - |
      rover subgraph check mygraph@production \
        --name orders \
        --schema orders-service/src/main/resources/schema/orders.graphqls \
        --routing-url http://orders-service/graphql
```

```yaml
# rover-config.yaml
federation_version: "=2.4.0"
subgraphs:
  orders:
    routing_url: http://orders-service:8080/graphql
    schema:
      file: ./orders-service/src/main/resources/schema/orders.graphqls
  users:
    routing_url: http://users-service:8080/graphql
    schema:
      file: ./users-service/src/main/resources/schema/users.graphqls
  products:
    routing_url: http://products-service:8080/graphql
    schema:
      file: ./products-service/src/main/resources/schema/products.graphqls
```

---

## 5. Wann Federation, wann normales GraphQL?

```
NORMALES GRAPHQL (ADR-053) verwenden wenn:
  ✅ Ein Team besitzt alle Daten die der Graph braucht
  ✅ < 3 Teams involviert
  ✅ Monolith oder Modulith (→ ADR-079)
  ✅ Keine Team-übergreifenden Type-Extensions

FEDERATION verwenden wenn:
  ✅ > 2 Teams sollen denselben Graphen erweitern
  ✅ Teams sollen ihre Subgraphs unabhängig deployen
  ✅ Verschiedene Teams besitzen verschiedene Teile desselben Domänenobjekts
  Beispiel: Customer hat Bestellungen (Orders-Team) UND Präferenzen (Users-Team)
```

---

## 6. Akzeptanzkriterien

- [ ] `rover supergraph compose` läuft in CI ohne Fehler
- [ ] Breaking-Change-Detection via `rover subgraph check` in CI aktiv
- [ ] Subgraph-Schema ist pro Team im eigenen Repository
- [ ] Router-Latenz < 20ms overhead (gemessen mit Prometheus)
- [ ] `@key`-Fields sind immer `ID!` (nicht nullable)

---

## Quellen & Referenzen

- **Apollo Documentation, "Federation v2"** — offizielle Referenz.
- **Marc-André Giroux, "Production Ready GraphQL" (2020)** — Federation-Best-Practices.

---

## Verwandte ADRs

- [ADR-053](ADR-053-graphql.md) — GraphQL-Grundkonfiguration
- [ADR-085](ADR-085-sozio-technische-systeme.md) — Team-Ownership für Subgraphs
