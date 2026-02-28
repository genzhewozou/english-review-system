/**
 * Deck Service for WeChat Mini Program
 * Matches frontend/src/services/deckService.js
 */

const api = require('../utils/api')

/**
 * Create a new deck
 */
function createDeck(deckData) {
  return api.post('/decks', deckData)
    .then(res => res.data)
    .catch(err => {
      console.error('Failed to create deck:', err)
      throw new Error('Failed to create deck')
    })
}

/**
 * Get all decks for the current user
 */
function getAllDecks() {
  return api.get('/decks')
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch decks:', err)
      throw new Error('Failed to fetch decks')
    })
}

/**
 * Get a specific deck by ID
 */
function getDeck(id) {
  return api.get(`/decks/${id}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to fetch deck ${id}:`, err)
      throw new Error('Failed to fetch deck')
    })
}

/**
 * Update a deck
 */
function updateDeck(id, updateData) {
  return api.put(`/decks/${id}`, updateData)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to update deck ${id}:`, err)
      throw new Error('Failed to update deck')
    })
}

/**
 * Delete a deck
 */
function deleteDeck(id) {
  return api.delete(`/decks/${id}`)
    .catch(err => {
      console.error(`Failed to delete deck ${id}:`, err)
      throw new Error('Failed to delete deck')
    })
}

/**
 * Get all cards in a deck
 */
function getCardsInDeck(id) {
  return api.get(`/decks/${id}/cards`)
    .then(res => res.data || [])
    .catch(err => {
      console.error(`Failed to fetch cards in deck ${id}:`, err)
      throw new Error('Failed to fetch deck cards')
    })
}

/**
 * Add a card to a deck
 */
function addCardToDeck(deckId, cardId) {
  return api.post(`/decks/${deckId}/cards`, { cardId })
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to add card to deck ${deckId}:`, err)
      throw new Error('Failed to add card to deck')
    })
}

/**
 * Remove a card from a deck
 */
function removeCardFromDeck(deckId, cardId) {
  return api.delete(`/decks/${deckId}/cards/${cardId}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to remove card from deck ${deckId}:`, err)
      throw new Error('Failed to remove card from deck')
    })
}

/**
 * Get all public decks
 */
function getPublicDecks() {
  return api.get('/decks/public')
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch public decks:', err)
      throw new Error('Failed to fetch public decks')
    })
}

/**
 * Duplicate a deck
 */
function duplicateDeck(deckId, newName) {
  return api.post(`/decks/${deckId}/duplicate`, { newName })
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to duplicate deck ${deckId}:`, err)
      throw new Error('Failed to duplicate deck')
    })
}

/**
 * Search decks by name or description
 */
function searchDecks(query) {
  return api.get('/decks/search', { q: query })
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to search decks:', err)
      throw new Error('Failed to search decks')
    })
}

module.exports = {
  createDeck,
  getAllDecks,
  getDeck,
  updateDeck,
  deleteDeck,
  getCardsInDeck,
  addCardToDeck,
  removeCardFromDeck,
  getPublicDecks,
  duplicateDeck,
  searchDecks
}
