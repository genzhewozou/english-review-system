<template>
  <div class="loading-container" :class="{ 'loading-fullscreen': fullscreen }">
    <div class="loading-spinner" :class="size">
      <div class="spinner"></div>
      <div v-if="message" class="loading-message">{{ message }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoadingSpinner',
  props: {
    /**
     * Whether to show the spinner in fullscreen mode
     */
    fullscreen: {
      type: Boolean,
      default: false
    },
    /**
     * Size of the spinner
     * Options: small, medium, large
     */
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    /**
     * Loading message to display
     */
    message: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped>
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
}

.loading-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(2px);
  z-index: var(--z-modal);
  transition: opacity var(--transition-normal) var(--transition-ease-in-out);
}

.dark-mode .loading-fullscreen {
  background-color: rgba(15, 23, 42, 0.8);
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

/* Spinner animation */
.spinner {
  border: 3px solid var(--surface-border);
  border-top: 3px solid var(--primary-600);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

/* Size variations */
.loading-spinner.small .spinner {
  width: 24px;
  height: 24px;
  border-width: 2px;
}

.loading-spinner.medium .spinner {
  width: 32px;
  height: 32px;
  border-width: 3px;
}

.loading-spinner.large .spinner {
  width: 48px;
  height: 48px;
  border-width: 4px;
}

/* Loading message */
.loading-message {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  text-align: center;
  max-width: 200px;
  line-height: var(--leading-relaxed);
}

/* Animation */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Fade in animation */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-normal) var(--transition-ease-in-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
