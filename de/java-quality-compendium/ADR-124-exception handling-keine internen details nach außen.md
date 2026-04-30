## Kontext

Wenn eine Exception in einem Spring Boot Service nicht behandelt wird,
gibt Spring standardmäßig einen Stacktrace oder interne Fehlermeldungen
zurück. Das ist ein **Information Disclosure** – Angreifer erfahren
Technologie-Stack, Bibliotheksversionen, interne Pfade und Klassenstrukturen.

Gleichzeitig ist schlechtes Exception Handling die häufigste Ursache
für kryptische Fehlermeldungen die Entwickler stundenlang debuggen müssen.

---

## Das Problem in der Praxis

### So sieht es schlecht aus

```java
// ❌ Exception einfach weiterwerfen – Spring gibt Stacktrace zurück
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found: " + id));
}
```

```json
// Was der Client bekommt – ein Geschenk für Angreifer:
{
  "timestamp": "2025-01-15T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "trace": "java.lang.RuntimeException: User not found: 123\n\tat com.company.UserService.getUser(UserService.java:47)\n\tat com.company.UserController.getUser(UserController.java:23)\n\tat org.springframework.web.servlet...",
  "path": "/api/v1/users/123"
}
```

**Was hier schiefläuft:**

- Stacktrace verrät **Technologie-Stack, Bibliotheken, interne Pfade**
- Interne Klassenstruktur und Zeilennummern sind sichtbar
- `RuntimeException` als Basis für alle Fehler → kein semantischer Unterschied
- Kein einheitliches Fehlerformat → jeder Service antwortet anders
- HTTP 500 für "nicht gefunden" → falscher Statuscode

```java
// ❌ Exception schlucken – noch schlimmer
try {
    userRepository.save(user);
} catch (Exception e) {
    // TODO: handle this later
    System.out.println("Error: " + e.getMessage());
}
// Der Fehler ist weg, der Datensatz nicht gespeichert,
// niemand weiß warum.
```

---

## Die Lösung

### So macht man es richtig

**Schritt 1: Eigene Exception-Hierarchie**

```java
// Basis-Exception für alle Business-Fehler
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;

    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}

// Konkrete Exceptions mit klarer Semantik
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " mit ID " + id + " nicht gefunden",
              "RESOURCE_NOT_FOUND");
    }
}

public class BusinessRuleViolationException extends BusinessException {
    public BusinessRuleViolationException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }
}
```

**Schritt 2: Zentrales Exception Handling (RFC 9457)**

```java
// ✅ Ein Handler für den gesamten Service
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // RFC 9457: Problem Details for HTTP APIs
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex,
                                        HttpServletRequest request) {
        // Vollständiger Log intern – nichts davon geht nach außen
        log.warn("Resource not found: {} | Path: {}",
                 ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Ressource nicht gefunden");
        problem.setProperty("errorCode", ex.getErrorCode());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        log.debug("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "Ungültig")
            ));

        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validierungsfehler");
        problem.setTitle("Ungültige Eingabe");
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    // Catch-all: Interne Fehler NIEMALS nach außen
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex,
                                          HttpServletRequest request) {
        // Vollständiger Stacktrace im Log
        log.error("Unexpected error on {}: ", request.getRequestURI(), ex);

        // Nach außen: nur generische Meldung
        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                               "Ein interner Fehler ist aufgetreten.");
        problem.setTitle("Interner Serverfehler");
        return problem;
        // Kein Stacktrace, kein ex.getMessage(), keine Klassen-Namen!
    }
}
```

**Schritt 3: Spring Boot konfigurieren**

```yaml
# application.yml
server:
  error:
    include-stacktrace: never       # Niemals Stacktrace in Response
    include-message: never          # Spring's Default-Fehlermeldungen unterdrücken
    include-binding-errors: never
    include-exception: false        # Klassenname der Exception verstecken
```

**Was der Client jetzt bekommt (RFC 9457):**

```json
{
  "type": "about:blank",
  "title": "Ressource nicht gefunden",
  "status": 404,
  "detail": "User mit ID 123 nicht gefunden",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

## Tipps

**Pitfall #1 – `server.error.include-message=always` in Produktion:**
Viele Teams aktivieren das für besseres Debugging – und vergeben damit
interne Fehlermeldungen inklusive Datenbankfehler, SQL-Statements und
Tabellenstrukturen an den Client.

**Pitfall #2 – Exception-Mapping in Feign/RestClient vergessen:**
Wenn ein Downstream-Service einen Fehler zurückgibt und der eigene
Service diesen 1:1 weiterleitet, kann interne Infrastruktur-Information
durchsickern. Immer eigene Exceptions mappen:

```java
// ✅ Feign ErrorDecoder
public class PaymentServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException("Payment", "unknown");
            case 422 -> new BusinessRuleViolationException("Zahlung abgelehnt");
            default  -> new ServiceUnavailableException("payment-service");
        };
    }
}
```

**Pitfall #3 – Exceptions loggen UND werfen:**
```java
// ❌ Double-Logging – der Fehler erscheint zweimal im Log
catch (Exception e) {
    log.error("Error occurred", e);  // Hier geloggt
    throw new RuntimeException(e);   // Und nochmal im GlobalExceptionHandler
}

// ✅ Entweder loggen ODER werfen – nicht beides
catch (Exception e) {
    throw new ServiceUnavailableException("payment-service", e);
    // GlobalExceptionHandler loggt einmal zentral
}
```

**War Story:** Ein Team hatte `include-stacktrace: always` in Produktion
aktiviert "weil das Debugging so viel einfacher ist". Ein Penetrationstest
zeigte: Aus den Stacktraces waren exakte Bibliotheksversionen mit bekannten
CVEs ablesbar. Der Fix war eine Yaml-Zeile – aber der Schaden am
Vertrauen war größer.

---
 
### Takeaway

- ✅ Keine Information Disclosure nach außen
- ✅ Einheitliches Fehlerformat über alle Services
- ✅ Klare Semantik durch eigene Exception-Typen
- ✅ Zentrales Logging ohne Double-Logging
- ⚠️ Eigene Exception-Klassen müssen initial erstellt werden
- ⚠️ Team muss wissen welche Exception wann zu werfen ist

---

## Checkliste

- [ ] `server.error.include-stacktrace: never` in allen Umgebungen
- [ ] `@RestControllerAdvice` vorhanden und alle Exception-Typen abgedeckt
- [ ] Kein `catch (Exception e) { log.error(...); throw ...; }` (Double-Logging)
- [ ] Feign/RestClient ErrorDecoder implementiert
- [ ] Keine `RuntimeException` direkt geworfen – eigene Typen verwenden
- [ ] HTTP-Statuscodes semantisch korrekt (404 für "nicht gefunden", nicht 500)