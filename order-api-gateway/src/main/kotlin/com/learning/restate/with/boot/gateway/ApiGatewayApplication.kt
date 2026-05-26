package com.learning.restate.with.boot.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the API Gateway.
 *
 * This is the entry point for the Spring Boot application that serves
 * as the API gateway for the order management system.
 */
@SpringBootApplication
class ApiGatewayApplication

/**
 * Main function to start the API Gateway application.
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
