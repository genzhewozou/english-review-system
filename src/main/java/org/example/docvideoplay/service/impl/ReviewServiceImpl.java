package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.dao.jpa.HighlightRepository;
import org.example.docvideoplay.dao.jpa.ReviewRecordRepository;
import org.example.docvideoplay.dao.jpa.ReviewSessionRepository;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.ReviewRecord;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.service.ReviewService;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.example.docvideoplay.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final HighlightRepository highlightRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final TodoService todoService;
    
    @Autowired
    public ReviewServiceImpl(ReviewSessionRepository reviewSessionRepository,
                           ReviewRecordRepository reviewRecordRepository,
                           HighlightRepository highlightRepository,
                           SpacedRepetitionService spacedRepetitionService,
                           TodoService todoService) {
        this.reviewSessionRepository = reviewSessionRepository;
        this.reviewRecordRepository = reviewRecordRepository;
        this.highlightRepository = highlightRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.todoService = todoService;
    }
    
    @Override
    public ReviewSession createReviewSession() {
        // Get highlights due for review (today and overdue)
        List<Highlight> dueHighlights = highlightRepository.findHighlightsDueToday();
        List<Highlight> overdueHighlights = highlightRepository.findOverdueHighlights();
        
        // Combine and deduplicate highlights
        List<Highlight> allDueHighlights = new ArrayList<>(overdueHighlights);
        for (Highlight highlight : dueHighlights) {
            if (!allDueHighlights.contains(highlight)) {
                allDueHighlights.add(highlight);
            }
        }
        
        // If no due highlights, get all available highlights for testing/demo purposes
        if (allDueHighlights.isEmpty()) {
            List<Highlight> allHighlights = highlightRepository.findAll();
            if (!allHighlights.isEmpty()) {
                logger.info("No highlights due for review, using all available highlights for session");
                allDueHighlights = allHighlights;
            } else {
                logger.info("No highlights available at all, creating empty session");
            }
        }
        
        // Create new review session
        ReviewSession session = new ReviewSession();
        session.setTotalQuestions(allDueHighlights.size());
        // Persist the specific highlights that belong to this session
        session.setSelectedHighlightIds(
                allDueHighlights.stream()
                        .map(Highlight::getId)
                        .collect(Collectors.toList())
        );
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} containing {} highlights", 
                   savedSession.getId(), allDueHighlights.size());
        
        return savedSession;
    }
    
    @Override
    public ReviewSession createReviewSessionWithHighlights(List<Long> highlightIds) {
        if (highlightIds == null || highlightIds.isEmpty()) {
            throw new IllegalArgumentException("Highlight IDs list cannot be null or empty");
        }
        
        // Validate that all highlights exist
        List<Highlight> highlights = new ArrayList<>();
        for (Long highlightId : highlightIds) {
            Optional<Highlight> highlight = highlightRepository.findById(highlightId);
            if (!highlight.isPresent()) {
                throw new IllegalArgumentException("Highlight not found with ID: " + highlightId);
            }
            highlights.add(highlight.get());
        }
        
        // Create new review session bound to these specific highlights
        ReviewSession session = new ReviewSession();
        session.setTotalQuestions(highlights.size());
        session.setSelectedHighlightIds(new ArrayList<>(highlightIds));
        
        ReviewSession savedSession = reviewSessionRepository.save(session);
        logger.info("Created review session with ID: {} containing {} specified highlights", 
                   savedSession.getId(), highlights.size());
        
        return savedSession;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Highlight getNextQuestion(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Session is already completed");
        }
        
        // Get highlights that are due and haven't been reviewed in this session yet
        List<Highlight> dueHighlights = getAvailableHighlightsForSession(sessionId);
        
        if (dueHighlights.isEmpty()) {
            logger.info("No more questions available for session: {}", sessionId);
            return null;
        }
        
        // Return the first available highlight
        Highlight nextQuestion = dueHighlights.get(0);
        logger.debug("Next question for session {}: highlight ID {}", sessionId, nextQuestion.getId());
        
        return nextQuestion;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getSessionQuestions(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        return getAllHighlightsForSession(sessionId);
    }
    
    @Override
    public ReviewRecord submitAnswer(Long sessionId, Long highlightId, AnswerQuality quality, Integer responseTimeSeconds) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        if (highlightId == null) {
            throw new IllegalArgumentException("Highlight ID cannot be null");
        }
        
        if (quality == null) {
            throw new IllegalArgumentException("Answer quality cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        
        if (session.getCompleted()) {
            throw new IllegalArgumentException("Cannot submit answer to completed session");
        }
        
        Optional<Highlight> highlightOpt = highlightRepository.findById(highlightId);
        if (!highlightOpt.isPresent()) {
            throw new IllegalArgumentException("Highlight not found with ID: " + highlightId);
        }
        
        Highlight highlight = highlightOpt.get();
        
        // Check if this highlight has already been answered in this session
        List<ReviewRecord> existingRecords = reviewRecordRepository.findBySessionIdAndHighlightId(sessionId, highlightId);
        if (!existingRecords.isEmpty()) {
            throw new IllegalArgumentException("Highlight has already been answered in this session");
        }
        
        // Create review record
        ReviewRecord reviewRecord = new ReviewRecord(session, highlight, quality);
        reviewRecord.setResponseTimeSeconds(responseTimeSeconds);
        
        ReviewRecord savedRecord = reviewRecordRepository.save(reviewRecord);
        
        // Update session statistics
        session.addReviewRecord(savedRecord);
        if (savedRecord.isCorrectAnswer()) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }
        reviewSessionRepository.save(session);
        
        // Update spaced repetition parameters
        spacedRepetitionService.processReviewAnswer(highlight, quality);
        highlightRepository.save(highlight);
        
        // Schedule next review reminder (this will create a new todo item for the next review date)
        todoService.scheduleReviewReminder(highlight);
        
        logger.info("Submitted answer for session: {}, highlight: {}, quality: {}", 
                   sessionId, highlightId, quality);
        
        return savedRecord;
    }
    
    @Override
    public ReviewRecord submitAnswer(Long sessionId, Long highlightId, AnswerQuality quality) {
        return submitAnswer(sessionId, highlightId, quality, null);
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
        List<Highlight> availableHighlights = getAvailableHighlightsForSession(sessionId);
        if (!availableHighlights.isEmpty()) {
            throw new IllegalStateException("Cannot complete session: " + availableHighlights.size() + " questions remain unanswered");
        }
        
        // Mark session as completed
        session.completeSession();
        
        ReviewSession completedSession = reviewSessionRepository.save(session);
        logger.info("Completed review session with ID: {}, accuracy: {:.1f}%", 
                   sessionId, completedSession.getAccuracyPercentage());
        
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
    public ReviewSession getSessionWithRecords(Long sessionId) {
        ReviewSession session = getSessionById(sessionId);
        
        // Load review records
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        session.setReviewRecords(records);
        
        return session;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getIncompleteSessions() {
        return reviewSessionRepository.findByCompletedFalseOrderByStartTimeDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getCompletedSessions() {
        return reviewSessionRepository.findByCompletedTrueOrderByStartTimeDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> getSessionsStartedToday() {
        return reviewSessionRepository.findSessionsStartedToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewSession getMostRecentIncompleteSession() {
        Optional<ReviewSession> session = reviewSessionRepository.findFirstByCompletedFalseOrderByStartTimeDesc();
        return session.orElse(null);
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
    public List<ReviewRecord> getHighlightReviewHistory(Long highlightId) {
        if (highlightId == null) {
            throw new IllegalArgumentException("Highlight ID cannot be null");
        }
        
        return reviewRecordRepository.findByHighlightIdOrderByReviewTimeDesc(highlightId);
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
        List<Highlight> availableHighlights = getAvailableHighlightsForSession(sessionId);
        List<ReviewRecord> sessionRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        return sessionRecords.size() >= availableHighlights.size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public int[] getSessionProgress(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        int totalQuestions = Math.max(session.getTotalQuestions(), getAvailableHighlightsForSession(sessionId).size());
        int answeredQuestions = records.size();
        
        return new int[]{answeredQuestions, totalQuestions};
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getHighlightsDueToday() {
        return highlightRepository.findHighlightsDueToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getOverdueHighlights() {
        return highlightRepository.findOverdueHighlights();
    }
    
    @Override
    @Transactional(readOnly = true)
    public SessionStatistics getSessionStatistics(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        
        ReviewSession session = getSessionById(sessionId);
        List<ReviewRecord> records = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        
        int totalQuestions = Math.max(session.getTotalQuestions(), getAvailableHighlightsForSession(sessionId).size());
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
     * Get highlights available for a session (due highlights that haven't been answered yet)
     */
    private List<Highlight> getAvailableHighlightsForSession(Long sessionId) {
        // Get highlights already answered in this session
        List<ReviewRecord> sessionRecords = reviewRecordRepository.findBySessionIdOrderByReviewTimeAsc(sessionId);
        List<Long> answeredHighlightIds = sessionRecords.stream()
                .map(record -> record.getHighlight().getId())
                .collect(Collectors.toList());
        
        // Get session to determine if it was created with specific highlights
        ReviewSession session = getSessionById(sessionId);

        List<Highlight> candidateHighlights;

        // If the session has explicit highlight IDs associated, use only those
        List<Long> selectedHighlightIds = session.getSelectedHighlightIds();
        if (selectedHighlightIds != null && !selectedHighlightIds.isEmpty()) {
            candidateHighlights = highlightRepository.findAllById(selectedHighlightIds);
        } else {
            // Fallback: original behaviour based on due and overdue highlights
            List<Highlight> dueHighlights = highlightRepository.findHighlightsDueToday();
            List<Highlight> overdueHighlights = highlightRepository.findOverdueHighlights();

            // Combine and deduplicate
            candidateHighlights = new ArrayList<>(overdueHighlights);
            for (Highlight highlight : dueHighlights) {
                if (!candidateHighlights.contains(highlight)) {
                    candidateHighlights.add(highlight);
                }
            }

            // If still empty but session expects questions, use all highlights
            if (candidateHighlights.isEmpty() && session.getTotalQuestions() > 0) {
                candidateHighlights = highlightRepository.findAll();
            }
        }

        // Filter out already answered highlights
        List<Highlight> availableHighlights = candidateHighlights.stream()
                .filter(highlight -> !answeredHighlightIds.contains(highlight.getId()))
                .collect(Collectors.toList());

        // Limit to expected number of questions when needed
        if (session.getTotalQuestions() != null && session.getTotalQuestions() > 0 && availableHighlights.size() > session.getTotalQuestions()) {
            return availableHighlights.subList(0, session.getTotalQuestions());
        }

        return availableHighlights;
    }

    /**
     * Get the full ordered question set for a session (including already answered).
     * If the session is bound to explicit highlight IDs, those are returned in that order.
     */
    private List<Highlight> getAllHighlightsForSession(Long sessionId) {
        ReviewSession session = getSessionById(sessionId);

        List<Long> selectedHighlightIds = session.getSelectedHighlightIds();
        if (selectedHighlightIds != null && !selectedHighlightIds.isEmpty()) {
            // Preserve the original ID order
            List<Highlight> found = highlightRepository.findAllById(selectedHighlightIds);
            java.util.Map<Long, Highlight> byId = found.stream()
                    .collect(java.util.stream.Collectors.toMap(Highlight::getId, h -> h));
            List<Highlight> ordered = new ArrayList<>();
            for (Long id : selectedHighlightIds) {
                Highlight h = byId.get(id);
                if (h != null) {
                    ordered.add(h);
                }
            }
            return ordered;
        }

        // Fallback (legacy sessions): use due/overdue; if none, use all highlights limited by totalQuestions
        List<Highlight> dueHighlights = highlightRepository.findHighlightsDueToday();
        List<Highlight> overdueHighlights = highlightRepository.findOverdueHighlights();

        List<Highlight> allDueHighlights = new ArrayList<>(overdueHighlights);
        for (Highlight highlight : dueHighlights) {
            if (!allDueHighlights.contains(highlight)) {
                allDueHighlights.add(highlight);
            }
        }

        if (allDueHighlights.isEmpty() && session.getTotalQuestions() != null && session.getTotalQuestions() > 0) {
            List<Highlight> allHighlights = highlightRepository.findAll();
            if (allHighlights.size() > session.getTotalQuestions()) {
                return allHighlights.subList(0, session.getTotalQuestions());
            }
            return allHighlights;
        }

        return allDueHighlights;
    }
}