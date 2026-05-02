# ADR-104 — Elasticsearch: Volltextsuche & Such-Architektur

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Datenbank · Suche · CQRS                                      |
| Betroffene Teams  | Teams mit Suche-Anforderungen                                 |
| Abhängigkeiten    | ADR-032 (CQRS), ADR-041 (Kafka), ADR-016 (JPA)               |

---

## 1. Kontext & Treiber

### 1.1 Warum PostgreSQL für Suche nicht ausreicht

```
USE CASE: Produktsuche auf E-Commerce-Plattform
  Eingabe: "blaue jeans slim fit"
  Erwartet: Produkte nach Relevanz, Tippfehler-tolerant, schnell

MIT PostgreSQL LIKE-Suche:
  SELECT * FROM products WHERE name LIKE '%blaue jeans%';
  → Kein Index möglich → Full Table Scan (500.000 Produkte → langsam)
  → "jeans blau" → 0 Ergebnisse (falsche Reihenfolge)
  → "blaue Jens" (Tippfehler) → 0 Ergebnisse
  → Keine Relevanz-Sortierung
  → Keine Facetten (Filter: Größe, Farbe, Preis)

MIT PostgreSQL Full-Text Search:
  → ts_vector + GIN-Index → schneller
  → Immer noch keine Fuzzy-Suche
  → Keine Facetten
  → Für komplexe Produktsuche unzureichend

MIT ELASTICSEARCH:
  → Invertierter Index → Millionen Dokumente in Millisekunden
  → Fuzzy-Suche: "jens" findet "jeans"
  → Relevanz-Scoring: beste Treffer zuerst
  → Facetten: Filter-Zählung in einer Query
  → Synonyme: "Hose" findet auch "Jeans"
  ✅ Richtige Werkzeug für Suche
```

---

## 2. Entscheidung

Elasticsearch wird als dedizierter Such-Index für alle Volltextsuch-Anforderungen verwendet. PostgreSQL bleibt die System-of-Record (Source of Truth). Elasticsearch ist das Read-Model der Such-Domäne im CQRS-Pattern (→ ADR-032). Synchronisation erfolgt über Kafka Events.

---

## 3. Architektur: CQRS mit Elasticsearch

```
WRITE-SIDE (System of Record):
  Nutzer erstellt Produkt
  → ProductService → PostgreSQL (Primärdaten)
  → Kafka Event: ProductCreatedEvent

READ-SIDE (Such-Index):
  ProductIndexer konsumiert Event
  → Elasticsearch-Index aktualisieren
  → Suchanfragen laufen NUR gegen Elasticsearch

VORTEILE:
  → Elasticsearch-Schema optimiert für Suche (nicht für CRUD)
  → PostgreSQL-Schema optimiert für Transaktionen
  → Suchindex kann ohne Produktions-Impact neu gebaut werden
  → Eventual Consistency: akzeptabel (Produkt erscheint in ~1s in Suche)
```

---

## 4. Spring Data Elasticsearch

### 4.1 Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("co.elastic.clients:elasticsearch-java:8.12.0")
}
```

```yaml
# application.yml
spring:
  elasticsearch:
    uris: http://elasticsearch:9200
    # Für Production mit TLS:
    # uris: https://elasticsearch:9200
    # username: ${ES_USERNAME}
    # password: ${ES_PASSWORD}
```

### 4.2 Index-Mapping: Das Herzstück der Suche

```java
// @Document: eine Elasticsearch-Mapping-Klasse
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;                 // product ID

    // analyzed: Volltext-Suche (tokenisiert, lowercase, stemming)
    @Field(type = FieldType.Text,
           analyzer = "german")        // Deutsches Stemming!
    private String name;

    // keyword: exakte Suche und Facetten
    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String brand;

    // nested: für Facetten mit mehreren Werten
    @Field(type = FieldType.Keyword)
    private List<String> colors;

    @Field(type = FieldType.Keyword)
    private List<String> sizes;

    // double: für Preis-Range-Filter und Sortierung
    @Field(type = FieldType.Double)
    private double priceEur;

    // boolean: für Verfügbarkeits-Filter
    @Field(type = FieldType.Boolean)
    private boolean available;

    // Für bessere Suche: mehrere Felder
    // name wird in mehreren Varianten gespeichert:
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "german"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword), // Exakt
            @InnerField(suffix = "suggest", type = FieldType.Text,
                analyzer = "autocomplete")  // Autovervollständigung
        })
    private String nameMulti;

    @Field(type = FieldType.Text, analyzer = "german")
    private String description;

    // Metadaten für Debugging
    @Field(type = FieldType.Date)
    private Instant indexedAt;
}
```

### 4.3 Custom Analyzer für Deutsch + Autocomplete

```java
@Configuration
public class ElasticsearchIndexConfig {

    @Bean
    public IndexCoordinates productIndex() {
        return IndexCoordinates.of("products");
    }

    // Analyzer-Konfiguration via REST-API beim Start
    @Bean
    public ApplicationRunner indexSetup(
            ElasticsearchOperations operations) {
        return args -> {
            var settings = Map.of(
                "analysis", Map.of(
                    "analyzer", Map.of(
                        // Autocomplete-Analyzer für Suchvorschläge
                        "autocomplete", Map.of(
                            "tokenizer", "autocomplete_tokenizer",
                            "filter", List.of("lowercase")
                        ),
                        "autocomplete_search", Map.of(
                            "tokenizer", "lowercase"
                        )
                    ),
                    "tokenizer", Map.of(
                        "autocomplete_tokenizer", Map.of(
                            "type", "edge_ngram",
                            "min_gram", 2,
                            "max_gram", 15,
                            "token_chars", List.of("letter", "digit")
                        )
                    )
                )
            );
            // Index mit Settings erstellen wenn nicht vorhanden
            // (in Produktion: Index-Templates verwenden)
        };
    }
}
```

### 4.4 Repository + Queries

```java
// Spring Data Elasticsearch Repository
public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    // Einfache Query Methods (automatisch generiert)
    List<ProductDocument> findByCategoryAndAvailable(String category, boolean available);
    Page<ProductDocument> findByBrand(String brand, Pageable pageable);
}

// Service mit komplexen Queries
@Service
public class ProductSearchService {

    private final ElasticsearchOperations esOps;

    public SearchResult search(SearchRequest request) {
        // Volltext + Filter + Facetten in EINER Query
        var nativeQuery = NativeQuery.builder()
            .withQuery(buildSearchQuery(request))
            .withFilter(buildFilterQuery(request))
            .withAggregation("categories",
                AggregationBuilders.terms("categories").field("category").size(50))
            .withAggregation("brands",
                AggregationBuilders.terms("brands").field("brand").size(20))
            .withAggregation("price_ranges",
                AggregationBuilders.range("price_ranges").field("priceEur")
                    .addRange(0, 25)
                    .addRange(25, 50)
                    .addRange(50, 100)
                    .addUnboundedTo(100))
            .withPageable(request.toPageable())
            .withHighlightQuery(buildHighlightQuery())  // Treffer markieren
            .build();

        var hits = esOps.search(nativeQuery, ProductDocument.class);

        return new SearchResult(
            hits.getTotalHits(),
            hits.getSearchHits().stream()
                .map(this::toProductDto)
                .toList(),
            extractFacets(hits.getAggregations())
        );
    }

    private Query buildSearchQuery(SearchRequest request) {
        if (request.query().isBlank()) {
            return QueryBuilders.matchAllQuery().build()._toQuery();
        }

        // Multi-Match: sucht in mehreren Feldern mit verschiedenen Gewichtungen
        return QueryBuilders.multiMatch(mm -> mm
            .query(request.query())
            .fields(
                "name^3",         // Name: 3× wichtiger
                "name.keyword^5", // Exakter Name: 5× wichtiger
                "description^1",  // Beschreibung: 1× wichtig
                "brand^2"         // Marke: 2× wichtig
            )
            .type(TextQueryType.BestFields)
            .fuzziness("AUTO")    // Tippfehler-Toleranz automatisch
            .minimumShouldMatch("75%") // Mindestens 75% der Wörter müssen passen
        );
    }

    private Query buildFilterQuery(SearchRequest request) {
        var filters = new ArrayList<Query>();

        // Immer: nur verfügbare Produkte
        filters.add(QueryBuilders.term(t ->
            t.field("available").value(true)));

        // Optional: Kategorie-Filter
        if (!request.categories().isEmpty()) {
            filters.add(QueryBuilders.terms(t ->
                t.field("category").terms(tv ->
                    tv.value(request.categories().stream()
                        .map(FieldValue::of).toList()))));
        }

        // Optional: Preis-Range
        if (request.maxPrice() != null) {
            filters.add(QueryBuilders.range(r ->
                r.field("priceEur").lte(JsonData.of(request.maxPrice()))));
        }

        return QueryBuilders.bool(b -> b.must(filters)).build()._toQuery();
    }

    // Autovervollständigung für Suchfeld
    public List<String> autocomplete(String prefix) {
        var query = NativeQuery.builder()
            .withQuery(QueryBuilders.matchPhrasePrefix(m ->
                m.field("name.suggest").query(prefix)))
            .withPageable(PageRequest.of(0, 10))
            .build();

        return esOps.search(query, ProductDocument.class)
            .getSearchHits().stream()
            .map(h -> h.getContent().getName())
            .distinct()
            .toList();
    }
}
```

---

## 5. Synchronisation: Kafka → Elasticsearch

```java
// Elasticsearch-Index aus Kafka-Events aufbauen (→ ADR-032 CQRS)
@Component
public class ProductIndexer {

    private final ProductSearchRepository searchRepo;
    private final ProductMapper mapper;

    // Event konsumieren und Index aktualisieren
    @KafkaListener(topics = {
        "products.created",
        "products.updated",
        "products.price-changed"
    }, groupId = "elasticsearch-indexer")
    @Transactional
    public void indexProduct(ProductEvent event) {
        log.info("Indexing product", kv("productId", event.productId()));

        var document = mapper.toDocument(event);
        document.setIndexedAt(Instant.now());

        searchRepo.save(document);
    }

    // Produkt aus Index entfernen (bei Löschung oder Deaktivierung)
    @KafkaListener(topics = "products.deleted",
                   groupId = "elasticsearch-indexer")
    public void removeFromIndex(ProductDeletedEvent event) {
        searchRepo.deleteById(event.productId());
    }
}

// Initialer Index-Build: alle Produkte neu indizieren
@Service
public class ProductReindexService {

    private final ProductRepository productRepo;   // PostgreSQL
    private final ProductSearchRepository searchRepo; // Elasticsearch

    @Scheduled(cron = "0 0 2 * * SUN")  // Wöchentlich Sonntag 2 Uhr
    public void reindexAll() {
        log.info("Starting full product reindex");
        var count = new AtomicInteger(0);

        // Batch-weise reindizieren (nicht alles auf einmal → OOM)
        productRepo.streamAll()
            .map(mapper::toDocument)
            .collect(Collectors.groupingBy(
                d -> count.getAndIncrement() / 1000))  // Batches à 1000
            .values()
            .forEach(batch -> {
                searchRepo.saveAll(batch);
                log.info("Reindexed {} products", count.get());
            });
    }
}
```

---

## 6. Gesundheits-Monitoring

```java
// Health-Check für Elasticsearch (erscheint in /actuator/health)
// Spring Boot Actuator bringt das automatisch mit

// Zusätzlich: Index-Lag überwachen
@Component
public class ElasticsearchLagMonitor {

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelay = 60_000)
    public void measureIndexLag() {
        var pgCount = productRepository.count();
        var esCount = searchRepository.count();
        var lag = pgCount - esCount;

        meterRegistry.gauge("elasticsearch.index.lag",
            Tags.of("index", "products"), lag);

        if (lag > 1000) {
            log.warn("Elasticsearch index lag: {} documents behind", lag);
        }
    }
}
```

---

## 7. Akzeptanzkriterien

- [ ] Produktsuche P95 < 100ms bei 10.000 Dokumenten (Gatling-Test)
- [ ] Tippfehler-Toleranz: "jens" findet "jeans" (Fuzziness-Test)
- [ ] Facetten werden korrekt gezählt
- [ ] Index-Lag bei normalem Betrieb < 5 Sekunden (Kafka-Delay)
- [ ] Index-Rebuild möglich ohne Produktions-Impact (blue/green Index)
- [ ] DSGVO: keine personenbezogenen Daten im Such-Index

---

## Quellen & Referenzen

- **Elasticsearch Documentation, "Full-text queries"** — offizielle Query-Referenz.
- **Clinton Gormley & Zachary Tong, "Elasticsearch: The Definitive Guide" (2015)** — Grundkonzepte des invertierten Index.
- **Martin Kleppmann, "Designing Data-Intensive Applications" (2017), Kap. 3** — Index-Strukturen und Such-Architekturen.

---

## Verwandte ADRs

- [ADR-032](ADR-032-cqrs.md) — CQRS: Elasticsearch als Read-Model
- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka als Synchronisations-Kanal
- [ADR-073](ADR-073-database-indexing.md) — PostgreSQL-Indizes als Komplementär
