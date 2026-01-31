package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.Highlight;
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
     * Create a todo item linked to a specific highlight for review scheduling.
     * 
     * @param title The title of the todo item
     * @param description The description of the todo item
     * @param dueDate The due date for the todo item
     * @param type The type of todo item
     * @param relatedHighlight The highlight this todo item is related to
     * @return The created todo item
     */
    TodoItem createTodoItem(String title, String description, LocalDate dueDate, TodoType type, Highlight relatedHighlight);
    
    /**
     * Get all incomplete todo items ordered by due date.
     * 
     * @return List of incomplete todo items
     */
    List<TodoItem> getAllIncompleteTodoItems();
    
    /**
     * Get all completed todo items ordered by completion date.
     * 
     * @return List of completed todo items
     */
    List<TodoItem> getAllCompletedTodoItems();
    
    /**
     * Get todo items by type and completion status.
     * 
     * @param type The todo item type
     * @param completed The completion status
     * @return List of todo items matching the criteria
     */
    List<TodoItem> getTodoItemsByType(TodoType type, Boolean completed);
    
    /**
     * Get todo items that are due today.
     * 
     * @return List of todo items due today
     */
    List<TodoItem> getTodoItemsDueToday();
    
    /**
     * Get todo items that are overdue.
     * 
     * @return List of overdue todo items
     */
    List<TodoItem> getOverdueTodoItems();
    
    /**
     * Get todo items due in the next specified number of days.
     * 
     * @param days Number of days to look ahead
     * @return List of todo items due in the specified period
     */
    List<TodoItem> getTodoItemsDueInNextDays(int days);
    
    /**
     * Mark a todo item as completed.
     * 
     * @param todoItemId The ID of the todo item to complete
     * @return The updated todo item
     */
    TodoItem completeTodoItem(Long todoItemId);
    
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
     * Delete a todo item.
     * 
     * @param todoItemId The ID of the todo item to delete
     */
    void deleteTodoItem(Long todoItemId);
    
    /**
     * Schedule a review reminder for a highlight based on its next review date.
     * This method automatically creates a todo item for the review session.
     * 
     * @param highlight The highlight to schedule a review for
     */
    void scheduleReviewReminder(Highlight highlight);
    
    /**
     * Synchronize todo items with completed review sessions.
     * This method marks review-related todo items as complete when their
     * associated highlights have been reviewed.
     * 
     * @param highlight The highlight that was reviewed
     */
    void synchronizeWithReviewCompletion(Highlight highlight);
    
    /**
     * Get todo items related to a specific highlight.
     * 
     * @param highlightId The ID of the highlight
     * @return List of todo items related to the highlight
     */
    List<TodoItem> getTodoItemsByHighlight(Long highlightId);
    
    /**
     * Count todo items by completion status.
     * 
     * @param completed The completion status
     * @return Count of todo items with the specified status
     */
    long countTodoItemsByStatus(Boolean completed);
    
    /**
     * Count overdue todo items.
     * 
     * @return Count of overdue todo items
     */
    long countOverdueTodoItems();
    
    /**
     * Search todo items by title.
     * 
     * @param searchTerm The search term for title
     * @return List of todo items matching the search
     */
    List<TodoItem> searchTodoItemsByTitle(String searchTerm);
    
    /**
     * Get todo items due between two dates.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of todo items due in the date range
     */
    List<TodoItem> getTodoItemsDueBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all review session todo items (for spaced repetition).
     * 
     * @return List of review session todo items
     */
    List<TodoItem> getReviewSessionTodoItems();
}