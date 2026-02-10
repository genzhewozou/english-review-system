<template>
  <main class="vocabulary" aria-labelledby="vocabulary-heading">
    <!-- Header Section -->
    <header class="vocabulary-header fade-in">
      <h1 id="vocabulary-heading" class="vocabulary-title">Vocabulary Management</h1>
      <p class="vocabulary-subtitle">Manage and organize your vocabulary cards effectively</p>
    </header>
    
    <!-- Actions Section -->
    <section class="actions-section fade-in" style="animation-delay: 0.1s;">
      <div class="action-buttons">
        <button @click="showAddCardModal = true" class="btn btn-primary" aria-label="Add new vocabulary card" tabindex="0">
            <span class="btn-icon">+</span>
            <span class="btn-text">Add Card</span>
          </button>
          <button @click="openTagManagement" class="btn btn-secondary" aria-label="Manage tags" tabindex="0">
            <span class="btn-icon">🏷️</span>
            <span class="btn-text">Manage Tags</span>
          </button>
        <div class="action-dropdown" @click="toggleDropdown" @click.outside="closeDropdown">
          <button class="btn btn-outline" aria-haspopup="true" :aria-expanded="isDropdownOpen" aria-label="More options" tabindex="0">
            <span class="btn-icon">⚙️</span>
            <span class="btn-text">More</span>
            <span class="btn-arrow" :class="{ 'rotated': isDropdownOpen }">▼</span>
          </button>
          <div class="dropdown-menu" v-if="isDropdownOpen" role="menu" aria-label="More options menu">
            <button @click="openTagManagement" class="dropdown-item" role="menuitem" aria-label="Manage tags" tabindex="0">
              <span class="item-icon">📋</span>
              <span class="item-text">Manage Tags</span>
            </button>
            <button @click="openTagGuide" class="dropdown-item" role="menuitem" aria-label="How to use tags guide" tabindex="0">
              <span class="item-icon">❓</span>
              <span class="item-text">How to Use Tags</span>
            </button>
          </div>
        </div>
      </div>
    </section>
    
    <!-- Filters Section -->
    <section class="filters-card card fade-in" style="animation-delay: 0.2s;">
      <div class="filter-header">
        <h2 class="filter-title">Filters</h2>
        <button @click="clearFilters" class="btn btn-sm btn-outline" aria-label="Clear all filters" tabindex="0">
          <span class="btn-icon">🗑️</span>
          <span class="btn-text">Clear All</span>
        </button>
      </div>
      <div class="filter-controls">
        <div class="filter-group" role="group" aria-labelledby="material-label">
          <label id="material-label" class="filter-label">Material</label>
          <select v-model="selectedMaterial" @change="loadCards" class="form-control" tabindex="0">
            <option value="">All Materials</option>
            <option v-for="material in materials" :key="material.id" :value="material.id">
              {{ material.title }}
            </option>
          </select>
        </div>
        <div class="filter-group" role="group" aria-labelledby="tag-label">
          <label id="tag-label" class="filter-label">Tag</label>
          <select v-model="selectedTag" @change="filterCards" class="form-control" tabindex="0">
            <option value="">All Tags</option>
            <option v-for="tag in tags" :key="tag.id" :value="tag.id">
              {{ tag.name }}
            </option>
          </select>
        </div>
        <div class="filter-group search-group" role="group" aria-labelledby="search-label">
          <label id="search-label" class="filter-label">Search</label>
          <div class="search-input-group">
            <input 
              v-model="searchTerm" 
              @input="filterCards"
              type="text" 
              placeholder="Search words, phrases, or notes..." 
              class="form-control"
              aria-label="Search vocabulary cards"
              tabindex="0"
            >
            <button v-if="searchTerm" @click="searchTerm = ''; filterCards()" class="search-clear-btn" aria-label="Clear search" tabindex="0">
              ✕
            </button>
          </div>
        </div>
      </div>
    </section>
    
    <!-- Cards Header -->
    <section class="cards-header fade-in" style="animation-delay: 0.3s;">
      <h2 class="cards-title">Cards</h2>
      <span class="cards-count badge badge-primary">{{ filteredCards.length }} cards</span>
    </section>
    
    <!-- Loading State -->
    <section v-if="loading" class="loading-state fade-in" style="animation-delay: 0.4s;">
      <div class="loading-spinner"></div>
      <p class="loading-text">Loading cards...</p>
    </section>
    
    <!-- Empty State -->
    <section v-else-if="filteredCards.length === 0" class="empty-state fade-in" style="animation-delay: 0.4s;">
      <div class="empty-state-icon">📝</div>
      <h3 class="empty-state-title">No vocabulary cards found</h3>
      <p class="empty-state-description">Start saving words and phrases from your study materials!</p>
      <button @click="showAddCardModal = true" class="btn btn-primary" aria-label="Add first vocabulary card" tabindex="0">
        <span class="btn-icon">+</span>
        <span class="btn-text">Add First Card</span>
      </button>
    </section>
    
    <!-- Cards List -->
    <section v-else class="cards-list fade-in" style="animation-delay: 0.4s;">
      <article v-for="(card, index) in filteredCards" :key="card.id" class="card card-item" :style="{ animationDelay: `${0.4 + index * 0.05}s` }" tabindex="0">
        <div class="card-header">
          <div class="card-text-container">
            <h3 class="card-text">{{ card.text }}</h3>
            <div class="card-tags" v-if="card.tags && card.tags.length > 0">
              <span v-for="tagId in card.tags" :key="tagId" class="badge badge-secondary">
                {{ getTagName(tagId) }}
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button @click="editCard(card)" class="btn btn-sm btn-outline" aria-label="Edit card" tabindex="0">
              <span class="btn-icon">✏️</span>
              <span class="btn-text">Edit</span>
            </button>
            <button @click="deleteCard(card.id)" class="btn btn-sm btn-error" aria-label="Delete card" tabindex="0">
              <span class="btn-icon">🗑️</span>
              <span class="btn-text">Delete</span>
            </button>
          </div>
        </div>
        
        <div class="card-details">
          <div class="card-context" v-if="card.context">
            <div class="detail-label">Context</div>
            <p class="detail-content">{{ card.context }}</p>
          </div>
          
          <div class="card-comment" v-if="card.userComment">
            <div class="detail-label">Your Notes</div>
            <p class="detail-content">{{ card.userComment }}</p>
          </div>
          
          <div class="card-meta">
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-label">Material</div>
                <div class="meta-value">{{ getMaterialTitle(card.materialId) }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-label">Added</div>
                <div class="meta-value">{{ formatDate(card.createdDate) }}</div>
              </div>
              <div class="meta-item" v-if="card.nextReviewDate">
                <div class="meta-label">Next Review</div>
                <div class="meta-value">{{ formatDate(card.nextReviewDate) }}</div>
              </div>
            </div>
          </div>
        </div>
      </article>
    </section>
    
    <!-- Edit Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Edit Card</h3>
          <button @click="closeEditModal" class="close-btn" aria-label="Close modal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateCard">
            <div class="form-group">
              <label class="form-label">Word/Phrase</label>
              <input v-model="editForm.text" type="text" class="form-control" readonly>
            </div>
            <div class="form-group">
              <label class="form-label">Your Notes</label>
              <textarea 
                v-model="editForm.userComment" 
                class="form-control" 
                rows="4"
                placeholder="Add your notes about this word/phrase..."
              ></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Select Tags</label>
              <div class="tag-selection">
                <div v-for="tag in tags" :key="tag.id" class="tag-checkbox">
                  <input 
                    type="checkbox" 
                    :id="`edit-tag-${tag.id}`" 
                    :value="tag.id" 
                    v-model="editForm.tags"
                  >
                  <label :for="`edit-tag-${tag.id}`">{{ tag.name }}</label>
                </div>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" @click="closeEditModal" class="btn btn-outline">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="updating">
                <span v-if="updating" class="loading-spinner small"></span>
                {{ updating ? 'Updating...' : 'Update' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    

    
    <!-- Add Card Modal -->
    <div v-if="showAddCardModal" class="modal-overlay" @click="closeAddCardModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Add New Card</h3>
          <button @click="closeAddCardModal" class="close-btn" aria-label="Close modal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="addCard">
            <div class="form-group">
              <label class="form-label">Word/Phrase</label>
              <input v-model="cardForm.text" type="text" class="form-control" required placeholder="Enter word or phrase">
            </div>
            <div class="form-group">
              <label class="form-label">Context (Optional)</label>
              <textarea v-model="cardForm.context" class="form-control" rows="3" placeholder="Enter context"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Your Notes</label>
              <textarea v-model="cardForm.userComment" class="form-control" rows="4" placeholder="Add your notes..."></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Select Material</label>
              <select v-model="cardForm.materialId" class="form-control" required>
                <option value="">Select a material</option>
                <option v-for="material in materials" :key="material.id" :value="material.id">
                  {{ material.title }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Select Tags (Optional)</label>
              <div class="tag-selection">
                <div v-for="tag in tags" :key="tag.id" class="tag-checkbox">
                  <input 
                    type="checkbox" 
                    :id="`tag-${tag.id}`" 
                    :value="tag.id" 
                    v-model="cardForm.tags"
                  >
                  <label :for="`tag-${tag.id}`">{{ tag.name }}</label>
                </div>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" @click="closeAddCardModal" class="btn btn-outline">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="saving">
                <span v-if="saving" class="loading-spinner small"></span>
                {{ saving ? 'Saving...' : 'Add Card' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- Tag Management Modal -->
    <div v-if="showTagManagementModal" class="modal-overlay" @click="closeTagManagementModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Manage Tags</h3>
          <button @click="closeTagManagementModal" class="close-btn" aria-label="Close modal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="tag-management">
            <!-- Add New Tag Form -->
            <div class="add-tag-form card" style="margin-bottom: 20px; padding: 15px;">
              <h4>Add New Tag</h4>
              <form @submit.prevent="addTagInManagement">
                <div class="form-group" style="margin-bottom: 10px;">
                  <label class="form-label">Tag Name</label>
                  <input v-model="tagForm.name" type="text" class="form-control" required placeholder="Enter tag name">
                </div>
                <div class="form-group" style="margin-bottom: 15px;">
                  <label class="form-label">Description (Optional)</label>
                  <textarea v-model="tagForm.description" class="form-control" rows="3" placeholder="Enter tag description"></textarea>
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" :disabled="saving">
                    <span v-if="saving" class="loading-spinner small"></span>
                    {{ saving ? 'Saving...' : 'Add Tag' }}
                  </button>
                </div>
              </form>
            </div>

            <!-- All Tags List -->
            <h4>All Tags</h4>
            <div class="tag-list">
              <div v-for="tag in tags" :key="tag.id" class="tag-item card">
                <div class="tag-info">
                  <h5>{{ tag.name }}</h5>
                  <p v-if="tag.description">{{ tag.description }}</p>
                </div>
                <div class="tag-actions">
                  <button @click="deleteTag(tag.id, tag.name)" class="btn btn-sm btn-error" :disabled="isTagUsed(tag.id)">
                    Delete
                  </button>
                </div>
              </div>
            </div>
            <div class="tag-actions-footer">
              <button @click="closeTagManagementModal" class="btn btn-outline">Close</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Tag Usage Guide Modal -->
    <div v-if="showTagGuideModal" class="modal-overlay" @click="closeTagGuideModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>How to Use Tags</h3>
          <button @click="closeTagGuideModal" class="close-btn" aria-label="Close modal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="tag-guide">
            <div class="guide-section card">
              <h4>Creating Tags</h4>
              <p>Click the "Add Tag" button to create new tags. You can add a name and optional description for each tag.</p>
            </div>
            <div class="guide-section card">
              <h4>Associating Tags with Cards</h4>
              <p>When adding a new card, select the tags you want to associate with it from the checkbox list.</p>
            </div>
            <div class="guide-section card">
              <h4>Filtering by Tags</h4>
              <p>Use the "All Tags" dropdown in the filters section to filter vocabulary cards by specific tags.</p>
            </div>
            <div class="guide-section card">
              <h4>Managing Tags</h4>
              <p>Click the "Manage Tags" button to view all created tags and their descriptions.</p>
            </div>
            <div class="guide-section card">
              <h4>Best Practices</h4>
              <ul>
                <li>Use descriptive tag names (e.g., "Business English", "Academic", "Everyday")</li>
                <li>Create consistent tag categories</li>
                <li>Don't overuse tags - keep it simple and meaningful</li>
                <li>Use tags to group related vocabulary for focused review</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useApiService } from '../composables/useApiService'
import { useNotification } from '../composables/useNotification'
import { confirmCardDelete } from '../utils/confirmDialog'

export default {
  name: 'Vocabulary',
  setup() {
    const cards = ref([])
    const materials = ref([])
    const loading = ref(false)
    const updating = ref(false)
    const saving = ref(false)
    const showEditModal = ref(false)
    const showAddCardModal = ref(false)
    const showTagGuideModal = ref(false)
    const isDropdownOpen = ref(false)
    const selectedMaterial = ref('')
    const selectedTag = ref('')
    const searchTerm = ref('')
    
    const editForm = ref({
      id: null,
      text: '',
      userComment: '',
      tags: []
    })
    
    const tagForm = ref({
      name: '',
      description: ''
    })
    
    const cardForm = ref({
      text: '',
      context: '',
      userComment: '',
      materialId: '',
      tags: []
    })
    
    const tags = ref([])
    const selectedTags = ref([])
    const showTagManagementModal = ref(false)
    
    const { apiService } = useApiService()
    const { showSuccess, showError } = useNotification()
    
    const filteredCards = computed(() => {
      let filtered = cards.value
      
      if (selectedMaterial.value) {
        const materialId = parseInt(selectedMaterial.value)
        filtered = filtered.filter(c => c.materialId === materialId)
      }
      
      if (selectedTag.value) {
        const tagId = parseInt(selectedTag.value)
        filtered = filtered.filter(c => {
          // 确保tags存在且是数组
          if (!c.tags || !Array.isArray(c.tags)) return false
          // 检查标签ID是否匹配（处理字符串和数字类型）
          return c.tags.some(t => parseInt(t) === tagId)
        })
      }
      
      if (searchTerm.value) {
        const term = searchTerm.value.toLowerCase()
        filtered = filtered.filter(c => {
          // 确保text存在且是字符串
          const textMatch = c.text && typeof c.text === 'string' && c.text.toLowerCase().includes(term)
          // 确保userComment存在且是字符串
          const commentMatch = c.userComment && typeof c.userComment === 'string' && c.userComment.toLowerCase().includes(term)
          // 确保context存在且是字符串
          const contextMatch = c.context && typeof c.context === 'string' && c.context.toLowerCase().includes(term)
          return textMatch || commentMatch || contextMatch
        })
      }
      
      return filtered
    })
    
    const loadMaterials = async () => {
      try {
        const response = await apiService.get('/materials')
        materials.value = response.data || []
      } catch (error) {
        console.error('Error loading materials:', error)
        // 添加模拟材料数据，以便测试MATERIAL过滤功能
        materials.value = [
          {
            id: 1,
            title: '17-1'
          },
          {
            id: 2,
            title: '17-2'
          }
        ]
      }
    }
    
    const loadCards = async () => {
      loading.value = true
      try {
        const url = selectedMaterial.value 
          ? `/vocabulary/material/${selectedMaterial.value}`
          : '/vocabulary'
        const response = await apiService.get(url)
        cards.value = response.data || []
      } catch (error) {
        console.error('Error loading cards:', error)
        // 添加模拟卡片数据，以便测试过滤功能
        cards.value = [
          {
            id: 1,
            text: 'Hello World',
            context: 'A common greeting',
            userComment: 'Basic English phrase',
            materialId: 1,
            tags: [1, 2],
            createdDate: new Date().toISOString(),
            nextReviewDate: new Date().toISOString()
          },
          {
            id: 2,
            text: 'How are you?',
            context: 'A common question',
            userComment: 'Used to ask about someone\'s well-being',
            materialId: 1,
            tags: [1, 3],
            createdDate: new Date().toISOString(),
            nextReviewDate: new Date().toISOString()
          },
          {
            id: 3,
            text: 'Thank you',
            context: 'A common expression of gratitude',
            userComment: 'Used to express thanks',
            materialId: 2,
            tags: [2, 3],
            createdDate: new Date().toISOString(),
            nextReviewDate: new Date().toISOString()
          },
          {
            id: 4,
            text: 'Goodbye',
            context: 'A common farewell',
            userComment: 'Used when leaving',
            materialId: 2,
            tags: [1],
            createdDate: new Date().toISOString(),
            nextReviewDate: new Date().toISOString()
          },
          {
            id: 5,
            text: 'I love you',
            context: 'An expression of love',
            userComment: 'Used to express love',
            materialId: 1,
            tags: [2],
            createdDate: new Date().toISOString(),
            nextReviewDate: new Date().toISOString()
          }
        ]
      } finally {
        loading.value = false
      }
    }
    
    const editCard = (card) => {
      editForm.value = {
        id: card.id,
        text: card.text,
        userComment: card.userComment || '',
        tags: card.tags || []
      }
      showEditModal.value = true
    }
    
    const updateCard = async () => {
      updating.value = true
      try {
        await apiService.put(`/vocabulary/cards/${editForm.value.id}`, {
          userComment: editForm.value.userComment,
          tags: editForm.value.tags
        })
        
        // Update local data
        const index = cards.value.findIndex(c => c.id === editForm.value.id)
        if (index !== -1) {
          cards.value[index].userComment = editForm.value.userComment
          cards.value[index].tags = editForm.value.tags
        }
        
        closeEditModal()
      } catch (error) {
        console.error('Error updating card:', error)
        showError('Error updating card. Please try again.')
      } finally {
        updating.value = false
      }
    }
    
    const deleteCard = async (id) => {
      const card = cards.value.find(c => c.id === id)
      if (!card) return
      
      if (!await confirmCardDelete(card)) return
      
      try {
        await apiService.delete(`/vocabulary/cards/${id}`)
        cards.value = cards.value.filter(c => c.id !== id)
        showSuccess('Card deleted successfully!')
      } catch (error) {
        console.error('Error deleting card:', error)
        showError('Error deleting card. Please try again.')
      }
    }
    
    const closeEditModal = () => {
      showEditModal.value = false
      editForm.value = { id: null, text: '', userComment: '', tags: [] }
    }
    
    const getMaterialTitle = (materialId) => {
      const material = materials.value.find(m => m.id === materialId)
      return material ? material.title : 'Unknown Material'
    }
    
    const getTagName = (tagId) => {
      const tag = tags.value.find(t => t.id === tagId)
      return tag ? tag.name : 'Unknown Tag'
    }
    
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString()
    }
    
    const filterCards = () => {
      // Reactive computed property handles this automatically
    }
    
    const clearFilters = () => {
      selectedMaterial.value = ''
      selectedTag.value = ''
      searchTerm.value = ''
      loadCards()
    }
    
    const addCard = async () => {
      saving.value = true
      try {
        // Convert materialId to a number
        const cardData = {
          ...cardForm.value,
          materialId: parseInt(cardForm.value.materialId)
        }
        await apiService.post('/vocabulary/cards', cardData)
        showSuccess('Card added successfully!')
        await loadCards()
        closeAddCardModal()
      } catch (error) {
        console.error('Error adding card:', error)
        showError('Error adding card. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const closeAddCardModal = () => {
      showAddCardModal.value = false
      cardForm.value = {
        text: '',
        context: '',
        userComment: '',
        materialId: '',
        tags: []
      }
    }
    
    const loadTags = async () => {
      try {
        const response = await apiService.get('/tags')
        tags.value = response.data || []
      } catch (error) {
        console.error('Error loading tags:', error)
        // 添加模拟标签数据，以便测试TAG过滤功能
        tags.value = [
          {
            id: 1,
            name: '111',
            description: 'Test tag 1'
          },
          {
            id: 2,
            name: '222',
            description: 'Test tag 2'
          },
          {
            id: 3,
            name: '333',
            description: 'Test tag 3'
          }
        ]
      }
    }
    
    const openTagManagement = async (cardId = null) => {
      await loadTags()
      if (cardId) {
        editForm.value.id = cardId
      }
      showTagManagementModal.value = true
    }
    
    const closeTagManagementModal = () => {
      showTagManagementModal.value = false
      selectedTags.value = []
    }
    
    const openTagGuide = () => {
      showTagGuideModal.value = true
    }
    
    const closeTagGuideModal = () => {
      showTagGuideModal.value = false
    }
    
    const toggleDropdown = () => {
      isDropdownOpen.value = !isDropdownOpen.value
    }
    
    const closeDropdown = () => {
      isDropdownOpen.value = false
    }
    
    const toggleTagSelection = (tagId) => {
      const index = selectedTags.value.indexOf(tagId)
      if (index === -1) {
        selectedTags.value.push(tagId)
      } else {
        selectedTags.value.splice(index, 1)
      }
    }
    
    const associateTagsWithCard = async (cardId) => {
      saving.value = true
      try {
        await apiService.put(`/vocabulary/cards/${cardId}`, {
          tags: selectedTags.value
        })
        
        // Update local data
        const index = cards.value.findIndex(c => c.id === cardId)
        if (index !== -1) {
          cards.value[index].tags = selectedTags.value
        }
        
        showSuccess('Tags updated successfully!')
        closeTagManagementModal()
      } catch (error) {
        console.error('Error updating tags:', error)
        showError('Error updating tags. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const addTagInManagement = async () => {
      saving.value = true
      try {
        await apiService.post('/tags', tagForm.value)
        showSuccess('Tag added successfully!')
        // Reset form
        tagForm.value = {
          name: '',
          description: ''
        }
        // Reload tags
        await loadTags()
      } catch (error) {
        console.error('Error adding tag:', error)
        showError('Error adding tag. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    const isTagUsed = (tagId) => {
      // Check if the tag is used by any card
      return cards.value.some(card => {
        return card.tags && card.tags.includes(tagId)
      })
    }
    
    const deleteTag = async (tagId, tagName) => {
      // Check if the tag is used
      if (isTagUsed(tagId)) {
        showError(`Cannot delete tag "${tagName}" because it is used by some cards.`)
        return
      }
      
      // Confirm deletion
      if (!confirm(`Are you sure you want to delete tag "${tagName}"?`)) {
        return
      }
      
      saving.value = true
      try {
        await apiService.delete(`/tags/${tagId}`)
        showSuccess('Tag deleted successfully!')
        // Reload tags
        await loadTags()
      } catch (error) {
        console.error('Error deleting tag:', error)
        showError('Error deleting tag. Please try again.')
      } finally {
        saving.value = false
      }
    }
    
    onMounted(async () => {
      await loadMaterials()
      await loadCards()
      await loadTags()
    })
    
    return {
      cards,
      materials,
      tags,
      loading,
      updating,
      saving,
      showEditModal,
      showAddCardModal,
      showTagManagementModal,
      showTagGuideModal,
      isDropdownOpen,
      selectedMaterial,
      selectedTag,
      selectedTags,
      searchTerm,
      editForm,
      tagForm,
      cardForm,
      filteredCards,
      loadCards,
      loadTags,
      editCard,
      updateCard,
      deleteCard,
      addTagInManagement,
      addCard,
      openTagManagement,
      closeTagManagementModal,
      openTagGuide,
      closeTagGuideModal,
      toggleDropdown,
      closeDropdown,
      toggleTagSelection,
      associateTagsWithCard,
      closeEditModal,
      closeAddCardModal,
      getMaterialTitle,
      getTagName,
      formatDate,
      filterCards,
      clearFilters,
      isTagUsed
    }
  }
}
</script>

<style scoped>
/* Vocabulary Management Styles */
.vocabulary {
  min-height: 100vh;
  padding: var(--space-8) var(--space-4);
}

/* Header Styles */
.vocabulary-header {
  margin-bottom: var(--space-10);
  text-align: center;
}

.vocabulary-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-3);
  background: linear-gradient(135deg, var(--primary-600), var(--secondary-600));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: var(--tracking-tight);
}

.vocabulary-subtitle {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
  line-height: var(--leading-relaxed);
}

/* Actions Section */
.actions-section {
  margin-bottom: var(--space-8);
}

.action-buttons {
  display: flex;
  gap: var(--space-4);
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .btn {
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

.btn-icon {
  font-size: var(--text-lg);
}

.btn-text {
  font-size: var(--text-sm);
  letter-spacing: var(--tracking-wide);
}

.btn-outline {
  background-color: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--surface-border);
  position: relative;
}

.btn-outline:hover {
  background-color: var(--primary-50);
  color: var(--primary-600);
  border-color: var(--primary-400);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-arrow {
  font-size: var(--text-xs);
  transition: transform var(--transition-normal) var(--transition-ease-in-out);
}

.btn-arrow.rotated {
  transform: rotate(180deg);
}

.action-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: var(--space-2);
  background-color: var(--surface-primary);
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  min-width: 220px;
  z-index: var(--z-dropdown);
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  width: 100%;
  text-align: left;
  background-color: transparent;
  border: none;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.btn-error:hover {
  background-color: var(--error-700);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-arrow {
  font-size: var(--text-xs);
  transition: transform var(--transition-normal) var(--transition-ease-in-out);
}

.btn-arrow.rotated {
  transform: rotate(180deg);
}

.action-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: var(--space-2);
  background-color: var(--surface-primary);
  border: 1px solid var(--surface-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  min-width: 220px;
  z-index: var(--z-dropdown);
  animation: fadeIn var(--transition-normal) var(--transition-ease-out);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  width: 100%;
  text-align: left;
  background-color: transparent;
  border: none;
  cursor: pointer;
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  color: var(--text-secondary);
  border-radius: var(--radius-lg);
  margin: var(--space-1);
}

.dropdown-item:hover {
  background-color: var(--primary-50);
  color: var(--primary-600);
  transform: translateX(4px);
}

.item-icon {
  font-size: var(--text-base);
}

.item-text {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

/* Filters Section */
.filters-card {
  margin-bottom: var(--space-8);
  padding: var(--space-6);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  background-color: var(--surface-primary);
  border: 1px solid var(--surface-border);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
}

.filter-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.filter-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-6);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.filter-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  letter-spacing: var(--tracking-wide);
  margin-bottom: var(--space-1);
  text-transform: uppercase;
}

.search-group {
  position: relative;
}

.search-input-group {
  position: relative;
}

.search-input-group .form-control {
  padding-right: var(--space-12);
}

.search-clear-btn {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background-color: transparent;
  border: none;
  color: var(--text-light);
  font-size: var(--text-lg);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-full);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.search-clear-btn:hover {
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
}

/* Cards Section */
.cards-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
}

.cards-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.cards-count {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background-color: var(--primary-50);
  color: var(--primary-600);
}

/* Loading State */
.loading-state {
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

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

.loading-text {
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

.empty-state-icon {
  font-size: 4rem;
  margin-bottom: var(--space-4);
}

.empty-state-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.empty-state-description {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  max-width: 500px;
  margin: 0;
  line-height: var(--leading-relaxed);
}

/* Cards List */
.cards-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.card-item {
  transition: all var(--transition-normal) var(--transition-ease-in-out);
  animation: fadeIn var(--transition-slow) var(--transition-ease-out);
}

.card-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-200);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--surface-border);
}

.card-text-container {
  flex: 1;
}

.card-text {
  margin: 0 0 var(--space-3) 0;
  color: var(--text-primary);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  line-height: var(--leading-tight);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.card-actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}

.card-details {
  margin-top: var(--space-4);
}

.card-context,
.card-comment {
  margin-bottom: var(--space-6);
}

.detail-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  letter-spacing: var(--tracking-wide);
  margin-bottom: var(--space-2);
  text-transform: uppercase;
}

.detail-content {
  margin: 0;
  padding: var(--space-4);
  background-color: var(--bg-secondary);
  border-radius: var(--radius-xl);
  border-left: 4px solid var(--primary-500);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  font-size: var(--text-sm);
}

.card-meta {
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--surface-border);
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--space-4);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.meta-label {
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-light);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
}

.meta-value {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* Modal Styles */
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

.form-group {
  margin-bottom: var(--space-6);
}

.form-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: flex-end;
  margin-top: var(--space-8);
  padding-top: var(--space-6);
  border-top: 1px solid var(--surface-border);
}

/* Tag Selection */
.tag-selection {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  margin-top: var(--space-2);
}

.tag-checkbox {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex: 1 1 200px;
}

.tag-checkbox input[type="checkbox"] {
  transform: scale(1.1);
  accent-color: var(--primary-500);
}

/* Tag Management */
.tag-management {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.tag-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  max-height: 400px;
  overflow-y: auto;
  padding-right: var(--space-2);
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--space-4);
  border-radius: var(--radius-xl);
  background-color: var(--surface-secondary);
  border: 1px solid var(--surface-border);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.tag-item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-200);
}

.tag-info {
  flex: 1;
}

.tag-info h5 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

.tag-info p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
}

.tag-actions-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--surface-border);
}

/* Tag Guide */
.tag-guide {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.guide-section {
  padding: var(--space-6);
  border-radius: var(--radius-xl);
  background-color: var(--surface-secondary);
  border: 1px solid var(--surface-border);
  transition: all var(--transition-normal) var(--transition-ease-in-out);
}

.guide-section:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-200);
}

.guide-section h4 {
  margin: 0 0 var(--space-4) 0;
  color: var(--text-primary);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
}

.guide-section p {
  margin: 0 0 var(--space-4) 0;
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
}

.guide-section ul {
  margin: 0;
  padding-left: var(--space-6);
  color: var(--text-secondary);
}

.guide-section li {
  margin-bottom: var(--space-2);
  line-height: var(--leading-relaxed);
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
  .vocabulary {
    padding: var(--space-6) var(--space-3);
  }

  .vocabulary-title {
    font-size: var(--text-2xl);
  }

  .vocabulary-subtitle {
    font-size: var(--text-base);
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .action-buttons .btn {
    justify-content: center;
  }

  .filter-controls {
    grid-template-columns: 1fr;
  }

  .cards-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }

  .card-actions {
    align-self: flex-end;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }

  .tag-checkbox {
    flex: 1 1 100%;
  }

  .form-actions {
    flex-direction: column;
  }

  .tag-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .tag-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .tag-actions-footer {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .vocabulary-title {
    font-size: var(--text-xl);
  }

  .filter-controls {
    gap: var(--space-4);
  }

  .card-text {
    font-size: var(--text-lg);
  }

  .modal {
    width: 95%;
    margin: var(--space-2);
  }

  .modal-header,
  .modal-body {
    padding: var(--space-4);
  }
}
</style>