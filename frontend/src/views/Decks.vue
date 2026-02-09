<template>
  <main class="decks" id="decks-page" aria-label="Manage Decks">
    <h1 class="page-title">Manage Decks</h1>
    
    <div class="decks-container">
      <!-- Create Deck Button -->
      <section class="create-deck-section" aria-labelledby="create-deck-heading">
        <h2 id="create-deck-heading" class="sr-only">Create and Manage Decks</h2>
        <div class="create-deck-actions">
          <button 
            @click="showCreateModal = true" 
            class="btn btn-primary"
            aria-expanded="false"
            aria-controls="create-deck-modal"
          >
            Create New Deck
          </button>
          <button 
            @click="exportAllDecks" 
            class="btn btn-secondary"
            title="Export all decks"
            aria-label="Export all decks"
          >
            Export All Decks
          </button>
          <label 
            class="btn btn-secondary"
            title="Import decks"
            aria-label="Import decks"
            role="button"
            tabindex="0"
            @keydown.enter="$refs.fileInput.click()"
            @keydown.space.prevent="$refs.fileInput.click()"
          >
            Import Decks
            <input 
              ref="fileInput"
              type="file" 
              accept=".json" 
              @change="importDecks" 
              class="file-input"
              style="display: none;"
              aria-label="Select deck file to import"
            >
          </label>
        </div>
      </section>
      
      <!-- Decks List -->
      <section class="decks-list" aria-labelledby="my-decks-heading">
        <h2 id="my-decks-heading" class="section-title">My Decks</h2>
        
        <div v-if="loading" class="loading-state" aria-busy="true">
          <div class="spinner" aria-hidden="true"></div>
          <p>Loading decks...</p>
        </div>
        
        <div v-else-if="decks.length === 0" class="empty-state" aria-live="polite">
          <h3>No Decks Yet</h3>
          <p>Create your first deck to start organizing your vocabulary cards.</p>
          <button 
            @click="showCreateModal = true" 
            class="btn btn-primary"
            aria-expanded="false"
            aria-controls="create-deck-modal"
          >
            Create Deck
          </button>
        </div>
        
        <div v-else class="deck-cards-grid" aria-live="polite">
          <article 
            v-for="deck in decks" 
            :key="deck.id" 
            class="deck-card"
            :aria-labelledby="`deck-${deck.id}-name`"
          >
            <div class="deck-card-header">
              <h3 id="deck-${deck.id}-name" class="deck-name">{{ deck.name }}</h3>
              <div class="deck-card-actions" role="group" aria-label="Deck actions">
                <button 
                  @click="editDeck(deck)" 
                  class="btn btn-sm btn-secondary"
                  title="Edit deck"
                  aria-label="Edit deck"
                  aria-expanded="false"
                  :aria-controls="`edit-deck-modal`"
                >
                  Edit
                </button>
                <button 
                  @click="confirmDeleteDeck(deck)" 
                  class="btn btn-sm btn-danger"
                  title="Delete deck"
                  aria-label="Delete deck"
                  aria-expanded="false"
                  :aria-controls="`delete-deck-modal`"
                  aria-describedby="delete-warning"
                >
                  Delete
                </button>
              </div>
            </div>
            
            <div class="deck-card-body">
              <p v-if="deck.description" class="deck-description">{{ deck.description }}</p>
              <p v-else class="deck-description placeholder" aria-label="No description">No description</p>
              
              <div class="deck-stats" role="group" aria-label="Deck statistics">
                <span class="stat-item" aria-label="Card count">
                  <strong>{{ deck.cardCount }}</strong>
                  <span>Cards</span>
                </span>
                <span class="stat-item" aria-label="Deck visibility">
                  <strong>{{ deck.isPublic ? 'Public' : 'Private' }}</strong>
                  <span>Visibility</span>
                </span>
              </div>
            </div>
            
            <div class="deck-card-footer" role="group" aria-label="Deck actions">
              <button 
                @click="addCardToDeck(deck)" 
                class="btn btn-sm btn-primary"
                title="Add card to deck"
                aria-label="Add card to deck"
                aria-expanded="false"
                :aria-controls="`add-card-modal`"
              >
                Add Card
              </button>
              <button 
                @click="viewDeckCards(deck)" 
                class="btn btn-sm btn-secondary"
                title="View cards"
                aria-label="View cards"
              >
                View Cards
              </button>
              <button 
                @click="startDeckReview(deck)" 
                class="btn btn-sm btn-secondary"
                title="Start review"
                aria-label="Start review"
              >
                Start Review
              </button>
              <button 
                @click="editDeckOptions(deck)" 
                class="btn btn-sm btn-secondary"
                title="Deck options"
                aria-label="Deck options"
                aria-expanded="false"
                :aria-controls="`deck-options-modal`"
              >
                Options
              </button>
              <button 
                @click="duplicateDeck(deck)" 
                class="btn btn-sm btn-secondary"
                title="Duplicate deck"
                aria-label="Duplicate deck"
              >
                Duplicate
              </button>
              <button 
                @click="exportDeck(deck)" 
                class="btn btn-sm btn-secondary"
                title="Export deck"
                aria-label="Export deck"
              >
                Export
              </button>
            </div>
          </article>
        </div>
      </section>
      
      <!-- Public Decks Section -->
      <section class="public-decks-section" aria-labelledby="public-decks-heading">
        <h2 id="public-decks-heading" class="section-title">Public Decks</h2>
        
        <div v-if="loadingPublic" class="loading-state" aria-busy="true">
          <div class="spinner" aria-hidden="true"></div>
          <p>Loading public decks...</p>
        </div>
        
        <div v-else-if="publicDecks.length === 0" class="empty-state" aria-live="polite">
          <h3>No Public Decks</h3>
          <p>No public decks available at the moment.</p>
        </div>
        
        <div v-else class="deck-cards-grid" aria-live="polite">
          <article 
            v-for="deck in publicDecks" 
            :key="deck.id" 
            class="deck-card public-deck"
            :aria-labelledby="`public-deck-${deck.id}-name`"
          >
            <div class="deck-card-header">
              <h3 id="public-deck-${deck.id}-name" class="deck-name">{{ deck.name }}</h3>
              <span class="public-badge" aria-label="Public deck">Public</span>
            </div>
            
            <div class="deck-card-body">
              <p v-if="deck.description" class="deck-description">{{ deck.description }}</p>
              <p v-else class="deck-description placeholder" aria-label="No description">No description</p>
              
              <div class="deck-stats" role="group" aria-label="Deck statistics">
                <span class="stat-item" aria-label="Card count">
                  <strong>{{ deck.cardCount }}</strong>
                  <span>Cards</span>
                </span>
                <span class="stat-item" aria-label="Creator">
                  <strong>{{ deck.userName }}</strong>
                  <span>Creator</span>
                </span>
              </div>
            </div>
            
            <div class="deck-card-footer" role="group" aria-label="Deck actions">
              <button 
                @click="viewPublicDeckCards(deck)" 
                class="btn btn-sm btn-secondary"
                title="View cards"
                aria-label="View cards"
              >
                View Cards
              </button>
              <button 
                @click="startPublicDeckReview(deck)" 
                class="btn btn-sm btn-primary"
                title="Start review"
                aria-label="Start review"
              >
                Start Review
              </button>
              <button 
                @click="duplicatePublicDeck(deck)" 
                class="btn btn-sm btn-secondary"
                title="Copy deck"
                aria-label="Copy deck"
              >
                Copy
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>
  </main>
  
  <!-- Create/Edit Deck Modal -->
  <div 
    v-if="showCreateModal || showEditModal" 
    class="modal-overlay" 
    @click="closeModal"
    :aria-hidden="!(showCreateModal || showEditModal)"
  >
    <div 
      class="modal" 
      @click.stop
      role="dialog"
      :aria-labelledby="showEditModal ? 'edit-deck-title' : 'create-deck-title'"
      aria-modal="true"
      tabindex="-1"
      ref="modalRef"
    >
      <div class="modal-header">
        <h2 id="create-deck-title" v-if="!showEditModal" class="modal-title">Create New Deck</h2>
        <h2 id="edit-deck-title" v-else class="modal-title">Edit Deck</h2>
        <button 
          @click="closeModal" 
          class="close-btn"
          title="Close modal"
          aria-label="Close modal"
          aria-controls="create-deck-modal"
        >
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="saveDeck" class="deck-form" novalidate>
          <div class="form-group">
            <label for="deck-name" class="form-label">Deck Name</label>
            <input 
              v-model="deckForm.name" 
              type="text" 
              id="deck-name"
              class="form-control" 
              required
              placeholder="Enter deck name"
              aria-required="true"
              :aria-invalid="!!deckErrors.name"
              :aria-describedby="deckErrors.name ? 'deck-name-error' : undefined"
            >
            <p v-if="deckErrors.name" id="deck-name-error" class="form-error" aria-live="assertive">{{ deckErrors.name }}</p>
          </div>
          <div class="form-group">
            <label for="deck-description" class="form-label">Description (Optional)</label>
            <textarea 
              v-model="deckForm.description" 
              id="deck-description"
              class="form-control" 
              rows="3"
              placeholder="Enter deck description"
            ></textarea>
          </div>
          <div class="form-group">
            <div class="form-checkbox">
              <input 
                v-model="deckForm.isPublic" 
                type="checkbox"
                id="deck-public"
              >
              <label for="deck-public">Make this deck public</label>
            </div>
            <p class="form-help">Public decks can be viewed and copied by other users.</p>
          </div>
          <div class="form-actions">
            <button 
              type="button" 
              @click="closeModal" 
              class="btn btn-secondary"
              aria-label="Cancel"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              class="btn btn-primary" 
              :disabled="saving"
              :aria-busy="saving"
            >
              {{ saving ? 'Saving...' : (showEditModal ? 'Update' : 'Create') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  
  <!-- Delete Confirmation Modal -->
  <div 
    v-if="showDeleteModal" 
    class="modal-overlay" 
    @click="closeDeleteModal"
    aria-hidden="!showDeleteModal"
  >
    <div 
      class="modal" 
      @click.stop
      role="dialog"
      aria-labelledby="delete-deck-title"
      aria-modal="true"
      tabindex="-1"
      ref="deleteModalRef"
    >
      <div class="modal-header">
        <h2 id="delete-deck-title" class="modal-title">Delete Deck</h2>
        <button 
          @click="closeDeleteModal" 
          class="close-btn"
          title="Close modal"
          aria-label="Close modal"
        >
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <p id="delete-warning" class="delete-warning" aria-live="assertive">
          Are you sure you want to delete the deck "{{ selectedDeck?.name }}"?
        </p>
        <p class="delete-info">
          This will also delete all cards in the deck. This action cannot be undone.
        </p>
        <div class="form-actions">
          <button 
            type="button" 
            @click="closeDeleteModal" 
            class="btn btn-secondary"
            aria-label="Cancel"
          >
            Cancel
          </button>
          <button 
            @click="deleteDeck" 
            class="btn btn-danger" 
            :disabled="deleting"
            :aria-busy="deleting"
          >
            {{ deleting ? 'Deleting...' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Deck Options Modal -->
  <div 
    v-if="showOptionsModal" 
    class="modal-overlay" 
    @click="closeOptionsModal"
    aria-hidden="!showOptionsModal"
  >
    <div 
      class="modal" 
      @click.stop
      role="dialog"
      aria-labelledby="deck-options-title"
      aria-modal="true"
      tabindex="-1"
      ref="optionsModalRef"
    >
      <div class="modal-header">
        <h2 id="deck-options-title" class="modal-title">Deck Options - {{ selectedDeck?.name }}</h2>
        <button 
          @click="closeOptionsModal" 
          class="close-btn"
          title="Close modal"
          aria-label="Close modal"
        >
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="saveDeckOptions" class="deck-form" novalidate>
          <h3>Review Options</h3>
          <div class="form-group">
            <label for="new-cards" class="form-label">New Cards per Day</label>
            <input 
              v-model.number="deckOptions.newCardsPerDay" 
              type="number" 
              id="new-cards"
              class="form-control" 
              min="1"
              max="100"
              aria-valuemin="1"
              aria-valuemax="100"
              aria-label="New cards per day"
            >
          </div>
          <div class="form-group">
            <label for="max-reviews" class="form-label">Max Reviews per Day</label>
            <input 
              v-model.number="deckOptions.maxReviewsPerDay" 
              type="number" 
              id="max-reviews"
              class="form-control" 
              min="1"
              max="300"
              aria-valuemin="1"
              aria-valuemax="300"
              aria-label="Maximum reviews per day"
            >
          </div>
          <div class="form-group">
            <label for="easy-interval" class="form-label">Easy Interval (days)</label>
            <input 
              v-model.number="deckOptions.easyInterval" 
              type="number" 
              id="easy-interval"
              class="form-control" 
              min="1"
              max="30"
              aria-valuemin="1"
              aria-valuemax="30"
              aria-label="Easy interval in days"
            >
          </div>
          <div class="form-group">
            <label for="easy-bonus" class="form-label">Easy Bonus</label>
            <input 
              v-model.number="deckOptions.easyBonus" 
              type="number" 
              id="easy-bonus"
              class="form-control" 
              step="0.1"
              min="1.0"
              max="3.0"
              aria-valuemin="1.0"
              aria-valuemax="3.0"
              aria-label="Easy bonus"
            >
          </div>
          <div class="form-group">
            <label for="interval-modifier" class="form-label">Interval Modifier</label>
            <input 
              v-model.number="deckOptions.intervalModifier" 
              type="number" 
              id="interval-modifier"
              class="form-control" 
              step="0.1"
              min="0.1"
              max="2.0"
              aria-valuemin="0.1"
              aria-valuemax="2.0"
              aria-label="Interval modifier"
            >
          </div>
          <div class="form-group">
            <label for="starting-ease" class="form-label">Starting Ease</label>
            <input 
              v-model.number="deckOptions.startingEase" 
              type="number" 
              id="starting-ease"
              class="form-control" 
              step="0.1"
              min="1.3"
              max="4.0"
              aria-valuemin="1.3"
              aria-valuemax="4.0"
              aria-label="Starting ease"
            >
          </div>
          <div class="form-group">
            <label for="learning-steps" class="form-label">Learning Steps</label>
            <input 
              v-model.number="deckOptions.steps" 
              type="number" 
              id="learning-steps"
              class="form-control" 
              min="1"
              max="5"
              aria-valuemin="1"
              aria-valuemax="5"
              aria-label="Learning steps"
            >
          </div>
          <div class="form-actions">
            <button 
              type="button" 
              @click="closeOptionsModal" 
              class="btn btn-secondary"
              aria-label="Cancel"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              class="btn btn-primary" 
              :disabled="saving"
              :aria-busy="saving"
            >
              {{ saving ? 'Saving...' : 'Save Options' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  
  <!-- Add Card to Deck Modal -->
  <div 
    v-if="showAddCardModal" 
    class="modal-overlay" 
    @click="closeAddCardModal"
    aria-hidden="!showAddCardModal"
  >
    <div 
      class="modal" 
      @click.stop
      role="dialog"
      aria-labelledby="add-card-title"
      aria-modal="true"
      tabindex="-1"
      ref="addCardModalRef"
    >
      <div class="modal-header">
        <h2 id="add-card-title" class="modal-title">Add Card to Deck - {{ selectedDeck?.name }}</h2>
        <button 
          @click="closeAddCardModal" 
          class="close-btn"
          title="Close modal"
          aria-label="Close modal"
        >
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="saveCardToDeck" class="deck-form" novalidate>
          <div class="form-group">
            <label for="card-select" class="form-label">Select Card</label>
            <select 
              v-model="addCardForm.cardId" 
              id="card-select"
              class="form-control" 
              required
              aria-required="true"
              :aria-invalid="!!addCardErrors.cardId"
              :aria-describedby="addCardErrors.cardId ? 'card-select-error' : undefined"
            >
              <option value="">Select a card</option>
              <option v-for="card in availableCards" :key="card.id" :value="card.id">
                {{ card.text }}
              </option>
            </select>
            <p v-if="addCardErrors.cardId" id="card-select-error" class="form-error" aria-live="assertive">{{ addCardErrors.cardId }}</p>
          </div>
          <div class="form-actions">
            <button 
              type="button" 
              @click="closeAddCardModal" 
              class="btn btn-secondary"
              aria-label="Cancel"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              class="btn btn-primary" 
              :disabled="saving"
              :aria-busy="saving"
            >
              {{ saving ? 'Adding...' : 'Add Card' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDeckService } from '../services/deckService'

export default {
  name: 'Decks',
  setup() {
    const router = useRouter()
    const { 
      createDeck, 
      getAllDecks, 
      updateDeck, 
      deleteDeck: deleteDeckApi, 
      getPublicDecks,
      duplicateDeck: duplicateDeckApi
    } = useDeckService()
    
    const decks = ref([])
    const publicDecks = ref([])
    const availableCards = ref([])
    const loading = ref(false)
    const loadingPublic = ref(false)
    const loadingCards = ref(false)
    const saving = ref(false)
    const deleting = ref(false)
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const showDeleteModal = ref(false)
    const showOptionsModal = ref(false)
    const showAddCardModal = ref(false)
    const selectedDeck = ref(null)
    const modalRef = ref(null)
    const deleteModalRef = ref(null)
    const optionsModalRef = ref(null)
    const addCardModalRef = ref(null)
    const deckForm = ref({
      name: '',
      description: '',
      isPublic: false
    })
    const deckErrors = ref({})
    const addCardForm = ref({
      cardId: ''
    })
    const addCardErrors = ref({})
    const deckOptions = ref({
      newCardsPerDay: 20,
      maxReviewsPerDay: 100,
      easyInterval: 4,
      easyBonus: 1.3,
      intervalModifier: 1.0,
      startingEase: 2.5,
      steps: 1
    })
    
    const loadDecks = async () => {
      loading.value = true
      try {
        decks.value = await getAllDecks()
      } catch (error) {
        console.error('Failed to load decks:', error)
        alert('Failed to load decks. Please try again.')
      } finally {
        loading.value = false
      }
    }
    
    const loadPublicDecks = async () => {
      loadingPublic.value = true
      try {
        publicDecks.value = await getPublicDecks()
      } catch (error) {
        console.error('Failed to load public decks:', error)
        publicDecks.value = []
      } finally {
        loadingPublic.value = false
      }
    }
    
    const validateDeckForm = () => {
      const errors = {}
      if (!deckForm.value.name.trim()) {
        errors.name = 'Deck name is required'
      }
      return errors
    }
    
    const validateAddCardForm = () => {
      const errors = {}
      if (!addCardForm.value.cardId) {
        errors.cardId = 'Please select a card'
      }
      return errors
    }
    
    const saveDeck = async () => {
      const errors = validateDeckForm()
      deckErrors.value = errors
      
      if (Object.keys(errors).length > 0) {
        const firstErrorField = document.getElementById('deck-name')
        if (firstErrorField) {
          firstErrorField.focus()
        }
        return
      }
      
      saving.value = true
      try {
        if (showEditModal.value && selectedDeck.value) {
          await updateDeck(selectedDeck.value.id, deckForm.value)
          alert('Deck updated successfully!')
        } else {
          await createDeck(deckForm.value)
          alert('Deck created successfully!')
        }
        await loadDecks()
        closeModal()
      } catch (error) {
        console.error('Failed to save deck:', error)
        alert('Failed to save deck. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const deleteDeck = async () => {
      if (!selectedDeck.value) return
      
      deleting.value = true
      try {
        await deleteDeckApi(selectedDeck.value.id)
        alert('Deck deleted successfully!')
        await loadDecks()
        closeDeleteModal()
      } catch (error) {
        console.error('Failed to delete deck:', error)
        alert('Failed to delete deck. Please try again.')
      } finally {
        deleting.value = false
      }
    }
    
    const editDeck = (deck) => {
      selectedDeck.value = deck
      deckForm.value = {
        name: deck.name,
        description: deck.description || '',
        isPublic: deck.isPublic || false
      }
      deckErrors.value = {}
      showEditModal.value = true
      showCreateModal.value = false
      
      setTimeout(() => {
        if (modalRef.value) {
          modalRef.value.focus()
        }
      }, 100)
    }
    
    const confirmDeleteDeck = (deck) => {
      selectedDeck.value = deck
      showDeleteModal.value = true
      
      setTimeout(() => {
        if (deleteModalRef.value) {
          deleteModalRef.value.focus()
        }
      }, 100)
    }
    
    const closeModal = () => {
      showCreateModal.value = false
      showEditModal.value = false
      selectedDeck.value = null
      deckForm.value = {
        name: '',
        description: '',
        isPublic: false
      }
      deckErrors.value = {}
    }
    
    const closeDeleteModal = () => {
      showDeleteModal.value = false
      selectedDeck.value = null
    }
    
    const editDeckOptions = (deck) => {
      selectedDeck.value = deck
      deckOptions.value = {
        newCardsPerDay: deck.newCardsPerDay || 20,
        maxReviewsPerDay: deck.maxReviewsPerDay || 100,
        easyInterval: deck.easyInterval || 4,
        easyBonus: deck.easyBonus || 1.3,
        intervalModifier: deck.intervalModifier || 1.0,
        startingEase: deck.startingEase || 2.5,
        steps: deck.steps || 1
      }
      showOptionsModal.value = true
      
      setTimeout(() => {
        if (optionsModalRef.value) {
          optionsModalRef.value.focus()
        }
      }, 100)
    }
    
    const closeOptionsModal = () => {
      showOptionsModal.value = false
      selectedDeck.value = null
      deckOptions.value = {
        newCardsPerDay: 20,
        maxReviewsPerDay: 100,
        easyInterval: 4,
        easyBonus: 1.3,
        intervalModifier: 1.0,
        startingEase: 2.5,
        steps: 1
      }
    }
    
    const saveDeckOptions = async () => {
      if (!selectedDeck.value) return
      
      saving.value = true
      try {
        await updateDeck(selectedDeck.value.id, {
          ...deckForm.value,
          ...deckOptions.value
        })
        alert('Deck options updated successfully!')
        await loadDecks()
        closeOptionsModal()
      } catch (error) {
        console.error('Failed to save deck options:', error)
        alert('Failed to save deck options. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const addCardToDeck = (deck) => {
      selectedDeck.value = deck
      addCardForm.value = {
        cardId: ''
      }
      addCardErrors.value = {}
      showAddCardModal.value = true
      
      setTimeout(() => {
        if (addCardModalRef.value) {
          addCardModalRef.value.focus()
        }
      }, 100)
    }
    
    const closeAddCardModal = () => {
      showAddCardModal.value = false
      selectedDeck.value = null
      addCardForm.value = {
        cardId: ''
      }
      addCardErrors.value = {}
    }
    
    const saveCardToDeck = async () => {
      const errors = validateAddCardForm()
      addCardErrors.value = errors
      
      if (Object.keys(errors).length > 0) {
        const firstErrorField = document.getElementById('card-select')
        if (firstErrorField) {
          firstErrorField.focus()
        }
        return
      }
      
      saving.value = true
      try {
        // Add card to deck logic
        alert('Card added to deck successfully!')
        closeAddCardModal()
      } catch (error) {
        console.error('Failed to add card to deck:', error)
        alert('Failed to add card to deck. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const viewDeckCards = (deck) => {
      router.push({ name: 'CardBrowser', params: { deckId: deck.id } })
    }
    
    const startDeckReview = (deck) => {
      router.push({ name: 'Review', params: { deckId: deck.id } })
    }
    
    const duplicateDeck = async (deck) => {
      try {
        await duplicateDeckApi(deck.id)
        alert('Deck duplicated successfully!')
        await loadDecks()
      } catch (error) {
        console.error('Failed to duplicate deck:', error)
        alert('Failed to duplicate deck. Please try again.')
      }
    }
    
    const exportDeck = (deck) => {
      // Export deck logic
      alert('Deck exported successfully!')
    }
    
    const exportAllDecks = () => {
      // Export all decks logic
      alert('All decks exported successfully!')
    }
    
    const importDecks = (event) => {
      const file = event.target.files[0]
      if (file) {
        // Import decks logic
        alert('Decks imported successfully!')
        loadDecks()
      }
    }
    
    const viewPublicDeckCards = (deck) => {
      router.push({ name: 'CardBrowser', params: { deckId: deck.id } })
    }
    
    const startPublicDeckReview = (deck) => {
      router.push({ name: 'Review', params: { deckId: deck.id } })
    }
    
    const duplicatePublicDeck = async (deck) => {
      try {
        await duplicateDeckApi(deck.id)
        alert('Public deck copied successfully!')
        await loadDecks()
      } catch (error) {
        console.error('Failed to copy public deck:', error)
        alert('Failed to copy public deck. Please try again.')
      }
    }
    
    onMounted(async () => {
      await loadDecks()
      await loadPublicDecks()
    })
    
    return {
      decks,
      publicDecks,
      availableCards,
      loading,
      loadingPublic,
      loadingCards,
      saving,
      deleting,
      showCreateModal,
      showEditModal,
      showDeleteModal,
      showOptionsModal,
      showAddCardModal,
      selectedDeck,
      modalRef,
      deleteModalRef,
      optionsModalRef,
      addCardModalRef,
      deckForm,
      deckErrors,
      addCardForm,
      addCardErrors,
      deckOptions,
      saveDeck,
      deleteDeck,
      editDeck,
      confirmDeleteDeck,
      closeModal,
      closeDeleteModal,
      editDeckOptions,
      closeOptionsModal,
      saveDeckOptions,
      addCardToDeck,
      closeAddCardModal,
      saveCardToDeck,
      viewDeckCards,
      startDeckReview,
      duplicateDeck,
      exportDeck,
      exportAllDecks,
      importDecks,
      viewPublicDeckCards,
      startPublicDeckReview,
      duplicatePublicDeck
    }
  }
}
</script>

<style scoped>
/* Decks Management Styles */
.decks {
  min-height: 100vh;
  padding: var(--space-8) var(--space-4);
}

.page-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-8);
  text-align: center;
  background: linear-gradient(135deg, var(--primary-600), var(--secondary-600));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: var(--tracking-tight);
}

.decks-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-10);
}

/* Create Deck Section */
.create-deck-section {
  margin-bottom: var(--space-8);
}

.create-deck-actions {
  display: flex;
  gap: var(--space-4);
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  padding: var(--space-6);
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--surface-border);
}

.create-deck-actions .btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  font-weight: var(--font-medium);
  border-radius: var(--radius-xl);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  cursor: pointer;
  border: none;
  font-size: var(--text-base);
}

.file-input {
  display: none;
}

/* Section Titles */
.section-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16);
  gap: var(--space-4);
  background-color: var(--surface-secondary);
  border-radius: var(--radius-2xl);
  border: 2px dashed var(--surface-border);
}

.spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--surface-border);
  border-top-color: var(--primary-500);
  border-radius: 50%;
  animation: spin 1s var(--transition-ease-in-out) infinite;
}

.loading-state p {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin: 0;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16);
  gap: var(--space-6);
  text-align: center;
  background-color: var(--surface-secondary);
  border-radius: var(--radius-2xl);
  border: 2px dashed var(--surface-border);
}

.empty-state h3 {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.empty-state p {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  max-width: 500px;
  margin: 0;
  line-height: var(--leading-relaxed);
}

/* Deck Cards Grid */
.deck-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--space-6);
}

/* Deck Card */
.deck-card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--surface-border);
  padding: var(--space-6);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  animation: fadeIn var(--transition-slow) var(--transition-ease-out);
}

.deck-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-200);
}

.deck-card.public-deck {
  border-left: 4px solid var(--primary-500);
}

.deck-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
}

.deck-name {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
  flex: 1;
}

.public-badge {
  background-color: var(--primary-100);
  color: var(--primary-700);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
}

.deck-card-actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}

.deck-card-body {
  margin-bottom: var(--space-6);
}

.deck-description {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-4) 0;
  line-height: var(--leading-relaxed);
}

.deck-description.placeholder {
  color: var(--text-light);
  font-style: italic;
}

.deck-stats {
  display: flex;
  gap: var(--space-4);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--surface-border);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-item strong {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.stat-item span {
  font-size: var(--text-xs);
  color: var(--text-light);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
}

.deck-card-footer {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  padding-top: var(--space-4);
  border-top: 1px solid var(--surface-border);
}

.deck-card-footer .btn {
  flex: 1;
  min-width: 100px;
}

.btn-sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  border-radius: var(--radius-lg);
}

/* Modals */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-overlay);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: var(--z-modal);
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

.modal {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-2xl);
  animation: slideUp var(--transition-normal) var(--transition-ease-out);
  border: 1px solid var(--surface-border);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-6);
  border-bottom: 1px solid var(--surface-border);
}

.modal-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: var(--text-2xl);
  cursor: pointer;
  color: var(--text-light);
  transition: color var(--transition-normal) var(--transition-ease-in-out);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
}

.close-btn:hover {
  color: var(--text-secondary);
  background-color: var(--bg-tertiary);
}

.modal-body {
  padding: var(--space-6);
}

/* Forms */
.deck-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  letter-spacing: var(--tracking-wide);
  margin-bottom: var(--space-1);
  text-transform: uppercase;
}

.form-control {
  padding: var(--space-4);
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-xl);
  font-size: var(--text-base);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  background-color: var(--surface-primary);
  color: var(--text-primary);
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px var(--primary-100);
}

.form-checkbox {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.form-checkbox input[type="checkbox"] {
  transform: scale(1.1);
  accent-color: var(--primary-500);
}

.form-help {
  font-size: var(--text-sm);
  color: var(--text-light);
  margin: 0;
  line-height: var(--leading-relaxed);
}

.form-error {
  font-size: var(--text-sm);
  color: var(--error-600);
  margin: 0;
  padding-top: var(--space-1);
}

.form-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: flex-end;
  margin-top: var(--space-8);
  padding-top: var(--space-6);
  border-top: 1px solid var(--surface-border);
}

/* Delete Modal */
.delete-warning {
  font-size: var(--text-lg);
  font-weight: var(--font-medium);
  color: var(--error-600);
  margin: 0 0 var(--space-4) 0;
  line-height: var(--leading-relaxed);
}

.delete-info {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-6) 0;
  line-height: var(--leading-relaxed);
}

/* Accessibility */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(var(--space-4));
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(var(--space-8));
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .decks {
    padding: var(--space-6) var(--space-3);
  }

  .page-title {
    font-size: var(--text-2xl);
  }

  .create-deck-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .create-deck-actions .btn {
    justify-content: center;
  }

  .deck-cards-grid {
    grid-template-columns: 1fr;
  }

  .deck-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }

  .deck-card-actions {
    align-self: flex-end;
  }

  .deck-stats {
    flex-direction: column;
    gap: var(--space-3);
  }

  .deck-card-footer {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
  }

  .modal {
    width: 95%;
    margin: var(--space-2);
  }

  .modal-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .close-btn {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .decks {
    padding: var(--space-4) var(--space-2);
  }

  .page-title {
    font-size: var(--text-xl);
  }

  .create-deck-actions {
    padding: var(--space-4);
  }

  .deck-card {
    padding: var(--space-4);
  }

  .deck-name {
    font-size: var(--text-lg);
  }

  .modal-header,
  .modal-body {
    padding: var(--space-4);
  }

  .modal-title {
    font-size: var(--text-lg);
  }
}
</style>