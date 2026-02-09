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

    @Autowired
    private UserService userService;

    @Test
    void testBasicWorkflow() throws Exception {
        // Create test user
        User testUser = userService.findByUsername("testuser")
                .orElseGet(() -> userService.registerUser("testuser", "test@example.com", "password123"));
        
        // Step 1: Upload material
        MockMultipartFile file = new MockMultipartFile(
                "test.pdf", 
                "test.pdf", 
                "application/pdf", 
                "Test content".getBytes()
        );
        
        StudyMaterial material = materialService.uploadMaterial(file, "Test Material", MaterialType.DOCUMENT, testUser.getId());
        assertNotNull(material);
        assertEquals("Test Material", material.getTitle());

        // Step 2: Create card
        Card card = vocabularyService.createCardFromHighlight(
                testUser.getId(),
                material.getId(),
                "test word",
                "This is a test word in context.",
                0,
                9
        );
        assertNotNull(card);
        assertEquals("test word", card.getText());
        assertEquals(material.getId(), card.getMaterialId());

        // Step 3: Verify spaced repetition initialization
        assertEquals(2.5, card.getEaseFactor());
        assertEquals(0, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays());
        assertNotNull(card.getNextReviewDate());

        // Step 4: Start review session with specific card
        ReviewSession session = reviewService.createReviewSessionWithCards(Arrays.asList(card.getId()));
        assertNotNull(session);
        assertTrue(session.getTotalQuestions() > 0);
        assertFalse(session.getCompleted());

        // Step 5: Get question and submit answer
        Card question = reviewService.getNextQuestion(session.getId());
        assertNotNull(question);
        assertEquals(card.getId(), question.getId());

        reviewService.submitAnswer(session.getId(), card.getId(), AnswerQuality.CORRECT, 5);

        // Step 6: Complete session
        ReviewSession completedSession = reviewService.completeSession(session.getId());
        assertTrue(completedSession.getCompleted());
        assertEquals(1, completedSession.getCorrectAnswers());

        // Step 7: Verify spaced repetition update
        Card updatedCard = vocabularyService.getCardById(card.getId());
        assertEquals(1, updatedCard.getRepetitionCount());
        assertEquals(1, updatedCard.getIntervalDays()); // First correct answer = 1 day interval
        assertNotNull(updatedCard.getLastReviewDate());
    }

    @Test
    void testDataConsistency() throws Exception {
        // Create test user
        User testUser = userService.findByUsername("testuser")
                .orElseGet(() -> userService.registerUser("testuser", "test@example.com", "password123"));
        
        // Create material
        MockMultipartFile file = new MockMultipartFile(
                "consistency.pdf", 
                "consistency.pdf", 
                "application/pdf", 
                "Consistency test content".getBytes()
        );
        
        StudyMaterial material = materialService.uploadMaterial(file, "Consistency Test", MaterialType.DOCUMENT, testUser.getId());

        // Create multiple cards
        Card h1 = vocabularyService.createCardFromHighlight(testUser.getId(), material.getId(), "word1", "context1", 0, 5);
        Card h2 = vocabularyService.createCardFromHighlight(testUser.getId(), material.getId(), "word2", "context2", 10, 15);

        // Verify material has correct card count
        StudyMaterial updatedMaterial = materialService.getMaterialById(material.getId());
        long cardCount = vocabularyService.getCardCountByMaterial(material.getId());
        assertEquals(2, cardCount);

        // Start review and verify all cards are included
        ReviewSession session = reviewService.createReviewSessionWithCards(Arrays.asList(h1.getId(), h2.getId()));
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
        // Create test user
        User testUser = userService.findByUsername("testuser")
                .orElseGet(() -> userService.registerUser("testuser", "test@example.com", "password123"));
        
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
        
        StudyMaterial material = materialService.uploadMaterial(file, "Error Test", MaterialType.DOCUMENT, testUser.getId());
        Card card = vocabularyService.createCardFromHighlight(testUser.getId(), material.getId(), "error word", "error context", 0, 10);

        // Start session with specific card
        ReviewSession session = reviewService.createReviewSessionWithCards(Arrays.asList(card.getId()));
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