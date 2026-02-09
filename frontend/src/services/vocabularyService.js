import { useApiService } from '../composables/useApiService'

/**
 * Service for vocabulary card management operations
 * Handles communication with the backend CardApi
 */
export function useVocabularyService() {
  const { apiService } = useApiService()

  /**
   * Create a new card from text selection
   * @param {Object} cardData - Card data
   * @param {number} cardData.materialId - Material ID
   * @param {string} cardData.text - Selected text
   * @param {string} cardData.context - Surrounding context
   * @param {number} cardData.startPosition - Start position in document
   * @param {number} cardData.endPosition - End position in document
   * @param {string} cardData.userComment - Optional user comment
   * @returns {Promise<Object>} Created card data
   */
  const createCardFromHighlight = async (cardData) => {
    try {
      const response = await apiService.post('/vocabulary/cards', cardData)
      return response.data
    } catch (error) {
      console.error('Failed to create card:', error)
      throw new Error(error.response?.data?.message || 'Failed to create card')
    }
  }

  /**
   * Get all cards for a specific material
   * @param {number} materialId - Material ID
   * @returns {Promise<Array>} List of cards for the material
   */
  const getCardsByMaterial = async (materialId) => {
    try {
      const response = await apiService.get(`/vocabulary/material/${materialId}`)
      return response.data || []
    } catch (error) {
      console.error(`Failed to fetch cards for material ${materialId}:`, error)
      throw new Error(error.response?.data?.message || 'Failed to fetch cards')
    }
  }

  /**
   * Get a specific card by ID
   * @param {number} id - Card ID
   * @returns {Promise<Object>} Card data
   */
  const getCard = async (id) => {
    try {
      const response = await apiService.get(`/vocabulary/cards/${id}`)
      return response.data
    } catch (error) {
      console.error(`Failed to fetch card ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Card not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch card')
    }
  }

  /**
   * Update a card (mainly for comments)
   * @param {number} id - Card ID
   * @param {Object} updateData - Update data
   * @param {string} updateData.userComment - Updated comment
   * @param {string} updateData.text - Updated text (optional)
   * @param {string} updateData.context - Updated context (optional)
   * @returns {Promise<Object>} Updated card data
   */
  const updateCard = async (id, updateData) => {
    try {
      const response = await apiService.put(`/vocabulary/cards/${id}`, updateData)
      return response.data
    } catch (error) {
      console.error(`Failed to update card ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Card not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to update card')
    }
  }

  /**
   * Delete a card by ID
   * @param {number} id - Card ID
   * @returns {Promise<void>}
   */
  const deleteCard = async (id) => {
    try {
      await apiService.delete(`/vocabulary/cards/${id}`)
    } catch (error) {
      console.error(`Failed to delete card ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Card not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to delete card')
    }
  }

  /**
   * Get cards that are due for review
   * @returns {Promise<Array>} List of cards due for review
   */
  const getCardsDueForReview = async () => {
    try {
      const response = await apiService.get('/vocabulary/cards/due-for-review')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch cards due for review:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch cards due for review')
    }
  }

  /**
   * Add comment to a card
   * @param {number} id - Card ID
   * @param {string} comment - Comment text
   * @returns {Promise<Object>} Updated card data
   */
  const addComment = async (id, comment) => {
    return updateCard(id, { userComment: comment })
  }

  /**
   * Update comment on a card
   * @param {number} id - Card ID
   * @param {string} comment - Updated comment text
   * @returns {Promise<Object>} Updated card data
   */
  const updateComment = async (id, comment) => {
    return updateCard(id, { userComment: comment })
  }

  /**
   * Remove comment from a card
   * @param {number} id - Card ID
   * @returns {Promise<Object>} Updated card data
   */
  const removeComment = async (id) => {
    return updateCard(id, { userComment: '' })
  }

  /**
   * Get all cards (across all materials)
   * @returns {Promise<Array>} List of all cards
   */
  const getAllCards = async () => {
    try {
      const response = await apiService.get('/vocabulary/cards')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch all cards:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch cards')
    }
  }

  /**
   * Search cards by text
   * @param {string} query - Search query
   * @returns {Promise<Array>} List of matching cards
   */
  const searchCards = async (query) => {
    try {
      const response = await apiService.get('/vocabulary/cards/search', {
        params: { q: query }
      })
      return response.data || []
    } catch (error) {
      console.error('Failed to search cards:', error)
      throw new Error(error.response?.data?.message || 'Failed to search cards')
    }
  }

  return {
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
}