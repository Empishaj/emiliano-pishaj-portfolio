# ADR-052 — Immutability & Defensive Programming

| Feld       | Wert                          |
|------------|-------------------------------|
| Status     | ✅ Akzeptiert                 |
| Java       | 21                            |
| Datum      | 2024-01-01                    |
| Kategorie  | Design-Prinzipien / Sicherheit|

---

## Kontext & Problem

Mutable State ist die Wurzel einer ganzen Klasse von Bugs: unerwartete Mutation durch Seiteneffekte, Thread-Safety-Probleme, schwer reproduzierbare Fehler. Defensive Programming stellt sicher dass ein Objekt seinen Invarianten nicht verlassen kann — egal wer es aufruft.

---

## Immutability: die stärkste Garantie

```java
// ❌ Schlecht: mutable, keine Invarianten geschützt
public class Money {
    private BigDecimal amount;   // public setter → jeder kann ändern
    private String currency;

    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
}

// Aufruf: money.setAmount(new BigDecimal("-100")); → negativer Betrag!
// Aufruf: money.setCurrency(null); → NullPointerException später
```

```java
// ✅ Gut: Record — immutable by design, Validierung im Konstruktor
public record Money(BigDecimal amount, Currency currency) {

    // Kompakter Konstruktor: Validierung, Normalisierung
    public Money {
        Objects.requireNonNull(amount,   "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(
                "Money amount must not be negative, was: " + amount);
        // Normalisierung: immer 2 Dezimalstellen
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    // "Mutation" durch Kopie — Original bleibt unverändert
    public Money add(Money other) {
        if (!currency.equals(other.currency))
            throw new CurrencyMismatchException(currency, other.currency);
        return new Money(amount.add(other.amount), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}
```

---

## Defensive Copies: Mutable Parameter schützen

```java
// ❌ Schlecht: interne Liste nach außen exponiert
public class Order {
    private final List<OrderItem> items;

    public List<OrderItem> getItems() {
        return items; // Aufrufer kann items.add() aufrufen → Invariante verletzt!
    }

    public Order(List<OrderItem> items) {
        this.items = items; // Aufrufer behält Referenz → kann später ändern!
    }
}

// ✅ Gut: Defensive Copies bei Ein- und Ausgabe
public final class Order {
    private final List<OrderItem> items;

    public Order(List<OrderItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        // Defensive Copy beim Eingang: Aufrufer kann seine Liste ändern → egal
        this.items = List.copyOf(items); // Unmodifiable!
    }

    // Defensive Copy beim Ausgang: unmodifiable View
    public List<OrderItem> items() {
        return Collections.unmodifiableList(items);
        // oder: return List.copyOf(items); // echte Kopie wenn paranoid
    }

    public int itemCount() { return items.size(); }
}
```

---

## Null-Handling: fail-fast, klar, früh

```java
// ❌ Schlecht: null propagiert still
public String formatName(String firstName, String lastName) {
    return firstName + " " + lastName;
    // Wenn null: "null Mustermann" — keine Exception, falsches Verhalten
}

// ❌ Schlecht: NPE tief in der Callstack — schwer zu debuggen
public void process(Order order) {
    // order kann null sein — merkt man erst bei order.total()
    var total = order.total(); // NullPointerException hier
}

// ✅ Gut: fail-fast an der Grenze
public String formatName(String firstName, String lastName) {
    Objects.requireNonNull(firstName, "firstName must not be null");
    Objects.requireNonNull(lastName,  "lastName must not be null");
    return firstName.trim() + " " + lastName.trim();
}

public void process(Order order) {
    Objects.requireNonNull(order, "order must not be null");
    // Rest des Codes kann order sicher nutzen
}

// Für optionale Werte: Optional<T> als Rückgabetyp (→ ADR-007)
public Optional<User> findByEmail(String email) {
    Objects.requireNonNull(email, "email must not be null");
    return userRepository.findByEmail(email);
}
```

---

## Preconditions: Verträge explizit machen

```java
// Guava Preconditions oder eigene Hilfsmethoden
public void reserve(int quantity) {
    Preconditions.checkArgument(quantity > 0,
        "quantity must be positive, was: %s", quantity);
    Preconditions.checkState(status == PENDING,
        "can only reserve items for PENDING orders, current status: %s", status);

    // Ab hier: quantity > 0 und status == PENDING garantiert
    items.add(new Reservation(quantity));
}

// Java-Standard: assert (nur aktiviert mit -ea JVM-Flag — nicht für Produktion)
// Für Produktion: explizite Checks mit IllegalArgumentException / IllegalStateException
```

---

## Unveränderliche Kollektionen

```java
// Java 21: Unveränderliche Kollektionen factory methods
List<String>  unmodList = List.of("a", "b", "c");          // throws UnsupportedOperationException bei add()
Set<String>   unmodSet  = Set.of("x", "y");
Map<String,Integer> unmodMap = Map.of("one", 1, "two", 2);
Map<String,Integer> unmodMap2 = Map.ofEntries(
    Map.entry("key1", 1),
    Map.entry("key2", 2)
);

// ACHTUNG: List.of() und Collections.unmodifiableList() sind verschieden!
var list = new ArrayList<>(List.of(1, 2, 3));
var unmod = Collections.unmodifiableList(list);
list.add(4); // Ändert die Original-Liste!
// unmod.get(3) gibt 4 zurück — unmod ist eine VIEW, keine Kopie!

// Sicher: List.copyOf() erzeugt echte unveränderliche Kopie
var safeCopy = List.copyOf(list); // Kopie, nicht View
```

---

## final überall — als Dokumentation und Schutz

```java
// final auf Felder: keine Reassignment möglich
public class OrderService {
    private final OrderRepository repository;  // final: Pflicht für injizierte Felder
    private final EventPublisher  publisher;

    // final auf Parameter: Kommuniziert "wird nicht geändert"
    public Order findById(final OrderId id) {
        // id kann nicht reassigned werden
        return repository.findById(id).orElseThrow();
    }
}

// final auf lokale Variablen: bevorzugt
public Money calculateTotal(final List<OrderItem> items) {
    final var sum = items.stream()
        .map(OrderItem::subtotal)
        .reduce(Money.zero(EUR), Money::add);
    return sum; // sum kann nicht reassigned werden
}
```

---

## Konsequenzen

**Positiv:** Immutable Objekte sind thread-sicher ohne Synchronisation (→ ADR-033). Defensive Copies verhindern Aliasing-Bugs — zwei Code-Stellen halten dieselbe Referenz und eine mutiert das Objekt unbemerkt. Fail-Fast macht Fehler früh und klar sichtbar statt spät und verdeckt.

**Negativ:** Defensive Copies kosten Speicher und CPU bei hoher Frequenz — für Performance-kritischen Code abwägen. `final` überall kann anfangs umständlich wirken.

---

## 💡 Guru-Tipps

- **Records first**: Wenn ein Objekt Daten trägt und sich nicht ändern soll → Record. Spart alle defensiven Patterns automatisch.
- **`List.copyOf()`** statt `Collections.unmodifiableList()` wenn echte Isolation nötig.
- **Fail-Fast in Konstruktoren**: Validierung im Konstruktor stellt sicher dass kein invalides Objekt existieren kann — nicht erst bei Verwendung.
- **`final` auf alle Felder** als Default — nur bei wirklicher Mutation explizit weglassen und kommentieren warum.

---

## Verwandte ADRs

- [ADR-001](ADR-001-records-statt-javabeans.md) — Records als immutable Standardtyp.
- [ADR-033](ADR-033-thread-safety-concurrency.md) — Immutability als Thread-Safety-Garantie.
- [ADR-008](ADR-008-falsche-objektorientierung.md) — Primitive Obsession → Value Objects → Immutability.
