package com.learning.restate.with.boot.handlers.storage

import com.learning.restate.with.boot.domain.readmodel.OrderSummary
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

/**
 * Virtual object for storing order summary data.
 *
 * This class manages the persistence of order summary read models,
 * providing operations to store, retrieve, and update order summaries.
 */
@RestateVirtualObject
class OrderSummaryStorage {
    companion object {
        /** State key for storing the order summary */
        private val SUMMARY = stateKey<OrderSummary>("summary")
    }

    /**
     * Sets the order summary for this order.
     *
     * @param ctx The object context for this Restate virtual object
     * @param summary The order summary to store
     */
    @Handler
    suspend fun setSummary(ctx: ObjectContext, summary: OrderSummary) {
        ctx.set(SUMMARY, summary)
    }

    /**
     * Retrieves the order summary for this order.
     *
     * @param ctx The shared object context for this Restate virtual object
     * @return The order summary, or null if not found
     */
    @Handler
    @Shared
    suspend fun getSummary(ctx: SharedObjectContext): OrderSummary? {
        return ctx.get(SUMMARY)
    }

    /**
     * Updates the status of the order summary.
     *
     * @param ctx The object context for this Restate virtual object
     * @param status The new status to set
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
