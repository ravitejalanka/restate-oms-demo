package com.learning.restate.with.boot.domain.readmodel

import kotlinx.serialization.Serializable

/**
 * Read model representing a summary view of an order for querying purposes
 *
 * @property id Unique identifier of the order
 * @property customerId Identifier of the customer who placed the order
 * @property status Current status of the order
 * @property totalAmount Total monetary value of the order
 * @property itemCount Number of items in the order
 */
@Serializable
data class OrderSummary(
    val id: String,
    val customerId: String,
    val status: String,
    val totalAmount: Double,
    val itemCount: Int
)
