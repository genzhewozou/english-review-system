package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.TodoType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class TodoItemParamsDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private LocalDate dueDate;
    
    @NotNull(message = "Todo type is required")
    private TodoType type;
    
    private Long relatedCardId;
    private Long relatedSessionId;
    
    // Constructors
    public TodoItemParamsDto() {}
    
    public TodoItemParamsDto(String title, String description, LocalDate dueDate, 
                            TodoType type) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.type = type;
    }
    
    public TodoItemParamsDto(String title, String description, LocalDate dueDate, 
                            TodoType type, Long relatedSessionId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.type = type;
        this.relatedSessionId = relatedSessionId;
    }
    
    public TodoItemParamsDto(String title, String description, LocalDate dueDate, 
                            TodoType type, Long relatedCardId, Long relatedSessionId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.type = type;
        this.relatedCardId = relatedCardId;
        this.relatedSessionId = relatedSessionId;
    }
    
    // Getters and Setters
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
    
    public TodoType getType() {
        return type;
    }
    
    public void setType(TodoType type) {
        this.type = type;
    }
    
    public Long getRelatedSessionId() {
        return relatedSessionId;
    }
    
    public void setRelatedSessionId(Long relatedSessionId) {
        this.relatedSessionId = relatedSessionId;
    }
    
    public Long getRelatedCardId() {
        return relatedCardId;
    }
    
    public void setRelatedCardId(Long relatedCardId) {
        this.relatedCardId = relatedCardId;
    }
}