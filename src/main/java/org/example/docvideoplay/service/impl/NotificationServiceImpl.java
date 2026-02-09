package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.config.EnglishLearningConfig;
import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.enums.TodoType;
import org.example.docvideoplay.service.NotificationService;
import org.example.docvideoplay.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService for managing notifications and alerts.
 * Handles notification triggering, batching, and user preference management.
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final TodoService todoService;
    private final EnglishLearningConfig config;
    
    // In-memory storage for user preferences (in a real application, this would be persisted)
    private NotificationPreferences userPreferences;
    
    // In-memory storage for pending notifications (in a real application, this would be persisted)
    private final List<NotificationDto> pendingNotifications = new ArrayList<>();
    
    @Autowired
    public NotificationServiceImpl(TodoService todoService, EnglishLearningConfig config) {
        this.todoService = todoService;
        this.config = config;
        this.userPreferences = createDefaultPreferences();
    }
    
    private NotificationPreferences createDefaultPreferences() {
        NotificationPreferences preferences = new NotificationPreferences();
        preferences.setEnabled(config.getNotifications().isEnabled());
        preferences.setBatchSize(config.getNotifications().getBatchSize());
        preferences.setReminderHours(new ArrayList<>(config.getNotifications().getReminderHours()));
        return preferences;
    }
    
    @Override
    public List<NotificationDto> sendDueReviewNotifications() {
        if (!areNotificationsEnabled()) {
            logger.debug("Notifications are disabled, skipping due review notifications");
            return new ArrayList<>();
        }
        
        List<TodoItem> dueToday = todoService.getTodoItemsDueToday();
        List<NotificationDto> sentNotifications = new ArrayList<>();
        
        if (dueToday.isEmpty()) {
            logger.debug("No todo items due today");
            return sentNotifications;
        }
        
        // Filter for review sessions
        List<TodoItem> dueReviews = dueToday.stream()
                .filter(todo -> todo.getType() == TodoType.REVIEW_SESSION)
                .collect(Collectors.toList());
        
        if (dueReviews.isEmpty()) {
            logger.debug("No review sessions due today");
            return sentNotifications;
        }
        
        // If there are multiple reviews, batch them
        if (dueReviews.size() > 1 && dueReviews.size() <= userPreferences.getBatchSize()) {
            NotificationDto batchedNotification = sendBatchedNotification(dueReviews);
            sentNotifications.add(batchedNotification);
        } else {
            // Send individual notifications
            for (TodoItem todoItem : dueReviews) {
                NotificationDto notification = sendTodoItemNotification(todoItem);
                sentNotifications.add(notification);
            }
        }
        
        logger.info("Sent {} due review notifications", sentNotifications.size());
        return sentNotifications;
    }
    
    @Override
    public List<NotificationDto> sendOverdueReviewAlerts() {
        if (!areNotificationsEnabled()) {
            logger.debug("Notifications are disabled, skipping overdue review alerts");
            return new ArrayList<>();
        }
        
        List<TodoItem> overdueTodos = todoService.getOverdueTodoItems();
        List<NotificationDto> sentNotifications = new ArrayList<>();
        
        if (overdueTodos.isEmpty()) {
            logger.debug("No overdue todo items");
            return sentNotifications;
        }
        
        // Filter for review sessions
        List<TodoItem> overdueReviews = overdueTodos.stream()
                .filter(todo -> todo.getType() == TodoType.REVIEW_SESSION)
                .collect(Collectors.toList());
        
        if (overdueReviews.isEmpty()) {
            logger.debug("No overdue review sessions");
            return sentNotifications;
        }
        
        // Create alert notifications for overdue reviews
        for (TodoItem todoItem : overdueReviews) {
            NotificationDto notification = createOverdueAlert(todoItem);
            pendingNotifications.add(notification);
            sentNotifications.add(notification);
        }
        
        logger.warn("Sent {} overdue review alerts", sentNotifications.size());
        return sentNotifications;
    }
    
    @Override
    public NotificationDto sendBatchedNotification(List<TodoItem> todoItems) {
        if (todoItems.isEmpty()) {
            return null;
        }
        
        int reviewCount = todoItems.size();
        String title = String.format("You have %d reviews due today", reviewCount);
        
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Time for your vocabulary review! You have ")
                     .append(reviewCount)
                     .append(" items to review:\n\n");
        
        for (int i = 0; i < Math.min(todoItems.size(), 5); i++) {
            TodoItem todo = todoItems.get(i);
            messageBuilder.append("• ").append(todo.getTitle()).append("\n");
        }
        
        if (todoItems.size() > 5) {
            messageBuilder.append("• ... and ").append(todoItems.size() - 5).append(" more");
        }
        
        NotificationDto notification = new NotificationDto(title, messageBuilder.toString(), "BATCH_REVIEW", LocalDate.now());
        pendingNotifications.add(notification);
        
        logger.info("Sent batched notification for {} review items", reviewCount);
        return notification;
    }
    
    @Override
    public NotificationDto sendTodoItemNotification(TodoItem todoItem) {
        String title = "Review Due: " + todoItem.getTitle();
        String message = "It's time to review your vocabulary!\n\n" + todoItem.getDescription();
        
        NotificationDto notification = new NotificationDto(title, message, "SINGLE_REVIEW", todoItem.getDueDate());
        notification.setRelatedTodoId(todoItem.getId());
        
        if (todoItem.getRelatedCardId() != null) {
            notification.setRelatedCardId(todoItem.getRelatedCardId());
        }
        
        pendingNotifications.add(notification);
        
        logger.info("Sent notification for todo item: {}", todoItem.getTitle());
        return notification;
    }
    
    private NotificationDto createOverdueAlert(TodoItem todoItem) {
        String title = "Overdue Review: " + todoItem.getTitle();
        String message = "This review is overdue! Don't let your vocabulary skills fade.\n\n" + todoItem.getDescription();
        
        NotificationDto notification = new NotificationDto(title, message, "OVERDUE_ALERT", todoItem.getDueDate());
        notification.setRelatedTodoId(todoItem.getId());
        
        if (todoItem.getRelatedCardId() != null) {
            notification.setRelatedCardId(todoItem.getRelatedCardId());
        }
        
        return notification;
    }
    
    @Override
    public NotificationPreferences getUserNotificationPreferences() {
        return userPreferences;
    }
    
    @Override
    public NotificationPreferences updateNotificationPreferences(NotificationPreferences preferences) {
        this.userPreferences = preferences;
        logger.info("Updated notification preferences: enabled={}, batchSize={}", 
                   preferences.isEnabled(), preferences.getBatchSize());
        return this.userPreferences;
    }
    
    @Override
    public boolean areNotificationsEnabled() {
        return userPreferences != null && userPreferences.isEnabled();
    }
    
    @Override
    public List<NotificationDto> getPendingNotifications() {
        return new ArrayList<>(pendingNotifications);
    }
    
    @Override
    public void markNotificationAsRead(Long notificationId) {
        // In a real implementation, this would mark the notification as read in the database
        // For now, we'll just log it
        logger.info("Marked notification {} as read", notificationId);
    }
    
    @Override
    public void clearAllNotifications() {
        pendingNotifications.clear();
        logger.info("Cleared all pending notifications");
    }
    
    @Override
    public void scheduleDailyNotificationCheck() {
        logger.info("Running daily notification check");
        
        // Send due review notifications
        List<NotificationDto> dueNotifications = sendDueReviewNotifications();
        
        // Send overdue alerts
        List<NotificationDto> overdueAlerts = sendOverdueReviewAlerts();
        
        logger.info("Daily notification check completed: {} due notifications, {} overdue alerts", 
                   dueNotifications.size(), overdueAlerts.size());
    }
    
    @Override
    public NotificationStatistics getNotificationStatistics() {
        List<TodoItem> dueToday = todoService.getTodoItemsDueToday();
        List<TodoItem> overdue = todoService.getOverdueTodoItems();
        List<TodoItem> upcoming = todoService.getTodoItemsDueInNextDays(7);
        long completed = todoService.countTodoItemsByStatus(true);
        
        // Filter for review sessions only
        long dueTodayCount = dueToday.stream()
                .filter(todo -> todo.getType() == TodoType.REVIEW_SESSION)
                .count();
        
        long overdueCount = overdue.stream()
                .filter(todo -> todo.getType() == TodoType.REVIEW_SESSION)
                .count();
        
        long upcomingCount = upcoming.stream()
                .filter(todo -> todo.getType() == TodoType.REVIEW_SESSION)
                .filter(todo -> todo.getDueDate() != null && todo.getDueDate().isAfter(LocalDate.now()))
                .count();
        
        return new NotificationStatistics(dueTodayCount, overdueCount, upcomingCount, completed);
    }
}