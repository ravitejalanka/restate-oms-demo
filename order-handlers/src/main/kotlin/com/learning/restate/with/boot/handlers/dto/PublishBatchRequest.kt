package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

/**
 * DTO for publishing a batch of events.
 *
 * This data class encapsulates the information needed to publish
 * a batch of events for an aggregate.
 *
 * @property currentState The current state of the aggregate
 * @property events The list of events to publish
 */
data class PublishBatchRequest(
    val currentState: Order?,
    val events: List<OrderEvent>
)
