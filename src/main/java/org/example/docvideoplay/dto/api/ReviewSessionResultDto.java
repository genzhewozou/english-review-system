package org.example.docvideoplay.dto.api;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewSessionResultDto {
    
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean completed;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double accuracyPercentage;
    private List<ReviewRecordResultDto> reviewRecords;
    
    // Constructors
    public ReviewSessionResultDto() {}
    
    public ReviewSessionResultDto(Long id, LocalDateTime startTime, LocalDateTime endTime,
                                 Boolean completed, Integer totalQuestions, Integer correctAnswers,
                                 Double accuracyPercentage, List<ReviewRecordResultDto> reviewRecords) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.completed = completed;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.accuracyPercentage = accuracyPercentage;
        this.reviewRecords = reviewRecords;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Boolean getCompleted() {
        return completed;
    }
    
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
    public Integer getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public Integer getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public Double getAccuracyPercentage() {
        return accuracyPercentage;
    }
    
    public void setAccuracyPercentage(Double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }
    
    public List<ReviewRecordResultDto> getReviewRecords() {
        return reviewRecords;
    }
    
    public void setReviewRecords(List<ReviewRecordResultDto> reviewRecords) {
        this.reviewRecords = reviewRecords;
    }
}