# ADR-034 — Datenbankmigrationen mit Flyway

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · Flyway 9+  |
| Datum      | 2024-01-01                        |
| Kategorie  | Persistenz / DevOps               |

---

## Kontext & Problem

Datenbankschemas ändern sich mit der Applikation — aber ohne Migrationswerkzeug entstehen Drifts zwischen Entwicklung, Test und Produktion, manuelle Schema-Änderungen sind nicht reproduzierbar, und Rollouts zu Versionen mit inkompatiblem Schema crashen. Flyway macht DB-Änderungen versioniert, reproduzierbar und rollback-fähig.

---

## Regel 1 — Niemals `spring.jpa.hibernate.ddl-auto=create` in Produktion

```yaml
# ❌ Vernichtet alle Daten beim Start!
spring:
  jpa:
    hibernate:
      ddl-auto: create        # Löscht und erstellt Schema neu
      # oder: create-drop     # Löscht Schema beim Herunterfahren

# ❌ Unsicherer Kompromiss in Produktion
      ddl-auto: update        # Fügt Spalten hinzu, löscht NIE — Schema-Drift

# ✅ Validierung ja, aber Flyway übernimmt Schema-Management
      ddl-auto: validate      # Prüft ob Schema mit Entities übereinstimmt
      # oder:
      ddl-auto: none          # Keine automatische Schema-Verwaltung
```

---

## Regel 2 — Flyway Namenskonvention einhalten

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_orders_table.sql
├── V3__add_email_index_to_users.sql
├── V4__add_shipping_address_to_orders.sql
├── V5__rename_user_status_to_active.sql
└── R__create_reporting_view.sql       ← Repeatable Migration

Namensschema: V{Version}__{Beschreibung}.sql
- V    = Versionsmigration (einmalig, in Reihenfolge)
- R    = Repeatable (bei Checksummen-Änderung erneut ausgeführt, für Views/Functions)
- U    = Undo (Flyway Teams: Rückgängig-Migration)
- __   = Doppelter Unterstrich (Pflicht)
```

---

## Regel 3 — Migrationen sind unveränderlich

```sql
-- V3__add_email_index_to_users.sql
-- EINMAL eingecheckt → NIE MEHR ÄNDERN
-- Flyway speichert den Checksummen-Hash
-- Änderung → Flyway verweigert den Start mit "checksum mismatch"

CREATE UNIQUE INDEX idx_users_email ON users(email);
```

```sql
-- ❌ Fehler gefunden in V3? NICHT V3 ändern!
-- ✅ Neue Migration erstellen:
-- V3_1__fix_email_index_to_be_case_insensitive.sql (oder V4)
DROP INDEX IF EXISTS idx_users_email;
CREATE UNIQUE INDEX idx_users_email ON users(LOWER(email));
```

---

## Regel 4 — Gute Migrationsskripte

```sql
-- V1__create_users_table.sql
-- Konventionen:
-- ① Idempotenz wo möglich (IF NOT EXISTS, IF EXISTS)
-- ② Explizite Constraints mit Namen (für aussagekräftige Fehlermeldungen)
-- ③ Kommentare für nicht-offensichtliche Entscheidungen
-- ④ Keine Daten-Manipulation in Schema-Migrationen (separate V-Datei)

CREATE TABLE users (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email UNIQUE (email),        -- Constraint mit Namen!
    CONSTRAINT chk_users_name_not_empty CHECK (LENGTH(TRIM(name)) > 0)
);

-- Index separat (performance-kritisch, explizit dokumentiert)
CREATE INDEX idx_users_active_created ON users(active, created_at DESC);

-- Kommentar: Warum TIMESTAMPTZ statt TIMESTAMP?
-- TIMESTAMPTZ speichert UTC, konvertiert automatisch für Client-Timezone
COMMENT ON TABLE users IS 'Core user accounts. Email is the primary identifier.';
```

```sql
-- V5__rename_user_status_to_active.sql
-- Spalte umbenennen: sicher, rückwärtskompatibel in Schritte aufteilen

-- Schritt 1 (diese Migration): Neue Spalte hinzufügen
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Schritt 2 (diese Migration): Daten migrieren
UPDATE users SET active = (status = 'ACTIVE');

-- Schritt 3 (nächste Migration nach Deployment): alte Spalte entfernen
-- → In V6__drop_old_status_column.sql (nach Deployment wenn kein Code mehr status liest)
-- ALTER TABLE users DROP COLUMN status;
```

---

## Regel 5 — Zero-Downtime Migrationen

```sql
-- ❌ Blockierende Migration: ALTER TABLE sperrt die Tabelle in PostgreSQL
ALTER TABLE orders ADD COLUMN priority INTEGER NOT NULL DEFAULT 0;
-- Bei großen Tabellen: Minuten-langer Lock → Downtime

-- ✅ Non-blocking: erst nullable, dann Constraint
-- Migration V10__add_priority_to_orders.sql:
ALTER TABLE orders ADD COLUMN priority INTEGER; -- Kein NOT NULL, kein DEFAULT → sofort
-- ↑ In PostgreSQL: sofort, kein Table Lock

-- Dann: Daten befüllen in Batches (keine große UPDATE-Transaktion!)
UPDATE orders SET priority = 0 WHERE priority IS NULL AND id BETWEEN 1 AND 10000;
UPDATE orders SET priority = 0 WHERE priority IS NULL AND id BETWEEN 10001 AND 20000;
-- ... (oder Application-Level-Batch)

-- Migration V11__add_priority_not_null_constraint.sql (nachdem alle Daten befüllt):
ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
ALTER TABLE orders ALTER COLUMN priority SET DEFAULT 0;
```

---

## Spring Boot Konfiguration

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true    # Für bestehende DBs ohne Flyway-Historie
    validate-on-migrate: true    # Checksummen prüfen
    out-of-order: false          # Keine Migrationen außer der Reihe
    table: flyway_schema_history # Standard-Tabelle

  jpa:
    hibernate:
      ddl-auto: validate        # Flyway übernimmt, JPA validiert nur
```

---

## Testumgebung: Flyway + Testcontainers

```java
// Testcontainers startet echte PostgreSQL (→ ADR-018)
// Flyway läuft automatisch beim Spring-Context-Start
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(TestcontainersConfig.class)
class UserRepositoryTest {
    // Flyway hat bereits alle Migrationen eingespielt
    // Test läuft gegen dasselbe Schema wie Produktion
}
```

---

## Konsequenzen

**Positiv:** Jeder Entwickler, jede CI-Umgebung, jede Produktionsinstanz hat exakt dasselbe Schema. `flyway_schema_history` ist der Audit-Trail aller Schema-Änderungen. Rollbacks durch Undo-Migrationen (Flyway Teams) oder manuelle V{n}__rollback-Migrationen.

**Negativ:** Migrationen sind unveränderlich — Fehler erfordern neue Migrationsdatei. Zero-Downtime-Migrationen erfordern Mehrschrittansatz. Lange Datenmigration in einer Transaktion kann Timeouts verursachen.

---

## 💡 Guru-Tipps

- **Migrationen reviewen** wie Code: zwei Augenpaare auf jede `.sql`-Datei.
- **Nie `flyway_schema_history` manuell ändern** — außer in absoluten Notfällen.
- **Separate Migrations-Datei für Daten** (V{n}__seed_reference_data.sql) statt Daten in Schema-Migrationen mischen.
- **PostgreSQL `CONCURRENTLY`** für Indexes: `CREATE INDEX CONCURRENTLY idx_name ON table(col)` — kein Table Lock.

---

## Verwandte ADRs

- [ADR-016](ADR-016-datenbank-jpa-n-plus-eins.md) — JPA-Schema muss mit Flyway-Migrationen übereinstimmen.
- [ADR-018](ADR-018-integrationstests-testcontainers.md) — Testcontainers führt Flyway-Migrationen aus.
