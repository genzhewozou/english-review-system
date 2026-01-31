package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.TodoApi;
import org.example.docvideoplay.dto.api.TodoItemParamsDto;
import org.example.docvideoplay.dto.api.TodoItemResultDto;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.service.TodoService;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for todo list and task management operations.
 * Implements TodoApi interface for todo item creation, completion, and listing.
 */
@RestController
public class TodoController implements TodoApi {
    
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);
    
    private final TodoService todoService;
    private final VocabularyService vocabularyService;
    
    @Autowired
    public TodoController(TodoService todoService, VocabularyService vocabularyService) {
        this.todoService = todoService;
        this.vocabularyService = vocabularyService;
    }
    
    @Override
    public ResponseEntity<List<TodoItemResultDto>> getTodoItems(Boolean completed, Boolean overdue) {
        try {
            logger.debug("Retrieving todo items: completed={}, overdue={}", completed, overdue);
            
            List<TodoItem> todoItems;
            
            if (overdue != null && overdue) {
                // Get overdue items
                todoItems = todoService.getOverdueTodoItems();
            } else if (completed != null) {
                if (completed) {
                    todoItems = todoService.getAllCompletedTodoItems();
                } else {
                    todoItems = todoService.getAllIncompleteTodoItems();
                }
            } else {
                // Get all incomplete items by default
                todoItems = todoService.getAllIncompleteTodoItems();
            }
            
            List<TodoItemResultDto> results = todoItems.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} todo items", results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error retrieving todo items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> createTodoItem(@Valid TodoItemParamsDto params) {
        try {
            logger.info("Creating todo item: title={}, type={}", params.getTitle(), params.getType());
            
            TodoItem todoItem;
            
            if (params.getRelatedHighlightId() != null) {
                // Create todo item with related highlight
                try {
                    Highlight relatedHighlight = vocabularyService.getHighlightById(params.getRelatedHighlightId());
                    todoItem = todoService.createTodoItem(
                        params.getTitle(),
                        params.getDescription(),
                        params.getDueDate(),
                        params.getType(),
                        relatedHighlight
                    );
                } catch (IllegalArgumentException e) {
                    logger.warn("Related highlight not found: id={}", params.getRelatedHighlightId());
                    return ResponseEntity.badRequest().build();
                }
            } else {
                // Create todo item without related highlight
                todoItem = todoService.createTodoItem(
                    params.getTitle(),
                    params.getDescription(),
                    params.getDueDate(),
                    params.getType()
                );
            }
            
            TodoItemResultDto result = convertToResultDto(todoItem);
            
            logger.info("Todo item created successfully: id={}", todoItem.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (Exception e) {
            logger.error("Error creating todo item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> getTodoItem(Long id) {
        try {
            logger.debug("Retrieving todo item: id={}", id);
            
            // Find the todo item by searching through all items
            List<TodoItem> allItems = new ArrayList<>();
            allItems.addAll(todoService.getAllIncompleteTodoItems());
            allItems.addAll(todoService.getAllCompletedTodoItems());
            
            TodoItem todoItem = allItems.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (todoItem == null) {
                logger.warn("Todo item not found: id={}", id);
                return ResponseEntity.notFound().build();
            }
            
            TodoItemResultDto result = convertToResultDto(todoItem);
            
            logger.debug("Retrieved todo item: id={}, title={}", id, todoItem.getTitle());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error retrieving todo item {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> updateTodoItem(Long id, @Valid TodoItemParamsDto params) {
        try {
            logger.info("Updating todo item: id={}", id);
            
            TodoItem updatedItem = todoService.updateTodoItem(
                id,
                params.getTitle(),
                params.getDescription(),
                params.getDueDate()
            );
            
            TodoItemResultDto result = convertToResultDto(updatedItem);
            
            logger.info("Todo item updated successfully: id={}", id);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Todo item not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating todo item {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> completeTodoItem(Long id) {
        try {
            logger.info("Completing todo item: id={}", id);
            
            TodoItem completedItem = todoService.completeTodoItem(id);
            TodoItemResultDto result = convertToResultDto(completedItem);
            
            logger.info("Todo item completed successfully: id={}", id);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Todo item not found for completion: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error completing todo item {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<Void> deleteTodoItem(Long id) {
        try {
            logger.info("Deleting todo item: id={}", id);
            
            todoService.deleteTodoItem(id);
            
            logger.info("Todo item deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Todo item not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting todo item {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<TodoItemResultDto>> getTodoItemsDueToday() {
        try {
            logger.debug("Retrieving todo items due today");
            
            List<TodoItem> todoItems = todoService.getTodoItemsDueToday();
            List<TodoItemResultDto> results = todoItems.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} todo items due today", results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error retrieving todo items due today: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<TodoItemResultDto>> getOverdueTodoItems() {
        try {
            logger.debug("Retrieving overdue todo items");
            
            List<TodoItem> todoItems = todoService.getOverdueTodoItems();
            List<TodoItemResultDto> results = todoItems.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} overdue todo items", results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error retrieving overdue todo items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Convert TodoItem entity to TodoItemResultDto
     * 
     * @param todoItem The TodoItem entity
     * @return The converted DTO
     */
    private TodoItemResultDto convertToResultDto(TodoItem todoItem) {
        TodoItemResultDto dto = new TodoItemResultDto();
        dto.setId(todoItem.getId());
        dto.setTitle(todoItem.getTitle());
        dto.setDescription(todoItem.getDescription());
        dto.setDueDate(todoItem.getDueDate());
        dto.setCompleted(todoItem.getCompleted());
        dto.setType(todoItem.getType());
        dto.setCreatedDate(todoItem.getCreatedDate());
        dto.setUpdatedDate(todoItem.getUpdatedDate());
        
        // Set related highlight information
        if (todoItem.getRelatedHighlight() != null) {
            dto.setRelatedHighlightId(todoItem.getRelatedHighlight().getId());
            dto.setRelatedHighlightText(todoItem.getRelatedHighlight().getText());
        }
        
        // Set status flags
        dto.setOverdue(todoItem.isOverdue());
        dto.setDueToday(todoItem.isDueToday());
        
        return dto;
    }
}