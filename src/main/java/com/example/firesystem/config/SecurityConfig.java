package com.example.firesystem.config;

import com.example.firesystem.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем сессии (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Отключаем CSRF, т.к. используем API
                .csrf(AbstractHttpConfigurer::disable)
                // Отключаем стандартную форму входа
                .formLogin(AbstractHttpConfigurer::disable)
                // Отключаем HTTP Basic Auth
                .httpBasic(AbstractHttpConfigurer::disable)
                // Отключаем logout (по умолчанию)
                .logout(AbstractHttpConfigurer::disable)
                // Управляем доступом к URL
                .authorizeHttpRequests(authz -> authz
                        // Позволяем всем доступ к главной странице (index.html) и к эндпоинтам
                        // аутентификации
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/**").permitAll() // Пример: login, register
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated())
                // Добавляем наш фильтр ДО UsernamePasswordAuthenticationFilter
                // Это важно, чтобы наш фильтр работал до стандартной обработки Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}