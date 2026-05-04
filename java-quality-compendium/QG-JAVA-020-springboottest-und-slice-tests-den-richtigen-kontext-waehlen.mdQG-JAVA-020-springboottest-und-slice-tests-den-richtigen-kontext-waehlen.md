# QG-JAVA-020 — `@SpringBootTest` und Slice-Tests: Den richtigen Kontext wählen

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-020 |
| Titel | `@SpringBootTest` und Slice-Tests: Den richtigen Kontext wählen |
| Status | Accepted / verbindlicher Standard für Spring-Boot-Testdesign |
| Version | 1.0.0 |
| Datum | 2023-09-28 |
| Review-Datum | 2026-05-01 |
| Kategorie | Testing / Spring Boot / Testarchitektur |
| Zielgruppe | Java-Entwickler, Tech Leads, QA, Reviewer, Architektur |
| Java-Baseline | Java 21 |
| Test-Baseline | JUnit Jupiter 5.10+, AssertJ, Mockito |
| Framework-Baseline | Spring Boot 3.x, Spring Framework 6.x, Spring Security Test, Spring Data JPA, Testcontainers |
| Geltungsbereich | Unit-Tests, Spring Slice-Tests, Repository-Tests, Controller-Tests, JSON-Tests, Integrationstests und Full-Stack-Tests in Spring-Boot-Anwendungen |
| Verbindlichkeit | Neue Tests MÜSSEN den kleinsten sinnvollen Testkontext verwenden. `@SpringBootTest` ist begründungspflichtig, wenn ein Slice-Test oder Unit-Test ausreicht. |
| Technische Validierung | Gegen Spring Boot Testing, Spring TestContext Context Caching, Spring Boot Testcontainers, Spring Framework `@MockitoBean` und JUnit 5 validiert |
| Kurzentscheidung | Unit-Test zuerst. Slice-Test für fokussierte Spring-Integration. `@SpringBootTest` nur, wenn das Zusammenspiel des vollständigen Application Contexts wirklich getestet werden soll. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Spring-Boot-Tests so ausgewählt und strukturiert werden, dass sie schnell, aussagekräftig, stabil und wartbar bleiben.

Viele Spring-Projekte entwickeln mit der Zeit eine gefährliche Testgewohnheit: Für fast alles wird `@SpringBootTest` verwendet. Das ist bequem, aber teuer. `@SpringBootTest` lädt den vollständigen Application Context, also Konfiguration, Beans, Auto-Konfiguration, Security, Datenquellen, Messaging-Konfigurationen, Scheduler, Clients und oft weitere Infrastruktur. Für einen Controller-Test, einen Repository-Test oder einen Jackson-Serialisierungstest ist das in den meisten Fällen zu viel.

Das Ergebnis sind langsame Builds, fragile Tests, unnötige Mocks, Context-Cache-Misses und eine Test-Suite, die Entwickler lokal nicht mehr gern ausführen. Genau das schwächt Qualität.

Diese Richtlinie setzt deshalb einen klaren Standard: **So wenig Spring-Kontext wie möglich, so viel Integration wie nötig.**

---

## 2. Kurzregel

Verwende für fachliche Logik reine Unit-Tests ohne Spring. Verwende Slice-Tests, wenn genau ein Spring-Teilbereich geprüft werden soll, zum Beispiel Web, JPA, JSON oder Redis. Verwende `@SpringBootTest` nur, wenn der vollständige Application Context, echte Auto-Konfiguration, mehrere Schichten oder ein echter HTTP-Stack Bestandteil der zu prüfenden Aussage sind. Ein langsamer Test braucht eine starke fachliche oder technische Begründung.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Controller-Tests
- Security-Tests
- Repository-Tests
- JPA-/Hibernate-Tests
- JSON-Serialisierungs- und Deserialisierungstests
- Service-Tests
- Application-Service-Tests
- Integrationstests mit Testcontainers
- Full-Stack-HTTP-Tests
- Tests mit Spring Security
- Tests mit Actuator, OpenAPI, Jackson, Redis, Kafka oder Messaging
- CI/CD-Testpyramiden für Spring-Boot-Services

Diese Richtlinie gilt nicht für reine Bibliotheksprojekte ohne Spring oder für bewusst explorative Spike-Tests. Sobald ein Test dauerhaft im Projekt bleibt, muss er sinnvoll eingeordnet werden.

---

## 4. Technischer Hintergrund

Spring Boot bietet unterschiedliche Testebenen.

Die wichtigste Unterscheidung lautet:

```text
┌─────────────────────────────────────────────────────────────┐
│ Wenige Tests: @SpringBootTest                                │
│ Voller Application Context, echte Auto-Konfiguration,        │
│ optional echter HTTP-Stack, häufig Testcontainers            │
├─────────────────────────────────────────────────────────────┤
│ Mittlere Anzahl: Slice-Tests                                 │
│ @WebMvcTest, @DataJpaTest, @JsonTest, @DataRedisTest,        │
│ @RestClientTest, @WebFluxTest                                │
├─────────────────────────────────────────────────────────────┤
│ Viele Tests: Unit-Tests ohne Spring                          │
│ @ExtendWith(MockitoExtension.class), normale JUnit-Tests,    │
│ Domain-Tests, Value-Object-Tests, Mapper-Tests                │
└─────────────────────────────────────────────────────────────┘
```

Spring Boot dokumentiert Slice-Tests für fokussierte Teile der Anwendung. `@WebMvcTest` ist zum Testen von Spring-MVC-Controllern gedacht. `@DataJpaTest` fokussiert JPA-Komponenten. `@JsonTest` fokussiert Jackson-Serialisierung und -Deserialisierung. `@SpringBootTest` lädt dagegen einen vollständigen Spring-Boot-Anwendungskontext.

Spring TestContext nutzt Context Caching. Das bedeutet: Ein einmal geladener Application Context kann für weitere Tests wiederverwendet werden, wenn die Konfiguration gleich bleibt. Unterschiedliche Mock-Beans, Profile, Properties, Konfigurationen oder Testklassen-Setups können neue Contexts erzeugen und die Suite drastisch verlangsamen.

---

## 5. Verbindlicher Standard

Für neue Tests gilt folgende Reihenfolge:

1. **Reiner Unit-Test ohne Spring**, wenn keine Spring-Funktionalität getestet wird.
2. **Slice-Test**, wenn ein klar abgegrenzter Spring-Bereich getestet wird.
3. **Integrationstest mit Testcontainers**, wenn echte Infrastruktur relevant ist.
4. **`@SpringBootTest`**, wenn das Zusammenspiel mehrerer Schichten, Auto-Konfigurationen oder der vollständige Runtime-Kontext relevant ist.

`@SpringBootTest` darf nicht verwendet werden, nur weil es bequem ist.

Ein Pull Request mit neuem `@SpringBootTest` muss im Review beantworten:

```text
Warum reicht kein Unit-Test?
Warum reicht kein Slice-Test?
Welche konkrete Integration wird durch den vollen Context geprüft?
Wie wird Testlaufzeit begrenzt?
Wie wird Testdaten-Isolation sichergestellt?
```

---

## 6. Entscheidungsmatrix

| Testziel | Empfohlener Testtyp | Details/Erklärung | Beispiel |
|---|---|---|---|
| Domänenregel prüfen | Unit-Test ohne Spring | Kein Context, schnell, direkt | `OrderTest` |
| Service mit gemockten Ports prüfen | Unit-Test mit Mockito | Nur echte Grenzen mocken | `PlaceOrderServiceTest` |
| Controller-Routing, Validation, JSON, Security prüfen | `@WebMvcTest` | MVC-Slice ohne DB/Service-Beans | `OrderControllerTest` |
| JPA-Query, Mapping, Constraint prüfen | `@DataJpaTest` + Testcontainers | Echte Datenbank, fokussierter JPA-Kontext | `OrderRepositoryTest` |
| Jackson-Serialisierung prüfen | `@JsonTest` | Nur ObjectMapper/Jackson-Kontext | `OrderDtoJsonTest` |
| Redis-Repository oder Cache prüfen | `@DataRedisTest` | Redis-Slice, optional Testcontainers | `CacheRepositoryTest` |
| REST-Client gegen simulierte externe API prüfen | `@RestClientTest` oder WireMock/Testcontainers | HTTP-Client-Konfiguration | `PaymentClientTest` |
| Voller HTTP-Fluss prüfen | `@SpringBootTest(webEnvironment = RANDOM_PORT)` | Echter Server-Port, Security, Controller, Service, DB | `OrderApiIntegrationTest` |
| Anwendung startet mit produktionsnaher Konfiguration | `@SpringBootTest` | Smoke-Test des Contexts | `ApplicationContextTest` |
| Contract gegen Provider prüfen | Pact/Spring Cloud Contract | Kein Ersatz durch `@SpringBootTest` allein | `ProviderContractTest` |

---

## 7. Unit-Tests ohne Spring zuerst

### 7.1 Gute Anwendung

```java
@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

    @Mock
    LoadOrderPort loadOrderPort;

    @Mock
    SaveOrderPort saveOrderPort;

    @InjectMocks
    CancelOrderService service;

    @Test
    void cancel_cancelsOrder_whenOrderIsPending() {
        var order = Order.pending(new OrderId(UUID.randomUUID()), new UserId(1L), validItems());
        when(loadOrderPort.load(order.id())).thenReturn(Optional.of(order));

        service.cancel(new CancelOrderCommand(order.id()));

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(saveOrderPort).save(order);
    }
}
```

Dieser Test braucht keinen Spring Context. Er prüft fachliches Verhalten direkt.

### 7.2 Schlechte Anwendung

```java
@SpringBootTest
class CancelOrderServiceTest {

    @Autowired
    CancelOrderService service;

    @MockitoBean
    LoadOrderPort loadOrderPort;

    @MockitoBean
    SaveOrderPort saveOrderPort;
}
```

Wenn nur zwei Ports gemockt werden und keine Spring-Funktionalität getestet wird, ist `@SpringBootTest` unnötig.

---

## 8. `@WebMvcTest`: Controller fokussiert testen

### 8.1 Zweck

`@WebMvcTest` prüft den Web-Layer. Dazu gehören typischerweise:

- Request Mapping
- HTTP-Methode und URL
- Request-/Response-JSON
- Bean Validation
- Controller Advice
- Security Filter Chain im Web-Kontext
- Jackson-Konfiguration im MVC-Kontext
- Status-Codes und Header

Nicht geprüft werden:

- echte Services
- echte Repositories
- echte Datenbank
- Messaging
- vollständige Anwendung

### 8.2 Gute Anwendung mit modernem `@MockitoBean`

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PlaceOrderUseCase placeOrderUseCase;

    @Test
    @WithMockUser(roles = "USER")
    void createOrder_returns201_whenRequestIsValid() throws Exception {
        var request = new CreateOrderRequest(
                1L,
                List.of(new CreateOrderItemRequest("P1", 2)),
                new ShippingAddressRequest("Hauptstraße 1", "26789", "Leer", "DE")
        );

        when(placeOrderUseCase.placeOrder(any()))
                .thenReturn(new PlaceOrderResult(
                        new OrderId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                        Money.eur(new BigDecimal("99.98")),
                        OrderStatus.PENDING
                ));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithAnonymousUser
    void createOrder_returns401_whenUserIsAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createOrder_returns400_whenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }
}
```

### 8.3 Hinweis zu `@MockBean` und `@MockitoBean`

In Spring Boot 3.4+ ist `@MockBean` als deprecated markiert und soll zugunsten von Spring Frameworks `@MockitoBean` verwendet werden. Für ältere Spring-Boot-3.x-Versionen kann `@MockBean` noch notwendig sein. Neue Projekte auf Spring Boot 3.4+ sollen `@MockitoBean` verwenden.

### 8.4 Schlechte Anwendung

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
    // Lädt Web, JPA, Services, Scheduler, Messaging, Security, Datenquelle ...
}
```

Wenn nur Controller-Verhalten geprüft wird, ist das zu breit.

---

## 9. `@DataJpaTest`: Repository und JPA fokussiert testen

### 9.1 Zweck

`@DataJpaTest` prüft JPA-bezogene Integration:

- Entity Mapping
- Repository Queries
- JPQL
- Native Queries
- Constraints
- Transaktionen
- Flyway-/Liquibase-Migrationen, je nach Projektsetup
- N+1-Verhalten
- Optimistic Locking
- Datenbank-spezifisches Verhalten

### 9.2 Gute Anwendung mit Testcontainers

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @Sql("/test-data/orders.sql")
    void findByUserIdAndStatus_returnsOnlyMatchingOrders() {
        var orders = orderRepository.findByUserIdAndStatus(new UserId(1L), OrderStatus.PENDING);

        assertThat(orders)
                .hasSize(2)
                .allMatch(order -> order.status() == OrderStatus.PENDING)
                .allMatch(order -> order.userId().equals(new UserId(1L)));
    }

    @Test
    void save_assignsIdAndPersistsStatus() {
        var order = OrderJpaEntity.pendingFor(1L);

        var saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        var loaded = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getId()).isNotNull();
        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
```

### 9.3 Regel zu H2

Wenn Produktion PostgreSQL, MySQL, Oracle oder SQL Server nutzt, darf H2 nicht als Standard für produktionsrelevante Repository-Tests verwendet werden. H2 ist nur zulässig für einfache Smoke-Tests oder technische Tests ohne produktionsspezifisches SQL-Verhalten.

---

## 10. `@JsonTest`: JSON-Vertrag isoliert testen

### 10.1 Zweck

`@JsonTest` prüft Jackson-Serialisierung und -Deserialisierung ohne Webserver und ohne vollständigen Spring Context.

Typische Ziele:

- Datumsformat
- Enum-Format
- Feldnamen
- `@JsonProperty`
- `@JsonIgnore`
- Custom Serializer
- Custom Deserializer
- keine internen Felder in Responses
- keine sensitiven Felder in JSON

### 10.2 Gute Anwendung

```java
@JsonTest
class OrderResponseJsonTest {

    @Autowired
    JacksonTester<OrderResponse> json;

    @Test
    void serialize_writesExpectedJsonFields() throws Exception {
        var response = new OrderResponse(
                "00000000-0000-0000-0000-000000000001",
                "PENDING",
                new BigDecimal("99.98"),
                "EUR",
                Instant.parse("2024-06-15T10:30:00Z")
        );

        var content = json.write(response);

        assertThat(content).extractingJsonPathStringValue("$.orderId")
                .isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(content).extractingJsonPathStringValue("$.createdAt")
                .isEqualTo("2024-06-15T10:30:00Z");
        assertThat(content).doesNotHaveJsonPath("$.internalState");
    }

    @Test
    void deserialize_readsCreateOrderRequest() throws Exception {
        var content = """
                {
                  "customerId": 1,
                  "items": [
                    { "productId": "P1", "quantity": 2 }
                  ],
                  "shippingAddress": {
                    "street": "Hauptstraße 1",
                    "zipCode": "26789",
                    "city": "Leer",
                    "countryCode": "DE"
                  }
                }
                """;

        var request = json.parse(content).getObject();

        assertThat(request.customerId()).isEqualTo(1L);
        assertThat(request.items()).hasSize(1);
    }
}
```

---

## 11. Weitere relevante Slice-Tests

### 11.1 `@RestClientTest`

Geeignet für REST-Clients, die externe HTTP-Services aufrufen.

```java
@RestClientTest(PaymentClient.class)
class PaymentClientTest {

    @Autowired
    PaymentClient paymentClient;

    @Autowired
    MockRestServiceServer server;

    @Test
    void authorize_returnsAccepted_whenProviderAcceptsPayment() {
        server.expect(requestTo("/payments"))
                .andRespond(withSuccess("""
                        { "status": "ACCEPTED", "transactionId": "tx-123" }
                        """, MediaType.APPLICATION_JSON));

        var result = paymentClient.authorize(validCommand());

        assertThat(result.status()).isEqualTo(PaymentStatus.ACCEPTED);
    }
}
```

### 11.2 `@DataRedisTest`

Geeignet für Redis-Repositories oder Redis-bezogene Serialisierung.

```java
@DataRedisTest
@Testcontainers
class TokenRepositoryTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2-alpine")
                    .withExposedPorts(6379);

    @Autowired
    TokenRepository tokenRepository;

    @Test
    void saveAndFind_roundtripsToken() {
        tokenRepository.save(new TokenEntity("token-id", "user-1"));

        assertThat(tokenRepository.findById("token-id")).isPresent();
    }
}
```

### 11.3 `@WebFluxTest`

Für reactive WebFlux-Controller, falls der Service wirklich WebFlux nutzt.

Nicht verwenden, wenn die Anwendung Spring MVC ist.

---

## 12. Wann `@SpringBootTest` richtig ist

`@SpringBootTest` ist richtig, wenn der vollständige Application Context Teil der Testaussage ist.

Typische Fälle:

- Anwendung startet mit echter Konfiguration.
- Security, Controller, Service und Repository sollen zusammen geprüft werden.
- Ein echter HTTP-Stack ist erforderlich.
- Testcontainers werden für echte Infrastruktur genutzt.
- Auto-Konfigurationen müssen zusammenwirken.
- Mehrere Spring-Slices interagieren.
- Actuator-Konfiguration wird geprüft.
- Observability-/Tracing-Konfiguration wird geprüft.
- Komplexe Bean-Verkabelung muss validiert werden.

### 12.1 Full-Stack-Test mit echtem HTTP-Port

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
class OrderApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @Sql("/test-data/user-and-product.sql")
    @Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void placeOrder_persistsOrderAndReturns201() {
        var request = new CreateOrderRequest(
                1L,
                List.of(new CreateOrderItemRequest("P1", 2)),
                new ShippingAddressRequest("Hauptstraße 1", "26789", "Leer", "DE")
        );

        var response = restTemplate.postForEntity(
                "/api/v1/orders",
                request,
                OrderResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().orderId()).isNotBlank();
    }
}
```

### 12.2 Context-Smoke-Test

```java
@SpringBootTest
class ApplicationContextTest {

    @Test
    void contextStarts() {
        // Absichtlich leer: Der Test schlägt fehl, wenn der Context nicht startet.
    }
}
```

Dieser Test ist erlaubt, aber er darf nicht zum Ersatz für fachliche Tests werden.

---

## 13. Context Caching: Performance bewusst schützen

Spring TestContext cached Application Contexts statisch. Tests mit gleicher Context-Konfiguration können dadurch denselben Context wiederverwenden. Das spart viel Zeit.

Context Caching wird geschwächt durch:

- unterschiedliche aktive Profile
- unterschiedliche Test-Properties
- unterschiedliche Mock-Bean-Konfigurationen
- unterschiedliche `@Import`-Konfigurationen
- häufiges `@DirtiesContext`
- unnötig viele `@SpringBootTest`-Varianten
- Forking im Build-Tool

### 13.1 Schlechte Anwendung: jeder Test hat andere Mocks

```java
@SpringBootTest
class OrderFlowTest {

    @MockitoBean
    PaymentGateway paymentGateway;
}
```

```java
@SpringBootTest
class ShippingFlowTest {

    @MockitoBean
    ShippingGateway shippingGateway;
}
```

Wenn beide Tests sonst denselben Context hätten, können unterschiedliche Mock-Konfigurationen zusätzliche Contexts erzeugen.

### 13.2 Bessere Anwendung: gemeinsame Testbasis für echte Systemtests

```java
@SpringBootTest
@ActiveProfiles("integration-test")
abstract class ApplicationIntegrationTestBase {

    @MockitoBean
    PaymentGateway paymentGateway;

    @MockitoBean
    ShippingGateway shippingGateway;
}
```

Nicht übertreiben: Eine gemeinsame Basisklasse mit zu vielen Mocks kann Tests unklar machen. Für viele Fälle ist ein Slice-Test oder Unit-Test besser.

### 13.3 `@DirtiesContext` vermeiden

`@DirtiesContext` entfernt den Context aus dem Cache. Das ist nur erlaubt, wenn ein Test den Context tatsächlich verändert und dadurch Folgetests gefährdet.

Nicht verwenden als allgemeines Reparaturmittel.

---

## 14. Moderne Mockito-Integration im Spring Context

### 14.1 `@MockitoBean`

Für Spring Framework 6.2+ und Spring Boot 3.4+ ist `@MockitoBean` der bevorzugte Weg, eine Bean im Test-ApplicationContext durch einen Mockito-Mock zu ersetzen.

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockitoBean
    PlaceOrderUseCase placeOrderUseCase;
}
```

### 14.2 `@MockBean` als Legacy

In Spring Boot 3.4+ ist `@MockBean` deprecated und für Entfernung in Spring Boot 4.0 vorgesehen. Für Projekte auf Spring Boot 3.2 oder 3.3 kann `@MockBean` weiterhin notwendig sein.

Projektregel:

```text
Spring Boot 3.4+  → @MockitoBean
Spring Boot <=3.3 → @MockBean zulässig
```

### 14.3 Kein Spring-Mock für reine Unit-Tests

Nicht:

```java
@SpringBootTest
class PriceCalculatorTest {

    @MockitoBean
    TaxService taxService;
}
```

Besser:

```java
@ExtendWith(MockitoExtension.class)
class PriceCalculatorTest {

    @Mock
    TaxService taxService;

    @InjectMocks
    PriceCalculator priceCalculator;
}
```

---

## 15. Testdaten und Transaktionen

### 15.1 `@DataJpaTest`

`@DataJpaTest` ist typischerweise transaktional und rollt Änderungen nach jedem Test zurück.

```java
@DataJpaTest
class RepositoryTest {
    // Rollback nach Testmethode
}
```

### 15.2 `@SpringBootTest(webEnvironment = RANDOM_PORT)`

Bei echtem HTTP-Stack läuft der Request in der Anwendung, nicht einfach im Testmethoden-Thread. Ein `@Transactional` am Test garantiert nicht automatisch, dass Datenbankänderungen aus dem HTTP-Request zurückgerollt werden.

Deshalb gilt:

- SQL-Fixtures gezielt einspielen.
- Cleanup-Skripte nutzen.
- Testdaten eindeutig machen.
- Nicht auf magischen Rollback verlassen.

---

## 16. Security-Tests

### 16.1 Controller-Security mit `@WebMvcTest`

```java
@WebMvcTest(OrderController.class)
class OrderControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PlaceOrderUseCase placeOrderUseCase;

    @Test
    @WithAnonymousUser
    void createOrder_returns401_whenAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createOrder_returns400_whenAuthenticatedButInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
```

### 16.2 Method Security braucht häufig mehr Context

Wenn `@PreAuthorize` auf Service-Methoden geprüft werden soll, reicht ein reiner Unit-Test nicht immer. Dann ist ein fokussierter Spring-Test mit aktivierter Method Security sinnvoll.

```java
@SpringBootTest
class OrderMethodSecurityIntegrationTest {

    @Autowired
    OrderService orderService;

    @Test
    @WithMockUser(roles = "USER")
    void cancel_throwsAccessDenied_whenUserLacksPermission() {
        assertThatThrownBy(() -> orderService.cancelOrder(new OrderId(UUID.randomUUID())))
                .isInstanceOf(AccessDeniedException.class);
    }
}
```

Wenn nur die Autorisierungslogik selbst geprüft wird, ist ein Unit-Test des Authorization Service besser.

---

## 17. SaaS- und Tenant-Aspekte

Slice- und Integrationstests müssen Mandantengrenzen sichtbar machen.

### 17.1 Controller-Test für Tenant Header

```java
@WebMvcTest(OrderController.class)
class OrderControllerTenantTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PlaceOrderUseCase placeOrderUseCase;

    @Test
    @WithMockUser
    void findOrder_returns400_whenTenantHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }
}
```

### 17.2 Repository-/Integrationstest für Tenant-Isolation

```java
@DataJpaTest
@Testcontainers
class OrderTenantRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    OrderRepository orderRepository;

    @Test
    @Sql("/test-data/orders-two-tenants.sql")
    void findByIdAndTenant_returnsEmpty_forOtherTenant() {
        var result = orderRepository.findByIdAndTenantId(
                new OrderId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                new TenantId("tenant-b")
        );

        assertThat(result).isEmpty();
    }
}
```

---

## 18. Anti-Patterns

### 18.1 `@SpringBootTest` für alles

```java
@SpringBootTest
class EmailAddressTest {
}
```

Value Objects und Domain-Objekte brauchen keinen Spring Context.

### 18.2 Slice-Test mit zu vielen Mocks

Wenn ein `@WebMvcTest` zehn `@MockitoBean`s braucht, ist der Controller vermutlich zu breit oder der Test prüft zu viel.

### 18.3 `@DirtiesContext` als Standard

`@DirtiesContext` ist teuer und zerstört Context Caching.

### 18.4 Unterschiedliche Properties pro Testklasse ohne Grund

```java
@SpringBootTest(properties = "feature.a=true")
class TestA {}

@SpringBootTest(properties = "feature.a=false")
class TestB {}
```

Das kann mehrere Contexts erzwingen. Für Varianten sind oft Unit-Tests mit Properties-Objekten oder `ApplicationContextRunner` besser.

### 18.5 `@SpringBootTest` mit `@MockBean`/`@MockitoBean` als Unit-Test-Ersatz

Wenn fast alle Abhängigkeiten gemockt werden, ist der Test meistens ein langsamer Unit-Test.

### 18.6 H2 in `@DataJpaTest` trotz PostgreSQL-Produktion

Das testet oft die falsche Datenbank.

### 18.7 Kein Security-Test im Web Slice

Wenn Controller Security-Regeln hat, muss mindestens ein positiver und ein negativer Security-Pfad getestet werden.

### 18.8 Testprofile als versteckte Produktlogik

Profile dürfen Testinfrastruktur konfigurieren, aber keine fachlichen Produktregeln verfälschen.

---

## 19. Gute Praxis: Testpyramide pro Modul

Für ein typisches Spring-Boot-Modul gilt als Richtwert:

```text
Viele:
- Domain-Tests ohne Spring
- Value-Object-Tests
- Mapper-Tests
- Service-Tests mit Mockito/Fakes

Einige:
- @WebMvcTest für Controller
- @DataJpaTest für Repositories
- @JsonTest für DTO-Verträge
- Security-Slice-Tests

Wenige:
- @SpringBootTest für End-to-End-Flüsse
- Context-Smoke-Tests
- Actuator-/Observability-/Full-Stack-Tests
```

Richtwert bedeutet nicht starre Quote. Ein reiner API-Gateway-Service braucht andere Tests als ein Datenbankservice.

---

## 20. `ApplicationContextRunner` für Auto-Konfiguration und Properties

Für eigene Auto-Konfigurationen, Conditional Beans und Feature-Flag-Konfiguration ist `ApplicationContextRunner` oft besser als `@SpringBootTest`.

```java
class MailSenderConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(MailSenderConfiguration.class);

    @Test
    void createsLoggingMailSender_whenMockSenderEnabled() {
        contextRunner
                .withPropertyValues("features.mail.mock-sender-enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingMailSender.class);
                    assertThat(context).doesNotHaveBean(SmtpMailSender.class);
                });
    }
}
```

Dieser Test startet einen kleinen, kontrollierten Context statt der vollständigen Anwendung.

---

## 21. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Kleinster Kontext | Wurde der kleinste sinnvolle Testtyp gewählt? | Unit vor Slice vor Full Context | Pflicht |
| `@SpringBootTest` begründet | Ist klar, warum der vollständige Context nötig ist? | echter HTTP-Fluss | Pflicht |
| Slice passend | Passt der Slice zum Testziel? | Controller → `@WebMvcTest` | Pflicht |
| Mock-Anzahl | Sind nicht zu viele Beans gemockt? | >5 Mocks prüfen | Prüfen |
| Context Cache | Vermeidet der Test unnötige Context-Varianten? | keine unnötigen Properties | Pflicht |
| `@DirtiesContext` | Wird es nur begründet eingesetzt? | Context wirklich verändert | Pflicht |
| Datenbank | Nutzt Repository-Test echte DB via Testcontainers? | PostgreSQLContainer | Pflicht bei produktionsnahen Queries |
| Security | Sind positive und negative Security-Pfade getestet? | 401/403/200 | Pflicht |
| SaaS | Sind Tenant-Grenzen sichtbar geprüft? | Cross-Tenant-Test | Pflicht bei SaaS |
| Testdaten | Sind Testdaten isoliert und synthetisch? | `.example.test` | Pflicht |
| Laufzeit | Bleibt lokale Ausführung praktikabel? | schnelle Unit-/Slice-Tests | Pflicht |
| Moderne Mocking-Annotation | Bei Boot 3.4+ `@MockitoBean` statt `@MockBean`? | `@MockitoBean` | Pflicht bei neuen Projekten |

---

## 22. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Tests mit Namen *ControllerTest dürfen nicht automatisch @SpringBootTest verwenden.
- Tests mit Namen *RepositoryTest sollen @DataJpaTest oder klaren Integrationstest verwenden.
- @SpringBootTest-Klassen müssen @Tag("integration") tragen, wenn sie echten Context oder Infrastruktur laden.
- @DirtiesContext darf nur mit Begründungskommentar verwendet werden.
- Keine H2-Dependency in Integrationstestprofilen produktionsnaher Datenbankservices.
- Bei Spring Boot 3.4+ keine neuen @MockBean-Verwendungen.
- @SpringBootTest(webEnvironment = RANDOM_PORT) muss Testdaten-Cleanup haben, wenn DB geschrieben wird.
```

Beispielhafte ArchUnit-/Reflektionsprüfung:

```java
@ArchTest
static final ArchRule controller_tests_should_not_use_spring_boot_test =
        noClasses()
                .that().haveSimpleNameEndingWith("ControllerTest")
                .should().beAnnotatedWith(SpringBootTest.class)
                .because("Controller-Tests sollen bevorzugt @WebMvcTest verwenden.");
```

Diese Regel muss projektspezifisch angepasst werden, weil es legitime API-Integrationstests geben kann.

---

## 23. Migration bestehender Tests

Migration erfolgt schrittweise:

1. Alle `@SpringBootTest`-Klassen inventarisieren.
2. Testziel je Klasse bestimmen: Unit, Web, JPA, JSON, Full-Stack.
3. Reine Service-Tests auf JUnit + Mockito/Fakes umstellen.
4. Controller-Tests auf `@WebMvcTest` umstellen.
5. Repository-Tests auf `@DataJpaTest` + Testcontainers umstellen.
6. JSON-Vertragstests auf `@JsonTest` umstellen.
7. Full-Stack-Tests bewusst behalten und taggen.
8. `@DirtiesContext` reduzieren.
9. Unterschiedliche Mock-Konfigurationen konsolidieren.
10. Bei Spring Boot 3.4+ `@MockBean` schrittweise durch `@MockitoBean` ersetzen.
11. CI-Laufzeiten messen und verbessern.

Vorher:

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
}
```

Nachher:

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
}
```

Vorher:

```java
@SpringBootTest
class EmailAddressTest {
}
```

Nachher:

```java
class EmailAddressTest {
}
```

---

## 24. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Test explizit Application-Context-Startfähigkeit prüft,
- mehrere Spring-Schichten gemeinsam Gegenstand des Tests sind,
- Security-Konfiguration im Zusammenspiel geprüft wird,
- Actuator-/Observability-Konfiguration geprüft wird,
- Auto-Konfigurationen validiert werden,
- ein Legacy-Test noch nicht sinnvoll geschnitten werden kann,
- der Slice-Test durch starke Projektbesonderheiten mehr Aufwand als Nutzen erzeugt.

Ausnahmen müssen im Review begründet werden. Eine Ausnahme darf nicht dazu führen, dass die gesamte Test-Suite langsam und unbrauchbar wird.

---

## 25. Definition of Done

Ein Spring-Test erfüllt diese Richtlinie, wenn:

1. der kleinste sinnvolle Testkontext gewählt wurde,
2. `@SpringBootTest` nur bei echter Full-Context-Anforderung verwendet wird,
3. Controller-Tests bevorzugt `@WebMvcTest` verwenden,
4. Repository-Tests bevorzugt `@DataJpaTest` mit produktionsnaher Datenbank verwenden,
5. JSON-Verträge über `@JsonTest` geprüft werden,
6. fachliche Logik ohne Spring getestet wird,
7. Mocks nicht den gesamten Context entwerten,
8. Context Caching nicht unnötig zerstört wird,
9. `@DirtiesContext` nur begründet eingesetzt wird,
10. Security- und Tenant-Pfade angemessen geprüft sind,
11. Testdaten isoliert, synthetisch und reproduzierbar sind,
12. die Testlaufzeit lokal und in CI akzeptabel bleibt.

---

## 26. Quellen und weiterführende Literatur

- Spring Boot Reference — Testing Spring Boot Applications: https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html
- Spring Boot Reference — Test Slices: https://docs.spring.io/spring-boot/appendix/test-auto-configuration/slices.html
- Spring Boot Reference — Testcontainers: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Spring Framework Reference — Context Caching: https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html
- Spring Framework Reference — `@MockitoBean` und `@MockitoSpyBean`: https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-mockitobean.html
- Spring Boot API — `@MockBean` Deprecation: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/test/mock/mockito/MockBean.html
- Spring Framework Reference — Transaction Management in Tests: https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html
- JUnit 5 User Guide — Tags und Testausführung: https://docs.junit.org/5.10.5/user-guide/
