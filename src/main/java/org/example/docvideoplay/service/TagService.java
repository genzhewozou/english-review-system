package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagService {
    
    /**
     * Create a new tag
     * @param name Tag name
     * @param description Tag description
     * @param userId Current user ID
     * @return Created tag
     */
    Tag createTag(String name, String description, Long userId);
    
    /**
     * Get all active tags for a user
     * @param userId Current user ID
     * @return List of tags
     */
    List<Tag> getAllTags(Long userId);
    
    /**
     * Get a tag by ID
     * @param id Tag ID
     * @param userId Current user ID
     * @return Optional tag
     */
    Optional<Tag> getTagById(Long id, Long userId);
    
    /**
     * Update a tag
     * @param id Tag ID
     * @param name New name
     * @param description New description
     * @param userId Current user ID
     * @return Updated tag
     */
    Tag updateTag(Long id, String name, String description, Long userId);
    
    /**
     * Delete a tag (soft delete)
     * @param id Tag ID
     * @param userId Current user ID
     */
    void deleteTag(Long id, Long userId);
    
    /**
     * Search tags by name
     * @param query Search query
     * @param userId Current user ID
     * @return List of matching tags
     */
    List<Tag> searchTags(String query, Long userId);
    
    /**
     * Check if a tag name already exists
     * @param name Tag name
     * @param userId Current user ID
     * @param excludeId Tag ID to exclude (for updates)
     * @return boolean
     */
    boolean tagNameExists(String name, Long userId, Long excludeId);
    
    /**
     * Count tags for a user
     * @param userId Current user ID
     * @return Tag count
     */
    long countTags(Long userId);
}
