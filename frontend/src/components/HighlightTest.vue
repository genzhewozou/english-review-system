<template>
  <div class="highlight-test">
    <h2>Highlight Functionality Test</h2>
    
    <div class="controls">
      <el-button 
        @click="toggleHighlightMode" 
        :type="highlightMode ? 'success' : 'primary'"
        :icon="EditPen"
      >
        {{ highlightMode ? 'Exit Highlight Mode' : 'Enter Highlight Mode' }}
      </el-button>
      
      <el-button @click="clearHighlights" type="warning" :icon="Delete">
        Clear All Highlights
      </el-button>
    </div>

    <div class="test-content">
      <DocumentViewer
        :material="testMaterial"
        :highlights="testHighlights"
        :highlight-mode="highlightMode"
        @text-selected="handleTextSelection"
        @highlight-clicked="handleHighlightClick"
      />
    </div>

    <!-- Results -->
    <div v-if="testHighlights.length > 0" class="results">
      <h3>Created Highlights:</h3>
      <div v-for="highlight in testHighlights" :key="highlight.id" class="highlight-result">
        <strong>"{{ highlight.text }}"</strong>
        <span v-if="highlight.userComment"> - {{ highlight.userComment }}</span>
        <el-button 
          size="small" 
          type="danger" 
          @click="deleteHighlight(highlight)"
          :icon="Delete"
        >
          Delete
        </el-button>
      </div>
    </div>

    <!-- Highlight Creation Dialog -->
    <el-dialog
      v-model="showDialog"
      title="Create Highlight"
      width="400px"
    >
      <div class="dialog-content">
        <p><strong>Selected Text:</strong> "{{ selectedText }}"</p>
        <el-input
          v-model="comment"
          type="textarea"
          placeholder="Add a comment (optional)"
          :rows="3"
        />
      </div>
      <template #footer>
        <el-button @click="cancelHighlight">Cancel</el-button>
        <el-button type="primary" @click="saveHighlight">Save Highlight</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Delete } from '@element-plus/icons-vue'
import DocumentViewer from './DocumentViewer.vue'

export default {
  name: 'HighlightTest',
  components: {
    DocumentViewer
  },
  setup() {
    const highlightMode = ref(false)
    const testHighlights = ref([])
    const showDialog = ref(false)
    const selectedText = ref('')
    const comment = ref('')
    let highlightIdCounter = 1

    const testMaterial = {
      id: 1,
      title: 'Test Document',
      fileName: 'test.txt',
      type: 'DOCUMENT',
      mimeType: 'text/plain',
      fileSize: 1024,
      createdDate: new Date().toISOString()
    }

    const toggleHighlightMode = () => {
      highlightMode.value = !highlightMode.value
      ElMessage.info(
        highlightMode.value 
          ? 'Highlight mode activated. Select text to create highlights.' 
          : 'Highlight mode deactivated.'
      )
    }

    const handleTextSelection = (selection) => {
      console.log('Text selected:', selection)
      selectedText.value = selection.text
      showDialog.value = true
    }

    const handleHighlightClick = (highlight) => {
      ElMessage.info(`Clicked highlight: "${highlight.text}"`)
    }

    const saveHighlight = () => {
      if (selectedText.value) {
        const newHighlight = {
          id: highlightIdCounter++,
          materialId: 1,
          text: selectedText.value,
          context: selectedText.value,
          startPosition: 0,
          endPosition: selectedText.value.length,
          userComment: comment.value || null,
          createdDate: new Date().toISOString()
        }
        
        testHighlights.value.push(newHighlight)
        ElMessage.success('Highlight created successfully!')
        cancelHighlight()
      }
    }

    const cancelHighlight = () => {
      showDialog.value = false
      selectedText.value = ''
      comment.value = ''
    }

    const deleteHighlight = (highlight) => {
      const index = testHighlights.value.findIndex(h => h.id === highlight.id)
      if (index !== -1) {
        testHighlights.value.splice(index, 1)
        ElMessage.success('Highlight deleted!')
      }
    }

    const clearHighlights = () => {
      testHighlights.value = []
      ElMessage.info('All highlights cleared!')
    }

    return {
      highlightMode,
      testMaterial,
      testHighlights,
      showDialog,
      selectedText,
      comment,
      toggleHighlightMode,
      handleTextSelection,
      handleHighlightClick,
      saveHighlight,
      cancelHighlight,
      deleteHighlight,
      clearHighlights,
      EditPen,
      Delete
    }
  }
}
</script>

<style scoped>
.highlight-test {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.controls {
  margin-bottom: 2rem;
  display: flex;
  gap: 1rem;
}

.test-content {
  margin-bottom: 2rem;
}

.results {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 6px;
}

.highlight-result {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem;
  margin: 0.5rem 0;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.dialog-content p {
  margin-bottom: 1rem;
  color: #409eff;
  font-weight: bold;
}
</style>