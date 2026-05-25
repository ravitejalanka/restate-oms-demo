package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.slf4j.LoggerFactory

/**
 * Handler that logs order events to the console for monitoring and debugging purposes.
 *
 * This handler receives events from the order aggregate and provides real-time
 * visibility into order processing by logging key information about each event.
 */
@RestateService
class ConsoleChangeHandler {
    private val log = LoggerFactory.getLogger(ConsoleChangeHandler::class.java)

    /**
     * Handle incoming change notifications and log them to the console.
     *
     * This method processes the change request, logs key information about
     * the order events, and provides visibility into system behavior.
     *
     * @param ctx Restate context for the current invocation
     * @param request The change handler request containing events to process
     */
    @Handler
    suspend fun handle(ctx: Context, request: ChangeHandlerRequest) {
        val aggregateKey = request.aggregateKey
        val currentState = request.currentState

        log.info("=== Event Notification ===")
        log.info("Order ID: $aggregateKey")
        log.info("Current Status: ${currentState?.status}")
        log.info("Events: ${request.events.size}")

        request.events.forEach { event ->
            when (event) {
                is OrderEvent.Created -> {
                    log.info("  [CREATED] Order ${event.id} for customer ${event.customerId}")
                }

                is OrderEvent.Confirmed -> {
                    log.info("  [CONFIRMED] Order ${event.id}")
                }

                is OrderEvent.Shipped -> {
                    log.info("  [SHIPPED] Order ${event.id} - Tracking: ${event.trackingNumber}")
                }

                is OrderEvent.Delivered -> {
                    log.info("  [DELIVERED] Order ${event.id}")
                }

                is OrderEvent.Cancelled -> {
                    log.info("  [CANCELLED] Order ${event.id} - Reason: ${event.reason}")
                }

                is OrderEvent.Rejected -> {
                    log.info("  [REJECTED] Order ${event.id} - Reason: ${event.reason}")
                }
            }
        }
        log.info("========================")
    }
}
