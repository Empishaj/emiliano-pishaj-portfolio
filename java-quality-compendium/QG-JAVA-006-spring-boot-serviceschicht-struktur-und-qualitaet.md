# QG-JAVA-006 — Spring-Boot-Serviceschicht: Struktur und Qualität

## Dokumentstatus

| Aspekt | Details/Erklärung |
| --- | --- |
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-006 |
| Titel | Spring-Boot-Serviceschicht: Struktur und Qualität |
| Status | Accepted / verbindlicher Standard für neue und wesentlich geänderte Spring-Boot-Services |
| Version | 2.0 |
| Zielgruppe | Java-Entwickler, Tech Leads, Reviewer, QA, Security, Architektur |
| Primärer Kontext | Java 21+, Spring Boot 3.4+, Spring Framework 6.x, SaaS-Plattformen, REST-/API-Backends, transaktionale Geschäftslogik |
| Java-Baseline | Java 21+ als Kompendium-Standard |
| Spring-Baseline | Spring Boot 3.4+ / Spring Framework 6.x als Mindestkontext; neuere Versionen sind zulässig, sofern die hier beschriebenen Architektur- und Transaktionsregeln weiterhin erfüllt werden |
| Kategorie | Spring Boot / Architektur / Service Design / Transaktionsgrenzen |
| Letzte Validierung | 2026-04-29 |
| Validierte Quellenbasis | Spring Framework Reference Documentation zu Dependency Injection, Transaktionen, Validation, Events und Error Responses; Spring Boot Reference Documentation zu Task Execution und Virtual Threads; Spring Data JPA Reference Documentation zu Projections und Locking; Hibernate Validator 8 Reference; Jakarta Bean Validation 3.0; Spring Modulith Reference; OWASP API Security Top 10 2023; OWASP Authentication Cheat Sheet; OWASP Logging Cheat Sheet; RFC 9457 Problem Details for HTTP APIs |
| Technische Beispielvalidierung | Reine Java-21-Beispiele ohne Spring-Abhängigkeiten wurden mit `javac --release 21` syntaktisch geprüft. Spring-spezifische Beispiele sind referenzbasiert gegen die offizielle Spring-Dokumentation validiert und benötigen ein Spring-Boot-Projekt mit den entsprechenden Dependencies. |
| Verbindlichkeit | Diese Richtlinie gilt verbindlich für neue Services und für wesentlich geänderte bestehende Services. Abweichungen sind zulässig, wenn ein konkreter fachlicher, technischer oder architektonischer Grund im Pull Request nachvollziehbar dokumentiert wird. |
| Wesentliche Änderungen ggü. v1.0 | Eigene Sektion zu Bean Validation auf Records · Outbox mit Same-Datasource-Anforderung und Polling-Worker · Trennung lokales Bulkhead vs. verteiltes Rate-Limiting · Tenant-Context-Pattern · Optimistic Locking mit `@Version` · JPA-Entity-Lifecycle · `findByIdAndTenantId` als Default-Pattern · User Enumeration · Spring Modulith · Idempotency-Keys, Pagination, Soft-Delete · erweiterte ArchUnit-Regeln · Inhaltsverzeichnis und Lese-Anleitung. |

---

## Inhaltsverzeichnis

1. [Zweck dieser Richtlinie](#1-zweck-dieser-richtlinie)
2. [Kurzregel für Entwickler](#2-kurzregel-für-entwickler)
3. [Verbindlicher Standard](#3-verbindlicher-standard)
4. [Geltungsbereich](#4-geltungsbereich)
5. [Begriffe](#5-begriffe)
6. [Technischer Hintergrund](#6-technischer-hintergrund)
7. [Schichtenmodell und Abhängigkeitsregeln](#7-schichtenmodell-und-abhängigkeitsregeln)
8. [Schlechtes Beispiel: God Service](#8-schlechtes-beispiel-god-service)
9. [Gute Anwendung: fokussierte Serviceschicht](#9-gute-anwendung-fokussierte-serviceschicht)
10. [Event-Verarbeitung und Nebeneffekte](#10-event-verarbeitung-und-nebeneffekte)
11. [Transaktionsregeln](#11-transaktionsregeln)
12. [DTO-, Entity- und Mapping-Regeln](#12-dto--entity--und-mapping-regeln)
13. [Validierung und Fehlersemantik](#13-validierung-und-fehlersemantik)
14. [Security- und SaaS-Aspekte](#14-security--und-saas-aspekte)
15. [Async, Virtual Threads und Ressourcensteuerung](#15-async-virtual-threads-und-ressourcensteuerung)
16. [Falsche Anwendung und Anti-Patterns](#16-falsche-anwendung-und-anti-patterns)
17. [Framework- und Plattform-Kontext](#17-framework--und-plattform-kontext)
18. [Testing-Regeln](#18-testing-regeln)
19. [Review-Checkliste](#19-review-checkliste)
20. [Automatisierbare Prüfungen](#20-automatisierbare-prüfungen)
21. [Migration bestehender Services](#21-migration-bestehender-services)
22. [Ausnahmen](#22-ausnahmen)
23. [Definition of Done](#23-definition-of-done)
24. [Entscheidungsbaum](#24-entscheidungsbaum)
25. [Quellen und weiterführende Literatur](#25-quellen-und-weiterführende-literatur)

### Wie liest du dieses Dokument

Dieses Dokument ist mit knapp 90 KB ein Nachschlagewerk, kein Lesebuch. Drei Lesepfade werden empfohlen:

**Wenn du neu im Team bist:** Starte mit Sektion 2 (Kurzregel), lies dann Sektion 1 (Zweck), Sektion 7 (Schichtenmodell), Sektion 8 (Schlechtes Beispiel) und Sektion 9 (Gute Anwendung). Damit hast du die mentale Karte für 80 % aller Service-Reviews.

**Wenn du im Code-Review bist:** Springe direkt zu Sektion 19 (Review-Checkliste). Jede Zeile hat einen Anker zu der Detail-Sektion, in der die Begründung steht. Bei Verstoß gegen eine Muss-Regel: Sektion 3.1.

**Wenn du etwas Spezifisches suchst:** Inhaltsverzeichnis oben. Die häufigsten Punkte: Transaktionen → Sektion 11. Tenant-Sicherheit → Sektion 14. Validierung → Sektion 13. Outbox → Sektion 10.3. Bean Validation auf Records → Sektion 13.3.

**Wenn du eine Architektur-Entscheidung treffen musst:** Sektion 24 (Entscheidungsbaum) und der Verweis auf die jeweilige Detail-Sektion.

---

## 1. Zweck dieser Richtlinie

Diese Richtlinie beschreibt, wie Services in Spring-Boot-Anwendungen strukturiert werden sollen, damit Geschäftslogik wartbar, testbar, sicher und transaktional nachvollziehbar bleibt.

Die Serviceschicht ist in vielen Spring-Boot-Systemen der Ort, an dem Use Cases koordiniert werden: Eingaben werden geprüft, fachliche Entscheidungen getroffen, Repositories aufgerufen, Transaktionen gesteuert, Domain-Objekte verändert und Events ausgelöst. Genau deshalb wird die Serviceschicht schnell zum Qualitätsrisiko, wenn sie unstrukturiert wächst.

Typische Fehler sind:

1. Services werden zu großen Alleskönnern.
2. Controller greifen direkt auf Repositories zu.
3. Entities werden als API-Response zurückgegeben.
4. Request-DTOs werden direkt als Persistenzmodell verwendet.
5. Transaktionen werden pauschal auf Klassenebene gesetzt.
6. Externe Calls werden innerhalb langer Datenbanktransaktionen ausgeführt.
7. Events werden vor Commit ausgelöst und erzeugen inkonsistente Nebeneffekte.
8. `RuntimeException` wird als generischer Fehlercontainer missbraucht.
9. Tenant- und Berechtigungskontext werden nicht in der Serviceschicht abgesichert.
10. Unit-Tests benötigen unnötig den gesamten Spring-Kontext.
11. Bean Validation auf Records wird angenommen, aber nicht verstanden.
12. Outbox-Tabellen liegen in einer anderen Datenquelle als die Domain-Tabellen.
13. Lokale Concurrency-Limits werden mit verteilten Rate-Limits verwechselt.
14. Aggregate-Roots haben kein Optimistic Locking.
15. `findById` wird in mandantenfähigen Systemen ohne Tenant-Filter verwendet.
16. Registrierungs-APIs offenbaren Benutzerexistenz (User Enumeration).

Ziel dieser Richtlinie ist nicht, Services künstlich klein oder akademisch rein zu machen. Ziel ist, dass jeder Service klar erkennbar beantwortet:

* Welchen Use Case führt er aus?
* Welche Eingabe akzeptiert er?
* Welche fachlichen Regeln prüft er?
* Welche Transaktionsgrenze gilt?
* Welche Daten verlassen die Schicht?
* Welche Nebeneffekte entstehen?
* Welche Fehler können auftreten?
* Welche Security- und Tenant-Regeln werden durchgesetzt?
* Wie kann der Service isoliert getestet werden?

Ein guter Spring-Boot-Service ist kein Sammelbecken für alles, was irgendwo hinmuss. Er ist eine klar geschnittene Anwendungskomponente, die fachliches Verhalten koordiniert, technische Details begrenzt und Systemgrenzen bewusst behandelt.

---

## 2. Kurzregel für Entwickler

Ein Spring-Boot-Service soll genau einen fachlich zusammenhängenden Use-Case-Bereich koordinieren, keine HTTP-Typen exponieren, keine Entities an API-Grenzen zurückgeben, Transaktionen bewusst auf öffentlichen Service-Methoden setzen, fachliche Fehler über sprechende Exception-Typen ausdrücken, Tenant-Kontext aus dem Sicherheitskontext ableiten, Aggregate-Roots optimistisch sperren und externe Nebeneffekte über Outbox oder explizite After-Commit-Hooks behandeln.

Controller dürfen Requests entgegennehmen und Responses ausliefern. Repositories dürfen Daten laden und speichern. Services verbinden beides durch fachliche Regeln, Transaktionsgrenzen, Autorisierung, Mapping und Use-Case-Orchestrierung.

Vermeide God Services. Vermeide generische `RuntimeException`. Vermeide direkte Entity-Exposition. Vermeide externe Calls innerhalb offener Datenbanktransaktionen. Vermeide `@Transactional` auf nicht-öffentlichen Methoden. Vermeide Self-Invocation bei transaktionalen Methoden. Vermeide `findById` ohne Tenant-Filter in mandantenfähigen Systemen. Vermeide User Enumeration in öffentlichen Auth-APIs.

---

## 3. Verbindlicher Standard

### 3.1 Muss-Regeln

Ein Spring-Boot-Service MUSS folgende Regeln erfüllen:

1. Er MUSS einen klaren fachlichen Verantwortungsbereich haben.
2. Er MUSS seine Abhängigkeiten per Konstruktor erhalten.
3. Er MUSS fachliche Eingaben über Command-, Query- oder Request-DTOs entgegennehmen, wenn mehrere Eingabewerte fachlich zusammengehören.
4. Er DARF keine JPA-Entities direkt an Controller oder externe API-Grenzen zurückgeben.
5. Er MUSS API-Response-Daten über dedizierte Response-DTOs oder Projection-DTOs ausgeben.
6. Er MUSS Transaktionen bewusst auf den Service-Methoden setzen, die tatsächlich Datenbankarbeit koordinieren.
7. Schreibende Use Cases mit mehreren persistenzrelevanten Schritten MÜSSEN eine klare `@Transactional`-Grenze haben.
8. Lesende Use Cases SOLLTEN `@Transactional(readOnly = true)` verwenden, wenn Lazy Loading, konsistente Leseeinheiten oder Provider-Optimierungen relevant sind.
9. Fachliche Fehler MÜSSEN über sprechende Exception-Typen oder klar modellierte Result-Typen ausdrückbar sein.
10. Generische `RuntimeException`, `Exception` oder `IllegalStateException` DÜRFEN nicht als fachlicher Standardfehler verwendet werden.
11. Autorisierung und Tenant-Isolation MÜSSEN in oder unterhalb der Serviceschicht abgesichert werden, nicht ausschließlich im Controller.
12. Tenant-IDs MÜSSEN aus dem authentifizierten Sicherheitskontext abgeleitet werden, nicht aus untrusted Request-Feldern.
13. Repository-Methoden in mandantenfähigen Systemen MÜSSEN Tenant-Bezug enthalten (z. B. `findByIdAndTenantId`).
14. Aggregate-Roots, die parallel modifiziert werden können, MÜSSEN über Optimistic Locking (`@Version`) abgesichert werden.
15. Externe Nebeneffekte wie E-Mail, Billing, Webhooks oder Notifications DÜRFEN nicht unbedacht innerhalb einer offenen Datenbanktransaktion ausgeführt werden.
16. Kritische externe Nebeneffekte MÜSSEN robust über ein geeignetes Muster abgesichert werden, zum Beispiel Transactional Outbox, idempotente Jobs oder zuverlässig verarbeitete Events.
17. Outbox-Tabellen MÜSSEN in derselben Datenquelle und derselben Transaktion wie die Domain-Tabellen geschrieben werden.
18. `@Transactional` DARF NICHT auf nicht-öffentlichen Methoden gesetzt werden, wenn dadurch das Verhalten für Reviewer unklar wird.
19. Transaktionale Methoden DÜRFEN NICHT über Self-Invocation innerhalb derselben Klasse aktiviert werden, wenn dadurch der Spring-Proxy umgangen wird.
20. Services MÜSSEN isoliert unit-testbar sein, sofern ihre Logik nicht bewusst Integrationsverhalten testet.
21. Logging in Services MUSS sensible Daten, Secrets, Tokens, Passwörter, personenbezogene Daten und Tenant-Grenzen beachten.
22. Methoden, die potenziell untrusted input verarbeiten, MÜSSEN validieren oder nur bereits validierte Command-Objekte akzeptieren.
23. Bean-Validation-Annotationen auf Record-Komponenten MÜSSEN gegen die im Projekt verwendete Hibernate-Validator-Version verifiziert sein (Sektion 13.3).
24. Öffentliche Auth- und Identity-Endpoints DÜRFEN keine Benutzerexistenz, Tenant-Strukturen oder interne Regeln offenlegen (Sektion 14.5).

### 3.2 Darf-nicht-Regeln

Ein Spring-Boot-Service DARF NICHT:

1. HTTP-spezifische Typen wie `HttpServletRequest`, `ResponseEntity`, `HttpStatus`, `MultipartFile` oder `Principal` ohne klare Ausnahme tief in die Geschäftslogik tragen.
2. Controller-Annotationen oder Web-Mapping-Logik enthalten.
3. Direkt View-, Wicket- oder REST-spezifische Response-Formate als interne Geschäftsobjekte verwenden.
4. Entities als öffentliche API-Verträge zurückgeben.
5. Request-DTOs direkt als Entities speichern.
6. Alle Abhängigkeiten einer fachlichen Domäne in einem einzigen God Service sammeln.
7. Feldinjektion mit `@Autowired` verwenden.
8. Transaktionen pauschal und unreflektiert auf Klassenebene setzen.
9. Externe HTTP-Calls, E-Mail-Versand oder Billing-Calls ohne Timeout, Retry-Konzept und Fehlerstrategie ausführen.
10. `@Async` als Ersatz für ein klares Konsistenz- und Fehlerkonzept verwenden.
11. Virtual Threads als Freifahrtschein für unbegrenzte Parallelisierung behandeln.
12. Lokale `Semaphore`-basierte Limits zur Einhaltung externer API-Rate-Limits verwenden (Sektion 15.3).
13. Tenant-IDs aus Request-DTOs ungeprüft vertrauen.
14. Technische Exceptions ungefiltert bis zur API-Grenze durchreichen.
15. Datenbank-Queries über Lazy Loading in Stream-, Mapper- oder Serialization-Schritten unkontrolliert auslösen.
16. Security-relevante Entscheidungen allein in Frontend, Controller oder Client verlagern.
17. `findById(id)` in mandantenfähigen Systemen ohne Tenant-Filter verwenden.
18. Outbox-Einträge in einer separaten Datenquelle vom Domain-Schreibvorgang ablegen.
19. Auf Existenz-Antworten in öffentlichen Registrierungs-Endpoints unterschiedlich reagieren (User Enumeration).
20. Unbegrenzte Result-Sets ohne Pagination an Controller zurückgeben.

### 3.3 Sollte-Regeln

Ein Spring-Boot-Service SOLLTE:

1. Nach Use Case oder fachlicher Fähigkeit benannt werden, nicht nach einer technischen Tabelle.
2. Kleine öffentliche Methoden mit klaren Command-/Query-Objekten anbieten.
3. Mapping zwischen Entity und DTO explizit oder über klar kontrollierte Mapper durchführen.
4. Events nur für echte Entkopplung verwenden, nicht um Kontrollfluss zu verstecken.
5. `@TransactionalEventListener` verwenden, wenn ein Event erst nach erfolgreichem Commit verarbeitet werden soll.
6. Für kritische Integrationsevents ein Outbox-Muster statt reiner In-Memory-Events verwenden.
7. Read-only Queries über DTO-Projections oder gezielte Fetch-Strategien modellieren, statt vollständige Aggregate unnötig zu laden.
8. Nebeneffekte idempotent gestalten und Idempotency-Keys explizit modellieren.
9. Keine übermäßig breite Service-Schnittstelle anbieten.
10. Technische Framework-Abhängigkeiten am Rand halten.
11. Bei modularen Monolithen Spring Modulith oder vergleichbare Disziplin-Werkzeuge verwenden (Sektion 17.6).
12. Soft-Delete-Pattern explizit modellieren, wenn Audit- oder DSGVO-Anforderungen es verlangen.
13. Verteilte Rate-Limiting-Anforderungen über zentrale Mechanismen (Redis, Bucket4j-Hazelcast, Service-Mesh-Quota) lösen.

---

## 4. Geltungsbereich

Diese Richtlinie gilt für Spring-Boot-Anwendungen, die Geschäftslogik in Serviceschichten abbilden. Sie gilt insbesondere für:

1. REST-Backends.
2. SaaS-Plattformen.
3. Interne Service-APIs.
4. Modulare Monolithen.
5. Klassische mehrschichtige Spring-Boot-Anwendungen.
6. Anwendungen mit Spring MVC, Spring Data JPA, Bean Validation und transaktionaler Persistenz.
7. Anwendungen, die Events, asynchrone Verarbeitung oder externe Integrationen nutzen.
8. Anwendungen mit Mandantenfähigkeit.
9. Anwendungen mit auditierbaren Geschäftsprozessen.
10. Anwendungen, in denen Services fachliche Use Cases koordinieren.

Diese Richtlinie gilt nicht automatisch für:

1. Reine technische Adapter ohne fachliche Logik.
2. Reine Batch-Worker, sofern sie eine eigene Architekturregel besitzen.
3. Kleine Prototypen ohne Produktionsanspruch.
4. Framework-Konfiguration.
5. Reine Repository-Implementierungen.
6. Reine Mapper-Klassen.
7. Event-Consumer, die bewusst als eigener Application Service geschnitten sind.
8. Sehr einfache CRUD-Backoffice-Werkzeuge, sofern Security-, Tenant- und API-Regeln trotzdem erfüllt bleiben.

---

## 5. Begriffe

| Begriff | Details/Erklärung | Beispiel |
| --- | --- | --- |
| Service | Spring-Komponente, die einen fachlichen Use Case oder eine fachliche Fähigkeit koordiniert. | `UserRegistrationService`, `OrderCheckoutService` |
| Application Service | Service, der Eingaben entgegennimmt, Transaktionen steuert, Repositories nutzt und Domain-Operationen koordiniert. | `register(command)`, `createOrder(command)` |
| Domain Service | Fachlicher Service ohne direkten Framework-Fokus, wenn Logik nicht sinnvoll in Entity oder Value Object gehört. | `TaxCalculator`, `DiscountPolicy` |
| Repository | Abstraktion für Datenzugriff. | `UserRepository`, `OrderRepository` |
| Aggregate Root | Konsistenzeinheit aus einem oder mehreren Entities mit klarer Identität. | `Order` mit zugehörigen `OrderLines` |
| Entity | Persistentes Objekt mit Identität und Lifecycle. | `UserEntity`, `OrderEntity` |
| Managed Entity | Entity innerhalb einer aktiven JPA-Transaktion; Änderungen werden automatisch persistiert. | innerhalb `@Transactional`-Methode geladenes Objekt |
| Detached Entity | Entity außerhalb einer aktiven Transaktion; Änderungen erfordern explizites `merge()` oder `save()`. | nach Transaktionsende oder Serialisierung |
| DTO | Datentransferobjekt für Schicht- oder API-Grenzen. | `UserResponse`, `RegisterUserCommand` |
| Command | Eingabeobjekt für einen schreibenden Use Case. | `RegisterUserCommand` |
| Query | Eingabeobjekt oder Methode für einen lesenden Use Case. | `FindOrdersQuery` |
| Response DTO | Ausgabeobjekt für API oder Service-Grenze. | `UserCreatedResponse` |
| Projection | Gezielte Auswahl von Daten für lesende Zwecke. | `OrderSummaryView` |
| Transaktionsgrenze | Bereich, in dem Datenbankoperationen atomar und konsistent ausgeführt werden sollen. | `@Transactional` auf `createOrder(...)` |
| Optimistic Locking | Konfliktdetektion über Versionsfeld; Konflikt wird beim Schreiben sichtbar, nicht beim Lesen. | `@Version` auf `OrderEntity` |
| Pessimistic Locking | Sperre auf Datenbankebene, die andere Transaktionen blockiert. | `LockModeType.PESSIMISTIC_WRITE` |
| Soft Delete | Logisches Löschen über Markierung statt physischer Entfernung. | `deleted_at`-Spalte mit Hibernate-Filter |
| Idempotency Key | Eindeutiger Schlüssel, der dieselbe Operation auch bei Wiederholung nur einmal effektiv ausführt. | `Idempotency-Key`-Header bei HTTP-POST |
| Nebeneffekt | Aktion außerhalb der unmittelbaren Datenänderung. | E-Mail, Billing, Webhook, Notification |
| Outbox | Muster, bei dem Integrationsevents transaktional in der Datenbank gespeichert und später zuverlässig ausgeliefert werden. | `outbox_event`-Tabelle in derselben Datenquelle |
| Bulkhead | Lokale Begrenzung paralleler Operationen pro JVM zur Schutz vor Resource-Exhaustion. | `Semaphore` mit `tryAcquire(timeout)` |
| Distributed Rate Limit | Über alle Instanzen einer Anwendung hinweg geltende Begrenzung. | Redis-basierter Counter, Bucket4j-Hazelcast |
| Tenant-Kontext | Informationen darüber, für welchen Mandanten eine Operation ausgeführt wird. | `TenantId`, Organisation, Account |
| Tenant-Isolation | Sicherstellung, dass Daten und Operationen eines Mandanten nicht mit anderen vermischt werden. | Tenant-Filter in Repository, Tenant-aware Queries |
| User Enumeration | Sicherheitslücke, bei der eine API durch unterschiedliche Antworten Benutzerexistenz verrät. | HTTP 409 bei vorhandener vs. HTTP 200 bei neuer E-Mail |
| Self-Invocation | Aufruf einer annotierten Methode aus derselben Klasse, wodurch Spring-Proxies umgangen werden können. | `this.saveInternal()` |

---

## 6. Technischer Hintergrund

Spring-Boot-Anwendungen sind häufig schichtenorientiert aufgebaut: Controller nehmen Requests entgegen, Services koordinieren Use Cases, Repositories greifen auf Daten zu, Entities bilden Persistenzzustand ab, DTOs definieren Grenzen nach außen. Diese Struktur ist nicht deshalb sinnvoll, weil Schichten hübsch aussehen, sondern weil sie Verantwortlichkeiten trennen und Fehler lokal begrenzen.

Spring unterstützt Dependency Injection, indem Objekte ihre Abhängigkeiten über Konstruktoren, Factory-Methoden oder Properties definieren und der Container diese Abhängigkeiten beim Erzeugen der Beans bereitstellt. Für Services ist Konstruktorinjektion der bevorzugte Standard, weil Abhängigkeiten explizit, testbar und unveränderlich werden.

Spring-Transaktionen funktionieren in typischen Spring-Boot-Anwendungen proxybasiert. Das bedeutet: Die Transaktionslogik wird angewendet, wenn ein Aufruf über den Spring-Proxy läuft. Aufrufe innerhalb derselben Klasse umgehen diesen Proxy häufig. Deshalb sind Self-Invocation und `@Transactional` auf nicht-öffentlichen Methoden klassische Fehlerquellen.

Spring Framework 6.0 hat das Verhalten für CGLIB-basierte Proxies erweitert: dort können auch protected oder package-visible Methoden transaktional intercepten. Diese Möglichkeit ist technisch korrekt, aber **operativ riskant**: ein Reviewer kann beim Lesen nicht zuverlässig erkennen, ob die Klasse einen CGLIB-Proxy oder einen JDK-Dynamic-Proxy verwendet. JDK-Proxies (Interface-basiert) unterstützen weiterhin nur public. Für eine wartbare Codebasis bleibt deshalb die einfache, unmissverständliche Regel: **`@Transactional` nur auf öffentlichen Methoden**, unabhängig von der Proxy-Variante. Diese Regel gilt durchgängig in dieser Richtlinie.

Für REST-APIs stellt Spring mit `ProblemDetail` und `ErrorResponse` Mechanismen bereit, um Fehlerantworten nach RFC 9457 auszugeben. Fachliche Exceptions sollen deshalb nicht beliebig bis zum Client durchfallen, sondern über `@ControllerAdvice` oder vergleichbare Fehler-Mapping-Mechanismen in stabile API-Fehler übersetzt werden.

JPA-Entities haben einen klar definierten Lifecycle: `new` (außerhalb der Persistenz), `managed` (innerhalb einer aktiven Transaktion, Änderungen werden automatisch persistiert), `detached` (nach Transaktionsende oder Serialisierung), `removed` (zum Löschen markiert). Diese Zustände sind unsichtbare Voraussetzung für viele Patterns in dieser Richtlinie. Innerhalb einer `@Transactional`-Methode reicht das Setzen eines Feldes, um eine Änderung zu persistieren — `save()` ist redundant, solange die Entity managed ist. Außerhalb braucht es explizit `merge()` oder einen erneuten Repository-Aufruf.

Bean Validation 3.0 (Jakarta) prüft Annotationen auf Feldern, Methoden und Parametern. Records werden vom Validator-Provider Hibernate Validator 8 als Property-Container interpretiert, sodass Annotationen auf Record-Komponenten in der Praxis funktionieren. Die genaue Semantik hängt allerdings von Hibernate-Validator-Version, Spring-Version und vom Zusammenspiel mit `@Validated` und `@Valid` ab. Sektion 13.3 behandelt diese Subtilität explizit.

---

## 7. Schichtenmodell und Abhängigkeitsregeln

### 7.1 Grundmodell

Die Standardstruktur lautet:

```
Controller / API Adapter
        ↓
Application Service
        ↓
Domain / Policies / Entities / Value Objects
        ↓
Repository / Persistence Adapter
```

Das ist kein Dogma. Es ist eine Qualitätsregel gegen Kopplung, unkontrollierten Datenzugriff und zufällige Architektur.

### 7.2 Erlaubte und verbotene Abhängigkeiten

| Schicht | Darf abhängen von | Darf nicht abhängen von | Begründung |
| --- | --- | --- | --- |
| Controller | Service, Request-DTO, Response-DTO, Mapper am Rand | Repository, Entity als API-Vertrag, Datenbankdetails | Controller sollen HTTP übersetzen, nicht Geschäftslogik oder Persistenz steuern. |
| Service | Repository, Domain, Mapper, Policies, Event Publisher, Security-Kontext, Tenant-Kontext | Controller, `ResponseEntity`, `HttpServletRequest`, UI-Typen | Services sollen Use Cases koordinieren und nicht vom Web-Protokoll abhängig sein. |
| Domain | Value Objects, Domain Policies, fachliche Typen | Spring MVC, Controller, Repository, HTTP, technische Infrastruktur | Domain-Logik soll möglichst frameworkarm bleiben. |
| Repository | Entity, Projection, Datenzugriffsmechanismen | Controller, Service-Orchestrierung, HTTP | Repositories laden und speichern Daten, sie steuern keine Use Cases. |
| Entity | JPA-Annotationen, Value Objects, fachliche Methoden | Controller, Response-DTOs, externe API-Typen | Entities dürfen Persistenzmodell sein, aber nicht API-Vertrag. |
| Event Handler | Event, benötigter Port/Service, Outbox/Integration | Zufälliger Zugriff auf alles | Handler sollen Nebeneffekte gezielt und testbar behandeln. |

### 7.3 Paketstruktur

Eine sinnvolle Paketstruktur kann so aussehen:

```
com.example.user
├── api
│   ├── UserController.java
│   ├── RegisterUserRequest.java
│   └── UserResponse.java
├── application
│   ├── UserRegistrationService.java
│   ├── RegisterUserCommand.java
│   ├── UserCreatedResponse.java
│   └── UserRegisteredEvent.java
├── domain
│   ├── EmailAddress.java
│   ├── PasswordPolicy.java
│   └── UserStatus.java
├── persistence
│   ├── UserEntity.java
│   └── UserRepository.java
└── integration
    ├── WelcomeEmailHandler.java
    └── BillingTrialHandler.java
```

Diese Struktur trennt API-Vertrag, Use-Case-Logik, fachliche Typen, Persistenz und Integrationen. Dadurch werden Reviews einfacher, Abhängigkeiten sichtbarer und Tests gezielter. Bei modularen Monolithen ist diese Paketstruktur die Einheit, die durch Spring Modulith verifiziert wird (Sektion 17.6).

---

## 8. Schlechtes Beispiel: God Service

```java
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final BillingService billingService;
    private final NotificationService notificationService;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void registerUser(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("bad name");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        emailService.sendWelcomeEmail(email, name);
        billingService.createFreeTrialSubscription(user.getId());
        notificationService.notifyAdmins("New user: " + email);
    }

    public List<User> getAllUsersWithOrders() {
        return userRepository.findAll().stream()
                .peek(user -> user.getOrders().size())
                .toList();
    }
}
```

### Warum ist das schlecht?

1. `@Transactional` auf Klassenebene versteckt, welche Methoden wirklich transaktional sein müssen.
2. `getUser(...)` gibt eine Entity zurück und koppelt API oder Aufrufer an das Persistenzmodell.
3. `RuntimeException` ist kein fachlich unterscheidbarer Fehler.
4. Registrierung, Passwortlogik, E-Mail, Billing und Admin-Notification liegen in einer Methode.
5. Externe Nebeneffekte laufen innerhalb derselben Service-Methode und potenziell innerhalb derselben Transaktion.
6. Wenn E-Mail oder Billing fehlschlagen, ist unklar, ob die Benutzeranlage zurückgerollt werden soll.
7. Wenn die Transaktion zurückrollt, könnte eine E-Mail bereits versendet worden sein.
8. `getAllUsersWithOrders()` lädt alle Nutzer ohne Pagination und provoziert N+1-Queries durch das Lazy Loading der Orders.
9. `findById` ohne Tenant-Filter ist in mandantenfähigen Systemen ein direktes Tenant-Isolation-Risiko.
10. `existsByEmail` ohne Tenant-Filter prüft global, nicht innerhalb des Mandanten.
11. Die Methode ist schwer isoliert zu testen, weil zu viele Verantwortlichkeiten vermischt sind.
12. Es ist unklar, wo Autorisierung, Tenant-Grenze und Datenschutz geprüft werden.

---

## 9. Gute Anwendung: fokussierte Serviceschicht

### 9.1 Command und Response trennen

```java
public record RegisterUserCommand(
        String tenantId,
        String name,
        String email,
        String password
) {}

public record UserCreatedResponse(
        Long userId,
        String name,
        String email,
        Instant registeredAt
) {}
```

Für produktiven Spring-Code sollten Command-Objekte zusätzlich Bean-Validation-Annotationen verwenden:

```java
public record RegisterUserCommand(
        @NotBlank String tenantId,
        @NotBlank @Size(max = 100) String name,
        @Email @NotBlank @Size(max = 254) String email,
        @Size(min = 12, max = 128) String password
) {}
```

Die genaue Semantik dieser Annotationen auf Records wird in Sektion 13.3 behandelt. Wichtig vorab: das `tenantId` aus dem Request-DTO darf in SaaS-Systemen nicht blind vertraut werden. Der Service muss den Tenant aus dem authentifizierten Sicherheitskontext ableiten oder gegen diesen validieren (Sektion 14.6).

### 9.2 Sprechende Exception-Hierarchie

Ein Java `record` kann nicht von `RuntimeException` erben, weil Records implizit von `java.lang.Record` erben. Fachliche Exceptions sind deshalb keine Records.

```java
public sealed abstract class DomainException extends RuntimeException
        permits UserNotFoundException, EmailAlreadyExistsException, InvalidCommandException {

    protected DomainException(String message) {
        super(message);
    }
}

public final class UserNotFoundException extends DomainException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
        this.userId = userId;
    }

    public Long userId() {
        return userId;
    }
}

public final class EmailAlreadyExistsException extends DomainException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("Email already registered");
        this.email = email;
    }

    public String email() {
        return email;
    }
}

public final class InvalidCommandException extends DomainException {

    public InvalidCommandException(String message) {
        super(message);
    }
}
```

Die Message von `EmailAlreadyExistsException` enthält bewusst nicht die vollständige E-Mail-Adresse. Je nach Kontext kann bereits eine E-Mail-Adresse personenbezogen und log-sensitiv sein.

**Wichtig:** Diese Exception ist für **interne Use Cases und Admin-Tools** geeignet — also für Aufrufer, denen die Existenz eines Accounts ohnehin bekannt sein darf. Für **öffentliche Registrierungs-Endpoints** ist sie ungeeignet, weil sie über das resultierende HTTP-409-Mapping User Enumeration ermöglicht. Die korrekte Behandlung in öffentlichen Endpoints ist in Sektion 14.5 beschrieben.

### 9.3 Fokussierter Application Service

```java
@Service
@Validated
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantContext tenantContext;
    private final TenantAccessPolicy tenantAccessPolicy;

    public UserRegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            TenantContext tenantContext,
            TenantAccessPolicy tenantAccessPolicy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.tenantContext = tenantContext;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    @Transactional
    public UserCreatedResponse register(@Valid RegisterUserCommand command) {
        var tenantId = tenantContext.currentTenantId();
        tenantAccessPolicy.assertCurrentUserMayCreateUserFor(tenantId);

        if (userRepository.existsByTenantIdAndEmail(tenantId, command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        var user = UserEntity.register(
                tenantId,
                command.name(),
                command.email(),
                passwordEncoder.encode(command.password())
        );

        var saved = userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                saved.getTenantId(),
                saved.getId(),
                saved.getEmail()
        ));

        return new UserCreatedResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getCreatedAt()
        );
    }
}
```

Warum ist das besser?

1. Der Service hat einen klaren Use Case: Registrierung.
2. Abhängigkeiten sind explizit und testbar.
3. Eingabe und Ausgabe sind dedizierte DTOs.
4. Die Transaktionsgrenze liegt auf der öffentlichen Use-Case-Methode.
5. Tenant-ID wird aus dem authentifizierten Kontext geholt, nicht aus dem Command.
6. Existenzprüfung erfolgt mit Tenant-Filter (`existsByTenantIdAndEmail`).
7. Tenant-Autorisierung wird in der Serviceschicht geprüft.
8. E-Mail und Billing werden nicht direkt in der Kernmethode ausgeführt.
9. Die Methode gibt keine Entity zurück.
10. Fachliche Fehler sind unterscheidbar.
11. Unit-Tests können Repository, Encoder, Event Publisher, TenantContext und Policy mocken.

### 9.4 UserEntity.register und JPA-Lifecycle

Die statische Factory-Methode `UserEntity.register(...)` ist gegenüber `new UserEntity(); user.setX();` zu bevorzugen, weil sie Invarianten an einem einzigen Ort durchsetzt. Sie ist aber **nicht** der Lifecycle-Konstruktor, den JPA verwendet.

```java
@Entity
@Table(name = "users", uniqueConstraints =
    @UniqueConstraint(columnNames = {"tenant_id", "email"}))
public class UserEntity {

    @Id @GeneratedValue
    private Long id;

    @Version                                     // ✅ Optimistic Locking
    private Long version;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Instant createdAt;

    // ✅ JPA-Pflicht-Konstruktor: package-private, nur für Hibernate
    protected UserEntity() {}

    // ✅ Domain-Factory: erzwingt Invarianten, verwendet von Service-Code
    public static UserEntity register(
            String tenantId,
            String name,
            String email,
            String passwordHash) {
        var user = new UserEntity();
        user.tenantId = requireNonBlank(tenantId, "tenantId");
        user.name = requireNonBlank(name, "name");
        user.email = requireValidEmail(email);
        user.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        user.createdAt = Instant.now();
        return user;
    }

    // Getter ... (kein Setter für invariante Felder)
}
```

Drei Lifecycle-Punkte, die in jedem Service-Layer-Code implizit gelten:

1. **JPA verwendet den protected/package-private no-arg-Konstruktor**, nicht die Factory-Methode. Invarianten in der Factory greifen nicht beim Laden aus der Datenbank — Daten in der DB werden als gültig vorausgesetzt.
2. **Die `id` ist erst nach `userRepository.save(user)` befüllt**, weil `@GeneratedValue` zur Insert-Zeit greift. Davor ist `user.getId()` `null`.
3. **Innerhalb der `@Transactional`-Methode ist die Entity managed.** Setter-Aufrufe persistieren automatisch beim Flush. `save()` ist nur beim Neuanlegen oder bei detached Entities nötig (Sektion 11.7).

---

## 10. Event-Verarbeitung und Nebeneffekte

### 10.1 Normale Events sind keine robuste Integration

Spring Application Events sind in-process Events. Sie sind hilfreich zur Entkopplung innerhalb einer Anwendung. Sie sind aber keine dauerhafte Queue, kein Message Broker und kein Garant für externe Zustellung.

Deshalb gilt:

1. Für einfache interne Nebeneffekte sind Spring Events geeignet.
2. Für E-Mail nach erfolgreicher Transaktion kann `@TransactionalEventListener` sinnvoll sein.
3. Für kritische externe Integrationen ist ein Outbox-Muster oder eine robuste Messaging-Lösung erforderlich.
4. Ein Event darf nicht verschleiern, dass eine fachliche Pflichtaktion zuverlässig erfolgen muss.

### 10.2 Event nach erfolgreichem Commit

```java
@Component
public class UserRegistrationEventHandler {

    private final EmailService emailService;
    private final BillingService billingService;

    public UserRegistrationEventHandler(
            EmailService emailService,
            BillingService billingService) {
        this.emailService = emailService;
        this.billingService = billingService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        emailService.sendWelcomeEmail(event.email());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createTrialSubscription(UserRegisteredEvent event) {
        billingService.createFreeTrialSubscription(event.tenantId(), event.userId());
    }
}
```

`@TransactionalEventListener` bindet Listener an den Transaktionsverlauf. Standardmäßig wird ein solcher Listener nach Commit ausgeführt. Das ist wichtig, wenn eine E-Mail erst versendet werden soll, nachdem die Datenbankänderung erfolgreich bestätigt wurde.

**Grenzen dieses Patterns:** Wenn der Prozess zwischen Commit und Listener-Ausführung abstürzt — JVM-Crash, Pod-Restart, OOM-Kill — ist das Event verloren. Für unkritische Nebeneffekte (Welcome-E-Mail) ist das akzeptabel. Für kritische Integrationen (Billing, externe Systeme mit Konsistenz-Garantie) reicht es nicht.

### 10.3 Outbox-Muster für kritische Nebeneffekte

Wenn die Anlage eines Kunden zwingend ein Billing-System informieren muss, darf diese Information nicht verloren gehen. Das Outbox-Muster speichert das Integrationsevent **in derselben Datenbank-Transaktion** wie die fachliche Änderung und liefert es separat an das externe System aus.

```java
@Transactional
public UserCreatedResponse register(@Valid RegisterUserCommand command) {
    var tenantId = tenantContext.currentTenantId();

    if (userRepository.existsByTenantIdAndEmail(tenantId, command.email())) {
        throw new EmailAlreadyExistsException(command.email());
    }

    var user = UserEntity.register(
            tenantId, command.name(), command.email(),
            passwordEncoder.encode(command.password())
    );
    var saved = userRepository.save(user);

    // ✅ Outbox-Eintrag in derselben Transaktion, derselben Datenquelle
    outboxRepository.save(OutboxEvent.billingTrialRequested(
            saved.getTenantId(),
            saved.getId(),
            saved.getEmail(),
            UUID.randomUUID().toString()                  // Idempotency-Key
    ));

    return mapper.toCreatedResponse(saved);
}
```

**Kritische Voraussetzungen, ohne die Outbox keine Outbox ist:**

1. **Outbox-Tabelle in derselben Datenquelle** wie die Domain-Tabellen. Eine separate Audit-DB oder Reporting-DB bricht die Atomicity. Selbst mit XA/JTA ist 2-Phase-Commit in modernen Cloud-Stacks selten verfügbar und meist nicht aktiviert.
2. **Outbox-Eintrag im selben `@Transactional`-Block.** Wenn die Domain-Transaktion zurückrollt, rollt auch der Outbox-Eintrag zurück. Das ist die ganze Garantie.
3. **Idempotency-Key** im Event. Externe Systeme können denselben Eintrag mehrfach erhalten — der Konsument muss damit umgehen können.

### 10.4 Outbox-Polling-Worker

Ein separater Worker liest die Outbox, ruft das externe System auf, markiert Events als verarbeitet und behandelt Retry, Dead Letter und Idempotenz.

```java
@Component
public class OutboxPollingWorker {

    private final OutboxRepository outboxRepository;
    private final BillingClient billingClient;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${outbox.poll-interval:1000}")
    public void pollAndProcess() {
        // ✅ SELECT FOR UPDATE SKIP LOCKED — mehrere Worker-Instanzen sind sicher
        var batch = transactionTemplate.execute(status ->
            outboxRepository.lockNextBatchForProcessing(50)
        );

        for (var event : batch) {
            try {
                billingClient.process(event);              // idempotent über Key
                transactionTemplate.executeWithoutResult(status ->
                    outboxRepository.markAsProcessed(event.getId())
                );
            } catch (TransientException e) {
                transactionTemplate.executeWithoutResult(status ->
                    outboxRepository.scheduleRetry(event.getId(),
                            backoff(event.getRetryCount()))
                );
            } catch (PermanentException e) {
                transactionTemplate.executeWithoutResult(status ->
                    outboxRepository.markAsDeadLetter(event.getId(), e.getMessage())
                );
            }
        }
    }
}
```

```java
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
            select * from outbox_event
            where status = 'PENDING'
              and (next_attempt_at is null or next_attempt_at <= now())
            order by created_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatchForProcessing(@Param("batchSize") int batchSize);
}
```

**Pflicht-Eigenschaften eines produktionsreifen Outbox-Workers:**

| Eigenschaft | Warum |
| --- | --- |
| `FOR UPDATE SKIP LOCKED` | Mehrere Worker-Instanzen können parallel pollen, ohne sich gegenseitig zu blockieren. PostgreSQL ab 9.5, MySQL ab 8.0. |
| Idempotency-Key im Event | Konsument kann doppelte Verarbeitung erkennen. |
| Backoff bei Retry | Exponentiell: 1s → 5s → 30s → 5min → 30min. Begrenzt auf Max-Versuche (z. B. 10). |
| Dead-Letter-Status | Events, die endgültig fehlschlagen, werden für manuelle Analyse markiert. |
| Monitoring auf Lag | Wie alt ist der älteste `PENDING`-Eintrag? Alarm bei Schwellwert. |
| Graceful Shutdown | Worker beendet aktuellen Batch sauber, kein Abbrechen mitten in einer Transaktion. |

### 10.5 Wann Event, wann Outbox

| Szenario | Empfehlung |
| --- | --- |
| Welcome-E-Mail nach Registrierung | `@TransactionalEventListener(AFTER_COMMIT)` — Verlust akzeptabel |
| In-App-Benachrichtigung an Admins | `@TransactionalEventListener(AFTER_COMMIT)` |
| Cache-Invalidierung nach Änderung | `@TransactionalEventListener(AFTER_COMMIT)` |
| Billing-Trial-Anlage bei externem Provider | Outbox — Verlust nicht akzeptabel |
| Webhook an Kunden-System | Outbox |
| Aktualisierung in einem Search-Index | Outbox bei Konsistenz-Anspruch, sonst Event |
| Event an Kafka/Pulsar/SQS | Outbox als Sicherheitsnetz, wenn Kafka-Connect-Outbox nicht eingesetzt wird |

---

## 11. Transaktionsregeln

### 11.1 Method-Level statt pauschaler Klassenannotation

Pauschales `@Transactional` auf Klassenebene ist bequem, aber oft zu grob. Es verschleiert, welche Methoden wirklich eine Transaktion benötigen.

Bevorzugt:

```java
@Service
public class OrderService {

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> findOrderSummaries(String tenantId, Long userId) {
        return orderRepository.findSummariesByTenantIdAndUserId(tenantId, userId);
    }

    @Transactional
    public OrderCreatedResponse createOrder(CreateOrderCommand command) {
        // schreibender Use Case
    }

    public BigDecimal calculateTaxPreview(BigDecimal amount, String countryCode) {
        return taxCalculator.calculate(amount, countryCode);
    }
}
```

### 11.2 `readOnly = true` richtig verstehen

`@Transactional(readOnly = true)` ist kein Security-Mechanismus und kein Ersatz für Datenbankrechte. Es drückt die Absicht einer lesenden Transaktion aus und kann je nach Transaktionsmanager, JDBC-Treiber, JPA-Provider und Datenbank Optimierungen ermöglichen.

Richtig:

1. Für lesende Use Cases verwenden, wenn Datenbankzugriff stattfindet.
2. Nicht als Garantie verstehen, dass niemand schreiben kann.
3. Nicht verwenden, wenn innerhalb der Methode persistente Änderungen erwartet werden.
4. Nicht verwenden, um fehlende fachliche Trennung zu kaschieren.

### 11.3 `@Transactional` nur auf öffentlichen Methoden

Die robuste Regel lautet: **`@Transactional` ausschließlich auf öffentlichen Methoden**. Auch wenn class-based proxies (CGLIB) seit Spring Framework 6.0 protected oder package-visible Methoden technisch unterstützen, ist diese Möglichkeit für Reviewer nicht zuverlässig erkennbar — JDK-Dynamic-Proxies (Interface-basiert) unterstützen nur public.

Falsch:

```java
@Service
public class PaymentService {

    public void pay(PaymentCommand command) {
        validate(command);
        persistPayment(command);
    }

    @Transactional
    private void persistPayment(PaymentCommand command) {
        // wird bei proxybasierter AOP nicht intercepted
    }
}
```

Richtig:

```java
@Service
public class PaymentService {

    @Transactional
    public void pay(PaymentCommand command) {
        validate(command);
        persistPayment(command);
    }

    private void persistPayment(PaymentCommand command) {
        // Teil derselben öffentlichen Transaktionsgrenze
    }
}
```

Oder bei bewusst getrennter Transaktion:

```java
@Service
public class PaymentPersistenceService {

    @Transactional
    public void persistPayment(PaymentCommand command) {
        // eigene öffentliche transaktionale Service-Methode
    }
}
```

### 11.4 Self-Invocation vermeiden

Falsch:

```java
@Service
public class InvoiceService {

    public void createInvoice(CreateInvoiceCommand command) {
        validate(command);
        this.persistInvoice(command);   // umgeht den Spring-Proxy
    }

    @Transactional
    public void persistInvoice(CreateInvoiceCommand command) {
        // Aufruf über this umgeht typischerweise den Spring-Proxy
    }
}
```

Richtig ist ein externer Aufruf über eine andere Spring-Bean oder eine sauber gesetzte Transaktionsgrenze auf der äußeren Methode.

### 11.5 Externe Calls nicht innerhalb langer Transaktionen

Falsch:

```java
@Transactional
public void checkout(CheckoutCommand command) {
    var order = orderRepository.save(createOrder(command));
    paymentClient.charge(command.paymentDetails());        // Netzwerk-Call!
    shippingClient.createShipment(order.getId());          // Netzwerk-Call!
    order.markCompleted();
}
```

Warum kritisch?

1. Die Datenbanktransaktion bleibt während externer Netzwerkaufrufe offen.
2. Locks können länger gehalten werden.
3. Timeouts werden schwerer kontrollierbar.
4. Fehlersemantik wird unklar: Was passiert, wenn Zahlung erfolgreich, aber DB-Commit fehlschlägt?
5. Wiederholungen können doppelte externe Aktionen erzeugen.
6. Connection-Pool wird über Netzwerk-Latenzen blockiert.

Besser:

1. Zustand in der Datenbank speichern.
2. Outbox-Event erzeugen (Sektion 10.3).
3. Externe Verarbeitung asynchron und idempotent ausführen.
4. Statusübergänge explizit modellieren.

### 11.6 Optimistic Locking mit @Version

Aggregate-Roots, die parallel modifiziert werden können, müssen über Optimistic Locking gegen verlorene Updates abgesichert werden.

```java
@Entity
public class OrderEntity {

    @Id @GeneratedValue
    private Long id;

    @Version                                     // ✅ Pflicht für Aggregate-Roots
    private Long version;

    private OrderStatus status;
    private BigDecimal total;

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalOrderStateException(
                "Order in status " + status + " cannot be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

```java
@Service
public class OrderConfirmationService {

    @Transactional
    public OrderResponse confirm(ConfirmOrderCommand command) {
        var tenantId = tenantContext.currentTenantId();
        var order = orderRepository.findByIdAndTenantId(command.orderId(), tenantId)
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        order.confirm();                          // setzt Status, version wird inkrementiert

        // Beim Commit: optimistic check; bei Konflikt → OptimisticLockException
        return mapper.toResponse(order);
    }
}
```

**Was bei Konflikt passiert:** Wenn zwei Transaktionen denselben `Order` mit Version 5 laden und beide `confirm()` aufrufen, schreibt die erste Version 6. Die zweite Transaktion versucht ebenfalls Version 5 → Version 6 zu schreiben, scheitert mit `ObjectOptimisticLockingFailureException` (Spring) bzw. `OptimisticLockException` (JPA), und die Transaktion rollt zurück.

**API-Mapping:** Optimistic-Lock-Konflikte sollen als HTTP 409 Conflict gemappt werden:

```java
@ExceptionHandler({ObjectOptimisticLockingFailureException.class,
                   OptimisticLockException.class})
ProblemDetail handleConflict(Exception exception) {
    var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Concurrent modification");
    problem.setDetail("The resource was modified by another request. Please retry.");
    return problem;
}
```

Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) ist nur in seltenen Fällen die richtige Antwort, etwa bei sehr kurzen kritischen Abschnitten mit hoher Konfliktwahrscheinlichkeit. Optimistic Locking ist der Default.

### 11.7 JPA-Entity-Lifecycle (managed vs. detached)

Innerhalb einer aktiven `@Transactional`-Methode sind aus dem Repository geladene Entities **managed**. Änderungen an ihren Feldern werden beim Flush automatisch persistiert — `save()` ist redundant.

```java
@Transactional
public OrderResponse updateShippingAddress(UpdateAddressCommand command) {
    var tenantId = tenantContext.currentTenantId();
    var order = orderRepository.findByIdAndTenantId(command.orderId(), tenantId)
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.updateShippingAddress(toAddress(command));   // ✅ kein save() nötig
    return mapper.toResponse(order);                   // automatisch persistiert beim Commit
}
```

Außerhalb der Transaktion oder nach Serialisierung sind Entities **detached**. Änderungen an detached Entities werden nicht automatisch persistiert — es braucht explizit `merge()` oder einen erneuten Repository-Save:

```java
// ❌ Schlecht: außerhalb der Transaktion geändert, nicht persistiert
public OrderResponse updateOutsideTransaction(...) {
    var order = orderRepository.findById(id).orElseThrow();
    order.updateShippingAddress(...);                  // detached → kein Effekt
    return mapper.toResponse(order);
}

// ✅ Richtig: explizites save oder Methode innerhalb @Transactional
@Transactional
public OrderResponse updateInsideTransaction(...) {
    var order = orderRepository.findByIdAndTenantId(id, tenantId).orElseThrow();
    order.updateShippingAddress(...);
    return mapper.toResponse(order);                   // automatisch beim Commit
}
```

### 11.8 Bulk-Updates und Cache-Konsistenz

JPA-`@Modifying`-Queries umgehen den First-Level-Cache. Nach einem Bulk-Update ist der Cache stale.

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Modifying
    @Query("update OrderEntity o set o.status = :newStatus " +
           "where o.tenantId = :tenantId and o.status = :oldStatus")
    int bulkUpdateStatus(
            @Param("tenantId") String tenantId,
            @Param("oldStatus") OrderStatus oldStatus,
            @Param("newStatus") OrderStatus newStatus);
}
```

```java
@Transactional
public int closeAllPendingOrders(String reason) {
    var tenantId = tenantContext.currentTenantId();
    var count = orderRepository.bulkUpdateStatus(
            tenantId, OrderStatus.PENDING, OrderStatus.CLOSED);

    entityManager.clear();                       // ✅ Cache leeren nach Bulk-Update
    return count;
}
```

Ohne `entityManager.clear()` (oder `@Modifying(clearAutomatically = true)`) zeigen nachfolgende Queries innerhalb derselben Transaktion möglicherweise alte Daten.

---

## 12. DTO-, Entity- und Mapping-Regeln

### 12.1 Entities nicht als API-Vertrag verwenden

Falsch:

```java
@GetMapping("/users/{id}")
public UserEntity getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

Warum ist das gefährlich?

1. Interne Datenbankstruktur wird öffentlicher Vertrag.
2. Neue Entity-Felder können versehentlich über die API sichtbar werden.
3. Lazy Loading kann während Serialisierung Datenbankzugriffe auslösen.
4. Bidirektionale Beziehungen können rekursive Serialisierung erzeugen.
5. Schreibbare Felder können zu Mass Assignment führen.
6. Berechtigungen auf Objekteigenschaften werden schwerer kontrollierbar.

Richtig:

```java
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return userQueryService.getUser(id);
}
```

```java
public record UserResponse(
        Long id,
        String name,
        String email,
        Instant registeredAt
) {}
```

### 12.2 Command und Response trennen

Falsch:

```java
public record UserDto(
        Long id,
        String name,
        String email,
        String password,
        boolean admin,
        Instant createdAt
) {}
```

Dieses DTO ist für alles und dadurch für nichts präzise. Es kann zu Overposting, ungewollter Datenexposition und unklarer Validierung führen.

Richtig:

```java
public record RegisterUserCommand(
        String name,
        String email,
        String password
) {}

public record UserResponse(
        Long id,
        String name,
        String email,
        Instant registeredAt
) {}

public record UpdateUserProfileCommand(
        String displayName,
        String preferredLanguage
) {}
```

### 12.3 Projections für lesende Use Cases nutzen

Wenn ein Use Case nur Zusammenfassungen braucht, soll nicht das vollständige Aggregat geladen werden.

```java
public record OrderSummaryDto(
        Long orderId,
        BigDecimal total,
        Instant createdAt,
        String status
) {}
```

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("""
            select new com.example.order.application.OrderSummaryDto(
                o.id,
                o.total,
                o.createdAt,
                o.status
            )
            from OrderEntity o
            where o.tenantId = :tenantId
              and o.userId = :userId
            order by o.createdAt desc
            """)
    Page<OrderSummaryDto> findSummariesByTenantIdAndUserId(
            @Param("tenantId") String tenantId,
            @Param("userId") Long userId,
            Pageable pageable);
}
```

Projections reduzieren unnötige Datenlast und senken das Risiko, versehentlich sensitive Entity-Felder nach außen zu tragen.

### 12.4 Tenant-bewusste Repository-Queries

In mandantenfähigen Systemen ist `findById(id)` ohne Tenant-Filter ein Tenant-Isolation-Risiko. Ein Angreifer (oder schlicht ein Bug) kann eine Resource eines anderen Mandanten abrufen.

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // ✅ Default-Pattern für SaaS: immer mit Tenant-Filter
    Optional<OrderEntity> findByIdAndTenantId(Long id, String tenantId);

    Page<OrderEntity> findAllByTenantId(String tenantId, Pageable pageable);

    boolean existsByTenantIdAndEmail(String tenantId, String email);

    @Query("select o from OrderEntity o " +
           "where o.tenantId = :tenantId and o.status = :status " +
           "order by o.createdAt desc")
    List<OrderEntity> findActiveOrdersForTenant(
            @Param("tenantId") String tenantId,
            @Param("status") OrderStatus status);
}
```

```java
// ❌ Schlecht: kein Tenant-Filter
@Transactional(readOnly = true)
public OrderResponse getOrder(Long orderId) {
    var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    return mapper.toResponse(order);
}

// ✅ Richtig: Tenant aus Kontext, Filter im Repository
@Transactional(readOnly = true)
public OrderResponse getOrder(Long orderId) {
    var tenantId = tenantContext.currentTenantId();
    var order = orderRepository.findByIdAndTenantId(orderId, tenantId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    return mapper.toResponse(order);
}
```

Alternativ kann ein Hibernate-Filter mit `@FilterDef` und `@Filter` automatisch den Tenant-Filter applizieren — dann ist die Disziplin im Repository-Code geringer, aber die Filter-Aktivierung muss zentral sichergestellt werden (Aspect, Interceptor, oder `EntityManager`-Hook). Für Onboarding-Klarheit ist explizites `findByIdAndTenantId` der robustere Default.

### 12.5 Pagination als Default für Listen

Unbegrenzte Result-Sets sind in Production immer ein Risiko. Listen-Endpoints müssen Pagination unterstützen.

```java
@Transactional(readOnly = true)
public Page<OrderSummaryDto> listOrders(ListOrdersQuery query, Pageable pageable) {
    var tenantId = tenantContext.currentTenantId();
    return orderRepository.findSummariesByTenantId(tenantId, pageable);
}
```

```java
@GetMapping("/orders")
public PagedModel<OrderSummaryDto> listOrders(
        @PageableDefault(size = 20, sort = "createdAt",
                         direction = Sort.Direction.DESC) Pageable pageable) {
    return new PagedModel<>(orderQueryService.listOrders(query, pageable));
}
```

`Page<T>` führt eine zusätzliche `count`-Query aus. Wenn die Gesamtanzahl nicht benötigt wird, ist `Slice<T>` (kein Count) die effizientere Wahl. Bei sehr großen Tabellen ist Cursor-basierte Pagination (`where id > lastId`) gegenüber Offset-Pagination zu bevorzugen, weil OFFSET in PostgreSQL und MySQL bei großen Werten ineffizient wird.

### 12.6 Soft-Delete-Pattern

In SaaS-Systemen mit Audit- oder DSGVO-Anforderungen ist Soft-Delete der Default. Daten werden nicht physisch gelöscht, sondern markiert.

```java
@Entity
@SQLDelete(sql = "update orders set deleted_at = now() where id = ?")
@Where(clause = "deleted_at is null")            // ✅ alle Queries filtern automatisch
public class OrderEntity {

    @Id @GeneratedValue
    private Long id;

    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

**Achtung bei Soft-Delete:**

1. `@Where` betrifft alle Queries, auch JPQL-Queries. Das ist meistens gewollt, aber muss bekannt sein.
2. Eindeutigkeits-Constraints müssen `deleted_at` einbeziehen, sonst sind Re-Registrierungen unmöglich. Beispiel: `unique (tenant_id, email, deleted_at)` oder partial index `where deleted_at is null`.
3. Berichte und Audit-Queries müssen explizit den `@Where`-Filter umgehen können.
4. DSGVO-Pflichtlöschungen müssen physisches Löschen unterstützen — Soft-Delete ist kein Ersatz.

---

## 13. Validierung und Fehlersemantik

### 13.1 Validierung am Rand und im Use Case

Request-Validierung im Controller ist notwendig, aber nicht ausreichend. Services können auch aus Jobs, Events, Tests, internen APIs oder anderen Services aufgerufen werden. Deshalb dürfen wichtige fachliche Invarianten nicht ausschließlich im Controller liegen.

Controller-Beispiel:

```java
@PostMapping("/users")
public ResponseEntity<UserCreatedResponse> register(
        @Valid @RequestBody RegisterUserRequest request) {
    var command = new RegisterUserCommand(
            request.name(),
            request.email(),
            request.password()
    );
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(userRegistrationService.register(command));
}
```

Service-Beispiel:

```java
@Service
@Validated
public class UserRegistrationService {

    @Transactional
    public UserCreatedResponse register(@Valid RegisterUserCommand command) {
        // zusätzlich fachliche Prüfung und Autorisierung
    }
}
```

Spring unterstützt Bean Validation in MVC und methodenbezogene Validierung über `@Validated`. Trotzdem ist Bean Validation nur ein Teil der Lösung. Fachliche Regeln wie „E-Mail im Tenant eindeutig", „Nutzer darf diesen Tenant verwalten" oder „Statusübergang ist erlaubt" gehören in Service, Domain Policy oder Domain-Modell.

### 13.2 Fehler in stabile API-Antworten übersetzen

Fachliche Exceptions sollen nicht ungefiltert als Stacktrace oder generischer 500-Fehler beim Client landen.

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("User not found");
        problem.setDetail("The requested user does not exist or is not accessible.");
        return problem;
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class,
                       OptimisticLockException.class})
    ProblemDetail handleConcurrentModification(Exception exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Concurrent modification");
        problem.setDetail("The resource was modified by another request. Please retry.");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleBeanValidation(MethodArgumentNotValidException exception) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setProperty("violations",
                exception.getBindingResult().getFieldErrors().stream()
                        .map(this::toViolation).toList());
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        // wird bei @Validated-Service-Methoden geworfen
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        return problem;
    }
}
```

**Wichtig:** Fehlermeldungen sollen hilfreich, aber nicht informationspreisgebend sein. Bei Login, Registrierung, Account Recovery und sicherheitskritischen APIs muss genau geprüft werden, ob eine Antwort Benutzerexistenz, Tenant-Strukturen oder interne Regeln offenlegt (Sektion 14.5).

### 13.3 Bean Validation auf Records — die Fallstricke

Bean-Validation-Annotationen auf Record-Komponenten funktionieren in Spring MVC mit Hibernate Validator 8 zuverlässig, **aber das Verhalten ist abhängig von Hibernate-Validator-Version, Spring-Version, Annotation-Targets und dem Zusammenspiel von `@Validated` und `@Valid`**. Drei häufige Fallstricke:

#### Fallstrick 1: Annotation-Targets

```java
// ✅ Funktioniert: Hibernate-Validator-Constraints haben implizit RECORD_COMPONENT
public record RegisterUserCommand(
        @NotBlank String tenantId,
        @Email @NotBlank String email,
        @Size(min = 12, max = 128) String password
) {}

// ⚠️ Custom Constraint Annotations brauchen explizit RECORD_COMPONENT
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD,
         ElementType.RECORD_COMPONENT})            // ✅ Pflicht für Records
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Password too weak";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### Fallstrick 2: `@Validated` auf Klasse, `@Valid` auf Parameter

`@Validated` auf der Service-Klasse aktiviert den `MethodValidationPostProcessor`. Damit eine Service-Methode auch wirklich validiert, muss **zusätzlich** `@Valid` am Parameter stehen.

```java
@Service
@Validated                                       // ✅ aktiviert Method Validation
public class UserRegistrationService {

    @Transactional
    public UserCreatedResponse register(@Valid RegisterUserCommand command) {
        // ✅ wirft ConstraintViolationException bei Verletzung
    }

    @Transactional
    public void updateProfile(UpdateProfileCommand command) {
        // ❌ kein @Valid → keine automatische Validierung
    }
}
```

Beim Verstoß wird `ConstraintViolationException` geworfen (nicht `MethodArgumentNotValidException` wie im Controller). Das `@RestControllerAdvice` muss beide Typen behandeln (siehe 13.2).

#### Fallstrick 3: Kaskadierung bei nested Records

`@Valid` auf einem Feld erzwingt Kaskadierung der Validierung in das verschachtelte Objekt. Ohne `@Valid` werden Constraints im inneren Record nicht geprüft.

```java
public record CreateOrderCommand(
        @NotBlank String tenantId,
        @Valid @NotNull ShippingAddress shippingAddress,    // ✅ @Valid Pflicht für Kaskade
        @NotEmpty List<@Valid OrderItemCommand> items       // ✅ @Valid auch in Listen
) {}

public record ShippingAddress(
        @NotBlank @Size(max = 200) String street,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Pattern(regexp = "[A-Z]{2}") String country
) {}
```

#### Pflicht-Test: ConstraintValidation gegen die eigene Library-Version

```java
class RegisterUserCommandValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankTenantId() {
        var command = new RegisterUserCommand(
                "", "Max", "max@example.org", "password-12345");
        var violations = validator.validate(command);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("tenantId");
    }
}
```

Diese Tests sind die einzige zuverlässige Garantie, dass die Bean-Validation-Annotationen mit der aktuellen Hibernate-Validator-Version greifen. Ohne sie ist „funktioniert" nur eine Annahme.

---

## 14. Security- und SaaS-Aspekte

### 14.1 Serviceschicht als Sicherheitsgrenze

Die Serviceschicht ist keine reine Komfortschicht. Sie ist eine wichtige Sicherheitsgrenze. Controller können leicht umgangen werden, wenn derselbe Service später aus Batch-Jobs, Events, internen APIs, Tests oder Admin-Werkzeugen aufgerufen wird.

Deshalb gilt:

1. Autorisierung darf nicht ausschließlich im Controller liegen.
2. Tenant-Isolation muss in Service und Repository abgesichert werden.
3. Request-Felder wie `tenantId`, `userId`, `role`, `admin`, `price`, `discount`, `status` dürfen nicht blind vertraut werden.
4. Service-Methoden müssen klar trennen, welche Werte vom Client kommen und welche aus dem vertrauenswürdigen Kontext stammen.
5. Entity-Exposition ist ein Risiko für übermäßige Datenfreigabe.
6. Generische Update-DTOs sind ein Risiko für Mass Assignment.

### 14.2 Broken Object Property Level Authorization vermeiden

OWASP API Security 2023 beschreibt Broken Object Property Level Authorization als Risiko, bei dem APIs Eigenschaften eines Objekts offenlegen oder ändern lassen, obwohl der Nutzer darauf keinen Zugriff haben sollte.

Schlechtes Beispiel:

```java
public record UpdateUserRequest(
        String name,
        String email,
        boolean admin,
        BigDecimal creditLimit,
        String tenantId
) {}
```

Wenn ein normaler Nutzer dieses DTO senden kann, besteht hohes Risiko. Er könnte versuchen, `admin`, `creditLimit` oder `tenantId` zu verändern.

Besser:

```java
public record UpdateOwnProfileCommand(
        String displayName,
        String preferredLanguage
) {}
```

Administrative Use Cases erhalten eigene Commands, eigene Services, eigene Berechtigungsprüfung und eigene Auditierung.

### 14.3 Tenant-Kontext nicht aus untrusted input ableiten

Falsch:

```java
@Transactional
public OrderCreatedResponse createOrder(CreateOrderCommand command) {
    // command.tenantId() kommt direkt aus dem Request
    return orderRepository.saveForTenant(command.tenantId(), command.toEntity());
}
```

Besser:

```java
@Transactional
public OrderCreatedResponse createOrder(CreateOrderCommand command) {
    var tenantId = tenantContext.currentTenantId();
    tenantAccessPolicy.assertMayCreateOrder(tenantId);

    var order = OrderEntity.create(
            tenantId,
            command.productId(),
            command.quantity()
    );

    return mapper.toCreatedResponse(orderRepository.save(order));
}
```

Der Tenant wird aus dem authentifizierten Kontext abgeleitet. Falls ein Request eine Tenant-ID enthält, muss sie gegen diesen Kontext geprüft werden. Die Implementierung des `TenantContext` ist in Sektion 14.6 beschrieben.

### 14.4 Logging in Services

Services dürfen fachliche Ereignisse loggen, aber keine sensitiven Payloads unkontrolliert ausgeben.

Nicht loggen:

1. Passwörter.
2. Tokens.
3. API Keys.
4. Session-IDs.
5. Vollständige Payment-Daten.
6. Vollständige personenbezogene Profile ohne Zweckbindung.
7. Komplette Request-DTOs aus Auth-, Identity-, Payment- oder Admin-Kontexten.
8. Cross-Tenant-Daten in gemeinsamen Log-Kontexten.

Besser:

```java
log.info("User registration completed: tenantId={}, userId={}", tenantId, userId);
```

Nicht:

```java
log.info("Register user command: {}", command);
```

Bei Records ist das besonders relevant, weil `toString()` automatisch alle Komponenten ausgeben kann. Records mit sensiblen Feldern sollten `toString()` überschreiben oder die Felder explizit ausnehmen.

### 14.5 User Enumeration in öffentlichen Endpoints vermeiden

Eine `EmailAlreadyExistsException` mit HTTP 409 in einem öffentlichen Registrierungs-Endpoint ist ein klassischer User-Enumeration-Fehler. Ein Angreifer kann durch unterschiedliche Antworten ableiten, welche E-Mail-Adressen registriert sind.

```java
// ❌ Schlecht: verrät Existenz
@PostMapping("/register")
public ResponseEntity<UserCreatedResponse> register(@Valid @RequestBody RegisterUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(userRegistrationService.register(toCommand(request)));
    // wirft EmailAlreadyExistsException → HTTP 409 → Existenz verraten
}
```

Stattdessen: in öffentlichen Endpoints **immer dieselbe Antwort** geben — ob die E-Mail neu ist oder bereits existiert. Im Hintergrund unterschiedliche E-Mails versenden:

```java
// ✅ Richtig: einheitliche Antwort für öffentliche Endpoints
@Service
public class PublicRegistrationService {

    @Transactional
    public void requestRegistration(RegisterRequestCommand command) {
        var existingUser = userRepository.findByEmail(command.email());

        if (existingUser.isEmpty()) {
            // Neuer Nutzer: Pending-Eintrag und Welcome-E-Mail mit Bestätigungs-Link
            var pending = pendingRegistrationRepository.save(
                    PendingRegistration.create(command));
            outboxRepository.save(
                    OutboxEvent.welcomeEmailRequested(pending.getToken(), command.email()));
        } else {
            // Vorhandener Nutzer: stille E-Mail mit Hinweis auf Login oder Reset
            outboxRepository.save(
                    OutboxEvent.alreadyRegisteredEmailRequested(command.email()));
        }
        // ✅ kein Exception-Pfad
    }
}

@PostMapping("/register")
public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
    publicRegistrationService.requestRegistration(toCommand(request));
    return ResponseEntity.accepted().build();   // ✅ immer dieselbe Antwort
}
```

Die `EmailAlreadyExistsException` aus Sektion 9.2 bleibt für **Admin-Tools, interne Use Cases und API-Calls von authentifizierten Tenants** geeignet — also überall dort, wo der Aufrufer das Recht hat, Existenz zu erfahren. Für öffentliche Endpoints ist sie zu vermeiden.

Dieselbe Disziplin gilt für:

1. Login-Fehler: niemals zwischen „Benutzer existiert nicht" und „Passwort falsch" unterscheiden.
2. Account Recovery: immer dieselbe Antwort, ob die E-Mail registriert ist oder nicht.
3. Eingabe von Tenant-Identifikatoren in öffentlichen Subdomain-Erkennungen.

### 14.6 Tenant-Context-Pattern

Der `TenantContext` ist die zentrale Abstraktion, durch die Services den aktuellen Mandanten erfahren — ohne ihn aus Request-Daten zu rekonstruieren.

```java
public interface TenantContext {
    String currentTenantId();
    Optional<String> currentTenantIdOptional();
}
```

Eine typische Implementierung in einem Spring-Security-basierten System:

```java
@Component
public class SecurityContextTenantContext implements TenantContext {

    @Override
    public String currentTenantId() {
        return currentTenantIdOptional()
                .orElseThrow(() -> new MissingTenantContextException());
    }

    @Override
    public Optional<String> currentTenantIdOptional() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(TenantAwarePrincipal.class::isInstance)
                .map(TenantAwarePrincipal.class::cast)
                .map(TenantAwarePrincipal::tenantId);
    }
}
```

**Wichtige Aspekte:**

| Aspekt | Empfehlung |
| --- | --- |
| Befüllung | Custom `JwtAuthenticationConverter` extrahiert den Tenant-Claim aus dem JWT und legt ein `TenantAwarePrincipal` in den `SecurityContext`. |
| ThreadLocal | `SecurityContextHolder` nutzt `ThreadLocal`. Funktioniert mit Virtual Threads, weil pro Virtual Thread ein eigener ThreadLocal existiert. |
| `@Async`-Propagation | Standardmäßig wird der `SecurityContext` nicht propagiert. Lösung: `DelegatingSecurityContextAsyncTaskExecutor`. |
| Reactive-Kontexte | In WebFlux: Tenant aus `ReactiveSecurityContextHolder` ableiten, nicht aus `SecurityContextHolder`. |
| Tests | In Tests `@WithMockUser` oder `TestSecurityContextHolder` verwenden, oder `TenantContext` direkt als Mock injizieren. |
| Batch-Jobs | Außerhalb eines authentifizierten Requests muss der Tenant explizit gesetzt werden (z. B. über einen `BatchTenantContext`-Decorator). |

```java
// Beispiel: Tenant-Setup in einem Job-Worker
public class TenantAwareJobRunner {

    public void runForTenant(String tenantId, Runnable job) {
        var auth = new SystemAuthentication(tenantId);
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            job.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
```

---

## 15. Async, Virtual Threads und Ressourcensteuerung

### 15.1 `@Async` ist kein Konsistenzmodell

`@Async` verschiebt Arbeit in einen anderen Thread. Es beantwortet aber nicht automatisch:

1. Was passiert bei Fehlern?
2. Wer retryt?
3. Wie wird Idempotenz sichergestellt?
4. Wie wird Backpressure umgesetzt?
5. Welche Tenant-Grenzen gelten?
6. Wie wird überwacht?
7. Wie wird bei Shutdown sauber beendet?
8. Wird der `SecurityContext` propagiert (Sektion 14.6)?

Deshalb darf `@Async` nicht als Ersatz für ein robustes Integrations- oder Jobmodell verwendet werden.

### 15.2 Virtual Threads richtig einordnen

Spring Boot kann mit Java 21 Virtual Threads aktivieren. Wenn `spring.threads.virtual.enabled=true` gesetzt ist, konfiguriert Spring Boot unter anderem einen `AsyncTaskExecutor`, der Virtual Threads nutzt.

Das bedeutet aber nicht, dass unbegrenzte Nebenläufigkeit fachlich richtig ist. Auch mit Virtual Threads bleiben Datenbankverbindungen, externe APIs, Rate Limits, CPU, Speicher und Tenant-Quotas begrenzte Ressourcen.

Regel:

1. Virtual Threads erleichtern blockierenden I/O-Code.
2. Virtual Threads ersetzen kein Ressourcenlimit.
3. Virtual Threads ersetzen kein Timeout.
4. Virtual Threads ersetzen keine Idempotenz.
5. Virtual Threads ersetzen kein Outbox-Muster.
6. Virtual Threads ersetzen keine fachliche Transaktionsmodellierung.

### 15.3 Bulkhead vs. verteiltes Rate-Limiting

Begrenzung paralleler Operationen ist zwei unterschiedliche Klassen von Problemen:

| Klasse | Reichweite | Werkzeug | Wofür |
| --- | --- | --- | --- |
| **Bulkhead** | lokal pro JVM | `Semaphore`, `ThreadPoolTaskExecutor` mit Queue, Resilience4j Bulkhead | Schutz der eigenen JVM vor Resource-Exhaustion |
| **Verteiltes Rate Limit** | global über alle Instanzen | Redis-Counter, Bucket4j-Hazelcast, Service-Mesh-Quota | Einhaltung externer API-Rate-Limits, Mandanten-Quoten |

Lokales Bulkhead-Beispiel:

```java
// ✅ Bulkhead: schützt die eigene JVM vor zu vielen parallelen externen Calls
private final Semaphore billingBulkhead = new Semaphore(20);

public void callBillingSafely(BillingCommand command) {
    boolean acquired;
    try {
        acquired = billingBulkhead.tryAcquire(2, TimeUnit.SECONDS);   // ✅ kurze Wartezeit
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RequestInterruptedException(e);
    }
    if (!acquired) {
        throw new TooManyConcurrentBillingRequestsException();
    }
    try {
        billingClient.createSubscription(command);
    } finally {
        billingBulkhead.release();
    }
}
```

**Wichtig:** `Semaphore` wirkt **pro Service-Instanz**. In einer mehrfach skalierten Anwendung (z. B. 10 Pods) ist die effektive Concurrency `20 × 10 = 200`. Wenn das Billing-System ein hartes Rate-Limit von 50 RPS hat, schlägt diese Strategie fehl. Bulkhead ist die richtige Wahl für Bulkhead-Zwecke (lokaler Schutz), nicht für externe Rate-Limits.

Verteiltes Rate-Limiting-Beispiel mit Bucket4j-Hazelcast:

```java
@Component
public class DistributedBillingRateLimiter {

    private final ProxyManager<String> proxyManager;

    public void enforceRateLimit(String tenantId) {
        var bucket = proxyManager.builder()
                .build(tenantKey(tenantId), this::createBucketConfiguration);

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "Billing rate limit exceeded for tenant " + tenantId);
        }
    }

    private BucketConfiguration createBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, Duration.ofMinutes(1)))
                .build();
    }
}
```

Für externe Provider-Rate-Limits ist ein zentraler Counter (Redis, Hazelcast, distributed Cache) Pflicht — egal wie viele JVM-Instanzen die Anwendung hat.

### 15.4 Idempotency-Keys bei Schreib-Use-Cases

Schreibende Use Cases sind selten von Natur aus idempotent. Wenn ein Client einen `POST /orders` zweimal sendet (Retry nach Timeout), entstehen zwei Bestellungen.

```java
public record CreateOrderCommand(
        @NotBlank String idempotencyKey,         // ✅ vom Client erzeugt
        @NotNull List<OrderItemCommand> items,
        ShippingAddress shippingAddress
) {}

@Transactional
public OrderCreatedResponse createOrder(CreateOrderCommand command) {
    var tenantId = tenantContext.currentTenantId();

    var existing = idempotencyRepository.findByTenantIdAndKey(
            tenantId, command.idempotencyKey());
    if (existing.isPresent()) {
        return existing.get().getResponse();    // ✅ identische Antwort
    }

    var order = OrderEntity.create(tenantId, command);
    var saved = orderRepository.save(order);
    var response = mapper.toCreatedResponse(saved);

    idempotencyRepository.save(IdempotencyRecord.of(
            tenantId, command.idempotencyKey(), response));

    return response;
}
```

Bei HTTP-APIs ist ein `Idempotency-Key`-Header die übliche Konvention (Stripe, Square, Adyen verwenden das so). Server-seitig wird er aus dem Header extrahiert und in den Command übernommen.

---

## 16. Falsche Anwendung und Anti-Patterns

### 16.1 God Service

Ein Service mit 20 Abhängigkeiten und 50 öffentlichen Methoden ist fast immer ein Zeichen für fehlenden Schnitt.

Symptome:

1. Name endet auf generisches `Manager`, `Helper` oder `Service` ohne Use-Case-Klarheit.
2. Methoden betreffen Registrierung, Suche, Billing, E-Mail, Admin, Export und Reporting zugleich.
3. Tests müssen riesige Mock-Setups bauen.
4. Jede Änderung erzeugt Merge-Konflikte.
5. Neue Anforderungen werden einfach „noch schnell" in denselben Service gebaut.

Korrektur — von einem `UserService` mit allem zu fokussierten Use-Case-Services:

```java
// Vorher
@Service public class UserService { /* 30 Methoden */ }

// Nachher
@Service public class UserRegistrationService { /* register */ }
@Service public class UserProfileUpdateService { /* updateProfile */ }
@Service public class UserDeactivationService { /* deactivate */ }
@Service public class UserQueryService { /* getUser, listUsers */ }
@Service public class UserPasswordResetService { /* requestReset, confirmReset */ }
```

Jeder dieser Services hat 1–3 öffentliche Methoden und einen klaren Use Case. Tests sind kleiner. Reviews sind fokussierter. Merge-Konflikte werden seltener.

### 16.2 Anämischer Weiterleitungsservice

Auch das Gegenteil ist schlecht: Ein Service, der nur Repository-Methoden eins zu eins weiterreicht.

```java
// ❌ Schlecht: Service ohne fachliche Funktion
@Service
public class UserService {

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
}
```

Wenn ein Service keine fachliche Regel, keine Transaktionsgrenze, kein Mapping, keine Autorisierung und keinen Use Case ausdrückt, sollte geprüft werden, ob er überhaupt gebraucht wird. Wenn doch, dann mit echtem Mehrwert:

```java
// ✅ Richtig: Service mit fachlicher Verantwortung
@Service
public class UserQueryService {

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        var tenantId = tenantContext.currentTenantId();
        var user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return mapper.toResponse(user);
    }
}
```

### 16.3 Transaction Script ohne Fachsprache

```java
// ❌ Schlecht: niemand erkennt den Use Case
@Transactional
public void process(Long id) {
    var a = repoA.findById(id).orElseThrow();
    var b = repoB.findByA(a);
    if (b.getX() > 10) {
        b.setY("OK");
    }
    repoB.save(b);
}
```

Gute Services nutzen fachliche Namen:

```java
// ✅ Richtig: Use Case lesbar
@Transactional
public ApprovalResponse approvePendingOrder(ApproveOrderCommand command) {
    var tenantId = tenantContext.currentTenantId();
    var order = orderRepository.findByIdAndTenantId(command.orderId(), tenantId)
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    if (order.canBeApproved()) {
        order.approve(command.approverId());
        return mapper.toApprovalResponse(order);
    }
    throw new InvalidOrderStateException(order.getStatus());
}
```

### 16.4 Entity als Request-Body

```java
// ❌ Schlecht: Mass Assignment, fehlende Validierung
@PostMapping("/users")
public UserEntity create(@RequestBody UserEntity user) {
    return userRepository.save(user);
}
```

Das ist in produktiven APIs verboten. Es öffnet die Tür für Mass Assignment, fehlende Validierung und direkte Kopplung an das Persistenzmodell.

```java
// ✅ Richtig: dediziertes Request-DTO und Service
@PostMapping("/users")
public ResponseEntity<UserCreatedResponse> create(
        @Valid @RequestBody RegisterUserRequest request) {
    var response = userRegistrationService.register(toCommand(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### 16.5 Event-Missbrauch als verdeckter Kontrollfluss

Events sind schlecht, wenn sie nur verwendet werden, damit niemand mehr sieht, was ein Use Case wirklich auslöst.

Ein Event ist geeignet, wenn mehrere unabhängige Reaktionen auf ein fachliches Ereignis möglich sind.

Ein Event ist ungeeignet, wenn die Hauptaktion fachlich zwingend von einem Schritt abhängt und dieser Schritt synchron, transaktional oder explizit fehlerbehandelt werden muss.

```java
// ❌ Schlecht: kritische Pflicht-Logik versteckt
@Transactional
public void register(RegisterUserCommand command) {
    var saved = userRepository.save(...);
    eventPublisher.publishEvent(new UserRegisteredEvent(saved));
    // Im Listener wird Billing aufgesetzt — aber niemand sieht das hier
    // Wenn der Listener fehlschlägt, weiß die Registrierung nichts davon
}

// ✅ Richtig: Pflicht-Logik im selben Use Case, optionale Reaktion über Event
@Transactional
public void register(RegisterUserCommand command) {
    var saved = userRepository.save(...);
    outboxRepository.save(OutboxEvent.billingTrialRequested(saved));   // Pflicht
    eventPublisher.publishEvent(new UserRegisteredEvent(saved));       // optional
}
```

---

## 17. Framework- und Plattform-Kontext

### 17.1 Spring MVC

Controller sollen HTTP in Commands übersetzen und Responses ausliefern. Sie sollen nicht entscheiden, wie Datenbanktransaktionen, Billing, Tenant-Regeln oder komplexe Fachlogik funktionieren.

### 17.2 Spring Data JPA

Repositories sind für Datenzugriff zuständig. Sie dürfen gezielte Query-Methoden, Projections und Entity-Ladeoperationen anbieten. Sie sollen aber keine Use-Case-Orchestrierung enthalten.

Für lesende Use Cases sind DTO-Projections sinnvoll, wenn nicht das vollständige Aggregat benötigt wird. Für mandantenfähige Systeme ist `findByIdAndTenantId` der Default (Sektion 12.4).

### 17.3 Bean Validation

Bean Validation ist sinnvoll für strukturelle Eingaberegeln:

1. Nicht leer.
2. Format.
3. Länge.
4. Wertebereiche.
5. Verschachtelte Objektvalidierung.

Bean Validation ersetzt nicht:

1. Autorisierung.
2. Tenant-Prüfung.
3. Datenbank-Eindeutigkeit.
4. Fachliche Statusübergänge.
5. Betrugs-/Missbrauchsregeln.
6. Cross-Field-Regeln, sofern sie besser als Domain Policy modelliert werden.

Für Records gelten besondere Regeln, siehe Sektion 13.3.

### 17.4 Wicket-Kontext

In Wicket-Anwendungen darf die Serviceschicht nicht von Wicket-Komponenten abhängen. Wicket-Seiten und Panels sollen Services aufrufen und DTOs anzeigen. Services sollen keine Wicket-Modelle, Komponenten oder Page-State-Objekte kennen.

Besonders wichtig:

1. Keine Wicket-Komponenten in Service-Methodenparametern.
2. Keine UI-spezifische Session-Logik in Services.
3. DTOs für View-Modelle bewusst klein halten — keine `IModel<T>` als Service-Rückgabewert.
4. Service-Ergebnisse nicht unkontrolliert im serialisierten Page State halten.

Typischer Wicket-Anti-Pattern und seine Korrektur:

```java
// ❌ Schlecht: Service kennt Wicket-Modelle
public IModel<UserEntity> loadUserModel(Long id) { ... }

// ✅ Richtig: Service liefert DTO, Wicket-Page wrappt in IModel
@Service
public class UserQueryService {
    public UserResponse getUser(Long id) { ... }
}

// In der Wicket-Page:
IModel<UserResponse> userModel = LoadableDetachableModel.of(
        () -> userQueryService.getUser(userId));
```

### 17.5 Modularer Monolith

Bei modularen Monolithen soll ein Service nicht direkt in die Persistenz eines anderen Moduls greifen. Stattdessen sind öffentliche Application Services, Domain Events oder klar definierte Ports zu verwenden.

### 17.6 Spring Modulith

Spring Modulith ist das offizielle Werkzeug für die Disziplin modularer Monolithen in Spring Boot. Es bietet:

1. **Application Module Validation** — `@ApplicationModule`-Annotation und Verifikation, dass Module nur über öffentliche Ports kommunizieren.
2. **Module Events** — Domain-Events mit `@Externalized` zur klaren Trennung interner und externer Events.
3. **Module Documentation** — Generierung von C4-Modellen und Modul-Dokumentation aus dem Code.
4. **Module Tests** — Bootstrap-Tests pro Modul, ohne den gesamten Spring-Kontext.

```java
// package: com.example.user
@ApplicationModule(
    displayName = "User Management",
    allowedDependencies = {"shared.security", "shared.events"}
)
package com.example.user;

import org.springframework.modulith.ApplicationModule;
```

```java
class ModularityTests {

    @Test
    void verifyModularStructure() {
        var modules = ApplicationModules.of(MyApplication.class);
        modules.verify();                        // ✅ scheitert bei Modul-Verstoß
    }
}
```

Für neue modulare Monolithen ist Spring Modulith der empfohlene Default. Für bestehende Codebasen ist Spring Modulith schrittweise einführbar — Module können einzeln annotiert und verifiziert werden.

---

## 18. Testing-Regeln

### 18.1 Unit-Tests ohne Spring-Kontext

Ein gut geschnittener Service kann in vielen Fällen ohne Spring-Kontext getestet werden.

```java
class UserRegistrationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final TenantContext tenantContext = mock(TenantContext.class);
    private final TenantAccessPolicy tenantAccessPolicy = mock(TenantAccessPolicy.class);

    private final UserRegistrationService service = new UserRegistrationService(
            userRepository,
            passwordEncoder,
            eventPublisher,
            tenantContext,
            tenantAccessPolicy
    );

    @Test
    void registerFailsWhenEmailAlreadyExists() {
        var command = new RegisterUserCommand(
                "Max Mustermann",
                "max@example.org",
                "a-very-long-password"
        );

        when(tenantContext.currentTenantId()).thenReturn("tenant-1");
        when(userRepository.existsByTenantIdAndEmail("tenant-1", "max@example.org"))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> service.register(command));
    }
}
```

### 18.2 Integrationstests für Transaktionen

Transaktionsverhalten, Lazy Loading, JPA-Mapping, Datenbankconstraints, `@TransactionalEventListener` und echte Repository-Queries sollten mit Integrationstests geprüft werden.

```java
@SpringBootTest
@Testcontainers
class OrderConfirmationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private OrderConfirmationService service;
    @Autowired private OrderRepository repository;

    @Test
    void optimisticLockingTriggersConflictOnConcurrentConfirmation() {
        // ... echte Konflikt-Simulation mit zwei Transaktionen
    }
}
```

Unit-Tests beweisen nicht, dass der Spring-Proxy, die Transaktion oder der JPA-Provider wie erwartet zusammenspielen.

### 18.3 Testfälle für Services

Für relevante Services sollen Tests mindestens prüfen:

1. Erfolgreicher Use Case.
2. Validierungsfehler (inkl. Bean Validation auf Records gegen die aktuelle Library-Version).
3. Autorisierungsfehler.
4. Tenant-Grenzverletzung (Resource eines anderen Tenants).
5. Fachlicher Konflikt.
6. Datenbank-Nichtfund.
7. Optimistic-Lock-Konflikt bei parallelen Modifikationen.
8. Nebenwirkung wird ausgelöst oder Outbox-Eintrag wird geschrieben.
9. Keine Entity wird nach außen exponiert.
10. Idempotentes Verhalten bei Wiederholung mit demselben Idempotency-Key.
11. Fehler-Mapping an der API-Grenze.

---

## 19. Review-Checkliste

Im Pull Request sind folgende Fragen zu prüfen. Jede Zeile verweist auf die Detail-Sektion mit der Begründung.

| Aspekt | Prüffrage | Detail |
| --- | --- | --- |
| Verantwortlichkeit | Hat der Service genau einen zusammenhängenden Use-Case-Bereich? | §8, §9 |
| Größe | Hat der Service zu viele öffentliche Methoden oder Abhängigkeiten? | §16.1 |
| Konstruktorinjektion | Werden alle Dependencies über den Konstruktor injiziert? | §3.1.2 |
| DTO-Trennung | Gibt der Service Entities an Controller oder API-Grenzen zurück? | §12.1 |
| Command Design | Gibt es generische Alles-DTOs oder präzise Commands? | §12.2 |
| Bean Validation | Sind Annotation-Targets, `@Validated`/`@Valid` und Kaskadierung korrekt? | §13.3 |
| Transaktionen | Ist `@Transactional` dort gesetzt, wo die fachliche Einheit liegt? | §11.1 |
| Method Visibility | Steht `@Transactional` ausschließlich auf öffentlichen Methoden? | §11.3 |
| Self-Invocation | Ruft eine Methode derselben Klasse eine annotierte Methode über `this` auf? | §11.4 |
| Read-only | Ist `readOnly = true` sinnvoll gesetzt? | §11.2 |
| Optimistic Locking | Haben Aggregate-Roots ein `@Version`-Feld? | §11.6 |
| JPA-Lifecycle | Wird `save()` redundant aufgerufen oder fehlt er bei detached Entities? | §11.7 |
| Externe Calls | Gibt es HTTP-, Mail-, Billing- oder Webhook-Calls innerhalb offener DB-Transaktionen? | §11.5 |
| Events | Ist klar, ob der Event kritisch, optional, synchron oder asynchron ist? | §10.5 |
| Outbox-Datasource | Liegt die Outbox-Tabelle in derselben Datenquelle wie die Domain? | §10.3 |
| Outbox-Worker | Gibt es einen Polling-Worker mit `FOR UPDATE SKIP LOCKED` und Backoff? | §10.4 |
| Fehlersemantik | Werden sprechende Exceptions oder Result-Typen verwendet? | §9.2 |
| API-Fehler | Gibt es `@ControllerAdvice` oder vergleichbares Mapping? | §13.2 |
| Security | Prüft der Service Berechtigungen und Tenant-Kontext? | §14.1, §14.3 |
| Tenant-Kontext | Wird der Tenant aus dem `TenantContext` geholt, nicht aus dem Request? | §14.6 |
| Tenant-Filter | Verwenden Repository-Queries `findByIdAndTenantId` o. ä.? | §12.4 |
| User Enumeration | Verraten öffentliche Auth-Endpoints Existenz? | §14.5 |
| Logging | Werden Commands, DTOs oder Entities unkontrolliert geloggt? | §14.4 |
| Bulkhead vs. Rate Limit | Wird das richtige Werkzeug für die richtige Klasse verwendet? | §15.3 |
| Idempotency | Haben schreibende Use Cases einen Idempotency-Key? | §15.4 |
| Pagination | Werden Listen-Endpoints paginiert? | §12.5 |
| Soft Delete | Ist Soft-Delete-Disziplin (Constraints, Filter) konsistent? | §12.6 |
| Testing | Kann der Service ohne Spring-Kontext unit-getestet werden? | §18.1 |
| Projections | Wird nur das geladen, was der Use Case benötigt? | §12.3 |
| Framework-Kopplung | Kennt der Service HTTP-, Wicket- oder Controller-Typen? | §3.2.1, §17.4 |
| Spring Modulith | Werden Modul-Grenzen verifiziert (sofern eingesetzt)? | §17.6 |

---

## 20. Automatisierbare Prüfungen

### 20.1 ArchUnit-Regeln

```java
@ArchTest
static final ArchRule controllers_should_not_access_repositories =
        noClasses()
                .that().resideInAPackage("..api..")
                .should().accessClassesThat().resideInAPackage("..persistence..");

@ArchTest
static final ArchRule services_should_not_depend_on_web =
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web..",
                        "jakarta.servlet.."
                );

@ArchTest
static final ArchRule repositories_should_not_depend_on_services =
        noClasses()
                .that().resideInAPackage("..persistence..")
                .should().dependOnClassesThat().resideInAPackage("..application..");

// ✅ Services dürfen keine Entities zurückgeben
@ArchTest
static final ArchRule services_should_not_return_entities =
        methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..application..")
                .and().arePublic()
                .should(notReturnTypeFrom("..persistence.."));

// ✅ Controller dürfen keine Entities annehmen
@ArchTest
static final ArchRule controllers_should_not_accept_entities =
        methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..api..")
                .should(notHaveParameterTypeFrom("..persistence.."));

// ✅ Keine Field-Injection
@ArchTest
static final ArchRule no_field_injection =
        noFields()
                .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .orShould().beAnnotatedWith("jakarta.inject.Inject");

// ✅ Aggregate-Roots haben @Version
@ArchTest
static final ArchRule aggregate_roots_have_version =
        classes()
                .that().resideInAPackage("..persistence..")
                .and().areAnnotatedWith("jakarta.persistence.Entity")
                .and().areMetaAnnotatedWith("com.example.shared.AggregateRoot")
                .should().haveFieldsThat().areAnnotatedWith("jakarta.persistence.Version");
```

### 20.2 Static-Analysis-Regeln

Automatisierbar oder halbautomatisierbar sind unter anderem:

1. Feldinjektion mit `@Autowired` markieren.
2. `@Transactional` auf nicht-öffentlichen Methoden markieren.
3. Controller-Methoden mit Entity-Rückgabetyp markieren.
4. `@RequestBody` auf Entity-Typen markieren.
5. Generische `throw new RuntimeException(...)` in Services markieren.
6. Services mit zu vielen Abhängigkeiten markieren (Threshold konfigurierbar).
7. Methoden mit externen Client-Aufrufen innerhalb `@Transactional` markieren.
8. Log-Ausgaben kompletter Commands oder Entities markieren.
9. Zugriff aus Controller-Package auf Repository-Package blockieren.
10. Zugriff aus Service-Package auf Web-/Servlet-/Wicket-Typen blockieren.
11. `findById` auf Repositories in mandantenfähigen Modulen markieren (zugunsten `findByIdAndTenantId`).
12. Bean-Validation-Annotationen ohne `RECORD_COMPONENT`-Target markieren.

### 20.3 CI-Gates

Für produktionsnahe Codebasen sollten folgende Gates existieren:

1. Unit-Tests für Service-Logik.
2. Integrationstests für relevante Transaktions- und Repository-Fälle.
3. ArchUnit-Regeln für Schichtengrenzen.
4. Spring Modulith Verification (sofern modularer Monolith).
5. SAST für offensichtliche Injection-, Logging- und Security-Probleme.
6. Dependency-Scanning.
7. Testdatenprüfung gegen echte Secrets.
8. Coverage-Grenzen für kritische Services.
9. Mutation Testing für besonders geschäftskritische Logik, wenn Aufwand und Nutzen passen.
10. Bean Validation Constraint-Tests gegen die aktuelle Hibernate-Validator-Version.

---

## 21. Migration bestehender Services

### 21.1 Schrittweise Migration

Bestehende God Services sollen nicht blind in viele kleine Klassen zerlegt werden. Migration erfolgt kontrolliert:

1. Öffentliche Methoden inventarisieren.
2. Use Cases gruppieren.
3. Transaktionale Methoden identifizieren.
4. Externe Nebeneffekte identifizieren.
5. Entity-Rückgaben und Entity-Request-Bodies finden.
6. Tenant-Isolation prüfen — `findById` ohne Filter ist kritisch.
7. Aggregate-Roots ohne `@Version` markieren.
8. Fachliche Exceptions einführen.
9. DTOs und Mapper einführen.
10. Lesende und schreibende Use Cases trennen, wenn sinnvoll.
11. Tests vor Refactoring stabilisieren.
12. Neue Services extrahieren.
13. Alte Methoden deprecaten oder intern weiterleiten.
14. ArchUnit-Regeln schrittweise aktivieren.

### 21.2 Migration von Entity-Responses

Wenn Controller aktuell Entities zurückgeben:

1. API-Vertrag dokumentieren.
2. Response-DTO einführen.
3. Mapping explizit implementieren.
4. Prüfen, welche Felder wirklich öffentlich sein dürfen.
5. Tests für Response-Struktur ergänzen.
6. Entity-Rückgabe entfernen.
7. Jackson-Annotationen auf Entities kritisch prüfen und reduzieren.

### 21.3 Migration von direkten Nebeneffekten

Wenn Services E-Mail, Billing oder Webhooks direkt innerhalb einer Transaktion ausführen:

1. Fachliche Kritikalität bewerten.
2. Entscheiden: synchroner Schritt, nach Commit Event oder Outbox.
3. Idempotenzschlüssel definieren.
4. Retry-Strategie definieren.
5. Fehlerstatus modellieren.
6. Outbox-Tabelle in derselben Datenquelle anlegen.
7. Polling-Worker mit `FOR UPDATE SKIP LOCKED` aufbauen.
8. Monitoring und Alerting ergänzen.
9. Tests für Fehlerfälle schreiben.

### 21.4 Migration zu Tenant-bewussten Repositories

Wenn `findById` ohne Tenant-Filter im Code verbreitet ist:

1. Repository-Methoden mit `findByIdAndTenantId` ergänzen (alte Methoden zunächst behalten).
2. Service-Aufrufe schrittweise umstellen.
3. ArchUnit- oder Static-Analysis-Regel aktivieren, die `findById` in betroffenen Modulen markiert.
4. Alte Methoden nach vollständiger Migration entfernen.
5. Hibernate-Filter als systemische Lösung evaluieren.

---

## 22. Ausnahmen

Abweichungen von dieser Richtlinie sind zulässig, wenn sie bewusst und nachvollziehbar sind.

Mögliche Ausnahmen:

1. Sehr kleine interne CRUD-Tools mit begrenztem Risiko.
2. Technische Adapter ohne fachliche Logik.
3. Legacy-Code, der nur minimal geändert wird.
4. Performance-kritische Pfade mit bewusstem Sonderdesign.
5. Framework-Zwänge.
6. Bewusst gewählte CQRS-, Hexagonal- oder modulare Architekturvarianten mit eigenem Standard.
7. Einfache Admin-Werkzeuge, solange Security- und Tenant-Regeln erfüllt bleiben.

Nicht zulässig als Begründung:

1. „Das war schneller."
2. „Das machen wir immer so."
3. „Der Controller kann doch direkt das Repository aufrufen."
4. „Die Entity hat schon alle Felder."
5. „Wir loggen einfach das ganze Objekt."
6. „Die Methode ist privat, aber `@Transactional` steht ja dran."
7. „Virtual Threads lösen das schon."
8. „Tenant-ID kommt aus dem Request, vertrauen wir dem Frontend."
9. „Optimistic Locking brauchen wir nicht, das passiert schon nicht parallel."

---

## 23. Definition of Done

Ein Spring-Boot-Service erfüllt diese Richtlinie, wenn alle folgenden Bedingungen erfüllt sind:

1. Der Service hat einen klaren fachlichen Verantwortungsbereich.
2. Abhängigkeiten werden per Konstruktor injiziert.
3. Es gibt keine Feldinjektion.
4. Controller greifen nicht direkt auf Repositories zu.
5. Services hängen nicht von HTTP-, Servlet-, Wicket- oder Controller-Typen ab.
6. Eingaben sind über präzise Commands oder Query-Parameter modelliert.
7. Ausgaben an API-Grenzen sind Response-DTOs oder Projections, keine Entities.
8. Request-DTOs werden nicht direkt als Entities gespeichert.
9. Bean-Validation-Annotationen auf Records sind gegen die aktuelle Hibernate-Validator-Version mit Constraint-Tests verifiziert.
10. Transaktionsgrenzen sind auf öffentlichen Service-Methoden bewusst gesetzt.
11. Schreibende Use Cases haben eine klare atomare Einheit.
12. Lesende Use Cases verwenden `readOnly = true`, wenn Datenbankzugriff und Konsistenz-/Provider-Aspekte relevant sind.
13. Es gibt kein `@Transactional` auf nicht-öffentlichen Methoden.
14. Es gibt keine transaktionale Self-Invocation, die den Spring-Proxy umgeht.
15. Aggregate-Roots haben ein `@Version`-Feld und Optimistic-Lock-Konflikte werden zu HTTP 409 gemappt.
16. JPA-Lifecycle (managed/detached) ist im Service-Code korrekt behandelt.
17. Externe Nebeneffekte innerhalb von Transaktionen sind vermieden oder explizit begründet.
18. Kritische Nebeneffekte sind über Outbox in derselben Datenquelle abgesichert; ein Polling-Worker mit Backoff existiert.
19. Tenant-IDs werden aus dem `TenantContext` geholt, nicht aus untrusted Requests.
20. Repository-Queries verwenden Tenant-Filter (`findByIdAndTenantId` o. ä.).
21. Öffentliche Auth-Endpoints geben einheitliche Antworten und vermeiden User Enumeration.
22. Lokale Bulkheads und verteilte Rate-Limits sind klar getrennt.
23. Schreibende Use Cases mit Wiederholungsrisiko unterstützen Idempotency-Keys.
24. Listen-Endpoints sind paginiert.
25. Soft-Delete-Disziplin (Filter, Constraints) ist konsistent angewendet, wo erforderlich.
26. Fachliche Fehler sind sprechend modelliert.
27. API-Fehler werden stabil und ohne sensible Details gemappt.
28. Tenant- und Berechtigungsprüfungen sind in oder unterhalb der Serviceschicht abgesichert.
29. Logs enthalten keine sensitiven Payloads.
30. Relevante Unit-Tests existieren ohne unnötigen Spring-Kontext.
31. Relevante Integrationstests prüfen Transaktionen, Repositories, Optimistic Locking und Event-Verhalten.
32. ArchUnit- oder vergleichbare Regeln sichern Schichtengrenzen.
33. Bei modularen Monolithen verifiziert Spring Modulith die Modul-Grenzen.
34. Abweichungen sind im Pull Request begründet.

---

## 24. Entscheidungsbaum

```
Soll eine neue Service-Methode entstehen?
├─ Koordiniert sie einen fachlichen Use Case?
│  ├─ Nein → Ist es wirklich ein Service oder eher Mapper/Policy/Repository?
│  └─ Ja
│     ├─ Greift sie auf die Datenbank zu?
│     │  ├─ Nein → keine Transaktion setzen, außer ein klarer Grund existiert.
│     │  └─ Ja
│     │     ├─ nur lesend?
│     │     │  ├─ Ja → @Transactional(readOnly = true) prüfen.
│     │     │  └─ Nein → @Transactional auf öffentlicher Use-Case-Methode setzen.
│     │     └─ Aggregate-Root mit paralleler Modifikation möglich?
│     │        ├─ Ja → @Version (Optimistic Locking) und 409-Mapping prüfen.
│     │        └─ Nein → keine Versionierung nötig.
│     ├─ Gibt sie Daten an API oder Controller zurück?
│     │  ├─ Ja → Response-DTO oder Projection verwenden, keine Entity.
│     │  └─ Nein → interne Typen prüfen.
│     ├─ Mandantenfähiges System?
│     │  ├─ Ja → Tenant aus TenantContext, Repository-Filter (findByIdAndTenantId).
│     │  └─ Nein → trotzdem Datenzugriff prüfen.
│     ├─ Listen-Endpoint?
│     │  ├─ Ja → Pageable-Parameter, Cursor-Pagination bei großen Tabellen.
│     │  └─ Nein → Einzelobjekt-Logik.
│     ├─ Schreibender Use Case mit Wiederholungsrisiko?
│     │  ├─ Ja → Idempotency-Key im Command.
│     │  └─ Nein → kein Idempotency-Mechanismus nötig.
│     ├─ Enthält sie externe Nebeneffekte?
│     │  ├─ Ja
│     │  │  ├─ kritisch → Outbox in gleicher Datasource + Polling-Worker.
│     │  │  └─ unkritisch → @TransactionalEventListener(AFTER_COMMIT).
│     │  └─ Nein → normale Service-Logik.
│     ├─ Öffentlicher Auth-/Identity-Endpoint?
│     │  ├─ Ja → einheitliche Antworten, keine Existenz-Verraten.
│     │  └─ Nein → reguläres Fehler-Mapping.
│     ├─ Enthält sie fachliche Fehlerfälle?
│     │  ├─ Ja → sprechende Exception oder Result-Typ modellieren.
│     │  └─ Nein → keine generischen RuntimeExceptions einführen.
│     └─ Ist sie isoliert testbar?
│        ├─ Ja → Unit-Test schreiben.
│        └─ Nein → Schnitt, Abhängigkeiten und Framework-Kopplung prüfen.
```

---

## 25. Quellen und weiterführende Literatur

* Spring Framework Reference — Dependency Injection: <https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html>
* Spring Framework Reference — Declarative Transaction Management with `@Transactional`: <https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html>
* Spring Framework Reference — Transaction-bound Events: <https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html>
* Spring Framework Reference — Bean Validation: <https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html>
* Spring Framework Reference — Spring MVC Validation: <https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html>
* Spring Framework Reference — Error Responses and `ProblemDetail`: <https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html>
* Spring Boot Reference — Task Execution and Scheduling / Virtual Threads: <https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html>
* Spring Data JPA Reference — Projections: <https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html>
* Spring Data JPA Reference — Locking: <https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html>
* Spring Modulith Reference: <https://docs.spring.io/spring-modulith/reference/>
* Hibernate Validator 8 Reference: <https://docs.jboss.org/hibernate/validator/8.0/reference/en-US/html_single/>
* Jakarta Bean Validation 3.0: <https://jakarta.ee/specifications/bean-validation/3.0/>
* RFC 9457 — Problem Details for HTTP APIs: <https://www.rfc-editor.org/rfc/rfc9457.html>
* OWASP API Security Top 10 2023 — API3 Broken Object Property Level Authorization: <https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/>
* OWASP API Security Top 10 2023 — Unrestricted Resource Consumption: <https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/>
* OWASP Authentication Cheat Sheet — User Enumeration: <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html>
* OWASP Logging Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html>
* PostgreSQL — `SELECT FOR UPDATE SKIP LOCKED`: <https://www.postgresql.org/docs/current/sql-select.html>
* Bucket4j — Distributed Rate Limiting: <https://bucket4j.com/>
* Stripe — Idempotent Requests: <https://docs.stripe.com/api/idempotent_requests>
* Microsoft — Transactional Outbox Pattern: <https://learn.microsoft.com/en-us/azure/architecture/databases/guide/transactional-outbox-cosmos>