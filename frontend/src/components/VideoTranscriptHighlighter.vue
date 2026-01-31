<template>
  <div class="video-transcript-highlighter">
    <!-- Transcript Header -->
    <div class="transcript-header">
      <div class="header-left">
        <h4>Video Transcript</h4>
        <el-tag v-if="transcript.length > 0" size="small" type="info">
          {{ transcript.length }} segments
        </el-tag>
      </div>
      
      <div class="header-controls">
        <el-switch
          v-model="autoScroll"
          active-text="Auto-scroll"
          inactive-text="Manual"
          size="small"
        />
        <el-button
          v-if="highlightMode"
          type="primary"
          size="small"
          @click="$emit('toggle-highlight-mode')"
          :icon="Edit"
        >
          Exit Highlighting
        </el-button>
        <el-button
          v-else
          type="default"
          size="small"
          @click="$emit('toggle-highlight-mode')"
          :icon="Plus"
        >
          Start Highlighting
        </el-button>
      </div>
    </div>

    <!-- Transcript Content -->
    <div class="transcript-content" ref="transcriptContainer">
      <div v-if="transcript.length === 0" class="no-transcript">
        <el-empty
          description="No transcript available for this video"
          :image-size="80"
        >
          <el-text type="info">
            Transcript highlighting will be available when transcript data is provided.
          </el-text>
        </el-empty>
      </div>

      <div v-else class="transcript-segments">
        <div
          v-for="(segment, index) in transcript"
          :key="index"
          class="transcript-segment"
          :class="{
            'active': currentSegment === index,
            'highlight-mode': highlightMode,
            'has-highlights': getSegmentHighlights(segment).length > 0
          }"
          @click="seekToSegment(segment)"
          @mouseup="handleTranscriptSelection(segment, index, $event)"
        >
          <!-- Timestamp -->
          <div class="segment-timestamp">
            <el-button
              type="text"
              size="small"
              @click.stop="seekToSegment(segment)"
              :icon="VideoPlay"
            >
              {{ formatTimestamp(segment.startTime) }}
            </el-button>
          </div>

          <!-- Segment Text with Highlights -->
          <div class="segment-text" v-html="getHighlightedSegmentText(segment)"></div>

          <!-- Segment Highlights -->
          <div v-if="getSegmentHighlights(segment).length > 0" class="segment-highlights">
            <div
              v-for="highlight in getSegmentHighlights(segment)"
              :key="highlight.id"
              class="segment-highlight-item"
              @click.stop="$emit('highlight-clicked', highlight)"
            >
              <span class="highlight-text">"{{ highlight.text }}"</span>
              <span v-if="highlight.userComment" class="highlight-comment">
                - {{ truncateText(highlight.userComment, 50) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Selection Popup for Transcript -->
    <div 
      v-if="showTranscriptPopup && selectedTranscriptText"
      ref="transcriptPopup"
      class="transcript-selection-popup"
      :style="transcriptPopupStyle"
    >
      <div class="popup-content">
        <div class="selected-info">
          <div class="selected-text">
            <strong>Selected:</strong> "{{ selectedTranscriptText.text }}"
          </div>
          <div class="selected-timestamp">
            <el-icon><Clock /></el-icon>
            At {{ formatTimestamp(selectedTranscriptText.timestamp) }}
          </div>
        </div>
        <div class="popup-actions">
          <el-button 
            type="primary" 
            size="small"
            @click="createTranscriptHighlight"
            :icon="Plus"
          >
            Create Highlight
          </el-button>
          <el-button 
            type="default" 
            size="small"
            @click="closeTranscriptPopup"
            :icon="Close"
          >
            Cancel
          </el-button>
        </div>
      </div>
    </div>

    <!-- Overlay for popup backdrop -->
    <div 
      v-if="showTranscriptPopup"
      class="popup-overlay"
      @click="closeTranscriptPopup"
    ></div>
  </div>
</template>

<script>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  Edit,
  Plus,
  Close,
  Clock
} from '@element-plus/icons-vue'

export default {
  name: 'VideoTranscriptHighlighter',
  components: {
    VideoPlay,
    Edit,
    Plus,
    Close,
    Clock
  },
  props: {
    transcript: {
      type: Array,
      default: () => []
    },
    highlights: {
      type: Array,
      default: () => []
    },
    currentTime: {
      type: Number,
      default: 0
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
    'seek-to-time',
    'text-selected',
    'highlight-clicked',
    'toggle-highlight-mode'
  ],
  setup(props, { emit }) {
    // Refs
    const transcriptContainer = ref(null)
    const transcriptPopup = ref(null)
    
    // State
    const autoScroll = ref(true)
    const currentSegment = ref(-1)
    const selectedTranscriptText = ref(null)
    const showTranscriptPopup = ref(false)
    const transcriptPopupPosition = ref({ x: 0, y: 0 })

    // Computed properties
    const transcriptPopupStyle = computed(() => ({
      left: `${transcriptPopupPosition.value.x}px`,
      top: `${transcriptPopupPosition.value.y}px`
    }))

    // Methods
    const updateCurrentSegment = () => {
      if (!props.transcript.length) return

      const current = props.currentTime
      const segmentIndex = props.transcript.findIndex(segment => 
        current >= segment.startTime && current < segment.endTime
      )
      
      if (segmentIndex !== -1 && segmentIndex !== currentSegment.value) {
        currentSegment.value = segmentIndex
        
        if (autoScroll.value) {
          scrollToCurrentSegment()
        }
      }
    }

    const scrollToCurrentSegment = async () => {
      await nextTick()
      if (!transcriptContainer.value) return

      const activeSegment = transcriptContainer.value.querySelector('.transcript-segment.active')
      if (activeSegment) {
        activeSegment.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        })
      }
    }

    const seekToSegment = (segment) => {
      emit('seek-to-time', segment.startTime)
    }

    const getSegmentHighlights = (segment) => {
      return props.highlights.filter(highlight => 
        highlight.timestamp >= segment.startTime && 
        highlight.timestamp < segment.endTime
      )
    }

    const getHighlightedSegmentText = (segment) => {
      const segmentHighlights = getSegmentHighlights(segment)
      
      if (segmentHighlights.length === 0) {
        return segment.text
      }

      let text = segment.text
      const sortedHighlights = [...segmentHighlights].sort((a, b) => b.startPosition - a.startPosition)

      // Apply highlights in reverse order to maintain positions
      sortedHighlights.forEach(highlight => {
        const beforeText = text.substring(0, highlight.startPosition)
        const highlightText = text.substring(highlight.startPosition, highlight.endPosition)
        const afterText = text.substring(highlight.endPosition)
        
        const highlightHtml = `<span class="transcript-highlight" data-highlight-id="${highlight.id}" title="${highlight.userComment || ''}">${highlightText}</span>`
        text = beforeText + highlightHtml + afterText
      })

      return text
    }

    const handleTranscriptSelection = async (segment, segmentIndex, event) => {
      if (!props.highlightMode) return

      // Check if clicked on existing highlight
      const target = event.target
      if (target.classList.contains('transcript-highlight')) {
        const highlightId = parseInt(target.dataset.highlightId)
        const highlight = props.highlights.find(h => h.id === highlightId)
        if (highlight) {
          emit('highlight-clicked', highlight)
        }
        return
      }

      await nextTick()
      
      const selection = window.getSelection()
      const selectedText = selection.toString().trim()

      if (selectedText.length === 0) {
        closeTranscriptPopup()
        return
      }

      // Validate selection is within the segment
      const segmentElement = event.currentTarget.querySelector('.segment-text')
      if (!segmentElement?.contains(selection.anchorNode)) {
        return
      }

      const range = selection.getRangeAt(0)
      const rect = range.getBoundingClientRect()

      // Calculate relative positions within the segment text
      const relativeStartPos = getTextPositionInSegment(segmentElement, range.startContainer, range.startOffset)
      const relativeEndPos = getTextPositionInSegment(segmentElement, range.endContainer, range.endOffset)

      selectedTranscriptText.value = {
        text: selectedText,
        context: segment.text,
        startPosition: relativeStartPos,
        endPosition: relativeEndPos,
        timestamp: segment.startTime,
        segmentIndex: segmentIndex
      }

      // Position popup
      transcriptPopupPosition.value = {
        x: rect.left + (rect.width / 2) - 100,
        y: rect.bottom + window.scrollY + 10
      }

      showTranscriptPopup.value = true
    }

    const getTextPositionInSegment = (segmentElement, node, offset) => {
      const walker = document.createTreeWalker(
        segmentElement,
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

    const createTranscriptHighlight = () => {
      if (!selectedTranscriptText.value) return

      emit('text-selected', {
        text: selectedTranscriptText.value.text,
        context: selectedTranscriptText.value.context,
        startPosition: selectedTranscriptText.value.startPosition,
        endPosition: selectedTranscriptText.value.endPosition,
        timestamp: selectedTranscriptText.value.timestamp
      })

      closeTranscriptPopup()
      clearSelection()
    }

    const closeTranscriptPopup = () => {
      showTranscriptPopup.value = false
      selectedTranscriptText.value = null
    }

    const clearSelection = () => {
      const selection = window.getSelection()
      selection.removeAllRanges()
    }

    const formatTimestamp = (seconds) => {
      if (!seconds && seconds !== 0) return '0:00'
      const hours = Math.floor(seconds / 3600)
      const mins = Math.floor((seconds % 3600) / 60)
      const secs = Math.floor(seconds % 60)
      
      if (hours > 0) {
        return `${hours}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
      }
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }

    const truncateText = (text, maxLength) => {
      if (!text || text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    const handleClickOutside = (event) => {
      if (showTranscriptPopup.value && transcriptPopup.value && !transcriptPopup.value.contains(event.target)) {
        closeTranscriptPopup()
      }
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        closeTranscriptPopup()
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

    // Watch for current time changes
    watch(() => props.currentTime, updateCurrentSegment)

    return {
      transcriptContainer,
      transcriptPopup,
      autoScroll,
      currentSegment,
      selectedTranscriptText,
      showTranscriptPopup,
      transcriptPopupStyle,
      seekToSegment,
      getSegmentHighlights,
      getHighlightedSegmentText,
      handleTranscriptSelection,
      createTranscriptHighlight,
      closeTranscriptPopup,
      formatTimestamp,
      truncateText
    }
  }
}
</script>

<style scoped>
.video-transcript-highlighter {
  width: 100%;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.transcript-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.header-left h4 {
  margin: 0;
  color: #303133;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.transcript-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 0.5rem;
}

.no-transcript {
  padding: 2rem;
  text-align: center;
}

.transcript-segments {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.transcript-segment {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.transcript-segment:hover {
  background-color: #f0f9ff;
  border-color: #d1ecf1;
}

.transcript-segment.active {
  background-color: #e6f7ff;
  border-color: #409eff;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.1);
}

.transcript-segment.highlight-mode {
  user-select: text;
}

.transcript-segment.has-highlights {
  border-left: 3px solid #ffc107;
}

.segment-timestamp {
  flex-shrink: 0;
  min-width: 60px;
}

.segment-text {
  flex: 1;
  color: #303133;
  line-height: 1.6;
  word-wrap: break-word;
}

.segment-highlights {
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid #ebeef5;
}

.segment-highlight-item {
  display: block;
  padding: 0.25rem 0;
  font-size: 0.85rem;
  color: #606266;
  cursor: pointer;
  transition: color 0.2s ease;
}

.segment-highlight-item:hover {
  color: #409eff;
}

.highlight-text {
  font-weight: 600;
  color: #ffc107;
}

.highlight-comment {
  font-style: italic;
}

/* Transcript highlight styling */
:deep(.transcript-highlight) {
  background-color: #fff3cd;
  border-bottom: 2px solid #ffc107;
  padding: 1px 3px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.transcript-highlight:hover) {
  background-color: #fff8e1;
  border-bottom-color: #ff9800;
  box-shadow: 0 1px 3px rgba(255, 193, 7, 0.3);
}

/* Popup styling */
.transcript-selection-popup {
  position: fixed;
  z-index: 1000;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 250px;
  max-width: 350px;
}

.popup-content {
  padding: 1rem;
}

.selected-info {
  margin-bottom: 0.75rem;
}

.selected-text {
  font-size: 0.9rem;
  color: #303133;
  margin-bottom: 0.5rem;
  word-wrap: break-word;
}

.selected-timestamp {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: #606266;
}

.popup-actions {
  display: flex;
  gap: 0.5rem;
}

.popup-actions .el-button {
  flex: 1;
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

/* Responsive design */
@media (max-width: 768px) {
  .transcript-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .header-controls {
    justify-content: space-between;
  }
  
  .transcript-segment {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .segment-timestamp {
    min-width: auto;
  }
  
  .transcript-selection-popup {
    min-width: 280px;
    max-width: 90vw;
  }
  
  .popup-actions {
    flex-direction: column;
  }
}

/* Scrollbar styling */
.transcript-content::-webkit-scrollbar {
  width: 6px;
}

.transcript-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.transcript-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.transcript-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>