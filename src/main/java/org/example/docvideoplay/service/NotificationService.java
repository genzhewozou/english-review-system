package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.TodoItem;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing notifications and alerts for due reviews and todo items.
 * Handles notification triggering, batching, and user preference management.
 */
public interface NotificationService {
    
    /**
     * Notification data transfer object for sending notifications.
     */
    class NotificationDto {
        private String title;
        private String message;
        private String type;
        private LocalDate dueDate;
        private Long relatedTodoId;
        private Long relatedHighlightId;
        
        public NotificationDto() {}
        
        public NotificationDto(String title, String message, String type, LocalDate dueDate) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.dueDate = dueDate;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        
        public Long getRelatedTodoId() { return relatedTodoId; }
        public void setRelatedTodoId(Long relatedTodoId) { this.relatedTodoId = relatedTodoId; }
        
        public Long getRelatedHighlightId() { return relatedHighlightId; }
        public void setRelatedHighlightId(Long relatedHighlightId) { this.relatedHighlightId = relatedHighlightId; }
    }
    
    /**
     * User notification preferences.
     */
    class NotificationPreferences {
        private boolean enabled = true;
        private boolean emailNotifications = false;
        private boolean pushNotifications = true;
        private List<Integer> reminderHours = Arrays.asList(9, 14, 19);
        private int batchSize = 10;
        
        public NotificationPreferences() {}
        
        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
        
        public boolean isPushNotifications() { return pushNotifications; }
        public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }
        
        public List<Integer> getReminderHours() { return reminderHours; }
        public void setReminderHours(List<Integer> reminderHours) { this.reminderHours = reminderHours; }
        
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    }
    
    /**
     * Send notifications for due review sessions.
     * This method checks for due reviews and sends appropriate notifications.
     * 
     * @return List of notifications that were sent
     */
    List<NotificationDto> sendDueReviewNotifications();
    
    /**
     * Send notifications for overdue review sessions.
     * This method checks for overdue reviews and sends alert notifications.
     * 
     * @return List of notifications that were sent
     */
    List<NotificationDto> sendOverdueReviewAlerts();
    
    /**
     * Send a batch of notifications for multiple due items.
     * Groups multiple due reviews into a single notification when appropriate.
     * 
     * @param todoItems List of todo items to notify about
     * @return The batched notification that was sent
     */
    NotificationDto sendBatchedNotification(List<TodoItem> todoItems);
    
    /**
     * Send a notification for a specific todo item.
     * 
     * @param todoItem The todo item to send notification for
     * @return The notification that was sent
     */
    NotificationDto sendTodoItemNotification(TodoItem todoItem);
    
    /**
     * Get the current user's notification preferences.
     * 
     * @return The user's notification preferences
     */
    NotificationPreferences getUserNotificationPreferences();
    
    /**
     * Update the user's notification preferences.
     * 
     * @param preferences The new notification preferences
     * @return The updated preferences
     */
    NotificationPreferences updateNotificationPreferences(NotificationPreferences preferences);
    
    /**
     * Check if notifications are enabled for the current user.
     * 
     * @return true if notifications are enabled, false otherwise
     */
    boolean areNotificationsEnabled();
    
    /**
     * Get all pending notifications for the current user.
     * 
     * @return List of pending notifications
     */
    List<NotificationDto> getPendingNotifications();
    
    /**
     * Mark a notification as read/acknowledged.
     * 
     * @param notificationId The ID of the notification to mark as read
     */
    void markNotificationAsRead(Long notificationId);
    
    /**
     * Clear all notifications for the current user.
     */
    void clearAllNotifications();
    
    /**
     * Schedule daily notification checks.
     * This method is typically called by a scheduled job to check for due reviews.
     */
    void scheduleDailyNotificationCheck();
    
    /**
     * Get notification statistics for the current user.
     * 
     * @return Statistics about notifications (count of due, overdue, etc.)
     */
    NotificationStatistics getNotificationStatistics();
    
    /**
     * Notification statistics data transfer object.
     */
    class NotificationStatistics {
        private long totalDueToday;
        private long totalOverdue;
        private long totalUpcoming;
        private long totalCompleted;
        
        public NotificationStatistics() {}
        
        public NotificationStatistics(long totalDueToday, long totalOverdue, long totalUpcoming, long totalCompleted) {
            this.totalDueToday = totalDueToday;
            this.totalOverdue = totalOverdue;
            this.totalUpcoming = totalUpcoming;
            this.totalCompleted = totalCompleted;
        }
        
        // Getters and Setters
        public long getTotalDueToday() { return totalDueToday; }
        public void setTotalDueToday(long totalDueToday) { this.totalDueToday = totalDueToday; }
        
        public long getTotalOverdue() { return totalOverdue; }
        public void setTotalOverdue(long totalOverdue) { this.totalOverdue = totalOverdue; }
        
        public long getTotalUpcoming() { return totalUpcoming; }
        public void setTotalUpcoming(long totalUpcoming) { this.totalUpcoming = totalUpcoming; }
        
        public long getTotalCompleted() { return totalCompleted; }
        public void setTotalCompleted(long totalCompleted) { this.totalCompleted = totalCompleted; }
    }
}