package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.CardParamsDto;
import org.example.docvideoplay.dto.api.CardResultDto;
import org.example.docvideoplay.dto.api.CardUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for vocabulary card operations
 * Handles card creation, updates, and comment management
 */
@RequestMapping("/api/vocabulary")
public interface VocabularyApi {

    /**
     * Create a new card
     *
     * @param params Card parameters (materialId, text, context, positions, comment)
     * @return ResponseEntity containing the created card details or error message
     */
    @PostMapping("/cards")
    ResponseEntity<?> createCard(@Valid @RequestBody CardParamsDto params);

    /**
     * Get all cards for a specific material
     *
     * @param materialId The material ID
     * @return ResponseEntity containing list of cards for the material
     */
    @GetMapping("/materials/{materialId}/cards")
    ResponseEntity<List<CardResultDto>> getCardsByMaterial(@PathVariable Long materialId);

    /**
     * Get all cards for a specific material
     *
     * @param materialId The material ID
     * @return ResponseEntity containing list of cards for the material
     */
    @GetMapping("/material/{materialId}")
    ResponseEntity<List<CardResultDto>> getCardsBySpecificMaterial(@PathVariable Long materialId);

    /**
     * Get a specific card by ID
     *
     * @param id The card ID
     * @return ResponseEntity containing the card details
     */
    @GetMapping("/cards/{id}")
    ResponseEntity<CardResultDto> getCard(@PathVariable Long id);

    /**
     * Update a card (mainly for adding/updating comments)
     *
     * @param id        The card ID
     * @param updateDto Updated card parameters (all fields optional)
     * @return ResponseEntity containing the updated card details
     */
    @PutMapping("/cards/{id}")
    ResponseEntity<CardResultDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateDto updateDto
    );

    /**
     * Delete a card by ID
     *
     * @param id The card ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/cards/{id}")
    ResponseEntity<Void> deleteCard(@PathVariable Long id);

    /**
     * Get cards that are due for review
     *
     * @return ResponseEntity containing list of cards due for review
     */
    @GetMapping("/cards/due-for-review")
    ResponseEntity<List<CardResultDto>> getCardsDueForReview();

    /**
     * Get all cards across all materials
     *
     * @return ResponseEntity containing list of all cards
     */
    @GetMapping("/cards")
    ResponseEntity<List<CardResultDto>> getAllCards();

    @GetMapping
    ResponseEntity<List<CardResultDto>> getAllCardsReal();

    /**
     * Search cards by text content
     *
     * @param query The search query
     * @return ResponseEntity containing list of matching cards
     */
    @GetMapping("/cards/search")
    ResponseEntity<List<CardResultDto>> searchCards(@RequestParam("q") String query);
}