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
      <h4>{{ getQuestionHeader() }}</h4>
      <div class="question-meta">
        <span class="material-source" v-if="question?.highlight?.material">
          From: {{ question.highlight.material.title }}
        </span>
        <span class="card-type" v-if="question?.highlight?.cardType">
          Card Type: {{ question.highlight.cardType }}
        </span>
      </div>
    </div>

    <!-- Flashcard -->
    <div class="flashcard-container">
      <div 
        class="flashcard" 
        :class="{ flipped: isFlipped }"
        @click="flipCard"
      >
        <!-- Front of card -->
        <div class="flashcard-front">
          <div class="flashcard-content">
            <div class="highlight-text">
              {{ question?.highlight?.text || 'No text available' }}
              <button 
                v-if="question?.highlight?.text" 
                @click.stop="speakText(question.highlight.text)" 
                class="btn-speak"
                title="Speak this word/phrase"
              >
                🔊
              </button>
            </div>
            <div class="highlight-pronunciation" v-if="question?.highlight?.pronunciation">
              <span class="pronunciation">{{ question.highlight.pronunciation }}</span>
            </div>
            <div class="flip-hint">Click to see answer</div>
          </div>
        </div>
        
        <!-- Back of card -->
        <div class="flashcard-back">
          <div class="flashcard-content">
            <div class="back-text">
              {{ question?.highlight?.backText || 'No answer available' }}
              <button 
                v-if="question?.highlight?.backText" 
                @click.stop="speakText(question.highlight.backText)" 
                class="btn-speak"
                title="Speak this answer"
              >
                🔊
              </button>
            </div>
            <div class="flip-hint">Click to go back</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Context Section -->
    <div class="context-section">
      <h5>Context:</h5>
      <div class="context-text">
        <p>{{ question?.highlight?.context || 'No context available' }}</p>
      </div>
    </div>
    
    <!-- User Comment Section (hidden until requested) -->
    <div v-if="question?.highlight?.userComment" class="comment-section">
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
    
    <!-- Answer Buttons -->
    <div class="answer-buttons">
      <button 
        @click="$emit('answer-selected', 'BLACKOUT')"
        class="answer-btn blackout-btn"
        :disabled="submitting"
      >
        Don't Know
      </button>
      <button 
        @click="$emit('answer-selected', 'HARD')"
        class="answer-btn hard-btn"
        :disabled="submitting"
      >
        Hard
      </button>
      <button 
        @click="$emit('answer-selected', 'GOOD')"
        class="answer-btn good-btn"
        :disabled="submitting"
      >
        Good
      </button>
      <button 
        @click="$emit('answer-selected', 'EASY')"
        class="answer-btn easy-btn"
        :disabled="submitting"
      >
        Easy
      </button>
    </div>
  </div>
</template>

<script>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useSpeechService } from '../composables/useSpeechService'

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
    const isFlipped = ref(false)
    const { speakText } = useSpeechService()

    // Reset note visibility and flip state when the question changes
    watch(
      () => props.question?.highlight?.id,
      () => {
        showComment.value = false
        isFlipped.value = false
      }
    )

    // Method to flip the card
    const flipCard = () => {
      isFlipped.value = !isFlipped.value
    }

    // Method to get question header based on card type
    const getQuestionHeader = () => {
      const cardType = props.question?.highlight?.cardType || 'BASIC'
      
      switch (cardType) {
        case 'BASIC':
          return 'What does this word/phrase mean?'
        case 'REVERSE':
          return 'What word/phrase matches this definition?'
        case 'BASIC_AND_REVERSE':
          return 'Answer the question'
        default:
          return 'What does this word/phrase mean?'
      }
    }

    // Keyboard shortcuts
    const handleKeyPress = (event) => {
      if (props.submitting) return
      
      const key = event.key
      
      // Flip card with spacebar
      if (key === ' ') {
        event.preventDefault()
        flipCard()
        return
      }
      
      // Answer buttons
      if (key === '1') {
        emit('answer-selected', 'BLACKOUT')
      } else if (key === '2') {
        emit('answer-selected', 'HARD')
      } else if (key === '3') {
        emit('answer-selected', 'GOOD')
      } else if (key === '4') {
        emit('answer-selected', 'EASY')
      }
    }

    onMounted(() => {
      document.addEventListener('keydown', handleKeyPress)
    })

    onUnmounted(() => {
      document.removeEventListener('keydown', handleKeyPress)
    })

    return {
      showComment,
      isFlipped,
      speakText,
      flipCard,
      getQuestionHeader
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
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 0.5rem;
}

.material-source {
  background-color: #f8f9fa;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.card-type {
  background-color: #f8f9fa;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.flashcard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 2rem 0;
  perspective: 1000px;
}

.flashcard {
  width: 100%;
  max-width: 600px;
  height: 350px;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.6s;
  cursor: pointer;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.flashcard.flipped {
  transform: rotateY(180deg);
}

.flashcard-front,
.flashcard-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.flashcard-front {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 3px solid #007bff;
}

.flashcard-back {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 3px solid #28a745;
  transform: rotateY(180deg);
}

.flashcard-content {
  text-align: center;
  width: 100%;
  max-height: 100%;
  overflow-y: auto;
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.back-text {
  font-size: 1.5rem;
  font-weight: 600;
  color: #28a745;
  background: linear-gradient(135deg, #f8fff9 0%, #e6f9ec 100%);
  padding: 1.5rem 2rem;
  border-radius: 12px;
  border: 3px solid #28a745;
  margin-bottom: 1rem;
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  min-height: 150px;
}

.btn-speak {
  background: none;
  border: 2px solid currentColor;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.btn-speak:hover {
  background-color: currentColor;
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
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

.flip-hint {
  margin-top: 1rem;
  color: #6c757d;
  font-style: italic;
  font-size: 0.9rem;
}

.answer-buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.answer-btn {
  padding: 1rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
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

.blackout-btn {
  background: linear-gradient(135deg, #dc3545 0%, #e83e8c 100%);
  color: white;
  border: 2px solid #dc3545;
}

.blackout-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #e83e8c 0%, #dc3545 100%);
}

.hard-btn {
  background: linear-gradient(135deg, #fd7e14 0%, #ff9f43 100%);
  color: white;
  border: 2px solid #fd7e14;
}

.hard-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff9f43 0%, #fd7e14 100%);
}

.good-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: 2px solid #28a745;
}

.good-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
}

.easy-btn {
  background: linear-gradient(135deg, #007bff 0%, #6610f2 100%);
  color: white;
  border: 2px solid #007bff;
}

.easy-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #6610f2 0%, #007bff 100%);
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
  
  .back-text {
    font-size: 1.2rem;
    padding: 1rem 1.5rem;
  }
  
  .flashcard {
    height: 300px;
  }
  
  .answer-buttons {
    flex-wrap: wrap;
    gap: 0.75rem;
  }
  
  .answer-btn {
    flex: 1;
    min-width: 100px;
    font-size: 1rem;
    padding: 0.75rem 1rem;
  }
  
  .question-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .flashcard {
    height: 250px;
    padding: 1rem;
  }
  
  .highlight-text {
    font-size: 1.2rem;
    padding: 0.75rem 1rem;
  }
  
  .back-text {
    font-size: 1rem;
    padding: 0.75rem 1rem;
  }
  
  .answer-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .answer-btn {
    width: 100%;
    max-width: 200px;
  }
}
</style>