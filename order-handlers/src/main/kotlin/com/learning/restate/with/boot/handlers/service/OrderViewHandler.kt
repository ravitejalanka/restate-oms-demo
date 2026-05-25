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
 * Handler responsible for maintaining read models (projections) of order data.
 *
 * This handler processes order events and updates denormalized read models
 * optimized for querying, including maintaining an index of all order IDs.
 */
@RestateService
class OrderViewHandler {
    /**
     * Handle incoming order events and update the corresponding read models.
     *
     * For each event, this method updates the appropriate projection data:
     * - On Created: Creates a new OrderSummary and adds it to the index
     * - On status changes: Updates the status field in the OrderSummary
     *
     * @param ctx Restate context for the current invocation
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
     * Retrieve all order summaries from the read model.
     *
     * This method queries the index of all order IDs and retrieves
     * the corresponding OrderSummary objects, returning them as a list.
     *
     * @param ctx Restate context for the current invocation
     * @return List of all order summaries
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
