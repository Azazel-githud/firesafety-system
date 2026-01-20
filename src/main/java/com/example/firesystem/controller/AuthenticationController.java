package com.example.firesystem.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firesystem.dto.ChangePasswordRequestDto;
import com.example.firesystem.dto.LoginRequestDto;
import com.example.firesystem.dto.LoginResponseDto;
import com.example.firesystem.dto.UserLoggedDto;
import com.example.firesystem.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Methods for user authentication and authorization")
public class AuthenticationController {
    private final AuthService authenticationService;

    @Operation(summary = "Login User", description = "Authenticates user credentials and returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @CookieValue(name = "access-token", required = false) String access,
            @CookieValue(name = "refresh-token", required = false) String refresh,
            @RequestBody LoginRequestDto request) {
        return authenticationService.login(request, access, refresh);
    }

    @Operation(summary = "Refresh User tokens", description = "Checks User tokens and refreshes the if needed")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @CookieValue(name = "refresh-token", required = false) String refresh) {
        return authenticationService.refresh(refresh);
    }

    @Operation(summary = "Logout User", description = "Revokes User tokens and logs him out")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDto> logout(
            @CookieValue(name = "access-token", required = false) String access) {
        return authenticationService.logout(access);
    }

    @Operation(summary = "Get User info", description = "Shows info about authorized User")
    @GetMapping("/info")
    public ResponseEntity<UserLoggedDto> info() {
        return ResponseEntity.ok(authenticationService.info());
    }

    @Operation(summary = "Change User password", description = "Changes User password to new one and logs him out after it")
    @PatchMapping("/changePassword")
    public ResponseEntity<LoginResponseDto> changePassword(ChangePasswordRequestDto request) {
        return authenticationService.changePassword(request);
    }
}