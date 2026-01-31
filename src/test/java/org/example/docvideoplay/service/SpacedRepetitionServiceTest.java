package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.service.impl.SpacedRepetitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SpacedRepetitionServiceTest {
    
    private SpacedRepetitionService spacedRepetitionService;
    private Highlight highlight;
    
    @BeforeEach
    void setUp() {
        spacedRepetitionService = new SpacedRepetitionServiceImpl();
        
        // Create a test highlight
        StudyMaterial material = new StudyMaterial();
        material.setId(1L);
        
        highlight = new Highlight(material, "test word", "test context", 0, 9);
        highlight.setId(1L);
    }
    
    @Test
    void testScheduleInitialReminder() {
        // When scheduling initial reminder
        spacedRepetitionService.scheduleInitialReminder(highlight);
        
        // Then initial parameters should be set correctly
        assertEquals(2.5, highlight.getEaseFactor());
        assertEquals(0, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(5), highlight.getNextReviewDate());
        assertNull(highlight.getLastReviewDate());
    }
    
    @Test
    void testProcessReviewAnswerFirstCorrect() {
        // Given a highlight with initial parameters
        spacedRepetitionService.scheduleInitialReminder(highlight);
        
        // When processing a correct answer on first review
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.CORRECT);
        
        // Then repetition count should increase and next review should be scheduled
        assertEquals(1, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays());
        assertEquals(LocalDate.now(), highlight.getLastReviewDate());
        assertNotNull(highlight.getNextReviewDate());
    }
    
    @Test
    void testProcessReviewAnswerFirstIncorrect() {
        // Given a highlight with initial parameters
        spacedRepetitionService.scheduleInitialReminder(highlight);
        
        // When processing an incorrect answer on first review
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.INCORRECT);
        
        // Then repetition count should reset and review should be today
        assertEquals(0, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays());
        assertEquals(LocalDate.now(), highlight.getLastReviewDate());
        assertEquals(LocalDate.now(), highlight.getNextReviewDate());
    }
    
    @Test
    void testProcessReviewAnswerSecondCorrect() {
        // Given a highlight that has been reviewed once correctly
        spacedRepetitionService.scheduleInitialReminder(highlight);
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.CORRECT);
        
        // When processing a correct answer on second review
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.CORRECT);
        
        // Then repetition count should be 2 and interval should be 6 days
        assertEquals(2, highlight.getRepetitionCount());
        assertEquals(6, highlight.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(6), highlight.getNextReviewDate());
    }
    
    @Test
    void testEaseFactorCalculation() {
        // Given a highlight with initial parameters
        spacedRepetitionService.scheduleInitialReminder(highlight);
        double initialEaseFactor = highlight.getEaseFactor();
        
        // When processing a perfect answer
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.PERFECT);
        
        // Then ease factor should increase (perfect answer improves ease)
        assertTrue(highlight.getEaseFactor() >= initialEaseFactor);
        
        // When processing a blackout answer
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.BLACKOUT);
        
        // Then ease factor should decrease but not go below minimum
        assertTrue(highlight.getEaseFactor() >= 1.3);
    }
    
    @Test
    void testResetSpacedRepetitionData() {
        // Given a highlight with some review history
        spacedRepetitionService.scheduleInitialReminder(highlight);
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.CORRECT);
        spacedRepetitionService.processReviewAnswer(highlight, AnswerQuality.CORRECT);
        
        // When resetting spaced repetition data
        spacedRepetitionService.resetSpacedRepetitionData(highlight);
        
        // Then all parameters should be reset to initial values
        assertEquals(2.5, highlight.getEaseFactor());
        assertEquals(0, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(5), highlight.getNextReviewDate());
        assertNull(highlight.getLastReviewDate());
    }
    
    @Test
    void testCalculateNextReviewDateProgression() {
        // Test the progression of review dates through multiple correct answers
        spacedRepetitionService.scheduleInitialReminder(highlight);
        
        // First review - correct
        LocalDate firstReview = spacedRepetitionService.calculateNextReviewDate(highlight, AnswerQuality.CORRECT);
        assertEquals(LocalDate.now().plusDays(1), firstReview);
        
        // Simulate first review completion
        highlight.setRepetitionCount(1);
        highlight.setIntervalDays(1);
        
        // Second review - correct
        LocalDate secondReview = spacedRepetitionService.calculateNextReviewDate(highlight, AnswerQuality.CORRECT);
        assertEquals(LocalDate.now().plusDays(6), secondReview);
        
        // Simulate second review completion
        highlight.setRepetitionCount(2);
        highlight.setIntervalDays(6);
        
        // Third review - correct (should use ease factor)
        LocalDate thirdReview = spacedRepetitionService.calculateNextReviewDate(highlight, AnswerQuality.CORRECT);
        int expectedInterval = (int) Math.round(6 * highlight.getEaseFactor());
        assertEquals(LocalDate.now().plusDays(expectedInterval), thirdReview);
    }
}