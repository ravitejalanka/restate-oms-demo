package com.learning.restate.with.boot.handlers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Handlers service.
 *
 * This is the entry point for the Spring Boot application that handles
 * order processing and event management in the Restate system.
 */
@SpringBootApplication
class HandlersApplication

/**
 * Main function to start the Handlers application.
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}
