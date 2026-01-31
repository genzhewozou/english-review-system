import { useApiService } from '../composables/useApiService'

/**
 * Service for review and quiz system operations
 * Handles communication with the backend ReviewApi
 */
export function useReviewService() {
  const { apiService } = useApiService()

  /**
   * Start a new review session
   * @returns {Promise<Object>} Created review session data
   */
  const startReviewSession = async () => {
    try {
      const response = await apiService.post('/api/reviews/sessions')
      return response.data
    } catch (error) {
      console.error('Failed to start review session:', error)
      if (error.response?.status === 204) {
        // No content - no highlights available for review
        return null
      }
      throw new Error(error.response?.data?.message || 'Failed to start review session')
    }
  }

  /**
   * Get the next question in a review session
   * @param {number} sessionId - Review session ID
   * @returns {Promise<Object|null>} Next question data or null if no more questions
   */
  const getNextQuestion = async (sessionId) => {
    try {
      const response = await apiService.get(`/api/reviews/sessions/${sessionId}/next-question`)
      return response.data
    } catch (error) {
      console.error(`Failed to get next question for session ${sessionId}:`, error)
      if (error.response?.status === 204) {
        // No content - no more questions
        return null
      }
      if (error.response?.status === 404) {
        throw new Error('Review session not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to get next question')
    }
  }

  /**
   * Submit an answer for a question in a review session
   * @param {number} sessionId - Review session ID
   * @param {Object} answerData - Answer data
   * @param {number} answerData.highlightId - Highlight ID being answered
   * @param {string} answerData.quality - Answer quality (PERFECT, CORRECT, DIFFICULT, INCORRECT, REMEMBERED, BLACKOUT)
   * @param {number} answerData.responseTimeSeconds - Time taken to answer in seconds
   * @returns {Promise<void>}
   */
  const submitAnswer = async (sessionId, answerData) => {
    try {
      await apiService.post(`/api/reviews/sessions/${sessionId}/answers`, answerData)
    } catch (error) {
      console.error(`Failed to submit answer for session ${sessionId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Review session not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to submit answer')
    }
  }

  /**
   * Complete a review session
   * @param {number} sessionId - Review session ID
   * @returns {Promise<Object>} Completed session results
   */
  const completeSession = async (sessionId) => {
    try {
      const response = await apiService.post(`/api/reviews/sessions/${sessionId}/complete`)
      return response.data
    } catch (error) {
      console.error(`Failed to complete session ${sessionId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Review session not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to complete session')
    }
  }

  /**
   * Get a specific review session by ID
   * @param {number} sessionId - Review session ID
   * @returns {Promise<Object>} Review session data
   */
  const getReviewSession = async (sessionId) => {
    try {
      const response = await apiService.get(`/api/reviews/sessions/${sessionId}`)
      return response.data
    } catch (error) {
      console.error(`Failed to fetch review session ${sessionId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Review session not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch review session')
    }
  }

  /**
   * Get all review sessions with optional filtering
   * @param {boolean} completed - Filter by completion status (optional)
   * @returns {Promise<Array>} List of review sessions
   */
  const getReviewSessions = async (completed = null) => {
    try {
      const params = {}
      if (completed !== null) {
        params.completed = completed
      }
      
      const response = await apiService.get('/api/reviews/sessions', { params })
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch review sessions:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch review sessions')
    }
  }

  /**
   * Get completed review sessions
   * @returns {Promise<Array>} List of completed review sessions
   */
  const getCompletedSessions = async () => {
    return getReviewSessions(true)
  }

  /**
   * Get incomplete review sessions
   * @returns {Promise<Array>} List of incomplete review sessions
   */
  const getIncompleteSessions = async () => {
    return getReviewSessions(false)
  }

  /**
   * Delete a review session by ID
   * @param {number} sessionId - Review session ID
   * @returns {Promise<void>}
   */
  const deleteReviewSession = async (sessionId) => {
    try {
      await apiService.delete(`/api/reviews/sessions/${sessionId}`)
    } catch (error) {
      console.error(`Failed to delete review session ${sessionId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Review session not found')
      }
      if (error.response?.status === 501) {
        throw new Error('Session deletion not implemented yet')
      }
      throw new Error(error.response?.data?.message || 'Failed to delete review session')
    }
  }

  /**
   * Get session progress information
   * @param {number} sessionId - Review session ID
   * @returns {Promise<Object>} Progress information
   */
  const getSessionProgress = async (sessionId) => {
    try {
      const session = await getReviewSession(sessionId)
      const answeredQuestions = session.reviewRecords?.length || 0
      const totalQuestions = session.totalQuestions || 0
      
      return {
        answeredQuestions,
        totalQuestions,
        remainingQuestions: totalQuestions - answeredQuestions,
        progressPercentage: totalQuestions > 0 ? Math.round((answeredQuestions / totalQuestions) * 100) : 0,
        isComplete: session.completed || false
      }
    } catch (error) {
      console.error(`Failed to get session progress for ${sessionId}:`, error)
      throw new Error('Failed to get session progress')
    }
  }

  /**
   * Get review statistics
   * @returns {Promise<Object>} Review statistics
   */
  const getReviewStatistics = async () => {
    try {
      const sessions = await getReviewSessions()
      const completedSessions = sessions.filter(s => s.completed)
      
      const totalSessions = sessions.length
      const totalCompletedSessions = completedSessions.length
      const totalQuestions = completedSessions.reduce((sum, s) => sum + (s.totalQuestions || 0), 0)
      const totalCorrectAnswers = completedSessions.reduce((sum, s) => sum + (s.correctAnswers || 0), 0)
      
      const averageAccuracy = totalQuestions > 0 ? Math.round((totalCorrectAnswers / totalQuestions) * 100) : 0
      
      return {
        totalSessions,
        totalCompletedSessions,
        totalQuestions,
        totalCorrectAnswers,
        averageAccuracy,
        incompleteSessions: totalSessions - totalCompletedSessions
      }
    } catch (error) {
      console.error('Failed to get review statistics:', error)
      throw new Error('Failed to get review statistics')
    }
  }

  return {
    startReviewSession,
    getNextQuestion,
    submitAnswer,
    completeSession,
    getReviewSession,
    getReviewSessions,
    getCompletedSessions,
    getIncompleteSessions,
    deleteReviewSession,
    getSessionProgress,
    getReviewStatistics
  }
}