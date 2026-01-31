<template>
  <div class="comment-manager">
    <!-- Comment Display Mode -->
    <div v-if="!editMode" class="comment-display">
      <div class="comment-header">
        <div class="highlight-info">
          <span class="highlight-text">"{{ highlight.text }}"</span>
          <span v-if="highlight.context" class="highlight-context">
            in "{{ truncateText(highlight.context, 50) }}"
          </span>
        </div>
        <div class="comment-actions">
          <el-button
            type="primary"
            size="small"
            @click="enterEditMode"
            :icon="Edit"
          >
            {{ hasComment ? 'Edit' : 'Add' }} Comment
          </el-button>
          <el-button
            v-if="hasComment"
            type="danger"
            size="small"
            @click="deleteComment"
            :icon="Delete"
          >
            Delete
          </el-button>
        </div>
      </div>

      <div v-if="hasComment" class="comment-content">
        <div class="comment-text">
          {{ highlight.userComment }}
        </div>
        
        <div v-if="highlight.tags && highlight.tags.length > 0" class="comment-tags">
          <el-tag
            v-for="tag in highlight.tags"
            :key="tag"
            size="small"
            type="info"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="comment-meta">
          <span class="created-date">
            <el-icon><Calendar /></el-icon>
            Created: {{ formatDate(highlight.createdDate) }}
          </span>
          <span v-if="highlight.updatedDate && highlight.updatedDate !== highlight.createdDate" class="updated-date">
            <el-icon><Edit /></el-icon>
            Updated: {{ formatDate(highlight.updatedDate) }}
          </span>
        </div>
      </div>

      <div v-else class="no-comment">
        <el-empty
          description="No comment added yet"
          :image-size="60"
        >
          <el-button type="primary" @click="enterEditMode" :icon="Plus">
            Add Comment
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- Comment Edit Mode -->
    <div v-else class="comment-edit">
      <div class="edit-header">
        <h4>{{ hasComment ? 'Edit' : 'Add' }} Comment</h4>
        <div class="edit-actions">
          <el-button size="small" @click="cancelEdit" :icon="Close">
            Cancel
          </el-button>
        </div>
      </div>

      <el-form
        ref="commentFormRef"
        :model="editForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="Comment" prop="comment">
          <el-input
            v-model="editForm.comment"
            type="textarea"
            :rows="6"
            placeholder="Add your notes, translation, or explanation..."
            maxlength="1000"
            show-word-limit
            class="comment-textarea"
          />
        </el-form-item>

        <!-- Quick Templates -->
        <el-form-item label="Templates">
          <div class="template-section">
            <el-text size="small" type="info">Quick templates:</el-text>
            <div class="template-buttons">
              <el-button
                v-for="template in commentTemplates"
                :key="template.id"
                size="small"
                type="default"
                @click="applyTemplate(template)"
              >
                {{ template.name }}
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="Tags" prop="tags">
          <el-select
            v-model="editForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="Add tags to categorize this highlight"
            class="tags-select"
          >
            <el-option
              v-for="tag in suggestedTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Difficulty">
          <el-radio-group v-model="editForm.difficulty">
            <el-radio-button label="easy">Easy</el-radio-button>
            <el-radio-button label="medium">Medium</el-radio-button>
            <el-radio-button label="hard">Hard</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <div class="form-actions">
          <el-button @click="cancelEdit">
            Cancel
          </el-button>
          <el-button type="primary" @click="saveComment" :loading="saving">
            {{ hasComment ? 'Update' : 'Save' }} Comment
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  Edit,
  Delete,
  Plus,
  Close,
  Calendar
} from '@element-plus/icons-vue'

export default {
  name: 'CommentManager',
  components: {
    Edit,
    Delete,
    Plus,
    Close,
    Calendar
  },
  props: {
    highlight: {
      type: Object,
      required: true
    },
    materialId: {
      type: [String, Number],
      required: true
    }
  },
  emits: ['comment-updated', 'comment-deleted'],
  setup(props, { emit }) {
    // Refs
    const commentFormRef = ref(null)
    
    // State
    const editMode = ref(false)
    const saving = ref(false)
    const editForm = ref({
      comment: '',
      tags: [],
      difficulty: 'medium'
    })

    // Data
    const commentTemplates = ref([
      { id: 1, name: 'Definition', template: 'Definition: ' },
      { id: 2, name: 'Translation', template: 'Translation: ' },
      { id: 3, name: 'Example', template: 'Example: ' },
      { id: 4, name: 'Pronunciation', template: 'Pronunciation: /' },
      { id: 5, name: 'Grammar Note', template: 'Grammar: ' },
      { id: 6, name: 'Synonym', template: 'Synonym: ' },
      { id: 7, name: 'Usage', template: 'Usage: ' },
      { id: 8, name: 'Etymology', template: 'Etymology: ' }
    ])

    const suggestedTags = ref([
      'vocabulary',
      'grammar',
      'idiom',
      'phrase',
      'difficult',
      'important',
      'review',
      'pronunciation',
      'collocation',
      'formal',
      'informal',
      'academic'
    ])

    const formRules = {
      comment: [
        {
          max: 1000,
          message: 'Comment cannot exceed 1000 characters',
          trigger: 'blur'
        }
      ]
    }

    // Computed properties
    const hasComment = computed(() => {
      return props.highlight.userComment && props.highlight.userComment.trim().length > 0
    })

    // Methods
    const enterEditMode = () => {
      editForm.value = {
        comment: props.highlight.userComment || '',
        tags: props.highlight.tags || [],
        difficulty: props.highlight.difficulty || 'medium'
      }
      editMode.value = true
    }

    const cancelEdit = () => {
      editMode.value = false
      editForm.value = {
        comment: '',
        tags: [],
        difficulty: 'medium'
      }
    }

    const saveComment = async () => {
      if (!commentFormRef.value) return

      try {
        const valid = await commentFormRef.value.validate()
        if (!valid) return

        saving.value = true

        const updatedHighlight = {
          ...props.highlight,
          userComment: editForm.value.comment.trim(),
          tags: editForm.value.tags,
          difficulty: editForm.value.difficulty,
          updatedDate: new Date().toISOString()
        }

        // Emit the update
        emit('comment-updated', updatedHighlight)
        
        editMode.value = false
        ElMessage.success('Comment saved successfully')
      } catch (error) {
        console.error('Error saving comment:', error)
        ElMessage.error('Failed to save comment')
      } finally {
        saving.value = false
      }
    }

    const deleteComment = async () => {
      try {
        await ElMessageBox.confirm(
          'Are you sure you want to delete this comment?',
          'Delete Comment',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        const updatedHighlight = {
          ...props.highlight,
          userComment: '',
          tags: [],
          updatedDate: new Date().toISOString()
        }

        emit('comment-updated', updatedHighlight)
        ElMessage.success('Comment deleted successfully')
      } catch {
        // User cancelled
      }
    }

    const applyTemplate = (template) => {
      const currentComment = editForm.value.comment.trim()
      if (currentComment && !currentComment.endsWith('\n')) {
        editForm.value.comment += '\n'
      }
      editForm.value.comment += template.template
    }

    const truncateText = (text, maxLength) => {
      if (!text || text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      return dayjs(dateString).format('MMM D, YYYY [at] h:mm A')
    }

    return {
      commentFormRef,
      editMode,
      saving,
      editForm,
      commentTemplates,
      suggestedTags,
      formRules,
      hasComment,
      enterEditMode,
      cancelEdit,
      saveComment,
      deleteComment,
      applyTemplate,
      truncateText,
      formatDate
    }
  }
}
</script>

<style scoped>
.comment-manager {
  width: 100%;
  max-width: 600px;
}

.comment-display {
  background-color: white;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.highlight-info {
  flex: 1;
  min-width: 0;
}

.highlight-text {
  display: block;
  font-weight: 600;
  color: #303133;
  margin-bottom: 0.25rem;
  word-wrap: break-word;
}

.highlight-context {
  display: block;
  font-size: 0.85rem;
  color: #606266;
  font-style: italic;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
  margin-left: 1rem;
}

.comment-content {
  padding: 1rem;
}

.comment-text {
  color: #303133;
  line-height: 1.6;
  margin-bottom: 1rem;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.comment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag-item {
  margin: 0;
}

.comment-meta {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: #909399;
}

.created-date,
.updated-date {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.no-comment {
  padding: 2rem;
  text-align: center;
}

.comment-edit {
  background-color: white;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  padding: 1rem;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #ebeef5;
}

.edit-header h4 {
  margin: 0;
  color: #303133;
}

.comment-textarea {
  width: 100%;
}

.template-section {
  width: 100%;
}

.template-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.template-buttons .el-button {
  font-size: 0.8rem;
  padding: 4px 8px;
}

.tags-select {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #ebeef5;
}

/* Responsive design */
@media (max-width: 768px) {
  .comment-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .comment-actions {
    margin-left: 0;
    justify-content: flex-end;
  }
  
  .edit-header {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }
  
  .edit-actions {
    align-self: flex-end;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
  
  .template-buttons {
    justify-content: center;
  }
}
</style>