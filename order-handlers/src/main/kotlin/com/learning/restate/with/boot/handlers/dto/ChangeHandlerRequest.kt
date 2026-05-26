package com.learning.restate.with.boot.handlers.dto

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent

/**
 * DTO for change handler requests.
 *
 * This data class encapsulates the information needed to process
 * changes to an aggregate, including its current state and the
 * events that have occurred.
 *
 * @property aggregateKey The identifier of the aggregate that changed
 * @property currentState The current state of the aggregate
 * @property events The list of events that occurred
 */
data class ChangeHandlerRequest(
    val aggregateKey: String,
    val currentState: Order?,
    val events: List<OrderEvent>
)
