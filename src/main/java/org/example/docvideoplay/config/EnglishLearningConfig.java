package org.example.docvideoplay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "english-learning")
public class EnglishLearningConfig {
    
    private SpacedRepetition spacedRepetition = new SpacedRepetition();
    private Notifications notifications = new Notifications();
    
    public static class SpacedRepetition {
        private int initialIntervalDays = 1;
        private double initialEaseFactor = 2.5;
        private double minimumEaseFactor = 1.3;
        private double maximumEaseFactor = 2.5;
        private boolean fiveDayReminder = true;
        
        // Getters and Setters
        public int getInitialIntervalDays() {
            return initialIntervalDays;
        }
        
        public void setInitialIntervalDays(int initialIntervalDays) {
            this.initialIntervalDays = initialIntervalDays;
        }
        
        public double getInitialEaseFactor() {
            return initialEaseFactor;
        }
        
        public void setInitialEaseFactor(double initialEaseFactor) {
            this.initialEaseFactor = initialEaseFactor;
        }
        
        public double getMinimumEaseFactor() {
            return minimumEaseFactor;
        }
        
        public void setMinimumEaseFactor(double minimumEaseFactor) {
            this.minimumEaseFactor = minimumEaseFactor;
        }
        
        public double getMaximumEaseFactor() {
            return maximumEaseFactor;
        }
        
        public void setMaximumEaseFactor(double maximumEaseFactor) {
            this.maximumEaseFactor = maximumEaseFactor;
        }
        
        public boolean isFiveDayReminder() {
            return fiveDayReminder;
        }
        
        public void setFiveDayReminder(boolean fiveDayReminder) {
            this.fiveDayReminder = fiveDayReminder;
        }
    }
    
    public static class Notifications {
        private boolean enabled = true;
        private int batchSize = 10;
        private List<Integer> reminderHours = Arrays.asList(9, 14, 19);
        
        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public List<Integer> getReminderHours() {
            return reminderHours;
        }
        
        public void setReminderHours(List<Integer> reminderHours) {
            this.reminderHours = reminderHours;
        }
    }
    
    // Getters and Setters
    public SpacedRepetition getSpacedRepetition() {
        return spacedRepetition;
    }
    
    public void setSpacedRepetition(SpacedRepetition spacedRepetition) {
        this.spacedRepetition = spacedRepetition;
    }
    
    public Notifications getNotifications() {
        return notifications;
    }
    
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }
}