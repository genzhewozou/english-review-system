import { ref, onMounted } from 'vue'

/**
 * Composable for managing browser notifications
 */
export function useBrowserNotifications() {
  const permission = ref(Notification.permission)
  const isSupported = ref('Notification' in window)
  
  // Request permission for browser notifications
  const requestPermission = async () => {
    if (!isSupported.value) {
      return false
    }
    
    try {
      const result = await Notification.requestPermission()
      permission.value = result
      return result === 'granted'
    } catch (error) {
      console.error('Error requesting notification permission:', error)
      return false
    }
  }
  
  // Show a browser notification
  const showNotification = (title, options = {}) => {
    if (!isSupported.value || permission.value !== 'granted') {
      return null
    }
    
    const defaultOptions = {
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      requireInteraction: false,
      silent: false,
      ...options
    }
    
    try {
      const notification = new Notification(title, defaultOptions)
      
      // Auto close after 5 seconds if not set to require interaction
      if (!defaultOptions.requireInteraction) {
        setTimeout(() => {
          notification.close()
        }, 5000)
      }
      
      return notification
    } catch (error) {
      console.error('Error showing notification:', error)
      return null
    }
  }
  
  // Show notification for overdue tasks
  const showOverdueNotification = (count, tasks = []) => {
    const title = count === 1 ? 'Overdue Task' : `${count} Overdue Tasks`
    const body = count === 1 
      ? `"${tasks[0]?.title || 'Task'}" is overdue`
      : `You have ${count} overdue tasks that need attention`
    
    return showNotification(title, {
      body,
      tag: 'overdue-tasks',
      requireInteraction: true,
      icon: '/favicon.ico'
    })
  }
  
  // Show notification for due tasks
  const showDueNotification = (count, tasks = []) => {
    const title = count === 1 ? 'Task Due Today' : `${count} Tasks Due Today`
    const body = count === 1 
      ? `"${tasks[0]?.title || 'Task'}" is due today`
      : `You have ${count} tasks due today`
    
    return showNotification(title, {
      body,
      tag: 'due-tasks',
      requireInteraction: false,
      icon: '/favicon.ico'
    })
  }
  
  // Show notification for review sessions
  const showReviewNotification = (count) => {
    const title = count === 1 ? 'Review Available' : `${count} Reviews Available`
    const body = count === 1 
      ? 'You have a vocabulary review ready for practice'
      : `You have ${count} vocabulary reviews ready for practice`
    
    return showNotification(title, {
      body,
      tag: 'review-sessions',
      requireInteraction: false,
      icon: '/favicon.ico'
    })
  }
  
  // Batch notifications to avoid spam
  const batchedNotifications = ref(new Map())
  
  const showBatchedNotification = (type, data) => {
    // Clear existing timeout for this type
    if (batchedNotifications.value.has(type)) {
      clearTimeout(batchedNotifications.value.get(type).timeout)
    }
    
    // Set new timeout to batch notifications
    const timeout = setTimeout(() => {
      const batchData = batchedNotifications.value.get(type)
      if (batchData) {
        switch (type) {
          case 'overdue':
            showOverdueNotification(batchData.count, batchData.tasks)
            break
          case 'due':
            showDueNotification(batchData.count, batchData.tasks)
            break
          case 'review':
            showReviewNotification(batchData.count)
            break
        }
        batchedNotifications.value.delete(type)
      }
    }, 1000) // Wait 1 second to batch similar notifications
    
    batchedNotifications.value.set(type, {
      timeout,
      count: data.count,
      tasks: data.tasks || []
    })
  }
  
  // Check if notifications are enabled and permission is granted
  const canShowNotifications = () => {
    return isSupported.value && permission.value === 'granted'
  }
  
  // Initialize - check current permission status
  onMounted(() => {
    if (isSupported.value) {
      permission.value = Notification.permission
    }
  })
  
  return {
    permission,
    isSupported,
    requestPermission,
    showNotification,
    showOverdueNotification,
    showDueNotification,
    showReviewNotification,
    showBatchedNotification,
    canShowNotifications
  }
}