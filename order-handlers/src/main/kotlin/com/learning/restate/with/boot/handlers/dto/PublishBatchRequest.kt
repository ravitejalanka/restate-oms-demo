package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

/**
 * DTO for publishing a batch of events to change handlers
 */
data class PublishBatchRequest(
    val currentState: Order?,
    val events: List<OrderEvent>
)
