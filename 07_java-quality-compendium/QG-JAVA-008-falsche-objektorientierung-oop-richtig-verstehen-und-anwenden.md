# QG-JAVA-008 — Falsche Objektorientierung: OOP richtig verstehen und anwenden

---
### Wie liest du dieses Dokument

Dieses Dokument ist mit ca. 110 KB ein Nachschlagewerk, kein Lesebuch. Drei Lesepfade werden empfohlen:

**Wenn du neu im Team bist:** Starte mit Sektion 2 (Kurzregel), lies dann Sektion 1 (Zweck) und Sektion 7 (Reiches Modell vs. Transaction Script). Damit hast du die mentale Karte, **wann** reiches Domänenmodell sinnvoll ist und wann nicht. Danach Sektion 8 (Anämisches Modell) und Sektion 12–13 (Primitive Obsession + Value Objects) — die zwei häufigsten Fehler.

**Wenn du im Code-Review bist:** Springe direkt zu Sektion 29 (Review-Checkliste). Jede Zeile hat einen Anker zur Detail-Sektion mit der Begründung. Bei Verstoß gegen eine MUSS-Regel: Sektion 3.1.

**Wenn du etwas Spezifisches suchst:** Inhaltsverzeichnis oben. Häufigste Punkte: Pattern Matching → Sektion 16. JPA → Sektion 20. Tenant-Context → Sektion 24. Aggregate Root → Sektion 18. Lombok → Sektion 22. Bean Validation → Sektion 21.

**Wenn du eine Modellierungsentscheidung treffen musst:** Sektion 27 (Entscheidungsbaum) — sechs Fragen, klare Antworten, ob ein eigener Typ, Value Object, Aggregate Root oder einfache Klasse die richtige Wahl ist.

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie objektorientiertes Design in Java fachlich korrekt, wartbar und prüfbar eingesetzt wird. Sie richtet sich gegen typische Fehlmuster: anämische Domänenmodelle, Utility-Klassen als Ersatz für Modellierung, Vererbung zur bloßen Code-Wiederverwendung, God Classes, primitive Typen für fachlich unterschiedliche Werte und öffentliche Setter, die gültige Zustände unterlaufen.

Objektorientierung bedeutet in dieser Richtlinie nicht: „Wir verwenden Klassen." Objektorientierung bedeutet: Fachliche Konzepte werden als Typen modelliert, schützen ihre Invarianten und stellen Verhalten über klare Methoden bereit. Ein Objekt soll nicht nur Daten tragen, sondern dort Verhalten besitzen, wo dieses Verhalten zur Identität, zum Zustand oder zur fachlichen Regel des Objekts gehört.

Diese Richtlinie ist kein Aufruf, jede Anwendung künstlich mit Domänenobjekten zu überladen. Für einfache CRUD-Flüsse können einfache Datenmodelle und Application Services ausreichen. Sobald jedoch fachliche Regeln, Zustandsübergänge, Berechtigungen, Geldbeträge, Mengen, Statusautomaten, Mandantenkontext oder sicherheitsrelevante Entscheidungen betroffen sind, MUSS das Modell expliziter und stärker gekapselt werden.

Mit Java 21 stehen Records, Sealed Types, Pattern Matching for Switch und Record Patterns als ausgereifte Sprachmittel zur Verfügung. Diese Richtlinie nutzt sie konsequent — nicht als Selbstzweck, sondern wo sie fachliche Klarheit erhöhen.

---

## 2. Kurzregel für Entwickler

Modelliere fachliche Regeln dort, wo sie hingehören: Invarianten und Zustandsübergänge gehören an das Domänenobjekt oder in einen klar benannten Domain Service; reine Ablaufkoordination gehört in den Application Service.

Vermeide öffentliche Setter für fachlich kritische Felder, untypisierte Primitive für fachliche Werte, God Classes, Utility-Friedhöfe und `instanceof`-Kaskaden, wenn sie fachliches Verhalten nur außerhalb der eigentlichen Domänentypen verstecken.

Value Objects MÜSSEN vollständige Operationen besitzen (nicht nur Validierung im Konstruktor). `Money` muss `plus`, `minus`, `isLessThan` und `requireSameCurrency` bieten — wer das nicht durchhält, hat kein Value Object, sondern einen validierten Record.

Tenant-IDs DÜRFEN NICHT aus Request-Daten gelesen werden — sie MÜSSEN aus dem `TenantContext` (siehe QG-JAVA-006 v2 Sektion 14.6) stammen. `@Data` und `@Setter` von Lombok sind für Domänenobjekte Anti-Patterns.

---

## 3. Verbindlicher Standard

### 3.1 MUSS-Regeln

Ein Domänenmodell MUSS folgende Regeln erfüllen:

1. Fachliche Invarianten MÜSSEN am Typ geschützt werden, nicht in externen Services.
2. Zustandsänderungen MÜSSEN über fachlich benannte Methoden erfolgen (`cancel()`, `confirm()`, `credit()`), nicht über öffentliche Setter.
3. Value Objects MÜSSEN vollständige Operationen anbieten (Vergleiche, Arithmetik, Konversionen), nicht nur Validierung.
4. `Money`-Arithmetik MUSS Currency-Konsistenz erzwingen — `plus(Money)` darf nicht zwei Währungen mischen.
5. `int`-Arithmetik bei Domänen-Werten MUSS `Math.addExact` / `Math.subtractExact` / `Math.multiplyExact` verwenden (Overflow-Schutz).
6. JPA-Entities mit fachlicher Logik MÜSSEN protected No-Arg-Konstruktor besitzen.
7. JPA-Entities MÜSSEN `@Version` für Optimistic Locking verwenden (Cross-Reference: QG-JAVA-006 v2 Sektion 11.6).
8. JPA-Entities mit Tenant-Bezug MÜSSEN `tenantId` als immutables Feld (`updatable = false`) und mit Index modellieren.
9. JPA-Entities DÜRFEN NICHT direkt als `@RequestBody` oder API-Response-Body verwendet werden — Command/Query DTOs sind Pflicht.
10. Tenant-Zugriff MUSS über fachlich benannte Methoden geprüft werden (`verifyBelongsTo(TenantId)`), nicht über externe Service-Bedingungen.
11. Tenant-IDs in Domänenmethoden MÜSSEN aus dem `TenantContext` (QG-JAVA-006 v2 Sektion 14.6) stammen, nicht aus Request-Daten.
12. `TenantAccessDeniedException` MUSS Information-Disclosure verhindern — Resource-Tenant-ID DARF NICHT in Caller-Messages auftauchen.
13. Aggregate Roots MÜSSEN `equals`/`hashCode` auf der ID basieren (mit Null-Check für transient Entities).
14. Value Objects (Records) verwenden Default-`equals`/`hashCode` über alle Komponenten.
15. Sealed Interfaces für Domänen-Varianten MÜSSEN mit Pattern Matching for Switch ausgewertet werden — der Compiler erzwingt Exhaustiveness.

### 3.2 DARF-NICHT-Regeln

Ein Domänenmodell DARF NICHT:

1. Öffentliche Setter für fachlich kritische Felder anbieten (`status`, `tenantId`, `role`, `permissions`, `balance`, `active`, `deleted`, `verified`, `locked`).
2. `@Data` von Lombok für JPA-Entities oder Aggregate Roots verwenden — generiert alle Setter inklusive der kritischen.
3. Setter auf Felder anbieten, deren Wert sich aus Domänenregeln ergibt.
4. JPA-Entities mit `@AllArgsConstructor` ohne Validierung ausstatten.
5. `instanceof`-Kaskaden über offene Typhierarchien aufbauen, wenn Sealed Types und Pattern Matching die geschlossene Variante ermöglichen.
6. `BigDecimal balance` als Account-Feld verwenden, wenn `Money` als Value Object existiert.
7. Geldbeträge ohne Currency-Validierung addieren oder vergleichen.
8. Email-Validierung mit `contains("@")` als ausreichend betrachten — Regex oder Bean Validation `@Email` MUSS verwendet werden.
9. `LocalDate.now()` direkt in Compact Constructors verwenden — `Clock` muss injizierbar sein.
10. Domänen-Klassen von Web-/HTTP-Typen abhängen lassen (`HttpServletRequest`, `ResponseEntity`, `@RequestBody`).
11. Vererbung allein zur Code-Wiederverwendung einsetzen — Komposition ist Default.
12. God Services bilden, die mehrere fachlich unabhängige Bereiche bündeln (`UserManager`, `AdminService`, `PlatformService`).
13. Mapper mit fachlicher Logik beladen — Mapper übersetzen Strukturen, sie entscheiden nicht über Berechtigungen.
14. Tenant-spezifische Logik im Property-Namen kodieren (siehe QG-JAVA-039-01 v2 Sektion 13.5).

### 3.3 SOLLTE-Regeln

Ein Domänenmodell SOLLTE:

1. Sealed Interfaces für geschlossene Domänen-Hierarchien verwenden (Customer-Typen, Payment-Status, Order-Events).
2. Record Patterns mit Destructuring für komplexe Pattern-Matching-Fälle nutzen (Java 21, JEP 440).
3. Bean Validation 3.0 Annotationen (`@NotNull`, `@Positive`, `@Email`, `@Pattern`) für Standard-Constraints verwenden.
4. Compact Constructor für komplexe, kontextabhängige Validierungen reservieren.
5. Pattern Matching for Switch als Refactoring-Schritt zwischen `instanceof`-Kaskade und Polymorphismus erwägen.
6. Aggregate Root und Aggregate Boundary explizit modellieren — ein Repository pro Aggregate.
7. Domain Events publizieren, wenn fachliche Zustandsänderungen andere Services betreffen (siehe QG-JAVA-041 v2 Sektion 8).
8. Domänen-Tests ohne Spring Context laufen lassen — Unit-Tests auf Aggregate-Ebene, Integration-Tests nur für JPA-Mappings.
9. Custom Validators für domänenspezifische Constraints (Currency-Match, Tenant-Match) verwenden.
10. Spezifische Domain-Exceptions (`OrderCannotBeCancelledException`, `InsufficientFundsException`) statt generischer `IllegalStateException`.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für:

1. Domänenobjekte mit fachlichen Invarianten.
2. JPA-Entities mit relevantem Verhalten.
3. Value Objects (Records oder Klassen mit Wertgleichheit).
4. Aggregate Roots und Aggregate Boundaries.
5. Application Services und Domain Services.
6. Status- und Zustandsübergänge.
7. Geld-, Mengen-, Identitäts- und Berechtigungslogik.
8. SaaS-Logik mit Tenant-, Account-, Plan-, User- oder Rollenbezug.
9. API- und Command-Verarbeitung, sofern daraus Domänenzustand entsteht.
10. Domain Events (siehe QG-JAVA-041 v2 für die Event-Driven-Seite).

Diese Richtlinie gilt nicht als pauschale Pflicht für:

1. Triviale CRUD-Administrationsmasken ohne relevante Fachlogik.
2. Reine DTOs für API-Verträge.
3. Einfache Read Models und Projections.
4. Technische Adapter, Mapper und Infrastrukturkomponenten.
5. Temporäre Testfixtures.
6. Bewusst funktionale oder pipeline-orientierte Datenverarbeitung, sofern sie klar, testbar und sicher bleibt.

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Anämisches Domänenmodell | Klassen mit Domänennamen, aber ohne Verhalten — Logik liegt in Services | `Order` mit nur Gettern/Settern, `OrderService` macht alles |
| Reiches Domänenmodell | Domänenobjekte tragen Verhalten und schützen Invarianten | `order.cancel()` statt `setStatus(CANCELLED)` |
| Transaction Script | Use-Case-Logik als Sequenz in einem Service, ohne Domänenobjekte | für einfache CRUD-Fälle akzeptabel |
| Aggregate Root | Einziger Einstiegspunkt in ein Aggregat, schützt Konsistenz innerhalb der Boundary | `Order` mit eingebetteten `OrderItem`s |
| Aggregate Boundary | Transaktionsgrenze um zusammengehörige Domänenobjekte | Ein Repository pro Aggregate Root |
| Value Object | Unveränderliches Objekt, das durch seinen Wert definiert ist | `Money(100, EUR)`, `Email("a@b.de")` |
| Domain Service | Fachliche Regel, die nicht natürlich zu einem Aggregat gehört | `TransferPolicy.verifyAllowed(source, target, amount)` |
| Application Service | Koordiniert Use Case (Laden, Berechtigung, Domain-Methode, Speichern) | `OrderApplicationService.cancelOrder(orderId)` |
| Sealed Interface | Geschlossene Typhierarchie — Compiler kennt alle Varianten | `sealed interface PaymentResult permits ...` |
| Pattern Matching for Switch | Java-21-Feature für exhaustive Typ-Verzweigung (JEP 441) | `switch (result) { case Success s -> ... }` |
| Record Pattern | Java-21-Feature für Destructuring in Pattern Matching (JEP 440) | `case Money(var amount, var currency) -> ...` |
| Compact Constructor | Records-Konstruktor ohne explizite Parameter, validiert Komponenten | `public Money { Objects.requireNonNull(amount); }` |
| Tenant Context | Server-validierter Mandanten-Kontext im aktuellen Thread | `TenantContext.currentTenantId()` (siehe QG-JAVA-006 v2) |
| Mass Assignment | Sicherheitsproblem, bei dem Request-Felder ungewollt Entity-Felder setzen | `@RequestBody UserEntity` ist Anti-Pattern |
| Information Disclosure | Sicherheitsproblem, bei dem Exception-Messages sensitive Daten preisgeben | `TenantMismatch: expected=tenant-acme` ist DI |
| Primitive Obsession | Verwendung primitiver Typen für fachlich unterschiedliche Werte | `Long userId` und `Long productId` verwechselbar |
| Optimistic Locking | JPA-Mechanismus zur Race-Condition-Vermeidung über `@Version` | `@Version private Long version;` |
| Bean Validation | Jakarta-Validation-API für deklarative Constraints | `@Validated`, `@Positive`, `@NotBlank`, `@Email` |

---

## 6. Technischer Hintergrund: Java 21 für Domänen-Modellierung

Java 21 ist die erste LTS-Version mit einem **vollständigen Set an Domain-Modellierungs-Features**. Diese Richtlinie nutzt sie konsequent.

### 6.1 Records (JEP 395, final seit Java 16)

Records eignen sich für transparente, unveränderliche Datenaggregate, insbesondere DTOs und Value Objects. Sie generieren automatisch:

- Konstruktor mit allen Komponenten
- Accessor-Methoden (`amount()`, `currency()`)
- `equals` und `hashCode` über alle Komponenten
- `toString`
- Pattern-Matching-Unterstützung

**Wichtig:** Records sind **flach unveränderlich** — Referenzen auf veränderliche Objekte (z. B. `List<...>`) sind nicht tief geschützt. Im Compact Constructor MUSS deshalb defensiv kopiert werden.

### 6.2 Sealed Classes und Sealed Interfaces (JEP 409, final seit Java 17)

Sealed Types erlauben, explizit festzulegen, welche Klassen eine Klasse erweitern oder ein Interface implementieren dürfen.

```java
public sealed interface PaymentResult
    permits PaymentSucceeded, PaymentFailed, PaymentPending {}
```

**Vorteile:**

- Geschlossene Hierarchie — keine ungewollten Subklassen.
- Pattern Matching for Switch wird **exhaustive** geprüft — der Compiler erzwingt, dass alle Varianten behandelt werden.
- Refactoring-Sicherheit — neue Variante hinzufügen bricht alle nicht-exhaustiven Switch-Statements.

### 6.3 Pattern Matching for Switch (JEP 441, final seit Java 21)

```java
public String describePayment(PaymentResult result) {
    return switch (result) {
        case PaymentSucceeded s -> "Erfolgreich: " + s.transactionId();
        case PaymentFailed f    -> "Fehlgeschlagen: " + f.reason();
        case PaymentPending p   -> "Ausstehend bis " + p.expectedCompletion();
        // ✅ Compiler erzwingt Exhaustiveness — kein default nötig!
    };
}
```

### 6.4 Record Patterns (JEP 440, final seit Java 21)

Record Patterns erlauben Destructuring direkt im Switch:

```java
public String describePayment(PaymentResult result) {
    return switch (result) {
        case PaymentSucceeded(var txId, var amount) ->
            "Erfolgreich: " + txId + " über " + amount;
        case PaymentFailed(var reason, var errorCode) ->
            "Fehlgeschlagen: " + errorCode + " (" + reason + ")";
        case PaymentPending(var expectedCompletion) ->
            "Ausstehend bis " + expectedCompletion;
    };
}
```

Record Patterns können auch **verschachtelt** werden:

```java
public sealed interface OrderEvent permits OrderPlaced, OrderCancelled {}
public record OrderPlaced(OrderId orderId, Money total, Instant placedAt)
    implements OrderEvent {}

public void handle(OrderEvent event) {
    switch (event) {
        case OrderPlaced(var orderId, Money(var amount, var currency), var placedAt) -> {
            // amount und currency sind direkt verfügbar — keine zusätzliche Dereferenzierung
            log.info("Order {} placed: {} {} at {}", orderId, amount, currency, placedAt);
        }
        case OrderCancelled c -> handleCancellation(c);
    }
}
```

Das ist **das wichtigste Java-21-Feature für Domänen-Modellierung** — und der Grund, warum diese Richtlinie Sealed Types und Pattern Matching aktiv empfiehlt.

### 6.5 JPA-Persistenz-Anforderungen

JPA-Entities besitzen technische Rahmenbedingungen aus der Jakarta Persistence 3.1 Spezifikation:

- public oder protected No-Arg-Konstruktor (für Reflection).
- Keine finalen Entity-Klassen.
- Keine finalen Felder, die JPA setzen muss.
- ID-Generation über `@GeneratedValue` (Standard-Use-Cases).

Diese Anforderungen schließen ein **reiches Domänenmodell mit JPA nicht aus** — sie verlangen aber Disziplin bei Konstruktoren, Kapselung, Lazy Loading und Transaktionsgrenzen. Records sind für JPA-Entities **nicht geeignet** (kein No-Arg-Konstruktor).

---

## 7. Reiches Domänenmodell vs. Transaction Script vs. CRUD

Ein reiches Domänenmodell ist nicht für jeden Code automatisch besser. Es ist dann besonders wertvoll, wenn:

1. Zustände nur in bestimmten Reihenfolgen erlaubt sind.
2. Regeln an mehreren Stellen wiederkehren.
3. Ungültige Zustände teuer oder gefährlich sind.
4. Geld, Mengen, Rechte oder Mandantengrenzen betroffen sind.
5. Eine Änderung mehrere Felder konsistent anpassen muss.
6. Die fachliche Sprache im Code sichtbar werden soll.

Ein einfacher Transaction-Script-Stil kann ausreichen, wenn:

1. Der Use Case sehr einfach ist.
2. Keine relevante Fachlogik existiert.
3. Daten nur validiert, gespeichert und gelesen werden.
4. Die Anwendung vor allem ein technisches Administrationswerkzeug ist.
5. Das Modell bewusst als Read Model oder Projection verwendet wird.

Diese Richtlinie verbietet einfache Lösungen nicht. Sie verbietet, komplexe Fachlogik in schwach benannten Services, Utility-Klassen und Setter-Sequenzen zu verstecken.

---

## 8. Fehlmuster 1 — Anämisches Domänenmodell

Ein anämisches Domänenmodell sieht auf den ersten Blick fachlich aus, enthält aber fast kein Verhalten. Es besitzt Klassen mit Domänennamen, Felder, Getter und Setter. Die eigentliche Logik liegt in Services, Utility-Klassen oder Controllern.

### 8.1 Schlechte Anwendung

```java
public class Order {

    private Long id;
    private List<OrderItem> items = new ArrayList<>();
    private OrderStatus status;
    private BigDecimal total;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<OrderItem> getItems() { return items; }
}
```

```java
public class OrderService {

    public void cancelOrder(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setTotal(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### 8.2 Warum das schlecht ist

Die `Order` schützt ihre eigenen Regeln nicht. Jeder Code kann den Status ändern, ohne die erlaubten Zustandsübergänge zu beachten. Dadurch werden ungültige Zustände möglich — eine bereits ausgelieferte Bestellung mit Status `CANCELLED` oder eine Bestellung mit Status `PENDING` aber `total = 0`.

Außerdem wird Verhalten schwer auffindbar. Wer wissen möchte, wann eine Bestellung storniert werden darf, muss Services durchsuchen. Das Modell selbst gibt keine Antwort.

### 8.3 Gute Anwendung

```java
public class Order {

    private Long id;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;

    protected Order() {
        // Für JPA
    }

    public Order(List<OrderItem> initialItems) {
        if (initialItems == null || initialItems.isEmpty()) {
            throw new IllegalArgumentException("Eine Bestellung benötigt mindestens eine Position.");
        }
        this.items.addAll(initialItems);
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id,
                "Bereits ausgelieferte Bestellungen können nicht storniert werden.");
        }

        if (status == OrderStatus.CANCELLED) {
            throw new OrderCannotBeCancelledException(id,
                "Die Bestellung ist bereits storniert.");
        }

        this.status = OrderStatus.CANCELLED;
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) {
            throw new OrderModificationException(id,
                "Nur offene Bestellungen dürfen verändert werden.");
        }
        this.items.add(Objects.requireNonNull(item));
    }

    public Money total() {
        return items.stream()
            .map(OrderItem::subtotal)
            .reduce(Money.zero(currencyOfItems()), Money::plus);
    }

    public OrderStatus status() { return status; }

    public List<OrderItem> items() {
        return List.copyOf(items);   // defensive Kopie
    }

    private Currency currencyOfItems() {
        return items.get(0).price().currency();
    }
}
```

```java
public class OrderApplicationService {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void cancelOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();    // ✅ Domänenobjekt schützt seine Regeln

        orderRepository.save(order);
    }
}
```

### 8.4 Qualitätsregel

Wenn eine Methode erst Daten aus einem Objekt liest, dann außerhalb entscheidet und anschließend über Setter denselben Objektzustand verändert, ist das ein starkes Signal für fehlendes Verhalten am Objekt.

**Schlecht:**

```java
if (order.getStatus() == OrderStatus.PENDING) {
    order.setStatus(OrderStatus.CANCELLED);
}
```

**Gut:**

```java
order.cancel();
```

---

## 9. Fehlmuster 2 — Utility-Klassen als Ersatz für Modellierung

Utility-Klassen sind häufig ein Symptom dafür, dass ein fachliches Konzept keinen eigenen Typ bekommen hat.

### 9.1 Schlechte Anwendung

```java
public final class UserUtils {

    private UserUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    public static String maskEmail(String email) {
        int at = email.indexOf("@");
        return email.substring(0, 1) + "***" + email.substring(at);
    }

    public static boolean isAdult(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}
```

### 9.2 Warum das schlecht ist

`UserUtils` hat keine klare Verantwortung. E-Mail-Validierung, Maskierung und Altersberechnung sind verschiedene fachliche Konzepte. Außerdem bleiben Werte weiterhin ungeschützt: Nach der Validierung ist `String email` immer noch irgendein String — der nächste Methodenaufruf bekommt wieder einen unvalidierten String.

### 9.3 Gute Anwendung mit Value Objects

```java
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "email must not be null");
        value = value.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException(value);
        }

        if (value.length() > 254) {           // RFC 5321 limit
            throw new InvalidEmailException(value);
        }
    }

    public String masked() {
        int at = value.indexOf('@');
        return value.charAt(0) + "***" + value.substring(at);
    }
}
```

```java
public record BirthDate(LocalDate value) {

    public BirthDate {
        Objects.requireNonNull(value, "birth date must not be null");
    }

    // ✅ Factory-Methode mit Clock — testbar
    public static BirthDate from(LocalDate value, Clock clock) {
        var birthDate = new BirthDate(value);
        if (value.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Geburtsdatum darf nicht in der Zukunft liegen.");
        }
        return birthDate;
    }

    public int ageInYears(Clock clock) {
        return Period.between(value, LocalDate.now(clock)).getYears();
    }

    public boolean isAdult(Clock clock) {
        return ageInYears(clock) >= 18;
    }
}
```

**Wichtig:** Für strikte Email-Validierung kann auch Bean Validation `@Email` (`jakarta.validation.constraints.Email`) verwendet werden — die hier gezeigte Regex ist eine pragmatische Mindestprüfung. `contains("@")` als alleinige Validierung ist **nicht ausreichend**.

### 9.4 Qualitätsregel

Wenn eine statische Utility-Methode regelmäßig denselben primitiven Werttyp entgegennimmt und fachliche Regeln darauf anwendet, SOLLTE geprüft werden, ob ein Value Object fehlt.

---

## 10. Fehlmuster 3 — Vererbung zur Code-Wiederverwendung

Vererbung ist eine starke Kopplung. Sie ist geeignet, wenn ein echtes substituierbares „ist ein"-Verhältnis besteht (Liskov-Substitutionsprinzip). Sie ist ungeeignet, wenn nur Implementierung wiederverwendet werden soll.

### 10.1 Schlechte Anwendung

```java
public abstract class BaseNotification {

    public void send(Notification notification) {
        log(notification);
        doSend(notification);
        retryIfNeeded(notification);
    }

    protected abstract void doSend(Notification notification);

    private void log(Notification notification) { /* ... */ }
    private void retryIfNeeded(Notification notification) { /* ... */ }
}

public class EmailNotification extends BaseNotification {
    @Override
    protected void doSend(Notification notification) {
        // E-Mail versenden
    }
}
```

### 10.2 Warum das schlecht ist

Subklassen sind vom Ablauf der Basisklasse abhängig. Eine Änderung in `BaseNotification` kann alle Subklassen beeinflussen. Dieses Problem ist als **fragile base class** bekannt. Außerdem wird Verhalten versteckt geerbt, statt explizit komponiert.

### 10.3 Gute Anwendung mit Komposition

```java
public interface NotificationSender {
    SendResult send(Notification notification);
}

public interface NotificationLogger {
    void log(Notification notification, SendResult result);
}

public class EmailNotificationSender implements NotificationSender {

    private final EmailClient emailClient;
    private final NotificationLogger logger;
    private final RetryPolicy retryPolicy;

    public EmailNotificationSender(
            EmailClient emailClient,
            NotificationLogger logger,
            RetryPolicy retryPolicy) {
        this.emailClient = emailClient;
        this.logger = logger;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public SendResult send(Notification notification) {
        return retryPolicy.execute(() -> {
            var result = emailClient.send(notification.recipient(), notification.body());
            logger.log(notification, result);
            return result;
        });
    }
}
```

### 10.4 Qualitätsregel

Vererbung DARF NICHT nur verwendet werden, um Methoden oder Felder wiederzuverwenden. Wenn eine Klasse lediglich Verhalten nutzt, SOLLTE Komposition verwendet werden. Wenn eine Klasse wirklich eine spezialisierte Form eines Typs ist, kann Vererbung sinnvoll sein. Bei geschlossenen Typfamilien SOLLTEN Sealed Interfaces geprüft werden.

---

## 11. Fehlmuster 4 — God Class und God Service

God Classes bündeln zu viele Verantwortlichkeiten. In Spring-Anwendungen treten sie oft als `UserManager`, `OrderService`, `AdminService`, `CommonService` oder `PlatformService` auf.

### 11.1 Schlechte Anwendung

```java
public class UserManager {

    public User register(String name, String email, String password) { /* ... */ }
    public User login(String email, String password) { /* ... */ }
    public void updateProfile(Long userId, String name, String bio) { /* ... */ }
    public void changePassword(Long userId, String oldPassword, String newPassword) { /* ... */ }
    public void upgradeToPremium(Long userId, String paymentToken) { /* ... */ }
    public void sendVerificationEmail(Long userId) { /* ... */ }
}
```

### 11.2 Warum das schlecht ist

Die Klasse hat keine klare Grenze. Änderungen an Registrierung, Authentifizierung, Profil, Payment und Verifikation landen im gleichen Typ. Dadurch steigen Kopplung, Testaufwand, Merge-Konflikte und Risiko unbeabsichtigter Seiteneffekte.

### 11.3 Gute Anwendung

```java
public class UserRegistrationService {
    public UserId register(RegisterUserCommand command) { /* ... */ }
}

public class UserAuthenticationService {
    public SessionToken login(LoginCommand command) { /* ... */ }
}

public class UserProfileService {
    public void updateProfile(UpdateUserProfileCommand command) { /* ... */ }
}

public class SubscriptionService {
    public void upgradeToPremium(UpgradeSubscriptionCommand command) { /* ... */ }
}
```

### 11.4 Qualitätsregel

Wenn die Beschreibung einer Klasse mehrere „und" enthält, ist die Klasse wahrscheinlich zu groß. Wenn ein Test für eine Methode viele fachlich unabhängige Mocks benötigt, ist die Verantwortung wahrscheinlich falsch geschnitten.

---

## 12. Fehlmuster 5 — Primitive Obsession

Primitive Obsession bedeutet, dass fachlich unterschiedliche Konzepte als `String`, `Long`, `int`, `BigDecimal` oder `boolean` modelliert werden, obwohl eigene Typen Verwechslungen und ungültige Zustände verhindern könnten.

### 12.1 Schlechte Anwendung

```java
public void createOrder(
        Long userId,
        Long productId,
        int quantity,
        BigDecimal amount,
        String currency) {
    // ...
}
```

Dieser Code lässt viele ungültige Aufrufe zu. `userId` und `productId` können vertauscht werden. `quantity` kann negativ sein. `currency` kann `"EURO"`, `"eur"`, `""` oder `null` sein. Der Compiler schützt nichts davon.

### 12.2 Gute Anwendung

```java
public record UserId(Long value) {
    public UserId {
        Objects.requireNonNull(value, "userId must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}

public record ProductId(Long value) {
    public ProductId {
        Objects.requireNonNull(value, "productId must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("productId must be positive");
        }
    }
}

public void createOrder(
        UserId userId,
        ProductId productId,
        Quantity quantity,
        Money price) {
    // ...
}
```

### 12.3 Qualitätsregel

Eigene Domänentypen SOLLTEN verwendet werden, wenn mindestens eine der folgenden Bedingungen erfüllt ist:

1. Der Wert hat eigene Validierungsregeln.
2. Der Wert kann leicht mit einem anderen Wert desselben technischen Typs verwechselt werden.
3. Der Wert trägt sicherheitsrelevante Bedeutung.
4. Der Wert erscheint in öffentlichen APIs, Events oder Persistenzgrenzen.
5. Der Wert ist Teil von Geld-, Mengen-, Rollen-, Tenant- oder Berechtigungslogik.

---

## 13. Value Objects mit vollständigen Operationen

Value Objects sind **nicht nur validierte Records**. Sie sind kleine Typen mit vollständigen Operationen — Vergleichen, Arithmetik, Konversionen, Kombinationen.

### 13.1 `Money` mit vollständigen Operationen

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.scale() > currency.getDefaultFractionDigits()) {
            amount = amount.setScale(
                currency.getDefaultFractionDigits(),
                RoundingMode.UNNECESSARY
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    // ✅ Factory-Methode für Null-Werte
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    // ✅ Arithmetik mit Currency-Validierung
    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        var result = amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result must not be negative");
        }
        return new Money(result, currency);
    }

    public Money multipliedBy(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("factor must not be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    // ✅ Vergleichsoperationen
    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }
}
```

### 13.2 `Quantity` mit Overflow-Schutz

```java
public record Quantity(int value) {

    public Quantity {
        if (value <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    // ✅ Math.addExact wirft ArithmeticException bei Overflow
    public Quantity plus(Quantity other) {
        Objects.requireNonNull(other, "other quantity must not be null");
        return new Quantity(Math.addExact(value, other.value));
    }

    public Quantity minus(Quantity other) {
        Objects.requireNonNull(other, "other quantity must not be null");
        var result = Math.subtractExact(value, other.value);
        if (result <= 0) {
            throw new InvalidQuantityException("Result must be positive");
        }
        return new Quantity(result);
    }

    public Quantity multipliedBy(int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("factor must be positive");
        }
        return new Quantity(Math.multiplyExact(value, factor));
    }

    public boolean isAtLeast(Quantity other) {
        Objects.requireNonNull(other);
        return value >= other.value;
    }
}
```

**Warum `Math.addExact`?** Bei `Quantity(2_000_000_000).plus(Quantity(2_000_000_000))` produziert `+` einen negativen Wert ohne Exception (Integer-Overflow). `Math.addExact` wirft `ArithmeticException`. Das ist die Disziplin, die Domänen-Werte von primitiven Operationen unterscheidet.

### 13.3 `Account` mit `Money` als Balance

```java
public class Account {

    private AccountStatus status = AccountStatus.ACTIVE;
    private Money balance;                            // ✅ Money, nicht BigDecimal

    public Account(Currency currency) {
        this.balance = Money.zero(currency);
    }

    public void close() {
        if (!balance.isZero()) {
            throw new AccountCannotBeClosedException(
                "Konto kann nur mit Saldo 0 geschlossen werden."
            );
        }
        this.status = AccountStatus.CLOSED;
    }

    public void credit(Money amount) {
        requireActive();
        this.balance = this.balance.plus(amount);     // ✅ Currency-Validierung in Money
    }

    public void debit(Money amount) {
        requireActive();
        if (balance.isLessThan(amount)) {              // ✅ Money.isLessThan(Money)
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.minus(amount);
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
    }

    public Money balance() { return balance; }
    public AccountStatus status() { return status; }
}
```

### 13.4 Qualitätsregel

Value Objects MÜSSEN vollständige Operationen anbieten. Wer nur Validierung im Konstruktor hat und dann `value()` aufruft, um den primitiven Wert herauszuholen, hat den Wert eines Value Objects nicht verstanden. Currency-Validierung beim `plus` ist kein optionales Feature — es ist die Existenzberechtigung des `Money`-Typs.

---

## 14. Fehlmuster 6 — Getter/Setter als Scheinkapselung

Getter und Setter werden häufig als Kapselung missverstanden. Kapselung bedeutet jedoch nicht, dass Felder private sind und über Methoden trotzdem beliebig verändert werden können. Kapselung bedeutet, dass ein Objekt seinen gültigen Zustand schützt.

### 14.1 Schlechte Anwendung

```java
public class Account {
    private AccountStatus status;
    private Money balance;

    public void setStatus(AccountStatus status) { this.status = status; }
    public void setBalance(Money balance) { this.balance = balance; }
}
```

### 14.2 Gute Anwendung

Siehe Sektion 13.3 (`Account` mit `Money` und fachlichen Methoden).

### 14.3 Qualitätsregel

Öffentliche Setter für fachlich relevante Felder sind grundsätzlich zu vermeiden. Stattdessen MÜSSEN fachlich benannte Methoden verwendet werden. Felder wie `status`, `role`, `tenantId`, `price`, `amount`, `balance`, `ownerId`, `plan`, `permissions`, `deleted`, `verified`, `active` oder `locked` DÜRFEN NICHT beliebig per Setter veränderbar sein.

---

## 15. Fehlmuster 7 — `instanceof` als Ersatz für Polymorphismus

`instanceof` ist nicht falsch. Pattern Matching for Switch und Sealed Classes machen Typprüfung in Java 21 deutlich besser. Trotzdem ist eine `instanceof`-Kaskade oft ein Signal, dass Verhalten nicht dort modelliert wurde, wo es hingehört.

### 15.1 Schlechte Anwendung

```java
public BigDecimal discountFor(Customer customer, BigDecimal amount) {
    if (customer instanceof PremiumCustomer) {
        return amount.multiply(new BigDecimal("0.20"));
    }

    if (customer instanceof RegularCustomer) {
        return amount.multiply(new BigDecimal("0.05"));
    }

    if (customer instanceof NewCustomer) {
        return BigDecimal.ZERO;
    }

    throw new IllegalStateException("Unbekannter Kundentyp.");
}
```

### 15.2 Refactoring-Pfad: erst Pattern Matching, dann Polymorphismus

Der häufigste Refactoring-Pfad geht in zwei Schritten:

**Schritt 1: Pattern Matching mit Sealed Interface**

```java
public sealed interface Customer
    permits PremiumCustomer, RegularCustomer, NewCustomer {}

public record PremiumCustomer(String id) implements Customer {}
public record RegularCustomer(String id) implements Customer {}
public record NewCustomer(String id) implements Customer {}

// ✅ Pattern Matching — Compiler erzwingt Exhaustiveness
public BigDecimal discountFor(Customer customer, BigDecimal amount) {
    return switch (customer) {
        case PremiumCustomer p -> amount.multiply(new BigDecimal("0.20"));
        case RegularCustomer r -> amount.multiply(new BigDecimal("0.05"));
        case NewCustomer n     -> BigDecimal.ZERO;
        // kein default nötig — alle Varianten abgedeckt
    };
}
```

**Schritt 2 (optional): Polymorphismus, wenn Verhalten zum Typ gehört**

```java
public sealed interface Customer
    permits PremiumCustomer, RegularCustomer, NewCustomer {
    BigDecimal discountFor(BigDecimal amount);
}

public record PremiumCustomer(String id) implements Customer {
    @Override
    public BigDecimal discountFor(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.20"));
    }
}
// ... analog für andere

public BigDecimal discountFor(Customer customer, BigDecimal amount) {
    return customer.discountFor(amount);
}
```

### 15.3 Wann welcher Schritt?

| Schritt | Wann |
|---|---|
| **Pattern Matching Switch (Schritt 1)** | Wenn das Verhalten **außerhalb** des Typs liegt — z. B. Mapping, API-Übersetzung, technische Klassifikation, Logging-Format |
| **Polymorphismus (Schritt 2)** | Wenn das Verhalten **fachlich zum Typ gehört** — der Kunde „kennt" seinen Rabatt |

**Pattern Matching ist weniger invasiv** — die Sealed-Hierarchie bleibt schlank, neue Varianten brechen automatisch alle nicht-exhaustiven Switches. Polymorphismus ist **fachlich expressiver** — das Domänenobjekt trägt sein Verhalten.

### 15.4 Wann `switch` trotzdem richtig ist

Bei geschlossenen Ergebnis- oder Zustandsmodellen kann ein exhaustiver `switch` über ein Sealed Interface richtig sein, insbesondere wenn das Verhalten bewusst außerhalb des Typs liegen soll:

```java
public String labelFor(PaymentResult result) {
    return switch (result) {
        case PaymentSucceeded s -> "Erfolgreich";
        case PaymentFailed f    -> "Fehlgeschlagen";
        case PaymentPending p   -> "Ausstehend";
    };
}
```

Hier ist der Switch korrekt — die UI-Beschriftung gehört nicht in die Domäne.

### 15.5 Qualitätsregel

Wenn dieselbe Typentscheidung an mehreren Stellen im Code wiederholt wird, SOLLTE geprüft werden, ob Verhalten in den Typ selbst gehört oder eine Sealed-Hierarchie mit zentralem Mapping sinnvoller ist.

---

## 16. Record Patterns und Pattern Matching for Switch

Java 21 bringt **Record Patterns** (JEP 440) — Destructuring direkt im Switch-Pattern. Das ist das wichtigste Domänen-Modellierungs-Feature der Sprache.

### 16.1 Basis: Pattern Matching for Switch

```java
public String describe(Shape shape) {
    return switch (shape) {
        case Circle c    -> "Circle with radius " + c.radius();
        case Square s    -> "Square with side " + s.side();
        case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
    };
}
```

### 16.2 Record Patterns mit Destructuring

```java
public String describe(Shape shape) {
    return switch (shape) {
        case Circle(var radius)              -> "Circle with radius " + radius;
        case Square(var side)                -> "Square with side " + side;
        case Rectangle(var width, var height) -> "Rectangle " + width + "x" + height;
    };
}
```

### 16.3 Verschachtelte Record Patterns

```java
public sealed interface OrderEvent permits OrderPlaced, OrderCancelled, OrderShipped {}

public record OrderPlaced(OrderId orderId, Money total, Instant placedAt)
    implements OrderEvent {}

public void handle(OrderEvent event) {
    switch (event) {
        case OrderPlaced(var orderId, Money(var amount, var currency), var placedAt) -> {
            // amount und currency direkt verfügbar — keine .amount()/.currency() nötig
            log.info("Order {} placed: {} {} at {}", orderId, amount, currency, placedAt);
            metrics.recordOrder(currency, amount);
        }
        case OrderCancelled c -> handleCancellation(c);
        case OrderShipped s   -> handleShipping(s);
    }
}
```

### 16.4 Pattern Matching mit Guard-Klauseln

```java
public DiscountCategory categorize(Customer customer) {
    return switch (customer) {
        case PremiumCustomer p when p.yearsActive() >= 5 -> DiscountCategory.LOYAL_PREMIUM;
        case PremiumCustomer p                            -> DiscountCategory.PREMIUM;
        case RegularCustomer r when r.totalSpent().isGreaterThanOrEqualTo(THRESHOLD)
                                                          -> DiscountCategory.HIGH_VALUE_REGULAR;
        case RegularCustomer r                            -> DiscountCategory.REGULAR;
        case NewCustomer n                                -> DiscountCategory.NEW;
    };
}
```

### 16.5 Qualitätsregel

Sealed Interfaces MÜSSEN mit Pattern Matching for Switch ausgewertet werden — der Compiler erzwingt Exhaustiveness. Record Patterns mit Destructuring SOLLTEN für komplexe Pattern-Matching-Fälle verwendet werden, weil sie das Boilerplate für `.amount()`, `.currency()` usw. eliminieren.

---

## 17. Domain Services vs. Application Services

Nicht jede Regel gehört zwingend in eine Entity. Manchmal betrifft eine fachliche Regel mehrere Aggregate oder externe fachliche Konzepte. Dann ist ein Domain Service sinnvoll.

### 17.1 Application Service

Ein Application Service koordiniert einen Use Case.

```java
public class TransferApplicationService {

    private final AccountRepository accountRepository;
    private final TransferPolicy transferPolicy;
    private final TenantContext tenantContext;

    public void transfer(TransferCommand command) {
        var source = accountRepository.findByIdAndTenant(
            command.sourceAccountId(), tenantContext.currentTenantId())
            .orElseThrow();
        var target = accountRepository.findByIdAndTenant(
            command.targetAccountId(), tenantContext.currentTenantId())
            .orElseThrow();

        transferPolicy.verifyAllowed(source, target, command.amount());

        source.debit(command.amount());
        target.credit(command.amount());

        accountRepository.save(source);
        accountRepository.save(target);
    }
}
```

### 17.2 Domain Service

Ein Domain Service kapselt eine fachliche Regel, die nicht natürlich zu genau einem Objekt gehört.

```java
public class TransferPolicy {

    public void verifyAllowed(Account source, Account target, Money amount) {
        if (source.sameAs(target)) {
            throw new TransferNotAllowedException("Quelle und Ziel dürfen nicht identisch sein.");
        }

        if (!source.canDebit(amount)) {
            throw new TransferNotAllowedException("Quellkonto hat nicht genug Deckung.");
        }
    }
}
```

### 17.3 Qualitätsregel

Ein Service ist problematisch, wenn er interne Zustände fremder Objekte ausliest, Entscheidungen trifft und anschließend mehrere Setter aufruft. Ein Service ist gut geschnitten, wenn er Objekte lädt, fachlich benannte Methoden aufruft und technische Abläufe koordiniert.

Cross-Reference: Service-Layer-Architektur in QG-JAVA-006 v2 Sektion 5 (Schichtenmodell).

---

## 18. Aggregate Root, Aggregate Boundary, Repository

Aggregate Root ist ein **DDD-Kernkonzept**, das in dieser Richtlinie als organisatorisches Pattern verwendet wird.

### 18.1 Definitionen

| Konzept | Bedeutung |
|---|---|
| **Aggregate** | Cluster von Domänenobjekten, die als Einheit behandelt werden (z. B. `Order` mit eingebetteten `OrderItem`s). |
| **Aggregate Root** | Einziger Einstiegspunkt in das Aggregat. Externe Code-Stellen DÜRFEN NICHT direkt auf interne Objekte zugreifen. |
| **Aggregate Boundary** | Transaktionsgrenze. Innerhalb der Boundary ist Konsistenz strikt, zwischen Aggregaten ist sie eventual. |
| **Repository** | Lädt und speichert Aggregate Roots. **Ein Repository pro Aggregate Root.** |

### 18.2 Beispiel

```java
// ✅ Aggregate Root: Order mit eingebetteten Items
@Entity
public class Order {

    @Id
    @GeneratedValue
    private OrderId id;

    @Version
    private Long version;

    @OneToMany(mappedBy = "order",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    private OrderStatus status;

    // ✅ Interne Items dürfen nur über die Aggregate Root verändert werden
    public void addItem(Product product, Quantity quantity) {
        if (status != OrderStatus.PENDING) {
            throw new OrderModificationException(id, "Nur offene Bestellungen.");
        }
        items.add(new OrderItem(this, product, quantity));
    }

    public void removeItem(OrderItemId itemId) {
        if (status != OrderStatus.PENDING) {
            throw new OrderModificationException(id, "Nur offene Bestellungen.");
        }
        items.removeIf(item -> item.id().equals(itemId));
    }

    // ✅ Items nur als unveränderliche Kopie nach außen
    public List<OrderItem> items() {
        return List.copyOf(items);
    }
}
```

```java
// ✅ Ein Repository pro Aggregate Root
public interface OrderRepository extends JpaRepository<Order, OrderId> {

    Optional<Order> findByIdAndTenantId(OrderId id, String tenantId);
}

// ❌ Kein separates Repository für OrderItem — nur Order hat eines
// public interface OrderItemRepository { ... }   // VERBOTEN
```

### 18.3 Konsistenzregeln

Innerhalb eines Aggregats — **strikte Konsistenz**. `Order.cancel()` und `Order.items` werden atomar in einer Transaktion verändert.

Zwischen Aggregaten — **eventual consistency**. Wenn `Order.cancel()` auch das `Customer`-Aggregat betrifft (z. B. Bonus-Punkte zurückbuchen), passiert das **asynchron** über Domain Events (siehe QG-JAVA-041 v2 Sektion 8).

```java
public class Order {

    public DomainEvent cancel() {
        // ... fachliche Validierung
        this.status = OrderStatus.CANCELLED;

        // ✅ Domain Event publizieren — anderes Aggregat reagiert asynchron
        return new OrderCancelledEvent(id, customerId, total, Instant.now());
    }
}
```

### 18.4 Qualitätsregel

Ein Aggregate Root MUSS die Konsistenz innerhalb seiner Boundary schützen. Zugriff auf interne Objekte darf nur über die Aggregate Root erfolgen. Externe Aggregate werden über Domain Events koordiniert, nicht über synchrone Service-Aufrufe innerhalb derselben Transaktion.

Cross-Reference: Eventual Consistency in QG-JAVA-041 v2 Sektion 24.

---

## 19. `equals` und `hashCode` für Aggregate Roots vs. Value Objects

Aggregate Roots und Value Objects haben **unterschiedliche Identitäts-Semantik** — und damit unterschiedliche `equals`/`hashCode`-Implementierungen.

### 19.1 Value Objects: Wertgleichheit

Value Objects sind durch ihren Wert definiert. Zwei `Money(100, EUR)` sind dieselbe „Sache". Records erzeugen automatisch das korrekte `equals`/`hashCode` über alle Komponenten:

```java
public record Money(BigDecimal amount, Currency currency) {}

// new Money(BigDecimal.TEN, EUR).equals(new Money(BigDecimal.TEN, EUR)) == true
```

### 19.2 Aggregate Roots: Identity-Gleichheit

Aggregate Roots haben **Identity** — zwei `Order` mit derselben ID sind dasselbe Aggregat, auch wenn sich der Zustand unterscheidet (z. B. nach einer Statusänderung):

```java
public class Order {

    private OrderId id;
    // ... weitere Felder

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        // ✅ Null-Check: transient Entities haben noch keine ID
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // ✅ Hibernate-Best-Practice: konstanter Wert (Klassen-Hash)
        // verhindert Probleme bei transient → persistent Übergang
        return getClass().hashCode();
    }
}
```

### 19.3 Warum nicht `id.hashCode()`?

Wenn eine transient Entity in einem `HashSet` liegt und dann persistiert wird (ID wird zugewiesen), würde sich ihr `hashCode` ändern — die Entity wäre im Set nicht mehr auffindbar. Das ist der klassische Hibernate-Falltest.

Der konstante `getClass().hashCode()` ist suboptimal für Performance (alle Entities derselben Klasse haben denselben Hash), aber **korrekt** über den Lebenszyklus.

### 19.4 Qualitätsregel

Aggregate Roots MÜSSEN `equals`/`hashCode` mit Null-Check auf der ID implementieren. `hashCode()` MUSS konstant bleiben über den Lebenszyklus (Empfehlung: `getClass().hashCode()`). Value Objects (Records) verwenden Default-`equals`/`hashCode` über alle Komponenten.

---

## 20. JPA und reiches Domänenmodell

JPA verhindert kein reiches Domänenmodell, macht aber bestimmte Kompromisse notwendig.

### 20.1 Vollständiges JPA-Entity-Beispiel

```java
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_orders_tenant_status", columnList = "tenant_id, status")
    }
)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)        // ✅ ID-Generation
    private Long id;

    @Version                                                    // ✅ Optimistic Locking
    private Long version;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;                                    // ✅ Immutable Tenant

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,                              // ✅ Cascade
        orphanRemoval = true,                                   // ✅ Orphan Removal
        fetch = FetchType.LAZY
    )
    private final List<OrderItemEntity> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderEntity() {
        // Für JPA — Reflection-Zugriff
    }

    // ✅ Factory-Methode statt öffentlichem Konstruktor
    public static OrderEntity create(String tenantId, List<OrderItemEntity> initialItems) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (initialItems == null || initialItems.isEmpty()) {
            throw new IllegalArgumentException("Eine Bestellung benötigt mindestens eine Position.");
        }

        var order = new OrderEntity();
        order.tenantId = tenantId;
        order.createdAt = Instant.now();
        initialItems.forEach(order::addItemInternal);
        return order;
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(id, "Bereits ausgeliefert.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new OrderCannotBeCancelledException(id, "Bereits storniert.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void addItemInternal(OrderItemEntity item) {
        item.setOrder(this);
        items.add(item);
    }

    public void verifyBelongsTo(String expectedTenantId) {
        if (!tenantId.equals(expectedTenantId)) {
            // ✅ Generische Message — kein Information Disclosure
            throw new TenantAccessDeniedException("Access denied");
        }
    }

    public List<OrderItemEntity> items() {
        return List.copyOf(items);                              // ✅ defensive Kopie
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

### 20.2 Was hier kritisch ist

| Element | Begründung |
|---|---|
| `@GeneratedValue` | Ohne ID-Generation muss man die ID manuell setzen — Anti-Pattern. |
| `@Version` | Optimistic Locking verhindert Lost Updates bei parallelen Transaktionen. Cross-Reference: QG-JAVA-006 v2 Sektion 11.6. |
| `tenantId` mit `updatable = false` | Tenant-ID kann nie versehentlich geändert werden — Defense-in-Depth. |
| `@Index` auf `tenant_id` | Performance-Pflicht in Multi-Tenant-Setups. |
| `@OneToMany` mit Cascade + Orphan Removal | Aggregate-Boundary wird auf DB-Ebene erzwungen. |
| `protected` No-Arg-Konstruktor | JPA-Pflicht, aber fachlich nicht öffentlich. |
| Factory-Methode `create(...)` | Validierung vor Construction, Fachlogik im Domänenobjekt. |
| `verifyBelongsTo` mit generischer Message | Information Disclosure verhindern (siehe Sektion 24). |
| `equals/hashCode` mit Null-Check | Hibernate-Best-Practice für transient → persistent Übergang. |

### 20.3 Nicht zulässige Ausrede

JPA ist keine Rechtfertigung für öffentliche Setter auf alle Felder. JPA benötigt Zugriff für Persistenz, aber die Fachlogik muss trotzdem Zustandsübergänge schützen.

### 20.4 Qualitätsregel

JPA-Entities mit Fachlogik MÜSSEN:

1. `protected` No-Arg-Konstruktor verwenden.
2. `@GeneratedValue` für Standard-ID-Generation.
3. `@Version` für Optimistic Locking.
4. `tenantId` mit `updatable = false` bei Multi-Tenant-Setups.
5. `@Index` für Tenant-Spalten.
6. `@OneToMany` mit Cascade und Orphan Removal für Aggregate-Boundary.
7. Öffentliche Setter für kritische Felder vermeiden.
8. Zustandsübergänge über fachliche Methoden abbilden.
9. Collections nicht direkt veränderbar herausgeben (defensive Kopie).
10. `equals`/`hashCode` mit Null-Check für transient Entities.
11. Generische Exception-Messages bei Tenant-Mismatch.
12. Keine Controller-, HTTP- oder DTO-Abhängigkeiten enthalten.

---

## 21. Bean Validation 3.0 als deklarative Alternative

Spring Boot 3.4 und Jakarta Validation 3.0 bieten **deklarative Bean Validation** für viele Standard-Constraints. Compact Constructor bleibt für komplexe Validierungen, deklarative Annotationen sind für Standard-Constraints idiomatischer.

### 21.1 Deklarative Validation

```java
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserId(@NotNull @Positive Long value) {}

public record Money(
    @NotNull @PositiveOrZero BigDecimal amount,
    @NotNull Currency currency
) {}

public record Quantity(@Positive int value) {}
```

### 21.2 Mit `@Validated` auf Application Services

```java
@Service
@Validated
public class OrderApplicationService {

    public void cancelOrder(@NotNull @Positive Long orderId) {
        // @Validated triggert die Constraints automatisch
        // ...
    }
}
```

### 21.3 Wann manuell, wann deklarativ?

| Constraint-Typ | Wo |
|---|---|
| Standard (`@NotNull`, `@Positive`, `@Email`, `@Pattern`, `@Min`, `@Max`) | Bean Validation Annotationen |
| Komplex (kontextabhängig, mehrfeld, Business-Rule) | Compact Constructor |
| Custom (z. B. Currency-Match) | Custom Validator + Annotation |

### 21.4 Custom Validator für Domänen-Constraints

```java
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SameCurrencyValidator.class)
public @interface SameCurrency {
    String message() default "Currencies must match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class SameCurrencyValidator implements ConstraintValidator<SameCurrency, Money[]> {
    @Override
    public boolean isValid(Money[] monies, ConstraintValidatorContext context) {
        if (monies == null || monies.length < 2) return true;
        var first = monies[0].currency();
        return Arrays.stream(monies).allMatch(m -> m.currency().equals(first));
    }
}
```

### 21.5 Qualitätsregel

Bean Validation 3.0 Annotationen SOLLTEN für Standard-Constraints verwendet werden. Compact Constructor bleibt für komplexe, kontextabhängige Validierungen reserviert.

Cross-Reference: QG-JAVA-039-01 v2 Sektion 10 (Bean Validation als Standard).

---

## 22. Lombok: Wann hilfreich, wann gefährlich

Lombok ist in DACH-Engineering-Codebases allgegenwärtig — und es ist eine **Hauptursache von anämischen Domänenmodellen**. Diese Richtlinie nimmt klar Position.

### 22.1 Anti-Patterns

```java
// ❌ Anti-Pattern: @Data generiert ALLE Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private OrderStatus status;        // ⚠️ public setStatus generiert!
    private BigDecimal total;          // ⚠️ public setTotal generiert!
    private String tenantId;           // ⚠️ public setTenantId generiert!
}
```

Das verstößt gegen **jede einzelne Regel** aus Sektion 14 (Getter/Setter als Scheinkapselung) und Sektion 23 (Security).

### 22.2 Akzeptable Lombok-Verwendung

| Annotation | Wann akzeptabel |
|---|---|
| `@Getter` | Für Read-Only-Felder ohne Setter — okay. |
| `@RequiredArgsConstructor` | Für Konstruktor-Injection in Services — sehr gut. |
| `@Slf4j` | Für Logger-Boilerplate — okay. |
| `@Builder` | Mit Vorsicht — kann Validierung umgehen. |
| `@ToString` | Mit Vorsicht — kann sensitive Felder offenlegen. Custom `@ToString(exclude = ...)` Pflicht für sensible Felder. |

### 22.3 Anti-Patterns für Domänenobjekte

| Annotation | Warum problematisch |
|---|---|
| `@Data` | Generiert alle Setter — zerstört Kapselung. |
| `@Setter` (auf Klasse) | Dasselbe Problem. |
| `@AllArgsConstructor` | Umgeht Factory-Methoden und Validierung. |
| `@NoArgsConstructor` (public) | Erzeugt Object in ungültigem Zustand. |

### 22.4 Pragmatik

| Klassentyp | Empfehlung |
|---|---|
| Aggregate Root | Kein `@Data`. `@Getter` für selektive Felder. Keine `@Setter`. Konstruktor manuell schreiben oder `@Builder` mit Validierung. |
| Value Object (Record) | Records brauchen kein Lombok. Compact Constructor reicht. |
| Spring Service | `@RequiredArgsConstructor` + `@Slf4j` ist gut. |
| Spring Component | `@RequiredArgsConstructor` + `@Slf4j` ist gut. |
| DTO / Command | `@Data` akzeptabel — DTOs sind transparente Datencontainer. |
| Read Model | `@Data` akzeptabel. |
| JPA Entity mit Fachlogik | Kein `@Data`, kein `@Setter`. `@Getter` selektiv. |

### 22.5 Qualitätsregel

`@Data`, `@Setter` (auf Klasse) und `@AllArgsConstructor` (public) DÜRFEN NICHT für Aggregate Roots oder fachliche Entities verwendet werden. Für DTOs und Read Models sind sie akzeptabel.

---

## 23. Security- und SaaS-Auswirkungen

Falsche Objektorientierung ist kein reines Stilproblem. Sie kann direkte Sicherheits- und Plattformfolgen haben.

### 23.1 Mass Assignment

Wenn API-Requests direkt auf Entities gebunden werden, können Angreifer versuchen, Felder zu setzen, die nie durch den Client änderbar sein sollten.

**Schlecht:**

```java
@PostMapping("/users/{id}")
public void updateUser(@PathVariable Long id, @RequestBody UserEntity user) {
    userRepository.save(user);           // ⚠️ Angreifer kann role, tenantId, active setzen!
}
```

**Besser:**

```java
public record UpdateUserProfileCommand(
    @NotBlank @Size(max = 100) String displayName,
    @Size(max = 500) String bio
) {}

@PostMapping("/users/{id}")
public void updateUser(
        @PathVariable Long id,
        @RequestBody @Valid UpdateUserProfileCommand command) {
    userProfileService.updateProfile(id, command);
}
```

Der Command enthält nur die Felder, die wirklich durch den Client geändert werden dürfen. Cross-Reference: OWASP Mass Assignment Cheat Sheet.

### 23.2 Tenant-Isolation

In SaaS-Plattformen darf ein Objekt seine Mandantengrenze nicht nur als zufälliges Feld besitzen, das überall gesetzt werden kann.

**Schlecht:**

```java
order.setTenantId(request.tenantId());     // ⚠️ Tenant aus Request — Privilege Escalation
```

**Besser:**

```java
// Tenant aus geprüftem SecurityContext, nicht aus Request
var currentTenant = tenantContext.currentTenantId();
order.verifyBelongsTo(currentTenant);
order.cancelBy(currentTenant, actor);
```

Siehe Sektion 24 für vollständige Tenant-Context-Implementierung.

### 23.3 Berechtigungen

Objektorientierung ersetzt keine Autorisierung. Sie kann aber verhindern, dass Zustandsänderungen an Berechtigungsprüfungen vorbeilaufen.

**Schlecht:**

```java
if (actor.canCancel(order)) {
    order.setStatus(OrderStatus.CANCELLED);     // ⚠️ Status setzbar ohne Domain-Check
}
```

**Besser:**

```java
order.cancelBy(actor);    // Methode prüft sowohl Domain-Regel als auch Actor-Berechtigung
```

### 23.4 Sensitive Data Exposure

Getter, `toString()`, Records und Mapper dürfen sensible Felder nicht unkontrolliert nach außen tragen. Ein gutes Objektmodell trennt interne Zustände, API-Responses und Logging-Repräsentationen.

```java
public class User {

    private final UserId id;
    private final Email email;
    private final HashedPassword passwordHash;     // ⚠️ niemals nach außen!
    private final List<Role> roles;

    @Override
    public String toString() {
        // ✅ kein passwordHash, keine vollständige Email
        return "User{id=" + id + ", email=" + email.masked() + "}";
    }
}
```

---

## 24. Tenant-Context: Domänen-Modellierung trifft Security

Tenant-Isolation ist **die Stelle**, an der OOP-Disziplin auf Security trifft. Diese Sektion baut auf QG-JAVA-006 v2 Sektion 14.6 (Tenant-Context-Pattern) auf.

### 24.1 TenantContext (Cross-Reference: QG-JAVA-006 v2 Sektion 14.6)

```java
@Component
public class TenantContext {

    private static final ThreadLocal<TenantId> CURRENT_TENANT = new ThreadLocal<>();

    public TenantScope setForCurrentThread(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        CURRENT_TENANT.set(tenantId);
        return () -> CURRENT_TENANT.remove();
    }

    public TenantId currentTenantId() {
        var tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new TenantContextNotSetException();
        }
        return tenantId;
    }

    public interface TenantScope extends AutoCloseable {
        @Override
        void close();
    }
}
```

Der `TenantContext` wird **nach Authentifizierung** im Request-Filter oder Kafka-Listener gesetzt — niemals aus Request-Daten.

### 24.2 Domänen-Methoden mit Tenant-Validierung

```java
public class OrderEntity {

    private String tenantId;

    public void verifyBelongsTo(TenantId expectedTenant) {
        Objects.requireNonNull(expectedTenant, "tenant must not be null");
        if (!tenantId.equals(expectedTenant.value())) {
            // ✅ Generische Message für Caller — kein Information Disclosure
            throw new TenantAccessDeniedException("Access denied");
        }
    }

    public void cancelBy(TenantId actor) {
        verifyBelongsTo(actor);
        cancel();
    }
}
```

### 24.3 `TenantAccessDeniedException` mit Information-Disclosure-Schutz

```java
public class TenantAccessDeniedException extends RuntimeException {

    private final String resourceTenantId;        // nur für interne Logs
    private final String requestingTenantId;

    public TenantAccessDeniedException(String message) {
        super(message);
        this.resourceTenantId = null;
        this.requestingTenantId = null;
    }

    public TenantAccessDeniedException(
            String message,
            String resourceTenantId,
            String requestingTenantId) {
        super(message);                            // ✅ generische Message für Caller
        this.resourceTenantId = resourceTenantId;
        this.requestingTenantId = requestingTenantId;
    }

    // ✅ Detail-Felder nur für Audit-Logs zugreifbar
    public String resourceTenantId() { return resourceTenantId; }
    public String requestingTenantId() { return requestingTenantId; }
}
```

```java
@ControllerAdvice
public class TenantSecurityAdvice {

    private static final Logger log = LoggerFactory.getLogger(TenantSecurityAdvice.class);
    private final AuditLogger auditLogger;

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleTenantAccessDenied(
            TenantAccessDeniedException ex,
            HttpServletRequest request) {

        // ✅ Vollständige Daten nur im Audit-Log
        auditLogger.logSecurityIncident(
            "TENANT_ACCESS_DENIED",
            ex.resourceTenantId(),
            ex.requestingTenantId(),
            request.getRequestURI()
        );

        // ✅ Generische Antwort für Caller
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Access denied"));
    }
}
```

### 24.4 Qualitätsregel

Tenant-IDs in Domänenmethoden MÜSSEN aus dem `TenantContext` stammen, nicht aus Request-Daten. `TenantAccessDeniedException` MUSS Information-Disclosure verhindern — Resource-Tenant-ID DARF NICHT in Caller-Messages auftauchen, sondern nur im Audit-Log.

Cross-Reference: QG-JAVA-006 v2 Sektion 14.6 (Tenant-Context-Pattern).

---

## 25. Framework- und Plattform-Kontext

### 25.1 Spring Boot

Spring Services SOLLTEN Use Cases koordinieren. Sie DÜRFEN fachliche Policies aufrufen, Repositories verwenden und Transaktionen steuern. Sie SOLLTEN jedoch nicht alle fachlichen Regeln enthalten, wenn dadurch Domänenobjekte ihre Verantwortung verlieren. Cross-Reference: QG-JAVA-006 v2 Sektion 6 (Service-Layer-Pattern).

### 25.2 Records

Records sind hervorragend für Value Objects und DTOs, wenn keine JPA-Entity-Anforderungen bestehen. Für JPA-Entities sind Records in dieser Richtlinie nicht vorgesehen (kein No-Arg-Konstruktor möglich).

### 25.3 Sealed Classes

Sealed Interfaces eignen sich für geschlossene Domänenvarianten: Payment Results, Statusübergänge, Customer-Hierarchien, Order-Events. Pattern Matching for Switch erzwingt Exhaustiveness.

### 25.4 Records vs. Klassen für Hierarchien

| Kontext | Empfehlung |
|---|---|
| **Read Model / Snapshot** (`Customer` als Lese-Repräsentation) | Records mit Sealed Interface — Wertgleichheit ist korrekt |
| **Aggregate Root** (`Customer` mit Identity und Lifecycle) | Klasse mit Sealed Interface — Identity-Gleichheit erforderlich |
| **Domain Event** | Record mit Sealed Interface |
| **Application Command** | Record |
| **JPA Entity** | Klasse (Records funktionieren nicht mit JPA) |

### 25.5 MapStruct und Mapper

Mapper sind sinnvoll für DTO-Entity-Transformationen. Sie dürfen jedoch keine versteckte Business-Logik enthalten. Ein Mapper übersetzt Strukturen. Er entscheidet nicht über Berechtigungen, Statusübergänge oder fachliche Gültigkeit.

### 25.6 Reflection

Reflection ist in Frameworks üblich. Im Anwendungscode ist Reflection kein Ersatz für klare Schnittstellen, Value Objects oder Mapper. Direkter Zugriff auf private Felder im Business-Code ist grundsätzlich zu vermeiden.

---

## 26. Designregeln

### 26.1 Regel: Verhalten gehört zur Verantwortung

Eine Methode gehört zu dem Typ, dessen fachlichen Zustand sie verändert oder dessen Invariante sie schützt.

### 26.2 Regel: Keine öffentlichen Setter für kritische Felder

Felder wie `status`, `role`, `tenantId`, `price`, `amount`, `balance`, `ownerId`, `plan`, `permissions`, `deleted`, `verified`, `active` oder `locked` DÜRFEN NICHT beliebig per Setter veränderbar sein.

### 26.3 Regel: Value Objects mit vollständigen Operationen

E-Mail-Adressen, Geldbeträge, Mengen, Tenant-IDs, User-IDs, Product-IDs, Rollennamen und externe Referenzen MÜSSEN eigene Typen erhalten, wenn Regeln oder Verwechslungsgefahr bestehen. Diese Typen MÜSSEN vollständige Operationen bieten — nicht nur Validierung.

### 26.4 Regel: Komposition vor Vererbung

Vererbung benötigt eine echte fachliche Substituierbarkeit. Für Wiederverwendung von Verhalten ist Komposition der Standard.

### 26.5 Regel: Keine Utility-Müllhalden

Utility-Klassen müssen klein, technisch und kohärent sein. Fachliche Hilfsmethoden gehören in Value Objects, Domain Services oder fachlich benannte Komponenten.

### 26.6 Regel: Services nicht mit Domänenobjekten verwechseln

Ein Service ist kein Ersatz für ein Modell. Ein Service orchestriert. Ein Domänenobjekt schützt Zustand. Eine Policy entscheidet fachlich. Ein Repository lädt und speichert.

### 26.7 Regel: Sealed Types mit Pattern Matching

Sealed Interfaces MÜSSEN mit Pattern Matching for Switch ausgewertet werden. Der Compiler erzwingt Exhaustiveness — keine vergessenen Varianten.

### 26.8 Regel: Tenant-Context aus SecurityContext

Tenant-IDs in Domänenmethoden MÜSSEN aus dem `TenantContext` stammen, nicht aus Request-Daten oder URL-Parametern.

---

## 27. Entscheidungsbaum

```
Hat der Typ fachliche Invarianten?
├─ Nein
│  ├─ Ist es ein API-Request/Response oder Read Model?
│  │  └─ Record oder einfache Klasse verwenden.
│  └─ Ist es technische Infrastruktur?
│     └─ Klare Komponente oder Adapter verwenden.
└─ Ja
   ├─ Hat der Typ Identität und Lifecycle?
   │  └─ Klasse als Aggregate Root mit Verhalten modellieren.
   │     ├─ equals/hashCode auf ID mit Null-Check (Sektion 19)
   │     ├─ JPA-Annotationen vollständig (Sektion 20)
   │     └─ Repository pro Aggregate (Sektion 18)
   ├─ Ist es ein unveränderlicher Wert?
   │  └─ Value Object verwenden, häufig als Record.
   │     ├─ Vollständige Operationen (Sektion 13)
   │     ├─ Compact Constructor für komplexe Constraints
   │     └─ Bean Validation für Standard-Constraints (Sektion 21)
   ├─ Betrifft die Regel mehrere Aggregate?
   │  └─ Domain Service oder Policy verwenden.
   ├─ Ist es ein Use-Case-Ablauf?
   │  └─ Application Service verwenden.
   │     ├─ Tenant-Context aus SecurityContext (Sektion 24)
   │     └─ Domain Events für Cross-Aggregate-Koordination (QG-JAVA-041 v2)
   └─ Gibt es eine geschlossene Menge von Varianten?
      └─ Sealed Interface mit Pattern Matching for Switch (Sektion 16).
```

---

## 28. Gute und falsche Anwendung im Überblick

| Aspekt | Falsche Anwendung | Gute Anwendung | Beispiel |
| --- | --- | --- | --- |
| Zustandsänderung | `setStatus(CANCELLED)` | `cancel()` | Bestellung schützt ihre Übergänge selbst |
| Geldbetrag | `BigDecimal amount, String currency` | `Money amount` mit `plus`/`minus` | Currency-Validierung erzwungen |
| Mengen-Arithmetik | `int quantity1 + quantity2` | `quantity1.plus(quantity2)` mit `Math.addExact` | Overflow-Schutz |
| Email-Validierung | `email.contains("@")` | Regex oder Bean Validation `@Email` | RFC 5321 |
| Mandant | `setTenantId(...)` | `verifyBelongsTo(currentTenant)` | Tenant-Isolation aus SecurityContext |
| Verhalten | `OrderUtils.calculateTotal(order)` | `order.total()` | Verhalten liegt am fachlichen Objekt |
| Wiederverwendung | abstrakte Basisklasse | Komposition über Interfaces | Sender + Logger + RetryPolicy |
| API-Binding | `@RequestBody Entity` | Command DTO mit `@Valid` | Schutz gegen Mass Assignment |
| Statusmodell | offenes Interface + `instanceof` | Sealed Type + Pattern Matching | Compiler erzwingt Exhaustiveness |
| Service | God Service | Use-Case-Service + Domainmodell | Registrierung, Profil, Billing getrennt |
| JPA-Entity | `BigDecimal balance`, kein `@Version` | `Money balance`, `@Version`, `@Index` | produktionsreif |
| Pattern Matching | `instanceof` Kaskade | `switch` mit Record Patterns | JEP 440 + JEP 441 |
| Lombok | `@Data` auf Entity | `@Getter` selektiv, `@RequiredArgsConstructor` | keine generierten Setter |
| `equals/hashCode` | Default Object | ID-basiert mit Null-Check (Aggregate) oder Records (Value Object) | Hibernate-Best-Practice |
| Tenant-Mismatch | `IllegalStateException("expected=X")` | `TenantAccessDeniedException("Access denied")` | kein Information Disclosure |

---

## 29. Review-Checkliste

Ein Pull Request erfüllt diese Richtlinie nur, wenn folgende Fragen sauber beantwortet werden können:

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| Verantwortung | Hat jede Klasse eine klar benannte Verantwortung? | §11 |
| God Class | Enthält eine Klasse mehrere unabhängige Themenbereiche? | §11.2 |
| Setter | Gibt es öffentliche Setter für fachlich kritische Felder? | §14, §3.2.1 |
| Zustandsübergänge | Werden Zustandsübergänge über fachlich benannte Methoden modelliert? | §8.3, §14.2 |
| Utility | Gibt es Utility-Klassen, die eigentlich Value Objects oder Domain Services sein sollten? | §9 |
| Primitive Obsession | Werden primitive Typen für fachlich unterschiedliche Werte verwendet? | §12 |
| ID-Verwechslung | Können `userId`, `productId`, `tenantId`, `accountId` verwechselt werden? | §12.2 |
| Value Object Operationen | Haben Value Objects vollständige Operationen (`plus`, `minus`, `isLessThan`)? | §13, §3.1.3 |
| `Money` Currency-Check | Erzwingen `Money`-Operationen Currency-Konsistenz? | §13.1, §3.1.4 |
| Overflow-Schutz | Verwenden `int`-Operationen `Math.addExact` / `subtractExact`? | §13.2, §3.1.5 |
| Email-Validierung | Ist Email-Validierung stärker als `contains("@")`? | §9.3 |
| `BirthDate` mit Clock | Ist zeitliche Validierung mit `Clock` testbar? | §9.3 |
| API-Binding | Werden Entities direkt als API-Request/-Response verwendet? | §23.1, §3.2 |
| Collections | Werden interne Collections veränderbar herausgegeben? | §20.1 |
| Service-Logik | Enthält ein Service Logik, die klar zu einem Domänenobjekt gehört? | §17 |
| Mapper | Enthält ein Mapper fachliche Entscheidungen? | §25.5 |
| Vererbung | Wird Vererbung nur für Code-Wiederverwendung verwendet? | §10 |
| `instanceof` | Gibt es `instanceof`-Kaskaden, die Pattern Matching ersetzen sollten? | §15 |
| Sealed Types | Sind geschlossene Hierarchien mit Sealed Interface modelliert? | §6.2, §15.2 |
| Pattern Matching | Wird `switch` mit Exhaustiveness-Check für Sealed Types verwendet? | §16 |
| Record Patterns | Werden Record Patterns für komplexe Destructuring-Fälle genutzt? | §16.3 |
| JPA `@GeneratedValue` | Haben Entities `@GeneratedValue` für Standard-IDs? | §20.1 |
| JPA `@Version` | Haben Entities `@Version` für Optimistic Locking? | §20.1 |
| Tenant-Index | Haben Multi-Tenant-Entities `@Index` auf `tenant_id`? | §20.1 |
| `protected` Konstruktor | Haben JPA-Entities `protected` No-Arg-Konstruktor? | §20.1 |
| Aggregate Boundary | Sind Aggregate Roots klar identifiziert mit einem Repository? | §18 |
| `equals`/`hashCode` | Aggregate Roots mit Null-Check, Value Objects über Records? | §19 |
| Tenant-Context | `verifyBelongsTo` mit `TenantContext` (nicht Request)? | §24 |
| Information Disclosure | Bietet `TenantAccessDeniedException` keine Resource-IDs nach außen? | §24.3 |
| Bean Validation | Standard-Constraints über `@NotNull`/`@Positive`? | §21 |
| Compact Constructor | Komplexe Constraints in Compact Constructor? | §21.3 |
| Lombok | Kein `@Data`/`@Setter` auf Aggregate Roots? | §22 |
| `toString` Sensitive | Keine sensiblen Felder in `toString`/Logs? | §23.4 |
| Test ohne Spring | Können wichtigste Regeln ohne Spring Context geprüft werden? | §29 |
| Cross-References | QG-JAVA-006 v2 (Tenant), QG-JAVA-041 v2 (Aggregate), QG-JAVA-039-01 v2 (Bean Validation) konsultiert? | — |

---

## 30. Automatisierbare Prüfungen

### 30.1 ArchUnit-Regeln

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "com.example")
class OopArchitectureTest {

    // ✅ Domänen-Pakete dürfen nicht von Web-/HTTP-Schichten abhängen
    @ArchTest
    static final ArchRule domain_should_not_depend_on_web =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web..",
                "jakarta.servlet..",
                "..controller..",
                "..rest.."
            )
            .because("Domänen-Klassen dürfen nicht von Web-Schichten abhängen");

    // ✅ Entities dürfen nicht direkt als API-Request-/Response-Body verwendet werden
    @ArchTest
    static final ArchRule controllers_should_not_use_entities_as_parameters =
        noMethods()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
            .should().haveRawParameterTypes(
                JavaClass.Predicates.resideInAPackage("..entity..")
            );

    // ✅ Domain-Klassen dürfen keine öffentlichen Setter für kritische Felder haben
    @ArchTest
    static final ArchRule domain_should_not_have_public_setters_on_critical_fields =
        noMethods()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().haveNameMatching("set(Status|Tenant|Owner|Role|Price|Amount|Balance|Permissions|Active|Deleted|Verified|Locked).*")
            .should().bePublic()
            .because("Kritische Domänen-Felder dürfen nicht über öffentliche Setter veränderbar sein");

    // ✅ Utility-Klassen müssen final sein und private Konstruktor haben
    @ArchTest
    static final ArchRule utility_classes_should_be_final =
        classes()
            .that().haveSimpleNameEndingWith("Utils")
            .or().haveSimpleNameEndingWith("Helper")
            .should().haveModifier(JavaModifier.FINAL)
            .andShould().haveOnlyPrivateConstructors();

    // ✅ Records dürfen nicht in `..entity..` (JPA-Klassen) liegen
    @ArchTest
    static final ArchRule records_should_not_be_jpa_entities =
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().beRecords()
            .because("JPA-Entities dürfen keine Records sein (kein No-Arg-Konstruktor möglich)");

    // ✅ @Data von Lombok darf nicht auf Aggregate Roots
    @ArchTest
    static final ArchRule aggregate_roots_should_not_use_lombok_data =
        noClasses()
            .that().resideInAPackage("..domain..")
            .or().resideInAPackage("..entity..")
            .should().beAnnotatedWith("lombok.Data")
            .because("@Data generiert alle Setter — zerstört Kapselung von Aggregate Roots");

    // ✅ Aggregate Roots dürfen nur über Repositories geladen werden
    @ArchTest
    static final ArchRule entities_should_have_version_annotation =
        classes()
            .that().areAnnotatedWith("jakarta.persistence.Entity")
            .and().resideInAPackage("..entity..")
            .should().beAnnotatedWith("jakarta.persistence.Version")
            .orShould().containAnyFieldsThat(
                JavaClass.Predicates.annotatedWith("jakarta.persistence.Version")
            );
}
```

### 30.2 Semgrep-Regeln

```yaml
rules:
  - id: no-entity-as-request-body
    message: "JPA-Entities dürfen nicht als @RequestBody verwendet werden (Mass Assignment Risiko)"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: |
          @$MAPPING $RET $METHOD(@RequestBody $TYPE $ARG, ...) { ... }
      - metavariable-pattern:
          metavariable: $TYPE
          patterns:
            - pattern-regex: '.*Entity$'

  - id: no-public-setter-on-status
    message: "Öffentliche Setter für Status-Felder zerstören Zustandsschutz"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: |
          public void set$FIELD($TYPE $ARG) { this.$F = $ARG; }
      - metavariable-pattern:
          metavariable: $FIELD
          pattern-regex: '^(Status|TenantId|Role|Permissions|Active|Deleted|Verified|Locked|Balance)$'

  - id: no-bigdecimal-balance
    message: "BigDecimal balance ist Anti-Pattern — Money Value Object verwenden"
    severity: WARNING
    languages: [java]
    patterns:
      - pattern: private BigDecimal balance;
      - pattern: private BigDecimal balance = ...;

  - id: no-data-annotation-on-entity
    message: "@Data auf @Entity generiert kritische Setter — selektives @Getter verwenden"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: |
          @Data
          @Entity
          public class $CLASS { ... }

  - id: no-tenant-id-from-request
    message: "tenantId DARF NICHT aus Request gelesen werden — TenantContext verwenden"
    severity: ERROR
    languages: [java]
    patterns:
      - pattern: $OBJ.setTenantId($REQUEST.tenantId())
      - pattern: $OBJ.setTenantId($REQUEST.getTenantId())

  - id: tenant-exception-information-disclosure
    message: "TenantMismatch-Exception darf Resource-Tenant-ID nicht in Caller-Message preisgeben"
    severity: WARNING
    languages: [java]
    pattern-regex: 'throw new TenantAccessDeniedException\(".*"\s*\+\s*\w*tenant\w*Id'

  - id: no-email-contains-at
    message: "Email-Validierung mit contains('@') ist zu schwach — Regex oder @Email verwenden"
    severity: WARNING
    languages: [java]
    pattern-regex: 'email\.contains\("@"\)'

  - id: int-arithmetic-without-overflow-protection
    message: "int-Addition in Value Object ohne Math.addExact ist Overflow-Risiko"
    severity: WARNING
    languages: [java]
    patterns:
      - pattern: |
          public $TYPE plus($TYPE other) {
            return new $TYPE(value + other.value);
          }
```

### 30.3 CI-Gates

1. ArchUnit-Regeln laufen in jedem Test-Run.
2. Semgrep-Regeln laufen in CI für Pull Requests.
3. Coverage für Domänen-Klassen und Value Objects.
4. Mutation Testing für kritische Aggregate Roots (PIT/Pitest).
5. Unit-Tests ohne Spring Context für 100 % der Aggregate Roots.

---

## 31. Migration und Refactoring

### 31.1 Vorgehen bei anämischen Entities

1. Kritische Fachregeln identifizieren.
2. Setter-Nutzung suchen (`grep -r "setStatus\|setBalance\|setActive"`).
3. Tests für aktuelles Verhalten ergänzen (Charakterisierungstests).
4. Fachlich benannte Methoden am Objekt einführen.
5. Aufrufer schrittweise von Setter-Sequenzen auf Methoden umstellen.
6. Setter für kritische Felder entfernen oder Sichtbarkeit reduzieren (`private`).
7. DTOs und API-Verträge von Entities trennen.
8. Regressionstests ausführen.
9. ArchUnit-Regel ergänzen.

### 31.2 Vorgehen bei God Services

1. Methoden thematisch gruppieren.
2. Use Cases identifizieren.
3. Fachliche Regeln von technischer Orchestrierung trennen.
4. Kleine Application Services extrahieren.
5. Value Objects für wiederkehrende Primitive einführen.
6. Policies oder Domain Services für objektübergreifende Regeln extrahieren.
7. Tests umstellen: erst Charakterisierungstests, dann gezielte Unit Tests.

### 31.3 Vorgehen bei Primitive Obsession

1. Werte mit Validierungslogik identifizieren.
2. Eigene Typen einführen (Records mit Compact Constructor).
3. Vollständige Operationen ergänzen (`plus`, `minus`, `isLessThan`).
4. API-/DTO-Grenzen bewusst mappen.
5. Persistenzmapping prüfen (`@Converter` für JPA).
6. Verwechslungstests und Validierungstests ergänzen.

### 31.4 Vorgehen bei `instanceof`-Kaskaden

1. Typhierarchie identifizieren.
2. Sealed Interface einführen.
3. Pattern Matching for Switch verwenden (Schritt 1).
4. Wenn Verhalten fachlich zum Typ gehört: Polymorphismus einführen (Schritt 2).
5. Tests anpassen.

### 31.5 Vorgehen bei Lombok-Anti-Patterns

1. `@Data`/`@Setter` auf Aggregate Roots identifizieren.
2. Durch `@Getter` selektiv ersetzen.
3. Fachliche Methoden statt Setter einführen.
4. Konstruktor manuell oder via `@Builder` mit Validierung.
5. ArchUnit-Regel `aggregate_roots_should_not_use_lombok_data` aktivieren.

---

## 32. Ausnahmen

Abweichungen sind zulässig, wenn sie begründet sind.

Akzeptable Ausnahmen:

1. Reine CRUD-Verwaltung ohne fachliche Zustandsregeln.
2. Technische DTOs und Read Models.
3. Performance-kritische Hot Paths nach Messung.
4. Framework-Anforderungen (z. B. JPA-`@NoArgsConstructor`).
5. Legacy-Code während kontrollierter Migration.
6. Bewusst funktionaler Code, wenn er klar, testbar und sicher bleibt.

Nicht akzeptable Ausnahmen:

1. „Das war schneller."
2. „Das haben wir immer so gemacht."
3. „Setter sind einfacher."
4. „Der Service kennt die Regel ja."
5. „Die Entity ist nur eine Tabelle."
6. „Das prüfen wir im Controller."
7. „Lombok `@Data` ist Standard."
8. „`BigDecimal` reicht — Currency setzen wir später."

---

## 33. Definition of Done

Code erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Fachliche Invarianten können nicht über öffentliche Setter umgangen werden.
2. Zustandsübergänge erfolgen über fachlich benannte Methoden.
3. Services orchestrieren Use Cases, ersetzen aber nicht das Domänenmodell.
4. Value Objects werden für fachlich relevante Werte verwendet.
5. Value Objects bieten vollständige Operationen (`plus`, `minus`, `isLessThan`).
6. `Money`-Operationen erzwingen Currency-Konsistenz.
7. `int`-Arithmetik in Value Objects verwendet `Math.addExact` / `subtractExact` / `multiplyExact`.
8. Primitive Obsession ist bei sicherheits- oder fachkritischen Werten vermieden.
9. Utility-Klassen dienen nicht als Ablageort für Fachlogik.
10. Vererbung ist fachlich begründet oder durch Komposition ersetzt.
11. Entities werden nicht direkt als API-Requests oder API-Responses verwendet.
12. Tenant-Logik wird über `verifyBelongsTo(TenantContext.currentTenantId())` modelliert.
13. `TenantAccessDeniedException` verhindert Information Disclosure.
14. Sensible Daten werden nicht unkontrolliert durch Getter, Mapper oder `toString()` offengelegt.
15. Sealed Interfaces werden mit Pattern Matching for Switch exhaustiv ausgewertet.
16. Record Patterns werden für komplexe Destructuring-Fälle genutzt.
17. JPA-Entities haben `@GeneratedValue`, `@Version`, `@OneToMany` mit Cascade und Orphan Removal.
18. JPA-Entities mit Tenant-Bezug haben `tenantId` mit `updatable = false` und `@Index`.
19. Aggregate Roots haben ein Repository, interne Objekte nicht.
20. `equals`/`hashCode` für Aggregate Roots verwenden ID-basierten Vergleich mit Null-Check.
21. Bean Validation 3.0 Annotationen werden für Standard-Constraints verwendet.
22. Compact Constructor wird für komplexe Constraints reserviert.
23. `@Data`, `@Setter` (auf Klasse) und public `@AllArgsConstructor` werden nicht auf Aggregate Roots verwendet.
24. Die wichtigsten Fachregeln werden durch Unit Tests ohne Spring Context geprüft.
25. Pull-Request-Review berücksichtigt die Checkliste aus Sektion 29.
26. Cross-References zu QG-JAVA-006 v2, QG-JAVA-019 v2, QG-JAVA-039-01 v2 und QG-JAVA-041 v2 sind beachtet.

---

## 34. Quellen und weiterführende Literatur

### Bücher

* Martin Fowler: Patterns of Enterprise Application Architecture. Addison-Wesley, 2002.
* Eric Evans: Domain-Driven Design: Tackling Complexity in the Heart of Software. Addison-Wesley, 2003.
* Vaughn Vernon: Implementing Domain-Driven Design. Addison-Wesley, 2013.
* Joshua Bloch: Effective Java. 3rd Edition. Addison-Wesley, 2018.
* Vlad Khononov: Learning Domain-Driven Design. O'Reilly, 2021.

### Web-Ressourcen

* Martin Fowler — Anemic Domain Model: <https://martinfowler.com/bliki/AnemicDomainModel.html>
* Martin Fowler — ValueObject: <https://martinfowler.com/bliki/ValueObject.html>
* Martin Fowler — AggregateRoot: <https://martinfowler.com/bliki/DDD_Aggregate.html>

### Java 21 Sprache

* Oracle Java SE 21 — Record Classes: <https://docs.oracle.com/en/java/javase/21/language/records.html>
* Oracle Java SE 21 — Sealed Classes: <https://docs.oracle.com/en/java/javase/21/language/sealed-classes-and-interfaces.html>
* JEP 395 — Records: <https://openjdk.org/jeps/395>
* JEP 409 — Sealed Classes: <https://openjdk.org/jeps/409>
* JEP 440 — Record Patterns: <https://openjdk.org/jeps/440>
* JEP 441 — Pattern Matching for Switch: <https://openjdk.org/jeps/441>

### Spring und JPA

* Spring Framework Documentation: <https://docs.spring.io/spring-framework/reference/>
* Spring Boot Reference: <https://docs.spring.io/spring-boot/reference/>
* Jakarta Persistence 3.1 Specification: <https://jakarta.ee/specifications/persistence/3.1/>
* Hibernate User Guide: <https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html>

### Bean Validation

* Jakarta Bean Validation 3.0: <https://jakarta.ee/specifications/bean-validation/3.0/>
* Hibernate Validator 8 Reference: <https://docs.jboss.org/hibernate/validator/8.0/reference/en-US/html_single/>

### Security

* OWASP — Mass Assignment Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Mass_Assignment_Cheat_Sheet.html>
* OWASP — Web Security Testing Guide (Mass Assignment): <https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/07-Input_Validation_Testing/20-Testing_for_Mass_Assignment>
* OWASP — API Security Top 10 (Mass Assignment): <https://owasp.org/API-Security/editions/2019/en/0xa6-mass-assignment/>

### Test-Frameworks

* ArchUnit User Guide: <https://www.archunit.org/userguide/html/000_Index.html>
* Semgrep Rules: <https://semgrep.dev/r>

---
 