<template>
  <main class="review" aria-labelledby="page-title">
    <h2 id="page-title" class="page-title">Review Sessions</h2>
    
    <section class="review-start">
      <div class="card text-center">
        <h3 class="card-title">Start a New Review Session</h3>
        <p class="card-subtitle">Review your vocabulary cards using spaced repetition.</p>
        
        <div class="review-stats mb-3">
          <div class="stat-item" aria-label="Due for Review">
            <strong>{{ pendingReviews }}</strong>
            <span>Due for Review</span>
          </div>
          <div class="stat-item" aria-label="Total Vocabulary">
            <strong>{{ totalCards }}</strong>
            <span>Total Vocabulary</span>
          </div>
          <div class="stat-item" aria-label="Your Decks">
            <strong>{{ userDecks.length }}</strong>
            <span>Your Decks</span>
          </div>
        </div>
        
        <!-- Session Type Selection -->
        <div class="session-type-selection mt-3">
          <h4 class="selection-title">Step 1: Choose session type</h4>
          <div class="session-type-tabs" role="tablist" aria-label="Session type selection">
            <button
              @click="sessionType = 'all'"
              :class="['session-type-tab', { active: sessionType === 'all' }]"
              role="tab"
              :aria-selected="sessionType === 'all'"
              tabindex="0"
            >
              All Vocabulary
            </button>
            <button
              @click="sessionType = 'deck'"
              :class="['session-type-tab', { active: sessionType === 'deck' }]"
              role="tab"
              :aria-selected="sessionType === 'deck'"
              tabindex="0"
            >
              From Deck
            </button>
            <button
              @click="sessionType = 'custom'"
              :class="['session-type-tab', { active: sessionType === 'custom' }]"
              role="tab"
              :aria-selected="sessionType === 'custom'"
              tabindex="0"
            >
              Custom Selection
            </button>
          </div>
        </div>
        
        <!-- Deck Selection -->
        <div class="deck-selection mt-3" v-if="sessionType === 'deck' && userDecks.length">
          <h4 class="selection-title">Step 2: Choose a deck</h4>
          <p class="selection-help">
            Select a deck to review all its cards.
          </p>
          <div class="deck-radio-list">
            <label
              v-for="deck in userDecks"
              :key="deck.id"
              class="deck-radio-item"
            >
              <input
                type="radio"
                name="deckSelection"
                :value="deck.id"
                v-model="selectedDeckId"
                tabindex="0"
              />
              <span class="deck-radio-label">{{ deck.name }}</span>
              <span class="deck-card-count">({{ deck.cardCount }} cards)</span>
            </label>
          </div>
        </div>
        
        <!-- Custom Selection (Original Material-Based) -->
        <div v-if="sessionType === 'custom'">
          <!-- Step 2: Select materials -->
          <div class="material-selection mt-3" v-if="materials.length">
            <h4 class="selection-title">Step 2: Choose study materials</h4>
            <p class="selection-help">
              Select one material to view its vocabulary cards.
            </p>
            <div class="material-radio-list">
              <label
                v-for="material in materials"
                :key="material.id"
                class="material-radio-item"
              >
                <input
                  type="radio"
                  name="materialSelection"
                  :value="material.id"
                  v-model="selectedMaterialId"
                  @change="onMaterialsChange"
                  tabindex="0"
                />
                <span class="material-radio-label">{{ material.title }}</span>
              </label>
            </div>
          </div>
          
          <!-- Step 3: Show vocabulary of the selected materials -->
          <div v-if="selectedMaterialId && materialCards.length" class="card-selection mt-3">
            <h4 class="selection-title">Step 3: Choose vocabulary for this session</h4>
            <p class="selection-help">
              Select the words you want to add to your new review session.
            </p>
            <!-- Search input for cards -->
            <div class="search-container mb-3">
              <input
                type="text"
                v-model="searchQuery"
                placeholder="Search cards..."
                class="search-input"
                aria-label="Search cards"
                tabindex="0"
              />
              <div class="search-options">
                <label class="similar-search-toggle">
                  <input
                    type="checkbox"
                    v-model="similarSearchEnabled"
                    tabindex="0"
                  />
                  <span>Include similar words</span>
                </label>
              </div>
            </div>
            <div class="selection-list">
              <label
                v-for="c in filteredCards"
                :key="c.id"
                class="selection-item"
              >
                <input
                  type="checkbox"
                  :value="c.id"
                  v-model="selectedCardIds"
                  tabindex="0"
                />
                <span class="selection-text">{{ c.text }}</span>
              </label>
            </div>
          </div>
          
          <!-- Step 4: Show selected cards with source information -->
          <div v-if="selectedCardsList.length" class="selected-cards-summary mt-3">
            <h4 class="selection-title">Step 4: Selected cards ({{ selectedCardsList.length }})</h4>
            <p class="selection-help">
              Review your selected cards before starting the session.
            </p>
            <div class="selected-cards-list">
              <div
                v-for="card in selectedCardsList"
                :key="card.id"
                class="selected-card-item"
              >
                <div class="card-info">
                  <span class="card-text">{{ card.text }}</span>
                  <span class="card-source">From: {{ card.materialTitle }}</span>
                </div>
                <button
                  @click="deleteCard(card.id)"
                  class="delete-btn"
                  title="Remove from selection"
                  aria-label="Remove card from selection"
                  tabindex="0"
                >
                  ×
                </button>
              </div>
            </div>
          </div>
        </div>

        <button 
          @click="startReviewSession" 
          :disabled="starting || (!canStartSession)"
          class="btn mt-3"
          aria-busy="{{ starting }}"
          tabindex="0"
        >
          <span v-if="starting" class="loading-spinner small"></span>
          {{ starting ? 'Starting...' : getStartButtonText() }}
        </button>
        
        <p v-if="totalCards === 0" class="mt-3">
          No vocabulary cards found. Please add some cards first!
        </p>
        
        <p v-if="materials.length === 0 && sessionType === 'custom'" class="mt-3">
          No study materials found. Please upload some materials first!
        </p>
        
        <p v-if="userDecks.length === 0 && sessionType === 'deck'" class="mt-3">
          No decks found. Please create a deck first!
        </p>
      </div>
    </section>
  </main>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useApiService } from '../composables/useApiService'
import { useMaterialService } from '../services/materialService'
import { useVocabularyService } from '../services/vocabularyService'
import { useDeckService } from '../services/deckService'

export default {
  name: 'Review',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const pendingReviews = ref(0)
    const totalCards = ref(0)
    const starting = ref(false)
    const cards = ref([])
    const selectedCardIds = ref([])
    const materials = ref([])
    const selectedMaterialId = ref('')
    const materialCards = ref([])
    const searchQuery = ref('')
    const similarSearchEnabled = ref(false)
    
    // Deck-related variables
    const sessionType = ref('all') // 'all', 'deck', 'custom'
    const selectedDeckId = ref('')
    const userDecks = ref([])
    
    // Helper function to calculate string similarity (Levenshtein distance)
    const getSimilarity = (str1, str2) => {
      const len1 = str1.length
      const len2 = str2.length
      const matrix = Array(len1 + 1).fill().map(() => Array(len2 + 1).fill(0))
      
      for (let i = 0; i <= len1; i++) matrix[i][0] = i
      for (let j = 0; j <= len2; j++) matrix[0][j] = j
      
      for (let i = 1; i <= len1; i++) {
        for (let j = 1; j <= len2; j++) {
          const cost = str1[i - 1] === str2[j - 1] ? 0 : 1
          matrix[i][j] = Math.min(
            matrix[i - 1][j] + 1,
            matrix[i][j - 1] + 1,
            matrix[i - 1][j - 1] + cost
          )
        }
      }
      
      const maxLength = Math.max(len1, len2)
      return (maxLength - matrix[len1][len2]) / maxLength
    }
    
    // Computed property for filtered cards
    const filteredCards = computed(() => {
      if (!searchQuery.value) {
        return materialCards.value
      }
      
      const query = searchQuery.value.toLowerCase()
      
      return materialCards.value.filter(c => {
        const cardText = c.text.toLowerCase()
        
        // Exact match
        if (cardText.includes(query)) {
          return true
        }
        
        // Similar word search if enabled
        if (similarSearchEnabled.value) {
          // Split into words and check similarity for each word
          const words = cardText.split(/\s+/)
          return words.some(word => {
            const similarity = getSimilarity(query, word)
            return similarity > 0.6 // 60% similarity threshold
          })
        }
        
        return false
      })
    })
    
    // Computed property for selected cards with material information
    const selectedCardsList = computed(() => {
      return selectedCardIds.value.map(id => {
        const card = cards.value.find(c => c.id === id)
        if (card) {
          const material = materials.value.find(m => m.id === card.materialId)
          return {
            ...card,
            materialTitle: material ? material.title : 'Unknown'
          }
        }
        return null
      }).filter(c => c !== null)
    })
    
    // Computed property to determine if session can start
    const canStartSession = computed(() => {
      switch (sessionType.value) {
        case 'all':
          return totalCards.value > 0
        case 'deck':
          return selectedDeckId.value !== ''
        case 'custom':
          return selectedMaterialId.value === '' || selectedCardIds.value.length > 0
        default:
          return false
      }
    })
    
    // Method to get start button text
    const getStartButtonText = () => {
      switch (sessionType.value) {
        case 'all':
          return 'Start Full Review Session'
        case 'deck':
          return 'Start Deck Review Session'
        case 'custom':
          return selectedCardIds.value.length > 0 ? 'Start Selected Review Session' : 'Start Review Session'
        default:
          return 'Start Review Session'
      }
    }
    
    // Method to delete a card from selection
    const deleteCard = (cardId) => {
      const index = selectedCardIds.value.indexOf(cardId)
      if (index > -1) {
        selectedCardIds.value.splice(index, 1)
      }
    }
    
    const { apiService } = useApiService()
    const { getAllMaterials } = useMaterialService()
    const { getCardsByMaterial, getAllCards } = useVocabularyService()
    const { getAllDecks } = useDeckService()
    
    const loadReviewData = async () => {
      try {
        // Load materials
        const materialsData = await getAllMaterials()
        materials.value = materialsData
        
        // Load all cards
        const allCards = await getAllCards()
        cards.value = allCards
        totalCards.value = allCards.length

        // Load user decks
        const decksData = await getAllDecks()
        userDecks.value = decksData

        // For now, just set pending reviews to total cards to enable the button
        pendingReviews.value = totalCards.value
      } catch (error) {
        console.error('Error loading review data:', error)
      }
    }
    
    // Handle cardId from query parameters
    const handleCardReview = async () => {
      const cardId = route.query.cardId
      if (cardId) {
        try {
          // Start review session for this specific card
          const response = await apiService.post('/reviews/sessions/custom', [cardId])
          const session = response.data
          
          // Navigate to the active review session
          router.push(`/review/${session.id}`)
        } catch (error) {
          console.error('Error starting card review:', error)
          // If error, just continue with normal review page
        }
      }
    }
    
    const onMaterialsChange = async () => {
      materialCards.value = []
      
      if (selectedMaterialId.value) {
        try {
          // Load cards for the selected material
          const cardsData = await getCardsByMaterial(selectedMaterialId.value)
          // Add material information to each card for grouping
          const cardsWithMaterial = cardsData.map(c => ({
            ...c,
            materialId: selectedMaterialId.value
          }))
          materialCards.value = cardsWithMaterial
        } catch (error) {
          console.error('Error loading material cards:', error)
        }
      }
    }
    
    const startReviewSession = async () => {
      starting.value = true
      try {
        let response
        
        switch (sessionType.value) {
          case 'deck':
            // Start review session from deck
            response = await apiService.post(`/reviews/sessions/deck/${selectedDeckId.value}`)
            break
          case 'custom':
            // Start custom review session with selected cards
            if (selectedCardIds.value.length > 0) {
              response = await apiService.post('/reviews/sessions/custom', selectedCardIds.value)
            } else {
              response = await apiService.post('/reviews/sessions')
            }
            break
          case 'all':
          default:
            // Start review session with all cards
            response = await apiService.post('/reviews/sessions')
            break
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
    
    onMounted(async () => {
      console.log('Debug: Component mounted, calling loadReviewData')
      await loadReviewData()
      console.log('Debug: Data loaded, checking for cardId')
      await handleCardReview()
    })
    
    return {
      pendingReviews,
      totalCards,
      starting,
      cards,
      selectedCardIds,
      materials,
      selectedMaterialId,
      materialCards,
      searchQuery,
      similarSearchEnabled,
      filteredCards,
      selectedCardsList,
      onMaterialsChange,
      startReviewSession,
      deleteCard,
      // Deck-related
      sessionType,
      selectedDeckId,
      userDecks,
      canStartSession,
      getStartButtonText,
    }
  }
}
</script>

<style scoped>
.review-start {
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  text-align: center;
  color: var(--text-primary);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  margin: var(--space-8) 0 var(--space-6);
  background: linear-gradient(90deg, var(--primary-600), var(--primary-800));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-title {
  color: var(--text-primary);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  margin: 0 0 var(--space-3);
}

.card-subtitle {
  color: var(--text-secondary);
  font-size: var(--text-lg);
  margin: 0 0 var(--space-8);
  line-height: var(--leading-relaxed);
}

.card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-10);
  box-shadow: var(--shadow-lg);
  margin-bottom: var(--space-8);
  border: 1px solid var(--surface-border);
  position: relative;
  overflow: hidden;
  transition: var(--transition-normal);
}

.card:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-2px);
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-600), var(--primary-800), var(--secondary-600));
}

.text-center {
  text-align: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4) var(--space-8);
  background: linear-gradient(135deg, var(--primary-600), var(--primary-800));
  color: white;
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  text-decoration: none;
  min-width: 240px;
  box-shadow: var(--shadow-md);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--primary-700), var(--primary-900));
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.btn:disabled {
  background: linear-gradient(135deg, var(--text-light), var(--text-secondary));
  cursor: not-allowed;
  opacity: 0.7;
  transform: none;
  box-shadow: var(--shadow-sm);
}

.btn:active {
  transform: translateY(0);
  box-shadow: var(--shadow-md);
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
  margin-right: var(--space-2);
}

.loading-spinner.small {
  width: 14px;
  height: 14px;
  border-width: 2px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.review-stats {
  display: flex;
  justify-content: center;
  gap: var(--space-12);
  margin: var(--space-8) 0;
  padding: var(--space-6);
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  border-radius: var(--radius-xl);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.stat-item {
  text-align: center;
  min-width: 120px;
}

.stat-item strong {
  display: block;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--primary-600);
  margin-bottom: var(--space-2);
  transition: transform var(--transition-normal);
}

.stat-item:hover strong {
  transform: scale(1.1);
}

.stat-item span {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.mt-3 {
  margin-top: var(--space-4);
}

.mb-3 {
  margin-bottom: var(--space-4);
}

.card-selection {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.material-selection {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.selection-title {
  margin: 0 0 var(--space-3) 0;
  color: var(--text-primary);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
}

.selection-help {
  margin: 0 0 var(--space-4) 0;
  color: var(--text-secondary);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
}

.selection-list {
  max-height: 250px;
  overflow: auto;
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background-color: var(--bg-secondary);
  transition: var(--transition-normal);
}

.selection-list:hover {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

.selection-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  cursor: pointer;
  transition: var(--transition-normal);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-1);
}

.selection-item:hover {
  background-color: var(--primary-50);
}

.selection-item input[type="checkbox"] {
  transform: scale(1.2);
  accent-color: var(--primary-500);
}

.selection-text {
  color: var(--text-primary);
  word-break: break-word;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

.material-dropdown {
  max-width: 100%;
  margin: 0 auto;
}

.material-select {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  background-color: var(--surface-primary);
  cursor: pointer;
  transition: var(--transition-normal);
}

.material-select:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

.material-radio-list {
  max-height: 250px;
  overflow: auto;
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background-color: var(--bg-secondary);
  transition: var(--transition-normal);
}

.material-radio-list:hover {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

.material-radio-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  transition: var(--transition-normal);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-1);
}

.material-radio-item:hover {
  background-color: var(--primary-50);
}

.material-radio-item input[type="radio"] {
  transform: scale(1.2);
  accent-color: var(--primary-500);
}

.material-radio-label {
  color: var(--text-primary);
  word-break: break-word;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

.search-container {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  transition: var(--transition-normal);
  background-color: var(--bg-secondary);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
  background-color: var(--surface-primary);
}

.search-input::placeholder {
  color: var(--text-secondary);
  font-style: italic;
}

.search-options {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-2);
}

.similar-search-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  transition: var(--transition-normal);
}

.similar-search-toggle:hover {
  color: var(--primary-600);
}

.similar-search-toggle input[type="checkbox"] {
  transform: scale(1.1);
  accent-color: var(--primary-500);
}

.selected-cards-summary {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.selected-cards-list {
  max-height: 300px;
  overflow: auto;
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background-color: var(--bg-secondary);
  transition: var(--transition-normal);
}

.selected-cards-list:hover {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

.selected-card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background-color: var(--surface-primary);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-2);
  transition: var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.selected-card-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.selected-card-item:last-child {
  margin-bottom: 0;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
  min-width: 0;
}

.card-text {
  color: var(--text-primary);
  font-weight: var(--font-medium);
  font-size: var(--text-base);
  word-break: break-word;
}

.card-source {
  color: var(--text-secondary);
  font-size: var(--text-xs);
  font-style: italic;
  background-color: var(--bg-tertiary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  display: inline-block;
  width: fit-content;
}

.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background-color: var(--error-600);
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  transition: var(--transition-normal);
  flex-shrink: 0;
  margin-left: var(--space-4);
}

.delete-btn:hover {
  background-color: var(--error-700);
  transform: scale(1.1);
}

.session-type-selection {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.session-type-tabs {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
  background-color: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--space-1);
  border: 2px solid var(--surface-border);
  transition: var(--transition-normal);
}

.session-type-tabs:hover {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

.session-type-tab {
  flex: 1;
  padding: var(--space-3) var(--space-4);
  border: none;
  background-color: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition-normal);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.session-type-tab:hover {
  background-color: var(--primary-50);
}

.session-type-tab.active {
  background: linear-gradient(135deg, var(--primary-600), var(--primary-800));
  color: white;
  box-shadow: var(--shadow-md);
}

.deck-selection {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.deck-radio-list {
  max-height: 250px;
  overflow: auto;
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background-color: var(--bg-secondary);
  transition: var(--transition-normal);
}

.deck-radio-list:hover {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
}

.deck-radio-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  transition: var(--transition-normal);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-1);
}

.deck-radio-item:hover {
  background-color: var(--primary-50);
}

.deck-radio-item input[type="radio"] {
  transform: scale(1.2);
  accent-color: var(--primary-500);
}

.deck-radio-label {
  color: var(--text-primary);
  word-break: break-word;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  flex: 1;
}

.deck-card-count {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  background-color: var(--bg-tertiary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .review-stats {
    flex-direction: column;
    gap: var(--space-4);
  }
  
  .btn {
    width: 100%;
  }
  
  .session-type-tabs {
    flex-direction: column;
  }
  
  .session-type-tab {
    width: 100%;
  }
  
  .page-title {
    font-size: var(--text-3xl);
  }
  
  .card-title {
    font-size: var(--text-xl);
  }
  
  .card {
    padding: var(--space-6);
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .card {
    border: 2px solid var(--text-primary);
  }
  
  .session-type-tabs {
    border: 2px solid var(--text-primary);
  }
  
  .selection-list,
  .material-radio-list,
  .deck-radio-list,
  .selected-cards-list {
    border: 2px solid var(--text-primary);
  }
  
  .btn {
    border: 2px solid var(--text-primary);
  }
}

/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
  .card {
    transition: none;
  }
  
  .card:hover {
    transform: none;
  }
  
  .stat-item strong {
    transition: none;
  }
  
  .stat-item:hover strong {
    transform: none;
  }
  
  .selection-item {
    transition: none;
  }
  
  .material-radio-item {
    transition: none;
  }
  
  .deck-radio-item {
    transition: none;
  }
  
  .selected-card-item {
    transition: none;
  }
  
  .selected-card-item:hover {
    transform: none;
  }
  
  .delete-btn {
    transition: none;
  }
  
  .delete-btn:hover {
    transform: none;
  }
  
  .session-type-tab {
    transition: none;
  }
  
  .btn {
    transition: none;
  }
  
  .btn:hover:not(:disabled) {
    transform: none;
  }
  
  .loading-spinner {
    animation: none;
  }
}
</style>