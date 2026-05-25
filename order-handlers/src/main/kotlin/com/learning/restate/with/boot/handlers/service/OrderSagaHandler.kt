package com.learning.restate.with.boot.handlers.service

import com.learning.restate.with.boot.domain.OrderEvent
import com.learning.restate.with.boot.handlers.dto.ChangeHandlerRequest
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.kotlin.Context
import dev.restate.sdk.springboot.RestateService
import org.slf4j.LoggerFactory

/**
 * Restate service that implements the order saga pattern
 *
 * This service listens to order events and logs important business process milestones.
 * In a more complex implementation, it could coordinate with external services like
 * payment providers, inventory systems, and shipping services.
 */
@RestateService
class OrderSagaHandler {
    private val log = LoggerFactory.getLogger(OrderSagaHandler::class.java)

    /**
     * Handler that processes order events for saga coordination
     *
     * This method logs key events in the order lifecycle that would typically
     * trigger actions in other services as part of a distributed business process.
     *
     * @param ctx Context provided by Restate
     * @param request Contains the events to process
     */
    @Handler
    suspend fun handle(ctx: Context, request: ChangeHandlerRequest) {
        val aggregateKey = request.aggregateKey

        request.events.forEach { event ->
            when (event) {
                is OrderEvent.Created -> {
                    log.info("Saga: Order ${event.id} created for customer ${event.customerId}")
                }

                is OrderEvent.Confirmed -> {
                    log.info("Saga: Order ${event.id} confirmed - preparing for shipment")
                }

                is OrderEvent.Shipped -> {
                    log.info("Saga: Order ${event.id} shipped with tracking ${event.trackingNumber}")
                }

                is OrderEvent.Delivered -> {
                    log.info("Saga: Order ${event.id} delivered - completing workflow")
                }

                is OrderEvent.Cancelled -> {
                    log.info("Saga: Order ${event.id} cancelled - reason: ${event.reason}")
                }

                is OrderEvent.Rejected -> {
                    log.info("Saga: Order ${event.id} rejected - reason: ${event.reason}")
                }
            }
        }
    }
}
