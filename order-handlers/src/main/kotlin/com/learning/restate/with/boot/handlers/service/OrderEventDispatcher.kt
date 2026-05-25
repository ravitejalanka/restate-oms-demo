package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.PublishBatchRequest
import com.learning.restate.with.boot.handlers.port.ChangeHandler
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.springframework.stereotype.Component

/**
 * Service responsible for distributing order events to registered change handlers.
 *
 * This service acts as a fan-out mechanism, taking batches of events from the
 * order aggregate and distributing them to all registered change handlers for
 * processing (e.g., updating read models, sending notifications).
 *
 * @property changeHandlers List of change handlers to which events should be distributed
 */
@RestateService
class OrderEventDispatcher(
    private val changeHandlers: List<ChangeHandler>
) {
    /**
     * Publish a batch of events to all registered change handlers.
     *
     * This method distributes events to all registered handlers, ensuring
     * each handler receives the complete batch for coordinated processing.
     *
     * @param ctx Restate context for the current invocation
     * @param request The batch of events to publish
     */
    @Handler
    suspend fun publishBatch(ctx: Context, request: PublishBatchRequest) {
        val currentState = request.currentState
        val events = request.events
        val aggregateKey = currentState?.id ?: return

        // Fan out to ALL handlers (independent invocations)
        changeHandlers.forEach { handler ->
            handler.handleChange(ctx, aggregateKey, currentState, events)
        }
    }
}
