package com.example.firesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    @Bean
    static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Часто отключают при работе с API
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/**").permitAll() // Разрешить всем доступ к /api/**
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Разрешить доступ к Swagger
                        .anyRequest().permitAll() // Разрешить все остальные запросы (временно)
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // Отключить Basic Auth
                .formLogin(formLogin -> formLogin.disable()); // Отключить стандартную HTML-форму входа

        return http.build();
    }
}
