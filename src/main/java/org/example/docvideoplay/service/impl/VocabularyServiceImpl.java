package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.dao.jpa.HighlightRepository;
import org.example.docvideoplay.dao.jpa.StudyMaterialRepository;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.service.SpacedRepetitionService;
import org.example.docvideoplay.service.TodoService;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of VocabularyService for managing vocabulary highlights and comments.
 * Integrates with SpacedRepetitionService for automatic review scheduling.
 */
@Service
@Transactional
public class VocabularyServiceImpl implements VocabularyService {
    
    private static final Logger logger = LoggerFactory.getLogger(VocabularyServiceImpl.class);
    
    private final HighlightRepository highlightRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final TodoService todoService;
    
    @Autowired
    public VocabularyServiceImpl(HighlightRepository highlightRepository,
                                StudyMaterialRepository studyMaterialRepository,
                                SpacedRepetitionService spacedRepetitionService,
                                TodoService todoService) {
        this.highlightRepository = highlightRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.todoService = todoService;
    }
    
    @Override
    public Highlight createHighlight(Long materialId, String text, String context, 
                                   Integer startPosition, Integer endPosition) {
        return createHighlightWithComment(materialId, text, context, startPosition, endPosition, null);
    }
    
    @Override
    public Highlight createHighlightWithComment(Long materialId, String text, String context,
                                              Integer startPosition, Integer endPosition, String userComment) {
        // Validate input parameters
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Highlight text cannot be null or empty");
        }
        
        if (!isValidPosition(startPosition, endPosition)) {
            throw new IllegalArgumentException("Invalid highlight positions: start=" + startPosition + ", end=" + endPosition);
        }
        
        // Verify that the study material exists
        Optional<StudyMaterial> materialOpt = studyMaterialRepository.findById(materialId);
        if (!materialOpt.isPresent()) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        StudyMaterial material = materialOpt.get();
        
        // Create new highlight
        Highlight highlight = new Highlight();
        highlight.setMaterial(material);
        highlight.setText(text.trim());
        highlight.setContext(context != null ? context.trim() : null);
        highlight.setStartPosition(startPosition);
        highlight.setEndPosition(endPosition);
        highlight.setUserComment(userComment != null ? userComment.trim() : null);
        
        // Schedule initial spaced repetition reminder
        spacedRepetitionService.scheduleInitialReminder(highlight);
        
        // Save the highlight
        Highlight savedHighlight = highlightRepository.save(highlight);
        
        // Flush to ensure the highlight is persisted before creating todo items
        highlightRepository.flush();
        
        // Schedule todo item for review reminder
        todoService.scheduleReviewReminder(savedHighlight);
        
        logger.info("Created highlight with ID: {} for material: {}", savedHighlight.getId(), materialId);
        
        return savedHighlight;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getHighlightsByMaterial(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // Verify that the study material exists
        if (!studyMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        return highlightRepository.findByMaterialIdOrderByStartPositionAsc(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getHighlightsByMaterialWithHistory(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        // Verify that the study material exists
        if (!studyMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + materialId);
        }
        
        return highlightRepository.findByMaterialIdWithReviewHistory(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Highlight getHighlightById(Long highlightId) {
        if (highlightId == null) {
            throw new IllegalArgumentException("Highlight ID cannot be null");
        }
        
        Optional<Highlight> highlight = highlightRepository.findById(highlightId);
        if (!highlight.isPresent()) {
            throw new IllegalArgumentException("Highlight not found with ID: " + highlightId);
        }
        
        return highlight.get();
    }
    
    @Override
    public Highlight updateHighlightComment(Long highlightId, String comment) {
        Highlight highlight = getHighlightById(highlightId);
        
        highlight.setUserComment(comment != null ? comment.trim() : null);
        
        Highlight updatedHighlight = highlightRepository.save(highlight);
        logger.info("Updated comment for highlight ID: {}", highlightId);
        
        return updatedHighlight;
    }
    
    @Override
    public Highlight updateHighlightText(Long highlightId, String text, String context) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Highlight text cannot be null or empty");
        }
        
        Highlight highlight = getHighlightById(highlightId);
        
        highlight.setText(text.trim());
        highlight.setContext(context != null ? context.trim() : null);
        
        Highlight updatedHighlight = highlightRepository.save(highlight);
        logger.info("Updated text for highlight ID: {}", highlightId);
        
        return updatedHighlight;
    }
    
    @Override
    public Highlight updateHighlightPosition(Long highlightId, Integer startPosition, Integer endPosition) {
        if (!isValidPosition(startPosition, endPosition)) {
            throw new IllegalArgumentException("Invalid highlight positions: start=" + startPosition + ", end=" + endPosition);
        }
        
        Highlight highlight = getHighlightById(highlightId);
        
        highlight.setStartPosition(startPosition);
        highlight.setEndPosition(endPosition);
        
        Highlight updatedHighlight = highlightRepository.save(highlight);
        logger.info("Updated position for highlight ID: {}", highlightId);
        
        return updatedHighlight;
    }
    
    @Override
    public void deleteHighlight(Long highlightId) {
        if (highlightId == null) {
            throw new IllegalArgumentException("Highlight ID cannot be null");
        }
        
        Highlight highlight = getHighlightById(highlightId);
        
        highlightRepository.delete(highlight);
        logger.info("Deleted highlight with ID: {}", highlightId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> searchHighlightsByText(String searchText) {
        if (!StringUtils.hasText(searchText)) {
            return new ArrayList<>(); // Return empty list for empty search
        }
        
        return highlightRepository.findByTextContainingIgnoreCase(searchText.trim());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getAllHighlights() {
        return highlightRepository.findAllByOrderByCreatedDateDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getHighlightsWithComments() {
        return highlightRepository.findHighlightsWithComments();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getNeverReviewedHighlights() {
        return highlightRepository.findNeverReviewedHighlights();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getHighlightCountByMaterial(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        return highlightRepository.countByMaterialId(materialId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getHighlightsDueToday() {
        return highlightRepository.findHighlightsDueToday();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Highlight> getOverdueHighlights() {
        return highlightRepository.findOverdueHighlights();
    }
    
    @Override
    public boolean isValidPosition(Integer startPosition, Integer endPosition) {
        if (startPosition == null || endPosition == null) {
            return false;
        }
        
        if (startPosition < 0 || endPosition < 0) {
            return false;
        }
        
        return startPosition <= endPosition;
    }
}