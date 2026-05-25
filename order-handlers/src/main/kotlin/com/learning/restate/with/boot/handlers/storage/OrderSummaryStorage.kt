package com.learning.restate.with.boot.handlers.storage

import com.learning.restate.with.boot.domain.readmodel.OrderSummary
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

/**
 * Virtual object for storing and managing order summary read models.
 *
 * This class provides storage operations for order summary projections,
 * including creating, retrieving, and updating order summary information.
 */
@RestateVirtualObject
class OrderSummaryStorage {
    companion object {
        private val SUMMARY = stateKey<OrderSummary>("summary")
    }

    /**
     * Store or update an order summary.
     *
     * @param ctx Object context for the current invocation
     * @param summary The order summary to store
     */
    @Handler
    suspend fun setSummary(ctx: ObjectContext, summary: OrderSummary) {
        ctx.set(SUMMARY, summary)
    }

    /**
     * Retrieve an order summary by its ID.
     *
     * @param ctx Shared object context for the current invocation
     * @return The order summary if found, null otherwise
     */
    @Handler
    @Shared
    suspend fun getSummary(ctx: SharedObjectContext): OrderSummary? {
        return ctx.get(SUMMARY)
    }

    /**
     * Update the status of an existing order summary.
     *
     * @param ctx Object context for the current invocation
     * @param status The new status value
     * @return The updated order summary, or null if not found
     */
    @Handler
    suspend fun updateStatus(ctx: ObjectContext, status: String): OrderSummary? {
        val summary = ctx.get(SUMMARY) ?: return null
        val updated = summary.copy(status = status)
        ctx.set(SUMMARY, updated)
        return updated
    }
}
