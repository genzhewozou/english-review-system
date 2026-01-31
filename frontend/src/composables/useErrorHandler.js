import { ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

/**
 * Composable for centralized error handling and user feedback
 */
export function useErrorHandler() {
  const isLoading = ref(false)
  const error = ref(null)

  /**
   * Handle API errors with user-friendly messages
   * @param {Error} error - The error object
   * @param {string} context - Context where the error occurred
   * @param {boolean} showNotification - Whether to show notification (default: true)
   */
  const handleError = (error, context = 'Operation', showNotification = true) => {
    console.error(`${context} error:`, error)
    
    let message = 'An unexpected error occurred'
    let type = 'error'
    
    if (error.response) {
      // HTTP error response
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 400:
          message = data?.message || 'Invalid request. Please check your input.'
          break
        case 401:
          message = 'Authentication required. Please log in.'
          type = 'warning'
          break
        case 403:
          message = 'Access denied. You do not have permission to perform this action.'
          type = 'warning'
          break
        case 404:
          message = data?.message || 'The requested resource was not found.'
          type = 'warning'
          break
        case 409:
          message = data?.message || 'Conflict occurred. The resource may already exist.'
          break
        case 413:
          message = 'File too large. Please choose a smaller file.'
          break
        case 415:
          message = 'Unsupported file type. Please choose a different file.'
          break
        case 422:
          message = data?.message || 'Validation failed. Please check your input.'
          break
        case 429:
          message = 'Too many requests. Please wait a moment and try again.'
          type = 'warning'
          break
        case 500:
          message = 'Server error occurred. Please try again later.'
          break
        case 502:
        case 503:
        case 504:
          message = 'Service temporarily unavailable. Please try again later.'
          break
        default:
          message = data?.message || `${context} failed with status ${status}`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = 'Request timeout. Please check your connection and try again.'
      type = 'warning'
    } else if (error.code === 'NETWORK_ERROR' || !error.response) {
      message = 'Network error. Please check your internet connection.'
      type = 'warning'
    } else if (error.message) {
      message = error.message
    }
    
    error.value = { message, type, context }
    
    if (showNotification) {
      if (type === 'error') {
        ElMessage.error(message)
      } else {
        ElMessage.warning(message)
      }
    }
    
    return { message, type }
  }

  /**
   * Handle success operations with user feedback
   * @param {string} message - Success message
   * @param {boolean} showNotification - Whether to show notification (default: true)
   */
  const handleSuccess = (message, showNotification = true) => {
    error.value = null
    
    if (showNotification) {
      ElMessage.success(message)
    }
  }

  /**
   * Show loading notification for long operations
   * @param {string} message - Loading message
   * @returns {Function} Close function
   */
  const showLoadingNotification = (message = 'Processing...') => {
    const notification = ElNotification({
      title: 'Please wait',
      message,
      type: 'info',
      duration: 0,
      showClose: false
    })
    
    return () => notification.close()
  }

  /**
   * Wrapper for async operations with loading and error handling
   * @param {Function} operation - Async operation to execute
   * @param {Object} options - Options
   * @param {string} options.loadingMessage - Loading message
   * @param {string} options.successMessage - Success message
   * @param {string} options.errorContext - Error context
   * @param {boolean} options.showSuccess - Show success notification
   * @param {boolean} options.showError - Show error notification
   * @returns {Promise} Operation result
   */
  const withErrorHandling = async (operation, options = {}) => {
    const {
      loadingMessage = null,
      successMessage = null,
      errorContext = 'Operation',
      showSuccess = true,
      showError = true
    } = options

    let closeLoading = null
    
    try {
      isLoading.value = true
      
      if (loadingMessage) {
        closeLoading = showLoadingNotification(loadingMessage)
      }
      
      const result = await operation()
      
      if (successMessage) {
        handleSuccess(successMessage, showSuccess)
      }
      
      return result
    } catch (error) {
      handleError(error, errorContext, showError)
      throw error
    } finally {
      isLoading.value = false
      if (closeLoading) {
        closeLoading()
      }
    }
  }

  /**
   * Clear current error state
   */
  const clearError = () => {
    error.value = null
  }

  /**
   * Validate form data and show validation errors
   * @param {Object} formData - Form data to validate
   * @param {Object} rules - Validation rules
   * @returns {boolean} Whether validation passed
   */
  const validateForm = (formData, rules) => {
    const errors = []
    
    Object.keys(rules).forEach(field => {
      const rule = rules[field]
      const value = formData[field]
      
      if (rule.required && (!value || (typeof value === 'string' && !value.trim()))) {
        errors.push(`${rule.label || field} is required`)
      }
      
      if (value && rule.minLength && value.length < rule.minLength) {
        errors.push(`${rule.label || field} must be at least ${rule.minLength} characters`)
      }
      
      if (value && rule.maxLength && value.length > rule.maxLength) {
        errors.push(`${rule.label || field} must not exceed ${rule.maxLength} characters`)
      }
      
      if (value && rule.pattern && !rule.pattern.test(value)) {
        errors.push(rule.message || `${rule.label || field} format is invalid`)
      }
      
      if (rule.custom && typeof rule.custom === 'function') {
        const customError = rule.custom(value, formData)
        if (customError) {
          errors.push(customError)
        }
      }
    })
    
    if (errors.length > 0) {
      errors.forEach(error => ElMessage.error(error))
      return false
    }
    
    return true
  }

  /**
   * Retry an operation with exponential backoff
   * @param {Function} operation - Operation to retry
   * @param {number} maxRetries - Maximum number of retries
   * @param {number} baseDelay - Base delay in milliseconds
   * @returns {Promise} Operation result
   */
  const withRetry = async (operation, maxRetries = 3, baseDelay = 1000) => {
    let lastError
    
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        return await operation()
      } catch (error) {
        lastError = error
        
        if (attempt === maxRetries) {
          break
        }
        
        // Don't retry on client errors (4xx)
        if (error.response && error.response.status >= 400 && error.response.status < 500) {
          break
        }
        
        const delay = baseDelay * Math.pow(2, attempt)
        await new Promise(resolve => setTimeout(resolve, delay))
      }
    }
    
    throw lastError
  }

  return {
    isLoading,
    error,
    handleError,
    handleSuccess,
    showLoadingNotification,
    withErrorHandling,
    clearError,
    validateForm,
    withRetry
  }
}