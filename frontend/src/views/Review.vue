<template>
  <div class="review">
    <h2>Review Sessions</h2>
    
    <div class="review-start">
      <div class="card text-center">
        <h3>Start a New Review Session</h3>
        <p>Review your highlighted vocabulary using spaced repetition.</p>
        
        <div class="review-stats mb-3">
          <div class="stat-item">
            <strong>{{ pendingReviews }}</strong>
            <span>Due for Review</span>
          </div>
          <div class="stat-item">
            <strong>{{ totalHighlights }}</strong>
            <span>Total Vocabulary</span>
          </div>
        </div>
        
        <!-- Optional: select specific highlights for this session -->
        <div v-if="highlights.length" class="highlight-selection mt-3">
          <h4 class="selection-title">Optional: choose highlights for this session</h4>
          <p class="selection-help">
            If you select highlights below, the session will only use these.  
            If you leave all unchecked, the system will choose based on review schedule.
          </p>
          <div class="selection-list">
            <label
              v-for="h in highlights"
              :key="h.id"
              class="selection-item"
            >
              <input
                type="checkbox"
                :value="h.id"
                v-model="selectedHighlightIds"
              />
              <span class="selection-text">{{ h.text }}</span>
            </label>
          </div>
        </div>

        <button 
          @click="startReviewSession" 
          :disabled="starting"
          class="btn mt-3"
        >
          {{ starting ? 'Starting...' : (selectedHighlightIds.length ? 'Start Selected Review Session' : 'Start Review Session') }}
        </button>
        
        <p v-if="totalHighlights === 0" class="mt-3">
          No vocabulary highlights found. Please add some highlights first!
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useApiService } from '../composables/useApiService'

export default {
  name: 'Review',
  setup() {
    const router = useRouter()
    const pendingReviews = ref(0)
    const totalHighlights = ref(0)
    const starting = ref(false)
    const highlights = ref([])
    const selectedHighlightIds = ref([])
    
    const { apiService } = useApiService()
    
    const loadReviewData = async () => {
      try {
        // Load pending reviews count
        const highlightsResponse = await apiService.get('/vocabulary')
        const allHighlights = highlightsResponse.data || []
        highlights.value = allHighlights
        totalHighlights.value = allHighlights.length

        // For now, just set pending reviews to total highlights to enable the button
        pendingReviews.value = totalHighlights.value
      } catch (error) {
        console.error('Error loading review data:', error)
      }
    }
    
    const startReviewSession = async () => {
      starting.value = true
      try {
        let response
        if (selectedHighlightIds.value.length > 0) {
          response = await apiService.post('/reviews/sessions/custom', selectedHighlightIds.value)
        } else {
          response = await apiService.post('/reviews/sessions')
        }
        const session = response.data

        // Navigate to the active review session
        router.push(`/review/${session.id}`)
      } catch (error) {
        console.error('Error starting review session:', error)
        alert('Error starting review session. Please try again.')
      } finally {
        starting.value = false
      }
    }
    
    onMounted(() => {
      console.log('Debug: Component mounted, calling loadReviewData')
      loadReviewData()
    })
    
    return {
      pendingReviews,
      totalHighlights,
      starting,
      highlights,
      selectedHighlightIds,
      startReviewSession,
    }
  }
}
</script>

<style scoped>
.review-start {
  max-width: 600px;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.text-center {
  text-align: center;
}

.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  min-width: 200px;
}

.btn:hover:not(:disabled) {
  background-color: #0056b3;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.btn:active {
  transform: translateY(0);
}

.review-stats {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin: 1.5rem 0;
}

.stat-item {
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 2rem;
  color: #007bff;
  margin-bottom: 0.25rem;
}

.stat-item span {
  color: #6c757d;
  font-size: 0.9rem;
}

.recent-sessions {
  margin-top: 2rem;
}

.recent-sessions h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.session-card {
  transition: transform 0.2s;
}

.session-card:hover {
  transform: translateY(-1px);
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.session-header h4 {
  margin: 0;
  color: #2c3e50;
}

.session-date {
  color: #6c757d;
  font-size: 0.9rem;
}

.session-stats {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.stat {
  background-color: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #495057;
}

.mt-3 {
  margin-top: 1rem;
}

.mb-3 {
  margin-bottom: 1rem;
}

.highlight-selection {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.selection-title {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.selection-help {
  margin: 0 0 0.75rem 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.selection-list {
  max-height: 220px;
  overflow: auto;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 0.75rem;
  background: #fafbfc;
}

.selection-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0;
  cursor: pointer;
}

.selection-text {
  color: #2c3e50;
  word-break: break-word;
}

@media (max-width: 768px) {
  .review-stats {
    flex-direction: column;
    gap: 1rem;
  }
  
  .session-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .session-stats {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .btn {
    width: 100%;
  }
}
</style>