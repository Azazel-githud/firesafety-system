// package com.example.firesystem.service;

// import java.net.Authenticator;
// import java.net.http.HttpHeaders;
// import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;
// import java.util.Set;

// import org.springframework.beans.factory.annotation.Value;
// import
// org.springframework.boot.autoconfigure.security.SecurityProperties.User;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.stereotype.Service;
// import org.springframework.web.servlet.function.ServerRequest.Headers;

// import com.example.firesystem.jwt.JwtTokenProvider;
// import com.example.firesystem.model.Token;
// import com.example.firesystem.repository.TokenRepository;
// import com.example.firesystem.repository.UserRepository;
// import com.example.firesystem.util.CookieUtil;

// @Service
// public class AuthenticationService {
// public UserRepository userRepository;
// public TokenRepository tokenRepository;
// public JwtTokenProvider jwtTokenProvider;
// public CookieUtil cookieUtil;
// public AuthenticationManager authenticationManager;

// @Value("${jwl.access.duration.minutes}")
// private long accessDurationMinute;
// @Value("${jwl.access.duration.seconds}")
// private long accessDurationSecond;
// @Value("${jwl.refresh.duration.days}")
// private long refreshDurationDay;
// @Value("${jwl.refresh.duration.seconds}")
// private long refreshDurationSecond;

// private void addAccessTokenCookie(Headers headers, Token token) {
// headers.add(HttpHeaders.SET_COOKIE,
// cookieUtil.createAccessCookie(token.getValue(),
// accessDurationSecond).toString());
// }

// private void refreshAccessTokenCookie(Headers headers, Token token) {
// headers.add(HttpHeaders.SET_COOKIE,
// cookieUtil.createRefreshCookie(token.getValue(),
// refreshDurationSecond).toString());
// }

// private void revokeAllTokens(User user) {
// Set<Token> tokens = user.getTokens();
// tokens.forEach(t ->{if(tokens.getExpiryDate().isBefore(LocalDateTime.now())){
// rokenRepository.delete(token);
// }
// elseif(!token.isDisabled()){
// token.setDisabled(true);
// tokenRepository.save(token);
// }
// });
// }

// public ResponseEntity<LoginResponse> login(LoginRequest req, String access,
// String refresh) {
// Authentication authentication = authenticationManager
// .authenticate(new UserPasswordAuthenticationToken(req.username(),
// req.password()));
// User user = userService.getUser(req.username());
// boolean accessValid = jwtTokenProvider.isValid(access);
// boolean refreshValid = jwtTokenProvider.isValid(refresh);
// HttpHeaders headers = new HttpHeaders();
// revokeAllTokens(user);

// if (!accessValid) {
// Token newAccess = tokenRepository.genAccess(Map.of("role",
// user.getRole().getAuthhority()),
// accessDurationMinute, ChronoUnit.MINUTES, user);
// Token newAccess = tokenRepository.genAccess(Map.of("role",
// user.getRole().getAuthhority()),
// accessDurationMinute, ChronoUnit.DAYS, user);
// newAccess.setUser(user);
// addAccessTokenCookie(headers, newAccess);
// tokenRepository.save(newAccess);
// }

// }
// }
