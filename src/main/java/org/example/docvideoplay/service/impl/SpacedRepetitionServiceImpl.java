package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;

/**
 * Implementation of the SpacedRepetitionService using Anki's modified SM-2 algorithm.
 * 
 * This implementation includes Anki's enhancements to the SM-2 algorithm:
 * 1. Interval fuzzing to prevent cards from bunching up
 * 2. Learning steps for new cards
 * 3. More granular ease factor adjustments
 * 4. Support for leeches (difficult cards)
 * 5. Better handling of review scheduling
 */
@Service
public class SpacedRepetitionServiceImpl implements SpacedRepetitionService {
    
    // Anki-modified SM-2 algorithm constants
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double DEFAULT_EASE_FACTOR = 2.5;
    private static final int INITIAL_INTERVAL_DAYS = 1;
    private static final int SECOND_INTERVAL_DAYS = 6;
    private static final int INITIAL_REMINDER_DAYS = 5;
    
    // Learning steps (in minutes) for new cards
    private static final int[] LEARNING_STEPS = {1, 10};
    
    // Maximum number of leech warnings before a card is marked as leech
    private static final int MAX_LEECH_WARNINGS = 3;
    
    // Fuzzing percentage for intervals (±10%)
    private static final double FUZZ_PERCENTAGE = 0.1;
    
    private final Random random = new Random();
    
    @Override
    public LocalDate calculateNextReviewDate(Card card, AnswerQuality quality) {
        LocalDate today = LocalDate.now();
        
        // If this is the first review (repetitionCount = 0), handle specially
        if (card.getRepetitionCount() == 0) {
            if (quality.getValue() >= 3) {
                // Correct answer on first review - use first learning step
                // Since we're using LocalDate, return today for short intervals
                return today;
            } else {
                // Incorrect answer on first review - review again today
                return today;
            }
        }
        
        // If this is the second review (repetitionCount = 1), handle specially
        if (card.getRepetitionCount() == 1) {
            if (quality.getValue() >= 3) {
                // Correct answer on second review - use second learning step
                // Since we're using LocalDate, return today for short intervals
                return today;
            } else {
                // Incorrect answer on second review - reset to first learning step
                // Since we're using LocalDate, return today for short intervals
                return today;
            }
        }
        
        // For subsequent reviews (repetitionCount >= 2), use Anki's modified SM-2 algorithm
        if (quality.getValue() >= 3) {
            // Correct answer - calculate next interval using ease factor with fuzzing
            int baseInterval = calculateBaseInterval(card);
            int fuzzedInterval = applyFuzzing(baseInterval);
            return today.plusDays(fuzzedInterval);
        } else {
            // Incorrect answer - reset to learning phase
            card.setLeechWarningCount(card.getLeechWarningCount() + 1);
            
            // Check if card is a leech
            if (card.getLeechWarningCount() >= MAX_LEECH_WARNINGS) {
                card.setLeech(true);
            }
            
            // Since we're using LocalDate, return today for short intervals
            return today;
        }
    }
    
    @Override
    public void processReviewAnswer(Card card, AnswerQuality quality) {
        LocalDate today = LocalDate.now();
        int qualityValue = quality.getValue();
        
        // Update last review date
        card.setLastReviewDate(today);
        
        // Calculate new ease factor using Anki's modified formula
        double currentEaseFactor = card.getEaseFactor();
        double easeAdjustment = calculateEaseAdjustment(qualityValue);
        double newEaseFactor = currentEaseFactor + easeAdjustment;
        
        // Ensure ease factor doesn't go below minimum
        if (newEaseFactor < MIN_EASE_FACTOR) {
            newEaseFactor = MIN_EASE_FACTOR;
        }
        
        card.setEaseFactor(newEaseFactor);
        
        // Update repetition count and interval based on answer quality
        if (qualityValue >= 3) {
            // Correct answer - increment repetition count first
            int newRepetitionCount = card.getRepetitionCount() + 1;
            card.setRepetitionCount(newRepetitionCount);
            
            // Calculate new interval based on the NEW repetition count
            int newInterval;
            if (newRepetitionCount == 1) {
                // First correct answer - use first learning step
                // Since we're using LocalDate, set next review to today
                card.setNextReviewDate(today);
            } else if (newRepetitionCount == 2) {
                // Second correct answer - use second learning step
                // Since we're using LocalDate, set next review to today
                card.setNextReviewDate(today);
            } else if (newRepetitionCount == 3) {
                // Graduating from learning phase to review phase
                newInterval = INITIAL_INTERVAL_DAYS;
                int fuzzedInterval = applyFuzzing(newInterval);
                card.setIntervalDays(fuzzedInterval);
                card.setNextReviewDate(today.plusDays(fuzzedInterval));
            } else {
                // Regular review phase
                newInterval = calculateBaseInterval(card);
                int fuzzedInterval = applyFuzzing(newInterval);
                card.setIntervalDays(fuzzedInterval);
                card.setNextReviewDate(today.plusDays(fuzzedInterval));
            }
        } else {
            // Incorrect answer - handle leech warnings and reset to learning
            card.setLeechWarningCount(card.getLeechWarningCount() + 1);
            
            // Check if card is a leech
            if (card.getLeechWarningCount() >= MAX_LEECH_WARNINGS) {
                card.setLeech(true);
            }
            
            // Reset to learning phase
            card.setRepetitionCount(0);
            card.setIntervalDays(INITIAL_INTERVAL_DAYS);
            // Since we're using LocalDate, set next review to today
            card.setNextReviewDate(today);
        }
    }
    
    @Override
    public void scheduleInitialReminder(Card card) {
        LocalDate today = LocalDate.now();
        
        // Set initial spaced repetition parameters
        card.setEaseFactor(DEFAULT_EASE_FACTOR);
        card.setRepetitionCount(0);
        card.setIntervalDays(INITIAL_INTERVAL_DAYS);
        card.setNextReviewDate(today.plusDays(INITIAL_REMINDER_DAYS));
        card.setLastReviewDate(null); // No review has been done yet
        card.setLeechWarningCount(0);
        card.setLeech(false);
    }
    
    @Override
    public void resetSpacedRepetitionData(Card card) {
        card.setEaseFactor(DEFAULT_EASE_FACTOR);
        card.setRepetitionCount(0);
        card.setIntervalDays(INITIAL_INTERVAL_DAYS);
        card.setLastReviewDate(null);
        card.setLeechWarningCount(0);
        card.setLeech(false);
        
        // Schedule new initial reminder
        LocalDate today = LocalDate.now();
        card.setNextReviewDate(today.plusDays(INITIAL_REMINDER_DAYS));
    }
    
    /**
     * Calculate base interval without fuzzing
     */
    private int calculateBaseInterval(Card card) {
        if (card.getRepetitionCount() == 3) {
            // First review after learning phase
            return INITIAL_INTERVAL_DAYS;
        } else if (card.getRepetitionCount() == 4) {
            // Second review
            return SECOND_INTERVAL_DAYS;
        } else {
            // Subsequent reviews
            return (int) Math.round(card.getIntervalDays() * card.getEaseFactor());
        }
    }
    
    /**
     * Apply fuzzing to interval to prevent cards from bunching up
     */
    private int applyFuzzing(int baseInterval) {
        if (baseInterval < 2) {
            // No fuzzing for very short intervals
            return baseInterval;
        }
        
        int fuzzRange = (int) Math.round(baseInterval * FUZZ_PERCENTAGE);
        if (fuzzRange < 1) {
            fuzzRange = 1;
        }
        
        int fuzz = random.nextInt(fuzzRange * 2 + 1) - fuzzRange;
        return baseInterval + fuzz;
    }
    
    /**
     * Calculate ease factor adjustment based on answer quality
     */
    private double calculateEaseAdjustment(int qualityValue) {
        switch (qualityValue) {
            case 5: // Easy
                return 0.15;
            case 4: // Good
                return 0.1;
            case 3: // Hard
                return -0.15;
            case 2: // Incorrect
                return -0.25;
            case 1: // Completely forgot
                return -0.35;
            default:
                return 0.0;
        }
    }
}