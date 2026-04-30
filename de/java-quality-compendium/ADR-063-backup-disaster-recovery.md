# ADR-063 — Backup, Restore & Disaster Recovery

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2024-01-01                        |
| Kategorie  | Operations / Business Continuity  |

---

## Kontext & Problem

"Kein Backup, kein Mitleid." Ein System ohne getestete Backup-Strategie hat keine Backup-Strategie — nur Hoffnung. Die Frage ist nicht ob Datenverlust passiert, sondern wann. Dieses ADR definiert RPO/RTO-Ziele, Backup-Strategie und — entscheidend — regelmäßige Restore-Tests.

---

## RPO und RTO: Ziele definieren

```
RPO (Recovery Point Objective): Wie viel Datenverlust ist akzeptabel?
  → RPO = 1 Stunde: Backups stündlich, max 1 Stunde Daten verloren

RTO (Recovery Time Objective): Wie lange darf die Wiederherstellung dauern?
  → RTO = 4 Stunden: System muss innerhalb 4h nach Incident laufen

Beispiel-Ziele nach Systemkritikalität:
  Order DB (Transaktionen): RPO = 1 Stunde, RTO = 2 Stunden
  Analytics DB (Reports):   RPO = 24 Stunden, RTO = 8 Stunden
  Audit Log:                RPO = 0 (Point-in-Time Recovery), RTO = 4 Stunden
```

---

## Datenbankbackup-Strategie (PostgreSQL)

```bash
# Kontinuierliches WAL-Archiving (Point-in-Time Recovery)
# Keine Lücken — jede Transaktion wiederherstellbar

# postgresql.conf
archive_mode = on
archive_command = 'gzip < %p > /backup/wal/%f.gz'  # WAL in S3/GCS
wal_level = replica

# pgBackRest: Enterprise-Backup für PostgreSQL
[global]
repo1-type=s3
repo1-path=/pgbackrest/order-service
repo1-s3-bucket=my-backups
repo1-s3-region=eu-central-1
repo1-retention-full=4  # 4 Full Backups behalten
repo1-retention-diff=14 # 14 Differential Backups

[order-service]
pg1-path=/var/lib/postgresql/16/main
```

```yaml
# Kubernetes CronJob: tägliches Full Backup
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgresql-backup
spec:
  schedule: "0 2 * * *"   # 2 Uhr nachts
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: pgbackrest:latest
              command:
                - pgbackrest
                - --stanza=order-service
                - backup
                - --type=full
              env:
                - name: PGPASSWORD
                  valueFrom:
                    secretKeyRef:
                      name: postgres-secrets
                      key: password
```

---

## Backup-Typen und Zeitplan

```
Strategie: 3-2-1 Regel
  3 Kopien der Daten
  2 verschiedene Medien/Technologien
  1 Kopie offsite (andere Cloud-Region oder Provider)

Zeitplan:
  Kontinuierlich: WAL-Archiving (PostgreSQL Point-in-Time Recovery)
  Stündlich:      Inkrementelles Backup (nur Änderungen seit letztem Backup)
  Täglich:        Differentielles Backup (Änderungen seit letztem Full Backup)
  Wöchentlich:    Full Backup (vollständige Datenbank)
  Aufbewahrung:
    - 7 Tage:  Stündliche Backups
    - 30 Tage: Tägliche Backups
    - 1 Jahr:  Wöchentliche Backups
    - Compliance-Backup: 10 Jahre (§ 147 AO für Geschäftsunterlagen)
```

---

## Application-Level Backup: Event Store

```java
// Für Event-Sourced Systeme (→ ADR-055): Events sind der Backup
// Kafka als persistenter Event-Store: retention.ms = -1 (unbegrenzt)

// application-Backup: regelmäßiger Export aller Events
@Scheduled(cron = "0 3 * * *")  // 3 Uhr nachts
public void exportEventSnapshot() {
    var events = eventStore.loadAllSince(lastExportTimestamp());
    var exportFile = storageService.writeGzippedJson(events,
        "events-" + LocalDate.now() + ".json.gz");
    backupStorage.upload(exportFile, "s3://backups/events/");
    log.info("Exported {} events to backup storage", events.size());
}
```

---

## KRITISCH: Regelmäßige Restore-Tests

```bash
# Backup-Test: monatlich, automatisiert
# Ein Backup das nie getestet wurde, ist kein Backup

# restore-test.sh
#!/bin/bash
set -euo pipefail

echo "=== Monthly Restore Test $(date) ==="

# 1. Frisches Test-Environment starten
kubectl create namespace restore-test-$(date +%Y%m)

# 2. Backup vom Vortag wiederherstellen
pgbackrest --stanza=order-service restore \
  --target="$(date -d yesterday '+%Y-%m-%d %H:%M:%S')" \
  --target-action=promote

# 3. Integrität prüfen
psql -c "SELECT COUNT(*) FROM orders WHERE created_at > NOW() - INTERVAL '7 days'"
psql -c "VACUUM ANALYZE"  # Prüft DB-Integrität implizit

# 4. Applikation gegen Restore starten
kubectl run restore-test --image=ghcr.io/example/order-service:latest \
  --env="SPRING_DATASOURCE_URL=jdbc:postgresql://restored-db/orderdb"

# 5. Smoke-Test gegen wiederhergestellte DB
./scripts/smoke-test.sh http://restore-test.internal

# 6. RTO messen
echo "Restore completed in $SECONDS seconds"
echo "RTO target: 7200 seconds (2 hours)"
if [ $SECONDS -gt 7200 ]; then
  echo "WARNING: RTO exceeded!"
  # Slack-Alert
fi

# 7. Test-Environment aufräumen
kubectl delete namespace restore-test-$(date +%Y%m)
```

```yaml
# CronJob: monatlicher Restore-Test
apiVersion: batch/v1
kind: CronJob
metadata:
  name: monthly-restore-test
spec:
  schedule: "0 4 1 * *"   # 1. jeden Monats um 4 Uhr
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: restore-test
              image: restore-tester:latest
              command: ["/scripts/restore-test.sh"]
```

---

## Disaster Recovery Playbook

```markdown
## DR-Playbook: Vollständiger Datenverlust Order-DB

### Phase 1: Incident-Erkennung (0-15 Min)
1. Alert von Prometheus: `PostgreSQLDown` oder `OrderServiceErrors`
2. On-Call benachrichtigen (PagerDuty → ADR-054)
3. Incident in PagerDuty anlegen
4. Communication: Statuspage aktualisieren

### Phase 2: Triage (15-30 Min)
5. Art des Problems identifizieren:
   - Kompletter Datenverlust → DR-Plan
   - Korrupte Daten → Point-in-Time Recovery
   - Hardware-Ausfall → Failover zu Replica
6. RPO bestimmen: welcher Zeitpunkt soll wiederhergestellt werden?

### Phase 3: Wiederherstellung (30 Min - 2 Std)
7. Backup identifizieren: `pgbackrest info --stanza=order-service`
8. Restore-DB starten (separater Namespace)
9. Restore ausführen: `pgbackrest restore --target="YYYY-MM-DD HH:MM:SS"`
10. Integrität prüfen: Checksum-Validierung, Row-Count-Vergleich
11. Traffic auf wiederhergestellte DB umschalten

### Phase 4: Verifikation (2-3 Std)
12. Smoke-Tests auf produktiver Umgebung
13. Business-Validierung: Bestellungen der letzten Stunden prüfen
14. Monitoring: Error-Rate < 0.5% für 30 Minuten

### Phase 5: Post-Mortem (nach Incident)
15. Blameless Post-Mortem innerhalb 48h
16. Root Cause Analysis
17. Maßnahmen zur Verhinderung

## Eskalationskette
On-Call → Team-Lead Backend → CTO → CEO (ab P0 > 2 Stunden)
```

---

## Backup-Monitoring

```yaml
# Prometheus Alert: kein Backup seit 26 Stunden
- alert: BackupMissing
  expr: time() - backup_last_success_timestamp_seconds > 93600  # 26h
  for: 0m
  labels:
    severity: critical
  annotations:
    summary: "No successful backup in 26 hours"
    runbook: "https://wiki.example.com/runbooks/backup-missing"
```

---

## Konsequenzen

**Positiv:** Definierte RPO/RTO-Ziele machen Backup-Anforderungen messbar. Monatliche Restore-Tests stellen sicher dass Backups funktionieren. 3-2-1-Regel schützt vor einem einzelnen Ausfallpunkt.

**Negativ:** Backup-Storage kostet Geld (kalkulieren: Backup-Größe × Retention × Preis/GB). Restore-Tests brauchen separate Infrastruktur und Zeit. WAL-Archiving erhöht Disk-I/O.

---

## 💡 Guru-Tipps

- **"Trust but verify"**: Backup ist kein Backup ohne erfolgreichen Restore-Test.
- **Backup-Encryption**: Backups mit AES-256 verschlüsseln — sie enthalten alle Kundendaten.
- **Cross-Region Backup**: mindestens eine Kopie in anderer Cloud-Region (Availability Zone reicht nicht).
- **Compliance**: DSGVO erlaubt Löschung personenbezogener Daten — auch aus Backups. Retention-Policy mit Legal abstimmen.

---

## Verwandte ADRs

- [ADR-034](ADR-034-db-migrations-flyway.md) — Flyway-Migrationen vor Backup-Restore prüfen.
- [ADR-035](ADR-035-gdpr-privacy-by-design.md) — DSGVO: Backup-Retention vs. Löschpflicht.
- [ADR-054](ADR-054-slo-sla-alerting.md) — RTO als SLO definieren und monitoren.
