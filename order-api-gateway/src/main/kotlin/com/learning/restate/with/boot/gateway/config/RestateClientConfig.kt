package com.learning.restate.with.boot.gateway.config

import dev.restate.client.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for Restate client setup.
 *
 * This configuration class creates and manages the Restate client bean
 * used for communicating with the Restate service cluster.
 */
@Configuration
class RestateClientConfig {
    @Value("\${restate.client.base-uri:http://localhost:8080}")
    private lateinit var restateBaseUri: String

    /**
     * Creates and configures the Restate client bean.
     *
     * @return A configured Restate client instance
     */
    @Bean
    fun restateClient(): Client {
        return Client.connect(restateBaseUri)
    }
}
