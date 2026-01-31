<template>
  <div class="review-question">
    <!-- Question Header -->
    <div class="question-header">
      <h4>What does this word/phrase mean?</h4>
      <div class="question-meta">
        <span class="material-source" v-if="question.highlight.material">
          From: {{ question.highlight.material.title }}
        </span>
      </div>
    </div>

    <!-- Highlighted Text -->
    <div class="highlight-display">
      <div class="highlight-text">
        {{ question.highlight.text }}
      </div>
      <div class="highlight-pronunciation" v-if="question.highlight.pronunciation">
        <span class="pronunciation">{{ question.highlight.pronunciation }}</span>
      </div>
    </div>

    <!-- Context Section -->
    <div class="context-section">
      <h5>Context:</h5>
      <div class="context-text">
        <p>{{ question.highlight.context }}</p>
      </div>
    </div>
    
    <!-- User Comment Section (hidden until requested) -->
    <div v-if="question.highlight.userComment" class="comment-section">
      <button
        type="button"
        class="btn-toggle-comment"
        @click="showComment = !showComment"
      >
        {{ showComment ? 'Hide My Note' : 'Show My Note' }}
      </button>
      <div v-if="showComment" class="user-comment">
        <p>{{ question.highlight.userComment }}</p>
      </div>
    </div>

    <!-- Answer Quality Selection -->
    <div class="answer-section">
      <h5>How well did you know this?</h5>
      <p class="answer-instruction">Select your confidence level:</p>
      
      <div class="quality-grid">
        <button 
          v-for="(quality, index) in answerQualities" 
          :key="quality.value"
          @click="$emit('answer-selected', quality.value)"
          :class="['quality-btn', quality.class]"
          :disabled="submitting"
          :title="`Press ${index + 1} for ${quality.label}`"
        >
          <div class="quality-content">
            <span class="quality-number">{{ index + 1 }}</span>
            <span class="quality-label">{{ quality.label }}</span>
            <span class="quality-description">{{ quality.description }}</span>
          </div>
        </button>
      </div>
      
      <div class="keyboard-hint">
        <small>💡 Tip: Use keyboard numbers 1-5 for quick selection</small>
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted, onUnmounted, ref, watch } from 'vue'

export default {
  name: 'ReviewQuestion',
  props: {
    question: {
      type: Object,
      required: true
    },
    submitting: {
      type: Boolean,
      default: false
    }
  },
  emits: ['answer-selected'],
  setup(props, { emit }) {
    const answerQualities = [
      {
        value: 'PERFECT',
        label: 'Perfect',
        description: 'I knew it immediately',
        class: 'btn-perfect'
      },
      {
        value: 'CORRECT',
        label: 'Correct',
        description: 'I knew it after thinking',
        class: 'btn-correct'
      },
      {
        value: 'DIFFICULT',
        label: 'Difficult',
        description: 'I struggled but got it',
        class: 'btn-difficult'
      },
      {
        value: 'INCORRECT',
        label: 'Incorrect',
        description: 'I got it wrong',
        class: 'btn-incorrect'
      },
      {
        value: 'BLACKOUT',
        label: 'No Idea',
        description: 'Complete blackout',
        class: 'btn-blackout'
      }
    ]

    const showComment = ref(false)

    // Reset note visibility when the question changes
    watch(
      () => props.question?.highlight?.id,
      () => {
        showComment.value = false
      }
    )

    // Keyboard shortcuts
    const handleKeyPress = (event) => {
      if (props.submitting) return
      
      const key = event.key
      if (key >= '1' && key <= '5') {
        const index = parseInt(key) - 1
        if (index < answerQualities.length) {
          emit('answer-selected', answerQualities[index].value)
        }
      }
    }

    onMounted(() => {
      document.addEventListener('keydown', handleKeyPress)
    })

    onUnmounted(() => {
      document.removeEventListener('keydown', handleKeyPress)
    })

    return {
      answerQualities,
      showComment
    }
  }
}
</script>

<style scoped>
.review-question {
  max-width: 800px;
  margin: 0 auto;
}

.question-header {
  text-align: center;
  margin-bottom: 2rem;
}

.question-header h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 600;
}

.question-meta {
  color: #6c757d;
  font-size: 0.9rem;
}

.material-source {
  background-color: #f8f9fa;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.highlight-display {
  text-align: center;
  margin: 2rem 0;
}

.highlight-text {
  font-size: 2rem;
  font-weight: 700;
  color: #007bff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f3ff 100%);
  padding: 1.5rem 2rem;
  border-radius: 12px;
  border: 3px solid #007bff;
  margin-bottom: 1rem;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
}

.highlight-pronunciation {
  margin-top: 0.5rem;
}

.pronunciation {
  font-style: italic;
  color: #6c757d;
  font-size: 1.1rem;
}

.context-section,
.comment-section {
  margin: 2rem 0;
}

.context-section h5,
.comment-section h5 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.context-text p {
  background-color: #f8f9fa;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin: 0;
  font-style: italic;
  border-left: 4px solid #007bff;
  line-height: 1.6;
}

.user-comment p {
  background-color: #fff3cd;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin: 0;
  border-left: 4px solid #ffc107;
  line-height: 1.6;
}

.btn-toggle-comment {
  display: inline-block;
  margin-bottom: 0.75rem;
  padding: 0.35rem 0.75rem;
  background-color: #ffc107;
  border: none;
  border-radius: 4px;
  color: #212529;
  font-size: 0.9rem;
  cursor: pointer;
}

.btn-toggle-comment:hover {
  background-color: #e0a800;
}

.answer-section {
  margin-top: 3rem;
}

.answer-section h5 {
  text-align: center;
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
}

.answer-instruction {
  text-align: center;
  color: #6c757d;
  margin: 0 0 2rem 0;
}

.quality-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.quality-btn {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  border: 2px solid transparent;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.quality-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.quality-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.quality-content {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 1rem;
}

.quality-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  font-weight: 700;
  font-size: 1rem;
}

.quality-label {
  font-weight: 600;
  font-size: 1.1rem;
  min-width: 80px;
}

.quality-description {
  flex: 1;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
}

/* Quality button styles */
.btn-perfect {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  border-color: #28a745;
  color: white;
}

.btn-correct {
  background: linear-gradient(135deg, #17a2b8 0%, #6f42c1 100%);
  border-color: #17a2b8;
  color: white;
}

.btn-difficult {
  background: linear-gradient(135deg, #ffc107 0%, #fd7e14 100%);
  border-color: #ffc107;
  color: white;
}

.btn-incorrect {
  background: linear-gradient(135deg, #dc3545 0%, #e83e8c 100%);
  border-color: #dc3545;
  color: white;
}

.btn-blackout {
  background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
  border-color: #6c757d;
  color: white;
}

.keyboard-hint {
  text-align: center;
  margin-top: 1rem;
  color: #6c757d;
}

@media (max-width: 768px) {
  .highlight-text {
    font-size: 1.5rem;
    padding: 1rem 1.5rem;
  }
  
  .quality-content {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
  
  .quality-label {
    min-width: auto;
  }
}
</style>