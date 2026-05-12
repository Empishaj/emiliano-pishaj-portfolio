# QG-JAVA-005 — Text Blocks für lesbare mehrzeilige Strings

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-005 |
| Titel | Text Blocks für lesbare mehrzeilige Strings |
| Status | Accepted / verbindlicher Standard für mehrzeilige statische String-Literale in neuen Java-Codebasen |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, SaaS-Plattformen, Clean Code, SQL-Lesbarkeit, Testdaten, JSON/XML/HTML/GraphQL-Fragmente, sichere String-Verarbeitung |
| Java-Baseline | Java 21+ als Kompendium-Standard; Text Blocks sind seit Java 15 finaler Bestandteil der Sprache |
| Primäres Java-Feature | Text Blocks gemäß JEP 378 |
| Verwandte Java-Features | `String::formatted`, `String::stripIndent`, `String::translateEscapes`, reguläre String-Literale, Prepared Statements, Template Engines |
| Letzte Validierung | 2026-05-02 |
| Validierte Quellenbasis | OpenJDK JEP 378, OpenJDK Programmer’s Guide to Text Blocks, Oracle Java SE Documentation, Oracle Java `String` API, OWASP SQL Injection Prevention Cheat Sheet, OWASP Query Parameterization Cheat Sheet, OWASP Cross-Site Scripting Prevention Cheat Sheet, OWASP Logging Cheat Sheet |
| Technische Beispielvalidierung | Die zentralen Java-21-Beispiele ohne externe Framework-Abhängigkeiten wurden mit `javac --release 21` syntaktisch geprüft. |
| Verbindlichkeit | Diese Richtlinie gilt verbindlich für neue mehrzeilige statische String-Literale. Abweichungen sind zulässig, wenn ein konkreter technischer, fachlicher oder sicherheitsbezogener Grund im Pull Request nachvollziehbar dokumentiert wird. |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wann Java Text Blocks verwendet werden sollen, wie sie korrekt formatiert werden und welche Security-Grenzen beim Einsatz mit SQL, JSON, HTML, XML, GraphQL, YAML und Testdaten gelten.

Text Blocks lösen ein altes Java-Problem: Mehrzeilige Inhalte mussten früher häufig über String-Konkatenation, manuelle `\n`-Sequenzen und escaped Anführungszeichen aufgebaut werden. Dadurch sah ein SQL-Statement im Code nicht wie SQL aus, ein JSON-Beispiel nicht wie JSON und ein HTML-Fragment nicht wie HTML. Genau an dieser Stelle entstehen unnötige Review-Kosten, Diff-Rauschen, Copy-Paste-Fehler, falsche Einrückung und fehlerhafte Escape-Sequenzen.

Der Zweck dieser Richtlinie ist nicht, jede String-Erzeugung durch Text Blocks zu ersetzen. Ziel ist, mehrzeilige statische Inhalte so zu schreiben, dass ihre visuelle Struktur im Java-Code möglichst exakt der tatsächlichen Struktur des Strings entspricht. Dadurch werden Lesbarkeit, Review-Fähigkeit und Wartbarkeit verbessert.

Diese Richtlinie stellt außerdem klar: Text Blocks sind ein Lesbarkeits- und Korrektheitswerkzeug für String-Literale. Sie sind kein Sicherheitsmechanismus. Sie verhindern keine SQL Injection, keine Cross-Site-Scripting-Schwachstellen, keine unsichere Template-Erzeugung und keine fehlerhafte Parametrisierung.

---

## 2. Kurzregel für Entwickler

Verwende Text Blocks für statische oder überwiegend statische Strings mit mehr als einer fachlichen Inhaltszeile, insbesondere für SQL, JSON, XML, HTML, YAML, GraphQL, Markdown und erwartete Testausgaben.

Verwende Text Blocks nicht, um ungeprüfte Nutzereingaben direkt in SQL, HTML, JSON, XML, Shell-Kommandos, LDAP-Queries, GraphQL-Queries oder andere interpretierte Sprachen einzubauen.

Nutze Text Blocks für die Struktur. Nutze Parametrisierung, Binding, Encoding oder eine Template Engine für dynamische Daten.

Ein guter Text Block macht den Inhalt lesbarer. Ein schlechter Text Block macht unsichere String-Interpolation nur hübscher.

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

Ein mehrzeiliger statischer String MUSS als Text Block geschrieben werden, wenn alle folgenden Bedingungen erfüllt sind:

1. Der String enthält mehr als eine fachliche Inhaltszeile.
2. Die visuelle Struktur des Inhalts ist für Verständnis, Review oder Wartung relevant.
3. Der Inhalt ist SQL, JSON, XML, HTML, YAML, GraphQL, Markdown, Test-Erwartungstext oder ein anderes textuelles Format.
4. Es gibt keinen Framework-Grund, der ein anderes Format verlangt.
5. Dynamische Werte werden sicher über geeignete Mechanismen eingefügt.

Bei SQL MUSS Nutzereingabe über Prepared Statements, Named Parameters, Criteria APIs oder vergleichbare Parametrisierung gebunden werden. `String::formatted`, Konkatenation und direkte Interpolation sind für untrusted SQL-Werte verboten.

Bei HTML, XML und anderen Ausgabeformaten MUSS untrusted data kontextgerecht encodiert oder über eine sichere Template Engine verarbeitet werden. Text Blocks ersetzen kein Output Encoding.

Bei JSON SOLLTE für produktive Payload-Erzeugung ein JSON-Serializer verwendet werden. Text Blocks sind für statische Beispiele, Tests, Dokumentation und einfache interne Fixtures geeignet, aber nicht als allgemeiner Ersatz für JSON-Bibliotheken.

### 3.2 Darf-nicht-Regeln

Ein Text Block DARF NICHT verwendet werden, um Sicherheitskontrollen zu umgehen oder dynamische Inhalte unkontrolliert in interpretierte Sprachen einzubauen.

Insbesondere ist verboten:

1. SQL mit Nutzereingaben per `.formatted(...)` oder Konkatenation zu bauen.
2. HTML mit unescaped Nutzereingaben per `.formatted(...)` zu erzeugen.
3. JSON mit unescaped Strings per `.formatted(...)` zu erzeugen, wenn Werte aus externen Quellen stammen.
4. Shell-, LDAP-, XPath-, GraphQL- oder Regex-Ausdrücke mit untrusted input unkontrolliert zusammenzubauen.
5. Secrets, Tokens, Passwörter oder personenbezogene Daten in Test-Text-Blocks, Logs oder Beispielpayloads dauerhaft abzulegen.
6. Große produktive Templates in Java-Code zu verstecken, wenn eine Template-Datei oder Template Engine fachlich sinnvoller ist.
7. Text Blocks als Ersatz für Validierung, Parametrisierung oder Encoding zu behandeln.

### 3.3 Sollte-Regeln

Text Blocks SOLLTEN verwendet werden für:

1. SQL-Statements mit mehreren Zeilen.
2. GraphQL-Queries und -Mutations.
3. JSON- und XML-Fixtures in Tests.
4. HTML-Fragmente in Tests oder sehr kleinen technischen E-Mails.
5. YAML- oder Markdown-Testdaten.
6. erwartete mehrzeilige Fehlermeldungen oder Reports in Tests.
7. dokumentierte Beispielpayloads in Entwicklerwerkzeugen.
8. Query-Strings, bei denen Struktur und Einrückung Teil der Lesbarkeit sind.

Text Blocks SOLLTEN NICHT verwendet werden für:

1. Einzeilige Strings.
2. komplexe dynamische Templates.
3. produktive HTML-Seiten.
4. produktive E-Mail-Templates mit Lokalisierung, Varianten und Layoutlogik.
5. SQL, das zur Laufzeit durch unkontrollierte String-Operationen zusammengesetzt wird.
6. internationale Texte, die eigentlich in Message Bundles gehören.
7. große statische Ressourcen, die besser als Datei im Classpath liegen.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Java-Code in Applikations-, Service-, Integrations-, Test- und Präsentationsschichten.

Sie gilt insbesondere für:

- SQL-Statements in Repository-, DAO- und Migration-Kontexten;
- JSON-, XML- und YAML-Fixtures in Unit- und Integrationstests;
- GraphQL-Queries;
- HTML- und Markdown-Fragmente in Tests, Dokumentationsgeneratoren oder technischen Nachrichten;
- erwartete API-Antworten in Tests;
- mehrzeilige Fehlermeldungen;
- kleine statische Templates ohne komplexe Logik;
- interne Entwicklerwerkzeuge;
- Beispielpayloads in Test- und Demo-Code.

Sie gilt nicht automatisch für:

- produktive HTML-Templates mit komplexer Logik;
- UI-Templates in Wicket, Thymeleaf, FreeMarker oder vergleichbaren Frameworks;
- E-Mail-Templates mit Lokalisierung, Branding und Variantenauswahl;
- große statische Dateien;
- Message Bundles und Übersetzungen;
- SQL-Generierung mit dynamischen Bedingungen, wenn ein Query Builder fachlich besser geeignet ist;
- JSON-Erzeugung für produktive API-Aufrufe, wenn ein Serializer verwendet werden sollte.

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Text Block | Mehrzeiliges String-Literal in Java, begrenzt durch drei doppelte Anführungszeichen. | `""" ... """` |
| Incidental Whitespace | Einrückung, die durch die Position des Java-Codes entsteht und vom Compiler entfernt werden kann. | Einrückung des Java-Blocks im Methodenkörper |
| Essential Whitespace | Leerzeichen, die fachlich Teil des Inhalts sind und erhalten bleiben müssen. | Einrückung in YAML, Markdown, ASCII-Tabellen |
| Trailing Newline | Zeilenumbruch am Ende eines Text Blocks, wenn das schließende `"""` in einer eigenen Zeile steht. | `"abc\n"` |
| Escape-Sequenz | Spezielle Zeichenfolge, die nach Whitespace-Verarbeitung übersetzt wird. | `\n`, `\t`, `\s`, `\"` |
| Parameterisierung | Sichere Übergabe dynamischer Werte an eine interpretierte Sprache, ohne sie in den Quelltext der Sprache einzukleben. | Prepared Statement mit `?` oder Named Parameter |
| Output Encoding | Kontextgerechte Kodierung von Ausgabedaten, damit untrusted data als Daten und nicht als ausführbarer Code interpretiert wird. | HTML-Encoding von `<script>` |

---

## 6. Technischer Hintergrund

Text Blocks wurden mit JEP 378 in Java 15 finalisiert. Sie sind zur Laufzeit normale `String`-Objekte. Der Unterschied liegt nur in der Quellcode-Repräsentation. Statt viele Zeilen mit `+`, `\n` und escaped Anführungszeichen zu schreiben, kann der mehrzeilige Inhalt direkt im Code dargestellt werden.

Ein Text Block beginnt mit drei doppelten Anführungszeichen, gefolgt von einem Zeilenumbruch. Der eigentliche Inhalt beginnt erst in der nächsten Zeile. Das schließende Delimiter `"""` kann die Einrückungsbaseline beeinflussen.

Ein einfaches Beispiel:

```java
String sql = """
        SELECT u.id, u.name
        FROM users u
        WHERE u.active = true
        ORDER BY u.name
        """;
```

Der Compiler verarbeitet Text Blocks im Wesentlichen in mehreren Schritten:

1. Zeilenenden werden normalisiert.
2. Gemeinsame incidental whitespace wird entfernt.
3. Escape-Sequenzen werden interpretiert.

Dadurch kann Java-Code sauber eingerückt bleiben, ohne dass diese Einrückung automatisch Bestandteil des erzeugten Strings wird.

Wichtig ist: Text Blocks verändern nicht die Sicherheit, Semantik oder Lebensdauer eines Strings. Ein Text Block ist am Ende nur ein `String`. Alles, was für normale Strings gefährlich ist, bleibt auch bei Text Blocks gefährlich.

---

## 7. Gute Anwendung

### 7.1 SQL als lesbarer Text Block

Gute SQL-Statements sollen im Java-Code wie SQL aussehen. Dadurch erkennt der Reviewer Joins, Filter, Sortierung und Parameter schneller.

```java
String sql = """
        SELECT u.id, u.name, o.created_at, o.total
        FROM users u
        JOIN orders o ON o.user_id = u.id
        WHERE u.active = true
          AND o.total > :minTotal
        ORDER BY o.created_at DESC
        """;
```

Dieses Beispiel ist gut, weil die SQL-Struktur sichtbar bleibt. Der Parameter `:minTotal` wird nicht per String-Formatierung eingesetzt, sondern soll über das verwendete Datenzugriffsframework gebunden werden.

### 7.2 JSON-Fixture im Test

Text Blocks eignen sich sehr gut für erwartete JSON-Antworten in Tests.

```java
String expectedJson = """
        {
          "id": 42,
          "name": "Ada Lovelace",
          "active": true
        }
        """;
```

Für Tests ist das deutlich lesbarer als escaped JSON in einer einzigen Zeile.

### 7.3 HTML-Fragment in einem Test

```java
String expectedHtml = """
        <html>
          <body>
            <h1>Hello, Ada!</h1>
          </body>
        </html>
        """;
```

Das ist für kleine Testfragmente in Ordnung. Für produktive HTML-Seiten sollte ein Template-System verwendet werden.

### 7.4 GraphQL-Query

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

Bei GraphQL wird die Lesbarkeit deutlich erhöht, weil Query-Struktur und Feldverschachtelung direkt sichtbar bleiben.

### 7.5 Mehrzeilige Fehlermeldung

```java
String message = """
        Import fehlgeschlagen.

        Ursache:
        - Pflichtfeld 'customerId' fehlt.
        - Feld 'amount' muss positiv sein.

        Bitte korrigiere die Eingabedatei und starte den Import erneut.
        """;
```

Mehrzeilige Fehlermeldungen sollten nur dann im Code stehen, wenn sie nicht übersetzt werden müssen und nicht Teil einer UI-Lokalisierung sind.

---

## 8. Falsche Anwendung und Anti-Patterns

### 8.1 SQL Injection durch `.formatted(...)`

```java
String sql = """
        SELECT *
        FROM users
        WHERE name = '%s'
        """.formatted(userInput);
```

Dieses Beispiel ist gefährlich. Der Text Block macht die SQL-Struktur lesbar, aber die Nutzereingabe wird direkt in das SQL-Statement eingebaut. Wenn `userInput` den Wert `' OR '1'='1` enthält, kann sich die Bedeutung der Query verändern.

Korrekt ist Parametrisierung:

```java
String sql = """
        SELECT *
        FROM users
        WHERE name = ?
        """;
```

Der Wert wird anschließend über `PreparedStatement#setString(...)`, Named Parameters oder das jeweilige Datenzugriffsframework gebunden.

### 8.2 HTML Injection durch direkte Formatierung

```java
String html = """
        <html>
          <body>
            <h1>Hello, %s!</h1>
          </body>
        </html>
        """.formatted(userProvidedName);
```

Dieses Beispiel ist nur dann ungefährlich, wenn `userProvidedName` bereits vertrauenswürdig und für den HTML-Kontext sicher encodiert ist. Bei untrusted data kann daraus Cross-Site Scripting entstehen.

Besser ist eine Template Engine mit kontextgerechtem Escaping oder explizites Output Encoding.

### 8.3 JSON-Erzeugung mit unescaped Strings

```java
String json = """
        {
          "userId": "%s",
          "comment": "%s"
        }
        """.formatted(userId, comment);
```

Dieses Beispiel ist gefährlich, wenn `comment` Anführungszeichen, Backslashes oder Steuerzeichen enthält. Dann kann ungültiges JSON entstehen oder die Struktur verändert werden.

Für produktive JSON-Erzeugung ist ein JSON-Serializer zu verwenden.

### 8.4 Text Block als verstecktes Riesentemplate

```java
String page = """
        <html>
          <!-- 400 Zeilen HTML, CSS und JavaScript -->
        </html>
        """;
```

Das ist kein Clean Code. Große Templates gehören in eigene Template-Dateien oder Ressourcen. Java-Code sollte nicht als Ablageort für umfangreiche UI-Strukturen missbraucht werden.

### 8.5 Text Blocks für einzeilige Strings

```java
String name = """
        Ada
        """;
```

Das ist unnötig und kann wegen des impliziten Zeilenumbruchs sogar zu unerwartetem Verhalten führen. Für einfache einzeilige Strings bleibt das normale String-Literal richtig.

---

## 9. Security- und Datenschutzrelevanz

Text Blocks erhöhen Lesbarkeit. Sie erhöhen nicht automatisch Sicherheit.

### 9.1 SQL Injection

Text Blocks dürfen SQL lesbarer machen. Sie dürfen aber niemals dazu führen, dass Nutzereingaben direkt in SQL eingesetzt werden. OWASP empfiehlt Prepared Statements beziehungsweise parametrisierte Queries als primäre Verteidigung gegen SQL Injection.

Falsch:

```java
String sql = """
        SELECT id, email
        FROM users
        WHERE tenant_id = '%s'
          AND email = '%s'
        """.formatted(tenantId, email);
```

Richtig:

```java
String sql = """
        SELECT id, email
        FROM users
        WHERE tenant_id = ?
          AND email = ?
        """;
```

Der Tenant-Kontext und die E-Mail-Adresse werden gebunden, nicht formatiert.

### 9.2 Cross-Site Scripting

Bei HTML, SVG, XML, JavaScript und CSS ist Kontext entscheidend. Derselbe Wert muss je nach Ausgabekontext unterschiedlich encodiert werden. Ein Text Block kennt diesen Kontext nicht. Er ist nur ein String.

Falsch:

```java
String html = """
        <a href="%s">Profil öffnen</a>
        """.formatted(userProvidedUrl);
```

Dieses Beispiel kann gefährlich werden, wenn `userProvidedUrl` nicht validiert und passend encodiert wird.

Richtig ist je nach Kontext:

- sichere Template Engine verwenden;
- URL validieren;
- Output kontextgerecht encodieren;
- keine untrusted data in gefährliche Kontexte einfügen.

### 9.3 Secrets und personenbezogene Daten

Text Blocks werden häufig in Tests verwendet. Dadurch besteht die Gefahr, echte Tokens, echte Kundendaten, echte E-Mail-Adressen oder produktive IDs in Test-Fixtures zu kopieren.

Verboten sind:

- echte Passwörter;
- echte API Keys;
- echte Access Tokens;
- echte Refresh Tokens;
- produktive Kundendaten;
- produktive Tenant-IDs;
- echte Zahlungsdaten;
- personenbezogene Daten ohne klare Testdatenfreigabe.

Testdaten müssen künstlich, minimal und zweckgebunden sein.

### 9.4 Logging

Text Blocks können mehrzeilige Log-Nachrichten verführerisch einfach machen. Das darf nicht dazu führen, dass sensible Payloads vollständig geloggt werden.

Falsch:

```java
log.info("""
        Zahlungsantwort erhalten:
        %s
        """.formatted(rawPaymentProviderResponse));
```

Besser:

```java
log.info("""
        Zahlungsantwort erhalten.
        provider={}
        correlationId={}
        status={}
        """, providerName, correlationId, status);
```

Logs sollen diagnostisch nützlich sein, aber keine vollständigen sensiblen Nutzdaten enthalten.

---

## 10. SaaS- und Plattformaspekte

In SaaS-Plattformen treten Text Blocks häufig in SQL-, Reporting-, E-Mail-, Import-, Export-, API- und Testkontexten auf. Gerade dort ist die Grenze zwischen Lesbarkeit und Sicherheitsrisiko wichtig.

### 10.1 Tenant-Kontext nicht per String bauen

Mandantenfilter dürfen nicht durch String-Konkatenation oder `.formatted(...)` in SQL eingebaut werden.

Falsch:

```java
String sql = """
        SELECT *
        FROM orders
        WHERE tenant_id = '%s'
        """.formatted(currentTenantId);
```

Richtig:

```java
String sql = """
        SELECT *
        FROM orders
        WHERE tenant_id = :tenantId
        """;
```

Der Tenant-Wert wird gebunden und muss zusätzlich aus einem vertrauenswürdigen serverseitigen Kontext stammen. Ein Tenant aus einem Request-DTO darf nicht blind übernommen werden.

### 10.2 Reporting Queries sauber strukturieren

Text Blocks sind gut geeignet für längere Reporting Queries, sofern Parameter gebunden werden.

```java
String sql = """
        SELECT
            c.id AS customer_id,
            c.name AS customer_name,
            COUNT(o.id) AS order_count,
            SUM(o.total) AS total_revenue
        FROM customers c
        JOIN orders o ON o.customer_id = c.id
        WHERE c.tenant_id = :tenantId
          AND o.created_at >= :from
          AND o.created_at < :to
        GROUP BY c.id, c.name
        ORDER BY total_revenue DESC
        """;
```

Die Lesbarkeit steigt, ohne die Sicherheit zu verschlechtern, weil dynamische Werte Parameter bleiben.

### 10.3 Feature Flags, E-Mail und Templates

Kleine technische Nachrichten können als Text Block sinnvoll sein. Produktive E-Mail-Templates, mandantenspezifische Templates und lokalisierte Texte gehören in Template-Systeme oder Ressourcen.

Ein Text Block im Java-Code ist nicht geeignet für:

- pro Tenant anpassbares Branding;
- mehrsprachige Texte;
- A/B-Varianten;
- Marketing-E-Mails;
- lange HTML-Templates;
- Inhalte, die von Fachbereichen gepflegt werden sollen.

---

## 11. Framework- und Tooling-Kontext

### 11.1 JDBC und JPA

Text Blocks sind sehr gut für SQL-Definitionen geeignet. Sie ändern aber nichts am Sicherheitsmodell. Bei JDBC sind `PreparedStatement` und Parameterbindung zu verwenden. Bei JPA, Hibernate oder Spring Data sind Named Parameters, Criteria API oder Repository-Mechanismen zu bevorzugen.

### 11.2 Spring Boot

In Spring-Boot-Anwendungen können Text Blocks für SQL, JSON-Test-Fixtures, GraphQL-Queries und erwartete Response Bodies genutzt werden. Für produktive HTML- oder E-Mail-Templates sind Thymeleaf, FreeMarker oder andere Template Engines meist besser geeignet.

### 11.3 Apache Wicket

In Wicket-Anwendungen gehören größere HTML-Strukturen grundsätzlich in Wicket-Markup-Dateien, nicht in Java-Text-Blocks. Text Blocks können aber in Tests, kleinen technischen Fragmenten oder erwarteten Markup-Snapshots sinnvoll sein.

### 11.4 JSON-Bibliotheken

Für produktive JSON-Erzeugung sind Bibliotheken wie Jackson oder JSON-B zu verwenden. Text Blocks sind geeignet für:

- statische Test-Fixtures;
- erwartete JSON-Antworten;
- Dokumentationsbeispiele;
- kleine interne Testpayloads.

Sie sind nicht geeignet als genereller JSON-Builder.

### 11.5 IDE-Support

Moderne IDEs können in Text Blocks häufig Sprachinjektion, Syntaxhervorhebung und Formatierung für SQL, JSON, HTML oder GraphQL anbieten. Das ist ein zusätzlicher Qualitätsgewinn, ersetzt aber keine Security-Prüfung.

---

## 12. Technische Detailregeln

### 12.1 Öffnender Delimiter

Nach dem öffnenden `"""` muss ein Zeilenumbruch folgen. Der Inhalt beginnt nicht auf derselben Zeile.

Falsch:

```java
String invalid = """Hello""";
```

Richtig:

```java
String valid = """
        Hello
        """;
```

### 12.2 Trailing Newline beachten

Wenn das schließende `"""` auf einer eigenen Zeile steht, enthält der Text Block normalerweise einen Zeilenumbruch am Ende.

```java
String value = """
        Hello
        """;
```

Der String entspricht sinngemäß `"Hello\n"`.

Wenn kein finaler Zeilenumbruch gewünscht ist, kann der Zeilenabschluss mit `\` unterdrückt werden:

```java
String value = """
        Hello\
        """;
```

Dieser String entspricht sinngemäß `"Hello"`.

### 12.3 Einrückung bewusst kontrollieren

Das schließende `"""` beeinflusst, welche Einrückung als incidental whitespace entfernt wird.

```java
String value = """
        Zeile 1
        Zeile 2
        """;
```

Die gemeinsame Einrückung wird entfernt. Der Inhalt beginnt mit `Zeile 1`.

Wenn die schließenden Anführungszeichen weiter links stehen, kann mehr Einrückung im Inhalt erhalten bleiben.

```java
String value = """
        Zeile 1
        Zeile 2
""";
```

Dieses Muster sollte nur bewusst verwendet werden, wenn führende Leerzeichen fachlich relevant sind.

### 12.4 Trailing Spaces mit `\s`

Trailing Spaces sind im Code schwer sichtbar. Wenn ein Leerzeichen am Zeilenende fachlich erforderlich ist, soll es explizit mit `\s` markiert werden.

```java
String columns = """
        A     \s
        B     \s
        C     \s
        """;
```

Dieses Muster ist selten nötig, aber besser als unsichtbare Leerzeichen.

### 12.5 `String::formatted` vorsichtig verwenden

`String::formatted` kann für einfache, vertrauenswürdige Formatierungen sinnvoll sein.

```java
String message = """
        Benutzer %s wurde erfolgreich importiert.
        Import-ID: %s
        """.formatted(displayName, importId);
```

Wichtig: `String::formatted` ist nicht compile-time-typsicher. Formatfehler können zur Laufzeit auftreten. Noch wichtiger: `.formatted(...)` ist keine sichere Parametrisierung für SQL, HTML, JSON, XML, Shell-Kommandos oder ähnliche Kontexte.

---

## 13. Codebeispiele

### 13.1 Schlechtes Beispiel: SQL mit Konkatenation

```java
String sql = "SELECT u.id, u.name, o.created_at, o.total\n" +
             "FROM users u\n" +
             "JOIN orders o ON o.user_id = u.id\n" +
             "WHERE u.active = true\n" +
             "  AND o.total > :minTotal\n" +
             "ORDER BY o.created_at DESC";
```

Dieses Beispiel ist schwerer zu lesen, schwerer zu diffen und fehleranfälliger als ein Text Block.

### 13.2 Gutes Beispiel: SQL mit Text Block und Parameter

```java
String sql = """
        SELECT u.id, u.name, o.created_at, o.total
        FROM users u
        JOIN orders o ON o.user_id = u.id
        WHERE u.active = true
          AND o.total > :minTotal
        ORDER BY o.created_at DESC
        """;
```

### 13.3 Schlechtes Beispiel: JSON per Konkatenation

```java
String json = "{\"userId\": " + userId + ", " +
              "\"action\": \"" + action + "\", " +
              "\"timestamp\": \"" + timestamp + "\"}";
```

Dieses Muster ist unleserlich und fehleranfällig. Außerdem kann es bei nicht sauber escaped Werten ungültiges JSON erzeugen.

### 13.4 Gutes Beispiel: JSON-Testfixture

```java
String json = """
        {
          "userId": 42,
          "action": "LOGIN",
          "timestamp": "2026-05-02T12:00:00Z"
        }
        """;
```

### 13.5 Vorsichtiges Beispiel: formatierte technische Nachricht

```java
String report = """
        Verarbeitung abgeschlossen.

        Import-ID: %s
        Erfolgreich: %d
        Fehlgeschlagen: %d
        """.formatted(importId, successCount, failureCount);
```

Das ist in Ordnung, wenn die Werte nicht in eine gefährliche interpretierte Sprache eingebettet werden und keine sensiblen Daten enthalten.

---

## 14. Review-Checkliste

Im Pull Request soll bei Text Blocks Folgendes geprüft werden:

1. Enthält der String mehr als eine fachliche Inhaltszeile?
2. Verbessert der Text Block die Lesbarkeit tatsächlich?
3. Ist die Einrückung korrekt und bewusst?
4. Ist der finale Zeilenumbruch gewünscht?
5. Wird `\` zur Unterdrückung des finalen Zeilenumbruchs bewusst verwendet?
6. Werden trailing spaces vermieden oder mit `\s` bewusst markiert?
7. Enthält der Text Block SQL?
8. Werden SQL-Parameter sicher gebunden?
9. Wird `.formatted(...)` bei SQL vermieden?
10. Enthält der Text Block HTML, XML, SVG, JavaScript oder CSS?
11. Werden untrusted values kontextgerecht encodiert?
12. Enthält der Text Block JSON?
13. Wäre ein JSON-Serializer statt String-Formatierung besser?
14. Enthält der Text Block Secrets, Tokens, Passwörter oder echte personenbezogene Daten?
15. Gehört der Inhalt wirklich in Java-Code oder besser in eine externe Ressource?
16. Ist der Text Block zu groß für sinnvolle Code-Reviews?
17. Ist der Text lokalisierungspflichtig?
18. Wird der Text Block geloggt?
19. Kann der Inhalt Mandanten- oder Nutzerdaten offenlegen?
20. Gibt es Tests für relevante Formatierungs- oder Parsing-Annahmen?

---

## 15. Automatisierbare Prüfungen

Text-Block-Qualität lässt sich teilweise automatisieren. Nicht jede Regel ist vollständig durch Tools prüfbar, aber mehrere Risiken können markiert werden.

### 15.1 Semgrep-Prüfideen

Mögliche Semgrep-Regeln:

- `.formatted(...)` auf SQL-Text-Blocks markieren;
- `.formatted(...)` auf HTML-Text-Blocks mit Variablen markieren;
- Text Blocks mit Begriffen wie `password`, `secret`, `token`, `apiKey`, `authorization` markieren;
- `SELECT` plus `%s` im selben Text Block markieren;
- `WHERE` plus String-Formatierung markieren;
- `<script`, `<a href`, `onclick`, `style=` plus `.formatted(...)` markieren.

### 15.2 Checkstyle- oder PMD-Prüfideen

Mögliche Prüfungen:

- String-Konkatenation mit `\n` ab bestimmter Länge markieren;
- mehrzeilige String-Konkatenationen als Refactoring-Kandidat markieren;
- Text Blocks mit sehr vielen Zeilen markieren;
- Text Blocks in produktiven Klassen mit HTML-Strukturen markieren;
- sehr lange Text Blocks außerhalb von Testcode markieren.

### 15.3 Secret Scanning

Repository Secret Scanning MUSS auch Text Blocks erfassen. Tokens und Credentials können in Text Blocks genauso versehentlich eingecheckt werden wie in normalen Strings.

### 15.4 Review-Gates

Für Pull Requests gilt:

- SQL-Text-Blocks mit dynamischen Werten benötigen Nachweis der Parameterbindung.
- HTML-/XML-Text-Blocks mit dynamischen Werten benötigen Nachweis des Encodings oder Template-Konzepts.
- Text Blocks mit produktionsnahen Testdaten benötigen Datenschutzprüfung.
- Große Text Blocks benötigen Begründung, warum keine externe Ressource verwendet wird.

---

## 16. Migration bestehender Strings

### 16.1 Wann migrieren?

Bestehende Strings sollen auf Text Blocks migriert werden, wenn:

1. sie mehrere fachliche Zeilen enthalten;
2. sie viele `\n`-Sequenzen enthalten;
3. sie viele escaped Anführungszeichen enthalten;
4. ihre Struktur im Review schwer erkennbar ist;
5. sie SQL, JSON, XML, HTML, YAML, GraphQL oder Markdown enthalten;
6. Tests wegen unlesbarer erwarteter Werte schwer wartbar sind.

### 16.2 Migrationsschritte

Eine sichere Migration erfolgt schrittweise:

1. Bestehenden String durch Tests absichern.
2. Tatsächlichen Zielstring sichtbar machen, zum Beispiel über Testausgabe oder Snapshot.
3. Text Block erstellen.
4. Einrückung prüfen.
5. Finalen Zeilenumbruch prüfen.
6. Escape-Sequenzen prüfen.
7. Tests ausführen.
8. Bei SQL prüfen, dass Parametrisierung erhalten bleibt.
9. Bei HTML/JSON/XML prüfen, dass Encoding oder Serialisierung nicht verschlechtert wurde.
10. Pull Request mit Hinweis auf reine Darstellungsänderung oder fachliche Änderung versehen.

### 16.3 Vorsicht bei Snapshot-Tests

Text Blocks können Snapshot-Tests verbessern, aber auch empfindlicher machen. Ein zusätzlicher finaler Zeilenumbruch kann Tests verändern. Deshalb muss bei Migration bewusst geprüft werden, ob der erwartete String mit oder ohne finalen Zeilenumbruch verglichen wird.

---

## 17. Ausnahmen

Eine klassische String-Schreibweise darf verwendet werden, wenn:

1. der String einzeilig ist;
2. der String sehr kurz ist;
3. der String aus fachlichen Gründen keinen finalen Zeilenumbruch enthalten darf und ein normales Literal klarer ist;
4. ein Framework eine bestimmte String-Form erwartet;
5. der Inhalt maschinell generiert wird;
6. eine externe Ressource, Template-Datei oder Message Bundle fachlich richtiger ist;
7. die Lesbarkeit durch einen Text Block nicht verbessert würde.

Eine Abweichung muss nicht für jeden kurzen String dokumentiert werden. Bei mehrzeiligen strukturierten Inhalten soll eine Abweichung im Pull Request begründet werden.

---

## 18. Definition of Done

Ein mehrzeiliger String erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Er ist bei strukturiertem mehrzeiligem Inhalt als Text Block formuliert.
2. Die visuelle Struktur im Code entspricht dem fachlichen Inhalt.
3. Die Einrückung ist bewusst und stabil.
4. Der finale Zeilenumbruch ist geprüft.
5. SQL-Werte werden parametrisiert und nicht formatiert.
6. HTML-/XML-/SVG-/JavaScript-Ausgaben behandeln untrusted data mit passendem Encoding oder Template-System.
7. JSON wird produktiv über einen Serializer erzeugt, sofern dynamische Daten enthalten sind.
8. Secrets und echte personenbezogene Daten sind nicht im Text Block enthalten.
9. Große Templates liegen nicht unnötig im Java-Code.
10. Tests prüfen relevante Formatierungsannahmen, wenn der exakte String fachlich wichtig ist.
11. Der Pull Request zeigt keine String-Konkatenations-Hölle mit `\n`, wenn ein Text Block klarer wäre.
12. Security-relevante dynamische Inhalte sind im Review explizit betrachtet worden.

---

## 19. Entscheidungsbaum

Verwende folgende Entscheidungslogik:

```text
Ist der String mehrzeilig?
├─ Nein → normales String-Literal verwenden.
└─ Ja
   ├─ Ist die Struktur für Lesbarkeit oder Review relevant?
   │  ├─ Nein → prüfen, ob externe Ressource besser ist.
   │  └─ Ja
   │     ├─ Enthält der String SQL?
   │     │  ├─ Ja → Text Block + Parameterbindung, keine direkte Interpolation.
   │     │  └─ Nein
   │     ├─ Enthält der String HTML/XML/SVG/JavaScript/CSS?
   │     │  ├─ Ja → Text Block nur mit Encoding/Template-Sicherheit.
   │     │  └─ Nein
   │     ├─ Enthält der String JSON/YAML?
   │     │  ├─ Testfixture → Text Block geeignet.
   │     │  └─ Produktiver Payload → Serializer bevorzugen.
   │     └─ Ist der Inhalt groß, lokalisiert oder fachlich pflegbar?
   │        ├─ Ja → externe Ressource oder Template-System verwenden.
   │        └─ Nein → Text Block verwenden.
```

---

## 20. Quellen und weiterführende Literatur

- OpenJDK JEP 378 — Text Blocks: https://openjdk.org/jeps/378
- OpenJDK Programmer’s Guide to Text Blocks: https://openjdk.org/projects/amber/guides/text-blocks-guide
- Oracle Programmer’s Guide to Text Blocks: https://docs.oracle.com/en/java/javase/18/text-blocks/index.html
- Oracle Java SE `String` API: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
- OWASP SQL Injection Prevention Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html
- OWASP Query Parameterization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Query_Parameterization_Cheat_Sheet.html
- OWASP Cross-Site Scripting Prevention Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
