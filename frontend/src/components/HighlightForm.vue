<template>
  <div class="highlight-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <!-- Selected Text Display -->
      <div v-if="selectedText" class="selected-text-section">
        <h4>Selected Text</h4>
        <div class="selected-text">
          "{{ selectedText }}"
        </div>
      </div>

      <!-- Context Display -->
      <div v-if="context" class="context-section">
        <h4>Context</h4>
        <div class="context-text">
          {{ context }}
        </div>
      </div>

      <!-- Comment Input with Rich Features -->
      <el-form-item label="Comment" prop="comment">
        <div class="comment-input-section">
          <el-input
            v-model="formData.comment"
            type="textarea"
            :rows="4"
            placeholder="Add your notes, translation, or explanation for this highlight..."
            maxlength="1000"
            show-word-limit
            class="comment-textarea"
          />
          
          <!-- Comment Templates -->
          <div class="comment-templates" v-if="commentTemplates.length > 0">
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
        </div>
      </el-form-item>

      <!-- Tags Input (Optional) -->
      <el-form-item label="Tags">
        <el-select
          v-model="formData.tags"
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

      <!-- Difficulty Level -->
      <el-form-item label="Difficulty">
        <el-radio-group v-model="formData.difficulty">
          <el-radio-button label="easy">Easy</el-radio-button>
          <el-radio-button label="medium">Medium</el-radio-button>
          <el-radio-button label="hard">Hard</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- Review Settings -->
      <el-form-item label="Review">
        <el-checkbox v-model="formData.enableReview">
          Add to spaced repetition review
        </el-checkbox>
      </el-form-item>

      <!-- Form Actions -->
      <div class="form-actions">
        <el-button @click="$emit('cancel')">
          Cancel
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">
          {{ isEditing ? 'Update' : 'Save' }} Highlight
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'HighlightForm',
  props: {
    selectedText: {
      type: String,
      default: ''
    },
    context: {
      type: String,
      default: ''
    },
    highlight: {
      type: Object,
      default: null
    },
    materialId: {
      type: [String, Number],
      required: true
    }
  },
  emits: ['save', 'cancel'],
  setup(props, { emit }) {
    const formRef = ref(null)
    const saving = ref(false)
    
    const formData = ref({
      comment: '',
      tags: [],
      difficulty: 'medium',
      enableReview: true
    })

    const suggestedTags = ref([
      'vocabulary',
      'grammar',
      'idiom',
      'phrase',
      'difficult',
      'important',
      'review',
      'pronunciation'
    ])

    const commentTemplates = ref([
      { id: 1, name: 'Definition', template: 'Definition: ' },
      { id: 2, name: 'Translation', template: 'Translation: ' },
      { id: 3, name: 'Example', template: 'Example: ' },
      { id: 4, name: 'Pronunciation', template: 'Pronunciation: /' },
      { id: 5, name: 'Grammar Note', template: 'Grammar: ' },
      { id: 6, name: 'Synonym', template: 'Synonym: ' }
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
    const isEditing = computed(() => {
      return props.highlight !== null
    })

    // Methods
    const initializeForm = () => {
      if (props.highlight) {
        // Editing existing highlight
        formData.value = {
          comment: props.highlight.userComment || '',
          tags: props.highlight.tags || [],
          difficulty: props.highlight.difficulty || 'medium',
          enableReview: props.highlight.enableReview !== false
        }
      } else {
        // Creating new highlight
        formData.value = {
          comment: '',
          tags: [],
          difficulty: 'medium',
          enableReview: true
        }
      }
    }

    const handleSubmit = async () => {
      if (!formRef.value) return

      try {
        const valid = await formRef.value.validate()
        if (!valid) return

        saving.value = true

        const highlightData = {
          comment: formData.value.comment.trim(),
          tags: formData.value.tags,
          difficulty: formData.value.difficulty,
          enableReview: formData.value.enableReview
        }

        // Add position data for new highlights
        if (!isEditing.value) {
          // These would be calculated by the parent component
          // based on the actual text selection
          highlightData.startPosition = 0
          highlightData.endPosition = props.selectedText?.length || 0
        }

        emit('save', highlightData)
      } catch (error) {
        console.error('Form validation failed:', error)
        ElMessage.error('Please check the form for errors')
      } finally {
        saving.value = false
      }
    }

    const addCustomTag = (tag) => {
      if (tag && !formData.value.tags.includes(tag)) {
        formData.value.tags.push(tag)
      }
    }

    const applyTemplate = (template) => {
      const currentComment = formData.value.comment.trim()
      if (currentComment && !currentComment.endsWith('\n')) {
        formData.value.comment += '\n'
      }
      formData.value.comment += template.template
    }

    onMounted(() => {
      initializeForm()
    })

    return {
      formRef,
      saving,
      formData,
      formRules,
      suggestedTags,
      commentTemplates,
      isEditing,
      handleSubmit,
      addCustomTag,
      applyTemplate
    }
  }
}
</script>

<style scoped>
.highlight-form {
  padding: 1rem 0;
}

.selected-text-section,
.context-section {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.selected-text-section h4,
.context-section h4 {
  margin: 0 0 0.75rem 0;
  color: #303133;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.selected-text {
  font-size: 1.1rem;
  font-weight: 600;
  color: #409eff;
  line-height: 1.5;
  font-style: italic;
}

.context-text {
  color: #606266;
  line-height: 1.5;
  font-style: italic;
}

.comment-input-section {
  width: 100%;
}

.comment-textarea {
  margin-bottom: 0.75rem;
}

.comment-templates {
  padding: 0.75rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
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
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #ebeef5;
}

/* Custom styling for form items */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 100px;
}

:deep(.el-radio-button__inner) {
  padding: 8px 16px;
}

/* Responsive design */
@media (max-width: 768px) {
  .highlight-form {
    padding: 0.5rem 0;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
  
  .selected-text-section,
  .context-section {
    margin-bottom: 1rem;
    padding: 0.75rem;
  }
}
</style>