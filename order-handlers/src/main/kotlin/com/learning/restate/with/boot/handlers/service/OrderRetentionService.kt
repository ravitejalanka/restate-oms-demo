package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.handlers.dto.OrderPurgeResult
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.slf4j.LoggerFactory

@RestateService
class OrderRetentionService {
    private val log = LoggerFactory.getLogger(OrderRetentionService::class.java)

    @Handler
    suspend fun purgeOrder(ctx: Context, orderId: String): String {
        log.info("Purging order $orderId from retention")
        return "Order $orderId purged successfully"
    }

    @Handler
    suspend fun purgeBatch(ctx: Context, orderIds: List<String>): OrderPurgeResult {
        val purged = mutableListOf<String>()
        val failed = mutableListOf<String>()

        orderIds.forEach { orderId ->
            try {
                purgeOrder(ctx, orderId)
                purged.add(orderId)
            } catch (e: Exception) {
                failed.add(orderId)
            }
        }

        return OrderPurgeResult(purged, failed)
    }
}
