package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.dao.jpa.ReviewRecordRepository;
import org.example.docvideoplay.dao.jpa.ReviewSessionRepository;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.ReviewRecord;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.service.DeckService;
import org.example.docvideoplay.service.ReviewService;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.example.docvideoplay.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ReviewService for managing review sessions and quiz functionality.
 * Integrates with SpacedRepetitionService for automatic scheduling updates.
 */
@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    
    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    private final CardRepository cardRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final TodoService todoService;
    private final DeckService deckService;
    
    @Autowired
    public ReviewServiceImpl(ReviewSessionRepository reviewSessionRepository,
                           ReviewRecordRepository reviewRecordRepository,
                           CardRepository cardRepository,
                           SpacedRepetitionService spacedRepetitionService,
                           TodoService todoService,
                           DeckService deckService) {
        this.reviewSessionRepository = reviewSessionRepository;
        this.reviewRecordRepository = reviewRecordRepository;
        this.cardRepository = cardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.todoService = todoService;
        this.deckService = deckService;
    }
    
    @Override
    public ReviewSession createReviewSession() {
        // Get cards due for review (today and overdue)
        List<Card> dueCards = cardRepository.findCardsDueToday();
        List<Card> overdueCards = cardRepository.findOverdueCards();
        
        // Combine and deduplicate cards
        List<Card> allDueCards = new ArrayList<>(overdueCards);
        for (Card card : dueCards) {
            if (!allDueCards.contains(card)) {
                allDueCards.add(card);
            }
        }
        
        // If no due cards, get all available cards for testing/demo purposes
        if (allDueCards.isEmpty()) {
            List<Card> allCards = cardRepository.findAll();
            if (!allCards.isEmpty()) {
                logger.info("No cards due for review, using all available cards for session");
                allDueCards = allCards;
            } else {
                logger.info("No cards available at all, creating empty session");
            }
        }
        
        // Create new review session
        ReviewSession session = new ReviewSession();
        session.setTotalQuestions(allDueCards.size());
        // Persist the specific cards that belong to this session
        session.setSelectedCardIds(
                allDueCards.stream()
                        .map(Card::getId)
                        .collect(Collectors.toList())
        );
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} containing {} cards", 
                   savedSession.getId(), allDueCards.size());
        
        return savedSession;
    }
    
    @Override
    public ReviewSession createReviewSession(Long userId) {
        // Get cards due for review (today and overdue) for the user
        List<Card> dueCards = cardRepository.findCardsDueTodayByUserId(userId);
        List<Card> overdueCards = cardRepository.findOverdueCardsByUserId(userId);
        
        // Combine and deduplicate cards
        List<Card> allDueCards = new ArrayList<>(overdueCards);
        for (Card card : dueCards) {
            if (!allDueCards.contains(card)) {
                allDueCards.add(card);
            }
        }
        
        // If no due cards, get all available cards for the user for testing/demo purposes
        if (allDueCards.isEmpty()) {
            List<Card> allCards = cardRepository.findByUserId(userId);
            if (!allCards.isEmpty()) {
                logger.info("No cards due for review for user ID {}, using all available cards for session", userId);
                allDueCards = allCards;
            } else {
                logger.info("No cards available at all for user ID {}, creating empty session", userId);
            }
        }
        
        // Create new review session
        ReviewSession session = new ReviewSession(userId);
        session.setTotalQuestions(allDueCards.size());
        // Persist the specific cards that belong to this session
        session.setSelectedCardIds(
                allDueCards.stream()
                        .map(Card::getId)
                        .collect(Collectors.toList())
        );
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} for user ID {} containing {} cards", 
                   savedSession.getId(), userId, allDueCards.size());
        
        return savedSession;
    }
    
    @Override
    public ReviewSession createReviewSessionWithCards(List<Long> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("Card IDs list cannot be null or empty");
        }
        
        // Validate that all cards exist
        List<Card> cards = new ArrayList<>();
        for (Long cardId : cardIds) {
            Optional<Card> card = cardRepository.findById(cardId);
            if (!card.isPresent()) {
                throw new IllegalArgumentException("Card not found with ID: " + cardId);
            }
            cards.add(card.get());
        }
        
        // Create new review session bound to these specific cards
        ReviewSession session = new ReviewSession();
        session.setTotalQuestions(cards.size());
        session.setSelectedCardIds(new ArrayList<>(cardIds));
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} containing {} specified cards", 
                   savedSession.getId(), cards.size());
        
        return savedSession;
    }
    
    @Override
    public ReviewSession createReviewSessionWithCards(List<Long> cardIds, Long userId) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("Card IDs list cannot be null or empty");
        }
        
        // Validate that all cards exist and belong to the user
        List<Card> cards = new ArrayList<>();
        for (Long cardId : cardIds) {
            Optional<Card> card = cardRepository.findById(cardId);
            if (!card.isPresent() || !card.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Card not found with ID: " + cardId);
            }
            cards.add(card.get());
        }
        
        // Create new review session bound to these specific cards
        ReviewSession session = new ReviewSession(userId);
        session.setTotalQuestions(cards.size());
        session.setSelectedCardIds(new ArrayList<>(cardIds));
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} for user ID {} containing {} specified cards", 
                   savedSession.getId(), userId, cards.size());
        
        return savedSession;
    }
    
    @Override
    public ReviewSession createReviewSessionFromDeck(Long deckId, Long userId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        // Get all cards in the deck
        List<Card> deckCards = deckService.getCardsInDeck(deckId, userId);
        
        if (deckCards.isEmpty()) {
            logger.info("No cards found in deck: {}, creating empty session", deckId);
        }
        
        // Extract card IDs
        List<Long> cardIds = deckCards.stream()
                .map(Card::getId)
                .collect(Collectors.toList());
        
        // Create review session with all deck cards
        ReviewSession session = new ReviewSession(userId);
        session.setTotalQuestions(deckCards.size());
        session.setSelectedCardIds(cardIds);
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} for user ID {} containing {} cards from deck {}", 
                   savedSession.getId(), userId, deckCards.size(), deckId);
        
        return savedSession;
    }
    
    @Override
    public ReviewSession createReviewSessionFromDeckDueCards(Long deckId, Long userId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        
        // Get all cards in the deck
        List<Card> deckCards = deckService.getCardsInDeck(deckId, userId);
        
        // Filter only cards that are due for review or overdue
        LocalDateTime today = LocalDateTime.now();
        List<Card> dueCards = deckCards.stream()
                .filter(card -> {
                    LocalDate nextReviewDate = card.getNextReviewDate();
                    return nextReviewDate != null && nextReviewDate.isBefore(LocalDate.now().plusDays(1));
                })
                .collect(Collectors.toList());
        
        if (dueCards.isEmpty()) {
            logger.info("No due cards found in deck: {}, creating empty session", deckId);
        }
        
        // Extract card IDs
        List<Long> cardIds = dueCards.stream()
                .map(Card::getId)
                .collect(Collectors.toList());
        
        // Create review session with due deck cards
        ReviewSession session = new ReviewSession(userId);
        session.setTotalQuestions(dueCards.size());
        session.setSelectedCardIds(cardIds);
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} for user ID {} containing {} due cards from deck {}", 
                   savedSession.getId(), userId, dueCards.size(), deckId);
        
        return savedSession;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Card getNextQuestion(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Session is already completed");
        }
        
        // Get cards that are due and haven't been reviewed in this session yet
        List<Card> dueCards = getAvailableCardsForSession(sessionId);
        
        if (dueCards.isEmpty()) {
            logger.info("No more questions available for session: {}", sessionId);
            return null;
        }
        
        // Return the first available card
        Card nextQuestion = dueCards.get(0);
        logger.debug("Next question for session {}: card ID {}", sessionId, nextQuestion.getId());
        
        return nextQuestion;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Card getNextQuestion(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId, userId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Session is already completed");
        }
        
        // Get cards that are due and haven't been reviewed in this session yet
        List<Card> dueCards = getAvailableCardsForSession(sessionId, userId);
        
        if (dueCards.isEmpty()) {
            logger.info("No more questions available for session: {}", sessionId);
            return null;
        }
        
        // Return the first available card
        Card nextQuestion = dueCards.get(0);
        logger.debug("Next question for session {}: card ID {}", sessionId, nextQuestion.getId());
        
        return nextQuestion;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getSessionQuestions(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        return getAllCardsForSession(sessionId);
    }
    
    @Override
    public ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality, Integer responseTimeSeconds) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        
        if (quality == null) {
            throw new IllegalArgumentException("Answer quality cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Cannot submit answer to completed session");
        }
        
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        Card card = cardOpt.get();
        
        // Check if this card has already been answered in this session
        List<ReviewRecord> existingRecords = reviewRecordRepository.findBySessionIdAndCardId(sessionId, cardId);
        if (!existingRecords.isEmpty()) {
            throw new IllegalArgumentException("Card has already been answered in this session");
        }
        
        // Create review record
        ReviewRecord reviewRecord = new ReviewRecord(sessionId, cardId, quality);
        reviewRecord.setResponseTimeSeconds(responseTimeSeconds);
        
        ReviewRecord savedRecord = reviewRecordRepository.save(reviewRecord);
        
        // Update session statistics
        if (savedRecord.isCorrectAnswer()) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }
        reviewSessionRepository.save(session);
        
        // Update spaced repetition parameters
        spacedRepetitionService.processReviewAnswer(card, quality);
        cardRepository.save(card);
        
        // Schedule next review reminder (this will create a new todo item for the next review date)
        todoService.scheduleReviewReminder(card);
        
        logger.info("Submitted answer for session: {}, card: {}, quality: {}", 
                   sessionId, cardId, quality);
        
        return savedRecord;
    }
    
    @Override
    public ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality) {
        return submitAnswer(sessionId, cardId, quality, (Integer) null);
    }
    
    @Override
    public ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality, Integer responseTimeSeconds, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        
        if (quality == null) {
            throw new IllegalArgumentException("Answer quality cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId, userId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Cannot submit answer to completed session");
        }
        
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        Card card = cardOpt.get();
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        // Check if this card has already been answered in this session
        List<ReviewRecord> existingRecords = reviewRecordRepository.findBySessionIdAndCardId(sessionId, cardId);
        if (!existingRecords.isEmpty()) {
            throw new IllegalArgumentException("Card has already been answered in this session");
        }
        
        // Create review record
        ReviewRecord reviewRecord = new ReviewRecord(sessionId, cardId, quality);
        reviewRecord.setResponseTimeSeconds(responseTimeSeconds);
        
        ReviewRecord savedRecord = reviewRecordRepository.save(reviewRecord);
        
        // Update session statistics
        if (savedRecord.isCorrectAnswer()) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }
        reviewSessionRepository.save(session);
        
        // Update spaced repetition parameters
        spacedRepetitionService.processReviewAnswer(card, quality);
        cardRepository.save(card);
        
        // Schedule next review reminder (this will create a new todo item for the next review date)
        todoService.scheduleReviewReminder(card);
        
        logger.info("Submitted answer for session: {}, card: {}, quality: {}, user ID: {}", 
                   sessionId, cardId, quality, userId);
        
        return savedRecord;
    }
    
    @Override
    public ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality, Long userId) {
        return submitAnswer(sessionId, cardId, quality, null, userId);
    }
    
    @Override
    public ReviewSession completeSession(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Session is already completed");
        }
        
        // Check if all questions have been answered
        List<Card> availableCards = getAvailableCardsForSession(sessionId);
        if (!availableCards.isEmpty()) {
            throw new IllegalStateException("Cannot complete session: " + availableCards.size() + " questions remain unanswered");
        }
        
        // Mark session as completed
        session.completeSession();
        
        ReviewSession completedSession = reviewSessionRepository.save(session);
        logger.info("Completed review session with ID: {}, accuracy: {:.1f}%", 
                   sessionId, completedSession.getAccuracyPercentage());
        
        return completedSession;
    }
    
    @Override
    public ReviewSession completeSession(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId, userId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Session is already completed");
        }
        
        // Check if all questions have been answered
        List<Card> availableCards = getAvailableCardsForSession(sessionId, userId);
        if (!availableCards.isEmpty()) {
            throw new IllegalStateException("Cannot complete session: " + availableCards.size() + " questions remain unanswered");
        }
        
        // Mark session as completed
        session.completeSession();
        
        ReviewSession completedSession = reviewSessionRepository.save(session);
        logger.info("Completed review session with ID: {}, accuracy: {:.1f}% for user ID {}", 
                   sessionId, completedSession.getAccuracyPercentage(), userId);
        
        return completedSession;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getSessionById(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        Optional<ReviewSession> session = reviewSessionRepository.findById(sessionId);
        if (!session.isPresent()) {
            throw new IllegalArgumentException("Review session not found with ID: " + sessionId);
        }
        
        return session.get();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getSessionById(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        Optional<ReviewSession> session = reviewSessionRepository.findById(sessionId);
        if (!session.isPresent()) {
            throw new IllegalArgumentException("Review session not found with ID: " + sessionId);
        }
        
        ReviewSession reviewSession = session.get();
        if (!reviewSession.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Review session not found with ID: " + sessionId);
        }
        
        return reviewSession;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getSessionWithRecords(Long sessionId) {
        ReviewSession session = getSessionById(sessionId);
        
        // Review records are loaded separately
        return session;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getSessionWithRecords(Long sessionId, Long userId) {
        ReviewSession session = getSessionById(sessionId, userId);
        
        // Load review records
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        return session;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getIncompleteSessions() {
        return reviewSessionRepository.findByCompletedFalseOrderByStartTimeDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getIncompleteSessions(Long userId) {
        return reviewSessionRepository.findByUserIdAndCompletedFalseOrderByStartTimeDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getCompletedSessions() {
        return reviewSessionRepository.findByCompletedTrueOrderByStartTimeDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getCompletedSessions(Long userId) {
        return reviewSessionRepository.findByUserIdAndCompletedTrueOrderByStartTimeDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getSessionsStartedToday() {
        return reviewSessionRepository.findSessionsStartedToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getSessionsStartedToday(Long userId) {
        return reviewSessionRepository.findSessionsStartedTodayByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getMostRecentIncompleteSession() {
        Optional<ReviewSession> session = reviewSessionRepository.findFirstByCompletedFalseOrderByStartTimeDesc();
        return session.orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getMostRecentIncompleteSession(Long userId) {
        List<ReviewSession> sessions = reviewSessionRepository.findByUserIdAndCompletedFalseOrderByStartTimeDesc(userId);
        return sessions.isEmpty() ? null : sessions.get(0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecord> getSessionReviewRecords(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        // Verify session exists
        getSessionById(sessionId);
        
        return reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecord> getSessionReviewRecords(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        // Verify session exists and belongs to user
        getSessionById(sessionId, userId);
        
        return reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecord> getCardReviewHistory(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        
        return reviewRecordRepository.findByCardIdOrderByReviewTimeDesc(cardId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecord> getCardReviewHistory(Long cardId, Long userId) {
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        
        // Verify card exists and belongs to user
        Optional<Card> card = cardRepository.findById(cardId);
        if (!card.isPresent() || !card.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card not found with ID: " + cardId);
        }
        
        return reviewRecordRepository.findByCardIdOrderByReviewTimeDesc(cardId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isSessionComplete(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        
        if (session.getCompleted()) {
            return true;
        }
        
        // Check if all available questions have been answered
        List<Card> availableCards = getAvailableCardsForSession(sessionId);
        List<ReviewRecord> sessionRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        return sessionRecords.size() >= availableCards.size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isSessionComplete(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId, userId);
        
        if (session.getCompleted()) {
            return true;
        }
        
        // Check if all available questions have been answered
        List<Card> availableCards = getAvailableCardsForSession(sessionId, userId);
        List<ReviewRecord> sessionRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        return sessionRecords.size() >= availableCards.size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public int[] getSessionProgress(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        int totalQuestions = Math.max(session.getTotalQuestions(), getAvailableCardsForSession(sessionId).size());
        int answeredQuestions = records.size();
        
        return new int[]{answeredQuestions, totalQuestions};
    }
    
    @Override
    @Transactional(readOnly = true)
    public int[] getSessionProgress(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId, userId);
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        int totalQuestions = Math.max(session.getTotalQuestions(), getAvailableCardsForSession(sessionId, userId).size());
        int answeredQuestions = records.size();
        
        return new int[]{answeredQuestions, totalQuestions};
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsDueToday() {
        return cardRepository.findCardsDueToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsDueToday(Long userId) {
        return cardRepository.findCardsDueTodayByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getOverdueCards() {
        return cardRepository.findOverdueCards();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Card> getOverdueCards(Long userId) {
        return cardRepository.findOverdueCardsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SessionStatistics getSessionStatistics(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        int totalQuestions = Math.max(session.getTotalQuestions(), getAvailableCardsForSession(sessionId).size());
        int answeredQuestions = records.size();
        int correctAnswers = (int) records.stream().filter(ReviewRecord::isCorrectAnswer).count();
        
        double accuracyPercentage = answeredQuestions > 0 ? (double) correctAnswers / answeredQuestions * 100.0 : 0.0;
        
        Double averageResponseTime = records.stream()
                .filter(r -> r.getResponseTimeSeconds() != null)
                .mapToInt(ReviewRecord::getResponseTimeSeconds)
                .average()
                .orElse(0.0);
        
        return new SessionStatistics(
                totalQuestions,
                answeredQuestions,
                correctAnswers,
                accuracyPercentage,
                averageResponseTime > 0 ? averageResponseTime : null,
                session.getStartTime(),
                session.getEndTime(),
                session.getCompleted()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public SessionStatistics getSessionStatistics(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId, userId);
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        int totalQuestions = Math.max(session.getTotalQuestions(), getAvailableCardsForSession(sessionId, userId).size());
        int answeredQuestions = records.size();
        int correctAnswers = (int) records.stream().filter(ReviewRecord::isCorrectAnswer).count();
        
        double accuracyPercentage = answeredQuestions > 0 ? (double) correctAnswers / answeredQuestions * 100.0 : 0.0;
        
        Double averageResponseTime = records.stream()
                .filter(r -> r.getResponseTimeSeconds() != null)
                .mapToInt(ReviewRecord::getResponseTimeSeconds)
                .average()
                .orElse(0.0);
        
        return new SessionStatistics(
                totalQuestions,
                answeredQuestions,
                correctAnswers,
                accuracyPercentage,
                averageResponseTime > 0 ? averageResponseTime : null,
                session.getStartTime(),
                session.getEndTime(),
                session.getCompleted()
        );
    }
    
    /**
     * Get cards available for a session (due cards that haven't been answered yet)
     */
    private List<Card> getAvailableCardsForSession(Long sessionId) {
        // Get cards already answered in this session
        List<ReviewRecord> sessionRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        List<Long> answeredCardIds = sessionRecords.stream()
                .map(ReviewRecord::getCardId)
                .collect(Collectors.toList());
        
        // Get session to determine if it was created with specific cards
        ReviewSession session = getSessionById(sessionId);

        List<Card> candidateCards;

        // If the session has explicit card IDs associated, use only those
        List<Long> selectedCardIds = session.getSelectedCardIds();
        if (selectedCardIds != null && !selectedCardIds.isEmpty()) {
            candidateCards = cardRepository.findAllById(selectedCardIds);
        } else {
            // Fallback: original behaviour based on due and overdue cards
            List<Card> dueCards = cardRepository.findCardsDueToday();
            List<Card> overdueCards = cardRepository.findOverdueCards();

            // Combine and deduplicate
            candidateCards = new ArrayList<>(overdueCards);
            for (Card card : dueCards) {
                if (!candidateCards.contains(card)) {
                    candidateCards.add(card);
                }
            }

            // If still empty but session expects questions, use all cards
            if (candidateCards.isEmpty() && session.getTotalQuestions() > 0) {
                candidateCards = cardRepository.findAll();
            }
        }

        // Filter out already answered cards
        List<Card> availableCards = candidateCards.stream()
                .filter(card -> !answeredCardIds.contains(card.getId()))
                .collect(Collectors.toList());

        // Limit to expected number of questions when needed
        if (session.getTotalQuestions() != null && session.getTotalQuestions() > 0 && availableCards.size() > session.getTotalQuestions()) {
            return availableCards.subList(0, session.getTotalQuestions());
        }

        return availableCards;
    }
    
    /**
     * Get cards available for a session for a specific user (due cards that haven't been answered yet)
     */
    private List<Card> getAvailableCardsForSession(Long sessionId, Long userId) {
        // Get cards already answered in this session
        List<ReviewRecord> sessionRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        List<Long> answeredCardIds = sessionRecords.stream()
                .map(ReviewRecord::getCardId)
                .collect(Collectors.toList());
        
        // Get session to determine if it was created with specific cards
        ReviewSession session = getSessionById(sessionId, userId);

        List<Card> candidateCards;

        // If the session has explicit card IDs associated, use only those
        List<Long> selectedCardIds = session.getSelectedCardIds();
        if (selectedCardIds != null && !selectedCardIds.isEmpty()) {
            candidateCards = cardRepository.findAllById(selectedCardIds);
            // Filter to only include cards that belong to the user
            candidateCards = candidateCards.stream()
                    .filter(card -> card.getUserId().equals(userId))
                    .collect(Collectors.toList());
        } else {
            // Fallback: user-specific due and overdue cards
            List<Card> dueCards = cardRepository.findCardsDueTodayByUserId(userId);
            List<Card> overdueCards = cardRepository.findOverdueCardsByUserId(userId);

            // Combine and deduplicate
            candidateCards = new ArrayList<>(overdueCards);
            for (Card card : dueCards) {
                if (!candidateCards.contains(card)) {
                    candidateCards.add(card);
                }
            }

            // If still empty but session expects questions, use all user's cards
            if (candidateCards.isEmpty() && session.getTotalQuestions() > 0) {
                candidateCards = cardRepository.findByUserId(userId);
            }
        }

        // Filter out already answered cards
        List<Card> availableCards = candidateCards.stream()
                .filter(card -> !answeredCardIds.contains(card.getId()))
                .collect(Collectors.toList());

        // Limit to expected number of questions when needed
        if (session.getTotalQuestions() != null && session.getTotalQuestions() > 0 && availableCards.size() > session.getTotalQuestions()) {
            return availableCards.subList(0, session.getTotalQuestions());
        }

        return availableCards;
    }

    /**
     * Get the full ordered question set for a session (including already answered).
     * If the session is bound to explicit card IDs, those are returned in that order.
     */
    private List<Card> getAllCardsForSession(Long sessionId) {
        ReviewSession session = getSessionById(sessionId);

        List<Long> selectedCardIds = session.getSelectedCardIds();
        if (selectedCardIds != null && !selectedCardIds.isEmpty()) {
            // Preserve the original ID order
            List<Card> found = cardRepository.findAllById(selectedCardIds);
            java.util.Map<Long, Card> byId = found.stream()
                    .collect(java.util.stream.Collectors.toMap(Card::getId, c -> c));
            List<Card> ordered = new ArrayList<>();
            for (Long id : selectedCardIds) {
                Card c = byId.get(id);
                if (c != null) {
                    ordered.add(c);
                }
            }
            return ordered;
        }

        // Fallback (legacy sessions): use due/overdue; if none, use all cards limited by totalQuestions
        List<Card> dueCards = cardRepository.findCardsDueToday();
        List<Card> overdueCards = cardRepository.findOverdueCards();

        List<Card> allDueCards = new ArrayList<>(overdueCards);
        for (Card card : dueCards) {
            if (!allDueCards.contains(card)) {
                allDueCards.add(card);
            }
        }

        if (allDueCards.isEmpty() && session.getTotalQuestions() != null && session.getTotalQuestions() > 0) {
            List<Card> allCards = cardRepository.findAll();
            if (allCards.size() > session.getTotalQuestions()) {
                return allCards.subList(0, session.getTotalQuestions());
            }
            return allCards;
        }

        return allDueCards;
    }
}