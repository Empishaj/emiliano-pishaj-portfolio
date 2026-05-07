# QG-JAVA-028 — Entwurfsmuster: Creational Patterns richtig anwenden

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-028 |
| Titel | Entwurfsmuster: Creational Patterns richtig anwenden |
| Status | Accepted / verbindlicher Standard für objektorientierte Erzeugungsmuster im Java-Code |
| Version | 1.0.0 |
| Datum | 2024-07-18 |
| Kategorie | Design Patterns / GoF / Objektorientierung / Code Quality |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, Architektur, QA |
| Java-Baseline | Java 21 |
| Framework-Kontext | Java 21, Spring Boot 3.x, Spring Framework Bean Scopes, Records, Dependency Injection |
| Geltungsbereich | Objekterzeugung, Factories, Builder, Spring Beans, Provider-Auswahl, Value Objects, Testdaten-Erzeugung, Domain-Objekte, Adapter und Integrationskomponenten |
| Verbindlichkeit | Erzeugungsmuster werden eingesetzt, wenn Objekterzeugung fachlich, technisch oder konfigurationsabhängig komplex ist. Sie dürfen nicht eingesetzt werden, um einfache Konstruktoraufrufe unnötig zu verschleiern. |
| Technische Validierung | Gegen GoF-Pattern-Katalog, Spring Bean Scopes, Java-Records-Dokumentation und moderne Java-/Spring-Praxis eingeordnet |
| Kurzentscheidung | Creational Patterns lösen nicht „wir brauchen mehr Klassen“, sondern „Objekterzeugung ist eine Entscheidung, die sauber gekapselt werden muss“. In Java 21 und Spring werden klassische Muster modern interpretiert: Spring Beans ersetzen viele Singleton-Implementierungen, Records vereinfachen Value Objects, Factory/Strategy-Registries vermeiden Switch-Kaskaden, und Builder werden nur bei echter Konstruktionskomplexität eingesetzt. |

---

## 1. Zweck

Diese Richtlinie erklärt die wichtigsten Erzeugungsmuster im Java-Alltag und zeigt, wann sie helfen, wann sie schaden und wie sie sauber umgesetzt werden.

Erzeugungsmuster beantworten eine einfache, aber wichtige Frage:

```text
Wie entsteht ein Objekt?
```

Diese Frage klingt klein. In echten Java-Systemen wird sie schnell groß:

- Welche Implementierung soll bei welchem Kanal verwendet werden?
- Wer entscheidet, ob E-Mail, SMS oder Push genutzt wird?
- Wie erzeugt man ein gültiges Domain-Objekt mit vielen Regeln?
- Wie verhindert man Konstruktoren mit acht Parametern?
- Wie erzeugt man zusammengehörige technische Komponenten eines Providers?
- Wie verhindert man globalen veränderlichen Zustand?
- Wie kopiert man Vorlagen, ohne versehentlich Listen und mutable Objekte zu teilen?
- Wie bleibt Code testbar, wenn Objekte nicht direkt mit `new` erzeugt werden sollten?
- Wie nutzt man Spring, ohne die GoF-Muster blind zu kopieren?

Creational Patterns sind nützlich, wenn Objekterzeugung eine fachliche oder technische Entscheidung enthält. Sie sind schädlich, wenn sie einfache Dinge komplizierter machen.

---

## 2. Kurzregel

Nutze direkte Konstruktoren für einfache, klare Objekte. Nutze statische Factory-Methoden, wenn Namen die Erzeugungsabsicht besser ausdrücken. Nutze Builder, wenn ein Objekt viele optionale oder validierungspflichtige Bestandteile hat. Nutze Factory Method oder Registry-basierte Factory, wenn die konkrete Implementierung abhängig von Typ, Konfiguration oder Kontext gewählt wird. Nutze Abstract Factory, wenn eine ganze Familie zusammengehöriger Objekte konsistent erzeugt werden muss. Nutze Spring Singleton Beans für zustandslose Services. Vermeide klassische manuelle Singletons mit globalem Zustand.

---

## 3. Warum dieses Thema im Java-Alltag wichtig ist

Viele Qualitätsprobleme entstehen nicht in der eigentlichen Business-Logik, sondern bereits bei der Erzeugung von Objekten.

Ein typischer Alltag:

Ein Entwickler baut einen neuen Benachrichtigungskanal. Zuerst gibt es nur E-Mail. Dann kommt SMS. Dann Push. Dann WhatsApp. Der Code startet oft so:

```java
if ("EMAIL".equals(type)) {
    return new EmailSender();
}
if ("SMS".equals(type)) {
    return new SmsSender();
}
```

Das funktioniert am Anfang. Später gibt es diese Entscheidung an fünf Stellen: im Service, im Controller, im Test, im Batch-Job und im Event-Consumer. Jetzt wird jede Erweiterung teuer.

Oder ein Entwickler erzeugt ein `Order`-Objekt mit vielen Parametern:

```java
new Order(userId, productId, 2, shippingAddress, billingAddress, null, true, "SAVE10");
```

Der Code kompiliert. Aber niemand sieht beim Lesen sofort, was `true` bedeutet. Noch schlimmer: Zwei Parameter gleicher Typen können vertauscht werden.

Erzeugungsmuster helfen, solche Stellen sauber zu machen. Aber sie müssen richtig dosiert werden.

---

## 4. Grundlagen kurz erklärt

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Pattern | Wiederverwendbare Lösung für ein wiederkehrendes Designproblem. | Builder für komplexe Objektkonstruktion |
| Creational Pattern | Muster, das Objekterzeugung kapselt. | Factory Method, Builder, Singleton |
| Konstruktor | Java-Sprachmittel zur Erzeugung eines Objekts. | `new Order(...)` |
| Factory | Objekt oder Methode, die andere Objekte erzeugt. | `NotificationSenderFactory` |
| Builder | Schrittweise Erzeugung eines komplexen Objekts. | `OrderBuilder` |
| Singleton | Genau eine Instanz im definierten Kontext. | Spring Bean im Singleton Scope |
| Prototype | Neues Objekt entsteht aus einer Vorlage. | `template.withChannel("APP")` |
| Abstract Factory | Factory für eine Familie zusammengehöriger Objekte. | Stripe Gateway + Validator + Mapper |
| Dependency Injection | Abhängigkeiten werden von außen übergeben, statt innen mit `new` erzeugt. | Konstruktorinjektion in Spring |
| Coupling | Kopplung zwischen Klassen. Hohe Kopplung macht Änderung teuer. | Service kennt alle konkreten Sender |
| OCP | Open/Closed Principle: Erweiterung ohne Änderung bestehender Logik. | neuer Sender als neue Bean |

---

## 5. Die zentrale Linie

Die wichtigste Designlinie ist:

```text
Je einfacher die Objekterzeugung, desto direkter darf sie sein.
Je mehr Entscheidung, Validierung oder Varianten beteiligt sind,
desto stärker muss die Erzeugung gekapselt werden.
```

Nicht jedes `new` ist schlecht.

Gutes direktes `new`:

```java
var quantity = new Quantity(2);
var email = new EmailAddress("max@example.com");
```

Schlechtes direktes `new`:

```java
if (provider.equals("stripe")) {
    gateway = new StripePaymentGateway(apiKey, timeout, mapper, logger);
} else if (provider.equals("paypal")) {
    gateway = new PayPalPaymentGateway(clientId, secret, mapper, logger);
}
```

Der Unterschied: Im zweiten Fall steckt eine Auswahlentscheidung, Providerlogik und Infrastrukturwissen im falschen Ort.

---

## 6. Anwendung im Daily Business eines Java-Entwicklers

Wenn du im Alltag ein Objekt erzeugst, gehst du künftig so vor:

### Schritt 1: Ist der Konstruktor selbsterklärend?

Wenn der Aufruf klar ist, reicht der Konstruktor.

```java
new Quantity(3);
new ProductId("P-123");
```

Wenn der Aufruf unklar ist, brauchst du ein besseres Erzeugungsmodell.

```java
new Order(userId, address, true, null, "SAVE10");
```

### Schritt 2: Gibt es viele optionale Werte?

Wenn ja, prüfe Builder oder Command-Objekt.

```java
OrderDraft.forCustomer(customerId)
        .shippingTo(address)
        .withCoupon(couponCode)
        .addItem(productId, quantity)
        .build();
```

### Schritt 3: Gibt es verschiedene Implementierungen?

Wenn ja, prüfe Factory oder Strategy Registry.

```java
notificationSenderRegistry.senderFor(NotificationChannel.EMAIL);
```

### Schritt 4: Gehören mehrere Objekte als Familie zusammen?

Wenn ja, prüfe Abstract Factory.

```java
PaymentProviderComponents components = paymentProviderFactory.create(provider);
```

### Schritt 5: Ist es ein zustandsloser Service?

Wenn ja, nutze eine Spring Singleton Bean.

```java
@Service
public class TaxCalculationService { ... }
```

### Schritt 6: Hat das Objekt veränderlichen Zustand?

Dann niemals unkritisch als Singleton teilen.

```java
@Component
@Scope("prototype")
class ExportJobState { ... }
```

Oder besser: Zustand als normales Objekt pro Use Case erzeugen.

### Schritt 7: Ist es nur ein hypothetischer Erweiterungspunkt?

Wenn ja: Kein Pattern einführen. YAGNI anwenden.

---

## 7. Entscheidungsmatrix

| Problem | Geeignetes Muster | Details/Erklärung |
|---|---|---|
| Einfaches Value Object | Konstruktor oder Record | kein Pattern nötig |
| Konstruktor braucht sprechenden Namen | Statische Factory Method | `Money.eur("10.00")` |
| Viele optionale Parameter | Builder | lesbare schrittweise Erzeugung |
| Komplexe Validierung am Ende | Builder mit `build()`-Validierung | erst alle Teile sammeln, dann prüfen |
| Konkrete Implementierung hängt von Typ ab | Factory Method / Registry Factory | `senderFor(channel)` |
| Familien zusammengehöriger Komponenten | Abstract Factory | Provider-spezifische Gruppe |
| Genau eine zustandslose Instanz | Spring Singleton Bean | Default in Spring |
| Neue Instanz pro Nutzung | Spring Prototype oder normales `new` | stateful Objekt |
| Objekt aus Vorlage kopieren | Prototype / Wither | Records mit `withX()` |
| Nur hypothetische Erweiterung | kein Pattern | YAGNI |

---

# Teil A — Direkte Konstruktion und statische Factory-Methoden

## 8. Direkter Konstruktor

### 8.1 Wann verwenden?

Direkte Konstruktoren sind gut, wenn:

- wenige Parameter vorhanden sind,
- Reihenfolge klar ist,
- keine Auswahlentscheidung nötig ist,
- keine komplexe Validierung über mehrere Schritte entsteht,
- das Objekt fachlich einfach ist.

Gut:

```java
var id = new OrderId(UUID.randomUUID());
var quantity = new Quantity(2);
var email = new EmailAddress("max@example.com");
```

### 8.2 Wann vermeiden?

Vermeide direkte Konstruktoren, wenn:

- viele Parameter vorhanden sind,
- mehrere Parameter denselben Typ haben,
- optionale Werte vorkommen,
- Boolean-Parameter die Lesbarkeit zerstören,
- konkrete Implementierungen im Aufrufer gewählt werden,
- Erzeugung technische Details enthält.

Schlecht:

```java
new PaymentClient("https://api.stripe.com", apiKey, 3, true, false, Duration.ofSeconds(2));
```

Besser:

```java
PaymentClient client = PaymentClientFactory.stripe(apiKey, timeout);
```

---

## 9. Statische Factory Method

### 9.1 Bedeutung

Eine statische Factory Method ist eine benannte statische Methode, die ein Objekt erzeugt. Sie ist kein GoF-Pattern im engeren Sinne, aber in modernem Java sehr nützlich.

Beispiel:

```java
public record Money(BigDecimal amount, Currency currency) {

    public static Money eur(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("EUR"));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}
```

Aufruf:

```java
var price = Money.eur("19.99");
```

Das ist lesbarer als:

```java
new Money(new BigDecimal("19.99"), Currency.getInstance("EUR"));
```

### 9.2 Gute Anwendung

```java
public final class OrderId {

    private final UUID value;

    private OrderId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId from(UUID value) {
        return new OrderId(value);
    }
}
```

### 9.3 Wann sinnvoll?

| Situation | Beispiel |
|---|---|
| Konstruktor braucht Namen | `Money.eur("10.00")` |
| unterschiedliche Erzeugungswege | `OrderId.newId()` vs. `OrderId.from(uuid)` |
| Validierung zentralisieren | `EmailAddress.of(raw)` |
| Caching möglich | `Currency.getInstance("EUR")` |
| Subtyp verbergen | Rückgabe Interface statt Klasse |
| Domänenbegriff ausdrücken | `CampaignDraft.createForMerchant(...)` |

---

# Teil B — Factory Method und Registry Factory

## 10. Factory Method

### 10.1 Bedeutung

Factory Method kapselt die Entscheidung, welche konkrete Klasse erzeugt oder verwendet wird. Klassisch beschreibt das Muster eine Methode, die in einer Oberklasse definiert und von Unterklassen konkretisiert wird. In modernen Spring-Systemen wird es oft als Factory/Registry mit injizierten Implementierungen umgesetzt.

Das Ziel ist:

```text
Der Aufrufer soll nicht wissen müssen, welche konkrete Klasse für EMAIL, SMS oder PUSH zuständig ist.
```

### 10.2 Schlechte Anwendung: Switch mit direkter Instanzierung

```java
@Service
public class NotificationService {

    public void send(NotificationChannel channel, String message) {
        switch (channel) {
            case EMAIL -> new EmailNotificationSender().send(message);
            case SMS -> new SmsNotificationSender().send(message);
            case PUSH -> new PushNotificationSender().send(message);
        }
    }
}
```

Probleme:

- Jede neue Variante ändert diese Klasse.
- Konkrete Klassen werden direkt erzeugt.
- Abhängigkeiten wie Clients, Config oder Metrics fehlen.
- Testbarkeit ist schlecht.
- Open/Closed Principle wird verletzt.

### 10.3 Gute Anwendung: Interface + Implementierungen

```java
public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH
}
```

```java
public interface NotificationSender {

    NotificationChannel channel();

    void send(NotificationMessage message);
}
```

```java
@Component
public class EmailNotificationSender implements NotificationSender {

    private final EmailClient emailClient;

    public EmailNotificationSender(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        emailClient.send(message.recipient(), message.subject(), message.body());
    }
}
```

```java
@Component
public class SmsNotificationSender implements NotificationSender {

    private final SmsClient smsClient;

    public SmsNotificationSender(SmsClient smsClient) {
        this.smsClient = smsClient;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationMessage message) {
        smsClient.send(message.recipient(), message.body());
    }
}
```

### 10.4 Gute Anwendung: Registry Factory

```java
@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationSenderFactory(List<NotificationSender> senders) {
        this.senders = senders.stream()
                .collect(Collectors.toUnmodifiableMap(
                        NotificationSender::channel,
                        Function.identity()
                ));
    }

    public NotificationSender senderFor(NotificationChannel channel) {
        var sender = senders.get(channel);

        if (sender == null) {
            throw new UnsupportedNotificationChannelException(channel);
        }

        return sender;
    }
}
```

Nutzung:

```java
@Service
public class NotificationService {

    private final NotificationSenderFactory senderFactory;

    public NotificationService(NotificationSenderFactory senderFactory) {
        this.senderFactory = senderFactory;
    }

    public void send(NotificationChannel channel, NotificationMessage message) {
        senderFactory.senderFor(channel).send(message);
    }
}
```

### 10.5 Warum das besser ist

Ein neuer Kanal braucht:

1. neue Implementierung,
2. Bean-Registrierung durch Spring,
3. Tests für diese Implementierung.

Bestehende Entscheidungslogik muss nicht geändert werden.

### 10.6 Daily-Business-Anwendung

Wenn du im Code einen `switch` über Typen findest und jeder Branch ein anderes Objekt erzeugt oder verwendet, frage:

```text
Ist das eine Factory/Strategy-Entscheidung?
Kann ich die Varianten als Implementierungen modellieren?
Kann Spring mir die Liste aller Implementierungen injizieren?
```

Wenn ja: Registry Factory verwenden.

---

# Teil C — Builder

## 11. Builder

### 11.1 Bedeutung

Builder erzeugen komplexe Objekte schrittweise. Sie machen Aufrufe lesbar und erlauben Validierung im `build()`-Schritt.

Builder sind sinnvoll, wenn:

- viele optionale Parameter existieren,
- mehrere Schritte nötig sind,
- Parameter abhängig voneinander validiert werden,
- ein Objekt im Test lesbar erzeugt werden soll,
- der Konstruktor sonst unverständlich wird.

### 11.2 Schlechte Anwendung: Teleskop-Konstruktor

```java
var order = new Order(
        userId,
        shippingAddress,
        billingAddress,
        items,
        couponCode,
        true,
        false,
        LocalDate.now(),
        "WEB"
);
```

Probleme:

- Boolean-Parameter sind unklar.
- Reihenfolgefehler sind wahrscheinlich.
- Optionale Werte erzeugen `null`.
- Fachliche Validierung ist schwer erkennbar.
- Der Aufruf liest sich nicht wie ein Fachprozess.

### 11.3 Gute Anwendung: expliziter Builder

```java
public final class OrderDraft {

    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final CouponCode couponCode;
    private final boolean gift;
    private final SalesChannel channel;

    private OrderDraft(Builder builder) {
        this.customerId = builder.customerId;
        this.items = List.copyOf(builder.items);
        this.shippingAddress = builder.shippingAddress;
        this.billingAddress = builder.billingAddress != null
                ? builder.billingAddress
                : builder.shippingAddress;
        this.couponCode = builder.couponCode;
        this.gift = builder.gift;
        this.channel = builder.channel;
    }

    public static Builder forCustomer(CustomerId customerId) {
        return new Builder(customerId);
    }

    public static final class Builder {

        private final CustomerId customerId;
        private final List<OrderItem> items = new ArrayList<>();

        private Address shippingAddress;
        private Address billingAddress;
        private CouponCode couponCode;
        private boolean gift;
        private SalesChannel channel = SalesChannel.WEB;

        private Builder(CustomerId customerId) {
            this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        }

        public Builder addItem(ProductId productId, Quantity quantity) {
            items.add(new OrderItem(productId, quantity));
            return this;
        }

        public Builder shippingTo(Address address) {
            this.shippingAddress = Objects.requireNonNull(address);
            return this;
        }

        public Builder billingTo(Address address) {
            this.billingAddress = Objects.requireNonNull(address);
            return this;
        }

        public Builder withCoupon(CouponCode couponCode) {
            this.couponCode = couponCode;
            return this;
        }

        public Builder asGift() {
            this.gift = true;
            return this;
        }

        public Builder fromChannel(SalesChannel channel) {
            this.channel = Objects.requireNonNull(channel);
            return this;
        }

        public OrderDraft build() {
            if (items.isEmpty()) {
                throw new EmptyOrderException();
            }
            if (shippingAddress == null) {
                throw new MissingShippingAddressException();
            }

            return new OrderDraft(this);
        }
    }
}
```

Aufruf:

```java
var draft = OrderDraft.forCustomer(customerId)
        .shippingTo(homeAddress)
        .addItem(bookId, new Quantity(2))
        .addItem(penId, new Quantity(1))
        .withCoupon(new CouponCode("SAVE10"))
        .asGift()
        .fromChannel(SalesChannel.WEB)
        .build();
```

Der Aufruf ist lesbar und fachlich.

### 11.4 Builder für Tests

Testdaten-Builder sind sehr nützlich, wenn Domain-Objekte viele Pflichtwerte haben.

```java
public final class OrderDraftTestBuilder {

    private CustomerId customerId = new CustomerId(1L);
    private Address shippingAddress = AddressFixture.validAddress();
    private final List<OrderItem> items = new ArrayList<>();

    private OrderDraftTestBuilder() {
        items.add(new OrderItem(new ProductId("P-1"), new Quantity(1)));
    }

    public static OrderDraftTestBuilder anOrderDraft() {
        return new OrderDraftTestBuilder();
    }

    public OrderDraftTestBuilder withoutItems() {
        items.clear();
        return this;
    }

    public OrderDraftTestBuilder withItem(String productId, int quantity) {
        items.add(new OrderItem(new ProductId(productId), new Quantity(quantity)));
        return this;
    }

    public OrderDraft build() {
        var builder = OrderDraft.forCustomer(customerId)
                .shippingTo(shippingAddress);

        items.forEach(item -> builder.addItem(item.productId(), item.quantity()));

        return builder.build();
    }
}
```

Test:

```java
var draft = anOrderDraft()
        .withItem("BOOK", 2)
        .build();
```

### 11.5 Lombok `@Builder`

Lombok `@Builder` kann Boilerplate reduzieren, aber es ersetzt keine fachliche Modellierung.

Risiken:

- Pflichtfelder sind nicht automatisch wirklich fachlich geprüft.
- Build-Reihenfolge ist beliebig.
- Fachliche Zwischenschritte fehlen.
- `null` kann leichter entstehen.
- Der Builder ist oft technisch, nicht fachlich.

Zulässig für einfache DTOs:

```java
@Builder
public record ProductSearchRequest(
        String query,
        Integer page,
        Integer size
) {}
```

Für Domain-Objekte mit Invarianten ist ein expliziter Builder meist besser.

### 11.6 Builder nicht übertreiben

Schlecht:

```java
Quantity.builder()
        .value(2)
        .build();
```

Besser:

```java
new Quantity(2);
```

Builder für einfache Value Objects ist Overengineering.

---

# Teil D — Singleton

## 12. Singleton

### 12.1 Bedeutung

Singleton bedeutet: In einem definierten Kontext existiert genau eine Instanz.

Wichtig: „genau eine Instanz“ bedeutet in Spring normalerweise:

```text
Eine Instanz pro Spring ApplicationContext.
```

Nicht zwingend:

```text
Eine Instanz im gesamten Cluster.
```

In Kubernetes mit fünf Pods gibt es fünf Spring ApplicationContexts und damit fünf Singleton-Bean-Instanzen.

### 12.2 Schlechte Anwendung: klassisches manuelles Singleton

```java
public class ConfigurationManager {

    private static ConfigurationManager instance;

    private final Map<String, String> values = new HashMap<>();

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    public void put(String key, String value) {
        values.put(key, value);
    }
}
```

Probleme:

- nicht thread-safe,
- globaler veränderlicher Zustand,
- schwer testbar,
- keine Dependency Injection,
- Lifecycle unklar,
- schwer austauschbar.

### 12.3 Gute Anwendung: Spring Singleton Bean

```java
@Service
public class ExchangeRateService {

    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateService(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public ExchangeRate currentRate(Currency source, Currency target) {
        return exchangeRateClient.fetchRate(source, target);
    }
}
```

Spring erzeugt standardmäßig eine Singleton Bean. Der Service ist testbar, weil seine Abhängigkeiten über den Konstruktor injiziert werden.

### 12.4 Wann Singleton gut ist

Gut:

- zustandslose Services,
- Repositories,
- Mapper ohne mutable Felder,
- Clients mit thread-sicherer Implementierung,
- Validatoren,
- Policies,
- ConfigurationProperties,
- Factories ohne mutable Laufzeitdaten.

Vorsicht:

- mutable Felder,
- User-/Request-spezifischer Zustand,
- nicht thread-sichere Clients,
- Caches ohne klare Eviction,
- Zähler ohne Atomic/Concurrency-Schutz.

### 12.5 Pure Java Singleton

Wenn außerhalb von Spring wirklich ein Singleton gebraucht wird, ist ein `enum` oft robuster als Lazy Initialization mit statischem Feld.

```java
public enum SystemClockProvider {
    INSTANCE;

    public Instant now() {
        return Instant.now();
    }
}
```

Trotzdem gilt: In Spring-Anwendungen ist Konstruktorinjektion fast immer besser.

### 12.6 Daily-Business-Regel

Wenn du in einer Spring-Anwendung `getInstance()` schreibst, halte an und frage:

```text
Warum ist das keine Spring Bean?
Warum soll diese Klasse global erreichbar sein?
Wie teste ich sie?
Hat sie Zustand?
Ist sie thread-safe?
```

In den meisten Fällen ist `getInstance()` in Spring-Code ein Warnsignal.

---

# Teil E — Prototype

## 13. Prototype

### 13.1 Bedeutung

Prototype erzeugt neue Objekte durch Kopieren bestehender Vorlagen. In modernem Java bedeutet das meist nicht `clone()`, sondern:

- Copy Constructor,
- `withX()`-Methoden,
- Records mit Kopiermethoden,
- Templates,
- Builder aus Vorlage.

### 13.2 Schlechte Anwendung: `clone()` blind verwenden

```java
public class OrderTemplate implements Cloneable {

    private List<OrderItem> items;

    @Override
    public OrderTemplate clone() throws CloneNotSupportedException {
        return (OrderTemplate) super.clone();
    }
}
```

Problem: `super.clone()` kopiert nur flach. Die Liste wird geteilt. Änderung an der Kopie kann die Vorlage verändern.

### 13.3 Gute Anwendung mit Record und defensiver Kopie

Oracle beschreibt Records als flach unveränderliche Träger einer festen Menge von Werten. „Flach“ ist wichtig: Wenn ein Record eine mutable Liste enthält, ist die Liste selbst nicht automatisch tief unveränderlich. Deshalb müssen Collections defensiv kopiert werden.

```java
public record OrderTemplate(
        Address defaultShippingAddress,
        List<OrderItem> standardItems,
        SalesChannel channel
) {
    public OrderTemplate {
        standardItems = List.copyOf(standardItems);
    }

    public OrderTemplate withChannel(SalesChannel newChannel) {
        return new OrderTemplate(defaultShippingAddress, standardItems, newChannel);
    }

    public OrderTemplate withShippingAddress(Address newAddress) {
        return new OrderTemplate(newAddress, standardItems, channel);
    }

    public OrderDraft instantiateFor(CustomerId customerId) {
        var builder = OrderDraft.forCustomer(customerId)
                .shippingTo(defaultShippingAddress)
                .fromChannel(channel);

        standardItems.forEach(item ->
                builder.addItem(item.productId(), item.quantity()));

        return builder.build();
    }
}
```

### 13.4 Spring Prototype Scope ist etwas anderes

Spring `prototype` bedeutet: Der Container erzeugt bei jeder Anfrage an diese Bean eine neue Instanz.

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExportJobState {

    private final List<String> processedRows = new ArrayList<>();

    public void markProcessed(String rowId) {
        processedRows.add(rowId);
    }
}
```

Wichtig: Spring-Dokumentation empfiehlt als Faustregel prototype für stateful Beans und singleton für stateless Beans.

### 13.5 Vorsicht: Prototype Bean in Singleton Bean

Wenn eine Prototype Bean einmal in eine Singleton Bean injiziert wird, bekommt die Singleton Bean nicht automatisch bei jedem Methodenaufruf eine neue Instanz.

Schlecht:

```java
@Service
public class ExportService {

    private final ExportJobState state;

    public ExportService(ExportJobState state) {
        this.state = state;
    }

    public void export() {
        // state ist nicht automatisch pro Export neu.
    }
}
```

Besser mit Provider:

```java
@Service
public class ExportService {

    private final ObjectProvider<ExportJobState> stateProvider;

    public ExportService(ObjectProvider<ExportJobState> stateProvider) {
        this.stateProvider = stateProvider;
    }

    public void export() {
        var state = stateProvider.getObject();
        ...
    }
}
```

Noch einfacher: Zustand als normales Objekt im Use Case erzeugen, wenn keine Spring-Abhängigkeiten nötig sind.

```java
public void export() {
    var state = new ExportJobState();
    ...
}
```

---

# Teil F — Abstract Factory

## 14. Abstract Factory

### 14.1 Bedeutung

Abstract Factory erzeugt Familien zusammengehöriger Objekte, ohne dass der Aufrufer konkrete Klassen kennen muss.

Das Muster ist sinnvoll, wenn nicht nur ein Objekt gewählt wird, sondern eine konsistente Gruppe:

```text
Stripe Gateway + Stripe Validator + Stripe Mapper + Stripe Error Translator
PayPal Gateway + PayPal Validator + PayPal Mapper + PayPal Error Translator
```

### 14.2 Schlechte Anwendung: Provider-Logik verstreut

```java
@Service
public class PaymentService {

    public PaymentResult pay(PaymentProvider provider, PaymentCommand command) {
        PaymentGateway gateway;
        PaymentValidator validator;

        if (provider == PaymentProvider.STRIPE) {
            gateway = new StripeGateway();
            validator = new StripeValidator();
        } else if (provider == PaymentProvider.PAYPAL) {
            gateway = new PayPalGateway();
            validator = new PayPalValidator();
        } else {
            throw new UnsupportedPaymentProviderException(provider);
        }

        validator.validate(command);
        return gateway.charge(command);
    }
}
```

Probleme:

- Erzeugung und Fachlogik vermischt.
- Provider-Familie kann inkonsistent werden.
- Neue Provider ändern den Service.
- Tests brauchen viele Branches.
- technische Abhängigkeiten werden direkt erzeugt.

### 14.3 Gute Anwendung: Provider Components

```java
public interface PaymentProviderFactory {

    PaymentProvider provider();

    PaymentGateway gateway();

    PaymentValidator validator();

    PaymentErrorTranslator errorTranslator();
}
```

```java
@Component
public class StripePaymentProviderFactory implements PaymentProviderFactory {

    private final StripeClient stripeClient;
    private final StripeProperties properties;

    public StripePaymentProviderFactory(
            StripeClient stripeClient,
            StripeProperties properties) {
        this.stripeClient = stripeClient;
        this.properties = properties;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentGateway gateway() {
        return new StripePaymentGateway(stripeClient, properties);
    }

    @Override
    public PaymentValidator validator() {
        return new StripePaymentValidator();
    }

    @Override
    public PaymentErrorTranslator errorTranslator() {
        return new StripePaymentErrorTranslator();
    }
}
```

```java
@Component
public class PayPalPaymentProviderFactory implements PaymentProviderFactory {

    private final PayPalClient payPalClient;

    public PayPalPaymentProviderFactory(PayPalClient payPalClient) {
        this.payPalClient = payPalClient;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.PAYPAL;
    }

    @Override
    public PaymentGateway gateway() {
        return new PayPalPaymentGateway(payPalClient);
    }

    @Override
    public PaymentValidator validator() {
        return new PayPalPaymentValidator();
    }

    @Override
    public PaymentErrorTranslator errorTranslator() {
        return new PayPalPaymentErrorTranslator();
    }
}
```

### 14.4 Gute Anwendung: Factory Registry

```java
@Component
public class PaymentProviderFactoryRegistry {

    private final Map<PaymentProvider, PaymentProviderFactory> factories;

    public PaymentProviderFactoryRegistry(List<PaymentProviderFactory> factories) {
        this.factories = factories.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PaymentProviderFactory::provider,
                        Function.identity()
                ));
    }

    public PaymentProviderFactory factoryFor(PaymentProvider provider) {
        var factory = factories.get(provider);

        if (factory == null) {
            throw new UnsupportedPaymentProviderException(provider);
        }

        return factory;
    }
}
```

```java
@Service
public class PaymentService {

    private final PaymentProviderFactoryRegistry registry;

    public PaymentService(PaymentProviderFactoryRegistry registry) {
        this.registry = registry;
    }

    public PaymentResult pay(PaymentProvider provider, PaymentCommand command) {
        var factory = registry.factoryFor(provider);

        factory.validator().validate(command);

        try {
            return factory.gateway().charge(command);
        } catch (PaymentProviderException ex) {
            throw factory.errorTranslator().translate(ex);
        }
    }
}
```

### 14.5 Korrektur zu `@Qualifier("${payment.provider}")`

In Spring ist `@Qualifier("${payment.provider}")` kein sauberer dynamischer Auswahlmechanismus für runtime-basierte Provider-Auswahl. `@Qualifier` ist für Bean-Auswahl bei Injection gedacht, nicht für fachliche Runtime-Routing-Logik.

Besser:

- Registry aus allen Factories,
- Auswahl über Konfiguration in einem Properties-Objekt,
- Auswahl im Application Service,
- klare Fehlermeldung bei unbekanntem Provider.

```java
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(PaymentProvider provider) {}
```

```java
@Service
public class DefaultPaymentProviderSelector {

    private final PaymentProperties properties;
    private final PaymentProviderFactoryRegistry registry;

    public PaymentProviderFactory defaultFactory() {
        return registry.factoryFor(properties.provider());
    }
}
```

---

# Teil G — Pattern-Auswahl Schritt für Schritt

## 15. Schritt-für-Schritt-Anwendung

### Schritt 1: Konstruktor prüfen

Frage:

```text
Ist der Konstruktoraufruf beim Lesen verständlich?
```

Wenn ja: Kein Pattern.

```java
new Quantity(2);
```

### Schritt 2: Namen für Erzeugung prüfen

Frage:

```text
Braucht die Erzeugung einen fachlichen Namen?
```

Wenn ja: Statische Factory Method.

```java
Money.eur("19.99");
OrderId.newId();
CampaignDraft.createForMerchant(merchantId);
```

### Schritt 3: Parameteranzahl prüfen

Frage:

```text
Mehr als 3–4 Parameter oder mehrere optionale Parameter?
```

Wenn ja: Builder oder Command-Objekt.

### Schritt 4: Varianten prüfen

Frage:

```text
Gibt es mehrere Implementierungen?
```

Wenn ja: Factory/Registry oder Strategy.

### Schritt 5: Objektfamilie prüfen

Frage:

```text
Müssen mehrere zusammengehörige Objekte konsistent erzeugt werden?
```

Wenn ja: Abstract Factory.

### Schritt 6: Lifecycle prüfen

Frage:

```text
Wer verwaltet die Lebensdauer?
```

- zustandsloser Service → Spring Singleton Bean,
- stateful Objekt pro Use Case → normales `new`,
- stateful Spring Bean pro Anfrage → Prototype oder Request Scope,
- Vorlage kopieren → Prototype/Wither.

### Schritt 7: Overengineering prüfen

Frage:

```text
Gibt es heute mindestens zwei echte Varianten oder echte Konstruktionskomplexität?
```

Wenn nein: Kein Pattern.

---

## 16. Wann welches Muster?

| Problem | Muster | Java-/Spring-Empfehlung |
|---|---|---|
| Einfaches fachliches Objekt | Konstruktor / Record | direkt halten |
| Benannte Erzeugung | Static Factory Method | `of`, `from`, `newId`, `eur` |
| Viele optionale Parameter | Builder | explizit bei Domain-Objekten |
| Testdaten komplex erzeugen | Test Data Builder | nur im Test-Source-Tree |
| Implementierung nach Typ wählen | Factory Method / Registry Factory | Spring injiziert `List<Interface>` |
| Zusammengehörige Provider-Komponenten | Abstract Factory | Factory Registry |
| Genau eine zustandslose Instanz | Singleton | Spring Bean Default Scope |
| Zustand pro Nutzung | Prototype / normales Objekt | nicht als Singleton teilen |
| Vorlage kopieren | Prototype / Wither | defensive Copies |
| Hypothetischer Bedarf | kein Pattern | YAGNI |

---

## 17. Typische Fehler und Korrekturen

| Fehler | Warum problematisch | Besser |
|---|---|---|
| Factory für eine einzige Klasse | unnötige Abstraktion | Konstruktor oder static factory |
| Builder für triviales Value Object | Boilerplate ohne Nutzen | Konstruktor/Record |
| Klassisches Singleton in Spring | globaler Zustand, schwer testbar | Spring Bean |
| Mutable Singleton | Race Conditions | zustandslos oder synchronisiert |
| `clone()` ohne Deep Copy | geteilte mutable Daten | Copy Constructor / Wither |
| `@Qualifier` für Runtime-Auswahl | falscher Mechanismus | Factory Registry |
| Switch bei jeder Variante | OCP-Verletzung | Strategy/Factory |
| Lombok Builder ohne Validierung | ungültige Objekte möglich | expliziter `build()` |
| Factory erzeugt mit `new` komplexe Clients | DI umgangen | Clients als Beans injizieren |
| Abstract Factory für zwei kleine DTOs | Overengineering | einfache Factory oder Konstruktor |

---

## 18. Security- und SaaS-Aspekte

### 18.1 Keine Secrets in Factories hardcoden

Schlecht:

```java
return new StripeGateway("sk_live_123");
```

Besser:

```java
@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(
        @NotBlank String apiKey,
        Duration timeout
) {}
```

### 18.2 Tenant-Kontext nicht in Singleton-State speichern

Schlecht:

```java
@Service
public class TenantAwareFactory {

    private TenantId currentTenant;

    public PaymentGateway gatewayFor(TenantId tenantId) {
        this.currentTenant = tenantId;
        ...
    }
}
```

Singleton Bean mit mutablem Tenant-State ist gefährlich.

Besser:

```java
public PaymentGateway gatewayFor(TenantId tenantId) {
    return gateways.get(tenantId);
}
```

Oder Tenant-Kontext explizit als Parameter weitergeben.

### 18.3 Provider-Auswahl validieren

Ein Payment Provider, Notification Channel oder Export Format darf nicht als freier String ungeprüft aus dem Client übernommen werden.

Schlecht:

```java
factory.create(request.getParameter("provider"));
```

Besser:

```java
PaymentProvider provider = PaymentProvider.from(request.provider());
registry.factoryFor(provider);
```

### 18.4 Keine NoOp-Fallbacks bei sicherheitskritischen Operationen

Schlecht:

```java
return providers.getOrDefault(channel, new NoOpNotification());
```

Für Benachrichtigungen kann NoOp in Tests sinnvoll sein. In Produktion kann stilles Nichtstun gefährlich sein.

Besser:

```java
throw new UnsupportedNotificationChannelException(channel);
```

Oder ein expliziter, konfigurierter Fallback mit Monitoring.

---

## 19. Testing

### 19.1 Factory Registry testen

```java
class NotificationSenderFactoryTest {

    @Test
    void senderFor_returnsEmailSender_forEmailChannel() {
        var emailSender = mock(NotificationSender.class);
        when(emailSender.channel()).thenReturn(NotificationChannel.EMAIL);

        var factory = new NotificationSenderFactory(List.of(emailSender));

        var result = factory.senderFor(NotificationChannel.EMAIL);

        assertThat(result).isSameAs(emailSender);
    }

    @Test
    void senderFor_throwsException_forUnsupportedChannel() {
        var factory = new NotificationSenderFactory(List.of());

        assertThatThrownBy(() -> factory.senderFor(NotificationChannel.EMAIL))
                .isInstanceOf(UnsupportedNotificationChannelException.class);
    }
}
```

### 19.2 Builder testen

```java
class OrderDraftBuilderTest {

    @Test
    void build_createsOrderDraft_whenRequiredFieldsArePresent() {
        var draft = OrderDraft.forCustomer(new CustomerId(1L))
                .shippingTo(AddressFixture.valid())
                .addItem(new ProductId("P-1"), new Quantity(2))
                .build();

        assertThat(draft.items()).hasSize(1);
    }

    @Test
    void build_throwsException_whenShippingAddressIsMissing() {
        var builder = OrderDraft.forCustomer(new CustomerId(1L))
                .addItem(new ProductId("P-1"), new Quantity(2));

        assertThatThrownBy(builder::build)
                .isInstanceOf(MissingShippingAddressException.class);
    }
}
```

### 19.3 Singleton-State testen

Für Spring Singleton Beans gilt:

```text
Keine request-/user-/tenant-spezifischen mutable Felder.
```

ArchUnit kann mutable Felder in Services markieren.

```java
@ArchTest
static final ArchRule services_should_not_have_non_final_fields =
        fields()
                .that().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
                .should().beFinal()
                .because("Spring Singleton Services sollen keinen veränderlichen Request-Zustand halten.");
```

Diese Regel braucht Ausnahmen für Logger, Meter, Atomic-Werte oder Framework-Felder, muss also teambezogen kalibriert werden.

---

## 20. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Einfachheit | Ist ein Pattern wirklich nötig? | kein Builder für `Quantity` | Pflicht |
| Konstruktorlesbarkeit | Ist der Aufruf verständlich? | keine acht Parameter | Pflicht |
| Factory-Bedarf | Gibt es echte Varianten? | Email/SMS/Push | Pflicht bei Typauswahl |
| OCP | Neue Variante ohne Änderung bestehender Logik? | neue Sender-Bean | Soll |
| Builder-Validierung | Prüft `build()` Pflichtregeln? | Items nicht leer | Pflicht |
| Singleton-State | Ist Bean zustandslos? | keine mutable User-Felder | Pflicht |
| Prototype-Copy | Werden mutable Collections kopiert? | `List.copyOf` | Pflicht |
| Abstract Factory | Erzeugt sie wirklich eine Familie? | Gateway + Validator + Translator | Pflicht |
| Security | Keine Secrets oder freie Providerstrings? | enum/properties | Pflicht |
| Tenant | Kein Tenant-State in Singleton? | Tenant als Parameter | Pflicht bei SaaS |
| Tests | Sind Factory/Builder-Fehlerfälle getestet? | Unsupported Provider | Pflicht |
| Overengineering | Gibt es keinen hypothetischen Pattern-Ballast? | YAGNI | Pflicht |

---

## 21. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Keine Klassen mit getInstance() im Spring-Code.
- Keine nicht-finalen Felder in @Service-Klassen ohne Ausnahme.
- Keine direkten new-Aufrufe für Provider-Clients außerhalb von Configuration/Factory/Adapter.
- Keine switch-Kaskaden über Provider/Channel in Services.
- Keine @Cache, @Transactional oder @Async auf privaten Factory-Methoden.
- Keine Secrets in Factory-Klassen.
- Factories müssen unbekannte Typen explizit ablehnen.
- Test-Builder gehören in src/test, nicht src/main.
```

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule spring_services_should_not_use_get_instance =
        noClasses()
                .that().resideInAPackage("..service..")
                .should().callMethodWhere(target ->
                        target.getName().equals("getInstance"))
                .because("Spring Services sollen Abhängigkeiten per Konstruktorinjektion erhalten.");
```

---

## 22. Migration bestehender Codebereiche

Migration erfolgt schrittweise:

1. `switch`-/`if`-Kaskaden mit direkter Instanzierung suchen.
2. Varianten als Interface-Implementierungen modellieren.
3. Registry Factory einführen.
4. direkte `new`-Aufrufe für technische Clients in Configuration/Factory verschieben.
5. Konstruktoren mit vielen Parametern identifizieren.
6. Command, Value Object oder Builder einführen.
7. manuelle Singletons durch Spring Beans ersetzen.
8. mutable Singleton-Felder entfernen.
9. Prototype-/Template-Kopien auf defensive Copies prüfen.
10. Tests für Auswahl, Validierung und Fehlerfälle ergänzen.

---

## 23. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein einfacher Konstruktor klarer ist als jedes Pattern,
- eine Factory nur für Testdaten im Test-Source-Tree existiert,
- ein manuelles Singleton außerhalb von Spring bewusst und thread-safe eingesetzt wird,
- Performancegründe direkte Erzeugung rechtfertigen,
- Frameworks eine bestimmte Konstruktion erwarten,
- Legacy-Code schrittweise migriert wird.

Ausnahmen müssen bei relevanter Architekturwirkung im Review begründet werden.

---

## 24. Definition of Done

Ein Codebereich erfüllt diese Richtlinie, wenn:

1. einfache Objekte direkt und lesbar erzeugt werden,
2. komplexe Konstruktion nicht über lange Parameterlisten läuft,
3. Builder nur bei echter Konstruktionskomplexität eingesetzt werden,
4. Builder Pflichtfelder und Invarianten prüfen,
5. Varianten nicht über verstreute Switch-Kaskaden erzeugt werden,
6. Factory/Registry neue Varianten ohne Änderung zentraler Services erlaubt,
7. Abstract Factory nur für echte Objektfamilien eingesetzt wird,
8. Spring Singleton Beans zustandslos oder thread-safe sind,
9. kein klassisches `getInstance()` im Spring-Fachcode nötig ist,
10. Prototype-/Wither-Methoden mutable Bestandteile defensiv kopieren,
11. Security-sensitive Provider- und Tenant-Entscheidungen typisiert sind,
12. Factories unbekannte Typen explizit ablehnen,
13. Tests die Erzeugungslogik und Fehlerfälle abdecken,
14. kein Pattern nur aus Gewohnheit oder wegen hypothetischer Zukunft eingeführt wurde.

---

## 25. Quellen und weiterführende Literatur

- Refactoring.Guru — Creational Design Patterns: https://refactoring.guru/design-patterns/creational-patterns
- Refactoring.Guru — Factory Method: https://refactoring.guru/design-patterns/factory-method
- Refactoring.Guru — Abstract Factory: https://refactoring.guru/design-patterns/abstract-factory
- Refactoring.Guru — Design Patterns Catalog: https://refactoring.guru/design-patterns/catalog
- Oracle Java SE 21 API — `java.lang.Record`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html
- Oracle Java SE 21 Language Guide — Record Classes: https://docs.oracle.com/en/java/javase/21/language/records.html
- Spring Framework Reference — Bean Scopes: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html
- Spring Framework Reference — The Singleton Scope: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html#beans-factory-scopes-singleton
- Spring Framework Reference — The Prototype Scope: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html#beans-factory-scopes-prototype
- Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides — Design Patterns: Elements of Reusable Object-Oriented Software
- Joshua Bloch — Effective Java, insbesondere Static Factory Methods, Builder und Singleton
