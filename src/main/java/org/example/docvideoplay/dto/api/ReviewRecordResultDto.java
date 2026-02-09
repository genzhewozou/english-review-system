package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.AnswerQuality;

import java.time.LocalDateTime;

public class ReviewRecordResultDto {
    
    private Long id;
    private Long sessionId;
    private Long cardId;
    private String cardText;
    private String cardContext;
    private AnswerQuality quality;
    private LocalDateTime reviewTime;
    private Integer responseTimeSeconds;
    
    // Constructors
    public ReviewRecordResultDto() {}
    
    public ReviewRecordResultDto(Long id, Long sessionId, Long cardId, String cardText,
                                String cardContext, AnswerQuality quality, LocalDateTime reviewTime,
                                Integer responseTimeSeconds) {
        this.id = id;
        this.sessionId = sessionId;
        this.cardId = cardId;
        this.cardText = cardText;
        this.cardContext = cardContext;
        this.quality = quality;
        this.reviewTime = reviewTime;
        this.responseTimeSeconds = responseTimeSeconds;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getCardId() {
        return cardId;
    }
    
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    
    public String getCardText() {
        return cardText;
    }
    
    public void setCardText(String cardText) {
        this.cardText = cardText;
    }
    
    public String getCardContext() {
        return cardContext;
    }
    
    public void setCardContext(String cardContext) {
        this.cardContext = cardContext;
    }
    
    public AnswerQuality getQuality() {
        return quality;
    }
    
    public void setQuality(AnswerQuality quality) {
        this.quality = quality;
    }
    
    public LocalDateTime getReviewTime() {
        return reviewTime;
    }
    
    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }
    
    public Integer getResponseTimeSeconds() {
        return responseTimeSeconds;
    }
    
    public void setResponseTimeSeconds(Integer responseTimeSeconds) {
        this.responseTimeSeconds = responseTimeSeconds;
    }
}