package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.dao.jpa.TodoItemRepository;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.enums.TodoType;
import org.example.docvideoplay.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TodoService for managing todo items and review scheduling.
 * Handles automatic scheduling for spaced repetition and synchronization with review sessions.
 */
@Service
@Transactional
public class TodoServiceImpl implements TodoService {
    
    private final TodoItemRepository todoItemRepository;
    
    @Autowired
    public TodoServiceImpl(TodoItemRepository todoItemRepository) {
        this.todoItemRepository = todoItemRepository;
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type) {
        TodoItem todoItem = new TodoItem(title, description, dueDate, type);
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    public TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Highlight relatedHighlight) {
        TodoItem todoItem = new TodoItem(title, description, dueDate, type, relatedHighlight);
        return todoItemRepository.save(todoItem);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getAllIncompleteTodoItems() {
        return todoItemRepository.findByCompletedFalseOrderByDueDateAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getAllCompletedTodoItems() {
        return todoItemRepository.findByCompletedTrueOrderByUpdatedDateDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsByType(TodoType type, Boolean completed) {
        return todoItemRepository.findByTypeAndCompletedOrderByDueDateAsc(type, completed);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueToday() {
        return todoItemRepository.findTodoItemsDueToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getOverdueTodoItems() {
        return todoItemRepository.findOverdueTodoItems();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueInNextDays(int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        return todoItemRepository.findTodoItemsDueInNextDays(endDate);
    }
    
    @Override
    public TodoItem completeTodoItem(Long todoItemId) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            todoItem.markCompleted();
            return todoItemRepository.save(todoItem);
        }
        throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
    }
    
    @Override
    public TodoItem updateTodoItem(Long todoItemId, String title, String description, LocalDate dueDate) {
        Optional<TodoItem> todoItemOpt = todoItemRepository.findById(todoItemId);
        if (todoItemOpt.isPresent()) {
            TodoItem todoItem = todoItemOpt.get();
            todoItem.setTitle(title);
            todoItem.setDescription(description);
            todoItem.setDueDate(dueDate);
            return todoItemRepository.save(todoItem);
        }
        throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
    }
    
    @Override
    public void deleteTodoItem(Long todoItemId) {
        if (todoItemRepository.existsById(todoItemId)) {
            todoItemRepository.deleteById(todoItemId);
        } else {
            throw new IllegalArgumentException("TodoItem with ID " + todoItemId + " not found");
        }
    }
    
    @Override
    public void scheduleReviewReminder(Highlight highlight) {
        if (highlight.getNextReviewDate() == null || highlight.getId() == null) {
            return; // No review date set or highlight not persisted, cannot schedule
        }
        
        // Check if there's already a todo item for this highlight's next review
        List<TodoItem> existingTodos = todoItemRepository.findByRelatedHighlightIdOrderByDueDateAsc(highlight.getId());
        boolean hasExistingReviewTodo = existingTodos.stream()
                .anyMatch(todo -> todo.getType() == TodoType.REVIEW_SESSION && 
                                !todo.getCompleted() && 
                                todo.getDueDate() != null &&
                                todo.getDueDate().equals(highlight.getNextReviewDate()));
        
        if (!hasExistingReviewTodo) {
            String title = "Review: " + (highlight.getText().length() > 30 ? 
                    highlight.getText().substring(0, 30) + "..." : highlight.getText());
            String description = "Review the highlighted word/phrase: \"" + highlight.getText() + "\"";
            if (highlight.getContext() != null && !highlight.getContext().trim().isEmpty()) {
                description += "\nContext: " + highlight.getContext();
            }
            
            createTodoItem(title, description, highlight.getNextReviewDate(), TodoType.REVIEW_SESSION, highlight);
        }
    }
    
    @Override
    public void synchronizeWithReviewCompletion(Highlight highlight) {
        // Find all incomplete review session todo items for this highlight
        List<TodoItem> reviewTodos = todoItemRepository.findByRelatedHighlightIdOrderByDueDateAsc(highlight.getId());
        
        for (TodoItem todo : reviewTodos) {
            if (todo.getType() == TodoType.REVIEW_SESSION && !todo.getCompleted()) {
                // If the highlight was reviewed (has a lastReviewDate) and the todo is due on or before today,
                // mark it as completed
                if (highlight.getLastReviewDate() != null && 
                    todo.getDueDate() != null && 
                    !todo.getDueDate().isAfter(LocalDate.now())) {
                    todo.markCompleted();
                    todoItemRepository.save(todo);
                }
            }
        }
        
        // Schedule the next review reminder if there's a next review date
        if (highlight.getNextReviewDate() != null && highlight.getNextReviewDate().isAfter(LocalDate.now())) {
            scheduleReviewReminder(highlight);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsByHighlight(Long highlightId) {
        return todoItemRepository.findByRelatedHighlightIdOrderByDueDateAsc(highlightId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTodoItemsByStatus(Boolean completed) {
        return todoItemRepository.countByCompleted(completed);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countOverdueTodoItems() {
        return todoItemRepository.countOverdueTodoItems();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> searchTodoItemsByTitle(String searchTerm) {
        return todoItemRepository.findByTitleContainingIgnoreCaseOrderByDueDateAsc(searchTerm);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getTodoItemsDueBetween(LocalDate startDate, LocalDate endDate) {
        return todoItemRepository.findTodoItemsDueBetween(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getReviewSessionTodoItems() {
        return todoItemRepository.findReviewSessionTodoItems();
    }
}