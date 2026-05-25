package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

/**
 * Data class that represents a request to handle changes to an order
 */
data class ChangeHandlerRequest(
    val aggregateKey: String,
    val currentState: Order?,
    val events: List<OrderEvent>
)
