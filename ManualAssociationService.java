package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.*;
import org.example.docvideoplay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer example for handling associations manually
 * without foreign key relationships
 */
@Service
public class ManualAssociationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private StudyMaterialRepository studyMaterialRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private ReviewSessionRepository reviewSessionRepository;

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @Autowired
    private CardTemplateRepository cardTemplateRepository;

    @Autowired
    private TagRepository tagRepository;

    /**
     * Example: Creating a Card with manual associations
     */
    @Transactional
    public Card createCard(Long userId, Long materialId, String text, String backText, String context) {
        // Validate user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // Validate study material exists if provided
        if (materialId != null) {
            Optional<StudyMaterial> materialOptional = studyMaterialRepository.findById(materialId);
            if (!materialOptional.isPresent()) {
                throw new IllegalArgumentException("Study material not found: " + materialId);
            }
        }

        // Create card with only ID fields
        Card card = new Card();
        card.setUserId(userId);
        card.setMaterialId(materialId);
        card.setText(text);
        card.setBackText(backText);
        card.setContext(context);

        return cardRepository.save(card);
    }

    /**
     * Example: Getting a Card with its associated entities
     */
    @Transactional(readOnly = true)
    public CardWithAssociations getCardWithAssociations(Long cardId) {
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        if (!cardOptional.isPresent()) {
            throw new IllegalArgumentException("Card not found: " + cardId);
        }

        Card card = cardOptional.get();
        CardWithAssociations result = new CardWithAssociations(card);

        // Manually fetch associated entities
        if (card.getUserId() != null) {
            Optional<User> userOptional = userRepository.findById(card.getUserId());
            userOptional.ifPresent(result::setUser);
        }

        if (card.getMaterialId() != null) {
            Optional<StudyMaterial> materialOptional = studyMaterialRepository.findById(card.getMaterialId());
            materialOptional.ifPresent(result::setMaterial);
        }

        if (card.getDeckId() != null) {
            Optional<Deck> deckOptional = deckRepository.findById(card.getDeckId());
            deckOptional.ifPresent(result::setDeck);
        }

        return result;
    }

    /**
     * Example: Creating a ReviewSession with associated ReviewRecords
     */
    @Transactional
    public ReviewSession createReviewSession(Long userId, List<Long> cardIds) {
        // Validate user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // Validate all cards exist
        for (Long cardId : cardIds) {
            Optional<Card> cardOptional = cardRepository.findById(cardId);
            if (!cardOptional.isPresent()) {
                throw new IllegalArgumentException("Card not found: " + cardId);
            }
        }

        // Create review session
        ReviewSession session = new ReviewSession();
        session.setUserId(userId);
        session.setSelectedCardIds(cardIds);
        session = reviewSessionRepository.save(session);

        return session;
    }

    /**
     * Example: Adding a ReviewRecord to a ReviewSession
     */
    @Transactional
    public ReviewRecord addReviewRecord(Long sessionId, Long cardId, String quality) {
        // Validate session exists
        Optional<ReviewSession> sessionOptional = reviewSessionRepository.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            throw new IllegalArgumentException("Review session not found: " + sessionId);
        }

        // Validate card exists
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        if (!cardOptional.isPresent()) {
            throw new IllegalArgumentException("Card not found: " + cardId);
        }

        // Create review record
        ReviewRecord record = new ReviewRecord();
        record.setSessionId(sessionId);
        record.setCardId(cardId);
        record.setQuality(quality); // Assuming quality is an enum
        record.setReviewTime(LocalDateTime.now());

        return reviewRecordRepository.save(record);
    }

    /**
     * Example: Creating a TodoItem with associations
     */
    @Transactional
    public TodoItem createTodoItem(Long userId, String title, String description, Long relatedCardId, Long relatedSessionId) {
        // Validate user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // Validate related card exists if provided
        if (relatedCardId != null) {
            Optional<Card> cardOptional = cardRepository.findById(relatedCardId);
            if (!cardOptional.isPresent()) {
                throw new IllegalArgumentException("Card not found: " + relatedCardId);
            }
        }

        // Validate related session exists if provided
        if (relatedSessionId != null) {
            Optional<ReviewSession> sessionOptional = reviewSessionRepository.findById(relatedSessionId);
            if (!sessionOptional.isPresent()) {
                throw new IllegalArgumentException("Review session not found: " + relatedSessionId);
            }
        }

        // Create todo item
        TodoItem todoItem = new TodoItem();
        todoItem.setUserId(userId);
        todoItem.setTitle(title);
        todoItem.setDescription(description);
        todoItem.setRelatedCardId(relatedCardId);
        todoItem.setRelatedSessionId(relatedSessionId);

        return todoItemRepository.save(todoItem);
    }

    /**
     * Example: Deleting a User with manual cleanup of associated entities
     */
    @Transactional
    public void deleteUser(Long userId) {
        // Validate user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // Manually delete associated todo items
        List<TodoItem> todoItems = todoItemRepository.findByUserId(userId);
        todoItemRepository.deleteAll(todoItems);

        // Manually delete associated review sessions and their records
        List<ReviewSession> reviewSessions = reviewSessionRepository.findByUserId(userId);
        for (ReviewSession session : reviewSessions) {
            List<ReviewRecord> reviewRecords = reviewRecordRepository.findBySessionId(session.getId());
            reviewRecordRepository.deleteAll(reviewRecords);
        }
        reviewSessionRepository.deleteAll(reviewSessions);

        // Manually delete associated cards
        List<Card> cards = cardRepository.findByUserId(userId);
        cardRepository.deleteAll(cards);

        // Manually delete associated study materials
        List<StudyMaterial> materials = studyMaterialRepository.findByUserId(userId);
        studyMaterialRepository.deleteAll(materials);

        // Manually delete associated decks
        List<Deck> decks = deckRepository.findByUserId(userId);
        deckRepository.deleteAll(decks);

        // Manually delete associated card templates
        List<CardTemplate> templates = cardTemplateRepository.findByUserId(userId);
        cardTemplateRepository.deleteAll(templates);

        // Manually delete associated tags
        List<Tag> tags = tagRepository.findByUserId(userId);
        tagRepository.deleteAll(tags);

        // Now delete the user
        userRepository.deleteById(userId);
    }

    /**
     * DTO for returning a Card with its associated entities
     */
    public static class CardWithAssociations {
        private Card card;
        private User user;
        private StudyMaterial material;
        private Deck deck;

        public CardWithAssociations(Card card) {
            this.card = card;
        }

        public Card getCard() {
            return card;
        }

        public void setCard(Card card) {
            this.card = card;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public StudyMaterial getMaterial() {
            return material;
        }

        public void setMaterial(StudyMaterial material) {
            this.material = material;
        }

        public Deck getDeck() {
            return deck;
        }

        public void setDeck(Deck deck) {
            this.deck = deck;
        }
    }
}
