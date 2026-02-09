package org.example.docvideoplay.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.docvideoplay.DocVideoPlayApplication;
import org.example.docvideoplay.dao.jpa.*;
import org.example.docvideoplay.dto.api.*;
import org.example.docvideoplay.entity.*;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.enums.MaterialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for data consistency across the entire system
 * Validates that data remains consistent across all operations and entities
 */
@SpringBootTest(classes = DocVideoPlayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DataConsistencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudyMaterialRepository materialRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ReviewSessionRepository reviewSessionRepository;

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Test
    void testMaterialCardConsistency() throws Exception {
        // Upload material
        Long materialId = uploadTestMaterial("Test Material", MaterialType.DOCUMENT);

        // Verify material exists in database
        Optional<StudyMaterial> materialOpt = materialRepository.findById(materialId);
        assertTrue(materialOpt.isPresent());
        StudyMaterial material = materialOpt.get();
        assertEquals("Test Material", material.getTitle());
        assertEquals(0, cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId).size());

        // Create cards
        Long card1Id = createTestCard(materialId, "word1", "context1");
        Long card2Id = createTestCard(materialId, "word2", "context2");

        // Verify cards exist and are linked to material
        Optional<Card> card1Opt = cardRepository.findById(card1Id);
        Optional<Card> card2Opt = cardRepository.findById(card2Id);
        
        assertTrue(card1Opt.isPresent());
        assertTrue(card2Opt.isPresent());
        
        Card card1 = card1Opt.get();
        Card card2 = card2Opt.get();
        
        assertEquals(materialId, card1.getMaterialId());
        assertEquals(materialId, card2.getMaterialId());

        // Refresh material and verify card count
        material = materialRepository.findById(materialId).get();
        assertEquals(2, cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId).size());

        // Verify API returns consistent data
        mockMvc.perform(get("/api/materials/{id}", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardCount").value(2));

        mockMvc.perform(get("/api/cards/material/{materialId}", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testReviewSessionDataConsistency() throws Exception {
        // Setup: Create material and cards
        Long materialId = uploadTestMaterial("Review Test Material", MaterialType.DOCUMENT);
        Long card1Id = createTestCard(materialId, "review word 1", "context 1");
        Long card2Id = createTestCard(materialId, "review word 2", "context 2");

        // Start review session
        MvcResult sessionResult = mockMvc.perform(post("/api/reviews/sessions"))
                .andExpect(status().isCreated())
                .andReturn();

        ReviewSessionResultDto sessionDto = objectMapper.readValue(
                sessionResult.getResponse().getContentAsString(), 
                ReviewSessionResultDto.class
        );

        Long sessionId = sessionDto.getId();

        // Verify session exists in database
        Optional<ReviewSession> sessionOpt = reviewSessionRepository.findById(sessionId);
        assertTrue(sessionOpt.isPresent());
        ReviewSession session = sessionOpt.get();
        assertEquals(2, session.getTotalQuestions());
        assertEquals(0, session.getCorrectAnswers());
        assertFalse(session.getCompleted());

        // Submit answers
        submitAnswer(sessionId, card1Id, AnswerQuality.CORRECT);
        submitAnswer(sessionId, card2Id, AnswerQuality.PERFECT);

        // Verify review records were created
        List<ReviewRecord> records = reviewRecordRepository.findAll();
        assertEquals(2, records.size());

        for (ReviewRecord record : records) {
            assertEquals(sessionId, record.getSessionId());
            assertTrue(record.getCardId().equals(card1Id) || 
                      record.getCardId().equals(card2Id));
        }

        // Complete session
        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/complete", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.correctAnswers").value(2))
                .andExpect(jsonPath("$.accuracyPercentage").value(100));

        // Verify database consistency
        session = reviewSessionRepository.findById(sessionId).get();
        assertTrue(session.getCompleted());
        assertEquals(2, session.getCorrectAnswers());
        assertNotNull(session.getEndTime());
    }

    @Test
    void testSpacedRepetitionConsistency() throws Exception {
        // Create material and card
        Long materialId = uploadTestMaterial("Spaced Repetition Test", MaterialType.DOCUMENT);
        Long cardId = createTestCard(materialId, "spaced word", "spaced context");

        // Verify initial spaced repetition values
        Card card = cardRepository.findById(cardId).get();
        assertEquals(2.5, card.getEaseFactor());
        assertEquals(0, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays());
        assertNotNull(card.getNextReviewDate());
        assertNull(card.getLastReviewDate());

        // Start review session and answer
        Long sessionId = startReviewSession();
        submitAnswer(sessionId, cardId, AnswerQuality.CORRECT);
        completeReviewSession(sessionId);

        // Verify spaced repetition values updated
        card = cardRepository.findById(cardId).get();
        assertEquals(1, card.getRepetitionCount());
        assertEquals(1, card.getIntervalDays()); // First correct answer = 1 day interval (SM-2 standard)
        assertNotNull(card.getLastReviewDate());
        assertNotNull(card.getNextReviewDate());
        assertTrue(card.getNextReviewDate().isAfter(LocalDate.now()));

        // Verify API consistency
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repetitionCount").value(1))
                .andExpect(jsonPath("$.intervalDays").value(1)) // First correct answer = 1 day
                .andExpect(jsonPath("$.lastReviewDate").exists())
                .andExpect(jsonPath("$.nextReviewDate").exists());
    }

    @Test
    void testTodoItemConsistency() throws Exception {
        // Create material and card
        Long materialId = uploadTestMaterial("Todo Test Material", MaterialType.DOCUMENT);
        Long cardId = createTestCard(materialId, "todo word", "todo context");

        // Verify todo item was created automatically
        List<TodoItem> todoItems = todoItemRepository.findByCompletedFalseOrderByDueDateAsc();
        assertEquals(1, todoItems.size());
        
        TodoItem todoItem = todoItems.get(0);
        assertEquals(cardId, todoItem.getRelatedCardId());
        assertFalse(todoItem.getCompleted());
        assertEquals(LocalDate.now().plusDays(5), todoItem.getDueDate()); // 5-day reminder

        // Complete review session
        Long sessionId = startReviewSession();
        submitAnswer(sessionId, cardId, AnswerQuality.CORRECT);
        completeReviewSession(sessionId);

        // Complete todo item
        mockMvc.perform(post("/api/todos/{id}/complete", todoItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // Verify database consistency
        todoItem = todoItemRepository.findById(todoItem.getId()).get();
        assertTrue(todoItem.getCompleted());

        // Verify new todo item was scheduled for next review
        Card card = cardRepository.findById(cardId).get();
        LocalDate nextReviewDate = card.getNextReviewDate();
        
        List<TodoItem> futureTodos = todoItemRepository.findByCompletedFalseOrderByDueDateAsc();
        boolean foundNextReviewTodo = futureTodos.stream()
                .anyMatch(todo -> todo.getRelatedCardId().equals(cardId) &&
                                 todo.getDueDate().equals(nextReviewDate));
        assertTrue(foundNextReviewTodo, "Next review todo should be scheduled");
    }

    @Test
    void testCascadingDeleteConsistency() throws Exception {
        // Create material with cards
        Long materialId = uploadTestMaterial("Delete Test Material", MaterialType.DOCUMENT);
        Long card1Id = createTestCard(materialId, "delete word 1", "delete context 1");
        Long card2Id = createTestCard(materialId, "delete word 2", "delete context 2");

        // Create review session with records
        Long sessionId = startReviewSession();
        submitAnswer(sessionId, card1Id, AnswerQuality.CORRECT);
        submitAnswer(sessionId, card2Id, AnswerQuality.PERFECT);
        completeReviewSession(sessionId);

        // Verify data exists
        assertTrue(materialRepository.existsById(materialId));
        assertTrue(cardRepository.existsById(card1Id));
        assertTrue(cardRepository.existsById(card2Id));
        assertTrue(reviewSessionRepository.existsById(sessionId));
        assertEquals(2, reviewRecordRepository.findAll().size());

        // Delete material
        mockMvc.perform(delete("/api/materials/{id}", materialId))
                .andExpect(status().isNoContent());

        // Verify cascading deletes
        assertFalse(materialRepository.existsById(materialId));
        assertFalse(cardRepository.existsById(card1Id));
        assertFalse(cardRepository.existsById(card2Id));
        
        // Review sessions and records should still exist (they're not tied to material directly)
        assertTrue(reviewSessionRepository.existsById(sessionId));
        assertEquals(2, reviewRecordRepository.findAll().size());

        // Todo items related to deleted cards should be cleaned up
        List<TodoItem> remainingTodos = todoItemRepository.findByRelatedCardIdOrderByDueDateAsc(card1Id);
        assertTrue(remainingTodos.isEmpty());
    }

    @Test
    void testTransactionConsistency() throws Exception {
        // This test verifies that operations are properly transactional
        Long materialId = uploadTestMaterial("Transaction Test", MaterialType.DOCUMENT);
        
        // Attempt to create card with invalid data (should rollback)
        CardParamsDto invalidParams = new CardParamsDto();
        invalidParams.setMaterialId(materialId);
        invalidParams.setText(""); // Invalid: empty text
        invalidParams.setContext("test context");

        mockMvc.perform(post("/api/cards/from-highlight")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidParams)))
                .andExpect(status().isBadRequest());

        // Verify no card was created
        List<Card> cards = cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId);
        assertEquals(0, cards.size());

        // Verify material card count is still 0
        StudyMaterial material = materialRepository.findById(materialId).get();
        assertEquals(0, cardRepository.findByMaterialIdOrderByStartPositionAsc(materialId).size());
    }

    // Helper methods

    private Long uploadTestMaterial(String title, MaterialType type) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.pdf", 
                "application/pdf", 
                "Test content".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/materials")
                .file(file)
                .param("title", title)
                .param("type", type.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        StudyMaterialResultDto material = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                StudyMaterialResultDto.class
        );

        return material.getId();
    }

    private Long createTestCard(Long materialId, String text, String context) throws Exception {
        CardParamsDto params = new CardParamsDto();
        params.setMaterialId(materialId);
        params.setText(text);
        params.setContext(context);
        params.setStartPosition(0);
        params.setEndPosition(text.length());
        params.setUserComment("Test comment");

        MvcResult result = mockMvc.perform(post("/api/cards/from-highlight")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated())
                .andReturn();

        CardResultDto card = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                CardResultDto.class
        );

        return card.getId();
    }

    private Long startReviewSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reviews/sessions"))
                .andExpect(status().isCreated())
                .andReturn();

        ReviewSessionResultDto session = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ReviewSessionResultDto.class
        );

        return session.getId();
    }

    private void submitAnswer(Long sessionId, Long cardId, AnswerQuality quality) throws Exception {
        AnswerParamsDto answer = new AnswerParamsDto();
        answer.setCardId(cardId);
        answer.setQuality(quality);
        answer.setResponseTimeSeconds(5);

        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/answers", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answer)))
                .andExpect(status().isOk());
    }

    private void completeReviewSession(Long sessionId) throws Exception {
        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/complete", sessionId))
                .andExpect(status().isOk());
    }
}