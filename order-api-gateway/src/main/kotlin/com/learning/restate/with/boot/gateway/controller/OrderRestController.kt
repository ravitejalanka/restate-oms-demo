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
 * REST controller providing the external API for order management operations.
 *
 * This controller exposes endpoints for creating, updating, and querying orders
 * through HTTP requests. It interacts with the underlying order aggregate system
 * to process commands and retrieve order information.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management API")
class OrderRestController(private val client: Client) {

    /**
     * Create a new order.
     *
     * This endpoint creates a new order with the specified details. If no order ID
     * is provided in the request, one will be automatically generated.
     *
     * @param request The create order request containing customer and item information
     * @return Response containing the result of the operation and the created order details
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
     * Confirm an existing order.
     *
     * This endpoint transitions an order from PENDING to CONFIRMED status.
     *
     * @param orderId The unique identifier of the order to confirm
     * @return Response containing the result of the operation
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
     * Ship an existing order.
     *
     * This endpoint transitions an order from CONFIRMED to SHIPPED status and
     * associates a tracking number with the shipment.
     *
     * @param orderId The unique identifier of the order to ship
     * @param request The ship order request containing the tracking number
     * @return Response containing the result of the operation
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
     * Cancel an existing order.
     *
     * This endpoint cancels an order and requires providing a reason for cancellation.
     *
     * @param orderId The unique identifier of the order to cancel
     * @param request The cancel order request containing the cancellation reason
     * @return Response containing the result of the operation
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
     * Mark an order as delivered.
     *
     * This endpoint transitions an order from SHIPPED to DELIVERED status.
     *
     * @param orderId The unique identifier of the order to mark as delivered
     * @return Response containing the result of the operation
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
     * Retrieve an order by its ID.
     *
     * This endpoint returns the current state of the specified order.
     *
     * @param orderId The unique identifier of the order to retrieve
     * @return Response containing the order details if found
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
     * Retrieve the event history for an order.
     *
     * This endpoint returns all events that have occurred for the specified order.
     *
     * @param orderId The unique identifier of the order whose events to retrieve
     * @return Response containing the list of events for the order
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
     * Retrieve all orders.
     *
     * This endpoint returns a list of all orders with their summary information.
     *
     * @return Response containing a list of all orders
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
     * Internal helper function to execute order commands and handle responses.
     *
     * This method sends a command to the order aggregate, retrieves the results,
     * and constructs an appropriate response.
     *
     * @param orderId The unique identifier of the order
     * @param command The command to execute
     * @param successMessage Message to include in successful responses
     * @return ResponseEntity containing the operation result
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
