package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.TodoType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoItemResultDto {
    
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Boolean completed;
    private TodoType type;
    private Long relatedHighlightId;
    private String relatedHighlightText;
    private Boolean overdue;
    private Boolean dueToday;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Constructors
    public TodoItemResultDto() {}
    
    public TodoItemResultDto(Long id, String title, String description, LocalDate dueDate,
                            Boolean completed, TodoType type, Long relatedHighlightId,
                            String relatedHighlightText, Boolean overdue, Boolean dueToday,
                            LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.type = type;
        this.relatedHighlightId = relatedHighlightId;
        this.relatedHighlightText = relatedHighlightText;
        this.overdue = overdue;
        this.dueToday = dueToday;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public Boolean getCompleted() {
        return completed;
    }
    
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
    public TodoType getType() {
        return type;
    }
    
    public void setType(TodoType type) {
        this.type = type;
    }
    
    public Long getRelatedHighlightId() {
        return relatedHighlightId;
    }
    
    public void setRelatedHighlightId(Long relatedHighlightId) {
        this.relatedHighlightId = relatedHighlightId;
    }
    
    public String getRelatedHighlightText() {
        return relatedHighlightText;
    }
    
    public void setRelatedHighlightText(String relatedHighlightText) {
        this.relatedHighlightText = relatedHighlightText;
    }
    
    public Boolean getOverdue() {
        return overdue;
    }
    
    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }
    
    public Boolean getDueToday() {
        return dueToday;
    }
    
    public void setDueToday(Boolean dueToday) {
        this.dueToday = dueToday;
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
}