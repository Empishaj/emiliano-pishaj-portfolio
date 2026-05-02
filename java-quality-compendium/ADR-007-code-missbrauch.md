# ADR-007 — Code-Missbrauch: Wenn gute Features falsch eingesetzt werden

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2024-01-15                        |
| Kategorie  | Code Quality / Anti-Patterns      |

---

## Kontext & Problem

Java 21 bringt mächtige neue Features — aber Macht ohne Disziplin erzeugt neuen Missbrauch. Viele Anti-Patterns entstehen nicht aus Unwissen, sondern aus dem Wunsch, "clever" zu sein: verschachtelte Streams als Einzeiler, `Optional` als Universalcontainer, Reflection für Dinge die Design lösen sollte. Dieser ADR benennt die häufigsten Missbrauchsmuster und zeigt die richtige Alternative.

---

## Missbrauch 1 — Stream-Ketten als Beweis von Intelligenz

### Schlecht — unlesbarer One-Liner

```java
// Was macht das? Niemand weiß es auf den ersten Blick.
Map<String, List<String>> result = users.stream()
    .filter(u -> u.isActive() && u.getAge() > 18)
    .flatMap(u -> u.getOrders().stream()
        .filter(o -> o.getStatus() == COMPLETED)
        .map(o -> Map.entry(u.getEmail(), o.getProductName())))
    .collect(groupingBy(Map.Entry::getKey,
        mapping(Map.Entry::getValue, toList())));
```

**Warum ist das schlecht?**
- Keine einzige Zwischenvariable — jeder Schritt ist implizit und namenlos.
- Debugging unmöglich: kein sinnvoller Breakpoint setzbar.
- Verschachtelte `flatMap` + `map` + `collect` in einer Zeile = kognitive Überlastung.
- "Clever" ist kein Qualitätsmerkmal — Lesbarkeit ist es.

### Gut — benannte Schritte mit erklärendem Intent

```java
// Jede Zwischenstufe hat einen Namen, der erklärt WARUM sie existiert.
var activeAdultUsers = users.stream()
    .filter(User::isActive)
    .filter(u -> u.getAge() > 18)
    .toList();

var completedOrdersByEmail = activeAdultUsers.stream()
    .flatMap(user -> completedOrdersOf(user).stream()
        .map(order -> Map.entry(user.getEmail(), order.getProductName())))
    .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

// Extrahierte Hilfsmethode — benannt, testbar, wiederverwendbar
private List<Order> completedOrdersOf(User user) {
    return user.getOrders().stream()
        .filter(o -> o.getStatus() == COMPLETED)
        .toList();
}
```

---

## Missbrauch 2 — `Optional` als Allzweck-Container

### Schlecht — Optional überall

```java
// Optional als Feldtyp — NIEMALS
public class User {
    private Optional<String> middleName; // Falsch! Optional ist kein Feldtyp.
}

// Optional als Parameter — NIEMALS
public void createUser(String name, Optional<String> email) { // Falsch!
    // Aufrufer muss Optional.of() oder Optional.empty() übergeben — umständlich
}

// Optional.get() ohne isPresent() — Exception vorprogrammiert
String name = findUser(id).get(); // NoSuchElementException wenn leer!

// Optional für Collections — sinnlos
Optional<List<User>> users = findAll(); // Eine leere Liste reicht — kein Optional nötig
```

### Gut — Optional nur für Rückgabewerte, die fehlen können

```java
// ✅ Optional NUR als Rückgabetyp von Methoden
public Optional<User> findById(Long id) {
    return userRepository.findById(id);
}

// ✅ Verarbeitung ohne get()
findById(id)
    .map(User::name)
    .orElse("Unbekannt");

// ✅ Für Pflicht-Ergebnisse: orElseThrow mit sprechender Exception
User user = findById(id)
    .orElseThrow(() -> new UserNotFoundException(id));

// ✅ Nullable Felder mit @Nullable annotieren, nicht Optional
public class User {
    private @Nullable String middleName; // Klar, direkt, kein Wrapper-Overhead
}

// ✅ Leere Collection statt Optional<Collection>
public List<Order> findOrdersByUser(Long userId) {
    return orderRepository.findByUserId(userId); // Nie null, nie Optional — einfach leer
}
```

---

## Missbrauch 3 — Reflection statt Design

### Schlecht — Reflection für fehlende Abstraktion

```java
// Reflection um private Felder zu lesen — ein Design-Versagen verkleidet als Cleverness
public class UserMapper {
    public Map<String, Object> toMap(User user) throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (Field field : User.class.getDeclaredFields()) {
            field.setAccessible(true); // Kapselung gewaltsam gebrochen
            map.put(field.getName(), field.get(user));
        }
        return map;
    }
}
```

**Warum ist das schlecht?**
- Kapselung (das A und O von OOP) wird aktiv zerstört.
- Kein Compile-Zeit-Check — Feldnamen als Strings sind refactoring-blind.
- Performance: Reflection ist 10–100x langsamer als direkter Zugriff.
- Meistens ein Zeichen, dass ein `record`, ein `toMap()`-Interface oder ein Mapper das eigentliche Problem löst.

### Gut — explizites Design statt Reflection-Magie

```java
// Records haben toMap() implizit über ihre Komponenten — oder einfach direkt:
public record User(Long id, String name, String email) {

    public Map<String, Object> toMap() {
        return Map.of(
            "id",    id,
            "name",  name,
            "email", email
        );
    }
}

// Oder: MapStruct für Mapping — Compile-Zeit-sicher, performant, wartbar
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserEntity entity);
    UserEntity toEntity(UserDto dto);
}
```

---

## Missbrauch 4 — `instanceof` als schlechter Polymorphismus-Ersatz

### Schlecht — instanceof-Kaskaden statt Polymorphismus

```java
// Überall im Code verstreut: Typprüfungen statt Verhalten
double calculateDiscount(Customer customer) {
    if (customer instanceof PremiumCustomer) {
        return 0.20;
    } else if (customer instanceof RegularCustomer) {
        return 0.05;
    } else if (customer instanceof NewCustomer) {
        return 0.0;
    }
    throw new IllegalStateException("Unbekannter Kundentyp");
}
```

**Warum ist das schlecht?**
- Neue Kundentypen erfordern das Auffinden und Ändern *aller* instanceof-Kaskaden im gesamten Codebase.
- Open/Closed Principle verletzt: offen für Erweiterung bedeutet hier: alle alten Methoden anfassen.
- Verhalten gehört zur Klasse — nicht zu einer Hilfsmethode irgendwo anders.

### Gut — Polymorphismus: Verhalten gehört zum Typ

```java
sealed interface Customer permits PremiumCustomer, RegularCustomer, NewCustomer {
    double discountRate(); // Jeder Typ kennt seinen eigenen Rabatt
}

record PremiumCustomer(String id) implements Customer {
    public double discountRate() { return 0.20; }
}

record RegularCustomer(String id) implements Customer {
    public double discountRate() { return 0.05; }
}

record NewCustomer(String id) implements Customer {
    public double discountRate() { return 0.0; }
}

// Aufruf — keine Typprüfung, kein Switch, kein if
double calculateDiscount(Customer customer) {
    return customer.discountRate(); // Fertig.
}
```

---

## Missbrauch 5 — Exceptions für Kontrollfluss

### Schlecht — Exception als if-Ersatz

```java
// Exceptions sind teuer (Stack-Trace-Erzeugung) und semantisch falsch
public boolean isValidEmail(String email) {
    try {
        new InternetAddress(email).validate();
        return true;
    } catch (AddressException e) {
        return false; // Exception als boolean-Ersatz — falsch!
    }
}

// Noch schlimmer: Exception fangen und ignorieren
try {
    doSomething();
} catch (Exception e) {
    // leer — Exception verschwindet lautlos
}
```

### Gut — Exceptions für außergewöhnliche Zustände

```java
// Validierung ohne Exception-Missbrauch
public boolean isValidEmail(String email) {
    return email != null
        && email.contains("@")
        && EMAIL_PATTERN.matcher(email).matches();
}

// Oder: Result-Typ statt Exception für erwartbare Fehlerzustände (→ ADR-002)
sealed interface ValidationResult permits Valid, Invalid {}
record Valid(String value)          implements ValidationResult {}
record Invalid(String reason)       implements ValidationResult {}

ValidationResult validate(String email) {
    if (email == null || !email.contains("@")) {
        return new Invalid("Ungültige E-Mail-Adresse");
    }
    return new Valid(email);
}

// Exceptions NUR für wirklich außergewöhnliche Zustände — und niemals leer fangen:
try {
    riskyOperation();
} catch (SpecificException e) {
    log.error("Kontext: was wurde versucht, id={}", id, e);
    throw new DomainException("Fachliche Beschreibung des Problems", e);
}
```

---

## Konsequenzen

**Positiv:**
- Code, der die Intention ausdrückt statt die Implementierung versteckt.
- Weniger überraschende Laufzeitfehler durch klare Contracts.
- Einfachere Testbarkeit: kein Mocking von Reflection oder Exception-Flows nötig.

**Negativ:**
- Mehr Klassen und Interfaces — initialer Eindruck: "mehr Aufwand". Tatsächlich: weniger Wartungsaufwand.

---

## Tipps

- **Die Faustregel für Streams**: Wenn du eine Zwischenstufe nicht in einem Satz erklären kannst, extrahiere sie in eine benannte Methode.
- **Die Faustregel für Optional**: Taucht `Optional` als Parametertyp, Feldtyp oder in einer Collection auf? Das ist falsch.
- **Die Faustregel für instanceof**: Mehr als zwei `instanceof`-Prüfungen auf denselben Typ verteilt im Code? Das schreit nach einer Methode im Interface.
- **Die goldene Regel für Exceptions**: Wenn du im `catch`-Block nichts sinnvolles tun kannst — zumindest loggen und weiterwerfen. Leer fangen ist verboten.
