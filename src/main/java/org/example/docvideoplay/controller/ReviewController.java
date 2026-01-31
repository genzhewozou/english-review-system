package org.example.docvideoplay.controller;

import java.util.ArrayList;
import org.example.docvideoplay.api.ReviewApi;
import org.example.docvideoplay.dto.api.AnswerParamsDto;
import org.example.docvideoplay.dto.api.QuestionResultDto;
import org.example.docvideoplay.dto.api.ReviewRecordResultDto;
import org.example.docvideoplay.dto.api.ReviewSessionResultDto;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.ReviewRecord;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for review and quiz system operations.
 * Implements ReviewApi interface for session management, question presentation, and answer processing.
 */
@RestController
public class ReviewController implements ReviewApi {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> startReviewSession() {
        try {
            logger.info("Starting new review session");

            ReviewSession session = reviewService.createReviewSession();

            // For integration tests, we should create a session even if no highlights are due
            // The session will contain all available highlights if no due highlights exist
            ReviewSessionResultDto result = convertToResultDto(session);

            if (session.getTotalQuestions() == 0) {
                logger.info("No highlights available for review");
                return ResponseEntity.noContent().build();
            }

            logger.info("Review session started successfully: id={}, totalQuestions={}",
                    session.getId(), session.getTotalQuestions());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            logger.error("Error starting review session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ReviewSessionResultDto> startCustomReviewSession(List<Long> highlightIds) {
        try {
            logger.info("Starting custom review session with {} highlight(s)",
                    highlightIds != null ? highlightIds.size() : 0);

            ReviewSession session = reviewService.createReviewSessionWithHighlights(highlightIds);
            ReviewSessionResultDto result = convertToResultDto(session);

            if (session.getTotalQuestions() == 0) {
                logger.info("Custom review session has no questions");
                return ResponseEntity.noContent().build();
            }

            logger.info("Custom review session started successfully: id={}, totalQuestions={}",
                    session.getId(), session.getTotalQuestions());
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
    public ResponseEntity<QuestionResultDto> getNextQuestion(Long sessionId) {
        try {
            logger.debug("Getting next question for session: id={}", sessionId);

            Highlight nextHighlight = reviewService.getNextQuestion(sessionId);

            if (nextHighlight == null) {
                logger.debug("No more questions available for session: id={}", sessionId);
                return ResponseEntity.noContent().build();
            }

            // Get session progress
            int[] progress = reviewService.getSessionProgress(sessionId);
            int answeredQuestions = progress[0];
            int totalQuestions = progress[1];

            QuestionResultDto result = new QuestionResultDto(
                    nextHighlight.getId(),
                    nextHighlight.getText(),
                    nextHighlight.getContext(),
                    nextHighlight.getUserComment(),
                    answeredQuestions + 1, // Current question number (1-based)
                    totalQuestions
            );

            logger.debug("Next question retrieved: highlightId={}, questionNumber={}/{}",
                    nextHighlight.getId(), answeredQuestions + 1, totalQuestions);
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
            logger.debug("Retrieving session questions: sessionId={}", sessionId);

            List<Highlight> highlights = reviewService.getSessionQuestions(sessionId);
            if (highlights == null || highlights.isEmpty()) {
                return ResponseEntity.ok().body(new ArrayList<>());
            }

            final int total = highlights.size();
            List<QuestionResultDto> results = new java.util.ArrayList<>(total);
            for (int i = 0; i < total; i++) {
                Highlight h = highlights.get(i);
                results.add(new QuestionResultDto(
                        h.getId(),
                        h.getText(),
                        h.getContext(),
                        h.getUserComment(),
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
            logger.info("Submitting answer for session: sessionId={}, highlightId={}, quality={}",
                    sessionId, answer.getHighlightId(), answer.getQuality());

            reviewService.submitAnswer(
                    sessionId,
                    answer.getHighlightId(),
                    answer.getQuality(),
                    answer.getResponseTimeSeconds()
            );

            logger.info("Answer submitted successfully: sessionId={}, highlightId={}",
                    sessionId, answer.getHighlightId());
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
            logger.info("Completing review session: id={}", sessionId);

            ReviewSession completedSession = reviewService.completeSession(sessionId);
            ReviewSessionResultDto result = convertToResultDto(completedSession);

            logger.info("Review session completed successfully: id={}, accuracy={}%",
                    sessionId, completedSession.getAccuracyPercentage());
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
            logger.debug("Retrieving review session: id={}", sessionId);

            ReviewSession session = reviewService.getSessionWithRecords(sessionId);
            ReviewSessionResultDto result = convertToResultDto(session);

            logger.debug("Retrieved review session: id={}, completed={}", sessionId, session.getCompleted());
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
            logger.debug("Retrieving review sessions: completed={}", completed);

            List<ReviewSession> sessions;
            if (completed != null) {
                if (completed) {
                    sessions = reviewService.getCompletedSessions();
                } else {
                    sessions = reviewService.getIncompleteSessions();
                }
            } else {
                // Get both completed and incomplete sessions
                sessions = reviewService.getCompletedSessions();
                sessions.addAll(reviewService.getIncompleteSessions());
            }

            List<ReviewSessionResultDto> results = sessions.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} review sessions", results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving review sessions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteReviewSession(Long sessionId) {
        try {
            logger.info("Deleting review session: id={}", sessionId);

            // First check if session exists
            reviewService.getSessionById(sessionId);

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

        // Convert review records if loaded
        if (session.getReviewRecords() != null) {
            List<ReviewRecordResultDto> recordDtos = session.getReviewRecords().stream()
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
        dto.setSessionId(record.getSession() != null ? record.getSession().getId() : null);
        dto.setHighlightId(record.getHighlight() != null ? record.getHighlight().getId() : null);

        if (record.getHighlight() != null) {
            dto.setHighlightText(record.getHighlight().getText());
            dto.setHighlightContext(record.getHighlight().getContext());
        }

        dto.setQuality(record.getQuality());
        dto.setReviewTime(record.getReviewTime());
        dto.setResponseTimeSeconds(record.getResponseTimeSeconds());

        return dto;
    }
}