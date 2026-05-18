## Start Here

Before writing any code, read:
- `order-domain/src/main/kotlin/com/learning/restate/with/boot/domain/` – understand the domain events and commands (likely OrderCommand, OrderEvent, etc.)
- `CLAUDE.md` — build instructions and conventions
- `order-handlers/src/main/resources/application.yml` — Restate endpoint config
- `order-api-gateway/src/main/resources/application.yml` — gateway server port and Restate ingress URL

## Architecture Notes

This is a **CQRS/Event Sourcing** system with **no traditional databases or brokers**. State is stored exclusively inside Restate Virtual Objects. The layers:

- **order-domain**: Pure Kotlin module containing domain types, command/event definitions – no dependencies on Spring or Restate.
- **order-handlers**: Implements Restate services (`@VirtualObject`, `@Workflow`) that handle commands, produce events, and update state. These are the **write side**.
- **order-api-gateway**: Spring Web controllers that act as the **read side** and entry point for external clients. It invokes handlers via the Restate client SDK (calls go through the Restate ingress).

Do **not** add JPA repositories, Kafka clients, or event store. All persistence lives in Restate.

## Implementing a Feature — Step by Step

1. **Domain model**: Define command/event classes in `order-domain/`. Use data classes, keep them immutable.
2. **Handler service**: In `order-handlers/`, create a Restate Virtual Object (e.g., `OrderObject`) with methods annotated `@Handler` that emit events and return the new state. Reference existing `OrderWorkflow` or similar.
3. **Idempotency**: Ensure handlers are deterministic; mark `@Idempotent` if safe, otherwise Restate manages deduplication.
4. **Gateway endpoint**: In `order-api-gateway/`, add a `@RestController` method that calls `OrderObjectClient` (or Restate’s `call` API) and maps the returned view to a DTO.
5. **Tests**: Write unit tests for domain logic; integration tests for handler workflows using Restate’s test kit (if present) or Spring Boot test slices with mocked Restate client.
6. **Swagger**: Verify the new endpoint appears and is documented.

## Testing

- Run all tests: `./gradlew test`
- Run module‑specific: `./gradlew :order-handlers:test`
- No dedicated integration test profile found; use `@SpringBootTest` with `webEnvironment = RANDOM_PORT` and a TestRestateServer (if available) or mock the Restate client.
- Domain tests should be pure Kotlin unit tests.
- Ensure any new test does not require a running Restate server unless it’s an end‑to‑end test.

## PR Requirements

- Branch naming: `<type>/<short-description>` (`feat/add-approval-flow`, `fix/order-state`)
- Commit messages: conventional commits (`feat:`, `fix:`, `chore:`)
- PR must pass CI (if configured) and be reviewable by at least one team member.
- If the feature changes a Virtual Object’s state shape, consider backward compatibility (snapshot upgrades in Restate).

## Common Pitfalls

- **Do not store state outside Restate** — no JPA, no Redis. Restate is the system of record.
- **Avoid blocking calls in suspend functions** — handlers run on coroutines; use non‑blocking I/O or `Dispatchers.IO` if unavoidable.
- **Idempotency is critical** — a handler may be retried; never use random IDs or external mutable state without careful deduplication.
- **Gateways must go through Restate Ingress** — do not directly call handler HTTP endpoints; use the Restate client so that calls are routed through the runtime and benefit from durable execution.
- **Don’t add Spring Data starters** — the build does not include them, and they would conflict with the Restate‑first persistence model.