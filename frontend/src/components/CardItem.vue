<template>
  <div class="card-item" @click="$emit('click', card)">
    <div class="card-content">
      <div class="card-text">
        "{{ card.frontText || card.text }}"
        <button 
          @click.stop="speakText(card.frontText || card.text)" 
          class="btn-speak"
          title="Speak this word/phrase"
        >
          🔊
        </button>
      </div>
      
      <div v-if="card.context" class="card-context">
        <el-icon class="context-icon"><Reading /></el-icon>
        <span>{{ truncateContext(card.context) }}</span>
      </div>
      
      <div v-if="card.userComment" class="card-comment">
        <el-icon class="comment-icon"><ChatDotRound /></el-icon>
        <span class="comment-text">{{ truncateComment(card.userComment) }}</span>
        <el-button
          v-if="card.userComment.length > 100"
          type="text"
          size="small"
          @click.stop="showFullComment = !showFullComment"
        >
          {{ showFullComment ? 'Show less' : 'Show more' }}
        </el-button>
      </div>
      
      <!-- Full comment display -->
      <div v-if="showFullComment && card.userComment" class="full-comment">
        <div class="full-comment-text">
          {{ card.userComment }}
        </div>
        <div v-if="card.tags && card.tags.length > 0" class="comment-tags">
          <el-tag
            v-for="tag in card.tags"
            :key="tag"
            size="small"
            type="info"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      
      <div class="card-meta">
        <span class="created-date">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(card.createdDate) }}
        </span>
        <span v-if="card.nextReviewDate" class="review-date">
          <el-icon><Clock /></el-icon>
          Next review: {{ formatDate(card.nextReviewDate) }}
        </span>
      </div>
    </div>
    
    <div class="card-actions" @click.stop>
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button type="text" :icon="MoreFilled" size="small" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="edit" :icon="Edit">
              Edit Comment
            </el-dropdown-item>
            <el-dropdown-item command="review" :icon="View">
              Start Review
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

<script>
import dayjs from 'dayjs'
import { ref } from 'vue'
import {
  MoreFilled,
  Edit,
  View,
  Delete,
  Reading,
  ChatDotRound,
  Calendar,
  Clock
} from '@element-plus/icons-vue'
import { useSpeechService } from '../composables/useSpeechService'

export default {
  name: 'CardItem',
  components: {
    MoreFilled,
    Edit,
    View,
    Delete,
    Reading,
    ChatDotRound,
    Calendar,
    Clock
  },
  props: {
    card: {
      type: Object,
      required: true
    }
  },
  emits: ['click', 'edit', 'delete', 'review'],
  setup(props, { emit }) {
    const showFullComment = ref(false)
    const { speakText } = useSpeechService()
    
    const truncateComment = (comment) => {
      if (!comment) return ''
      if (comment.length <= 100) return comment
      return comment.substring(0, 100) + '...'
    }

    const truncateContext = (context) => {
      if (!context) return ''
      if (context.length <= 100) return context
      return context.substring(0, 100) + '...'
    }

    const formatDate = (dateString) => {
      return dayjs(dateString).format('MMM D')
    }

    const handleCommand = (command) => {
      switch (command) {
        case 'edit':
          emit('edit', props.card)
          break
        case 'delete':
          emit('delete', props.card)
          break
        case 'review':
          emit('review', props.card)
          break
      }
    }

    return {
      showFullComment,
      truncateComment,
      truncateContext,
      formatDate,
      handleCommand,
      speakText
    }
  }
}
</script>

<style scoped>
.card-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.card-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-text {
  font-weight: 600;
  color: #303133;
  margin-bottom: 0.5rem;
  line-height: 1.4;
  word-break: break-word;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-speak {
  background: none;
  border: 2px solid #409eff;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.btn-speak:hover {
  background-color: #409eff;
  transform: scale(1.1);
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

.highlight-context,
.highlight-comment {
  display: flex;
  align-items: flex-start;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #606266;
  line-height: 1.4;
}

.comment-text {
  flex: 1;
  word-wrap: break-word;
}

.full-comment {
  margin-top: 0.5rem;
  padding: 0.75rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.full-comment-text {
  color: #303133;
  line-height: 1.5;
  margin-bottom: 0.5rem;
  white-space: pre-wrap;
}

.comment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.highlight-context {
  font-style: italic;
}

.context-icon,
.comment-icon {
  flex-shrink: 0;
  margin-top: 0.1rem;
  font-size: 12px;
  color: #909399;
}

.highlight-meta {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #909399;
}

.created-date,
.review-date {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.review-date {
  color: #e6a23c;
}

.highlight-actions {
  flex-shrink: 0;
}

/* Responsive design */
@media (max-width: 768px) {
  .highlight-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .highlight-actions {
    align-self: flex-end;
  }
  
  .highlight-meta {
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>