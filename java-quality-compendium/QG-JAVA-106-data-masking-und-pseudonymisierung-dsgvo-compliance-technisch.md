# QG-JAVA-106 — Data Masking und Pseudonymisierung: DSGVO-konforme technische Umsetzung

## Dokumentstatus

| Aspekt | Details/Erklärung | Beispiel | Prüfkriterium |
|---|---|---|---|
| Dokumenttyp | Java Quality Guideline / Security Guideline | Technischer Standard für Java-/Spring-Boot-Systeme | Muss im Review als verbindlicher Qualitätsstandard behandelt werden |
| ID | QG-JAVA-106 | Fortlaufende Kompendiumsnummer | Eindeutige Referenz in Reviews, Tickets und Architekturprüfungen |
| Titel | Data Masking und Pseudonymisierung: DSGVO-konforme technische Umsetzung | Masking, Pseudonymisierung, Verschlüsselung, Anonymisierung | Titel muss den technischen Zweck klar benennen |
| Status | Akzeptiert | Verbindlich für neue Implementierungen mit personenbezogenen Daten | Abweichungen müssen im Pull Request und in der technischen Risikobewertung begründet werden |
| Zielgruppe | Java-Entwickler, Tech Leads, Security Engineers, QA, DevOps, Plattformteams, Architektur, DPO-nahe technische Rollen | Teams mit Zugriff auf personenbezogene Daten | Zielgruppe muss Richtlinie anwenden können |
| Java-Baseline | Java 21+ | Records, Sealed Types, Pattern Matching können genutzt werden | Beispiele orientieren sich an Java 21 |
| Framework-Kontext | Spring Boot 3.x, JPA/Hibernate, Logback/SLF4J, OpenTelemetry, relationale Datenbanken, CI/CD | SaaS-Plattformen und Microservices | Framework-spezifische Regeln müssen technisch geprüft werden |
| Kategorie | Security · Datenschutz · DSGVO · Logging · Observability · Datenhaltung | Datenschutz durch Technikgestaltung | Gehört in Security- und Code-Review-Pfade |
| Verbindlichkeit | Personenbezogene Daten dürfen in Logs, Traces, Metrics, Testdaten und Entwicklerumgebungen nicht unkontrolliert im Klartext verarbeitet werden | E-Mail im Log ist verboten, wenn sie nicht explizit maskiert und begründet ist | Verstöße blockieren Merge oder Release |
| Prüfstatus | Fachlich validiert gegen DSGVO, ENISA, OWASP, BSI/NIST-Kryptoquellen und technische Java-/Spring-Praxis | Siehe Quellenabschnitt | Quellen müssen bei Änderungen erneut geprüft werden |
| Letzte fachliche Prüfung | 2026-05-02 | Aktuelle Prüfung zum Zeitpunkt der Erstellung | Bei regulatorischen oder Architekturänderungen aktualisieren |
| Review-Zyklus | Mindestens jährlich oder bei Änderungen an Logging, Tracing, Datenmodellen, Analytics, Testdaten oder Verschlüsselung | Security Review, Datenschutzprüfung, Architekturboard | Review-Termin im Repository pflegen |

## 1. Zweck

Diese Richtlinie beschreibt, wie personenbezogene Daten in Java-/Spring-Boot-Systemen technisch geschützt werden. Sie legt fest, wann Daten maskiert, pseudonymisiert, verschlüsselt, anonymisiert oder vollständig vermieden werden müssen. Ziel ist, Datenschutzanforderungen nicht nur organisatorisch zu dokumentieren, sondern direkt in Code, Logging, Tracing, Datenhaltung, Testdatenprozessen und Reviews umzusetzen.

Diese Richtlinie ersetzt keine juristische Prüfung durch Datenschutzbeauftragte oder Rechtsberatung. Sie beschreibt jedoch die technische Mindestqualität, die Entwickler in SaaS-Systemen einhalten müssen, wenn personenbezogene Daten verarbeitet werden.

## 2. Kurzregel

Personenbezogene Daten werden nur verarbeitet, wenn sie fachlich notwendig sind. Sie werden in Logs, Traces und Metrics standardmäßig nicht im Klartext ausgegeben. Hochsensible oder direkt identifizierende Daten werden in der Datenbank verschlüsselt, für Suche und Analytics nur kontrolliert pseudonymisiert verwendet und in Dev-/Test-Umgebungen nicht aus Produktionsdaten übernommen. Feature-Code, Tests, Logs, Spans und Datenexporte müssen nachweisen, dass keine unnötige Offenlegung personenbezogener Daten entsteht.

## 3. Geltungsbereich

Diese Richtlinie gilt für alle Java-Anwendungen, Services, Batch-Jobs, Worker, APIs, Admin-Tools, Datenmigrationen, Testdatenprozesse, Observability-Pipelines und CI/CD-Prozesse, die personenbezogene oder personenbeziehbare Daten verarbeiten.

Sie gilt insbesondere für:

- Application Logs
- Audit Logs
- Fehlerlogs
- Exception Messages
- Stack Traces
- OpenTelemetry Spans und Span-Attribute
- Metrics und Labels
- Datenbankfelder
- Suchindizes
- Event-Nachrichten
- Kafka-/Messaging-Payloads
- Testdatenbanken
- lokale Entwicklerumgebungen
- Support- und Admin-Oberflächen
- CSV-/Excel-/JSON-Exporte
- Debug-Endpunkte und Actuator-ähnliche Diagnosefunktionen

Sie gilt nicht für rein technische Daten ohne Personenbezug, solange diese Daten weder direkt noch indirekt einer natürlichen Person zugeordnet werden können. Sobald eine technische Kennung mit einem Nutzer, Kunden, Mitarbeiter, Endgerät, Vertrag, Mandanten oder Account verknüpft werden kann, wird sie mindestens als personenbeziehbar behandelt.

## 4. Grundbegriffe

| Aspekt | Details/Erklärung | Beispiel | Prüfkriterium |
|---|---|---|---|
| Personenbezogene Daten | Informationen, die sich auf eine identifizierte oder identifizierbare natürliche Person beziehen | Name, E-Mail, Telefonnummer, Adresse, Kundennummer mit Personenbezug | Muss klassifiziert werden |
| Direkt identifizierende Daten | Daten, die allein eine Person identifizieren oder sehr leicht identifizieren können | E-Mail, Name, Telefonnummer, IBAN | In Logs grundsätzlich nicht im Klartext erlaubt |
| Indirekt identifizierende Daten | Daten, die in Kombination eine Person identifizieren können | IP-Adresse, User-ID, Geräte-ID, Order-ID | Kontextabhängig schützen |
| Masking | Ersetzen sichtbarer Teile eines Wertes, meist nicht umkehrbar | `max.mustermann@example.com` → `max***@***.com` | Für Logs und UI-Diagnose geeignet |
| Redaction | Vollständiges Entfernen oder Ersetzen eines Wertes | `Authorization` → `<redacted>` | Für Secrets und hochsensible Werte bevorzugt |
| Pseudonymisierung | Ersetzung durch Kennung, bei der Zuordnung mit Zusatzinformationen möglich bleibt | E-Mail → HMAC-basierter Suchwert | Bleibt personenbezogen, benötigt Schutzkonzept |
| Verschlüsselung | Umkehrbare kryptographische Transformation mit Schlüssel | AES-GCM-Ciphertext in DB | Schlüsselmanagement ist Pflicht |
| Anonymisierung | Irreversible Entfernung des Personenbezugs | synthetische Testdaten ohne Rückweg zur Person | Nur dann nicht mehr DSGVO-bezogen, wenn Re-Identifikation praktisch ausgeschlossen ist |
| Tokenisierung | Ersetzung durch Token, Zuordnung liegt in separatem Token-Tresor | IBAN → Token `tok_...` | Nur mit Zugriffskontrolle und Audit sinnvoll |

## 5. Rechtlich-technische Einordnung

Die DSGVO definiert personenbezogene Daten breit: Eine Person kann auch indirekt über Kennungen, Standortdaten, Online-Kennungen oder besondere Merkmale identifizierbar sein. Pseudonymisierung ist in der DSGVO ausdrücklich definiert, entfernt den Personenbezug aber nicht vollständig, wenn eine Zuordnung über zusätzliche Informationen möglich bleibt.

Für Entwickler bedeutet das: Eine interne `userId` ist nicht automatisch anonym. Wenn sie über Datenbank, Admin-Tool, Support-System oder Identity-Service einer Person zugeordnet werden kann, ist sie personenbezogen oder personenbeziehbar. Eine gehashte E-Mail ist ebenfalls nicht automatisch anonym, wenn sie deterministisch erzeugt wurde und durch Wörterbuchangriffe oder Vergleichslisten wiedererkannt werden kann.

Die DSGVO verlangt zudem Grundsätze wie Datenminimierung, Integrität und Vertraulichkeit sowie Datenschutz durch Technikgestaltung und durch datenschutzfreundliche Voreinstellungen. Technische Maßnahmen wie Masking, Pseudonymisierung, Verschlüsselung, Zugriffskontrolle, Löschkonzepte und Logging-Disziplin sind konkrete Umsetzungsbausteine, ersetzen aber nicht die Prüfung von Zweck, Rechtsgrundlage, Aufbewahrung und Betroffenenrechten.

## 6. Datenklassifizierung

### 6.1 Kategorie A — direkt identifizierend oder hochsensibel

| Aspekt | Details/Erklärung | Beispiel | Verbindliche Behandlung |
|---|---|---|---|
| Identitätsdaten | Direkte Identifikation | Name, E-Mail, Telefonnummer, Adresse | Log: maskiert oder redacted; DB: je nach Risiko verschlüsselt; Analytics: pseudonymisiert oder aggregiert |
| Finanzdaten | Hoher Missbrauchsschaden | IBAN, Kreditkartennummer, Zahlungsreferenz mit Personenbezug | Log: nie Klartext; DB: verschlüsselt/tokenisiert; Testdaten: synthetisch |
| Authentifizierungsdaten | Sicherheitskritisch | Passwort, Token, Session-ID, API-Key, Recovery-Code | Nie loggen; nie maskiert als „noch teilweise sichtbar“ ausgeben; vollständig redacted |
| Besondere Datenkategorien | Sehr hohe Schutzanforderung | Gesundheitsdaten, biometrische Daten, besondere Kategorien personenbezogener Daten | Nur nach expliziter Datenschutz-/Security-Freigabe verarbeiten |

### 6.2 Kategorie B — indirekt identifizierend

| Aspekt | Details/Erklärung | Beispiel | Verbindliche Behandlung |
|---|---|---|---|
| Interne Kennungen | Personenbezug über Systeme möglich | User-ID, Customer-ID, Account-ID | In Logs nur verwenden, wenn notwendig; bevorzugt pseudonyme Referenz |
| Technische Kennungen | Können Personen oder Geräte identifizieren | IP-Adresse, Device-ID, Cookie-ID | Logs minimieren; Traces prüfen; Aufbewahrung begrenzen |
| Geschäftliche Referenzen | Können über Fachsysteme zu Personen führen | Order-ID, Ticket-ID, Vertrag-ID | In Audit-Logs möglich, aber nicht mit unnötiger PII kombinieren |

### 6.3 Kategorie C — nicht personenbezogen im konkreten Kontext

| Aspekt | Details/Erklärung | Beispiel | Verbindliche Behandlung |
|---|---|---|---|
| Produktdaten | Kein Personenbezug ohne Kontext | Produktname, SKU, Kategorie | Normal verarbeitbar |
| Systemdaten | Reine technische Zustände | Service-Version, Build-Nummer, Feature-Flag-Name | Normal verarbeitbar |
| Aggregierte Werte | Kein Rückschluss auf Einzelperson | Anzahl Bestellungen pro Stunde | Aggregationsgröße prüfen |

## 7. Verbindlicher Standard

### 7.1 Datenminimierung vor Masking

Die erste Schutzmaßnahme ist nicht Masking, sondern Vermeidung. Wenn ein Wert nicht benötigt wird, darf er nicht erhoben, gespeichert, geloggt, getraced oder exportiert werden.

Schlecht:

```java
log.info("User login succeeded: email={}, name={}, phone={}, tenant={}",
        user.email(), user.name(), user.phoneNumber(), user.tenantId());
```

Besser:

```java
log.info("User login succeeded: tenantId={}, userRef={}",
        user.tenantId(), piiReference.userRef(user.id()));
```

Die zweite Variante enthält genug Diagnosekontext, ohne direkte Identifikatoren offenzulegen.

### 7.2 Logs sind keine Datenablage

Application Logs dienen Diagnose, Betrieb und Security Monitoring. Sie dürfen nicht als Nebenablage für fachliche Daten missbraucht werden. Folgende Daten dürfen in normalen Application Logs nicht im Klartext erscheinen:

- E-Mail-Adressen
- Namen
- Telefonnummern
- IBAN
- Kreditkartendaten
- Passwörter
- Tokens
- API Keys
- Session IDs
- vollständige Adressen
- Request Bodies mit Nutzerinput
- personenbezogene Freitextfelder

### 7.3 Pseudonymisierung bleibt schutzbedürftig

Pseudonymisierte Daten werden weiterhin als schutzbedürftig behandelt. Der Zuordnungsschlüssel, HMAC-Key, Token-Tresor oder Mapping-Service muss getrennt geschützt werden. Zugriff darauf ist zu protokollieren und auf berechtigte technische Rollen zu begrenzen.

### 7.4 Verschlüsselung braucht Schlüsselmanagement

Verschlüsselung ohne sauberes Schlüsselmanagement ist keine belastbare Schutzmaßnahme. Schlüssel dürfen nicht in `application.yml`, Code, Tests, Docker-Images oder CI-Logs stehen. Sie müssen über ein Secret-Management-System bereitgestellt werden. Rotation, Zugriffskontrolle, Auditierung und Notfallverfahren gehören zum Design.

### 7.5 Testdaten sind synthetisch oder nachweisbar anonymisiert

Produktionsdaten dürfen nicht ohne freigegebenen, dokumentierten und technisch geprüften Anonymisierungsprozess in Dev-, Test-, Demo- oder lokale Umgebungen übernommen werden. Für neue Testdaten ist synthetische Erzeugung der Standard.

## 8. Gute Anwendung: Zentrale PII-Typen statt roher Strings

Eine einfache Maßnahme ist, direkt identifizierende Daten nicht als beliebige Strings durch das System wandern zu lassen. Value Objects machen die Sensitivität sichtbar.

```java
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern BASIC_EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmailAddress {
        Objects.requireNonNull(value, "email must not be null");
        value = value.trim().toLowerCase(Locale.ROOT);

        if (!BASIC_EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid email format");
        }
    }

    public String masked() {
        int at = value.indexOf('@');
        if (at <= 1) {
            return "***" + value.substring(at);
        }
        return value.substring(0, Math.min(3, at)) + "***" + value.substring(at);
    }

    @Override
    public String toString() {
        return masked();
    }
}
```

Diese Klasse verhindert nicht alle Datenschutzfehler, aber sie macht sensible Werte im Code sichtbar. Wichtig: `toString()` gibt bewusst keinen Klartext aus.

## 9. Gute Anwendung: Explizite Log-Views statt Domain-Objekte loggen

Domain-Objekte, Entities und Request-DTOs dürfen nicht direkt geloggt werden. Stattdessen werden kleine Log-Views oder strukturierte Felder genutzt.

Schlecht:

```java
log.info("Customer created: {}", customer);
```

Problem: `customer.toString()` kann E-Mail, Adresse, Telefonnummer oder interne Zustände enthalten.

Gut:

```java
public record CustomerLogView(
        String tenantId,
        String customerRef,
        String country,
        String status
) {}
```

```java
var logView = new CustomerLogView(
        customer.tenantId().value(),
        piiReference.customerRef(customer.id()),
        customer.countryCode(),
        customer.status().name()
);

log.info("Customer created: {}", logView);
```

Noch besser ist strukturiertes Logging mit kontrollierten Feldern:

```java
log.info("Customer created tenantId={} customerRef={} country={} status={}",
        customer.tenantId().value(),
        piiReference.customerRef(customer.id()),
        customer.countryCode(),
        customer.status().name());
```

## 10. Masking-Strategien

| Aspekt | Details/Erklärung | Beispiel | Einsatz |
|---|---|---|---|
| Full Redaction | Wert vollständig ersetzen | `***` oder `<redacted>` | Tokens, Passwörter, API Keys, IBAN in Logs |
| Partial Masking | Teile sichtbar lassen | `max***@example.com` | Support-nahe Diagnose, wenn fachlich notwendig |
| Domain-aware Masking | Format bewusst erhalten | `DE89 **** **** **** **00` | IBAN-/Telefonanzeige mit begrenzter Sichtbarkeit |
| HMAC-Pseudonym | Stabiler Such-/Korrelationswert | `hmac_sha256(key, canonicalEmail)` | Suche, Deduplizierung, Analytics |
| Aggregation | Einzelwerte entfernen | Anzahl pro Tenant/Tag | Reporting und Monitoring |

## 11. Schlechte Anwendung: Regex-only Masking als Sicherheitskonzept

Regex-Masking kann eine zusätzliche Sicherheitsleine sein, aber es darf nicht das primäre Datenschutzkonzept sein.

```java
public String maskEverythingWithRegex(String input) {
    return input.replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "***");
}
```

Problem: Regex erkennt nur bekannte Muster. Freitext, ungewöhnliche Formate, serialisierte Objekte, Telefonnummernvarianten oder zusammengesetzte Felder können durchrutschen.

Besser:

1. Keine PII an Logger übergeben.
2. Log-Views mit explizit erlaubten Feldern verwenden.
3. Sensitive Value Objects mit sicherem `toString()` verwenden.
4. Regex-Masking nur als zusätzliche Absicherung im Log-Appender oder Collector nutzen.
5. Tests gegen typische und kritische PII-Muster schreiben.

## 12. PII-Masking-Service

Ein zentraler Masking-Service verhindert uneinheitliche Einzellösungen.

```java
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class PiiMaskingService {

    private final byte[] hmacKey;

    public PiiMaskingService(byte[] hmacKey) {
        this.hmacKey = hmacKey.clone();
    }

    public String redact() {
        return "<redacted>";
    }

    public String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "<empty>";
        }

        var normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
        int at = normalized.indexOf('@');
        if (at <= 1) {
            return "***";
        }

        var local = normalized.substring(0, at);
        var domain = normalized.substring(at + 1);
        var visibleLocal = local.substring(0, Math.min(3, local.length()));
        var visibleDomain = domain.contains(".")
                ? domain.substring(domain.lastIndexOf('.'))
                : "";

        return visibleLocal + "***@***" + visibleDomain;
    }

    public String stablePseudonym(String canonicalValue) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            var digest = mac.doFinal(canonicalValue.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 32);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create PII pseudonym", e);
        }
    }
}
```

Wichtig: Der HMAC-Key kommt aus einem Secret-Management-System, nicht aus Code oder Konfigurationsdateien. `String.hashCode()`, MD5 oder ungesalzene SHA-Hashes sind nicht geeignet.

## 13. Schlechte Anwendung: `hashCode()` als Pseudonymisierung

```java
String pseudonym = Integer.toHexString(email.hashCode());
```

Das ist falsch. `String.hashCode()` ist nicht kryptographisch, nicht kollisionssicher und nicht für Datenschutz- oder Sicherheitszwecke gedacht.

Auch ein einfacher SHA-256-Hash ohne geheimen Key ist für E-Mail-Adressen riskant, weil Angreifer bekannte E-Mail-Listen hashen und vergleichen können.

Besser:

```java
String emailSearchHash = hmacSha256(searchKey, normalizeEmail(email));
```

Dabei gilt:

- Der Key liegt im Secret-Management.
- Eingaben werden kanonisch normalisiert.
- Key Rotation ist geplant.
- Zugriff auf Hash-Felder ist begrenzt.
- Hashes werden nicht als anonyme Daten behandelt.

## 14. Datenbank: Verschlüsselung sensibler Felder

### 14.1 Grundregel

Direkt identifizierende und hochsensible Felder werden nicht unkontrolliert im Klartext gespeichert. Für Felder mit erhöhtem Risiko ist Application-Level Encryption oder Tokenisierung zu prüfen.

Typische Kandidaten:

- IBAN
- Telefonnummer
- E-Mail bei erhöhtem Risiko
- Adresse
- Steuer-ID
- Zahlungsdaten
- Freitext mit personenbezogenem Inhalt

### 14.2 Sauberes Modell mit Ciphertext und Suchhash

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class CustomerEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String tenantId;

    @Column(name = "email_ciphertext", nullable = false, length = 2048)
    private String emailCiphertext;

    @Column(name = "email_search_hash", nullable = false, length = 64)
    private String emailSearchHash;

    @Column(name = "phone_ciphertext", length = 2048)
    private String phoneCiphertext;

    protected CustomerEntity() {
        // JPA
    }

    public CustomerEntity(Long id, String tenantId,
                          String emailCiphertext,
                          String emailSearchHash,
                          String phoneCiphertext) {
        this.id = id;
        this.tenantId = tenantId;
        this.emailCiphertext = emailCiphertext;
        this.emailSearchHash = emailSearchHash;
        this.phoneCiphertext = phoneCiphertext;
    }
}
```

Diese Struktur trennt Klartext, Ciphertext und Suchindex. Der Klartext existiert nur kurz in der Anwendung, nicht dauerhaft in der Datenbank.

### 14.3 Warum nicht blind `AttributeConverter`?

Ein JPA `AttributeConverter` kann bequem wirken, ist aber nicht immer die beste Lösung. Er versteckt Verschlüsselung so stark, dass Entwickler leicht vergessen, welche Felder suchbar, indexierbar oder im Klartext verfügbar sind. Außerdem ist Dependency Injection in Convertern je nach Setup und Provider-Konfiguration sorgfältig zu prüfen.

Für hochsensible Felder ist häufig explizites Mapping klarer:

```java
public final class CustomerPersistenceMapper {

    private final FieldEncryptor encryptor;
    private final PiiMaskingService pii;

    public CustomerPersistenceMapper(FieldEncryptor encryptor, PiiMaskingService pii) {
        this.encryptor = encryptor;
        this.pii = pii;
    }

    public CustomerEntity toEntity(Customer customer) {
        var canonicalEmail = customer.email().value();
        return new CustomerEntity(
                customer.id().value(),
                customer.tenantId().value(),
                encryptor.encrypt(canonicalEmail),
                pii.stablePseudonym(customer.tenantId().value() + ":" + canonicalEmail),
                customer.phoneNumber().map(PhoneNumber::value).map(encryptor::encrypt).orElse(null)
        );
    }
}
```

Der Suchhash enthält hier bewusst den Tenant-Kontext. Dadurch wird verhindert, dass derselbe E-Mail-Wert tenantübergreifend denselben Pseudonymwert erzeugt, sofern tenantübergreifende Erkennung nicht ausdrücklich gewollt und freigegeben ist.

## 15. Kryptographische Mindestregeln

| Aspekt | Details/Erklärung | Beispiel | Prüfkriterium |
|---|---|---|---|
| Authenticated Encryption | Verschlüsselung muss Integrität mitprüfen | AES-GCM oder vergleichbarer AEAD-Modus | Kein selbstgebautes Kryptoformat |
| Zufälliger IV/Nonce | Pro Verschlüsselung neuer Nonce | 96-bit GCM-Nonce | Nie wiederverwenden |
| Key Management | Schlüssel getrennt verwalten | Vault, KMS, HSM, Secret Manager | Kein Key in Code/YAML/Logs |
| Rotation | Schlüsselwechsel muss möglich sein | Key-Version im Ciphertext | Rotation getestet |
| Search Hash | Suche über HMAC, nicht über Klartext | HMAC-SHA-256 | Key geschützt, Normalisierung dokumentiert |
| Keine Eigenkrypto | Keine selbst entworfenen Algorithmen | Keine XOR-/Base64-„Verschlüsselung“ | Security Review blockiert |

Base64 ist keine Verschlüsselung. Encoding schützt keine Daten.

## 16. Logging-Standard

### 16.1 Schlechte Anwendung

```java
log.info("User {} logged in with email {}", user.id(), user.email());
```

Problem: E-Mail landet im Log und wird häufig an zentrale Logsysteme, SIEM, Support-Tools oder externe Observability-Dienste weitergeleitet.

### 16.2 Gute Anwendung

```java
log.info("User login succeeded tenantId={} userRef={}",
        user.tenantId().value(),
        piiReference.userRef(user.id()));
```

### 16.3 Fehlerfall ohne PII

```java
try {
    paymentService.charge(command);
} catch (PaymentProviderException e) {
    log.warn("Payment provider rejected charge tenantId={} orderRef={} reasonCode={}",
            command.tenantId().value(),
            piiReference.orderRef(command.orderId()),
            e.reasonCode());
    throw new PaymentFailedException(command.orderId(), e.reasonCode());
}
```

Nicht erlaubt:

```java
log.warn("Payment failed for customer={} iban={} error={}",
        command.customerEmail(), command.iban(), e.getMessage(), e);
```

## 17. Exception Messages

Exception Messages dürfen keine sensiblen Rohdaten enthalten. Das gilt besonders, weil Exceptions in Logs, Monitoring, APM-Systemen, Sentry-ähnlichen Tools und API-Antworten landen können.

Schlecht:

```java
throw new CustomerNotFoundException("Customer not found for email: " + email);
```

Gut:

```java
throw new CustomerNotFoundException("Customer not found for provided lookup key.");
```

Noch besser mit internem Referenzwert:

```java
throw new CustomerNotFoundException(
        "Customer not found for customerRef=" + piiReference.emailLookupRef(email));
```

## 18. OpenTelemetry Tracing

### 18.1 Grundregel

Span-Namen, Span-Attribute, Events und Baggage dürfen keine direkt identifizierenden Daten, Secrets oder vollständigen Request Bodies enthalten.

Schlecht:

```java
span.setAttribute("user.email", user.email().value());
span.setAttribute("customer.iban", customer.iban().value());
span.setAttribute("http.request.body", requestBody);
```

Gut:

```java
span.setAttribute("tenant.id", tenantId.value());
span.setAttribute("user.ref", piiReference.userRef(user.id()));
span.setAttribute("order.ref", piiReference.orderRef(order.id()));
```

### 18.2 OTel Collector Redaction

Collector-Regeln sind eine zusätzliche Absicherung, ersetzen aber keine saubere Instrumentierung im Code.

```yaml
processors:
  attributes/redact-pii:
    actions:
      - key: http.request.header.authorization
        action: delete
      - key: http.request.header.cookie
        action: delete
      - key: http.request.body.content
        action: delete
      - key: user.email
        action: delete
      - key: customer.iban
        action: delete
      - key: customer.phone
        action: delete
      - key: enduser.id
        action: hash
```

Der konkrete Prozessor und die verfügbaren Actions müssen gegen die eingesetzte OpenTelemetry-Collector-Version geprüft werden. Die Richtlinie bleibt: PII darf nicht absichtlich in Spans geschrieben werden.

## 19. Metrics

Metrics dürfen keine personenbezogenen Daten in Labels verwenden. Labels erzeugen hohe Kardinalität und werden oft langfristig gespeichert.

Schlecht:

```java
registry.counter("login.success", "email", user.email().value()).increment();
```

Gut:

```java
registry.counter("login.success", "tenant", tenantId.value(), "method", "password").increment();
```

Noch besser bei vielen Tenants:

```java
registry.counter("login.success", "tenant_tier", tenant.tier().name()).increment();
```

Tenant-IDs in Metrics sind je nach Plattformkontext zu prüfen. In großen SaaS-Systemen kann auch `tenantId` zu hoher Kardinalität und zu sensiblen Betriebsdaten führen.

## 20. Testdaten und Entwicklerumgebungen

### 20.1 Verbindlicher Standard

Dev-, Test-, Demo- und lokale Umgebungen verwenden synthetische Daten. Produktionsdaten dürfen nur nach dokumentierter Freigabe, nachweisbarer Anonymisierung und technischer Prüfung übernommen werden.

### 20.2 Schlechte Anwendung

```text
prod_dump_2026_04_30.sql → lokal importieren → Tests ausführen
```

Problem: Entwickler erhalten Zugriff auf reale Kundendaten. Kopien entstehen auf Notebooks, Backup-Systemen, CI-Runnern und temporären Datenbanken.

### 20.3 Gute Anwendung

```java
public final class SyntheticCustomerFactory {

    public Customer createCustomer(int index) {
        return new Customer(
                new CustomerId("cust-" + index),
                new EmailAddress("customer" + index + "@example.test"),
                new PhoneNumber("+491510000" + String.format("%04d", index)),
                CountryCode.DE
        );
    }
}
```

Für fachlich realistische Daten werden synthetische Generatoren genutzt. Wichtig ist nicht, dass Daten „echt aussehen“, sondern dass sie keine echte Person repräsentieren.

## 21. Löschung, Anonymisierung und Aufbewahrung

### 21.1 Grundregel

Löschanforderungen werden nicht als einzelnes SQL-Delete verstanden. In SaaS-Systemen müssen Datenbanken, Suchindizes, Caches, Objekt-Speicher, Events, Analytics, Logs, Backups und Downstream-Systeme betrachtet werden.

### 21.2 Technisches Löschverfahren

```java
@Service
public class DataDeletionService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final SearchIndexGateway searchIndexGateway;
    private final TokenService tokenService;
    private final AuditLog auditLog;
    private final DomainEventPublisher events;

    @Transactional
    public void processDeletionRequest(CustomerId customerId, DeletionRequestId requestId) {
        var customerRef = customerId.value();

        customerRepository.anonymizeDirectIdentifiers(customerId);
        orderRepository.removeCustomerReferenceWhereAllowed(customerId);
        tokenService.invalidateAllForCustomer(customerId);

        auditLog.recordDeletionProcessed(requestId, customerRef, java.time.Instant.now());
        events.publish(new CustomerDeletionProcessedEvent(customerId, requestId));
    }

    public void processAfterCommit(CustomerId customerId) {
        searchIndexGateway.deleteCustomer(customerId);
    }
}
```

Wichtig: Löschung kann durch gesetzliche Aufbewahrungspflichten begrenzt sein. Dann ist häufig Anonymisierung, Sperrung, Zweckbegrenzung oder Entfernung direkter Identifikatoren erforderlich. Diese Entscheidung wird nicht allein im Code getroffen.

### 21.3 Fristen

Betroffenenanfragen sind unverzüglich zu bearbeiten. Die DSGVO sieht für Mitteilungen zu Anträgen nach Art. 15 bis 22 grundsätzlich eine Frist von einem Monat vor; in komplexen Fällen ist eine Verlängerung möglich, muss aber begründet und rechtzeitig mitgeteilt werden. Technische Systeme müssen deshalb Lösch-, Auskunfts- und Korrekturprozesse nachvollziehbar unterstützen.

## 22. SaaS-spezifische Regeln

| Aspekt | Details/Erklärung | Beispiel | Prüfkriterium |
|---|---|---|---|
| Tenant-Kontext | Pseudonyme Werte sollten tenantbezogen erzeugt werden, wenn tenantübergreifende Korrelation nicht erforderlich ist | HMAC über `tenantId + email` | Keine unnötige Cross-Tenant-Korrelation |
| Support-Zugriff | Support darf nur minimierte oder maskierte Daten sehen | E-Mail teilweise maskiert | Rollenprüfung und Audit |
| Admin-Tools | Admin-Ansichten dürfen keine Rohdaten ohne Zweck anzeigen | IBAN nur redacted | Berechtigungen und Logging prüfen |
| Export | Exporte müssen Datenklassifizierung beachten | CSV ohne Telefonnummer, wenn nicht nötig | Export-Review erforderlich |
| Observability | Logs/Traces/Metrics gehen oft an zentrale Plattformen | Grafana, Loki, Jaeger, Sentry | PII-Schutz vor Weiterleitung |
| Multi-Tenant Analytics | Analytics darf keine Einzelpersonen rekonstruierbar machen | Aggregation pro Zeitraum | Mindestgruppengröße prüfen |

## 23. Anti-Patterns

### 23.1 „Wir maskieren später im Logsystem“

Das ist riskant. Wenn die Anwendung PII bereits ausgibt, können Zwischenstationen, Sidecars, lokale Logs, Crash Dumps oder Debug-Ausgaben den Klartext enthalten. Primär wird im Code minimiert; Collector-/Logsystem-Masking ist Zusatzschutz.

### 23.2 Produktionsdaten in Entwicklerdatenbanken

Nicht erlaubt, solange kein dokumentierter, geprüfter und freigegebener Anonymisierungsprozess existiert. Auch „nur kurz lokal“ ist eine Datenkopie.

### 23.3 E-Mail als technische ID

E-Mail-Adressen ändern sich, identifizieren direkt und gehören nicht als technische Primärschlüssel in URLs, Logs, Events oder Datenbankbeziehungen.

Schlecht:

```java
GET /users/max.mustermann@example.com/orders
```

Gut:

```java
GET /users/usr_8f3a92/orders
```

### 23.4 Secrets maskieren statt redakten

Tokens, API Keys, Passwörter und Session IDs werden nicht teilweise angezeigt. Sie werden vollständig entfernt.

```java
Authorization: <redacted>
```

Nicht:

```java
Authorization: Bearer eyJ***
```

### 23.5 Klartext in Events

Events leben oft lange, werden mehrfach konsumiert und in Dead-Letter-Queues gespeichert. Personenbezogene Daten in Events brauchen dieselbe Disziplin wie Datenbankfelder.

Schlecht:

```java
public record CustomerRegisteredEvent(
        String customerId,
        String email,
        String phoneNumber
) {}
```

Besser:

```java
public record CustomerRegisteredEvent(
        String customerId,
        String tenantId,
        Instant occurredAt
) {}
```

Wenn Downstream-Systeme E-Mail benötigen, muss die Notwendigkeit explizit begründet, vertraglich und technisch abgesichert und die Event-Aufbewahrung geprüft werden.

## 24. Tests

### 24.1 Masking-Tests

```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskingServiceTest {

    @Test
    void maskEmail_doesNotExposeFullEmail() {
        var service = new PiiMaskingService("0123456789abcdef0123456789abcdef".getBytes());

        var masked = service.maskEmail("max.mustermann@example.com");

        assertThat(masked).doesNotContain("max.mustermann@example.com");
        assertThat(masked).contains("***");
    }

    @Test
    void stablePseudonym_returnsSameValue_forSameCanonicalInput() {
        var service = new PiiMaskingService("0123456789abcdef0123456789abcdef".getBytes());

        var first = service.stablePseudonym("tenant-a:max@example.com");
        var second = service.stablePseudonym("tenant-a:max@example.com");

        assertThat(first).isEqualTo(second);
    }

    @Test
    void stablePseudonym_returnsDifferentValue_forDifferentTenantContext() {
        var service = new PiiMaskingService("0123456789abcdef0123456789abcdef".getBytes());

        var tenantA = service.stablePseudonym("tenant-a:max@example.com");
        var tenantB = service.stablePseudonym("tenant-b:max@example.com");

        assertThat(tenantA).isNotEqualTo(tenantB);
    }
}
```

### 24.2 Log-Safety-Tests

Bei kritischen Use Cases muss getestet werden, dass typische PII nicht im Log erscheint. Das kann mit einem Test-Appender, Log-Capture oder Architekturtest erfolgen.

```java
@Test
void login_doesNotLogRawEmailAddress() {
    var email = new EmailAddress("max.mustermann@example.com");

    loginService.login(new LoginCommand(email, "valid-password"));

    assertThat(logCapture.loggedMessages())
            .noneMatch(message -> message.contains("max.mustermann@example.com"));
}
```

## 25. Automatisierbare Prüfungen

| Aspekt | Details/Erklärung | Beispiel | Automatisierung |
|---|---|---|---|
| Keine PII in Logs | Erkennung typischer Muster | E-Mail, IBAN, Token | Semgrep, Regex-Scanner, Log-Tests |
| Keine Secrets in Konfiguration | Keys, Tokens, Passwörter | `apiKey=...` | Secret Scanning |
| Keine Request Bodies in Traces | OTel Attribute prüfen | `http.request.body` | Collector-Konfigurationsprüfung |
| Keine PII-Metrics-Labels | Label-Namen prüfen | `email`, `userName` | Code-Scan |
| Testdatenkontrolle | Kein Prod-Dump im Repo/CI | `.sql`, `.dump`, `.backup` | CI-Scanner |
| Verschlüsselungsfelder | Sensitive Fields markiert | `emailCiphertext` statt `email` | ArchUnit/Schema Review |

Beispiel für eine ArchUnit-Regel als Richtung:

```java
@ArchTest
static final ArchRule services_should_not_log_domain_entities_directly =
        noClasses()
                .that().resideInAPackage("..service..")
                .should().callMethod(org.slf4j.Logger.class, "info", String.class, Object.class)
                .because("Domain-Objekte dürfen nicht direkt geloggt werden; Log-Views oder strukturierte Felder verwenden.");
```

Diese Regel ist nur beispielhaft. In echten Projekten muss die Logger-Nutzung differenzierter geprüft werden.

## 26. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Datenklassifizierung | Sind alle neuen Felder klassifiziert? | E-Mail Kategorie A | Pflicht |
| Datenminimierung | Wird jedes personenbezogene Feld wirklich benötigt? | Telefonnummer im Export | Begründen oder entfernen |
| Logging | Wird keine PII im Klartext geloggt? | `log.info(email)` | Nicht erlaubt |
| Exceptions | Enthalten Fehlermeldungen keine Rohdaten? | `Customer not found for email` | Nicht erlaubt |
| Tracing | Enthalten Spans keine PII/Secrets? | `user.email` | Nicht erlaubt |
| Metrics | Werden keine PII-Labels verwendet? | `email` Label | Nicht erlaubt |
| Datenbank | Sind sensible Felder verschlüsselt oder begründet im Klartext? | `iban_ciphertext` | Pflichtprüfung |
| Suche | Wird HMAC statt Klartextsuche genutzt, wenn Feld verschlüsselt ist? | `email_search_hash` | Pflichtprüfung |
| Schlüssel | Kommen Keys aus Secret Management? | Vault/KMS | Pflicht |
| Testdaten | Sind Daten synthetisch oder freigegeben anonymisiert? | lokaler DB-Dump | Prod-Daten nicht erlaubt |
| SaaS | Gibt es tenantbezogene Pseudonymisierung? | HMAC mit Tenant-Kontext | Prüfen |
| Löschung | Sind Lösch-/Anonymisierungspfade berücksichtigt? | Suchindex, Cache, Events | Pflicht bei PII |

## 27. Migration bestehender Systeme

Bestehende Systeme werden schrittweise gehärtet.

### Schritt 1: Dateninventar

Alle personenbezogenen Felder in Datenbank, Events, APIs, Logs, Traces und Exporten werden inventarisiert.

### Schritt 2: Klassifizierung

Jedes Feld wird Kategorie A, B oder C zugeordnet. Unklare Felder werden konservativ höher eingestuft.

### Schritt 3: Log- und Trace-Bereinigung

Direkte PII-Ausgaben werden entfernt, Log-Views eingeführt und Collector-Regeln ergänzt.

### Schritt 4: Datenbank-Härtung

Hochsensible Felder werden verschlüsselt oder tokenisiert. Suchanforderungen werden über HMAC-basierte Suchindizes umgesetzt.

### Schritt 5: Testdatenprozess

Produktionsdatenkopien werden entfernt. Synthetische Testdaten oder geprüfte Anonymisierung werden eingeführt.

### Schritt 6: Automatisierung

Secret Scanning, Log-PII-Scanning, ArchUnit-/Semgrep-Regeln und CI-Prüfungen werden ergänzt.

### Schritt 7: Definition of Done

Neue Features mit PII dürfen nur gemerged werden, wenn Datenklassifizierung, Logging, Tracing, Persistenz, Tests, Löschung und Security Review abgeschlossen sind.

## 28. Ausnahmen

Ausnahmen sind nur zulässig, wenn alle folgenden Bedingungen erfüllt sind:

1. Der fachliche Zweck ist dokumentiert.
2. Die Verarbeitung ist mit Datenschutz-/Security-Verantwortlichen abgestimmt.
3. Zugriff und Aufbewahrung sind begrenzt.
4. Logging, Tracing und Exporte sind geprüft.
5. Die Ausnahme ist im Pull Request sichtbar.
6. Es gibt ein Ablauf- oder Review-Datum.

Beispiel: Eine Support-Funktion darf für berechtigte Rollen teilweise maskierte E-Mail-Adressen anzeigen, wenn dies zur Identifikation im Support-Prozess notwendig ist, der Zugriff protokolliert wird und keine Rohdaten in Logs gelangen.

## 29. Definition of Done

Ein Feature, das personenbezogene oder personenbeziehbare Daten verarbeitet, erfüllt diese Richtlinie, wenn alle folgenden Punkte erfüllt sind:

- Alle personenbezogenen Felder sind klassifiziert.
- Nicht notwendige Daten wurden entfernt.
- Logs enthalten keine direkt identifizierenden Daten im Klartext.
- Exceptions enthalten keine sensiblen Rohdaten.
- OpenTelemetry-Spans enthalten keine PII, Secrets oder Request Bodies.
- Metrics verwenden keine PII als Labels.
- Hochsensible Datenbankfelder sind verschlüsselt oder die Klartextspeicherung ist dokumentiert und freigegeben.
- Suchanforderungen auf verschlüsselten Feldern nutzen kontrollierte HMAC-/Token-Mechanismen.
- Schlüssel liegen im Secret-Management und nicht in Code, YAML, Docker-Images oder CI-Logs.
- Dev-/Test-Daten sind synthetisch oder nachweisbar anonymisiert.
- Lösch-, Sperr- oder Anonymisierungspfade sind berücksichtigt.
- Tests prüfen mindestens typische Masking-, Logging- und Pseudonymisierungsfälle.
- Security- und Datenschutz-Review sind bei Kategorie-A-Daten abgeschlossen.

## 30. Quellen und Referenzen

- DSGVO / GDPR, Verordnung (EU) 2016/679, insbesondere Art. 4, Art. 5, Art. 12, Art. 17, Art. 25 und Art. 32: https://eur-lex.europa.eu/eli/reg/2016/679/oj?locale=de
- ENISA, „Pseudonymisation techniques and best practices“, 2019: https://www.enisa.europa.eu/publications/pseudonymisation-techniques-and-best-practices
- ENISA, „Data Pseudonymisation: Advanced Techniques and Use Cases“: https://www.enisa.europa.eu/publications/data-pseudonymisation-advanced-techniques-and-use-cases
- OWASP Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- OWASP Logging Vocabulary Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Vocabulary_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- BSI TR-02102, Kryptographische Verfahren und Empfehlungen: https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr02102/tr02102_node.html
- NIST SP 800-175B Rev. 1, Guideline for Using Cryptographic Standards: https://csrc.nist.gov/pubs/sp/800/175/b/r1/final
- EDPB/Hamburg Commissioner, H&M fine 35.3 million EUR, 2020: https://www.edpb.europa.eu/news/national-news/2020/hamburg-commissioner-fines-hm-353-million-euro-data-protection-violations_en

## 31. Kurzzusammenfassung für Entwickler

Wenn du personenbezogene Daten verarbeitest, stelle zuerst die Frage, ob du den Wert überhaupt brauchst. Wenn ja, klassifiziere ihn. Logge keine Rohdaten. Schreibe keine PII in Traces oder Metrics. Nutze für Diagnose pseudonyme Referenzen. Verschlüssele oder tokenisiere hochsensible Daten in der Datenbank. Verwende HMAC für Suchindizes, nicht `hashCode()` oder ungeschützte Hashes. Nutze synthetische Testdaten. Behandle Pseudonymisierung nicht als Anonymisierung. Baue Löschung und Anonymisierung als technische Prozesse ein, nicht als spätere Handarbeit.
