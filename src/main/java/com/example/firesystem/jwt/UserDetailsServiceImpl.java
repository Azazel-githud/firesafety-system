package com.example.firesystem.jwt;

import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username: {}", username);

        try {
            UserDetails userDetails = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("User not found in database: {}", username);
                        return new ResourceNotFoundException("User not found: " + username);
                    });

            log.debug("Successfully loaded user: {} with authorities: {}",
                    username, userDetails.getAuthorities());
            return userDetails;
        } catch (Exception e) {
            log.error("Error loading user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
}