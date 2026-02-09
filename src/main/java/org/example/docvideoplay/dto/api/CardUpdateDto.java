package org.example.docvideoplay.dto.api;

/**
 * DTO for updating card information.
 * All fields are optional - only provided fields will be updated.
 */
public class CardUpdateDto {
    
    private String text;
    private String context;
    private Integer startPosition;
    private Integer endPosition;
    private String userComment;
    
    // Constructors
    public CardUpdateDto() {}
    
    public CardUpdateDto(String text, String context, 
                         Integer startPosition, Integer endPosition, String userComment) {
        this.text = text;
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.userComment = userComment;
    }
    
    // Getters and Setters
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
