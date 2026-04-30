# ADR-073 — Datenbankindizes: Strategie, Pitfalls & Analyse

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · PostgreSQL |
| Datum      | 2024-01-01                        |
| Kategorie  | Performance / Datenbank           |

---

## Kontext & Problem

Falsch indexierte Datenbanken sind die häufigste Ursache für Performance-Probleme in Java-Applikationen. Kein Index = Full Table Scan. Zu viele Indizes = langsame Writes. Falscher Index = kein Nutzen. Fehlender Composite-Index = zwei separate Scans statt einem. Dieses ADR definiert die Indexierungsstrategie und wie Indizes analysiert werden.

---

## Wann wird ein Index genutzt?

```sql
-- PostgreSQL Query Planner entscheidet ob Index genutzt wird
-- Grundregeln:
-- ① Kleine Tabellen: Full Scan oft schneller als Index (< 1000 Rows: oft egal)
-- ② Selektivität: Index sinnvoll wenn < 10-20% der Rows betroffen
-- ③ WHERE-Klausel muss Index-Spalte linksbündig nutzen
-- ④ Funktionen auf Index-Spalte: Index nicht nutzbar (ohne Functional Index)

-- ❌ Index auf status wird NICHT genutzt (Funktion verändert Spalte)
SELECT * FROM orders WHERE LOWER(status) = 'pending';

-- ✅ Functional Index für diesen Fall
CREATE INDEX idx_orders_status_lower ON orders(LOWER(status));
```

---

## Indizes richtig anlegen (Flyway-Migration)

```sql
-- V25__add_performance_indexes.sql

-- ① Primary Key ist automatisch ein Index (schon vorhanden)
-- CREATE INDEX idx_orders_pk ON orders(id);  ← nicht nötig!

-- ② Häufig gefilterte Spalten: Foreign Keys und Status-Spalten
CREATE INDEX idx_orders_user_id
    ON orders(user_id);          -- Fast immer gesucht: WHERE user_id = ?

CREATE INDEX idx_orders_status
    ON orders(status)
    WHERE status != 'DELIVERED'; -- Partial Index! Nur nicht-abgeschlossene Bestellungen
                                  -- Klein, schnell, treffsicher

-- ③ Composite Index: Spalten-Reihenfolge ist entscheidend!
-- Regel: Spalten mit hoher Selektivität zuerst, dann Spalten für ORDER BY

-- Query: WHERE user_id = ? AND status = ? ORDER BY created_at DESC
CREATE INDEX idx_orders_user_status_date
    ON orders(user_id, status, created_at DESC);
-- Diese Query nutzt den Index vollständig
-- Aber: WHERE status = ? OHNE user_id nutzt den Index NICHT (linksbündig!)

-- ④ Unique Index: erzwingt Constraint UND bietet Performance
CREATE UNIQUE INDEX uq_orders_payment_ref
    ON orders(payment_reference)
    WHERE payment_reference IS NOT NULL;  -- Nullable-Spalte: NULL-Werte ausschließen

-- ⑤ CONCURRENTLY: Index anlegen ohne Table Lock (für Produktions-Tabellen!)
CREATE INDEX CONCURRENTLY idx_orders_created_at
    ON orders(created_at DESC);

-- ⑥ GIN-Index für JSONB (wenn Metadaten als JSON gespeichert)
CREATE INDEX idx_orders_metadata
    ON orders USING GIN (metadata jsonb_path_ops);

-- ⑦ Covering Index: alle benötigten Spalten im Index (kein Heap-Zugriff!)
-- Query: SELECT id, status, total FROM orders WHERE user_id = ?
CREATE INDEX idx_orders_user_covering
    ON orders(user_id) INCLUDE (status, total);
--                       ↑ INCLUDE: Spalten im Index mitführen ohne Key zu sein
```

---

## EXPLAIN ANALYZE: Indizes verifizieren

```sql
-- Vor und nach Index-Erstellung messen:
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT o.id, o.status, o.total
FROM orders o
WHERE o.user_id = 42
  AND o.status = 'PENDING'
ORDER BY o.created_at DESC
LIMIT 20;

-- ✅ Gut — mit Index:
-- Index Scan using idx_orders_user_status_date on orders
--   (cost=0.43..8.45 rows=20 width=48) (actual time=0.052..0.123 rows=20 loops=1)
--   Index Cond: ((user_id = 42) AND (status = 'PENDING'))
-- Planning Time: 0.3 ms
-- Execution Time: 0.2 ms

-- ❌ Schlecht — ohne Index (Full Table Scan bei 500.000 Rows):
-- Seq Scan on orders
--   (cost=0.00..18500.00 rows=20 width=48) (actual time=125.23..1234.45 rows=20 loops=1)
--   Filter: ((user_id = 42) AND (status = 'PENDING'))
-- Execution Time: 1234 ms
```

---

## Spring Data JPA: Indizes deklarativ

```java
// @Table mit @Index: Index als Teil der Entity-Definition
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_user_id",
               columnList = "user_id"),

        @Index(name = "idx_orders_user_status_date",
               columnList = "user_id, status, created_at DESC"),

        @Index(name = "uq_orders_payment_ref",
               columnList = "payment_reference",
               unique = true)
    }
)
public class OrderEntity {
    // ...
}

// ACHTUNG: JPA generiert Indizes nur bei ddl-auto=create/update
// In Produktion: IMMER Flyway-Migration verwenden (→ ADR-034)!
// @Index ist Dokumentation in Produktion, nicht operativ.
```

---

## Ungenutzte Indizes identifizieren

```sql
-- PostgreSQL: Indizes die selten oder nie genutzt werden
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan         AS times_used,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
  AND idx_scan < 100      -- Weniger als 100 mal genutzt seit letztem Reset
ORDER BY pg_relation_size(indexrelid) DESC;

-- Ungenutzte Indizes kosten Disk + Schreib-Performance ohne Nutzen
-- Löschen wenn sicher nicht gebraucht:
DROP INDEX CONCURRENTLY idx_unused_example;
```

---

## Fehlende Indizes finden

```sql
-- PostgreSQL: welche Queries haben Sequential Scans auf großen Tabellen?
SELECT
    relname                AS table_name,
    seq_scan               AS full_scans,
    seq_tup_read           AS rows_read_by_seq_scan,
    idx_scan               AS index_scans,
    pg_size_pretty(pg_relation_size(relid)) AS table_size
FROM pg_stat_user_tables
WHERE seq_scan > 0
  AND pg_relation_size(relid) > 10 * 1024 * 1024  -- > 10MB Tabellen
ORDER BY seq_tup_read DESC
LIMIT 20;

-- pg_stat_statements: häufig ausgeführte langsame Queries
SELECT
    query,
    calls,
    round(mean_exec_time::numeric, 2) AS avg_ms,
    round(total_exec_time::numeric, 2) AS total_ms
FROM pg_stat_statements
WHERE mean_exec_time > 100    -- Queries die im Schnitt > 100ms dauern
ORDER BY total_exec_time DESC
LIMIT 20;
```

---

## JPA Repository: Hints für Query-Optimierung

```java
// @QueryHints: dem JPA-Provider Hinweise geben
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Timeout: Query darf max 3 Sekunden dauern
    @QueryHints(@QueryHint(
        name = "jakarta.persistence.query.timeout", value = "3000"))
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId")
    List<OrderEntity> findByUserId(@Param("userId") Long userId);

    // Fetch Size: JDBC-Level-Optimierung für große Result-Sets
    @QueryHints(@QueryHint(
        name = "org.hibernate.fetchSize", value = "100"))
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status")
    Stream<OrderEntity> streamByStatus(@Param("status") OrderStatus status);
    // Stream<> statt List<>: verarbeitet Row-für-Row, kein alles-in-RAM
}
```

---

## Konsequenzen

**Positiv:** Richtige Indizes können Queries von Sekunden auf Millisekunden reduzieren. Partial Indexes und Covering Indexes minimieren Index-Größe. CONCURRENTLY verhindert Downtime beim Index-Anlegen in Produktion.

**Negativ:** Jeder Index kostet Schreib-Performance und Disk-Space. Composite-Index-Reihenfolge ist nicht intuitiv — falsche Reihenfolge = kein Nutzen. Indizes veralten wenn Query-Patterns sich ändern.

---

## 💡 Guru-Tipps

- **`EXPLAIN ANALYZE` ist Pflicht** bevor jeder neue Index deployed wird — Effekt messen!
- **Partial Index bevorzugen**: `WHERE status != 'ARCHIVED'` — kleiner, schneller, treffsicher.
- **`pg_stat_statements`** aktivieren und wöchentlich die Top-10 langsamen Queries analysieren.
- **Autovacuum-Statistiken**: veraltete Statistiken führen zu schlechten Query-Plänen. `ANALYZE orders;` nach großen Datenänderungen.

---

## Verwandte ADRs

- [ADR-016](ADR-016-datenbank-jpa-n-plus-eins.md) — Indizes und Fetch-Strategien zusammen denken.
- [ADR-034](ADR-034-db-migrations-flyway.md) — Indizes als Flyway-Migration anlegen.
- [ADR-044](ADR-044-load-performance-testing.md) — Load Tests zeigen wo Indizes fehlen.
