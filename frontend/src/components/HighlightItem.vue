<template>
  <div class="highlight-item" @click="$emit('click', highlight)">
    <div class="highlight-content">
      <div class="highlight-text">
        "{{ highlight.text }}"
      </div>
      
      <div v-if="highlight.context" class="highlight-context">
        <el-icon class="context-icon"><Reading /></el-icon>
        <span>{{ truncateContext(highlight.context) }}</span>
      </div>
      
      <div v-if="highlight.userComment" class="highlight-comment">
        <el-icon class="comment-icon"><ChatDotRound /></el-icon>
        <span class="comment-text">{{ truncateComment(highlight.userComment) }}</span>
        <el-button
          v-if="highlight.userComment.length > 100"
          type="text"
          size="small"
          @click.stop="showFullComment = !showFullComment"
        >
          {{ showFullComment ? 'Show less' : 'Show more' }}
        </el-button>
      </div>
      
      <!-- Full comment display -->
      <div v-if="showFullComment && highlight.userComment" class="full-comment">
        <div class="full-comment-text">
          {{ highlight.userComment }}
        </div>
        <div v-if="highlight.tags && highlight.tags.length > 0" class="comment-tags">
          <el-tag
            v-for="tag in highlight.tags"
            :key="tag"
            size="small"
            type="info"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      
      <div class="highlight-meta">
        <span class="created-date">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(highlight.createdDate) }}
        </span>
        <span v-if="highlight.nextReviewDate" class="review-date">
          <el-icon><Clock /></el-icon>
          Next review: {{ formatDate(highlight.nextReviewDate) }}
        </span>
      </div>
    </div>
    
    <div class="highlight-actions" @click.stop>
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

export default {
  name: 'HighlightItem',
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
    highlight: {
      type: Object,
      required: true
    }
  },
  emits: ['click', 'edit', 'delete', 'review'],
  setup(props, { emit }) {
    const showFullComment = ref(false)
    
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
          emit('edit', props.highlight)
          break
        case 'delete':
          emit('delete', props.highlight)
          break
        case 'review':
          emit('review', props.highlight)
          break
      }
    }

    return {
      showFullComment,
      truncateComment,
      truncateContext,
      formatDate,
      handleCommand
    }
  }
}
</script>

<style scoped>
.highlight-item {
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

.highlight-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.highlight-content {
  flex: 1;
  min-width: 0;
}

.highlight-text {
  font-weight: 600;
  color: #303133;
  margin-bottom: 0.5rem;
  line-height: 1.4;
  word-break: break-word;
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