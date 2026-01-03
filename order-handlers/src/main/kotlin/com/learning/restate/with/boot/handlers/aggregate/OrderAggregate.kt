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

@RestateVirtualObject
class OrderAggregate {
    companion object {
        private val EVENTS = stateKey<List<OrderEvent>>("events")
        private val STATE = stateKey<Order>("state")
    }

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

    @Handler
    @Shared
    suspend fun get(ctx: SharedObjectContext): Order? {
        val snapshotState = ctx.get(STATE)
        val events = ctx.get(EVENTS) ?: emptyList()

        return snapshotState
            ?: events.fold(null as Order?) { s, e -> evolve(s, e) }
    }

    @Handler
    @Shared
    suspend fun getEvents(ctx: SharedObjectContext): List<OrderEvent> {
        return ctx.get(EVENTS) ?: emptyList()
    }
}
