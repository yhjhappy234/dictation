<template>
  <div class="control-buttons">
    <!-- 开始/重新开始按钮 -->
    <el-button
      v-if="!isStarted"
      type="primary"
      size="large"
      class="control-btn start-btn"
      :disabled="disabled"
      @click="$emit('start')"
    >
      <el-icon><VideoPlay /></el-icon>
      <span>开始听写</span>
    </el-button>

    <template v-else>
      <!-- 主要控制按钮组 -->
      <div class="main-controls">
        <!-- 暂停/继续 -->
        <el-button
          v-if="!isCompleted"
          :type="isPaused ? 'success' : 'warning'"
          size="large"
          class="control-btn"
          @click="$emit(isPaused ? 'resume' : 'pause')"
        >
          <el-icon>
            <VideoPlay v-if="isPaused" />
            <VideoPause v-else />
          </el-icon>
          <span>{{ isPaused ? '继续听写' : '暂停' }}</span>
        </el-button>

        <!-- 重复播放 -->
        <el-button
          type="info"
          size="large"
          class="control-btn"
          :disabled="isPaused || isCompleted"
          @click="$emit('repeat')"
        >
          <el-icon><RefreshRight /></el-icon>
          <span>再次朗读</span>
        </el-button>
      </div>

      <!-- 导航按钮组 -->
      <div class="navigation-controls" v-if="!isCompleted">
        <el-button
          type="primary"
          size="large"
          class="control-btn nav-btn"
          :disabled="!canGoPrevious || isPaused"
          @click="$emit('previous')"
        >
          <el-icon><ArrowLeft /></el-icon>
          <span>上一个</span>
        </el-button>

        <el-button
          type="primary"
          size="large"
          class="control-btn nav-btn"
          :disabled="isPaused"
          @click="$emit('next')"
        >
          <span>{{ canGoNext ? '下一个' : '完成听写' }}</span>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 完成状态 -->
      <div v-if="isCompleted" class="completed-actions">
        <el-button
          type="success"
          size="large"
          class="control-btn"
          @click="$emit('view-result')"
        >
          <el-icon><Document /></el-icon>
          <span>查看结果</span>
        </el-button>

        <el-button
          type="primary"
          size="large"
          class="control-btn"
          @click="$emit('restart')"
        >
          <el-icon><Refresh /></el-icon>
          <span>重新开始</span>
        </el-button>
      </div>
    </template>

    <!-- 语音控制 -->
    <div class="voice-controls" v-if="isStarted && !isCompleted">
      <el-button
        :type="isListening ? 'danger' : 'default'"
        size="default"
        class="voice-btn"
        @click="$emit('toggle-listen')"
      >
        <el-icon>
          <Mic v-if="!isListening" />
          <Mute v-else />
        </el-icon>
        <span>{{ isListening ? '关闭语音识别' : '开启语音识别' }}</span>
      </el-button>
    </div>

    <!-- 语音识别提示 -->
    <div class="voice-tip" v-if="showVoiceTip">
      <el-icon><InfoFilled /></el-icon>
      <span>说出"下一个"、"重复"等指令可控制听写流程</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isStarted: {
    type: Boolean,
    default: false
  },
  isPaused: {
    type: Boolean,
    default: false
  },
  isCompleted: {
    type: Boolean,
    default: false
  },
  isListening: {
    type: Boolean,
    default: false
  },
  isSpeaking: {
    type: Boolean,
    default: false
  },
  canGoPrevious: {
    type: Boolean,
    default: false
  },
  canGoNext: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showVoiceTip: {
    type: Boolean,
    default: true
  }
})

defineEmits([
  'start',
  'pause',
  'resume',
  'next',
  'previous',
  'repeat',
  'restart',
  'view-result',
  'toggle-listen'
])
</script>

<style lang="scss" scoped>
.control-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.control-btn {
  min-width: 140px;
  height: 56px;
  font-size: 18px;
  font-weight: 500;
  border-radius: 16px;
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  }

  .el-icon {
    margin-right: 8px;
    font-size: 20px;
  }
}

.start-btn {
  min-width: 200px;
  height: 64px;
  font-size: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
  }
}

.main-controls {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.navigation-controls {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;

  .nav-btn {
    min-width: 120px;
  }
}

.completed-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.voice-controls {
  margin-top: 10px;

  .voice-btn {
    min-width: auto;
    height: 44px;
    font-size: 14px;
  }
}

.voice-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #ECF5FF;
  border-radius: 10px;
  font-size: 14px;
  color: #409EFF;

  .el-icon {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .control-buttons {
    padding: 16px;
  }

  .control-btn {
    min-width: 100px;
    height: 48px;
    font-size: 16px;
  }

  .start-btn {
    min-width: 160px;
    height: 56px;
    font-size: 18px;
  }

  .main-controls, .navigation-controls, .completed-actions {
    width: 100%;

    .control-btn {
      flex: 1;
    }
  }
}
</style>