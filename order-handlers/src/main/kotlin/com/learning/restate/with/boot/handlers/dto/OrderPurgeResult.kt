package com.learning.restate.with.boot.handlers.dto

/**
 * DTO for the result of an order purge operation.
 *
 * @property purgedOrders List of order IDs that were successfully purged
 * @property failedOrders List of order IDs that failed to be purged
 */
data class OrderPurgeResult(
    val purgedOrders: List<String>,
    val failedOrders: List<String>
)
