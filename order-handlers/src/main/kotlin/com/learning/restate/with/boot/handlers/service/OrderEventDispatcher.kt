package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.PublishBatchRequest
import com.learning.restate.with.boot.handlers.port.ChangeHandler
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.springframework.stereotype.Component

@RestateService
class OrderEventDispatcher(
    private val changeHandlers: List<ChangeHandler>
) {
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
