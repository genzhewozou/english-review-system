package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.repository.DeckRepository;
import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.dao.jpa.StudyMaterialRepository;
import org.example.docvideoplay.entity.Deck;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.entity.Tag;
import org.example.docvideoplay.repository.TagRepository;
import org.example.docvideoplay.service.CardService;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.example.docvideoplay.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CardService for managing cards (including highlights converted to cards).
 * Integrates with SpacedRepetitionService for automatic review scheduling.
 */
@Service
@Transactional
public class CardServiceImpl implements CardService {
    
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);
    
    private final CardRepository cardRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final DeckRepository deckRepository;
    private final TagRepository tagRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final TodoService todoService;
    
    @Autowired
    public CardServiceImpl(CardRepository cardRepository,
                          StudyMaterialRepository studyMaterialRepository,
                          DeckRepository deckRepository,
                          TagRepository tagRepository,
                          SpacedRepetitionService spacedRepetitionService,
                          TodoService todoService) {
        this.cardRepository = cardRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.deckRepository = deckRepository;
        this.tagRepository = tagRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.todoService = todoService;
    }
    
    @Override
    public Card createCardFromHighlight(Long userId, Long materialId, String text, String context, 
                                           Integer startPosition, Integer endPosition) {
        return createCardFromHighlightWithCommentAndTags(userId, materialId, text, context, startPosition, endPosition, null, null);
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
        
        if (startPosition != null && endPosition != null && startPosition > endPosition) {
            throw new IllegalArgumentException("Invalid positions: start must be <= end");
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
        
        logger.info("Created card from highlight with ID: {} for material: {}", savedCard.getId(), materialId);
        
        return savedCard;
    }
    
    @Override
    public Card createCard(Long userId, Long deckId, String frontText, String backText, String cardType, List<Long> tags) {
        // Validate input parameters
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        if (!StringUtils.hasText(frontText)) {
            throw new IllegalArgumentException("Card front text cannot be null or empty");
        }
        
        if (!StringUtils.hasText(backText)) {
            throw new IllegalArgumentException("Card back text cannot be null or empty");
        }
        
        // Verify that the deck exists and belongs to the user
        Optional<Deck> deckOpt = deckRepository.findById(deckId);
        if (!deckOpt.isPresent()) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        Deck deck = deckOpt.get();
        if (!deck.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        // Create new card
        Card card = new Card();
        card.setUserId(userId);
        card.setDeckId(deckId);
        card.setText(frontText.trim());
        card.setBackText(backText.trim());
        card.setCardType(cardType != null ? cardType : "BASIC");
        
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
        
        logger.info("Created card with ID: {} for deck: {}", savedCard.getId(), deckId);
        
        return savedCard;
    }
    
    @Override
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
    public List<Card> getCardsByDeck(Long userId, Long deckId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        // Verify that the deck exists and belongs to the user
        Optional<Deck> deckOpt = deckRepository.findById(deckId);
        if (!deckOpt.isPresent()) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        Deck deck = deckOpt.get();
        if (!deck.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        return cardRepository.findByDeckId(deckId);
    }
    
    @Override
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
    public Card updateCardText(Long cardId, String frontText, String backText, String context, Long userId) {
        if (!StringUtils.hasText(frontText)) {
            throw new IllegalArgumentException("Card front text cannot be null or empty");
        }
        
        Card card = getCardById(cardId, userId);
        
        card.setText(frontText.trim());
        if (backText != null) {
            card.setBackText(backText.trim());
        }
        card.setContext(context != null ? context.trim() : null);
        
        Card updatedCard = cardRepository.save(card);
        logger.info("Updated text for card ID: {}", cardId);
        
        return updatedCard;
    }
    
    @Override
    public Card updateCardPosition(Long cardId, Integer startPosition, Integer endPosition, Long userId) {
        if (startPosition != null && endPosition != null && startPosition > endPosition) {
            throw new IllegalArgumentException("Invalid positions: start must be <= end");
        }
        
        Card card = getCardById(cardId, userId);
        
        card.setStartPosition(startPosition);
        card.setEndPosition(endPosition);
        
        Card updatedCard = cardRepository.save(card);
        logger.info("Updated position for card ID: {}", cardId);
        
        return updatedCard;
    }
    
    @Override
    public Card updateCardDeck(Long cardId, Long deckId, Long userId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        Card card = getCardById(cardId, userId);
        
        // Verify that the deck exists and belongs to the user
        Optional<Deck> deckOpt = deckRepository.findById(deckId);
        if (!deckOpt.isPresent()) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        Deck deck = deckOpt.get();
        if (!deck.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Deck not found with ID: " + deckId);
        }
        
        card.setDeckId(deckId);
        
        Card updatedCard = cardRepository.save(card);
        logger.info("Updated deck for card ID: {}", cardId);
        
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
    public List<Card> searchCardsByText(String searchText) {
        if (!StringUtils.hasText(searchText)) {
            return java.util.Collections.emptyList();
        }
        
        return cardRepository.findByTextContainingIgnoreCase(searchText.trim());
    }
    
    @Override
    public List<Card> getAllCards() {
        return cardRepository.findAllByOrderByCreatedDateDesc();
    }
    
    @Override
    public List<Card> getAllCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
    
    @Override
    public List<Card> getNeverReviewedCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findNeverReviewedCardsByUserId(userId);
    }
    
    @Override
    public List<Card> getCardsDueToday(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findCardsDueTodayByUserId(userId);
    }
    
    @Override
    public List<Card> getOverdueCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cardRepository.findOverdueCardsByUserId(userId);
    }
}
