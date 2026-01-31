package org.example.docvideoplay.dto.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HighlightResultDto {
    
    private Long id;
    private Long materialId;
    private String text;
    private String context;
    private Integer startPosition;
    private Integer endPosition;
    private String userComment;
    private Double easeFactor;
    private Integer repetitionCount;
    private Integer intervalDays;
    private LocalDate nextReviewDate;
    private LocalDate lastReviewDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Constructors
    public HighlightResultDto() {}
    
    public HighlightResultDto(Long id, Long materialId, String text, String context, 
                             Integer startPosition, Integer endPosition, String userComment,
                             Double easeFactor, Integer repetitionCount, Integer intervalDays,
                             LocalDate nextReviewDate, LocalDate lastReviewDate,
                             LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.materialId = materialId;
        this.text = text;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.userComment = userComment;
        this.easeFactor = easeFactor;
        this.repetitionCount = repetitionCount;
        this.intervalDays = intervalDays;
        this.nextReviewDate = nextReviewDate;
        this.lastReviewDate = lastReviewDate;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public Integer getStartPosition() {
        return startPosition;
    }
    
    public void setStartPosition(Integer startPosition) {
        this.startPosition = startPosition;
    }
    
    public Integer getEndPosition() {
        return endPosition;
    }
    
    public void setEndPosition(Integer endPosition) {
        this.endPosition = endPosition;
    }
    
    public String getUserComment() {
        return userComment;
    }
    
    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }
    
    public Double getEaseFactor() {
        return easeFactor;
    }
    
    public void setEaseFactor(Double easeFactor) {
        this.easeFactor = easeFactor;
    }
    
    public Integer getRepetitionCount() {
        return repetitionCount;
    }
    
    public void setRepetitionCount(Integer repetitionCount) {
        this.repetitionCount = repetitionCount;
    }
    
    public Integer getIntervalDays() {
        return intervalDays;
    }
    
    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }
    
    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }
    
    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }
    
    public LocalDate getLastReviewDate() {
        return lastReviewDate;
    }
    
    public void setLastReviewDate(LocalDate lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}