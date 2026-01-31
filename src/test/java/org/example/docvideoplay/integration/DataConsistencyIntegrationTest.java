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
    private HighlightRepository highlightRepository;

    @Autowired
    private ReviewSessionRepository reviewSessionRepository;

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Test
    void testMaterialHighlightConsistency() throws Exception {
        // Upload material
        Long materialId = uploadTestMaterial("Test Material", MaterialType.DOCUMENT);

        // Verify material exists in database
        Optional<StudyMaterial> materialOpt = materialRepository.findById(materialId);
        assertTrue(materialOpt.isPresent());
        StudyMaterial material = materialOpt.get();
        assertEquals("Test Material", material.getTitle());
        assertEquals(0, material.getHighlights().size());

        // Create highlights
        Long highlight1Id = createTestHighlight(materialId, "word1", "context1");
        Long highlight2Id = createTestHighlight(materialId, "word2", "context2");

        // Verify highlights exist and are linked to material
        Optional<Highlight> highlight1Opt = highlightRepository.findById(highlight1Id);
        Optional<Highlight> highlight2Opt = highlightRepository.findById(highlight2Id);
        
        assertTrue(highlight1Opt.isPresent());
        assertTrue(highlight2Opt.isPresent());
        
        Highlight highlight1 = highlight1Opt.get();
        Highlight highlight2 = highlight2Opt.get();
        
        assertEquals(materialId, highlight1.getMaterial().getId());
        assertEquals(materialId, highlight2.getMaterial().getId());

        // Refresh material and verify highlight count
        material = materialRepository.findById(materialId).get();
        assertEquals(2, material.getHighlights().size());

        // Verify API returns consistent data
        mockMvc.perform(get("/api/materials/{id}", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlightCount").value(2));

        mockMvc.perform(get("/api/vocabulary/materials/{materialId}/highlights", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testReviewSessionDataConsistency() throws Exception {
        // Setup: Create material and highlights
        Long materialId = uploadTestMaterial("Review Test Material", MaterialType.DOCUMENT);
        Long highlight1Id = createTestHighlight(materialId, "review word 1", "context 1");
        Long highlight2Id = createTestHighlight(materialId, "review word 2", "context 2");

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
        submitAnswer(sessionId, highlight1Id, AnswerQuality.CORRECT);
        submitAnswer(sessionId, highlight2Id, AnswerQuality.PERFECT);

        // Verify review records were created
        List<ReviewRecord> records = reviewRecordRepository.findAll();
        assertEquals(2, records.size());

        for (ReviewRecord record : records) {
            assertEquals(sessionId, record.getSession().getId());
            assertTrue(record.getHighlight().getId().equals(highlight1Id) || 
                      record.getHighlight().getId().equals(highlight2Id));
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
        // Create material and highlight
        Long materialId = uploadTestMaterial("Spaced Repetition Test", MaterialType.DOCUMENT);
        Long highlightId = createTestHighlight(materialId, "spaced word", "spaced context");

        // Verify initial spaced repetition values
        Highlight highlight = highlightRepository.findById(highlightId).get();
        assertEquals(2.5, highlight.getEaseFactor());
        assertEquals(0, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays());
        assertNotNull(highlight.getNextReviewDate());
        assertNull(highlight.getLastReviewDate());

        // Start review session and answer
        Long sessionId = startReviewSession();
        submitAnswer(sessionId, highlightId, AnswerQuality.CORRECT);
        completeReviewSession(sessionId);

        // Verify spaced repetition values updated
        highlight = highlightRepository.findById(highlightId).get();
        assertEquals(1, highlight.getRepetitionCount());
        assertEquals(1, highlight.getIntervalDays()); // First correct answer = 1 day interval (SM-2 standard)
        assertNotNull(highlight.getLastReviewDate());
        assertNotNull(highlight.getNextReviewDate());
        assertTrue(highlight.getNextReviewDate().isAfter(LocalDate.now()));

        // Verify API consistency
        mockMvc.perform(get("/api/vocabulary/highlights/{id}", highlightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repetitionCount").value(1))
                .andExpect(jsonPath("$.intervalDays").value(1)) // First correct answer = 1 day
                .andExpect(jsonPath("$.lastReviewDate").exists())
                .andExpect(jsonPath("$.nextReviewDate").exists());
    }

    @Test
    void testTodoItemConsistency() throws Exception {
        // Create material and highlight
        Long materialId = uploadTestMaterial("Todo Test Material", MaterialType.DOCUMENT);
        Long highlightId = createTestHighlight(materialId, "todo word", "todo context");

        // Verify todo item was created automatically
        List<TodoItem> todoItems = todoItemRepository.findByCompletedFalseOrderByDueDateAsc();
        assertEquals(1, todoItems.size());
        
        TodoItem todoItem = todoItems.get(0);
        assertEquals(highlightId, todoItem.getRelatedHighlight().getId());
        assertFalse(todoItem.getCompleted());
        assertEquals(LocalDate.now().plusDays(5), todoItem.getDueDate()); // 5-day reminder

        // Complete review session
        Long sessionId = startReviewSession();
        submitAnswer(sessionId, highlightId, AnswerQuality.CORRECT);
        completeReviewSession(sessionId);

        // Complete todo item
        mockMvc.perform(post("/api/todos/{id}/complete", todoItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // Verify database consistency
        todoItem = todoItemRepository.findById(todoItem.getId()).get();
        assertTrue(todoItem.getCompleted());

        // Verify new todo item was scheduled for next review
        Highlight highlight = highlightRepository.findById(highlightId).get();
        LocalDate nextReviewDate = highlight.getNextReviewDate();
        
        List<TodoItem> futureTodos = todoItemRepository.findByCompletedFalseOrderByDueDateAsc();
        boolean foundNextReviewTodo = futureTodos.stream()
                .anyMatch(todo -> todo.getRelatedHighlight().getId().equals(highlightId) &&
                                 todo.getDueDate().equals(nextReviewDate));
        assertTrue(foundNextReviewTodo, "Next review todo should be scheduled");
    }

    @Test
    void testCascadingDeleteConsistency() throws Exception {
        // Create material with highlights
        Long materialId = uploadTestMaterial("Delete Test Material", MaterialType.DOCUMENT);
        Long highlight1Id = createTestHighlight(materialId, "delete word 1", "delete context 1");
        Long highlight2Id = createTestHighlight(materialId, "delete word 2", "delete context 2");

        // Create review session with records
        Long sessionId = startReviewSession();
        submitAnswer(sessionId, highlight1Id, AnswerQuality.CORRECT);
        submitAnswer(sessionId, highlight2Id, AnswerQuality.PERFECT);
        completeReviewSession(sessionId);

        // Verify data exists
        assertTrue(materialRepository.existsById(materialId));
        assertTrue(highlightRepository.existsById(highlight1Id));
        assertTrue(highlightRepository.existsById(highlight2Id));
        assertTrue(reviewSessionRepository.existsById(sessionId));
        assertEquals(2, reviewRecordRepository.findAll().size());

        // Delete material
        mockMvc.perform(delete("/api/materials/{id}", materialId))
                .andExpect(status().isNoContent());

        // Verify cascading deletes
        assertFalse(materialRepository.existsById(materialId));
        assertFalse(highlightRepository.existsById(highlight1Id));
        assertFalse(highlightRepository.existsById(highlight2Id));
        
        // Review sessions and records should still exist (they're not tied to material directly)
        assertTrue(reviewSessionRepository.existsById(sessionId));
        assertEquals(2, reviewRecordRepository.findAll().size());

        // Todo items related to deleted highlights should be cleaned up
        List<TodoItem> remainingTodos = todoItemRepository.findByRelatedHighlightIdOrderByDueDateAsc(highlight1Id);
        assertTrue(remainingTodos.isEmpty());
    }

    @Test
    void testTransactionConsistency() throws Exception {
        // This test verifies that operations are properly transactional
        Long materialId = uploadTestMaterial("Transaction Test", MaterialType.DOCUMENT);
        
        // Attempt to create highlight with invalid data (should rollback)
        HighlightParamsDto invalidParams = new HighlightParamsDto();
        invalidParams.setMaterialId(materialId);
        invalidParams.setText(""); // Invalid: empty text
        invalidParams.setContext("test context");

        mockMvc.perform(post("/api/vocabulary/highlights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidParams)))
                .andExpect(status().isBadRequest());

        // Verify no highlight was created
        List<Highlight> highlights = highlightRepository.findByMaterialIdOrderByStartPositionAsc(materialId);
        assertEquals(0, highlights.size());

        // Verify material highlight count is still 0
        StudyMaterial material = materialRepository.findById(materialId).get();
        assertEquals(0, material.getHighlights().size());
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

    private Long createTestHighlight(Long materialId, String text, String context) throws Exception {
        HighlightParamsDto params = new HighlightParamsDto();
        params.setMaterialId(materialId);
        params.setText(text);
        params.setContext(context);
        params.setStartPosition(0);
        params.setEndPosition(text.length());
        params.setUserComment("Test comment");

        MvcResult result = mockMvc.perform(post("/api/vocabulary/highlights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated())
                .andReturn();

        HighlightResultDto highlight = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                HighlightResultDto.class
        );

        return highlight.getId();
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

    private void submitAnswer(Long sessionId, Long highlightId, AnswerQuality quality) throws Exception {
        AnswerParamsDto answer = new AnswerParamsDto();
        answer.setHighlightId(highlightId);
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