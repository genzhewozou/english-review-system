<template>
  <div class="video-player">
    <div class="viewer-toolbar" v-if="highlightMode">
      <el-alert
        title="Video Highlighting"
        description="Video player is active. Transcript highlighting will be available when transcript data is provided."
        type="info"
        :closable="false"
        show-icon
      />
    </div>

    <div class="video-content">
      <!-- Video.js Player -->
      <div class="video-player-container">
        <div v-if="!videoError" class="video-wrapper">
          <video
            ref="videoElement"
            class="video-js vjs-default-skin"
            controls
            preload="auto"
            :poster="posterUrl"
            data-setup="{}"
          >
            <source :src="videoUrl" :type="videoMimeType" />
            <p class="vjs-no-js">
              To view this video please enable JavaScript, and consider upgrading to a web browser that
              <a href="https://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>.
            </p>
          </video>
        </div>

        <!-- Fallback for unsupported formats -->
        <div v-else class="video-error">
          <el-alert
            title="Video Format Not Supported"
            :description="errorMessage"
            type="warning"
            show-icon
            :closable="false"
          />
          
          <div class="fallback-options">
            <h4>Alternative Options:</h4>
            <el-button type="primary" @click="downloadVideo" :icon="Download">
              Download Video File
            </el-button>
            <p class="fallback-note">
              Download the video to play it in your preferred video player application.
            </p>
          </div>
        </div>
      </div>

      <!-- Video Controls and Info -->
      <div class="video-controls">
        <div class="video-info-panel">
          <h4>{{ material.title }}</h4>
          <div class="video-meta">
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              {{ material.fileName }}
            </span>
            <span class="meta-item">
              <el-icon><DataAnalysis /></el-icon>
              {{ formatFileSize(material.fileSize) }}
            </span>
            <span v-if="videoDuration" class="meta-item">
              <el-icon><Clock /></el-icon>
              Duration: {{ formatDuration(videoDuration) }}
            </span>
          </div>
        </div>

        <div class="playback-controls">
          <el-button-group>
            <el-button @click="seekBackward" :icon="DArrowLeft" size="small">
              -10s
            </el-button>
            <el-button @click="togglePlayPause" :icon="playIcon" size="small">
              {{ isPlaying ? 'Pause' : 'Play' }}
            </el-button>
            <el-button @click="seekForward" :icon="DArrowRight" size="small">
              +10s
            </el-button>
          </el-button-group>
          
          <div class="volume-control">
            <el-icon><Mute /></el-icon>
            <el-slider
              v-model="volume"
              :min="0"
              :max="100"
              @change="updateVolume"
              style="width: 100px; margin-left: 8px;"
            />
          </div>
        </div>
      </div>

      <!-- Enhanced Transcript Panel -->
      <VideoTranscriptHighlighter
        v-if="transcript && transcript.length > 0"
        :transcript="transcript"
        :highlights="transcriptHighlights"
        :current-time="currentTime"
        :highlight-mode="highlightMode"
        :material-id="material.id"
        @seek-to-time="seekToTime"
        @text-selected="handleTranscriptTextSelected"
        @highlight-clicked="handleHighlightClick"
        @toggle-highlight-mode="$emit('toggle-highlight-mode')"
      />

      <!-- Existing highlights display -->
      <div v-if="highlights.length > 0" class="highlights-section">
        <h4>Video Highlights ({{ highlights.length }}):</h4>
        <div class="highlight-list">
          <div
            v-for="highlight in sortedHighlights"
            :key="highlight.id"
            class="highlight-preview"
            @click="handleHighlightClick(highlight)"
          >
            <div class="highlight-content">
              <span class="highlight-text">"{{ highlight.text }}"</span>
              <span v-if="highlight.timestamp" class="highlight-timestamp">
                @ {{ formatTimestamp(highlight.timestamp) }}
              </span>
              <span v-if="highlight.userComment" class="highlight-comment">
                {{ highlight.userComment }}
              </span>
            </div>
            <el-button
              type="text"
              size="small"
              @click.stop="seekToHighlight(highlight)"
              :icon="VideoPlay"
            >
              Play
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import {
  VideoPlay,
  Download,
  Document,
  DataAnalysis,
  Clock,
  DArrowLeft,
  DArrowRight,
  Mute
} from '@element-plus/icons-vue'
import { useApiService } from '../composables/useApiService'
import VideoTranscriptHighlighter from './VideoTranscriptHighlighter.vue'

export default {
  name: 'VideoPlayer',
  components: {
    VideoPlay,
    Download,
    Document,
    DataAnalysis,
    Clock,
    DArrowLeft,
    DArrowRight,
    Mute,
    VideoTranscriptHighlighter
  },
  props: {
    material: {
      type: Object,
      required: true
    },
    highlights: {
      type: Array,
      default: () => []
    },
    highlightMode: {
      type: Boolean,
      default: false
    }
  },
  emits: ['text-selected', 'highlight-clicked', 'toggle-highlight-mode'],
  setup(props, { emit }) {
    const { apiService } = useApiService()
    
    // Refs
    const videoElement = ref(null)
    const transcriptContainer = ref(null)
    
    // State
    const player = ref(null)
    const videoError = ref(false)
    const errorMessage = ref('')
    const isPlaying = ref(false)
    const volume = ref(50)
    const videoDuration = ref(0)
    const currentTime = ref(0)
    const currentSegment = ref(-1)
    const autoScroll = ref(true)
    
    // Mock transcript data (in real implementation, this would come from the backend)
    const transcript = ref([
      { startTime: 0, endTime: 5, text: "Welcome to this English learning video." },
      { startTime: 5, endTime: 12, text: "Today we'll be discussing advanced vocabulary and pronunciation techniques." },
      { startTime: 12, endTime: 18, text: "Let's start with some common phrases that are often mispronounced." },
      { startTime: 18, endTime: 25, text: "The first word we'll examine is 'pronunciation' itself." }
    ])

    // Computed properties
    const videoUrl = computed(() => {
      return `/api/materials/${props.material.id}/content`
    })

    const posterUrl = computed(() => {
      return `/api/materials/${props.material.id}/thumbnail`
    })

    const videoMimeType = computed(() => {
      const extension = props.material.fileName.split('.').pop().toLowerCase()
      const mimeTypes = {
        'mp4': 'video/mp4',
        'webm': 'video/webm',
        'ogg': 'video/ogg',
        'avi': 'video/x-msvideo',
        'mov': 'video/quicktime'
      }
      return mimeTypes[extension] || 'video/mp4'
    })

    const playIcon = computed(() => {
      return isPlaying.value ? 'VideoPause' : 'VideoPlay'
    })

    const sortedHighlights = computed(() => {
      return [...props.highlights].sort((a, b) => {
        return (a.timestamp || 0) - (b.timestamp || 0)
      })
    })

    // Separate highlights for transcript (those with timestamps)
    const transcriptHighlights = computed(() => {
      return props.highlights.filter(highlight => 
        highlight.timestamp !== undefined && highlight.timestamp !== null
      )
    })

    // Methods
    const initializePlayer = async () => {
      if (!videoElement.value) return

      try {
        // Check if video format is supported
        const video = document.createElement('video')
        const canPlay = video.canPlayType(videoMimeType.value)
        
        if (canPlay === '') {
          throw new Error(`Video format ${videoMimeType.value} is not supported by this browser`)
        }

        // Initialize Video.js player
        player.value = videojs(videoElement.value, {
          controls: true,
          responsive: true,
          fluid: true,
          playbackRates: [0.5, 1, 1.25, 1.5, 2],
          plugins: {
            // Add any Video.js plugins here
          }
        })

        // Set up event listeners
        player.value.ready(() => {
          console.log('Video player is ready')
          videoDuration.value = player.value.duration() || 0
        })

        player.value.on('play', () => {
          isPlaying.value = true
        })

        player.value.on('pause', () => {
          isPlaying.value = false
        })

        player.value.on('timeupdate', () => {
          currentTime.value = player.value.currentTime()
          updateCurrentSegment()
        })

        player.value.on('volumechange', () => {
          volume.value = Math.round(player.value.volume() * 100)
        })

        player.value.on('error', (error) => {
          console.error('Video player error:', error)
          handleVideoError('Failed to load video. The format may not be supported.')
        })

        // Set initial volume
        player.value.volume(volume.value / 100)

      } catch (error) {
        console.error('Error initializing video player:', error)
        handleVideoError(error.message)
      }
    }

    const handleVideoError = (message) => {
      videoError.value = true
      errorMessage.value = message
    }

    const updateCurrentSegment = () => {
      if (!transcript.value.length) return

      const current = currentTime.value
      const segmentIndex = transcript.value.findIndex(segment => 
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

    const togglePlayPause = () => {
      if (!player.value) return

      if (isPlaying.value) {
        player.value.pause()
      } else {
        player.value.play()
      }
    }

    const seekBackward = () => {
      if (!player.value) return
      const newTime = Math.max(0, player.value.currentTime() - 10)
      player.value.currentTime(newTime)
    }

    const seekForward = () => {
      if (!player.value) return
      const newTime = Math.min(videoDuration.value, player.value.currentTime() + 10)
      player.value.currentTime(newTime)
    }

    const seekToTime = (time) => {
      if (!player.value) return
      player.value.currentTime(time)
    }

    const updateVolume = (newVolume) => {
      if (!player.value) return
      player.value.volume(newVolume / 100)
    }

    const seekToHighlight = (highlight) => {
      if (highlight.timestamp !== undefined) {
        seekToTime(highlight.timestamp)
      }
    }

    const handleHighlightClick = (highlight) => {
      emit('highlight-clicked', highlight)
    }

    const handleTranscriptTextSelected = (selectionData) => {
      // Add timestamp information to the selection data for transcript highlights
      emit('text-selected', {
        ...selectionData,
        isTranscript: true
      })
    }

    const downloadVideo = async () => {
      try {
        await apiService.download(
          `/api/materials/${props.material.id}/download`,
          props.material.fileName
        )
      } catch (error) {
        console.error('Error downloading video:', error)
      }
    }

    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const formatDuration = (seconds) => {
      if (!seconds || seconds === 0) return '0:00'
      const hours = Math.floor(seconds / 3600)
      const mins = Math.floor((seconds % 3600) / 60)
      const secs = Math.floor(seconds % 60)
      
      if (hours > 0) {
        return `${hours}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
      }
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }

    const formatTimestamp = (seconds) => {
      return formatDuration(seconds)
    }

    // Lifecycle
    onMounted(() => {
      initializePlayer()
    })

    onUnmounted(() => {
      if (player.value) {
        player.value.dispose()
      }
    })

    // Watch for material changes
    watch(() => props.material.id, () => {
      if (player.value) {
        player.value.dispose()
        player.value = null
      }
      videoError.value = false
      errorMessage.value = ''
      nextTick(() => {
        initializePlayer()
      })
    })

    return {
      videoElement,
      transcriptContainer,
      videoError,
      errorMessage,
      isPlaying,
      volume,
      videoDuration,
      currentTime,
      currentSegment,
      autoScroll,
      transcript,
      videoUrl,
      posterUrl,
      videoMimeType,
      playIcon,
      sortedHighlights,
      transcriptHighlights,
      togglePlayPause,
      seekBackward,
      seekForward,
      seekToTime,
      updateVolume,
      seekToHighlight,
      handleHighlightClick,
      handleTranscriptTextSelected,
      downloadVideo,
      formatFileSize,
      formatDuration,
      formatTimestamp
    }
  }
}
</script>

<style scoped>
.video-player {
  width: 100%;
  height: 100%;
}

.viewer-toolbar {
  margin-bottom: 1rem;
}

.video-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.video-player-container {
  width: 100%;
}

.video-wrapper {
  position: relative;
  width: 100%;
  max-width: 100%;
}

.video-js {
  width: 100%;
  height: auto;
  max-height: 70vh;
}

.video-error {
  padding: 2rem;
  text-align: center;
}

.fallback-options {
  margin-top: 2rem;
}

.fallback-options h4 {
  margin-bottom: 1rem;
  color: #303133;
}

.fallback-note {
  margin-top: 0.5rem;
  color: #909399;
  font-size: 0.9rem;
}

.video-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.video-info-panel h4 {
  margin: 0 0 0.5rem 0;
  color: #303133;
}

.video-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #606266;
  font-size: 0.9rem;
}

.playback-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.volume-control {
  display: flex;
  align-items: center;
}

.highlights-section {
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.highlights-section h4 {
  margin: 0 0 1rem 0;
  color: #303133;
}

.highlight-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.highlight-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid #667eea;
}

.highlight-preview:hover {
  background-color: #f0f9ff;
  transform: translateX(4px);
}

.highlight-content {
  flex: 1;
  min-width: 0;
}

.highlight-text {
  font-weight: 600;
  color: #667eea;
  display: block;
  margin-bottom: 0.25rem;
}

.highlight-timestamp {
  color: #e6a23c;
  font-size: 0.85rem;
  font-weight: 500;
  display: block;
  margin-bottom: 0.25rem;
}

.highlight-comment {
  color: #606266;
  font-style: italic;
  display: block;
  font-size: 0.9rem;
}

/* Responsive design */
@media (max-width: 768px) {
  .video-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .playback-controls {
    justify-content: center;
  }
  
  .video-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .highlight-preview {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }
}

/* Video.js custom styling */
:deep(.video-js .vjs-big-play-button) {
  background-color: rgba(64, 158, 255, 0.8);
  border-color: #409eff;
}

:deep(.video-js .vjs-control-bar) {
  background-color: rgba(0, 0, 0, 0.7);
}

:deep(.video-js .vjs-progress-control .vjs-progress-holder) {
  height: 6px;
}

:deep(.video-js .vjs-progress-control .vjs-play-progress) {
  background-color: #409eff;
}
</style>