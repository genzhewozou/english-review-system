package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Deck;
import org.example.docvideoplay.entity.Card;

import java.util.List;

/**
 * Service for managing decks and their associated cards.
 * Handles deck creation, retrieval, and card management.
 */
public interface DeckService {
    
    /**
     * Create a new deck for a user.
     * 
     * @param userId The ID of the user creating the deck
     * @param name The name of the deck
     * @param description The description of the deck
     * @param isPublic Whether the deck is public
     * @return The created Deck entity
     */
    Deck createDeck(Long userId, String name, String description, Boolean isPublic);
    
    /**
     * Create a new deck with custom review options.
     * 
     * @param userId The ID of the user creating the deck
     * @param name The name of the deck
     * @param description The description of the deck
     * @param isPublic Whether the deck is public
     * @param newCardsPerDay Number of new cards per day
     * @param maxReviewsPerDay Maximum reviews per day
     * @param easyInterval Interval for easy cards
     * @param easyBonus Bonus multiplier for easy answers
     * @param intervalModifier Modifier for all intervals
     * @param startingEase Starting ease factor
     * @param steps Number of learning steps
     * @return The created Deck entity
     */
    Deck createDeckWithOptions(Long userId, String name, String description, Boolean isPublic,
                              Integer newCardsPerDay, Integer maxReviewsPerDay, Integer easyInterval,
                              Double easyBonus, Double intervalModifier, Double startingEase, Integer steps);
    
    /**
     * Retrieve a specific deck by ID.
     * 
     * @param deckId The ID of the deck
     * @return The deck entity
     * @throws IllegalArgumentException if deck not found
     */
    Deck getDeckById(Long deckId);
    
    /**
     * Retrieve a specific deck by ID for a specific user.
     * 
     * @param deckId The ID of the deck
     * @param userId The ID of the user who owns the deck
     * @return The deck entity
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    Deck getDeckById(Long deckId, Long userId);
    
    /**
     * Update an existing deck.
     * 
     * @param deckId The ID of the deck
     * @param name The new name
     * @param description The new description
     * @param isPublic The new public status
     * @param userId The ID of the user who owns the deck
     * @return The updated Deck entity
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    Deck updateDeck(Long deckId, String name, String description, Boolean isPublic, Long userId);
    
    /**
     * Update an existing deck with custom review options.
     * 
     * @param deckId The ID of the deck
     * @param name The new name
     * @param description The new description
     * @param isPublic The new public status
     * @param newCardsPerDay Number of new cards per day
     * @param maxReviewsPerDay Maximum reviews per day
     * @param easyInterval Interval for easy cards
     * @param easyBonus Bonus multiplier for easy answers
     * @param intervalModifier Modifier for all intervals
     * @param startingEase Starting ease factor
     * @param steps Number of learning steps
     * @param userId The ID of the user who owns the deck
     * @return The updated Deck entity
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    Deck updateDeckWithOptions(Long deckId, String name, String description, Boolean isPublic,
                              Integer newCardsPerDay, Integer maxReviewsPerDay, Integer easyInterval,
                              Double easyBonus, Double intervalModifier, Double startingEase, Integer steps,
                              Long userId);
    
    /**
     * Delete a deck and all its associated cards.
     * 
     * @param deckId The ID of the deck to delete
     * @param userId The ID of the user who owns the deck
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    void deleteDeck(Long deckId, Long userId);
    
    /**
     * Retrieve all decks for a specific user.
     * 
     * @param userId The ID of the user who created the decks
     * @return List of decks ordered by creation date
     */
    List<Deck> getDecksByUser(Long userId);
    
    /**
     * Retrieve all public decks.
     * 
     * @return List of public decks ordered by creation date
     */
    List<Deck> getPublicDecks();
    
    /**
     * Retrieve all public decks created by a specific user.
     * 
     * @param userId The ID of the user who created the decks
     * @return List of public decks ordered by creation date
     */
    List<Deck> getPublicDecksByUser(Long userId);
    
    /**
     * Add a card to a deck.
     * 
     * @param deckId The ID of the deck
     * @param card The card to add
     * @param userId The ID of the user who owns the deck
     * @return The updated Deck entity
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    Deck addCardToDeck(Long deckId, Card card, Long userId);
    
    /**
     * Remove a card from a deck.
     * 
     * @param deckId The ID of the deck
     * @param cardId The ID of the card to remove
     * @param userId The ID of the user who owns the deck
     * @return The updated Deck entity
     * @throws IllegalArgumentException if deck not found, card not found, or not owned by user
     */
    Deck removeCardFromDeck(Long deckId, Long cardId, Long userId);
    
    /**
     * Retrieve all cards in a deck.
     * 
     * @param deckId The ID of the deck
     * @param userId The ID of the user who owns the deck
     * @return List of cards in the deck
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    List<Card> getCardsInDeck(Long deckId, Long userId);
    
    /**
     * Retrieve all cards in a public deck.
     * 
     * @param deckId The ID of the public deck
     * @return List of cards in the deck
     * @throws IllegalArgumentException if deck not found or not public
     */
    List<Card> getCardsInPublicDeck(Long deckId);
    
    /**
     * Search decks by name or description.
     * 
     * @param searchText The text to search for
     * @param userId The ID of the user performing the search
     * @return List of decks matching the search criteria
     */
    List<Deck> searchDecks(String searchText, Long userId);
    
    /**
     * Get the count of cards in a deck.
     * 
     * @param deckId The ID of the deck
     * @param userId The ID of the user who owns the deck
     * @return The number of cards in the deck
     * @throws IllegalArgumentException if deck not found or not owned by user
     */
    long getCardCountInDeck(Long deckId, Long userId);
    
    /**
     * Duplicate a deck and its cards.
     * 
     * @param deckId The ID of the deck to duplicate
     * @param userId The ID of the user who will own the duplicate deck
     * @param newName The name of the duplicate deck
     * @return The duplicated Deck entity
     * @throws IllegalArgumentException if deck not found
     */
    Deck duplicateDeck(Long deckId, Long userId, String newName);
}