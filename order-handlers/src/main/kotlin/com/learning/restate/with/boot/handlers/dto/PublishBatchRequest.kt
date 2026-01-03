package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

data class PublishBatchRequest(
    val currentState: Order?,
    val events: List<OrderEvent>
)
