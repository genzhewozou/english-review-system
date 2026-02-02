<template>
  <div class="review-question">
    <!-- Selected Highlights List -->
    <div class="selected-highlights-section">
      <h4>Selected Highlights</h4>
      <p class="selected-count">{{ selectedHighlights.length }} highlights selected</p>
      
      <div class="highlights-list" v-if="selectedHighlights.length > 0">
        <div
          v-for="highlight in selectedHighlights"
          :key="highlight.id"
          class="highlight-list-item"
        >
          <div class="highlight-info">
            <span class="highlight-text-preview">{{ highlight.text }}</span>
            <span class="highlight-material">From: {{ highlight.materialTitle }}</span>
          </div>
          <button
            @click="$emit('delete-highlight', highlight.id)"
            class="delete-highlight-btn"
            title="Remove from selection"
          >
            ×
          </button>
        </div>
      </div>
      <div v-else class="no-highlights">
        No highlights selected yet
      </div>
    </div>
    
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
      
      <!-- Answer Buttons -->
      <div class="answer-buttons">
        <button 
          @click="$emit('answer-selected', 'PASS')"
          class="answer-btn pass-btn"
          :disabled="submitting"
        >
          Pass
        </button>
        <button 
          @click="$emit('answer-selected', 'NOT_GOT_IT')"
          class="answer-btn not-got-it-btn"
          :disabled="submitting"
        >
          Not Got It
        </button>
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
    },
    selectedHighlights: {
      type: Array,
      default: () => []
    }
  },
  emits: ['answer-selected', 'delete-highlight'],
  setup(props, { emit }) {
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
      if (key === '1') {
        emit('answer-selected', 'PASS')
      } else if (key === '2') {
        emit('answer-selected', 'NOT_GOT_IT')
      }
    }

    onMounted(() => {
      document.addEventListener('keydown', handleKeyPress)
    })

    onUnmounted(() => {
      document.removeEventListener('keydown', handleKeyPress)
    })

    return {
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
  margin-bottom: 1.5rem;
}

.pronunciation {
  font-style: italic;
  color: #6c757d;
  font-size: 1.1rem;
}

.answer-buttons {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.answer-btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 150px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.answer-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.answer-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.pass-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: 2px solid #28a745;
}

.pass-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
}

.not-got-it-btn {
  background: linear-gradient(135deg, #dc3545 0%, #e83e8c 100%);
  color: white;
  border: 2px solid #dc3545;
}

.not-got-it-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #e83e8c 0%, #dc3545 100%);
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

.selected-highlights-section {
  margin: 2rem 0;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 2px solid #e9ecef;
}

.selected-highlights-section h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.3rem;
  font-weight: 600;
}

.selected-count {
  color: #6c757d;
  font-size: 0.9rem;
  margin: 0 0 1rem 0;
}

.highlights-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: white;
  padding: 0.5rem;
}

.highlight-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e9ecef;
  transition: all 0.2s ease;
}

.highlight-list-item:hover {
  background-color: #f8f9fa;
}

.highlight-list-item:last-child {
  border-bottom: none;
}

.highlight-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.highlight-text-preview {
  color: #2c3e50;
  font-weight: 500;
  font-size: 1rem;
  word-break: break-word;
  max-width: 300px;
}

.highlight-material {
  color: #6c757d;
  font-size: 0.85rem;
  font-style: italic;
  background: #e9ecef;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.delete-highlight-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: #dc3545;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.delete-highlight-btn:hover {
  background: #c82333;
  transform: scale(1.1);
}

.no-highlights {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
  font-style: italic;
}



@media (max-width: 768px) {
  .highlight-text {
    font-size: 1.5rem;
    padding: 1rem 1.5rem;
  }
  
  .answer-buttons {
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }
  
  .answer-btn {
    width: 100%;
    max-width: 200px;
  }
}
</style>