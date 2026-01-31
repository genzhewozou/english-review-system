package org.example.docvideoplay.entity;

import org.example.docvideoplay.enums.TodoType;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "todo_items")
public class TodoItem extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column
    private LocalDate dueDate;
    
    @Column(nullable = false)
    private Boolean completed = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_highlight_id")
    private Highlight relatedHighlight;
    
    // Constructors
    public TodoItem() {}
    
    public TodoItem(String title, String description, LocalDate dueDate, TodoType type) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.type = type;
    }
    
    public TodoItem(String title, String description, LocalDate dueDate, TodoType type, Highlight relatedHighlight) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.type = type;
        this.relatedHighlight = relatedHighlight;
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
    
    public Highlight getRelatedHighlight() {
        return relatedHighlight;
    }
    
    public void setRelatedHighlight(Highlight relatedHighlight) {
        this.relatedHighlight = relatedHighlight;
    }
    
    // Helper methods
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !completed;
    }
    
    public boolean isDueToday() {
        return dueDate != null && dueDate.equals(LocalDate.now()) && !completed;
    }
    
    public void markCompleted() {
        this.completed = true;
    }
}