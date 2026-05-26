package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.domain.readmodel.OrderSummary
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import com.learning.restate.with.boot.handlers.storage.OrderSummaryIndexClient
import com.learning.restate.with.boot.handlers.storage.OrderSummaryStorageClient
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService

/**
 * Service for handling order view updates.
 *
 * This service manages the read model for orders, updating denormalized
 * order summaries based on domain events and providing query capabilities.
 */
@RestateService
class OrderViewHandler {
    /**
     * Handles change events for an order by updating the read model.
     *
     * @param ctx The Restate context
     * @param request The change handler request containing events to process
     */
    @Handler
    suspend fun handle(ctx: Context, request: ChangeHandlerRequest) {
        request.events.forEach { event ->
            when (event) {
                is OrderEvent.Created -> {
                    // Create denormalized read model
                    val summary = OrderSummary(
                        id = event.id,
                        customerId = event.customerId,
                        status = "PENDING",
                        totalAmount = event.totalAmount,
                        itemCount = event.orderItems.size
                    )

                    // Store in Virtual Object
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .setSummary(summary)

                    // Add to index for listings
                    OrderSummaryIndexClient.fromContext(ctx, "index")
                        .addOrderId(event.id)
                }

                is OrderEvent.Confirmed -> {
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .updateStatus("CONFIRMED")
                }

                is OrderEvent.Shipped -> {
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .updateStatus("SHIPPED")
                }

                is OrderEvent.Delivered -> {
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .updateStatus("DELIVERED")
                }

                is OrderEvent.Cancelled -> {
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .updateStatus("CANCELLED")
                }

                is OrderEvent.Rejected -> {
                    OrderSummaryStorageClient.fromContext(ctx, event.id)
                        .updateStatus("REJECTED")
                }
            }
        }
    }

    /**
     * Retrieves all order summaries.
     *
     * @param ctx The Restate context
     * @return A list of all order summaries
     */
    @Handler
    suspend fun getAllOrders(ctx: Context): List<OrderSummary> {
        val orderIds = OrderSummaryIndexClient.fromContext(ctx, "index")
            .getAllOrderIds()
            .await()

        return orderIds.mapNotNull { orderId ->
            OrderSummaryStorageClient.fromContext(ctx, orderId)
                .getSummary()
                .await()
        }
    }
}
