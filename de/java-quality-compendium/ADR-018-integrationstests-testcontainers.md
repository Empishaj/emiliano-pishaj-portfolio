# ADR-018 — Integrationstests mit Testcontainers

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · JUnit 5.10+ · Testcontainers 1.19+           |
| Datum      | 2024-01-01                                        |
| Kategorie  | Testing / Integration                             |

---

## Kontext & Problem

Unit-Tests prüfen Klassen isoliert. Integrationstests prüfen ob die Teile zusammen korrekt funktionieren: Service + Repository + echte Datenbank, Controller + Security + echter HTTP-Stack. H2-In-Memory-Datenbanken sind kein Ersatz — sie weichen in SQL-Dialekt, Constraints und Verhalten von PostgreSQL ab. Testcontainers starten echte Docker-Container im Test.

---

## Regel 1 — Kein H2, echte Datenbank mit Testcontainers

### ❌ Schlecht — H2 als "Test-Datenbank"

```java
// application-test.yml mit H2:
// spring:
//   datasource:
//     url: jdbc:h2:mem:testdb
//     driver-class-name: org.h2.Driver

// Probleme:
// - PostgreSQL-spezifische Funktionen (jsonb, array, window functions) nicht verfügbar
// - Constraint-Verhalten weicht ab (case sensitivity, FK-Checks)
// - Native Queries schlagen in Produktion fehl, nicht im Test
// - Flyway-Skripte müssen für H2 kompatibel gehalten werden — extra Aufwand
@DataJpaTest  // Startet H2 by default
class OrderRepositoryTest { ... } // Testet gegen falsche Datenbank
```

### ✅ Gut — Testcontainers mit echter PostgreSQL

```java
// Einmalige Basis-Konfiguration — alle Tests erben davon
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration-test")
public abstract class IntegrationTestBase {

    // @Container + static: Container läuft für alle Tests der JVM-Session (shared)
    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Spring Properties dynamisch aus laufendem Container setzen
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}

// Konkreter Test erbt nur — kein Boilerplate
class OrderRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired OrderRepository orderRepository;

    @Test
    @Sql("/test-data/orders.sql") // SQL-Fixture einspielen
    void findWithItems_usesJoinFetch_andReturnsCorrectData() {
        var orders = orderRepository.findWithItemsByUserId(1L);

        assertThat(orders)
            .hasSize(2)
            .allSatisfy(o -> assertThat(o.items()).isNotEmpty());
    }
}
```

---

## Regel 2 — Testcontainers für alle externen Systeme

```java
// Vollständige Basis-Klasse mit mehreren Containern
@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class FullIntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static final KafkaContainer kafka =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

---

## Regel 3 — Testdaten: SQL-Fixtures statt @BeforeEach-Setup-Code

### ❌ Schlecht — Testdaten in Java aufgebaut

```java
@BeforeEach
void setUp() {
    // 30 Zeilen Java für das was 5 Zeilen SQL wären
    var user = new UserEntity();
    user.setName("Max");
    user.setEmail("max@example.com");
    user.setActive(true);
    userRepository.save(user);

    var product = new ProductEntity();
    product.setName("Java Buch");
    product.setPrice(new BigDecimal("49.99"));
    productRepository.save(product);

    var order = new OrderEntity();
    order.setUser(user);
    // ...
    orderRepository.save(order);
}
```

### ✅ Gut — SQL-Fixtures mit `@Sql`

```sql
-- src/test/resources/test-data/order-with-items.sql
INSERT INTO users (id, name, email, active)
VALUES (1, 'Max Mustermann', 'max@example.com', true);

INSERT INTO products (id, name, price)
VALUES (100, 'Java Buch', 49.99);

INSERT INTO orders (id, user_id, status, created_at)
VALUES (1000, 1, 'PENDING', NOW());

INSERT INTO order_items (order_id, product_id, quantity, price)
VALUES (1000, 100, 2, 49.99);
```

```java
@Test
@Sql("/test-data/order-with-items.sql")
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
void findOrder_returnsCorrectDetails() {
    var order = orderRepository.findWithItemsById(1000L).orElseThrow();

    assertThat(order.status()).isEqualTo(PENDING);
    assertThat(order.items()).hasSize(1);
    assertThat(order.items().get(0).quantity().value()).isEqualTo(2);
}
```

---

## Regel 4 — Transaktions-Rollback für Testisolation

```java
// @Transactional auf Testklasse: rollback nach jedem Test — saubere Isolation
@Transactional
class OrderRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired OrderRepository orderRepository;
    @Autowired TestEntityManager em; // Spring-Test-Helfer für JPA-Tests

    @Test
    void save_persistsOrderCorrectly() {
        var order = new OrderEntity(new UserId(1L));
        order.addItem(new ProductId("P1"), new Quantity(3));

        var saved = orderRepository.save(order);
        em.flush(); // Schreibt sofort in DB (noch innerhalb der Test-Transaktion)
        em.clear(); // Leert den First-Level-Cache — erzwingt echten DB-Read

        var loaded = orderRepository.findById(saved.id()).orElseThrow();
        assertThat(loaded.items()).hasSize(1);
    }
    // Nach Test: Transaktion wird zurückgerollt — kein Cleanup nötig
}
```

---

## Konsequenzen

**Positiv:** Tests laufen gegen denselben Datenbanktyp wie Produktion — keine Überraschungen. Container werden einmal pro JVM-Session gestartet (wenn `static`) — akzeptable Build-Zeiten.

**Negativ:** Docker muss auf der CI-Maschine verfügbar sein. Erster Testlauf lädt Docker-Images — initialer Overhead. Lösung: Images in CI vorwärmen oder Testcontainers Cloud nutzen.

---

## 💡 Guru-Tipps

- **Ryuk deaktivieren** in CI-Umgebungen ohne Docker-Socket: `TESTCONTAINERS_RYUK_DISABLED=true`.
- **Testcontainers Reuse**: `.withReuse(true)` behält Container zwischen Test-Runs — drastisch schnellere lokale Entwicklung.
- **`@Sql` mit `executionPhase = BEFORE_TEST_CLASS`** für Referenzdaten die alle Tests brauchen.
- **WireMock als Container**: `WireMockContainer` für HTTP-Mocks externer APIs im Integrationstest.

---

## Verwandte ADRs

- [ADR-025](ADR-025-spring-boot-slice-tests.md) — `@DataJpaTest` für schlanke Repository-Tests ohne Testcontainers.
- [ADR-010](ADR-010-junit-grundlagen-struktur.md) — Testisolation ist Grundprinzip.
