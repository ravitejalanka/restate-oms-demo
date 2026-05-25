package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Sealed class representing all possible events that can occur to an Order
 *
 * @property id Unique identifier of the order this event relates to
 * @property timestamp Timestamp when the event occurred
 */
@Serializable
sealed class OrderEvent {
    abstract val id: String
    abstract val timestamp: String

    /**
     * Event representing the creation of a new order
     *
     * @property id Unique identifier of the created order
     * @property customerId Identifier of the customer who placed the order
     * @property orderItems List of items included in the order
     * @property totalAmount Total monetary value of the order
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Created(
        override val id: String,
        val customerId: String,
        val orderItems: List<LineItem>,
        val totalAmount: Double,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the confirmation of an order
     *
     * @property id Unique identifier of the confirmed order
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Confirmed(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the shipment of an order
     *
     * @property id Unique identifier of the shipped order
     * @property trackingNumber Tracking number for the shipment
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Shipped(
        override val id: String,
        val trackingNumber: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the cancellation of an order
     *
     * @property id Unique identifier of the cancelled order
     * @property reason Reason for cancelling the order
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Cancelled(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the delivery of an order
     *
     * @property id Unique identifier of the delivered order
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Delivered(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the rejection of a command (no state change)
     *
     * @property id Unique identifier of the order related to the rejected command
     * @property reason Reason why the command was rejected
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Rejected(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()
}
