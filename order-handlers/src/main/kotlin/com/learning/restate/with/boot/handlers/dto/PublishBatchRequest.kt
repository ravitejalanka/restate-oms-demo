package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

/**
 * Data transfer object representing a batch of events to be published to change handlers.
 *
 * This DTO encapsulates the information needed by change handlers to process
 * a batch of events, including the current state of the aggregate and the
 * list of events that were generated.
 *
 * @property currentState The current state of the order after applying events
 * @property events The list of events that were generated and need to be processed
 */
data class PublishBatchRequest(
    val currentState: Order?,
    val events: List<OrderEvent>
)
