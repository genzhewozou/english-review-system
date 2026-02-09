package org.example.docvideoplay.controller;

import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");

            if (username == null || email == null || password == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            User user = userService.registerUser(username, email, password);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            Optional<User> userOptional = userService.authenticateUser(username, password);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Load user details and set authentication context
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // For now, return user info directly. In a real app, you would return a JWT token.
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
