# QG-JAVA-001 — Java Records for DTOs

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-001 |
| Titel | Java Records for DTOs |
| Status | Accepted / verbindlicher Standard für neue DTOs |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, SaaS-Plattformen, API- und Service-Grenzen, sichere Datenrepräsentation |
| Letzte Validierung | 2026-05-02 |
| Validierte Quellenbasis | OpenJDK JEP 395, Oracle Java SE 21, Jackson 2.12 Release Notes, Spring Boot Reference Documentation, Jakarta Persistence 3.1, OWASP Input Validation Cheat Sheet, OWASP Logging Cheat Sheet, Apache Wicket 10 Reference Guide |
| Zugehörige ADR | ADR-001 — Records als Standard für einfache DTOs |
| Technische Beispielvalidierung | Die reinen Java-21-Beispiele ohne externe Framework-Abhängigkeiten wurden mit `javac --release 21` syntaktisch geprüft. |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wann Java Records für Datentransferobjekte verwendet werden sollen, wann sie nicht geeignet sind und welche Qualitäts-, Security-, Framework- und Review-Regeln dabei gelten.

Ziel ist nicht, jede datenähnliche Klasse durch einen Record zu ersetzen. Ziel ist, einfache Datentransferobjekte präziser, unveränderlicher, besser prüfbar und weniger fehleranfällig zu modellieren. Diese Richtlinie soll Entwicklern helfen, im Alltag zuverlässig zu entscheiden:

- Ist ein Record hier richtig?
- Muss ich eine klassische Klasse verwenden?
- Welche Validierung gehört in den Record?
- Welche Security-Fallen entstehen durch `toString()`?
- Was muss ich bei Collections, Arrays, JPA, Jackson, Spring Boot und Wicket beachten?
- Wie prüfe ich das sauber im Pull Request?

Diese Datei ist bewusst ausführlicher als ein ADR. Ein ADR dokumentiert die Architekturentscheidung. Diese Guideline erklärt die korrekte Anwendung im Code.

---

## 2. Kurzregel für Entwickler

Verwende Java Records als Standard für einfache DTOs, wenn das Objekt nach seiner Erzeugung nicht verändert werden soll und keine Framework-Anforderung gegen Records spricht.

Verwende Records nicht für JPA-Entities, komplexe Domain-Aggregate mit Lifecycle, Objekte mit bewusstem Mutable State, Framework-Proxies oder DTOs mit sensiblen Feldern, wenn `toString()` nicht abgesichert ist.

Wenn ein Record mutable Komponenten enthält, zum Beispiel `List`, `Map`, `Set`, Arrays oder mutable eigene Typen, muss die Unveränderlichkeit bewusst hergestellt oder die Verwendung begründet werden.

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

Ein neues einfaches DTO MUSS als Record modelliert werden, wenn alle folgenden Bedingungen erfüllt sind:

1. Das Objekt dient primär dem Transport von Daten.
2. Das Objekt benötigt nach Konstruktion keine fachlich gewollte Mutation.
3. Das Objekt wird nicht als JPA-Entity verwendet.
4. Das Objekt benötigt keine Klassenvererbung.
5. Das verwendete Framework unterstützt Konstruktor- oder Record-Binding.
6. Sensible Felder werden nicht versehentlich über `toString()` offengelegt.
7. Mutable Komponenten werden vermieden oder defensiv kopiert.
8. Pflichtfelder werden validiert.
9. API-Grenzen behandeln externe Eingaben als nicht vertrauenswürdig.
10. Ausnahmen werden im Pull Request begründet.

### 3.2 Darf-nicht-Regeln

Ein Record DARF NICHT verwendet werden für:

1. JPA-Entities.
2. Klassen, die von einem Framework per Proxy erweitert werden müssen.
3. Objekte, deren Zustand während ihres Lebenszyklus bewusst verändert wird.
4. Domain-Aggregate mit komplexem Verhalten, Identität und Zustandsübergängen.
5. DTOs mit Passwörtern, Tokens, API Keys oder Secrets, sofern `toString()` nicht geschützt ist.
6. Records mit Arrays, wenn `equals()`, `hashCode()` oder `toString()` fachlich relevant sind und nicht explizit überschrieben werden.
7. Große Objektgraphen in Wicket-Seitenzustand ohne bewusstes Serializable- und Detach-Konzept.

### 3.3 Sollte-Regeln

Ein Record SOLLTE verwendet werden für:

1. API-Response-DTOs.
2. einfache API-Request-DTOs.
3. interne Service-DTOs.
4. Query-/Projection-DTOs.
5. Integration-DTOs.
6. kompakte Rückgabewerte aus Methoden.
7. einfache Value Objects, wenn alle Invarianten vollständig abgesichert werden.
8. lokale Zwischenstrukturen in Methoden, wenn sie nur innerhalb eines Algorithmus verwendet werden.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Java-Code in Applikations-, Service-, API-, Integrations- und Präsentationsschichten.

Sie gilt insbesondere für:

- REST-Request-DTOs
- REST-Response-DTOs
- GraphQL-DTOs, sofern das Framework Record-Binding unterstützt
- interne Command-/Query-DTOs
- Service-Rückgabeobjekte
- Projection-Objekte aus Datenbankabfragen
- Mapping-Zwischenobjekte
- Konfigurationsdaten, wenn Spring Boot Constructor Binding genutzt wird
- Wicket-View-Model-Daten, sofern Serialisierung und Page-State bewusst behandelt werden

Sie gilt nicht automatisch für:

- JPA-Entities
- Hibernate-Proxies
- persistente Aggregate
- mutable Form-Backing-Beans, wenn das UI-Framework Setter-Binding voraussetzt
- komplexe Domain-Objekte
- technische Framework-Klassen
- Objekte mit bewusstem Cache-, Session- oder Lifecycle-Verhalten

---

## 5. Begriffe

| Aspekt | Details/Erklärung | Beispiel |
|---|---|---|
| DTO | Datentransferobjekt. Es transportiert Daten über Schicht-, Prozess- oder Systemgrenzen. | `UserResponse`, `CreateUserRequest` |
| Record | Spezielle Java-Klassenform für transparente Datenaggregate mit festem Komponentensatz. | `public record UserDto(Long id, String name) {}` |
| Record-Komponente | Ein Wert im Record-Header. Der Compiler erzeugt dafür Feld, Accessor und Konstruktorparameter. | `String name` in `record UserDto(String name)` |
| Canonical Constructor | Konstruktor mit exakt den Record-Komponenten als Parametern. | `public UserDto(Long id, String name)` |
| Compact Constructor | Kurzform des canonical constructor ohne Parameterliste. | `public UserDto { ... }` |
| Shallow Immutability | Die Komponentenreferenzen sind final, die referenzierten Objekte können aber selbst mutable sein. | `record Page(List<String> items)` |
| Invariante | Regel, die für ein Objekt immer gelten muss. | `id > 0`, `email` nicht leer |
| Normalisierung | Vereinheitlichung eines Eingabewerts ohne Bedeutungsverlust. | `email.trim().toLowerCase(Locale.ROOT)` |
| Validierung | Prüfung, ob ein Wert fachlich und technisch zulässig ist. | `@NotBlank`, `@Email`, Wertebereich |
| Sensitive Data | Daten, die nicht unkontrolliert geloggt, serialisiert oder angezeigt werden dürfen. | Passwort, Token, API Key, Session-ID |

---

## 6. Technischer Hintergrund

Java Records wurden mit JEP 395 in JDK 16 finalisiert. Sie sind eine spezielle Klassenform für einfache Datenaggregate. Ein Record deklariert seinen Zustand direkt im Header. Der Compiler erzeugt daraus unter anderem:

- private final fields,
- einen canonical constructor,
- Accessor-Methoden mit Komponentennamen,
- `equals()`,
- `hashCode()`,
- `toString()`.

Ein Record ist implizit `final` und kann keine andere Klasse erweitern. Er kann aber Interfaces implementieren.

Wichtig ist die präzise Formulierung: Records sind shallow immutable. Das bedeutet, die Komponentenreferenzen können nach Konstruktion nicht neu gesetzt werden. Wenn eine Komponente aber auf ein mutable Objekt zeigt, bleibt dieses Objekt veränderbar.

Beispiel:

```java
public record PageDto(List<String> items) {}
```

Der Record verhindert, dass `items` auf eine andere Liste zeigt. Er verhindert aber nicht automatisch, dass die übergebene Liste außerhalb des Records verändert wird.

Deshalb gilt: Records reduzieren Mutation, aber sie ersetzen kein bewusstes Immutability-Design.

---

## 7. Warum Records die Standardwahl für DTOs sind

Klassische JavaBeans sind für reine DTOs häufig unnötig schwergewichtig. Sie enthalten Felder, Getter, Setter, Konstruktoren und manuell oder generiert erzeugte Methoden. Dadurch entsteht Code, der gelesen, geprüft und gewartet werden muss, obwohl er keinen fachlichen Mehrwert liefert.

Records verbessern einfache DTOs in fünf Punkten:

1. Weniger Boilerplate.
2. Klare Datenstruktur.
3. Unveränderlichkeit als Standardnähe.
4. Automatisch konsistente `equals()`- und `hashCode()`-Implementierung.
5. Besser erkennbare Absicht im Code.

Der wichtigste Qualitätsgewinn ist nicht Kürze allein. Der eigentliche Gewinn ist semantische Klarheit: Ein Record sagt dem Leser, dass dieser Typ primär Daten trägt und nicht als veränderliches Objekt mit komplexem Verhalten gedacht ist.

---

## 8. Entscheidungsmatrix

| Aspekt | Details/Erklärung | Entscheidung |
|---|---|---|
| Einfaches API-Response-DTO | Daten werden erzeugt und nach außen gegeben. Keine Mutation nötig. | Record verwenden |
| Einfaches API-Request-DTO | Daten kommen von außen, werden validiert und dann weiterverarbeitet. | Record verwenden, Validierung verpflichtend |
| DTO mit `List` oder `Map` | Mutable Komponente möglich. | Record nur mit defensiver Kopie |
| DTO mit Array | Arrays bleiben mutable und haben problematische `equals()`-/`hashCode()`-Semantik. | Möglichst vermeiden |
| JPA-Entity | JPA verlangt u. a. No-Arg-Konstruktor und nicht-finale Entity-Klassen. | Kein Record |
| Domain-Aggregat | Hat Identität, Lifecycle, Verhalten und Zustandsübergänge. | In der Regel klassische Klasse |
| Value Object | Kann als Record geeignet sein, wenn alle Invarianten gesichert sind. | Record möglich, aber strenger prüfen |
| Login-/Token-DTO | `toString()` kann sensible Werte offenlegen. | Nur mit Redaction oder Logging-Verbot |
| Wicket Page State | Wicket serialisiert Seitenzustand. | Record nur bewusst serialisierbar oder über detachable model |
| Spring Boot ConfigurationProperties | Constructor Binding unterstützt Records. | Record geeignet |

---

## 9. Guter Standardfall: einfaches Response-DTO

### 9.1 Zu vermeiden: klassische JavaBean ohne Grund

```java
public class UserResponse {
    private Long id;
    private String displayName;
    private String email;

    public UserResponse() {
    }

    public UserResponse(Long id, String displayName, String email) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

Diese Klasse transportiert nur Daten. Die Setter erzeugen den Eindruck, dass nachträgliche Mutation gewünscht ist. Für ein reines Response-DTO ist das unnötig.

### 9.2 Bevorzugt: Record

```java
public record UserResponse(
    Long id,
    String displayName,
    String email
) {}
```

Dieser Code ist kürzer, klarer und drückt die Absicht besser aus: Das Objekt ist ein Datencontainer mit festem Wertset.

---

## 10. API-Request-DTOs mit Bean Validation

Für externe Eingaben ist ein Record allein nicht ausreichend. Ein Record beschreibt eine Struktur. Er prüft nicht automatisch, ob die Werte zulässig sind.

Bei API-Request-DTOs MUSS Validierung vorhanden sein. In Spring-Anwendungen wird dafür typischerweise Jakarta Bean Validation eingesetzt.

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank
    @Size(max = 120)
    String displayName,

    @NotBlank
    @Email
    @Size(max = 254)
    String email
) {}
```

Controller-Beispiel:

```java
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userApplicationService.createUser(request);
        return ResponseEntity.ok(response);
    }
}
```

Regel: Jeder API-Request aus einer externen Quelle ist zunächst nicht vertrauenswürdig. Validierung muss so früh wie möglich im Datenfluss erfolgen.

---

## 11. Compact Constructor für Invarianten und Normalisierung

Ein kompakter Konstruktor ist geeignet für technische Invarianten und einfache Normalisierung. Er darf nicht für Datenbankzugriffe, Remote Calls, Berechtigungsprüfungen oder komplexe Business-Prozesse missbraucht werden.

### 11.1 Gutes Beispiel

```java
import java.util.Locale;
import java.util.Objects;

public record UserEmail(String value) {

    public UserEmail {
        Objects.requireNonNull(value, "email must not be null");

        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        if (value.length() > 254) {
            throw new IllegalArgumentException("email must not exceed 254 characters");
        }
    }
}
```

Dieses Beispiel ist geeignet, weil es eine lokale Invariante des Value Objects absichert. Die Normalisierung ist deterministisch und hat keine Seiteneffekte.

### 11.2 Fehlerhaftes Beispiel

```java
public record UserEmail(String value) {

    public UserEmail {
        value = value.trim().toLowerCase();
    }
}
```

Fehler:

1. `value` kann `null` sein.
2. `toLowerCase()` nutzt die Default-Locale.
3. Leere Werte werden nicht verhindert.
4. Maximallänge wird nicht begrenzt.

Korrektur:

```java
import java.util.Locale;
import java.util.Objects;

public record UserEmail(String value) {

    public UserEmail {
        Objects.requireNonNull(value, "email must not be null");
        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
```

---

## 12. Shallow Immutability und defensive Kopien

Records sind shallow immutable. Deshalb müssen mutable Komponenten bewusst behandelt werden.

### 12.1 Fehlerhaft: Mutable List ohne defensive Kopie

```java
import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int total
) {}
```

Problem:

```java
List<String> names = new ArrayList<>();
names.add("Ada");

PageResponse<String> page = new PageResponse<>(names, 1);
names.add("Grace");

// page.content() enthält jetzt ebenfalls "Grace".
```

Der Record selbst ist nicht neu zugewiesen worden. Sein Inhalt hat sich trotzdem verändert, weil die Liste mutable war.

### 12.2 Korrekt: defensive Kopie

```java
import java.util.List;
import java.util.Objects;

public record PageResponse<T>(
    List<T> content,
    int total
) {
    public PageResponse {
        Objects.requireNonNull(content, "content must not be null");
        content = List.copyOf(content);

        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }

        if (content.size() > total) {
            throw new IllegalArgumentException("content size must not exceed total");
        }
    }
}
```

`List.copyOf(...)` erzeugt eine nicht veränderbare Kopie und weist Null-Elemente zurück. Das ist für DTOs häufig sinnvoll, weil Null-Elemente in Listen selten eine saubere API-Semantik haben.

### 12.3 Regel für Collections

| Aspekt | Details/Erklärung | Beispiel |
|---|---|---|
| `List<T>` | Nur mit defensiver Kopie verwenden. | `content = List.copyOf(content);` |
| `Set<T>` | Nur mit defensiver Kopie verwenden. | `roles = Set.copyOf(roles);` |
| `Map<K,V>` | Nur mit defensiver Kopie verwenden. | `labels = Map.copyOf(labels);` |
| Mutable Elemente | Defensive Kopie der Collection reicht nicht, wenn die Elemente selbst mutable sind. | `List<MutableAddress>` bleibt riskant |
| Null-Elemente | In DTO-Collections grundsätzlich vermeiden. | Bean Validation oder `List.copyOf` |

---

## 13. Arrays in Records

Arrays sind in Records besonders riskant. Sie bleiben mutable. Außerdem verwendet die generierte `equals()`-Implementierung bei Array-Komponenten die normale Objektgleichheit der Array-Referenz und nicht den inhaltlichen Vergleich.

### 13.1 Fehlerhaft

```java
public record AvatarResponse(
    byte[] content,
    String mediaType
) {}
```

Probleme:

1. `content` kann nach außen verändert werden.
2. `equals()` vergleicht Array-Referenzen, nicht Array-Inhalte.
3. `hashCode()` basiert nicht auf dem Array-Inhalt.
4. `toString()` gibt keinen sinnvollen Inhalt aus und kann Debugging erschweren.
5. Große Binärdaten gehören selten direkt in DTOs, die geloggt oder in Page State gespeichert werden.

### 13.2 Besser: Metadaten statt Binärinhalt

```java
public record AvatarResponse(
    String resourceUrl,
    String mediaType,
    long sizeInBytes
) {}
```

### 13.3 Wenn Array unvermeidbar ist

Wenn ein Array fachlich unvermeidbar ist, muss defensiv kopiert und die Semantik bewusst dokumentiert werden.

```java
import java.util.Arrays;
import java.util.Objects;

public record BinaryPayload(
    byte[] content,
    String mediaType
) {
    public BinaryPayload {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(mediaType, "mediaType must not be null");
        content = Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BinaryPayload that
            && Arrays.equals(this.content, that.content)
            && this.mediaType.equals(that.mediaType);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(content);
        result = 31 * result + mediaType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BinaryPayload[content=<" + content.length + " bytes>, mediaType=" + mediaType + "]";
    }
}
```

Regel: Arrays in Records sind ein Warnsignal. Sie müssen im Review aktiv geprüft werden.

---

## 14. Sensitive Data und `toString()`

Records erzeugen automatisch `toString()`. Das ist praktisch, aber bei sensiblen Daten gefährlich.

### 14.1 Fehlerhaft

```java
public record LoginRequest(
    String username,
    String password
) {}
```

Der automatisch erzeugte String kann sinngemäß so aussehen:

```text
LoginRequest[username=alice, password=secret123]
```

Wenn dieses Objekt geloggt, in Exceptions eingebettet oder in Debug-Ausgaben geschrieben wird, werden sensible Daten offengelegt.

### 14.2 Korrekt mit Redaction

```java
import java.util.Objects;

public record LoginRequest(
    String username,
    String password
) {
    public LoginRequest {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
    }

    @Override
    public String toString() {
        return "LoginRequest[username=%s, password=<redacted>]".formatted(username);
    }
}
```

### 14.3 Noch besser: kein Logging sensibler DTOs

Für folgende Kontexte gilt ein strenges Logging-Verbot vollständiger Request-/Response-Objekte:

- Login
- Passwortänderung
- Passwort-Reset
- Token-Erzeugung
- Token-Aktualisierung
- API-Key-Verwaltung
- OAuth-/OIDC-Flows
- Payment
- personenbezogene Exportfunktionen
- Admin-Operationen mit erweiterten Rechten

Stattdessen dürfen nur technische Korrelationsdaten geloggt werden, zum Beispiel:

- Request-ID
- Correlation-ID
- Tenant-ID, sofern zulässig und nicht geheim
- User-ID, sofern zulässig und notwendig
- Operation
- Ergebnisstatus
- Fehlerkategorie
- Zeitpunkt

Beispiel:

```java
log.info(
    "login failed: correlationId={}, tenantId={}, usernameHash={}, reason={}",
    correlationId,
    tenantId,
    hashForLog(username),
    failureReason
);
```

Regel: Records mit sensiblen Komponenten müssen im Review markiert werden. Automatisch erzeugtes `toString()` ist hier nicht akzeptabel.

---

## 15. Records und Domain Design

Records sind sehr gut für Datenrepräsentationen. Sie sind nicht automatisch gute Domain-Objekte.

### 15.1 Gutes Value Object

```java
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(
    BigDecimal amount,
    Currency currency
) {
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new IllegalArgumentException("amount scale exceeds currency fraction digits");
        }
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("cannot add money with different currency");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

Dieses Record ist geeignet, weil es ein Value Object mit klarer Wertgleichheit und Invarianten ist.

### 15.2 Schlechtes Domain-Aggregat als Record

```java
public record Order(
    Long id,
    String status,
    List<OrderLine> lines
) {}
```

Das ist kritisch, wenn `Order` ein echtes Domain-Aggregat sein soll. Ein Auftrag hat typischerweise Identität, Zustandsübergänge, Regeln, Berechtigungsbezug und Lifecycle. Dafür ist eine klassische Klasse oft besser geeignet.

Besser:

```java
public class Order {
    private final OrderId id;
    private OrderStatus status;
    private final List<OrderLine> lines = new ArrayList<>();

    public void confirm() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("only draft orders can be confirmed");
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("order must contain at least one line");
        }
        status = OrderStatus.CONFIRMED;
    }
}
```

Regel: Records sind für Value Objects möglich, aber für Aggregate mit Lifecycle nur in Ausnahmefällen geeignet.

---

## 16. Records und JPA

Records dürfen nicht als JPA-Entities verwendet werden.

Grund:

- JPA-Entities müssen einen public oder protected No-Arg-Konstruktor haben.
- Entity-Klassen dürfen nicht final sein.
- Persistente Methoden oder Instanzvariablen dürfen nicht final sein.
- JPA arbeitet häufig mit Proxies, Lazy Loading und Lifecycle-Mechanismen.

Records sind implizit final und ihre Komponenten sind final. Damit passen sie nicht zum klassischen JPA-Entity-Modell.

### 16.1 Fehlerhaft

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public record UserEntity(
    @Id Long id,
    String name
) {}
```

Diese Modellierung ist für JPA-Entities nicht zulässig und nicht in dieser Codebase erlaubt.

### 16.2 Korrekt: Entity als Klasse, DTO als Record

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Long id;

    private String name;

    protected UserEntity() {
        // Required by JPA
    }

    public UserEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

```java
public record UserResponse(
    Long id,
    String name
) {
    public static UserResponse from(UserEntity entity) {
        return new UserResponse(entity.getId(), entity.getName());
    }
}
```

Regel: Persistenzmodell und API-Modell bleiben getrennt. Entities werden nicht direkt nach außen gegeben.

---

## 17. Records und Jackson

Jackson unterstützt Records seit Version 2.12. In modernen Java-/Spring-Boot-Anwendungen funktionieren einfache Records üblicherweise ohne `@JsonCreator`.

### 17.1 Standardfall

```java
public record UserResponse(
    Long id,
    String name,
    String email
) {}
```

### 17.2 Wenn JSON-Namen abweichen

```java
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
    @JsonProperty("user_id") Long id,
    @JsonProperty("display_name") String displayName
) {}
```

### 17.3 Wenn Werte nicht ausgegeben werden dürfen

```java
import com.fasterxml.jackson.annotation.JsonIgnore;

public record InternalUserView(
    Long id,
    String username,
    @JsonIgnore String internalRiskMarker
) {}
```

Achtung: `@JsonIgnore` verhindert JSON-Ausgabe, aber nicht automatisch `toString()`.

Regel: Jackson-Sichtbarkeit und Logging-Sichtbarkeit sind getrennte Themen.

---

## 18. Records und Spring Boot Configuration Properties

Spring Boot unterstützt Constructor Binding mit Records. Wenn ein Record nur einen Konstruktor hat, ist keine zusätzliche `@ConstructorBinding`-Annotation nötig. Bei mehreren Konstruktoren muss der zu verwendende Konstruktor eindeutig gemacht werden.

### 18.1 Beispiel

```java
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
    String senderAddress,
    int maxRetries
) {}
```

### 18.2 Mit Validierung

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
    @NotBlank @Email String senderAddress,
    @Min(0) @Max(10) int maxRetries
) {}
```

Regel: Konfigurationsdaten sollen möglichst unveränderlich sein. Records sind dafür gut geeignet, wenn keine Framework-Einschränkung entgegensteht.

---

## 19. Records und Apache Wicket

In Apache Wicket muss besonders auf Serialisierung und Page State geachtet werden. Wicket speichert Seitenversionen und verwendet dabei Java-Serialisierung. Deshalb muss jedes Objekt, das in einer Page oder Komponente gehalten wird, serialisierbar sein oder über ein detachable model sauber aus dem serialisierten Zustand entfernt werden.

Records implementieren `Serializable` nicht automatisch.

### 19.1 Fehlerhaft

```java
public record UserViewDto(
    Long id,
    String displayName
) {}
```

Wenn dieses Objekt dauerhaft in einer Wicket-Komponente oder Page gehalten wird, kann das problematisch sein, weil `Serializable` fehlt.

### 19.2 Korrekt für kleinen Page-State

```java
import java.io.Serializable;

public record UserViewDto(
    Long id,
    String displayName
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
```

### 19.3 Besser bei größeren Daten

Große Listen, Suchergebnisse, Binärdaten oder komplexe Objektgraphen sollen nicht dauerhaft im Wicket Page State gehalten werden. Hier ist ein detachable model oder ein ID-basiertes Nachladen vorzuziehen.

```java
public record UserSearchCriteria(
    String query,
    int page,
    int size
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
```

Regel für Wicket: Records sind für kleine, serialisierbare View-Daten geeignet. Große Datenmengen und Services gehören nicht in Record-Komponenten, die im Page State landen.

---

## 20. Records und SaaS-Plattformen

In SaaS-Systemen tragen DTOs häufig Tenant-, Account-, Location-, Role- oder Permission-Bezug. Records helfen, Daten klar zu strukturieren. Sie ersetzen aber keine Autorisierung und keine Tenant-Isolation.

### 20.1 Gutes SaaS-DTO

```java
import java.util.UUID;

public record LocationOfferResponse(
    UUID tenantId,
    UUID locationId,
    UUID offerId,
    String title,
    int remainingCapacity
) {}
```

Dieses DTO ist lesbar und klar. Trotzdem muss die Service-Schicht sicherstellen, dass der aufrufende Nutzer diesen Tenant und diese Location sehen darf.

### 20.2 Gefährliches Missverständnis

```java
public record UpdateOfferRequest(
    UUID tenantId,
    UUID offerId,
    String title
) {}
```

Ein `tenantId` im Request ist keine Sicherheitsentscheidung. Ein Angreifer kann diese ID verändern. Der Tenant-Kontext muss aus vertrauenswürdigen Quellen kommen, zum Beispiel aus Authentifizierungs- und Autorisierungskontext, nicht blind aus dem Request Body.

Besser:

```java
public record UpdateOfferRequest(
    UUID offerId,
    String title
) {}
```

Der Tenant wird serverseitig aus dem Security-Kontext ermittelt.

Regel: Ein Record macht Daten strukturiert, aber nicht vertrauenswürdig.

---

## 21. Anti-Pattern-Katalog

| Aspekt | Details/Erklärung | Beispiel | Korrektur |
|---|---|---|---|
| Record als Entity | JPA-Anforderungen passen nicht zu Records. | `@Entity public record User(...)` | Entity als Klasse, DTO als Record |
| Mutable Collection | Inhalt kann nach Konstruktion verändert werden. | `record Page(List<T> items)` | `items = List.copyOf(items)` |
| Array-Komponente | Mutable und problematische Equality. | `record FileDto(byte[] content)` | vermeiden oder defensiv kopieren und Methoden überschreiben |
| Sensitive `toString()` | Geheimnisse landen in Logs. | `record Login(String password)` | `toString()` überschreiben oder Logging verbieten |
| Business-Logik im Konstruktor | Konstruktor führt komplexe Prozesse aus. | Datenbankzugriff im compact constructor | nur lokale Invarianten prüfen |
| Record als Aggregate | Lifecycle und Zustandsübergänge fehlen. | `record Order(status, lines)` | Domain-Klasse verwenden |
| Blindes Tenant-Feld | Request liefert Tenant-ID. | `record Request(UUID tenantId)` | Tenant aus Security-Kontext ableiten |
| Nullable Chaos | Unklare Null-Semantik. | `record User(String name)` ohne Validierung | Pflichtfelder absichern |
| Framework-Binding ignoriert | Framework braucht Setter/No-Arg. | Legacy Binder mit Record | klassische Klasse begründen |
| Riesige DTOs | Record mit 20+ Komponenten. | unübersichtlicher Record-Header | fachlich schneiden oder Modell überdenken |

---

## 22. Größen- und Lesbarkeitsregeln

Ein Record sollte klein und verständlich bleiben.

Richtwerte:

- Bis 5 Komponenten: unkritisch.
- 6 bis 10 Komponenten: fachlich prüfen.
- Mehr als 10 Komponenten: Warnsignal.
- Mehr als 15 Komponenten: nur mit Begründung im Review akzeptieren.

Ein sehr großer Record kann auf folgende Probleme hinweisen:

1. Das DTO bündelt mehrere fachliche Konzepte.
2. Eine API-Grenze ist zu grob.
3. Es fehlt ein verschachteltes Modell.
4. Ein Response enthält mehr Daten als fachlich notwendig.
5. Es entstehen unnötige Exposure-Risiken.

Beispiel für fachliche Zerlegung:

```java
public record UserProfileResponse(
    UserIdentity identity,
    ContactInformation contact,
    AccountStatus status
) {}

public record UserIdentity(
    Long id,
    String displayName
) {}

public record ContactInformation(
    String email,
    String phoneNumber
) {}

public record AccountStatus(
    boolean active,
    String role
) {}
```

Regel: Records sollen Daten strukturieren, nicht unstrukturierte Datenberge kompakt verstecken.

---

## 23. Null-Handling

Records verbieten `null` nicht automatisch. Deshalb muss Null-Semantik explizit entschieden werden.

### 23.1 Pflichtfelder

Pflichtfelder müssen validiert werden.

```java
import java.util.Objects;

public record ProductResponse(
    Long id,
    String name
) {
    public ProductResponse {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
```

### 23.2 Optionale Felder

Optionale Felder sind in API-DTOs bewusst zu modellieren. `Optional` als Record-Komponente ist für JSON-DTOs häufig nicht ideal, weil es Serialisierung, API-Verträge und Client-Verständnis erschweren kann.

Häufig besser:

```java
public record UserResponse(
    Long id,
    String displayName,
    String middleName
) {}
```

Dazu muss die API-Dokumentation klar sagen, ob `middleName` fehlen oder `null` sein darf.

Regel: `null` ist eine API-Entscheidung, kein Zufall.

---

## 24. Bean Validation vs. Konstruktorvalidierung

Bean Validation und Konstruktorvalidierung haben unterschiedliche Stärken.

| Aspekt | Details/Erklärung | Geeignet für |
|---|---|---|
| Bean Validation | Deklarative Validierung an API-Grenzen. Gute Fehlermeldungen durch Frameworks. | Request-DTOs |
| Compact Constructor | Lokale Invarianten und Normalisierung. Wird immer bei Konstruktion ausgeführt. | Value Objects, interne DTOs |
| Service-Validierung | Kontextabhängige Regeln mit Datenbank-, Tenant- oder Berechtigungsbezug. | Business-Regeln |

### 24.1 Gute Kombination

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameLocationRequest(
    @NotBlank
    @Size(max = 120)
    String newName
) {}
```

Service-Regel:

```java
public void renameLocation(TenantContext tenantContext, UUID locationId, RenameLocationRequest request) {
    Location location = locationRepository.findForTenant(tenantContext.tenantId(), locationId)
        .orElseThrow(LocationNotFoundException::new);

    location.rename(request.newName());
}
```

Hier prüft das DTO die Form des Inputs. Der Service prüft Tenant-Zugriff, Existenz und fachliche Operation.

---

## 25. Wither-Pattern

Records sind unveränderlich in Bezug auf ihre Komponentenreferenzen. Wenn eine geänderte Variante benötigt wird, kann eine neue Instanz erzeugt werden.

```java
public record UserResponse(
    Long id,
    String displayName,
    String email
) {
    public UserResponse withDisplayName(String newDisplayName) {
        return new UserResponse(id, newDisplayName, email);
    }
}
```

Wither-Methoden sind nur sinnvoll, wenn sie häufig und verständlich genutzt werden. Sie dürfen nicht dazu führen, dass Records wie mutable Objekte behandelt werden.

Regel: Mehr als zwei oder drei Wither-Methoden sind ein Warnsignal. Prüfe dann, ob ein Builder, Mapper oder eine klassische Klasse besser geeignet ist.

---

## 26. Builder und Records

Records benötigen für einfache DTOs keinen Builder. Ein Builder kann bei großen Request-/Response-Objekten sinnvoll sein, ist aber ein Warnsignal für zu große DTOs.

### 26.1 Nicht nötig

```java
public record RoleResponse(
    String code,
    String label
) {}
```

### 26.2 Möglich bei komplexer Testdatenerzeugung

Für Tests darf ein Test Data Builder verwendet werden, ohne den Produktionsrecord künstlich aufzublähen.

```java
public final class UserResponseTestData {

    private Long id = 1L;
    private String displayName = "Ada Lovelace";
    private String email = "ada@example.org";

    public UserResponseTestData withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserResponse build() {
        return new UserResponse(id, displayName, email);
    }
}
```

Regel: Builder für Records gehören meistens in Tests oder Mapping-Code, nicht automatisch in das DTO selbst.

---

## 27. Mapping-Regeln

DTOs sollen nicht direkt aus Entities nach außen gereicht werden. Records eignen sich gut als explizite API-Grenze.

### 27.1 Mapper-Methode im DTO nur bei kleinen Fällen

```java
public record UserResponse(
    Long id,
    String displayName
) {
    public static UserResponse from(UserEntity entity) {
        return new UserResponse(entity.getId(), entity.getDisplayName());
    }
}
```

### 27.2 Eigener Mapper bei komplexeren Fällen

```java
public final class UserResponseMapper {

    private UserResponseMapper() {
    }

    public static UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
            entity.getId(),
            entity.getDisplayName(),
            entity.getEmail()
        );
    }
}
```

Regel: Mapping darf keine Berechtigungsprüfung ersetzen. Wenn ein Feld nur für bestimmte Rollen sichtbar ist, muss die Service- oder Application-Schicht entscheiden, welches DTO oder welche Felder ausgegeben werden.

---

## 28. Tests für Record-DTOs

Nicht jeder einfache Record braucht eigene Unit Tests. Tests sind aber erforderlich, wenn der Record eigene Validierung, Normalisierung, defensive Kopien, Redaction oder abgeleitete Methoden enthält.

### 28.1 Test für Normalisierung

```java
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserEmailTest {

    @Test
    void normalizesEmail() {
        UserEmail email = new UserEmail("  ADA@EXAMPLE.ORG  ");

        assertThat(email.value()).isEqualTo("ada@example.org");
    }
}
```

### 28.2 Test für defensive Kopie

```java
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResponseTest {

    @Test
    void copiesContentDefensively() {
        List<String> names = new ArrayList<>();
        names.add("Ada");

        PageResponse<String> page = new PageResponse<>(names, 1);
        names.add("Grace");

        assertThat(page.content()).containsExactly("Ada");
    }
}
```

### 28.3 Test für Redaction

```java
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoginRequestTest {

    @Test
    void doesNotExposePasswordInToString() {
        LoginRequest request = new LoginRequest("ada", "secret123");

        assertThat(request.toString()).doesNotContain("secret123");
        assertThat(request.toString()).contains("<redacted>");
    }
}
```

Regel: Ein Record mit eigenem Konstruktor oder überschriebenem `toString()` braucht Tests.

---

## 29. Review-Checkliste

Diese Checkliste ist im Pull Request zu verwenden, wenn neue DTOs eingeführt oder bestehende DTOs geändert werden.

| Aspekt | Details/Erklärung | Review-Frage |
|---|---|---|
| DTO-Eignung | Ist der Typ wirklich primär Datentransport? | Warum ist dies ein DTO und kein Domain-Objekt? |
| Record-Standard | Wird ein einfacher DTO-Typ als Record modelliert? | Falls nein: Warum nicht? |
| Mutable Komponenten | Enthält der Record `List`, `Set`, `Map`, Array oder mutable eigene Typen? | Wird defensiv kopiert? |
| Sensitive Data | Enthält der Record Passwort, Token, Secret, API Key, Session-ID oder vertrauliche Personendaten? | Ist `toString()` geschützt oder Logging verboten? |
| Validierung | Kommen Werte von außen? | Gibt es Bean Validation oder Konstruktorvalidierung? |
| Normalisierung | Müssen Werte getrimmt, case-normalisiert oder kanonisiert werden? | Passiert das kontrolliert und locale-sicher? |
| Null-Semantik | Sind Nullwerte erlaubt? | Ist das dokumentiert oder validiert? |
| Framework-Kompatibilität | Wird der Typ mit Jackson, Spring Boot, Wicket oder JPA verwendet? | Ist die jeweilige Einschränkung geprüft? |
| Tenant-Kontext | Enthält das DTO tenantbezogene Felder? | Werden diese serverseitig abgesichert? |
| API-Exposure | Enthält die Response nur notwendige Felder? | Gibt es Überexposition? |
| Größe | Hat der Record viele Komponenten? | Muss das DTO geschnitten werden? |
| Tests | Hat der Record eigene Logik? | Gibt es Tests für diese Logik? |

---

## 30. Automatisierbare Prüfungen

Diese Richtlinie soll nicht nur gelesen, sondern auch technisch unterstützt werden.

### 30.1 ArchUnit-Idee: DTOs sollen Records sein

Beispielhafte Regelidee:

```java
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.example")
class DtoArchitectureTest {

    @ArchTest
    static final ArchRule dtoClassesShouldBeRecordsOrExplicitExceptions = classes()
        .that().resideInAPackage("..dto..")
        .and().haveSimpleNameEndingWith("Dto")
        .should().beRecords();
}
```

Hinweis: Je nach ArchUnit-Version kann die konkrete API abweichen. Die Regelidee ist verbindlich, die Implementierung wird projektspezifisch validiert.

### 30.2 Semgrep-Idee: sensible Record-Komponenten finden

```yaml
rules:
  - id: java-record-sensitive-component
    message: "Record contains a potentially sensitive component. Check toString(), logging and exposure."
    severity: WARNING
    languages: [java]
    patterns:
      - pattern-regex: 'record\s+\w+\s*\([^)]*(password|token|secret|apiKey|sessionId)[^)]*\)'
```

### 30.3 Review-Gate

Jeder Pull Request mit neuen oder geänderten DTOs muss mindestens eine der folgenden Aussagen erfüllen:

1. Das DTO ist ein Record und erfüllt diese Richtlinie.
2. Das DTO ist bewusst keine Record-Klasse und die Begründung steht im Pull Request.
3. Das DTO enthält mutable oder sensible Komponenten und die Schutzmaßnahme ist sichtbar implementiert.

---

## 31. Migration bestehender JavaBeans zu Records

Migration soll nicht mechanisch und blind erfolgen. Zuerst muss geprüft werden, ob die Klasse wirklich ein DTO ist.

### 31.1 Migrationsablauf

1. Klasse identifizieren.
2. Prüfen, ob sie nur Daten transportiert.
3. Framework-Nutzung prüfen.
4. Setter-Nutzung analysieren.
5. Tests stabilisieren.
6. JavaBean in Record überführen.
7. `getX()`-Aufrufe auf `x()` umstellen.
8. JSON-Serialisierung und Deserialisierung testen.
9. Mapping-Code testen.
10. Logging prüfen.
11. Mutable Komponenten defensiv kopieren.
12. Pull Request mit Begründung und Review-Checkliste öffnen.

### 31.2 Beispielmigration

Vorher:

```java
public class UserDto {
    private final Long id;
    private final String name;

    public UserDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

Nachher:

```java
public record UserDto(
    Long id,
    String name
) {}
```

Aufrufer ändern:

```java
// vorher
userDto.getName();

// nachher
userDto.name();
```

---

## 32. Ausnahmen

Ausnahmen sind erlaubt, aber nicht stillschweigend.

Eine klassische Klasse darf statt eines Records verwendet werden, wenn mindestens einer dieser Gründe vorliegt:

1. Framework benötigt No-Arg-Konstruktor und Setter.
2. Objekt ist eine JPA-Entity.
3. Objekt hat bewusst mutable Zustände.
4. Objekt ist ein Domain-Aggregat mit Lifecycle.
5. Vererbung ist fachlich oder technisch erforderlich.
6. Objekt muss von einem Framework proxyfähig sein.
7. Objekt enthält sensible Daten und eine Klasse mit kontrollierter Darstellung ist klarer.
8. Die Lesbarkeit eines sehr großen Record-Headers wäre schlechter als eine explizite Klasse.

Pull-Request-Begründung:

```markdown
### Ausnahme von QG-JAVA-001

Dieses DTO wird nicht als Record modelliert, weil:

- [ ] Framework-Binding benötigt No-Arg-Konstruktor/Setter
- [ ] JPA-Entity
- [ ] bewusst mutable
- [ ] Domain-Aggregat
- [ ] Proxy-/Vererbungsanforderung
- [ ] sensible Daten / kontrollierte Darstellung
- [ ] anderer Grund: ...

Begründung:
...
```

---

## 33. Definition of Done

Ein DTO erfüllt diese Richtlinie, wenn alle folgenden Punkte erfüllt sind:

1. Es ist als Record modelliert oder hat eine begründete Ausnahme.
2. Es enthält nur fachlich notwendige Komponenten.
3. Pflichtfelder sind validiert.
4. Externe Eingaben werden als nicht vertrauenswürdig behandelt.
5. Mutable Komponenten werden defensiv kopiert oder vermieden.
6. Arrays werden vermieden oder vollständig abgesichert.
7. Sensible Felder werden nicht über `toString()` offengelegt.
8. JPA-Entities werden nicht als Records modelliert.
9. Framework-Kompatibilität wurde geprüft.
10. Tenant- oder Berechtigungsinformationen werden nicht blind aus Request-DTOs vertraut.
11. Eigene Konstruktorlogik ist getestet.
12. Redaction ist getestet, wenn sensible Felder vorhanden sind.
13. Wicket Page-State und Serialisierung sind geprüft, falls das DTO in Wicket-Komponenten gehalten wird.
14. Pull-Request-Review hat die Checkliste sichtbar berücksichtigt.

---

## 34. Entwickler-Entscheidungsbaum

```text
Ist der Typ primär ein Datentransferobjekt?
│
├─ Nein → Kein Record-Standard. Domain-/Framework-Modell prüfen.
│
└─ Ja
   │
   ├─ Muss das Objekt nach Konstruktion verändert werden?
   │  ├─ Ja → Klassische Klasse prüfen.
   │  └─ Nein
   │
   ├─ Ist es eine JPA-Entity oder ein Proxy-Typ?
   │  ├─ Ja → Kein Record.
   │  └─ Nein
   │
   ├─ Enthält es mutable Komponenten?
   │  ├─ Ja → Defensive Kopien oder Design ändern.
   │  └─ Nein
   │
   ├─ Enthält es sensible Daten?
   │  ├─ Ja → toString schützen oder Logging verbieten.
   │  └─ Nein
   │
   ├─ Unterstützt das Framework Record-/Constructor-Binding?
   │  ├─ Nein → Klassische Klasse begründen.
   │  └─ Ja → Record verwenden.
```

---

## 35. Pull-Request-Template-Ergänzung

```markdown
## QG-JAVA-001 — Records for DTOs

- [ ] Neue einfache DTOs wurden als Records modelliert.
- [ ] Abweichungen von Records sind begründet.
- [ ] API-Request-DTOs sind validiert.
- [ ] Mutable Collections werden defensiv kopiert.
- [ ] Arrays in Records wurden vermieden oder abgesichert.
- [ ] Sensible Felder werden nicht über `toString()` offengelegt.
- [ ] DTOs werden nicht als JPA-Entities missbraucht.
- [ ] Tenant-/Security-Kontext wird nicht blind aus Request-Feldern vertraut.
- [ ] Wicket-Serialisierung wurde geprüft, falls relevant.
- [ ] Eigene Record-Logik ist getestet.
```

---

## 36. Minimaler Qualitätsstandard für Codebeispiele im Projekt

Wenn ein Entwickler in Dokumentation, Tests oder Produktivcode ein Record-Beispiel erstellt, muss das Beispiel folgende Mindestqualität erfüllen:

1. Keine ungeprüfte `null`-Normalisierung.
2. Bei `toLowerCase()` immer `Locale.ROOT`, wenn technische Normalisierung gemeint ist.
3. Keine sensiblen Daten im automatisch erzeugten `toString()`.
4. Keine mutable Collection ohne defensive Kopie.
5. Keine Arrays ohne Warnhinweis oder Schutzmaßnahmen.
6. Kein Record als JPA-Entity.
7. Kein komplexer Business-Prozess im compact constructor.
8. Kein blindes Vertrauen in Tenant-/User-IDs aus externen Requests.

---

## 37. Validierungsnotizen

Diese Richtlinie wurde gegen folgende Sachverhalte validiert:

| Aspekt | Validierter Sachverhalt | Quelle |
|---|---|---|
| Java Records | Records wurden mit JEP 395 in JDK 16 finalisiert. | OpenJDK JEP 395 — https://openjdk.org/jeps/395 |
| Record Semantik | `java.lang.Record` beschreibt Records als shallowly immutable transparente Carrier fester Werte. | Oracle Java SE 21 API — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html |
| Record Classes | Record Classes modellieren plain data aggregates mit weniger Zeremonie. | Oracle Java SE 21 Language Guide — https://docs.oracle.com/en/java/javase/21/language/records.html |
| Jackson | Jackson 2.12 führte explizite Unterstützung für `java.lang.Record` ein. | FasterXML Jackson 2.12 Release Notes — https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.12 |
| Spring Boot | Constructor Binding kann mit Records genutzt werden; bei einem Konstruktor ist `@ConstructorBinding` nicht nötig. | Spring Boot Reference Documentation — https://docs.spring.io/spring-boot/reference/features/external-config.html |
| JPA | Entity-Klassen brauchen No-Arg-Konstruktor, dürfen nicht final sein und persistente Methoden/Felder dürfen nicht final sein. | Jakarta Persistence 3.1 Specification — https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html |
| Input Validation | Externe und potenziell nicht vertrauenswürdige Daten sollen früh validiert werden. | OWASP Input Validation Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html |
| Logging | Application Logging muss bewusst gestaltet werden; sensible Daten dürfen nicht unkontrolliert in Logs landen. | OWASP Logging Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html |
| Wicket | Wicket speichert Seitenversionen per Java-Serialisierung; referenzierte Objekte müssen serialisierbar sein oder detachable behandelt werden. | Apache Wicket 10 Reference Guide — https://nightlies.apache.org/wicket/guide/10.x/single.html |

---

## 38. Bezug zu ADR-001

Der zugehörige ADR dokumentiert die Architekturentscheidung:

> Wir verwenden Java Records als Standard für einfache, unveränderliche Datentransferobjekte, sofern keine technischen oder fachlichen Gründe dagegen sprechen.

Diese Guideline operationalisiert die Entscheidung. Sie beschreibt, wie Entwickler Records korrekt einsetzen, welche Fehlerbilder zu vermeiden sind und wie die Entscheidung im Review überprüft wird.

---

## 39. Kurzfassung für Team-Onboarding

Records sind unsere Standardwahl für einfache DTOs. Sie reduzieren Boilerplate und machen Datenstrukturen klarer. Sie sind aber nur shallow immutable. Deshalb müssen Collections defensiv kopiert werden. Arrays sind zu vermeiden. JPA-Entities sind keine Records. Sensitive Felder wie Passwörter oder Tokens dürfen nicht durch automatisch erzeugtes `toString()` in Logs landen. Externe Request-Daten bleiben nicht vertrauenswürdig und müssen validiert werden. In Wicket muss Serialisierung beachtet werden. Jede Abweichung vom Record-Standard braucht eine kurze technische Begründung im Pull Request.

---

## 40. Merksatz

Ein Record ist richtig, wenn er Daten klar und unveränderlich repräsentiert. Ein Record ist falsch, wenn er Framework-Anforderungen ignoriert, sensible Daten offenlegt, mutable Inhalte versteckt oder ein echtes Domain-Modell ersetzt.
