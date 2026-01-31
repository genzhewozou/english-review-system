package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.HighlightParamsDto;
import org.example.docvideoplay.dto.api.HighlightResultDto;
import org.example.docvideoplay.dto.api.HighlightUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for vocabulary highlighting and management operations
 * Handles highlight creation, updates, and comment management
 */
@RequestMapping("/api/vocabulary")
public interface VocabularyApi {

    /**
     * Create a new highlight
     *
     * @param params Highlight parameters (materialId, text, context, positions, comment)
     * @return ResponseEntity containing the created highlight details
     */
    @PostMapping("/highlights")
    ResponseEntity<HighlightResultDto> createHighlight(@Valid @RequestBody HighlightParamsDto params);

    /**
     * Get all highlights for a specific material
     *
     * @param materialId The material ID
     * @return ResponseEntity containing list of highlights for the material
     */
    @GetMapping("/materials/{materialId}/highlights")
    ResponseEntity<List<HighlightResultDto>> getHighlightsByMaterial(@PathVariable Long materialId);

    /**
     * Get all highlights for a specific material
     *
     * @param materialId The material ID
     * @return ResponseEntity containing list of highlights for the material
     */
    @GetMapping("/material/{materialId}")
    ResponseEntity<List<HighlightResultDto>> getHighlightsBySpecificMaterial(@PathVariable Long materialId);

    /**
     * Get a specific highlight by ID
     *
     * @param id The highlight ID
     * @return ResponseEntity containing the highlight details
     */
    @GetMapping("/highlights/{id}")
    ResponseEntity<HighlightResultDto> getHighlight(@PathVariable Long id);

    /**
     * Update a highlight (mainly for adding/updating comments)
     *
     * @param id        The highlight ID
     * @param updateDto Updated highlight parameters (all fields optional)
     * @return ResponseEntity containing the updated highlight details
     */
    @PutMapping("/highlights/{id}")
    ResponseEntity<HighlightResultDto> updateHighlight(
            @PathVariable Long id,
            @Valid @RequestBody HighlightUpdateDto updateDto
    );

    /**
     * Delete a highlight by ID
     *
     * @param id The highlight ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/highlights/{id}")
    ResponseEntity<Void> deleteHighlight(@PathVariable Long id);

    /**
     * Get highlights that are due for review
     *
     * @return ResponseEntity containing list of highlights due for review
     */
    @GetMapping("/highlights/due-for-review")
    ResponseEntity<List<HighlightResultDto>> getHighlightsDueForReview();

    /**
     * Get all highlights across all materials
     *
     * @return ResponseEntity containing list of all highlights
     */
    @GetMapping("/highlights")
    ResponseEntity<List<HighlightResultDto>> getAllHighlights();

    @GetMapping
    ResponseEntity<List<HighlightResultDto>> getAllHighlightsReal();

    /**
     * Search highlights by text content
     *
     * @param query The search query
     * @return ResponseEntity containing list of matching highlights
     */
    @GetMapping("/highlights/search")
    ResponseEntity<List<HighlightResultDto>> searchHighlights(@RequestParam("q") String query);
}