# QG-JAVA-030 — Entwurfsmuster: Behavioral Patterns richtig anwenden

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-030 |
| Titel | Entwurfsmuster: Behavioral Patterns richtig anwenden |
| Status | Accepted / verbindlicher Standard für Verhaltensmuster im Java-Code |
| Version | 1.0.0 |
| Datum | 2025-04-28 |
| Kategorie | Design Patterns / GoF / Verhaltensmuster / Objektorientierung |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, Architektur, QA |
| Java-Baseline | Java 21 |
| Framework-Kontext | Java 21, Spring Boot 3.x, Spring Framework Events, Spring Security Filter Chain, Spring AOP, Spring Transactions |
| Geltungsbereich | Strategy, Observer/Event, Command, Chain of Responsibility, State, Template Method, Iterator, Mediator, Visitor, Memento, fachliche Workflows, Validierungsketten, Event-basierte Entkopplung, Algorithmus-Auswahl, Zustandsautomaten |
| Verbindlichkeit | Verhaltensmuster werden eingesetzt, wenn Verhalten, Ablauf, Zustandswechsel oder Verantwortungsverteilung explizit, testbar und erweiterbar modelliert werden müssen. Sie dürfen nicht verwendet werden, um einfache Kontrollflüsse unnötig zu abstrahieren. |
| Technische Validierung | Gegen GoF-Pattern-Katalog, Refactoring.Guru, Spring Application Events, Spring Security Filter Chain, Spring AOP und Java-21-Sprachmittel eingeordnet |
| Kurzentscheidung | Behavioral Patterns lösen nicht „Wie baue ich Klassen zusammen?“, sondern „Wie verteile ich Verhalten, Entscheidungen, Abläufe und Zustandsänderungen sauber?“. In Java 21 werden viele klassische Muster moderner umgesetzt: Strategy oft mit Interfaces und Lambdas, State mit sealed Interfaces, Observer über Spring Events oder Messaging, Chain über geordnete Handler, Template Method nur bewusst wegen Vererbung, Iterator meist über `Iterable`, `Stream` oder `Spliterator`. |

---

## 1. Zweck

Diese Richtlinie erklärt die wichtigsten Verhaltensmuster und zeigt, wie sie in Java- und Spring-Boot-Anwendungen richtig eingesetzt werden.

Verhaltensmuster beantworten eine andere Frage als Erzeugungs- und Strukturmuster.

Erzeugungsmuster fragen:

```text
Wie entstehen Objekte?
```

Strukturmuster fragen:

```text
Wie verbinde ich Objekte?
```

Verhaltensmuster fragen:

```text
Wer entscheidet was, wann und in welcher Reihenfolge?
```

Damit sind sie besonders wichtig für echte Business-Software. Denn dort geht es fast immer um Verhalten: Welcher Rabatt gilt für welchen Kunden? Was passiert, nachdem eine Bestellung erzeugt wurde? Welche Validierungen müssen nacheinander laufen? Wie wechselt eine Bestellung ihren Status? Wie kann ein Algorithmus austauschbar werden? Wie wird ein Command geloggt, in eine Queue gelegt oder rückgängig gemacht? Wie lassen sich fachliche Nebenwirkungen entkoppeln? Wie verhindert man, dass ein Service fünf andere Services direkt kennt? Wie bleibt ein Workflow testbar, obwohl er mehrere Schritte enthält? Wie vermeidet man endlose `if`, `switch` und `instanceof`-Kaskaden?

Verhaltensmuster helfen, solche Entscheidungen sichtbar und beherrschbar zu machen. Sie sind aber kein Selbstzweck. Ein Pattern ist nur dann gut, wenn der Code danach verständlicher, testbarer oder erweiterbarer ist.

---

## 2. Kurzregel

Nutze Strategy, wenn ein Algorithmus oder fachliches Verfahren austauschbar sein soll. Nutze Observer/Event, wenn eine Zustandsänderung mehrere unabhängige Reaktionen auslösen soll. Nutze Command, wenn eine Aktion als Objekt behandelt, geloggt, verzögert, wiederholt, in eine Queue gelegt oder rückgängig gemacht werden soll. Nutze Chain of Responsibility, wenn mehrere Handler nacheinander entscheiden dürfen. Nutze State, wenn Verhalten stark vom aktuellen Zustand abhängt. Nutze Template Method nur bewusst, wenn ein stabiler Algorithmusrahmen über Vererbung variiert werden soll. Nutze Iterator nicht neu, wenn Java `Iterable`, `Iterator`, `Stream` oder `Spliterator` bereits genügt. Nutze Visitor nur sehr bewusst; in Java 21 sind sealed Interfaces und Pattern Matching oft die bessere Alternative.

---

## 3. Warum Verhaltensmuster im Java-Alltag wichtig sind

Viele schlechte Java-Klassen scheitern nicht an Syntax. Sie scheitern daran, dass Verhalten am falschen Ort liegt.

Ein typisches Beispiel ist Rabattlogik:

```java
if (customer.type() == PREMIUM) {
    ...
} else if (customer.type() == VIP) {
    ...
} else if (campaign.active()) {
    ...
}
```

Am Anfang ist das harmlos. Nach sechs Monaten stehen dieselben Bedingungen in Checkout, Cart Preview, Invoice, Testdaten, Admin UI und Reporting. Jede neue Rabattregel wird zu einer Suchaktion über die gesamte Codebasis.

Ein weiteres Beispiel ist Bestellabschluss:

```java
emailService.sendConfirmation(order);
inventoryService.reserve(order);
loyaltyService.addPoints(order);
analyticsService.track(order);
```

Auch das funktioniert. Aber der Order-Service kennt plötzlich E-Mail, Lager, Loyalty und Analytics. Ein neuer Nebeneffekt ändert den Kernservice. Das verletzt Erweiterbarkeit und Testbarkeit.

Oder Zustand:

```java
if (status == PENDING && action == CONFIRM) ...
if (status == CONFIRMED && action == SHIP) ...
if (status == SHIPPED && action == CANCEL) ...
```

Ab einer gewissen Anzahl von Zuständen und Aktionen wird das zur Fehlerquelle. Dann ist ein State-Modell sauberer.

Behavioral Patterns machen solche Verantwortlichkeiten explizit. Sie helfen Entwicklern, im Daily Business schneller zu erkennen:

```text
Das ist keine if-else-Frage mehr. Das ist eine Modellierungsfrage.
```

---

## 4. Grundlagen kurz erklärt

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Verhaltensmuster | Muster zur Organisation von Verhalten, Abläufen und Entscheidungen. | Strategy, Observer, State |
| Algorithmus | Ein klarer Ablauf zur Lösung eines Problems. | Rabatt berechnen |
| Strategy | Austauschbarer Algorithmus hinter gleichem Interface. | `DiscountStrategy` |
| Observer | Objekt reagiert auf Ereignis eines anderen Objekts. | `@EventListener` |
| Event | Nachricht über eine Zustandsänderung. | `OrderPlacedEvent` |
| Command | Aktion als Objekt. | `CancelOrderCommand` |
| Handler | Objekt, das eine Anfrage bearbeitet oder weitergibt. | Validierungsregel |
| Chain | Kette von Handlern. | Spring Security Filter Chain |
| State | Zustand als Objekt mit eigenem Verhalten. | `PendingState` |
| Template Method | Fester Algorithmusrahmen, variable Teilschritte. | Report-Generator |
| Iterator | Kapselt Traversierung über Elemente. | `Iterator`, `Stream` |
| Mediator | Vermittler koordiniert Kommunikation zwischen Komponenten. | Workflow-Orchestrator |
| Visitor | Neue Operation über Objektstruktur legen. | Export über AST/Tree |
| Memento | Zustand speichern und später wiederherstellen. | Draft-Snapshot |
| Hook | Erweiterungspunkt in einem Algorithmus. | `beforeExecute()` |
| Callback | Funktion/Methode, die später aufgerufen wird. | Event Listener |
| Orchestrierung | Koordination mehrerer Schritte. | Checkout-Prozess |
| Komposition | Verhalten durch Zusammenarbeit von Objekten statt Vererbung. | Strategy statt Template |

---

## 5. Die zentrale Linie

Die wichtigste Linie bei Behavioral Patterns lautet:

```text
Wenn Verhalten wächst, muss Verantwortung sichtbar werden.
```

Ein einzelnes `if` ist kein Problem. Ein einzelnes `switch` ist kein Problem. Ein einzelner Event ist kein Problem.

Problematisch wird es, wenn Verhalten an mehreren Stellen dupliziert wird, immer neue Varianten bekommt, mehrere Services direkt koppelt, Zustandswechsel unübersichtlich macht, Tests schwer lesbar macht, neue Anforderungen alte Klassen ändern, Seiteneffekte im Kernprozess versteckt oder Fachlogik und technische Orchestrierung vermischt.

Dann ist ein Verhaltensmuster oft sinnvoll.

---

## 6. Anwendung im Daily Business eines Java-Entwicklers

Wenn du eine neue fachliche Entscheidung, einen neuen Workflow oder eine neue Variante einbaust, gehst du so vor.

### Schritt 1: Gibt es mehrere Varianten desselben Verhaltens?

Wenn ja, prüfe Strategy.

```text
Rabattberechnung, Steuerberechnung, Versandkosten, Exportformat, Suchranking
```

### Schritt 2: Reagieren mehrere unabhängige Dinge auf ein Ereignis?

Wenn ja, prüfe Observer/Event.

```text
OrderPlaced → Mail, Inventory, Analytics, Loyalty
```

### Schritt 3: Soll eine Aktion gespeichert, wiederholt, verzögert oder rückgängig gemacht werden?

Wenn ja, prüfe Command.

```text
CancelOrderCommand, RetryPaymentCommand, ImportFileCommand
```

### Schritt 4: Gibt es mehrere Prüfungen oder Handler in fester oder konfigurierbarer Reihenfolge?

Wenn ja, prüfe Chain of Responsibility.

```text
Validierung, Security Filter, Import-Pipeline
```

### Schritt 5: Hängt Verhalten stark vom Zustand ab?

Wenn ja, prüfe State.

```text
Order: Pending, Confirmed, Shipped, Delivered, Cancelled
```

### Schritt 6: Gibt es einen festen Algorithmusrahmen mit variablen Teilschritten?

Wenn ja, prüfe Template Method. Prüfe aber zuerst, ob Strategy oder Komposition besser ist.

### Schritt 7: Geht es nur um Traversierung?

Nutze zuerst Java Standardmittel: `Iterable`, `Iterator`, `Stream`, `Spliterator`.

### Schritt 8: Ist das Pattern wirklich einfacher als direkter Code?

Wenn nein, kein Pattern einführen.

---

## 7. Entscheidungsmatrix

| Problem | Muster | Details/Erklärung |
|---|---|---|
| Algorithmus zur Laufzeit austauschen | Strategy | Rabatt, Versand, Steuer, Ranking |
| Mehrere Reaktionen auf Zustandsänderung | Observer / Event | Bestellung erzeugt, User registriert |
| Aktion als Objekt speichern/ausführen | Command | Undo, Queue, Retry, Audit |
| Anfrage durch mehrere Handler schicken | Chain of Responsibility | Validierung, Security, Filter |
| Verhalten hängt vom Zustand ab | State | Statusmaschine |
| Fester Algorithmus, variable Schritte | Template Method | Report-/Import-/Export-Flow |
| Traversierung kapseln | Iterator | Baum, Graph, Paging |
| Komponenten sollen nicht direkt miteinander reden | Mediator | Workflow-Orchestrator |
| Operation über Objektstruktur | Visitor | AST, Tree, Exporter |
| Zustand wiederherstellen | Memento | Draft, Editor, Workflow-Snapshot |

---

# Teil A — Strategy

## 8. Strategy

### 8.1 Bedeutung

Strategy macht Algorithmen austauschbar. Die aufrufende Klasse kennt nur ein gemeinsames Interface, aber nicht die konkrete Implementierung.

Einfach gesagt:

```text
Strategy = austauschbare fachliche Entscheidung.
```

Typische Einsatzfälle sind Rabattberechnung, Steuerberechnung, Versandkosten, Zahlungsprovider-Auswahl, Suchranking, Sortierung, Validierungsvarianten, Preisberechnung, Exportformat und Tenant-spezifische Regeln.

### 8.2 Schlechtes Beispiel: Algorithmus hardcodiert

```java
public class DiscountService {

    public Money calculateDiscount(Customer customer, Money price) {
        return switch (customer.type()) {
            case PREMIUM -> price.multiply(new BigDecimal("0.20"));
            case VIP -> price.multiply(new BigDecimal("0.30"));
            case REGULAR -> price.multiply(new BigDecimal("0.05"));
            case NEW -> Money.zero(price.currency());
        };
    }
}
```

Probleme: Neue Kundentypen ändern bestehende Klasse. Tests wachsen in einer Klasse. OCP wird verletzt. Fachliche Regeln werden zentral aufgebläht. Sonderregeln werden schnell unübersichtlich.

### 8.3 Gutes Beispiel: Strategy Interface

```java
public interface DiscountStrategy {

    boolean appliesTo(Customer customer, Cart cart);

    Money calculateDiscount(Customer customer, Cart cart);
}
```

```java
@Component
public class PremiumCustomerDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean appliesTo(Customer customer, Cart cart) {
        return customer.type() == CustomerType.PREMIUM;
    }

    @Override
    public Money calculateDiscount(Customer customer, Cart cart) {
        return cart.total().multiply(new BigDecimal("0.20"));
    }
}
```

```java
@Component
public class VipCustomerDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean appliesTo(Customer customer, Cart cart) {
        return customer.type() == CustomerType.VIP;
    }

    @Override
    public Money calculateDiscount(Customer customer, Cart cart) {
        return cart.total().multiply(new BigDecimal("0.30"));
    }
}
```

### 8.4 Strategy Service mit Spring

```java
@Service
public class DiscountService {

    private final List<DiscountStrategy> strategies;

    public DiscountService(List<DiscountStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public Money calculateDiscount(Customer customer, Cart cart) {
        return strategies.stream()
                .filter(strategy -> strategy.appliesTo(customer, cart))
                .map(strategy -> strategy.calculateDiscount(customer, cart))
                .reduce(Money.zero(cart.currency()), Money::add);
    }
}
```

### 8.5 Wenn genau eine Strategy gelten darf

```java
public Money calculateDiscount(Customer customer, Cart cart) {
    var matchingStrategies = strategies.stream()
            .filter(strategy -> strategy.appliesTo(customer, cart))
            .toList();

    if (matchingStrategies.isEmpty()) {
        return Money.zero(cart.currency());
    }

    if (matchingStrategies.size() > 1) {
        throw new AmbiguousDiscountStrategyException(customer.id(), matchingStrategies.size());
    }

    return matchingStrategies.getFirst().calculateDiscount(customer, cart);
}
```

### 8.6 Strategy mit Priorität

```java
public interface DiscountStrategy {

    int priority();

    boolean appliesTo(Customer customer, Cart cart);

    Money calculateDiscount(Customer customer, Cart cart);
}
```

```java
@Service
public class DiscountService {

    private final List<DiscountStrategy> strategies;

    public DiscountService(List<DiscountStrategy> strategies) {
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(DiscountStrategy::priority))
                .toList();
    }
}
```

### 8.7 Strategy mit Lambdas

Für kleine Algorithmen kann `@FunctionalInterface` sinnvoll sein.

```java
@FunctionalInterface
public interface SortStrategy<T> {

    List<T> sort(List<T> items);
}
```

```java
SortStrategy<Integer> ascending =
        items -> items.stream().sorted().toList();

SortStrategy<Integer> descending =
        items -> items.stream().sorted(Comparator.reverseOrder()).toList();
```

Für Spring-Services, fachliche Regeln und testbare Businesslogik sind benannte Klassen oft besser lesbar als anonyme Lambdas.

### 8.8 Typischer Fehler: String-basierte Strategy-Auswahl

Schlecht:

```java
discountService.calculate(customer, cart, "VIP");
```

Besser:

```java
discountService.calculate(customer, cart, DiscountPolicy.VIP);
```

Oder noch besser: Die Strategy entscheidet selbst über `appliesTo(...)`.

### 8.9 Review-Frage

```text
Ist diese switch-/if-Kaskade eigentlich eine austauschbare fachliche Strategie?
```

---

# Teil B — Observer / Event

## 9. Observer / Event

### 9.1 Bedeutung

Observer benachrichtigt abhängige Komponenten über Ereignisse, ohne diese Komponenten direkt zu kennen.

Einfach gesagt:

```text
Observer = etwas ist passiert, wer sich interessiert, reagiert.
```

Typische Einsatzfälle sind Bestellung wurde aufgegeben, User wurde registriert, Zahlung wurde bestätigt, Angebot wurde veröffentlicht, Merchant wurde verifiziert, Datei wurde importiert, Passwort wurde geändert.

### 9.2 Schlechtes Beispiel: direkte Kopplung

```java
@Service
public class OrderService {

    public OrderId placeOrder(CreateOrderCommand command) {
        var order = createAndSaveOrder(command);

        emailService.sendConfirmation(order);
        inventoryService.reserve(order.items());
        loyaltyService.addPoints(order.customerId());
        analyticsService.trackOrderPlaced(order);

        return order.id();
    }
}
```

Probleme: OrderService kennt alle Nebenwirkungen. Neue Reaktion ändert OrderService. Tests brauchen viele Mocks. Fehlerbehandlung ist unklar. Transaktionszeit kann wachsen. Kernlogik und Integrationslogik vermischen sich.

### 9.3 Gutes Beispiel: Spring Event

Event:

```java
public record OrderPlacedEvent(
        OrderId orderId,
        CustomerId customerId,
        Money total,
        Instant occurredAt
) {}
```

Publisher:

```java
@Service
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderId placeOrder(CreateOrderCommand command) {
        var order = createAndSaveOrder(command);

        eventPublisher.publishEvent(new OrderPlacedEvent(
                order.id(),
                order.customerId(),
                order.total(),
                Instant.now()
        ));

        return order.id();
    }
}
```

Listener:

```java
@Component
public class OrderConfirmationListener {

    private final EmailService emailService;

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        emailService.sendOrderConfirmation(event.orderId(), event.customerId());
    }
}
```

### 9.4 Wichtig: `@EventListener` ist standardmäßig synchron

Spring Events sind nicht automatisch asynchron. Standardmäßig läuft ein `@EventListener` im selben Thread. Wenn der Listener langsam ist, kann der ursprüngliche Aufruf langsam werden.

Asynchron:

```java
@Component
public class OrderAnalyticsListener {

    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        analyticsService.track(event);
    }
}
```

Aber: `@Async` verändert Fehlerverhalten, Thread-Kontext, MDC, SecurityContext und Transaktionsnähe. Deshalb bewusst einsetzen.

### 9.5 Transactional Events

Wenn ein Event erst nach erfolgreichem Datenbank-Commit verarbeitet werden soll, nutze `@TransactionalEventListener`.

```java
@Component
public class OrderConfirmationListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        emailService.sendOrderConfirmation(event.orderId(), event.customerId());
    }
}
```

Das verhindert E-Mails für Bestellungen, deren Transaktion später zurückgerollt wurde.

### 9.6 Für verteilte Systeme: Outbox statt nur Spring Event

Spring Events sind In-Process Events. Sie verlassen nicht automatisch den Serviceprozess.

Für Microservices, Kafka oder zuverlässige Integration gilt:

```text
DB-Änderung + Event-Publishing brauchen Outbox Pattern.
```

Nicht:

```java
orderRepository.save(order);
kafkaTemplate.send("orders.placed", event);
```

wenn garantiert werden muss, dass beides konsistent passiert.

### 9.7 Event-Design

Gute Events enthalten stabile IDs, fachliche Bedeutung, Zeitpunkt, Version, Tenant-/Kontextinformation wenn nötig, keine vollständigen internen Entities, keine sensitiven Daten und keine Lazy-Loading-Objekte.

Schlecht:

```java
public record OrderPlacedEvent(OrderEntity order) {}
```

Besser:

```java
public record OrderPlacedEvent(
        UUID eventId,
        String eventVersion,
        TenantId tenantId,
        OrderId orderId,
        CustomerId customerId,
        Money total,
        Instant occurredAt
) {}
```

### 9.8 Review-Frage

```text
Ist das eine echte fachliche Reaktion auf ein Ereignis oder verstecken wir nur synchrone Pflichtlogik?
```

Nicht alles gehört in Events. Wenn ein Schritt zwingend Teil der Transaktion ist, muss er oft im Use Case bleiben.

---

# Teil C — Command

## 10. Command

### 10.1 Bedeutung

Command kapselt eine Anfrage oder Aktion als Objekt.

Einfach gesagt:

```text
Command = Aktion als Objekt.
```

Typische Einsatzfälle sind Undo/Redo, Warteschlange, Retry, Audit, Logging, Batch-Verarbeitung, Scheduling, User-Aktionen speichern, Workflow-Schritte und kompensierende Aktion.

Wichtig: Ein DTO mit dem Namen `CreateOrderCommand` ist noch nicht automatisch das GoF Command Pattern. In vielen modernen Java-Systemen heißen Eingabeobjekte „Command“, obwohl sie nur Daten tragen. Das ist okay, aber man muss die Begriffe sauber trennen.

### 10.2 Request Command als DTO

```java
public record CreateOrderCommand(
        CustomerId customerId,
        List<OrderItemRequest> items,
        Address shippingAddress
) {}
```

Das ist ein Application Command im Sinne von Use-Case-Eingabe. Es enthält Daten, aber keine `execute()`-Methode.

### 10.3 GoF Command mit `execute()`

```java
public interface OrderCommand {

    OrderCommandResult execute();
}
```

```java
public final class CancelOrderCommand implements OrderCommand {

    private final OrderId orderId;
    private final OrderRepository orderRepository;

    public CancelOrderCommand(OrderId orderId, OrderRepository orderRepository) {
        this.orderId = orderId;
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderCommandResult execute() {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();
        orderRepository.save(order);

        return OrderCommandResult.success(orderId);
    }
}
```

### 10.4 Command Handler / Invoker

```java
@Service
public class OrderCommandHandler {

    public OrderCommandResult execute(OrderCommand command) {
        return command.execute();
    }
}
```

### 10.5 Undo ist in Business-Systemen schwierig

Naives Undo:

```java
order.restoreStatus(previousStatus);
```

ist in echten Systemen oft falsch, weil Nebenwirkungen passiert sein können: E-Mail verschickt, Zahlung ausgelöst, Inventory reserviert, Event publiziert, Rechnung erstellt, Audit geschrieben.

In Business-Systemen ist oft nicht „Undo“, sondern „Compensation“ korrekt.

Beispiel:

```java
public interface CompensatingCommand {

    CommandResult execute();

    CommandResult compensate();
}
```

```java
public final class ReserveInventoryCommand implements CompensatingCommand {

    @Override
    public CommandResult execute() {
        inventory.reserve(orderId, items);
        return CommandResult.success();
    }

    @Override
    public CommandResult compensate() {
        inventory.release(orderId, items);
        return CommandResult.success();
    }
}
```

### 10.6 Command für Queues

```java
public record RetryPaymentCommand(
        PaymentId paymentId,
        OrderId orderId,
        int attempt,
        Instant scheduledAt
) {}
```

Command wird gespeichert oder in eine Queue gelegt. Ein Handler verarbeitet ihn später.

```java
@Component
public class RetryPaymentCommandHandler {

    public void handle(RetryPaymentCommand command) {
        paymentService.retry(command.paymentId(), command.attempt());
    }
}
```

### 10.7 Review-Frage

```text
Brauchen wir hier wirklich eine ausführbare Aktion als Objekt oder reicht ein normales Request-DTO?
```

---

# Teil D — Chain of Responsibility

## 11. Chain of Responsibility

### 11.1 Bedeutung

Chain of Responsibility schickt eine Anfrage durch eine Kette von Handlern. Jeder Handler kann die Anfrage bearbeiten, ablehnen oder weitergeben.

Einfach gesagt:

```text
Chain = mehrere Verantwortliche prüfen nacheinander.
```

Typische Einsatzfälle sind Validierungsketten, Security Filter, Import-Pipelines, Request-Filter, Fraud Checks, Freigabeprozesse, Pre-Processing und Fehlerbehandlungsketten.

### 11.2 Klassisches Beispiel: abstrakte Basisklasse

```java
public abstract class OrderValidationHandler {

    private OrderValidationHandler next;

    public OrderValidationHandler then(OrderValidationHandler next) {
        this.next = next;
        return next;
    }

    public final ValidationResult validate(CreateOrderCommand command) {
        var result = doValidate(command);

        if (!result.isValid()) {
            return result;
        }

        return next != null
                ? next.validate(command)
                : ValidationResult.valid();
    }

    protected abstract ValidationResult doValidate(CreateOrderCommand command);
}
```

```java
public final class UserExistsValidator extends OrderValidationHandler {

    @Override
    protected ValidationResult doValidate(CreateOrderCommand command) {
        return userRepository.existsById(command.customerId())
                ? ValidationResult.valid()
                : ValidationResult.invalid("customer does not exist");
    }
}
```

### 11.3 Moderne Spring-Variante: Liste geordneter Validatoren

In Spring ist oft eine Liste von Handlern einfacher und testbarer als eine manuell verkettete Objektstruktur.

```java
public interface OrderValidator {

    ValidationResult validate(CreateOrderCommand command);
}
```

```java
@Component
@Order(10)
public class UserExistsOrderValidator implements OrderValidator {

    @Override
    public ValidationResult validate(CreateOrderCommand command) {
        ...
    }
}
```

```java
@Component
@Order(20)
public class StockAvailableOrderValidator implements OrderValidator {

    @Override
    public ValidationResult validate(CreateOrderCommand command) {
        ...
    }
}
```

```java
@Service
public class OrderValidationChain {

    private final List<OrderValidator> validators;

    public OrderValidationChain(List<OrderValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    public ValidationResult validate(CreateOrderCommand command) {
        for (var validator : validators) {
            var result = validator.validate(command);
            if (!result.isValid()) {
                return result;
            }
        }

        return ValidationResult.valid();
    }
}
```

### 11.4 Alle Fehler sammeln statt beim ersten Fehler stoppen

Manchmal soll die Chain nicht beim ersten Fehler abbrechen, sondern alle Fehler sammeln.

```java
public ValidationResult validateAll(CreateOrderCommand command) {
    var errors = validators.stream()
            .map(validator -> validator.validate(command))
            .filter(result -> !result.isValid())
            .flatMap(result -> result.errors().stream())
            .toList();

    return errors.isEmpty()
            ? ValidationResult.valid()
            : ValidationResult.invalid(errors);
}
```

### 11.5 Spring Security als Chain

Spring Security nutzt eine Filterkette. Mehrere Filter haben jeweils eigene Verantwortung: Authentifizierung, Autorisierung, CSRF, Session, Header, Exception Handling und weitere Sicherheitsaufgaben. Die Reihenfolge ist entscheidend.

Das ist ein praktisches Beispiel dafür, dass Chain of Responsibility nicht nur Theorie ist.

### 11.6 Review-Frage

```text
Ist die Reihenfolge der Handler fachlich relevant und durch Tests abgesichert?
```

---

# Teil E — State

## 12. State

### 12.1 Bedeutung

State kapselt zustandsabhängiges Verhalten in eigene Zustandsobjekte.

Einfach gesagt:

```text
State = jeder Zustand weiß, was in ihm erlaubt ist.
```

Typische Einsatzfälle sind Order Lifecycle, Payment Lifecycle, Ticket Lifecycle, Workflow Engine, Import Job, Campaign Publishing, Subscription Lifecycle und Approval Process.

### 12.2 Schlechtes Beispiel: Status-String und if-else

```java
public class Order {

    private String status = "PENDING";

    public void confirm() {
        if ("PENDING".equals(status)) {
            status = "CONFIRMED";
        } else if ("CONFIRMED".equals(status)) {
            throw new IllegalStateException("already confirmed");
        } else {
            throw new IllegalStateException("cannot confirm from " + status);
        }
    }

    public void ship() {
        if ("CONFIRMED".equals(status)) {
            status = "SHIPPED";
        } else {
            throw new IllegalStateException("cannot ship from " + status);
        }
    }
}
```

Probleme: String statt Typ, ungültige Werte möglich, viele if-else-Zweige, Zustandslogik verteilt sich, neue Zustände ändern viele Methoden, Fehler erst zur Laufzeit.

### 12.3 Gute Anwendung mit sealed Interface

```java
public sealed interface OrderState
        permits PendingState, ConfirmedState, ShippedState, DeliveredState, CancelledState {

    OrderState confirm();

    OrderState ship();

    OrderState deliver();

    OrderState cancel();

    OrderStatus status();
}
```

```java
public record PendingState() implements OrderState {

    @Override
    public OrderState confirm() {
        return new ConfirmedState();
    }

    @Override
    public OrderState ship() {
        throw new InvalidOrderTransitionException(OrderStatus.PENDING, "ship");
    }

    @Override
    public OrderState deliver() {
        throw new InvalidOrderTransitionException(OrderStatus.PENDING, "deliver");
    }

    @Override
    public OrderState cancel() {
        return new CancelledState();
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PENDING;
    }
}
```

```java
public record ConfirmedState() implements OrderState {

    @Override
    public OrderState confirm() {
        throw new InvalidOrderTransitionException(OrderStatus.CONFIRMED, "confirm");
    }

    @Override
    public OrderState ship() {
        return new ShippedState();
    }

    @Override
    public OrderState deliver() {
        throw new InvalidOrderTransitionException(OrderStatus.CONFIRMED, "deliver");
    }

    @Override
    public OrderState cancel() {
        return new CancelledState();
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.CONFIRMED;
    }
}
```

Order:

```java
public class Order {

    private OrderState state = new PendingState();

    public void confirm() {
        state = state.confirm();
    }

    public void ship() {
        state = state.ship();
    }

    public void deliver() {
        state = state.deliver();
    }

    public void cancel() {
        state = state.cancel();
    }

    public OrderStatus status() {
        return state.status();
    }
}
```

### 12.4 Persistenz von State

JPA persistiert meist kein State-Objekt direkt. Häufig wird ein Enum persistiert und beim Laden in State übersetzt.

```java
public final class OrderStateFactory {

    public static OrderState from(OrderStatus status) {
        return switch (status) {
            case PENDING -> new PendingState();
            case CONFIRMED -> new ConfirmedState();
            case SHIPPED -> new ShippedState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
        };
    }
}
```

Entity:

```java
@Enumerated(EnumType.STRING)
private OrderStatus status;
```

Domain:

```java
private OrderState state;
```

Mapping:

```java
order.restoreState(OrderStateFactory.from(entity.status()));
```

### 12.5 Wann Enum reicht

Ein Enum reicht, wenn es wenige Zustände gibt, Verhalten nicht stark variiert, Übergänge einfach sind und keine komplexen Regeln pro Zustand existieren.

State Pattern lohnt sich, wenn viele Aktionen je Zustand existieren, Übergänge fachlich kritisch sind, Fehlübergänge teuer sind, Statuslogik wächst und Tests pro Zustand wichtig sind.

### 12.6 Review-Frage

```text
Wird hier eine Zustandsmaschine mit if-else gebaut, die eigentlich eigene Zustände braucht?
```

---

# Teil F — Template Method

## 13. Template Method

### 13.1 Bedeutung

Template Method definiert einen festen Algorithmusrahmen in einer Basisklasse. Einzelne Schritte werden von Unterklassen überschrieben.

Einfach gesagt:

```text
Template Method = Ablauf ist fix, einzelne Schritte sind variabel.
```

Typische Einsatzfälle sind Report-Generierung, Import-Pipeline, Export-Pipeline, Batch-Verarbeitung, Parser, Test-Fixtures und Framework-Hooks.

### 13.2 Beispiel

```java
public abstract class ReportGenerator {

    public final Report generate(ReportRequest request) {
        validate(request);
        var data = fetchData(request);
        var processed = processData(data);
        var report = formatReport(processed);
        audit(request, report);
        return report;
    }

    protected void validate(ReportRequest request) {
        Objects.requireNonNull(request);
    }

    protected abstract List<DataRow> fetchData(ReportRequest request);

    protected abstract List<DataRow> processData(List<DataRow> rows);

    protected abstract Report formatReport(List<DataRow> rows);

    protected void audit(ReportRequest request, Report report) {
        // Default Hook
    }
}
```

```java
public final class SalesCsvReportGenerator extends ReportGenerator {

    @Override
    protected List<DataRow> fetchData(ReportRequest request) {
        return salesRepository.findRows(request);
    }

    @Override
    protected List<DataRow> processData(List<DataRow> rows) {
        return salesAggregator.aggregate(rows);
    }

    @Override
    protected Report formatReport(List<DataRow> rows) {
        return csvFormatter.format(rows);
    }
}
```

### 13.3 Risiken

Template Method nutzt Vererbung. Das ist starke Kopplung.

Risiken: Basisklasse wird zu mächtig. Unterklassen hängen an internen Abläufen. Änderungen am Template brechen Subklassen. Testbarkeit kann schlechter werden. Hooks werden unklar. Reihenfolge ist implizit in Basisklasse versteckt.

### 13.4 Alternative mit Komposition

Oft ist Strategy/Composition besser.

```java
public interface DataFetcher {
    List<DataRow> fetch(ReportRequest request);
}

public interface DataProcessor {
    List<DataRow> process(List<DataRow> rows);
}

public interface ReportFormatter {
    Report format(List<DataRow> rows);
}
```

```java
public class ReportGenerationService {

    private final DataFetcher fetcher;
    private final DataProcessor processor;
    private final ReportFormatter formatter;

    public Report generate(ReportRequest request) {
        var data = fetcher.fetch(request);
        var processed = processor.process(data);
        return formatter.format(processed);
    }
}
```

### 13.5 Review-Frage

```text
Brauchen wir hier wirklich Vererbung oder ist Strategy/Komposition einfacher?
```

---

# Teil G — Iterator

## 14. Iterator

### 14.1 Bedeutung

Iterator kapselt, wie über eine Menge von Elementen gelaufen wird.

Einfach gesagt:

```text
Iterator = wie komme ich zum nächsten Element, ohne die interne Struktur zu kennen?
```

Java hat Iterator bereits eingebaut: `Iterator`, `Iterable`, `for-each`, `Stream`, `Spliterator`. Deshalb wird Iterator fast nie neu als Pattern implementiert.

### 14.2 Moderne Java-Nutzung

```java
orderRepository.findAll()
        .stream()
        .filter(order -> order.status() == OrderStatus.PENDING)
        .map(orderMapper::toDto)
        .toList();
```

### 14.3 Eigenen Iterator nur bei komplexem Traversal

Beispiel: Baumstruktur.

```java
public class OrderTreeIterator implements Iterator<Order> {

    private final Deque<Order> stack = new ArrayDeque<>();

    public OrderTreeIterator(Order root) {
        stack.push(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public Order next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        var order = stack.pop();

        order.subOrders().stream()
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .forEach(stack::push);

        return order;
    }
}
```

### 14.4 Besser: Iterable anbieten

```java
public class OrderTree implements Iterable<Order> {

    private final Order root;

    @Override
    public Iterator<Order> iterator() {
        return new OrderTreeIterator(root);
    }
}
```

Nutzung:

```java
for (var order : orderTree) {
    ...
}
```

### 14.5 Review-Frage

```text
Kann Java Stream/Iterable das schon, oder bauen wir unnötig eigenen Traversal-Code?
```

---

# Teil H — Mediator

## 15. Mediator

### 15.1 Bedeutung

Mediator koordiniert Kommunikation zwischen Komponenten, damit diese nicht alle direkt voneinander abhängen.

Einfach gesagt:

```text
Mediator = Vermittler zwischen Komponenten.
```

Typische Einsatzfälle sind komplexer UI-Dialog, Workflow-Orchestrator, Checkout-Orchestrierung, Saga-Koordination, Prozesssteuerung und Multi-Step Use Case.

### 15.2 Schlechtes Beispiel: Services rufen sich gegenseitig

```java
paymentService.setInventoryService(inventoryService);
inventoryService.setNotificationService(notificationService);
notificationService.setPaymentService(paymentService);
```

Das erzeugt zyklische Abhängigkeiten.

### 15.3 Gute Anwendung: Workflow-Mediator

```java
@Service
public class CheckoutWorkflow {

    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher events;

    @Transactional
    public CheckoutResult checkout(CheckoutCommand command) {
        inventoryService.reserve(command.items());

        var payment = paymentService.charge(command.payment(), command.total());

        if (!payment.succeeded()) {
            inventoryService.release(command.items());
            return CheckoutResult.paymentFailed(payment.failureReason());
        }

        var order = Order.place(command, payment.transactionId());
        orderRepository.save(order);

        events.publishEvent(new OrderPlacedEvent(order.id()));

        return CheckoutResult.success(order.id());
    }
}
```

### 15.4 Mediator vs. Facade

| Aspekt | Mediator | Facade |
|---|---|---|
| Zweck | Kommunikation/Koordination zwischen Komponenten steuern | Komplexes Subsystem vereinfachen |
| Fokus | Verhalten und Interaktion | Schnittstelle und Vereinfachung |
| Beispiel | CheckoutWorkflow steuert Ablauf | CheckoutFacade bietet einfachen Eingang |
| Risiko | God Workflow | God Facade |

In der Praxis überschneiden sie sich. Wichtig ist: Der Mediator darf nicht alle Businesslogik aufsaugen.

### 15.5 Review-Frage

```text
Koordiniert diese Klasse nur den Ablauf oder enthält sie plötzlich alle Fachregeln?
```

---

# Teil I — Visitor

## 16. Visitor

### 16.1 Bedeutung

Visitor erlaubt, neue Operationen über eine Objektstruktur zu legen, ohne die Objektklassen selbst zu ändern.

Einfach gesagt:

```text
Visitor = neue Operation über bestehende Struktur.
```

Typische Einsatzfälle sind Abstract Syntax Tree, Dokumentbaum, Regelbaum, Exporter, statische Analyse, Compiler/Parser und komplexe Objektstrukturen mit vielen Operationen.

### 16.2 Klassischer Visitor

```java
public interface DocumentElement {

    void accept(DocumentVisitor visitor);
}
```

```java
public final class Heading implements DocumentElement {

    private final String text;

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visitHeading(this);
    }
}
```

```java
public final class Paragraph implements DocumentElement {

    private final String text;

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visitParagraph(this);
    }
}
```

```java
public interface DocumentVisitor {

    void visitHeading(Heading heading);

    void visitParagraph(Paragraph paragraph);
}
```

```java
public final class HtmlExportVisitor implements DocumentVisitor {

    @Override
    public void visitHeading(Heading heading) {
        ...
    }

    @Override
    public void visitParagraph(Paragraph paragraph) {
        ...
    }
}
```

### 16.3 Java-21-Alternative: sealed + switch

In Java 21 ist klassischer Visitor oft nicht mehr nötig, wenn die Typmenge geschlossen ist.

```java
public sealed interface DocumentElement
        permits Heading, Paragraph, ImageBlock {
}
```

```java
public record Heading(String text) implements DocumentElement {}
public record Paragraph(String text) implements DocumentElement {}
public record ImageBlock(String url, String altText) implements DocumentElement {}
```

```java
public String toHtml(DocumentElement element) {
    return switch (element) {
        case Heading heading -> "<h1>%s</h1>".formatted(escape(heading.text()));
        case Paragraph paragraph -> "<p>%s</p>".formatted(escape(paragraph.text()));
        case ImageBlock image -> "<img src=\"%s\" alt=\"%s\" />"
                .formatted(escape(image.url()), escape(image.altText()));
    };
}
```

Der Compiler prüft Vollständigkeit, wenn `DocumentElement` sealed ist.

### 16.4 Wann Visitor trotzdem sinnvoll ist

Visitor kann sinnvoll bleiben, wenn sehr viele Operationen über stabile Objektstruktur laufen, Operationen eigene Zustände brauchen, externe Erweiterungen möglich sein sollen, man klassische Double-Dispatch-Strukturen hat oder man bestehende OOP-Strukturen nicht auf sealed/switch umbauen kann.

### 16.5 Review-Frage

```text
Ist Visitor hier wirklich besser als sealed interface + pattern switch?
```

---

# Teil J — Memento

## 17. Memento

### 17.1 Bedeutung

Memento speichert einen Zustand, um ihn später wiederherstellen zu können, ohne interne Details offenzulegen.

Einfach gesagt:

```text
Memento = sicherer Snapshot.
```

Typische Einsatzfälle sind Drafts, Undo in Editor, Workflow-Snapshots, Import-Zwischenstand, Simulation, Wizard-Formulare und temporäre Benutzerentwürfe.

### 17.2 Beispiel

```java
public final class CampaignDraft {

    private String title;
    private String description;
    private Money discountedPrice;
    private LocalDate validUntil;

    public CampaignDraftSnapshot snapshot() {
        return new CampaignDraftSnapshot(
                title,
                description,
                discountedPrice,
                validUntil,
                Instant.now()
        );
    }

    public void restore(CampaignDraftSnapshot snapshot) {
        this.title = snapshot.title();
        this.description = snapshot.description();
        this.discountedPrice = snapshot.discountedPrice();
        this.validUntil = snapshot.validUntil();
    }
}
```

```java
public record CampaignDraftSnapshot(
        String title,
        String description,
        Money discountedPrice,
        LocalDate validUntil,
        Instant capturedAt
) {}
```

### 17.3 Risiken

Snapshots können sensitive Daten enthalten. Snapshots brauchen Aufbewahrungsregeln. Alte Snapshots können inkompatibel mit neuer Modellversion werden. Bei persistenten Systemen ist Audit/Versioning oft besser als naives Memento.

### 17.4 Review-Frage

```text
Ist ein Snapshot fachlich erlaubt, sicher und versionierbar?
```

---

# Teil K — Interpreter

## 18. Interpreter

### 18.1 Bedeutung

Interpreter modelliert eine kleine Sprache oder Ausdruckslogik als Objektstruktur.

Einfach gesagt:

```text
Interpreter = kleine Regel-/Ausdruckssprache auswerten.
```

Typische Einsatzfälle sind Suchfilter, Regel-Engine, Segmentierungsregeln, Rabattregeln, Berechtigungsregeln und einfache DSL.

### 18.2 Vorsicht

Interpreter ist schnell Overengineering. In modernen Java-Systemen sind oft besser: Specification Pattern, Query DSL, bestehende Rule Engine, Spring Expression Language, Datenbank-Query oder klare Strategy-Implementierungen.

### 18.3 Kleines Beispiel: Specification statt Interpreter

```java
public interface CustomerSpecification {

    boolean isSatisfiedBy(Customer customer);
}
```

```java
public record IsPremiumCustomer() implements CustomerSpecification {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.type() == CustomerType.PREMIUM;
    }
}
```

```java
public record HasMinimumOrderCount(int minimum) implements CustomerSpecification {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.orderCount() >= minimum;
    }
}
```

### 18.4 Review-Frage

```text
Bauen wir gerade eine eigene Sprache, obwohl einfache Strategies oder Specifications reichen?
```

---

# Teil L — Spring und Behavioral Patterns

## 19. Spring-Bezüge

| Spring-Funktion | Musterbezug | Erklärung |
|---|---|---|
| `@EventListener` | Observer | Listener reagiert auf Application Event |
| `ApplicationEventPublisher` | Observer/Event | Ereignisse publizieren |
| `@TransactionalEventListener` | Observer/Event | Listener an Transaktionsphase binden |
| Spring Security Filter Chain | Chain of Responsibility | Filter prüfen nacheinander |
| Servlet Filter | Chain of Responsibility | Request durch Filterkette |
| HandlerInterceptor | Chain/Template | Vor/Nach Controller-Verarbeitung |
| `@Transactional` Proxy | Proxy/Template | Verhalten um Methode herum |
| `@Async` | Command/Event/Proxy | Ausführung in anderem Thread |
| `List<Interface>` Injection | Strategy/Chain | alle Implementierungen injizieren |
| `@Order` | Chain/Strategy | Reihenfolge definieren |
| Spring Batch | Template/Command/Chain | Jobs, Steps, Reader/Processor/Writer |

---

# Teil M — Security- und SaaS-Aspekte

## 20. Strategy und Tenant-Regeln

In SaaS-Systemen können Regeln pro Tenant variieren. Strategy kann helfen, darf aber Tenant-Isolation nicht verstecken.

```java
public interface PricingStrategy {

    boolean appliesTo(TenantId tenantId, Product product);

    Money calculate(TenantId tenantId, Product product);
}
```

Nicht:

```java
static TenantId currentTenant;
```

Tenant-Kontext wird explizit übergeben oder sicher aus einem AccessScope gelesen.

## 21. Events ohne PII

Events dürfen keine unnötigen personenbezogenen Daten enthalten.

Schlecht:

```java
public record UserRegisteredEvent(String email, String fullName, String phone) {}
```

Besser:

```java
public record UserRegisteredEvent(UserId userId, TenantId tenantId, Instant occurredAt) {}
```

Listener kann bei Bedarf berechtigt nachladen.

## 22. Command und Audit

Commands eignen sich für Audit, wenn sie sauber serialisierbar und frei von Secrets sind.

```java
public record DisableMerchantCommand(
        MerchantId merchantId,
        UserId requestedBy,
        String reason
) {}
```

Vorsicht: Reason-Felder können Freitext und PII enthalten. Logging und Speicherung müssen kontrolliert werden.

## 23. Chain und Security

Security-Ketten müssen deterministisch und getestet sein. Reihenfolge ist kritisch.

```text
Authentication vor Authorization.
Tenant Resolution vor Tenant Access Check.
Input Validation vor Business Processing.
```

## 24. State und Berechtigungen

Zustandsübergänge müssen Berechtigungen berücksichtigen.

```java
public void publish(AccessScope accessScope) {
    accessPolicy.assertCanPublish(accessScope, this);
    state = state.publish();
}
```

State allein ersetzt keine Autorisierung.

---

# Teil N — Testing

## 25. Strategy testen

```java
class DiscountServiceTest {

    @Test
    void calculateDiscount_appliesPremiumStrategy_forPremiumCustomer() {
        var strategy = new PremiumCustomerDiscountStrategy();
        var service = new DiscountService(List.of(strategy));

        var discount = service.calculateDiscount(
                CustomerFixture.premiumCustomer(),
                CartFixture.cartWithTotal(Money.eur("100.00"))
        );

        assertThat(discount).isEqualTo(Money.eur("20.00"));
    }
}
```

## 26. Observer/Event testen

```java
@SpringBootTest
class OrderEventTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @SpyBean
    OrderConfirmationListener listener;

    @Test
    void orderPlacedEvent_isHandledByConfirmationListener() {
        var event = new OrderPlacedEvent(
                new OrderId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                Money.eur("100.00"),
                Instant.now()
        );

        publisher.publishEvent(event);

        verify(listener).onOrderPlaced(event);
    }
}
```

Für reine Unit-Tests ist oft besser:

```java
listener.onOrderPlaced(event);
verify(emailService).sendOrderConfirmation(...);
```

## 27. Chain testen

```java
@Test
void validationChain_stopsAtFirstInvalidValidator() {
    var first = mock(OrderValidator.class);
    var second = mock(OrderValidator.class);

    when(first.validate(any()))
            .thenReturn(ValidationResult.invalid("user missing"));

    var chain = new OrderValidationChain(List.of(first, second));

    var result = chain.validate(validCommand());

    assertThat(result.isValid()).isFalse();
    verify(second, never()).validate(any());
}
```

## 28. State testen

```java
@Test
void pendingState_confirm_transitionsToConfirmed() {
    var state = new PendingState();

    var next = state.confirm();

    assertThat(next).isInstanceOf(ConfirmedState.class);
}

@Test
void pendingState_ship_throwsInvalidTransition() {
    var state = new PendingState();

    assertThatThrownBy(state::ship)
            .isInstanceOf(InvalidOrderTransitionException.class);
}
```

## 29. Template Method testen

```java
@Test
void reportGenerator_runsStepsInDefinedOrder() {
    var generator = new TestReportGenerator();

    var report = generator.generate(validRequest());

    assertThat(report).isNotNull();
    assertThat(generator.executedSteps())
            .containsExactly("validate", "fetch", "process", "format", "audit");
}
```

## 30. Command testen

```java
@Test
void cancelOrderCommand_cancelsAndPersistsOrder() {
    var repository = mock(OrderRepository.class);
    var order = OrderFixture.pendingOrder();

    when(repository.findById(order.id())).thenReturn(Optional.of(order));

    var command = new CancelOrderCommand(order.id(), repository);

    var result = command.execute();

    assertThat(result.succeeded()).isTrue();
    assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    verify(repository).save(order);
}
```

---

# Teil O — Typische Fehler

## 31. Fehler 1: Strategy ohne echtes Variantenproblem

Schlecht:

```java
interface NameFormatterStrategy { ... }
class DefaultNameFormatterStrategy implements NameFormatterStrategy { ... }
```

wenn es nur eine Implementierung gibt und keine absehbare Variante.

Besser:

```java
public String formatName(User user) { ... }
```

## 32. Fehler 2: Events für zwingende Transaktionslogik

Schlecht:

```java
eventPublisher.publishEvent(new InventoryMustBeReservedEvent(order));
```

wenn Bestellung ohne Reservierung nie existieren darf.

Besser: Reservierung im Use Case oder über Outbox/Saga mit bewusstem Konsistenzmodell.

## 33. Fehler 3: `@Async` ohne Fehlerstrategie

Asynchrone Listener können Fehler verlieren, wenn kein Error Handling, Monitoring oder Retry existiert.

## 34. Fehler 4: Command enthält zu viele Abhängigkeiten

Wenn jedes Command fünf Services injiziert bekommt, ist vielleicht ein Application Service sauberer.

## 35. Fehler 5: Chain-Reihenfolge ungetestet

Wenn Fraud Check vor Basic Validation läuft und dann an null-Werten scheitert, ist die Reihenfolge falsch.

## 36. Fehler 6: State Pattern für einfache Enums

Bei drei einfachen Statuswerten ohne eigenes Verhalten reicht oft ein Enum.

## 37. Fehler 7: Template Method für alles

Template Method kann zu starrer Vererbung führen. Komposition ist oft flexibler.

## 38. Fehler 8: Visitor trotz sealed Pattern Switch

In Java 21 ist Visitor nicht mehr automatisch die beste Lösung für geschlossene Typfamilien.

## 39. Fehler 9: Events mit vollständigen Entities

Entities in Events erzeugen Lazy-Loading-Probleme, PII-Risiken und starke Kopplung.

---

# Teil P — Review-Checkliste

## 40. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Strategy | Gibt es echte austauschbare Varianten? | Rabattregeln | Prüfen |
| Strategy-Auswahl | Keine freien Strings | Enum/`appliesTo` | Pflicht |
| Observer/Event | Entkoppelt echte Nebenwirkungen? | `OrderPlacedEvent` | Prüfen |
| Transactional Event | Nach Commit nötig? | E-Mail erst nach Commit | Pflicht bei DB-Abhängigkeit |
| Event-Inhalt | Keine vollständigen Entities/PII | IDs statt Entity | Pflicht |
| Command | Aktion muss als Objekt behandelt werden? | Retry/Queue/Undo | Prüfen |
| Undo | Compensation statt naivem Restore? | Inventory release | Pflicht bei Side Effects |
| Chain | Reihenfolge klar und getestet? | Validatoren | Pflicht |
| State | Zustand hat eigenes Verhalten? | Order Lifecycle | Prüfen |
| State-Persistenz | Mapping Enum ↔ State geklärt? | JPA Enum | Pflicht |
| Template | Vererbung wirklich sinnvoll? | ReportGenerator | Prüfen |
| Iterator | Java-Standard reicht nicht? | Baum-Traversal | Prüfen |
| Mediator | Koordiniert nur, statt alles zu wissen? | CheckoutWorkflow | Prüfen |
| Visitor | Besser als sealed switch? | Dokumentbaum | Prüfen |
| Security | Tenant/Auth nicht versteckt? | AccessScope | Pflicht |
| Tests | Pattern-Verhalten explizit getestet? | Strategy/Chain/State | Pflicht |
| Overengineering | Pattern macht Code einfacher? | keine Fake-Abstraktion | Pflicht |

---

# Teil Q — Automatisierbare Prüfungen

## 41. Mögliche Regeln

```text
- Keine switch-Kaskaden über fachliche Typen in Services, wenn Strategy existiert.
- Events dürfen keine JPA-Entities enthalten.
- Event-Klassen müssen eventId/occurredAt enthalten.
- @EventListener mit @Async braucht dokumentierte Fehlerstrategie.
- State-Transitions müssen getestet sein.
- Commands dürfen keine sensitiven Daten in toString/loggable Feldern tragen.
- Chain-Handler müssen eine definierte Ordnung haben.
- Template-Method-Basisklassen dürfen nicht zu viele protected Hooks haben.
```

### 41.1 ArchUnit-Beispiel: Events enthalten keine Entities

```java
@ArchTest
static final ArchRule events_should_not_depend_on_entities =
        noClasses()
                .that().haveSimpleNameEndingWith("Event")
                .should().dependOnClassesThat()
                .resideInAPackage("..entity..")
                .because("Events sollen stabile fachliche Daten tragen, keine JPA-Entities.");
```

### 41.2 ArchUnit-Beispiel: Listener nicht in Domain

```java
@ArchTest
static final ArchRule domain_should_not_listen_to_spring_events =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith(EventListener.class)
                .because("Spring Event Listener gehören in Application/Adapter-Schichten, nicht in die reine Domain.");
```

---

# Teil R — Migration bestehender Codebereiche

## 42. Schrittweise Migration

1. Lange `switch`-/`if`-Kaskaden identifizieren.
2. Prüfen, ob Varianten echte Strategies sind.
3. Gemeinsames Interface definieren.
4. Implementierungen als Spring Beans anlegen.
5. Auswahl über `List<Strategy>` oder Registry lösen.
6. Direkte Service-Nebeneffekte identifizieren.
7. Fachliche Events definieren.
8. Listener für unabhängige Reaktionen auslagern.
9. Transaktionsphase prüfen: `@EventListener` oder `@TransactionalEventListener`.
10. Validierungsblöcke in Chain/Validatoren aufteilen.
11. Statuslogik analysieren: Enum reicht oder State Pattern nötig?
12. Template-Method-Vererbung kritisch auf Komposition prüfen.
13. Events und Commands auf PII prüfen.
14. Tests für neue Pattern-Struktur schreiben.
15. Alte Kaskaden entfernen.

---

# Teil S — Ausnahmen

## 43. Zulässige Ausnahmen

| Ausnahme | Bedingung |
|---|---|
| `switch` bleibt bestehen | wenige stabile Fälle, klare sealed Exhaustiveness |
| kein Event | Nebenwirkung ist zwingend transaktional |
| keine Strategy | nur eine echte Variante |
| kein State Pattern | Enum reicht und Verhalten ist simpel |
| Template Method | Framework-/Algorithmusrahmen ist stabil |
| eigene Iterator-Implementierung | Traversal ist wirklich komplex |
| Visitor | Objektstruktur stabil, Operationen wachsen stark |
| Command mit `execute()` | Aktion muss queued/retried/auditiert werden |

Nicht zulässig:

- Events mit vollständigen Entities,
- freie String-Strategien aus Userinput,
- asynchrone Listener ohne Fehlerstrategie,
- State-Übergänge ohne Tests,
- Chain-Reihenfolge ohne Tests,
- Pattern-Schicht ohne konkretes Verhaltensproblem.

---

# Teil T — Definition of Done

## 44. Definition of Done

Ein Codebereich erfüllt diese Richtlinie, wenn:

1. austauschbare Algorithmen als Strategy modelliert sind,
2. Strategy-Auswahl typisiert oder fachlich über `appliesTo` erfolgt,
3. Events echte fachliche Zustandsänderungen repräsentieren,
4. Events keine vollständigen Entities oder unnötige PII enthalten,
5. transaktionsabhängige Listener `@TransactionalEventListener` nutzen,
6. asynchrone Listener Fehlerhandling und Monitoring besitzen,
7. Commands nur eingesetzt werden, wenn Aktion als Objekt Mehrwert hat,
8. Undo/Redo in Business-Systemen als Compensation geprüft wurde,
9. Chains eine klare Reihenfolge und Tests besitzen,
10. State Pattern nur bei echter zustandsabhängiger Logik eingesetzt wird,
11. State-Transitions vollständig getestet sind,
12. Template Method bewusst gegen Strategy/Komposition abgewogen wurde,
13. Iterator nur bei echter Traversal-Komplexität selbst implementiert wurde,
14. Mediator/Workflow-Klassen nicht zu God Services werden,
15. Visitor nicht eingesetzt wird, wenn sealed switch klarer ist,
16. Security- und Tenant-Kontext explizit bleiben,
17. Pattern-Einsatz die Lesbarkeit erhöht,
18. Review-Checkliste erfüllt ist.

---

## 45. Quellen und weiterführende Literatur

- Refactoring.Guru — Behavioral Design Patterns: https://refactoring.guru/design-patterns/behavioral-patterns
- Refactoring.Guru — Strategy: https://refactoring.guru/design-patterns/strategy
- Refactoring.Guru — Observer: https://refactoring.guru/design-patterns/observer
- Refactoring.Guru — Command: https://refactoring.guru/design-patterns/command
- Refactoring.Guru — Chain of Responsibility: https://refactoring.guru/design-patterns/chain-of-responsibility
- Refactoring.Guru — State: https://refactoring.guru/design-patterns/state
- Refactoring.Guru — Template Method: https://refactoring.guru/design-patterns/template-method
- Refactoring.Guru — Iterator: https://refactoring.guru/design-patterns/iterator
- Refactoring.Guru — Mediator: https://refactoring.guru/design-patterns/mediator
- Refactoring.Guru — Visitor: https://refactoring.guru/design-patterns/visitor
- Refactoring.Guru — Memento: https://refactoring.guru/design-patterns/memento
- Spring Framework Javadoc — `@EventListener`: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/event/EventListener.html
- Spring Framework Javadoc — `@TransactionalEventListener`: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/event/TransactionalEventListener.html
- Spring Security Reference — Servlet Architecture / FilterChainProxy: https://docs.spring.io/spring-security/reference/servlet/architecture.html
- Spring Framework Reference — Proxying Mechanisms: https://docs.spring.io/spring-framework/reference/core/aop/proxying.html
- Oracle Java SE 21 — Pattern Matching for switch: https://docs.oracle.com/en/java/javase/21/language/pattern-matching-switch.html
- Oracle Java SE 21 — Sealed Classes and Interfaces: https://docs.oracle.com/en/java/javase/21/language/sealed-classes-and-interfaces.html
- Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides — Design Patterns: Elements of Reusable Object-Oriented Software
