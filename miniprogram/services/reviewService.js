/**
 * Review Service for WeChat Mini Program
 * Matches frontend/src/services/reviewService.js
 */

const api = require('../utils/api')

/**
 * Start a new review session
 */
function startReviewSession() {
  return api.post('/reviews/sessions')
    .then(res => res.data)
    .catch(err => {
      if (err.statusCode === 204) {
        return null // No content - no highlights available
      }
      console.error('Failed to start review session:', err)
      throw new Error('Failed to start review session')
    })
}

/**
 * Start a review session from a specific deck
 */
function startDeckReviewSession(deckId) {
  return api.post(`/reviews/sessions/deck/${deckId}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to start deck review session ${deckId}:`, err)
      throw new Error('Failed to start deck review session')
    })
}

/**
 * Start a custom review session with selected cards
 */
function startCustomReviewSession(cardIds) {
  return api.post('/reviews/sessions/custom', cardIds)
    .then(res => res.data)
    .catch(err => {
      console.error('Failed to start custom review session:', err)
      throw new Error('Failed to start custom review session')
    })
}

/**
 * Get all questions in a review session
 */
function getSessionQuestions(sessionId) {
  return api.get(`/reviews/sessions/${sessionId}/questions`)
    .then(res => res.data || [])
    .catch(err => {
      console.error(`Failed to get questions for session ${sessionId}:`, err)
      throw new Error('Failed to get session questions')
    })
}

/**
 * Get the next question in a review session
 */
function getNextQuestion(sessionId) {
  return api.get(`/reviews/sessions/${sessionId}/next-question`)
    .then(res => res.data)
    .catch(err => {
      if (err.statusCode === 204) {
        return null // No more questions
      }
      console.error(`Failed to get next question for session ${sessionId}:`, err)
      throw new Error('Failed to get next question')
    })
}

/**
 * Submit an answer for a question in a review session
 */
function submitAnswer(sessionId, answerData) {
  return api.post(`/reviews/sessions/${sessionId}/answers`, answerData)
    .catch(err => {
      console.error(`Failed to submit answer for session ${sessionId}:`, err)
      throw new Error('Failed to submit answer')
    })
}

/**
 * Complete a review session
 */
function completeSession(sessionId) {
  return api.post(`/reviews/sessions/${sessionId}/complete`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to complete session ${sessionId}:`, err)
      throw new Error('Failed to complete session')
    })
}

/**
 * Get a specific review session by ID
 */
function getReviewSession(sessionId) {
  return api.get(`/reviews/sessions/${sessionId}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to fetch review session ${sessionId}:`, err)
      throw new Error('Failed to fetch review session')
    })
}

/**
 * Get all review sessions with optional filtering
 */
function getReviewSessions(completed = null) {
  const params = {}
  if (completed !== null) {
    params.completed = completed
  }
  
  return api.get('/reviews/sessions', params)
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch review sessions:', err)
      throw new Error('Failed to fetch review sessions')
    })
}

/**
 * Get completed review sessions
 */
function getCompletedSessions() {
  return getReviewSessions(true)
}

/**
 * Get incomplete review sessions
 */
function getIncompleteSessions() {
  return getReviewSessions(false)
}

/**
 * Delete a review session by ID
 */
function deleteReviewSession(sessionId) {
  return api.delete(`/reviews/sessions/${sessionId}`)
    .catch(err => {
      console.error(`Failed to delete review session ${sessionId}:`, err)
      throw new Error('Failed to delete review session')
    })
}

module.exports = {
  startReviewSession,
  startDeckReviewSession,
  startCustomReviewSession,
  getSessionQuestions,
  getNextQuestion,
  submitAnswer,
  completeSession,
  getReviewSession,
  getReviewSessions,
  getCompletedSessions,
  getIncompleteSessions,
  deleteReviewSession
}
