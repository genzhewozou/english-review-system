<template>
  <div v-if="showAlert" class="notification-alert" :class="alertClass">
    <div class="alert-content">
      <el-icon class="alert-icon">
        <component :is="alertIcon" />
      </el-icon>
      <div class="alert-text">
        <div class="alert-title">{{ title }}</div>
        <div class="alert-message">{{ message }}</div>
      </div>
    </div>
    <div class="alert-actions">
      <el-button v-if="actionText" @click="handleAction" size="small" :type="actionType">
        {{ actionText }}
      </el-button>
      <el-button @click="dismiss" size="small" type="text">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { Warning, Clock, InfoFilled, Close } from '@element-plus/icons-vue'

export default {
  name: 'NotificationAlert',
  components: {
    Warning,
    Clock,
    InfoFilled,
    Close
  },
  props: {
    type: {
      type: String,
      default: 'info',
      validator: value => ['success', 'warning', 'error', 'info', 'overdue'].includes(value)
    },
    title: {
      type: String,
      required: true
    },
    message: {
      type: String,
      required: true
    },
    actionText: {
      type: String,
      default: ''
    },
    actionType: {
      type: String,
      default: 'primary'
    },
    dismissible: {
      type: Boolean,
      default: true
    },
    autoClose: {
      type: Number,
      default: 0 // 0 means no auto close
    }
  },
  emits: ['action', 'dismiss'],
  setup(props, { emit }) {
    const showAlert = ref(true)
    
    const alertClass = computed(() => {
      return `alert-${props.type}`
    })
    
    const alertIcon = computed(() => {
      switch (props.type) {
        case 'warning':
        case 'overdue':
          return Warning
        case 'info':
          return InfoFilled
        default:
          return Clock
      }
    })
    
    const handleAction = () => {
      emit('action')
    }
    
    const dismiss = () => {
      showAlert.value = false
      emit('dismiss')
    }
    
    // Auto close if specified
    if (props.autoClose > 0) {
      setTimeout(() => {
        if (showAlert.value) {
          dismiss()
        }
      }, props.autoClose)
    }
    
    return {
      showAlert,
      alertClass,
      alertIcon,
      handleAction,
      dismiss
    }
  }
}
</script>

<style scoped>
.notification-alert {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 16px;
  border-radius: 8px;
  border: 1px solid;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.alert-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.alert-text {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 2px;
}

.alert-message {
  font-size: 13px;
  opacity: 0.9;
}

.alert-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

/* Alert type styles */
.alert-info {
  background-color: #f0f9ff;
  border-color: #409eff;
  color: #409eff;
}

.alert-warning {
  background-color: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
}

.alert-error {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

.alert-success {
  background-color: #f0f9ff;
  border-color: #67c23a;
  color: #67c23a;
}

.alert-overdue {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    border-color: #f56c6c;
  }
  50% {
    border-color: #ff8080;
  }
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .notification-alert {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .alert-actions {
    margin-left: 0;
    align-self: flex-end;
  }
}
</style>