const todoService = require('../../services/todoService')

Page({
  data: {
    tasks: [],
    filteredTasks: [],
    loading: false,
    saving: false,
    
    showAddModal: false,
    editingTask: null,
    
    taskForm: {
      title: '',
      description: '',
      dueDate: '',
      type: 'CUSTOM_TASK'
    },
    
    filterStatus: 0, // 0: All, 1: Pending, 2: Completed, 3: Overdue
    filterType: 0, // 0: All, 1: REVIEW_SESSION, 2: CUSTOM_TASK
    
    statusOptions: ['All Tasks', 'Pending', 'Completed', 'Overdue'],
    typeOptions: ['All Types', 'Review Sessions', 'Custom Tasks']
  },

  onLoad() {
    this.loadTasks()
  },

  onShow() {
    // Refresh tasks when returning to page
    this.loadTasks()
  },

  // Load tasks
  async loadTasks() {
    this.setData({ loading: true })
    
    try {
      const tasks = await todoService.getAllTodos()
      this.setData({ tasks })
      this.filterTasks()
      this.checkForOverdueItems()
    } catch (error) {
      console.error('Error loading tasks:', error)
      this.setData({ tasks: [] })
      this.filterTasks()
      wx.showToast({ title: 'Failed to load tasks', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // Filter tasks
  filterTasks() {
    let filtered = this.data.tasks
    
    // Filter by status
    const today = new Date().toISOString().split('T')[0]
    
    switch (this.data.filterStatus) {
      case 1: // Pending
        filtered = filtered.filter(t => !t.completed)
        break
      case 2: // Completed
        filtered = filtered.filter(t => t.completed)
        break
      case 3: // Overdue
        filtered = filtered.filter(t => !t.completed && t.dueDate && t.dueDate < today)
        break
      case 0: // All
      default:
        break
    }
    
    // Filter by type
    switch (this.data.filterType) {
      case 1: // Review Sessions
        filtered = filtered.filter(t => t.type === 'REVIEW_SESSION')
        break
      case 2: // Custom Tasks
        filtered = filtered.filter(t => t.type === 'CUSTOM_TASK')
        break
      case 0: // All
      default:
        break
    }
    
    // Sort by due date, then by creation date
    filtered.sort((a, b) => {
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
    
    this.setData({ filteredTasks: filtered })
  },

  // Handle status filter change
  handleStatusFilterChange(e) {
    this.setData({ filterStatus: parseInt(e.detail.value) })
    this.filterTasks()
  },

  // Handle type filter change
  handleTypeFilterChange(e) {
    this.setData({ filterType: parseInt(e.detail.value) })
    this.filterTasks()
  },

  // Check for overdue items
  checkForOverdueItems() {
    const today = new Date().toISOString().split('T')[0]
    const overdueItems = this.data.tasks.filter(t => 
      !t.completed && t.dueDate && t.dueDate < today
    )
    
    if (overdueItems.length > 0) {
      const message = overdueItems.length === 1 
        ? `You have 1 overdue task: ${overdueItems[0].title}`
        : `You have ${overdueItems.length} overdue tasks`
      
      wx.showToast({
        title: message,
        icon: 'none',
        duration: 3000
      })
    }
    
    // Check for items due today
    const dueTodayItems = this.data.tasks.filter(t => 
      !t.completed && t.dueDate && t.dueDate === today
    )
    
    if (dueTodayItems.length > 0) {
      const message = dueTodayItems.length === 1 
        ? `You have 1 task due today: ${dueTodayItems[0].title}`
        : `You have ${dueTodayItems.length} tasks due today`
      
      wx.showToast({
        title: message,
        icon: 'none',
        duration: 3000
      })
    }
  },

  // Add task
  addTask() {
    this.setData({
      editingTask: null,
      taskForm: {
        title: '',
        description: '',
        dueDate: '',
        type: 'CUSTOM_TASK'
      },
      showAddModal: true
    })
  },

  // Edit task
  editTask(e) {
    const index = e.currentTarget.dataset.index
    const task = this.data.filteredTasks[index]
    
    if (!task) {
      console.error('Task not found for editing:', index)
      return
    }
    
    this.setData({
      editingTask: task,
      taskForm: {
        title: task.title || '',
        description: task.description || '',
        dueDate: task.dueDate || '',
        type: task.type || 'CUSTOM_TASK'
      },
      showAddModal: true
    })
  },

  // Close add modal
  closeAddModal() {
    this.setData({ 
      showAddModal: false,
      editingTask: null
    })
  },

  // Handle task title input
  handleTaskTitleInput(e) {
    this.setData({ 'taskForm.title': e.detail.value })
  },

  // Handle task description input
  handleTaskDescriptionInput(e) {
    this.setData({ 'taskForm.description': e.detail.value })
  },

  // Handle task due date change
  handleTaskDueDateChange(e) {
    this.setData({ 'taskForm.dueDate': e.detail.value })
  },

  // Save task
  async saveTask(e) {
    e.preventDefault()
    
    if (!this.data.taskForm.title.trim()) {
      wx.showToast({ title: 'Please enter task title', icon: 'none' })
      return
    }
    
    this.setData({ saving: true })
    
    try {
      const taskData = {
        title: this.data.taskForm.title,
        description: this.data.taskForm.description,
        dueDate: this.data.taskForm.dueDate || null,
        type: this.data.taskForm.type || 'CUSTOM_TASK'
      }
      
      if (this.data.editingTask) {
        // Update existing task
        await todoService.updateTodo(this.data.editingTask.id, taskData)
        wx.showToast({ title: 'Task updated successfully', icon: 'success' })
      } else {
        // Add new task
        await todoService.createTodo(taskData)
        wx.showToast({ title: 'Task added successfully', icon: 'success' })
      }
      
      this.closeAddModal()
      await this.loadTasks()
    } catch (error) {
      console.error('Error saving task:', error)
      wx.showToast({ 
        title: this.data.editingTask ? 'Failed to update task' : 'Failed to add task', 
        icon: 'none' 
      })
    } finally {
      this.setData({ saving: false })
    }
  },

  // Toggle task complete
  async toggleTaskComplete(e) {
    const id = e.currentTarget.dataset.id
    const task = this.data.tasks.find(t => t.id == id)
    
    if (!task) return
    
    try {
      await todoService.completeTodo(id)
      await this.loadTasks()
      
      if (!task.completed) {
        wx.showToast({ title: 'Task completed!', icon: 'success' })
      }
    } catch (error) {
      console.error('Error toggling task complete:', error)
      wx.showToast({ title: 'Failed to update task status', icon: 'none' })
    }
  },

  // Delete task
  async deleteTask(e) {
    const id = e.currentTarget.dataset.id
    const task = this.data.tasks.find(t => t.id == id)
    
    if (!task) return
    
    wx.showModal({
      title: 'Confirm Deletion',
      content: `Are you sure you want to delete "${task.title}"?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            await todoService.deleteTodo(id)
            await this.loadTasks()
            wx.showToast({ title: 'Task deleted successfully', icon: 'success' })
          } catch (error) {
            console.error('Error deleting task:', error)
            wx.showToast({ title: 'Failed to delete task', icon: 'none' })
          }
        }
      }
    })
  },

  // Handle task click
  handleTaskClick(e) {
    const id = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    const task = this.data.filteredTasks[index]
    
    if (!task) {
      console.error('Task not found:', id, index)
      return
    }
    
    // If it's a review session task, navigate to review-session page
    if (task.type === 'REVIEW_SESSION') {
      const sessionId = task.relatedSessionId || task.id
      
      wx.navigateTo({
        url: `/pages/review-session/review-session?id=${sessionId}`,
        fail: (err) => {
          console.error('Navigation failed:', err)
          wx.showToast({ title: 'Failed to navigate to review session', icon: 'none' })
        }
      })
    } else {
      // For other tasks, open edit modal
      this.editTask(e)
    }
  },

  // Format date
  formatDate(dateString) {
    if (!dateString) return ''
    return new Date(dateString).toLocaleDateString()
  },

  // Get todo card class
  getTodoCardClass(todo) {
    if (todo.completed) return 'completed-todo'
    
    if (todo.dueDate) {
      const today = new Date().toISOString().split('T')[0]
      if (todo.dueDate < today) return 'overdue-todo'
      if (todo.dueDate === today) return 'due-today-todo'
    }
    
    return ''
  },

  // Get due date class
  getDueDateClass(todo) {
    if (todo.completed) return ''
    
    const today = new Date().toISOString().split('T')[0]
    if (todo.dueDate < today) return 'overdue'
    if (todo.dueDate === today) return 'due-today'
    
    return ''
  }
})
