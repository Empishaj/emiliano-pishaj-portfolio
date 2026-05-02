# ADR-122 — SonarQube: Statische Code-Analyse als Qualitäts- und Sicherheitsgate

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Datum             | 2025-12-21                                                    |
| Kategorie         | Code-Qualität · SAST · Security · Wartbarkeit                 |

---

## 1. Kontext & Treiber

### 1.1 Das Problem ohne statische Analyse

```
OHNE STATISCHE ANALYSE — was in der Praxis passiert:

Problem A: Sicherheitslücke bleibt unentdeckt bis Produktion
  Entwickler schreibt:
    String query = "SELECT * FROM users WHERE name = '" + input + "'";
  Code-Review: alle müde, niemand sieht SQL-Injection
  Produktion: Angreifer exfiltriert gesamte Datenbank
  Kosten: 50.000 EUR Incident Response + DSGVO-Bußgeld

Problem B: Technische Schulden akkumulieren unbemerkt
  Methode wächst auf Cyclomatic Complexity 47
  Kein Werkzeug das warnt
  6 Monate später: "Diese Klasse kann niemand mehr anfassen"
  Kosten: 3 Sprints Refactoring = 50.000 EUR

Problem C: Code-Review als letztes Netz ist überlastet
  1.200 Zeilen Diff, 20 Minuten Review
  Reviewer übersieht: NPE-Risiko, Resource-Leak, duplizierte Logik
  → Bugs gehen durch

Problem D: Kein gemeinsamer, objektiver Qualitätsstandard
  Team A: sehr sauber, viele Reviews
  Team B: "funktioniert = gut genug"
  → Endlose subjektive Diskussionen, kein messbarer Fortschritt

STATISCHE ANALYSE LIEFERT:
  → Objektive, reproduzierbare Urteile: kein "das ist meine Meinung"
  → Lückenlose Abdeckung: jede Zeile, jede Datei, jeder Commit
  → Messbarer Trend: wird die Qualität besser oder schlechter?
  → Kein Review-Overhead für mechanisch erkennbare Probleme
```

---

## 2. Was SonarQube ist — Einordnung und Architektur

### 2.1 Einordnung im Sicherheits-Ökosystem

```
SAST (Static Application Security Testing) — SonarQubes Rolle:
  Analysiert QUELLCODE ohne ihn auszuführen.
  Findet Fehler durch Mustererkennung und Datenflussanalyse.
  Läuft im CI bei jedem Commit.

Die vier Sicherheitsebenen im Überblick:

  ┌────────────────────────────────────────────────────────────┐
  │  SCHICHT 1: SAST — SonarQube                               │
  │  Was: Quellcode analysieren                                │
  │  Wann: Jeder Commit                                        │
  │  Findet: SQL-Injection-Code, schwache Krypto, Hardcoded    │
  │          Secrets, NPE-Risiken, Code-Smells                 │
  │  NICHT: Laufzeitfehler, Library-CVEs, Infra-Probleme       │
  ├────────────────────────────────────────────────────────────┤
  │  SCHICHT 2: SCA — OWASP DC / Snyk                          │
  │  Was: Dependencies auf bekannte CVEs prüfen                │
  │  Wann: Jeder Build                                         │
  │  Findet: Log4Shell, Spring4Shell, CVEs in Jackson etc.     │
  │  NICHT: Fehler im eigenen Code                             │
  ├────────────────────────────────────────────────────────────┤
  │  SCHICHT 3: DAST — OWASP ZAP / Burp                        │
  │  Was: Laufende Anwendung angreifen                         │
  │  Wann: Staging-Deployment                                  │
  │  Findet: XSS, fehlende Header, Auth-Fehler, SSRF           │
  │  NICHT: Code-Qualität, Library-CVEs                        │
  ├────────────────────────────────────────────────────────────┤
  │  SCHICHT 4: Penetration Test (manuell)                     │
  │  Was: Tiefe manuelle Sicherheitsanalyse                    │
  │  Wann: Vor Major-Releases, jährlich                        │
  │  Findet: Business-Logik-Fehler, Auth-Flow-Lücken           │
  └────────────────────────────────────────────────────────────┘

SonarQube deckt SAST ab. Es ist ein Baustein, nicht die komplette Lösung.
```

### 2.2 SonarQube-Editionen

```
COMMUNITY EDITION (kostenlos, Open Source):
  ✅ 30+ Sprachen: Java, JavaScript/TypeScript, Python, C#, PHP, Go,
                   Kotlin, Ruby, Scala, Swift, CSS, HTML, XML...
  ✅ Bugs, Vulnerabilities, Code Smells, Security Hotspots
  ✅ OWASP Top 10 teilweise abgedeckt
  ✅ Quality Gates und Quality Profiles
  ✅ Coverage-Integration (JaCoCo, Istanbul, pytest-cov)
  ✅ Duplikationsanalyse
  ✅ On-Premise (eigener Server)
  ❌ Nur Default-Branch analysiert (kein Branch-Analyse!)
  ❌ Keine Pull-Request-Decoration (Inline-Kommentare im MR)
  ❌ Keine erweiterte Taint-Analyse
  ❌ Keine Security-Hotspot-Review-Workflows

DEVELOPER EDITION (~120–500 EUR/Monat je nach Projektgröße):
  ✅ Alles aus Community
  ✅ Branch-Analyse (Feature-Branches, Release-Branches)
  ✅ Pull-Request-Decoration: Inline-Kommentare direkt im GitLab/GitHub MR
  ✅ Taint-Analyse (Datenfluss über Methodengrenzen verfolgen)
  ✅ Security-Hotspot-Review-Workflow (Status: "Reviewed Safe / Fixed")

ENTERPRISE EDITION (~1.500+ EUR/Monat):
  ✅ Alles aus Developer
  ✅ Portfolios: Qualitätsübersicht über viele Projekte
  ✅ Executive-Reports
  ✅ Vollständige OWASP / CWE / SANS / PCI-DSS-Abdeckung

SONARCLOUD (SaaS, von Sonarsource gehostet):
  ✅ Developer-Edition-Features
  ✅ Kein eigener Server
  ✅ Free für Open-Source-Projekte
  ⚠ Quellcode verlässt das Unternehmen → DSGVO-Prüfung nötig
```

### 2.3 Systemarchitektur

```
┌─────────────────────────────────────────────────────────────────────┐
│                   SONARQUBE SYSTEMARCHITEKTUR                       │
│                                                                     │
│  CI-AGENT (GitLab Runner)            SONARQUBE SERVER              │
│  ┌──────────────────────────┐        ┌────────────────────────────┐│
│  │                          │        │                            ││
│  │  1. Code kompilieren     │        │  ① Report empfangen        ││
│  │     mvn compile          │        │  ② Quality Profile laden   ││
│  │                          │        │     (aktive Regelsammlung) ││
│  │  2. Tests + Coverage     │        │  ③ Issues berechnen        ││
│  │     mvn test             │        │  ④ Metriken aggregieren    ││
│  │     → jacoco.xml         │  HTTP  │  ⑤ Trends fortschreiben   ││
│  │                          │ Report │  ⑥ Quality Gate auswerten ││
│  │  3. Scanner ausführen    │───────▶│  ⑦ Status zurückschicken  ││
│  │     mvn sonar:sonar      │        │                            ││
│  │     → liest Sourcen,     │        │  ┌──────────────────────┐  ││
│  │       Binaries,          │◀───────│  │ PASSED ✅ / FAILED ❌│  ││
│  │       Coverage,          │ Status │  └──────────────────────┘  ││
│  │       SCM-Infos          │        │                            ││
│  │                          │        └────────────────────────────┘│
│  └──────────────────────────┘                                       │
│                                                                     │
│  CI-PIPELINE ERGEBNIS:                                              │
│    Quality Gate PASSED → Job grün → Pipeline läuft weiter          │
│    Quality Gate FAILED → Job rot → Pipeline blockiert              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Der Analyse-Mechanismus — Wie SonarQube "denkt"

### 3.1 Die vier Analyse-Stufen im Detail

SonarQube analysiert Code in aufsteigender Komplexität. Jede Stufe baut
auf der vorherigen auf und erkennt tiefere Klassen von Problemen.

```
╔═══════════════════════════════════════════════════════════════════════╗
║  STUFE 1: LEXIKALISCHE ANALYSE (Tokenisierung)                       ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  Quellcode wird in Tokens zerlegt — genau wie ein Compiler.          ║
║                                                                       ║
║  EINGABE:                                                             ║
║    String sql = "SELECT * FROM users WHERE id = " + userId;          ║
║                                                                       ║
║  AUSGABE (Token-Stream):                                              ║
║    [TYPE:String]  [IDENT:sql]   [ASSIGN:=]                           ║
║    [STRING:"SELECT * FROM users WHERE id = "]                        ║
║    [OPERATOR:+]  [IDENT:userId]  [SEMICOLON:;]                       ║
║                                                                       ║
║  WAS DAMIT ERKANNT WIRD:                                             ║
║    → Syntaktische Muster: "LITERAL + IDENTIFIER" in SQL              ║
║    → Duplikate: identische Token-Sequenzen in verschiedenen Dateien  ║
║    → Bestimmte verbotene Schlüsselwörter oder String-Literale        ║
║                                                                       ║
╠═══════════════════════════════════════════════════════════════════════╣
║  STUFE 2: SYNTAKTISCHE ANALYSE (Abstract Syntax Tree / AST)          ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  Token-Stream → Baumstruktur die die Grammatik des Codes abbildet.   ║
║                                                                       ║
║  AST für: String sql = "SELECT..." + userId;                         ║
║                                                                       ║
║    VariableDeclaration                                                ║
║    ├── Type: String                                                   ║
║    ├── Identifier: sql                                                ║
║    └── Initializer: BinaryExpression(+)                              ║
║         ├── StringLiteral("SELECT * FROM users WHERE id = ")        ║
║         └── IdentifierRef(userId)                                    ║
║                                                                       ║
║  WAS DAMIT ERKANNT WIRD:                                             ║
║    → "String-Literal + Variable" als SQL-Query-Muster                ║
║    → Methoden-Struktur: Cyclomatic Complexity berechnen              ║
║    → Klassen-Struktur: zu viele Methoden, zu tief verschachtelt      ║
║    → Annotation-Muster: @Transactional auf private Methoden          ║
║                                                                       ║
╠═══════════════════════════════════════════════════════════════════════╣
║  STUFE 3: SEMANTISCHE ANALYSE (Typ- und Symbol-Auflösung)            ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  AST + Typ-Informationen + Symbol-Tabellen aus kompilierten Klassen. ║
║  Sonar MUSS Bytecode/Binaries haben für volle semantische Analyse.   ║
║                                                                       ║
║  Für das SQL-Beispiel:                                                ║
║    userId ist vom Typ Long                                            ║
║    aber: userId wurde aus request.getParameter() gewonnen            ║
║    request.getParameter() gibt String zurück (HTTP-Parameter)        ║
║    String + String in SQL = Injection-Risiko                         ║
║                                                                       ║
║  WAS DAMIT ERKANNT WIRD:                                             ║
║    → Typen von Variablen und Rückgabewerten                          ║
║    → Welche Methoden zu welchen Klassen/Interfaces gehören           ║
║    → Vererbungshierarchien                                            ║
║    → @Override korrekt? Methode tatsächlich in Superklasse?          ║
║    → equals()/hashCode() zusammen implementiert?                     ║
║                                                                       ║
╠═══════════════════════════════════════════════════════════════════════╣
║  STUFE 4: TAINT-ANALYSE / DATENFLUSSANALYSE                          ║
║  (nur Developer Edition und höher)                                   ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  Verfolgt woher Daten stammen und wohin sie fließen —                ║
║  über Methoden- und Klassengrenzen hinweg.                            ║
║                                                                       ║
║  KONZEPT: Taint Sources, Taint Sinks, Sanitizer                      ║
║                                                                       ║
║  TAINT SOURCE = nicht vertrauenswürdige Eingabe:                     ║
║    request.getParameter(...)          HTTP-Parameter                 ║
║    request.getHeader(...)             HTTP-Header                    ║
║    resultSet.getString(...)           Datenbankwert                  ║
║    System.getenv(...)                 Umgebungsvariable              ║
║    Files.readString(...)              Dateiinhalt                    ║
║                                                                       ║
║  TAINT SINK = gefährlicher Verwendungsort:                           ║
║    Statement.executeQuery(sql)        SQL-Injection-Sink             ║
║    Runtime.exec(command)              OS-Command-Injection-Sink      ║
║    response.getWriter().print(html)   XSS-Sink                       ║
║    new URL(url).openConnection()      SSRF-Sink                      ║
║                                                                       ║
║  SANITIZER = bereinigt den Taint:                                    ║
║    PreparedStatement (? Parameter)    SQL-Injection neutralisiert    ║
║    StringEscapeUtils.escapeHtml()     XSS neutralisiert              ║
║    Pattern.matches("[0-9]+", input)   Validierung kann Taint entfernen║
║                                                                       ║
║  BEISPIEL — Taint fließt durch 3 Methoden:                           ║
║                                                                       ║
║    @GetMapping("/search")                                             ║
║    public String search(HttpServletRequest request) {                 ║
║        String term = request.getParameter("q");  // ← TAINT SOURCE   ║
║        return searchService.find(term);           // Taint fließt    ║
║    }                                                                  ║
║                                                                       ║
║    public List<Product> find(String term) {                           ║
║        return repository.query(term);             // Taint fließt    ║
║    }                                                                  ║
║                                                                       ║
║    public List<Product> query(String term) {                          ║
║        String sql = "SELECT * WHERE name='" + term + "'"; // ← SINK  ║
║        // Sonar: SQL-INJECTION! Taint von Source zu Sink verfolgt     ║
║        return jdbcTemplate.query(sql, ...);                           ║
║    }                                                                  ║
║                                                                       ║
║  WICHTIG: Ohne Taint-Analyse würde nur query() als verdächtig        ║
║  markiert werden. MIT Taint-Analyse: der gesamte Fluss inkl.         ║
║  des ursprünglichen HTTP-Parameters.                                  ║
╚═══════════════════════════════════════════════════════════════════════╝
```

### 3.2 Was der Scanner an den Server schickt

```
HÄUFIGES MISSVERSTÄNDNIS: "Der Quellcode wird zu SonarQube hochgeladen"

NEIN. Der Scanner analysiert LOKAL und schickt nur einen Report.

WAS DER SCANNER LOKAL MACHT:
  ① Liest alle Quelldateien (.java, .ts etc.)
  ② Liest kompilierte Klassen (.class) für semantische Analyse
  ③ Liest Coverage-Report (target/site/jacoco/jacoco.xml)
  ④ Liest SCM-Informationen (git blame: Wer hat Zeile N wann geschrieben?)
  ⑤ Baut AST und Token-Stream für jede Datei
  ⑥ Berechnet: Metriken, Token-Hashes für Duplikate
  ⑦ Komprimiert den Report

WAS IM REPORT STEHT (kein Quellcode!):
  → Metriken pro Datei (LOC, Complexity, Coverage-Daten)
  → Token-Hashes für Duplikationserkennung
  → AST-Informationen für regelbasierte Analyse am Server
  → SCM-Daten (Autor, Datum je Zeile)
  → Pfad-Informationen

WAS NICHT IM REPORT STEHT:
  → Kein Quellcode im Klartext (wichtig für Datenschutz!)
  → Keine kompilierten Klassen

KONSEQUENZ FÜR SELF-HOSTED SONARQUBE:
  Der Quellcode verlässt das Unternehmensnetz nicht.
  Reports werden verschlüsselt übertragen (HTTPS).
  → Gut für DSGVO-Compliance, für vertraulichen Geschäftscode.
```

---

## 4. Was SonarQube erkennt — Vollständige Kategorienbeschreibung

### 4.1 Bugs — Fehler die zur Laufzeit Probleme verursachen

```
SEVERITY: BLOCKER > CRITICAL > MAJOR > MINOR > INFO

BLOCKER Bugs (sofortiger CI-Stop, kein Merge möglich):
────────────────────────────────────────────────────────

  Null-Dereference nach expliziter Null-Prüfung ignoriert:
    if (user == null) {
        log.warn("User not found");
        // KEIN return, kein throw!
    }
    String name = user.getName();  // BLOCKER: user ist hier null!

  Unendliche Rekursion:
    public int factorial(int n) {
        return n * factorial(n);  // BLOCKER: kein Abbruch, StackOverflow!
    }

CRITICAL Bugs:
────────────────
  Integer-Overflow bei Arithmetik:
    long bytes = file.length() * 1024 * 1024;
    // file.length() gibt int zurück! Overflow vor long-Zuweisung.
    // Korrekt: file.length() * 1024L * 1024L

  equals() vergleicht statt Inhalt die Referenz:
    if (status == "CONFIRMED") { ... }
    // String-Vergleich mit == → immer false bei String-Pool-Problemen
    // Korrekt: status.equals("CONFIRMED") oder "CONFIRMED".equals(status)

  Resource-Leak (Stream/Connection nie geschlossen):
    InputStream is = new FileInputStream(file);
    processData(is);
    // is wird nie geschlossen → Datei-Handle-Leak
    // Korrekt: try-with-resources

MAJOR Bugs:
────────────
  Collection.size() auf unveränderter Schleife:
    for (int i = 0; i < list.size(); i++) {
        list.add(newElement);  // MAJOR: Endlosschleife!
    }

  Falsche equals/hashCode-Implementierung:
    @Override public boolean equals(Object o) { /* ... */ }
    // hashCode() NICHT überschrieben!
    // MAJOR: HashSet/HashMap funktioniert falsch mit diesem Objekt
```

### 4.2 Vulnerabilities — direkt ausnutzbare Sicherheitslücken

```
BLOCKER Vulnerabilities (niemals in Produktion deployen):
──────────────────────────────────────────────────────────

  SQL-Injection (OWASP A03):
    ❌ String q = "SELECT * FROM orders WHERE id='" + id + "'";
    ✅ PreparedStatement ps = conn.prepareStatement(
           "SELECT * FROM orders WHERE id=?");
       ps.setString(1, id);

  Hardcodierte Credentials (OWASP A07):
    ❌ String password = "admin123";
    ❌ String apiKey = "sk-prod-xxxxxxxxxxxxxxxxxxx";
    ✅ String password = System.getenv("DB_PASSWORD");

  OS-Command-Injection:
    ❌ Runtime.getRuntime().exec("ffmpeg -i " + userInput);
    ✅ ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", sanitizedInput);

CRITICAL Vulnerabilities:
───────────────────────────
  Cross-Site-Scripting (XSS) (OWASP A03):
    ❌ response.getWriter().println("<p>Hello " + username + "</p>");
    ✅ response.getWriter().println("<p>Hello "
           + HtmlUtils.htmlEscape(username) + "</p>");

  Schwache Kryptografie (OWASP A02):
    ❌ MessageDigest.getInstance("MD5")    // gebrochen seit 2004
    ❌ MessageDigest.getInstance("SHA-1")  // kollisionsunsicher
    ✅ MessageDigest.getInstance("SHA-256") // oder SHA-3-256

  Schwache Verschlüsselung:
    ❌ Cipher.getInstance("DES")           // 56-Bit Key, gebrochen
    ❌ Cipher.getInstance("AES/ECB/...")   // ECB-Mode: unsicher!
    ✅ Cipher.getInstance("AES/GCM/NoPadding")  // authentifizierte Verschlüsselung

  LDAP-Injection:
    ❌ ctx.search("ou=users", "(uid=" + username + ")", sc);
    ✅ ctx.search("ou=users",
           "(uid={0})", new Object[]{username}, sc);

  XML External Entity (XXE):
    ❌ DocumentBuilderFactory.newInstance().newDocumentBuilder();
       // ohne XXE-Schutz!
    ✅ DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
       factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
       factory.setExpandEntityReferences(false);

  Server-Side Request Forgery (SSRF):
    ❌ URL url = new URL(request.getParameter("url"));
       url.openConnection().getInputStream();  // SSRF: Angreifer kann interne URLs abrufen
    ✅ // Whitelist erlaubter Hosts + Schema-Prüfung

  Unsichere Deserialisierung:
    ❌ ObjectInputStream ois = new ObjectInputStream(inputStream);
       Object obj = ois.readObject();  // Beliebige Klassen deserialiserbar → RCE!
    ✅ // Verwende sichere Deserialisierer oder Whitelist-basierte Lösung
```

### 4.3 Security Hotspots — Manueller Überprüfungsbedarf

```
KONZEPT: Code der sicherheitsrelevant ist aber kein definitiver Fehler.
         SonarQube kann nicht automatisch entscheiden ob er sicher ist.
         Ein Mensch muss prüfen und "Reviewed: Safe / Fixed" bestätigen.

WARUM KEIN DIREKTES ISSUE:
  Manchmal ist der Code korrekt und sicher.
  Manchmal ist er unsicher.
  Nur mit Business-Kontext entscheidbar.

BEISPIEL 1: CORS-Konfiguration
  response.setHeader("Access-Control-Allow-Origin", "*");
  → Hotspot: Ist * für diese API wirklich gewollt?
  → Review: öffentliche API → Safe. Interne API → ändern!

BEISPIEL 2: Random statt SecureRandom
  Random random = new Random();
  int token = random.nextInt(1000000);
  → Hotspot: Ist kryptografische Sicherheit nötig?
  → Review: für Zufalls-Rabattcode → Safe.
             für Passwort-Reset-Token → KRITISCH, muss SecureRandom sein!

BEISPIEL 3: HTTP-Request-Parameter ohne Validierung
  String data = request.getParameter("data");
  // Wird data sanitiert bevor es verwendet wird?
  → Review: Folgende Verwendungen analysieren

BEISPIEL 4: JNDI-Lookup (Log4Shell-Klasse)
  InitialContext ctx = new InitialContext();
  ctx.lookup(name);
  → Hotspot: Kommt name aus User-Input? → Kritisch!
  → Review: Wenn name aus Konfiguration → Safe.
             Wenn name aus HTTP-Request → CVE-ähnlich!

WORKFLOW FÜR HOTSPOTS:
  1. SonarQube markiert als "To Review"
  2. Security-Engineer öffnet Hotspot
  3. Prüft Code-Kontext, Verwendung
  4. Entscheidung:
     "Acknowledged": bekannt, wird beobachtet
     "Safe":         bewusst so implementiert, sicher
     "Fixed":        war unsicher, wurde korrigiert
  5. Quality Gate berücksichtigt: % reviewte Hotspots
```

### 4.4 Code Smells — Wartbarkeitsprobleme

```
TECHNICAL DEBT: Jeder Code Smell hat eine geschätzte "Debt"-Zeit.
  MINOR Code Smell = 5 Minuten Debt
  MAJOR Code Smell = 30 Minuten Debt
  CRITICAL Code Smell = 1 Stunde Debt

  Technical Debt Ratio = Debt / Entwicklungszeit gesamt

RATING: A (gut) bis E (sehr schlecht)
  A: Debt Ratio ≤ 5%
  B: Debt Ratio ≤ 10%
  C: Debt Ratio ≤ 20%
  D: Debt Ratio ≤ 50%
  E: Debt Ratio > 50%

BEISPIELE CODE SMELLS:
  Cyclomatic Complexity > 10 (MAJOR):
    Jedes if/else/while/for/case erhöht die Complexity um 1.
    Methode mit Complexity 30 braucht theoretisch 30 Testfälle!

  Methode zu lang > 30 Zeilen (MINOR):
    Single Responsibility Principle verletzt.

  God Class (zu viele Methoden/Felder):
    Klasse mit 50+ Methoden → schlechte Kohäsion.

  Toter Code:
    private void unusedMethod() { ... }
    → Verwirrt, erhöht Maintenance-Aufwand, kein Wert.

  TODO-Kommentare:
    // TODO: fix this properly
    → Wird nie gemacht, verschmutzt Code.

  Parameteranzahl > 7:
    public void process(String a, int b, boolean c,
                        List d, Map e, String f, int g)
    → Builder-Pattern oder Objekt-Parameter verwenden.

  Duplikation (Copy-Paste-Code):
    Dieselbe Logik an 3 Stellen → wird an 2 Stellen falsch gepatcht.
    Sonar erkennt auch semantische Duplikate (umbenannte Variablen).

  Magic Numbers:
    if (status == 3) { ... }  // Was bedeutet 3?
    → Konstante: if (status == STATUS_CONFIRMED) { ... }
```

### 4.5 Coverage & Duplikation — Metriken

```
COVERAGE-METRIKEN (aus JaCoCo XML-Report):

  Line Coverage:
    Welcher Prozentsatz aller ausführbaren Zeilen wurde
    von mindestens einem Test ausgeführt?
    Ziel: ≥ 80% für neuen Code

  Branch Coverage (wichtiger als Line Coverage!):
    Wurde jeder if/else-Zweig einmal "wahr" UND einmal "falsch" ausgeführt?
    if (user == null)         ← wurde null-Fall getestet?
      return Optional.empty();
    return Optional.of(user); ← wurde nicht-null-Fall getestet?
    Ziel: ≥ 70% für neuen Code

  Condition Coverage:
    Wurden alle Teile zusammengesetzter Bedingungen getestet?
    if (a && b)  ← Test für: a=true/b=true, a=true/b=false,
                             a=false/b=true, a=false/b=false ?

DUPLIKATIONS-METRIKEN:
  Duplicated Lines Density:
    Prozent der Zeilen die als Duplikat erkannt werden.
    Sonar erkennt Duplikate auch wenn:
    - Variablennamen geändert wurden
    - Kommentare hinzugefügt wurden
    - Leerzeilen verändert wurden
    Ziel: < 3% für neuen Code
```

---

## 5. OWASP Top 10 Abdeckung — Was Sonar erkennt und was nicht

```
OWASP TOP 10 (2021) — detaillierte Einschätzung:

A01 Broken Access Control ──────────────────────────────────────────────
  ✅ Erkennt: fehlende @PreAuthorize auf Methoden (mit Spring-Plugin)
  ✅ Erkennt: hartcodierte User-IDs in Abfragen
  ❌ Nicht erkennbar: ob die Zugriffssteuerungslogik korrekt ist
     ("User darf nur eigene Bestellungen sehen" — ist Business-Logik)

A02 Cryptographic Failures ─────────────────────────────────────────────
  ✅ MD5, SHA-1 als Hash-Funktion
  ✅ DES, 3DES, RC4 als Cipher
  ✅ ECB-Modus für AES
  ✅ Kleine RSA-Keys (< 2048 Bit)
  ✅ Fehlende IV-Randomisierung bei CBC
  ✅ Hardcodierte Schlüssel und Passwörter
  ❌ Nicht: falsche Zertifikat-Validierung zur Laufzeit
  ❌ Nicht: TLS-Protokoll-Konfiguration (web.xml/application.yml)

A03 Injection ──────────────────────────────────────────────────────────
  ✅ SQL-Injection (Community Edition: Muster; Dev-Edition: Taint)
  ✅ LDAP-Injection
  ✅ OS-Command-Injection (Runtime.exec, ProcessBuilder)
  ✅ Code-Injection (Groovy-Eval, ScriptEngine mit User-Input)
  ✅ XSS (Cross-Site-Scripting) in Servlet/JSP/Thymeleaf
  ❌ Nicht: Blind-Injection (nur durch Timing-Unterschiede erkennbar)
  ❌ Nicht: Second-Order-Injection (Daten werden erst gespeichert, dann genutzt)

A04 Insecure Design ────────────────────────────────────────────────────
  ❌ Größtenteils nicht erkennbar — Architektur- und Design-Entscheidungen
  ✅ Teilweise: fehlende Input-Validierung als Muster
  Lösung: Threat Modeling, Architecture Reviews

A05 Security Misconfiguration ──────────────────────────────────────────
  ✅ CORS: Access-Control-Allow-Origin: * als Hotspot
  ✅ Fehlende Security-Features (z.B. HttpOnly-Flag in Code)
  ❌ Nicht: application.yml/web.xml-Konfiguration
  ❌ Nicht: Kubernetes/Docker-Konfiguration
  ❌ Nicht: Cloud-IAM-Berechtigungen

A06 Vulnerable and Outdated Components ─────────────────────────────────
  ❌ VOLLSTÄNDIG AUSSERHALB DES SCOPE!
  SonarQube analysiert EIGENEN Code, nicht Libraries.
  Für CVEs in Abhängigkeiten: OWASP Dependency Check, Snyk (→ ADR-057)

A07 Identification and Authentication Failures ─────────────────────────
  ✅ Hardcodierte Passwörter/Tokens im Code
  ✅ Schwache Hashing-Algorithmen für Passwörter (MD5, SHA-1)
  ✅ Fehlende Passwort-Stärke-Validierungen (als Hotspot)
  ❌ Nicht: Brute-Force-Schutz (Logik-Ebene)
  ❌ Nicht: Session-Management (Runtime-Verhalten)
  ❌ Nicht: MFA fehlt (Architektur-Entscheidung)

A08 Software and Data Integrity Failures ───────────────────────────────
  ✅ Unsichere Java-Deserialisierung (ObjectInputStream ohne Whitelist)
  ❌ Nicht: Dependency-Integrität (kein Checksum-Vergleich)
  ❌ Nicht: Update-Signatur-Validierung

A09 Security Logging & Monitoring Failures ─────────────────────────────
  ✅ Logging sensitiver Daten: log.info("Password: " + password)
  ✅ Logging von Credentials in Fehlermeldungen
  ❌ Nicht: ob genug geloggt wird (fehlende Log-Statements)
  ❌ Nicht: SIEM-Konfiguration, Alert-Vollständigkeit

A10 Server-Side Request Forgery (SSRF) ─────────────────────────────────
  ✅ URL-Konstruktion aus User-Input (Hotspot / Vulnerability)
  ✅ HttpURLConnection / OkHttp mit User-Input als URL
  ❌ Nicht: alle SSRF-Varianten (Header-basiert, Redirect-basiert)
```

---

## 6. Quality Gates — Das CI/CD-Entscheidungssystem

### 6.1 Konzept und Standard-Gate

```
QUALITY GATE = Türsteher zwischen Code und Deployment.

"Dein Code darf nur gemergt/deployed werden wenn er DIESE
 Qualitätskriterien erfüllt."

STANDARD QUALITY GATE ("Sonar way"):
  Alle Bedingungen beziehen sich auf NEW CODE (nur geänderte Zeilen):
  
  Condition 1: New Bugs           = 0
  Condition 2: New Vulnerabilities = 0
  Condition 3: New Hotspots: 0 unreviewed
  Condition 4: New Coverage       ≥ 80%
  Condition 5: New Duplication    ≤ 3%

WARUM "New Code" statt "Overall Code":
  Ein bestehendes Projekt hat oft hunderte alter Issues.
  Alles auf einmal zu fixen: unrealistisch.
  New-Code-Strategie = Clean-as-you-go:
    → Neuer Code muss sauber sein
    → Altlast wird schrittweise abgebaut
    → Quality Gate ist erreichbar, auch für Legacy-Projekte
```

### 6.2 Eigenes Quality Gate konfigurieren

```
Navigation: Administration → Quality Gates → Create

EMPFOHLENE KONFIGURATION für produktives Java-Projekt:

Bedingung                        | Operator         | Wert  | Severity
─────────────────────────────────┼──────────────────┼───────┼──────────
New Bugs                         | is greater than  |  0    | ERROR
New Vulnerabilities              | is greater than  |  0    | ERROR
New Security Hotspots Reviewed   | is less than     | 100%  | ERROR
New Coverage (on New Code)       | is less than     | 80%   | ERROR
New Duplicated Lines Density     | is greater than  |  3%   | ERROR
New Reliability Rating           | is worse than    |  A    | ERROR
New Security Rating              | is worse than    |  A    | ERROR
New Maintainability Rating       | is worse than    |  A    | WARNING
Overall Technical Debt Ratio     | is greater than  |  10%  | WARNING

WICHTIG: ERROR = Pipeline schlägt fehl (Merge blockiert!)
         WARNING = wird angezeigt, Pipeline läuft weiter

ASSIGNMENT zu Projekten:
  Projekt → Administration → Quality Gate → wähle das Gate
```

---

## 7. Quality Profiles — Regelsammlungen verwalten

### 7.1 Was ein Quality Profile ist

```
QUALITY PROFILE = die aktive Regelsammlung für eine Sprache.

Jedes Projekt hat genau EIN Quality Profile pro Sprache.
Das Profile bestimmt welche Regeln analysiert werden und mit
welcher Severity Issues reported werden.

STANDARD: "Sonar way" (von Sonarsource gepflegt)
  → ~600 aktive Regeln für Java
  → Wird von Sonarsource aktualisiert (neue Regeln bei Updates!)
  → PROBLEM: Update aktiviert neue Regeln → CI schlägt unerwartet fehl

EMPFEHLUNG: Eigenes Profil auf Basis von "Sonar way"
  1. "Sonar way" kopieren → "Company Java Rules" benennen
  2. Eigene Anpassungen: Regeln hinzufügen/entfernen/severity ändern
  3. Dieses Profil als Default setzen für alle Java-Projekte
  4. Updates zu "Sonar way" KONTROLLIERT übernehmen (nicht automatisch)
```

### 7.2 Regeln aktivieren, deaktivieren, severity ändern

```
Navigation: Quality Profiles → [Profil] → Activate More / Inactive Rules

SINNVOLLE ANPASSUNGEN:

① Regel aktivieren die in "Sonar way" nicht aktiv ist:
   java:S5841 — Use "notEmpty()" to check if the collection is not empty
   → Aktivieren: MINOR
   (Sonar way hat es nicht, wir wollen es explizit haben)

② Severity erhöhen für kritische Bereiche:
   java:S2068 — Credentials should not be hard-coded
   Standard: CRITICAL → Wir setzen: BLOCKER
   (Für uns absolutes No-Go, kein Merge möglich)

③ Regeln für Test-Code weniger streng:
   java:S2699 — Tests should include assertions
   → Gilt für src/test/**, aber nicht für src/main/**
   Exclude-Pattern: **/*Test.java, **/*IT.java, **/*Spec.java

④ Framework-spezifische Regeln aktivieren:
   Spring-Plugin: S4605, S3457 (Spring-MVC-spezifische Regeln)
   → Müssen manuell aktiviert werden wenn Spring verwendet wird
```

---

## 8. Eigene Regeln — Das vollständige Custom-Rule-System

### 8.1 Die drei Wege für Custom Rules

```
WEG 1: XPATH-REGELN (nur für einfache strukturelle Muster)
  → Im SonarQube UI konfigurierbar, kein Java-Code
  → Gut für: "Annotation X darf nicht auf Konstruktor stehen"
  → Begrenzt: kein Zugriff auf Typinformationen, kein Datenfluss

WEG 2: TEMPLATE-REGELN (parametrisierbare Standardregeln)
  → Vordefinierte Templates anpassen (z.B. "Methode X nie aufrufen")
  → Gut für: "System.exit() ist verboten", "Thread.sleep() ist verboten"

WEG 3: JAVA-PLUGIN (vollständig, für komplexe Regeln)
  → Maven-Projekt, deployed als .jar auf SonarQube-Server
  → Vollständiger AST-Zugriff
  → Semantische Analyse (Typen, Vererbung, Symboltabellen)
  → Beliebig komplexe Regeln
  → Testbar mit Sonar Test-Framework
  ← DIESER WEG wird hier vollständig beschrieben
```

### 8.2 Maven-Projekt für Custom Rules aufsetzen

```xml
<!-- pom.xml des Custom-Rule-Plugins -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.sonar</groupId>
    <artifactId>sonar-custom-java-rules</artifactId>
    <version>1.0.0</version>

    <!--
        WICHTIG: packaging = sonar-plugin
        Nicht "jar"! Das sonar-packaging-maven-plugin
        verarbeitet diesen Typ und baut ein SonarQube-kompatibles Artefakt.
    -->
    <packaging>sonar-plugin</packaging>

    <name>Company Custom Java Rules</name>
    <description>Unternehmens-spezifische Java-Regeln für SonarQube</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- SonarQube-Versionen MÜSSEN mit dem Server übereinstimmen! -->
        <!-- Server-Version: Administration → System → About -->
        <sonar.api.version>10.3.0.82913</sonar.api.version>
        <java.plugin.version>7.29.0.33895</java.plugin.version>
    </properties>

    <dependencies>

        <!--
            SonarQube Plugin API: Interfaces und Basisklassen für Plugins.
            scope=provided: Der SonarQube-Server liefert diese Klassen
            zur Laufzeit. Darf NICHT ins Plugin-JAR gepackt werden!
        -->
        <dependency>
            <groupId>org.sonarsource.sonarqube</groupId>
            <artifactId>sonar-plugin-api</artifactId>
            <version>${sonar.api.version}</version>
            <scope>provided</scope>
        </dependency>

        <!--
            Java Frontend: AST-Klassen für Java-Analyse.
            Stellt die Basis-Besucher und Tree-Interfaces bereit.
            scope=provided: Wird vom java-Plugin des Servers bereitgestellt.
        -->
        <dependency>
            <groupId>org.sonarsource.java</groupId>
            <artifactId>java-frontend</artifactId>
            <version>${java.plugin.version}</version>
            <scope>provided</scope>
        </dependency>

        <!--
            Test-Kit: Ermöglicht das Testen von Custom Rules gegen
            Beispiel-Java-Dateien ohne echten SonarQube-Server.
        -->
        <dependency>
            <groupId>org.sonarsource.java</groupId>
            <artifactId>java-checks-testkit</artifactId>
            <version>${java.plugin.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.25.3</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>
        <plugins>

            <!-- Java 21 Compiler -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>

            <!--
                SonarQube Packaging Plugin: baut das Plugin-JAR
                mit korrektem MANIFEST.MF (Plugin-Metadaten).
                PFLICHT für SonarQube-Plugins!
            -->
            <plugin>
                <groupId>org.sonarsource.sonar-packaging-maven-plugin</groupId>
                <artifactId>sonar-packaging-maven-plugin</artifactId>
                <version>1.23.0.740</version>
                <extensions>true</extensions>
                <configuration>
                    <!-- Eindeutiger Plugin-Key (Kleinbuchstaben, keine Sonderzeichen) -->
                    <pluginKey>companyjava</pluginKey>
                    <!-- Anzeigename in SonarQube UI -->
                    <pluginName>Company Java Rules</pluginName>
                    <!-- Beschreibung in SonarQube UI -->
                    <pluginDescription>
                        Unternehmens-spezifische Java-Analyseregeln
                    </pluginDescription>
                    <!-- Vollqualifizierter Klassenname des Plugin-Einstiegspunkts -->
                    <pluginClass>com.example.sonar.CompanyJavaPlugin</pluginClass>
                    <!--
                        Base-Plugin: dieses Plugin erweitert das Java-Plugin.
                        Ohne diesen Eintrag läuft das Plugin nicht korrekt!
                    -->
                    <basePlugin>java</basePlugin>
                    <!-- Minimale SonarQube-Version -->
                    <sonarQubeMinVersion>10.0</sonarQubeMinVersion>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
```

### 8.3 Plugin-Einstiegspunkt und Repository

```java
// CompanyJavaPlugin.java — Plugin-Hauptklasse
package com.example.sonar;

import org.sonar.api.Plugin;

/**
 * Plugin-Einstiegspunkt: registriert alle Erweiterungen.
 *
 * SonarQube instantiiert diese Klasse beim Server-Start und ruft
 * define() auf. Alle hier registrierten Klassen werden vom
 * Dependency-Injection-Container des Servers verwaltet.
 */
public class CompanyJavaPlugin implements Plugin {

    @Override
    public void define(Context context) {
        context.addExtensions(
            // Repository-Definition (Metadaten für alle Regeln)
            CompanyJavaRulesDefinition.class,

            // Individuelle Regel-Klassen
            NoTransactionalOnPrivateRule.class,
            NoPiiFieldInLogsRule.class,
            MoneyAsDoubleRule.class,
            NoDirectRepoAccessRule.class
        );
    }
}
```

```java
// CompanyJavaRulesDefinition.java — Repository-Metadaten
package com.example.sonar;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class CompanyJavaRulesDefinition implements RulesDefinition {

    // Eindeutiger Key des Repositories — erscheint in der Regel-URL
    public static final String REPO_KEY  = "company-java";
    public static final String REPO_NAME = "Company Java Rules";
    public static final String LANGUAGE  = "java";

    // Pfad zu den HTML-Beschreibungs-Dateien (resources/org/sonar/l10n/java/rules/)
    private static final String RESOURCE_BASE_PATH =
        "org/sonar/l10n/java/rules/company-java";

    @Override
    public void define(Context context) {
        NewRepository repo = context
            .createRepository(REPO_KEY, LANGUAGE)
            .setName(REPO_NAME);

        // RuleMetadataLoader liest Metadaten aus Annotations UND aus
        // optionalen HTML/JSON-Dateien in resources/
        new RuleMetadataLoader(RESOURCE_BASE_PATH)
            .addRulesByAnnotatedClass(repo, List.of(
                NoTransactionalOnPrivateRule.class,
                NoPiiFieldInLogsRule.class,
                MoneyAsDoubleRule.class,
                NoDirectRepoAccessRule.class
            ));

        repo.done();
    }
}
```

### 8.4 Regel 1: @Transactional auf privaten Methoden verboten

```java
package com.example.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;

import java.util.List;

/**
 * REGEL: @Transactional auf private Methoden hat keinen Effekt.
 *
 * PROBLEM:
 *   Spring implementiert @Transactional über AOP-Proxies.
 *   Proxies können private Methoden NICHT intercepten.
 *   Der Proxy ruft bei private Methoden direkt das Objekt auf,
 *   ohne den Transaktions-Aspekt zu weben.
 *
 *   Das Ergebnis: @Transactional auf private Methoden wird
 *   STILLSCHWEIGEND IGNORIERT. Kein Fehler, kein Warning — nur
 *   kein Transaktions-Schutz. Das führt zu inkonsistenten Daten.
 *
 * ❌ SCHLECHT:
 *   @Transactional           // Wird von Spring ignoriert!
 *   private void saveOrder(Order order) {
 *       orderRepo.save(order);
 *       inventoryRepo.reserve(order);  // Wenn das fehlschlägt:
 *       // orderRepo.save() wurde NICHT zurückgerollt!
 *   }
 *
 * ✅ GUT:
 *   @Transactional           // Proxy greift, Transaktion aktiv
 *   public void saveOrder(Order order) { ... }
 *
 *   // Wenn private Methode nötig:
 *   // Die public-Methode mit @Transactional die private aufruft —
 *   // Transaktion läuft durch die public-Methode.
 */
@Rule(
    key         = "COMPANY001",
    name        = "@Transactional must not be placed on private methods",
    description = "Spring AOP proxies cannot intercept private methods. "
                + "@Transactional on a private method is silently ignored, "
                + "meaning no transaction boundary is applied. "
                + "This leads to data inconsistency without any error or warning. "
                + "Make the method public, or move the @Transactional to the "
                + "calling public method.",
    tags        = {"spring", "transaction", "correctness", "bug"}
)
public class NoTransactionalOnPrivateRule extends IssuableSubscriptionVisitor {

    // IssuableSubscriptionVisitor: Wir abonnieren nur MethodTree-Knoten.
    // Effizienter als BaseTreeVisitor (der den gesamten Baum traversiert).
    @Override
    public List<Tree.Kind> nodesToVisit() {
        return List.of(Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree method = (MethodTree) tree;

        // Schritt 1: Ist die Methode privat?
        boolean isPrivate = method.modifiers().modifiers().stream()
            .anyMatch(modifier -> modifier.modifier() == Modifier.PRIVATE);

        if (!isPrivate) {
            return; // Nicht privat → kein Problem
        }

        // Schritt 2: Hat sie eine @Transactional-Annotation?
        method.modifiers().annotations().stream()
            .filter(NoTransactionalOnPrivateRule::isTransactional)
            .findFirst()
            .ifPresent(annotation ->
                // Issue an der Position der Annotation melden
                reportIssue(annotation,
                    "Remove @Transactional from private method '"
                    + method.simpleName().name() + "'. "
                    + "Spring AOP cannot intercept private methods — "
                    + "this annotation has no effect here.")
            );
    }

    /**
     * Prüft ob eine Annotation @Transactional ist.
     * Berücksichtigt: vollqualifizierter Name, einfacher Name,
     * Spring-Transaktion, Jakarta-Transaktion.
     */
    private static boolean isTransactional(AnnotationTree annotation) {
        String name = annotation.annotationType().toString();
        return switch (name) {
            case "Transactional",
                 "org.springframework.transaction.annotation.Transactional",
                 "javax.transaction.Transactional",
                 "jakarta.transaction.Transactional"  -> true;
            default -> false;
        };
    }
}
```

### 8.5 Regel 2: PII-Annotierte Felder nicht ins Log

```java
package com.example.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.*;

import java.util.List;
import java.util.Set;

/**
 * REGEL: Getter-Methoden für @PiiField-annotierte Felder
 *        dürfen nicht direkt in Logger-Aufrufe eingebettet werden.
 *
 * HINTERGRUND:
 *   DSGVO Art. 5 fordert Datenminimierung.
 *   Logs werden langfristig gespeichert und haben breiten Zugang.
 *   E-Mail, IBAN, Telefonnummer im Log = DSGVO-Verstoß.
 *
 * ❌ SCHLECHT:
 *   log.info("Bestellung für Kunde: {}", customer.getEmail());
 *   // customer.getEmail() gibt ein @PiiField zurück
 *   // → E-Mail-Adresse landet im Log
 *
 * ✅ GUT:
 *   log.info("Bestellung für Kunde-ID: {}", customer.getId());
 *   // ID ist kein PII
 *   // Oder: Masking-Utility verwenden:
 *   log.info("Bestellung für: {}", PiiMasker.mask(customer.getEmail()));
 *
 * TECHNISCHE IMPLEMENTIERUNG:
 *   1. Finde alle Logger-Aufrufe (SLF4J, Log4j, JUL)
 *   2. Für jedes Argument: prüfe ob es ein Getter-Aufruf ist
 *   3. Für Getter-Aufrufe: prüfe ob das korrespondierende Feld @PiiField hat
 *   4. Wenn ja: Issue melden
 */
@Rule(
    key         = "COMPANY002",
    name        = "@PiiField values must not be passed to log methods",
    description = "Fields annotated with @PiiField contain Personally Identifiable "
                + "Information (PII). Logging PII directly violates GDPR Article 5 "
                + "(data minimisation principle) and may expose sensitive data to "
                + "anyone with log access. Use the customer ID instead of PII fields, "
                + "or apply PiiMasker.mask() before logging.",
    tags        = {"gdpr", "privacy", "security", "pii", "logging"}
)
public class NoPiiFieldInLogsRule extends IssuableSubscriptionVisitor {

    // Alle bekannten Logger-Methoden (SLF4J + Log4j2 + java.util.logging)
    private static final Set<String> LOG_METHODS = Set.of(
        "trace", "debug", "info", "warn", "error", "fatal",
        "log", "severe", "warning", "fine", "finer", "finest"
    );

    // Vollqualifizierte Namen der @PiiField-Annotation
    // (einfacher Name + vollqualifizierter Name für beide Fälle)
    private static final Set<String> PII_ANNOTATION_NAMES = Set.of(
        "PiiField",
        "com.example.annotation.PiiField"
    );

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return List.of(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree invocation = (MethodInvocationTree) tree;

        // Ist es ein Logger-Aufruf?
        if (!isLoggerCall(invocation)) {
            return;
        }

        // Prüfe jedes Argument des Logger-Aufrufs
        for (ExpressionTree argument : invocation.arguments()) {
            if (isGetterOnPiiField(argument)) {
                reportIssue(argument,
                    "This argument accesses a @PiiField. "
                    + "Do not log PII directly. "
                    + "Use the entity ID or apply PiiMasker.mask() before logging. "
                    + "GDPR requires data minimisation — logs are long-term storage.");
            }
        }
    }

    /**
     * Prüft ob ein Methodenaufruf ein Logger-Aufruf ist.
     * Erkennt: log.info(...), LOG.warn(...), logger.debug(...) etc.
     */
    private boolean isLoggerCall(MethodInvocationTree invocation) {
        if (!(invocation.methodSelect() instanceof MemberSelectExpressionTree member)) {
            return false;
        }

        // Methodenname muss ein bekannter Log-Methodenname sein
        String methodName = member.identifier().name();
        if (!LOG_METHODS.contains(methodName)) {
            return false;
        }

        // Empfänger-Name muss wie ein Logger aussehen
        // (heuristisch: enthält "log" oder "Log" case-insensitiv)
        if (member.expression() instanceof IdentifierTree receiver) {
            String receiverName = receiver.name().toLowerCase();
            return receiverName.contains("log") || receiverName.equals("logger");
        }
        return false;
    }

    /**
     * Prüft ob ein Ausdruck ein Getter-Aufruf auf einem @PiiField-Feld ist.
     *
     * Erkennt: customer.getEmail() wenn email mit @PiiField annotiert ist.
     */
    private boolean isGetterOnPiiField(ExpressionTree expression) {
        if (!(expression instanceof MethodInvocationTree getter)) {
            return false;
        }
        if (!(getter.methodSelect() instanceof MemberSelectExpressionTree member)) {
            return false;
        }

        // Methodenname muss Getter-Konvention folgen: getXxx()
        String methodName = member.identifier().name();
        if (!methodName.startsWith("get") || methodName.length() <= 3) {
            return false;
        }

        // Feldname ableiten: getEmail → email
        String fieldName = Character.toLowerCase(methodName.charAt(3))
            + methodName.substring(4);

        // Symbol des Getter-Aufrufs auflösen
        Symbol methodSymbol = getter.methodSymbol();
        if (methodSymbol == null || methodSymbol.isUnknown()) {
            return false; // Keine semantischen Infos → überspringen
        }

        // Besitzer-Typ des Getters finden
        Symbol.TypeSymbol ownerType = methodSymbol.enclosingClass();
        if (ownerType == null) {
            return false;
        }

        // Alle Member-Symbole des Besitzer-Typs durchsuchen
        // und prüfen ob das korrespondierende Feld @PiiField hat
        return ownerType.memberSymbols().stream()
            .filter(Symbol::isVariableSymbol)
            .filter(field -> field.name().equals(fieldName))
            .anyMatch(field ->
                PII_ANNOTATION_NAMES.stream()
                    .anyMatch(annName -> field.metadata().isAnnotatedWith(annName))
            );
    }
}
```

### 8.6 Regel 3: Geldbeträge nicht als double

```java
package com.example.sonar.rules;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * REGEL: Variablen und Methoden mit Geld-Bezeichnern dürfen nicht
 *        den Typ double oder float verwenden.
 *
 * WARUM:
 *   double und float sind binäre Gleitkommazahlen (IEEE 754).
 *   Sie können dezimale Brüche wie 0.1 nicht exakt darstellen.
 *   0.1 + 0.2 = 0.30000000000000004 (kein Scherz!)
 *
 *   Bei Finanzberechnungen akkumulieren sich diese Fehler:
 *     double price = 19.99;
 *     double quantity = 3;
 *     double total = price * quantity;
 *     // Erwartet: 59.97
 *     // Tatsächlich: 59.96999999999999
 *
 *   Bei 10.000 Transaktionen: echte Buchhaltungsfehler,
 *   Differenzen in Jahresabschlüssen, rechtliche Risiken.
 *
 * LÖSUNG:
 *   long  → Betrag in Cent (19.99 EUR = 1999 long)
 *   BigDecimal → exakte Dezimalzahl für Berechnungen
 *
 * ❌ SCHLECHT:
 *   double totalPrice;      // → 59.96999999999999 statt 59.97
 *   float  discountAmount;  // → Präzisionsverlust
 *
 * ✅ GUT:
 *   long      totalCents;   // 5997 (= 59.97 EUR)
 *   BigDecimal discountAmount; // exakte Dezimalzahl
 */
@Rule(
    key         = "COMPANY003",
    name        = "Monetary amounts must use long (cents) or BigDecimal, not double/float",
    description = "The types 'double' and 'float' cannot represent decimal fractions "
                + "exactly (IEEE 754 binary floating point). For monetary values, "
                + "this causes rounding errors that accumulate over many transactions. "
                + "Use 'long' to store amounts in the smallest currency unit (cents), "
                + "or use 'BigDecimal' for exact decimal arithmetic.",
    tags        = {"finance", "correctness", "money", "precision"}
)
public class MoneyAsDoubleRule extends IssuableSubscriptionVisitor {

    // Schlüsselwörter im Variablennamen die auf Geldbeträge hinweisen
    // Pattern matcht case-insensitiv
    private static final Pattern MONEY_NAME = Pattern.compile(
        "(?i)(price|amount|cost|fee|total|subtotal|tax|discount|"
        + "balance|payment|charge|rate|sum|revenue|profit|margin|"
        + "wage|salary|budget|expense|invoice|tariff)",
        Pattern.CASE_INSENSITIVE
    );

    // Typen die NICHT erlaubt sind für Geld-Variablen
    private static final Set<String> FORBIDDEN_TYPES = Set.of(
        "double", "float", "Double", "Float"
    );

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return List.of(Tree.Kind.VARIABLE, Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        if (tree instanceof VariableTree variable) {
            checkVariable(variable);
        } else if (tree instanceof MethodTree method) {
            checkMethodReturnType(method);
        }
    }

    private void checkVariable(VariableTree variable) {
        String name = variable.simpleName().name();

        if (!MONEY_NAME.matcher(name).find()) {
            return; // Name deutet nicht auf Geld hin
        }

        String typeName = variable.type().toString();
        if (FORBIDDEN_TYPES.contains(typeName)) {
            reportIssue(variable.type(),
                "Variable '" + name + "' appears to represent a monetary amount "
                + "but uses '" + typeName + "' which has precision errors. "
                + "Use 'long' (store as cents, e.g. long priceInCents = 1999) "
                + "or 'BigDecimal' (e.g. BigDecimal price = new BigDecimal(\"19.99\")).");
        }
    }

    private void checkMethodReturnType(MethodTree method) {
        if (method.returnType() == null) {
            return; // void oder Konstruktor
        }

        String name = method.simpleName().name();
        if (!MONEY_NAME.matcher(name).find()) {
            return;
        }

        String returnType = method.returnType().toString();
        if (FORBIDDEN_TYPES.contains(returnType)) {
            reportIssue(method.returnType(),
                "Method '" + name + "' appears to return a monetary amount "
                + "but has return type '" + returnType + "'. "
                + "Return 'long' (cents) or 'BigDecimal' instead.");
        }
    }
}
```

### 8.7 Testdateien für die Regeln schreiben

```java
// src/test/java/com/example/sonar/rules/NoTransactionalOnPrivateRuleTest.java
package com.example.sonar.rules;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

class NoTransactionalOnPrivateRuleTest {

    /**
     * JavaCheckVerifier analysiert eine Java-Testdatei mit der Regel.
     * Kommentare in der Testdatei beschreiben was erwartet wird:
     *   // Noncompliant  → hier MUSS ein Issue gemeldet werden
     *   // Compliant     → hier darf KEIN Issue gemeldet werden (optional, nur Doku)
     */
    @Test
    void private_transactional_is_reported() {
        JavaCheckVerifier.newVerifier()
            .onFile("src/test/files/TransactionalPrivate.java")
            .withCheck(new NoTransactionalOnPrivateRule())
            .verifyIssues();  // Erwartet: Issues an den Noncompliant-Zeilen
    }

    @Test
    void public_transactional_is_not_reported() {
        JavaCheckVerifier.newVerifier()
            .onFile("src/test/files/TransactionalPublic.java")
            .withCheck(new NoTransactionalOnPrivateRule())
            .verifyNoIssues(); // Erwartet: kein einziges Issue
    }
}
```

```java
// src/test/files/TransactionalPrivate.java
// Testdatei: SOLLTE Issues erzeugen

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Transactional                                                    // Noncompliant
    private void saveInternal(Object order) { }

    @Transactional                                                    // Noncompliant
    private Object findByIdInternal(Long id) { return null; }

    // Compliant: public Methoden sind OK
    @Transactional
    public void save(Object order) { }

    // Compliant: protected ist AUCH OK (wird von Sub-Proxies interceptet)
    @Transactional
    protected void audit(Object order) { }

    // Compliant: private OHNE @Transactional ist selbstverständlich OK
    private void helperMethod() { }
}
```

```java
// src/test/files/TransactionalPublic.java
// Testdatei: darf KEINE Issues erzeugen

import org.springframework.transaction.annotation.Transactional;

public class PaymentService {

    @Transactional
    public void processPayment(Object payment) { }

    @Transactional
    protected void auditPayment(Object payment) { }

    // Package-private (kein Modifier) — Spring AOP interceptet das
    @Transactional
    void packagePrivateMethod() { }

    // Private ohne Annotation — kein Problem
    private void validate(Object payment) { }
}
```

### 8.8 Plugin bauen und deployen

```bash
# ── BAUEN ──────────────────────────────────────────────────────────────────

# Tests ausführen (PFLICHT vor Deployment)
mvn test

# Plugin-JAR bauen
mvn clean package

# Erzeugt: target/sonar-custom-java-rules-1.0.0.jar
# Das JAR enthält:
#   - Kompilierte Klassen
#   - MANIFEST.MF mit Plugin-Metadaten (pluginKey, pluginClass, basePlugin)
#   - Keine provided-Abhängigkeiten (sonar-plugin-api, java-frontend)

# ── DEPLOYEN auf SonarQube-Server ──────────────────────────────────────────

# Option A: Direktes Kopieren (Self-Hosted Linux)
cp target/sonar-custom-java-rules-1.0.0.jar \
   /opt/sonarqube/extensions/plugins/

# SonarQube MUSS neugestartet werden!
# Plugins werden nur beim Start geladen.
systemctl restart sonarqube
# Warte bis Web-UI wieder erreichbar ist (~30 Sekunden)

# Option B: Docker-Container
docker cp target/sonar-custom-java-rules-1.0.0.jar \
    sonarqube:/opt/sonarqube/extensions/plugins/
docker restart sonarqube

# Option C: Kubernetes (Persistent Volume für Extensions)
kubectl cp target/sonar-custom-java-rules-1.0.0.jar \
    sonarqube-pod:/opt/sonarqube/extensions/plugins/
kubectl rollout restart deployment/sonarqube

# ── VERIFIKATION ────────────────────────────────────────────────────────────

# In SonarQube UI:
#   Administration → Marketplace → Installed
#   → "Company Java Rules" erscheint

# Regeln prüfen:
#   Rules → Repository: "Company Java Rules"
#   → COMPANY001, COMPANY002, COMPANY003 erscheinen

# ── REGELN AKTIVIEREN ───────────────────────────────────────────────────────

# Quality Profile → [Dein Profil] → Activate More Rules
# → Repository: "Company Java Rules" filtern
# → Alle Regeln aktivieren (und gewünschte Severity setzen)

# Nach Aktivierung: nächste Analyse wendet die Regeln an
mvn sonar:sonar -Dsonar.host.url=... -Dsonar.token=...
```

---

## 9. SonarQube in der CI/CD-Pipeline

### 9.1 Maven-Integration

```xml
<!-- pom.xml des analysierten Projekts (NICHT des Plugin-Projekts!) -->
<properties>
    <!-- Projekt-Key: eindeutig, unveränderlich nach erstem Scan -->
    <sonar.projectKey>com.example:order-service</sonar.projectKey>
    <sonar.projectName>Order Service</sonar.projectName>

    <!-- Coverage-Report: JaCoCo XML muss VOR dem Sonar-Scan erzeugt werden -->
    <sonar.coverage.jacoco.xmlReportPaths>
        ${project.build.directory}/site/jacoco/jacoco.xml
    </sonar.coverage.jacoco.xmlReportPaths>

    <!--
        Ausschlüsse: was soll Sonar NICHT analysieren?
        Kommaseparierte Ant-Patterns.
    -->
    <sonar.exclusions>
        **/generated/**,
        **/target/generated-sources/**,
        **/*Application.java,
        **/migration/**
    </sonar.exclusions>

    <!--
        Coverage-Ausschlüsse: für was soll Coverage NICHT berechnet werden?
        (beeinflusst den Quality Gate Coverage-Check)
    -->
    <sonar.coverage.exclusions>
        **/config/**,
        **/*Exception.java,
        **/dto/**,
        **/*Mapper.java
    </sonar.coverage.exclusions>

    <!--
        Duplikations-Ausschlüsse: Wo ist Duplikation akzeptabel?
        (z.B. Migrations-Skripte, generierte DTOs)
    -->
    <sonar.cpd.exclusions>
        **/dto/**,
        **/model/**
    </sonar.cpd.exclusions>
</properties>

<build>
    <plugins>
        <!-- JaCoCo: Coverage messen -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>   <!-- Vor Tests -->
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>           <!-- Nach Tests: XML erzeugen -->
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 9.2 GitLab CI Integration

```yaml
# .gitlab-ci.yml — SonarQube vollständig integriert

variables:
  SONAR_HOST_URL: "https://sonar.example.com"
  # $SONAR_TOKEN: Protected + Masked Variable in GitLab UI Settings!
  # NIEMALS hier im YAML!

stages:
  - test
  - quality

# ── Schritt 1: Tests mit Coverage ─────────────────────────────────────────
test:unit:
  stage: test
  image: eclipse-temurin:21-jdk
  script:
    - mvn verify --no-transfer-progress
    # mvn verify: compile + test + jacoco:report
    # Erzeugt: target/site/jacoco/jacoco.xml
  artifacts:
    when: always
    reports:
      junit: target/surefire-reports/*.xml
    paths:
      - target/site/jacoco/
      - target/classes/           # Benötigt von Sonar für Bytecode-Analyse!
      - target/surefire-reports/
    expire_in: 1 day

# ── Schritt 2: SonarQube-Analyse ──────────────────────────────────────────
quality:sonarqube:
  stage: quality
  image: eclipse-temurin:21-jdk
  needs:
    - job: test:unit
      artifacts: true  # Artifacts (coverage, classes) von vorherigem Job!

  script:
    - mvn sonar:sonar
        --no-transfer-progress
        -Dsonar.projectKey=order-service
        -Dsonar.host.url=$SONAR_HOST_URL
        -Dsonar.token=$SONAR_TOKEN
        -Dsonar.qualitygate.wait=true
        # qualitygate.wait=true: Job wartet auf Quality Gate Entscheidung!
        # Ohne das: Job endet sofort, CI weiß nicht ob Gate passed/failed.
        -Dsonar.qualitygate.timeout=300  # Max 5 Minuten warten

  allow_failure: false  # Quality Gate Fail = CI Fail. Niemals allow_failure: true!

  rules:
    # Bei jedem Push auf main: vollständige Analyse
    - if: '$CI_COMMIT_BRANCH == "main"'
    # Bei Merge Requests: PR-Decoration (Developer Edition)
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
```

---

## 10. Was SonarQube NICHT schützt

```
╔══════════════════════════════════════════════════════════════════════════╗
║         WAS SONARQUBE NICHT ERKENNT — VOLLSTÄNDIGE ÜBERSICHT           ║
╠══════════════════════════════════════════════════════════════════════════╣
║                                                                          ║
║  LAUFZEIT-VERHALTEN (statische Analyse sieht nur Code, nicht Ausführung)║
║  ─────────────────────────────────────────────────────────────────────  ║
║  ❌ Race Conditions                                                      ║
║     synchronized(a) { synchronized(b) { ... }}  // Thread A             ║
║     synchronized(b) { synchronized(a) { ... }}  // Thread B             ║
║     Deadlock-Risiko — aber nur zur Laufzeit erkennbar.                   ║
║     Sonar sieht: zwei synchronized-Blöcke. Kein definiertes Issue.      ║
║     Lösung: ThreadSanitizer, Java Flight Recorder (→ ADR-117)           ║
║                                                                          ║
║  ❌ Memory Leaks durch zirkuläre Referenzen                              ║
║     Objektgraph zur Laufzeit nicht vorhersehbar.                         ║
║     Lösung: Eclipse Memory Analyzer (MAT), VisualVM, JProfiler          ║
║                                                                          ║
║  ❌ Concurrency-Bugs (Lost Update, Dirty Read)                           ║
║     Tritt nur bei gleichzeitiger Ausführung auf.                         ║
║     Lösung: Integrations-Tests mit parallelen Threads, jcstress          ║
║                                                                          ║
║  ❌ Performance-Probleme durch schlechte Algorithmen                     ║
║     O(n²) ist syntaktisch identisch zu O(n log n).                       ║
║     Sonar kennt nicht die Datenmenge zur Laufzeit.                       ║
║     Lösung: Profiling mit JFR, Async-Profiler (→ ADR-117)               ║
║                                                                          ║
║  ❌ Infinite Loops durch externe Bedingungen                             ║
║     while (!queue.isEmpty())  // Wenn queue nie leer wird...             ║
║     Sonar: kein Problem. Laufzeit: endloser Hang.                        ║
║                                                                          ║
║  BEKANNTE SCHWACHSTELLEN IN BIBLIOTHEKEN (SCA-Bereich)                  ║
║  ─────────────────────────────────────────────────────────────────────  ║
║  ❌ Log4Shell (CVE-2021-44228)                                           ║
║     Sicherheitslücke in log4j-core 2.14 und früher.                     ║
║     SonarQube analysiert deinen Code, nicht den Log4j-Code.             ║
║                                                                          ║
║  ❌ Spring4Shell (CVE-2022-22965)                                        ║
║  ❌ Jackson-Deserialisierungs-CVEs                                       ║
║  ❌ Jackson-Databind-Polymorph-Angriffe                                  ║
║  ❌ Struts2-RCE, Hibernate-Vulnerabilities                               ║
║     → Für alle: OWASP Dependency Check oder Snyk verwenden (→ ADR-057)  ║
║                                                                          ║
║  INFRASTRUKTUR & KONFIGURATION                                           ║
║  ─────────────────────────────────────────────────────────────────────  ║
║  ❌ Fehlende HTTP-Security-Header                                        ║
║     HSTS, CSP, X-Frame-Options in application.yml, nicht im Code.       ║
║     Lösung: DAST (ZAP), securityheaders.com (→ ADR-118)                 ║
║                                                                          ║
║  ❌ Kubernetes/Docker-Sicherheitsprobleme                                ║
║     Privilegierte Container, fehlende Network Policies,                  ║
║     zu weite RBAC-Berechtigungen.                                        ║
║     Lösung: Trivy, Checkov, OPA/Rego                                     ║
║                                                                          ║
║  ❌ Terraform/Cloud-Fehlkonfigurationen                                  ║
║     Öffentliche S3-Buckets, zu weite IAM-Rollen.                        ║
║     Lösung: tfsec, checkov, AWS Security Hub                            ║
║                                                                          ║
║  ❌ SSL/TLS-Protokoll-Konfiguration                                      ║
║     TLS 1.0/1.1 erlaubt, schwache Cipher Suites.                        ║
║     Steht in Deployment-Config, nicht im Quellcode.                     ║
║                                                                          ║
║  BUSINESS-LOGIK-FEHLER                                                   ║
║  ─────────────────────────────────────────────────────────────────────  ║
║  ❌ Falsche Berechnungslogik die syntaktisch korrekt ist                 ║
║     if (discount > 100) { applyDiscount(); }                             ║
║     // Sollte sein: if (discount > 0 && discount <= 100)                 ║
║     Sonar: gültiges Java, kein Fehler.                                   ║
║     Lösung: Unit-Tests, Property-Based-Tests (→ ADR-046)                ║
║                                                                          ║
║  ❌ Fehlende fachliche Validierungen                                     ║
║     "Wir hätten IBAN-Format prüfen sollen."                              ║
║     Sonar weiß nicht was fachlich korrekt ist.                           ║
║     Lösung: Anforderungs-Reviews, BDD-Tests                              ║
║                                                                          ║
║  ❌ Autorisierungs-Logik-Fehler ("User A sieht Daten von User B")       ║
║     if (userId.equals(order.getCustomerId()))  // Falsche ID-Prüfung     ║
║     Syntaktisch korrekt, semantisch falsch für diesen Kontext.           ║
║     Lösung: Security-Tests, Penetration-Test                            ║
║                                                                          ║
║  ❌ Fehlende Rate-Limiting / Brute-Force-Schutz                         ║
║     Kein Code der "fehlt" — die Abwesenheit von Code.                    ║
║     Lösung: Security-Requirements, DAST                                  ║
║                                                                          ║
║  PROTOKOLL & INTEGRATIONS-FEHLER                                         ║
║  ─────────────────────────────────────────────────────────────────────  ║
║  ❌ OAuth2-Flow-Fehler                                                   ║
║     Code kann OAuth2-Flows implementieren die funktionieren              ║
║     aber unsichere Varianten nutzen (Implicit Flow, kein PKCE).          ║
║     Lösung: Security-Code-Review, Auth0/Keycloak-Best-Practices          ║
║                                                                          ║
║  ❌ JWT-Algorithm-Confusion-Angriffe                                     ║
║     alg: "none" akzeptieren, HS256 statt RS256, fehlende Validierung.   ║
║     Zu kontextspezifisch für statische Analyse.                          ║
║     Lösung: JWT-Security-Tests, spezialisierte Tools                     ║
║                                                                          ║
║  ❌ DNS-Rebinding, Timing-Angriffe, Cache-Poisoning                     ║
║     Laufzeit-Angriffsvektoren, statisch nicht erkennbar.                 ║
║     Lösung: DAST, Penetration-Tests                                      ║
╚══════════════════════════════════════════════════════════════════════════╝
```

### 10.1 Das vollständige Security-Ökosystem

```
SCHICHT 1 — Statisch / Code-Ebene (bei jedem Commit):
  SonarQube SAST          → eigener Code: SQL-Injection, schwache Krypto
  OWASP Dependency Check  → Libraries: bekannte CVEs
  Trivy (Container Scan)  → Docker-Images: OS-CVEs, Misconfig

SCHICHT 2 — Dynamisch / Laufzeit (nach Deployment in Staging):
  OWASP ZAP               → laufende App: XSS, Auth-Fehler, Header
  Burp Suite              → manuelle Security-Tests

SCHICHT 3 — Infrastruktur (bei jeder Infra-Änderung):
  tfsec / Checkov         → Terraform: IAM, Netzwerk, Storage
  OPA / Kyverno           → Kubernetes: RBAC, Pod-Security

SCHICHT 4 — Periodisch (vor Major-Releases):
  Penetration Test        → manuell durch Security-Experten
  Threat Modeling         → Architektur-Review

SONARQUBE IST SCHICHT 1 VON 4.
Ohne die anderen Schichten hat man kein vollständiges Sicherheitsbild.
```

---
 

## Quellen & Referenzen

- **SonarSource, "Writing Custom Java Rules" (2024)** — offizielle Anleitung für Plugin-Entwicklung. docs.sonarqube.org/latest/extension-guide/adding-coding-rules/
- **SonarSource, "Java Checks Source Code" (GitHub)** — beste Lernressource: echte SonarQube-Regeln als Beispiele. github.com/SonarSource/sonar-java/tree/master/java-checks
- **OWASP, "Static Code Analysis" (2023)** — Einordnung von SAST im Security-Testing-Ökosystem. owasp.org/www-community/controls/Static_Code_Analysis
- **CWE (Common Weakness Enumeration)** — Basis-Taxonomie vieler SonarQube-Regeln. cwe.mitre.org
- **NIST, "SAST Tools" (SP 800-218)** — NIST-Empfehlungen für statische Analyse im SSDF.
- **Freddy Mallet & Simon Brandhof, "Continuous Inspection" (2009)** — Konzept-Artikel der SonarQube-Gründer. SonarSource Blog.
