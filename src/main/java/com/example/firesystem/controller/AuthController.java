package com.example.firesystem.controller;

import com.example.firesystem.dto.ChangePasswordRequestDto;
import com.example.firesystem.dto.LoginRequestDto;
import com.example.firesystem.dto.LoginResponseDto;
import com.example.firesystem.dto.RegisterRequest;
import com.example.firesystem.dto.UserLoggedDto;
import com.example.firesystem.service.AuthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto request,
            @CookieValue(name = "access-token", required = false) String accessToken,
            @CookieValue(name = "refresh-token", required = false) String refreshToken) {

        return authService.login(request, accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @CookieValue(name = "refresh-token") String refreshToken) {

        return authService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDto> logout(
            @CookieValue(name = "access-token", required = false) String accessToken) {

        return authService.logout(accessToken);
    }

    @GetMapping("/info")
    public ResponseEntity<UserLoggedDto> info() {
        return ResponseEntity.ok(authService.info());
    }

    @PatchMapping("/change-password")
    public ResponseEntity<LoginResponseDto> changePassword(
            @RequestBody ChangePasswordRequestDto request) {

        return authService.changePassword(request);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }
}
