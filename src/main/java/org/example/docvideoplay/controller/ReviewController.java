package org.example.docvideoplay.controller;

import java.util.ArrayList;
import org.example.docvideoplay.api.ReviewApi;
import org.example.docvideoplay.dto.api.AnswerParamsDto;
import org.example.docvideoplay.dto.api.QuestionResultDto;
import org.example.docvideoplay.dto.api.ReviewRecordResultDto;
import org.example.docvideoplay.dto.api.ReviewSessionResultDto;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.ReviewRecord;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.service.ReviewService;
import org.example.docvideoplay.service.UserService;
import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.dao.jpa.ReviewRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for review and quiz system operations.
 * Implements ReviewApi interface for session management, question presentation, and answer processing.
 */
@RestController
public class ReviewController implements ReviewApi {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final UserService userService;
    private final CardRepository cardRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    
    @Autowired
    public ReviewController(ReviewService reviewService, UserService userService, 
                           CardRepository cardRepository, ReviewRecordRepository reviewRecordRepository) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.cardRepository = cardRepository;
        this.reviewRecordRepository = reviewRecordRepository;
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
    public ResponseEntity<ReviewSessionResultDto> startReviewSession() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Starting new review session for userId: {}", currentUserId);

            ReviewSession session = reviewService.createReviewSession(currentUserId);

            // For integration tests, we should create a session even if no cards are due
            // The session will contain all available cards if no due cards exist
            ReviewSessionResultDto result = convertToResultDto(session);

            if (session.getTotalQuestions() == 0) {
                logger.info("No cards available for review for userId: {}", currentUserId);
                return ResponseEntity.noContent().build();
            }

            logger.info("Review session started successfully: id={}, totalQuestions={}, userId={}",
                    session.getId(), session.getTotalQuestions(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            logger.error("Error starting review session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> startCustomReviewSession(List<Long> cardIds) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Starting custom review session with {} card(s) for userId: {}",
                    cardIds != null ? cardIds.size() : 0, currentUserId);

            ReviewSession session = reviewService.createReviewSessionWithCards(cardIds, currentUserId);
            ReviewSessionResultDto result = convertToResultDto(session);

            if (session.getTotalQuestions() == 0) {
                logger.info("Custom review session has no questions for userId: {}", currentUserId);
                return ResponseEntity.noContent().build();
            }

            logger.info("Custom review session started successfully: id={}, totalQuestions={}, userId={}",
                    session.getId(), session.getTotalQuestions(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Error starting custom review session: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error starting custom review session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> startDeckReviewSession(Long deckId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Starting deck review session for deck: {} for userId: {}", deckId, currentUserId);

            ReviewSession session = reviewService.createReviewSessionFromDeck(deckId, currentUserId);
            ReviewSessionResultDto result = convertToResultDto(session);

            if (session.getTotalQuestions() == 0) {
                logger.info("Deck review session has no questions for userId: {}", currentUserId);
                return ResponseEntity.noContent().build();
            }

            logger.info("Deck review session started successfully: id={}, totalQuestions={}, userId={}",
                    session.getId(), session.getTotalQuestions(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Error starting deck review session: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error starting deck review session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> startDeckDueReviewSession(Long deckId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Starting deck due review session for deck: {} for userId: {}", deckId, currentUserId);

            ReviewSession session = reviewService.createReviewSessionFromDeckDueCards(deckId, currentUserId);
            ReviewSessionResultDto result = convertToResultDto(session);

            if (session.getTotalQuestions() == 0) {
                logger.info("Deck due review session has no questions for userId: {}", currentUserId);
                return ResponseEntity.noContent().build();
            }

            logger.info("Deck due review session started successfully: id={}, totalQuestions={}, userId={}",
                    session.getId(), session.getTotalQuestions(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Error starting deck due review session: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error starting deck due review session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<QuestionResultDto> getNextQuestion(Long sessionId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Getting next question for session: id={}, userId={}", sessionId, currentUserId);

            Card nextCard = reviewService.getNextQuestion(sessionId, currentUserId);

            if (nextCard == null) {
                logger.debug("No more questions available for session: id={}, userId={}", sessionId, currentUserId);
                return ResponseEntity.noContent().build();
            }

            // Get session progress
            int[] progress = reviewService.getSessionProgress(sessionId, currentUserId);
            int answeredQuestions = progress[0];
            int totalQuestions = progress[1];

            QuestionResultDto result = new QuestionResultDto(
                    nextCard.getId(),
                    nextCard.getText(),
                    nextCard.getBackText(),
                    nextCard.getContext(),
                    nextCard.getUserComment(),
                    answeredQuestions + 1, // Current question number (1-based)
                    totalQuestions
            );

            logger.debug("Next question retrieved: cardId={}, questionNumber={}/{}, userId={}",
                    nextCard.getId(), answeredQuestions + 1, totalQuestions, currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Session not found or invalid: id={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting next question for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<QuestionResultDto>> getSessionQuestions(Long sessionId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving session questions: sessionId={}, userId={}", sessionId, currentUserId);

            List<Card> cards = reviewService.getSessionQuestions(sessionId);
            if (cards == null || cards.isEmpty()) {
                return ResponseEntity.ok().body(new ArrayList<>());
            }

            final int total = cards.size();
            List<QuestionResultDto> results = new java.util.ArrayList<>(total);
            for (int i = 0; i < total; i++) {
                Card c = cards.get(i);
                results.add(new QuestionResultDto(
                        c.getId(),
                        c.getText(),
                        c.getBackText(),
                        c.getContext(),
                        c.getUserComment(),
                        i + 1,
                        total
                ));
            }

            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            logger.warn("Session not found or invalid: id={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving session questions {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> submitAnswer(Long sessionId, @Valid AnswerParamsDto answer) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Submitting answer for session: sessionId={}, cardId={}, quality={}, userId={}",
                    sessionId, answer.getCardId(), answer.getQuality(), currentUserId);

            reviewService.submitAnswer(
                    sessionId,
                    answer.getCardId(),
                    answer.getQuality(),
                    answer.getResponseTimeSeconds(),
                    currentUserId
            );

            logger.info("Answer submitted successfully: sessionId={}, cardId={}, userId={}",
                    sessionId, answer.getCardId(), currentUserId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Answer submission validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error submitting answer for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> completeSession(Long sessionId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Completing review session: id={}, userId={}", sessionId, currentUserId);

            ReviewSession completedSession = reviewService.completeSession(sessionId, currentUserId);
            ReviewSessionResultDto result = convertToResultDto(completedSession);

            logger.info("Review session completed successfully: id={}, accuracy={}%, userId={}",
                    sessionId, completedSession.getAccuracyPercentage(), currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Session not found: id={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Session cannot be completed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error completing session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> getReviewSession(Long sessionId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving review session: id={}, userId={}", sessionId, currentUserId);

            ReviewSession session = reviewService.getSessionWithRecords(sessionId, currentUserId);
            ReviewSessionResultDto result = convertToResultDto(session);

            logger.debug("Retrieved review session: id={}, completed={}, userId={}", sessionId, session.getCompleted(), currentUserId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Session not found: id={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<ReviewSessionResultDto>> getReviewSessions(Boolean completed) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving review sessions: completed={}, userId={}", completed, currentUserId);

            List<ReviewSession> sessions;
            if (completed != null) {
                if (completed) {
                    sessions = reviewService.getCompletedSessions(currentUserId);
                } else {
                    sessions = reviewService.getIncompleteSessions(currentUserId);
                }
            } else {
                // Get both completed and incomplete sessions
                sessions = reviewService.getCompletedSessions(currentUserId);
                sessions.addAll(reviewService.getIncompleteSessions(currentUserId));
            }

            List<ReviewSessionResultDto> results = sessions.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} review sessions for userId: {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving review sessions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteReviewSession(Long sessionId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting review session: id={}, userId={}", sessionId, currentUserId);

            // First check if session exists
            reviewService.getSessionById(sessionId, currentUserId);

            // Note: Actual deletion would need to be implemented in the service
            // For now, we'll just return not implemented
            logger.warn("Review session deletion not implemented yet");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (IllegalArgumentException e) {
            logger.warn("Session not found for deletion: id={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert ReviewSession entity to ReviewSessionResultDto
     *
     * @param session The ReviewSession entity
     * @return The converted DTO
     */
    private ReviewSessionResultDto convertToResultDto(ReviewSession session) {
        ReviewSessionResultDto dto = new ReviewSessionResultDto();
        dto.setId(session.getId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setCompleted(session.getCompleted());
        dto.setTotalQuestions(session.getTotalQuestions());
        dto.setCorrectAnswers(session.getCorrectAnswers());
        dto.setAccuracyPercentage(session.getAccuracyPercentage());

        // Get review records from repository
        List<ReviewRecord> reviewRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(session.getId());
        if (reviewRecords != null && !reviewRecords.isEmpty()) {
            List<ReviewRecordResultDto> recordDtos = reviewRecords.stream()
                    .map(this::convertToRecordResultDto)
                    .collect(Collectors.toList());
            dto.setReviewRecords(recordDtos);
        }

        return dto;
    }

    /**
     * Convert ReviewRecord entity to ReviewRecordResultDto
     *
     * @param record The ReviewRecord entity
     * @return The converted DTO
     */
    private ReviewRecordResultDto convertToRecordResultDto(ReviewRecord record) {
        ReviewRecordResultDto dto = new ReviewRecordResultDto();
        dto.setId(record.getId());
        dto.setSessionId(record.getSessionId());
        dto.setCardId(record.getCardId());

        // Get card details from repository
        if (record.getCardId() != null) {
            Optional<Card> cardOpt = cardRepository.findById(record.getCardId());
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();
                dto.setCardText(card.getText());
                dto.setCardContext(card.getContext());
            }
        }

        dto.setQuality(record.getQuality());
        dto.setReviewTime(record.getReviewTime());
        dto.setResponseTimeSeconds(record.getResponseTimeSeconds());

        return dto;
    }
}