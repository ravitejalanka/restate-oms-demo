package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
sealed class OrderEvent {
    abstract val id: String
    abstract val timestamp: String

    @Serializable
    data class Created(
        override val id: String,
        val customerId: String,
        val orderItems: List<LineItem>,
        val totalAmount: Double,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    @Serializable
    data class Confirmed(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    @Serializable
    data class Shipped(
        override val id: String,
        val trackingNumber: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    @Serializable
    data class Cancelled(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    @Serializable
    data class Delivered(
        override val id: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()

    @Serializable
    data class Rejected(
        override val id: String,
        val reason: String,
        override val timestamp: String = Instant.now().toString()
    ) : OrderEvent()
}
