# QG-JAVA-023 — Domain-Driven Design: Aggregate, Bounded Context und Ubiquitous Language

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-023 |
| Titel | Domain-Driven Design: Aggregate, Bounded Context und Ubiquitous Language |
| Status | Accepted / verbindlicher Standard für domänenkritische Java-Systeme |
| Version | 1.0.0 |
| Datum | 2026-02-21 |
| Kategorie | Architektur / Domain Design / Modellierung |
| Zielgruppe | Java-Entwickler, Tech Leads, Architektur, Product Owner, QA, Security, Plattform-Team |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Data JPA 3.x, optional Spring Modulith, Kafka/Spring Events bei Integrationsereignissen |
| Geltungsbereich | Domänenmodellierung, Aggregate, Value Objects, Entities, Bounded Contexts, Domain Events, Context Mapping, Repository-Schnittstellen, Service-Grenzen und SaaS-/Tenant-Modellierung |
| Verbindlichkeit | DDD-Muster werden dort eingesetzt, wo fachliche Komplexität, langlebige Geschäftsregeln oder teamübergreifende Modellgrenzen bestehen. Für einfache CRUD-Anwendungen ist vollständiges DDD nicht verpflichtend und kann Overengineering sein. |
| Technische Validierung | Gegen Eric Evans DDD-Referenz, Fowler Bounded Context, Microsoft Domain Analysis/Tactical DDD und Spring Data Domain Events eingeordnet |
| Kurzentscheidung | Fachlich komplexe Software wird nicht primär um Tabellen, Controller oder technische Services modelliert, sondern um explizite Domänenbegriffe, Bounded Contexts, Aggregate, Value Objects und klar geschützte Invarianten. |

---

## 1. Zweck

Diese Richtlinie beschreibt, wie Domain-Driven Design in Java- und Spring-Boot-Systemen pragmatisch angewendet wird.

DDD ist kein dekoratives Architekturwort und kein Grund, für jedes CRUD-Problem eine komplexe Modellierungslandschaft zu bauen. DDD ist ein Werkzeugkasten für fachlich anspruchsvolle Domänen, in denen Begriffe, Regeln, Zustände, Verantwortlichkeiten und Grenzen sauber verstanden und im Code sichtbar gemacht werden müssen.

Der Zweck dieser Guideline ist:

- eine gemeinsame fachliche Sprache zwischen Produkt, Fachbereich und Entwicklung zu erzwingen,
- fachliche Regeln an den richtigen Ort im Code zu bringen,
- Konsistenzgrenzen über Aggregate zu schützen,
- teamübergreifende Modellgrenzen über Bounded Contexts sichtbar zu machen,
- technische Kopplung zwischen Kontexten zu reduzieren,
- Datenbanktabellen nicht automatisch mit Domänenmodellen zu verwechseln,
- SaaS-/Tenant-Regeln explizit zu modellieren,
- Domain Events kontrolliert einzusetzen,
- Overengineering durch falsches oder zu frühes DDD zu vermeiden.

DDD ist erfolgreich, wenn Entwickler und Domänenexperten dieselben Begriffe verwenden und der Code diese Begriffe wiedererkennbar ausdrückt.

---

## 2. Kurzregel

Verwende DDD, wenn fachliche Komplexität vorliegt. Benenne Code nach der Sprache der Domäne. Schütze Invarianten innerhalb von Aggregaten. Greife auf interne Aggregate-Objekte nicht direkt zu. Ziehe Bounded Contexts, wenn derselbe Begriff in verschiedenen fachlichen Bereichen unterschiedliche Bedeutung hat. Nutze Value Objects für fachlich bedeutsame Werte. Nutze Domain Events für entkoppelte Reaktionen, aber nicht als Ersatz für fehlende Modellgrenzen. Führe DDD nicht ein, wenn ein einfacher CRUD-Service dadurch nur komplizierter wird.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- fachliche Kernlogik,
- Aggregate,
- Entities,
- Value Objects,
- Domain Services,
- Application Services,
- Use Cases,
- Repository-Abstraktionen,
- Bounded Contexts,
- Context Mapping,
- Domain Events,
- Integration Events,
- Anti-Corruption Layer,
- SaaS-/Tenant-Domänenmodelle,
- Bestellung, Zahlung, Abrechnung, Kampagnen, Nutzer, Berechtigungen, Inventar, Subscription, Offers oder vergleichbare fachliche Subdomänen.

Diese Richtlinie gilt nicht als Pflicht für:

- reine Admin-CRUD-Masken,
- einfache Lookup-Tabellen,
- technische Konfigurationsendpunkte,
- Datenimport-Skripte ohne relevante Fachlogik,
- einfache Reporting-Projektionen,
- DTO-only Adapter ohne Domänenentscheidung.

---

## 4. Technischer Hintergrund

Domain-Driven Design wurde durch Eric Evans geprägt. In seiner DDD-Referenz fasst Evans DDD als Ansatz für komplexe Software zusammen: Fokus auf die Core Domain, kreatives Zusammenarbeiten von Domänen- und Softwarepraktikern sowie eine Ubiquitous Language innerhalb eines expliziten Bounded Contexts.

Martin Fowler beschreibt den Bounded Context als zentrales Muster im strategischen Design von DDD. Große Domänen werden in unterschiedliche Bounded Contexts geteilt, und die Beziehungen zwischen diesen Kontexten werden explizit gemacht.

Microsofts Architekturleitfäden ordnen DDD ebenfalls als Methode zur Modellierung von Microservice-Grenzen ein. Dort wird empfohlen, Bounded Contexts zu definieren und innerhalb eines Kontextes taktische DDD-Muster wie Entities, Aggregates und Domain Services anzuwenden. Als praktische Faustregel für Microservices wird formuliert: Ein Microservice sollte nicht kleiner als ein Aggregate und nicht größer als ein Bounded Context sein.

Spring Data unterstützt Domain Events direkt über `@DomainEvents` und `@AfterDomainEventPublication` an Aggregate Roots. Das ist hilfreich, muss aber bewusst eingesetzt werden: Solche Events sind Teil des Repository-/Spring-Data-Lebenszyklus und ersetzen kein sauberes Integrations- oder Outbox-Konzept für serviceübergreifende Events.

---

## 5. Wann DDD sinnvoll ist

DDD lohnt sich besonders, wenn mehrere dieser Kriterien zutreffen:

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Fachliche Regeln | Viele Geschäftsregeln bestimmen gültige Zustände. | Bestellung darf nur vor Versand storniert werden. | DDD sinnvoll |
| Mehrdeutige Begriffe | Derselbe Begriff bedeutet in verschiedenen Bereichen anderes. | „Kunde“ in Identity, Billing und Support. | Bounded Context nötig |
| Langlebige Domäne | Geschäftsregeln entwickeln sich über Jahre. | Subscription, Payment, Campaigns. | DDD sinnvoll |
| Hohe Änderungskosten | Änderungen erzeugen oft Seiteneffekte. | Preislogik, Rabatte, Steuern. | DDD sinnvoll |
| Teamübergreifende Arbeit | Mehrere Teams arbeiten am gleichen fachlichen Raum. | Ordering, Inventory, Notification. | Context Mapping nötig |
| Konsistenzregeln | Mehrere Objekte müssen gemeinsam gültig bleiben. | Order und OrderItems. | Aggregate nötig |
| SaaS-/Tenant-Regeln | Daten und Regeln sind tenantgebunden. | Händler, Standort, Kampagnen. | explizites Modell nötig |
| Nur CRUD | Daten werden gespeichert, gelesen, geändert, gelöscht. | Admin-Lookup für Länder. | vollständiges DDD meist unnötig |
| Nur technischer Adapter | Mapping von HTTP zu DTO. | Health Endpoint. | DDD unnötig |

---

## 6. Ubiquitous Language

### 6.1 Bedeutung

Ubiquitous Language bedeutet: Fachbereich, Product Owner, Entwickler, QA, Architektur und Code verwenden dieselben Begriffe für dieselben Konzepte.

Es reicht nicht, wenn Begriffe in Meetings verwendet werden. Die Sprache muss im Code sichtbar sein:

- Klassennamen,
- Methodennamen,
- Eventnamen,
- Testnamen,
- Package-Namen,
- Datenbankmigrationen,
- API-Felder,
- Fehlermeldungen,
- Dokumentation,
- Acceptance Criteria.

### 6.2 Schlechte Anwendung

```java
public class DataRecord {

    private String str1;
    private String str2;
    private int num1;
    private boolean flag;
}
```

```java
public class UserDataProcessor {

    public void processUserData(DataRecord record) {
        // ...
    }
}
```

Probleme:

- `DataRecord` beschreibt nichts Fachliches.
- `str1`, `num1` und `flag` verstecken Bedeutung.
- `Processor` ist ein technischer Sammelbegriff.
- Der Code kann nicht mit dem Fachbereich besprochen werden.
- Tests werden ebenfalls unklar.

### 6.3 Gute Anwendung

```java
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private OrderStatus status;

    public void place() { /* ... */ }

    public void cancel() { /* ... */ }

    public void confirmPayment(PaymentConfirmation confirmation) { /* ... */ }

    public void markAsShipped(ShipmentId shipmentId) { /* ... */ }
}
```

Der Code spricht die Sprache der Domäne:

- Bestellung,
- Artikel,
- Kunde,
- Zahlung,
- Versand,
- Status,
- Stornierung.

### 6.4 Review-Regel

Wenn ein Entwickler eine Klasse oder Methode nicht mit fachlichen Begriffen erklären kann, ist das ein Modellierungsproblem.

Schlechter Review-Kommentar:

```text
Der Name gefällt mir nicht.
```

Guter Review-Kommentar:

```text
[major] `UserDataProcessor` beschreibt keine Domänenverantwortung.
Im Ticket und in den Akzeptanzkriterien geht es um die Aktivierung eines Händlerkontos.
Bitte einen fachlichen Namen wie `MerchantAccountActivationService` prüfen.
```

---

## 7. Entity

### 7.1 Bedeutung

Eine Entity besitzt Identität über Zeit. Zwei Entities können gleiche Attribute haben und trotzdem unterschiedlich sein, weil ihre Identität zählt.

Beispiel:

```java
public class Merchant {

    private final MerchantId id;
    private MerchantStatus status;
    private CompanyName companyName;

    public Merchant(MerchantId id, CompanyName companyName) {
        this.id = Objects.requireNonNull(id);
        this.companyName = Objects.requireNonNull(companyName);
        this.status = MerchantStatus.PENDING_VERIFICATION;
    }

    public void verify() {
        if (status != MerchantStatus.PENDING_VERIFICATION) {
            throw new MerchantVerificationException(id, status);
        }
        this.status = MerchantStatus.VERIFIED;
    }

    public MerchantId id() {
        return id;
    }
}
```

### 7.2 Entity-Regeln

| Regel | Details/Erklärung |
|---|---|
| Identität zählt | Entity wird über ID verglichen, nicht über alle Felder. |
| Verhalten gehört zur Entity | Zustandsübergänge nicht nur per Setter. |
| Keine öffentlichen Setter für kritische Zustände | Fachliche Methoden verwenden. |
| Invarianten schützen | Entity verhindert ungültigen Zustand. |
| Persistenz nicht mit Fachmodell verwechseln | JPA-Entity und Domain-Entity können getrennt sein. |

---

## 8. Value Object

### 8.1 Bedeutung

Ein Value Object beschreibt einen fachlichen Wert ohne eigene Identität. Es wird über seine Werte verglichen und ist bevorzugt unveränderlich.

Beispiele:

- `Money`,
- `EmailAddress`,
- `Quantity`,
- `TenantId`,
- `OrderId`,
- `Address`,
- `DateRange`,
- `CampaignPeriod`,
- `Percentage`,
- `PhoneNumber`.

### 8.2 Schlechte Anwendung

```java
void createCampaign(
        Long merchantId,
        String name,
        BigDecimal discount,
        String currency,
        LocalDate startDate,
        LocalDate endDate) {
    // ...
}
```

Probleme:

- `discount` kann negativ sein.
- `currency` kann ungültig sein.
- Start-/Enddatum können vertauscht sein.
- `merchantId` kann mit anderer ID verwechselt werden.
- Regeln sind außerhalb der Werte verteilt.

### 8.3 Gute Anwendung

```java
public record CampaignPeriod(LocalDate startDate, LocalDate endDate) {

    public CampaignPeriod {
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);

        if (endDate.isBefore(startDate)) {
            throw new InvalidCampaignPeriodException(startDate, endDate);
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
```

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);

        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new InvalidMoneyScaleException(amount, currency);
        }
        if (amount.signum() < 0) {
            throw new NegativeMoneyAmountException(amount);
        }
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("factor must not be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }
}
```

### 8.4 Java-21-Empfehlung

Records sind sehr gut für Value Objects geeignet, wenn sie unveränderlich sind und ihre Invarianten im kompakten Konstruktor schützen.

```java
public record Quantity(int value) {

    public Quantity {
        if (value <= 0) {
            throw new InvalidQuantityException(value);
        }
    }
}
```

---

## 9. Aggregate

### 9.1 Bedeutung

Ein Aggregate ist eine Konsistenzgrenze. Es gruppiert Entities und Value Objects, die gemeinsam gültig bleiben müssen. Das Aggregate Root ist der einzige Einstiegspunkt für Änderungen am Aggregate.

Ein Aggregate beantwortet:

```text
Welche Objekte müssen innerhalb einer Transaktion konsistent bleiben?
Welches Objekt kontrolliert Änderungen?
Welche Invarianten dürfen nie verletzt werden?
Was darf von außen referenziert werden?
```

### 9.2 Grundregeln

| Regel | Details/Erklärung |
|---|---|
| Ein Aggregate Root | Nur das Root wird von außen geladen und gespeichert. |
| Interne Objekte kapseln | Keine direkte Mutation interner Listen oder Entities. |
| Repository nur für Root | Keine Repositories für interne Aggregate-Objekte. |
| Invarianten im Root | Root schützt Konsistenzregeln. |
| Referenzen über IDs | Andere Aggregate werden über ID referenziert, nicht als Objektgraph. |
| Kleine Aggregate bevorzugen | Große Aggregate erzeugen Locking- und Performance-Probleme. |
| Transaktion pro Aggregate | Keine verteilten Transaktionen als Standard. |

### 9.3 Schlechte Anwendung

```java
@Service
public class OrderItemService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public void addItem(OrderId orderId, ProductId productId, int quantity) {
        var order = orderRepository.findById(orderId).orElseThrow();
        var item = new OrderItem(productId, quantity);

        order.items().add(item);

        orderItemRepository.save(item);
    }
}
```

Probleme:

- Interne Liste wird direkt verändert.
- `Order` kann ihre Invarianten nicht schützen.
- `OrderItemRepository` deutet auf falsche Aggregate-Grenze hin.
- Bestellung könnte bereits bestätigt, versendet oder storniert sein.
- Maximalmengen oder Preisregeln werden umgangen.

### 9.4 Gute Anwendung

```java
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(OrderId id, CustomerId customerId) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
    }

    public void addItem(ProductSnapshot product, Quantity quantity) {
        assertModifiable();

        if (items.size() >= 10) {
            throw new OrderItemLimitExceededException(id, 10);
        }

        items.add(OrderItem.from(product, quantity));
    }

    public void place() {
        assertModifiable();

        if (items.isEmpty()) {
            throw new EmptyOrderCannotBePlacedException(id);
        }

        this.status = OrderStatus.PLACED;
        registerEvent(new OrderPlaced(id, customerId, total()));
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    private void assertModifiable() {
        if (status != OrderStatus.DRAFT) {
            throw new OrderModificationException(id, status);
        }
    }

    public Money total() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(Currency.getInstance("EUR")), Money::add);
    }
}
```

Repository nur für das Root:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
```

Keine direkte Speicherung von `OrderItem`.

---

## 10. Aggregate-Größe

### 10.1 Zu großes Aggregate

Schlecht:

```java
public class Customer {

    private List<Order> orders;
    private List<Invoice> invoices;
    private List<PaymentMethod> paymentMethods;
    private List<SupportTicket> tickets;
    private List<LoginSession> sessions;
}
```

Probleme:

- Jede Änderung lädt zu viel.
- Locking wird teuer.
- Verschiedene Konsistenzregeln werden vermischt.
- Teams können schwer unabhängig arbeiten.
- Transaktionen werden unnötig breit.

### 10.2 Besser: mehrere Aggregate mit ID-Referenzen

```java
public class CustomerAccount {
    private CustomerId id;
    private EmailAddress email;
    private AccountStatus status;
}
```

```java
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
}
```

```java
public class Invoice {
    private InvoiceId id;
    private CustomerId customerId;
    private OrderId orderId;
}
```

Regel:

```text
Wenn mehrere Dinge nicht zwingend in derselben Transaktion konsistent sein müssen,
gehören sie wahrscheinlich nicht in dasselbe Aggregate.
```

---

## 11. Bounded Context

### 11.1 Bedeutung

Ein Bounded Context ist eine Grenze, innerhalb der ein Modell gültig ist. Derselbe Begriff kann in unterschiedlichen Kontexten bewusst unterschiedliche Bedeutung haben.

Beispiel „Kunde“:

| Kontext | Begriff | Bedeutung |
|---|---|---|
| Identity | UserAccount | Login, Passwort, MFA, Rollen |
| Ordering | CustomerId | Käuferreferenz für Bestellung |
| Billing | BillingCustomer | Rechnungsempfänger, Steuernummer, Zahlungsdaten |
| Support | CustomerProfile | Kontakt- und Supporthistorie |
| Marketing | AudienceMember | Segmentierung, Kampagnenpräferenzen |

Ein einheitliches riesiges `User`-Objekt für alle Kontexte ist fast immer ein Modellierungsfehler.

### 11.2 Schlechte Anwendung

```java
@Entity
public class User {

    private String email;
    private String passwordHash;
    private String mfaSecret;
    private List<Role> roles;

    private String displayName;
    private String avatarUrl;
    private String bio;

    private String stripeCustomerId;
    private SubscriptionTier subscriptionTier;
    private LocalDate subscriptionExpiry;

    private boolean emailNotifications;
    private boolean smsNotifications;
    private String phoneNumber;

    private List<Order> orders;
    private List<Invoice> invoices;
}
```

Probleme:

- Identity, Profil, Billing, Notification und Ordering werden vermischt.
- Jede Änderung am Nutzer betrifft viele Teams.
- Sicherheitsdaten liegen neben Anzeige- und Billingdaten.
- Performance und Datenschutz werden schwer kontrollierbar.
- Modell ist nicht fachlich präzise.

### 11.3 Gute Anwendung

```java
package com.example.identity;

public class UserAccount {
    private UserId id;
    private EmailAddress email;
    private PasswordHash passwordHash;
    private MfaStatus mfaStatus;
    private Set<Role> roles;
}
```

```java
package com.example.ordering;

public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
}
```

```java
package com.example.billing;

public class BillingCustomer {
    private CustomerId customerId;
    private BillingAddress billingAddress;
    private TaxNumber taxNumber;
    private PaymentProviderCustomerId providerCustomerId;
}
```

```java
package com.example.notification;

public record NotificationPreferences(
        UserId userId,
        boolean emailEnabled,
        boolean smsEnabled,
        PhoneNumber phoneNumber
) {}
```

---

## 12. Context Mapping

Bounded Contexts existieren nicht isoliert. Sie haben Beziehungen. Diese Beziehungen müssen explizit sein.

| Beziehung | Details/Erklärung | Beispiel |
|---|---|---|
| Customer/Supplier | Ein Context liefert Modell oder API, anderer konsumiert. | Identity liefert User-Referenz. |
| Conformist | Ein Context übernimmt Modell des anderen. | Reporting übernimmt Event-Schema. |
| Anti-Corruption Layer | Übersetzt fremdes Modell in eigenes Modell. | Billing übersetzt Payment-Provider-Modell. |
| Shared Kernel | Kleiner geteilter Kern zwischen Teams. | `Money`, `Currency`, `TenantId` |
| Published Language | Gemeinsame öffentliche Sprache über Events/APIs. | `OrderPlacedEvent` |
| Open Host Service | Ein Context bietet stabile API. | Customer API |
| Separate Ways | Keine Integration, Modelle getrennt. | unabhängige Admin-Domäne |

### 12.1 Anti-Corruption Layer

Schlecht:

```java
public class BillingService {

    public void createInvoice(StripeCustomer stripeCustomer) {
        // Stripe-Modell sickert in Billing-Domäne.
    }
}
```

Besser:

```java
public class StripeCustomerAdapter {

    public BillingCustomerReference toBillingCustomer(StripeCustomer stripeCustomer) {
        return new BillingCustomerReference(
                new PaymentProviderCustomerId(stripeCustomer.getId()),
                new EmailAddress(stripeCustomer.getEmail())
        );
    }
}
```

```java
public class BillingService {

    public void createInvoice(BillingCustomerReference customer) {
        // ...
    }
}
```

---

## 13. Domain Service

### 13.1 Bedeutung

Ein Domain Service enthält fachliche Logik, die nicht natürlich zu einer Entity oder einem Value Object gehört.

Ein Domain Service ist nicht einfach ein Spring `@Service`. Er ist fachlich motiviert.

Gute Kandidaten:

- Berechnung, die mehrere Aggregate betrifft,
- fachliche Policy,
- Preis-/Steuerregel, die nicht in ein Objekt passt,
- Validierung über mehrere Quellen,
- Domain-spezifische Entscheidung.

### 13.2 Schlechte Anwendung

```java
@Service
public class OrderDomainService {

    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
    }
}
```

Das Verhalten gehört zur `Order`.

### 13.3 Gute Anwendung

```java
public class CampaignEligibilityPolicy {

    public EligibilityResult evaluate(
            Merchant merchant,
            Campaign campaign,
            CustomerSegment customerSegment,
            LocalDate today) {

        if (!merchant.isVerified()) {
            return EligibilityResult.rejected("merchant is not verified");
        }

        if (!campaign.period().contains(today)) {
            return EligibilityResult.rejected("campaign is not active");
        }

        if (!campaign.targets(customerSegment)) {
            return EligibilityResult.rejected("customer segment not eligible");
        }

        return EligibilityResult.accepted();
    }
}
```

Dieser Service formuliert eine fachliche Policy, die mehrere Konzepte zusammen bewertet.

---

## 14. Application Service vs. Domain Service

### 14.1 Application Service

Ein Application Service orchestriert einen Use Case:

- Transaktion,
- Laden von Aggregaten,
- Aufruf von Domain-Methoden,
- Speichern,
- Publizieren von Events,
- Aufruf von Ports.

```java
@Service
public class PlaceOrderApplicationService {

    private final OrderRepository orderRepository;
    private final ProductCatalogPort productCatalogPort;

    @Transactional
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        var order = new Order(OrderId.newId(), command.customerId());

        for (var item : command.items()) {
            var product = productCatalogPort.snapshotOf(item.productId());
            order.addItem(product, item.quantity());
        }

        order.place();
        orderRepository.save(order);

        return new PlaceOrderResult(order.id(), order.total());
    }
}
```

### 14.2 Domain Service

Ein Domain Service enthält fachliche Logik ohne technische Orchestrierung.

```java
public class DiscountPolicy {

    public Money calculateDiscount(CustomerSegment segment, Money subtotal) {
        // ...
    }
}
```

### 14.3 Regel

```text
Application Service koordiniert.
Domain Entity und Domain Service entscheiden.
Repository persistiert.
Adapter übersetzen.
```

---

## 15. Repository

### 15.1 DDD-Bedeutung

Ein Repository stellt eine Sammlung von Aggregate Roots dar. Es ist keine generische DAO-Schicht für jedes Objekt.

Schlecht:

```java
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
}
```

Wenn `OrderItem` nur innerhalb von `Order` existiert, braucht es kein eigenes Repository.

Gut:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
```

### 15.2 Spring Data JPA Umsetzung

Bei einfacher Architektur kann das Spring-Data-Repository direkt das Aggregate Root speichern:

```java
public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
}
```

Bei hexagonaler Architektur wird ein Adapter empfohlen:

```java
@Repository
class OrderPersistenceAdapter implements OrderRepository {

    private final JpaOrderRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public void save(Order order) {
        jpaRepository.save(mapper.toEntity(order));
    }
}
```

---

## 16. Domain Events

### 16.1 Bedeutung

Ein Domain Event beschreibt eine fachliche Tatsache, die bereits eingetreten ist.

Beispiele:

```text
OrderPlaced
PaymentCaptured
CampaignActivated
MerchantVerified
OfferRedeemed
CustomerDeleted
```

Eventnamen stehen bevorzugt in der Vergangenheitsform. Ein Event ist keine Aufforderung.

### 16.2 Schlechte Anwendung: direkter Kontextzugriff

```java
@Service
public class OrderService {

    public void placeOrder(PlaceOrderCommand command) {
        var order = createOrder(command);
        orderRepository.save(order);

        notificationService.sendOrderConfirmation(order.customerId());
        analyticsService.trackOrder(order);
        inventoryService.reserve(order.items());
    }
}
```

Probleme:

- Ordering kennt Notification, Analytics und Inventory.
- Neue Reaktion erfordert Änderung am Order-Service.
- Fehler in Notification kann Order-Flow beeinflussen.
- Testsetup wächst stark.

### 16.3 Gute Anwendung: Domain Event

```java
public class Order {

    private final List<Object> domainEvents = new ArrayList<>();

    public void place() {
        if (items.isEmpty()) {
            throw new EmptyOrderCannotBePlacedException(id);
        }

        this.status = OrderStatus.PLACED;
        domainEvents.add(new OrderPlaced(id, customerId, total()));
    }

    public List<Object> domainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

Spring-Data-Variante:

```java
@Entity
public class OrderEntity {

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    @DomainEvents
    Collection<Object> domainEvents() {
        return List.copyOf(domainEvents);
    }

    @AfterDomainEventPublication
    void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

### 16.4 Wichtige Einschränkung

Spring Data Domain Events sind gut für lokale In-Process-Reaktionen. Für serviceübergreifende Integration über Kafka oder andere Broker reicht das allein nicht aus, wenn DB-Commit und Event-Publikation konsistent sein müssen. Dafür ist ein Outbox-Pattern oder eine gleichwertige Strategie erforderlich.

---

## 17. Integration Event vs. Domain Event

| Aspekt | Domain Event | Integration Event |
|---|---|---|
| Zweck | Fachliche Tatsache innerhalb der Domäne | Vertrag zwischen Systemen/Kontexten |
| Modell | Darf internes Domänenmodell kennen | Muss stabil und minimal sein |
| Lebensdauer | intern, refactorbar | langlebiger Vertrag |
| Transport | In-Process oder intern | Kafka, REST, Event Bus |
| Versionierung | weniger streng | zwingend |
| Datenschutz | trotzdem beachten | besonders streng |
| Beispiel | `OrderPlaced` im Ordering-Modul | `orders.placed.v1` Avro Event |

Regel:

```text
Nicht jedes Domain Event wird automatisch als Integration Event veröffentlicht.
```

Zwischen Domain Event und Integration Event liegt ein Mapping.

---

## 18. SaaS- und Tenant-Modellierung

In SaaS-Systemen ist Tenant-Kontext Teil der Domäne, nicht nur ein HTTP-Header.

### 18.1 Schlechte Anwendung

```java
public class Campaign {

    private Long id;
    private Long merchantId;
    private String name;
}
```

Queries:

```java
Optional<Campaign> findById(Long id);
```

Problem: Cross-Tenant-Zugriff ist leicht möglich.

### 18.2 Gute Anwendung

```java
public record TenantId(String value) {
    public TenantId {
        if (value == null || value.isBlank()) {
            throw new InvalidTenantIdException(value);
        }
    }
}
```

```java
public class Campaign {

    private final CampaignId id;
    private final TenantId tenantId;
    private final MerchantId merchantId;
    private CampaignStatus status;

    public void activate(AccessScope accessScope) {
        if (!accessScope.tenantId().equals(tenantId)) {
            throw new TenantAccessViolationException(accessScope.tenantId(), tenantId);
        }

        if (status != CampaignStatus.DRAFT) {
            throw new CampaignActivationException(id, status);
        }

        this.status = CampaignStatus.ACTIVE;
    }
}
```

Repository:

```java
Optional<Campaign> findByIdAndTenantId(CampaignId campaignId, TenantId tenantId);
```

### 18.3 Regeln

- TenantId ist ein Value Object.
- Tenant-Kontext wird nicht als freier String behandelt.
- tenantgebundene Aggregate enthalten TenantId oder sind eindeutig tenantgebunden.
- Repositories für tenantgebundene Daten filtern tenantbewusst.
- Tests prüfen Cross-Tenant-Zugriff.
- Events enthalten Tenant-Kontext, wenn fachlich relevant.
- Logs enthalten keine personenbezogenen Tenantdetails, aber technische TenantId kann für Betrieb zulässig sein.

---

## 19. DDD und JPA

### 19.1 Grundproblem

DDD und JPA passen zusammen, aber nicht automatisch. JPA bevorzugt persistierbare Objektgraphen. DDD bevorzugt fachlich gekapselte Modelle. Beides muss bewusst integriert werden.

### 19.2 Praktische Regeln

| Regel | Details/Erklärung |
|---|---|
| Keine öffentlichen Setter für Invarianten | JPA kann protected/private Konstruktoren nutzen. |
| Collections kapseln | `List.copyOf` nach außen. |
| `@OneToMany` nur innerhalb Aggregate bewusst nutzen | Keine beliebigen großen Objektgraphen. |
| `@ManyToOne(fetch = LAZY)` explizit setzen | EAGER vermeiden. |
| IDs anderer Aggregate statt Objektgraph | Kopplung reduzieren. |
| Mapping trennen, wenn Domain sauber bleiben muss | Domain-Modell + Persistence-Modell. |
| Keine Domainlogik in Repository | Repository lädt und speichert, entscheidet nicht. |
| Keine JPA-Entity direkt als API Response | DTO/Response verwenden. |

### 19.3 Zwei zulässige Varianten

#### Variante A: JPA-Entity als Aggregate Root

Gut für moderate Komplexität:

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected Order() {
        // JPA
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new OrderCannotBeCancelledException(id);
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

#### Variante B: getrenntes Domain- und Persistence-Modell

Gut für komplexe Domäne oder hexagonale Architektur:

```java
public class Order {
    private OrderId id;
    private List<OrderItem> items;
    private OrderStatus status;
}
```

```java
@Entity
@Table(name = "orders")
class OrderJpaEntity {
    @Id
    private UUID id;
    // ...
}
```

```java
class OrderMapper {
    Order toDomain(OrderJpaEntity entity) { /* ... */ }
    OrderJpaEntity toEntity(Order domain) { /* ... */ }
}
```

Variante B erzeugt mehr Mapping-Aufwand, schützt aber die Domäne stärker.

---

## 20. DDD und Microservices

DDD hilft bei Microservice-Grenzen, ist aber kein Automatismus.

Regeln:

- Ein Bounded Context ist ein Kandidat für einen Service, nicht zwingend genau ein Service.
- Ein Microservice sollte nicht kleiner als ein Aggregate sein.
- Ein Microservice sollte nicht mehrere unzusammenhängende Bounded Contexts vermischen.
- Servicegrenzen sollen fachlich, nicht nur technisch gezogen werden.
- Datenbanktabellen sind kein guter primärer Service-Schnitt.
- Ein gemeinsames Datenmodell über Services hinweg erzeugt Kopplung.
- Integration läuft über APIs, Events oder Anti-Corruption Layer, nicht über fremde Tabellen.

### 20.1 Schlechte Anwendung

```text
user-service enthält:
- Login
- Profil
- Rechnungsadresse
- Subscription
- Notification Preferences
- Supportdaten
- Kampagnenzielgruppen
```

Das ist kein Bounded Context, sondern ein Sammelservice.

### 20.2 Gute Anwendung

```text
identity-service
- UserAccount
- Credential
- Role
- MFA

billing-service
- BillingCustomer
- Invoice
- Subscription
- PaymentMethod

notification-service
- NotificationPreferences
- NotificationTemplate
- DeliveryAttempt
```

---

## 21. Event Storming

Event Storming ist ein Workshop-Format, um gemeinsam mit Domänenexperten fachliche Ereignisse, Commands, Akteure, Policies, Aggregate und Bounded Contexts sichtbar zu machen.

Typischer Ablauf:

1. Domain Events sammeln.
2. Ereignisse zeitlich sortieren.
3. Commands identifizieren.
4. Akteure/Systeme markieren.
5. Policies und Regeln ergänzen.
6. Pain Points markieren.
7. Aggregate-Kandidaten erkennen.
8. Bounded Contexts gruppieren.
9. Integrationsereignisse und APIs ableiten.
10. Risiken und offene Fragen dokumentieren.

Beispiel für Yulento-ähnliche Domäne:

```text
MerchantRegistered
LocationVerified
OfferCreated
CampaignScheduled
CustomerSubscribedToChannel
DealReserved
QrCodeGenerated
DealRedeemed
MerchantPaid
ReviewSubmitted
```

Aus diesen Events entstehen mögliche Aggregates und Contexts.

---

## 22. Gute Anwendung: Campaign Aggregate

```java
public class Campaign {

    private final CampaignId id;
    private final TenantId tenantId;
    private final MerchantId merchantId;
    private final CampaignPeriod period;
    private final List<CampaignVariant> variants = new ArrayList<>();

    private CampaignStatus status = CampaignStatus.DRAFT;

    public void addVariant(CampaignVariant variant) {
        assertDraft();
        variants.add(variant);
    }

    public void schedule() {
        assertDraft();

        if (variants.isEmpty()) {
            throw new CampaignWithoutVariantsException(id);
        }

        this.status = CampaignStatus.SCHEDULED;
        registerEvent(new CampaignScheduled(id, tenantId, merchantId, period));
    }

    public void activate(LocalDate today) {
        if (status != CampaignStatus.SCHEDULED) {
            throw new CampaignActivationException(id, status);
        }

        if (!period.contains(today)) {
            throw new CampaignOutsidePeriodException(id, period, today);
        }

        this.status = CampaignStatus.ACTIVE;
    }

    private void assertDraft() {
        if (status != CampaignStatus.DRAFT) {
            throw new CampaignModificationException(id, status);
        }
    }
}
```

Hier werden fachliche Regeln sichtbar:

- Varianten nur im Draft,
- Scheduling nur mit Varianten,
- Aktivierung nur im Zeitraum,
- Tenant und Merchant sind Teil des Aggregates,
- Events entstehen bei fachlich relevanten Zustandsübergängen.

---

## 23. Anti-Patterns

### 23.1 Big Ball of Mud

Alles hängt mit allem zusammen. Keine klaren Grenzen, keine klare Sprache, keine Invarianten.

### 23.2 Entity-per-Table als Domänenmodell

Datenbanktabellen werden 1:1 als Domäne interpretiert. Das führt oft zu anämischen Modellen.

### 23.3 Repository für jedes Objekt

Wenn jedes interne Objekt ein Repository bekommt, existiert keine echte Aggregate-Grenze.

### 23.4 Setter-basierte Fachlogik

```java
order.setStatus(CANCELLED);
```

statt:

```java
order.cancel();
```

### 23.5 Ein globales User-Modell

Ein `User` für Identity, Billing, Support, Marketing und Notification ist fast immer falsch.

### 23.6 Domain Events als versteckte Methodenaufrufe

Wenn Events nur benutzt werden, um direkte Aufrufe zu verstecken, aber dieselbe Kopplung bleibt, wurde nichts gewonnen.

### 23.7 DDD für CRUD erzwingen

Wenn eine einfache Admin-Tabelle durch Aggregate, Domain Events und Policies aufgeblasen wird, ist das Overengineering.

### 23.8 Fremdes Modell übernehmen

Wenn ein Payment Provider, ERP oder Identity Provider das interne Domänenmodell bestimmt, fehlt ein Anti-Corruption Layer.

### 23.9 Tenant als String überall

Mandantenfähigkeit darf nicht in freien Strings und losen Headern zerfallen.

### 23.10 Aggregate zu groß schneiden

Ein Aggregate, das halbe Geschäftsbereiche lädt und sperrt, wird zum Performance- und Locking-Problem.

---

## 24. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Ubiquitous Language | Entsprechen Namen der Fachsprache? | `Order`, `Campaign`, `Offer` | Pflicht |
| Bounded Context | Ist klar, in welchem Kontext das Modell gilt? | Ordering vs. Billing | Pflicht bei komplexer Domäne |
| Aggregate Root | Gibt es einen kontrollierten Einstiegspunkt? | `Order.addItem()` | Pflicht |
| Interne Objekte | Werden interne Objekte nicht direkt verändert? | keine `items().add()` | Pflicht |
| Repository | Gibt es Repository nur für Aggregate Root? | kein `OrderItemRepository` | Pflicht |
| Invarianten | Schützt das Modell seine Regeln selbst? | `order.cancel()` prüft Status | Pflicht |
| Value Objects | Sind fachliche Werte typisiert? | `Money`, `TenantId`, `Quantity` | Pflicht |
| Kontextgrenzen | Werden fremde Modelle übersetzt? | ACL | Pflicht bei Integration |
| Domain Events | Beschreiben Events fachliche Tatsachen? | `OrderPlaced` | Pflicht bei Events |
| Integration Events | Sind externe Events stabil versioniert? | `orders.placed.v1` | Pflicht bei Kafka/API |
| JPA | Verletzt Persistenz nicht die Domäne? | keine öffentlichen Setter für Status | Prüfen |
| SaaS | Ist Tenant-Kontext explizit? | `TenantId` im Aggregate | Pflicht |
| Tests | Werden Invarianten getestet? | Statusübergänge | Pflicht |
| Overengineering | Ist DDD hier angemessen? | kein DDD für triviales CRUD | Prüfen |

---

## 25. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Controller dürfen nicht direkt auf Repositories zugreifen.
- API darf keine JPA-Entities zurückgeben.
- Domain-Paket darf nicht von Web-Paket abhängen.
- Domain-Paket darf nicht von Kafka-/HTTP-Client-Paketen abhängen.
- Interne Aggregate-Objekte haben keine eigenen Repositories.
- Tenantgebundene Repository-Methoden müssen TenantId oder AccessScope verwenden.
- Klassen mit Namen *Manager oder *Processor werden im Review markiert.
- Public Setter auf Domain-Entities werden markiert.
- Events müssen auf *ed oder fachlich vergangene Tatsache geprüft werden.
```

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule controllers_must_not_depend_on_repositories =
        noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("Repository")
                .because("Controller sind Adapter und dürfen Persistenzdetails nicht kennen.");
```

```java
@ArchTest
static final ArchRule domain_must_not_depend_on_infrastructure =
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..adapter..", "..infrastructure..", "..web..")
                .because("Die Domäne darf nicht von technischen Adaptern abhängen.");
```

---

## 26. Tests für DDD-Modelle

DDD-Tests prüfen Verhalten und Invarianten, nicht Getter/Setter.

### 26.1 Guter Domain-Test

```java
class OrderTest {

    @Test
    void addItem_addsItem_whenOrderIsDraft() {
        var order = Order.draft(new CustomerId(1L));

        order.addItem(productSnapshot("P1", "19.99"), new Quantity(2));

        assertThat(order.items()).hasSize(1);
        assertThat(order.total()).isEqualTo(Money.eur("39.98"));
    }

    @Test
    void addItem_throwsException_whenOrderIsAlreadyPlaced() {
        var order = Order.draft(new CustomerId(1L));
        order.addItem(productSnapshot("P1", "19.99"), new Quantity(1));
        order.place();

        assertThatThrownBy(() ->
                order.addItem(productSnapshot("P2", "9.99"), new Quantity(1)))
                .isInstanceOf(OrderModificationException.class);
    }
}
```

### 26.2 Schlechter Domain-Test

```java
@Test
void setStatus_setsStatus() {
    order.setStatus(OrderStatus.CANCELLED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
}
```

Dieser Test prüft technische Mutation, nicht fachliches Verhalten.

---

## 27. Migration von anämischem Modell zu DDD

Migration erfolgt schrittweise.

1. Fachliche Hotspots identifizieren.
2. Ubiquitous Language sammeln.
3. Setter-Nutzung analysieren.
4. Value Objects für wichtigste Fachwerte einführen.
5. Zustandsübergänge als Methoden modellieren.
6. Invarianten in Aggregate Root verschieben.
7. Interne Repositories entfernen.
8. API-DTOs von Entities trennen.
9. Bounded Contexts aus Package-/Service-Struktur sichtbar machen.
10. Domain-Tests ergänzen.
11. Architekturregeln automatisieren.
12. Integration Events von Domain Events trennen.

Vorher:

```java
order.setStatus(CANCELLED);
```

Nachher:

```java
order.cancel();
```

Vorher:

```java
campaign.setStartDate(start);
campaign.setEndDate(end);
```

Nachher:

```java
campaign.reschedule(new CampaignPeriod(start, end));
```

---

## 28. Ausnahmen

Ausnahmen sind zulässig, wenn:

- es sich um einfache CRUD-Funktion ohne relevante Fachlogik handelt,
- ein Legacy-System schrittweise migriert wird,
- JPA-Entity und Domain-Modell aus pragmatischen Gründen kombiniert werden,
- ein separates Domain-Modell mehr Komplexität erzeugen würde als Nutzen,
- ein Reporting-Modell bewusst nur eine Projektion ist,
- ein technischer Adapter keine fachliche Entscheidung enthält.

Ausnahmen müssen im Review benannt werden, wenn sie von einer bestehenden Guideline abweichen.

---

## 29. Definition of Done

Ein domänenkritischer Codebereich erfüllt diese Richtlinie, wenn:

1. zentrale Begriffe der Domäne im Code sichtbar sind,
2. Fachbegriffe konsistent verwendet werden,
3. Bounded Contexts explizit sind,
4. Aggregate Roots identifiziert sind,
5. interne Aggregate-Objekte nicht direkt von außen manipuliert werden,
6. Repositories nur für Aggregate Roots existieren,
7. Value Objects fachlich bedeutsame Werte kapseln,
8. Invarianten im Domänenmodell geschützt sind,
9. Application Services orchestrieren statt Fachregeln zu besitzen,
10. Domain Services nur für echte fachliche Logik verwendet werden,
11. Domain Events fachliche Tatsachen beschreiben,
12. Integration Events von Domain Events getrennt sind,
13. fremde Modelle über Anti-Corruption Layer übersetzt werden,
14. Tenant-Kontext in SaaS-Systemen explizit modelliert ist,
15. Domain-Tests fachliches Verhalten und Invarianten prüfen,
16. einfache CRUD-Fälle nicht unnötig mit DDD überladen werden.

---

## 30. Quellen und weiterführende Literatur

- Eric Evans — Domain-Driven Design Reference: https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf
- Martin Fowler — Bounded Context: https://martinfowler.com/bliki/BoundedContext.html
- Martin Fowler — Domain-Driven Design: https://martinfowler.com/bliki/DomainDrivenDesign.html
- Microsoft Azure Architecture Center — Use domain analysis to model microservices: https://learn.microsoft.com/en-us/azure/architecture/microservices/model/domain-analysis
- Microsoft Azure Architecture Center — Use tactical DDD to design microservices: https://learn.microsoft.com/en-us/azure/architecture/microservices/model/tactical-domain-driven-design
- Spring Data JPA Reference — Publishing Events from Aggregate Roots: https://docs.spring.io/spring-data/jpa/reference/repositories/core-domain-events.html
- Vaughn Vernon — Implementing Domain-Driven Design
- Alberto Brandolini — Introducing EventStorming
- Eric Evans — Domain-Driven Design: Tackling Complexity in the Heart of Software
