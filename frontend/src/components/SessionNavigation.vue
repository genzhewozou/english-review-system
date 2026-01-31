<template>
  <div class="session-navigation">
    <div class="nav-controls">
      <button 
        @click="$emit('previous-question')"
        :disabled="!canGoPrevious || disabled"
        class="nav-btn nav-btn-prev"
        title="Previous Question (Left Arrow)"
      >
        <span class="nav-icon">←</span>
        <span class="nav-label">Previous</span>
      </button>
      
      <div class="question-indicator">
        <span class="current-question">{{ currentQuestion }}</span>
        <span class="question-separator">/</span>
        <span class="total-questions">{{ totalQuestions }}</span>
      </div>
      
      <button 
        @click="$emit('next-question')"
        :disabled="!canGoNext || disabled"
        class="nav-btn nav-btn-next"
        title="Next Question (Right Arrow)"
      >
        <span class="nav-label">Next</span>
        <span class="nav-icon">→</span>
      </button>
    </div>
    
    <div class="question-grid" v-if="showQuestionGrid">
      <button
        v-for="(question, index) in questions"
        :key="index"
        @click="$emit('go-to-question', index)"
        :class="[
          'question-dot',
          {
            'current': index === currentQuestion - 1,
            'answered': question.answered,
            'correct': question.correct,
            'incorrect': question.answered && !question.correct
          }
        ]"
        :title="`Question ${index + 1}${question.answered ? (question.correct ? ' - Correct' : ' - Incorrect') : ''}`"
      >
        {{ index + 1 }}
      </button>
    </div>
    
    <div class="nav-actions">
      <button 
        @click="toggleQuestionGrid"
        class="action-btn"
        :class="{ active: showQuestionGrid }"
      >
        {{ showQuestionGrid ? 'Hide' : 'Show' }} Overview
      </button>
      
      <button 
        @click="$emit('skip-question')"
        :disabled="disabled"
        class="action-btn skip-btn"
        v-if="allowSkip"
      >
        Skip Question
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'

export default {
  name: 'SessionNavigation',
  props: {
    currentQuestion: {
      type: Number,
      required: true
    },
    totalQuestions: {
      type: Number,
      required: true
    },
    questions: {
      type: Array,
      default: () => []
    },
    disabled: {
      type: Boolean,
      default: false
    },
    allowSkip: {
      type: Boolean,
      default: false
    },
    allowPrevious: {
      type: Boolean,
      default: true
    }
  },
  emits: ['previous-question', 'next-question', 'go-to-question', 'skip-question'],
  setup(props, { emit }) {
    const showQuestionGrid = ref(false)

    const canGoPrevious = computed(() => {
      return props.allowPrevious && props.currentQuestion > 1
    })

    const canGoNext = computed(() => {
      return props.currentQuestion < props.totalQuestions
    })

    const toggleQuestionGrid = () => {
      showQuestionGrid.value = !showQuestionGrid.value
    }

    // Keyboard navigation
    const handleKeyPress = (event) => {
      if (props.disabled) return

      switch (event.key) {
        case 'ArrowLeft':
          if (canGoPrevious.value) {
            emit('previous-question')
          }
          break
        case 'ArrowRight':
          if (canGoNext.value) {
            emit('next-question')
          }
          break
        case 'Escape':
          showQuestionGrid.value = false
          break
      }
    }

    onMounted(() => {
      document.addEventListener('keydown', handleKeyPress)
    })

    onUnmounted(() => {
      document.removeEventListener('keydown', handleKeyPress)
    })

    return {
      showQuestionGrid,
      canGoPrevious,
      canGoNext,
      toggleQuestionGrid
    }
  }
}
</script>

<style scoped>
.session-navigation {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
}

.nav-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.nav-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #007bff;
  color: #007bff;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-icon {
  font-size: 1.2rem;
  font-weight: bold;
}

.question-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}

.current-question {
  color: #007bff;
  font-size: 1.3rem;
}

.question-separator {
  color: #6c757d;
  margin: 0 0.5rem;
}

.total-questions {
  color: #6c757d;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.question-dot {
  width: 40px;
  height: 40px;
  border: 2px solid #dee2e6;
  background: white;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.question-dot:hover {
  border-color: #007bff;
  background: #f0f9ff;
}

.question-dot.current {
  border-color: #007bff;
  background: #007bff;
  color: white;
}

.question-dot.answered {
  border-color: #28a745;
  background: #d4edda;
  color: #155724;
}

.question-dot.correct {
  border-color: #28a745;
  background: #28a745;
  color: white;
}

.question-dot.incorrect {
  border-color: #dc3545;
  background: #dc3545;
  color: white;
}

.nav-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.action-btn:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.action-btn.active {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

.skip-btn {
  color: #6c757d;
}

.skip-btn:hover {
  color: #495057;
  border-color: #6c757d;
}

@media (max-width: 768px) {
  .nav-controls {
    flex-direction: column;
    gap: 1rem;
  }
  
  .nav-btn {
    width: 100%;
    justify-content: center;
  }
  
  .question-grid {
    grid-template-columns: repeat(auto-fill, minmax(35px, 1fr));
  }
  
  .question-dot {
    width: 35px;
    height: 35px;
    font-size: 0.8rem;
  }
  
  .nav-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
  }
}
</style>