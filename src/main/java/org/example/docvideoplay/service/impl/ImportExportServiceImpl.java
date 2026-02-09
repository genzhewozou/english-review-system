package org.example.docvideoplay.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.docvideoplay.entity.Deck;
import org.example.docvideoplay.entity.Card;
import org.example.docvideoplay.entity.Tag;
import org.example.docvideoplay.repository.DeckRepository;
import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.repository.TagRepository;
import org.example.docvideoplay.service.ImportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the ImportExportService
 * This implementation uses JSON format for deck import/export
 */
@Service
public class ImportExportServiceImpl implements ImportExportService {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TagRepository tagRepository;

    private final ObjectMapper objectMapper;

    public ImportExportServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void exportDeck(Long deckId, OutputStream outputStream) throws IOException {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IOException("Deck not found"));

        // Create export data structure
        Map<String, Object> exportData = new java.util.HashMap<>();
        exportData.put("version", "1.0");
        exportData.put("type", "single_deck");
        exportData.put("deck", createExportableDeck(deck));

        objectMapper.writeValue(outputStream, exportData);
    }

    @Override
    public Deck importDeck(MultipartFile file, Long userId) throws IOException {
        Map<String, Object> importData = objectMapper.readValue(file.getInputStream(), Map.class);

        if (!"1.0".equals(importData.get("version"))) {
            throw new IOException("Unsupported file version");
        }

        Map<String, Object> deckData = (Map<String, Object>) importData.get("deck");
        return importDeckFromData(deckData, userId);
    }

    @Override
    public void exportAllDecks(Long userId, OutputStream outputStream) throws IOException {
        List<Deck> userDecks = deckRepository.findByUserId(userId);

        List<Map<String, Object>> exportableDecks = new ArrayList<>();
        for (Deck deck : userDecks) {
            exportableDecks.add(createExportableDeck(deck));
        }

        // Create export data structure
        Map<String, Object> exportData = new java.util.HashMap<>();
        exportData.put("version", "1.0");
        exportData.put("type", "multiple_decks");
        exportData.put("decks", exportableDecks);

        objectMapper.writeValue(outputStream, exportData);
    }

    @Override
    public int importMultipleDecks(MultipartFile file, Long userId) throws IOException {
        Map<String, Object> importData = objectMapper.readValue(file.getInputStream(), Map.class);

        if (!"1.0".equals(importData.get("version"))) {
            throw new IOException("Unsupported file version");
        }

        List<Map<String, Object>> decksData = (List<Map<String, Object>>) importData.get("decks");
        int importedCount = 0;

        for (Map<String, Object> deckData : decksData) {
            importDeckFromData(deckData, userId);
            importedCount++;
        }

        return importedCount;
    }

    @Override
    public void exportCards(List<Long> cardIds, OutputStream outputStream) throws IOException {
        List<Card> cards = cardRepository.findAllById(cardIds);

        List<Map<String, Object>> exportableCards = new ArrayList<>();
        for (Card card : cards) {
            Map<String, Object> cardData = new java.util.HashMap<>();
            cardData.put("text", card.getText());
            cardData.put("backText", card.getBackText());
            cardData.put("context", card.getContext());
            cardData.put("userComment", card.getUserComment());
            cardData.put("cardType", card.getCardType());
            cardData.put("tags", card.getTags());
            // Get deck name from deckId
            String deckName = null;
            if (card.getDeckId() != null) {
                Optional<Deck> deckOpt = deckRepository.findById(card.getDeckId());
                if (deckOpt.isPresent()) {
                    deckName = deckOpt.get().getName();
                }
            }
            cardData.put("deckName", deckName);
            exportableCards.add(cardData);
        }

        // Create export data structure
        Map<String, Object> exportData = new java.util.HashMap<>();
        exportData.put("version", "1.0");
        exportData.put("type", "selected_cards");
        exportData.put("cards", exportableCards);
        exportData.put("exportDate", java.time.LocalDateTime.now());

        objectMapper.writeValue(outputStream, exportData);
    }

    private Map<String, Object> createExportableDeck(Deck deck) {
        // We'll need to implement these methods in the repositories or find another way to get the data
        List<Card> cards = new ArrayList<>(); // Placeholder
        List<Tag> tags = new ArrayList<>(); // Placeholder

        List<Map<String, Object>> exportableCards = new ArrayList<>();
        for (Card card : cards) {
            Map<String, Object> cardData = new java.util.HashMap<>();
            cardData.put("text", card.getText());
            cardData.put("backText", card.getBackText());
            cardData.put("context", card.getContext());
            cardData.put("userComment", card.getUserComment());
            cardData.put("cardType", card.getCardType());
            cardData.put("tags", card.getTags());
            exportableCards.add(cardData);
        }

        List<String> exportableTags = new ArrayList<>();
        for (Tag tag : tags) {
            exportableTags.add(tag.getName());
        }

        Map<String, Object> deckData = new java.util.HashMap<>();
        deckData.put("name", deck.getName());
        deckData.put("description", deck.getDescription());
        deckData.put("tags", exportableTags);
        deckData.put("cards", exportableCards);
        return deckData;
    }

    private Deck importDeckFromData(Map<String, Object> deckData, Long userId) {
        // Create new deck with unique name to avoid conflicts
        String originalName = (String) deckData.get("name");
        String uniqueName = originalName + " (Imported " + UUID.randomUUID().toString().substring(0, 8) + ")";

        Deck deck = new Deck();
        deck.setName(uniqueName);
        deck.setDescription((String) deckData.get("description"));
        deck.setUserId(userId);
        deck.setIsPublic(false);
        deck = deckRepository.save(deck);

        // Import tags
        List<String> tagNames = new ArrayList<>();
        if (deckData.get("tags") instanceof List) {
            tagNames = (List<String>) deckData.get("tags");
        }
        for (String tagName : tagNames) {
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setUserId(userId);
            tag.setIsActive(true);
            tagRepository.save(tag);
        }

        // Import cards
        List<Map<String, Object>> cardsData = new ArrayList<>();
        if (deckData.get("cards") instanceof List) {
            cardsData = (List<Map<String, Object>>) deckData.get("cards");
        } else if (deckData.get("highlights") instanceof List) {
            // For backward compatibility with old export format
            cardsData = (List<Map<String, Object>>) deckData.get("highlights");
        }
        for (Map<String, Object> cardData : cardsData) {
            Card card = new Card();
            card.setText((String) cardData.get("text"));
            card.setBackText((String) cardData.get("backText"));
            card.setContext((String) cardData.get("context"));
            card.setUserComment((String) cardData.get("userComment"));
            card.setCardType((String) cardData.getOrDefault("cardType", "BASIC"));
            // setTags method expects a String, not a List
            if (cardData.get("tags") instanceof List) {
                List<String> tagsList = (List<String>) cardData.get("tags");
                card.setTags(String.join(",", tagsList));
            } else if (cardData.get("tags") instanceof String) {
                card.setTags((String) cardData.get("tags"));
            }
            card.setDeckId(deck.getId());
            card.setUserId(userId);
            card.setIsActive(true);
            cardRepository.save(card);
        }

        return deck;
    }
}
