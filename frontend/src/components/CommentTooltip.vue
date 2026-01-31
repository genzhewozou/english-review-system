<template>
  <el-tooltip
    :content="tooltipContent"
    :disabled="!hasComment"
    placement="top"
    :show-after="300"
    :hide-after="100"
    popper-class="highlight-comment-tooltip"
  >
    <template #content>
      <div class="comment-tooltip-content">
        <div class="comment-header">
          <el-icon class="comment-icon"><ChatDotRound /></el-icon>
          <span class="comment-label">Comment</span>
        </div>
        
        <div class="comment-text">
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
        
        <div class="comment-meta">
          <span class="created-date">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(highlight.createdDate) }}
          </span>
          <span v-if="highlight.nextReviewDate" class="review-date">
            <el-icon><Clock /></el-icon>
            Next: {{ formatDate(highlight.nextReviewDate) }}
          </span>
        </div>
        
        <div class="comment-actions">
          <el-button
            type="primary"
            size="small"
            @click="$emit('edit-comment')"
            :icon="Edit"
          >
            Edit
          </el-button>
          <el-button
            type="default"
            size="small"
            @click="$emit('start-review')"
            :icon="View"
          >
            Review
          </el-button>
        </div>
      </div>
    </template>
    
    <slot></slot>
  </el-tooltip>
</template>

<script>
import { computed } from 'vue'
import dayjs from 'dayjs'
import {
  ChatDotRound,
  Calendar,
  Clock,
  Edit,
  View
} from '@element-plus/icons-vue'

export default {
  name: 'CommentTooltip',
  components: {
    ChatDotRound,
    Calendar,
    Clock,
    Edit,
    View
  },
  props: {
    highlight: {
      type: Object,
      required: true
    }
  },
  emits: ['edit-comment', 'start-review'],
  setup(props) {
    const hasComment = computed(() => {
      return props.highlight.userComment && props.highlight.userComment.trim().length > 0
    })

    const tooltipContent = computed(() => {
      if (!hasComment.value) return ''
      return props.highlight.userComment
    })

    const formatDate = (dateString) => {
      if (!dateString) return ''
      return dayjs(dateString).format('MMM D, YYYY')
    }

    return {
      hasComment,
      tooltipContent,
      formatDate
    }
  }
}
</script>

<style scoped>
.comment-tooltip-content {
  max-width: 300px;
  padding: 0.5rem;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.comment-icon {
  color: #409eff;
}

.comment-label {
  font-weight: 600;
  color: #fff;
  font-size: 0.9rem;
}

.comment-text {
  color: #fff;
  line-height: 1.5;
  margin-bottom: 0.75rem;
  word-wrap: break-word;
}

.comment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-bottom: 0.75rem;
}

.comment-meta {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 0.75rem;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.8);
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

.comment-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.comment-actions .el-button {
  font-size: 0.8rem;
  padding: 4px 8px;
}
</style>