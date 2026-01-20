package com.example.firesystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fireSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fire System Management API")
                        .description("REST API for managing fire system alerts, sensors, and users. " +
                                "This API provides comprehensive control over fire safety monitoring system.")
                        .version("1.0.0"));
    }
}