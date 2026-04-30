# ADR-108 — Developer Experience & Golden Path: Platform Engineering

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | CTO / Head of Platform / VP Engineering                       |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Platform Engineering · Developer Experience · Produktivität   |
| Betroffene Teams  | Platform-Team (baut), alle Engineering-Teams (nutzen)         |
| Abhängigkeiten    | ADR-085 (Team Topologies), ADR-092 (Cloud-Native), ADR-100 (Helm)|

---

## 1. Kontext & Treiber

### 1.1 Das Produktivitätsproblem ohne Platform Engineering

```
OHNE GOLDEN PATH: jedes Team löst dieselben Probleme neu

Team A (Orders): 2 Wochen für lokales Dev-Setup
Team B (Payments): 3 Wochen für lokales Dev-Setup (anderer Ansatz)
Team C (Inventory): 1 Woche (hat von Team B gelernt)

Jedes Team baut:
  ✗ eigene Docker-Compose-Konfiguration
  ✗ eigene CI/CD-Pipeline (Copy-Paste mit kleinen Unterschieden)
  ✗ eigene Monitoring-Dashboards
  ✗ eigene Datenbank-Migration-Strategie
  ✗ eigene Secret-Verwaltung

ERGEBNIS:
  → Neue Entwickler-Onboarding: 2-4 Wochen bis produktiv
  → Security-Lücken weil jedes Team anders
  → 30-40% der Engineering-Zeit für Infrastruktur statt Features
  → Inkonsistente Erfahrung zwischen Teams

DORA-Forschung:
  Teams mit guter Developer Experience deployen 5× häufiger.
  Teams die auf Infrastruktur warten: DORA Low-Performer.
```

### 1.2 Der Golden Path

```
GOLDEN PATH (auch: Paved Road):
  Der empfohlene, vorgefertigte Weg für neue Services.
  
  "Wenn du einem neuen Service erstellst, geh diesen Weg.
   Er ist vorgefertigt, gut dokumentiert, sicher und produktiv.
   Du kannst abweichen, aber du weißt warum."
   
  Netflix Paved Road: "We build the road. You decide if you walk it."
  Spotify Golden Path: "The happy path for building microservices."

NICHT: Zwang zu einem einzigen Weg
SONDERN: Der beste Weg soll der einfachste sein
```

---

## 2. Entscheidung

Das Platform-Team baut und pflegt einen Golden Path für alle Standard-Anwendungsfälle: Service-Scaffolding, lokale Entwicklungsumgebung, CI/CD-Templates, Deployment-Manifeste, Monitoring. Entwicklungsteams können diesen Weg nutzen ohne Platform-Tickets oder Meetings.

---

## 3. Komponenten des Golden Path

### 3.1 Service-Scaffolding: Neuer Service in 5 Minuten

```bash
# Neuen Service erstellen mit Platform-CLI
platform-cli new service \
  --name inventory-service \
  --type java-spring-boot \
  --team inventory-team \
  --database postgres \
  --messaging kafka

# Was das erzeugt (innerhalb von 60 Sekunden):
# ✅ Git-Repository mit korrekter Struktur
# ✅ build.gradle.kts mit allen Standard-Dependencies
# ✅ .gitlab-ci.yml mit vollständiger Pipeline (→ ADR-080)
# ✅ Dockerfile (Multi-Stage, non-root → ADR-037)
# ✅ Kubernetes-Manifeste (Kustomize Base + Overlays → ADR-100)
# ✅ application.yml mit Standards (Actuator, Logging, Tracing)
# ✅ SecurityConfig (OAuth2 Resource Server → ADR-101)
# ✅ Logback-Konfiguration (strukturiert, PII-Masking → ADR-106)
# ✅ GlobalExceptionHandler (Problem Details → ADR-086)
# ✅ OpenAPI-Grundstruktur (→ ADR-066)
# ✅ ArchUnit-Tests (→ ADR-061)
# ✅ Testcontainers-Konfiguration (→ ADR-018)
```

```python
# platform-cli Implementierung (vereinfacht)
# tools/platform-cli/src/scaffolding.py

import typer
from jinja2 import Environment, FileSystemLoader

app = typer.Typer()

@app.command()
def new_service(
    name: str,
    type: str = "java-spring-boot",
    team: str = "",
    database: str = "none",
    messaging: str = "none"
):
    """Erstellt einen neuen Service nach dem Golden Path."""

    context = {
        "service_name": name,
        "service_name_pascal": to_pascal_case(name),
        "team": team,
        "has_database": database != "none",
        "database_type": database,
        "has_messaging": messaging != "none",
        "messaging_type": messaging,
        "java_version": "21",
        "spring_boot_version": "3.3.0",
    }

    # Templates rendern
    templates_dir = Path("templates") / type
    env = Environment(loader=FileSystemLoader(str(templates_dir)))

    output_dir = Path(name)
    output_dir.mkdir(exist_ok=True)

    for template_file in templates_dir.rglob("*.j2"):
        template = env.get_template(str(template_file.relative_to(templates_dir)))
        output_path = output_dir / template_file.with_suffix("").relative_to(templates_dir)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(template.render(**context))

    typer.echo(f"✅ Service '{name}' erstellt!")
    typer.echo(f"   cd {name} && git init && git add . && git commit -m 'Initial commit'")
    typer.echo(f"   Dann: push zu GitLab → Pipeline startet automatisch")
```

### 3.2 Template: Standard build.gradle.kts

```kotlin
// templates/java-spring-boot/build.gradle.kts.j2
plugins {
    id("org.springframework.boot") version "{{ spring_boot_version }}"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"  // Optional
    id("org.openapi.generator") version "7.4.0"
    id("info.solidsoft.pitest") version "1.15.0"
    id("org.cyclonedx.bom") version "1.8.2"
{% if has_database %}    id("org.flywaydb.flyway") version "10.7.0"{% endif %}
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of({{ java_version }})) } }

dependencies {
    // Spring Boot Core
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")

{% if has_database %}
    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
{% endif %}

{% if has_messaging %}
    // Messaging
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.confluent:kafka-avro-serializer:7.5.0")
{% endif %}

    // Observability
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
{% if has_database %}    testImplementation("org.testcontainers:postgresql"){% endif %}
{% if has_messaging %}    testImplementation("org.testcontainers:kafka"){% endif %}
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}
```

### 3.3 Lokale Entwicklung: Ein Befehl

```yaml
# docker-compose.yml (generiert durch platform-cli)
# Ziel: `docker-compose up` → alles läuft lokal

services:
  # Die eigene Anwendung
  {{ service_name }}:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/{{ service_name }}
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4317
    depends_on:
      db:
        condition: service_healthy
      kafka:
        condition: service_healthy

  # Infrastruktur-Services (Platform-Standards)
{% if has_database %}
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: {{ service_name }}
      POSTGRES_PASSWORD: local-dev-password
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "postgres"]
      interval: 5s
{% endif %}

{% if has_messaging %}
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_KRAFT_MODE: "true"
      CLUSTER_ID: "local-dev-cluster"
    healthcheck:
      test: ["CMD", "kafka-topics", "--bootstrap-server", "localhost:9092", "--list"]
{% endif %}

  # Optionales lokales Monitoring (schnelles Feedback)
  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "16686:16686"  # Jaeger UI: http://localhost:16686

  otel-collector:
    image: otel/opentelemetry-collector-contrib:latest
    volumes:
      - ./config/otel-local.yaml:/etc/otel/config.yaml
```

---

## 4. Self-Service-Portal: Internal Developer Portal

```
INTERNAL DEVELOPER PORTAL (IDP):
  Backstage (Spotify) oder ähnlich.
  
  Was es bietet:
  ✅ Service-Katalog: alle Services, Teams, APIs, Docs
  ✅ Tech Docs: automatisch generiert (aus Code → ADR-093)
  ✅ Templates: neuer Service mit einem Klick
  ✅ Cost Visibility: meine Services kosten X EUR/Monat
  ✅ Security Scores: Vulnerability-Status meiner Services
  ✅ Deployment Status: wo ist Version X deployed?

BACKSTAGE-KONFIGURATION:
```

```yaml
# backstage/catalog-info.yaml (in jedem Service-Repository)
apiVersion: backstage.io/v1alpha1
kind: Component
metadata:
  name: order-service
  description: Bestellverwaltung — Kernservice des E-Commerce-Systems
  annotations:
    # Automatische Verknüpfungen:
    gitlab.com/project-slug: example/order-service
    backstage.io/techdocs-ref: dir:.           # Tech Docs aus docs/
    prometheus.io/rule: "..."
  tags:
    - java
    - spring-boot
    - kafka
    - postgresql
  links:
    - url: https://grafana.example.com/d/order-service
      title: Grafana Dashboard
    - url: https://api.example.com/v2/docs
      title: API Documentation

spec:
  type: service
  lifecycle: production
  owner: orders-team
  system: e-commerce
  dependsOn:
    - component:inventory-service
    - resource:order-database
    - resource:kafka-cluster
  providesApis:
    - orders-api
```

---

## 5. Developer Experience Metriken

```python
# Messen ob der Golden Path tatsächlich Zeit spart
# DORA + Developer-Experience-spezifische Metriken

METRIKEN:
  ① Onboarding-Zeit bis erster PR:
     Ziel: < 1 Tag (mit Golden Path)
     Messung: Datum Zugang zu Repo → Datum erster merged PR

  ② Zeit von "Idee" bis "in Staging deployed":
     Ziel: < 30 Minuten für Standard-Feature
     Messung: Commit-Timestamp bis ArgoCD-Sync

  ③ Lokale Build-Zeit:
     Ziel: < 3 Minuten (Hot-Reload: < 10 Sekunden)
     Messung: Gradle Build Scan Daten

  ④ CI-Pipeline-Dauer:
     Ziel: < 10 Minuten bis "grün" für Feature-Branches
     Messung: GitLab Pipeline-Analytics

  ⑤ Developer Satisfaction Score (DORA Metric #5):
     Vierteljährliche Umfrage: "Wie zufrieden sind Sie mit Ihren Tools?"
     Skala 1-5, Ziel: ≥ 4.0
```

---

## 6. Platform-Team als Product-Team

```
ANTI-PATTERN: Platform-Team als Support-Team
  → Teams erstellen Jira-Tickets für jede Infrastruktur-Anfrage
  → Platform-Team hat 50 offene Tickets
  → Durchschnittliche Wartezeit: 3 Tage
  → Platform-Team ist Bottleneck (→ ADR-085)

RICHTIG: Platform-Team als internes Product-Team
  → Kunden sind die Entwickler-Teams
  → Product Roadmap: was brauchen Teams am meisten?
  → Self-Service: Teams können deployen ohne Ticket
  → Office Hours statt Ticket-Queue (→ ADR-085 Team-API)
  → Erfolg gemessen an: Developer Satisfaction, Time-to-Deploy

PLATFORM TEAM OKRs BEISPIEL:
  Objective: "Entwickler-Teams verbringen < 20% ihrer Zeit mit Infrastruktur"
  Key Result 1: Onboarding-Zeit neuer Services: < 1 Tag (aktuell: 1 Woche)
  Key Result 2: Developer Satisfaction Score ≥ 4.2 (aktuell: 3.1)
  Key Result 3: Platform-Support-Tickets: < 5/Woche (aktuell: 35/Woche)
```

---

## 7. Akzeptanzkriterien

- [ ] `platform-cli new service` erstellt produktionsbereiten Service-Scaffolding in < 5 Minuten
- [ ] `docker-compose up` funktioniert für alle generierten Services ohne Anpassung
- [ ] Backstage/IDP ist deployed und zeigt alle Services mit Team-Ownership
- [ ] Onboarding neuer Entwickler: erster produktiver PR innerhalb 1 Tag (gemessen)
- [ ] Developer Satisfaction Score ≥ 4.0 in quartalsweiser Umfrage
- [ ] Platform-Tickets < 10/Woche (Self-Service löst den Rest)

---

## Quellen & Referenzen

- **Evan Bottcher, "What I Talk About When I Talk About Platforms" (2018)** — Golden Path als Konzept.
- **Matthew Skelton & Manuel Pais, "Team Topologies" (2019)** — Platform-Team als Enabler.
- **Spotify Engineering, "Golden Path" Blog Posts (2020-2022)** — Praxiserfahrungen.
- **DORA Research, "State of DevOps 2023"** — Developer Experience als Produktivitätsfaktor.
- **Backstage Documentation** — Internal Developer Portal Framework.

---

## Verwandte ADRs

- [ADR-085](ADR-085-sozio-technische-systeme.md) — Team Topologies: Platform-Team-Rolle
- [ADR-036](ADR-036-devops-cicd.md) — CI/CD-Templates im Golden Path
- [ADR-092](ADR-092-isaqb-cloud-native.md) — Cloud-Native Tooling im Golden Path
- [ADR-093](ADR-093-isaqb-living-documentation.md) — Tech Docs automatisch generiert
