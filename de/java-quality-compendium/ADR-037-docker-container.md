# ADR-037 — Docker & Container: Sicher, klein, reproduzierbar

| Feld       | Wert                              |
|------------|-----------------------------------|
| Java       | 21 · Spring Boot 3.x · Docker     |
| Datum      | 2024-01-01                        |
| Kategorie  | DevOps / Container                |

---

## Regel 1 — Multi-Stage Builds: kleines, sicheres Image

```dockerfile
# ❌ Schlecht: fat image, Build-Tools im Produktions-Image
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./gradlew build
EXPOSE 8080
CMD ["java", "-jar", "build/libs/app.jar"]
# Image-Größe: ~600MB. Enthält: JDK, Gradle, Quellcode, Test-Dependencies

# ✅ Multi-Stage: Build-Stage von Runtime-Stage trennen
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
# Abhängigkeiten zuerst (Layer-Caching: ändert sich selten)
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Layered JAR extrahieren (Spring Boot 3.x)
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination layers

# ── Runtime Stage: minimales Image ──────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
# Kein JDK, kein Gradle, kein Quellcode in Produktion

# Security: non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# Layered Copy: Dependencies ändern sich selten → besseres Caching
COPY --from=builder /build/layers/dependencies/          ./
COPY --from=builder /build/layers/spring-boot-loader/    ./
COPY --from=builder /build/layers/snapshot-dependencies/ ./
COPY --from=builder /build/layers/application/           ./

# Gesundheitscheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
# Image-Größe: ~150MB. Nur Runtime.
```

---

## Regel 2 — JVM Container-Awareness

```dockerfile
# JVM-Flags für Container-Betrieb
ENTRYPOINT ["java", \
    # Container-Limits respektieren (Standard ab Java 11)
    "-XX:+UseContainerSupport", \
    # Max 75% des Container-RAM als Heap
    "-XX:MaxRAMPercentage=75.0", \
    # G1GC: gut für Container mit variablem Heap
    "-XX:+UseG1GC", \
    # GC-Logs für Diagnose
    "-Xlog:gc*:file=/tmp/gc.log:time,uptime:filecount=5,filesize=20m", \
    # Schnellere Zufallszahlen (ENTROPIE-Problem in Containern)
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
```

---

## Regel 3 — Security: Non-Root, Read-Only Filesystem

```dockerfile
# Non-Root-User
RUN addgroup --system --gid 1001 spring \
 && adduser  --system --uid 1001 --ingroup spring --no-create-home spring
USER 1001:1001

# Read-Only-Filesystem + explizite Schreibverzeichnisse
# Im Kubernetes-Manifest:
# securityContext:
#   readOnlyRootFilesystem: true
# volumeMounts:
#   - mountPath: /tmp
#     name: tmp-vol
```

---

## Regel 4 — .dockerignore: Build-Kontext minimieren

```dockerignore
# .dockerignore
.git
.gradle
build/
*.md
docs/
.env
.env.*
docker-compose*.yml
Dockerfile*
*.log
*.tmp
src/test/
```

---

## Spring Boot Buildpacks (Alternative zu Dockerfile)

```kotlin
// build.gradle.kts: Spring Boot Buildpacks ohne eigenes Dockerfile
tasks.named<BootBuildImage>("bootBuildImage") {
    imageName.set("ghcr.io/${System.getenv("GITHUB_REPOSITORY") ?: "my-app"}:${project.version}")
    environment.set(mapOf(
        "BP_JVM_VERSION" to "21",
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:MaxRAMPercentage=75.0"
    ))
    // Automatisch: non-root, layered, OCI-konform
}
```

---

## Konsequenzen

**Positiv:** Multi-Stage Build: Image 4× kleiner als naiver Ansatz. Non-Root und Read-Only minimieren Angriffsfläche. Container-Support-JVM-Flags verhindern OOM-Kills durch falsch dimensionierten Heap.

**Negativ:** Layered JAR erfordert Spring Boot 2.3+. Buildpacks: einfacher, aber weniger transparent.

---

## Tipps

- **Trivy** für Image-Vulnerability-Scanning in der CI-Pipeline: `trivy image my-image:latest`.
- **Distroless Images** (gcr.io/distroless/java21) für minimale Angriffsfläche — kein Shell, kein Package-Manager.
- **Image-Tag = Git-SHA** (→ ADR-036): niemals `latest` in Produktion.
 