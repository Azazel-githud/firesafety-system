package com.example.firesystem.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.firesystem.bot.FireAlertBot;
import com.example.firesystem.dto.ChangePasswordRequestDto;
import com.example.firesystem.dto.LoginRequestDto;
import com.example.firesystem.dto.LoginResponseDto;
import com.example.firesystem.dto.UserLoggedDto;
import com.example.firesystem.jwt.JwtTokenProvider;
import com.example.firesystem.mapper.UserMapper;
import com.example.firesystem.model.Token;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.TokenRepository;
import com.example.firesystem.util.CookieUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FireAlertBot alertBot;

    @Value("${jwt.access.duration.minute}")
    private long accessDurationMin;
    @Value("${jwt.access.duration.second}")
    private long accessDurationSec;
    @Value("${jwt.refresh.duration.day}")
    private long refreshDurationDate;
    @Value("${jwt.refresh.duration.second}")
    private long refreshDurationSec;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) {
        log.debug("Отзыв всех токенов для пользователя: {}", user.getUsername());
        Set<Token> tokens = user.getTokens();

        tokens.forEach(token -> {
            if (token.getExpiringDate().isBefore(LocalDateTime.now())) {
                log.debug("Удаление просроченного токена: {}", token.getId());
                tokenRepository.delete(token);
            } else if (!token.isDisabled()) {
                log.debug("Отключение активного токена: {}", token.getId());
                token.setDisabled(true);
                tokenRepository.save(token);
            }
        });
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto request, String access, String refresh) {
        log.info("Попытка входа пользователя: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(), request.password()));
        log.debug("Аутентификация успешна для пользователя: {}", request.username());

        User user = userService.getUserByUsername(request.username());

        boolean accessValid = jwtTokenProvider.isValid(access);
        boolean refreshValid = jwtTokenProvider.isValid(refresh);
        log.debug("Проверка токенов: accessValid={}, refreshValid={}", accessValid, refreshValid);

        HttpHeaders headers = new HttpHeaders();

        // revokeAllTokens(user);

        if (!accessValid) {
            log.debug("Создание нового access токена для пользователя: {}", user.getUsername());
            Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                    accessDurationMin, ChronoUnit.MINUTES, user);
            newAccess.setUser(user);
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);
        }

        if (!refreshValid || accessValid) {
            log.debug("Создание нового refresh токена для пользователя: {}", user.getUsername());
            Token newRefresh = jwtTokenProvider.generatedRefreshToken(refreshDurationDate, ChronoUnit.MINUTES, user);
            newRefresh.setUser(user);
            addRefreshTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
        }

        try {
            alertBot.sendToAdmin("Пользователь " + user.getUsername() + " успешно вошел!");
        } catch (Exception e) {
            log.error("Не удалось отправить сообщение админу: " + e);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Пользователь {} успешно вошел в систему. Роль: {}", user.getUsername(), user.getRole().getName());

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDto> refresh(String refreshToken) {
        log.info("Обновление токена доступа");

        if (!jwtTokenProvider.isValid(refreshToken)) {
            log.error("Предоставлен невалидный refresh токен");
            throw new RuntimeException("Invalid token provided");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        log.debug("Получение пользователя по имени из токена: {}", username);

        User user = userService.getUserByUsername(username);
        Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                accessDurationMin, ChronoUnit.MINUTES, user);

        newAccess.setUser(user);
        HttpHeaders headers = new HttpHeaders();
        addAccessTokenCookie(headers, newAccess);
        tokenRepository.save(newAccess);

        log.info("Access токен успешно обновлен для пользователя: {}", username);

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDto> logout(String accessToken) {
        log.info("Запрос на выход из системы");

        SecurityContextHolder.clearContext();
        String username = jwtTokenProvider.getUsername(accessToken);
        log.debug("Получение пользователя для выхода: {}", username);

        User user = userService.getUserByUsername(username);
        revokeAllTokens(user);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());

        log.info("Пользователь {} успешно вышел из системы", username);

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }

    public UserLoggedDto info() {
        log.debug("Получение информации о текущем пользователе");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            log.error("Попытка получения информации без аутентификации");
            throw new RuntimeException("User is not authenticated");
        }

        User user = userService.getUserByUsername(authentication.getName());
        log.debug("Информация о пользователе получена: {}", user.getUsername());

        return UserMapper.userToUserLoggedDto(user);
    }

    public ResponseEntity<LoginResponseDto> changePassword(ChangePasswordRequestDto request) {
        log.info("Запрос на смену пароля");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            log.error("Попытка смены пароля без аутентификации");
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        log.debug("Проверка старого пароля для пользователя: {}", username);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.error("Неверный старый пароль для пользователя: {}", username);
            throw new BadCredentialsException("Old password is invalid");
        }

        log.debug("Проверка совпадения новых паролей для пользователя: {}", username);
        if (!request.newPassword().equals(request.newPasswordAgain())) {
            log.error("Новые пароли не совпадают для пользователя: {}", username);
            throw new BadCredentialsException("New passwords don't match each other");
        }

        log.info("Смена пароля для пользователя: {}", username);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.saveUser(user);
        revokeAllTokens(user);
        SecurityContextHolder.clearContext();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());

        log.info("Пароль успешно изменен для пользователя: {}", username);

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }
}