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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReviewSession session;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", nullable = false)
    private Highlight highlight;
    
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
    
    public ReviewRecord(ReviewSession session, Highlight highlight, AnswerQuality quality) {
        this.session = session;
        this.highlight = highlight;
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
    
    public ReviewSession getSession() {
        return session;
    }
    
    public void setSession(ReviewSession session) {
        this.session = session;
    }
    
    public Highlight getHighlight() {
        return highlight;
    }
    
    public void setHighlight(Highlight highlight) {
        this.highlight = highlight;
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
               quality == AnswerQuality.DIFFICULT;
    }
}