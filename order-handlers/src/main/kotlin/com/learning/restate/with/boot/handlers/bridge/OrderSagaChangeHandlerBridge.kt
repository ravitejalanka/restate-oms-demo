package com.learning.restate.with.boot.handlers.bridge

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import com.learning.restate.with.boot.handlers.port.ChangeHandler
import com.learning.restate.with.boot.handlers.service.OrderSagaHandlerClient
import dev.restate.sdk.kotlin.Context
import org.springframework.stereotype.Component

/**
 * Bridge component that connects the change handler pattern to the OrderSagaHandler
 *
 * This component implements the ChangeHandler interface and forwards events to the
 * OrderSagaHandler Restate service using the generated client.
 */
@Component
class OrderSagaChangeHandlerBridge : ChangeHandler {
    /**
     * Handle changes by forwarding to the OrderSagaHandler
     *
     * @param ctx Context provided by Restate
     * @param aggregateKey The key identifying the aggregate that generated the events
     * @param currentState The current state of the order (can be null)
     * @param events List of events that occurred
     */
    override suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    ) {
        // Use generated Restate client
        OrderSagaHandlerClient.fromContext(ctx)
            .send()
            .handle(ChangeHandlerRequest(aggregateKey, currentState, events))
    }
}
