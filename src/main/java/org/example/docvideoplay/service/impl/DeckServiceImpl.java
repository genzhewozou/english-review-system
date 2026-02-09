package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.entity.Deck;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.repository.DeckRepository;
import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.service.DeckService;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DeckService for managing decks and their associated cards.
 * Provides functionality for deck creation, management, and card organization.
 */
@Service
@Transactional
public class DeckServiceImpl implements DeckService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeckServiceImpl.class);
    
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    
    @Autowired
    public DeckServiceImpl(DeckRepository deckRepository,
                         CardRepository cardRepository,
                         SpacedRepetitionService spacedRepetitionService) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
    }
    
    @Override
    public Deck createDeck(Long userId, String name, String description, Boolean isPublic) {
        // Validate input parameters
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        
        // Create new deck
        Deck deck = new Deck();
        deck.setUserId(userId);
        deck.setName(name.trim());
        deck.setDescription(description != null ? description.trim() : null);
        deck.setIsPublic(isPublic != null ? isPublic : false);
        
        // Set default review options
        deck.setNewCardsPerDay(20);
        deck.setMaxReviewsPerDay(100);
        deck.setEasyInterval(4);
        deck.setEasyBonus(1.3);
        deck.setIntervalModifier(1.0);
        deck.setStartingEase(2.5);
        deck.setSteps(1);
        
        // Save the deck
        Deck savedDeck = deckRepository.save(deck);
        
        logger.info("Created deck with ID: {} for user: {}", savedDeck.getId(), userId);
        
        return savedDeck;
    }
    
    @Override
    public Deck createDeckWithOptions(Long userId, String name, String description, Boolean isPublic,
                                     Integer newCardsPerDay, Integer maxReviewsPerDay, Integer easyInterval,
                                     Double easyBonus, Double intervalModifier, Double startingEase, Integer steps) {
        // Validate input parameters
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        
        // Create new deck
        Deck deck = new Deck();
        deck.setUserId(userId);
        deck.setName(name.trim());
        deck.setDescription(description != null ? description.trim() : null);
        deck.setIsPublic(isPublic != null ? isPublic : false);
        
        // Set review options
        deck.setNewCardsPerDay(newCardsPerDay != null ? newCardsPerDay : 20);
        deck.setMaxReviewsPerDay(maxReviewsPerDay != null ? maxReviewsPerDay : 100);
        deck.setEasyInterval(easyInterval != null ? easyInterval : 4);
        deck.setEasyBonus(easyBonus != null ? easyBonus : 1.3);
        deck.setIntervalModifier(intervalModifier != null ? intervalModifier : 1.0);
        deck.setStartingEase(startingEase != null ? startingEase : 2.5);
        deck.setSteps(steps != null ? steps : 1);
        
        // Save the deck
        Deck savedDeck = deckRepository.save(deck);
        
        logger.info("Created deck with ID: {} for user: {}", savedDeck.getId(), userId);
        
        return savedDeck;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Deck getDeckById(Long deckId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        Optional<Deck> deck = deckRepository.findById(deckId);
        if (!deck.isPresent()) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        return deck.get();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Deck getDeckById(Long deckId, Long userId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId);
        if (deck == null) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        return deck;
    }
    
    @Override
    public Deck updateDeck(Long deckId, String name, String description, Boolean isPublic, Long userId) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        
        Deck deck = getDeckById(deckId, userId);
        
        deck.setName(name.trim());
        deck.setDescription(description != null ? description.trim() : null);
        deck.setIsPublic(isPublic != null ? isPublic : false);
        
        Deck updatedDeck = deckRepository.save(deck);
        logger.info("Updated deck with ID: {}", deckId);
        
        return updatedDeck;
    }
    
    @Override
    public Deck updateDeckWithOptions(Long deckId, String name, String description, Boolean isPublic,
                                     Integer newCardsPerDay, Integer maxReviewsPerDay, Integer easyInterval,
                                     Double easyBonus, Double intervalModifier, Double startingEase, Integer steps,
                                     Long userId) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        
        Deck deck = getDeckById(deckId, userId);
        
        deck.setName(name.trim());
        deck.setDescription(description != null ? description.trim() : null);
        deck.setIsPublic(isPublic != null ? isPublic : false);
        
        // Update review options
        if (newCardsPerDay != null) deck.setNewCardsPerDay(newCardsPerDay);
        if (maxReviewsPerDay != null) deck.setMaxReviewsPerDay(maxReviewsPerDay);
        if (easyInterval != null) deck.setEasyInterval(easyInterval);
        if (easyBonus != null) deck.setEasyBonus(easyBonus);
        if (intervalModifier != null) deck.setIntervalModifier(intervalModifier);
        if (startingEase != null) deck.setStartingEase(startingEase);
        if (steps != null) deck.setSteps(steps);
        
        Deck updatedDeck = deckRepository.save(deck);
        logger.info("Updated deck with ID: {}", deckId);
        
        return updatedDeck;
    }
    
    @Override
    public void deleteDeck(Long deckId, Long userId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        Deck deck = getDeckById(deckId, userId);
        
        // Delete the deck (cascades to cards)
        deckRepository.delete(deck);
        logger.info("Deleted deck with ID: {}", deckId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Deck> getDecksByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return deckRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Deck> getPublicDecks() {
        return deckRepository.findByIsPublicTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Deck> getPublicDecksByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return deckRepository.findByUserIdAndIsPublicTrue(userId);
    }
    
    @Override
    public Deck addCardToDeck(Long deckId, Card card, Long userId) {
        Deck deck = getDeckById(deckId, userId);
        
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        
        // Check if card already belongs to a deck
        if (card.getDeckId() != null) {
            throw new IllegalArgumentException("Card already belongs to another deck");
        }
        
        // Associate card with deck
        card.setDeckId(deckId);
        cardRepository.save(card);
        
        // Save changes
        Deck updatedDeck = deckRepository.save(deck);
        logger.info("Added card with ID: {} to deck ID: {}", card.getId(), deckId);
        
        return updatedDeck;
    }
    
    @Override
    public Deck removeCardFromDeck(Long deckId, Long cardId, Long userId) {
        Deck deck = getDeckById(deckId, userId);
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));
        
        // Check if card belongs to this deck
        if (!deck.getId().equals(card.getDeckId())) {
            throw new IllegalArgumentException("Card does not belong to this deck");
        }
        
        // Remove association
        card.setDeckId(null);
        cardRepository.save(card);
        
        // Save changes
        Deck updatedDeck = deckRepository.save(deck);
        logger.info("Removed card with ID: {} from deck ID: {}", cardId, deckId);
        
        return updatedDeck;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsInDeck(Long deckId, Long userId) {
        Deck deck = getDeckById(deckId, userId);
        return cardRepository.findByDeckId(deckId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsInPublicDeck(Long deckId) {
        Deck deck = deckRepository.findByIdAndIsPublicTrue(deckId);
        if (deck == null) {
            throw new IllegalArgumentException("Public deck not found with ID: " + deckId);
        }
        return cardRepository.findByDeckId(deckId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Deck> searchDecks(String searchText, Long userId) {
        if (!StringUtils.hasText(searchText)) {
            return getDecksByUser(userId);
        }
        
        // For now, get all user decks and filter by search text
        // In a real app, we would add a specific repository method for this
        String searchLower = searchText.toLowerCase().trim();
        return getDecksByUser(userId).stream()
                .filter(deck -> deck.getName().toLowerCase().contains(searchLower) ||
                               (deck.getDescription() != null && deck.getDescription().toLowerCase().contains(searchLower)))
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCardCountInDeck(Long deckId, Long userId) {
        Deck deck = getDeckById(deckId, userId);
        return cardRepository.findByDeckId(deckId).size();
    }
    
    @Override
    public Deck duplicateDeck(Long deckId, Long userId, String newName) {
        if (!StringUtils.hasText(newName)) {
            throw new IllegalArgumentException("New deck name cannot be null or empty");
        }
        
        // Get the original deck
        Deck originalDeck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found with ID: " + deckId));
        
        // Create new deck
        Deck newDeck = new Deck();
        newDeck.setUserId(userId);
        newDeck.setName(newName.trim());
        newDeck.setDescription(originalDeck.getDescription());
        newDeck.setIsPublic(false); // Duplicated decks are private by default
        
        // Save new deck
        Deck savedDeck = deckRepository.save(newDeck);
        
        // Duplicate cards
        List<Card> originalCards = cardRepository.findByDeckId(deckId);
        for (Card originalCard : originalCards) {
            Card newCard = new Card();
            newCard.setUserId(userId);
            newCard.setDeckId(savedDeck.getId());
            newCard.setText(originalCard.getText());
            newCard.setBackText(originalCard.getBackText());
            newCard.setContext(originalCard.getContext());
            newCard.setUserComment(originalCard.getUserComment());
            newCard.setCardType(originalCard.getCardType());
            newCard.setTags(originalCard.getTags());
            newCard.setIsActive(originalCard.getIsActive());
            
            // Initialize spaced repetition data
            spacedRepetitionService.scheduleInitialReminder(newCard);
            
            // Save the new card
            cardRepository.save(newCard);
        }
        
        // Save changes
        Deck updatedDeck = deckRepository.save(savedDeck);
        logger.info("Duplicated deck ID: {} to new deck ID: {}", deckId, updatedDeck.getId());
        
        return updatedDeck;
    }
}