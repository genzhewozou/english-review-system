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
 * Service-level validation examples to ensure data integrity
 * after removing foreign key constraints
 */
@Service
public class ServiceValidationExamples {

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

    /**
     * Example: Creating a Card with validation
     */
    @Transactional
    public Card createCard(Card card) {
        // Validate user exists
        if (card.getUser() != null && card.getUser().getId() != null) {
            Optional<User> userOptional = userRepository.findById(card.getUser().getId());
            if (!userOptional.isPresent()) {
                throw new IllegalArgumentException("User not found: " + card.getUser().getId());
            }
            card.setUser(userOptional.get());
        }
        
        // Validate study material exists if provided
        if (card.getMaterial() != null && card.getMaterial().getId() != null) {
            Optional<StudyMaterial> materialOptional = studyMaterialRepository.findById(card.getMaterial().getId());
            if (!materialOptional.isPresent()) {
                throw new IllegalArgumentException("Study material not found: " + card.getMaterial().getId());
            }
            card.setMaterial(materialOptional.get());
        }
        
        // Validate deck exists if provided
        if (card.getDeck() != null && card.getDeck().getId() != null) {
            Optional<Deck> deckOptional = deckRepository.findById(card.getDeck().getId());
            if (!deckOptional.isPresent()) {
                throw new IllegalArgumentException("Deck not found: " + card.getDeck().getId());
            }
            card.setDeck(deckOptional.get());
        }
        
        return cardRepository.save(card);
    }

    /**
     * Example: Creating a TodoItem with validation
     */
    @Transactional
    public TodoItem createTodoItem(TodoItem todoItem) {
        // Validate user exists
        if (todoItem.getUser() != null && todoItem.getUser().getId() != null) {
            Optional<User> userOptional = userRepository.findById(todoItem.getUser().getId());
            if (!userOptional.isPresent()) {
                throw new IllegalArgumentException("User not found: " + todoItem.getUser().getId());
            }
            todoItem.setUser(userOptional.get());
        }
        
        // Validate card exists if provided
        if (todoItem.getRelatedCard() != null && todoItem.getRelatedCard().getId() != null) {
            Optional<Card> cardOptional = cardRepository.findById(todoItem.getRelatedCard().getId());
            if (!cardOptional.isPresent()) {
                throw new IllegalArgumentException("Card not found: " + todoItem.getRelatedCard().getId());
            }
            todoItem.setRelatedCard(cardOptional.get());
        }
        
        // Validate review session exists if provided
        if (todoItem.getRelatedSession() != null && todoItem.getRelatedSession().getId() != null) {
            Optional<ReviewSession> sessionOptional = reviewSessionRepository.findById(todoItem.getRelatedSession().getId());
            if (!sessionOptional.isPresent()) {
                throw new IllegalArgumentException("Review session not found: " + todoItem.getRelatedSession().getId());
            }
            todoItem.setRelatedSession(sessionOptional.get());
        }
        
        return todoItemRepository.save(todoItem);
    }

    /**
     * Example: Deleting a User with explicit cleanup of associated entities
     * (replaces CascadeType.ALL)
     */
    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        User user = userOptional.get();

        // Explicitly delete associated todo items
        List<TodoItem> todoItems = todoItemRepository.findByUserId(userId);
        todoItemRepository.deleteAll(todoItems);

        // Explicitly delete associated review sessions and their records
        List<ReviewSession> reviewSessions = reviewSessionRepository.findByUserId(userId);
        for (ReviewSession session : reviewSessions) {
            List<ReviewRecord> reviewRecords = reviewRecordRepository.findBySessionId(session.getId());
            reviewRecordRepository.deleteAll(reviewRecords);
        }
        reviewSessionRepository.deleteAll(reviewSessions);

        // Explicitly delete associated cards
        List<Card> cards = cardRepository.findByUserId(userId);
        cardRepository.deleteAll(cards);

        // Explicitly delete associated study materials
        List<StudyMaterial> materials = studyMaterialRepository.findByUserId(userId);
        studyMaterialRepository.deleteAll(materials);

        // Now delete the user
        userRepository.delete(user);
    }

    /**
     * Example: Deleting a StudyMaterial with explicit cleanup of associated cards
     * (replaces CascadeType.ALL)
     */
    @Transactional
    public void deleteStudyMaterial(Long materialId) {
        Optional<StudyMaterial> materialOptional = studyMaterialRepository.findById(materialId);
        if (!materialOptional.isPresent()) {
            throw new IllegalArgumentException("Study material not found: " + materialId);
        }

        StudyMaterial material = materialOptional.get();

        // Explicitly delete or reassign associated cards
        List<Card> cards = cardRepository.findByMaterialId(materialId);
        // Option 1: Delete cards
        cardRepository.deleteAll(cards);
        
        // Option 2: Reassign cards to a different material or set to null
        // for (Card card : cards) {
        //     card.setMaterial(null);
        //     cardRepository.save(card);
        // }

        // Now delete the study material
        studyMaterialRepository.delete(material);
    }

    /**
     * Example: Creating a ReviewSession with validation
     */
    @Transactional
    public ReviewSession createReviewSession(ReviewSession session) {
        // Validate user exists
        if (session.getUser() != null && session.getUser().getId() != null) {
            Optional<User> userOptional = userRepository.findById(session.getUser().getId());
            if (!userOptional.isPresent()) {
                throw new IllegalArgumentException("User not found: " + session.getUser().getId());
            }
            session.setUser(userOptional.get());
        }

        // Validate selected card IDs if provided
        if (session.getSelectedCardIds() != null && !session.getSelectedCardIds().isEmpty()) {
            for (Long cardId : session.getSelectedCardIds()) {
                Optional<Card> cardOptional = cardRepository.findById(cardId);
                if (!cardOptional.isPresent()) {
                    throw new IllegalArgumentException("Card not found: " + cardId);
                }
            }
        }

        session.setStartTime(LocalDateTime.now());
        return reviewSessionRepository.save(session);
    }
}
