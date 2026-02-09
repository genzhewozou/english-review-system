<template>
  <div class="review-progress">
    <!-- Session Info -->
    <div class="session-info">
      <div class="session-title">
        <h3>Review Session</h3>
        <div class="session-stats">
          <span class="stat-item">
            <strong>{{ currentQuestion }}</strong> of <strong>{{ totalQuestions }}</strong>
          </span>
          <span class="stat-separator">•</span>
          <span class="stat-item">
            <strong>{{ correctAnswers }}</strong> correct
          </span>
          <span class="stat-separator">•</span>
          <span class="stat-item">
            {{ accuracy }}% accuracy
          </span>
        </div>
      </div>
      
      <div class="session-actions">
        <button 
          @click="$emit('pause-session')" 
          class="btn btn-outline"
          :disabled="paused"
        >
          {{ paused ? 'Paused' : 'Pause' }}
        </button>
        <button 
          @click="$emit('end-session')" 
          class="btn btn-danger"
        >
          End Session
        </button>
      </div>
    </div>

    <!-- Progress Bar -->
    <div class="progress-container">
      <div class="progress-bar">
        <div 
          class="progress-fill" 
          :style="{ width: progressPercentage + '%' }"
        ></div>
        <div class="progress-markers">
          <div 
            v-for="(marker, index) in progressMarkers" 
            :key="index"
            :class="['progress-marker', marker.class]"
            :style="{ left: marker.position + '%' }"
            :title="marker.tooltip"
          ></div>
        </div>
      </div>
      <div class="progress-labels">
        <span class="progress-start">Start</span>
        <span class="progress-percentage">{{ Math.round(progressPercentage) }}%</span>
        <span class="progress-end">Complete</span>
      </div>
    </div>

    <!-- Time Tracking -->
    <div class="time-info">
      <div class="time-item">
        <span class="time-label">Session Time:</span>
        <span class="time-value">{{ formatTime(sessionDuration) }}</span>
      </div>
      <div class="time-item">
        <span class="time-label">Avg per Question:</span>
        <span class="time-value">{{ formatTime(averageTimePerQuestion) }}</span>
      </div>
      <div class="time-item">
        <span class="time-label">Estimated Remaining:</span>
        <span class="time-value">{{ formatTime(estimatedTimeRemaining) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ReviewProgress',
  props: {
    currentQuestion: {
      type: Number,
      default: 0
    },
    totalQuestions: {
      type: Number,
      default: 0
    },
    correctAnswers: {
      type: Number,
      default: 0
    },
    sessionStartTime: {
      type: Date,
      default: null
    },
    paused: {
      type: Boolean,
      default: false
    },
    answerHistory: {
      type: Array,
      default: () => []
    }
  },
  emits: ['pause-session', 'end-session'],
  setup(props) {
    const progressPercentage = computed(() => {
      const total = props.totalQuestions || 0
      if (total === 0) return 0
      return (props.currentQuestion / total) * 100
    })

    const accuracy = computed(() => {
      if (props.currentQuestion === 0) return 0
      return Math.round((props.correctAnswers / props.currentQuestion) * 100)
    })

    const sessionDuration = computed(() => {
      if (!props.sessionStartTime) return 0
      return Math.floor((new Date() - props.sessionStartTime) / 1000)
    })

    const averageTimePerQuestion = computed(() => {
      if (props.currentQuestion === 0) return 0
      return Math.floor(sessionDuration.value / props.currentQuestion)
    })

    const estimatedTimeRemaining = computed(() => {
      const total = props.totalQuestions || 0
      const remaining = total - props.currentQuestion
      return remaining * averageTimePerQuestion.value
    })

    const progressMarkers = computed(() => {
      const markers = []
      const total = props.totalQuestions || 0
      
      // Add markers for answered questions
      props.answerHistory.forEach((answer, index) => {
        if (total === 0) return
        const position = ((index + 1) / total) * 100
        const isCorrect = answer.quality === 'PERFECT' || answer.quality === 'CORRECT'
        
        markers.push({
          position,
          class: isCorrect ? 'marker-correct' : 'marker-incorrect',
          tooltip: `Question ${index + 1}: ${answer.quality}`
        })
      })
      
      return markers
    })

    const formatTime = (seconds) => {
      if (seconds < 60) {
        return `${seconds}s`
      } else if (seconds < 3600) {
        const minutes = Math.floor(seconds / 60)
        const remainingSeconds = seconds % 60
        return `${minutes}m ${remainingSeconds}s`
      } else {
        const hours = Math.floor(seconds / 3600)
        const minutes = Math.floor((seconds % 3600) / 60)
        return `${hours}h ${minutes}m`
      }
    }

    return {
      progressPercentage,
      accuracy,
      sessionDuration,
      averageTimePerQuestion,
      estimatedTimeRemaining,
      progressMarkers,
      formatTime
    }
  }
}
</script>

<style scoped>
.review-progress {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.session-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.session-title h3 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.25rem;
  font-weight: 600;
}

.session-stats {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6c757d;
  font-size: 0.9rem;
}

.stat-item strong {
  color: #007bff;
}

.stat-separator {
  color: #dee2e6;
}

.session-actions {
  display: flex;
  gap: 0.75rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-outline {
  background: white;
  border: 1px solid #dee2e6;
  color: #6c757d;
}

.btn-outline:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.progress-container {
  margin-bottom: 1rem;
}

.progress-bar {
  position: relative;
  width: 100%;
  height: 12px;
  background-color: #e9ecef;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #007bff 0%, #28a745 100%);
  transition: width 0.5s ease;
  border-radius: 6px;
}

.progress-markers {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.progress-marker {
  position: absolute;
  top: 50%;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  border: 2px solid white;
  z-index: 2;
}

.marker-correct {
  background-color: #28a745;
}

.marker-incorrect {
  background-color: #dc3545;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #6c757d;
}

.progress-percentage {
  font-weight: 600;
  color: #007bff;
}

.time-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.time-item {
  display: flex;
  flex-direction: column;
  text-align: center;
}

.time-label {
  font-size: 0.8rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.time-value {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .session-info {
    flex-direction: column;
    gap: 1rem;
  }
  
  .session-actions {
    align-self: stretch;
  }
  
  .session-actions .btn {
    flex: 1;
  }
  
  .time-info {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .time-item {
    flex-direction: row;
    justify-content: space-between;
    text-align: left;
  }
}
</style>