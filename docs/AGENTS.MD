# AGENTS.MD - AI Agent Guide to Restate Spring Boot Integration

> **Purpose**: This guide is specifically designed for AI agents (like Claude, GPT, etc.) working on this codebase. It provides a comprehensive understanding of the architecture, patterns, design decisions, and maintenance tasks.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture Deep Dive](#architecture-deep-dive)
3. [Key Patterns & Design Decisions](#key-patterns--design-decisions)
4. [Module Breakdown](#module-breakdown)
5. [Code Flow Analysis](#code-flow-analysis)
6. [What We Fixed & Improved](#what-we-fixed--improved)
7. [Maintenance Tasks Completed](#maintenance-tasks-completed)
8. [Common Tasks & How to Execute](#common-tasks--how-to-execute)
9. [Troubleshooting Guide](#troubleshooting-guide)
10. [Future Enhancement Opportunities](#future-enhancement-opportunities)

---

## Project Overview

### What This Project Demonstrates

This is a **production-ready implementation** of Event Sourcing and CQRS using **Restate.dev** with Spring Boot and Kotlin. The key innovation is how Restate's **Durable Execution** paradigm replaces an entire stack of infrastructure components.

### The Problem It Solves

Traditional Event Sourcing/CQRS requires:
- EventStore (Axon Server, EventStoreDB)
- Message Broker (Kafka, RabbitMQ)
- Distributed Locks (Redis, Zookeeper)
- Saga Frameworks (Axon Saga, NServiceBus)
- Idempotency Storage
- Retry/DLQ Infrastructure

**Restate replaces ALL of this** with a single runtime that provides:
- Built-in event journaling via Virtual Object state
- Durable execution with automatic retries
- Single-writer concurrency (no locks needed)
- Exactly-once processing guarantees
- Built-in service-to-service communication

### Technology Stack

```yaml
Build System: Gradle 9.2.1 (Kotlin DSL)
Language: Kotlin 2.2.21
Framework: Spring Boot 4.0.0
Restate SDK: 2.4.1
Java Version: 21 (via toolchain)
Serialization: KotlinX Serialization (domain), Jackson (REST API)
Code Generation: KSP (Kotlin Symbol Processing)
API Documentation: SpringDoc OpenAPI 2.8.0
Infrastructure: Docker Compose (Restate server)
```

---

## Architecture Deep Dive

### Multi-Module Structure

The project uses **Hexagonal Architecture** with clear separation of concerns:

```
restate-oms-demo/
├── order-domain/          → Pure business logic (no frameworks)
├── order-handlers/        → Restate services (application layer)
└── order-api-gateway/     → REST API (presentation layer)
```

### Why This Structure?

1. **order-domain**: Domain-Driven Design - pure business logic
   - No Restate annotations
   - No Spring annotations
   - Pure functions (decide/evolve)
   - Can be reused in any Kotlin project

2. **order-handlers**: Application layer with Restate
   - Contains Virtual Objects (write side)
   - Contains Services (read side)
   - Bridges domain to infrastructure

3. **order-api-gateway**: Presentation layer
   - REST API endpoints
   - Only uses Restate **client** SDK
   - Calls handlers via Restate ingress

### Port Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Client (Browser/Postman)                                   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  order-api-gateway (Port 3000) - REST API                   │
│  • Spring Web                                               │
│  • Swagger/OpenAPI                                          │
│  • Restate Client SDK (NOT server)                         │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ HTTP calls to Restate Ingress
┌─────────────────────────────────────────────────────────────┐
│  Restate Runtime (Port 8080) - Ingress                      │
│  • Routes requests to handlers                              │
│  • Provides durable execution guarantees                    │
│  • Manages Virtual Object state                             │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ Calls handlers
┌─────────────────────────────────────────────────────────────┐
│  order-handlers (Port 9080) - Restate SDK Endpoint         │
│  • OrderAggregate (Virtual Object)                          │
│  • OrderEventDispatcher (Service)                           │
│  • OrderViewHandler, OrderSagaHandler (Services)           │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Patterns & Design Decisions

### 1. The Decider Pattern (Pure Functional Business Logic)

**Location**: `order-domain/src/main/kotlin/com/learning/restate/with/boot/domain/OrderDecider.kt`

**Purpose**: Separate business logic from infrastructure

```kotlin
// Pure function: Command + State → Events
fun decide(command: OrderCommand, state: Order?): Flow<OrderEvent> = flow {
    when (command) {
        is OrderCommand.Create -> {
            if (state == null) {
                emit(OrderEvent.Created(...))
            } else {
                emit(OrderEvent.Rejected(...))
            }
        }
        // More command handlers...
    }
}

// Pure function: State + Event → New State
fun evolve(state: Order?, event: OrderEvent): Order? {
    return when (event) {
        is OrderEvent.Created -> Order(...)
        is OrderEvent.Confirmed -> state?.copy(status = Status.CONFIRMED)
        // More event handlers...
    }
}
```

**Why This Pattern?**
- ✅ **Testable**: No mocks needed, pure functions
- ✅ **Framework-agnostic**: No Spring/Restate dependencies
- ✅ **Clear business rules**: All validation in one place
- ✅ **Reusable**: Can be used in any Kotlin project

**AI Agent Note**: When adding new commands/events:
1. Add to `OrderCommand.kt` or `OrderEvent.kt`
2. Implement logic in `decide()` function
3. Implement state evolution in `evolve()` function
4. **DO NOT** add framework annotations here

---

### 2. Virtual Objects for Event Sourcing

**Location**: `order-handlers/src/main/kotlin/com/learning/restate/with/boot/handlers/aggregate/OrderAggregate.kt`

**Pattern**: Event-sourced aggregate with snapshot optimization

```kotlin
@RestateVirtualObject  // Marks as stateful entity
class OrderAggregate {
    companion object {
        // Type-safe state keys
        private val EVENTS = stateKey<List<OrderEvent>>("events")
        private val STATE = stateKey<Order>("state")
    }

    @Handler  // Write handler - exclusive access per orderId
    suspend fun handle(ctx: ObjectContext, command: OrderCommand) {
        // 1. Load snapshot state
        val snapshotState = ctx.get(STATE)
        val events = ctx.get(EVENTS) ?: emptyList()

        // 2. Rebuild state from events if snapshot missing
        val state = snapshotState
            ?: events.fold(null as Order?) { s, e -> evolve(s, e) }

        // 3. Use pure domain logic
        val newEvents = decide(command, state).toList()

        if (newEvents.isNotEmpty()) {
            // 4. Persist events (durable, survives crashes)
            ctx.set(EVENTS, events + newEvents)

            // 5. Update snapshot (optimization)
            val newState = newEvents.fold(state) { s, e -> evolve(s, e) }
            newState?.let { ctx.set(STATE, newState) }

            // 6. Publish to read side
            OrderEventDispatcherClient.fromContext(ctx)
                .send()
                .publishBatch(PublishBatchRequest(newState, newEvents))
        }
    }

    @Handler
    @Shared  // Read handler - concurrent access allowed
    suspend fun get(ctx: SharedObjectContext): Order? {
        // Return snapshot or rebuild from events
    }
}
```

**Key Insights for AI Agents**:

1. **Virtual Objects = Single Writer**:
   - Only ONE `@Handler` runs per `orderId` at a time
   - No distributed locks needed
   - Restate queues concurrent requests

2. **State Storage**:
   - `ctx.set()` is durable - survives crashes
   - State is partitioned by Virtual Object key (`orderId`)
   - Each order has isolated state

3. **Snapshot Pattern**:
   - `STATE` key = optimized snapshot
   - `EVENTS` key = full event history
   - Rebuild from events if snapshot missing (for event replay)

4. **@Shared Annotation**:
   - Read-only handlers can run concurrently
   - Use for queries (`get()`, `getEvents()`)

---

### 3. ChangeHandler Pattern (Loose Coupling)

**Problem**: How to decouple write side (aggregates) from read side (projections, sagas, notifications)?

**Solution**: ChangeHandler interface + Bridge pattern

#### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  OrderAggregate (WRITE SIDE)                                │
│  Produces events, doesn't know about read side              │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ publishBatch()
┌─────────────────────────────────────────────────────────────┐
│  OrderEventDispatcher                                       │
│  Auto-discovers all ChangeHandler implementations           │
│  Fans out to each handler independently                     │
└─────────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
   ┌─────────┐      ┌─────────┐      ┌─────────┐
   │ Bridge  │      │ Bridge  │      │ Bridge  │
   │   ↓     │      │   ↓     │      │   ↓     │
   │ Handler │      │ Handler │      │ Handler │
   └─────────┘      └─────────┘      └─────────┘
    OrderView       OrderSaga      Console
```

#### Implementation

**Step 1: Interface** (`order-handlers/src/main/kotlin/com/learning/restate/with/boot/handlers/port/ChangeHandler.kt`)

```kotlin
interface ChangeHandler {
    suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    )
}
```

**Step 2: Restate Service** (actual handler logic)

```kotlin
@RestateService
class OrderViewHandler {
    @Handler
    suspend fun handle(ctx: Context, request: ChangeHandlerRequest) {
        request.events.forEach { event ->
            when (event) {
                is OrderEvent.Created -> updateReadModel(ctx, event)
                // Handle other events...
            }
        }
    }
}
```

**Step 3: Bridge Component** (connects interface to service)

```kotlin
@Component  // Spring discovers this
class OrderViewChangeHandlerBridge : ChangeHandler {
    override suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    ) {
        // Use generated Restate client
        OrderViewHandlerClient.fromContext(ctx)
            .send()  // Fire-and-forget, independent retry
            .handle(ChangeHandlerRequest(aggregateKey, currentState, events))
    }
}
```

**Step 4: Dispatcher** (auto-discovers bridges)

```kotlin
@RestateService
class OrderEventDispatcher(
    private val changeHandlers: List<ChangeHandler>  // Spring injects ALL
) {
    @Handler
    suspend fun publishBatch(ctx: Context, request: PublishBatchRequest) {
        // Fan out to ALL handlers
        changeHandlers.forEach { handler ->
            handler.handleChange(ctx, aggregateKey, currentState, events)
        }
    }
}
```

**Why This Pattern?**

✅ **Open-Closed Principle**: Add new handlers without modifying dispatcher
✅ **Loose Coupling**: Write side doesn't know about read side
✅ **Independent Retry**: Each handler invocation is separate
✅ **Type Safety**: Generated clients provide compile-time checks
✅ **Testability**: Mock `ChangeHandler` interface in tests

**AI Agent Note - Adding New Handler**:

1. Create handler in `order-handlers/src/main/kotlin/.../service/`
   ```kotlin
   @RestateService
   class EmailChangeHandler { ... }
   ```

2. Create bridge in `order-handlers/src/main/kotlin/.../bridge/`
   ```kotlin
   @Component
   class EmailChangeHandlerBridge : ChangeHandler { ... }
   ```

3. **NO changes to OrderEventDispatcher** - Spring auto-discovers!

---

### 4. CQRS Read Models

**Write Side**: `OrderAggregate` (event-sourced, full history)
**Read Side**: `OrderViewHandler` + `OrderSummaryStorage` (optimized for queries)

#### Read Model Storage

```kotlin
@RestateVirtualObject
class OrderSummaryStorage {
    companion object {
        private val SUMMARY = stateKey<OrderSummary>("summary")
    }

    @Handler
    suspend fun setSummary(ctx: ObjectContext, summary: OrderSummary) {
        ctx.set(SUMMARY, summary)  // Durable K/V storage
    }

    @Handler
    @Shared
    suspend fun getSummary(ctx: SharedObjectContext): OrderSummary? {
        return ctx.get(SUMMARY)
    }
}
```

#### Projection Handler

```kotlin
@RestateService
class OrderViewHandler {
    @Handler
    suspend fun handle(ctx: Context, request: ChangeHandlerRequest) {
        request.events.forEach { event ->
            when (event) {
                is OrderEvent.Created -> {
                    // Create denormalized view
                    val summary = OrderSummary(
                        id = event.id,
                        customerId = event.customerId,
                        status = "PENDING",
                        totalAmount = event.totalAmount,
                        itemCount = event.orderItems.size
                    )
                    // Store in Virtual Object
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .setSummary(summary)
                }
                // Handle other events...
            }
        }
    }

    @Handler
    suspend fun getAllOrders(ctx: Context): List<OrderSummary> {
        // Query all order summaries (fast!)
    }
}
```

**Why Two Models?**

| Aspect | Write Side (Aggregate) | Read Side (Summary) |
|--------|------------------------|---------------------|
| **Purpose** | Source of truth | Optimized queries |
| **Storage** | Full events + snapshot | Denormalized view |
| **Query** | Rebuild from events | Direct K/V lookup |
| **Use Case** | Commands, event history | List views, search |

---

## Module Breakdown

### Module 1: order-domain

**Purpose**: Pure business logic, no framework dependencies

**Key Files**:
```
order-domain/src/main/kotlin/com/learning/restate/with/boot/domain/
├── Order.kt              → Aggregate state (Order, LineItem, Status)
├── OrderCommand.kt       → Commands (Create, Confirm, Ship, Cancel, Deliver)
├── OrderEvent.kt         → Events (Created, Confirmed, Shipped, Delivered, Rejected)
├── OrderDecider.kt       → Pure functions (decide, evolve)
└── readmodel/
    └── OrderSummary.kt   → CQRS read model projection
```

**Dependencies** (from `build.gradle.kts`):
```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
```

**AI Agent Note**: This module should NEVER have:
- Spring annotations
- Restate annotations
- Any framework dependencies

---

### Module 2: order-handlers

**Purpose**: Restate application layer (Virtual Objects, Services, Bridges)

**Structure**:
```
order-handlers/src/main/kotlin/com/learning/restate/with/boot/handlers/
├── HandlersApplication.kt               → Spring Boot entry point
├── aggregate/
│   └── OrderAggregate.kt                → Virtual Object (write side)
├── service/
│   ├── OrderEventDispatcher.kt          → Event fan-out
│   ├── OrderViewHandler.kt              → CQRS projections
│   ├── OrderSagaHandler.kt              → Process manager
│   ├── OrderRetentionService.kt         → Data cleanup
│   └── ConsoleChangeHandler.kt          → Notifications
├── storage/
│   ├── OrderSummaryStorage.kt           → K/V store (Virtual Object)
│   └── OrderSummaryIndex.kt             → Index for listings
├── bridge/
│   ├── OrderViewChangeHandlerBridge.kt  → Connects to OrderViewHandler
│   ├── OrderSagaChangeHandlerBridge.kt  → Connects to OrderSagaHandler
│   └── ConsoleChangeHandlerBridge.kt    → Connects to ConsoleChangeHandler
├── dto/
│   ├── ChangeHandlerRequest.kt          → Event notification DTO
│   ├── PublishBatchRequest.kt           → Batch event DTO
│   └── OrderPurgeResult.kt              → Retention result DTO
└── port/
    └── ChangeHandler.kt                 → Generic interface
```

**Configuration** (`application.properties`):
```properties
spring.application.name=order-handlers
restate.sdk.http.port=9080  # Restate SDK endpoint
```

**Dependencies** (key ones):
```kotlin
dependencies {
    implementation(project(":order-domain"))
    implementation("dev.restate:sdk-spring-boot-kotlin-starter:2.4.1")
    ksp("dev.restate:sdk-api-kotlin-gen:2.4.1")  // Code generation
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
```

**Generated Code** (by KSP):
- `OrderAggregateClient.kt`
- `OrderEventDispatcherClient.kt`
- `OrderViewHandlerClient.kt`
- `OrderSagaHandlerClient.kt`
- `ConsoleChangeHandlerClient.kt`
- `OrderSummaryStorageClient.kt`
- `OrderSummaryIndexClient.kt`
- `OrderRetentionServiceClient.kt`

**AI Agent Note**:
- This module uses Restate **server** SDK
- KSP generates client code from `@RestateService` and `@RestateVirtualObject`
- Regenerate clients: `./gradlew :order-handlers:kspKotlin`

---

### Module 3: order-api-gateway

**Purpose**: REST API client-facing layer

**Structure**:
```
order-api-gateway/src/main/kotlin/com/learning/restate/with/boot/gateway/
├── ApiGatewayApplication.kt              → Spring Boot entry point
├── config/
│   └── RestateClientConfig.kt            → Restate client bean
├── controller/
│   └── OrderRestController.kt            → REST endpoints
└── dto/
    └── OrderRestDto.kt                   → Request/Response DTOs
```

**Configuration** (`application.properties`):
```properties
spring.application.name=order-api-gateway
server.port=3000                          # REST API port
restate.client.base-uri=http://localhost:8080  # Restate ingress
springdoc.swagger-ui.enabled=true         # Swagger UI
```

**Dependencies** (key ones):
```kotlin
dependencies {
    implementation(project(":order-domain"))
    implementation(project(":order-handlers"))  // For generated clients
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("dev.restate:sdk-kotlin-http:2.4.1")  // Client only!
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")
}
```

**AI Agent Note**:
- Uses Restate **client** SDK only (NOT server)
- DTOs use Jackson (NOT kotlinx.serialization)
- Calls handlers via generated clients from order-handlers

---

## Code Flow Analysis

### Complete Request Flow: Creating an Order

Let's trace a request from HTTP to database with line-by-line analysis.

#### Step 1: REST API Request

**File**: `order-api-gateway/src/main/kotlin/.../controller/OrderRestController.kt`

```kotlin
@PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
suspend fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
    // 1. Generate orderId
    val orderId = request.id ?: UUID.randomUUID().toString()

    // 2. Create domain command
    val command = OrderCommand.Create(
        id = orderId,
        customerId = request.customerId,
        lineItems = request.items
    )

    // 3. Execute via helper
    return executeCommand(orderId, command, "Order created successfully", ...)
}

private suspend fun executeCommand(...): ResponseEntity<OrderResponse> {
    return runCatching {
        // 4. Get generated client for OrderAggregate Virtual Object
        val aggregateClient = OrderAggregateClient.fromClient(client, orderId)

        // 5. Call Restate (non-blocking, durable)
        aggregateClient.handle(command)

        // 6. Get results
        Pair(aggregateClient.get(), aggregateClient.getEvents())
    }.fold(
        onFailure = { /* Error response */ },
        onSuccess = { (order, events) -> /* Success response */ }
    )
}
```

**What happens here?**
- Controller receives HTTP POST
- Creates domain command (no business logic yet)
- Uses **OrderAggregateClient** (generated by KSP)
- `fromClient(client, orderId)` → targets specific Virtual Object instance
- `handle(command)` → sends to Restate ingress at `http://localhost:8080`

---

#### Step 2: Restate Ingress Routing

**Component**: Restate Runtime (Docker container, port 8080)

**What Restate does**:
1. Receives HTTP call to `OrderAggregate/handle`
2. Looks up Virtual Object instance for `orderId`
3. Checks if another handler is running for this `orderId`
   - **If YES**: Queues request (single-writer guarantee)
   - **If NO**: Routes to order-handlers at `http://localhost:9080`
4. Creates durable execution journal for this invocation

---

#### Step 3: Virtual Object Handler

**File**: `order-handlers/src/main/kotlin/.../aggregate/OrderAggregate.kt`

```kotlin
@RestateVirtualObject
class OrderAggregate {
    companion object {
        private val EVENTS = stateKey<List<OrderEvent>>("events")
        private val STATE = stateKey<Order>("state")
    }

    @Handler
    suspend fun handle(ctx: ObjectContext, command: OrderCommand) {
        // STEP 3.1: Load existing state (if any)
        val snapshotState = ctx.get(STATE)  // Null for new orders
        val events = ctx.get(EVENTS) ?: emptyList()

        // STEP 3.2: Rebuild state from events (event sourcing)
        val state = snapshotState
            ?: events.fold(null as Order?) { s, e -> evolve(s, e) }

        // STEP 3.3: Apply business logic (pure domain function)
        val newEvents = decide(command, state).toList()
        // For Create command on new order:
        // → [OrderEvent.Created(...)]

        if (newEvents.isNotEmpty()) {
            // STEP 3.4: Persist events (DURABLE - survives crashes)
            ctx.set(EVENTS, events + newEvents)

            // STEP 3.5: Update snapshot (optimization)
            val newState = newEvents.fold(state) { s, e -> evolve(s, e) }
            newState?.let { ctx.set(STATE, newState) }

            // STEP 3.6: Publish to read side
            OrderEventDispatcherClient.fromContext(ctx)
                .send()  // Fire-and-forget, durable
                .publishBatch(PublishBatchRequest(newState, newEvents))
        }
    }
}
```

**What happens here?**
- Loads state from Restate K/V storage
- Calls pure `decide()` function (no side effects)
- Persists events durably via `ctx.set()`
- Publishes to dispatcher (separate Restate invocation)

---

#### Step 4: Event Dispatcher

**File**: `order-handlers/src/main/kotlin/.../service/OrderEventDispatcher.kt`

```kotlin
@RestateService
class OrderEventDispatcher(
    private val changeHandlers: List<ChangeHandler>  // Spring DI
) {
    @Handler
    suspend fun publishBatch(ctx: Context, request: PublishBatchRequest) {
        val currentState = request.currentState
        val events = request.events
        val aggregateKey = currentState?.id ?: return

        // Fan-out to ALL handlers (independent invocations)
        changeHandlers.forEach { handler ->
            handler.handleChange(ctx, aggregateKey, currentState, events)
        }
    }
}
```

**What happens here?**
- Receives batch of events
- Fans out to ALL registered ChangeHandler implementations
- Each handler call is independent (separate retry/recovery)

---

#### Step 5: Read Model Update

**File**: `order-handlers/src/main/kotlin/.../service/OrderViewHandler.kt`

```kotlin
@RestateService
class OrderViewHandler {
    @Handler
    suspend fun handle(ctx: Context, request: ChangeHandlerRequest) {
        request.events.forEach { event ->
            when (event) {
                is OrderEvent.Created -> {
                    // Create denormalized read model
                    val summary = OrderSummary(
                        id = event.id,
                        customerId = event.customerId,
                        status = "PENDING",
                        totalAmount = event.totalAmount,
                        itemCount = event.orderItems.size
                    )

                    // Store in K/V Virtual Object
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .setSummary(summary)

                    // Add to index for listings
                    OrderSummaryIndexClient.fromContext(ctx, "index")
                        .addOrderId(event.id)
                }
                // Other events...
            }
        }
    }
}
```

**What happens here?**
- Receives event notification
- Creates denormalized view (OrderSummary)
- Stores in OrderSummaryStorage Virtual Object
- Updates index for "get all orders" queries

---

#### Complete Flow Summary

```
HTTP POST /orders
    ↓
OrderRestController.createOrder()
    ↓ (creates OrderCommand.Create)
    ↓
OrderAggregateClient.handle(command)
    ↓ (HTTP to Restate ingress)
    ↓
Restate Runtime (8080)
    ↓ (routes to handler, ensures single-writer)
    ↓
OrderAggregate.handle() (9080)
    ├─ Load state: ctx.get(STATE), ctx.get(EVENTS)
    ├─ Apply logic: decide(command, state)
    ├─ Persist: ctx.set(EVENTS), ctx.set(STATE)
    └─ Publish: OrderEventDispatcherClient.send().publishBatch()
        ↓
        OrderEventDispatcher.publishBatch()
        ├─ OrderViewHandler → Updates OrderSummaryStorage
        ├─ OrderSagaHandler → Logs saga steps
        └─ ConsoleChangeHandler → Logs to console
    ↓
HTTP Response with OrderResponse
```

---

## What We Fixed & Improved

### Issue 1: order-api-gateway Dependencies

**Problem Found**:
```kotlin
// Old build.gradle.kts
dependencies {
    implementation("dev.restate:sdk-kotlin-http:${restateVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Missing: Coroutine reactor bridge
    // Missing: Jackson Kotlin module
}
```

**Issues**:
1. No `kotlinx-coroutines-reactor` - Spring Web coroutines wouldn't integrate properly
2. No `jackson-module-kotlin` - Kotlin data classes wouldn't serialize correctly
3. No `jackson-datatype-jsr310` - Java 8 date/time types unsupported

**Fix Applied**:
```kotlin
dependencies {
    implementation("dev.restate:sdk-kotlin-http:${restateVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")  // ✅ Added
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")  // ✅ Added
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")  // ✅ Added
}
```

---

### Issue 2: ApiGatewayApplication Configuration

**Problem Found**:
```kotlin
// Old code
package com.learning.restate.with.boot.gateway

import dev.restate.client.Client
import dev.restate.sdk.http.vertx.RestateHttpServer  // ❌ Wrong import!
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}

@Bean
fun restateClient(): Client {
    return Client.connect("http://localhost:8080")  // ❌ Hardcoded URL
}
```

**Issues**:
1. `RestateHttpServer` import - belongs in order-handlers (server), not gateway (client)
2. Hardcoded URL - should read from application.properties
3. Bean defined as top-level function - not a Spring best practice
4. Mixed concerns - application class + configuration

**Fix Applied**:

```kotlin
// ApiGatewayApplication.kt - Clean separation
package com.learning.restate.with.boot.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}

// RestateClientConfig.kt - Proper configuration class
package com.learning.restate.with.boot.gateway.config

import dev.restate.client.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestateClientConfig {
    @Value("\${restate.client.base-uri:http://localhost:8080}")
    private lateinit var restateBaseUri: String

    @Bean
    fun restateClient(): Client {
        return Client.connect(restateBaseUri)
    }
}
```

**Benefits**:
- ✅ Reads from application.properties
- ✅ Proper `@Configuration` class
- ✅ Clear separation of concerns
- ✅ Removed wrong import

---

### Issue 3: DTO Serialization Mismatch

**Problem Found**:
```kotlin
// Old OrderRestDto.kt
package com.learning.restate.with.boot.gateway.dto

import kotlinx.serialization.Serializable  // ❌ Wrong serializer!

@Serializable  // ❌ This uses kotlinx.serialization
data class CreateOrderRequest(
    val id: String? = null,
    val customerId: String,
    val items: List<LineItem>
)
```

**Issue**:
- Spring Web uses **Jackson** for JSON serialization
- DTOs were annotated with `@Serializable` from **kotlinx.serialization**
- This creates conflicts and won't work properly with Spring Web

**Fix Applied**:
```kotlin
// New OrderRestDto.kt
package com.learning.restate.with.boot.gateway.dto

import com.learning.restate.with.boot.domain.LineItem
import com.learning.restate.with.boot.domain.OrderEvent

// Plain Kotlin data classes - Jackson handles them automatically
data class CreateOrderRequest(
    val id: String? = null,
    val customerId: String,
    val items: List<LineItem>
)

data class OrderResponse(
    val success: Boolean,
    val orderId: String,
    val message: String,
    val events: List<OrderEvent>
)
```

**Benefits**:
- ✅ Compatible with Spring Web's Jackson
- ✅ No annotation conflicts
- ✅ `jackson-module-kotlin` handles Kotlin data classes perfectly

---

## Maintenance Tasks Completed

### Task 1: Project Cleanup

**Goal**: Remove build artifacts, caches, and IDE files while preserving source code

**Executed Steps**:

```bash
# 1. Pre-cleanup verification
find . -path "*/src/main/*.kt" -type f | wc -l  # 25 files
du -sh .  # 118M

# 2. Delete build artifacts
rm -rf build order-domain/build order-handlers/build order-api-gateway/build

# 3. Delete Gradle cache
rm -rf .gradle

# 4. Delete IDE configuration
rm -rf .idea .cursor .claude .kotlin

# 5. Post-cleanup verification
du -sh .  # 328K (99.7% reduction!)
find . -path "*/src/main/*.kt" -type f | wc -l  # 25 files (preserved)

# 6. Test integrity
./gradlew clean build  # ✅ BUILD SUCCESSFUL
```

**Results**:
- **Space saved**: 117.7 MB (118 MB → 328 KB)
- **Source code**: 25 files preserved
- **Documentation**: 8 files preserved
- **Configuration**: All build files preserved
- **Build system**: Gradle wrapper intact
- **Reversibility**: 100% (./gradlew build regenerates everything)

---

### Task 2: Documentation Reorganization

**Goal**: Organize documentation files for better project structure

**Executed Steps**:

```bash
# 1. Create docs directory
mkdir -p docs

# 2. Move all .md files except README.md
mv ARCHITECTURE.md docs/
mv CLAUDE.md docs/
mv HELP.md docs/
mv RESET_RESTATE_SERVER.md docs/
mv RESTATE_SPRING_INTEGRATION.md docs/
mv SWAGGER_SETUP.md docs/

# 3. Move related text file
mv API_EXAMPLES.txt docs/
```

**Final Structure**:
```
restate-oms-demo/
├── README.md              ← Quick start (GitHub landing)
├── docs/                  ← All detailed docs
│   ├── AGENTS.MD          ← This file
│   ├── API_EXAMPLES.txt
│   ├── ARCHITECTURE.md
│   ├── CLAUDE.md
│   ├── HELP.md
│   ├── RESET_RESTATE_SERVER.md
│   ├── RESTATE_SPRING_INTEGRATION.md
│   └── SWAGGER_SETUP.md
├── build.gradle.kts
├── docker-compose.yml
└── [modules...]
```

**Benefits**:
- ✅ Clean root directory
- ✅ README.md visible for GitHub
- ✅ All docs in one place
- ✅ Easier to navigate

---

## Common Tasks & How to Execute

### Building the Project

```bash
# Clean build (removes previous artifacts)
./gradlew clean build

# Build specific module
./gradlew :order-domain:build
./gradlew :order-handlers:build
./gradlew :order-api-gateway:build

# Build without tests
./gradlew build -x test

# Check dependencies
./gradlew :order-api-gateway:dependencies
```

---

### Running the Application

```bash
# 1. Start Restate server
docker-compose up -d

# 2. Run handlers module
./gradlew :order-handlers:bootRun
# Or from IDE: Run HandlersApplication.kt

# 3. Run API gateway (in separate terminal)
./gradlew :order-api-gateway:bootRun
# Or from IDE: Run ApiGatewayApplication.kt

# 4. Register services with Restate
curl -X POST http://localhost:9070/deployments \
  -H 'content-type: application/json' \
  -d '{"uri": "http://host.docker.internal:9080"}'

# 5. Access application
# REST API: http://localhost:3000
# Swagger UI: http://localhost:3000/swagger-ui.html
# Restate Admin: http://localhost:9070
```

---

### Regenerating KSP Code

When you add new `@RestateService` or `@RestateVirtualObject`:

```bash
# Regenerate all KSP code
./gradlew :order-handlers:kspKotlin

# Clean and regenerate
./gradlew :order-handlers:clean :order-handlers:kspKotlin
```

Generated clients appear in:
```
order-handlers/build/generated/ksp/main/kotlin/
```

---

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

# Response: {"success": true, "orderId": "...", ...}

# Get order
curl http://localhost:3000/orders/{orderId}

# Get all orders
curl http://localhost:3000/orders

# Get event history
curl http://localhost:3000/orders/{orderId}/events

# Confirm order
curl -X POST http://localhost:3000/orders/{orderId}/confirm

# Ship order
curl -X POST http://localhost:3000/orders/{orderId}/ship \
  -H "Content-Type: application/json" \
  -d '{"trackingNumber": "TRACK-123"}'
```

---

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

---

## Troubleshooting Guide

### Issue: "Connection refused to localhost:8080"

**Cause**: Restate server not running

**Solution**:
```bash
docker-compose ps  # Check if restate is running
docker-compose up -d  # Start if not running
docker-compose logs restate  # Check logs
```

---

### Issue: "Service not found" error

**Cause**: Services not registered with Restate

**Solution**:
```bash
# Check registration
curl http://localhost:9070/deployments

# Re-register
curl -X POST http://localhost:9070/deployments \
  -H 'content-type: application/json' \
  -d '{"uri": "http://host.docker.internal:9080"}'
```

---

### Issue: "Cannot find symbol: OrderAggregateClient"

**Cause**: KSP hasn't generated client code

**Solution**:
```bash
./gradlew :order-handlers:kspKotlin
./gradlew :order-handlers:build
```

---

### Issue: Build fails with "Duplicate class" error

**Cause**: Old build artifacts conflicting

**Solution**:
```bash
./gradlew clean
rm -rf build order-*/build .gradle
./gradlew build
```

---

### Issue: Jackson serialization errors in API Gateway

**Cause**: DTOs using kotlinx.serialization instead of Jackson

**Solution**: Ensure DTOs are plain Kotlin data classes (no `@Serializable`)

```kotlin
// ❌ Wrong
@Serializable
data class MyDto(val field: String)

// ✅ Correct
data class MyDto(val field: String)
```

---

## Future Enhancement Opportunities

### 1. Add Integration Tests

**Location**: Create `order-handlers/src/test/kotlin/`

**Pattern**:
```kotlin
@SpringBootTest
class OrderAggregateTest {
    @Test
    fun `should create order successfully`() {
        // Use Restate testing SDK
    }
}
```

**Dependencies needed**:
```kotlin
testImplementation("dev.restate:sdk-testing:2.4.1")
```

---

### 2. Add OpenTelemetry Tracing

**Why**: Distributed tracing across Restate invocations

**Dependencies**:
```kotlin
implementation("io.opentelemetry:opentelemetry-api")
implementation("io.opentelemetry:opentelemetry-sdk")
```

**Configuration**: Restate has built-in OpenTelemetry support

---

### 3. Add Metrics/Monitoring

**Options**:
- Spring Boot Actuator
- Micrometer + Prometheus
- Restate metrics endpoint

**Example**:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

### 4. Add Authentication/Authorization

**Recommendation**: JWT tokens with Spring Security

**Pattern**:
```kotlin
@RestController
@RequestMapping("/orders")
class OrderRestController {
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    suspend fun createOrder(...) { ... }
}
```

---

### 5. Add Event Replay Capability

**Use Case**: Rebuild read models from event history

**Pattern**:
```kotlin
@RestateService
class EventReplayService {
    @Handler
    suspend fun replay(ctx: Context, orderId: String) {
        val events = OrderAggregateClient.fromContext(ctx, orderId)
            .getEvents()
            .await()

        // Replay to read models
        events.forEach { event ->
            OrderEventDispatcherClient.fromContext(ctx)
                .send()
                .publishBatch(...)
        }
    }
}
```

---

### 6. Add Data Retention/Archival

**Current**: `OrderRetentionService` exists but minimal

**Enhancement**: Add configurable retention policies

```kotlin
@RestateService
class OrderRetentionService {
    @Handler
    suspend fun archiveOldOrders(ctx: Context, olderThanDays: Int) {
        // Move to cold storage
        // Clear from hot Virtual Objects
    }
}
```

---

## Summary for AI Agents

### Key Takeaways

1. **This is NOT traditional Spring Boot**:
   - Uses Restate for durable execution
   - Virtual Objects replace traditional databases for state
   - Services are durably executed (survive crashes)

2. **Module Separation Matters**:
   - `order-domain`: Pure logic, no frameworks
   - `order-handlers`: Restate server SDK
   - `order-api-gateway`: Restate client SDK

3. **Generated Code is Key**:
   - KSP generates clients from annotations
   - Always regenerate after adding services
   - Clients are in `build/generated/ksp/`

4. **Two Serialization Systems**:
   - Domain: kotlinx.serialization
   - REST API: Jackson
   - Never mix them!

5. **ChangeHandler Pattern is Extensible**:
   - Add new handler = 2 files (service + bridge)
   - No changes to dispatcher needed
   - Spring auto-discovers via interface

6. **State is Durable**:
   - `ctx.set()` survives crashes
   - Virtual Objects = single-writer guarantee
   - No distributed locks needed

7. **Build System**:
   - Multi-module Gradle with Kotlin DSL
   - KSP code generation
   - Spring Boot 4.0.0 + Restate 2.4.1

### When Working on This Codebase

**DO**:
- ✅ Use pure functions in order-domain
- ✅ Regenerate KSP after adding services
- ✅ Follow ChangeHandler pattern for read side
- ✅ Use `ctx.set()` for durable state
- ✅ Use generated clients for service calls
- ✅ Test with `./gradlew build`

**DON'T**:
- ❌ Add framework annotations to order-domain
- ❌ Mix kotlinx.serialization with Jackson
- ❌ Use traditional databases (Restate manages state)
- ❌ Add distributed locks (Virtual Objects are single-writer)
- ❌ Modify dispatcher for new handlers (use bridges)
- ❌ Hardcode URLs (use application.properties)

---

## Conclusion

This project demonstrates a **radically simplified** approach to Event Sourcing and CQRS. By leveraging Restate's durable execution, we eliminate an entire stack of infrastructure while maintaining all the benefits of event-driven architecture.

The key innovation is the **ChangeHandler pattern** which provides loose coupling and extensibility without the complexity of traditional message brokers or saga frameworks.

**For AI Agents**: This codebase is production-ready and follows best practices for Spring Boot + Restate integration. When making changes, always consider the separation between domain logic (pure), application logic (Restate), and presentation logic (REST API).

---

**Document Version**: 1.0
**Last Updated**: 2025-01-03
**Maintained By**: AI Agents (Claude, GPT, etc.)
**Project**: Restate Spring Boot Integration Demo