<template>
  <div class="modern-confirm-dialog">
    <!-- This component is used via the provided methods, not directly in templates -->
  </div>
</template>

<script>
import { ElMessageBox, ElMessage } from 'element-plus'
import 'element-plus/es/components/message-box/style/css'

/**
 * Modern Confirm Dialog
 * A modern, beautiful confirmation dialog component based on Element Plus
 * Provides consistent, user-friendly confirmation dialogs throughout the application
 */
export default {
  name: 'ModernConfirmDialog',
  methods: {
    /**
     * Show a confirmation dialog
     * @param {string} message - The confirmation message
     * @param {string} title - The dialog title
     * @param {Object} options - Additional options
     * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
     */
    async confirm(message, title = 'Confirmation', options = {}) {
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
    },

    /**
     * Show a delete confirmation dialog
     * @param {string} itemName - The name of the item being deleted
     * @param {Object} options - Additional options
     * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
     */
    async confirmDelete(itemName, options = {}) {
      return this.confirm(
        `Are you sure you want to delete ${itemName}? This action cannot be undone.`,
        'Confirm Delete',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'error',
          ...options
        }
      )
    },

    /**
     * Show a card delete confirmation dialog
     * @param {Object} card - The card being deleted
     * @param {Object} options - Additional options
     * @returns {Promise<boolean>} - Resolves to true if confirmed, false if cancelled
     */
    async confirmCardDelete(card, options = {}) {
      const cardText = card.text ? `