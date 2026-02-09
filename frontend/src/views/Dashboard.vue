<template>
  <div class="dashboard" aria-label="Learning dashboard">
    <!-- Dashboard Header with Welcome Message -->
    <header class="dashboard-header" role="banner">
      <div class="dashboard-header-content">
        <h1 class="dashboard-title" tabindex="0">Welcome Back!</h1>
        <p class="dashboard-subtitle" tabindex="0">Here's your personalized learning overview</p>
        <div class="dashboard-meta">
          <span class="dashboard-date" tabindex="0">{{ currentDate }}</span>
          <span class="dashboard-streak" v-if="learningStreak > 0" tabindex="0" aria-live="polite">
            📅 {{ learningStreak }}-day learning streak!
          </span>
        </div>
      </div>
      <div class="dashboard-actions" role="group" aria-label="Dashboard actions">
        <router-link to="/review" class="btn btn-primary" v-if="pendingReviews > 0" aria-label="Start review session">
          <span class="btn-icon">🔄</span>
          <span class="btn-text">Start Review</span>
        </router-link>
        <router-link to="/materials" class="btn btn-secondary" aria-label="Add new study material">
          <span class="btn-icon">📄</span>
          <span class="btn-text">Add Material</span>
        </router-link>
      </div>
    </header>
    
    <!-- Notification Alerts -->
    <div class="notification-alerts" role="alert" aria-live="assertive">
      <div 
        v-if="overdueCount > 0" 
        class="alert alert-overdue" 
        :class="{ 'alert-dismissed': !showOverdueAlert }"
        aria-atomic="true"
      >
        <div class="alert-icon">⚠️</div>
        <div class="alert-content">
          <h4 class="alert-title">{{ overdueCount }} Overdue Task{{ overdueCount > 1 ? 's' : '' }}</h4>
          <p class="alert-message">{{ overdueMessage }}</p>
        </div>
        <div class="alert-actions">
          <button class="alert-dismiss" @click="dismissOverdueAlert" aria-label="Dismiss overdue alert" tabindex="0">✕</button>
          <router-link to="/todo" class="btn btn-sm btn-primary" aria-label="View overdue tasks">View Tasks</router-link>
        </div>
      </div>
      
      <div 
        v-if="dueTodayCount > 0" 
        class="alert alert-warning" 
        :class="{ 'alert-dismissed': !showDueTodayAlert }"
        aria-atomic="true"
      >
        <div class="alert-icon">⏰</div>
        <div class="alert-content">
          <h4 class="alert-title">{{ dueTodayCount }} Task{{ dueTodayCount > 1 ? 's' : '' }} Due Today</h4>
          <p class="alert-message">{{ dueTodayMessage }}</p>
        </div>
        <div class="alert-actions">
          <button class="alert-dismiss" @click="dismissDueTodayAlert" aria-label="Dismiss due today alert" tabindex="0">✕</button>
          <router-link to="/todo" class="btn btn-sm btn-primary" aria-label="View tasks due today">View Tasks</router-link>
        </div>
      </div>
      
      <div 
        v-if="pendingReviews > 0" 
        class="alert alert-info" 
        :class="{ 'alert-dismissed': !showReviewAlert }"
        aria-atomic="true"
      >
        <div class="alert-icon">📝</div>
        <div class="alert-content">
          <h4 class="alert-title">{{ pendingReviews }} Review{{ pendingReviews > 1 ? 's' : '' }} Available</h4>
          <p class="alert-message">You have vocabulary reviews ready for practice</p>
        </div>
        <div class="alert-actions">
          <button class="alert-dismiss" @click="dismissReviewAlert" aria-label="Dismiss review alert" tabindex="0">✕</button>
          <router-link to="/review" class="btn btn-sm btn-primary" aria-label="Start review session">Start Review</router-link>
        </div>
      </div>
    </div>
    
    <!-- Key Metrics Grid -->
    <section class="dashboard-grid" aria-labelledby="metrics-heading">
      <h2 id="metrics-heading" class="sr-only">Learning Metrics</h2>
      <div class="dashboard-card metric-card" tabindex="0">
        <div class="metric-header">
          <div class="metric-icon materials-icon" aria-hidden="true">📄</div>
          <div class="metric-badge" v-if="newMaterials > 0" aria-live="polite">
            +{{ newMaterials }}
          </div>
        </div>
        <div class="metric-content">
          <h3 class="metric-title">Study Materials</h3>
          <p class="metric-value">{{ materialsCount }}</p>
          <p class="metric-desc">Total materials</p>
          <div class="metric-progress">
            <div class="progress-bar" role="progressbar" aria-valuenow="{{ materialsProgress }}" aria-valuemin="0" aria-valuemax="100">
              <div class="progress-fill" :style="{ width: materialsProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ materialsProgress }}% of weekly goal</span>
          </div>
        </div>
        <router-link to="/materials" class="metric-link" aria-label="Manage study materials">
          Manage Materials
          <span class="link-arrow" aria-hidden="true">→</span>
        </router-link>
      </div>
      
      <div class="dashboard-card metric-card" tabindex="0">
        <div class="metric-header">
          <div class="metric-icon vocabulary-icon" aria-hidden="true">📝</div>
          <div class="metric-badge" v-if="newCards > 0" aria-live="polite">
            +{{ newCards }}
          </div>
        </div>
        <div class="metric-content">
          <h3 class="metric-title">Vocabulary</h3>
          <p class="metric-value">{{ cardsCount }}</p>
          <p class="metric-desc">Words & phrases</p>
          <div class="metric-progress">
            <div class="progress-bar" role="progressbar" aria-valuenow="{{ vocabularyProgress }}" aria-valuemin="0" aria-valuemax="100">
              <div class="progress-fill" :style="{ width: vocabularyProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ vocabularyProgress }}% of weekly goal</span>
          </div>
        </div>
        <router-link to="/vocabulary" class="metric-link" aria-label="View vocabulary list">
          View Vocabulary
          <span class="link-arrow" aria-hidden="true">→</span>
        </router-link>
      </div>
      
      <div class="dashboard-card metric-card" tabindex="0">
        <div class="metric-header">
          <div class="metric-icon review-icon" aria-hidden="true">🔄</div>
          <div class="metric-badge urgent" v-if="pendingReviews > 0" aria-live="polite">
            {{ pendingReviews }}
          </div>
        </div>
        <div class="metric-content">
          <h3 class="metric-title">Reviews</h3>
          <p class="metric-value">{{ pendingReviews }}</p>
          <p class="metric-desc">Pending reviews</p>
          <div class="metric-progress">
            <div class="progress-bar" role="progressbar" aria-valuenow="{{ reviewProgress }}" aria-valuemin="0" aria-valuemax="100">
              <div class="progress-fill urgent" :style="{ width: reviewProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ reviewProgress }}% completed this week</span>
          </div>
        </div>
        <router-link to="/review" class="metric-link" aria-label="Start review session">
          Start Review
          <span class="link-arrow" aria-hidden="true">→</span>
        </router-link>
      </div>
      
      <div class="dashboard-card metric-card" tabindex="0">
        <div class="metric-header">
          <div class="metric-icon todo-icon" aria-hidden="true">✅</div>
          <div class="metric-badge" v-if="completedTasks > 0" aria-live="polite">
            +{{ completedTasks }}
          </div>
        </div>
        <div class="metric-content">
          <h3 class="metric-title">Tasks</h3>
          <p class="metric-value">{{ todoCount }}</p>
          <p class="metric-desc">Remaining tasks</p>
          <div class="metric-progress">
            <div class="progress-bar" role="progressbar" aria-valuenow="{{ taskProgress }}" aria-valuemin="0" aria-valuemax="100">
              <div class="progress-fill" :style="{ width: taskProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ taskProgress }}% completed this week</span>
          </div>
        </div>
        <router-link to="/todo" class="metric-link" aria-label="View task list">
          View Tasks
          <span class="link-arrow" aria-hidden="true">→</span>
        </router-link>
      </div>
    </section>
    
    <!-- Learning Insights Section -->
    <section class="learning-insights" aria-labelledby="insights-heading">
      <div class="section-header">
        <h2 id="insights-heading" class="section-title">Learning Insights</h2>
        <router-link to="/statistics" class="view-all-link" aria-label="View detailed analytics">View Detailed Analytics</router-link>
      </div>
      
      <div class="insights-grid">
        <!-- Learning Progress Chart -->
        <div class="insight-card chart-card" tabindex="0">
          <div class="insight-header">
            <h3>Weekly Learning Progress</h3>
            <div class="chart-legend">
              <span class="legend-item">
                <span class="legend-color" style="background-color: var(--primary-500)" aria-hidden="true"></span>
                <span class="legend-text">Vocabulary</span>
              </span>
              <span class="legend-item">
                <span class="legend-color" style="background-color: var(--secondary-500)" aria-hidden="true"></span>
                <span class="legend-text">Reviews</span>
              </span>
            </div>
          </div>
          <div class="chart-container" aria-label="Weekly learning progress chart">
            <div class="progress-chart">
              <div 
                v-for="(day, index) in weeklyProgress" 
                :key="index"
                class="chart-bar"
                aria-hidden="true"
              >
                <div class="bar-container">
                  <div 
                    class="bar vocabulary-bar"
                    :style="{ height: day.vocabulary + '%' }"
                    :title="`${day.vocabulary}% vocabulary progress`"
                  ></div>
                  <div 
                    class="bar review-bar"
                    :style="{ height: day.review + '%' }"
                    :title="`${day.review}% review progress`"
                  ></div>
                </div>
                <span class="bar-label">{{ day.label }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Study Recommendations -->
        <div class="insight-card recommendations-card" tabindex="0">
          <div class="insight-header">
            <h3>Personalized Recommendations</h3>
            <span class="recommendations-badge" aria-label="AI-powered recommendations">AI-Powered</span>
          </div>
          <div class="recommendations-list">
            <div 
              v-for="(recommendation, index) in recommendations" 
              :key="index"
              class="recommendation-item"
            >
              <div class="recommendation-icon" aria-hidden="true">{{ recommendation.icon }}</div>
              <div class="recommendation-content">
                <h4 class="recommendation-title">{{ recommendation.title }}</h4>
                <p class="recommendation-desc">{{ recommendation.description }}</p>
              </div>
              <button 
                class="recommendation-action"
                @click="executeRecommendation(recommendation.action)"
                :aria-label="recommendation.actionText + ': ' + recommendation.title"
                tabindex="0"
              >
                {{ recommendation.actionText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- Recent Activity -->
    <section class="recent-activity" aria-labelledby="activity-heading">
      <div class="section-header">
        <h2 id="activity-heading" class="section-title">Recent Activity</h2>
        <router-link to="/statistics" class="view-all-link" aria-label="View all activity">View All Activity</router-link>
      </div>
      <div class="activity-card">
        <div v-if="recentActivities.length === 0" class="empty-state" tabindex="0">
          <div class="empty-state-icon" aria-hidden="true">📊</div>
          <h4>No recent activity</h4>
          <p>Start learning to see your activity here</p>
          <router-link to="/materials" class="btn btn-primary" aria-label="Add new study material">
            <span class="btn-icon" aria-hidden="true">+</span>
            <span class="btn-text">Add Study Material</span>
          </router-link>
        </div>
        <div v-else class="activity-list">
          <div 
            v-for="activity in recentActivities" 
            :key="activity.id"
            class="activity-item"
            tabindex="0"
          >
            <div class="activity-icon-container">
              <div class="activity-icon" aria-hidden="true">{{ getActivityIcon(activity.type) }}</div>
            </div>
            <div class="activity-content">
              <div class="activity-header">
                <span class="activity-type">{{ activity.type }}</span>
                <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
              </div>
              <p class="activity-description">{{ activity.description }}</p>
            </div>
            <div class="activity-meta" v-if="activity.meta">
              <span class="activity-meta-item">{{ activity.meta }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useApiService } from '../composables/useApiService'
import { useMaterialService } from '../services/materialService'
import { useVocabularyService } from '../services/vocabularyService'
import { useReviewService } from '../services/reviewService'
import { useTodoService } from '../services/todoService'
import { useNotificationStore } from '../stores/notificationStore'

export default {
  name: 'Dashboard',
  setup() {
    // Core dashboard data
    const materialsCount = ref(0)
    const cardsCount = ref(0)
    const pendingReviews = ref(0)
    const todoCount = ref(0)
    const recentActivities = ref([])
    const todos = ref([])
    
    // New dashboard data
    const currentDate = ref('')
    const learningStreak = ref(0)
    const newMaterials = ref(0)
    const newCards = ref(0)
    const completedTasks = ref(0)
    
    // Progress data
    const materialsProgress = ref(0)
    const vocabularyProgress = ref(0)
    const reviewProgress = ref(0)
    const taskProgress = ref(0)
    
    // Chart data
    const weeklyProgress = ref([])
    
    // Recommendations
    const recommendations = ref([])
    
    // Alert dismissal states
    const showOverdueAlert = ref(true)
    const showDueTodayAlert = ref(true)
    const showReviewAlert = ref(true)
    
    const { apiService } = useApiService()
    const { getAllMaterials } = useMaterialService()
    const { getAllCards, getCardsDueForReview } = useVocabularyService()
    const { getReviewSessions } = useReviewService()
    const { getTodoItems } = useTodoService()
    const notificationStore = useNotificationStore()
    
    // Computed properties for alerts
    const overdueCount = computed(() => {
      if (!showOverdueAlert.value) return 0
      const today = new Date().toISOString().split('T')[0]
      return todos.value.filter(t => 
        !t.completed && t.dueDate && t.dueDate < today
      ).length
    })
    
    const dueTodayCount = computed(() => {
      if (!showDueTodayAlert.value) return 0
      const today = new Date().toISOString().split('T')[0]
      return todos.value.filter(t => 
        !t.completed && t.dueDate && t.dueDate === today
      ).length
    })
    
    const overdueMessage = computed(() => {
      const overdueTasks = todos.value.filter(t => {
        const today = new Date().toISOString().split('T')[0]
        return !t.completed && t.dueDate && t.dueDate < today
      })
      
      if (overdueTasks.length === 1) {
        return `"${overdueTasks[0].title}" is overdue`
      }
      return `You have ${overdueTasks.length} overdue tasks that need attention`
    })
    
    const dueTodayMessage = computed(() => {
      const dueTasks = todos.value.filter(t => {
        const today = new Date().toISOString().split('T')[0]
        return !t.completed && t.dueDate && t.dueDate === today
      })
      
      if (dueTasks.length === 1) {
        return `"${dueTasks[0].title}" is due today`
      }
      return `You have ${dueTasks.length} tasks due today`
    })
    
    // Initialize dashboard data
    const loadDashboardData = async () => {
      try {
        // Set current date
        currentDate.value = new Date().toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          weekday: 'long'
        })
        
        // Load todo items for alert calculations
        todos.value = await getTodoItems()
        todoCount.value = todos.value.filter(t => !t.completed).length
        completedTasks.value = todos.value.filter(t => t.completed).length
        
        // Load other dashboard statistics
        try {
          const materials = await getAllMaterials()
          materialsCount.value = materials.length
          // Simulate new materials count
          newMaterials.value = Math.floor(Math.random() * 3)
          // Calculate progress (simulated)
          materialsProgress.value = Math.min(100, Math.floor(Math.random() * 120))
        } catch (error) {
          console.log('Materials API not ready yet')
          materialsCount.value = 0
          materialsProgress.value = 0
        }
        
        try {
          const cards = await getAllCards()
          cardsCount.value = cards.length
          // Simulate new cards count
          newCards.value = Math.floor(Math.random() * 5)
          // Calculate progress (simulated)
          vocabularyProgress.value = Math.min(100, Math.floor(Math.random() * 120))
        } catch (error) {
          console.log('Cards API not ready yet')
          cardsCount.value = 0
          vocabularyProgress.value = 0
        }
        
        try {
          const dueCards = await getCardsDueForReview()
          pendingReviews.value = dueCards.length
          // Calculate progress (simulated)
          reviewProgress.value = Math.min(100, Math.floor(Math.random() * 120))
        } catch (error) {
          console.log('Reviews API not ready yet')
          pendingReviews.value = 0
          reviewProgress.value = 0
        }
        
        // Calculate task progress (simulated)
        taskProgress.value = Math.min(100, Math.floor(Math.random() * 120))
        
        // Simulate learning streak
        learningStreak.value = Math.floor(Math.random() * 15) + 1
        
        // Initialize weekly progress chart data
        initWeeklyProgress()
        
        // Initialize recommendations
        initRecommendations()
        
        // Load recent activities (placeholder data)
        loadRecentActivities()
        
      } catch (error) {
        console.error('Error loading dashboard data:', error)
      }
    }
    
    // Initialize weekly progress chart data
    const initWeeklyProgress = () => {
      const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      const progress = days.map(day => ({
        label: day,
        vocabulary: Math.floor(Math.random() * 100),
        review: Math.floor(Math.random() * 100)
      }))
      weeklyProgress.value = progress
    }
    
    // Initialize recommendations
    const initRecommendations = () => {
      recommendations.value = [
        {
          icon: '📚',
          title: 'Review Difficult Cards',
          description: 'Focus on cards you struggled with in previous sessions',
          action: 'review-difficult',
          actionText: 'Start Review'
        },
        {
          icon: '🎯',
          title: 'Set Weekly Goal',
          description: 'Define your vocabulary learning target for the week',
          action: 'set-goal',
          actionText: 'Set Goal'
        },
        {
          icon: '📈',
          title: 'Track Your Progress',
          description: 'View detailed analytics of your learning journey',
          action: 'view-analytics',
          actionText: 'View Analytics'
        }
      ]
    }
    
    // Load recent activities (placeholder data)
    const loadRecentActivities = () => {
      recentActivities.value = [
        {
          id: 1,
          type: 'Card Created',
          description: 'Added new vocabulary card: "perspicacious"',
          timestamp: new Date(Date.now() - 3600000).toISOString(),
          meta: 'Vocabulary'
        },
        {
          id: 2,
          type: 'Review Completed',
          description: 'Finished review session with 85% accuracy',
          timestamp: new Date(Date.now() - 7200000).toISOString(),
          meta: 'Reviews'
        },
        {
          id: 3,
          type: 'Material Uploaded',
          description: 'Uploaded new study material: "Business English Essentials"',
          timestamp: new Date(Date.now() - 86400000).toISOString(),
          meta: 'Materials'
        },
        {
          id: 4,
          type: 'Task Completed',
          description: 'Completed task: "Review 20 vocabulary cards"',
          timestamp: new Date(Date.now() - 172800000).toISOString(),
          meta: 'Tasks'
        }
      ]
    }
    
    // Execute recommendation action
    const executeRecommendation = (action) => {
      switch (action) {
        case 'review-difficult':
          window.location.href = '/review'
          break
        case 'set-goal':
          window.location.href = '/vocabulary'
          break
        case 'view-analytics':
          window.location.href = '/statistics'
          break
        default:
          break
      }
    }
    
    // Alert dismissal functions
    const dismissOverdueAlert = () => {
      showOverdueAlert.value = false
    }
    
    const dismissDueTodayAlert = () => {
      showDueTodayAlert.value = false
    }
    
    const dismissReviewAlert = () => {
      showReviewAlert.value = false
    }
    
    // Utility functions
    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleString()
    }
    
    const getActivityIcon = (type) => {
      const iconMap = {
        'Card Created': '📝',
        'Material Uploaded': '📄',
        'Review Completed': '✅',
        'Task Completed': '✔️',
        'Deck Created': '🃏',
        'Goal Achieved': '🎯',
        'Streak Extended': '🔥'
      }
      return iconMap[type] || '📊'
    }
    
    // Mounted hook
    onMounted(() => {
      loadDashboardData()
      // Initialize notification store
      notificationStore.initialize()
    })
    
    return {
      // Core data
      materialsCount,
      cardsCount,
      pendingReviews,
      todoCount,
      recentActivities,
      
      // New data
      currentDate,
      learningStreak,
      newMaterials,
      newCards,
      completedTasks,
      
      // Progress data
      materialsProgress,
      vocabularyProgress,
      reviewProgress,
      taskProgress,
      
      // Chart data
      weeklyProgress,
      
      // Recommendations
      recommendations,
      
      // Alert data
      overdueCount,
      dueTodayCount,
      overdueMessage,
      dueTodayMessage,
      showOverdueAlert,
      showDueTodayAlert,
      showReviewAlert,
      
      // Methods
      dismissOverdueAlert,
      dismissDueTodayAlert,
      dismissReviewAlert,
      executeRecommendation,
      formatTime,
      getActivityIcon
    }
  }
}
</script>

<style scoped>
/* Dashboard Container */
.dashboard {
  max-width: var(--container-2xl);
  margin: 0 auto;
  width: 100%;
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

/* Dashboard Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-8);
  padding: var(--space-6);
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--surface-border);
  gap: var(--space-6);
}

.dashboard-header-content {
  flex: 1;
}

.dashboard-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  letter-spacing: var(--tracking-tight);
  line-height: var(--leading-tight);
}

.dashboard-subtitle {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
  line-height: var(--leading-normal);
}

.dashboard-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.dashboard-date {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

.dashboard-streak {
  font-size: var(--text-sm);
  color: var(--secondary-600);
  font-weight: var(--font-semibold);
  background-color: var(--secondary-50);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
  border: 1px solid var(--secondary-200);
}

.dashboard-actions {
  display: flex;
  gap: var(--space-4);
  align-items: center;
  flex-shrink: 0;
  flex-wrap: wrap;
}

/* Alert Styles */
.notification-alerts {
  margin-bottom: var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.alert {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-xl);
  border: 1px solid var(--surface-border);
  background-color: var(--surface-primary);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  animation: slideIn var(--transition-normal) var(--transition-ease-out);
}

.alert:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.alert-overdue {
  border-left: 4px solid var(--error-500);
  background-color: var(--error-50);
}

.alert-warning {
  border-left: 4px solid var(--warning-500);
  background-color: var(--warning-50);
}

.alert-info {
  border-left: 4px solid var(--primary-500);
  background-color: var(--primary-50);
}

.alert-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.alert-message {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.alert-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.alert-dismiss {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast) var(--transition-ease-in-out);
}

.alert-dismiss:hover {
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
}

.alert-dismiss:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.alert-dismissed {
  animation: slideOut var(--transition-normal) var(--transition-ease-in) forwards;
}

/* Metric Cards Grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-10);
}

/* Metric Card Styles */
.dashboard-card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  border: 1px solid var(--surface-border);
  position: relative;
  overflow: hidden;
}

.dashboard-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
  border-color: var(--primary-200);
}

.dashboard-card:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
}

.metric-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.metric-badge {
  background-color: var(--accent-500);
  color: white;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  min-width: 24px;
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.metric-badge.urgent {
  background-color: var(--error-500);
}

.metric-content {
  flex: 1;
  margin-bottom: var(--space-4);
}

.metric-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.metric-value {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  line-height: var(--leading-none);
}

.metric-desc {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-bottom: var(--space-4);
}

.metric-progress {
  margin-bottom: var(--space-4);
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: var(--space-2);
}

.progress-fill {
  height: 100%;
  background-color: var(--primary-500);
  border-radius: var(--radius-full);
  transition: width var(--transition-slow) var(--transition-ease-out);
}

.progress-fill.urgent {
  background-color: var(--error-500);
}

.progress-text {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

.metric-link {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--primary-600);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  border: 1px solid var(--primary-100);
  background-color: var(--primary-50);
}

.metric-link:hover {
  background-color: var(--primary-100);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.metric-link:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.link-arrow {
  font-size: var(--text-xs);
  transition: transform var(--transition-fast) var(--transition-ease-in-out);
}

.metric-link:hover .link-arrow {
  transform: translateX(4px);
}

/* Learning Insights Section */
.learning-insights {
  margin-bottom: var(--space-10);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.section-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: var(--tracking-tight);
}

.view-all-link {
  color: var(--primary-600);
  text-decoration: none;
  font-weight: var(--font-medium);
  font-size: var(--text-sm);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--primary-100);
  background-color: var(--primary-50);
}

.view-all-link:hover {
  color: var(--primary-700);
  background-color: var(--primary-100);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.view-all-link:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--space-6);
}

.insight-card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--surface-border);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.insight-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary-200);
}

.insight-card:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.insight-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
}

.insight-header h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: var(--space-4);
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.chart-container {
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

.progress-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-2);
  width: 100%;
  height: 100%;
}

.chart-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 24px;
}

.bar-container {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 160px;
  width: 100%;
}

.bar {
  flex: 1;
  min-height: 4px;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  transition: height var(--transition-slow) var(--transition-ease-out);
}

.vocabulary-bar {
  background-color: var(--primary-500);
  width: 50%;
}

.review-bar {
  background-color: var(--secondary-500);
  width: 50%;
}

.bar-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
  margin-top: var(--space-2);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

/* Recommendations Card */
.recommendations-card {
  display: flex;
  flex-direction: column;
}

.recommendations-badge {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--accent-600);
  background-color: var(--accent-50);
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  border: 1px solid var(--accent-200);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  flex: 1;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-xl);
  background-color: var(--surface-secondary);
  border: 1px solid var(--surface-border);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.recommendation-item:hover {
  background-color: var(--surface-tertiary);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.recommendation-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.recommendation-content {
  flex: 1;
  min-width: 0;
}

.recommendation-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.recommendation-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  line-height: var(--leading-normal);
}

.recommendation-action {
  background-color: var(--primary-600);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.recommendation-action:hover {
  background-color: var(--primary-700);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.recommendation-action:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

/* Recent Activity Section */
.recent-activity {
  margin-bottom: var(--space-8);
}

.activity-card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--surface-border);
}

.empty-state {
  text-align: center;
  padding: var(--space-12);
  color: var(--text-secondary);
}

.empty-state:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.empty-state-icon {
  font-size: 4rem;
  margin-bottom: var(--space-4);
}

.empty-state h4 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.empty-state p {
  font-size: var(--text-base);
  color: var(--text-tertiary);
  margin-bottom: var(--space-6);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-xl);
  border: 1px solid var(--surface-border);
  background-color: var(--surface-secondary);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.activity-item:hover {
  background-color: var(--surface-tertiary);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.activity-item:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.activity-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background-color: var(--primary-50);
  border: 1px solid var(--primary-200);
  flex-shrink: 0;
}

.activity-icon {
  font-size: 1.25rem;
  color: var(--primary-600);
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}

.activity-type {
  font-weight: var(--font-semibold);
  color: var(--primary-600);
  font-size: var(--text-sm);
}

.activity-time {
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.activity-description {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
  margin: 0;
}

.activity-meta {
  flex-shrink: 0;
}

.activity-meta-item {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  background-color: var(--surface-tertiary);
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(-10px);
    height: 0;
    margin: 0;
    padding: 0;
    overflow: hidden;
  }
}

/* Responsive Design */
@media (max-width: 1200px) {
  .insights-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-4);
  }
  
  .dashboard-actions {
    justify-content: center;
  }
  
  .dashboard-title {
    font-size: var(--text-3xl);
  }
  
  .dashboard-subtitle {
    font-size: var(--text-base);
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  
  .dashboard-card {
    padding: var(--space-4);
  }
  
  .metric-value {
    font-size: var(--text-3xl);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
  
  .insights-grid {
    gap: var(--space-4);
  }
  
  .insight-card {
    padding: var(--space-4);
  }
  
  .chart-container {
    height: 180px;
  }
  
  .bar-container {
    height: 140px;
  }
  
  .recommendation-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .recommendation-action {
    align-self: flex-start;
  }
  
  .activity-item {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-3);
  }
  
  .activity-icon-container {
    align-self: flex-start;
  }
  
  .activity-meta {
    align-self: flex-start;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: var(--space-4);
  }
  
  .dashboard-title {
    font-size: var(--text-2xl);
  }
  
  .dashboard-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .dashboard-actions .btn {
    justify-content: center;
  }
  
  .alert {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
  
  .alert-actions {
    justify-content: center;
  }
  
  .dashboard-card {
    padding: var(--space-4);
  }
  
  .metric-icon {
    font-size: 1.5rem;
  }
  
  .metric-title {
    font-size: var(--text-sm);
  }
  
  .metric-value {
    font-size: var(--text-2xl);
  }
  
  .section-title {
    font-size: var(--text-xl);
  }
  
  .insight-card {
    padding: var(--space-4);
  }
  
  .chart-container {
    height: 160px;
  }
  
  .bar-container {
    height: 120px;
  }
  
  .empty-state {
    padding: var(--space-8);
  }
  
  .empty-state-icon {
    font-size: 3rem;
  }
}

/* Focus styles for accessibility */
*:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .dashboard-card {
    border: 2px solid var(--text-primary);
  }
  
  .alert {
    border: 2px solid var(--text-primary);
  }
  
  .activity-item {
    border: 2px solid var(--text-primary);
  }
}

/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
</style>