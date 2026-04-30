# ADR-009 — Clean Code: ADR-Entscheidungen im Quellcode sichtbar machen

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2025-05-06                        |
| Kategorie  | Clean Code / Dokumentation        |

---

## Kontext & Problem

ADRs (Architecture Decision Records) dokumentieren *warum* etwas so gebaut wurde — nicht nur *wie*. Das Problem: ADR-Dokumente liegen in einem `docs/`-Ordner und werden beim täglichen Coding nicht gesehen. Entwickler stoßen auf Entscheidungen im Code und verstehen den Kontext nicht — warum ist das eine `sealed interface`? Warum kein reaktiver Stack? Warum ist diese Klasse `final`?

Das Resultat: Entscheidungen werden unbewusst rückgängig gemacht, weil niemand weiß, dass sie bewusst getroffen wurden.

Dieses ADR definiert, wie ADR-Entscheidungen **direkt im Quellcode** sichtbar und durchsetzbar gemacht werden.

---

## Prinzip: Code kommentiert das *Warum*, nicht das *Was*

```java
// ❌ Schlechter Kommentar — erklärt was der Code tut, nicht warum
// Addiert alle Preise zusammen
BigDecimal total = items.stream()
    .map(Item::price)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// ✅ Guter Kommentar — erklärt die Entscheidung
// Streaming statt for-loop: konsistent mit ADR-003 (Pattern Matching & Streams).
// Kein parallelStream(): Reihenfolge der Preisaddition muss deterministisch bleiben
// für Buchhaltungs-Audit-Trail.
BigDecimal total = items.stream()
    .map(Item::price)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

---

## Methode 1 — `@ADR`-Annotation als lebende Verknüpfung

Erstelle eine eigene `@ADR`-Annotation, die ADR-Nummern direkt an Klassen, Methoden oder Felder koppelt. Die Annotation ist dokumentarisch und optionally durch ArchUnit prüfbar.

### Annotation definieren:

```java
package com.example.arch;

import java.lang.annotation.*;

/**
 * Verknüpft Code-Stellen mit Architecture Decision Records (ADRs).
 * Beschreibt WARUM eine Entscheidung so getroffen wurde.
 *
 * Konvention:
 *   @ADR(id = "ADR-004", reason = "Virtual Threads statt reaktivem Stack (Spring WebFlux).")
 */
@Retention(RetentionPolicy.SOURCE)   // Nur zur Compile-Zeit sichtbar, kein Runtime-Overhead
@Target({
    ElementType.TYPE,
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.CONSTRUCTOR
})
@Documented
@Repeatable(ADRs.class)
public @interface ADR {

    /** ADR-Nummer, z. B. "ADR-004" */
    String id();

    /** Kurze Begründung im Kontext dieser Code-Stelle */
    String reason() default "";
}
```

```java
// Container für mehrere ADRs an derselben Stelle
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface ADRs {
    ADR[] value();
}
```

### Annotation im Code verwenden:

```java
// Auf Klassenebene: erklärt die fundamentale Design-Entscheidung der Klasse
@ADR(
    id     = "ADR-002",
    reason = "Sealed Interface statt abstrakter Klasse: alle Varianten sind zur "
           + "Compile-Zeit bekannt. Compiler prüft Exhaustiveness bei Switch-Expressions."
)
public sealed interface PaymentResult permits Success, Failure, Pending {}


// Auf Methodenebene: erklärt eine spezifische Implementierungsentscheidung
@ADR(
    id     = "ADR-004",
    reason = "Kein @Async hier: Virtual Threads (spring.threads.virtual.enabled=true) "
           + "übernehmen das Non-Blocking. Blocking-Code ist mit Loom wieder akzeptabel."
)
@Transactional(readOnly = true)
public List<OrderSummaryDto> findPendingOrders(UserId userId) {
    return orderRepository.findByUserIdAndStatus(userId, PENDING);
}


// Auf Feldebene: erklärt Typwahl
@ADR(
    id     = "ADR-008",
    reason = "UserId statt Long: Primitive Obsession verhindern. "
           + "Typsicherheit verhindert Verwechslung mit anderen Long-Parametern."
)
private final UserId ownerId;


// Mehrere ADRs an einer Stelle
@ADR(id = "ADR-001", reason = "Record statt JavaBean: immutable DTO, kein Boilerplate.")
@ADR(id = "ADR-007", reason = "Kein Optional<List>: leere Liste ist das korrekte Leer-Signal.")
public record OrderSearchResult(
    List<OrderSummaryDto> orders,
    int totalCount
) {}
```

---

## Methode 2 — ADR-Referenzen in Javadoc

Für öffentliche APIs und zentrale Architekturklassen gehört die ADR-Referenz in den Javadoc-Kommentar — so ist sie in der IDE direkt sichtbar.

```java
/**
 * Zentrale Domänenklasse für Bestellungen.
 *
 * <p>Implementiert das Rich Domain Model Prinzip (gegen das Anämische Domänenmodell):
 * Alle Zustandsübergänge und Geschäftsregeln leben in dieser Klasse.
 * Services koordinieren nur, entscheiden nicht.
 *
 * @see <a href="../../../docs/adr/ADR-008-falsche-objektorientierung.md">ADR-008</a>
 * @see <a href="../../../docs/adr/ADR-001-records-statt-javabeans.md">ADR-001</a>
 */
@Entity
public class Order {

    /**
     * Storniert diese Bestellung.
     *
     * <p>Geschäftsregel: Nur Bestellungen im Status {@code PENDING} oder {@code PROCESSING}
     * können storniert werden. Zustandsübergang ist atomar und in dieser Methode gekapselt.
     *
     * @throws OrderCannotBeCancelledException wenn der aktuelle Status keine Stornierung erlaubt.
     * @see <a href="../../../docs/adr/ADR-008-falsche-objektorientierung.md#fehler-1">ADR-008, Abschnitt: Anämisches Domänenmodell</a>
     */
    public void cancel() {
        if (status == DELIVERED || status == CANCELLED) {
            throw new OrderCannotBeCancelledException(id, status);
        }
        this.status = CANCELLED;
    }
}
```

---

## Methode 3 — ArchUnit: ADR-Entscheidungen automatisch prüfen

ArchUnit ermöglicht es, Architekturregeln als Unit-Tests zu schreiben — so werden ADR-Entscheidungen nicht nur dokumentiert, sondern auch **maschinell durchgesetzt**.

```java
// build.gradle
// testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'

@AnalyzeClasses(packages = "com.example")
public class ArchitectureRulesTest {

    // ADR-006: Services dürfen nicht direkt auf Entities anderer Aggregate zugreifen
    @ArchTest
    static final ArchRule services_duerfen_keine_fremden_entities_referenzieren =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain..entity..")
            .andShould().haveSimpleNameNotEndingWith("Repository")
            .because("ADR-006: Services arbeiten mit DTOs und Repositories, nicht direkt mit Entities.");

    // ADR-008: Keine Utils-Klassen erlaubt
    @ArchTest
    static final ArchRule keine_utils_klassen =
        noClasses()
            .should().haveSimpleNameEndingWith("Utils")
            .orShould().haveSimpleNameEndingWith("Helper")
            .orShould().haveSimpleNameEndingWith("Util")
            .because("ADR-008: Utility-Klassen sind ein Anti-Pattern. "
                   + "Verhalten gehört in Domänentypen oder dedizierte Services.");

    // ADR-007: Optional darf nicht als Feldtyp verwendet werden
    @ArchTest
    static final ArchRule kein_optional_als_feld =
        noFields()
            .should().haveRawType(java.util.Optional.class)
            .because("ADR-007: Optional ist kein Feldtyp. "
                   + "Nullable Felder mit @Nullable annotieren.");

    // ADR-006: Controller dürfen nicht auf Repositories zugreifen
    @ArchTest
    static final ArchRule controller_nicht_auf_repositories =
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("ADR-006: Controller → Service → Repository. "
                   + "Controller kennen keine Repositories.");

    // ADR-008: Keine statischen Methoden in Domänenklassen (ausser Factory-Methoden)
    @ArchTest
    static final ArchRule keine_statischen_methoden_in_domain =
        noMethods()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().areStatic()
            .and().areNotAnnotatedWith(ADR.class) // Explizit annotierte Ausnahmen erlaubt
            .should().beDeclaredInClassesThat().haveSimpleNameEndingWith("Factory")
            .because("ADR-008: Statische Methoden in Domänenklassen sind ein Zeichen für "
                   + "fehlende Objektorientierung. Verhalten gehört zur Instanz.");

    // ADR-004: @Async nicht in Service-Klassen verwenden (Virtual Threads übernehmen das)
    @ArchTest
    static final ArchRule kein_async_in_services =
        noMethods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
            .should().beAnnotatedWith(Async.class)
            .because("ADR-004: Virtual Threads sind aktiviert. @Async ist nicht mehr "
                   + "nötig und verschleiert den Kontrollfluss.");
}
```

---

## Methode 4 — `// ADR-XXX:`-Kommentarkonvention für kritische Stellen

Für Stellen, die auf den ersten Blick kontraintuitiv wirken, ist ein inline Kommentar mit ADR-Referenz das direkte Signal an den nächsten Entwickler.

### Konvention:

```
// ADR-XXX: <Ein-Satz-Erklärung warum diese Entscheidung hier gilt>
```

### Beispiele:

```java
// ADR-004: Kein CompletableFuture hier. Virtual Threads machen blocking Code skalierbar.
var user  = userRepository.findById(userId).orElseThrow();
var stats = statsService.loadFor(userId); // Blockiert — ist mit Loom OK.

// ADR-007: Kein Optional.get() — orElseThrow() mit sprechender Exception ist Pflicht.
var order = orderRepository.findById(id)
    .orElseThrow(() -> new OrderNotFoundException(id)); // ADR-007: nicht .get()

// ADR-008: ReentrantLock statt synchronized — Virtual Thread Pinning verhindern.
// synchronized würde den carrier thread blockieren; ReentrantLock gibt ihn frei.
private final ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // kritischer Abschnitt
} finally {
    lock.unlock();
}

// ADR-002: Default-Branch explizit als Programmierfehler kennzeichnen.
// Dieser Switch ist über ein sealed interface — der default darf nie eintreten.
default -> throw new AssertionError("Unbekannte PaymentResult-Variante: " + result);
```

---

## Methode 5 — ADR-Index im Package-Info

Jedes Paket kann eine `package-info.java` haben, die den architekturellen Kontext und geltende ADRs für das gesamte Paket dokumentiert.

```java
/**
 * Domänenschicht: Order-Aggregat.
 *
 * <p>Dieses Paket enthält das Rich Domain Model für Bestellungen.
 * Alle Business-Regeln und Zustandsübergänge leben in den Domänenklassen selbst.
 *
 * <h2>Geltende ADRs</h2>
 * <ul>
 *   <li><b>ADR-008</b>: Rich Domain Model — kein anämisches Modell, Verhalten in Entitäten</li>
 *   <li><b>ADR-001</b>: Records für Value Objects und DTOs innerhalb des Aggregats</li>
 *   <li><b>ADR-002</b>: Sealed Interfaces für Domänenzustand-Hierarchien</li>
 *   <li><b>ADR-007</b>: Kein Optional als Feldtyp, keine statischen Utility-Methoden</li>
 * </ul>
 *
 * <h2>Verboten in diesem Paket</h2>
 * <ul>
 *   <li>Keine Spring-Annotationen außer {@code @Entity}, {@code @Embeddable}</li>
 *   <li>Keine direkte Abhängigkeit auf andere Aggregate</li>
 *   <li>Keine statischen Hilfsmethoden (→ ADR-008)</li>
 * </ul>
 */
@ADR(id = "ADR-008", reason = "Rich Domain Model für das Order-Aggregat.")
package com.example.domain.order;

import com.example.arch.ADR;
```

---

## Zusammenfassung: Wann welche Methode?

| Situation | Methode |
|---|---|
| Fundamentale Design-Entscheidung einer Klasse | `@ADR`-Annotation auf Klasse + Javadoc |
| Kontraintuitiver Code (warum kein async? warum kein Optional?) | `// ADR-XXX:`-Inline-Kommentar |
| Öffentliche API mit architekturellem Kontext | Javadoc mit `@see`-Link auf ADR |
| Regel die nie verletzt werden darf | ArchUnit-Test |
| Geltende Regeln für ein ganzes Paket | `package-info.java` |

---

## Konsequenzen

**Positiv:**
- ADRs sterben nicht im `docs/`-Ordner — sie leben im Code, wo Entscheidungen sichtbar sind.
- Neue Entwickler verstehen sofort *warum*, nicht nur *was*.
- ArchUnit macht Entscheidungen zu Tests — Rückschritte werden im CI/CD aufgedeckt.
- Reviews werden fokussierter: "Warum weichst du von ADR-004 ab?" statt "Warum kein @Async?"

**Negativ:**
- `@ADR`-Annotationen müssen gepflegt werden — wenn sich ein ADR ändert, müssen alle Annotationen aktualisiert werden. Empfehlung: ADR-Nummer stabil halten, neues ADR für geänderte Entscheidungen.
- ArchUnit-Tests erhöhen die Testlaufzeit moderat — durch `@AnalyzeClasses`-Scope begrenzen.

---

## Tipps

- **Annotation als Pflicht im Review**: Neue Klassen, die eine ADR-Entscheidung umsetzen, müssen `@ADR` tragen — das ist ein Review-Kriterium.
- **`@ADR(reason = "")` nicht leer lassen**: Der `reason` ist der eigentliche Mehrwert — er erklärt den *lokalen* Kontext, nicht nur den ADR-Titel.
- **ArchUnit in die CI-Pipeline**: ArchUnit-Tests laufen als normale JUnit-Tests — einfach in die bestehende Test-Suite aufnehmen.
- **Archunit-Verletzungen kommentieren**: Wenn eine Ausnahme nötig ist: `@SuppressWarnings` reicht nicht — ein `// ADR-XXX: Ausnahme weil ...`-Kommentar ist Pflicht.
- **Lebende Dokumentation**: Der `docs/adr/`-Ordner + `@ADR`-Annotationen + ArchUnit-Tests bilden zusammen eine dreifach verankerte Architekturentscheidung: Dokument → Code → Test.
 