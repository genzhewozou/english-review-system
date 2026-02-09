<template>
  <div class="document-viewer">
    <div class="viewer-toolbar" v-if="selectionMode">
          <el-alert
            title="Document Text Selection"
            description="Select text in the document to create cards. Click on existing cards to view or edit them."
            type="info"
            :closable="false"
            show-icon
          />
        </div>

    <div class="document-content" :class="{ 'selection-mode': selectionMode }">
      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
        <p>Loading document content...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-state">
        <el-alert
          :title="error.title"
          :description="error.message"
          type="error"
          show-icon
        />
        <div class="error-actions">
          <el-button type="primary" @click="downloadDocument" :icon="Download">
            Download Document
          </el-button>
        </div>
      </div>

      <!-- Document Content -->
      <div v-else class="document-text-content">
        <div class="document-info">
          <h4>{{ material.title }}</h4>
          <p><strong>File:</strong> {{ material.fileName }}</p>
          <p><strong>Type:</strong> {{ material.mimeType || 'text/plain' }}</p>
          <p><strong>Size:</strong> {{ formatFileSize(material.fileSize) }}</p>
        </div>

        <!-- Text Highlighter -->
        <div class="text-highlighter" :class="{ 'selection-mode': selectionMode }">
          <div 
            ref="textContainer"
            class="text-content"
            @mouseup="handleTextSelection"
            v-text="displayContent"
          ></div>
          
          <!-- Selection Popup -->
          <div 
            v-if="showPopup && selectedText"
            class="selection-popup"
            :style="popupStyle"
          >
            <div class="popup-content">
              <div class="selected-text">
                Selected: "{{ selectedText }}"
              </div>
              <div class="popup-actions">
                <button @click="speakText(selectedText)" class="btn-primary">
                  Speak
                </button>
                <button @click="createCard" class="btn-primary">
                  Create Card
                </button>
                <button @click="closePopup" class="btn-secondary">
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Pagination Controls -->
        <div v-if="totalPages > 1" class="pagination-controls">
          <div class="pagination-info">
            Page {{ currentPage }} of {{ totalPages }}
          </div>
          <div class="pagination-buttons">
            <button 
              @click="prevPage"
              :disabled="currentPage === 1"
              class="pagination-btn"
            >
              Previous
            </button>
            <div class="page-numbers">
              <button 
                v-for="page in Math.min(5, totalPages)"
                :key="page"
                @click="goToPage(page)"
                :class="['page-btn', { active: currentPage === page }]"
              >
                {{ page }}
              </button>
              <button 
                v-if="totalPages > 5"
                @click="goToPage(totalPages)"
                class="page-btn"
              >
                {{ totalPages }}
              </button>
            </div>
            <button 
              @click="nextPage"
              :disabled="currentPage === totalPages"
              class="pagination-btn"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      <!-- Existing cards overlay -->
      <div v-if="cards.length > 0" class="cards-overlay">
        <h4>Existing Cards:</h4>
        <div class="card-list">
          <div
            v-for="card in cards"
            :key="card.id"
            class="card-preview"
            @click="$emit('card-clicked', card)"
          >
            <span class="card-text">"{{ card.frontText || card.text }}"</span>
            <span v-if="card.userComment" class="card-comment">
              - {{ card.userComment }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { useApiService } from '../composables/useApiService'

export default {
  name: 'DocumentViewer',
  components: {
    Download
  },
  props: {
    material: {
      type: Object,
      required: true
    },
    cards: {
      type: Array,
      default: () => []
    },
    selectionMode: {
      type: Boolean,
      default: false
    }
  },
  emits: ['text-selected', 'card-clicked', 'card-deleted', 'toggle-selection-mode'],
  setup(props, { emit }) {
    const { apiService } = useApiService()

    // State
    const documentContent = ref('')
    const loading = ref(false)
    const error = ref(null)
    
    // Pagination state
    const currentPage = ref(1)
    const itemsPerPage = ref(5000) // ~5000 characters per page
    const totalPages = ref(1)
    const pageContent = ref([])
    
    // Highlighter state
    const textContainer = ref(null)
    const showPopup = ref(false)
    const selectedText = ref('')
    const popupPosition = ref({ x: 0, y: 0 })

    // Computed
    const displayContent = computed(() => {
      if (pageContent.value.length > 0 && pageContent.value[currentPage.value - 1]) {
        return pageContent.value[currentPage.value - 1]
      }
      // Fallback content for testing
      return `This is sample document content for highlighting.

You can select any text when highlight mode is active to create highlights.

Here are some URLs and text from the material:
https://developers.google.com/machine-learning/crash-course/linear-regression?hl=zh-cn
https://dictionary.cambridge.org/dictionary/english-chinese-simplified/caption
https://dictionary.cambridge.org
https://www.ieltsonlinetests.com/zh-hans/reading-tips?page=10

This is additional sample text content for testing the highlighting functionality.
Select words, phrases, or entire sentences to test the highlighting feature.

The system should detect text selection and show a popup with options to create highlights.`
    })

    const popupStyle = computed(() => ({
      position: 'fixed',
      left: `${popupPosition.value.x}px`,
      top: `${popupPosition.value.y}px`,
      zIndex: 1000,
      background: 'white',
      border: '1px solid #ddd',
      borderRadius: '6px',
      boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
      minWidth: '200px'
    }))

    // Methods
    const handleTextSelection = (event) => {
      console.log('Text selection triggered', { selectionMode: props.selectionMode })
      
      if (!props.selectionMode) {
        return
      }

      const selection = window.getSelection()
      const text = selection.toString().trim()
      
      console.log('Selected text:', text)

      if (text.length === 0) {
        closePopup()
        return
      }

      try {
        const range = selection.getRangeAt(0)
        const rect = range.getBoundingClientRect()
        
        selectedText.value = text
        popupPosition.value = {
          x: rect.left + (rect.width / 2) - 100,
          y: rect.bottom + 10
        }
        
        showPopup.value = true
        console.log('Showing popup for:', text)
      } catch (error) {
        console.error('Error handling selection:', error)
      }
    }

    const speakText = (text) => {
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(text)
        utterance.lang = 'en-US' // Use English (US) for pronunciation
        speechSynthesis.speak(utterance)
        console.log('Speaking text:', text)
      } else {
        console.warn('Text-to-speech is not supported in this browser')
      }
    }

    const createCard = () => {
      console.log('Creating card:', selectedText.value)
      
      if (selectedText.value) {
        // Emit the text-selected event to parent
        const selectionData = {
          text: selectedText.value,
          context: selectedText.value,
          startPosition: 0,
          endPosition: selectedText.value.length
        }
        console.log('Emitting text-selected event:', selectionData)
        emit('text-selected', selectionData)
      }
      closePopup()
    }

    const closePopup = () => {
      showPopup.value = false
      selectedText.value = ''
      window.getSelection().removeAllRanges()
    }

    const downloadDocument = async () => {
      try {
        await apiService.download(
          `/api/materials/${props.material.id}/download`,
          props.material.fileName
        )
      } catch (error) {
        console.error('Error downloading document:', error)
      }
    }

    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    // Pagination methods
    const splitContentIntoPages = (content) => {
      const pages = []
      const pageSize = itemsPerPage.value
      
      for (let i = 0; i < content.length; i += pageSize) {
        // Try to split at a natural break point (like a paragraph or sentence)
        let end = Math.min(i + pageSize, content.length)
        
        // If we're not at the end of the content, look for a natural break
        if (end < content.length) {
          // Try to find the next newline
          const nextNewline = content.indexOf('\n', end)
          if (nextNewline !== -1 && nextNewline - i < pageSize * 1.2) {
            end = nextNewline
          } else {
            // Try to find the next space
            const nextSpace = content.indexOf(' ', end)
            if (nextSpace !== -1 && nextSpace - i < pageSize * 1.2) {
              end = nextSpace
            }
          }
        }
        
        pages.push(content.substring(i, end))
      }
      
      return pages
    }

    const updatePagination = () => {
      if (documentContent.value) {
        pageContent.value = splitContentIntoPages(documentContent.value)
        totalPages.value = pageContent.value.length
        currentPage.value = 1 // Reset to first page when content changes
      } else {
        pageContent.value = []
        totalPages.value = 1
        currentPage.value = 1
      }
    }

    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
      }
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
      }
    }

    const loadDocumentContent = async () => {
      console.log('Loading document content for material:', props.material?.id)
      
      if (!props.material || props.material.type !== 'DOCUMENT') {
        console.log('Not a document or no material')
        return
      }

      loading.value = true
      error.value = null

      try {
        const url = `/materials/${props.material.id}/text`
        console.log('Fetching text content from:', url)
        
        const response = await apiService.get(url)
        
        // Handle different response formats
        let content = ''
        if (typeof response.data === 'string') {
          content = response.data
        } else if (response.data && typeof response.data === 'object') {
          content = JSON.stringify(response.data, null, 2)
        } else {
          content = String(response.data || '')
        }
        
        documentContent.value = content
        updatePagination() // Update pagination when content is loaded
        console.log('Document content loaded successfully:', content.length, 'characters')
        console.log('Split into', totalPages.value, 'pages')
      } catch (err) {
        console.error('Error loading document content:', err.message)
        
        if (err.response?.status === 400) {
          error.value = {
            title: 'Document Format Not Supported',
            message: 'This document format cannot be displayed for text highlighting. You can download the file to view it in your preferred application.'
          }
        } else if (err.response?.status === 404) {
          error.value = {
            title: 'Document Not Found',
            message: `The document content could not be found. Please try downloading the file instead.`
          }
        } else {
          error.value = {
            title: 'Error Loading Document',
            message: `Failed to load document content: ${err.message}. Please try downloading the file instead.`
          }
        }
      } finally {
        loading.value = false
      }
    }

    // Load content when component mounts or material changes
    onMounted(() => {
      loadDocumentContent()
    })

    watch(() => props.material, () => {
      loadDocumentContent()
    }, { deep: true })

    return {
      documentContent,
      loading,
      error,
      textContainer,
      showPopup,
      selectedText,
      displayContent,
      popupStyle,
      // Pagination
      currentPage,
      totalPages,
      goToPage,
      nextPage,
      prevPage,
      // Methods
      handleTextSelection,
      createCard,
      speakText,
      closePopup,
      downloadDocument,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.document-viewer {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.viewer-toolbar {
  margin-bottom: 1rem;
}

.document-content {
  padding: 2rem;
  min-height: 400px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 2px dashed #d9d9d9;
}

.document-content.selection-mode {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.loading-state {
  text-align: center;
  color: #606266;
}

.loading-state p {
  margin-top: 1rem;
  color: #909399;
}

.error-state {
  text-align: center;
  color: #606266;
}

.error-actions {
  margin-top: 1rem;
}

.document-text-content {
  background-color: white;
  border-radius: 6px;
  padding: 0;
}

.document-info {
  margin: 0 0 2rem 0;
  padding: 1.5rem;
  background-color: white;
  border-radius: 6px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.document-info h4 {
  margin: 0 0 1rem 0;
  color: #303133;
}

.document-info p {
  margin: 0.5rem 0;
  color: #606266;
}

.text-highlighter {
  width: 100%;
  padding: 1.5rem;
  background-color: white;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  line-height: 1.7;
  font-size: 1rem;
  color: #303133;
  position: relative;
}

.text-highlighter.selection-mode {
  cursor: text;
  user-select: text;
  border-color: #409eff;
  background-color: #fefefe;
}

.text-content {
  white-space: pre-wrap;
  word-wrap: break-word;
  user-select: text;
}

.selection-popup {
  position: fixed;
  z-index: 1000;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  min-width: 200px;
}

.popup-content {
  padding: 1rem;
}

.selected-text {
  margin-bottom: 1rem;
  font-weight: bold;
  color: #409eff;
}

.popup-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-primary {
  background: #409eff;
  color: white;
  border: 1px solid #409eff;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.highlights-overlay {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: 6px;
  text-align: left;
}

.highlights-overlay h4 {
  margin: 0 0 1rem 0;
  color: #303133;
}

.highlight-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.highlight-preview {
  padding: 0.75rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid #409eff;
}

.highlight-preview:hover {
  background-color: #e6f7ff;
  transform: translateX(4px);
}

.highlight-text {
  font-weight: 600;
  color: #409eff;
}

.highlight-comment {
  color: #606266;
  font-style: italic;
}

.pagination-controls {
  margin-top: 2rem;
  padding: 1rem;
  background-color: white;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-info {
  text-align: center;
  margin-bottom: 1rem;
  color: #606266;
  font-size: 14px;
}

.pagination-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-btn {
  padding: 0.375rem 0.75rem;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  min-width: 32px;
  text-align: center;
}

.page-btn:hover:not(.active) {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
}

.page-btn.active {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

/* Responsive design */
@media (max-width: 768px) {
  .document-content {
    padding: 1rem;
  }
  
  .document-info {
    max-width: none;
  }
  
  .pagination-buttons {
    flex-wrap: wrap;
  }
  
  .page-numbers {
    order: 1;
    width: 100%;
    justify-content: center;
    margin: 0.5rem 0;
  }
  
  .pagination-btn {
    order: 2;
  }
}
</style>