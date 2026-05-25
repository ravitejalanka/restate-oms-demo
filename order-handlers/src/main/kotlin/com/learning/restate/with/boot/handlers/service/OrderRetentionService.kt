package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.handlers.dto.OrderPurgeResult
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.slf4j.LoggerFactory

/**
 * Service responsible for managing order data retention policies.
 *
 * This service provides functionality to purge orders from the system
 * according to retention policies, either individually or in batches.
 */
@RestateService
class OrderRetentionService {
    private val log = LoggerFactory.getLogger(OrderRetentionService::class.java)

    /**
     * Purge a single order from the system based on retention policies.
     *
     * @param ctx Restate context for the current invocation
     * @param orderId The unique identifier of the order to purge
     * @return A success message confirming the purge operation
     */
    @Handler
    suspend fun purgeOrder(ctx: Context, orderId: String): String {
        log.info("Purging order $orderId from retention")
        return "Order $orderId purged successfully"
    }

    /**
     * Purge multiple orders from the system in a batch operation.
     *
     * This method attempts to purge each order in the provided list,
     * collecting results for successful and failed operations separately.
     *
     * @param ctx Restate context for the current invocation
     * @param orderIds List of order identifiers to purge
     * @return Result object containing lists of successfully purged and failed orders
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
