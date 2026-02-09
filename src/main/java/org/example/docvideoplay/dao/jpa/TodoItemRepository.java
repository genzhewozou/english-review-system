package org.example.docvideoplay.dao.jpa;

import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.enums.TodoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository for TodoItem entity
 * Provides data access methods for todo list management and due date queries
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    
    /**
     * Find all incomplete todo items ordered by due date (earliest first)
     * @return list of incomplete todo items ordered by due date
     */
    List<TodoItem> findByCompletedFalseOrderByDueDateAsc();
    
    /**
     * Find all completed todo items ordered by completion date (newest first)
     * @return list of completed todo items
     */
    List<TodoItem> findByCompletedTrueOrderByUpdatedDateDesc();
    
    /**
     * Find todo items by type and completion status
     * @param type the todo item type
     * @param completed the completion status
     * @return list of todo items matching the criteria
     */
    List<TodoItem> findByTypeAndCompletedOrderByDueDateAsc(TodoType type, Boolean completed);
    
    /**
     * Find todo items due on or before a specific date (incomplete only)
     * @param date the due date threshold
     * @return list of todo items due on or before the specified date
     */
    List<TodoItem> findByCompletedFalseAndDueDateLessThanEqualOrderByDueDateAsc(LocalDate date);
    
    /**
     * Find overdue todo items (past due date and not completed)
     * @return list of overdue todo items
     */
    @Query("SELECT t FROM TodoItem t WHERE t.completed = false AND t.dueDate < CURRENT_DATE ORDER BY t.dueDate ASC")
    List<TodoItem> findOverdueTodoItems();
    
    /**
     * Find todo items due today (not completed)
     * @return list of todo items due today
     */
    @Query("SELECT t FROM TodoItem t WHERE t.completed = false AND t.dueDate = CURRENT_DATE ORDER BY t.createdDate ASC")
    List<TodoItem> findTodoItemsDueToday();
    
    /**
     * Find todo items due in the next N days
     * @param days number of days to look ahead
     * @return list of todo items due in the specified period
     */
    @Query("SELECT t FROM TodoItem t WHERE t.completed = false AND t.dueDate BETWEEN CURRENT_DATE AND :endDate ORDER BY t.dueDate ASC")
    List<TodoItem> findTodoItemsDueInNextDays(@Param("endDate") LocalDate endDate);
    
    /**
     * Find todo items by related card ID
     * @param cardId the ID of the related card
     * @return list of todo items related to the card
     */
    List<TodoItem> findByRelatedCardIdOrderByDueDateAsc(Long cardId);
    
    /**
     * Find todo items with no due date (incomplete only)
     * @return list of todo items without due dates
     */
    List<TodoItem> findByCompletedFalseAndDueDateIsNullOrderByCreatedDateAsc();
    
    /**
     * Count todo items by completion status
     * @param completed the completion status
     * @return count of todo items with the specified completion status
     */
    long countByCompleted(Boolean completed);
    
    /**
     * Count todo items by type
     * @param type the todo item type
     * @return count of todo items of the specified type
     */
    long countByType(TodoType type);
    
    /**
     * Find todo items by title containing search term (case insensitive)
     * @param title the search term for title
     * @return list of todo items matching the title search
     */
    List<TodoItem> findByTitleContainingIgnoreCaseOrderByDueDateAsc(String title);
    
    /**
     * Find todo items by user ID
     * @param userId the user ID
     * @return list of todo items
     */
    List<TodoItem> findByUserId(Long userId);
    
    /**
     * Find todo items by user ID and completion status
     * @param userId the user ID
     * @param completed completion status
     * @return list of todo items
     */
    List<TodoItem> findByUserIdAndCompleted(Long userId, boolean completed);
    
    /**
     * Find todo items by user ID and due date before or equal to specified date
     * @param userId the user ID
     * @param date comparison date
     * @return list of todo items
     */
    List<TodoItem> findByUserIdAndDueDateLessThanEqual(Long userId, LocalDate date);
    
    /**
     * Find todo items by user ID and due date before specified date
     * @param userId the user ID
     * @param date comparison date
     * @return list of todo items
     */
    List<TodoItem> findByUserIdAndDueDateBefore(Long userId, LocalDate date);
    
    /**
     * Find todo items by user ID and type
     * @param userId the user ID
     * @param type todo type
     * @return list of todo items
     */
    List<TodoItem> findByUserIdAndType(Long userId, TodoType type);
    
    /**
     * Find todo items by user ID, type, and completion status
     * @param userId the user ID
     * @param type todo type
     * @param completed completion status
     * @return list of todo items
     */
    List<TodoItem> findByUserIdAndTypeAndCompleted(Long userId, TodoType type, boolean completed);
    
    /**
     * Find todo items by user ID and related card ID, ordered by due date
     * @param userId the user ID
     * @param cardId the card ID
     * @return list of todo items
     */
    List<TodoItem> findByUserIdAndRelatedCardIdOrderByDueDateAsc(Long userId, Long cardId);
    
    /**
     * Find all incomplete todo items for a user ID ordered by due date (earliest first)
     * @param userId the user ID
     * @return list of incomplete todo items ordered by due date
     */
    List<TodoItem> findByUserIdAndCompletedFalseOrderByDueDateAsc(Long userId);
    
    /**
     * Find all completed todo items for a user ID ordered by completion date (newest first)
     * @param userId the user ID
     * @return list of completed todo items
     */
    List<TodoItem> findByUserIdAndCompletedTrueOrderByUpdatedDateDesc(Long userId);
    
    /**
     * Find todo items by user ID, type and completion status
     * @param userId the user ID
     * @param type the todo item type
     * @param completed the completion status
     * @return list of todo items matching the criteria
     */
    List<TodoItem> findByUserIdAndTypeAndCompletedOrderByDueDateAsc(Long userId, TodoType type, Boolean completed);
    
    /**
     * Find todo items due on or before a specific date for a user ID (incomplete only)
     * @param userId the user ID
     * @param date the due date threshold
     * @return list of todo items due on or before the specified date
     */
    List<TodoItem> findByUserIdAndCompletedFalseAndDueDateLessThanEqualOrderByDueDateAsc(Long userId, LocalDate date);
    
    /**
     * Find overdue todo items for a user ID (past due date and not completed)
     * @param userId the user ID
     * @return list of overdue todo items
     */
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate < CURRENT_DATE ORDER BY t.dueDate ASC")
    List<TodoItem> findOverdueTodoItems(@Param("userId") Long userId);
    
    /**
     * Find todo items due today for a user ID (not completed)
     * @param userId the user ID
     * @return list of todo items due today
     */
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate = CURRENT_DATE ORDER BY t.createdDate ASC")
    List<TodoItem> findTodoItemsDueToday(@Param("userId") Long userId);
    
    /**
     * Find todo items due in the next N days for a user ID
     * @param userId the user ID
     * @param endDate the end date of the period
     * @return list of todo items due in the specified period
     */
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate BETWEEN CURRENT_DATE AND :endDate ORDER BY t.dueDate ASC")
    List<TodoItem> findTodoItemsDueInNextDays(@Param("userId") Long userId, @Param("endDate") LocalDate endDate);
    
    /**
     * Find todo items with no due date for a user ID (incomplete only)
     * @param userId the user ID
     * @return list of todo items without due dates
     */
    List<TodoItem> findByUserIdAndCompletedFalseAndDueDateIsNullOrderByCreatedDateAsc(Long userId);
    
    /**
     * Count todo items by user ID and completion status
     * @param userId the user ID
     * @param completed the completion status
     * @return count of todo items with the specified completion status
     */
    long countByUserIdAndCompleted(Long userId, Boolean completed);
    
    /**
     * Count todo items by user ID and type
     * @param userId the user ID
     * @param type the todo item type
     * @return count of todo items of the specified type
     */
    long countByUserIdAndType(Long userId, TodoType type);
    
    /**
     * Find todo items due between two dates (inclusive)
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of todo items due in the date range
     */
    @Query("SELECT t FROM TodoItem t WHERE t.completed = false AND t.dueDate BETWEEN :startDate AND :endDate ORDER BY t.dueDate ASC")
    List<TodoItem> findTodoItemsDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find review session todo items (for spaced repetition scheduling)
     * @return list of review session todo items
     */
    @Query("SELECT t FROM TodoItem t WHERE t.type = 'REVIEW_SESSION' AND t.completed = false ORDER BY t.dueDate ASC")
    List<TodoItem> findReviewSessionTodoItems();
    
    /**
     * Count overdue todo items
     * @return count of overdue todo items
     */
    @Query("SELECT COUNT(t) FROM TodoItem t WHERE t.completed = false AND t.dueDate < CURRENT_DATE")
    long countOverdueTodoItems();
    
    // User ID-based queries
    List<TodoItem> findByUserIdOrderByDueDateAsc(Long userId);
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate < CURRENT_DATE ORDER BY t.dueDate ASC")
    List<TodoItem> findOverdueTodoItemsByUserId(@Param("userId") Long userId);
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate = CURRENT_DATE ORDER BY t.createdDate ASC")
    List<TodoItem> findTodoItemsDueTodayByUserId(@Param("userId") Long userId);
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate BETWEEN CURRENT_DATE AND :endDate ORDER BY t.dueDate ASC")
    List<TodoItem> findTodoItemsDueInNextDaysByUserId(@Param("userId") Long userId, @Param("endDate") LocalDate endDate);
    List<TodoItem> findByUserIdAndTitleContainingIgnoreCaseOrderByDueDateAsc(Long userId, String title);
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate BETWEEN :startDate AND :endDate ORDER BY t.dueDate ASC")
    List<TodoItem> findTodoItemsDueBetweenByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.type = 'REVIEW_SESSION' AND t.completed = false ORDER BY t.dueDate ASC")
    List<TodoItem> findReviewSessionTodoItemsByUserId(@Param("userId") Long userId);
    @Query("SELECT COUNT(t) FROM TodoItem t WHERE t.userId = :userId AND t.completed = false AND t.dueDate < CURRENT_DATE")
    long countOverdueTodoItemsByUserId(@Param("userId") Long userId);
}