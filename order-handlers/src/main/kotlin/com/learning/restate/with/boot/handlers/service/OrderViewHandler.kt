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
 * Restate service for managing order read models and views
 *
 * This service handles events from the write side and updates the read models
 * used for querying order information. It maintains order summaries and indexes
 * for efficient retrieval.
 */
@RestateService
class OrderViewHandler {
    /**
     * Handler that processes order events and updates read models
     *
     * This method receives events from the write side and:
     * 1. Creates or updates order summaries for querying
     * 2. Maintains indexes for efficient order listing
     *
     * @param ctx Context provided by Restate
     * @param request Contains the events to process
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
     * Handler that retrieves all order summaries
     *
     * @param ctx Context provided by Restate
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
