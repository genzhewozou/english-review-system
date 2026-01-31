<template>
  <div class="notification-panel">
    <!-- Notification Bell Icon -->
    <div class="notification-trigger" @click="togglePanel">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-icon :size="24" class="notification-icon" :class="{ 'has-notifications': unreadCount > 0 }">
          <Bell />
        </el-icon>
      </el-badge>
    </div>

    <!-- Notification Panel -->
    <div v-if="showPanel" class="notification-dropdown" @click.stop>
      <div class="notification-header">
        <h3>Notifications</h3>
        <div class="notification-actions">
          <el-button 
            v-if="unreadCount > 0" 
            @click="markAllAsRead" 
            size="small" 
            type="text"
          >
            Mark All Read
          </el-button>
          <el-button @click="showSettings = true" size="small" type="text">
            <el-icon><Setting /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="notification-content">
        <!-- Loading State -->
        <div v-if="loading" class="notification-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>Loading notifications...</span>
        </div>

        <!-- Empty State -->
        <div v-else-if="notifications.length === 0" class="notification-empty">
          <el-icon><Bell /></el-icon>
          <p>No notifications yet</p>
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
            >
              <div class="notification-content-item">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
              </div>
              <div class="notification-actions-item">
                <el-button @click.stop="markAsRead(notification.id)" size="small" type="text">
                  <el-icon><Check /></el-icon>
                </el-button>
                <el-button @click.stop="deleteNotification(notification.id)" size="small" type="text">
                  <el-icon><Close /></el-icon>
                </el-button>
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
            >
              <div class="notification-content-item">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
              </div>
              <div class="notification-actions-item">
                <el-button @click.stop="markAsRead(notification.id)" size="small" type="text">
                  <el-icon><Check /></el-icon>
                </el-button>
                <el-button @click.stop="deleteNotification(notification.id)" size="small" type="text">
                  <el-icon><Close /></el-icon>
                </el-button>
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
            >
              <div class="notification-content-item">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
              </div>
              <div class="notification-actions-item">
                <el-button @click.stop="markAsRead(notification.id)" size="small" type="text">
                  <el-icon><Check /></el-icon>
                </el-button>
                <el-button @click.stop="deleteNotification(notification.id)" size="small" type="text">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="notification-footer">
        <el-button @click="$router.push('/todo')" size="small" type="primary">
          View All Tasks
        </el-button>
      </div>
    </div>

    <!-- Settings Modal -->
    <el-dialog v-model="showSettings" title="Notification Settings" width="700px" :before-close="handleSettingsClose">
      <NotificationSettings @saved="handleSettingsSaved" @cancel="showSettings = false" />
    </el-dialog>

    <!-- Click outside to close -->
    <div v-if="showPanel" class="notification-overlay" @click="showPanel = false"></div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '../stores/notificationStore'
import { useNotification } from '../composables/useNotification'
import { Bell, Setting, Loading, Check, Close } from '@element-plus/icons-vue'
import NotificationSettings from './NotificationSettings.vue'

export default {
  name: 'NotificationPanel',
  components: {
    Bell,
    Setting,
    Loading,
    Check,
    Close,
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
.notification-panel {
  position: relative;
}

.notification-trigger {
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.notification-trigger:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.notification-icon {
  color: #606266;
  transition: color 0.3s;
}

.notification-icon.has-notifications {
  color: #f56c6c;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.notification-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 400px;
  max-height: 500px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.notification-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.notification-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
}

.notification-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.notification-actions {
  display: flex;
  gap: 8px;
}

.notification-content {
  max-height: 350px;
  overflow-y: auto;
}

.notification-loading,
.notification-empty {
  padding: 32px;
  text-align: center;
  color: #909399;
}

.notification-empty .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.notification-section {
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  padding: 12px 16px 8px;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  background-color: #f8f9fa;
}

.section-title.overdue {
  color: #f56c6c;
  background-color: #fef0f0;
}

.section-title.due {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.notification-item:hover {
  background-color: #f8f9fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.notification-item.overdue-item {
  background-color: #fef0f0;
  border-left: 3px solid #f56c6c;
}

.notification-item.due-item {
  background-color: #fdf6ec;
  border-left: 3px solid #e6a23c;
}

.notification-content-item {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
  color: #303133;
}

.notification-message {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions-item {
  display: flex;
  gap: 4px;
  margin-left: 8px;
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
  background-color: #f8f9fa;
  text-align: center;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .notification-dropdown {
    width: 320px;
    right: -50px;
  }
}
</style>