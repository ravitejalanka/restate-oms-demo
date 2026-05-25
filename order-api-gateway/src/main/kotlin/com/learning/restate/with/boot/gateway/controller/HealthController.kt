package com.learning.restate.with.boot.gateway.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}