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
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewRecord> reviewRecords = new ArrayList<>();

    /**
     * Optional list of highlight IDs explicitly selected for this session.
     * When present, the session will only cycle through these highlights.
     */
    @ElementCollection
    @CollectionTable(name = "review_session_highlights", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "highlight_id")
    private List<Long> selectedHighlightIds = new ArrayList<>();
    
    // Constructors
    public ReviewSession() {
        this.startTime = LocalDateTime.now();
    }
    
    public ReviewSession(LocalDateTime startTime) {
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
    
    public List<ReviewRecord> getReviewRecords() {
        return reviewRecords;
    }
    
    public void setReviewRecords(List<ReviewRecord> reviewRecords) {
        this.reviewRecords = reviewRecords;
    }

    public List<Long> getSelectedHighlightIds() {
        return selectedHighlightIds;
    }

    public void setSelectedHighlightIds(List<Long> selectedHighlightIds) {
        this.selectedHighlightIds = selectedHighlightIds;
    }
    
    // Helper methods
    public void addReviewRecord(ReviewRecord reviewRecord) {
        reviewRecords.add(reviewRecord);
        reviewRecord.setSession(this);
        this.totalQuestions = reviewRecords.size();
    }
    
    public void removeReviewRecord(ReviewRecord reviewRecord) {
        reviewRecords.remove(reviewRecord);
        reviewRecord.setSession(null);
        this.totalQuestions = reviewRecords.size();
    }
    
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