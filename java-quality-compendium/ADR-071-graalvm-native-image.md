# ADR-071 — GraalVM Native Image: Wann und Wie

| Feld       | Wert                                              |
|------------|-----------------------------------|
| Status     | ✅ Akzeptiert                     |
| Java       | 21 · Spring Boot 3.x · GraalVM 21 |
| Datum      | 2024-01-01                        |
| Kategorie  | Performance / Deployment          |

---

## Kontext & Problem

Spring Boot mit JVM startet in 3–10 Sekunden und verbraucht 300–500 MB RAM. Für Serverless, Kubernetes-Skalierung oder kurze Batch-Jobs ist das problematisch. **GraalVM Native Image** kompiliert die Spring-Anwendung zu einer nativen Binary: Startup in <100ms, RAM-Verbrauch <100MB — aber mit Einschränkungen und höherem Build-Aufwand.

---

## JVM vs. Native Image: Trade-offs

```
JVM (Standard):
✓ Vollständige Java-Kompatibilität (Reflection, Dynamic Proxy, alles)
✓ JIT-Optimierung: Peak-Performance nach Warmup (~30 Sekunden)
✓ Einfacher Build
✗ Startup: 3-10 Sekunden
✗ RAM: 300-500 MB für Spring Boot

Native Image (GraalVM):
✓ Startup: 50-200 ms
✓ RAM: 50-150 MB
✓ Kein JVM-Overhead
✗ Closed-World Assumption: alles muss zur Build-Zeit bekannt sein
✗ Reflection, Dynamic Proxy: erfordert Hints
✗ Peak-Performance niedriger als JVM mit JIT
✗ Build dauert 3-10 Minuten (Resource-intensiv)

Wähle Native Image wenn:
→ Serverless (AWS Lambda, Google Cloud Run): Kaltstart-Latenz kritisch
→ Viele kurzlebige Pods (Kubernetes Autoscaling schnell)
→ Batch-Jobs die nach Verarbeitung beendet werden
→ Ressourcenbeschränkte Umgebungen

Bleib bei JVM wenn:
→ Langlebige Services (99% der Web-Anwendungen)
→ Peak-Throughput wichtiger als Startup-Zeit
→ Viel Reflection/Dynamic Proxy (JPA Entities, einige Libraries)
```

---

## Spring Boot Native Image: Konfiguration

```kotlin
// build.gradle.kts
plugins {
    id("org.graalvm.buildtools.native") version "0.10.1"
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("order-service")
            buildArgs.addAll(
                "--no-fallback",           // Kein JVM-Fallback
                "--initialize-at-build-time=org.slf4j",
                "-H:+ReportExceptionStackTraces",
                // Optimierungsflags
                "-O2",                     // Optimierungslevel
                "--gc=G1"                  // GraalVM 21: G1GC für Native
            )
        }
    }
}

// Build: ./gradlew nativeCompile (dauert ~5 Minuten)
// Output: build/native/nativeCompile/order-service (native binary)
```

---

## Reflection Hints: was GraalVM nicht automatisch erkennt

```java
// Native Image braucht Hints für Reflection, Proxies, Ressourcen
// Spring Boot AOT Processing generiert viele Hints automatisch

// Eigene Hints für Custom-Code
@Configuration
@ImportRuntimeHints(OrderServiceHints.class)
public class NativeConfig {}

public class OrderServiceHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Reflection: Klassen die dynamisch instanziert werden
        hints.reflection()
            .registerType(OrderEntity.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS)
            .registerType(OrderStatus.class,
                MemberCategory.DECLARED_FIELDS);

        // Ressourcen: Properties, SQL-Dateien, etc.
        hints.resources()
            .registerPattern("db/migration/*.sql")
            .registerPattern("messages*.properties");

        // Serialization: Jackson braucht Hints für Records/Generics
        hints.serialization()
            .registerType(TypeReference.of(List.class));
    }
}
```

---

## AOT Testing: Native-Kompatibilität früh prüfen

```java
// Native-Kompatibilität testen ohne vollständigen Native-Build
@SpringBootTest
@TestPropertySource(properties = "spring.aot.enabled=true")
class NativeCompatibilityTest {

    @Test
    void applicationContext_startsSuccessfully() {
        // Wenn dieser Test scheitert: Native Build wird auch scheitern
    }

    @Test
    void orderService_worksWithNativeReflection() {
        // Spezifische Features testen die Reflection nutzen
        var order = new Order(new UserId(1L));
        assertThat(order).isNotNull();
    }
}

// Nativetest: vollständiger Native-Build + Test
// ./gradlew nativeTest (dauert länger, aber verifiziert echtes Native)
```

---

## Docker: Native Binary im Container

```dockerfile
# Ultrakleine Native Binary in Distroless-Container
# Build Stage: GraalVM für Kompilierung
FROM ghcr.io/graalvm/native-image:21 AS builder
WORKDIR /build
COPY . .
RUN ./gradlew nativeCompile --no-daemon

# Runtime Stage: kein JVM, nur Binary
FROM gcr.io/distroless/base-debian12
COPY --from=builder /build/build/native/nativeCompile/order-service /app/order-service
EXPOSE 8080
ENTRYPOINT ["/app/order-service"]
# Image-Größe: ~50MB (vs. ~150MB mit JRE)
# Startup: <100ms
# RAM: ~80MB (vs. ~300MB)
```

---

## Serverless: AWS Lambda mit Native Image

```kotlin
// build.gradle.kts für Lambda
dependencies {
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws:4.1.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
}

graalvmNative {
    binaries {
        named("main") {
            buildArgs.add("--enable-url-protocols=http,https")
        }
    }
}
```

```java
// Lambda Handler
@SpringBootApplication
public class OrderLambdaHandler
        implements ApplicationContextInitializer<GenericApplicationContext> {

    public static void main(String[] args) {
        SpringApplication.run(OrderLambdaHandler.class, args);
    }

    // Kaltstart mit Native Image: ~50ms statt ~5000ms mit JVM
    @Bean
    public Function<OrderRequest, OrderResponse> processOrder(OrderService service) {
        return request -> service.process(request);
    }
}
```

---

## Bekannte Einschränkungen und Lösungen

```java
// ① JPA/Hibernate: größter Stolperstein
// Lösung: Spring Data JPA unterstützt Native ab Spring Boot 3.1+ mit AOT
// @Entity-Klassen brauchen explizite Hints wenn Vererbung/Discriminators verwendet

// ② Spring AOP (@Transactional, @Cacheable): funktioniert via Spring Proxy (kein CGLIB)
// Spring Boot 3.x generiert AOT-Proxies für Native automatisch

// ③ Liquibase/Flyway: funktioniert out-of-the-box mit Spring Boot 3.1+

// ④ Lombok: partiell — @Slf4j, @Builder funktionieren, @Data problematisch
// Lösung: Records statt Lombok (→ ADR-001)

// ⑤ Testcontainers: nicht in Native Tests (Docker-Integration nicht nativ)
// Lösung: Testcontainers-Tests als separate Test-Source-Set
```

---

## Konsequenzen

**Positiv:** Startup <100ms ermöglicht echte Serverless-Nutzung. RAM-Verbrauch 3-5× niedriger. Kein JVM-Overhead — einfacher Container, kleineres Angriffsziel.

**Negativ:** Build-Zeit: 5-10 Minuten (CI-Kosten). Closed-World Assumption bricht manche Libraries. Debugging nativer Binaries schwieriger. Peak-Throughput bei langlebigen Services schlechter als JVM mit JIT.

---

## 💡 Guru-Tipps

- **Nicht für jeden Service**: Native Image macht Sinn für Serverless/Lambda, nicht für typische Web-Services mit langer Laufzeit.
- **AOT-Modus zuerst testen**: `spring.aot.enabled=true` im Test zeigt Probleme früh ohne Native-Build.
- **GraalVM Tracing Agent**: `java -agentlib:native-image-agent=config-output-dir=hints/ -jar app.jar` generiert Hints automatisch.
- **Build Cache**: GraalVM Native Build ist teuer — Gradle Build Cache (→ ADR-050) drastisch hilft.

---

## Verwandte ADRs

- [ADR-037](ADR-037-docker-container.md) — Native Binary in Distroless-Container.
- [ADR-001](ADR-001-records-statt-javabeans.md) — Records statt Lombok für Native-Kompatibilität.
- [ADR-043](ADR-043-jvm-tuning-hikaricp.md) — JVM-Tuning ist irrelevant für Native Image.
