<template>
  <div class="material-viewer">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <!-- Error State -->
    <div v-else-if="!material" class="error-state">
      <el-result
        icon="warning"
        title="Material Not Found"
        sub-title="The requested material could not be found."
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/materials')">
            Back to Materials
          </el-button>
        </template>
      </el-result>
    </div>
    
    <!-- Material Content -->
    <div v-else class="material-container">
      <!-- Material Header -->
      <div class="material-header">
        <div class="header-content">
          <div class="material-info">
            <h1 class="material-title">{{ material.title }}</h1>
            <div class="material-meta">
              <el-tag :type="getTypeTagType(material.type)" size="small">
                {{ formatMaterialType(material.type) }}
              </el-tag>
              <span class="meta-item">
                <el-icon><Document /></el-icon>
                {{ material.fileName }}
              </span>
              <span class="meta-item">
                <el-icon><DataAnalysis /></el-icon>
                {{ formatFileSize(material.fileSize) }}
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(material.createdDate) }}
              </span>
            </div>
          </div>
          
          <div class="header-actions">
            <el-button @click="$router.push('/materials')" :icon="ArrowLeft">
              Back
            </el-button>
            <el-button @click="downloadMaterial" :icon="Download">
              Download
            </el-button>
            <el-button 
              type="primary" 
              @click="toggleHighlightMode" 
              :icon="EditPen"
              :class="{ 'is-active': isHighlightMode }"
            >
              {{ isHighlightMode ? 'Exit Highlight' : 'Highlight Mode' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- Mode Toggle -->
      <div v-if="isHighlightMode" class="highlight-toolbar">
        <el-alert
          title="Highlight Mode Active"
          description="Select text to create highlights. Click on existing highlights to edit them."
          type="info"
          :closable="false"
          show-icon
        />
      </div>

      <!-- Main Content Area -->
      <div class="content-area">
        <!-- Material Viewer -->
        <div class="material-content" :class="{ 'highlight-mode': isHighlightMode }">
          <!-- Document Viewer -->
          <div v-if="material.type === 'DOCUMENT'" class="document-viewer">
            <DocumentViewer
              :material="material"
              :highlights="highlights"
              :highlight-mode="isHighlightMode"
              @text-selected="handleTextSelection"
              @highlight-clicked="handleHighlightClick"
            />
          </div>
          
          <!-- Video Player -->
          <div v-else-if="material.type === 'VIDEO'" class="video-viewer">
            <el-card>
              <VideoPlayer
                :material="material"
                :highlights="highlights"
                :highlight-mode="isHighlightMode"
                @text-selected="handleTextSelection"
                @highlight-clicked="handleHighlightClick"
              />
            </el-card>
          </div>
          
          <!-- Article Viewer -->
          <div v-else-if="material.type === 'ARTICLE'" class="article-viewer">
            <el-card>
              <ArticleViewer
                :material="material"
                :highlights="highlights"
                :highlight-mode="isHighlightMode"
                @text-selected="handleTextSelection"
                @highlight-clicked="handleHighlightClick"
              />
            </el-card>
          </div>
        </div>

        <!-- Highlights Sidebar -->
        <div class="highlights-sidebar">
          <el-card>
            <template #header>
              <div class="sidebar-header">
                <h3>Highlights</h3>
                <el-badge :value="highlights.length" type="primary" />
              </div>
            </template>

            <!-- Highlights List -->
            <div v-if="highlights.length === 0" class="empty-highlights">
              <el-empty
                description="No highlights yet"
                :image-size="80"
              >
                <p>Start highlighting text to create vocabulary entries!</p>
              </el-empty>
            </div>
            
            <div v-else class="highlights-list">
              <HighlightItem
                v-for="highlight in sortedHighlights"
                :key="highlight.id"
                :highlight="highlight"
                @edit="editHighlight"
                @delete="deleteHighlight"
                @click="scrollToHighlight"
              />
            </div>
          </el-card>
        </div>
      </div>

      <!-- Highlight Creation Dialog -->
      <el-dialog
        v-model="showHighlightDialog"
        title="Create Highlight"
        width="500px"
        @close="resetHighlightDialog"
      >
        <HighlightForm
          :selected-text="selectedText"
          :context="selectedContext"
          :material-id="material.id"
          @save="handleHighlightSave"
          @cancel="resetHighlightDialog"
        />
      </el-dialog>

      <!-- Highlight Edit Dialog -->
      <el-dialog
        v-model="showEditDialog"
        title="Edit Highlight"
        width="500px"
        @close="resetEditDialog"
      >
        <HighlightForm
          :highlight="editingHighlight"
          :material-id="material.id"
          @save="handleHighlightUpdate"
          @cancel="resetEditDialog"
        />
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  ArrowLeft,
  Download,
  EditPen,
  Document,
  DataAnalysis,
  Calendar
} from '@element-plus/icons-vue'
import { useApiService } from '../composables/useApiService'
import DocumentViewer from '../components/DocumentViewer.vue'
import VideoPlayer from '../components/VideoPlayer.vue'
import ArticleViewer from '../components/ArticleViewer.vue'
import HighlightItem from '../components/HighlightItem.vue'
import HighlightForm from '../components/HighlightForm.vue'

export default {
  name: 'MaterialViewer',
  components: {
    DocumentViewer,
    VideoPlayer,
    ArticleViewer,
    HighlightItem,
    HighlightForm
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const material = ref(null)
    const highlights = ref([])
    const loading = ref(false)
    
    // Highlight mode state
    const isHighlightMode = ref(false)
    const showHighlightDialog = ref(false)
    const showEditDialog = ref(false)
    const selectedText = ref('')
    const selectedContext = ref('')
    const selectedTextPosition = ref({ startPosition: 0, endPosition: 0 })
    const editingHighlight = ref(null)
    
    const { apiService } = useApiService()
    
    // Computed properties
    const sortedHighlights = computed(() => {
      return [...highlights.value].sort((a, b) => {
        // Sort by position if available, otherwise by creation date
        if (a.startPosition !== undefined && b.startPosition !== undefined) {
          return a.startPosition - b.startPosition
        }
        return new Date(b.createdDate) - new Date(a.createdDate)
      })
    })

    const materialTypeColors = {
      VIDEO: 'primary',
      DOCUMENT: 'success',
      ARTICLE: 'warning'
    }

    const getTypeTagType = (type) => {
      return materialTypeColors[type] || 'info'
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
        
        // Load highlights for this material
        await loadHighlights(materialId)
        
        // Check if we should start in highlight mode
        if (route.query.mode === 'highlight') {
          isHighlightMode.value = true
        }
      } catch (error) {
        console.error('Error loading material:', error)
        ElMessage.error('Failed to load material')
        material.value = null
      } finally {
        loading.value = false
      }
    }
    
    const loadHighlights = async (materialId) => {
      try {
        const response = await apiService.get(`/vocabulary/materials/${materialId}/highlights`)
        highlights.value = response.data || []
      } catch (error) {
        console.error('Error loading highlights:', error)
        highlights.value = []
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
        ElMessage.error('Failed to download material')
      }
    }

    const toggleHighlightMode = () => {
      isHighlightMode.value = !isHighlightMode.value
      
      if (isHighlightMode.value) {
        ElMessage.info('Highlight mode activated. Select text to create highlights.')
      } else {
        ElMessage.info('Highlight mode deactivated.')
      }
    }

    const handleTextSelection = (selection) => {
      console.log('MaterialViewer: handleTextSelection called', { selection, isHighlightMode: isHighlightMode.value })
      
      if (!isHighlightMode.value) {
        console.log('MaterialViewer: Not in highlight mode, returning')
        return
      }
      
      selectedText.value = selection.text
      selectedContext.value = selection.context
      
      // Store the position data for later use
      selectedTextPosition.value = {
        startPosition: selection.startPosition,
        endPosition: selection.endPosition
      }
      
      console.log('MaterialViewer: Showing highlight dialog', { selectedText: selectedText.value })
      showHighlightDialog.value = true
    }

    const handleHighlightClick = (highlight) => {
      if (isHighlightMode.value) {
        editingHighlight.value = highlight
        showEditDialog.value = true
      }
    }

    const handleHighlightSave = async (highlightData) => {
      try {
        const response = await apiService.post('/vocabulary/highlights', {
          materialId: material.value.id,
          text: selectedText.value,
          context: selectedContext.value,
          startPosition: selectedTextPosition.value.startPosition,
          endPosition: selectedTextPosition.value.endPosition,
          userComment: highlightData.comment
        })
        
        highlights.value.push(response.data)
        resetHighlightDialog()
        ElMessage.success('Highlight created successfully')
      } catch (error) {
        console.error('Error creating highlight:', error)
        ElMessage.error('Failed to create highlight')
      }
    }

    const handleHighlightUpdate = async (highlightData) => {
      try {
        const response = await apiService.put(`/vocabulary/highlights/${editingHighlight.value.id}`, {
          userComment: highlightData.comment
        })
        
        const index = highlights.value.findIndex(h => h.id === editingHighlight.value.id)
        if (index !== -1) {
          highlights.value[index] = response.data
        }
        
        resetEditDialog()
        ElMessage.success('Highlight updated successfully')
      } catch (error) {
        console.error('Error updating highlight:', error)
        ElMessage.error('Failed to update highlight')
      }
    }

    const editHighlight = (highlight) => {
      editingHighlight.value = highlight
      showEditDialog.value = true
    }
    
    const deleteHighlight = async (highlight) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete the highlight "${highlight.text}"?`,
          'Confirm Deletion',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )
        
        await apiService.delete(`/vocabulary/highlights/${highlight.id}`)
        highlights.value = highlights.value.filter(h => h.id !== highlight.id)
        ElMessage.success('Highlight deleted successfully')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting highlight:', error)
          ElMessage.error('Failed to delete highlight')
        }
      }
    }

    const scrollToHighlight = (highlight) => {
      // This would scroll to the highlight in the document
      // Implementation depends on the specific viewer component
      console.log('Scrolling to highlight:', highlight.id)
    }

    const resetHighlightDialog = () => {
      showHighlightDialog.value = false
      selectedText.value = ''
      selectedContext.value = ''
      selectedTextPosition.value = { startPosition: 0, endPosition: 0 }
    }

    const resetEditDialog = () => {
      showEditDialog.value = false
      editingHighlight.value = null
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
      if (event.key === 'Escape' && isHighlightMode.value) {
        toggleHighlightMode()
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
      highlights,
      loading,
      isHighlightMode,
      showHighlightDialog,
      showEditDialog,
      selectedText,
      selectedContext,
      editingHighlight,
      sortedHighlights,
      getTypeTagType,
      formatMaterialType,
      downloadMaterial,
      toggleHighlightMode,
      handleTextSelection,
      handleHighlightClick,
      handleHighlightSave,
      handleHighlightUpdate,
      editHighlight,
      deleteHighlight,
      scrollToHighlight,
      resetHighlightDialog,
      resetEditDialog,
      formatFileSize,
      formatDate,
      ArrowLeft,
      Download,
      EditPen,
      Document,
      DataAnalysis,
      Calendar
    }
  }
}
</script>

<style scoped>
.material-viewer {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem;
}

.loading-container {
  padding: 2rem;
}

.error-state {
  padding: 3rem 1rem;
}

.material-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.material-header {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.material-info {
  flex: 1;
  min-width: 0;
}

.material-title {
  margin: 0 0 1rem 0;
  color: #303133;
  font-size: 1.75rem;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-word;
}

.material-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #606266;
  font-size: 0.9rem;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.header-actions .el-button.is-active {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.highlight-toolbar {
  margin-bottom: 1rem;
}

.content-area {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 1.5rem;
  align-items: start;
}

.material-content {
  min-height: 600px;
}

.material-content.highlight-mode {
  cursor: text;
}

.highlights-sidebar {
  position: sticky;
  top: 1rem;
  max-height: calc(100vh - 2rem);
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
}

.empty-highlights {
  text-align: center;
  padding: 2rem 1rem;
}

.empty-highlights p {
  margin-top: 1rem;
  color: #909399;
  font-size: 0.9rem;
}

.highlights-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 500px;
  overflow-y: auto;
}

/* Responsive design */
@media (max-width: 1200px) {
  .content-area {
    grid-template-columns: 1fr;
  }
  
  .highlights-sidebar {
    position: static;
    max-height: none;
  }
}

@media (max-width: 768px) {
  .material-viewer {
    padding: 0.5rem;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .header-actions .el-button {
    flex: 1;
  }
  
  .material-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>