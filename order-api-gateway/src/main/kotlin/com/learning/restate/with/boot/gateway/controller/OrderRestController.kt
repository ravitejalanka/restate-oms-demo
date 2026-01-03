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

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management API")
class OrderRestController(private val client: Client) {

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

    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm an order")
    suspend fun confirmOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<OrderResponse> {
        val command = OrderCommand.Confirm(id = orderId)
        return executeCommand(orderId, command, "Order confirmed successfully")
    }

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

    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Mark order as delivered")
    suspend fun deliverOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: String
    ): ResponseEntity<OrderResponse> {
        val command = OrderCommand.Deliver(id = orderId)
        return executeCommand(orderId, command, "Order delivered successfully")
    }

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
