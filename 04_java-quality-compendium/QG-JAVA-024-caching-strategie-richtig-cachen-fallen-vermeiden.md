# QG-JAVA-024 — Caching-Strategie: Richtig cachen, Fallen vermeiden

## Dokumentstatus

| Aspekt | Details/Erklärung |
|---|---|
| Dokumenttyp | Java Quality Guideline |
| ID | QG-JAVA-024 |
| Titel | Caching-Strategie: Richtig cachen, Fallen vermeiden |
| Status | Accepted / verbindlicher Standard für Cache-Entscheidungen in Java-Services |
| Version | 1.0.0 |
| Datum | 2025-03-24 |
| Kategorie | Performance / Architektur / Betrieb / Security |
| Zielgruppe | Java-Entwickler, Tech Leads, Architektur, DevOps, SRE, Security, QA |
| Java-Baseline | Java 21 |
| Framework-Baseline | Spring Boot 3.x, Spring Framework Cache Abstraction, Caffeine, Redis/Spring Data Redis, Micrometer |
| Geltungsbereich | Lokale Caches, verteilte Caches, Spring Cache, Caffeine, Redis, Cache-Key-Design, TTL, Invalidierung, Stampede-Schutz, Security- und Tenant-Grenzen, Monitoring |
| Verbindlichkeit | Caching wird nur nach Messung oder nachvollziehbarer Performance-/Resilience-Begründung eingeführt. Jeder Cache benötigt Zweck, Key-Strategie, TTL, Invalidierungsstrategie, Monitoring und klare Security-Bewertung. |
| Technische Validierung | Gegen Spring Cache Abstraction, Spring Boot Caching, Spring Data Redis Cache, Caffeine, Redis Eviction/TTL und Micrometer-Cache-Metriken eingeordnet |
| Kurzentscheidung | Cache ist kein Ersatz für gutes Datenmodell, gute Queries oder saubere Architektur. Cache wird gezielt eingesetzt, begrenzt, beobachtet und invalidiert. Unbegrenzte, ungemessene oder sicherheitskritische Caches sind nicht zulässig. |

---

## 1. Zweck

Diese Richtlinie definiert, wann, wie und womit in Java- und Spring-Boot-Systemen gecacht wird.

Caching kann Antwortzeiten verbessern, teure Datenbankzugriffe vermeiden, externe Systeme entlasten und Lastspitzen abfedern. Falsch eingesetzt erzeugt Caching aber neue Fehlerklassen: veraltete Daten, falsche Daten durch unvollständige Cache-Keys, Cache-Stampede, Speicherlecks, Cross-Tenant-Datenlecks, falsch gecachte Berechtigungen, schwer reproduzierbare Inkonsistenzen, zusätzliche Betriebsabhängigkeiten und Debugging-Aufwand.

Die erste professionelle Caching-Regel lautet:

```text
Erst messen, dann cachen.
```

Caching darf nicht genutzt werden, um schlechtes Query-Design, fehlende Indexe, falsche Aggregate-Grenzen, N+1-Probleme oder überladene APIs zu verdecken. Diese Probleme werden zuerst analysiert. Cache ist eine bewusste Optimierung, keine Reparaturschicht.

---

## 2. Kurzregel

Cache nur Daten, die häufig gelesen, selten geändert, teuer zu berechnen oder teuer zu laden sind und fachlich leicht veraltete Werte tolerieren. Jeder Cache braucht begrenzte Größe, TTL, eindeutigen Key, Invalidierungsstrategie, Monitoring und Security-Prüfung. Keine Berechtigungen, Credentials, Tokens, personenbezogenen Rohdaten oder tenantübergreifenden Ergebnisse cachen, wenn Isolation und Aktualität nicht explizit garantiert sind.

---

## 3. Geltungsbereich

Diese Richtlinie gilt für Spring Cache mit `@Cacheable`, `@CachePut`, `@CacheEvict`, Caffeine als lokalen In-Memory-Cache, Redis als verteilten Cache, read-through und cache-aside Patterns, Cache Keys, TTL und Eviction, Stampede-Schutz, Cache-Warming, Cache-Invalidierung, SaaS-/Tenant-Caching, Security-sensitive Daten, HTTP-Client-nahe Caches, Referenzdaten-Caches, externe API-Response-Caches, Reporting-/Projection-Caches sowie Integration in Observability und CI/CD.

Diese Richtlinie gilt nicht für Datenbankpuffer, interne DB-Caches, Browser-Caches, CDN-Caching, reine HTTP-Cache-Header-Strategien, dedizierte Suchindizes wie Elasticsearch, CQRS-Read-Models oder Materialized Views, auch wenn diese Konzepte fachlich angrenzen.

---

## 4. Technischer Hintergrund

Spring Framework stellt eine Cache Abstraction bereit, die Caching transparent über Annotationen oder APIs integriert. Die zentralen Annotationen sind `@Cacheable` für Cache-Population, `@CacheEvict` für Eviction, `@CachePut` für Aktualisierung, `@Caching` für kombinierte Operationen und `@CacheConfig` für gemeinsame Einstellungen auf Klassenebene.

Spring Boot erkennt Cache-Provider auf dem Classpath und kann unter anderem Caffeine oder Redis als Cache-Backend konfigurieren. Für Caffeine kann die Cache-Spezifikation über `spring.cache.caffeine.spec` gesetzt werden. Spring Data Redis stellt mit `RedisCacheManager` eine Implementierung der Spring-Cache-Abstraction für Redis bereit.

Caffeine ist eine leistungsfähige lokale Java-Caching-Bibliothek mit Größenbegrenzung, Expiration, Refresh, LoadingCache, AsyncLoadingCache und Statistiken. Redis ist ein externer In-Memory-Datenspeicher, der häufig als verteilter Cache genutzt wird und TTL sowie Eviction Policies unterstützt.

Wichtig: Spring Cache ist eine Abstraktion. Die Semantik hängt vom Cache-Provider ab. Caffeine ist lokal und sehr schnell, aber nicht zwischen Instanzen geteilt. Redis ist verteilt, aber Netzwerkzugriff, Serialisierung und Ausfallverhalten müssen berücksichtigt werden.

---

## 5. Entscheidungsregel: Wann cachen?

### 5.1 Entscheidungsmatrix

| Kriterium | Cache sinnvoll | Cache vermeiden | Details/Erklärung |
|---|---:|---:|---|
| Leseintensiv | Ja |  | Viele Reads, wenige Writes |
| Selten geändert | Ja |  | Produktkatalog, Referenzdaten |
| Teuer zu berechnen | Ja |  | komplexer Report, externe API |
| Teuer zu laden | Ja |  | langsamer Downstream |
| Veraltete Daten tolerierbar | Ja |  | Kategoriebaum, Wechselkurse mit kurzer TTL |
| Schreibintensiv |  | Ja | viele Invalidierungen |
| Transaktional konsistent nötig |  | Ja | Kontostand, verfügbare Tickets, Lagerbestand |
| Sicherheitsrelevant |  | Meist ja | Permissions, Rollen, Tokens |
| Hoch personalisiert |  | Meist ja | viele Keys, geringe Hit Rate |
| Tenantübergreifend riskant |  | Ja | fehlender Tenant im Key |
| Kleine schnelle DB-Query |  | Ja | Cache overhead größer als Nutzen |
| Unklare Invalidierung |  | Ja | Daten ändern sich aus vielen Quellen |
| Kein Monitoring |  | Ja | Cache kann nicht bewertet werden |

### 5.2 Caching-Entscheidung als Fragenkette

Vor jeder Cache-Einführung beantworten:

1. Welches konkrete Performanceproblem wird gelöst?
2. Wurde das Problem gemessen?
3. Welche Operation ist teuer?
4. Wie hoch ist die aktuelle Latenz?
5. Wie hoch ist die Ziel-Latenz?
6. Wie häufig wird gelesen?
7. Wie häufig wird geschrieben?
8. Wie alt dürfen Daten maximal sein?
9. Wer invalidiert den Cache?
10. Welche Parameter beeinflussen das Ergebnis?
11. Ist Tenant, User, Locale oder Permission relevant?
12. Welche maximale Cache-Größe ist zulässig?
13. Was passiert bei Cache-Ausfall?
14. Wie wird Hit Rate gemessen?
15. Wie wird ein falscher Cache-Eintrag erkannt?

Wenn diese Fragen nicht beantwortet werden können, ist Caching verfrüht.

---

## 6. Cache-Arten

| Cache-Art | Details/Erklärung | Typischer Einsatz | Risiko |
|---|---|---|---|
| Local In-Memory Cache | Cache pro JVM-Instanz | Referenzdaten, kleine Lookup-Tabellen | Instanzen haben unterschiedliche Werte |
| Distributed Cache | Gemeinsamer Cache über Redis | mehrere Instanzen, Sessions, gemeinsame Lookups | Netzwerk, Serialisierung, Ausfall |
| Read-Through Cache | Cache lädt Daten bei Miss selbst | Caffeine LoadingCache | Loader-Komplexität |
| Cache-Aside | Anwendung liest Cache, lädt bei Miss, schreibt Cache | manuelle Kontrolle | Fehleranfällige Invalidierung |
| Write-Through | Schreibzugriff aktualisiert Cache und Store | starke Aktualität | komplexer |
| Write-Behind | Cache nimmt Write an, Store später | hohe Performance | Datenverlustrisiko |
| Refresh Cache | Eintrag wird im Hintergrund aktualisiert | teure, häufig gelesene Daten | alte Werte bleiben sichtbar |
| Negative Cache | Fehlende Ergebnisse werden kurz gecacht | Schutz vor wiederholten Misses | Gefahr bei späterer Existenz |
| HTTP Cache | Cache über HTTP-Header/ETag | APIs/CDN/Clients | andere Semantik |
| Materialized View | vorberechnete Sicht in DB | Reporting | kein klassischer App-Cache |

Für normale Spring-Boot-Services gilt: Caffeine lokal zuerst prüfen. Redis nur, wenn verteilte Konsistenz, instanzübergreifendes Teilen oder größere Cache-Datenmenge wirklich erforderlich ist.

---

## 7. Lokaler Cache mit Caffeine

### 7.1 Gute Einsatzfälle

Caffeine ist geeignet für kleine bis mittlere Referenzdaten, Produktkatalog-Ausschnitte, Konfigurationswerte, Lookups, teure Berechnungen, externe API-Ergebnisse mit kurzer TTL und Daten, die pro Instanz unterschiedlich alt sein dürfen.

### 7.2 Spring-Boot-Konfiguration

```yaml
spring:
  cache:
    type: caffeine
    cache-names:
      - products
      - categories
      - exchange-rates
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m,recordStats
```

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
}
```

### 7.3 Schlechte Anwendung: manuelle HashMap

```java
@Service
public class ProductService {

    private final Map<ProductId, ProductDto> cache = new HashMap<>();

    public ProductDto findById(ProductId id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        var product = productRepository.findById(id).orElseThrow();
        var dto = productMapper.toDto(product);
        cache.put(id, dto);
        return dto;
    }
}
```

Probleme:

- kein TTL,
- keine Maximalgröße,
- nicht thread-safe,
- kein Monitoring,
- keine Invalidierung,
- Speicherleck,
- kein einheitlicher Standard.

### 7.4 Gute Anwendung: Spring Cache

```java
@Service
@CacheConfig(cacheNames = "products")
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Cacheable(key = "#id.value()", unless = "#result == null")
    public ProductDto findById(ProductId id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElse(null);
    }

    @CacheEvict(key = "#id.value()")
    public void evict(ProductId id) {
        // bewusst leer: expliziter Eviction-Einstiegspunkt
    }
}
```

### 7.5 Hinweis zu `null`

`null`-Caching muss bewusst entschieden werden. Negative Caching kann sinnvoll sein, aber nur mit kurzer TTL.

Schlecht:

```java
@Cacheable("products")
public ProductDto findById(ProductId id) {
    return productRepository.findById(id)
            .map(productMapper::toDto)
            .orElse(null);
}
```

Besser:

```java
@Cacheable(value = "products", key = "#id.value()", unless = "#result == null")
public ProductDto findById(ProductId id) {
    ...
}
```

---

## 8. Verteiltes Caching mit Redis

### 8.1 Gute Einsatzfälle

Redis ist geeignet, wenn mehrere Instanzen denselben Cache teilen müssen, Cache-Werte zentral verwaltet sind, Cache-Invalidierung instanzübergreifend wirken muss, horizontale Skalierung lokale Caches ineffektiv macht oder externe API-Antworten instanzübergreifend entlastet werden sollen.

Redis ist nicht automatisch besser als Caffeine. Redis ist ein zusätzlicher Netzwerk-Hop und ein zusätzlicher Failure Point.

### 8.2 Spring Data Redis Cache-Konfiguration

```java
@Configuration
@EnableCaching
public class RedisCacheConfigurationProvider {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration defaultConfiguration) {

        var productConfig = defaultConfiguration.entryTtl(Duration.ofMinutes(10));
        var categoryConfig = defaultConfiguration.entryTtl(Duration.ofHours(1));
        var exchangeRateConfig = defaultConfiguration.entryTtl(Duration.ofMinutes(30));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withCacheConfiguration("products", productConfig)
                .withCacheConfiguration("categories", categoryConfig)
                .withCacheConfiguration("exchange-rates", exchangeRateConfig)
                .transactionAware()
                .build();
    }
}
```

### 8.3 Redis-Serialisierung

Nicht empfohlen:

- Java Native Serialization,
- unversionierte Binärformate ohne Strategie,
- Cache von JPA-Entities,
- Cache von Lazy Proxies,
- Cache von Objekten mit sensitiven Feldern.

Empfohlen:

- DTOs,
- records,
- stabile Cache-Value-Klassen,
- JSON mit kontrolliertem ObjectMapper,
- explizite Version im Cache-Key oder im Value bei langlebigen Einträgen.

Beispiel:

```java
public record CachedProductV1(
        String id,
        String name,
        String category,
        BigDecimal price,
        String currency
) {}
```

---

## 9. Cache-Key-Design

### 9.1 Grundregel

Der Cache-Key muss alle Eingaben enthalten, die das Ergebnis beeinflussen.

Schlecht:

```java
@Cacheable(value = "offers", key = "#merchantId")
public List<OfferDto> findOffers(
        MerchantId merchantId,
        ChannelId channelId,
        Locale locale,
        CustomerSegment segment) {
    ...
}
```

Problem: `channelId`, `locale` und `segment` beeinflussen das Ergebnis, fehlen aber im Key. Nutzer können falsche Angebote sehen.

### 9.2 Gute Anwendung

```java
@Cacheable(
        value = "offers",
        key = "T(String).join(':', #tenantId.value(), #merchantId.value(), #channelId.value(), #locale.toLanguageTag(), #segment.name())"
)
public List<OfferDto> findOffers(
        TenantId tenantId,
        MerchantId merchantId,
        ChannelId channelId,
        Locale locale,
        CustomerSegment segment) {
    ...
}
```

Noch besser: zentraler Key-Generator.

```java
@Component("offerCacheKeyGenerator")
public class OfferCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        var tenantId = (TenantId) params[0];
        var merchantId = (MerchantId) params[1];
        var channelId = (ChannelId) params[2];
        var locale = (Locale) params[3];
        var segment = (CustomerSegment) params[4];

        return "v1:tenant:%s:merchant:%s:channel:%s:locale:%s:segment:%s"
                .formatted(
                        tenantId.value(),
                        merchantId.value(),
                        channelId.value(),
                        locale.toLanguageTag(),
                        segment.name()
                );
    }
}
```

```java
@Cacheable(value = "offers", keyGenerator = "offerCacheKeyGenerator")
public List<OfferDto> findOffers(
        TenantId tenantId,
        MerchantId merchantId,
        ChannelId channelId,
        Locale locale,
        CustomerSegment segment) {
    ...
}
```

### 9.3 Key-Regeln

| Regel | Details/Erklärung |
|---|---|
| Tenant immer aufnehmen | In SaaS-Systemen ist Tenant Teil des Keys. |
| User nur wenn wirklich personalisiert | sonst explodiert Kardinalität. |
| Locale aufnehmen | wenn Texte, Währungen oder Formate variieren. |
| Version aufnehmen | bei Value-Strukturänderung. |
| Filter aufnehmen | wenn sie Ergebnis beeinflussen. |
| Sortierung aufnehmen | wenn Reihenfolge relevant ist. |
| Page aufnehmen | bei Pagination. |
| Keine sensitiven Rohwerte | E-Mail, Token, IBAN nicht im Klartext als Key. |
| Stabil und lesbar | Redis-Keys sollen debugbar sein. |
| Länge begrenzen | sehr lange Filter ggf. hashen. |

### 9.4 Sensitive Keys

Schlecht:

```text
user-permissions:max.mustermann@example.com
```

Besser:

```text
user-permissions:v1:user:7f3a2...
```

Bei Hashing: HMAC statt einfachem Hash, wenn Rückschlüsse auf sensible Eingaben möglich sind.

---

## 10. TTL und Eviction

### 10.1 Grundregel

Jeder Cache braucht TTL oder eine klar begründete alternative Eviction-Strategie.

Nicht erlaubt:

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=100000
```

ohne Expiration, wenn Daten sich ändern können.

Besser:

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m,recordStats
```

### 10.2 TTL-Orientierung

| Datentyp | Empfohlene TTL | Details/Erklärung |
|---|---:|---|
| Produktdetails | 5–30 Minuten | abhängig von Änderungsfrequenz |
| Kategorien | 30–120 Minuten | selten geändert |
| Länder-/Referenzdaten | Stunden bis Tage | bei kontrollierter Invalidierung |
| Wechselkurse | Minuten bis Stunden | externe Quelle beachten |
| Feature-Konfiguration | Sekunden bis Minuten | schnelle Rollout-Wirkung nötig |
| Negative Cache für Missing Data | 10–60 Sekunden | kurze TTL, damit neue Daten sichtbar werden |
| Reports | Minuten bis Stunden | fachliche Aktualitätsanforderung |
| Berechtigungen | normalerweise nicht cachen | nur mit sehr kurzer TTL und klarer Invalidation |
| Tenant-Konfiguration | kurz bis mittel | Tenant im Key, Invalidation bei Änderung |
| Tokens/Credentials | nicht cachen | Security-Risiko |

### 10.3 Redis Eviction

Redis kann Keys automatisch entfernen, wenn ein Speicherlimit überschritten wird und eine Eviction Policy gesetzt ist. Wichtig: TTL und Eviction sind nicht dasselbe.

- TTL: Ein Key läuft nach definierter Zeit ab.
- Eviction: Redis entfernt Keys unter Speicherdruck nach Policy.

Für Cache-Use-Cases sind sinnvolle Eviction Policies wie `allkeys-lru`, `allkeys-lfu` oder TTL-basierte Varianten zu prüfen. Die Wahl hängt davon ab, ob Redis ausschließlich als Cache genutzt wird oder auch persistente Daten enthält.

Regel:

```text
Ein Redis-Cache ohne maxmemory-Strategie und ohne TTL ist ein Betriebsrisiko.
```

---

## 11. Cache-Invalidierung

### 11.1 Grundproblem

Cache-Invalidierung ist schwierig, weil Daten sich an mehreren Stellen ändern können. Deshalb muss jede Cache-Einführung eine Invalidierungsstrategie enthalten.

### 11.2 Strategien

| Strategie | Details/Erklärung | Einsatz |
|---|---|---|
| TTL-only | Daten laufen automatisch ab. | wenn leichte Veraltung akzeptiert ist |
| Explicit Evict | Schreiboperation löscht Eintrag. | einzelne Entities |
| CachePut | Schreiboperation aktualisiert Eintrag. | Update liefert vollständigen neuen Wert |
| All Entries Evict | gesamter Cache wird geleert. | Bulk Import, Admin-Änderung |
| Event-based Evict | Event löscht relevante Keys. | verteilte Systeme |
| Versioned Key | neuer Key durch Versionswechsel. | Struktur-/Regeländerung |
| Write-through | Cache wird beim Schreiben aktualisiert. | klare Schreibpfade |
| No Cache | wenn Invalidierung unklar ist. | kritische Konsistenz |

### 11.3 Gute Anwendung

```java
@Service
@CacheConfig(cacheNames = "products")
public class ProductApplicationService {

    @CachePut(key = "#result.id().value()")
    @Transactional
    public ProductDto update(ProductId id, UpdateProductCommand command) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.update(command);

        return productMapper.toDto(productRepository.save(product));
    }

    @CacheEvict(key = "#id.value()")
    @Transactional
    public void delete(ProductId id) {
        productRepository.deleteById(id);
    }
}
```

### 11.4 Bulk-Invalidierung

```java
@CacheEvict(cacheNames = "products", allEntries = true)
@Transactional
public void importProducts(ProductImportFile file) {
    productImporter.importFile(file);
}
```

Bulk-Invalidierung ist einfach, aber teuer. Sie ist akzeptabel, wenn Bulk-Änderungen selten sind.

---

## 12. Cache-Stampede verhindern

### 12.1 Problem

Cache-Stampede entsteht, wenn ein populärer Cache-Eintrag abläuft und viele Requests gleichzeitig denselben Wert neu berechnen oder laden.

Folge:

- Datenbankspike,
- Downstream-Überlastung,
- Thread-Pool-Blockade,
- Latenzanstieg,
- mögliche Kaskadenausfälle.

### 12.2 Spring Cache `sync = true`

```java
@Cacheable(value = "reports", key = "#reportId.value()", sync = true)
public ReportDto generateReport(ReportId reportId) {
    return reportGenerator.generate(reportId);
}
```

`sync = true` sorgt dafür, dass bei parallelem Cache-Miss nur ein Thread den Wert berechnet, während andere warten. Ob und wie gut das wirkt, hängt vom Cache-Provider ab.

### 12.3 Caffeine Refresh

```java
@Bean
public LoadingCache<ProductId, ProductDto> productCache() {
    return Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .refreshAfterWrite(Duration.ofMinutes(8))
            .recordStats()
            .build(this::loadProduct);
}
```

`refreshAfterWrite` erlaubt, Werte nach Ablauf der Refresh-Zeit neu zu laden, während der alte Wert weiter nutzbar bleibt. Das reduziert harte Miss-Spitzen.

### 12.4 AsyncLoadingCache

```java
@Bean
public AsyncLoadingCache<ProductId, ProductDto> asyncProductCache() {
    return Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .refreshAfterWrite(Duration.ofMinutes(8))
            .recordStats()
            .buildAsync((productId, executor) ->
                    CompletableFuture.supplyAsync(() -> loadProduct(productId), executor));
}
```

Async Loading ist sinnvoll, wenn Neuladen teuer ist und nicht den Request-Thread blockieren soll.

### 12.5 Weitere Schutzmaßnahmen

- Jitter auf TTL,
- Locking pro Key,
- Refresh ahead,
- stale-while-revalidate,
- Rate Limiting,
- Bulkhead für Loader,
- Fallback auf stale data,
- Pre-Warming nach Deploy.

---

## 13. Was nicht gecacht werden darf

### 13.1 Verbotene oder sehr kritische Cache-Kandidaten

| Datentyp | Bewertung | Begründung |
|---|---|---|
| Passwörter | verboten | dürfen nicht im Cache liegen |
| Raw Tokens | verboten | Session-/Token-Diebstahlrisiko |
| API Keys | verboten | Secret Leakage |
| Kreditkartendaten | verboten | Compliance-/Security-Risiko |
| IBAN im Klartext | verboten | personenbezogen und hochsensibel |
| Berechtigungen | kritisch | Änderungen müssen schnell wirken |
| Rollen | kritisch | Rechteentzug darf nicht verspätet greifen |
| personenbezogene Profile | kritisch | PII, DSGVO, Tenant-Isolation |
| Suchergebnisse pro User | meist vermeiden | hohe Kardinalität, niedrige Hit Rate |
| Warenkorb | meist vermeiden | stark veränderlich |
| Lagerbestand | meist vermeiden | Konsistenz kritisch |
| Kontostand | verboten/streng | transaktionale Aktualität |
| Payment-Status | kritisch | nur mit klarer fachlicher Semantik |
| Tenant-übergreifende Listen | verboten ohne Tenant-Key | Cross-Tenant-Risiko |

### 13.2 Berechtigungen cachen

Standard: Nicht cachen.

Wenn zwingend notwendig:

- sehr kurze TTL,
- explizite Invalidierung bei Rollenänderung,
- Tenant/User im Key,
- keine permissive Fallback-Strategie,
- Tests für Rechteentzug,
- Audit und Monitoring,
- konservative Fehlerbehandlung.

Schlecht:

```java
@Cacheable("permissions")
public Set<Permission> permissionsOf(UserId userId) {
    ...
}
```

Besser, wenn zwingend nötig:

```java
@Cacheable(
        value = "permissions",
        key = "'v1:tenant:' + #tenantId.value() + ':user:' + #userId.value()",
        unless = "#result == null || #result.empty"
)
public PermissionSet permissionsOf(TenantId tenantId, UserId userId) {
    ...
}
```

Zusätzlich: Eviction bei Rollenänderung.

---

## 14. SaaS- und Tenant-Caching

### 14.1 Grundregel

In SaaS-Systemen muss Tenant-Kontext Teil des Cache-Designs sein.

Schlecht:

```java
@Cacheable(value = "campaigns", key = "#campaignId.value()")
public CampaignDto findCampaign(CampaignId campaignId) {
    ...
}
```

Wenn Campaign IDs nicht global eindeutig oder Zugriff tenantabhängig ist, droht Cross-Tenant-Leakage.

Gut:

```java
@Cacheable(
        value = "campaigns",
        key = "'v1:tenant:' + #tenantId.value() + ':campaign:' + #campaignId.value()"
)
public CampaignDto findCampaign(TenantId tenantId, CampaignId campaignId) {
    ...
}
```

### 14.2 Tenant-Regeln

1. TenantId gehört in den Key.
2. UserId gehört nur in den Key, wenn Ergebnis userabhängig ist.
3. Rechteabhängige Ergebnisse werden normalerweise nicht gecacht.
4. Locale, Channel, Segment und Standort gehören in den Key, wenn sie Ergebnis beeinflussen.
5. Cross-Tenant-Tests müssen falsche Cache-Hits verhindern.
6. Eviction muss tenantbezogen möglich sein.
7. Logs dürfen Cache-Keys nicht mit PII anreichern.

### 14.3 Tenantweite Invalidierung

```java
public void evictTenantCampaigns(TenantId tenantId) {
    // Bei Redis: Keys über Prefix vermeiden, wenn KEYS produktiv teuer wäre.
    // Besser: Key-Index pro Tenant oder Versioned Tenant Cache Namespace.
}
```

Besser: versionierter Namespace.

```text
campaigns:v3:tenant:t-123:campaign:c-456
```

Wenn Tenant invalidiert wird, wird Tenant-Cache-Version erhöht. Alte Keys laufen per TTL aus.

---

## 15. Cache und Transaktionen

### 15.1 Problem

Cache-Update und Datenbank-Commit können auseinanderlaufen.

Schlecht:

```java
@CachePut(key = "#result.id()")
@Transactional
public ProductDto update(ProductId id, UpdateProductCommand command) {
    var product = productRepository.findById(id).orElseThrow();
    product.update(command);
    return productMapper.toDto(productRepository.save(product));
}
```

Wenn die Transaktion später fehlschlägt, kann der Cache bereits aktualisiert worden sein, abhängig von Proxy-/Transaction-Verhalten und CacheManager.

### 15.2 Empfehlungen

- Cache-Updates nach erfolgreichem Commit auslösen.
- `transactionAware()` bei RedisCacheManager prüfen.
- Für kritische Fälle Event nach Commit verwenden.
- Bei Unsicherheit eher evicten statt putten.
- Keine fachlich kritische Konsistenz auf Cache stützen.

Beispiel mit Transactional Event:

```java
@Transactional
public ProductDto update(ProductId id, UpdateProductCommand command) {
    var product = productRepository.findById(id).orElseThrow();
    product.update(command);
    var saved = productRepository.save(product);

    eventPublisher.publishEvent(new ProductUpdatedEvent(saved.id()));

    return productMapper.toDto(saved);
}
```

```java
@Component
public class ProductCacheInvalidationListener {

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductUpdated(ProductUpdatedEvent event) {
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict(event.productId().value());
        }
    }
}
```

---

## 16. Spring Cache Proxy-Fallen

Spring Cache arbeitet wie viele Spring-Features über Proxies. Daraus folgen typische Fallen.

### 16.1 Self-Invocation

Schlecht:

```java
@Service
public class ProductService {

    public ProductDto find(ProductId id) {
        return findCached(id); // Self-invocation: Cache-Proxy wird umgangen
    }

    @Cacheable("products")
    public ProductDto findCached(ProductId id) {
        ...
    }
}
```

Der interne Methodenaufruf läuft nicht über den Spring-Proxy.

Besser:

- Cache-Methode in eigene Bean auslagern,
- öffentlichen Service über Proxy aufrufen,
- Architektur vereinfachen.

```java
@Service
public class ProductCacheService {

    @Cacheable(value = "products", key = "#id.value()")
    public ProductDto findCached(ProductId id) {
        ...
    }
}
```

### 16.2 Private Methoden

`@Cacheable` auf private Methoden wirkt nicht wie erwartet, weil private Methoden nicht über Spring AOP abgefangen werden.

Nicht erlaubt:

```java
@Cacheable("products")
private ProductDto load(ProductId id) {
    ...
}
```

### 16.3 Final Classes/Methods

Je nach Proxy-Art können final Klassen oder final Methoden problematisch sein. Caching-Annotationen gehören auf Spring-Bean-Methoden, die tatsächlich proxied werden können.

---

## 17. Fehlerbehandlung bei Cache-Ausfall

### 17.1 Lokaler Cache

Caffeine-Ausfall ist selten ein externer Ausfall, da er in der JVM läuft. Fehler entstehen eher im Loader.

Regel: Loader-Fehler dürfen nicht unbemerkt zu falschen Werten führen.

### 17.2 Redis-Ausfall

Redis ist ein externes System. Wenn Redis ausfällt:

- Darf der Service weiterlaufen?
- Wird auf Datenbank zurückgefallen?
- Gibt es Timeouts?
- Gibt es Circuit Breaker?
- Gibt es Bulkheads?
- Wird Redis-Ausfall geloggt und gemessen?
- Wird bei Redis-Ausfall kein Security-Bypass erzeugt?

Beispiel: Redis als Performance-Cache

```text
Redis down → Daten aus DB laden → Warnung/Metrik → Service läuft langsamer weiter.
```

Beispiel: Redis als Token-Blacklist

```text
Redis down → nicht einfach Zugriff erlauben.
```

Security-Caches müssen fail-closed oder bewusst fachlich behandelt werden.

---

## 18. Monitoring und Metriken

### 18.1 Pflichtmetriken

| Metrik | Bedeutung |
|---|---|
| Hit Rate | Anteil der Treffer |
| Miss Rate | Anteil der Misses |
| Eviction Count | verdrängte Einträge |
| Cache Size | aktuelle Größe |
| Load Duration | Dauer für Nachladen |
| Load Failures | Fehler beim Laden |
| Redis Latency | Netzwerk-/Serverlatenz |
| Redis Memory | Speicherverbrauch |
| Redis Evicted Keys | Eviction unter Druck |
| Key Cardinality | Anzahl unterschiedlicher Keys |
| Stampede-Indikatoren | viele parallele Misses |

### 18.2 Caffeine Stats

Caffeine muss mit `recordStats` betrieben werden, wenn Metriken relevant sind.

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m,recordStats
```

Micrometer kann Cache-Metriken exportieren. In Spring Boot werden viele Cache-Metriken über Actuator/Micrometer sichtbar, abhängig vom CacheManager.

### 18.3 Warnsignale

| Signal | Mögliche Ursache |
|---|---|
| Hit Rate < 50% | falscher Cache-Kandidat oder Key zu spezifisch |
| hohe Evictions | Cache zu klein oder TTL/Policy falsch |
| hohe Miss-Spitzen | Stampede |
| Redis Latenz steigt | Netzwerk/Redis überlastet |
| Cache wächst stark | fehlende Maximalgröße |
| falsche Daten | Key unvollständig oder Invalidierung defekt |
| viele Nullwerte | negatives Caching falsch |
| Rechte wirken verspätet | Permission-Cache gefährlich |

---

## 19. Cache-Warming

Cache-Warming bedeutet, wichtige Daten vor der ersten Nutzeranfrage zu laden.

Sinnvoll für:

- kleine Referenzdaten,
- Kategorien,
- häufig genutzte Konfiguration,
- Feature-/Tenant-Konfiguration,
- statische Lookup-Daten.

Nicht sinnvoll für:

- riesige Produktkataloge,
- userpersonalisierte Ergebnisse,
- volatile Daten,
- Daten ohne bekannte Zugriffsmuster.

Beispiel:

```java
@Component
public class ReferenceDataCacheWarmer implements ApplicationRunner {

    private final CategoryQueryService categoryQueryService;

    @Override
    public void run(ApplicationArguments args) {
        categoryQueryService.findAllActiveCategories();
    }
}
```

Wichtig: Cache-Warming darf Startup nicht unkontrolliert verlangsamen. Bei kritischen Services ist asynchrones oder gestaffeltes Warming vorzuziehen.

---

## 20. HTTP-Caching ist ein anderes Thema

Application Cache und HTTP Cache sind nicht identisch.

HTTP-Caching nutzt Header wie:

- `Cache-Control`,
- `ETag`,
- `Last-Modified`,
- `Expires`,
- `Vary`.

Beispiel:

```java
@GetMapping("/api/v1/categories")
public ResponseEntity<List<CategoryDto>> categories() {
    return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
            .body(categoryService.findAll());
}
```

Achtung:

- personalisierte Antworten nicht public cachen,
- Authorization und Cache-Control korrekt kombinieren,
- `Vary` bei Sprache, Encoding oder Auth-Kontext beachten,
- Browser/CDN-Cache ist nicht durch `@CacheEvict` invalidiert.

Diese Guideline behandelt primär Application-Level-Caching.

---

## 21. Gute Cache-Kandidaten

| Kandidat | Warum geeignet | Typischer Cache |
|---|---|---|
| Produktkategorien | selten geändert, oft gelesen | Caffeine/Redis |
| Länderlisten | sehr stabil | Caffeine |
| Versandzonen | selten geändert, teuer zu berechnen | Caffeine/Redis |
| Wechselkurse | externe Quelle, definierte Aktualität | Redis/Caffeine mit TTL |
| Feature-Konfigurationsleseoperationen | oft gelesen | kurzer Caffeine Cache |
| Merchant Public Profile | oft gelesen, selten geändert | Redis mit Tenant-Key |
| Angebotskatalog pro Stadt/Kanal | read-heavy | Redis mit Key-Design |
| berechnete Reports | teuer | Redis/Caffeine mit Stampede-Schutz |
| externe API-Lookups | teuer/langsam | Redis mit TTL und Circuit Breaker |
| Tax Rules | selten geändert, fachlich versionierbar | Caffeine |

---

## 22. Schlechte Cache-Kandidaten

| Kandidat | Warum problematisch |
|---|---|
| Warenkorb | häufig geändert, userbezogen |
| Lagerbestand | Aktualität kritisch |
| Payment-Status | fachlich kritisch |
| Berechtigungen | Rechteentzug muss schnell wirken |
| komplette User-Profile | PII und Kardinalität |
| Suchergebnisse mit vielen Filtern | Key-Explosion |
| JPA-Entities | Lazy Proxies, Serialisierung, Kopplung |
| transaktionale Kontostände | starke Konsistenz |
| Admin-Konfiguration ohne Eviction | veraltete Steuerung |
| sehr schnelle DB-Lookups | Cache overhead lohnt nicht |

---

## 23. Anti-Patterns

### 23.1 Cache als Reparatur für N+1

Schlecht:

```text
Repository erzeugt 101 Queries, also cachen wir das Ergebnis.
```

Besser:

```text
N+1 mit Fetch Join, EntityGraph oder Projection beheben.
Dann messen, ob Cache noch nötig ist.
```

### 23.2 Cache ohne TTL

Ein Cache ohne TTL ist fast immer ein zukünftiger Incident.

### 23.3 Unvollständiger Cache-Key

Wenn `locale`, `tenantId`, `currency`, `channel`, `segment` oder `permissions` das Ergebnis beeinflussen, müssen sie berücksichtigt werden.

### 23.4 Redis für alles

Redis ist nicht kostenlos. Jeder Zugriff kostet Netzwerk, Serialisierung, Betrieb und Fehlerbehandlung.

### 23.5 Caching von Entities

Cache DTOs oder explizite Cache-Values, nicht JPA-Entities.

### 23.6 Berechtigungen langfristig cachen

Rechteentzug muss schnell wirken. Langer Permission-Cache ist gefährlich.

### 23.7 Cache-Invalidierung vergessen

Jede Schreiboperation muss prüfen, ob ein Cache betroffen ist.

### 23.8 Cache mit unbounded Key Cardinality

User × Filter × Page × Sort × Locale × Segment kann Millionen Keys erzeugen.

### 23.9 Silent Cache Fallback

Wenn Redis ausfällt und niemand es merkt, ist Observability unzureichend.

### 23.10 Cache-Warming mit Produktionslast beim Startup

Wenn alle Instanzen beim Deploy den gesamten Katalog laden, erzeugt Warming selbst den Ausfall.

---

## 24. Tests

### 24.1 Unit-Test für Key-Design

```java
@Test
void generate_includesTenantLocaleAndSegment() throws Exception {
    var generator = new OfferCacheKeyGenerator();

    var key = generator.generate(
            new Object(),
            OfferQueryService.class.getMethod(
                    "findOffers",
                    TenantId.class,
                    MerchantId.class,
                    ChannelId.class,
                    Locale.class,
                    CustomerSegment.class),
            new TenantId("t1"),
            new MerchantId("m1"),
            new ChannelId("leer"),
            Locale.GERMANY,
            CustomerSegment.NEW_CUSTOMER
    );

    assertThat(key.toString())
            .contains("tenant:t1")
            .contains("merchant:m1")
            .contains("channel:leer")
            .contains("locale:de-DE")
            .contains("segment:NEW_CUSTOMER");
}
```

### 24.2 Integrationstest für Cacheable

```java
@SpringBootTest
class ProductCacheIntegrationTest {

    @Autowired ProductQueryService productQueryService;
    @MockBean ProductRepository productRepository;

    @Test
    void findById_usesCache_onSecondCall() {
        var id = new ProductId("p1");
        when(productRepository.findById(id))
                .thenReturn(Optional.of(product("p1")));

        productQueryService.findById(id);
        productQueryService.findById(id);

        verify(productRepository, times(1)).findById(id);
    }
}
```

### 24.3 Eviction-Test

```java
@Test
void update_evictsProductCache() {
    var id = new ProductId("p1");

    productQueryService.findById(id);
    productApplicationService.update(id, updateCommand());
    productQueryService.findById(id);

    verify(productRepository, times(2)).findById(id);
}
```

### 24.4 Cross-Tenant-Test

```java
@Test
void findCampaign_doesNotReuseCacheAcrossTenants() {
    var campaignId = new CampaignId("c1");

    campaignQueryService.findCampaign(new TenantId("tenant-a"), campaignId);
    campaignQueryService.findCampaign(new TenantId("tenant-b"), campaignId);

    verify(campaignRepository).findByIdAndTenantId(campaignId, new TenantId("tenant-a"));
    verify(campaignRepository).findByIdAndTenantId(campaignId, new TenantId("tenant-b"));
}
```

---

## 25. Review-Checkliste

| Aspekt | Details/Erklärung | Beispiel | Entscheidung |
|---|---|---|---|
| Messung | Gibt es Latenz-/Lastdaten? | P95 800 ms vor Cache | Pflicht |
| Kandidat | Ist das Ergebnis cachewürdig? | read-heavy, selten geändert | Pflicht |
| Key | Enthält Key alle Einflussfaktoren? | tenant, locale, filter | Pflicht |
| TTL | Ist TTL gesetzt und begründet? | 5 Minuten | Pflicht |
| Größe | Ist Maximalgröße gesetzt? | maximumSize | Pflicht bei lokalem Cache |
| Eviction | Wird bei Änderung invalidiert? | `@CacheEvict` | Pflicht |
| Stampede | Gibt es Schutz bei Hot Keys? | `sync=true`, refresh | Pflicht bei teuren Loads |
| Security | Werden keine Secrets/PII gecacht? | keine Tokens | Pflicht |
| Tenant | Ist Tenant-Isolation garantiert? | TenantId im Key | Pflicht bei SaaS |
| Serialisierung | Sind Cache Values stabil? | DTO/Record | Pflicht bei Redis |
| Transaktion | Passt Cache-Update zum Commit? | after commit evict | Prüfen |
| Failure | Was passiert bei Redis-Ausfall? | Fallback/Timeout | Pflicht |
| Monitoring | Sind Hit Rate und Fehler sichtbar? | Micrometer | Pflicht |
| Tests | Gibt es Cache-/Eviction-Tests? | Integrationstest | Soll |
| Dokumentation | Zweck und TTL dokumentiert? | Cache-Katalog | Soll |

---

## 26. Cache-Katalog

Jeder produktive Cache wird im Repository dokumentiert.

Beispiel:

```markdown
# Cache-Katalog

| Cache | Backend | Zweck | Key | TTL | Max Size | Invalidierung | Owner |
|---|---|---|---|---|---|---|---|
| products | Caffeine | Produktdetails | tenant:product | 10m | 1000 | ProductUpdatedEvent | Catalog Team |
| categories | Caffeine | Kategoriebaum | tenant:locale | 1h | 100 | Admin Update | Catalog Team |
| offers | Redis | Angebotsliste je Kanal | tenant:channel:segment:locale | 5m | Redis maxmemory | OfferChangedEvent | Offers Team |
```

Der Cache-Katalog verhindert unsichtbare Performance-Logik.

---

## 27. Automatisierbare Prüfungen

Mögliche Regeln:

```text
- Keine direkte Verwendung von HashMap als Cache in Services.
- Keine @Cacheable auf Methoden, die Credentials oder Tokens liefern.
- Keine @Cacheable auf private Methoden.
- Cache-Namen müssen im Cache-Katalog stehen.
- Redis-Caches müssen TTL-Konfiguration besitzen.
- Caffeine-Caches müssen maximumSize besitzen.
- tenantgebundene Cache-Keys müssen TenantId enthalten.
- Produktive Manifeste müssen Redis-Timeouts definieren.
- Cache-Metriken müssen für produktive Caches aktiv sein.
```

Beispiel ArchUnit:

```java
@ArchTest
static final ArchRule services_should_not_use_hashmap_as_cache =
        noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                .should().haveRawType(HashMap.class)
                .because("Manuelle HashMap-Caches haben keine TTL, keine Eviction und kein Monitoring.");
```

---

## 28. Migration bestehender Caches

Migration erfolgt schrittweise:

1. Bestehende Caches inventarisieren.
2. Cache-Zweck dokumentieren.
3. TTL und Maximalgröße prüfen.
4. Cache-Key auf Tenant/User/Locale/Filter prüfen.
5. Sicherheitskritische Caches identifizieren.
6. Manuelle Maps ersetzen.
7. JPA-Entity-Caches entfernen.
8. Redis-Serialisierung standardisieren.
9. Monitoring aktivieren.
10. Tests für Eviction und Cross-Tenant ergänzen.
11. Cache-Katalog pflegen.
12. Überflüssige Caches löschen.

---

## 29. Ausnahmen

Ausnahmen sind zulässig, wenn:

- ein Cache bewusst nur für lokale Entwicklungswerkzeuge existiert,
- ein temporärer Cache für einen Spike verwendet wird,
- ein Framework intern caching nutzt,
- eine externe Library Cache-Verhalten kapselt,
- ein Cache ohne TTL fachlich vollständig immutable Daten enthält,
- ein Security-relevanter Cache durch zusätzliche Kontrollen abgesichert ist.

Ausnahmen müssen dokumentiert sein. Besonders Caches ohne TTL oder sicherheitsrelevante Caches benötigen explizite Begründung und Review.

---

## 30. Definition of Done

Ein Cache erfüllt diese Richtlinie, wenn:

1. der Performance- oder Resilience-Grund dokumentiert ist,
2. der Cache-Kandidat geeignet ist,
3. der Cache-Key vollständig ist,
4. Tenant-Kontext berücksichtigt ist,
5. keine Secrets oder ungeeigneten PII-Daten gecacht werden,
6. TTL gesetzt ist,
7. Maximalgröße oder Redis Memory Policy definiert ist,
8. Invalidierungsstrategie vorhanden ist,
9. Stampede-Schutz bei teuren Loads berücksichtigt ist,
10. Redis-Ausfallverhalten definiert ist, falls Redis genutzt wird,
11. Cache-Values stabil serialisierbar sind,
12. JPA-Entities nicht direkt gecacht werden,
13. Metriken sichtbar sind,
14. Tests für Cache-Hit und Eviction existieren, soweit sinnvoll,
15. Cache im Cache-Katalog dokumentiert ist,
16. der Cache nicht als Ersatz für schlechte Queries oder fehlende Indexe dient.

---

## 31. Quellen und weiterführende Literatur

- Spring Framework Reference — Cache Abstraction: https://docs.spring.io/spring-framework/reference/integration/cache.html
- Spring Framework Reference — Declarative Annotation-based Caching: https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html
- Spring Boot Reference — Caching: https://docs.spring.io/spring-boot/reference/io/caching.html
- Spring Data Redis Reference — Redis Cache: https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html
- Caffeine GitHub Repository: https://github.com/ben-manes/caffeine
- Caffeine Javadoc — AsyncCacheLoader / refreshAfterWrite: https://www.javadoc.io/doc/com.github.ben-manes.caffeine/caffeine/latest/com.github.benmanes.caffeine/com/github/benmanes/caffeine/cache/AsyncCacheLoader.html
- Redis Documentation — Key eviction: https://redis.io/docs/latest/develop/reference/eviction/
- Redis Documentation — TTL command: https://redis.io/docs/latest/commands/ttl/
- Micrometer Documentation — Cache instrumentation: https://docs.micrometer.io/
- OWASP Cheat Sheet Series — Logging Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
