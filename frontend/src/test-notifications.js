// Simple test file to verify notification functionality
// This can be run in the browser console to test notifications

// Test notification store functionality
function testNotificationStore() {
  console.log('Testing notification store...')
  
  // Mock todo items for testing
  const mockTodos = [
    {
      id: 1,
      title: 'Review vocabulary from Chapter 1',
      dueDate: '2026-01-28', // Yesterday (overdue)
      completed: false,
      type: 'REVIEW_SESSION'
    },
    {
      id: 2,
      title: 'Complete grammar exercises',
      dueDate: '2026-01-29', // Today
      completed: false,
      type: 'CUSTOM_TASK'
    },
    {
      id: 3,
      title: 'Watch video lesson',
      dueDate: '2026-01-30', // Tomorrow
      completed: false,
      type: 'CUSTOM_TASK'
    }
  ]
  
  // Test overdue detection
  const today = new Date().toISOString().split('T')[0]
  const overdueItems = mockTodos.filter(t => 
    !t.completed && t.dueDate && t.dueDate < today
  )
  
  const dueTodayItems = mockTodos.filter(t => 
    !t.completed && t.dueDate && t.dueDate === today
  )
  
  console.log('Overdue items:', overdueItems)
  console.log('Due today items:', dueTodayItems)
  
  return {
    overdueItems,
    dueTodayItems,
    totalPending: mockTodos.filter(t => !t.completed).length
  }
}

// Test browser notification permissions
function testBrowserNotifications() {
  console.log('Testing browser notifications...')
  
  if (!('Notification' in window)) {
    console.log('Browser notifications not supported')
    return false
  }
  
  console.log('Current permission:', Notification.permission)
  
  if (Notification.permission === 'granted') {
    // Show test notification
    const notification = new Notification('Test Notification', {
      body: 'This is a test notification from the English Learning System',
      icon: '/favicon.ico',
      tag: 'test-notification'
    })
    
    setTimeout(() => {
      notification.close()
    }, 3000)
    
    return true
  } else if (Notification.permission !== 'denied') {
    Notification.requestPermission().then(permission => {
      console.log('Permission result:', permission)
      if (permission === 'granted') {
        testBrowserNotifications()
      }
    })
  }
  
  return false
}

// Test notification preferences
function testNotificationPreferences() {
  console.log('Testing notification preferences...')
  
  const defaultPreferences = {
    enabled: true,
    browserNotifications: true,
    emailNotifications: false,
    reminderHours: 24,
    batchNotifications: true,
    overdueAlerts: true,
    dueTodayAlerts: true,
    reviewReminders: true
  }
  
  // Save to localStorage for testing
  localStorage.setItem('notificationPreferences', JSON.stringify(defaultPreferences))
  
  // Retrieve and verify
  const saved = JSON.parse(localStorage.getItem('notificationPreferences'))
  console.log('Saved preferences:', saved)
  
  return saved
}

// Export functions for browser console testing
if (typeof window !== 'undefined') {
  window.testNotificationStore = testNotificationStore
  window.testBrowserNotifications = testBrowserNotifications
  window.testNotificationPreferences = testNotificationPreferences
  
  console.log('Notification test functions loaded:')
  console.log('- testNotificationStore()')
  console.log('- testBrowserNotifications()')
  console.log('- testNotificationPreferences()')
}

export {
  testNotificationStore,
  testBrowserNotifications,
  testNotificationPreferences
}