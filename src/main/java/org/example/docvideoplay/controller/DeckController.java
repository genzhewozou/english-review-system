package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.DeckApi;
import org.example.docvideoplay.dto.api.*;
import org.example.docvideoplay.entity.Deck;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.service.DeckService;
import org.example.docvideoplay.service.UserService;
import org.example.docvideoplay.service.VocabularyService;
import org.example.docvideoplay.dao.jpa.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for deck management operations.
 * Implements DeckApi interface for deck creation, updates, and card management.
 */
@RestController
public class DeckController implements DeckApi {

    private static final Logger logger = LoggerFactory.getLogger(DeckController.class);

    private final DeckService deckService;
    private final UserService userService;
    private final VocabularyService vocabularyService;
    private final CardRepository cardRepository;

    @Autowired
    public DeckController(DeckService deckService, UserService userService, VocabularyService vocabularyService, 
                         CardRepository cardRepository) {
        this.deckService = deckService;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
        this.cardRepository = cardRepository;
    }

    /**
     * Get the current authenticated user ID or default user ID
     * 
     * @return The current authenticated user ID or default user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If authenticated, get user by username
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            return userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
                    .getId();
        }
        
        // Default to user 'leo' if not authenticated
        return userService.findByUsername("leo")
                .orElseThrow(() -> new IllegalArgumentException("Default user not found"))
                .getId();
    }

    @Override
    public ResponseEntity<DeckResultDto> createDeck(@Valid DeckParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Creating deck: name={}, isPublic={}, userId={}",
                    params.getName(), params.getIsPublic(), currentUserId);

            Deck deck = deckService.createDeck(
                    currentUserId,
                    params.getName(),
                    params.getDescription(),
                    params.getIsPublic()
            );

            DeckResultDto result = convertToResultDto(deck);

            logger.info("Deck created successfully: id={}", deck.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Deck creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during deck creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<DeckResultDto>> getDecks() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving decks for userId: {}", currentUserId);

            List<Deck> decks = deckService.getDecksByUser(currentUserId);
            List<DeckResultDto> results = decks.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} decks for userId {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving decks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DeckResultDto> getDeck(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving deck: id={}, userId={}", id, currentUserId);

            Deck deck = deckService.getDeckById(id, currentUserId);
            DeckResultDto result = convertToResultDto(deck);

            logger.debug("Retrieved deck: id={}, name={}, userId={}", id, deck.getName(), currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Deck not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DeckResultDto> updateDeck(Long id, @Valid DeckUpdateDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Updating deck: id={}, userId={}", id, currentUserId);

            Deck deck = deckService.getDeckById(id, currentUserId);

            // Update deck fields
            String name = params.getName() != null ? params.getName() : deck.getName();
            String description = params.getDescription() != null ? params.getDescription() : deck.getDescription();
            Boolean isPublic = params.getIsPublic() != null ? params.getIsPublic() : deck.getIsPublic();

            deck = deckService.updateDeck(id, name, description, isPublic, currentUserId);

            DeckResultDto result = convertToResultDto(deck);

            logger.info("Deck updated successfully: id={}, userId={}", id, currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Deck update validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteDeck(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting deck: id={}, userId={}", id, currentUserId);

            deckService.deleteDeck(id, currentUserId);

            logger.info("Deck deleted successfully: id={}, userId={}", id, currentUserId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Deck not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResultDto>> getDeckCards(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards for deck: id={}, userId={}", id, currentUserId);

            List<Card> cards = deckService.getCardsInDeck(id, currentUserId);
            List<CardResultDto> results = cards.stream()
                    .map(this::convertToCardResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} cards for deck {} and userId {}", results.size(), id, currentUserId);
            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
            logger.warn("Deck not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving cards for deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DeckResultDto> addCardToDeck(Long id, @Valid DeckCardParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Adding card to deck: deckId={}, cardId={}, userId={}",
                    id, params.getCardId(), currentUserId);

            Card card = vocabularyService.getCardById(params.getCardId(), currentUserId);
            Deck deck = deckService.addCardToDeck(id, card, currentUserId);

            DeckResultDto result = convertToResultDto(deck);

            logger.info("Card added successfully to deck {}: cardId={}", id, params.getCardId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Card addition validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error adding card to deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DeckResultDto> removeCardFromDeck(Long id, Long cardId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Removing card from deck: deckId={}, cardId={}, userId={}",
                    id, cardId, currentUserId);

            Deck deck = deckService.removeCardFromDeck(id, cardId, currentUserId);

            DeckResultDto result = convertToResultDto(deck);

            logger.info("Card removed successfully from deck {}: cardId={}", id, cardId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Card removal validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error removing card from deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<DeckResultDto>> getPublicDecks() {
        try {
            logger.debug("Retrieving public decks");

            List<Deck> decks = deckService.getPublicDecks();
            List<DeckResultDto> results = decks.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} public decks", results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving public decks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DeckResultDto> duplicateDeck(Long id, @Valid DeckDuplicateParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Duplicating deck: id={}, newName={}, userId={}",
                    id, params.getNewName(), currentUserId);

            Deck deck = deckService.duplicateDeck(id, currentUserId, params.getNewName());

            DeckResultDto result = convertToResultDto(deck);

            logger.info("Deck duplicated successfully: originalId={}, newId={}", id, deck.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Deck duplication validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error duplicating deck {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<DeckResultDto>> searchDecks(String query) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Searching decks with query: {}, userId={}", query, currentUserId);

            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest().build();
            }

            List<Deck> decks = deckService.searchDecks(query.trim(), currentUserId);
            List<DeckResultDto> results = decks.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} decks matching query: {}, userId={}", results.size(), query, currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error searching decks with query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Deck entity to DeckResultDto
     *
     * @param deck The Deck entity
     * @return The converted DTO
     */
    private DeckResultDto convertToResultDto(Deck deck) {
        DeckResultDto dto = new DeckResultDto();
        dto.setId(deck.getId());
        dto.setName(deck.getName());
        dto.setDescription(deck.getDescription());
        dto.setIsPublic(deck.getIsPublic());
        dto.setUserId(deck.getUserId());
        // Get username from user service
        String userName = userService.findById(deck.getUserId())
                .map(User::getUsername)
                .orElse("Unknown");
        dto.setUserName(userName);
        // Get card count from repository
        dto.setCardCount(cardRepository.findByDeckId(deck.getId()).size());
        dto.setCreatedDate(deck.getCreatedDate());
        dto.setUpdatedDate(deck.getUpdatedDate());
        
        // Set review options
        dto.setNewCardsPerDay(deck.getNewCardsPerDay());
        dto.setMaxReviewsPerDay(deck.getMaxReviewsPerDay());
        dto.setEasyInterval(deck.getEasyInterval());
        dto.setEasyBonus(deck.getEasyBonus());
        dto.setIntervalModifier(deck.getIntervalModifier());
        dto.setStartingEase(deck.getStartingEase());
        dto.setSteps(deck.getSteps());

        return dto;
    }

    /**
     * Convert Card entity to CardResultDto
     *
     * @param card The Card entity
     * @return The converted DTO
     */
    private CardResultDto convertToCardResultDto(Card card) {
        CardResultDto dto = new CardResultDto();
        dto.setId(card.getId());
        dto.setMaterialId(card.getMaterialId());
        dto.setText(card.getText());
        dto.setContext(card.getContext());
        dto.setStartPosition(card.getStartPosition());
        dto.setEndPosition(card.getEndPosition());
        dto.setUserComment(card.getUserComment());
        dto.setEaseFactor(card.getEaseFactor());
        dto.setRepetitionCount(card.getRepetitionCount());
        dto.setIntervalDays(card.getIntervalDays());
        dto.setNextReviewDate(card.getNextReviewDate());
        dto.setLastReviewDate(card.getLastReviewDate());
        dto.setCreatedDate(card.getCreatedDate());
        dto.setUpdatedDate(card.getUpdatedDate());

        return dto;
    }
}