package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.enums.AnswerQuality;

import java.time.LocalDate;

/**
 * Service for managing spaced repetition algorithm and scheduling review dates.
 * Implements the SM-2 algorithm for optimal learning intervals.
 */
public interface SpacedRepetitionService {
    
    /**
     * Calculate the next review date for a card based on the answer quality.
     * Uses the SM-2 algorithm to determine optimal spacing intervals.
     * 
     * @param card The card being reviewed
     * @param quality The quality of the user's answer
     * @return The calculated next review date
     */
    LocalDate calculateNextReviewDate(Card card, AnswerQuality quality);
    
    /**
     * Process a review answer and update the card's spaced repetition parameters.
     * This method updates easeFactor, repetitionCount, intervalDays, and nextReviewDate.
     * 
     * @param card The card being reviewed
     * @param quality The quality of the user's answer
     */
    void processReviewAnswer(Card card, AnswerQuality quality);
    
    /**
     * Schedule the initial 5-day reminder for a newly created card.
     * This is called when a card is first created to set up the initial review.
     * 
     * @param card The newly created card
     */
    void scheduleInitialReminder(Card card);
    
    /**
     * Reset a card's spaced repetition parameters to initial values.
     * Useful when a user wants to restart learning a particular item.
     * 
     * @param card The card to reset
     */
    void resetSpacedRepetitionData(Card card);
}