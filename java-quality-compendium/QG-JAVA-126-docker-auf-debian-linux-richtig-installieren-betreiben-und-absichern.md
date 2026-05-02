# QG-JAVA-013 — Docker auf Debian/Linux richtig installieren, betreiben und absichern

## Dokumentenstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline / Infrastruktur- und Tooling-Standard |
| ID | QG-JAVA-013 |
| Titel | Docker auf Debian/Linux richtig installieren, betreiben und absichern |
| Status | Accepted / verbindlicher Standard für lokale Entwicklungsumgebungen und einfache Linux-Server-Setups |
| Sprache | Deutsch |
| Kategorie | Containerisierung / Linux / Developer Platform / DevSecOps |
| Zielgruppe | Java-Entwickler, Tech Leads, DevOps Engineers, Plattformverantwortliche, Security Reviewer, QA |
| Betriebssystem-Fokus | Debian GNU/Linux und Debian-nahe Linux-Systeme |
| Empfohlene Debian-Basis | Debian 12 „bookworm“ oder Debian 13 „trixie“, abhängig vom Systemstandard der Organisation |
| Docker-Basis | Docker Engine aus dem offiziellen Docker-APT-Repository, nicht aus veralteten oder distributionsfremden Quellen |
| Java-Bezug | Java- und Spring-Boot-Anwendungen sollen reproduzierbar, isoliert, lokal testbar und CI/CD-fähig containerisiert werden |
| Verbindlichkeit | Für neue Entwickler- und Team-Setups MUSS Docker nach dieser Richtlinie oder einer gleichwertig geprüften internen Plattformvorgabe installiert und betrieben werden |
| Letzte fachliche Prüfung | 2026-05-02 |
| Validierungsstatus | Installationspfad, Security-Hinweise, Compose-Plugin, Rootless Mode, Docker-Daemon-Angriffsfläche und Dockerfile-Best-Practices wurden gegen offizielle Docker-Dokumentation und OWASP Docker Security Cheat Sheet geprüft |
| Abweichungen | Abweichungen sind zulässig, wenn sie technisch begründet, dokumentiert und durch Security/Platform Review freigegeben sind |
| Nicht-Ziel | Diese Richtlinie ersetzt keine Kubernetes-, Produktions-Orchestrierungs-, Cloud-Plattform- oder Unternehmens-Hardening-Vorgabe |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Docker auf einem Debian-/Linux-System sauber installiert, betrieben, geprüft und abgesichert wird. Ziel ist nicht nur, dass `docker run hello-world` funktioniert. Ziel ist eine reproduzierbare, wartbare und sicherheitsbewusste Entwickler- und Plattformbasis für Java-, Spring-Boot- und SaaS-Anwendungen.

Docker ist für moderne Java-Teams ein zentrales Werkzeug: Datenbanken, Message Broker, lokale Integrationsumgebungen, Build-Container, Testcontainer, CI/CD-Jobs und Deployment-Artefakte hängen häufig daran. Eine unsaubere Docker-Installation führt deshalb nicht nur zu lokalen Problemen, sondern zu instabilen Tests, nicht reproduzierbaren Umgebungen, Sicherheitsrisiken und späteren Produktionsfehlern.

Diese Richtlinie legt fest, wie Docker auf Debian installiert wird, welche Post-Installationsschritte erlaubt sind, welche Sicherheitsgrenzen gelten, wie Entwickler Docker Compose verwenden sollen und welche Fehlmuster vermieden werden müssen.

---

## 2. Kurzregel für Entwickler

Installiere Docker Engine auf Debian über das offizielle Docker-APT-Repository. Verwende das moderne Docker-Compose-Plugin über `docker compose`, nicht das alte `docker-compose`-Standalone-Kommando. Behandle Mitgliedschaft in der `docker`-Gruppe wie Root-Zugriff. Verwende Docker für reproduzierbare Entwicklungs- und Testumgebungen, aber nicht als Ersatz für saubere Anwendungskonfiguration, Secrets-Handling, Netzwerkgrenzen, Ressourcenlimits oder Security Reviews.

Für neue Setups gilt:

```bash
docker version
docker compose version
docker run --rm hello-world
```

müssen erfolgreich laufen. Zusätzlich MUSS geprüft sein, ob der lokale Benutzer wirklich Docker-Rechte erhalten darf oder ob Rootless Mode beziehungsweise `sudo docker ...` die bessere Wahl ist.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für Debian-basierte Entwicklerarbeitsplätze, Linux-VMs, Build-Hosts, einfache Testserver, lokale Java-/Spring-Boot-Entwicklungsumgebungen und projektnahe Docker-Compose-Setups.

Sie gilt insbesondere für:

- Installation von Docker Engine auf Debian/Linux
- Einrichtung des offiziellen Docker-APT-Repository
- Installation von Docker CLI, Docker Engine, containerd, Buildx und Compose Plugin
- grundlegende Systemprüfung nach der Installation
- Benutzerrechte und `docker`-Gruppe
- Rootless Mode als Sicherheitsoption
- Docker Compose für lokale Entwicklungsumgebungen
- Dockerfile-Grundregeln für Java-Anwendungen
- Umgang mit Volumes, Netzwerken, Logs und Ressourcen
- Security- und SaaS-Risiken durch Fehlkonfiguration

Diese Richtlinie gilt nicht als vollständiger Standard für:

- Kubernetes-Produktionsbetrieb
- Docker Swarm
- Enterprise Registry Governance
- vollständige CIS-Docker-Benchmark-Umsetzung
- Cloud-spezifische Containerplattformen
- Air-gapped Enterprise-Installationen
- hochverfügbare Produktions-Cluster
- Runtime-Security mit EDR/CNAPP/SIEM-Integration

---

## 4. Technischer Hintergrund

Docker besteht nicht nur aus einem einzelnen Programm. Für Entwickler ist `docker` meist das CLI-Kommando. Technisch gehört dazu aber ein Stack aus Docker Engine, Docker Daemon, Docker CLI, containerd, runc, Buildx, Images, Containern, Netzwerken, Volumes und optional Docker Compose.

Ein Container ist kein vollständiger virtueller Rechner. Container teilen sich den Kernel des Hosts. Daraus folgt: Die Sicherheit des Hosts, des Kernels, des Docker Daemons und der Container-Konfiguration ist direkt relevant für die Sicherheit der laufenden Anwendungen. Docker kann Isolation verbessern, aber falsche Konfigurationen können die Host-Sicherheit deutlich verschlechtern.

Für Java-Teams ist Docker vor allem aus vier Gründen wichtig:

1. Lokale Abhängigkeiten wie PostgreSQL, MySQL, Redis, Kafka oder Mailhog können reproduzierbar gestartet werden.
2. Integrationstests können gegen realistischere Infrastruktur laufen.
3. Anwendungen können als Images gebaut und in CI/CD geprüft werden.
4. Laufzeitumgebungen werden expliziter, weil Betriebssystem, JDK, Ports, Umgebungsvariablen und Startkommando im Image beschrieben sind.

Docker löst aber keine Architekturprobleme. Ein schlechter Service wird durch einen Container nicht sauber. Ein unsicheres Secret bleibt unsicher. Eine fehlende Tenant-Isolation wird nicht durch Docker repariert. Ein falsch konfigurierter Port ist auch dann gefährlich, wenn er aus einem Container kommt.

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
|---|---|---|
| Docker Engine | Serverkomponente, die Container erstellt, startet, stoppt und verwaltet | `dockerd` |
| Docker CLI | Kommandozeilenwerkzeug zur Steuerung des Docker Daemons | `docker ps`, `docker run`, `docker build` |
| Docker Daemon | Hintergrunddienst mit weitreichenden Rechten auf dem Host | `systemctl status docker` |
| Image | Unveränderliches Artefakt aus Schichten, aus dem Container gestartet werden | `eclipse-temurin:21-jre` |
| Container | Laufende Instanz eines Images | `docker run nginx` |
| Volume | Von Docker verwalteter persistenter Speicher | PostgreSQL-Daten in `pgdata` |
| Bind Mount | Direkte Einbindung eines Host-Verzeichnisses in einen Container | `./app:/app` |
| Network | Virtuelles Docker-Netzwerk für Container-Kommunikation | `backend-network` |
| Docker Compose | Werkzeug zur Beschreibung und Ausführung mehrerer Container | `compose.yaml`, `docker compose up` |
| Buildx | Erweiterung für moderne Docker-Builds, Multi-Arch-Builds und BuildKit | `docker buildx build` |
| Rootless Mode | Betriebsmodus, bei dem Daemon und Container ohne Root-Rechte laufen | Entwickler-Workstation mit reduzierter Host-Gefährdung |
| `docker`-Gruppe | Unix-Gruppe, deren Mitglieder Docker ohne `sudo` steuern dürfen | Sicherheitskritisch, faktisch root-nah |

---

## 6. Verbindlicher Standard

### 6.1 Installation

Docker Engine MUSS auf Debian über das offizielle Docker-APT-Repository installiert werden, sofern keine interne Plattformvorgabe etwas anderes bestimmt.

Nicht erlaubt als Standardinstallation sind:

- veraltete Distributionspakete ohne bewusste Prüfung
- manuell heruntergeladene Einzelpakete ohne Updatekonzept
- `curl | sh`-Installationen auf produktionsnahen Systemen
- nicht nachvollziehbare Blog-Kommandos
- gemischte Installationen aus Docker-Repository, Debian-Repository und manuellen `.deb`-Paketen

### 6.2 Compose

Docker Compose MUSS als modernes CLI-Plugin verwendet werden:

```bash
docker compose version
```

Das alte Kommando

```bash
docker-compose version
```

DARF in neuen Projekten nicht als Standard vorausgesetzt werden.

### 6.3 Benutzerrechte

Die Aufnahme eines Benutzers in die `docker`-Gruppe DARF NICHT als harmlose Komforteinstellung behandelt werden. Wer Docker ohne `sudo` bedienen darf, kann über Docker sehr weitreichend auf den Host einwirken. Für Teammaschinen, Build-Hosts und gemeinsam genutzte Systeme MUSS bewusst entschieden werden, wer Docker steuern darf.

### 6.4 Rootless Mode

Rootless Mode SOLLTE geprüft werden, wenn Entwickler Docker lokal ohne Root-ähnliche Host-Rechte nutzen sollen. Rootless Mode ist nicht in jedem Setup gleich bequem, reduziert aber eine wichtige Risikoklasse, weil Docker Daemon und Container ohne Root-Rechte laufen.

### 6.5 Dockerfile-Qualität

Dockerfiles MÜSSEN nachvollziehbar, minimal, reproduzierbar und sicherheitsbewusst geschrieben werden. Für Java-Anwendungen bedeutet das insbesondere:

- passende JDK-/JRE-Basis verwenden
- keine unnötigen Build-Werkzeuge im Runtime-Image
- keine Secrets im Image
- kein `latest` für produktionsnahe Images
- Anwendung nach Möglichkeit als Nicht-Root-Benutzer starten
- `.dockerignore` verwenden
- Healthchecks, Ports und Startkommando explizit dokumentieren

---

## 7. Debian-Installation Schritt für Schritt

### 7.1 System prüfen

Vor der Installation MUSS geprüft werden, auf welchem Debian-System gearbeitet wird.

```bash
cat /etc/os-release
uname -m
```

Erwartete Ausgabe für ein aktuelles Debian-System ist beispielsweise `bookworm` oder `trixie` als Version Codename. Die Architektur sollte zu den von Docker unterstützten Architekturen passen, typischerweise `x86_64` beziehungsweise `amd64`.

### 7.2 Alte oder kollidierende Pakete entfernen

Vor der offiziellen Installation SOLLTEN kollidierende Pakete entfernt werden. Das betrifft insbesondere ältere oder distributionsseitige Pakete wie `docker.io`, `docker-compose`, `docker-doc`, `podman-docker`, `containerd` oder `runc`, wenn sie nicht bewusst Teil einer internen Plattformvorgabe sind.

```bash
sudo apt remove $(dpkg --get-selections docker.io docker-compose docker-doc podman-docker containerd runc 2>/dev/null | cut -f1)
```

Wenn `apt` meldet, dass keine solchen Pakete installiert sind, ist das unkritisch.

### 7.3 Benötigte Basispakete installieren

```bash
sudo apt update
sudo apt install -y ca-certificates curl
```

### 7.4 Docker-GPG-Key und APT-Repository einrichten

```bash
sudo install -m 0755 -d /etc/apt/keyrings

sudo curl -fsSL https://download.docker.com/linux/debian/gpg \
  -o /etc/apt/keyrings/docker.asc

sudo chmod a+r /etc/apt/keyrings/docker.asc
```

Danach wird die Docker-APT-Quelle gesetzt:

```bash
sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/debian
Suites: $(. /etc/os-release && echo "$VERSION_CODENAME")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
```

Anschließend wird der Paketindex aktualisiert:

```bash
sudo apt update
```

### 7.5 Docker Engine, CLI, containerd, Buildx und Compose installieren

```bash
sudo apt install -y \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin
```

### 7.6 Dienststatus prüfen

```bash
sudo systemctl status docker
```

Wenn Docker nicht läuft:

```bash
sudo systemctl start docker
```

Für Systeme, auf denen Docker automatisch starten soll:

```bash
sudo systemctl enable docker.service
sudo systemctl enable containerd.service
```

### 7.7 Installation verifizieren

```bash
sudo docker run --rm hello-world
```

Danach:

```bash
docker version
docker compose version
docker buildx version
```

Wenn `docker version` ohne `sudo` noch nicht funktioniert, ist das erwartbar, solange der Benutzer nicht in der `docker`-Gruppe ist oder Rootless Mode nicht eingerichtet wurde.

---

## 8. Benutzerrechte nach der Installation

### 8.1 Option A: Docker mit `sudo` verwenden

Für sicherheitsbewusste Einzelmaschinen oder Admin-Kontexte ist es akzeptabel, Docker bewusst mit `sudo` zu verwenden.

```bash
sudo docker ps
sudo docker compose up
```

Vorteil: Keine dauerhafte Erweiterung der Benutzerrechte über die `docker`-Gruppe.  
Nachteil: Weniger bequem im Entwickleralltag.

### 8.2 Option B: Benutzer zur `docker`-Gruppe hinzufügen

Für typische Entwicklerarbeitsplätze ist häufig gewünscht, Docker ohne `sudo` auszuführen. Das ist bequem, aber sicherheitskritisch.

```bash
sudo groupadd docker
sudo usermod -aG docker "$USER"
newgrp docker
```

Prüfung:

```bash
docker run --rm hello-world
```

Wichtig: Nach `usermod` kann ein Logout/Login oder Neustart nötig sein.

Wenn vorher Docker-Kommandos mit `sudo` ausgeführt wurden, kann `~/.docker` falsche Rechte haben:

```bash
sudo chown "$USER":"$USER" "$HOME/.docker" -R
sudo chmod g+rwx "$HOME/.docker" -R
```

### 8.3 Option C: Rootless Mode prüfen

Rootless Mode ist eine Sicherheitsoption, bei der Docker Daemon und Container innerhalb eines User Namespace laufen. Das reduziert Host-Risiken, kann aber Einschränkungen bei Netzwerk, Ports, Storage, Performance oder Integrationen haben.

Für Entwicklerarbeitsplätze mit erhöhtem Sicherheitsbedarf SOLLTE Rootless Mode geprüft werden. Für Server-Setups MUSS die Entscheidung mit Plattform-/Security-Verantwortlichen abgestimmt werden.

---

## 9. Gutes Beispiel: Sauberes Setup-Skript für Debian

Ein Setup-Skript DARF nur verwendet werden, wenn es im Repository versioniert, lesbar und reviewt ist. Es darf keine unkontrollierten Remote-Skripte ausführen.

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "Prüfe Debian-Version ..."
. /etc/os-release
echo "Distribution: ${PRETTY_NAME}"
echo "Codename: ${VERSION_CODENAME}"

echo "Entferne kollidierende Pakete, falls vorhanden ..."
sudo apt remove -y $(dpkg --get-selections docker.io docker-compose docker-doc podman-docker containerd runc 2>/dev/null | cut -f1) || true

echo "Installiere Basisabhängigkeiten ..."
sudo apt update
sudo apt install -y ca-certificates curl

echo "Richte Docker-Keyring ein ..."
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo "Richte Docker-APT-Quelle ein ..."
sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/debian
Suites: ${VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

echo "Installiere Docker Engine und Plugins ..."
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "Aktiviere Docker-Dienste ..."
sudo systemctl enable docker.service
sudo systemctl enable containerd.service
sudo systemctl start docker

echo "Verifiziere Installation ..."
sudo docker run --rm hello-world
docker compose version || true

echo "Fertig. Prüfe bewusst, ob der Benutzer in die docker-Gruppe aufgenommen werden soll."
```

---

## 10. Schlechtes Beispiel: Unsicheres Copy-Paste-Setup

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
docker run -v /:/host -it ubuntu bash
```

Warum ist das schlecht?

- Remote-Skripte werden direkt ausgeführt, ohne Review.
- Die Installation ist nicht reproduzierbar versioniert.
- Der Benutzer erhält faktisch root-nahe Docker-Rechte, ohne Security-Entscheidung.
- Das Mounten von `/` in einen Container kann den Host vollständig kompromittieren.
- Es gibt kein Update-, Logging-, Rechte- oder Deinstallationskonzept.

---

## 11. Docker Compose als lokaler Entwicklungsstandard

Docker Compose SOLLTE verwendet werden, wenn mehrere Dienste lokal zusammen gestartet werden müssen, etwa Java-App, Datenbank, Redis, Kafka, Mailserver oder Observability-Komponenten.

### 11.1 Gutes Compose-Beispiel für lokale Java-Entwicklung

```yaml
services:
  postgres:
    image: postgres:16.4
    container_name: example-postgres
    environment:
      POSTGRES_DB: example
      POSTGRES_USER: example
      POSTGRES_PASSWORD: example-dev-password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U example -d example"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend

  mailhog:
    image: mailhog/mailhog:v1.0.1
    container_name: example-mailhog
    ports:
      - "8025:8025"
    networks:
      - backend

volumes:
  postgres-data:

networks:
  backend:
    driver: bridge
```

Start:

```bash
docker compose up -d
docker compose ps
docker compose logs -f postgres
docker compose down
```

Mit Datenlöschung:

```bash
docker compose down -v
```

### 11.2 Compose-Regeln

- Compose-Dateien MÜSSEN im Repository liegen, wenn sie für das Projekt notwendig sind.
- Lokale Entwicklungs-Passwörter DÜRFEN nur für lokale Container verwendet werden.
- Produktive Secrets DÜRFEN NICHT in `compose.yaml` stehen.
- Ports MÜSSEN bewusst exponiert werden.
- Volumes MÜSSEN benannt sein, wenn Daten erhalten bleiben sollen.
- `docker compose down -v` MUSS bewusst eingesetzt werden, weil Volumes gelöscht werden.
- Services SOLLTEN Healthchecks besitzen, wenn andere Dienste von ihnen abhängen.

---

## 12. Dockerfile-Grundstandard für Java-Anwendungen

### 12.1 Schlechtes Dockerfile

```dockerfile
FROM openjdk:latest

COPY . /app
WORKDIR /app

RUN ./mvnw package

EXPOSE 8080

CMD ["java", "-jar", "target/app.jar"]
```

Warum ist das schlecht?

- `latest` ist nicht reproduzierbar.
- Build- und Runtime-Umgebung sind vermischt.
- Der gesamte Quellcode wird ins Image kopiert.
- Keine `.dockerignore`.
- Anwendung läuft potenziell als Root.
- Maven-Cache und Build-Artefakte landen ineffizient im Image.
- Keine klare Versionierung der JDK-/JRE-Basis.
- Keine expliziten JVM-Optionen für Containerbetrieb.

### 12.2 Besseres Dockerfile für Spring Boot / Java 21

```dockerfile
# Build-Stage
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B clean package -DskipTests

# Runtime-Stage
FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app --no-create-home app

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 12.3 `.dockerignore`

```dockerignore
.git
.gitignore
target
build
.idea
.vscode
*.iml
*.log
.env
.env.*
docker-compose.override.yml
README.md
```

### 12.4 Regeln für Java-Images

- Runtime-Images SOLLTEN keine Build-Werkzeuge enthalten.
- Basisimages MÜSSEN bewusst versioniert werden.
- `latest` DARF für produktionsnahe Images nicht verwendet werden.
- Die Anwendung SOLLTE als Nicht-Root-Benutzer laufen.
- `.dockerignore` MUSS vorhanden sein.
- Secrets DÜRFEN NICHT in das Image kopiert werden.
- Build- und Runtime-Stage SOLLTEN getrennt werden.
- Image-Builds SOLLTEN in CI wiederholbar sein.
- Vulnerability Scans SOLLTEN im CI/CD-Prozess laufen.
- SBOM-Erzeugung SOLLTE für produktionsnahe Artefakte geprüft werden.

---

## 13. Security- und SaaS-Aspekte

### 13.1 Docker-Gruppe ist kein harmloser Komfort

Die `docker`-Gruppe ist sicherheitskritisch. Ein Benutzer mit Zugriff auf den Docker-Daemon kann Container mit Host-Mounts starten und dadurch weitreichend auf Host-Dateien einwirken. Deshalb gilt:

- Nur vertrauenswürdige Benutzer DÜRFEN Mitglied der `docker`-Gruppe sein.
- Auf gemeinsam genutzten Servern MUSS die Mitgliedschaft restriktiv sein.
- Für Build-Server MUSS geprüft werden, ob Runner-Prozesse Docker-Rechte wirklich benötigen.
- Docker-Socket-Mounts sind besonders kritisch.

### 13.2 Docker-Socket nicht in Container mounten

Dieses Muster ist gefährlich:

```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

Ein Container mit Zugriff auf den Docker-Socket kann den Docker-Daemon kontrollieren. Das kann faktisch Host-Kontrolle bedeuten. Dieses Muster DARF nur in bewusst freigegebenen Infrastrukturkomponenten verwendet werden, etwa kontrollierten CI-Runnern oder spezialisierten Build-Agenten.

### 13.3 Keine privilegierten Container als Standard

```bash
docker run --privileged ...
```

DARF NICHT im normalen Entwicklungs- oder Anwendungsbetrieb verwendet werden. `--privileged` erweitert Containerrechte massiv und entfernt wesentliche Sicherheitsgrenzen.

### 13.4 Keine produktiven Secrets in Images oder Compose-Dateien

Nicht erlaubt:

```dockerfile
ENV DB_PASSWORD=prod-secret
```

Nicht erlaubt:

```yaml
environment:
  JWT_SECRET: "super-secret-production-key"
```

Stattdessen:

- lokale `.env` nur für lokale Entwicklung
- `.env` nicht committen
- CI/CD-Secrets über Secret Store
- Kubernetes Secrets, Vault, Cloud Secret Manager oder interne Plattformlösung für produktive Umgebungen
- Tests mit synthetischen Test-Secrets

### 13.5 Port-Exposition bewusst steuern

```yaml
ports:
  - "5432:5432"
```

macht den Port auf dem Host erreichbar. Auf Entwicklergeräten ist das häufig okay. Auf gemeinsam genutzten Servern kann es gefährlich sein.

Besser bei rein interner Container-Kommunikation:

```yaml
expose:
  - "5432"
```

oder gar keine Exposition nach außen, wenn nur Compose-interne Services darauf zugreifen.

### 13.6 Firewall und Docker-Netzwerk beachten

Docker kann eigene iptables-Regeln setzen. Wer `ufw`, `firewalld` oder nftables nutzt, MUSS prüfen, ob Docker-exponierte Ports die erwarteten Firewall-Regeln umgehen. Host-Firewall-Regeln und Docker-Port-Exposition dürfen nicht getrennt betrachtet werden.

### 13.7 Ressourcenlimits setzen

Für lokale Entwicklung sind harte Limits nicht immer notwendig. Für gemeinsam genutzte Systeme, CI-Runner und Testserver SOLLTEN CPU- und Speichergrenzen gesetzt werden.

Beispiel:

```yaml
services:
  app:
    image: example-app:dev
    mem_limit: 768m
    cpus: 1.0
```

In SaaS-Kontexten ist Ressourcenbegrenzung wichtig, damit ein einzelner fehlerhafter Container, Testlauf oder Tenant-nahe Prozess nicht das gesamte System destabilisiert.

---

## 14. Häufige Fehlmuster und korrekte Alternativen

### 14.1 Fehlmuster: Docker aus falscher Quelle installieren

Schlecht:

```bash
sudo apt install docker.io docker-compose
```

Das kann je nach Debian-Version alte oder anders gepackte Versionen installieren.

Besser:

```bash
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

aus dem offiziellen Docker-Repository.

### 14.2 Fehlmuster: `latest` verwenden

Schlecht:

```dockerfile
FROM eclipse-temurin:latest
```

Besser:

```dockerfile
FROM eclipse-temurin:21-jre-jammy
```

Noch strenger für produktionsnahe Systeme: Image-Digests und Registry-Policies verwenden.

### 14.3 Fehlmuster: Root im Container

Schlecht:

```dockerfile
FROM eclipse-temurin:21-jre
COPY app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Besser:

```dockerfile
RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app --no-create-home app

USER app
```

### 14.4 Fehlmuster: Secrets in Images

Schlecht:

```dockerfile
COPY production.env /app/.env
```

Besser:

```bash
docker run --env-file .env.local example-app
```

für lokale Entwicklung, aber produktive Secrets über Secret-Management.

### 14.5 Fehlmuster: Datenbankdaten ohne Volume

Schlecht:

```yaml
services:
  postgres:
    image: postgres:16.4
```

Besser:

```yaml
volumes:
  - postgres-data:/var/lib/postgresql/data
```

### 14.6 Fehlmuster: Container als VM behandeln

Schlecht:

```bash
docker exec -it app bash
apt install vim
service cron start
```

Besser:

- Image neu bauen
- Konfiguration versionieren
- Container unveränderlich behandeln
- ein Prozess pro Container als Grundregel beachten
- Debugging nicht mit manueller Produktionsmutation verwechseln

### 14.7 Fehlmuster: `docker system prune -a --volumes` ohne Verständnis

Dieses Kommando kann Images, Container, Netzwerke, Build-Cache und Volumes entfernen. Volumes können Datenbanken enthalten.

Besser:

```bash
docker system df
docker image prune
docker builder prune
```

und `--volumes` nur bewusst einsetzen.

---

## 15. Betrieb und Diagnose

### 15.1 Grundkommandos

```bash
docker ps
docker ps -a
docker images
docker volume ls
docker network ls
docker logs <container>
docker inspect <container>
docker stats
docker system df
```

### 15.2 Compose-Diagnose

```bash
docker compose ps
docker compose logs -f
docker compose config
docker compose down
docker compose down -v
```

`docker compose config` ist besonders nützlich, weil es die final aufgelöste Compose-Konfiguration zeigt.

### 15.3 Docker-Dienst prüfen

```bash
systemctl status docker
journalctl -u docker --no-pager -n 200
```

### 15.4 Speicherplatz prüfen

Docker kann viel Speicher belegen:

```bash
docker system df
du -sh /var/lib/docker
```

Auf Servern SOLLTE `/var/lib/docker` bewusst auf einer geeigneten Partition liegen oder zumindest überwacht werden.

---

## 16. Updates und Wartung

Docker und der Linux-Kernel MÜSSEN regelmäßig aktualisiert werden. Container teilen sich den Host-Kernel; ein verwundbarer Host-Kernel kann Container-Isolation schwächen.

Update:

```bash
sudo apt update
sudo apt upgrade
```

Docker-spezifische Versionen prüfen:

```bash
apt list --installed | grep -E 'docker|containerd'
docker version
docker compose version
```

Für produktionsnahe Systeme SOLLTE nicht blind jedes Major-Upgrade installiert werden. Besser ist:

1. Versionen auf Testsystem aktualisieren.
2. zentrale Container-Workloads prüfen.
3. Compose-Setups starten.
4. Logs und Netzwerkverhalten prüfen.
5. Update auf Zielsystemen ausrollen.

---

## 17. Deinstallation und sauberes Entfernen

Docker-Pakete entfernen:

```bash
sudo apt purge docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin docker-ce-rootless-extras
```

Achtung: Images, Container, Volumes und Konfigurationen werden nicht automatisch vollständig entfernt.

Datenverzeichnisse löschen:

```bash
sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd
```

APT-Quelle und Key entfernen:

```bash
sudo rm -f /etc/apt/sources.list.d/docker.sources
sudo rm -f /etc/apt/keyrings/docker.asc
sudo apt update
```

Diese Schritte DÜRFEN NICHT unbedacht auf Systemen ausgeführt werden, auf denen Volumes produktive oder relevante Testdaten enthalten.

---

## 18. Review-Checkliste

Vor Aufnahme eines Docker-Setups in ein Projekt müssen folgende Fragen beantwortet werden:

- Wurde Docker über das offizielle Repository installiert?
- Ist klar, welche Benutzer Zugriff auf Docker haben?
- Ist dokumentiert, ob `docker`-Gruppenmitgliedschaft erlaubt ist?
- Wurde Rootless Mode geprüft, wenn erhöhte Host-Sicherheit erforderlich ist?
- Wird `docker compose` statt `docker-compose` verwendet?
- Sind Images versioniert und wird `latest` vermieden?
- Läuft die Anwendung im Container als Nicht-Root-Benutzer?
- Gibt es eine `.dockerignore`?
- Sind Secrets aus Dockerfile, Image und Repository ausgeschlossen?
- Werden produktive Secrets über einen Secret Store bereitgestellt?
- Sind Ports bewusst exponiert?
- Sind Datenbankdaten in benannten Volumes abgelegt?
- Sind gefährliche Mounts wie `/var/run/docker.sock` verboten oder explizit begründet?
- Wird `--privileged` vermieden?
- Gibt es Healthchecks für abhängige Services?
- Sind Ressourcenlimits für gemeinsam genutzte Systeme definiert?
- Gibt es ein Update- und Patch-Konzept?
- Gibt es einen Image-Scan oder SBOM-Prozess für produktionsnahe Images?
- Sind Docker-Kommandos in README oder Developer Guide dokumentiert?
- Können neue Entwickler das Setup reproduzierbar ausführen?

---

## 19. Automatisierbare Prüfungen

Folgende Prüfungen SOLLTEN automatisiert werden:

| Prüfung | Werkzeug/Ansatz | Ziel |
|---|---|---|
| Dockerfile-Linting | Hadolint oder vergleichbares Tool | Dockerfile-Anti-Patterns erkennen |
| Image-Scan | Docker Scout, Trivy, Grype oder interne Lösung | bekannte Schwachstellen erkennen |
| Secret-Scanning | Gitleaks, TruffleHog, GitHub Secret Scanning oder interne Lösung | Secrets im Repository verhindern |
| Compose-Validierung | `docker compose config` | syntaktische und strukturelle Fehler erkennen |
| Build-Prüfung | CI-Pipeline mit `docker build` | Image reproduzierbar bauen |
| Runtime-Smoke-Test | `docker run` oder `docker compose up` im CI-Kontext | Startfähigkeit prüfen |
| Nicht-Root-Prüfung | Container-Inspect oder Testkommando | Root-Laufzeit vermeiden |
| Port-Prüfung | Review/Script | unerwartet exponierte Ports erkennen |
| SBOM-Erzeugung | Docker Scout/Syft/CI-Tooling | Komponentenbestand sichtbar machen |

Beispiel CI-Schritte:

```bash
docker compose config
docker build -t example-app:test .
docker run --rm example-app:test java -version
```

---

## 20. Migration bestehender Setups

Bestehende Docker-Setups sollen schrittweise verbessert werden:

1. Aktuelle Installation prüfen:
   ```bash
   docker version
   docker compose version
   apt list --installed | grep -E 'docker|containerd'
   ```
2. Prüfen, ob `docker.io` oder altes `docker-compose` verwendet wird.
3. Projekt auf `docker compose` umstellen.
4. Compose-Dateien vereinheitlichen.
5. `.dockerignore` ergänzen.
6. `latest`-Tags ersetzen.
7. Runtime-Image von Build-Image trennen.
8. Nicht-Root-Benutzer im Image einführen.
9. Secrets aus Images und Compose-Dateien entfernen.
10. Ports, Volumes und Netzwerke dokumentieren.
11. Image-Scan in CI ergänzen.
12. Entwicklerdokumentation aktualisieren.

Migration darf nicht als reine Technikaufgabe verstanden werden. Sie verändert, wie Entwickler lokal arbeiten, wie Tests laufen und wie Fehler diagnostiziert werden. Deshalb muss sie mit klaren Kommandos, Troubleshooting und Review-Regeln begleitet werden.

---

## 21. Ausnahmen

Abweichungen von dieser Richtlinie sind erlaubt, wenn ein nachvollziehbarer Grund vorliegt.

Beispiele:

- Ein Unternehmen gibt eine interne Docker-/Podman-Plattform verbindlich vor.
- CI-Runner verwenden einen kontrollierten Docker-Socket-Mount.
- Ein Container muss für spezifische Systemtests kurzzeitig privilegiert laufen.
- Ein Legacy-Projekt benötigt vorübergehend `docker-compose`.
- Rootless Mode ist wegen konkreter technischer Einschränkungen nicht geeignet.
- Ein Image verwendet temporär Root, weil eine Legacy-Komponente nicht anders startet.

Jede Ausnahme MUSS dokumentiert werden:

- Was weicht ab?
- Warum ist die Abweichung notwendig?
- Welche Risiken entstehen?
- Wie wird das Risiko begrenzt?
- Wann wird die Ausnahme erneut geprüft?

---

## 22. Definition of Done

Ein Docker-Setup erfüllt diese Richtlinie, wenn alle folgenden Punkte erfüllt sind:

- Docker Engine ist über eine nachvollziehbare, geprüfte Quelle installiert.
- Auf Debian wird das offizielle Docker-APT-Repository verwendet.
- Docker CLI, Engine, containerd, Buildx und Compose Plugin sind installiert.
- `docker run --rm hello-world` funktioniert.
- `docker compose version` funktioniert.
- Benutzerrechte sind bewusst entschieden und dokumentiert.
- Die `docker`-Gruppe wird als sicherheitskritisch behandelt.
- Rootless Mode wurde bei erhöhtem Sicherheitsbedarf geprüft.
- Projekt-Compose-Dateien sind versioniert und verständlich.
- Keine produktiven Secrets liegen im Repository, Image oder Compose-File.
- Dockerfiles verwenden keine unkontrollierten `latest`-Tags.
- Images laufen nach Möglichkeit als Nicht-Root-Benutzer.
- `.dockerignore` ist vorhanden.
- Ports, Volumes und Netzwerke sind bewusst konfiguriert.
- Gefährliche Optionen wie `--privileged`, Host-Root-Mounts und Docker-Socket-Mounts sind verboten oder explizit freigegeben.
- Für produktionsnahe Images gibt es einen Vulnerability-/SBOM-Prüfprozess.
- Entwickler können das Setup anhand der Dokumentation reproduzierbar ausführen.
- Die wichtigsten Docker-Kommandos für Start, Stop, Logs, Reset und Diagnose sind dokumentiert.

---

## 23. Quellen und weiterführende Literatur

- Docker Docs — Install Docker Engine on Debian: https://docs.docker.com/engine/install/debian/
- Docker Docs — Linux post-installation steps for Docker Engine: https://docs.docker.com/engine/install/linux-postinstall/
- Docker Docs — Rootless mode: https://docs.docker.com/engine/security/rootless/
- Docker Docs — Docker Engine security: https://docs.docker.com/engine/security/
- Docker Docs — Install the Docker Compose plugin: https://docs.docker.com/compose/install/linux/
- Docker Docs — Dockerfile building best practices: https://docs.docker.com/build/building/best-practices/
- Docker Docs — Docker Scout: https://docs.docker.com/scout/
- OWASP Cheat Sheet Series — Docker Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html
