package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.AnswerQuality;

import java.time.LocalDateTime;

public class ReviewRecordResultDto {
    
    private Long id;
    private Long sessionId;
    private Long highlightId;
    private String highlightText;
    private String highlightContext;
    private AnswerQuality quality;
    private LocalDateTime reviewTime;
    private Integer responseTimeSeconds;
    
    // Constructors
    public ReviewRecordResultDto() {}
    
    public ReviewRecordResultDto(Long id, Long sessionId, Long highlightId, String highlightText,
                                String highlightContext, AnswerQuality quality, LocalDateTime reviewTime,
                                Integer responseTimeSeconds) {
        this.id = id;
        this.sessionId = sessionId;
        this.highlightId = highlightId;
        this.highlightText = highlightText;
        this.highlightContext = highlightContext;
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
    
    public Long getHighlightId() {
        return highlightId;
    }
    
    public void setHighlightId(Long highlightId) {
        this.highlightId = highlightId;
    }
    
    public String getHighlightText() {
        return highlightText;
    }
    
    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
    }
    
    public String getHighlightContext() {
        return highlightContext;
    }
    
    public void setHighlightContext(String highlightContext) {
        this.highlightContext = highlightContext;
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