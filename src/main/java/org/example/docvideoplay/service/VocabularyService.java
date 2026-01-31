package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.StudyMaterial;

import java.util.List;

/**
 * Service for managing vocabulary highlights and comments.
 * Handles highlight creation, retrieval, and comment management for study materials.
 */
public interface VocabularyService {
    
    /**
     * Create a new highlight for a study material.
     * Automatically schedules the initial 5-day reminder using spaced repetition.
     * 
     * @param materialId The ID of the study material
     * @param text The highlighted text
     * @param context The surrounding context of the highlight
     * @param startPosition The start position of the highlight in the text
     * @param endPosition The end position of the highlight in the text
     * @return The created Highlight entity
     * @throws IllegalArgumentException if material not found or invalid parameters
     */
    Highlight createHighlight(Long materialId, String text, String context, Integer startPosition, Integer endPosition);
    
    /**
     * Create a new highlight with an initial comment.
     * 
     * @param materialId The ID of the study material
     * @param text The highlighted text
     * @param context The surrounding context of the highlight
     * @param startPosition The start position of the highlight in the text
     * @param endPosition The end position of the highlight in the text
     * @param userComment The initial comment for the highlight
     * @return The created Highlight entity
     * @throws IllegalArgumentException if material not found or invalid parameters
     */
    Highlight createHighlightWithComment(Long materialId, String text, String context, 
                                       Integer startPosition, Integer endPosition, String userComment);
    
    /**
     * Retrieve all highlights for a specific study material ordered by position.
     * 
     * @param materialId The ID of the study material
     * @return List of highlights ordered by start position
     * @throws IllegalArgumentException if material not found
     */
    List<Highlight> getHighlightsByMaterial(Long materialId);
    
    /**
     * Retrieve all highlights for a specific study material with review history loaded.
     * 
     * @param materialId The ID of the study material
     * @return List of highlights with review history eagerly loaded
     * @throws IllegalArgumentException if material not found
     */
    List<Highlight> getHighlightsByMaterialWithHistory(Long materialId);
    
    /**
     * Retrieve a specific highlight by ID.
     * 
     * @param highlightId The ID of the highlight
     * @return The highlight entity
     * @throws IllegalArgumentException if highlight not found
     */
    Highlight getHighlightById(Long highlightId);
    
    /**
     * Update the comment for an existing highlight.
     * 
     * @param highlightId The ID of the highlight
     * @param comment The new comment text
     * @return The updated Highlight entity
     * @throws IllegalArgumentException if highlight not found
     */
    Highlight updateHighlightComment(Long highlightId, String comment);
    
    /**
     * Update the text and context of an existing highlight.
     * 
     * @param highlightId The ID of the highlight
     * @param text The new highlighted text
     * @param context The new context
     * @return The updated Highlight entity
     * @throws IllegalArgumentException if highlight not found
     */
    Highlight updateHighlightText(Long highlightId, String text, String context);
    
    /**
     * Update the position of an existing highlight.
     * 
     * @param highlightId The ID of the highlight
     * @param startPosition The new start position
     * @param endPosition The new end position
     * @return The updated Highlight entity
     * @throws IllegalArgumentException if highlight not found or invalid positions
     */
    Highlight updateHighlightPosition(Long highlightId, Integer startPosition, Integer endPosition);
    
    /**
     * Delete a highlight and all its associated review records.
     * 
     * @param highlightId The ID of the highlight to delete
     * @throws IllegalArgumentException if highlight not found
     */
    void deleteHighlight(Long highlightId);
    
    /**
     * Search highlights by text content (case insensitive).
     * 
     * @param searchText The text to search for
     * @return List of highlights containing the search text
     */
    List<Highlight> searchHighlightsByText(String searchText);
    
    /**
     * Get all highlights across all materials.
     * 
     * @return List of all highlights ordered by creation date
     */
    List<Highlight> getAllHighlights();
    
    /**
     * Get all highlights that have user comments.
     * 
     * @return List of highlights with comments ordered by creation date
     */
    List<Highlight> getHighlightsWithComments();
    
    /**
     * Get highlights that have never been reviewed.
     * 
     * @return List of highlights with no review history
     */
    List<Highlight> getNeverReviewedHighlights();
    
    /**
     * Get the count of highlights for a specific material.
     * 
     * @param materialId The ID of the study material
     * @return Count of highlights for the material
     */
    long getHighlightCountByMaterial(Long materialId);
    
    /**
     * Get highlights due for review today.
     * 
     * @return List of highlights due for review today
     */
    List<Highlight> getHighlightsDueToday();
    
    /**
     * Get overdue highlights (past due date).
     * 
     * @return List of overdue highlights ordered by due date
     */
    List<Highlight> getOverdueHighlights();
    
    /**
     * Validate highlight position parameters.
     * 
     * @param startPosition The start position
     * @param endPosition The end position
     * @return true if positions are valid, false otherwise
     */
    boolean isValidPosition(Integer startPosition, Integer endPosition);
}