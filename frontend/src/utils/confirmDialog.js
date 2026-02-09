import { ElMessageBox } from 'element-plus'
import 'element-plus/es/components/message-box/style/css'

/**
 * Modern Confirm Dialog Utility
 * Provides consistent, user-friendly confirmation dialogs throughout the application
 */

/**
 * Show a confirmation dialog
 * @param {string} message - The confirmation message
 * @param {string} title - The dialog title
 * @param {Object} options - Additional options
 * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
 */
export async function confirm(message, title = 'Confirmation', options = {}) {
  try {
    await ElMessageBox.confirm(
      message,
      title,
      {
        confirmButtonText: options.confirmText || 'Confirm',
        cancelButtonText: options.cancelText || 'Cancel',
        type: options.type || 'warning',
        customClass: 'modern-confirm-box',
        center: true,
        closeOnClickModal: options.closeOnClickModal !== false,
        closeOnPressEscape: options.closeOnPressEscape !== false,
        distinguishCancelAndClose: true,
        ...options
      }
    )
    return true
  } catch (error) {
    // User cancelled or closed the dialog
    return false
  }
}

/**
 * Show a delete confirmation dialog
 * @param {string} itemName - The name of the item being deleted
 * @param {Object} options - Additional options
 * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
 */
export async function confirmDelete(itemName, options = {}) {
  return confirm(
    `Are you sure you want to delete ${itemName}? This action cannot be undone.`,
    'Confirm Delete',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'error',
      ...options
    }
  )
}

/**
 * Show a card delete confirmation dialog
 * @param {Object} card - The card being deleted
 * @param {Object} options - Additional options
 * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
 */
export async function confirmCardDelete(card, options = {}) {
  const cardText = card.text ? `"${card.text}"` : 'this card'
  return confirm(
    `Are you sure you want to delete the card ${cardText}? This action cannot be undone.`,
    'Confirm Delete',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'error',
      ...options
    }
  )
}

/**
 * Show a batch delete confirmation dialog
 * @param {number} count - The number of items being deleted
 * @param {string} itemType - The type of items being deleted
 * @param {Object} options - Additional options
 * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
 */
export async function confirmBatchDelete(count, itemType = 'items', options = {}) {
  return confirm(
    `Are you sure you want to delete ${count} ${itemType}? This action cannot be undone.`,
    'Confirm Batch Delete',
    {
      confirmButtonText: 'Delete All',
      cancelButtonText: 'Cancel',
      type: 'error',
      ...options
    }
  )
}

/**
 * Show an action confirmation dialog
 * @param {string} message - The confirmation message
 * @param {string} action - The action being confirmed
 * @param {Object} options - Additional options
 * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
 */
export async function confirmAction(message, action = 'proceed', options = {}) {
  return confirm(
    message,
    `Confirm ${action.charAt(0).toUpperCase() + action.slice(1)}`,
    {
      confirmButtonText: action.charAt(0).toUpperCase() + action.slice(1),
      cancelButtonText: 'Cancel',
      type: 'info',
      ...options
    }
  )
}

/**
 * Show a session end confirmation dialog
 * @param {Object} options - Additional options
 * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
 */
export async function confirmEndSession(options = {}) {
  return confirm(
    'Are you sure you want to end this session? Your progress will be saved.',
    'End Session',
    {
      confirmButtonText: 'End Session',
      cancelButtonText: 'Continue',
      type: 'info',
      ...options
    }
  )
}

// Export all functions as a single object
export default {
  confirm,
  confirmDelete,
  confirmCardDelete,
  confirmBatchDelete,
  confirmAction,
  confirmEndSession
}
