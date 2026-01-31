<template>
  <div class="notification-settings">
    <div class="settings-header">
      <h3>Notification Settings</h3>
      <p class="settings-description">
        Configure how and when you receive notifications about your learning progress.
      </p>
    </div>
    
    <div class="settings-form">
      <!-- Master Enable/Disable -->
      <div class="setting-group">
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Enable Notifications</label>
            <p class="setting-description">Turn all notifications on or off</p>
          </div>
          <el-switch 
            v-model="localSettings.enabled" 
            @change="handleSettingChange"
            size="large"
          />
        </div>
      </div>
      
      <!-- Browser Notifications -->
      <div class="setting-group" :class="{ 'disabled': !localSettings.enabled }">
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Browser Notifications</label>
            <p class="setting-description">Show desktop notifications in your browser</p>
          </div>
          <div class="setting-control">
            <el-switch 
              v-model="localSettings.browserNotifications" 
              @change="handleBrowserNotificationChange"
              :disabled="!localSettings.enabled"
            />
            <el-button 
              v-if="!browserPermissionGranted && localSettings.browserNotifications"
              @click="requestBrowserPermission"
              size="small"
              type="primary"
            >
              Grant Permission
            </el-button>
          </div>
        </div>
        
        <div v-if="browserPermissionStatus === 'denied'" class="permission-warning">
          <el-alert
            title="Browser notifications are blocked"
            description="Please enable notifications in your browser settings to receive desktop alerts."
            type="warning"
            :closable="false"
          />
        </div>
      </div>
      
      <!-- Email Notifications -->
      <div class="setting-group" :class="{ 'disabled': !localSettings.enabled }">
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Email Notifications</label>
            <p class="setting-description">Receive notifications via email</p>
          </div>
          <el-switch 
            v-model="localSettings.emailNotifications" 
            @change="handleSettingChange"
            :disabled="!localSettings.enabled"
          />
        </div>
      </div>
      
      <!-- Notification Timing -->
      <div class="setting-group" :class="{ 'disabled': !localSettings.enabled }">
        <h4 class="group-title">Timing Settings</h4>
        
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Reminder Time</label>
            <p class="setting-description">How many hours before due date to send reminders</p>
          </div>
          <el-input-number 
            v-model="localSettings.reminderHours" 
            @change="handleSettingChange"
            :min="1" 
            :max="168"
            :disabled="!localSettings.enabled"
            size="small"
          />
        </div>
        
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Batch Notifications</label>
            <p class="setting-description">Group multiple notifications together to reduce interruptions</p>
          </div>
          <el-switch 
            v-model="localSettings.batchNotifications" 
            @change="handleSettingChange"
            :disabled="!localSettings.enabled"
          />
        </div>
      </div>
      
      <!-- Notification Types -->
      <div class="setting-group" :class="{ 'disabled': !localSettings.enabled }">
        <h4 class="group-title">Notification Types</h4>
        
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Overdue Tasks</label>
            <p class="setting-description">Alert when tasks become overdue</p>
          </div>
          <el-switch 
            v-model="localSettings.overdueAlerts" 
            @change="handleSettingChange"
            :disabled="!localSettings.enabled"
          />
        </div>
        
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Due Today</label>
            <p class="setting-description">Notify about tasks due today</p>
          </div>
          <el-switch 
            v-model="localSettings.dueTodayAlerts" 
            @change="handleSettingChange"
            :disabled="!localSettings.enabled"
          />
        </div>
        
        <div class="setting-item">
          <div class="setting-info">
            <label class="setting-label">Review Reminders</label>
            <p class="setting-description">Remind about available vocabulary reviews</p>
          </div>
          <el-switch 
            v-model="localSettings.reviewReminders" 
            @change="handleSettingChange"
            :disabled="!localSettings.enabled"
          />
        </div>
      </div>
    </div>
    
    <div class="settings-footer">
      <el-button @click="resetToDefaults" :disabled="saving">
        Reset to Defaults
      </el-button>
      <div class="save-actions">
        <el-button @click="$emit('cancel')" :disabled="saving">
          Cancel
        </el-button>
        <el-button 
          type="primary" 
          @click="saveSettings" 
          :loading="saving"
        >
          Save Settings
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useNotificationStore } from '../stores/notificationStore'
import { useBrowserNotifications } from '../composables/useBrowserNotifications'
import { useNotification } from '../composables/useNotification'

export default {
  name: 'NotificationSettings',
  emits: ['saved', 'cancel'],
  setup(props, { emit }) {
    const notificationStore = useNotificationStore()
    const { 
      permission, 
      isSupported, 
      requestPermission 
    } = useBrowserNotifications()
    const { showSuccess, showError } = useNotification()
    
    const saving = ref(false)
    
    // Default settings
    const defaultSettings = {
      enabled: true,
      browserNotifications: true,
      emailNotifications: false,
      reminderHours: 24,
      batchNotifications: true,
      overdueAlerts: true,
      dueTodayAlerts: true,
      reviewReminders: true
    }
    
    // Local copy of settings for editing
    const localSettings = ref({ 
      ...defaultSettings,
      ...notificationStore.preferences 
    })
    
    // Computed properties
    const browserPermissionGranted = computed(() => 
      permission.value === 'granted'
    )
    
    const browserPermissionStatus = computed(() => permission.value)
    
    // Handle browser notification permission
    const requestBrowserPermission = async () => {
      const granted = await requestPermission()
      if (granted) {
        showSuccess('Browser notifications enabled')
      } else {
        showError('Browser notification permission denied')
        localSettings.value.browserNotifications = false
      }
    }
    
    const handleBrowserNotificationChange = (enabled) => {
      if (enabled && !browserPermissionGranted.value) {
        requestBrowserPermission()
      }
      handleSettingChange()
    }
    
    const handleSettingChange = () => {
      // Auto-save could be implemented here if desired
      // For now, we'll save manually
    }
    
    const saveSettings = async () => {
      saving.value = true
      try {
        const success = await notificationStore.savePreferences(localSettings.value)
        if (success) {
          showSuccess('Notification settings saved successfully')
          emit('saved')
        } else {
          showError('Failed to save notification settings')
        }
      } catch (error) {
        console.error('Error saving settings:', error)
        showError('An error occurred while saving settings')
      } finally {
        saving.value = false
      }
    }
    
    const resetToDefaults = () => {
      localSettings.value = { ...defaultSettings }
      showSuccess('Settings reset to defaults')
    }
    
    // Watch for changes in store preferences
    watch(
      () => notificationStore.preferences,
      (newPreferences) => {
        localSettings.value = { ...defaultSettings, ...newPreferences }
      },
      { deep: true }
    )
    
    onMounted(() => {
      // Load current preferences
      localSettings.value = { 
        ...defaultSettings, 
        ...notificationStore.preferences 
      }
    })
    
    return {
      localSettings,
      saving,
      browserPermissionGranted,
      browserPermissionStatus,
      isSupported,
      requestBrowserPermission,
      handleBrowserNotificationChange,
      handleSettingChange,
      saveSettings,
      resetToDefaults
    }
  }
}
</script>

<style scoped>
.notification-settings {
  max-width: 600px;
  margin: 0 auto;
}

.settings-header {
  margin-bottom: 2rem;
  text-align: center;
}

.settings-header h3 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.settings-description {
  color: #6c757d;
  margin: 0;
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.setting-group {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 1.5rem;
  transition: opacity 0.3s;
}

.setting-group.disabled {
  opacity: 0.5;
}

.group-title {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 600;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 0.5rem;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-info {
  flex: 1;
}

.setting-label {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.setting-description {
  font-size: 0.9rem;
  color: #6c757d;
  margin: 0;
  line-height: 1.4;
}

.setting-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.permission-warning {
  margin-top: 1rem;
}

.settings-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e4e7ed;
}

.save-actions {
  display: flex;
  gap: 0.75rem;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .setting-control {
    align-self: flex-end;
  }
  
  .settings-footer {
    flex-direction: column;
    gap: 1rem;
  }
  
  .save-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>