package org.example.docvideoplay.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class Card extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "material_id", nullable = true)
    private Long materialId;
    
    @Column(name = "deck_id", nullable = true)
    private Long deckId;
    
    @Column(nullable = false, length = 1000)
    private String text;
    
    @Column(nullable = false, length = 1000)
    private String backText;
    
    @Column(length = 2000)
    private String context;
    
    @Column
    private Integer startPosition;
    
    @Column
    private Integer endPosition;
    
    @Column(length = 2000)
    private String userComment;
    
    @Column(length = 50)
    private String cardType = "BASIC";
    
    @Column
    private Long templateId;
    
    @Column(length = 1000)
    private String tags;
    
    @Column
    private Boolean isActive = true;
    
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
    
    // Leech-related fields
    @Column(nullable = false)
    private Integer leechWarningCount = 0;
    
    @Column
    private Boolean leech = false;
    
    // Constructors
    public Card() {}
    
    public Card(Long materialId, String text, String backText, String context, Integer startPosition, Integer endPosition) {
        this.materialId = materialId;
        this.text = text;
        this.backText = backText;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    public Card(Long userId, Long materialId, String text, String backText, String context, Integer startPosition, Integer endPosition) {
        this.userId = userId;
        this.materialId = materialId;
        this.text = text;
        this.backText = backText;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    public Card(Long userId, Long deckId, String text, String backText, String cardType, String tags) {
        this.userId = userId;
        this.deckId = deckId;
        this.text = text;
        this.backText = backText;
        this.cardType = cardType;
        this.tags = tags;
    }
    
    public Card(Long userId, Long deckId, String text, String backText, String cardType, String tags, Long templateId) {
        this.userId = userId;
        this.deckId = deckId;
        this.text = text;
        this.backText = backText;
        this.cardType = cardType;
        this.tags = tags;
        this.templateId = templateId;
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
    
    public Long getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }
    
    public Long getDeckId() {
        return deckId;
    }
    
    public void setDeckId(Long deckId) {
        this.deckId = deckId;
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
    

    
    public String getBackText() {
        return backText;
    }
    
    public void setBackText(String backText) {
        this.backText = backText;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getLeechWarningCount() {
        return leechWarningCount;
    }
    
    public void setLeechWarningCount(Integer leechWarningCount) {
        this.leechWarningCount = leechWarningCount;
    }
    
    public Boolean getLeech() {
        return leech;
    }
    
    public void setLeech(Boolean leech) {
        this.leech = leech;
    }
}