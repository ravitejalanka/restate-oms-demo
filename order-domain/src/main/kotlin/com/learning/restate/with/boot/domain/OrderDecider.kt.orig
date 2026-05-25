package com.learning.restate.with.boot.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Pure function: Command + State → Events
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
                    reason = "Order cannot be confirmed from status ${state.status}"
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
                    reason = "Order cannot be shipped from status ${state.status}"
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
                    reason = "Order cannot be delivered from status ${state.status}"
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
                    reason = "Order cannot be cancelled from status ${state.status}"
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

// Pure function: State + Event → New State
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
