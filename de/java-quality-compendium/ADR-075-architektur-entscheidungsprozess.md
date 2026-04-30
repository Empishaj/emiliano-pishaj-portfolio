# ADR-075 — Der Architektur-Entscheidungsprozess selbst

| Feld              | Wert                                              |
|-------------------|---------------------------------------------------|
| Status            | ✅ Akzeptiert                                     |
| Entscheider       | CTO / Architektur-Board                           |
| Datum             | 2024-01-01                                        |
| Review-Datum      | 2025-01-01                                        |
| Kategorie         | Governance / Meta-ADR                             |
| Betroffene Teams  | Alle Engineering-Teams                            |

---

## Kontext & Treiber

Ohne definierten Entscheidungsprozess entstehen Architekturentscheidungen implizit, individuell und undokumentiert. Die Konsequenzen: Cargo-Culting (Technologien werden kopiert weil "Netflix das macht"), Entscheidungsparalyse, inkonsistente Codebasis, und — am schädlichsten — gute Entscheidungen die nach 12 Monaten niemand mehr begründen kann und deshalb zurückgedreht werden.

**Qualitätsziele unter Druck:**
- Konsistenz der Architektur über Teams und Zeit
- Nachvollziehbarkeit technischer Schulden
- Einbeziehung der richtigen Stakeholder zur richtigen Zeit
- Entscheidungsgeschwindigkeit (kein Entscheidungs-Bottleneck beim CTO)

---

## Entscheidung

Architekturentscheidungen die mindestens zwei der folgenden Kriterien erfüllen, werden als ADR dokumentiert und durchlaufen einen definierten Review-Prozess vor der Umsetzung:

- Betrifft mehr als ein Team
- Ist schwer rückgängig zu machen (Datenbankwahl, API-Kontrakt, Security-Modell)
- Hat Auswirkungen auf Performance, Sicherheit oder Kosten > 1.000 EUR/Monat
- Widerspricht einem bestehenden ADR

---

## Begründung

**Referenzen:**
- Michael Nygard, "Documenting Architecture Decisions" (2011) — Originalkonzept: ADRs als lightweight, nachvollziehbare Dokumentation die mit dem Code lebt.
- Gregor Hohpe, "The Software Architect Elevator" (2020), Kap. 8 — Architekturentscheidungen müssen die Begründung transportieren, nicht nur das Ergebnis; sonst werden sie nach Personalwechsel blind zurückgedreht.
- DORA State of DevOps Report 2023 — Teams mit dokumentierten Architektur-Standards haben 2,4× höhere Deployment-Frequenz und 50% weniger Change-Failure-Rate.
- ThoughtWorks Technology Radar 2023 — "Architecture Decision Records" als ADOPT empfohlen.

---

## ADR-Lebenszyklus

```
Proposed → [Review 5 Werktage] → Accepted / Rejected
                                        ↓
                               [Implementierung]
                                        ↓
                               [Review-Datum: 6-12 Mo]
                                        ↓
                    Confirmed (weiterhin gültig) / Superseded (neues ADR)
```

---

## Alternativen & Warum sie abgelehnt wurden

| Alternative              | Stärken                              | Schwächen                                    | Ablehnungsgrund                              |
|--------------------------|--------------------------------------|----------------------------------------------|----------------------------------------------|
| Wiki-Seiten              | Einfach zu schreiben                 | Nicht versioniert, nicht im Code-Repository  | Veraltet sofort, nicht reviewbar als PR      |
| RFCs (wie bei Google)    | Sehr detailliert, Peer-Review-Kultur | Schwerer Prozess, hohe Hürde                 | Zu langsam für kleine Teams                  |
| Mündliche Entscheidungen | Schnell                              | Kein Audit-Trail, Wissen geht verloren       | Das ist das Problem das wir lösen            |
| Nur Kommentare im Code   | Nah am Code                          | Kein übergreifender Kontext, nicht durchsuchbar | Kontext fehlt, kein formaler Entscheidungsweg |

---

## Trade-off-Matrix

| Qualitätsziel          | ADR-Prozess (gewählt) | Wiki   | Mündlich |
|------------------------|-----------------------|--------|----------|
| Nachvollziehbarkeit    | ⭐⭐⭐⭐⭐            | ⭐⭐⭐  | ⭐       |
| Entscheidungsgeschw.   | ⭐⭐⭐⭐              | ⭐⭐⭐⭐| ⭐⭐⭐⭐⭐|
| Team-Akzeptanz         | ⭐⭐⭐⭐              | ⭐⭐⭐  | ⭐⭐⭐⭐  |
| Konsistenz über Zeit   | ⭐⭐⭐⭐⭐            | ⭐⭐    | ⭐       |
| Versionierbarkeit      | ⭐⭐⭐⭐⭐            | ⭐     | ⭐       |

---

## Kosten-Nutzen-Analyse

**Initiale Kosten:**
- ADR-Vorlage erstellen & Team schulen: 2 Personentage
- Bestehende Entscheidungen dokumentieren (Backlog): 5–10 Personentage (einmalig)

**Laufende Kosten:**
- Pro neuem ADR: 2–4 Stunden (Schreiben + Review)
- Review-Prozess: 1–2 Stunden pro ADR für Reviewer
- Geschätzte Frequenz: 1–3 ADRs pro Monat

**Erwarteter Nutzen:**
- Reduktion von "Warum haben wir das so gemacht?"-Diskussionen: −70% geschätzt
- Kürzere Onboarding-Zeit neuer Entwickler (weniger Wissenssilos): −30% geschätzt
- Weniger Regressions durch vergessene Constraints (z.B. Security-Entscheidungen): messbar via Incident-Rate

**Break-Even:** Nach dem dritten ADR der eine Wiederholung einer bereits getroffenen Entscheidungsdiskussion verhindert — typisch: 2–3 Monate.

---

## Konsequenzen

**Sofort:**
- ADR-Template (dieses Dokument) als `docs/adr/ADR-000-template.md` im Repository hinterlegen.
- CODEOWNERS: `docs/adr/` → CTO oder Architektur-Board als Pflicht-Reviewer.
- Branch Protection: PRs die `docs/adr/` ändern brauchen Architektur-Approval.

**Mittelfristig (3-6 Monate):**
- Bestehende nicht-dokumentierte Entscheidungen nacherfassen.
- ArchUnit-Regeln (→ ADR-009, ADR-061) als maschinenprüfbares Pendant zu jedem strukturellen ADR.

**Langfristig:**
- ADR-Index als lebende Architekturdokumentation — Grundlage für C4-Diagramme (→ ADR-047).
- Jährliches Review: welche ADRs sind noch gültig, welche müssen superseded werden?

**Risiken:**
- **Prozess-Overhead** wird als Bremse empfunden: Wahrscheinlichkeit M, Auswirkung M. Mitigation: klare Schwellwerte (nicht jede Entscheidung braucht ADR), 5-Tage-Review-SLA.
- **ADR-Friedhof**: Entscheidungen werden dokumentiert aber nicht durchgesetzt: Wahrscheinlichkeit H, Auswirkung H. Mitigation: ArchUnit-Tests (→ ADR-061) als maschinelle Durchsetzung.

---

## Akzeptanzkriterien

- [ ] ADR-Template liegt in `docs/adr/ADR-000-template.md`
- [ ] CODEOWNERS-Eintrag für `docs/adr/` ist aktiv
- [ ] Alle ADRs seit Projektbeginn sind nacherfasst oder als "nicht dokumentiert" markiert
- [ ] Jedes neue ADR seit diesem Datum hat ein Review-Datum
- [ ] Mindestens ein ArchUnit-Test existiert pro strukturellem ADR

---

## Verwandte ADRs

- Grundlage für: alle ADRs dieses Kompendiums
- [ADR-009](ADR-009-clean-code-adrs-im-quellcode.md) — ADRs im Quellcode verankern
- [ADR-061](ADR-061-fitness-functions.md) — ArchUnit als maschinelle Durchsetzung
- [ADR-047](ADR-047-c4-architekturdokumentation.md) — C4 als komplementäre Dokumentation
