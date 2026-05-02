# QG-JAVA-009 — Architekturentscheidungen im Quellcode sichtbar machen

## 0. Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-009 |
| Titel | Architekturentscheidungen im Quellcode sichtbar machen |
| Status | Accepted / verbindlicher Standard für neue architekturrelevante Java-Codebestandteile |
| Sprache | Deutsch |
| Java-Baseline | Java 21+ |
| Spring-Baseline | Spring Boot 3.x, sofern Spring-Beispiele verwendet werden |
| Kategorie | Clean Code / Architekturkommunikation / Governance im Code |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur, DevSecOps |
| Zweck | Diese Richtlinie beschreibt, wie wichtige Architektur- und Qualitätsentscheidungen im Quellcode sichtbar gemacht werden, ohne den Code mit Kommentaren zu überladen. Ziel ist, dass Entwickler die lokale Begründung einer ungewöhnlichen oder verbindlichen Implementierung direkt an der relevanten Code-Stelle verstehen. |
| Verbindlichkeit | Architekturrelevante Code-Stellen MÜSSEN ihre Begründung entweder durch klare Benennung, Javadoc, eine Entscheidungsreferenz-Annotation, eine Paketdokumentation oder eine automatisierte ArchUnit-Regel sichtbar machen. Nicht jede Klasse benötigt eine Referenz. Referenzen sind dort Pflicht, wo eine Entscheidung sonst leicht rückgängig gemacht oder falsch interpretiert würde. |
| Prüfstatus | Fachlich geprüft gegen Java-21-Annotationsmodell, ArchUnit-1.4.x-Dokumentation, Spring-Transaktionsevents und OWASP-Logging-Grundsätze. |
| Technische Validierung | Die reinen Java-21-Beispiele für Annotationen und grundlegende Nutzung wurden mit `javac --release 21` syntaktisch geprüft. Framework-Beispiele mit Spring und ArchUnit sind als umsetzbare Muster formuliert und müssen im Zielprojekt mit den konkret verwendeten Dependencies kompiliert werden. |
| Letzte Aktualisierung | 2026-05-02 |

---

## 1. Zweck dieser Richtlinie

Architekturentscheidungen und Qualitätsregeln verlieren an Wirkung, wenn sie nur in separaten Dokumenten stehen. Entwickler arbeiten täglich im Code, nicht dauerhaft im Dokumentationsordner. Wenn eine Entscheidung im Code nicht sichtbar ist, wird sie später oft unbeabsichtigt verletzt: Ein `sealed interface` wird durch eine offene Hierarchie ersetzt, ein synchroner Service wird wieder mit unnötigem `@Async` erweitert, eine Entity wird versehentlich als API-Response zurückgegeben oder eine bewusst gewählte Domänenklasse wird wieder zu einem Datenbehälter mit Settern reduziert.

Diese Richtlinie legt fest, wie wichtige Entscheidungen im Java-Quellcode sichtbar, wartbar und prüfbar gemacht werden. Sie ist keine Aufforderung, jede Zeile zu kommentieren. Sie definiert ein kontrolliertes System aus sprechenden Namen, knappen Warum-Kommentaren, Javadoc, Paketdokumentation, Entscheidungsreferenz-Annotationen und ArchUnit-Regeln.

Das Ziel ist nicht mehr Dokumentation. Das Ziel ist bessere Entscheidungsstabilität im Code.

---

## 2. Kurzregel für Entwickler

Kommentiere nicht, was der Code offensichtlich tut. Mache sichtbar, warum ein Codeabschnitt bewusst so gestaltet wurde, wenn diese Entscheidung für Architektur, Sicherheit, SaaS-Betrieb, Performance, Transaktionsverhalten oder Wartbarkeit relevant ist.

Eine Entscheidungsreferenz ist dann erforderlich, wenn ein späterer Entwickler die Stelle ohne Kontext wahrscheinlich „vereinfachen“, umbauen oder entfernen würde, obwohl genau diese Konstruktion eine bewusste Qualitätsentscheidung schützt.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- zentrale Domänenmodelle,
- geschlossene Typfamilien mit `sealed interface` oder `sealed class`,
- Services mit wichtigen Transaktionsgrenzen,
- sicherheitsrelevante Validierungs- und Autorisierungslogik,
- Mandantenisolierung in SaaS-Systemen,
- bewusste Entscheidungen gegen bestimmte Framework-Muster,
- öffentliche APIs und Integrationsgrenzen,
- Architekturschichten und Paketgrenzen,
- Infrastrukturentscheidungen, die im Code leicht missverstanden werden können,
- Stellen mit absichtlich kontraintuitivem Code.

Diese Richtlinie gilt nicht für:

- triviale Getter, Setter, Records und DTOs ohne besondere Bedeutung,
- offensichtliche Implementierungsdetails,
- temporäre Kommentare,
- auskommentierten Code,
- Kommentare, die nur wiederholen, was der Code bereits sagt,
- Kommentare, die schlechten Code rechtfertigen, statt ihn zu verbessern.

---

## 4. Technischer Hintergrund

### 4.1 Kommentare sind kein Ersatz für guten Code

Guter Code erklärt sich zuerst über Namen, Typen, Struktur und Tests. Kommentare sind nur dann sinnvoll, wenn sie zusätzliche Entscheidungsinformation liefern. Ein Kommentar wie `// addiere alle Preise` ist wertlos, wenn darunter `items.stream().map(Item::price).reduce(...)` steht. Der Code sagt bereits, was passiert.

Ein hilfreicher Kommentar erklärt dagegen, warum eine Entscheidung getroffen wurde, welche Alternative bewusst nicht gewählt wurde oder welche fachliche Eigenschaft geschützt wird.

### 4.2 Entscheidungswissen altert schneller als Code

Architekturentscheidungen werden häufig in Workshops, Reviews oder Dokumenten getroffen. Im Quellcode ist später nur noch das Ergebnis sichtbar. Wenn die Begründung fehlt, entsteht ein Risiko: Der nächste Entwickler sieht eine ungewöhnliche Struktur, hält sie für überkompliziert und entfernt sie. Dadurch verschwindet nicht nur Code, sondern Entscheidungswissen.

Diese Richtlinie sorgt dafür, dass wichtige Entscheidungen an der Stelle sichtbar bleiben, an der sie wirken.

### 4.3 Annotationen: `SOURCE`, `CLASS` und `RUNTIME`

Für Entscheidungsreferenzen ist die Retention-Policy entscheidend. `RetentionPolicy.SOURCE` wird vom Compiler verworfen. Solche Annotationen sind nicht im Bytecode sichtbar. ArchUnit analysiert Java-Bytecode; wenn ArchUnit eine Annotation prüfen soll, reicht `SOURCE` daher nicht aus. Für prüfbare Architekturreferenzen ohne Runtime-Reflection-Overhead ist `RetentionPolicy.CLASS` der bessere Standard. Die Annotation wird in der `.class`-Datei gespeichert, aber nicht zwingend zur Laufzeit durch die JVM reflektierbar gehalten.

`RetentionPolicy.RUNTIME` ist nur nötig, wenn produktiver Code die Annotation zur Laufzeit per Reflection auswerten muss. Für diese Richtlinie ist das normalerweise nicht erforderlich.

---

## 5. Verbindlicher Standard

### 5.1 Grundsatz

Architekturrelevante Entscheidungen MÜSSEN im Code mindestens auf eine der folgenden Arten sichtbar sein:

1. durch sprechende Typen, Methoden und Paketnamen,
2. durch kurzen Warum-Kommentar,
3. durch Javadoc an öffentlichen oder zentralen APIs,
4. durch eine Entscheidungsreferenz-Annotation,
5. durch `package-info.java`,
6. durch ArchUnit-Regeln,
7. durch Tests, die das beabsichtigte Verhalten schützen.

Je wichtiger und risikoreicher eine Entscheidung ist, desto stärker muss sie verankert werden. Für zentrale Architekturregeln reicht ein Kommentar nicht aus. Solche Regeln sollen zusätzlich automatisiert geprüft werden.

### 5.2 Standard-ID-Format

Für dieses Kompendium werden Qualitätsrichtlinien mit folgender ID referenziert:

```text
QG-JAVA-XXX
```

Beispiele:

```text
QG-JAVA-001  Records für DTOs
QG-JAVA-002  Sealed Classes für geschlossene Domänentypen
QG-JAVA-004  Virtual Threads für I/O-lastige Nebenläufigkeit
QG-JAVA-006  Spring Boot Serviceschicht
QG-JAVA-008  Objektorientierung richtig anwenden
```

Wenn ein Projekt zusätzlich klassische Architecture Decision Records führt, dürfen auch IDs wie `ADR-004` referenziert werden. Innerhalb dieses Kompendiums ist jedoch `QG-JAVA-XXX` das bevorzugte Referenzformat.

### 5.3 Keine Referenz ohne lokale Begründung

Eine ID allein reicht nicht aus. Der Entwickler muss an der Code-Stelle verstehen, warum die Regel genau dort gilt.

Schlecht:

```java
// QG-JAVA-004
var user = userRepository.findById(userId).orElseThrow();
```

Gut:

```java
// QG-JAVA-004: Kein CompletableFuture hier. Der Aufruf ist I/O-lastig,
// Virtual Threads sind aktiviert, und synchroner Code bleibt dadurch lesbar.
var user = userRepository.findById(userId).orElseThrow();
```

---

## 6. Gute Anwendung

### 6.1 Warum-Kommentar statt Was-Kommentar

Schlecht:

```java
// Addiert alle Preise zusammen.
BigDecimal total = items.stream()
    .map(Item::price)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

Dieser Kommentar hat keinen Wert. Er wiederholt nur den Code.

Besser:

```java
// QG-JAVA-007: Kein parallelStream(). Die Reihenfolge der Preisverarbeitung
// muss für Audit und Rundungsanalyse deterministisch nachvollziehbar bleiben.
BigDecimal total = items.stream()
    .map(Item::price)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

Dieser Kommentar erklärt eine bewusste Entscheidung: kein `parallelStream()`, obwohl Parallelisierung scheinbar naheliegen könnte.

### 6.2 Entscheidungsreferenz-Annotation definieren

Für zentrale Klassen, Methoden, Felder, Konstruktoren und Pakete SOLLTE eine gemeinsame Annotation verwendet werden. Der Name `DecisionRef` ist bewusst allgemeiner als `ADR`, weil diese Richtlinie sowohl Qualitätsrichtlinien als auch Architekturentscheidungen referenzieren kann.

```java
package com.example.architecture;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Verknüpft eine Code-Stelle mit einer dokumentierten Architektur- oder Qualitätsentscheidung.
 *
 * <p>Die Annotation erklärt nicht, was der Code tut, sondern warum eine bestimmte
 * Struktur, Regel oder Einschränkung an dieser Stelle bewusst gilt.
 */
@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.TYPE,
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.CONSTRUCTOR,
    ElementType.PACKAGE
})
@Documented
@Repeatable(DecisionRefs.class)
public @interface DecisionRef {

    /**
     * Stabile Entscheidungs-ID, zum Beispiel "QG-JAVA-006" oder "ADR-004".
     */
    String id();

    /**
     * Lokale Begründung, warum die referenzierte Entscheidung genau hier gilt.
     */
    String reason();

    /**
     * Optional: relevante Konsequenz für Pflege, Review oder Betrieb.
     */
    String consequence() default "";
}
```

```java
package com.example.architecture;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.TYPE,
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.CONSTRUCTOR,
    ElementType.PACKAGE
})
@Documented
public @interface DecisionRefs {
    DecisionRef[] value();
}
```

Wichtig: `RetentionPolicy.CLASS` ist hier bewusst gewählt. So kann die Referenz von Bytecode-basierten Werkzeugen wie ArchUnit ausgewertet werden, ohne dass produktiver Runtime-Code sie reflektieren muss.

### 6.3 Annotation auf einer geschlossenen Domänentypfamilie

```java
import com.example.architecture.DecisionRef;
import java.time.Instant;

@DecisionRef(
    id = "QG-JAVA-002",
    reason = "PaymentResult ist eine geschlossene Ergebnisfamilie. "
           + "Alle Varianten sind zur Compile-Zeit bekannt und sollen exhaustiv verarbeitet werden.",
    consequence = "Neue Varianten müssen alle switch-Auswertungen sichtbar zum Kompilieren zwingen."
)
public sealed interface PaymentResult permits Success, Failure, Pending {
}

record Success(String transactionId) implements PaymentResult {
}

record Failure(String reason, int errorCode) implements PaymentResult {
}

record Pending(Instant since) implements PaymentResult {
}
```

Die Annotation erklärt hier nicht Java-Syntax. Sie erklärt, warum diese Typfamilie geschlossen ist.

### 6.4 Annotation auf einer Service-Methode

```java
import com.example.architecture.DecisionRef;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @DecisionRef(
        id = "QG-JAVA-006",
        reason = "Lesende Query-Methode mit klarer Transaktionsgrenze. "
               + "Kein Entity-Graph außerhalb der Serviceschicht exponieren.",
        consequence = "Controller erhalten DTOs, keine JPA-Entities."
    )
    @Transactional(readOnly = true)
    public List<OrderSummaryDto> findSummariesFor(UserId userId) {
        return orderRepository.findSummariesByUserId(userId);
    }
}
```

Die Entscheidung ist hier wichtig, weil spätere Entwickler sonst versucht sein könnten, direkt Entities auszugeben oder die Transaktionsgrenze zu entfernen.

### 6.5 Mehrere Entscheidungen an einer Stelle

```java
import com.example.architecture.DecisionRef;
import java.util.List;

@DecisionRef(
    id = "QG-JAVA-001",
    reason = "DTO ist ein unveränderlicher Datenträger und wird als Record modelliert."
)
@DecisionRef(
    id = "QG-JAVA-007",
    reason = "Kein Optional<List>. Eine leere Liste ist das korrekte Leer-Signal."
)
public record OrderSearchResult(
    List<OrderSummaryDto> orders,
    int totalCount
) {
    public OrderSearchResult {
        orders = List.copyOf(orders);
    }
}
```

Mehrfachreferenzen sind erlaubt, aber sparsam zu verwenden. Wenn eine Klasse fünf oder mehr Entscheidungsreferenzen braucht, ist sie oft zu breit verantwortlich oder das Team versucht, Dokumentation durch Annotationen zu ersetzen.

---

## 7. Falsche Anwendung und Anti-Patterns

### 7.1 Kommentar erklärt nur den offensichtlichen Code

Schlecht:

```java
// Prüft, ob der User aktiv ist.
if (user.isActive()) {
    sendNotification(user);
}
```

Besser ist kein Kommentar oder eine sprechendere Methode.

```java
if (user.canReceiveNotifications()) {
    sendNotification(user);
}
```

Wenn wirklich ein besonderer Grund existiert, dann gehört dieser Grund in den Kommentar.

```java
// QG-JAVA-006: Benachrichtigung nur nach fachlicher Opt-in-Prüfung;
// technische Aktivität des Accounts allein reicht nicht aus.
if (user.canReceiveNotifications()) {
    sendNotification(user);
}
```

### 7.2 Kommentar als Entschuldigung für schlechten Code

Schlecht:

```java
// Komplex, aber funktioniert. Nicht anfassen.
var result = a.stream().flatMap(x -> b(x).stream()).filter(y -> c(y)).map(z -> d(z)).toList();
```

Ein solcher Kommentar schützt keinen Qualitätsstandard. Er blockiert Verbesserung. Besser ist die Zerlegung in benannte Schritte.

```java
var eligibleItems = findEligibleItems(a);
var transformedItems = transformForExport(eligibleItems);
```

### 7.3 Entscheidungsreferenz ohne Begründung

Schlecht:

```java
@DecisionRef(id = "QG-JAVA-008", reason = "")
public class Order {
}
```

Die leere Begründung macht die Annotation wertlos. Die ID allein zwingt den nächsten Entwickler, das Dokument zu öffnen, ohne zu wissen, was lokal relevant ist.

Gut:

```java
@DecisionRef(
    id = "QG-JAVA-008",
    reason = "Order schützt ihre Zustandsübergänge selbst. "
           + "Services koordinieren nur und dürfen Status nicht direkt setzen."
)
public class Order {
}
```

### 7.4 Zu viele Inline-Referenzen

Schlecht:

```java
// QG-JAVA-001: Record nutzen.
public record UserDto(
    // QG-JAVA-001: id ist Record-Komponente.
    Long id,
    // QG-JAVA-001: name ist Record-Komponente.
    String name
) {
}
```

Das ist Kommentarverschmutzung. Der Code wird nicht klarer, sondern schwerer lesbar. Eine Referenz auf Klassenebene reicht.

```java
@DecisionRef(
    id = "QG-JAVA-001",
    reason = "Einfaches Response-DTO ohne Mutation und ohne Framework-Zwang zu Settern."
)
public record UserDto(Long id, String name) {
}
```

### 7.5 Veraltete Referenz nach Refactoring

Schlecht:

```java
// QG-JAVA-004: Kein @Async, Virtual Threads übernehmen I/O-Parallelität.
@Async
public void exportOrders() {
    // ...
}
```

Kommentar und Code widersprechen sich. In diesem Fall ist der Kommentar nicht Dokumentation, sondern ein Bug-Signal. Review und CI müssen solche Widersprüche aufdecken.

### 7.6 Interne Sicherheitsdetails im Kommentar offenlegen

Schlecht:

```java
// Security-Fix: Umgeht Schwachstelle im alten Tenant-Filter, weil Angreifer tenantId manipulieren konnten.
```

Kommentare und Annotationen landen im Repository. Sie dürfen keine ausnutzbaren Details, geheimen Systeminformationen, Kundennamen, Tokens, internen Angriffsvektoren oder produktionsnahen Exploit-Hinweise enthalten.

Besser:

```java
// QG-JAVA-006: Tenant-Kontext wird serverseitig aus der authentifizierten Session bestimmt.
// Request-Felder dürfen die Mandantenzuordnung nicht überschreiben.
```

---

## 8. Javadoc für öffentliche APIs und zentrale Klassen

Javadoc ist sinnvoll, wenn die Entscheidung für Nutzer der Klasse in der IDE sichtbar sein soll. Das betrifft öffentliche APIs, Domänenaggregate, zentrale Services, Integrationsadapter und sicherheitsrelevante Komponenten.

Gutes Beispiel:

```java
/**
 * Domänenobjekt für eine Bestellung.
 *
 * <p>Diese Klasse schützt fachliche Zustandsübergänge selbst. Externe Services
 * dürfen den Status nicht direkt setzen, sondern müssen fachlich benannte Methoden
 * wie {@link #cancel()} verwenden.
 *
 * <p>Relevante Qualitätsregeln:
 * <ul>
 *   <li>QG-JAVA-008: Verhalten gehört zum Domänenobjekt, wenn es dessen Invarianten schützt.</li>
 *   <li>QG-JAVA-006: Services koordinieren Use Cases, aber umgehen keine Domänenregeln.</li>
 * </ul>
 */
public class Order {

    /**
     * Storniert diese Bestellung, sofern der aktuelle Status eine Stornierung erlaubt.
     *
     * @throws OrderCannotBeCancelledException wenn die Bestellung bereits geliefert
     *         oder bereits storniert wurde.
     */
    public void cancel() {
        // ...
    }
}
```

Javadoc soll keine langen Architekturabhandlungen enthalten. Es soll die lokale Nutzung erklären und auf die relevante Regel hinweisen.

---

## 9. `package-info.java` für Paketregeln

Für Pakete mit klarer Architekturrolle SOLLTE eine `package-info.java` verwendet werden. Das ist besonders wertvoll für Domänenpakete, Adapterpakete, API-Pakete, Security-Pakete und Integrationspakete.

```java
@DecisionRef(
    id = "QG-JAVA-008",
    reason = "Dieses Paket enthält das Order-Aggregat. "
           + "Fachliche Invarianten liegen in Domänentypen, nicht in Utility-Klassen."
)
package com.example.order.domain;

import com.example.architecture.DecisionRef;
```

Ausführlicher:

```java
/**
 * Domänenschicht für das Order-Aggregat.
 *
 * <p>Geltende Regeln in diesem Paket:</p>
 * <ul>
 *   <li>QG-JAVA-008: Domänenobjekte schützen ihre eigenen Invarianten.</li>
 *   <li>QG-JAVA-002: Geschlossene Zustandsfamilien werden als sealed types modelliert.</li>
 *   <li>QG-JAVA-007: Keine Utility-Klassen als Ersatz für Domänenverhalten.</li>
 * </ul>
 *
 * <p>Nicht erlaubt:</p>
 * <ul>
 *   <li>Keine Abhängigkeit auf Controller oder Web-DTOs.</li>
 *   <li>Keine direkte Verwendung von HTTP-Typen.</li>
 *   <li>Keine statischen Helper-Friedhöfe.</li>
 * </ul>
 */
@DecisionRef(
    id = "QG-JAVA-008",
    reason = "Paket enthält fachliche Zustandsmodelle und Invarianten."
)
package com.example.order.domain;

import com.example.architecture.DecisionRef;
```

---

## 10. ArchUnit: Entscheidungen als Tests durchsetzen

Dokumentation ist wichtig, aber nicht ausreichend. Regeln, die nicht verletzt werden dürfen, SOLLTEN automatisiert geprüft werden. ArchUnit ist dafür geeignet, weil es Java-Bytecode analysiert und Architekturregeln als Tests formulieren kann.

### 10.1 Dependency

Maven:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.4.2</version>
    <scope>test</scope>
</dependency>
```

Gradle:

```groovy
testImplementation 'com.tngtech.archunit:archunit-junit5:1.4.2'
```

Die konkrete Version ist im Projekt zentral über Dependency Management zu pflegen.

### 10.2 Controller dürfen nicht auf Repositories zugreifen

```java
package com.example.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example")
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule controller_duerfen_nicht_auf_repositories_zugreifen =
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("QG-JAVA-006: Controller delegieren an Services und kennen keine Repositories.");
}
```

### 10.3 Keine Utility-Klassen als Standardmuster

```java
@ArchTest
static final ArchRule keine_generischen_utility_klassen =
    noClasses()
        .should().haveSimpleNameEndingWith("Utils")
        .orShould().haveSimpleNameEndingWith("Util")
        .orShould().haveSimpleNameEndingWith("Helper")
        .because("QG-JAVA-008 und QG-JAVA-007: Generische Utility-Klassen sind meist ein Hinweis auf fehlende Modellierung.");
```

Diese Regel darf nicht blind global eingeführt werden, wenn das Projekt bereits etablierte technische Utilities besitzt. In solchen Fällen wird zunächst ein Baseline-Modus oder eine Ausnahmeliste verwendet.

### 10.4 Keine direkte Entity-Exposition aus Controllern

```java
@ArchTest
static final ArchRule controller_duerfen_keine_entities_zurueckgeben =
    noClasses()
        .that().haveSimpleNameEndingWith("Controller")
        .should().dependOnClassesThat().resideInAPackage("..domain..entity..")
        .because("QG-JAVA-006: API-Verträge verwenden Request-/Response-DTOs, keine JPA-Entities.");
```

Je nach Paketstruktur muss diese Regel projektbezogen angepasst werden.

### 10.5 Keine `Optional`-Felder

```java
import java.util.Optional;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@ArchTest
static final ArchRule optional_nicht_als_feldtyp =
    noFields()
        .should().haveRawType(Optional.class)
        .because("QG-JAVA-007: Optional ist im Projektstandard nur für fehlbare Einzel-Rückgabewerte vorgesehen, nicht als Feldtyp.");
```

### 10.6 Referenzregeln nicht überziehen

Nicht jede Qualitätsregel muss sofort mit ArchUnit erzwungen werden. Gute Kandidaten für Automatisierung sind Regeln, die:

- objektiv prüfbar sind,
- häufig verletzt werden,
- hohe Folgekosten haben,
- wenig false positives erzeugen,
- klar begründbare Ausnahmen besitzen.

Schlechte Kandidaten sind Regeln, die viel Kontext benötigen oder Entwickler zu Umgehungslösungen verleiten würden.

---

## 11. Kommentare für kontraintuitive Entscheidungen

Inline-Kommentare sind für lokale Ausnahmen oder kontraintuitive Stellen geeignet. Sie müssen kurz sein und die Entscheidung erklären.

Standardformat:

```java
// QG-JAVA-XXX: Ein Satz, warum diese Entscheidung hier gilt.
```

Beispiele:

```java
// QG-JAVA-004: Kein @Async. Der Service ist I/O-lastig, Virtual Threads sind aktiviert,
// und synchroner Kontrollfluss ist hier einfacher zu testen und zu debuggen.
var invoice = invoiceClient.load(invoiceId);
```

```java
// QG-JAVA-006: TenantId wird nicht aus dem Request übernommen.
// Der serverseitige Tenant-Kontext ist die einzige autoritative Quelle.
var tenantId = tenantContext.currentTenantId();
```

```java
// QG-JAVA-002: Kein default-Zweig. Der sealed switch soll bei neuen Varianten
// bewusst einen Compiler-Fehler erzeugen.
return switch (result) {
    case Success s -> handleSuccess(s);
    case Failure f -> handleFailure(f);
    case Pending p -> handlePending(p);
};
```

```java
// QG-JAVA-005: Text Block nur für die statische SQL-Struktur.
// Werte werden über Parameter gebunden, nicht per formatted() interpoliert.
var sql = """
        SELECT id, name, email
        FROM users
        WHERE email = :email
        """;
```

---

## 12. Security- und SaaS-Relevanz

### 12.1 Entscheidungsreferenzen dürfen keine Geheimnisse enthalten

Kommentare, Javadocs und Annotationen sind Teil des Repositories. Sie dürfen keine Secrets, Tokens, produktionsnahen Zugangsdaten, internen Kundennamen, konkreten Schwachstellen-Exploit oder sensible Incident-Details enthalten.

Falsch:

```java
// Workaround für Token-Leak im alten Admin-Endpunkt /internal/foo mit Header X-...
```

Richtig:

```java
// QG-JAVA-006: Admin-Operationen verwenden serverseitig geprüfte Berechtigungen;
// Clientseitige Flags dürfen keine Autorisierungsentscheidung treffen.
```

### 12.2 Mandantenentscheidungen müssen sichtbar sein

In SaaS-Systemen ist Mandantenisolierung eine Kernentscheidung. Stellen, an denen `tenantId`, `organizationId`, `accountId` oder ähnliche Konzepte verarbeitet werden, SOLLTEN die autoritative Quelle klar machen.

```java
@DecisionRef(
    id = "QG-JAVA-006",
    reason = "Mandantenzuordnung wird aus dem authentifizierten Sicherheitskontext abgeleitet. "
           + "Request-Daten dürfen die Tenant-Grenze nicht bestimmen.",
    consequence = "Verhindert Cross-Tenant-Zugriff durch manipulierte Request-Felder."
)
public TenantId currentTenant() {
    return tenantContext.currentTenantId();
}
```

### 12.3 Logging-Entscheidungen müssen sensible Daten berücksichtigen

Wenn ein Kommentar erklärt, warum etwas geloggt oder nicht geloggt wird, darf er nicht zur Preisgabe sensibler Daten führen. Besonders vorsichtig sind Authentifizierung, Payment, Token-Verarbeitung, personenbezogene Daten, Admin-Aktionen und Security-Events zu behandeln.

```java
// QG-JAVA-007: Kein Logging des vollständigen Request-Objekts.
// Der Request kann personenbezogene und sicherheitsrelevante Felder enthalten.
log.info("Registration requested for tenant={}, correlationId={}", tenantId, correlationId);
```

---

## 13. Framework- und Plattform-Kontext

### 13.1 Spring Boot

In Spring-Boot-Anwendungen sind Entscheidungsreferenzen besonders nützlich an:

- `@Service`-Methoden mit Transaktionsgrenzen,
- `@TransactionalEventListener`-Handlern,
- Security-Konfiguration,
- Controller-Methoden mit besonderen Autorisierungsregeln,
- Batch- oder Scheduling-Komponenten,
- Integrationsadaptern für externe Systeme,
- Stellen, an denen bewusst kein `@Async` verwendet wird,
- Stellen, an denen Virtual Threads oder synchrone I/O bewusst genutzt werden.

Beispiel:

```java
@DecisionRef(
    id = "QG-JAVA-006",
    reason = "Event wird erst nach erfolgreichem Commit verarbeitet. "
           + "E-Mail darf nicht versendet werden, wenn die Registrierung zurückgerollt wird."
)
@TransactionalEventListener
public void onUserRegistered(UserRegisteredEvent event) {
    emailService.sendWelcomeEmail(event.email());
}
```

### 13.2 Wicket

In Wicket-Anwendungen sind Entscheidungsreferenzen besonders nützlich an:

- serialisierbaren Komponenten,
- detachable Models,
- bewusst nicht serialisierten Feldern,
- Security- und CSP-Integrationsstellen,
- Page-/Panel-Grenzen,
- Stellen, an denen Daten bewusst nicht in der Komponente gehalten werden.

Beispiel:

```java
@DecisionRef(
    id = "QG-JAVA-006",
    reason = "Model lädt Daten requestbezogen nach und hält keine nicht-serialisierbare Repository-Instanz im Panel."
)
private final IModel<OrderSummaryDto> orderModel;
```

### 13.3 CI/CD

Die Richtlinie wird wirksam, wenn sie in die Pipeline integriert wird. Geeignete Prüfungen sind:

- ArchUnit-Tests,
- Checkstyle-Regeln für Kommentarformate,
- Linkprüfung für referenzierte Dokumente,
- Semgrep-Regeln für verbotene Muster,
- Maven-/Gradle-Task zur Prüfung existierender `QG-JAVA-XXX`-IDs,
- Review-Gates für architekturrelevante Änderungen.

---

## 14. Entscheidungsbaum

Verwende folgende Entscheidungshilfe:

```text
Ist die Stelle architekturrelevant?
 ├─ Nein → Keine Entscheidungsreferenz nötig.
 └─ Ja
    ├─ Erklärt der Code die Entscheidung bereits durch Typen und Namen?
    │   ├─ Ja → Keine zusätzliche Referenz nötig, außer bei zentralen Regeln.
    │   └─ Nein
    │      ├─ Betrifft die Entscheidung eine Klasse oder Methode dauerhaft?
    │      │   ├─ Ja → @DecisionRef oder Javadoc verwenden.
    │      │   └─ Nein → kurzer QG-JAVA-XXX-Kommentar reicht.
    │      ├─ Gilt die Entscheidung für ein ganzes Paket?
    │      │   └─ package-info.java verwenden.
    │      └─ Darf die Regel nie verletzt werden?
    │          └─ ArchUnit-/CI-Regel ergänzen.
```

---

## 15. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel |
|---|---|---|
| Warum statt Was | Erklärt der Kommentar eine Entscheidung, nicht den offensichtlichen Ablauf? | `// Kein default bei sealed switch ...` statt `// switcht über result` |
| Lokaler Kontext | Wird klar, warum die Regel genau an dieser Stelle gilt? | `TenantId aus SecurityContext, nicht aus Request` |
| Referenz-ID | Ist die ID stabil und korrekt geschrieben? | `QG-JAVA-006` |
| Keine leeren Gründe | Ist `reason` aussagekräftig? | Nicht `reason = ""` |
| Keine Geheimnisse | Enthält Kommentar/Javadoc keine sensiblen Details? | Keine Tokens, Kundennamen, Exploit-Hinweise |
| Keine Kommentarverschmutzung | Wird nicht jede triviale Zeile referenziert? | Eine Klassenreferenz statt fünf Feldkommentare |
| Automatisierbarkeit | Ist eine wichtige Regel zusätzlich durch ArchUnit/CI geschützt? | Controller dürfen keine Repositories kennen |
| Aktualität | Widersprechen Kommentar und Code einander? | Kommentar „kein @Async“, Code hat `@Async` |
| Richtige Retention | Ist die Annotation für ArchUnit mit `CLASS` oder `RUNTIME` sichtbar? | Nicht `SOURCE`, wenn ArchUnit prüfen soll |
| Ausnahmebehandlung | Ist eine Abweichung klar begründet und lokal nachvollziehbar? | `consequence` oder kurzer Kommentar |

---

## 16. Automatisierbare Prüfungen

### 16.1 Referenz-ID-Format prüfen

Eine einfache CI-Prüfung kann verhindern, dass ungültige Referenzformate entstehen.

Beispielhafte Regel:

```regex
QG-JAVA-[0-9]{3}
```

### 16.2 Referenzierte Dokumente müssen existieren

Ein Build-Skript kann alle `QG-JAVA-XXX`-Vorkommen im Code suchen und prüfen, ob eine passende Markdown-Datei im Kompendium existiert.

Beispielstrategie:

```text
1. Suche alle Vorkommen von QG-JAVA-[0-9]{3} in src/main/java.
2. Extrahiere eindeutige IDs.
3. Prüfe, ob guidelines/QG-JAVA-XXX-*.md existiert.
4. Build schlägt fehl, wenn eine ID nicht auflösbar ist.
```

### 16.3 ArchUnit als Architektur-Gate

ArchUnit-Regeln SOLLTEN in der normalen Testphase laufen. Für große Legacy-Systeme kann zunächst mit eingefrorenen Regeln oder begrenztem Paket-Scope gearbeitet werden.

Geeignete Startregeln:

- Controller dürfen nicht auf Repositories zugreifen.
- Controller dürfen keine Entities zurückgeben.
- Services dürfen nicht auf Web-/HTTP-Typen abhängen.
- `Optional` darf nicht als Feldtyp verwendet werden.
- Keine generischen `*Utils`, `*Helper`, `*Util`-Klassen ohne Ausnahme.
- Domain-Pakete dürfen nicht auf Controller-Pakete abhängen.
- Security-relevante Pakete dürfen keine unsicheren Logging-Muster enthalten.

---

## 17. Migration bestehender Codebasen

### Schritt 1: Nicht sofort alles annotieren

Ein bestehendes System darf nicht in einem Big-Bang mit hunderten Annotationen überzogen werden. Das erzeugt Rauschen und wird nicht gepflegt.

### Schritt 2: Kritische Stellen identifizieren

Beginne mit:

- Authentifizierung und Autorisierung,
- Tenant-Kontext,
- Transaktionsgrenzen,
- Event-Verarbeitung,
- Domänenaggregaten,
- API-Verträgen,
- Persistenzgrenzen,
- bewusst nicht verwendeten Framework-Mustern,
- Stellen, die in Reviews regelmäßig diskutiert werden.

### Schritt 3: Annotation einführen

Führe `@DecisionRef` zentral ein. Verwende `RetentionPolicy.CLASS`, wenn ArchUnit später darauf zugreifen soll.

### Schritt 4: Paketdokumentation ergänzen

Ergänze `package-info.java` zuerst für zentrale Pakete wie:

```text
..domain..
..service..
..web..
..security..
..integration..
..persistence..
```

### Schritt 5: Erste ArchUnit-Regeln ergänzen

Starte mit zwei bis vier objektiven Regeln. Gute Kandidaten:

- Controller dürfen keine Repositories nutzen.
- Controller dürfen keine Entities zurückgeben.
- Keine `Optional`-Felder.
- Domain darf nicht von Web abhängen.

### Schritt 6: Review-Regel einführen

Neue architekturrelevante Änderungen müssen im Pull Request beantworten:

```text
Welche bestehende Qualitätsregel oder Architekturentscheidung betrifft diese Änderung?
Ist die Entscheidung im Code sichtbar?
Ist eine ArchUnit-Regel sinnvoll oder nötig?
```

---

## 18. Ausnahmen

Eine Entscheidungsreferenz darf entfallen, wenn:

- die Entscheidung durch Namen und Struktur vollständig offensichtlich ist,
- die Stelle nicht architekturrelevant ist,
- der Kommentar nur das Offensichtliche wiederholen würde,
- die Klasse rein technischer Glue-Code ohne langlebige Entscheidung ist,
- die Regel bereits durch Paketdokumentation und ArchUnit ausreichend abgedeckt ist.

Eine Ausnahme von einer Architekturregel ist zulässig, wenn:

- der technische oder fachliche Grund lokal dokumentiert ist,
- die Abweichung begrenzt ist,
- die Abweichung im Review akzeptiert wurde,
- kein Sicherheits- oder Mandantenrisiko entsteht,
- die Abweichung nicht als Präzedenzfall für beliebige Nachahmung missverstanden werden kann.

---

## 19. Definition of Done

Eine architekturrelevante Codeänderung erfüllt diese Richtlinie, wenn:

1. die relevante Entscheidung im Code sichtbar ist,
2. die Sichtbarkeit nicht durch überflüssige Kommentare, sondern durch geeignete Mittel hergestellt wurde,
3. Kommentare das Warum erklären und nicht nur das Was wiederholen,
4. Entscheidungsreferenzen eine gültige ID und eine lokale Begründung besitzen,
5. sensitive oder ausnutzbare Details nicht in Kommentaren, Javadocs oder Annotationen stehen,
6. Paketregeln in `package-info.java` dokumentiert sind, sofern sie ein ganzes Paket betreffen,
7. nicht verhandelbare Architekturregeln durch ArchUnit oder CI geprüft werden,
8. Code und Kommentar einander nicht widersprechen,
9. Ausnahmen explizit und nachvollziehbar begründet sind,
10. Reviewende die Entscheidung ohne externe Nachfrage nachvollziehen können.

---

## 20. Quellen und weiterführende Literatur

- Oracle Java SE 21 API — `RetentionPolicy`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html
- ArchUnit User Guide 1.4.2: https://www.archunit.org/userguide/html/000_Index.html
- Spring Framework Reference — Transaction-bound Events: https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
