# ADR-098 — Zero-Downtime DB-Migrationen: Expand/Contract Pattern

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Datenbank · Migrations-Strategie · Zero-Downtime             |
| Betroffene Teams  | Alle Teams die DB-Schemas ändern                              |
| Abhängigkeiten    | ADR-034 (Flyway), ADR-059 (Blue/Green), ADR-038 (Kubernetes)  |

---

## 1. Kontext & Treiber

### 1.1 Das Problem mit normalen DB-Migrationen

```
SZENARIO: Spalte umbenennen (customer_id → user_id)

NAIVE MIGRATION (mit Downtime):
  1. Service stoppen
  2. ALTER TABLE orders RENAME COLUMN customer_id TO user_id
  3. Neuen Code deployen der user_id schreibt/liest
  4. Service starten

PROBLEM: Schritt 1 bis 4 = Downtime.
  → Bei einfacher Umbenennung: 5-10 Minuten
  → Bei großen Tabellen (100M Rows): ALTER TABLE dauert Stunden!
  → SLA 99.9% erlaubt 43 Minuten/Monat — 1 Migration = 25% des Monatsbudgets

SZENARIO MIT KUBERNETES ROLLING DEPLOYMENT:
  Alt: Code liest customer_id
  Neu: Code liest user_id
  
  Während Rolling Deployment:
  Pod 1 (alt): SELECT customer_id FROM orders → ✅
  Pod 2 (neu): SELECT user_id FROM orders → ❌ Column not found!
  
  Es gibt IMMER eine Phase wo alt und neu GLEICHZEITIG laufen.
  → Migrationsstrategie muss das berücksichtigen.
```

### 1.2 Zero-Downtime erfordert mehrere Deployments

```
REALISIERUNG: Eine Breaking-Schema-Änderung = DREI Deployments

Deployment 1: EXPAND
  DB wird erweitert (backwards-compatible)
  Alter Code: funktioniert noch
  Neuer Code: kann beginnen zu schreiben

Deployment 2: MIGRATE
  Daten umziehen/transformieren
  Beide Spalten gefüllt

Deployment 3: CONTRACT
  Altes Schema entfernen
  Nur neuer Code läuft noch

KOSTEN: 3× Deploy-Aufwand statt 1×
NUTZEN: 0 Minuten Downtime statt 10-60+ Minuten
```

---

## 2. Entscheidung

Jede DB-Schemaänderung die Breaking für den laufenden Code wäre, wird nach dem Expand/Contract-Pattern in mindestens drei rückwärtskompatible Deployments aufgeteilt. Das Flyway-Migrations-Verzeichnis ist nach Phasen strukturiert. Keine Migration darf `ALTER TABLE ... DROP COLUMN` oder `RENAME COLUMN` enthalten ohne vorheriges Expand-Deployment.

---

## 3. Expand/Contract in der Praxis

### 3.1 Beispiel 1: Spalte umbenennen (customer_id → user_id)

```
AUSGANGSLAGE:
  orders Tabelle: id, customer_id, status, total
  Code liest: SELECT customer_id FROM orders

PHASE 1 — EXPAND: Neue Spalte hinzufügen (nullable!)
```

```sql
-- V15__expand_add_user_id.sql
ALTER TABLE orders ADD COLUMN user_id BIGINT;

-- Noch KEIN NOT NULL! Alter Code schreibt customer_id, nicht user_id.
-- user_id muss NULL sein dürfen in dieser Phase.

-- Trigger: automatisch befüllen wenn alter Code schreibt
CREATE OR REPLACE FUNCTION sync_user_id()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.user_id IS NULL AND NEW.customer_id IS NOT NULL THEN
        NEW.user_id := NEW.customer_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER orders_sync_user_id
    BEFORE INSERT OR UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION sync_user_id();
```

```java
// Code nach EXPAND (V1): liest beide Spalten, schreibt beide
@Entity
public class OrderEntity {
    private Long customerId;  // Alt: noch vorhanden
    private Long userId;      // Neu: jetzt auch vorhanden

    // Getter: nimmt was verfügbar ist (Migration-Phase)
    public Long getEffectiveUserId() {
        return userId != null ? userId : customerId;
    }
}
```

```
PHASE 2 — MIGRATE: Bestehende Daten befüllen
```

```sql
-- V16__migrate_fill_user_id.sql
-- Alle bestehenden Rows haben noch NULL in user_id (vor dem Trigger)
UPDATE orders
SET user_id = customer_id
WHERE user_id IS NULL AND customer_id IS NOT NULL;

-- Jetzt NOT NULL setzen (alle Rows haben user_id)
ALTER TABLE orders ALTER COLUMN user_id SET NOT NULL;
```

```java
// Code nach MIGRATE (V2): schreibt nur noch user_id, liest nur noch user_id
@Entity
public class OrderEntity {
    private Long customerId;  // Noch vorhanden für Rückwärtskompatibilität
    @Column(name = "user_id", nullable = false)
    private Long userId;      // Primäre Spalte
}
```

```
PHASE 3 — CONTRACT: Alte Spalte entfernen
```

```sql
-- V17__contract_remove_customer_id.sql
-- Nur wenn kein alter Code mehr läuft!
DROP TRIGGER IF EXISTS orders_sync_user_id ON orders;
DROP FUNCTION IF EXISTS sync_user_id();
ALTER TABLE orders DROP COLUMN customer_id;
```

```java
// Code nach CONTRACT (V3): customer_id ist weg
@Entity
public class OrderEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;  // Nur noch userId
}
```

---

### 3.2 Beispiel 2: Spalte hinzufügen (Pflichtfeld)

```
FALSCH — direktes Hinzufügen eines Pflichtfelds:
```

```sql
-- ❌ BRICHT alten Code!
ALTER TABLE orders ADD COLUMN priority INT NOT NULL;
-- Alter Code macht INSERT ohne priority → Constraint-Violation → 500er!
```

```sql
-- ✅ EXPAND: Nullable mit Default
-- V20__expand_add_priority.sql
ALTER TABLE orders
    ADD COLUMN priority INT
    DEFAULT 0;           -- Default: alter Code muss priority nicht setzen

-- NOCH KEIN NOT NULL — alter Code kann priority weglassen
```

```sql
-- ✅ MIGRATE: Default-Werte befüllen, dann NOT NULL
-- V21__migrate_priority_not_null.sql
UPDATE orders SET priority = 0 WHERE priority IS NULL;
ALTER TABLE orders ALTER COLUMN priority SET NOT NULL;
```

---

### 3.3 Beispiel 3: Tabelle aufteilen (größte Herausforderung)

```
AUSGANGSLAGE: orders Tabelle hat Zahlungsdaten
  orders: id, status, total, stripe_payment_id, payment_status

ZIEL: Payments in eigene Tabelle auslagern

PHASE 1 — EXPAND: Neue Tabelle erstellen
```

```sql
-- V25__expand_create_payments_table.sql
CREATE TABLE payments (
    id                 BIGSERIAL PRIMARY KEY,
    order_id           BIGINT       NOT NULL REFERENCES orders(id),
    stripe_payment_id  VARCHAR(100),
    payment_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order_id ON payments(order_id);

-- Alte Spalten BLEIBEN in orders — alter Code funktioniert noch!
```

```java
// EXPAND: Dual-Write — beide Tabellen befüllen
@Transactional
public void processPayment(OrderId orderId, String stripeId, PaymentStatus status) {
    // Alter Code: schreibt in orders (für Rückwärtskompatibilität)
    orderRepository.updatePaymentInfo(orderId, stripeId, status);

    // Neuer Code: schreibt AUCH in payments-Tabelle
    paymentRepository.save(new PaymentEntity(orderId, stripeId, status));
}
```

```sql
-- V26__migrate_backfill_payments.sql
-- Historische Daten migrieren
INSERT INTO payments (order_id, stripe_payment_id, payment_status, created_at)
SELECT id, stripe_payment_id, payment_status, created_at
FROM orders
WHERE stripe_payment_id IS NOT NULL
ON CONFLICT (order_id) DO NOTHING;  -- Idempotent!
```

```java
// EXPAND+MIGRATE: Read von payments, Write in beide
public PaymentStatus getPaymentStatus(OrderId orderId) {
    // Zuerst neue Tabelle versuchen
    return paymentRepository.findByOrderId(orderId)
        .map(PaymentEntity::status)
        .orElseGet(() ->
            // Fallback: alte Spalte (für Rows vor Migration)
            orderRepository.getPaymentStatus(orderId));
}
```

```sql
-- V27__contract_remove_payment_columns.sql
-- Erst wenn KEIN alter Code mehr liest und ALLE Rows in payments migriert
ALTER TABLE orders
    DROP COLUMN stripe_payment_id,
    DROP COLUMN payment_status;
```

---

## 4. Flyway-Migrations-Struktur für Expand/Contract

```
src/main/resources/db/migration/
├── V15__expand_add_user_id.sql          ← EXPAND: safe, rückwärtskompatibel
├── V16__migrate_fill_user_id.sql        ← MIGRATE: Daten transformieren
├── V17__contract_remove_customer_id.sql ← CONTRACT: nur nach vollst. Rollout
│
├── V20__expand_add_priority.sql
├── V21__migrate_priority_not_null.sql
├── V22__contract_priority_cleanup.sql

NAMING CONVENTION (Pflicht):
  expand_   → rückwärtskompatible Erweiterung (sicher deploybar)
  migrate_  → Daten-Transformation (idempotent!)
  contract_ → Abbau alter Strukturen (nur wenn kein alter Code)
  feature_  → Neue Features (keine Breaking Changes)
  fix_      → Bugfix-Migration
```

### 4.1 Große Tabellen: Online Schema Change

```sql
-- Problem: ALTER TABLE auf 500M-Row-Tabelle = Table-Lock für Stunden
-- Lösung: gh-ost (GitHub's Online Schema Change) oder pt-online-schema-change

-- Mit gh-ost (MySQL) oder pg_repack (PostgreSQL):
-- 1. Neue Shadow-Tabelle erstellen
-- 2. Daten schrittweise kopieren (ohne Lock)
-- 3. Binlog/WAL replizieren (neue Writes während Kopie)
-- 4. Tabellen atomar umbenennen (minimaler Lock: Millisekunden)

-- PostgreSQL: CREATE INDEX CONCURRENTLY (→ ADR-073)
CREATE INDEX CONCURRENTLY idx_orders_user_id ON orders(user_id);
-- CONCURRENTLY: kein Table-Lock, Build im Hintergrund (~Minuten statt Sekunden)

-- Für Tabellenstruktur-Änderungen auf großen Tabellen:
-- Verwende pg_repack: https://github.com/reorg/pg_repack
```

---

## 5. CI/CD-Integration: Migrations-Phasen im Deployment

```yaml
# Kubernetes: Flyway als Init Container (→ ADR-038)
# Wichtig: Migrations laufen VOR dem neuen Code-Start

spec:
  initContainers:
    - name: db-migrate
      image: ghcr.io/example/order-service:${NEW_VERSION}
      command:
        - java
        - -jar
        - /app/app.jar
        - --spring.flyway.enabled=true
        - --spring.batch.job.enabled=false  # Nur Migration, kein Server
      env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://db:5432/orders"
      # Init-Container läuft bis Migration abgeschlossen
      # Haupt-Container startet erst danach

  containers:
    - name: order-service
      image: ghcr.io/example/order-service:${NEW_VERSION}

# Rolling Update Reihenfolge:
# 1. Init Container (neuer Code): migrate DB (EXPAND)
# 2. Alter Code läuft: funktioniert mit erweitertem Schema ✅
# 3. Neuer Code startet: läuft mit erweitertem Schema ✅
# 4. Alter Code gestoppt
# 5. Nächstes Deployment: CONTRACT (alter Code nicht mehr aktiv)
```

---

## 6. Checkliste vor jeder DB-Migration

```markdown
## Migration-Review-Checkliste

**VOR dem Schreiben der Migration:**
- [ ] Ist das eine Breaking Change? (Spalte entfernen/umbenennen, Typ ändern, NOT NULL hinzufügen)
  - JA → Expand/Contract-Pattern verwenden (3 Deployments)
  - NEIN → Direkte Migration OK

**Bei EXPAND-Migrationen:**
- [ ] Neue Spalten sind nullable (alter Code kann sie weglassen)
- [ ] Neue Spalten haben sinnvolle Defaults (alter Code setzt sie nicht)
- [ ] Kein ALTER COLUMN auf bestehende Spalten
- [ ] Kein DROP COLUMN, DROP TABLE, RENAME

**Bei MIGRATE-Migrationen:**
- [ ] Migration ist idempotent (ON CONFLICT DO NOTHING, WHERE NULL)
- [ ] Batching bei großen Tabellen (UPDATE ... WHERE id BETWEEN X AND Y)
- [ ] Kein Table-Lock bei > 1M Rows (CONCURRENTLY, pg_repack)

**Bei CONTRACT-Migrationen:**
- [ ] Alter Code ist vollständig aus Produktion entfernt
- [ ] Kein Rollback auf alten Code geplant
- [ ] Shadow-Tests: neue Code ohne alte Spalten getestet

**IMMER:**
- [ ] Migration ist in lokaler Testumgebung ausgeführt
- [ ] Rollback-Plan dokumentiert
- [ ] Ausführungszeit geschätzt (EXPLAIN ANALYZE für UPDATE)
```

---

## 7. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| Maintenance-Window | Einfach, ein Schritt | SLA-Verletzung, Kundenerlebnis | 99.9% SLA erlaubt nur 43 Min/Monat |
| Feature-Flags für Migration | Granulare Kontrolle | Komplexer Code, doppelte Logik lange | Expand/Contract einfacher und sicherer |
| Event-Sourcing (kein Schema) | Kein Schema-Problem | Komplexitätssprung für simplen Use-Case | Overengineering wenn nur Schema-Change |
| Blue/Green mit DB-Copy | Einfach für Code | Doppelter DB-Speicher, Sync-Problem | Impraktisch bei großen Datenmengen |

---

## 8. Trade-off-Matrix

| Qualitätsziel | Expand/Contract | Maintenance Window | Direkt mit DT |
|---|---|---|---|
| Availability | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| Einfachheit | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Rollback-Fähigkeit | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| Deployment-Aufwand | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| SLA-Einhaltung | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |

---

## 9. Akzeptanzkriterien

- [ ] Code-Review-Checkliste enthält: "Ist das eine Breaking Change?"
- [ ] Jede Breaking-Change-Migration ist als EXPAND + MIGRATE + CONTRACT aufgeteilt
- [ ] Flyway-Migrations-Naming-Convention (`expand_`, `migrate_`, `contract_`) wird in CI geprüft
- [ ] DB-Init-Container in allen Kubernetes-Deployments konfiguriert
- [ ] Für Tabellen > 1M Rows: `CREATE INDEX CONCURRENTLY` statt normales CREATE INDEX
- [ ] Runbook dokumentiert: wie CONTRACT-Phase sicher durchgeführt wird

---

## Quellen & Referenzen

- **Martin Fowler, "Evolutionary Database Design" (2016)** — Expand/Contract als Grundlage von Zero-Downtime-Migrationen; co-authored mit Pramod Sadalage.
- **Pramod Sadalage & Martin Fowler, "Refactoring Databases" (2006)** — Vollständiger Katalog von DB-Refactoring-Patterns.
- **Sam Newman, "Building Microservices" (2022), Kap. 4** — Datenbankmigrationen in Microservice-Umgebungen.
- **Flyway Documentation** — Versioning und Callback-Mechanismen.
- **GitHub, "gh-ost: GitHub's online schema migration tool"** — Online Schema Change ohne Locks.

---

## Verwandte ADRs

- [ADR-034](ADR-034-db-migrations-flyway.md) — Flyway als Migrations-Tool
- [ADR-059](ADR-059-blue-green-canary.md) — Blue/Green Deployment erfordert rückwärtskompatible Schemas
- [ADR-073](ADR-073-database-indexing.md) — CONCURRENTLY für Index-Erstellung
- [ADR-038](ADR-038-kubernetes.md) — Init-Container für Migrations
