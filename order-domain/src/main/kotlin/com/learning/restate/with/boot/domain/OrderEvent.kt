package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Base sealed class for all order events.
 * Events represent facts that have occurred in the system.
 */
@Serializable
sealed class OrderEvent {
    /** Unique identifier of the order associated with this event */
    abstract val id: String
    
    /** Timestamp when the event occurred */
    abstract val timestamp: String

    /**
     * Event representing the creation of an order.
     *
     * @property id Unique identifier of the order
     * @property customerId Identifier of the customer who placed the order
     * @property orderItems List of items in the order
     * @property totalAmount Total amount of the order
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
     * Event representing the confirmation of an order.
     *
     * @property id Unique identifier of the order
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Confirmed(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the shipment of an order.
     *
     * @property id Unique identifier of the order
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
     * Event representing the cancellation of an order.
     *
     * @property id Unique identifier of the order
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
     * Event representing the delivery of an order.
     *
     * @property id Unique identifier of the order
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Delivered(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event representing the rejection of a command.
     *
     * @property id Unique identifier of the order
     * @property reason Reason for rejecting the command
     * @property timestamp Timestamp when the event occurred
     */
    @Serializable
    data class Rejected(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()
}
