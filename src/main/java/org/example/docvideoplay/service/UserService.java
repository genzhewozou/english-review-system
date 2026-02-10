package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.dao.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String username, String email, String password) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // First try BCrypt password matching
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return userOptional;
            }
            // If BCrypt fails, try direct password comparison (for old users with plain text passwords)
            else if (password.equals(user.getPasswordHash())) {
                // Update old user's password to BCrypt format
                user.setPasswordHash(passwordEncoder.encode(password));
                userRepository.save(user);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    // Initialize default user if not exists
    public void initializeDefaultUser() {
        Optional<User> userOptional = userRepository.findByUsername("leo");
        if (userOptional.isPresent()) {
            // Update existing user's password to BCrypt format
            User user = userOptional.get();
            user.setPasswordHash(passwordEncoder.encode("111111"));
            userRepository.save(user);
        } else {
            // Create new user if not exists
            User user = new User();
            user.setId(1L);
            user.setUsername("leo");
            user.setEmail("leo@example.com");
            user.setPasswordHash(passwordEncoder.encode("111111"));
            userRepository.save(user);
        }
    }
}
