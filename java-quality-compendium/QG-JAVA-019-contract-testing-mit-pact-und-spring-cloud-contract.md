# QG-JAVA-019 — Contract Testing mit Pact und Spring Cloud Contract

## Dokumentstatus

| Aspekt | Details/Erklärung |
| --- | --- |
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-019 |
| Titel | Contract Testing mit Pact und Spring Cloud Contract |
| Status | Accepted / verbindlicher Qualitätsstandard für API-Verträge zwischen Services |
| Version | 2.0 |
| Datum | 2026-05-03 (überarbeitet von 1.0.0 vom 2026-05-03) |
| Review-Zyklus | Halbjährlich oder bei Major-Änderung von Pact JVM, Spring Cloud Contract, Spring Boot oder Java |
| Kategorie | Testing · Microservices · API-Kompatibilität · CI/CD |
| Zielgruppe | Java-Entwickler, Tech Leads, QA, Architektur, DevOps, Platform Engineering, Security |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.4+, JUnit 5.10+, Pact JVM 4.6+, Spring Cloud Contract 4.1+ (passend zur verwendeten Spring-Cloud-Release-Train-Version) |
| Verbindlichkeit | Verbindlich für alle Service-zu-Service-Schnittstellen, die unabhängig deployed oder von mehreren Consumern genutzt werden. Abweichungen sind im Pull Request nachvollziehbar zu begründen. |
| Technischer Prüfstatus | Code-Beispiele sind gegen Pact JVM 4.6+ und Spring Cloud Contract 4.1+ referenzbasiert validiert; Provider-State-Beispiele setzen Testcontainers-isolierte Datenbanken voraus |
| Security-Relevanz | Hoch: API-Verträge beeinflussen Datenminimierung, Zugriffskontext, Fehlerantworten, PII-Leaks, Mandantengrenzen und Rückwärtskompatibilität |
| Schwester-Guidelines | QG-JAVA-006 (Spring-Boot-Serviceschicht), QG-JAVA-008 (Objektorientierung) |
| Wesentliche Änderungen ggü. v1.0 | Korrigierte Provider-State-Beispiele · `consumerVersionSelectors` als Pflicht · `publishVerificationResults` ergänzt · `record-deployment`-Workflow · Random Port statt fester Port · `LambdaDsl` als moderne API · `MockMvcTestTarget`-Alternative · Bidirectional Contracts (Pact V4) · Messaging Contracts mit `MessagePact` · Spring Cloud Contract Stub Runner · Performance- und Wartungsaspekte · Tenant-Beispiel in Provider States · vollständige ArchUnit-Sektion · strukturell an QG-JAVA-006 v2 angeglichen |

---

## Inhaltsverzeichnis

1. [Zweck dieser Richtlinie](#1-zweck-dieser-richtlinie)
2. [Kurzregel für Entwickler](#2-kurzregel-für-entwickler)
3. [Verbindlicher Standard](#3-verbindlicher-standard)
4. [Geltungsbereich](#4-geltungsbereich)
5. [Begriffe](#5-begriffe)
6. [Technischer Hintergrund](#6-technischer-hintergrund)
7. [Wann Contract Testing erforderlich ist](#7-wann-contract-testing-erforderlich-ist)
8. [Pact: Consumer-Driven Contract Testing](#8-pact-consumer-driven-contract-testing)
9. [Consumer-Contract-Regeln](#9-consumer-contract-regeln)
10. [Provider-Verifikation mit Pact](#10-provider-verifikation-mit-pact)
11. [Pact Broker, Versionierung und Deployment-Gates](#11-pact-broker-versionierung-und-deployment-gates)
12. [Spring Cloud Contract](#12-spring-cloud-contract)
13. [Bidirectional Contracts (Pact V4)](#13-bidirectional-contracts-pact-v4)
14. [Messaging Contracts](#14-messaging-contracts)
15. [Pact oder Spring Cloud Contract?](#15-pact-oder-spring-cloud-contract)
16. [Anti-Patterns](#16-anti-patterns)
17. [Security- und SaaS-Aspekte](#17-security--und-saas-aspekte)
18. [Contract Testing, OpenAPI und GraphQL](#18-contract-testing-openapi-und-graphql)
19. [Teststrategie](#19-teststrategie)
20. [CI/CD-Standard](#20-cicd-standard)
21. [Performance- und Wartungsaspekte](#21-performance--und-wartungsaspekte)
22. [Review-Checkliste](#22-review-checkliste)
23. [Automatisierbare Prüfungen](#23-automatisierbare-prüfungen)
24. [Migration bestehender Tests](#24-migration-bestehender-tests)
25. [Ausnahmen](#25-ausnahmen)
26. [Definition of Done](#26-definition-of-done)
27. [Quellen und weiterführende Literatur](#27-quellen-und-weiterführende-literatur)

### Wie liest du dieses Dokument

Dieses Dokument ist mit ca. 100 KB ein Nachschlagewerk, kein Lesebuch. Drei Lesepfade werden empfohlen:

**Wenn du neu im Team bist:** Starte mit Sektion 2 (Kurzregel), lies dann Sektion 1 (Zweck), Sektion 7 (Wann erforderlich), Sektion 8 (Pact-Grundablauf) und Sektion 16 (Anti-Patterns). Damit hast du die mentale Karte für 80 % aller Contract-Test-Reviews.

**Wenn du im Code-Review bist:** Springe direkt zu Sektion 22 (Review-Checkliste). Jede Zeile hat einen Anker zur Detail-Sektion mit der Begründung. Bei Verstoß gegen eine Muss-Regel: Sektion 3.1.

**Wenn du etwas Spezifisches suchst:** Inhaltsverzeichnis oben. Häufigste Punkte: Provider States → Sektion 10. Broker-Gates → Sektion 11. Tool-Wahl → Sektion 15. Messaging → Sektion 14. Performance → Sektion 21.

**Wenn du eine Tool-Entscheidung treffen musst:** Sektion 7 (ist Contract Testing erforderlich?), dann Sektion 15 (Pact vs. SCC), dann Sektion 13 (Bidirectional als drittes Modell).

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Contract Testing in Java- und Spring-Boot-basierten Microservice-Systemen eingesetzt wird, um API-Kompatibilität zwischen Consumer und Provider frühzeitig, automatisiert und nachvollziehbar zu prüfen.

Contract Testing beantwortet eine konkrete Frage:

> **Erfüllt der Provider weiterhin genau die API-Erwartungen, die ein Consumer tatsächlich nutzt?**

Damit ersetzt Contract Testing nicht alle Integrationstests, nicht alle End-to-End-Tests und keine Security-Tests. Es reduziert aber das Risiko, dass API-Breaking-Changes erst in Staging, Produktion oder beim echten Consumer auffallen.

In modernen Spring-Boot-Microservice-Landschaften — insbesondere in SaaS-Plattformen mit mehreren Tenants und unabhängig deployten Services — ist Contract Testing kein Luxus, sondern eine Voraussetzung für sichere Releases. Diese Richtlinie macht die Praxis explizit: welche Werkzeuge für welchen Kontext, welche Anti-Patterns vermieden werden müssen, welche Sicherheits- und Tenant-Aspekte einfließen müssen, und wie die Pipeline-Integration in CI/CD aussieht.

---

## 2. Kurzregel für Entwickler

Für Service-zu-Service-Kommunikation werden Contract Tests eingesetzt, wenn Consumer und Provider unabhängig entwickelt, getestet oder deployed werden. Consumer dürfen nicht nur gegen manuell gepflegte WireMock-Stubs testen. Provider müssen veröffentlichte Contracts automatisiert gegen ihre echte API verifizieren — mit `consumerVersionSelectors` und `publishVerificationResults`. Provider States müssen idempotent, tenant-aware und transaktional isoliert sein. Deployments dürfen nur erfolgen, wenn `can-i-deploy` für die Ziel-Umgebung erfolgreich war.

Für polyglotte Microservice-Landschaften ist Pact der bevorzugte Standard. Für reine Spring-/JVM-Landschaften mit stark provider-gesteuertem API-Modell kann Spring Cloud Contract verwendet werden. Für Provider, die OpenAPI-First arbeiten, sind Bidirectional Contracts (Pact V4 / PactFlow) eine moderne dritte Option.

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

Jede Service-zu-Service-API mit unabhängigem Deployment MUSS folgende Regeln erfüllen:

1. Sie MUSS Contract Tests haben, wenn ein API-Bruch produktive Auswirkungen hätte.
2. Consumer-Contracts MÜSSEN minimal sein und nur Felder beschreiben, die der Consumer tatsächlich nutzt.
3. Provider-Verifikation MUSS automatisiert gegen die echte API laufen, nicht gegen Mocks oder Test-Doubles.
4. `@PactBroker` MUSS mit `consumerVersionSelectors` konfiguriert sein, damit alle relevanten Pacts (main branch, deployed/released, matching branch) verifiziert werden.
5. Provider-Verifikation MUSS Verification Results an den Broker zurückspielen (`pact.verifier.publishResults=true`).
6. Provider States MÜSSEN deterministisch, idempotent und isoliert sein.
7. Provider States in mandantenfähigen Systemen MÜSSEN Tenant-Kontext explizit setzen.
8. Provider-Tests MÜSSEN gegen isolierte Test-Datenbanken laufen (Testcontainers oder gleichwertig).
9. Contracts DÜRFEN keine produktiven personenbezogenen Daten, Secrets oder Tokens enthalten.
10. Pact Broker und CI/CD MÜSSEN über Secrets-Management verbunden werden, niemals über committete Tokens.
11. Deployments MÜSSEN durch `can-i-deploy --to-environment` blockiert werden, wenn Vertragsverletzungen vorliegen.
12. Nach erfolgreichem Deployment MUSS `record-deployment` ausgeführt werden, damit der Broker den Deployment-Zustand kennt.
13. Contract Tests MÜSSEN in Consumer- und Provider-Pipeline laufen.
14. Contract-Verletzungen MÜSSEN wie Build-Brüche behandelt werden — kein Override-Mechanismus ohne dokumentierte Architekturentscheidung.
15. Pact-Versionen MÜSSEN über Git-SHA oder semantische Versionen eindeutig identifizierbar sein.
16. Pacticipant-Branches MÜSSEN explizit gesetzt werden (Pact JVM 4.x verwendet Branches als modernen Ersatz für Tags).
17. Consumer-Contracts MÜSSEN Fehlerfälle abdecken (mindestens 401, 403, 404, 409 bei kritischen APIs).

### 3.2 Darf-nicht-Regeln

Ein Contract Test DARF NICHT:

1. Den vollständigen Provider-DTO-Snapshot spiegeln (siehe Sektion 16.1).
2. Gegen einen laufenden echten Provider als Consumer-Test ausgeführt werden (Pact Mockserver wird verwendet).
3. Provider States als globale Demo-Datenbank voraussetzen.
4. Produktive Daten oder Secrets enthalten — auch nicht in Beispielwerten.
5. Berechtigungsprüfungen oder fachliche Autorisierung ersetzen.
6. `pactVerify` ohne `publishVerificationResults` ausführen, wenn der Broker konsultiert wird.
7. Mit festen Ports (`port = "8080"`) konfiguriert sein — das führt zu Port-Konflikten in CI.
8. `userRepository.deleteById(id)` ohne Existenz-Prüfung in Provider States verwenden.
9. Provider-Tests ohne `@Transactional`-Rollback oder isolierte Datenbank ausführen.
10. Contracts ohne Tenant-Header in mandantenfähigen Systemen schreiben.
11. `@PactBroker` ohne `consumerVersionSelectors` verwenden — das verifiziert nur `latest` und verpasst Production-Pacts.

### 3.3 Sollte-Regeln

Ein Contract Test SOLLTE:

1. `LambdaDsl.newJsonBody(...)` statt `PactDslJsonBody` verwenden — moderne, wartbarere API.
2. Type-Matcher (`stringType`) statt Wert-Matcher (`stringValue`) verwenden, außer bei fachlich relevanten Konstanten.
3. Provider States nach dem Schema „Resource X exists in state Y" benennen, nicht „User1Setup".
4. `MockMvcTestTarget` statt `HttpTestTarget` verwenden, wenn Performance der Verifikation wichtiger ist als Web-Stack-Vollständigkeit.
5. Pacticipant-Versionen mit Branch-Information veröffentlichen (`pact_provider_branch`).
6. Pact-Tests in einem dedizierten Package (`..contracts..`) ablegen, damit ArchUnit-Regeln greifen können.
7. Bidirectional Contracts (Pact V4) erwägen, wenn der Provider OpenAPI-First arbeitet.
8. Performance-Aspekte berücksichtigen (Sektion 21), wenn ein Provider mehr als 5 Consumer hat.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für:

1. HTTP-APIs zwischen Microservices (REST/JSON, GraphQL).
2. Synchrone Service-zu-Service-Aufrufe.
3. Interne Plattform-APIs.
4. Externe APIs, sofern Consumer-Erwartungen kontrolliert beschreibbar sind.
5. Events und Messaging-Schnittstellen (Kafka, RabbitMQ, SQS) — siehe Sektion 14.
6. Kritische Provider mit mehreren Consumern.
7. APIs mit unabhängigen Deployments.
8. APIs mit hohem Rückwärtskompatibilitätsanspruch.
9. Provider, die OpenAPI-Schemas pflegen — siehe Bidirectional Contracts in Sektion 13.

Diese Richtlinie gilt nicht als alleinige Teststrategie für:

1. Vollständige End-to-End-Prozessvalidierung.
2. Last- und Performance-Tests.
3. Security-Tests (Penetration Testing, OWASP-Scans).
4. Autorisierungstests im Detail.
5. Datenbankmigrationstests.
6. UI-Verhalten.
7. Fachliche Prozessketten über viele Services hinweg.
8. Exploratives Testen.
9. Reine interne Klassen-zu-Klassen-Kopplung ohne API-Grenze.

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Consumer | Service, der eine API nutzt | `order-service` ruft `user-service` auf |
| Provider | Service, der eine API bereitstellt | `user-service` liefert User-Daten |
| Pacticipant | Im Broker registrierter Consumer oder Provider | `user-service`, `order-service` |
| Contract / Pact | Maschinenlesbare Beschreibung einer konkreten API-Erwartung | Pact-JSON-Datei, SCC-DSL |
| Interaction | Einzelne Anfrage-Antwort-Erwartung | `GET /api/users/1` liefert `200` mit `id`, `name`, `email` |
| Provider State | Vorbedingung, damit der Provider die erwartete Antwort liefern kann | „User with ID 1 exists" |
| Pact Broker | Zentrale Ablage für Pact-Contracts und Verifikationsergebnisse | Self-hosted Pact Broker oder PactFlow |
| Pacticipant Branch | Zweig, auf dem ein Consumer/Provider entwickelt wird | `main`, `feature/payment-v2` |
| Pacticipant Version | Eindeutige Version eines Consumers/Providers | Git-SHA, semantische Version |
| Environment | Logische Deployment-Umgebung im Broker | `staging`, `production` |
| Verification Result | Ergebnis der Provider-Verifikation eines Pacts | grün/rot, mit Detail-Output |
| Stub | Simulierter Provider für Consumer-Tests | Pact Mockserver, SCC Stub Runner |
| can-i-deploy | CI/CD-Prüfung, ob eine konkrete Version sicher deployed werden darf | Broker-Anfrage mit Pacticipant + Version + Environment |
| record-deployment | Registrierung eines erfolgten Deployments im Broker | `pact-broker record-deployment ...` |
| consumerVersionSelectors | Filter, welche Consumer-Pacts ein Provider verifizieren soll | `mainBranch`, `deployedOrReleased`, `matchingBranch` |
| Bidirectional Contract | Pact V4 Modell mit OpenAPI auf Provider-Seite und Pact auf Consumer-Seite | PactFlow Bidirectional |
| Message Pact | Pact für asynchrone Nachrichten | `OrderCreatedEvent` von `order-service` an `billing-service` |
| Stub Runner | SCC-Mechanismus zur automatischen Stub-Bereitstellung im Consumer | `@AutoConfigureStubRunner` |

---

## 6. Technischer Hintergrund

In einer Microservice-Architektur sind Services über Schnittstellen gekoppelt. Diese Kopplung ist nicht automatisch schlecht; sie muss aber explizit gemacht und getestet werden.

Ein Provider kann eine Änderung einführen, die aus seiner Sicht harmlos ist, aber einen Consumer bricht. Beispiele:

- Feld wird umbenannt: `email` → `emailAddress`.
- HTTP-Status ändert sich: `404` → `200` mit Fehlerobjekt.
- Pflichtfeld verschwindet.
- Datentyp ändert sich: `id` von Zahl zu String.
- Error-Response-Struktur wird geändert.
- Content-Type-Header fehlt.
- Array wird zu Objekt.
- Leere Collection wird zu `null`.
- Paging-Felder werden anders benannt.
- Consumer erwartet ein Feld, das Provider nicht mehr liefert.

End-to-End-Tests können solche Fehler finden, sind aber langsam, teuer, fragil und schwer eindeutig zu debuggen. Contract Tests prüfen API-Erwartungen isolierter und schneller.

**Pact JVM 4.x** verwendet Branches als modernes Identifikationsmodell für Pacticipants. Die ältere Tag-basierte Strategie ist Legacy. Branch-basierte Selektoren wie `mainBranch`, `deployedOrReleased` und `matchingBranch` ersetzen die alte `latest`-Tag-Verwendung. Wer noch mit `latest` arbeitet, verifiziert nicht das, was wirklich in Production läuft.

**Pact Broker 2.80+** unterstützt Environments als first-class concept. `--to-environment staging` ist die moderne Form gegenüber `--to=staging`. Environments müssen einmalig im Broker angelegt werden (`create-environment`), und Deployments müssen aktiv registriert werden (`record-deployment`).

**Pact V4 / PactFlow** unterstützt seit 2022 Bidirectional Contracts: Provider veröffentlicht OpenAPI, Consumer veröffentlichen Pacts, Broker matcht beides automatisch. Das löst den Trade-off zwischen Pact (Consumer-driven) und SCC (Provider-driven) für viele Setups.

**Spring Cloud Contract 4.x** integriert sich nahtlos in Spring Boot und bietet mit Stub Runner einen Mechanismus, bei dem Consumer gegen automatisch generierte und in Maven/Nexus veröffentlichte Stubs testen. Das ist der Kernmehrwert von SCC und unterscheidet es architektonisch von einem reinen Pact-Workflow.

Contract Testing prüft nicht, ob das gesamte System fachlich korrekt arbeitet. Es prüft, ob Consumer und Provider denselben Vertrag verstehen.

---

## 7. Wann Contract Testing erforderlich ist

Contract Testing ist verpflichtend, wenn mindestens eine der folgenden Bedingungen zutrifft:

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
| --- | --- | --- | --- |
| Unabhängiges Deployment | Consumer und Provider werden unabhängig released | `order-service` und `user-service` | **Pflicht** |
| Mehrere Consumer | Ein Provider hat mehrere Aufrufer | `customer-service` wird von Order, Billing, Support genutzt | **Pflicht** |
| Kritische API | Fehler führt zu Zahlungsausfall, Bestellabbruch oder Kundendatenproblem | Payment, Identity, Order | **Pflicht** |
| Externe Integration | Provider oder Consumer liegt außerhalb des Teams | Partner-API, Plattform-API | **Pflicht oder formaler Ersatz** |
| Hohe Änderungsfrequenz | API verändert sich häufig | neue Checkout-API | **Pflicht** |
| E2E-Tests zu langsam | API-Kompatibilität soll früher geprüft werden | CI braucht 45 Minuten | **Pflicht** |
| Provider mit OpenAPI | Provider arbeitet OpenAPI-First | API-First-Plattform | **Bidirectional Contract erwägen** |
| Async Messaging | Events zwischen Services über Kafka, RabbitMQ, SQS | `OrderCreatedEvent` | **Pflicht (Message Pact)** |
| Nur ein Service intern | Keine unabhängige Versionierung, keine echte Schnittstellenkopplung | interne Klassenmethode | Nicht erforderlich |
| Reiner Datenbankzugriff | Kein API-Vertrag zwischen Services | Repository-Test | Nicht geeignet |

---

## 8. Pact: Consumer-Driven Contract Testing

Pact ist besonders geeignet, wenn Consumer ihre Erwartungen selbst beschreiben sollen. Der Consumer schreibt einen Test für seinen API-Client. Nebenbei erzeugt dieser Test einen maschinenlesbaren Contract, der später vom Provider verifiziert wird.

### 8.1 Grundablauf mit Pact

```
Consumer-Test (gegen Pact Mockserver)
    ↓
Pact-JSON-Datei wird erzeugt
    ↓
Pact wird im Broker veröffentlicht (mit Branch-Information)
    ↓
Provider-Pipeline lädt Pacts via consumerVersionSelectors
    ↓
Provider verifiziert Pacts gegen echte Implementierung
    ↓
Verification Results werden an den Broker zurückgegeben
    ↓
CI/CD prüft per can-i-deploy --to-environment, ob Deployment sicher ist
    ↓
Nach Deployment: record-deployment registriert Status im Broker
```

### 8.2 Schlechte Anwendung: WireMock ohne gemeinsamen Contract

```java
// ❌ Kein Contract zwischen Consumer und Provider
@Test
void getUser_returnsUserDto() {
    wireMock.stubFor(get("/api/users/1")
            .willReturn(okJson("""
                {
                  "id": 1,
                  "name": "Max",
                  "email": "max@example.com"
                }
                """)));

    var user = userClient.findById(1L);

    assertThat(user.name()).isEqualTo("Max");
}
```

Dieser Test ist nicht wertlos, aber er löst nicht das Contract-Problem. Der Stub lebt nur im Consumer-Projekt. Wenn der Provider später `email` in `emailAddress` umbenennt, bleibt dieser Consumer-Test grün — bis der Provider in Production deployed wird und der Consumer bricht. Genau deshalb braucht es einen Contract, der vom Provider verifiziert wird.

### 8.3 Gute Anwendung: Consumer definiert minimale Erwartung

```java
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "user-service")        // ✅ kein fester Port
class UserServiceClientContractTest {

    @Pact(consumer = "order-service")
    RequestResponsePact getUserById(PactDslWithProvider builder) {
        return builder
                .given("User with ID 1 exists in tenant tenant-test")  // ✅ tenant-aware
                .uponReceiving("GET user by id")
                    .method("GET")
                    .path("/api/users/1")
                    .headers("Accept", "application/json",
                             "X-Tenant-Id", "tenant-test")             // ✅ Tenant-Header
                .willRespondWith()
                    .status(200)
                    .headers("Content-Type", "application/json")
                    .body(newJsonBody(body -> {                        // ✅ moderne LambdaDsl
                        body.integerType("id", 1);
                        body.stringType("name", "Max Mustermann");
                        body.stringType("email", "max@example.com");
                    }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUserById")
    void findById_parsesUserResponse_whenUserExists(MockServer mockServer) {
        // ✅ MockServer.getUrl() liefert die echte URL mit Random-Port
        var client = new UserServiceClient(mockServer.getUrl());

        var user = client.findById(1L, "tenant-test");

        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.name()).isEqualTo("Max Mustermann");
        assertThat(user.email()).isEqualTo("max@example.com");
    }
}
```

**Drei Disziplinen, die diesen Test produktionsreif machen:**

1. **Kein fester Port.** `@PactTestFor(providerName = "user-service")` ohne `port`-Attribut → Pact wählt automatisch einen freien Port. Der `MockServer`-Parameter bekommt die tatsächliche URL injiziert. Vermeidet Port-Konflikte in CI.
2. **`LambdaDsl.newJsonBody(...)` statt `PactDslJsonBody`.** Block-Struktur entspricht JSON-Struktur — bei verschachtelten Objekten und Arrays dramatisch lesbarer. Default seit Pact JVM 4.0.
3. **Tenant-Header und Tenant-spezifischer Provider State.** In SaaS-Systemen ist das nicht optional — der Provider muss wissen, in welchem Tenant er den User suchen soll.

Wichtig ist außerdem: Der Consumer beschreibt nicht die vollständige Provider-Antwort, sondern nur die Felder, die er wirklich benötigt. Dadurch bleibt der Provider evolvierbar.

---

## 9. Consumer-Contract-Regeln

Consumer-Contracts müssen minimal, fachlich relevant und stabil sein.

### 9.1 Nur genutzte Felder beschreiben

```java
// ❌ Schlecht: kompletter DTO-Snapshot
.body(newJsonBody(body -> {
    body.integerType("id", 1);
    body.stringType("name", "Max");
    body.stringType("email", "max@example.com");
    body.stringType("phone", "+49123456789");
    body.stringType("createdAt", "2026-01-01T00:00:00Z");
    body.stringType("internalStatus", "ACTIVE");
    body.stringType("crmId", "CRM-123");
}).build())
```

Wenn der Consumer nur `id`, `name` und `email` nutzt, dürfen `phone`, `internalStatus`, `crmId` und ähnliche Felder nicht in den Consumer-Contract. Sonst koppelt sich der Consumer unnötig an Provider-Details.

```java
// ✅ Gut: minimal
.body(newJsonBody(body -> {
    body.integerType("id", 1);
    body.stringType("name", "Max");
    body.stringType("email", "max@example.com");
}).build())
```

### 9.2 Typen statt exakte Werte prüfen

```java
// ❌ Schlecht: koppelt an einen konkreten Wert
.body(newJsonBody(body -> body.stringValue("email", "max@example.com")).build())

// ✅ Gut: prüft Typ und akzeptiert Variation
.body(newJsonBody(body -> body.stringType("email", "max@example.com")).build())

// ✅ Noch besser bei fachlicher Relevanz: Pattern
.body(newJsonBody(body -> body.stringMatcher(
        "email",
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
        "max@example.com"
)).build())
```

### 9.3 Fehlerfälle testen

Consumer-Contracts müssen nicht nur Happy Paths enthalten. Pflicht-Fälle für kritische APIs:

- **404** wenn Ressource fehlt
- **400** wenn Request ungültig ist
- **401** wenn Token fehlt
- **403** wenn Zugriff verboten ist
- **409** wenn fachlicher Konflikt besteht (siehe QG-JAVA-006 Sektion 11.6 — Optimistic Locking)
- **429** wenn Rate Limit greift
- **503** wenn Provider temporär nicht verfügbar ist

```java
@Pact(consumer = "order-service")
RequestResponsePact getUserById_notFound(PactDslWithProvider builder) {
    return builder
            .given("User with ID 99 does not exist in tenant tenant-test")
            .uponReceiving("GET non-existent user")
                .method("GET")
                .path("/api/users/99")
                .headers("X-Tenant-Id", "tenant-test")
            .willRespondWith()
                .status(404)
                .headers("Content-Type", "application/problem+json")
                .body(newJsonBody(body -> {
                    body.stringType("type",
                        "https://api.example.com/problems/user-not-found");
                    body.stringType("title", "User not found");
                    body.integerType("status", 404);
                }).build())
            .toPact();
}
```

---

## 10. Provider-Verifikation mit Pact

Der Provider verifiziert veröffentlichte Contracts gegen seine echte API. Dabei wird nicht der Consumer gestartet. Pact ruft die Provider-Endpunkte mit den im Contract beschriebenen Requests auf und prüft die tatsächliche Response.

### 10.1 Vollständiger Provider-Test

```java
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Provider("user-service")
@PactBroker(
    url = "${PACT_BROKER_BASE_URL}",
    authentication = @PactBrokerAuth(token = "${PACT_BROKER_TOKEN}"),
    consumerVersionSelectors = {                              // ✅ Pflicht!
        @VersionSelector(mainBranch = "true"),                // aktuelle main jedes Consumers
        @VersionSelector(deployedOrReleased = "true"),        // alles in Production
        @VersionSelector(matchingBranch = "true")             // gleicher Branch wie Provider
    }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers                                               // ✅ isolierte Test-DB
class UserServiceProviderContractTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort int port;
    @Autowired UserRepository userRepository;
    @Autowired TenantContext tenantContext;     // siehe QG-JAVA-006 Sektion 14.6

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyContract(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // ✅ Provider States: idempotent, transaktional, tenant-aware

    @State("User with ID 1 exists in tenant tenant-test")
    @Transactional
    void userWithId1Exists() {
        var tenantId = "tenant-test";
        if (userRepository.findByIdAndTenantId(1L, tenantId).isEmpty()) {
            userRepository.save(UserEntity.create(
                1L, tenantId, "Max Mustermann", "max@example.com"
            ));
        }
        // wenn schon vorhanden: nichts tun → idempotent
    }

    @State("User with ID 99 does not exist in tenant tenant-test")
    @Transactional
    void userWithId99DoesNotExist() {
        var tenantId = "tenant-test";
        // ✅ defensiv: nur löschen, wenn vorhanden
        userRepository.findByIdAndTenantId(99L, tenantId)
            .ifPresent(userRepository::delete);
    }

    @State("Tenant tenant-test exists with active subscription")
    @Transactional
    void tenantTestActive() {
        // weitere State-Setups, alle idempotent
    }
}
```

### 10.2 Provider-State-Regeln

| Aspekt | Regel | Beispiel |
| --- | --- | --- |
| Name | Muss exakt zum Consumer-Contract passen | `User with ID 1 exists in tenant tenant-test` |
| Granularität | Nur notwendige Testdaten anlegen | ein User, nicht komplette Demo-Datenbank |
| Idempotenz | Mehrfache Ausführung erzeugt denselben Zustand | `findById().isEmpty() ? save : noop` |
| Determinismus | Keine zufälligen IDs ohne Rückgabe | feste Testdaten oder explizite ID-Rückgabe |
| Isolation | Testcontainers oder transaktionaler Rollback | `@Container PostgreSQLContainer` |
| Sicherheit | Keine produktiven Daten | synthetische Testdaten |
| Tenant | In SaaS-Systemen explizit tenant-aware | `findByIdAndTenantId` |
| Performance | State-Aufbau muss schnell bleiben | keine Massendaten |
| Cleanup | Vor jedem Test definierter Zustand | `@Transactional` + Rollback oder fresh Container |

### 10.3 `MockMvcTestTarget` als Performance-Alternative

Für Provider mit vielen Pacts und langer Verifikationszeit kann `MockMvcTestTarget` schneller sein als `HttpTestTarget`. Trade-off: keine Servlet-Filter, keine echten Connection-Stacks.

```java
@BeforeEach
void before(PactVerificationContext context) {
    // ✅ schneller, aber umgeht Servlet-/Security-Filter
    context.setTarget(new MockMvcTestTarget(mockMvc));
}
```

| Target | Vorteil | Nachteil |
|---|---|---|
| `HttpTestTarget` | testet vollständigen Web-Stack inkl. Filter, Interceptors, Connection-Handling | langsamer, braucht `WebEnvironment.RANDOM_PORT` |
| `MockMvcTestTarget` | schneller, leichtgewichtiger, ohne Server-Start | umgeht Spring-Security-Filter und Servlet-Filter |

Für sicherheitskritische APIs ist `HttpTestTarget` der robustere Default. Für reine Datenformat-Verifikation auf nicht-sicherheitsrelevanten APIs ist `MockMvcTestTarget` eine valide Performance-Optimierung.

---

## 11. Pact Broker, Versionierung und Deployment-Gates

Der Pact Broker ist die zentrale Ablage für Contracts und Verifikationsergebnisse. Er beantwortet im Deployment-Prozess die Frage, ob eine konkrete Consumer- oder Provider-Version sicher deployed werden kann.

### 11.1 Broker-Grundregeln

1. Broker-URL und Token werden niemals im Repository gespeichert.
2. Broker-Zugriff erfolgt über Secrets-Management oder CI/CD-Secret-Store.
3. Consumer veröffentlichen Contracts mit Version **und** Branch.
4. Provider veröffentlichen Verifikationsergebnisse mit Version **und** Branch.
5. Deployments werden über `can-i-deploy --to-environment` geprüft.
6. Nach erfolgreichem Deployment wird `record-deployment` ausgeführt.
7. Environments müssen einmalig im Broker angelegt werden.

### 11.2 Versionierung mit Branches (Pact JVM 4.x)

Die moderne Pact-Strategie verwendet **Branches** statt Tags. Branches bilden Git-Branches direkt im Broker ab:

```bash
# Consumer publishes pact with branch information
pact_consumer_version_branch=main \
pact_consumer_version=$GITHUB_SHA \
./gradlew pactPublish
```

```bash
# Provider publishes verification results with branch information
pact_provider_version_branch=main \
pact_provider_version=$GITHUB_SHA \
pact.verifier.publishResults=true \
./gradlew pactVerify
```

Das ermöglicht den Selectors auf Provider-Seite (`mainBranch`, `matchingBranch`), die richtigen Pacts zu laden.

### 11.3 Environments und record-deployment

```bash
# Einmalig: Environments im Broker anlegen
pact-broker create-environment --name staging --display-name "Staging"
pact-broker create-environment --name production --display-name "Production" --production
```

```bash
# Vor Deployment: Sicherheitsprüfung
pact-broker can-i-deploy \
    --pacticipant user-service \
    --version "$GITHUB_SHA" \
    --to-environment staging

# Nach erfolgreichem Deployment: registrieren
pact-broker record-deployment \
    --pacticipant user-service \
    --version "$GITHUB_SHA" \
    --environment staging
```

**Konsequenz ohne `record-deployment`:** Der Broker weiß nicht, welche Version in Staging läuft. Der nächste Provider-Verifikationslauf mit `consumerVersionSelectors(deployedOrReleased = true)` kann nicht korrekt antworten. `can-i-deploy` für Production wird unsicher.

### 11.4 `can-i-deploy` als Pflicht-Gate

`can-i-deploy` ist kein optionaler Bericht. Es ist ein Deployment-Gate. Wenn der Broker zeigt, dass eine Version relevante Contracts verletzt, darf diese Version nicht in die entsprechende Umgebung deployed werden.

In CI/CD MUSS `can-i-deploy` als Build-Step **vor** dem Deployment laufen. Ein roter Status MUSS das Deployment blockieren — kein Override ohne dokumentierte Architekturentscheidung.

---

## 12. Spring Cloud Contract

Spring Cloud Contract ist eine gute Alternative, wenn die Organisation stark Spring-/JVM-zentriert ist und Contracts zentral durch den Provider oder in einem gemeinsamen Repository gepflegt werden. Der **Kernmehrwert** liegt in der Kombination aus Contract DSL und Stub Runner.

### 12.1 Contract DSL

```groovy
// src/test/resources/contracts/getUserById.groovy
Contract.make {
    description "Gibt einen User zurück, wenn die ID existiert"

    request {
        method GET()
        url "/api/users/1"
        headers {
            accept(applicationJson())
            header("X-Tenant-Id", "tenant-test")
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            id: 1,
            name: "Max Mustermann",
            email: "max@example.com"
        ])
    }
}
```

Spring Cloud Contract erzeugt aus solchen Contracts:

1. **Provider-Tests** (JUnit 5), die in der Provider-CI laufen.
2. **Consumer-Stubs** (WireMock-Format), die als JAR-Artefakt in Maven/Nexus veröffentlicht werden.

### 12.2 Stub Runner: Consumer testet gegen veröffentlichte Stubs

Der Kernmechanismus, der SCC zu mehr macht als einer Provider-Test-DSL:

```java
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.example:user-service:+:stubs:8080",      // ✅ holt aktuellen Stub
    stubsMode = StubRunnerProperties.StubsMode.REMOTE,  // aus Maven-Repository
    repositoryRoot = "${nexus.url}"
)
class OrderServiceClientTest {

    @Autowired
    private UserServiceClient userClient;

    @Test
    void findsUserById_returnsExpectedFields() {
        // ✅ läuft gegen automatisch generierten Stub aus Provider-Repo
        var user = userClient.findById(1L, "tenant-test");
        assertThat(user.email()).isEqualTo("max@example.com");
    }
}
```

**Modi:**

- `LOCAL` — Stubs aus lokalem Maven-Repo (für Monorepo-Setups).
- `REMOTE` — Stubs aus Nexus/Artifactory (Standard für Multi-Repo-Setups).
- `CLASSPATH` — Stubs als Test-Dependency.

### 12.3 Gute Einsatzfälle für Spring Cloud Contract

- Alle Teams nutzen Spring Boot oder JVM.
- Provider-Teams besitzen die API-Spezifikation stark.
- Contracts sollen im Provider-Repository versioniert werden.
- Stub-Artefakte sollen über Maven/Nexus/Artifactory verteilt werden.
- Messaging- oder HTTP-Contracts sollen im Spring-Ökosystem getestet werden.
- Monorepo-Setup, in dem Broker-Infrastruktur unverhältnismäßig aufwändig wäre.

### 12.4 Grenzen

Spring Cloud Contract ist weniger geeignet, wenn:

- Consumer polyglott sind (Go, Python, .NET, JavaScript).
- Consumer selbst Contracts schreiben und veröffentlichen sollen (consumer-driven).
- Ein zentrales Broker-Modell mit Deployment-Matrix benötigt wird.
- Viele unabhängige Consumer existieren.
- Teams nicht alle im Spring-Ökosystem arbeiten.

---

## 13. Bidirectional Contracts (Pact V4)

Pact V4 / PactFlow unterstützt seit 2022 **Bidirectional Contracts**: Provider veröffentlicht ein OpenAPI-Schema, Consumer veröffentlichen Pact-Contracts. Der Broker matcht beide automatisch.

### 13.1 Wann Bidirectional Contracts sinnvoll sind

Bidirectional löst den Trade-off zwischen Pact (Consumer-driven) und SCC (Provider-driven) für drei häufige Szenarien:

1. **Provider arbeitet OpenAPI-First.** Das Provider-Team pflegt sowieso ein OpenAPI-Schema für externe Dokumentation. Statt zusätzlich Provider-Verifikation gegen Pacts zu betreiben, wird das OpenAPI-Schema selbst als Provider-Contract genutzt.

2. **Provider hat zu viele Consumer.** Bei 10+ Consumern wird die klassische Provider-Verifikation langsam. Bidirectional verschiebt die Last: Provider veröffentlicht einmal OpenAPI, Consumer veröffentlichen Pacts, der Broker macht das Matching ohne Provider-Test-Run.

3. **Externe Provider.** Wenn der Provider außerhalb des eigenen Teams liegt und keine Pact-Verifikation durchführt, aber OpenAPI bereitstellt, ist Bidirectional die einzige praktische Möglichkeit für Contract Testing.

### 13.2 Workflow

```
Provider-Seite:
  1. OpenAPI-Schema generieren oder pflegen
  2. Schema-Validierung gegen echte API (z. B. via Schemathesis, Dredd, oder OpenAPI-Validator)
  3. OpenAPI im Broker veröffentlichen mit pacticipant version

Consumer-Seite:
  1. Pact-Test schreiben (wie in Sektion 8.3)
  2. Pact im Broker veröffentlichen

Broker:
  1. Matcht Consumer-Pact gegen Provider-OpenAPI
  2. Findet Vertragsverletzungen ohne Provider-Test-Run
  3. can-i-deploy berücksichtigt Bidirectional Match
```

### 13.3 Grenzen von Bidirectional

- Erfordert PactFlow oder Pact Broker mit Bidirectional-Support.
- OpenAPI-Schema muss **aktiv gegen die echte API verifiziert** werden — sonst beweist der Match nichts.
- Subtile Pact-Features wie Provider States werden anders behandelt.
- Messaging Contracts sind aktuell nicht Teil des Bidirectional-Modells.

Bidirectional ist nicht universell besser. Es ist eine **dritte Option** neben Pact und SCC, die für bestimmte organisatorische Setups die richtige Wahl ist.

---

## 14. Messaging Contracts

In Event-Driven-Architekturen — Kafka, RabbitMQ, SQS, Google Pub/Sub — sind Contracts genauso wichtig wie bei synchronen REST-APIs. Pact JVM unterstützt Messaging Contracts seit Version 3.

### 14.1 Consumer-Test für asynchrone Nachrichten

```java
import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "order-events", providerType = ProviderType.ASYNCH)
class BillingServiceConsumerTest {

    @Pact(consumer = "billing-service")
    MessagePact orderCreatedEvent(MessagePactBuilder builder) {
        return builder
            .given("a new order is placed in tenant tenant-test")
            .expectsToReceive("OrderCreatedEvent")
            .withMetadata(Map.of(
                "contentType", "application/json",
                "tenantId", "tenant-test"
            ))
            .withContent(newJsonBody(body -> {
                body.uuid("eventId");
                body.stringType("eventType", "OrderCreatedEvent");
                body.integerType("orderId");
                body.stringType("status", "CREATED");
                body.decimalType("amount", new BigDecimal("99.99"));
                body.stringType("currency", "EUR");
                body.timestamp("createdAt");
            }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "orderCreatedEvent")
    void consumesOrderCreatedEvent_createsBillingEntry(List<Message> messages) {
        var rawMessage = messages.get(0).contentsAsString();

        // Hier wird der echte Consumer-Code aufgerufen
        var event = orderEventDeserializer.deserialize(rawMessage);
        billingService.handle(event);

        assertThat(billingRepository.findByOrderId(event.orderId()))
            .isPresent();
    }
}
```

### 14.2 Provider-Test für Messaging

```java
import au.com.dius.pact.provider.junitsupport.PactVerifyProvider;

@Provider("order-events")
@PactBroker(
    url = "${PACT_BROKER_BASE_URL}",
    consumerVersionSelectors = {
        @VersionSelector(mainBranch = "true"),
        @VersionSelector(deployedOrReleased = "true")
    }
)
class OrderEventsProviderContractTest {

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyMessage(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactVerifyProvider("OrderCreatedEvent")
    String orderCreatedEvent() {
        // ✅ Echte Event-Erzeugung, nicht hartkodiertes JSON
        var order = orderFactory.createTestOrder();
        var event = orderEventPublisher.toMessage(order);
        return objectMapper.writeValueAsString(event);
    }
}
```

### 14.3 Pflichtdisziplinen für Messaging Contracts

- **Schema-Felder im Event** sind Vertragsbestandteil. Hinzufügen ist meistens kompatibel, Umbenennen oder Entfernen bricht Consumer.
- **Header und Metadaten** (Tenant-ID, Correlation-ID, Event-Version) gehören in den Contract.
- **Idempotency-Key** im Event ist Pflicht — Consumer können denselben Event mehrfach erhalten.
- **Schema-Versionierung** über `eventType` und ggf. `schemaVersion`-Feld.
- **Tenant-Isolation** im Provider-State und Event.

---

## 15. Pact oder Spring Cloud Contract?

| Aspekt | Pact | Spring Cloud Contract | Bidirectional Contracts | Empfehlung |
| --- | --- | --- | --- | --- |
| Consumer-driven | sehr stark | möglich, aber häufig provider-näher genutzt | ja (Consumer-Seite) | Pact bei echten Consumer-Erwartungen |
| Polyglott | stark | primär JVM/Spring | ja | Pact oder Bidirectional bei heterogenen Teams |
| Provider-OpenAPI-First | nicht ideal | nein | sehr gut | Bidirectional |
| Stub-Verteilung | Pact Mockserver aus Pacts | Maven/Nexus Stub-JARs | Pact Mockserver | Pact für unabhängige Consumer; SCC für Maven-Workflow |
| Broker-Matrix | Pact Broker / PactFlow | nicht Kernmodell | PactFlow | Pact bei unabhängigen Deployments |
| Spring-Integration | gut | sehr gut | gut | SCC bei reinem Spring-Stack |
| Messaging | sehr gut (`MessagePact`) | gut (Stream/Messaging-Module) | nicht unterstützt | Pact oder SCC |
| Async-Architekturen | sehr gut | gut | nein | Pact |
| Kulturmodell | Consumer beschreibt Erwartung | Provider oder gemeinsames Contract-Repo | Provider OpenAPI + Consumer Pact | Teamstruktur entscheidet |
| Betrieb | Broker oder PactFlow nötig | Build-/Artifact-Infrastruktur reicht oft | PactFlow oder kompatibler Broker | SCC einfacher im Monorepo |
| `can-i-deploy` | reifer Standard | muss anders abgebildet werden | unterstützt | Pact und Bidirectional für Deployment-Gates |
| Externe Provider | schwierig ohne Provider-Mitarbeit | nein | ja, wenn OpenAPI verfügbar | Bidirectional |

**Daumenregel:**

- **Polyglotte Microservice-Landschaft mit Consumer-driven Kultur und Broker-Infrastruktur:** Pact.
- **Monorepo oder reiner Spring-Stack mit Provider-driven API-Pflege:** Spring Cloud Contract.
- **Provider-OpenAPI-First, viele Consumer, geringe Provider-Test-Kapazität:** Bidirectional Contracts.

---

## 16. Anti-Patterns

### 16.1 Contract als vollständiger DTO-Snapshot

Ein Contract darf nicht alle Felder einer Provider-Response spiegeln, nur weil sie vorhanden sind. Das erzeugt Scheinkontrolle und unnötige Kopplung. Provider können das DTO nicht mehr evolvieren, ohne Consumer zu brechen, obwohl die zusätzlichen Felder gar nicht genutzt werden.

### 16.2 Consumer testet gegen echten Provider

Consumer-Contract-Tests laufen gegen einen Pact-Mockserver oder generierte SCC-Stubs, nicht gegen einen laufenden Provider. Sonst entstehen langsame Integrationstests mit unklarem Fehlerbild.

### 16.3 Provider States als Demo-Datenbank

Provider States dürfen keine globale Demo-Datenbank voraussetzen. Jeder State muss reproduzierbar und isoliert sein — Testcontainers oder transaktionale Rollbacks.

### 16.4 `can-i-deploy` nur als Dashboard

Ein grünes Dashboard ist nutzlos, wenn es Deployment nicht beeinflusst. `can-i-deploy` gehört als Gate in die Pipeline.

### 16.5 Contracts mit produktiven Daten

Contracts dürfen keine echten E-Mail-Adressen, Tokens, Kundennamen, IBANs, Telefonnummern oder internen IDs aus Produktion enthalten — auch nicht in Beispielwerten.

### 16.6 Contracts für Berechtigungslogik missverstehen

Ein Contract kann prüfen, dass `403` bei fehlender Berechtigung zurückkommt. Er ersetzt aber nicht die fachliche Autorisierungsprüfung und keine Security-Tests.

### 16.7 Zu viele Provider-Details im Consumer-Contract

Consumer dürfen nicht interne Statusfelder, Debug-Felder oder Felder aus anderen Use Cases in ihren Contract aufnehmen.

### 16.8 Keine Versionierung mit Branches

Contracts ohne saubere Consumer-/Provider-Versionen mit Branch-Information verlieren ihren Wert. Ohne Branch-Versionierung kann der Broker keine belastbare `mainBranch`/`matchingBranch`-Aussage treffen.

### 16.9 Provider States mit Side Effects in Production-DB

Provider States, die echte Daten persistieren ohne Rollback oder Container-Isolation, hinterlassen Müll in Test-Datenbanken. Klassisches Symptom: Test funktioniert beim ersten Mal, scheitert beim zweiten Mal mit Constraint-Violation.

### 16.10 `pactVerify` ohne `publishVerificationResults`

Provider verifiziert lokal grün, aber der Broker erfährt davon nichts. `can-i-deploy` blockiert das Deployment, weil keine Verifikation registriert wurde. Engineers debuggen stundenlang, warum der Broker nichts anzeigt.

### 16.11 `@PactBroker` ohne `consumerVersionSelectors`

Provider verifiziert nur `latest`-Tag-Pacts. Verpasst Production-Pacts. Bricht Consumer in Production, obwohl die Provider-Pipeline grün war.

### 16.12 Fester Port im Consumer-Test

`@PactTestFor(port = "8080")` führt zu Port-Konflikten in CI mit parallelen Test-Runs oder bei GitHub-Actions-Runnern, deren Port 8080 belegt ist.

---

## 17. Security- und SaaS-Aspekte

Contract Testing ist kein Security-Framework, aber API-Verträge haben direkte Sicherheitswirkung.

### 17.1 Datenminimierung

Consumer-Contracts sollen nur die Felder enthalten, die der Consumer benötigt. Dadurch wird übermäßige Datenexposition sichtbar.

```json
// ❌ Schlecht: Provider gibt mehr preis als nötig
{
  "id": 1,
  "email": "max@example.com",
  "phone": "+49123456789",
  "iban": "DE89370400440532013000",
  "internalRiskScore": 91
}

// ✅ Gut: Minimal-Vertrag
{
  "id": 1,
  "email": "max@example.com"
}
```

Wenn ein Consumer-Contract zeigt, dass `iban` oder `internalRiskScore` als Erwartung enthalten ist, ist das ein Code-Smell — der Consumer braucht sie wahrscheinlich nicht. Mass-Datenexposition wird im Contract sichtbar (vergleiche QG-JAVA-006 v2 Sektion 14).

### 17.2 Mandantentrennung in Contracts

In SaaS-Systemen müssen Contracts Tenant-Kontext sichtbar machen, wenn APIs tenantbezogen arbeiten. Das gilt für **alle** Beispiele in diesem Dokument:

```java
.headers(Map.of(
    "Accept", "application/json",
    "X-Tenant-Id", "tenant-test"
))
```

Provider-Verifikation muss sicherstellen, dass Provider States tenantbezogen sind und nicht versehentlich Daten anderer Mandanten zurückliefern (siehe Sektion 10.1).

### 17.3 Fehlerantworten ohne Information Disclosure

Contracts für Fehlerfälle dürfen keine Stacktraces, SQL-Fehler, interne Klassenpfade oder Secrets erwarten.

```json
// ✅ Gutes Fehlerobjekt (ProblemDetail nach RFC 9457)
{
  "type": "https://api.example.com/problems/user-not-found",
  "title": "User not found",
  "status": 404,
  "detail": "The requested user does not exist",
  "correlationId": "7f9c2d"
}

// ❌ Schlechtes Fehlerobjekt
{
  "exception": "org.hibernate.LazyInitializationException",
  "sql": "select * from users where email='max@example.com'",
  "stackTrace": "..."
}
```

Diese Disziplin verbindet Contract Testing direkt mit QG-JAVA-006 v2 Sektion 13.2 (API-Fehler-Mapping).

### 17.4 Authentifizierung und Autorisierung

Contracts können erwartete Statuscodes und Header abbilden, ersetzen aber keine Security-Tests.

Pflichtfälle für kritische APIs:

- **Fehlendes Token** → `401`.
- **Ungültiges Token** → `401`.
- **Gültiges Token ohne Berechtigung** → `403`.
- **Zugriff auf fremde Tenant-Ressource** → `403` oder `404`, je nach Security-Design (Sektion 14.5 in QG-JAVA-006 v2 — User Enumeration).
- **Ungültiger Tenant-Kontext** → definierter Fehlerstatus.

### 17.5 Broker-Sicherheit

Pact Broker, PactFlow oder Stub-Repositories enthalten API-Strukturen. Sie können interne Pfade, Datenmodelle, Fehlercodes und Beispielwerte enthalten. Zugriff muss deshalb rollenbasiert, authentifiziert und auditierbar sein.

---

## 18. Contract Testing, OpenAPI und GraphQL

### 18.1 OpenAPI vs. Pact

OpenAPI und Pact lösen unterschiedliche Probleme.

| Aspekt | OpenAPI | Contract Testing (Pact/SCC) |
| --- | --- | --- |
| Ziel | API beschreiben und dokumentieren | API-Erwartung zwischen Consumer und Provider verifizieren |
| Perspektive | häufig provider-zentriert | häufig consumer-zentriert (Pact) oder provider-shared (SCC) |
| Nutzung | Dokumentation, Client-Generierung, Schema-Validierung | CI/CD-Kompatibilität |
| Stärke | öffentliche API-Beschreibung | konkrete Consumer-Erwartungen |
| Grenze | zeigt nicht automatisch, was ein Consumer wirklich nutzt | ersetzt keine vollständige API-Dokumentation |

Für öffentliche oder stark regulierte APIs kann **beides** notwendig sein: OpenAPI als formale API-Beschreibung und Pact/SCC als automatisierte Kompatibilitätsprüfung.

### 18.2 Bidirectional Contracts als Brücke

Bidirectional Contracts (Sektion 13) verbinden OpenAPI und Pact: Provider veröffentlicht OpenAPI, Consumer veröffentlichen Pacts, der Broker matcht beides. Das ist die moderne Antwort auf den Trade-off „OpenAPI oder Pact?" — es kann beides sein.

### 18.3 Contract Testing für GraphQL

Pact JVM 4.5+ unterstützt GraphQL-Queries. Der Contract beschreibt Query, Variablen und erwartete Response-Struktur:

```java
@Pact(consumer = "frontend")
RequestResponsePact getUserGraphQL(PactDslWithProvider builder) {
    return builder
        .given("User with ID 1 exists in tenant tenant-test")
        .uponReceiving("GraphQL query for user")
            .method("POST")
            .path("/graphql")
            .headers("Content-Type", "application/json",
                     "X-Tenant-Id", "tenant-test")
            .body(newJsonBody(body -> {
                body.stringType("query",
                    "query GetUser($id: ID!) { user(id: $id) { id name email } }");
                body.object("variables", vars -> vars.stringType("id", "1"));
            }).build())
        .willRespondWith()
            .status(200)
            .body(newJsonBody(body -> {
                body.object("data", data -> {
                    data.object("user", user -> {
                        user.stringType("id", "1");
                        user.stringType("name", "Max Mustermann");
                        user.stringType("email", "max@example.com");
                    });
                });
            }).build())
        .toPact();
}
```

GraphQL hat zusätzliche Eigenheiten (Schema-Stitching, Federation, Subscriptions), die in einer eigenständigen Erweiterung dieser Guideline behandelt werden müssten.

---

## 19. Teststrategie

Contract Tests stehen zwischen Unit Tests und End-to-End-Tests:

```
Unit Tests
  Schnell, isoliert, interne Logik

Component Tests (Service-Layer-Tests aus QG-JAVA-006)
  Service mit Mocks, ohne Spring-Kontext

Contract Tests
  API-Kompatibilität Consumer ↔ Provider
  über Pact Mockserver (Consumer) und echte API (Provider)

Integration Tests
  Provider mit Datenbank, Messaging, Infrastruktur
  Testcontainers, vollständiger Spring-Kontext

End-to-End-Tests
  wenige kritische Geschäftsflüsse über mehrere Services
```

Contract Tests reduzieren die Anzahl notwendiger E2E-Tests, ersetzen sie aber nicht vollständig. Geschäftsprozesse, die mehrere Services orchestrieren, brauchen weiterhin E2E-Tests — aber **deutlich weniger** als ohne Contract Tests.

---

## 20. CI/CD-Standard

### 20.1 Consumer Pipeline

```yaml
name: consumer-contract

on:
  push:
    branches: [main, "feature/**"]

env:
  PACT_BROKER_BASE_URL: ${{ secrets.PACT_BROKER_BASE_URL }}
  PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}

jobs:
  contract:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Run consumer tests
        run: ./gradlew test

      - name: Publish Pact contracts with branch
        env:
          pact_consumer_version: ${{ github.sha }}
          pact_consumer_version_branch: ${{ github.ref_name }}
        run: ./gradlew pactPublish

      - name: Check deployability for staging
        run: |
          docker run --rm \
            -e PACT_BROKER_BASE_URL \
            -e PACT_BROKER_TOKEN \
            pactfoundation/pact-cli:latest \
            broker can-i-deploy \
              --pacticipant order-service \
              --version ${{ github.sha }} \
              --to-environment staging
```

### 20.2 Provider Pipeline

```yaml
name: provider-contract

on:
  push:
    branches: [main, "feature/**"]

env:
  PACT_BROKER_BASE_URL: ${{ secrets.PACT_BROKER_BASE_URL }}
  PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}

jobs:
  verify-contracts:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Verify provider contracts and publish results
        env:
          pact_provider_version: ${{ github.sha }}
          pact_provider_version_branch: ${{ github.ref_name }}
          pact_publish_verification_results: "true"      # ✅ Pflicht
        run: ./gradlew pactVerify

      - name: Check deployability for staging
        run: |
          docker run --rm \
            -e PACT_BROKER_BASE_URL \
            -e PACT_BROKER_TOKEN \
            pactfoundation/pact-cli:latest \
            broker can-i-deploy \
              --pacticipant user-service \
              --version ${{ github.sha }} \
              --to-environment staging

  deploy-staging:
    needs: verify-contracts
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to staging
        run: ./deploy-to-staging.sh

      - name: Record deployment in Pact Broker
        run: |
          docker run --rm \
            -e PACT_BROKER_BASE_URL \
            -e PACT_BROKER_TOKEN \
            pactfoundation/pact-cli:latest \
            broker record-deployment \
              --pacticipant user-service \
              --version ${{ github.sha }} \
              --environment staging
```

### 20.3 Initialer Setup-Schritt: Environments anlegen

Einmalig pro Broker-Instanz:

```bash
pact-broker create-environment --name staging --display-name "Staging"
pact-broker create-environment --name production --display-name "Production" --production
```

Die konkreten Tasks und CLI-Befehle hängen von Gradle-/Maven-Plugin, Broker-Version und Organisationsstandard ab. Entscheidend ist die Regel: **Veröffentlichen mit Branch, Verifizieren mit `publishResults`, Deployment-Gate via `can-i-deploy --to-environment`, Deployment-Status via `record-deployment` registrieren.**

---

## 21. Performance- und Wartungsaspekte

### 21.1 Performance bei vielen Consumern

Bei Providern mit 5+ Consumern wächst die Verifikationszeit linear. Strategien:

- **`consumerVersionSelectors` mit Filterung.** Nicht jeder Branch jedes Consumers muss verifiziert werden. `mainBranch=true` plus `deployedOrReleased=true` reicht meist.
- **Parallele Test-Ausführung.** Pact JVM unterstützt JUnit-5-parallele Ausführung (`junit.jupiter.execution.parallel.enabled=true`). Pacts sind unabhängig, können also parallel verifiziert werden.
- **`MockMvcTestTarget`** statt `HttpTestTarget` (Sektion 10.3) bei vielen Pacts.
- **Provider-State-Caching.** Wenn mehrere Pacts denselben State benötigen, kann der State-Aufbau gecached werden. Vorsicht bei Test-Isolation.

### 21.2 Pact-Pflege als organisatorischer Prozess

Ein Pact-Broker, der nicht aktiv gepflegt wird, sammelt Müll an: alte Pacts toter Branches, deprecated Consumer, längst abgelöste Provider-Versionen. Konsequenz: `consumerVersionSelectors` werden langsam, `can-i-deploy` antwortet auf veraltete Daten.

**Pflichtdisziplinen:**

- **Branch-Cleanup:** Pacts, deren Branch im Git nicht mehr existiert, werden aus dem Broker entfernt (`pact-broker delete-branch` oder regelmäßiger Cleanup-Job).
- **Pacticipant-Deprecation:** Wenn ein Consumer dauerhaft eingestellt wird, wird der Pacticipant im Broker markiert oder gelöscht.
- **Version-Garbage-Collection:** Pacticipant-Versionen, die älter als N Monate sind und in keinem Environment registriert sind, können entfernt werden.
- **Monitoring auf Verifikationszeit:** Wenn die Provider-Verifikation eine Schwelle überschreitet (z. B. 5 Minuten), muss reagiert werden.

### 21.3 Ownership und Verantwortung

Wer pflegt den Pact Broker? Wer ist für die Pact-Disziplin in einem Team verantwortlich? Diese Fragen müssen organisatorisch geklärt sein, sonst verkommt Contract Testing zum „funktioniert nicht mehr seit drei Monaten"-Problem.

Empfehlungen:

- **Platform-Team** betreibt den Broker.
- **Service-Team** ist für die Pact-Disziplin seines eigenen Service verantwortlich.
- **Architecture-Board** definiert Branches, Environments und `consumerVersionSelectors`-Standards.
- **Deprecation-Reviews** alle 6 Monate.

---

## 22. Review-Checkliste

Im Pull Request sind folgende Fragen zu prüfen. Jede Zeile verweist auf die Detail-Sektion mit der Begründung.

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| Notwendigkeit | Erfordert die API laut Sektion 7 Contract Testing? | §7 |
| Tool-Wahl | Ist die Tool-Wahl (Pact/SCC/Bidirectional) für den Kontext begründet? | §15 |
| Consumer-Minimalität | Beschreibt der Contract nur tatsächlich genutzte Felder? | §9.1 |
| Type-Matcher | Werden Type-Matcher statt Wert-Matcher verwendet? | §9.2 |
| Fehlerfälle | Sind 401, 403, 404, 409 abgedeckt? | §9.3 |
| Modern API | Wird `LambdaDsl.newJsonBody` statt `PactDslJsonBody` genutzt? | §3.3.1, §8.3 |
| Random Port | Ist `@PactTestFor` ohne festen Port? | §3.2.7, §8.3 |
| `consumerVersionSelectors` | Ist `@PactBroker` mit Selectors konfiguriert? | §3.1.4, §10.1 |
| Provider States idempotent | Sind alle States bei wiederholter Ausführung sicher? | §3.1.6, §10.2 |
| Provider States transaktional | Wird `@Transactional` oder Testcontainers verwendet? | §3.1.8, §10.1 |
| Provider States tenant-aware | Ist Tenant-Kontext in States gesetzt? | §3.1.7, §10.1 |
| `publishResults` | Wird `pact.verifier.publishResults=true` gesetzt? | §3.1.5, §20.2 |
| Branch-Versionierung | Sind Pacticipant-Branches gesetzt? | §3.1.16, §20.1 |
| `record-deployment` | Wird Deployment im Broker registriert? | §3.1.12, §20.2 |
| `can-i-deploy` Gate | Ist `can-i-deploy --to-environment` als Gate konfiguriert? | §3.1.11, §11.4 |
| Keine Production-Daten | Enthält der Contract keine Secrets oder Produktions-PII? | §3.1.9, §16.5 |
| Tenant-Header | Wird Tenant-Kontext modelliert (SaaS)? | §17.2 |
| Datenminimierung | Werden nicht zu viele Felder erwartet? | §17.1 |
| Fehler-Format | Ist Fehler-Response RFC 9457 konform? | §17.3 |
| Messaging | Sind Messaging-Contracts mit `MessagePact` modelliert? | §14 |
| Performance | Bei 5+ Consumern: Strategien angewandt? | §21.1 |
| ArchUnit | Sind Tests im richtigen Package? | §23 |

---

## 23. Automatisierbare Prüfungen

### 23.1 ArchUnit-Regeln

```java
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ContractTestArchitectureTest {

    // ✅ Pact-Consumer-Tests müssen im richtigen Package liegen
    @ArchTest
    static final ArchRule consumer_contract_tests_in_contracts_package =
        classes()
            .that().areAnnotatedWith("au.com.dius.pact.consumer.junit5.PactConsumerTestExt")
            .or().haveSimpleNameEndingWith("ContractTest")
            .should().resideInAPackage("..contracts..");

    // ✅ Provider-Tests müssen Testcontainers verwenden
    @ArchTest
    static final ArchRule provider_tests_use_testcontainers =
        classes()
            .that().areAnnotatedWith("au.com.dius.pact.provider.junitsupport.Provider")
            .should().beAnnotatedWith("org.testcontainers.junit.jupiter.Testcontainers");

    // ✅ Consumer-Contract-Test darf keine echten HTTP-Clients als Test-Dependency haben
    @ArchTest
    static final ArchRule consumer_contract_no_real_http_client =
        noClasses()
            .that().resideInAPackage("..contracts.consumer..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("org.springframework.web.client.RestTemplate")
            .as("Consumer-Contract-Tests müssen gegen Pact MockServer laufen");

    // ✅ Provider States müssen @Transactional sein
    @ArchTest
    static final ArchRule provider_states_must_be_transactional =
        methods()
            .that().areAnnotatedWith("au.com.dius.pact.provider.junitsupport.State")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .as("Provider States müssen transaktional isoliert sein");

    // ✅ Pact Mockserver darf nicht in normalen Service-Tests verwendet werden
    @ArchTest
    static final ArchRule pact_mockserver_only_in_contract_tests =
        classes()
            .that().dependOnClassesThat().haveFullyQualifiedName("au.com.dius.pact.consumer.MockServer")
            .should().resideInAPackage("..contracts..");
}
```

### 23.2 Static-Analysis-Regeln (Semgrep, PMD, Custom)

```yaml
# semgrep-rules.yml
rules:
  - id: pact-no-fixed-port
    message: "@PactTestFor ohne festen Port verwenden — Random Port wird automatisch gewählt"
    severity: ERROR
    languages: [java]
    pattern: |
      @PactTestFor(... port = $PORT, ...)

  - id: pact-no-pactdsl-json-body
    message: "PactDslJsonBody ist veraltet — verwende LambdaDsl.newJsonBody(...)"
    severity: WARNING
    languages: [java]
    pattern: new PactDslJsonBody()

  - id: pact-broker-must-have-selectors
    message: "@PactBroker ohne consumerVersionSelectors verifiziert nur 'latest'"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: |
          @PactBroker(...)
          $CLASS
      - pattern-not-inside: |
          @PactBroker(... consumerVersionSelectors = {...}, ...)
          $CLASS

  - id: contract-no-production-emails
    message: "Contracts dürfen keine produktiven E-Mail-Adressen enthalten"
    severity: ERROR
    languages: [java, generic]
    pattern-regex: '"\w+@(?!example\.(com|org|net))\w+\.\w+"'
```

### 23.3 CI-Gates

Für produktionsnahe Codebasen sollten folgende Gates existieren:

1. Pact Consumer Tests laufen in Consumer-Pipeline.
2. Pact Provider Verification läuft in Provider-Pipeline mit `publishResults`.
3. `can-i-deploy --to-environment` blockiert Deployment bei Vertragsverletzung.
4. `record-deployment` läuft nach erfolgreichem Deployment.
5. ArchUnit-Regeln für Test-Package-Struktur.
6. Semgrep- oder Custom-Static-Analysis für Pact-Anti-Patterns.
7. Secret-Scan in Contracts (keine echten Tokens, IBANs, Telefonnummern).
8. Performance-Gate bei Provider-Verifikation > 5 Minuten.

---

## 24. Migration bestehender Tests

Bestehende Service-zu-Service-Tests werden schrittweise migriert.

### 24.1 Schritt 1: Kritische Interaktionen identifizieren

Beginne nicht mit allen APIs. Starte mit:

- Payment-APIs.
- Order-APIs.
- Identity- und Auth-APIs.
- Customer-APIs.
- Billing-APIs.
- Tenant-/Entitlement-APIs.

### 24.2 Schritt 2: Consumer-Client isolieren

Consumer-Contract-Tests sollten den API-Client testen, nicht den kompletten Consumer-Service.

### 24.3 Schritt 3: Minimalen Contract schreiben

Nur die Felder beschreiben, die der Consumer benötigt. Mit `LambdaDsl.newJsonBody`. Mit Tenant-Header. Ohne festen Port.

### 24.4 Schritt 4: Provider-Verifikation mit Selectors

Provider muss den Contract gegen echte Controller/API-Schicht verifizieren — mit `consumerVersionSelectors`, mit Testcontainers, mit idempotenten States, mit `publishVerificationResults=true`.

### 24.5 Schritt 5: Broker- und Gate-Integration

- Pacts veröffentlichen mit Branch.
- Verifikation mit Branch und Publish.
- `can-i-deploy --to-environment` als Gate.
- `record-deployment` nach erfolgreichem Deployment.
- Environments einmalig anlegen.

### 24.6 Schritt 6: E2E-Tests reduzieren

Erst wenn Contract Tests stabil sind, werden redundante E2E-Tests reduziert. E2E-Tests bleiben für echte Geschäftsprozess-Validierung — aber nicht mehr für API-Format-Prüfung.

### 24.7 Schritt 7: Wartung etablieren

Branch-Cleanup, Pacticipant-Deprecation, Performance-Monitoring (Sektion 21.2).

---

## 25. Ausnahmen

Contract Testing kann entfallen, wenn:

1. Consumer und Provider im selben Deployable leben.
2. Keine Netzwerk-/API-Grenze existiert.
3. Die API nicht unabhängig versioniert wird.
4. Der Aufruf rein intern und nicht serviceübergreifend ist.
5. OpenAPI-/Schema-Validation plus Integrationstest fachlich ausreichend und begründet ist.
6. Eine externe API keine Contract-Mitwirkung erlaubt und ein anderer stabiler Testansatz verwendet wird.

Die Ausnahme MUSS im Pull Request oder technischen Dokument begründet werden.

Nicht zulässig als Begründung:

1. „Pact ist kompliziert."
2. „Wir haben E2E-Tests."
3. „Der andere Service ist auch in unserem Team."
4. „Wir haben WireMock."
5. „can-i-deploy ist optional."
6. „Wir machen das nächstes Quartal."

---

## 26. Definition of Done

Eine Service-zu-Service-API erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Die Notwendigkeit von Contract Testing ist gegen Sektion 7 geprüft.
2. Die Tool-Wahl (Pact, SCC, Bidirectional) ist begründet.
3. Consumer-Contracts sind minimal und beschreiben nur genutzte Felder.
4. Type-Matcher werden statt Wert-Matcher verwendet, außer bei fachlich relevanten Konstanten.
5. Fehlerfälle (401, 403, 404, 409) sind als Contract abgedeckt.
6. `LambdaDsl.newJsonBody(...)` wird als Default-API verwendet.
7. `@PactTestFor` verwendet keinen festen Port.
8. `@PactBroker` ist mit `consumerVersionSelectors` konfiguriert (`mainBranch`, `deployedOrReleased`, `matchingBranch`).
9. Provider States sind idempotent.
10. Provider States sind transaktional (`@Transactional`) oder durch Testcontainers isoliert.
11. Provider States sind in mandantenfähigen Systemen explizit tenant-aware.
12. Provider-Verifikation läuft mit `pact.verifier.publishResults=true`.
13. Consumer veröffentlichen Pacts mit Branch (`pact_consumer_version_branch`).
14. Provider veröffentlichen Verifikationsergebnisse mit Branch (`pact_provider_version_branch`).
15. Environments sind im Broker angelegt.
16. `can-i-deploy --to-environment` ist als Pflicht-Gate in der Pipeline.
17. `record-deployment` läuft nach erfolgreichem Deployment.
18. Contracts enthalten keine produktiven personenbezogenen Daten oder Secrets.
19. Tenant-Header sind in mandantenfähigen Contracts modelliert.
20. Fehler-Responses folgen RFC 9457 (ProblemDetail).
21. Messaging-Contracts mit `MessagePact` sind für asynchrone Schnittstellen vorhanden.
22. ArchUnit-Regeln für Test-Package-Struktur sind aktiv.
23. Performance-Strategien sind bei 5+ Consumern angewandt.
24. Pact-Pflege (Branch-Cleanup, Deprecation) ist organisatorisch geregelt.
25. Pull-Request-Review berücksichtigt die Checkliste aus Sektion 22.

---

## 27. Quellen und weiterführende Literatur

* Pact Documentation — Consumer tests: <https://docs.pact.io/consumer>
* Pact JVM Consumer JUnit 5: <https://docs.pact.io/implementation_guides/jvm/consumer/junit5>
* Pact JVM Provider JUnit 5: <https://docs.pact.io/implementation_guides/jvm/provider/junit5>
* Pact JVM Spring/JUnit 5 Support: <https://docs.pact.io/implementation_guides/jvm/provider/junit5spring>
* Pact JVM Provider States: <https://docs.pact.io/implementation_guides/jvm/provider/junit5#provider-states>
* Pact Broker: <https://docs.pact.io/pact_broker>
* Pact Broker can-i-deploy: <https://docs.pact.io/pact_broker/can_i_deploy>
* Pact Broker Versioning Branches: <https://docs.pact.io/pact_broker/branches>
* Pact Broker Environments and record-deployment: <https://docs.pact.io/pact_broker/recording_deployments_and_releases>
* Pact Consumer Version Selectors: <https://docs.pact.io/pact_broker/advanced_topics/consumer_version_selectors>
* Pact LambdaDsl: <https://docs.pact.io/implementation_guides/jvm/consumer#building-json-bodies-with-lambdadsl>
* Pact Messaging (asynchronous messages): <https://docs.pact.io/getting_started/how_pact_works#non-http-testing-message-pact>
* Pact V4 Bidirectional Contracts (PactFlow): <https://pactflow.io/blog/bi-directional-contract-testing/>
* Spring Cloud Contract Reference Documentation: <https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/>
* Spring Cloud Contract Stub Runner: <https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/project-features.html#features-stub-runner>
* Spring Cloud Contract Project Page: <https://spring.io/projects/spring-cloud-contract>
* Spring Cloud Contract Messaging: <https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/project-features.html#features-messaging>
* Martin Fowler — Consumer-Driven Contracts: <https://martinfowler.com/articles/consumerDrivenContracts.html>
* Martin Fowler — Contract Test: <https://martinfowler.com/bliki/ContractTest.html>
* RFC 9457 — Problem Details for HTTP APIs: <https://www.rfc-editor.org/rfc/rfc9457.html>
* OWASP API Security Top 10: <https://owasp.org/API-Security/>
* Testcontainers Documentation: <https://java.testcontainers.org/>
* JUnit 5 Parallel Execution: <https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution>

---

*Diese Richtlinie ersetzt die Erstfassung vom 2026-05-03 (v1.0). Für inhaltliche Querverweise auf Service-Layer-Themen siehe QG-JAVA-006 (Spring-Boot-Serviceschicht: Struktur und Qualität) — insbesondere Sektion 11.6 (Optimistic Locking, 409-Mapping), Sektion 12.4 (`findByIdAndTenantId`), Sektion 13.2 (API-Fehler-Mapping mit ProblemDetail), Sektion 14.5 (User Enumeration) und Sektion 14.6 (Tenant-Context-Pattern).*