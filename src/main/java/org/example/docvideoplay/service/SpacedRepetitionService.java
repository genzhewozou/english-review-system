package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.enums.AnswerQuality;

import java.time.LocalDate;

/**
 * Service for managing spaced repetition algorithm and scheduling review dates.
 * Implements the SM-2 algorithm for optimal learning intervals.
 */
public interface SpacedRepetitionService {
    
    /**
     * Calculate the next review date for a highlight based on the answer quality.
     * Uses the SM-2 algorithm to determine optimal spacing intervals.
     * 
     * @param highlight The highlight being reviewed
     * @param quality The quality of the user's answer
     * @return The calculated next review date
     */
    LocalDate calculateNextReviewDate(Highlight highlight, AnswerQuality quality);
    
    /**
     * Process a review answer and update the highlight's spaced repetition parameters.
     * This method updates easeFactor, repetitionCount, intervalDays, and nextReviewDate.
     * 
     * @param highlight The highlight being reviewed
     * @param quality The quality of the user's answer
     */
    void processReviewAnswer(Highlight highlight, AnswerQuality quality);
    
    /**
     * Schedule the initial 5-day reminder for a newly created highlight.
     * This is called when a highlight is first created to set up the initial review.
     * 
     * @param highlight The newly created highlight
     */
    void scheduleInitialReminder(Highlight highlight);
    
    /**
     * Reset a highlight's spaced repetition parameters to initial values.
     * Useful when a user wants to restart learning a particular item.
     * 
     * @param highlight The highlight to reset
     */
    void resetSpacedRepetitionData(Highlight highlight);
}