package com.learning.restate.with.boot.gateway.config

import dev.restate.client.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up the Restate client.
 *
 * This class configures and provides a Restate client bean
 * for communicating with the Restate runtime.
 */
@Configuration
class RestateClientConfig {
    @Value("\${restate.client.base-uri:http://localhost:8080}")
    private lateinit var restateBaseUri: String

    /**
     * Create and configure a Restate client bean.
     *
     * @return Configured Restate client instance
     */
    @Bean
    fun restateClient(): Client {
        return Client.connect(restateBaseUri)
    }
}
