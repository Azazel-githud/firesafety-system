package com.example.firesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.firesystem.jwt.JwtAuthEntryPoint;
import com.example.firesystem.jwt.JwtAuthFilter;

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

    private static final String[] ALLOWED_URLS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui",
            "/index.html",
            "/",
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico",
            "/static/**",
            "/webjars/**"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> {
            // auth.requestMatchers(ALLOWED_URLS).permitAll();
            // auth.requestMatchers("/api/auth/login", "/api/auth/refresh",
            // "/api/auth/register").permitAll();
            // auth.requestMatchers("/").permitAll(); // для теста, потом уберешь
            // auth.anyRequest().authenticated();
            auth.anyRequest().permitAll(); // разрешает все, удали после тестов
        });
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // http.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint));
        // http.addFilterBefore(jwtAuthFilter,
        // UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}