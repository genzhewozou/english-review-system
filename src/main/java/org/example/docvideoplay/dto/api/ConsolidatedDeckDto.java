package org.example.docvideoplay.dto.api;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Consolidated Deck DTO that combines fields from both DeckParamsDto and DeckResultDto.
 * This DTO can be used for both creating/updating decks and returning deck information.
 */
public class ConsolidatedDeckDto {
    
    // Identity fields
    private Long id;
    
    // Core deck fields (from DeckParamsDto)
    @NotBlank(message = "Deck name is required")
    private String name;
    
    private String description;
    private Boolean isPublic;
    
    // Additional deck fields (from DeckResultDto)
    private Long userId;
    private String userName;
    private int cardCount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Review options (from DeckResultDto)
    private Integer newCardsPerDay;
    private Integer maxReviewsPerDay;
    private Integer easyInterval;
    private Double easyBonus;
    private Double intervalModifier;
    private Double startingEase;
    private Integer steps;
    
    // Constructors
    public ConsolidatedDeckDto() {}
    
    // From DeckParamsDto
    public ConsolidatedDeckDto(DeckParamsDto paramsDto) {
        this.name = paramsDto.getName();
        this.description = paramsDto.getDescription();
        this.isPublic = paramsDto.getIsPublic();
    }
    
    // From DeckResultDto
    public ConsolidatedDeckDto(DeckResultDto resultDto) {
        this.id = resultDto.getId();
        this.name = resultDto.getName();
        this.description = resultDto.getDescription();
        this.isPublic = resultDto.getIsPublic();
        this.userId = resultDto.getUserId();
        this.userName = resultDto.getUserName();
        this.cardCount = resultDto.getCardCount();
        this.createdDate = resultDto.getCreatedDate();
        this.updatedDate = resultDto.getUpdatedDate();
        this.newCardsPerDay = resultDto.getNewCardsPerDay();
        this.maxReviewsPerDay = resultDto.getMaxReviewsPerDay();
        this.easyInterval = resultDto.getEasyInterval();
        this.easyBonus = resultDto.getEasyBonus();
        this.intervalModifier = resultDto.getIntervalModifier();
        this.startingEase = resultDto.getStartingEase();
        this.steps = resultDto.getSteps();
    }
    
    // Full constructor
    public ConsolidatedDeckDto(Long id, String name, String description, Boolean isPublic, 
                              Long userId, String userName, int cardCount,
                              LocalDateTime createdDate, LocalDateTime updatedDate,
                              Integer newCardsPerDay, Integer maxReviewsPerDay, Integer easyInterval,
                              Double easyBonus, Double intervalModifier, Double startingEase, Integer steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.userId = userId;
        this.userName = userName;
        this.cardCount = cardCount;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.newCardsPerDay = newCardsPerDay;
        this.maxReviewsPerDay = maxReviewsPerDay;
        this.easyInterval = easyInterval;
        this.easyBonus = easyBonus;
        this.intervalModifier = intervalModifier;
        this.startingEase = startingEase;
        this.steps = steps;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public int getCardCount() {
        return cardCount;
    }
    
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
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
    
    // Conversion methods
    public DeckParamsDto toDeckParamsDto() {
        return new DeckParamsDto(
            this.name,
            this.description,
            this.isPublic
        );
    }
    
    public DeckResultDto toDeckResultDto() {
        return new DeckResultDto(
            this.id,
            this.name,
            this.description,
            this.isPublic,
            this.userId,
            this.userName,
            this.cardCount,
            this.createdDate,
            this.updatedDate,
            this.newCardsPerDay,
            this.maxReviewsPerDay,
            this.easyInterval,
            this.easyBonus,
            this.intervalModifier,
            this.startingEase,
            this.steps
        );
    }
}
