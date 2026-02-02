import { ref, computed } from 'vue'
import { useApiService } from './useApiService'
import { useNotification } from './useNotification'

/**
 * Composable for handling file uploads
 */
export function useFileUpload() {
  const { apiService } = useApiService()
  const { showError, showSuccess } = useNotification()

  const uploadProgress = ref(0)
  const isUploading = ref(false)
  const uploadedFiles = ref([])

  // Get supported file formats from environment
  const supportedVideoFormats = computed(() => {
    return import.meta.env.VITE_SUPPORTED_VIDEO_FORMATS?.split(',') || ['mp4', 'avi', 'mov', 'webm']
  })

  const supportedDocumentFormats = computed(() => {
    return import.meta.env.VITE_SUPPORTED_DOCUMENT_FORMATS?.split(',') || ['pdf', 'doc', 'docx', 'txt', 'rtf', 'odt', 'ods', 'odp', 'xls', 'xlsx', 'ppt', 'pptx', 'md', 'html', 'htm', 'csv']
  })

  const maxFileSize = computed(() => {
    const sizeStr = import.meta.env.VITE_FILE_UPLOAD_MAX_SIZE || '50MB'
    const size = parseInt(sizeStr)
    const unit = sizeStr.replace(/[0-9]/g, '').toUpperCase()
    
    switch (unit) {
      case 'KB':
        return size * 1024
      case 'MB':
        return size * 1024 * 1024
      case 'GB':
        return size * 1024 * 1024 * 1024
      default:
        return size
    }
  })

  // Validate file type and size
  const validateFile = (file) => {
    const fileExtension = file.name.split('.').pop().toLowerCase()
    const allSupportedFormats = [...supportedVideoFormats.value, ...supportedDocumentFormats.value]
    
    if (!allSupportedFormats.includes(fileExtension)) {
      showError(`Unsupported file format: ${fileExtension}. Supported formats: ${allSupportedFormats.join(', ')}`)
      return false
    }

    if (file.size > maxFileSize.value) {
      showError(`File size exceeds maximum limit of ${import.meta.env.VITE_FILE_UPLOAD_MAX_SIZE}`)
      return false
    }

    return true
  }

  // Determine material type based on file extension
  const getMaterialType = (file) => {
    const fileExtension = file.name.split('.').pop().toLowerCase()
    
    if (supportedVideoFormats.value.includes(fileExtension)) {
      return 'VIDEO'
    } else if (supportedDocumentFormats.value.includes(fileExtension)) {
      return 'DOCUMENT'
    } else {
      return 'ARTICLE' // Default fallback
    }
  }

  // Upload single file
  const uploadFile = async (file, title = null, onProgress = null) => {
    if (!validateFile(file)) {
      return null
    }

    isUploading.value = true
    uploadProgress.value = 0

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('title', title || file.name.split('.')[0])
      formData.append('type', getMaterialType(file))

      const response = await apiService.upload('/api/materials', formData, (progress) => {
        uploadProgress.value = progress
        if (onProgress) {
          onProgress(progress)
        }
      })

      const uploadedFile = response.data
      uploadedFiles.value.push(uploadedFile)
      
      showSuccess(`File "${file.name}" uploaded successfully`)
      return uploadedFile
    } catch (error) {
      console.error('Upload error:', error)
      showError(`Failed to upload file "${file.name}": ${error.response?.data?.message || error.message}`)
      throw error // Re-throw to allow caller to handle
    } finally {
      isUploading.value = false
      uploadProgress.value = 0
    }
  }

  // Upload multiple files
  const uploadFiles = async (files, titles = []) => {
    const results = []
    
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      const title = titles[i] || null
      const result = await uploadFile(file, title)
      results.push(result)
    }

    return results.filter(result => result !== null)
  }

  // Reset upload state
  const resetUpload = () => {
    uploadProgress.value = 0
    isUploading.value = false
    uploadedFiles.value = []
  }

  return {
    uploadProgress,
    isUploading,
    uploadedFiles,
    supportedVideoFormats,
    supportedDocumentFormats,
    maxFileSize,
    validateFile,
    getMaterialType,
    uploadFile,
    uploadFiles,
    resetUpload
  }
}