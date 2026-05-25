package com.learning.restate.with.boot.handlers.storage

import com.learning.restate.with.boot.domain.readmodel.OrderSummary
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

/**
 * Restate Virtual Object for storing order summaries
 *
 * This virtual object provides storage operations for OrderSummary read models,
 * including CRUD operations and status updates.
 */
@RestateVirtualObject
class OrderSummaryStorage {
    companion object {
        private val SUMMARY = stateKey<OrderSummary>("summary")
    }

    /**
     * Handler to store an order summary
     *
     * @param ctx Object context provided by Restate
     * @param summary The order summary to store
     */
    @Handler
    suspend fun setSummary(ctx: ObjectContext, summary: OrderSummary) {
        ctx.set(SUMMARY, summary)
    }

    /**
     * Handler to retrieve an order summary
     *
     * @param ctx Shared object context provided by Restate
     * @return The stored order summary or null if not found
     */
    @Handler
    @Shared
    suspend fun getSummary(ctx: SharedObjectContext): OrderSummary? {
        return ctx.get(SUMMARY)
    }

    /**
     * Handler to update the status of an order summary
     *
     * @param ctx Object context provided by Restate
     * @param status The new status to set
     * @return The updated order summary or null if not found
     */
    @Handler
    suspend fun updateStatus(ctx: ObjectContext, status: String): OrderSummary? {
        val summary = ctx.get(SUMMARY) ?: return null
        val updated = summary.copy(status = status)
        ctx.set(SUMMARY, updated)
        return updated
    }
}
