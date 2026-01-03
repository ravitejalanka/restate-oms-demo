package com.learning.restate.with.boot.handlers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HandlersApplication

fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}
