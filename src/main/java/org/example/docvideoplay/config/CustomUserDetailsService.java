package org.example.docvideoplay.config;

import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Provides user authentication and authorization capabilities
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for username: {}", username);
        
        // Find user by username
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        logger.debug("User found: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
        
        // Create UserDetails object with user information
        // For now, we're giving all users the same role (USER)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(() -> "ROLE_USER")) // Simple role for now
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
