package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.ReviewSession;
import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.enums.TodoType;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing todo items and scheduling review reminders.
 * Handles automatic scheduling for spaced repetition and custom task management.
 */
public interface TodoService {
    
    /**
     * Create a new todo item with the specified details.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item (REVIEW_SESSION or CUSTOM_TASK)
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type);
    
    /**
     * Create a new todo item for a specific user with the specified details.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item (REVIEW_SESSION or CUSTOM_TASK)
     * @param userId The ID of the user who owns the todo item
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Long userId);
    
    /**
     * Create a todo item linked to a specific card for review scheduling.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item
     * @param relatedCard The card this todo item is related to
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Card relatedCard);
    
    /**
     * Create a todo item linked to a specific card for review scheduling for a specific user.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item
     * @param relatedCard The card this todo item is related to
     * @param userId The ID of the user who owns the todo item
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Card relatedCard, Long userId);
    
    /**
     * Create a todo item linked to a specific review session.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item
     * @param relatedSession The review session this todo item is related to
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, ReviewSession relatedSession);
    
    /**
     * Create a todo item linked to a specific review session for a specific user.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item
     * @param relatedSession The review session this todo item is related to
     * @param userId The ID of the user who owns the todo item
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, ReviewSession relatedSession, Long userId);
    
    /**
     * Get all incomplete todo items ordered by due date.
     * 
     * @return List of incomplete todo items
     */
    List<TodoItem> getAllIncompleteTodoItems();
    
    /**
     * Get all incomplete todo items for a specific user ordered by due date.
     * 
     * @param userId The ID of the user who owns the todo items
     * @return List of incomplete todo items
     */
    List<TodoItem> getAllIncompleteTodoItems(Long userId);
    
    /**
     * Get all completed todo items ordered by completion date.
     * 
     * @return List of completed todo items
     */
    List<TodoItem> getAllCompletedTodoItems();
    
    /**
     * Get all completed todo items for a specific user ordered by completion date.
     * 
     * @param userId The ID of the user who owns the todo items
     * @return List of completed todo items
     */
    List<TodoItem> getAllCompletedTodoItems(Long userId);
    
    /**
     * Get todo items by type and completion status.
     * 
     * @param type The todo item type
     * @param completed The completion status
     * @return List of todo items matching the criteria
     */
    List<TodoItem> getTodoItemsByType(TodoType type, Boolean completed);
    
    /**
     * Get todo items by type and completion status for a specific user.
     * 
     * @param type The todo item type
     * @param completed The completion status
     * @param userId The ID of the user who owns the todo items
     * @return List of todo items matching the criteria
     */
    List<TodoItem> getTodoItemsByType(TodoType type, Boolean completed, Long userId);
    
    /**
     * Get todo items that are due today.
     * 
     * @return List of todo items due today
     */
    List<TodoItem> getTodoItemsDueToday();
    
    /**
     * Get todo items that are due today for a specific user.
     * 
     * @param userId The ID of the user who owns the todo items
     * @return List of todo items due today
     */
    List<TodoItem> getTodoItemsDueToday(Long userId);
    
    /**
     * Get todo items that are overdue.
     * 
     * @return List of overdue todo items
     */
    List<TodoItem> getOverdueTodoItems();
    
    /**
     * Get todo items that are overdue for a specific user.
     * 
     * @param userId The ID of the user who owns the todo items
     * @return List of overdue todo items
     */
    List<TodoItem> getOverdueTodoItems(Long userId);
    
    /**
     * Get todo items due in the next specified number of days.
     * 
     * @param days Number of days to look ahead
     * @return List of todo items due in the specified period
     */
    List<TodoItem> getTodoItemsDueInNextDays(int days);
    
    /**
     * Get todo items due in the next specified number of days for a specific user.
     * 
     * @param days Number of days to look ahead
     * @param userId The ID of the user who owns the todo items
     * @return List of todo items due in the specified period
     */
    List<TodoItem> getTodoItemsDueInNextDays(int days, Long userId);
    
    /**
     * Mark a todo item as completed.
     * 
     * @param todoItemId The ID of the todo item to complete
     * @return The updated todo item
     */
    TodoItem completeTodoItem(Long todoItemId);
    
    /**
     * Mark a todo item as completed for a specific user.
     * 
     * @param todoItemId The ID of the todo item to complete
     * @param userId The ID of the user who owns the todo item
     * @return The updated todo item
     */
    TodoItem completeTodoItem(Long todoItemId, Long userId);
    
    /**
     * Update an existing todo item.
     * 
     * @param todoItemId The ID of the todo item to update
     * @param title The new title
     * @param description The new description
     * @param dueDate The new due date
     * @return The updated todo item
     */
    TodoItem updateTodoItem(Long todoItemId, String title, String description, LocalDate dueDate);
    
    /**
     * Update an existing todo item for a specific user.
     * 
     * @param todoItemId The ID of the todo item to update
     * @param title The new title
     * @param description The new description
     * @param dueDate The new due date
     * @param userId The ID of the user who owns the todo item
     * @return The updated todo item
     */
    TodoItem updateTodoItem(Long todoItemId, String title, String description, LocalDate dueDate, Long userId);
    
    /**
     * Delete a todo item.
     * 
     * @param todoItemId The ID of the todo item to delete
     */
    void deleteTodoItem(Long todoItemId);
    
    /**
     * Delete a todo item for a specific user.
     * 
     * @param todoItemId The ID of the todo item to delete
     * @param userId The ID of the user who owns the todo item
     */
    void deleteTodoItem(Long todoItemId, Long userId);
    
    /**
     * Schedule a review reminder for a card based on its next review date.
     * This method automatically creates a todo item for the review session.
     * 
     * @param card The card to schedule a review for
     */
    void scheduleReviewReminder(Card card);
    
    /**
     * Synchronize todo items with completed review sessions.
     * This method marks review-related todo items as complete when their
     * associated cards have been reviewed.
     * 
     * @param card The card that was reviewed
     */
    void synchronizeWithReviewCompletion(Card card);
    
    /**
     * Get todo items related to a specific card.
     * 
     * @param cardId The ID of the card
     * @return List of todo items related to the card
     */
    List<TodoItem> getTodoItemsByCard(Long cardId);
    
    /**
     * Get todo items related to a specific card for a specific user.
     * 
     * @param cardId The ID of the card
     * @param userId The ID of the user who owns the todo items
     * @return List of todo items related to the card
     */
    List<TodoItem> getTodoItemsByCard(Long cardId, Long userId);
    
    /**
     * Count todo items by completion status.
     * 
     * @param completed The completion status
     * @return Count of todo items with the specified status
     */
    long countTodoItemsByStatus(Boolean completed);
    
    /**
     * Count todo items by completion status for a specific user.
     * 
     * @param completed The completion status
     * @param userId The ID of the user who owns the todo items
     * @return Count of todo items with the specified status
     */
    long countTodoItemsByStatus(Boolean completed, Long userId);
    
    /**
     * Count overdue todo items.
     * 
     * @return Count of overdue todo items
     */
    long countOverdueTodoItems();
    
    /**
     * Count overdue todo items for a specific user.
     * 
     * @param userId The ID of the user who owns the todo items
     * @return Count of overdue todo items
     */
    long countOverdueTodoItems(Long userId);
    
    /**
     * Search todo items by title.
     * 
     * @param searchTerm The search term for title
     * @return List of todo items matching the search
     */
    List<TodoItem> searchTodoItemsByTitle(String searchTerm);
    
    /**
     * Search todo items by title for a specific user.
     * 
     * @param searchTerm The search term for title
     * @param userId The ID of the user who owns the todo items
     * @return List of todo items matching the search
     */
    List<TodoItem> searchTodoItemsByTitle(String searchTerm, Long userId);
    
    /**
     * Get todo items due between two dates.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of todo items due in the date range
     */
    List<TodoItem> getTodoItemsDueBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get todo items due between two dates for a specific user.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param userId The ID of the user who owns the todo items
     * @return List of todo items due in the date range
     */
    List<TodoItem> getTodoItemsDueBetween(LocalDate startDate, LocalDate endDate, Long userId);
    
    /**
     * Get all review session todo items (for spaced repetition).
     * 
     * @return List of review session todo items
     */
    List<TodoItem> getReviewSessionTodoItems();
    
    /**
     * Get all review session todo items (for spaced repetition) for a specific user.
     * 
     * @param userId The ID of the user who owns the todo items
     * @return List of review session todo items
     */
    List<TodoItem> getReviewSessionTodoItems(Long userId);
}