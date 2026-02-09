import axios from 'axios'
import { ElMessage } from 'element-plus'

// Create axios instance with default configuration
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000, // Increased timeout for file uploads
  headers: {
    'Content-Type': 'application/json'
  }
})

// Log the base URL for debugging
console.log('API Base URL:', import.meta.env.VITE_API_BASE_URL || '/api')

// Request interceptor for adding auth tokens, logging, etc.
apiClient.interceptors.request.use(
  config => {
    // Add auth token if available
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Log request in development
    if (import.meta.env.DEV) {
      console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`)
    }

    return config
  },
  error => {
    console.error('❌ Request interceptor error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor for handling errors, logging, etc.
apiClient.interceptors.response.use(
  response => {
    // Log response in development
    if (import.meta.env.DEV) {
      console.log(`API Response: ${response.status} ${response.config.url}`)
    }

    return response
  },
  error => {
    // Log error in development
    if (import.meta.env.DEV) {
      console.error(`API Error: ${error.response?.status} ${error.config?.url}`, error.response?.data)
    }

    // Handle common error scenarios
    if (error.response?.status === 401) {
      // Unauthorized - redirect to login or clear auth
      localStorage.removeItem('authToken')
      ElMessage.error('Authentication required. Please log in.')
    } else if (error.response?.status === 403) {
      // Forbidden - show access denied message
      ElMessage.error('Access denied. You do not have permission to perform this action.')
    } else if (error.response?.status === 404) {
      // Not found - don't show error message for expected 404s
      console.warn('API endpoint not found:', error.config?.url)
    } else if (error.response?.status >= 500) {
      // Server error - show generic error message
      ElMessage.error('Server error occurred. Please try again later.')
    } else if (error.code === 'ECONNABORTED') {
      // Timeout error
      ElMessage.error('Request timeout. Please check your connection and try again.')
    } else if (!error.response) {
      // Network error - don't show error message
      console.warn('Network error:', error.message)
    }

    return Promise.reject(error)
  }
)

// Composable function for using the API service
export function useApiService() {
  const apiService = {
    // GET request
    get: (url, config = {}) => {
      return apiClient.get(url, config)
    },

    // POST request
    post: (url, data = {}, config = {}) => {
      return apiClient.post(url, data, config)
    },

    // PUT request
    put: (url, data = {}, config = {}) => {
      return apiClient.put(url, data, config)
    },

    // PATCH request
    patch: (url, data = {}, config = {}) => {
      return apiClient.patch(url, data, config)
    },

    // DELETE request
    delete: (url, config = {}) => {
      return apiClient.delete(url, config)
    },

    // Upload file with progress tracking
    upload: (url, formData, onProgress = null) => {
      const config = {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        timeout: 300000 // 5 minutes for file uploads
      }

      if (onProgress) {
        config.onUploadProgress = progressEvent => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percentCompleted)
        }
      }

      return apiClient.post(url, formData, config)
    },

    // Download file
    download: (url, filename, config = {}) => {
      return apiClient
        .get(url, {
          ...config,
          responseType: 'blob'
        })
        .then(response => {
          const blob = new Blob([response.data])
          const downloadUrl = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = downloadUrl
          link.download = filename
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(downloadUrl)
        })
    }
  }

  return {
    apiService,
    apiClient // Expose raw client for advanced usage
  }
}

// Export the raw client for direct usage if needed
export { apiClient }