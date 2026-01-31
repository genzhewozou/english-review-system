<template>
  <div class="dashboard">
    <h2>Dashboard</h2>
    
    <!-- Notification Alerts -->
    <div class="notification-alerts">
      <NotificationAlert
        v-if="overdueCount > 0"
        type="overdue"
        :title="`${overdueCount} Overdue Task${overdueCount > 1 ? 's' : ''}`"
        :message="overdueMessage"
        action-text="View Tasks"
        @action="$router.push('/todo')"
        @dismiss="dismissOverdueAlert"
      />
      
      <NotificationAlert
        v-if="dueTodayCount > 0"
        type="warning"
        :title="`${dueTodayCount} Task${dueTodayCount > 1 ? 's' : ''} Due Today`"
        :message="dueTodayMessage"
        action-text="View Tasks"
        @action="$router.push('/todo')"
        @dismiss="dismissDueTodayAlert"
      />
      
      <NotificationAlert
        v-if="pendingReviews > 0"
        type="info"
        :title="`${pendingReviews} Review${pendingReviews > 1 ? 's' : ''} Available`"
        message="You have vocabulary reviews ready for practice"
        action-text="Start Review"
        @action="$router.push('/review')"
        @dismiss="dismissReviewAlert"
      />
    </div>
    
    <div class="dashboard-grid">
      <div class="card">
        <h3>Study Materials</h3>
        <p>{{ materialsCount }} materials uploaded</p>
        <router-link to="/materials" class="btn">Manage Materials</router-link>
      </div>
      
      <div class="card">
        <h3>Vocabulary</h3>
        <p>{{ highlightsCount }} words/phrases highlighted</p>
        <router-link to="/vocabulary" class="btn">View Vocabulary</router-link>
      </div>
      
      <div class="card">
        <h3>Review Sessions</h3>
        <p>{{ pendingReviews }} reviews pending</p>
        <router-link to="/review" class="btn">Start Review</router-link>
      </div>
      
      <div class="card">
        <h3>Todo List</h3>
        <p>{{ todoCount }} tasks remaining</p>
        <router-link to="/todo" class="btn">View Tasks</router-link>
      </div>
    </div>
    
    <div class="recent-activity">
      <h3>Recent Activity</h3>
      <div class="card">
        <div v-if="recentActivities.length === 0" class="text-center">
          <p>No recent activity</p>
        </div>
        <div v-else>
          <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <span class="activity-type">{{ activity.type }}</span>
            <span class="activity-description">{{ activity.description }}</span>
            <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
          </div>
        </div>
      </div>
    </div>
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
import NotificationAlert from '../components/NotificationAlert.vue'

export default {
  name: 'Dashboard',
  components: {
    NotificationAlert
  },
  setup() {
    const materialsCount = ref(0)
    const highlightsCount = ref(0)
    const pendingReviews = ref(0)
    const todoCount = ref(0)
    const recentActivities = ref([])
    const todos = ref([])
    
    // Alert dismissal states
    const showOverdueAlert = ref(true)
    const showDueTodayAlert = ref(true)
    const showReviewAlert = ref(true)
    
    const { apiService } = useApiService()
    const { getAllMaterials } = useMaterialService()
    const { getAllHighlights, getHighlightsDueForReview } = useVocabularyService()
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
    
    const loadDashboardData = async () => {
      try {
        // Load todo items for alert calculations
        todos.value = await getTodoItems()
        todoCount.value = todos.value.filter(t => !t.completed).length
        
        // Load other dashboard statistics
        try {
          const materials = await getAllMaterials()
          materialsCount.value = materials.length
        } catch (error) {
          console.log('Materials API not ready yet')
          materialsCount.value = 0
        }
        
        try {
          const highlights = await getAllHighlights()
          highlightsCount.value = highlights.length
        } catch (error) {
          console.log('Highlights API not ready yet')
          highlightsCount.value = 0
        }
        
        try {
          const dueHighlights = await getHighlightsDueForReview()
          pendingReviews.value = dueHighlights.length
        } catch (error) {
          console.log('Reviews API not ready yet')
          pendingReviews.value = 0
        }
        
        // Load recent activities (placeholder for now)
        recentActivities.value = []
        
      } catch (error) {
        console.error('Error loading dashboard data:', error)
      }
    }
    
    const dismissOverdueAlert = () => {
      showOverdueAlert.value = false
    }
    
    const dismissDueTodayAlert = () => {
      showDueTodayAlert.value = false
    }
    
    const dismissReviewAlert = () => {
      showReviewAlert.value = false
    }
    
    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleString()
    }
    
    onMounted(() => {
      loadDashboardData()
      // Initialize notification store
      notificationStore.initialize()
    })
    
    return {
      materialsCount,
      highlightsCount,
      pendingReviews,
      todoCount,
      recentActivities,
      overdueCount,
      dueTodayCount,
      overdueMessage,
      dueTodayMessage,
      dismissOverdueAlert,
      dismissDueTodayAlert,
      dismissReviewAlert,
      formatTime
    }
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard h2 {
  margin-bottom: 2rem;
  color: #2c3e50;
}

.notification-alerts {
  margin-bottom: 2rem;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.dashboard-grid .card {
  text-align: center;
}

.dashboard-grid .card h3 {
  color: #34495e;
  margin-bottom: 1rem;
}

.dashboard-grid .card p {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: #7f8c8d;
}

.recent-activity h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-type {
  font-weight: 600;
  color: #007bff;
  min-width: 120px;
}

.activity-description {
  flex: 1;
  margin: 0 1rem;
}

.activity-time {
  color: #6c757d;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .activity-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .activity-type {
    min-width: auto;
  }
}
</style>