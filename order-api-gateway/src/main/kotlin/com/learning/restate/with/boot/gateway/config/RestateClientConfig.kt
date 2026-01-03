package com.learning.restate.with.boot.gateway.config

import dev.restate.client.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestateClientConfig {
    @Value("\${restate.client.base-uri:http://localhost:8080}")
    private lateinit var restateBaseUri: String

    @Bean
    fun restateClient(): Client {
        return Client.connect(restateBaseUri)
    }
}
