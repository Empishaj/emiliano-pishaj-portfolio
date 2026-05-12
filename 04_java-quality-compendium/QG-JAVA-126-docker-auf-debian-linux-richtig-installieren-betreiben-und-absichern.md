# QG-JAVA-126 — Docker auf Debian/Linux richtig installieren, betreiben und absichern

## Dokumentstatus

| Aspekt | Details/Erklärung |
| --- | --- |
| Dokumenttyp | Java Quality Guideline / Infrastruktur- und Tooling-Standard |
| ID | QG-JAVA-126 |
| Titel | Docker auf Debian/Linux richtig installieren, betreiben und absichern |
| Status | Accepted / verbindlicher Standard für lokale Entwicklungsumgebungen und einfache Linux-Server-Setups |
| Version | 2.0 |
| Datum | 2026-05-03 (überarbeitet von 1.0 vom 2026-05-02) |
| Sprache | Deutsch |
| Kategorie | Containerisierung · Linux · Developer Platform · DevSecOps |
| Zielgruppe | Java-Entwickler, Tech Leads, DevOps Engineers, Plattformverantwortliche, Security Reviewer, QA |
| Betriebssystem-Fokus | Debian GNU/Linux und Debian-nahe Linux-Systeme |
| Empfohlene Debian-Basis | Debian 12 „bookworm" oder Debian 13 „trixie", abhängig vom Systemstandard der Organisation |
| Docker-Basis | Docker Engine 27.x+ aus dem offiziellen Docker-APT-Repository, nicht aus veralteten oder distributionsfremden Quellen |
| Docker-Compose-Basis | Docker Compose v2 (CLI-Plugin, `docker compose`), niemals `docker-compose` v1 |
| Java-Bezug | Java 21 LTS und Spring Boot 3.4+; Anwendungen sollen reproduzierbar, isoliert, lokal testbar und CI/CD-fähig containerisiert werden |
| Verbindlichkeit | Verbindlich für neue Entwickler- und Team-Setups. Abweichungen sind im Pull Request oder Plattform-Review nachvollziehbar zu begründen. |
| Letzte fachliche Prüfung | 2026-05-03 |
| Validierungsstatus | Installationspfad, Security-Hinweise, Compose-Plugin, Rootless Mode, Docker-Daemon-Angriffsfläche und Dockerfile-Best-Practices wurden gegen offizielle Docker-Dokumentation, Spring Boot Reference und OWASP Docker Security Cheat Sheet geprüft |
| Schwester-Guidelines | QG-JAVA-006 (Spring-Boot-Serviceschicht), QG-JAVA-008 (Objektorientierung), QG-JAVA-019 (Contract Testing) |
| Wesentliche Änderungen ggü. v1.0 | ID-Inkonsistenz behoben · `apt remove` durch offizielle Docker-Schleife ersetzt · Dockerfile mit `--chown=app:app` und `HEALTHCHECK` · JVM-Container-Optionen ergänzt · GPG-Fingerprint-Verifikation · `depends_on: condition: service_healthy` · BuildKit Cache Mounts · Spring Boot Layered JARs · Cloud Native Buildpacks · Distroless als Runtime-Option · SBOM mit konkreten Tools und EU-CRA-Kontext · Container Runtime Security · User Namespace Remapping · Compose Profiles · `EXPOSE` als Metadata · Multi-Arch-Builds · strukturelle Angleichung an QG-JAVA-006 v2 (TOC, Lese-Anleitung, MUSS/DARF/SOLLTE getrennt, DoD nummeriert) |
| Nicht-Ziel | Diese Richtlinie ersetzt keine Kubernetes-, Produktions-Orchestrierungs-, Cloud-Plattform- oder Unternehmens-Hardening-Vorgabe |

---

## Inhaltsverzeichnis

1. [Zweck dieser Richtlinie](#1-zweck-dieser-richtlinie)
2. [Kurzregel für Entwickler](#2-kurzregel-für-entwickler)
3. [Verbindlicher Standard](#3-verbindlicher-standard)
4. [Geltungsbereich](#4-geltungsbereich)
5. [Begriffe](#5-begriffe)
6. [Technischer Hintergrund](#6-technischer-hintergrund)
7. [Debian-Installation Schritt für Schritt](#7-debian-installation-schritt-für-schritt)
8. [Benutzerrechte nach der Installation](#8-benutzerrechte-nach-der-installation)
9. [Gutes Beispiel: Sauberes Setup-Skript für Debian](#9-gutes-beispiel-sauberes-setup-skript-für-debian)
10. [Schlechtes Beispiel: Unsicheres Copy-Paste-Setup](#10-schlechtes-beispiel-unsicheres-copy-paste-setup)
11. [Docker Compose als lokaler Entwicklungsstandard](#11-docker-compose-als-lokaler-entwicklungsstandard)
12. [Dockerfile-Standard für Java-Anwendungen](#12-dockerfile-standard-für-java-anwendungen)
13. [Cloud Native Buildpacks als Dockerfile-Alternative](#13-cloud-native-buildpacks-als-dockerfile-alternative)
14. [BuildKit-Features und Multi-Architektur](#14-buildkit-features-und-multi-architektur)
15. [Security- und SaaS-Aspekte](#15-security--und-saas-aspekte)
16. [Container Runtime Security](#16-container-runtime-security)
17. [Häufige Fehlmuster und korrekte Alternativen](#17-häufige-fehlmuster-und-korrekte-alternativen)
18. [Betrieb und Diagnose](#18-betrieb-und-diagnose)
19. [Updates und Wartung](#19-updates-und-wartung)
20. [Deinstallation und sauberes Entfernen](#20-deinstallation-und-sauberes-entfernen)
21. [SBOM und EU Cyber Resilience Act](#21-sbom-und-eu-cyber-resilience-act)
22. [Review-Checkliste](#22-review-checkliste)
23. [Automatisierbare Prüfungen](#23-automatisierbare-prüfungen)
24. [Migration bestehender Setups](#24-migration-bestehender-setups)
25. [Ausnahmen](#25-ausnahmen)
26. [Definition of Done](#26-definition-of-done)
27. [Quellen und weiterführende Literatur](#27-quellen-und-weiterführende-literatur)

### Wie liest du dieses Dokument

Dieses Dokument ist mit ca. 95 KB ein Nachschlagewerk, kein Lesebuch. Drei Lesepfade werden empfohlen:

**Wenn du neu im Team bist:** Starte mit Sektion 2 (Kurzregel), lies dann Sektion 1 (Zweck), Sektion 7 (Installation Schritt für Schritt) und Sektion 12 (Dockerfile-Standard). Damit hast du die mentale Karte für 80 % aller Docker-Reviews.

**Wenn du im Code-Review bist:** Springe direkt zu Sektion 22 (Review-Checkliste). Jede Zeile hat einen Anker zur Detail-Sektion mit der Begründung. Bei Verstoß gegen eine MUSS-Regel: Sektion 3.1.

**Wenn du etwas Spezifisches suchst:** Inhaltsverzeichnis oben. Häufigste Punkte: Security → Sektion 15. Dockerfile-Patterns → Sektion 12. Compose → Sektion 11. SBOM/CRA → Sektion 21. Anti-Patterns → Sektion 17.

**Wenn du ein Setup ausführen musst:** Sektion 7 (Installation), Sektion 9 (Setup-Skript) und Sektion 8 (Benutzerrechte) — in dieser Reihenfolge.

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Docker auf einem Debian-/Linux-System sauber installiert, betrieben, geprüft und abgesichert wird. Ziel ist nicht nur, dass `docker run hello-world` funktioniert. Ziel ist eine reproduzierbare, wartbare und sicherheitsbewusste Entwickler- und Plattformbasis für Java-, Spring-Boot- und SaaS-Anwendungen.

Docker ist für moderne Java-Teams ein zentrales Werkzeug: Datenbanken, Message Broker, lokale Integrationsumgebungen, Build-Container, Testcontainers, CI/CD-Jobs und Deployment-Artefakte hängen häufig daran. Eine unsaubere Docker-Installation führt deshalb nicht nur zu lokalen Problemen, sondern zu instabilen Tests, nicht reproduzierbaren Umgebungen, Sicherheitsrisiken und späteren Produktionsfehlern.

Diese Richtlinie legt fest, wie Docker auf Debian installiert wird, welche Post-Installationsschritte erlaubt sind, welche Sicherheitsgrenzen gelten, wie Entwickler Docker Compose verwenden sollen, wie produktionsreife Java-Images gebaut werden und welche Fehlmuster vermieden werden müssen.

---

## 2. Kurzregel für Entwickler

Installiere Docker Engine auf Debian über das offizielle Docker-APT-Repository. Verifiziere den GPG-Fingerprint vor dem Hinzufügen der APT-Quelle. Verwende das moderne Docker-Compose-Plugin über `docker compose`, nicht das alte `docker-compose`-Standalone-Kommando. Behandle Mitgliedschaft in der `docker`-Gruppe wie Root-Zugriff. Verwende Docker für reproduzierbare Entwicklungs- und Testumgebungen, aber nicht als Ersatz für saubere Anwendungskonfiguration, Secrets-Handling, Netzwerkgrenzen, Ressourcenlimits oder Security Reviews.

Für Dockerfiles in Java-Projekten gelten kompromisslos: Multi-Stage-Builds, getrennte Build- und Runtime-Stages, `--chown=app:app` bei `COPY` für Nicht-Root-Ownership, expliziter `USER app`, `HEALTHCHECK` und JVM-Container-Optionen (`MaxRAMPercentage=75.0`, `+ExitOnOutOfMemoryError`).

Für neue Setups gilt:

```
docker version
docker compose version
docker run --rm hello-world
```

müssen erfolgreich laufen. Zusätzlich MUSS geprüft sein, ob der lokale Benutzer wirklich Docker-Rechte erhalten darf oder ob Rootless Mode beziehungsweise `sudo docker ...` die bessere Wahl ist.

---

## 3. Verbindlicher Standard

### 3.1 MUSS-Regeln

Ein Docker-Setup MUSS folgende Regeln erfüllen:

1. Docker Engine MUSS über das offizielle Docker-APT-Repository installiert werden.
2. Der GPG-Key des Docker-APT-Repositorys MUSS vor der ersten Paketinstallation per Fingerprint verifiziert werden.
3. Docker Compose MUSS als CLI-Plugin (`docker compose`) verwendet werden.
4. Mitgliedschaft in der `docker`-Gruppe MUSS als root-äquivalente Berechtigung behandelt werden — pro Benutzer dokumentiert und freigegeben.
5. Auf gemeinsam genutzten Servern, Build-Hosts oder Mehr-Benutzer-Systemen MUSS die `docker`-Gruppen-Mitgliedschaft restriktiv sein.
6. Dockerfiles MÜSSEN in Multi-Stage gebaut werden — Build-Tools dürfen nicht im Runtime-Image landen.
7. Anwendungen in Containern MÜSSEN als Nicht-Root-Benutzer laufen.
8. Datei-Ownership im Container MUSS über `--chown=app:app` bei `COPY` korrekt gesetzt sein.
9. Dockerfiles MÜSSEN einen `HEALTHCHECK` enthalten, wenn andere Services oder Orchestrierung darauf angewiesen sind.
10. Java-Container-Anwendungen MÜSSEN containerbewusste JVM-Optionen verwenden (`-XX:MaxRAMPercentage`, `-XX:+ExitOnOutOfMemoryError`).
11. `.dockerignore` MUSS in jedem Projekt vorhanden sein und mindestens `target/`, `build/`, `.git/`, `*.env*`, IDE-Verzeichnisse ausschließen.
12. Image-Tags MÜSSEN versioniert sein. `latest` DARF NICHT für produktionsnahe Images verwendet werden.
13. Produktive Secrets MÜSSEN über Secret Stores (Vault, Cloud Secret Manager, Kubernetes Secrets) bereitgestellt werden — niemals in Image, Dockerfile oder Compose-Datei.
14. Build-Time-Secrets MÜSSEN über BuildKit-Secrets (`--mount=type=secret`) übergeben werden, nicht als Build-Argument oder ENV.
15. Compose-Dateien MÜSSEN im Repository versioniert sein, wenn sie für das Projekt notwendig sind.
16. Healthchecks von abhängigen Services MÜSSEN über `depends_on: condition: service_healthy` durchgesetzt werden, wenn die Reihenfolge wichtig ist.
17. Docker und Linux-Kernel MÜSSEN regelmäßig aktualisiert werden.
18. Image-Scanning auf bekannte CVEs MUSS für produktionsnahe Images in CI laufen.
19. Für CRA-relevante Software MUSS ein SBOM generiert und versioniert werden (siehe Sektion 21).

### 3.2 DARF-NICHT-Regeln

Ein Docker-Setup DARF NICHT:

1. Docker mit `curl ... | sh` aus uncodierten Remote-Skripten auf produktionsnahen Systemen installieren.
2. Veraltete Distributionspakete (`docker.io`, `docker-compose` v1) als Standard verwenden.
3. Docker-Pakete aus gemischten Quellen installieren (Docker-APT-Repo + Debian-Repo + manuelle `.deb`s).
4. `docker-compose` v1 als CLI-Standard für neue Projekte voraussetzen.
5. Container ohne fachliche Notwendigkeit mit `--privileged` starten.
6. Den Docker-Socket (`/var/run/docker.sock`) ohne explizite Architekturentscheidung in Container mounten.
7. Host-Verzeichnisse wie `/`, `/etc`, `/home` ungeschützt in Container mounten.
8. Produktive Secrets, Tokens, API-Keys, Datenbank-Passwörter in Dockerfile, Image-Layer oder Compose-Datei speichern.
9. Anwendungen als root im Container laufen lassen, wenn die Anwendung selbst keine root-Privilegien benötigt.
10. `latest` als Tag für produktionsnahe Images verwenden.
11. Blind `docker system prune -a --volumes` ausführen — Volumes können Datenbank-Daten enthalten.
12. Ports auf gemeinsam genutzten Servern unbedacht exponieren.

### 3.3 SOLLTE-Regeln

Ein Docker-Setup SOLLTE:

1. Rootless Mode oder `userns-remap` prüfen, wenn erhöhter Sicherheitsbedarf besteht.
2. BuildKit Cache Mounts (`--mount=type=cache`) für Maven-/Gradle-Caches verwenden.
3. Spring Boot Layered JARs für effizientere Image-Layer einsetzen.
4. Cloud Native Buildpacks (`spring-boot:build-image`) als Dockerfile-freie Alternative erwägen.
5. Distroless oder Alpine als Runtime-Basis prüfen, wenn Image-Größe und Angriffsfläche kritisch sind.
6. Compose Profiles für optionale Services in lokalen Entwicklungsumgebungen verwenden.
7. Multi-Architektur-Images (linux/amd64, linux/arm64) bauen, wenn Apple-Silicon-Entwickler beteiligt sind.
8. Falco oder gleichwertige Runtime-Security-Tools auf Server-Setups erwägen.
9. SBOM-Generierung in CI integrieren — auch wenn CRA-Compliance noch nicht zwingend erforderlich ist.
10. Ressourcenlimits (`mem_limit`, `cpus`) auf gemeinsam genutzten Systemen setzen.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Debian-basierte Entwicklerarbeitsplätze, Linux-VMs, Build-Hosts, einfache Testserver, lokale Java-/Spring-Boot-Entwicklungsumgebungen und projektnahe Docker-Compose-Setups.

Sie gilt insbesondere für:

* Installation von Docker Engine auf Debian/Linux
* Einrichtung des offiziellen Docker-APT-Repository inkl. GPG-Fingerprint-Verifikation
* Installation von Docker CLI, Docker Engine, containerd, Buildx und Compose Plugin
* grundlegende Systemprüfung nach der Installation
* Benutzerrechte und `docker`-Gruppe
* Rootless Mode, `userns-remap` als Sicherheitsoptionen
* Docker Compose v2 für lokale Entwicklungsumgebungen
* Dockerfile-Grundregeln für Java-Anwendungen inkl. Multi-Stage, Layered JARs, JVM-Container-Optionen
* Cloud Native Buildpacks als Dockerfile-Alternative
* BuildKit-Features (Cache Mounts, Multi-Arch, Build-Secrets)
* Umgang mit Volumes, Netzwerken, Logs und Ressourcen
* Container Runtime Security (Falco, gVisor, Kata Containers)
* SBOM-Generierung und EU CRA-Kontext
* Security- und SaaS-Risiken durch Fehlkonfiguration

Diese Richtlinie gilt nicht als vollständiger Standard für:

* Kubernetes-Produktionsbetrieb
* Docker Swarm
* Enterprise Registry Governance
* vollständige CIS-Docker-Benchmark-Umsetzung
* Cloud-spezifische Containerplattformen
* Air-gapped Enterprise-Installationen
* hochverfügbare Produktions-Cluster
* Runtime-Security mit kompletter EDR/CNAPP/SIEM-Integration

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Docker Engine | Serverkomponente, die Container erstellt, startet, stoppt und verwaltet | `dockerd` |
| Docker CLI | Kommandozeilenwerkzeug zur Steuerung des Docker Daemons | `docker ps`, `docker run`, `docker build` |
| Docker Daemon | Hintergrunddienst mit weitreichenden Rechten auf dem Host | `systemctl status docker` |
| containerd | Standard-Container-Runtime, die von Docker Engine verwendet wird | systemd-Dienst `containerd.service` |
| runc | Low-Level-OCI-Runtime, die einzelne Container ausführt | wird von containerd aufgerufen |
| Image | Unveränderliches Artefakt aus Schichten, aus dem Container gestartet werden | `eclipse-temurin:21-jre-jammy` |
| Image-Tag | Veränderbarer Verweis auf eine Image-Version | `21-jre-jammy`, `latest` |
| Image-Digest | Unveränderliche, kryptografische Identität eines Images | `sha256:abc123...` |
| Container | Laufende Instanz eines Images | `docker run nginx` |
| Volume | Von Docker verwalteter persistenter Speicher | PostgreSQL-Daten in `pgdata` |
| Bind Mount | Direkte Einbindung eines Host-Verzeichnisses in einen Container | `./app:/app` |
| Network | Virtuelles Docker-Netzwerk für Container-Kommunikation | `backend-network` |
| Docker Compose v2 | Werkzeug zur Beschreibung und Ausführung mehrerer Container; CLI-Plugin | `compose.yaml`, `docker compose up` |
| BuildKit | Moderne Build-Engine in Docker mit Caching, Multi-Stage und Multi-Arch | Default seit Docker 23+ |
| Buildx | CLI-Erweiterung für moderne BuildKit-Builds und Multi-Arch | `docker buildx build --platform linux/amd64,linux/arm64` |
| Multi-Stage Build | Build-Pattern mit mehreren `FROM`-Stages, oft Build-Stage und Runtime-Stage | siehe Sektion 12.2 |
| Layered JAR | Spring-Boot-Mechanismus, JARs in mehrere Image-Layer zu zerlegen | `java -Djarmode=layertools -jar ... extract` |
| Cloud Native Buildpacks | Dockerfile-freier Build-Mechanismus, OCI-Image aus Quelltext | `./mvnw spring-boot:build-image` |
| Distroless | Minimal-Image ohne Shell, Package-Manager oder unnötige Tools | `gcr.io/distroless/java21-debian12` |
| Rootless Mode | Betriebsmodus, bei dem Daemon und Container ohne Root-Rechte laufen | reduziert Host-Gefährdung |
| userns-remap | Daemon-Modus, der Container-User-IDs auf nicht-privilegierte Host-IDs mappt | `/etc/docker/daemon.json` mit `"userns-remap": "default"` |
| `docker`-Gruppe | Unix-Gruppe, deren Mitglieder Docker ohne `sudo` steuern dürfen | sicherheitskritisch, faktisch root-nah |
| Healthcheck | Mechanismus, der den Zustand eines Containers regelmäßig prüft | `HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health` |
| SBOM | Software Bill of Materials — maschinenlesbare Komponentenliste eines Artefakts | `sbom.spdx.json`, `sbom.cyclonedx.json` |
| EU CRA | Cyber Resilience Act, EU-Verordnung 2024/2847, in Kraft ab 2027 | verlangt SBOM und Sicherheits-Updates für viele Software-Produkte |

---

## 6. Technischer Hintergrund

Docker besteht nicht nur aus einem einzelnen Programm. Für Entwickler ist `docker` meist das CLI-Kommando. Technisch gehört dazu aber ein Stack aus Docker Engine, Docker Daemon, Docker CLI, containerd, runc, BuildKit, Buildx, Images, Containern, Netzwerken, Volumes und optional Docker Compose.

Ein Container ist kein vollständiger virtueller Rechner. Container teilen sich den Kernel des Hosts. Daraus folgt: Die Sicherheit des Hosts, des Kernels, des Docker Daemons und der Container-Konfiguration ist direkt relevant für die Sicherheit der laufenden Anwendungen. Docker kann Isolation verbessern, aber falsche Konfigurationen können die Host-Sicherheit deutlich verschlechtern.

Für Java-Teams ist Docker vor allem aus vier Gründen wichtig:

1. Lokale Abhängigkeiten wie PostgreSQL, MySQL, Redis, Kafka oder Mailhog können reproduzierbar gestartet werden.
2. Integrationstests können gegen realistischere Infrastruktur laufen (Testcontainers nutzt Docker).
3. Anwendungen können als Images gebaut und in CI/CD geprüft werden.
4. Laufzeitumgebungen werden expliziter, weil Betriebssystem, JDK, Ports, Umgebungsvariablen, JVM-Optionen und Startkommando im Image beschrieben sind.

Docker löst aber keine Architekturprobleme. Ein schlechter Service wird durch einen Container nicht sauber. Ein unsicheres Secret bleibt unsicher. Eine fehlende Tenant-Isolation wird nicht durch Docker repariert. Ein falsch konfigurierter Port ist auch dann gefährlich, wenn er aus einem Container kommt.

**Java in Containern: was sich seit Java 8 geändert hat.** Java 10+ ist container-aware (`UseContainerSupport=true` ist Default), Java 17+ erkennt cgroup v2, Java 21+ hat verbesserte Heap-Heuristiken. **Aber:** `MaxRAMPercentage` ist standardmäßig **25 %**, was für moderne Container-Workloads viel zu konservativ ist. Bei einem Container mit 1 GB Memory-Limit nutzt die JVM nur 256 MB — der Rest verfällt. Container-Tuning ist deshalb auch in Java 21 eine Pflichtaufgabe (siehe Sektion 12.2).

**BuildKit** ist die moderne Build-Engine in Docker, Default seit Docker 23. Sie bietet parallele Layer-Builds, Cache Mounts, Build-Secrets, Multi-Arch und vieles mehr. Wer noch mit Legacy-Builder arbeitet, verschenkt massive Performance- und Sicherheits-Verbesserungen.

**Compose v2 vs. v1.** Compose v1 (`docker-compose`) ist seit 2023 deprecated. Compose v2 (`docker compose`, mit Leerzeichen, als CLI-Plugin) ist der Standard. Funktionale Unterschiede: v2 ist in Go geschrieben (statt Python), unterstützt Profiles, ist deutlich schneller und ist Teil der Docker-Engine-Installation.

---

## 7. Debian-Installation Schritt für Schritt

### 7.1 System prüfen

Vor der Installation MUSS geprüft werden, auf welchem Debian-System gearbeitet wird.

```bash
cat /etc/os-release
uname -m
```

Erwartete Ausgabe für ein aktuelles Debian-System ist beispielsweise `bookworm` oder `trixie` als Version Codename. Die Architektur sollte zu den von Docker unterstützten Architekturen passen, typischerweise `x86_64` (entspricht `amd64`) oder `aarch64` (entspricht `arm64`).

### 7.2 Alte oder kollidierende Pakete entfernen

Vor der offiziellen Installation müssen kollidierende Pakete entfernt werden. Die offizielle Docker-Empfehlung verwendet eine **robuste Schleife**, die nicht bei leeren Argumenten oder fehlenden Paketen abbricht:

```bash
# ✅ Offizielle Docker-Empfehlung — robust gegen leere Argumente
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do
    sudo apt-get remove -y "$pkg" || true
done
```

Wenn ein Paket nicht installiert ist, gibt `apt-get remove` eine harmlose Meldung aus und arbeitet weiter. Die Schleife behandelt jedes Paket einzeln und scheitert nicht an leeren Listen.

**Was nicht funktioniert** (alter Stil aus Tutorials):

```bash
# ❌ scheitert auf fresh Debian-Systemen
sudo apt remove $(dpkg --get-selections docker.io ... | cut -f1)
```

Auf einem frischen Debian-System ist die Subshell leer → `apt remove` ohne Argumente wirft Fehler.

### 7.3 Benötigte Basispakete installieren

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
```

`gnupg` wird für die Fingerprint-Verifikation in Schritt 7.4 benötigt.

### 7.4 Docker-GPG-Key herunterladen und Fingerprint verifizieren

**GPG-Key herunterladen:**

```bash
sudo install -m 0755 -d /etc/apt/keyrings

sudo curl -fsSL https://download.docker.com/linux/debian/gpg \
  -o /etc/apt/keyrings/docker.asc

sudo chmod a+r /etc/apt/keyrings/docker.asc
```

**Fingerprint verifizieren — Pflichtschritt für Sicherheits-Setup:**

```bash
gpg --show-keys --with-fingerprint /etc/apt/keyrings/docker.asc
```

Erwartete Ausgabe (Stand 2026, Docker Inc.):

```
pub   rsa4096 2017-02-22 [SCEA]
      9DC8 5822 9FC7 DD38 854A  E2D8 8D81 803C 0EBF CD88
uid                      Docker Release (CE deb) <docker@docker.com>
sub   rsa4096 2017-02-22 [S]
```

**Wenn der Fingerprint nicht exakt `9DC8 5822 9FC7 DD38 854A E2D8 8D81 803C 0EBF CD88` lautet:**
Installation abbrechen, Datei löschen, Netzwerk und CDN prüfen. Eine Doc, die sich „absichern" nennt, akzeptiert keinen unverifizierten Key.

### 7.5 Docker-APT-Repository einrichten (DEB822-Format)

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

Das DEB822-Format (`.sources` statt `.list`) ist die moderne Form für Debian 12+ und bindet den GPG-Key explizit über `Signed-By`.

```bash
sudo apt update
```

### 7.6 Docker Engine, CLI, containerd, Buildx und Compose installieren

```bash
sudo apt install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin
```

### 7.7 Dienststatus prüfen und aktivieren

```bash
sudo systemctl status docker
sudo systemctl status containerd
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

### 7.8 Installation verifizieren

```bash
sudo docker run --rm hello-world
```

Danach:

```bash
docker version
docker compose version
docker buildx version
```

Erwartete Ausgaben:
- `docker version`: Docker Engine 27.x oder neuer
- `docker compose version`: v2.x
- `docker buildx version`: v0.18+ oder neuer

Wenn `docker version` ohne `sudo` noch nicht funktioniert, ist das erwartbar, solange der Benutzer nicht in der `docker`-Gruppe ist oder Rootless Mode nicht eingerichtet wurde (siehe Sektion 8).

---

## 8. Benutzerrechte nach der Installation

Die Frage „Wer darf Docker steuern?" ist eine Sicherheitsentscheidung, keine Komfortentscheidung. Drei Optionen, drei Trade-offs.

### 8.1 Option A: Docker mit `sudo` verwenden

Für sicherheitsbewusste Einzelmaschinen oder Admin-Kontexte ist es akzeptabel, Docker bewusst mit `sudo` zu verwenden.

```bash
sudo docker ps
sudo docker compose up
```

**Vorteil:** Keine dauerhafte Erweiterung der Benutzerrechte.
**Nachteil:** Weniger bequem im Entwickleralltag.
**Wann:** Server, gemeinsam genutzte Systeme, Admin-Sessions, einmalige Setup-Aufgaben.

### 8.2 Option B: Benutzer zur `docker`-Gruppe hinzufügen

Für typische Entwicklerarbeitsplätze ist häufig gewünscht, Docker ohne `sudo` auszuführen. Das ist bequem, aber sicherheitskritisch.

> **Wer in der `docker`-Gruppe ist, kann mit einem einzigen `docker run`-Kommando Host-Root-Rechte erlangen** — z. B. durch `docker run -v /:/host -it ubuntu chroot /host`. Mitgliedschaft in der `docker`-Gruppe ist faktisch root-nah und MUSS so behandelt werden.

```bash
sudo groupadd docker         # falls noch nicht vorhanden
sudo usermod -aG docker "$USER"
newgrp docker                # neue Gruppe in der aktuellen Shell aktivieren
```

Prüfung:

```bash
docker run --rm hello-world
```

Wichtig: Nach `usermod` kann ein Logout/Login oder Neustart nötig sein, damit alle Shells und Services die Gruppenmitgliedschaft sehen.

Wenn vorher Docker-Kommandos mit `sudo` ausgeführt wurden, kann `~/.docker` falsche Rechte haben:

```bash
sudo chown "$USER":"$USER" "$HOME/.docker" -R
sudo chmod g+rwx "$HOME/.docker" -R
```

**Wann:** Einzelner Entwickler-Arbeitsplatz, kein Mehrbenutzer-Server, der Benutzer ist ohnehin Admin der Maschine.

### 8.3 Option C: Rootless Mode

Rootless Mode ist eine Sicherheitsoption, bei der Docker Daemon und Container innerhalb eines User Namespace laufen. Das reduziert Host-Risiken massiv, kann aber Einschränkungen haben.

**Setup:**

```bash
sudo apt install -y docker-ce-rootless-extras uidmap

# Als nicht-privilegierter Benutzer:
dockerd-rootless-setuptool.sh install

# Daemon-Socket-Variable setzen
echo 'export DOCKER_HOST=unix:///run/user/'$(id -u)'/docker.sock' >> ~/.bashrc
source ~/.bashrc
```

**Einschränkungen:**

- Ports < 1024 sind ohne zusätzliche Konfiguration nicht erreichbar.
- Manche Storage-Driver werden nicht unterstützt.
- Manche Netzwerk-Features (Multicast, Raw IP) sind eingeschränkt.
- Performance kann bei IO-intensiven Workloads leiden.
- Manche Volumes / Bind Mounts müssen anders konfiguriert werden.

**Wann:** Entwickler-Arbeitsplätze mit erhöhtem Sicherheitsbedarf, Sicherheits-Pilotprojekte, Multi-Mandanten-Hosts.

### 8.4 Option D: User Namespace Remapping (`userns-remap`)

Ein Mittelweg zwischen Standard-Modus und Rootless Mode: Docker-Daemon läuft als Root, aber Container-User-IDs werden auf nicht-privilegierte Host-User-IDs gemappt.

```json
// /etc/docker/daemon.json
{
  "userns-remap": "default"
}
```

```bash
sudo systemctl restart docker
```

Damit ist UID 0 im Container ≠ UID 0 auf dem Host. Das verhindert eine ganze Klasse von Container-Escape-Angriffen.

**Trade-offs:**

- Volume-Permissions werden komplizierter — Host-Dateien müssen den gemappten UIDs gehören.
- Manche Tools (z. B. Kafka mit File-Owner-Annahmen) brechen.
- Kann mit bestimmten Drivern und Mount-Patterns interagieren.

**Wann:** Server-Setups mit moderatem Sicherheitsbedarf, wo Rootless Mode zu restriktiv ist, aber Standard-Modus zu offen.

### 8.5 Auswahlmatrix

| Setup | Empfehlung |
|---|---|
| Persönlicher Entwickler-Laptop, Single-User | Option B (`docker`-Gruppe) |
| Persönlicher Entwickler-Laptop, hoher Sicherheitsbedarf | Option C (Rootless) |
| Geteilter Build-Server | Option D (`userns-remap`) oder Option A (`sudo`) |
| Multi-Tenant-Host | Option C (Rootless) oder Option D |
| Production-Host | NICHT Docker direkt — Kubernetes, Nomad oder Cloud-Plattform |

---

## 9. Gutes Beispiel: Sauberes Setup-Skript für Debian

Ein Setup-Skript DARF nur verwendet werden, wenn es im Repository versioniert, lesbar und reviewt ist. Es darf keine unkontrollierten Remote-Skripte ausführen.

```bash
#!/usr/bin/env bash
# install-docker-debian.sh
# Reproducible Docker installation for Debian 12 / 13
# Verifies GPG fingerprint before adding APT source

set -euo pipefail

EXPECTED_FINGERPRINT="9DC8 5822 9FC7 DD38 854A  E2D8 8D81 803C 0EBF CD88"

echo "==> Prüfe Debian-Version ..."
. /etc/os-release
echo "    Distribution: ${PRETTY_NAME}"
echo "    Codename: ${VERSION_CODENAME}"
echo "    Architektur: $(dpkg --print-architecture)"

echo "==> Entferne kollidierende Pakete ..."
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do
    sudo apt-get remove -y "$pkg" 2>/dev/null || true
done

echo "==> Installiere Basisabhängigkeiten ..."
sudo apt update
sudo apt install -y ca-certificates curl gnupg

echo "==> Lade Docker-GPG-Key ..."
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/debian/gpg \
    -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo "==> Verifiziere GPG-Fingerprint ..."
ACTUAL_FINGERPRINT=$(gpg --show-keys --with-fingerprint --with-colons /etc/apt/keyrings/docker.asc \
    | awk -F: '/^fpr:/ {print $10; exit}' \
    | sed 's/.\{4\}/& /g; s/ $//')

if [[ "${ACTUAL_FINGERPRINT}" != "${EXPECTED_FINGERPRINT}" ]]; then
    echo "FEHLER: GPG-Fingerprint stimmt nicht überein!"
    echo "Erwartet: ${EXPECTED_FINGERPRINT}"
    echo "Erhalten: ${ACTUAL_FINGERPRINT}"
    sudo rm -f /etc/apt/keyrings/docker.asc
    exit 1
fi
echo "    Fingerprint OK: ${ACTUAL_FINGERPRINT}"

echo "==> Richte Docker-APT-Quelle ein (DEB822-Format) ..."
sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/debian
Suites: ${VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

echo "==> Installiere Docker Engine und Plugins ..."
sudo apt update
sudo apt install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin

echo "==> Aktiviere Docker-Dienste ..."
sudo systemctl enable docker.service
sudo systemctl enable containerd.service
sudo systemctl start docker

echo "==> Verifiziere Installation ..."
sudo docker run --rm hello-world
docker version --format '{{.Server.Version}}' | xargs -I {} echo "    Docker Engine: {}"
docker compose version --short | xargs -I {} echo "    Compose: {}"
docker buildx version | head -1 | xargs -I {} echo "    Buildx: {}"

echo ""
echo "✅ Installation abgeschlossen."
echo ""
echo "Nächste Schritte:"
echo "  1. Bewusst entscheiden: docker-Gruppen-Mitgliedschaft, Rootless Mode oder sudo?"
echo "  2. Bei docker-Gruppe: 'sudo usermod -aG docker \$USER' und neu einloggen."
echo "  3. Bei Rootless Mode: 'dockerd-rootless-setuptool.sh install' (siehe Sektion 8.3)."
```

---

## 10. Schlechtes Beispiel: Unsicheres Copy-Paste-Setup

```bash
# ❌ Anti-Pattern, niemals so verwenden
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
docker run -v /:/host -it ubuntu bash
```

Warum ist das schlecht?

* `curl ... | sh` führt ein Remote-Skript ohne Review aus. Wer das Skript zwischen Download und Ausführung manipuliert, kontrolliert die Installation.
* Die Installation ist nicht reproduzierbar versioniert — bei jedem Lauf kann etwas anderes installiert werden.
* Der Benutzer erhält faktisch root-nahe Docker-Rechte, ohne bewusste Sicherheits-Entscheidung.
* `-v /:/host` mountet das gesamte Host-Filesystem in den Container. Das ist eine direkte Pfad-zu-Host-Root-Übernahme.
* Es gibt kein Update-, Logging-, Rechte- oder Deinstallationskonzept.
* Kein GPG-Fingerprint, keine APT-Quelle, kein Rollback-Pfad.

`get.docker.com` ist ein offizielles Docker-Skript und für sich genommen nicht bösartig — aber es ist explizit für **Test- und Convenience-Setups** gedacht, nicht für produktionsnahe Installationen. Die offizielle Docker-Doku selbst empfiehlt für Produktionssysteme den APT-Repository-Weg aus Sektion 7.

---

## 11. Docker Compose als lokaler Entwicklungsstandard

Docker Compose v2 (`docker compose`) wird verwendet, wenn mehrere Dienste lokal zusammen gestartet werden müssen, etwa Java-App, Datenbank, Redis, Kafka, Mailserver oder Observability-Komponenten.

### 11.1 Gutes Compose-Beispiel mit Healthcheck-Abhängigkeit

```yaml
# compose.yaml
services:
  postgres:
    image: postgres:16.4
    container_name: example-postgres
    environment:
      POSTGRES_DB: example
      POSTGRES_USER: example
      POSTGRES_PASSWORD: example-dev-password   # lokal, nicht produktiv
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U example -d example"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    networks:
      - backend

  app:
    image: example-app:dev
    container_name: example-app
    depends_on:
      postgres:
        condition: service_healthy             # ✅ wartet auf Healthcheck
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/example
      SPRING_DATASOURCE_USERNAME: example
      SPRING_DATASOURCE_PASSWORD: example-dev-password
      SPRING_PROFILES_ACTIVE: dev
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 60s
    mem_limit: 1g
    cpus: 1.0
    networks:
      - backend

  mailhog:
    image: mailhog/mailhog:v1.0.1
    profiles: ["mail"]                          # ✅ optional, nur mit --profile mail
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
docker compose up -d                            # nur app + postgres
docker compose --profile mail up -d             # zusätzlich mailhog
docker compose ps
docker compose logs -f app
docker compose down                             # stoppt, Volumes bleiben
docker compose down -v                          # ⚠️ löscht auch Volumes
```

### 11.2 Compose-Profiles für optionale Services

Compose v2 unterstützt **Profiles** — ein hervorragendes Feature für lokale Entwicklung mit optionalen Komponenten:

```yaml
services:
  app:
    image: example-app:dev
    # immer aktiv

  postgres:
    image: postgres:16.4
    profiles: ["db", "default"]               # in default und db

  observability:
    image: grafana/grafana
    profiles: ["observability"]               # nur mit --profile observability

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    profiles: ["messaging"]                   # nur mit --profile messaging
```

```bash
docker compose up                                  # app, postgres
docker compose --profile messaging up              # + kafka
docker compose --profile observability up          # + grafana
docker compose --profile messaging --profile observability up   # alles
```

In Java-Teams mit verschiedenen Aufgaben (Backend-Entwickler braucht DB, Performance-Engineer braucht Grafana, Integration-Tester braucht Kafka) ist das ein massiver Komfortgewinn.

### 11.3 Compose-Regeln

* Compose-Dateien MÜSSEN im Repository liegen, wenn sie für das Projekt notwendig sind.
* Die kanonische Datei heißt `compose.yaml` (nicht `docker-compose.yml`, das ist Legacy).
* Lokale Entwicklungs-Passwörter DÜRFEN nur für lokale Container verwendet werden.
* Produktive Secrets DÜRFEN NICHT in `compose.yaml` stehen.
* Ports MÜSSEN bewusst exponiert werden — `expose:` (intern) vs. `ports:` (auf Host).
* Volumes MÜSSEN benannt sein, wenn Daten erhalten bleiben sollen.
* `docker compose down -v` MUSS bewusst eingesetzt werden, weil Volumes gelöscht werden.
* Services SOLLTEN Healthchecks besitzen, wenn andere Dienste von ihnen abhängen.
* `depends_on: condition: service_healthy` SOLLTE statt einfachem `depends_on:` verwendet werden, wenn Reihenfolge wichtig ist.
* Profiles SOLLTEN verwendet werden, um optionale Services zu kapseln.

---

## 12. Dockerfile-Standard für Java-Anwendungen

### 12.1 Schlechtes Dockerfile

```dockerfile
# ❌ Anti-Pattern, niemals so verwenden
FROM openjdk:latest

COPY . /app
WORKDIR /app

RUN ./mvnw package

EXPOSE 8080

CMD ["java", "-jar", "target/app.jar"]
```

Warum ist das schlecht?

* `latest` ist nicht reproduzierbar — der Build kann morgen ein anderes Image liefern.
* `openjdk` ist seit 2024 deprecated — Eclipse Temurin oder Amazon Corretto sind die Nachfolger.
* Build- und Runtime-Umgebung sind vermischt. Maven, JDK, Build-Cache landen alles im finalen Image.
* `COPY . /app` kopiert den gesamten Quellcode ins Image, inklusive `.git/`, IDE-Verzeichnissen, `.env`-Dateien.
* Keine `.dockerignore`.
* Anwendung läuft als Root.
* Keine `HEALTHCHECK`.
* Keine JVM-Container-Optionen → Heap-Größe falsch berechnet.
* Keine klare Versionierung.

### 12.2 Produktionsreifes Dockerfile für Spring Boot 3.4 / Java 21

```dockerfile
# syntax=docker/dockerfile:1.7
# ==========================================================================
# Build Stage
# ==========================================================================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

# Copy build files (small, rarely changes)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source last (changes most often)
COPY src src

# ✅ BuildKit Cache Mount — Maven-Cache überlebt Image-Rebuilds
RUN --mount=type=cache,target=/root/.m2,id=maven-cache \
    ./mvnw -q -B clean package -DskipTests

# Extract Spring Boot Layered JAR for optimal Docker layer caching
RUN java -Djarmode=layertools -jar target/*.jar extract --destination layers/

# ==========================================================================
# Runtime Stage
# ==========================================================================
FROM eclipse-temurin:21-jre-jammy

# ✅ Install curl for HEALTHCHECK
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# ✅ Non-root user
RUN groupadd --system --gid 1001 app \
    && useradd --system --uid 1001 --gid app --no-create-home app

WORKDIR /app

# ✅ Copy layered JAR contents — most stable layers first
# This order maximizes Docker layer cache hits when only application code changes.
COPY --from=build --chown=app:app /workspace/layers/dependencies/ ./
COPY --from=build --chown=app:app /workspace/layers/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/layers/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/layers/application/ ./

USER app

# ✅ Container-aware JVM options
# - MaxRAMPercentage=75: use 75% of container memory (default 25% is too conservative)
# - ExitOnOutOfMemoryError: hard crash on OOM so orchestrator can restart
# - G1GC: low-pause garbage collector
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 \
                       -XX:InitialRAMPercentage=50.0 \
                       -XX:+ExitOnOutOfMemoryError \
                       -XX:+UseG1GC \
                       -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# ✅ HEALTHCHECK against Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health || exit 1

# ✅ Use JarLauncher to take advantage of layered JAR
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

**Was hier richtig gemacht wird:**

| Element | Begründung |
|---|---|
| `# syntax=docker/dockerfile:1.7` | aktiviert BuildKit-Features (Cache Mounts, Build-Secrets) |
| Multi-Stage | Build-Tools (Maven, JDK) bleiben im Build-Image, nicht im Runtime |
| `eclipse-temurin:21-jdk-jammy` (Build) und `21-jre-jammy` (Runtime) | versioniert, kein `latest`, JDK für Build, JRE für Runtime |
| `--mount=type=cache,target=/root/.m2` | Maven-Cache überlebt Image-Rebuilds, Build-Zeit drastisch reduziert |
| Layered JAR extraction | Layer-Cache wird optimal genutzt: Dependencies (stabil) vs. Application (volatil) |
| `useradd --system --uid 1001 --gid app` | feste UID/GID für reproducibility und Volume-Permissions |
| `COPY --chown=app:app` | Datei-Ownership korrekt von Anfang an, nicht nachträgliches `chown` |
| `JAVA_TOOL_OPTIONS` | Container-bewusste JVM-Settings, die JVM nutzt 75% statt 25% des Memory |
| `+ExitOnOutOfMemoryError` | bei OOM crasht JVM hart, Orchestrator startet neu |
| `HEALTHCHECK` | Spring Boot Actuator `/actuator/health` |
| `JarLauncher` | nutzt Layered-JAR-Struktur korrekt |

### 12.3 `.dockerignore`

Pflicht-Datei in jedem Java-Projekt:

```
# .dockerignore
# Build output
target/
build/
out/
*.class

# Version control
.git/
.gitignore
.gitattributes

# IDE
.idea/
.vscode/
.eclipse/
*.iml
*.iws
*.ipr

# Logs and runtime
*.log
*.pid
hs_err_pid*

# Environment files
.env
.env.*
!.env.example

# Compose overrides
docker-compose.override.yml
compose.override.yaml

# Documentation that doesn't belong in image
README.md
docs/
*.md
!HEALTH.md

# OS
.DS_Store
Thumbs.db

# Test artifacts
.junit/
.pytest_cache/
coverage/
```

**Warum das wichtig ist:**

- Verhindert versehentliche Geheimnis-Lecks (`.env`-Dateien).
- Reduziert Build-Context-Größe drastisch (kein `.git/`, kein `target/`).
- Beschleunigt `docker build` durch kleineren Build-Context-Upload.
- Verhindert Kollision zwischen Host-`target/` und Container-`/workspace/target/`.

### 12.4 Distroless oder Alpine als Runtime-Alternative

`eclipse-temurin:21-jre-jammy` ist eine sichere, kompatible Wahl. Für **produktionsnahe Images** mit erhöhtem Sicherheitsanspruch sind kleinere Basisimages eine Überlegung wert:

| Image | Größe | Vor- und Nachteile |
|---|---|---|
| `eclipse-temurin:21-jre-jammy` | ~340 MB | Ubuntu 22.04, voll-featured, `bash`, `apt`, `curl` |
| `eclipse-temurin:21-jre-noble` | ~330 MB | Ubuntu 24.04 (neuer LTS) |
| `eclipse-temurin:21-jre-alpine` | ~190 MB | musl libc — Achtung: subtile JVM-Inkompatibilitäten möglich |
| `gcr.io/distroless/java21-debian12` | ~220 MB | kein Shell, kein Package-Manager, minimal — Distroless |

**Distroless-Beispiel:**

```dockerfile
# Build stage unchanged
FROM eclipse-temurin:21-jdk-jammy AS build
# ...

# Runtime stage with Distroless
FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /workspace/layers/ ./

USER nonroot:nonroot                            # Distroless hat eingebauten nonroot-User

EXPOSE 8080

# ⚠️ HEALTHCHECK schwierig — kein curl in Distroless
# Alternative: Spring Boot Actuator probe-friendly endpoints + Kubernetes liveness/readiness probes

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

**Trade-offs Distroless:**

- **Pro:** drastisch reduzierte Angriffsfläche (kein Shell-Exploit, keine spätere Tool-Installation).
- **Pro:** kleinere Images, weniger CVEs, weniger Patch-Pflicht.
- **Pro:** built-in nonroot-User.
- **Contra:** kein `docker exec -it ... bash` zum Debugging.
- **Contra:** `HEALTHCHECK` braucht alternative Methoden (z. B. Kubernetes Probes, oder eingebauter Spring-Boot-Mechanismus).
- **Contra:** kein `apt install` möglich, falls etwas fehlt.

**Wann Distroless:** Production-Workloads in Kubernetes mit klar definierten Probes. Image-Größe ist relevant. Security-Anforderungen sind hoch.

**Wann nicht:** Lokale Entwicklung, Setups ohne Orchestrierung, Debugging-intensive Phasen.

### 12.5 Regeln für Java-Images

| Regel | Sektion |
|---|---|
| Multi-Stage Build | §12.2 |
| Versionierter Tag, kein `latest` | §12.1 (anti), §3.1.12 |
| `--chown=app:app` bei `COPY` | §12.2 |
| Nicht-Root-User mit `USER` | §12.2, §3.1.7 |
| `HEALTHCHECK` bei Service-relevanten Containern | §12.2, §3.1.9 |
| JVM-Container-Optionen via `JAVA_TOOL_OPTIONS` | §12.2, §3.1.10 |
| `.dockerignore` | §12.3, §3.1.11 |
| BuildKit Cache Mount für Maven/Gradle | §12.2, §14 |
| Layered JAR für Spring Boot | §12.2 |
| Distroless / Alpine erwägen | §12.4 |
| Keine Secrets im Image | §12.1 (anti), §15.4 |
| Image-Scan in CI | §3.1.18, §23 |

---

## 13. Cloud Native Buildpacks als Dockerfile-Alternative

Spring Boot hat seit 2.3 ein eingebautes Build-Goal, das **OCI-Images ohne Dockerfile** erzeugt:

```bash
./mvnw spring-boot:build-image \
    -Dspring-boot.build-image.imageName=example-app:1.0.0
```

oder mit Gradle:

```bash
./gradlew bootBuildImage --imageName=example-app:1.0.0
```

Das nutzt **Cloud Native Buildpacks (CNB)** — eine CNCF-Spezifikation, die Container-Images aus Quelltext erzeugt.

### 13.1 Vorteile von Cloud Native Buildpacks

- **Automatische Security-Patches der Base-Layer.** Paketo Buildpacks (das Default-CNB-Provider von Spring Boot) pflegt die Base-Images aktiv.
- **Optimierte Layer-Struktur** — vergleichbar mit Spring Boot Layered JARs, aber automatisch.
- **Reproduzierbare Builds** — gleiche Source erzeugt gleiches Image.
- **SBOM-Erzeugung automatisch** (siehe Sektion 21).
- **Multi-Architektur-Support** out of the box.
- **Integrierte Best Practices** — Nicht-Root-User, JVM-Tuning, Health-Probes ohne Konfiguration.
- **Kein Dockerfile** — weniger Code, weniger Drift, weniger Wartung.

### 13.2 Konfiguration

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <image>
            <name>example-app:${project.version}</name>
            <env>
                <BP_JVM_VERSION>21</BP_JVM_VERSION>
                <BPE_APPEND_JAVA_TOOL_OPTIONS>-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError</BPE_APPEND_JAVA_TOOL_OPTIONS>
            </env>
        </image>
    </configuration>
</plugin>
```

### 13.3 Wann CNB statt Dockerfile

**CNB sinnvoll wenn:**

- Standard-Spring-Boot-Anwendung ohne ungewöhnliche System-Anforderungen.
- Team will Dockerfile-Wartung vermeiden.
- Automatische Security-Updates der Base-Layer sind erwünscht.
- SBOM und Multi-Arch sollen automatisch erzeugt werden.

**Dockerfile besser wenn:**

- Native Libraries oder spezifische OS-Pakete werden benötigt.
- Sehr feinkörnige Kontrolle über Layer-Struktur erforderlich.
- Bestehende Dockerfile-Patterns sollen weiter gepflegt werden.
- Distroless oder ähnlich spezialisierte Base-Images werden gefordert.

CNB und Dockerfile sind kein „entweder oder" für die ganze Organisation. Verschiedene Services können verschiedene Strategien wählen — wichtig ist, dass die Wahl bewusst und dokumentiert ist.

---

## 14. BuildKit-Features und Multi-Architektur

BuildKit ist die moderne Build-Engine in Docker (Default seit Docker 23). Ihre Features sind in produktiven Setups Pflicht.

### 14.1 BuildKit aktivieren

In Docker 23+ ist BuildKit Default. Falls explizit nötig:

```bash
export DOCKER_BUILDKIT=1
```

Oder in `/etc/docker/daemon.json`:

```json
{
  "features": {
    "buildkit": true
  }
}
```

Die erste Zeile im Dockerfile aktiviert die spezifische Syntax-Version:

```dockerfile
# syntax=docker/dockerfile:1.7
```

### 14.2 BuildKit Cache Mounts

Der Maven-/Gradle-Cache lebt **außerhalb** des Image-Layers und übersteht Image-Rebuilds:

```dockerfile
# Maven
RUN --mount=type=cache,target=/root/.m2,id=maven-cache \
    ./mvnw -q -B clean package -DskipTests

# Gradle
RUN --mount=type=cache,target=/home/gradle/.gradle,id=gradle-cache \
    ./gradlew clean bootJar --no-daemon
```

**Vorteile:**
- Erste Builds laden Dependencies in den Cache.
- Folgende Builds nutzen den Cache, **auch wenn `pom.xml` sich geändert hat**.
- Image bleibt klein (Cache wird nicht in den Layer geschrieben).
- Build-Zeit von 3 Minuten auf 30 Sekunden bei kleinen Code-Änderungen.

### 14.3 BuildKit Build-Secrets

Build-Time-Secrets (Maven-Credentials, NPM-Token, SSH-Keys) müssen aus Image-Layern ferngehalten werden:

```dockerfile
# Dockerfile
RUN --mount=type=secret,id=mvn_credentials,target=/root/.m2/settings.xml \
    ./mvnw -B package
```

```bash
# Build-Aufruf
docker build \
    --secret id=mvn_credentials,src=$HOME/.m2/settings.xml \
    -t example-app:1.0 .
```

**Was hier passiert:** `settings.xml` ist während `RUN` verfügbar, landet aber **nicht** im finalen Image-Layer. Klassisches Pattern für Maven-Credentials, NPM-Token, SSH-Keys, GitHub-Tokens.

**Anti-Pattern, niemals so:**

```dockerfile
# ❌ Secret im Layer
ARG MAVEN_TOKEN
RUN echo "<settings>...$MAVEN_TOKEN...</settings>" > /root/.m2/settings.xml && \
    ./mvnw package
```

`ARG` und `ENV` werden im Image-Layer gespeichert. Auch wenn man sie später überschreibt, sind sie über `docker history` extrahierbar.

### 14.4 Multi-Architektur-Builds

In Zeiten von Apple Silicon (ARM64) für Entwickler und x86_64 für Server ist Multi-Arch oft Pflicht:

```bash
# Builder erstellen
docker buildx create --name multi-arch --use

# Multi-Arch-Build
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t registry.example.com/example-app:1.0 \
    --push .
```

**Was hier passiert:**
- BuildKit baut zwei Varianten parallel: amd64 und arm64.
- Beide werden unter demselben Tag in der Registry abgelegt.
- Beim Pull wählt Docker automatisch die zur Plattform passende Variante.
- Für CI: Apple-Silicon-Mac-Mini-Runners pullen arm64, x86-Linux-Runners pullen amd64.

**Voraussetzung:** Registry muss Multi-Arch-Manifest unterstützen (Docker Hub, GitHub Container Registry, AWS ECR, Google Artifact Registry — alle unterstützen das).

### 14.5 Buildx Bake für komplexe Multi-Image-Builds

Bei Microservice-Repos mit mehreren Images:

```hcl
# docker-bake.hcl
group "default" {
  targets = ["api", "worker", "frontend"]
}

target "api" {
  context = "./services/api"
  dockerfile = "Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
  tags = ["registry.example.com/api:latest"]
}

target "worker" {
  context = "./services/worker"
  dockerfile = "Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
  tags = ["registry.example.com/worker:latest"]
}

target "frontend" {
  context = "./services/frontend"
  platforms = ["linux/amd64"]   # Frontend nur für Server
  tags = ["registry.example.com/frontend:latest"]
}
```

```bash
docker buildx bake --push
```

Vorteil: alle Images in einem Befehl, deklarativ konfiguriert, parallel gebaut.

---

## 15. Security- und SaaS-Aspekte

### 15.1 `docker`-Gruppe ist kein harmloser Komfort

Die `docker`-Gruppe ist sicherheitskritisch. Ein Benutzer mit Zugriff auf den Docker-Daemon kann Container mit Host-Mounts starten und dadurch weitreichend auf Host-Dateien einwirken. Beispiele:

```bash
# Faktisch root: Container mit Host-Root-Mount
docker run -v /:/host -it ubuntu chroot /host

# Faktisch root: Privilegierter Container
docker run --privileged -it ubuntu

# Faktisch root: Cap-Add ohne Limit
docker run --cap-add ALL -it ubuntu
```

Deshalb gilt:

* Nur vertrauenswürdige Benutzer DÜRFEN Mitglied der `docker`-Gruppe sein.
* Auf gemeinsam genutzten Servern MUSS die Mitgliedschaft restriktiv sein und auditiert werden.
* Für Build-Server MUSS geprüft werden, ob Runner-Prozesse Docker-Rechte wirklich benötigen.
* Docker-Socket-Mounts sind besonders kritisch (siehe 15.2).

### 15.2 Docker-Socket nicht in Container mounten

Dieses Muster ist gefährlich:

```yaml
# ❌ Niemals als Default
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

Ein Container mit Zugriff auf den Docker-Socket kann den Docker-Daemon kontrollieren. Das kann faktisch Host-Kontrolle bedeuten — der Container kann andere Container starten, die Host-Mounts haben, und sich darüber Host-Root holen.

Dieses Muster DARF nur in **bewusst freigegebenen Infrastrukturkomponenten** verwendet werden, etwa kontrollierten CI-Runnern, Watchtower-Setups oder spezialisierten Build-Agenten — und dann mit zusätzlichen Sicherheitsmaßnahmen wie Read-Only-Mount, AppArmor-Profilen oder Socket-Proxies.

**Sicherere Alternativen:**

- **Socket-Proxy (z. B. `tecnativa/docker-socket-proxy`):** Container greift nur auf Read-API zu, nicht auf Container-Management.
- **`/var/run/docker.sock` Read-Only mounten:** `:ro`-Flag verhindert Schreibzugriff.
- **Sysbox** oder **gVisor** als Runtime: kein direkter Socket-Zugriff erforderlich.

### 15.3 Keine privilegierten Container als Standard

```bash
# ❌ Niemals als Default
docker run --privileged ...
```

`--privileged` erweitert Containerrechte massiv und entfernt wesentliche Sicherheitsgrenzen — Capabilities, Seccomp-Profile, AppArmor, Device-Cgroups alle aufgehoben.

**Wenn wirklich nötig:** stattdessen einzelne Capabilities adden:

```bash
# ✅ Nur die wirklich benötigten Rechte
docker run --cap-add NET_ADMIN --cap-add NET_RAW ubuntu
```

Capabilities sind ein gut dokumentiertes Linux-Konzept, das fein granular vergeben werden kann. `--privileged` ist die Holzhammer-Variante und gehört nicht in normale Workloads.

### 15.4 Keine produktiven Secrets in Images oder Compose-Dateien

```dockerfile
# ❌ Niemals
ENV DB_PASSWORD=prod-secret
```

```yaml
# ❌ Niemals
environment:
  JWT_SECRET: "super-secret-production-key"
```

Stattdessen:

* **Lokale Entwicklung:** `.env`-Datei (nicht im Git!).
* **CI/CD:** Secret-Store (GitHub Secrets, GitLab CI Variables, Azure Key Vault).
* **Produktion:** Vault, Cloud Secret Manager, Kubernetes Secrets, AWS Secrets Manager.
* **Build-Time-Secrets:** BuildKit Secrets (siehe 14.3).
* **Tests:** synthetische Test-Secrets in dedizierten Test-Stores.

### 15.5 Port-Exposition bewusst steuern

```yaml
# Macht Port auf Host erreichbar
ports:
  - "5432:5432"
```

Auf Entwicklergeräten ist das häufig okay. Auf gemeinsam genutzten Servern kann es gefährlich sein. Eine Datenbank, die per IPv4 auf `0.0.0.0:5432` lauscht, ist im LAN sichtbar.

**Bessere Patterns:**

```yaml
# ✅ Nur an localhost binden
ports:
  - "127.0.0.1:5432:5432"

# ✅ Nur Compose-intern, kein Host-Port
expose:
  - "5432"

# ✅ Bind an spezifisches Interface
ports:
  - "192.168.1.100:5432:5432"
```

### 15.6 `EXPOSE` ist nur Metadata

Verbreiteter Irrtum:

```dockerfile
EXPOSE 8080
```

→ „Damit ist Port 8080 jetzt offen". **Falsch.** `EXPOSE` ist reine Dokumentation. Es öffnet keinen Port, mappt nichts, ändert nichts am Netzwerk.

Was Ports tatsächlich öffnet:
- `docker run -p 8080:8080` (CLI)
- `ports: - "8080:8080"` (Compose)

`EXPOSE` wirkt nur, wenn:
1. `docker run -P` (großes P) verwendet wird → Docker exposiert *alle* `EXPOSE`-Ports auf zufällige Host-Ports.
2. Tools wie Compose oder Kubernetes nutzen `EXPOSE` als Default für andere Konfiguration.

`EXPOSE` ist trotzdem sinnvoll: es dokumentiert, welche Ports der Container intern bedient — eine Form von API-Dokumentation auf Image-Ebene.

### 15.7 Firewall und Docker-Netzwerk beachten

Docker setzt eigene iptables-Regeln. Wer `ufw`, `firewalld` oder nftables nutzt, MUSS prüfen, ob Docker-exponierte Ports die erwarteten Firewall-Regeln umgehen. Standardmäßig **umgeht Docker** UFW-Regeln — was bedeutet: ein `ports: "5432:5432"` ist von außen erreichbar, auch wenn UFW Port 5432 blockiert.

**Lösung 1:** Bind an localhost (siehe 15.5).

**Lösung 2:** `iptables=false` in `/etc/docker/daemon.json` und manuelle Firewall-Regeln (kompliziert).

**Lösung 3:** Reverse Proxy (nginx, Traefik) als einziger Host-Port-Exposer; Docker-Ports nur intern.

### 15.8 Ressourcenlimits setzen

Für lokale Entwicklung sind harte Limits nicht immer notwendig. Für gemeinsam genutzte Systeme, CI-Runner und Testserver MÜSSEN CPU- und Speichergrenzen gesetzt werden.

```yaml
services:
  app:
    image: example-app:dev
    mem_limit: 1g
    cpus: 1.5
    pids_limit: 200            # Maximum Prozesse
    ulimits:
      nofile:
        soft: 1024
        hard: 4096
```

In SaaS-Kontexten ist Ressourcenbegrenzung wichtig, damit ein einzelner fehlerhafter Container, Testlauf oder Tenant-naher Prozess nicht das gesamte System destabilisiert. Ein OOM-Container darf nicht den Host mitziehen.

### 15.9 Cross-Reference zu QG-JAVA-006 (Tenant-Isolation)

Tenant-Isolation in SaaS-Systemen wird primär in der Service-Schicht implementiert (siehe QG-JAVA-006 v2 Sektion 14). Docker selbst trennt nicht zwischen Tenants — ein Container kennt den Begriff Tenant nicht. Aber:

- **Volume-Trennung pro Tenant** kann sinnvoll sein, wenn Tenant-Daten physisch getrennt liegen müssen.
- **Compose-Profile pro Tenant** für lokale Entwicklung mit unterschiedlichen Tenant-Datenbanken.
- **Network-Segmentierung** wenn Multi-Tenant-Daemon-Setup.

Diese Aspekte gehen über reine Docker-Konfiguration hinaus und sind in der jeweiligen Architektur-Doku zu behandeln.

---

## 16. Container Runtime Security

Build-Time-Security (Image-Scans, Nicht-Root, Secrets) ist nur die Hälfte der Sicherheitsstrategie. Runtime-Security beobachtet, was **im laufenden Container** passiert.

### 16.1 Falco — Runtime Threat Detection

[Falco](https://falco.org/) ist ein CNCF Graduated Project zur Runtime Threat Detection. Es erkennt verdächtiges Verhalten in laufenden Containern basierend auf Kernel-System-Calls.

**Beispiel-Detections:**

- Shell-Spawn in einem Container, der normalerweise keine Shell startet.
- Datei-Manipulation in `/etc/`, `/usr/bin/` zur Laufzeit.
- Ungewöhnliche Netzwerk-Verbindungen (Crypto-Mining-Pools, Reverse-Shells).
- Privilege-Eskalation-Versuche.

**Setup auf Debian:**

```bash
curl -fsSL https://falco.org/repo/falcosecurity-packages.asc | \
    sudo tee /etc/apt/keyrings/falco-archive-keyring.asc > /dev/null

echo "deb [signed-by=/etc/apt/keyrings/falco-archive-keyring.asc] \
    https://download.falco.org/packages/deb stable main" | \
    sudo tee /etc/apt/sources.list.d/falcosecurity.list

sudo apt update
sudo apt install -y falco
```

**Wann sinnvoll:** Server-Setups mit erhöhtem Sicherheitsbedarf, Multi-Tenant-Hosts, Compliance-relevante Workloads.

### 16.2 gVisor — Sandbox Runtime

[gVisor](https://gvisor.dev/) ist eine alternative Container-Runtime von Google. Statt direkt auf den Host-Kernel zuzugreifen, bietet gVisor einen **User-Space-Kernel** zwischen Container und Host. Das reduziert die Kernel-Surface drastisch.

**Trade-offs:**

- **Pro:** Massive Reduzierung der Kernel-CVE-Surface.
- **Pro:** Container-Escape wird viel schwerer.
- **Contra:** Performance-Overhead (5-15 % je nach Workload).
- **Contra:** Nicht alle System-Calls werden unterstützt — manche Apps brechen.

```bash
# Installation
curl -fsSL https://gvisor.dev/archive.key | \
    sudo tee /etc/apt/keyrings/gvisor.asc

echo "deb [signed-by=/etc/apt/keyrings/gvisor.asc] \
    https://storage.googleapis.com/gvisor/releases release main" | \
    sudo tee /etc/apt/sources.list.d/gvisor.list

sudo apt update
sudo apt install -y runsc

# Docker-Konfiguration
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "runtimes": {
    "runsc": {
      "path": "/usr/bin/runsc"
    }
  }
}
EOF

sudo systemctl restart docker

# Verwendung
docker run --runtime=runsc nginx
```

### 16.3 Kata Containers — VM-basierte Isolation

[Kata Containers](https://katacontainers.io/) gehen einen Schritt weiter: jeder Container läuft in einer **echten leichtgewichtigen VM**. Die Isolation ist auf Hypervisor-Ebene.

**Trade-offs:**

- **Pro:** Stärkste Isolation, vergleichbar mit echten VMs.
- **Pro:** Compatibility mit OCI-Standard-Images.
- **Contra:** Höherer Overhead (Memory, Boot-Time).
- **Contra:** Komplexerer Setup.

**Wann:** Multi-Tenant-Hosts mit untrusted Tenant-Code, Production-Workloads mit höchsten Isolation-Anforderungen.

### 16.4 Auswahl der richtigen Runtime-Security-Strategie

| Setup | Empfehlung |
|---|---|
| Lokaler Entwickler-Laptop | nichts erforderlich |
| Geteilter Build-Server | Falco erwägen |
| Production-Single-Tenant | Falco + Image-Scanning |
| Multi-Tenant Production | Falco + gVisor oder Kata + Image-Scanning |
| Compliance-Workloads | Falco + gVisor + AppArmor + SELinux |

---

## 17. Häufige Fehlmuster und korrekte Alternativen

### 17.1 Fehlmuster: Docker aus falscher Quelle installieren

**Schlecht:**

```bash
sudo apt install docker.io docker-compose
```

`docker.io` ist die Debian-Distribution-Variante, oft veraltet. `docker-compose` ist die Legacy-v1-Variante.

**Besser:**

```bash
# Aus dem offiziellen Docker-Repo (siehe Sektion 7)
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 17.2 Fehlmuster: `latest` verwenden

**Schlecht:**

```dockerfile
FROM eclipse-temurin:latest
```

`latest` ist nicht reproduzierbar. Heute Java 21, morgen Java 22.

**Besser:**

```dockerfile
FROM eclipse-temurin:21-jre-jammy
```

**Noch strenger** für produktionsnahe Systeme — Image-Digest:

```dockerfile
FROM eclipse-temurin:21-jre-jammy@sha256:abc123def456...
```

### 17.3 Fehlmuster: Root im Container

**Schlecht:**

```dockerfile
FROM eclipse-temurin:21-jre
COPY app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
# läuft als root
```

**Besser:**

```dockerfile
FROM eclipse-temurin:21-jre

RUN groupadd --system --gid 1001 app \
    && useradd --system --uid 1001 --gid app --no-create-home app

COPY --chown=app:app app.jar /app/app.jar

USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 17.4 Fehlmuster: Secrets in Images

**Schlecht:**

```dockerfile
COPY production.env /app/.env
```

oder:

```dockerfile
ENV DB_PASSWORD=actual-prod-secret
```

**Besser für Build-Time-Secrets:**

```dockerfile
RUN --mount=type=secret,id=mvn_credentials \
    ./mvnw -s /run/secrets/mvn_credentials package
```

**Besser für Runtime-Secrets:**

```bash
# Lokale Entwicklung
docker run --env-file .env.local example-app

# Produktion: Secret Store
# Kubernetes: Secrets, Vault, AWS Secrets Manager
```

### 17.5 Fehlmuster: Datenbankdaten ohne Volume

**Schlecht:**

```yaml
services:
  postgres:
    image: postgres:16.4
    # keine Volume-Definition
```

`docker compose down` löscht den Container, alle Daten sind weg.

**Besser:**

```yaml
services:
  postgres:
    image: postgres:16.4
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

### 17.6 Fehlmuster: Container als VM behandeln

**Schlecht:**

```bash
docker exec -it app bash
apt install vim
service cron start
# Container ist nun "speziell" — nicht reproduzierbar
```

**Besser:**

- Image neu bauen, wenn Tools nötig sind.
- Konfiguration versionieren.
- Container unveränderlich behandeln (Cattle, not Pets).
- Ein Prozess pro Container als Grundregel beachten.
- Debugging nicht mit manueller Produktionsmutation verwechseln.

### 17.7 Fehlmuster: `docker system prune -a --volumes` ohne Verständnis

Dieses Kommando kann Images, Container, Netzwerke, Build-Cache und **Volumes** entfernen. Volumes können Datenbanken enthalten.

**Besser:** gezielter cleanen:

```bash
docker system df                    # zeige Speicherplatznutzung
docker image prune                  # nur unbenutzte Images
docker container prune              # nur gestoppte Container
docker builder prune                # nur Build-Cache
docker volume prune                 # ⚠️ nur unbenutzte Volumes
```

Und `--volumes` nur bewusst einsetzen, wenn man sicher ist, dass keine wichtigen Daten verloren gehen.

### 17.8 Fehlmuster: Healthcheck für eine Webseite mit `wget` ohne Timeout

**Schlecht:**

```dockerfile
HEALTHCHECK CMD wget http://localhost:8080/health
```

`wget` ohne Timeout kann ewig hängen. Healthcheck-Status bleibt unbestimmt.

**Besser:**

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -fs --max-time 2 http://localhost:8080/actuator/health || exit 1
```

`--max-time 2` ist die zusätzliche Versicherung — auch wenn `--timeout 3s` greift, ist das individuelle curl auf 2 Sekunden begrenzt.

### 17.9 Fehlmuster: Kein `start_period` bei langsamen Java-Apps

**Schlecht:**

```dockerfile
HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
```

Spring Boot-Apps brauchen oft 30-60 Sekunden zum Start. Bei `--retries=3` und `--interval=10s` wäre die App nach 30 Sekunden als „unhealthy" markiert — obwohl sie gerade noch hochfährt.

**Besser:**

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health || exit 1
```

`--start-period=60s` gibt der App Zeit zu starten, bevor Retries gezählt werden.

---

## 18. Betrieb und Diagnose

### 18.1 Grundkommandos

```bash
docker ps                          # laufende Container
docker ps -a                       # alle Container
docker images                      # Images auf dem Host
docker volume ls                   # Volumes
docker network ls                  # Netzwerke
docker logs <container>            # Logs
docker logs -f --tail 100 <container>  # live, letzte 100
docker inspect <container>         # vollständige Konfiguration
docker stats                       # live CPU/Memory pro Container
docker system df                   # Speicherplatznutzung
```

### 18.2 Compose-Diagnose

```bash
docker compose ps                  # Status aller Services
docker compose logs -f             # Live-Logs aller Services
docker compose logs -f app         # Live-Logs nur app
docker compose config              # finale aufgelöste Konfiguration
docker compose down                # stoppen, Volumes bleiben
docker compose down -v             # ⚠️ stoppen + Volumes löschen
docker compose down --remove-orphans  # auch verwaiste Services entfernen
```

`docker compose config` ist besonders nützlich, weil es zeigt, wie Compose die Datei tatsächlich interpretiert hat — inklusive Variablen-Auflösung, Profile-Merge, Override-Dateien.

### 18.3 In Container hineinschauen

```bash
docker exec -it <container> bash      # in laufenden Container (wenn bash existiert)
docker exec -it <container> sh        # Alpine-/Distroless-Fallback
docker run --rm -it <image> sh        # Image direkt starten

# Bei Distroless-Images ohne Shell — Workaround mit ephemeral debug container
docker run -it --rm --pid=container:<container> --net=container:<container> \
    nicolaka/netshoot
```

### 18.4 Docker-Dienst prüfen

```bash
systemctl status docker
journalctl -u docker --no-pager -n 200
journalctl -u docker -f                  # Live
```

### 18.5 Speicherplatz prüfen

Docker kann viel Speicher belegen:

```bash
docker system df                          # Übersicht
docker system df -v                       # detailliert
du -sh /var/lib/docker
```

Auf Servern SOLLTE `/var/lib/docker` bewusst auf einer geeigneten Partition liegen oder zumindest überwacht werden:

```json
// /etc/docker/daemon.json
{
  "data-root": "/data/docker"
}
```

```bash
sudo systemctl restart docker
```

### 18.6 Container-Resource-Limits prüfen

```bash
# Für laufenden Container
docker inspect <container> --format \
    'CPU: {{.HostConfig.NanoCpus}} | Memory: {{.HostConfig.Memory}}'

# Live-Stats
docker stats --no-stream
```

---

## 19. Updates und Wartung

Docker und der Linux-Kernel MÜSSEN regelmäßig aktualisiert werden. Container teilen sich den Host-Kernel; ein verwundbarer Host-Kernel kann Container-Isolation schwächen.

### 19.1 Update-Workflow

```bash
sudo apt update
sudo apt upgrade
```

Docker-spezifische Versionen prüfen:

```bash
apt list --installed 2>/dev/null | grep -E 'docker|containerd'
docker version
docker compose version
docker buildx version
```

### 19.2 Major-Upgrade-Strategie

Für produktionsnahe Systeme SOLLTE nicht blind jedes Major-Upgrade installiert werden. Besser ist:

1. Versionen auf Testsystem aktualisieren.
2. Zentrale Container-Workloads prüfen (laufen sie noch?).
3. Compose-Setups starten und prüfen.
4. Logs und Netzwerkverhalten beobachten.
5. Update auf Zielsystemen ausrollen.

**Besonders kritische Stellen bei Major-Upgrades:**

- Compose v1 → v2 — wurde 2023 vollzogen, nicht erneut umkehren.
- BuildKit als Default — kann Build-Pipelines beeinflussen.
- Cgroup v1 → v2 — kann Resource-Limits beeinflussen.
- iptables → nftables — Firewall-Interaktion ändert sich.

### 19.3 Image-Update-Disziplin

Base-Images in Dockerfiles werden nicht automatisch aktualisiert:

```bash
# Alte Image-Version ist immer noch da
docker images eclipse-temurin

# Neueste Tag-Version ziehen
docker pull eclipse-temurin:21-jre-jammy

# Image neu bauen
docker build -t example-app:1.0 .
```

Für regelmäßige Image-Refreshes in CI:

```yaml
# .github/workflows/rebuild-images.yml
name: Weekly Image Rebuild
on:
  schedule:
    - cron: '0 4 * * 1'        # Montags 4 Uhr UTC
jobs:
  rebuild:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: docker build --pull --no-cache -t example-app:weekly .
      - run: docker push registry.example.com/example-app:weekly
```

`--pull` zwingt das Pulling der neuesten Base-Image-Version, `--no-cache` verhindert alte Layer-Caches.

---

## 20. Deinstallation und sauberes Entfernen

Docker-Pakete entfernen:

```bash
sudo apt purge \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin \
    docker-ce-rootless-extras
```

Achtung: Images, Container, Volumes und Konfigurationen werden **nicht automatisch vollständig entfernt**.

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

User aus `docker`-Gruppe entfernen:

```bash
sudo gpasswd -d "$USER" docker
sudo groupdel docker         # nur wenn keine anderen User mehr in der Gruppe
```

> **WARNUNG:** Diese Schritte DÜRFEN NICHT unbedacht auf Systemen ausgeführt werden, auf denen Volumes produktive oder relevante Testdaten enthalten. Vor `rm -rf /var/lib/docker` immer prüfen, ob noch wichtige Daten vorliegen.

---

## 21. SBOM und EU Cyber Resilience Act

### 21.1 Was ist ein SBOM?

Ein **SBOM (Software Bill of Materials)** ist eine maschinenlesbare Liste aller Komponenten in einem Software-Artefakt — Bibliotheken, Versionen, Lizenzen, Hashes. Bei Container-Images: alle Pakete, JARs, OS-Komponenten.

### 21.2 Warum SBOM jetzt relevant ist

Die **EU Cyber Resilience Act (CRA)** — Verordnung 2024/2847 — tritt **2027 in Kraft**. Sie verpflichtet Hersteller von Software-Produkten in der EU zu:

- SBOM-Bereitstellung pro Produkt-Version.
- Sicherheits-Updates über die Produkt-Lebenszeit.
- Schwachstellen-Berichtspflicht.
- Sicherer Standardkonfiguration.

In den USA ist **Executive Order 14028** seit 2021 in ähnlicher Richtung wirksam — Bundesbehörden müssen SBOM von Software-Lieferanten verlangen.

**Konsequenz:** Software, die in 2026 entwickelt wird, sollte SBOM-fähig sein. Container-Images sind besonders relevant, weil sie Dutzende bis Hunderte transitive Abhängigkeiten enthalten.

### 21.3 SBOM-Formate

| Format | Standard | Verwendung |
|---|---|---|
| **SPDX** | Linux Foundation, ISO/IEC 5962 | NIST-Standard, US-Behörden |
| **CycloneDX** | OWASP-Projekt | weit verbreitet, gut für Vulnerability-Management |

Beide Formate sind 2026 etabliert. Welches verwendet wird, hängt vom Toolchain-Standard der Organisation ab.

### 21.4 SBOM-Generierung mit konkreten Tools

**Docker Scout (Docker Inc., empfohlen für Docker-User):**

```bash
docker scout sbom --format spdx example-app:latest > sbom.spdx.json
docker scout sbom --format cyclonedx example-app:latest > sbom.cyclonedx.json
```

**Syft (Anchore, Tool-agnostisch):**

```bash
# Installation
curl -sSfL https://raw.githubusercontent.com/anchore/syft/main/install.sh | sh

# SBOM erstellen
syft example-app:latest -o spdx-json > sbom.spdx.json
syft example-app:latest -o cyclonedx-json > sbom.cyclonedx.json
```

**Trivy (Aqua Security, oft schon in CI):**

```bash
# CycloneDX
trivy image --format cyclonedx --output sbom.cyclonedx.json example-app:latest

# SPDX
trivy image --format spdx-json --output sbom.spdx.json example-app:latest
```

### 21.5 SBOM in CI integrieren

```yaml
# .github/workflows/build.yml
name: Build and SBOM
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build image
        run: docker build -t example-app:${{ github.sha }} .

      - name: Generate SBOM (CycloneDX)
        uses: anchore/sbom-action@v0
        with:
          image: example-app:${{ github.sha }}
          format: cyclonedx-json
          output-file: sbom.cyclonedx.json

      - name: Upload SBOM as artifact
        uses: actions/upload-artifact@v4
        with:
          name: sbom-${{ github.sha }}
          path: sbom.cyclonedx.json

      - name: Vulnerability scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: example-app:${{ github.sha }}
          format: sarif
          output: trivy-results.sarif
```

### 21.6 SBOM und Cloud Native Buildpacks

Buildpacks **erzeugen SBOMs automatisch**:

```bash
./mvnw spring-boot:build-image
docker scout sbom <image-name>
```

Das ist einer der CNB-Vorteile gegenüber manuell gepflegten Dockerfiles.

### 21.7 SBOM-Wartung als Prozess

SBOM-Generierung allein reicht nicht. Was braucht es zusätzlich:

- **Versionierung der SBOMs** — pro Release ein archiviertes SBOM.
- **Vulnerability-Continuous-Monitoring** — neue CVEs gegen alte SBOMs prüfen.
- **Disclosure-Pflicht** — bei kritischen Findings nach EU-CRA Meldepflicht.
- **Update-Strategie** — Sicherheits-Patches innerhalb definierter Zeitfenster.

Diese Pflichten gehen über Docker hinaus und sind Teil eines umfassenden Software-Supply-Chain-Security-Programms (SSCS).

---

## 22. Review-Checkliste

Vor Aufnahme eines Docker-Setups in ein Projekt müssen folgende Fragen beantwortet werden. Jede Zeile verweist auf die Detail-Sektion mit der Begründung.

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| Installation | Wurde Docker über das offizielle Repository installiert? | §7.5 |
| GPG | Wurde der GPG-Fingerprint verifiziert? | §7.4, §3.1.2 |
| Compose | Wird `docker compose` (v2) statt `docker-compose` (v1) verwendet? | §3.1.3 |
| Benutzerrechte | Ist klar, welche Benutzer Zugriff auf Docker haben? | §8 |
| `docker`-Gruppe | Ist Mitgliedschaft als root-äquivalent dokumentiert? | §15.1 |
| Rootless / userns-remap | Wurde bei erhöhtem Sicherheitsbedarf geprüft? | §8.3, §8.4 |
| Multi-Stage | Trennt das Dockerfile Build- und Runtime-Stage? | §12.2 |
| Image-Tag | Sind Images versioniert, wird `latest` vermieden? | §12.2, §17.2 |
| Nicht-Root | Läuft die Anwendung als Nicht-Root-Benutzer? | §12.2, §3.1.7 |
| `--chown` | Wird `--chown=app:app` bei `COPY` verwendet? | §12.2, §3.1.8 |
| `.dockerignore` | Vorhanden und vollständig? | §12.3, §3.1.11 |
| HEALTHCHECK | Im Dockerfile definiert, mit `start_period`? | §12.2, §17.9 |
| JVM-Container-Optionen | `MaxRAMPercentage`, `+ExitOnOutOfMemoryError` gesetzt? | §12.2, §3.1.10 |
| Cache Mounts | BuildKit Cache Mounts für Maven/Gradle? | §14.2 |
| Layered JAR | Spring Boot Layered JAR Pattern verwendet? | §12.2 |
| Buildpacks | Cloud Native Buildpacks als Alternative geprüft? | §13 |
| Distroless | Distroless als Runtime-Option für Production geprüft? | §12.4 |
| Build-Secrets | Werden BuildKit Secrets statt ENV verwendet? | §14.3 |
| Multi-Arch | linux/amd64 und linux/arm64 unterstützt? | §14.4 |
| Compose Healthchecks | `depends_on: condition: service_healthy` gesetzt? | §11.1 |
| Compose Profiles | Optionale Services in Profiles? | §11.2 |
| Docker-Socket | `/var/run/docker.sock` nicht ohne Begründung gemountet? | §15.2 |
| Privileged | Kein `--privileged` als Default? | §15.3 |
| Secrets | Keine produktiven Secrets in Image, Compose, ENV? | §15.4, §3.1.13 |
| Port-Exposition | Bewusst gesteuert (`127.0.0.1:port:port`)? | §15.5 |
| Firewall | Docker-Port-Exposition + UFW-Verhalten geprüft? | §15.7 |
| Ressourcenlimits | `mem_limit`, `cpus` für gemeinsam genutzte Systeme? | §15.8 |
| Runtime-Security | Falco / gVisor / Kata für Server geprüft? | §16 |
| SBOM | SBOM-Generierung in CI integriert? | §21.4, §21.5 |
| Image-Scan | Vulnerability-Scan in CI? | §3.1.18 |
| Update-Strategie | Image-Refresh-Plan vorhanden? | §19.3 |

---

## 23. Automatisierbare Prüfungen

Folgende Prüfungen MÜSSEN/SOLLEN automatisiert werden:

| Prüfung | Werkzeug | Ziel |
| --- | --- | --- |
| Dockerfile-Linting | Hadolint, dockerfilelint | Anti-Patterns erkennen |
| Image-Scan auf CVEs | Docker Scout, Trivy, Grype, Snyk | bekannte Schwachstellen erkennen |
| SBOM-Erzeugung | Docker Scout, Syft, Trivy, Buildpacks | Komponentenbestand sichtbar |
| Secret-Scanning | Gitleaks, TruffleHog, GitHub Secret Scanning | Secrets im Repository verhindern |
| Compose-Validierung | `docker compose config` | Konfiguration prüfen |
| Build-Reproduzierbarkeit | CI-Pipeline mit `docker build` | reproduzierbarer Build |
| Runtime-Smoke-Test | `docker run` oder `compose up` im CI | Startfähigkeit |
| Nicht-Root-Prüfung | Custom Script oder Hadolint | Root-Laufzeit verhindern |
| ArchUnit für Docker-Patterns (Java-Code) | ArchUnit | unsachgemäße Container-Annahmen |
| Multi-Arch-Build | `docker buildx` in CI | Plattform-Kompatibilität |

### 23.1 Hadolint-Beispiel

```yaml
# .github/workflows/dockerfile-lint.yml
- name: Lint Dockerfile
  uses: hadolint/hadolint-action@v3.1.0
  with:
    dockerfile: Dockerfile
    failure-threshold: warning
```

Hadolint erkennt:
- Verwendung von `latest`-Tags.
- Fehlende `USER`-Direktive.
- `apt-get` ohne `--no-install-recommends`.
- Fehlende `apt-get clean`.
- `ADD` statt `COPY`.

### 23.2 Trivy in CI

```yaml
- name: Trivy vulnerability scan
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: example-app:${{ github.sha }}
    format: sarif
    output: trivy-results.sarif
    severity: CRITICAL,HIGH
    exit-code: 1                          # Build failt bei Findings
    ignore-unfixed: true                  # nur fixbare Schwachstellen
```

### 23.3 Beispiel CI-Workflow (komplett)

```yaml
name: Docker Build & Security
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Lint Dockerfile
        uses: hadolint/hadolint-action@v3.1.0
        with:
          dockerfile: Dockerfile

      - name: Validate Compose
        run: docker compose config

      - name: Build image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: false
          tags: example-app:${{ github.sha }}
          load: true
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Smoke test
        run: |
          docker run --rm example-app:${{ github.sha }} java -version

      - name: Vulnerability scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: example-app:${{ github.sha }}
          format: sarif
          output: trivy-results.sarif
          severity: CRITICAL,HIGH
          exit-code: 1
          ignore-unfixed: true

      - name: Generate SBOM
        uses: anchore/sbom-action@v0
        with:
          image: example-app:${{ github.sha }}
          format: cyclonedx-json
          output-file: sbom.cyclonedx.json

      - name: Upload SBOM
        uses: actions/upload-artifact@v4
        with:
          name: sbom
          path: sbom.cyclonedx.json
```

---

## 24. Migration bestehender Setups

Bestehende Docker-Setups sollen schrittweise verbessert werden:

1. **Aktuelle Installation prüfen:**

   ```bash
   docker version
   docker compose version
   apt list --installed | grep -E 'docker|containerd'
   ```

2. **Prüfen, ob `docker.io` oder altes `docker-compose` verwendet wird** — wenn ja: Migration auf offizielles Repo (Sektion 7).
3. **Projekt auf `docker compose` (v2) umstellen** — `docker-compose.yml` nach `compose.yaml` umbenennen, Befehle anpassen.
4. **Dockerfile auf Multi-Stage umstellen** (Sektion 12.2).
5. **`.dockerignore` ergänzen** (Sektion 12.3).
6. **`latest`-Tags ersetzen** durch versionierte Tags oder Digests.
7. **Nicht-Root-Benutzer im Image einführen** mit `--chown=app:app` bei `COPY`.
8. **HEALTHCHECK ergänzen** mit angemessenem `start_period`.
9. **JVM-Container-Optionen ergänzen** (`MaxRAMPercentage`, `+ExitOnOutOfMemoryError`).
10. **BuildKit Cache Mounts einführen** für Build-Beschleunigung.
11. **Spring Boot Layered JARs einführen** für effizientere Layer.
12. **Secrets aus Images und Compose-Dateien entfernen** — auf Secret-Store umstellen.
13. **Ports, Volumes und Netzwerke dokumentieren.**
14. **Image-Scan in CI ergänzen** (Trivy, Docker Scout).
15. **SBOM-Generierung in CI ergänzen** (Sektion 21).
16. **Entwicklerdokumentation aktualisieren** (Sektion 22-Checkliste).
17. **Multi-Arch-Build prüfen** wenn Apple-Silicon-Entwickler im Team sind.

Migration darf nicht als reine Technikaufgabe verstanden werden. Sie verändert, wie Entwickler lokal arbeiten, wie Tests laufen und wie Fehler diagnostiziert werden. Deshalb muss sie mit klaren Kommandos, Troubleshooting und Review-Regeln begleitet werden.

---

## 25. Ausnahmen

Abweichungen von dieser Richtlinie sind erlaubt, wenn ein nachvollziehbarer Grund vorliegt.

Beispiele:

* Ein Unternehmen gibt eine interne Docker-/Podman-Plattform verbindlich vor.
* CI-Runner verwenden einen kontrollierten Docker-Socket-Mount mit Socket-Proxy.
* Ein Container muss für spezifische Systemtests kurzzeitig privilegiert laufen.
* Ein Legacy-Projekt benötigt vorübergehend `docker-compose` v1.
* Rootless Mode ist wegen konkreter technischer Einschränkungen nicht geeignet.
* Ein Image verwendet temporär Root, weil eine Legacy-Komponente nicht anders startet.
* Distroless ist nicht möglich, weil native Libraries OS-Pakete benötigen.

Jede Ausnahme MUSS dokumentiert werden:

* **Was weicht ab?**
* **Warum ist die Abweichung notwendig?**
* **Welche Risiken entstehen?**
* **Wie wird das Risiko begrenzt?** (Compensating Controls)
* **Wann wird die Ausnahme erneut geprüft?** (Review-Termin)
* **Wer hat die Ausnahme freigegeben?** (Security/Platform-Approval)

---

## 26. Definition of Done

Ein Docker-Setup erfüllt diese Richtlinie, wenn alle folgenden Punkte erfüllt sind:

1. Docker Engine ist über das offizielle Docker-APT-Repository installiert.
2. GPG-Fingerprint wurde verifiziert.
3. Docker Compose v2 (`docker compose`) ist installiert und in Projekten verwendet.
4. `docker version`, `docker compose version`, `docker buildx version` und `docker run --rm hello-world` funktionieren.
5. Benutzerrechte sind bewusst entschieden und dokumentiert (Option A/B/C/D in Sektion 8).
6. Die `docker`-Gruppe wird als sicherheitskritisch behandelt.
7. Rootless Mode oder `userns-remap` wurde bei erhöhtem Sicherheitsbedarf geprüft.
8. Projekt-Compose-Dateien sind versioniert und verständlich.
9. Compose verwendet `compose.yaml` (nicht `docker-compose.yml`).
10. `depends_on: condition: service_healthy` ist gesetzt, wo Reihenfolge wichtig ist.
11. Dockerfile baut in Multi-Stage.
12. Dockerfile verwendet `# syntax=docker/dockerfile:1.7` für BuildKit-Features.
13. BuildKit Cache Mounts werden für Maven/Gradle genutzt.
14. Spring Boot Layered JARs werden verwendet.
15. Build- und Runtime-Image sind sauber getrennt.
16. `--chown=app:app` wird bei `COPY` verwendet.
17. Anwendung läuft als Nicht-Root-Benutzer (`USER app`).
18. Image-Tags sind versioniert; `latest` wird nicht für produktionsnahe Images verwendet.
19. `.dockerignore` ist vorhanden und vollständig.
20. `HEALTHCHECK` ist mit angemessenem `start_period` definiert.
21. JVM-Container-Optionen (`MaxRAMPercentage`, `+ExitOnOutOfMemoryError`) sind gesetzt.
22. Keine produktiven Secrets liegen in Image, Compose-Datei oder ENV.
23. Build-Time-Secrets werden über BuildKit `--mount=type=secret` übergeben.
24. Cloud Native Buildpacks oder Distroless wurden als Alternative geprüft.
25. Multi-Architektur-Build (linux/amd64 + linux/arm64) ist eingerichtet, wenn nötig.
26. Ports, Volumes und Netzwerke sind bewusst konfiguriert.
27. Gefährliche Optionen wie `--privileged`, Host-Root-Mounts und Docker-Socket-Mounts sind verboten oder explizit freigegeben.
28. Ressourcenlimits sind auf gemeinsam genutzten Systemen gesetzt.
29. Runtime-Security (Falco / gVisor / Kata) wurde bei Server-Setups geprüft.
30. Vulnerability-/SBOM-Scanning ist in CI integriert.
31. SBOM wird mit jedem Release im SPDX- oder CycloneDX-Format archiviert.
32. EU-CRA-Compliance wurde bei produktrelevanten Software-Artefakten geprüft.
33. Update- und Patch-Konzept ist vorhanden.
34. Entwickler können das Setup anhand der Dokumentation reproduzierbar ausführen.
35. Die wichtigsten Docker-Kommandos für Start, Stop, Logs, Reset und Diagnose sind dokumentiert.
36. Pull-Request-Review berücksichtigt die Checkliste aus Sektion 22.

---

## 27. Quellen und weiterführende Literatur

### Docker offiziell

* Docker Docs — Install Docker Engine on Debian: <https://docs.docker.com/engine/install/debian/>
* Docker Docs — Linux post-installation steps for Docker Engine: <https://docs.docker.com/engine/install/linux-postinstall/>
* Docker Docs — Rootless mode: <https://docs.docker.com/engine/security/rootless/>
* Docker Docs — Docker Engine security: <https://docs.docker.com/engine/security/>
* Docker Docs — Use namespaces (`userns-remap`): <https://docs.docker.com/engine/security/userns-remap/>
* Docker Docs — Install the Docker Compose plugin: <https://docs.docker.com/compose/install/linux/>
* Docker Docs — Compose Profiles: <https://docs.docker.com/compose/how-tos/profiles/>
* Docker Docs — Dockerfile reference: <https://docs.docker.com/reference/dockerfile/>
* Docker Docs — Best practices for writing Dockerfiles: <https://docs.docker.com/build/building/best-practices/>
* Docker Docs — BuildKit: <https://docs.docker.com/build/buildkit/>
* Docker Docs — BuildKit Cache Mounts: <https://docs.docker.com/build/cache/optimize/#use-cache-mounts>
* Docker Docs — Build secrets: <https://docs.docker.com/build/building/secrets/>
* Docker Docs — Multi-platform builds: <https://docs.docker.com/build/building/multi-platform/>
* Docker Docs — Buildx Bake: <https://docs.docker.com/build/bake/>
* Docker Docs — Docker Scout: <https://docs.docker.com/scout/>

### Spring Boot

* Spring Boot Reference — Container Images: <https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html>
* Spring Boot Reference — Layered JARs: <https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html#container-images.efficient-images.layering>
* Spring Boot Reference — Cloud Native Buildpacks: <https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html#container-images.buildpacks>
* Paketo Buildpacks: <https://paketo.io/>

### Java in Containern

* Eclipse Temurin: <https://adoptium.net/temurin/>
* OpenJDK Container Awareness: <https://openjdk.org/jeps/192>
* Java 21 Container Improvements: <https://openjdk.org/jeps/425>

### Distroless und Alpine

* Google Distroless: <https://github.com/GoogleContainerTools/distroless>
* Alpine Linux: <https://alpinelinux.org/>

### Sicherheit

* OWASP Cheat Sheet — Docker Security: <https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html>
* CIS Docker Benchmark: <https://www.cisecurity.org/benchmark/docker>
* Falco Project: <https://falco.org/>
* gVisor: <https://gvisor.dev/>
* Kata Containers: <https://katacontainers.io/>

### SBOM und Compliance

* SPDX Specification: <https://spdx.dev/specifications/>
* CycloneDX Specification: <https://cyclonedx.org/>
* Anchore Syft: <https://github.com/anchore/syft>
* Aqua Trivy: <https://trivy.dev/>
* EU Cyber Resilience Act (Regulation 2024/2847): <https://digital-strategy.ec.europa.eu/en/policies/cyber-resilience-act>
* US Executive Order 14028: <https://www.whitehouse.gov/briefing-room/presidential-actions/2021/05/12/executive-order-on-improving-the-nations-cybersecurity/>
* NIST SBOM Resources: <https://www.cisa.gov/sbom>

---

*Diese Richtlinie ersetzt die Erstfassung vom 2026-05-02 (v1.0). Für inhaltliche Querverweise auf Service-Layer-Themen siehe QG-JAVA-006 v2 (Spring-Boot-Serviceschicht), insbesondere Sektion 14 (Security- und SaaS-Aspekte) und Sektion 14.6 (Tenant-Context-Pattern). Für Contract-Testing in containerisierten Service-Setups siehe QG-JAVA-019 v2 (Contract Testing mit Pact und Spring Cloud Contract).*