import { useApiService } from '../composables/useApiService'

/**
 * Service for todo list and task management operations
 * Handles communication with the backend TodoApi
 */
export function useTodoService() {
  const { apiService } = useApiService()

  /**
   * Get all todo items with optional filtering
   * @param {boolean} completed - Filter by completion status (optional)
   * @param {boolean} overdue - Filter by overdue status (optional)
   * @returns {Promise<Array>} List of todo items
   */
  const getTodoItems = async (completed = null, overdue = null) => {
    try {
      const params = {}
      if (completed !== null) {
        params.completed = completed
      }
      if (overdue !== null) {
        params.overdue = overdue
      }
      
      const response = await apiService.get('/todos', { params })
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch todo items:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch todo items')
    }
  }

  /**
   * Create a new todo item
   * @param {Object} todoData - Todo item data
   * @param {string} todoData.title - Todo title
   * @param {string} todoData.description - Todo description (optional)
   * @param {string} todoData.dueDate - Due date in YYYY-MM-DD format (optional)
   * @param {string} todoData.type - Todo type (REVIEW_SESSION, CUSTOM_TASK)
   * @param {number} todoData.relatedHighlightId - Related highlight ID (optional)
   * @returns {Promise<Object>} Created todo item data
   */
  const createTodoItem = async (todoData) => {
    try {
      const response = await apiService.post('/todos', todoData)
      return response.data
    } catch (error) {
      console.error('Failed to create todo item:', error)
      throw new Error(error.response?.data?.message || 'Failed to create todo item')
    }
  }

  /**
   * Get a specific todo item by ID
   * @param {number} id - Todo item ID
   * @returns {Promise<Object>} Todo item data
   */
  const getTodoItem = async (id) => {
    try {
      const response = await apiService.get(`/todos/${id}`)
      return response.data
    } catch (error) {
      console.error(`Failed to fetch todo item ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Todo item not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to fetch todo item')
    }
  }

  /**
   * Update a todo item
   * @param {number} id - Todo item ID
   * @param {Object} updateData - Update data
   * @param {string} updateData.title - Updated title (optional)
   * @param {string} updateData.description - Updated description (optional)
   * @param {string} updateData.dueDate - Updated due date (optional)
   * @returns {Promise<Object>} Updated todo item data
   */
  const updateTodoItem = async (id, updateData) => {
    try {
      const response = await apiService.put(`/todos/${id}`, updateData)
      return response.data
    } catch (error) {
      console.error(`Failed to update todo item ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Todo item not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to update todo item')
    }
  }

  /**
   * Mark a todo item as completed
   * @param {number} id - Todo item ID
   * @returns {Promise<Object>} Updated todo item data
   */
  const completeTodoItem = async (id) => {
    try {
      const response = await apiService.post(`/todos/${id}/complete`)
      return response.data
    } catch (error) {
      console.error(`Failed to complete todo item ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Todo item not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to complete todo item')
    }
  }

  /**
   * Delete a todo item by ID
   * @param {number} id - Todo item ID
   * @returns {Promise<void>}
   */
  const deleteTodoItem = async (id) => {
    try {
      await apiService.delete(`/api/todos/${id}`)
    } catch (error) {
      console.error(`Failed to delete todo item ${id}:`, error)
      if (error.response?.status === 404) {
        throw new Error('Todo item not found')
      }
      throw new Error(error.response?.data?.message || 'Failed to delete todo item')
    }
  }

  /**
   * Get todo items due today
   * @returns {Promise<Array>} List of todo items due today
   */
  const getTodoItemsDueToday = async () => {
    try {
      const response = await apiService.get('/api/todos/due-today')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch todo items due today:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch todo items due today')
    }
  }

  /**
   * Get overdue todo items
   * @returns {Promise<Array>} List of overdue todo items
   */
  const getOverdueTodoItems = async () => {
    try {
      const response = await apiService.get('/api/todos/overdue')
      return response.data || []
    } catch (error) {
      console.error('Failed to fetch overdue todo items:', error)
      throw new Error(error.response?.data?.message || 'Failed to fetch overdue todo items')
    }
  }

  /**
   * Get completed todo items
   * @returns {Promise<Array>} List of completed todo items
   */
  const getCompletedTodoItems = async () => {
    return getTodoItems(true)
  }

  /**
   * Get incomplete todo items
   * @returns {Promise<Array>} List of incomplete todo items
   */
  const getIncompleteTodoItems = async () => {
    return getTodoItems(false)
  }

  /**
   * Create a review session todo item
   * @param {number} highlightId - Related highlight ID
   * @param {string} dueDate - Due date in YYYY-MM-DD format
   * @returns {Promise<Object>} Created todo item data
   */
  const createReviewTodo = async (highlightId, dueDate) => {
    const todoData = {
      title: 'Review Vocabulary',
      description: 'Review highlighted vocabulary using spaced repetition',
      dueDate,
      type: 'REVIEW_SESSION',
      relatedHighlightId: highlightId
    }
    return createTodoItem(todoData)
  }

  /**
   * Create a custom task todo item
   * @param {string} title - Task title
   * @param {string} description - Task description (optional)
   * @param {string} dueDate - Due date in YYYY-MM-DD format (optional)
   * @returns {Promise<Object>} Created todo item data
   */
  const createCustomTodo = async (title, description = '', dueDate = null) => {
    const todoData = {
      title,
      description,
      dueDate,
      type: 'CUSTOM_TASK'
    }
    return createTodoItem(todoData)
  }

  /**
   * Get todo statistics
   * @returns {Promise<Object>} Todo statistics
   */
  const getTodoStatistics = async () => {
    try {
      const allTodos = await getTodoItems()
      const completedTodos = allTodos.filter(t => t.completed)
      const incompleteTodos = allTodos.filter(t => !t.completed)
      
      const today = new Date().toISOString().split('T')[0]
      const dueTodayTodos = incompleteTodos.filter(t => t.dueDate === today)
      const overdueTodos = incompleteTodos.filter(t => t.dueDate && t.dueDate < today)
      
      const reviewTodos = allTodos.filter(t => t.type === 'REVIEW_SESSION')
      const customTodos = allTodos.filter(t => t.type === 'CUSTOM_TASK')
      
      return {
        totalTodos: allTodos.length,
        completedTodos: completedTodos.length,
        incompleteTodos: incompleteTodos.length,
        dueTodayTodos: dueTodayTodos.length,
        overdueTodos: overdueTodos.length,
        reviewTodos: reviewTodos.length,
        customTodos: customTodos.length,
        completionRate: allTodos.length > 0 ? Math.round((completedTodos.length / allTodos.length) * 100) : 0
      }
    } catch (error) {
      console.error('Failed to get todo statistics:', error)
      throw new Error('Failed to get todo statistics')
    }
  }

  /**
   * Bulk complete multiple todo items
   * @param {Array<number>} ids - Array of todo item IDs
   * @returns {Promise<Array>} Array of updated todo items
   */
  const bulkCompleteTodos = async (ids) => {
    try {
      const results = await Promise.allSettled(
        ids.map(id => completeTodoItem(id))
      )
      
      const successful = results
        .filter(result => result.status === 'fulfilled')
        .map(result => result.value)
      
      const failed = results
        .filter(result => result.status === 'rejected')
        .map((result, index) => ({ id: ids[index], error: result.reason }))
      
      if (failed.length > 0) {
        console.warn('Some todos failed to complete:', failed)
      }
      
      return successful
    } catch (error) {
      console.error('Failed to bulk complete todos:', error)
      throw new Error('Failed to complete todos')
    }
  }

  /**
   * Bulk delete multiple todo items
   * @param {Array<number>} ids - Array of todo item IDs
   * @returns {Promise<Array>} Array of successfully deleted IDs
   */
  const bulkDeleteTodos = async (ids) => {
    try {
      const results = await Promise.allSettled(
        ids.map(id => deleteTodoItem(id).then(() => id))
      )
      
      const successful = results
        .filter(result => result.status === 'fulfilled')
        .map(result => result.value)
      
      const failed = results
        .filter(result => result.status === 'rejected')
        .map((result, index) => ({ id: ids[index], error: result.reason }))
      
      if (failed.length > 0) {
        console.warn('Some todos failed to delete:', failed)
      }
      
      return successful
    } catch (error) {
      console.error('Failed to bulk delete todos:', error)
      throw new Error('Failed to delete todos')
    }
  }

  return {
    getTodoItems,
    createTodoItem,
    getTodoItem,
    updateTodoItem,
    completeTodoItem,
    deleteTodoItem,
    getTodoItemsDueToday,
    getOverdueTodoItems,
    getCompletedTodoItems,
    getIncompleteTodoItems,
    createReviewTodo,
    createCustomTodo,
    getTodoStatistics,
    bulkCompleteTodos,
    bulkDeleteTodos
  }
}