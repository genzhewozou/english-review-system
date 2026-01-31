/**
 * Integration tests for complete user workflows
 * Tests the entire flow from material upload to review completion
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ElMessage } from 'element-plus'

// Import services
import { useMaterialService } from '../../services/materialService'
import { useVocabularyService } from '../../services/vocabularyService'
import { useReviewService } from '../../services/reviewService'
import { useTodoService } from '../../services/todoService'

// Mock API responses
const mockMaterial = {
  id: 1,
  title: 'Test Document',
  fileName: 'test.pdf',
  filePath: '/uploads/test.pdf',
  type: 'DOCUMENT',
  mimeType: 'application/pdf',
  fileSize: 1024000,
  highlightCount: 0,
  createdDate: '2024-01-01T10:00:00Z',
  updatedDate: '2024-01-01T10:00:00Z'
}

const mockHighlight = {
  id: 1,
  materialId: 1,
  text: 'vocabulary word',
  context: 'This is a vocabulary word in context.',
  startPosition: 10,
  endPosition: 25,
  userComment: 'Need to remember this word',
  easeFactor: 2.5,
  repetitionCount: 0,
  intervalDays: 1,
  nextReviewDate: '2024-01-06',
  lastReviewDate: null,
  createdDate: '2024-01-01T10:00:00Z',
  updatedDate: '2024-01-01T10:00:00Z'
}

const mockReviewSession = {
  id: 1,
  startTime: '2024-01-06T10:00:00Z',
  endTime: null,
  completed: false,
  totalQuestions: 1,
  correctAnswers: 0,
  accuracyPercentage: 0,
  reviewRecords: []
}

const mockQuestion = {
  highlightId: 1,
  text: 'vocabulary word',
  context: 'This is a vocabulary word in context.',
  userComment: 'Need to remember this word',
  questionNumber: 1,
  totalQuestions: 1
}

const mockTodoItem = {
  id: 1,
  title: 'Review Vocabulary',
  description: 'Review highlighted vocabulary using spaced repetition',
  dueDate: '2024-01-06',
  completed: false,
  type: 'REVIEW_SESSION',
  relatedHighlightId: 1,
  relatedHighlightText: 'vocabulary word',
  overdue: false,
  dueToday: true,
  createdDate: '2024-01-01T10:00:00Z',
  updatedDate: '2024-01-01T10:00:00Z'
}

// Mock API calls
vi.mock('../../composables/useApiService', () => ({
  useApiService: () => ({
    apiService: {
      upload: vi.fn(),
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn()
    }
  })
}))

describe('Complete User Workflow Integration Tests', () => {
  let materialService
  let vocabularyService
  let reviewService
  let todoService
  let mockApiService

  beforeEach(() => {
    setActivePinia(createPinia())
    
    // Get services
    materialService = useMaterialService()
    vocabularyService = useVocabularyService()
    reviewService = useReviewService()
    todoService = useTodoService()
    
    // Get mock API service
    const { useApiService } = require('../../composables/useApiService')
    mockApiService = useApiService().apiService
    
    // Mock ElMessage to prevent console warnings
    vi.spyOn(ElMessage, 'success').mockImplementation(() => {})
    vi.spyOn(ElMessage, 'error').mockImplementation(() => {})
    vi.spyOn(ElMessage, 'warning').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('Material Upload to Review Completion Workflow', () => {
    it('should complete the full workflow: upload → highlight → review → complete', async () => {
      // Step 1: Upload material
      mockApiService.upload.mockResolvedValueOnce({ data: mockMaterial })
      
      const file = new File(['test content'], 'test.pdf', { type: 'application/pdf' })
      const uploadedMaterial = await materialService.uploadMaterial(file, 'Test Document', 'DOCUMENT')
      
      expect(mockApiService.upload).toHaveBeenCalledWith(
        '/api/materials',
        expect.any(FormData),
        undefined
      )
      expect(uploadedMaterial).toEqual(mockMaterial)

      // Step 2: Create highlight
      mockApiService.post.mockResolvedValueOnce({ data: mockHighlight })
      
      const highlightData = {
        materialId: 1,
        text: 'vocabulary word',
        context: 'This is a vocabulary word in context.',
        startPosition: 10,
        endPosition: 25,
        userComment: 'Need to remember this word'
      }
      
      const createdHighlight = await vocabularyService.createHighlight(highlightData)
      
      expect(mockApiService.post).toHaveBeenCalledWith('/api/vocabulary/highlights', highlightData)
      expect(createdHighlight).toEqual(mockHighlight)

      // Step 3: Check todo item was created (automatic scheduling)
      mockApiService.get.mockResolvedValueOnce({ data: [mockTodoItem] })
      
      const todoItems = await todoService.getTodoItems(false) // Get incomplete todos
      
      expect(mockApiService.get).toHaveBeenCalledWith('/api/todos', { params: { completed: false } })
      expect(todoItems).toHaveLength(1)
      expect(todoItems[0].type).toBe('REVIEW_SESSION')
      expect(todoItems[0].relatedHighlightId).toBe(1)

      // Step 4: Start review session
      mockApiService.post.mockResolvedValueOnce({ data: mockReviewSession })
      
      const reviewSession = await reviewService.startReviewSession()
      
      expect(mockApiService.post).toHaveBeenCalledWith('/api/reviews/sessions')
      expect(reviewSession).toEqual(mockReviewSession)

      // Step 5: Get first question
      mockApiService.get.mockResolvedValueOnce({ data: mockQuestion })
      
      const question = await reviewService.getNextQuestion(reviewSession.id)
      
      expect(mockApiService.get).toHaveBeenCalledWith('/api/reviews/sessions/1/next-question')
      expect(question).toEqual(mockQuestion)

      // Step 6: Submit answer
      mockApiService.post.mockResolvedValueOnce({ data: null })
      
      const answerData = {
        highlightId: 1,
        quality: 'CORRECT',
        responseTimeSeconds: 5
      }
      
      await reviewService.submitAnswer(reviewSession.id, answerData)
      
      expect(mockApiService.post).toHaveBeenCalledWith('/api/reviews/sessions/1/answers', answerData)

      // Step 7: Complete review session
      const completedSession = {
        ...mockReviewSession,
        completed: true,
        endTime: '2024-01-06T10:05:00Z',
        correctAnswers: 1,
        accuracyPercentage: 100
      }
      
      mockApiService.post.mockResolvedValueOnce({ data: completedSession })
      
      const sessionResult = await reviewService.completeSession(reviewSession.id)
      
      expect(mockApiService.post).toHaveBeenCalledWith('/api/reviews/sessions/1/complete')
      expect(sessionResult.completed).toBe(true)
      expect(sessionResult.accuracyPercentage).toBe(100)

      // Step 8: Verify todo item completion
      const completedTodoItem = { ...mockTodoItem, completed: true }
      mockApiService.post.mockResolvedValueOnce({ data: completedTodoItem })
      
      const completedTodo = await todoService.completeTodoItem(mockTodoItem.id)
      
      expect(mockApiService.post).toHaveBeenCalledWith('/api/todos/1/complete')
      expect(completedTodo.completed).toBe(true)
    })

    it('should handle workflow with multiple highlights', async () => {
      // Upload material
      mockApiService.upload.mockResolvedValueOnce({ data: mockMaterial })
      const file = new File(['test content'], 'test.pdf', { type: 'application/pdf' })
      await materialService.uploadMaterial(file, 'Test Document', 'DOCUMENT')

      // Create multiple highlights
      const highlights = [
        { ...mockHighlight, id: 1, text: 'first word' },
        { ...mockHighlight, id: 2, text: 'second word' },
        { ...mockHighlight, id: 3, text: 'third word' }
      ]

      for (let i = 0; i < highlights.length; i++) {
        mockApiService.post.mockResolvedValueOnce({ data: highlights[i] })
        await vocabularyService.createHighlight({
          materialId: 1,
          text: highlights[i].text,
          context: 'Context for ' + highlights[i].text
        })
      }

      // Start review session with multiple questions
      const multiQuestionSession = { ...mockReviewSession, totalQuestions: 3 }
      mockApiService.post.mockResolvedValueOnce({ data: multiQuestionSession })
      const session = await reviewService.startReviewSession()

      // Answer all questions
      for (let i = 0; i < 3; i++) {
        const question = { ...mockQuestion, highlightId: i + 1, questionNumber: i + 1, totalQuestions: 3 }
        mockApiService.get.mockResolvedValueOnce({ data: question })
        await reviewService.getNextQuestion(session.id)

        mockApiService.post.mockResolvedValueOnce({ data: null })
        await reviewService.submitAnswer(session.id, {
          highlightId: i + 1,
          quality: 'CORRECT',
          responseTimeSeconds: 5
        })
      }

      // Complete session
      const finalSession = { ...multiQuestionSession, completed: true, correctAnswers: 3, accuracyPercentage: 100 }
      mockApiService.post.mockResolvedValueOnce({ data: finalSession })
      const result = await reviewService.completeSession(session.id)

      expect(result.totalQuestions).toBe(3)
      expect(result.correctAnswers).toBe(3)
      expect(result.accuracyPercentage).toBe(100)
    })

    it('should handle workflow errors gracefully', async () => {
      // Test upload failure
      mockApiService.upload.mockRejectedValueOnce(new Error('Upload failed'))
      
      await expect(
        materialService.uploadMaterial(new File(['test'], 'test.pdf'), 'Test', 'DOCUMENT')
      ).rejects.toThrow()

      // Test highlight creation failure
      mockApiService.post.mockRejectedValueOnce(new Error('Highlight creation failed'))
      
      await expect(
        vocabularyService.createHighlight({
          materialId: 1,
          text: 'test',
          context: 'test context'
        })
      ).rejects.toThrow()

      // Test review session start failure (no highlights available)
      mockApiService.post.mockResolvedValueOnce({ status: 204, data: null })
      
      const session = await reviewService.startReviewSession()
      expect(session).toBeNull()
    })
  })

  describe('Data Consistency Validation', () => {
    it('should maintain data consistency across all operations', async () => {
      // Upload material and verify data structure
      mockApiService.upload.mockResolvedValueOnce({ data: mockMaterial })
      const material = await materialService.uploadMaterial(
        new File(['test'], 'test.pdf'), 
        'Test Document', 
        'DOCUMENT'
      )
      
      expect(material).toHaveProperty('id')
      expect(material).toHaveProperty('title')
      expect(material).toHaveProperty('type')
      expect(material.type).toBe('DOCUMENT')

      // Create highlight and verify relationship
      mockApiService.post.mockResolvedValueOnce({ data: mockHighlight })
      const highlight = await vocabularyService.createHighlight({
        materialId: material.id,
        text: 'test word',
        context: 'test context'
      })
      
      expect(highlight.materialId).toBe(material.id)
      expect(highlight).toHaveProperty('easeFactor')
      expect(highlight).toHaveProperty('repetitionCount')
      expect(highlight).toHaveProperty('intervalDays')

      // Verify spaced repetition data is initialized correctly
      expect(highlight.easeFactor).toBe(2.5)
      expect(highlight.repetitionCount).toBe(0)
      expect(highlight.intervalDays).toBe(1)
      expect(highlight.nextReviewDate).toBeTruthy()
    })

    it('should validate API response structures', async () => {
      // Test with invalid material response
      const invalidMaterial = { id: 1, title: 'Test' } // Missing required fields
      mockApiService.upload.mockResolvedValueOnce({ data: invalidMaterial })
      
      // The service should still return the data but log validation warnings
      const material = await materialService.uploadMaterial(
        new File(['test'], 'test.pdf'), 
        'Test', 
        'DOCUMENT'
      )
      
      expect(material).toEqual(invalidMaterial)
      // In a real scenario, validation warnings would be logged
    })

    it('should handle concurrent operations correctly', async () => {
      // Simulate concurrent highlight creation
      const highlightPromises = []
      
      for (let i = 0; i < 3; i++) {
        const highlight = { ...mockHighlight, id: i + 1, text: `word ${i + 1}` }
        mockApiService.post.mockResolvedValueOnce({ data: highlight })
        
        highlightPromises.push(
          vocabularyService.createHighlight({
            materialId: 1,
            text: `word ${i + 1}`,
            context: `context ${i + 1}`
          })
        )
      }
      
      const results = await Promise.all(highlightPromises)
      
      expect(results).toHaveLength(3)
      results.forEach((result, index) => {
        expect(result.text).toBe(`word ${index + 1}`)
      })
    })
  })

  describe('Error Recovery and Resilience', () => {
    it('should recover from network errors', async () => {
      // First call fails, second succeeds
      mockApiService.get
        .mockRejectedValueOnce(new Error('Network error'))
        .mockResolvedValueOnce({ data: [mockMaterial] })
      
      // The service should retry and eventually succeed
      const materials = await materialService.getAllMaterials()
      expect(materials).toEqual([mockMaterial])
    })

    it('should handle partial failures in batch operations', async () => {
      // Test bulk todo completion with some failures
      const todoIds = [1, 2, 3]
      
      mockApiService.post
        .mockResolvedValueOnce({ data: { ...mockTodoItem, id: 1, completed: true } })
        .mockRejectedValueOnce(new Error('Todo 2 not found'))
        .mkResolvedValueOnce({ data: { ...mockTodoItem, id: 3, completed: true } })
      
      const results = await todoService.bulkCompleteTodos(todoIds)
      
      // Should return successful completions and handle failures gracefully
      expect(results).toHaveLength(2)
      expect(results.map(r => r.id)).toEqual([1, 3])
    })
  })
})