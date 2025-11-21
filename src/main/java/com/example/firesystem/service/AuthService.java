package com.example.firesystem.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.firesystem.dto.LoginRequestDto;
import com.example.firesystem.dto.LoginResponseDto;
import com.example.firesystem.jwt.JwtTokenProvider;
import com.example.firesystem.model.Token;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.TokenRepository;
import com.example.firesystem.repository.UserRepository;
import com.example.firesystem.util.CookieUtil;

@Service
public class AuthService {
    public UserService userService;
    public UserRepository userRepository;
    public TokenRepository tokenRepository;
    public JwtTokenProvider jwtTokenProvider;
    public CookieUtil cookieUtil;
    public AuthenticationManager authenticationManager;

    @Value("${jwt.access.duration.minutes}")
    private long accessDurationMin;
    @Value("${jwt.access.duration.seconds}")
    private long accessDurationSec;
    @Value("${jwt.refresh.duration.days}")
    private long refreshDurationDay;
    @Value("${jwt.refresh.duration.seconds}")
    private long refreshDurationSec;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString());
    }

    private void refreshAccessTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) {
        Set<Token> tokens = user.getTokens();
        tokens.forEach(token -> {
            if (token.getExpiringDate().isBefore(LocalDateTime.now())) {
                tokenRepository.delete(token);
            } else if (!token.isDisabled()) {
                token.setDisabled(true);
                tokenRepository.save(token);
            }
        });
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto req, String access, String refresh) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        User user = userService.getUser(req.username());
        boolean acccessValid = jwtTokenProvider.isValid(access);
        boolean refreshValid = jwtTokenProvider.isValid(refresh);
        HttpHeaders headers = new HttpHeaders();
        revokeAllTokens(user);

        if (!acccessValid) {
            Token newAccess = jwtTokenProvider.generateAccessToken(Map.of("role", user.getRole().getAuthority()),
                    accessDurationMin, ChronoUnit.MINUTES, user);
            newAccess.setUser(user);
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);
        }
        if (!refreshValid) {
            Token newRefresh = jwtTokenProvider.generateRefreshToken(refreshDurationDay, ChronoUnit.DAYS, user);
            newRefresh.setUser(user);
            addAccessTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginResponseDto loginResponse = new LoginResponseDto(true, user.getRole().getName());
        return ResponseEntity.ok().headers(headers).body(loginResponse);
    }
}