package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.CardResultDto;
import org.example.docvideoplay.dto.api.StudyMaterialResultDto;
import org.example.docvideoplay.dto.api.DeckParamsDto;
import org.example.docvideoplay.dto.api.DeckResultDto;
import org.example.docvideoplay.dto.api.DeckUpdateDto;
import org.example.docvideoplay.dto.api.DeckCardParamsDto;
import org.example.docvideoplay.dto.api.DeckDuplicateParamsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for deck management operations
 * Handles deck creation, retrieval, update, and deletion
 * as well as card management within decks
 */
@RequestMapping("/api/decks")
public interface DeckApi {
    
    /**
     * Create a new deck
     * 
     * @param params Deck creation parameters (name, description, isPublic)
     * @return ResponseEntity containing the created deck details
     */
    @PostMapping
    ResponseEntity<DeckResultDto> createDeck(@Valid @RequestBody DeckParamsDto params);
    
    /**
     * Get all decks for the current user
     * 
     * @return ResponseEntity containing list of decks
     */
    @GetMapping
    ResponseEntity<List<DeckResultDto>> getDecks();
    
    /**
     * Get a specific deck by ID
     * 
     * @param id The deck ID
     * @return ResponseEntity containing the deck details
     */
    @GetMapping("/{id}")
    ResponseEntity<DeckResultDto> getDeck(@PathVariable Long id);
    
    /**
     * Update a deck
     * 
     * @param id The deck ID
     * @param params Updated deck parameters
     * @return ResponseEntity containing the updated deck details
     */
    @PutMapping("/{id}")
    ResponseEntity<DeckResultDto> updateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckUpdateDto params
    );
    
    /**
     * Delete a deck by ID
     * 
     * @param id The deck ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteDeck(@PathVariable Long id);
    
    /**
     * Get all cards in a deck
     * 
     * @param id The deck ID
     * @return ResponseEntity containing list of cards in the deck
     */
    @GetMapping("/{id}/cards")
    ResponseEntity<List<CardResultDto>> getDeckCards(@PathVariable Long id);
    
    /**
     * Add a card to a deck
     * 
     * @param id The deck ID
     * @param params Card addition parameters (highlightId)
     * @return ResponseEntity containing the updated deck details
     */
    @PostMapping("/{id}/cards")
    ResponseEntity<DeckResultDto> addCardToDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckCardParamsDto params
    );
    
    /**
     * Remove a card from a deck
     * 
     * @param id The deck ID
     * @param cardId The card (highlight) ID to remove
     * @return ResponseEntity containing the updated deck details
     */
    @DeleteMapping("/{id}/cards/{cardId}")
    ResponseEntity<DeckResultDto> removeCardFromDeck(
            @PathVariable Long id,
            @PathVariable Long cardId
    );
    
    /**
     * Get all public decks
     * 
     * @return ResponseEntity containing list of public decks
     */
    @GetMapping("/public")
    ResponseEntity<List<DeckResultDto>> getPublicDecks();
    
    /**
     * Duplicate a deck
     * 
     * @param id The deck ID to duplicate
     * @param params Duplication parameters (newName)
     * @return ResponseEntity containing the duplicated deck details
     */
    @PostMapping("/{id}/duplicate")
    ResponseEntity<DeckResultDto> duplicateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckDuplicateParamsDto params
    );
    
    /**
     * Search decks by name or description
     * 
     * @param query The search query
     * @return ResponseEntity containing list of matching decks
     */
    @GetMapping("/search")
    ResponseEntity<List<DeckResultDto>> searchDecks(@RequestParam("q") String query);
}