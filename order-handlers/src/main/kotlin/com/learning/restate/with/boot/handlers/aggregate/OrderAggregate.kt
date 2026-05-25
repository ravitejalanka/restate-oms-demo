package com.learning.restate.with.boot.handlers.aggregate

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderCommand
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.domain.decide
import com.learning.restate.with.boot.domain.evolve
import com.learning.restate.with.boot.handlers.dto.PublishBatchRequest
import com.learning.restate.with.boot.handlers.service.OrderEventDispatcherClient
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject
import kotlinx.coroutines.flow.toList

/**
 * Virtual object representing the order aggregate root in the Restate system.
 *
 * This class implements the aggregate pattern, maintaining the consistency boundary
 * for orders. It handles commands, generates events, persists state changes,
 * and publishes events to the read side for projections.
 */
@RestateVirtualObject
class OrderAggregate {
    companion object {
        private val EVENTS = stateKey<List<OrderEvent>>("events")
        private val STATE = stateKey<Order>("state")
    }

    /**
     * Handle an incoming command by applying business logic and generating events.
     *
     * This method loads the current state, applies the command using domain logic,
     * persists any resulting events, updates snapshots, and publishes events to
     * read-side handlers.
     *
     * @param ctx Object context for the current invocation
     * @param command The command to process
     */
    @Handler
    suspend fun handle(ctx: ObjectContext, command: OrderCommand) {
        // Load existing state
        val snapshotState = ctx.get(STATE)
        val events = ctx.get(EVENTS) ?: emptyList()

        // Rebuild state from events if snapshot is missing
        val state = snapshotState
            ?: events.fold(null as Order?) { s, e -> evolve(s, e) }

        // Apply business logic (pure domain function)
        val newEvents = decide(command, state).toList()

        if (newEvents.isNotEmpty()) {
            // Persist events (durable - survives crashes)
            ctx.set(EVENTS, events + newEvents)

            // Update snapshot (optimization)
            val newState = newEvents.fold(state) { s, e -> evolve(s, e) }
            newState?.let { ctx.set(STATE, newState) }

            // Publish to read side
            OrderEventDispatcherClient.fromContext(ctx)
                .send()
                .publishBatch(PublishBatchRequest(newState, newEvents))
        }
    }

    /**
     * Retrieve the current state of the order.
     *
     * This method returns the current snapshot state if available,
     * otherwise reconstructs it by replaying all persisted events.
     *
     * @param ctx Shared object context for the current invocation
     * @return The current order state, or null if the order doesn't exist
     */
    @Handler
    @Shared
    suspend fun get(ctx: SharedObjectContext): Order? {
        val snapshotState = ctx.get(STATE)
        val events = ctx.get(EVENTS) ?: emptyList()

        return snapshotState
            ?: events.fold(null as Order?) { s, e -> evolve(s, e) }
    }

    /**
     * Retrieve the complete event history for this order.
     *
     * This method returns all events that have been persisted for this order.
     *
     * @param ctx Shared object context for the current invocation
     * @return List of all events associated with this order
     */
    @Handler
    @Shared
    suspend fun getEvents(ctx: SharedObjectContext): List<OrderEvent> {
        return ctx.get(EVENTS) ?: emptyList()
    }
}
