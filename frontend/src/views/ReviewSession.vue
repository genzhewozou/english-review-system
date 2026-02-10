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
            <strong>{{ session.correctAnswers || 0 }}</strong>
            <span>Correct Answers</span>
          </div>
          <div class="stat-item">
            <strong>{{ session.totalQuestions || 0 }}</strong>
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
          <form @submit.prevent="addToTodoList" class="todo-form">
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
              <input v-model="todoForm.dueDate" type="date" class="form-control">
            </div>
            <div class="form-actions">
              <button type="button" @click="closeAddToTodoModal" class="btn btn-secondary">Cancel</button>
              <button type="submit" class="btn btn-primary" :disabled="addingToTodo">
                {{ addingToTodo ? 'Adding...' : 'Add to Todo List' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div v-else-if="session && !session.completed" class="active-session">
      <!-- Session Progress -->
      <ReviewProgress
        :current-question="Math.min(currentQuestionIndex + 1, session?.totalQuestions || 0)"
        :total-questions="session?.totalQuestions || 0"
        :correct-answers="session?.correctAnswers || 0"
        :session-start-time="sessionStartTime"
        :paused="sessionPaused"
        :answer-history="answerHistory"
        @pause-session="togglePause"
        @end-session="confirmEndSession"
      />

      <!-- Add to Todo List Button -->
      <div class="session-actions">
        <button @click="showAddToTodoModal = true" class="btn btn-secondary">Add to Todo List</button>
      </div>

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
      <div v-else-if="!session.completed" class="text-center">
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
import { confirmEndSession as confirmSessionEnd } from '../utils/confirmDialog'

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
        a && (a.quality === 'PERFECT' || a.quality === 'DIFFICULT')
      )
      // Group by material or category if available
      return [...new Set(correctAnswers.map(a => a && a.text ? a.text.substring(0, 20) + '...' : 'General'))]
    })

    const weakAreas = computed(() => {
      const incorrectAnswers = answerHistory.value.filter(a => 
        a && (a.quality === 'BLACKOUT' || a.quality === 'REMEMBERED')
      )
      return [...new Set(incorrectAnswers.map(a => a && a.text ? a.text.substring(0, 20) + '...' : 'General'))]
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
        if (!session.value) return
        
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
        id: q.cardId,
        text: q.text,
        backText: q.backText,
        context: q.context,
        userComment: q.userComment || null,
        position: q.questionNumber || q.position,
        total: q.totalQuestions || q.total,
        dueDate: q.dueDate,
        easeFactor: q.easeFactor,
        interval: q.interval
      }
    }

    const loadNextQuestion = async () => {
      try {
        if (!session.value) return
        
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
          id: data.cardId,
          text: data.text,
          backText: data.backText,
          context: data.context,
          userComment: data.userComment || null,
          position: data.questionNumber || data.position,
          total: data.totalQuestions || data.total,
          dueDate: data.dueDate,
          easeFactor: data.easeFactor,
          interval: data.interval
        }

        // Keep session totalQuestions in sync if backend provides it
        if (typeof data.totalQuestions === 'number') {
          session.value.totalQuestions = data.totalQuestions
        } else if (typeof data.total === 'number') {
          session.value.totalQuestions = data.total
        }
      } catch (error) {
        console.error('Failed to load next question:', error)
      }
    }

    const submitAnswer = async (quality) => {
      if (submittingAnswer.value || !session.value || !currentQuestion.value) return
      
      try {
        submittingAnswer.value = true
        
        console.log('Submitting answer:', {
          sessionId: session.value.id,
          cardId: currentQuestion.value.id,
          quality: quality
        })
        
        await apiService.post(`/reviews/sessions/${session.value.id}/answers`, {
          cardId: currentQuestion.value.id,
          quality: quality,
          responseTimeSeconds: null
        })

        // Record answer in history
        answerHistory.value.push({
          id: currentQuestion.value.id,
          text: currentQuestion.value.text,
          backText: currentQuestion.value.backText,
          context: currentQuestion.value.context,
          userComment: currentQuestion.value.userComment,
          quality: quality,
          timestamp: new Date()
        })

        // Update session stats
        session.value.totalQuestions = session.value.totalQuestions || 0
        if (quality === 'PERFECT' || quality === 'DIFFICULT') {
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
        console.error('Error response:', error.response)
      } finally {
        submittingAnswer.value = false
      }
    }

    const completeSession = async () => {
      try {
        if (!session.value) return
        
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

    const confirmEndSession = async () => {
      const confirmed = await confirmSessionEnd()
      if (confirmed) {
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
      
      // Set default form values based on session status
      if (session.value && session.value.completed) {
        todoForm.value = {
          title: `Review Session - ${new Date().toLocaleDateString()}`,
          description: `Review session completed with ${session.value.correctAnswers}/${session.value.totalQuestions || 0} correct answers (${calculateAccuracy()}% accuracy).`,
          dueDate: ''
        }
      } else if (session.value) {
        todoForm.value = {
          title: `Review Session - ${new Date().toLocaleDateString()}`,
          description: `Active review session in progress: ${currentQuestionIndex + 1}/${session.value.totalQuestions || 0} questions completed.`,
          dueDate: ''
        }
      } else {
        todoForm.value = {
          title: `Review Session - ${new Date().toLocaleDateString()}`,
          description: '',
          dueDate: ''
        }
      }
    }

    const addToTodoList = async () => {
      addingToTodo.value = true
      try {
        const todoData = {
          title: todoForm.value.title,
          description: todoForm.value.description,
          dueDate: todoForm.value.dueDate,
          type: 'REVIEW_SESSION',
          relatedSessionId: session.value?.id
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
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.question-container {
  margin-top: 2rem;
  animation: fadeIn 0.5s ease forwards;
}

.question-nav {
  margin-top: 1.25rem;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.question-nav .btn {
  transition: var(--transition);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.question-nav .btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.question-nav .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.paused-state {
  margin-top: 2rem;
  padding: 3rem 2rem;
  animation: fadeIn 0.5s ease forwards;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  color: white;
  text-align: center;
}

.paused-state h4 {
  margin: 0 0 1rem 0;
  color: white;
  font-size: 1.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.paused-state p {
  margin: 0 0 2rem 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
}

.paused-state .btn {
  background: white;
  color: #667eea;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.paused-state .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.completion-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 24px;
  margin: 32px 0;
  animation: fadeIn 0.5s ease forwards;
}

.stat-item {
  text-align: center;
  padding: 1.5rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.stat-item strong {
  display: block;
  font-size: 2.5rem;
  color: #667eea;
  margin-bottom: 8px;
  font-weight: 700;
}

.stat-item span {
  color: #666;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.performance-analysis {
  margin: 2rem 0;
  padding: 1.5rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.5s ease forwards;
}

.performance-analysis h4 {
  margin: 0 0 1.5rem 0;
  color: #333;
  text-align: center;
  font-size: 1.25rem;
  font-weight: 600;
}

.analysis-grid {
  display: grid;
  gap: 1rem;
}

.analysis-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #667eea;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.analysis-item:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  transform: translateX(2px);
}

.analysis-label {
  font-weight: 600;
  color: #333;
}

.analysis-value {
  color: #666;
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
  animation: fadeIn 0.5s ease forwards;
}

.completion-actions .btn {
  transition: var(--transition);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.completion-actions .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.completion-actions .btn-secondary {
  background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
}

.completion-actions .btn-secondary:hover {
  box-shadow: 0 6px 16px rgba(108, 117, 125, 0.4);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
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
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease forwards;
}

.modal {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px 20px 0 0;
  color: white;
}

.modal-header h3 {
  margin: 0;
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: white;
  transition: var(--transition);
  padding: 0.25rem;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: rgba(255, 255, 255, 0.8);
  background-color: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 1.5rem;
}

.todo-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #333;
  font-size: 0.9rem;
}

.form-control {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  background-color: #fff;
  transition: all 0.3s ease;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.form-actions .btn {
  transition: var(--transition);
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.form-actions .btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.form-actions .btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.form-actions .btn-secondary {
  background: #f8f9fa;
  color: #333;
  border: 2px solid #e2e8f0;
}

.session-actions {
  margin: 1.5rem 0;
  display: flex;
  justify-content: center;
}

.session-actions .btn {
  transition: var(--transition);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.session-actions .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* Responsive design */
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
  
  .completion-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-item {
    padding: 1rem;
  }
  
  .stat-item strong {
    font-size: 2rem;
  }
}

@media (max-width: 480px) {
  .completion-stats {
    grid-template-columns: 1fr;
  }
  
  .paused-state {
    padding: 2rem 1.5rem;
  }
  
  .question-nav {
    flex-direction: column;
    align-items: center;
  }
  
  .question-nav .btn {
    width: 100%;
    max-width: 200px;
  }
}

/* Animation for session completion */
.session-completed {
  animation: fadeIn 0.8s ease forwards;
}

.session-completed .card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  padding: 2rem;
  animation: slideIn 0.5s ease forwards;
}

.session-completed h3 {
  color: #667eea;
  font-size: 2rem;
  margin-bottom: 1.5rem;
  text-align: center;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Animation for question loading */
.question-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  animation: fadeIn 0.5s ease forwards;
}

.question-loading p {
  margin-top: 1rem;
  color: #666;
  font-size: 1.1rem;
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>