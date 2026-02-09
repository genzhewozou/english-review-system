package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.StudyMaterial;

import java.util.List;

/**
 * Service for managing vocabulary cards and comments.
 * Handles card creation, retrieval, and comment management for study materials.
 */
public interface VocabularyService {
    
    /**
     * Create a new card from highlighted text in a study material.
     * Automatically schedules the initial 5-day reminder using spaced repetition.
     * 
     * @param userId The ID of the user creating the card
     * @param materialId The ID of the study material
     * @param text The card front text
     * @param context The surrounding context
     * @param startPosition The start position in the text
     * @param endPosition The end position in the text
     * @return The created Card entity
     * @throws IllegalArgumentException if material not found or invalid parameters
     */
    Card createCardFromHighlight(Long userId, Long materialId, String text, String context, Integer startPosition, Integer endPosition);
    
    /**
     * Create a new card from highlighted text with an initial comment.
     * 
     * @param userId The ID of the user creating the card
     * @param materialId The ID of the study material
     * @param text The card front text
     * @param context The surrounding context
     * @param startPosition The start position in the text
     * @param endPosition The end position in the text
     * @param userComment The initial comment for the card
     * @return The created Card entity
     * @throws IllegalArgumentException if material not found or invalid parameters
     */
    Card createCardFromHighlightWithComment(Long userId, Long materialId, String text, String context,
                                       Integer startPosition, Integer endPosition, String userComment);
    
    /**
     * Create a new card from highlighted text with an initial comment and tags.
     * 
     * @param userId The ID of the user creating the card
     * @param materialId The ID of the study material
     * @param text The card front text
     * @param context The surrounding context
     * @param startPosition The start position in the text
     * @param endPosition The end position in the text
     * @param userComment The initial comment for the card
     * @param tags The list of tag IDs to associate with the card
     * @return The created Card entity
     * @throws IllegalArgumentException if material not found or invalid parameters
     */
    Card createCardFromHighlightWithCommentAndTags(Long userId, Long materialId, String text, String context,
                                               Integer startPosition, Integer endPosition, String userComment, List<Long> tags);
    
    /**
     * Retrieve all cards for a specific study material ordered by position.
     * 
     * @param materialId The ID of the study material
     * @return List of cards ordered by start position
     * @throws IllegalArgumentException if material not found
     */
    List<Card> getCardsByMaterial(Long materialId);
    
    /**
     * Retrieve all cards for a specific study material ordered by position for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @param materialId The ID of the study material
     * @return List of cards ordered by start position
     * @throws IllegalArgumentException if material not found
     */
    List<Card> getCardsByMaterial(Long userId, Long materialId);
    
    /**
     * Retrieve all cards for a specific study material with review history loaded.
     * 
     * @param materialId The ID of the study material
     * @return List of cards with review history eagerly loaded
     * @throws IllegalArgumentException if material not found
     */
    List<Card> getCardsByMaterialWithHistory(Long materialId);
    
    /**
     * Retrieve all cards for a specific study material with review history loaded for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @param materialId The ID of the study material
     * @return List of cards with review history eagerly loaded
     * @throws IllegalArgumentException if material not found
     */
    List<Card> getCardsByMaterialWithHistory(Long userId, Long materialId);
    
    /**
     * Retrieve a specific card by ID.
     * 
     * @param cardId The ID of the card
     * @return The card entity
     * @throws IllegalArgumentException if card not found
     */
    Card getCardById(Long cardId);
    
    /**
     * Retrieve a specific card by ID for a specific user.
     * 
     * @param cardId The ID of the card
     * @param userId The ID of the user who owns the card
     * @return The card entity
     * @throws IllegalArgumentException if card not found or not owned by user
     */
    Card getCardById(Long cardId, Long userId);
    
    /**
     * Update the comment for an existing card.
     * 
     * @param cardId The ID of the card
     * @param comment The new comment text
     * @param userId The ID of the user who owns the card
     * @return The updated Card entity
     * @throws IllegalArgumentException if card not found or not owned by user
     */
    Card updateCardComment(Long cardId, String comment, Long userId);
    
    /**
     * Update the text and context of an existing card.
     * 
     * @param cardId The ID of the card
     * @param text The new front text
     * @param context The new context
     * @param userId The ID of the user who owns the card
     * @return The updated Card entity
     * @throws IllegalArgumentException if card not found or not owned by user
     */
    Card updateCardText(Long cardId, String text, String context, Long userId);
    
    /**
     * Update the position of an existing card.
     * 
     * @param cardId The ID of the card
     * @param startPosition The new start position
     * @param endPosition The new end position
     * @param userId The ID of the user who owns the card
     * @return The updated Card entity
     * @throws IllegalArgumentException if card not found, not owned by user, or invalid positions
     */
    Card updateCardPosition(Long cardId, Integer startPosition, Integer endPosition, Long userId);
    
    /**
     * Delete a card and all its associated review records.
     * 
     * @param cardId The ID of the card to delete
     * @param userId The ID of the user who owns the card
     * @throws IllegalArgumentException if card not found or not owned by user
     */
    void deleteCard(Long cardId, Long userId);
    
    /**
     * Search cards by text content (case insensitive).
     * 
     * @param searchText The text to search for
     * @return List of cards containing the search text
     */
    List<Card> searchCardsByText(String searchText);
    
    /**
     * Get all cards across all materials.
     * 
     * @return List of all cards ordered by creation date
     */
    List<Card> getAllCards();
    
    /**
     * Get all cards for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @return List of cards ordered by creation date
     */
    List<Card> getAllCards(Long userId);
    
    /**
     * Get all cards that have user comments.
     * 
     * @return List of cards with comments ordered by creation date
     */
    List<Card> getCardsWithComments();
    
    /**
     * Get all cards with comments for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @return List of cards with comments ordered by creation date
     */
    List<Card> getCardsWithComments(Long userId);
    
    /**
     * Get cards that have never been reviewed.
     * 
     * @return List of cards with no review history
     */
    List<Card> getNeverReviewedCards();
    
    /**
     * Get cards that have never been reviewed for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @return List of cards with no review history
     */
    List<Card> getNeverReviewedCards(Long userId);
    
    /**
     * Get the count of cards for a specific material.
     * 
     * @param materialId The ID of the study material
     * @return Count of cards for the material
     */
    long getCardCountByMaterial(Long materialId);
    
    /**
     * Get the count of cards for a specific material and user.
     * 
     * @param userId The ID of the user who created the cards
     * @param materialId The ID of the study material
     * @return Count of cards for the material and user
     */
    long getCardCountByMaterial(Long userId, Long materialId);
    
    /**
     * Get cards due for review today.
     * 
     * @return List of cards due for review today
     */
    List<Card> getCardsDueToday();
    
    /**
     * Get cards due for review today for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @return List of cards due for review today
     */
    List<Card> getCardsDueToday(Long userId);
    
    /**
     * Get overdue cards (past due date).
     * 
     * @return List of overdue cards ordered by due date
     */
    List<Card> getOverdueCards();
    
    /**
     * Get overdue cards (past due date) for a specific user.
     * 
     * @param userId The ID of the user who created the cards
     * @return List of overdue cards ordered by due date
     */
    List<Card> getOverdueCards(Long userId);
    
    /**
     * Validate card position parameters.
     * 
     * @param startPosition The start position
     * @param endPosition The end position
     * @return true if positions are valid, false otherwise
     */
    boolean isValidPosition(Integer startPosition, Integer endPosition);
}