<template>
  <div class="statistics-panel">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8">
        <div class="stat-card current">
          <div class="stat-icon">
            <el-icon :size="32"><EditPen /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ currentWord || '-' }}</div>
            <div class="stat-label">当前听写</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="8">
        <div class="stat-card completed">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-label">本次已听写</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="8">
        <div class="stat-card remaining">
          <div class="stat-icon">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ remainingCount }}</div>
            <div class="stat-label">还剩余</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 进度条 -->
    <div class="progress-section" v-if="showProgress">
      <div class="progress-header">
        <span>听写进度</span>
        <span class="progress-text">{{ progress }}%</span>
      </div>
      <el-progress
        :percentage="progress"
        :stroke-width="20"
        :show-text="false"
        class="progress-bar"
      />
    </div>

    <!-- 计时器 -->
    <div class="timer-section" v-if="showTimer">
      <el-icon :size="20"><Timer /></el-icon>
      <span class="timer-text">{{ formattedTime }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentWord: {
    type: String,
    default: ''
  },
  completedCount: {
    type: Number,
    default: 0
  },
  remainingCount: {
    type: Number,
    default: 0
  },
  progress: {
    type: Number,
    default: 0
  },
  elapsedTime: {
    type: Number,
    default: 0
  },
  showProgress: {
    type: Boolean,
    default: true
  },
  showTimer: {
    type: Boolean,
    default: true
  }
})

// 格式化时间
const formattedTime = computed(() => {
  const minutes = Math.floor(props.elapsedTime / 60)
  const seconds = props.elapsedTime % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})
</script>

<style lang="scss" scoped>
.statistics-panel {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  margin-bottom: 16px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  }

  &.current {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    .stat-icon, .stat-content {
      color: white;
    }

    .stat-label {
      color: rgba(255, 255, 255, 0.9);
    }
  }

  &.completed {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);

    .stat-icon, .stat-content {
      color: white;
    }

    .stat-label {
      color: rgba(255, 255, 255, 0.9);
    }
  }

  &.remaining {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);

    .stat-icon, .stat-content {
      color: white;
    }

    .stat-label {
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
  }

  .stat-content {
    flex: 1;

    .stat-value {
      font-size: 36px;
      font-weight: bold;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 16px;
      margin-top: 4px;
    }
  }
}

.progress-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-top: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);

  .progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    font-size: 16px;
    color: #606266;

    .progress-text {
      font-weight: bold;
      color: #409EFF;
    }
  }

  .progress-bar {
    :deep(.el-progress-bar__outer) {
      border-radius: 10px;
      background: #EBEEF5;
    }

    :deep(.el-progress-bar__inner) {
      border-radius: 10px;
      background: linear-gradient(90deg, #409EFF 0%, #67C23A 100%);
    }
  }
}

.timer-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  width: fit-content;
  margin-left: auto;
  margin-right: auto;

  .timer-text {
    font-size: 24px;
    font-weight: bold;
    color: #409EFF;
    font-family: 'Courier New', monospace;
  }
}

@media (max-width: 768px) {
  .stat-card {
    .stat-content {
      .stat-value {
        font-size: 28px;
      }

      .stat-label {
        font-size: 14px;
      }
    }

    .stat-icon {
      width: 48px;
      height: 48px;
    }
  }
}
</style>