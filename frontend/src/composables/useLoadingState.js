import { ref, computed } from 'vue'

/**
 * Composable for managing loading states across the application
 */
export function useLoadingState() {
  const loadingStates = ref(new Map())

  /**
   * Set loading state for a specific operation
   * @param {string} key - Unique key for the operation
   * @param {boolean} isLoading - Loading state
   */
  const setLoading = (key, isLoading) => {
    if (isLoading) {
      loadingStates.value.set(key, true)
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
   * @returns {Promise} Operation result
   */
  const withLoading = async (key, operation) => {
    try {
      setLoading(key, true)
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
      withLoading: (key, operation) => withLoading(scopedKey(key), operation),
      clearScope: () => {
        const keysToDelete = Array.from(loadingStates.value.keys())
          .filter(key => key.startsWith(`${prefix}:`))
        keysToDelete.forEach(key => loadingStates.value.delete(key))
      }
    }
  }

  return {
    setLoading,
    isLoading,
    isAnyLoading,
    getLoadingOperations,
    clearAllLoading,
    withLoading,
    createScopedLoader
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