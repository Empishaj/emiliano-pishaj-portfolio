# ADR-048 — JavaDoc: Standards & Dokumentationsqualität

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21                                |
| Datum      | 2024-01-01                        |
| Kategorie  | Dokumentation / Clean Code        |

---

## Kontext & Problem

JavaDoc ist kein optionaler Kommentar — für öffentliche APIs ist es der Vertrag. Schlechtes JavaDoc ist schlimmer als keines: es lügt, veraltet und schafft falsches Vertrauen. Dieses ADR definiert verbindliche Standards für was dokumentiert wird, wie es dokumentiert wird, und was explizit nicht dokumentiert werden soll.

---

## Goldene Regel: Dokumentiere das Warum, nicht das Was

```java
// ❌ Schlecht: Kommentar wiederholt den Code
/**
 * Gibt den Namen zurück.
 * @return der Name
 */
public String getName() { return name; }

// ❌ Schlecht: Implementierungsdetails statt Vertrag
/**
 * Ruft userRepository.findById() auf und wirft eine Exception wenn nicht gefunden.
 */
public User findById(Long id) { ... }

// ✅ Gut: Vertrag dokumentiert (Vor-/Nachbedingungen, Exceptions, Semantik)
/**
 * Findet einen aktiven Benutzer anhand seiner eindeutigen ID.
 *
 * <p>Diese Methode garantiert dass der zurückgegebene User den Status
 * {@link UserStatus#ACTIVE} hat. Für alle anderen Status-Werte wird eine
 * Exception geworfen, da inaktive User keine API-Operationen durchführen dürfen.
 *
 * @param  id  die eindeutige User-ID; muss positiv sein
 * @return den aktiven User mit der gegebenen ID
 * @throws UserNotFoundException      wenn kein User mit dieser ID existiert
 * @throws UserDeactivatedException   wenn der User existiert aber nicht aktiv ist
 * @throws IllegalArgumentException   wenn {@code id} ≤ 0
 */
public User findActiveById(Long id) { ... }
```

---

## Was MUSS dokumentiert werden?

```java
/**
 * Verwaltet den Lebenszyklus von Bestellungen.
 *
 * <p>Diese Klasse implementiert den Use Case "Bestellung aufgeben" gemäß
 * dem Domänenmodell (→ ADR-023). Sie koordiniert Validierung, Persistenz
 * und Event-Publizierung ohne selbst Business-Entscheidungen zu treffen.
 *
 * <p>Thread-Safety: Diese Klasse ist thread-sicher. Alle Zustands-
 * änderungen laufen über {@link @Transactional}-Methoden.
 *
 * @see PlaceOrderUseCase
 * @see <a href="../../../docs/adr/ADR-032-cqrs.md">ADR-032: CQRS</a>
 */
@Service
public class OrderCommandService implements PlaceOrderUseCase { ... }
```

```java
/**
 * Storniert eine Bestellung.
 *
 * <p><strong>Geschäftsregel:</strong> Nur Bestellungen im Status
 * {@link OrderStatus#PENDING} oder {@link OrderStatus#PROCESSING} können
 * storniert werden. Bei ausgelieferten oder bereits stornierten Bestellungen
 * wird eine Exception geworfen.
 *
 * <p><strong>Seiteneffekte:</strong>
 * <ul>
 *   <li>Publiziert ein {@link OrderCancelledEvent} wenn erfolgreich</li>
 *   <li>Gibt gebuchte Inventory-Kapazität frei (async via Event)</li>
 * </ul>
 *
 * @param  orderId  die ID der zu stornierenden Bestellung
 * @throws OrderNotFoundException             wenn keine Bestellung mit dieser ID existiert
 * @throws OrderCannotBeCancelledException    wenn der Status keine Stornierung erlaubt;
 *                                            enthält den aktuellen Status im Message
 * @throws AccessDeniedException              wenn der aktuelle User nicht der Eigentümer ist
 */
@Transactional
public void cancel(OrderId orderId) { ... }
```

---

## Was NICHT dokumentiert werden muss

```java
// Einfache Getter/Setter: kein JavaDoc nötig wenn Name alles sagt
public String name()  { return name; }
public Long   id()    { return id;   }

// Records: JavaDoc auf Klassen-Ebene reicht
/**
 * Unveränderliche Repräsentation einer Bestellanfrage.
 * Alle Felder werden im Konstruktor validiert.
 */
public record CreateOrderCommand(
    UserId          customerId,    // self-explanatory
    List<OrderItem> items,
    Address         shippingAddress
) { ... }

// Private Methoden: JavaDoc optional, nur wenn Algorithmus komplex
private BigDecimal applyRoundingStrategy(BigDecimal amount) {
    // Kommentar hier reicht (kein JavaDoc Tag-Overhead nötig)
    // Verwendet HALF_UP wegen steuerlicher Anforderungen (§ ... UStG)
    return amount.setScale(2, RoundingMode.HALF_UP);
}
```

---

## JavaDoc Formatting-Regeln

```java
/**
 * [ERSTE ZEILE: Kurze Zusammenfassung — ein Satz, endet mit Punkt.]
 *
 * [LEERZEILE]
 *
 * <p>[Erster zusätzlicher Absatz — Details, Hintergrund, Einschränkungen.]
 *
 * <p>[Zweiter zusätzlicher Absatz wenn nötig.]
 *
 * <p>Beispiel:
 * <pre>{@code
 * var result = service.calculate(Money.of("100", "EUR"), 0.19);
 * assertThat(result).isEqualTo(Money.of("19.00", "EUR"));
 * }</pre>
 *
 * @param  paramName  Beschreibung (lowercase, kein Punkt am Ende)
 * @param  other      weitere Parameter — ausrichten wenn mehrere
 * @return            was zurückgegeben wird und wann null möglich ist
 * @throws SpecificException  wann genau diese Exception geworfen wird
 * @throws OtherException     weitere Exception — jede mit eigenem @throws
 * @since  2.1.0              wann diese Methode hinzugefügt wurde (optional)
 * @see    RelatedClass        Querverweis auf verwandte Klassen/Methoden
 */
```

---

## Checkstyle-Konfiguration für JavaDoc-Enforcement

```xml
<!-- checkstyle.xml -->
<module name="JavadocMethod">
    <property name="scope" value="public"/>
    <property name="allowMissingParamTags" value="false"/>
    <property name="allowMissingReturnTag" value="false"/>
    <!-- Ausnahme: Getter/Setter -->
    <property name="allowedAnnotations" value="Override, Test, Bean"/>
</module>

<module name="JavadocType">
    <property name="scope" value="public"/>
    <property name="authorFormat" value="\S"/>
</module>

<module name="JavadocVariable">
    <property name="scope" value="public"/>
</module>

<!-- Leere JavaDoc-Blöcke verbieten -->
<module name="JavadocStyle">
    <property name="checkEmptyJavadoc" value="true"/>
</module>
```

---

## JavaDoc-Antipatterns

```java
// ❌ Lügendes JavaDoc (schlimmer als keines!)
/**
 * Löscht den User.
 */
public void deactivateUser(Long id) {
    user.setActive(false); // Löscht NICHT — deaktiviert nur!
}

// ❌ @param ohne sinnvolle Erklärung
/**
 * @param id the id    ← wiederholt nur den Parameternamen
 * @return the result  ← sagt gar nichts
 */

// ❌ Interne Implementierungsdetails
/**
 * Ruft userRepository.findById() auf, dann emailService.send().
 */
// ↑ Das ist Implementierung, kein Vertrag. Veraltet sofort bei Refactoring.

// ❌ TODO-Kommentare in JavaDoc
/**
 * TODO: Diese Methode ist noch nicht vollständig implementiert.
 * @deprecated
 */
// ↑ TODO gehört ins Issue-Tracker, nicht in JavaDoc
```

---

## Konsequenzen

**Positiv:** Öffentliche APIs sind selbstdokumentierend — kein "Quellcode lesen um zu verstehen was eine Methode tut". Checkstyle-Enforcement verhindert verrottende Dokumentation. `@throws` macht Fehlerbehandlung explizit für Aufrufer.

**Negativ:** Disziplin nötig: JavaDoc muss bei Code-Änderungen mitgepflegt werden — sonst lügt es. Overhead bei trivialen Methoden — deshalb: nur public APIs vollständig dokumentieren.

---

## Tipps

- **`{@code}` statt `<code>`**: `{@code List<String>}` statt `<code>List&lt;String&gt;</code>`.
- **`{@link}`** für Querverweise: `{@link UserNotFoundException}` erzeugt klickbaren Link.
- **`{@inheritDoc}`** in Überschreibungen wenn Vertrag identisch mit Elternklasse.
- **Nie in JavaDoc commiten mit `@author`-Tags**: Git-History ist die Autorenliste.
 