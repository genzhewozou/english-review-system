import { ElMessage, ElNotification } from 'element-plus'

/**
 * Composable for handling notifications and messages
 */
export function useNotification() {
  // Success message
  const showSuccess = (message, title = 'Success') => {
    ElMessage.success(message)
  }

  // Error message
  const showError = (message, title = 'Error') => {
    ElMessage.error(message)
  }

  // Warning message
  const showWarning = (message, title = 'Warning') => {
    ElMessage.warning(message)
  }

  // Info message
  const showInfo = (message, title = 'Info') => {
    ElMessage.info(message)
  }

  // Success notification (more prominent)
  const notifySuccess = (message, title = 'Success') => {
    ElNotification.success({
      title,
      message,
      duration: 4000
    })
  }

  // Error notification (more prominent)
  const notifyError = (message, title = 'Error') => {
    ElNotification.error({
      title,
      message,
      duration: 6000
    })
  }

  // Warning notification
  const notifyWarning = (message, title = 'Warning') => {
    ElNotification.warning({
      title,
      message,
      duration: 5000
    })
  }

  // Info notification
  const notifyInfo = (message, title = 'Info') => {
    ElNotification.info({
      title,
      message,
      duration: 4000
    })
  }

  return {
    showSuccess,
    showError,
    showWarning,
    showInfo,
    notifySuccess,
    notifyError,
    notifyWarning,
    notifyInfo
  }
}