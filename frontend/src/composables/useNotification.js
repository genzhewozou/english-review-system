/**
 * Composable for handling notifications and messages
 */
export function useNotification() {
  // Create toast element
  const createToast = (message, type, duration = 3000) => {
    const toast = document.createElement('div')
    toast.className = `toast toast-${type}`
    toast.textContent = message
    
    // Add to DOM
    document.body.appendChild(toast)
    
    // Trigger animation
    setTimeout(() => {
      toast.classList.add('toast-visible')
    }, 10)
    
    // Remove after duration
    setTimeout(() => {
      toast.classList.remove('toast-visible')
      setTimeout(() => {
        document.body.removeChild(toast)
      }, 300)
    }, duration)
    
    return toast
  }

  // Success message
  const showSuccess = (message, title = 'Success') => {
    createToast(message, 'success', 3000)
  }

  // Error message
  const showError = (message, title = 'Error') => {
    createToast(message, 'error', 4000)
  }

  // Warning message
  const showWarning = (message, title = 'Warning') => {
    createToast(message, 'warning', 3500)
  }

  // Info message
  const showInfo = (message, title = 'Info') => {
    createToast(message, 'info', 3000)
  }

  // Success notification (more prominent)
  const notifySuccess = (message, title = 'Success') => {
    createNotification(title, message, 'success', 4000)
  }

  // Error notification (more prominent)
  const notifyError = (message, title = 'Error') => {
    createNotification(title, message, 'error', 6000)
  }

  // Warning notification
  const notifyWarning = (message, title = 'Warning') => {
    createNotification(title, message, 'warning', 5000)
  }

  // Info notification
  const notifyInfo = (message, title = 'Info') => {
    createNotification(title, message, 'info', 4000)
  }

  // Create notification element
  const createNotification = (title, message, type, duration = 4000) => {
    const notification = document.createElement('div')
    notification.className = `notification notification-${type}`
    
    notification.innerHTML = `
      <div class="notification-title">${title}</div>
      <div class="notification-message">${message}</div>
      <button class="notification-close">×</button>
    `
    
    // Add close button functionality
    const closeButton = notification.querySelector('.notification-close')
    closeButton.addEventListener('click', () => {
      notification.classList.remove('notification-visible')
      setTimeout(() => {
        document.body.removeChild(notification)
      }, 300)
    })
    
    // Add to DOM
    document.body.appendChild(notification)
    
    // Trigger animation
    setTimeout(() => {
      notification.classList.add('notification-visible')
    }, 10)
    
    // Remove after duration
    setTimeout(() => {
      notification.classList.remove('notification-visible')
      setTimeout(() => {
        if (document.body.contains(notification)) {
          document.body.removeChild(notification)
        }
      }, 300)
    }, duration)
    
    return notification
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

// Add styles for toasts and notifications
if (!document.getElementById('notification-styles')) {
  const style = document.createElement('style')
  style.id = 'notification-styles'
  style.textContent = `
    /* Toast styles */
    .toast {
      position: fixed;
      top: 20px;
      right: 20px;
      padding: 12px 20px;
      border-radius: var(--radius-xl);
      color: white;
      font-weight: var(--font-medium);
      font-size: var(--text-sm);
      box-shadow: var(--shadow-lg);
      z-index: var(--z-toast);
      transform: translateX(100%);
      opacity: 0;
      transition: all var(--transition-normal) var(--transition-ease-out);
      max-width: 320px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .toast-visible {
      transform: translateX(0);
      opacity: 1;
    }

    .toast-success {
      background-color: var(--success-600);
    }

    .toast-error {
      background-color: var(--error-600);
    }

    .toast-warning {
      background-color: var(--warning-600);
    }

    .toast-info {
      background-color: var(--info-600);
    }

    /* Notification styles */
    .notification {
      position: fixed;
      top: 80px;
      right: 20px;
      width: 360px;
      background-color: var(--surface-primary);
      border: 1px solid var(--surface-border);
      border-radius: var(--radius-xl);
      box-shadow: var(--shadow-lg);
      padding: 16px;
      z-index: var(--z-notification);
      transform: translateX(100%);
      opacity: 0;
      transition: all var(--transition-normal) var(--transition-ease-out);
      overflow: hidden;
    }

    .notification-visible {
      transform: translateX(0);
      opacity: 1;
    }

    .notification-title {
      font-weight: var(--font-semibold);
      font-size: var(--text-sm);
      margin-bottom: 8px;
      color: var(--text-primary);
    }

    .notification-message {
      font-size: var(--text-sm);
      color: var(--text-secondary);
      margin-bottom: 12px;
      line-height: var(--leading-relaxed);
    }

    .notification-close {
      position: absolute;
      top: 12px;
      right: 12px;
      background: none;
      border: none;
      font-size: 18px;
      cursor: pointer;
      color: var(--text-light);
      padding: 0;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--radius-full);
      transition: all var(--transition-fast) var(--transition-ease-in-out);
    }

    .notification-close:hover {
      background-color: var(--bg-tertiary);
      color: var(--text-primary);
    }

    .notification-success {
      border-left: 4px solid var(--success-500);
    }

    .notification-error {
      border-left: 4px solid var(--error-500);
    }

    .notification-warning {
      border-left: 4px solid var(--warning-500);
    }

    .notification-info {
      border-left: 4px solid var(--info-500);
    }

    /* Mobile responsiveness */
    @media (max-width: 768px) {
      .toast {
        right: 10px;
        left: 10px;
        max-width: none;
        white-space: normal;
        text-align: center;
      }

      .notification {
        right: 10px;
        left: 10px;
        width: auto;
        max-width: 90%;
      }
    }
  `
  document.head.appendChild(style)
}
