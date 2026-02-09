package org.example.docvideoplay.utils;

import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations
 */
@Component
public class SecurityUtils {

    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        SecurityUtils.userService = userService;
    }

    /**
     * Get the current authenticated user
     * 
     * @return The current authenticated user
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                // Assuming userService has a method to find user by username
                return userService.findByUsername(username).orElse(null);
            }
        }
        
        // Return null or handle anonymous user case as needed
        return null;
    }

    /**
     * Check if the current user is authenticated
     * 
     * @return true if the current user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Get the username of the current authenticated user
     * 
     * @return The username of the current authenticated user, or null if not authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        
        return null;
    }
}
