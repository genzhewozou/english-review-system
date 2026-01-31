<template>
  <div class="text-highlighter" :class="{ 'highlight-mode': highlightMode }">
    <!-- Highlighting Toolbar -->
    <div v-if="highlightMode" class="highlight-toolbar">
      <el-alert
        title="Highlighting Mode Active"
        description="Select any text to create a highlight. Click on existing highlights to view or edit them."
        type="info"
        :closable="false"
        show-icon
      />
      <div class="toolbar-actions">
        <el-button 
          type="primary" 
          size="small" 
          @click="$emit('toggle-highlight-mode')"
          :icon="Edit"
        >
          Exit Highlighting
        </el-button>
        <el-button 
          type="default" 
          size="small" 
          @click="clearSelection"
          :icon="Close"
        >
          Clear Selection
        </el-button>
      </div>
    </div>

    <!-- Content Container -->
    <div 
      ref="contentContainer"
      class="content-container"
      @mouseup="handleTextSelection"
      @touchend="handleTextSelection"
      @click="handleClick"
    >
      <div 
        class="text-content"
        v-html="highlightedContent"
      ></div>
    </div>

    <!-- Selection Popup -->
    <div 
      v-if="showSelectionPopup && selectedText"
      ref="selectionPopup"
      class="selection-popup"
      :style="popupStyle"
    >
      <div class="popup-content">
        <div class="selected-text-preview">
          <strong>Selected:</strong> "{{ selectedText.text }}"
        </div>
        <div class="popup-actions">
          <el-button 
            type="primary" 
            size="small"
            @click="createHighlight"
            :icon="Plus"
          >
            Create Highlight
          </el-button>
          <el-button 
            type="default" 
            size="small"
            @click="closeSelectionPopup"
            :icon="Close"
          >
            Cancel
          </el-button>
        </div>
      </div>
    </div>

    <!-- Highlight Details Popup -->
    <div 
      v-if="showHighlightPopup && selectedHighlight"
      ref="highlightPopup"
      class="highlight-popup"
      :style="highlightPopupStyle"
    >
      <div class="popup-content">
        <div class="highlight-text">
          <strong>Highlight:</strong> "{{ selectedHighlight.text }}"
        </div>
        <div v-if="selectedHighlight.userComment" class="highlight-comment">
          <strong>Comment:</strong> {{ selectedHighlight.userComment }}
        </div>
        <div class="popup-actions">
          <el-button 
            type="primary" 
            size="small"
            @click="editHighlight"
            :icon="Edit"
          >
            Edit
          </el-button>
          <el-button 
            type="warning" 
            size="small"
            @click="deleteHighlight"
            :icon="Delete"
          >
            Delete
          </el-button>
          <el-button 
            type="default" 
            size="small"
            @click="closeHighlightPopup"
            :icon="Close"
          >
            Close
          </el-button>
        </div>
      </div>
    </div>

    <!-- Overlay for popup backdrop -->
    <div 
      v-if="showSelectionPopup || showHighlightPopup"
      class="popup-overlay"
      @click="closeAllPopups"
    ></div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Close, Plus, Delete } from '@element-plus/icons-vue'

export default {
  name: 'TextHighlighter',
  components: {
    Edit,
    Close,
    Plus,
    Delete
  },
  props: {
    content: {
      type: String,
      required: true
    },
    highlights: {
      type: Array,
      default: () => []
    },
    highlightMode: {
      type: Boolean,
      default: false
    },
    materialId: {
      type: [String, Number],
      required: true
    }
  },
  emits: [
    'text-selected', 
    'highlight-created', 
    'highlight-clicked', 
    'highlight-edited',
    'highlight-deleted',
    'toggle-highlight-mode'
  ],
  setup(props, { emit }) {
    // Refs
    const contentContainer = ref(null)
    const selectionPopup = ref(null)
    const highlightPopup = ref(null)
    
    // State
    const selectedText = ref(null)
    const selectedHighlight = ref(null)
    const showSelectionPopup = ref(false)
    const showHighlightPopup = ref(false)
    const popupPosition = ref({ x: 0, y: 0 })
    const highlightPopupPosition = ref({ x: 0, y: 0 })

    // Computed properties
    const highlightedContent = computed(() => {
      if (!props.content || !props.highlights.length) {
        return props.content
      }

      let content = props.content
      const sortedHighlights = [...props.highlights].sort((a, b) => b.startPosition - a.startPosition)

      // Apply highlights in reverse order to maintain positions
      sortedHighlights.forEach(highlight => {
        const beforeText = content.substring(0, highlight.startPosition)
        const highlightText = content.substring(highlight.startPosition, highlight.endPosition)
        const afterText = content.substring(highlight.endPosition)
        
        const highlightHtml = `<span class="highlight-span" data-highlight-id="${highlight.id}" title="${highlight.userComment || ''}">${highlightText}</span>`
        content = beforeText + highlightHtml + afterText
      })

      return content
    })

    const popupStyle = computed(() => ({
      left: `${popupPosition.value.x}px`,
      top: `${popupPosition.value.y}px`
    }))

    const highlightPopupStyle = computed(() => ({
      left: `${highlightPopupPosition.value.x}px`,
      top: `${highlightPopupPosition.value.y}px`
    }))

    // Methods
    const handleTextSelection = async (event) => {
      console.log('TextHighlighter: handleTextSelection called', { 
        highlightMode: props.highlightMode, 
        eventType: event.type,
        target: event.target 
      })
      
      if (!props.highlightMode) {
        console.log('Not in highlight mode, returning')
        return
      }

      // Check if clicked on existing highlight first
      const target = event.target
      if (target.classList.contains('highlight-span')) {
        console.log('Clicked on existing highlight')
        await handleHighlightClick(target, event)
        return
      }

      // Small delay to ensure selection is complete (like in SimpleTextHighlighter)
      setTimeout(() => {
        const selection = window.getSelection()
        const selectedTextContent = selection.toString().trim()

        console.log('Selection check:', { 
          selectedTextContent, 
          rangeCount: selection.rangeCount,
          anchorNode: selection.anchorNode 
        })

        if (selectedTextContent.length === 0) {
          console.log('No text selected, closing popups')
          closeAllPopups()
          return
        }

        // Validate selection is within our content
        if (!contentContainer.value?.contains(selection.anchorNode)) {
          console.log('Selection not within content container')
          return
        }

        try {
          const range = selection.getRangeAt(0)
          const rect = range.getBoundingClientRect()

          console.log('Selection range:', { rect, selectedText: selectedTextContent })

          // Use simple position calculation
          const relativeStartPos = 0 // Simplified for now
          const relativeEndPos = selectedTextContent.length

          // Get surrounding context using the full content
          const context = selectedTextContent // Simplified for now

          selectedText.value = {
            text: selectedTextContent,
            context: context,
            startPosition: relativeStartPos,
            endPosition: relativeEndPos,
            range: range.cloneRange()
          }

          // Position popup (similar to SimpleTextHighlighter)
          popupPosition.value = {
            x: Math.max(10, rect.left + (rect.width / 2) - 100),
            y: rect.bottom + window.scrollY + 10
          }

          console.log('Showing selection popup', { 
            selectedText: selectedText.value, 
            popupPosition: popupPosition.value 
          })

          showSelectionPopup.value = true
          closeHighlightPopup()
        } catch (error) {
          console.error('Error handling text selection:', error)
        }
      }, 100) // Small delay like in SimpleTextHighlighter
    }

    const handleHighlightClick = async (element, event) => {
      event.stopPropagation()
      
      const highlightId = parseInt(element.dataset.highlightId)
      const highlight = props.highlights.find(h => h.id === highlightId)
      
      if (!highlight) return

      selectedHighlight.value = highlight
      
      const rect = element.getBoundingClientRect()
      highlightPopupPosition.value = {
        x: rect.left + (rect.width / 2) - 100,
        y: rect.bottom + window.scrollY + 10
      }

      showHighlightPopup.value = true
      closeSelectionPopup()
    }

    const getTextPosition = (node, offset) => {
      if (!contentContainer.value) return 0
      
      const walker = document.createTreeWalker(
        contentContainer.value,
        NodeFilter.SHOW_TEXT,
        null,
        false
      )

      let position = 0
      let currentNode

      while (currentNode = walker.nextNode()) {
        if (currentNode === node) {
          return position + offset
        }
        position += currentNode.textContent.length
      }

      return position
    }

    // Simplified text position calculation
    const getTextPositionSimple = (node, offset) => {
      if (!contentContainer.value || !props.content) return 0
      
      try {
        // Get the text content of the container
        const containerText = contentContainer.value.textContent || ''
        
        // Create a range from the start of the container to the selection point
        const range = document.createRange()
        range.setStart(contentContainer.value, 0)
        range.setEnd(node, offset)
        
        // Get the text content of this range
        const textBeforeSelection = range.toString()
        
        return textBeforeSelection.length
      } catch (error) {
        console.error('Error calculating text position:', error)
        return 0
      }
    }

    const getSelectionContext = (range, contextLength = 100) => {
      const container = range.commonAncestorContainer
      const fullText = container.textContent || ''
      const startOffset = range.startOffset
      const endOffset = range.endOffset

      const contextStart = Math.max(0, startOffset - contextLength)
      const contextEnd = Math.min(fullText.length, endOffset + contextLength)

      return fullText.substring(contextStart, contextEnd)
    }

    // Simplified context calculation
    const getSelectionContextSimple = (selectedText, startPosition) => {
      if (!props.content) return selectedText
      
      const contextLength = 100
      const contextStart = Math.max(0, startPosition - contextLength)
      const contextEnd = Math.min(props.content.length, startPosition + selectedText.length + contextLength)
      
      return props.content.substring(contextStart, contextEnd)
    }

    const handleClick = (event) => {
      // Simple click handler for debugging if needed
    }

    const handleSelectionStart = (event) => {
      if (!props.highlightMode) {
        event.preventDefault()
        return false
      }
    }

    const createHighlight = () => {
      console.log('createHighlight called', { selectedText: selectedText.value })
      
      if (!selectedText.value) {
        console.log('No selected text, returning')
        return
      }

      console.log('Emitting text-selected event')
      emit('text-selected', {
        text: selectedText.value.text,
        context: selectedText.value.context,
        startPosition: selectedText.value.startPosition,
        endPosition: selectedText.value.endPosition
      })

      closeSelectionPopup()
      clearSelection()
    }

    const editHighlight = () => {
      if (!selectedHighlight.value) return
      
      emit('highlight-clicked', selectedHighlight.value)
      closeHighlightPopup()
    }

    const deleteHighlight = async () => {
      if (!selectedHighlight.value) return

      try {
        await ElMessageBox.confirm(
          'Are you sure you want to delete this highlight?',
          'Delete Highlight',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        emit('highlight-deleted', selectedHighlight.value)
        closeHighlightPopup()
        ElMessage.success('Highlight deleted successfully')
      } catch {
        // User cancelled
      }
    }

    const clearSelection = () => {
      const selection = window.getSelection()
      selection.removeAllRanges()
      selectedText.value = null
    }

    const closeSelectionPopup = () => {
      showSelectionPopup.value = false
      selectedText.value = null
    }

    const closeHighlightPopup = () => {
      showHighlightPopup.value = false
      selectedHighlight.value = null
    }

    const closeAllPopups = () => {
      closeSelectionPopup()
      closeHighlightPopup()
      clearSelection()
    }

    const handleClickOutside = (event) => {
      if (showSelectionPopup.value && selectionPopup.value && !selectionPopup.value.contains(event.target)) {
        closeSelectionPopup()
      }
      if (showHighlightPopup.value && highlightPopup.value && !highlightPopup.value.contains(event.target)) {
        closeHighlightPopup()
      }
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        closeAllPopups()
      }
    }

    // Lifecycle
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
      document.addEventListener('keydown', handleKeyDown)
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      document.removeEventListener('keydown', handleKeyDown)
    })

    // Watch for content changes
    watch(() => props.content, () => {
      closeAllPopups()
    })

    // Watch for highlight mode changes
    watch(() => props.highlightMode, (newMode) => {
      if (!newMode) {
        closeAllPopups()
      }
    })

    return {
      contentContainer,
      selectionPopup,
      highlightPopup,
      selectedText,
      selectedHighlight,
      showSelectionPopup,
      showHighlightPopup,
      highlightedContent,
      popupStyle,
      highlightPopupStyle,
      handleTextSelection,
      handleSelectionStart,
      handleClick,
      createHighlight,
      editHighlight,
      deleteHighlight,
      clearSelection,
      closeSelectionPopup,
      closeHighlightPopup,
      closeAllPopups
    }
  }
}
</script>

<style scoped>
.text-highlighter {
  position: relative;
  width: 100%;
}

.highlight-toolbar {
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: #f0f9ff;
  border-radius: 6px;
  border: 1px solid #409eff;
}

.toolbar-actions {
  margin-top: 1rem;
  display: flex;
  gap: 0.75rem;
}

.content-container {
  position: relative;
  min-height: 200px;
  padding: 1.5rem;
  background-color: white;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  line-height: 1.7;
  font-size: 1rem;
  color: #303133;
}

.highlight-mode .content-container {
  cursor: text;
  user-select: text;
  border-color: #409eff;
  background-color: #fefefe;
}

.text-content {
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Highlight styling */
:deep(.highlight-span) {
  background-color: #fff3cd;
  border-bottom: 2px solid #ffc107;
  padding: 2px 4px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

:deep(.highlight-span:hover) {
  background-color: #fff8e1;
  border-bottom-color: #ff9800;
  box-shadow: 0 2px 4px rgba(255, 193, 7, 0.3);
}

:deep(.highlight-span[title]:hover::after) {
  content: attr(title);
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  white-space: nowrap;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  z-index: 1000;
  pointer-events: none;
}

/* Popup styling */
.selection-popup,
.highlight-popup {
  position: fixed;
  z-index: 1000;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  max-width: 300px;
}

.popup-content {
  padding: 1rem;
}

.selected-text-preview,
.highlight-text {
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
  color: #303133;
  word-wrap: break-word;
}

.highlight-comment {
  margin-bottom: 0.75rem;
  font-size: 0.85rem;
  color: #606266;
  font-style: italic;
}

.popup-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.popup-actions .el-button {
  flex: 1;
  min-width: 0;
}

.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background: transparent;
}

/* Selection styling */
::selection {
  background-color: rgba(64, 158, 255, 0.3);
}

::-moz-selection {
  background-color: rgba(64, 158, 255, 0.3);
}

/* Responsive design */
@media (max-width: 768px) {
  .content-container {
    padding: 1rem;
    font-size: 0.95rem;
  }
  
  .toolbar-actions {
    flex-direction: column;
  }
  
  .selection-popup,
  .highlight-popup {
    min-width: 250px;
    max-width: 90vw;
  }
  
  .popup-actions {
    flex-direction: column;
  }
  
  .popup-actions .el-button {
    width: 100%;
  }
}

/* Print styles */
@media print {
  .highlight-toolbar,
  .selection-popup,
  .highlight-popup {
    display: none !important;
  }
  
  :deep(.highlight-span) {
    background-color: #f0f0f0 !important;
    border-bottom: 1px solid #666 !important;
  }
}
</style>