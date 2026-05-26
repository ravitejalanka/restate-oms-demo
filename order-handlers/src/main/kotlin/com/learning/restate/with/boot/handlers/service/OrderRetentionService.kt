package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.handlers.dto.OrderPurgeResult
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.slf4j.LoggerFactory

/**
 * Service for handling order retention policies.
 *
 * This service provides functionality for purging orders according to retention policies,
 * helping to manage data lifecycle and compliance requirements.
 */
@RestateService
class OrderRetentionService {
    private val log = LoggerFactory.getLogger(OrderRetentionService::class.java)

    /**
     * Purges a single order according to retention policies.
     *
     * @param ctx The Restate context
     * @param orderId The identifier of the order to purge
     * @return A success message indicating the order was purged
     */
    @Handler
    suspend fun purgeOrder(ctx: Context, orderId: String): String {
        log.info("Purging order $orderId from retention")
        return "Order $orderId purged successfully"
    }

    /**
     * Purges a batch of orders according to retention policies.
     *
     * @param ctx The Restate context
     * @param orderIds The list of order identifiers to purge
     * @return The result of the batch purge operation
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
