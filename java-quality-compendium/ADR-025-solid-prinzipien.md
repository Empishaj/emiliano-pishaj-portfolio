# ADR-025 — SOLID: Die fünf Prinzipien der objektorientierten Gestaltung

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 21                            |
| Datum      | 2023-12-02                    |
| Kategorie  | Design-Prinzipien             |

---

## Kontext & Problem

SOLID ist kein Regelwerk das blind angewendet wird — es ist ein Diagnosewerkzeug. Jedes Prinzip benennt eine Klasse von Problemen die regelmäßig auftreten und macht sie erkennbar. Ein Entwickler der SOLID verinnerlicht hat, erkennt das Problem bevor es zum Bug wird.

---

## S — Single Responsibility Principle (SRP)

**Eine Klasse hat genau einen Grund sich zu ändern.**

### Schlecht

```java
public class UserService {
    public void register(RegisterCommand cmd)      { /* DB + Email + Logging */ }
    public void sendWelcomeEmail(User user)        { /* SMTP-Logik */ }
    public String generateCsvReport(List<User> u) { /* CSV-Generierung */ }
    public void logUserAction(User u, String act) { /* Logging */ }
    // Ändert sich wenn: DB-Schema, Email-Template, CSV-Format, Log-Format → 4 Gründe
}
```

### Gut

```java
@Service public class UserRegistrationService { /* nur: Registrierungsfluss */ }
@Service public class WelcomeEmailService     { /* nur: Email-Versand */ }
@Service public class UserReportService       { /* nur: Berichtserzeugung */ }
// Jede Klasse ändert sich aus genau einem fachlichen Grund
```

**Test**: Kann man die Klasse in einem Satz beschreiben ohne "und"?

---

## O — Open/Closed Principle (OCP)

**Offen für Erweiterung, geschlossen für Modifikation.**

### Schlecht

```java
// Jeder neue Rabatttyp erfordert Änderung dieser Methode
public BigDecimal calculateDiscount(Order order, String type) {
    if ("PREMIUM".equals(type))   return order.total().multiply(new BigDecimal("0.20"));
    if ("SEASONAL".equals(type))  return order.total().multiply(new BigDecimal("0.10"));
    if ("LOYALTY".equals(type))   return order.total().multiply(new BigDecimal("0.15"));
    // Neue Anforderung: EMPLOYEE-Rabatt → diese Methode muss geändert werden
    return BigDecimal.ZERO;
}
```

### Gut

```java
// Strategie-Interface — geschlossen für Modifikation
public interface DiscountStrategy {
    BigDecimal calculate(Order order);
    boolean appliesTo(Order order);
}

// Erweiterung durch neue Klasse — keine Änderung bestehender Klassen
@Component public class PremiumDiscount  implements DiscountStrategy { /* +20% */ }
@Component public class SeasonalDiscount implements DiscountStrategy { /* +10% */ }
@Component public class EmployeeDiscount implements DiscountStrategy { /* +30% */ }

@Service
public class DiscountService {
    private final List<DiscountStrategy> strategies; // Alle automatisch injiziert

    public BigDecimal calculate(Order order) {
        return strategies.stream()
            .filter(s -> s.appliesTo(order))
            .map(s -> s.calculate(order))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

---

## L — Liskov Substitution Principle (LSP)

**Subtypen müssen durch ihre Basistypen ersetzbar sein, ohne das Programm zu brechen.**

### Schlecht

```java
public class Rectangle {
    protected int width, height;
    public void setWidth(int w)  { this.width  = w; }
    public void setHeight(int h) { this.height = h; }
    public int area() { return width * height; }
}

// Verletzt LSP: Ein Quadrat IST kein Rectangle im Sinne des Verhaltens
public class Square extends Rectangle {
    @Override public void setWidth(int w)  { this.width = this.height = w; }  // Seiteneffekt!
    @Override public void setHeight(int h) { this.width = this.height = h; }  // Seiteneffekt!
}

// Test der mit Rectangle funktioniert, mit Square bricht:
void testArea(Rectangle r) {
    r.setWidth(5);
    r.setHeight(4);
    assert r.area() == 20; // Schlägt bei Square fehl: 4*4=16 statt 5*4=20
}
```

### Gut

```java
// Gemeinsames Interface nur mit Verhalten das beide teilen
public interface Shape {
    int area();
}

public record Rectangle(int width, int height) implements Shape {
    public int area() { return width * height; }
}

public record Square(int side) implements Shape {
    public int area() { return side * side; }
}
// Kein Vererbungs-Seiteneffekt — jede Form ist eigenständig
```

**LSP-Test**: Funktioniert jeder Test der für den Basistyp geschrieben wurde auch für alle Subtypen?

---

## I — Interface Segregation Principle (ISP)

**Clients sollen nicht von Interfaces abhängen die sie nicht nutzen.**

### Schlecht

```java
// Fettes Interface — Implementierer müssen alle Methoden anbieten
public interface UserRepository {
    User findById(Long id);
    List<User> findAll();
    User save(User user);
    void delete(Long id);
    List<User> findByEmailDomain(String domain); // Nur vom AdminService gebraucht
    Statistics generateStatistics();              // Nur vom ReportService gebraucht
    void bulkImport(List<User> users);            // Nur vom ImportService gebraucht
}
// UserService muss alle 7 Methoden implementieren, obwohl er 3 braucht
```

### Gut

```java
// Fokussierte Interfaces — jeder Abnehmer bekommt genau was er braucht
public interface UserReader {
    Optional<User> findById(Long id);
    List<User> findAll();
}

public interface UserWriter {
    User save(User user);
    void delete(Long id);
}

public interface UserAdminRepository extends UserReader {
    List<User> findByEmailDomain(String domain);
    Statistics generateStatistics();
}

// Repository implementiert was nötig ist
@Repository
public class UserJpaRepository implements UserReader, UserWriter, UserAdminRepository { ... }

// Services hängen nur von dem ab was sie brauchen
@Service
public class UserService {
    private final UserReader reader;  // Kein Zugriff auf Writer oder Admin
    private final UserWriter writer;
}
```

---

## D — Dependency Inversion Principle (DIP)

**High-Level-Module hängen von Abstraktionen ab — nicht von konkreten Implementierungen.**

### Schlecht

```java
// UserService hängt direkt von konkreter MySQL-Implementierung ab
public class UserService {
    // Direkte Instanzierung — kein Austausch, kein Test ohne DB
    private final MySqlUserRepository repository = new MySqlUserRepository();
    private final SmtpEmailService    emailService = new SmtpEmailService("smtp.gmail.com");
}
```

### Gut

```java
// UserService hängt von Abstraktionen ab
public class UserService {
    private final UserRepository userRepository; // Interface, nicht Klasse
    private final EmailService   emailService;   // Interface, nicht Klasse

    // Konstruktorinjektion — Abhängigkeiten von außen, austauschbar
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService   = emailService;
    }
}

// Im Test: andere Implementierungen, kein Spring-Context nötig
var service = new UserService(
    new InMemoryUserRepository(),  // Testimplementierung
    new FakeEmailService()         // Testimplementierung
);
```

---

## Konsequenzen

**Positiv:** SOLID-konforme Klassen sind kleiner, fokussierter und unabhängig testbar. OCP + DIP zusammen ermöglichen es, Verhalten durch neue Klassen hinzuzufügen statt bestehende Klassen zu ändern — das reduziert Regressionen.

**Negativ:** Zu frühes Anwenden von SOLID erzeugt Over-Engineering. YAGNI (→ ADR-026) gilt: erst SOLID anwenden wenn ein zweiter Anwendungsfall auftaucht, nicht spekulativ.

---

## Tipps

- **SRP-Test**: Zähle die Abhängigkeiten. Mehr als 4–5 Konstruktor-Parameter? Verletzt fast immer SRP.
- **OCP über Strategie oder Konfiguration**: Nicht jede Erweiterung braucht eine neue Klasse — manchmal reicht ein Konfigurationswert.
- **LSP täglich anwenden**: Vor jeder Vererbung fragen: "Kann ich überall wo der Basistyp steht auch den Subtyp einsetzen?" Wenn nein: Komposition statt Vererbung (→ ADR-008).
- **DIP ≠ DI-Framework**: Dependency Inversion ist ein Designprinzip. Dependency Injection (Spring) ist ein Mechanismus es umzusetzen.
