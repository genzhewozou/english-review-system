package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.CardTemplate;

import java.util.List;

/**
 * Service for managing card templates
 * Handles template creation, retrieval, and management
 */
public interface CardTemplateService {
    
    /**
     * Create a new card template
     * 
     * @param userId The user ID creating the template
     * @param name The name of the template
     * @param description The description of the template
     * @param templateType The type of template
     * @param frontTemplate The front template HTML/CSS
     * @param backTemplate The back template HTML/CSS
     * @return The created CardTemplate entity
     */
    CardTemplate createTemplate(Long userId, String name, String description, String templateType,
                              String frontTemplate, String backTemplate);
    
    /**
     * Get a template by ID
     * 
     * @param templateId The ID of the template
     * @return The template entity
     * @throws IllegalArgumentException if template not found
     */
    CardTemplate getTemplateById(Long templateId);
    
    /**
     * Get all templates for a user, including system templates
     * 
     * @param userId The user ID
     * @return List of templates
     */
    List<CardTemplate> getAllTemplatesForUser(Long userId);
    
    /**
     * Get system templates only
     * 
     * @return List of system templates
     */
    List<CardTemplate> getSystemTemplates();
    
    /**
     * Get user-specific templates
     * 
     * @param userId The user ID
     * @return List of user templates
     */
    List<CardTemplate> getUserTemplates(Long userId);
    
    /**
     * Update an existing template
     * 
     * @param templateId The ID of the template
     * @param name The new name
     * @param description The new description
     * @param frontTemplate The new front template
     * @param backTemplate The new back template
     * @param userId The user ID updating the template
     * @return The updated template
     * @throws IllegalArgumentException if template not found or not owned by user
     */
    CardTemplate updateTemplate(Long templateId, String name, String description,
                              String frontTemplate, String backTemplate, Long userId);
    
    /**
     * Delete a template
     * 
     * @param templateId The ID of the template
     * @param userId The user ID deleting the template
     * @throws IllegalArgumentException if template not found, not owned by user, or is a system template
     */
    void deleteTemplate(Long templateId, Long userId);
    
    /**
     * Get templates by type for a user
     * 
     * @param userId The user ID
     * @param templateType The template type
     * @return List of templates
     */
    List<CardTemplate> getTemplatesByType(Long userId, String templateType);
    
    /**
     * Search templates by name
     * 
     * @param searchText The search text
     * @return List of matching templates
     */
    List<CardTemplate> searchTemplates(String searchText);
}
