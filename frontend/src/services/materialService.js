import { useApiService } from '../composables/useApiService'
import { useErrorHandler } from '../composables/useErrorHandler'
import { createResponseValidator, sanitizeApiData, schemas } from '../utils/dataFlowValidator'

/**
 * Service for study material management operations
 * Handles communication with the backend StudyMaterialApi
 */
export function useMaterialService() {
  const { apiService } = useApiService()
  const { withErrorHandling } = useErrorHandler()
  
  // Response validators
  const validateMaterial = createResponseValidator(schemas.studyMaterial)
  const validateMaterialList = createResponseValidator(schemas.studyMaterialList)

  /**
   * Upload a new study material
   * @param {File} file - The file to upload
   * @param {string} title - Material title
   * @param {string} type - Material type (DOCUMENT, VIDEO, ARTICLE)
   * @param {Function} onProgress - Progress callback function
   * @returns {Promise<Object>} The uploaded material data
   */
  const uploadMaterial = async (file, title, type, onProgress = null) => {
    return withErrorHandling(
      async () => {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('title', title || file.name.split('.').slice(0, -1).join('.'))
        formData.append('type', type)

        const response = await apiService.upload('/materials', formData, onProgress)
        return validateMaterial(response.data)
      },
      {
        errorContext: 'Material upload',
        loadingMessage: 'Uploading material...',
        successMessage: 'Material uploaded successfully'
      }
    )
  }

  /**
   * Get all study materials
   * @returns {Promise<Array>} List of all materials
   */
  const getAllMaterials = async () => {
    return withErrorHandling(
      async () => {
        const response = await apiService.get('/materials')
        return validateMaterialList(response.data || [])
      },
      {
        errorContext: 'Fetch materials',
        showSuccess: false
      }
    )
  }

  /**
   * Get a specific material by ID
   * @param {number} id - Material ID
   * @returns {Promise<Object>} Material data
   */
  const getMaterial = async (id) => {
    return withErrorHandling(
      async () => {
        const response = await apiService.get(`/materials/${id}`)
        return validateMaterial(response.data)
      },
      {
        errorContext: 'Fetch material',
        showSuccess: false
      }
    )
  }

  /**
   * Delete a material by ID
   * @param {number} id - Material ID
   * @returns {Promise<void>}
   */
  const deleteMaterial = async (id) => {
    return withErrorHandling(
      async () => {
        await apiService.delete(`/materials/${id}`)
      },
      {
        errorContext: 'Delete material',
        successMessage: 'Material deleted successfully'
      }
    )
  }

  /**
   * Download a material file
   * @param {number} id - Material ID
   * @param {string} filename - Filename for download
   * @returns {Promise<void>}
   */
  const downloadMaterial = async (id, filename) => {
    try {
      await apiService.download(`/materials/${id}/download`, filename)
    } catch (error) {
      console.error(`Failed to download material ${id}:`, error)
      throw new Error(error.response?.data?.message || 'Failed to download material')
    }
  }

  /**
   * Get material content for viewing
   * @param {number} id - Material ID
   * @returns {Promise<string>} Material content URL or data
   */
  const getMaterialContent = async (id) => {
    try {
      const response = await apiService.get(`/materials/${id}/content`)
      return response.data
    } catch (error) {
      console.error(`Failed to fetch material content ${id}:`, error)
      throw new Error(error.response?.data?.message || 'Failed to fetch material content')
    }
  }

  return {
    uploadMaterial,
    getAllMaterials,
    getMaterial,
    deleteMaterial,
    downloadMaterial,
    getMaterialContent
  }
}