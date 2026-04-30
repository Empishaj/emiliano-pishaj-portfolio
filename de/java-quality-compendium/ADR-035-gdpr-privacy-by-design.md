# ADR-035 — GDPR & Privacy by Design

| Feld       | Wert                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x              |
| Datum      | 2024-01-01                        |
| Kategorie  | Security / Compliance (EU)        |

---

## Kontext & Problem

DSGVO (GDPR) ist kein bürokratisches Add-on — es ist ein Designprinzip. Personenbezogene Daten (Name, Email, IP, Standort, Nutzungsverhalten) müssen von Anfang an mit Bedacht erhoben, gespeichert und gelöscht werden. "Privacy by Design" bedeutet: Datenschutz ist in die Architektur eingebaut, nicht nachträglich draufgeklebt.

---

## Grundsätze (Art. 5 DSGVO) im Code

```
Datenminimierung    → Erhebe nur was wirklich gebraucht wird
Zweckbindung        → Verwende Daten nur für den Zweck für den sie erhoben wurden
Speicherbegrenzung  → Lösche Daten wenn sie nicht mehr gebraucht werden
Integrität          → Sichere Daten gegen unautorisierten Zugriff
Transparenz         → Betroffene können Auskunft, Berichtigung, Löschung verlangen
```

---

## Regel 1 — Datenminimierung: Nur erheben was gebraucht wird

```java
// ❌ Zu viele Felder — werden nie alle gebraucht
public record UserRegistrationRequest(
    String firstName,
    String lastName,
    String email,
    String phoneNumber,      // Wozu? Kein Use Case
    LocalDate birthDate,     // Wozu? Kein Use Case
    String gender,           // Wozu? Kein Use Case
    String streetAddress,    // Wozu bei Registration?
    String nationality       // Wozu?
) {}

// ✅ Nur das Minimum für den Zweck "Account erstellen"
public record UserRegistrationRequest(
    @NotBlank String name,   // Anzeigename — ja
    @Email    String email,  // Login-Identifier — ja
    @NotBlank String password // Authentifizierung — ja
    // Alles andere: erst erheben wenn ein konkreter Use Case es erfordert
) {}
```

---

## Regel 2 — Pseudonymisierung & Anonymisierung

```java
// Pseudonymisierung: echte Daten durch Identifier ersetzt — reversibel
@Entity
public class OrderAnalytics {
    private Long   orderId;
    private Long   pseudoUserId;    // Gehashter User-ID — kein direkter Personenbezug
    private String productCategory; // Kategorie statt Produktname
    private Money  totalAmount;
    private YearMonth period;       // Monat statt exaktes Datum — reduziert Identifizierbarkeit
    // KEIN: userId, name, email, shippingAddress
}

// Anonymisierungshelfermethode
public class PersonalDataAnonymizer {

    public String anonymizeEmail(String email) {
        // max@example.com → m***@example.com
        int atIndex = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    public String anonymizeName(String name) {
        // "Max Mustermann" → "M. M."
        return Arrays.stream(name.split(" "))
            .map(part -> part.charAt(0) + ".")
            .collect(joining(" "));
    }

    public String anonymizeIp(String ip) {
        // 192.168.1.42 → 192.168.1.0 (letztes Oktett entfernt)
        return ip.replaceAll("\\.[0-9]+$", ".0");
    }
}
```

---

## Regel 3 — Retention Policy: automatisches Löschen

```java
// Jede Entity mit personenbezogenen Daten hat eine Retention Policy
@Entity
public class UserActivity {
    @Id private Long id;
    private Long   userId;
    private String action;
    private Instant occurredAt;

    // Retention: 90 Tage (§ ... interne Policy)
    public static final Duration RETENTION = Duration.ofDays(90);
}

// Scheduled Job: löscht abgelaufene Daten
@Component
public class DataRetentionJob {

    @Scheduled(cron = "0 0 2 * * SUN") // Jeden Sonntag um 2 Uhr
    @Transactional
    public void purgeExpiredData() {
        var cutoff = Instant.now().minus(UserActivity.RETENTION);
        int deleted = activityRepository.deleteByOccurredAtBefore(cutoff);
        log.info("Data retention: deleted {} activity records older than {}", deleted, cutoff);
        auditLog.record("DATA_PURGE", Map.of("table", "user_activity", "count", deleted));
    }
}

// DSGVO Recht auf Löschung: Account-Löschung löscht alle personenbezogenen Daten
@Transactional
public void deleteUserAccount(UserId userId) {
    userRepository.delete(userId);
    activityRepository.deleteByUserId(userId);
    orderRepository.anonymizeUser(userId); // Bestellungen behalten, User anonymisieren
    auditLog.record("ACCOUNT_DELETION", Map.of("userId", userId));
    // ACHTUNG: Aufbewahrungspflichten (§ 147 AO: 10 Jahre für Rechnungen) beachten!
}
```

---

## Regel 4 — Sensitive Daten niemals im Log (→ ADR-017)

```java
// ❌ PII im Log
log.info("User registered: name={}, email={}, ip={}", name, email, request.getRemoteAddr());
log.debug("Payment processed: card={}, cvv={}", cardNumber, cvv);

// ✅ Nur Pseudonyme und nicht-identifizierende Informationen loggen
log.info("User registered: userId={}", userId);          // ID, kein Name
log.info("Payment processed: orderId={}", orderId);       // Transaktions-ID
// Nie: Name, Email, IP (ohne Anonymisierung), Kreditkarte, IBAN, Passwort

// AnnotationProcessor für sensitiven Felder:
public record PaymentData(
    @Sensitive String cardNumber,  // Custom-Annotation
    @Sensitive String cvv,
    Money amount
) {
    @Override
    public String toString() {
        return "PaymentData[cardNumber=***, cvv=***, amount=" + amount + "]";
    }
}
```

---

## Regel 5 — Verschlüsselung sensitiver Daten at rest

```java
// Verschlüsselte Felder direkt in JPA
@Entity
public class UserProfile {
    @Id private Long id;

    // Transparente Verschlüsselung via JPA AttributeConverter
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber; // Im Klartext im Java-Heap, AES-256 in der DB

    @Convert(converter = EncryptedStringConverter.class)
    private String bankAccountIban;
}

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private final EncryptionService encryption; // AES-256-GCM

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        return plaintext == null ? null : encryption.encrypt(plaintext);
    }

    @Override
    public String convertToEntityAttribute(String ciphertext) {
        return ciphertext == null ? null : encryption.decrypt(ciphertext);
    }
}
```

---

## Regel 6 — Auskunftsrecht (Art. 15 DSGVO) implementieren

```java
// Endpunkt für Datenauskunft
@GetMapping("/api/v1/privacy/my-data")
@PreAuthorize("isAuthenticated()")
public PersonalDataExport exportMyData(Authentication auth) {
    var userId = extractUserId(auth);

    return PersonalDataExport.builder()
        .profile(userService.findProfile(userId))
        .orders(orderService.findByUser(userId))
        .activities(activityService.findByUser(userId))
        .consentHistory(consentService.findByUser(userId))
        .exportedAt(Instant.now())
        .build();
}

// Endpunkt für Löschantrag
@DeleteMapping("/api/v1/privacy/my-account")
@PreAuthorize("isAuthenticated()")
@ResponseStatus(ACCEPTED) // 202: Asynchrone Verarbeitung
public void requestAccountDeletion(Authentication auth) {
    var userId = extractUserId(auth);
    deletionRequestService.submit(userId); // Asynchron: prüft Aufbewahrungspflichten
}
```

---

## Audit-Log für Datenzugriffe

```java
// Wer hat wann auf personenbezogene Daten zugegriffen?
@Aspect
@Component
public class PersonalDataAccessAudit {

    @AfterReturning("@annotation(PersonalDataAccess)")
    public void logAccess(JoinPoint jp, Object returnValue) {
        auditRepository.save(new AuditEntry(
            SecurityContextHolder.getContext().getAuthentication().getName(),
            jp.getSignature().getName(),
            Instant.now()
        ));
    }
}

// Verwendung
@PersonalDataAccess
public UserProfile getProfile(UserId userId) { ... }
```

---

## Konsequenzen

**Positiv:** Regulatorische Compliance. Vertrauen der Nutzer. Datenverluste haben kleinere Auswirkungen wenn weniger Daten vorhanden sind.

**Negativ:** Retention-Jobs und Löschlogik sind zusätzlicher Entwicklungsaufwand. Verschlüsselung at rest macht gewisse DB-Abfragen unmöglich (LIKE auf verschlüsselte Spalte funktioniert nicht).

---

## 💡 Guru-Tipps

- **Data Protection Impact Assessment (DPIA)** vor dem Verarbeiten neuer sensibler Datenkategorien.
- **Consent Management**: Einwilligungen mit Zeitstempel und Zweck speichern — nicht nur boolean.
- **Aufbewahrungspflichten** beachten: §§ 147 AO, 257 HGB schreiben 10 Jahre für bestimmte Geschäftsdaten vor — DSGVO-Löschpflicht hat Ausnahmen.
- **Datenschutzfolgenabschätzung** bei Profiling, Tracking, biometrischen Daten — Pflicht nach Art. 35 DSGVO.

---

## Verwandte ADRs

- [ADR-015](ADR-015-sicherheit-owasp.md) — Technische Sicherheitsmaßnahmen.
- [ADR-017](ADR-017-observability-logging-tracing.md) — Kein PII im Log.
- [ADR-034](ADR-034-db-migrations-flyway.md) — Schema-Änderungen für Anonymisierung.
