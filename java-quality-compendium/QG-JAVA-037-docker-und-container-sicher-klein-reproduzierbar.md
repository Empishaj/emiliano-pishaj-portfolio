# QG-JAVA-037 — Docker & Container: sicher, klein, reproduzierbar

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline / DevSecOps- und Containerisierungsstandard |
| ID | QG-JAVA-037 |
| Titel | Docker & Container: sicher, klein, reproduzierbar |
| Status | Accepted / verbindlicher Standard für neue Java-/Spring-Boot-Container-Images |
| Sprache | Deutsch |
| Kategorie | DevOps / Container / Software Supply Chain / Runtime Security |
| Zielgruppe | Java-Entwickler, Tech Leads, DevOps Engineers, Plattformverantwortliche, Security Reviewer, QA |
| Java-Baseline | Java 21 LTS |
| Spring-Baseline | Spring Boot 3.x |
| Container-Baseline | Docker Engine / OCI-kompatible Container Images / BuildKit empfohlen |
| Empfohlene Build-Strategien | Multi-Stage-Dockerfile oder Spring Boot Buildpacks, abhängig vom benötigten Kontrollgrad |
| Verbindlichkeit | Neue Java-/Spring-Boot-Images MÜSSEN klein, reproduzierbar, nicht-root, scanbar und ohne Build-Tools im Runtime-Image gebaut werden |
| Letzte fachliche Prüfung | 2026-05-02 |
| Validierungsstatus | Multi-Stage Builds, Dockerfile-Best-Practices, Spring Boot Layered JARs, Buildpacks, Dockerfile-Referenz, OWASP Docker Security Cheat Sheet und Trivy-Dokumentation wurden geprüft |
| Abweichungen | Abweichungen sind zulässig, wenn sie technisch begründet, dokumentiert und im Review freigegeben sind |
| Nicht-Ziel | Diese Richtlinie ersetzt keine Kubernetes-Produktionsrichtlinie, keine vollständige CIS-Benchmark-Umsetzung, keine Registry-Governance und kein Organisationsstandard für Signaturen, SBOMs oder Runtime Protection |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Java- und Spring-Boot-Anwendungen als Docker-/OCI-Images gebaut werden, die klein, reproduzierbar, sicher, wartbar und für CI/CD geeignet sind. Ziel ist nicht nur, dass ein Container startet. Ziel ist ein Container-Image, das im Betrieb nachvollziehbar ist, keine unnötigen Werkzeuge enthält, keine Secrets mitbringt, nicht als Root läuft, mit Container-Ressourcen korrekt umgeht und durch automatisierte Prüfungen kontrollierbar bleibt.

Containerisierung ist ein Qualitätsmerkmal, weil sie die Grenze zwischen Anwendung, Build, Laufzeit, Betrieb und Security sichtbar macht. Ein schlechtes Dockerfile kann Quellcode, Build-Tools, Testdaten, `.env`-Dateien, Root-Rechte, Shells, Paketmanager und unnötige Betriebssystempakete in Produktion bringen. Das ist kein kosmetisches Problem. Es vergrößert die Angriffsfläche, verschlechtert Reproduzierbarkeit, erschwert Scans und erhöht das Risiko von Laufzeitfehlern.

Diese Richtlinie legt fest, wie Container-Images für Java 21 und Spring Boot 3.x gebaut, benannt, geprüft, abgesichert und betrieben werden.

---

## 2. Kurzregel für Entwickler

Baue Java-/Spring-Boot-Container-Images grundsätzlich mit Multi-Stage Builds oder Spring Boot Buildpacks. Das Runtime-Image DARF keine Build-Tools, keinen Quellcode, keine Testabhängigkeiten, keine Secrets und keinen unnötigen Paketmanager enthalten. Der Container MUSS als Non-Root-User laufen. Image-Tags MÜSSEN reproduzierbar und eindeutig sein; `latest` ist für produktive Deployments verboten. Images MÜSSEN in CI/CD auf bekannte Schwachstellen und Fehlkonfigurationen geprüft werden.

Für neue Dockerfiles gilt:

```text
Builder Stage: JDK, Gradle/Maven, Quellcode, Tests/Build
Runtime Stage: JRE oder minimales Runtime-Image, fertiges Artefakt, Non-Root-User
```

Nicht erlaubt ist:

```dockerfile
FROM eclipse-temurin:21-jdk
COPY . .
RUN ./gradlew build
CMD ["java", "-jar", "build/libs/app.jar"]
```

Dieser Ansatz enthält Build-Tools, Quellcode und unnötige Abhängigkeiten im Produktionsimage.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Java-21-Anwendungen
- Spring-Boot-3.x-Anwendungen
- REST-Services
- Worker
- Batch-Anwendungen
- interne Plattformservices
- lokale Entwicklungsimages
- CI/CD-Build-Images
- Images für Docker Compose, Kubernetes oder andere OCI-kompatible Runtimes

Sie gilt insbesondere für:

- Dockerfiles
- `.dockerignore`
- Multi-Stage Builds
- Spring Boot Layered JARs
- Spring Boot Buildpacks
- Runtime-Images
- Non-Root-User
- JVM-Container-Konfiguration
- Health Checks
- Image-Scanning
- Image-Tags
- Container-Security-Basics

Sie gilt nicht als vollständiger Ersatz für:

- Kubernetes SecurityContext-Standards
- Pod Security Admission
- Network Policies
- Registry-Zugriffskontrolle
- SBOM-/Signatur-Governance
- Runtime Detection
- vollständige Supply-Chain-Security nach SLSA
- vollständige CIS-Docker-Benchmark-Umsetzung

---

## 4. Technischer Hintergrund

Docker-Images bestehen aus Layern. Jede Dockerfile-Instruktion erzeugt einen neuen Layer oder beeinflusst Metadaten. Gute Dockerfiles nutzen diese Layer bewusst: selten geänderte Abhängigkeiten werden vor häufig geändertem Anwendungscode kopiert, Build-Werkzeuge bleiben in der Builder Stage, und das Runtime-Image enthält nur das, was zur Ausführung wirklich benötigt wird.

Multi-Stage Builds nutzen mehrere `FROM`-Anweisungen. Jede Stage kann ein anderes Basisimage verwenden. Artefakte werden gezielt aus einer Build-Stage in eine Runtime-Stage kopiert. Docker beschreibt Multi-Stage Builds ausdrücklich als Mittel, um Build-Umgebung und Runtime-Artefakt sauber zu trennen und nur die benötigten Dateien in das finale Image zu übernehmen.

Spring Boot unterstützt zusätzlich Layered JARs. Das bedeutet: Abhängigkeiten, Spring-Boot-Loader, Snapshot-Abhängigkeiten und Anwendungscode können getrennt in Container-Layer gelegt werden. Dadurch müssen bei einer normalen Codeänderung nicht alle Abhängigkeiten erneut in das Image geschrieben werden.

Buildpacks sind eine Alternative zum eigenen Dockerfile. Spring Boot kann über `bootBuildImage` OCI-Images erzeugen. Paketo Buildpacks konfigurieren Java-Runtime und Layering automatisch und erzeugen Images, die typischerweise ohne Root-Rechte laufen. Buildpacks reduzieren Wartungsaufwand, nehmen dem Team aber einen Teil der direkten Dockerfile-Kontrolle.

---

## 5. Verbindlicher Standard

Für neue Java-/Spring-Boot-Images gilt:

| Regel | Verbindlichkeit | Begründung |
|---|---:|---|
| Multi-Stage Build oder Buildpacks verwenden | MUSS | Build-Tools und Quellcode dürfen nicht ins Runtime-Image |
| Non-Root-User verwenden | MUSS | Root im Container erhöht Schadwirkung bei Ausbruch oder Fehlkonfiguration |
| `.dockerignore` pflegen | MUSS | Build-Kontext muss klein und secret-frei bleiben |
| Keine Secrets in Image, Layern oder ENV | MUSS | Images werden verteilt, gecached, gescannt und gespeichert |
| Image-Scanning in CI/CD | MUSS | CVEs und Fehlkonfigurationen müssen vor Deployment sichtbar werden |
| `latest` in Produktion vermeiden | MUSS | Deployments müssen reproduzierbar sein |
| Runtime-Image klein halten | MUSS | Weniger Pakete bedeuten weniger Angriffsfläche und schnellere Distribution |
| JVM-Container-Ressourcen bewusst konfigurieren | MUSS | Heap darf Container-Limit nicht verdrängen |
| Health/Readiness über passende Plattformmechanismen | MUSS | Containerstart ist nicht gleich Anwendungsbereitschaft |
| Labels und Metadaten setzen | SOLLTE | Nachvollziehbarkeit und Supply-Chain-Transparenz verbessern sich |
| Distroless/Minimal Images prüfen | SOLLTE | Angriffsfläche kann weiter sinken, Debugging wird aber schwerer |
| BuildKit Cache Mounts nutzen | SOLLTE | Builds werden schneller und reproduzierbarer |

---

## 6. Schlechte Anwendung: Fat Image mit Build-Tools im Runtime-Image

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . .
RUN ./gradlew build

EXPOSE 8080
CMD ["java", "-jar", "build/libs/app.jar"]
```

Dieses Dockerfile ist für produktive Images ungeeignet.

Problematisch ist:

- Das Image enthält ein JDK statt einer Runtime.
- Das Image enthält Gradle, Wrapper, Build-Dateien, Quellcode und eventuell Tests.
- Der gesamte Build-Kontext wird kopiert.
- Ohne `.dockerignore` können `.git`, `.env`, lokale Dateien und Logs im Image landen.
- Build und Runtime sind nicht getrennt.
- Image-Layer enthalten potenziell Informationen, die später nicht mehr sichtbar, aber weiterhin im Image vorhanden sind.
- Der Container läuft wahrscheinlich als Root.
- Das Image ist groß, schwer scanbar und schlecht reproduzierbar.

---

## 7. Gute Anwendung: Multi-Stage Dockerfile für Spring Boot

Das folgende Beispiel ist ein produktionsnaher Standard für Gradle-basierte Spring-Boot-3.x-Anwendungen.

```dockerfile
# syntax=docker/dockerfile:1.7

# ─────────────────────────────────────────────────────────────────────────────
# Builder Stage
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Build-Dateien zuerst kopieren, damit Dependency-Layer gecached werden können.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

# Dependency-Auflösung separat für besseres Layer-Caching.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# Anwendungscode danach kopieren.
COPY src ./src

# Tests sollten in CI vorher laufen. Für Image-Build nur bootJar erzeugen.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -x test

# Spring Boot Layered JAR extrahieren.
RUN java -Djarmode=layertools \
    -jar build/libs/*.jar \
    extract --destination /workspace/layers

# ─────────────────────────────────────────────────────────────────────────────
# Runtime Stage
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre AS runtime

# OCI-Metadaten für Nachvollziehbarkeit.
LABEL org.opencontainers.image.title="order-service" \
      org.opencontainers.image.description="Spring Boot Order Service" \
      org.opencontainers.image.vendor="Example Organization" \
      org.opencontainers.image.source="https://example.invalid/repository" \
      org.opencontainers.image.licenses="UNSPECIFIED"

# Non-Root-User mit stabiler UID/GID.
RUN groupadd --system --gid 10001 app \
 && useradd  --system --uid 10001 --gid app --home-dir /app --shell /usr/sbin/nologin app

WORKDIR /app

# Nur extrahierte Runtime-Layer kopieren, nicht Quellcode und nicht Build-Tools.
COPY --from=builder --chown=10001:10001 /workspace/layers/dependencies/ ./
COPY --from=builder --chown=10001:10001 /workspace/layers/spring-boot-loader/ ./
COPY --from=builder --chown=10001:10001 /workspace/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=10001:10001 /workspace/layers/application/ ./

USER 10001:10001

EXPOSE 8080

# Java-Optionen über JAVA_TOOL_OPTIONS steuerbar halten.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.io.tmpdir=/tmp"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Dieses Dockerfile trennt Build und Runtime, nutzt Layer-Caching, entfernt Build-Tools aus dem finalen Image, verwendet einen Non-Root-User und vermeidet direkte Kopie des gesamten Projekts in die Runtime.

---

## 8. Warum Alpine nicht automatisch der beste Standard ist

Viele Dockerfile-Beispiele verwenden Alpine, weil Alpine-Images klein sind. Für Java-/Spring-Boot-Anwendungen ist Alpine aber nicht automatisch die beste Wahl. Alpine verwendet `musl` statt `glibc`. Das kann bei nativen Bibliotheken, Monitoring-Agenten, DNS-Verhalten, Fonts, TLS-/CA-Themen, Performance-Diagnose oder bestimmten JVM-/JNI-Abhängigkeiten zu unerwartetem Verhalten führen.

Deshalb gilt:

```text
Alpine ist erlaubt, aber nicht der Default.
```

Alpine DARF verwendet werden, wenn:

- die Anwendung keine problematischen nativen Abhängigkeiten nutzt,
- TLS, DNS, Encoding, Zeitzonen und Monitoring-Agenten getestet wurden,
- Vulnerability-Scanning und Betriebserfahrung passen,
- Debugging- und Supportfähigkeit geklärt sind.

Für Standard-Spring-Boot-Services ist ein Debian-/Ubuntu-basiertes Temurin-, Distroless-, Paketo- oder organisationsfreigegebenes Runtime-Image häufig wartbarer als ein minimaler Alpine-Ansatz.

---

## 9. `.dockerignore`: Build-Kontext minimieren

Eine gute `.dockerignore` ist Pflicht. Sie verhindert, dass unnötige oder gefährliche Dateien in den Build-Kontext gelangen.

```dockerignore
.git
.gitignore
.gradle
build/
out/
target/

# IDE / lokale Dateien
.idea/
.vscode/
*.iml

# Dokumentation und lokale Artefakte
README.md
docs/
*.log
*.tmp

# Secrets und lokale Konfiguration
.env
.env.*
*.pem
*.key
*.p12
*.jks
*.keystore

# Compose- und lokale Docker-Dateien, sofern nicht bewusst benötigt
docker-compose*.yml
compose*.yml

# Tests nur ausschließen, wenn Tests nicht im Docker-Build laufen sollen
src/test/
```

Wichtig: Wenn Tests im Docker-Build laufen sollen, darf `src/test/` nicht ignoriert werden. Diese Entscheidung muss zum Build-Modell passen. In vielen CI/CD-Pipelines laufen Tests vor dem Image-Build; dann ist es sinnvoll, `src/test/` aus dem Image-Build-Kontext auszuschließen.

---

## 10. JVM-Container-Awareness

Java 21 ist container-aware. Die JVM erkennt Container-Ressourcen grundsätzlich und `-XX:+UseContainerSupport` ist in modernen JVMs standardmäßig aktiv. Trotzdem muss das Team bewusst entscheiden, wie viel Container-Speicher für Heap, Metaspace, Thread Stacks, Direct Memory, Code Cache, Native Libraries, GC und Betriebssystemanteile frei bleiben soll.

Empfohlener Standard:

```dockerfile
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.io.tmpdir=/tmp"
```

Diese Einstellung bedeutet nicht, dass 75 % immer richtig sind. Sie ist ein Startwert für typische Spring-Boot-Services. Bei speicherintensiven Anwendungen, vielen Threads, Netty, großen Buffers, Caches, ML-Bibliotheken oder vielen nativen Abhängigkeiten muss dieser Wert gemessen und angepasst werden.

Nicht empfohlen als blinder Standard:

```dockerfile
# ❌ Meist redundant bei Java 21
-XX:+UseContainerSupport

# ❌ Meist redundant, weil G1 bei modernen Server-JVMs häufig Standard ist
-XX:+UseG1GC

# ❌ In modernen JVMs normalerweise nicht mehr nötig
-Djava.security.egd=file:/dev/./urandom
```

Diese Optionen sind nicht grundsätzlich verboten. Sie dürfen aber nicht als Cargo-Cult-Flags in jedes Dockerfile kopiert werden. Jede JVM-Option muss entweder einen messbaren Zweck haben oder aus einem dokumentierten Plattformstandard stammen.

---

## 11. Heap nicht gleich Container-Limit setzen

Ein häufiger Fehler ist, den gesamten Container-Speicher als Java-Heap zu verwenden.

```dockerfile
# ❌ Gefährlich, wenn Container nur 512 MiB hat
ENV JAVA_TOOL_OPTIONS="-Xmx512m"
```

Wenn der Container ein Limit von 512 MiB hat und der Heap 512 MiB verwenden darf, bleibt kein ausreichender Platz für Metaspace, Thread Stacks, Direct Memory, Native Memory, TLS, Kompression, JIT und Betriebssystemanteile. Das kann zu OOM-Kills führen, obwohl der Heap selbst noch plausibel aussieht.

Besser:

```dockerfile
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0"
```

Zusätzlich müssen Container-Limits in der Zielplattform gesetzt werden. Ein Container ohne Memory-Limit kann auf Host-Ebene gefährlich werden.

---

## 12. Non-Root-Container

Container MÜSSEN als Non-Root-User laufen.

Gutes Beispiel für Debian-/Ubuntu-basierte Runtime-Images:

```dockerfile
RUN groupadd --system --gid 10001 app \
 && useradd  --system --uid 10001 --gid app --home-dir /app --shell /usr/sbin/nologin app

WORKDIR /app
COPY --chown=10001:10001 app.jar ./app.jar

USER 10001:10001
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Schlecht:

```dockerfile
# ❌ Kein USER gesetzt: Container läuft häufig als root
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Non-Root ist kein vollständiger Schutz. Es reduziert aber die Schadwirkung bei Schwachstellen, Fehlkonfigurationen und ausbrechenden Prozessen.

---

## 13. Read-only Filesystem und Schreibverzeichnisse

Runtime-Container sollen so gebaut sein, dass das Root-Filesystem read-only laufen kann. Die Anwendung darf nur in explizit vorgesehene Verzeichnisse schreiben, typischerweise `/tmp` oder gemountete Volumes.

Docker-Compose-Beispiel:

```yaml
services:
  order-service:
    image: registry.example.com/order-service:1.7.3-a1b2c3d
    read_only: true
    tmpfs:
      - /tmp:size=64m,mode=1777
    security_opt:
      - no-new-privileges:true
    cap_drop:
      - ALL
    ports:
      - "8080:8080"
```

Kubernetes-Beispiel:

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 10001
  runAsGroup: 10001
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
volumeMounts:
  - name: tmp
    mountPath: /tmp
volumes:
  - name: tmp
    emptyDir:
      sizeLimit: 64Mi
```

Diese Einstellungen gehören nicht zwingend ins Dockerfile, sondern oft in die Laufzeitplattform. Das Image muss aber dafür vorbereitet sein.

---

## 14. Health Check: Docker, Compose und Kubernetes unterscheiden

Ein Container kann gestartet sein, obwohl die Anwendung noch nicht bereit ist. Deshalb braucht es Health- und Readiness-Prüfungen.

Für Docker/Compose kann ein Dockerfile-`HEALTHCHECK` sinnvoll sein:

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1
```

Aber: Minimal-Images oder Distroless-Images enthalten häufig weder `curl` noch `wget`. Dann ist ein Dockerfile-Healthcheck oft nicht sinnvoll, weil er zusätzliche Tools ins Image bringen würde.

Für Kubernetes gilt: Liveness-, Readiness- und Startup-Probes sollen im Kubernetes-Manifest definiert werden, nicht über einen Dockerfile-Healthcheck.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 10

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
```

Spring Boot Actuator muss so konfiguriert werden, dass nur geeignete Health-Endpunkte öffentlich oder intern verfügbar sind. Health Checks dürfen keine Secrets, Datenbankdetails oder interne Topologieinformationen preisgeben.

---

## 15. Spring Boot Buildpacks als Alternative

Spring Boot kann Container-Images ohne eigenes Dockerfile erzeugen.

Gradle-Beispiel:

```kotlin
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName.set("registry.example.com/order-service:${project.version}")
    environment.set(
        mapOf(
            "BP_JVM_VERSION" to "21",
            "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
            "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:MaxRAMPercentage=75.0"
        )
    )
}
```

Buildpacks sind besonders geeignet, wenn:

- das Team keinen speziellen Dockerfile-Kontrollbedarf hat,
- Standard-Java-/Spring-Boot-Images reichen,
- Layering, JVM-Konfiguration und Non-Root-Ausführung automatisiert werden sollen,
- Plattformteams einheitliche Builder freigeben.

Ein eigenes Dockerfile ist sinnvoller, wenn:

- besondere native Abhängigkeiten benötigt werden,
- zusätzliche Zertifikate oder Betriebssystempakete notwendig sind,
- ein stark kontrolliertes Runtime-Image erforderlich ist,
- Distroless, jlink oder spezielle Hardening-Vorgaben umgesetzt werden müssen.

---

## 16. Distroless und minimale Images

Distroless-Images reduzieren die Angriffsfläche, weil sie keine Shell, keinen Paketmanager und nur minimale Runtime-Komponenten enthalten. Das ist sicherheitlich attraktiv, erschwert aber Debugging und Diagnose.

Distroless ist geeignet, wenn:

- die Anwendung reif und gut beobachtbar ist,
- Debugging nicht im Container erfolgen muss,
- Health Checks über Plattform-Probes laufen,
- Zertifikate, Zeitzonen, Fonts und Native Libraries geprüft wurden,
- Security- und Plattformteam den Betrieb unterstützen.

Distroless ist nicht geeignet, wenn:

- Entwickler regelmäßig im Container debuggen müssen,
- unklare native Abhängigkeiten bestehen,
- Health Checks Shell-Tools voraussetzen,
- die Organisation keine Erfahrung mit minimalen Runtime-Images hat.

Regel: Minimal ist gut, aber nicht auf Kosten von Betriebsfähigkeit.

---

## 17. Image-Tags und Reproduzierbarkeit

`latest` ist in Produktion verboten.

Schlecht:

```bash
docker run registry.example.com/order-service:latest
```

Gut:

```bash
docker run registry.example.com/order-service:1.7.3-a1b2c3d
```

Empfohlene Tag-Strategie:

```text
<semver>-<git-sha>
1.7.3-a1b2c3d
```

Zusätzlich kann das Deployment intern über Image Digest fixiert werden:

```text
registry.example.com/order-service@sha256:<digest>
```

SemVer beschreibt die fachliche Version. Git-SHA beschreibt den konkreten Source-Zustand. Digest beschreibt das exakte Image-Artefakt.

---

## 18. OCI Labels und Nachvollziehbarkeit

Images SOLLTEN OCI-Labels enthalten:

```dockerfile
LABEL org.opencontainers.image.title="order-service" \
      org.opencontainers.image.description="Spring Boot Order Service" \
      org.opencontainers.image.version="1.7.3" \
      org.opencontainers.image.revision="a1b2c3d" \
      org.opencontainers.image.created="2026-05-02T12:00:00Z" \
      org.opencontainers.image.source="https://example.invalid/repository" \
      org.opencontainers.image.vendor="Example Organization"
```

Achtung: Für reproduzierbare Builds kann ein dynamisches `created`-Label problematisch sein. In streng reproduzierbaren Build-Prozessen wird der Zeitstempel kontrolliert oder weggelassen.

---

## 19. Secrets dürfen niemals ins Image

Nicht erlaubt:

```dockerfile
# ❌ Secret im Image-Layer
ENV DB_PASSWORD=super-secret

# ❌ Secret wird über Build-Argument in Layer-Historie sichtbar
ARG NPM_TOKEN
RUN echo $NPM_TOKEN

# ❌ lokale .env-Datei im Build-Kontext
COPY .env /app/.env
```

Erlaubt ist:

- Secrets zur Laufzeit über Secret Store, Vault, Kubernetes Secrets oder Plattformmechanismus bereitstellen.
- BuildKit Secrets für Build-Zeit-Zugriffe verwenden, ohne Secrets in Layer zu schreiben.

Beispiel mit BuildKit Secret:

```dockerfile
# syntax=docker/dockerfile:1.7
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    ./mvnw -B package
```

Build-Aufruf:

```bash
docker build \
  --secret id=maven_settings,src=$HOME/.m2/settings.xml \
  -t registry.example.com/order-service:1.7.3-a1b2c3d .
```

---

## 20. Security-Anti-Patterns

### 20.1 Root-Container

```dockerfile
# ❌ Kein USER gesetzt
FROM eclipse-temurin:21-jre
COPY app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Korrektur: Non-Root-User anlegen und verwenden.

### 20.2 `--privileged`

```bash
# ❌ Fast nie für Anwendungscontainer erlaubt
docker run --privileged order-service
```

`--privileged` hebt viele Sicherheitsgrenzen auf. Für normale Java-Anwendungen ist das verboten.

### 20.3 Docker Socket mounten

```yaml
# ❌ Container kann Host-Docker kontrollieren
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

Der Docker-Socket entspricht praktisch weitreichender Kontrolle über den Host. Nur dedizierte Build-/CI-Komponenten dürfen so etwas nach Plattformfreigabe verwenden.

### 20.4 Paketmanager im Runtime-Image

```dockerfile
# ❌ Debugging-Komfort im Produktionsimage
RUN apt-get update && apt-get install -y curl vim procps
```

Jedes Zusatzpaket erhöht die Angriffsfläche. Für Debugging werden separate Debug-Images, Ephemeral Containers oder Plattformmechanismen verwendet.

### 20.5 Wildes `COPY . .`

```dockerfile
# ❌ Kopiert alles
COPY . .
```

Wenn `COPY . .` verwendet wird, MUSS `.dockerignore` sauber gepflegt sein. Besser ist gezieltes Kopieren.

---

## 21. Image-Scanning mit Trivy

Container-Images MÜSSEN in CI/CD gescannt werden.

Beispiel:

```bash
trivy image \
  --severity HIGH,CRITICAL \
  --exit-code 1 \
  --ignore-unfixed \
  registry.example.com/order-service:1.7.3-a1b2c3d
```

Zusätzlich sinnvoll:

```bash
trivy fs --scanners vuln,secret,misconfig .
trivy config .
```

Trivy kann Container Images, Dateisysteme, Git-Repositories, SBOMs, Kubernetes-Konfigurationen und andere Artefakte auf Schwachstellen, Fehlkonfigurationen und Secrets prüfen. Scans ersetzen keinen Security Review, machen aber offensichtliche Risiken früh sichtbar.

---

## 22. Build-Reihenfolge und Layer-Caching

Gute Dockerfiles kopieren zuerst Dateien, die sich selten ändern.

Gut:

```dockerfile
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test
```

Schlecht:

```dockerfile
COPY . .
RUN ./gradlew bootJar --no-daemon -x test
```

Beim schlechten Muster invalidiert jede Änderung im Projekt den gesamten Build-Cache. Beim guten Muster bleiben Dependency-Layer erhalten, solange Build-Dateien unverändert sind.

---

## 23. Tests und Image-Build trennen

Tests MÜSSEN vor dem produktiven Image-Build laufen. Das Image-Build darf in CI/CD davon ausgehen, dass Unit-, Integration- und Security-Tests bereits erfolgreich waren.

Beispiel-Pipeline:

```text
1. ./gradlew clean test integrationTest
2. ./gradlew bootJar
3. docker build -t registry.example.com/order-service:<version>-<sha> .
4. trivy image ...
5. container smoke test
6. push image
7. deploy by digest/tag
```

Ein Dockerfile mit `-x test` ist nur dann zulässig, wenn die Pipeline Tests vorher verbindlich ausgeführt hat.

---

## 24. Container Smoke Test

Nach dem Build SOLLTE das Image kurz gestartet und geprüft werden.

```bash
IMAGE="registry.example.com/order-service:1.7.3-a1b2c3d"

docker run --rm -d --name order-service-test -p 18080:8080 "$IMAGE"

for i in $(seq 1 30); do
  if curl -fsS http://localhost:18080/actuator/health/readiness >/dev/null; then
    docker stop order-service-test
    exit 0
  fi
  sleep 2
done

docker logs order-service-test
docker stop order-service-test
exit 1
```

Dieser Test ersetzt keine fachlichen Tests. Er prüft, ob das Image überhaupt startet, der Port erreichbar ist und die Anwendung bereit wird.

---

## 25. Docker Compose für lokale Entwicklung

Für lokale Entwicklungsumgebungen ist Docker Compose sinnvoll. Dabei gelten andere Grenzen als für Produktionsimages. Ein lokales Compose-Setup darf Datenbanken, Broker und Mock-Services starten, aber keine produktionsähnliche Security vortäuschen.

Beispiel:

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      JAVA_TOOL_OPTIONS: "-XX:MaxRAMPercentage=75.0"
    depends_on:
      postgres:
        condition: service_healthy
    read_only: true
    tmpfs:
      - /tmp:size=64m,mode=1777
    security_opt:
      - no-new-privileges:true
    cap_drop:
      - ALL

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: app
      POSTGRES_USER: app
      POSTGRES_PASSWORD: app-local-only
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U app -d app"]
      interval: 10s
      timeout: 5s
      retries: 5
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

Lokale Passwörter wie `app-local-only` dürfen nur für lokale Entwicklungsdatenbanken verwendet werden. Sie dürfen nicht produktiven Secrets ähneln oder wiederverwendet werden.

---

## 26. Logs und temporäre Dateien

Containerisierte Anwendungen schreiben Logs grundsätzlich nach `stdout` und `stderr`. Logdateien im Container sind zu vermeiden.

Schlecht:

```properties
logging.file.name=/app/logs/application.log
```

Gut:

```properties
logging.file.name=
```

Die Laufzeitplattform sammelt Logs. Wenn GC-Logs benötigt werden, muss geklärt sein, wohin sie geschrieben werden und wie sie rotiert werden. Ein read-only Root-Filesystem erfordert ein explizites Schreibziel wie `/tmp` oder ein gemountetes Volume.

---

## 27. Ports und Netzwerk

`EXPOSE` dokumentiert den erwarteten Port, öffnet ihn aber nicht automatisch nach außen.

```dockerfile
EXPOSE 8080
```

Port-Freigaben passieren zur Laufzeit über Docker, Compose oder Kubernetes. In SaaS-Systemen dürfen interne Ports nicht unnötig nach außen veröffentlicht werden.

Schlecht:

```yaml
ports:
  - "0.0.0.0:5432:5432"
```

Besser für lokale Entwicklung:

```yaml
ports:
  - "127.0.0.1:5432:5432"
```

Damit ist die Datenbank nur lokal erreichbar.

---

## 28. Ressourcenlimits

Container müssen Ressourcenlimits bekommen, besonders in geteilten Umgebungen.

Docker-Run-Beispiel:

```bash
docker run --rm \
  --memory=512m \
  --cpus=1.0 \
  registry.example.com/order-service:1.7.3-a1b2c3d
```

Compose-Beispiel für lokale Orientierung:

```yaml
services:
  app:
    mem_limit: 512m
    cpus: 1.0
```

Kubernetes-Beispiel:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "1"
    memory: "1024Mi"
```

JVM-Heap-Konfiguration und Container-Limits müssen zusammenpassen.

---

## 29. Buildpacks vs. Dockerfile: Entscheidungsmatrix

| Aspekt | Dockerfile | Buildpacks |
|---|---|---|
| Kontrolle | Sehr hoch | Mittel |
| Wartungsaufwand | Höher | Niedriger |
| Security-Hardening | Sehr gezielt möglich | Über Builder-/Run-Image-Politik gesteuert |
| Standardisierung | Team muss Regeln pflegen | Plattform kann Builder standardisieren |
| Debugging | Transparenter | Teilweise indirekter |
| Layering | Manuell steuerbar | Automatisch |
| Non-Root | Muss selbst umgesetzt werden | Üblicherweise eingebaut |
| Spezialpakete | Einfacher einbaubar | Möglich, aber weniger direkt |

Empfehlung: Für Standard-Spring-Boot-Services sind Buildpacks eine sehr gute Option. Für stark kontrollierte Runtime-Images, Spezialabhängigkeiten oder sehr strenge Security-Anforderungen ist ein geprüftes Dockerfile sinnvoll.

---

## 30. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Build-Trennung | Gibt es Builder und Runtime Stage? | `AS builder`, `AS runtime` | Pflicht |
| Runtime-Inhalt | Enthält Runtime nur Artefakte? | kein Quellcode, kein Gradle | Pflicht |
| User | Läuft der Container non-root? | `USER 10001:10001` | Pflicht |
| Build-Kontext | Gibt es `.dockerignore`? | `.env`, `.git`, `build/` ausgeschlossen | Pflicht |
| Secrets | Sind keine Secrets im Image? | keine `.env`, keine Keys | Pflicht |
| JVM | Ist Speicher bewusst konfiguriert? | `MaxRAMPercentage` | Pflicht |
| Image-Tag | Ist der Tag eindeutig? | `1.7.3-a1b2c3d` | Pflicht |
| `latest` | Wird `latest` vermieden? | produktiv verboten | Pflicht |
| Scan | Läuft Image-Scan in CI? | Trivy/Docker Scout | Pflicht |
| Health | Gibt es sinnvolle Readiness-Prüfung? | Actuator readiness | Pflicht |
| Minimalität | Sind unnötige Tools entfernt? | kein `curl` nur für Komfort | Pflicht |
| Root FS | Kann read-only laufen? | `/tmp` explizit | Soll |
| Labels | Gibt es OCI-Labels? | Revision, Source | Soll |
| Base Image | Ist das Image freigegeben und aktuell? | Temurin/Paketo/Distroless | Pflicht |
| Plattform | Passen Dockerfile und Kubernetes/Compose zusammen? | Probes, Volumes, UID | Pflicht |

---

## 31. Automatisierbare Prüfungen

Diese Richtlinie soll in CI/CD überprüfbar sein.

Mögliche Prüfungen:

```text
- Dockerfile-Linting mit Hadolint oder vergleichbarem Tool
- Image-Scanning mit Trivy oder Docker Scout
- Secret-Scanning im Repository und Build-Kontext
- Prüfung auf USER-Anweisung im Dockerfile
- Prüfung auf verbotene Tags wie latest
- Prüfung auf COPY . . ohne robuste .dockerignore
- Prüfung auf ENV/ARG-Namen mit PASSWORD, TOKEN, SECRET, KEY
- Prüfung auf Base-Image-Allowlist
- Prüfung auf Image-Größe
- Prüfung auf OCI-Labels
- Smoke-Test nach Image-Build
```

Beispiel CI-Auszug:

```yaml
container-image:
  stage: package
  script:
    - docker build -t "$IMAGE:$VERSION-$GIT_SHA" .
    - trivy image --severity HIGH,CRITICAL --exit-code 1 "$IMAGE:$VERSION-$GIT_SHA"
    - ./scripts/container-smoke-test.sh "$IMAGE:$VERSION-$GIT_SHA"
```

---

## 32. Migration bestehender Dockerfiles

Bestehende Dockerfiles werden in sieben Schritten modernisiert.

1. Build- und Runtime-Stage trennen.
2. `.dockerignore` einführen und Secrets ausschließen.
3. Runtime-Image von JDK auf JRE, Buildpack-Run-Image, Distroless oder freigegebenes Minimalimage umstellen.
4. Non-Root-User einführen.
5. JVM-Optionen auf Container-Betrieb abstimmen.
6. Image-Scanning und Smoke-Test in CI ergänzen.
7. Image-Tagging von `latest` auf Version + Git-SHA + optional Digest umstellen.

Vorher:

```dockerfile
FROM eclipse-temurin:21-jdk
COPY . .
RUN ./gradlew build
CMD ["java", "-jar", "build/libs/app.jar"]
```

Nachher:

```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre AS runtime
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --home-dir /app --shell /usr/sbin/nologin app
WORKDIR /app
COPY --from=builder --chown=10001:10001 /workspace/build/libs/*.jar app.jar
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.io.tmpdir=/tmp"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 33. Ausnahmen

Abweichungen sind erlaubt, wenn sie begründet und überprüft sind.

Mögliche Ausnahmen:

- Build-Image wird bewusst als Tooling-Image verwendet und ist kein Runtime-Image.
- Root-Rechte sind für eine eng begrenzte technische Aufgabe nötig und durch Plattformreview freigegeben.
- Distroless ist nicht möglich, weil zertifizierte Diagnosewerkzeuge im Image vorhanden sein müssen.
- Alpine wird bewusst verwendet, weil Kompatibilität geprüft und freigegeben wurde.
- Tests laufen im Docker-Build, weil die Organisation hermetische Builds erzwingt.
- Healthcheck liegt bewusst nicht im Dockerfile, weil Kubernetes-Probes verwendet werden.

Jede Ausnahme muss dokumentieren:

```text
1. Welche Regel wird verletzt?
2. Warum ist die Abweichung nötig?
3. Welche Risiken entstehen?
4. Welche Gegenmaßnahme reduziert das Risiko?
5. Wann wird die Abweichung erneut geprüft?
```

---

## 34. Definition of Done

Ein Java-/Spring-Boot-Container-Image erfüllt diese Richtlinie, wenn:

- Build und Runtime getrennt sind oder ein freigegebener Buildpack-Prozess verwendet wird,
- das Runtime-Image keinen Quellcode und keine Build-Tools enthält,
- eine `.dockerignore` vorhanden ist,
- keine Secrets im Image, Dockerfile, Build-Kontext oder Image-Layer enthalten sind,
- der Container als Non-Root-User läuft,
- JVM-Speicherverhalten bewusst für Container konfiguriert ist,
- das Image eindeutig versioniert ist und nicht über `latest` produktiv deployed wird,
- ein Image-Scan in CI/CD läuft,
- ein Smoke-Test das gebaute Image startet,
- Health-/Readiness-Verhalten definiert ist,
- unnötige Pakete und Debug-Tools aus dem Runtime-Image entfernt sind,
- Base Image, Tagging und Security-Ausnahmen reviewbar dokumentiert sind.

---

## 35. Quellen und weiterführende Literatur

- Docker Docs — Building best practices: https://docs.docker.com/build/building/best-practices/
- Docker Docs — Multi-stage builds: https://docs.docker.com/build/building/multi-stage/
- Docker Docs — Dockerfile reference: https://docs.docker.com/reference/dockerfile/
- Spring Boot Reference — Dockerfiles and layered JARs: https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html
- Spring Boot Reference — Packaging OCI Images / Buildpacks: https://docs.spring.io/spring-boot/maven-plugin/build-image.html
- Paketo Buildpacks — Java How-To: https://paketo.io/docs/howto/java/
- OWASP Cheat Sheet Series — Docker Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html
- Trivy Documentation — Container Image Scanning: https://trivy.dev/docs/latest/guide/target/container_image/
- Trivy Documentation — CI/CD Integrations: https://trivy.dev/docs/latest/ecosystem/cicd/
- Oracle Java 21 Tool Reference — `java` command: https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html

---

## 36. Merksatz

Ein gutes Container-Image ist kein gezippter Entwicklerrechner. Es ist ein minimales, nachvollziehbares und geprüftes Laufzeitartefakt: nur die Anwendung, nur die benötigte Runtime, keine Secrets, keine Build-Werkzeuge, keine Root-Rechte, klare Ressourcen, eindeutige Version und automatisierte Sicherheitsprüfung.
