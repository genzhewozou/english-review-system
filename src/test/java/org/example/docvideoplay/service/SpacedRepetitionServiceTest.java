package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.service.impl.SpacedRepetitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SpacedRepetitionServiceTest {
    
    private SpacedRepetitionService spacedRepetitionService;
    private Card card;
    
    @BeforeEach
    void setUp() {
        spacedRepetitionService = new SpacedRepetitionServiceImpl();
        
        // Create a test card
        StudyMaterial material = new StudyMaterial();
        material.setId(1L);
        
        card = new Card(material.getId(), "test word", "test back", "test context", 0, 9);
        card.setId(1L);
    }
    
    @Test
    void testScheduleInitialReminder() {
        // When scheduling initial reminder
        spacedRepetitionService.scheduleInitialReminder(card);
        
        // Then initial parameters should be set correctly
        assertEquals(2.5, card.getEaseFactor());
        assertEquals(0, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(5), card.getNextReviewDate());
        assertNull(card.getLastReviewDate());
    }
    
    @Test
    void testProcessReviewAnswerFirstCorrect() {
        // Given a card with initial parameters
        spacedRepetitionService.scheduleInitialReminder(card);
        
        // When processing a correct answer on first review
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.CORRECT);
        
        // Then repetition count should increase and next review should be scheduled
        assertEquals(1, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays());
        assertEquals(LocalDate.now(), card.getLastReviewDate());
        assertNotNull(card.getNextReviewDate());
    }
    
    @Test
    void testProcessReviewAnswerFirstIncorrect() {
        // Given a card with initial parameters
        spacedRepetitionService.scheduleInitialReminder(card);
        
        // When processing an incorrect answer on first review
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.INCORRECT);
        
        // Then repetition count should reset and review should be today
        assertEquals(0, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays());
        assertEquals(LocalDate.now(), card.getLastReviewDate());
        assertEquals(LocalDate.now(), card.getNextReviewDate());
    }
    
    @Test
    void testProcessReviewAnswerSecondCorrect() {
        // Given a card that has been reviewed once correctly
        spacedRepetitionService.scheduleInitialReminder(card);
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.CORRECT);
        
        // When processing a correct answer on second review
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.CORRECT);
        
        // Then repetition count should be 2 and interval should be 6 days
        assertEquals(2, card.getRepetitionCount());
        assertEquals(6, card.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(6), card.getNextReviewDate());
    }
    
    @Test
    void testEaseFactorCalculation() {
        // Given a card with initial parameters
        spacedRepetitionService.scheduleInitialReminder(card);
        double initialEaseFactor = card.getEaseFactor();
        
        // When processing a perfect answer
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.PERFECT);
        
        // Then ease factor should increase (perfect answer improves ease)
        assertTrue(card.getEaseFactor() >= initialEaseFactor);
        
        // When processing a blackout answer
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.BLACKOUT);
        
        // Then ease factor should decrease but not go below minimum
        assertTrue(card.getEaseFactor() >= 1.3);
    }
    
    @Test
    void testResetSpacedRepetitionData() {
        // Given a card with some review history
        spacedRepetitionService.scheduleInitialReminder(card);
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.CORRECT);
        spacedRepetitionService.processReviewAnswer(card, AnswerQuality.CORRECT);
        
        // When resetting spaced repetition data
        spacedRepetitionService.resetSpacedRepetitionData(card);
        
        // Then all parameters should be reset to initial values
        assertEquals(2.5, card.getEaseFactor());
        assertEquals(0, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(5), card.getNextReviewDate());
        assertNull(card.getLastReviewDate());
    }
    
    @Test
    void testCalculateNextReviewDateProgression() {
        // Test the progression of review dates through multiple correct answers
        spacedRepetitionService.scheduleInitialReminder(card);
        
        // First review - correct
        LocalDate firstReview = spacedRepetitionService.calculateNextReviewDate(card, AnswerQuality.CORRECT);
        assertEquals(LocalDate.now().plusDays(1), firstReview);
        
        // Simulate first review completion
        card.setRepetitionCount(1);
        card.setIntervalDays(1);
        
        // Second review - correct
        LocalDate secondReview = spacedRepetitionService.calculateNextReviewDate(card, AnswerQuality.CORRECT);
        assertEquals(LocalDate.now().plusDays(6), secondReview);
        
        // Simulate second review completion
        card.setRepetitionCount(2);
        card.setIntervalDays(6);
        
        // Third review - correct (should use ease factor)
        LocalDate thirdReview = spacedRepetitionService.calculateNextReviewDate(card, AnswerQuality.CORRECT);
        int expectedInterval = (int) Math.round(6 * card.getEaseFactor());
        assertEquals(LocalDate.now().plusDays(expectedInterval), thirdReview);
    }
}