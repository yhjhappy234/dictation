<template>
  <div class="home-page">
    <el-row :gutter="24">
      <!-- 左侧：词语输入 -->
      <el-col :xs="24" :lg="10">
        <WordInput
          ref="wordInputRef"
          :disabled="isStarted && !isPaused"
          :button-text="isStarted ? '重新开始' : '开始听写'"
          @submit="handleStart"
        />
      </el-col>

      <!-- 右侧：听写区域 -->
      <el-col :xs="24" :lg="14">
        <!-- 统计面板 -->
        <StatisticsPanel
          :current-word="currentWord"
          :completed-count="completedCount"
          :remaining-count="remainingCount"
          :progress="progress"
          :elapsed-time="elapsedTime"
          :show-progress="isStarted"
          :show-timer="isStarted"
        />

        <!-- 词语显示 -->
        <WordDisplay
          :word="currentWord"
          :words="words"
          :current-index="currentIndex"
          :is-speaking="isSpeaking"
          :is-listening="isListening"
          :is-started="isStarted"
          :is-completed="isCompleted"
          placeholder="点击"开始听写"开始"
          @go-to="handleGoTo"
        />

        <!-- 控制按钮 -->
        <ControlButtons
          :is-started="isStarted"
          :is-paused="isPaused"
          :is-completed="isCompleted"
          :is-listening="isListening"
          :is-speaking="isSpeaking"
          :can-go-previous="canGoPrevious"
          :can-go-next="canGoNext"
          @start="handleStart"
          @pause="handlePause"
          @resume="handleResume"
          @next="handleNext"
          @previous="handlePrevious"
          @repeat="handleRepeat"
          @restart="handleRestart"
          @view-result="handleViewResult"
          @toggle-listen="handleToggleListen"
        />
      </el-col>
    </el-row>

    <!-- 结果弹窗 -->
    <el-dialog
      v-model="showResultDialog"
      title="听写结果"
      width="600px"
      center
      class="result-dialog"
    >
      <div class="result-content">
        <div class="result-summary">
          <el-statistic title="总词语数" :value="resultData.totalWords" />
          <el-statistic title="总耗时" :value="formatTime(resultData.totalTime)" />
        </div>

        <el-divider />

        <div class="result-details">
          <h4>各词语耗时</h4>
          <el-table :data="resultData.wordDetails" style="width: 100%">
            <el-table-column prop="word" label="词语" width="120" />
            <el-table-column prop="time" label="耗时">
              <template #default="{ row }">
                {{ row.time }}秒
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="showResultDialog = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import WordInput from '@/components/WordInput.vue'
import StatisticsPanel from '@/components/StatisticsPanel.vue'
import WordDisplay from '@/components/WordDisplay.vue'
import ControlButtons from '@/components/ControlButtons.vue'
import { useDictation } from '@/composables/useDictation'

// 使用听写组合式函数
const {
  words,
  currentIndex,
  currentWord,
  isStarted,
  isPaused,
  isCompleted,
  isSpeaking,
  isListening,
  elapsedTime,
  progress,
  remainingCount,
  completedCount,
  canGoPrevious,
  canGoNext,
  setWords,
  start,
  next,
  previous,
  repeat,
  pause,
  resume,
  reset,
  goTo,
  formatTime,
  speak,
  startListening,
  stopListening
} = useDictation()

// 组件引用
const wordInputRef = ref(null)

// 结果弹窗
const showResultDialog = ref(false)
const resultData = ref({
  totalWords: 0,
  totalTime: 0,
  wordDetails: []
})

// 开始听写
const handleStart = async (wordList) => {
  if (isStarted.value) {
    // 重新开始
    try {
      await ElMessageBox.confirm(
        '确定要重新开始吗？当前进度将丢失。',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
    reset()
  }

  if (wordList && wordList.length > 0) {
    setWords(wordList)
  }

  if (words.value.length === 0) {
    ElMessage.warning('请先输入要听写的词语')
    return
  }

  await start()
}

// 暂停
const handlePause = async () => {
  await pause()
}

// 继续
const handleResume = async () => {
  await resume()
}

// 下一个
const handleNext = async () => {
  if (isCompleted.value) return

  if (!canGoNext.value) {
    // 完成听写
    await handleComplete()
  } else {
    await next()
  }
}

// 上一个
const handlePrevious = async () => {
  await previous()
}

// 重复
const handleRepeat = async () => {
  await repeat()
}

// 重新开始
const handleRestart = () => {
  reset()
  if (wordInputRef.value) {
    wordInputRef.value.clearInput()
  }
}

// 跳转到指定词语
const handleGoTo = async (index) => {
  if (isStarted.value && !isPaused.value) {
    await goTo(index)
  }
}

// 切换语音识别
const handleToggleListen = () => {
  if (isListening.value) {
    stopListening()
    ElMessage.info('语音识别已关闭')
  } else {
    startListening({
      autoRestart: true,
      onNext: handleNext,
      onRepeat: handleRepeat,
      onPrevious: handlePrevious
    })
    ElMessage.success('语音识别已开启')
  }
}

// 完成听写
const handleComplete = async () => {
  const result = await next() // 这会触发完成逻辑

  // 准备结果数据
  resultData.value = {
    totalWords: words.value.length,
    totalTime: elapsedTime.value,
    wordDetails: words.value.map(word => ({
      word,
      time: Math.floor(Math.random() * 30) + 5 // 模拟数据，实际应从后端获取
    }))
  }

  showResultDialog.value = true
  ElMessage.success('听写完成！')
}

// 查看结果
const handleViewResult = () => {
  showResultDialog.value = true
}
</script>

<style lang="scss" scoped>
.home-page {
  padding: 20px 0;
}

.result-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 30px;
  }
}

.result-content {
  .result-summary {
    display: flex;
    justify-content: space-around;
    text-align: center;

    :deep(.el-statistic__head) {
      font-size: 16px;
      color: #909399;
    }

    :deep(.el-statistic__content) {
      font-size: 32px;
      font-weight: bold;
      color: #409EFF;
    }
  }

  .result-details {
    h4 {
      margin-bottom: 16px;
      color: #303133;
    }
  }
}

@media (max-width: 768px) {
  .home-page {
    :deep(.el-col) {
      margin-bottom: 20px;
    }
  }
}
</style>