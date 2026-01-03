package com.learning.restate.with.boot.domain.readmodel

import kotlinx.serialization.Serializable

@Serializable
data class OrderSummary(
    val id: String,
    val customerId: String,
    val status: String,
    val totalAmount: Double,
    val itemCount: Int
)
