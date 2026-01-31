package org.example.docvideoplay.dto.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class HighlightParamsDto {
    
    @NotNull(message = "Material ID is required")
    private Long materialId;
    
    @NotBlank(message = "Text is required")
    private String text;
    
    private String context;
    private Integer startPosition;
    private Integer endPosition;
    private String userComment;
    
    // Constructors
    public HighlightParamsDto() {}
    
    public HighlightParamsDto(Long materialId, String text, String context, 
                             Integer startPosition, Integer endPosition, String userComment) {
        this.materialId = materialId;
        this.text = text;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.userComment = userComment;
    }
    
    // Getters and Setters
    public Long getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
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
}