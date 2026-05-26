package com.learning.restate.with.boot.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Pure function that determines which events should occur based on a command and current state.
 *
 * This function implements the business logic for order processing. Given a command and the
 * current state of an order, it returns a flow of events that should be emitted.
 *
 * @param command The command to process
 * @param state The current state of the order, or null if the order doesn't exist
 * @return A flow of events that should be emitted as a result of processing the command
 */
fun decide(command: OrderCommand, state: Order?): Flow<OrderEvent> = when (command) {
    is OrderCommand.Create -> {
        if (state == null) {
            flowOf(
                OrderEvent.Created(
                    id = command.id,
                    customerId = command.customerId,
                    orderItems = command.lineItems,
                    totalAmount = command.lineItems.sumOf { it.price * it.quantity }
                )
            )
        } else {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order already exists"
                )
            )
        }
    }

    is OrderCommand.Confirm -> {
        if (state == null) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order not found"
                )
            )
        } else if (state.status != Status.PENDING) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order cannot be confirmed from status ${'$'}{state.status}"
                )
            )
        } else {
            flowOf(
                OrderEvent.Confirmed(id = command.id)
            )
        }
    }

    is OrderCommand.Ship -> {
        if (state == null) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order not found"
                )
            )
        } else if (state.status != Status.CONFIRMED) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order cannot be shipped from status ${'$'}{state.status}"
                )
            )
        } else {
            flowOf(
                OrderEvent.Shipped(
                    id = command.id,
                    trackingNumber = command.trackingNumber
                )
            )
        }
    }

    is OrderCommand.Deliver -> {
        if (state == null) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order not found"
                )
            )
        } else if (state.status != Status.SHIPPED) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order cannot be delivered from status ${'$'}{state.status}"
                )
            )
        } else {
            flowOf(
                OrderEvent.Delivered(id = command.id)
            )
        }
    }

    is OrderCommand.Cancel -> {
        if (state == null) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order not found"
                )
            )
        } else if (state.status !in listOf(Status.PENDING, Status.CONFIRMED)) {
            flowOf(
                OrderEvent.Rejected(
                    id = command.id,
                    reason = "Order cannot be cancelled from status ${'$'}{state.status}"
                )
            )
        } else {
            flowOf(
                OrderEvent.Cancelled(
                    id = command.id,
                    reason = command.reason
                )
            )
        }
    }
}

/**
 * Pure function that evolves the state based on an event.
 *
 * This function implements the state transition logic. Given the current state and an event,
 * it returns the new state after applying the event.
 *
 * @param state The current state of the order, or null if the order doesn't exist
 * @param event The event to apply to the state
 * @return The new state after applying the event, or null if the event doesn't change the existence of the order
 */
fun evolve(state: Order?, event: OrderEvent): Order? = when (event) {
    is OrderEvent.Created -> {
        Order(
            id = event.id,
            customerId = event.customerId,
            lineItems = event.orderItems,
            status = Status.PENDING,
            version = 1
        )
    }

    is OrderEvent.Confirmed -> {
        state?.copy(
            status = Status.CONFIRMED,
            version = state.version + 1
        )
    }

    is OrderEvent.Shipped -> {
        state?.copy(
            status = Status.SHIPPED,
            version = state.version + 1
        )
    }

    is OrderEvent.Delivered -> {
        state?.copy(
            status = Status.DELIVERED,
            version = state.version + 1
        )
    }

    is OrderEvent.Cancelled -> {
        state?.copy(
            status = Status.CANCELLED,
            version = state.version + 1
        )
    }

    is OrderEvent.Rejected -> {
        // Rejected events don't change state
        state
    }
}
