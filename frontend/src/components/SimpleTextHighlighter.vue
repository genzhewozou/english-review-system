<template>
  <div class="simple-highlighter">
    <div class="controls">
      <button @click="toggleSelectionMode" :class="{ active: selectionMode }">
        {{ selectionMode ? 'Exit Selection Mode' : 'Enter Selection Mode' }}
      </button>
    </div>
    
    <div 
      ref="textContainer"
      class="text-container"
      :class="{ 'selection-mode': selectionMode }"
      @mouseup="handleMouseUp"
    >
      <p>
        This is a sample text for testing text selection functionality. 
        Select any part of this text when selection mode is active to create a card.
        You can select words, phrases, or entire sentences.
      </p>
      <p>
        Here's another paragraph with more text to test the selection feature.
        The system should detect text selection and show a popup with options.
      </p>
    </div>

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
          <button @click="createCard" class="btn-primary">
            Create Card
          </button>
          <button @click="closePopup" class="btn-secondary">
            Cancel
          </button>
        </div>
      </div>
    </div>

    <!-- Results -->
    <div v-if="cards.length > 0" class="cards-list">
      <h3>Created Cards:</h3>
      <div v-for="(card, index) in cards" :key="index" class="card-item">
        <strong>{{ card.text }}</strong>
        <span v-if="card.comment"> - {{ card.comment }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'

export default {
  name: 'SimpleTextHighlighter',
  setup() {
    const textContainer = ref(null)
    const selectionMode = ref(false)
    const showPopup = ref(false)
    const selectedText = ref('')
    const popupPosition = ref({ x: 0, y: 0 })
    const cards = ref([])

    const toggleSelectionMode = () => {
      selectionMode.value = !selectionMode.value
      if (!selectionMode.value) {
        closePopup()
      }
      console.log('Selection mode:', selectionMode.value)
    }

    const handleMouseUp = (event) => {
      console.log('Mouse up event', { selectionMode: selectionMode.value })
      
      if (!selectionMode.value) {
        return
      }

      setTimeout(() => {
        const selection = window.getSelection()
        const text = selection.toString().trim()
        
        console.log('Selection:', { text, rangeCount: selection.rangeCount })

        if (text.length === 0) {
          closePopup()
          return
        }

        if (!textContainer.value?.contains(selection.anchorNode)) {
          console.log('Selection not in container')
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
      }, 100)
    }

    const createCard = () => {
      if (selectedText.value) {
        cards.value.push({
          text: selectedText.value,
          comment: 'Test card'
        })
        console.log('Created card:', selectedText.value)
      }
      closePopup()
    }

    const closePopup = () => {
      showPopup.value = false
      selectedText.value = ''
      window.getSelection().removeAllRanges()
    }

    const popupStyle = computed(() => ({
      left: `${popupPosition.value.x}px`,
      top: `${popupPosition.value.y}px`
    }))

    return {
      textContainer,
      selectionMode,
      showPopup,
      selectedText,
      cards,
      popupStyle,
      toggleSelectionMode,
      handleMouseUp,
      createCard,
      closePopup
    }
  }
}
</script>

<style scoped>
.simple-highlighter {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.controls {
  margin-bottom: 2rem;
}

button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

button.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.text-container {
  padding: 2rem;
  border: 2px dashed #ddd;
  border-radius: 8px;
  line-height: 1.6;
  font-size: 1.1rem;
}

.text-container.selection-mode {
  border-color: #409eff;
  background: #f0f9ff;
  cursor: text;
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
  border-color: #409eff;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

.cards-list {
  margin-top: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.card-item {
  padding: 0.5rem;
  margin: 0.5rem 0;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}
</style>