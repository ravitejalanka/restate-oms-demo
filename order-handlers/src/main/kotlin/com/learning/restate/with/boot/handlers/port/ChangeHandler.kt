package com.learning.restate.with.boot.handlers.port

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import dev.restate.sdk.kotlin.Context

interface ChangeHandler {
    suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    )
}
