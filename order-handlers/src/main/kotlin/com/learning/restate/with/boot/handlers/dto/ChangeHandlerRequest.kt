package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

data class ChangeHandlerRequest(
    val aggregateKey: String,
    val currentState: Order?,
    val events: List<OrderEvent>
)
