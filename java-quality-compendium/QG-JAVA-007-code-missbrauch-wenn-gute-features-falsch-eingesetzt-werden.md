# QG-JAVA-007 — Code-Missbrauch: Wenn gute Features falsch eingesetzt werden

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-007 |
| Titel | Code-Missbrauch: Wenn gute Features falsch eingesetzt werden |
| Status | Accepted / verbindlicher Standard für lesbaren, wartbaren und sicheren Java-Code in neuen und wesentlich geänderten Codebereichen |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, Spring Boot 3.x, SaaS-Plattformen, Enterprise-Backends, API-Services, Domänenlogik, Integrationscode |
| Java-Baseline | Java 21+ als Kompendium-Standard |
| Kategorie | Code Quality / Anti-Patterns / Wartbarkeit / Security by Design |
| Letzte Validierung | 2026-05-02 |
| Validierte Quellenbasis | Oracle Java SE 21 API zu `Optional`, `Stream` und Reflection; Oracle-Dokumentation zu Pattern Matching; OpenJDK JEP 395, JEP 409 und JEP 441; OWASP Logging Cheat Sheet; OWASP Input Validation Cheat Sheet; OWASP API Security Top 10 2023; Spring Framework Reference Documentation zu Validation und Fehlerbehandlung |
| Technische Beispielvalidierung | Die eigenständigen Java-21-Beispiele ohne externe Framework-Abhängigkeiten wurden syntaktisch mit `javac --release 21` geprüft. Spring-, Bean-Validation-, MapStruct- und OWASP-bezogene Beispiele sind referenzbasiert validiert und benötigen die jeweiligen Projektabhängigkeiten. |
| Verbindlichkeit | Diese Richtlinie gilt verbindlich für neuen Code und für wesentlich geänderte bestehende Codebereiche. Abweichungen sind zulässig, wenn ein konkreter fachlicher, technischer oder architektonischer Grund im Pull Request nachvollziehbar dokumentiert wird. |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie moderne Java-Features und etablierte Sprachmittel verantwortbar eingesetzt werden, ohne Lesbarkeit, Wartbarkeit, Testbarkeit oder Sicherheit zu verschlechtern.

Moderne Java-Features sind wertvoll. Streams, `Optional`, Records, Sealed Classes, Pattern Matching, Reflection, Exceptions und Framework-Abstraktionen können Code deutlich besser machen. Dieselben Features können aber auch Schaden verursachen, wenn sie als Beweis technischer Cleverness statt als Werkzeug für Verständlichkeit eingesetzt werden.

Typische Missbrauchsmuster sind:

1. überlange Stream-Ketten ohne benannte Zwischenschritte.
2. `Optional` als Feldtyp, Parametertyp oder Collection-Ersatz.
3. Reflection als Ersatz für klare Schnittstellen, Mapper oder Domänenmodelle.
4. `instanceof`-Kaskaden als Ersatz für Polymorphismus oder geschlossene Typmodelle.
5. Exceptions als normaler Kontrollfluss.
6. leere `catch`-Blöcke.
7. Records als Ausrede für fehlende Invarianten.
8. Pattern Matching als Vorwand für zu große `switch`-Blöcke.
9. Virtual Threads als Freifahrtschein für unbegrenzte Parallelisierung.
10. Framework-Magie als Ersatz für explizites Design.

Ziel dieser Richtlinie ist nicht, bestimmte Java-Features zu verbieten. Ziel ist, ihren Einsatz an klare Qualitätskriterien zu binden. Ein Feature ist nur dann gut eingesetzt, wenn es den Code einfacher macht, Fehler früher sichtbar macht, fachliche Absicht ausdrückt und im Review verständlich bleibt.

Ein guter Entwickler schreibt nicht den kürzesten Code. Ein guter Entwickler schreibt Code, dessen Absicht unter Produktionsbedingungen noch erkennbar ist.

---

## 2. Kurzregel für Entwickler

Verwende moderne Java-Features nur dann, wenn sie den Code lesbarer, sicherer, testbarer oder fachlich präziser machen. Wenn ein Feature den Code kürzer, aber schwerer verständlich macht, ist es falsch eingesetzt.

Streams sind gut für klare Datenpipelines. `Optional` ist gut für Rückgabewerte, die fehlen können. Reflection ist gut für Frameworks und Infrastruktur, aber schlecht als Ersatz für Modellierung. `instanceof` ist akzeptabel für lokale Typprüfung und geschlossene Datenvarianten, aber schlecht als verstreuter Polymorphismus-Ersatz. Exceptions sind für außergewöhnliche Fehlerzustände gedacht, nicht für erwartbare fachliche Alternativen.

Die Leitfrage im Review lautet:

> Wird hier ein Java-Feature verwendet, weil es die Absicht klarer macht, oder weil es clever aussieht?

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

Java-Code MUSS folgende Regeln erfüllen:

1. Ein Feature MUSS die fachliche Absicht des Codes deutlicher machen.
2. Komplexe Datenverarbeitung MUSS durch sprechende Zwischenschritte, Hilfsmethoden oder kleine Domänentypen verständlich gemacht werden.
3. `Optional` MUSS primär als Rückgabetyp für möglicherweise fehlende Werte verwendet werden.
4. Collections MÜSSEN als leere Collection zurückgegeben werden, wenn kein Ergebnis vorhanden ist; sie DÜRFEN NICHT als `Optional<List<T>>`, `Optional<Set<T>>` oder `Optional<Map<K,V>>` modelliert werden.
5. Public APIs, DTOs und Entities DÜRFEN `Optional` nicht als Feldtyp verwenden.
6. Public Methoden DÜRFEN `Optional` nicht als Standard-Parametertyp verwenden.
7. Reflection DARF NICHT verwendet werden, um fehlende Modellierung, fehlende Schnittstellen oder fehlende Mapper zu kaschieren.
8. Reflection, die Zugriffskontrollen umgeht, MUSS besonders begründet, gekapselt, getestet und im Security Review sichtbar sein.
9. Wiederholte `instanceof`-Kaskaden über denselben Typraum MÜSSEN durch Polymorphismus, Sealed Types oder Pattern Matching mit exhaustivem `switch` ersetzt werden.
10. Exceptions DÜRFEN NICHT als normaler Kontrollfluss für erwartbare fachliche Alternativen verwendet werden.
11. `catch`-Blöcke DÜRFEN NICHT leer sein.
12. Gefangene Exceptions MÜSSEN entweder sinnvoll behandelt, mit Kontext geloggt oder in eine fachlich passende Exception übersetzt und weitergeworfen werden.
13. Logging DARF keine Passwörter, Tokens, Secrets, Session-IDs, vollständige personenbezogene Hochrisikodaten oder vertrauliche Mandantendaten ausgeben.
14. Code-Muster, die statische Analyse, Refactoring oder Tests erschweren, MÜSSEN begründet werden.
15. Cleverness DARF NIE wichtiger sein als Verständlichkeit.

### 3.2 Darf-nicht-Regeln

Java-Code DARF NICHT:

1. Stream-Pipelines als unlesbare Einzeiler erzwingen.
2. verschachtelte `flatMap`-/`map`-/`collect`-Ketten ohne benannte Absicht verwenden.
3. `Optional.get()` ohne vorherige Präsenzprüfung oder ohne sichere Alternative verwenden.
4. `Optional` in DTOs, Entities, JSON-Modellen oder Persistenzmodellen als Feldtyp einsetzen.
5. `Optional` zur Unterscheidung zwischen leerer und nicht vorhandener Collection verwenden.
6. private Felder produktiver Domänenklassen per Reflection auslesen, wenn ein DTO, Mapper, Interface oder eine explizite Methode möglich ist.
7. `setAccessible(true)` als normale Mapping-Technik verwenden.
8. fachliche Entscheidungen über Strings, Feldnamen oder Reflection-Magie verstecken.
9. `Exception`, `RuntimeException` oder `Throwable` breit fangen und verschlucken.
10. `catch (Exception e) {}` oder vergleichbare leere Fehlerbehandlung enthalten.
11. `printStackTrace()` als produktives Fehlerhandling verwenden.
12. Typprüfungen über die Codebase streuen, wenn das Verhalten zum Typ selbst gehört.
13. moderne Sprachfeatures verwenden, um fehlendes Domänendesign zu verdecken.

### 3.3 Sollte-Regeln

Java-Code SOLLTE:

1. einfache Streams für lineare Transformationen verwenden.
2. bei komplexeren Pipelines sprechende Zwischenvariablen oder Hilfsmethoden nutzen.
3. bei mehreren fachlichen Alternativen Sealed Types und Pattern Matching prüfen.
4. erwartbare Fehlerzustände als Result-Typ, validiertes Objekt oder fachliche Rückgabe modellieren.
5. Exceptions für technische Fehler, verletzte Invarianten und unerwartete Zustände verwenden.
6. Mapping explizit, generiert oder über klar geprüfte Mapper durchführen.
7. Framework-Reflection an Infrastrukturgrenzen halten.
8. Tests so schreiben, dass die fachliche Absicht eines Features sichtbar bleibt.
9. Code so formatieren, dass Reviews Unterschiede sinnvoll erkennen können.
10. komplexe Logik lieber in kleinen benannten Methoden ausdrücken als in einem einzigen Ausdruck.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für:

1. Java-21-Code in produktiven Anwendungen.
2. Spring-Boot-Services.
3. Controller-, Service-, Domain-, Mapper- und Integrationscode.
4. SaaS-Plattformen mit Mandantenfähigkeit.
5. API-Backends.
6. Batch- und Worker-Komponenten.
7. Testcode, sofern Lesbarkeit und Wartbarkeit betroffen sind.
8. Code Reviews und Pull Requests.
9. Refactorings bestehender Codebereiche.
10. technische Schulungs- und Onboarding-Unterlagen.

Diese Richtlinie gilt nicht als pauschales Verbot moderner Features. Sie definiert Qualitätsgrenzen für ihren Einsatz. Frameworks wie Spring, Hibernate, Jackson, MapStruct, JUnit oder Mockito dürfen Reflection, Proxies, Annotation Processing oder generierte Artefakte verwenden, sofern dies Teil ihres dokumentierten Mechanismus ist. Anwendungscode darf diese Mechanismen nicht unkontrolliert nachbauen.

---

## 5. Grundprinzip: Feature-Nutzung muss Absicht sichtbar machen

Ein Java-Feature ist gut eingesetzt, wenn es mindestens eines der folgenden Ziele erfüllt:

| Aspekt | Details/Erklärung | Beispiel |
|---|---|---|
| Lesbarkeit | Der Code wird leichter zu verstehen. | `if (value instanceof String s)` statt Cast nach `instanceof`. |
| Korrektheit | Der Compiler kann mehr Fehler erkennen. | Exhaustiver `switch` über sealed Hierarchie. |
| Wartbarkeit | Änderungen betreffen weniger Stellen. | Verhalten als Methode im Interface statt verstreuter `instanceof`-Kaskaden. |
| Testbarkeit | Fachliche Schritte lassen sich isoliert prüfen. | Extrahierte Hilfsmethode statt komplexer Stream-Kette. |
| Security | Risiken werden sichtbar und kontrollierbar. | Kein Logging sensibler Felder in Exceptions oder DTOs. |
| Refactoring-Sicherheit | IDE und Compiler können Änderungen unterstützen. | Mapper mit konkreten Typen statt Reflection über Feldnamen. |

Ein Feature ist schlecht eingesetzt, wenn es eine oder mehrere dieser Eigenschaften verschlechtert.

---

## 6. Missbrauch 1: Stream-Ketten als Beweis von Cleverness

### 6.1 Problem

Die Stream API unterstützt funktionale Operationen über Sequenzen von Elementen. Streams sind besonders geeignet für lineare Transformationen wie Filtern, Mappen, Gruppieren und Reduzieren. Sie werden problematisch, wenn Entwickler komplexe Fachlogik in eine einzige Pipeline pressen.

Ein Stream ist kein Qualitätsmerkmal an sich. Ein Stream ist gut, wenn er eine Datenbewegung klar ausdrückt. Er ist schlecht, wenn jeder Schritt erst im Kopf rekonstruiert werden muss.

### 6.2 Schlechtes Beispiel

```java
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

Map<String, List<String>> productsByUserEmail = users.stream()
    .filter(user -> user.active() && user.age() > 18)
    .flatMap(user -> user.orders().stream()
        .filter(order -> order.status() == OrderStatus.COMPLETED)
        .map(order -> Map.entry(user.email(), order.productName())))
    .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
```

Dieser Code ist nicht falsch, aber er ist schwer zu lesen. Die fachliche Absicht ist nicht sofort sichtbar. Die Pipeline kombiniert Altersprüfung, Aktivitätsprüfung, Order-Filter, Produktprojektion und Gruppierung in einem Ausdruck.

### 6.3 Warum das schlecht ist

1. Es gibt keine benannten Zwischenschritte.
2. Debugging wird unnötig schwer.
3. Breakpoints liegen mitten in Lambdas.
4. Die fachliche Absicht ist nicht explizit.
5. Erweiterungen werden riskanter.
6. Fehlende Tests einzelner Zwischenschritte fallen später auf.
7. Performance-Probleme durch Lazy Loading oder externe Aufrufe in Streams werden schlechter sichtbar.

### 6.4 Gute Anwendung

```java
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

var activeAdultUsers = users.stream()
    .filter(User::active)
    .filter(user -> user.age() > 18)
    .toList();

var productsByUserEmail = activeAdultUsers.stream()
    .flatMap(user -> completedProductEntries(user).stream())
    .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

private List<Map.Entry<String, String>> completedProductEntries(User user) {
    return user.orders().stream()
        .filter(order -> order.status() == OrderStatus.COMPLETED)
        .map(order -> Map.entry(user.email(), order.productName()))
        .toList();
}
```

Die zweite Variante ist länger, aber verständlicher. Sie benennt fachliche Zwischenschritte. Genau das ist der Punkt: Wartbarer Code darf länger sein, wenn er klarer ist.

### 6.5 Noch bessere fachliche Variante

Wenn die Gruppierung ein echtes Fachkonzept ist, sollte sie einen sprechenden Methodennamen bekommen.

```java
Map<String, List<String>> productsByUserEmail = completedProductsByActiveAdultUser(users);

private Map<String, List<String>> completedProductsByActiveAdultUser(List<User> users) {
    return activeAdultUsers(users).stream()
        .flatMap(user -> completedProductEntries(user).stream())
        .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
}

private List<User> activeAdultUsers(List<User> users) {
    return users.stream()
        .filter(User::active)
        .filter(user -> user.age() > 18)
        .toList();
}
```

Jetzt liest sich der Aufrufer wie Fachsprache. Die Implementierung bleibt testbar.

### 6.6 Review-Regel für Streams

Ein Stream ist akzeptabel, wenn er in einem Satz erklärbar ist. Wenn ein Reviewer drei oder mehr fachliche Transformationen in einer Pipeline zählen kann, SOLLTE die Pipeline in benannte Methoden oder Zwischenschritte zerlegt werden.

### 6.7 Besondere SaaS-/Security-Falle

Streams dürfen keine versteckten externen Aufrufe, Datenbankzugriffe oder Berechtigungsentscheidungen enthalten, die im Review unsichtbar werden.

Schlecht:

```java
var visibleOrders = orders.stream()
    .filter(order -> authorizationService.canRead(currentUser, order))
    .filter(order -> tenantService.belongsToCurrentTenant(order.tenantId()))
    .map(orderMapper::toResponse)
    .toList();
```

Das sieht kompakt aus, kann aber pro Order mehrere Service- oder Datenbankaufrufe auslösen. Bei vielen Orders entstehen Performance-, Audit- und Tenant-Isolationsrisiken. Berechtigungs- und Tenant-Filter gehören so modelliert, dass sie möglichst früh und kontrolliert im Query, Repository oder Service erfolgen.

---

## 7. Missbrauch 2: `Optional` als Allzweck-Container

### 7.1 Problem

`Optional<T>` ist ein guter Rückgabetyp, wenn ein Wert fehlen kann und der Aufrufer diesen Fall bewusst behandeln soll. Es ist aber kein allgemeiner Container für Felder, Parameter, Collections oder JSON-Modelle.

Die Java-API beschreibt `Optional` als value-based class. Solche Objekte sollen nicht über Identität, Synchronisierung oder ähnliche Identitätsmechanismen verwendet werden. Für DTOs, Entities und serialisierte Modelle ist `Optional` daher in der Regel ein schlechtes Signal.

### 7.2 Schlechtes Beispiel: Optional als Feldtyp

```java
public class UserProfile {
    private Optional<String> middleName;

    public Optional<String> getMiddleName() {
        return middleName;
    }
}
```

Dieser Code wirkt explizit, verschiebt aber ein Rückgabekonzept in den Objektzustand. Bei JSON-Serialisierung, JPA, Mapping, Bean Validation und Framework-Binding entstehen unnötige Sonderfälle.

### 7.3 Gute Alternative für optionale Felder

```java
public class UserProfile {
    private String middleName;

    public String middleNameOrEmpty() {
        return middleName == null ? "" : middleName;
    }

    public boolean hasMiddleName() {
        return middleName != null && !middleName.isBlank();
    }
}
```

In Spring-/Jakarta-Projekten kann zusätzlich eine geeignete Nullable-Annotation verwendet werden, wenn das Projekt dafür einen einheitlichen Standard definiert.

### 7.4 Schlechtes Beispiel: Optional als Parameter

```java
public void createUser(String name, Optional<String> email) {
    // Aufrufer müssen Optional.of(...) oder Optional.empty() erzeugen.
}
```

Das macht die Aufruferseite unhandlich und löst keine echte Modellierungsfrage. Wenn E-Mail optional ist, sollte dies im Command-Objekt oder über überladene Methoden klarer ausgedrückt werden.

### 7.5 Gute Alternative: Command-Objekt

```java
public record CreateUserCommand(
    String name,
    String email
) {}

public UserId createUser(CreateUserCommand command) {
    // Validierung entscheidet, ob email null, leer oder verpflichtend ist.
    return userCreator.create(command);
}
```

Wenn die E-Mail fachlich wirklich optional ist, muss das Command dies über Validierungsregeln, Dokumentation und API-Vertrag ausdrücken. `Optional` als Parameter ist dafür kein sauberer Ersatz.

### 7.6 Schlechtes Beispiel: Optional.get()

```java
String email = findUser(userId).get().email();
```

Das ist gefährlich. Wenn kein User gefunden wird, entsteht eine `NoSuchElementException`, häufig ohne ausreichenden Fachkontext.

### 7.7 Gute Alternative: sprechende Behandlung

```java
var user = findUser(userId)
    .orElseThrow(() -> new UserNotFoundException(userId));

var email = user.email();
```

Der fehlende User wird jetzt fachlich verständlich.

### 7.8 Schlechtes Beispiel: Optional für Collections

```java
Optional<List<Order>> findOrdersByUserId(UserId userId) {
    return orderRepository.findOrdersByUserId(userId);
}
```

Eine leere Ergebnisliste ist kein fehlender Wert. Sie ist ein gültiges Ergebnis.

### 7.9 Gute Alternative: leere Collection

```java
List<Order> findOrdersByUserId(UserId userId) {
    return orderRepository.findOrdersByUserId(userId);
}
```

Die Methode MUSS garantieren, dass sie niemals `null` zurückgibt.

### 7.10 Review-Regel für Optional

`Optional` ist im Standardfall nur als Rückgabetyp erlaubt. `Optional` als Feld, DTO-Komponente, Entity-Feld, Collection-Wrapper oder Parameter ist im Pull Request zu beanstanden, sofern keine begründete Ausnahme dokumentiert ist.

---

## 8. Missbrauch 3: Reflection statt Design

### 8.1 Problem

Reflection erlaubt zur Laufzeit Zugriff auf Klassen, Methoden, Konstruktoren und Felder. Das ist für Frameworks, Serializer, Dependency Injection, Tests, Migrationstools und Infrastruktur oft notwendig. Im Anwendungscode ist Reflection aber häufig ein Zeichen, dass eine explizite Schnittstelle, ein Mapper, ein Record oder ein Domänentyp fehlt.

Reflection ist nicht verboten. Reflection ist aber ein Werkzeug für klar begrenzte Infrastruktur, nicht für alltägliche Geschäftslogik.

### 8.2 Schlechtes Beispiel

```java
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UserMapper {

    public Map<String, Object> toMap(User user) {
        try {
            Map<String, Object> values = new HashMap<>();

            for (Field field : User.class.getDeclaredFields()) {
                field.setAccessible(true);
                values.put(field.getName(), field.get(user));
            }

            return values;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not map user", e);
        }
    }
}
```

### 8.3 Warum das schlecht ist

1. Kapselung wird umgangen.
2. Der Compiler erkennt Feldnamenänderungen nicht als fachliche Mapping-Änderung.
3. Refactorings werden riskanter.
4. Security-Reviews sehen nicht sofort, welche Daten exponiert werden.
5. Sensible Felder können versehentlich in Maps, Logs oder API-Responses landen.
6. Performance und Zugriffskontrollen sind schwerer einzuschätzen.
7. Framework- und Modulgrenzen können brechen.

### 8.4 Gute Alternative: explizites Mapping

```java
import java.util.Map;

public record UserDto(
    Long id,
    String displayName,
    String email
) {
    public Map<String, Object> toMap() {
        return Map.of(
            "id", id,
            "displayName", displayName,
            "email", email
        );
    }
}
```

Dieses Mapping ist explizit. Es zeigt, welche Felder wirklich ausgegeben werden. Das ist besser für Review, Security und Refactoring.

### 8.5 Gute Alternative: Mapper-Schnittstelle

```java
public interface UserMapper {
    UserDto toDto(UserEntity entity);
}
```

In Spring-Projekten kann diese Schnittstelle manuell implementiert oder mit einem Compile-Time-Mapper wie MapStruct generiert werden. Wichtig ist nicht das konkrete Tool, sondern die Eigenschaft: Das Mapping ist typisiert, überprüfbar und fachlich sichtbar.

### 8.6 Zulässige Reflection-Fälle

Reflection ist zulässig, wenn alle folgenden Bedingungen erfüllt sind:

1. Der Zweck ist Infrastruktur, Framework-Integration, Migration, Testunterstützung oder generische Tooling-Funktion.
2. Der Code ist auf wenige Klassen oder Pakete begrenzt.
3. Der Zugriff ist gekapselt.
4. Es gibt Tests für Feldänderungen, fehlende Felder und Zugriffsfehler.
5. Sensible Daten werden bewusst ausgeschlossen.
6. Die Abweichung ist im Pull Request erkennbar begründet.

### 8.7 Security-Regel für Reflection

Reflection mit Zugriff auf private Member MUSS als Security-relevanter Code betrachtet werden. Besonders kritisch sind Mapper, Debug-Endpunkte, Audit-Exporte, Admin-Werkzeuge, generische JSON-Konverter und Logging-Helfer. Solcher Code kann ungewollt Secrets, Tokens, personenbezogene Daten oder mandantenspezifische Informationen offenlegen.

---

## 9. Missbrauch 4: `instanceof` als schlechter Polymorphismus-Ersatz

### 9.1 Problem

`instanceof` ist nicht grundsätzlich schlecht. Mit Pattern Matching ist es in Java sogar deutlich lesbarer geworden. Problematisch wird `instanceof`, wenn Typprüfungen überall im Code verteilt werden, obwohl das Verhalten fachlich zum Typ gehört.

### 9.2 Schlechtes Beispiel

```java
double calculateDiscount(Customer customer) {
    if (customer instanceof PremiumCustomer) {
        return 0.20;
    }
    if (customer instanceof RegularCustomer) {
        return 0.05;
    }
    if (customer instanceof NewCustomer) {
        return 0.0;
    }
    throw new IllegalStateException("Unknown customer type: " + customer.getClass().getName());
}
```

### 9.3 Warum das schlecht ist

1. Verhalten liegt außerhalb des Typs.
2. Neue Typen erzwingen Änderungen an vielen Stellen.
3. Fehler werden erst zur Laufzeit sichtbar.
4. Die Codebase sammelt ähnliche `if`-Kaskaden.
5. Fachliche Regeln werden zerstreut.

### 9.4 Gute Alternative: Polymorphismus

```java
sealed interface Customer permits PremiumCustomer, RegularCustomer, NewCustomer {
    double discountRate();
}

record PremiumCustomer(String id) implements Customer {
    @Override
    public double discountRate() {
        return 0.20;
    }
}

record RegularCustomer(String id) implements Customer {
    @Override
    public double discountRate() {
        return 0.05;
    }
}

record NewCustomer(String id) implements Customer {
    @Override
    public double discountRate() {
        return 0.0;
    }
}

double calculateDiscount(Customer customer) {
    return customer.discountRate();
}
```

Wenn das Verhalten natürlich zum Typ gehört, ist Polymorphismus die einfachste Lösung.

### 9.5 Gute Alternative: Pattern Matching für geschlossene Datenvarianten

Nicht jedes Verhalten gehört in den Typ. Manchmal ist ein externer `switch` sinnvoll, zum Beispiel bei Darstellung, Mapping oder Serialisierung.

```java
String label(Customer customer) {
    return switch (customer) {
        case PremiumCustomer premium -> "Premium";
        case RegularCustomer regular -> "Regular";
        case NewCustomer fresh -> "New";
    };
}
```

Bei einer sealed Hierarchie kann der Compiler prüfen, ob alle Varianten behandelt werden. Das ist deutlich besser als verstreute offene `instanceof`-Kaskaden.

### 9.6 Entscheidungsregel

Wenn das Verhalten fachlich zur Variante gehört, verwende Polymorphismus. Wenn das Verhalten eine externe Sicht auf geschlossene Datenvarianten ist, verwende sealed types mit exhaustivem Pattern Matching. Wenn die Hierarchie bewusst offen ist, verwende klare Erweiterungspunkte statt verstreuter Typprüfungen.

---

## 10. Missbrauch 5: Exceptions für normalen Kontrollfluss

### 10.1 Problem

Exceptions sind wichtig. Sie beschreiben technische Fehler, verletzte Invarianten, unerwartete Zustände und fachliche Abbrüche, die nicht als normale Rückgabe modelliert werden sollen. Sie werden missbraucht, wenn erwartbare Alternativen über `try`/`catch` gesteuert werden.

### 10.2 Schlechtes Beispiel

```java
boolean isPositiveInteger(String input) {
    try {
        return Integer.parseInt(input) > 0;
    } catch (NumberFormatException e) {
        return false;
    }
}
```

Dieses Muster kann für sehr kleine Hilfsfunktionen akzeptabel wirken, wird aber problematisch, wenn es in hot paths, Batch-Verarbeitung oder massenhafter Validierung eingesetzt wird. Der Code verwendet eine Exception für einen erwartbaren Fall: Die Eingabe ist kein Integer.

### 10.3 Gute Alternative: explizite Validierung

```java
boolean isPositiveInteger(String input) {
    if (input == null || input.isBlank()) {
        return false;
    }
    for (int i = 0; i < input.length(); i++) {
        if (!Character.isDigit(input.charAt(i))) {
            return false;
        }
    }
    return Integer.parseInt(input) > 0;
}
```

Für produktive Validierung sollte projektweit ein konsistenter Validierungsstandard gelten, zum Beispiel Bean Validation, Value Objects oder dedizierte Validatoren.

### 10.4 Gute Alternative: Result-Typ für erwartbare fachliche Fehler

```java
sealed interface EmailValidationResult permits ValidEmail, InvalidEmail {}

record ValidEmail(String value) implements EmailValidationResult {}

record InvalidEmail(String reason) implements EmailValidationResult {}

EmailValidationResult validateEmail(String email) {
    if (email == null || email.isBlank()) {
        return new InvalidEmail("E-Mail darf nicht leer sein");
    }
    if (!email.contains("@")) {
        return new InvalidEmail("E-Mail muss ein @ enthalten");
    }
    return new ValidEmail(email);
}
```

Für echte E-Mail-Validierung ist das Beispiel bewusst vereinfacht. Produktiv muss der projektspezifische Validierungsstandard gelten.

### 10.5 Schlechtes Beispiel: Exception verschlucken

```java
try {
    synchronizeUser(userId);
} catch (Exception e) {
    // nichts
}
```

Das ist verboten. Der Fehler ist unsichtbar. Monitoring, Support, Audit und Datenqualität verlieren die Möglichkeit, den Zustand zu verstehen.

### 10.6 Gute Alternative: behandeln, loggen, übersetzen

```java
try {
    synchronizeUser(userId);
} catch (RemoteSystemUnavailableException e) {
    log.warn("Benutzersynchronisation vorübergehend nicht möglich, userId={}", userId, e);
    retryQueue.enqueue(userId);
} catch (RemoteSystemRejectedUserException e) {
    log.info("Benutzersynchronisation fachlich abgelehnt, userId={}, reason={}", userId, e.reason());
    throw new UserSynchronizationRejectedException(userId, e);
}
```

Der Code unterscheidet technische Wiederholbarkeit von fachlicher Ablehnung.

### 10.7 Logging-Regel

Exceptions MÜSSEN mit fachlichem und technischem Kontext behandelt werden. Der Kontext darf aber keine sensiblen Daten enthalten. Passwörter, Tokens, vollständige Secrets, Session-IDs und ungefilterte Request-Payloads gehören nicht in Logs.

---

## 11. Missbrauch 6: Records ohne Invarianten

### 11.1 Problem

Records reduzieren Boilerplate. Sie ersetzen aber keine fachliche Modellierung. Ein Record, der ungültige Werte akzeptiert, ist nur kompakt falsch.

### 11.2 Schlechtes Beispiel

```java
public record Money(
    java.math.BigDecimal amount,
    String currency
) {}
```

Der Typ sieht fachlich aus, erlaubt aber `null`, negative Beträge, leere Währungscodes und beliebige Währungsstrings.

### 11.3 Gute Alternative

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
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }
}
```

Ein Record für ein Value Object MUSS seine Invarianten absichern. Sonst ist er nur ein Datenbeutel mit moderner Syntax.

---

## 12. Missbrauch 7: Pattern Matching als neuer God Switch

### 12.1 Problem

Pattern Matching for `switch` ist stark, besonders mit sealed types. Es wird aber problematisch, wenn ein riesiger `switch` fachliche Workflows, externe Aufrufe, Datenbankzugriffe und Nebenwirkungen bündelt.

### 12.2 Schlechtes Beispiel

```java
void process(Event event) {
    switch (event) {
        case UserRegistered userRegistered -> {
            userRepository.save(userRegistered.user());
            emailService.sendWelcomeMail(userRegistered.email());
            billingService.createTrial(userRegistered.userId());
            auditService.log(userRegistered);
        }
        case PaymentFailed paymentFailed -> {
            paymentRepository.markFailed(paymentFailed.paymentId());
            notificationService.notifyCustomer(paymentFailed.customerId());
            supportService.createTicket(paymentFailed.paymentId());
        }
    }
}
```

Der `switch` ist exhaustiv, aber trotzdem schlecht. Er ist ein zentraler Workflow-Knoten ohne klare Verantwortungsgrenzen.

### 12.3 Gute Alternative

```java
void process(Event event) {
    switch (event) {
        case UserRegistered userRegistered -> userRegistrationHandler.handle(userRegistered);
        case PaymentFailed paymentFailed -> paymentFailureHandler.handle(paymentFailed);
    }
}
```

Der `switch` verteilt nur noch an passende Handler. Die Fachlogik ist testbar und sauber geschnitten.

### 12.4 Review-Regel

Ein `switch` über Pattern darf fachliche Varianten unterscheiden. Er darf aber nicht zu einer zentralen Workflow-Klasse werden. Wenn ein Case mehr als wenige fachlich zusammenhängende Zeilen enthält, ist ein Handler, eine Methode oder ein polymorphes Verhalten zu prüfen.

---

## 13. Missbrauch 8: Virtual Threads als Freifahrtschein

### 13.1 Problem

Virtual Threads machen wartende I/O-Tasks skalierbarer. Sie machen externe Systeme, Datenbanken und CPUs nicht unbegrenzt skalierbar. Wenn Entwickler wegen Virtual Threads unbegrenzt viele Tasks starten, entstehen Ressourcenprobleme.

### 13.2 Schlechtes Beispiel

```java
try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
    for (var customer : customers) {
        executor.submit(() -> billingClient.charge(customer));
    }
}
```

Der Code ist kurz, kann aber tausende parallele Billing-Aufrufe auslösen. Das gefährdet Rate Limits, externe Systeme, Datenbankverbindungen, Tenant-Fairness und Fehlerbehandlung.

### 13.3 Gute Alternative

```java
try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
    for (var batch : batchesOf(customers, 100)) {
        var futures = batch.stream()
            .map(customer -> executor.submit(() -> billingClient.charge(customer)))
            .toList();

        for (var future : futures) {
            future.get();
        }
    }
}
```

Dieses Beispiel ist bewusst einfach. In produktivem Code gehören zusätzlich Timeouts, Retries, Idempotenz, Backoff, Bulkheads, Monitoring und tenantbezogene Limits dazu.

---

## 14. Security- und SaaS-Relevanz

Code-Missbrauch ist nicht nur ein Lesbarkeitsproblem. Viele Missbrauchsmuster erzeugen Sicherheits- und Betriebsrisiken.

| Aspekt | Details/Erklärung | Beispiel | Risiko |
|---|---|---|---|
| Stream-Missbrauch | Sicherheitsprüfungen werden in Pipelines versteckt. | `filter(authService::canRead)` pro Datensatz | N+1, Audit-Lücken, unklare Tenant-Isolation |
| Optional-Missbrauch | API-Verträge werden unklar. | `Optional<List<T>>` | Mehrdeutigkeit zwischen leer, nicht vorhanden und nicht geladen |
| Reflection-Missbrauch | Private Felder werden ungeprüft exponiert. | generisches `toMap()` | Sensitive Data Exposure |
| Instanceof-Missbrauch | Autorisierungsregeln werden verstreut. | mehrere Typprüfungen in Services | vergessene Fälle, inkonsistente Rechteprüfung |
| Exception-Missbrauch | Fehler werden verschluckt oder falsch klassifiziert. | leerer `catch` | fehlende Logs, fehlende Alarme, Dateninkonsistenz |
| Record-Missbrauch | Fachliche Invarianten fehlen. | `record Money(BigDecimal, String)` | ungültige Zustände im System |
| Pattern-Switch-Missbrauch | zentrale God-Switches entstehen. | Event-Workflow in einem Switch | schlechte Testbarkeit, unklare Verantwortlichkeit |
| Virtual-Thread-Missbrauch | parallele Last wird unkontrolliert erzeugt. | tausende externe Calls | Ressourcenerschöpfung, Noisy Neighbor |

In SaaS-Plattformen ist besonders wichtig: Jeder Code, der Tenant-Kontext, Berechtigungen, externe Aufrufe, Logging oder Datenexport betrifft, muss explizit und reviewbar bleiben. Cleverer Code darf keine Zugriffskontrolle verstecken.

---

## 15. Gute Anwendungsmuster

### 15.1 Streams richtig einsetzen

Streams sind geeignet für:

1. einfache Filter-Map-Collect-Pipelines.
2. lineare Transformationen.
3. Aggregationen ohne komplexe Nebenwirkungen.
4. unveränderliche Ergebnislisten.
5. kleine lokale Datenmengen.

Streams sind ungeeignet für:

1. komplexe Workflows.
2. versteckte Datenbankzugriffe.
3. externe HTTP-Calls pro Element.
4. tief verschachtelte Fachlogik.
5. schwer nachvollziehbare Exception-Behandlung.
6. Mutation außerhalb der Pipeline.

### 15.2 Optional richtig einsetzen

`Optional` ist geeignet für:

1. Rückgabewerte aus Suchmethoden.
2. bewusst fehlende Einzelwerte.
3. klare Aufruferpflicht zur Behandlung des Fehlens.

`Optional` ist ungeeignet für:

1. Felder.
2. DTO-Komponenten.
3. Entity-Felder.
4. Parameter in öffentlichen APIs.
5. Collections.
6. Serialisierungsmodelle.
7. Synchronisierung oder Identitätslogik.

### 15.3 Reflection richtig einsetzen

Reflection ist geeignet für:

1. Framework-Infrastruktur.
2. Annotation Processing.
3. Testtools.
4. Migrationstools.
5. generische technische Bibliotheken.

Reflection ist ungeeignet für:

1. Alltagsmapping.
2. API-Response-Erzeugung.
3. Logging sensibler Objekte.
4. fachliche Entscheidungen.
5. Umgehung von Kapselung ohne zwingenden Grund.

### 15.4 Exceptions richtig einsetzen

Exceptions sind geeignet für:

1. verletzte technische Annahmen.
2. verletzte fachliche Invarianten.
3. externe Systemfehler.
4. transaktionale Abbrüche.
5. nicht sinnvoll lokal behandelbare Fehler.

Exceptions sind ungeeignet für:

1. normale boolesche Entscheidungen.
2. häufige Validierungszweige in Massendaten.
3. Schleifensteuerung.
4. stille Ignorierung.
5. pauschale Fehlerklassifikation.

---

## 16. Schlechte Anwendungsmuster und Anti-Patterns

### 16.1 Clever One-Liner

Ein Ausdruck macht zu viel und hat keine benannten Zwischenschritte.

Korrektur: In sprechende Methoden, lokale Variablen oder kleine Typen zerlegen.

### 16.2 Optional Everywhere

`Optional` wird als allgemeine Abwesenheitsmarkierung für alles verwendet.

Korrektur: `Optional` nur als Rückgabetyp für fehlbare Einzelwerte verwenden.

### 16.3 Reflection Mapper

Private Felder werden generisch ausgelesen und in Maps, JSON oder Logs geschrieben.

Korrektur: explizites Mapping, DTOs, Interfaces oder generierte Mapper verwenden.

### 16.4 Type Check Forest

Viele `instanceof`-Prüfungen über dieselbe Hierarchie sind im Code verteilt.

Korrektur: Polymorphismus oder sealed types mit exhaustivem `switch` einsetzen.

### 16.5 Swallowed Exception

Ein Fehler wird gefangen und ignoriert.

Korrektur: behandeln, loggen, übersetzen oder bewusst in einen Result-Zustand überführen.

### 16.6 Magic Framework Escape

Framework-Funktionen werden genutzt, um Designentscheidungen zu umgehen.

Korrektur: explizite Architekturgrenze definieren und Framework-Magie auf Adapter- oder Infrastrukturcode begrenzen.

---

## 17. Review-Checkliste

Ein Pull Request erfüllt diese Richtlinie nur, wenn folgende Fragen zufriedenstellend beantwortet werden können:

1. Macht das verwendete Java-Feature den Code wirklich verständlicher?
2. Ist die fachliche Absicht des Codes auf den ersten Blick erkennbar?
3. Gibt es überlange Stream-Ketten?
4. Haben komplexe Zwischenschritte sprechende Namen?
5. Enthalten Streams versteckte externe Calls, Datenbankzugriffe oder Berechtigungsprüfungen?
6. Wird `Optional` nur als Rückgabetyp für fehlbare Einzelwerte verwendet?
7. Gibt es `Optional` als Feld, DTO-Komponente, Entity-Feld, Collection-Wrapper oder öffentlichen Parameter?
8. Wird `Optional.get()` unsicher verwendet?
9. Gibt es Reflection im Anwendungscode?
10. Wird durch Reflection private Kapselung umgangen?
11. Werden sensible Felder durch generisches Mapping oder Logging sichtbar?
12. Gibt es verstreute `instanceof`-Kaskaden?
13. Gehört das Verhalten eigentlich in den Typ selbst?
14. Wäre eine sealed Hierarchie mit exhaustivem `switch` passender?
15. Werden Exceptions als normaler Kontrollfluss verwendet?
16. Gibt es leere oder zu breite `catch`-Blöcke?
17. Werden Exceptions mit ausreichendem Kontext, aber ohne sensible Daten geloggt?
18. Werden fachliche Fehler klar von technischen Fehlern getrennt?
19. Werden moderne Java-Features zur Klarheit oder zur Cleverness eingesetzt?
20. Ist der Code für einen neuen Entwickler im Team nachvollziehbar?

---

## 18. Automatisierbare Prüfungen

Nicht alle Regeln dieser Guideline sind vollständig automatisierbar. Dennoch können Tools viele Missbrauchsmuster sichtbar machen.

### 18.1 Mögliche Checkstyle-/PMD-/Error-Prone-Regeln

1. Markiere `Optional` als Feldtyp.
2. Markiere `Optional` als Parametertyp in öffentlichen Methoden.
3. Markiere `Optional.get()`.
4. Markiere leere `catch`-Blöcke.
5. Markiere `catch (Exception e)` ohne Re-Throw oder spezifische Behandlung.
6. Markiere `printStackTrace()`.
7. Markiere Methoden mit sehr hoher kognitiver Komplexität.
8. Markiere überlange Lambda-Ausdrücke.
9. Markiere Methoden mit zu vielen Stream-Operationen.
10. Markiere `setAccessible(true)` außerhalb freigegebener Infrastrukturpakete.

### 18.2 Mögliche ArchUnit-Regeln

```java
// Beispielidee, nicht als vollständige produktive Regel zu verstehen:
// Klassen in ..domain.. dürfen nicht auf java.lang.reflect zugreifen.
// Klassen in ..api.. dürfen keine Optional-Felder enthalten.
// Klassen in ..service.. dürfen keine leeren catch-Blöcke enthalten.
```

### 18.3 Mögliche Semgrep-Regeln

1. Suche nach `Optional<$T> $field;`.
2. Suche nach `.get()` auf Optional.
3. Suche nach `catch (Exception $E) { }`.
4. Suche nach `$FIELD.setAccessible(true)`.
5. Suche nach `printStackTrace()`.
6. Suche nach Logger-Aufrufen mit Parameternamen `password`, `token`, `secret`, `apiKey` oder `authorization`.

### 18.4 CI-Gate

Neue Verstöße gegen diese Richtlinie SOLLTEN im Pull Request sichtbar gemacht werden. Harte Blocker sind insbesondere:

1. leere `catch`-Blöcke.
2. unsicheres Logging sensibler Daten.
3. Reflection mit `setAccessible(true)` in Fachlogik.
4. `Optional.get()` ohne begründeten Kontext.
5. Entity-/DTO-Modelle mit `Optional`-Feldern.
6. neue God-Switches oder God-Streams in sicherheitskritischen Bereichen.

---

## 19. Migration bestehender Codebereiche

Bestehende Missbrauchsmuster werden nicht blind in einem Big-Bang refaktoriert. Migration erfolgt risikoorientiert.

### 19.1 Priorisierung

Zuerst werden Codebereiche refaktoriert, die:

1. sicherheitsrelevante Entscheidungen enthalten.
2. Tenant-Isolation betreffen.
3. externe Schnittstellen bedienen.
4. produktionsrelevante Fehler verschlucken.
5. sensible Daten loggen oder exportieren.
6. häufig geändert werden.
7. bereits bekannte Bugs erzeugt haben.
8. hohe kognitive Komplexität besitzen.

### 19.2 Vorgehen

1. Bestehendes Verhalten durch Tests absichern.
2. Missbrauchsmuster identifizieren.
3. Refactoring-Ziel festlegen.
4. Kleine Schritte durchführen.
5. Fachliche Namen einführen.
6. Reflection durch explizites Mapping ersetzen.
7. `Optional`-Felder und -Parameter entfernen.
8. Exceptions fachlich klassifizieren.
9. Logging prüfen und sensible Daten entfernen.
10. Code Review mit dieser Guideline durchführen.

### 19.3 Keine kosmetischen Refactorings ohne Nutzen

Refactoring ist kein Selbstzweck. Wenn ein altes Muster stabil, isoliert und ungefährlich ist, kann es warten. Wenn ein Muster Security, Tenant-Isolation, Datenqualität, Fehlersuche oder Änderbarkeit gefährdet, hat es Priorität.

---

## 20. Ausnahmen

Abweichungen sind erlaubt, wenn sie konkret begründet werden.

Zulässige Ausnahmen können sein:

1. Reflection in Framework-, Test- oder Migrationstools.
2. `Optional` als Parameter in sehr kleinen privaten Hilfsmethoden, wenn der Nutzen klar ist und keine API-Grenze betroffen ist.
3. Exception-basierte Validierung durch etablierte Bibliotheken, wenn der Fehler selten ist und nicht als Massenkontrollfluss genutzt wird.
4. Komplexere Streams in lokal begrenztem, gut getestetem Datenverarbeitungscode.
5. `instanceof` bei Integration mit externen Bibliotheken oder offenen Hierarchien.
6. Pattern Matching in Mappern, wenn sealed types nicht möglich sind.

Jede Ausnahme MUSS im Pull Request nachvollziehbar begründet werden, wenn sie öffentliche APIs, Security, Tenant-Isolation, Persistenzmodelle oder zentrale Geschäftslogik betrifft.

---

## 21. Definition of Done

Ein Codebereich erfüllt diese Richtlinie, wenn:

1. moderne Java-Features die Absicht des Codes verbessern.
2. komplexe Stream-Pipelines verständlich benannt oder zerlegt sind.
3. `Optional` nicht als Feld, DTO-Komponente, Entity-Feld oder Collection-Wrapper verwendet wird.
4. `Optional.get()` nicht unsicher verwendet wird.
5. Reflection nicht als Ersatz für Mapping, Schnittstellen oder Domänendesign genutzt wird.
6. Reflection-Zugriffe auf private Member begründet, gekapselt und getestet sind.
7. wiederholte Typprüfungen durch Polymorphismus oder sealed types ersetzt wurden, sofern fachlich sinnvoll.
8. Exceptions nicht als normaler Kontrollfluss missbraucht werden.
9. leere `catch`-Blöcke nicht vorkommen.
10. Fehlerbehandlung fachlichen und technischen Kontext enthält.
11. Logs keine sensiblen Daten enthalten.
12. Security- und Tenant-Regeln nicht in cleveren Pipelines versteckt sind.
13. kritische Missbrauchsmuster durch Review oder Tooling erkannt werden können.
14. der Code für neue Teammitglieder nachvollziehbar bleibt.
15. Abweichungen im Pull Request begründet sind.

---

## 22. Entscheidungsbaum für Entwickler

### 22.1 Stream oder Schleife?

Verwende einen Stream, wenn die Verarbeitung linear, frei von Seiteneffekten und leicht lesbar ist. Verwende eine Schleife oder benannte Methoden, wenn komplexe Verzweigungen, mehrere Nebenwirkungen, Fehlerbehandlung oder Debugging im Vordergrund stehen.

### 22.2 Optional oder null?

Verwende `Optional` als Rückgabetyp, wenn ein einzelner Wert fehlen kann. Verwende eine leere Collection für fehlende Sammlungsergebnisse. Verwende projektweit definierte Nullable-Konventionen für interne optionale Felder. Verwende kein `Optional` in DTOs und Entities.

### 22.3 Reflection oder Mapper?

Verwende einen Mapper, wenn Felder fachlich bekannt sind. Verwende Reflection nur für technische Infrastruktur, wenn der dynamische Zugriff wirklich Teil des Problems ist.

### 22.4 Instanceof, switch oder Polymorphismus?

Verwende Polymorphismus, wenn das Verhalten zum Typ gehört. Verwende sealed switch, wenn eine externe Sicht auf eine geschlossene Variantenmenge benötigt wird. Verwende `instanceof` nur lokal und begrenzt.

### 22.5 Exception oder Result?

Verwende Result-Typen oder Validierungsobjekte für erwartbare fachliche Alternativen. Verwende Exceptions für unerwartete, transaktional relevante oder nicht lokal sinnvoll behandelbare Fehler.

---

## 23. Quellen und weiterführende Literatur

1. Oracle Java SE 21 API — `java.util.Optional`.
2. Oracle Java SE 21 API — `java.util.stream.Stream` und `java.util.stream` Package Summary.
3. Oracle Java SE 21 API — `java.lang.reflect` Package Summary.
4. Oracle Java SE 21 API — Value-based Classes.
5. OpenJDK JEP 395 — Records.
6. OpenJDK JEP 409 — Sealed Classes.
7. OpenJDK JEP 441 — Pattern Matching for `switch`.
8. Oracle Java SE 21 Language Guide — Pattern Matching.
9. OWASP Logging Cheat Sheet.
10. OWASP Input Validation Cheat Sheet.
11. OWASP API Security Top 10 2023 — Broken Object Property Level Authorization und Unrestricted Resource Consumption.
12. Spring Framework Reference Documentation — Validation, Data Binding und Error Handling.
13. Joshua Bloch, Effective Java, 3rd Edition — Empfehlungen zu `Optional`, Exceptions und API-Design.
14. Robert C. Martin, Clean Code — Lesbarkeit, kleine Funktionen und sprechende Namen.
15. Martin Fowler — Refactoring und Code Smells.

