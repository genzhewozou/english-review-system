package org.example.docvideoplay.controller;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.service.CardService;
import org.example.docvideoplay.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for card management operations.
 * Handles card creation, updates, and management, including highlights converted to cards.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;
    private final UserService userService;

    @Autowired
    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    /**
     * Get the current authenticated user or default user
     * 
     * @return The current authenticated user or default user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If authenticated, get user by username
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            return userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        }
        
        // Default to user 'leo' if not authenticated
        return userService.findByUsername("leo")
                .orElseThrow(() -> new IllegalArgumentException("Default user not found"));
    }

    /**
     * Get the current authenticated user ID or default user ID
     * 
     * @return The current authenticated user ID or default user ID
     */
    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Create a new card from highlighted text in a study material.
     */
    @PostMapping("/from-highlight")
    public ResponseEntity<CardResultDto> createCardFromHighlight(@Valid @RequestBody CardFromHighlightParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Creating card from highlight: materialId={}, text={}, userId={}",
                    params.getMaterialId(), params.getText(), currentUserId);

            // Create card from highlight
            Card card = cardService.createCardFromHighlightWithCommentAndTags(
                    currentUserId,
                    params.getMaterialId(),
                    params.getText(),
                    params.getContext(),
                    params.getStartPosition(),
                    params.getEndPosition(),
                    params.getUserComment(),
                    params.getTags()
            );

            CardResultDto result = convertToResultDto(card);

            logger.info("Card created successfully: id={}", card.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Card creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during card creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new card directly (not from highlight).
     */
    @PostMapping
    public ResponseEntity<CardResultDto> createCard(@Valid @RequestBody CardParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Creating card: deckId={}, frontText={}, userId={}",
                    params.getDeckId(), params.getFrontText(), currentUserId);

            // Create card
            Card card = cardService.createCard(
                    currentUserId,
                    params.getDeckId(),
                    params.getFrontText(),
                    params.getBackText(),
                    params.getCardType(),
                    params.getTags()
            );

            CardResultDto result = convertToResultDto(card);

            logger.info("Card created successfully: id={}", card.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Card creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during card creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cards by material ID.
     */
    @GetMapping("/material/{materialId}")
    public ResponseEntity<List<CardResultDto>> getCardsByMaterial(@PathVariable Long materialId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards for material: id={}, userId={}", materialId, currentUserId);

            List<Card> cards = cardService.getCardsByMaterial(currentUserId, materialId);
            List<CardResultDto> results = cards.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} cards for material {} and userId {}", results.size(), materialId, currentUserId);
            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
            logger.warn("Material not found: id={}", materialId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving cards for material {}: {}", materialId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cards by deck ID.
     */
    @GetMapping("/deck/{deckId}")
    public ResponseEntity<List<CardResultDto>> getCardsByDeck(@PathVariable Long deckId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards for deck: id={}, userId={}", deckId, currentUserId);

            List<Card> cards = cardService.getCardsByDeck(currentUserId, deckId);
            List<CardResultDto> results = cards.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} cards for deck {} and userId {}", results.size(), deckId, currentUserId);
            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
            logger.warn("Deck not found: id={}", deckId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving cards for deck {}: {}", deckId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific card by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardResultDto> getCard(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving card: id={}, userId={}", id, currentUserId);

            Card card = cardService.getCardById(id, currentUserId);
            CardResultDto result = convertToResultDto(card);

            logger.debug("Retrieved card: id={}, text={}, userId={}", id, card.getText(), currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Card not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving card {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a card.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CardResultDto> updateCard(@PathVariable Long id, @Valid @RequestBody CardUpdateDto updateDto) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Updating card: id={}, userId={}", id, currentUserId);

            Card card = cardService.getCardById(id, currentUserId);

            // Update comment if provided
            if (updateDto.getUserComment() != null) {
                card = cardService.updateCardComment(id, updateDto.getUserComment(), currentUserId);
            }

            // Update text and context if provided
            if (updateDto.getFrontText() != null && !updateDto.getFrontText().trim().isEmpty()) {
                card = cardService.updateCardText(id, updateDto.getFrontText(), updateDto.getBackText(), updateDto.getContext(), currentUserId);
            }

            // Update positions if provided
            if (updateDto.getStartPosition() != null && updateDto.getEndPosition() != null) {
                card = cardService.updateCardPosition(id, updateDto.getStartPosition(), updateDto.getEndPosition(), currentUserId);
            }

            // Update deck if provided
            if (updateDto.getDeckId() != null) {
                card = cardService.updateCardDeck(id, updateDto.getDeckId(), currentUserId);
            }

            CardResultDto result = convertToResultDto(card);

            logger.info("Card updated successfully: id={}, userId={}", id, currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Card update validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating card {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a card.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting card: id={}, userId={}", id, currentUserId);

            cardService.deleteCard(id, currentUserId);

            logger.info("Card deleted successfully: id={}, userId={}", id, currentUserId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Card not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting card {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all cards for the current user.
     */
    @GetMapping
    public ResponseEntity<List<CardResultDto>> getAllCards() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving all cards for userId: {}", currentUserId);

            List<Card> cards = cardService.getAllCards(currentUserId);
            List<CardResultDto> results = cards.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} cards for userId {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving all cards: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cards due for review today.
     */
    @GetMapping("/due-today")
    public ResponseEntity<List<CardResultDto>> getCardsDueToday() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards due for review for userId: {}", currentUserId);

            List<Card> cards = cardService.getCardsDueToday(currentUserId);
            List<CardResultDto> results = cards.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} cards due for review for userId {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving cards due for review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search cards by text.
     */
    @GetMapping("/search")
    public ResponseEntity<List<CardResultDto>> searchCards(@RequestParam String query) {
        try {
            logger.debug("Searching cards with query: {}", query);

            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest().build();
            }

            List<Card> cards = cardService.searchCardsByText(query.trim());
            List<CardResultDto> results = cards.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} cards matching query: {}", results.size(), query);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error searching cards with query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Card entity to CardResultDto
     */
    private CardResultDto convertToResultDto(Card card) {
        CardResultDto dto = new CardResultDto();
        dto.setId(card.getId());
        dto.setMaterialId(card.getMaterialId());
        dto.setDeckId(card.getDeckId());
        dto.setFrontText(card.getText());
        dto.setBackText(card.getBackText());
        dto.setContext(card.getContext());
        dto.setStartPosition(card.getStartPosition());
        dto.setEndPosition(card.getEndPosition());
        dto.setUserComment(card.getUserComment());
        dto.setCardType(card.getCardType());
        dto.setEaseFactor(card.getEaseFactor());
        dto.setRepetitionCount(card.getRepetitionCount());
        dto.setIntervalDays(card.getIntervalDays());
        dto.setNextReviewDate(card.getNextReviewDate());
        dto.setLastReviewDate(card.getLastReviewDate());
        dto.setCreatedDate(card.getCreatedDate());
        dto.setUpdatedDate(card.getUpdatedDate());
        
        // Set tags
        if (card.getTags() != null && !card.getTags().isEmpty()) {
            List<Long> tagIds = java.util.Arrays.stream(card.getTags().split(","))
                    .map(Long::parseLong)
                    .collect(java.util.stream.Collectors.toList());
            dto.setTags(tagIds);
        }

        return dto;
    }

    // DTO classes for card operations
    public static class CardFromHighlightParamsDto {
        private Long materialId;
        private String text;
        private String context;
        private Integer startPosition;
        private Integer endPosition;
        private String userComment;
        private List<Long> tags;

        // Getters and setters
        public Long getMaterialId() { return materialId; }
        public void setMaterialId(Long materialId) { this.materialId = materialId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public Integer getStartPosition() { return startPosition; }
        public void setStartPosition(Integer startPosition) { this.startPosition = startPosition; }
        public Integer getEndPosition() { return endPosition; }
        public void setEndPosition(Integer endPosition) { this.endPosition = endPosition; }
        public String getUserComment() { return userComment; }
        public void setUserComment(String userComment) { this.userComment = userComment; }
        public List<Long> getTags() { return tags; }
        public void setTags(List<Long> tags) { this.tags = tags; }
    }

    public static class CardParamsDto {
        private Long deckId;
        private String frontText;
        private String backText;
        private String cardType;
        private List<Long> tags;

        // Getters and setters
        public Long getDeckId() { return deckId; }
        public void setDeckId(Long deckId) { this.deckId = deckId; }
        public String getFrontText() { return frontText; }
        public void setFrontText(String frontText) { this.frontText = frontText; }
        public String getBackText() { return backText; }
        public void setBackText(String backText) { this.backText = backText; }
        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }
        public List<Long> getTags() { return tags; }
        public void setTags(List<Long> tags) { this.tags = tags; }
    }

    public static class CardUpdateDto {
        private String frontText;
        private String backText;
        private String context;
        private Integer startPosition;
        private Integer endPosition;
        private String userComment;
        private Long deckId;

        // Getters and setters
        public String getFrontText() { return frontText; }
        public void setFrontText(String frontText) { this.frontText = frontText; }
        public String getBackText() { return backText; }
        public void setBackText(String backText) { this.backText = backText; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public Integer getStartPosition() { return startPosition; }
        public void setStartPosition(Integer startPosition) { this.startPosition = startPosition; }
        public Integer getEndPosition() { return endPosition; }
        public void setEndPosition(Integer endPosition) { this.endPosition = endPosition; }
        public String getUserComment() { return userComment; }
        public void setUserComment(String userComment) { this.userComment = userComment; }
        public Long getDeckId() { return deckId; }
        public void setDeckId(Long deckId) { this.deckId = deckId; }
    }

    public static class CardResultDto {
        private Long id;
        private Long materialId;
        private Long deckId;
        private String frontText;
        private String backText;
        private String context;
        private Integer startPosition;
        private Integer endPosition;
        private String userComment;
        private String cardType;
        private Double easeFactor;
        private Integer repetitionCount;
        private Integer intervalDays;
        private java.time.LocalDate nextReviewDate;
        private java.time.LocalDate lastReviewDate;
        private java.time.LocalDateTime createdDate;
        private java.time.LocalDateTime updatedDate;
        private List<Long> tags;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getMaterialId() { return materialId; }
        public void setMaterialId(Long materialId) { this.materialId = materialId; }
        public Long getDeckId() { return deckId; }
        public void setDeckId(Long deckId) { this.deckId = deckId; }
        public String getFrontText() { return frontText; }
        public void setFrontText(String frontText) { this.frontText = frontText; }
        public String getBackText() { return backText; }
        public void setBackText(String backText) { this.backText = backText; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public Integer getStartPosition() { return startPosition; }
        public void setStartPosition(Integer startPosition) { this.startPosition = startPosition; }
        public Integer getEndPosition() { return endPosition; }
        public void setEndPosition(Integer endPosition) { this.endPosition = endPosition; }
        public String getUserComment() { return userComment; }
        public void setUserComment(String userComment) { this.userComment = userComment; }
        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }
        public Double getEaseFactor() { return easeFactor; }
        public void setEaseFactor(Double easeFactor) { this.easeFactor = easeFactor; }
        public Integer getRepetitionCount() { return repetitionCount; }
        public void setRepetitionCount(Integer repetitionCount) { this.repetitionCount = repetitionCount; }
        public Integer getIntervalDays() { return intervalDays; }
        public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
        public java.time.LocalDate getNextReviewDate() { return nextReviewDate; }
        public void setNextReviewDate(java.time.LocalDate nextReviewDate) { this.nextReviewDate = nextReviewDate; }
        public java.time.LocalDate getLastReviewDate() { return lastReviewDate; }
        public void setLastReviewDate(java.time.LocalDate lastReviewDate) { this.lastReviewDate = lastReviewDate; }
        public java.time.LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(java.time.LocalDateTime createdDate) { this.createdDate = createdDate; }
        public java.time.LocalDateTime getUpdatedDate() { return updatedDate; }
        public void setUpdatedDate(java.time.LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
        public List<Long> getTags() { return tags; }
        public void setTags(List<Long> tags) { this.tags = tags; }
    }
}
