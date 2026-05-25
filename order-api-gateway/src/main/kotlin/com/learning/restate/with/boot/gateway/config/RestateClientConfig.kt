package com.learning.restate.with.boot.gateway.config

import dev.restate.client.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for Restate client setup
 *
 * Configures the Restate client bean that connects to the Restate service.
 */
@Configuration
class RestateClientConfig {
    @Value("\${restate.client.base-uri:http://localhost:8080}")
    private lateinit var restateBaseUri: String

    /**
     * Creates and configures the Restate client bean
     *
     * @return Configured Restate client instance
     */
    @Bean
    fun restateClient(): Client {
        return Client.connect(restateBaseUri)
    }
}
