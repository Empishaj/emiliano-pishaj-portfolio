# ADR-005 — Text Blocks für Lesbarkeit und Richtigkeit

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 15 · JEP 378                  |
| Datum      | 2025-07-03                    |
| Kategorie  | Clean Code / Lesbarkeit       |

---

## Kontext & Problem

Mehrzeilige Strings in Java waren jahrelang ein Alptraum: `+`-Konkatenation, manuelle Escape-Zeichen, eigenhändige Einrückung mit `\n`, keine visuelle Struktur. SQL-Abfragen, JSON-Blobs, HTML-Templates und XML-Fragmente wurden dadurch unleserlich und fehleranfällig. Text Blocks (JEP 378) lösen das Problem final.

---

## Schlechtes Beispiel — String-Konkatenations-Hölle

```java
// SQL mit Konkatenation — unleserlich, fehleranfällig, schwer wartbar
String sql = "SELECT u.id, u.name, o.created_at, o.total\n" +
             "FROM users u\n" +
             "JOIN orders o ON o.user_id = u.id\n" +
             "WHERE u.active = true\n" +
             "  AND o.total > :minTotal\n" +
             "ORDER BY o.created_at DESC";

// JSON-Payload — wer sieht hier auf Anhieb den Fehler?
String json = "{\"userId\": " + userId + ", \"action\": \"" + action + "\", " +
              "\"timestamp\": \"" + Instant.now() + "\"}";
```

**Warum ist das schlecht?**
- Keine visuelle Entsprechung zum tatsächlichen String — man muss im Kopf `\n` und `"` mental übersetzen.
- Jedes vergessene `\n` oder falsch escaptes `"` ist ein Laufzeitfehler, kein Compile-Fehler.
- Einrückung des Java-Codes und Einrückung des Inhalts sind voneinander entkoppelt.
- Diff-Tools können keine sinnvolle Diff-Ansicht liefern.

---

## Gutes Beispiel — Text Blocks

```java
// SQL — so wie er tatsächlich aussieht
String sql = """
        SELECT u.id, u.name, o.created_at, o.total
        FROM users u
        JOIN orders o ON o.user_id = u.id
        WHERE u.active = true
          AND o.total > :minTotal
        ORDER BY o.created_at DESC
        """;

// JSON — klar strukturiert
String json = """
        {
          "userId": %d,
          "action": "%s",
          "timestamp": "%s"
        }
        """.formatted(userId, action, Instant.now());
```

### HTML-Template ohne Escape-Chaos:

```java
// Vorher — unlesbar
String html = "<html>\n  <body>\n    <h1>Hello, " + name + "!</h1>\n  </body>\n</html>";

// ✅ Nachher — so wie HTML aussehen soll
String html = """
        <html>
          <body>
            <h1>Hello, %s!</h1>
          </body>
        </html>
        """.formatted(name);
```

### GraphQL Query — komplexe Queries ohne Chaos:

```java
String query = """
        query GetUser($id: ID!) {
          user(id: $id) {
            id
            name
            email
            orders(status: PENDING) {
              id
              total
              createdAt
            }
          }
        }
        """;
```

---

## Technische Details: Einrückung und Whitespace

Die JVM berechnet automatisch den **gemeinsamen Einrückungsblock** (incidental whitespace) und entfernt ihn. Das schließende `"""` bestimmt die Baseline:

```java
String a = """
        Zeile 1
        Zeile 2
        """; // schließendes """ auf Column 8 → 8 Spaces werden entfernt

// Entspricht: "Zeile 1\nZeile 2\n"

String b = """
        Zeile 1
        Zeile 2
"""; // schließendes """ auf Column 0 → kein Whitespace entfernt

// Entspricht: "        Zeile 1\n        Zeile 2\n"
```

**Wichtig:** Text Blocks enden immer mit `\n` (sofern das schließende `"""` auf einer eigenen Zeile steht). Um das trailing newline zu entfernen: `\` am Ende der letzten Inhaltszeile:

```java
String noTrailingNewline = """
        Kein Zeilenumbruch am Ende\
        """;
```

---

## Begründung

Text Blocks sind nicht nur Syntaxzucker — sie machen den Code **korrekt by design**: Die visuelle Struktur des Strings entspricht seiner tatsächlichen Struktur. Ein SQL-Query sieht im Code wie ein SQL-Query aus, ein JSON wie JSON. Das vereinfacht Reviews, Diffs, Copy-Paste aus anderen Tools, und reduziert Escape-Fehler auf null.

---

## Konsequenzen

**Positiv:**
- Kein manuelles Escaping von `"` — nur `"""` muss escaped werden (selten nötig).
- `String::formatted` als typsichere Alternative zu Konkatenation.
- IDE-Support: IntelliJ und Eclipse erkennen Text Blocks und bieten Language-Injection (SQL, JSON, HTML-Highlighting).
- Kompatibel mit allen String-Methoden — Text Blocks sind zur Laufzeit gewöhnliche `String`-Objekte.

**Negativ / Einschränkungen:**
- Text Blocks eignen sich **nicht** für dynamische Strings mit komplexer Logik — dort Template Engines (Thymeleaf, Freemarker) oder Prepared Statements verwenden.
- Für SQL unbedingt **Prepared Statements** statt `formatted()` verwenden — Text Blocks lösen kein SQL-Injection-Problem!

---

## Sicherheitshinweis

Text Blocks lösen **kein** SQL-Injection- oder XSS-Problem. Nutzereingaben dürfen nie direkt in einen Text Block interpoliert werden:

```java
// ❌ SQL Injection — NIEMALS so!
String sql = """
        SELECT * FROM users WHERE name = '%s'
        """.formatted(userInput); // Katastrophe wenn userInput = "' OR '1'='1"

// ✅ Immer Prepared Statements verwenden
String sql = """
        SELECT * FROM users WHERE name = :name
        """;
// Parameter via JPA/JDBC binden
```

---

## Tipps

- Text Blocks für alle statischen Strings > 2 Zeilen verwenden: SQL, JSON, YAML, HTML, XML, GraphQL, Regex.
- `stripIndent()` und `translateEscapes()` kennen — sie sind die Hilfsmethoden, die Text Blocks intern nutzen.
- `\s` am Zeilenende erzwingt einen trailing Space, der sonst entfernt würde (nützlich für formatierte Ausgaben).
- In Tests: Text Blocks machen erwartete JSON/XML-Antworten im `assertThat` sofort lesbar.

