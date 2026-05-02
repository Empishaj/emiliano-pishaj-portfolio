# ADR-058 — Multi-Tenancy: Datenisolation & Mandantenfähigkeit

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Hibernate  |
| Datum      | 2024-01-01                        |
| Kategorie  | Architektur / Daten               |

---

## Kontext & Problem

SaaS-Anwendungen bedienen viele Kunden (Tenants) mit derselben Software-Instanz. Ohne Multi-Tenancy-Strategie passiert das Schlimmste: Tenant A sieht Daten von Tenant B. Es gibt drei Isolationsstrategien — jede mit anderen Trade-offs bezüglich Isolation, Performance und Kosten.

---

## Die drei Multi-Tenancy-Strategien

```
Schema 1: Shared Database, Shared Schema
  → Alle Tenants in denselben Tabellen, unterschieden durch tenant_id-Spalte
  → Einfachste Implementierung, höchste Effizienz
  → Niedrigste Isolation: SQL-Fehler kann Tenant-Leak verursachen

Schema 2: Shared Database, Separate Schema
  → Jeder Tenant hat eigenes DB-Schema (PostgreSQL schema)
  → Mittlere Isolation, mittlerer Overhead
  → Flyway pro Schema nötig

Schema 3: Separate Database
  → Jeder Tenant hat eigene Datenbank
  → Höchste Isolation, höchste Kosten
  → Compliance-Anforderungen (DSGVO: Daten in DE/EU)
```

---

## Strategie 1: Shared Schema mit tenant_id (empfohlen für Start)

### Tenant-Kontext

```java
// Tenant-Kontext: wer ist gerade eingeloggt?
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT =
        new InheritableThreadLocal<>();

    public static void set(String tenantId) {
        CURRENT_TENANT.set(Objects.requireNonNull(tenantId));
    }

    public static String get() {
        var tenantId = CURRENT_TENANT.get();
        if (tenantId == null) throw new TenantNotSetException("No tenant in context");
        return tenantId;
    }

    public static void clear() { CURRENT_TENANT.remove(); }
}

// Filter: Tenant aus JWT extrahieren und in Context setzen
@Component
@Order(1)
public class TenantExtractionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse res,
                                     FilterChain chain) throws IOException, ServletException {
        try {
            var jwt = extractJwt(req);
            var tenantId = jwt.getClaim("tenant_id");

            if (tenantId == null)
                throw new MissingTenantException("JWT missing tenant_id claim");

            TenantContext.set(tenantId.toString());
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear(); // PFLICHT: ThreadLocal leeren!
        }
    }
}
```

### Hibernate Filter: automatische tenant_id-Filterung

```java
// Hibernate Multi-Tenancy via Filter — automatisch für alle Queries
@Entity
@Table(name = "orders")
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class OrderEntity {

    @Id @GeneratedValue
    private Long   id;

    @Column(nullable = false)
    private String tenantId;   // Pflichtfeld in jeder Entity

    // ... weitere Felder
}

// Filter aktivieren: pro Session
@Component
public class TenantFilterActivator {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void enableTenantFilter() {
        // Hibernate Session öffnen
        var session = em.unwrap(Session.class);
        session.enableFilter("tenantFilter")
               .setParameter("tenantId", TenantContext.get());
    }
}
```

### Repository: tenant_id automatisch setzen

```java
// Spring Data JPA: tenant_id immer aus Context
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Kein explizites tenant_id nötig — Hibernate Filter übernimmt das
    Optional<OrderEntity> findById(Long id);
    Page<OrderEntity> findAll(Pageable pageable);
}

// Service: tenant_id beim Speichern setzen
@Service
public class OrderService {

    @Transactional
    public OrderCreatedResponse create(CreateOrderCommand cmd) {
        var entity = new OrderEntity();
        entity.setTenantId(TenantContext.get()); // Automatisch aus Context
        entity.setItems(cmd.items());
        // ...
        return mapper.toResponse(orderRepository.save(entity));
    }
}
```

---

## Strategie 2: Separate Schema per Tenant

```java
// Hibernate Multi-Tenancy: Schema-basiert
@Configuration
public class MultiTenancyConfig {

    @Bean
    public MultiTenancyStrategy multiTenancyStrategy() {
        return MultiTenancyStrategy.SCHEMA;
    }

    @Bean
    public CurrentTenantIdentifierResolver tenantResolver() {
        return new CurrentTenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                return TenantContext.get(); // z.B. "tenant_acme"
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }

    @Bean
    public MultiTenantConnectionProvider connectionProvider(DataSource ds) {
        return new SchemaBasedMultiTenantConnectionProvider(ds);
    }
}

// Schema-basierter ConnectionProvider
public class SchemaBasedMultiTenantConnectionProvider
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    @Override
    protected DataSource selectAnyDataSource() { return dataSource; }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return dataSource; // Dieselbe DB, anderes Schema
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        var conn = dataSource.getConnection();
        // PostgreSQL Schema wechseln
        conn.createStatement().execute(
            "SET search_path TO tenant_" + sanitize(tenantIdentifier) + ", public");
        return conn;
    }

    private String sanitize(String tenantId) {
        // SQL Injection verhindern!
        if (!tenantId.matches("[a-zA-Z0-9_-]+"))
            throw new InvalidTenantIdException(tenantId);
        return tenantId;
    }
}
```

---

## Flyway: Migration pro Tenant-Schema

```java
@Component
public class TenantSchemaInitializer {

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    // Beim Start: alle bekannten Tenants migrieren
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAllTenants() {
        tenantRepository.findAll().forEach(this::migrateSchema);
    }

    // Neuen Tenant anlegen: Schema + Migration
    @Transactional
    public void onboardTenant(String tenantId) {
        createSchema(tenantId);
        migrateSchema(tenantId);
        tenantRepository.save(new Tenant(tenantId, Instant.now()));
    }

    private void migrateSchema(Tenant tenant) {
        Flyway.configure()
            .dataSource(dataSource)
            .schemas("tenant_" + tenant.id())
            .locations("classpath:db/migration/tenant")
            .load()
            .migrate();
    }
}
```

---

## Sicherheits-Tests: Tenant-Isolation prüfen

```java
@SpringBootTest
class TenantIsolationTest extends IntegrationTestBase {

    @Test
    void orderFromTenantA_notVisibleToTenantB() {
        // Order für Tenant A anlegen
        TenantContext.set("tenant-a");
        var orderId = orderService.create(validCommand()).id();
        TenantContext.clear();

        // Als Tenant B versuchen auf Tenant-A-Order zuzugreifen
        TenantContext.set("tenant-b");
        var result = orderRepository.findById(orderId);
        TenantContext.clear();

        // Muss leer sein — Tenant-B sieht Tenant-A-Daten nicht
        assertThat(result).isEmpty();
    }

    @Test
    void tenantContext_clearedAfterRequest() throws Exception {
        // Simulated: Context nicht geleert → nächster Request hat alten Kontext
        // Filter muss TenantContext.clear() im finally-Block aufrufen!
        mockMvc.perform(get("/api/v1/orders")
                .header("Authorization", jwtForTenant("tenant-a")))
            .andExpect(status().isOk());

        // Nach Request: Context leer
        assertThatThrownBy(TenantContext::get)
            .isInstanceOf(TenantNotSetException.class);
    }
}
```

---

## Konsequenzen

**Positiv:** Shared Schema ist einfach und effizient. Separate Schemas ermöglichen Tenant-spezifische Migrationen. Hibernate Filter eliminiert händisches `WHERE tenant_id = ?`.

**Negativ:** Shared Schema: SQL-Bug kann tenant_id vergessen → Daten-Leak. Separate Schemas: Migrations-Overhead × Anzahl Tenants. ThreadLocal muss immer in `finally` geleert werden — Fehler führt zu Tenant-Leak!

---

## 💡 Guru-Tipps

- **ArchUnit-Test**: Alle JPA-Entities müssen `tenantId`-Feld haben.
- **Logging immer mit tenantId**: `MDC.put("tenantId", TenantContext.get())` in Filter.
- **Rate Limiting pro Tenant**: ein Tenant darf nicht alle anderen beeinflussen (Noisy Neighbor).
- **Virtual Threads**: `InheritableThreadLocal` funktioniert mit Virtual Threads, aber: `ThreadLocal` bei `ThreadLocal`-basierter Propagation prüfen.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — Tenant-Isolation als Sicherheitsanforderung.
- [ADR-034](ADR-034-db-migrations-flyway.md) — Flyway pro Tenant-Schema.
- [ADR-040](ADR-040-oauth2-oidc-jwt.md) — tenant_id als JWT-Claim.
