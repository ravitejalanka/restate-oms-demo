package com.learning.restate.with.boot.handlers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application class for the order handlers service
 *
 * This application provides the Restate handlers for processing order commands
 * and maintaining order state using event sourcing.
 */
@SpringBootApplication
class HandlersApplication

/**
 * Main entry point for the order handlers application
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}
