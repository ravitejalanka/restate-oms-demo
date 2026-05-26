package com.learning.restate.with.boot.domain.readmodel

import kotlinx.serialization.Serializable

/**
 * Data class representing a summary view of an order.
 *
 * This read model provides a lightweight representation of order data
 * for querying and display purposes.
 */
@Serializable
data class OrderSummary(
    /** The unique identifier of the order */
    val id: String,
    
    /** The ID of the customer who placed the order */
    val customerId: String,
    
    /** The current status of the order */
    val status: String,
    
    /** The total monetary amount of the order */
    val totalAmount: Double,
    
    /** The total number of items in the order */
    val itemCount: Int
)
