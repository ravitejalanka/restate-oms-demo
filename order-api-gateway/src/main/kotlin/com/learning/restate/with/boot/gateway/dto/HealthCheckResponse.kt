package com.learning.restate.with.boot.gateway.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class HealthCheckResponse(
    @JsonProperty("status")
    val status: String
)