package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.handlers.dto.OrderPurgeResult
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.slf4j.LoggerFactory

/**
 * Restate service for order data retention management
 *
 * This service handles purging of order data based on retention policies.
 * It provides methods for purging individual orders or batches of orders.
 */
@RestateService
class OrderRetentionService {
    private val log = LoggerFactory.getLogger(OrderRetentionService::class.java)

    /**
     * Handler that purges a single order
     *
     * @param ctx Context provided by Restate
     * @param orderId The ID of the order to purge
     * @return Confirmation message
     */
    @Handler
    suspend fun purgeOrder(ctx: Context, orderId: String): String {
        log.info("Purging order $orderId from retention")
        return "Order $orderId purged successfully"
    }

    /**
     * Handler that purges a batch of orders
     *
     * @param ctx Context provided by Restate
     * @param orderIds List of order IDs to purge
     * @return Result containing successful and failed purges
     */
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
