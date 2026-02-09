package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.entity.CardTemplate;
import org.example.docvideoplay.repository.CardTemplateRepository;
import org.example.docvideoplay.service.CardTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CardTemplateService
 */
@Service
public class CardTemplateServiceImpl implements CardTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CardTemplateServiceImpl.class);
    
    private final CardTemplateRepository cardTemplateRepository;
    
    @Autowired
    public CardTemplateServiceImpl(CardTemplateRepository cardTemplateRepository) {
        this.cardTemplateRepository = cardTemplateRepository;
    }
    
    @Override
    public CardTemplate createTemplate(Long userId, String name, String description, String templateType,
                                     String frontTemplate, String backTemplate) {
        // Validate input parameters
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        if (!StringUtils.hasText(templateType)) {
            throw new IllegalArgumentException("Template type cannot be null or empty");
        }
        
        if (!StringUtils.hasText(frontTemplate)) {
            throw new IllegalArgumentException("Front template cannot be null or empty");
        }
        
        if (!StringUtils.hasText(backTemplate)) {
            throw new IllegalArgumentException("Back template cannot be null or empty");
        }
        
        // Create new template
        CardTemplate template = new CardTemplate();
        template.setUserId(userId);
        template.setName(name.trim());
        template.setDescription(description != null ? description.trim() : null);
        template.setTemplateType(templateType.trim());
        template.setFrontTemplate(frontTemplate.trim());
        template.setBackTemplate(backTemplate.trim());
        template.setIsSystemTemplate(false);
        
        // Save the template
        CardTemplate savedTemplate = cardTemplateRepository.save(template);
        
        logger.info("Created card template with ID: {} for user: {}", savedTemplate.getId(), userId);
        
        return savedTemplate;
    }
    
    @Override
    @Transactional(readOnly = true)
    public CardTemplate getTemplateById(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID cannot be null");
        }
        
        return cardTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardTemplate> getAllTemplatesForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return cardTemplateRepository.findByUserIdOrIsSystemTemplateTrue(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardTemplate> getSystemTemplates() {
        return cardTemplateRepository.findByIsSystemTemplateTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardTemplate> getUserTemplates(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return cardTemplateRepository.findByUserId(userId);
    }
    
    @Override
    public CardTemplate updateTemplate(Long templateId, String name, String description,
                                     String frontTemplate, String backTemplate, Long userId) {
        // Validate input parameters
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID cannot be null");
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        if (!StringUtils.hasText(frontTemplate)) {
            throw new IllegalArgumentException("Front template cannot be null or empty");
        }
        
        if (!StringUtils.hasText(backTemplate)) {
            throw new IllegalArgumentException("Back template cannot be null or empty");
        }
        
        // Get the template
        CardTemplate template = cardTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
        
        // Check if user owns the template or it's a system template
        if (!template.getIsSystemTemplate() && !template.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Template not found or not owned by user");
        }
        
        // System templates cannot be modified
        if (template.getIsSystemTemplate()) {
            throw new IllegalArgumentException("System templates cannot be modified");
        }
        
        // Update template fields
        template.setName(name.trim());
        template.setDescription(description != null ? description.trim() : null);
        template.setFrontTemplate(frontTemplate.trim());
        template.setBackTemplate(backTemplate.trim());
        
        // Save the updated template
        CardTemplate updatedTemplate = cardTemplateRepository.save(template);
        logger.info("Updated card template with ID: {}", templateId);
        
        return updatedTemplate;
    }
    
    @Override
    public void deleteTemplate(Long templateId, Long userId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID cannot be null");
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        // Get the template
        CardTemplate template = cardTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
        
        // Check if user owns the template
        if (!template.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Template not found or not owned by user");
        }
        
        // System templates cannot be deleted
        if (template.getIsSystemTemplate()) {
            throw new IllegalArgumentException("System templates cannot be deleted");
        }
        
        // Delete the template
        cardTemplateRepository.delete(template);
        logger.info("Deleted card template with ID: {}", templateId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardTemplate> getTemplatesByType(Long userId, String templateType) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (!StringUtils.hasText(templateType)) {
            throw new IllegalArgumentException("Template type cannot be null or empty");
        }
        
        return cardTemplateRepository.findByUserIdAndTemplateType(userId, templateType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardTemplate> searchTemplates(String searchText) {
        if (!StringUtils.hasText(searchText)) {
            return new ArrayList<>();
        }
        
        return cardTemplateRepository.findByNameContainingIgnoreCase(searchText.trim());
    }
}
