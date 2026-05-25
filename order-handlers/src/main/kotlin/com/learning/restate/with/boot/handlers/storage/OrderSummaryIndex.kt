package com.learning.restate.with.boot.handlers.storage

import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

/**
 * Restate Virtual Object that maintains an index of all order IDs
 *
 * This virtual object stores a set of all order IDs to enable efficient
 * retrieval of all orders in the system without scanning the entire storage.
 */
@RestateVirtualObject
class OrderSummaryIndex {
    companion object {
        private val ORDER_IDS = stateKey<Set<String>>("order_ids")
    }

    /**
     * Handler to add an order ID to the index
     *
     * @param ctx Object context provided by Restate
     * @param orderId The order ID to add to the index
     */
    @Handler
    suspend fun addOrderId(ctx: ObjectContext, orderId: String) {
        val currentIds = ctx.get(ORDER_IDS) ?: emptySet()
        ctx.set(ORDER_IDS, currentIds + orderId)
    }

    /**
     * Handler to get all order IDs from the index
     *
     * @param ctx Shared object context provided by Restate
     * @return The set of all order IDs in the index
     */
    @Handler
    @Shared
    suspend fun getAllOrderIds(ctx: SharedObjectContext): Set<String> {
        return ctx.get(ORDER_IDS) ?: emptySet()
    }
}
