# QG-JAVA-018 — Integrationstests mit Testcontainers

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-018 |
| Titel | Integrationstests mit Testcontainers |
| Status | Accepted / verbindlicher Standard für neue und wesentlich geänderte Integrationstests |
| Version | 1.0.0 |
| Datum | 2025-04-07 |
| Kategorie | Testing / Integration / Persistenz / DevOps |
| Zielgruppe | Java-Entwickler, Tech Leads, QA, DevOps, Architektur, Plattform-Team |
| Java-Baseline | Java 21 |
| Test-Baseline | JUnit Jupiter 5.10+ |
| Framework-Baseline | Spring Boot 3.x, Spring Data JPA, Hibernate 6, Testcontainers |
| Empfohlene Testcontainers-Baseline | Für Spring-Boot-3.x-Projekte bevorzugt Dependency Management des Projekts verwenden; explizit mindestens Testcontainers 1.21.x. Testcontainers 2.x ist wegen Modul-/Paketumbenennungen als bewusste Migration zu behandeln. |
| Geltungsbereich | Integrationstests gegen echte Infrastruktur wie PostgreSQL, Redis, Kafka, RabbitMQ, LocalStack, WireMock, Elasticsearch/OpenSearch oder andere containerisierte Abhängigkeiten |
| Verbindlichkeit | Persistenz- und Integrationsverhalten MUSS gegen dieselbe Technologieklasse getestet werden, die produktiv eingesetzt wird. H2 ist für PostgreSQL-/MySQL-/Oracle-/SQL-Server-Integrationstests kein gleichwertiger Ersatz. |
| Technische Validierung | Gegen offizielle Testcontainers-, Spring-Boot-Testcontainers-, Spring-Test- und JUnit-5-Dokumentation validiert |
| Kurzentscheidung | Integrationstests verwenden Testcontainers für echte technische Abhängigkeiten, definierte Testdaten, klare Isolation, reproduzierbare Container-Images und CI/CD-taugliche Ausführung. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Java- und Spring-Boot-Anwendungen Integrationstests mit Testcontainers sauber, stabil, reproduzierbar und sicher umsetzen.

Unit-Tests prüfen kleine Einheiten isoliert. Integrationstests prüfen, ob mehrere technische und fachliche Bausteine gemeinsam korrekt funktionieren. Dazu gehören zum Beispiel:

- Controller + Security + HTTP-Stack
- Service + Repository + echte Datenbank
- JPA-Mapping + Flyway-/Liquibase-Migrationen
- Kafka Producer + Kafka Consumer
- Redis Cache + Serialisierung
- HTTP-Client + simulierte externe API
- Datenbank-Constraints + Transaktionen
- Anwendungskonfiguration + Spring Context

Der zentrale Qualitätsgrundsatz lautet: **Integrationstests dürfen nicht gegen eine Ersatztechnologie testen, wenn genau diese Ersatztechnologie produktionsrelevantes Verhalten verdeckt.**

---

## 2. Kurzregel

Für Integrationstests werden echte technische Abhängigkeiten über Testcontainers gestartet. Eine PostgreSQL-Anwendung wird gegen PostgreSQL getestet, ein Kafka-Consumer gegen Kafka, ein Redis-Cache gegen Redis. H2, manuelle Mocks und selbst gebaute Fake-Infrastrukturen sind für produktionsrelevantes Integrationsverhalten nicht ausreichend. Testdaten müssen eindeutig, reproduzierbar, isoliert und frei von produktiven personenbezogenen Daten sein.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Repository-Integrationstests
- JPA-/Hibernate-Tests
- Flyway-/Liquibase-Migrationstests
- REST-API-Integrationstests
- Security-Integrationstests
- Messaging-Integrationstests
- Cache-Integrationstests
- Tests gegen externe Systeme über WireMock oder MockServer
- Tests gegen AWS-ähnliche Dienste über LocalStack
- CI/CD-Gates für technische Integrationsfähigkeit

Diese Richtlinie gilt nicht für reine Unit-Tests, reine Domain-Tests ohne technische Abhängigkeit oder schnelle Mapper-/Validator-Tests. Solche Tests sollen weiterhin ohne Container laufen.

---

## 4. Technischer Hintergrund

Testcontainers ist eine Java-Bibliothek, die Tests mit leichtgewichtigen, wegwerfbaren Docker-Containern unterstützt. Dadurch können Tests gegen echte Datenbanken, Message Broker, Webserver, Browser oder andere containerisierbare Abhängigkeiten laufen.

Spring Boot bietet seit Version 3.1 eine deutlich verbesserte Testcontainers-Integration. `@ServiceConnection` kann Standard-Container wie PostgreSQL, Redis, Kafka oder RabbitMQ automatisch mit Spring-Boot-Auto-Konfiguration verbinden. `@DynamicPropertySource` bleibt eine flexible Alternative, wenn `@ServiceConnection` nicht ausreicht oder spezielle Properties gesetzt werden müssen.

Wichtig ist: Testcontainers verbessert die Realitätsnähe von Integrationstests. Es ersetzt aber nicht sauberes Testdesign, Datenisolation, sinnvolle Testgrenzen, gute Fehlermeldungen oder CI-Ressourcenplanung.

---

## 5. Verbindlicher Standard

Integrationstests MÜSSEN folgende Standards erfüllen:

1. Produktive Datenbanktechnologien werden durch dieselbe Datenbanktechnologie im Container getestet.
2. Datenbankmigrationen laufen im Test gegen den Container.
3. Testcontainer verwenden gepinnte Image-Versionen, nicht `latest`.
4. Container-Konfiguration liegt zentral in Test-Basisklassen oder dedizierter Testkonfiguration.
5. Testdaten sind pro Test oder pro Testklasse klar definiert und isoliert.
6. Tests enthalten keine produktiven Secrets.
7. Tests verwenden keine produktiven personenbezogenen Daten.
8. CI-Systeme müssen Docker-kompatible Runtime oder Testcontainers Cloud bereitstellen.
9. Reuse-Container sind nur für lokale Entwicklung erlaubt, nicht als CI-Standard.
10. Integrationstests sind über Tags oder Build-Profile separat ausführbar.
11. Full-Stack-Tests werden sparsam eingesetzt; Slice-Tests bleiben für fokussierte Szenarien erlaubt.
12. Der Test muss fachliches Verhalten prüfen, nicht nur, ob der Container startet.

---

## 6. Abhängigkeiten und Build-Konfiguration

### 6.1 Maven mit Spring Boot Dependency Management

Für Spring-Boot-Projekte wird bevorzugt die vom Projekt verwaltete Dependency-Version verwendet.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 6.2 Gradle Kotlin DSL

```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
```

### 6.3 Hinweis zu Testcontainers 2.x

Testcontainers 2.x hat Modul- und Paketänderungen eingeführt. Neue 2.x-Artefakte tragen teilweise den Präfix `testcontainers-`, zum Beispiel `testcontainers-postgresql` oder `testcontainers-junit-jupiter`. Eine Migration auf 2.x darf nicht nebenbei erfolgen, sondern benötigt eine bewusste Build- und Importprüfung.

Für Spring-Boot-3.x-Projekte gilt deshalb:

```text
Standard: Spring Boot Dependency Management verwenden.
Migration auf 2.x: eigenes Upgrade-Ticket, Build-Anpassung, Import-Anpassung, CI-Validierung.
```

---

## 7. Kein H2 als Ersatz für produktive Datenbanken

### 7.1 Schlechte Anwendung: H2 als PostgreSQL-Ersatz

```yaml
# ❌ application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

Problematisch ist diese Konfiguration, wenn Produktion PostgreSQL, MySQL, Oracle oder SQL Server nutzt. H2 hat andere SQL-Dialekte, andere Funktionen, andere Constraint-Details und andere Optimizer-Eigenschaften.

Typische verdeckte Fehler:

- PostgreSQL-`jsonb` wird nicht real getestet.
- Native Queries sind H2-kompatibel, aber produktiv falsch.
- Case Sensitivity unterscheidet sich.
- Index- und Constraint-Verhalten weicht ab.
- Flyway-Skripte werden künstlich H2-kompatibel gemacht.
- Locking-, Transaction- und Isolation-Verhalten ist nicht realitätsnah.
- Produktionsfehler erscheinen erst nach Deployment.

### 7.2 Gute Anwendung: PostgreSQLContainer

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration-test")
abstract class IntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine")
                    .withDatabaseName("app_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

Diese Variante ist explizit, robust und funktioniert auch bei nicht direkt unterstützten Spezialkonfigurationen.

---

## 8. Moderne Spring-Boot-Variante mit `@ServiceConnection`

Wenn Spring Boot den verwendeten Container erkennt, ist `@ServiceConnection` häufig die sauberste Variante.

```java
@Testcontainers
@SpringBootTest
class OrderRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    OrderRepository orderRepository;

    @Test
    void findById_returnsOrder_whenOrderExists() {
        var result = orderRepository.findById(1L);

        assertThat(result).isPresent();
    }
}
```

Vorteil: Kein stringbasiertes Setzen von `spring.datasource.url`, `username` und `password`. Spring Boot stellt die Verbindung automatisch her.

`@DynamicPropertySource` bleibt richtig, wenn zusätzliche Properties gesetzt werden müssen oder der Container nicht direkt durch Spring Boot erkannt wird.

---

## 9. Container-Lifecycle richtig wählen

### 9.1 Container pro Testklasse

```java
@Testcontainers
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Test
    void testA() {}

    @Test
    void testB() {}
}
```

Ein statischer `@Container` wird für die Testklasse geteilt. Das ist der Standard für viele Integrationstests.

### 9.2 Container pro Testmethode

```java
@Container
PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16.4-alpine");
```

Ein nicht-statischer Container wird pro Testmethode neu gestartet. Das ist maximal isoliert, aber meistens zu langsam.

### 9.3 Singleton-Container-Pattern

Für große Test-Suiten kann ein einmal gestarteter Container sinnvoll sein:

```java
public abstract class SingletonPostgresTestBase {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16.4-alpine")
                    .withDatabaseName("app_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
```

Wichtig: Das Singleton-Pattern darf nicht unbewusst mit JUnit-`@Container` vermischt werden, wenn dadurch Container früher gestoppt werden als der Spring Context erwartet.

---

## 10. Testdaten sauber bereitstellen

### 10.1 Schlechte Anwendung: komplexes Java-Setup in `@BeforeEach`

```java
@BeforeEach
void setUp() {
    var user = new UserEntity();
    user.setName("Max");
    user.setEmail("max@example.com");
    userRepository.save(user);

    var product = new ProductEntity();
    product.setName("Java Buch");
    product.setPrice(new BigDecimal("49.99"));
    productRepository.save(product);

    var order = new OrderEntity();
    order.setUser(user);
    orderRepository.save(order);
}
```

Dieses Setup ist lang, indirekt und wiederholt häufig produktive Konstruktion statt gezielte Testdaten zu beschreiben.

### 10.2 Gute Anwendung: SQL-Fixtures mit `@Sql`

```sql
-- src/test/resources/test-data/order-with-items.sql
INSERT INTO users (id, name, email, active)
VALUES (1, 'Max Mustermann', 'max@example.test', true);

INSERT INTO products (id, name, price)
VALUES (100, 'Java Buch', 49.99);

INSERT INTO orders (id, user_id, status, created_at)
VALUES (1000, 1, 'PENDING', now());

INSERT INTO order_items (id, order_id, product_id, quantity, price)
VALUES (5000, 1000, 100, 2, 49.99);
```

```java
@Test
@Sql("/test-data/order-with-items.sql")
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
void findOrder_returnsOrderWithItems() {
    var order = orderRepository.findWithItemsById(1000L).orElseThrow();

    assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    assertThat(order.items()).hasSize(1);
}
```

### 10.3 Cleanup

```sql
-- src/test/resources/test-data/cleanup.sql
TRUNCATE TABLE order_items RESTART IDENTITY CASCADE;
TRUNCATE TABLE orders RESTART IDENTITY CASCADE;
TRUNCATE TABLE products RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
```

Cleanup-Skripte müssen zur Datenbank passen. PostgreSQL-`TRUNCATE ... CASCADE` ist nicht portabel, aber genau deshalb ist der Test gegen PostgreSQL wertvoll.

---

## 11. Transaktionen und Rollback in Integrationstests

### 11.1 Repository-Test mit Rollback

```java
@DataJpaTest
@Testcontainers
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
    void save_persistsOrderCorrectly() {
        var order = OrderEntity.pendingFor(new UserId(1L));

        var saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        var loaded = orderRepository.findById(saved.id()).orElseThrow();

        assertThat(loaded.status()).isEqualTo(OrderStatus.PENDING);
    }
}
```

`@DataJpaTest` ist transaktional und rollt Tests typischerweise nach jeder Testmethode zurück. Das eignet sich gut für fokussierte Repository-Tests.

### 11.2 Full-Stack-HTTP-Test: Rollback nicht blind erwarten

Bei `@SpringBootTest(webEnvironment = RANDOM_PORT)` läuft der HTTP-Request häufig in einem anderen Thread und in einer anderen Transaktion als der Test selbst. Ein `@Transactional` am Test garantiert dann nicht automatisch, dass alle Datenbankänderungen des HTTP-Requests zurückgerollt werden.

Für Full-Stack-Tests gilt deshalb:

- Daten vor dem Test explizit einspielen.
- Daten nach dem Test explizit bereinigen.
- Testdaten eindeutig machen.
- Nicht auf magischen Rollback verlassen.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
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
    void createOrder_returns201() {
        var response = restTemplate.postForEntity(
                "/api/v1/orders",
                new CreateOrderRequest(1L, "P100", 2),
                OrderCreatedResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id()).isNotNull();
    }
}
```

---

## 12. Testcontainers für mehrere Abhängigkeiten

```java
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class FullIntegrationTestBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Container
    @ServiceConnection
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2-alpine")
                    .withExposedPorts(6379);

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void registerKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

Container dürfen nicht unkontrolliert in jeder Testklasse neu gestartet werden. Für große Test-Suites wird eine Basisklasse oder dedizierte Testkonfiguration verwendet.

---

## 13. Externe HTTP-APIs testen

Für HTTP-Integrationen werden echte Netzwerkgrenzen simuliert, aber keine echten externen Provider aufgerufen.

Beispiel mit WireMock als Container:

```java
@Testcontainers
@SpringBootTest
class PaymentClientIntegrationTest {

    @Container
    static final GenericContainer<?> wiremock =
            new GenericContainer<>("wiremock/wiremock:3.9.1")
                    .withExposedPorts(8080);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.base-url",
                () -> "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080));
    }

    @Test
    void authorize_returnsAccepted_whenProviderAcceptsPayment() {
        // Stub vorbereiten, Client aufrufen, Response prüfen
    }
}
```

Für Consumer-Provider-Kompatibilität ist zusätzlich Contract Testing zu verwenden. WireMock allein ist kein Contract Testing, wenn der Stub nicht aus einem geteilten Contract stammt.

---

## 14. CI/CD-Ausführung

### 14.1 Voraussetzungen

CI muss bereitstellen:

- Docker-kompatible Runtime oder Testcontainers Cloud
- Zugriff auf benötigte Images
- ausreichend RAM und CPU
- Netzwerkzugriff auf Registry
- Berechtigung für Ryuk oder alternative Cleanup-Strategie
- klare Test-Tags für Integrationstests

### 14.2 GitHub Actions Beispiel

```yaml
name: Integration Tests

on:
  pull_request:
  push:
    branches: [ main ]

jobs:
  integration-tests:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
          cache: maven

      - name: Run integration tests
        run: ./mvnw verify -Pintegration-test
```

### 14.3 Ryuk

Ryuk ist der Resource Reaper von Testcontainers und sorgt für Cleanup. Wenn eine CI-Umgebung keine privilegierten Container erlaubt, kann `TESTCONTAINERS_RYUK_DISABLED=true` nötig sein. Das darf nur geschehen, wenn die Umgebung selbst zuverlässiges Cleanup sicherstellt.

Nicht als Standard setzen:

```bash
TESTCONTAINERS_RYUK_DISABLED=true
```

Nur als begründete CI-Ausnahme verwenden.

---

## 15. Reusable Containers

Testcontainers unterstützt wiederverwendbare Container als experimentelles Feature. Das kann lokale Entwicklung stark beschleunigen, darf aber nicht als CI-Standard verwendet werden.

Lokale Aktivierung:

```properties
# ~/.testcontainers.properties
testcontainers.reuse.enable=true
```

Container:

```java
static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16.4-alpine")
                .withReuse(true);
```

Regeln:

- Nur lokal verwenden.
- Nicht in CI erzwingen.
- Testdaten trotzdem pro Test isolieren.
- Keine Annahme treffen, dass der Container leer startet.
- Keine produktiven Daten in wiederverwendbaren Containern.

---

## 16. Security- und SaaS-Aspekte

### 16.1 Keine produktiven Secrets

Nicht erlaubt:

```java
.withPassword(System.getenv("PROD_DB_PASSWORD"));
```

Integrationstests verwenden Test-Credentials. Diese dürfen keinen Zugriff auf produktive Systeme haben.

### 16.2 Keine produktiven personenbezogenen Daten

Testdaten müssen synthetisch, anonym oder künstlich sein.

Nicht erlaubt:

```sql
INSERT INTO users (email, first_name, last_name)
VALUES ('echter.kunde@example.com', 'Echter', 'Kunde');
```

Erlaubt:

```sql
INSERT INTO users (email, first_name, last_name)
VALUES ('max.mustermann@example.test', 'Max', 'Mustermann');
```

### 16.3 Tenant-Isolation testen

SaaS-Integrationstests müssen Mandantengrenzen prüfen.

```java
@Test
@Sql("/test-data/two-tenants-orders.sql")
void findOrder_returns404_whenOrderBelongsToOtherTenant() {
    var response = restTemplate.exchange(
            "/api/v1/orders/2000",
            HttpMethod.GET,
            requestWithTenant("tenant-a"),
            ProblemDetail.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
}
```

### 16.4 Keine Docker-Socket-Mounts im Anwendungstest

Tests dürfen nicht leichtfertig Container mit Host-Docker-Socket starten. Ein gemounteter Docker Socket ist sicherheitskritisch und kann Host-Zugriff ermöglichen.

### 16.5 Image-Quellen kontrollieren

Container-Images müssen aus vertrauenswürdigen Quellen kommen und gepinnt sein:

```java
new PostgreSQLContainer<>("postgres:16.4-alpine")
```

Nicht:

```java
new PostgreSQLContainer<>("postgres:latest")
```

---

## 17. Performance und Stabilität

### 17.1 Test-Suite aufteilen

Integrationstests sind langsamer als Unit-Tests. Sie werden separat tagbar gemacht:

```java
@Tag("integration")
@SpringBootTest
class OrderApiIntegrationTest {
}
```

Maven Surefire/Failsafe-Trennung:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 17.2 Container-Start minimieren

Gute Regeln:

- Container statisch pro Testklasse oder als Singleton starten.
- Nicht für jeden Test neue Container erzeugen.
- Nur benötigte Container starten.
- Full-Stack-Tests begrenzen.
- Repository-Tests und API-Tests getrennt halten.
- Images in CI cachen oder vorziehen, wenn möglich.

### 17.3 Wait Strategies

Container müssen erst genutzt werden, wenn sie wirklich bereit sind.

```java
new GenericContainer<>("redis:7.2-alpine")
        .withExposedPorts(6379)
        .waitingFor(Wait.forListeningPort());
```

Für HTTP-Services:

```java
new GenericContainer<>("wiremock/wiremock:3.9.1")
        .withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/__admin").forStatusCode(200));
```

---

## 18. Gute Anwendung: Repository-Integrationstest

```java
@DataJpaTest
@Testcontainers
class OrderRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    OrderRepository orderRepository;

    @Test
    @Sql("/test-data/order-with-items.sql")
    void findWithItemsById_returnsOrderWithItems() {
        var order = orderRepository.findWithItemsById(1000L).orElseThrow();

        assertThat(order.items()).hasSize(1);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }
}
```

---

## 19. Gute Anwendung: API-Integrationstest mit Security

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderApiSecurityIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @Sql("/test-data/order-for-tenant-a.sql")
    @Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getOrder_returnsForbidden_whenUserHasWrongTenant() {
        var headers = new HttpHeaders();
        headers.setBearerAuth(testJwtForTenant("tenant-b"));

        var response = restTemplate.exchange(
                "/api/v1/orders/1000",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProblemDetail.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND);
    }
}
```

---

## 20. Anti-Patterns

### 20.1 H2 für PostgreSQL-Integrationstests

H2 ist schnell, aber bei produktionsrelevantem SQL kein Ersatz.

### 20.2 `latest` als Container-Tag

```java
new PostgreSQLContainer<>("postgres:latest")
```

Problem: Tests ändern ihr Verhalten ohne Codeänderung.

### 20.3 Echte Produktionsdaten

Testdaten dürfen keine echten Kundendaten enthalten.

### 20.4 Container pro Testmethode ohne Grund

Maximale Isolation, aber häufig unnötig langsam.

### 20.5 Unklare Testdaten aus globalem Setup

Ein Test muss sichtbar machen, welche Daten sein Szenario benötigt.

### 20.6 Integrationstest als Ersatz für Unit-Test

Nicht jede Klasse braucht Spring Context und Container. Fachlogik bleibt bevorzugt ohne Container testbar.

### 20.7 Tests gegen externe Live-Systeme

Tests dürfen nicht von Stripe, realer Mail, produktiver Kafka-Instanz oder echter AWS-Umgebung abhängen.

### 20.8 Magisches Rollback in Full-Stack-Tests erwarten

HTTP-basierte Full-Stack-Tests benötigen explizite Datenbereinigung.

---

## 21. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Richtige Technologie | Wird dieselbe Datenbanktechnologie wie produktiv genutzt? | PostgreSQLContainer für PostgreSQL | Pflicht |
| Image-Version | Ist das Image gepinnt? | `postgres:16.4-alpine` | Pflicht |
| Keine H2-Ersatztests | Wird H2 nur dort genutzt, wo es fachlich ungefährlich ist? | keine JPA-Produktionsqueries gegen H2 | Pflicht |
| Container-Lifecycle | Wird der Container sinnvoll geteilt? | statisch pro Klasse oder Singleton | Pflicht |
| Properties | Werden Verbindungsdaten sauber über `@ServiceConnection` oder `@DynamicPropertySource` gesetzt? | keine hardcodierten Ports | Pflicht |
| Testdaten | Sind Testdaten eindeutig und reproduzierbar? | `@Sql`, Builder, Fixtures | Pflicht |
| Cleanup | Werden Daten nach Tests bereinigt? | Rollback oder Cleanup-SQL | Pflicht |
| Security | Keine echten Secrets und keine echten PII? | `.example.test`, Testpasswörter | Pflicht |
| SaaS | Werden Tenant-Grenzen getestet? | Cross-Tenant-Zugriff | Pflicht bei SaaS |
| CI | Läuft der Test in CI mit Docker-kompatibler Runtime? | GitHub Actions, Runner | Pflicht |
| Performance | Startet die Suite nicht unnötig viele Container? | geteilte Container | Pflicht |
| Aussagekraft | Prüft der Test Verhalten, nicht nur Startfähigkeit? | Repository-Ergebnis, HTTP-Status | Pflicht |

---

## 22. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Keine H2-Dependency in Integrationstest-Profilen für Services mit PostgreSQL/MySQL/Oracle-Produktion.
- Keine Container-Images mit :latest.
- Keine produktiven Domains oder echten E-Mail-Adressen in test/resources.
- Keine Umgebungsvariablen mit PROD_* in Tests.
- Integrationstests müssen @Tag("integration") tragen.
- Tests mit @SpringBootTest müssen begründen, warum kein Slice-Test reicht.
- SQL-Fixtures dürfen keine produktiven Kundendaten enthalten.
```

Beispielhafte ArchUnit-Idee:

```java
@ArchTest
static final ArchRule integration_tests_should_be_tagged =
        classes()
                .that().haveSimpleNameEndingWith("IntegrationTest")
                .should().beAnnotatedWith(Tag.class)
                .because("Integrationstests müssen im Build separat steuerbar sein.");
```

---

## 23. Migration bestehender H2-Tests

Migration erfolgt in sechs Schritten:

1. Produktionsdatenbanktechnologie identifizieren.
2. Testcontainers-Dependency ergänzen.
3. H2-Testprofil entfernen oder klar auf nicht-produktive Tests begrenzen.
4. Container-Basisklasse oder `@ServiceConnection` einführen.
5. Flyway-/Liquibase-Migrationen gegen Container laufen lassen.
6. H2-spezifische SQL-Anpassungen entfernen.
7. Testdaten als SQL-Fixtures oder Builder neu strukturieren.
8. CI-Runtime für Testcontainers aktivieren.
9. Performance beobachten und Container-Lifecycle optimieren.

Vorher:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
```

Nachher:

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16.4-alpine");
```

---

## 24. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Test reine Domainlogik prüft,
- ein Test keine technische Integration berührt,
- ein Legacy-Service noch nicht CI-fähig für Docker ist,
- eine Technologie nicht sinnvoll containerisiert werden kann,
- ein sehr schneller Smoke-Test bewusst ohne Container läuft,
- eine spezielle Embedded-Lösung exakt produktiv identisch ist.

Die Ausnahme muss im Pull Request begründet werden. Für produktionsrelevante Datenbank-, Messaging-, HTTP-Client- oder Cache-Integration bleibt Testcontainers der Standard.

---

## 25. Definition of Done

Ein Integrationstest erfüllt diese Richtlinie, wenn:

1. die reale technische Abhängigkeit über Testcontainers gestartet wird,
2. das Container-Image gepinnt ist,
3. keine produktiven Secrets verwendet werden,
4. keine produktiven personenbezogenen Daten verwendet werden,
5. Verbindungsdaten über `@ServiceConnection` oder `@DynamicPropertySource` gesetzt werden,
6. Testdaten eindeutig und reproduzierbar sind,
7. Isolation über Rollback, Cleanup oder eindeutige Testdaten sichergestellt ist,
8. der Test in CI ausführbar ist,
9. der Test fachliches oder technisches Integrationsverhalten prüft,
10. Container-Lifecycle und Testlaufzeit angemessen sind,
11. SaaS-relevante Mandantengrenzen bei betroffenen Flows geprüft werden,
12. der Test nicht nur H2-, Mock- oder Fake-Verhalten bestätigt.

---

## 26. Quellen und weiterführende Literatur

- Testcontainers for Java — Dokumentation: https://java.testcontainers.org/
- Testcontainers JUnit 5 Integration: https://java.testcontainers.org/test_framework_integration/junit_5/
- Testcontainers Reusable Containers: https://java.testcontainers.org/features/reuse/
- Testcontainers Configuration / Ryuk: https://java.testcontainers.org/features/configuration/
- Spring Boot Reference — Testcontainers: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Spring Framework — `@DynamicPropertySource`: https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-dynamicpropertysource.html
- Spring Framework — `@Sql`: https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/executing-sql.html
- Spring Framework — Transaction Management in Tests: https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html
- JUnit 5 User Guide: https://docs.junit.org/5.10.5/user-guide/
