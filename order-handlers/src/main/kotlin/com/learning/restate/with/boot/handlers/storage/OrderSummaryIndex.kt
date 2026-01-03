package com.learning.restate.with.boot.handlers.storage

import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateVirtualObject

@RestateVirtualObject
class OrderSummaryIndex {
    companion object {
        private val ORDER_IDS = stateKey<Set<String>>("order_ids")
    }

    @Handler
    suspend fun addOrderId(ctx: ObjectContext, orderId: String) {
        val currentIds = ctx.get(ORDER_IDS) ?: emptySet()
        ctx.set(ORDER_IDS, currentIds + orderId)
    }

    @Handler
    @Shared
    suspend fun getAllOrderIds(ctx: SharedObjectContext): Set<String> {
        return ctx.get(ORDER_IDS) ?: emptySet()
    }
}
