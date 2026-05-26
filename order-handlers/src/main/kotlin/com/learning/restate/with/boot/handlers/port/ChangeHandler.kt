package com.learning.restate.with.boot.handlers.port

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import dev.restate.sdk.kotlin.Context

/**
 * Interface for handling changes to aggregates.
 *
 * This interface defines the contract for components that need to respond
 * to changes in aggregate state by processing events.
 */
interface ChangeHandler {
    /**
     * Handles changes to an aggregate.
     *
     * @param ctx The Restate context
     * @param aggregateKey The identifier of the aggregate that changed
     * @param currentState The current state of the aggregate
     * @param events The list of events that occurred
     */
    suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    )
}
