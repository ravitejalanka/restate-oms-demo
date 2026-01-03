package com.learning.restate.with.boot.handlers.dto

data class OrderPurgeResult(
    val purgedOrders: List<String>,
    val failedOrders: List<String>
)
