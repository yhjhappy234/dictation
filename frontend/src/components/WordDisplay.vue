<template>
  <div class="word-display-container">
    <div
      class="word-display"
      :class="{
        'is-speaking': isSpeaking,
        'is-empty': !word,
        'is-completed': isCompleted
      }"
    >
      <transition name="word-fade" mode="out-in">
        <div v-if="word" :key="word" class="word-content">
          <span class="word-text">{{ word }}</span>
          <div v-if="showPinyin" class="word-pinyin">{{ pinyin }}</div>
        </div>
        <div v-else class="word-placeholder">
          <el-icon :size="48"><EditPen /></el-icon>
          <span>{{ placeholder }}</span>
        </div>
      </transition>
    </div>

    <!-- 状态指示器 -->
    <div v-if="isStarted" class="status-indicator">
      <div class="status-item" :class="{ active: isSpeaking }">
        <el-icon><Microphone /></el-icon>
        <span>正在朗读</span>
      </div>
      <div class="status-item" :class="{ active: isListening }">
        <el-icon><Headset /></el-icon>
        <span>正在监听</span>
      </div>
    </div>

    <!-- 词语进度指示 -->
    <div v-if="words.length > 0 && currentIndex >= 0" class="word-progress">
      <span class="progress-text">
        第 {{ currentIndex + 1 }} / {{ words.length }} 个词语
      </span>
      <div class="progress-dots">
        <span
          v-for="(w, index) in words"
          :key="index"
          class="dot"
          :class="{
            active: index === currentIndex,
            completed: index < currentIndex
          }"
          @click="$emit('go-to', index)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  word: {
    type: String,
    default: ''
  },
  words: {
    type: Array,
    default: () => []
  },
  currentIndex: {
    type: Number,
    default: -1
  },
  isSpeaking: {
    type: Boolean,
    default: false
  },
  isListening: {
    type: Boolean,
    default: false
  },
  isStarted: {
    type: Boolean,
    default: false
  },
  isCompleted: {
    type: Boolean,
    default: false
  },
  showPinyin: {
    type: Boolean,
    default: false
  },
  pinyin: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '准备听写...'
  }
})

defineEmits(['go-to'])
</script>

<style lang="scss" scoped>
.word-display-container {
  margin: 24px 0;
}

.word-display {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 24px;
  padding: 60px 40px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &.is-speaking {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    .word-text, .word-pinyin {
      color: white;
    }

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: radial-gradient(circle at center, rgba(255,255,255,0.1) 0%, transparent 70%);
      animation: pulse-bg 2s ease-in-out infinite;
    }
  }

  &.is-completed {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);

    .word-text, .word-pinyin {
      color: white;
    }
  }

  &.is-empty {
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  }

  .word-content {
    text-align: center;
    z-index: 1;

    .word-text {
      font-size: 64px;
      font-weight: bold;
      color: #303133;
      line-height: 1.4;
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
    }

    .word-pinyin {
      font-size: 24px;
      color: #909399;
      margin-top: 10px;
    }
  }

  .word-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    color: #909399;

    span {
      font-size: 24px;
    }
  }
}

@keyframes pulse-bg {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.word-fade-enter-active,
.word-fade-leave-active {
  transition: all 0.5s ease;
}

.word-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.word-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.9);
}

.status-indicator {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;

  .status-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    color: #909399;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      transform: scale(1.05);
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
    }

    span {
      font-size: 14px;
    }
  }
}

.word-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;

  .progress-text {
    font-size: 16px;
    color: #606266;
    margin-bottom: 12px;
  }

  .progress-dots {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    max-width: 100%;

    .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: #DCDFE6;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.2);
      }

      &.active {
        background: #409EFF;
        transform: scale(1.3);
        box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
      }

      &.completed {
        background: #67C23A;
      }
    }
  }
}

@media (max-width: 768px) {
  .word-display {
    padding: 40px 20px;
    min-height: 150px;

    .word-content {
      .word-text {
        font-size: 48px;
      }

      .word-pinyin {
        font-size: 18px;
      }
    }

    .word-placeholder {
      span {
        font-size: 18px;
      }
    }
  }

  .status-indicator {
    flex-direction: column;
    align-items: center;
    gap: 10px;

    .status-item {
      width: 100%;
      justify-content: center;
    }
  }
}
</style>