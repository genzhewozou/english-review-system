<template>
  <div class="vocabulary">
    <h2>Vocabulary Management</h2>
    
    <div class="filters mb-3">
      <div class="d-flex gap-1">
        <select v-model="selectedMaterial" @change="loadHighlights" class="form-control">
          <option value="">All Materials</option>
          <option v-for="material in materials" :key="material.id" :value="material.id">
            {{ material.title }}
          </option>
        </select>
        <input 
          v-model="searchTerm" 
          @input="filterHighlights"
          type="text" 
          placeholder="Search vocabulary..." 
          class="form-control"
        >
      </div>
    </div>
    
    <div v-if="loading" class="text-center">
      <div class="spinner"></div>
    </div>
    
    <div v-else-if="filteredHighlights.length === 0" class="card text-center">
      <p>No vocabulary found. Start highlighting words in your study materials!</p>
    </div>
    
    <div v-else class="highlights-list">
      <div v-for="highlight in filteredHighlights" :key="highlight.id" class="card highlight-card">
        <div class="highlight-header">
          <h4 class="highlight-text">{{ highlight.text }}</h4>
          <div class="highlight-actions">
            <button @click="editHighlight(highlight)" class="btn btn-secondary">Edit</button>
            <button @click="deleteHighlight(highlight.id)" class="btn btn-danger">Delete</button>
          </div>
        </div>
        
        <div class="highlight-details">
          <div class="highlight-context" v-if="highlight.context">
            <strong>Context:</strong>
            <p>{{ highlight.context }}</p>
          </div>
          
          <div class="highlight-comment" v-if="highlight.userComment">
            <strong>Your Notes:</strong>
            <p>{{ highlight.userComment }}</p>
          </div>
          
          <div class="highlight-meta">
            <span class="meta-item">
              <strong>Material:</strong> {{ getMaterialTitle(highlight.materialId) }}
            </span>
            <span class="meta-item">
              <strong>Added:</strong> {{ formatDate(highlight.createdDate) }}
            </span>
            <span class="meta-item" v-if="highlight.nextReviewDate">
              <strong>Next Review:</strong> {{ formatDate(highlight.nextReviewDate) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Edit Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Edit Highlight</h3>
          <button @click="closeEditModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateHighlight">
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
              <button type="submit" class="btn" :disabled="updating">
                {{ updating ? 'Updating...' : 'Update' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useApiService } from '../composables/useApiService'

export default {
  name: 'Vocabulary',
  setup() {
    const highlights = ref([])
    const materials = ref([])
    const loading = ref(false)
    const updating = ref(false)
    const showEditModal = ref(false)
    const selectedMaterial = ref('')
    const searchTerm = ref('')
    
    const editForm = ref({
      id: null,
      text: '',
      userComment: ''
    })
    
    const { apiService } = useApiService()
    
    const filteredHighlights = computed(() => {
      let filtered = highlights.value
      
      if (selectedMaterial.value) {
        filtered = filtered.filter(h => h.materialId === parseInt(selectedMaterial.value))
      }
      
      if (searchTerm.value) {
        const term = searchTerm.value.toLowerCase()
        filtered = filtered.filter(h => 
          h.text.toLowerCase().includes(term) ||
          (h.userComment && h.userComment.toLowerCase().includes(term)) ||
          (h.context && h.context.toLowerCase().includes(term))
        )
      }
      
      return filtered
    })
    
    const loadMaterials = async () => {
      try {
        const response = await apiService.get('/materials')
        materials.value = response.data || []
      } catch (error) {
        console.error('Error loading materials:', error)
      }
    }
    
    const loadHighlights = async () => {
      loading.value = true
      try {
        const url = selectedMaterial.value 
          ? `/vocabulary/material/${selectedMaterial.value}`
          : '/vocabulary'
        const response = await apiService.get(url)
        highlights.value = response.data || []
      } catch (error) {
        console.error('Error loading highlights:', error)
        highlights.value = []
      } finally {
        loading.value = false
      }
    }
    
    const editHighlight = (highlight) => {
      editForm.value = {
        id: highlight.id,
        text: highlight.text,
        userComment: highlight.userComment || ''
      }
      showEditModal.value = true
    }
    
    const updateHighlight = async () => {
      updating.value = true
      try {
        await apiService.put(`/vocabulary/${editForm.value.id}`, {
          userComment: editForm.value.userComment
        })
        
        // Update local data
        const index = highlights.value.findIndex(h => h.id === editForm.value.id)
        if (index !== -1) {
          highlights.value[index].userComment = editForm.value.userComment
        }
        
        closeEditModal()
      } catch (error) {
        console.error('Error updating highlight:', error)
        alert('Error updating highlight. Please try again.')
      } finally {
        updating.value = false
      }
    }
    
    const deleteHighlight = async (id) => {
      if (!confirm('Are you sure you want to delete this highlight?')) return
      
      try {
        await apiService.delete(`/vocabulary/${id}`)
        highlights.value = highlights.value.filter(h => h.id !== id)
      } catch (error) {
        console.error('Error deleting highlight:', error)
        alert('Error deleting highlight. Please try again.')
      }
    }
    
    const closeEditModal = () => {
      showEditModal.value = false
      editForm.value = { id: null, text: '', userComment: '' }
    }
    
    const getMaterialTitle = (materialId) => {
      const material = materials.value.find(m => m.id === materialId)
      return material ? material.title : 'Unknown Material'
    }
    
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString()
    }
    
    const filterHighlights = () => {
      // Reactive computed property handles this automatically
    }
    
    onMounted(async () => {
      await loadMaterials()
      await loadHighlights()
    })
    
    return {
      highlights,
      materials,
      loading,
      updating,
      showEditModal,
      selectedMaterial,
      searchTerm,
      editForm,
      filteredHighlights,
      loadHighlights,
      editHighlight,
      updateHighlight,
      deleteHighlight,
      closeEditModal,
      getMaterialTitle,
      formatDate,
      filterHighlights
    }
  }
}
</script>

<style scoped>
.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.gap-1 {
  gap: 1rem;
}

.highlights-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.highlight-card {
  transition: transform 0.2s;
}

.highlight-card:hover {
  transform: translateY(-1px);
}

.highlight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.highlight-text {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.highlight-actions {
  display: flex;
  gap: 0.5rem;
}

.highlight-context,
.highlight-comment {
  margin-bottom: 1rem;
}

.highlight-context p,
.highlight-comment p {
  margin: 0.5rem 0 0 0;
  padding: 0.75rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #007bff;
}

.highlight-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.meta-item {
  display: flex;
  align-items: center;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  padding: 1.5rem;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }
  
  .highlight-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .highlight-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>