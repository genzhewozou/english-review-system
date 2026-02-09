package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.dao.jpa.StudyMaterialRepository;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.StudyMaterial;

import org.example.docvideoplay.entity.Tag;
import org.example.docvideoplay.repository.TagRepository;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.example.docvideoplay.service.TodoService;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of VocabularyService for managing vocabulary cards and comments.
 * Integrates with SpacedRepetitionService for automatic review scheduling.
 */
@Service
@Transactional
public class VocabularyServiceImpl implements VocabularyService {
    
    private static final Logger logger = LoggerFactory.getLogger(VocabularyServiceImpl.class);
    
    private final CardRepository cardRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final TagRepository tagRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final TodoService todoService;
    
    @Autowired
    public VocabularyServiceImpl(CardRepository cardRepository,
                                StudyMaterialRepository studyMaterialRepository,
                                TagRepository tagRepository,
                                SpacedRepetitionService spacedRepetitionService,
                                TodoService todoService) {
        this.cardRepository = cardRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.tagRepository = tagRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.todoService = todoService;
    }
    
    @Override
    public Card createCardFromHighlight(Long userId, Long materialId, String text, String context, 
                                   Integer startPosition, Integer endPosition) {
        return createCardFromHighlightWithComment(userId, materialId, text, context, startPosition, endPosition, null);
    }
    
    @Override
    public Card createCardFromHighlightWithComment(Long userId, Long materialId, String text, String context,
                                               Integer startPosition, Integer endPosition, String userComment) {
        return createCardFromHighlightWithCommentAndTags(userId, materialId, text, context, startPosition, endPosition, userComment, null);
    }
    
    @Override
    public Card createCardFromHighlightWithCommentAndTags(Long userId, Long materialId, String text, String context,
                                               Integer startPosition, Integer endPosition, String userComment, List<Long> tags) {
        // Validate input parameters
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Card text cannot be null or empty");
        }
        
        if (startPosition != null && endPosition != null) {
            if (!isValidPosition(startPosition, endPosition)) {
                throw new IllegalArgumentException("Invalid card positions: start=" + startPosition + ", end=" + endPosition);
            }
        }
        
        // Verify that the study material exists and belongs to the user
        Optional<StudyMaterial> materialOpt = studyMaterialRepository.findById(materialId);
        if (!materialOpt.isPresent()) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        StudyMaterial material = materialOpt.get();
        if (!material.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        // Create new card
        Card card = new Card();
        card.setUserId(userId);
        card.setMaterialId(materialId);
        card.setText(text.trim());
        card.setBackText(text.trim()); // Set backText to match text (required field)
        card.setContext(context != null ? context.trim() : null);
        card.setStartPosition(startPosition);
        card.setEndPosition(endPosition);
        card.setUserComment(userComment != null ? userComment.trim() : null);
        
        // Handle tags if provided
        if (tags != null && !tags.isEmpty()) {
            // Get the actual tag entities
            List<Tag> tagEntities = tagRepository.findAllById(tags);
            // Filter out tags that don't belong to the current user
            tagEntities = tagEntities.stream()
                    .filter(tag -> tag.getUserId().equals(userId) && tag.getIsActive())
                    .collect(Collectors.toList());
            // Convert tags to comma-separated string of IDs
            if (!tagEntities.isEmpty()) {
                String tagIds = tagEntities.stream()
                        .map(tag -> tag.getId().toString())
                        .collect(Collectors.joining(","));
                card.setTags(tagIds);
            }
        }
        
        // Schedule initial spaced repetition reminder
        spacedRepetitionService.scheduleInitialReminder(card);
        
        // Save the card
        Card savedCard = cardRepository.save(card);
       
        // Flush to ensure the card is persisted before creating todo items
        cardRepository.flush();
        
        // Schedule todo item for review reminder
        todoService.scheduleReviewReminder(savedCard);
        
        logger.info("Created card with ID: {} for material: {}", savedCard.getId(), materialId);
        
        return savedCard;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByMaterial(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // Verify that the study material exists
        if (!studyMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        return cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByMaterial(Long userId, Long materialId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // Verify that the study material exists
        if (!studyMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        return cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByMaterialWithHistory(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // Verify that the study material exists
        if (!studyMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        return cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByMaterialWithHistory(Long userId, Long materialId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // Verify that the study material exists
        if (!studyMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        // Return all cards for the material and filter by user
        return cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId).stream()
                .filter(card -> card.getUserId().equals(userId))
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getAllCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsWithComments(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findCardsWithCommentsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getNeverReviewedCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findNeverReviewedCardsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCardCountByMaterial(Long userId, Long materialId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // For now, get all cards for the material and count those belonging to the user
        // In a real app, we would add a specific repository method for this
        return cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId).stream()
                .filter(card -> card.getUserId().equals(userId))
                .count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsDueToday(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findCardsDueTodayByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getOverdueCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findOverdueCardsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Card getCardById(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        
        Optional<Card> card = cardRepository.findById(cardId);
        if (!card.isPresent()) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        return card.get();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Card getCardById(Long cardId, Long userId) {
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        Optional<Card> card = cardRepository.findById(cardId);
        if (!card.isPresent()) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        Card foundCard = card.get();
        if (!foundCard.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        return foundCard;
    }
    
    @Override
    public Card updateCardComment(Long cardId, String comment, Long userId) {
        Card card = getCardById(cardId, userId);
        
        card.setUserComment(comment != null ? comment.trim() : null);
        
        Card updatedCard = cardRepository.save(card);
        logger.info("Updated comment for card ID: {}", cardId);
        
        return updatedCard;
    }
    
    @Override
    public Card updateCardText(Long cardId, String text, String context, Long userId) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Card text cannot be null or empty");
        }
        
        Card card = getCardById(cardId, userId);
        
        card.setText(text.trim());
        card.setContext(context != null ? context.trim() : null);
        
        Card updatedCard = cardRepository.save(card);
        logger.info("Updated text for card ID: {}", cardId);
        
        return updatedCard;
    }
    
    @Override
    public Card updateCardPosition(Long cardId, Integer startPosition, Integer endPosition, Long userId) {
        if (!isValidPosition(startPosition, endPosition)) {
            throw new IllegalArgumentException("Invalid card positions: start=" + startPosition + ", end=" + endPosition);
        }
        
        Card card = getCardById(cardId, userId);
        
        card.setStartPosition(startPosition);
        card.setEndPosition(endPosition);
        
        Card updatedCard = cardRepository.save(card);
        logger.info("Updated position for card ID: {}", cardId);
        
        return updatedCard;
    }
    
    @Override
    public void deleteCard(Long cardId, Long userId) {
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        
        Card card = getCardById(cardId, userId);
        
        cardRepository.delete(card);
        logger.info("Deleted card with ID: {}", cardId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> searchCardsByText(String searchText) {
        if (!StringUtils.hasText(searchText)) {
            return new ArrayList<>(); // Return empty list for empty search
        }
        
        return cardRepository.findByTextContainingIgnoreCase(searchText.trim());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getAllCards() {
        return cardRepository.findAllByOrderByCreatedDateDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsWithComments() {
        return cardRepository.findCardsWithComments();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getNeverReviewedCards() {
        return cardRepository.findNeverReviewedCards();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCardCountByMaterial(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        return cardRepository.countByMaterialId(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsDueToday() {
        return cardRepository.findCardsDueToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getOverdueCards() {
        return cardRepository.findOverdueCards();
    }
    
    @Override
    public boolean isValidPosition(Integer startPosition, Integer endPosition) {
        if (startPosition == null || endPosition == null) {
            return false;
        }
        
        if (startPosition < 0 || endPosition < 0) {
            return false;
        }
        
        return startPosition <= endPosition;
    }
}