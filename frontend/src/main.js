import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'video.js/dist/video-js.css'
import 'quill/dist/quill.snow.css'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import './assets/common.css'

// Import global error handler
import { useErrorHandler } from './composables/useErrorHandler'

const app = createApp(App)

// Register Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// Global error handler with integration support
const { handleError } = useErrorHandler()

app.config.errorHandler = (err, vm, info) => {
  console.error('Global error:', err, info)
  
  // Handle different types of errors
  if (err.name === 'ChunkLoadError') {
    // Handle chunk loading errors (usually from code splitting)
    handleError(new Error('Application update detected. Please refresh the page.'), 'Application Loading')
  } else if (err.message?.includes('Network Error')) {
    // Handle network errors
    handleError(err, 'Network Connection')
  } else {
    // Handle other errors
    handleError(err, 'Application Error')
  }
}

// Global unhandled promise rejection handler
window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled promise rejection:', event.reason)
  handleError(event.reason, 'Unhandled Promise')
  event.preventDefault() // Prevent default browser error handling
})

// Performance monitoring
if (import.meta.env.DEV) {
  // Development-only performance monitoring
  const observer = new PerformanceObserver((list) => {
    for (const entry of list.getEntries()) {
      if (entry.entryType === 'navigation') {
        console.log('Navigation timing:', entry)
      } else if (entry.entryType === 'resource' && entry.duration > 1000) {
        console.warn('Slow resource load:', entry.name, entry.duration + 'ms')
      }
    }
  })
  
  observer.observe({ entryTypes: ['navigation', 'resource'] })
}

app.mount('#app')