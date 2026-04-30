# ADR-026 — KISS, DRY, YAGNI & Law of Demeter

| Feld       | Wert                          |
|------------|-------------------------------|
| Status     | ✅ Akzeptiert                 |
| Java       | 21                            |
| Datum      | 2024-01-01                    |
| Kategorie  | Design-Prinzipien             |

---

## KISS — Keep It Simple, Stupid

**Die einfachste Lösung die funktioniert ist die richtige.**

### ❌ Schlecht — überengineerte Lösung für ein triviales Problem

```java
// Für eine Liste von 5 konfigurierten Strings:
public class ConfigurationRegistry {
    private static final Map<String, ConfigurationEntry> registry = new ConcurrentHashMap<>();
    private final ConfigurationValidator validator;
    private final ConfigurationSerializer serializer;

    public <T> Optional<T> getConfiguration(String key, Class<T> type,
            ConfigurationContext ctx, ValidationStrategy strategy) {
        return Optional.ofNullable(registry.get(key))
            .filter(e -> validator.validate(e, strategy))
            .map(e -> serializer.deserialize(e.getValue(), type, ctx));
    }
}
```

### ✅ Gut — so einfach wie möglich

```java
// application.yml: feature.flags: [FLAG_A, FLAG_B, FLAG_C]
@ConfigurationProperties(prefix = "feature")
public record FeatureConfig(List<String> flags) {}

// Verwendung: featureConfig.flags().contains("FLAG_A")
// Fertig. Keine Registry, kein Validator, kein Serializer.
```

**KISS-Test**: Kann ein Entwickler der das erste Mal den Code sieht in 30 Sekunden verstehen was er tut? Wenn nicht: vereinfachen.

**Symptome von KISS-Verletzungen:**
- Klassen mit `Manager`, `Processor`, `Handler`, `Registry` im Namen ohne klare Domänenbedeutung.
- Interfaces mit genau einer Implementierung die niemals ausgetauscht wird.
- Abstraktion für Code der einmal aufgerufen wird.

---

## DRY — Don't Repeat Yourself

**Jedes Stück Wissen hat eine einzige, eindeutige, maßgebliche Darstellung im System.**

### ❌ Schlecht — Wissen dupliziert

```java
// Validierungslogik an drei verschiedenen Orten
@PostMapping("/users")
public void createUser(@RequestBody CreateUserRequest req) {
    if (req.email() == null || !req.email().contains("@")) // ①
        throw new BadRequestException("Invalid email");
    userService.create(req);
}

@Service
public class UserService {
    public void create(CreateUserRequest req) {
        if (req.email() == null || !req.email().contains("@")) // ② dupliziert!
            throw new ValidationException("Invalid email");
        userRepository.save(new UserEntity(req));
    }
}

@Repository
public class UserRepository {
    public UserEntity save(UserEntity entity) {
        if (entity.email() == null || !entity.email().contains("@")) // ③ dupliziert!
            throw new DataIntegrityException("Email required");
        // ...
    }
}
```

### ✅ Gut — Wissen einmal, am richtigen Ort

```java
// Validierung einmal: im Record-Konstruktor (der "Single Source of Truth")
public record Email(String value) {
    public Email {
        Objects.requireNonNull(value, "email must not be null");
        if (!value.matches("^[^@]+@[^@]+\\.[^@]+$"))
            throw new InvalidEmailException(value);
        value = value.trim().toLowerCase();
    }
}

// Controller, Service, Repository: alle nutzen Email — nie wieder dupliziert
public record CreateUserRequest(@NotNull Email email, @NotBlank String name) {}
```

**DRY ≠ "kein copy-paste"**. DRY bezieht sich auf Wissen, nicht auf Syntax. Zwei ähnlich aussehende Code-Stücke die verschiedenes Wissen repräsentieren, dürfen ähnlich aussehen. Zwei Code-Stücke die dieselbe Geschäftsregel implementieren, dürfen nicht dupliziert sein — auch wenn sie syntaktisch einfach wirken.

**Symptome von DRY-Verletzungen:**
- Wenn eine Geschäftsregel sich ändert, müssen mehrere Stellen im Code angepasst werden.
- Kommentare wie `// Achtung: auch in UserValidator.java anpassen!`
- Copy-Paste mit geringfügigen Variationen.

---

## YAGNI — You Aren't Gonna Need It

**Implementiere nichts was aktuell nicht gebraucht wird.**

### ❌ Schlecht — spekulative Generalisierung

```java
// Aufgabe: Benutzer-Email aus DB lesen und zurückgeben
public interface DataFetcher<T, ID, CTX, OPT> {
    Optional<T> fetch(ID id, CTX context, FetchOptions<OPT> options);
    CompletableFuture<T> fetchAsync(ID id, CTX context);
    List<T> fetchBatch(List<ID> ids, CTX context);
    Page<T> fetchPaged(Specification<T> spec, Pageable page, CTX context);
}

// "Falls wir mal verschiedene Datenquellen brauchen..."
// "Falls wir mal async brauchen..."
// "Falls wir mal Batch brauchen..."
// → Alle diese Fälle existieren NICHT. Jetzt wird alles wartungsaufwändiger.
```

### ✅ Gut — nur was jetzt gebraucht wird

```java
// Aufgabe ist: Email aus DB lesen
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findById(Long id); // Das ist alles was gebraucht wird
}

// Wenn wirklich async gebraucht wird: dann erweitern.
// Nicht jetzt, nicht spekulativ.
```

**YAGNI-Test**: Gibt es heute einen konkreten Anwendungsfall für diese Abstraktion? Nein → nicht implementieren.

**YAGNI gilt nicht für**: Sicherheit, Logging, Fehlerbehandlung — diese werden immer gebraucht und sind teuer nachzurüsten.

---

## Law of Demeter — "Rede nur mit deinen direkten Freunden"

**Eine Methode soll nur Methoden aufrufen von:**
1. dem eigenen Objekt (`this`)
2. direkt übergebenen Parametern
3. selbst erzeugten Objekten
4. direkten Feldern

**Nie**: Methoden von Objekten die durch Methodenaufrufe zurückgeliefert werden (`a.getB().getC().doSomething()`).

### ❌ Schlecht — Train Wreck / Message Chain

```java
// Drei Ebenen tief — enge Kopplung an interne Struktur
public class OrderPrinter {
    public String format(Order order) {
        // Kopplung an: Order → Customer → Address → City → Name
        String city = order.getCustomer()
                           .getAddress()
                           .getCity()
                           .getName(); // ← Train Wreck!

        String zip = order.getCustomer()
                          .getAddress()
                          .getZipCode(); // ← nochmal

        return city + " " + zip;
    }
}
// Wenn Address.getCity() sich ändert: OrderPrinter bricht.
// Wenn Customer.getAddress() sich ändert: OrderPrinter bricht.
// Enge Kopplung über 3 Ebenen.
```

### ✅ Gut — Verhalten delegieren, nicht Daten holen

```java
// Order kennt ihre eigene Lieferadresse
public class Order {
    private final Customer customer;

    // Delegiert — kennt interne Struktur, gibt nach außen nur was gebraucht wird
    public String shippingCity()    { return customer.shippingCity(); }
    public String shippingZipCode() { return customer.shippingZipCode(); }
}

public class Customer {
    private final Address shippingAddress;

    public String shippingCity()    { return shippingAddress.city(); }
    public String shippingZipCode() { return shippingAddress.zipCode(); }
}

// OrderPrinter: kein Wissen über Customer oder Address
public class OrderPrinter {
    public String format(Order order) {
        return order.shippingCity() + " " + order.shippingZipCode();
    }
}
```

**"Tell, Don't Ask"** ist die positive Formulierung desselben Prinzips: Sage einem Objekt was es tun soll, frage es nicht nach seinen Daten um dann selbst zu entscheiden.

```java
// ❌ Ask: Daten holen, extern entscheiden
if (user.getRole().equals("ADMIN") && user.isActive()) {
    user.setPermission(FULL_ACCESS);
}

// ✅ Tell: Objekt selbst entscheiden lassen
user.grantAdminAccessIfEligible(); // User kennt seine eigene Logik
```

---

## Konsequenzen

**Positiv:** Code der KISS + YAGNI folgt ist dramatisch einfacher zu lesen, zu testen und zu ändern. Law of Demeter reduziert Kopplung messbar — Änderungen breiten sich weniger aus.

**Negativ:** YAGNI erfordert Disziplin gegen den Impuls "das könnten wir später brauchen". DRY kann in seltenen Fällen zu falschen Abstraktionen führen wenn zwei ähnliche Stellen doch verschiedenes Wissen darstellen ("Wrong Abstraction" — AHA-Principle als Ergänzung).

---

## 💡 Guru-Tipps

- **AHA-Principle** (Avoid Hasty Abstractions): Erst ab der dritten Wiederholung abstrahieren — nicht beim zweiten Mal. Zwei ähnliche Stellen können zufällig ähnlich sein.
- **YAGNI vs. SOLID**: SOLID sagt "halte es offen für Erweiterung". YAGNI sagt "aber implementiere die Erweiterung erst wenn sie gebraucht wird". Kein Widerspruch — aber im Zweifel zuerst YAGNI.
- **Law of Demeter im Review**: Mehr als zwei `.` in einer Methodenkette? Als Warnsignal markieren.

---

## Verwandte ADRs

- [ADR-025](ADR-025-solid-prinzipien.md) — SOLID als ergänzendes Fundament.
- [ADR-008](ADR-008-falsche-objektorientierung.md) — Tell don't ask als OOP-Grundprinzip.
