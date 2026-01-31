import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useErrorHandler } from './useErrorHandler'
import { useLoadingState } from './useLoadingState'
import { validateDataFlow } from '../utils/dataFlowValidator'

/**
 * Composable for managing component integration and data flow
 */
export function useComponentIntegration(componentName) {
  const router = useRouter()
  const { handleError, handleSuccess } = useErrorHandler()
  const { createScopedLoader } = useLoadingState()
  
  const loader = createScopedLoader(componentName)
  const componentState = reactive({
    initialized: false,
    error: null,
    data: {},
    dependencies: new Set()
  })

  /**
   * Register a dependency on another component or service
   * @param {string} dependency - Dependency name
   */
  const addDependency = (dependency) => {
    componentState.dependencies.add(dependency)
  }

  /**
   * Check if all dependencies are satisfied
   * @returns {boolean} Whether all dependencies are ready
   */
  const areDependenciesReady = () => {
    // In a real implementation, this would check actual dependency states
    return true
  }

  /**
   * Initialize component with dependencies
   * @param {Function} initFunction - Initialization function
   * @returns {Promise<void>}
   */
  const initialize = async (initFunction) => {
    if (componentState.initialized) {
      return
    }

    try {
      await loader.withLoading('init', async () => {
        if (!areDependenciesReady()) {
          throw new Error('Dependencies not ready')
        }

        await initFunction()
        componentState.initialized = true
        componentState.error = null
      })
    } catch (error) {
      componentState.error = error
      handleError(error, `${componentName} initialization`)
      throw error
    }
  }

  /**
   * Navigate to another route with data validation
   * @param {string} route - Route path
   * @param {Object} params - Route parameters
   * @param {Object} data - Data to pass (optional)
   */
  const navigateWithData = (route, params = {}, data = null) => {
    try {
      if (data) {
        // Store data in session storage for cross-component communication
        sessionStorage.setItem(`navigation_data_${route}`, JSON.stringify(data))
      }

      if (Object.keys(params).length > 0) {
        router.push({ path: route, params })
      } else {
        router.push(route)
      }
    } catch (error) {
      handleError(error, 'Navigation')
    }
  }

  /**
   * Retrieve navigation data for current route
   * @returns {Object|null} Navigation data
   */
  const getNavigationData = () => {
    try {
      const currentRoute = router.currentRoute.value.path
      const dataKey = `navigation_data_${currentRoute}`
      const data = sessionStorage.getItem(dataKey)
      
      if (data) {
        sessionStorage.removeItem(dataKey) // Clean up after use
        return JSON.parse(data)
      }
      
      return null
    } catch (error) {
      console.warn('Failed to retrieve navigation data:', error)
      return null
    }
  }

  /**
   * Emit event to parent component with data validation
   * @param {Function} emit - Vue emit function
   * @param {string} eventName - Event name
   * @param {any} payload - Event payload
   * @param {Object} schema - Payload validation schema (optional)
   */
  const emitWithValidation = (emit, eventName, payload, schema = null) => {
    try {
      if (schema) {
        const isValid = validateDataFlow(componentName, 'parent', payload, schema)
        if (!isValid) {
          console.warn(`Event payload validation failed for ${eventName}`)
        }
      }

      emit(eventName, payload)
    } catch (error) {
      handleError(error, `Event emission: ${eventName}`)
    }
  }

  /**
   * Handle component communication with error boundaries
   * @param {Function} operation - Communication operation
   * @param {string} context - Operation context
   * @returns {Promise<any>} Operation result
   */
  const withCommunication = async (operation, context) => {
    try {
      return await operation()
    } catch (error) {
      handleError(error, `${componentName} communication: ${context}`)
      throw error
    }
  }

  /**
   * Create a reactive data synchronizer between components
   * @param {string} key - Data key
   * @param {any} initialValue - Initial value
   * @returns {Object} Reactive data object
   */
  const createSyncedData = (key, initialValue = null) => {
    const data = ref(initialValue)
    
    // Watch for changes and sync to session storage
    watch(data, (newValue) => {
      try {
        if (newValue !== null && newValue !== undefined) {
          sessionStorage.setItem(`synced_${componentName}_${key}`, JSON.stringify(newValue))
        } else {
          sessionStorage.removeItem(`synced_${componentName}_${key}`)
        }
      } catch (error) {
        console.warn(`Failed to sync data for ${key}:`, error)
      }
    }, { deep: true })

    // Load initial value from storage
    try {
      const stored = sessionStorage.getItem(`synced_${componentName}_${key}`)
      if (stored) {
        data.value = JSON.parse(stored)
      }
    } catch (error) {
      console.warn(`Failed to load synced data for ${key}:`, error)
    }

    return data
  }

  /**
   * Create a computed property that depends on multiple reactive sources
   * @param {Function} computeFn - Compute function
   * @param {Array} dependencies - Dependency array
   * @returns {ComputedRef} Computed property
   */
  const createDependentComputed = (computeFn, dependencies = []) => {
    return computed(() => {
      try {
        return computeFn()
      } catch (error) {
        handleError(error, `${componentName} computed property`)
        return null
      }
    })
  }

  /**
   * Handle component lifecycle with proper cleanup
   * @param {Function} setupFn - Setup function
   * @param {Function} cleanupFn - Cleanup function (optional)
   */
  const handleLifecycle = (setupFn, cleanupFn = null) => {
    onMounted(async () => {
      try {
        await setupFn()
      } catch (error) {
        handleError(error, `${componentName} setup`)
      }
    })

    if (cleanupFn) {
      onUnmounted(() => {
        try {
          cleanupFn()
          loader.clearScope()
        } catch (error) {
          console.warn(`${componentName} cleanup error:`, error)
        }
      })
    }
  }

  /**
   * Create a form handler with validation and submission
   * @param {Object} initialData - Initial form data
   * @param {Object} validationRules - Validation rules
   * @param {Function} submitFn - Submit function
   * @returns {Object} Form handler object
   */
  const createFormHandler = (initialData, validationRules, submitFn) => {
    const formData = reactive({ ...initialData })
    const errors = ref({})
    const isSubmitting = ref(false)

    const validate = () => {
      const newErrors = {}
      let isValid = true

      Object.keys(validationRules).forEach(field => {
        const rule = validationRules[field]
        const value = formData[field]

        if (rule.required && (!value || (typeof value === 'string' && !value.trim()))) {
          newErrors[field] = `${rule.label || field} is required`
          isValid = false
        } else if (value && rule.validator && !rule.validator(value)) {
          newErrors[field] = rule.message || `${rule.label || field} is invalid`
          isValid = false
        }
      })

      errors.value = newErrors
      return isValid
    }

    const submit = async () => {
      if (!validate()) {
        return false
      }

      try {
        isSubmitting.value = true
        await submitFn(formData)
        handleSuccess('Form submitted successfully')
        return true
      } catch (error) {
        handleError(error, 'Form submission')
        return false
      } finally {
        isSubmitting.value = false
      }
    }

    const reset = () => {
      Object.assign(formData, initialData)
      errors.value = {}
    }

    return {
      formData,
      errors,
      isSubmitting,
      validate,
      submit,
      reset
    }
  }

  /**
   * Create a list manager with CRUD operations
   * @param {Function} fetchFn - Fetch function
   * @param {Function} createFn - Create function (optional)
   * @param {Function} updateFn - Update function (optional)
   * @param {Function} deleteFn - Delete function (optional)
   * @returns {Object} List manager object
   */
  const createListManager = (fetchFn, createFn = null, updateFn = null, deleteFn = null) => {
    const items = ref([])
    const loading = ref(false)
    const error = ref(null)

    const fetch = async () => {
      try {
        loading.value = true
        error.value = null
        items.value = await fetchFn()
      } catch (err) {
        error.value = err
        handleError(err, `${componentName} fetch`)
      } finally {
        loading.value = false
      }
    }

    const create = async (data) => {
      if (!createFn) throw new Error('Create function not provided')
      
      try {
        const newItem = await createFn(data)
        items.value.push(newItem)
        handleSuccess('Item created successfully')
        return newItem
      } catch (error) {
        handleError(error, `${componentName} create`)
        throw error
      }
    }

    const update = async (id, data) => {
      if (!updateFn) throw new Error('Update function not provided')
      
      try {
        const updatedItem = await updateFn(id, data)
        const index = items.value.findIndex(item => item.id === id)
        if (index !== -1) {
          items.value[index] = updatedItem
        }
        handleSuccess('Item updated successfully')
        return updatedItem
      } catch (error) {
        handleError(error, `${componentName} update`)
        throw error
      }
    }

    const remove = async (id) => {
      if (!deleteFn) throw new Error('Delete function not provided')
      
      try {
        await deleteFn(id)
        items.value = items.value.filter(item => item.id !== id)
        handleSuccess('Item deleted successfully')
      } catch (error) {
        handleError(error, `${componentName} delete`)
        throw error
      }
    }

    return {
      items,
      loading,
      error,
      fetch,
      create,
      update,
      remove
    }
  }

  return {
    componentState,
    loader,
    addDependency,
    initialize,
    navigateWithData,
    getNavigationData,
    emitWithValidation,
    withCommunication,
    createSyncedData,
    createDependentComputed,
    handleLifecycle,
    createFormHandler,
    createListManager
  }
}