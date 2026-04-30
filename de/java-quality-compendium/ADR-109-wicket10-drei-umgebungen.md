# ADR-109 — Apache Wicket 10: Drei-Umgebungen-Konfiguration (dev / test / prod) ohne Spring Boot

| Feld              | Wert                                                              |
|-------------------|-------------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                     |
| Entscheider       | Architektur-Board                                                 |
| Datum             | 2024-01-01                                                        |
| Review-Datum      | 2025-01-01                                                        |
| Kategorie         | Apache Wicket 10 · Servlet · Umgebungskonfiguration               |
| Betroffene Teams  | Alle Entwickler des Wicket-Projekts                               |
| Stack             | Apache Wicket 10.x · Java 21 · Maven 3.9 · Servlet 6.0 · Tomcat 10 / Jetty 12 |

---

## 1. Kontext & Treiber

### 1.1 Was Wicket von anderen Web-Frameworks unterscheidet

Wicket ist ein **Servlet-basiertes** Framework. Es läuft als `WicketFilter`
innerhalb eines Servlet-Containers (Tomcat, Jetty, Undertow) und kennt kein
eigenes Dependency-Injection-Framework. Die Umgebungskonfiguration muss deshalb
vollständig **ohne Spring, CDI oder andere DI-Container** gelöst werden.

Das erzeugt eine spezifische Herausforderung: Wicket selbst unterscheidet zwei
grundlegend verschiedene Betriebsmodi, und die Umgebungserkennung liegt komplett
in der Hand des Entwicklers.

### 1.2 Wickets eigene Umgebungslogik: RuntimeConfigurationType

```
WICKET RuntimeConfigurationType.DEVELOPMENT:
  → Markup-Dateien (.html) werden bei JEDEM Request neu von Disk geladen
    → HTML-Änderung sofort sichtbar, kein Neustart nötig
  → Java-Klassen werden per HotSwap-Mechanismus neu geladen (kein Neustart)
  → DebugBar erscheint automatisch unten auf jeder Seite:
    - Komponentenbaum anklickbar
    - Session-Inhalt sichtbar
    - Request-Dauer und Hibernate-Queries anzeigbar
  → Vollständiger Stack-Trace + Quellcode-Referenz IM BROWSER bei Exception
  → Zusätzliche Assertions prüfen Wicket-Komponentenregeln zur Laufzeit
  → AJAX-Debug-Modus: jede AJAX-Anfrage wird ausführlich protokolliert
  → Performance: 3–5× langsamer als DEPLOYMENT (kein Markup-Cache)

WICKET RuntimeConfigurationType.DEPLOYMENT:
  → Alle Ressourcen gecacht: Markup, Properties, Bilder, CSS, JS
  → Kein automatisches Neuladen — Änderungen erfordern Neustart
  → Fehlerseite zeigt neutrales "An internal error occurred" (kein Stack-Trace!)
  → Interne Paketstruktur nicht sichtbar
  → Maximale Performance durch vollständiges Caching
  → DebugBar ist nicht gerendert
```

**Der kritische Unterschied zu anderen Konfigurationsunterschieden:** Das ist
kein Feature-Flag. DEVELOPMENT-Modus in Produktion bedeutet Sicherheitslücke
(Stack-Traces mit Pfaden und Klassenstruktur sichtbar) und Performanceverlust
um Faktor 3–5. DEPLOYMENT-Modus in Entwicklung bedeutet: kein Hot-Reload,
keine DebugBar — halbe Produktivität.

### 1.3 Das Kernproblem ohne explizite Strategie

```
SZENARIO A: Vergessen den Modus zu setzen (häufigstes Problem)
  Entwickler deployt in Produktion.
  Niemand hat RuntimeConfigurationType.DEPLOYMENT explizit gesetzt.
  Wicket fällt auf DEVELOPMENT zurück (Standard wenn web.xml-Parameter fehlt).
  → Stack-Traces im Browser für jeden Nutzer sichtbar.
  → Interner Klassenpfad lesbar: "at com.example.order.service.OrderService:42"
  → Performance: 800ms statt 200ms.
  → Sicherheitsaudit findet das sofort.

SZENARIO B: Falscher Modus in Entwicklung
  Neuer Entwickler klont das Repository.
  Build-System setzt DEPLOYMENT-Modus (weil es "sicher" klingt).
  Entwickler bearbeitet Template-HTML → kein Effekt.
  Entwickler wundert sich, startet 20x neu.
  → 30 Minuten Verlust pro Tag.

SZENARIO C: Keine Umgebungstrennung für DB-URLs
  Entwickler-DB-URL ist in einer einzigen Properties-Datei.
  Falsches Deployment → Testdaten in Produktionsdatenbank.
  → Datenverlust oder Inkonsistenz.

DIE LÖSUNG:
  Eine einzige, verlässliche Mechanismus-Kette:
  Umgebungsvariable → Properties-Datei → WicketApplication
  Kein manuelles Eingreifen, kein Vergessen möglich.
```

---

## 2. Entscheidung

Die Umgebung wird über eine **`WICKET_ENV`-Umgebungsvariable** gesteuert
(Betriebssystem-Level, gesetzt durch Deployment-Prozess). Die `WicketApplication`
liest diese Variable beim Start und leitet daraus **automatisch und deterministisch**
`RuntimeConfigurationType` und alle umgebungs-spezifischen Konfigurationen ab.

```
WICKET_ENV Wert │ RuntimeConfigurationType │ Properties-Datei
────────────────┼──────────────────────────┼──────────────────────────────
dev             │ DEVELOPMENT              │ config/dev.properties
test            │ DEPLOYMENT               │ config/test.properties
prod            │ DEPLOYMENT               │ config/prod.properties
(nicht gesetzt) │ DEPLOYMENT               │ config/prod.properties  ← sicherer Default
```

**Sicherer Default:** Wenn `WICKET_ENV` nicht gesetzt ist, läuft die Anwendung
im DEPLOYMENT-Modus. Das ist die sichere Richtung — lieber kein Hot-Reload in
Entwicklung als Stack-Traces in Produktion.

---

## 3. Projektstruktur

```
mein-wicket-projekt/
├── pom.xml                                 ← Maven-Build (einzige Build-Datei)
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── MyApplication.java          ← Wicket-Hauptklasse
│   │   │   ├── config/
│   │   │   │   ├── Environment.java        ← Enum: DEV / TEST / PROD
│   │   │   │   ├── AppConfig.java          ← Konfigurations-Aggregator
│   │   │   │   └── EnvironmentGuard.java   ← Startup-Sicherheitsprüfung
│   │   │   ├── pages/
│   │   │   │   ├── BasePage.java
│   │   │   │   ├── BasePage.html           ← Liegt NEBEN der Java-Klasse!
│   │   │   │   ├── HomePage.java
│   │   │   │   ├── HomePage.html           ← Wicket-Konvention: HTML == Java
│   │   │   │   └── ProductionErrorPage.java
│   │   │   └── components/
│   │   │       └── DevInfoPanel.java
│   │   ├── resources/
│   │   │   ├── config/
│   │   │   │   ├── dev.properties          ← Lokale Entwicklung
│   │   │   │   ├── test.properties         ← Test / CI-Server
│   │   │   │   └── prod.properties         ← Produktion (nur Schlüssel)
│   │   │   └── logback.xml                 ← Logging-Konfiguration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml                 ← WicketFilter-Konfiguration
│   │       └── static/                     ← CSS, JS, Bilder (direkt zugänglich)
│   └── test/
│       ├── java/com/example/
│       │   ├── BaseWicketTest.java          ← Basis aller Wicket-Tests
│       │   └── pages/
│       │       └── HomePageTest.java
│       └── resources/
│           └── logback-test.xml            ← Separates Logging für Tests
└── README.md

WICHTIGE MAVEN-WICKET-BESONDERHEIT:
  HTML-Templates liegen NEBEN den Java-Klassen (in src/main/java/!).
  Maven-Ressourcen-Plugin muss so konfiguriert sein dass *.html
  aus src/main/java/ kopiert werden:

  <build>
    <resources>
      <resource>
        <directory>src/main/java</directory>
        <includes>
          <include>**/*.html</include>
          <include>**/*.properties</include>
          <include>**/*.xml</include>
        </includes>
      </resource>
      <resource>
        <directory>src/main/resources</directory>
      </resource>
    </resources>
  </build>
```

---

## 4. Dependencies (Maven)

```xml
<!-- pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>mein-wicket-projekt</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>war</packaging>   <!-- WAR für Servlet-Container-Deployment -->

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Zentrale Versionsverwaltung: eine Stelle ändern, überall wirksam -->
        <wicket.version>10.1.0</wicket.version>
        <slf4j.version>2.0.11</slf4j.version>
        <logback.version>1.5.3</logback.version>
        <hikaricp.version>5.1.0</hikaricp.version>
        <postgresql.version>42.7.1</postgresql.version>
        <junit.version>5.10.2</junit.version>
        <assertj.version>3.25.3</assertj.version>
        <h2.version>2.2.224</h2.version>
    </properties>

    <dependencies>

        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- WICKET 10                                                      -->
        <!-- ══════════════════════════════════════════════════════════════ -->

        <!-- Kern: Komponentenmodell, Request-Lifecycle, Session -->
        <dependency>
            <groupId>org.apache.wicket</groupId>
            <artifactId>wicket-core</artifactId>
            <version>${wicket.version}</version>
        </dependency>

        <!-- Erweiterte Komponenten: DataTable, DatePicker, AjaxTabs etc. -->
        <dependency>
            <groupId>org.apache.wicket</groupId>
            <artifactId>wicket-extensions</artifactId>
            <version>${wicket.version}</version>
        </dependency>

        <!--
            Dev-Utilities: DebugBar, InspectorPage, PageView etc.

            WARUM als compile-Scope (nicht optional/provided)?
            MyApplication.java referenziert Klassen aus diesem Artefakt
            (z.B. DebugBar-Initialisierung in configureDevelopment()).
            Die Klassen müssen im Classpath sein, auch wenn DebugBar im
            DEPLOYMENT-Modus niemals gerendert wird.
            Overhead: ~150 KB JAR — vernachlässigbar.
        -->
        <dependency>
            <groupId>org.apache.wicket</groupId>
            <artifactId>wicket-devutils</artifactId>
            <version>${wicket.version}</version>
        </dependency>

        <!-- Rollen-basierte Authentifizierung (optional, empfohlen) -->
        <dependency>
            <groupId>org.apache.wicket</groupId>
            <artifactId>wicket-auth-roles</artifactId>
            <version>${wicket.version}</version>
        </dependency>

        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- SERVLET-API                                                    -->
        <!-- provided: Tomcat/Jetty/Undertow liefert die Implementierung   -->
        <!-- ══════════════════════════════════════════════════════════════ -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- DATENBANK                                                      -->
        <!-- ══════════════════════════════════════════════════════════════ -->

        <!-- HikariCP: schnellster JDBC Connection Pool für Java -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>${hikaricp.version}</version>
        </dependency>

        <!-- PostgreSQL JDBC-Treiber (runtime: wird zur Laufzeit benötigt) -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgresql.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- LOGGING                                                        -->
        <!-- ══════════════════════════════════════════════════════════════ -->

        <!-- SLF4J API: Logging-Abstraktionsschicht -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>

        <!-- Logback: SLF4J-Implementierung (runtime: keine Compile-Abhängigkeit) -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- TEST                                                           -->
        <!-- scope=test: landet NICHT im WAR, nur für mvn test             -->
        <!-- ══════════════════════════════════════════════════════════════ -->

        <!-- WicketTester: Unit-Test-Framework für Wicket-Seiten und Panels -->
        <dependency>
            <groupId>org.apache.wicket</groupId>
            <artifactId>wicket-tester</artifactId>
            <version>${wicket.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- AssertJ: fluente, lesbare Assertions -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>

        <!--
            H2 In-Memory-Datenbank für Tests.
            Ersetzt PostgreSQL im Test-Setup (TestApplication.initInfrastructure()).
            MODE=PostgreSQL: simuliert PostgreSQL-Verhalten soweit möglich.
        -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${h2.version}</version>
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
                    <compilerArgs>
                        <!-- Warnung wenn veraltete APIs genutzt werden -->
                        <arg>-Xlint:deprecation</arg>
                        <arg>-Xlint:unchecked</arg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <!--
                Ressourcen-Plugin: Wicket-HTML-Templates kopieren.

                WICKET-BESONDERHEIT: HTML-Templates liegen per Konvention
                NEBEN den Java-Klassen in src/main/java/ (nicht in resources/).
                Maven kopiert standardmäßig nur *.java aus src/main/java.
                Ohne diese Konfiguration fehlen die Templates im WAR → 500-Fehler!

                Beispiel:
                  src/main/java/com/example/pages/HomePage.java
                  src/main/java/com/example/pages/HomePage.html  ← MUSS kopiert werden!
            -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>
        </plugins>

        <resources>
            <!--
                Ressource 1: Wicket-Templates aus src/main/java kopieren.
                Alle nicht-Java-Dateien: HTML, Properties, XML, PNG etc.
            -->
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.html</include>
                    <include>**/*.properties</include>
                    <include>**/*.xml</include>
                    <include>**/*.png</include>
                    <include>**/*.gif</include>
                    <include>**/*.css</include>
                    <include>**/*.js</include>
                </includes>
            </resource>

            <!-- Ressource 2: Standard-Ressourcen aus src/main/resources -->
            <resource>
                <directory>src/main/resources</directory>
            </resource>
        </resources>

            <!-- WAR-Plugin: baut das deploybare Artefakt -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <!-- Kein web.xml-Pflichtfehler wenn Datei vorhanden ist -->
                    <failOnMissingWebXml>true</failOnMissingWebXml>
                    <!-- WAR-Name ohne Version: meinprojekt.war statt meinprojekt-1.0.0.war -->
                    <warName>meinprojekt</warName>
                </configuration>
            </plugin>

            <!-- Maven Surefire: JUnit 5 Tests ausführen -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <!-- Umgebungsvariable für Tests setzen: immer test-Umgebung -->
                    <environmentVariables>
                        <WICKET_ENV>test</WICKET_ENV>
                    </environmentVariables>
                    <!-- JUnit 5 Platform -->
                    <useModulePath>false</useModulePath>
                </configuration>
            </plugin>

            <!--
                Jetty-Plugin: lokaler Entwicklungsserver.
                mvn jetty:run  →  startet Jetty auf Port 8080.
                WICKET_ENV=dev muss als Umgebungsvariable gesetzt sein!
            -->
            <plugin>
                <groupId>org.eclipse.jetty</groupId>
                <artifactId>jetty-maven-plugin</artifactId>
                <version>11.0.20</version>
                <configuration>
                    <webApp>
                        <contextPath>/</contextPath>
                    </webApp>
                    <httpConnector>
                        <port>8080</port>
                    </httpConnector>
                    <!-- Hot-Reload: Klassen alle 2 Sekunden auf Änderungen prüfen -->
                    <scan>2</scan>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
```

### 4.1 Wichtige Maven-Befehle

```bash
# ── BAUEN ─────────────────────────────────────────────────────────────────

# WAR bauen (ohne Tests): schneller für CI
mvn package -DskipTests

# WAR bauen mit Tests
mvn package

# Nur kompilieren (kein WAR)
mvn compile

# ── TESTEN ────────────────────────────────────────────────────────────────

# Alle Tests ausführen
# WICKET_ENV=test wird automatisch durch Surefire-Konfiguration gesetzt
mvn test

# Einzelnen Test ausführen
mvn test -Dtest=HomePageTest

# ── LOKALE ENTWICKLUNG ────────────────────────────────────────────────────

# Jetty starten mit DEVELOPMENT-Modus
# WICHTIG: Umgebungsvariable VOR mvn setzen!
export WICKET_ENV=dev
mvn jetty:run

# Oder einzeilig (überschreibt nur für diesen Prozess):
WICKET_ENV=dev mvn jetty:run

# Jetty startet auf http://localhost:8080
# WICKET_ENV=dev → DEVELOPMENT-Modus → DebugBar sichtbar

# ── DEPLOYMENT ────────────────────────────────────────────────────────────

# WAR-Datei liegt nach mvn package in:
# target/meinprojekt.war
# → in Tomcat's webapps/ kopieren oder via CI deployen
```

### 4.2 Dependency-Baum prüfen

```bash
# Alle transitiven Abhängigkeiten anzeigen
mvn dependency:tree

# Auf Konflikte prüfen (zwei Versionen derselben Library)
mvn dependency:tree -Dverbose | grep "conflict"

# Unused / undeclared dependencies finden
mvn dependency:analyze
# "Used undeclared" → fehlt in pom.xml, aber im Classpath → explizit hinzufügen
# "Unused declared"  → in pom.xml, aber nicht genutzt → entfernen (außer runtime)
```

---

## 5. Umgebungs-Enum und Konfigurations-Aggregator

```java
package com.example.config;

/**
 * Die drei Umgebungen als typsicherer Enum.
 *
 * Warum Enum statt String-Vergleiche überall?
 * - Tippfehler werden zur Compile-Zeit erkannt, nicht zur Laufzeit
 * - IDE-Autovervollständigung
 * - switch-Expressions sind exhaustiv (Compiler warnt bei fehlendem Fall)
 */
public enum Environment {

    DEV("dev"),
    TEST("test"),
    PROD("prod");

    private final String key;

    Environment(String key) { this.key = key; }

    /**
     * Liest die WICKET_ENV-Umgebungsvariable und gibt den Enum zurück.
     *
     * SICHERER DEFAULT: Wenn nicht gesetzt oder unbekannt → PROD.
     * Lieber kein Hot-Reload in Entwicklung als Stack-Traces in Produktion.
     */
    public static Environment detect() {
        var env = System.getenv("WICKET_ENV");
        if (env == null || env.isBlank()) {
            // Nicht gesetzt → sicher: Produktion
            return PROD;
        }
        return switch (env.trim().toLowerCase()) {
            case "dev"  -> DEV;
            case "test" -> TEST;
            case "prod" -> PROD;
            default -> {
                // Unbekannter Wert → loggen und sicher auf PROD fallen
                System.err.println("WARN: Unbekannter WICKET_ENV-Wert '" + env
                    + "'. Verwende PROD als sicheren Default.");
                yield PROD;
            }
        };
    }

    public boolean isDev()  { return this == DEV;  }
    public boolean isTest() { return this == TEST; }
    public boolean isProd() { return this == PROD; }

    public String getKey() { return key; }
}
```

```java
package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lädt die umgebungs-spezifische Properties-Datei und stellt
 * Konfigurationswerte typsicher bereit.
 *
 * Warum Properties-Datei statt direkt Umgebungsvariablen?
 * - Viele Werte sind keine Secrets (DB-Host in Test ist kein Secret)
 * - Properties-Datei ist lesbar, kommentierbar, versionierbar
 * - Nur echte Secrets (Passwörter, API-Keys) kommen aus Umgebungsvariablen
 *
 * Das Pattern:
 *   Nicht-Secret-Wert → Properties-Datei
 *   Secret-Wert       → Umgebungsvariable (überschreibt Properties-Datei)
 */
public class AppConfig {

    private final Environment environment;
    private final Properties  properties;

    public AppConfig(Environment environment) {
        this.environment = environment;
        this.properties  = loadProperties(environment);
    }

    private Properties loadProperties(Environment env) {
        var filename = "config/" + env.getKey() + ".properties";
        var props    = new Properties();

        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(filename)) {

            if (is == null) {
                throw new IllegalStateException(
                    "Konfigurationsdatei nicht gefunden: " + filename
                    + "\nErwartet unter: src/main/resources/" + filename);
            }
            props.load(is);

        } catch (IOException e) {
            throw new IllegalStateException(
                "Fehler beim Lesen der Konfiguration: " + filename, e);
        }

        // Umgebungsvariablen überschreiben Properties (Secrets-Injection)
        // Konvention: DB_URL überschreibt db.url, DB_PASSWORD überschreibt db.password
        overrideWithEnvVars(props);

        return props;
    }

    /**
     * Umgebungsvariablen überschreiben Properties-Werte.
     *
     * Konvention: Env-Var-Name = Property-Schlüssel in GROSSBUCHSTABEN
     *             mit Punkt durch Unterstrich ersetzt.
     * Beispiel: db.password → DB_PASSWORD
     *           smtp.host   → SMTP_HOST
     *
     * Warum: Secrets sollen NICHT in Dateien stehen (auch nicht in prod.properties).
     * Deployment-Systeme (Kubernetes Secrets, Vault) setzen Umgebungsvariablen.
     */
    private void overrideWithEnvVars(Properties props) {
        props.stringPropertyNames().forEach(key -> {
            var envKey = key.toUpperCase().replace('.', '_');
            var envVal = System.getenv(envKey);
            if (envVal != null && !envVal.isBlank()) {
                props.setProperty(key, envVal);
            }
        });
    }

    // ── Getter mit typsicherer API ────────────────────────────────────

    public String  dbUrl()           { return require("db.url"); }
    public String  dbUser()          { return require("db.user"); }
    public String  dbPassword()      { return require("db.password"); }
    public int     dbPoolMaxSize()   { return parseInt("db.pool.maxSize", 10); }

    public String  appBaseUrl()      { return require("app.baseUrl"); }

    public boolean mailEnabled()     { return parseBoolean("mail.enabled", false); }
    public String  mailHost()        { return get("mail.host", "localhost"); }
    public int     mailPort()        { return parseInt("mail.port", 25); }
    public String  mailFrom()        { return get("mail.from", "noreply@localhost"); }
    public String  mailUser()        { return get("mail.user", ""); }
    public String  mailPassword()    { return get("mail.password", ""); }

    public Environment environment() { return environment; }

    // ── Interne Hilfsmethoden ─────────────────────────────────────────

    private String require(String key) {
        var value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Pflicht-Konfigurationswert fehlt: '" + key
                + "' in " + environment.getKey() + ".properties"
                + " oder Umgebungsvariable "
                + key.toUpperCase().replace('.', '_'));
        }
        return value.trim();
    }

    private String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }

    private int parseInt(String key, int defaultValue) {
        var value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private boolean parseBoolean(String key, boolean defaultValue) {
        var value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }
}
```

---

## 6. Properties-Dateien

### 6.1 config/dev.properties

```properties
# src/main/resources/config/dev.properties
# Lokale Entwicklungsumgebung
# Klartext-Credentials OK: lokale Docker-Datenbank, kein Produktionszugriff

db.url             = jdbc:postgresql://localhost:5432/meinprojekt_dev
db.user            = dev
db.password        = dev
db.pool.maxSize    = 5

app.baseUrl        = http://localhost:8080

# MailHog: lokaler SMTP-Catch-All (fängt alle E-Mails ab, keine echte Zustellung)
# Web-UI: http://localhost:8025
mail.enabled       = true
mail.host          = localhost
mail.port          = 1025
mail.from          = dev@localhost

# Logging: ausführlich in Entwicklung
log.level.root     = WARN
log.level.app      = DEBUG
```

### 6.2 config/test.properties

```properties
# src/main/resources/config/test.properties
# Test- / Staging-Umgebung (CI-Server, dedizierter Test-Server)
#
# WICHTIG: Sensitive Werte (db.password, mail.password) sind LEER.
# Sie MÜSSEN über Umgebungsvariablen gesetzt werden:
# DB_PASSWORD, MAIL_PASSWORD
# CI-System setzt diese als geschützte Variablen.

db.url             = ${DB_URL:jdbc:postgresql://test-db:5432/meinprojekt_test}
db.user            = ${DB_USER:testuser}
db.password        =
db.pool.maxSize    = 10

app.baseUrl        = https://test.meinprojekt.example.com

mail.enabled       = true
mail.host          =
mail.port          = 587
mail.from          = noreply@test.meinprojekt.example.com
mail.user          =
mail.password      =

log.level.root     = WARN
log.level.app      = INFO
```

### 6.3 config/prod.properties

```properties
# src/main/resources/config/prod.properties
# PRODUKTION
#
# Diese Datei enthält KEINE Secrets — sie ist im Git-Repository sichtbar.
# ALLE sensitiven Werte kommen aus Umgebungsvariablen (Vault, Kubernetes-Secrets).
# Leere Werte hier = Pflicht-Umgebungsvariable muss gesetzt sein.
#
# Konvention: leerer Wert = MUSS als Umgebungsvariable gesetzt sein
#             (AppConfig.require() wirft Exception bei fehlendem Wert)

db.url             =
db.user            =
db.password        =
db.pool.maxSize    = 20

app.baseUrl        = https://www.meinprojekt.example.com

mail.enabled       = true
mail.host          =
mail.port          = 587
mail.from          = noreply@meinprojekt.example.com
mail.user          =
mail.password      =

log.level.root     = WARN
log.level.app      = INFO
```

---

## 7. WicketApplication — die zentrale Konfigurationsklasse

### 7.1 ❌ Schlecht — drei Anti-Patterns

```java
// ❌ ANTI-PATTERN 1: RuntimeConfigurationType hardcodiert
public class MyApplication extends WebApplication {
    @Override
    public RuntimeConfigurationType getConfigurationType() {
        return RuntimeConfigurationType.DEVELOPMENT; // Vergessen auf DEPLOYMENT zu ändern!
    }
    // Bei Deployment in Produktion: Stack-Traces sichtbar, 3-5× langsamer.
    // Passiert unter Zeitdruck — garantiert.
}

// ❌ ANTI-PATTERN 2: web.xml-Parameter lesen (funktioniert nicht zuverlässig)
public class MyApplication extends WebApplication {
    @Override
    public RuntimeConfigurationType getConfigurationType() {
        // WicketFilter setzt diesen Parameter — aber nur wenn web.xml korrekt
        // konfiguriert ist. Fehlt der Parameter → DEVELOPMENT als Fallback.
        // Fehleranfällig, schwer zu testen.
        String param = getServletContext().getInitParameter("wicket.configuration");
        return "deployment".equals(param)
            ? RuntimeConfigurationType.DEPLOYMENT
            : RuntimeConfigurationType.DEVELOPMENT; // ← Unsicherer Default!
    }
}

// ❌ ANTI-PATTERN 3: System Property direkt in getConfigurationType()
public class MyApplication extends WebApplication {
    @Override
    public RuntimeConfigurationType getConfigurationType() {
        // System Properties können von außen geändert werden.
        // Kein typsicheres Handling, kein Logging welcher Wert erkannt wurde.
        String env = System.getProperty("app.env", "production");
        return "development".equals(env)
            ? RuntimeConfigurationType.DEVELOPMENT
            : RuntimeConfigurationType.DEPLOYMENT;
    }
    // Wo ist AppConfig? Wie werden Properties geladen? Alles fragmentiert.
}
```

### 7.2 ✅ Gut — vollständige, typsichere Konfiguration

```java
package com.example;

import com.example.config.AppConfig;
import com.example.config.Environment;
import com.example.config.EnvironmentGuard;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zentrale Wicket-Anwendungsklasse.
 *
 * Verantwortlichkeiten:
 * ① Umgebung erkennen (WICKET_ENV-Variable)
 * ② RuntimeConfigurationType aus Umgebung ableiten
 * ③ Umgebungs-spezifische Wicket-Einstellungen vornehmen
 * ④ Gemeinsame Konfiguration (URL-Mounting, Encoding)
 * ⑤ Startup-Guard: falsche Kombination → sofortiger Abbruch
 */
public class MyApplication extends WebApplication {

    private static final Logger log = LoggerFactory.getLogger(MyApplication.class);

    // Werden in init() gesetzt und danach unveränderlich
    private Environment environment;
    private AppConfig   config;

    @Override
    public Class<? extends org.apache.wicket.Page> getHomePage() {
        return HomePage.class;
    }

    // ── Wicket-Typ: EINZIGE Steuerungsquelle ist WICKET_ENV ──────────────
    @Override
    public RuntimeConfigurationType getConfigurationType() {
        // DEV → DEVELOPMENT. Alles andere (TEST, PROD, unbekannt) → DEPLOYMENT.
        // Diese Methode wird von Wicket wiederholt aufgerufen — kein Logging hier.
        return (environment != null && environment.isDev())
            ? RuntimeConfigurationType.DEVELOPMENT
            : RuntimeConfigurationType.DEPLOYMENT;
    }

    @Override
    protected void init() {
        super.init();

        // ① Umgebung erkennen
        environment = Environment.detect();
        config      = new AppConfig(environment);

        // ② Startup-Guard: inkonsistente Konfiguration sofort abbrechen
        EnvironmentGuard.verify(environment, this);

        // ③ Umgebungs-spezifisch konfigurieren
        switch (environment) {
            case DEV  -> configureDevelopment();
            case TEST -> configureTest();
            case PROD -> configureProduction();
        }

        // ④ Gemeinsame Konfiguration
        configureCommon();

        // ⑤ Infrastruktur initialisieren (DB-Pool, Mail etc.)
        initInfrastructure();
    }

    // ─────────────────────────────────────────────────────────────────────
    // DEV — Maximale Entwicklerproduktivität
    // ─────────────────────────────────────────────────────────────────────
    private void configureDevelopment() {
        // DebugBar: erscheint automatisch unten auf jeder Seite.
        // Zeigt: Komponentenbaum, Session-Inhalt, Request-Dauer, Hibernate-Queries.
        // Wird NUR gerendert wenn RuntimeConfigurationType = DEVELOPMENT.
        getDebugSettings().setDevelopmentUtilitiesEnabled(true);

        // Markup-Cache deaktivieren: .html-Dateien werden bei jedem Request
        // neu von Disk geladen. HTML-Änderung sofort sichtbar, kein Neustart.
        getMarkupSettings().setMarkupFactory(
            new org.apache.wicket.markup.MarkupFactory() {
                @Override
                public boolean isMarkupCacheEnabled() {
                    return false; // Kein Cache: jede Anfrage liest frisches HTML
                }
            });

        // Unminifiziertes JS/CSS: im Browser-DevTools lesbar und debuggbar.
        // In Produktion: minifizierte Versionen für Performance.
        getResourceSettings().setUseMinifiedResources(false);

        // Ressourcen alle 2 Sekunden auf Änderungen prüfen.
        // Bilder, CSS, JS-Änderungen werden ohne Neustart wirksam.
        getResourceSettings().setResourcePollFrequency(
            org.apache.wicket.util.time.Duration.seconds(2));

        // Vollständiger Stack-Trace mit Quellcode-Referenzen im Browser.
        // NUR in Development akzeptabel — exponiert interne Struktur!
        getExceptionSettings().setUnexpectedExceptionDisplay(
            org.apache.wicket.settings.ExceptionSettings.SHOW_EXCEPTION_PAGE);

        // Komponentenbaum-Assertions: prüft Wicket-Verwendungsregeln.
        // Hilft neuen Entwicklern, Wicket korrekt zu verwenden.
        getDebugSettings().setComponentUseCheck(true);

        // wicket:id als CSS-Klasse im HTML ausgeben.
        // Hilft bei der Zuordnung HTML ↔ Java-Klasse im DevTools.
        getDebugSettings().setOutputMarkupContainerClassName(true);

        log.warn("╔════════════════════════════════════════════════════════╗");
        log.warn("║  ⚠️  WICKET DEVELOPMENT-MODUS AKTIV (WICKET_ENV=dev)  ║");
        log.warn("║  Stack-Traces sichtbar! Nur für lokale Entwicklung!   ║");
        log.warn("╚════════════════════════════════════════════════════════╝");
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST — Wie Produktion, aber für CI/Staging angepasst
    // ─────────────────────────────────────────────────────────────────────
    private void configureTest() {
        // Keine DebugBar auf dem Staging-Server
        getDebugSettings().setDevelopmentUtilitiesEnabled(false);
        getDebugSettings().setComponentUseCheck(false);

        // Neutrale Fehlerseite — kein Stack-Trace, kein Pfad
        getExceptionSettings().setUnexpectedExceptionDisplay(
            org.apache.wicket.settings.ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

        // Markup-Cache aktiv (wie Produktion!)
        // Tests prüfen das Produktionsverhalten, nicht den Dev-Modus.
        getMarkupSettings().setStripWicketTags(true);

        log.info("Wicket DEPLOYMENT-Modus aktiv (WICKET_ENV=test)");
    }

    // ─────────────────────────────────────────────────────────────────────
    // PROD — Maximale Performance, kein internes Wissen nach außen
    // ─────────────────────────────────────────────────────────────────────
    private void configureProduction() {
        // Keine Debug-Utilities in Produktion
        getDebugSettings().setDevelopmentUtilitiesEnabled(false);
        getDebugSettings().setComponentUseCheck(false);

        // Eigene Fehlerseite mit Branding und Support-Kontakt.
        // ProductionErrorPage.java zeigt: "Es ist ein Fehler aufgetreten."
        // Keine Pfade, keine Stack-Traces, keine internen Details.
        getApplicationSettings().setInternalErrorPage(ProductionErrorPage.class);
        getExceptionSettings().setUnexpectedExceptionDisplay(
            org.apache.wicket.settings.ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

        // Minifiziertes JS/CSS: kleinere Dateien, schnellere Ladezeiten.
        // Wicket sucht *.min.js und *.min.css neben den normalen Dateien.
        getResourceSettings().setUseMinifiedResources(true);

        // Aggressives Caching für statische Ressourcen (1 Jahr).
        // Wicket nutzt URL-Fingerprinting: neue Version → neue URL.
        // Kein stale-Cache-Problem trotz langem Caching.
        getResourceSettings().setDefaultCacheDuration(
            org.apache.wicket.util.time.Duration.days(365));

        // wicket:id, wicket:panel, wicket:message aus HTML entfernen.
        // Browser-Quelltext zeigt kein Hinweis auf das Framework.
        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setStripXmlDeclarationFromOutput(true);
        getMarkupSettings().setCompressWhitespace(true);

        // Session-Größe begrenzen: verhindert Memory-Probleme bei vielen
        // gleichzeitigen Sitzungen (Wicket speichert Pages in der Session).
        getStoreSettings().setMaxSizePerSession(
            org.apache.wicket.util.lang.Bytes.megabytes(10));

        log.info("Wicket DEPLOYMENT-Modus aktiv (WICKET_ENV=prod)");
    }

    // ─────────────────────────────────────────────────────────────────────
    // GEMEINSAM — identisch in allen drei Umgebungen
    // ─────────────────────────────────────────────────────────────────────
    private void configureCommon() {
        // Lesbare URLs durch Mounting.
        // Ohne Mounting: ?wicket:bookmarkablePage=com.example.pages.HomePage
        // Mit Mounting:  /home, /bestellungen, /login
        mountPage("/home",                          HomePage.class);
        mountPage("/bestellungen",                  OrderListPage.class);
        mountPage("/bestellungen/${bestellNummer}", OrderDetailPage.class);
        mountPage("/profil",                        ProfilePage.class);
        mountPage("/login",                         LoginPage.class);
        mountPage("/admin",                         AdminPage.class);

        // Einheitliches Encoding
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
    }

    // ─────────────────────────────────────────────────────────────────────
    // INFRASTRUKTUR — DB-Pool, Mail etc. initialisieren
    // ─────────────────────────────────────────────────────────────────────
    private void initInfrastructure() {
        // HikariCP Connection Pool aus AppConfig konfigurieren
        var hikariConfig = new com.zaxxer.hikari.HikariConfig();
        hikariConfig.setJdbcUrl(config.dbUrl());
        hikariConfig.setUsername(config.dbUser());
        hikariConfig.setPassword(config.dbPassword());
        hikariConfig.setMaximumPoolSize(config.dbPoolMaxSize());
        hikariConfig.setPoolName("WicketHikariPool-" + environment.getKey());

        // DataSource als Application-Attribut speichern → für Pages zugänglich
        var dataSource = new com.zaxxer.hikari.HikariDataSource(hikariConfig);
        setMetaData(ApplicationKeys.DATA_SOURCE, dataSource);
        setMetaData(ApplicationKeys.APP_CONFIG, config);

        log.info("HikariCP Pool initialisiert: maxSize={}, url={}",
            config.dbPoolMaxSize(),
            // URL loggen aber Passwort niemals!
            config.dbUrl().replaceAll("password=[^&]+", "password=***"));
    }

    @Override
    protected void onDestroy() {
        // DataSource beim Shutdown sauber schließen
        var ds = getMetaData(ApplicationKeys.DATA_SOURCE);
        if (ds instanceof com.zaxxer.hikari.HikariDataSource hikari) {
            hikari.close();
            log.info("HikariCP Pool geschlossen.");
        }
        super.onDestroy();
    }

    // Typsichere Keys für Application.getMetaData()
    public static final class ApplicationKeys {
        public static final
            org.apache.wicket.MetaDataKey<javax.sql.DataSource> DATA_SOURCE =
                new org.apache.wicket.MetaDataKey<>() {};

        public static final
            org.apache.wicket.MetaDataKey<AppConfig> APP_CONFIG =
                new org.apache.wicket.MetaDataKey<>() {};
    }
}
```

---

## 8. EnvironmentGuard — Startup-Sicherheitsprüfung

```java
package com.example.config;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prüft beim Anwendungsstart ob Umgebung und Wicket-Konfiguration konsistent sind.
 *
 * Fail-Fast-Prinzip: Besser sofort scheitern mit klarer Fehlermeldung
 * als stumm falsch laufen und später einen Incident erzeugen.
 */
public class EnvironmentGuard {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentGuard.class);

    /**
     * Wirft IllegalStateException bei kritischer Fehlkonfiguration.
     * Gibt Warnung aus bei verdächtiger Konfiguration.
     * Loggt die aktive Konfiguration bei korrektem Setup.
     */
    public static void verify(Environment env, WebApplication app) {
        var wicketType = app.getConfigurationType();

        // ── KRITISCH: prod + DEVELOPMENT = sofortiger Abbruch ────────────
        if (env.isProd() && wicketType == RuntimeConfigurationType.DEVELOPMENT) {
            throw new IllegalStateException("""
                ╔══════════════════════════════════════════════════════════════╗
                ║  KRITISCHE FEHLKONFIGURATION — ANWENDUNG WIRD GESTOPPT      ║
                ╠══════════════════════════════════════════════════════════════╣
                ║  WICKET_ENV:  prod                                           ║
                ║  Wicket-Typ:  DEVELOPMENT  ←  FALSCH FÜR PRODUKTION!        ║
                ║                                                              ║
                ║  DEVELOPMENT-Modus in Produktion bedeutet:                  ║
                ║  → Vollständige Java-Stack-Traces im Browser sichtbar        ║
                ║  → Interne Paketstruktur exponiert (Security-Audit-Fail)     ║
                ║  → Performance 3–5× schlechter wegen fehlender Caches       ║
                ║                                                              ║
                ║  Ursache: MyApplication.getConfigurationType() gibt für      ║
                ║  PROD-Umgebung DEVELOPMENT zurück.                           ║
                ║  Prüfe die Implementierung von getConfigurationType().       ║
                ╚══════════════════════════════════════════════════════════════╝
                """);
        }

        // ── WARNUNG: test + DEVELOPMENT ──────────────────────────────────
        if (env.isTest() && wicketType == RuntimeConfigurationType.DEVELOPMENT) {
            log.warn("⚠️  WICKET_ENV=test, aber Wicket läuft im DEVELOPMENT-Modus. "
                + "Tests prüfen nicht das Produktionsverhalten. "
                + "Prüfe MyApplication.getConfigurationType().");
        }

        // ── INFO: aktive Konfiguration ────────────────────────────────────
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  Wicket-Anwendung gestartet                          ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  WICKET_ENV:   {}",
            System.getenv("WICKET_ENV") != null
                ? System.getenv("WICKET_ENV") : "(nicht gesetzt → PROD)");
        log.info("║  Environment:  {}", env.name());
        log.info("║  Wicket-Typ:   {}", wicketType);
        log.info("║  Modus:        {}", switch (env) {
            case DEV  -> "DEVELOPMENT 🛠️";
            case TEST -> "TEST        🧪";
            case PROD -> "PRODUCTION  🚀";
        });
        log.info("╚══════════════════════════════════════════════════════╝");
    }
}
```

---

## 9. web.xml — WicketFilter konfigurieren

```xml
<!-- src/main/webapp/WEB-INF/web.xml -->
<!-- Servlet 6.0 / Jakarta EE 10 -->
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>Mein Wicket Projekt</display-name>

    <!--
    WicketFilter: der einzige Servlet-Filter der nötig ist.

    WICHTIG: KEIN web.xml-Parameter für RuntimeConfigurationType!
    Das würde MyApplication.getConfigurationType() ÜBERSCHREIBEN.
    Wicket liest zuerst den web.xml-Parameter "configuration" —
    wenn er gesetzt ist, ignoriert Wicket den Rückgabewert von
    getConfigurationType(). Das ist der häufigste Fehler.

    Wir setzen den Parameter NICHT → getConfigurationType() hat volle Kontrolle.
    -->
    <filter>
        <filter-name>wicket.mein-projekt</filter-name>
        <filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>

        <!-- Pflicht: welche Application-Klasse soll Wicket verwenden? -->
        <init-param>
            <param-name>applicationClassName</param-name>
            <param-value>com.example.MyApplication</param-value>
        </init-param>

        <!--
        ABSICHTLICH NICHT GESETZT: wicket.configuration
        Wenn wir hier "development" oder "deployment" setzen würden,
        würde Wicket unsere getConfigurationType()-Implementierung ignorieren!

        <init-param>
            <param-name>wicket.configuration</param-name>
            <param-value>deployment</param-value>
        </init-param>
        -->
    </filter>

    <filter-mapping>
        <filter-name>wicket.mein-projekt</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <!-- Session-Konfiguration -->
    <session-config>
        <session-timeout>30</session-timeout>
        <cookie-config>
            <http-only>true</http-only>
            <secure>true</secure>   <!-- HTTPS-only in prod und test -->
        </cookie-config>
        <tracking-mode>COOKIE</tracking-mode>
    </session-config>

</web-app>
```

---

## 10. BasePage — umgebungs-abhängige Sichtbarkeit

```java
package com.example.pages;

import com.example.MyApplication;
import com.example.config.AppConfig;
import com.example.config.Environment;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public abstract class BasePage extends WebPage {

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // AppConfig aus der Application holen (kein DI-Framework nötig!)
        var config = MyApplication.get()
            .getMetaData(MyApplication.ApplicationKeys.APP_CONFIG);

        var env    = config.environment();
        var isDev  = getApplication().getConfigurationType()
                         == RuntimeConfigurationType.DEVELOPMENT;

        // ── Dev-Banner: nur in DEVELOPMENT, deutlich sichtbar ────────────
        add(new Label("devBanner",
                String.format("🛠 DEVELOPMENT | Env: %s | DB: %s",
                    env.name(),
                    config.dbUrl().replaceAll("//.*@", "//*@")))  // Passwort verstecken
            .setVisible(isDev));

        // ── Test-Banner: nur in Test-Umgebung ────────────────────────────
        add(new Label("testBanner",
                "🧪 TEST-UMGEBUNG — keine Produktionsdaten")
            .setVisible(env.isTest() && !isDev));
    }
}
```

```html
<!-- src/main/java/com/example/pages/BasePage.html -->
<!DOCTYPE html>
<html xmlns:wicket="http://wicket.apache.org">
<head>
    <meta charset="UTF-8"/>
    <title wicket:id="pageTitle">Mein Wicket Projekt</title>
</head>
<body>

<!-- Dev-Banner: in DEPLOYMENT-Modus komplett abwesend (setVisible=false) -->
<div wicket:id="devBanner"
     style="position:sticky;top:0;z-index:9999;
            background:#b34700;color:white;
            padding:5px 12px;font-family:monospace;font-size:12px;
            text-align:center;">
</div>

<!-- Test-Banner -->
<div wicket:id="testBanner"
     style="background:#1565c0;color:white;padding:5px 12px;
            text-align:center;font-weight:bold;">
</div>

<!-- Haupt-Inhalt -->
<main wicket:id="mainContent">
    <!-- Unterklassen füllen das -->
</main>

<!--
Wicket DebugBar: erscheint automatisch wenn DEVELOPMENT-Modus.
Kein explizites HTML nötig — Wicket fügt es selbst unten an.
-->

</body>
</html>
```

---

## 11. WicketTester — korrekt für reine Wicket-Tests

### 11.1 ❌ Schlecht

```java
// ❌ WicketApplication direkt mit new() — Umgebungserkennung umgangen
class HomePageTest {
    @Test
    void test() {
        // new MyApplication() ruft init() nicht auf!
        // WICKET_ENV wird nicht gelesen, AppConfig nicht initialisiert.
        // Beim ersten @SpringBean-Äquivalent-Zugriff: NullPointerException.
        var tester = new WicketTester(new MyApplication());
        tester.startPage(HomePage.class);  // Sofortiger Fehler
    }
}
```

### 11.2 ✅ Gut — WicketTester mit kontrollierter Testanwendung

```java
package com.example;

import com.example.config.Environment;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basis für alle Wicket-UI-Tests.
 *
 * Strategie:
 * Eine dedizierte TestApplication (innere Klasse) überschreibt die
 * Datenbankverbindung durch H2 In-Memory.
 * WICKET_ENV muss für Tests NICHT gesetzt sein — TestApplication
 * setzt den Typ direkt auf DEPLOYMENT.
 *
 * Warum DEPLOYMENT in Tests?
 * Tests sollen das Produktionsverhalten prüfen, nicht den Dev-Modus.
 * Im DEPLOYMENT-Modus: kein Dev-Banner, kein Stack-Trace-Seite,
 * volle Caches — genau wie in Produktion.
 */
public abstract class BaseWicketTest {

    protected WicketTester tester;
    private   TestApplication application;

    /**
     * Dedizierte Test-Anwendung:
     * - Überschreibt getConfigurationType() → immer DEPLOYMENT
     * - Überschreibt initInfrastructure() → H2 statt PostgreSQL
     * - Überschreibt EnvironmentGuard → kein Logging-Spam in Tests
     */
    static class TestApplication extends MyApplication {

        private final DataSource testDataSource;

        TestApplication() {
            // H2 In-Memory Datenbank für Tests
            var h2 = new JdbcDataSource();
            h2.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            h2.setUser("sa");
            h2.setPassword("");
            this.testDataSource = h2;
        }

        // DEPLOYMENT-Modus für alle Tests — egal welche Umgebungsvariable gesetzt ist
        @Override
        public RuntimeConfigurationType getConfigurationType() {
            return RuntimeConfigurationType.DEPLOYMENT;
        }

        @Override
        protected void init() {
            // Environment manuell auf TEST setzen
            // (damit BasePage den Test-Banner zeigt, kein Dev-Banner)
            // Wir umgehen Environment.detect() für kontrollierten Test-Setup.
            super.init();
        }

        // Infrastruktur-Initialisierung überschreiben: H2 statt PostgreSQL
        @Override
        protected void initInfrastructure() {
            // DataSource direkt setzen (kein HikariCP, kein echtes PostgreSQL)
            setMetaData(ApplicationKeys.DATA_SOURCE, testDataSource);

            // Schema erstellen (oder Flyway in Tests)
            createTestSchema();
        }

        private void createTestSchema() {
            try (var conn = testDataSource.getConnection();
                 var stmt = conn.createStatement()) {
                // Minimal-Schema für Tests
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        customer_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        total_cents BIGINT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            } catch (Exception e) {
                throw new RuntimeException("Test-Schema-Erstellung fehlgeschlagen", e);
            }
        }
    }

    @BeforeEach
    void setUp() {
        application = new TestApplication();
        tester      = new WicketTester(application);

        // PFLICHT: DEPLOYMENT-Modus in Tests explizit prüfen.
        // Wenn dieser Assert fehlschlägt, testet man den Dev-Modus statt Produktion.
        assertThat(tester.getApplication().getConfigurationType())
            .as("Tests müssen im DEPLOYMENT-Modus laufen")
            .isEqualTo(RuntimeConfigurationType.DEPLOYMENT);
    }

    @AfterEach
    void tearDown() {
        tester.destroy();
    }
}
```

### 11.3 Konkrete Seitentests

```java
package com.example.pages;

import com.example.BaseWicketTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomePageTest extends BaseWicketTest {

    @Test
    void startseite_rendert_ohne_fehler() {
        tester.startPage(HomePage.class);

        tester.assertRenderedPage(HomePage.class);
        tester.assertNoErrorMessage();
        tester.assertNoInfoMessage();
    }

    @Test
    void devBanner_ist_NICHT_sichtbar_im_deploymentModus() {
        tester.startPage(HomePage.class);
        // Im DEPLOYMENT-Modus: kein Dev-Banner — setVisible(false)
        tester.assertInvisible("devBanner");
    }

    @Test
    void testBanner_ist_sichtbar_weil_testEnvironment() {
        tester.startPage(HomePage.class);
        // Test-Umgebung: blauer Banner "TEST-UMGEBUNG" sichtbar
        tester.assertVisible("testBanner");
    }

    @Test
    void unerwartete_exception_zeigt_neutrale_fehlerseite() {
        // Seite die absichtlich eine Exception wirft
        tester.startPage(ExceptionThrowingPage.class);

        // Im DEPLOYMENT-Modus: zur ProductionErrorPage umgeleitet
        tester.assertRenderedPage(ProductionErrorPage.class);

        // NICHT auf Wickets Debug-ExceptionPage gelandet
        assertThat(tester.getLastRenderedPage())
            .isNotInstanceOf(
                org.apache.wicket.markup.html.pages.ExceptionErrorPage.class);
    }

    @Test
    void geschuetzte_seite_ohne_login_leitet_zu_login_um() {
        tester.startPage(OrderListPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    void loginFormular_mit_falschen_credentials_zeigt_fehlermeldung() {
        tester.startPage(LoginPage.class);
        var form = tester.newFormTester("loginForm");
        form.setValue("username", "unbekannt@test.de");
        form.setValue("password", "falsch");
        form.submit("loginButton");

        tester.assertRenderedPage(LoginPage.class);
        tester.assertErrorMessages("Benutzername oder Passwort ungültig.");
    }
}
```

---

## 12. Deployment-Aktivierung

### 12.1 Lokal — Entwicklung

```bash
# Umgebungsvariable setzen (Terminal-Session)
export WICKET_ENV=dev

# Embedded Jetty starten via Maven
mvn jetty:run

# Oder einzeilig:
WICKET_ENV=dev mvn jetty:run

# Erwartet:
# → Startup-Log: "DEVELOPMENT 🛠️"
# → Browser: orange Dev-Banner sichtbar
# → Browser: DebugBar unten sichtbar
# → HTML ändern → sofort ohne Neustart sichtbar (scan=2 Sekunden)

# IntelliJ IDEA: Run/Debug Configuration → Environment Variables: WICKET_ENV=dev
```

### 12.2 Test-Server / CI

```bash
# Systemd-Service (Linux-Server):
# /etc/systemd/system/meinprojekt.service
[Unit]
Description=Mein Wicket Projekt
After=network.target

[Service]
Type=simple
Environment="WICKET_ENV=test"
Environment="DB_URL=jdbc:postgresql://test-db:5432/meinprojekt_test"
Environment="DB_USER=testuser"
Environment="DB_PASSWORD=${TEST_DB_PASSWORD}"
ExecStart=/usr/bin/java -jar /opt/meinprojekt/meinprojekt.war
Restart=on-failure
User=wicket

[Install]
WantedBy=multi-user.target
```

```xml
<!-- Tomcat: setenv.sh oder Kontext-Parameter alternativ zu Systemd -->
<!-- /opt/tomcat/bin/setenv.sh -->
<!-- export WICKET_ENV=test -->
<!-- export DB_URL=jdbc:postgresql://test-db:5432/meinprojekt_test -->
<!-- export DB_USER=testuser -->
<!-- export DB_PASSWORD=${TEST_DB_PASSWORD} -->
```

```bash
# CI/CD (GitLab CI Beispiel):
# .gitlab-ci.yml
# deploy:test:
#   variables:
#     WICKET_ENV: test
#     DB_URL: jdbc:postgresql://test-db:5432/meinprojekt_test
#     DB_USER: testuser
#     DB_PASSWORD: $TEST_DB_PASSWORD
#   script:
#     - mvn package -DskipTests
#     - scp target/meinprojekt.war test-server:/opt/tomcat/webapps/
```

### 12.3 Produktion

```bash
# Docker-Container:
docker run \
    -e WICKET_ENV=prod \
    -e DB_URL=jdbc:postgresql://prod-db:5432/meinprojekt \
    -e DB_USER=appuser \
    -e DB_PASSWORD="${VAULT_DB_PASSWORD}" \
    -e MAIL_HOST=smtp.example.com \
    -e MAIL_USER=noreply@example.com \
    -e MAIL_PASSWORD="${VAULT_MAIL_PASSWORD}" \
    -p 8080:8080 \
    meinprojekt:latest

# Kubernetes Deployment:
env:
  - name: WICKET_ENV
    value: "prod"
  - name: DB_URL
    valueFrom:
      secretKeyRef:
        name: db-credentials
        key: url
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-credentials
        key: password
```

---

## 13. Übersichtstabelle: Was gilt wo

```
┌────────────────────────────────────────────────────────────────────────────┐
│              UMGEBUNGS-KONFIGURATION IM VERGLEICH                          │
├──────────────────────────┬────────────────┬────────────────┬───────────────┤
│ Eigenschaft              │ dev            │ test           │ prod          │
├──────────────────────────┼────────────────┼────────────────┼───────────────┤
│ WICKET_ENV               │ dev            │ test           │ prod          │
│ RuntimeConfigurationType │ DEVELOPMENT    │ DEPLOYMENT     │ DEPLOYMENT    │
│ DebugBar                 │ ✅ Sichtbar    │ ❌ Nicht da    │ ❌ Nicht da   │
│ Stack-Trace im Browser   │ ✅ Vollständig │ ❌ Neutrale    │ ❌ Neutrale   │
│                          │                │    Fehlerseite │    Fehlerseite│
│ Markup-Cache             │ ❌ Aus         │ ✅ An          │ ✅ An         │
│ Minified JS/CSS          │ ❌ Original    │ ✅ Minifiziert │ ✅ Minifiziert│
│ wicket:id im HTML        │ ✅ Sichtbar    │ ❌ Entfernt    │ ❌ Entfernt   │
│ HTML-Änderung sofort?    │ ✅ Ja          │ ❌ Neustart    │ ❌ Neustart   │
│ Dev-Banner               │ ✅ Orange      │ ❌ Nicht da    │ ❌ Nicht da   │
│ Test-Banner              │ ❌ Nicht da    │ ✅ Blau        │ ❌ Nicht da   │
│ DB-Passwort in Config    │ ✅ OK (lokal)  │ ❌ Env-Var     │ ❌ Env-Var    │
│ Startup-Guard            │ —              │ Warnung        │ Exception     │
│ Session-Cache            │ Groß           │ Normal         │ Begrenzt 10MB │
└──────────────────────────┴────────────────┴────────────────┴───────────────┘
```

---

## 14. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| web.xml `wicket.configuration`-Parameter | Einfach, Wicket-Standard | Überschreibt `getConfigurationType()` komplett — unsere Logik wird ignoriert | Kontrolle geht an XML verloren; schwer testbar |
| Hardcodierter Modus in Klasse | Einfachste Implementierung | Vergessen zu ändern vor Deployment → Production im DEVELOPMENT-Modus | Menschlicher Fehler unter Zeitdruck ist unausweichlich |
| System Property (`-Dwicket.configuration=...`) | Explizit, flexibel | JVM-Argument muss bei jedem Start korrekt gesetzt sein; nicht standardisiert | Umgebungsvariablen sind Container/Cloud-nativer als JVM-Argumente |
| Profil-Parameter in Properties-Datei | Kein externes Setup nötig | Properties-Datei muss pro Umgebung ausgewählt werden — wer wählt sie aus? | Verlagert das Problem nur, löst es nicht |
| Separater `environment.properties` | Klar getrennt | Noch eine Datei die verwaltet werden muss | Umgebungsvariable ist einfacher und Deployment-Standard |

---

## 15. Trade-off-Matrix

| Qualitätsziel | ENV-Variable + Properties (gewählt) | Hardcodiert | web.xml-Parameter |
|---|---|---|---|
| Sicherheit (kein Dev in Prod) | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐ |
| Entwicklerproduktivität (dev) | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐⭐⭐ |
| Einfachheit des Setup | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| CI/CD-Integration | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐ |
| Wartbarkeit | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐⭐ |
| Testbarkeit | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |

---

## 16. Kosten-Nutzen-Analyse

```
Initiale Kosten:
  Environment-Enum + AppConfig schreiben:   1 PT
  EnvironmentGuard implementieren:           0.5 PT
  Properties-Dateien erstellen:             0.5 PT
  Team-Schulung (30 Minuten):               ~0.2 PT
  GESAMT:                                    ~2.2 PT = 1.400 EUR

Verhinderte Incidents:
  "DEVELOPMENT in Produktion" ohne Guard:
    Durchschnittliche Zeit bis Entdeckung:  2–8 Stunden
    Fix-Aufwand:                            1–2 Stunden
    Reputationsschaden (Kunden sehen Traces): nicht quantifizierbar
    Geschätzter Kosten pro Incident:        ~2.000 EUR
    Wahrscheinlichkeit/Jahr ohne Guard:     2–3× (unter Zeitdruck deployen)
    Ersparnis/Jahr:                         4.000–6.000 EUR

Break-Even: nach dem ersten verhinderten Incident (< 3 Monate)
```

---

## 17. Akzeptanzkriterien

- [ ] `WICKET_ENV=dev` → Startup-Log zeigt "DEVELOPMENT 🛠️", DebugBar sichtbar, Dev-Banner orange
- [ ] `WICKET_ENV=prod` + DEVELOPMENT in `getConfigurationType()` → Anwendung startet NICHT
- [ ] `WICKET_ENV=` (leer) oder nicht gesetzt → PROD-Modus aktiv (sicherer Default)
- [ ] `web.xml` enthält KEINEN `wicket.configuration`-Parameter
- [ ] `BaseWicketTest.setUp()`: Assert `DEPLOYMENT`-Modus grün
- [ ] Test `devBanner_ist_NICHT_sichtbar_im_deploymentModus` grün
- [ ] `prod.properties` enthält keine Klartext-Passwörter (CI-Scan prüft das)
- [ ] Kein HTML-Quelltext in Produktion enthält `wicket:id` oder `wicket:panel`

---

## 18. Quellen & Referenzen

- **Apache Wicket Documentation, "Configuration"** — RuntimeConfigurationType: DEVELOPMENT vs. DEPLOYMENT; Verhalten im Detail; getConfigurationType() vs. web.xml-Parameter. wicket.apache.org
- **Martin Dashorst & Eelco Hillenius, "Wicket in Action" (2008), Kap. 11** — "Deploying Wicket Applications": Produktions-Konfiguration, Performance-Optimierung, Sicherheitsaspekte.
- **12-Factor App Methodology, Faktor III "Config"** — "Store config in the environment": Unterschied zwischen Code, Konfiguration und Secrets; Umgebungsvariablen als Standard.
- **Apache Wicket ChangeLog 10.x** — Änderungen der RuntimeConfigurationType-Erkennung in Wicket 10 gegenüber Wicket 9.

---

## Verwandte ADRs

Da dieses ADR für ein reines Wicket-Projekt ohne Spring Boot gilt, gibt es
keine Abhängigkeit zu Spring-spezifischen ADRs dieses Kompendiums.
Konzepte die analog gelten:
- Prinzip des sicheren Defaults: konservativste Konfiguration als Fallback
- Fail-Fast: Startup-Guard schlägt früh fehl statt stumm falsch zu laufen
- Separation of Concerns: Umgebungserkennung in eigener Klasse isoliert
