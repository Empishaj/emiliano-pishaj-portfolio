# QG-JAVA-019 — Contract Testing mit Pact und Spring Cloud Contract

| Feld | Wert |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-019 |
| Titel | Contract Testing mit Pact und Spring Cloud Contract |
| Status | Accepted / verbindlicher Qualitätsstandard für API-Verträge zwischen Services |
| Version | 1.0.0 |
| Datum | 2026-05-03 |
| Review-Zyklus | Halbjährlich oder bei Änderung von Java-, Spring-Boot-, Pact- oder Spring-Cloud-Contract-Baseline |
| Kategorie | Testing · Microservices · API-Kompatibilität · CI/CD |
| Zielgruppe | Java-Entwickler, Tech Leads, QA, Architektur, DevOps, Platform Engineering, Security |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, JUnit 5.x, Pact JVM 4.x, Spring Cloud Contract passend zur verwendeten Spring-Cloud-Release-Train-Version |
| Verbindlichkeit | Verbindlich für alle Service-zu-Service-Schnittstellen, die unabhängig deployed oder von mehreren Consumern genutzt werden |
| Technischer Prüfstatus | Gegen Pact-, Spring-Cloud-Contract- und Spring-Dokumentation validiert; Codebeispiele sind Guideline-Snippets und müssen im Projekt gegen konkrete Dependency-Versionen kompiliert werden |
| Security-Relevanz | Hoch: API-Verträge beeinflussen Datenminimierung, Zugriffskontext, Fehlerantworten, PII-Leaks, Mandantengrenzen und Rückwärtskompatibilität |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Contract Testing in Java- und Spring-Boot-basierten Microservice-Systemen eingesetzt wird, um API-Kompatibilität zwischen Consumer und Provider frühzeitig, automatisiert und nachvollziehbar zu prüfen.

Contract Testing beantwortet eine konkrete Frage:

> Erfüllt der Provider weiterhin genau die API-Erwartungen, die ein Consumer tatsächlich nutzt?

Damit ersetzt Contract Testing nicht alle Integrationstests, nicht alle End-to-End-Tests und keine Security-Tests. Es reduziert aber das Risiko, dass API-Breaking-Changes erst in Staging, Produktion oder beim echten Consumer auffallen.

---

## 2. Kurzregel

Für Service-zu-Service-Kommunikation werden Contract Tests eingesetzt, wenn Consumer und Provider unabhängig entwickelt, getestet oder deployed werden. Consumer dürfen nicht nur gegen manuell gepflegte WireMock-Stubs testen. Provider müssen veröffentlichte Contracts automatisiert gegen ihre echte API verifizieren. Deployments dürfen nur erfolgen, wenn die relevanten Contracts verifiziert sind.

Für polyglotte Microservice-Landschaften ist Pact der bevorzugte Standard. Für reine Spring-/JVM-Landschaften mit stark providerseitig gesteuertem API-Modell kann Spring Cloud Contract verwendet werden.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- HTTP-APIs zwischen Microservices
- REST-/JSON-Schnittstellen
- synchrone Service-zu-Service-Aufrufe
- interne Plattform-APIs
- externe APIs, sofern Consumer-Erwartungen kontrolliert beschrieben werden können
- Events und Messaging-Schnittstellen, wenn das Contract-Tool die Interaktion unterstützt
- kritische Provider mit mehreren Consumern
- APIs mit unabhängigen Deployments
- APIs mit hohem Rückwärtskompatibilitätsanspruch

Diese Richtlinie gilt nicht als alleinige Teststrategie für:

- vollständige End-to-End-Prozessvalidierung
- Last- und Performance-Tests
- Security-Tests
- Autorisierungstests
- Datenbankmigrationstests
- UI-Verhalten
- fachliche Prozessketten über viele Services hinweg
- exploratives Testen

---

## 4. Technischer Hintergrund

In einer Microservice-Architektur sind Services über Schnittstellen gekoppelt. Diese Kopplung ist nicht automatisch schlecht; sie muss aber explizit gemacht und getestet werden.

Ein Provider kann eine Änderung einführen, die aus seiner Sicht harmlos ist, aber einen Consumer bricht. Beispiele:

- Feld wird umbenannt: `email` → `emailAddress`
- HTTP-Status ändert sich: `404` → `200` mit Fehlerobjekt
- Pflichtfeld verschwindet
- Datentyp ändert sich: `id` von Zahl zu String
- Error-Response-Struktur wird geändert
- Content-Type fehlt
- Array wird zu Objekt
- leere Collection wird zu `null`
- Paging-Felder werden anders benannt
- Consumer erwartet ein Feld, das Provider nicht mehr liefert

End-to-End-Tests können solche Fehler finden, sind aber oft langsam, teuer, fragil und schwer eindeutig zu debuggen. Contract Tests prüfen API-Erwartungen isolierter und schneller.

Contract Testing prüft nicht, ob das gesamte System fachlich korrekt arbeitet. Es prüft, ob Consumer und Provider denselben Vertrag verstehen.

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Consumer | Service, der eine API nutzt | `order-service` ruft `user-service` auf |
| Provider | Service, der eine API bereitstellt | `user-service` liefert User-Daten |
| Contract | Maschinenlesbare Beschreibung einer konkreten API-Erwartung | Pact-Datei, Spring-Cloud-Contract-DSL |
| Interaction | Einzelne Anfrage-Antwort-Erwartung | `GET /api/users/1` liefert `200` mit `id`, `name`, `email` |
| Provider State | Vorbedingung, damit der Provider die erwartete Antwort liefern kann | „User with ID 1 exists“ |
| Pact Broker | Zentrale Ablage für Pact-Contracts und Verifikationsergebnisse | Self-hosted Pact Broker oder PactFlow |
| Stub | Simulierter Provider für Consumer-Tests | generierter WireMock-Stub |
| can-i-deploy | CI/CD-Prüfung, ob eine konkrete Version sicher deployed werden darf | Pact Broker Matrix |

---

## 6. Verbindlicher Standard

Für API-Interaktionen zwischen unabhängig deploybaren Services gilt:

1. Jeder Consumer muss die Provider-Erwartungen als Contract Test beschreiben, wenn ein API-Bruch produktive Auswirkungen hätte.
2. Der Contract muss minimal sein und nur das beschreiben, was der Consumer tatsächlich benötigt.
3. Provider müssen alle relevanten Contracts automatisiert gegen ihre echte Implementierung verifizieren.
4. Contracts dürfen keine produktiven personenbezogenen Daten, Secrets oder Tokens enthalten.
5. Provider States müssen deterministisch, klein und reproduzierbar sein.
6. Contract Tests müssen in CI laufen.
7. Breaking Changes dürfen nicht deployed werden, solange relevante Consumer-Contracts verletzt werden.
8. Contracts müssen versioniert, veröffentlicht und im Deployment-Gate berücksichtigt werden.
9. Contract Tests ersetzen keine Autorisierung, keine Mandantentests und keine Security-Tests.
10. Contract-Verletzungen werden wie Build-Brüche behandelt.

---

## 7. Wann Contract Testing erforderlich ist

Contract Testing ist verpflichtend, wenn mindestens eine der folgenden Bedingungen zutrifft:

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Unabhängiges Deployment | Consumer und Provider werden unabhängig released | `order-service` und `user-service` | Pflicht |
| Mehrere Consumer | Ein Provider hat mehrere Aufrufer | `customer-service` wird von Order, Billing, Support genutzt | Pflicht |
| Kritische API | Fehler führt zu Zahlungsausfall, Bestellabbruch oder Kundendatenproblem | Payment, Identity, Order | Pflicht |
| Externe Integration | Provider oder Consumer liegt außerhalb des Teams | Partner-API, Plattform-API | Pflicht oder formaler Ersatz |
| Hohe Änderungsfrequenz | API verändert sich häufig | neue Checkout-API | Pflicht |
| E2E-Tests zu langsam | API-Kompatibilität soll früher geprüft werden | CI braucht 45 Minuten | Pflicht |
| Nur ein Service intern | Keine unabhängige Versionierung, keine echte Schnittstellenkopplung | interne Klassenmethode | Nicht erforderlich |
| Reiner Datenbankzugriff | Kein API-Vertrag zwischen Services | Repository-Test | Nicht geeignet |

---

## 8. Pact: Consumer-Driven Contract Testing

Pact ist besonders geeignet, wenn Consumer ihre Erwartungen selbst beschreiben sollen. Der Consumer schreibt einen Test für seinen API-Client. Nebenbei erzeugt dieser Test einen maschinenlesbaren Contract, der später vom Provider verifiziert wird.

### 8.1 Grundablauf mit Pact

```text
Consumer-Test
    ↓
Pact-Datei wird erzeugt
    ↓
Pact wird im Broker veröffentlicht
    ↓
Provider lädt Pact aus dem Broker
    ↓
Provider verifiziert Pact gegen echte Implementierung
    ↓
Broker speichert Verifikationsergebnis
    ↓
CI/CD prüft per can-i-deploy, ob Deployment sicher ist
```

### 8.2 Schlechte Anwendung: WireMock ohne gemeinsamen Contract

```java
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

Dieser Test ist nicht wertlos, aber er löst nicht das Contract-Problem. Der Stub lebt nur im Consumer-Projekt. Wenn der Provider später `email` in `emailAddress` umbenennt, bleibt dieser Consumer-Test grün. Genau deshalb braucht es einen Contract, der vom Provider verifiziert wird.

### 8.3 Gute Anwendung: Consumer definiert minimale Erwartung

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "user-service", port = "8080")
class UserServiceClientContractTest {

    @Pact(consumer = "order-service")
    RequestResponsePact getUserById(PactDslWithProvider builder) {
        return builder
                .given("User with ID 1 exists")
                .uponReceiving("GET user by id")
                    .method("GET")
                    .path("/api/users/1")
                    .headers("Accept", "application/json")
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody()
                            .integerType("id", 1)
                            .stringType("name", "Max Mustermann")
                            .stringType("email", "max@example.com"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUserById")
    void findById_parsesUserResponse_whenUserExists(MockServer mockServer) {
        var client = new UserServiceClient(mockServer.getUrl());

        var user = client.findById(1L);

        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.name()).isEqualTo("Max Mustermann");
        assertThat(user.email()).isEqualTo("max@example.com");
    }
}
```

Wichtig ist: Der Consumer beschreibt nicht die vollständige Provider-Antwort, sondern nur die Felder, die er wirklich benötigt. Dadurch bleiben Provider evolvierbar.

---

## 9. Consumer-Contract-Regeln

Consumer-Contracts müssen minimal, fachlich relevant und stabil sein.

### 9.1 Nur genutzte Felder beschreiben

Schlecht:

```java
.body(new PactDslJsonBody()
        .integerType("id", 1)
        .stringType("name", "Max")
        .stringType("email", "max@example.com")
        .stringType("phone", "+49123456789")
        .stringType("createdAt", "2026-01-01T00:00:00Z")
        .stringType("internalStatus", "ACTIVE")
        .stringType("crmId", "CRM-123"))
```

Wenn der Consumer nur `id`, `name` und `email` nutzt, dürfen `phone`, `internalStatus`, `crmId` und ähnliche Felder nicht in den Consumer-Contract. Sonst koppelt sich der Consumer unnötig an Provider-Details.

Gut:

```java
.body(new PactDslJsonBody()
        .integerType("id", 1)
        .stringType("name", "Max")
        .stringType("email", "max@example.com"))
```

### 9.2 Typen statt exakte Werte prüfen

Contracts sollen Struktur und Typ sichern, nicht zufällige Beispielwerte überfixieren.

Schlecht:

```java
.stringValue("email", "max@example.com")
```

Besser:

```java
.stringType("email", "max@example.com")
```

Noch besser bei fachlicher Relevanz:

```java
.stringMatcher(
        "email",
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
        "max@example.com"
)
```

### 9.3 Fehlerfälle testen

Consumer-Contracts müssen nicht nur Happy Paths enthalten.

Beispiele:

- `404`, wenn Ressource fehlt
- `400`, wenn Request ungültig ist
- `401`, wenn Token fehlt
- `403`, wenn Zugriff verboten ist
- `409`, wenn fachlicher Konflikt besteht
- `429`, wenn Rate Limit greift
- `503`, wenn Provider temporär nicht verfügbar ist

---

## 10. Provider-Verifikation mit Pact

Der Provider verifiziert veröffentlichte Contracts gegen seine echte API. Dabei wird nicht der Consumer gestartet. Pact ruft die Provider-Endpunkte mit den im Contract beschriebenen Requests auf und prüft die tatsächliche Response.

```java
@Provider("user-service")
@PactBroker(
        url = "${PACT_BROKER_URL}",
        authentication = @PactBrokerAuth(token = "${PACT_BROKER_TOKEN}")
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceProviderContractTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyContract(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("User with ID 1 exists")
    void userWithId1Exists() {
        userRepository.save(new UserEntity(1L, "Max Mustermann", "max@example.com"));
    }

    @State("User with ID 99 does not exist")
    void userWithId99DoesNotExist() {
        userRepository.deleteById(99L);
    }
}
```

### Provider-State-Regeln

Provider States sind keine Mini-End-to-End-Setups. Sie stellen exakt die Daten her, die für eine Interaction notwendig sind.

| Aspekt | Regel | Beispiel |
|---|---|---|
| Name | Muss exakt zum Consumer-Contract passen | `User with ID 1 exists` |
| Umfang | Nur notwendige Testdaten anlegen | ein User, nicht komplette Demo-Datenbank |
| Determinismus | Keine zufälligen IDs ohne Rückgabe | feste Testdaten |
| Isolation | Jeder State räumt seine Daten auf oder nutzt isolierte Testdatenbank | Testcontainers, Transaktion, Cleanup |
| Sicherheit | Keine produktiven Daten | synthetische Testdaten |
| Performance | State-Aufbau muss schnell bleiben | keine Massendaten |

---

## 11. Pact Broker und Deployment-Gates

Der Pact Broker ist die zentrale Ablage für Contracts und Verifikationsergebnisse. Er beantwortet im Deployment-Prozess die Frage, ob eine konkrete Consumer- oder Provider-Version sicher deployed werden kann.

### 11.1 Broker-Grundregeln

1. Broker-URL und Token werden niemals im Repository gespeichert.
2. Broker-Zugriff erfolgt über Secrets Management oder CI/CD-Secret Store.
3. Consumer veröffentlichen Contracts mit Version und Branch.
4. Provider veröffentlichen Verifikationsergebnisse mit Version und Branch.
5. Deployments werden über `can-i-deploy` geprüft.
6. Nach erfolgreichem Deployment wird der Deployment-Status im Broker erfasst.

### 11.2 Beispiel: CI/CD-Schritte

```text
Consumer Pipeline:
  1. Unit Tests
  2. Consumer Contract Tests
  3. Pact veröffentlichen
  4. can-i-deploy prüfen
  5. Deployment
  6. Deployment im Broker registrieren

Provider Pipeline:
  1. Unit Tests
  2. Integration Tests
  3. Provider Contract Verification
  4. Verifikationsergebnisse veröffentlichen
  5. can-i-deploy prüfen
  6. Deployment
  7. Deployment im Broker registrieren
```

### 11.3 can-i-deploy

`can-i-deploy` darf nicht als optionaler Bericht verstanden werden. Es ist ein Deployment-Gate. Wenn der Broker zeigt, dass eine Version relevante Contracts verletzt, darf diese Version nicht in die entsprechende Umgebung deployed werden.

---

## 12. Spring Cloud Contract

Spring Cloud Contract kann eine gute Alternative sein, wenn die Organisation stark Spring-/JVM-zentriert ist und Contracts zentral durch den Provider oder in einem gemeinsamen Repository gepflegt werden.

### 12.1 Beispiel: Contract DSL

```groovy
Contract.make {
    description "Gibt einen User zurück, wenn die ID existiert"

    request {
        method GET()
        url "/api/users/1"
        headers {
            accept(applicationJson())
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

Spring Cloud Contract kann aus solchen Contracts Provider-Tests und Consumer-Stubs erzeugen. Dadurch laufen Consumer gegen Stubs, die aus den Provider-Contracts abgeleitet und gegen den Provider verifiziert wurden.

### 12.2 Gute Einsatzfälle für Spring Cloud Contract

- alle Teams nutzen Spring Boot oder JVM
- Provider-Teams besitzen die API-Spezifikation stark
- Contracts sollen im Provider-Repository versioniert werden
- Stub-Artefakte sollen über Maven/Nexus/Artifactory verteilt werden
- Messaging- oder HTTP-Contracts sollen im Spring-Ökosystem getestet werden

### 12.3 Grenzen

Spring Cloud Contract ist weniger geeignet, wenn:

- Consumer polyglott sind
- Consumer selbst Contracts schreiben und veröffentlichen sollen
- ein zentrales Broker-Modell mit Deployment-Matrix benötigt wird
- viele unabhängige Consumer existieren
- Teams nicht alle im Spring-Ökosystem arbeiten

---

## 13. Pact oder Spring Cloud Contract?

| Aspekt | Pact | Spring Cloud Contract | Empfehlung |
|---|---|---|---|
| Consumer-driven | Sehr stark | Möglich, aber häufig provider-näher genutzt | Pact bei echten Consumer-Erwartungen |
| Polyglott | Stark | Primär JVM/Spring | Pact bei heterogenen Teams |
| Broker-Matrix | Pact Broker / PactFlow | Nicht Kernmodell | Pact bei unabhängigen Deployments |
| Spring-Integration | Gut | Sehr gut | Spring Cloud Contract bei reinem Spring-Stack |
| Stub-Verteilung | Broker/Dateien | Maven/Nexus/Artifactory-Stubs | Projektabhängig |
| Kulturmodell | Consumer beschreibt Erwartung | Provider oder gemeinsames Contract-Repo | Teamstruktur entscheidet |
| Betrieb | Broker oder PactFlow nötig | Build-/Artifact-Infrastruktur reicht oft | Spring Cloud Contract einfacher im Monorepo |
| `can-i-deploy` | Reifer Standard | Muss anders abgebildet werden | Pact für Deployment-Gates |

---

## 14. Gute Anwendung: Contract Tests minimal halten

Contract Tests sind kein Ersatz für vollständige API-Integrationstests.

Schlecht:

```java
.body(new PactDslJsonBody()
        .integerType("id")
        .stringType("name")
        .stringType("email")
        .stringType("phone")
        .stringType("street")
        .stringType("city")
        .stringType("country")
        .stringType("createdAt")
        .stringType("updatedAt")
        .stringType("internalComment")
        .stringType("crmStatus"))
```

Das ist kein minimaler Consumer-Contract, sondern ein Snapshot des Provider-DTOs. Solche Contracts werden spröde und bremsen Provider-Weiterentwicklung.

Gut:

```java
.body(new PactDslJsonBody()
        .integerType("id", 1)
        .stringType("email", "max@example.com"))
```

Wenn der Consumer nur `id` und `email` für einen Versandprozess braucht, soll genau das im Contract stehen.

---

## 15. Anti-Patterns

### 15.1 Contract als vollständiger DTO-Snapshot

Ein Contract darf nicht alle Felder einer Provider-Response spiegeln, nur weil sie vorhanden sind. Das erzeugt Scheinkontrolle und unnötige Kopplung.

### 15.2 Consumer testet gegen echten Provider

Consumer-Contract-Tests laufen gegen einen Pact-Mockserver oder generierte Stubs, nicht gegen einen laufenden Provider. Sonst entstehen langsame Integrationstests mit unklarem Fehlerbild.

### 15.3 Provider States als Demo-Datenbank

Provider States dürfen keine globale Demo-Datenbank voraussetzen. Jeder State muss reproduzierbar und isoliert sein.

### 15.4 `can-i-deploy` nur als Dashboard

Ein grünes Dashboard ist nutzlos, wenn es Deployment nicht beeinflusst. `can-i-deploy` gehört als Gate in die Pipeline.

### 15.5 Contracts mit produktiven Daten

Contracts dürfen keine echten E-Mail-Adressen, Tokens, Kundennamen, IBANs, Telefonnummern oder internen IDs aus Produktion enthalten.

### 15.6 Contracts für Berechtigungslogik missverstehen

Ein Contract kann prüfen, dass `403` bei fehlender Berechtigung zurückkommt. Er ersetzt aber nicht die fachliche Autorisierungsprüfung und keine Security-Tests.

### 15.7 Zu viele Provider-Details im Consumer-Contract

Consumer dürfen nicht interne Statusfelder, Debug-Felder oder Felder aus anderen Use Cases in ihren Contract aufnehmen.

### 15.8 Keine Versionierung

Contracts ohne saubere Consumer-/Provider-Versionen verlieren ihren Wert. Ohne Versionierung kann der Broker keine belastbare Deployment-Aussage treffen.

---

## 16. Security- und SaaS-Aspekte

Contract Testing ist kein Security-Framework, aber API-Verträge haben direkte Sicherheitswirkung.

### 16.1 Datenminimierung

Consumer-Contracts sollen nur die Felder enthalten, die der Consumer benötigt. Dadurch wird übermäßige Datenexposition sichtbar.

Schlecht:

```json
{
  "id": 1,
  "email": "max@example.com",
  "phone": "+49123456789",
  "iban": "DE89370400440532013000",
  "internalRiskScore": 91
}
```

Gut:

```json
{
  "id": 1,
  "email": "max@example.com"
}
```

### 16.2 Mandantentrennung

In SaaS-Systemen müssen Contracts auch Tenant-Kontext sichtbar machen, wenn APIs tenantbezogen arbeiten.

Beispiel:

```java
.headers(Map.of(
        "Accept", "application/json",
        "X-Tenant-Id", "tenant-a"
))
```

Provider-Verifikation muss sicherstellen, dass Provider States tenantbezogen sind und nicht versehentlich Daten anderer Mandanten zurückliefern.

### 16.3 Fehlerantworten

Contracts für Fehlerfälle dürfen keine Stacktraces, SQL-Fehler, interne Klassenpfade oder Secrets erwarten.

Gutes Fehlerobjekt:

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User was not found",
  "correlationId": "7f9c2d"
}
```

Schlechtes Fehlerobjekt:

```json
{
  "exception": "org.hibernate.LazyInitializationException",
  "sql": "select * from users where email='max@example.com'",
  "stackTrace": "..."
}
```

### 16.4 Authentifizierung und Autorisierung

Contracts können erwartete Statuscodes und Header abbilden, ersetzen aber keine Security-Tests.

Pflichtfälle für kritische APIs:

- fehlendes Token → `401`
- ungültiges Token → `401`
- gültiges Token ohne Berechtigung → `403`
- Zugriff auf fremde Ressource → `403` oder `404`, je nach Security-Design
- ungültiger Tenant-Kontext → definierter Fehlerstatus

### 16.5 Broker-Sicherheit

Pact Broker, PactFlow oder Stub-Repositories enthalten API-Strukturen. Sie können interne Pfade, Datenmodelle, Fehlercodes und Beispielwerte enthalten. Zugriff muss deshalb rollenbasiert, authentifiziert und auditierbar sein.

---

## 17. Contract Testing und OpenAPI

OpenAPI und Contract Testing lösen unterschiedliche Probleme.

| Aspekt | OpenAPI | Contract Testing |
|---|---|---|
| Ziel | API beschreiben und dokumentieren | API-Erwartung zwischen Consumer und Provider verifizieren |
| Perspektive | häufig providerzentriert | häufig consumerzentriert |
| Nutzung | Dokumentation, Client-Generierung, Schema-Validierung | CI/CD-Kompatibilität |
| Stärke | öffentliche API-Beschreibung | konkrete Consumer-Erwartungen |
| Grenze | zeigt nicht automatisch, was ein Consumer wirklich nutzt | ersetzt keine vollständige API-Dokumentation |

Für öffentliche oder stark regulierte APIs kann beides notwendig sein: OpenAPI als formale API-Beschreibung und Pact/Spring Cloud Contract als automatisierte Kompatibilitätsprüfung.

---

## 18. Teststrategie

Contract Tests stehen zwischen Unit Tests und End-to-End-Tests.

```text
Unit Tests
  Schnell, isoliert, interne Logik

Contract Tests
  API-Kompatibilität Consumer ↔ Provider

Integration Tests
  Provider mit Datenbank, Messaging, Infrastruktur

End-to-End-Tests
  wenige kritische Geschäftsflüsse über mehrere Services
```

Contract Tests reduzieren die Anzahl notwendiger E2E-Tests, ersetzen sie aber nicht vollständig.

---

## 19. CI/CD-Standard

### Consumer Pipeline

```yaml
name: consumer-contract

on:
  push:
    branches: [ main ]

jobs:
  contract:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run tests including Pact consumer tests
        run: ./gradlew test

      - name: Publish Pact contracts
        env:
          PACT_BROKER_BASE_URL: ${{ secrets.PACT_BROKER_BASE_URL }}
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
        run: ./gradlew pactPublish
```

### Provider Pipeline

```yaml
name: provider-contract

on:
  push:
    branches: [ main ]

jobs:
  verify-contracts:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Verify provider contracts
        env:
          PACT_BROKER_URL: ${{ secrets.PACT_BROKER_URL }}
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
        run: ./gradlew pactVerify

      - name: Check deployability
        env:
          PACT_BROKER_BASE_URL: ${{ secrets.PACT_BROKER_BASE_URL }}
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
        run: pact-broker can-i-deploy --pacticipant user-service --version "$GITHUB_SHA" --to-environment staging
```

Die konkreten Tasks und CLI-Befehle hängen von Gradle-/Maven-Plugin, Broker-Version und Organisationsstandard ab. Entscheidend ist die Regel: Veröffentlichen, Verifizieren, Deployment prüfen.

---

## 20. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Consumer-Nutzen | Beschreibt der Contract nur tatsächlich genutzte Felder? | `id`, `email`, nicht komplettes DTO | Pflicht |
| Provider State | Ist der State klein, deterministisch und isoliert? | `User with ID 1 exists` | Pflicht |
| Fehlerfälle | Sind relevante Fehlerstatus abgedeckt? | `404`, `403`, `409` | Pflicht bei kritischen APIs |
| Security | Enthält der Contract keine Secrets oder Produktions-PII? | Fake-E-Mail | Pflicht |
| Tenant | Wird Tenant-Kontext korrekt modelliert? | Header oder Claim | Pflicht bei SaaS |
| CI | Läuft Contract Testing in Consumer- und Provider-Pipeline? | `pactPublish`, `pactVerify` | Pflicht |
| Gate | Gibt es ein Deployment-Gate? | `can-i-deploy` | Pflicht bei Pact |
| Minimalität | Wird nicht das komplette Provider-DTO gespiegelt? | nur genutzte Felder | Pflicht |
| Versionierung | Sind Consumer- und Provider-Versionen eindeutig? | Git-SHA, SemVer | Pflicht |
| Ownership | Ist klar, wer den Contract pflegt? | Consumer-Team oder gemeinsames API-Team | Pflicht |

---

## 21. Automatisierbare Prüfungen

Mögliche CI-/Repository-Regeln:

```text
- Pact-Dateien dürfen keine echten E-Mails, Tokens, IBANs oder produktiven IDs enthalten.
- Provider-Pipelines müssen Contract Verification ausführen.
- Consumer-Pipelines müssen Pact Contracts veröffentlichen.
- Deployments müssen can-i-deploy oder gleichwertiges Gate ausführen.
- Contracts dürfen keine Felder mit Namen password, token, secret, iban enthalten, außer sie sind explizit Security-Testfälle mit synthetischen Werten.
- Provider States müssen eindeutige Namen besitzen.
- Contract Tests dürfen nicht gegen produktive Endpunkte laufen.
```

---

## 22. Migration

Bestehende Service-zu-Service-Tests werden schrittweise migriert.

### Schritt 1: Kritische Interaktionen identifizieren

Beginne nicht mit allen APIs. Starte mit:

- Payment
- Order
- Identity
- Customer
- Billing
- Tenant-/Entitlement-APIs

### Schritt 2: Consumer-Client isolieren

Consumer-Contract-Tests sollten den API-Client testen, nicht den kompletten Consumer-Service.

### Schritt 3: Minimalen Contract schreiben

Nur die Felder beschreiben, die der Consumer benötigt.

### Schritt 4: Provider-Verifikation ergänzen

Provider muss den Contract gegen echte Controller/API-Schicht verifizieren.

### Schritt 5: Broker/Gate integrieren

Contracts und Verifikationsergebnisse müssen Teil der Pipeline werden.

### Schritt 6: E2E-Tests reduzieren

Erst wenn Contract Tests stabil sind, werden redundante E2E-Tests reduziert.

---

## 23. Ausnahmen

Contract Testing kann entfallen, wenn:

- Consumer und Provider im selben Deployable leben
- keine Netzwerk-/API-Grenze existiert
- die API nicht unabhängig versioniert wird
- der Aufruf rein intern und nicht serviceübergreifend ist
- OpenAPI-/Schema-Validation plus Integrationstest fachlich ausreichend und begründet ist
- eine externe API keine Contract-Mitwirkung erlaubt und ein anderer stabiler Testansatz verwendet wird

Die Ausnahme muss im Pull Request oder technischen Dokument begründet werden.

---

## 24. Definition of Done

Eine Service-zu-Service-API erfüllt diese Richtlinie, wenn die relevanten Consumer-Erwartungen als minimale Contracts beschrieben sind, Provider States deterministisch und isoliert sind, Provider Contracts automatisiert gegen die echte API verifiziert werden, Contracts und Verifikationsergebnisse versioniert veröffentlicht werden, Deployment-Gates Vertragsverletzungen verhindern, Security- und Tenant-Fälle angemessen abgedeckt sind, keine produktiven personenbezogenen Daten oder Secrets in Contracts enthalten sind und Contract Tests in der CI/CD-Pipeline stabil laufen.

---

## 25. Quellen und weiterführende Literatur

- Pact Documentation — Consumer tests: https://docs.pact.io/consumer
- Pact JVM Consumer JUnit 5: https://docs.pact.io/implementation_guides/jvm/consumer/junit5
- Pact JVM Provider JUnit 5: https://docs.pact.io/implementation_guides/jvm/provider/junit5
- Pact JVM Spring/JUnit 5 Support: https://docs.pact.io/implementation_guides/jvm/provider/junit5spring
- Pact Broker: https://docs.pact.io/pact_broker
- Pact Broker can-i-deploy: https://docs.pact.io/pact_broker/can_i_deploy
- Pact Provider States: https://docs.pact.io/getting_started/provider_states
- Spring Cloud Contract Reference Documentation: https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/
- Spring Cloud Contract Project Page: https://spring.io/projects/spring-cloud-contract
- Martin Fowler — Consumer-Driven Contracts: https://martinfowler.com/articles/consumerDrivenContracts.html
- Martin Fowler — Contract Test: https://martinfowler.com/bliki/ContractTest.html
- OWASP API Security Top 10: https://owasp.org/API-Security/
