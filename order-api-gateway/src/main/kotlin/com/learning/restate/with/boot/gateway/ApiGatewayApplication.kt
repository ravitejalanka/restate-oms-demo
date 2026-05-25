package com.learning.restate.with.boot.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application class for the API gateway service
 *
 * This application provides REST endpoints that interface with the Restate workflow engine
 * to manage order lifecycle operations.
 */
@SpringBootApplication
class ApiGatewayApplication

/**
 * Main entry point for the API gateway application
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
