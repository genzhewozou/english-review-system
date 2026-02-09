<template>
  <div class="card-editor">
    <h2 class="page-title">{{ isEditing ? 'Edit Card' : 'Create New Card' }}</h2>
    
    <div class="card-editor-container">
      <div class="card">
        <!-- Card Form -->
        <form @submit.prevent="saveCard" class="card-form">
          <!-- Card Type Selection -->
          <div class="form-group">
            <label class="form-label">Card Type</label>
            <div class="card-type-selector">
              <label
                v-for="type in cardTypes"
                :key="type.value"
                class="card-type-option"
                :class="{ active: form.cardType === type.value }"
              >
                <input
                  type="radio"
                  name="cardType"
                  :value="type.value"
                  v-model="form.cardType"
                />
                <span class="card-type-label">{{ type.label }}</span>
                <span class="card-type-description">{{ type.description }}</span>
              </label>
            </div>
          </div>
          
          <!-- Deck Selection -->
          <div class="form-group">
            <label class="form-label">Deck</label>
            <select
              v-model="form.deckId"
              class="form-control"
            >
              <option value="">No Deck</option>
              <option
                v-for="deck in userDecks"
                :key="deck.id"
                :value="deck.id"
              >
                {{ deck.name }}
              </option>
            </select>
          </div>
          
          <!-- Card Front -->
          <div class="form-group">
            <label class="form-label">Front (Question)</label>
            <textarea
              v-model="form.text"
              class="form-control"
              rows="3"
              placeholder="Enter the question or word here"
              required
            ></textarea>
            <button
              v-if="form.text"
              @click="speakText(form.text)"
              class="btn-speak"
              title="Speak this text"
            >
              🔊
            </button>
          </div>
          
          <!-- Card Back -->
          <div class="form-group">
            <label class="form-label">Back (Answer)</label>
            <textarea
              v-model="form.backText"
              class="form-control"
              rows="4"
              placeholder="Enter the answer or definition here"
              required
            ></textarea>
            <button
              v-if="form.backText"
              @click="speakText(form.backText)"
              class="btn-speak"
              title="Speak this text"
            >
              🔊
            </button>
          </div>
          
          <!-- Tags -->
          <div class="form-group">
            <label class="form-label">Tags</label>
            <div class="tags-input-container">
              <input
                type="text"
                v-model="tagInput"
                class="form-control"
                placeholder="Enter tags separated by spaces"
                @keyup.enter.prevent="addTag"
              />
              <button
                @click="addTag"
                class="btn-add-tag"
                title="Add tag"
              >
                +
              </button>
            </div>
            <div class="tags-list" v-if="form.tagsArray.length > 0">
              <span
                v-for="(tag, index) in form.tagsArray"
                :key="index"
                class="tag-item"
              >
                {{ tag }}
                <button
                  @click="removeTag(index)"
                  class="btn-remove-tag"
                  title="Remove tag"
                >
                  ×
                </button>
              </span>
            </div>
          </div>
          
          <!-- Preview -->
          <div class="form-group">
            <label class="form-label">Preview</label>
            <div class="card-preview">
              <div class="preview-card">
                <div class="preview-front">
                  <h4>Front</h4>
                  <div class="preview-content">{{ form.text || 'No content' }}</div>
                </div>
                <div class="preview-back">
                  <h4>Back</h4>
                  <div class="preview-content">{{ form.backText || 'No content' }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Form Actions -->
          <div class="form-actions">
            <router-link to="/card-browser" class="btn btn-secondary">
              Cancel
            </router-link>
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="saving || !isFormValid"
            >
              {{ saving ? 'Saving...' : (isEditing ? 'Update Card' : 'Create Card') }}
            </button>
          </div>
        </form>
      </div>
      
      <!-- Recent Cards -->
      <div class="recent-cards">
        <h3 class="section-title">Recent Cards</h3>
        <div class="recent-cards-list">
          <div
            v-for="card in recentCards"
            :key="card.id"
            class="recent-card-item"
            @click="editRecentCard(card)"
          >
            <div class="recent-card-front">{{ card.text }}</div>
            <div class="recent-card-back">{{ card.backText }}</div>
            <div class="recent-card-meta">
              <span class="recent-card-type">{{ card.cardType }}</span>
              <span class="recent-card-date">{{ formatDate(card.createdAt) }}</span>
            </div>
          </div>
          <div v-if="recentCards.length === 0" class="no-recent-cards">
            No recent cards found
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useApiService } from '../composables/useApiService'
import { useSpeechService } from '../composables/useSpeechService'
import { useDeckService } from '../services/deckService'

export default {
  name: 'CardEditor',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const { apiService } = useApiService()
    const { speakText } = useSpeechService()
    const { getAllDecks } = useDeckService()
    
    // Reactive state
    const saving = ref(false)
    const userDecks = ref([])
    const recentCards = ref([])
    
    // Card types
    const cardTypes = [
      {
        value: 'BASIC',
        label: 'Basic',
        description: 'Standard front-back card'
      },
      {
        value: 'REVERSE',
        label: 'Reverse',
        description: 'Answer on front, question on back'
      },
      {
        value: 'BASIC_AND_REVERSE',
        label: 'Basic & Reverse',
        description: 'Both directions'
      }
    ]
    
    // Form state
    const form = ref({
      id: null,
      text: '',
      backText: '',
      cardType: 'BASIC',
      deckId: '',
      tagsArray: []
    })
    
    const tagInput = ref('')
    
    // Computed properties
    const isEditing = computed(() => !!form.value.id)
    
    const isFormValid = computed(() => {
      return form.value.text.trim() !== '' && form.value.backText.trim() !== ''
    })
    
    // Methods
    const loadDecks = async () => {
      try {
        const decks = await getAllDecks()
        userDecks.value = decks
      } catch (error) {
        console.error('Error loading decks:', error)
      }
    }
    
    const loadRecentCards = async () => {
      try {
        const response = await apiService.get('/highlights?limit=5')
        recentCards.value = response.data || []
      } catch (error) {
        console.error('Error loading recent cards:', error)
      }
    }
    
    const loadCardForEditing = async (cardId) => {
      try {
        const response = await apiService.get(`/highlights/${cardId}`)
        const card = response.data
        form.value = {
          id: card.id,
          text: card.text,
          backText: card.backText,
          cardType: card.cardType || 'BASIC',
          deckId: card.deckId || '',
          tagsArray: card.tags ? card.tags.split(' ') : []
        }
      } catch (error) {
        console.error('Error loading card:', error)
        alert('Failed to load card. Please try again.')
      }
    }
    
    const addTag = () => {
      const tag = tagInput.value.trim()
      if (tag && !form.value.tagsArray.includes(tag)) {
        form.value.tagsArray.push(tag)
        tagInput.value = ''
      }
    }
    
    const removeTag = (index) => {
      form.value.tagsArray.splice(index, 1)
    }
    
    const saveCard = async () => {
      if (!isFormValid.value) return
      
      saving.value = true
      try {
        const cardData = {
          text: form.value.text,
          backText: form.value.backText,
          cardType: form.value.cardType,
          deckId: form.value.deckId,
          tags: form.value.tagsArray.join(' ')
        }
        
        let response
        if (isEditing.value) {
          response = await apiService.put(`/highlights/${form.value.id}`, cardData)
        } else {
          response = await apiService.post('/highlights', cardData)
        }
        
        // Redirect to card browser
        router.push('/card-browser')
      } catch (error) {
        console.error('Error saving card:', error)
        alert('Failed to save card. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const editRecentCard = (card) => {
      form.value = {
        id: card.id,
        text: card.text,
        backText: card.backText,
        cardType: card.cardType || 'BASIC',
        deckId: card.deckId || '',
        tagsArray: card.tags ? card.tags.split(' ') : []
      }
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      const date = new Date(dateString)
      return date.toLocaleDateString()
    }
    
    // Lifecycle hooks
    onMounted(async () => {
      await Promise.all([
        loadDecks(),
        loadRecentCards()
      ])
      
      // Check if editing a card
      const cardId = route.params.id
      if (cardId) {
        await loadCardForEditing(cardId)
      }
    })
    
    return {
      // State
      saving,
      userDecks,
      recentCards,
      cardTypes,
      form,
      tagInput,
      
      // Computed
      isEditing,
      isFormValid,
      
      // Methods
      speakText,
      addTag,
      removeTag,
      saveCard,
      editRecentCard,
      formatDate
    }
  }
}
</script>

<style scoped>
.card-editor {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  color: #2c3e50;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 2rem 0 1.5rem;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-editor-container {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.card-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-label {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1rem;
}

.form-control {
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-control:focus {
  outline: none;
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
}

.card-type-selector {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.card-type-option {
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.card-type-option:hover {
  border-color: #4361ee;
  background: rgba(67, 97, 238, 0.05);
}

.card-type-option.active {
  border-color: #4361ee;
  background: rgba(67, 97, 238, 0.1);
}

.card-type-option input[type="radio"] {
  transform: scale(1.2);
  accent-color: #4361ee;
}

.card-type-label {
  font-weight: 600;
  color: #2c3e50;
}

.card-type-description {
  color: #6c757d;
  font-size: 0.9rem;
}

.tags-input-container {
  position: relative;
  display: flex;
  gap: 0.5rem;
}

.tags-input-container .form-control {
  flex: 1;
}

.btn-add-tag {
  padding: 0 1.5rem;
  border: 2px solid #4361ee;
  border-radius: 10px;
  background: #4361ee;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-add-tag:hover {
  background: #3a0ca3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.3);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-item {
  padding: 0.5rem 1rem;
  background: #4361ee;
  color: white;
  border-radius: 20px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-remove-tag {
  background: none;
  border: none;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.btn-remove-tag:hover {
  background: rgba(255, 255, 255, 0.2);
}

.btn-speak {
  background: none;
  border: 2px solid #e9ecef;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}

.btn-speak:hover {
  border-color: #4361ee;
  background: rgba(67, 97, 238, 0.05);
}

.card-preview {
  border: 2px solid #e9ecef;
  border-radius: 10px;
  padding: 1.5rem;
  background: #f8f9fa;
}

.preview-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preview-front,
.preview-back {
  padding: 1rem;
  border-radius: 8px;
  background: white;
  border: 2px solid #e9ecef;
}

.preview-front h4,
.preview-back h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.preview-content {
  color: #495057;
  line-height: 1.4;
  min-height: 60px;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.recent-cards {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.section-title {
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1.5rem 0;
}

.recent-cards-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 600px;
  overflow-y: auto;
}

.recent-card-item {
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recent-card-item:hover {
  border-color: #4361ee;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.15);
}

.recent-card-front {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.recent-card-back {
  color: #495057;
  margin-bottom: 0.75rem;
  font-size: 0.85rem;
  line-height: 1.4;
}

.recent-card-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.8rem;
  color: #6c757d;
}

.no-recent-cards {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
  font-style: italic;
}

/* Responsive design */
@media (max-width: 768px) {
  .card-editor-container {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .btn {
    width: 100%;
  }
  
  .tags-input-container {
    flex-direction: column;
  }
  
  .btn-add-tag {
    align-self: flex-start;
    padding: 0.75rem 1.5rem;
  }
  
  .card-type-option {
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }
}

@media (max-width: 480px) {
  .card-editor {
    padding: 10px;
  }
  
  .card {
    padding: 1.5rem;
  }
  
  .recent-cards {
    padding: 1.5rem;
  }
}
</style>
