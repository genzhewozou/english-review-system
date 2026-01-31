package org.example.docvideoplay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.docvideoplay.config.FileStorageConfig;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    @Autowired
    private FileStorageConfig fileStorageConfig;
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "English Learning System");
        response.put("timestamp", System.currentTimeMillis());
        
        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                response.put("database", "UP");
            } else {
                response.put("database", "DOWN");
                response.put("status", "DEGRADED");
            }
        } catch (SQLException e) {
            response.put("database", "DOWN");
            response.put("database_error", e.getMessage());
            response.put("status", "DEGRADED");
        }
        
        // Check file storage
        try {
            if (Files.exists(Paths.get(fileStorageConfig.getBasePath())) && 
                Files.isWritable(Paths.get(fileStorageConfig.getBasePath()))) {
                response.put("fileStorage", "UP");
                response.put("fileStorageBasePath", fileStorageConfig.getBasePath());
            } else {
                response.put("fileStorage", "DOWN");
                response.put("status", "DEGRADED");
            }
        } catch (Exception e) {
            response.put("fileStorage", "DOWN");
            response.put("fileStorage_error", e.getMessage());
            response.put("status", "DEGRADED");
        }
        
        if ("DEGRADED".equals(response.get("status"))) {
            return ResponseEntity.status(503).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}