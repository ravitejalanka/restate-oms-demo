package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class OrderCommand {
    @Serializable
    data class Create(
        val id: String,
        val customerId: String,
        val lineItems: List<LineItem>
    ) : OrderCommand()

    @Serializable
    data class Confirm(
        val id: String
    ) : OrderCommand()

    @Serializable
    data class Ship(
        val id: String,
        val trackingNumber: String
    ) : OrderCommand()

    @Serializable
    data class Cancel(
        val id: String,
        val reason: String
    ) : OrderCommand()

    @Serializable
    data class Deliver(
        val id: String
    ) : OrderCommand()
}
