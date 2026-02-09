<template>
  <div class="text-highlighter" :class="{ 'selection-mode': selectionMode }">
    <!-- Selection Toolbar -->
    <div v-if="selectionMode" class="selection-toolbar">
      <el-alert
        title="Selection Mode Active"
        description="Select any text to create a card. Click on existing cards to view or edit them."
        type="info"
        :closable="false"
        show-icon
      />
      <div class="toolbar-actions">
        <el-button 
          type="primary" 
          size="small" 
          @click="$emit('toggle-selection-mode')"
          :icon="Edit"
        >
          Exit Selection
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
        v-html="cardContent"
      ></div>
    </div>

    <!-- Selection Popup -->
    <div 
      v-if="showSelectionPopup && selectedText"
      ref="selectionPopup"
      class="selection-popup"
      :style="popupStyle"
      role="dialog"
      aria-modal="true"
      aria-labelledby="selection-popup-title"
      aria-describedby="selection-popup-description"
      tabindex="0"
      @keydown.esc="closeAllPopups"
      @keydown.enter="createCard"
      @keydown.tab="handlePopupTabKey"
    >
      <div class="popup-content">
        <div class="selected-text-preview" id="selection-popup-description">
          <strong id="selection-popup-title">Selected:</strong> "{{ selectedText.text }}"
        </div>
        <div class="popup-actions">
          <el-button 
            type="primary" 
            size="small"
            @click="createCard"
            :icon="Plus"
            tabindex="0"
            aria-label="Create card"
          >
            Create Card
          </el-button>
          <el-button 
            type="default" 
            size="small"
            @click="closeSelectionPopup"
            :icon="Close"
            tabindex="0"
            aria-label="Cancel"
          >
            Cancel
          </el-button>
        </div>
      </div>
    </div>

    <!-- Card Details Popup -->
    <div 
      v-if="showCardPopup && selectedCard"
      ref="cardPopup"
      class="card-popup"
      :style="cardPopupStyle"
      role="dialog"
      aria-modal="true"
      aria-labelledby="card-popup-title"
      aria-describedby="card-popup-description"
      tabindex="0"
      @keydown.esc="closeAllPopups"
      @keydown.tab="handlePopupTabKey"
    >
      <div class="popup-content">
        <div class="card-text" id="card-popup-description">
          <strong id="card-popup-title">Card:</strong> "{{ selectedCard.text }}"
        </div>
        <div v-if="selectedCard.userComment" class="card-comment">
          <strong>Comment:</strong> {{ selectedCard.userComment }}
        </div>
        <div class="popup-actions">
          <el-button 
            type="primary" 
            size="small"
            @click="editCard"
            :icon="Edit"
            tabindex="0"
            aria-label="Edit card"
          >
            Edit
          </el-button>
          <el-button 
            type="warning" 
            size="small"
            @click="deleteCard"
            :icon="Delete"
            tabindex="0"
            aria-label="Delete card"
          >
            Delete
          </el-button>
          <el-button 
            type="default" 
            size="small"
            @click="closeCardPopup"
            :icon="Close"
            tabindex="0"
            aria-label="Close popup"
          >
            Close
          </el-button>
        </div>
      </div>
    </div>

    <!-- Overlay for popup backdrop -->
    <div 
      v-if="showSelectionPopup || showCardPopup"
      class="popup-overlay"
      @click="closeAllPopups"
      aria-hidden="true"
      role="presentation"
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
    cards: {
      type: Array,
      default: () => []
    },
    selectionMode: {
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
    'card-created', 
    'card-clicked', 
    'card-edited',
    'card-deleted',
    'toggle-selection-mode'
  ],
  setup(props, { emit }) {
    // Refs
    const contentContainer = ref(null)
    const selectionPopup = ref(null)
    const cardPopup = ref(null)
    
    // State
    const selectedText = ref(null)
    const selectedCard = ref(null)
    const showSelectionPopup = ref(false)
    const showCardPopup = ref(false)
    const popupPosition = ref({ x: 0, y: 0 })
    const cardPopupPosition = ref({ x: 0, y: 0 })

    // Computed properties
    const cardContent = computed(() => {
      if (!props.content || !props.cards.length) {
        return props.content
      }

      let content = props.content
      const sortedCards = [...props.cards].sort((a, b) => b.startPosition - a.startPosition)

      // Apply card markers in reverse order to maintain positions
      sortedCards.forEach(card => {
        const beforeText = content.substring(0, card.startPosition)
        const cardText = content.substring(card.startPosition, card.endPosition)
        const afterText = content.substring(card.endPosition)
        
        const cardHtml = `<span class="card-span" data-card-id="${card.id}" title="${card.userComment || ''}">${cardText}</span>`
        content = beforeText + cardHtml + afterText
      })

      return content
    })

    const popupStyle = computed(() => ({
      left: `${popupPosition.value.x}px`,
      top: `${popupPosition.value.y}px`
    }))

    const cardPopupStyle = computed(() => ({
      left: `${cardPopupPosition.value.x}px`,
      top: `${cardPopupPosition.value.y}px`
    }))

    // Methods
    const handleTextSelection = async (event) => {
      console.log('TextHighlighter: handleTextSelection called', { 
        selectionMode: props.selectionMode, 
        eventType: event.type,
        target: event.target 
      })
      
      if (!props.selectionMode) {
        console.log('Not in selection mode, returning')
        return
      }

      // Check if clicked on existing card first
      const target = event.target
      if (target.classList.contains('card-span')) {
        console.log('Clicked on existing card')
        await handleCardClick(target, event)
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
          closeCardPopup()
        } catch (error) {
          console.error('Error handling text selection:', error)
        }
      }, 100) // Small delay like in SimpleTextHighlighter
    }

    const handleCardClick = async (element, event) => {
      event.stopPropagation()
      
      const cardId = parseInt(element.dataset.cardId)
      const card = props.cards.find(c => c.id === cardId)
      
      if (!card) return

      selectedCard.value = card
      
      const rect = element.getBoundingClientRect()
      cardPopupPosition.value = {
        x: rect.left + (rect.width / 2) - 100,
        y: rect.bottom + window.scrollY + 10
      }

      showCardPopup.value = true
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
      if (!props.selectionMode) {
        event.preventDefault()
        return false
      }
    }

    const createCard = () => {
      console.log('createCard called', { selectedText: selectedText.value })
      
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

    const editCard = () => {
      if (!selectedCard.value) return
      
      emit('card-clicked', selectedCard.value)
      closeCardPopup()
    }

    const deleteCard = async () => {
      if (!selectedCard.value) return

      try {
        await ElMessageBox.confirm(
          'Are you sure you want to delete this card?',
          'Delete Card',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        emit('card-deleted', selectedCard.value)
        closeCardPopup()
        ElMessage.success('Card deleted successfully')
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

    const closeCardPopup = () => {
      showCardPopup.value = false
      selectedCard.value = null
    }

    const closeAllPopups = () => {
      closeSelectionPopup()
      closeCardPopup()
      clearSelection()
    }

    const handlePopupTabKey = (event) => {
      // Get all focusable elements in the current popup
      const popup = showSelectionPopup.value ? selectionPopup.value : cardPopup.value
      if (!popup) return

      const focusableElements = popup.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])')
      const focusable = Array.from(focusableElements).filter(el => {
        return !el.disabled && el.offsetWidth > 0 && el.offsetHeight > 0
      })

      if (focusable.length === 0) return

      const firstFocusable = focusable[0]
      const lastFocusable = focusable[focusable.length - 1]

      // Trap tab key within popup
      if (event.key === 'Tab') {
        if (event.shiftKey) {
          // Shift + Tab
          if (document.activeElement === firstFocusable) {
            event.preventDefault()
            lastFocusable.focus()
          }
        } else {
          // Tab
          if (document.activeElement === lastFocusable) {
            event.preventDefault()
            firstFocusable.focus()
          }
        }
      }
    }

    const handleClickOutside = (event) => {
      if (showSelectionPopup.value && selectionPopup.value && !selectionPopup.value.contains(event.target)) {
        closeSelectionPopup()
      }
      if (showCardPopup.value && cardPopup.value && !cardPopup.value.contains(event.target)) {
        closeCardPopup()
      }
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        closeAllPopups()
      }
    }

    // Lifecycle
    onMounted(() => {
      // Use passive event listeners where appropriate
      document.addEventListener('click', handleClickOutside, { passive: true })
      document.addEventListener('keydown', handleKeyDown, { passive: true })
    })

    onUnmounted(() => {
      // Clean up event listeners
      document.removeEventListener('click', handleClickOutside)
      document.removeEventListener('keydown', handleKeyDown)
    })

    // Watch for content changes
    watch(() => props.content, () => {
      closeAllPopups()
    })

    // Watch for selection mode changes
    watch(() => props.selectionMode, (newMode) => {
      if (!newMode) {
        closeAllPopups()
      }
    })

    return {
      contentContainer,
      selectionPopup,
      cardPopup,
      selectedText,
      selectedCard,
      showSelectionPopup,
      showCardPopup,
      cardContent,
      popupStyle,
      cardPopupStyle,
      handleTextSelection,
      handleSelectionStart,
      handleClick,
      createCard,
      editCard,
      deleteCard,
      clearSelection,
      closeSelectionPopup,
      closeCardPopup,
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

.card-toolbar {
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

.selection-mode .content-container {
  cursor: text;
  user-select: text;
  border-color: #409eff;
  background-color: #fefefe;
}

.text-content {
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Card styling */
:deep(.card-span) {
  background-color: #fff3cd;
  border-bottom: 2px solid #ffc107;
  padding: 2px 4px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

:deep(.card-span:hover) {
  background-color: #fff8e1;
  border-bottom-color: #ff9800;
  box-shadow: 0 2px 4px rgba(255, 193, 7, 0.3);
}

:deep(.card-span[title]:hover::after) {
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
.card-popup {
  position: fixed;
  z-index: var(--z-popover);
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  box-shadow: var(--shadow-md);
  min-width: 200px;
  max-width: 300px;
}

.popup-content {
  padding: 1rem;
}

.selected-text-preview,
.card-text {
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
  color: #303133;
  word-wrap: break-word;
}

.card-comment {
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
  z-index: var(--z-modal-backdrop);
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
  .card-popup {
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
  .card-toolbar,
  .selection-popup,
  .card-popup {
    display: none !important;
  }
  
  :deep(.card-span) {
    background-color: #f0f0f0 !important;
    border-bottom: 1px solid #666 !important;
  }
}
</style>