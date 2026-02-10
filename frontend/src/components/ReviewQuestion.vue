<template>
  <div class="review-question">
    <!-- Question Header -->
    <div class="question-header">
      <h4>{{ getQuestionHeader() }}</h4>
      <div class="question-meta">
        <span class="question-number">Card {{ question?.position }} of {{ question?.total }}</span>
        <span v-if="question?.dueDate" class="due-date">Due: {{ formatDueDate(question.dueDate) }}</span>
        <span v-if="question?.interval" class="interval-info">Interval: {{ question.interval }} days</span>
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
              {{ question?.text || 'No text available' }}
              <button 
                v-if="question?.text" 
                @click.stop="speakText(question.text)" 
                class="btn-speak"
                title="Speak this word/phrase"
              >
                🔊
              </button>
            </div>
            <div class="flip-hint">Click to see answer</div>
          </div>
        </div>
        
        <!-- Back of card -->
        <div class="flashcard-back">
          <div class="flashcard-content">
            <div class="back-text">
              {{ question?.backText || 'No answer available' }}
              <button 
                v-if="question?.backText" 
                @click.stop="speakText(question.backText)" 
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
    <div v-if="question?.context" class="context-section">
      <h5>Context</h5>
      <div class="context-text">
        <p>{{ question.context }}</p>
      </div>
    </div>
    
    <!-- User Comment Section (hidden until requested) -->
    <div v-if="question?.userComment" class="comment-section">
      <button
        type="button"
        class="btn-toggle-comment"
        @click="showComment = !showComment"
      >
        {{ showComment ? 'Hide My Note' : 'Show My Note' }}
      </button>
      <div v-if="showComment" class="user-comment">
        <p>{{ question.userComment }}</p>
      </div>
    </div>
    
    <!-- Answer Buttons -->
    <div class="answer-buttons">
      <button 
        @click="$emit('answer-selected', 'BLACKOUT')"
        class="answer-btn blackout-btn"
        :disabled="submitting"
      >
        <span class="btn-emoji">😵</span>
        Again
      </button>
      <button 
        @click="$emit('answer-selected', 'REMEMBERED')"
        class="answer-btn remembered-btn"
        :disabled="submitting"
      >
        <span class="btn-emoji">😅</span>
        Hard
      </button>
      <button 
        @click="$emit('answer-selected', 'DIFFICULT')"
        class="answer-btn difficult-btn"
        :disabled="submitting"
      >
        <span class="btn-emoji">🙂</span>
        Good
      </button>
      <button 
        @click="$emit('answer-selected', 'PERFECT')"
        class="answer-btn perfect-btn"
        :disabled="submitting"
      >
        <span class="btn-emoji">😊</span>
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
    }
  },
  emits: ['answer-selected'],
  setup(props, { emit }) {
    const showComment = ref(false)
    const isFlipped = ref(false)
    const { speakText } = useSpeechService()

    // Reset note visibility and flip state when the question changes
    watch(
      () => props.question?.id,
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
      return 'What does this word/phrase mean?'
    }

    // Method to format due date
    const formatDueDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
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
        emit('answer-selected', 'REMEMBERED')
      } else if (key === '3') {
        emit('answer-selected', 'DIFFICULT')
      } else if (key === '4') {
        emit('answer-selected', 'PERFECT')
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
      getQuestionHeader,
      formatDueDate
    }
  }
}
</script>

<style scoped>
.review-question {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  color: white;
}

.question-header {
  text-align: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.question-header h4 {
  margin: 0 0 1rem 0;
  color: white;
  font-size: 1.8rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.question-meta {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 0.5rem;
}

.question-number {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 0.4rem 1rem;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.question-number:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.due-date {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 0.4rem 1rem;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.due-date:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.interval-info {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 0.4rem 1rem;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.interval-info:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.flashcard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 2rem 0;
  perspective: 1200px;
}

.flashcard {
  width: 100%;
  max-width: 600px;
  height: 400px;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
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
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2.5rem;
  background: white;
  color: #333;
  transition: all 0.3s ease;
}

.flashcard-back {
  transform: rotateY(180deg);
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.flashcard-content {
  width: 100%;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
}

.highlight-text {
  font-size: 2.4rem;
  font-weight: 700;
  color: #333;
  line-height: 1.4;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 0 1rem;
}

.back-text {
  font-size: 2rem;
  font-weight: 600;
  color: white;
  line-height: 1.4;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 0 1rem;
}

.btn-speak {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 50%;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  font-size: 1.2rem;
}

.btn-speak:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.flip-hint {
  margin-top: 1.5rem;
  color: #666;
  font-size: 0.9rem;
  opacity: 0.8;
  animation: pulse 2s infinite;
  font-style: italic;
}

.flashcard-back .flip-hint {
  color: rgba(255, 255, 255, 0.8);
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.8;
  }
  50% {
    opacity: 1;
  }
}

.context-section {
  margin: 2rem 0;
  padding: 1.5rem;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.context-section:hover {
  background-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.context-section h5 {
  margin: 0 0 1rem 0;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.context-section h5::before {
  content: '📝';
  font-size: 1.2rem;
}

.context-text p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  font-size: 1rem;
  font-style: italic;
}

.comment-section {
  margin: 1.5rem 0;
  padding: 1.5rem;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.comment-section:hover {
  background-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.btn-toggle-comment {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 0.8rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-toggle-comment:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(240, 147, 251, 0.4);
}

.user-comment {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 1.2rem;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  animation: slideIn 0.3s ease forwards;
}

.user-comment p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  font-size: 1rem;
}

.answer-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.answer-btn {
  flex: 1;
  min-width: 120px;
  padding: 1.2rem 2rem;
  border: none;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-emoji {
  font-size: 1.3rem;
  line-height: 1;
}

.answer-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.answer-btn:hover::before {
  left: 100%;
}

.answer-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.answer-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.blackout-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  color: white;
}

.remembered-btn {
  background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
  color: white;
}

.difficult-btn {
  background: linear-gradient(135deg, #48dbfb 0%, #0abde3 100%);
  color: white;
}

.perfect-btn {
  background: linear-gradient(135deg, #1dd1a1 0%, #10ac84 100%);
  color: white;
}

/* Responsive design */
@media (max-width: 768px) {
  .review-question {
    margin: 1rem;
    padding: 1.5rem;
  }
  
  .flashcard {
    height: 320px;
    max-width: 100%;
  }
  
  .highlight-text {
    font-size: 2rem;
  }
  
  .back-text {
    font-size: 1.6rem;
  }
  
  .answer-buttons {
    flex-direction: column;
  }
  
  .answer-btn {
    width: 100%;
  }
  
  .btn-speak {
    width: 48px;
    height: 48px;
    font-size: 1rem;
  }
}

/* Animations */
.review-question,
.question-header,
.flashcard-container,
.context-section,
.comment-section,
.answer-buttons {
  animation: fadeInUp 0.6s ease forwards;
}

.question-header {
  animation-delay: 0.1s;
}

.flashcard-container {
  animation-delay: 0.2s;
}

.context-section {
  animation-delay: 0.3s;
}

.comment-section {
  animation-delay: 0.4s;
}

.answer-buttons {
  animation-delay: 0.5s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.8;
  }
  50% {
    opacity: 1;
  }
}
</style>