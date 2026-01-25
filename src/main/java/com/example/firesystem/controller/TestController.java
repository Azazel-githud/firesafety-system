package com.example.firesystem.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        log.info("Public endpoint accessed");
        return ResponseEntity.ok("This is a public endpoint - no auth required");
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint(Authentication authentication) {
        log.info("Protected endpoint accessed by: {}",
                authentication != null ? authentication.getName() : "anonymous");

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("Protected endpoint - Authenticated as: " + authentication.getName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
    }

    @GetMapping("/cookies")
    public ResponseEntity<Map<String, String>> checkCookies(
            @CookieValue(name = "access-token", required = false) String accessToken,
            @CookieValue(name = "refresh-token", required = false) String refreshToken,
            HttpServletRequest request) {

        Map<String, String> response = new HashMap<>();

        // Проверяем куки
        response.put("hasAccessToken", accessToken != null ? "YES" : "NO");
        response.put("hasRefreshToken", refreshToken != null ? "YES" : "NO");
        response.put("accessTokenLength", accessToken != null ? String.valueOf(accessToken.length()) : "0");
        response.put("refreshTokenLength", refreshToken != null ? String.valueOf(refreshToken.length()) : "0");

        // Проверяем все куки
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            response.put("totalCookies", String.valueOf(cookies.length));
            for (int i = 0; i < cookies.length; i++) {
                response.put("cookie_" + i + "_name", cookies[i].getName());
                response.put("cookie_" + i + "_value_length", String.valueOf(cookies[i].getValue().length()));
            }
        } else {
            response.put("totalCookies", "0");
        }

        log.info("Cookie check: {}", response);
        return ResponseEntity.ok(response);
    }
}