# QG-JAVA-029 — Entwurfsmuster: Structural Patterns richtig anwenden

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-029 |
| Titel | Entwurfsmuster: Structural Patterns richtig anwenden |
| Status | Accepted / verbindlicher Standard für Strukturmuster im Java-Code |
| Version | 1.0.0 |
| Datum | 2024-10-09 |
| Kategorie | Design Patterns / GoF / Strukturmuster / Architektur |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, Architektur, QA, Security |
| Java-Baseline | Java 21 |
| Framework-Kontext | Java 21, Spring Boot 3.x, Spring AOP, Spring Security, Spring Cache, Spring Transaction Management |
| Geltungsbereich | Adapter, Decorator, Proxy, Facade, Composite, Bridge, Flyweight, Anti-Corruption Layer, externe Systeme, Spring-Proxies, Service-Grenzen, SaaS-/Tenant-Kontext |
| Verbindlichkeit | Strukturmuster werden eingesetzt, wenn Objektstrukturen, Schnittstellen, Zugriffe oder Subsysteme klarer, entkoppelter und testbarer gestaltet werden müssen. Sie dürfen nicht genutzt werden, um einfache direkte Abhängigkeiten unnötig zu verschleiern. |
| Technische Validierung | Gegen GoF-Strukturmuster, Refactoring.Guru, Spring-AOP-Proxying, Spring Transaction Proxy-Verhalten und Java-Records-Dokumentation eingeordnet |
| Kurzentscheidung | Structural Patterns lösen nicht die Frage „Wie erzeuge ich Objekte?“, sondern „Wie verbinde, kapsle, erweitere oder vereinfache ich bestehende Objekte und Schnittstellen?“. Im Java-/Spring-Alltag sind sie besonders wichtig für externe Systeme, Legacy-Integration, technische Querschnittsfunktionen, Security, Caching, Transaktionen, API-Fassaden und komplexe Domänenstrukturen. |

---

## 1. Zweck

Diese Richtlinie erklärt die wichtigsten Strukturmuster und zeigt, wie sie im Java-Alltag sinnvoll eingesetzt werden.

Structural Patterns, auf Deutsch Strukturmuster, helfen dabei, Klassen und Objekte zu größeren Strukturen zusammenzusetzen, ohne dass diese Strukturen starr, unverständlich oder schwer testbar werden. Sie beantworten nicht primär die Frage, wie ein Objekt erzeugt wird. Das war Thema der Creational Patterns. Strukturmuster beantworten eine andere Frage:

```text
Wie verbinde ich bestehende Objekte so, dass der Code flexibel, klar und wartbar bleibt?
```

Im Alltag eines Java-Entwicklers tauchen diese Probleme ständig auf:

- Ein altes Payment-System hat eine andere Schnittstelle als die neue Domäne.
- Ein externer Provider liefert ein fremdes Datenmodell.
- Ein Repository soll zusätzlich Logging, Caching oder Metriken bekommen.
- Ein Service braucht Zugriffskontrolle, ohne dass die Fachlogik verschmutzt.
- Ein Controller kennt zu viele Subsysteme.
- Eine Baumstruktur wie Menü, Kategorie, Bundle oder Rechtehierarchie muss einheitlich behandelt werden.
- Eine Benachrichtigung soll unabhängig vom Versandkanal wachsen.
- Ein Spring-Service verhält sich anders als erwartet, weil `@Transactional` über Proxies funktioniert.
- Ein SaaS-System muss Tenant-Isolation erzwingen, ohne überall dieselbe Prüfung zu duplizieren.

Strukturmuster machen solche Konstruktionen explizit. Sie helfen, Kopplung zu reduzieren, fremde Systeme zu kapseln und technische Zusatzfunktionen kontrolliert um Fachlogik herumzulegen.

---

## 2. Kurzregel

Nutze Adapter, wenn eine fremde oder alte Schnittstelle in dein eigenes Modell übersetzt werden muss. Nutze Decorator, wenn du Verhalten ergänzen willst, ohne die ursprüngliche Klasse zu ändern. Nutze Proxy, wenn Zugriff, Lazy Loading, Remote-Zugriff oder technische Querschnittsfunktionen kontrolliert werden müssen. Nutze Facade, wenn ein komplexes Subsystem eine einfache fachliche Einstiegsschnittstelle braucht. Nutze Composite für Baumstrukturen, bei denen Einzelobjekte und Gruppen gleich behandelt werden sollen. Nutze Bridge, wenn Abstraktion und technische Implementierung unabhängig variieren sollen. Nutze Flyweight nur bei sehr vielen gleichartigen Objekten und messbarem Speicherproblem.

---

## 3. Warum Strukturmuster im Java-Alltag wichtig sind

Viele Java-Systeme werden nicht unwartbar, weil einzelne Methoden schlecht geschrieben sind. Sie werden unwartbar, weil die Beziehungen zwischen Klassen schlecht wachsen.

Ein typischer Fall: Eine Anwendung integriert einen externen Payment Provider. Am Anfang wird dessen SDK direkt im Service verwendet.

```java
stripeClient.paymentIntents().create(...);
```

Dann kommt PayPal hinzu. Danach ein Legacy-Gateway. Danach Mocking im Test. Danach Auditing. Danach Fehlerübersetzung. Nach einem Jahr kennt der Payment-Service drei externe SDKs, mehrere Fehlerformate und technische Details, die mit der eigentlichen Domäne nichts zu tun haben.

Ein Adapter verhindert genau das: Die Domäne spricht mit `PaymentGateway`, nicht mit Stripe, PayPal oder Legacy.

Oder ein anderes Beispiel: Ein Repository soll gecacht und geloggt werden. Man könnte Logging, Cache und Datenzugriff in eine Klasse schreiben. Das funktioniert, aber es mischt Verantwortlichkeiten. Ein Decorator kann Verhalten außen herum ergänzen.

Oder Spring: `@Transactional`, `@Cacheable`, `@Async` und Security-Annotationen funktionieren nicht durch Magie. Spring erzeugt Proxy-Objekte, die Methodenaufrufe abfangen und Zusatzverhalten ausführen. Wer das nicht versteht, macht typische Fehler: private annotierte Methoden, Self-Invocation oder final Methoden, die nicht wie erwartet funktionieren.

Strukturmuster sind deshalb keine Theorie. Sie erklären Dinge, die Java-Entwickler täglich benutzen, oft ohne sie zu benennen.

---

## 4. Grundlagen kurz erklärt

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Strukturmuster | Muster zum Zusammensetzen von Klassen und Objekten. | Adapter, Decorator, Facade |
| Adapter | Übersetzer zwischen inkompatiblen Schnittstellen. | Legacy Payment API zu `PaymentGateway` |
| Decorator | Wrapper, der Verhalten ergänzt. | Caching um Repository |
| Proxy | Stellvertreter, der Zugriff kontrolliert oder Aufrufe abfängt. | Spring `@Transactional` Proxy |
| Facade | Vereinfachte Schnittstelle zu komplexem Subsystem. | `CheckoutFacade` |
| Composite | Baumstruktur mit einheitlicher Behandlung von Einzelobjekt und Gruppe. | Produktbundle |
| Bridge | Trennt Abstraktion von Implementierung. | Notification-Art von Versandkanal |
| Flyweight | Teilt unveränderliche, häufig wiederholte Daten. | Icon-/Format-/Rule-Objekte |
| Wrapper | Objekt, das ein anderes Objekt umhüllt und weiterleitet. | Decorator/Proxy/Adapter |
| Delegate | Das Objekt, an das ein Wrapper weiterleitet. | `private final OrderRepository delegate` |
| Kopplung | Grad, in dem Klassen voneinander abhängig sind. | Service kennt externe SDK-Klassen |
| Anti-Corruption Layer | Schutzschicht gegen fremde Modelle. | Adapter zwischen ERP und eigener Domäne |
| Querschnittsfunktion | Technische Funktion, die viele Bereiche betrifft. | Logging, Security, Caching, Transaktionen |

---

## 5. Die zentrale Linie

Die wichtigste Linie bei Strukturmustern lautet:

```text
Schütze die Domäne vor technischen Details.
Schütze Aufrufer vor unnötiger Komplexität.
Schütze bestehende Klassen vor ständiger Erweiterung.
```

Strukturmuster helfen genau dann, wenn eine direkte Abhängigkeit zu teuer wird:

- zu viele technische Details,
- zu viele externe Modelle,
- zu viele Subsysteme,
- zu viele wiederholte Prüfungen,
- zu viele if/switch-Verzweigungen,
- zu viele Wrapper-Funktionen ohne klare Verantwortung.

Sie schaden, wenn sie nur eingeführt werden, weil ein Pattern bekannt ist. Ein Pattern ohne konkretes Strukturproblem ist Overengineering.

---

## 6. Anwendung im Daily Business eines Java-Entwicklers

Wenn du im Alltag eine neue Abhängigkeit, einen neuen Provider, eine neue API oder ein neues Subsystem einbaust, gehst du so vor:

### Schritt 1: Ist die fremde Schnittstelle Teil meiner Domäne?

Wenn nein, nicht direkt verwenden. Adapter bauen.

```java
PaymentGateway gateway = new StripePaymentAdapter(stripeClient);
```

### Schritt 2: Muss ich Verhalten ergänzen, ohne die Klasse zu ändern?

Wenn ja, Decorator prüfen.

```java
OrderRepository repository =
        new LoggingOrderRepository(new CachingOrderRepository(jpaRepository, cache));
```

### Schritt 3: Muss Zugriff kontrolliert oder technisch abgefangen werden?

Wenn ja, Proxy prüfen.

```java
SecuredOrderRepository secured = new SecuredOrderRepository(repository, accessPolicy);
```

Oder in Spring: `@Transactional`, `@Cacheable`, `@PreAuthorize` bewusst als Proxy-Verhalten verstehen.

### Schritt 4: Kennt mein Controller oder Service zu viele Subsysteme?

Wenn ja, Facade oder Application Service prüfen.

```java
checkoutFacade.checkout(command);
```

### Schritt 5: Habe ich eine Baumstruktur?

Wenn Einzelobjekt und Gruppe gleich behandelt werden sollen: Composite.

```java
PriceComponent bundle = new ProductBundle(...);
Money total = bundle.totalPrice();
```

### Schritt 6: Variieren zwei Dimensionen unabhängig?

Wenn ja, Bridge prüfen.

```text
Benachrichtigungstyp: Order, Invoice, Security
Versandkanal: Email, SMS, Push
```

Nicht jede Kombination als eigene Klasse bauen. Abstraktion und Implementierung trennen.

### Schritt 7: Habe ich Millionen kleiner gleichartiger Objekte?

Wenn ja, Flyweight prüfen. Vorher messen.

---

## 7. Entscheidungsmatrix

| Problem | Muster | Details/Erklärung |
|---|---|---|
| Fremdes Interface passt nicht | Adapter | Übersetzt fremd nach intern |
| Legacy-System soll gekapselt werden | Adapter / Anti-Corruption Layer | Domäne bleibt sauber |
| Verhalten ergänzen ohne Klasse zu ändern | Decorator | Logging, Cache, Metrics |
| Zugriff kontrollieren | Proxy | Security, Lazy Loading, Remote |
| Spring-Annotation wirkt um Methode herum | Proxy | AOP-basiert |
| Viele Subsysteme sollen einfach nutzbar sein | Facade | vereinfachter Einstieg |
| Controller hat zu viele Abhängigkeiten | Facade / Application Service | Orchestrierung bündeln |
| Einzelobjekt und Gruppe gleich behandeln | Composite | Baumstruktur |
| Zwei Achsen variieren unabhängig | Bridge | Abstraktion vs. Implementierung |
| Sehr viele gleiche Objektteile | Flyweight | Speicher sparen |
| Nur eine einfache direkte Abhängigkeit | Kein Pattern | direkt halten |

---

# Teil A — Adapter

## 8. Adapter

### 8.1 Bedeutung

Ein Adapter übersetzt zwischen zwei Schnittstellen, die nicht zusammenpassen.

Einfach gesagt:

```text
Adapter = Übersetzer.
```

Er ist besonders wichtig, wenn ein externes System, ein Legacy-System oder ein fremdes SDK nicht zur eigenen Domänensprache passt.

### 8.2 Schlechte Anwendung: Fremde Schnittstelle direkt in der Domäne

```java
@Service
public class PaymentService {

    private final LegacyPaymentGateway legacyGateway;

    public PaymentResult charge(PaymentCommand command) {
        boolean success = legacyGateway.processPayment(
                command.accountId().value(),
                command.amount().amount().doubleValue(),
                command.amount().currency().getCurrencyCode()
        );

        if (success) {
            return PaymentResult.success(UUID.randomUUID().toString());
        }

        return PaymentResult.failure("legacy gateway declined");
    }
}
```

Probleme:

- Domänenservice kennt Legacy-Signatur.
- `double` für Geldbetrag ist fachlich riskant.
- Legacy-Fehlerformat sickert in die Domäne.
- Austausch des Legacy-Gateways betrifft Fachlogik.
- Tests müssen Legacy-Details kennen.

### 8.3 Gute Anwendung: eigener Port + Adapter

Eigene Domänenschnittstelle:

```java
public interface PaymentGateway {

    PaymentResult charge(PaymentCommand command);
}
```

Legacy-Schnittstelle:

```java
public interface LegacyPaymentGateway {

    boolean processPayment(String accountNumber, double amount, String currencyCode);
}
```

Adapter:

```java
@Component
public class LegacyPaymentAdapter implements PaymentGateway {

    private final LegacyPaymentGateway legacyGateway;

    public LegacyPaymentAdapter(LegacyPaymentGateway legacyGateway) {
        this.legacyGateway = legacyGateway;
    }

    @Override
    public PaymentResult charge(PaymentCommand command) {
        boolean success = legacyGateway.processPayment(
                command.accountId().value(),
                command.amount().amount().doubleValue(),
                command.amount().currency().getCurrencyCode()
        );

        if (success) {
            return PaymentResult.success(TransactionId.newId());
        }

        return PaymentResult.failure(PaymentFailureReason.DECLINED_BY_PROVIDER);
    }
}
```

Domänenservice:

```java
@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult charge(PaymentCommand command) {
        return paymentGateway.charge(command);
    }
}
```

### 8.4 Adapter als Anti-Corruption Layer

In DDD-Kontext ist ein Adapter häufig Teil eines Anti-Corruption Layers. Das bedeutet: Ein fremdes Modell wird nicht einfach übernommen, sondern in das eigene Modell übersetzt.

Schlecht:

```java
public void createInvoice(StripeCustomer stripeCustomer) {
    ...
}
```

Besser:

```java
public BillingCustomerReference toBillingCustomer(StripeCustomer stripeCustomer) {
    return new BillingCustomerReference(
            new PaymentProviderCustomerId(stripeCustomer.getId()),
            new EmailAddress(stripeCustomer.getEmail())
    );
}
```

### 8.5 Daily-Business-Regel

Immer wenn du ein externes SDK, eine Legacy-API oder ein fremdes DTO direkt in einem Domain- oder Application-Service verwendest, prüfe Adapter.

Review-Frage:

```text
Warum kennt unsere Domäne dieses externe Modell?
```

---

# Teil B — Decorator

## 9. Decorator

### 9.1 Bedeutung

Ein Decorator ergänzt Verhalten zu einem Objekt, ohne dessen Klasse zu ändern. Er implementiert dasselbe Interface wie das dekorierte Objekt und leitet an dieses weiter.

Einfach gesagt:

```text
Decorator = Verhalten außen herumlegen.
```

Typische Zusatzverhalten:

- Logging,
- Caching,
- Metriken,
- Tracing,
- Retry,
- Validation,
- Auditing,
- Kompression,
- Verschlüsselung.

### 9.2 Schlechte Anwendung: alles in einer Klasse

```java
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final EntityManager entityManager;
    private final Cache<OrderId, Order> cache;
    private final MeterRegistry meterRegistry;

    public Order findById(OrderId id) {
        log.debug("Loading order {}", id);

        var cached = cache.getIfPresent(id);
        if (cached != null) {
            meterRegistry.counter("orders.cache.hit").increment();
            return cached;
        }

        var order = entityManager.find(Order.class, id.value());
        cache.put(id, order);
        meterRegistry.counter("orders.cache.miss").increment();

        return order;
    }
}
```

Probleme:

- Repository macht Persistenz, Logging, Caching und Metriken.
- Testfälle werden größer.
- Änderungen an Cache oder Logging ändern Repository.
- Wiederverwendung ist schwer.
- Verantwortung ist vermischt.

### 9.3 Gute Anwendung: Interface + Decorator

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    Order save(Order order);
}
```

Basisimplementierung:

```java
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataRepository;
    private final OrderMapper mapper;

    @Override
    public Optional<Order> findById(OrderId id) {
        return springDataRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(springDataRepository.save(mapper.toEntity(order)));
    }
}
```

Caching Decorator:

```java
public class CachingOrderRepository implements OrderRepository {

    private final OrderRepository delegate;
    private final Cache<OrderId, Order> cache;

    public CachingOrderRepository(OrderRepository delegate, Cache<OrderId, Order> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(cache.get(id, key ->
                delegate.findById(key).orElse(null)));
    }

    @Override
    public Order save(Order order) {
        var saved = delegate.save(order);
        cache.invalidate(saved.id());
        return saved;
    }
}
```

Logging Decorator:

```java
public class LoggingOrderRepository implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(LoggingOrderRepository.class);

    private final OrderRepository delegate;

    public LoggingOrderRepository(OrderRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        log.debug("Loading order {}", id);
        var result = delegate.findById(id);
        log.debug("Loaded order {} result={}", id, result.isPresent());
        return result;
    }

    @Override
    public Order save(Order order) {
        log.info("Saving order {}", order.id());
        return delegate.save(order);
    }
}
```

Spring-Konfiguration:

```java
@Configuration
public class OrderRepositoryConfiguration {

    @Bean
    @Primary
    public OrderRepository orderRepository(
            JpaOrderRepository jpaOrderRepository,
            Cache<OrderId, Order> orderCache) {

        return new LoggingOrderRepository(
                new CachingOrderRepository(jpaOrderRepository, orderCache)
        );
    }
}
```

### 9.4 Reihenfolge ist wichtig

```java
new LoggingOrderRepository(
        new CachingOrderRepository(
                new JpaOrderRepository(...)
        )
);
```

Hier loggt der äußerste Decorator jeden Aufruf. Der Cache entscheidet innen, ob JPA aufgerufen wird.

Andere Reihenfolge:

```java
new CachingOrderRepository(
        new LoggingOrderRepository(
                new JpaOrderRepository(...)
        )
);
```

Jetzt wird bei Cache-Hit kein Logging des JPA-Zugriffs erfolgen, weil der innere Decorator nicht erreicht wird.

Review-Frage:

```text
Ist die Decorator-Reihenfolge bewusst gewählt und getestet?
```

### 9.5 Decorator vs. Vererbung

Decorator ist oft besser als Vererbung, wenn Verhalten flexibel kombiniert werden soll.

Schlecht:

```java
class CachedLoggingJpaOrderRepository extends JpaOrderRepository {
}
```

Das skaliert nicht. Bei Kombinationen entstehen viele Klassen.

Besser:

```text
Logging + Caching + Metrics werden gestapelt.
```

---

# Teil C — Proxy

## 10. Proxy

### 10.1 Bedeutung

Ein Proxy ist ein Stellvertreter für ein Objekt. Er hat dieselbe Schnittstelle, kontrolliert aber den Zugriff oder ergänzt Verhalten vor/nach dem Aufruf.

Einfach gesagt:

```text
Proxy = Stellvertreter mit Kontrolle.
```

Typische Einsatzfälle:

- Lazy Loading,
- Zugriffskontrolle,
- Remote-Zugriff,
- Transaktionen,
- Caching,
- Security,
- Metriken,
- AOP.

### 10.2 Protection Proxy: Zugriffskontrolle

```java
public class SecuredOrderRepository implements OrderRepository {

    private final OrderRepository delegate;
    private final OrderAccessPolicy accessPolicy;
    private final CurrentUserProvider currentUserProvider;

    public SecuredOrderRepository(
            OrderRepository delegate,
            OrderAccessPolicy accessPolicy,
            CurrentUserProvider currentUserProvider) {
        this.delegate = delegate;
        this.accessPolicy = accessPolicy;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        var order = delegate.findById(id);

        order.ifPresent(value ->
                accessPolicy.assertCanAccess(currentUserProvider.currentUser(), value));

        return order;
    }

    @Override
    public Order save(Order order) {
        accessPolicy.assertCanModify(currentUserProvider.currentUser(), order);
        return delegate.save(order);
    }
}
```

### 10.3 Virtual Proxy: Lazy Loading

```java
public class LazyReportProxy implements Report {

    private final ReportId reportId;
    private final ReportRepository repository;

    private Report loaded;

    public LazyReportProxy(ReportId reportId, ReportRepository repository) {
        this.reportId = reportId;
        this.repository = repository;
    }

    @Override
    public String content() {
        if (loaded == null) {
            loaded = repository.loadFull(reportId);
        }
        return loaded.content();
    }
}
```

Vorsicht: Lazy Loading braucht Thread-Safety, wenn mehrere Threads dasselbe Proxy-Objekt nutzen.

### 10.4 Spring AOP als Proxy-Mechanismus

Spring AOP erzeugt Proxies für Beans. Diese Proxies können Methodenaufrufe abfangen und Zusatzverhalten ausführen. Spring nutzt dafür JDK Dynamic Proxies oder CGLIB-Proxies.

Beispiele:

```java
@Transactional
public void placeOrder(PlaceOrderCommand command) {
    ...
}
```

```java
@Cacheable("products")
public ProductDto findById(ProductId id) {
    ...
}
```

```java
@PreAuthorize("hasAuthority('ORDER_CANCEL')")
public void cancel(OrderId orderId) {
    ...
}
```

Die Annotation allein ist nicht die Magie. Der Aufruf muss über den Spring-Proxy laufen.

### 10.5 Typische Proxy-Fallen

#### Self-Invocation

Schlecht:

```java
@Service
public class OrderService {

    public void placeOrder(PlaceOrderCommand command) {
        saveOrder(command); // interner Aufruf, Proxy wird umgangen
    }

    @Transactional
    public void saveOrder(PlaceOrderCommand command) {
        ...
    }
}
```

Wenn `placeOrder()` innerhalb derselben Klasse `saveOrder()` aufruft, läuft dieser Aufruf nicht über den Proxy. Das Transaktionsverhalten kann ausbleiben.

Besser:

```java
@Service
public class OrderPlacementService {

    private final OrderTransactionService transactionService;

    public void placeOrder(PlaceOrderCommand command) {
        transactionService.saveOrder(command);
    }
}
```

```java
@Service
public class OrderTransactionService {

    @Transactional
    public void saveOrder(PlaceOrderCommand command) {
        ...
    }
}
```

#### Private Methoden

Schlecht:

```java
@Transactional
private void saveInternal() {
    ...
}
```

Private Methoden können nicht sinnvoll über Spring AOP proxied werden.

#### Final Methoden/Klassen

Final Methoden können nicht überschrieben werden. Bei CGLIB-basierten Proxies kann das relevant sein. Deshalb Annotationen auf final Methoden vermeiden, wenn Proxying erwartet wird.

### 10.6 Daily-Business-Regel

Wenn du `@Transactional`, `@Cacheable`, `@Async` oder Security-Annotationen nutzt, prüfe:

1. Ist die Methode public?
2. Wird sie von außen über eine Spring Bean aufgerufen?
3. Gibt es keine Self-Invocation?
4. Ist die Klasse/methode proxyfähig?
5. Ist die Annotation an der richtigen Schicht?

---

# Teil D — Facade

## 11. Facade

### 11.1 Bedeutung

Eine Facade bietet eine einfache Schnittstelle zu einem komplexen Subsystem.

Einfach gesagt:

```text
Facade = vereinfachter Eingang.
```

Sie versteckt Komplexität, aber sie darf nicht zu einem God Service werden.

### 11.2 Schlechte Anwendung: Controller kennt alles

```java
@RestController
public class CheckoutController {

    private final InventoryService inventoryService;
    private final PaymentProcessor paymentProcessor;
    private final ShippingCalculator shippingCalculator;
    private final NotificationDispatcher notificationDispatcher;
    private final AuditLogger auditLogger;
    private final OrderRepository orderRepository;

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        var shippingCost = shippingCalculator.calculate(request.shippingAddress());
        var payment = paymentProcessor.charge(request.payment(), shippingCost);

        if (!payment.succeeded()) {
            return CheckoutResponse.paymentFailed();
        }

        inventoryService.reserve(request.items());
        var order = orderRepository.save(new Order(...));
        notificationDispatcher.notify(order);
        auditLogger.log("ORDER_PLACED", order);

        return CheckoutResponse.success(order.id());
    }
}
```

Probleme:

- Controller orchestriert Fachprozess.
- Controller kennt zu viele Subsysteme.
- Test ist schwer.
- Änderungen am Checkout-Prozess ändern API-Schicht.
- Transaktionsgrenzen sind unklar.

### 11.3 Gute Anwendung: Checkout Facade / Application Service

```java
@Service
public class CheckoutFacade {

    private final ShippingCalculator shippingCalculator;
    private final PaymentProcessor paymentProcessor;
    private final InventoryReservationService inventoryReservationService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher events;

    @Transactional
    public CheckoutResult checkout(CheckoutCommand command) {
        var shippingCost = shippingCalculator.calculate(command.shippingAddress());
        var total = command.subtotal().add(shippingCost);

        var payment = paymentProcessor.charge(command.payment(), total);

        if (!payment.succeeded()) {
            return CheckoutResult.paymentFailed(payment.failureReason());
        }

        inventoryReservationService.reserve(command.items());

        var order = Order.place(command.customerId(), command.items(), total, payment.transactionId());
        orderRepository.save(order);

        events.publishEvent(new OrderPlacedEvent(order.id(), order.customerId(), total));

        return CheckoutResult.success(order.id());
    }
}
```

Controller:

```java
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutFacade checkoutFacade;
    private final CheckoutMapper mapper;

    @PostMapping
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        var result = checkoutFacade.checkout(mapper.toCommand(request));
        return mapper.toResponse(result);
    }
}
```

### 11.4 Facade ≠ God Service

Eine Facade ist gut, wenn sie:

- eine einfache Einstiegsmethode bietet,
- Subsysteme koordiniert,
- technische Details versteckt,
- selbst nicht alle Fachregeln enthält,
- auf Domänenobjekte und Services delegiert.

Eine Facade wird schlecht, wenn sie:

- alle Fachregeln selbst enthält,
- viele private Methoden mit Businesslogik hat,
- stark wächst,
- jedes neue Feature aufnimmt,
- mehrere unabhängige Use Cases vermischt.

Review-Frage:

```text
Ist diese Facade ein klarer Use Case oder ein neuer Sammelservice?
```

---

# Teil E — Composite

## 12. Composite

### 12.1 Bedeutung

Composite erlaubt, Einzelobjekte und Gruppen von Objekten gleich zu behandeln. Es eignet sich für Baumstrukturen.

Einfach gesagt:

```text
Composite = Einzelteil und Gruppe haben dieselbe Schnittstelle.
```

Typische Beispiele:

- Produktbundle,
- Kategorienbaum,
- Menüstruktur,
- Organisationsstruktur,
- Rechtebaum,
- UI-Komponenten,
- Preisbestandteile,
- Regelgruppen,
- Datei/Ordner-Struktur.

### 12.2 Gute Anwendung: Preisstruktur

```java
public interface PriceComponent {

    Money totalPrice();

    String description();
}
```

Leaf:

```java
public record Product(
        ProductId id,
        String name,
        Money price
) implements PriceComponent {

    @Override
    public Money totalPrice() {
        return price;
    }

    @Override
    public String description() {
        return "%s: %s".formatted(name, price);
    }
}
```

Composite:

```java
public class ProductBundle implements PriceComponent {

    private final String name;
    private final List<PriceComponent> components = new ArrayList<>();

    public ProductBundle(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void add(PriceComponent component) {
        components.add(Objects.requireNonNull(component));
    }

    @Override
    public Money totalPrice() {
        return components.stream()
                .map(PriceComponent::totalPrice)
                .reduce(Money.zero(Currency.getInstance("EUR")), Money::add);
    }

    @Override
    public String description() {
        return name + " ["
                + components.stream()
                        .map(PriceComponent::description)
                        .collect(Collectors.joining(", "))
                + "]";
    }
}
```

Nutzung:

```java
var book = new Product(new ProductId("BOOK"), "Java Book", Money.eur("49.99"));
var pen = new Product(new ProductId("PEN"), "Pen", Money.eur("2.99"));

var bundle = new ProductBundle("Developer Kit");
bundle.add(book);
bundle.add(pen);

Money total = bundle.totalPrice();
```

### 12.3 Wichtige Risiken

Composite kann gefährlich werden, wenn:

- Rekursion sehr tief wird,
- Zyklen entstehen,
- unterschiedliche Währungen vermischt werden,
- Gruppen unerwartet leer sind,
- Performance bei großen Bäumen nicht geprüft wird,
- Parent/Child-Beziehungen mutable und unkontrolliert sind.

### 12.4 Zyklenschutz

```java
public void add(PriceComponent component) {
    if (component == this) {
        throw new IllegalArgumentException("bundle cannot contain itself");
    }
    components.add(component);
}
```

Bei komplexeren Bäumen muss Zyklenschutz über IDs oder Pfade erfolgen.

---

# Teil F — Bridge

## 13. Bridge

### 13.1 Bedeutung

Bridge trennt eine Abstraktion von ihrer Implementierung, sodass beide unabhängig wachsen können.

Einfach gesagt:

```text
Bridge = zwei Veränderungsachsen voneinander trennen.
```

Beispiel:

- Achse 1: Art der Benachrichtigung.
- Achse 2: Versandkanal.

Ohne Bridge entstehen viele Klassen:

```text
EmailOrderNotification
SmsOrderNotification
PushOrderNotification
EmailInvoiceNotification
SmsInvoiceNotification
PushInvoiceNotification
EmailSecurityNotification
SmsSecurityNotification
PushSecurityNotification
```

Mit Bridge:

```text
NotificationType × MessageSender
```

### 13.2 Gute Anwendung

Implementierungsseite:

```java
public interface MessageSender {

    void send(ContactPoint recipient, MessageContent content);
}
```

```java
@Component
public class EmailMessageSender implements MessageSender {

    private final EmailClient emailClient;

    @Override
    public void send(ContactPoint recipient, MessageContent content) {
        emailClient.send(recipient.email(), content.subject(), content.body());
    }
}
```

```java
@Component
public class SmsMessageSender implements MessageSender {

    private final SmsClient smsClient;

    @Override
    public void send(ContactPoint recipient, MessageContent content) {
        smsClient.send(recipient.phoneNumber(), content.body());
    }
}
```

Abstraktionsseite:

```java
public abstract class Notification {

    protected final MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = sender;
    }

    public abstract void notifyUser(User user, NotificationEvent event);
}
```

```java
public class OrderNotification extends Notification {

    public OrderNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void notifyUser(User user, NotificationEvent event) {
        sender.send(
                user.contactPoint(),
                new MessageContent(
                        "Order update",
                        "Your order status changed: " + event.description()
                )
        );
    }
}
```

### 13.3 Wann Bridge sinnvoll ist

Bridge ist sinnvoll, wenn zwei Dimensionen unabhängig variieren:

| Abstraktion | Implementierung |
|---|---|
| Notification Type | Email/SMS/Push |
| Export Document | PDF/CSV/XLSX |
| Report | Local File/S3/Email |
| Payment Workflow | Stripe/PayPal/Legacy |
| Rendering | HTML/PDF/Text |
| Alert Type | Slack/Email/PagerDuty |

### 13.4 Wann Bridge übertrieben ist

Bridge ist übertrieben, wenn:

- es nur eine Dimension gibt,
- nur eine Implementierung existiert,
- keine echte Kombinationsvielfalt erwartet wird,
- Strategy Pattern ausreicht,
- der Code dadurch schwerer statt klarer wird.

---

# Teil G — Flyweight

## 14. Flyweight

### 14.1 Bedeutung

Flyweight teilt unveränderliche Daten zwischen vielen Objekten, um Speicher zu sparen.

Einfach gesagt:

```text
Flyweight = gemeinsame, unveränderliche Teile nicht tausendfach speichern.
```

Flyweight ist ein Spezialmuster. Es sollte nur eingesetzt werden, wenn ein messbares Speicherproblem existiert.

### 14.2 Beispiel: Produktdarstellung mit geteilten Stammdaten

Viele Offer-Views können dieselben Produktstammdaten referenzieren.

```java
public record ProductDescriptor(
        ProductId productId,
        String name,
        CategoryId categoryId,
        String imageUrl
) {}
```

```java
public record OfferView(
        OfferId offerId,
        ProductDescriptor product,
        Money discountedPrice,
        CampaignId campaignId
) {}
```

Wenn `ProductDescriptor` unveränderlich ist, kann er geteilt werden.

### 14.3 Flyweight Factory

```java
@Component
public class ProductDescriptorFlyweightFactory {

    private final ConcurrentMap<ProductId, ProductDescriptor> descriptors = new ConcurrentHashMap<>();

    public ProductDescriptor descriptorFor(Product product) {
        return descriptors.computeIfAbsent(product.id(), ignored ->
                new ProductDescriptor(
                        product.id(),
                        product.name(),
                        product.categoryId(),
                        product.imageUrl()
                )
        );
    }
}
```

### 14.4 Risiken

- Cache wächst unbegrenzt,
- stale Daten bei Produktänderung,
- Thread-Safety,
- unnötige Komplexität,
- falsches Teilen veränderlicher Objekte.

Regel:

```text
Flyweight nur bei gemessenem Speicherproblem und unveränderlichen geteilten Daten.
```

In vielen Java-Anwendungen ist normales Caching oder Value Object Design ausreichend.

---

## 15. Spring-Strukturmuster im Alltag

Spring nutzt mehrere Strukturmuster selbst:

| Spring-Funktion | Musterbezug | Erklärung |
|---|---|---|
| `@Transactional` | Proxy | Proxy startet/committet/rollbackt Transaktion |
| `@Cacheable` | Proxy/Decorator | Cache-Verhalten um Methode herum |
| `@PreAuthorize` | Proxy | Zugriffskontrolle vor Methodenaufruf |
| `RestClient` Wrapper | Adapter | externe HTTP-API hinter internem Port |
| `Repository` Adapter | Adapter | Persistenz hinter Domain-Port |
| `@Controller` | Adapter | HTTP zu Application Use Case |
| `ApplicationService` | Facade | vereinfachter Use-Case-Einstieg |
| `HandlerInterceptor` | Decorator/Interceptor | Verhalten um Web Requests |
| JPA Lazy Proxy | Proxy | Relation wird bei Zugriff geladen |

Wichtig: Nur weil Spring diese Muster nutzt, heißt das nicht, dass jeder Service eigene Proxies oder Decorators braucht. Die Muster erklären Verhalten und helfen bei bewussten Architekturentscheidungen.

---

## 16. Security- und SaaS-Aspekte

### 16.1 Adapter schützt vor fremden Modellen

Externe Systeme dürfen keine internen Sicherheitsannahmen bestimmen. Ein Adapter übersetzt und normalisiert.

```java
public TenantId tenantIdFrom(ExternalMerchant externalMerchant) {
    return tenantMappingRepository.findTenantIdForExternalMerchant(externalMerchant.id())
            .orElseThrow(() -> new UnknownExternalMerchantException(externalMerchant.id()));
}
```

### 16.2 Proxy kann Zugriff kontrollieren

```java
public class TenantAwareOfferRepository implements OfferRepository {

    private final OfferRepository delegate;
    private final TenantAccessPolicy accessPolicy;

    @Override
    public Optional<Offer> findById(TenantId tenantId, OfferId offerId) {
        accessPolicy.assertTenantAllowed(tenantId);
        return delegate.findById(tenantId, offerId);
    }
}
```

### 16.3 Decorator darf keine sensiblen Daten loggen

Schlecht:

```java
log.info("Payment command={}", command);
```

Besser:

```java
log.info("Payment requested orderId={} provider={}",
        command.orderId(),
        command.provider());
```

### 16.4 Facade darf Security nicht verschlucken

Eine Facade muss Security- und Tenant-Kontext explizit weiterreichen.

```java
checkoutFacade.checkout(command, accessScope);
```

Nicht:

```java
checkoutFacade.checkout(command);
```

wenn die Fachentscheidung tenant- oder userabhängig ist.

### 16.5 Composite für Berechtigungen

Composite kann für Berechtigungsregeln sinnvoll sein:

```java
public interface AccessRule {
    boolean allows(AccessContext context);
}
```

```java
public class AndRule implements AccessRule {

    private final List<AccessRule> rules;

    @Override
    public boolean allows(AccessContext context) {
        return rules.stream().allMatch(rule -> rule.allows(context));
    }
}
```

Aber Security-Regelbäume müssen getestet und auditierbar sein.

---

## 17. Testing

### 17.1 Adapter-Test

```java
class LegacyPaymentAdapterTest {

    @Test
    void charge_returnsSuccess_whenLegacyGatewayAcceptsPayment() {
        var legacy = mock(LegacyPaymentGateway.class);
        when(legacy.processPayment("ACC-1", 19.99, "EUR")).thenReturn(true);

        var adapter = new LegacyPaymentAdapter(legacy);

        var result = adapter.charge(new PaymentCommand(
                new AccountId("ACC-1"),
                Money.eur("19.99")
        ));

        assertThat(result.succeeded()).isTrue();
    }
}
```

### 17.2 Decorator-Test

```java
class CachingOrderRepositoryTest {

    @Test
    void findById_usesDelegateOnlyOnce_forSameId() {
        var delegate = mock(OrderRepository.class);
        var cache = Caffeine.newBuilder().build();
        var repository = new CachingOrderRepository(delegate, cache);

        var orderId = new OrderId(UUID.randomUUID());
        when(delegate.findById(orderId)).thenReturn(Optional.of(order(orderId)));

        repository.findById(orderId);
        repository.findById(orderId);

        verify(delegate, times(1)).findById(orderId);
    }
}
```

### 17.3 Proxy-Test für Security

```java
class SecuredOrderRepositoryTest {

    @Test
    void findById_throwsAccessDenied_whenUserCannotAccessOrder() {
        var delegate = mock(OrderRepository.class);
        var policy = mock(OrderAccessPolicy.class);
        var userProvider = mock(CurrentUserProvider.class);

        var order = order(new OrderId(UUID.randomUUID()));
        when(delegate.findById(order.id())).thenReturn(Optional.of(order));
        doThrow(new AccessDeniedException("denied"))
                .when(policy).assertCanAccess(any(), eq(order));

        var repository = new SecuredOrderRepository(delegate, policy, userProvider);

        assertThatThrownBy(() -> repository.findById(order.id()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
```

### 17.4 Facade-Test

Facade-Tests prüfen Orchestrierung, aber nicht jedes Detail der Subsysteme.

```java
@Test
void checkout_reservesInventoryAndSavesOrder_whenPaymentSucceeds() {
    when(paymentProcessor.charge(any(), any()))
            .thenReturn(PaymentResult.success(new TransactionId("tx-1")));

    var result = checkoutFacade.checkout(validCheckoutCommand());

    assertThat(result.succeeded()).isTrue();
    verify(inventoryReservationService).reserve(any());
    verify(orderRepository).save(any(Order.class));
}
```

### 17.5 Composite-Test

```java
@Test
void totalPrice_sumsNestedBundlePrices() {
    var root = new ProductBundle("Root");
    var child = new ProductBundle("Child");

    child.add(new Product(new ProductId("A"), "A", Money.eur("10.00")));
    child.add(new Product(new ProductId("B"), "B", Money.eur("5.00")));
    root.add(child);

    assertThat(root.totalPrice()).isEqualTo(Money.eur("15.00"));
}
```

---

## 18. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Adapter | Kapselt er fremde Modelle? | Stripe → PaymentGateway | Pflicht bei externen APIs |
| Decorator | Ergänzt er klar ein Verhalten? | Caching, Logging | Prüfen |
| Proxy | Ist Zugriff/Lazy/AOP bewusst? | Security Proxy | Prüfen |
| Facade | Vereinfacht sie ein Subsystem? | CheckoutFacade | Prüfen |
| Facade-Grenze | Wird sie nicht zum God Service? | delegiert Fachlogik | Pflicht |
| Composite | Gibt es wirklich Baumstruktur? | Bundle/Kategorie | Pflicht |
| Bridge | Gibt es zwei unabhängige Achsen? | Notification × Channel | Prüfen |
| Flyweight | Gibt es messbares Speicherproblem? | viele Descriptoren | Pflicht |
| Spring AOP | Gibt es keine Self-Invocation? | externe Bean-Aufrufe | Pflicht |
| Security | Wird Zugriff nicht nur dekorativ geprüft? | Policy + Tests | Pflicht |
| Tenant | Ist Tenant-Kontext explizit? | AccessScope | Pflicht bei SaaS |
| Logging | Keine sensiblen Daten im Decorator? | keine Tokens | Pflicht |
| Tests | Sind Wrapper-Verhalten getestet? | Decorator/Proxy/Adapter | Pflicht |
| Overengineering | Ist Pattern wirklich nötig? | kein Adapter für gleiche API | Pflicht |

---

## 19. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Domain-Pakete dürfen nicht von externen SDK-Paketen abhängen.
- Controller dürfen nicht direkt mehrere technische Subsysteme orchestrieren.
- @Transactional darf nicht auf private Methoden gesetzt werden.
- @Cacheable darf nicht auf private Methoden gesetzt werden.
- Services dürfen externe Provider-Dtos nicht als Parameter in Public Methods haben.
- Adapter-Klassen müssen in adapter/infrastructure-Paketen liegen.
- Security-sensitive Repository-Zugriffe müssen TenantId oder AccessScope erhalten.
- Decorator-Klassen dürfen keine sensitiven Felder loggen.
```

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule domain_must_not_depend_on_external_payment_sdk =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.stripe..", "com.paypal..")
                .because("Externe SDKs müssen über Adapter gekapselt werden.");
```

```java
@ArchTest
static final ArchRule controllers_should_not_depend_on_many_subsystems =
        classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().haveOnlyFinalFields()
                .because("Controller sollen als Adapter dünn bleiben und nicht Subsysteme orchestrieren.");
```

Die zweite Regel ist nur ein grober Hinweis. Gute Architekturprüfung braucht zusätzlich Review.

---

## 20. Migration bestehender Codebereiche

Migration erfolgt schrittweise:

1. Externe SDK-Abhängigkeiten in Domain/Application Services suchen.
2. Eigene Ports definieren.
3. Adapter für externe Systeme bauen.
4. Services auf eigene Ports umstellen.
5. Cross-cutting Code wie Logging/Caching aus Kernklassen entfernen.
6. Decorator oder Spring-AOP-Mechanismus bewusst einsetzen.
7. Controller mit vielen Abhängigkeiten auf Facade/Application Service umstellen.
8. Baumlogik mit Composite modellieren, wenn Einzel und Gruppe gleich behandelt werden.
9. Spring-Proxy-Fallen prüfen.
10. Security-/Tenant-Zugriffe in Proxy/Policy/AccessScope zentralisieren.
11. Tests für Adapter, Decorator, Proxy und Facade ergänzen.
12. Überflüssige Pattern-Schichten entfernen.

---

## 21. Anti-Patterns

### 21.1 Adapter ohne Übersetzung

Wenn Adapter nur 1:1 durchleitet und keinen Schutz bietet, ist er möglicherweise unnötig.

### 21.2 Decorator mit Businesslogik

Decorator sollte Zusatzverhalten kapseln, nicht zentrale Fachlogik übernehmen.

### 21.3 Proxy als versteckte Security

Security-Proxies müssen testbar und sichtbar sein. Versteckte Zugriffskontrolle ohne Tests ist gefährlich.

### 21.4 Facade als God Service

Wenn Facade alles selbst macht, ist sie kein Strukturmuster mehr, sondern ein neuer Monolith im Kleinen.

### 21.5 Composite für flache Listen

Nicht jede Liste ist ein Baum. Composite nur einsetzen, wenn rekursive Struktur wirklich existiert.

### 21.6 Bridge ohne zwei Achsen

Bridge ist übertrieben, wenn nur eine Dimension variiert.

### 21.7 Flyweight ohne Messung

Flyweight macht Code komplexer. Ohne Speicherproblem nicht einsetzen.

### 21.8 Spring-Annotation ohne Proxy-Verständnis

`@Transactional` auf private Methoden oder Self-Invocation ist ein klassischer Fehler.

---

## 22. Ausnahmen

Ausnahmen sind zulässig, wenn:

- eine direkte Abhängigkeit einfacher und fachlich unkritisch ist,
- ein externer Client nur in einem technischen Adapter verwendet wird,
- eine Facade bei kleinem Use Case unnötig wäre,
- Decorator-Reihenfolge keinen Mehrwert bringt,
- Spring AOP das benötigte Verhalten bereits sauber abdeckt,
- Legacy-Code schrittweise migriert wird,
- ein Pattern nur im Testkontext als Hilfsstruktur verwendet wird.

Ausnahmen müssen im Review begründet werden, wenn sie externe Systeme, Security, Tenant-Grenzen oder Transaktionen betreffen.

---

## 23. Definition of Done

Ein Codebereich erfüllt diese Richtlinie, wenn:

1. externe Systeme nicht direkt in der Domäne verwendet werden,
2. Adapter fremde Modelle in eigene Modelle übersetzen,
3. Decorators klar ein Zusatzverhalten kapseln,
4. Decorator-Reihenfolge bewusst ist,
5. Proxies für Zugriff, Lazy Loading oder AOP bewusst eingesetzt werden,
6. Spring-Proxy-Fallen geprüft sind,
7. Facades komplexe Subsysteme vereinfachen, ohne God Services zu werden,
8. Composite nur bei echter Baumstruktur verwendet wird,
9. Bridge nur bei zwei unabhängigen Veränderungsachsen eingesetzt wird,
10. Flyweight nur bei gemessenem Speicherproblem eingesetzt wird,
11. Security- und Tenant-Kontext nicht verloren gehen,
12. sensible Daten nicht in Logging-/Decorator-Schichten auftauchen,
13. Tests Adapter-, Decorator-, Proxy- und Facade-Verhalten abdecken,
14. keine Pattern-Schicht ohne konkretes Strukturproblem eingeführt wurde.

---

## 24. Quellen und weiterführende Literatur

- Refactoring.Guru — Structural Design Patterns: https://refactoring.guru/design-patterns/structural-patterns
- Refactoring.Guru — Adapter: https://refactoring.guru/design-patterns/adapter
- Refactoring.Guru — Decorator: https://refactoring.guru/design-patterns/decorator
- Refactoring.Guru — Composite: https://refactoring.guru/design-patterns/composite
- Refactoring.Guru — Proxy: https://refactoring.guru/design-patterns/proxy
- Refactoring.Guru — Facade: https://refactoring.guru/design-patterns/facade
- Refactoring.Guru — Bridge: https://refactoring.guru/design-patterns/bridge
- Refactoring.Guru — Flyweight: https://refactoring.guru/design-patterns/flyweight
- Spring Framework Reference — Proxying Mechanisms: https://docs.spring.io/spring-framework/reference/core/aop/proxying.html
- Spring Framework Reference — Declarative Transaction Management / `@Transactional`: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- Spring Framework Reference — Cache Abstraction: https://docs.spring.io/spring-framework/reference/integration/cache.html
- Oracle Java SE 21 API — `java.lang.Record`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html
- Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides — Design Patterns: Elements of Reusable Object-Oriented Software
- Martin Fowler — Patterns of Enterprise Application Architecture
