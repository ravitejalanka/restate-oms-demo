package com.learning.restate.with.boot.handlers.bridge

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import com.learning.restate.with.boot.handlers.port.ChangeHandler
import com.learning.restate.with.boot.handlers.service.OrderSagaHandlerClient
import dev.restate.sdk.kotlin.Context
import org.springframework.stereotype.Component

/**
 * Bridge implementation for handling order saga changes.
 *
 * This component acts as a bridge between the order aggregate and the saga handler,
 * forwarding change notifications to coordinate complex business processes.
 */
@Component
class OrderSagaChangeHandlerBridge : ChangeHandler {
    /**
     * Handles changes to an order by forwarding them to the saga handler service.
     *
     * @param ctx The Restate context
     * @param aggregateKey The identifier of the order that changed
     * @param currentState The current state of the order
     * @param events The list of events that occurred
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
