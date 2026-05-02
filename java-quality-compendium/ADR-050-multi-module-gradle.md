# ADR-050 — Multi-Module Gradle Build & Dependency Management

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Gradle 8.x                   |
| Datum      | 2024-01-01                        |
| Kategorie  | Build / Architektur               |

---

## Kontext & Problem

Ab einer gewissen Projektgröße wird ein einzelnes Gradle-Modul zum Problem: alles hängt von allem ab, Kompilierzeit steigt, Module-Grenzen werden nicht erzwungen, Deployment-Einheiten lassen sich nicht isolieren. Multi-Module-Projekte machen Abhängigkeiten explizit und ermöglichen inkrementelle Builds.

---

## Projektstruktur

```
e-commerce/
├── settings.gradle.kts          ← Root: alle Module registrieren
├── build.gradle.kts             ← Root: gemeinsame Konfiguration
├── gradle/
│   └── libs.versions.toml       ← Version Catalog: alle Versionen zentral
│
├── domain/                      ← Reines Java, KEIN Spring
│   └── build.gradle.kts
│
├── application/                 ← Use Cases, Ports
│   └── build.gradle.kts
│
├── adapter-persistence/         ← JPA-Implementierungen
│   └── build.gradle.kts
│
├── adapter-rest/                ← Spring MVC Controller
│   └── build.gradle.kts
│
├── adapter-messaging/           ← Kafka Consumer/Producer
│   └── build.gradle.kts
│
└── bootstrap/                   ← Spring Boot App, alles zusammen
    └── build.gradle.kts
```

---

## settings.gradle.kts

```kotlin
rootProject.name = "e-commerce"

// Alle Submodule registrieren
include(
    "domain",
    "application",
    "adapter-persistence",
    "adapter-rest",
    "adapter-messaging",
    "bootstrap"
)

// Gradle Build Cache: beschleunigt Rebuilds
buildCache {
    local { isEnabled = true }
}
```

---

## gradle/libs.versions.toml — Version Catalog

```toml
# Alle Versionen zentral — kein copy-paste in jedem build.gradle.kts
[versions]
spring-boot      = "3.3.0"
spring-dep-mgmt  = "1.1.5"
java             = "21"
junit            = "5.10.2"
assertj          = "3.25.3"
mockito          = "5.11.0"
testcontainers   = "1.19.7"
mapstruct        = "1.5.5.Final"
pitest           = "1.15.3"
jqwik            = "1.8.4"
archunit         = "1.3.0"
gatling          = "3.10.3"

[libraries]
# Spring Boot BOM (kein explizites Versions-Management nötig)
spring-boot-starter-web        = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-starter-data-jpa   = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-starter-security   = { module = "org.springframework.boot:spring-boot-starter-security" }
spring-boot-starter-test       = { module = "org.springframework.boot:spring-boot-starter-test" }
spring-boot-starter-actuator   = { module = "org.springframework.boot:spring-boot-starter-actuator" }
spring-kafka                   = { module = "org.springframework.kafka:spring-kafka" }

# Test
junit-jupiter     = { module = "org.junit.jupiter:junit-jupiter",     version.ref = "junit" }
assertj-core      = { module = "org.assertj:assertj-core",            version.ref = "assertj" }
mockito-junit5    = { module = "org.mockito:mockito-junit-jupiter",   version.ref = "mockito" }
testcontainers-pg = { module = "org.testcontainers:postgresql",       version.ref = "testcontainers" }
archunit-junit5   = { module = "com.tngtech.archunit:archunit-junit5", version.ref = "archunit" }
jqwik             = { module = "net.jqwik:jqwik",                     version.ref = "jqwik" }

# Infrastructure
mapstruct         = { module = "org.mapstruct:mapstruct",             version.ref = "mapstruct" }
mapstruct-proc    = { module = "org.mapstruct:mapstruct-processor",   version.ref = "mapstruct" }
postgresql        = { module = "org.postgresql:postgresql" }
flyway-core       = { module = "org.flywaydb:flyway-core" }

[plugins]
spring-boot       = { id = "org.springframework.boot",                  version.ref = "spring-boot" }
spring-dep-mgmt   = { id = "io.spring.dependency-management",           version.ref = "spring-dep-mgmt" }
pitest            = { id = "info.solidsoft.pitest",                     version.ref = "pitest" }
gatling           = { id = "io.gatling.gradle",                         version.ref = "gatling" }

[bundles]
# Oft zusammen verwendete Dependencies als Bundle
testing = ["junit-jupiter", "assertj-core", "mockito-junit5"]
```

---

## Root build.gradle.kts

```kotlin
// Root-Build: gemeinsame Konfiguration für alle Submodule
plugins {
    alias(libs.plugins.spring.boot)     apply false
    alias(libs.plugins.spring.dep.mgmt) apply false
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    // Gemeinsame Compile-Flags
    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf(
            "-parameters",        // Für Spring Parameter-Namen-Auflösung
            "-Xlint:unchecked",   // Warnungen bei unchecked Casts
            "-Xlint:deprecation"  // Warnungen bei deprecated API
        ))
    }

    // Gemeinsame Test-Konfiguration
    tasks.withType<Test> {
        useJUnitPlatform()
        systemProperty("spring.test.constructor.autowire.mode", "all")
        maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
    }

    // Gemeinsame Test-Abhängigkeiten
    dependencies {
        "testImplementation"(libs.bundles.testing)
    }
}
```

---

## domain/build.gradle.kts

```kotlin
// Domain: kein Spring, kein JPA — reines Java
plugins {
    java
}

dependencies {
    // Nur: Logging-Interface, Validation-API, kein Framework!
    implementation("org.slf4j:slf4j-api")
    implementation("jakarta.validation:jakarta.validation-api")
    // KEIN spring-boot, JPA, Jackson — Domain kennt kein Framework
}
```

---

## adapter-persistence/build.gradle.kts

```kotlin
plugins {
    java
    alias(libs.plugins.spring.dep.mgmt)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    implementation(project(":domain"))        // Domain-Interfaces
    implementation(project(":application"))   // Ports

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.proc)

    testImplementation(libs.testcontainers.pg)
}
```

---

## bootstrap/build.gradle.kts

```kotlin
// Bootstrap: assembliert alle Module zur deployable Applikation
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.mgmt)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapter-persistence"))
    implementation(project(":adapter-rest"))
    implementation(project(":adapter-messaging"))

    implementation(libs.spring.boot.starter.actuator)
}

// Nur bootstrap erzeugt ein ausführbares JAR
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

// Alle anderen Module: nur normales JAR
tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("") // kein -plain Suffix
}
```

---

## Abhängigkeitsregeln (ArchUnit)

```java
// Modulare Abhängigkeiten maschinell prüfen (→ ADR-009)
@AnalyzeClasses(packages = "com.example")
class ModuleDependencyTest {

    @ArchTest
    static final ArchRule domain_hat_keine_framework_abhängigkeiten =
        noClasses()
            .that().resideInAPackage("com.example.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.."
            )
            .because("ADR-031/ADR-050: Domain kennt kein Framework");
}
```

---

## Inkrementeller Build: Warum Multi-Module schneller ist

```bash
# Änderung nur in adapter-rest:
./gradlew :bootstrap:bootJar

# Gradle baut NUR:
# - adapter-rest (geändert)
# - bootstrap (hängt von adapter-rest ab)
# NICHT:
# - domain (unverändert, gecacht)
# - application (unverändert, gecacht)
# - adapter-persistence (unverändert, gecacht)
# → Build-Zeit: 15s statt 60s
```

---

## Konsequenzen

**Positiv:** Modulare Grenzen sind durch Gradle-Abhängigkeiten erzwungen — kein "accidental coupling". Inkrementelle Builds: nur geänderte Module neu kompilieren. Version Catalog eliminiert Versions-Drift zwischen Modulen.

**Negativ:** Initiale Setup-Komplexität. Refactoring über Modulgrenzen (Klasse von `domain` nach `application` verschieben) erfordert Gradle-Kenntnisse.

---

## Tipps

- **`./gradlew dependencies --configuration runtimeClasspath`**: zeigt den vollständigen Abhängigkeitsbaum.
- **`./gradlew :domain:test`**: Tests nur für ein einzelnes Modul ausführen — schnell.
- **Gradle Build Scan**: `./gradlew build --scan` → interaktiver Build-Report in der Cloud.
- **Version Catalog in der IDE**: IntelliJ 2023+ unterstützt `libs.versions.toml` mit Autocomplete.
 