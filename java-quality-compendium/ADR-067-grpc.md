# ADR-067 — gRPC: Typsichere Service-Kommunikation

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · gRPC 1.6x  |
| Datum      | 2024-01-01                        |
| Kategorie  | API Design / Microservices        |

---

## Kontext & Problem

REST/JSON ist für externe APIs die richtige Wahl. Für interne Service-Kommunikation hat gRPC Vorteile: stark typisierte Contracts (Protobuf), bidirektionales Streaming, HTTP/2 Multiplexing, und bis zu 10× kleiner als JSON. Wähle gRPC für interne Services mit hohem Durchsatz oder Streaming-Anforderungen.

---

## REST vs. gRPC: wann was?

```
REST/JSON (→ ADR-021):
✓ Externe APIs (Browser, Mobile, Third-Party)
✓ Einfache Request/Response ohne Streaming
✓ Menschenlesbare Payloads wichtig
✓ HTTP-Caching gewünscht

gRPC:
✓ Interne Service-zu-Service-Kommunikation
✓ Bidirektionales Streaming (z.B. Live-Updates)
✓ Performance-kritische Pfade (niedrige Latenz, hoher Durchsatz)
✓ Polyglot: Java → Go → Python alles typsicher
✗ Kein Browser-Support ohne gRPC-Web
✗ Nicht human-readable (Protobuf binary)
```

---

## Protobuf Schema definieren

```protobuf
// src/main/proto/order_service.proto
syntax = "proto3";
package com.example.order;
option java_package = "com.example.grpc";
option java_multiple_files = true;

import "google/protobuf/timestamp.proto";

// Service Definition — der typsichere Vertrag
service OrderService {
  // Unary RPC: Request → Response
  rpc PlaceOrder(PlaceOrderRequest) returns (PlaceOrderResponse);
  rpc GetOrder(GetOrderRequest)     returns (OrderResponse);

  // Server Streaming: ein Request, viele Responses
  rpc WatchOrderStatus(WatchOrderRequest)
      returns (stream OrderStatusUpdate);

  // Client Streaming: viele Requests, eine Response
  rpc ImportOrders(stream ImportOrderRequest)
      returns (ImportOrdersResponse);

  // Bidirectional Streaming
  rpc ChatWithOrderSupport(stream SupportMessage)
      returns (stream SupportMessage);
}

message PlaceOrderRequest {
  string customer_id = 1;
  repeated OrderItem items = 2;
  Address shipping_address = 3;
}

message OrderItem {
  string product_id = 1;
  int32  quantity   = 2;
}

message Address {
  string street   = 1;
  string city     = 2;
  string zip_code = 3;
  string country  = 4;
}

message PlaceOrderResponse {
  int64  order_id = 1;
  string status   = 2;
  Money  total    = 3;
}

message Money {
  int64  amount_cents = 1;   // Cents statt Decimal — kein Floating-Point!
  string currency     = 2;
}

message WatchOrderRequest {
  int64 order_id = 1;
}

message OrderStatusUpdate {
  int64                        order_id   = 1;
  string                       status     = 2;
  google.protobuf.Timestamp    updated_at = 3;
}

// Fehler via gRPC Status Codes + Details
message OrderError {
  string code    = 1;
  string message = 2;
  string field   = 3;  // Für Validierungsfehler
}
```

---

## Spring Boot gRPC Server

```kotlin
// build.gradle.kts
plugins {
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0")
    implementation("io.grpc:grpc-protobuf:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.3" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.62.2" }
    }
    generateProtoTasks {
        all().forEach { it.plugins { id("grpc") {} } }
    }
}
```

```java
// gRPC Service Implementierung
@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private final PlaceOrderUseCase placeOrder;
    private final OrderQueryService orderQuery;

    @Override
    public void placeOrder(PlaceOrderRequest request,
                            StreamObserver<PlaceOrderResponse> responseObserver) {
        try {
            // Protobuf → Domain-Command
            var command = new PlaceOrderCommand(
                new CustomerId(request.getCustomerId()),
                request.getItemsList().stream()
                    .map(i -> new OrderItem(new ProductId(i.getProductId()),
                                            new Quantity(i.getQuantity())))
                    .toList(),
                toAddress(request.getShippingAddress())
            );

            var result = placeOrder.execute(command);

            // Domain-Result → Protobuf-Response
            var response = PlaceOrderResponse.newBuilder()
                .setOrderId(result.orderId().value())
                .setStatus("PENDING")
                .setTotal(Money.newBuilder()
                    .setAmountCents(result.total().toCents())
                    .setCurrency(result.total().currency().getCurrencyCode())
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (InsufficientStockException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                .withDescription("Insufficient stock: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal error")
                .withCause(e)
                .asRuntimeException());
        }
    }

    // Server Streaming: Bestellstatus live beobachten
    @Override
    public void watchOrderStatus(WatchOrderRequest request,
                                  StreamObserver<OrderStatusUpdate> responseObserver) {
        var orderId = request.getOrderId();

        // Events subscriben und an Stream senden
        orderEventSubscription.subscribe(orderId, event -> {
            if (event instanceof OrderStatusChangedEvent statusEvent) {
                responseObserver.onNext(OrderStatusUpdate.newBuilder()
                    .setOrderId(orderId)
                    .setStatus(statusEvent.newStatus().name())
                    .setUpdatedAt(toTimestamp(statusEvent.occurredAt()))
                    .build());
            }
        });
        // Stream bleibt offen bis Client disconnect oder Order abgeschlossen
    }
}
```

---

## gRPC Client (anderer Service)

```java
// gRPC Client in einem anderen Service
@Configuration
public class GrpcClientConfig {

    @Bean
    public OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub() {
        var channel = ManagedChannelBuilder
            .forAddress("order-service", 9090)
            .usePlaintext() // TLS im Kubernetes via mTLS (Service Mesh)
            .build();
        return OrderServiceGrpc.newBlockingStub(channel)
            .withDeadlineAfter(3, TimeUnit.SECONDS); // Timeout obligatorisch!
    }
}

// Oder mit spring-boot-starter:
// grpc.client.order-service.address=static://order-service:9090
// grpc.client.order-service.negotiation-type=PLAINTEXT

@Service
public class ExternalOrderService {

    @GrpcClient("order-service")
    private OrderServiceGrpc.OrderServiceBlockingStub stub;

    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        try {
            return stub.placeOrder(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServiceUnavailableException("Order service unavailable");
            }
            throw new ExternalServiceException("Order service error: " + e.getStatus());
        }
    }
}
```

---

## gRPC Interceptors: Auth, Logging, Tracing

```java
// Server Interceptor: JWT-Validierung für alle gRPC-Calls
@Component
public class AuthGrpcInterceptor implements ServerInterceptor {

    @Override
    public <Req, Resp> ServerCall.Listener<Req> interceptCall(
            ServerCall<Req, Resp> call,
            Metadata headers,
            ServerCallHandler<Req, Resp> next) {

        var authHeader = headers.get(
            Metadata.Key.of("authorization", ASCII_STRING_MARSHALLER));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing auth token"), headers);
            return new ServerCall.Listener<>() {};
        }

        try {
            var jwt = jwtValidator.validate(authHeader.substring(7));
            var ctx = Context.current().withValue(USER_CONTEXT_KEY, jwt.getSubject());
            return Contexts.interceptCall(ctx, call, headers, next);
        } catch (InvalidTokenException e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), headers);
            return new ServerCall.Listener<>() {};
        }
    }
}
```

---

## gRPC Konfiguration

```yaml
# application.yml
grpc:
  server:
    port: 9090
    security:
      enabled: false  # mTLS via Service Mesh (Istio/Linkerd)

  client:
    order-service:
      address: "discovery:///order-service"  # Kubernetes Service Discovery
      negotiation-type: PLAINTEXT
      deadline: 3000  # 3 Sekunden Timeout
```

---

## Konsequenzen

**Positiv:** Protobuf ist ~3-10× kleiner als JSON. HTTP/2 Multiplexing — kein Head-of-Line-Blocking. Stark typisierter Contract — Compiler-Fehler statt Runtime-Fehler bei Inkompatibilität. Bidirektionales Streaming nativ.

**Negativ:** Nicht browser-kompatibel ohne gRPC-Web. Binäres Format schwerer zu debuggen (Postman hat gRPC-Support). Schema-Evolution mit Protobuf-Kompatibilitätsregeln.

---

## 💡 Guru-Tipps

- **Field Numbers in Protobuf sind unveränderlich** — entfernen oder wiederverwenden bricht Kompatibilität.
- **`reserved` für entfernte Felder**: `reserved 5, 6; reserved "old_field";`
- **Deadline statt Timeout**: gRPC propagiert Deadlines automatisch durch Call-Chain.
- **Evans (gRPC Postman)**: `evans -r repl --host localhost --port 9090` für interaktives Testing.
- **Buf**: modernes Tool für Protobuf Linting, Breaking Change Detection, Schema Registry.

---

## Verwandte ADRs

- [ADR-021](ADR-021-rest-api-design.md) — REST für externe APIs, gRPC für interne.
- [ADR-022](ADR-022-resilience-circuit-breaker.md) — Circuit Breaker auch für gRPC-Clients.
- [ADR-066](ADR-066-api-first-openapi.md) — Protobuf ist gRPCs API-First-Schema.
