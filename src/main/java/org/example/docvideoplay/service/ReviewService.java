package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.ReviewRecord;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.enums.AnswerQuality;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing review sessions and quiz functionality.
 * Handles session creation, question generation, answer processing, and session completion.
 */
public interface ReviewService {
    
    /**
     * Create a new review session with cards due for review.
     * Automatically includes cards that are due today or overdue.
     * 
     * @return The created ReviewSession entity
     */
    ReviewSession createReviewSession();
    
    /**
     * Create a review session with specific cards.
     * 
     * @param cardIds List of card IDs to include in the session
     * @return The created ReviewSession entity
     * @throws IllegalArgumentException if any card ID is invalid
     */
    ReviewSession createReviewSessionWithCards(List<Long> cardIds);
    
    /**
     * Get the next question (card) for a review session.
     * Returns the next unreviewed card in the session.
     * 
     * @param sessionId The ID of the review session
     * @return The next Card to review, or null if session is complete
     * @throws IllegalArgumentException if session not found or already completed
     */
    Card getNextQuestion(Long sessionId);
    
    /**
     * Get all questions (cards) for a review session.
     * 
     * @param sessionId The ID of the review session
     * @return List of cards in the session
     * @throws IllegalArgumentException if session not found
     */
    List<Card> getSessionQuestions(Long sessionId);
    
    /**
     * Submit an answer for a card in a review session.
     * Records the answer quality and updates spaced repetition parameters.
     * 
     * @param sessionId The ID of the review session
     * @param cardId The ID of the card being reviewed
     * @param quality The quality of the user's answer
     * @param responseTimeSeconds The time taken to answer (optional)
     * @return The created ReviewRecord
     * @throws IllegalArgumentException if session or card not found
     */
    ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality, Integer responseTimeSeconds);
    
    /**
     * Submit an answer without response time tracking.
     * 
     * @param sessionId The ID of the review session
     * @param cardId The ID of the card being reviewed
     * @param quality The quality of the user's answer
     * @return The created ReviewRecord
     * @throws IllegalArgumentException if session or card not found
     */
    ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality);
    
    /**
     * Complete a review session and calculate final statistics.
     * Marks the session as completed and sets the end time.
     * 
     * @param sessionId The ID of the review session to complete
     * @return The completed ReviewSession with updated statistics
     * @throws IllegalArgumentException if session not found or already completed
     */
    ReviewSession completeSession(Long sessionId);
    
    /**
     * Get a review session by ID.
     * 
     * @param sessionId The ID of the review session
     * @return The ReviewSession entity
     * @throws IllegalArgumentException if session not found
     */
    ReviewSession getSessionById(Long sessionId);
    
    /**
     * Get a review session with review records loaded.
     * 
     * @param sessionId The ID of the review session
     * @return The ReviewSession entity with review records
     * @throws IllegalArgumentException if session not found
     */
    ReviewSession getSessionWithRecords(Long sessionId);
    
    /**
     * Get all incomplete review sessions.
     * 
     * @return List of incomplete review sessions
     */
    List<ReviewSession> getIncompleteSessions();
    
    /**
     * Get all completed review sessions.
     * 
     * @return List of completed review sessions ordered by start time
     */
    List<ReviewSession> getCompletedSessions();
    
    /**
     * Get review sessions started today.
     * 
     * @return List of review sessions started today
     */
    List<ReviewSession> getSessionsStartedToday();
    
    /**
     * Get the most recent incomplete session.
     * 
     * @return The most recent incomplete session, or null if none exists
     */
    ReviewSession getMostRecentIncompleteSession();
    
    /**
     * Get review records for a specific session.
     * 
     * @param sessionId The ID of the review session
     * @return List of review records for the session
     * @throws IllegalArgumentException if session not found
     */
    List<ReviewRecord> getSessionReviewRecords(Long sessionId);
    
    /**
     * Get review history for a specific card.
     * 
     * @param cardId The ID of the card
     * @return List of review records for the card ordered by review time
     */
    List<ReviewRecord> getCardReviewHistory(Long cardId);
    
    /**
     * Check if a session is complete (all questions answered).
     * 
     * @param sessionId The ID of the review session
     * @return true if all questions in the session have been answered
     * @throws IllegalArgumentException if session not found
     */
    boolean isSessionComplete(Long sessionId);
    
    /**
     * Get session progress information.
     * 
     * @param sessionId The ID of the review session
     * @return Array containing [answered_questions, total_questions]
     * @throws IllegalArgumentException if session not found
     */
    int[] getSessionProgress(Long sessionId);
    
    /**
     * Get cards due for review today.
     * 
     * @return List of cards due for review today
     */
    List<Card> getCardsDueToday();
    
    /**
     * Get overdue cards.
     * 
     * @return List of overdue cards
     */
    List<Card> getOverdueCards();
    
    /**
     * Get session statistics.
     * 
     * @param sessionId The ID of the review session
     * @return SessionStatistics object with accuracy, timing, and other metrics
     * @throws IllegalArgumentException if session not found
     */
    SessionStatistics getSessionStatistics(Long sessionId);
    
    /**
     * Inner class for session statistics
     */
    class SessionStatistics {
        private final int totalQuestions;
        private final int answeredQuestions;
        private final int correctAnswers;
        private final double accuracyPercentage;
        private final Double averageResponseTime;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final boolean completed;
        
        public SessionStatistics(int totalQuestions, int answeredQuestions, int correctAnswers, 
                               double accuracyPercentage, Double averageResponseTime,
                               LocalDateTime startTime, LocalDateTime endTime, boolean completed) {
            this.totalQuestions = totalQuestions;
            this.answeredQuestions = answeredQuestions;
            this.correctAnswers = correctAnswers;
            this.accuracyPercentage = accuracyPercentage;
            this.averageResponseTime = averageResponseTime;
            this.startTime = startTime;
            this.endTime = endTime;
            this.completed = completed;
        }
        
        // Getters
        public int getTotalQuestions() { return totalQuestions; }
        public int getAnsweredQuestions() { return answeredQuestions; }
        public int getCorrectAnswers() { return correctAnswers; }
        public double getAccuracyPercentage() { return accuracyPercentage; }
        public Double getAverageResponseTime() { return averageResponseTime; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public boolean isCompleted() { return completed; }
    }
    
    // User-based methods
    
    /**
     * Create a new review session with cards due for review for a specific user.
     * Automatically includes cards that are due today or overdue for the user.
     * 
     * @param userId The ID of the user who owns the cards
     * @return The created ReviewSession entity
     */
    ReviewSession createReviewSession(Long userId);
    
    /**
     * Create a review session with specific cards for a user.
     * 
     * @param cardIds List of card IDs to include in the session
     * @param userId The ID of the user who owns the cards
     * @return The created ReviewSession entity
     * @throws IllegalArgumentException if any card ID is invalid or not owned by the user
     */
    ReviewSession createReviewSessionWithCards(List<Long> cardIds, Long userId);
    
    /**
     * Create a review session for all cards in a deck for a user.
     * 
     * @param deckId The ID of the deck
     * @param userId The ID of the user who owns the deck
     * @return The created ReviewSession entity
     * @throws IllegalArgumentException if deck not found or not owned by the user
     */
    ReviewSession createReviewSessionFromDeck(Long deckId, Long userId);
    
    /**
     * Create a review session for cards in a deck that are due for review for a user.
     * 
     * @param deckId The ID of the deck
     * @param userId The ID of the user who owns the deck
     * @return The created ReviewSession entity
     * @throws IllegalArgumentException if deck not found or not owned by the user
     */
    ReviewSession createReviewSessionFromDeckDueCards(Long deckId, Long userId);
    
    /**
     * Get all incomplete review sessions for a user.
     * 
     * @param userId The ID of the user who owns the sessions
     * @return List of incomplete review sessions
     */
    List<ReviewSession> getIncompleteSessions(Long userId);
    
    /**
     * Get all completed review sessions for a user.
     * 
     * @param userId The ID of the user who owns the sessions
     * @return List of completed review sessions ordered by start time
     */
    List<ReviewSession> getCompletedSessions(Long userId);
    
    /**
     * Get review sessions started today for a user.
     * 
     * @param userId The ID of the user who owns the sessions
     * @return List of review sessions started today
     */
    List<ReviewSession> getSessionsStartedToday(Long userId);
    
    /**
     * Get the most recent incomplete session for a user.
     * 
     * @param userId The ID of the user who owns the sessions
     * @return The most recent incomplete session, or null if none exists
     */
    ReviewSession getMostRecentIncompleteSession(Long userId);
    
    /**
     * Get cards due for review today for a user.
     * 
     * @param userId The ID of the user who owns the cards
     * @return List of cards due for review today
     */
    List<Card> getCardsDueToday(Long userId);
    
    /**
     * Get overdue cards for a user.
     * 
     * @param userId The ID of the user who owns the cards
     * @return List of overdue cards
     */
    List<Card> getOverdueCards(Long userId);
    
    /**
     * Get a review session by ID for a user.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return The ReviewSession entity
     * @throws IllegalArgumentException if session not found or not owned by the user
     */
    ReviewSession getSessionById(Long sessionId, Long userId);
    
    /**
     * Get a review session with review records loaded for a user.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return The ReviewSession entity with review records
     * @throws IllegalArgumentException if session not found or not owned by the user
     */
    ReviewSession getSessionWithRecords(Long sessionId, Long userId);
    
    /**
     * Get review records for a specific session for a user.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return List of review records for the session
     * @throws IllegalArgumentException if session not found or not owned by the user
     */
    List<ReviewRecord> getSessionReviewRecords(Long sessionId, Long userId);
    
    /**
     * Get review history for a specific card for a user.
     * 
     * @param cardId The ID of the card
     * @param userId The ID of the user who owns the card
     * @return List of review records for the card ordered by review time
     * @throws IllegalArgumentException if card not found or not owned by the user
     */
    List<ReviewRecord> getCardReviewHistory(Long cardId, Long userId);
    
    /**
     * Check if a session is complete (all questions answered) for a user.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return true if all questions in the session have been answered
     * @throws IllegalArgumentException if session not found or not owned by the user
     */
    boolean isSessionComplete(Long sessionId, Long userId);
    
    /**
     * Get session progress information for a user.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return Array containing [answered_questions, total_questions]
     * @throws IllegalArgumentException if session not found or not owned by the user
     */
    int[] getSessionProgress(Long sessionId, Long userId);
    
    /**
     * Get the next question (card) for a review session for a user.
     * Returns the next unreviewed card in the session.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return The next Card to review, or null if session is complete
     * @throws IllegalArgumentException if session not found, already completed, or not owned by the user
     */
    Card getNextQuestion(Long sessionId, Long userId);
    
    /**
     * Submit an answer for a card in a review session for a user.
     * Records the answer quality and updates spaced repetition parameters.
     * 
     * @param sessionId The ID of the review session
     * @param cardId The ID of the card being reviewed
     * @param quality The quality of the user's answer
     * @param responseTimeSeconds The time taken to answer (optional)
     * @param userId The ID of the user who owns the session and card
     * @return The created ReviewRecord
     * @throws IllegalArgumentException if session or card not found, or not owned by the user
     */
    ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality, Integer responseTimeSeconds, Long userId);
    
    /**
     * Submit an answer without response time tracking for a user.
     * 
     * @param sessionId The ID of the review session
     * @param cardId The ID of the card being reviewed
     * @param quality The quality of the user's answer
     * @param userId The ID of the user who owns the session and card
     * @return The created ReviewRecord
     * @throws IllegalArgumentException if session or card not found, or not owned by the user
     */
    ReviewRecord submitAnswer(Long sessionId, Long cardId, AnswerQuality quality, Long userId);
    
    /**
     * Complete a review session and calculate final statistics for a user.
     * Marks the session as completed and sets the end time.
     * 
     * @param sessionId The ID of the review session to complete
     * @param userId The ID of the user who owns the session
     * @return The completed ReviewSession with updated statistics
     * @throws IllegalArgumentException if session not found, already completed, or not owned by the user
     */
    ReviewSession completeSession(Long sessionId, Long userId);
    
    /**
     * Get session statistics for a user.
     * 
     * @param sessionId The ID of the review session
     * @param userId The ID of the user who owns the session
     * @return SessionStatistics object with accuracy, timing, and other metrics
     * @throws IllegalArgumentException if session not found or not owned by the user
     */
    SessionStatistics getSessionStatistics(Long sessionId, Long userId);
}