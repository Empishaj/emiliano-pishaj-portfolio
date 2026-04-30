# ADR-099 — MapStruct: Typsicheres Objekt-Mapping ohne Reflection

| Feld              | Wert                                                          |
|-------------------|---------------------------------------------------------------|
| Status            | ✅ Akzeptiert                                                 |
| Entscheider       | Architektur-Board                                             |
| Datum             | 2024-01-01                                                    |
| Review-Datum      | 2025-01-01                                                    |
| Kategorie         | Code-Qualität · Hexagonale Architektur · Performance          |
| Betroffene Teams  | Alle Engineering-Teams                                        |
| Abhängigkeiten    | ADR-031 (Hexagonal), ADR-001 (Records), ADR-050 (Gradle)     |

---

## 1. Kontext & Treiber

### 1.1 Das Mapping-Problem in Hexagonaler Architektur

```
HEXAGONALE ARCHITEKTUR (→ ADR-031) hat mindestens drei Objektmodelle:

  Domain-Objekt:    Order (reines Java, keine Annotations)
  JPA-Entity:       OrderEntity (JPA-Annotations, Datenbankstruktur)
  REST-DTO:         OrderDto (Jackson-Annotations, API-Format)
  Kafka-Event:      OrderPlacedEvent (Avro-Schema, Event-Format)

PROBLEM: Diese Objekte müssen an Schichtgrenzen transformiert werden:
  HTTP Request  → DTO    → Domain-Command
  Domain-Object → Entity → DB
  Domain-Object → DTO    → HTTP Response
  Domain-Object → Event  → Kafka

Das sind 6-8 Transformationen pro Domänen-Objekt.
Bei 10 Domänen-Objekten: 60-80 Mapping-Implementierungen.
```

### 1.2 Die Mapping-Ansätze im Vergleich

```
ANSATZ 1: Manuelles Mapping
  public OrderDto toDto(Order order) {
      return new OrderDto(order.id().value(), order.status().name(), ...);
  }
  + Vollständige Kontrolle, performant, typsicher
  - Sehr viel Boilerplate, fehleranfällig bei Umbenennung

ANSATZ 2: Reflection (ModelMapper, Dozer)
  modelMapper.map(order, OrderDto.class);
  + Wenig Code
  - Laufzeit-Reflection = Performance-Problem
  - Fehler erst zur Laufzeit (falsche Feldnamen → kein Compiler-Fehler!)
  - Nicht kompatibel mit GraalVM Native (→ ADR-071)

ANSATZ 3: MapStruct (Compile-Time-Generierung)
  @Mapper interface OrderMapper { OrderDto toDto(Order order); }
  + Compile-Time-Generierung → Fehler sofort sichtbar
  + Kein Reflection → performant wie manuelles Mapping
  + GraalVM-kompatibel
  + Wenig Code, viel Automatik mit Overrides
  ✅ GEWÄHLT
```

---

## 2. Entscheidung

MapStruct wird für alle Objekt-Transformationen zwischen Schichtgrenzen verwendet. Reflection-basierte Mapper (ModelMapper, Dozer) sind verboten. Manuelles Mapping ist nur akzeptabel für sehr spezifische Transformationen die MapStruct nicht ausdrücken kann (< 5% der Fälle).

---

## 3. MapStruct: Grundlagen bis Experte

### 3.1 Setup

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    // Mit Lombok (falls verwendet — aber wir bevorzugen Records → ADR-001):
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
}
```

### 3.2 Einfaches Mapping: automatisch wenn Namen übereinstimmen

```java
// Domain-Objekt (reines Java)
public record Order(
    OrderId            id,
    CustomerId         customerId,
    List<OrderItem>    items,
    OrderStatus        status,
    Money              total,
    ShippingAddress    shippingAddress,
    Instant            createdAt
) {}

// REST-DTO (Jackson-Annotations)
public record OrderDto(
    String          id,
    String          customerId,
    List<OrderItemDto> items,
    String          status,
    BigDecimal      totalAmount,
    String          currency,
    String          street,
    String          city,
    String          zipCode,
    Instant         createdAt
) {}

// ❌ OHNE MapStruct — viel Boilerplate:
public OrderDto toDto(Order order) {
    return new OrderDto(
        order.id().value().toString(),
        order.customerId().value().toString(),
        order.items().stream().map(this::toItemDto).toList(),
        order.status().name(),
        order.total().amount(),
        order.total().currency().getCurrencyCode(),
        order.shippingAddress().street(),
        order.shippingAddress().city(),
        order.shippingAddress().zipCode(),
        order.createdAt()
    );
}
// 15 Zeilen, fehleranfällig, muss bei jeder Feld-Änderung angepasst werden

// ✅ MIT MapStruct:
@Mapper(componentModel = "spring")  // Spring-Bean, @Autowired möglich
public interface OrderMapper {

    // Automatisch wenn Namen und Typen übereinstimmen
    // Für abweichende Felder: @Mapping

    @Mapping(source = "id.value",              target = "id")
    @Mapping(source = "customerId.value",      target = "customerId")
    @Mapping(source = "status",                target = "status",
             qualifiedByName = "enumToString")
    @Mapping(source = "total.amount",          target = "totalAmount")
    @Mapping(source = "total.currency.currencyCode", target = "currency")
    @Mapping(source = "shippingAddress.street",target = "street")
    @Mapping(source = "shippingAddress.city",  target = "city")
    @Mapping(source = "shippingAddress.zipCode",target = "zipCode")
    OrderDto toDto(Order order);

    // Umgekehrtes Mapping
    @InheritInverseConfiguration
    @Mapping(target = "id",              expression = "java(new OrderId(Long.parseLong(dto.id())))")
    @Mapping(target = "customerId",      expression = "java(new CustomerId(Long.parseLong(dto.customerId())))")
    @Mapping(target = "total",           expression = "java(Money.of(dto.totalAmount(), dto.currency()))")
    @Mapping(target = "shippingAddress", source = "dto")  // Flat → Objekt
    Order toDomain(OrderDto dto);

    // Listen werden automatisch gemappt wenn Element-Mapping definiert:
    List<OrderItemDto> toItemDtos(List<OrderItem> items);
    OrderItemDto toItemDto(OrderItem item);

    // Custom-Konverter als benannte Methode
    @Named("enumToString")
    default String enumToString(Enum<?> e) { return e.name(); }
}
// MapStruct generiert zur Compile-Zeit die Implementierung
// → Vollständig typsicher, keine Reflection, performant wie manuell
```

### 3.3 Komplexere Szenarien

```java
// Szenario: JPA-Entity ↔ Domain-Object (Hexagonale Adapter-Schicht)

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id @GeneratedValue
    private Long id;
    private Long customerId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    // Adresse als Embedded (flat in DB)
    private String shippingStreet;
    private String shippingCity;
    private String shippingZip;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items;
}

@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class})  // Delegate für Items
public interface OrderEntityMapper {

    // Entity → Domain
    @Mapping(source = "id",             target = "id",
             qualifiedByName = "longToOrderId")
    @Mapping(source = "customerId",     target = "customerId",
             qualifiedByName = "longToCustomerId")
    @Mapping(source = "totalAmount",    target = "total",
             qualifiedByName = "buildMoney")
    @Mapping(source = "entity",         target = "shippingAddress",
             qualifiedByName = "buildAddress")
    Order toDomain(OrderEntity entity);

    // Domain → Entity
    @Mapping(source = "id.value",       target = "id")
    @Mapping(source = "customerId.value", target = "customerId")
    @Mapping(source = "total.amount",   target = "totalAmount")
    @Mapping(source = "total.currency.currencyCode", target = "currency")
    @Mapping(source = "shippingAddress.street", target = "shippingStreet")
    @Mapping(source = "shippingAddress.city",   target = "shippingCity")
    @Mapping(source = "shippingAddress.zipCode",target = "shippingZip")
    OrderEntity toEntity(Order domain);

    // Update bestehende Entity (statt neue erstellen → kein neues JPA-Objekt)
    @Mapping(source = "id.value",       target = "id")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(Order domain, @MappingTarget OrderEntity entity);

    // Custom-Konverter
    @Named("longToOrderId")
    default OrderId longToOrderId(Long id) {
        return id != null ? new OrderId(id) : null;
    }

    @Named("longToCustomerId")
    default CustomerId longToCustomerId(Long id) {
        return id != null ? new CustomerId(id) : null;
    }

    @Named("buildMoney")
    @Mapping(source = "totalAmount",    target = "amount")
    @Mapping(source = "currency",       target = "currency",
             qualifiedByName = "stringToCurrency")
    Money buildMoney(OrderEntity entity);

    @Named("buildAddress")
    @Mapping(source = "shippingStreet", target = "street")
    @Mapping(source = "shippingCity",   target = "city")
    @Mapping(source = "shippingZip",    target = "zipCode")
    ShippingAddress buildAddress(OrderEntity entity);

    @Named("stringToCurrency")
    default Currency stringToCurrency(String code) {
        return Currency.getInstance(code);
    }
}
```

### 3.4 Records und MapStruct (Java 21)

```java
// MapStruct 1.5+ unterstützt Java Records vollständig

public record CreateOrderCommand(
    CustomerId customerId,
    List<OrderItemRequest> items,
    Address shippingAddress
) {}

public record PlaceOrderRequest(
    String customerId,
    List<OrderItemInput> items,
    AddressInput shippingAddress
) {}

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(source = "customerId",
             target = "customerId",
             qualifiedByName = "stringToCustomerId")
    CreateOrderCommand toCommand(PlaceOrderRequest request);

    @Named("stringToCustomerId")
    default CustomerId stringToCustomerId(String id) {
        return new CustomerId(Long.parseLong(id));
    }
}
// MapStruct erkennt Records und nutzt den Konstruktor automatisch
// → kein manuelles new CreateOrderCommand(...) nötig
```

### 3.5 Testen von Mappern

```java
// Mapper als Spring-Bean testbar (ohne Spring-Context)
@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    // Direktes Testen ohne Spring-Context:
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void toDto_mapptAlleFelder() {
        var order = Order.builder()
            .id(new OrderId(42L))
            .customerId(new CustomerId(100L))
            .status(OrderStatus.CONFIRMED)
            .total(Money.of("99.99", EUR))
            .shippingAddress(new ShippingAddress("Hauptstr. 1", "Berlin", "10115"))
            .createdAt(Instant.parse("2024-01-15T10:00:00Z"))
            .build();

        var dto = mapper.toDto(order);

        assertThat(dto.id()).isEqualTo("42");
        assertThat(dto.customerId()).isEqualTo("100");
        assertThat(dto.status()).isEqualTo("CONFIRMED");
        assertThat(dto.totalAmount()).isEqualByComparingTo("99.99");
        assertThat(dto.currency()).isEqualTo("EUR");
        assertThat(dto.street()).isEqualTo("Hauptstr. 1");
        assertThat(dto.createdAt()).isEqualTo(Instant.parse("2024-01-15T10:00:00Z"));
    }

    @Test
    void roundTrip_domainToDtoAndBack_preservesAllFields() {
        var original = buildValidOrder();
        var dto      = mapper.toDto(original);
        var restored = mapper.toDomain(dto);

        // Round-Trip: kein Datenverlust
        assertThat(restored.id()).isEqualTo(original.id());
        assertThat(restored.total()).isEqualTo(original.total());
        assertThat(restored.shippingAddress()).isEqualTo(original.shippingAddress());
    }
}
```

---

## 4. MapStruct in der Hexagonalen Architektur

```
SCHICHT-VERANTWORTLICHKEITEN:

REST-Adapter:
  PlaceOrderRequest (JSON) → CreateOrderCommand (Domain)   via: OrderRequestMapper
  Order (Domain)           → OrderDto (JSON)               via: OrderResponseMapper

JPA-Adapter:
  OrderEntity (JPA)        → Order (Domain)                via: OrderEntityMapper
  Order (Domain)           → OrderEntity (JPA)             via: OrderEntityMapper

Kafka-Adapter:
  OrderPlacedEvent (Avro)  → OrderEventDto (intern)        via: OrderEventMapper
  Order (Domain)           → OrderPlacedEvent (Avro)       via: OrderEventMapper

WICHTIG: Mapper leben im ADAPTER, nicht in der Domain!
         Domain kennt kein MapStruct, kein JPA, kein Jackson.
```

---

## 5. ArchUnit-Regel für Mapper-Platzierung

```java
@ArchTest
static final ArchRule mapper_nur_in_adapter =
    noClasses()
        .that().areAnnotatedWith(Mapper.class)
        .should().resideInAPackage("com.example.domain..")
        .orShould().resideInAPackage("com.example.application..")
        .because("ADR-099: Mapper gehören in den Adapter, nicht in Domain");
```

---

## 6. Alternativen & Warum sie abgelehnt wurden

| Alternative | Stärken | Schwächen | Ablehnungsgrund |
|---|---|---|---|
| ModelMapper | Wenig Konfiguration | Reflection, Laufzeit-Fehler, langsam | Typ-Fehler erst zur Laufzeit; nicht GraalVM-kompatibel |
| Manuelles Mapping | Vollständige Kontrolle | Viel Boilerplate, fehleranfällig | Bei 50 Domänen-Objekten: 300+ Mapper-Methoden manuell |
| Lombok @Builder + toDto() in Klasse | Einfach | Domain kennt DTOs → Schicht-Verletzung! | Verletzt Hexagonale Architektur (→ ADR-031) |
| Kotlin Data Classes + copy() | Elegant in Kotlin | Wir sind Java-Stack | Stack-Entscheidung (→ ADR-076) |

---

## 7. Trade-off-Matrix

| Qualitätsziel | MapStruct | ModelMapper | Manuell |
|---|---|---|---|
| Typsicherheit | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Performance | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Boilerplate | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ |
| GraalVM-Kompatibilität | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐⭐⭐⭐ |
| Debug-Freundlichkeit | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 8. Akzeptanzkriterien

- [ ] Kein ModelMapper oder Dozer im Classpath (ArchUnit oder Dependency-Check)
- [ ] Alle Schicht-Grenzen-Transformationen nutzen MapStruct-generierte Mapper
- [ ] Mapper sind in Adapter-Packages, nicht in Domain oder Application
- [ ] Jeder Mapper hat mindestens einen Round-Trip-Test
- [ ] MapStruct-Warnungen behandelt als Fehler: `-Amapstruct.unmappedTargetPolicy=ERROR`

---

## Quellen & Referenzen

- **MapStruct Reference Guide (2024)** — offizielle Dokumentation; mapstruct.org.
- **Gunnar Morling, "MapStruct 1.5: What's New" (2022)** — Record-Support, Spring Boot 3.x Integration.
- **Robert C. Martin, "Clean Architecture" (2017)** — Objektmodelle pro Schicht; Mapping als Kosten der Schichtenarchitektur.
- **Vaughn Vernon, "Implementing Domain-Driven Design" (2013), Kap. 4** — Anti-Corruption Layer benötigt Übersetzung.

---

## Verwandte ADRs

- [ADR-031](ADR-031-hexagonal-architecture.md) — Hexagonale Architektur erzeugt Mapping-Bedarf
- [ADR-001](ADR-001-records-statt-javabeans.md) — Records als Mapping-Ziele
- [ADR-050](ADR-050-multi-module-gradle.md) — `annotationProcessor` in build.gradle.kts
- [ADR-071](ADR-071-graalvm-native-image.md) — MapStruct ist GraalVM-kompatibel (Reflection-frei)
