import { ref, watch } from 'vue'

/**
 * Composable for reactive localStorage management
 */
export function useLocalStorage(key, defaultValue = null) {
  // Read initial value from localStorage
  const read = () => {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue
    } catch (error) {
      console.warn(`Error reading localStorage key "${key}":`, error)
      return defaultValue
    }
  }

  // Write value to localStorage
  const write = (value) => {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.warn(`Error writing localStorage key "${key}":`, error)
    }
  }

  // Remove value from localStorage
  const remove = () => {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.warn(`Error removing localStorage key "${key}":`, error)
    }
  }

  // Create reactive reference
  const storedValue = ref(read())

  // Watch for changes and update localStorage
  watch(
    storedValue,
    (newValue) => {
      if (newValue === null || newValue === undefined) {
        remove()
      } else {
        write(newValue)
      }
    },
    { deep: true }
  )

  return {
    value: storedValue,
    remove
  }
}

/**
 * Composable for managing user preferences
 */
export function useUserPreferences() {
  const { value: preferences, remove } = useLocalStorage('userPreferences', {
    theme: 'light',
    language: 'en',
    notifications: {
      enabled: true,
      sound: true,
      desktop: true,
      email: false
    },
    review: {
      questionsPerSession: 10,
      showContext: true,
      autoAdvance: false
    }
  })

  const updatePreference = (path, value) => {
    const keys = path.split('.')
    let current = preferences.value
    
    for (let i = 0; i < keys.length - 1; i++) {
      if (!current[keys[i]]) {
        current[keys[i]] = {}
      }
      current = current[keys[i]]
    }
    
    current[keys[keys.length - 1]] = value
  }

  const getPreference = (path, defaultValue = null) => {
    const keys = path.split('.')
    let current = preferences.value
    
    for (const key of keys) {
      if (current && typeof current === 'object' && key in current) {
        current = current[key]
      } else {
        return defaultValue
      }
    }
    
    return current
  }

  const resetPreferences = () => {
    remove()
  }

  return {
    preferences,
    updatePreference,
    getPreference,
    resetPreferences
  }
}