package com.example.firesystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
        @Bean
        public OpenAPI fireSystemOpenAPI() {
                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.COOKIE)
                                                                                .name("access-token"))
                                                .addSecuritySchemes("refreshAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.COOKIE)
                                                                                .name("refresh-token")))
                                .info(new Info()
                                                .title("Fire System Management API")
                                                .description("REST API for managing fire system alerts, sensors, and users.")
                                                .version("1.0.0"))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}