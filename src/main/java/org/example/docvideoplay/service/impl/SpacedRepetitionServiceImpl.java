package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Implementation of the SpacedRepetitionService using the SM-2 algorithm.
 * 
 * The SM-2 algorithm is a spaced repetition algorithm developed by Piotr Wozniak.
 * It calculates optimal intervals between reviews based on the difficulty of recall.
 */
@Service
public class SpacedRepetitionServiceImpl implements SpacedRepetitionService {
    
    // SM-2 algorithm constants
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double DEFAULT_EASE_FACTOR = 2.5;
    private static final int INITIAL_INTERVAL_DAYS = 1;
    private static final int SECOND_INTERVAL_DAYS = 6;
    private static final int INITIAL_REMINDER_DAYS = 5;
    
    @Override
    public LocalDate calculateNextReviewDate(Highlight highlight, AnswerQuality quality) {
        LocalDate today = LocalDate.now();
        
        // If this is the first review (repetitionCount = 0), handle specially
        if (highlight.getRepetitionCount() == 0) {
            if (quality.getValue() >= 3) {
                // Correct answer on first review
                return today.plusDays(INITIAL_INTERVAL_DAYS);
            } else {
                // Incorrect answer on first review - review again today
                return today;
            }
        }
        
        // If this is the second review (repetitionCount = 1), handle specially
        if (highlight.getRepetitionCount() == 1) {
            if (quality.getValue() >= 3) {
                // Correct answer on second review
                return today.plusDays(SECOND_INTERVAL_DAYS);
            } else {
                // Incorrect answer on second review - reset to first interval
                return today.plusDays(INITIAL_INTERVAL_DAYS);
            }
        }
        
        // For subsequent reviews (repetitionCount >= 2), use SM-2 algorithm
        if (quality.getValue() >= 3) {
            // Correct answer - calculate next interval using ease factor
            int newInterval = (int) Math.round(highlight.getIntervalDays() * highlight.getEaseFactor());
            return today.plusDays(newInterval);
        } else {
            // Incorrect answer - reset to first interval
            return today.plusDays(INITIAL_INTERVAL_DAYS);
        }
    }
    
    @Override
    public void processReviewAnswer(Highlight highlight, AnswerQuality quality) {
        LocalDate today = LocalDate.now();
        int qualityValue = quality.getValue();
        
        // Update last review date
        highlight.setLastReviewDate(today);
        
        // Calculate new ease factor using SM-2 formula
        double currentEaseFactor = highlight.getEaseFactor();
        double newEaseFactor = currentEaseFactor + (0.1 - (5 - qualityValue) * (0.08 + (5 - qualityValue) * 0.02));
        
        // Ensure ease factor doesn't go below minimum
        if (newEaseFactor < MIN_EASE_FACTOR) {
            newEaseFactor = MIN_EASE_FACTOR;
        }
        
        highlight.setEaseFactor(newEaseFactor);
        
        // Update repetition count and interval based on answer quality
        if (qualityValue >= 3) {
            // Correct answer - increment repetition count first
            int newRepetitionCount = highlight.getRepetitionCount() + 1;
            highlight.setRepetitionCount(newRepetitionCount);
            
            // Calculate new interval based on the NEW repetition count
            int newInterval;
            if (newRepetitionCount == 1) {
                // First correct answer - use initial interval (1 day)
                newInterval = INITIAL_INTERVAL_DAYS;
            } else if (newRepetitionCount == 2) {
                newInterval = SECOND_INTERVAL_DAYS;
            } else {
                newInterval = (int) Math.round(highlight.getIntervalDays() * highlight.getEaseFactor());
            }
            
            highlight.setIntervalDays(newInterval);
            highlight.setNextReviewDate(today.plusDays(newInterval));
        } else {
            // Incorrect answer - reset repetition count and interval
            highlight.setRepetitionCount(0);
            highlight.setIntervalDays(INITIAL_INTERVAL_DAYS);
            highlight.setNextReviewDate(today); // Review again today
        }
    }
    
    @Override
    public void scheduleInitialReminder(Highlight highlight) {
        LocalDate today = LocalDate.now();
        LocalDate initialReminderDate = today.plusDays(INITIAL_REMINDER_DAYS);
        
        // Set initial spaced repetition parameters
        highlight.setEaseFactor(DEFAULT_EASE_FACTOR);
        highlight.setRepetitionCount(0);
        highlight.setIntervalDays(INITIAL_INTERVAL_DAYS);
        highlight.setNextReviewDate(initialReminderDate);
        highlight.setLastReviewDate(null); // No review has been done yet
    }
    
    @Override
    public void resetSpacedRepetitionData(Highlight highlight) {
        highlight.setEaseFactor(DEFAULT_EASE_FACTOR);
        highlight.setRepetitionCount(0);
        highlight.setIntervalDays(INITIAL_INTERVAL_DAYS);
        highlight.setLastReviewDate(null);
        
        // Schedule new initial reminder
        LocalDate today = LocalDate.now();
        highlight.setNextReviewDate(today.plusDays(INITIAL_REMINDER_DAYS));
    }
}