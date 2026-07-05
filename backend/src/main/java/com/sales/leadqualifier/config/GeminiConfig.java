package com.sales.leadqualifier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to define Spring RestTemplate configuration.
 * Configures specific connection and read timeouts to prevent blocking.
 */
@Configuration
public class GeminiConfig {

    /**
     * Configures and registers a RestTemplate bean.
     * Enforces connection timeout (10 seconds) and read timeout (20 seconds).
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10,000 milliseconds = 10 seconds
        factory.setReadTimeout(20000);    // 20,000 milliseconds = 20 seconds
        return new RestTemplate(factory);
    }
}
