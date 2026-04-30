# ADR-020 — @SpringBootTest & Slice-Tests: Den richtigen Kontext wählen

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x · JUnit 5.10+               |
| Datum      | 2025-08-17                                        |
| Kategorie  | Testing / Spring                                  |

---

## Kontext & Problem

`@SpringBootTest` lädt den gesamten Application Context — alle Beans, alle Konfigurationen, alle Abhängigkeiten. Das dauert 10–30 Sekunden. Für einen Repository-Test der nur JPA braucht, oder einen Controller-Test der nur MVC braucht, ist das Verschwendung. Spring Boot bietet Slice-Tests: fokussierte Kontexte die nur den relevanten Stack laden.

---

## Die Test-Pyramide im Spring-Kontext

```
                    ┌─────────────────────────────────────┐
                    │  @SpringBootTest (RANDOM_PORT)       │  ← Wenige, langsam
                    │  Voller Context + echter HTTP-Stack  │    Integrationstest
                    ├─────────────────────────────────────┤
                    │  @WebMvcTest     @DataJpaTest        │  ← Mittel, schnell
                    │  @JsonTest       @DataRedisTest      │    Slice-Tests
                    ├─────────────────────────────────────┤
                    │  @ExtendWith(MockitoExtension.class) │  ← Viele, sehr schnell
                    │  Kein Spring-Kontext                 │    Unit-Tests
                    └─────────────────────────────────────┘
```

**Grundregel**: So wenig Spring-Kontext wie möglich. Unit-Tests first, Slice-Tests für Integrationspunkte, `@SpringBootTest` nur für echte End-to-End-Szenarien.

---

## Slice 1 — `@WebMvcTest`: Controller isoliert testen

### Schlecht — voller Spring-Kontext für Controller-Test

```java
@SpringBootTest  // Lädt ALLES — JPA, Kafka, Security, alle Services...
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    // 30 Sekunden Startup für einen Controller-Test mit 3 Endpoints
}
```

### Gut — `@WebMvcTest` lädt nur den Web-Layer

```java
// Lädt NUR: Controller, Filter, Security, Jackson, Validation
// NICHT: Services, Repositories, Datenbankverbindungen
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Alle Service-Abhängigkeiten des Controllers werden als @MockBean bereitgestellt
    @MockBean OrderService     orderService;
    @MockBean UserService      userService;

    @Test
    @WithMockUser(roles = "USER") // Spring Security Mock
    void createOrder_returns201_withValidRequest() throws Exception {
        var command = new CreateOrderCommand(new ProductId("P1"), new Quantity(2));
        var response = new OrderCreatedResponse(1L, PENDING, Instant.now());

        when(orderService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(header().string("Location", containsString("/api/orders/1")));
    }

    @Test
    @WithAnonymousUser
    void createOrder_returns401_forUnauthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createOrder_returns400_whenRequestIsInvalid() throws Exception {
        // Leerer Request → Bean Validation schlägt an
        mockMvc.perform(post("/api/orders")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

---

## Slice 2 — `@DataJpaTest`: Repository isoliert testen

```java
// Lädt NUR: JPA, DataSource, Repositories, Flyway/Liquibase
// NICHT: Web-Layer, Services, Security, Kafka
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE) // Eigene Datenbank statt H2!
@Import(TestcontainersConfig.class)        // Testcontainers-Konfiguration
class OrderRepositoryTest {

    @Autowired OrderRepository  orderRepository;
    @Autowired TestEntityManager em; // Direkter JPA-Zugriff für Test-Setup

    @Test
    @Sql("/test-data/orders.sql")
    void findByUserIdAndStatus_returnsOnlyMatchingOrders() {
        var orders = orderRepository.findByUserIdAndStatus(1L, PENDING);

        assertThat(orders)
            .hasSize(2)
            .allMatch(o -> o.status() == PENDING)
            .allMatch(o -> o.userId().value().equals(1L));
    }

    @Test
    void save_assignsIdAndTimestamp() {
        var order = new OrderEntity(new UserId(1L));

        var saved = orderRepository.save(order);
        em.flush();
        em.clear();

        var loaded = orderRepository.findById(saved.id()).orElseThrow();
        assertThat(loaded.id()).isNotNull().isPositive();
        assertThat(loaded.createdAt()).isNotNull();
    }

    @Test
    void findWithItems_noNPlusOneQuery() {
        // Query-Zählung via Hibernate Statistik (→ ADR-016)
        var stats = em.getEntityManager().getEntityManagerFactory()
            .unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        orderRepository.findWithItemsByUserId(1L);

        assertThat(stats.getQueryExecutionCount())
            .as("JOIN FETCH darf nur eine Query erzeugen")
            .isEqualTo(1);
    }
}
```

---

## Slice 3 — `@JsonTest`: JSON-Serialisierung isoliert testen

```java
// Lädt NUR: Jackson, JsonComponent, Serializer/Deserializer
@JsonTest
class OrderDtoJsonTest {

    @Autowired JacksonTester<OrderCreatedResponse> json;

    @Test
    void serialize_formatsInstantAsIso8601() throws Exception {
        var response = new OrderCreatedResponse(
            1L, PENDING, Instant.parse("2024-06-15T10:30:00Z"));

        var content = json.write(response);

        assertThat(content).extractingJsonPathStringValue("$.createdAt")
            .isEqualTo("2024-06-15T10:30:00Z");
        assertThat(content).hasJsonPathNumberValue("$.id");
        assertThat(content).doesNotHaveJsonPath("$.internalField"); // Nicht exponiert
    }

    @Test
    void deserialize_parsesRequestCorrectly() throws Exception {
        var jsonContent = """
            { "productId": "P1", "quantity": 3 }
            """;

        var command = json.parse(jsonContent).getObject();

        assertThat(command.productId().value()).isEqualTo("P1");
        assertThat(command.quantity().value()).isEqualTo(3);
    }
}
```

---

## Wann `@SpringBootTest` — und wie?

```java
// @SpringBootTest NUR wenn der vollständige Stack nötig ist
@SpringBootTest(webEnvironment = RANDOM_PORT) // Echter HTTP-Stack, zufälliger Port
@ActiveProfiles("integration-test")
class OrderEndToEndTest extends IntegrationTestBase { // Testcontainers (→ ADR-018)

    @Autowired TestRestTemplate restTemplate;
    @Autowired OrderRepository  orderRepository;

    @Test
    @Sql("/test-data/users.sql")
    void placeOrder_fullFlow_persistsAndReturns201() {
        var request = new CreateOrderCommand(new ProductId("P1"), new Quantity(2));

        var response = restTemplate
            .withBasicAuth("user@example.com", "password")
            .postForEntity("/api/orders", request, OrderCreatedResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody().id()).isPositive();

        // Prüfen ob wirklich persistiert
        var saved = orderRepository.findById(response.getBody().id()).orElseThrow();
        assertThat(saved.status()).isEqualTo(PENDING);
    }
}
```

---

## Application-Context-Caching: Performance-Falle vermeiden

```java
// Spring cached Application Contexts wenn möglich — NICHT zerstören!

// ❌ Schlecht: @MockBean in @SpringBootTest erzeugt immer neuen Context (Cache-Miss!)
@SpringBootTest
class TestA {
    @MockBean SomeService service; // → neuer Context
}
@SpringBootTest
class TestB {
    @MockBean OtherService other;  // → wieder neuer Context, 30s Startup
}

// ✅ Gut: gemeinsame Basis-Klasse mit allen @MockBeans → ein Context für alle
@SpringBootTest
abstract class SpringBootTestBase {
    @MockBean SomeService  someService;
    @MockBean OtherService otherService;
    // Alle Tests erben → Context wird gecacht und wiederverwendet
}
class TestA extends SpringBootTestBase { ... }
class TestB extends SpringBootTestBase { ... }
```

---

## Konsequenzen

**Positiv:** `@WebMvcTest` startet in 2–3 Sekunden statt 30. `@DataJpaTest` testet Query-Logik ohne Web-Overhead. Context-Caching verhindert wiederholte Startup-Zeiten.

**Negativ:** `@MockBean` in Slice-Tests erfordert, dass alle echten Abhängigkeiten als Mock konfiguriert werden — Aufwand bei vielen Abhängigkeiten.

---

## Tipps

- **`@WebMvcTest` testet Security** — `@SpringBootTest` für Controller ist fast nie nötig.
- **`@DataJpaTest` ist transaktional by default** — Rollback nach jedem Test ohne Konfiguration.
- **`@TestConfiguration`** für Test-spezifische Beans die nicht gemockt werden sollen.
- **`spring.jpa.show-sql=true` in `application-test.yml`**: SQL in Tests sichtbar machen — N+1 sofort erkennen.
 