package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.CardTemplateApi;
import org.example.docvideoplay.entity.CardTemplate;
import org.example.docvideoplay.service.CardTemplateService;
import org.example.docvideoplay.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for card template management operations
 */
@RestController
public class CardTemplateController implements CardTemplateApi {
    
    private static final Logger logger = LoggerFactory.getLogger(CardTemplateController.class);
    
    private final CardTemplateService cardTemplateService;
    private final UserService userService;
    
    @Autowired
    public CardTemplateController(CardTemplateService cardTemplateService, UserService userService) {
        this.cardTemplateService = cardTemplateService;
        this.userService = userService;
    }
    
    /**
     * Get the current authenticated user ID or default user ID
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
    public ResponseEntity<CardTemplate> createTemplate(CardTemplate template) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Creating card template: name={}, type={}, userId={}",
                    template.getName(), template.getTemplateType(), currentUserId);
            
            CardTemplate createdTemplate = cardTemplateService.createTemplate(
                    currentUserId,
                    template.getName(),
                    template.getDescription(),
                    template.getTemplateType(),
                    template.getFrontTemplate(),
                    template.getBackTemplate()
            );
            
            logger.info("Template created successfully: id={}", createdTemplate.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Template creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during template creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<CardTemplate>> getTemplates() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving templates for userId: {}", currentUserId);
            
            List<CardTemplate> templates = cardTemplateService.getAllTemplatesForUser(currentUserId);
            logger.debug("Retrieved {} templates for userId {}", templates.size(), currentUserId);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error retrieving templates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<CardTemplate> getTemplate(Long id) {
        try {
            logger.debug("Retrieving template: id={}", id);
            
            CardTemplate template = cardTemplateService.getTemplateById(id);
            logger.debug("Retrieved template: id={}, name={}", id, template.getName());
            return ResponseEntity.ok(template);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Template not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving template {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<CardTemplate> updateTemplate(Long id, CardTemplate template) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Updating template: id={}, userId={}", id, currentUserId);
            
            CardTemplate updatedTemplate = cardTemplateService.updateTemplate(
                    id,
                    template.getName(),
                    template.getDescription(),
                    template.getFrontTemplate(),
                    template.getBackTemplate(),
                    currentUserId
            );
            
            logger.info("Template updated successfully: id={}", id);
            return ResponseEntity.ok(updatedTemplate);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Template update validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating template {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<Void> deleteTemplate(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting template: id={}, userId={}", id, currentUserId);
            
            cardTemplateService.deleteTemplate(id, currentUserId);
            
            logger.info("Template deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Template deletion validation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting template {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<CardTemplate>> getSystemTemplates() {
        try {
            logger.debug("Retrieving system templates");
            
            List<CardTemplate> templates = cardTemplateService.getSystemTemplates();
            logger.debug("Retrieved {} system templates", templates.size());
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error retrieving system templates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<CardTemplate>> getTemplatesByType(String type) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving templates by type: {}, userId={}", type, currentUserId);
            
            List<CardTemplate> templates = cardTemplateService.getTemplatesByType(currentUserId, type);
            logger.debug("Retrieved {} templates of type {} for userId {}", templates.size(), type, currentUserId);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error retrieving templates by type {}: {}", type, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<CardTemplate>> searchTemplates(String q) {
        try {
            logger.debug("Searching templates: q={}", q);
            
            List<CardTemplate> templates = cardTemplateService.searchTemplates(q);
            logger.debug("Found {} templates matching query: {}", templates.size(), q);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error searching templates with query '{}': {}", q, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
