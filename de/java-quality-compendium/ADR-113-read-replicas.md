# ADR-113 — Read Replicas & Routing: Leselast von Schreiblast trennen

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / DBA                                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Datenbank · Performance · Read-Skalierung                     |
| Betroffene Teams  | Teams mit Read-Heavy-Workloads (> 80% Reads)                  |
| Abhängigkeiten    | ADR-043 (HikariCP), ADR-032 (CQRS), ADR-016 (JPA)            |

---

## 1. Wann Read Replicas sinnvoll sind

```
TYPISCHE READ/WRITE-RATIO in Web-Anwendungen:
  Standard E-Commerce: 90% Reads, 10% Writes
  
  Ohne Read Replica:
    Primary-DB verarbeitet alles: Reports, Produktlisten,
    Bestellhistorie UND alle Schreiboperationen.
    → Primary unter Last: Writes werden langsamer
    → Schreibe-Latenz steigt

  Mit Read Replica:
    Primary: nur Writes (10% der Last)
    Replica: Reads (90% der Last)
    → Primary entlastet → Writes bleiben schnell
    → Replica kann für Reports dediziert verwendet werden

NICHT GEEIGNET für Read Replicas:
  ❌ Reads die nach einem Write sofortige Konsistenz brauchen
     ("Ich habe gerade Bestellung aufgegeben — zeig sie mir jetzt")
  ❌ Transaktionen die Read + Write in einer Transaktion kombinieren
  
GEEIGNET:
  ✅ Produktlisten, Kategorien, Suche-Autocomplete
  ✅ Reporting, Analytics, Dashboard-Queries
  ✅ Admin-Übersichten (leichte Verzögerung OK)
  ✅ API-Endpoints die nur lesen
```

---

## 2. Entscheidung

Lesezugriffe werden auf Read Replicas gerouted via Spring `AbstractRoutingDataSource`.
Die Entscheidung ob Primary oder Replica erfolgt per `@Transactional(readOnly = true)`.
Konsistenz-kritische Reads (nach Writes) gehen immer auf den Primary.

---

## 3. AbstractRoutingDataSource: transparentes Routing

```java
// DataSource-Routing: Primary für Writes, Replica für Reads
public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // readOnly-Transaktion? → Replica
        // write-Transaktion? → Primary
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            ? "REPLICA"
            : "PRIMARY";
    }
}

@Configuration
public class DatabaseRoutingConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public DataSource replicaDataSource() {
        return replicaDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Primary
    @Bean
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("replicaDataSource") DataSource replica) {

        var routing = new ReadWriteRoutingDataSource();
        routing.setTargetDataSources(Map.of(
            "PRIMARY", primary,
            "REPLICA", replica
        ));
        routing.setDefaultTargetDataSource(primary); // Fallback: Primary
        return routing;
    }
}
```

### 3.1 application.yml: Zwei DataSources

```yaml
spring:
  datasource:
    primary:
      url:      ${DB_PRIMARY_URL}      # Schreib-Instanz
      username: ${DB_USER}
      password: ${DB_PASSWORD}
      hikari:
        pool-name: HikariPrimary
        maximum-pool-size: 15          # Writes: kleinerer Pool
        minimum-idle: 3

    replica:
      url:      ${DB_REPLICA_URL}      # Lese-Instanz
      username: ${DB_READONLY_USER}    # Read-only DB-User (Prinzip der geringsten Rechte!)
      password: ${DB_READONLY_PASSWORD}
      hikari:
        pool-name: HikariReplica
        maximum-pool-size: 30          # Reads: größerer Pool
        minimum-idle: 5
        read-only: true                # HikariCP setzt SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY
```

---

## 4. Verwendung: @Transactional(readOnly = true)

```java
// Das Routing passiert AUTOMATISCH über @Transactional(readOnly = true)
// Kein explizites "bitte Replica verwenden" nötig

@Service
public class ProductQueryService {

    private final ProductRepository productRepo;

    // readOnly = true → geht automatisch auf Replica
    @Transactional(readOnly = true)
    public Page<ProductDto> listProducts(Pageable pageable) {
        return productRepo.findAll(pageable)
            .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        return productRepo.findByNameContaining(query)
            .stream().map(productMapper::toDto).toList();
    }
}

@Service
public class OrderDomainService {

    // Kein readOnly → geht auf Primary
    @Transactional
    public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
        var order = Order.place(cmd);
        orderRepository.save(order);  // → Primary
        return OrderCreatedResult.from(order);
    }

    // ⚠️ WICHTIG: Sofort nach Write lesen? MUSS Primary verwenden!
    // Wegen Replikations-Lag: Replica hat die Daten eventuell noch nicht.
    @Transactional   // KEIN readOnly! Konsistenz nötig.
    public OrderDetailDto placeAndRead(PlaceOrderCommand cmd) {
        var result = placeOrder(cmd);
        // Dieser Read ist in DERSELBEN Transaktion → Primary → konsistent
        return orderRepository.findById(result.orderId());
    }
}
```

---

## 5. Replikations-Lag überwachen

```java
// Replikations-Lag sollte < 100ms sein für typische Web-Apps
// > 1 Sekunde: Alert, weil Nutzer veraltete Daten sehen könnten

@Scheduled(fixedDelay = 30_000)
public void monitorReplicationLag() {
    try {
        // PostgreSQL: Replikations-Lag messen
        var lagMs = replicaJdbcTemplate.queryForObject(
            """
            SELECT EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp()))
                   * 1000 AS lag_ms
            """,
            Long.class);

        meterRegistry.gauge("db.replication.lag.ms",
            lagMs != null ? lagMs : 0L);

        if (lagMs != null && lagMs > 5000) {
            log.warn("Replikations-Lag zu hoch: {}ms", lagMs);
        }
    } catch (Exception e) {
        log.error("Replikations-Lag-Check fehlgeschlagen", e);
    }
}
```

---

## 6. Circuit Breaker für Replica-Ausfall

```java
// Wenn Replica down: Fallback auf Primary (Verfügbarkeit > Skalierung)

@Component
public class SafeReadWriteRoutingDataSource extends ReadWriteRoutingDataSource {

    private final AtomicBoolean replicaHealthy = new AtomicBoolean(true);

    @Override
    protected Object determineCurrentLookupKey() {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                && replicaHealthy.get()) {
            return "REPLICA";
        }
        return "PRIMARY";
    }

    @Scheduled(fixedDelay = 10_000)
    public void checkReplicaHealth() {
        try {
            replicaDataSource.getConnection().isValid(1);
            replicaHealthy.set(true);
        } catch (Exception e) {
            replicaHealthy.set(false);
            log.warn("Replica nicht erreichbar, verwende Primary als Fallback");
        }
    }
}
```

---

## 7. CQRS und Read Replicas

```
Read Replicas sind die einfachste Form von CQRS (→ ADR-032):
  Write-Pfad → Primary DB
  Read-Pfad  → Replica DB

Ergänzend zu Elasticsearch (→ ADR-104):
  Einfache Reads:     → Read Replica (PostgreSQL, SQL-Queries)
  Volltextsuche:      → Elasticsearch
  Analytics/Reports:  → Dedizierte Reporting-Replica oder Data Warehouse
```

---

## 8. Akzeptanzkriterien

- [ ] Read-Replica-Routing ist aktiv: `@Transactional(readOnly = true)` geht auf Replica (Metrics)
- [ ] Replikations-Lag-Metrik: `db.replication.lag.ms` in Prometheus
- [ ] Alert bei Replikations-Lag > 5 Sekunden
- [ ] Fallback: bei Replica-Ausfall gehen alle Reads auf Primary (kein Fehler)
- [ ] Read-only DB-User für Replica (keine Write-Rechte)

---

## Quellen & Referenzen

- **PostgreSQL Documentation, "Streaming Replication"** — physische Replikation.
- **Kai Waehner, "Data Streaming with Apache Kafka" (2022)** — Event-driven Replikation als Alternative.
- **Martin Kleppmann, "Designing Data-Intensive Applications" (2017), Kap. 5** — Replikation, Eventual Consistency, Lag.

---

## Verwandte ADRs

- [ADR-032](ADR-032-cqrs.md) — CQRS: Read/Write-Trennung auf Anwendungsebene
- [ADR-043](ADR-043-jvm-tuning-hikaricp.md) — HikariCP Pool-Sizing
- [ADR-112](ADR-112-database-partitioning.md) — Partitionierung (komplementär)
