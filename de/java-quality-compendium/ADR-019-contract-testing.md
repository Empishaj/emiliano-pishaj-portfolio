# ADR-019 — Contract Testing: Pact & Spring Cloud Contract

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x · Pact JVM 4.x              |
| Datum      | 2026-02-23                                       |
| Kategorie  | Testing / Microservices                           |

---

## Kontext & Problem

In einer Microservice-Architektur kommunizieren Services über APIs. Wenn Service A eine Änderung an seiner API macht, bricht Service B — aber das merkt man erst in Produktion oder im Integrationstest im CI. End-to-End-Tests sind langsam, fragil und teuer. Contract Testing löst das: Consumer und Provider einigen sich auf einen Vertrag (Contract) und testen ihn unabhängig voneinander.

---

## Das Consumer-Driven Contract Modell

```
Consumer (bestellt) → schreibt Contract → veröffentlicht im Pact Broker
Provider (liefert)  → verifiziert Contract → CI schlägt fehl bei Verletzung

Ablauf:
1. Consumer-Team schreibt Test, der definiert was es vom Provider erwartet
2. Pact generiert einen Contract (JSON-Datei)
3. Contract wird im Pact Broker veröffentlicht
4. Provider-Team holt Contract, verifiziert ihn gegen seine echte Implementierung
5. CI/CD: Deployment nur möglich wenn beide Seiten den Contract erfüllen
```

---

## Consumer-Seite: Contract definieren

### Schlecht — Consumer testet gegen echten Provider oder WireMock ohne Contract

```java
// WireMock ohne Contract — beide Seiten können sich unabgestimmt ändern
@Test
void getUser_returnsUserDto() {
    // WireMock manuell konfiguriert — kein geteilter Vertrag mit dem Provider
    wireMock.stubFor(get("/api/users/1")
        .willReturn(okJson("""
            { "id": 1, "name": "Max", "email": "max@example.com" }
            """)));

    var user = userClient.findById(1L);
    assertThat(user.name()).isEqualTo("Max");
    // Provider ändert "email" zu "emailAddress" → dieser Test bleibt grün, Production bricht!
}
```

### Gut — Consumer definiert Contract mit Pact

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "user-service", port = "8080")
class UserServiceContractConsumerTest {

    // Consumer definiert: was ich erwarte (Minimum — nicht zu viel!)
    @Pact(consumer = "order-service")
    public RequestResponsePact getUserById(PactDslWithProvider builder) {
        return builder
            .given("User with ID 1 exists") // Provider-State
            .uponReceiving("Request for user with ID 1")
                .method("GET")
                .path("/api/users/1")
                .headers("Accept", "application/json")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .integerType("id", 1)          // Typ-Prüfung, nicht exakter Wert
                    .stringType("name", "Max")      // Nur das prüfen was der Consumer nutzt!
                    .stringType("email", "max@example.com")
                    // emailAddress, createdAt etc. ignorieren — der Consumer braucht sie nicht
                )
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUserById")
    void getUser_parsesResponseCorrectly(MockServer mockServer) {
        // Pact startet einen MockServer der exakt den Contract simuliert
        var client = new UserServiceClient("http://localhost:" + mockServer.getPort());

        var user = client.findById(1L);

        assertThat(user.name()).isEqualTo("Max");
        assertThat(user.email()).isEqualTo("max@example.com");
    }
}
```

---

## Provider-Seite: Contract verifizieren

```java
// Provider holt Contract vom Pact Broker und verifiziert ihn
@Provider("user-service")
@PactBroker(
    url = "${PACT_BROKER_URL:http://localhost:9292}",
    authentication = @PactBrokerAuth(token = "${PACT_BROKER_TOKEN}")
)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class UserServiceContractProviderTest {

    @LocalServerPort int port;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // Provider States: Datenbankzustand für jeden Testfall vorbereiten
    @State("User with ID 1 exists")
    void userWithId1Exists() {
        // Testdaten einspielen oder Mocks setzen
        given(userRepository.findById(1L))
            .willReturn(Optional.of(new UserEntity(1L, "Max", "max@example.com")));
    }

    @State("User with ID 99 does not exist")
    void userWithId99DoesNotExist() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());
    }
}
```

---

## Spring Cloud Contract als Alternative (für Spring-only Projekte)

```groovy
// src/test/resources/contracts/getUserById.groovy
// Vom Provider definiert — Consumer generiert Client-Stubs daraus
Contract.make {
    description "Gibt User zurück wenn ID existiert"

    request {
        method GET()
        url "/api/users/1"
        headers { accept applicationJson() }
    }

    response {
        status OK()
        headers { contentType applicationJson() }
        body([
            id   : 1,
            name : "Max Mustermann",
            email: "max@example.com"
        ])
    }
}
```

```java
// Provider-seitiger Test — automatisch generiert aus Groovy-Contract
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.example:user-service:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class OrderServiceContractTest {
    // Spring Cloud Contract generiert WireMock-Stubs aus dem Contract
    // Consumer-Tests laufen gegen diese Stubs — immer in Sync mit Provider
}
```

---

## Wann Pact, wann Spring Cloud Contract?

| Kriterium                      | Pact                        | Spring Cloud Contract       |
|--------------------------------|-----------------------------|-----------------------------|
| Consumer-driven (Consumer schreibt Contract) | ✅ | ❌ Provider-driven |
| Polyglot (non-Java Consumer)   | ✅ Node, Go, Python, etc.   | ❌ Nur JVM                 |
| Spring-only Projekt            | ✅ Möglich                  | ✅ Einfacher                |
| Pact Broker nötig              | ✅ Ja (oder PactFlow)       | ❌ Nein (Git-Repo reicht)  |
| Empfehlung                     | Microservice-Ökosystem      | Spring-Monorepo             |

---

## Konsequenzen

**Positiv:** API-Breaking-Changes werden vor dem Deployment entdeckt, nicht danach. Kein End-to-End-Test-Overhead für API-Kompatibilität. Consumer und Provider können unabhängig deployen — "can I deploy?" wird durch den Broker beantwortet.

**Negativ:** Pact Broker muss betrieben werden (oder PactFlow SaaS). Teams müssen Consumer-driven Contracts kulturell akzeptieren — Provider-Team darf nichts brechen ohne Consumer zu informieren.

---

## Tipps

- **Contracts minimal halten**: Consumer testet nur die Felder die er wirklich nutzt — kein `assertThat(response).isEqualTo(fullDto)`.
- **`can-i-deploy`** im CI-Gate: Pact CLI prüft ob alle Contracts erfüllt sind bevor Deployment erlaubt wird.
- **Provider States sauber halten**: Ein Provider State pro Testfall — keine Kombinations-States.
- **Versionierung**: Contracts sind semantisch versioniert — breaking changes erfordern eine neue Contract-Version.
 