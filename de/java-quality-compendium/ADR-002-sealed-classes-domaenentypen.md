# ADR-002 — Sealed Classes für geschlossene Domänentypen

| Feld       | Wert                          |
|------------|-------------------------------|
| Java       | 17+ · JEP 409                 |
| Datum      | 2024-01-01                    |
| Kategorie  | Domain Design                 |

---

## Kontext & Problem

Domänenmodelle haben oft eine **endliche, bekannte Menge von Varianten**: ein Zahlungsstatus ist entweder `Pending`, `Success` oder `Failed` — niemals etwas anderes. Mit klassischer Vererbung kann jedoch jede Klasse im Classpath eine Subklasse erstellen. Der Compiler kann nicht prüfen, ob alle Fälle behandelt sind. Dieses implizite Wissen lebt im Kopf des Entwicklers — nicht im Code.

Sealed Classes schließen diese Lücke.

---

## Schlechtes Beispiel — offene Hierarchie

```java
// Jede Klasse im Projekt kann PaymentResult erweitern — unkontrollierbar.
abstract class PaymentResult {
    // kein permits → offen für alle
}

class Success extends PaymentResult {
    String transactionId;
}

class Failure extends PaymentResult {
    String reason;
}

// Im Service: Der Compiler warnt NICHT bei fehlendem Zweig!
void handle(PaymentResult result) {
    if (result instanceof Success s) {
        book(s.transactionId);
    }
    // Failure komplett vergessen — kein Compiler-Fehler, kein Laufzeit-Fehler bis es passiert.
}
```

**Warum ist das schlecht?**
- Neue Variante (`Pending`) hinzugefügt → alle bestehenden `if-instanceof`-Ketten müssen manuell gefunden und aktualisiert werden. Der Compiler hilft nicht.
- Vergessene Fälle führen zu Silent-Failures oder NullPointerExceptions in Produktion.
- Die Menge der erlaubten Subklassen ist nirgendwo dokumentiert — implizites Wissen.

---

## Gutes Beispiel — Sealed Interface + Records + Pattern Switch

```java
// Der Compiler kennt ALLE erlaubten Implementierungen.
sealed interface PaymentResult
    permits Success, Failure, Pending {}

record Success(String transactionId)
    implements PaymentResult {}

record Failure(String reason, int errorCode)
    implements PaymentResult {}

record Pending(Instant since)
    implements PaymentResult {}
```

```java
// Exhaustiver Switch — fehlt ein Fall, schlägt der Compiler Alarm.
void handle(PaymentResult result) {
    switch (result) {
        case Success  s -> book(s.transactionId());
        case Failure  f -> alert(f.reason());
        case Pending  p -> queue(p.since());
        // Kein default nötig — der Compiler weiß, dass alle Fälle abgedeckt sind.
    }
}
```

### Mit Guarded Patterns für feingranulare Fälle:

```java
String describe(PaymentResult result) {
    return switch (result) {
        case Success  s                      -> "Gebucht: " + s.transactionId();
        case Failure  f when f.errorCode() >= 500 -> "Serverfehler: " + f.reason();
        case Failure  f                      -> "Abgelehnt: " + f.reason();
        case Pending  p                      -> "Ausstehend seit: " + p.since();
    };
}
```

---

## Begründung

Sealed Classes machen implizites Wissen explizit. Der Compiler weiß, welche Implementierungen existieren, und kann **Exhaustiveness** bei Switch-Ausdrücken prüfen. Das eliminiert die häufigste Fehlerklasse bei Statusmaschinen: den vergessenen Zweig.

Kombiniert mit Records entsteht ein **algebraischer Datentyp (ADT)** — ein Konzept aus funktionalen Sprachen (Haskell, Scala, Rust `enum`), das Java nun nativ unterstützt.

---

## Konsequenzen

**Positiv:**
- Compiler-geprüfte Vollständigkeit bei Switch-Expressions über sealed Typen.
- Neue Varianten hinzufügen → Compiler zeigt alle Stellen, die angepasst werden müssen.
- Perfekt geeignet für: Result-Typen, Event-Hierarchien, AST-Knoten, API-Response-Varianten, State-Machines.

**Negativ / Einschränkungen:**
- Alle `permits`-Typen müssen im selben Paket (oder derselben Compilation Unit) liegen.
- Nicht geeignet für Erweiterungspunkte durch Dritte (z. B. Plugin-Systeme) — dort weiterhin offene Interfaces verwenden.

---

## Tipps

- Kombiniere **immer** `sealed interface` + `record` — das ist die natürliche Paarung in Java 21.
- Verwende `sealed` für alle Typen, deren Varianten zur Compile-Zeit bekannt sind.
- `non-sealed` als `permits`-Typ erlaubt, dass eine Subklasse wieder offen ist — nutze das bewusst und sparsam.
- Für externe Bibliotheken, die Sealed-Typen noch nicht kennen: Default-Branch mit explizitem `throw new AssertionError("Unbekannte Variante: " + result)` absichern.

