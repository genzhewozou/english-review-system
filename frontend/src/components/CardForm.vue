<template>
  <div class="card-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      @submit.prevent="handleSubmit"
      class="modern-form"
    >

      <!-- Selected Text Display -->
      <div v-if="selectedText" class="selected-text-section">
        <div class="section-label">SELECTED TEXT</div>
        <div class="selected-text">
          "{{ selectedText }}"
        </div>
      </div>

      <!-- Context Display -->
      <div v-if="context" class="context-section">
        <div class="section-label">CONTEXT</div>
        <div class="context-text">
          {{ context }}
        </div>
      </div>

      <!-- Back Text Input (Translation/Definition) -->
      <el-form-item label="Back Text" prop="backText">
        <el-input
          v-model="formData.backText"
          type="textarea"
          :rows="3"
          placeholder="Enter translation, definition, or explanation..."
          maxlength="1000"
          show-word-limit
          class="back-text-textarea"
          resize="vertical"
        />
      </el-form-item>

      <!-- Context Input (Optional) -->
      <el-form-item label="Context" prop="context">
        <el-input
          v-model="formData.context"
          type="textarea"
          :rows="2"
          placeholder="Enter surrounding context (optional)..."
          maxlength="500"
          show-word-limit
          class="context-textarea"
          resize="vertical"
        />
      </el-form-item>

      <!-- Comment Input with Rich Features -->
      <el-form-item label="Comment" prop="comment">
        <div class="comment-input-section">
          <el-input
            v-model="formData.comment"
            type="textarea"
            :rows="4"
            placeholder="Add your notes, translation, or explanation for this card..."
            maxlength="1000"
            show-word-limit
            class="comment-textarea"
            resize="vertical"
          />
          
          <!-- Comment Templates -->
          <div class="comment-templates" v-if="commentTemplates.length > 0">
            <div class="templates-label">Quick templates:</div>
            <div class="template-buttons">
              <el-button
                v-for="template in commentTemplates"
                :key="template.id"
                size="small"
                type="default"
                @click="applyTemplate(template)"
                class="template-button"
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
          placeholder="Add tags to categorize this card"
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
        <div class="difficulty-buttons">
          <button
            v-for="level in difficultyLevels"
            :key="level.value"
            @click="formData.difficulty = level.value"
            :class="['difficulty-button', { active: formData.difficulty === level.value }]"
          >
            {{ level.label }}
          </button>
        </div>
      </el-form-item>

      <!-- Review Settings -->
      <el-form-item label="Review">
        <el-checkbox v-model="formData.enableReview" class="review-checkbox">
          Add to spaced repetition review
        </el-checkbox>
      </el-form-item>

      <!-- Form Actions -->
      <div class="form-actions">
        <el-button @click="$emit('cancel')" class="btn-secondary">
          Cancel
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving" class="btn-primary">
          {{ isEditing ? 'Update' : 'Save' }} Card
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'CardForm',
  props: {
    selectedText: {
      type: String,
      default: ''
    },
    context: {
      type: String,
      default: ''
    },
    card: {
      type: Object,
      default: null
    },
    materialId: {
      type: [String, Number],
      required: true
    },
    tags: {
      type: Array,
      default: () => []
    }
  },
  emits: ['save', 'cancel'],
  setup(props, { emit }) {
    const formRef = ref(null)
    const saving = ref(false)
    
    const formData = ref({
      backText: '',
      context: '',
      comment: '',
      tags: [],
      difficulty: 'medium',
      enableReview: true
    })

    const difficultyLevels = ref([
      { value: 'easy', label: 'Easy' },
      { value: 'medium', label: 'Medium' },
      { value: 'hard', label: 'Hard' }
    ])

    const suggestedTags = ref(props.tags)

    const commentTemplates = ref([
      { id: 1, name: 'Definition', template: 'Definition: ' },
      { id: 2, name: 'Translation', template: 'Translation: ' },
      { id: 3, name: 'Example', template: 'Example: ' },
      { id: 4, name: 'Pronunciation', template: 'Pronunciation: /' },
      { id: 5, name: 'Grammar Note', template: 'Grammar: ' },
      { id: 6, name: 'Synonym', template: 'Synonym: ' }
    ])

    const formRules = {
      backText: [
        {
          required: true,
          message: 'Back text is required',
          trigger: 'blur'
        },
        {
          max: 1000,
          message: 'Back text cannot exceed 1000 characters',
          trigger: 'blur'
        }
      ],
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
      return props.card !== null
    })

    // Methods
    const initializeForm = () => {
      if (props.card) {
        // Editing existing card
        formData.value = {
          backText: props.card.backText || '',
          context: props.card.context || '',
          comment: props.card.userComment || '',
          tags: [],
          difficulty: props.card.difficulty || 'medium',
          enableReview: props.card.enableReview !== false
        }
      } else {
        // Creating new card
        formData.value = {
          backText: '',
          context: props.context || '',
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
          backText: formData.value.backText.trim(),
          context: formData.value.context.trim(),
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
      difficultyLevels,
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
/* Base styles */
.card-form {
  width: 100%;
  max-width: 500px;
}

.modern-form {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.modern-form:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
}

/* Form header */
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.form-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333333;
}

.form-close {
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.form-close:hover {
  background-color: #f5f5f5;
}

.close-icon {
  font-size: 1.5rem;
  color: #999999;
  line-height: 1;
}

/* Selected text and context sections */
.selected-text-section,
.context-section {
  margin-bottom: 1.5rem;
  padding: 1.25rem;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 10px;
  border: 1px solid #e0e7ff;
  transition: all 0.3s ease;
}

.selected-text-section:hover,
.context-section:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.section-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
  display: block;
}

.selected-text {
  font-size: 1.1rem;
  font-weight: 600;
  color: #3b82f6;
  line-height: 1.5;
  font-style: italic;
}

.context-text {
  color: #475569;
  line-height: 1.5;
  font-style: italic;
}

/* Comment section */
.comment-input-section {
  width: 100%;
}

.comment-textarea {
  margin-bottom: 1rem;
  border-radius: 8px !important;
}

.comment-templates {
  padding: 1rem;
  background-color: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  margin-top: 0.5rem;
}

.templates-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.75rem;
  display: block;
}

.template-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.template-button {
  font-size: 0.75rem !important;
  padding: 0.375rem 0.75rem !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;
}

.template-button:hover {
  background-color: #f1f5f9 !important;
  transform: translateY(-1px) !important;
}

/* Tags section */
.tags-select {
  width: 100%;
  border-radius: 8px !important;
}

/* Difficulty section */
.difficulty-buttons {
  display: flex;
  gap: 0.5rem;
}

.difficulty-button {
  flex: 1;
  padding: 0.625rem 1rem;
  border: 2px solid #e2e8f0;
  background-color: #ffffff;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.difficulty-button:hover {
  border-color: #cbd5e1;
  background-color: #f8fafc;
  transform: translateY(-1px);
}

.difficulty-button.active {
  border-color: #3b82f6;
  background-color: #3b82f6;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.difficulty-button.active:hover {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* Review section */
.review-checkbox {
  font-size: 0.875rem;
  color: #475569;
  cursor: pointer;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #3b82f6;
  border-color: #3b82f6;
}

/* Form actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #f0f0f0;
}

.btn-secondary {
  padding: 0.625rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background-color: #f5f5f5;
  transform: translateY(-1px);
}

.btn-primary {
  padding: 0.625rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* Custom Element Plus overrides */
:deep(.el-form-item) {
  margin-bottom: 1.25rem;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #64748b;
  font-size: 0.875rem;
}

:deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 120px;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  transition: all 0.2s ease;
}

:deep(.el-textarea__inner:focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

:deep(.el-select) {
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  transition: all 0.2s ease;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: #cbd5e1;
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* Responsive design */
@media (max-width: 768px) {
  .card-form {
    max-width: 100%;
  }
  
  .modern-form {
    padding: 1.25rem;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .btn-secondary,
  .btn-primary {
    width: 100%;
  }
  
  .selected-text-section,
  .context-section {
    margin-bottom: 1.25rem;
    padding: 1rem;
  }
  
  .difficulty-buttons {
    flex-direction: column;
  }
  
  .difficulty-button {
    text-align: center;
  }
}

/* Animation classes */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.selected-text-section,
.context-section,
.form-header,
.el-form-item {
  animation: fadeIn 0.3s ease forwards;
}

.selected-text-section {
  animation-delay: 0.1s;
}

.context-section {
  animation-delay: 0.2s;
}

.el-form-item {
  animation-delay: 0.3s;
}

.form-actions {
  animation-delay: 0.4s;
}
</style>