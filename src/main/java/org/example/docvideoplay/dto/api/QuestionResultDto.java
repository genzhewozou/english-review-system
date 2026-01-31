package org.example.docvideoplay.dto.api;

public class QuestionResultDto {
    
    private Long highlightId;
    private String text;
    private String context;
    private String userComment;
    private Integer questionNumber;
    private Integer totalQuestions;
    
    // Constructors
    public QuestionResultDto() {}
    
    public QuestionResultDto(Long highlightId, String text, String context, String userComment,
                            Integer questionNumber, Integer totalQuestions) {
        this.highlightId = highlightId;
        this.text = text;
        this.context = context;
        this.userComment = userComment;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
    }
    
    // Getters and Setters
    public Long getHighlightId() {
        return highlightId;
    }
    
    public void setHighlightId(Long highlightId) {
        this.highlightId = highlightId;
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
    
    public String getUserComment() {
        return userComment;
    }
    
    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }
    
    public Integer getQuestionNumber() {
        return questionNumber;
    }
    
    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }
    
    public Integer getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}