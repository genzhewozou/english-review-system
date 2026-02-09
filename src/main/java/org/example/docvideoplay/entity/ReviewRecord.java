package org.example.docvideoplay.entity;

import org.example.docvideoplay.enums.AnswerQuality;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_records")
public class ReviewRecord extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    @Column(name = "card_id", nullable = false)
    private Long cardId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerQuality quality;
    
    @Column(nullable = false)
    private LocalDateTime reviewTime;
    
    @Column
    private Integer responseTimeSeconds;
    
    // Constructors
    public ReviewRecord() {
        this.reviewTime = LocalDateTime.now();
    }
    
    public ReviewRecord(Long sessionId, Long cardId, AnswerQuality quality) {
        this.sessionId = sessionId;
        this.cardId = cardId;
        this.quality = quality;
        this.reviewTime = LocalDateTime.now();
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
    
    // Helper methods
    public boolean isCorrectAnswer() {
        return quality == AnswerQuality.PERFECT || 
               quality == AnswerQuality.CORRECT || 
               quality == AnswerQuality.DIFFICULT ||
               quality == AnswerQuality.PASS;
    }
}