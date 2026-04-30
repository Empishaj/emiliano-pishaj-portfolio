# ADR-003 — Pattern Matching & Switch Expressions

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 21 · JEP 441 (finalisiert)    |
| Datum      | 2025-05-22                    |
| Kategorie  | Language Pattern              |

---

## Kontext & Problem

Das klassische `instanceof`-Idiom besteht aus drei redundanten Schritten: Typ prüfen, casten, nutzen. Nach dem Check weiß der Compiler bereits den Typ — der explizite Cast ist reines Rauschen. Pattern Matching (JEP 441) kollabiert diese drei Schritte in einen einzigen, typsicheren Ausdruck.

Kombiniert mit Switch Expressions entsteht der mächtigste Kontrollfluss-Mechanismus in Java seit Jahren.

---

## Schlechtes Beispiel — klassisches instanceof + Cast

```java
// Drei Schritte für eine einzige Information:
void printArea(Shape shape) {
    if (shape instanceof Circle) {
        Circle c = (Circle) shape;       // ← redundanter Cast
        System.out.println(Math.PI * c.radius() * c.radius());
    } else if (shape instanceof Rectangle) {
        Rectangle r = (Rectangle) shape; // ← wieder redundant
        System.out.println(r.width() * r.height());
    } else if (shape instanceof Triangle) {
        Triangle t = (Triangle) shape;   // ← und nochmal
        System.out.println(t.base() * t.height() / 2.0);
    } else {
        // Neue Shape-Variante hinzugefügt? Kein Compiler-Fehler.
        throw new IllegalStateException("Unbekannte Form: " + shape);
    }
}
```

**Warum ist das schlecht?**
- Jeder Cast ist redundant — der Compiler weiß nach `instanceof` bereits den Typ.
- `else`-Zweig mit `throw` ist ein Laufzeitschutz statt eines Compile-Zeit-Schutzes.
- Bei einer offenen Hierarchie (kein `sealed`) gibt es keine Garantie auf Vollständigkeit.
- Lesbarkeit leidet stark bei tiefer Verschachtelung.

---

## Gutes Beispiel — Pattern Matching Switch Expression

```java
// sealed interface Shape permits Circle, Rectangle, Triangle {}

double area(Shape shape) {
    return switch (shape) {
        case Circle    c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
        case Triangle  t -> t.base() * t.height() / 2.0;
        // Kein default nötig bei sealed — der Compiler prüft Vollständigkeit.
        // Neue Shape-Variante? Compiler zeigt sofort alle unvollständigen Switches.
    };
}
```

### Pattern Matching bei instanceof (ohne Switch):

```java
// Vorher:
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.toUpperCase());
}

// Java 16+ Pattern Matching:
if (obj instanceof String s) {
    System.out.println(s.toUpperCase()); // s ist direkt verfügbar
}

// Sogar in Bedingungen kombinierbar:
if (obj instanceof String s && s.length() > 5) {
    System.out.println("Langer String: " + s);
}
```

### Guarded Patterns — Bedingungen direkt im Case:

```java
String classify(Object obj) {
    return switch (obj) {
        case Integer i when i < 0    -> "Negative Zahl";
        case Integer i when i == 0   -> "Null";
        case Integer i               -> "Positive Zahl: " + i;
        case String  s when s.isEmpty() -> "Leerer String";
        case String  s               -> "String: " + s;
        case null                    -> "null";
        default                      -> "Unbekannt: " + obj.getClass().getSimpleName();
    };
}
```

### Record Patterns — Destrukturierung direkt im Case:

```java
// Record: record Point(int x, int y) {}
// Record: record Line(Point start, Point end) {}

String describeShape(Object shape) {
    return switch (shape) {
        case Point(int x, int y) when x == 0 && y == 0
            -> "Ursprung";
        case Point(int x, int y)
            -> "Punkt (%d, %d)".formatted(x, y);
        case Line(Point(int x1, int y1), Point(int x2, int y2))
            -> "Linie von (%d,%d) nach (%d,%d)".formatted(x1, y1, x2, y2);
        default
            -> "Unbekannte Form";
    };
}
```

---

## Begründung

Pattern Matching beseitigt die strukturelle Redundanz des alten `instanceof`-Idioms. Der Compiler kann Typ-Informationen, die er nach einem erfolgreichen Check bereits besitzt, direkt als gebundene Variable bereitstellen — ohne dass der Entwickler explizit casten muss.

Switch Expressions (mit `->`) sind **Ausdrücke**, keine Statements: sie liefern einen Wert, benötigen kein `return`, und haben kein Fall-through per Design. Das macht sie sicherer und kompakter als klassische `switch`-Statements.

---

## Konsequenzen

**Positiv:**
- Keine redundanten Casts mehr — der Code drüct die Intention direkt aus.
- `case null` endlich sauber behandelbar, statt implizit NullPointerException zu riskieren.
- Compiler prüft bei `sealed`-Hierarchien die Vollständigkeit — vergessene Fälle werden zur Compile-Zeit aufgedeckt.
- Record Patterns ermöglichen elegante Destrukturierung verschachtelter Datenstrukturen.

**Negativ / Einschränkungen:**
- Reihenfolge der Cases zählt: spezifischere Patterns müssen vor allgemeineren stehen. Der Compiler warnt bei unerreichbaren Cases.
- Guarded Patterns (`when`) sind ausdrucksstärker als klassische `if-else`-Ketten, erfordern aber Eingewöhnung.

---

## Tipps

- **Arrow-Syntax verwenden** (`->`), niemals Doppelpunkt-Syntax (`:`) in neuen Switch-Ausdrücken — kein Fall-through, kein `break`, klarer Intent.
- Bei mehreren Cases mit gleicher Aktion: `case A, B, C -> doSomething();`
- Switch Expressions müssen exhaustiv sein: entweder alle Fälle abdecken (sealed) oder einen `default`-Branch haben.
- `yield` verwenden, wenn im Case-Body mehr als ein Ausdruck nötig ist:
  ```java
  case Circle c -> {
      double r = c.radius();
      yield Math.PI * r * r;
  }
  ```

 