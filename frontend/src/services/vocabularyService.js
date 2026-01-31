import { useApiService } from '../composables/useApiService'

/**
 * Service for vocabulary highlighting and management operations
 * Handles communication with the backend VocabularyApi
 */
export function useVocabularyService() {
  const { apiService } = useApiService()

  /**
   * Create a new highlight
   * @param {Object} highlightData - Highlight data
   * @param {number} highlightData.materialId - Material ID
   * @param {string} highlightData.text - Highlighted text
   * @param {string} highlightData.context - Surrounding context
   * @param {number} highlightData.startPosition - Start position in document
   * @param {number} highlightData.endPosition - End position in document
   * @param {string} highlightData.userComment - Optional user comment
   * @returns {Promise<Object>} Created highlight data
   */
  const createHighlight = async (highlightData) => {
    try {
      const response = await apiService.post('/vocabulary/highlights', highlightData)
      return response.data
    } catch (error) {
      console.error('Failed to create highlight:', error)
      throw new Error(error.response?.data?.message || 'Failed to create highlight')
    }
  }

  /**
   * Get all highlights for a specific material
   * @param {number} materialId - Material ID
   * @returns {Promise<Array>} List of highlights for the material
   */
  const getHighlightsByMaterial = async (materialId) => {
    try {
      const response = await apiService.get(`/vocabulary/materials/${materialId}/highlights`)
      return response.data || []
    } catch (error) {
      console.error(`Failed to fetch highlights for material ${materialId}:`, error)
      throw new Error(error.response?.data?.message || 'Failed to fetch highlights')
    }
  }

  /**
   * Get a specific highlight by ID
   * @param {number} id - Highlight ID
   * @returns {Promise<Object>} Highlight data
   */
  const getHighlight = async (id) => {
    try {
      const response = await apiService.get(`/vocabulary/highlights/${id}`)
      return response.data
    } catch (error) {
      console.error(`Failed to fetch highlight ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Highlight not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch highlight')
    }
  }

  /**
   * Update a highlight (mainly for comments)
   * @param {number} id - Highlight ID
   * @param {Object} updateData - Update data
   * @param {string} updateData.userComment - Updated comment
   * @param {string} updateData.text - Updated text (optional)
   * @param {string} updateData.context - Updated context (optional)
   * @returns {Promise<Object>} Updated highlight data
   */
  const updateHighlight = async (id, updateData) => {
    try {
      const response = await apiService.put(`/vocabulary/highlights/${id}`, updateData)
      return response.data
    } catch (error) {
      console.error(`Failed to update highlight ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Highlight not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to update highlight')
    }
  }

  /**
   * Delete a highlight by ID
   * @param {number} id - Highlight ID
   * @returns {Promise<void>}
   */
  const deleteHighlight = async (id) => {
    try {
      await apiService.delete(`/vocabulary/highlights/${id}`)
    } catch (error) {
      console.error(`Failed to delete highlight ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Highlight not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to delete highlight')
    }
  }

  /**
   * Get highlights that are due for review
   * @returns {Promise<Array>} List of highlights due for review
   */
  const getHighlightsDueForReview = async () => {
    try {
      const response = await apiService.get('/vocabulary/highlights/due-for-review')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch highlights due for review:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch highlights due for review')
    }
  }

  /**
   * Add comment to a highlight
   * @param {number} id - Highlight ID
   * @param {string} comment - Comment text
   * @returns {Promise<Object>} Updated highlight data
   */
  const addComment = async (id, comment) => {
    return updateHighlight(id, { userComment: comment })
  }

  /**
   * Update comment on a highlight
   * @param {number} id - Highlight ID
   * @param {string} comment - Updated comment text
   * @returns {Promise<Object>} Updated highlight data
   */
  const updateComment = async (id, comment) => {
    return updateHighlight(id, { userComment: comment })
  }

  /**
   * Remove comment from a highlight
   * @param {number} id - Highlight ID
   * @returns {Promise<Object>} Updated highlight data
   */
  const removeComment = async (id) => {
    return updateHighlight(id, { userComment: '' })
  }

  /**
   * Get all highlights (across all materials)
   * @returns {Promise<Array>} List of all highlights
   */
  const getAllHighlights = async () => {
    try {
      const response = await apiService.get('/vocabulary/highlights')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch all highlights:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch highlights')
    }
  }

  /**
   * Search highlights by text
   * @param {string} query - Search query
   * @returns {Promise<Array>} List of matching highlights
   */
  const searchHighlights = async (query) => {
    try {
      const response = await apiService.get('/vocabulary/highlights/search', {
        params: { q: query }
      })
      return response.data || []
    } catch (error) {
      console.error('Failed to search highlights:', error)
      throw new Error(error.response?.data?.message || 'Failed to search highlights')
    }
  }

  return {
    createHighlight,
    getHighlightsByMaterial,
    getHighlight,
    updateHighlight,
    deleteHighlight,
    getHighlightsDueForReview,
    addComment,
    updateComment,
    removeComment,
    getAllHighlights,
    searchHighlights
  }
}