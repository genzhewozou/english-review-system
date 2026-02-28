/**
 * Vocabulary Service for WeChat Mini Program
 * Matches frontend/src/services/vocabularyService.js
 */

const api = require('../utils/api')

/**
 * Create a new card from text selection
 */
function createCardFromHighlight(cardData) {
  return api.post('/vocabulary/cards', cardData)
    .then(res => res.data)
    .catch(err => {
      console.error('Failed to create card:', err)
      throw new Error('Failed to create card')
    })
}

/**
 * Get all cards for a specific material
 */
function getCardsByMaterial(materialId) {
  return api.get(`/vocabulary/material/${materialId}`)
    .then(res => res.data || [])
    .catch(err => {
      console.error(`Failed to fetch cards for material ${materialId}:`, err)
      throw new Error('Failed to fetch cards')
    })
}

/**
 * Get a specific card by ID
 */
function getCard(id) {
  return api.get(`/vocabulary/cards/${id}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to fetch card ${id}:`, err)
      throw new Error('Failed to fetch card')
    })
}

/**
 * Update a card
 */
function updateCard(id, updateData) {
  return api.put(`/vocabulary/cards/${id}`, updateData)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to update card ${id}:`, err)
      throw new Error('Failed to update card')
    })
}

/**
 * Delete a card by ID
 */
function deleteCard(id) {
  return api.delete(`/vocabulary/cards/${id}`)
    .catch(err => {
      console.error(`Failed to delete card ${id}:`, err)
      throw new Error('Failed to delete card')
    })
}

/**
 * Get cards that are due for review
 */
function getCardsDueForReview() {
  return api.get('/vocabulary/cards/due-for-review')
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch cards due for review:', err)
      throw new Error('Failed to fetch cards due for review')
    })
}

/**
 * Add comment to a card
 */
function addComment(id, comment) {
  return updateCard(id, { userComment: comment })
}

/**
 * Update comment on a card
 */
function updateComment(id, comment) {
  return updateCard(id, { userComment: comment })
}

/**
 * Remove comment from a card
 */
function removeComment(id) {
  return updateCard(id, { userComment: '' })
}

/**
 * Get all cards (across all materials)
 */
function getAllCards() {
  return api.get('/vocabulary/cards')
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch all cards:', err)
      throw new Error('Failed to fetch cards')
    })
}

/**
 * Search cards by text
 */
function searchCards(query) {
  return api.get('/vocabulary/cards/search', { q: query })
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to search cards:', err)
      throw new Error('Failed to search cards')
    })
}

module.exports = {
  createCardFromHighlight,
  getCardsByMaterial,
  getCard,
  updateCard,
  deleteCard,
  getCardsDueForReview,
  addComment,
  updateComment,
  removeComment,
  getAllCards,
  searchCards
}
