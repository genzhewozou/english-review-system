package org.example.docvideoplay.dto.api;

import javax.validation.constraints.NotNull;

public class DeckCardParamsDto {
    
    @NotNull(message = "Card ID is required")
    private Long cardId;
    
    // Constructors
    public DeckCardParamsDto() {}
    
    public DeckCardParamsDto(Long cardId) {
        this.cardId = cardId;
    }
    
    // Getters and Setters
    public Long getCardId() {
        return cardId;
    }
    
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
}