<template>
  <div class="todo-list">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2>Todo List</h2>
      <button @click="showAddModal = true" class="btn">Add Task</button>
    </div>
    
    <!-- Add Task Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeAddModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Add New Task</h3>
          <button @click="closeAddModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="addTask">
            <div class="form-group">
              <label class="form-label">Title</label>
              <input v-model="addForm.title" type="text" class="form-control" required>
            </div>
            <div class="form-group">
              <label class="form-label">Description</label>
              <textarea v-model="addForm.description" class="form-control" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Due Date</label>
              <input v-model="addForm.dueDate" type="date" class="form-control">
            </div>
            <div class="form-group">
              <button type="submit" class="btn" :disabled="adding">
                {{ adding ? 'Adding...' : 'Add Task' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- Filters -->
    <div class="filters mb-3">
      <div class="d-flex gap-1">
        <select v-model="filterStatus" @change="filterTodos" class="form-control">
          <option value="all">All Tasks</option>
          <option value="pending">Pending</option>
          <option value="completed">Completed</option>
          <option value="overdue">Overdue</option>
        </select>
        <select v-model="filterType" @change="filterTodos" class="form-control">
          <option value="all">All Types</option>
          <option value="REVIEW_SESSION">Review Sessions</option>
          <option value="CUSTOM_TASK">Custom Tasks</option>
        </select>
      </div>
    </div>
    
    <div v-if="loading" class="text-center">
      <div class="spinner"></div>
    </div>
    
    <div v-else-if="filteredTodos.length === 0" class="card text-center">
      <p>No tasks found. Add a task or adjust your filters.</p>
    </div>
    
    <div v-else class="todos-list">
      <div v-for="todo in filteredTodos" :key="todo.id" class="card todo-card" :class="getTodoCardClass(todo)">
        <div class="todo-header">
          <div class="todo-title-section">
            <input 
              type="checkbox" 
              :checked="todo.completed" 
              @change="toggleTodo(todo.id)"
              class="todo-checkbox"
            >
            <h4 :class="{ 'completed': todo.completed }">{{ todo.title }}</h4>
          </div>
          <div class="todo-actions">
            <span class="todo-type">{{ todo.type }}</span>
            <button @click="deleteTodo(todo.id)" class="btn btn-danger btn-sm">Delete</button>
          </div>
        </div>
        
        <div v-if="todo.description" class="todo-description">
          <p>{{ todo.description }}</p>
        </div>
        
        <div class="todo-meta">
          <span v-if="todo.dueDate" class="due-date" :class="getDueDateClass(todo)">
            <strong>Due:</strong> {{ formatDate(todo.dueDate) }}
          </span>
          <span class="created-date">
            <strong>Created:</strong> {{ formatDate(todo.createdDate) }}
          </span>
          <span v-if="todo.relatedHighlight" class="related-highlight">
            <strong>Related to:</strong> {{ todo.relatedHighlight.text }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useApiService } from '../composables/useApiService'
import { useNotificationStore } from '../stores/notificationStore'
import { useNotification } from '../composables/useNotification'

export default {
  name: 'TodoList',
  setup() {
    const todos = ref([])
    const loading = ref(false)
    const adding = ref(false)
    const showAddModal = ref(false)
    const filterStatus = ref('all')
    const filterType = ref('all')
    
    const addForm = ref({
      title: '',
      description: '',
      dueDate: ''
    })
    
    const { apiService } = useApiService()
    const notificationStore = useNotificationStore()
    const { showSuccess, showError, notifyWarning } = useNotification()
    
    const filteredTodos = computed(() => {
      let filtered = todos.value
      
      // Filter by status
      if (filterStatus.value === 'pending') {
        filtered = filtered.filter(t => !t.completed)
      } else if (filterStatus.value === 'completed') {
        filtered = filtered.filter(t => t.completed)
      } else if (filterStatus.value === 'overdue') {
        const today = new Date().toISOString().split('T')[0]
        filtered = filtered.filter(t => !t.completed && t.dueDate && t.dueDate < today)
      }
      
      // Filter by type
      if (filterType.value !== 'all') {
        filtered = filtered.filter(t => t.type === filterType.value)
      }
      
      // Sort by due date, then by creation date
      return filtered.sort((a, b) => {
        if (a.completed !== b.completed) {
          return a.completed ? 1 : -1
        }
        
        if (a.dueDate && b.dueDate) {
          return new Date(a.dueDate) - new Date(b.dueDate)
        }
        
        if (a.dueDate && !b.dueDate) return -1
        if (!a.dueDate && b.dueDate) return 1
        
        return new Date(b.createdDate) - new Date(a.createdDate)
      })
    })
    
    const loadTodos = async () => {
      loading.value = true
      try {
        const response = await apiService.get('/todo')
        todos.value = response.data || []
        
        // Check for overdue items and show notifications if needed
        checkForOverdueItems()
      } catch (error) {
        console.error('Error loading todos:', error)
        showError('Failed to load todo items')
        todos.value = []
      } finally {
        loading.value = false
      }
    }
    
    const checkForOverdueItems = () => {
      const today = new Date().toISOString().split('T')[0]
      const overdueItems = todos.value.filter(t => 
        !t.completed && t.dueDate && t.dueDate < today
      )
      
      if (overdueItems.length > 0) {
        // Create overdue notification
        notificationStore.createOverdueNotification(overdueItems)
        
        // Show warning notification
        const message = overdueItems.length === 1 
          ? `You have 1 overdue task: ${overdueItems[0].title}`
          : `You have ${overdueItems.length} overdue tasks`
        
        notifyWarning(message, 'Overdue Tasks')
      }
      
      // Check for items due today
      const dueTodayItems = todos.value.filter(t => 
        !t.completed && t.dueDate && t.dueDate === today
      )
      
      if (dueTodayItems.length > 0) {
        // Create due today notification
        notificationStore.createDueTodayNotification(dueTodayItems)
      }
    }
    
    const addTask = async () => {
      adding.value = true
      try {
        const taskData = {
          title: addForm.value.title,
          description: addForm.value.description,
          dueDate: addForm.value.dueDate || null,
          type: 'CUSTOM_TASK'
        }
        
        const response = await apiService.post('/todo', taskData)
        todos.value.unshift(response.data)
        
        // Reset form and close modal
        addForm.value = { title: '', description: '', dueDate: '' }
        showAddModal.value = false
        showSuccess('Task added successfully')
      } catch (error) {
        console.error('Error adding task:', error)
        showError('Failed to add task. Please try again.')
      } finally {
        adding.value = false
      }
    }
    
    const toggleTodo = async (id) => {
      try {
        await apiService.put(`/todo/${id}/complete`)
        
        // Update local state
        const todo = todos.value.find(t => t.id === id)
        if (todo) {
          todo.completed = !todo.completed
          
          if (todo.completed) {
            showSuccess('Task completed!')
          }
        }
      } catch (error) {
        console.error('Error toggling todo:', error)
        showError('Failed to update task. Please try again.')
      }
    }
    
    const deleteTodo = async (id) => {
      if (!confirm('Are you sure you want to delete this task?')) return
      
      try {
        await apiService.delete(`/todo/${id}`)
        todos.value = todos.value.filter(t => t.id !== id)
        showSuccess('Task deleted successfully')
      } catch (error) {
        console.error('Error deleting todo:', error)
        showError('Failed to delete task. Please try again.')
      }
    }
    
    const closeAddModal = () => {
      showAddModal.value = false
      addForm.value = { title: '', description: '', dueDate: '' }
    }
    
    const getTodoCardClass = (todo) => {
      if (todo.completed) return 'completed-todo'
      
      if (todo.dueDate) {
        const today = new Date().toISOString().split('T')[0]
        if (todo.dueDate < today) return 'overdue-todo'
        if (todo.dueDate === today) return 'due-today-todo'
      }
      
      return ''
    }
    
    const getDueDateClass = (todo) => {
      if (todo.completed) return ''
      
      const today = new Date().toISOString().split('T')[0]
      if (todo.dueDate < today) return 'overdue'
      if (todo.dueDate === today) return 'due-today'
      
      return ''
    }
    
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString()
    }
    
    const filterTodos = () => {
      // Reactive computed property handles this automatically
    }
    
    onMounted(() => {
      loadTodos()
    })
    
    return {
      todos,
      loading,
      adding,
      showAddModal,
      filterStatus,
      filterType,
      addForm,
      filteredTodos,
      addTask,
      toggleTodo,
      deleteTodo,
      closeAddModal,
      getTodoCardClass,
      getDueDateClass,
      formatDate,
      filterTodos
    }
  }
}
</script>

<style scoped>
.filters {
  margin-bottom: 1.5rem;
}

.gap-1 {
  gap: 1rem;
}

.todos-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.todo-card {
  transition: transform 0.2s;
}

.todo-card:hover {
  transform: translateY(-1px);
}

.completed-todo {
  opacity: 0.7;
  background-color: #f8f9fa;
}

.overdue-todo {
  border-left: 4px solid #dc3545;
}

.due-today-todo {
  border-left: 4px solid #ffc107;
}

.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.todo-title-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.todo-checkbox {
  width: 1.2rem;
  height: 1.2rem;
}

.todo-title-section h4 {
  margin: 0;
  color: #2c3e50;
}

.todo-title-section h4.completed {
  text-decoration: line-through;
  color: #6c757d;
}

.todo-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.todo-type {
  background-color: #007bff;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.todo-description p {
  margin: 0;
  color: #6c757d;
  margin-bottom: 1rem;
}

.todo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.due-date.overdue {
  color: #dc3545;
  font-weight: 600;
}

.due-date.due-today {
  color: #ffc107;
  font-weight: 600;
}

.related-highlight {
  font-style: italic;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  padding: 1.5rem;
}

@media (max-width: 768px) {
  .filters .d-flex {
    flex-direction: column;
  }
  
  .todo-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .todo-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>