package org.example.docvideoplay.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "highlights")
public class Highlight extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private StudyMaterial material;
    
    @Column(nullable = false, length = 1000)
    private String text;
    
    @Column(length = 2000)
    private String context;
    
    @Column
    private Integer startPosition;
    
    @Column
    private Integer endPosition;
    
    @Column(length = 2000)
    private String userComment;
    
    // Spaced repetition fields
    @Column(nullable = false)
    private Double easeFactor = 2.5;
    
    @Column(nullable = false)
    private Integer repetitionCount = 0;
    
    @Column(nullable = false)
    private Integer intervalDays = 1;
    
    @Column
    private LocalDate nextReviewDate;
    
    @Column
    private LocalDate lastReviewDate;
    
    @OneToMany(mappedBy = "highlight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewRecord> reviewHistory = new ArrayList<>();
    
    // Constructors
    public Highlight() {}
    
    public Highlight(StudyMaterial material, String text, String context, Integer startPosition, Integer endPosition) {
        this.material = material;
        this.text = text;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public StudyMaterial getMaterial() {
        return material;
    }
    
    public void setMaterial(StudyMaterial material) {
        this.material = material;
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
    
    public List<ReviewRecord> getReviewHistory() {
        return reviewHistory;
    }
    
    public void setReviewHistory(List<ReviewRecord> reviewHistory) {
        this.reviewHistory = reviewHistory;
    }
    
    // Helper methods
    public void addReviewRecord(ReviewRecord reviewRecord) {
        reviewHistory.add(reviewRecord);
        reviewRecord.setHighlight(this);
    }
    
    public void removeReviewRecord(ReviewRecord reviewRecord) {
        reviewHistory.remove(reviewRecord);
        reviewRecord.setHighlight(null);
    }
}