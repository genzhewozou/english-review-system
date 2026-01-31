<template>
  <div class="article-viewer">
    <div class="viewer-toolbar" v-if="highlightMode">
      <el-alert
        title="Article Highlighting"
        description="Select text in the article to create highlights. This is a placeholder - full article rendering will be implemented in a future task."
        type="info"
        :closable="false"
        show-icon
      />
    </div>

    <div class="article-content" :class="{ 'highlight-mode': highlightMode }">
      <!-- Placeholder content for article viewer -->
      <div class="placeholder-content">
        <el-icon class="placeholder-icon" :size="64">
          <Reading />
        </el-icon>
        
        <h3>Article Viewer</h3>
        <p>This is a placeholder for the article viewer component.</p>
        
        <div class="article-info">
          <h4>{{ material.title }}</h4>
          <p><strong>File:</strong> {{ material.fileName }}</p>
          <p><strong>Type:</strong> {{ material.mimeType || 'Article' }}</p>
          <p><strong>Size:</strong> {{ formatFileSize(material.fileSize) }}</p>
        </div>

        <!-- Text Highlighter Component -->
        <div v-if="highlightMode" class="text-highlighter-section">
          <h4>Sample Article Content (for highlighting demonstration):</h4>
          <TextHighlighter
            :content="sampleArticleContent"
            :highlights="highlights"
            :highlight-mode="highlightMode"
            :material-id="material.id"
            @text-selected="handleTextSelected"
            @highlight-clicked="handleHighlightClicked"
            @highlight-deleted="handleHighlightDeleted"
            @toggle-highlight-mode="$emit('toggle-highlight-mode')"
          />
        </div>

        <div class="download-section">
          <el-button type="primary" @click="downloadArticle" :icon="Download">
            Download Article
          </el-button>
          <p class="download-note">
            Download the file to view it in your preferred application
          </p>
        </div>
      </div>

      <!-- Existing highlights overlay -->
      <div v-if="highlights.length > 0" class="highlights-overlay">
        <h4>Article Highlights:</h4>
        <div class="highlight-list">
          <div
            v-for="highlight in highlights"
            :key="highlight.id"
            class="highlight-preview"
            @click="$emit('highlight-clicked', highlight)"
          >
            <span class="highlight-text">"{{ highlight.text }}"</span>
            <span v-if="highlight.userComment" class="highlight-comment">
              - {{ highlight.userComment }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Reading, Download } from '@element-plus/icons-vue'
import { useApiService } from '../composables/useApiService'
import TextHighlighter from './TextHighlighter.vue'

export default {
  name: 'ArticleViewer',
  components: {
    Reading,
    Download,
    TextHighlighter
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
  emits: ['text-selected', 'highlight-clicked', 'highlight-deleted', 'toggle-highlight-mode'],
  setup(props, { emit }) {
    const { apiService } = useApiService()

    // Sample article content for demonstration
    const sampleArticleContent = `The Importance of Vocabulary in Language Learning

Vocabulary acquisition is one of the most crucial aspects of language learning. Research has consistently shown that a robust vocabulary is essential for effective communication in any language. Students who systematically build their vocabulary through spaced repetition and contextual learning demonstrate significantly better language proficiency outcomes.

The process of vocabulary learning involves multiple cognitive mechanisms, including encoding, storage, and retrieval. When learners encounter new words in meaningful contexts, they are more likely to retain these words in their long-term memory. This is why highlighting and annotating vocabulary within authentic texts is such an effective learning strategy.

Modern language learning applications leverage technology to enhance vocabulary acquisition through features like spaced repetition algorithms, contextual highlighting, and personalized review schedules. These tools help learners optimize their study time and improve retention rates significantly.`

    const handleTextSelected = (selectionData) => {
      emit('text-selected', selectionData)
    }

    const handleHighlightClicked = (highlight) => {
      emit('highlight-clicked', highlight)
    }

    const handleHighlightDeleted = (highlight) => {
      emit('highlight-deleted', highlight)
    }

    const downloadArticle = async () => {
      try {
        await apiService.download(
          `/api/materials/${props.material.id}/download`,
          props.material.fileName
        )
      } catch (error) {
        console.error('Error downloading article:', error)
      }
    }

    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    return {
      sampleArticleContent,
      handleTextSelected,
      handleHighlightClicked,
      handleHighlightDeleted,
      downloadArticle,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.article-viewer {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.viewer-toolbar {
  margin-bottom: 1rem;
}

.article-content {
  padding: 2rem;
  min-height: 400px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 2px dashed #d9d9d9;
}

.article-content.highlight-mode {
  border-color: #e6a23c;
  background-color: #fef9e7;
}

.placeholder-content {
  text-align: center;
  color: #606266;
}

.placeholder-icon {
  color: #e6a23c;
  margin-bottom: 1rem;
}

.article-info {
  margin: 2rem 0;
  padding: 1.5rem;
  background-color: white;
  border-radius: 6px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.article-info h4 {
  margin: 0 0 1rem 0;
  color: #303133;
}

.article-info p {
  margin: 0.5rem 0;
  color: #606266;
}

.sample-article {
  margin: 2rem 0;
  text-align: left;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.text-highlighter-section {
  margin: 2rem 0;
  text-align: left;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.text-highlighter-section h4 {
  color: #e6a23c;
  margin-bottom: 1rem;
  text-align: center;
}

.download-section {
  margin-top: 2rem;
}

.download-note {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #909399;
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
  background-color: #fef9e7;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid #e6a23c;
}

.highlight-preview:hover {
  background-color: #fdf6ec;
  transform: translateX(4px);
}

.highlight-text {
  font-weight: 600;
  color: #e6a23c;
}

.highlight-comment {
  color: #606266;
  font-style: italic;
}

/* Responsive design */
@media (max-width: 768px) {
  .article-content {
    padding: 1rem;
  }
  
  .article-info,
  .sample-article,
  .text-highlighter-section {
    max-width: none;
  }
}
</style>