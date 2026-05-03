# QG-JAVA-031 — Hexagonal Architecture: Ports & Adapters

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-031 |
| Titel | Hexagonal Architecture: Ports & Adapters |
| Status | Accepted / verbindlicher Standard für domänenkritische Services und neue fachlich komplexe Module |
| Version | 1.0.0 |
| Datum | 2024-08-19 |
| Review-Datum | 2026-05-01 |
| Kategorie | Architektur / Domain Design / Testbarkeit |
| Zielgruppe | Java-Entwickler, Tech Leads, Architektur, QA, Security, Plattform-Team |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Framework 6.x, JUnit 5, ArchUnit |
| Geltungsbereich | Domänenkritische Services, Microservices, Integrationsmodule, fachliche Use Cases, Systeme mit mehreren technischen Adaptern oder hohem Änderungsdruck |
| Verbindlichkeit | Hexagonal Architecture SOLLTE für fachlich komplexe oder integrationsreiche Services verwendet werden. Für einfache CRUD-Services ist eine bewusst einfachere Schichtenarchitektur zulässig. |
| Technische Validierung | Gegen Alistair Cockburns Ports-&-Adapters-Konzept, Spring Dependency Injection und ArchUnit-Regeln eingeordnet |
| Kurzentscheidung | Die Domäne bleibt frei von Framework-, Transport-, Persistenz- und Infrastrukturdetails. Kommunikation mit der Außenwelt erfolgt über Ports und Adapter. Abhängigkeiten zeigen nach innen. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Hexagonal Architecture, auch Ports & Adapters genannt, in Java-21- und Spring-Boot-3.x-Systemen sauber eingesetzt wird.

Das Ziel ist nicht, mehr Ordner oder mehr Interfaces zu erzeugen. Das Ziel ist, fachliche Logik gegen technische Details zu schützen. Eine fachliche Entscheidung wie „Eine Bestellung kann nach Zahlungserfassung nicht mehr storniert werden“ darf nicht von HTTP, JPA, Kafka, Redis, Stripe, JSON, Spring oder Datenbankschemas abhängen.

Eine gute Hexagonal Architecture erreicht vier Dinge:

1. Die Domäne ist ohne Spring, Datenbank und HTTP testbar.
2. Technische Adapter können ausgetauscht werden, ohne die Fachlogik zu ändern.
3. Use Cases sind klar sichtbar.
4. Die Abhängigkeitsrichtung zeigt nach innen: Außen hängt vom Kern ab, nicht umgekehrt.

---

## 2. Kurzregel

In fachlich relevanten Services liegt die Geschäftslogik im inneren Kern. Der Kern kennt keine Spring-, JPA-, HTTP-, Kafka-, Redis-, Cloud- oder Datenbankdetails. Inbound Adapter übersetzen externe Eingaben in Use-Case-Kommandos. Outbound Adapter implementieren Ports zu Datenbanken, externen APIs, Message Brokern oder Dateien. Ports sind fachliche Verträge, keine technischen Kopien von Framework-Schnittstellen.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- neue fachlich komplexe Microservices,
- Services mit mehreren externen Integrationen,
- Services mit langlebiger Domänenlogik,
- fachliche Kernmodule in SaaS-Plattformen,
- Module mit hoher Testbarkeitsanforderung,
- Systeme mit möglichen Adapterwechseln,
- Anwendungen mit komplexen Use Cases,
- Services, bei denen Business-Regeln aktuell in Controllern, JPA-Entities oder technischen Services verstreut sind.

Diese Richtlinie gilt nicht zwingend für:

- sehr kleine CRUD-Services ohne nennenswerte Domänenlogik,
- administrative Hilfstools,
- Prototypen,
- einfache interne Konfigurationsendpunkte,
- Wegwerfskripte,
- technische Adapter ohne fachlichen Kern.

Auch in einfachen Services gilt jedoch: Controller sollen keine Geschäftslogik enthalten, Entities sollen nicht direkt als API-Vertrag dienen und technische Details sollen nicht unnötig in Fachlogik eindringen.

---

## 4. Technischer Hintergrund

Hexagonal Architecture wurde von Alistair Cockburn als Ports-&-Adapters-Architektur beschrieben. Die zentrale Idee ist, die Anwendung über Ports mit der Außenwelt zu verbinden, sodass unterschiedliche Adapter denselben Port bedienen können. Ein Port kann etwa durch einen REST-Controller, einen CLI-Adapter, einen Testadapter oder einen Messaging-Adapter angesprochen werden. Ebenso kann ein ausgehender Port durch einen SQL-Adapter, einen Dateiadapter, einen Mock-Adapter oder einen externen API-Adapter implementiert werden.

Der Begriff „hexagonal“ bedeutet nicht, dass es genau sechs Seiten oder sechs Schichten geben muss. Die Form soll verdeutlichen: Die Anwendung hat mehrere Außenseiten, über die sie mit unterschiedlichen Technologien verbunden werden kann.

Im Vergleich zur klassischen Schichtenarchitektur ist der wichtigste Unterschied die Abhängigkeitsrichtung. Klassische Schichtenarchitektur wird häufig so gebaut:

```text
Controller → Service → Repository → Datenbank
```

In einer schlechten Variante sickern technische Details nach innen:

```text
Domain → JPA
Domain → Spring
Service → HttpServletRequest
Use Case → KafkaTemplate
```

Hexagonal Architecture dreht die Richtung:

```text
REST Adapter      ┐
Kafka Adapter     ├──> Inbound Port → Application/Domain Core → Outbound Port <── JPA Adapter
CLI Adapter       ┘                                             <── Stripe Adapter
Test Adapter                                                     <── Redis Adapter
```

Der Kern definiert, was er braucht. Die Außenwelt implementiert diese Bedürfnisse.

---

## 5. Verbindlicher Standard

Für Services, die diese Richtlinie anwenden, gelten folgende Regeln:

1. Die Domäne darf keine Spring-Annotationen enthalten.
2. Die Domäne darf keine JPA-Annotationen enthalten, wenn ein reines Domänenmodell gewählt wurde.
3. Die Domäne darf keine HTTP-, Kafka-, Redis-, JDBC-, Cloud-, Servlet- oder Framework-Typen kennen.
4. Inbound Ports beschreiben fachliche Use Cases.
5. Outbound Ports beschreiben fachliche Abhängigkeiten des Kerns.
6. Adapter übersetzen zwischen technischer Außenwelt und fachlichem Kern.
7. API-DTOs, Persistence-Entities und Domain-Modelle sind getrennte Modelle, sofern die Domäne nicht trivial ist.
8. Mapping findet im Adapter oder in dedizierten Mappern statt.
9. Transaktionsgrenzen liegen im Application Layer oder in der Adapter-Orchestrierung, nicht in Domain-Objekten.
10. ArchUnit-Regeln sichern die Abhängigkeitsrichtung.
11. Domain-Tests laufen ohne Spring Context.
12. Integrationstests prüfen Adapter gegen echte Infrastruktur oder Testcontainers.
13. Ausnahmen müssen im Pull Request begründet werden.

---

## 6. Begriffe

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Domain Core | Fachlicher Kern ohne Frameworkabhängigkeit | `Order`, `Money`, `OrderPolicy` |
| Inbound Port | Eingang in das System, meist Use-Case-Schnittstelle | `PlaceOrderUseCase` |
| Outbound Port | Vom Kern benötigte externe Fähigkeit | `OrderRepositoryPort`, `PaymentGatewayPort` |
| Inbound Adapter | Übersetzt externe Eingabe in Use-Case-Aufruf | REST-Controller, Kafka-Consumer, CLI |
| Outbound Adapter | Implementiert ausgehenden Port mit Technik | JPA, HTTP-Client, S3, Redis |
| Application Service | Orchestriert Use Case, Transaktion, Ports | `PlaceOrderApplicationService` |
| Domain Service | Fachliche Logik, die nicht natürlich in ein Entity/Value Object passt | `PricingPolicy` |
| DTO | Transportmodell für API oder Messaging | `CreateOrderRequest` |
| Persistence Entity | Datenbankmodell | `OrderJpaEntity` |
| Domain Entity | Fachliches Modell mit Invarianten | `Order` |

---

## 7. Zielpaketstruktur

Eine mögliche Struktur:

```text
com.example.order
├── domain
│   ├── model
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── OrderId.java
│   │   ├── UserId.java
│   │   └── Money.java
│   ├── service
│   │   └── OrderPricingService.java
│   └── exception
│       └── OrderCannotBeCancelledException.java
│
├── application
│   ├── port
│   │   ├── in
│   │   │   ├── PlaceOrderUseCase.java
│   │   │   └── CancelOrderUseCase.java
│   │   └── out
│   │       ├── LoadOrderPort.java
│   │       ├── SaveOrderPort.java
│   │       └── ChargePaymentPort.java
│   └── service
│       ├── PlaceOrderApplicationService.java
│       └── CancelOrderApplicationService.java
│
├── adapter
│   ├── in
│   │   ├── rest
│   │   │   ├── OrderController.java
│   │   │   ├── CreateOrderRequest.java
│   │   │   └── OrderResponse.java
│   │   └── messaging
│   │       └── OrderCommandConsumer.java
│   └── out
│       ├── persistence
│       │   ├── OrderJpaEntity.java
│       │   ├── SpringDataOrderRepository.java
│       │   ├── JpaOrderRepositoryAdapter.java
│       │   └── OrderPersistenceMapper.java
│       └── payment
│           ├── StripePaymentAdapter.java
│           └── StripePaymentClient.java
│
└── config
    └── OrderModuleConfiguration.java
```

Diese Struktur ist ein Beispiel, kein Dogma. Entscheidend ist die Abhängigkeitsregel, nicht der exakte Ordnername.

---

## 8. Schlechte Anwendung: Framework-Details im Kern

```java
@Entity
@Table(name = "orders")
@Service
@Transactional
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void cancel(HttpServletRequest request) {
        var userId = request.getHeader("X-User-Id");

        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }

        this.status = OrderStatus.CANCELLED;
        kafkaTemplate.send("order-events", new OrderCancelledEvent(id, userId));
    }
}
```

Warum ist das schlecht?

- Die Domain-Klasse ist zugleich JPA-Entity, Spring-Service und Event-Publisher.
- HTTP-Header werden in fachlicher Logik gelesen.
- Kafka ist direkt im Domain-Objekt.
- Die Klasse ist ohne Spring, JPA und Kafka nicht sinnvoll testbar.
- Fachregel, Persistenz, Transport und Integration sind vermischt.
- Änderungen an Kafka oder HTTP können Fachlogik brechen.
- Die Invarianten sind nicht klar isoliert.

---

## 9. Gute Anwendung: Domain Core ohne Framework

```java
public final class Order {

    private final OrderId id;
    private final UserId customerId;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(OrderId id, UserId customerId, List<OrderItem> items) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.items = List.copyOf(items);
        this.status = OrderStatus.PENDING;

        if (this.items.isEmpty()) {
            throw new EmptyOrderException(id);
        }
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id, status);
        }

        if (status == OrderStatus.CANCELLED) {
            throw new OrderCannotBeCancelledException(id, status);
        }

        this.status = OrderStatus.CANCELLED;
    }

    public Money total() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(), Money::add);
    }

    public OrderId id() {
        return id;
    }

    public UserId customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return items;
    }
}
```

Diese Klasse ist normales Java. Sie kennt kein Spring, kein JPA, kein HTTP, keine Datenbank, keinen Message Broker und keinen Cloud-Provider. Genau deshalb ist sie schnell und isoliert testbar.

---

## 10. Inbound Port: Use Case als Vertrag

Inbound Ports beschreiben, was die Anwendung fachlich anbietet.

```java
public interface PlaceOrderUseCase {

    PlaceOrderResult placeOrder(PlaceOrderCommand command);

    record PlaceOrderCommand(
            UserId customerId,
            List<OrderItem> items,
            Address shippingAddress
    ) {
        public PlaceOrderCommand {
            Objects.requireNonNull(customerId);
            items = List.copyOf(items);
            Objects.requireNonNull(shippingAddress);

            if (items.isEmpty()) {
                throw new IllegalArgumentException("items must not be empty");
            }
        }
    }

    record PlaceOrderResult(
            OrderId orderId,
            Money total,
            OrderStatus status
    ) {}
}
```

Regeln für Inbound Ports:

- Sie enthalten keine HTTP-Typen.
- Sie enthalten keine JPA-Entities.
- Sie enthalten keine Spring-spezifischen Annotationen.
- Sie beschreiben fachliche Kommandos und Ergebnisse.
- Sie sind stabiler als Adaptermodelle.
- Sie sollen aus Consumer-Sicht verständlich sein.

---

## 11. Outbound Port: Abhängigkeit des Kerns

Outbound Ports beschreiben, welche externen Fähigkeiten der Kern benötigt.

```java
public interface SaveOrderPort {
    void save(Order order);
}
```

```java
public interface LoadOrderPort {
    Optional<Order> load(OrderId orderId);
}
```

```java
public interface ChargePaymentPort {
    PaymentResult charge(UserId customerId, Money amount);
}
```

Regeln für Outbound Ports:

- Der Port gehört zur Anwendung oder Domäne, nicht zum technischen Adapter.
- Der Port spricht in Fachsprache.
- Der Port gibt keine JPA-Entities zurück.
- Der Port gibt keine HTTP-Responses zurück.
- Der Port versteckt technische Details wie SQL, REST, Kafka oder Redis.
- Der Port sollte so klein sein wie möglich.

---

## 12. Application Service: Use Case orchestrieren

```java
public class PlaceOrderApplicationService implements PlaceOrderUseCase {

    private final SaveOrderPort saveOrderPort;
    private final ChargePaymentPort chargePaymentPort;
    private final OrderIdGenerator orderIdGenerator;

    public PlaceOrderApplicationService(
            SaveOrderPort saveOrderPort,
            ChargePaymentPort chargePaymentPort,
            OrderIdGenerator orderIdGenerator) {
        this.saveOrderPort = saveOrderPort;
        this.chargePaymentPort = chargePaymentPort;
        this.orderIdGenerator = orderIdGenerator;
    }

    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        var order = new Order(
                orderIdGenerator.nextId(),
                command.customerId(),
                command.items()
        );

        var payment = chargePaymentPort.charge(command.customerId(), order.total());

        if (!payment.succeeded()) {
            throw new PaymentFailedException(payment.reason());
        }

        saveOrderPort.save(order);

        return new PlaceOrderResult(order.id(), order.total(), order.status());
    }
}
```

Der Application Service orchestriert den Use Case. Er darf Ports verwenden. Er darf Transaktionsgrenzen tragen, wenn er als Spring Bean im Application Layer konfiguriert wird. Er darf aber keine HTTP-Request-Objekte, JPA-Entities oder technischen Clients direkt kennen.

---

## 13. Spring-Konfiguration ohne Spring im Domain Core

Eine Möglichkeit ist, den Application Service als Spring Bean in einer Konfigurationsklasse zu verdrahten.

```java
@Configuration
class OrderApplicationConfiguration {

    @Bean
    PlaceOrderUseCase placeOrderUseCase(
            SaveOrderPort saveOrderPort,
            ChargePaymentPort chargePaymentPort,
            OrderIdGenerator orderIdGenerator) {

        return new PlaceOrderApplicationService(
                saveOrderPort,
                chargePaymentPort,
                orderIdGenerator
        );
    }
}
```

Vorteil: `PlaceOrderApplicationService` selbst braucht keine Spring-Annotation. Das ist für strikte Hexagonal Architecture besonders sauber.

Alternativ kann der Application Service mit `@Service` annotiert werden, wenn das Team Spring im Application Layer erlaubt. Die Domäne bleibt trotzdem frei von Spring.

---

## 14. Inbound Adapter: REST Controller

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderRestMapper mapper;

    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           OrderRestMapper mapper) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var command = mapper.toCommand(request);
        var result = placeOrderUseCase.placeOrder(command);
        return mapper.toResponse(result);
    }
}
```

Der Controller übersetzt HTTP nach Anwendungssprache. Er entscheidet nicht fachlich. Er validiert Transportgrenzen, Authentifizierung und API-Formate, aber er enthält keine Kernregeln wie „Bestellung kann nur im Status PENDING storniert werden“.

---

## 15. REST Request/Response Modelle

```java
public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotEmpty List<CreateOrderItemRequest> items,
        @NotNull ShippingAddressRequest shippingAddress
) {}
```

```java
public record OrderResponse(
        String orderId,
        String status,
        BigDecimal total,
        String currency
) {}
```

Diese Modelle gehören zum Inbound Adapter. Sie sind API-Vertrag, nicht Domain-Modell.

---

## 16. REST Mapper

```java
@Component
public class OrderRestMapper {

    public PlaceOrderUseCase.PlaceOrderCommand toCommand(CreateOrderRequest request) {
        return new PlaceOrderUseCase.PlaceOrderCommand(
                new UserId(request.customerId()),
                request.items().stream()
                        .map(this::toOrderItem)
                        .toList(),
                toAddress(request.shippingAddress())
        );
    }

    public OrderResponse toResponse(PlaceOrderUseCase.PlaceOrderResult result) {
        return new OrderResponse(
                result.orderId().value().toString(),
                result.status().name(),
                result.total().amount(),
                result.total().currency().getCurrencyCode()
        );
    }

    private OrderItem toOrderItem(CreateOrderItemRequest request) {
        return new OrderItem(
                new ProductId(request.productId()),
                new Quantity(request.quantity()),
                Money.of(request.unitPrice(), request.currency())
        );
    }

    private Address toAddress(ShippingAddressRequest request) {
        return new Address(
                request.street(),
                request.zipCode(),
                request.city(),
                request.countryCode()
        );
    }
}
```

Der Mapper ist bewusst explizit. Bei größeren Projekten kann MapStruct genutzt werden. Der Mapper bleibt Adapter-Code.

---

## 17. Outbound Adapter: Persistence mit JPA

```java
@Repository
class JpaOrderRepositoryAdapter implements SaveOrderPort, LoadOrderPort {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    JpaOrderRepositoryAdapter(SpringDataOrderRepository repository,
                              OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(Order order) {
        repository.save(mapper.toEntity(order));
    }

    @Override
    public Optional<Order> load(OrderId orderId) {
        return repository.findById(orderId.value())
                .map(mapper::toDomain);
    }
}
```

```java
interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
```

```java
@Entity
@Table(name = "orders")
class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    protected OrderJpaEntity() {
        // JPA
    }

    // Getter/Setter oder package-private Mutatoren nur im Adapterpaket
}
```

Die JPA-Entity ist Persistence-Modell. Sie gehört nicht in die Domain. Sie darf JPA-Annotationen haben, aber keine fachlichen Use-Case-Entscheidungen enthalten.

---

## 18. Persistence Mapper

```java
@Component
class OrderPersistenceMapper {

    OrderJpaEntity toEntity(Order order) {
        var entity = new OrderJpaEntity();
        entity.setId(order.id().value());
        entity.setCustomerId(order.customerId().value());
        entity.setStatus(order.status());
        return entity;
    }

    Order toDomain(OrderJpaEntity entity) {
        return Order.reconstitute(
                new OrderId(entity.getId()),
                new UserId(entity.getCustomerId()),
                entity.getStatus()
        );
    }
}
```

Für Rehydration aus der Datenbank kann eine benannte Factory wie `reconstitute(...)` verwendet werden. Dadurch bleibt sichtbar, dass ein bestehendes Aggregat geladen wird und nicht neu erstellt wird.

---

## 19. Outbound Adapter: externer Payment Provider

```java
@Component
class StripePaymentAdapter implements ChargePaymentPort {

    private final StripeClient stripeClient;
    private final StripePaymentMapper mapper;

    StripePaymentAdapter(StripeClient stripeClient,
                         StripePaymentMapper mapper) {
        this.stripeClient = stripeClient;
        this.mapper = mapper;
    }

    @Override
    public PaymentResult charge(UserId customerId, Money amount) {
        var request = mapper.toStripeRequest(customerId, amount);
        var response = stripeClient.charge(request);
        return mapper.toPaymentResult(response);
    }
}
```

Der Adapter versteckt Stripe-spezifische Objekte. Der Kern sieht nur `ChargePaymentPort` und `PaymentResult`.

---

## 20. Transaktionsgrenzen

Transaktionen gehören nicht in Domain-Objekte.

Nicht erlaubt:

```java
public class Order {

    @Transactional
    public void cancel() {
        // ...
    }
}
```

Besser:

```java
@Service
public class CancelOrderApplicationService implements CancelOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;

    @Transactional
    @Override
    public void cancel(CancelOrderCommand command) {
        var order = loadOrderPort.load(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        order.cancel();

        saveOrderPort.save(order);
    }
}
```

Der Application Service koordiniert Transaktion und Ports. Die Domain schützt die Fachregel.

---

## 21. Domain Events

Domain Events dürfen im Kern fachlich entstehen, aber technische Veröffentlichung passiert im Adapter oder Application Layer.

```java
public final class Order {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id, status);
        }

        this.status = OrderStatus.CANCELLED;
        domainEvents.add(new OrderCancelled(id));
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

```java
@Service
class CancelOrderApplicationService implements CancelOrderUseCase {

    private final DomainEventPublisherPort eventPublisher;

    @Transactional
    public void cancel(CancelOrderCommand command) {
        var order = loadOrder(command.orderId());
        order.cancel();
        saveOrderPort.save(order);

        order.pullDomainEvents()
                .forEach(eventPublisher::publish);
    }
}
```

Der Port `DomainEventPublisherPort` kann später durch Kafka, Outbox, Spring Events oder In-Memory-Testadapter implementiert werden.

Für zuverlässige Event-Verarbeitung in verteilten Systemen ist ein Transactional-Outbox-Pattern zu prüfen.

---

## 22. Teststrategie

### 22.1 Domain-Test ohne Spring

```java
class OrderTest {

    @Test
    void cancel_setsStatusToCancelled_whenOrderIsPending() {
        var order = Order.pending(new OrderId(UUID.randomUUID()), new UserId(1L), validItems());

        order.cancel();

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_throwsException_whenOrderIsDelivered() {
        var order = Order.delivered(new OrderId(UUID.randomUUID()), new UserId(1L), validItems());

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderCannotBeCancelledException.class);
    }
}
```

### 22.2 Application-Service-Test mit Fake Ports

```java
class PlaceOrderApplicationServiceTest {

    @Test
    void placeOrder_chargesPaymentAndSavesOrder() {
        var saveOrderPort = new InMemorySaveOrderPort();
        var chargePaymentPort = new FakeChargePaymentPort(PaymentResult.success("tx-1"));
        var idGenerator = () -> new OrderId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        var service = new PlaceOrderApplicationService(
                saveOrderPort,
                chargePaymentPort,
                idGenerator
        );

        var result = service.placeOrder(validCommand());

        assertThat(result.orderId().value())
                .isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(saveOrderPort.savedOrders()).hasSize(1);
    }
}
```

### 22.3 Adapter-Integrationstest

```java
@DataJpaTest
@Testcontainers
class JpaOrderRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    SpringDataOrderRepository springDataRepository;

    @Test
    void saveAndLoad_roundtripsDomainOrder() {
        var adapter = new JpaOrderRepositoryAdapter(
                springDataRepository,
                new OrderPersistenceMapper()
        );

        var order = Order.pending(new OrderId(UUID.randomUUID()), new UserId(1L), validItems());

        adapter.save(order);

        var loaded = adapter.load(order.id());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().status()).isEqualTo(order.status());
    }
}
```

---

## 23. Security- und SaaS-Aspekte

### 23.1 Tenant-Isolation als Port-Vertrag

In SaaS-Systemen dürfen Ports keine Mandantengrenzen verstecken.

Schlecht:

```java
Optional<Order> load(OrderId orderId);
```

Wenn Orders tenantgebunden sind, fehlt hier der Sicherheitskontext.

Besser:

```java
Optional<Order> loadForTenant(OrderId orderId, TenantId tenantId);
```

Oder:

```java
Optional<Order> load(OrderId orderId, AccessScope scope);
```

Das erzwingt, dass jeder Adapter den Tenant-Filter technisch berücksichtigt.

### 23.2 Autorisierung nicht im REST-Controller verstecken

Schlecht:

```java
if (request.userId().equals(authentication.getName())) {
    placeOrderUseCase.placeOrder(command);
}
```

Besser:

```java
@PreAuthorize("@orderAuthorization.canPlaceOrder(#request.customerId, authentication)")
@PostMapping
public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
    return mapper.toResponse(placeOrderUseCase.placeOrder(mapper.toCommand(request)));
}
```

Noch besser für fachliche Berechtigungen: Application Service prüft zusätzlich invariantnahe Regeln über einen Authorization Port oder Domain Policy.

### 23.3 Keine PII in Ports, wenn technische ID reicht

Ports sollten fachlich notwendig sein, aber keine unnötigen personenbezogenen Daten transportieren.

Schlecht:

```java
PaymentResult charge(String email, String fullName, Money amount);
```

Besser:

```java
PaymentResult charge(CustomerPaymentReference reference, Money amount);
```

### 23.4 Adapter validieren technische Eingaben

Inbound Adapter validieren Transportformate:

- JSON-Form
- Pflichtfelder
- Formatgrenzen
- Größenlimits
- Dateitypen
- Header
- Authentifizierung

Domain und Application Layer validieren fachliche Invarianten:

- Statusübergänge
- Mandantengrenzen
- Berechtigungen
- Geschäftsregeln
- Konsistenz

---

## 24. Wann Hexagonal Architecture sinnvoll ist

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Domänenlogik | Fachregeln sind komplex oder langlebig | Bestellung, Vertrag, Abrechnung | Sinnvoll |
| Mehrere Adapter | REST, Messaging, Batch, CLI greifen auf gleiche Use Cases zu | Order Import + REST API | Sinnvoll |
| Austauschbare Infrastruktur | Payment Provider, Datenbank, Messaging können wechseln | Stripe zu Adyen | Sinnvoll |
| Testbarkeit | Fachlogik soll ohne Spring/DB testbar sein | Domain Tests in ms | Sinnvoll |
| SaaS-Security | Tenant-/Berechtigungsregeln müssen zentral bleiben | Multi-Tenant Order Service | Sinnvoll |
| Einfache CRUD-Verwaltung | Wenig Fachlogik, direkte Tabellenpflege | Admin Lookup Tables | Möglicherweise Overengineering |
| Kurzlebiger Prototyp | Erkenntnisgewinn wichtiger als Langzeitwartung | Spike | Nicht zwingend |
| Technischer Adapter ohne Kern | dünner Proxy | reine Weiterleitung | Nicht zwingend |

---

## 25. Anti-Patterns

### 25.1 Ordner-Hexagon ohne Abhängigkeitsregel

```text
domain/
application/
adapter/
```

Ordner allein sind keine Architektur. Wenn `domain` trotzdem `JpaRepository`, `HttpServletRequest` oder `KafkaTemplate` kennt, ist die Architektur verletzt.

### 25.2 Port als Kopie eines technischen Repositories

```java
public interface OrderRepositoryPort {
    Page<OrderJpaEntity> findAll(Pageable pageable);
    OrderJpaEntity save(OrderJpaEntity entity);
}
```

Das ist kein fachlicher Port. Das ist Spring Data durch ein Interface gespiegelt.

Besser:

```java
public interface LoadOrdersPort {
    List<OrderSummary> loadOpenOrdersForTenant(TenantId tenantId, OrderLimit limit);
}
```

### 25.3 Zu viele Ports

Nicht jeder Methodenaufruf braucht ein eigenes Interface. Ports sollen fachliche Grenzen beschreiben, nicht jede Hilfsmethode abstrahieren.

### 25.4 Domain kennt technische Exceptions

```java
catch (DataIntegrityViolationException e) {
    throw new DuplicateOrderException();
}
```

Spring-Exceptions gehören in Adapter oder Application Layer, nicht in die Domain.

### 25.5 Mapper-Explosion

Wenn jedes Feld dreimal ohne fachlichen Nutzen gemappt wird, kann die Architektur zu schwer werden. Dann prüfen:

- Ist die Domäne wirklich komplex?
- Ist getrenntes Persistence-Modell nötig?
- Reicht ein einfacheres Moduldesign?
- Können Mapper automatisiert oder vereinfacht werden?

### 25.6 Tests nur noch mit Mocks

Hexagonal Architecture ermöglicht schnelle Tests, aber zu viele Mocks können Verhalten nur spiegeln. Für Ports sind Fakes oft besser als Mocks, wenn sie fachliches Verhalten stabiler ausdrücken.

---

## 26. ArchUnit-Regeln

### 26.1 Domain kennt keine Adapter

```java
@AnalyzeClasses(packages = "com.example.order")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_adapters =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..")
                    .because("Die Domäne darf keine Adapter kennen.");
}
```

### 26.2 Domain kennt kein Spring

```java
@ArchTest
static final ArchRule domain_must_not_depend_on_spring =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("Die Domäne muss ohne Spring testbar bleiben.");
```

### 26.3 Domain kennt kein JPA

```java
@ArchTest
static final ArchRule domain_must_not_depend_on_jpa =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.persistence..")
                .because("JPA ist Persistenztechnologie und gehört in den Adapter.");
```

### 26.4 Adapter dürfen Ports implementieren

```java
@ArchTest
static final ArchRule outbound_adapters_should_implement_ports =
        classes()
                .that().resideInAPackage("..adapter.out..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..application.port.out..")
                .because("Outbound Adapter implementieren ausgehende Ports.");
```

ArchUnit-Regeln müssen projektspezifisch angepasst werden. Sie sollen echte Architekturgrenzen schützen, nicht nur Namen erzwingen.

---

## 27. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Domain-Unabhängigkeit | Kennt die Domain keine Frameworks? | kein Spring/JPA/HTTP | Pflicht |
| Ports | Beschreiben Ports fachliche Verträge? | `ChargePaymentPort` | Pflicht |
| Adapter | Übersetzen Adapter sauber zwischen Technik und Fachlichkeit? | REST → Command | Pflicht |
| Mapping | Ist Mapping explizit und testbar? | RestMapper, PersistenceMapper | Pflicht |
| Transaktionen | Liegen Transaktionen außerhalb der Domain? | Application Service | Pflicht |
| Tests | Laufen Domain-Tests ohne Spring? | JUnit only | Pflicht |
| SaaS | Sind Tenant-Grenzen in Ports sichtbar? | `loadForTenant` | Pflicht |
| Security | Wird Autorisierung nicht nur im Controller versteckt? | Method Security + Use Case Check | Pflicht |
| Overengineering | Ist Hexagonal Architecture für diesen Service angemessen? | kein CRUD-Overkill | Prüfen |
| ArchUnit | Werden Abhängigkeitsregeln automatisiert geprüft? | `domain_must_not_depend_on_spring` | Pflicht bei neuen Modulen |

---

## 28. Automatisierbare Prüfungen

Mögliche Prüfungen:

```text
- domain darf nicht von org.springframework.. abhängen.
- domain darf nicht von jakarta.persistence.. abhängen.
- domain darf nicht von jakarta.servlet.. abhängen.
- domain darf nicht von adapter.. abhängen.
- adapter.in.rest darf nicht direkt auf adapter.out.persistence zugreifen.
- Controller dürfen nur Inbound Ports oder Application Services verwenden.
- JPA-Entities dürfen nicht im package domain liegen.
- API-DTOs dürfen nicht im package domain liegen.
- Ports dürfen keine technischen Framework-Typen enthalten.
- Outbound Adapter müssen im adapter.out-Paket liegen.
```

---

## 29. Migration bestehender Schichtenarchitektur

Migration erfolgt schrittweise:

1. Fachliche Kernregeln identifizieren.
2. Controller- und Repository-Code von fachlicher Logik trennen.
3. Use Cases benennen.
4. Inbound Ports für Use Cases einführen.
5. Outbound Ports für technische Abhängigkeiten einführen.
6. Existing Repositories als Adapter hinter Ports stellen.
7. API-DTOs von Domain-Modellen trennen.
8. JPA-Entities von Domain-Modellen trennen, wenn Domänenkomplexität es rechtfertigt.
9. Domain-Tests ohne Spring Context schreiben.
10. ArchUnit-Regeln einführen.
11. Migration in kleinen Pull Requests durchführen.

Vorher:

```java
@RestController
class OrderController {

    @Autowired
    OrderRepository repository;

    @PostMapping("/orders/{id}/cancel")
    void cancel(@PathVariable Long id) {
        var order = repository.findById(id).orElseThrow();

        if (order.getStatus() == DELIVERED) {
            throw new IllegalStateException();
        }

        order.setStatus(CANCELLED);
        repository.save(order);
    }
}
```

Nachher:

```java
@RestController
class OrderController {

    private final CancelOrderUseCase cancelOrderUseCase;

    @PostMapping("/orders/{id}/cancellations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@PathVariable UUID id) {
        cancelOrderUseCase.cancel(new CancelOrderCommand(new OrderId(id)));
    }
}
```

```java
public class CancelOrderApplicationService implements CancelOrderUseCase {

    public void cancel(CancelOrderCommand command) {
        var order = loadOrderPort.load(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        order.cancel();

        saveOrderPort.save(order);
    }
}
```

---

## 30. Ausnahmen

Ausnahmen sind zulässig, wenn:

- der Service bewusst einfaches CRUD ohne relevante Domänenlogik abbildet,
- ein Modul nur ein technischer Adapter ist,
- ein Legacy-Service schrittweise migriert wird,
- Framework-Vorgaben eine bestimmte Struktur erzwingen,
- Performance- oder Mappingkosten nachweislich unverhältnismäßig sind,
- ein Team ein anderes, dokumentiertes Architekturmodell mit gleicher Abhängigkeitsdisziplin nutzt.

Ausnahmen müssen im Pull Request oder in der technischen Dokumentation begründet werden. Eine Ausnahme darf nicht dazu führen, dass Security-, Tenant- oder fachliche Invarianten in Controllern oder technischen Adaptern verstreut werden.

---

## 31. Definition of Done

Ein Modul erfüllt diese Richtlinie, wenn:

1. der fachliche Kern keine Spring-, JPA-, HTTP-, Kafka-, Redis- oder Cloud-Typen kennt,
2. Use Cases als Inbound Ports oder klar benannte Application Services sichtbar sind,
3. technische Abhängigkeiten über Outbound Ports gekapselt sind,
4. Adapter zwischen Außenwelt und Kern übersetzen,
5. API-DTOs nicht als Domain-Modell missbraucht werden,
6. Persistence-Entities nicht ungeprüft als Domain-Modell missbraucht werden,
7. Transaktionsgrenzen außerhalb der Domain liegen,
8. Domain-Tests ohne Spring Context laufen,
9. Adapter-Tests mit geeigneter Infrastruktur geprüft werden,
10. SaaS-/Tenant-Grenzen in Ports oder Application Services sichtbar sind,
11. ArchUnit-Regeln die Abhängigkeitsrichtung schützen,
12. die Architektur nicht mehr Komplexität erzeugt als sie reduziert.

---

## 32. Quellen und weiterführende Literatur

- Alistair Cockburn — Hexagonal Architecture: https://alistair.cockburn.us/hexagonal-architecture
- Alistair Cockburn — Hexagonal Architecture Explained: https://alistaircockburn.com/figs%20hexarch%20book.pdf
- Martin Fowler / Badri Janakiraman — Hexagonal Rails: https://martinfowler.com/articles/badri-hexagonal/
- Spring Framework Reference — Dependency Injection: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- ArchUnit User Guide: https://www.archunit.org/userguide/html/000_Index.html
- Vaughn Vernon — Implementing Domain-Driven Design, weiterführend für Aggregate, Ports und fachliche Grenzen
- Robert C. Martin — Clean Architecture, weiterführend zur Dependency Rule
