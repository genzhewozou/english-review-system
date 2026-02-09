package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.VocabularyApi;
import org.example.docvideoplay.dto.api.CardParamsDto;
import org.example.docvideoplay.dto.api.CardResultDto;
import org.example.docvideoplay.dto.api.CardUpdateDto;
import org.example.docvideoplay.service.UserService;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for vocabulary card operations.
 * Implements VocabularyApi interface for card creation, updates, and comment management.
 */
@RestController
public class VocabularyController implements VocabularyApi {

    private static final Logger logger = LoggerFactory.getLogger(VocabularyController.class);

    private final VocabularyService vocabularyService;
    private final UserService userService;

    @Autowired
    public VocabularyController(VocabularyService vocabularyService, UserService userService) {
        this.vocabularyService = vocabularyService;
        this.userService = userService;
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
    public ResponseEntity<CardResultDto> createCard(@Valid CardParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Creating card: materialId={}, text={}, userId={}",
                    params.getMaterialId(), params.getText(), currentUserId);

            // Validate positions if provided
            if (params.getStartPosition() != null && params.getEndPosition() != null) {
                if (!vocabularyService.isValidPosition(params.getStartPosition(), params.getEndPosition())) {
                    logger.warn("Invalid card positions: start={}, end={}",
                            params.getStartPosition(), params.getEndPosition());
                    return ResponseEntity.badRequest().build();
                }
            }

            // Create card with comment and tags
            org.example.docvideoplay.entity.Card card = vocabularyService.createCardFromHighlightWithCommentAndTags(
                    currentUserId,
                    params.getMaterialId(),
                    params.getText(),
                    params.getContext(),
                    params.getStartPosition(),
                    params.getEndPosition(),
                    params.getUserComment(),
                    params.getTags()
            );

            // Convert to DTO
            CardResultDto result = new CardResultDto();
            result.setId(card.getId());
            result.setMaterialId(card.getMaterialId());
            result.setText(card.getText());
            result.setContext(card.getContext());
            result.setStartPosition(card.getStartPosition());
            result.setEndPosition(card.getEndPosition());
            result.setUserComment(card.getUserComment());
            result.setCreatedDate(card.getCreatedDate());

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

    @Override
    public ResponseEntity<List<CardResultDto>> getCardsByMaterial(Long materialId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards for material: id={}, userId={}", materialId, currentUserId);

            List<org.example.docvideoplay.entity.Card> cards = vocabularyService.getCardsByMaterial(currentUserId, materialId);
            List<CardResultDto> results = cards.stream()
                    .map(card -> {
                        CardResultDto dto = new CardResultDto();
                        dto.setId(card.getId());
                        dto.setMaterialId(card.getMaterialId());
                        dto.setText(card.getText());
                        dto.setContext(card.getContext());
                        dto.setStartPosition(card.getStartPosition());
                        dto.setEndPosition(card.getEndPosition());
                        dto.setUserComment(card.getUserComment());
                        dto.setCreatedDate(card.getCreatedDate());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());

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

    @Override
    public ResponseEntity<List<CardResultDto>> getCardsBySpecificMaterial(Long materialId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards for material: id={}, userId={}", materialId, currentUserId);

            List<org.example.docvideoplay.entity.Card> cards = vocabularyService.getCardsByMaterial(currentUserId, materialId);
            List<CardResultDto> results = cards.stream()
                    .map(card -> {
                        CardResultDto dto = new CardResultDto();
                        dto.setId(card.getId());
                        dto.setMaterialId(card.getMaterialId());
                        dto.setText(card.getText());
                        dto.setContext(card.getContext());
                        dto.setStartPosition(card.getStartPosition());
                        dto.setEndPosition(card.getEndPosition());
                        dto.setUserComment(card.getUserComment());
                        dto.setCreatedDate(card.getCreatedDate());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());

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

    @Override
    public ResponseEntity<CardResultDto> getCard(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving card: id={}, userId={}", id, currentUserId);

            org.example.docvideoplay.entity.Card card = vocabularyService.getCardById(id, currentUserId);
            
            // Convert to DTO
            CardResultDto result = new CardResultDto();
            result.setId(card.getId());
            result.setMaterialId(card.getMaterialId());
            result.setText(card.getText());
            result.setContext(card.getContext());
            result.setStartPosition(card.getStartPosition());
            result.setEndPosition(card.getEndPosition());
            result.setUserComment(card.getUserComment());
            result.setCreatedDate(card.getCreatedDate());

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

    @Override
    public ResponseEntity<CardResultDto> updateCard(Long id, @Valid CardUpdateDto updateDto) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Updating card: id={}, userId={}", id, currentUserId);

            org.example.docvideoplay.entity.Card card = vocabularyService.getCardById(id, currentUserId);

            // Update comment if provided
            if (updateDto.getUserComment() != null) {
                card = vocabularyService.updateCardComment(id, updateDto.getUserComment(), currentUserId);
            }

            // Update text and context if provided
            if (updateDto.getText() != null && !updateDto.getText().trim().isEmpty()) {
                card = vocabularyService.updateCardText(id, updateDto.getText(), updateDto.getContext(), currentUserId);
            }

            // Update positions if provided and valid
            if (updateDto.getStartPosition() != null && updateDto.getEndPosition() != null) {
                if (!vocabularyService.isValidPosition(updateDto.getStartPosition(), updateDto.getEndPosition())) {
                    logger.warn("Invalid card positions: start={}, end={}",
                            updateDto.getStartPosition(), updateDto.getEndPosition());
                    return ResponseEntity.badRequest().build();
                }
                card = vocabularyService.updateCardPosition(id, updateDto.getStartPosition(), updateDto.getEndPosition(), currentUserId);
            }

            // Convert to DTO
            CardResultDto result = new CardResultDto();
            result.setId(card.getId());
            result.setMaterialId(card.getMaterialId());
            result.setText(card.getText());
            result.setContext(card.getContext());
            result.setStartPosition(card.getStartPosition());
            result.setEndPosition(card.getEndPosition());
            result.setUserComment(card.getUserComment());
            result.setCreatedDate(card.getCreatedDate());

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

    @Override
    public ResponseEntity<Void> deleteCard(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting card: id={}, userId={}", id, currentUserId);

            vocabularyService.deleteCard(id, currentUserId);

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

    @Override
    public ResponseEntity<List<CardResultDto>> getCardsDueForReview() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving cards due for review for userId: {}", currentUserId);

            List<org.example.docvideoplay.entity.Card> cards = vocabularyService.getCardsDueToday(currentUserId);
            List<CardResultDto> results = cards.stream()
                    .map(card -> {
                        CardResultDto dto = new CardResultDto();
                        dto.setId(card.getId());
                        dto.setMaterialId(card.getMaterialId());
                        dto.setText(card.getText());
                        dto.setContext(card.getContext());
                        dto.setStartPosition(card.getStartPosition());
                        dto.setEndPosition(card.getEndPosition());
                        dto.setUserComment(card.getUserComment());
                        dto.setCreatedDate(card.getCreatedDate());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());

            logger.debug("Retrieved {} cards due for review for userId {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving cards due for review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResultDto>> getAllCards() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving all cards for userId: {}", currentUserId);

            List<org.example.docvideoplay.entity.Card> cards = vocabularyService.getAllCards(currentUserId);
            List<CardResultDto> results = cards.stream()
                    .map(card -> {
                        CardResultDto dto = new CardResultDto();
                        dto.setId(card.getId());
                        dto.setMaterialId(card.getMaterialId());
                        dto.setText(card.getText());
                        dto.setContext(card.getContext());
                        dto.setStartPosition(card.getStartPosition());
                        dto.setEndPosition(card.getEndPosition());
                        dto.setUserComment(card.getUserComment());
                        dto.setCreatedDate(card.getCreatedDate());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());

            logger.debug("Retrieved {} cards for userId {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving all cards: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResultDto>> getAllCardsReal() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving all cards for userId: {}", currentUserId);

            List<org.example.docvideoplay.entity.Card> cards = vocabularyService.getAllCards(currentUserId);
            List<CardResultDto> results = cards.stream()
                    .map(card -> {
                        CardResultDto dto = new CardResultDto();
                        dto.setId(card.getId());
                        dto.setMaterialId(card.getMaterialId());
                        dto.setText(card.getText());
                        dto.setContext(card.getContext());
                        dto.setStartPosition(card.getStartPosition());
                        dto.setEndPosition(card.getEndPosition());
                        dto.setUserComment(card.getUserComment());
                        dto.setCreatedDate(card.getCreatedDate());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());

            logger.debug("Retrieved {} cards for userId {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving all cards: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResultDto>> searchCards(String query) {
        try {
            logger.debug("Searching cards with query: {}", query);

            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest().build();
            }

            List<org.example.docvideoplay.entity.Card> cards = vocabularyService.searchCardsByText(query.trim());
            List<CardResultDto> results = cards.stream()
                    .map(card -> {
                        CardResultDto dto = new CardResultDto();
                        dto.setId(card.getId());
                        dto.setMaterialId(card.getMaterialId());
                        dto.setText(card.getText());
                        dto.setContext(card.getContext());
                        dto.setStartPosition(card.getStartPosition());
                        dto.setEndPosition(card.getEndPosition());
                        dto.setUserComment(card.getUserComment());
                        dto.setCreatedDate(card.getCreatedDate());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());

            logger.debug("Found {} cards matching query: {}", results.size(), query);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error searching cards with query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
