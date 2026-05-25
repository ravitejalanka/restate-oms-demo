package com.learning.restate.with.boot.handlers.bridge

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import com.learning.restate.with.boot.handlers.port.ChangeHandler
import com.learning.restate.with.boot.handlers.service.ConsoleChangeHandlerClient
import dev.restate.sdk.kotlin.Context
import org.springframework.stereotype.Component

/**
 * Bridge component that forwards change handler requests to the console change handler service.
 *
 * This component implements the ChangeHandler interface and acts as a bridge
 * to the ConsoleChangeHandler service, using the generated Restate client
 * to forward requests asynchronously.
 */
@Component
class ConsoleChangeHandlerBridge : ChangeHandler {
    /**
     * Forward change handler request to the console change handler service.
     *
     * @param ctx Restate context for the current invocation
     * @param aggregateKey The identifier of the aggregate that changed
     * @param currentState The current state of the aggregate
     * @param events List of events that were generated
     */
    override suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    ) {
        // Use generated Restate client
        ConsoleChangeHandlerClient.fromContext(ctx)
            .send()
            .handle(ChangeHandlerRequest(aggregateKey, currentState, events))
    }
}
