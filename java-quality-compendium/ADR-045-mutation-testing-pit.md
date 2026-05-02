# ADR-045 — Mutation Testing mit PIT

| Feld       | Wert                                        |
|------------|---------------------------------------------|
| Java       | 21 · JUnit 5 · PIT 1.15+                   |
| Datum      | 2024-01-01                                  |
| Kategorie  | Testing / Qualitätssicherung                |

---

## Kontext & Problem

Code-Coverage misst ob eine Zeile ausgeführt wurde — nicht ob sie korrekt getestet wird. Eine Zeile ist "covered" wenn ein Test sie durchläuft, auch wenn der Test kein `assertThat()` hat. **Mutation Testing** geht weiter: PIT ändert den Quellcode automatisch (Mutation) und prüft ob mindestens ein Test fehlschlägt. Überlebende Mutanten sind ungetestetes Verhalten.

---

## Was ist eine Mutation?

```java
// Originaler Code
public boolean isAdult(int age) {
    return age >= 18;
}

// PIT erzeugt automatisch diese Mutanten:
return age > 18;     // Boundary Mutation — überlebt wenn kein Test mit age=18 existiert!
return age <= 18;    // Negation
return true;         // Return Value Mutation
return false;        // Return Value Mutation
return age >= 19;    // Increment Constant
```

```java
// Schlechter Test: Coverage 100%, aber Mutant überlebt
@Test
void isAdult_returnsTrue_whenOver18() {
    assertThat(validator.isAdult(25)).isTrue(); // Nur 25 getestet
    // age=18 nie getestet → "age > 18" Mutant überlebt!
}

// Guter Test: tötet Boundary-Mutant
@Test
void isAdult_returnsTrue_whenExactly18() {
    assertThat(validator.isAdult(18)).isTrue();  // Grenzwert getestet
}

@Test
void isAdult_returnsFalse_whenBelow18() {
    assertThat(validator.isAdult(17)).isFalse(); // Untergrenze
}
```

---

## PIT Konfiguration

```kotlin
// build.gradle.kts
plugins {
    id("info.solidsoft.pitest") version "1.15.0"
}

pitest {
    junit5PluginVersion.set("1.2.1")

    // Welche Klassen mutieren (Domain und Service — nicht Konfiguration)
    targetClasses.set(listOf(
        "com.example.domain.*",
        "com.example.service.*"
    ))

    // Welche Tests ausführen
    targetTests.set(listOf("com.example.**.*Test"))

    // Mutationsoperatoren
    mutators.set(listOf(
        "STRONGER"   // Starkes Set: CONDITIONALS_BOUNDARY, NEGATE_CONDITIONALS,
                     // MATH, INCREMENTS, INVERT_NEGS, RETURN_VALS, VOID_METHOD_CALLS
    ))

    // Schwellwerte — Pipeline schlägt fehl wenn unterschritten
    mutationThreshold.set(80)    // 80% der Mutanten müssen getötet werden
    coverageThreshold.set(85)    // 85% Line Coverage

    // Ausgabeformate
    outputFormats.set(listOf("HTML", "XML"))

    // Performance: parallele Ausführung
    threads.set(4)

    // Exkludieren: generierter Code, Konfiguration, DTOs
    excludedClasses.set(listOf(
        "com.example.config.*",
        "com.example.**.dto.*",
        "com.example.**.*Config"
    ))

    // Timeout: verhindert Endlosschleifen durch Mutanten
    timeoutConstant.set(4000)
}

// Ausführen:
// ./gradlew pitest
// Bericht: build/reports/pitest/index.html
```

---

## Was bedeuten die Mutation-Ergebnisse?

```
KILLED    ✅ Mutant wurde von einem Test entdeckt → gut
SURVIVED  ❌ Kein Test hat den Mutant getötet → Lücke im Test
NO_COVERAGE ❌ Kein Test hat den Mutanten überhaupt ausgeführt
TIMED_OUT ⚠️ Mutant erzeugte Endlosschleife → evtl. kein Loop-Test
```

---

## Typische überlebende Mutanten und ihre Bedeutung

```java
// ① Boundary-Mutant überlebt: kein Grenzwert-Test
if (quantity > 0)  // Mutant: quantity >= 0 überlebt
// Lösung: @ParameterizedTest mit 0, 1, -1 (→ ADR-014)

// ② Null-Return überlebt: kein Test für Null-Pfad
public User findOrNull(Long id) {
    return repository.findById(id).orElse(null); // Mutant: return null → überlebt
// Lösung: Test für nicht-existierende ID

// ③ Void-Methoden-Mutant überlebt: Seiteneffekt nicht getestet
public void sendEmail(User user) {
    emailService.send(user.email()); // Mutant: Methode entfernt → überlebt
// Lösung: verify(emailService).send(any()) im Test

// ④ Math-Mutant überlebt: Berechnungsformel nicht getestet
public Money calculateTax(Money price) {
    return price.multiply(0.19); // Mutant: multiply(0.18) überlebt
// Lösung: Assertion auf konkreten Betrag, nicht nur isNotNull()
```

---

## Mutation Score als Qualitätsmetrik

```
Mutation Score = (getötete Mutanten / gesamte Mutanten) × 100

< 60%: Tests sind Fassade — kaum Assertions
60–70%: Grundlegende Tests vorhanden, viele Lücken
70–80%: Solide Basis, wichtige Pfade getestet
80–90%: Gute Testqualität — für die meisten Projekte ausreichend
> 90%: Exzellent — für sicherheitskritische Systeme

Empfehlung: 80% als CI-Gate
```

---

## PIT in der CI-Pipeline

```yaml
# .github/workflows/pipeline.yml
- name: Mutation Tests
  run: ./gradlew pitest
  # Schlägt fehl wenn Mutation Score < 80% (konfiguriert in build.gradle.kts)

- name: Upload PIT Report
  uses: actions/upload-artifact@v4
  if: always()
  with:
    name: pitest-report
    path: build/reports/pitest/
```

---

## Konsequenzen

**Positiv:** Findet Tests die zwar Coverage haben aber nichts prüfen. Zeigt konkret welche Grenzwerte fehlen. Mutation Score ist eine ehrlichere Metrik als Line-Coverage.

**Negativ:** PIT ist langsam — für große Codebasen 10–30 Minuten. Nur in dedizierten Nightly-Runs oder selektiv ausführen. Äquivalente Mutanten (Mutant ist semantisch identisch zum Original) können nicht automatisch erkannt werden.

---

## Tipps

- **Nicht auf 100% optimieren** — ab ~85% werden die verbleibenden Mutanten oft äquivalent.
- **Selektiv ausführen**: `./gradlew pitest --targetClasses "com.example.domain.Order*"` für spezifische Klassen.
- **PIT + Parametrisierte Tests** (→ ADR-014): `@ParameterizedTest` mit Grenzwerten tötet Boundary-Mutanten systematisch.
- **Coverage ≠ Mutation Score**: Ein Projekt mit 95% Coverage und 50% Mutation Score hat schlechte Tests.
