package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.AnswerParamsDto;
import org.example.docvideoplay.dto.api.QuestionResultDto;
import org.example.docvideoplay.dto.api.ReviewSessionResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for review and quiz system operations
 * Handles review session management, question presentation, and answer processing
 */
@RequestMapping("/api/reviews")
public interface ReviewApi {
    
    /**
     * Start a new review session
     * 
     * @return ResponseEntity containing the created review session details
     */
    @PostMapping("/sessions")
    ResponseEntity<ReviewSessionResultDto> startReviewSession();
    
    /**
     * Start a new review session with a specific set of cards.
     *
     * @param cardIds List of card IDs to include in the session
     * @return ResponseEntity containing the created review session details
     */
    @PostMapping("/sessions/custom")
    ResponseEntity<ReviewSessionResultDto> startCustomReviewSession(
            @RequestBody List<Long> cardIds
    );
    
    /**
     * Start a new review session with all cards from a deck.
     *
     * @param deckId The ID of the deck
     * @return ResponseEntity containing the created review session details
     */
    @PostMapping("/sessions/deck/{deckId}")
    ResponseEntity<ReviewSessionResultDto> startDeckReviewSession(
            @PathVariable Long deckId
    );
    
    /**
     * Start a new review session with due cards from a deck.
     *
     * @param deckId The ID of the deck
     * @return ResponseEntity containing the created review session details
     */
    @PostMapping("/sessions/deck/{deckId}/due")
    ResponseEntity<ReviewSessionResultDto> startDeckDueReviewSession(
            @PathVariable Long deckId
    );
    
    /**
     * Get the next question in a review session
     * 
     * @param sessionId The review session ID
     * @return ResponseEntity containing the next question details
     */
    @GetMapping("/sessions/{sessionId}/next-question")
    ResponseEntity<QuestionResultDto> getNextQuestion(@PathVariable Long sessionId);

    /**
     * Get all questions (cards) for a review session in order.
     * This supports prev/next navigation on the client.
     *
     * @param sessionId The review session ID
     * @return ResponseEntity containing ordered list of questions
     */
    @GetMapping("/sessions/{sessionId}/questions")
    ResponseEntity<List<QuestionResultDto>> getSessionQuestions(@PathVariable Long sessionId);
    
    /**
     * Submit an answer for a question in a review session
     * 
     * @param sessionId The review session ID
     * @param answer The answer parameters (cardId, quality, responseTime)
     * @return ResponseEntity with no content
     */
    @PostMapping("/sessions/{sessionId}/answers")
    ResponseEntity<Void> submitAnswer(
            @PathVariable Long sessionId, 
            @Valid @RequestBody AnswerParamsDto answer
    );
    
    /**
     * Complete a review session
     * 
     * @param sessionId The review session ID
     * @return ResponseEntity containing the completed session results
     */
    @PostMapping("/sessions/{sessionId}/complete")
    ResponseEntity<ReviewSessionResultDto> completeSession(@PathVariable Long sessionId);
    
    /**
     * Get a specific review session by ID
     * 
     * @param sessionId The review session ID
     * @return ResponseEntity containing the session details
     */
    @GetMapping("/sessions/{sessionId}")
    ResponseEntity<ReviewSessionResultDto> getReviewSession(@PathVariable Long sessionId);
    
    /**
     * Get all review sessions (with optional filtering)
     * 
     * @param completed Optional filter for completed sessions (true/false)
     * @return ResponseEntity containing list of review sessions
     */
    @GetMapping("/sessions")
    ResponseEntity<List<ReviewSessionResultDto>> getReviewSessions(
            @RequestParam(required = false) Boolean completed
    );
    
    /**
     * Delete a review session by ID
     * 
     * @param sessionId The review session ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/sessions/{sessionId}")
    ResponseEntity<Void> deleteReviewSession(@PathVariable Long sessionId);
}