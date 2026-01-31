package org.example.docvideoplay.enums;

public enum AnswerQuality {
    PERFECT(5),     // Perfect response
    CORRECT(4),     // Correct after hesitation
    DIFFICULT(3),   // Correct with serious difficulty
    INCORRECT(2),   // Incorrect but seemed easy
    REMEMBERED(1),  // Incorrect but remembered
    BLACKOUT(0);    // Complete blackout
    
    private final int value;
    
    AnswerQuality(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}