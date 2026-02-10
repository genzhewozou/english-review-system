package org.example.docvideoplay.dto.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class CardParamsDto {
    
    private Long materialId;
   
    @NotBlank(message = "Text is required")
    private String text;
    
    private String backText;
    
    private String context;
    private Integer startPosition;
    private Integer endPosition;
    private String userComment;
    private List<Object> tags;
    
    // Constructors
    public CardParamsDto() {}
    
    public CardParamsDto(Long materialId, String text, String backText, String context, 
                          Integer startPosition, Integer endPosition, String userComment) {
        this.materialId = materialId;
        this.text = text;
        this.backText = backText;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.userComment = userComment;
    }
    
    public CardParamsDto(Long materialId, String text, String backText, String context, 
                          Integer startPosition, Integer endPosition, String userComment, List<Object> tags) {
        this.materialId = materialId;
        this.text = text;
        this.backText = backText;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.userComment = userComment;
        this.tags = tags;
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
    
    public String getBackText() {
        return backText;
    }
    
    public void setBackText(String backText) {
        this.backText = backText;
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
    
    public List<Object> getTags() {
        return tags;
    }
    
    public void setTags(List<Object> tags) {
        this.tags = tags;
    }
}
