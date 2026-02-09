package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.MaterialType;

import java.time.LocalDateTime;

public class StudyMaterialResultDto {
    
    private Long id;
    private String title;
    private String fileName;
    private MaterialType type;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Integer cardCount;
    
    // Constructors
    public StudyMaterialResultDto() {}
    
    public StudyMaterialResultDto(Long id, String title, String fileName, MaterialType type, 
                                 String mimeType, Long fileSize, LocalDateTime createdDate, 
                                 LocalDateTime updatedDate, Integer cardCount) {
        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.type = type;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.cardCount = cardCount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public MaterialType getType() {
        return type;
    }
    
    public void setType(MaterialType type) {
        this.type = type;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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
    
    public Integer getCardCount() {
        return cardCount;
    }
    
    public void setCardCount(Integer cardCount) {
        this.cardCount = cardCount;
    }
}