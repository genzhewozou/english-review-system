import { ref, computed } from 'vue'

/**
 * Composable for managing loading states across the application
 */
export function useLoadingState() {
  const loadingStates = ref(new Map())

  /**
   * Set loading state for a specific operation
   * @param {string} key - Unique key for the operation
   * @param {boolean|Object} isLoading - Loading state or loading options
   */
  const setLoading = (key, isLoading) => {
    if (isLoading) {
      const loadingOptions = typeof isLoading === 'object' ? isLoading : { active: true }
      loadingStates.value.set(key, { ...loadingOptions, active: true })
    } else {
      loadingStates.value.delete(key)
    }
  }

  /**
   * Check if a specific operation is loading
   * @param {string} key - Operation key
   * @returns {boolean} Whether the operation is loading
   */
  const isLoading = (key) => {
    return loadingStates.value.has(key)
  }

  /**
   * Get loading options for a specific operation
   * @param {string} key - Operation key
   * @returns {Object|null} Loading options or null if not loading
   */
  const getLoadingOptions = (key) => {
    return loadingStates.value.get(key) || null
  }

  /**
   * Check if any operation is loading
   * @returns {boolean} Whether any operation is loading
   */
  const isAnyLoading = computed(() => {
    return loadingStates.value.size > 0
  })

  /**
   * Get all currently loading operations
   * @returns {Array<string>} Array of loading operation keys
   */
  const getLoadingOperations = computed(() => {
    return Array.from(loadingStates.value.keys())
  })

  /**
   * Clear all loading states
   */
  const clearAllLoading = () => {
    loadingStates.value.clear()
  }

  /**
   * Wrapper for async operations with automatic loading state management
   * @param {string} key - Operation key
   * @param {Function} operation - Async operation
   * @param {Object} options - Loading options
   * @returns {Promise} Operation result
   */
  const withLoading = async (key, operation, options = {}) => {
    try {
      setLoading(key, { ...options, active: true })
      return await operation()
    } finally {
      setLoading(key, false)
    }
  }

  /**
   * Create a loading state manager for a specific component or feature
   * @param {string} prefix - Prefix for operation keys
   * @returns {Object} Scoped loading state manager
   */
  const createScopedLoader = (prefix) => {
    const scopedKey = (key) => `${prefix}:${key}`

    return {
      setLoading: (key, isLoading) => setLoading(scopedKey(key), isLoading),
      isLoading: (key) => isLoading(scopedKey(key)),
      getLoadingOptions: (key) => getLoadingOptions(scopedKey(key)),
      withLoading: (key, operation, options) => withLoading(scopedKey(key), operation, options),
      clearScope: () => {
        const keysToDelete = Array.from(loadingStates.value.keys())
          .filter(key => key.startsWith(`${prefix}:`))
        keysToDelete.forEach(key => loadingStates.value.delete(key))
      }
    }
  }

  /**
   * Generate loading message based on operation type
   * @param {string} operationType - Type of operation
   * @returns {string} Loading message
   */
  const getLoadingMessage = (operationType) => {
    const messages = {
      loading: 'Loading...',
      saving: 'Saving...',
      updating: 'Updating...',
      deleting: 'Deleting...',
      fetching: 'Fetching data...',
      processing: 'Processing...',
      authenticating: 'Authenticating...',
      uploading: 'Uploading...',
      downloading: 'Downloading...'
    }

    return messages[operationType] || messages.loading
  }

  return {
    setLoading,
    isLoading,
    getLoadingOptions,
    isAnyLoading,
    getLoadingOperations,
    clearAllLoading,
    withLoading,
    createScopedLoader,
    getLoadingMessage
  }
}

// Global loading state instance
const globalLoadingState = useLoadingState()

/**
 * Hook for using global loading state
 * @returns {Object} Global loading state manager
 */
export function useGlobalLoading() {
  return globalLoadingState
}

/**
 * Utility function for creating loading states with common patterns
 */
export const loadingUtils = {
  /**
   * Create a loading state for form submission
   * @param {Function} loader - Loading state manager
   * @param {string} formName - Name of the form
   * @param {Function} submitFn - Submission function
   * @returns {Function} Wrapped submission function
   */
  createFormSubmitHandler: (loader, formName, submitFn) => {
    return async (...args) => {
      return loader.withLoading(`${formName}:submit`, async () => {
        return await submitFn(...args)
      }, {
        message: 'Submitting form...'
      })
    }
  },

  /**
   * Create a loading state for data fetching
   * @param {Function} loader - Loading state manager
   * @param {string} dataName - Name of the data
   * @param {Function} fetchFn - Fetch function
   * @returns {Function} Wrapped fetch function
   */
  createDataFetchHandler: (loader, dataName, fetchFn) => {
    return async (...args) => {
      return loader.withLoading(`${dataName}:fetch`, async () => {
        return await fetchFn(...args)
      }, {
        message: 'Fetching data...'
      })
    }
  }
}
