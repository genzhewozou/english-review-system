package org.example.docvideoplay.service;

import java.util.List;
import org.example.docvideoplay.entity.Deck;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Service for handling deck import/export operations
 * This service provides functionality to export decks to files and import decks from files
 */
public interface ImportExportService {

    /**
     * Export a deck to a file
     * @param deckId The ID of the deck to export
     * @param outputStream The output stream to write the deck to
     * @throws IOException If there's an error during export
     */
    void exportDeck(Long deckId, OutputStream outputStream) throws IOException;

    /**
     * Import a deck from a file
     * @param file The file to import the deck from
     * @param userId The ID of the user importing the deck
     * @return The imported deck
     * @throws IOException If there's an error during import
     */
    Deck importDeck(MultipartFile file, Long userId) throws IOException;

    /**
     * Export all decks for a user
     * @param userId The ID of the user whose decks to export
     * @param outputStream The output stream to write the decks to
     * @throws IOException If there's an error during export
     */
    void exportAllDecks(Long userId, OutputStream outputStream) throws IOException;

    /**
     * Import multiple decks from a file
     * @param file The file to import decks from
     * @param userId The ID of the user importing the decks
     * @return The number of decks imported
     * @throws IOException If there's an error during import
     */
    int importMultipleDecks(MultipartFile file, Long userId) throws IOException;

    /**
     * Export selected cards to a file
     * @param cardIds The list of card IDs to export
     * @param outputStream The output stream to write the cards to
     * @throws IOException If there's an error during export
     */
    void exportCards(List<Long> cardIds, OutputStream outputStream) throws IOException;
}
