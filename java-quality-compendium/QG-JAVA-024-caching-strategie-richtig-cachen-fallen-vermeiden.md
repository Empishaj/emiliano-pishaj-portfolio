# ADR-024 — Caching-Strategie: Richtig cachen, Fallen vermeiden

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Java       | 21 · Spring Boot 3.x · Caffeine · Redis           |
| Datum      | 2025-03-24                                       |
| Kategorie  | Performance / Architektur                         |

---

## Kontext & Problem

Caching löst Performanceprobleme — aber falsch eingesetzt schafft es neue: veraltete Daten, Cache-Stampede, Speicherlecks, schwer debuggbare Inkonsistenzen. Die erste Regel des Cachings lautet: "Do not cache". Erst messen, dann cachen. Dieses ADR definiert wann, wie und wie nicht gecacht wird.

---

## Wann cachen? — Die Entscheidungsmatrix

| Kriterium                         | Cache sinnvoll | Cache vermeiden |
|-----------------------------------|----------------|-----------------|
| Leseintensiv, selten geändert     | ✅             |                 |
| Teuer zu berechnen (CPU/I/O)      | ✅             |                 |
| Toleriert leicht veraltete Daten  | ✅             |                 |
| Schreibintensiv, oft geändert     |                | ❌              |
| Stark personalisiert (pro User)   |                | ❌ (hoch kardinal) |
| Transaktional konsistent nötig    |                | ❌              |
| Sicherheitsrelevant (Permissions) |                | ❌              |

---

## Regel 1 — Cache-Abstraktionen: Spring Cache + Caffeine/Redis

### Schlecht — manuelles Cache-Handling

```java
@Service
public class ProductService {

    // Manuelles HashMap-Cache — kein Eviction, kein TTL, kein Distributed Cache
    private final Map<Long, Product> cache = new HashMap<>();

    public Product findById(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id); // Kein TTL — Daten werden nie alt
        }
        var product = productRepository.findById(id).orElseThrow();
        cache.put(id, product); // Speicherleck: wächst unbegrenzt
        return product;
    }

    public void updateProduct(Long id, UpdateProductCommand cmd) {
        productRepository.save(...);
        // Cache-Invalidierung vergessen! → veraltete Daten
    }
}
```

### Gut — Spring Cache mit @Cacheable / @CacheEvict

```java
// application.yml (Caffeine für lokalen Cache):
// spring:
//   cache:
//     type: caffeine
//     caffeine:
//       spec: maximumSize=1000,expireAfterWrite=5m,recordStats

// application.yml (Redis für verteilten Cache):
// spring:
//   cache:
//     type: redis
//   data:
//     redis:
//       time-to-live: 300  # 5 Minuten

@Service
@CacheConfig(cacheNames = "products") // Default-Cache-Name für alle Methoden
public class ProductService {

    // ①  Lesen: Cache-First
    @Cacheable(key = "#id", unless = "#result == null")
    public ProductDto findById(Long id) {
        // Wird nur aufgerufen wenn Cache-Miss
        return productRepository.findById(id)
            .map(productMapper::toDto)
            .orElse(null); // unless="#result == null" verhindert null-Caching
    }

    // ② Schreiben: Cache aktualisieren
    @CachePut(key = "#result.id()") // Cache direkt mit neuem Wert befüllen — kein Extra-Read
    @Transactional
    public ProductDto update(Long id, UpdateProductCommand command) {
        var product = productRepository.findById(id).orElseThrow();
        product.update(command);
        return productMapper.toDto(productRepository.save(product));
    }

    // ③ Löschen: Cache invalidieren
    @CacheEvict(key = "#id")
    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    // ④ Ganzen Cache leeren (z. B. nach Bulk-Import)
    @CacheEvict(allEntries = true)
    public void invalidateAll() {
        log.info("Product cache vollständig geleert");
    }
}
```

---

## Regel 2 — TTL: Immer setzen, nie vergessen

```java
// Konfiguration: verschiedene TTLs für verschiedene Caches
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        var builder = Caffeine.newBuilder();

        return CaffeineCacheManager.builder()
            .initialCapacity(100)
            .maximumSize(1_000)
            // Verschiedene TTLs pro Cache:
            .caches(Map.of(
                "products",    Caffeine.newBuilder().expireAfterWrite(5, MINUTES).maximumSize(1000),
                "categories",  Caffeine.newBuilder().expireAfterWrite(1, HOURS).maximumSize(100),
                "userSessions",Caffeine.newBuilder().expireAfterAccess(30, MINUTES).maximumSize(10_000),
                "exchangeRates",Caffeine.newBuilder().expireAfterWrite(1, HOURS).maximumSize(50)
            ))
            .build();
    }
}
```

---

## Regel 3 — Cache-Stampede verhindern

Das Cache-Stampede-Problem: Cache läuft ab, 1000 Requests treffen gleichzeitig auf Cache-Miss, alle holen dieselben Daten aus der DB → Datenbanküberlastung.

```java
// ❌ Ohne Schutz: Cache-Stampede möglich
@Cacheable("expensive-report")
public Report generateReport() {
    return reportService.generate(); // 10 Sekunden Berechnung
    // Wenn Cache abläuft: 100 parallele Threads berechnen alle gleichzeitig
}

// ✅ Mit Caffeine AsyncLoadingCache: automatischer Stampede-Schutz
@Bean
public AsyncLoadingCache<Long, ProductDto> productCache() {
    return Caffeine.newBuilder()
        .maximumSize(1_000)
        .expireAfterWrite(5, MINUTES)
        .refreshAfterWrite(4, MINUTES) // Background-Refresh 1 Minute vor Ablauf!
        // Nur ein Thread berechnet bei Cache-Miss, andere warten
        .buildAsync(id -> productRepository.findById(id)
            .map(productMapper::toDto)
            .orElse(null));
}

// Oder: @Cacheable mit Synchronisation
@Cacheable(value = "products", sync = true) // sync=true: nur ein Thread bei Cache-Miss
public ProductDto findById(Long id) { ... }
```

---

## Regel 4 — Was NICHT gecacht werden darf

```java
// ❌ Sicherheitsrelevante Daten: Berechtigungen können sich ändern
@Cacheable("userPermissions") // GEFÄHRLICH: User könnte Rolle verloren haben!
public Set<Permission> getPermissionsForUser(UserId userId) { ... }

// ❌ Personalisierte Daten mit hoher Kardinalität (zu viele Cache-Keys)
@Cacheable(key = "#userId + '-' + #page + '-' + #filters.hashCode()")
// Bei 100.000 Usern × 50 Seiten × viele Filter-Kombinationen = Cache-Überlastung

// ❌ Sensitive Daten (Passwörter, Tokens, PII)
@Cacheable("userCredentials") // Credentials im Cache: Security-Risiko!

// ✅ Was sicher gecacht werden kann:
@Cacheable("productCatalog")     // Selten geändert, nicht personalisiert
@Cacheable("exchangeRates")      // Externe Daten, kurzer TTL
@Cacheable("countryList")        // Referenzdaten, sehr stabiles Änderungsintervall
@Cacheable("shippingZones")      // Selten geändert, teuer zu berechnen
```

---

## Regel 5 — Distributed Caching mit Redis: Serialisierung

```java
// Redis-Serialisierung: JSON statt Java-Serialisierung
@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .disableCachingNullValues()
            .serializeValuesWith(
                // JSON statt Java-Binary-Serialisierung:
                // - Lesbar im Redis-CLI
                // - Kein serialVersionUID-Problem bei Klassen-Änderungen
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
            .cacheDefaults(cacheConfiguration())
            .withCacheConfiguration("products",
                cacheConfiguration().entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration("sessions",
                cacheConfiguration().entryTtl(Duration.ofHours(1)))
            .build();
    }
}
```

---

## Cache-Monitoring

```java
// Caffeine Statistiken via Micrometer (→ ADR-017)
// application.yml:
// spring.cache.caffeine.spec: maximumSize=1000,expireAfterWrite=5m,recordStats

// Metriken automatisch verfügbar:
// cache.gets{cache="products",result="hit"}     → Hits
// cache.gets{cache="products",result="miss"}    → Misses
// cache.size{cache="products"}                  → Aktuelle Größe

// Dashboard-Alert wenn Hit-Rate unter 70% fällt:
// → Cache-Key-Strategie oder TTL anpassen
```

---

## Konsequenzen

**Positiv:** `@Cacheable` + `@CacheEvict` halten Cache-Logik aus dem Business-Code heraus. TTL-Konfiguration verhindert veraltete Daten. Caffeine's `sync=true` und `refreshAfterWrite` lösen das Stampede-Problem elegant.

**Negativ:** Verteilter Cache (Redis) erfordert Netzwerk-Overhead und ist ein weiterer Failure-Point. Cache-Invalidierung bei komplexen Abhängigkeiten ist schwierig — "Cache-Invalidierung ist eines der schwersten Probleme der Informatik".

---

## Tipps

- **Cache zuletzt**: Erst ohne Cache messen, dann gezielt cachen. Falsches Caching verdeckt Design-Probleme.
- **Cache-Key-Design**: Keys müssen alle Variablen enthalten die das Ergebnis beeinflussen — vergessene Parameter führen zu falschen Cache-Treffern.
- **Lokaler Cache vs. Redis**: Lokaler Caffeine-Cache ist 10–100x schneller als Redis-Netzwerkcall — für einzelne Instanzen bevorzugen. Redis wenn Cache über mehrere Instanzen geteilt werden muss.
- **Cache-Warming**: Kritische Caches beim Start befüllen statt kalte Starts zu erleiden.
