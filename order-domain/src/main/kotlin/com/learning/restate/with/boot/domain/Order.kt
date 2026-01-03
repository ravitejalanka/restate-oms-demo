package com.learning.restate.with.boot.domain

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val customerId: String,
    val lineItems: List<LineItem>,
    val status: Status,
    val version: Int = 0
)

@Serializable
data class LineItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)

@Serializable
enum class Status {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REJECTED
}
