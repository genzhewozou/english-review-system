package org.example.docvideoplay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    // In-memory storage for notifications (for development purposes)
    private final Map<Long, Notification> notifications = new ConcurrentHashMap<>();
    private long nextId = 1;

    // In-memory storage for user preferences (for development purposes)
    private final Map<String, Map<String, Object>> preferences = new ConcurrentHashMap<>();

    // Notification class for development purposes
    private static class Notification {
        private final long id;
        private final String type;
        private final String title;
        private final String message;
        private boolean read;
        private final String createdAt;

        public Notification(long id, String type, String title, String message) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.message = message;
            this.read = false;
            this.createdAt = new java.util.Date().toString();
        }

        public long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(new ArrayList<>(notifications.values()));
    }

    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getPreferences() {
        // Return default preferences if none exist
        Map<String, Object> defaultPrefs = new HashMap<>();
        defaultPrefs.put("enabled", true);
        defaultPrefs.put("emailNotifications", false);
        defaultPrefs.put("browserNotifications", true);
        defaultPrefs.put("reminderHours", 24);
        defaultPrefs.put("batchNotifications", true);
        defaultPrefs.put("overdueAlerts", true);
        defaultPrefs.put("dueTodayAlerts", true);
        defaultPrefs.put("reviewReminders", true);
        return ResponseEntity.ok(defaultPrefs);
    }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Object>> savePreferences(@RequestBody Map<String, Object> newPreferences) {
        // Save preferences (for development purposes)
        preferences.put("default", newPreferences);
        return ResponseEntity.ok(newPreferences);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification notification = notifications.get(id);
        if (notification != null) {
            notification.setRead(true);
            return ResponseEntity.ok(notification);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notifications.values().forEach(n -> n.setRead(true));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notifications.remove(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-due")
    public ResponseEntity<List<Notification>> checkForDueReviews() {
        // Return empty list for development purposes
        return ResponseEntity.ok(new ArrayList<>());
    }
}
