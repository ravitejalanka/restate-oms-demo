package com.learning.restate.with.boot.handlers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application class for the order handlers service.
 *
 * This application handles order processing, including command handling,
 * event sourcing, and various workflow components such as sagas and
 * read model projections.
 */
@SpringBootApplication
class HandlersApplication

/**
 * Main entry point for the order handlers application.
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}
