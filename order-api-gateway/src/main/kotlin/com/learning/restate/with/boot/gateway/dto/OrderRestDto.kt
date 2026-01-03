package com.learning.restate.with.boot.gateway.dto

import com.learning.restate.with.boot.domain.LineItem
import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.domain.readmodel.OrderSummary

// Request DTOs
data class CreateOrderRequest(
    val id: String? = null,
    val customerId: String,
    val items: List<LineItem>
)

data class ShipOrderRequest(
    val trackingNumber: String
)

data class CancelOrderRequest(
    val reason: String
)

// Response DTOs
data class OrderResponse(
    val success: Boolean,
    val orderId: String,
    val message: String,
    val order: Order? = null,
    val events: List<OrderEvent> = emptyList()
)

data class OrderListResponse(
    val orders: List<OrderSummary>
)

data class ErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null
)
