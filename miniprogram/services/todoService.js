/**
 * Todo Service for WeChat Mini Program
 */

const api = require('../utils/api')

/**
 * Create a new todo item
 */
function createTodo(todoData) {
  return api.post('/todos', todoData)
    .then(res => res.data)
    .catch(err => {
      console.error('Failed to create todo:', err)
      throw new Error('Failed to create todo')
    })
}

/**
 * Get all todos
 */
function getAllTodos() {
  return api.get('/todos')
    .then(res => res.data || [])
    .catch(err => {
      console.error('Failed to fetch todos:', err)
      throw new Error('Failed to fetch todos')
    })
}

/**
 * Get a specific todo by ID
 */
function getTodo(id) {
  return api.get(`/todos/${id}`)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to fetch todo ${id}:`, err)
      throw new Error('Failed to fetch todo')
    })
}

/**
 * Update a todo
 */
function updateTodo(id, updateData) {
  return api.put(`/todos/${id}`, updateData)
    .then(res => res.data)
    .catch(err => {
      console.error(`Failed to update todo ${id}:`, err)
      throw new Error('Failed to update todo')
    })
}

/**
 * Delete a todo
 */
function deleteTodo(id) {
  return api.delete(`/todos/${id}`)
    .catch(err => {
      console.error(`Failed to delete todo ${id}:`, err)
      throw new Error('Failed to delete todo')
    })
}

/**
 * Mark todo as completed
 */
function completeTodo(id) {
  return updateTodo(id, { completed: true })
}

/**
 * Mark todo as incomplete
 */
function incompleteTodo(id) {
  return updateTodo(id, { completed: false })
}

module.exports = {
  createTodo,
  getAllTodos,
  getTodo,
  updateTodo,
  deleteTodo,
  completeTodo,
  incompleteTodo
}
