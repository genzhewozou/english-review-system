package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Highlight;
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
     * Create a new review session with highlights due for review.
     * Automatically includes highlights that are due today or overdue.
     * 
     * @return The created ReviewSession entity
     */
    ReviewSession createReviewSession();
    
    /**
     * Create a review session with specific highlights.
     * 
     * @param highlightIds List of highlight IDs to include in the session
     * @return The created ReviewSession entity
     * @throws IllegalArgumentException if any highlight ID is invalid
     */
    ReviewSession createReviewSessionWithHighlights(List<Long> highlightIds);
    
    /**
     * Get the next question (highlight) for a review session.
     * Returns the next unreviewed highlight in the session.
     * 
     * @param sessionId The ID of the review session
     * @return The next Highlight to review, or null if session is complete
     * @throws IllegalArgumentException if session not found or already completed
     */
    Highlight getNextQuestion(Long sessionId);
    
    /**
     * Get all questions (highlights) for a review session.
     * 
     * @param sessionId The ID of the review session
     * @return List of highlights in the session
     * @throws IllegalArgumentException if session not found
     */
    List<Highlight> getSessionQuestions(Long sessionId);
    
    /**
     * Submit an answer for a highlight in a review session.
     * Records the answer quality and updates spaced repetition parameters.
     * 
     * @param sessionId The ID of the review session
     * @param highlightId The ID of the highlight being reviewed
     * @param quality The quality of the user's answer
     * @param responseTimeSeconds The time taken to answer (optional)
     * @return The created ReviewRecord
     * @throws IllegalArgumentException if session or highlight not found
     */
    ReviewRecord submitAnswer(Long sessionId, Long highlightId, AnswerQuality quality, Integer responseTimeSeconds);
    
    /**
     * Submit an answer without response time tracking.
     * 
     * @param sessionId The ID of the review session
     * @param highlightId The ID of the highlight being reviewed
     * @param quality The quality of the user's answer
     * @return The created ReviewRecord
     * @throws IllegalArgumentException if session or highlight not found
     */
    ReviewRecord submitAnswer(Long sessionId, Long highlightId, AnswerQuality quality);
    
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
     * Get review history for a specific highlight.
     * 
     * @param highlightId The ID of the highlight
     * @return List of review records for the highlight ordered by review time
     */
    List<ReviewRecord> getHighlightReviewHistory(Long highlightId);
    
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
     * Get highlights due for review today.
     * 
     * @return List of highlights due for review today
     */
    List<Highlight> getHighlightsDueToday();
    
    /**
     * Get overdue highlights.
     * 
     * @return List of overdue highlights
     */
    List<Highlight> getOverdueHighlights();
    
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
}