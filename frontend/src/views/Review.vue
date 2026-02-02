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
            Select one or more materials to view their vocabulary highlights.
          </p>
          <div class="material-checkbox-list">
            <label
              v-for="material in materials"
              :key="material.id"
              class="material-checkbox-item"
            >
              <input
                type="checkbox"
                :value="material.id"
                v-model="selectedMaterialIds"
                @change="onMaterialsChange"
              />
              <span class="material-checkbox-label">{{ material.title }}</span>
            </label>
          </div>
        </div>
        
        <!-- Step 2: Show vocabulary of the selected materials -->
        <div v-if="selectedMaterialIds.length && materialHighlights.length" class="highlight-selection mt-3">
          <h4 class="selection-title">Step 2: Choose vocabulary for this session</h4>
          <p class="selection-help">
            Select the words you want to add to your new review session.
          </p>
          <div class="selection-list">
            <label
              v-for="h in materialHighlights"
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

        <button 
          @click="startReviewSession" 
          :disabled="starting || (selectedMaterialIds.length > 0 && selectedHighlightIds.length === 0)"
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
import { ref, onMounted } from 'vue'
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
    const selectedMaterialIds = ref([])
    const materialHighlights = ref([])
    
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
      selectedHighlightIds.value = []
      materialHighlights.value = []
      
      if (selectedMaterialIds.value.length > 0) {
        try {
          // Load highlights for each selected material
          const allHighlights = []
          for (const materialId of selectedMaterialIds.value) {
            const highlightsData = await getHighlightsByMaterial(materialId)
            // Add material information to each highlight for grouping
            const highlightsWithMaterial = highlightsData.map(h => ({
              ...h,
              materialId: materialId
            }))
            allHighlights.push(...highlightsWithMaterial)
          }
          materialHighlights.value = allHighlights
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
      selectedMaterialIds,
      materialHighlights,
      onMaterialsChange,
      startReviewSession,
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

.material-checkbox-list {
  max-height: 250px;
  overflow: auto;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  padding: 1rem;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.material-checkbox-list:hover {
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
}

.material-checkbox-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.25rem;
}

.material-checkbox-item:hover {
  background: rgba(67, 97, 238, 0.05);
}

.material-checkbox-item input[type="checkbox"] {
  transform: scale(1.2);
  accent-color: #4361ee;
}

.material-checkbox-label {
  color: #2c3e50;
  word-break: break-word;
  font-size: 1rem;
  font-weight: 500;
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