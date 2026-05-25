package com.learning.restate.with.boot.handlers.dto

/**
 * Data transfer object representing the result of an order purge operation.
 *
 * This DTO encapsulates the results of batch order purging, separating
 * orders that were successfully purged from those that failed.
 *
 * @property purgedOrders List of order IDs that were successfully purged
 * @property failedOrders List of order IDs that failed to be purged
 */
data class OrderPurgeResult(
    val purgedOrders: List<String>,
    val failedOrders: List<String>
)
