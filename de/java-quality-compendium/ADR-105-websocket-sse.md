# ADR-105 — WebSocket & Server-Sent Events: Echtzeit-Kommunikation

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | API Design · Echtzeit · WebSocket                             |
| Betroffene Teams  | Teams mit Echtzeit-Anforderungen                              |
| Abhängigkeiten    | ADR-004 (Virtual Threads), ADR-091 (Reactive), ADR-041 (Kafka)|

---

## 1. Wann welches Protokoll?

```
HTTP REQUEST/RESPONSE:
  Client fragt → Server antwortet → Verbindung geschlossen
  Gut für: CRUD-Operationen, Abfragen
  Nicht gut für: "Push von Server zu Client"

SERVER-SENT EVENTS (SSE):
  Server sendet Events → Client hört zu (einseitig)
  Gut für: Live-Updates, Status-Benachrichtigungen
  Nicht gut für: bidirektionale Kommunikation
  Protokoll: HTTP (!) mit text/event-stream Content-Type
  Vorteil: einfach, HTTP/2-kompatibel, automatisches Reconnect

WEBSOCKET:
  Bidirektionale Verbindung (Client ↔ Server)
  Gut für: Chat, Kollaboration, Live-Editing, Games
  Nicht gut für: einseitiger Push (SSE ist einfacher)
  Protokoll: WS/WSS (Upgrade von HTTP)

LONG POLLING (veraltet):
  Client fragt → Server hält Verbindung offen bis Event → Antwort
  Heute nicht mehr verwenden (SSE ist besser)

ENTSCHEIDUNGSREGEL:
  Server → Client: SSE (einfacher, HTTP)
  Client ↔ Server (bidirektional): WebSocket
```

---

## 2. Server-Sent Events mit Spring Boot

### 2.1 Bestellstatus live verfolgen (SSE)

```java
// Use Case: "Zeige Bestellstatus in Echtzeit wenn Nutzer auf der Bestellseite ist"
// Typischer E-Commerce-Flow: Bezahlen → warte → "Bestätigt!" → "Versendet!"

@RestController
@RequestMapping("/api/v2/orders")
public class OrderStatusStreamController {

    private final OrderEventService orderEventService;

    // SSE-Endpunkt: gibt einen Datenstrom zurück statt einer einzelnen Antwort
    @GetMapping(
        value = "/{orderId}/status-stream",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE  // "text/event-stream"
    )
    @PreAuthorize("@orderSecurityService.isOwner(#orderId, authentication)")
    public SseEmitter streamOrderStatus(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {

        // SseEmitter: Kanal für Server → Client Events
        var emitter = new SseEmitter(
            Duration.ofMinutes(30).toMillis()  // Timeout nach 30 Minuten
        );

        // Event-Quelle abonnieren (z.B. Kafka-Consumer für diesen Order)
        var subscription = orderEventService.subscribeToOrderEvents(
            orderId,
            event -> {
                try {
                    // Event an Client senden
                    emitter.send(SseEmitter.event()
                        .id(event.eventId())           // Client kann ab hier reconnecten
                        .name(event.eventType())       // Event-Name für JS-Filter
                        .data(event)                   // Payload (wird zu JSON)
                        .reconnectTime(3000)           // Client reconnect nach 3s
                    );

                    // Finale Status → Stream schließen
                    if (event.isFinalStatus()) {
                        emitter.complete();
                    }

                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        );

        // Cleanup wenn Client disconnect
        emitter.onCompletion(subscription::cancel);
        emitter.onTimeout(subscription::cancel);
        emitter.onError(error -> subscription.cancel());

        // Initialen Status sofort senden (kein Warten auf erstes Event)
        try {
            var currentStatus = orderService.getCurrentStatus(orderId);
            emitter.send(SseEmitter.event()
                .name("current-status")
                .data(currentStatus));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}

// Event-Service: Kafka-Consumer für spezifischen Order
@Service
public class OrderEventService {

    private final Map<String, List<Consumer<OrderStatusEvent>>> subscribers
        = new ConcurrentHashMap<>();

    // Kafka-Consumer: alle Order-Events
    @KafkaListener(topics = "orders.status-changed")
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        var handlers = subscribers.getOrDefault(event.orderId(), List.of());
        handlers.forEach(handler -> handler.accept(toStatusEvent(event)));
    }

    public Subscription subscribeToOrderEvents(
            String orderId, Consumer<OrderStatusEvent> handler) {

        subscribers.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>())
            .add(handler);

        return () -> subscribers.getOrDefault(orderId, List.of()).remove(handler);
    }
}
```

### 2.2 Frontend-Integration (JavaScript)

```javascript
// Browser-seitige SSE-Nutzung (EventSource API)
const orderId = "ord-123";

const eventSource = new EventSource(
    `/api/v2/orders/${orderId}/status-stream`,
    { withCredentials: true }  // JWT-Cookie mitschicken
);

// Auf spezifische Event-Types hören
eventSource.addEventListener("order-status-changed", (event) => {
    const data = JSON.parse(event.data);
    updateStatusUI(data.newStatus, data.message);
});

eventSource.addEventListener("current-status", (event) => {
    const data = JSON.parse(event.data);
    initializeStatusUI(data.status);
});

// Fehler + automatisches Reconnect (eingebaut in EventSource)
eventSource.onerror = (error) => {
    console.log("SSE error, will reconnect automatically");
};

// Manuell schließen
function cleanup() {
    eventSource.close();
}
```

---

## 3. WebSocket mit Spring Boot (STOMP)

### 3.1 Wann WebSocket statt SSE?

```
CHAT-FUNKTION: Nutzer schreibt Nachricht → Server → andere Nutzer
→ BIDIREKTIONAL nötig → WebSocket

KOLLABORATIVER EDITOR: Nutzer A ändert Text → Nutzer B sieht es live
→ BIDIREKTIONAL nötig → WebSocket

BESTELLSTATUS LIVE: Server sendet Statusupdates → Nutzer sieht es
→ NUR Server → Client → SSE reicht!
```

```java
// WebSocket-Konfiguration mit STOMP (Sub-Protokoll über WebSocket)
// STOMP = Simple Text Oriented Messaging Protocol
// Vorteil: Message-Broker-Semantik (Topics, Queues) über WebSocket

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-Memory Broker für Topics (für Produktion: externe RabbitMQ oder Redis)
        config.enableSimpleBroker(
            "/topic",   // Broadcast: alle Subscriber bekommen die Nachricht
            "/queue"    // User-spezifisch: nur ein bestimmter Nutzer
        );

        // Prefix für Client → Server Nachrichten
        config.setApplicationDestinationPrefixes("/app");

        // Prefix für User-spezifische Queues
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")          // WebSocket-Endpunkt
            .setAllowedOriginPatterns("https://*.example.com")  // CORS!
            .withSockJS();                   // SockJS-Fallback für ältere Browser
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {
        // JWT-Validierung für eingehende WebSocket-Messages
        registration.interceptors(new JwtWebSocketInterceptor());
    }
}

// Controller: verarbeitet Client → Server Nachrichten und sendet Antworten
@Controller
public class OrderSupportController {

    private final SimpMessagingTemplate messagingTemplate;

    // Client sendet an: /app/support/chat
    @MessageMapping("/support/chat")
    public void handleChatMessage(
            ChatMessage message,
            Principal principal) {       // Authentifizierter Nutzer

        log.info("Message from {}: {}", principal.getName(), message.text());

        // An alle Supporter senden (Broadcast):
        messagingTemplate.convertAndSend(
            "/topic/support.queue",
            new SupportMessage(principal.getName(), message.text(), Instant.now())
        );
    }

    // Antwort an spezifischen Nutzer (nicht Broadcast)
    @MessageMapping("/order/status")
    public void subscribeToOrderStatus(
            StatusSubscriptionRequest request,
            Principal principal) {

        // Antwort NUR an diesen Nutzer: /user/{username}/queue/order-status
        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/queue/order-status",
            orderService.getCurrentStatus(request.orderId())
        );
    }
}

// Proaktive Server → Client Push (z.B. nach Kafka-Event)
@Service
public class OrderStatusPushService {

    private final SimpMessagingTemplate messaging;

    @KafkaListener(topics = "orders.status-changed")
    public void pushStatusUpdate(OrderStatusChangedEvent event) {
        // Nur an den Nutzer dessen Bestellung sich geändert hat
        messaging.convertAndSendToUser(
            event.customerId(),           // Username/Subject
            "/queue/order-status",
            new StatusUpdate(event.orderId(), event.newStatus())
        );
    }
}
```

---

## 4. Skalierung: WebSocket + Redis

```java
// Problem: WebSocket-Verbindungen sind stateful
// Bei 3 Instanzen: Nutzer A verbunden mit Instanz 1
//                 Kafka-Event kommt bei Instanz 2 an
//                 Instanz 2 kann Nachricht nicht an Nutzer A senden!

// Lösung: Redis als Message-Broker zwischen Instanzen

@Configuration
@EnableWebSocketMessageBroker
public class ScalableWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Redis statt In-Memory Broker (alle Instanzen teilen Broker)
        config.enableStompBrokerRelay(
            "/topic", "/queue")
            .setRelayHost("redis")
            .setRelayPort(6379);
        // Redis als Message-Relay:
        // Instanz 2 bekommt Kafka-Event → sendet an Redis
        // Redis routet zu Instanz 1 (wo Nutzer A verbunden ist)

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
}
```

---

## 5. Security für WebSocket/SSE

```java
// JWT-Validierung für WebSocket-Verbindungsaufbau
@Component
public class JwtWebSocketInterceptor
        implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);

        // Nur beim Verbindungsaufbau validieren
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            var authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing or invalid Authorization header");
            }

            var token = authHeader.substring(7);
            var authentication = jwtAuthenticationProvider.authenticate(
                new BearerTokenAuthenticationToken(token));

            accessor.setUser(authentication);
        }
        return message;
    }
}

// SSE: Security durch Standard-Spring-Security
// @PreAuthorize auf SSE-Endpunkt (→ ADR-101)
// SSE ist ein normaler HTTP-Request → Standard JWT-Validierung greift!
```

---

## 6. Monitoring

```java
// Aktive WebSocket-Verbindungen überwachen
@Component
public class WebSocketMetrics implements WebSocketHandlerDecorator {

    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final MeterRegistry meterRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        activeConnections.incrementAndGet();
        meterRegistry.gauge("websocket.connections.active",
            activeConnections.get());
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session, CloseStatus status) {
        activeConnections.decrementAndGet();
    }
}
```

---

## 7. Wann welche Technologie

```
USE CASE                         | TECHNOLOGIE
─────────────────────────────────────────────────────
Bestellstatus live               | SSE ← einfacher
Lagerbestand live                | SSE
Lieferverfolgung live            | SSE
Dashboard-Updates                | SSE
Chat-Support                     | WebSocket
Kollaborativer Editor            | WebSocket
Multiplayer-Features             | WebSocket
Echtzeit-Benachrichtigungen      | SSE oder WebSocket
Polling durch Client ersetzen    | SSE ← fast immer besser
```

---

## 8. Akzeptanzkriterien

- [ ] SSE-Endpunkte sind durch Spring Security abgesichert (→ ADR-101)
- [ ] Verbindungs-Timeout konfiguriert (max. 30 Minuten)
- [ ] Aktive WebSocket-/SSE-Verbindungen werden als Metrik exponiert
- [ ] Reconnect-Mechanismus getestet: Client reconnectet nach Server-Neustart automatisch
- [ ] CORS konfiguriert: nur erlaubte Origins

---

## Quellen & Referenzen

- **Spring Documentation, "WebSockets"** — vollständige Spring-WS-Referenz.
- **MDN Web Docs, "Server-sent events"** — SSE-Browser-API Referenz.
- **Kaazing, "WebSocket vs. SSE"** — Entscheidungsmatrix für bidirektionale vs. unidirektionale Kommunikation.

---

## Verwandte ADRs

- [ADR-004](ADR-004-virtual-threads.md) — Virtual Threads für viele WebSocket-Verbindungen
- [ADR-041](ADR-041-event-driven-kafka.md) — Kafka als Event-Quelle für SSE/WS
- [ADR-101](ADR-101-spring-security-oauth2.md) — Security für WebSocket-Verbindungen
