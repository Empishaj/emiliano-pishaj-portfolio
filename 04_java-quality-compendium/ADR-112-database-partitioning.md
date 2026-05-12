# ADR-112 — Database Partitioning & Sharding: Sehr große Tabellen skalieren

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / DBA                                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Datenbank · Performance · Skalierung                          |
| Betroffene Teams  | Teams mit Tabellen > 100 Millionen Rows                       |
| Abhängigkeiten    | ADR-073 (Indexing), ADR-016 (JPA), ADR-034 (Flyway)           |

---

## 1. Wann Partitionierung nötig wird

```
KEIN PARTITIONIERUNGS-BEDARF:
  Tabelle < 10 Mio Rows + gute Indizes (→ ADR-073) → kein Problem

PARTITIONIERUNG NÖTIG wenn:
  ① Query-Zeit trotz Indizes > 1 Sekunde bei einfachen WHERE-Klauseln
  ② VACUUM/AUTOVACUUM dauert Stunden (PostgreSQL)
  ③ Tabellen-Statistiken veralten zu schnell
  ④ Archivierung/Löschung alter Daten ist langsam
  ⑤ Tabelle > 100 GB auf SSD

UNTERSCHIED PARTITIONIERUNG vs. SHARDING:
  Partitionierung: eine logische Tabelle, mehrere physische Partitionen,
                   ein Datenbankserver
  Sharding:        mehrere Datenbanken auf mehreren Servern,
                   Anwendung routet selbst
```

---

## 2. Entscheidung

PostgreSQL Deklarative Partitionierung (Range oder List) wird für Tabellen
> 100 Mio Rows verwendet. Sharding wird erst eingesetzt wenn ein einzelner
Datenbankserver keine ausreichende Schreibleistung mehr bietet (> 50.000 Writes/s).

---

## 3. Range Partitionierung: Zeitbasiert (häufigster Fall)

```sql
-- Beispiel: orders-Tabelle mit 500 Mio Rows, primär nach Datum abgefragt

-- SCHRITT 1: Partitionierte Tabelle erstellen
CREATE TABLE orders (
    id          BIGSERIAL,
    customer_id BIGINT       NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    total_cents BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,  -- ← Partitionsschlüssel
    PRIMARY KEY (id, created_at)         -- created_at MUSS im PK sein!
) PARTITION BY RANGE (created_at);

-- SCHRITT 2: Monatliche Partitionen erstellen
CREATE TABLE orders_2024_01 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE orders_2024_02 PARTITION OF orders
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- ...

CREATE TABLE orders_2024_12 PARTITION OF orders
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');

-- Default-Partition für unerwartete Werte
CREATE TABLE orders_default PARTITION OF orders DEFAULT;

-- SCHRITT 3: Indizes auf Partitionen (nicht auf Parent-Tabelle!)
-- Werden automatisch auf alle existierenden Partitionen angewendet
CREATE INDEX CONCURRENTLY idx_orders_customer_date
    ON orders (customer_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_orders_status
    ON orders (status) WHERE status IN ('PENDING_PAYMENT', 'CONFIRMED');
```

### 3.1 Automatische Partition-Erstellung

```java
// Neue Monats-Partitionen automatisch erstellen (vor Monatsende!)
@Scheduled(cron = "0 0 1 25 * ?")  // 25. jeden Monats
public void createNextMonthPartition() {
    var nextMonth = YearMonth.now().plusMonths(1);
    var start = nextMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    var end   = nextMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    var sql = """
        CREATE TABLE IF NOT EXISTS orders_%d_%02d
            PARTITION OF orders
            FOR VALUES FROM ('%s') TO ('%s')
        """.formatted(
            nextMonth.getYear(),
            nextMonth.getMonthValue(),
            start.toString(),
            end.toString()
        );

    jdbcTemplate.execute(sql);
    log.info("Partition erstellt: orders_{}_{:02d}",
        nextMonth.getYear(), nextMonth.getMonthValue());
}
```

### 3.2 Partition Pruning: Warum Partitionierung Performance bringt

```sql
-- OHNE Partitionierung: Full Table Scan über 500 Mio Rows
EXPLAIN SELECT * FROM orders WHERE created_at > '2024-06-01';
-- Seq Scan on orders (cost=0.00..52000000.00 rows=5000000 width=...)

-- MIT Partitionierung: nur relevante Partitionen werden gescannt
EXPLAIN SELECT * FROM orders WHERE created_at > '2024-06-01';
-- Append
--   -> Index Scan on orders_2024_06 (cost=0.00..1200.00 ...)
--   -> Index Scan on orders_2024_07 (cost=0.00..1100.00 ...)
--   -> Index Scan on orders_2024_08 (cost=0.00..1050.00 ...)
-- Nur 3 Partitionen statt 500 Mio Rows!

-- WICHTIG: Partition Pruning funktioniert NUR wenn created_at im WHERE ist!
-- Ohne Datum-Filter → trotzdem alle Partitionen gescannt
```

### 3.3 Alte Partitionen archivieren

```sql
-- FAST: Partition als Ganzes entfernen (kein Row-by-Row DELETE!)
-- OHNE Partitionierung: DELETE FROM orders WHERE created_at < '2022-01-01'
--   → Stunden bei 100 Mio Rows, massive VACUUM-Last danach

-- MIT Partitionierung:
-- Option A: Detach und separat archivieren
ALTER TABLE orders DETACH PARTITION orders_2022_01;
-- orders_2022_01 ist jetzt eine normale Tabelle → in cold storage verschieben

-- Option B: Direkt löschen (sofort, kein VACUUM nötig!)
DROP TABLE orders_2022_01;
-- ← In Millisekunden, unabhängig von Row-Anzahl!
```

---

## 4. List Partitionierung: Nach Tenant oder Region

```sql
-- Multi-Tenant oder geografische Trennung
CREATE TABLE orders (
    id          BIGSERIAL,
    tenant_id   VARCHAR(50) NOT NULL,  -- ← Partitionsschlüssel
    -- ...
    PRIMARY KEY (id, tenant_id)
) PARTITION BY LIST (tenant_id);

-- Je Tenant eine Partition (Tenant-Isolation!)
CREATE TABLE orders_tenant_de PARTITION OF orders
    FOR VALUES IN ('DE', 'AT', 'CH');

CREATE TABLE orders_tenant_us PARTITION OF orders
    FOR VALUES IN ('US', 'CA');

CREATE TABLE orders_default PARTITION OF orders DEFAULT;
```

---

## 5. JPA mit partitionierten Tabellen

```java
// JPA/Hibernate arbeitet transparent mit partitionierten Tabellen.
// Für PostgreSQL-Partitionierung: keine besondere JPA-Konfiguration nötig.
// Hibernate sieht nur die Parent-Tabelle.

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "orders_seq")
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;   // Partitionsschlüssel — MUSS immer gesetzt sein!

    // Achtung: JPQL-Queries müssen Partitionsschlüssel im WHERE haben
    // um Partition Pruning zu nutzen!
}

// Repository mit Partition-Pruning-freundlichen Queries
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // GUT: Datum im WHERE → Partition Pruning aktiv
    @Query("SELECT o FROM OrderEntity o " +
           "WHERE o.customerId = :customerId " +
           "AND o.createdAt >= :from AND o.createdAt < :to")
    List<OrderEntity> findByCustomerInDateRange(
        @Param("customerId") Long customerId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    // SCHLECHT: kein Datum-Filter → alle Partitionen gescannt!
    // List<OrderEntity> findByCustomerId(Long customerId);
    // → Besser: immer Datum-Range mitgeben
}
```

---

## 6. Sharding: Wann und Wie

```
SHARDING ist nötig wenn:
  ① Ein Server > 10.000 Writes/s nicht mehr schafft
  ② Datenbankgröße > RAM (kein Hot-Data mehr im Cache)
  ③ Single-Server-Limits (max. ~32 CPU-Cores effizient nutzbar)

SHARDING-STRATEGIEN:
  
  Range Sharding:
    Shard 1: customer_id 1-1.000.000
    Shard 2: customer_id 1.000.001-2.000.000
    Problem: Hot-Spots wenn neue Kunden häufiger abgefragt werden

  Hash Sharding:
    shard = hash(customer_id) % 4
    Gleichmäßige Verteilung
    Problem: Range-Queries über Shards teuer

  Directory-Based Sharding:
    Lookup-Tabelle: welcher Shard für welchen Key?
    Flexibel, aber Lookup-Overhead

UNSERE EMPFEHLUNG:
  Sharding vermeiden solange PostgreSQL-Partitionierung reicht.
  Bei Sharding-Bedarf: Citus (PostgreSQL-Extension) oder
  separates Datenbank-Cluster mit PgBouncer-Routing.
```

---

## 7. Flyway für partitionierte Tabellen

```sql
-- V20__create_partitioned_orders_table.sql
-- Migrate: orders → orders_partitioned

-- Neue partitionierte Tabelle erstellen
CREATE TABLE orders_v2 (
    id BIGSERIAL, created_at TIMESTAMPTZ NOT NULL, -- ...
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Partitionen erstellen (für aktuelle + vergangene 2 Jahre)
-- ... (monatliche Partitionen)

-- Daten umziehen (Expand/Contract → ADR-098):
-- Phase 1: Dual-Write in v1 und v2
-- Phase 2: Daten migrieren
-- Phase 3: v1 löschen, v2 umbenennen
```

---

## 8. Akzeptanzkriterien

- [ ] Query-Time für partitionierte Tabellen: P95 < 50ms mit Datum-Filter
- [ ] EXPLAIN ANALYZE zeigt "Partition Pruning" für alle produktions-relevanten Queries
- [ ] Monatliche Partition-Erstellung läuft automatisch (CronJob/Scheduled)
- [ ] Alte Partitionen werden nach Retention-Policy archiviert/gelöscht
- [ ] Keine JPA-Queries ohne Partitionsschlüssel im WHERE für > 10M-Row-Tabellen

---

## Quellen & Referenzen

- **PostgreSQL Documentation, "Table Partitioning"** — Deklarative Partitionierung, Pruning, Constraints.
- **Markus Winand, "SQL Performance Explained" (2012)** — Partitionierung als Performance-Strategie.
- **Martin Kleppmann, "Designing Data-Intensive Applications" (2017), Kap. 6** — Partitionierung und Sharding in verteilten Systemen.

---

## Verwandte ADRs

- [ADR-073](ADR-073-database-indexing.md) — Indizes (Basis vor Partitionierung)
- [ADR-098](ADR-098-zero-downtime-migrations.md) — Expand/Contract für die Migration
- [ADR-113](ADR-113-read-replicas.md) — Read Replicas als komplementäre Skalierungsstrategie
