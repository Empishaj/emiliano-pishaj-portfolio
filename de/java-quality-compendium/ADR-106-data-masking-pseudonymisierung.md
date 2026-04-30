# ADR-106 — Data Masking & Pseudonymisierung: DSGVO-Compliance technisch

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / DPO (Datenschutzbeauftragter) / Architektur-Board       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Security · DSGVO · Datenschutz                                |
| Betroffene Teams  | Alle Teams die personenbezogene Daten verarbeiten             |
| Abhängigkeiten    | ADR-035 (GDPR), ADR-017 (Logging), ADR-102 (OTel)             |

---

## 1. Kontext & Treiber

### 1.1 Das konkrete DSGVO-Risiko

```
DSGVO ARTIKEL 5 (Grundsätze):
  "Datenminimierung": nur nötige Daten erheben
  "Integrität und Vertraulichkeit": technische Schutzmaßnahmen

TYPISCHE VERSTÖSSE (mit Bußgeldern):
  
  Verstoß 1: Kundenname im Application-Log
    → H&M: 35 Mio EUR Bußgeld (2020) für übermäßige Datenerfassung
    → Symptom: log.info("User {} logged in", user.getEmail())
    
  Verstoß 2: E-Mail in Fehler-Tracebacks (Grafana, Jaeger, Sentry)
    → Traces enthalten PII → wer hat Zugriff auf Grafana?
    
  Verstoß 3: Klartext-PII in Testdatenbanken
    → Entwickler haben Produktions-Kundendaten in Dev-Umgebung

  Verstoß 4: IBAN/Kreditkarte in DB ohne Verschlüsselung
    → Bei Datenleck: direkter Schaden für Kunden

TECHNISCHE LÖSUNGEN:
  → Masking:          PII in Logs/Traces ersetzen (nicht umkehrbar)
  → Pseudonymisierung: PII durch ID ersetzen (umkehrbar für Berechtigte)
  → Verschlüsselung:  PII verschlüsselt in DB (nur mit Key lesbar)
  → Anonymisierung:   PII komplett entfernen (nicht umkehrbar, kein DSGVO-Schutz mehr nötig)
```

---

## 2. Entscheidung

PII (Personally Identifiable Information) wird nach drei Strategien behandelt:
1. **Logs/Traces**: immer gemasktes Format (kein Rückschluss möglich)
2. **Datenbank**: verschlüsselt für hochsensible Felder, pseudonymisiert für analytische Zwecke
3. **Testdaten**: immer anonymisiert/synthetisch — niemals Produktionsdaten in Dev/Test

---

## 3. PII-Klassifizierung

```
KATEGORIE A — Direkt identifizierend (strenge Behandlung):
  E-Mail-Adresse, Name, Telefonnummer, Adresse
  IBAN, Kreditkartennummer, Steuer-ID
  → DB: verschlüsselt | Log: IMMER maskiert | Analytics: pseudonymisiert

KATEGORIE B — Indirekt identifizierend:
  IP-Adresse, User-ID, Order-ID (wenn zu Person zurückverfolgbar)
  → DB: Klartext OK | Log: abgekürzt | Analytics: OK wenn aggregiert

KATEGORIE C — Nicht-personenbezogen:
  Produktname, Preis, Zeitstempel ohne Bezug zu Person
  → Keine besonderen Maßnahmen
```

---

## 4. Masking in Logs

### 4.1 Automatisches Log-Masking

```java
// Eigener Logback-Converter: maskiert PII bevor es in den Log geht
public class PiiMaskingConverter extends ClassicConverter {

    // Regex-Pattern für bekannte PII-Formate
    private static final List<Pattern> PII_PATTERNS = List.of(
        // E-Mail-Adressen
        Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE),
        // Deutsche IBAN
        Pattern.compile("DE\\d{2}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{2}"),
        // Kreditkartennummern (grob)
        Pattern.compile("\\b\\d{4}[\\s\\-]?\\d{4}[\\s\\-]?\\d{4}[\\s\\-]?\\d{4}\\b"),
        // Telefonnummern
        Pattern.compile("\\+?\\d[\\s\\-]?(\\d[\\s\\-]?){8,14}\\d")
    );

    @Override
    public String convert(ILoggingEvent event) {
        var message = event.getFormattedMessage();
        for (var pattern : PII_PATTERNS) {
            message = pattern.matcher(message).replaceAll(match -> {
                var original = match.group();
                // Ersten 3 Zeichen sichtbar, Rest maskiert
                if (original.length() <= 3) return "***";
                return original.substring(0, 3) + "***";
            });
        }
        return message;
    }
}
```

```xml
<!-- logback-spring.xml: PII-Masking-Converter registrieren -->
<configuration>
  <conversionRule conversionWord="piiMasked"
                  converterClass="com.example.logging.PiiMaskingConverter"/>

  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <messageConverter class="com.example.logging.PiiMaskingConverter"/>
    </encoder>
  </appender>
</configuration>
```

### 4.2 Strukturiertes Masking für bekannte Felder

```java
// Besser als Regex: bekannte Felder gezielt maskieren

// Eigene Masking-Annotation
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PiiField {
    MaskingStrategy strategy() default MaskingStrategy.PARTIAL;

    enum MaskingStrategy {
        FULL,    // vollständig: "***"
        PARTIAL, // teilweise: "max***@***.com"
        HASH,    // Hash: "a1b2c3d4" (gleicher Input = gleicher Hash für Analytics)
        NONE     // nicht maskieren (für nicht-sensible Felder)
    }
}

// Auf Domain-Objekten:
public record Customer(
    @PiiField(strategy = HASH)    String userId,    // Analytics möglich
    @PiiField(strategy = PARTIAL) String email,     // max***@***.com
    @PiiField(strategy = FULL)    String phone,     // ***
    @PiiField(strategy = FULL)    String iban,      // ***
                                  String country    // Kein PII
) {}

// Jackson-Serializer: beachtet @PiiField beim Loggen
public class PiiAwareSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen,
                           SerializerProvider provider) throws IOException {
        if (value == null) { gen.writeNull(); return; }

        // Felder mit @PiiField maskieren
        var fields = value.getClass().getDeclaredFields();
        gen.writeStartObject();
        for (var field : fields) {
            field.setAccessible(true);
            var piiAnnotation = field.getAnnotation(PiiField.class);
            var fieldValue = field.get(value);

            gen.writeFieldName(field.getName());
            if (piiAnnotation != null && fieldValue != null) {
                gen.writeString(mask(fieldValue.toString(), piiAnnotation.strategy()));
            } else {
                gen.writeObject(fieldValue);
            }
        }
        gen.writeEndObject();
    }

    private String mask(String value, PiiField.MaskingStrategy strategy) {
        return switch (strategy) {
            case FULL    -> "***";
            case PARTIAL -> value.length() > 3
                ? value.substring(0, 3) + "***" : "***";
            case HASH    -> Integer.toHexString(value.hashCode());
            case NONE    -> value;
        };
    }
}
```

---

## 5. Datenbank: Verschlüsselung sensitiver Felder

### 5.1 Application-Level Encryption

```java
// Verschlüsselung im Application Layer (nicht DB-Level)
// Vorteil: DB-Admin sieht keine Klartextdaten

@Configuration
public class EncryptionConfig {

    @Bean
    public Encryptor fieldEncryptor(@Value("${encryption.key}") String key) {
        // AES-256-GCM: authentifizierte Verschlüsselung
        // Key kommt aus Vault (→ ADR-069), NICHT aus Config-Datei!
        return new AesGcmEncryptor(key);
    }
}

// JPA AttributeConverter: transparente Ver-/Entschlüsselung
@Converter
public class EncryptedStringConverter
        implements AttributeConverter<String, String> {

    @Autowired
    private Encryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        return encryptor.encrypt(plaintext);   // → DB: verschlüsselter Ciphertext
    }

    @Override
    public String convertToEntityAttribute(String ciphertext) {
        if (ciphertext == null) return null;
        return encryptor.decrypt(ciphertext);  // → Anwendung: Klartext
    }
}

// Auf Entity-Felder anwenden:
@Entity
public class CustomerEntity {
    @Id
    private Long id;

    // Klartext in DB:
    private String userId;       // Kein PII (interne ID)

    // Verschlüsselt in DB:
    @Convert(converter = EncryptedStringConverter.class)
    private String email;        // AES-256 Ciphertext in DB

    @Convert(converter = EncryptedStringConverter.class)
    private String iban;         // AES-256 Ciphertext in DB

    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;  // AES-256 Ciphertext in DB
}
```

### 5.2 Suchbarkeit bei verschlüsselten Feldern

```java
// Problem: Wenn E-Mail verschlüsselt ist, kann man nicht nach ihr suchen
// (SELECT * FROM customers WHERE email = 'max@example.com' → findet nichts)

// Lösung: Deterministischer Hash als Suchindex
@Entity
public class CustomerEntity {

    @Convert(converter = EncryptedStringConverter.class)
    private String email;              // Verschlüsselt (DSGVO-sicher)

    @Column(name = "email_hash")
    private String emailHash;          // SHA-256 Hash für Suche

    // Beim Speichern: Hash automatisch setzen
    @PrePersist
    @PreUpdate
    void updateEmailHash() {
        if (email != null) {
            // HMAC-SHA256 mit festem Key: deterministisch, aber ohne Key nicht umkehrbar
            this.emailHash = hmacSha256(email, SEARCH_KEY);
        }
    }
}

// Repository: nach Hash suchen
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByEmailHash(String emailHash);

    default Optional<CustomerEntity> findByEmail(String email) {
        var hash = hmacSha256(email, SEARCH_KEY);
        return findByEmailHash(hash);
    }
}
```

---

## 6. Testdaten: Anonymisierung für Dev/Test

```java
// DSGVO verbietet Produktionsdaten in Dev-Umgebung!
// Lösung: automatische Anonymisierung bei Datenbankexport

@Service
public class TestDataAnonymizer {

    // Erstellt anonymisierten DB-Dump für Dev/Test
    public void anonymizeDatabase(DataSource source, DataSource target) {
        var faker = new Faker(Locale.GERMAN);

        // Alle Kunden anonymisieren
        jdbcTemplate.query("SELECT * FROM customers", rs -> {
            var anonymized = new CustomerEntity();
            anonymized.setId(rs.getLong("id"));          // ID bleibt (für Referenzen)
            anonymized.setEmail(faker.internet().emailAddress());  // Fake E-Mail
            anonymized.setFirstName(faker.name().firstName());     // Fake Name
            anonymized.setLastName(faker.name().lastName());
            anonymized.setPhone(faker.phoneNumber().phoneNumber());
            anonymized.setIban(generateFakeIban());
            // Bestell-History bleibt (anonyme Referenz via ID)
            targetJdbc.update("INSERT INTO customers ...", anonymized);
        });
    }

    // CI-Pipeline: Testdaten täglich refreshen
    // (nicht manuell aus Produktion kopieren!)
}
```

---

## 7. OTel Tracing: PII aus Spans entfernen

```yaml
# OTel Collector: PII-Attribute entfernen bevor Traces gespeichert werden
# (→ ADR-102 OTel Collector-Konfiguration)
processors:
  attributes/redact-pii:
    actions:
      # HTTP-Header die PII enthalten können
      - key: http.request.header.authorization
        action: delete
      - key: http.request.header.cookie
        action: delete

      # User-Input der PII enthalten könnte
      - key: http.request.body.content
        action: delete

      # Custom Attribute: wenn PII-Felder explizit gesetzt wurden
      - key: user.email
        action: delete
      - key: customer.iban
        action: delete
      - key: customer.phone
        action: delete

      # User-ID ist OK (pseudonymisiert durch interne ID)
      # - key: user.id → NICHT löschen
```

---

## 8. Lösch-Recht umsetzen (DSGVO Art. 17)

```java
// "Right to be Forgotten": technische Umsetzung

@Service
public class DataDeletionService {

    @Transactional
    public void deleteCustomerData(String customerId) {
        log.info("Processing deletion request",
            kv("customerId", customerId));  // customerId loggen (keine E-Mail!)

        // 1. Kundendaten anonymisieren (nicht löschen wegen Buchhaltungspflicht!)
        customerRepository.anonymize(customerId);

        // 2. Auftragshistorie: Kundenbezug entfernen (Bestellung bleibt für Buchhaltung)
        orderRepository.removeCustomerReference(customerId);

        // 3. Aus Such-Index entfernen (→ ADR-104 Elasticsearch)
        customerSearchIndex.delete(customerId);

        // 4. Sessions/Tokens invalidieren (→ ADR-040)
        tokenBlacklist.invalidateAll(customerId);

        // 5. Audit-Log: Löschung dokumentieren (DSGVO-Nachweis!)
        auditLog.record(new DeletionEvent(customerId, Instant.now(),
            "Customer requested deletion (Art. 17 DSGVO)"));

        // 6. Event für andere Services (Kafka → ADR-041)
        events.publish(new CustomerDeletedEvent(customerId));
    }
}
```

---

## 9. Akzeptanzkriterien

- [ ] Alle Log-Ausgaben mit E-Mail, IBAN, Telefon werden durch PII-Masking-Converter gefiltert
- [ ] OTel Collector entfernt PII-Attribute aus Traces/Spans
- [ ] Hochsensible DB-Felder (E-Mail, IBAN, Telefon) sind AES-256 verschlüsselt
- [ ] Dev/Test-Umgebungen nutzen ausschließlich synthetische Testdaten
- [ ] Lösch-Anfragen (Art. 17) werden innerhalb 30 Tage technisch umgesetzt
- [ ] Datenschutz-Folgeabschätzung (DPIA) für neue PII-verarbeitende Features

---

## Quellen & Referenzen

- **DSGVO Art. 5, 17, 25, 32** — Datenschutzgrundsätze, Löschrecht, Privacy by Design, technische Maßnahmen.
- **ENISA, "Pseudonymisation Techniques and Best Practices" (2019)** — Technische Pseudonymisierungsverfahren.
- **BSI, "Kryptographische Verfahren" TR-02102** — AES-256-GCM als empfohlenes Verfahren.
- **NIST SP 800-175B, "Guideline for Using Cryptographic Standards"** — Key Management.

---

## Verwandte ADRs

- [ADR-035](ADR-035-gdpr-privacy-by-design.md) — DSGVO-Prinzipien
- [ADR-017](ADR-017-observability-logging-tracing.md) — Logging (PII-frei)
- [ADR-069](ADR-069-configuration-management.md) — Vault für Encryption Keys
