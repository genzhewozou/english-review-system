package org.example.docvideoplay.repository;

import org.example.docvideoplay.entity.CardTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for CardTemplate entity
 */
@Repository
public interface CardTemplateRepository extends JpaRepository<CardTemplate, Long> {
    
    /**
     * Find all templates by user ID
     * 
     * @param userId The user ID to search for
     * @return List of card templates for the user
     */
    List<CardTemplate> findByUserId(Long userId);
    
    /**
     * Find templates by user ID and template type
     * 
     * @param userId The user ID to search for
     * @param templateType The template type to filter by
     * @return List of card templates matching the criteria
     */
    List<CardTemplate> findByUserIdAndTemplateType(Long userId, String templateType);
    
    /**
     * Find templates by user ID or system templates
     * 
     * @param userId The user ID to search for
     * @return List of card templates for the user and system templates
     */
    List<CardTemplate> findByUserIdOrIsSystemTemplateTrue(Long userId);
    
    /**
     * Find all system templates
     * 
     * @return List of system card templates
     */
    List<CardTemplate> findByIsSystemTemplateTrue();
    
    /**
     * Search templates by name containing the search text
     * 
     * @param searchText The text to search for in template names
     * @return List of matching card templates
     */
    List<CardTemplate> findByNameContainingIgnoreCase(String searchText);
    
    /**
     * Check if a template with the given name exists for the user ID
     * 
     * @param name The template name to check
     * @param userId The user ID to search for
     * @return true if a template with the name exists, false otherwise
     */
    boolean existsByNameAndUserId(String name, Long userId);
}
