# QG-JAVA-034 — Datenbankmigrationen mit Flyway
  
---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Datenbankmigrationen in Java-/Spring-Boot-Services mit Flyway sauber umgesetzt werden. Der Schwerpunkt liegt auf:

- versionierten Schema-Migrationen,
- reproduzierbarer Schema-Verwaltung,
- non-blocking Production-Migrationen,
- Multi-Tenant-Setups,
- Kubernetes-Deployment-Patterns,
- realistischer Rollback-Strategie.

Datenbankschemas ändern sich mit der Applikation. Ohne Migrationswerkzeug entstehen **Drifts zwischen Entwicklung, Test und Produktion**, manuelle Schema-Änderungen sind nicht reproduzierbar, und Rollouts zu Versionen mit inkompatiblem Schema crashen. Flyway macht DB-Änderungen versioniert, reproduzierbar und nachvollziehbar.

Aber: **Flyway ist kein Magie-Werkzeug.** In Produktion kommen subtile Probleme hinzu:

- Lock Queue Pileup auf großen Tabellen
- Mehrere Pods, die parallel Migrationen starten wollen
- Multi-Tenant-Setups mit Schema-pro-Tenant
- Datenbackfills, die nicht in eine einzelne Transaktion passen
- Echte Rollback-Anforderungen vs. Forward-Fix-Realität

Diese Richtlinie verhindert vier typische Fehler:

1. `ddl-auto=create` oder `update` wird versehentlich in Produktion aktiviert.
2. Migrationen werden nachträglich geändert, wodurch Checksums brechen.
3. Schema-Änderungen blockieren produktive Workloads, weil Lock-Timeouts fehlen.
4. Multi-Tenant-Setups werden mit Single-Schema-Pattern abgebildet, was bei Onboarding und Migration eskaliert.

---

## 2. Kurzregel für Entwickler

Flyway MUSS für Schema-Management verwendet werden. `spring.jpa.hibernate.ddl-auto` MUSS auf `validate` oder `none` stehen. Migrationen MÜSSEN unveränderlich sein — Fehler werden über neue Folge-Migrationen behoben, nicht durch Änderungen an bestehenden.

Schema-Migrationen und Daten-Migrationen MÜSSEN getrennt werden. Datenbackfills auf großen Tabellen MÜSSEN über die Anwendungsschicht (Background Job) erfolgen, nicht in der Flyway-Migration selbst. Lock-Timeouts MÜSSEN vor `ALTER TABLE`-Operationen gesetzt sein, um Lock Queue Pileup zu verhindern.

Bei Multi-Tenant-Setups MÜSSEN tenantspezifische Migrationen explizit modelliert werden (Schema-pro-Tenant oder Tenant-ID-Spalte mit allen Folgen). `baseline-on-migrate: true` DARF NICHT als Dauer-Default in `application.yml` stehen — das ist die klassische silent-skip-Falle.

Rollback ist in der Praxis meistens Forward-Fix: eine neue Migration, die den Stand wiederherstellt. „Echte" Rollbacks (Undo-Migrationen) sind ein Flyway-Teams-Feature und in Production-Setups oft nicht praktikabel.

---

## 3. Verbindlicher Standard

### 3.1 MUSS-Regeln

Eine Datenbankmigration MUSS folgende Regeln erfüllen:

1. Flyway MUSS für Schema-Management verwendet werden — `ddl-auto` darf NUR `validate` oder `none` sein.
2. Migrationen MÜSSEN dem Namensschema `V<Version>__<Beschreibung>.sql` oder `R__<Beschreibung>.sql` folgen.
3. Eingecheckte V-Migrationen MÜSSEN unveränderlich bleiben — Fixes erfolgen über neue Folge-Migrationen.
4. Repeatable Migrations (R\_\_) MÜSSEN idempotent sein (`CREATE OR REPLACE`, `ON CONFLICT DO UPDATE`).
5. Schema-Migrationen und Daten-Migrationen MÜSSEN getrennt sein — Namensschema `V<n>__schema__...` und `V<n>__data__...`.
6. `ALTER TABLE`-Migrationen auf großen Tabellen MÜSSEN Lock-Timeouts setzen (`SET LOCAL lock_timeout = '5s'`).
7. `CREATE INDEX` auf großen Tabellen MUSS `CONCURRENTLY` verwenden (mit `transactional: false`).
8. CHECK- und FOREIGN-KEY-Constraints auf großen Tabellen MÜSSEN über `NOT VALID` + `VALIDATE CONSTRAINT` angelegt werden.
9. `ADD COLUMN ... NOT NULL` mit non-konstantem Default MUSS in mehrschrittiger Migration erfolgen.
10. Datenbackfills auf großen Tabellen (>100k Zeilen) MÜSSEN über Application-Layer (Background Job) erfolgen, nicht in der Migration.
11. Multi-Tenant-Setups MÜSSEN explizit modelliert sein — entweder Schema-pro-Tenant mit programmatischer Migration oder Tenant-ID-Spalte mit Index.
12. Produktive Setups MÜSSEN getrennte DB-User für Migration (DDL-Rechte) und App (DML-Rechte) verwenden.
13. DB-Credentials MÜSSEN über Secret Management (Vault, K8s Secrets, Cloud Secret Manager) bezogen werden, nicht in `application.yml`.
14. In Kubernetes-Deployments MUSS Flyway über Init-Container oder Job laufen — nicht parallel in mehreren App-Pods.
15. CI/CD MUSS Flyway-Validation (`flyway validate`) als Pre-Merge-Check ausführen.
16. Production-Migrationen MÜSSEN in Staging gegen produktionsähnliche Datenmengen getestet sein.
17. Schema-Änderungen MÜSSEN rückwärtskompatibel sein, solange alte Anwendungsversion noch läuft (Rolling Deployment).

### 3.2 DARF-NICHT-Regeln

Eine Datenbankmigration DARF NICHT:

1. `spring.jpa.hibernate.ddl-auto=create`, `create-drop` oder `update` in Produktion aktivieren.
2. Eine bereits eingecheckte V-Migration nachträglich ändern (auch keine Kommentar-Änderungen, die die Checksum betreffen).
3. `baseline-on-migrate: true` als Dauer-Default in `application.yml` setzen — nur als einmaliger Setup-Schritt.
4. Nicht-idempotente Statements in einer Repeatable-Migration verwenden (`INSERT` ohne `ON CONFLICT`).
5. `CREATE INDEX` ohne `CONCURRENTLY` auf produktiv-relevanten Tabellen ausführen.
6. `ALTER TABLE ADD CONSTRAINT ... CHECK (...)` ohne `NOT VALID` auf großen Tabellen.
7. `ALTER COLUMN ... TYPE` mit Typumwandlung, die einen Full Table Rewrite triggert, ohne mehrschrittigen Migrationsplan.
8. `UPDATE`-Statements auf >100k Zeilen direkt in einer Flyway-Migration ausführen.
9. Flyway-Migrationen ohne Lock-Timeout in Produktion ausführen.
10. DB-Credentials plain in `application.yml` ablegen.
11. Mit denselben DB-Credentials Schema-Migrationen und App-Workload ausführen.
12. Mehrere App-Pods parallel Flyway-Migrationen starten lassen (Lock-Contention).
13. `flyway_schema_history`-Tabelle manuell editieren — außer in dokumentierten Notfällen mit DBA-Freigabe.
14. Datenbankspezifische Features ohne Datenbank-Versionshinweis in Migration-Skripten verwenden.

### 3.3 SOLLTE-Regeln

Eine Datenbankmigration SOLLTE:

1. Timestamp-basierte Versionen (`V20260518101500__...`) statt sequentielle Zahlen verwenden, wenn parallele Branches die Regel sind.
2. Constraint-Namen explizit setzen (`CONSTRAINT chk_users_email_format CHECK (...)`), nicht generieren lassen.
3. Java-Migrationen (`BaseJavaMigration`) für komplexe Logik mit Transaction-Handling verwenden.
4. Debezium CDC oder Outbox-Pattern berücksichtigen (siehe QG-JAVA-041 v2 Sektion 14/15), wenn Schema-Änderungen Event-Producer betreffen.
5. Pre-Production-Migrations-Dry-Runs gegen ein Cluster-Snapshot durchführen.
6. `flyway info` in CI für Pull Requests ausführen, um geplante Migrationen sichtbar zu machen.
7. `out-of-order: false` in Produktion verwenden, `out-of-order: true` nur in Development.
8. PostgreSQL `pg_squeeze` oder `pg_repack` für massive Datenmigrationen auf laufenden Systemen erwägen.
9. Spring Boot's `spring.flyway.locations` mit getrennten Pfaden für Schema und Daten verwenden.
10. Migrationen reviewen wie Code — zwei Augenpaare auf jede `.sql`-Datei.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für:

1. Schema-Migrationen in Spring-Boot-Services mit JPA/Hibernate.
2. Daten-Migrationen über Flyway oder Background Jobs.
3. Multi-Tenant-Setups (Schema-pro-Tenant und Tenant-ID-Spalte).
4. Kubernetes-Deployments mit Init-Container oder Job für Flyway.
5. CI/CD-Pipelines mit Flyway-Validation und -Migration.
6. Testumgebungen mit Testcontainers + Flyway.
7. DB-User-Trennung für Migration und App.
8. Rollback- und Forward-Fix-Strategien.
9. PostgreSQL 16+ als Default-Datenbank (andere Datenbanken siehe Sektion 28).

Diese Richtlinie gilt nicht als vollständiger Standard für:

1. NoSQL-Migrationen (MongoDB Migrations via Mongock o. Ä., Cassandra-Schema-Evolution).
2. Big-Data-ETL-Pipelines.
3. Cluster-Operations (Replication-Setup, Backup-Strategien, Disaster Recovery).
4. PostgreSQL-Performance-Tuning auf Operations-Ebene.
5. Event-Sourcing-Schema-Evolution (siehe QG-JAVA-041 v2 Sektion 22 für Event-Versionierung).
6. Legacy-System-Migrationen ohne Flyway-Vorgeschichte (separates Setup-Projekt).

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Flyway | Versionierungswerkzeug für Datenbankschemas, prüft Checksum aller eingespielten Migrationen | `org.flywaydb:flyway-database-postgresql:10.20.1` |
| V-Migration | Versionsmigration, einmalig in fester Reihenfolge ausgeführt | `V100__create_users_table.sql` |
| R-Migration | Repeatable Migration, bei Checksum-Änderung erneut ausgeführt | `R__001_create_reporting_view.sql` |
| U-Migration | Undo Migration (Flyway Teams, Bezahlfeature) | `U100__drop_users_table.sql` |
| `flyway_schema_history` | Audit-Tabelle aller eingespielten Migrationen mit Checksums | wird automatisch von Flyway angelegt |
| Checksum | SHA-1-Hash einer Migration, zur Erkennung von Änderungen | bricht Flyway-Start bei Mismatch |
| Baseline | Versionsmarke, ab der Flyway eine bestehende DB übernimmt | nur einmal pro DB setzen |
| Schema-Drift | Unterschiede zwischen Dev/Staging/Prod-Schema | das Problem, das Flyway löst |
| Lock Queue Pileup | Wenn eine Migration auf Lock wartet und alle anderen Queries blockiert | klassischer Production-Killer |
| `NOT VALID` Constraint | Constraint, der nur für neue Zeilen gilt, bis manueller `VALIDATE` läuft | non-blocking CHECK/FK |
| `CONCURRENTLY` | PostgreSQL-Option für Index-Erstellung ohne Table-Lock | nur außerhalb Transaktion |
| `SKIP LOCKED` | PostgreSQL-Klausel zur Batch-Verarbeitung ohne Lock-Wait | für Multi-Pod-Backfill-Jobs |
| Forward-Fix | Rollback-Alternative: neue Migration, die Stand wiederherstellt | praktische Realität |
| Schema-pro-Tenant | Multi-Tenant-Pattern, jeder Tenant hat eigenes DB-Schema | bessere Isolation, mehr Operations-Overhead |
| Init-Container | Kubernetes-Pattern: Container läuft vor App-Container | Standard für Flyway-Migration in K8s |
| `BaseJavaMigration` | Flyway-Klasse für Java-basierte Migrationen mit eigenem Transaction-Handling | Alternative zu SQL-Migrationen |

---

## 6. Technischer Hintergrund: Flyway 10+ und PostgreSQL 16+

### 6.1 Flyway 10+ Baseline

Spring Boot 3.4 bringt Flyway 10.x als Default. Flyway 11.x ist seit Q3/2024 verfügbar. Wichtige Änderungen gegenüber Flyway 9.x:

- **Java 17+ Mindestanforderung** (Flyway 9 unterstützte noch Java 8/11).
- **`flyway-database-*` Module** sind separate Dependencies — `flyway-database-postgresql`, `flyway-database-mysql`, etc.
- **PostgreSQL-Lock-Strategien** wurden modernisiert.
- **Native Image Support** mit GraalVM ist erst in 10+ voll ausgebaut.
- **`baselineOnMigrate` Verhalten** wurde strikter bei fehlenden Migrationen.
- **MariaDB, Sybase, Informix** wurden aus Community Edition entfernt — relevant für Plattformwahl.

**Maven-Setup für Flyway 10+ mit PostgreSQL:**

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

**Gradle:**

```kotlin
implementation("org.flywaydb:flyway-core")
implementation("org.flywaydb:flyway-database-postgresql")
```

In Flyway 9.x war `flyway-database-postgresql` Teil von `flyway-core`. Wer ohne explizite Dependency upgraded, bekommt einen `FlywayException: Unsupported Database` zur Laufzeit.

### 6.2 PostgreSQL 16+ als Default-Datenbank

Diese Richtlinie geht von PostgreSQL 16+ als Default-Datenbank aus. Wichtige Features gegenüber älteren Versionen:

- **Seit PostgreSQL 11 (2018):** `ALTER TABLE ADD COLUMN ... NOT NULL DEFAULT <constant>` ist **kein Full Table Rewrite mehr** — der Default wird in Metadaten gespeichert.
- **Seit PostgreSQL 12 (2019):** `REINDEX CONCURRENTLY` möglich (vorher nur `CREATE INDEX CONCURRENTLY`).
- **Seit PostgreSQL 14 (2021):** Verbesserte `VACUUM` und `ANALYZE` Performance bei großen Tabellen.
- **Seit PostgreSQL 16 (2023):** Logical Replication für Standbys, bessere Performance bei Parallel-Queries.

**Diese Richtlinie nutzt diese Features konsequent.** Wer auf älteren PostgreSQL-Versionen läuft, muss manche Patterns anpassen — entsprechende Hinweise stehen in den jeweiligen Sektionen.

### 6.3 Flyway in modernem Spring Boot 3.4

```yaml
spring:
  flyway:
    enabled: true
    locations:
      - classpath:db/migration/schema
      - classpath:db/migration/data
    baseline-on-migrate: false           # ✅ strikter Default
    validate-on-migrate: true            # ✅ Checksums prüfen
    out-of-order: false                  # ✅ keine Migrationen außer der Reihe
    table: flyway_schema_history
    lock-retry-count: 50                 # ✅ Kubernetes-Setup
    init-sql: "SET lock_timeout = '5s'; SET statement_timeout = '600s';"
  jpa:
    hibernate:
      ddl-auto: validate                  # ✅ JPA validiert nur, Flyway managed
```

---

## 7. Niemals `ddl-auto=create` in Produktion

Das ist die **wichtigste Regel** dieser Richtlinie.

### 7.1 Anti-Patterns

```yaml
# ❌ Vernichtet alle Daten beim Start!
spring:
  jpa:
    hibernate:
      ddl-auto: create               # Löscht und erstellt Schema neu

# ❌ Löscht Schema beim Herunterfahren
      ddl-auto: create-drop

# ❌ Unsicherer Kompromiss in Produktion
      ddl-auto: update                # Fügt Spalten hinzu, löscht NIE — Schema-Drift,
                                      # keine Reproduzierbarkeit, keine Versionierung
```

**Was an `update` subtil falsch ist:** Es klingt sicher („fügt nur hinzu, löscht nie") — aber genau das ist das Problem. Engineers verlieren das Bewusstsein, dass jede Änderung in Java-Entities still und unkontrolliert das Schema modifiziert. Bei Mehrfach-Deployments ist das Schema nicht reproduzierbar.

### 7.2 Korrekt

```yaml
# ✅ Flyway übernimmt Schema-Management, JPA validiert
spring:
  jpa:
    hibernate:
      ddl-auto: validate              # Prüft, ob Schema mit Entities übereinstimmt
                                      # → Mismatch wirft beim Start eine Exception

# ✅ Alternative: keine JPA-Schema-Verwaltung
      ddl-auto: none                  # JPA macht gar nichts mit Schema
```

**Empfehlung:** `validate` für Spring-Boot-Services mit JPA-Entities — das fängt Drift zwischen Code und Schema beim Start ab, ohne automatische Modifikation.

### 7.3 Qualitätsregel

`ddl-auto` MUSS in Produktion auf `validate` oder `none` stehen. `create`, `create-drop` und `update` sind verboten. Diese Regel wird per ArchUnit oder Property-Check in CI erzwungen (siehe Sektion 32).

---

## 8. Flyway-Namenskonvention und Versionsschema

### 8.1 Standard-Konvention

```
src/main/resources/db/migration/
├── schema/
│   ├── V100__create_users_table.sql
│   ├── V101__create_orders_table.sql
│   ├── V102__add_email_index_to_users.sql
│   ├── V103__add_shipping_address_to_orders.sql
│   └── V104__rename_user_status_to_active.sql
├── data/
│   ├── V200__seed_country_codes.sql
│   └── V201__seed_default_roles.sql
└── repeatable/
    ├── R__001_create_users_summary_view.sql
    ├── R__002_create_orders_summary_view.sql
    └── R__999_apply_grants.sql

Namensschema:
- V{Version}__{Beschreibung}.sql      ← Versionsmigration (einmalig)
- R__{NumPrefix}_{Beschreibung}.sql   ← Repeatable (bei Checksum-Änderung)
- U{Version}__{Beschreibung}.sql      ← Undo (Flyway Teams, Bezahlfeature)
- __                                    ← Doppelter Unterstrich (Pflicht)
```

### 8.2 Spring-Boot-Konfiguration für getrennte Pfade

```yaml
spring:
  flyway:
    locations:
      - classpath:db/migration/schema
      - classpath:db/migration/data
      - classpath:db/migration/repeatable
```

### 8.3 Versionsschema-Optionen

**Option A: Sequentielle Zahlen mit großem Abstand**

```
V100__create_users.sql
V110__create_orders.sql
V120__create_payments.sql
```

Vorteil: Lesbar. Lücken erlauben Hotfix-Migrationen (V105, V115).
Nachteil: Parallele Branches können kollidieren — zwei Engineers schreiben gleichzeitig V103.

**Option B: Timestamp-basierte Versionen**

```
V20260518101500__create_users.sql
V20260518103000__create_orders.sql
V20260519094500__add_email_index.sql
```

Vorteil: Keine Kollisionen bei parallelen Branches.
Nachteil: Längere Dateinamen, weniger lesbar.

**Empfehlung:** Bei kleinen Teams mit sequenziellem Workflow Option A. Bei großen Teams mit vielen parallelen Branches Option B.

### 8.4 Repeatable-Migration-Sortierung

R-Migrationen werden **nach allen V-Migrationen** ausgeführt, in **alphabetischer Reihenfolge** ihrer Dateinamen. Das hat wichtige Implikationen:

```
R__001_base_view.sql           ← läuft als 1. R
R__002_dependent_view.sql      ← läuft als 2. R, kann von R__001 abhängen
R__999_grants.sql              ← läuft als letzte R
```

**Anti-Pattern ohne Präfix:**

```
R__create_view_a.sql            ← läuft 1. (a kommt vor v)
R__create_view_b.sql            ← läuft 2.
R__create_view_z.sql            ← läuft 3.
R__view_dependent_on_a.sql      ← läuft 4. — funktioniert nur zufällig
```

Wenn `view_dependent_on_a.sql` von `view_b.sql` abhängt, scheitert es. Numerisches Präfix MUSS verwendet werden.

### 8.5 Qualitätsregel

Migrations-Dateinamen MÜSSEN dem Namensschema folgen. Repeatable Migrations MÜSSEN numerisches Präfix haben, um Abhängigkeiten kontrolliert zu sortieren. Schema- und Daten-Migrationen MÜSSEN in getrennten Pfaden liegen.

---

## 9. Migrationen sind unveränderlich

### 9.1 Grundregel

Eine eingecheckte V-Migration DARF NICHT mehr geändert werden. Flyway speichert die Checksum (SHA-1-Hash) jeder eingespielten Migration. Bei Änderung verweigert Flyway den Start mit `MigrationChecksumMismatchException`.

```sql
-- V100__add_email_index_to_users.sql
-- EINMAL eingecheckt → NIE MEHR ÄNDERN
-- Flyway speichert den Checksum-Hash
-- Änderung → Flyway verweigert den Start mit "checksum mismatch"

CREATE UNIQUE INDEX idx_users_email ON users(email);
```

### 9.2 Wenn ein Fehler in V100 entdeckt wird

```sql
-- ❌ V100 ÄNDERN ist verboten

-- ✅ Neue Migration V101__fix_email_index_to_be_case_insensitive.sql
DROP INDEX IF EXISTS idx_users_email;
CREATE UNIQUE INDEX idx_users_email ON users(LOWER(email));
```

### 9.3 Notfall-Reset einer kaputten Checksum

In **Ausnahmefällen** (z. B. nach manuellem Eingriff in eine bereits eingespielte Migration vor Production-Rollout) kann die Checksum repariert werden:

```bash
# CLI-Befehl: setzt Checksum aller eingespielten Migrationen zurück
flyway -url=jdbc:postgresql://... -user=... -password=... repair
```

**Wichtig:** `flyway repair` ist ein **Notfall-Tool**, nicht Teil des normalen Workflows. Es darf nur mit DBA-Freigabe und nach Risikoanalyse in Production verwendet werden.

### 9.4 Qualitätsregel

V-Migrationen sind unveränderlich, sobald sie eingecheckt und gegen eine produktive oder produktionsnahe DB ausgeführt wurden. Fixes erfolgen über neue Folge-Migrationen. `flyway repair` ist Notfall-Tool, nicht Standard-Workflow.

---

## 10. Schema-Migrationen vs. Daten-Migrationen

### 10.1 Warum trennen?

Schema-Änderungen und Datenmanipulationen haben unterschiedliche Charakteristika:

| Aspekt | Schema-Migration | Daten-Migration |
|---|---|---|
| Dauer | meistens Sekunden | kann Minuten/Stunden dauern |
| Lock-Verhalten | DDL-Lock (kurz, aber stark) | Row-Level-Locks (länger, aber weniger blockierend) |
| Rollback | über DROP COLUMN o.Ä. möglich | meistens nicht möglich |
| Idempotenz | über `IF NOT EXISTS` möglich | über Constraint-Check oder `WHERE` |
| Test | trivial in Testcontainers | braucht produktionsähnliche Daten |

Wenn beides in einer Migration zusammengeworfen wird, sind Reviews schwerer und Operations-Probleme häufiger.

### 10.2 Pattern: getrennte Pfade

```
src/main/resources/db/migration/schema/
    V100__create_users.sql
    V101__add_active_column_to_users.sql

src/main/resources/db/migration/data/
    V200__seed_country_codes.sql
    V201__backfill_active_for_existing_users.sql
```

Mit:

```yaml
spring:
  flyway:
    locations:
      - classpath:db/migration/schema
      - classpath:db/migration/data
```

### 10.3 Pattern: Namensschema mit Suffix

```
V100__schema__create_users.sql
V101__schema__add_active_column.sql
V102__data__seed_country_codes.sql
V103__data__backfill_active.sql
```

### 10.4 Wichtig: Daten-Migrationen auf großen Tabellen NICHT in Flyway

Für Datenbackfills auf großen Tabellen (>100k Zeilen) MUSS Application-Layer (Background Job) verwendet werden, nicht eine Flyway-Migration. Details siehe Sektion 18.

### 10.5 Qualitätsregel

Schema-Migrationen und Daten-Migrationen MÜSSEN getrennt sein — entweder über separate Pfade oder über Namensschema-Suffix. Daten-Migrationen auf großen Tabellen MÜSSEN über Application-Layer erfolgen.

---

## 11. Repeatable Migrations und Idempotenz-Pflicht

### 11.1 Was sind Repeatable Migrations?

Repeatable Migrations (`R__...`) werden bei jedem Flyway-Lauf erneut ausgeführt, **wenn sich ihre Checksum geändert hat**. Sie laufen **nach allen V-Migrationen** in alphabetischer Reihenfolge.

### 11.2 Geeignete Anwendungsfälle

**Views:**

```sql
-- R__001_create_users_summary_view.sql
CREATE OR REPLACE VIEW users_summary AS
SELECT
    tenant_id,
    COUNT(*)                            AS user_count,
    COUNT(*) FILTER (WHERE active)      AS active_user_count
FROM users
GROUP BY tenant_id;
```

**Stored Procedures / Functions:**

```sql
-- R__010_calculate_order_total_function.sql
CREATE OR REPLACE FUNCTION calculate_order_total(p_order_id BIGINT)
RETURNS DECIMAL(10, 2)
LANGUAGE plpgsql
AS $$
DECLARE
    total DECIMAL(10, 2);
BEGIN
    SELECT SUM(unit_price * quantity)
    INTO total
    FROM order_items
    WHERE order_id = p_order_id;

    RETURN COALESCE(total, 0);
END;
$$;
```

**Materialized View Refresh:**

```sql
-- R__020_refresh_reporting_view.sql
REFRESH MATERIALIZED VIEW CONCURRENTLY reporting_summary;
```

**Permissions / Grants:**

```sql
-- R__999_apply_grants.sql
GRANT SELECT ON users TO readonly_user;
GRANT SELECT, INSERT, UPDATE ON users TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON users TO admin_user;
```

### 11.3 Anti-Pattern: Nicht-idempotente R-Migrationen

```sql
-- ❌ Anti-Pattern: R__seed_admin.sql
INSERT INTO users (email, role) VALUES ('admin@example.com', 'ADMIN');
-- → Bei jedem Run: Constraint Violation (unique email)
```

### 11.4 Korrekt: Idempotente R-Migrationen

```sql
-- ✅ Idempotent: R__seed_admin.sql
INSERT INTO users (email, role)
VALUES ('admin@example.com', 'ADMIN')
ON CONFLICT (email) DO UPDATE SET role = EXCLUDED.role;
```

### 11.5 Wann NICHT R verwenden

R-Migrationen sind **nicht** geeignet für:

- **`CREATE TABLE`** ohne `IF NOT EXISTS` — nicht idempotent.
- **`CREATE INDEX`** — performance-kritisch, soll nicht bei jedem Run laufen.
- **`ALTER TABLE`** — Schema-Änderungen gehören in V-Migrationen.
- **Datenmanipulationen** ohne Idempotenz-Schutz.

### 11.6 Qualitätsregel

R-Migrationen MÜSSEN idempotent sein — `CREATE OR REPLACE`, `INSERT ... ON CONFLICT DO UPDATE`, `REFRESH MATERIALIZED VIEW`. Nicht-idempotente Statements DÜRFEN NICHT in R-Migrationen verwendet werden.

---

## 12. `baseline-on-migrate` als Anti-Pattern

### 12.1 Das Problem

```yaml
# ❌ Anti-Pattern in application.yml
spring:
  flyway:
    baseline-on-migrate: true       # ⚠️ Klassische silent-skip-Falle
```

`baseline-on-migrate: true` bedeutet: Wenn Flyway eine **nicht-leere DB** ohne `flyway_schema_history` findet, setzt es eine Baseline und **überspringt damit alle Migrationen**, die diese existierenden Tabellen erstellt hätten.

### 12.2 Wann das gefährlich wird

Szenario: Eine neue Test-Umgebung wird hochgefahren mit einem PostgreSQL-Container, der versehentlich aus einem Backup wiederhergestellt wurde. Die DB hat Tabellen aus einem alten Snapshot.

- Mit `baseline-on-migrate: true`: Flyway sieht die existierenden Tabellen, setzt Baseline auf z. B. V1, **überspringt** V2 bis V100 — auch wenn die alte DB nur V1 entsprach. Schema-Drift entsteht, niemand merkt es, bis Code gegen ein nicht-existentes Feld läuft.

- Mit `baseline-on-migrate: false` (Default): Flyway scheitert mit klarer Fehlermeldung. Engineer untersucht und behebt explizit.

### 12.3 Wann `baseline-on-migrate: true` sinnvoll ist

Nur als **einmaliger Setup-Schritt** für eine bestehende DB, die Flyway zum ersten Mal verwendet — und auch dann besser über CLI:

```bash
# Einmaliger Setup-Schritt
flyway baseline -url=jdbc:postgresql://... -user=... -password=... -baselineVersion=0
```

Oder über temporäre Environment-Variable für ein einzelnes Deployment:

```bash
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true ./run-once-migration.sh
# Danach Environment-Variable wieder entfernen
```

### 12.4 Qualitätsregel

`baseline-on-migrate: true` DARF NICHT als Dauer-Default in `application.yml` stehen. Wenn Baseline-Setup nötig ist, dann über CLI oder temporäre Environment-Variable für ein einzelnes Deployment.

---

## 13. Gute Schema-Migrationsskripte

### 13.1 Beispiel-Migration mit allen Konventionen

```sql
-- V100__create_users_table.sql
--
-- Konventionen:
-- ① Idempotenz wo möglich (IF NOT EXISTS, IF EXISTS)
-- ② Explizite Constraint-Namen (für aussagekräftige Fehlermeldungen)
-- ③ Kommentare für nicht-offensichtliche Entscheidungen
-- ④ Keine Daten-Manipulation in Schema-Migrationen (separate V-Datei)
-- ⑤ TIMESTAMPTZ für alle Zeitstempel (UTC-konsistent)
-- ⑥ Index auf Foreign Keys (Performance bei Joins und DELETE-Kaskaden)

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       VARCHAR(36)     NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    password_hash   VARCHAR(60)     NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Explizite Constraint-Namen
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT chk_users_email_format CHECK (email LIKE '%@%'),
    CONSTRAINT chk_users_password_hash_length CHECK (LENGTH(password_hash) = 60)
);

-- Indizes für Performance
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_tenant_active_created
    ON users(tenant_id, active, created_at DESC);

-- Dokumentation im Schema
COMMENT ON TABLE users
    IS 'Core user accounts. Tenant-scoped via tenant_id.';
COMMENT ON COLUMN users.password_hash
    IS 'BCrypt hash, exactly 60 characters.';
```

### 13.2 Migration für Spalten-Umbenennung (rückwärtskompatibel)

Spalten-Umbenennungen MÜSSEN in rückwärtskompatible Schritte aufgeteilt werden, weil während eines Rolling Deployments alte und neue App-Versionen parallel laufen:

```sql
-- V104__add_active_column_to_users.sql
-- Schritt 1: Neue Spalte hinzufügen (nullable, kein Default → schnell)
ALTER TABLE users ADD COLUMN active BOOLEAN;
```

```sql
-- V105__backfill_active_for_existing_users.sql
-- Schritt 2: Daten befüllen (NICHT in einer Migration für große Tabellen!)
-- → bei kleinen Tabellen okay, bei großen siehe Sektion 18 (Application Backfill)
UPDATE users SET active = (status = 'ACTIVE') WHERE active IS NULL;
```

```sql
-- V106__add_active_not_null_constraint.sql
-- Schritt 3: Constraint hinzufügen (NACH Deployment der neuen App-Version)
-- App-Code schreibt jetzt sowohl status als auch active
ALTER TABLE users
    ADD CONSTRAINT chk_users_active_not_null CHECK (active IS NOT NULL) NOT VALID;
ALTER TABLE users VALIDATE CONSTRAINT chk_users_active_not_null;
ALTER TABLE users ALTER COLUMN active SET NOT NULL;
```

```sql
-- V110__drop_old_status_column.sql
-- Schritt 4: NACHDEM kein Code mehr "status" liest oder schreibt
ALTER TABLE users DROP COLUMN status;
```

### 13.3 Qualitätsregel

Schema-Migrationen MÜSSEN explizite Constraint-Namen verwenden, `TIMESTAMPTZ` statt `TIMESTAMP` für alle Zeitstempel, Indizes für alle Foreign Keys und Tenant-Spalten, und Dokumentation über `COMMENT ON TABLE/COLUMN`. Spalten-Umbenennungen MÜSSEN in rückwärtskompatible Schritte aufgeteilt werden.

---

## 14. Zero-Downtime-Migrationen mit PostgreSQL 16+

### 14.1 Was sich seit PostgreSQL 11 geändert hat

Seit PostgreSQL 11 (2018) ist `ALTER TABLE ADD COLUMN ... NOT NULL DEFAULT <constant>` **kein Full Table Rewrite mehr**:

```sql
-- ✅ Sicher seit PostgreSQL 11 (mit konstantem Default!)
ALTER TABLE orders ADD COLUMN priority INTEGER NOT NULL DEFAULT 0;
-- → Default wird in Metadaten gespeichert, kein Rewrite
```

**Aber:** Bei **non-constant Defaults** (z. B. `gen_random_uuid()`) wird die Tabelle weiterhin umgeschrieben:

```sql
-- ❌ Triggert Full Table Rewrite — bei großen Tabellen problematisch
ALTER TABLE users ADD COLUMN external_id UUID NOT NULL DEFAULT gen_random_uuid();
```

Für non-constant Defaults MUSS mehrschrittig migriert werden:

```sql
-- V200__add_external_id_nullable.sql
ALTER TABLE users ADD COLUMN external_id UUID;

-- V201__backfill_external_ids.sql (oder via Application Job, siehe Sektion 18)
-- → für große Tabellen: NICHT hier, sondern in Background Job

-- V202__add_external_id_not_null.sql
ALTER TABLE users
    ADD CONSTRAINT chk_users_external_id_not_null CHECK (external_id IS NOT NULL) NOT VALID;
ALTER TABLE users VALIDATE CONSTRAINT chk_users_external_id_not_null;
ALTER TABLE users ALTER COLUMN external_id SET NOT NULL;
```

### 14.2 Was tatsächlich blockiert in modernem PostgreSQL

| Operation | Blockierend? | Lösung |
|---|---|---|
| `ADD COLUMN ... NULL` | Nein (sofort) | direkt ausführen |
| `ADD COLUMN ... NOT NULL DEFAULT <constant>` | Nein (seit PG 11) | direkt ausführen |
| `ADD COLUMN ... NOT NULL DEFAULT <non-constant>` | Ja (Full Rewrite) | mehrschrittig |
| `ADD CONSTRAINT ... CHECK (...)` | Ja (Full Scan unter Lock) | `NOT VALID` + `VALIDATE` |
| `ADD CONSTRAINT ... FOREIGN KEY` | Ja (Full Scan unter Lock) | `NOT VALID` + `VALIDATE` |
| `ALTER COLUMN ... TYPE` (inkompatibel) | Ja (Full Rewrite) | neue Spalte + Backfill + Drop |
| `ALTER COLUMN ... SET NOT NULL` | Ja (Full Scan unter Lock) | `CHECK NOT VALID` + `VALIDATE` + `SET NOT NULL` |
| `CREATE INDEX` ohne `CONCURRENTLY` | Ja (sperrt Writes) | `CONCURRENTLY` |
| `DROP COLUMN` mit FK | Ja (Kaskade) | mehrschrittig |
| `ADD FOREIGN KEY` | Ja (Full Scan) | `NOT VALID` + `VALIDATE` |

### 14.3 Qualitätsregel

Vor jeder `ALTER TABLE`-Migration MUSS geprüft werden, ob sie blockierend ist. Bei Production-Tabellen über 100k Zeilen MÜSSEN non-blocking Patterns (siehe Sektion 15) verwendet werden.

---

## 15. `NOT VALID` und `VALIDATE CONSTRAINT` für non-blocking Constraints

Das ist das **Production-Standard-Pattern** für CHECK- und FOREIGN-KEY-Constraints auf großen Tabellen.

### 15.1 Das Problem

```sql
-- ❌ Anti-Pattern: validiert ALLE Zeilen unter Lock
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_priority_non_negative CHECK (priority >= 0);
-- → Bei 100M Zeilen: Minuten-langer Table Lock
```

### 15.2 Korrektes Pattern

```sql
-- ✅ Schritt 1: Constraint anlegen, aber existierende Zeilen NICHT prüfen
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_priority_non_negative CHECK (priority >= 0) NOT VALID;
-- → Sekunden statt Minuten
-- → Existierende Zeilen werden NICHT geprüft
-- → Aber: alle NEUEN INSERT/UPDATE werden geprüft

-- ✅ Schritt 2: Constraint nachträglich validieren (Online-Operation)
ALTER TABLE orders VALIDATE CONSTRAINT chk_orders_priority_non_negative;
-- → Scannt zeilenweise, kein Table Lock auf neue Zeilen
-- → Kann parallel zu produktivem Workload laufen
```

### 15.3 Pattern für `SET NOT NULL`

```sql
-- ❌ Anti-Pattern: Full Scan unter Lock
ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
-- → Bei 100M Zeilen: Minuten-langer Lock

-- ✅ Schritt 1: CHECK-Constraint mit NOT VALID
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_priority_not_null CHECK (priority IS NOT NULL) NOT VALID;

-- ✅ Schritt 2: Daten befüllen (über Application Job, siehe Sektion 18)
-- → in Batches, mit SKIP LOCKED, mit Throttling

-- ✅ Schritt 3: Constraint validieren
ALTER TABLE orders VALIDATE CONSTRAINT chk_orders_priority_not_null;
-- → kein Table Lock

-- ✅ Schritt 4: SET NOT NULL — PostgreSQL überspringt Scan dank validierter Constraint
ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
-- → Sekunden statt Minuten
```

### 15.4 Pattern für Foreign Keys

```sql
-- ❌ Anti-Pattern: validiert alle Zeilen
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id);

-- ✅ Schritt 1: FK anlegen ohne Validierung
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
    NOT VALID;

-- ✅ Schritt 2: nachträglich validieren
ALTER TABLE orders VALIDATE CONSTRAINT fk_orders_customer;
```

### 15.5 Qualitätsregel

CHECK- und FOREIGN-KEY-Constraints auf Production-Tabellen über 100k Zeilen MÜSSEN über `NOT VALID` + `VALIDATE CONSTRAINT` angelegt werden. `SET NOT NULL` auf großen Tabellen MUSS über das Vier-Schritt-Pattern (CHECK NOT VALID + Backfill + VALIDATE + SET NOT NULL) erfolgen.

---

## 16. Lock-Timeouts und Statement-Timeouts

### 16.1 Das Problem: Lock Queue Pileup

Wenn eine Migration auf einen Lock wartet, blockiert sie potenziell alle anderen Queries, die auf dieselbe Tabelle wollen — das ist **Lock Queue Pileup**.

Szenario:
1. Migration startet `ALTER TABLE orders ADD COLUMN priority INTEGER`.
2. Ein länger laufender Read-Query hält noch einen Share-Lock auf `orders`.
3. Migration wartet auf Exclusive-Lock.
4. Während sie wartet, kommen neue Queries — die warten alle auf den Migration-Lock.
5. Innerhalb von Sekunden: Hunderte Queries hängen, App-Pods werden unresponsive, Cluster-Outage.

### 16.2 Lösung: Lock-Timeout

```sql
-- V300__add_priority_to_orders.sql
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '60s';

ALTER TABLE orders ADD COLUMN priority INTEGER;
```

`SET LOCAL` setzt die Werte nur für die aktuelle Transaktion. Wenn der Lock nicht innerhalb von 5 Sekunden verfügbar ist, wird die Migration **abgebrochen** — bevor sie andere Queries blockiert.

### 16.3 Global über Flyway-Konfiguration

```yaml
spring:
  flyway:
    init-sql: "SET lock_timeout = '5s'; SET statement_timeout = '600s';"
```

`init-sql` wird vor jeder Migration ausgeführt. Damit gilt das Timeout für alle Migrationen, ohne dass jede Migration es selbst setzen muss.

### 16.4 Statement-Timeout

Statement-Timeout begrenzt die Dauer einer einzelnen SQL-Anweisung:

```sql
-- ✅ Verhindert dass eine ALTER TABLE über 10 Minuten läuft
SET LOCAL statement_timeout = '10min';
ALTER TABLE orders REINDEX;
```

### 16.5 Was passiert bei Timeout?

Wenn `lock_timeout` greift, wird `54P03 lock_not_available` geworfen. Flyway scheitert, die Migration ist als `Failed` in `flyway_schema_history` markiert. Engineer kann:

- Migration zu einer anderen Zeit erneut versuchen (wenn der Lock-Halter beendet ist).
- Lock-Halter explizit identifizieren (`SELECT * FROM pg_locks`) und behandeln.

### 16.6 Qualitätsregel

Production-Migrationen MÜSSEN Lock-Timeout setzen (empfohlen: 5 Sekunden). Statement-Timeout MUSS für lange laufende Operationen gesetzt sein. Lock-Timeout-Abbruch ist erwünschtes Verhalten, nicht Bug.

---

## 17. `CREATE INDEX CONCURRENTLY` und `transactional: false`

### 17.1 Das Problem

```sql
-- ❌ Anti-Pattern auf großen Tabellen
CREATE INDEX idx_users_email ON users(email);
-- → Sperrt alle Writes auf users für die Dauer der Index-Erstellung
-- → Bei 100M Zeilen: 10+ Minuten Schreibblockade
```

### 17.2 Lösung: `CONCURRENTLY`

```sql
-- ✅ Non-blocking Index-Erstellung
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
-- → Kein Table Lock
-- → Reads und Writes laufen weiter
-- → Etwas langsamer als nicht-concurrent
```

### 17.3 Aber: `CONCURRENTLY` kann nicht in einer Transaktion laufen

Flyway führt jede Migration standardmäßig **in einer Transaktion** aus. `CONCURRENTLY` kann **nicht** innerhalb einer Transaktion verwendet werden — PostgreSQL wirft einen Fehler:

```
ERROR: CREATE INDEX CONCURRENTLY cannot run inside a transaction block
```

**Lösung: Migration als nicht-transaktional markieren** (Flyway 8+):

```sql
-- V200__add_email_index.sql
-- Hint to Flyway: this migration runs without a transaction
-- Required for CREATE INDEX CONCURRENTLY

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email ON users(email);
```

In Spring Boot wird das über das `executeInTransaction`-Property pro Migration gesteuert. Bei Java-Migrationen kann das explizit über `BaseJavaMigration.canExecuteInTransaction()` gesetzt werden (siehe Sektion 19).

### 17.4 Defensiv: alten Invalid-Index vor erneutem Versuch droppen

Bei `CREATE INDEX CONCURRENTLY`-Fehlschlag bleibt ein **invalid index** in der DB zurück, der bei nächsten Migrations-Versuchen kollidieren kann:

```sql
-- ✅ Defensiv: erst alten Invalid-Index droppen
DROP INDEX CONCURRENTLY IF EXISTS idx_users_email;
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
```

### 17.5 Qualitätsregel

`CREATE INDEX` auf Production-Tabellen MUSS `CONCURRENTLY` verwenden. Die Migration MUSS als nicht-transaktional gekennzeichnet sein. Defensive `DROP INDEX CONCURRENTLY IF EXISTS` vor `CREATE` ist empfohlen, um Invalid-Index-Probleme zu vermeiden.

---

## 18. Datenbackfills über Application Layer

### 18.1 Das Problem mit Backfills in Flyway

Backfills auf großen Tabellen in einer Flyway-Migration sind problematisch:

1. **Eine einzige Transaktion** — bei mehreren Stunden Laufzeit hängt die ganze App fest.
2. **Kein Resume bei Fehler** — wenn Migration nach 80 % crasht, beginnt sie beim Retry komplett von vorne.
3. **CI/CD-Timeouts** — Pipeline schlägt fehl, weil Migration zu lange läuft.
4. **Lock-Wartezeiten** für Application-Queries — App-Workload wird blockiert.

### 18.2 Lösung: Application-Layer Background Job

Die Schema-Migration legt nur die Spalte an. Daten-Backfill passiert als **Background Job** in der Anwendung — mit eigener Transaction pro Batch, eigenem Error-Handling, Resumption-Logik.

**Schritt 1: Schema-Migration (nur Spalte)**

```sql
-- V100__add_priority_to_orders.sql
SET LOCAL lock_timeout = '5s';
ALTER TABLE orders ADD COLUMN priority INTEGER;
-- → Sekunden, kein Backfill
```

**Schritt 2: Application Job (Background Backfill)**

```java
@Component
@ConditionalOnProperty(
    prefix = "migration.priority-backfill",
    name = "enabled",
    havingValue = "true"
)
public class PriorityBackfillJob {

    private static final Logger log = LoggerFactory.getLogger(PriorityBackfillJob.class);
    private static final int BATCH_SIZE = 1_000;
    private static final long THROTTLE_MS = 100;

    private final JdbcTemplate jdbcTemplate;
    private final BackfillMetrics metrics;

    @Scheduled(fixedDelay = 60_000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backfillBatch() {
        try {
            var updated = jdbcTemplate.update("""
                UPDATE orders
                SET priority = 0
                WHERE id IN (
                    SELECT id FROM orders
                    WHERE priority IS NULL
                    ORDER BY id
                    LIMIT ?
                    FOR UPDATE SKIP LOCKED
                )
                """, BATCH_SIZE);

            metrics.incrementBackfilled(updated);

            if (updated == 0) {
                log.info("Priority backfill complete.");
                return;
            }

            log.info("Backfilled {} rows. Throttling...", updated);
            Thread.sleep(THROTTLE_MS);
        } catch (Exception ex) {
            log.error("Backfill batch failed", ex);
            metrics.incrementBackfillFailures();
        }
    }
}
```

**Schritt 3: NACHDEM Backfill abgeschlossen — neue Migration für Constraint**

```sql
-- V101__add_priority_not_null.sql
-- → wird nach Production-Backfill ausgeführt
SET LOCAL lock_timeout = '5s';
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_priority_not_null CHECK (priority IS NOT NULL) NOT VALID;
ALTER TABLE orders VALIDATE CONSTRAINT chk_orders_priority_not_null;
ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
```

### 18.3 Vorteile

| Aspekt | Migration-Backfill | Application-Backfill |
|---|---|---|
| Transaction | eine lange | viele kurze |
| Lock-Verhalten | Tabelle blockiert | Row-Level (SKIP LOCKED) |
| Resume bei Fehler | nein, Neustart | ja, läuft weiter |
| Throttling | nein | ja, Thread.sleep zwischen Batches |
| Monitoring | nur Logs | Micrometer-Metriken |
| Kontrolle | binär (ein/aus) | feingranular (Feature Flag) |
| CI/CD-Timeout | möglich | nicht relevant |

### 18.4 Pattern für Backfill mit Tenant-Awareness

```java
@Component
@ConditionalOnProperty(
    prefix = "migration.tenant-backfill",
    name = "enabled",
    havingValue = "true"
)
public class TenantPriorityBackfillJob {

    private final JdbcTemplate jdbcTemplate;
    private final TenantContext tenantContext;       // siehe QG-JAVA-006 v2 Sektion 14.6

    @Scheduled(fixedDelay = 60_000)
    public void backfillAllTenants() {
        var tenants = jdbcTemplate.queryForList(
            "SELECT DISTINCT tenant_id FROM orders WHERE priority IS NULL",
            String.class);

        for (var tenantId : tenants) {
            try (var scope = tenantContext.setForCurrentThread(tenantId)) {
                backfillForTenant(tenantId);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backfillForTenant(String tenantId) {
        jdbcTemplate.update("""
            UPDATE orders
            SET priority = 0
            WHERE tenant_id = ?
              AND id IN (
                SELECT id FROM orders
                WHERE tenant_id = ?
                  AND priority IS NULL
                ORDER BY id
                LIMIT 1000
                FOR UPDATE SKIP LOCKED
              )
            """, tenantId, tenantId);
    }
}
```

### 18.5 Qualitätsregel

Datenbackfills auf Production-Tabellen über 100k Zeilen MÜSSEN über Application-Layer Background Jobs erfolgen, nicht in Flyway-Migrationen. Batches MÜSSEN `FOR UPDATE SKIP LOCKED` verwenden, eigene Transaction pro Batch nutzen und throttlen.

---

## 19. Java-Migrationen mit `BaseJavaMigration`

### 19.1 Wann Java-Migration statt SQL?

Java-Migrationen (`BaseJavaMigration`) sind sinnvoll für:

- Komplexe Logik mit Schleifen oder Branching.
- Custom Transaction-Handling (z. B. mehrere kleine Transaktionen statt einer langen).
- Aufruf externer Systeme während Migration (selten, mit Vorsicht).
- Dynamic SQL basierend auf DB-Inhalt.

### 19.2 Beispiel: Java-Migration mit Batch-Backfill

```java
package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class V200__BackfillPriority extends BaseJavaMigration {

    private static final Logger log = LoggerFactory.getLogger(V200__BackfillPriority.class);
    private static final int BATCH_SIZE = 10_000;
    private static final long THROTTLE_MS = 200;

    @Override
    public void migrate(Context context) throws Exception {
        var conn = context.getConnection();
        conn.setAutoCommit(true);            // ✅ keine lange Transaktion

        int totalUpdated = 0;
        int rowsUpdated;

        do {
            try (var stmt = conn.prepareStatement("""
                UPDATE orders SET priority = 0
                WHERE id IN (
                    SELECT id FROM orders
                    WHERE priority IS NULL
                    LIMIT ?
                    FOR UPDATE SKIP LOCKED
                )
                """)) {
                stmt.setInt(1, BATCH_SIZE);
                rowsUpdated = stmt.executeUpdate();
                totalUpdated += rowsUpdated;

                if (rowsUpdated > 0) {
                    log.info("Backfilled batch of {} rows ({} total)", rowsUpdated, totalUpdated);
                    Thread.sleep(THROTTLE_MS);    // throttle
                }
            }
        } while (rowsUpdated > 0);

        log.info("Backfill complete: {} rows updated total", totalUpdated);
    }

    @Override
    public boolean canExecuteInTransaction() {
        return false;     // ✅ wegen autoCommit
    }
}
```

### 19.3 Wichtige Hinweise

1. **Klassenname** MUSS dem Schema `V<Version>__<Beschreibung>` folgen (analog zu SQL-Migrationen).
2. **Pfad** MUSS `db.migration` Package sein (oder über `spring.flyway.locations: classpath:custom/path` umkonfiguriert).
3. **`canExecuteInTransaction()` = false** bei autoCommit-basierten Patterns nötig.
4. **Java-Migrationen sind teurer** als SQL-Migrationen (Klassen-Loading, Reflection). Für einfache Operationen SQL bevorzugen.

### 19.4 Qualitätsregel

Java-Migrationen SOLLTEN für komplexe Logik mit Batch-Handling oder dynamischem SQL verwendet werden. Für einfache Schema- oder Daten-Änderungen sind SQL-Migrationen vorzuziehen — sie sind lesbarer und leichter zu reviewen.

---

## 20. Multi-Tenant: Schema-pro-Tenant und programmatische Migration

In SaaS-Setups gibt es zwei Hauptpatterns für Multi-Tenant-Isolation:

### 20.1 Pattern A: Tenant-ID-Spalte (Single Schema)

Alle Tenants teilen sich dieselben Tabellen, getrennt durch eine `tenant_id`-Spalte. Standard-Flyway funktioniert ohne Anpassung.

**Voraussetzungen:**

- Jede Tabelle mit Tenant-Daten hat `tenant_id` als NOT NULL Spalte.
- Index auf `tenant_id` (und ggf. compound Indizes).
- Row-Level Security (RLS) oder explizite `WHERE tenant_id = ?`-Filter in jedem Query.

**Migrations-Spezifika:**

- Schema-Änderungen gelten für alle Tenants gleichzeitig.
- Daten-Backfills auf großen Tabellen müssen tenant-aware sein (siehe Sektion 18.4).
- Tenant-Onboarding ist trivial — neue Zeilen mit neuer `tenant_id`.

### 20.2 Pattern B: Schema-pro-Tenant

Jeder Tenant hat ein eigenes Datenbankschema. Migrations laufen pro Tenant.

**Vorteile:**

- Bessere Isolation (Backup pro Tenant, GDPR-Löschung trivial).
- Performance bei großen Tenants (eigene Indizes pro Tenant).
- Tenant-spezifische Schema-Variation möglich.

**Nachteile:**

- Operations-Overhead (N Schemas zu migrieren).
- Tenant-Onboarding braucht programmatische Migration.

### 20.3 Programmatische Migration bei Tenant-Onboarding

```java
@Service
public class TenantMigrationService {

    private static final Logger log = LoggerFactory.getLogger(TenantMigrationService.class);

    private final DataSource dataSource;

    public TenantMigrationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener
    public void onTenantCreated(TenantCreatedEvent event) {
        log.info("Migrating new tenant schema: {}", event.tenantId());

        var schemaName = "tenant_" + event.tenantId();

        var flyway = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schemaName)
            .createSchemas(true)                              // Schema erzeugen
            .locations("classpath:db/migration/tenant")       // Pfad mit Tenant-Migrationen
            .table("flyway_schema_history")                   // pro Tenant eigene History
            .load();

        flyway.migrate();

        log.info("Tenant schema {} migrated to version {}",
            schemaName, flyway.info().current().getVersion());
    }
}
```

**Wichtig:** `flyway.migrate()` ist eine synchrone Operation und kann mehrere Sekunden dauern. Bei Tenant-Onboarding über REST-API SOLLTE das asynchron passieren (z. B. via Kafka, siehe QG-JAVA-041 v2).

### 20.4 Migrations-Pfad-Struktur

```
src/main/resources/
├── db/migration/
│   ├── global/                  ← gilt für alle Tenants (Shared Tables)
│   │   ├── V100__create_tenants_table.sql
│   │   └── V101__create_users_table.sql
│   └── tenant/                  ← gilt pro Tenant-Schema
│       ├── V100__create_orders_table.sql
│       ├── V101__create_invoices_table.sql
│       └── V102__add_priority_to_orders.sql
```

**Spring-Boot-Konfiguration:**

```yaml
spring:
  flyway:
    enabled: true
    locations:
      - classpath:db/migration/global
    # Tenant-spezifische Locations werden programmatisch genutzt
```

### 20.5 Onboarding und Bestandstenant-Migration

Bei einer neuen Schema-Version müssen **alle bestehenden Tenants** migriert werden:

```java
@Service
public class TenantMigrationBatchService {

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    @Scheduled(cron = "0 0 2 * * *")          // täglich 02:00 UTC
    public void migrateAllTenants() {
        var allTenants = tenantRepository.findAllActiveTenantIds();

        for (var tenantId : allTenants) {
            try {
                migrateTenant(tenantId);
            } catch (Exception ex) {
                log.error("Migration failed for tenant {}", tenantId, ex);
                // Alert auslösen, aber andere Tenants weitermachen
            }
        }
    }

    private void migrateTenant(String tenantId) {
        var schemaName = "tenant_" + tenantId;

        Flyway.configure()
            .dataSource(dataSource)
            .schemas(schemaName)
            .locations("classpath:db/migration/tenant")
            .load()
            .migrate();
    }
}
```

### 20.6 Qualitätsregel

Multi-Tenant-Setups MÜSSEN explizit modelliert sein. Bei Pattern A (Tenant-ID-Spalte) MUSS jede Migration tenant-aware Backfills berücksichtigen. Bei Pattern B (Schema-pro-Tenant) MUSS programmatische Migration bei Onboarding und für Bestandstenants implementiert sein.

Cross-Reference: QG-JAVA-006 v2 Sektion 14.6 (Tenant-Context-Pattern).

---

## 21. Spring-Boot-Konfiguration

### 21.1 Produktionskonfiguration

```yaml
spring:
  flyway:
    enabled: true
    locations:
      - classpath:db/migration/schema
      - classpath:db/migration/data
    baseline-on-migrate: false             # ✅ strikt, kein silent skip
    validate-on-migrate: true              # ✅ Checksums prüfen
    out-of-order: false                    # ✅ keine out-of-order in Produktion
    table: flyway_schema_history
    lock-retry-count: 50                   # ✅ Kubernetes-Setup
    init-sql: "SET lock_timeout = '5s'; SET statement_timeout = '600s';"
    user: ${FLYWAY_DB_USER}                # ✅ separater Migration-User
    password: ${FLYWAY_DB_PASSWORD}
    url: ${DATABASE_URL}
  datasource:
    username: ${APP_DB_USER}               # ✅ separater App-User (DML-Rechte)
    password: ${APP_DB_PASSWORD}
    url: ${DATABASE_URL}
  jpa:
    hibernate:
      ddl-auto: validate                   # ✅ JPA validiert, Flyway managed
```

### 21.2 Wichtige Properties im Detail

| Property | Wert | Begründung |
|---|---|---|
| `enabled` | `true` | Flyway aktivieren |
| `baseline-on-migrate` | `false` | Strikt — kein silent skip bei nicht-leerer DB |
| `validate-on-migrate` | `true` | Checksum-Prüfung bei jedem Start |
| `out-of-order` | `false` (Prod), `true` (Dev) | Keine nachträglichen Migrationen in Produktion |
| `lock-retry-count` | `50` | Bei Multi-Pod-Deployments wichtig |
| `init-sql` | `SET lock_timeout=...` | Vor jeder Migration ausführen |
| `placeholders` | je nach Bedarf | Substitution in Migrations-Skripten |
| `clean-disabled` | `true` (Prod) | Verhindert versehentliches `flyway clean` |

### 21.3 Development-Konfiguration

```yaml
spring:
  config:
    activate:
      on-profile: dev
  flyway:
    out-of-order: true                     # Entwickler-freundlich
    clean-on-validation-error: false       # Niemals!
    placeholders:
      tenant_id: "dev-tenant"
```

### 21.4 Qualitätsregel

Spring-Boot-Konfiguration MUSS getrennte DB-User für Flyway-Migration und App-Workload verwenden. `baseline-on-migrate` MUSS auf `false` stehen. `validate-on-migrate` MUSS auf `true` stehen. Lock-Timeout MUSS via `init-sql` gesetzt sein.

---

## 22. Kubernetes-Init-Container-Pattern

### 22.1 Das Problem mit Multi-Pod-Deployments

In Kubernetes laufen typischerweise 3+ App-Pods. Ohne spezielle Behandlung starten alle gleichzeitig — und alle versuchen, Flyway-Migrationen zu starten. Das führt zu:

- **Lock-Contention** auf `flyway_schema_history`.
- **Race Conditions** bei der Versions-Erkennung.
- **Wartezeiten** beim App-Start (alle Pods warten auf den ersten).

### 22.2 Lösung: Init-Container-Pattern

Migration läuft **einmal** vor allen App-Pods, über einen Kubernetes Init-Container oder Job. Die App-Pods starten erst, wenn die Migration erfolgreich war.

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 3
  template:
    spec:
      initContainers:
        - name: flyway-migrate
          image: flyway/flyway:10.20.1
          args:
            - "-url=jdbc:postgresql://db:5432/mydb"
            - "-user=$(FLYWAY_USER)"
            - "-password=$(FLYWAY_PASSWORD)"
            - "-locations=filesystem:/flyway/sql"
            - "migrate"
          env:
            - name: FLYWAY_USER
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: flyway-user
            - name: FLYWAY_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: flyway-password
          volumeMounts:
            - name: migrations
              mountPath: /flyway/sql
      containers:
        - name: app
          image: my-app:1.0.0
          # ... läuft erst nach erfolgreichem Init-Container
      volumes:
        - name: migrations
          configMap:
            name: db-migrations
```

### 22.3 Alternative: Pre-Deploy Job

```yaml
# job.yaml — läuft als Helm-Hook vor Deployment
apiVersion: batch/v1
kind: Job
metadata:
  name: db-migration
  annotations:
    "helm.sh/hook": pre-install,pre-upgrade
    "helm.sh/hook-delete-policy": before-hook-creation,hook-succeeded
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
        - name: flyway-migrate
          image: flyway/flyway:10.20.1
          args: ["-url=...", "migrate"]
  backoffLimit: 3
```

### 22.4 In Spring Boot: Flyway zur Laufzeit deaktivieren

Wenn Migrationen über Init-Container/Job laufen, MUSS Flyway in der App selbst deaktiviert sein:

```yaml
# application-k8s.yml
spring:
  flyway:
    enabled: false                         # ✅ App führt keine Migrations aus
  jpa:
    hibernate:
      ddl-auto: validate                   # ✅ App validiert Schema
```

### 22.5 Qualitätsregel

In Kubernetes-Deployments mit mehreren Pods MUSS Flyway über Init-Container oder Job laufen, nicht parallel in mehreren App-Pods. App-Pods MÜSSEN Flyway zur Laufzeit deaktivieren (`spring.flyway.enabled=false`).

---

## 23. Separate DB-User für Migration und App

### 23.1 Warum trennen?

Schema-Migrationen brauchen `DDL`-Rechte (CREATE TABLE, ALTER, DROP). Application-Code braucht nur `DML`-Rechte (SELECT, INSERT, UPDATE, DELETE). Wenn beides denselben User nutzt, hat ein kompromittierter App-Pod die Möglichkeit, Schema-Änderungen vorzunehmen.

### 23.2 Pattern: Zwei User

```sql
-- Setup-Skript (einmalig, DBA-Aufgabe)
CREATE USER flyway_migrator WITH PASSWORD '...';
CREATE USER app_runtime WITH PASSWORD '...';

-- Migration-User: DDL-Rechte
GRANT ALL ON SCHEMA public TO flyway_migrator;

-- App-User: nur DML
GRANT USAGE ON SCHEMA public TO app_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_runtime;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO app_runtime;

-- Default-Privilegien für neue Tabellen (automatisch)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE ON SEQUENCES TO app_runtime;
```

### 23.3 Spring-Boot-Konfiguration

```yaml
spring:
  flyway:
    user: ${FLYWAY_DB_USER}            # flyway_migrator
    password: ${FLYWAY_DB_PASSWORD}
    url: ${DATABASE_URL}
  datasource:
    username: ${APP_DB_USER}           # app_runtime
    password: ${APP_DB_PASSWORD}
    url: ${DATABASE_URL}
```

### 23.4 Secret Management

DB-Credentials DÜRFEN NICHT plain in `application.yml` stehen. Bezug über:

- **Kubernetes Secrets** (`valueFrom.secretKeyRef`).
- **HashiCorp Vault** (`spring-cloud-vault`).
- **Cloud Secret Manager** (AWS Secrets Manager, GCP Secret Manager, Azure Key Vault).
- **External Secrets Operator** für Kubernetes.

### 23.5 Qualitätsregel

Produktive Setups MÜSSEN getrennte DB-User für Flyway-Migration (DDL-Rechte) und App-Workload (DML-Rechte) verwenden. Credentials MÜSSEN über Secret Management bezogen werden, nicht plain in `application.yml`.

---

## 24. Rollback-Strategie: Forward-Fix als Standard

### 24.1 Die ehrliche Realität

In der Praxis sind Datenbankmigrationen **meistens nicht rollback-fähig**. Was die meisten Teams als „Rollback" bezeichnen, ist tatsächlich:

- **Forward-Fix:** Eine neue Migration, die den vorherigen Stand wiederherstellt.
- **Database Restore:** Aus Backup (sehr selten, oft Stunden-Operation).
- **Manuelle Reparatur:** DBA-Intervention bei kritischen Bugs.

### 24.2 Warum kein klassisches Rollback?

Bei **Schema-only-Änderungen** (neue Spalte, neuer Index) ist Rollback theoretisch möglich:

```sql
-- V100__add_priority_to_orders.sql
ALTER TABLE orders ADD COLUMN priority INTEGER;

-- U100__remove_priority_from_orders.sql (Undo)
ALTER TABLE orders DROP COLUMN priority;
```

**Aber:** Bei **Datenänderungen** ist Rollback fast nie möglich, weil Daten geändert wurden und nicht rekonstruiert werden können (außer aus Backup).

**Außerdem:** Undo-Migrationen sind ein **Flyway Teams Feature** (Bezahlfeature). In der Community Edition existiert kein automatisches `flyway undo`.

### 24.3 Korrektes Pattern: Forward-Fix

```sql
-- V100__add_priority_to_orders.sql (deployed in Production, später Fehler entdeckt)
ALTER TABLE orders ADD COLUMN priority INTEGER NOT NULL DEFAULT 0;
```

```sql
-- V101__forward_fix_remove_priority.sql (neue Migration, statt Rollback)
SET LOCAL lock_timeout = '5s';
ALTER TABLE orders DROP COLUMN priority;
```

Das ist die **realistische Strategie** für Production:

1. App-Code-Rollback via Container-Image-Rollback (schnell, sicher).
2. Schema-State bleibt — oft kompatibel mit alter App-Version.
3. Wenn Schema-Rollback nötig: neue Migration.

### 24.4 Wann ist klassisches Rollback praktikabel?

| Situation | Rollback praktikabel? |
|---|---|
| **Schema-only Addition** (neue Spalte/Index) | Ja, über Forward-Fix-Migration |
| **Datenänderungen ohne Backup** | Nein |
| **Datenänderungen mit Audit-Tabelle** | Bedingt möglich |
| **Spaltenlöschung** | Nein, ohne Backup |
| **Typänderung** | Nein, ohne Backup |
| **Aus Backup wiederhergestellt** | Ja, aber Stunden-Operation |

### 24.5 Pre-Production Discipline

Die beste Rollback-Strategie ist **Pre-Production Discipline**:

1. Jede Migration wird in Staging gegen Production-Snapshot getestet.
2. Performance-Tests mit produktionsähnlichen Datenmengen.
3. Rolling Deployment mit Wartezeit zwischen Schema-Migration und Code-Deployment.
4. Feature Flags (siehe QG-JAVA-039-01 v2), um Schema-abhängigen Code stufenweise zu aktivieren.

### 24.6 Audit-Tabelle für datenkritische Migrationen

Wenn eine Daten-Migration kritische Werte ändert, kann eine Audit-Tabelle einen Rollback ermöglichen:

```sql
-- V100__migrate_order_status_with_audit.sql
CREATE TABLE migration_audit_v100 AS
SELECT id, status, NOW() AS audited_at
FROM orders
WHERE status IS NOT NULL;

UPDATE orders SET status = LOWER(status) WHERE status IS NOT NULL;
```

```sql
-- V101__rollback_migration_v100.sql (falls nötig)
UPDATE orders SET status = a.status
FROM migration_audit_v100 a
WHERE orders.id = a.id;
```

### 24.7 Qualitätsregel

Rollback-Strategie ist primär **Forward-Fix** über neue Migration. Klassische Undo-Migrationen sind in Community Edition nicht verfügbar. Für datenkritische Migrationen MUSS Audit-Tabelle angelegt werden, die rückwirkende Wiederherstellung erlaubt. Pre-Production Discipline (Staging-Tests mit Production-Snapshot) ist die beste Rollback-Strategie.

---

## 25. `out-of-order` und Timestamp-basierte Versionierung

### 25.1 Das Problem mit parallelen Branches

Szenario: Drei Engineers arbeiten parallel auf Feature-Branches.

- Alice mergt PR mit `V100__add_address.sql`.
- Bob hatte `V101__add_phone.sql` bereits in seinem Branch — mergt nach Alice.
- Charlie hatte `V102__add_email_index.sql` parallel — mergt nach Bob.

Soweit gut. Aber: Dave hat einen Hotfix für ein Problem in `V100`, schreibt `V100_5__urgent_fix.sql` und will diesen **nachträglich** einfügen.

Mit `out-of-order: false` (Default): Flyway weigert sich. V100_5 kann nicht zwischen bereits ausgeführten Migrationen eingefügt werden.

Mit `out-of-order: true`: Flyway erlaubt nachträgliche Ausführung — aber `flyway_schema_history` ist nicht mehr aufsteigend.

### 25.2 Best Practice je nach Umgebung

| Umgebung | `out-of-order` |
|---|---|
| Production | `false` (strikt) |
| Staging | `false` (Production-äquivalent) |
| Development | `true` (Engineers wechseln Branches) |
| CI Tests | `false` (deterministisch) |

### 25.3 Vermeidung über Timestamp-basierte Versionierung

```
V20260518101500__create_users.sql
V20260519094500__add_email_index.sql
V20260520143200__add_priority_column.sql
```

Vorteile:

- Keine Kollisionen bei parallelen Branches — jede Migration hat einen einzigartigen Timestamp.
- `out-of-order: false` funktioniert problemlos, weil neue Migrationen IMMER neueren Timestamp haben.
- Reihenfolge in `flyway_schema_history` ist chronologisch.

Nachteile:

- Längere Dateinamen.
- Manuelles Auflisten (keine direkten Lücken zu sehen).

### 25.4 Empfehlung

| Team-Größe | Workflow | Empfehlung |
|---|---|---|
| Klein (2-5 Engineers) | Sequenziell | V100, V101, V102 mit `out-of-order: false` |
| Mittel (5-15 Engineers) | Mehrere Branches | V100 mit 10-Schritt-Bereichen (V100-V109 für Feature A) |
| Groß (15+ Engineers) | Viele Branches | Timestamp-basierte Versionen |

### 25.5 Qualitätsregel

`out-of-order: false` in Production. Timestamp-basierte Versionierung SOLLTE bei großen Teams mit vielen parallelen Branches verwendet werden, um Kollisionen zu vermeiden.

---

## 26. Testumgebung: Flyway + Testcontainers

### 26.1 Pattern

Testcontainers startet eine echte PostgreSQL-Instanz. Flyway läuft automatisch beim Spring-Context-Start und spielt alle Migrationen ein — Tests laufen gegen **dasselbe Schema** wie Produktion.

```java
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Testcontainers
@Import(TestcontainersConfig.class)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void shouldFindActiveUsers() {
        // Flyway hat bereits alle Migrationen eingespielt
        // Test läuft gegen Production-äquivalentes Schema
        // ...
    }
}
```

### 26.2 Wichtige Konfiguration für Tests

```yaml
# src/test/resources/application-test.yml
spring:
  flyway:
    enabled: true
    locations:
      - classpath:db/migration/schema
      - classpath:db/migration/data
    # Im Test KEIN Init-Container, kein separater User
    clean-disabled: false      # ✅ Tests dürfen sauber starten
  jpa:
    hibernate:
      ddl-auto: validate
```

### 26.3 Wann Tests ohne Flyway?

Für **schmale Unit-Tests** (z. B. Repository-Tests gegen ein In-Memory-DB) kann Flyway umgangen werden:

```yaml
spring:
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop      # ✅ nur in Tests akzeptabel
```

Aber: Diese Tests prüfen NICHT, ob die JPA-Entities mit dem Flyway-Schema kompatibel sind. Mindestens ein Integration-Test pro Repository SOLLTE gegen Testcontainers + Flyway laufen.

### 26.4 Qualitätsregel

Integration-Tests MÜSSEN gegen Testcontainers + Flyway laufen, um Schema-App-Konsistenz zu prüfen. Unit-Tests dürfen H2 oder ähnliches verwenden, aber mindestens ein Repository-Test pro Entity MUSS gegen Production-äquivalentes Schema laufen.

---

## 27. CI/CD-Integration

### 27.1 Pre-Merge-Validation

Vor jedem Pull Request Merge MUSS Flyway-Validation laufen:

```yaml
# .github/workflows/pr-checks.yml
name: PR Validation

on:
  pull_request:
    branches: [main]

jobs:
  flyway-validate:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: test
          POSTGRES_DB: test
        ports:
          - 5432:5432
    steps:
      - uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Flyway Validate
        run: |
          ./mvnw flyway:validate \
            -Dflyway.url=jdbc:postgresql://localhost:5432/test \
            -Dflyway.user=postgres \
            -Dflyway.password=test

      - name: Flyway Info
        run: |
          ./mvnw flyway:info \
            -Dflyway.url=jdbc:postgresql://localhost:5432/test \
            -Dflyway.user=postgres \
            -Dflyway.password=test
```

### 27.2 Production-Deployment-Pipeline

```yaml
# .github/workflows/production-deploy.yml
name: Production Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build Application
        run: ./mvnw clean package

      - name: Flyway Migrate (Production)
        run: |
          docker run --rm \
            -v $(pwd)/src/main/resources/db/migration:/flyway/sql \
            flyway/flyway:10.20.1 \
            -url=${{ secrets.PROD_DB_URL }} \
            -user=${{ secrets.FLYWAY_USER }} \
            -password=${{ secrets.FLYWAY_PASSWORD }} \
            -locations=filesystem:/flyway/sql/schema,filesystem:/flyway/sql/data \
            migrate

      - name: Deploy Application
        run: |
          kubectl apply -f k8s/deployment.yaml
```

### 27.3 Migration-Test gegen Staging-Snapshot

Vor jedem Production-Deployment SOLLTE die Migration gegen einen Snapshot der Production-DB getestet werden:

```bash
# Pre-Production: Staging mit Production-Snapshot füttern
pg_dump $PROD_DB_URL | psql $STAGING_DB_URL

# Flyway Migration auf Staging testen
./mvnw flyway:migrate -Dflyway.url=$STAGING_DB_URL

# Performance-Tests laufen
./run-performance-tests.sh $STAGING_DB_URL
```

### 27.4 Qualitätsregel

CI MUSS bei jedem Pull Request `flyway validate` und `flyway info` ausführen. Production-Deployments MÜSSEN Migration über separaten Schritt (Init-Container oder Pipeline-Step) ausführen, nicht in der App. Migrations-Tests gegen Production-Snapshot SOLLTEN vor jedem Major-Migration-Deployment laufen.

---

## 28. Datenbankspezifische Aspekte

Diese Richtlinie geht von **PostgreSQL 16+** als Default aus. Andere Datenbanken haben eigene Quirks:

### 28.1 PostgreSQL 16+ (Default)

- `ALTER TABLE ADD COLUMN ... NOT NULL DEFAULT <constant>` ist non-blocking seit PG 11.
- `CREATE INDEX CONCURRENTLY` für Online-Index-Erstellung.
- `NOT VALID` + `VALIDATE CONSTRAINT` für non-blocking CHECK/FK.
- `pg_repack` / `pg_squeeze` für massive Datenmigrationen.

### 28.2 MySQL / MariaDB

- `ALTER TABLE` ist standardmäßig **nicht online** — sperrt Writes.
- Tools wie `pt-online-schema-change` (Percona) oder `gh-ost` (GitHub) sind Pflicht für große Tabellen.
- Flyway-Migrationen SOLLTEN diese Tools als Schritt einbinden, statt direkt `ALTER TABLE` auszuführen.

```bash
# Pattern für große MySQL-Tabellen
pt-online-schema-change --alter "ADD COLUMN priority INT" D=mydb,t=orders --execute
```

### 28.3 Oracle

- `ALTER TABLE` ist meistens online dank Oracle's Architecture.
- DDL-Statements sind in Oracle nicht transaktional — Flyway-Verhalten ist anders.
- `flyway-database-oracle` ist seit Flyway 10 separate Dependency.

### 28.4 SQL Server

- `ALTER TABLE ADD COLUMN` mit Default ist seit SQL Server 2012 instant.
- `CREATE INDEX WITH (ONLINE = ON)` für non-blocking Index (Enterprise Edition).

### 28.5 Qualitätsregel

Bei Datenbanken außer PostgreSQL 16+ MÜSSEN spezifische Patterns berücksichtigt werden. Datenbankversion und -typ MÜSSEN in Migrations-Skripten kommentiert werden, wenn datenbankspezifische Features verwendet werden.

---

## 29. Anti-Patterns

### 29.1 `ddl-auto=create` in Produktion

```yaml
# ❌ Verheerend
spring:
  jpa:
    hibernate:
      ddl-auto: create
```

Siehe Sektion 7.

### 29.2 Migration nachträglich ändern

```sql
-- ❌ V100 wurde vor 3 Monaten deployed
-- Heute kommt Engineer und ändert V100:
CREATE INDEX idx_users_email ON users(email);
-- ↑ neu hinzugefügt → Checksum-Mismatch → Production-Start scheitert
```

Siehe Sektion 9.

### 29.3 `baseline-on-migrate: true` als Default

Siehe Sektion 12.

### 29.4 Nicht-idempotente R-Migration

```sql
-- ❌ R__seed_admin.sql
INSERT INTO users (email, role) VALUES ('admin@example.com', 'ADMIN');
-- ↑ Bei jedem Run: Constraint Violation
```

Siehe Sektion 11.

### 29.5 Backfill in Flyway-Migration

```sql
-- ❌ V100__add_priority_with_backfill.sql
ALTER TABLE orders ADD COLUMN priority INTEGER;
UPDATE orders SET priority = calculate_priority(...);    -- 50M Zeilen!
ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
-- ↑ Stunden-langer Lock, alles in einer Transaktion
```

Siehe Sektion 18.

### 29.6 `CREATE INDEX` ohne `CONCURRENTLY`

```sql
-- ❌ Sperrt Writes auf großer Tabelle
CREATE INDEX idx_orders_customer ON orders(customer_id);
```

Siehe Sektion 17.

### 29.7 Foreign Key ohne `NOT VALID`

```sql
-- ❌ Full Table Scan unter Lock
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id);
```

Siehe Sektion 15.

### 29.8 Migration ohne Lock-Timeout

```sql
-- ❌ Kann ewig auf Lock warten und Cluster lahmlegen
ALTER TABLE orders ADD COLUMN priority INTEGER;
```

Siehe Sektion 16.

### 29.9 DB-Credentials in `application.yml`

```yaml
# ❌ Verheerend
spring:
  datasource:
    password: my-secret-password-123
```

Siehe Sektion 23.

### 29.10 Multiple Pods starten Flyway parallel

Ohne Init-Container racen alle App-Pods auf die Migration. Siehe Sektion 22.

### 29.11 Single DB-User für Migration und App

```yaml
# ❌ Sicherheitsrisiko
spring:
  flyway:
    user: app_user           # hat DDL-Rechte!
  datasource:
    username: app_user       # selber User wie Flyway
```

Siehe Sektion 23.

### 29.12 Migration ohne Pre-Production-Test

Engineers, die direkt auf Production migrieren, ohne Staging-Test mit Production-Snapshot — klassisches Recipe for Disaster. Siehe Sektion 24/27.

---

## 30. Migration bestehender Setups

### 30.1 Vorgehen bei Legacy-Anwendungen ohne Flyway

1. **Schema-Snapshot** erstellen via `pg_dump --schema-only` oder Migration-Tool.
2. **Baseline-Skript V0__baseline.sql** mit aktuellem Schema.
3. **Flyway aktivieren** mit `baseline-on-migrate: true` (einmalig).
4. **Erste echte Migration V1__... schreiben** für neue Änderungen.
5. **`baseline-on-migrate: true` entfernen** für künftige Deployments.

### 30.2 Vorgehen bei `ddl-auto=update` Setups

1. **Aktuelles Schema dumpen** und als V0-Baseline einchecken.
2. **`ddl-auto=validate` setzen** und gegen Staging testen.
3. **Schema-Drift identifizieren** (JPA-Entities vs. DB-Schema).
4. **Drift in V1-Migration konsolidieren**.
5. **Production-Deployment** mit Backup vorher.

### 30.3 Vorgehen bei Anti-Patterns aus Sektion 29

1. **Setter-Nutzung suchen** in Migration-Skripten.
2. **Nicht-idempotente R-Migrationen** identifizieren und idempotent machen.
3. **Backfill-Migrationen** durch Application Layer Jobs ersetzen.
4. **Lock-Timeouts** in `init-sql` zentral einführen.
5. **Separate DB-User** etablieren.
6. **CI Validation** ergänzen.

### 30.4 Multi-Tenant-Migration einführen

1. **Aktuellen Stand bewerten** (Single Schema oder Schema-pro-Tenant?).
2. **Tenant-ID-Spalten** ergänzen, falls fehlend.
3. **Indizes** auf `tenant_id` anlegen.
4. **Application-Filter** in Repositories einbauen (siehe QG-JAVA-006 v2 Sektion 14).
5. **Tenant-Onboarding-Service** implementieren.
6. **Bestandstenant-Migration** als Batch-Job.

---

## 31. Review-Checkliste

Im Pull Request sind folgende Fragen zu prüfen. Jede Zeile verweist auf die Detail-Sektion mit der Begründung.

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| `ddl-auto` | Ist `ddl-auto` auf `validate` oder `none`? | §7 |
| Namensschema | Folgen Migrationen `V<n>__<...>.sql` / `R__<n>_<...>.sql`? | §8.1 |
| Unveränderlichkeit | Werden bestehende Migrationen nicht geändert? | §9 |
| Schema vs. Daten | Sind Schema- und Daten-Migrationen getrennt? | §10 |
| R-Idempotenz | Sind R-Migrationen idempotent? | §11 |
| `baseline-on-migrate` | Steht es auf `false` als Default? | §12 |
| Constraint-Namen | Sind alle Constraints explizit benannt? | §13 |
| `TIMESTAMPTZ` | Werden Zeitstempel mit Timezone gespeichert? | §13 |
| Rückwärtskompatibel | Sind Schema-Änderungen rückwärtskompatibel zur alten App-Version? | §13.2 |
| PostgreSQL 11+ Verhalten | Wird `NOT NULL DEFAULT <const>` als non-blocking erkannt? | §14.1 |
| `NOT VALID` | Werden CHECK/FK auf großen Tabellen mit `NOT VALID` angelegt? | §15 |
| Lock-Timeout | Ist `init-sql` mit `lock_timeout` gesetzt? | §16 |
| `CONCURRENTLY` | Wird `CREATE INDEX CONCURRENTLY` mit `transactional: false` verwendet? | §17 |
| Backfill | Werden Backfills > 100k Zeilen über Application Layer ausgeführt? | §18 |
| Java-Migration | Wird bei komplexer Logik `BaseJavaMigration` mit `canExecuteInTransaction() = false` verwendet? | §19 |
| Multi-Tenant | Ist Multi-Tenant-Setup explizit modelliert? | §20 |
| Spring-Konfiguration | Sind getrennte DB-User für Migration und App konfiguriert? | §21, §23 |
| Init-Container | Läuft Flyway in K8s über Init-Container oder Job, nicht in App-Pods? | §22 |
| Secret Management | Sind DB-Credentials über Secret Management bezogen? | §23.4 |
| Rollback-Strategie | Ist Forward-Fix-Strategie dokumentiert? | §24 |
| `out-of-order` | Ist `out-of-order: false` in Production? | §25 |
| Tests | Laufen Integration-Tests gegen Testcontainers + Flyway? | §26 |
| CI Validation | Läuft `flyway validate` bei jedem PR? | §27.1 |
| Pre-Production-Test | Wurde Migration gegen Staging mit Production-Snapshot getestet? | §27.3 |
| Datenbankspezifische Patterns | Sind datenbankspezifische Features kommentiert? | §28 |

---

## 32. Automatisierbare Prüfungen

### 32.1 Spring Properties Check

```java
@Configuration
public class FlywayPropertiesValidator {

    @PostConstruct
    public void validateProperties(Environment env) {
        var ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto");

        if (ddlAuto == null) {
            throw new IllegalStateException("spring.jpa.hibernate.ddl-auto must be set");
        }

        if (List.of("create", "create-drop", "update").contains(ddlAuto)) {
            throw new IllegalStateException(
                "spring.jpa.hibernate.ddl-auto=" + ddlAuto + " is not allowed in production. " +
                "Use 'validate' or 'none'."
            );
        }

        var baselineOnMigrate = env.getProperty(
            "spring.flyway.baseline-on-migrate", Boolean.class, false);

        if (Boolean.TRUE.equals(baselineOnMigrate)) {
            // Warn statt fail — kann legitim sein bei einmaligem Setup
            log.warn("spring.flyway.baseline-on-migrate=true is set. " +
                     "This should only be used for one-time setup, not as default.");
        }
    }
}
```

### 32.2 Semgrep-Regeln

```yaml
rules:
  - id: flyway-no-ddl-auto-create
    message: "ddl-auto=create/create-drop/update ist in Produktion verboten"
    severity: ERROR
    languages: [yaml]
    pattern-regex: 'ddl-auto:\s*(create|create-drop|update)'

  - id: flyway-no-plaintext-password
    message: "DB-Password in application.yml — Secret Management verwenden"
    severity: ERROR
    languages: [yaml]
    pattern-regex: 'password:\s*[^$\s].*'

  - id: flyway-no-baseline-on-migrate-true
    message: "baseline-on-migrate: true als Default ist Anti-Pattern"
    severity: WARNING
    languages: [yaml]
    pattern-regex: 'baseline-on-migrate:\s*true'

  - id: flyway-migration-with-large-update
    message: "UPDATE ohne WHERE oder LIMIT in Migration — Application Layer Job erwägen"
    severity: WARNING
    languages: [sql]
    patterns:
      - pattern: UPDATE $TABLE SET $COL = $VAL
      - pattern-not: UPDATE $TABLE SET $COL = $VAL WHERE $CONDITION

  - id: flyway-create-index-without-concurrently
    message: "CREATE INDEX ohne CONCURRENTLY blockiert Writes"
    severity: WARNING
    languages: [sql]
    patterns:
      - pattern: CREATE INDEX $NAME ON $TABLE
      - pattern-not: CREATE INDEX CONCURRENTLY $NAME ON $TABLE
      - pattern-not-inside: CREATE UNIQUE INDEX $NAME ON $TABLE
```

### 32.3 Migration-File-Linter (Custom)

```java
@Test
void allMigrationsFollowNamingConvention() throws IOException {
    var migrationDir = Path.of("src/main/resources/db/migration");

    Files.walk(migrationDir)
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".sql"))
        .forEach(p -> {
            var fileName = p.getFileName().toString();
            assertThat(fileName)
                .matches("^(V\\d+|R__\\d+_).*__.*\\.sql$")
                .as("Migration %s does not follow naming convention", fileName);
        });
}
```

---

## 33. Ausnahmen

Ausnahmen sind zulässig, wenn sie bewusst und nachvollziehbar sind.

Mögliche Ausnahmen:

1. **Sehr kleine interne Tools** mit minimaler DB ohne Production-Bedeutung.
2. **Legacy-Code während kontrollierter Migration zu Flyway** (max. 3 Monate).
3. **Einmalige Baseline-Operation** mit `baseline-on-migrate: true` über CLI.
4. **Performance-kritische Hot Paths**, in denen Lock-Timeouts nicht praktikabel sind.
5. **Bewusst nicht-tenant-aware** Migrations für globale Konfigurationstabellen.

Ausnahmen MÜSSEN dokumentiert werden:

- **Was weicht ab?**
- **Warum ist die Abweichung notwendig?**
- **Welche Risiken entstehen?**
- **Wie wird das Risiko begrenzt?** (Compensating Controls)
- **Wann wird die Ausnahme erneut geprüft?**
- **Wer hat freigegeben?**

Nicht zulässig als Begründung:

1. „Das war schneller."
2. „Wir testen das im nächsten Release."
3. „`ddl-auto=update` ist doch praktisch."
4. „Flyway-Migrationen sind übertrieben."
5. „Wir haben kein Staging."
6. „Lock-Timeouts sind paranoid."

---

## 34. Definition of Done

Eine Datenbankmigration erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. `spring.jpa.hibernate.ddl-auto` ist auf `validate` oder `none` gesetzt.
2. Migrationen folgen dem Namensschema `V<n>__<...>.sql` / `R__<n>_<...>.sql`.
3. Eingecheckte Migrationen werden nicht nachträglich geändert.
4. Repeatable Migrations sind idempotent.
5. Schema- und Daten-Migrationen sind getrennt.
6. `baseline-on-migrate` ist `false` als Default.
7. Alle Constraints haben explizite Namen.
8. Zeitstempel verwenden `TIMESTAMPTZ`, nicht `TIMESTAMP`.
9. Schema-Änderungen sind rückwärtskompatibel zur alten App-Version.
10. PostgreSQL 11+ Verhalten für `NOT NULL DEFAULT <const>` ist berücksichtigt.
11. CHECK/FK auf großen Tabellen verwenden `NOT VALID` + `VALIDATE CONSTRAINT`.
12. Lock-Timeout ist via `init-sql` gesetzt (z. B. 5 Sekunden).
13. `CREATE INDEX` verwendet `CONCURRENTLY` mit `transactional: false`.
14. Backfills auf Tabellen > 100k Zeilen laufen über Application Layer Background Jobs.
15. Java-Migrationen mit komplexer Logik verwenden `BaseJavaMigration` mit `canExecuteInTransaction() = false`.
16. Multi-Tenant-Setups sind explizit modelliert (Schema-pro-Tenant oder Tenant-ID-Spalte).
17. Separate DB-User für Flyway-Migration (DDL) und App-Workload (DML) sind konfiguriert.
18. DB-Credentials kommen aus Secret Management, nicht aus `application.yml`.
19. In Kubernetes läuft Flyway über Init-Container oder Job, nicht in App-Pods.
20. App-Pods haben `spring.flyway.enabled=false` in K8s-Setup.
21. Forward-Fix-Strategie ist als primäre Rollback-Methode dokumentiert.
22. `out-of-order: false` in Production, optional `true` in Development.
23. Integration-Tests laufen gegen Testcontainers + Flyway.
24. CI/CD führt `flyway validate` und `flyway info` bei jedem Pull Request aus.
25. Pre-Production-Tests gegen Production-Snapshot wurden für Major-Migrations durchgeführt.
26. Datenbankspezifische Features sind kommentiert.
27. Cross-References zu QG-JAVA-006 v2 (Tenant-Context), QG-JAVA-041 v2 (Outbox-Pattern) sind beachtet.

---

## 35. Quellen und weiterführende Literatur

### Flyway

* Flyway Documentation: <https://documentation.red-gate.com/fd/>
* Flyway Maven Plugin: <https://documentation.red-gate.com/fd/maven-plugin-184127473.html>
* Flyway Java API: <https://documentation.red-gate.com/fd/java-api-184127574.html>
* Flyway Callbacks: <https://documentation.red-gate.com/fd/callbacks-184127472.html>

### Spring Boot

* Spring Boot — Database Migration: <https://docs.spring.io/spring-boot/reference/howto/data-initialization.html#howto.data-initialization.migration-tool.flyway>
* Spring Boot — Flyway Properties: <https://docs.spring.io/spring-boot/appendix/application-properties/index.html#appendix.application-properties.data-migration>

### PostgreSQL

* PostgreSQL ALTER TABLE: <https://www.postgresql.org/docs/16/sql-altertable.html>
* PostgreSQL CREATE INDEX: <https://www.postgresql.org/docs/16/sql-createindex.html>
* PostgreSQL — Building Indexes Concurrently: <https://www.postgresql.org/docs/16/sql-createindex.html#SQL-CREATEINDEX-CONCURRENTLY>
* PostgreSQL — Adding NOT NULL Constraints: <https://www.postgresql.org/docs/16/sql-altertable.html#SQL-ALTERTABLE-NOTES>
* PostgreSQL 11 Release Notes (NOT NULL DEFAULT): <https://www.postgresql.org/docs/11/release-11.html>

### Production Patterns

* Strong Migrations (Ruby): <https://github.com/ankane/strong_migrations> (Pattern-Sammlung, sprachunabhängig)
* GitLab — Database Migration Style Guide: <https://docs.gitlab.com/ee/development/migration_style_guide.html>
* Braintree — PostgreSQL at Scale: <https://medium.com/braintree-product-technology/postgresql-at-scale-database-schema-changes-without-downtime-20d3749ed680>

### Multi-Tenant

* Multi-Tenancy with Spring Boot: <https://www.baeldung.com/multitenancy-with-spring-data-jpa>

### Kubernetes

* Kubernetes Init Containers: <https://kubernetes.io/docs/concepts/workloads/pods/init-containers/>
* Helm Hooks: <https://helm.sh/docs/topics/charts_hooks/>

### Testing

* Testcontainers — PostgreSQL Module: <https://java.testcontainers.org/modules/databases/postgres/>

---
 