package com.learning.restate.with.boot.gateway.controller

import com.learning.restate.with.boot.domain.OrderCommand
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
 * REST controller for managing orders
 *
 * Provides HTTP endpoints for creating, updating, and querying orders.
 * All operations are processed through the Restate workflow engine.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management API")
class OrderRestController(private val client: Client) {

    /**
     * Create a new order
     *
     * @param request The order creation request containing customer and item details
     * @return Response with the created order details and events
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
     * Confirm an existing order
     *
     * @param orderId The ID of the order to confirm
     * @return Response with the updated order details and events
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
     * Ship an order
     *
     * @param orderId The ID of the order to ship
     * @param request The shipment details including tracking number
     * @return Response with the updated order details and events
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
     * Cancel an order
     *
     * @param orderId The ID of the order to cancel
     * @param request The cancellation reason
     * @return Response with the updated order details and events
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
     * Mark an order as delivered
     *
     * @param orderId The ID of the order to mark as delivered
     * @return Response with the updated order details and events
     */
    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Mark order as delivered")
    suspend fun deliverOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<OrderResponse> {
        val command = OrderCommand.Deliver(id = orderId)
        return executeCommand(orderId, command, "Order delivered successfully")
    }

    /**
     * Get order by ID
     *
     * @param orderId The ID of the order to retrieve
     * @return Response with the order details or 404 if not found
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    suspend fun getOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<Any> {
        return runCatching {
            val aggregateClient = OrderAggregateClient.fromClient(client, orderId)
            val order = aggregateClient.get()

            order?.let {
                ResponseEntity.ok(it)
            } ?: ResponseEntity.notFound().build()
        }.fold(
            onFailure = { e ->
                ResponseEntity.status(500).body(
                    ErrorResponse(
                        error = "Internal Error",
                        message = "Failed to retrieve order",
                        details = e.message
                    )
                )
            },
            onSuccess = { it as ResponseEntity<Any> }
        )
    }

    /**
     * Get event history for an order
     *
     * @param orderId The ID of the order to retrieve events for
     * @return Response with the order event history
     */
    @GetMapping("/{orderId}/events")
    @Operation(summary = "Get event history for an order")
    suspend fun getOrderEvents(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<Any> {
        return runCatching {
            val aggregateClient = OrderAggregateClient.fromClient(client, orderId)
            val events = aggregateClient.getEvents()

            ResponseEntity.ok(events)
        }.fold(
            onFailure = { e ->
                ResponseEntity.status(500).body(
                    ErrorResponse(
                        error = "Internal Error",
                        message = "Failed to retrieve order events",
                        details = e.message
                    )
                )
            },
            onSuccess = { it as ResponseEntity<Any> }
        )
    }

    /**
     * Get all orders
     *
     * @return Response with all orders in the system
     */
    @GetMapping
    @Operation(summary = "Get all orders")
    suspend fun getAllOrders(): ResponseEntity<Any> {
        return runCatching {
            val viewHandlerClient = OrderViewHandlerClient.fromClient(client)
            val orders = viewHandlerClient.getAllOrders()

            ResponseEntity.ok(OrderListResponse(orders = orders))
        }.fold(
            onFailure = { e ->
                ResponseEntity.status(500).body(
                    ErrorResponse(
                        error = "Internal Error",
                        message = "Failed to retrieve orders",
                        details = e.message
                    )
                )
            },
            onSuccess = { it as ResponseEntity<Any> }
        )
    }

    /**
     * Helper method to execute a command and return structured response
     *
     * @param orderId The ID of the order to process
     * @param command The command to execute
     * @param successMessage Message to include in successful response
     * @return Response with order details and execution results
     */
    private suspend fun executeCommand(
        orderId: String,
        command: OrderCommand,
        successMessage: String
    ): ResponseEntity<OrderResponse> {
        return runCatching {
            val aggregateClient = OrderAggregateClient.fromClient(client, orderId)

            // Execute command
            aggregateClient.handle(command)

            // Get results
            val order = aggregateClient.get()
            val events = aggregateClient.getEvents()

            OrderResponse(
                success = true,
                orderId = orderId,
                message = successMessage,
                order = order,
                events = events
            )
        }.fold(
            onFailure = { e ->
                OrderResponse(
                    success = false,
                    orderId = orderId,
                    message = "Command failed: ${e.message}"
                )
            },
            onSuccess = { it }
        ).let { response ->
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.badRequest().body(response)
            }
        }
    }
}
