package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable

/**
 * Sealed class representing all possible commands that can be issued against an Order
 */
@Serializable
sealed class OrderCommand {
    /**
     * Command to create a new order
     *
     * @property id Unique identifier for the new order. If not provided, a UUID will be generated
     * @property customerId Identifier of the customer placing the order
     * @property lineItems List of items to include in the order
     */
    @Serializable
    data class Create(
        val id: String,
        val customerId: String,
        val lineItems: List<LineItem>
    ) : OrderCommand()

    /**
     * Command to confirm an existing order
     *
     * @property id Identifier of the order to confirm
     */
    @Serializable
    data class Confirm(
        val id: String
    ) : OrderCommand()

    /**
     * Command to ship an order
     *
     * @property id Identifier of the order to ship
     * @property trackingNumber Tracking number for the shipment
     */
    @Serializable
    data class Ship(
        val id: String,
        val trackingNumber: String
    ) : OrderCommand()

    /**
     * Command to cancel an order
     *
     * @property id Identifier of the order to cancel
     * @property reason Reason for cancelling the order
     */
    @Serializable
    data class Cancel(
        val id: String,
        val reason: String
    ) : OrderCommand()

    /**
     * Command to mark an order as delivered
     *
     * @property id Identifier of the order to mark as delivered
     */
    @Serializable
    data class Deliver(
        val id: String
    ) : OrderCommand()
}
