<template>
  <div class="notification-panel">
    <!-- Notification Bell Icon -->
    <div class="notification-trigger" @click="togglePanel" aria-label="Notifications">
      <div class="notification-badge-container" :class="{ 'has-notifications': unreadCount > 0 }">
        <div class="notification-icon">
          <Bell />
        </div>
        <span v-if="unreadCount > 0" class="notification-badge" :class="{ 'pulse': unreadCount > 0 }">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
    </div>

    <!-- Notification Panel -->
    <transition name="notification-fade">
      <div v-if="showPanel" class="notification-dropdown" @click.stop>
        <div class="notification-header">
          <h3 class="notification-title">Notifications</h3>
          <div class="notification-actions">
            <button 
              v-if="unreadCount > 0" 
              @click="markAllAsRead" 
              class="btn btn-sm btn-outline notification-btn"
              aria-label="Mark all notifications as read"
            >
              Mark All Read
            </button>
            <button @click="showSettings = true" class="btn btn-sm btn-outline notification-btn" aria-label="Notification settings">
              <Setting />
            </button>
          </div>
        </div>

        <div class="notification-content">
          <!-- Loading State -->
          <div v-if="loading" class="notification-loading">
            <div class="loading loading-dark"></div>
            <span>Loading notifications...</span>
          </div>

          <!-- Empty State -->
          <div v-else-if="notifications.length === 0" class="notification-empty">
            <div class="empty-icon">
              <Bell />
            </div>
            <h4 class="empty-title">No notifications yet</h4>
            <p class="empty-subtitle">You'll see updates and reminders here</p>
          </div>

          <!-- Notifications List -->
          <div v-else class="notifications-list">
            <!-- Overdue Alerts -->
            <div v-if="overdueNotifications.length > 0" class="notification-section">
              <h4 class="section-title overdue">Overdue Reviews</h4>
              <div 
                v-for="notification in overdueNotifications" 
                :key="notification.id"
                class="notification-item overdue-item"
                @click="handleNotificationClick(notification)"
                :aria-label="`Overdue notification: ${notification.title}`"
              >
                <div class="notification-content-item">
                  <div class="notification-item-title">{{ notification.title }}</div>
                  <div class="notification-item-message">{{ notification.message }}</div>
                  <div class="notification-item-time">{{ formatTime(notification.createdAt) }}</div>
                </div>
                <div class="notification-actions-item">
                  <button @click.stop="markAsRead(notification.id)" class="action-btn" aria-label="Mark as read">
                    <Check />
                  </button>
                  <button @click.stop="deleteNotification(notification.id)" class="action-btn" aria-label="Delete notification">
                    <Close />
                  </button>
                </div>
              </div>
            </div>

            <!-- Due Today -->
            <div v-if="dueNotifications.length > 0" class="notification-section">
              <h4 class="section-title due">Due Today</h4>
              <div 
                v-for="notification in dueNotifications" 
                :key="notification.id"
                class="notification-item due-item"
                @click="handleNotificationClick(notification)"
                :aria-label="`Due today notification: ${notification.title}`"
              >
                <div class="notification-content-item">
                  <div class="notification-item-title">{{ notification.title }}</div>
                  <div class="notification-item-message">{{ notification.message }}</div>
                  <div class="notification-item-time">{{ formatTime(notification.createdAt) }}</div>
                </div>
                <div class="notification-actions-item">
                  <button @click.stop="markAsRead(notification.id)" class="action-btn" aria-label="Mark as read">
                    <Check />
                  </button>
                  <button @click.stop="deleteNotification(notification.id)" class="action-btn" aria-label="Delete notification">
                    <Close />
                  </button>
                </div>
              </div>
            </div>

            <!-- Other Notifications -->
            <div class="notification-section">
              <div 
                v-for="notification in otherNotifications" 
                :key="notification.id"
                class="notification-item"
                :class="{ 'unread': !notification.read }"
                @click="handleNotificationClick(notification)"
                :aria-label="`Notification: ${notification.title}`"
              >
                <div class="notification-content-item">
                  <div class="notification-item-title">{{ notification.title }}</div>
                  <div class="notification-item-message">{{ notification.message }}</div>
                  <div class="notification-item-time">{{ formatTime(notification.createdAt) }}</div>
                </div>
                <div class="notification-actions-item">
                  <button @click.stop="markAsRead(notification.id)" class="action-btn" aria-label="Mark as read">
                    <Check />
                  </button>
                  <button @click.stop="deleteNotification(notification.id)" class="action-btn" aria-label="Delete notification">
                    <Close />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="notification-footer">
          <button @click="$router.push('/todo')" class="btn btn-sm btn-primary" aria-label="View all tasks">
            View All Tasks
          </button>
        </div>
      </div>
    </transition>

    <!-- Settings Modal -->
    <div v-if="showSettings" class="modal-overlay" @click="showSettings = false">
      <div class="settings-modal" @click.stop>
        <div class="modal-header">
          <h3>Notification Settings</h3>
          <button @click="showSettings = false" class="btn btn-sm btn-outline" aria-label="Close settings">
            <Close />
          </button>
        </div>
        <div class="modal-content">
          <NotificationSettings @saved="handleSettingsSaved" @cancel="showSettings = false" />
        </div>
      </div>
    </div>

    <!-- Click outside to close -->
    <div v-if="showPanel" class="notification-overlay" @click="showPanel = false"></div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '../stores/notificationStore'
import { useNotification } from '../composables/useNotification'
import NotificationSettings from './NotificationSettings.vue'

export default {
  name: 'NotificationPanel',
  components: {
    NotificationSettings
  },
  setup() {
    const router = useRouter()
    const notificationStore = useNotificationStore()
    const { showSuccess, showError } = useNotification()
    
    const showPanel = ref(false)
    const showSettings = ref(false)
    
    // Computed properties from store
    const notifications = computed(() => notificationStore.notifications)
    const unreadCount = computed(() => notificationStore.unreadCount)
    const overdueNotifications = computed(() => notificationStore.overdueNotifications)
    const dueNotifications = computed(() => notificationStore.dueNotifications)
    const loading = computed(() => notificationStore.loading)
    
    // Other notifications (not overdue or due today)
    const otherNotifications = computed(() => 
      notifications.value.filter(n => 
        n.type !== 'overdue' && n.type !== 'due'
      )
    )
    
    const togglePanel = () => {
      showPanel.value = !showPanel.value
    }
    
    const handleNotificationClick = async (notification) => {
      // Mark as read
      await notificationStore.markAsRead(notification.id)
      
      // Navigate based on notification type
      if (notification.type === 'review' || notification.type === 'due' || notification.type === 'overdue') {
        if (notification.type === 'review') {
          router.push('/review')
        } else {
          router.push('/todo')
        }
      } else if (notification.relatedTodoId) {
        router.push('/todo')
      }
      
      showPanel.value = false
    }
    
    const markAsRead = async (notificationId) => {
      await notificationStore.markAsRead(notificationId)
    }
    
    const markAllAsRead = async () => {
      await notificationStore.markAllAsRead()
      showSuccess('All notifications marked as read')
    }
    
    const deleteNotification = async (notificationId) => {
      await notificationStore.deleteNotification(notificationId)
    }
    
    const handleSettingsSaved = () => {
      showSettings.value = false
      showSuccess('Notification settings saved')
    }
    
    const handleSettingsClose = () => {
      showSettings.value = false
    }
    
    const formatTime = (dateString) => {
      const date = new Date(dateString)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMins / 60)
      const diffDays = Math.floor(diffHours / 24)
      
      if (diffMins < 1) return 'Just now'
      if (diffMins < 60) return `${diffMins}m ago`
      if (diffHours < 24) return `${diffHours}h ago`
      if (diffDays < 7) return `${diffDays}d ago`
      
      return date.toLocaleDateString()
    }
    
    // Handle click outside
    const handleClickOutside = (event) => {
      if (!event.target.closest('.notification-panel')) {
        showPanel.value = false
      }
    }
    
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
      // Initialize notification store
      notificationStore.initialize()
    })
    
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
    
    return {
      showPanel,
      showSettings,
      notifications,
      unreadCount,
      overdueNotifications,
      dueNotifications,
      otherNotifications,
      loading,
      togglePanel,
      handleNotificationClick,
      markAsRead,
      markAllAsRead,
      deleteNotification,
      handleSettingsSaved,
      handleSettingsClose,
      formatTime
    }
  }
}
</script>

<style scoped>
/* Notification Panel Container */
.notification-panel {
  position: relative;
}

/* Notification Bell Trigger */
.notification-trigger {
  cursor: pointer;
  padding: var(--space-2);
  border-radius: var(--radius-full);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-trigger:hover {
  background-color: var(--primary-50);
  transform: translateY(-1px);
}

/* Badge Container */
.notification-badge-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Notification Icon */
.notification-icon {
  font-size: 1.25rem;
  color: var(--text-secondary);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.notification-badge-container.has-notifications .notification-icon {
  color: var(--error-600);
  animation: pulse var(--transition-slow) var(--transition-ease-in-out) infinite;
}

/* Notification Badge */
.notification-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: var(--error-600);
  color: white;
  border-radius: var(--radius-full);
  padding: 0.125rem 0.5rem;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  min-width: 20px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.notification-badge.pulse {
  animation: pulse var(--transition-slow) var(--transition-ease-in-out) infinite;
}

/* Dropdown Panel */
.notification-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 420px;
  max-height: 550px;
  background-color: var(--surface-primary);
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-dropdown);
  overflow: hidden;
  transform-origin: top right;
}

/* Overlay */
.notification-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: var(--z-sticky);
}

/* Header */
.notification-header {
  padding: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-secondary);
}

.notification-header .notification-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.notification-actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.notification-btn {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

/* Content */
.notification-content {
  max-height: 400px;
  overflow-y: auto;
}

/* Loading State */
.notification-loading {
  padding: var(--space-8);
  text-align: center;
  color: var(--text-tertiary);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

/* Empty State */
.notification-empty {
  padding: var(--space-10);
  text-align: center;
  color: var(--text-tertiary);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.empty-icon {
  font-size: 3rem;
  color: var(--text-light);
  margin-bottom: var(--space-2);
}

.empty-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
}

.empty-subtitle {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  max-width: 200px;
}

/* Notification Sections */
.notification-section {
  border-bottom: 1px solid var(--surface-border);
}

.section-title {
  padding: var(--space-3) var(--space-4);
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  background-color: var(--bg-secondary);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.section-title.overdue {
  color: var(--error-700);
  background-color: var(--error-50);
}

.section-title.due {
  color: var(--warning-700);
  background-color: var(--warning-50);
}

/* Notification Items */
.notification-item {
  padding: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  position: relative;
  overflow: hidden;
}

.notification-item:hover {
  background-color: var(--bg-secondary);
  transform: translateX(4px);
}

.notification-item.unread {
  background-color: var(--primary-50);
  border-left: 3px solid var(--primary-600);
}

.notification-item.overdue-item {
  background-color: var(--error-50);
  border-left: 3px solid var(--error-600);
}

.notification-item.due-item {
  background-color: var(--warning-50);
  border-left: 3px solid var(--warning-600);
}

/* Notification Content */
.notification-content-item {
  flex: 1;
  min-width: 0;
}

.notification-item-title {
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  margin-bottom: var(--space-1);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-item-message {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
  line-height: var(--leading-relaxed);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-item-time {
  font-size: var(--text-xs);
  color: var(--text-light);
  font-weight: var(--font-medium);
}

/* Action Buttons */
.notification-actions-item {
  display: flex;
  gap: var(--space-1);
  margin-left: var(--space-2);
  flex-shrink: 0;
}

.action-btn {
  background-color: transparent;
  border: none;
  color: var(--text-tertiary);
  padding: var(--space-1);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast) var(--transition-ease-in-out);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
  transform: scale(1.1);
}

/* Footer */
.notification-footer {
  padding: var(--space-4);
  border-top: 1px solid var(--surface-border);
  background-color: var(--bg-secondary);
  text-align: center;
}

/* Settings Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

.settings-modal {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  width: 90%;
  max-width: 700px;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp var(--transition-normal) var(--transition-ease-out);
}

.modal-header {
  padding: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-secondary);
}

.modal-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.modal-content {
  padding: var(--space-6);
  max-height: 60vh;
  overflow-y: auto;
}

/* Animations */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Transitions */
.notification-fade-enter-active,
.notification-fade-leave-active {
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.notification-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.notification-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .notification-dropdown {
    width: 340px;
    right: -20px;
    max-height: 500px;
  }
  
  .notification-header {
    padding: var(--space-3);
  }
  
  .notification-header .notification-title {
    font-size: var(--text-base);
  }
  
  .notification-content {
    max-height: 350px;
  }
  
  .notification-item {
    padding: var(--space-3);
  }
  
  .notification-btn {
    font-size: var(--text-xs);
    padding: var(--space-1) var(--space-2);
  }
  
  .settings-modal {
    width: 95%;
    max-width: 500px;
  }
  
  .modal-content {
    padding: var(--space-4);
  }
}

@media (max-width: 480px) {
  .notification-dropdown {
    width: 300px;
    right: -10px;
  }
  
  .notification-item-title {
    font-size: var(--text-xs);
  }
  
  .notification-item-message {
    font-size: var(--text-xs);
  }
  
  .section-title {
    font-size: var(--text-xs);
    padding: var(--space-2) var(--space-3);
  }
}
</style>