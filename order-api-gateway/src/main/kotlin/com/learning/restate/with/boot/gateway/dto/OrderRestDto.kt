package com.learning.restate.with.boot.gateway.dto

import com.learning.restate.with.boot.domain.LineItem
import com.learning.restate.with.boot.domain.Order
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.domain.readmodel.OrderSummary

/**
 * DTO for creating a new order request.
 *
 * @property id Optional identifier for the new order. If not provided, a new ID will be generated.
 * @property customerId Identifier of the customer placing the order
 * @property items List of line items to include in the order
 */
data class CreateOrderRequest(
    val id: String? = null,
    val customerId: String,
    val items: List<LineItem>
)

/**
 * DTO for shipping an order request.
 *
 * @property trackingNumber Tracking number for the shipment
 */
data class ShipOrderRequest(
    val trackingNumber: String
)

/**
 * DTO for cancelling an order request.
 *
 * @property reason Reason for cancelling the order
 */
data class CancelOrderRequest(
    val reason: String
)

/**
 * DTO for order response.
 *
 * @property success Indicates whether the operation was successful
 * @property orderId Identifier of the order
 * @property message Descriptive message about the operation result
 * @property order The order details, if available
 * @property events List of events related to the order
 */
data class OrderResponse(
    val success: Boolean,
    val orderId: String,
    val message: String,
    val order: Order? = null,
    val events: List<OrderEvent> = emptyList()
)

/**
 * DTO for listing orders response.
 *
 * @property orders List of order summaries
 */
data class OrderListResponse(
    val orders: List<OrderSummary>
)

/**
 * DTO for error responses.
 *
 * @property error Error code or type
 * @property message Human-readable error message
 * @property details Additional details about the error, if available
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null
)
