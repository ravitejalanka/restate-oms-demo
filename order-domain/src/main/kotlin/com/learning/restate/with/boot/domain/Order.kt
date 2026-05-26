package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable

/**
 * Represents an order in the system.
 *
 * @property id Unique identifier for the order
 * @property customerId Identifier of the customer who placed the order
 * @property lineItems List of items in the order
 * @property status Current status of the order
 * @property version Version number for optimistic locking
 */
@Serializable
data class Order(
    val id: String,
    val customerId: String,
    val lineItems: List<LineItem>,
    val status: Status,
    val version: Int = 0
)

/**
 * Represents a line item in an order.
 *
 * @property productId Unique identifier of the product
 * @property productName Name of the product
 * @property quantity Quantity of the product ordered
 * @property price Price per unit of the product
 */
@Serializable
data class LineItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)

/**
 * Enum representing the possible statuses of an order.
 */
@Serializable
enum class Status {
    /** Order has been created but not yet confirmed */
    PENDING,
    
    /** Order has been confirmed and is ready for processing */
    CONFIRMED,
    
    /** Order has been shipped */
    SHIPPED,
    
    /** Order has been delivered to the customer */
    DELIVERED,
    
    /** Order has been cancelled */
    CANCELLED,
    
    /** Order has been rejected */
    REJECTED
}
