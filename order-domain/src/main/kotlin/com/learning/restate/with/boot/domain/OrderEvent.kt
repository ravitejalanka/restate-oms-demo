package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Sealed class representing all possible events that can occur to an order.
 *
 * Events represent facts that have happened to an order. They are immutable
 * and are used to build up the current state of an order through event sourcing.
 */
@Serializable
sealed class OrderEvent {
    /** Unique identifier of the order this event relates to */
    abstract val id: String
    
    /** Timestamp when the event occurred */
    abstract val timestamp: String

    /**
     * Event indicating an order has been created.
     *
     * This event is generated when a new order is successfully created.
     *
     * @property id Unique identifier of the order
     * @property customerId Identifier of the customer who placed the order
     * @property orderItems List of items included in the order
     * @property totalAmount Total monetary amount of the order
     * @property timestamp When the event occurred
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
     * Event indicating an order has been confirmed.
     *
     * This event is generated when an order transitions from PENDING to CONFIRMED status.
     *
     * @property id Unique identifier of the order
     * @property timestamp When the event occurred
     */
    @Serializable
    data class Confirmed(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event indicating an order has been shipped.
     *
     * This event is generated when an order transitions from CONFIRMED to SHIPPED status.
     *
     * @property id Unique identifier of the order
     * @property trackingNumber Tracking number for the shipment
     * @property timestamp When the event occurred
     */
    @Serializable
    data class Shipped(
        override val id: String,
        val trackingNumber: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event indicating an order has been cancelled.
     *
     * This event is generated when an order transitions to CANCELLED status.
     *
     * @property id Unique identifier of the order
     * @property reason Reason for cancelling the order
     * @property timestamp When the event occurred
     */
    @Serializable
    data class Cancelled(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event indicating an order has been delivered.
     *
     * This event is generated when an order transitions from SHIPPED to DELIVERED status.
     *
     * @property id Unique identifier of the order
     * @property timestamp When the event occurred
     */
    @Serializable
    data class Delivered(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    /**
     * Event indicating an order command has been rejected.
     *
     * This event is generated when a command cannot be processed due to business rule violations.
     *
     * @property id Unique identifier of the order
     * @property reason Reason for rejecting the command
     * @property timestamp When the event occurred
     */
    @Serializable
    data class Rejected(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()
}
