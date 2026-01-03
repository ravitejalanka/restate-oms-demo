package com.learning.restate.with.boot.handlers.storage

import com.learning.restate.with.boot.domain.readmodel.OrderSummary
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

@RestateVirtualObject
class OrderSummaryStorage {
    companion object {
        private val SUMMARY = stateKey<OrderSummary>("summary")
    }

    @Handler
    suspend fun setSummary(ctx: ObjectContext, summary: OrderSummary) {
        ctx.set(SUMMARY, summary)
    }

    @Handler
    @Shared
    suspend fun getSummary(ctx: SharedObjectContext): OrderSummary? {
        return ctx.get(SUMMARY)
    }

    @Handler
    suspend fun updateStatus(ctx: ObjectContext, status: String): OrderSummary? {
        val summary = ctx.get(SUMMARY) ?: return null
        val updated = summary.copy(status = status)
        ctx.set(SUMMARY, updated)
        return updated
    }
}
