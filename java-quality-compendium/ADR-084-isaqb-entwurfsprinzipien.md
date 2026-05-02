# ADR-084 — iSAQB F4: Entwurfsprinzipien — Kopplung, Kohäsion & Separation of Concerns

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board / alle Entwickler                           |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | iSAQB Foundation · Entwurfsprinzipien                         |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| iSAQB-Lernziel    | LZ-4-1 bis LZ-4-9 (Foundation Level)                         |

---

## 1. Das fundamentale Ziel aller Entwurfsprinzipien

Alle Entwurfsprinzipien verfolgen ein gemeinsames Ziel:

```
WARTBARKEIT = Fähigkeit, das System zu ändern ohne unverhältnismäßige Kosten

Die Kosten einer Änderung hängen von zwei Faktoren ab:
  ① Wie stark sind Teile des Systems GEKOPPELT? (→ Ripple-Effect-Radius)
  ② Wie stark gehören Teile zusammen? (→ KOHÄSION, Verständlichkeit)

Formel für gute Architektur:
  HOHE KOHÄSION + NIEDRIGE KOPPLUNG = hohe Wartbarkeit

Formel für schlechte Architektur (Big Ball of Mud):
  NIEDRIGE KOHÄSION + HOHE KOPPLUNG = jede Änderung bricht alles
```

---

## 2. Kopplung: Warum sie schadet und wie man sie misst

### 2.1 Was Kopplung ist

```
KOPPLUNG = Maß für die Abhängigkeit zwischen Teilen eines Systems

Wenn A sich ändert und B sich deshalb auch ändern muss → A und B sind gekoppelt

Arten von Kopplung (von schlimm zu besser):
  1. Content Coupling:    B greift direkt auf A's interne Daten zu        → FATAL
  2. Common Coupling:     A und B teilen globalen State                   → SCHLECHT
  3. Control Coupling:    A steuert B's Ablauf durch Parameter-Flags      → SCHLECHT
  4. Stamp Coupling:      A und B teilen komplexe Datenstruktur           → AKZEPTABEL
  5. Data Coupling:       A und B tauschen nur primitive Werte aus        → GUT
  6. Message Coupling:    A und B kommunizieren nur durch Events           → SEHR GUT
  7. No Coupling:         A und B wissen nichts voneinander               → IDEAL (selten möglich)
```

### 2.2 Kopplung konkret messen

```java
// METRIC: Afferent Coupling (Ca) = wer hängt von dieser Klasse ab?
// METRIC: Efferent Coupling (Ce) = von wem hängt diese Klasse ab?
// METRIC: Instability (I) = Ce / (Ca + Ce)   → 0 = stabil, 1 = instabil

// ❌ SCHLECHT — zu hohe Kopplung
public class OrderService {
    // 7 direkte Abhängigkeiten = Ce = 7 → instabile Klasse
    private final UserRepository       userRepo;       // JPA-Klasse
    private final ProductRepository    productRepo;    // JPA-Klasse
    private final InventoryService     inventory;      // Konkreter Service
    private final PaymentService       payment;        // Konkreter Service
    private final NotificationService  notification;   // Konkreter Service
    private final AuditLogger          logger;         // Konkrete Implementierung
    private final StripeClient         stripe;         // Externe Bibliothek direkt!

    // Wenn eine der 7 Klassen sich ändert → OrderService muss sich ändern
    // Wenn Stripe ausgetauscht wird → OrderService muss geändert werden
    // Test: alle 7 müssen gemockt oder echte Instanzen verwendet werden
}

// ✅ GUT — Kopplung auf Abstraktionen reduziert
public class OrderDomainService {
    // 3 Abhängigkeiten, alle Interfaces (Ports)
    private final OrderRepository   orderRepo;     // Interface (Port)
    private final PaymentGateway    payment;       // Interface (Port)
    private final EventPublisher    events;        // Interface (Port)

    // Stripe austauschen? Nur der Adapter ändert sich, nicht dieser Service.
    // Test: 3 einfache Mocks — keine echten Datenbanken, keine Stripe-Verbindung.
}
```

### 2.3 Temporal Coupling: Die unterschätzte Kopplung

```java
// Temporal Coupling: Komponenten müssen zur gleichen Zeit verfügbar sein

// ❌ SCHLECHT — synchrone Kette = temporale Kopplung
public void placeOrder(PlaceOrderCommand cmd) {
    var order = save(cmd);            // Muss DB-Verbindung haben
    inventory.reserve(order.items()); // Inventory-Service muss up sein
    payment.charge(order.total());    // Stripe muss up sein
    notification.send(order);         // Email-Server muss up sein
    // Wenn EINES down: Bestellung schlägt fehl
    // Latenz: 100ms DB + 200ms Inventory + 500ms Stripe + 300ms Email = 1100ms
}

// ✅ GUT — Ereignisse entkoppeln temporal
@Transactional
public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
    var order = save(cmd);            // Nur DB nötig
    events.publish(OrderPlacedEvent.from(order));
    return OrderCreatedResult.from(order);
    // Inventory, Payment, Notification reagieren asynchron
    // Latenz: 100ms DB — fertig
    // Wenn Stripe down: Order trotzdem angelegt, Zahlung später retried
}
```

---

## 3. Kohäsion: Warum sie entscheidend für Verständlichkeit ist

### 3.1 Was Kohäsion ist

```
KOHÄSION = Maß dafür, wie stark die Elemente einer Komponente zusammengehören

Hohe Kohäsion: Alles in dieser Klasse/Modul ist wegen derselben Sache hier.
Niedrige Kohäsion: Verschiedene, unzusammenhängende Verantwortlichkeiten.

Arten von Kohäsion (von gut zu schlecht):
  1. Funktionale Kohäsion:    Alle Teile dienen einer einzigen Funktion  → OPTIMAL
  2. Sequentielle Kohäsion:   Ausgabe von A ist Eingabe von B            → GUT
  3. Kommunikative Kohäsion:  Alle Teile operieren auf denselben Daten   → GUT
  4. Prozedurale Kohäsion:    Teile müssen in bestimmter Reihenfolge     → AKZEPTABEL
  5. Temporale Kohäsion:      Teile laufen zur selben Zeit (z.B. Init)   → SCHWACH
  6. Logische Kohäsion:       Teile tun "ähnliche Dinge" (broad category)→ SCHLECHT
  7. Zufällige Kohäsion:      Teile ohne erkennbare Beziehung            → FATAL
```

### 3.2 Kohäsion konkret erkennen

```java
// ❌ NIEDRIGE KOHÄSION — Zufällige/Logische Kohäsion
public class Utils {                    // "Utils" = Warnsignal (→ ADR-008)
    public static String formatEmail(String email) { ... }
    public static BigDecimal calculateTax(BigDecimal amount) { ... }
    public static void sendSlackMessage(String msg) { ... }
    public static List<Order> sortOrders(List<Order> orders) { ... }
    public static String generatePdf(Order order) { ... }
}
// Problem: Warum gehört Steuerberechnung mit Slack-Messaging zusammen?
// Jede Änderung des Steuerrechts betrifft diese Klasse.
// Jede Slack-API-Änderung betrifft diese Klasse.
// Nicht testbar ohne alle Abhängigkeiten.

// ✅ HOHE KOHÄSION — Funktionale Kohäsion
public class TaxCalculator {            // Nur eine Verantwortung: Steuer
    public Money calculateVat(Money amount, Country country) { ... }
    public Money calculateGrossAmount(Money net, Country country) { ... }
}

public class Email {                    // Value Object mit Email-spezifischem Verhalten
    private final String value;
    public Email { /* Validierung */ }
    public String masked() { return value.charAt(0) + "***@" + domain(); }
}

public class NotificationDispatcher {  // Nur: Notifications senden
    public void sendOrderConfirmation(Order order, Customer customer) { ... }
    public void sendPasswordReset(Customer customer) { ... }
}
```

---

## 4. Separation of Concerns (SoC): Das Grundprinzip aller Architektur

### 4.1 Was SoC bedeutet

```
SEPARATION OF CONCERNS = jeder Teil des Systems hat genau eine Verantwortung,
und diese ist klar abgegrenzt von anderen Verantwortlichkeiten.

Edsger Dijkstra (1974): "It is what I sometimes have called the separation of concerns,
which, even if not perfectly possible, is yet the only available technique for
effective ordering of one's thoughts."

Konkret in Software:
  ① Fachliche Logik ≠ Persistenzlogik ≠ UI-Logik ≠ Infrastrukturlogik
  ② Lesen ≠ Schreiben (→ CQRS, ADR-032)
  ③ Validierung ≠ Transformation ≠ Persistenz
  ④ Business-Entscheidung ≠ Framework-Details
```

### 4.2 SoC Verletzungen erkennen

```java
// ❌ SCHLECHT — Alle Concerns in einem Controller vermischt
@RestController
public class OrderController {

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        // Concern 1: Input-Parsing (sollte im DTO sein)
        String productId = (String) body.get("productId");
        int quantity = (int) body.get("quantity");

        // Concern 2: Validierung (sollte im Domain-Objekt sein)
        if (productId == null || productId.isEmpty()) {
            return ResponseEntity.badRequest().body("productId required");
        }
        if (quantity <= 0) {
            return ResponseEntity.badRequest().body("quantity must be positive");
        }

        // Concern 3: Business-Logik (sollte im Domain-Service sein)
        var product = productRepo.findById(productId).orElseThrow();
        var price = product.price().multiply(BigDecimal.valueOf(quantity));
        if (price.compareTo(new BigDecimal("10000")) > 0) {
            // Geschäftsregel: Bestellungen > 10.000€ brauchen Genehmigung
        }

        // Concern 4: Datenbankzugriff (sollte im Repository sein)
        var order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        jdbcTemplate.update("INSERT INTO orders ...", order);

        // Concern 5: Externe API (sollte im Gateway sein)
        stripe.charge(price, currentUser.getStripeId());

        // Concern 6: Response-Serialisierung
        return ResponseEntity.ok(Map.of("orderId", order.getId()));
    }
}
// 6 verschiedene Concerns in einem Controller.
// Jeder dieser 6 kann sich unabhängig ändern.
// Jede Änderung betrifft den Controller.

// ✅ GUT — Jeder Concern in seiner Schicht
@RestController
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;

    @PostMapping("/orders")
    @ResponseStatus(CREATED)
    public OrderCreatedResponse createOrder(
            @Valid @RequestBody PlaceOrderRequest request,  // Validierung durch Bean Validation
            @AuthenticationPrincipal Jwt jwt) {
        return placeOrderUseCase.execute(             // Business-Logik im Use Case
            PlaceOrderCommand.from(request, jwt));   // Transformation hier
    }
}
// Dieser Controller: NUR HTTP-Concern (Request → Command → Response)
```

---

## 5. Information Hiding: Kapselung als Architekturprinzip

### 5.1 David Parnas' Schlüsselerkenntnis (1972)

```
David Parnas, "On the Criteria To Be Used in Decomposing Systems into Modules" (1972):

"We propose instead that one begins with a list of difficult design decisions
or design decisions which are likely to change. Each module is then designed
to hide such a decision from the others."

Übersetzt: Teile das System entlang von Entscheidungen die sich wahrscheinlich ändern.
           Verstecke jede solche Entscheidung hinter einer stabilen Schnittstelle.
```

### 5.2 Information Hiding in Java

```java
// ❌ SCHLECHT — Implementierungsdetail exponiert
public class Order {
    // Öffentliche Felder: jeder kann interne Struktur sehen und ändern
    public List<OrderItem> items;  // Konkrete Liste! Wechsel zu Set? Bricht API.
    public BigDecimal totalAmount; // Wer berechnet das? Kann inkonsistent werden.
    public String status;          // String statt Enum: was sind gültige Werte?
}

// Aufrufer-Code der von Implementierungsdetails abhängt:
order.items.add(newItem);   // Direkte Mutation der internen Liste
order.totalAmount = order.items.stream()
    .map(i -> i.price)
    .reduce(BigDecimal.ZERO, BigDecimal::add);  // Berechnung außen!

// ✅ GUT — Information Hiding durch Kapselung
public final class Order {
    private final List<OrderItem> items;   // Implementierung verborgen
    private OrderStatus status;

    // Öffentliche API: stabile Schnittstelle, Implementierung austauschbar
    public void addItem(Product product, Quantity quantity) {
        if (status != PENDING) throw new OrderModificationException(id, status);
        items.add(new OrderItem(product, quantity));
        // Interne Konsistenz garantiert durch die Order selbst
    }

    public Money total() {
        return items.stream()
            .map(OrderItem::subtotal)
            .reduce(Money.zero(EUR), Money::add);
    }

    // Unmodifiable View — kein direkter Zugriff auf interne Struktur
    public List<OrderItem> items() {
        return Collections.unmodifiableList(items);
    }
}
// Interne Änderung (ArrayList → LinkedList)? Aufrufer merkt nichts.
// Neue Berechnungsformel für total()? Nur diese Klasse ändert sich.
```

---

## 6. Dependency Inversion Principle: Die Krönung aller Entwurfsprinzipien

```
DEPENDENCY INVERSION PRINCIPLE (DIP):
  Regel 1: High-Level-Module sollen nicht von Low-Level-Modulen abhängen.
            Beide sollen von Abstraktionen abhängen.
  Regel 2: Abstraktionen sollen nicht von Details abhängen.
            Details sollen von Abstraktionen abhängen.

Warum ist DIP das mächtigste Prinzip?
  → Es ist die technische Basis für alle anderen Prinzipien
  → Ohne DIP: keine echten Schichten, keine Ports & Adapters, kein testbarer Code
  → Mit DIP: jede Komponente kann ausgetauscht werden ohne andere zu brechen
```

```java
// ❌ SCHLECHT — High-Level-Modul (OrderService) hängt von Low-Level (JPA, Stripe) ab
public class OrderService {
    private final JpaOrderRepository jpaRepo;   // LOW-LEVEL: JPA
    private final StripePaymentClient stripe;   // LOW-LEVEL: Stripe SDK

    public void placeOrder(PlaceOrderCommand cmd) {
        var entity = new OrderEntity(cmd);      // JPA-spezifisch!
        jpaRepo.save(entity);                   // JPA direkt
        stripe.charge(cmd.amount(), cmd.card()); // Stripe direkt
    }
}
// JPA durch MongoDB ersetzen? OrderService muss geändert werden.
// Stripe durch PayPal ersetzen? OrderService muss geändert werden.
// Test ohne echte DB und echtes Stripe? Unmöglich.

// ✅ GUT — Dependency Inversion: beide hängen von Abstraktion ab
// ABSTRAKTION (Interface in Domain-Schicht):
public interface OrderRepository {
    void save(Order order);
}

public interface PaymentGateway {
    PaymentResult charge(Money amount, PaymentMethod method);
}

// HIGH-LEVEL-MODUL hängt von Abstraktion ab:
public class OrderDomainService {
    private final OrderRepository repository;  // Interface!
    private final PaymentGateway  gateway;     // Interface!

    public OrderCreatedResult placeOrder(PlaceOrderCommand cmd) {
        var order = Order.place(cmd);
        repository.save(order);               // Abstraktion — kein JPA
        var payment = gateway.charge(         // Abstraktion — kein Stripe
            order.total(), cmd.paymentMethod());
        return OrderCreatedResult.from(order, payment);
    }
}

// DETAILS hängen von Abstraktion ab (Infrastruktur-Adapter):
@Repository
class JpaOrderRepository implements OrderRepository { ... }    // LOW-LEVEL: JPA

@Component
class StripePaymentGateway implements PaymentGateway { ... }   // LOW-LEVEL: Stripe
```

---

## 7. Abstraktionsebenen: Wie hoch soll die Abstraktion sein?

```
KEINE ABSTRAKTION:
  OrderService → JpaOrderRepository (Klasse)
  → Direktkopplung, keine Testbarkeit ohne DB

SINNVOLLE ABSTRAKTION:
  OrderService → OrderRepository (Interface/Port)
  → Testbar, austauschbar, klar dokumentiert

ZU VIEL ABSTRAKTION (YAGNI-Verletzung → ADR-026):
  OrderService → AbstractOrderRepositoryFactoryBeanProvider<T extends Identifiable<K>>
  → Niemand versteht es, kein Nutzen, erhöhte Komplexität ohne Gewinn

FAUSTREGEL:
  Abstraktion einführen wenn:
    ① Zwei oder mehr Implementierungen existieren oder absehbar sind
    ② Testbarkeit ohne Abstraktion unmöglich (externe Systeme, DB)
    ③ Deployment- oder Konfigurationsvariation nötig ist
  
  Abstraktion NICHT einführen wenn:
    ① Genau eine Implementierung, keine weitere absehbar (YAGNI)
    ② Abstraktion komplexer als konkreter Aufruf
```

---

## 8. Metriken für gutes Design

```java
// Metriken die ArchUnit und SonarQube berechnen:

// CYCLOMATIC COMPLEXITY (McCabe):
// Anzahl linear unabhängiger Pfade durch eine Methode
// Gut: ≤ 5 | Akzeptabel: 6-10 | Schlecht: > 10

// COGNITIVE COMPLEXITY (SonarQube):
// Wie schwer ist die Methode zu verstehen?
// Schlechter als Cyclomatic Complexity für nested if/for
// Gut: ≤ 10 | Schlecht: > 15

// LINES OF CODE (LOC):
// Methode: ≤ 20 Zeilen | Klasse: ≤ 300 Zeilen

// AFFERENT COUPLING (Ca):
// Wer hängt von dieser Klasse ab?
// Hohe Ca = stabile Klasse (Interface, Domain-Objekt)
// Schlecht wenn: hohe Ca UND häufige Änderungen

// EFFERENT COUPLING (Ce):
// Von wem hängt diese Klasse ab?
// Hohe Ce = instabile Klasse (Service, Koordinator)
// Problem wenn: hohe Ce AUF konkrete Klassen (keine Interfaces)

// INSTABILITY (I = Ce / (Ca + Ce)):
// 0 = maximal stabil (kein Ce) | 1 = maximal instabil (kein Ca)
// Domain-Objekte: I → 0 (stabil, wenig Abhängigkeiten)
// Adapter: I → 1 (instabil — gut, sie dürfen sich häufig ändern)

// ABSTRACTNESS (A = abstrakte Klassen / alle Klassen):
// 0 = keine Abstraktionen | 1 = nur Abstraktionen
// Ideal: A = 0.5 im Gesamtsystem

// DISTANCE FROM MAIN SEQUENCE (D = |A + I - 1|):
// D → 0 = ausgeglichene Klasse (weder zu konkret noch zu abstrakt)
// D → 1 = Problem: "Zone of Pain" (konkret und stabil) oder
//                  "Zone of Uselessness" (abstrakt und instabil)
```

---

## 9. Validierung durch ArchUnit

```java
@AnalyzeClasses(packages = "com.example")
class DesignPrinciplesTest {

    // SoC: Controller hat keine Business-Logik (keine direkten Berechnungen)
    @ArchTest
    static final ArchRule controller_keine_business_logik =
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().containNumberOfElements(GreaterThan.greaterThan(0))
            // Komplexere Regel: keine direkten Repository-Aufrufe
            .andShould().dependOnClassesThat()
            .haveSimpleNameEndingWith("Repository")
            .because("ADR-084: Controller kennt keine Repositories — nur Services");

    // Information Hiding: keine öffentlichen Felder in Domain-Klassen
    @ArchTest
    static final ArchRule keine_oeffentlichen_felder_in_domain =
        noFields()
            .that().areDeclaredInClassesThat()
            .resideInAPackage("com.example.domain..")
            .should().bePublic()
            .because("ADR-084: Information Hiding — Felder sind immer privat");

    // DIP: Domain-Klassen dürfen nur auf Interfaces hängen (keine Konkretisierungen)
    @ArchTest
    static final ArchRule domain_haengt_nur_von_interfaces =
        noClasses()
            .that().resideInAPackage("com.example.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.example.adapter..")
            .because("ADR-084: DIP — Domain kennt keine Adapter");
}
```

---

## 10. Quellen & Referenzen

- **iSAQB CPSA-F Curriculum (2023), LZ-4-1 bis LZ-4-9** — Entwurfsprinzipien als Kern der Foundation-Ausbildung.
- **David Parnas, "On the Criteria To Be Used in Decomposing Systems into Modules" (1972), CACM** — Information Hiding als fundamentales Dekompositionsprinzip.
- **Edsger Dijkstra, "A Discipline of Programming" (1976)** — Separation of Concerns als grundlegendes Denkprinzip.
- **Robert C. Martin, "Agile Software Development, Principles, Patterns, and Practices" (2002)** — SOLID-Prinzipien, Dependency Inversion, Coupling-Metriken.
- **Edward Yourdon, Larry Constantine, "Structured Design" (1979)** — Kopplung und Kohäsion als Qualitätsmetriken; Klassifikation der Kohäsionsarten.
- **Tom McCabe, "A Complexity Measure" (1976), IEEE TSE** — Cyclomatic Complexity als erste quantitative Design-Metrik.

---

## Akzeptanzkriterien

- [ ] SonarQube-Quality-Gate: Cyclomatic Complexity ≤ 10 pro Methode, Cognitive Complexity ≤ 15
- [ ] ArchUnit: keine öffentlichen Felder in Domain-Klassen
- [ ] ArchUnit: Domain-Klassen hängen nicht von Adaptern ab (DIP)
- [ ] ArchUnit: Controller haben keine direkten Repository-Aufrufe (SoC)
- [ ] Code-Review-Checkliste enthält: Kopplung geprüft? Kohäsion geprüft? SoC eingehalten?

---

## Verwandte ADRs

- [ADR-083](ADR-083-isaqb-architekturmuster.md) — Architekturmuster die auf diesen Prinzipien aufbauen
- [ADR-025](ADR-025-solid-prinzipien.md) — SOLID (Spezialisierung dieser Prinzipien)
- [ADR-026](ADR-026-kiss-dry-yagni-demeter.md) — KISS/DRY/YAGNI/Demeter (Ergänzende Prinzipien)
- [ADR-008](ADR-008-falsche-objektorientierung.md) — Konkrete OOP-Fehler durch Prinzip-Verletzungen
- [ADR-085](ADR-085-isaqb-querschnittskonzepte.md) — Querschnittskonzepte (iSAQB F5)
