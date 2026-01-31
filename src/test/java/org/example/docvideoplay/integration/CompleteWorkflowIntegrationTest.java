package org.example.docvideoplay.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.docvideoplay.DocVideoPlayApplication;
import org.example.docvideoplay.dto.api.*;
import org.example.docvideoplay.entity.*;
import org.example.docvideoplay.enums.AnswerQuality;
import org.example.docvideoplay.enums.MaterialType;
import org.example.docvideoplay.enums.TodoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complete user workflows
 * Tests the entire flow from material upload to review completion
 */
@SpringBootTest(classes = DocVideoPlayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class CompleteWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long materialId;
    private Long highlightId;
    private Long reviewSessionId;
    private Long todoItemId;

    @BeforeEach
    void setUp() {
        // Reset IDs for each test
        materialId = null;
        highlightId = null;
        reviewSessionId = null;
        todoItemId = null;
    }

    @Test
    @Order(1)
    void testCompleteWorkflow_UploadToReviewCompletion() throws Exception {
        // Step 1: Upload study material
        materialId = uploadTestMaterial();
        assertNotNull(materialId, "Material should be uploaded successfully");

        // Step 2: Create highlight
        highlightId = createTestHighlight(materialId);
        assertNotNull(highlightId, "Highlight should be created successfully");

        // Step 3: Verify todo item was created automatically
        todoItemId = verifyTodoItemCreated(highlightId);
        assertNotNull(todoItemId, "Todo item should be created automatically");

        // Step 4: Start review session
        reviewSessionId = startReviewSession();
        assertNotNull(reviewSessionId, "Review session should start successfully");

        // Step 5: Complete review workflow
        completeReviewWorkflow(reviewSessionId, highlightId);

        // Step 6: Verify todo item completion
        verifyTodoItemCompleted(todoItemId);

        // Step 7: Verify spaced repetition scheduling
        verifySpacedRepetitionScheduling(highlightId);
    }

    @Test
    @Order(2)
    void testMultipleHighlightsWorkflow() throws Exception {
        // Upload material
        materialId = uploadTestMaterial();

        // Create multiple highlights
        Long highlight1 = createTestHighlight(materialId, "first word", "Context for first word");
        Long highlight2 = createTestHighlight(materialId, "second word", "Context for second word");
        Long highlight3 = createTestHighlight(materialId, "third word", "Context for third word");

        // Start review session
        reviewSessionId = startReviewSession();

        // Verify session has multiple questions
        mockMvc.perform(get("/api/reviews/sessions/{sessionId}", reviewSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestions", is(3)));

        // Answer all questions
        answerQuestion(reviewSessionId, highlight1, AnswerQuality.CORRECT);
        answerQuestion(reviewSessionId, highlight2, AnswerQuality.PERFECT);
        answerQuestion(reviewSessionId, highlight3, AnswerQuality.DIFFICULT);

        // Complete session
        MvcResult result = mockMvc.perform(post("/api/reviews/sessions/{sessionId}/complete", reviewSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.totalQuestions", is(3)))
                .andExpect(jsonPath("$.correctAnswers", is(3)))
                .andReturn();

        ReviewSessionResultDto session = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ReviewSessionResultDto.class
        );

        assertEquals(100, session.getAccuracyPercentage());
    }

    @Test
    @Order(3)
    void testErrorHandlingInWorkflow() throws Exception {
        // Test starting review session with no highlights
        mockMvc.perform(post("/api/reviews/sessions"))
                .andExpect(status().isNoContent()); // Should return 204 when no highlights available

        // Upload material and create highlight
        materialId = uploadTestMaterial();
        highlightId = createTestHighlight(materialId);

        // Start review session
        reviewSessionId = startReviewSession();

        // Test submitting answer for non-existent highlight
        AnswerParamsDto invalidAnswer = new AnswerParamsDto();
        invalidAnswer.setHighlightId(99999L);
        invalidAnswer.setQuality(AnswerQuality.CORRECT);
        invalidAnswer.setResponseTimeSeconds(5);

        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/answers", reviewSessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAnswer)))
                .andExpect(status().isBadRequest());

        // Test completing non-existent session
        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/complete", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void testDataConsistencyAcrossWorkflow() throws Exception {
        // Upload material
        materialId = uploadTestMaterial();

        // Verify material exists and has correct data
        mockMvc.perform(get("/api/materials/{id}", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(materialId.intValue())))
                .andExpect(jsonPath("$.title", is("Test Document")))
                .andExpect(jsonPath("$.type", is("DOCUMENT")))
                .andExpect(jsonPath("$.highlightCount", is(0)));

        // Create highlight
        highlightId = createTestHighlight(materialId);

        // Verify material highlight count updated
        mockMvc.perform(get("/api/materials/{id}", materialId))
                .andExpect(jsonPath("$.highlightCount", is(1)));

        // Verify highlight has correct spaced repetition data
        mockMvc.perform(get("/api/vocabulary/highlights/{id}", highlightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId", is(materialId.intValue())))
                .andExpect(jsonPath("$.easeFactor", is(2.5)))
                .andExpect(jsonPath("$.repetitionCount", is(0)))
                .andExpect(jsonPath("$.intervalDays", is(1)))
                .andExpect(jsonPath("$.nextReviewDate", notNullValue()));

        // Start and complete review
        reviewSessionId = startReviewSession();
        answerQuestion(reviewSessionId, highlightId, AnswerQuality.CORRECT);
        
        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/complete", reviewSessionId))
                .andExpect(status().isOk());

        // Verify highlight spaced repetition data updated
        mockMvc.perform(get("/api/vocabulary/highlights/{id}", highlightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repetitionCount", is(1)))
                .andExpect(jsonPath("$.intervalDays", is(1))) // First correct answer = 1 day (SM-2 standard)
                .andExpect(jsonPath("$.lastReviewDate", notNullValue()));
    }

    @Test
    @Order(5)
    void testConcurrentOperations() throws Exception {
        // Upload material
        materialId = uploadTestMaterial();

        // Create multiple highlights concurrently (simulated)
        HighlightParamsDto highlight1 = createHighlightParams(materialId, "word1", "context1");
        HighlightParamsDto highlight2 = createHighlightParams(materialId, "word2", "context2");
        HighlightParamsDto highlight3 = createHighlightParams(materialId, "word3", "context3");

        // Create highlights
        MvcResult result1 = mockMvc.perform(post("/api/vocabulary/highlights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(highlight1)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/vocabulary/highlights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(highlight2)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result3 = mockMvc.perform(post("/api/vocabulary/highlights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(highlight3)))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify all highlights were created with unique IDs
        HighlightResultDto h1 = objectMapper.readValue(result1.getResponse().getContentAsString(), HighlightResultDto.class);
        HighlightResultDto h2 = objectMapper.readValue(result2.getResponse().getContentAsString(), HighlightResultDto.class);
        HighlightResultDto h3 = objectMapper.readValue(result3.getResponse().getContentAsString(), HighlightResultDto.class);

        assertNotEquals(h1.getId(), h2.getId());
        assertNotEquals(h2.getId(), h3.getId());
        assertNotEquals(h1.getId(), h3.getId());

        // Verify material has correct highlight count
        mockMvc.perform(get("/api/materials/{id}", materialId))
                .andExpect(jsonPath("$.highlightCount", is(3)));
    }

    // Helper methods

    private Long uploadTestMaterial() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                "Test document content".getBytes()
        );

        StudyMaterialParamsDto params = new StudyMaterialParamsDto();
        params.setTitle("Test Document");
        params.setType(MaterialType.DOCUMENT);

        MvcResult result = mockMvc.perform(multipart("/api/materials")
                .file(file)
                .param("title", params.getTitle())
                .param("type", params.getType().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Document")))
                .andExpect(jsonPath("$.type", is("DOCUMENT")))
                .andReturn();

        StudyMaterialResultDto material = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                StudyMaterialResultDto.class
        );

        return material.getId();
    }

    private Long createTestHighlight(Long materialId) throws Exception {
        return createTestHighlight(materialId, "test vocabulary", "This is a test vocabulary word in context.");
    }

    private Long createTestHighlight(Long materialId, String text, String context) throws Exception {
        HighlightParamsDto params = createHighlightParams(materialId, text, context);

        MvcResult result = mockMvc.perform(post("/api/vocabulary/highlights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.materialId", is(materialId.intValue())))
                .andExpect(jsonPath("$.text", is(text)))
                .andReturn();

        HighlightResultDto highlight = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                HighlightResultDto.class
        );

        return highlight.getId();
    }

    private HighlightParamsDto createHighlightParams(Long materialId, String text, String context) {
        HighlightParamsDto params = new HighlightParamsDto();
        params.setMaterialId(materialId);
        params.setText(text);
        params.setContext(context);
        params.setStartPosition(10);
        params.setEndPosition(10 + text.length());
        params.setUserComment("Test comment for " + text);
        return params;
    }

    private Long verifyTodoItemCreated(Long highlightId) throws Exception {
        // Check if todo item was created for the highlight
        MvcResult result = mockMvc.perform(get("/api/todos")
                .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andReturn();

        TodoItemResultDto[] todos = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                TodoItemResultDto[].class
        );

        // Find todo item related to our highlight
        for (TodoItemResultDto todo : todos) {
            if (todo.getRelatedHighlightId() != null && todo.getRelatedHighlightId().equals(highlightId)) {
                assertEquals(TodoType.REVIEW_SESSION, todo.getType());
                assertFalse(todo.getCompleted());
                return todo.getId();
            }
        }

        fail("Todo item not found for highlight " + highlightId);
        return null;
    }

    private Long startReviewSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reviews/sessions"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.totalQuestions", greaterThan(0)))
                .andReturn();

        ReviewSessionResultDto session = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ReviewSessionResultDto.class
        );

        return session.getId();
    }

    private void completeReviewWorkflow(Long sessionId, Long highlightId) throws Exception {
        // Get first question
        mockMvc.perform(get("/api/reviews/sessions/{sessionId}/next-question", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlightId", is(highlightId.intValue())));

        // Submit answer
        answerQuestion(sessionId, highlightId, AnswerQuality.CORRECT);

        // Complete session
        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/complete", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    private void answerQuestion(Long sessionId, Long highlightId, AnswerQuality quality) throws Exception {
        AnswerParamsDto answer = new AnswerParamsDto();
        answer.setHighlightId(highlightId);
        answer.setQuality(quality);
        answer.setResponseTimeSeconds(5);

        mockMvc.perform(post("/api/reviews/sessions/{sessionId}/answers", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answer)))
                .andExpect(status().isOk());
    }

    private void verifyTodoItemCompleted(Long todoId) throws Exception {
        // Complete the todo item
        mockMvc.perform(post("/api/todos/{id}/complete", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    private void verifySpacedRepetitionScheduling(Long highlightId) throws Exception {
        // Verify that the highlight's spaced repetition data was updated
        mockMvc.perform(get("/api/vocabulary/highlights/{id}", highlightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repetitionCount", greaterThan(0)))
                .andExpect(jsonPath("$.lastReviewDate", notNullValue()))
                .andExpect(jsonPath("$.nextReviewDate", notNullValue()));

        // Verify that a new todo item was scheduled for the next review
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/todos")
                .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.relatedHighlightId == " + highlightId + " && @.dueDate > '" + today + "')]", hasSize(1)));
    }
}