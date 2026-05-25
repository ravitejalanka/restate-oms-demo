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
 * Restate service that dispatches order events to multiple change handlers
 *
 * This service implements the fan-out pattern by distributing order events to
 * all registered change handlers. Each handler can process the events independently
 * to maintain different read models or trigger side effects like notifications.
 *
 * @property changeHandlers List of handlers that will receive the events
 */
@RestateService
class OrderEventDispatcher(
    private val changeHandlers: List<ChangeHandler>
) {
    /**
     * Handler that publishes events to all registered change handlers
     *
     * This method receives a batch of events and forwards them to each registered
     * change handler. Each handler processes the events independently.
     *
     * @param ctx Context provided by Restate
     * @param request Contains the events and current state to distribute
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
