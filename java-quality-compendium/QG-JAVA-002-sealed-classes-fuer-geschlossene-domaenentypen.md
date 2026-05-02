# QG-JAVA-002 — Sealed Classes für geschlossene Domänentypen

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-002 |
| Titel | Sealed Classes für geschlossene Domänentypen |
| Status | Accepted / verbindlicher Standard für geschlossene Domänentypen in neuen Java-21+-Codebasen |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, SaaS-Plattformen, Domain Design, Typensicherheit, sicherer Kontrollfluss, State Machines, API- und Service-Ergebnisvarianten |
| Java-Baseline | Java 21+ als empfohlener Standard, weil Sealed Classes seit Java 17 final sind, Pattern Matching for `switch` aber erst seit Java 21 final ist |
| Primäres Java-Feature | Sealed Classes und Sealed Interfaces |
| Verwandte Java-Features | Records, Pattern Matching for `switch`, Guarded Patterns |
| Letzte Validierung | 2026-05-02 |
| Validierte Quellenbasis | OpenJDK JEP 409, OpenJDK JEP 441, Oracle Java 21 Language Documentation, OWASP Logging Cheat Sheet, OWASP Deserialization Cheat Sheet, OWASP Mass Assignment Cheat Sheet, OWASP Secure Code Review Cheat Sheet, Jackson-Dokumentation, Apache Wicket 10 User Guide |
| Technische Beispielvalidierung | Die zentralen Java-Beispiele ohne externe Framework-Abhängigkeiten wurden mit `javac --release 21` syntaktisch geprüft. |
| Verbindlichkeit | Diese Richtlinie gilt für neue geschlossene Domänentypen verbindlich. Abweichungen sind zulässig, wenn ein technischer oder fachlicher Grund im Pull Request nachvollziehbar dokumentiert wird. |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wann Java Sealed Classes und Sealed Interfaces verwendet werden sollen, um geschlossene Domänentypen präzise, überprüfbar und wartbar zu modellieren.

Ein geschlossener Domänentyp ist ein Typ, dessen gültige Varianten zur Compile-Zeit bekannt und bewusst begrenzt sind. Typische Beispiele sind Zahlungsergebnisse, Autorisierungsentscheidungen, Workflow-Zustände, Import-Ergebnisse, Parser-Ergebnisse, Validierungsergebnisse, interne API-Antwortvarianten und Zustände von fachlichen State Machines.

Der Zweck dieser Richtlinie ist, implizites Domänenwissen explizit im Typsystem abzubilden. Wenn ein fachliches Konzept nur eine endliche und bekannte Menge von Alternativen haben darf, soll diese Menge nicht nur in Köpfen, Kommentaren oder Wikis existieren. Sie soll im Code sichtbar sein, damit Compiler, Reviewer und Tests damit arbeiten können.

Der Qualitätsgewinn entsteht vor allem dann, wenn sealed Typen mit Pattern Matching for `switch` in Java 21 kombiniert werden. Der Compiler kann dann prüfen, ob alle bekannten Varianten behandelt wurden. Vergessene Zweige werden dadurch früher sichtbar: nicht erst durch Produktionsfehler, sondern bereits beim Kompilieren.

Diese Richtlinie ersetzt keine saubere Fachmodellierung. Sie verhindert auch nicht automatisch Sicherheitsfehler. Sie liefert aber ein robustes Modellierungswerkzeug für Fälle, in denen eine offene Vererbungshierarchie zu viel Freiheit und zu wenig Kontrolle bietet.

---

## 2. Kurzregel für Entwickler

Verwende ein `sealed interface` oder eine `sealed abstract class`, wenn ein Domänentyp eine bekannte, endliche und bewusst geschlossene Menge von Varianten besitzt.

Bevorzuge für datenhaltende Varianten diese Form:

```java
import java.time.Instant;

sealed interface PaymentResult permits Success, Failure, Pending {}

record Success(String transactionId) implements PaymentResult {}
record Failure(String reason, int errorCode) implements PaymentResult {}
record Pending(Instant since) implements PaymentResult {}
```

Verwende einen `switch` über den sealed Root-Typ, wenn alle Varianten bewusst behandelt werden müssen:

```java
void handle(PaymentResult result) {
    switch (result) {
        case Success s -> book(s.transactionId());
        case Failure f -> alert(f.reason());
        case Pending p -> queue(p.since());
    }
}
```

Verwende sealed Typen nicht für Plugin-Schnittstellen, öffentliche Erweiterungspunkte für Dritte oder Hierarchien, deren Varianten bewusst unbekannt bleiben sollen.

Füge bei einem `switch` über eine eigene sealed Hierarchie keinen `default`-Zweig hinzu, wenn kein klarer defensiver Grund besteht. Ein `default`-Zweig kann fehlende Fälle verdecken, die der Compiler sonst melden würde.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für Java-Anwendungscode, Domänencode, Service-Layer-Ergebnisobjekte, Workflow-Zustandsmodelle, Command Handling, Event Handling, API-interne Antwortmodelle und Integrationsergebnisse.

Sie ist besonders relevant für SaaS-Plattformen, weil dort falsch behandelte Varianten konkrete Schäden erzeugen können: Zahlungsfehler, falsche Autorisierungsentscheidungen, inkonsistente Mandanten-Zustände, fehlerhafte Workflow-Übergänge, stille Betriebsfehler oder nicht behandelte Integrationszustände.

Diese Richtlinie gilt insbesondere für:

- Service-Result-Typen;
- Zahlungs- und Bestellfluss-Ergebnisse;
- Autorisierungsentscheidungen;
- Validierungsergebnisse;
- Workflow- und State-Machine-Zustände;
- Import- und Export-Ergebnisse;
- Parser- und Klassifikationsergebnisse;
- Domänenereignisse mit geschlossener Menge von Ereignisformen;
- interne API-Antwortvarianten;
- Command-Result-Hierarchien;
- Abstract-Syntax-Tree-Knoten;
- interne Integrationsergebnis-Typen.

Diese Richtlinie gilt nicht automatisch für:

- JPA-Entities;
- Framework-Proxy-Typen;
- Plugin-APIs;
- öffentliche Erweiterungspunkte für Dritte;
- Hierarchien, deren Varianten zur Compile-Zeit bewusst unbekannt sind;
- Datenmodelle, die primär persistiert und nicht per Pattern Matching verarbeitet werden;
- einfache Statuswerte, bei denen ein `enum` genügt;
- dynamische Regelwerke, bei denen neue Varianten konfiguriert statt kompiliert werden.

---

## 4. Technischer Hintergrund

Java Sealed Classes und Sealed Interfaces beschränken, welche Klassen oder Interfaces einen Typ direkt erweitern oder implementieren dürfen. Dadurch wird eine Vererbungshierarchie an einer definierten Stelle geschlossen.

Ein sealed Root-Typ nennt seine erlaubten direkten Subtypen mit `permits`:

```java
sealed interface PaymentResult permits Success, Failure, Pending {}
```

Jeder direkt erlaubte Subtyp muss selbst eine der folgenden Formen besitzen:

- `final`: Der Zweig endet an dieser Stelle.
- `sealed`: Der Zweig bleibt geschlossen, erlaubt aber weitere kontrollierte Untertypen.
- `non-sealed`: Der Zweig wird bewusst wieder geöffnet.

Records passen häufig sehr gut zu sealed Varianten, weil Records kompakte, unveränderliche Datenrepräsentationen sind. Ein sealed Interface beschreibt das geschlossene fachliche Konzept, während Records die konkreten Daten der einzelnen Varianten tragen.

```java
sealed interface ImportResult permits Imported, Rejected, Skipped {}

record Imported(int rowCount) implements ImportResult {}
record Rejected(String reason) implements ImportResult {}
record Skipped(String reason) implements ImportResult {}
```

Sealed Classes wurden mit Java 17 finalisiert. Pattern Matching for `switch` wurde mit Java 21 finalisiert. Deshalb empfiehlt diese Richtlinie Java 21+ als Baseline, weil der wichtigste Qualitätsgewinn aus der Kombination von sealed Typen und exhaustivem Pattern Matching entsteht.

### Wichtige Java-Versionsgrenze

Sealed Classes und Sealed Interfaces sind seit Java 17 ein finales Sprachfeature. Pattern Matching for `switch`, inklusive Type Patterns in `case`-Labels und Guarded Patterns mit `when`, ist erst seit Java 21 final.

Das ist für diese Richtlinie entscheidend. Eine Java-17-Codebasis kann sealed Typen verwenden, aber die hier gezeigten Java-21-`switch`-Beispiele nicht ohne Preview-Feature-Konfiguration nutzen. Für produktive Qualitätsrichtlinien wird hier Java 21+ angenommen.

---

## 5. Zentrales Designprinzip

Eine geschlossene fachliche Regel soll im Typsystem sichtbar sein.

Wenn die Fachdomäne sagt:

```text
Ein Zahlungsergebnis ist genau eines von: Erfolg, Fehler oder ausstehend.
```

Dann sollte der Code nicht nur sagen:

```java
abstract class PaymentResult {}
```

Diese Form lässt die Hierarchie offen. Jede beliebige Klasse im passenden Zugriffskontext kann eine neue Unterklasse erzeugen. Der Compiler weiß nicht, ob `Success`, `Failure` und `Pending` wirklich vollständig sind.

Besser ist:

```java
sealed interface PaymentResult permits Success, Failure, Pending {}
```

Damit wird die fachliche Regel direkt im Code ausgedrückt. Der Compiler kennt die zulässigen Varianten und kann bei einem `switch` prüfen, ob alle Fälle behandelt wurden.

Der zentrale Qualitätsvorteil lautet: **Vergessene Varianten werden zu Compile-Zeit-Feedback statt zu Produktionsfehlern.**

---

## 6. Wann sealed Typen verwendet werden sollen

Verwende sealed Typen, wenn alle folgenden Bedingungen erfüllt sind:

1. Die Menge der Varianten ist zur Compile-Zeit bekannt.
2. Die Menge der Varianten ist fachlich bewusst begrenzt.
3. Jede Variante beschreibt eine echte fachliche Alternative.
4. Aufrufer sollen in der Regel alle Varianten behandeln.
5. Eine neue Variante soll betroffene Stellen im Code sichtbar machen.
6. Die Varianten werden innerhalb derselben Team-, Modul- oder Ownership-Grenze gepflegt.
7. Die Hierarchie ist nicht für beliebige Erweiterungen durch Dritte gedacht.

Gute Kandidaten sind:

```text
PaymentResult = Success | Failure | Pending
AuthorizationDecision = Granted | Denied | StepUpRequired
ImportResult = Imported | Rejected | PartiallyImported
InvoiceState = Draft | Submitted | Paid | Cancelled
ValidationResult = Valid | Invalid
CommandResult = Accepted | Rejected | Deferred
DocumentParseResult = Parsed | UnsupportedFormat | Malformed
```

Ein sealed Typ ist besonders sinnvoll, wenn jede Variante unterschiedliche Daten trägt.

```java
import java.time.Instant;

sealed interface PaymentResult permits Success, Failure, Pending {}

record Success(String transactionId) implements PaymentResult {}
record Failure(String reason, int errorCode) implements PaymentResult {}
record Pending(Instant since) implements PaymentResult {}
```

Ein `enum` wäre hier zu schwach, weil die Varianten nicht dieselben Daten tragen. Eine erfolgreiche Zahlung hat eine Transaktions-ID. Eine fehlgeschlagene Zahlung hat einen Fehlergrund und einen Fehlercode. Eine ausstehende Zahlung hat einen Zeitpunkt. Sealed Records modellieren diese Unterschiede direkt.

---

## 7. Wann ein `enum` besser ist

Verwende ein `enum`, wenn die Varianten reine Konstanten sind und keine variantenabhängigen Datenstrukturen benötigen.

Gutes Beispiel:

```java
enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
```

Das ist ausreichend, wenn nur ein benannter Statuswert benötigt wird.

Ersetze einfache Enums nicht durch sealed Hierarchien, nur weil sealed Typen moderner wirken. Das erhöht die Komplexität, ohne die Korrektheit zu verbessern.

Verwende sealed Typen statt Enums, wenn Varianten unterschiedliche Daten tragen:

```java
sealed interface PaymentResult permits Success, Failure, Pending {}

record Success(String transactionId) implements PaymentResult {}
record Failure(String reason, int errorCode) implements PaymentResult {}
record Pending(java.time.Instant since) implements PaymentResult {}
```

Faustregel:

```text
Gleiche Struktur für alle Varianten        -> enum prüfen
Unterschiedliche Daten je Variante        -> sealed Typ prüfen
Alle Varianten müssen behandelt werden    -> sealed Typ plus switch prüfen
Varianten sollen von Dritten erweiterbar sein -> offenes Interface verwenden
```

---

## 8. Wann sealed Typen nicht verwendet werden sollen

Verwende keine sealed Typen, wenn die Hierarchie absichtlich offen sein soll.

Schlechte Kandidaten sind:

- Plugin-Schnittstellen;
- SPI-Mechanismen;
- öffentliche Erweiterungspunkte für externe Teams oder Kunden;
- Framework-Abstraktionen, die von unbekannten Typen implementiert werden;
- generische technische Interfaces wie `Repository`, `Validator`, `Handler`, `Mapper`;
- Domänenbereiche, in denen Varianten zur Laufzeit konfiguriert werden;
- Bibliotheks-APIs, die bewusst langfristige Erweiterbarkeit ermöglichen sollen.

Beispiel für eine offene Schnittstelle:

```java
public interface PaymentProvider {
    PaymentResponse charge(PaymentRequest request);
}
```

Diese Schnittstelle sollte nicht sealed sein, wenn neue Provider später durch andere Module, Kundenprojekte oder Partner ergänzt werden sollen.

Sealing ist kein Qualitätsgewinn, wenn Erweiterbarkeit der eigentliche Zweck der Abstraktion ist.

---

## 9. Schlechtes Beispiel — offene Hierarchie mit implizitem Wissen

```java
abstract class PaymentResult {
    // offen für unbekannte Unterklassen
}

class Success extends PaymentResult {
    String transactionId;
}

class Failure extends PaymentResult {
    String reason;
}
```

Problematischer Service-Code:

```java
void handle(PaymentResult result) {
    if (result instanceof Success s) {
        book(s.transactionId);
    }

    // Failure wird vollständig vergessen.
    // Der Compiler kann diesen Fehler nicht erkennen.
}
```

Warum ist das schlecht?

- Die erlaubten Varianten sind nicht im Root-Typ dokumentiert.
- Neue Varianten können unkontrolliert ergänzt werden.
- `if instanceof`-Ketten müssen manuell gesucht und aktualisiert werden.
- Der Compiler kann nicht prüfen, ob alle Fälle behandelt wurden.
- Vergessene Fälle können zu stillen Fehlern, falschen Workflows oder verspäteten Produktionsproblemen führen.
- Reviewer müssen Domänenwissen aus dem Kopf prüfen, statt es im Code zu sehen.

Diese Form ist besonders gefährlich bei Zahlungs-, Autorisierungs-, Mandanten-, Import- und Workflow-Logik.

---

## 10. Gutes Beispiel — Sealed Interface, Records und exhaustiver Switch

```java
import java.time.Instant;
import java.util.Objects;

sealed interface PaymentResult permits Success, Failure, Pending {}

record Success(String transactionId) implements PaymentResult {
    public Success {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        if (transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
    }
}

record Failure(String reason, int errorCode) implements PaymentResult {
    public Failure {
        Objects.requireNonNull(reason, "reason must not be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (errorCode < 400 || errorCode > 599) {
            throw new IllegalArgumentException("errorCode must be in HTTP error range");
        }
    }
}

record Pending(Instant since) implements PaymentResult {
    public Pending {
        Objects.requireNonNull(since, "since must not be null");
    }
}
```

Verarbeitung mit `switch`:

```java
void handle(PaymentResult result) {
    switch (result) {
        case Success s -> book(s.transactionId());
        case Failure f -> alert(f.reason());
        case Pending p -> queue(p.since());
    }
}
```

Der Vorteil: Der Compiler kennt die vollständige Menge der erlaubten Varianten. Wenn später eine neue Variante ergänzt wird, kann ein unvollständiger `switch` fehlschlagen, bis die neue Variante behandelt wird.

Das ist kein kosmetischer Vorteil. Das ist eine harte Qualitätsverbesserung im Kontrollfluss.

---

## 11. Guarded Patterns für feinere Fallunterscheidung

In Java 21 können `switch`-Fälle mit Guards verfeinert werden. Das ist sinnvoll, wenn eine Variante mehrere fachlich relevante Unterfälle besitzt.

```java
String describe(PaymentResult result) {
    return switch (result) {
        case Success s -> "Gebucht: " + s.transactionId();
        case Failure f when f.errorCode() >= 500 -> "Serverfehler: " + f.reason();
        case Failure f -> "Abgelehnt: " + f.reason();
        case Pending p -> "Ausstehend seit: " + p.since();
    };
}
```

Wichtig: Guards dürfen nicht verwendet werden, um eine schlecht modellierte Domäne zu kaschieren. Wenn ein Unterfall fachlich stabil und zentral ist, kann eine eigene Variante besser sein.

Beispiel:

```text
Failure mit errorCode >= 500 ist nur technische Anzeige?     -> Guard genügt.
ServerFailure hat eigene fachliche Behandlung und Regeln?    -> eigene Variante prüfen.
```

---

## 12. Null-Handling

Ein `switch` über einen sealed Typ behandelt nicht automatisch `null`. Übergib an fachliche Handler keine `null`-Werte. Prüfe den Wert an der Systemgrenze oder direkt am Methodeneingang.

Empfohlen:

```java
import java.util.Objects;

void handle(PaymentResult result) {
    Objects.requireNonNull(result, "result must not be null");

    switch (result) {
        case Success s -> book(s.transactionId());
        case Failure f -> alert(f.reason());
        case Pending p -> queue(p.since());
    }
}
```

Nicht empfohlen:

```java
void handle(PaymentResult result) {
    switch (result) {
        case null -> ignore();
        case Success s -> book(s.transactionId());
        case Failure f -> alert(f.reason());
        case Pending p -> queue(p.since());
    }
}
```

`case null` kann in manchen technischen Fällen korrekt sein, sollte aber in fachlichem Domänencode sparsam verwendet werden. Häufig ist `null` kein fachlicher Zustand, sondern ein Fehler in der Aufruferlogik.

Qualitätsregel: **Ein fehlender Wert soll explizit modelliert werden, nicht als `null` durch die Domäne wandern.**

Beispiel:

```java
sealed interface LookupResult permits Found, NotFound {}
record Found(String value) implements LookupResult {}
record NotFound(String key) implements LookupResult {}
```

---

## 13. `default`-Zweige in `switch`-Statements

Bei einem `switch` über eine eigene sealed Hierarchie sollte normalerweise kein `default`-Zweig verwendet werden.

Nicht empfohlen:

```java
String describe(PaymentResult result) {
    return switch (result) {
        case Success s -> "Gebucht";
        case Failure f -> "Fehler";
        default -> "Unbekannt";
    };
}
```

Warum ist das problematisch?

Wenn später `Pending` ergänzt wird, kann der `default`-Zweig verhindern, dass der Compiler die fehlende explizite Behandlung meldet. Die neue fachliche Variante wird dann nur generisch abgefangen. Genau das widerspricht dem Zweck einer sealed Hierarchie.

Besser:

```java
String describe(PaymentResult result) {
    return switch (result) {
        case Success s -> "Gebucht";
        case Failure f -> "Fehler";
        case Pending p -> "Ausstehend";
    };
}
```

Ein `default`-Zweig kann erlaubt sein, wenn externe Daten, alte Serialisate, Modulgrenzen oder Kompatibilitätsfälle abgesichert werden müssen. Dann muss er bewusst fail-closed gestaltet sein.

```java
String describeExternal(PaymentResult result) {
    return switch (result) {
        case Success s -> "Gebucht";
        case Failure f -> "Fehler";
        case Pending p -> "Ausstehend";
        default -> throw new IllegalStateException(
            "Unbekannte PaymentResult-Variante: " + result.getClass().getName()
        );
    };
}
```

Qualitätsregel: **Ein `default`-Zweig darf fehlende Fachlogik nicht verstecken.**

---

## 14. Sealed Interface oder Sealed Abstract Class?

Bevorzuge ein `sealed interface`, wenn die Varianten primär unterschiedliche Datenformen oder fachliche Alternativen darstellen.

```java
sealed interface AuthorizationDecision permits Granted, Denied, StepUpRequired {}

record Granted(String subjectId) implements AuthorizationDecision {}
record Denied(String reason) implements AuthorizationDecision {}
record StepUpRequired(String challengeId) implements AuthorizationDecision {}
```

Verwende eine `sealed abstract class`, wenn gemeinsame Implementierung, geschützter Zustand oder gemeinsame Methoden sinnvoll sind.

```java
abstract sealed class DomainEvent permits UserRegistered, UserDeleted {
    private final java.time.Instant occurredAt;

    protected DomainEvent(java.time.Instant occurredAt) {
        this.occurredAt = java.util.Objects.requireNonNull(occurredAt);
    }

    public java.time.Instant occurredAt() {
        return occurredAt;
    }
}

final class UserRegistered extends DomainEvent {
    UserRegistered(java.time.Instant occurredAt) {
        super(occurredAt);
    }
}

final class UserDeleted extends DomainEvent {
    UserDeleted(java.time.Instant occurredAt) {
        super(occurredAt);
    }
}
```

In vielen modernen Java-21-Codebasen ist die Kombination aus `sealed interface` und `record` die bevorzugte Form für geschlossene datenhaltende Alternativen.

Entscheidungshilfe:

```text
Varianten tragen nur eigene Daten                  -> sealed interface + record
Varianten teilen substanzielle Implementierung      -> sealed abstract class prüfen
Varianten benötigen Framework-Proxies               -> sealed Typ vermeiden
Varianten sind externe Erweiterungspunkte           -> offenes Interface verwenden
```

---

## 15. Empfohlene Form für datenhaltende Domänenalternativen

Für datenhaltende Varianten ist diese Form der Standard:

```java
sealed interface CommandResult permits Accepted, Rejected, Deferred {}

record Accepted(String commandId) implements CommandResult {}
record Rejected(String reason) implements CommandResult {}
record Deferred(String reason, java.time.Instant retryAfter) implements CommandResult {}
```

Warum diese Form gut ist:

- Der Root-Typ beschreibt das geschlossene fachliche Konzept.
- Jede Variante trägt nur die Daten, die sie wirklich benötigt.
- Records reduzieren Boilerplate.
- Der `switch` kann exhaustiv sein.
- Neue Varianten zwingen betroffene Verarbeitungspunkte zur Prüfung.
- Reviewende sehen die vollständige Variantensammlung direkt im Code.

Nicht empfohlen:

```java
class CommandResult {
    boolean accepted;
    boolean rejected;
    boolean deferred;
    String reason;
    java.time.Instant retryAfter;
}
```

Diese Form erlaubt widersprüchliche Zustände wie `accepted = true` und `rejected = true` gleichzeitig. Sie erzeugt ein Objekt, das viele ungültige Kombinationen zulässt.

Sealed Varianten verhindern solche Zustände strukturell.

---

## 16. Invariantenregeln für Varianten

Jede Variante muss ihre eigenen Invarianten schützen.

Nicht ausreichend:

```java
record Failure(String reason, int errorCode) implements PaymentResult {}
```

Besser:

```java
import java.util.Objects;

record Failure(String reason, int errorCode) implements PaymentResult {
    public Failure {
        Objects.requireNonNull(reason, "reason must not be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (errorCode < 400 || errorCode > 599) {
            throw new IllegalArgumentException("errorCode must be in HTTP error range");
        }
    }
}
```

Eine sealed Hierarchie stellt sicher, dass nur erlaubte Varianten existieren. Sie stellt nicht sicher, dass die Daten innerhalb einer Variante gültig sind. Diese Aufgabe bleibt bei Konstruktorvalidierung, Bean Validation, Factory-Methoden oder fachlichen Services.

Qualitätsregel: **Sealed modelliert die erlaubten Formen. Konstruktorvalidierung schützt die erlaubten Werte.**

---

## 17. Collections und Mutability

Wenn eine sealed Variante als Record modelliert wird und Collections enthält, gelten dieselben Regeln wie bei allen Records: Records sind nur shallow immutable. Die Komponentenreferenz ist final, aber eine mutable Collection kann weiterhin verändert werden.

Schlecht:

```java
import java.util.List;

record ImportErrors(List<String> messages) implements ImportResult {}
```

Besser:

```java
import java.util.List;
import java.util.Objects;

record ImportErrors(List<String> messages) implements ImportResult {
    public ImportErrors {
        Objects.requireNonNull(messages, "messages must not be null");
        messages = List.copyOf(messages);
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
    }
}
```

Arrays sollten in sealed Records vermieden werden, weil sie mutable bleiben und bei `equals()` sowie `hashCode()` besondere Aufmerksamkeit verlangen.

Qualitätsregel: **Varianten sollen nach Konstruktion fachlich stabil sein. Mutable Komponenten müssen defensiv kopiert oder vermieden werden.**

---

## 18. Verwendung von `non-sealed`

`non-sealed` öffnet einen zuvor geschlossenen Zweig bewusst wieder.

```java
sealed interface PaymentResult permits Success, Failure, Pending, ProviderSpecificFailure {}

record Success(String transactionId) implements PaymentResult {}
record Failure(String reason) implements PaymentResult {}
record Pending(java.time.Instant since) implements PaymentResult {}

non-sealed interface ProviderSpecificFailure extends PaymentResult {}
```

Das ist ein starkes Signal: Ab diesem Punkt ist der Zweig nicht mehr geschlossen.

`non-sealed` darf nur verwendet werden, wenn die Wiederöffnung fachlich notwendig ist. Es ist nicht als bequemer Ausweg zu verwenden, wenn das Modell noch nicht durchdacht wurde.

Erlaubte Gründe können sein:

- Ein stabiler Kern ist geschlossen, aber provider-spezifische Erweiterungen bleiben offen.
- Eine Bibliothek definiert einen kontrollierten Root-Typ, aber einzelne Erweiterungsbereiche sind bewusst offen.
- Übergangsweise wird eine alte offene Hierarchie schrittweise geschlossen.

Nicht erlaubt:

- „Wir wissen noch nicht, welche Varianten kommen.“
- „Es ist einfacher für später.“
- „Dann müssen wir nicht über die Domäne nachdenken.“

Qualitätsregel: **`non-sealed` muss im Code Review aktiv begründet werden.**

---

## 19. Paket- und Modulregeln

Die erlaubten direkten Subtypen eines sealed Typs unterliegen Java-Regeln zu Paket und Modul.

In einem named module müssen der sealed Root-Typ und seine direkt erlaubten Subtypen im selben Modul liegen.

In einem unnamed module müssen sie im selben Package liegen.

Das ist wichtig für Repository- und Modulstruktur. Eine sealed Hierarchie sollte nicht wahllos über technische Schichten verteilt werden. Sie beschreibt eine fachliche Einheit und sollte auch strukturell zusammenliegen.

Empfohlene Struktur:

```text
com.example.payment.domain
├── PaymentResult.java
├── Success.java
├── Failure.java
└── Pending.java
```

Oder bei kleinen Hierarchien in einer Datei:

```java
package com.example.payment.domain;

public sealed interface PaymentResult permits Success, Failure, Pending {}

record Success(String transactionId) implements PaymentResult {}
record Failure(String reason, int errorCode) implements PaymentResult {}
record Pending(java.time.Instant since) implements PaymentResult {}
```

Für öffentliche APIs ist besondere Vorsicht nötig. Wenn der sealed Root-Typ öffentlich ist, wird die geschlossene Variantenmenge Teil des API-Vertrags.

---

## 20. Erwartete Ergebnisse ohne Exceptions modellieren

Sealed Typen eignen sich sehr gut, um erwartbare fachliche Ergebnisse ohne Exceptions zu modellieren.

Nicht empfohlen:

```java
Payment charge(PaymentRequest request) {
    if (cardDeclined(request)) {
        throw new PaymentFailedException("Card declined");
    }
    return bookPayment(request);
}
```

Besser, wenn ein Fehler fachlich erwartbar ist:

```java
sealed interface ChargeResult permits Charged, Declined, RequiresAction {}

record Charged(String transactionId) implements ChargeResult {}
record Declined(String reason) implements ChargeResult {}
record RequiresAction(String challengeId) implements ChargeResult {}
```

Verarbeitung:

```java
void handle(ChargeResult result) {
    switch (result) {
        case Charged c -> confirm(c.transactionId());
        case Declined d -> informCustomer(d.reason());
        case RequiresAction r -> startChallenge(r.challengeId());
    }
}
```

Exceptions bleiben für technische Fehler, unerwartete Zustände und Infrastrukturprobleme sinnvoll. Erwartbare fachliche Alternativen sollten häufig als Ergebnisvarianten modelliert werden.

Qualitätsregel: **Nicht jeder negative Ausgang ist eine Exception. Viele fachliche Alternativen sind bessere sealed Result-Typen.**

---

## 21. Generische Result-Typen

Generische sealed Result-Typen können nützlich sein, sollten aber nicht das gesamte Domänenmodell ersetzen.

Beispiel:

```java
sealed interface Result<T> permits Ok, Problem {}

record Ok<T>(T value) implements Result<T> {}
record Problem<T>(String code, String message) implements Result<T> {}
```

Das kann für technische Operationen oder interne Hilfsfunktionen sinnvoll sein.

Für fachlich wichtige Ergebnisse sind spezifische Typen oft besser:

```java
sealed interface RegistrationResult
    permits Registered, EmailAlreadyUsed, WeakPassword {}

record Registered(String userId) implements RegistrationResult {}
record EmailAlreadyUsed(String email) implements RegistrationResult {}
record WeakPassword(String reason) implements RegistrationResult {}
```

Warum spezifische Typen oft besser sind:

- Varianten sind sprechender.
- Fachliche Fälle werden explizit.
- Reviews sind einfacher.
- Fehlercodes werden nicht zu String-Magie.
- Der Compiler hilft präziser.

Qualitätsregel: **Generische Result-Typen sind für technische Muster erlaubt, aber fachliche Kernfälle sollen spezifisch modelliert werden.**

---

## 22. Security-Relevanz

Sealed Typen sind kein Security-Feature im engeren Sinn. Sie ersetzen keine Authentifizierung, Autorisierung, Eingabevalidierung, Ausgabekodierung, sichere Deserialisierung oder Zugriffskontrolle.

Sie können aber sicherheitsrelevanten Kontrollfluss robuster machen, weil erlaubte Varianten explizit und exhaustiv behandelbar werden.

Gute Kandidaten im Security-Kontext:

```text
AuthorizationDecision = Granted | Denied | StepUpRequired
AuthenticationResult = Authenticated | InvalidCredentials | Locked | MfaRequired
TokenValidationResult = Valid | Expired | Revoked | Malformed
AccessCheckResult = Allowed | Forbidden | NotFound
```

Beispiel:

```java
sealed interface AuthorizationDecision permits Granted, Denied, StepUpRequired {}

record Granted(String subjectId) implements AuthorizationDecision {}
record Denied(String reason) implements AuthorizationDecision {}
record StepUpRequired(String challengeId) implements AuthorizationDecision {}
```

Verarbeitung:

```java
void continueIfAllowed(AuthorizationDecision decision) {
    switch (decision) {
        case Granted g -> continueRequest(g.subjectId());
        case Denied d -> reject(d.reason());
        case StepUpRequired s -> requireAdditionalCheck(s.challengeId());
    }
}
```

Hier ist der Sicherheitsgewinn nicht, dass `sealed` selbst schützt. Der Gewinn liegt darin, dass `Denied` und `StepUpRequired` nicht stillschweigend vergessen werden können, wenn der Kontrollfluss exhaustiv modelliert wird.

Security-Regel: **Bei sicherheitsrelevanten sealed Typen müssen alle Varianten fail-closed verarbeitet werden.**

Nicht erlaubt:

```java
void continueIfAllowed(AuthorizationDecision decision) {
    if (decision instanceof Granted g) {
        continueRequest(g.subjectId());
    }
    // Denied und StepUpRequired werden ignoriert.
}
```

Dieser Code ist in Security-relevantem Kontrollfluss unzulässig.

---

## 23. SaaS-Regel: Tenant-Kontext darf nicht aus einer unvertrauenswürdigen Variante übernommen werden

In SaaS-Systemen ist Mandantentrennung ein kritischer Qualitäts- und Sicherheitsaspekt. Sealed Varianten dürfen nicht dazu führen, dass Entwickler `tenantId` aus externen Request-Daten ungeprüft übernehmen.

Gefährlich:

```java
sealed interface TenantCommand permits CreateOrder, CancelOrder {}

record CreateOrder(String tenantId, String productId) implements TenantCommand {}
record CancelOrder(String tenantId, String orderId) implements TenantCommand {}
```

Wenn `tenantId` aus dem Request Body kommt, kann ein Angreifer versuchen, auf fremde Mandanten zuzugreifen.

Besser:

```java
record TenantContext(String tenantId, String subjectId) {}

sealed interface TenantCommand permits CreateOrder, CancelOrder {}

record CreateOrder(String productId) implements TenantCommand {}
record CancelOrder(String orderId) implements TenantCommand {}
```

Verarbeitung:

```java
void handle(TenantContext context, TenantCommand command) {
    switch (command) {
        case CreateOrder c -> createOrder(context.tenantId(), c.productId());
        case CancelOrder c -> cancelOrder(context.tenantId(), c.orderId());
    }
}
```

Die `tenantId` stammt hier aus einem authentifizierten und autorisierten Kontext, nicht aus einer beliebigen Variante des Request-Modells.

Qualitätsregel: **Mandantenkontext gehört in den Sicherheitskontext, nicht ungeprüft in fachliche Request-Varianten.**

---

## 24. Logging und sensible Daten

Sealed Varianten werden häufig als Records modelliert. Records erzeugen automatisch `toString()`. Das ist praktisch, kann aber sensible Daten offenlegen.

Gefährlich:

```java
sealed interface AuthenticationResult permits Authenticated, InvalidCredentials {}

record Authenticated(String userId, String accessToken) implements AuthenticationResult {}
record InvalidCredentials(String username, String password) implements AuthenticationResult {}
```

Wenn solche Objekte geloggt werden, können Tokens oder Passwörter sichtbar werden.

Besser:

```java
record Authenticated(String userId, String accessToken) implements AuthenticationResult {
    @Override
    public String toString() {
        return "Authenticated[userId=%s, accessToken=<redacted>]".formatted(userId);
    }
}

record InvalidCredentials(String username, String password) implements AuthenticationResult {
    @Override
    public String toString() {
        return "InvalidCredentials[username=%s, password=<redacted>]".formatted(username);
    }
}
```

Noch besser ist häufig: sensible Werte gar nicht in solchen Ergebnisobjekten speichern, wenn sie dort nicht zwingend benötigt werden.

Review-Regel:

```text
Wenn eine sealed Variante Felder wie password, token, secret, credential, apiKey, sessionId, refreshToken oder accessToken enthält, muss Logging explizit geprüft werden.
```

---

## 25. Serialisierung und API-Grenzen

Sealed Typen können intern sehr nützlich sein. An externen API-Grenzen sind sie jedoch vorsichtig zu verwenden.

Polymorphe JSON-Deserialisierung kann gefährlich werden, wenn Typinformationen aus unvertrauenswürdigen Eingaben übernommen werden oder wenn zu breite Subtyp-Mengen erlaubt sind.

Interner Einsatz:

```java
sealed interface PaymentResult permits Success, Failure, Pending {}
```

Das ist für Service- und Domänencode gut geeignet.

Externe API-Grenze:

```json
{
  "type": "Success",
  "transactionId": "tx-123"
}
```

Hier muss genau geprüft werden:

- Welche `type`-Werte sind erlaubt?
- Werden unbekannte Typen abgelehnt?
- Gibt es eine Allowlist?
- Wird externe Typinformation sicher gemappt?
- Werden sensible Felder ausgeschlossen?
- Gibt es stabile API-Versionierung?

Qualitätsregel: **Sealed Typen dürfen interne Klarheit schaffen, aber polymorphe externe Deserialisierung muss explizit abgesichert werden.**

Für öffentliche APIs kann eine flachere DTO-Struktur stabiler sein als eine direkt veröffentlichte sealed Hierarchie.

---

## 26. Framework-Hinweise

### Jackson

Jackson kann mit sealed Typen und Records arbeiten, benötigt bei polymorpher Serialisierung/Deserialisierung aber häufig explizite Typinformationen und Annotationen. Diese Konfiguration muss bewusst erfolgen und darf nicht pauschal für unbekannte Subtypen geöffnet werden.

Prüffragen:

- Wird eine explizite Allowlist von Subtypen verwendet?
- Werden unbekannte Typen abgelehnt?
- Ist die Typinformation Teil eines stabilen API-Vertrags?
- Werden sensible Felder ausgeschlossen oder maskiert?

### Spring Boot

Spring Boot unterstützt Records in vielen Binding-Kontexten. Sealed Hierarchien sollten aber nicht blind für externe Request-Bodies verwendet werden, wenn die Deserialisierung polymorph ist. Für interne Service-Ergebnisse und Domänenmodelle sind sealed Typen oft sehr gut geeignet.

### JPA

Sealed Typen sind nicht als Standardmodell für JPA-Entities zu verwenden. JPA-Entities haben Anforderungen an Konstruktoren, Proxies, Vererbung und Persistenzzustand, die schlecht zu sealed Records passen können.

Empfehlung:

```text
Persistenzmodell: klassische Entity oder Embeddable prüfen
Domänen-/Service-Ergebnis: sealed Typ prüfen
API-DTO: je nach Stabilitäts- und Serialisierungsanforderung prüfen
```

### Apache Wicket

In Wicket-Anwendungen können sealed Records für View Models, DTOs oder Ergebnisobjekte sinnvoll sein. Dabei muss beachtet werden, dass Wicket Seiten und Komponenten serialisieren kann. Referenzierte Objekte müssen serialisierbar sein oder über detachable Models verwaltet werden.

Prüffragen:

- Wird ein sealed Variantentyp in einer Wicket-Komponente gehalten?
- Ist die Variante serialisierbar?
- Enthält sie nicht-serialisierbare Services, Lambdas oder Ressourcen?
- Sollte stattdessen ein Loadable-/Detachable-Model verwendet werden?

---

## 27. Anti-Patterns

### 27.1 Sealed nur wegen Modernität

Schlecht:

```java
sealed interface Name permits FirstName, LastName {}
record FirstName(String value) implements Name {}
record LastName(String value) implements Name {}
```

Wenn kein echter geschlossener fachlicher Alternativenraum existiert, ist das übermodelliert.

### 27.2 Offene Extension Points versiegeln

Schlecht:

```java
public sealed interface NotificationChannel permits EmailChannel, SmsChannel {}
```

Wenn später Push, Webhook, Partnerkanäle oder kundenspezifische Kanäle ergänzt werden sollen, ist sealing falsch.

### 27.3 `default` versteckt neue Fälle

Schlecht:

```java
return switch (result) {
    case Success s -> "ok";
    default -> "ignored";
};
```

Das kann neue fachliche Varianten verschlucken.

### 27.4 `non-sealed` ohne Begründung

Schlecht:

```java
non-sealed interface Failure extends PaymentResult {}
```

Ohne klare fachliche Begründung wird dadurch der Nutzen der geschlossenen Hierarchie reduziert.

### 27.5 Sealed Variante enthält ungültige Zustände

Schlecht:

```java
record Rejected(String reason) implements ImportResult {}
```

Wenn `reason` `null` oder leer sein kann, ist die Variante formal erlaubt, aber fachlich ungültig.

### 27.6 Booleans statt Varianten

Schlecht:

```java
class AccessResult {
    boolean granted;
    boolean denied;
    boolean stepUpRequired;
}
```

Diese Struktur erlaubt widersprüchliche Zustände. Ein sealed Typ ist hier deutlich stärker.

### 27.7 Sensible Daten in `toString()`

Schlecht:

```java
record MfaRequired(String userId, String oneTimePassword) implements AuthenticationResult {}
```

Records erzeugen automatisch `toString()`. Sensible Werte können in Logs auftauchen.

### 27.8 Externe Request-Typen direkt als Domänenvarianten verwenden

Schlecht:

```java
sealed interface PaymentCommand permits PayByCard, PayByInvoice {}
record PayByCard(String tenantId, String cardNumber) implements PaymentCommand {}
```

Hier werden Mandantenkontext und sensible Zahlungsdaten direkt in ein fachliches Kommandomodell gelegt. Das muss kritisch geprüft und meist getrennt werden.

---

## 28. Entscheidungsbaum

```text
1. Gibt es eine endliche, bekannte Menge fachlicher Varianten?
   Nein -> kein sealed Typ.
   Ja  -> weiter.

2. Sollen alle Varianten in Handlern vollständig behandelt werden?
   Nein -> enum, Strategie, offene Schnittstelle oder klassisches Modell prüfen.
   Ja  -> weiter.

3. Tragen Varianten unterschiedliche Daten?
   Nein -> enum prüfen.
   Ja  -> sealed interface + records prüfen.

4. Soll die Hierarchie von Dritten erweitert werden können?
   Ja  -> kein sealed Typ oder bewusst non-sealed-Zweig.
   Nein -> weiter.

5. Liegen Varianten in derselben Ownership-, Paket- oder Modulgrenze?
   Nein -> Struktur überarbeiten oder sealing vermeiden.
   Ja  -> sealed Typ geeignet.

6. Enthalten Varianten sensible Daten?
   Ja  -> Logging, toString, Serialisierung und Redaction prüfen.
   Nein -> Standardregeln anwenden.
```

---

## 29. Pull-Request-Checkliste

Bei sealed Typen müssen im Review folgende Fragen beantwortet werden:

- Ist die Variantenmenge fachlich wirklich geschlossen?
- Ist ein `enum` ausreichend oder braucht jede Variante eigene Daten?
- Ist der Root-Typ als `sealed interface` oder `sealed abstract class` passend gewählt?
- Sind alle direkten Subtypen bewusst `final`, `sealed` oder `non-sealed`?
- Ist `non-sealed` vorhanden? Wenn ja: Ist es fachlich begründet?
- Werden alle Varianten in `switch`-Ausdrücken exhaustiv behandelt?
- Gibt es einen unnötigen `default`-Zweig?
- Werden `null`-Werte ausgeschlossen oder bewusst modelliert?
- Schützen Varianten ihre eigenen Invarianten?
- Enthalten Varianten mutable Collections oder Arrays?
- Werden mutable Komponenten defensiv kopiert?
- Enthalten Varianten sensible Felder?
- Ist `toString()` bei sensiblen Feldern abgesichert?
- Wird Mandantenkontext aus vertrauenswürdiger Quelle bezogen?
- Wird externe polymorphe Deserialisierung sicher behandelt?
- Ist die Paket- und Modulstruktur mit Java-Regeln kompatibel?
- Sind alle Varianten sinnvoll getestet?
- Wird eine offene Schnittstelle versehentlich geschlossen?
- Wird eine fachliche State Machine korrekt als Variantenraum modelliert?

---

## 30. Automatisierbare Prüfungen

Nicht jede Regel ist vollständig automatisierbar, aber mehrere Qualitätsprüfungen können unterstützt werden.

Mögliche Prüfungen:

```text
- ArchUnit: Sealed Typen in Domain-Packages dürfen nur erlaubte Subtypen im selben Package/Modul haben.
- ArchUnit: Klassen mit Namen *Result, *Decision oder *Outcome sollen bei mehreren fachlichen Varianten als sealed Typ geprüft werden.
- Semgrep: Records mit Feldern wie password, token, secret, apiKey oder credential markieren.
- Semgrep: switch über sealed Root-Typ mit default-Zweig markieren.
- Checkstyle/PMD: Lange if-instanceof-Ketten in Domain-Handlern markieren.
- SpotBugs/Review-Gate: Serializable-Kontext in Wicket prüfen.
```

Beispielhafte Review-Gate-Regel:

```text
Wenn eine neue Variante zu einer sealed Hierarchie hinzugefügt wird, müssen alle Handler, Tests und API-Mappings sichtbar angepasst oder bewusst als unverändert begründet werden.
```

Automatisierung soll Reviews unterstützen, nicht ersetzen. Die wichtigste Entscheidung — ob eine Variantenmenge fachlich wirklich geschlossen ist — bleibt eine Designentscheidung.

---

## 31. Migrationsleitfaden

### 31.1 Von offener Hierarchie zu sealed Hierarchie

Ausgangspunkt:

```java
abstract class PaymentResult {}
class Success extends PaymentResult {}
class Failure extends PaymentResult {}
```

Schritt 1: Varianten vollständig erfassen.

```text
Welche direkten Subtypen existieren?
Wer erzeugt sie?
Wer verarbeitet sie?
Gibt es externe Erweiterungen?
```

Schritt 2: Prüfen, ob die Hierarchie wirklich geschlossen sein darf.

Wenn externe Erweiterungen existieren, ist sealing möglicherweise falsch oder nur für einen Teilbaum geeignet.

Schritt 3: Root-Typ versiegeln.

```java
sealed interface PaymentResult permits Success, Failure, Pending {}
```

Schritt 4: Varianten als Records oder finale Klassen modellieren.

```java
record Success(String transactionId) implements PaymentResult {}
record Failure(String reason, int errorCode) implements PaymentResult {}
record Pending(java.time.Instant since) implements PaymentResult {}
```

Schritt 5: `if instanceof`-Ketten durch `switch` ersetzen.

```java
switch (result) {
    case Success s -> book(s.transactionId());
    case Failure f -> alert(f.reason());
    case Pending p -> queue(p.since());
}
```

Schritt 6: Tests pro Variante ergänzen.

Schritt 7: Logging, Serialisierung und API-Grenzen prüfen.

### 31.2 Von Boolean-Statusobjekt zu sealed Varianten

Ausgangspunkt:

```java
class ValidationResult {
    boolean valid;
    String errorMessage;
}
```

Besser:

```java
sealed interface ValidationResult permits Valid, Invalid {}
record Valid() implements ValidationResult {}
record Invalid(String message) implements ValidationResult {}
```

Vorteil: Es ist unmöglich, `valid = true` und gleichzeitig eine Fehlermeldung als vermeintlich aktiven Zustand zu transportieren.

### 31.3 Von Exception-Fluss zu Result-Typ

Wenn ein fachlich erwartbarer Ausgang häufig über Exceptions modelliert wird, prüfe eine sealed Result-Hierarchie.

Nicht jedes Scheitern ist außergewöhnlich. Viele Ergebnisse sind normale fachliche Alternativen.

---

## 32. Definition of Done

Ein sealed Domänentyp erfüllt diese Richtlinie, wenn alle folgenden Kriterien erfüllt sind:

1. Die Variantenmenge ist fachlich bekannt und bewusst geschlossen.
2. Der Root-Typ ist als `sealed interface` oder `sealed abstract class` modelliert.
3. Alle direkten Subtypen sind bewusst `final`, `sealed` oder `non-sealed`.
4. `non-sealed` ist nur mit klarer fachlicher Begründung vorhanden.
5. Datenhaltende Varianten sind bevorzugt als Records modelliert.
6. Varianten schützen ihre eigenen Invarianten.
7. Mutable Komponenten werden defensiv kopiert oder vermieden.
8. `switch`-Ausdrücke über die Hierarchie sind exhaustiv.
9. Es gibt keinen unnötigen `default`-Zweig.
10. `null` wird nicht als versteckte fachliche Variante verwendet.
11. Sensible Daten werden nicht über `toString()` oder Logs offengelegt.
12. Mandantenkontext stammt nicht aus unvertrauenswürdigen Request-Varianten.
13. Polymorphe Deserialisierung ist explizit abgesichert oder wird vermieden.
14. Paket- und Modulregeln sind eingehalten.
15. Jede Variante ist durch Tests abgedeckt.
16. Reviewende können den fachlichen Variantenraum direkt im Code erkennen.

---

## 33. Namensregeln

Root-Typen sollten fachlich sprechen:

```text
PaymentResult
AuthorizationDecision
ImportOutcome
ValidationResult
CommandResult
InvoiceState
TokenValidationResult
```

Varianten sollten konkrete fachliche Bedeutung tragen:

```text
PaymentAuthorized
PaymentDeclined
PaymentRequiresAction
InvoicePaid
InvoiceCancelled
AccessDenied
StepUpRequired
```

Schwache Namen:

```text
Success
Failure
Other
Unknown
VariantA
VariantB
```

Kurze Namen wie `Success` und `Failure` sind akzeptabel, wenn Package und Root-Typ die Bedeutung eindeutig machen. Für öffentliche APIs und große Domänen sind spezifischere Namen vorzuziehen.

Vermeide `Other` und `Unknown` als bequeme Sammelvarianten. Solche Varianten können die fachliche Genauigkeit der Hierarchie zerstören. Wenn eine unbekannte externe Eingabe verarbeitet werden muss, ist ein expliziter Fehler- oder Ablehnungsfall meist besser.

---

## 34. Testleitfaden

Tests sollen jede Variante explizit abdecken.

Beispiel:

```java
class PaymentResultHandlerTest {

    @Test
    void handlesSuccess() {
        var result = new Success("tx-123");
        // assert booking behavior
    }

    @Test
    void handlesFailure() {
        var result = new Failure("Card declined", 402);
        // assert alert or rejection behavior
    }

    @Test
    void handlesPending() {
        var result = new Pending(java.time.Instant.now());
        // assert queue behavior
    }
}
```

Eine sealed Hierarchie sollte Testdesign einfacher machen, weil die Variantenmenge explizit ist. Wenn Entwickler die Varianten in Tests nicht leicht aufzählen können, ist das Modell möglicherweise nicht so geschlossen, wie es sein soll.

Für sicherheitsrelevante sealed Typen sind zusätzlich Negativtests erforderlich:

```text
- Denied führt nicht zur Fortsetzung des geschützten Flusses.
- StepUpRequired wird nicht wie Granted behandelt.
- Unbekannte externe Varianten schlagen fail-closed fehl.
- Sensible Daten erscheinen nicht in Logausgaben.
- Tenant-IDs aus Request-Bodies werden ignoriert oder abgelehnt.
```

---

## 35. Häufige Fragen

### Soll jedes Interface sealed werden?

Nein. Die meisten Interfaces sind Erweiterungspunkte. Sealed Interfaces sind für geschlossene Alternativen gedacht, nicht für allgemeine Abstraktion.

### Soll jedes enum durch sealed Typen ersetzt werden?

Nein. Enums sind hervorragend für einfache benannte Konstanten. Sealed Hierarchien sind sinnvoll, wenn Varianten unterschiedliche Daten oder unterschiedliches Verhalten benötigen.

### Sind sealed Typen immer besser als das Visitor Pattern?

Nein. Für viele interne Domänenmodelle ist Java-21-Pattern-Matching über sealed Typen einfacher als ein Visitor. Das Visitor Pattern kann weiterhin sinnvoll sein, wenn häufig neue Operationen ergänzt werden sollen, ohne die Variantenklassen zu ändern.

### Darf ein sealed Root-Typ public sein?

Ja. Dann wird die geschlossene Variantenmenge aber Teil des öffentlichen API-Vertrags. Das Ergänzen einer neuen permitted Variante kann für Clients eine Kompatibilitätsfrage werden.

### Können sealed Typen mit Records kombiniert werden?

Ja. Das ist für geschlossene datenhaltende Alternativen häufig die bevorzugte Form.

### Schützt Sealing gegen bösartige Eingaben?

Nein. Sealing beschränkt Java-Vererbung auf Typ-Hierarchie-Ebene. Es validiert keine externen Eingaben, erzwingt keine Autorisierung und macht Deserialisierung nicht automatisch sicher.

---

## 36. Quellen und Validierungsbasis

Die technischen Aussagen dieser Richtlinie wurden gegen folgende Quellen geprüft:

- OpenJDK JEP 409 — Sealed Classes: https://openjdk.org/jeps/409
- OpenJDK JEP 441 — Pattern Matching for switch: https://openjdk.org/jeps/441
- Oracle Java 21 Documentation — Sealed Classes and Interfaces: https://docs.oracle.com/en/java/javase/21/language/sealed-classes-and-interfaces.html
- Oracle Java 21 Documentation — Pattern Matching for switch: https://docs.oracle.com/en/java/javase/21/language/pattern-matching-switch.html
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Deserialization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Deserialization_Cheat_Sheet.html
- OWASP Mass Assignment Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Mass_Assignment_Cheat_Sheet.html
- OWASP Secure Code Review Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secure_Code_Review_Cheat_Sheet.html
- Jackson Documentation — Polymorphic Deserialization: https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization
- Apache Wicket 10 User Guide: https://nightlies.apache.org/wicket/guide/10.x/single.html

---

## 37. Schlussregel

Verwende sealed Typen, wenn die Domäne geschlossen ist, die Varianten fachlich bedeutsam sind und der Compiler bei vollständiger Fallbehandlung helfen soll.

Verwende sealed Typen nicht, nur weil sie modern aussehen.

Der Qualitätsgewinn entsteht dann, wenn echte fachliche Grenzen sichtbar im Typsystem modelliert werden. Eine sealed Hierarchie ist gut, wenn sie vergessene Fälle verhindert, ungültige Erweiterung unmöglich macht und Entwickler zwingt, bedeutsame Alternativen bewusst zu behandeln.
