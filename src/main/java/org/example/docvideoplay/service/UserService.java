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
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return userOptional;
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
        if (!userRepository.existsByUsername("leo")) {
            User user = new User();
            user.setId(1L);
            user.setUsername("leo");
            user.setEmail("leo@example.com");
            user.setPasswordHash(passwordEncoder.encode("password123"));
            userRepository.save(user);
        }
    }
}
