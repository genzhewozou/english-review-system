package org.example.docvideoplay.enums;

public enum AnswerQuality {
    PERFECT(5),     // Perfect response
    CORRECT(4),     // Correct after hesitation
    DIFFICULT(3),   // Correct with serious difficulty
    PASS(3),        // User passed on this question
    INCORRECT(2),   // Incorrect but seemed easy
    REMEMBERED(1),  // Incorrect but remembered
    BLACKOUT(0),    // Complete blackout
    NOT_GOT_IT(0);  // User didn't get it
    
    private final int value;
    
    AnswerQuality(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}