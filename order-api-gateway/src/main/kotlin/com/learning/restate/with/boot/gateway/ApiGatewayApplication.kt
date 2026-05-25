package com.learning.restate.with.boot.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application class for the API gateway service.
 *
 * This application provides REST endpoints for interacting with the
 * order management system, serving as the entry point for external
 * clients to create, manage, and query orders.
 */
@SpringBootApplication
class ApiGatewayApplication

/**
 * Main entry point for the API gateway application.
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
