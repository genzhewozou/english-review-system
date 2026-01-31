package org.example.docvideoplay.integration;

import org.example.docvideoplay.service.*;
import org.example.docvideoplay.entity.*;
import org.example.docvideoplay.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration test for core workflow validation
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SimpleWorkflowTest {

    @Autowired
    private StudyMaterialService materialService;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TodoService todoService;

    @Test
    void testBasicWorkflow() throws Exception {
        // Step 1: Upload material
        MockMultipartFile file = new MockMultipartFile(
                "test.pdf", 
                "test.pdf", 
                "application/pdf", 
                "Test content".getBytes()
        );
        
        StudyMaterial material = materialService.uploadMaterial(file, "Test Material", MaterialType.DOCUMENT);
        assertNotNull(material);
        assertEquals("Test Material", material.getTitle());

        // Step 2: Create highlight
        Highlight highlight = vocabularyService.createHighlight(
                material.getId(),
                "test word",
                "This is a test word in context.",
                0,
                9
        );
        assertNotNull(highlight);
        assertEquals("test word", highlight.getText());
        assertEquals(material.getId(), highlight.getMaterial().getId());

        // Step 3: Verify spaced repetition initialization
        assertEquals(2.5, highlight.getEaseFactor());
        assertEquals(0, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays());
        assertNotNull(highlight.getNextReviewDate());

        // Step 4: Start review session with specific highlight
        ReviewSession session = reviewService.createReviewSessionWithHighlights(Arrays.asList(highlight.getId()));
        assertNotNull(session);
        assertTrue(session.getTotalQuestions() > 0);
        assertFalse(session.getCompleted());

        // Step 5: Get question and submit answer
        Highlight question = reviewService.getNextQuestion(session.getId());
        assertNotNull(question);
        assertEquals(highlight.getId(), question.getId());

        reviewService.submitAnswer(session.getId(), highlight.getId(), AnswerQuality.CORRECT, 5);

        // Step 6: Complete session
        ReviewSession completedSession = reviewService.completeSession(session.getId());
        assertTrue(completedSession.getCompleted());
        assertEquals(1, completedSession.getCorrectAnswers());

        // Step 7: Verify spaced repetition update
        Highlight updatedHighlight = vocabularyService.getHighlightById(highlight.getId());
        assertEquals(1, updatedHighlight.getRepetitionCount());
        assertEquals(1, updatedHighlight.getIntervalDays()); // First correct answer = 1 day interval
        assertNotNull(updatedHighlight.getLastReviewDate());
    }

    @Test
    void testDataConsistency() throws Exception {
        // Create material
        MockMultipartFile file = new MockMultipartFile(
                "consistency.pdf", 
                "consistency.pdf", 
                "application/pdf", 
                "Consistency test content".getBytes()
        );
        
        StudyMaterial material = materialService.uploadMaterial(file, "Consistency Test", MaterialType.DOCUMENT);

        // Create multiple highlights
        Highlight h1 = vocabularyService.createHighlight(material.getId(), "word1", "context1", 0, 5);
        Highlight h2 = vocabularyService.createHighlight(material.getId(), "word2", "context2", 10, 15);

        // Verify material has correct highlight count
        StudyMaterial updatedMaterial = materialService.getMaterialById(material.getId());
        long highlightCount = vocabularyService.getHighlightCountByMaterial(material.getId());
        assertEquals(2, highlightCount);

        // Start review and verify all highlights are included
        ReviewSession session = reviewService.createReviewSessionWithHighlights(Arrays.asList(h1.getId(), h2.getId()));
        assertEquals(2, session.getTotalQuestions());

        // Answer both questions
        reviewService.submitAnswer(session.getId(), h1.getId(), AnswerQuality.CORRECT, 3);
        reviewService.submitAnswer(session.getId(), h2.getId(), AnswerQuality.PERFECT, 2);

        // Complete session
        ReviewSession completed = reviewService.completeSession(session.getId());
        assertEquals(2, completed.getCorrectAnswers());
        assertEquals(100, completed.getAccuracyPercentage());
    }

    @Test
    void testErrorHandling() throws Exception {
        // Test starting review with no highlights
        ReviewSession emptySession = reviewService.createReviewSession();
        assertNotNull(emptySession); // Should create empty session when no highlights available
        assertEquals(0, emptySession.getTotalQuestions()); // Should have 0 questions

        // Create material and highlight
        MockMultipartFile file = new MockMultipartFile(
                "error.pdf", 
                "error.pdf", 
                "application/pdf", 
                "Error test content".getBytes()
        );
        
        StudyMaterial material = materialService.uploadMaterial(file, "Error Test", MaterialType.DOCUMENT);
        Highlight highlight = vocabularyService.createHighlight(material.getId(), "error word", "error context", 0, 10);

        // Start session with specific highlight
        ReviewSession session = reviewService.createReviewSessionWithHighlights(Arrays.asList(highlight.getId()));
        assertNotNull(session);

        // Test invalid answer submission
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.submitAnswer(session.getId(), 99999L, AnswerQuality.CORRECT, 5);
        });

        // Test completing session without answering all questions
        assertThrows(IllegalStateException.class, () -> {
            reviewService.completeSession(session.getId());
        });
    }
}