<template>
  <main class="material-viewer" aria-labelledby="material-title">
    <!-- Loading State -->
    <section v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Loading material...</p>
    </section>
    
    <!-- Error State -->
    <section v-else-if="!material" class="error-state">
      <div class="error-content">
        <div class="error-icon">⚠️</div>
        <h2 class="error-title">Material Not Found</h2>
        <p class="error-description">The requested material could not be found.</p>
        <div class="error-actions">
          <button @click="$router.push('/materials')" class="btn btn-primary" tabindex="0">
            Back to Materials
          </button>
        </div>
      </div>
    </section>
    
    <!-- Material Content -->
    <div v-else class="material-container">
      <!-- Material Header -->
      <header class="material-header">
        <div class="header-content">
          <div class="material-info">
            <h1 id="material-title" class="material-title">{{ material.title }}</h1>
            <div class="material-meta">
              <span :class="['badge', getTypeBadgeClass(material.type)]">
                {{ formatMaterialType(material.type) }}
              </span>
              <span class="meta-item" aria-label="File name">
                <span class="meta-icon">📄</span>
                {{ material.fileName }}
              </span>
              <span class="meta-item" aria-label="File size">
                <span class="meta-icon">📊</span>
                {{ formatFileSize(material.fileSize) }}
              </span>
              <span class="meta-item" aria-label="Created date">
                <span class="meta-icon">📅</span>
                {{ formatDate(material.createdDate) }}
              </span>
            </div>
          </div>
          
          <div class="header-actions">
            <button @click="$router.push('/materials')" class="btn btn-outline" aria-label="Back to materials" tabindex="0">
              <span class="btn-icon">←</span>
              <span class="btn-text">Back</span>
            </button>
            <button @click="downloadMaterial" class="btn btn-outline" aria-label="Download material" tabindex="0">
              <span class="btn-icon">↓</span>
              <span class="btn-text">Download</span>
            </button>
            <button 
              @click="toggleSelectionMode" 
              :class="['btn', isSelectionMode ? 'btn-primary' : 'btn-outline']"
              aria-label="Toggle selection mode"
              tabindex="0"
            >
              <span class="btn-icon">✏️</span>
              <span class="btn-text">{{ isSelectionMode ? 'Exit Selection' : 'Selection Mode' }}</span>
            </button>
          </div>
        </div>
      </header>

      <!-- Mode Toggle -->
      <section v-if="isSelectionMode" class="card-toolbar">
        <div class="alert alert-info">
          <div class="alert-icon">ℹ️</div>
          <div class="alert-content">
            <h3 class="alert-title">Selection Mode Active</h3>
            <p class="alert-description">Select text to create cards. Click on existing cards to edit them.</p>
          </div>
        </div>
      </section>

      <!-- Main Content Area -->
      <div class="content-area">
        <!-- Material Viewer -->
        <section class="material-content" :class="{ 'selection-mode': isSelectionMode }">
          <!-- Document Viewer -->
          <div v-if="material.type === 'DOCUMENT'" class="document-viewer">
            <DocumentViewer
                :material="material"
                :cards="cards"
                :selection-mode="isSelectionMode"
                @text-selected="handleTextSelection"
                @card-clicked="handleCardClick"
              />
          </div>
          
          <!-- Video Player -->
          <div v-else-if="material.type === 'VIDEO'" class="video-viewer">
            <div class="card">
              <VideoPlayer
                :material="material"
                :cards="cards"
                :selection-mode="isSelectionMode"
                @text-selected="handleTextSelection"
                @card-clicked="handleCardClick"
              />
            </div>
          </div>
          
          <!-- Article Viewer -->
          <div v-else-if="material.type === 'ARTICLE'" class="article-viewer">
            <div class="card">
              <ArticleViewer
                :material="material"
                :cards="cards"
                :selection-mode="isSelectionMode"
                @text-selected="handleTextSelection"
                @card-clicked="handleCardClick"
              />
            </div>
          </div>
        </section>

        <!-- Cards Sidebar -->
        <aside class="cards-sidebar">
          <div class="card">
            <div class="card-header">
              <h3 class="sidebar-title">Cards</h3>
              <span class="cards-count badge badge-primary">{{ cards.length }}</span>
            </div>
            <div class="card-body">
              <!-- Cards List -->
              <div v-if="cards.length === 0" class="empty-cards">
                <div class="empty-content">
                  <div class="empty-icon">📝</div>
                  <h4 class="empty-title">No cards yet</h4>
                  <p class="empty-description">Start selecting text to create vocabulary cards!</p>
                </div>
              </div>
              
              <div v-else class="cards-list">
                <CardItem
                  v-for="card in sortedCards"
                  :key="card.id"
                  :card="card"
                  @edit="editCard"
                  @delete="deleteCard"
                  @click="scrollToCard"
                />
              </div>
            </div>
          </div>
        </aside>
      </div>

      <!-- Card Creation Dialog -->
      <div v-if="showCardDialog" class="modal-overlay" @click="resetCardDialog">
        <div class="modal" @click.stop>
          <div class="modal-header">
            <h3>Create Card</h3>
            <button @click="resetCardDialog" class="close-btn" aria-label="Close dialog">&times;</button>
          </div>
          <div class="modal-body">
            <CardForm
              :selected-text="selectedText"
              :context="selectedContext"
              :material-id="material.id"
              :tags="tags.map(tag => tag.name)"
              @save="handleCardSave"
              @cancel="resetCardDialog"
            />
          </div>
        </div>
      </div>

      <!-- Card Edit Dialog -->
      <div v-if="showEditDialog" class="modal-overlay" @click="resetEditDialog">
        <div class="modal" @click.stop>
          <div class="modal-header">
            <h3>Edit Card</h3>
            <button @click="resetEditDialog" class="close-btn" aria-label="Close dialog">&times;</button>
          </div>
          <div class="modal-body">
            <CardForm
              :card="editingCard"
              :material-id="material.id"
              :tags="tags.map(tag => tag.name)"
              @save="handleCardUpdate"
              @cancel="resetEditDialog"
            />
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useApiService } from '../composables/useApiService'
import { useNotification } from '../composables/useNotification'
import DocumentViewer from '../components/DocumentViewer.vue'
import VideoPlayer from '../components/VideoPlayer.vue'
import ArticleViewer from '../components/ArticleViewer.vue'
import CardItem from '../components/CardItem.vue'
import CardForm from '../components/CardForm.vue'
import { confirmCardDelete } from '../utils/confirmDialog'

export default {
  name: 'MaterialViewer',
  components: {
    DocumentViewer,
    VideoPlayer,
    ArticleViewer,
    CardItem,
    CardForm
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const material = ref(null)
    const cards = ref([])
    const loading = ref(false)
    
    // Selection mode state
    const isSelectionMode = ref(false)
    const showCardDialog = ref(false)
    const showEditDialog = ref(false)
    const selectedText = ref('')
    const selectedContext = ref('')
    const selectedTextPosition = ref({ startPosition: 0, endPosition: 0 })
    const editingCard = ref(null)
    const tags = ref([])
    
    const { apiService } = useApiService()
    const { showSuccess, showError } = useNotification()
    
    // Computed properties
    const sortedCards = computed(() => {
      return [...cards.value].sort((a, b) => {
        // Sort by position if available, otherwise by creation date
        if (a.startPosition !== undefined && b.startPosition !== undefined) {
          return a.startPosition - b.startPosition
        }
        return new Date(b.createdDate) - new Date(a.createdDate)
      })
    })

    const materialTypeClasses = {
      VIDEO: 'badge-primary',
      DOCUMENT: 'badge-success',
      ARTICLE: 'badge-warning'
    }

    const getTypeBadgeClass = (type) => {
      return materialTypeClasses[type] || 'badge-info'
    }

    const formatMaterialType = (type) => {
      const typeMap = {
        VIDEO: 'Video',
        DOCUMENT: 'Document',
        ARTICLE: 'Article'
      }
      return typeMap[type] || type
    }

    // Methods
    const loadMaterial = async () => {
      loading.value = true
      try {
        const materialId = route.params.id
        const response = await apiService.get(`/materials/${materialId}`)
        material.value = response.data
        
        // Load cards for this material
        await loadCards(materialId)
        
        // Load tags for the user
        await loadTags()
        
        // Check if we should start in selection mode
        if (route.query.mode === 'selection' || route.query.mode === 'highlight') {
          isSelectionMode.value = true
        }
      } catch (error) {
        console.error('Error loading material:', error)
        alert('Failed to load material')
        material.value = null
      } finally {
        loading.value = false
      }
    }
    
    const loadCards = async (materialId) => {
      try {
        const response = await apiService.get(`/vocabulary/material/${materialId}`)
        cards.value = response.data || []
      } catch (error) {
        console.error('Error loading cards:', error)
        cards.value = []
      }
    }
    
    const loadTags = async () => {
      try {
        const response = await apiService.get('/tags')
        tags.value = response.data || []
      } catch (error) {
        console.error('Error loading tags:', error)
        tags.value = []
      }
    }
    
    const downloadMaterial = async () => {
      if (!material.value) return
      
      try {
        await apiService.download(
          `/materials/${material.value.id}/download`,
          material.value.fileName
        )
      } catch (error) {
        console.error('Error downloading material:', error)
        alert('Failed to download material')
      }
    }

    const toggleSelectionMode = () => {
      isSelectionMode.value = !isSelectionMode.value
      
      if (isSelectionMode.value) {
        alert('Selection mode activated. Select text to create cards.')
      } else {
        alert('Selection mode deactivated.')
      }
    }

    const handleTextSelection = (selection) => {
      console.log('MaterialViewer: handleTextSelection called', { selection, isSelectionMode: isSelectionMode.value })
      
      if (!isSelectionMode.value) {
        console.log('MaterialViewer: Not in selection mode, returning')
        return
      }
      
      if (!selection || !selection.text) {
        console.log('MaterialViewer: No text selected, returning')
        return
      }
      
      selectedText.value = selection.text
      selectedContext.value = selection.context
      
      // Store the position data for later use
      selectedTextPosition.value = {
        startPosition: selection.startPosition || 0,
        endPosition: selection.endPosition || selection.text.length
      }
      
      console.log('MaterialViewer: Showing card dialog', { selectedText: selectedText.value })
      showCardDialog.value = true
    }

    const handleCardClick = (card) => {
      if (isSelectionMode.value) {
        editingCard.value = card
        showEditDialog.value = true
      }
    }

    const handleCardSave = async (cardData) => {
      try {
        const response = await apiService.post('/vocabulary/cards', {
          materialId: material.value.id,
          text: selectedText.value,
          backText: cardData.backText,
          context: cardData.context,
          startPosition: selectedTextPosition.value.startPosition,
          endPosition: selectedTextPosition.value.endPosition,
          userComment: cardData.comment,
          tags: cardData.tags
        })
        
        cards.value.push(response.data)
        resetCardDialog()
        showSuccess('Card created successfully')
      } catch (error) {
        console.error('Error creating card:', error)
        showError('Failed to create card')
      }
    }

    const handleCardUpdate = async (cardData) => {
      try {
        const response = await apiService.put(`/vocabulary/cards/${editingCard.value.id}`, {
          backText: cardData.backText,
          context: cardData.context,
          userComment: cardData.comment,
          tags: cardData.tags
        })
        
        const index = cards.value.findIndex(c => c.id === editingCard.value.id)
        if (index !== -1) {
          cards.value[index] = response.data
        }
        
        resetEditDialog()
        showSuccess('Card updated successfully')
      } catch (error) {
        console.error('Error updating card:', error)
        showError('Failed to update card')
      }
    }

    const editCard = (card) => {
      editingCard.value = card
      showEditDialog.value = true
    }
    
    const deleteCard = async (card) => {
      try {
        const confirmed = await confirmCardDelete(card.text)
        if (!confirmed) {
          return
        }
        
        await apiService.delete(`/vocabulary/cards/${card.id}`)
        cards.value = cards.value.filter(c => c.id !== card.id)
        showSuccess('Card deleted successfully')
      } catch (error) {
        console.error('Error deleting card:', error)
        showError('Failed to delete card')
      }
    }

    const scrollToCard = (card) => {
      // This would scroll to the card in the document
      // Implementation depends on the specific viewer component
      console.log('Scrolling to card:', card.id)
    }

    const resetCardDialog = () => {
      showCardDialog.value = false
      selectedText.value = ''
      selectedContext.value = ''
      selectedTextPosition.value = { startPosition: 0, endPosition: 0 }
    }

    const resetEditDialog = () => {
      showEditDialog.value = false
      editingCard.value = null
    }
    
    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const formatDate = (dateString) => {
      return dayjs(dateString).format('MMM D, YYYY')
    }
    
    // Keyboard shortcuts
    const handleKeydown = (event) => {
      if (event.key === 'Escape' && isSelectionMode.value) {
        toggleSelectionMode()
      }
    }

    onMounted(() => {
      loadMaterial()
      document.addEventListener('keydown', handleKeydown)
    })

    onUnmounted(() => {
      document.removeEventListener('keydown', handleKeydown)
    })
    
    return {
      material,
      cards,
      tags,
      loading,
      isSelectionMode,
      showCardDialog,
      showEditDialog,
      selectedText,
      selectedContext,
      editingCard,
      sortedCards,
      getTypeBadgeClass,
      formatMaterialType,
      loadMaterial,
      loadCards,
      loadTags,
      downloadMaterial,
      toggleSelectionMode,
      handleTextSelection,
      handleCardClick,
      handleCardSave,
      handleCardUpdate,
      editCard,
      deleteCard,
      scrollToCard,
      resetCardDialog,
      resetEditDialog,
      formatFileSize,
      formatDate
    }
  }
}
</script>

<style scoped>
.material-viewer {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-4);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16);
  gap: var(--space-4);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--surface-border);
  border-top-color: var(--primary-500);
  border-radius: 50%;
  animation: spin 1s var(--transition-ease-in-out) infinite;
}

.loading-text {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin: 0;
}

.error-state {
  padding: var(--space-16) var(--space-4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.error-content {
  text-align: center;
  max-width: 500px;
  padding: var(--space-8);
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--surface-border);
}

.error-icon {
  font-size: 4rem;
  margin-bottom: var(--space-4);
}

.error-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.error-description {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-6) 0;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-4);
}

.material-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.material-header {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--surface-border);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-8);
}

.material-info {
  flex: 1;
  min-width: 0;
}

.material-title {
  margin: 0 0 var(--space-4) 0;
  color: var(--text-primary);
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  line-height: var(--leading-tight);
  word-break: break-word;
}

.material-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.meta-icon {
  font-size: var(--text-base);
}

.header-actions {
  display: flex;
  gap: var(--space-3);
  flex-shrink: 0;
}

.header-actions .btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  border-radius: var(--radius-lg);
  transition: var(--transition-normal);
}

.header-actions .btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.card-toolbar {
  margin-bottom: var(--space-4);
}

.alert {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--surface-border);
  background-color: var(--surface-primary);
}

.alert-info {
  border-left: 4px solid var(--info-500);
  background-color: var(--info-50);
}

.alert-icon {
  font-size: var(--text-xl);
  flex-shrink: 0;
  margin-top: var(--space-1);
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
}

.alert-description {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.content-area {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: var(--space-6);
  align-items: start;
}

.material-content {
  min-height: 600px;
}

.material-content.selection-mode {
  cursor: text;
}

.cards-sidebar {
  position: sticky;
  top: var(--space-4);
  max-height: calc(100vh - var(--space-8));
  overflow-y: auto;
}

.card {
  background-color: var(--surface-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--surface-border);
  transition: var(--transition-normal);
}

.card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-1px);
}

.card-header {
  padding: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.card-body {
  padding: var(--space-4);
}

.empty-cards {
  text-align: center;
  padding: var(--space-8);
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: var(--space-2);
}

.empty-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.empty-description {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  max-width: 300px;
  line-height: var(--leading-relaxed);
}

.cards-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-height: 500px;
  overflow-y: auto;
}

/* Modal styles */
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
  max-width: 500px;
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

.modal-header h3 {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
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

/* Animations */
@keyframes spin {
  to { transform: rotate(360deg); }
}

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

/* Responsive design */
@media (max-width: 1200px) {
  .content-area {
    grid-template-columns: 1fr;
  }
  
  .cards-sidebar {
    position: static;
    max-height: none;
  }
}

@media (max-width: 768px) {
  .material-viewer {
    padding: var(--space-3);
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .header-actions .btn {
    flex: 1;
    justify-content: center;
  }
  
  .material-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
  
  .material-title {
    font-size: var(--text-2xl);
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .material-header {
    border: 2px solid var(--text-primary);
  }
  
  .card {
    border: 2px solid var(--text-primary);
  }
  
  .alert {
    border: 2px solid var(--text-primary);
  }
}

/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner {
    animation: none;
  }
  
  .modal-overlay {
    animation: none;
  }
  
  .modal {
    animation: none;
  }
  
  .header-actions .btn:hover {
    transform: none;
  }
  
  .card:hover {
    transform: none;
  }
}
</style>