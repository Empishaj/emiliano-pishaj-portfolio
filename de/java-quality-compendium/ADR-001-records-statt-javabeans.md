# ADR-001 — Records statt JavaBeans für Datentransferobjekte

## Kontext & Problem

In traditionellen Java-Projekten werden Datentransferobjekte (DTOs) als JavaBeans implementiert: Felder, Getter, Setter, `equals()`, `hashCode()`, `toString()` — alles von Hand. Das führt zu hunderten Zeilen Boilerplate, die keinen Business-Wert haben, aber aktiv gewartet werden müssen. Java 16 hat **Records** als finale Lösung eingeführt.

---

## Schlechtes Beispiel — JavaBean-Hölle

```java
public class UserDto {
    private Long id;
    private String name;
    private String email;

    public UserDto() {}

    public UserDto(Long id, String name, String email) {
        this.id    = id;
        this.name  = name;
        this.email = email;
    }

    public Long   getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }

    public void setId(Long id)       { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String e)   { this.email = e; }

    @Override
    public boolean equals(Object o) {
        // ... 15 weitere Zeilen
    }

    @Override
    public int hashCode() { /* ... */ }

    @Override
    public String toString() { /* ... */ }
}
```

**Warum ist das schlecht?**
- **47+ Zeilen** für ein reines Dateobjekt — kein einziger Satz davon ist Business-Logik.
- Mutable by default — jeder kann jeden Wert jederzeit ändern, was zu subtilen Bugs führt.
- `equals()` und `hashCode()` müssen manuell synchron gehalten werden — wird ein Feld hinzugefügt, vergisst man es leicht.
- Setter suggerieren, dass Mutation gewünscht ist, obwohl DTOs üblicherweise unveränderlich sein sollten.

---

## Gutes Beispiel — Java 21 Record

```java
// Immutable, equals, hashCode, toString — alles inklusive.
// Accessor-Methoden: user.id(), user.name(), user.email()
public record UserDto(
    Long   id,
    String name,
    String email
) {}
```

### Mit Validierung im kompakten Konstruktor:

```java
public record UserDto(
    Long   id,
    String name,
    String email
) {
    // Kompakter Konstruktor — keine Zuweisung nötig, die erledigt der Compiler
    public UserDto {
        Objects.requireNonNull(name, "name must not be null");
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be a positive number");
        }
        // Optional: Normalisierung
        email = email.trim().toLowerCase();
    }
}
```

### Wither-Pattern für "geänderte Kopien":

```java
public record UserDto(Long id, String name, String email) {

    public UserDto withName(String newName) {
        return new UserDto(this.id, newName, this.email);
    }

    public UserDto withEmail(String newEmail) {
        return new UserDto(this.id, this.name, newEmail);
    }
}

// Verwendung:
var updated = original.withName("Max Mustermann");
```

---

## Begründung

Records sind von Natur aus **immutable** — alle Felder sind implizit `final`. Das eliminiert eine ganze Kategorie von Bugs durch unbeabsichtigte Mutation. `equals()` und `hashCode()` basieren auf allen Komponenten, was das erwartete Verhalten für DTOs ist.

Die Kürze ist kein Zufall: sie erzwingt, dass ein Record wirklich nur Daten transportiert — keine Logik, keine Seiteneffekte.

---

## Konsequenzen

**Positiv:**
- Drastisch weniger Boilerplate → weniger Fehler, schnellere Reviews.
- Immutability als Default → Thread-sicher ohne weitere Maßnahmen.
- Jackson serialisiert Records out-of-the-box ab Jackson 2.12 — kein `@JsonCreator` nötig.
- Generics funktionieren: `record Page<T>(List<T> content, int total) {}`

**Negativ / Einschränkungen:**
- Records können nicht extended werden — sie sind implizit `final`.
- Kein `@Entity` auf Records setzen — JPA benötigt mutable Klassen mit No-Arg-Konstruktor.
- Wer Vererbung braucht, muss Interfaces nutzen (→ ADR-002).

---

## Tipps

- Nutze Records für alles, was reine Daten trägt: DTOs, Value Objects, API-Request/Response-Typen, Map-Einträge, Methodenrückgaben mit mehreren Werten.
- Records als lokale Klassen innerhalb einer Methode sind erlaubt — perfekt für temporäre Zwischenergebnisse.
- Spring Boot unterstützt Records als `@ConfigurationProperties` ab Version 2.6.
