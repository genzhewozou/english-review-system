package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     * 
     * @param username The username to search for
     * @return The user with the given username wrapped in Optional
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if user exists by username
     * 
     * @param username The username to check
     * @return True if user exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if user exists by email
     * 
     * @param email The email to check
     * @return True if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user by email
     * 
     * @param email The email to search for
     * @return The user with the given email wrapped in Optional
     */
    Optional<User> findByEmail(String email);
}