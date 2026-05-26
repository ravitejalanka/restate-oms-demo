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
 * Service for dispatching order events to registered change handlers.
 *
 * This service acts as an event dispatcher, fanning out order events to all
 * registered change handlers for processing and updating read models.
 *
 * @param changeHandlers The list of change handlers to notify of events
 */
@RestateService
class OrderEventDispatcher(
    private val changeHandlers: List<ChangeHandler>
) {
    /**
     * Publishes a batch of events to all registered change handlers.
     *
     * @param ctx The Restate context
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
