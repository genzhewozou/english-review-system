package org.example.docvideoplay.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.example.docvideoplay.entity.Deck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * API interface for deck import/export operations
 */
@RequestMapping("/api/import-export")
public interface ImportExportApi {

    /**
     * Export a deck to a JSON file
     * @param deckId The ID of the deck to export
     * @return The exported deck as a JSON file
     */
    @Operation(
            summary = "Export deck",
            description = "Export a deck to a JSON file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Deck exported successfully",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Deck not found"
                    )
            }
    )
    @GetMapping("/export/deck/{deckId}")
    ResponseEntity<?> exportDeck(@PathVariable Long deckId);

    /**
     * Export all decks for the current user
     * @return All decks as a JSON file
     */
    @Operation(
            summary = "Export all decks",
            description = "Export all decks for the current user to a JSON file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Decks exported successfully",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/export/all")
    ResponseEntity<?> exportAllDecks();

    /**
     * Import a deck from a JSON file
     * @param file The JSON file containing the deck
     * @return The imported deck
     */
    @Operation(
            summary = "Import deck",
            description = "Import a deck from a JSON file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Deck imported successfully",
                            content = @Content(schema = @Schema(implementation = Deck.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file format"
                    )
            }
    )
    @PostMapping("/import/deck")
    ResponseEntity<Deck> importDeck(@RequestParam("file") MultipartFile file);

    /**
     * Import multiple decks from a JSON file
     * @param file The JSON file containing multiple decks
     * @return The number of decks imported
     */
    @Operation(
            summary = "Import multiple decks",
            description = "Import multiple decks from a JSON file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Decks imported successfully",
                            content = @Content(schema = @Schema(implementation = Integer.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file format"
                    )
            }
    )
    @PostMapping("/import/multiple")
    ResponseEntity<Integer> importMultipleDecks(@RequestParam("file") MultipartFile file);

    /**
     * Export selected cards to a JSON file
     * @param cardIds The list of card IDs to export
     * @return The exported cards as a JSON file
     */
    @Operation(
            summary = "Export selected cards",
            description = "Export selected cards to a JSON file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cards exported successfully",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/export/cards")
    ResponseEntity<?> exportCards(@RequestBody List<Long> cardIds);
}
