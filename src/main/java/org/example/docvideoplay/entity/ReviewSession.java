package org.example.docvideoplay.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_sessions")
public class ReviewSession extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private Boolean completed = false;
    
    @Column
    private Integer totalQuestions = 0;
    
    @Column
    private Integer correctAnswers = 0;

    /**
     * Optional list of card IDs explicitly selected for this session.
     * When present, the session will only cycle through these cards.
     */
    @ElementCollection
    @CollectionTable(name = "review_session_cards", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "card_id")
    private List<Long> selectedCardIds = new ArrayList<>();
    
    // Constructors
    public ReviewSession() {
        this.startTime = LocalDateTime.now();
    }
    
    public ReviewSession(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public ReviewSession(Long userId) {
        this.userId = userId;
        this.startTime = LocalDateTime.now();
    }
    
    public ReviewSession(Long userId, LocalDateTime startTime) {
        this.userId = userId;
        this.startTime = startTime;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getSelectedCardIds() {
        return selectedCardIds;
    }

    public void setSelectedCardIds(List<Long> selectedCardIds) {
        this.selectedCardIds = selectedCardIds;
    }
    
    // Helper methods
    public void completeSession() {
        this.completed = true;
        this.endTime = LocalDateTime.now();
    }
    
    public double getAccuracyPercentage() {
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) correctAnswers / totalQuestions * 100.0;
    }
}