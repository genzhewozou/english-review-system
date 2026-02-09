/**
 * Data flow validation utilities for ensuring proper communication between components
 */

/**
 * Validate API response structure
 * @param {any} data - Response data
 * @param {Object} schema - Expected schema
 * @returns {Object} Validation result
 */
export function validateApiResponse(data, schema) {
  const errors = []
  
  const validateField = (value, fieldSchema, path = '') => {
    if (fieldSchema.required && (value === undefined || value === null)) {
      errors.push(`Required field missing: ${path}`)
      return
    }
    
    if (value === undefined || value === null) {
      return // Optional field not provided
    }
    
    if (fieldSchema.type && typeof value !== fieldSchema.type) {
      errors.push(`Type mismatch at ${path}: expected ${fieldSchema.type}, got ${typeof value}`)
      return
    }
    
    if (fieldSchema.type === 'array' && Array.isArray(value)) {
      if (fieldSchema.items) {
        value.forEach((item, index) => {
          validateField(item, fieldSchema.items, `${path}[${index}]`)
        })
      }
    }
    
    if (fieldSchema.type === 'object' && fieldSchema.properties) {
      Object.keys(fieldSchema.properties).forEach(key => {
        const fieldPath = path ? `${path}.${key}` : key
        validateField(value[key], fieldSchema.properties[key], fieldPath)
      })
    }
    
    if (fieldSchema.enum && !fieldSchema.enum.includes(value)) {
      errors.push(`Invalid enum value at ${path}: ${value}. Expected one of: ${fieldSchema.enum.join(', ')}`)
    }
    
    if (fieldSchema.minLength && typeof value === 'string' && value.length < fieldSchema.minLength) {
      errors.push(`String too short at ${path}: minimum length ${fieldSchema.minLength}`)
    }
    
    if (fieldSchema.maxLength && typeof value === 'string' && value.length > fieldSchema.maxLength) {
      errors.push(`String too long at ${path}: maximum length ${fieldSchema.maxLength}`)
    }
  }
  
  if (schema.type === 'array' && Array.isArray(data)) {
    if (schema.items) {
      data.forEach((item, index) => {
        validateField(item, schema.items, `[${index}]`)
      })
    }
  } else if (schema.properties) {
    Object.keys(schema.properties).forEach(key => {
      validateField(data[key], schema.properties[key], key)
    })
  } else {
    validateField(data, schema, 'root')
  }
  
  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * Common API response schemas
 */
export const schemas = {
  studyMaterial: {
    type: 'object',
    properties: {
      id: { type: 'number', required: true },
      title: { type: 'string', required: true },
      fileName: { type: 'string', required: true },
      filePath: { type: 'string', required: true },
      type: { type: 'string', enum: ['DOCUMENT', 'VIDEO', 'ARTICLE'], required: true },
      mimeType: { type: 'string' },
      fileSize: { type: 'number' },
      cardCount: { type: 'number' },
      createdDate: { type: 'string', required: true },
      updatedDate: { type: 'string', required: true }
    }
  },
  
  studyMaterialList: {
    type: 'array',
    items: {
      type: 'object',
      properties: {
        id: { type: 'number', required: true },
        title: { type: 'string', required: true },
        fileName: { type: 'string', required: true },
        type: { type: 'string', enum: ['DOCUMENT', 'VIDEO', 'ARTICLE'], required: true },
        fileSize: { type: 'number' },
        cardCount: { type: 'number' },
        createdDate: { type: 'string', required: true }
      }
    }
  },
  
  card: {
    type: 'object',
    properties: {
      id: { type: 'number', required: true },
      materialId: { type: 'number', required: true },
      text: { type: 'string', required: true },
      frontText: { type: 'string' },
      backText: { type: 'string' },
      context: { type: 'string' },
      startPosition: { type: 'number' },
      endPosition: { type: 'number' },
      userComment: { type: 'string' },
      easeFactor: { type: 'number', required: true },
      repetitionCount: { type: 'number', required: true },
      intervalDays: { type: 'number', required: true },
      nextReviewDate: { type: 'string' },
      lastReviewDate: { type: 'string' },
      createdDate: { type: 'string', required: true },
      updatedDate: { type: 'string', required: true }
    }
  },
  
  cardList: {
    type: 'array',
    items: {
      type: 'object',
      properties: {
        id: { type: 'number', required: true },
        materialId: { type: 'number', required: true },
        text: { type: 'string', required: true },
        frontText: { type: 'string' },
        backText: { type: 'string' },
        context: { type: 'string' },
        userComment: { type: 'string' },
        nextReviewDate: { type: 'string' },
        createdDate: { type: 'string', required: true }
      }
    }
  },
  
  reviewSession: {
    type: 'object',
    properties: {
      id: { type: 'number', required: true },
      startTime: { type: 'string', required: true },
      endTime: { type: 'string' },
      completed: { type: 'boolean', required: true },
      totalQuestions: { type: 'number', required: true },
      correctAnswers: { type: 'number', required: true },
      accuracyPercentage: { type: 'number' },
      reviewRecords: { type: 'array' }
    }
  },
  
  question: {
    type: 'object',
    properties: {
      cardId: { type: 'number', required: true },
      text: { type: 'string', required: true },
      frontText: { type: 'string' },
      backText: { type: 'string' },
      context: { type: 'string' },
      userComment: { type: 'string' },
      questionNumber: { type: 'number', required: true },
      totalQuestions: { type: 'number', required: true }
    }
  },
  
  todoItem: {
    type: 'object',
    properties: {
      id: { type: 'number', required: true },
      title: { type: 'string', required: true },
      description: { type: 'string' },
      dueDate: { type: 'string' },
      completed: { type: 'boolean', required: true },
      type: { type: 'string', enum: ['REVIEW_SESSION', 'CUSTOM_TASK'], required: true },
      relatedCardId: { type: 'number' },
      relatedCardText: { type: 'string' },
      overdue: { type: 'boolean' },
      dueToday: { type: 'boolean' },
      createdDate: { type: 'string', required: true },
      updatedDate: { type: 'string', required: true }
    }
  },
  
  todoItemList: {
    type: 'array',
    items: {
      type: 'object',
      properties: {
        id: { type: 'number', required: true },
        title: { type: 'string', required: true },
        dueDate: { type: 'string' },
        completed: { type: 'boolean', required: true },
        type: { type: 'string', enum: ['REVIEW_SESSION', 'CUSTOM_TASK'], required: true },
        overdue: { type: 'boolean' },
        dueToday: { type: 'boolean' }
      }
    }
  }
}

/**
 * Validate component props
 * @param {Object} props - Component props
 * @param {Object} propTypes - Expected prop types
 * @returns {Object} Validation result
 */
export function validateComponentProps(props, propTypes) {
  const errors = []
  
  Object.keys(propTypes).forEach(propName => {
    const propType = propTypes[propName]
    const propValue = props[propName]
    
    if (propType.required && (propValue === undefined || propValue === null)) {
      errors.push(`Required prop missing: ${propName}`)
      return
    }
    
    if (propValue !== undefined && propValue !== null) {
      if (propType.type && typeof propValue !== propType.type) {
        errors.push(`Prop type mismatch for ${propName}: expected ${propType.type}, got ${typeof propValue}`)
      }
      
      if (propType.validator && typeof propType.validator === 'function') {
        if (!propType.validator(propValue)) {
          errors.push(`Prop validation failed for ${propName}`)
        }
      }
    }
  })
  
  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * Validate event payload
 * @param {any} payload - Event payload
 * @param {Object} schema - Expected schema
 * @returns {Object} Validation result
 */
export function validateEventPayload(payload, schema) {
  return validateApiResponse(payload, schema)
}

/**
 * Create a validation middleware for API responses
 * @param {Object} schema - Response schema
 * @returns {Function} Validation middleware
 */
export function createResponseValidator(schema) {
  return (data) => {
    const validation = validateApiResponse(data, schema)
    
    if (!validation.valid) {
      console.warn('API Response validation failed:', validation.errors)
      // In development, you might want to throw an error
      if (import.meta.env.DEV) {
        console.error('Response data:', data)
        console.error('Expected schema:', schema)
      }
    }
    
    return data
  }
}

/**
 * Sanitize data before sending to API
 * @param {Object} data - Data to sanitize
 * @returns {Object} Sanitized data
 */
export function sanitizeApiData(data) {
  if (Array.isArray(data)) {
    return data.map(item => sanitizeApiData(item))
  }
  
  if (data && typeof data === 'object') {
    const sanitized = {}
    
    Object.keys(data).forEach(key => {
      const value = data[key]
      
      // Remove undefined values
      if (value !== undefined) {
        // Trim strings
        if (typeof value === 'string') {
          sanitized[key] = value.trim()
        } else if (value && typeof value === 'object') {
          sanitized[key] = sanitizeApiData(value)
        } else {
          sanitized[key] = value
        }
      }
    })
    
    return sanitized
  }
  
  return data
}

/**
 * Deep clone object to prevent mutation
 * @param {any} obj - Object to clone
 * @returns {any} Cloned object
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }
  
  if (obj instanceof Date) {
    return new Date(obj.getTime())
  }
  
  if (Array.isArray(obj)) {
    return obj.map(item => deepClone(item))
  }
  
  const cloned = {}
  Object.keys(obj).forEach(key => {
    cloned[key] = deepClone(obj[key])
  })
  
  return cloned
}

/**
 * Validate data flow between components
 * @param {string} source - Source component name
 * @param {string} target - Target component name
 * @param {any} data - Data being passed
 * @param {Object} schema - Expected data schema
 */
export function validateDataFlow(source, target, data, schema) {
  const validation = validateApiResponse(data, schema)
  
  if (!validation.valid) {
    console.warn(`Data flow validation failed from ${source} to ${target}:`, validation.errors)
    
    if (import.meta.env.DEV) {
      console.error('Invalid data:', data)
      console.error('Expected schema:', schema)
    }
  }
  
  return validation.valid
}