import { useApiService } from '../composables/useApiService'

/**
 * Service for deck management operations
 * Handles communication with the backend DeckApi
 */
export function useDeckService() {
  const { apiService } = useApiService()

  /**
   * Create a new deck
   * @param {Object} deckData - Deck creation data
   * @param {string} deckData.name - Deck name
   * @param {string} deckData.description - Optional deck description
   * @param {boolean} deckData.isPublic - Optional public visibility
   * @returns {Promise<Object>} Created deck data
   */
  const createDeck = async (deckData) => {
    try {
      const response = await apiService.post('/decks', deckData)
      return response.data
    } catch (error) {
      console.error('Failed to create deck:', error)
      throw new Error(error.response?.data?.message || 'Failed to create deck')
    }
  }

  /**
   * Get all decks for the current user
   * @returns {Promise<Array>} List of user's decks
   */
  const getAllDecks = async () => {
    try {
      const response = await apiService.get('/decks')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch decks:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch decks')
    }
  }

  /**
   * Get a specific deck by ID
   * @param {number} id - Deck ID
   * @returns {Promise<Object>} Deck data
   */
  const getDeck = async (id) => {
    try {
      const response = await apiService.get(`/decks/${id}`)
      return response.data
    } catch (error) {
      console.error(`Failed to fetch deck ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch deck')
    }
  }

  /**
   * Update a deck
   * @param {number} id - Deck ID
   * @param {Object} updateData - Update data
   * @param {string} updateData.name - Updated deck name
   * @param {string} updateData.description - Updated deck description
   * @param {boolean} updateData.isPublic - Updated public visibility
   * @returns {Promise<Object>} Updated deck data
   */
  const updateDeck = async (id, updateData) => {
    try {
      const response = await apiService.put(`/decks/${id}`, updateData)
      return response.data
    } catch (error) {
      console.error(`Failed to update deck ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to update deck')
    }
  }

  /**
   * Delete a deck
   * @param {number} id - Deck ID
   * @returns {Promise<void>}
   */
  const deleteDeck = async (id) => {
    try {
      await apiService.delete(`/decks/${id}`)
    } catch (error) {
      console.error(`Failed to delete deck ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to delete deck')
    }
  }

  /**
   * Get all cards in a deck
   * @param {number} id - Deck ID
   * @returns {Promise<Array>} List of cards in the deck
   */
  const getCardsInDeck = async (id) => {
    try {
      const response = await apiService.get(`/decks/${id}/cards`)
      return response.data || []
    } catch (error) {
      console.error(`Failed to fetch cards in deck ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch deck cards')
    }
  }

  /**
   * Add a card to a deck
   * @param {number} deckId - Deck ID
   * @param {number} cardId - Card ID to add
   * @returns {Promise<Object>} Updated deck data
   */
  const addCardToDeck = async (deckId, cardId) => {
    try {
      const response = await apiService.post(`/decks/${deckId}/cards`, { cardId })
      return response.data
    } catch (error) {
      console.error(`Failed to add card to deck ${deckId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to add card to deck')
    }
  }

  /**
   * Remove a card from a deck
   * @param {number} deckId - Deck ID
   * @param {number} cardId - Card (highlight) ID to remove
   * @returns {Promise<Object>} Updated deck data
   */
  const removeCardFromDeck = async (deckId, cardId) => {
    try {
      const response = await apiService.delete(`/decks/${deckId}/cards/${cardId}`)
      return response.data
    } catch (error) {
      console.error(`Failed to remove card from deck ${deckId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck or card not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to remove card from deck')
    }
  }

  /**
   * Get all public decks
   * @returns {Promise<Array>} List of public decks
   */
  const getPublicDecks = async () => {
    try {
      const response = await apiService.get('/decks/public')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch public decks:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch public decks')
    }
  }

  /**
   * Duplicate a deck
   * @param {number} deckId - Deck ID to duplicate
   * @param {string} newName - Name for the duplicated deck
   * @returns {Promise<Object>} Duplicated deck data
   */
  const duplicateDeck = async (deckId, newName) => {
    try {
      const response = await apiService.post(`/decks/${deckId}/duplicate`, { newName })
      return response.data
    } catch (error) {
      console.error(`Failed to duplicate deck ${deckId}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Deck not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to duplicate deck')
    }
  }

  /**
   * Search decks by name or description
   * @param {string} query - Search query
   * @returns {Promise<Array>} List of matching decks
   */
  const searchDecks = async (query) => {
    try {
      const response = await apiService.get('/decks/search', {
        params: { q: query }
      })
      return response.data || []
    } catch (error) {
      console.error('Failed to search decks:', error)
      throw new Error(error.response?.data?.message || 'Failed to search decks')
    }
  }

  return {
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
}
