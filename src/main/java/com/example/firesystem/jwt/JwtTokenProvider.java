package com.example.firesystem.jwt;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.firesystem.enums.TokenType;
import com.example.firesystem.model.Token;
import com.example.firesystem.repository.TokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String key;

    private final TokenRepository tokenRepository;

    private boolean isDisabled(String value) {
        log.debug("Checking if token is disabled for value starting with: {}",
                value != null && value.length() > 10 ? value.substring(0, 10) + "..." : "null");

        Token token = tokenRepository.findByValue(value).orElse(null);

        if (token == null) {
            log.warn("Token not found in repository");
            return true;
        }

        boolean disabled = token.isDisabled();
        log.debug("Token disabled status: {}", disabled);
        return disabled;
    }

    private Date toDate(LocalDateTime time) {
        return Date.from(time.toInstant(ZoneOffset.UTC));
    }

    private LocalDateTime toLocalDateTime(Date time) {
        return time.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }

    private Claims extractAllClaims(String value) {
        log.debug("Extracting all claims from token");
        return Jwts.parserBuilder().setSigningKey(decodeSecretKey(key)).build().parseClaimsJws(value).getBody();
    }

    private Key decodeSecretKey(String key) {
        log.debug("Decoding secret key");
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(key));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim from token");
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String getUsername(String token) {
        log.debug("Getting username from token");
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.debug("Extracted username: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage(), e);
            return null;
        }
    }

    public LocalDateTime getExpiration(String token) {
        log.debug("Getting expiration from token");
        try {
            LocalDateTime expiration = toLocalDateTime(extractClaim(token, Claims::getExpiration));
            log.debug("Token expires at: {}", expiration);
            return expiration;
        } catch (Exception e) {
            log.error("Failed to extract expiration from token: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isValid(String token) {
        log.debug("Validating token: {}...",
                token != null && token.length() > 10 ? token.substring(0, 10) + "..." : "null");

        if (token == null) {
            log.warn("Token is null");
            return false;
        }

        try {
            Jwts.parserBuilder().setSigningKey(decodeSecretKey(key)).build().parseClaimsJws(token);
            boolean disabled = isDisabled(token);

            if (disabled) {
                log.warn("Token is disabled");
            } else {
                log.debug("Token is valid and not disabled");
            }

            return !disabled;
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage(), e);
            return false;
        }
    }

    public Token generatedAccessToken(Map<String, Object> extra, long duration, TemporalUnit durationType,
            UserDetails user) {
        String username = user.getUsername();
        log.info("Generating ACCESS token for user: {}, duration: {} {}",
                username, duration, durationType);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plus(duration, durationType);

        String value = Jwts.builder()
                .setClaims(extra)
                .setSubject(username)
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(expirationDate))
                .signWith(decodeSecretKey(key), SignatureAlgorithm.HS256)
                .compact();

        log.debug("Generated access token for {} expiring at {}", username, expirationDate);

        return new Token(TokenType.ACCESS, value, expirationDate, false, null);
    }

    public Token generatedRefreshToken(long duration, TemporalUnit durationType, UserDetails user) {
        String username = user.getUsername();
        log.info("Generating REFRESH token for user: {}, duration: {} {}",
                username, duration, durationType);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plus(duration, durationType);

        String value = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(expirationDate))
                .signWith(decodeSecretKey(key), SignatureAlgorithm.HS256)
                .compact();

        log.debug("Generated refresh token for {} expiring at {}", username, expirationDate);

        return new Token(TokenType.REFRESH, value, expirationDate, false, null);
    }
}