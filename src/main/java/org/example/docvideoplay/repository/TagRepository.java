package org.example.docvideoplay.repository;

import org.example.docvideoplay.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // Find all active tags for a user
    List<Tag> findByUserIdAndIsActiveTrue(Long userId);
    
    // Find tag by name for a user
    Optional<Tag> findByNameAndUserId(String name, Long userId);
    
    // Find tags by name containing for a user
    List<Tag> findByNameContainingAndUserIdAndIsActiveTrue(String name, Long userId);
    
    // Count tags for a user
    long countByUserIdAndIsActiveTrue(Long userId);
    
    // Check if tag exists by name for a user
    boolean existsByNameAndUserId(String name, Long userId);
    
    // Check if tag exists by name for a user excluding a specific tag ID
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
}
