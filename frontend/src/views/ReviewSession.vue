<template>
  <div class="review-session">
    <div v-if="loading" class="text-center">
      <div class="spinner"></div>
    </div>
    
    <div v-else-if="!session" class="card text-center">
      <h3>Session Not Found</h3>
      <p>The review session could not be found or has expired.</p>
      <router-link to="/review" class="btn">Back to Review</router-link>
    </div>
    
    <div v-else-if="session.completed" class="session-completed">
      <div class="card text-center">
        <h3>🎉 Session Complete!</h3>
        <div class="completion-stats">
          <div class="stat-item">
            <strong>{{ session.correctAnswers }}</strong>
            <span>Correct Answers</span>
          </div>
          <div class="stat-item">
            <strong>{{ session.totalQuestions }}</strong>
            <span>Total Questions</span>
          </div>
          <div class="stat-item">
            <strong>{{ calculateAccuracy() }}%</strong>
            <span>Accuracy</span>
          </div>
          <div class="stat-item">
            <strong>{{ formatDuration() }}</strong>
            <span>Duration</span>
          </div>
        </div>
        
        <!-- Performance Analysis -->
        <div class="performance-analysis">
          <h4>Performance Analysis</h4>
          <div class="analysis-grid">
            <div class="analysis-item">
              <span class="analysis-label">Strong Areas:</span>
              <span class="analysis-value">{{ strongAreas.join(', ') || 'None identified' }}</span>
            </div>
            <div class="analysis-item">
              <span class="analysis-label">Needs Review:</span>
              <span class="analysis-value">{{ weakAreas.join(', ') || 'None identified' }}</span>
            </div>
          </div>
        </div>
        
        <div class="completion-actions">
          <button @click="showAddToTodoModal = true" class="btn">Add to Todo List</button>
          <router-link to="/review" class="btn btn-secondary">Start Another Session</router-link>
          <router-link to="/vocabulary" class="btn btn-secondary">View Vocabulary</router-link>
        </div>
      </div>
    </div>

    <!-- Add to Todo List Modal -->
    <div v-if="showAddToTodoModal" class="modal-overlay" @click="closeAddToTodoModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Add Review Session to Todo List</h3>
          <button @click="closeAddToTodoModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="addToTodoList">
            <div class="form-group">
              <label class="form-label">Title</label>
              <input v-model="todoForm.title" type="text" class="form-control" required>
            </div>
            <div class="form-group">
              <label class="form-label">Description</label>
              <textarea v-model="todoForm.description" class="form-control" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Due Date</label>
              <input v-model="todoForm.dueDate" type="date" class="form-control" required>
            </div>
            <div class="form-group">
              <button type="submit" class="btn" :disabled="addingToTodo">
                {{ addingToTodo ? 'Adding...' : 'Add to Todo List' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div v-else class="active-session">
      <!-- Session Progress -->
      <ReviewProgress
        :current-question="currentQuestionIndex + 1"
        :total-questions="session.totalQuestions"
        :correct-answers="session.correctAnswers"
        :session-start-time="sessionStartTime"
        :paused="sessionPaused"
        :answer-history="answerHistory"
        @pause-session="togglePause"
        @end-session="confirmEndSession"
      />

      <!-- Current Question -->
      <div v-if="currentQuestion && !sessionPaused" class="question-container">
        <ReviewQuestion
          :question="currentQuestion"
          :submitting="submittingAnswer"
          @answer-selected="submitAnswer"
        />

        <!-- Navigation (does not submit answers) -->
        <div v-if="questions.length" class="question-nav">
          <button class="btn btn-secondary" @click="goPrev" :disabled="currentQuestionIndex <= 0">
            Previous
          </button>
          <button class="btn btn-secondary" @click="goNext" :disabled="currentQuestionIndex >= questions.length - 1">
            Next
          </button>
        </div>
      </div>

      <!-- Paused State -->
      <div v-else-if="sessionPaused" class="paused-state card text-center">
        <h4>Session Paused</h4>
        <p>Take your time. Click resume when you're ready to continue.</p>
        <button @click="togglePause" class="btn">Resume Session</button>
      </div>

      <!-- Loading next question -->
      <div v-else class="text-center">
        <div class="spinner"></div>
        <p>Loading next question...</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useApiService } from '@/composables/useApiService'
import ReviewProgress from '@/components/ReviewProgress.vue'
import ReviewQuestion from '@/components/ReviewQuestion.vue'

export default {
  name: 'ReviewSession',
  components: {
    ReviewProgress,
    ReviewQuestion
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const { apiService } = useApiService()

    // Reactive state
    const loading = ref(true)
    const session = ref(null)
    const currentQuestion = ref(null)
    const currentQuestionIndex = ref(0)
    const questions = ref([])
    const submittingAnswer = ref(false)
    const sessionStartTime = ref(null)
    const sessionPaused = ref(false)
    const answerHistory = ref([])
    const showAddToTodoModal = ref(false)
    const addingToTodo = ref(false)
    const todoForm = ref({
      title: '',
      description: '',
      dueDate: ''
    })

    // Computed properties
    const strongAreas = computed(() => {
      const correctAnswers = answerHistory.value.filter(a => 
        a.quality === 'PERFECT' || a.quality === 'CORRECT'
      )
      // Group by material or category if available
      return [...new Set(correctAnswers.map(a => a.highlight?.material?.title || 'General'))]
    })

    const weakAreas = computed(() => {
      const incorrectAnswers = answerHistory.value.filter(a => 
        a.quality === 'INCORRECT' || a.quality === 'BLACKOUT'
      )
      return [...new Set(incorrectAnswers.map(a => a.highlight?.material?.title || 'General'))]
    })

    // Methods
    const loadSession = async () => {
      try {
        loading.value = true
        const sessionId = route.params.sessionId
        const response = await apiService.get(`/reviews/sessions/${sessionId}`)
        session.value = response.data
        
        if (!session.value.completed) {
          sessionStartTime.value = new Date()
          await loadQuestions()
        }
      } catch (error) {
        console.error('Failed to load session:', error)
        session.value = null
      } finally {
        loading.value = false
      }
    }

    const loadQuestions = async () => {
      try {
        const response = await apiService.get(`/reviews/sessions/${session.value.id}/questions`)
        const list = response.data || []
        questions.value = list
        session.value.totalQuestions = list.length

        if (list.length === 0) {
          await completeSession()
          currentQuestion.value = null
          return
        }

        currentQuestionIndex.value = 0
        setCurrentQuestionFromIndex()
      } catch (error) {
        console.error('Failed to load questions list, falling back to next-question:', error)
        // Fallback to old behaviour
        await loadNextQuestion()
      }
    }

    const setCurrentQuestionFromIndex = () => {
      const q = questions.value[currentQuestionIndex.value]
      if (!q) {
        currentQuestion.value = null
        return
      }
      currentQuestion.value = {
        highlight: {
          id: q.highlightId,
          text: q.text,
          context: q.context,
          userComment: q.userComment || null
        },
        questionNumber: q.questionNumber,
        totalQuestions: q.totalQuestions
      }
    }

    const loadNextQuestion = async () => {
      try {
        const response = await apiService.get(`/reviews/sessions/${session.value.id}/next-question`)
        const data = response.data

        // If no question returned (e.g. HTTP 204 or empty body), complete the session
        if (!data) {
          await completeSession()
          currentQuestion.value = null
          return
        }

        // Map flat QuestionResultDto into the structure expected by ReviewQuestion
        currentQuestion.value = {
          highlight: {
            id: data.highlightId,
            text: data.text,
            context: data.context,
            userComment: data.userComment || null
          },
          questionNumber: data.questionNumber,
          totalQuestions: data.totalQuestions
        }

        // Keep session totalQuestions in sync if backend provides it
        if (typeof data.totalQuestions === 'number') {
          session.value.totalQuestions = data.totalQuestions
        }
      } catch (error) {
        console.error('Failed to load next question:', error)
      }
    }

    const submitAnswer = async (quality) => {
      if (submittingAnswer.value) return
      
      try {
        submittingAnswer.value = true
        
        await apiService.post(`/reviews/sessions/${session.value.id}/answers`, {
          highlightId: currentQuestion.value.highlight.id,
          quality: quality
        })

        // Record answer in history
        answerHistory.value.push({
          highlight: currentQuestion.value.highlight,
          quality: quality,
          timestamp: new Date()
        })

        // Update session stats
        session.value.totalQuestions = session.value.totalQuestions || 0
        if (quality === 'PASS' || quality === 'PERFECT' || quality === 'CORRECT') {
          session.value.correctAnswers = (session.value.correctAnswers || 0) + 1
        }

        currentQuestionIndex.value++
        
        // Auto-advance to next item if we have a list; otherwise fall back
        if (questions.value.length > 0) {
          if (currentQuestionIndex.value >= questions.value.length) {
            await completeSession()
            currentQuestion.value = null
          } else {
            setCurrentQuestionFromIndex()
          }
        } else {
          await loadNextQuestion()
        }
        
      } catch (error) {
        console.error('Failed to submit answer:', error)
      } finally {
        submittingAnswer.value = false
      }
    }

    const completeSession = async () => {
      try {
        await apiService.post(`/reviews/sessions/${session.value.id}/complete`)
        session.value.completed = true
        session.value.endTime = new Date()
      } catch (error) {
        console.error('Failed to complete session:', error)
      }
    }

    const togglePause = () => {
      sessionPaused.value = !sessionPaused.value
    }

    const goPrev = () => {
      if (questions.value.length === 0) return
      if (currentQuestionIndex.value <= 0) return
      currentQuestionIndex.value -= 1
      setCurrentQuestionFromIndex()
    }

    const goNext = () => {
      if (questions.value.length === 0) return
      if (currentQuestionIndex.value >= questions.value.length - 1) return
      currentQuestionIndex.value += 1
      setCurrentQuestionFromIndex()
    }

    const confirmEndSession = () => {
      if (confirm('Are you sure you want to end this session? Your progress will be saved.')) {
        completeSession()
      }
    }

    const calculateAccuracy = () => {
      if (!session.value || session.value.totalQuestions === 0) return 0
      return Math.round((session.value.correctAnswers / session.value.totalQuestions) * 100)
    }

    const formatDuration = () => {
      if (!session.value || !sessionStartTime.value) return '0m'
      
      const endTime = session.value.endTime || new Date()
      const duration = Math.floor((endTime - sessionStartTime.value) / 1000 / 60)
      
      if (duration < 60) {
        return `${duration}m`
      } else {
        const hours = Math.floor(duration / 60)
        const minutes = duration % 60
        return `${hours}h ${minutes}m`
      }
    }

    const closeAddToTodoModal = () => {
      showAddToTodoModal.value = false
      todoForm.value = {
        title: `Review Session - ${new Date().toLocaleDateString()}`,
        description: `Review session completed with ${session.value.correctAnswers}/${session.value.totalQuestions} correct answers (${calculateAccuracy()}% accuracy).`,
        dueDate: ''
      }
    }

    const addToTodoList = async () => {
      addingToTodo.value = true
      try {
        const todoData = {
          title: todoForm.value.title,
          description: todoForm.value.description,
          dueDate: todoForm.value.dueDate,
          type: 'REVIEW_SESSION'
        }
        
        await apiService.post('/todos', todoData)
        closeAddToTodoModal()
        alert('Review session added to todo list successfully!')
      } catch (error) {
        console.error('Error adding to todo list:', error)
        alert('Failed to add review session to todo list. Please try again.')
      } finally {
        addingToTodo.value = false
      }
    }

    // Lifecycle
    onMounted(() => {
      loadSession()
    })

    return {
      loading,
      session,
      currentQuestion,
      currentQuestionIndex,
      questions,
      submittingAnswer,
      sessionStartTime,
      sessionPaused,
      answerHistory,
      showAddToTodoModal,
      addingToTodo,
      todoForm,
      strongAreas,
      weakAreas,
      submitAnswer,
      togglePause,
      goPrev,
      goNext,
      confirmEndSession,
      calculateAccuracy,
      formatDuration,
      closeAddToTodoModal,
      addToTodoList
    }
  }
}
</script>

<style scoped>
.review-session {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.question-container {
  margin-top: 2rem;
}

.question-nav {
  margin-top: 1.25rem;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.paused-state {
  margin-top: 2rem;
  padding: 3rem 2rem;
}

.paused-state h4 {
  margin: 0 0 1rem 0;
  color: #6c757d;
}

.paused-state p {
  margin: 0 0 2rem 0;
  color: #6c757d;
}

.completion-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 24px;
  margin: 32px 0;
}

.stat-item {
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 32px;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-item span {
  color: #666;
  font-size: 14px;
}

.performance-analysis {
  margin: 2rem 0;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.performance-analysis h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  text-align: center;
}

.analysis-grid {
  display: grid;
  gap: 1rem;
}

.analysis-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: white;
  border-radius: 6px;
  border-left: 4px solid #007bff;
}

.analysis-label {
  font-weight: 600;
  color: #2c3e50;
}

.analysis-value {
  color: #6c757d;
  text-align: right;
  flex: 1;
  margin-left: 1rem;
}

.completion-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 2rem;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 1rem;
  background-color: #fff;
}

.form-control:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

@media (max-width: 768px) {
  .review-session {
    padding: 16px;
  }
  
  .completion-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .completion-actions .btn {
    width: 100%;
    max-width: 300px;
  }
  
  .analysis-item {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
  
  .analysis-value {
    text-align: center;
    margin-left: 0;
  }
}
</style>