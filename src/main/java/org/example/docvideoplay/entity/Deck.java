package org.example.docvideoplay.entity;

import javax.persistence.*;

@Entity
@Table(name = "decks")
public class Deck extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Boolean isPublic = false;
    
    // Review options
    @Column(nullable = false)
    private Integer newCardsPerDay = 20;
    
    @Column(nullable = false)
    private Integer maxReviewsPerDay = 100;
    
    @Column(nullable = false)
    private Integer easyInterval = 4;
    
    @Column(nullable = false)
    private Double easyBonus = 1.3;
    
    @Column(nullable = false)
    private Double intervalModifier = 1.0;
    
    @Column(nullable = false)
    private Double startingEase = 2.5;
    
    @Column(nullable = false)
    private Integer steps = 1;
    
    // Constructors
    public Deck() {}
    
    public Deck(Long userId, String name, String description) {
        this.userId = userId;
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Integer getNewCardsPerDay() {
        return newCardsPerDay;
    }
    
    public void setNewCardsPerDay(Integer newCardsPerDay) {
        this.newCardsPerDay = newCardsPerDay;
    }
    
    public Integer getMaxReviewsPerDay() {
        return maxReviewsPerDay;
    }
    
    public void setMaxReviewsPerDay(Integer maxReviewsPerDay) {
        this.maxReviewsPerDay = maxReviewsPerDay;
    }
    
    public Integer getEasyInterval() {
        return easyInterval;
    }
    
    public void setEasyInterval(Integer easyInterval) {
        this.easyInterval = easyInterval;
    }
    
    public Double getEasyBonus() {
        return easyBonus;
    }
    
    public void setEasyBonus(Double easyBonus) {
        this.easyBonus = easyBonus;
    }
    
    public Double getIntervalModifier() {
        return intervalModifier;
    }
    
    public void setIntervalModifier(Double intervalModifier) {
        this.intervalModifier = intervalModifier;
    }
    
    public Double getStartingEase() {
        return startingEase;
    }
    
    public void setStartingEase(Double startingEase) {
        this.startingEase = startingEase;
    }
    
    public Integer getSteps() {
        return steps;
    }
    
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
}