package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable

/**
 * Sealed class representing all possible commands that can be issued against an order.
 *
 * Commands represent intent to change the state of an order. Each command is processed
 * by the order aggregate, which may result in events being generated based on business rules.
 */
@Serializable
sealed class OrderCommand {
    /**
     * Command to create a new order.
     *
     * This command is used to create a new order with the specified details.
     * It will fail if an order with the same ID already exists.
     *
     * @property id Unique identifier for the new order
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
     * Command to confirm an existing order.
     *
     * This command transitions an order from PENDING to CONFIRMED status.
     * It will fail if the order doesn't exist or is not in PENDING status.
     *
     * @property id Identifier of the order to confirm
     */
    @Serializable
    data class Confirm(
        val id: String
    ) : OrderCommand()

    /**
     * Command to ship a confirmed order.
     *
     * This command transitions an order from CONFIRMED to SHIPPED status
     * and associates a tracking number with the shipment.
     * It will fail if the order doesn't exist or is not in CONFIRMED status.
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
     * Command to cancel an order.
     *
     * This command transitions an order to CANCELLED status and provides
     * a reason for the cancellation. It will fail if the order doesn't exist
     * or is not in a cancellable status (PENDING or CONFIRMED).
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
     * Command to mark an order as delivered.
     *
     * This command transitions an order from SHIPPED to DELIVERED status.
     * It will fail if the order doesn't exist or is not in SHIPPED status.
     *
     * @property id Identifier of the order to mark as delivered
     */
    @Serializable
    data class Deliver(
        val id: String
    ) : OrderCommand()
}