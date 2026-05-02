# ADR-078 — Datenstrategie: Ownership, Qualität & Compliance

| Feld              | Wert                                              |
|-------------------|---------------------------------------------------|
| Status            | ✅ Akzeptiert                                     |
| Entscheider       | CTO / CDO (Chief Data Officer)                    |
| Datum             | 2024-01-01                                        |
| Review-Datum      | 2025-01-01                                        |
| Kategorie         | Datenstrategie / Governance                       |
| Betroffene Teams  | Alle Engineering-Teams, Legal, Compliance         |

---

## Kontext & Treiber

Daten sind das wertvollste Asset eines Software-Unternehmens — und gleichzeitig die größte regulatorische Exposition. Ohne klare Ownership entstehen:
- Unklare Verantwortlichkeit bei Datenschutzverletzungen (DSGVO: bis 4% des weltweiten Jahresumsatzes)
- Daten-Silos: dieselben Daten werden in 5 verschiedenen Modellen an 5 verschiedenen Orten gespeichert
- Analytik-Chaos: Reporting-Teams fragen Teams um Datenzugriff und blockieren den Development-Flow
- Migrationsrisiken: niemand weiß welche Systeme von welchen Datenfeldern abhängen

**Regulatory Context (Deutschland/EU):**
- DSGVO Art. 5, 17, 25 — Datensparsamkeit, Löschpflichten, Privacy by Design
- § 147 AO, § 257 HGB — 10-Jahres-Aufbewahrungspflicht für Geschäftsunterlagen
- BSI IT-Grundschutz — Vertraulichkeit, Integrität, Verfügbarkeit

---

## Entscheidung

Jedes Datensystem hat einen **Data Owner** (Fachbereich) und einen **Data Steward** (Engineering-Team). Daten werden nach dem **Datenmesh-Prinzip** als Produkt behandelt: jedes Domänen-Team ist verantwortlich für die Qualität und Zugänglichkeit seiner eigenen Daten. Keine zentrales Data Warehouse als Engpass.

**Daten-Ownership-Prinzipien:**
1. **Domain-Ownership**: Daten gehören der Domäne die sie erzeugt (Orders-Team besitzt Order-Daten)
2. **Data as a Product**: Daten werden mit SLAs, Dokumentation und Quality-Metriken bereitgestellt
3. **Self-serve Infrastructure**: Analytics-Teams können Daten selbst abfragen ohne Engineering-Ticket
4. **Federated Governance**: Standards (Schema, Security, Compliance) zentral, Umsetzung dezentral

---

## Begründung

**Referenzen:**
- Zhamak Dehghani, "Data Mesh" (2022) — Domain-oriented data ownership mit self-serve analytics. Empirische Basis: zentrales Data Warehouse als Engpass bei > 3 Teams.
- Martin Kleppmann, "Designing Data-Intensive Applications" (2017), Kap. 1 — Reliability, Scalability, Maintainability als Qualitätsziele für Datensysteme.
- DSGVO Art. 25 "Privacy by Design" — Datenschutz muss in die Systemarchitektur eingebaut sein, nicht nachträglich.
- DSGVO Art. 17 "Recht auf Vergessenwerden" — technische Löschfähigkeit muss von Anfang an designt sein.
- Thoughtworks, "Data Mesh in Practice" (2022) — Case Studies zeigen 3–5× schnellere Feature-Lieferung nach Data-Mesh-Migration.

---

## Datenkategorien & Anforderungen

| Kategorie          | Beispiele                        | Retention         | Verschlüsselung | Backup-Klasse | DSGVO           |
|---------------------|----------------------------------|-------------------|-----------------|---------------|-----------------|
| Transaktionsdaten   | Bestellungen, Zahlungen          | 10 Jahre (§ 147 AO)| at-rest + in-transit | A (RPO 1h) | eingeschränkt   |
| Personenbezogene Daten | Name, Email, Adresse          | Löschpflicht (Art. 17) | at-rest + in-transit | A (RPO 1h) | streng          |
| Audit-Logs          | Zugriffe, Änderungen             | 7 Jahre           | at-rest         | B (RPO 24h)   | eingeschränkt   |
| Analytik-Events     | Click-Streams, Features-Usage    | 90 Tage           | at-rest         | C (RPO 7d)    | pseudonymisiert |
| Session-Daten       | Auth-Token, User-Präferenzen     | Session-TTL       | in-transit      | nicht relevant | eingeschränkt   |
| Betriebsdaten       | Logs, Metriken, Traces           | 30–90 Tage        | at-rest         | D (keine)     | nein            |

---

## Alternativen & Warum sie abgelehnt wurden

| Alternative              | Stärken                           | Schwächen                                    | Ablehnungsgrund                                    |
|--------------------------|----------------------------------|----------------------------------------------|---------------------------------------------------|
| Zentrales Data Warehouse | Einheitliches Datenmodell        | Engpass-Team, langsame Iteration              | Dehghani: "Central data teams can't keep up"      |
| Kein explizites Data Ownership | Einfacher initialer Aufwand | Verantwortungslosigkeit, DSGVO-Risiko        | Regulatorische Pflicht erfordert klare Ownership  |
| Data Lake ohne Governance | Maximale Flexibilität           | "Data Swamp" — unkontrollierte Qualität      | Ohne Governance ist ein Data Lake wertlos          |

---

## Trade-off-Matrix

| Qualitätsziel         | Data Mesh (gewählt) | Zentrales DWH | Data Lake ohne Governance |
|-----------------------|---------------------|---------------|---------------------------|
| Datenqualität         | ⭐⭐⭐⭐              | ⭐⭐⭐⭐         | ⭐⭐                       |
| Analytik-Geschwindigkeit | ⭐⭐⭐⭐⭐          | ⭐⭐            | ⭐⭐⭐                     |
| DSGVO-Compliance      | ⭐⭐⭐⭐              | ⭐⭐⭐          | ⭐                         |
| Team-Autonomie        | ⭐⭐⭐⭐⭐            | ⭐⭐            | ⭐⭐⭐⭐⭐                  |
| Initiale Komplexität  | ⭐⭐⭐                | ⭐⭐⭐⭐         | ⭐⭐⭐⭐⭐                  |

---

## Kosten-Nutzen-Analyse

**Initiale Kosten:**
- Data-Ownership-Zuweisung und Dokumentation: 5 Personentage
- Privacy-by-Design-Review aller Datenmodelle: 10–20 Personentage (einmalig)
- Technische Löschfähigkeit implementieren: 5–15 PT pro Domäne

**Laufende Kosten:**
- Data-Steward-Rolle: ~10% einer Engineering-Stelle pro Domänen-Team
- Compliance-Audits: 2 × jährlich, je 2–3 Personentage

**Erwarteter Nutzen:**
- DSGVO-Bußgeld-Risiko: bis 4% Jahresumsatz ohne Compliance — Versicherungswert der Implementierung
- Analytics-Tickets für Engineering: −80% durch Self-serve-Infrastruktur
- Datenverlust-Incidents: messbar durch Backup-Testing (→ ADR-063)

**Break-Even:** Bereits beim ersten verhinderten DSGVO-Auskunftsersuchen (Manualaufwand ohne System: 40–80 Stunden) oder beim ersten verhinderten Bußgeld.

---

## Konsequenzen

**Sofort:**
- Data-Ownership-Register: `docs/data/ownership.md` — jede Datenklasse mit Owner, Steward, Retention, DSGVO-Relevanz
- Privacy-Impact-Assessment-Template für neue Features die personenbezogene Daten verarbeiten
- Technische Löschfähigkeit: Auskunfts- und Lösch-Endpunkte (→ ADR-035) sind Pflicht vor Go-Live

**Mittelfristig:**
- Self-serve Analytics: dbt + Apache Superset oder Metabase für Domain-Teams
- Daten-Qualitäts-SLAs: jede Domain-API dokumentiert Schema, Freshness und Availability

**Risiken:**
- **Inkonsistente Domänen-Schemas**: Wahrscheinlichkeit H ohne Standards, M mit. Mitigation: zentrales Schema-Registry (Confluent / Apicurio).
- **DSGVO-Auskunftsersuchen ohne Löschfähigkeit**: Wahrscheinlichkeit M, Auswirkung H (Bußgeld). Mitigation: technische Löschfähigkeit als Akzeptanzkriterium vor Launch.

---

## Akzeptanzkriterien

- [ ] `docs/data/ownership.md` existiert mit allen Datensystemen
- [ ] Jede personenbezogene Datenklasse hat definierten Retention-Zeitraum
- [ ] Technische Löschfähigkeit implementiert und getestet (→ ADR-035)
- [ ] Privacy-by-Design-Checkliste ist Teil des DoD (→ ADR-051)
- [ ] Jährliches Data-Audit dokumentiert

---

## Verwandte ADRs

- [ADR-035](ADR-035-gdpr-privacy-by-design.md) — technische DSGVO-Implementierung
- [ADR-063](ADR-063-backup-disaster-recovery.md) — Backup für Transaktionsdaten
- [ADR-023](ADR-023-domain-driven-design.md) — Domain-Ownership als DDD-Konzept
- [ADR-057](ADR-057-sbom-dependency-auditing.md) — Supply-Chain für Datenpipelines
