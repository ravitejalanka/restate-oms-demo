package com.learning.restate.with.boot.gateway.controller

import com.learning.restate.with.boot.domain.OrderCommand
import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.gateway.dto.*
import com.learning.restate.with.boot.handlers.aggregate.OrderAggregateClient
import com.learning.restate.with.boot.handlers.service.OrderViewHandlerClient
import dev.restate.client.Client
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for managing orders.
 *
 * Provides RESTful endpoints for creating, confirming, shipping, cancelling,
 * and retrieving order information through the Restate orchestration system.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management API")
class OrderRestController(
    /**
     * The Restate client used to communicate with the backend services
     */
    private val client: Client
) {

    /**
     * Creates a new order.
     *
     * @param request The order creation request containing customer and item information
     * @return Response containing the created order details and success status
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a new order")
    suspend fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val orderId = request.id ?: UUID.randomUUID().toString()

        val command = OrderCommand.Create(
            id = orderId,
            customerId = request.customerId,
            lineItems = request.items
        )

        return executeCommand(orderId, command, "Order created successfully")
    }

    /**
     * Confirms an existing order.
     *
     * @param orderId The unique identifier of the order to confirm
     * @return Response containing the confirmed order details and success status
     */
    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm an order")
    suspend fun confirmOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<OrderResponse> {
        val command = OrderCommand.Confirm(id = orderId)
        return executeCommand(orderId, command, "Order confirmed successfully")
    }

    /**
     * Ships an existing order.
     *
     * @param orderId The unique identifier of the order to ship
     * @param request The shipping request containing tracking information
     * @return Response containing the shipped order details and success status
     */
    @PostMapping(
        path = ["/{orderId}/ship"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Ship an order")
    suspend fun shipOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String,
        @RequestBody request: ShipOrderRequest
    ): ResponseEntity<OrderResponse> {
        val command = OrderCommand.Ship(
            id = orderId,
            trackingNumber = request.trackingNumber
        )
        return executeCommand(orderId, command, "Order shipped successfully")
    }

    /**
     * Cancels an existing order.
     *
     * @param orderId The unique identifier of the order to cancel
     * @param request The cancellation request containing reason information
     * @return Response containing the cancelled order details and success status
     */
    @PostMapping(
        path = ["/{orderId}/cancel"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Cancel an order")
    suspend fun cancelOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String,
        @RequestBody request: CancelOrderRequest
    ): ResponseEntity<OrderResponse> {
        val command = OrderCommand.Cancel(
            id = orderId,
            reason = request.reason
        )
        return executeCommand(orderId, command, "Order cancelled successfully")
    }

    /**
     * Retrieves an existing order by ID.
     *
     * @param orderId The unique identifier of the order to retrieve
     * @return Response containing the order details
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    suspend fun getOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<OrderResponse> {
        try {
            val order = OrderAggregateClient.fromId(client, orderId).get()
            return ResponseEntity.ok(
                OrderResponse(
                    success = true,
                    orderId = orderId,
                    message = "Order retrieved successfully",
                    order = order
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(
                OrderResponse(
                    success = false,
                    orderId = orderId,
                    message = "Failed to retrieve order: ${e.message}"
                )
            )
        }
    }

    /**
     * Lists all orders.
     *
     * @return Response containing a list of all orders
     */
    @GetMapping
    @Operation(summary = "List all orders")
    suspend fun listOrders(): ResponseEntity<OrderListResponse> {
        try {
            val orders = OrderViewHandlerClient.listOrders(client)
            return ResponseEntity.ok(OrderListResponse(orders))
        } catch (e: Exception) {
            // Return empty list on error
            return ResponseEntity.ok(OrderListResponse(emptyList()))
        }
    }

    /**
     * Executes an order command through the Restate client.
     *
     * @param orderId The unique identifier of the order
     * @param command The order command to execute
     * @param successMessage The message to return on successful execution
     * @return Response containing the result of the command execution
     */
    private suspend fun executeCommand(
        orderId: String,
        command: OrderCommand,
        successMessage: String
    ): ResponseEntity<OrderResponse> {
        return try {
            val events = OrderAggregateClient.fromId(client, orderId).handle(command)
            ResponseEntity.ok(
                OrderResponse(
                    success = true,
                    orderId = orderId,
                    message = successMessage,
                    events = events
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                OrderResponse(
                    success = false,
                    orderId = orderId,
                    message = "Failed to execute command: ${e.message}"
                )
            )
        }
    }
}