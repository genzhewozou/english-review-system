import { ref } from 'vue'
import { useNotification } from './useNotification'

/**
 * Composable for centralized error handling and user feedback
 */
export function useErrorHandler() {
  const isLoading = ref(false)
  const error = ref(null)
  const { showError, showWarning, showSuccess, showInfo } = useNotification()

  /**
   * Handle API errors with user-friendly messages
   * @param {Error} errorObj - The error object
   * @param {string} context - Context where the error occurred
   * @param {boolean} showNotification - Whether to show notification (default: true)
   */
  const handleError = (errorObj, context = 'Operation', showNotification = true) => {
    console.error(`${context} error:`, errorObj)
    
    let message = 'An unexpected error occurred'
    let type = 'error'
    
    if (errorObj.response) {
      // HTTP error response
      const status = errorObj.response.status
      const data = errorObj.response.data
      
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
    } else if (errorObj.code === 'ECONNABORTED') {
      message = 'Request timeout. Please check your connection and try again.'
      type = 'warning'
    } else if (errorObj.message) {
      message = errorObj.message
    }
    
    error.value = { message, type, context }
    
    if (showNotification) {
      if (type === 'error') {
        showError(message)
      } else {
        showWarning(message)
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
      showSuccess(message)
    }
  }

  /**
   * Show loading notification for long operations
   * @param {string} message - Loading message
   * @returns {Function} Close function
   */
  const showLoadingNotification = (message = 'Processing...') => {
    // Create a loading notification element
    const loadingElement = document.createElement('div')
    loadingElement.className = 'loading-notification'
    loadingElement.innerHTML = `
      <div class="loading-spinner small"></div>
      <div class="loading-message">${message}</div>
    `
    
    // Add to DOM
    document.body.appendChild(loadingElement)
    
    // Trigger animation
    setTimeout(() => {
      loadingElement.classList.add('loading-visible')
    }, 10)
    
    // Close function
    const close = () => {
      loadingElement.classList.remove('loading-visible')
      setTimeout(() => {
        if (document.body.contains(loadingElement)) {
          document.body.removeChild(loadingElement)
        }
      }, 300)
    }
    
    return close
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
    } catch (errorObj) {
      handleError(errorObj, errorContext, showError)
      throw errorObj
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
      // Show first error as toast, then console.log the rest
      showError(errors[0])
      if (errors.length > 1) {
        console.error('Additional form validation errors:', errors.slice(1))
      }
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
      } catch (errorObj) {
        lastError = errorObj
        
        if (attempt === maxRetries) {
          break
        }
        
        // Don't retry on client errors (4xx)
        if (errorObj.response && errorObj.response.status >= 400 && errorObj.response.status < 500) {
          break
        }
        
        const delay = baseDelay * Math.pow(2, attempt)
        await new Promise(resolve => setTimeout(resolve, delay))
      }
    }
    
    throw lastError
  }

  /**
   * Handle form field validation errors
   * @param {Object} formData - Form data
   * @param {string} field - Field name
   * @param {string} errorMessage - Error message
   */
  const handleFieldError = (formData, field, errorMessage) => {
    const fieldError = {
      field,
      message: errorMessage,
      timestamp: Date.now()
    }
    
    error.value = {
      ...error.value,
      fieldErrors: {
        ...error.value?.fieldErrors,
        [field]: fieldError
      }
    }
    
    showError(errorMessage)
  }

  /**
   * Clear field error
   * @param {string} field - Field name
   */
  const clearFieldError = (field) => {
    if (error.value?.fieldErrors) {
      const fieldErrors = { ...error.value.fieldErrors }
      delete fieldErrors[field]
      
      error.value = {
        ...error.value,
        fieldErrors: Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined
      }
    }
  }

  /**
   * Get field error
   * @param {string} field - Field name
   * @returns {Object|null} Field error or null
   */
  const getFieldError = (field) => {
    return error.value?.fieldErrors?.[field] || null
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
    withRetry,
    handleFieldError,
    clearFieldError,
    getFieldError
  }
}

// Add styles for loading notification
if (!document.getElementById('error-handler-styles')) {
  const style = document.createElement('style')
  style.id = 'error-handler-styles'
  style.textContent = `
    /* Loading notification */
    .loading-notification {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%) scale(0.9);
      background-color: var(--surface-primary);
      border: 1px solid var(--surface-border);
      border-radius: var(--radius-xl);
      box-shadow: var(--shadow-lg);
      padding: var(--space-4);
      display: flex;
      align-items: center;
      gap: var(--space-3);
      z-index: var(--z-modal);
      opacity: 0;
      transition: all var(--transition-normal) var(--transition-ease-out);
    }

    .loading-notification.loading-visible {
      opacity: 1;
      transform: translate(-50%, -50%) scale(1);
    }

    .loading-notification .loading-spinner {
      border: 2px solid var(--surface-border);
      border-top: 2px solid var(--primary-600);
      border-radius: 50%;
      width: 24px;
      height: 24px;
      animation: spin 1s linear infinite;
    }

    .loading-notification .loading-spinner.small {
      width: 20px;
      height: 20px;
      border-width: 2px;
    }

    .loading-notification .loading-message {
      font-size: var(--text-sm);
      font-weight: var(--font-medium);
      color: var(--text-secondary);
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `
  document.head.appendChild(style)
}
