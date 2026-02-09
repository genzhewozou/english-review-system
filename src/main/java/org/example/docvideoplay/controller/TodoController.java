package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.TodoApi;
import org.example.docvideoplay.dto.api.TodoItemParamsDto;
import org.example.docvideoplay.dto.api.TodoItemResultDto;

import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.service.ReviewService;
import org.example.docvideoplay.service.TodoService;
import org.example.docvideoplay.service.UserService;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;
    private final VocabularyService vocabularyService;
    private final ReviewService reviewService;
    
    @Autowired
    public TodoController(TodoService todoService, UserService userService, VocabularyService vocabularyService, ReviewService reviewService) {
        this.todoService = todoService;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
        this.reviewService = reviewService;
    }
    
    /**
     * Get the current authenticated user ID or default user ID
     * 
     * @return The current authenticated user ID or default user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If authenticated, get user by username
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            return userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
                    .getId();
        }
        
        // Default to user 'leo' if not authenticated
        return userService.findByUsername("leo")
                .orElseThrow(() -> new IllegalArgumentException("Default user not found"))
                .getId();
    }
    
    @Override
    public ResponseEntity<List<TodoItemResultDto>> getTodoItems(Boolean completed, Boolean overdue) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving todo items: completed={}, overdue={}, userId={}", completed, overdue, currentUserId);
            
            List<TodoItem> todoItems;
            
            if (overdue != null && overdue) {
                // Get overdue items
                todoItems = todoService.getOverdueTodoItems(currentUserId);
            } else if (completed != null) {
                if (completed) {
                    todoItems = todoService.getAllCompletedTodoItems(currentUserId);
                } else {
                    todoItems = todoService.getAllIncompleteTodoItems(currentUserId);
                }
            } else {
                // Get all incomplete items by default
                todoItems = todoService.getAllIncompleteTodoItems(currentUserId);
            }
            
            List<TodoItemResultDto> results = todoItems.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} todo items for userId: {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error retrieving todo items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> createTodoItem(@Valid TodoItemParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Creating todo item: title={}, type={}, userId={}", params.getTitle(), params.getType(), currentUserId);
            
            TodoItem todoItem;
            
            if (params.getRelatedCardId() != null) {
                // Create todo item with related card
                try {
                    // Get the card object
                    org.example.docvideoplay.entity.Card relatedCard = vocabularyService.getCardById(params.getRelatedCardId(), currentUserId);
                    todoItem = todoService.createTodoItem(
                        params.getTitle(),
                        params.getDescription(),
                        params.getDueDate(),
                        params.getType(),
                        relatedCard,
                        currentUserId
                    );
                } catch (IllegalArgumentException e) {
                    logger.warn("Related card not found: id={}", params.getRelatedCardId());
                    return ResponseEntity.badRequest().build();
                }
            } else if (params.getRelatedSessionId() != null) {
                // Create todo item with related review session
                try {
                    // Get the session object
                    org.example.docvideoplay.entity.ReviewSession relatedSession = reviewService.getSessionById(params.getRelatedSessionId());
                    todoItem = todoService.createTodoItem(
                        params.getTitle(),
                        params.getDescription(),
                        params.getDueDate(),
                        params.getType(),
                        relatedSession,
                        currentUserId
                    );
                } catch (IllegalArgumentException e) {
                    logger.warn("Related review session not found: id={}", params.getRelatedSessionId());
                    return ResponseEntity.badRequest().build();
                }
            } else {
                // Create todo item without related card or session
                todoItem = todoService.createTodoItem(
                    params.getTitle(),
                    params.getDescription(),
                    params.getDueDate(),
                    params.getType(),
                    currentUserId
                );
            }
            
            TodoItemResultDto result = convertToResultDto(todoItem);
            
            logger.info("Todo item created successfully: id={}, userId={}", todoItem.getId(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (Exception e) {
            logger.error("Error creating todo item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> getTodoItem(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving todo item: id={}, userId={}", id, currentUserId);
            
            // Find the todo item by searching through all items
            List<TodoItem> allItems = new ArrayList<>();
            allItems.addAll(todoService.getAllIncompleteTodoItems(currentUserId));
            allItems.addAll(todoService.getAllCompletedTodoItems(currentUserId));
            
            TodoItem todoItem = allItems.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (todoItem == null) {
                logger.warn("Todo item not found: id={}", id);
                return ResponseEntity.notFound().build();
            }
            
            TodoItemResultDto result = convertToResultDto(todoItem);
            
            logger.debug("Retrieved todo item: id={}, title={}, userId={}", id, todoItem.getTitle(), currentUserId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error retrieving todo item {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TodoItemResultDto> updateTodoItem(Long id, @Valid TodoItemParamsDto params) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Updating todo item: id={}, userId={}", id, currentUserId);
            
            TodoItem updatedItem = todoService.updateTodoItem(
                id,
                params.getTitle(),
                params.getDescription(),
                params.getDueDate(),
                currentUserId
            );
            
            TodoItemResultDto result = convertToResultDto(updatedItem);
            
            logger.info("Todo item updated successfully: id={}, userId={}", id, currentUserId);
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
            Long currentUserId = getCurrentUserId();
            logger.info("Completing todo item: id={}, userId={}", id, currentUserId);
            
            TodoItem completedItem = todoService.completeTodoItem(id, currentUserId);
            TodoItemResultDto result = convertToResultDto(completedItem);
            
            logger.info("Todo item completed successfully: id={}, userId={}", id, currentUserId);
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
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting todo item: id={}, userId={}", id, currentUserId);
            
            todoService.deleteTodoItem(id, currentUserId);
            
            logger.info("Todo item deleted successfully: id={}, userId={}", id, currentUserId);
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
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving todo items due today for userId: {}", currentUserId);
            
            List<TodoItem> todoItems = todoService.getTodoItemsDueToday(currentUserId);
            List<TodoItemResultDto> results = todoItems.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} todo items due today for userId: {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error retrieving todo items due today: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<TodoItemResultDto>> getOverdueTodoItems() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving overdue todo items for userId: {}", currentUserId);
            
            List<TodoItem> todoItems = todoService.getOverdueTodoItems(currentUserId);
            List<TodoItemResultDto> results = todoItems.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} overdue todo items for userId: {}", results.size(), currentUserId);
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
        
        // Set related card information
        if (todoItem.getRelatedCardId() != null) {
            dto.setRelatedCardId(todoItem.getRelatedCardId());
            try {
                // Get card details to set card text
                org.example.docvideoplay.entity.Card card = vocabularyService.getCardById(todoItem.getRelatedCardId(), todoItem.getUserId());
                dto.setRelatedCardText(card.getText());
            } catch (Exception e) {
                // Ignore if card not found
                logger.warn("Related card not found for todo item: cardId={}", todoItem.getRelatedCardId());
            }
        }
        
        // Set related session information
        if (todoItem.getRelatedSessionId() != null) {
            dto.setRelatedSessionId(todoItem.getRelatedSessionId());
        }
        
        // Set status flags
        dto.setOverdue(todoItem.isOverdue());
        dto.setDueToday(todoItem.isDueToday());
        
        return dto;
    }
}