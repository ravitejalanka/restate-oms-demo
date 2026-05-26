package com.learning.restate.with.boot.handlers.storage

import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

/**
 * Virtual object for indexing order IDs.
 *
 * This class maintains an index of all order IDs to enable efficient querying
 * of all orders in the system.
 */
@RestateVirtualObject
class OrderSummaryIndex {
    companion object {
        /** State key for storing the set of order IDs */
        private val ORDER_IDS = stateKey<Set<String>>("order_ids")
    }

    /**
     * Adds an order ID to the index.
     *
     * @param ctx The object context for this Restate virtual object
     * @param orderId The order ID to add to the index
     */
    @Handler
    suspend fun addOrderId(ctx: ObjectContext, orderId: String) {
        val currentIds = ctx.get(ORDER_IDS) ?: emptySet()
        ctx.set(ORDER_IDS, currentIds + orderId)
    }

    /**
     * Retrieves all order IDs from the index.
     *
     * @param ctx The shared object context for this Restate virtual object
     * @return The set of all order IDs
     */
    @Handler
    @Shared
    suspend fun getAllOrderIds(ctx: SharedObjectContext): Set<String> {
        return ctx.get(ORDER_IDS) ?: emptySet()
    }
}
