package com.learning.restate.with.boot.gateway.controller

import com.learning.restate.with.boot.gateway.dto.HealthCheckResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Health check API")
class HealthCheckController {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Health check endpoint")
    fun healthCheck(): HealthCheckResponse {
        return HealthCheckResponse(status = "ok")
    }
}