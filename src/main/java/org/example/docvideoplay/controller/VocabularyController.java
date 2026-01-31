package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.VocabularyApi;
import org.example.docvideoplay.dto.api.HighlightParamsDto;
import org.example.docvideoplay.dto.api.HighlightResultDto;
import org.example.docvideoplay.dto.api.HighlightUpdateDto;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for vocabulary highlighting and management operations.
 * Implements VocabularyApi interface for highlight creation, updates, and comment management.
 */
@RestController
public class VocabularyController implements VocabularyApi {

    private static final Logger logger = LoggerFactory.getLogger(VocabularyController.class);

    private final VocabularyService vocabularyService;

    @Autowired
    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @Override
    public ResponseEntity<HighlightResultDto> createHighlight(@Valid HighlightParamsDto params) {
        try {
            logger.info("Creating highlight: materialId={}, text={}",
                    params.getMaterialId(), params.getText());

            // Validate positions if provided
            if (params.getStartPosition() != null && params.getEndPosition() != null) {
                if (!vocabularyService.isValidPosition(params.getStartPosition(), params.getEndPosition())) {
                    logger.warn("Invalid highlight positions: start={}, end={}",
                            params.getStartPosition(), params.getEndPosition());
                    return ResponseEntity.badRequest().build();
                }
            }

            Highlight highlight;
            if (params.getUserComment() != null && !params.getUserComment().trim().isEmpty()) {
                // Create highlight with comment
                highlight = vocabularyService.createHighlightWithComment(
                        params.getMaterialId(),
                        params.getText(),
                        params.getContext(),
                        params.getStartPosition(),
                        params.getEndPosition(),
                        params.getUserComment()
                );
            } else {
                // Create highlight without comment
                highlight = vocabularyService.createHighlight(
                        params.getMaterialId(),
                        params.getText(),
                        params.getContext(),
                        params.getStartPosition(),
                        params.getEndPosition()
                );
            }

            HighlightResultDto result = convertToResultDto(highlight);

            logger.info("Highlight created successfully: id={}", highlight.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Highlight creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during highlight creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<HighlightResultDto>> getHighlightsByMaterial(Long materialId) {
        try {
            logger.debug("Retrieving highlights for material: id={}", materialId);

            List<Highlight> highlights = vocabularyService.getHighlightsByMaterial(materialId);
            List<HighlightResultDto> results = highlights.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} highlights for material {}", results.size(), materialId);
            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
            logger.warn("Material not found: id={}", materialId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving highlights for material {}: {}", materialId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<HighlightResultDto>> getHighlightsBySpecificMaterial(Long materialId) {
        try {
            logger.debug("Retrieving highlights for material: id={}", materialId);

            List<Highlight> highlights = vocabularyService.getHighlightsByMaterial(materialId);
            List<HighlightResultDto> results = highlights.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} highlights for material {}", results.size(), materialId);
            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
            logger.warn("Material not found: id={}", materialId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving highlights for material {}: {}", materialId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<HighlightResultDto> getHighlight(Long id) {
        try {
            logger.debug("Retrieving highlight: id={}", id);

            Highlight highlight = vocabularyService.getHighlightById(id);
            HighlightResultDto result = convertToResultDto(highlight);

            logger.debug("Retrieved highlight: id={}, text={}", id, highlight.getText());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Highlight not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving highlight {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<HighlightResultDto> updateHighlight(Long id, @Valid HighlightUpdateDto updateDto) {
        try {
            logger.info("Updating highlight: id={}", id);

            Highlight highlight = vocabularyService.getHighlightById(id);

            // Update comment if provided
            if (updateDto.getUserComment() != null) {
                highlight = vocabularyService.updateHighlightComment(id, updateDto.getUserComment());
            }

            // Update text and context if provided
            if (updateDto.getText() != null && !updateDto.getText().trim().isEmpty()) {
                highlight = vocabularyService.updateHighlightText(id, updateDto.getText(), updateDto.getContext());
            }

            // Update positions if provided and valid
            if (updateDto.getStartPosition() != null && updateDto.getEndPosition() != null) {
                if (!vocabularyService.isValidPosition(updateDto.getStartPosition(), updateDto.getEndPosition())) {
                    logger.warn("Invalid highlight positions: start={}, end={}",
                            updateDto.getStartPosition(), updateDto.getEndPosition());
                    return ResponseEntity.badRequest().build();
                }
                highlight = vocabularyService.updateHighlightPosition(id, updateDto.getStartPosition(), updateDto.getEndPosition());
            }

            HighlightResultDto result = convertToResultDto(highlight);

            logger.info("Highlight updated successfully: id={}", id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("Highlight update validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating highlight {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteHighlight(Long id) {
        try {
            logger.info("Deleting highlight: id={}", id);

            vocabularyService.deleteHighlight(id);

            logger.info("Highlight deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Highlight not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting highlight {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<HighlightResultDto>> getHighlightsDueForReview() {
        try {
            logger.debug("Retrieving highlights due for review");

            List<Highlight> highlights = vocabularyService.getHighlightsDueToday();
            List<HighlightResultDto> results = highlights.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} highlights due for review", results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving highlights due for review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<HighlightResultDto>> getAllHighlights() {
        try {
            logger.debug("Retrieving all highlights");

            List<Highlight> highlights = vocabularyService.getAllHighlights();
            List<HighlightResultDto> results = highlights.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} highlights", results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving all highlights: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<HighlightResultDto>> getAllHighlightsReal() {
        try {
            logger.debug("Retrieving all highlights");

            List<Highlight> highlights = vocabularyService.getAllHighlights();
            List<HighlightResultDto> results = highlights.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} highlights", results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error retrieving all highlights: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<HighlightResultDto>> searchHighlights(String query) {
        try {
            logger.debug("Searching highlights with query: {}", query);

            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest().build();
            }

            List<Highlight> highlights = vocabularyService.searchHighlightsByText(query.trim());
            List<HighlightResultDto> results = highlights.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} highlights matching query: {}", results.size(), query);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error searching highlights with query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Highlight entity to HighlightResultDto
     *
     * @param highlight The Highlight entity
     * @return The converted DTO
     */
    private HighlightResultDto convertToResultDto(Highlight highlight) {
        HighlightResultDto dto = new HighlightResultDto();
        dto.setId(highlight.getId());
        dto.setMaterialId(highlight.getMaterial() != null ? highlight.getMaterial().getId() : null);
        dto.setText(highlight.getText());
        dto.setContext(highlight.getContext());
        dto.setStartPosition(highlight.getStartPosition());
        dto.setEndPosition(highlight.getEndPosition());
        dto.setUserComment(highlight.getUserComment());
        dto.setEaseFactor(highlight.getEaseFactor());
        dto.setRepetitionCount(highlight.getRepetitionCount());
        dto.setIntervalDays(highlight.getIntervalDays());
        dto.setNextReviewDate(highlight.getNextReviewDate());
        dto.setLastReviewDate(highlight.getLastReviewDate());
        dto.setCreatedDate(highlight.getCreatedDate());
        dto.setUpdatedDate(highlight.getUpdatedDate());

        return dto;
    }
}