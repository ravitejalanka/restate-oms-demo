package com.learning.restate.with.boot.handlers.storage

import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

/**
 * Virtual object for maintaining an index of all order IDs.
 *
 * This class provides operations for managing a collection of order IDs,
 * allowing for efficient retrieval of all orders in the system.
 */
@RestateVirtualObject
class OrderSummaryIndex {
    companion object {
        private val ORDER_IDS = stateKey<Set<String>>("order_ids")
    }

    /**
     * Add an order ID to the index.
     *
     * @param ctx Object context for the current invocation
     * @param orderId The order ID to add to the index
     */
    @Handler
    suspend fun addOrderId(ctx: ObjectContext, orderId: String) {
        val currentIds = ctx.get(ORDER_IDS) ?: emptySet()
        ctx.set(ORDER_IDS, currentIds + orderId)
    }

    /**
     * Retrieve all order IDs from the index.
     *
     * @param ctx Shared object context for the current invocation
     * @return Set of all order IDs in the index
     */
    @Handler
    @Shared
    suspend fun getAllOrderIds(ctx: SharedObjectContext): Set<String> {
        return ctx.get(ORDER_IDS) ?: emptySet()
    }
}
