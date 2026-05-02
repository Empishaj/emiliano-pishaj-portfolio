# QG-JAVA-003 — Pattern Matching und Switch Expressions richtig einsetzen

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-003 |
| Titel | Pattern Matching und Switch Expressions richtig einsetzen |
| Status | Accepted / verbindlicher Standard für neue Kontrollflusslogik über Typvarianten, Datenvarianten und geschlossene Domänentypen |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, SaaS-Plattformen, Domänenlogik, Service-Layer, API- und Integrationslogik, sichere Fallunterscheidung |
| Java-Baseline | Java 21+ als verbindlicher Standard für Pattern Matching in `switch`, Record Patterns und Guarded Patterns. Pattern Matching für `instanceof` ist seit Java 16 final; Switch Expressions sind seit Java 14 final; Pattern Matching for `switch` und Record Patterns sind seit Java 21 final. |
| Primäre Java-Features | Pattern Matching für `instanceof`, Switch Expressions, Pattern Matching for `switch`, Record Patterns |
| Verwandte Java-Features | Records, Sealed Classes, Sealed Interfaces, Enums, Exhaustiveness Checking, Guarded Patterns mit `when` |
| Letzte Validierung | 2026-05-02 |
| Validierte Quellenbasis | OpenJDK JEP 361, OpenJDK JEP 394, OpenJDK JEP 440, OpenJDK JEP 441, Oracle Java SE 21 Language Documentation, OWASP Input Validation Cheat Sheet, OWASP Logging Cheat Sheet, OWASP Deserialization Cheat Sheet, OWASP Secure Code Review Cheat Sheet |
| Technische Beispielvalidierung | Die zentralen Java-21-Beispiele ohne externe Framework-Abhängigkeiten wurden mit `javac --release 21` syntaktisch geprüft. |
| Verbindlichkeit | Diese Richtlinie gilt für neue Java-21+-Codepfade verbindlich, in denen Typprüfungen, Variantenbehandlung, geschlossene Domänentypen oder mehrarmige Fallunterscheidungen implementiert werden. Abweichungen sind zulässig, wenn ein technischer oder fachlicher Grund im Pull Request nachvollziehbar dokumentiert wird. |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Pattern Matching und Switch Expressions in modernem Java korrekt, lesbar, sicher und wartbar eingesetzt werden.

Das Ziel ist nicht, jeden `if`-Block durch einen `switch` zu ersetzen. Das Ziel ist, Kontrollfluss dort präziser auszudrücken, wo Code abhängig vom Typ, von der Form eines Datenobjekts oder von einer bekannten Menge fachlicher Varianten unterschiedliche Pfade ausführen muss.

Klassische Java-Implementierungen enthalten häufig drei typische Probleme:

1. Sie prüfen einen Typ mit `instanceof`, casten anschließend denselben Wert explizit und verwenden ihn danach.
2. Sie behandeln Varianten mit langen `if-else`-Ketten, obwohl ein `switch` die Absicht klarer ausdrücken würde.
3. Sie verlassen sich bei offenen Hierarchien auf einen `else`- oder `default`-Zweig, statt Vollständigkeit über das Typsystem und den Compiler sichtbar zu machen.

Pattern Matching reduziert diese Redundanz. Switch Expressions machen mehrarmige Fallunterscheidungen wertorientiert und verhindern klassisches Fall-through. In Kombination mit sealed Typen und Records entsteht ein sehr starkes Modellierungswerkzeug für Domänenlogik: Der Code kann ausdrücken, welche Varianten existieren, welche Daten sie tragen und welche Fälle behandelt werden müssen.

Diese Richtlinie soll Entwicklern helfen, im Alltag zuverlässig zu entscheiden:

- Wann reicht ein normales `if`?
- Wann ist Pattern Matching für `instanceof` sinnvoll?
- Wann ist ein `switch` besser als eine `if-else`-Kette?
- Wann muss ein `switch` exhaustiv sein?
- Wann ist ein `default`-Zweig hilfreich und wann verdeckt er Fehler?
- Wie werden `null`, Guards, Records und verschachtelte Datenstrukturen sauber behandelt?
- Welche Security- und SaaS-Risiken entstehen bei falsch modellierten Fallunterscheidungen?
- Wie prüft man solche Konstrukte im Pull Request?

---

## 2. Kurzregel für Entwickler

Verwende Pattern Matching für `instanceof`, wenn ein Objekt auf einen Typ geprüft und danach als dieser Typ verwendet wird.

Verwende Switch Expressions mit Arrow-Syntax (`->`), wenn ein Wert abhängig von mehreren klaren Fällen berechnet oder zurückgegeben wird.

Verwende Pattern Matching for `switch`, wenn mehrere Typvarianten oder Datenvarianten eines Wertes unterschiedlich behandelt werden müssen. Bei eigenen geschlossenen Hierarchien soll der `switch` exhaustiv sein und nach Möglichkeit keinen `default`-Zweig enthalten, damit der Compiler fehlende Varianten melden kann.

Verwende Record Patterns, wenn Records nicht nur als Ganzes erkannt, sondern ihre Komponenten direkt und lesbar ausgewertet werden sollen.

Verwende diese Sprachmittel nicht, um schlechte Modellierung zu kaschieren. Wenn der Code ständig auf `Object` switcht, viele fremde Typen unterscheidet oder fachliche Regeln über technische Typprüfungen verteilt, ist meist das Design zu prüfen.

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

1. Neue Typprüfung mit anschließender Nutzung des konkreten Typs MUSS mit Pattern Matching für `instanceof` geschrieben werden, sofern keine Kompatibilitätsgründe dagegen sprechen.
2. Neue mehrarmige Wertberechnungen SOLLEN als Switch Expression mit Arrow-Syntax geschrieben werden, wenn dies die Lesbarkeit erhöht.
3. Neue `switch`-Konstrukte in Java-21+-Code MÜSSEN grundsätzlich Arrow-Syntax (`->`) verwenden, wenn kein bewusst begründeter Grund für klassische Doppelpunkt-Syntax (`:`) vorliegt.
4. Bei `switch` über eigene sealed Hierarchien MUSS Vollständigkeit durch explizite Fälle erreicht werden. Ein `default`-Zweig DARF NICHT leichtfertig verwendet werden, weil er neue Varianten verdecken kann.
5. Wenn der Selector eines `switch` fachlich `null` sein kann, MUSS `case null` explizit behandelt oder `null` vor dem `switch` eindeutig ausgeschlossen werden.
6. Guarded Patterns mit `when` MÜSSEN von spezifisch nach allgemein sortiert werden.
7. Jeder `switch` über sicherheitsrelevante Ergebnis- oder Entscheidungsvarianten MUSS fail-closed modelliert sein. Ein unbekannter, nicht behandelter oder nicht validierter Zustand darf nicht stillschweigend als erlaubt interpretiert werden.
8. Pattern Matching DARF NICHT als Ersatz für Eingabevalidierung, Autorisierung oder Mandantenprüfung missverstanden werden.
9. Switch Expressions mit Block-Body MÜSSEN `yield` verwenden und dürfen keine versteckten Seiteneffekte als Hauptzweck haben.
10. Pull Requests mit neuen Pattern-Switches MÜSSEN prüfen, ob die Fallmenge fachlich vollständig, sicher und verständlich ist.

### 3.2 Darf-nicht-Regeln

1. Klassische `instanceof`-Prüfung mit anschließendem explizitem Cast DARF in neuem Code nicht verwendet werden, wenn Pattern Matching möglich ist.
2. Neue Switch Expressions DÜRFEN NICHT mit Fall-through-Logik konstruiert werden.
3. Ein `default`-Zweig DARF bei eigenen sealed Hierarchien nicht genutzt werden, um unklare Modellierung zu verstecken.
4. Ein `switch` DARF nicht auf unvalidierten externen JSON- oder API-Typinformationen beruhen, wenn daraus fachliche oder sicherheitsrelevante Entscheidungen entstehen.
5. Pattern Matching DARF NICHT dazu führen, dass Autorisierungs-, Tenant- oder Zugriffskontrollen über technische Klassenformen statt über explizite Sicherheitsregeln entschieden werden.
6. Record Patterns DÜRFEN nicht so tief verschachtelt werden, dass Fachlogik unlesbar wird.
7. `case null` DARF nicht verwendet werden, um unsaubere Null-Behandlung im System zu normalisieren. Er ist ein bewusstes Grenzfallwerkzeug, keine Einladung zu beliebigem `null`.
8. Logging in `default`-, Fehler- oder Fallback-Zweigen DARF keine sensiblen Werte aus dem gematchten Objekt ausgeben.
9. Ein `switch` über `Object` DARF nur verwendet werden, wenn der fachliche Kontext wirklich heterogene Typen erwartet.
10. Ein Pattern-Switch DARF nicht als Ersatz für Polymorphie verwendet werden, wenn Verhalten sauberer im Domänentyp selbst liegt.

### 3.3 Sollte-Regeln

1. Verwende `switch` als Ausdruck, wenn aus mehreren Fällen genau ein Wert entstehen soll.
2. Verwende `if` für einfache binäre Entscheidungen, besonders wenn keine Typvarianten unterschieden werden.
3. Verwende `enum`, wenn Varianten reine Konstanten sind und keine unterschiedlichen Daten tragen.
4. Verwende sealed Interfaces mit Records, wenn Varianten unterschiedliche Daten tragen und zur Compile-Zeit bekannt sind.
5. Verwende Record Patterns für kleine, gut erkennbare Dekonstruktionen.
6. Verwende sprechende Variablennamen in Patterns, nicht nur `x`, `y`, `o`, wenn der fachliche Kontext wichtig ist.
7. Sortiere Pattern Cases so, dass Leser vom Spezifischen zum Allgemeinen geführt werden.
8. Halte Case-Zweige klein. Wenn ein Case viel Logik enthält, extrahiere eine Methode.
9. Verwende `default` bei externen, offenen oder fremden Hierarchien defensiv mit explizitem Fehlerverhalten.
10. Prüfe bei jedem Pattern-Switch, ob ein besseres Domänenmodell den Kontrollfluss vereinfachen würde.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Java-Code in Applikations-, Domain-, Service-, API-, Integrations-, Präsentations- und Infrastruktur-nahen Schichten, sofern dort Typ- oder Variantenlogik implementiert wird.

Sie gilt insbesondere für:

- Service-Ergebnisverarbeitung;
- API-Request- und API-Response-Klassifikation;
- Mapping zwischen Domänen- und Transportmodellen;
- Fehler- und Ergebnisbehandlung;
- Autorisierungs- und Zugriffsergebnisverarbeitung;
- Zahlungs-, Bestell-, Buchungs- und Workflowlogik;
- Validierungsresultate;
- Import- und Exportprozesse;
- Event- und Command-Handling;
- Parser und Klassifizierer;
- Verarbeitung geschlossener Domänentypen;
- UI-Präsentationslogik, wenn Zustände klar unterschieden werden müssen.

Diese Richtlinie gilt nicht automatisch für:

- einfache binäre Bedingungen;
- rein numerische Berechnungen ohne Variantenlogik;
- Performance-kritische Hotspots ohne Messung und Designprüfung;
- polymorphe Domänenoperationen, die besser als Methode am Typ selbst modelliert werden;
- dynamische Plugin-Systeme;
- Regelwerke, deren Varianten zur Laufzeit konfiguriert werden;
- Code, der aus Kompatibilitätsgründen auf älteren Java-Versionen laufen muss;
- Framework-Code, der durch Bibliothekskonventionen eine bestimmte Form erzwingt.

---

## 5. Begriffe

| Aspekt | Details/Erklärung | Beispiel |
|---|---|---|
| Pattern Matching | Sprachmechanismus, bei dem ein Wert auf eine Form geprüft und passende Bestandteile direkt gebunden werden. | `obj instanceof String value` |
| Type Pattern | Pattern, das prüft, ob ein Wert einen bestimmten Typ hat, und ihn als Variable bindet. | `case Circle circle -> ...` |
| Pattern Variable | Variable, die durch ein erfolgreiches Pattern gebunden wird. | `circle` in `case Circle circle` |
| Switch Expression | `switch`, der einen Wert liefert. | `return switch (status) { ... };` |
| Switch Statement | `switch`, der Anweisungen ausführt, aber nicht zwingend einen Wert liefert. | `switch (status) { case A -> log(); }` |
| Arrow-Syntax | Moderne `switch`-Schreibweise mit `->`, ohne Fall-through. | `case SUCCESS -> handle();` |
| Doppelpunkt-Syntax | Klassische `switch`-Schreibweise mit `:`, historisch mit Fall-through. | `case SUCCESS: handle(); break;` |
| Exhaustiveness | Vollständigkeit eines `switch`: Alle möglichen Fälle sind abgedeckt. | sealed Typ ohne `default` vollständig gematcht |
| Guarded Pattern | Pattern mit zusätzlicher Bedingung über `when`. | `case Failure f when f.code() >= 500 -> ...` |
| Record Pattern | Pattern, das einen Record erkennt und seine Komponenten bindet. | `case Point(int x, int y) -> ...` |
| Dominance | Regel, dass allgemeinere Patterns spezifischere Patterns überdecken können. | `case Object o` vor `case String s` wäre problematisch |
| Selector | Ausdruck, über den der `switch` entscheidet. | `result` in `switch (result)` |
| Fall-through | Klassisches Verhalten, bei dem ein `case` ohne `break` in den nächsten fällt. | In neuer Arrow-Syntax nicht vorhanden |
| Fail-Closed | Sicheres Fehlerverhalten: Unbekannte oder nicht erlaubte Fälle werden abgelehnt. | Unbekannte Autorisierungsentscheidung wird nicht erlaubt |

---

## 6. Technischer Hintergrund

Pattern Matching ist ein Oberbegriff. In Java besteht die moderne Pattern-Matching-Familie aus mehreren Sprachfeatures, die zu unterschiedlichen Zeitpunkten finalisiert wurden.

| Feature | Java-Version | JEP | Bedeutung |
|---|---:|---:|---|
| Switch Expressions | Java 14 | JEP 361 | `switch` kann einen Wert liefern; Arrow-Syntax und `yield` werden eingeführt. |
| Pattern Matching für `instanceof` | Java 16 | JEP 394 | Typprüfung und Cast werden zu einem Pattern zusammengeführt. |
| Record Patterns | Java 21 | JEP 440 | Records können in Patterns destrukturiert werden. |
| Pattern Matching for `switch` | Java 21 | JEP 441 | `switch` kann Type Patterns, `case null` und Guards verwenden. |

Diese Richtlinie verwendet Java 21 als Baseline, weil erst dort die entscheidende Kombination aus Pattern Matching for `switch` und Record Patterns final verfügbar ist.

### 6.1 Pattern Matching für `instanceof`

Vor Pattern Matching musste Java-Code den gleichen Typ faktisch zweimal ausdrücken:

```java
if (obj instanceof String) {
    String value = (String) obj;
    System.out.println(value.toUpperCase());
}
```

Mit Pattern Matching wird daraus:

```java
if (obj instanceof String value) {
    System.out.println(value.toUpperCase());
}
```

Der Compiler prüft den Typ und stellt die Pattern Variable nur in dem Bereich bereit, in dem der Typ sicher bekannt ist.

### 6.2 Switch Expressions

Eine Switch Expression liefert einen Wert. Das ist ein wichtiger Unterschied zum klassischen Switch Statement.

```java
String label = switch (status) {
    case PENDING -> "Ausstehend";
    case SUCCESS -> "Erfolgreich";
    case FAILED -> "Fehlgeschlagen";
};
```

Das Ergebnis wird direkt zugewiesen oder zurückgegeben. Jeder Zweig muss einen Wert liefern. Das verhindert viele unvollständige Kontrollflüsse.

### 6.3 Pattern Matching for `switch`

Pattern Matching for `switch` erlaubt, nicht nur Konstanten, sondern Typformen und Datenformen zu unterscheiden.

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle circle -> Math.PI * circle.radius() * circle.radius();
        case Rectangle rectangle -> rectangle.width() * rectangle.height();
        case Triangle triangle -> triangle.base() * triangle.height() / 2.0;
    };
}
```

Besonders stark ist diese Form bei sealed Hierarchien, weil der Compiler die erlaubten Varianten kennt und Vollständigkeit prüfen kann.

### 6.4 Record Patterns

Record Patterns erlauben, Records direkt im Pattern zu dekonstruieren.

```java
String describe(Object value) {
    return switch (value) {
        case Point(int x, int y) -> "Punkt (%d, %d)".formatted(x, y);
        default -> "Unbekannt";
    };
}
```

Das ist lesbar, wenn die Struktur klein ist. Es wird unlesbar, wenn zu viele Ebenen ineinander verschachtelt werden.

---

## 7. Warum diese Sprachmittel die Codequalität erhöhen

Pattern Matching und Switch Expressions verbessern Codequalität nicht durch Modernität allein. Sie verbessern Codequalität, wenn sie Redundanz reduzieren, Kontrollfluss expliziter machen und Fehler früher sichtbar machen.

Der Qualitätsgewinn entsteht aus fünf Mechanismen:

1. Typprüfung und Cast werden zusammengeführt.
2. Mehrarmige Fallunterscheidungen werden als Wertberechnung formulierbar.
3. Arrow-Syntax verhindert unbeabsichtigtes Fall-through.
4. Sealed Typen ermöglichen exhaustiven `switch` ohne `default`.
5. Record Patterns erlauben, datenorientierte Strukturen kompakt und kontrolliert auszuwerten.

Ein besonders wichtiger Effekt ist die Verschiebung von Laufzeitfehlern zu Compile-Zeit-Feedback. Wenn eine neue Variante in eine sealed Hierarchie aufgenommen wird, kann der Compiler unvollständige Pattern-Switches melden. Das ist für geschäftskritische SaaS-Systeme wertvoll, weil vergessene Zustände oft nicht sofort auffallen, sondern erst unter seltenen Bedingungen in Produktion sichtbar werden.

---

## 8. Entscheidungsmatrix

| Aspekt | Details/Erklärung | Entscheidung |
|---|---|---|
| Einfache binäre Bedingung | Eine Bedingung, zwei Pfade, keine Typvariante. | Normales `if` verwenden |
| Typprüfung plus Nutzung | Objekt wird auf Typ geprüft und danach als dieser Typ verwendet. | `instanceof` Pattern verwenden |
| Mehrere konstante Statuswerte | Varianten sind reine Konstanten. | `enum` mit Switch Expression verwenden |
| Geschlossene Varianten mit unterschiedlichen Daten | Varianten tragen unterschiedliche Daten und sind zur Compile-Zeit bekannt. | Sealed Interface + Records + Pattern Switch verwenden |
| Offene Fremdhierarchie | Varianten können außerhalb des eigenen Codes entstehen. | Pattern Switch mit defensivem `default` oder Polymorphie prüfen |
| Komplexes Verhalten pro Variante | Verhalten gehört stabil zur Variante selbst. | Polymorphie statt großen externen `switch` prüfen |
| Externe Eingabe klassifizieren | Daten kommen aus API, JSON, Message Broker oder Formular. | Erst validieren, dann klassifizieren |
| `null` fachlich möglich | Selector kann wirklich `null` sein. | `case null` oder vorherige Null-Abwehr verwenden |
| Sensible Fehlerobjekte | Varianten enthalten Tokens, Secrets, personenbezogene Daten oder interne Fehlerdetails. | Kein unkontrolliertes Logging im `switch` |
| Sehr tiefe Record-Struktur | Pattern dekonstruieren mehrere Ebenen. | Lesbarkeit prüfen, ggf. Methode extrahieren |

---

## 9. Gute Anwendung: `instanceof` Pattern

### 9.1 Schlechtes Beispiel

```java
void printLength(Object value) {
    if (value instanceof String) {
        String text = (String) value;
        System.out.println(text.length());
    }
}
```

Dieses Beispiel ist unnötig redundant. Der Typ wird geprüft, danach erneut im Cast wiederholt. Der Cast kann falsch kopiert werden und erzeugt visuelles Rauschen.

### 9.2 Gutes Beispiel

```java
void printLength(Object value) {
    if (value instanceof String text) {
        System.out.println(text.length());
    }
}
```

Die Absicht ist klarer: Wenn `value` ein `String` ist, steht `text` direkt als `String` zur Verfügung.

### 9.3 Mit zusätzlicher Bedingung

```java
boolean isLongText(Object value) {
    return value instanceof String text && text.length() > 100;
}
```

Diese Form ist sinnvoll, wenn Typprüfung und einfache Zusatzbedingung zusammengehören. Wird die Bedingung komplexer, sollte sie in eine sprechende Methode ausgelagert werden.

---

## 10. Gute Anwendung: Switch Expression für Wertberechnung

### 10.1 Schlechtes Beispiel mit klassischem Switch

```java
String label(PaymentStatus status) {
    String label;

    switch (status) {
        case PENDING:
            label = "Ausstehend";
            break;
        case SUCCESS:
            label = "Erfolgreich";
            break;
        case FAILED:
            label = "Fehlgeschlagen";
            break;
        default:
            throw new IllegalArgumentException("Unbekannter Status: " + status);
    }

    return label;
}
```

Dieses Beispiel ist unnötig lang. Die Variable `label` existiert nur, weil das alte Switch Statement keinen Wert liefert. Außerdem muss `break` korrekt gesetzt werden.

### 10.2 Gutes Beispiel mit Switch Expression

```java
enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}

String label(PaymentStatus status) {
    return switch (status) {
        case PENDING -> "Ausstehend";
        case SUCCESS -> "Erfolgreich";
        case FAILED -> "Fehlgeschlagen";
    };
}
```

Diese Variante ist kürzer, aber vor allem präziser. Die Methode sagt: Aus einem `PaymentStatus` wird genau ein Text erzeugt.

### 10.3 Block-Case mit `yield`

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle circle -> {
            double radius = circle.radius();
            yield Math.PI * radius * radius;
        }
        case Rectangle rectangle -> rectangle.width() * rectangle.height();
        case Triangle triangle -> triangle.base() * triangle.height() / 2.0;
    };
}
```

`yield` wird verwendet, wenn innerhalb eines Case-Zweigs mehrere Anweisungen nötig sind und am Ende ein Wert geliefert wird. Wenn ein Case sehr groß wird, ist meistens eine private Methode besser.

---

## 11. Gute Anwendung: Pattern Matching for `switch`

### 11.1 Geschlossene Shape-Hierarchie

```java
sealed interface Shape permits Circle, Rectangle, Triangle {}

record Circle(double radius) implements Shape {}

record Rectangle(double width, double height) implements Shape {}

record Triangle(double base, double height) implements Shape {}
```

### 11.2 Exhaustiver Pattern Switch

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle circle -> Math.PI * circle.radius() * circle.radius();
        case Rectangle rectangle -> rectangle.width() * rectangle.height();
        case Triangle triangle -> triangle.base() * triangle.height() / 2.0;
    };
}
```

Bei einer sealed Hierarchie kennt der Compiler alle erlaubten direkten Varianten. Wenn später eine neue Variante ergänzt wird, muss dieser `switch` angepasst werden. Genau das ist erwünscht.

### 11.3 Warum hier kein `default` stehen sollte

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle circle -> Math.PI * circle.radius() * circle.radius();
        case Rectangle rectangle -> rectangle.width() * rectangle.height();
        case Triangle triangle -> triangle.base() * triangle.height() / 2.0;
        default -> throw new IllegalStateException("Unbekannte Form: " + shape);
    };
}
```

Der `default` wirkt defensiv, kann aber bei eigener sealed Hierarchie die gewünschte Compiler-Unterstützung schwächen. Wenn später `Square` ergänzt wird, ist der `switch` weiterhin syntaktisch vollständig, obwohl `Square` fachlich nicht korrekt behandelt wird. Das kann einen wichtigen Review-Hinweis verdecken.

Die Regel lautet deshalb: Bei eigenen sealed Hierarchien Fälle explizit behandeln und auf `default` verzichten, sofern kein außergewöhnlicher defensiver Grund besteht.

---

## 12. Gute Anwendung: Guarded Patterns mit `when`

Guarded Patterns kombinieren Typmuster mit einer zusätzlichen Bedingung.

```java
String describe(PaymentResult result) {
    return switch (result) {
        case Success success -> "Gebucht: " + success.transactionId();
        case Failure failure when failure.errorCode() >= 500 -> "Technischer Fehler: " + failure.reason();
        case Failure failure -> "Abgelehnt: " + failure.reason();
        case Pending pending -> "Ausstehend seit: " + pending.since();
    };
}
```

Die Reihenfolge ist wichtig. Der spezifischere `Failure`-Fall mit `errorCode() >= 500` muss vor dem allgemeinen `Failure`-Fall stehen. Sonst wäre der spezifische Fall unerreichbar oder fachlich wirkungslos.

Guarded Patterns sind gut, wenn die zusätzliche Bedingung einfach und fachlich unmittelbar verständlich ist. Sie sind schlecht, wenn komplexe Geschäftslogik direkt in den `case`-Header wandert.

Schlecht:

```java
String route(Order order) {
    return switch (order) {
        case Order o when o.customer() != null
                && o.customer().contract() != null
                && o.customer().contract().isActive()
                && o.total().signum() > 0
                && o.items().stream().noneMatch(Item::blocked) -> "PROCESS";
        default -> "REVIEW";
    };
}
```

Besser:

```java
String route(Order order) {
    return switch (order) {
        case Order o when canBeProcessed(o) -> "PROCESS";
        default -> "REVIEW";
    };
}

private boolean canBeProcessed(Order order) {
    return order.customer() != null
            && order.customer().contract() != null
            && order.customer().contract().isActive()
            && order.total().signum() > 0
            && order.items().stream().noneMatch(Item::blocked);
}
```

Die zweite Variante ist reviewbarer, testbarer und fachlich leichter diskutierbar.

---

## 13. Gute Anwendung: Record Patterns

Record Patterns eignen sich, wenn die Struktur eines Records klein und stabil ist.

```java
record Point(int x, int y) {}

String describe(Object value) {
    return switch (value) {
        case Point(int x, int y) when x == 0 && y == 0 -> "Ursprung";
        case Point(int x, int y) -> "Punkt (%d, %d)".formatted(x, y);
        default -> "Unbekannt";
    };
}
```

Verschachtelte Record Patterns sind möglich:

```java
record Line(Point start, Point end) {}

String describeLine(Object value) {
    return switch (value) {
        case Line(Point(int x1, int y1), Point(int x2, int y2)) ->
                "Linie von (%d,%d) nach (%d,%d)".formatted(x1, y1, x2, y2);
        default -> "Keine Linie";
    };
}
```

Diese Form ist elegant, solange sie lesbar bleibt. Wird die Dekonstruktion zu tief, sollte sie in Zwischenschritte oder Methoden zerlegt werden.

Schlecht:

```java
String classify(Object value) {
    return switch (value) {
        case Envelope(Header(String tenant, String channel), Payload(Order(Customer(String id, String name), Items items)))
                when tenant != null && channel != null && id != null -> "VALID";
        default -> "INVALID";
    };
}
```

Besser:

```java
String classify(Object value) {
    return switch (value) {
        case Envelope envelope when hasValidRouting(envelope) -> "VALID";
        default -> "INVALID";
    };
}
```

Nicht jede mögliche Sprachkompaktheit ist ein Qualitätsgewinn. Kompaktheit ist nur dann gut, wenn die fachliche Absicht klarer wird.

---

## 14. Null-Behandlung im Pattern Switch

Vor Pattern Matching for `switch` war `null` im klassischen `switch` problematisch, weil häufig eine `NullPointerException` entstand. Java 21 erlaubt `case null` im Pattern Switch.

```java
String classify(Object value) {
    return switch (value) {
        case null -> "Kein Wert";
        case String text when text.isBlank() -> "Leerer Text";
        case String text -> "Text: " + text;
        case Integer number -> "Zahl: " + number;
        default -> "Unbekannter Typ";
    };
}
```

Diese Möglichkeit ist hilfreich, aber sie darf nicht dazu führen, dass `null` überall als normale Domänenvariante akzeptiert wird.

Die Regel lautet:

1. Wenn `null` fachlich ein gültiger Eingangszustand ist, behandle ihn explizit mit `case null`.
2. Wenn `null` fachlich nicht erlaubt ist, weise ihn früh zurück, zum Beispiel mit `Objects.requireNonNull(...)`.
3. Verwende `case null` nicht, um fehlende Validierung an API- oder Service-Grenzen zu verstecken.

Beispiel für frühe Zurückweisung:

```java
import java.util.Objects;

String label(PaymentStatus status) {
    Objects.requireNonNull(status, "status must not be null");

    return switch (status) {
        case PENDING -> "Ausstehend";
        case SUCCESS -> "Erfolgreich";
        case FAILED -> "Fehlgeschlagen";
    };
}
```

---

## 15. Security- und SaaS-Relevanz

Pattern Matching ist kein Sicherheitsmechanismus. Es ersetzt keine Authentifizierung, keine Autorisierung, keine Mandantenprüfung, keine Eingabevalidierung und keine sichere Deserialisierung.

Trotzdem hat dieses Sprachfeature Sicherheitsrelevanz, weil es Kontrollfluss präziser macht. Viele Sicherheitsfehler entstehen nicht durch Kryptographie oder Framework-Konfiguration, sondern durch falsch behandelte Zustände: ein vergessenes `Denied`, ein ignoriertes `Pending`, ein versehentlich als Erfolg interpretierter Fehler, ein nicht behandelter Mandantenkontext oder ein Fallback, der zu offen ist.

### 15.1 Fail-Closed statt Fail-Open

Sicherheitsrelevante Entscheidungsvarianten müssen so modelliert werden, dass unbekannte oder nicht erlaubte Zustände nicht versehentlich Zugriff gewähren.

Schlecht:

```java
boolean mayAccess(AccessDecision decision) {
    return switch (decision) {
        case Granted granted -> true;
        default -> true; // gefährlich: unbekannte Fälle werden erlaubt
    };
}
```

Besser:

```java
boolean mayAccess(AccessDecision decision) {
    return switch (decision) {
        case Granted granted -> true;
        case Denied denied -> false;
        case StepUpRequired stepUpRequired -> false;
    };
}
```

Wenn ein defensiver `default` bei einer offenen Fremdhierarchie nötig ist, muss er sicher ablehnen:

```java
boolean mayAccess(Object decision) {
    return switch (decision) {
        case Granted granted -> true;
        case Denied denied -> false;
        case StepUpRequired stepUpRequired -> false;
        case null, default -> false;
    };
}
```

### 15.2 Tenant-Kontext nicht aus untrusted DTOs ableiten

In SaaS-Systemen ist Mandantentrennung zentral. Pattern Matching darf nicht dazu verführen, einem extern gelieferten DTO-Typ blind zu vertrauen.

Schlecht:

```java
TenantId resolveTenant(Object request) {
    return switch (request) {
        case TenantScopedRequest scoped -> scoped.tenantId();
        default -> throw new IllegalArgumentException("No tenant");
    };
}
```

Das Problem: Der Mandant wird aus einem Request-Objekt übernommen. Bei externen Eingaben kann das manipuliert oder falsch gemappt sein.

Besser:

```java
TenantId resolveTenant(SecurityContext securityContext) {
    return securityContext.authenticatedTenantId();
}
```

Der Request kann zusätzliche fachliche Daten tragen. Die autoritative Mandantenidentität muss aus einem vertrauenswürdigen Sicherheitskontext stammen.

### 15.3 Logging sensibler Varianten vermeiden

Fehler- oder Fallback-Zweige dürfen nicht ungeprüft ganze Objekte loggen.

Schlecht:

```java
String classify(Object value) {
    return switch (value) {
        case LoginRequest request -> "Login for " + request.username();
        default -> "Unknown value: " + value;
    };
}
```

Wenn `value.toString()` sensible Daten enthält, landen diese im Log oder in einer Fehlermeldung.

Besser:

```java
String classify(Object value) {
    return switch (value) {
        case LoginRequest request -> "LoginRequest(username=%s)".formatted(request.username());
        case null -> "null";
        default -> "Unknown value type: " + value.getClass().getName();
    };
}
```

Auch der Typname kann in extern sichtbaren Fehlermeldungen zu viel verraten. Für interne Logs und externe Responses gelten unterschiedliche Regeln.

### 15.4 Deserialisierung bleibt ein eigener Risikobereich

Pattern Matching verarbeitet Objekte, die bereits existieren. Es macht deren Herkunft nicht vertrauenswürdig.

Wenn Objekte aus JSON, Message Brokern, Java-Serialisierung oder anderen externen Quellen entstehen, müssen Validierung, erlaubte Typen, Schema-Verträge und sichere Deserialisierung separat abgesichert werden. Ein Pattern-Switch auf ein deserialisiertes Objekt ist keine Sicherheitsgrenze.

### 15.5 Typprüfung ist keine Autorisierung

Schlecht:

```java
boolean canRefund(Object actor) {
    return actor instanceof AdminUser;
}
```

Diese Regel verwechselt technische Typform mit Berechtigung. Besser ist eine explizite Autorisierungsentscheidung:

```java
boolean canRefund(AuthorizationService authorizationService, UserId userId, OrderId orderId) {
    return authorizationService.isAllowed(userId, "ORDER_REFUND", orderId);
}
```

Pattern Matching darf Autorisierungsmodelle lesbarer machen. Es darf sie nicht ersetzen.

---

## 16. Framework- und Plattform-Kontext

### 16.1 Spring Boot und REST-Controller

In Controller- oder Service-Code kann Pattern Matching helfen, Ergebnisvarianten klar in HTTP-Antworten zu übersetzen.

```java
ResponseEntity<?> toResponse(CreateOrderResult result) {
    return switch (result) {
        case OrderCreated created -> ResponseEntity.ok(created.response());
        case ValidationFailed failed -> ResponseEntity.badRequest().body(failed.errors());
        case DuplicateOrder duplicate -> ResponseEntity.status(409).body(duplicate.message());
    };
}
```

Wichtig: Die HTTP-Zuordnung darf keine sensiblen internen Details preisgeben. Interne Fehlerobjekte müssen in sichere externe Antwortmodelle übersetzt werden.

### 16.2 Jackson und polymorphe Daten

Wenn JSON in polymorphe Java-Typen deserialisiert wird, reicht Pattern Matching nicht aus. Die erlaubten Typen und Typinformationen müssen bewusst konfiguriert werden. Unkontrollierte polymorphe Deserialisierung ist ein bekanntes Risiko.

Empfehlung:

1. Externe JSON-Verträge möglichst explizit und stabil modellieren.
2. Eingaben gegen Bean Validation oder eigene Validatoren prüfen.
3. Interne sealed Result-Typen nicht blind als externe API-Verträge veröffentlichen.
4. Typinformationen aus JSON nicht als Sicherheitsentscheidung verwenden.

### 16.3 JPA und Persistence

Pattern Matching kann in Service- oder Domain-Logik sinnvoll sein, sollte aber nicht dazu führen, Persistenzmodelle künstlich als Typvariantenhierarchien zu modellieren.

Für persistente Zustände ist häufig ein `enum` oder eine klare State-Spalte besser als eine komplexe Vererbungshierarchie. Sealed und Pattern Switch sind stark im Code, aber Datenbanken speichern andere Strukturen. Das Mapping muss bewusst entworfen werden.

### 16.4 Apache Wicket

In Wicket-Anwendungen kann Pattern Matching Präsentationslogik lesbarer machen, zum Beispiel bei View-Model-Zuständen. Gleichzeitig muss beachtet werden, dass Wicket Seitenzustand serialisiert. Records, sealed Typen und Pattern-Switches ändern nichts an der Pflicht, serialisierbare und detachable Modelle sauber zu behandeln.

Gutes Muster:

```java
String cssClass(ViewState state) {
    return switch (state) {
        case Loading loading -> "is-loading";
        case Loaded loaded -> "is-loaded";
        case Failed failed -> "is-error";
    };
}
```

Die UI sollte Zustände klar darstellen, aber keine sensiblen Fehlerdetails direkt aus internen Objekten anzeigen.

### 16.5 Observability und Fehlerdiagnose

Pattern-Switches können Fehlerklassifikation verbessern:

```java
String metricKey(ImportResult result) {
    return switch (result) {
        case Imported imported -> "import.success";
        case Rejected rejected -> "import.rejected";
        case PartiallyImported partiallyImported -> "import.partial";
    };
}
```

Metriken sollten stabile technische Schlüssel verwenden. Logs und Metriken dürfen keine personenbezogenen oder geheimen Werte enthalten.

---

## 17. Falsche Anwendung und Anti-Patterns

### 17.1 Alter `instanceof`-Cast in neuem Code

```java
if (event instanceof PaymentEvent) {
    PaymentEvent paymentEvent = (PaymentEvent) event;
    handle(paymentEvent);
}
```

Besser:

```java
if (event instanceof PaymentEvent paymentEvent) {
    handle(paymentEvent);
}
```

### 17.2 Pattern Switch über `Object` als Design-Ersatz

```java
void process(Object command) {
    switch (command) {
        case CreateUserCommand c -> createUser(c);
        case DeleteUserCommand d -> deleteUser(d);
        case UpdateUserCommand u -> updateUser(u);
        default -> throw new IllegalArgumentException("Unknown command");
    }
}
```

Das kann sinnvoll sein, wenn die Methode bewusst eine heterogene Command-Schnittstelle verarbeitet. Es ist aber verdächtig, wenn `Object` nur verwendet wird, weil ein klares gemeinsames Interface fehlt.

Besser:

```java
sealed interface UserCommand permits CreateUserCommand, DeleteUserCommand, UpdateUserCommand {}

void process(UserCommand command) {
    switch (command) {
        case CreateUserCommand c -> createUser(c);
        case DeleteUserCommand d -> deleteUser(d);
        case UpdateUserCommand u -> updateUser(u);
    }
}
```

### 17.3 `default` verdeckt neue sealed Variante

```java
String label(PaymentResult result) {
    return switch (result) {
        case Success success -> "Erfolg";
        case Failure failure -> "Fehler";
        default -> "Unbekannt";
    };
}
```

Wenn später `Pending` ergänzt wird, bleibt der Code kompilierbar und behandelt `Pending` als „Unbekannt“. In kritischer Domänenlogik ist das meist falsch.

Besser:

```java
String label(PaymentResult result) {
    return switch (result) {
        case Success success -> "Erfolg";
        case Failure failure -> "Fehler";
        case Pending pending -> "Ausstehend";
    };
}
```

### 17.4 Zu komplexe Guards

```java
case Order order when order.customer().contract().isActive()
        && order.customer().riskScore() < 80
        && order.items().stream().allMatch(Item::available)
        && order.payment().isPresent()
        && order.payment().get().confirmed() -> approve(order);
```

Besser:

```java
case Order order when isApprovalCandidate(order) -> approve(order);
```

Guards sollen Fallunterscheidung unterstützen, nicht Geschäftsregeln verstecken.

### 17.5 Zu tiefe Record Patterns

```java
case A(B(C(D(E(String value))))) -> handle(value);
```

Diese Form ist syntaktisch möglich, aber selten gut reviewbar. Spätestens bei mehreren verschachtelten Ebenen sollte eine Hilfsmethode oder ein explizites Domänenmodell geprüft werden.

### 17.6 Switch Expression mit Seiteneffekten

```java
String result = switch (command) {
    case CreateCommand c -> {
        repository.save(c.toEntity());
        audit.log("created");
        yield "OK";
    }
    case DeleteCommand d -> {
        repository.deleteById(d.id());
        audit.log("deleted");
        yield "OK";
    }
};
```

Das ist nicht automatisch falsch, aber gefährlich, wenn die Switch Expression wie eine reine Wertberechnung aussieht, tatsächlich aber Seiteneffekte ausführt. In solchen Fällen ist ein normales `switch` Statement oder eine Methodenextraktion oft klarer.

Besser:

```java
CommandResult result = switch (command) {
    case CreateCommand c -> create(c);
    case DeleteCommand d -> delete(d);
};
```

### 17.7 `case null` als Ersatz für saubere Eingabegrenzen

```java
String normalize(String value) {
    return switch (value) {
        case null -> "";
        case String text -> text.trim();
    };
}
```

Das kann in einzelnen Normalisierungsfunktionen bewusst sinnvoll sein. Es ist aber schlecht, wenn dadurch unklare `null`-Verträge durch das System wandern. Besser ist meistens, an Grenzen klar zu entscheiden: `null` erlaubt oder nicht erlaubt.

---

## 18. Designregeln

### 18.1 Regel: Erst Modell prüfen, dann Pattern verwenden

Pattern Matching macht bestehende Strukturen leichter behandelbar. Es ersetzt keine gute Modellierung.

Wenn viele Stellen auf dieselben Typvarianten switchen, ist zu prüfen:

1. Gehört das Verhalten in die Varianten selbst?
2. Fehlt ein gemeinsames Interface?
3. Sollte die Hierarchie sealed sein?
4. Ist ein `enum` ausreichend?
5. Wird ein externer API-Vertrag zu direkt in Domänenlogik verwendet?

### 18.2 Regel: `switch` über sealed Typen ohne `default`

Bei eigenen sealed Typen soll der Compiler neue Varianten sichtbar machen.

Richtig:

```java
String label(AuthorizationDecision decision) {
    return switch (decision) {
        case Granted granted -> "Erlaubt";
        case Denied denied -> "Verweigert";
        case StepUpRequired stepUpRequired -> "Zusätzliche Prüfung erforderlich";
    };
}
```

Nur bei offenen, externen oder nicht vollständig kontrollierten Typen ist `default` sinnvoll.

### 18.3 Regel: Fälle fachlich sortieren

Die Reihenfolge soll fachlich lesbar sein. Bei Guards gilt zusätzlich: spezifisch vor allgemein.

```java
String describe(Failure failure) {
    return switch (failure) {
        case Failure f when f.errorCode() >= 500 -> "Technischer Fehler";
        case Failure f when f.errorCode() >= 400 -> "Fachliche Ablehnung";
        case Failure f -> "Fehler";
    };
}
```

### 18.4 Regel: Keine langen Case-Bodies

Ein Case-Zweig sollte kurz bleiben. Lange Case-Bodies verschlechtern Lesbarkeit und Testbarkeit.

Schlecht:

```java
case PaymentConfirmed event -> {
    validate(event);
    updateOrder(event);
    createInvoice(event);
    notifyCustomer(event);
    publishAnalytics(event);
    yield Done.INSTANCE;
}
```

Besser:

```java
case PaymentConfirmed event -> handlePaymentConfirmed(event);
```

### 18.5 Regel: Keine sensiblen Objekte direkt in Fehlermeldungen

Schlecht:

```java
throw new IllegalArgumentException("Unsupported value: " + value);
```

Besser:

```java
throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
```

Noch besser bei extern sichtbaren Fehlern: neutraler Fehlercode ohne interne Typdetails.

---

## 19. Teststrategie

Pattern Matching selbst muss nicht getestet werden. Getestet werden muss die fachliche Fallunterscheidung.

### 19.1 Unit Tests pro Variante

Für jeden fachlichen Fall muss mindestens ein Test existieren.

```java
@Test
void mapsSuccessToOkResponse() {
    var result = new Success("tx-123");

    var response = mapper.toResponse(result);

    assertEquals(200, response.statusCode());
}
```

### 19.2 Tests für Guard-Reihenfolge

Bei Guarded Patterns müssen Grenzwerte getestet werden.

```java
@Test
void classifiesServerErrorsFrom500Upwards() {
    assertEquals("SERVER", classifier.classify(new Failure("error", 500)));
    assertEquals("CLIENT", classifier.classify(new Failure("error", 499)));
}
```

### 19.3 Tests für `null`, wenn fachlich erlaubt

Wenn `case null` verwendet wird, muss die Bedeutung von `null` getestet werden.

```java
@Test
void classifiesNullExplicitly() {
    assertEquals("Kein Wert", classifier.classify(null));
}
```

### 19.4 Security-Tests für Fail-Closed-Verhalten

Bei Zugriffskontrollen muss getestet werden, dass nicht erlaubte Varianten nicht versehentlich Zugriff erhalten.

```java
@Test
void deniesAccessWhenStepUpIsRequired() {
    assertFalse(accessPolicy.mayAccess(new StepUpRequired("mfa")));
}
```

---

## 20. Review-Checkliste

| Aspekt | Details/Erklärung | Review-Frage |
|---|---|---|
| Java-Version | Werden Java-21-Features nur in Java-21+-Modulen verwendet? | Passt der Code zur Baseline? |
| `instanceof` | Wurde alter Cast-Code durch Pattern Matching ersetzt? | Gibt es noch redundante Casts? |
| Switch-Form | Wird Arrow-Syntax verwendet? | Gibt es unnötige Doppelpunkt-Syntax? |
| Exhaustiveness | Sind alle Fälle abgedeckt? | Prüft der Compiler die Vollständigkeit? |
| `default` | Verdeckt `default` neue Varianten? | Ist `default` wirklich nötig? |
| `null` | Ist `null` möglich? | Wird `null` bewusst behandelt oder ausgeschlossen? |
| Guarded Patterns | Sind Guards einfach und verständlich? | Gehört die Bedingung in eine Methode? |
| Case-Reihenfolge | Spezifisch vor allgemein? | Gibt es dominierte oder unerreichbare Fälle? |
| Record Patterns | Ist die Dekonstruktion lesbar? | Ist das Pattern zu tief verschachtelt? |
| Security | Gibt es fail-open Verhalten? | Werden unbekannte Fälle sicher abgelehnt? |
| Tenant-Kontext | Wird Mandantenidentität aus vertrauenswürdiger Quelle genommen? | Kommt Tenant-ID aus SecurityContext statt Request? |
| Logging | Werden sensible Objekte geloggt? | Gibt es Redaction oder neutrale Fehlertexte? |
| Design | Wird Pattern Matching als Ersatz für gutes Modell genutzt? | Sollte ein Interface, sealed Typ oder enum eingeführt werden? |
| Tests | Gibt es Tests pro Variante? | Sind Guards und Grenzfälle getestet? |
| Lesbarkeit | Ist der Switch kleiner als die ersetzte Alternative? | Hilft das Sprachfeature wirklich? |

---

## 21. Automatisierbare Prüfungen

Diese Richtlinie kann teilweise automatisiert werden. Nicht jede Regel ist statisch vollständig prüfbar, aber wichtige Hinweise lassen sich in CI und Review-Prozesse integrieren.

### 21.1 Mögliche Checkstyle-/PMD-/Sonar-Regeln

- Markiere klassische `instanceof`-Prüfungen mit anschließendem Cast als Refactoring-Kandidaten.
- Markiere neue klassische `switch`-Statements mit Doppelpunkt-Syntax in Java-21-Modulen.
- Markiere lange `switch`-Case-Bodies.
- Markiere `default`-Zweige in `switch` über eigene sealed Typen zur manuellen Prüfung.
- Markiere `switch` über `Object` zur Designprüfung.
- Markiere `case null` zur Prüfung des Null-Vertrags.

### 21.2 Mögliche ArchUnit-Regeln

- Domänen-Result-Typen in bestimmten Packages müssen sealed sein.
- Command- und Event-Hierarchien müssen ein gemeinsames Interface besitzen.
- API-Controller dürfen interne sealed Domänentypen nicht direkt nach außen serialisieren.
- Security-Entscheidungstypen dürfen nicht in Präsentationsschichten direkt gematcht werden, wenn dadurch interne Details sichtbar werden.

### 21.3 Mögliche Semgrep-Regeln

- Finde `instanceof` gefolgt von explizitem Cast.
- Finde `default -> true` in Methoden mit Namen wie `mayAccess`, `isAllowed`, `can*`.
- Finde Logging oder String-Konkatenation von Objekten in `default`-Zweigen.
- Finde `switch` über `Object` in Service- oder Security-Packages.
- Finde `case null` ohne begleitende Validierungsentscheidung.

### 21.4 CI-Gate

Diese Richtlinie sollte nicht als hartes CI-Gate für jeden Einzelfall umgesetzt werden. Viele Regeln brauchen Kontext. Sinnvoll ist ein zweistufiges Modell:

1. Statische Analyse markiert verdächtige Muster.
2. Pull-Request-Review entscheidet fachlich, ob das Muster begründet ist.

---

## 22. Migration bestehender Codebasis

Migration soll risikoarm und testgestützt erfolgen. Ziel ist nicht, alle alten `if-else`-Ketten mechanisch umzuschreiben. Ziel ist, die Lesbarkeit und Prüfbarkeit dort zu erhöhen, wo Typ- oder Variantenlogik tatsächlich relevant ist.

### 22.1 Geeignete Migrationskandidaten

Gute Kandidaten sind:

- `instanceof` plus Cast;
- lange `if-else`-Ketten über Typen;
- klassische `switch`-Statements, die nur einen Wert berechnen;
- Result-Mapping in Services;
- UI-State-Mapping;
- API-Response-Mapping;
- Fehlerklassifikation;
- Event- und Command-Dispatcher.

Schlechte Kandidaten sind:

- einfache Bedingungen mit zwei Zweigen;
- bewusst polymorphes Verhalten;
- Performance-kritischer Code ohne Messung;
- Code in älteren Java-Modulen;
- instabile oder schlecht verstandene Geschäftslogik ohne Tests.

### 22.2 Migrationsvorgehen

1. Bestehende Tests stabilisieren.
2. Fachliche Varianten identifizieren.
3. Prüfen, ob `enum`, sealed Typ oder bestehende Klasse geeignet ist.
4. Alte `instanceof`-Casts durch Pattern Matching ersetzen.
5. Wertberechnende `switch`-Statements in Switch Expressions überführen.
6. Bei sealed Typen `default` entfernen, wenn alle Varianten explizit behandelt werden.
7. `null`-Vertrag klären.
8. Security-relevante Fallbacks auf fail-closed prüfen.
9. Logging in Fehlerzweigen auf sensible Daten prüfen.
10. Tests pro Variante ergänzen.
11. Review mit Checkliste durchführen.

### 22.3 Beispielmigration

Vorher:

```java
String describe(Object event) {
    if (event instanceof PaymentConfirmed) {
        PaymentConfirmed confirmed = (PaymentConfirmed) event;
        return "Payment confirmed: " + confirmed.transactionId();
    } else if (event instanceof PaymentFailed) {
        PaymentFailed failed = (PaymentFailed) event;
        return "Payment failed: " + failed.reason();
    } else {
        return "Unknown";
    }
}
```

Nachher:

```java
sealed interface PaymentEvent permits PaymentConfirmed, PaymentFailed {}

record PaymentConfirmed(String transactionId) implements PaymentEvent {}

record PaymentFailed(String reason) implements PaymentEvent {}

String describe(PaymentEvent event) {
    return switch (event) {
        case PaymentConfirmed confirmed -> "Payment confirmed: " + confirmed.transactionId();
        case PaymentFailed failed -> "Payment failed: " + failed.reason();
    };
}
```

Diese Migration verbessert nicht nur Syntax. Sie macht aus einem offenen `Object`-Vertrag einen geschlossenen fachlichen Vertrag.

---

## 23. Ausnahmen

Abweichungen von dieser Richtlinie sind erlaubt, wenn sie begründet sind.

Zulässige Gründe sind insbesondere:

1. Das Modul muss auf Java 17 oder älter ohne Preview-Features laufen.
2. Die Zielhierarchie ist bewusst offen, zum Beispiel bei Plugin-Systemen.
3. Polymorphie ist fachlich klarer als zentraler `switch`.
4. Ein Framework erzwingt eine bestimmte Struktur.
5. Die Fallunterscheidung ist zu dynamisch für statisches Pattern Matching.
6. Eine einfache `if`-Bedingung ist lesbarer als ein `switch`.
7. Performance-Messungen zeigen ein reales Problem und eine alternative Form ist nachweislich besser.
8. Externe Typen sind nicht unter eigener Kontrolle und erfordern defensiven `default`.

Die Begründung muss im Pull Request nachvollziehbar sein. Eine pauschale Aussage wie „altes Muster ist bekannt“ reicht nicht.

---

## 24. Definition of Done

Eine Implementierung erfüllt diese Richtlinie, wenn alle folgenden Kriterien erfüllt sind:

1. Typprüfung plus Nutzung verwendet Pattern Matching für `instanceof`, sofern möglich.
2. Mehrarmige Wertberechnungen verwenden Switch Expressions mit Arrow-Syntax, sofern dadurch Lesbarkeit entsteht.
3. Pattern Switches über eigene sealed Typen behandeln alle Varianten explizit.
4. `default` wird nicht verwendet, wenn er Compiler-Feedback über neue sealed Varianten verdecken würde.
5. `null` ist bewusst behandelt oder bewusst ausgeschlossen.
6. Guarded Patterns sind verständlich, spezifisch vor allgemein sortiert und nicht überladen.
7. Record Patterns sind lesbar und nicht unnötig tief verschachtelt.
8. Security-relevante Kontrollflüsse sind fail-closed.
9. Tenant-, Authentifizierungs- und Autorisierungsinformationen stammen aus vertrauenswürdigen Kontexten, nicht aus bloßen Typmustern.
10. Fehler- und Fallback-Zweige loggen keine sensiblen Daten.
11. Pro fachlicher Variante existiert ein Test oder eine nachvollziehbare Testabdeckung.
12. Die gewählte Sprachform verbessert Lesbarkeit, Prüfbarkeit oder Fehlersicherheit gegenüber der alten Form.
13. Abweichungen sind im Pull Request begründet.

---

## 25. Quellen und weiterführende Literatur

- OpenJDK JEP 361 — Switch Expressions: https://openjdk.org/jeps/361
- OpenJDK JEP 394 — Pattern Matching for instanceof: https://openjdk.org/jeps/394
- OpenJDK JEP 440 — Record Patterns: https://openjdk.org/jeps/440
- OpenJDK JEP 441 — Pattern Matching for switch: https://openjdk.org/jeps/441
- Oracle Java SE 21 Language Documentation — Pattern Matching: https://docs.oracle.com/en/java/javase/21/language/pattern-matching.html
- Oracle Java SE 21 Language Documentation — Pattern Matching for switch: https://docs.oracle.com/en/java/javase/21/language/pattern-matching-switch.html
- Oracle Java SE 21 Language Documentation — Record Patterns: https://docs.oracle.com/en/java/javase/21/language/record-patterns.html
- OWASP Input Validation Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Deserialization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Deserialization_Cheat_Sheet.html
- OWASP Secure Code Review Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secure_Code_Review_Cheat_Sheet.html
