<template>
  <div class="file-upload-container">
    <!-- Drag and Drop Area -->
    <div
      class="drop-zone"
      :class="{
        'drag-over': isDragOver,
        'has-files': selectedFiles.length > 0,
        'uploading': isUploading
      }"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        multiple
        :accept="acceptedFileTypes"
        @change="handleFileSelect"
        class="file-input"
      />
      
      <div class="drop-zone-content">
        <el-icon class="upload-icon" :size="48">
          <Upload />
        </el-icon>
        
        <div v-if="selectedFiles.length === 0" class="drop-zone-text">
          <h3>Drop files here or click to browse</h3>
          <p>Supported formats: {{ supportedFormatsText }}</p>
          <p>Maximum file size: {{ maxFileSizeText }}</p>
        </div>
        
        <div v-else class="selected-files-summary">
          <h3>{{ selectedFiles.length }} file(s) selected</h3>
          <p>Click to add more files or drag additional files here</p>
        </div>
      </div>
    </div>

    <!-- File List with Previews -->
    <div v-if="selectedFiles.length > 0" class="file-list">
      <h4>Selected Files</h4>
      <div class="file-items">
        <div
          v-for="(file, index) in selectedFiles"
          :key="`${file.name}-${index}`"
          class="file-item"
        >
          <!-- File Preview -->
          <div class="file-preview">
            <div v-if="isImageFile(file)" class="image-preview">
              <img :src="getFilePreview(file)" :alt="file.name" />
            </div>
            <div v-else class="file-icon">
              <el-icon :size="32">
                <Document v-if="isDocumentFile(file)" />
                <VideoPlay v-else-if="isVideoFile(file)" />
                <Files v-else />
              </el-icon>
            </div>
          </div>

          <!-- File Info -->
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-details">
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <span class="file-type">{{ getFileType(file) }}</span>
            </div>
            
            <!-- Title Input -->
            <div class="file-title-input">
              <el-input
                v-model="fileTitles[index]"
                placeholder="Enter title (optional)"
                size="small"
              />
            </div>

            <!-- Upload Progress -->
            <div v-if="uploadProgress[index] !== undefined" class="upload-progress">
              <el-progress
                :percentage="uploadProgress[index]"
                :status="uploadProgress[index] === 100 ? 'success' : 'active'"
                :stroke-width="6"
              />
            </div>

            <!-- Validation Status -->
            <div v-if="fileValidation[index]" class="validation-status">
              <el-tag
                :type="fileValidation[index].valid ? 'success' : 'danger'"
                size="small"
              >
                {{ fileValidation[index].message }}
              </el-tag>
            </div>
          </div>

          <!-- Actions -->
          <div class="file-actions">
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click="removeFile(index)"
              :disabled="isUploading"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Upload Controls -->
    <div v-if="selectedFiles.length > 0" class="upload-controls">
      <el-button
        type="primary"
        :loading="isUploading"
        :disabled="!hasValidFiles"
        @click="startUpload"
      >
        <el-icon><Upload /></el-icon>
        {{ isUploading ? 'Uploading...' : `Upload ${validFileCount} file(s)` }}
      </el-button>
      
      <el-button
        type="default"
        @click="clearFiles"
        :disabled="isUploading"
      >
        Clear All
      </el-button>
    </div>

    <!-- Overall Progress -->
    <div v-if="isUploading && overallProgress > 0" class="overall-progress">
      <h4>Upload Progress</h4>
      <el-progress
        :percentage="overallProgress"
        :status="overallProgress === 100 ? 'success' : 'active'"
      />
      <p>{{ completedUploads }} of {{ totalUploads }} files completed</p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Delete, Document, VideoPlay, Files } from '@element-plus/icons-vue'
import { useFileUpload } from '../composables/useFileUpload'
import { useMaterialService } from '../services/materialService'

export default {
  name: 'FileUpload',
  components: {
    Upload,
    Delete,
    Document,
    VideoPlay,
    Files
  },
  emits: ['upload-complete', 'upload-error', 'files-selected'],
  setup(props, { emit }) {
    const fileInput = ref(null)
    const selectedFiles = ref([])
    const fileTitles = ref([])
    const fileValidation = ref([])
    const uploadProgress = ref({})
    const isDragOver = ref(false)
    const isUploading = ref(false)
    const completedUploads = ref(0)
    const totalUploads = ref(0)

    const {
      supportedVideoFormats,
      supportedDocumentFormats,
      maxFileSize,
      validateFile,
      getMaterialType
    } = useFileUpload()

    const { uploadMaterial } = useMaterialService()

    // Computed properties
    const acceptedFileTypes = computed(() => {
      const allFormats = [...supportedVideoFormats.value, ...supportedDocumentFormats.value]
      return allFormats.map(format => `.${format}`).join(',')
    })

    const supportedFormatsText = computed(() => {
      const allFormats = [...supportedVideoFormats.value, ...supportedDocumentFormats.value]
      return allFormats.join(', ').toUpperCase()
    })

    const maxFileSizeText = computed(() => {
      const mb = Math.round(maxFileSize.value / (1024 * 1024))
      return `${mb}MB`
    })

    const hasValidFiles = computed(() => {
      return fileValidation.value.some(validation => validation?.valid)
    })

    const validFileCount = computed(() => {
      return fileValidation.value.filter(validation => validation?.valid).length
    })

    const overallProgress = computed(() => {
      if (totalUploads.value === 0) return 0
      return Math.round((completedUploads.value / totalUploads.value) * 100)
    })

    // File handling methods
    const triggerFileInput = () => {
      if (!isUploading.value) {
        fileInput.value?.click()
      }
    }

    const handleFileSelect = (event) => {
      const files = Array.from(event.target.files)
      addFiles(files)
      // Clear input to allow selecting same file again
      event.target.value = ''
    }

    const handleDrop = (event) => {
      event.preventDefault()
      isDragOver.value = false
      
      const files = Array.from(event.dataTransfer.files)
      addFiles(files)
    }

    const handleDragOver = (event) => {
      event.preventDefault()
    }

    const handleDragEnter = (event) => {
      event.preventDefault()
      isDragOver.value = true
    }

    const handleDragLeave = (event) => {
      event.preventDefault()
      // Only set to false if leaving the drop zone entirely
      if (!event.currentTarget.contains(event.relatedTarget)) {
        isDragOver.value = false
      }
    }

    const addFiles = (files) => {
      files.forEach(file => {
        // Check for duplicates
        const isDuplicate = selectedFiles.value.some(
          existingFile => existingFile.name === file.name && existingFile.size === file.size
        )
        
        if (!isDuplicate) {
          const index = selectedFiles.value.length
          selectedFiles.value.push(file)
          fileTitles.value.push(getDefaultTitle(file))
          
          // Validate file
          const validation = validateFileWithDetails(file)
          fileValidation.value.push(validation)
          
          // Generate preview for images
          if (isImageFile(file)) {
            generateImagePreview(file, index)
          }
        } else {
          ElMessage.warning(`File "${file.name}" is already selected`)
        }
      })

      emit('files-selected', selectedFiles.value)
    }

    const removeFile = (index) => {
      selectedFiles.value.splice(index, 1)
      fileTitles.value.splice(index, 1)
      fileValidation.value.splice(index, 1)
      delete uploadProgress.value[index]
      
      // Reindex remaining progress entries
      const newProgress = {}
      Object.keys(uploadProgress.value).forEach(key => {
        const keyIndex = parseInt(key)
        if (keyIndex > index) {
          newProgress[keyIndex - 1] = uploadProgress.value[key]
        } else if (keyIndex < index) {
          newProgress[keyIndex] = uploadProgress.value[key]
        }
      })
      uploadProgress.value = newProgress

      emit('files-selected', selectedFiles.value)
    }

    const clearFiles = () => {
      selectedFiles.value = []
      fileTitles.value = []
      fileValidation.value = []
      uploadProgress.value = {}
      completedUploads.value = 0
      totalUploads.value = 0
    }

    // File validation
    const validateFileWithDetails = (file) => {
      const fileExtension = file.name.split('.').pop().toLowerCase()
      const allSupportedFormats = [...supportedVideoFormats.value, ...supportedDocumentFormats.value]
      
      if (!allSupportedFormats.includes(fileExtension)) {
        return {
          valid: false,
          message: `Unsupported format: ${fileExtension.toUpperCase()}`
        }
      }

      if (file.size > maxFileSize.value) {
        return {
          valid: false,
          message: `File too large (${formatFileSize(file.size)})`
        }
      }

      return {
        valid: true,
        message: 'Valid file'
      }
    }

    // File type detection
    const isImageFile = (file) => {
      return file.type.startsWith('image/')
    }

    const isVideoFile = (file) => {
      const videoExtensions = supportedVideoFormats.value
      const extension = file.name.split('.').pop().toLowerCase()
      return videoExtensions.includes(extension)
    }

    const isDocumentFile = (file) => {
      const docExtensions = supportedDocumentFormats.value
      const extension = file.name.split('.').pop().toLowerCase()
      return docExtensions.includes(extension)
    }

    const getFileType = (file) => {
      return getMaterialType(file)
    }

    const getDefaultTitle = (file) => {
      return file.name.split('.').slice(0, -1).join('.')
    }

    // File preview
    const getFilePreview = (file) => {
      return URL.createObjectURL(file)
    }

    const generateImagePreview = (file, index) => {
      // Image previews are handled by getFilePreview method
      // This method can be extended for other preview types
    }

    // Upload functionality
    const startUpload = async () => {
      const validFiles = selectedFiles.value.filter((_, index) => fileValidation.value[index]?.valid)
      
      if (validFiles.length === 0) {
        ElMessage.error('No valid files to upload')
        return
      }

      isUploading.value = true
      completedUploads.value = 0
      totalUploads.value = validFiles.length

      const uploadResults = []

      for (let i = 0; i < selectedFiles.value.length; i++) {
        const file = selectedFiles.value[i]
        const validation = fileValidation.value[i]
        
        if (!validation?.valid) continue

        try {
          uploadProgress.value[i] = 0
          
          const materialType = getMaterialType(file)
          const result = await uploadMaterial(file, fileTitles.value[i], materialType, (progress) => {
            uploadProgress.value[i] = progress
          })

          if (result) {
            uploadProgress.value[i] = 100
            completedUploads.value++
            uploadResults.push(result)
          }
        } catch (error) {
          console.error(`Upload failed for ${file.name}:`, error)
          uploadProgress.value[i] = 0
          emit('upload-error', { file, error })
        }
      }

      isUploading.value = false
      
      if (uploadResults.length > 0) {
        emit('upload-complete', uploadResults)
        ElMessage.success(`Successfully uploaded ${uploadResults.length} file(s)`)
        clearFiles()
      }
    }

    // Utility methods
    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    // Cleanup
    onUnmounted(() => {
      // Revoke object URLs to prevent memory leaks
      selectedFiles.value.forEach(file => {
        if (isImageFile(file)) {
          URL.revokeObjectURL(getFilePreview(file))
        }
      })
    })

    return {
      fileInput,
      selectedFiles,
      fileTitles,
      fileValidation,
      uploadProgress,
      isDragOver,
      isUploading,
      completedUploads,
      totalUploads,
      acceptedFileTypes,
      supportedFormatsText,
      maxFileSizeText,
      hasValidFiles,
      validFileCount,
      overallProgress,
      triggerFileInput,
      handleFileSelect,
      handleDrop,
      handleDragOver,
      handleDragEnter,
      handleDragLeave,
      removeFile,
      clearFiles,
      isImageFile,
      isVideoFile,
      isDocumentFile,
      getFileType,
      getFilePreview,
      startUpload,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.file-upload-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.drop-zone {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fafafa;
  position: relative;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drop-zone:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.drop-zone.drag-over {
  border-color: #409eff;
  background-color: #e6f7ff;
  transform: scale(1.02);
}

.drop-zone.has-files {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.drop-zone.uploading {
  pointer-events: none;
  opacity: 0.7;
}

.file-input {
  display: none;
}

.drop-zone-content {
  width: 100%;
}

.upload-icon {
  color: #409eff;
  margin-bottom: 1rem;
}

.drop-zone-text h3 {
  margin: 0 0 0.5rem 0;
  color: #303133;
  font-weight: 500;
}

.drop-zone-text p {
  margin: 0.25rem 0;
  color: #606266;
  font-size: 0.9rem;
}

.selected-files-summary h3 {
  margin: 0 0 0.5rem 0;
  color: #67c23a;
  font-weight: 500;
}

.selected-files-summary p {
  margin: 0;
  color: #606266;
  font-size: 0.9rem;
}

.file-list {
  margin-top: 1.5rem;
}

.file-list h4 {
  margin: 0 0 1rem 0;
  color: #303133;
}

.file-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.file-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
}

.file-preview {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.file-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 0.25rem;
  word-break: break-all;
}

.file-details {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #606266;
}

.file-title-input {
  margin-bottom: 0.5rem;
}

.upload-progress {
  margin-bottom: 0.5rem;
}

.validation-status {
  margin-top: 0.25rem;
}

.file-actions {
  flex-shrink: 0;
}

.upload-controls {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.overall-progress {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.overall-progress h4 {
  margin: 0 0 1rem 0;
  color: #303133;
}

.overall-progress p {
  margin: 0.5rem 0 0 0;
  color: #606266;
  font-size: 0.9rem;
  text-align: center;
}

/* Responsive design */
@media (max-width: 768px) {
  .drop-zone {
    padding: 1.5rem 1rem;
    min-height: 150px;
  }
  
  .file-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .file-preview {
    align-self: center;
  }
  
  .upload-controls {
    flex-direction: column;
  }
  
  .file-details {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style>