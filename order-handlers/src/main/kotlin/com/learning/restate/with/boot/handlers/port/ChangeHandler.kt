package com.learning.restate.with.boot.handlers.port

import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import dev.restate.sdk.kotlin.Context

/**
 * Interface for handling changes to order aggregates
 *
 * Implementations of this interface receive events from order aggregates and
 * process them to maintain read models or trigger side effects.
 */
interface ChangeHandler {
    /**
     * Handle changes to an order aggregate
     *
     * @param ctx Context provided by Restate
     * @param aggregateKey The key identifying the aggregate that generated the events
     * @param currentState The current state of the order (can be null)
     * @param events List of events that occurred
     */
    suspend fun handleChange(
        ctx: Context,
        aggregateKey: String,
        currentState: Order?,
        events: List<OrderEvent>
    )
}
