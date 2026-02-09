<template>
  <main class="card-browser" aria-label="Card browser">
    <h1 class="page-title">Card Browser</h1>
    
    <!-- Search and Filter Section -->
    <section class="search-filter-section">
      <div class="content-card">
        <h2 class="section-title">Search & Filter</h2>
        
        <!-- Search Input -->
        <div class="search-input-group">
          <label for="search-input" class="sr-only">Search cards</label>
          <input
            type="text"
            id="search-input"
            v-model="searchQuery"
            placeholder="Search cards..."
            class="form-control"
            @input="onSearchChange"
            aria-describedby="search-hint"
          />
          <button
            @click="clearSearch"
            class="btn-clear-search"
            title="Clear search"
            aria-label="Clear search"
            tabindex="0"
            @keydown.enter="clearSearch"
            @keydown.space="clearSearch"
          >
            ×
          </button>
        </div>
        <p id="search-hint" class="form-help">Search by card front, back, or tags</p>
        
        <!-- Filter Options -->
        <div class="filter-options">
          <!-- Deck Filter -->
          <div class="filter-group">
            <label for="deck-filter" class="filter-label">Deck</label>
            <select
              id="deck-filter"
              v-model="selectedDeckId"
              class="form-control"
              @change="onFilterChange"
              aria-label="Filter by deck"
            >
              <option value="">All Decks</option>
              <option
                v-for="deck in userDecks"
                :key="deck.id"
                :value="deck.id"
              >
                {{ deck.name }}
              </option>
            </select>
          </div>
          
          <!-- Card Type Filter -->
          <div class="filter-group">
            <label for="card-type-filter" class="filter-label">Card Type</label>
            <select
              id="card-type-filter"
              v-model="selectedCardType"
              class="form-control"
              @change="onFilterChange"
              aria-label="Filter by card type"
            >
              <option value="">All Types</option>
              <option value="BASIC">Basic</option>
              <option value="REVERSE">Reverse</option>
              <option value="BASIC_AND_REVERSE">Basic & Reverse</option>
            </select>
          </div>
          
          <!-- Status Filter -->
          <div class="filter-group">
            <label for="status-filter" class="filter-label">Status</label>
            <select
              id="status-filter"
              v-model="selectedStatus"
              class="form-control"
              @change="onFilterChange"
              aria-label="Filter by card status"
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="SUSPENDED">Suspended</option>
            </select>
          </div>
        </div>
        
        <!-- Tag Filter -->
        <div class="tag-filter-section">
          <label class="filter-label">Tags</label>
          <div class="tag-selector" role="group" aria-label="Select tags">
            <span
              v-for="tag in availableTags"
              :key="tag.id"
              class="tag-option"
              :class="{ active: selectedTags.includes(tag.id) }"
              @click="toggleTagSelection(tag.id)"
              tabindex="0"
              @keydown.enter="toggleTagSelection(tag.id)"
              @keydown.space="toggleTagSelection(tag.id)"
              role="button"
              :aria-pressed="selectedTags.includes(tag.id)"
              :aria-label="`Toggle tag ${tag.name}`"
            >
              {{ tag.name }}
            </span>
          </div>
        </div>
        
        <!-- Sort Options -->
        <div class="sort-options" role="group" aria-label="Sort options">
          <label for="sort-by" class="filter-label">Sort By</label>
          <select
            id="sort-by"
            v-model="sortBy"
            class="form-control"
            @change="onFilterChange"
            aria-label="Sort by"
          >
            <option value="CREATED_AT">Creation Date</option>
            <option value="TEXT">Card Text</option>
            <option value="NEXT_REVIEW_DATE">Next Review Date</option>
            <option value="EASE_FACTOR">Ease Factor</option>
          </select>
          <select
            id="sort-order"
            v-model="sortOrder"
            class="form-control"
            @change="onFilterChange"
            aria-label="Sort order"
          >
            <option value="DESC">Descending</option>
            <option value="ASC">Ascending</option>
          </select>
        </div>
        
        <!-- Clear Filters Button -->
        <button
          @click="clearFilters"
          class="btn btn-outline"
          aria-label="Clear all filters"
        >
          Clear All Filters
        </button>
      </div>
    </section>
    
    <!-- Cards List Section -->
    <section class="cards-list-section">
      <div class="section-header">
        <h2 class="section-title">Cards ({{ filteredCards.length }})</h2>
        <div class="cards-actions">
          <button
            @click="exportCards"
            class="btn"
            :disabled="filteredCards.length === 0"
            aria-label="Export selected cards"
            :aria-disabled="filteredCards.length === 0"
          >
            Export Selected
          </button>
          <button
            @click="batchActionsDialog = true"
            class="btn btn-secondary"
            :disabled="selectedCards.length === 0"
            aria-label="Batch actions"
            :aria-disabled="selectedCards.length === 0"
            :aria-describedby="`selected-count-${selectedCards.length}`"
          >
            Batch Actions
          </button>
          <span id="selected-count-0" class="sr-only">No cards selected</span>
          <span v-if="selectedCards.length > 0" :id="`selected-count-${selectedCards.length}`" class="sr-only">{{ selectedCards.length }} cards selected</span>
        </div>
      </div>
      
      <!-- Loading State -->
      <div v-if="loading" class="loading-state" aria-busy="true" aria-label="Loading cards">
        <div class="spinner"></div>
        <p>Loading cards...</p>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="filteredCards.length === 0" class="empty-state" aria-label="No cards found">
        <div class="empty-icon">📝</div>
        <h3>No cards found</h3>
        <p>Try adjusting your search or filter criteria</p>
      </div>
      
      <!-- Cards Grid -->
      <div v-else class="cards-grid" aria-label="Card list">
        <div
          v-for="card in paginatedCards"
          :key="card.id"
          class="content-card"
          :aria-label="`Card: ${card.text}`"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="card-select">
              <input
                type="checkbox"
                :id="`card-checkbox-${card.id}`"
                :value="card.id"
                v-model="selectedCards"
                aria-label="Select card"
              />
            </div>
            <div class="card-meta">
              <span class="badge card-type-badge" :class="card.cardType.toLowerCase()">
                {{ card.cardType }}
              </span>
              <span class="badge card-deck" v-if="card.deckName">
                {{ card.deckName }}
              </span>
            </div>
          </div>
          
          <!-- Card Content -->
          <div class="card-content">
            <div class="card-front">
              <h3 class="card-front-text">{{ card.text }}</h3>
              <button
                @click="speakText(card.text)"
                class="btn-speak"
                title="Speak this text"
                aria-label="Speak card front"
                tabindex="0"
                @keydown.enter="speakText(card.text)"
                @keydown.space="speakText(card.text)"
              >
                🔊
              </button>
            </div>
            <div class="card-back">
              <p class="card-back-text">{{ card.backText }}</p>
              <button
                @click="speakText(card.backText)"
                class="btn-speak"
                title="Speak this text"
                aria-label="Speak card back"
                tabindex="0"
                @keydown.enter="speakText(card.backText)"
                @keydown.space="speakText(card.backText)"
              >
                🔊
              </button>
            </div>
          </div>
          
          <!-- Card Tags -->
          <div class="card-tags" v-if="card.tags" aria-label="Card tags">
            <span
              v-for="tag in card.tags.split(' ').filter(Boolean)"
              :key="tag"
              class="badge"
            >
              {{ tag }}
            </span>
          </div>
          
          <!-- Card Stats -->
          <div class="card-stats" role="group" aria-label="Card statistics">
            <div class="stat-item" :aria-label="`Ease factor: ${card.easeFactor.toFixed(1)}`">
              <span class="stat-label">EF:</span>
              <span class="stat-value">{{ card.easeFactor.toFixed(1) }}</span>
            </div>
            <div class="stat-item" :aria-label="`Repetitions: ${card.repetitionCount}`">
              <span class="stat-label">Reps:</span>
              <span class="stat-value">{{ card.repetitionCount }}</span>
            </div>
            <div class="stat-item" :aria-label="`Interval: ${card.intervalDays} days`">
              <span class="stat-label">Interval:</span>
              <span class="stat-value">{{ card.intervalDays }}d</span>
            </div>
            <div class="stat-item" v-if="card.nextReviewDate" :aria-label="`Next review: ${formatDate(card.nextReviewDate)}`">
              <span class="stat-label">Next:</span>
              <span class="stat-value">{{ formatDate(card.nextReviewDate) }}</span>
            </div>
          </div>
          
          <!-- Card Actions -->
          <div class="card-actions" role="group" aria-label="Card actions">
            <button
              @click="editCard(card)"
              class="btn-action edit"
              title="Edit card"
              aria-label="Edit card"
              tabindex="0"
              @keydown.enter="editCard(card)"
              @keydown.space="editCard(card)"
            >
              ✏️
            </button>
            <button
              @click="deleteCard(card.id)"
              class="btn-action delete"
              title="Delete card"
              aria-label="Delete card"
              tabindex="0"
              @keydown.enter="deleteCard(card.id)"
              @keydown.space="deleteCard(card.id)"
            >
              🗑️
            </button>
            <button
              @click="suspendCard(card.id, !card.isActive)"
              class="btn-action"
              :class="card.isActive ? 'suspend' : 'unsuspend'"
              :title="card.isActive ? 'Suspend card' : 'Unsuspend card'"
              :aria-label="card.isActive ? 'Suspend card' : 'Unsuspend card'"
              tabindex="0"
              @keydown.enter="suspendCard(card.id, !card.isActive)"
              @keydown.space="suspendCard(card.id, !card.isActive)"
            >
              {{ card.isActive ? '⏸️' : '▶️' }}
            </button>
          </div>
        </div>
      </div>
      
      <!-- Pagination -->
      <div v-if="filteredCards.length > 0" class="pagination" role="navigation" aria-label="Pagination">
        <button
          @click="currentPage = 1"
          class="btn-page"
          :disabled="currentPage === 1"
          aria-label="First page"
          :aria-disabled="currentPage === 1"
          tabindex="0"
          @keydown.enter="currentPage = 1"
          @keydown.space="currentPage = 1"
        >
          First
        </button>
        <button
          @click="currentPage--"
          class="btn-page"
          :disabled="currentPage === 1"
          aria-label="Previous page"
          :aria-disabled="currentPage === 1"
          tabindex="0"
          @keydown.enter="currentPage--"
          @keydown.space="currentPage--"
        >
          Previous
        </button>
        <span class="page-info" aria-label="Current page">
          Page {{ currentPage }} of {{ totalPages }}
        </span>
        <button
          @click="currentPage++"
          class="btn-page"
          :disabled="currentPage === totalPages"
          aria-label="Next page"
          :aria-disabled="currentPage === totalPages"
          tabindex="0"
          @keydown.enter="currentPage++"
          @keydown.space="currentPage++"
        >
          Next
        </button>
        <button
          @click="currentPage = totalPages"
          class="btn-page"
          :disabled="currentPage === totalPages"
          aria-label="Last page"
          :aria-disabled="currentPage === totalPages"
          tabindex="0"
          @keydown.enter="currentPage = totalPages"
          @keydown.space="currentPage = totalPages"
        >
          Last
        </button>
      </div>
    </section>
    
    <!-- Edit Card Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal" aria-hidden="false" role="dialog" aria-modal="true" aria-labelledby="edit-modal-title">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3 id="edit-modal-title">Edit Card</h3>
          <button @click="closeEditModal" class="close-btn" title="Close modal" aria-label="Close modal" tabindex="0" @keydown.enter="closeEditModal" @keydown.space="closeEditModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveCardChanges" class="card-form">
            <div class="form-group">
              <label for="edit-front" class="form-label">Front</label>
              <input
                id="edit-front"
                v-model="editForm.text"
                type="text"
                class="form-control"
                required
                aria-required="true"
              />
            </div>
            <div class="form-group">
              <label for="edit-back" class="form-label">Back</label>
              <textarea
                id="edit-back"
                v-model="editForm.backText"
                class="form-control"
                rows="3"
                required
                aria-required="true"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="edit-card-type" class="form-label">Card Type</label>
              <select
                id="edit-card-type"
                v-model="editForm.cardType"
                class="form-control"
              >
                <option value="BASIC">Basic</option>
                <option value="REVERSE">Reverse</option>
                <option value="BASIC_AND_REVERSE">Basic & Reverse</option>
              </select>
            </div>
            <div class="form-group">
              <label for="edit-deck" class="form-label">Deck</label>
              <select
                id="edit-deck"
                v-model="editForm.deckId"
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
            <div class="form-group">
              <label for="edit-tags" class="form-label">Tags</label>
              <input
                id="edit-tags"
                v-model="editForm.tags"
                type="text"
                class="form-control"
                placeholder="Enter tags separated by spaces"
                aria-describedby="tags-hint"
              />
              <p id="tags-hint" class="form-help">Separate tags with spaces</p>
            </div>
            <div class="form-actions">
              <button type="button" @click="closeEditModal" class="btn btn-secondary" tabindex="0">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="savingCard" :aria-busy="savingCard" tabindex="0">
                {{ savingCard ? 'Saving...' : 'Save Changes' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- Batch Actions Modal -->
    <div v-if="batchActionsDialog" class="modal-overlay" @click="batchActionsDialog = false" aria-hidden="false" role="dialog" aria-modal="true" aria-labelledby="batch-modal-title">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3 id="batch-modal-title">Batch Actions</h3>
          <button @click="batchActionsDialog = false" class="close-btn" title="Close modal" aria-label="Close modal" tabindex="0" @keydown.enter="batchActionsDialog = false" @keydown.space="batchActionsDialog = false">&times;</button>
        </div>
        <div class="modal-body">
          <p>Selected {{ selectedCards.length }} cards. Choose an action:</p>
          <div class="batch-actions-list" role="list" aria-label="Batch actions">
            <button
              @click="batchDeleteCards"
              class="btn-action-item danger"
              role="listitem"
              aria-label="Delete selected cards"
              tabindex="0"
              @keydown.enter="batchDeleteCards"
              @keydown.space="batchDeleteCards"
            >
              🗑️ Delete Selected Cards
            </button>
            <button
              @click="batchSuspendCards"
              class="btn-action-item"
              role="listitem"
              aria-label="Suspend selected cards"
              tabindex="0"
              @keydown.enter="batchSuspendCards"
              @keydown.space="batchSuspendCards"
            >
              ⏸️ Suspend Selected Cards
            </button>
            <button
              @click="batchUnsuspendCards"
              class="btn-action-item"
              role="listitem"
              aria-label="Unsuspend selected cards"
              tabindex="0"
              @keydown.enter="batchUnsuspendCards"
              @keydown.space="batchUnsuspendCards"
            >
              ▶️ Unsuspend Selected Cards
            </button>
            <button
              @click="batchChangeDeck"
              class="btn-action-item"
              role="listitem"
              aria-label="Change deck for selected cards"
              tabindex="0"
              @keydown.enter="batchChangeDeck"
              @keydown.space="batchChangeDeck"
            >
              📁 Change Deck for Selected Cards
            </button>
            <button
              @click="batchAddTags"
              class="btn-action-item"
              role="listitem"
              aria-label="Add tags to selected cards"
              tabindex="0"
              @keydown.enter="batchAddTags"
              @keydown.space="batchAddTags"
            >
              🏷️ Add Tags to Selected Cards
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useApiService } from '../composables/useApiService'
import { useSpeechService } from '../composables/useSpeechService'
import { useDeckService } from '../services/deckService'
import { confirmCardDelete, confirmBatchDelete } from '../utils/confirmDialog'

export default {
  name: 'CardBrowser',
  setup() {
    const { apiService } = useApiService()
    const { speakText } = useSpeechService()
    const { getAllDecks } = useDeckService()
    
    // Reactive state
    const loading = ref(false)
    const savingCard = ref(false)
    const cards = ref([])
    const userDecks = ref([])
    const availableTags = ref([])
    
    // Search and filter state
    const searchQuery = ref('')
    const selectedDeckId = ref('')
    const selectedCardType = ref('')
    const selectedStatus = ref('')
    const selectedTags = ref([])
    const sortBy = ref('CREATED_AT')
    const sortOrder = ref('DESC')
    
    // Pagination state
    const currentPage = ref(1)
    const pageSize = ref(20)
    
    // Selection state
    const selectedCards = ref([])
    
    // Modal states
    const showEditModal = ref(false)
    const batchActionsDialog = ref(false)
    
    // Edit form state
    const editForm = ref({
      id: null,
      text: '',
      backText: '',
      cardType: 'BASIC',
      deckId: '',
      tags: ''
    })
    
    // Computed properties
    const filteredCards = computed(() => {
      let result = [...cards.value]
      
      // Filter by search query
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(card => 
          card.text.toLowerCase().includes(query) ||
          card.backText.toLowerCase().includes(query) ||
          (card.tags && card.tags.toLowerCase().includes(query))
        )
      }
      
      // Filter by deck
      if (selectedDeckId.value) {
        result = result.filter(card => card.deckId === parseInt(selectedDeckId.value))
      }
      
      // Filter by card type
      if (selectedCardType.value) {
        result = result.filter(card => card.cardType === selectedCardType.value)
      }
      
      // Filter by status
      if (selectedStatus.value) {
        const isActive = selectedStatus.value === 'ACTIVE'
        result = result.filter(card => card.isActive === isActive)
      }
      
      // Filter by tags
      if (selectedTags.value.length > 0) {
        result = result.filter(card => {
          if (!card.tags) return false
          const cardTags = card.tags.split(' ')
          return selectedTags.value.some(tagId => 
            cardTags.includes(availableTags.value.find(t => t.id === tagId)?.name)
          )
        })
      }
      
      // Sort results
      result.sort((a, b) => {
        let comparison = 0
        
        switch (sortBy.value) {
          case 'CREATED_AT':
            comparison = new Date(b.createdAt) - new Date(a.createdAt)
            break
          case 'TEXT':
            comparison = a.text.localeCompare(b.text)
            break
          case 'NEXT_REVIEW_DATE':
            if (!a.nextReviewDate) return 1
            if (!b.nextReviewDate) return -1
            comparison = new Date(a.nextReviewDate) - new Date(b.nextReviewDate)
            break
          case 'EASE_FACTOR':
            comparison = b.easeFactor - a.easeFactor
            break
          default:
            comparison = 0
        }
        
        return sortOrder.value === 'ASC' ? comparison : -comparison
      })
      
      return result
    })
    
    const totalPages = computed(() => {
      return Math.ceil(filteredCards.value.length / pageSize.value)
    })
    
    const paginatedCards = computed(() => {
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      return filteredCards.value.slice(startIndex, endIndex)
    })
    
    // Methods
    const loadCards = async () => {
      loading.value = true
      try {
        const response = await apiService.get('/vocabulary/cards')
        cards.value = response.data || []
      } catch (error) {
        console.error('Error loading cards:', error)
      } finally {
        loading.value = false
      }
    }
    
    const loadDecks = async () => {
      try {
        const decks = await getAllDecks()
        userDecks.value = decks
      } catch (error) {
        console.error('Error loading decks:', error)
      }
    }
    
    const loadTags = async () => {
      try {
        const response = await apiService.get('/tags')
        availableTags.value = response.data || []
      } catch (error) {
        console.error('Error loading tags:', error)
      }
    }
    
    const onSearchChange = () => {
      currentPage.value = 1
    }
    
    const onFilterChange = () => {
      currentPage.value = 1
    }
    
    const clearSearch = () => {
      searchQuery.value = ''
      currentPage.value = 1
    }
    
    const clearFilters = () => {
      selectedDeckId.value = ''
      selectedCardType.value = ''
      selectedStatus.value = ''
      selectedTags.value = []
      sortBy.value = 'CREATED_AT'
      sortOrder.value = 'DESC'
      currentPage.value = 1
    }
    
    const toggleTagSelection = (tagId) => {
      const index = selectedTags.value.indexOf(tagId)
      if (index > -1) {
        selectedTags.value.splice(index, 1)
      } else {
        selectedTags.value.push(tagId)
      }
      currentPage.value = 1
    }
    
    const editCard = (card) => {
      editForm.value = {
        id: card.id,
        text: card.text,
        backText: card.backText,
        cardType: card.cardType || 'BASIC',
        deckId: card.deckId || '',
        tags: card.tags || ''
      }
      showEditModal.value = true
    }
    
    const saveCardChanges = async () => {
      savingCard.value = true
      try {
        await apiService.put(`/vocabulary/cards/${editForm.value.id}`, {
          text: editForm.value.text,
          backText: editForm.value.backText,
          cardType: editForm.value.cardType,
          deckId: editForm.value.deckId,
          tags: editForm.value.tags
        })
        
        // Refresh cards
        await loadCards()
        showEditModal.value = false
      } catch (error) {
        console.error('Error saving card changes:', error)
        alert('Failed to save card changes. Please try again.')
      } finally {
        savingCard.value = false
      }
    }
    
    const deleteCard = async (cardId) => {
      const card = cards.value.find(c => c.id === cardId)
      if (!card) return
      
      if (!await confirmCardDelete(card)) return
      
      try {
        await apiService.delete(`/vocabulary/cards/${cardId}`)
        await loadCards()
      } catch (error) {
        console.error('Error deleting card:', error)
        alert('Failed to delete card. Please try again.')
      }
    }
    
    const suspendCard = async (cardId, shouldSuspend) => {
      try {
        await apiService.put(`/vocabulary/cards/${cardId}/status`, {
          isActive: !shouldSuspend
        })
        await loadCards()
      } catch (error) {
        console.error('Error updating card status:', error)
        alert('Failed to update card status. Please try again.')
      }
    }
    
    const closeEditModal = () => {
      showEditModal.value = false
      editForm.value = {
        id: null,
        text: '',
        backText: '',
        cardType: 'BASIC',
        deckId: '',
        tags: ''
      }
    }
    
    const batchDeleteCards = async () => {
      if (!await confirmBatchDelete(selectedCards.value.length, 'cards')) return
      
      try {
        for (const cardId of selectedCards.value) {
          await apiService.delete(`/vocabulary/cards/${cardId}`)
        }
        selectedCards.value = []
        batchActionsDialog.value = false
        await loadCards()
      } catch (error) {
        console.error('Error deleting cards:', error)
        alert('Failed to delete cards. Please try again.')
      }
    }
    
    const batchSuspendCards = async () => {
      try {
        for (const cardId of selectedCards.value) {
          await apiService.put(`/cards/${cardId}/status`, { isActive: false })
        }
        selectedCards.value = []
        batchActionsDialog.value = false
        await loadCards()
      } catch (error) {
        console.error('Error suspending cards:', error)
        alert('Failed to suspend cards. Please try again.')
      }
    }
    
    const batchUnsuspendCards = async () => {
      try {
        for (const cardId of selectedCards.value) {
          await apiService.put(`/cards/${cardId}/status`, { isActive: true })
        }
        selectedCards.value = []
        batchActionsDialog.value = false
        await loadCards()
      } catch (error) {
        console.error('Error unsuspending cards:', error)
        alert('Failed to unsuspend cards. Please try again.')
      }
    }
    
    const batchChangeDeck = async () => {
      const deckOptions = userDecks.value.map(deck => `(${deck.id}) ${deck.name}`).join('\n')
      const deckIdStr = prompt(`Enter deck ID to move selected cards to:\n\n${deckOptions}`)
      if (deckIdStr) {
        const deckId = parseInt(deckIdStr)
        if (isNaN(deckId)) {
          alert('Please enter a valid deck ID')
          return
        }
        
        try {
          for (const cardId of selectedCards.value) {
            await apiService.put(`/cards/${cardId}`, {
              deckId: deckId
            })
          }
          selectedCards.value = []
          batchActionsDialog.value = false
          await loadCards()
          alert('Successfully changed deck for selected cards')
        } catch (error) {
          console.error('Error changing deck:', error)
          alert('Failed to change deck for selected cards. Please try again.')
        }
      }
    }
    
    const batchAddTags = async () => {
      const tags = prompt('Enter tags to add (separated by spaces):')
      if (tags) {
        try {
          for (const cardId of selectedCards.value) {
            // First get the current card to preserve existing tags
            const response = await apiService.get(`/cards/${cardId}`)
            const currentCard = response.data
            const existingTags = currentCard.tags || ''
            const newTags = [...new Set([...existingTags.split(' '), ...tags.split(' ')].filter(Boolean))].join(' ')
            
            await apiService.put(`/cards/${cardId}`, {
              tags: newTags
            })
          }
          selectedCards.value = []
          batchActionsDialog.value = false
          await loadCards()
          alert('Successfully added tags to selected cards')
        } catch (error) {
          console.error('Error adding tags:', error)
          alert('Failed to add tags to selected cards. Please try again.')
        }
      }
    }
    
    const exportCards = async () => {
      try {
        const response = await apiService.post('/import-export/export-cards', {
          cardIds: selectedCards.value
        })
        
        // Create download link for the exported file
        const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `cards-export-${new Date().toISOString().split('T')[0]}.json`
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
        
        alert('Successfully exported selected cards')
      } catch (error) {
        console.error('Error exporting cards:', error)
        alert('Failed to export selected cards. Please try again.')
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
        loadCards(),
        loadDecks(),
        loadTags()
      ])
    })
    
    return {
      // State
      loading,
      savingCard,
      cards,
      userDecks,
      availableTags,
      searchQuery,
      selectedDeckId,
      selectedCardType,
      selectedStatus,
      selectedTags,
      sortBy,
      sortOrder,
      currentPage,
      pageSize,
      selectedCards,
      showEditModal,
      batchActionsDialog,
      editForm,
      
      // Computed
      filteredCards,
      totalPages,
      paginatedCards,
      
      // Methods
      speakText,
      onSearchChange,
      onFilterChange,
      clearSearch,
      clearFilters,
      toggleTagSelection,
      editCard,
      saveCardChanges,
      deleteCard,
      suspendCard,
      closeEditModal,
      batchDeleteCards,
      batchSuspendCards,
      batchUnsuspendCards,
      batchChangeDeck,
      batchAddTags,
      exportCards,
      formatDate
    }
  }
}
</script>

<style scoped>
.card-browser {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);
}

.page-title {
  text-align: center;
  color: var(--dark-color);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  margin: var(--spacing-xl) 0 var(--spacing-lg);
  background: linear-gradient(90deg, var(--primary-color), var(--primary-700));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.search-filter-section {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  color: var(--dark-color);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin: 0 0 var(--spacing-lg) 0;
}

.search-input-group {
  position: relative;
  margin-bottom: var(--spacing-lg);
}

.search-input-group .form-control {
  padding-right: var(--spacing-xl);
}

.btn-clear-search {
  position: absolute;
  right: var(--spacing-sm);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: var(--font-size-xl);
  cursor: pointer;
  color: var(--text-tertiary);
  padding: var(--spacing-xs);
  border-radius: var(--radius-full);
  transition: var(--transition);
  min-width: 44px;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-clear-search:hover,
.btn-clear-search:focus {
  background: var(--bg-tertiary);
  color: var(--dark-color);
  outline: 2px solid var(--primary-300);
  outline-offset: 2px;
}

.filter-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.filter-label {
  font-weight: var(--font-weight-medium);
  color: var(--dark-color);
  font-size: var(--font-size-sm);
}

.tag-filter-section {
  margin-bottom: var(--spacing-lg);
}

.tag-selector {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
}

.tag-option {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: var(--transition);
  background: var(--bg-secondary);
  color: var(--dark-color);
  min-height: 44px;
  display: inline-flex;
  align-items: center;
}

.tag-option:hover,
.tag-option:focus {
  border-color: var(--primary-color);
  background: var(--primary-50);
  outline: 2px solid var(--primary-300);
  outline-offset: 2px;
}

.tag-option.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.sort-options {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
}

.cards-list-section {
  margin-bottom: var(--spacing-xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.cards-actions {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: var(--spacing-xl);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--surface-border);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--bg-tertiary);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--spacing-md);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-md);
}

.empty-state h3 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--dark-color);
}

.empty-state p {
  margin: 0;
  color: var(--text-tertiary);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--spacing-lg);
}

.content-card {
  transition: var(--transition);
}

.content-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.card-select input[type="checkbox"] {
  transform: scale(1.2);
  accent-color: var(--primary-color);
}

.card-meta {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.card-type-badge {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-type-badge.basic {
  background: var(--info-50);
  color: var(--info-color);
}

.card-type-badge.reverse {
  background: var(--success-50);
  color: var(--success-color);
}

.card-type-badge.basic_and_reverse {
  background: var(--warning-50);
  color: var(--warning-color);
}

.card-deck {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  background: var(--bg-tertiary);
}

.card-content {
  margin-bottom: var(--spacing-md);
}

.card-front {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.card-front-text {
  margin: 0;
  color: var(--dark-color);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  flex: 1;
}

.btn-speak {
  background: none;
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-full);
  width: 44px;
  height: 44px;
  font-size: var(--font-size-lg);
  cursor: pointer;
  transition: var(--transition);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-speak:hover,
.btn-speak:focus {
  border-color: var(--primary-color);
  background: var(--primary-50);
  outline: 2px solid var(--primary-300);
  outline-offset: 2px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.card-tags .badge {
  font-size: var(--font-size-xs);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border: 1px solid var(--surface-border);
}

.card-stats {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  gap: var(--spacing-xs);
  align-items: center;
  font-size: var(--font-size-sm);
}

.stat-label {
  font-weight: var(--font-weight-semibold);
  color: var(--text-tertiary);
}

.stat-value {
  color: var(--dark-color);
  font-weight: var(--font-weight-medium);
}

.card-actions {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
}

.btn-action {
  background: none;
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-md);
  width: 44px;
  height: 44px;
  font-size: var(--font-size-md);
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-action:hover,
.btn-action:focus {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  outline: 2px solid var(--primary-300);
  outline-offset: 2px;
}

.btn-action.edit:hover {
  border-color: var(--warning-color);
  background: var(--warning-50);
}

.btn-action.delete:hover {
  border-color: var(--error-color);
  background: var(--error-50);
}

.btn-action.suspend:hover {
  border-color: var(--text-tertiary);
  background: var(--bg-tertiary);
}

.btn-action.unsuspend:hover {
  border-color: var(--success-color);
  background: var(--success-50);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
  flex-wrap: wrap;
}

.btn-page {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-md);
  background: var(--surface-primary);
  color: var(--dark-color);
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
  transition: var(--transition);
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-page:hover:not(:disabled),
.btn-page:focus:not(:disabled) {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  outline: none;
}

.btn-page:focus {
  outline: 2px solid var(--primary-300);
  outline-offset: 2px;
}

.btn-page:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.page-info {
  font-weight: var(--font-weight-semibold);
  color: var(--dark-color);
}

.batch-actions-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.btn-action-item {
  padding: var(--spacing-md) var(--spacing-lg);
  border: 2px solid var(--surface-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  cursor: pointer;
  transition: var(--transition);
  background: var(--surface-primary);
  color: var(--dark-color);
  text-align: left;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  min-height: 44px;
}

.btn-action-item:hover,
.btn-action-item:focus {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
  background: var(--primary-50);
  outline: 2px solid var(--primary-300);
  outline-offset: 2px;
}

.btn-action-item.danger:hover,
.btn-action-item.danger:focus {
  border-color: var(--error-color);
  background: var(--error-50);
}

/* Responsive design */
@media (max-width: 768px) {
  .card-browser {
    padding: var(--spacing-md);
  }
  
  .cards-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-options {
    grid-template-columns: 1fr;
  }
  
  .sort-options {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .cards-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .card-stats {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }
  
  .pagination {
    gap: var(--spacing-sm);
  }
  
  .btn-page {
    padding: var(--spacing-sm);
    font-size: var(--font-size-sm);
  }
}

@media (max-width: 480px) {
  .card-browser {
    padding: var(--spacing-sm);
  }
  
  .content-card {
    padding: var(--spacing-md);
  }
  
  .cards-actions {
    flex-direction: column;
  }
  
  .cards-actions .btn {
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .btn {
    width: 100%;
  }
}
</style>
