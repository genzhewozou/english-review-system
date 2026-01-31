<template>
  <el-card class="material-card" shadow="hover">
    <!-- Material Header -->
    <template #header>
      <div class="material-header">
        <div class="material-title">
          <h4>{{ material.title }}</h4>
          <el-tag :type="getTypeTagType(material.type)" size="small">
            {{ formatMaterialType(material.type) }}
          </el-tag>
        </div>
        <div class="material-menu">
          <el-dropdown trigger="click" @command="handleMenuCommand">
            <el-button type="text" :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="view" :icon="View">
                  View Material
                </el-dropdown-item>
                <el-dropdown-item command="highlight" :icon="EditPen">
                  Add Highlights
                </el-dropdown-item>
                <el-dropdown-item command="download" :icon="Download">
                  Download
                </el-dropdown-item>
                <el-dropdown-item command="delete" :icon="Delete" divided>
                  Delete
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </template>

    <!-- Material Preview -->
    <div class="material-preview">
      <div class="preview-container">
        <div v-if="material.type === 'VIDEO'" class="video-preview">
          <el-icon class="preview-icon" :size="48">
            <VideoPlay />
          </el-icon>
          <div class="preview-overlay">
            <span>{{ formatMaterialType(material.type) }}</span>
          </div>
        </div>
        
        <div v-else-if="material.type === 'DOCUMENT'" class="document-preview">
          <el-icon class="preview-icon" :size="48">
            <Document />
          </el-icon>
          <div class="preview-overlay">
            <span>{{ formatMaterialType(material.type) }}</span>
          </div>
        </div>
        
        <div v-else class="article-preview">
          <el-icon class="preview-icon" :size="48">
            <Reading />
          </el-icon>
          <div class="preview-overlay">
            <span>{{ formatMaterialType(material.type) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Material Info -->
    <div class="material-info">
      <div class="info-row">
        <el-icon class="info-icon"><Document /></el-icon>
        <span class="info-label">File:</span>
        <span class="info-value" :title="material.fileName">{{ truncateFileName(material.fileName) }}</span>
      </div>
      
      <div class="info-row">
        <el-icon class="info-icon"><DataAnalysis /></el-icon>
        <span class="info-label">Size:</span>
        <span class="info-value">{{ formatFileSize(material.fileSize) }}</span>
      </div>
      
      <div class="info-row">
        <el-icon class="info-icon"><Calendar /></el-icon>
        <span class="info-label">Uploaded:</span>
        <span class="info-value">{{ formatDate(material.createdDate) }}</span>
      </div>
      
      <div v-if="material.highlightCount !== undefined" class="info-row">
        <el-icon class="info-icon"><EditPen /></el-icon>
        <span class="info-label">Highlights:</span>
        <span class="info-value">{{ material.highlightCount || 0 }}</span>
      </div>
    </div>

    <!-- Material Stats -->
    <div v-if="showStats" class="material-stats">
      <div class="stat-item">
        <span class="stat-number">{{ material.highlightCount || 0 }}</span>
        <span class="stat-label">Highlights</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ material.reviewCount || 0 }}</span>
        <span class="stat-label">Reviews</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ material.lastAccessed ? formatRelativeTime(material.lastAccessed) : 'Never' }}</span>
        <span class="stat-label">Last Viewed</span>
      </div>
    </div>

    <!-- Action Buttons -->
    <template #footer>
      <div class="material-actions">
        <el-button type="primary" @click="$emit('view', material)" :icon="View">
          View
        </el-button>
        <el-button type="success" @click="$emit('highlight', material)" :icon="EditPen">
          Highlight
        </el-button>
        <el-button
          type="danger"
          @click="$emit('delete', material)"
          :icon="Delete"
          plain
        >
          Delete
        </el-button>
      </div>
    </template>
  </el-card>
</template>

<script>
import { computed } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import {
  MoreFilled,
  View,
  EditPen,
  Download,
  Delete,
  VideoPlay,
  Document,
  Reading,
  DataAnalysis,
  Calendar
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)

export default {
  name: 'MaterialCard',
  components: {
    MoreFilled,
    View,
    EditPen,
    Download,
    Delete,
    VideoPlay,
    Document,
    Reading,
    DataAnalysis,
    Calendar
  },
  props: {
    material: {
      type: Object,
      required: true
    },
    showStats: {
      type: Boolean,
      default: false
    }
  },
  emits: ['view', 'highlight', 'delete', 'download'],
  setup(props, { emit }) {
    // Computed properties
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

    const formatRelativeTime = (dateString) => {
      return dayjs(dateString).fromNow()
    }

    const truncateFileName = (fileName) => {
      if (!fileName) return ''
      if (fileName.length <= 30) return fileName
      
      const extension = fileName.split('.').pop()
      const nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'))
      const truncatedName = nameWithoutExt.substring(0, 25) + '...'
      
      return `${truncatedName}.${extension}`
    }

    // Methods
    const handleMenuCommand = (command) => {
      switch (command) {
        case 'view':
          emit('view', props.material)
          break
        case 'highlight':
          emit('highlight', props.material)
          break
        case 'download':
          emit('download', props.material)
          break
        case 'delete':
          emit('delete', props.material)
          break
      }
    }

    return {
      getTypeTagType,
      formatMaterialType,
      formatFileSize,
      formatDate,
      formatRelativeTime,
      truncateFileName,
      handleMenuCommand
    }
  }
}
</script>

<style scoped>
.material-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.material-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.material-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.material-title {
  flex: 1;
  min-width: 0;
}

.material-title h4 {
  margin: 0 0 0.5rem 0;
  color: #303133;
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-word;
}

.material-menu {
  flex-shrink: 0;
}

.material-preview {
  margin: 1rem 0;
}

.preview-container {
  position: relative;
  height: 120px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.video-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.document-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #67c23a;
}

.article-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #e6a23c;
}

.preview-icon {
  margin-bottom: 0.5rem;
}

.preview-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 0.5rem;
  text-align: center;
  font-size: 0.85rem;
  font-weight: 500;
}

.material-info {
  flex: 1;
  margin-bottom: 1rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.info-icon {
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
}

.info-label {
  color: #606266;
  font-weight: 500;
  min-width: 60px;
}

.info-value {
  color: #303133;
  flex: 1;
  min-width: 0;
  word-break: break-all;
}

.material-stats {
  display: flex;
  justify-content: space-around;
  padding: 1rem 0;
  border-top: 1px solid #ebeef5;
  margin-bottom: 1rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 1.2rem;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.8rem;
  color: #909399;
}

.material-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: space-between;
}

.material-actions .el-button {
  flex: 1;
}

/* Responsive design */
@media (max-width: 768px) {
  .material-actions {
    flex-direction: column;
  }
  
  .material-actions .el-button {
    width: 100%;
  }
  
  .material-stats {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .stat-number {
    display: inline;
    margin-bottom: 0;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .preview-container {
    background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  }
}
</style>