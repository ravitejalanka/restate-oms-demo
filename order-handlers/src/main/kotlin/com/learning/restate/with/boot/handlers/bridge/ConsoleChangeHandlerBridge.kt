package com.learning.restate.with.boot.handlers.bridge

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import com.learning.restate.with.boot.handlers.port.ChangeHandler
import com.learning.restate.with.boot.handlers.service.ConsoleChangeHandlerClient
import dev.restate.sdk.kotlin.Context
import org.springframework.stereotype.Component

@Component
class ConsoleChangeHandlerBridge : ChangeHandler {
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
