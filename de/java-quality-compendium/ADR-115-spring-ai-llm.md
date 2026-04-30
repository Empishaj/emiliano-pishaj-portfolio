# ADR-115 — Spring AI: LLM-Integration in Spring Boot

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Datum             | 2026-02-24                                                    |
| Kategorie         | KI/ML · Spring AI · LLM                                       |
 
---

## 1. Kontext & Treiber

```
SPRING AI (1.0 GA, März 2024):
  Einheitliche Abstraktion für verschiedene LLM-Anbieter:
  OpenAI, Anthropic Claude, Azure OpenAI, Google Gemini, Ollama (lokal)
  
  Ohne Spring AI: jeder Anbieter hat ein eigenes SDK
  Mit Spring AI: ein Interface, Provider wechselbar
  
USE CASES in unserem Stack:
  ① Produkt-Beschreibungen generieren
  ② Support-Chat mit Kontext aus Bestellhistorie
  ③ Such-Query-Verbesserung ("blaue jeans" → strukturierter Filter)
  ④ Dokument-Zusammenfassung (Rechnungen, Verträge)
  ⑤ Code-Generierung (intern, Entwickler-Tools)
```

---

## 2. Entscheidung

Spring AI wird als einheitliche Abstraktionsschicht für alle LLM-Integrationen
verwendet. Kein direktes SDK eines Anbieters in Business-Code. PII wird
vor LLM-Aufrufen maskiert (→ ADR-106). Kosten werden pro Funktion gemessen.

---

## 3. Dependencies

```kotlin
// build.gradle.kts
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.0")
    }
}

dependencies {
    // Spring AI Core
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    // Oder: Anthropic, Azure, Ollama (lokal/on-premise)
    // implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter")
    // implementation("org.springframework.ai:spring-ai-ollama-spring-boot-starter")

    // Vector Store für RAG (Retrieval Augmented Generation)
    implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter")
}
```

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}    # Vault! (→ ADR-069)
      chat:
        options:
          model:       gpt-4o-mini  # Günstig für Standard-Aufgaben
          temperature: 0.7           # Kreativität: 0 = deterministisch, 1 = kreativ
          max-tokens:  1000
```

---

## 4. ChatClient: Einfache LLM-Aufrufe

```java
// Spring AI ChatClient: einfache API für Chat-Completions

@Service
public class ProductDescriptionService {

    private final ChatClient chatClient;

    // Spring AI: Builder-Pattern für ChatClient
    public ProductDescriptionService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("""
                Du bist ein E-Commerce-Texter der prägnante, verkaufsfördernde
                Produktbeschreibungen auf Deutsch schreibt.
                Beschreibungen sind maximal 3 Sätze lang.
                Keine reißerischen Übertreibungen.
                """)
            .build();
    }

    public String generateDescription(Product product) {
        // PII-Check: Produktdaten dürfen keine Kundendaten enthalten
        // (→ ADR-106: Data Masking)

        return chatClient.prompt()
            .user("""
                Schreibe eine Produktbeschreibung für:
                Name: %s
                Kategorie: %s
                Eigenschaften: %s
                """.formatted(
                    product.name(),
                    product.category(),
                    product.attributes()))
            .call()
            .content();
    }
}
```

---

## 5. RAG: Retrieval Augmented Generation

```java
// RAG-Pattern: LLM mit eigenem Wissen anreichern
// Use Case: Support-Chat der Bestellhistorie kennt

@Service
public class OrderSupportChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;    // pgvector (PostgreSQL-Extension)

    public String answerSupportQuestion(String question, String customerId) {

        // SCHRITT 1: PII aus Question maskieren bevor sie an LLM geht
        var maskedQuestion = piiMasker.mask(question);

        // SCHRITT 2: Relevante Dokumente aus VectorStore laden
        // (Bestellhistorie, FAQs, Produktinfos)
        var relevantDocs = vectorStore.similaritySearch(
            SearchRequest.query(maskedQuestion)
                .withTopK(5)
                .withFilterExpression("customerId == '" + customerId + "'")
        );

        // SCHRITT 3: Kontext + Frage an LLM
        return chatClient.prompt()
            .system("""
                Du bist ein freundlicher Kundenservice-Mitarbeiter.
                Antworte nur basierend auf den bereitgestellten Informationen.
                Wenn du die Antwort nicht weißt, sag es ehrlich.
                """)
            .user(u -> u
                .text("""
                    Kundenfrage: {question}

                    Relevante Informationen:
                    {context}
                    """)
                .param("question", maskedQuestion)
                .param("context", formatDocuments(relevantDocs)))
            .call()
            .content();
    }
}
```

---

## 6. Structured Output

```java
// LLM-Antwort als Java-Objekt erhalten (nicht nur String)
public record ProductFilter(
    String  category,
    Double  minPrice,
    Double  maxPrice,
    String  color,
    String  size
) {}

@Service
public class SearchQueryEnhancer {

    private final ChatClient chatClient;

    public ProductFilter parseNaturalLanguageQuery(String userInput) {
        // Strukturierte Ausgabe: Spring AI konvertiert JSON → Java Record
        return chatClient.prompt()
            .user("""
                Extrahiere Suchfilter aus dieser Suchanfrage und antworte NUR mit JSON:
                "%s"
                """.formatted(userInput))
            .call()
            .entity(ProductFilter.class);  // ← Spring AI parsed JSON automatisch
    }
}
// Input: "blaue Jeans unter 50 Euro Größe 32"
// Output: ProductFilter(category="jeans", maxPrice=50.0, color="blau", size="32")
```

---

## 7. Kosten-Monitoring

```java
// LLM-Kosten sind messbar und müssen überwacht werden!
// GPT-4o-mini: ca. $0.00015 per 1K input tokens

@Aspect
@Component
public class LlmCostMonitor {

    private final MeterRegistry metrics;

    @Around("@annotation(TrackLlmCost)")
    public Object trackCost(ProceedingJoinPoint jp) throws Throwable {
        var start = System.currentTimeMillis();
        var result = jp.proceed();

        if (result instanceof ChatResponse response) {
            var usage = response.getMetadata().getUsage();
            var feature = jp.getSignature().getName();

            // Tokens als Metrik erfassen
            Counter.builder("llm.tokens.used")
                .tag("feature", feature)
                .tag("type", "input")
                .register(metrics)
                .increment(usage.getPromptTokens());

            Counter.builder("llm.tokens.used")
                .tag("feature", feature)
                .tag("type", "output")
                .register(metrics)
                .increment(usage.getGenerationTokens());
        }
        return result;
    }
}
```

---

## 8. Lokale Entwicklung: Ollama statt OpenAI

```yaml
# application-dev.yml: Ollama statt OpenAI (kein API-Kosten in dev!)
spring:
  ai:
    openai:
      base-url: http://localhost:11434  # Ollama API-URL
      api-key: unused                    # Ollama braucht keinen Key
      chat:
        options:
          model: llama3.2               # Lokales Modell
```

```bash
# Ollama lokal starten (Docker):
docker run -d -v ollama:/root/.ollama -p 11434:11434 ollama/ollama
docker exec -it ollama ollama pull llama3.2
```

---

## 9. Sicherheit: PII niemals an externe LLMs

```java
// PFLICHT: PII-Check vor jedem LLM-Aufruf (→ ADR-106)
@Component
public class SafeLlmClient {

    private final ChatClient chatClient;
    private final PiiDetector piiDetector;

    public String promptSafely(String userPrompt) {
        // PII-Erkennung: E-Mail, Name, IBAN etc.
        var piiFound = piiDetector.detect(userPrompt);
        if (!piiFound.isEmpty()) {
            throw new PiiInPromptException(
                "Prompt enthält PII — nicht an externen LLM senden: "
                + piiFound.types());
        }
        return chatClient.prompt().user(userPrompt).call().content();
    }
}
```

---

## 10. Akzeptanzkriterien

- [ ] `OPENAI_API_KEY` kommt aus Vault, nicht aus application.yml
- [ ] PII-Prüfung vor jedem LLM-Aufruf (automatischer Test)
- [ ] Kosten-Metrik `llm.tokens.used` in Grafana sichtbar
- [ ] Lokale Entwicklung nutzt Ollama (kein API-Key nötig)
- [ ] Kein direktes OpenAI-SDK im Business-Code (ArchUnit-Regel)
- [ ] Budget-Alert: wenn LLM-Kosten > X EUR/Tag

---

## Quellen & Referenzen

- **Spring AI Documentation (2024)** — spring.io/projects/spring-ai
- **Josh Long, "Spring AI" Talk (2024), Spring I/O** — Architektur und Use Cases.
- **Patrick Lewis et al., "Retrieval-Augmented Generation for NLP" (2020)** — RAG-Pattern.

---

## Verwandte ADRs

- [ADR-069](ADR-069-configuration-management.md) — API-Keys aus Vault
- [ADR-106](ADR-106-data-masking-pseudonymisierung.md) — PII-Schutz vor LLM-Aufruf
- [ADR-107](ADR-107-finops-cloud-kosten.md) — LLM-Kosten überwachen
