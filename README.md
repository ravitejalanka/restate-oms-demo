# Restate Spring Boot Integration - Event Sourcing & CQRS Demo

A production-ready implementation of **Event Sourcing and CQRS** using [Restate.dev](https://restate.dev/) with Spring Boot and Kotlin. This project demonstrates how Restate's **Durable Execution** paradigm eliminates the need for traditional infrastructure components like message brokers, event stores, and distributed lock managers.

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 9.2.1+ (included via wrapper)

### 1. Start Restate Server

```bash
docker-compose up -d
```

Restate will be available at:
- **Ingress**: http://localhost:8080
- **Admin UI**: http://localhost:9070
- **Admin API**: http://localhost:9071

### 2. Build the Project

```bash
./gradlew build
```

### 3. Run Handlers Module

```bash
./gradlew :order-handlers:bootRun
```

Or run `HandlersApplication.kt` from your IDE.

### 4. Run API Gateway (in separate terminal)

```bash
./gradlew :order-api-gateway:bootRun
```

Or run `ApiGatewayApplication.kt` from your IDE.

### 5. Register Services with Restate

```bash
curl -X POST http://localhost:9070/deployments \
  -H 'content-type: application/json' \
  -d '{"uri": "http://host.docker.internal:9080"}'
```

### 6. Access the Application

- **REST API**: http://localhost:3000
- **Swagger UI**: http://localhost:3000/swagger-ui.html
- **Restate Admin**: http://localhost:9070

## 📖 Project Overview

### What Makes This Different?

Traditional Event Sourcing/CQRS requires:
- ❌ EventStore (Axon Server, EventStoreDB)
- ❌ Message Broker (Kafka, RabbitMQ)
- ❌ Distributed Locks (Redis, Zookeeper)
- ❌ Saga Frameworks (Axon Saga, NServiceBus)
- ❌ Idempotency Storage
- ❌ Retry/DLQ Infrastructure

**Restate replaces ALL of this** with:
- ✅ Built-in event journaling via Virtual Object state
- ✅ Durable execution with automatic retries
- ✅ Single-writer concurrency (no locks needed)
- ✅ Exactly-once processing guarantees
- ✅ Built-in service-to-service communication

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Client (Browser/Postman)                                   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  order-api-gateway (Port 3000) - REST API                   │
│  • Spring Web • Swagger • Restate Client SDK               │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ HTTP calls to Restate Ingress
┌─────────────────────────────────────────────────────────────┐
│  Restate Runtime (Port 8080) - Durable Execution            │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ Calls handlers
┌─────────────────────────────────────────────────────────────┐
│  order-handlers (Port 9080) - Restate Services             │
│  • OrderAggregate (Virtual Object)                          │
│  • OrderEventDispatcher, OrderViewHandler (Services)       │
└─────────────────────────────────────────────────────────────┘
```

### Module Structure

```
restate-oms-demo/
├── order-domain/          → Pure business logic (no frameworks)
├── order-handlers/        → Restate services (application layer)
└── order-api-gateway/     → REST API (presentation layer)
```

**See [`docs/AGENTS.MD`](docs/AGENTS.MD) for comprehensive architecture documentation.**

## 🎯 Key Patterns

### 1. Decider Pattern (Pure Functional Business Logic)

```kotlin
// Pure function: Command + State → Events
fun decide(command: OrderCommand, state: Order?): Flow<OrderEvent>

// Pure function: State + Event → New State
fun evolve(state: Order?, event: OrderEvent): Order?
```

**Benefits**: Testable, framework-agnostic, clear business rules

### 2. Virtual Objects for Event Sourcing

```kotlin
@RestateVirtualObject
class OrderAggregate {
    @Handler  // Single-writer per orderId
    suspend fun handle(ctx: ObjectContext, command: OrderCommand)

    @Handler @Shared  // Concurrent reads allowed
    suspend fun get(ctx: SharedObjectContext): Order?
}
```

**Benefits**: Durable state, automatic retries, no distributed locks

### 3. ChangeHandler Pattern (Loose Coupling)

```kotlin
interface ChangeHandler {
    suspend fun handleChange(ctx, aggregateKey, currentState, events)
}
```

**Benefits**: Open-Closed Principle, independent retry, extensible

## 🔧 Common Tasks

### Building

```bash
./gradlew clean build
./gradlew :order-domain:build
./gradlew :order-handlers:build
./gradlew :order-api-gateway:build
```

### Regenerating KSP Code

After adding new `@RestateService` or `@RestateVirtualObject`:

```bash
./gradlew :order-handlers:kspKotlin
```

### Testing the API

```bash
# Create order
curl -X POST http://localhost:3000/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "items": [
      {"productId": "p1", "productName": "Widget", "quantity": 2, "price": 29.99}
    ]
  }'

# Get order
curl http://localhost:3000/orders/{orderId}

# Get all orders
curl http://localhost:3000/orders

# Confirm order
curl -X POST http://localhost:3000/orders/{orderId}/confirm

# Ship order
curl -X POST http://localhost:3000/orders/{orderId}/ship \
  -H "Content-Type: application/json" \
  -d '{"trackingNumber": "TRACK-123"}'

# Cancel order
curl -X POST http://localhost:3000/orders/{orderId}/cancel \
  -H "Content-Type: application/json" \
  -d '{"reason": "Customer request"}'
```

### Resetting State

```bash
# Stop Restate and delete all data
docker-compose down -v

# Start fresh
docker-compose up -d

# Re-register services
curl -X POST http://localhost:9070/deployments \
  -H 'content-type: application/json' \
  -d '{"uri": "http://host.docker.internal:9080"}'
```

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| Build System | Gradle 9.2.1 (Kotlin DSL) |
| Language | Kotlin 2.2.21 |
| Framework | Spring Boot 4.0.0 |
| Restate SDK | 2.4.1 |
| Java Version | 21 (via toolchain) |
| Serialization | KotlinX Serialization (domain), Jackson (REST) |
| Code Generation | KSP (Kotlin Symbol Processing) |
| API Documentation | SpringDoc OpenAPI 2.8.0 |
| Infrastructure | Docker Compose |

## 📚 Documentation

- [`docs/AGENTS.MD`](docs/AGENTS.MD) - **Comprehensive guide for AI agents** working on this codebase

## 🤝 Contributing

When working on this codebase:

**DO:**
- ✅ Use pure functions in order-domain
- ✅ Regenerate KSP after adding services
- ✅ Follow ChangeHandler pattern for read side
- ✅ Use `ctx.set()` for durable state
- ✅ Use generated clients for service calls

**DON'T:**
- ❌ Add framework annotations to order-domain
- ❌ Mix kotlinx.serialization with Jackson
- ❌ Use traditional databases (Restate manages state)
- ❌ Add distributed locks (Virtual Objects are single-writer)
- ❌ Modify dispatcher for new handlers (use bridges)

## 📝 License

This is a demonstration project for educational purposes.

## 🔗 Links

- [Restate.dev Documentation](https://docs.restate.dev/)
- [Restate Spring Boot Integration Guide](https://docs.restate.dev/components/spring-boot/)
- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

---

**Generated with Claude Code** - Restate Spring Boot Integration Demo
