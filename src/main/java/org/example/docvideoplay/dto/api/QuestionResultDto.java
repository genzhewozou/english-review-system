package org.example.docvideoplay.dto.api;

public class QuestionResultDto {
    
    private Long cardId;
    private String text;
    private String backText;
    private String context;
    private String userComment;
    private Integer questionNumber;
    private Integer totalQuestions;
    
    // Constructors
    public QuestionResultDto() {}
    
    public QuestionResultDto(Long cardId, String text, String backText, String context, String userComment,
                            Integer questionNumber, Integer totalQuestions) {
        this.cardId = cardId;
        this.text = text;
        this.backText = backText;
        this.context = context;
        this.userComment = userComment;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
    }
    
    // Getters and Setters
    public Long getCardId() {
        return cardId;
    }
    
    public void setCardId(Long cardId) {
        this.cardId = cardId;
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