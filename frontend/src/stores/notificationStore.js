import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useApiService } from '../composables/useApiService'

export const useNotificationStore = defineStore('notification', () => {
  // State
  const notifications = ref([])
  const preferences = ref({
    enabled: true,
    emailNotifications: false,
    browserNotifications: true,
    reminderHours: 24, // Hours before due date to remind
    batchNotifications: true,
    overdueAlerts: true,
    dueTodayAlerts: true,
    reviewReminders: true
  })
  const loading = ref(false)

  // API service
  const { apiService } = useApiService()

  // Computed
  const unreadCount = computed(() => 
    notifications.value.filter(n => !n.read).length
  )

  const overdueNotifications = computed(() =>
    notifications.value.filter(n => n.type === 'overdue' && !n.read)
  )

  const dueNotifications = computed(() =>
    notifications.value.filter(n => n.type === 'due' && !n.read)
  )

  const reviewNotifications = computed(() =>
    notifications.value.filter(n => n.type === 'review' && !n.read)
  )

  // Actions
  const loadNotifications = async () => {
    loading.value = true
    try {
      const response = await apiService.get('/notifications')
      notifications.value = response.data || []
    } catch (error) {
      console.error('Error loading notifications:', error)
      // For development, create some mock notifications
      notifications.value = [
        {
          id: 1,
          type: 'due',
          title: '6 Tasks Due Today',
          message: 'You have 6 tasks due today',
          read: false,
          createdAt: new Date().toISOString()
        },
        {
          id: 2,
          type: 'overdue',
          title: '2 Overdue Tasks',
          message: 'You have 2 tasks that are overdue',
          read: false,
          createdAt: new Date(Date.now() - 3600000).toISOString()
        },
        {
          id: 3,
          type: 'review',
          title: 'Review Available',
          message: 'You have 10 cards ready for review',
          read: true,
          createdAt: new Date(Date.now() - 7200000).toISOString()
        }
      ]
    } finally {
      loading.value = false
    }
  }

  const loadPreferences = async () => {
    try {
      const response = await apiService.get('/notifications/preferences')
      if (response.data) {
        preferences.value = { ...preferences.value, ...response.data }
      }
    } catch (error) {
      console.error('Error loading notification preferences:', error)
      // Use default preferences for development
    }
  }

  const savePreferences = async (newPreferences) => {
    try {
      await apiService.put('/notifications/preferences', newPreferences)
      preferences.value = { ...preferences.value, ...newPreferences }
      return true
    } catch (error) {
      console.error('Error saving notification preferences:', error)
      // For development, save to localStorage
      localStorage.setItem('notificationPreferences', JSON.stringify(newPreferences))
      preferences.value = { ...preferences.value, ...newPreferences }
      return true
    }
  }

  const markAsRead = async (notificationId) => {
    try {
      await apiService.put(`/notifications/${notificationId}/read`)
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.read = true
      }
    } catch (error) {
      console.error('Error marking notification as read:', error)
      // For development, update locally
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.read = true
      }
    }
  }

  const markAllAsRead = async () => {
    try {
      await apiService.put('/notifications/read-all')
      notifications.value.forEach(n => n.read = true)
    } catch (error) {
      console.error('Error marking all notifications as read:', error)
      // For development, update locally
      notifications.value.forEach(n => n.read = true)
    }
  }

  const deleteNotification = async (notificationId) => {
    try {
      await apiService.delete(`/notifications/${notificationId}`)
      notifications.value = notifications.value.filter(n => n.id !== notificationId)
    } catch (error) {
      console.error('Error deleting notification:', error)
      // For development, delete locally
      notifications.value = notifications.value.filter(n => n.id !== notificationId)
    }
  }

  const checkForDueReviews = async () => {
    try {
      const response = await apiService.get('/notifications/check-due')
      if (response.data && response.data.length > 0) {
        // Add new notifications to the list
        const newNotifications = response.data.filter(newNotif => 
          !notifications.value.some(existing => existing.id === newNotif.id)
        )
        notifications.value.unshift(...newNotifications)
        
        // Show browser notifications if enabled
        if (preferences.value.browserNotifications && 'Notification' in window) {
          showBrowserNotifications(newNotifications)
        }
        
        return newNotifications
      }
      return []
    } catch (error) {
      console.error('Error checking for due reviews:', error)
      return []
    }
  }

  const showBrowserNotifications = (notificationList) => {
    if (!preferences.value.enabled || !preferences.value.browserNotifications) {
      return
    }

    if (Notification.permission === 'granted') {
      if (preferences.value.batchNotifications && notificationList.length > 1) {
        // Show batched notification
        const title = `${notificationList.length} New Notifications`
        const body = notificationList.map(n => n.title).join(', ')
        new Notification(title, {
          body: body.length > 100 ? body.substring(0, 97) + '...' : body,
          icon: '/favicon.ico',
          tag: 'batched-notifications'
        })
      } else {
        // Show individual notifications
        notificationList.forEach(notif => {
          new Notification(notif.title, {
            body: notif.message,
            icon: '/favicon.ico',
            tag: `notification-${notif.id}`
          })
        })
      }
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          showBrowserNotifications(notificationList)
        }
      })
    }
  }

  const addNotification = (notification) => {
    const newNotification = {
      id: Date.now() + Math.random(), // Temporary ID for local notifications
      ...notification,
      read: false,
      createdAt: new Date().toISOString()
    }
    
    notifications.value.unshift(newNotification)
    
    // Show browser notification if enabled
    if (preferences.value.enabled && preferences.value.browserNotifications) {
      showBrowserNotifications([newNotification])
    }
    
    return newNotification
  }

  const createOverdueNotification = (tasks) => {
    if (!preferences.value.enabled || !preferences.value.overdueAlerts) {
      return null
    }

    const count = tasks.length
    const title = count === 1 ? 'Overdue Task' : `${count} Overdue Tasks`
    const message = count === 1 
      ? `"${tasks[0].title}" is overdue`
      : `You have ${count} overdue tasks that need attention`

    return addNotification({
      type: 'overdue',
      title,
      message,
      relatedTodoIds: tasks.map(t => t.id)
    })
  }

  const createDueTodayNotification = (tasks) => {
    if (!preferences.value.enabled || !preferences.value.dueTodayAlerts) {
      return null
    }

    const count = tasks.length
    const title = count === 1 ? 'Task Due Today' : `${count} Tasks Due Today`
    const message = count === 1 
      ? `"${tasks[0].title}" is due today`
      : `You have ${count} tasks due today`

    return addNotification({
      type: 'due',
      title,
      message,
      relatedTodoIds: tasks.map(t => t.id)
    })
  }

  const createReviewNotification = (count) => {
    if (!preferences.value.enabled || !preferences.value.reviewReminders) {
      return null
    }

    const title = count === 1 ? 'Review Available' : `${count} Reviews Available`
    const message = count === 1 
      ? 'You have a vocabulary review ready for practice'
      : `You have ${count} vocabulary reviews ready for practice`

    return addNotification({
      type: 'review',
      title,
      message
    })
  }

  // Initialize store
  const initialize = async () => {
    // Load preferences from localStorage for development
    const savedPreferences = localStorage.getItem('notificationPreferences')
    if (savedPreferences) {
      try {
        const parsed = JSON.parse(savedPreferences)
        preferences.value = { ...preferences.value, ...parsed }
      } catch (error) {
        console.error('Error parsing saved preferences:', error)
      }
    }

    await Promise.all([
      loadNotifications(),
      loadPreferences()
    ])
    
    // Set up periodic checking for due reviews
    setInterval(checkForDueReviews, 5 * 60 * 1000) // Check every 5 minutes
  }

  return {
    // State
    notifications,
    preferences,
    loading,
    
    // Computed
    unreadCount,
    overdueNotifications,
    dueNotifications,
    reviewNotifications,
    
    // Actions
    loadNotifications,
    loadPreferences,
    savePreferences,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    checkForDueReviews,
    addNotification,
    createOverdueNotification,
    createDueTodayNotification,
    createReviewNotification,
    initialize
  }
})