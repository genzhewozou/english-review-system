package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.AnswerQuality;

import javax.validation.constraints.NotNull;

public class AnswerParamsDto {
    
    @NotNull(message = "Card ID is required")
    private Long cardId;
    
    @NotNull(message = "Answer quality is required")
    private AnswerQuality quality;
    
    private Integer responseTimeSeconds;
    
    // Constructors
    public AnswerParamsDto() {}
    
    public AnswerParamsDto(Long cardId, AnswerQuality quality, Integer responseTimeSeconds) {
        this.cardId = cardId;
        this.quality = quality;
        this.responseTimeSeconds = responseTimeSeconds;
    }
    
    // Getters and Setters
    public Long getCardId() {
        return cardId;
    }
    
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    
    public AnswerQuality getQuality() {
        return quality;
    }
    
    public void setQuality(AnswerQuality quality) {
        this.quality = quality;
    }
    
    public Integer getResponseTimeSeconds() {
        return responseTimeSeconds;
    }
    
    public void setResponseTimeSeconds(Integer responseTimeSeconds) {
        this.responseTimeSeconds = responseTimeSeconds;
    }
}