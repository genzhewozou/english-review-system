package org.example.docvideoplay.controller;

import java.util.List;
import org.example.docvideoplay.api.ImportExportApi;
import org.example.docvideoplay.entity.Deck;
import org.example.docvideoplay.service.ImportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Controller for handling deck import/export operations
 */
@RestController
public class ImportExportController implements ImportExportApi {

    @Autowired
    private ImportExportService importExportService;

    @Override
    public ResponseEntity<?> exportDeck(Long deckId) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            importExportService.exportDeck(deckId, outputStream);

            byte[] exportData = outputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=deck_" + deckId + ".json");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error exporting deck: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> exportAllDecks() {
        try {
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = Long.parseLong(userDetails.getUsername());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            importExportService.exportAllDecks(userId, outputStream);

            byte[] exportData = outputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_decks.json");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exporting decks: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Deck> importDeck(MultipartFile file) {
        try {
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = Long.parseLong(userDetails.getUsername());

            Deck importedDeck = importExportService.importDeck(file, userId);
            return ResponseEntity.ok(importedDeck);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Override
    public ResponseEntity<Integer> importMultipleDecks(MultipartFile file) {
        try {
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = Long.parseLong(userDetails.getUsername());

            int importedCount = importExportService.importMultipleDecks(file, userId);
            return ResponseEntity.ok(importedCount);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Override
    public ResponseEntity<?> exportCards(List<Long> cardIds) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            importExportService.exportCards(cardIds, outputStream);

            byte[] exportData = outputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cards_export_" + System.currentTimeMillis() + ".json");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exporting cards: " + e.getMessage());
        }
    }
}
