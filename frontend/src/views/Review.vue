<template>
  <div class="review">
    <h2 class="page-title">Review Sessions</h2>
    
    <div class="review-start">
      <div class="card text-center">
        <h3 class="card-title">Start a New Review Session</h3>
        <p class="card-subtitle">Review your highlighted vocabulary using spaced repetition.</p>
        
        <div class="review-stats mb-3">
          <div class="stat-item">
            <strong>{{ pendingReviews }}</strong>
            <span>Due for Review</span>
          </div>
          <div class="stat-item">
            <strong>{{ totalHighlights }}</strong>
            <span>Total Vocabulary</span>
          </div>
        </div>
        
        <!-- Step 1: Select materials -->
        <div class="material-selection mt-3" v-if="materials.length">
          <h4 class="selection-title">Step 1: Choose study materials</h4>
          <p class="selection-help">
            Select one material to view its vocabulary highlights.
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
              />
              <span class="material-radio-label">{{ material.title }}</span>
            </label>
          </div>
        </div>
        
        <!-- Step 2: Show vocabulary of the selected materials -->
        <div v-if="selectedMaterialId && materialHighlights.length" class="highlight-selection mt-3">
          <h4 class="selection-title">Step 2: Choose vocabulary for this session</h4>
          <p class="selection-help">
            Select the words you want to add to your new review session.
          </p>
          <!-- Search input for highlights -->
          <div class="search-container mb-3">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="Search highlights..."
              class="search-input"
            />
            <div class="search-options">
              <label class="similar-search-toggle">
                <input
                  type="checkbox"
                  v-model="similarSearchEnabled"
                />
                <span>Include similar words</span>
              </label>
            </div>
          </div>
          <div class="selection-list">
            <label
              v-for="h in filteredHighlights"
              :key="h.id"
              class="selection-item"
            >
              <input
                type="checkbox"
                :value="h.id"
                v-model="selectedHighlightIds"
              />
              <span class="selection-text">{{ h.text }}</span>
            </label>
          </div>
        </div>
        
        <!-- Step 3: Show selected highlights with source information -->
        <div v-if="selectedHighlightsList.length" class="selected-highlights-summary mt-3">
          <h4 class="selection-title">Step 3: Selected highlights ({{ selectedHighlightsList.length }})</h4>
          <p class="selection-help">
            Review your selected highlights before starting the session.
          </p>
          <div class="selected-highlights-list">
            <div
              v-for="highlight in selectedHighlightsList"
              :key="highlight.id"
              class="selected-highlight-item"
            >
              <div class="highlight-info">
                <span class="highlight-text">{{ highlight.text }}</span>
                <span class="highlight-source">From: {{ highlight.materialTitle }}</span>
              </div>
              <button
                @click="deleteHighlight(highlight.id)"
                class="delete-btn"
                title="Remove from selection"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <button 
          @click="startReviewSession" 
          :disabled="starting || (selectedMaterialId && selectedHighlightIds.length === 0)"
          class="btn mt-3"
        >
          {{ starting ? 'Starting...' : (selectedHighlightIds.length ? 'Start Selected Review Session' : 'Start Review Session') }}
        </button>
        
        <p v-if="totalHighlights === 0" class="mt-3">
          No vocabulary highlights found. Please add some highlights first!
        </p>
        
        <p v-if="materials.length === 0" class="mt-3">
          No study materials found. Please upload some materials first!
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useApiService } from '../composables/useApiService'
import { useMaterialService } from '../services/materialService'
import { useVocabularyService } from '../services/vocabularyService'

export default {
  name: 'Review',
  setup() {
    const router = useRouter()
    const pendingReviews = ref(0)
    const totalHighlights = ref(0)
    const starting = ref(false)
    const highlights = ref([])
    const selectedHighlightIds = ref([])
    const materials = ref([])
    const selectedMaterialId = ref('')
    const materialHighlights = ref([])
    const searchQuery = ref('')
    const similarSearchEnabled = ref(false)
    
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
    
    // Computed property for filtered highlights
    const filteredHighlights = computed(() => {
      if (!searchQuery.value) {
        return materialHighlights.value
      }
      
      const query = searchQuery.value.toLowerCase()
      
      return materialHighlights.value.filter(h => {
        const highlightText = h.text.toLowerCase()
        
        // Exact match
        if (highlightText.includes(query)) {
          return true
        }
        
        // Similar word search if enabled
        if (similarSearchEnabled.value) {
          // Split into words and check similarity for each word
          const words = highlightText.split(/\s+/)
          return words.some(word => {
            const similarity = getSimilarity(query, word)
            return similarity > 0.6 // 60% similarity threshold
          })
        }
        
        return false
      })
    })
    
    // Computed property for selected highlights with material information
    const selectedHighlightsList = computed(() => {
      return selectedHighlightIds.value.map(id => {
        const highlight = highlights.value.find(h => h.id === id)
        if (highlight) {
          const material = materials.value.find(m => m.id === highlight.materialId)
          return {
            ...highlight,
            materialTitle: material ? material.title : 'Unknown'
          }
        }
        return null
      }).filter(h => h !== null)
    })
    
    // Method to delete a highlight from selection
    const deleteHighlight = (highlightId) => {
      const index = selectedHighlightIds.value.indexOf(highlightId)
      if (index > -1) {
        selectedHighlightIds.value.splice(index, 1)
      }
    }
    
    const { apiService } = useApiService()
    const { getAllMaterials } = useMaterialService()
    const { getHighlightsByMaterial, getAllHighlights } = useVocabularyService()
    
    const loadReviewData = async () => {
      try {
        // Load materials
        const materialsData = await getAllMaterials()
        materials.value = materialsData
        
        // Load all highlights
        const allHighlights = await getAllHighlights()
        highlights.value = allHighlights
        totalHighlights.value = allHighlights.length

        // For now, just set pending reviews to total highlights to enable the button
        pendingReviews.value = totalHighlights.value
      } catch (error) {
        console.error('Error loading review data:', error)
      }
    }
    
    const onMaterialsChange = async () => {
      materialHighlights.value = []
      
      if (selectedMaterialId.value) {
        try {
          // Load highlights for the selected material
          const highlightsData = await getHighlightsByMaterial(selectedMaterialId.value)
          // Add material information to each highlight for grouping
          const highlightsWithMaterial = highlightsData.map(h => ({
            ...h,
            materialId: selectedMaterialId.value
          }))
          materialHighlights.value = highlightsWithMaterial
        } catch (error) {
          console.error('Error loading material highlights:', error)
        }
      }
    }
    
    const startReviewSession = async () => {
      starting.value = true
      try {
        let response
        if (selectedHighlightIds.value.length > 0) {
          response = await apiService.post('/reviews/sessions/custom', selectedHighlightIds.value)
        } else {
          response = await apiService.post('/reviews/sessions')
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
    
    onMounted(() => {
      console.log('Debug: Component mounted, calling loadReviewData')
      loadReviewData()
    })
    
    return {
      pendingReviews,
      totalHighlights,
      starting,
      highlights,
      selectedHighlightIds,
      materials,
      selectedMaterialId,
      materialHighlights,
      searchQuery,
      similarSearchEnabled,
      filteredHighlights,
      selectedHighlightsList,
      onMaterialsChange,
      startReviewSession,
      deleteHighlight,
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
  color: #2c3e50;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 2rem 0 1.5rem;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-title {
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0 0 0.75rem;
}

.card-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
  margin: 0 0 2rem;
  line-height: 1.5;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3, #f72585);
}

.text-center {
  text-align: center;
}

.btn {
  display: inline-block;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  min-width: 240px;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #3a0ca3, #4361ee);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(67, 97, 238, 0.4);
}

.btn:disabled {
  background: linear-gradient(135deg, #adb5bd, #6c757d);
  cursor: not-allowed;
  opacity: 0.7;
  transform: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
}

.review-stats {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin: 2rem 0;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.stat-item {
  text-align: center;
  min-width: 120px;
}

.stat-item strong {
  display: block;
  font-size: 2.5rem;
  font-weight: 700;
  color: #4361ee;
  margin-bottom: 0.5rem;
  transition: transform 0.3s ease;
}

.stat-item:hover strong {
  transform: scale(1.1);
}

.stat-item span {
  color: #6c757d;
  font-size: 1rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mt-3 {
  margin-top: 1rem;
}

.mb-3 {
  margin-bottom: 1rem;
}

.highlight-selection {
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
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 1.3rem;
  font-weight: 600;
}

.selection-help {
  margin: 0 0 1rem 0;
  color: #6c757d;
  font-size: 1rem;
  line-height: 1.4;
}

.selection-list {
  max-height: 250px;
  overflow: auto;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  padding: 1rem;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.selection-list:hover {
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
}

.selection-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.25rem;
}

.selection-item:hover {
  background: rgba(67, 97, 238, 0.05);
}

.selection-item input[type="checkbox"] {
  transform: scale(1.2);
  accent-color: #4361ee;
}

.selection-text {
  color: #2c3e50;
  word-break: break-word;
  font-size: 1rem;
  font-weight: 500;
}

.material-dropdown {
  max-width: 100%;
  margin: 0 auto;
}

.material-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 1rem;
  background-color: #fff;
  cursor: pointer;
}

.material-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.material-radio-list {
  max-height: 250px;
  overflow: auto;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  padding: 1rem;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.material-radio-list:hover {
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
}

.material-radio-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.25rem;
}

.material-radio-item:hover {
  background: rgba(67, 97, 238, 0.05);
}

.material-radio-item input[type="radio"] {
  transform: scale(1.2);
  accent-color: #4361ee;
}

.material-radio-label {
  color: #2c3e50;
  word-break: break-word;
  font-size: 1rem;
  font-weight: 500;
}

.search-container {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.search-input:focus {
  outline: none;
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  background: white;
}

.search-input::placeholder {
  color: #6c757d;
  font-style: italic;
}

.search-options {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.similar-search-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
  color: #2c3e50;
  transition: all 0.2s ease;
}

.similar-search-toggle:hover {
  color: #4361ee;
}

.similar-search-toggle input[type="checkbox"] {
  transform: scale(1.1);
  accent-color: #4361ee;
}

.selected-highlights-summary {
  text-align: left;
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.selected-highlights-list {
  max-height: 300px;
  overflow: auto;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  padding: 1rem;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.selected-highlights-list:hover {
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
}

.selected-highlight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: white;
  border-radius: 8px;
  margin-bottom: 0.5rem;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.selected-highlight-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.selected-highlight-item:last-child {
  margin-bottom: 0;
}

.highlight-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
  min-width: 0;
}

.highlight-text {
  color: #2c3e50;
  font-weight: 500;
  font-size: 1rem;
  word-break: break-word;
}

.highlight-source {
  color: #6c757d;
  font-size: 0.85rem;
  font-style: italic;
  background: #e9ecef;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
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
  background: #dc3545;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-left: 1rem;
}

.delete-btn:hover {
  background: #c82333;
  transform: scale(1.1);
}

@media (max-width: 768px) {
  .review-stats {
    flex-direction: column;
    gap: 1rem;
  }
  
  .btn {
    width: 100%;
  }
}
</style>