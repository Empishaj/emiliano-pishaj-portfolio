# ADR-077 — Microservices vs. Monolith: Der Entscheidungsrahmen

| Feld              | Wert                                              |
|-------------------|---------------------------------------------------|
| Status            | ✅ Akzeptiert                                     |
| Entscheider       | CTO                                               |
| Datum             | 2024-01-01                                        |
| Review-Datum      | 2025-01-01                                        |
| Kategorie         | Architektur-Strategie                             |
| Betroffene Teams  | Alle Teams, Engineering-Manager, Product          |

---

## Kontext & Treiber

Die Frage "Microservices oder Monolith?" wird in der Industrie religiös diskutiert — oft ohne Bezug zu den tatsächlichen Treibern. Teams wählen Microservices wegen Hype (Netflix macht das), nicht wegen konkreter Probleme die sie lösen. Das Ergebnis: verteilte Komplexität ohne den Nutzen von Microservices, weil die Treiber fehlen.

**Konkrete Treiber in diesem Projekt:**
- Aktuelle Teamgröße und Wachstumsprognose
- Deployment-Unabhängigkeit einzelner Teams
- Unterschiedliche Skalierungsanforderungen verschiedener Domänen
- Technologie-Diversität-Anforderungen

---

## Entscheidung

Wir starten als **Modulith** (→ ADR-056) und migrieren explizit einzelne Module zu Microservices wenn mindestens **zwei** der folgenden konkreten Treiber auftreten:

1. **Unabhängiges Deployment**: zwei Teams können sich nicht koordinieren und blockieren sich gegenseitig
2. **Unterschiedliche Skalierung**: Modul X braucht 10× mehr Ressourcen als der Rest
3. **Technologie-Divergenz**: Modul X muss eine andere Sprache/Runtime verwenden (z.B. ML in Python)
4. **Regulatorische Isolation**: Compliance erfordert physische Datentrennung (z.B. PCI-DSS)
5. **Team-Autonomie > 8 Personen**: Conway's Law — die Architektur folgt der Kommunikationsstruktur

---

## Begründung

**Referenzen:**
- Sam Newman, "Building Microservices" (2022), 2. Auflage, Kap. 1 — "Don't start with microservices. Start with a monolith and extract services when needed." Explizite Empfehlung für Monolith-First.
- Martin Fowler, "Microservice Premium" (2015) — Microservices haben einen Komplexitätspreis der nur bei ausreichend großen Systemen rentiert.
- Melvin Conway, "How Do Committees Invent?" (1968) — "Organisations which design systems [...] are constrained to produce designs which are copies of the communication structures of these organisations." Team-Struktur bestimmt Architektur, nicht umgekehrt.
- DORA State of DevOps 2019 — Low-performing Teams mit Microservices haben *schlechtere* Ergebnisse als High-performers mit Monolithen. Microservices verstärken bestehende Fähigkeiten, ersetzen sie nicht.
- Stefan Tilkov, "Don't start with a monolith" (2015) — Gegenperspektive: wenn Bounded Contexts klar sind, können Microservices von Anfang an sinnvoll sein.

---

## Entscheidungsbaum: Modul → Service extrahieren

```
Gibt es ein konkretes Problem mit dem Modulith?
├── Nein → Modulith behalten
└── Ja → Welches Problem?
    ├── Teams blockieren sich gegenseitig beim Deploy
    │   └── UND Team > 8 Personen → Service extrahieren
    ├── Modul braucht 5× mehr Ressourcen als Rest
    │   └── UND Skalierungskosten > 2.000 EUR/Monat → Service extrahieren
    ├── Modul muss andere Technologie nutzen
    │   └── Kann das mit JVM/Polyglot gelöst werden? → Nein → Service
    └── Compliance erfordert physische Trennung
        └── Sofort Service extrahieren
```

---

## Alternativen & Warum sie abgelehnt wurden

| Alternative                        | Stärken                                  | Schwächen                                            | Ablehnungsgrund                                        |
|------------------------------------|------------------------------------------|------------------------------------------------------|-------------------------------------------------------|
| Microservices von Anfang an        | Team-Autonomie von Tag 1                 | Verteilte Komplexität ohne Treiber, Distributed Monolith-Risiko | DORA: schlechtere Ergebnisse ohne Reife; Newman: Monolith-First |
| Strikter Monolith ohne Modulgrenzen | Einfachste Implementierung               | Skaliert nicht mit Team, verhindert spätere Extraktion | "Big Ball of Mud" — nicht das Ziel                    |
| Sofortige Service-Extraktion nach DDD | Aligned mit Bounded Contexts           | Erfordert perfektes DDD-Verständnis vor erstem Deploy | Bounded Contexts sind zu Beginn oft unklar            |

---

## Trade-off-Matrix

| Qualitätsziel              | Modulith (gewählt) | Microservices sofort | Strikter Monolith |
|----------------------------|--------------------|----------------------|-------------------|
| Deployment-Einfachheit     | ⭐⭐⭐⭐⭐           | ⭐⭐                  | ⭐⭐⭐⭐⭐          |
| Team-Autonomie             | ⭐⭐⭐⭐             | ⭐⭐⭐⭐⭐             | ⭐⭐               |
| Operativer Aufwand         | ⭐⭐⭐⭐⭐           | ⭐⭐                  | ⭐⭐⭐⭐⭐          |
| Skalierbarkeit             | ⭐⭐⭐               | ⭐⭐⭐⭐⭐             | ⭐⭐               |
| Migrationsfähigkeit        | ⭐⭐⭐⭐⭐           | ⭐⭐⭐                 | ⭐⭐               |
| Cognitive Load             | ⭐⭐⭐⭐             | ⭐⭐                  | ⭐⭐⭐⭐⭐          |

---

## Kosten-Nutzen-Analyse: Microservice-Extraktion (wenn Trigger erreicht)

**Kosten einer Service-Extraktion:**
- Technische Umsetzung: 10–30 Personentage (abhängig von Modul-Größe)
- Betrieb: +300–500 EUR/Monat pro Service (Kubernetes, Monitoring, Logging)
- Kognitiver Overhead: +1–2 Wochen Einarbeitung pro Entwickler
- Contract Testing (→ ADR-019): +5 PT initial

**Nutzen der Extraktion (quantifizierbar):**
- Independent Deployment: −2 Tage/Monat Koordinationsaufwand zwischen Teams
- Separate Skalierung: −X EUR/Monat Infrastrukturkosten wenn Modul isoliert skaliert
- Risikoreduktion: Fehler in Service A stürzen Service B nicht mehr ab

**Entscheidungsregel für CTO:** Service-Extraktion lohnt sich wenn (Monatlicher Nutzen × 12) > (Extraktionskosten + 12 × Betriebskosten)

---

## Konsequenzen

**Sofort:**
- Spring Modulith (→ ADR-056): Modulgrenzen jetzt durchsetzen, auch wenn alles ein Deployment ist
- Schema-per-Modul (→ ADR-058 Muster): Datenbank-Isolation vorbereiten
- Kein direkter DB-Zugriff über Modulgrenzen — nur über definierte Service-APIs

**Bei Service-Extraktion:**
- Strangler Fig Pattern (Martin Fowler): neue Funktionalität im neuen Service, alter Code bleibt vorerst
- Contract Testing obligatorisch (→ ADR-019)
- Outbox Pattern für transaktionale Konsistenz (→ ADR-042)

**Risiken:**
- **Distributed Monolith**: Service A ruft Service B synchron auf — kein echter Nutzen, aber alle Nachteile von Microservices: Wahrscheinlichkeit H, Auswirkung H. Mitigation: asynchrone Kommunikation als Standard (→ ADR-041).
- **Zu frühe Extraktion**: Bounded Contexts ändern sich in frühen Phasen häufig: Wahrscheinlichkeit M, Auswirkung M. Mitigation: 6-Monate-Regel — kein Modul wird in den ersten 6 Monaten extrahiert.

---

## Akzeptanzkriterien

- [ ] Entscheidungsbaum ist dokumentiert und dem gesamten Engineering-Team bekannt
- [ ] Spring Modulith verifiziert Modulgrenzen in CI (→ ADR-056)
- [ ] Kein direkter DB-Zugriff über Modulgrenzen (ArchUnit-Test)
- [ ] Service-Extraktion-Trigger sind messbar dokumentiert
- [ ] Nächstes Team-Review: prüfen ob Trigger erreicht wurden

---

## Verwandte ADRs

- [ADR-056](ADR-056-modulith.md) — Spring Modulith als Implementierung
- [ADR-023](ADR-023-domain-driven-design.md) — Bounded Contexts als Basis für Service-Grenzen
- [ADR-019](ADR-019-contract-testing.md) — Contract Testing bei Service-Extraktion obligatorisch
- [ADR-041](ADR-041-event-driven-kafka.md) — Asynchrone Kommunikation als Standard zwischen Services
