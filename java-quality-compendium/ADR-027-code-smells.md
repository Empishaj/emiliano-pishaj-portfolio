# ADR-027 — Code Smells: Diagnosekatalog

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 21                            |
| Datum      | 2024-01-01                    |
| Kategorie  | Code Quality / Refactoring    |

---

## Kontext & Problem

Code Smells sind keine Bugs — der Code funktioniert. Sie sind Indikatoren dass das Design verbessert werden sollte. Ein Smell signalisiert: "Hier wird zukünftige Änderung schmerzhaft sein." Dieses ADR katalogisiert die häufigsten Smells mit konkreten Gegenmaßnahmen.

---

## Smell 1 — Long Method

**Symptom**: Eine Methode hat mehr als 15–20 Zeilen.

```java
// ❌ 60-Zeilen-Methode — macht zu viele Dinge
public OrderResult processOrder(CreateOrderCommand cmd) {
    // Block 1: Validierung (10 Zeilen)
    if (cmd.userId() == null) throw new ...;
    if (cmd.items().isEmpty()) throw new ...;
    for (var item : cmd.items()) { if (item.quantity() <= 0) throw new ...; }

    // Block 2: User laden (5 Zeilen)
    var user = userRepository.findById(cmd.userId()).orElseThrow(...);
    if (!user.isActive()) throw new ...;

    // Block 3: Preisberechnung (15 Zeilen)
    var subtotal = cmd.items().stream()
        .map(i -> productRepository.findById(i.productId())
            .map(p -> p.price().multiply(BigDecimal.valueOf(i.quantity())))
            .orElseThrow())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    var discount = discountService.calculate(user, subtotal);
    var total = subtotal.subtract(discount);

    // Block 4: Persistieren (5 Zeilen)
    var order = new OrderEntity(user.id(), cmd.items(), total);
    orderRepository.save(order);

    // Block 5: Notifications (10 Zeilen)
    emailService.sendOrderConfirmation(user.email(), order);
    inventoryService.reserve(cmd.items());
    eventPublisher.publishEvent(new OrderCreatedEvent(order.id()));
    return new OrderResult(order.id(), total);
}
```

```java
// ✅ Kurze Methode, jeder Schritt benannt
public OrderResult processOrder(CreateOrderCommand cmd) {
    validateCommand(cmd);
    var user  = loadActiveUser(cmd.userId());
    var total = calculateTotal(cmd.items(), user);
    var order = persistOrder(cmd.items(), user.id(), total);
    notifyOrderCreated(order, user);
    return new OrderResult(order.id(), total);
}
// Jede Hilfsmethode: 5–10 Zeilen, eigenständig testbar
```

---

## Smell 2 — Long Parameter List

**Symptom**: Eine Methode hat mehr als 3–4 Parameter.

```java
// ❌ 7 Parameter — was ist was?
public User createUser(String firstName, String lastName, String email,
                       String password, String role, boolean active, LocalDate birthDate) { ... }

// Aufruf: createUser("Max", "M", "max@x.com", "pw", "ADMIN", true, LocalDate.of(1990,1,1));
// Was bedeutet true? Was bedeutet "ADMIN"?
```

```java
// ✅ Parameter-Objekt
public record CreateUserCommand(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email    String email,
    @NotBlank String password,
    UserRole  role,
    boolean   active,
    LocalDate birthDate
) {}

public User createUser(CreateUserCommand cmd) { ... }
// Klar, selbstdokumentierend, erweiterbar ohne Signaturänderung
```

---

## Smell 3 — Divergent Change

**Symptom**: Eine Klasse wird aus verschiedenen Gründen geändert (verletzt SRP).

```java
// ❌ UserService ändert sich wenn:
// → Email-Template sich ändert
// → DB-Schema sich ändert
// → Report-Format sich ändert
// → Notification-Kanal sich ändert
public class UserService {
    public void register(...)    { /* DB + Email + Logging */ }
    public void sendReport(...)  { /* CSV + Email + DB */ }
    public void notify(...)      { /* Push + SMS + Email */ }
}

//Separate Klassen, jede ändert sich aus einem Grund
```

---

## Smell 4 — Shotgun Surgery

**Symptom**: Eine einzige Änderung erfordert Anpassungen an vielen Klassen.

```java
// ❌ Überall im Code wird "email" als String übergeben
// Wenn E-Mail-Validierung sich ändert: 47 Stellen im Code anpassen
void createUser(String email) { validateEmail(email); ... }
void sendNotification(String email) { validateEmail(email); ... }
void updateContact(String email) { validateEmail(email); ... }
// validateEmail() ist an 47 Stellen aufgerufen

//Email als Value Object — Validierung einmal, im Typ 
public record Email(String value) {
    public Email { /* Validierung hier und nur hier */ }
}
// Änderung der Validierungsregel: eine Stelle
```

---

## Smell 5 — Feature Envy

**Symptom**: Eine Methode interessiert sich mehr für Daten einer anderen Klasse als für ihre eigene.

```java
// ❌ OrderPrinter "beneidet" Order und Customer um ihre Daten
public class OrderPrinter {
    public String format(Order order) {
        // Diese Methode kennt die interne Struktur von Order, Customer, Address
        return order.getCustomer().getName() + ", " +
               order.getCustomer().getAddress().getStreet() + ", " +
               order.getCustomer().getAddress().getCity();
    }
}

// ✅ Methode dorthin verschieben wo die Daten leben
public class Order {
    public String formattedShippingAddress() {
        return customer.formattedAddress(); // Delegiert an Customer
    }
}
// OrderPrinter: order.formattedShippingAddress() — kein Wissen über Interna
```

---

## Smell 6 — Data Clumps

**Symptom**: Dieselben 3–4 Datenfelder tauchen immer zusammen auf.

```java
// ❌ street, city, zipCode wandern immer gemeinsam durch den Code
void createOrder(String street, String city, String zipCode, ...) { ... }
void validateAddress(String street, String city, String zipCode) { ... }
void formatAddress(String street, String city, String zipCode)   { ... }

// ✅ Als eigenes Objekt extrahieren
public record Address(
    @NotBlank String street,
    @NotBlank String city,
    @Pattern(regexp = "\\d{5}") String zipCode
) {}

void createOrder(Address shippingAddress, ...) { ... }
```

---

## Smell 7 — Speculative Generality

**Symptom**: Abstraktion für Anwendungsfälle die noch nicht existieren.

```java
// ❌ "Vielleicht brauchen wir mal verschiedene Speicher-Backends..."
public interface OrderStorage<T extends Serializable, ID, CTX extends StorageContext> {
    CompletableFuture<T> storeAsync(T entity, ID id, CTX ctx, StorageOptions opts);
}
// Konkrete Implementierung: nur MySqlOrderStorage. Nie etwas anderes.
// Alle diese Abstraktion: YAGNI-Verletzung

// ✅ Direkt und konkret (→ ADR-026 YAGNI)
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> { }
```

---

## Smell 8 — Dead Code

**Symptom**: Code der nie ausgeführt wird.

```java
// ❌ Nie aufgerufen, nie gelöscht
private List<User> getAllUsersLegacy() { /* aus 2019, nie mehr verwendet */ }

// Parameter der nie genutzt wird
public Order process(Order order, boolean legacyMode) {
    // legacyMode wird nie gelesen — für immer false
    return process(order);
}

// ✅ Löschen. Git merkt sich alles. YAGNI.
// Wenn wirklich doch gebraucht: Git-History wiederherstellen.
```

---

## Smell 9 — Comments as Deodorant

**Symptom**: Kommentare erklären was der Code tut statt warum — weil der Code selbst nicht spricht.

```java
// ❌ Kommentar erklärt unleserlichen Code
// Berechne den Preis nach Formel: Basis * (1 - d) * m + t
double p = b * (1 - d) * m + t;

// ❌ Kommentar als Ersatz für einen guten Methodennamen
// Prüfe ob User aktiv und Erwachsener
if (user.active && Period.between(user.dob, now).getYears() >= 18) { ... }

// ✅ Selbsterklärender Code braucht keinen Kommentar
double price = basePrice * (1 - discountRate) * multiplier + tax;

if (user.isActive() && user.isAdult()) { ... }
// Kein Kommentar nötig — der Code sagt selbst was er tut
```

---

## Smell 10 — Primitive Obsession (→ ADR-008)

```java
// ❌ Alles ist String oder int
void transfer(String fromAccountId, String toAccountId, double amount, String currency) { ... }

// ✅ Domänentypen
void transfer(AccountId from, AccountId to, Money amount) { ... }
```

---

## Smell 11 — Switch Statements / instanceof-Kaskaden (→ ADR-002, ADR-003)

```java
// ❌ Überall scattered: if-instanceof oder switch auf Typ
// ✅ Sealed Interface + Pattern Matching
```

---

## Refactoring-Entscheidungsbaum

```
Code-Smell erkannt?
├── Zu lang (Methode/Klasse)
│   ├── Long Method       → Extract Method
│   └── Large Class       → Extract Class / SRP anwenden
├── Kopplung zu stark
│   ├── Feature Envy      → Move Method zur betroffenen Klasse
│   ├── Data Clumps       → Extract Value Object
│   ├── Train Wreck       → Law of Demeter / Delegationsmethoden
│   └── Shotgun Surgery   → Move Field + DRY anwenden
├── Falsche Abstraktion
│   ├── Speculative Gen.  → Inline / YAGNI
│   └── Dead Code         → Delete
└── Schlechte Benennung
    ├── Misleading Name   → Rename
    └── Kommentar-Deodorant → Rename + Remove Comment
```

---

## Tipps

- **Smells sind kumulativ**: Ein Smell allein schadet kaum. Drei Smells in derselben Klasse — dann wird Refactoring dringend.
- **Smells vor Features fixen**: "Boy Scout Rule" — hinterlasse den Code sauberer als du ihn vorgefunden hast. Kleines Refactoring bei jedem Commit.
- **SonarQube / SpotBugs** automatisiert Smell-Erkennung im CI-Pipeline.
- **Refactoring ≠ Rewrite**: Refactoring ist eine schrittweise Verbesserung mit laufenden Tests — kein "Big Bang" Umbau.

