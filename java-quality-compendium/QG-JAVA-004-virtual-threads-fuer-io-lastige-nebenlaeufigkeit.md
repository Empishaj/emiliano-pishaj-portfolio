# QG-JAVA-004 — Virtual Threads für I/O-lastige Nebenläufigkeit

## Dokumentenstatus

| Feld | Wert |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-004 |
| Titel | Virtual Threads für I/O-lastige Nebenläufigkeit |
| Status | Akzeptiert / verbindlicher Standard für neue Java-21+-Services mit hohem Anteil blockierender I/O |
| Sprache | Deutsch |
| Java-Baseline | Java 21+ |
| Zentrale Java-Features | Virtual Threads, `Executors.newVirtualThreadPerTaskExecutor()`, `Thread.ofVirtual()` |
| Relevante JEPs | JEP 444 — Virtual Threads; JEP 453 — Structured Concurrency als Preview in Java 21 |
| Kategorie | Nebenläufigkeit, Performance, Betriebsqualität, SaaS-Skalierung |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, Plattformteams, SRE/Operations, Security, QA |
| Geltung | Neue Java-21+-Services und Modernisierung bestehender blockierender I/O-Flows |
| Nicht-Ziel | Kein Ersatz für Rate Limiting, Backpressure, Timeouts, Kapazitätsplanung, Lasttests oder saubere Ressourcenbegrenzung |
| Verbindlichkeit | Virtual Threads SOLLEN für I/O-lastige, synchron modellierte Workloads geprüft und bevorzugt werden. Abweichungen sind zulässig, wenn ein technischer Grund vorliegt, zum Beispiel echter Streaming-/Backpressure-Bedarf, CPU-intensive Verarbeitung oder vorhandener stabiler reaktiver Stack. |
| Prüfstatus | Reine Java-Beispiele wurden syntaktisch gegen Java 21 validiert. Spring-Beispiele sind konzeptionelle Integrationsbeispiele und setzen Spring Boot 3.2+ voraus. |
| Letzte fachliche Validierung | 2026-05-02 |
| Quellenbasis | OpenJDK JEP 444, Oracle Java 21 Virtual Threads Documentation, Spring Boot 3.2 Reference Documentation, OpenJDK JEP 453, OWASP API Security und OWASP Logging Guidance |

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wann und wie Virtual Threads in Java-21+-Anwendungen eingesetzt werden sollen. Ziel ist es, I/O-lastige Java-Services einfacher, besser lesbar, besser testbar und betrieblich skalierbarer zu machen, ohne in unnötig komplexe Callback-, Reactive- oder Thread-Pool-Strukturen auszuweichen.

Virtual Threads sind besonders relevant für SaaS-Plattformen, die viele gleichzeitige Requests verarbeiten und dabei regelmäßig auf Datenbanken, HTTP-Schnittstellen, Dateisysteme, Message Broker oder andere externe Systeme warten. In solchen Fällen ist der limitierende Faktor häufig nicht CPU-Zeit, sondern blockierende Wartezeit. Virtual Threads erlauben, den einfachen synchronen Programmierstil beizubehalten und trotzdem deutlich mehr gleichzeitige blockierende Tasks zu verwalten.

Diese Richtlinie ist bewusst keine pauschale Anti-Reactive-Regel. Reaktive Stacks bleiben sinnvoll, wenn echtes Streaming, non-blocking Backpressure, eventgetriebene Verarbeitung oder bestehende reaktive Plattformarchitektur fachlich oder technisch erforderlich sind. Für klassische requestorientierte CRUD-, Integrations- und Orchestrierungsservices ist synchroner Code mit Virtual Threads jedoch häufig die einfachere und wartbarere Standardlösung.

---

## 2. Kurzregel für Entwickler

Verwende Virtual Threads für viele gleichzeitige, überwiegend blockierende I/O-Tasks, wenn der Code als einfacher synchroner Ablauf verständlicher, testbarer und wartbarer bleibt.

Verwende Virtual Threads nicht als allgemeinen Performance-Turbo, nicht für CPU-intensive Berechnungen und nicht als Ersatz für Ressourcenbegrenzung. Virtual Threads erhöhen die mögliche Nebenläufigkeit. Sie machen Datenbanken, Partner-APIs, Dateisysteme, Locks, Transaktionen und externe Services nicht automatisch schneller.

**Kurzform:**

> Virtual Threads sind für Skalierung bei vielen wartenden Tasks geeignet, nicht für schnellere CPU-Ausführung. Begrenze weiterhin Datenbankverbindungen, externe Aufrufe, Timeouts, Tenant-Quoten und Request-Raten.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für:

- Java-21+-Services mit klassischem Thread-per-Request-Modell.
- Spring-Boot-3.2+-Anwendungen, die blockierende APIs verwenden.
- REST-Endpunkte mit blockierenden Datenbank-, HTTP- oder Dateizugriffen.
- interne Service-Orchestrierung mit mehreren unabhängigen I/O-Aufrufen.
- Batch- oder Worker-Prozesse mit vielen unabhängigen I/O-Tasks.
- SaaS-Plattformen mit vielen gleichzeitigen Requests und klaren Ressourcenlimits.
- Migration von überdimensionierten Platform-Thread-Pools, wenn diese wegen blockierender I/O zum Engpass werden.

Diese Richtlinie gilt nicht oder nur eingeschränkt für:

- CPU-intensive Berechnungen, Bildverarbeitung, Kryptographie, große In-Memory-Transformationen oder Data-Parallelism.
- echte Reactive-Streaming-Anforderungen mit Backpressure über die gesamte Verarbeitungskette.
- Event-Loop-Code, der bewusst nicht blockieren darf.
- parallele Zugriffe auf nicht thread-sichere Objekte wie eine gemeinsam genutzte JPA-`EntityManager`-Instanz.
- Systeme ohne relevante Nebenläufigkeits- oder Blockierungsprobleme.
- sicherheitskritische Endpunkte, bei denen hohe Nebenläufigkeit ohne Rate Limiting zu Ressourcenerschöpfung führen kann.

---

## 4. Technischer Hintergrund

### 4.1 Platform Threads

Ein Platform Thread ist in Java ein `java.lang.Thread`, der eng an einen Betriebssystem-Thread gekoppelt ist. Platform Threads sind leistungsfähig und universell einsetzbar, aber sie sind vergleichsweise teuer. Sie werden vom Betriebssystem geplant, besitzen einen nativen Stack und sind als Ressource begrenzt.

In klassischen Servlet-Anwendungen führt das häufig zu einem Thread-per-Request-Modell: Jeder eingehende Request belegt während seiner Bearbeitung einen Thread. Wenn dieser Thread auf eine Datenbank, ein Dateisystem oder eine HTTP-Antwort wartet, ist der zugrundeliegende Betriebssystem-Thread während dieser Wartezeit gebunden.

### 4.2 Virtual Threads

Ein Virtual Thread ist ebenfalls ein `java.lang.Thread`, wird aber von der JVM verwaltet und ist nicht dauerhaft an einen bestimmten Betriebssystem-Thread gebunden. Wenn ein Virtual Thread bei einer unterstützten blockierenden Operation wartet, kann die JVM ihn parken und den darunterliegenden Carrier Thread für andere Arbeit freigeben.

Dadurch können sehr viele gleichzeitige Tasks im einfachen synchronen Stil modelliert werden. Der Code bleibt lesbar, debuggbar und testbar, während die JVM das Scheduling übernimmt.

Wichtig: Virtual Threads sind keine „schnelleren Threads“. Sie führen CPU-Code nicht schneller aus als Platform Threads. Ihr Nutzen entsteht vor allem dadurch, dass blockierende Wartezeit nicht dauerhaft wertvolle Betriebssystem-Threads bindet.

### 4.3 Mounting, Unmounting und Carrier Threads

Ein Virtual Thread wird zur Ausführung auf einen Platform Thread montiert. Dieser Platform Thread wird Carrier genannt. Wenn der Virtual Thread blockierend wartet und entkoppelt werden kann, wird er vom Carrier gelöst. Der Carrier kann dann einen anderen Virtual Thread ausführen.

Diese Entkopplung ist der zentrale Mechanismus für höhere Skalierbarkeit bei I/O-lastiger Nebenläufigkeit.

### 4.4 Pinning

Pinning bedeutet, dass ein Virtual Thread während einer blockierenden Operation nicht vom Carrier Thread gelöst werden kann. Dann bleibt auch der zugrundeliegende Platform Thread blockiert. Das macht das Programm nicht automatisch falsch, kann aber die Skalierbarkeit deutlich reduzieren.

Typische Pinning-Situationen in Java 21:

- ein Virtual Thread blockiert innerhalb eines `synchronized`-Blocks oder einer `synchronized`-Methode.
- ein Virtual Thread blockiert innerhalb nativer Methoden oder Foreign-Function-Aufrufe.

Daraus folgt keine pauschale Regel „nie `synchronized`“. Kurze, rein speicherinterne und selten ausgeführte Synchronisation ist meist unproblematisch. Problematisch sind häufige oder lang laufende blockierende I/O-Aufrufe innerhalb von `synchronized`.

---

## 5. Verbindlicher Standard

### 5.1 Grundstandard

Neue Java-21+-Services SOLLEN für klassische I/O-lastige Request-Verarbeitung Virtual Threads prüfen und bevorzugen, wenn dadurch synchroner Code möglich bleibt und keine echte Reactive-Anforderung besteht.

Für neue Spring-Boot-3.2+-Services mit blockierenden APIs SOLL die Aktivierung von Virtual Threads in der Zielarchitektur geprüft werden:

```properties
spring.threads.virtual.enabled=true
```

Wenn die Anwendung ausschließlich oder wesentlich über virtual-thread-basierte Scheduler am Leben gehalten wird, MUSS geprüft werden, ob zusätzlich folgende Einstellung erforderlich ist:

```properties
spring.main.keep-alive=true
```

### 5.2 Messpflicht

Virtual Threads DÜRFEN NICHT rein aus Modegründen aktiviert werden. Vor produktiver Nutzung MUSS mindestens geprüft werden:

- Gibt es einen I/O-lastigen Workload?
- Ist Thread-Starvation oder Thread-Pool-Warteschlange tatsächlich ein Problem?
- Sind Datenbank-, HTTP-Client- und externe Ressourcen limitiert?
- Gibt es Timeouts und Abbruchlogik?
- Treten Pinning-Ereignisse auf?
- Verändert sich Durchsatz, Latenz, Fehlerrate oder Ressourcenverbrauch?
- Ist Observability für virtuelle Threads ausreichend eingerichtet?

### 5.3 Ressourcenbegrenzung bleibt Pflicht

Virtual Threads ersetzen keine Kapazitätskontrolle. Auch mit Virtual Threads MÜSSEN begrenzt werden:

- Datenbankverbindungen.
- gleichzeitige Partner-API-Aufrufe.
- gleichzeitige Aufrufe pro Tenant.
- gleichzeitige teure Operationen pro User.
- Request-Größen.
- Timeouts.
- Retry-Verhalten.
- Queue-Längen.
- Datei- und Speicherverbrauch.

---

## 6. Gute Anwendung

### 6.1 Einfacher Spring-Boot-Endpunkt mit blockierender I/O

```java
@GetMapping("/orders/{id}")
public OrderDto getOrder(@PathVariable String id) {

    var order = orderRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Order not found: " + id));

    var user = userRepository
        .findById(order.userId())
        .orElseThrow(() -> new NotFoundException("User not found: " + order.userId()));

    return OrderDto.from(order, user);
}
```

Dieser Code ist gut, wenn folgende Bedingungen erfüllt sind:

- Der Service läuft auf Java 21+.
- Die Laufzeitumgebung unterstützt Virtual Threads.
- Datenbankverbindungen sind sauber über einen Connection Pool begrenzt.
- Timeouts sind konfiguriert.
- Fehler werden sauber behandelt.
- Der Code blockiert nicht in lang laufenden `synchronized`-Blöcken.
- Der Endpunkt ist durch Rate Limits und Tenant-Quoten geschützt, falls er teuer ist.

Der Nutzen liegt nicht darin, dass die Datenbank schneller antwortet. Der Nutzen liegt darin, dass der Request-Thread während der Wartezeit nicht dauerhaft einen teuren Betriebssystem-Thread bindet.

### 6.2 Parallele unabhängige I/O-Aufrufe mit Virtual Thread Executor

Wenn zwei unabhängige I/O-Aufrufe parallel ausgeführt werden können, kann ein Virtual-Thread-Executor den Code einfach halten.

```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public final class UserOverviewService {

    public UserOverview loadUserOverview(String userId)
            throws ExecutionException, InterruptedException {

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var userTask = executor.submit(() -> fetchUser(userId));
            var ordersTask = executor.submit(() -> fetchOrders(userId));

            var user = userTask.get();
            var orders = ordersTask.get();

            return new UserOverview(user, orders);
        }
    }

    private User fetchUser(String userId) {
        return new User(userId);
    }

    private Orders fetchOrders(String userId) {
        return new Orders(userId);
    }

    public record User(String id) {}
    public record Orders(String userId) {}
    public record UserOverview(User user, Orders orders) {}
}
```

Diese Anwendung ist gut, wenn die Tasks wirklich unabhängig sind und keine nicht thread-sichere Ressource gemeinsam verwenden.

### 6.3 Begrenzung externer Aufrufe trotz Virtual Threads

Virtual Threads machen es leicht, viele Tasks zu starten. Genau deshalb müssen teure Ressourcen explizit begrenzt werden.

```java
import java.util.concurrent.Semaphore;

public final class PartnerClient {

    private final Semaphore partnerApiLimit = new Semaphore(50);

    public PartnerResponse callPartnerApi(String requestId) throws InterruptedException {
        partnerApiLimit.acquire();
        try {
            return doBlockingHttpCall(requestId);
        } finally {
            partnerApiLimit.release();
        }
    }

    private PartnerResponse doBlockingHttpCall(String requestId) {
        return new PartnerResponse(requestId);
    }

    public record PartnerResponse(String requestId) {}
}
```

Das ist eine zentrale Produktionsregel: Virtual Threads erhöhen die Zahl der wartenden Tasks. Sie erhöhen nicht automatisch die Belastbarkeit eines Partnerdienstes.

---

## 7. Falsche Anwendung und Anti-Patterns

### 7.1 Anti-Pattern: Virtual Threads für CPU-intensive Arbeit

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var input : hugeInputs) {
        executor.submit(() -> expensiveHashCalculation(input));
    }
}
```

Das ist falsch, wenn `expensiveHashCalculation` überwiegend CPU verbraucht. Virtual Threads sind für wartende I/O-Tasks geeignet. Für CPU-intensive Arbeit sind begrenzte Executor-Strukturen, der `ForkJoinPool`, Parallel Streams oder explizite Work-Stealing-/Batching-Strategien sinnvoller.

**Regel:** CPU-intensive Tasks werden an der Anzahl verfügbarer Prozessorkerne und der gewünschten Systemlast ausgerichtet, nicht an der Zahl möglicher Virtual Threads.

### 7.2 Anti-Pattern: Unbegrenzter Fan-out auf externe Systeme

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var customerId : customerIds) {
        executor.submit(() -> partnerApi.fetchCustomer(customerId));
    }
}
```

Dieser Code kann technisch sehr viele Virtual Threads starten. Fachlich kann er trotzdem falsch sein, weil er Partner-APIs, Netzwerk, Datenbanken oder Rate Limits überlastet.

**Korrektur:** Fan-out immer mit Semaphoren, Timeouts, Bulkheads, Rate Limits oder Queueing begrenzen.

### 7.3 Anti-Pattern: Blockierende I/O in `synchronized`

```java
public final class InvoiceExporter {

    private final Object lock = new Object();

    public void exportInvoice(String invoiceId) {
        synchronized (lock) {
            var invoice = loadInvoiceFromDatabase(invoiceId);
            sendToExternalArchive(invoice);
        }
    }

    private Invoice loadInvoiceFromDatabase(String invoiceId) {
        return new Invoice(invoiceId);
    }

    private void sendToExternalArchive(Invoice invoice) {
        // blockierender HTTP- oder Datei-Aufruf
    }

    public record Invoice(String id) {}
}
```

Dieser Code ist problematisch, weil ein Virtual Thread während blockierender I/O im Monitor festgehalten werden kann. Das kann Pinning verursachen und die Skalierung massiv reduzieren.

Besser ist es, die kritische Sektion klein zu halten und keine blockierende I/O innerhalb des Locks auszuführen.

```java
import java.util.concurrent.locks.ReentrantLock;

public final class InvoiceExporter {

    private final ReentrantLock stateLock = new ReentrantLock();
    private ExportState state = new ExportState();

    public void exportInvoice(String invoiceId) {
        var invoice = loadInvoiceFromDatabase(invoiceId);

        stateLock.lock();
        try {
            state.markStarted(invoiceId);
        } finally {
            stateLock.unlock();
        }

        sendToExternalArchive(invoice);

        stateLock.lock();
        try {
            state.markFinished(invoiceId);
        } finally {
            stateLock.unlock();
        }
    }

    private Invoice loadInvoiceFromDatabase(String invoiceId) {
        return new Invoice(invoiceId);
    }

    private void sendToExternalArchive(Invoice invoice) {
        // blockierender Aufruf außerhalb der kritischen Sektion
    }

    public record Invoice(String id) {}

    private static final class ExportState {
        void markStarted(String invoiceId) {}
        void markFinished(String invoiceId) {}
    }
}
```

Die Regel lautet nicht „ersetze jedes `synchronized`“. Die Regel lautet: **Keine häufige oder lang laufende blockierende I/O innerhalb von `synchronized`.**

### 7.4 Anti-Pattern: Virtual Threads als Ersatz für Timeouts

```java
public OrderDto getOrder(String id) {
    var order = orderRepository.findById(id).orElseThrow();
    var details = slowPartnerClient.loadDetails(order.partnerId());
    return OrderDto.from(order, details);
}
```

Auch mit Virtual Threads kann ein Partneraufruf hängen bleiben. Der blockierte Task ist leichtergewichtig, aber er ist trotzdem ein belegter Task mit Speicher, Kontext und fachlicher Auswirkung.

**Regel:** Jeder externe Aufruf braucht Timeouts. Jeder Retry braucht Begrenzung. Jeder teure Flow braucht Abbruchlogik.

### 7.5 Anti-Pattern: Gemeinsamer JPA-`EntityManager` in parallelen Tasks

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var taskA = executor.submit(() -> entityManager.find(User.class, userId));
    var taskB = executor.submit(() -> entityManager.find(Order.class, orderId));

    return new Result(taskA.get(), taskB.get());
}
```

Das ist gefährlich, weil Persistenzkontexte und EntityManager-Instanzen typischerweise nicht für parallele Nutzung durch mehrere Threads gedacht sind. Parallelisierung darf nicht dadurch entstehen, dass nicht thread-sichere Infrastruktur gemeinsam in mehrere Virtual Threads gereicht wird.

**Regel:** Parallelisierte Tasks benötigen getrennte, thread-sichere Ressourcen oder klar getrennte Transaktions-/Persistenzkontexte.

---

## 8. Security- und SaaS-Relevanz

### 8.1 Virtual Threads lösen keine Ressourcenerschöpfung

Virtual Threads machen es leichter, sehr viele gleichzeitige Anfragen zu verarbeiten. Genau deshalb können sie unbeabsichtigt DoS- und Ressourcenerschöpfungsprobleme verschärfen, wenn fachliche Limits fehlen.

Für SaaS-Plattformen gilt:

- Jeder Tenant braucht faire Ressourcenbegrenzung.
- Teure Endpunkte brauchen Rate Limits.
- externe Partner-APIs brauchen Bulkheads und Timeouts.
- Datenbankzugriffe brauchen Connection-Pool-Limits.
- lange laufende Operationen brauchen Abbruch- und Monitoring-Regeln.
- Uploads, Exporte, Reports und Suchendpunkte brauchen Größen- und Laufzeitlimits.

Ein System ist nicht sicherer, nur weil es mehr gleichzeitige Tasks starten kann.

### 8.2 Noisy-Neighbor-Risiko in Multi-Tenant-Systemen

In einer SaaS-Plattform kann ein einzelner Tenant viele gleichzeitige Virtual Threads auslösen und dadurch Datenbankverbindungen, Partner-API-Kontingente oder CPU-Nachverarbeitung belegen. Das ist ein klassisches Noisy-Neighbor-Problem.

**Verbindliche Regel:** Hohe Nebenläufigkeit MUSS pro Tenant, pro User oder pro API-Schlüssel begrenzt werden, wenn die Operation teure oder knappe Ressourcen verwendet.

Beispielhafte Grenzen:

- maximal gleichzeitige Report-Generierungen pro Tenant.
- maximal gleichzeitige externe API-Aufrufe pro Tenant.
- maximal gleichzeitige Exporte pro Organisation.
- getrennte Queues für Premium-/Standard-Tenants, falls das Geschäftsmodell dies vorsieht.
- technische Schutzgrenzen gegen Mass Requests.

### 8.3 Authentifizierungs- und Security-Kontext

Viele Java-Frameworks nutzen `ThreadLocal`, um Security-Kontext, Request-Kontext, Locale, Tracing oder Transaktionsinformationen zu halten. Virtual Threads unterstützen `ThreadLocal`, aber bei sehr vielen Virtual Threads können große ThreadLocal-Werte zu Speicherproblemen führen.

**Regeln:**

- Große Objekte dürfen nicht unkritisch in `ThreadLocal` gespeichert werden.
- Security-Kontext darf nicht manuell in globale statische Strukturen ausgelagert werden.
- Bei selbst gestarteten Virtual Threads muss geprüft werden, ob Security-, Tracing- und Tenant-Kontext korrekt weitergegeben wird.
- Kontextweitergabe muss explizit getestet werden.
- `InheritableThreadLocal` darf nicht unkritisch verwendet werden, weil dadurch Kontext ungewollt in Untertasks gelangen kann.

### 8.4 Logging und Observability

Virtual Threads können die Zahl paralleler Abläufe stark erhöhen. Dadurch steigt auch das Log-Volumen. Schlechte Logging-Strategien werden dadurch schneller zu Kosten-, Datenschutz- und Analyseproblemen.

**Regeln:**

- Keine sensiblen Daten in Logs.
- Correlation IDs müssen erhalten bleiben.
- Tenant-ID, Request-ID und User-Kontext sollen nur in der notwendigen, freigegebenen Form geloggt werden.
- Fehlerhäufungen durch Fan-out dürfen nicht zu Log-Stürmen führen.
- Pinning-Ereignisse, Timeouts und externe Fehler müssen beobachtbar sein.
- Logs dürfen keine Tokens, Secrets, vollständigen personenbezogenen Nutzdaten oder Zahlungsdaten enthalten.

### 8.5 Fail-Fast und Fail-Closed

Bei parallelen I/O-Aufrufen muss klar sein, was bei Fehlern passiert. Besonders in sicherheitsrelevanten Flows darf ein fehlgeschlagener Untertask nicht stillschweigend ignoriert werden.

Beispiele:

- Wenn die Berechtigungsprüfung fehlschlägt, darf die Fachoperation nicht fortgesetzt werden.
- Wenn Tenant-Auflösung fehlschlägt, darf kein Fallback auf einen Default-Tenant erfolgen.
- Wenn eine externe Risiko- oder Fraud-Prüfung nicht erreichbar ist, muss die fachlich definierte sichere Standardreaktion greifen.
- Wenn ein Untertask abbricht, müssen abhängige Untertasks sauber abgebrochen oder ignoriert werden.

---

## 9. Framework- und Plattform-Kontext

### 9.1 Spring Boot 3.2+

Spring Boot 3.2 unterstützt Virtual Threads bei Ausführung auf Java 21+ über:

```properties
spring.threads.virtual.enabled=true
```

Vor produktiver Aktivierung MUSS geprüft werden:

- Welche Executor- und Scheduler-Komponenten werden tatsächlich auf Virtual Threads umgestellt?
- Gibt es `@Scheduled`-Jobs, die die JVM am Leben halten sollen?
- Ist `spring.main.keep-alive=true` erforderlich?
- Gibt es blockierende Aufrufe innerhalb von `synchronized`?
- Sind Datenbank-, HTTP-Client- und Messaging-Pools korrekt dimensioniert?
- Funktionieren Security-Kontext, Tracing und MDC wie erwartet?
- Gibt es Lasttests mit realistischen I/O-Latenzen?

### 9.2 Spring MVC vs. Spring WebFlux

Virtual Threads stärken den synchronen Spring-MVC-Stil bei blockierenden Workloads. Sie machen viele WebFlux-Anwendungsfälle unnötig, wenn WebFlux nur eingeführt wurde, um Thread-Knappheit bei blockierender I/O zu umgehen.

WebFlux bleibt sinnvoll, wenn:

- echtes Streaming erforderlich ist.
- Backpressure über die gesamte Verarbeitungskette relevant ist.
- ein bestehender reaktiver Stack konsistent betrieben wird.
- Netty-/Event-Loop-Architektur bewusst genutzt wird.
- non-blocking Treiber und reaktive End-to-End-Verarbeitung tatsächlich vorhanden sind.

WebFlux ist nicht automatisch falsch. Falsch ist es, reaktive Komplexität einzuführen, wenn ein einfacher synchroner Ablauf mit Virtual Threads die Anforderungen besser erfüllt.

### 9.3 JDBC, JPA und Datenbank-Pools

Virtual Threads passen gut zu blockierenden JDBC- und JPA-Aufrufen, solange die Ressourcenbegrenzung stimmt. Der Datenbank-Connection-Pool bleibt aber ein hartes Limit. Wenn der Pool maximal 30 Verbindungen bereitstellt, können auch 30.000 Virtual Threads nicht gleichzeitig produktiv Datenbankarbeit leisten.

**Regeln:**

- Connection-Pool-Größe bewusst dimensionieren.
- Query-Timeouts setzen.
- Transaktionen kurz halten.
- keine parallele Nutzung derselben EntityManager-/Session-Instanz in mehreren Threads.
- langsame Queries nicht durch mehr Nebenläufigkeit kaschieren.
- Datenbankmetriken beobachten: Wait Time, Active Connections, Pool Exhaustion, Lock Waits.

### 9.4 HTTP-Clients und Partner-Integrationen

Virtual Threads eignen sich gut für blockierende HTTP-Clients, wenn Timeouts, Verbindungspools und Limits sauber konfiguriert sind.

**Regeln:**

- Connect Timeout setzen.
- Read/Response Timeout setzen.
- maximale gleichzeitige Partneraufrufe begrenzen.
- Retries begrenzen und jitter verwenden, wenn Retry-Strategien vorhanden sind.
- Circuit-Breaker-/Bulkhead-Strategien prüfen.
- keine unlimitierte Parallelisierung gegen Drittanbieter.

### 9.5 Apache Wicket und Servlet-Umgebungen

In Wicket-basierten Anwendungen wirken Virtual Threads primär über den Servlet-Container und die Serverkonfiguration. Der Wicket-Request-Cycle, Komponentenmodelle und Serialisierungspflichten werden dadurch nicht aufgehoben.

**Regeln für Wicket-Kontexte:**

- Wicket-Komponenten dürfen nicht unkritisch in selbst gestartete Virtual Threads gegeben werden.
- UI-Komponenten und Models müssen weiter nach Wicket-Regeln behandelt werden.
- Long-running Tasks sollten über Services, Queues oder klar definierte Hintergrundprozesse laufen.
- Request-gebundene Objekte dürfen nicht außerhalb ihres Lebenszyklus verwendet werden.
- Detachable Models und Serialisierbarkeit bleiben unabhängig von Virtual Threads relevant.

---

## 10. Designregeln

### 10.1 Wann Virtual Threads verwendet werden sollen

Virtual Threads SOLLEN verwendet werden, wenn alle folgenden Aussagen überwiegend zutreffen:

1. Der Workload ist I/O-lastig.
2. Viele Tasks warten auf Datenbank, Netzwerk, Datei oder externe Dienste.
3. Der synchrone Code ist fachlich einfacher als ein reaktiver Ablauf.
4. Die Anwendung läuft auf Java 21+.
5. Die Framework-Version unterstützt Virtual Threads zuverlässig.
6. Downstream-Ressourcen sind begrenzt.
7. Timeouts und Fehlerbehandlung sind vorhanden.
8. Lasttests zeigen einen Nutzen oder mindestens keine Verschlechterung.
9. Pinning wurde geprüft.
10. Observability ist eingerichtet.

### 10.2 Wann Virtual Threads nicht verwendet werden sollen

Virtual Threads SOLLEN NICHT als Hauptlösung verwendet werden, wenn:

1. Der Workload CPU-intensiv ist.
2. die Operationen lange CPU-Zeit verbrauchen und kaum warten.
3. ein Event Loop nicht blockiert werden darf.
4. echter Backpressure über Streaming-Pipelines notwendig ist.
5. die Anwendung keine relevante Nebenläufigkeit hat.
6. die Hauptprobleme schlechte Queries, fehlende Indizes oder langsame Partner-APIs sind.
7. Ressourcenlimits fehlen.
8. Lasttests nicht durchgeführt wurden.
9. viele lange blockierende `synchronized`-Abschnitte vorhanden sind.
10. Security- und Tenant-Grenzen nicht definiert sind.

### 10.3 Entscheidungsbaum

```text
Ist der Workload überwiegend I/O-lastig?
├─ Nein → Virtual Threads sind wahrscheinlich nicht der Haupthebel.
└─ Ja
   ├─ Ist synchroner Code fachlich einfacher und gut testbar?
   │  ├─ Nein → Reaktive/eventgetriebene Architektur prüfen.
   │  └─ Ja
   │     ├─ Läuft die Anwendung auf Java 21+ und einem unterstützten Framework?
   │     │  ├─ Nein → Erst Plattformvoraussetzungen schaffen.
   │     │  └─ Ja
   │     │     ├─ Gibt es Timeouts, Limits, Rate Limiting und Observability?
   │     │     │  ├─ Nein → Erst Betriebs- und Sicherheitsgrenzen ergänzen.
   │     │     │  └─ Ja → Virtual Threads einsetzen oder kontrolliert pilotieren.
```

---

## 11. Codebeispiele

### 11.1 Virtual Thread direkt starten

```java
public final class SimpleVirtualThreadExample {

    public void runTask() throws InterruptedException {
        var thread = Thread.ofVirtual()
            .name("qg-java-worker-")
            .start(this::doBlockingWork);

        thread.join();
    }

    private void doBlockingWork() {
        System.out.println("Running in " + Thread.currentThread());
    }
}
```

### 11.2 Virtual Thread pro Task

```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public final class VirtualThreadPerTaskExample {

    public String load() throws ExecutionException, InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = executor.submit(this::blockingCall);
            return result.get();
        }
    }

    private String blockingCall() {
        return "result";
    }
}
```

### 11.3 Fall mit mehreren Ergebnissen

```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public final class CustomerDashboardService {

    public Dashboard loadDashboard(String customerId)
            throws ExecutionException, InterruptedException {

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var profileTask = executor.submit(() -> loadProfile(customerId));
            var invoiceTask = executor.submit(() -> loadInvoices(customerId));
            var ticketTask = executor.submit(() -> loadTickets(customerId));

            return new Dashboard(
                profileTask.get(),
                invoiceTask.get(),
                ticketTask.get()
            );
        }
    }

    private Profile loadProfile(String customerId) {
        return new Profile(customerId);
    }

    private Invoices loadInvoices(String customerId) {
        return new Invoices(customerId);
    }

    private Tickets loadTickets(String customerId) {
        return new Tickets(customerId);
    }

    public record Profile(String customerId) {}
    public record Invoices(String customerId) {}
    public record Tickets(String customerId) {}
    public record Dashboard(Profile profile, Invoices invoices, Tickets tickets) {}
}
```

### 11.4 Structured Concurrency als Preview-Konzept

Structured Concurrency passt konzeptionell sehr gut zu Virtual Threads, weil mehrere Untertasks als zusammengehörige Arbeitseinheit behandelt werden. In Java 21 ist `StructuredTaskScope` jedoch eine Preview-API. Deshalb ist es in dieser Richtlinie kein verbindlicher Produktionsstandard.

Preview-Code darf nur verwendet werden, wenn das Projekt bewusst Preview-Features erlaubt, Build und Runtime entsprechend konfiguriert sind und die Risiken bei Versionswechseln akzeptiert wurden.

Beispielhafte Java-21-Preview-Kompilierung:

```bash
javac --release 21 --enable-preview Main.java
java --enable-preview Main
```

---

## 12. Review-Checkliste

Eine Änderung mit Virtual Threads darf nur freigegeben werden, wenn die folgenden Fragen beantwortet sind:

| Aspekt | Details/Erklärung | Prüffrage | Erwartung |
|---|---|---|---|
| Workload | Virtual Threads passen primär zu I/O-lastiger Nebenläufigkeit. | Wartet der Code überwiegend auf I/O? | Ja, oder Abweichung begründet |
| CPU-Last | CPU-intensive Aufgaben profitieren nicht automatisch. | Wird CPU-Arbeit fälschlich massiv parallelisiert? | Nein |
| Downstream-Limits | Externe Systeme bleiben begrenzt. | Sind Datenbank, HTTP-Clients und Partner-APIs begrenzt? | Ja |
| Timeouts | Blockierende Aufrufe brauchen Abbruchgrenzen. | Sind Connect-, Read- und Query-Timeouts gesetzt? | Ja |
| Fan-out | Virtual Threads erleichtern unlimitierte Parallelisierung. | Gibt es eine Obergrenze für parallele Untertasks? | Ja |
| Pinning | Blockierende I/O in `synchronized` kann Carrier binden. | Wurde auf häufige/lang laufende Pinning-Risiken geprüft? | Ja |
| ThreadLocal | Viele Virtual Threads können ThreadLocal-Speicher multiplizieren. | Werden große ThreadLocal-Werte vermieden? | Ja |
| Security-Kontext | Kontextweitergabe muss korrekt bleiben. | Funktionieren Tenant-, User-, Security- und Tracing-Kontext? | Ja |
| Multi-Tenant-Schutz | Ein Tenant darf nicht alle Ressourcen belegen. | Gibt es tenantbezogene Limits für teure Operationen? | Ja |
| Logging | Mehr Nebenläufigkeit kann Log-Volumen erhöhen. | Werden sensible Daten vermieden und Log-Stürme verhindert? | Ja |
| Observability | Betrieb muss Virtual Threads erkennen können. | Sind JFR, Metriken, Thread Dumps und Dashboards vorbereitet? | Ja |
| Migration | Produktive Aktivierung braucht Lasttests. | Gibt es Vorher-/Nachher-Metriken? | Ja |

---

## 13. Automatisierbare Prüfungen

### 13.1 JVM-Diagnose

Für Migrations- und Lasttestumgebungen SOLLEN Pinning-Diagnosen aktiviert werden:

```bash
-Djdk.tracePinnedThreads=full
```

Alternativ kann eine kürzere Ausgabe genutzt werden:

```bash
-Djdk.tracePinnedThreads=short
```

In produktionsnahen Lasttests SOLLEN JFR-Ereignisse ausgewertet werden, insbesondere:

- `jdk.VirtualThreadPinned`
- `jdk.VirtualThreadStart`
- `jdk.VirtualThreadEnd`
- `jdk.VirtualThreadSubmitFailed`

### 13.2 CI-/Review-Prüfungen

Automatisierungsideen:

- statische Suche nach `synchronized` in Klassen mit blockierenden HTTP-, JDBC- oder Dateiaufrufen.
- Architekturregel gegen unlimitierte Fan-out-Loops mit `newVirtualThreadPerTaskExecutor()`.
- Regel: externe Clients müssen Timeouts besitzen.
- Regel: teure Operationen müssen Rate Limits oder Bulkheads besitzen.
- Testprofil mit `-Djdk.tracePinnedThreads=full`.
- Lasttest-Gate mit Vorher-/Nachher-Vergleich.
- Observability-Check für Thread-Dumps und JFR-Auswertung.

### 13.3 Metriken

Für produktive Bewertung SOLLEN mindestens folgende Metriken betrachtet werden:

- Request Throughput.
- p95/p99-Latenz.
- Fehlerrate.
- Timeout-Rate.
- aktive DB-Verbindungen.
- Wartezeit auf DB-Verbindungen.
- externe API-Latenz.
- Thread-Pool-Warteschlangen, falls weiterhin vorhanden.
- Zahl und Dauer von Pinning-Ereignissen.
- Speicherverbrauch.
- GC-Verhalten.
- Tenant-bezogene Ressourcennutzung.

---

## 14. Migration bestehender Anwendungen

### 14.1 Schritt 1: Workload klassifizieren

Vor der Migration muss festgestellt werden, ob die Anwendung wirklich I/O-lastig ist. Hinweise:

- Viele Requests warten auf Datenbank oder externe HTTP-Dienste.
- Thread Pools laufen voll, obwohl CPU nicht ausgelastet ist.
- Latenz entsteht vor allem durch Wartezeit.
- reaktive Komplexität wurde nur eingeführt, um blockierende I/O skalierbar zu machen.

### 14.2 Schritt 2: Baseline messen

Vor der Aktivierung von Virtual Threads müssen Metriken erfasst werden:

- Durchsatz.
- p95/p99-Latenz.
- CPU-Auslastung.
- Speicherverbrauch.
- aktive Threads.
- Datenbankpool-Auslastung.
- externe API-Latenzen.
- Fehlerraten.
- Timeouts.
- Queue-Längen.

### 14.3 Schritt 3: Risiken im Code suchen

Vor Aktivierung prüfen:

- blockierende I/O in `synchronized`.
- große `ThreadLocal`-Objekte.
- implizite Kontextweitergabe.
- parallele Nutzung nicht thread-sicherer Objekte.
- fehlende Timeouts.
- fehlende Tenant-Limits.
- unlimitierte Schleifen mit externen Aufrufen.
- lang laufende Transaktionen.
- Log-Ausgaben mit sensiblen Daten.

### 14.4 Schritt 4: Virtual Threads in Testumgebung aktivieren

Für Spring Boot:

```properties
spring.threads.virtual.enabled=true
```

Bei Bedarf:

```properties
spring.main.keep-alive=true
```

Zusätzlich in Test-/Lasttestumgebungen:

```bash
-Djdk.tracePinnedThreads=full
```

### 14.5 Schritt 5: Lasttest mit realistischen Latenzen

Lasttests müssen realistische Bedingungen enthalten:

- echte oder simulierte Datenbanklatenz.
- echte oder simulierte Partner-API-Latenz.
- Timeouts.
- Fehlerfälle.
- hohe Tenant-Konzentration.
- langsame Clients.
- große Requests, falls fachlich möglich.
- parallele teure Operationen.

### 14.6 Schritt 6: Kontrollierter Rollout

Produktive Aktivierung sollte schrittweise erfolgen:

1. einzelner Service oder begrenzter Endpoint.
2. begrenzte Traffic-Gruppe.
3. enges Monitoring.
4. Vergleich mit Baseline.
5. Prüfung von Pinning, DB-Wartezeit und Fehlerraten.
6. Erweiterung nur bei stabilen Ergebnissen.

---

## 15. Ausnahmen

Eine klassische Platform-Thread-, Reactive- oder begrenzte Executor-Lösung darf verwendet werden, wenn mindestens einer der folgenden Gründe vorliegt:

- CPU-intensive Verarbeitung steht im Vordergrund.
- Backpressure über eine Streaming-Pipeline ist erforderlich.
- bestehender reaktiver Stack ist stabil, konsistent und fachlich begründet.
- Bibliotheken sind nicht mit Virtual Threads kompatibel oder erzeugen problematisches Pinning.
- Monitoring und Betrieb sind für Virtual Threads noch nicht vorbereitet.
- regulatorische oder betriebliche Anforderungen verlangen ein konservatives Rollout.
- die Anwendung hat keine relevante Nebenläufigkeit.
- der Hauptengpass liegt eindeutig in Datenbankdesign, Query-Qualität oder Partner-API-Latenz.

Ausnahmen müssen im Pull Request oder im technischen Design nachvollziehbar begründet werden.

---

## 16. Definition of Done

Eine Änderung erfüllt diese Richtlinie, wenn folgende Kriterien erfüllt sind:

- Der Workload ist als überwiegend I/O-lastig klassifiziert oder die Abweichung ist begründet.
- Java 21+ wird verwendet.
- Framework- und Runtime-Unterstützung sind geprüft.
- Virtual Threads werden nicht für CPU-intensive Massenausführung missbraucht.
- Es gibt Timeouts für externe und interne blockierende Aufrufe.
- Downstream-Ressourcen sind begrenzt.
- Fan-out ist kontrolliert.
- Datenbank-Connection-Pools sind passend dimensioniert.
- Es gibt keine häufige oder lang laufende blockierende I/O innerhalb von `synchronized`.
- Pinning wurde in Test- oder Lasttestumgebung geprüft.
- Security-, Tenant-, User- und Tracing-Kontext funktionieren korrekt.
- Multi-Tenant-Limits sind für teure Operationen definiert.
- Logs enthalten keine sensiblen Daten.
- Vorher-/Nachher-Metriken liegen vor.
- Die Änderung wurde unter realistischen Lastbedingungen getestet.
- Reviewer können nachvollziehen, warum Virtual Threads in diesem Fall passend sind.

---

## 17. Häufige Missverständnisse

### „Virtual Threads machen meinen Code schneller.“

Falsch. Virtual Threads führen CPU-Code nicht schneller aus. Sie verbessern Skalierbarkeit und Durchsatz bei vielen wartenden Tasks.

### „Mit Virtual Threads brauche ich keine Connection Pools mehr.“

Falsch. Gerade weil mehr parallele Tasks möglich sind, bleiben Connection Pools und Ressourcenlimits notwendig.

### „Virtual Threads ersetzen WebFlux vollständig.“

Falsch. Virtual Threads ersetzen viele Fälle, in denen Reactive nur wegen Thread-Knappheit eingeführt wurde. Sie ersetzen nicht echtes Streaming, Backpressure oder eventgetriebene Architektur.

### „`newVirtualThreadPerTaskExecutor()` ist ein Anti-Pattern, weil für jeden Task ein Thread erstellt wird.“

Falsch. Genau das ist das vorgesehene Nutzungsmodell. Falsch ist nicht der Virtual Thread pro Task, sondern unlimitierter Zugriff auf knappe Ressourcen.

### „Jedes `synchronized` muss weg.“

Falsch. Problematisch ist häufige oder lang laufende blockierende I/O innerhalb von `synchronized`. Kurze speicherinterne Synchronisation ist nicht automatisch ein Problem.

### „Structured Concurrency ist in Java 21 produktionsreif.“

Falsch formuliert. Structured Concurrency ist in Java 21 eine Preview-API. Sie kann konzeptionell hilfreich sein, ist aber in dieser Richtlinie kein verbindlicher Produktionsstandard.

---

## 18. Quellen und weiterführende Literatur

- OpenJDK — JEP 444: Virtual Threads  
  https://openjdk.org/jeps/444

- Oracle Java SE 21 Documentation — Virtual Threads  
  https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html

- Spring Boot 3.2 Reference Documentation — Virtual Threads  
  https://docs.spring.io/spring-boot/docs/3.2.8/reference/html/features.html#features.spring-application.virtual-threads

- OpenJDK — JEP 453: Structured Concurrency (Preview)  
  https://openjdk.org/jeps/453

- OWASP API Security Top 10 — API4:2023 Unrestricted Resource Consumption  
  https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/

- OWASP Cheat Sheet Series — Denial of Service Cheat Sheet  
  https://cheatsheetseries.owasp.org/cheatsheets/Denial_of_Service_Cheat_Sheet.html

- OWASP Cheat Sheet Series — Logging Cheat Sheet  
  https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
