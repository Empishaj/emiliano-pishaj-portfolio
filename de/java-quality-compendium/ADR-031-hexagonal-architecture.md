# ADR-031 — Hexagonal Architecture (Ports & Adapters)

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur                       |

---

## Kontext & Problem

In klassischen Schichtarchitekturen fließen Framework-Details (JPA-Annotationen, Spring-Typen, HTTP-Konzepte) in die Domänenklassen ein. Das Ergebnis: die Domäne ist nicht ohne Framework testbar, ein Datenbankwechsel erfordert Domänenänderungen, und Business-Logik ist über alle Schichten verteilt. Die Hexagonale Architektur löst das durch eine klare Inversion der Abhängigkeiten.

---

## Das Kernprinzip

```
Außen → Adapter → Port → Domain ← Port ← Adapter ← Außen

Domain kennt NICHTS von:
  - Spring, JPA, HTTP, Kafka, Redis
  - Datenbankschemas, API-Strukturen
  - Infrastruktur-Technologien

Alles außerhalb der Domain kennt die Domain (über Ports)
Die Domain kennt niemanden außer sich selbst
```

---

## ❌ Schlecht — Framework-Details in der Domäne

```java
// Domain-Entity: voller JPA-Annotationen — kein Test ohne Spring-Context
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;

    // Business-Logik gemischt mit JPA-Verhalten
    @Transactional  // Spring-Annotation in der Domain!
    public void cancel() { ... }
}

// Service: kennt HTTP-Request-Struktur
@Service
public class OrderService {
    public void createOrder(HttpServletRequest request) { // HTTP-Objekt in der Domain!
        var json = request.getReader().lines().collect(joining());
        // JSON parsen, Domain-Logik, JPA-Aufrufe — alles gemischt
    }
}
```

---

## ✅ Gut — Hexagonale Struktur

### Paketstruktur

```
com.example.
├── domain/                        ← Kern — kennt kein Framework
│   ├── model/
│   │   ├── Order.java             ← Pure Java, keine Annotationen
│   │   ├── OrderItem.java
│   │   └── Money.java
│   ├── port/
│   │   ├── in/                    ← Inbound Ports (Use Cases)
│   │   │   ├── PlaceOrderUseCase.java
│   │   │   └── CancelOrderUseCase.java
│   │   └── out/                   ← Outbound Ports (Abhängigkeiten der Domain)
│   │       ├── OrderRepository.java
│   │       └── PaymentGateway.java
│   └── service/
│       └── OrderDomainService.java ← Implementiert Inbound Ports
│
├── application/                   ← Orchestrierung (darf Spring kennen)
│   └── OrderApplicationService.java
│
└── adapter/                       ← Außenwelt → Framework
    ├── in/
    │   ├── rest/
    │   │   └── OrderController.java  ← HTTP → Domain
    │   └── messaging/
    │       └── OrderEventConsumer.java ← Kafka → Domain
    └── out/
        ├── persistence/
        │   └── JpaOrderRepository.java  ← Domain → JPA
        └── payment/
            └── StripePaymentAdapter.java ← Domain → Stripe
```

### Domain — pure Java, kein Framework

```java
// Domain-Modell: keine Annotationen, keine Abhängigkeiten
public final class Order {
    private final OrderId id;
    private final UserId  customerId;
    private final List<OrderItem> items;
    private OrderStatus status;

    // Reines Domänenverhalten — testbar ohne Spring, JPA, DB
    public void cancel() {
        if (status == DELIVERED) throw new OrderCannotBeCancelledException(id);
        this.status = CANCELLED;
    }

    public Money total() {
        return items.stream().map(OrderItem::subtotal).reduce(Money.zero(), Money::add);
    }
}
```

### Inbound Port (Use Case Interface)

```java
// Port: definiert was die Domain nach außen anbietet
// Input/Output als Records — kein Framework-Typ
public interface PlaceOrderUseCase {

    record PlaceOrderCommand(
        UserId          customerId,
        List<OrderItem> items,
        Address         shippingAddress
    ) {}

    record PlaceOrderResult(OrderId orderId, Money total) {}

    PlaceOrderResult placeOrder(PlaceOrderCommand command);
}
```

### Outbound Port (Repository Interface)

```java
// Port: definiert was die Domain von der Infrastruktur braucht
// Domain-Interface — wird von einem Adapter implementiert
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomer(UserId customerId);
}
```

### Domain Service — implementiert Use Cases

```java
// Implementiert Inbound Port — kennt nur Outbound Ports (keine Infrastruktur)
public class OrderDomainService implements PlaceOrderUseCase, CancelOrderUseCase {

    private final OrderRepository orderRepository; // Outbound Port — kein JPA direkt!
    private final PaymentGateway  paymentGateway;  // Outbound Port — kein Stripe direkt!

    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        var order = new Order(OrderId.generate(), command.customerId(), command.items());
        var payment = paymentGateway.charge(order.total(), command.customerId());
        if (!payment.succeeded()) throw new PaymentFailedException(payment.reason());
        orderRepository.save(order);
        return new PlaceOrderResult(order.id(), order.total());
    }
}
// Test: kein Spring, kein JPA, nur Mocks der Ports
```

### Inbound Adapter — REST → Domain

```java
// Adapter übersetzt HTTP → Domain-Command
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrder; // Inbound Port, keine Domain-Klasse direkt

    @PostMapping
    @ResponseStatus(CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // HTTP-Request → Domain-Command
        var command = new PlaceOrderUseCase.PlaceOrderCommand(
            new UserId(request.customerId()),
            request.items().stream().map(this::toOrderItem).toList(),
            toAddress(request.shippingAddress())
        );

        var result = placeOrder.placeOrder(command);

        // Domain-Result → HTTP-Response
        return new OrderResponse(result.orderId().value(), result.total().toString());
    }
}
```

### Outbound Adapter — Domain → JPA

```java
// Adapter implementiert das Domain-Repository-Interface mit JPA
@Repository
public class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springRepo; // Spring Data Repository
    private final OrderEntityMapper         mapper;

    @Override
    public void save(Order order) {
        springRepo.save(mapper.toEntity(order)); // Domain → JPA Entity
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return springRepo.findById(id.value())
            .map(mapper::toDomain); // JPA Entity → Domain
    }
}
// Mapper: bidirektionale Übersetzung Domain ↔ Persistence-Modell
```

---

## Abhängigkeitsregel (nach innen zeigen)

```
❌ Domain → JpaOrderRepository (konkrete Klasse)
✅ Domain → OrderRepository (Port / Interface)
   JpaOrderRepositoryAdapter → OrderRepository (implementiert)
   JpaOrderRepositoryAdapter → Spring Data (darf Framework kennen)
```

---

## Testing-Vorteil

```java
// Domain-Test: kein Spring, kein JPA, blitzschnell
class OrderDomainServiceTest {

    @Test
    void placeOrder_chargesPaymentAndPersists() {
        // Mocks der Ports — keine Container, keine DB
        var repository  = mock(OrderRepository.class);
        var gateway     = mock(PaymentGateway.class);
        when(gateway.charge(any(), any())).thenReturn(PaymentResult.success("tx-1"));

        var service = new OrderDomainService(repository, gateway);
        var result  = service.placeOrder(validCommand());

        verify(repository).save(any(Order.class));
        assertThat(result.orderId()).isNotNull();
    }
}
// Läuft in Millisekunden — kein @SpringBootTest nötig
```

---

## Konsequenzen

**Positiv:** Domain ist 100% framework-unabhängig und in Millisekunden testbar. Infrastruktur austauschbar ohne Domänenänderungen (PostgreSQL → MongoDB → keine Codeänderung in der Domain). Business-Regeln klar abgegrenzt von technischen Details.

**Negativ:** Mehr Klassen und Pakete als klassische Schichtarchitektur. Mapping zwischen Domain- und Infrastruktur-Modellen ist Aufwand. Für kleine CRUD-Applikationen oft Overengineering — erst sinnvoll wenn Domänenkomplexität vorhanden ist.

---

## 💡 Guru-Tipps

- **Ports sind Interfaces** — niemals Klassen. Sie sind der Vertrag zwischen Domain und Außenwelt.
- **Mapper-Klassen** für Domain ↔ Persistence: MapStruct (→ ADR-011) spart Boilerplate.
- **ArchUnit-Regel** um Abhängigkeitsregel maschinell zu prüfen (→ ADR-009):
  ```java
  noClasses().that().resideInAPackage("..domain..")
      .should().dependOnClassesThat()
      .resideInAPackage("..adapter..")
      .because("ADR-031: Domain kennt keine Adapter");
  ```
- **Spring-Annotationen auf Adapter beschränken**: `@Service`, `@Repository`, `@Component` — nie in `domain/`.

---

## Verwandte ADRs

- [ADR-025](ADR-025-solid-prinzipien.md) — DIP ist das Fundament der Hexagonalen Architektur.
- [ADR-023](ADR-023-domain-driven-design.md) — DDD + Hexagonal ergänzen sich.
- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ArchUnit sichert Abhängigkeitsregel.
