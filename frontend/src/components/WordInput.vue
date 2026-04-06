<template>
  <el-card class="word-input-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <el-icon :size="24" color="#409EFF"><Edit /></el-icon>
        <span>词语录入</span>
      </div>
    </template>

    <div class="input-section">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="6"
        placeholder="请输入要听写的词语，用空格分隔&#10;例如：苹果 香蕉 橘子 葡萄 西瓜"
        class="word-textarea"
        :disabled="disabled"
      />

      <div class="input-actions">
        <div class="word-count">
          <el-tag type="info" size="large">
            已输入 {{ wordCount }} 个词语
          </el-tag>
        </div>

        <div class="action-buttons">
          <el-button
            type="info"
            plain
            @click="clearInput"
            :disabled="!inputText.trim() || disabled"
          >
            <el-icon><Delete /></el-icon>
            清空
          </el-button>

          <el-button
            type="primary"
            size="large"
            @click="handleSubmit"
            :disabled="!inputText.trim() || disabled"
            class="start-button"
          >
            <el-icon><VideoPlay /></el-icon>
            {{ buttonText }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 词语预览 -->
    <transition name="slide">
      <div v-if="words.length > 0" class="words-preview">
        <div class="preview-title">
          <el-icon><List /></el-icon>
          <span>词语预览</span>
        </div>
        <div class="preview-list">
          <el-tag
            v-for="(word, index) in words"
            :key="index"
            class="word-tag"
            :type="getTagType(index)"
            closable
            @close="removeWord(index)"
          >
            {{ word }}
          </el-tag>
        </div>
      </div>
    </transition>
  </el-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '开始听写'
  }
})

const emit = defineEmits(['submit'])

const inputText = ref('')
const words = ref([])

// 计算词语数量
const wordCount = computed(() => {
  return words.value.length
})

// 监听输入变化
watch(inputText, (newVal) => {
  const trimmed = newVal.trim()
  if (trimmed) {
    words.value = trimmed.split(/\s+/).filter(w => w.trim())
  } else {
    words.value = []
  }
})

// 获取标签类型（用于彩色显示）
const getTagType = (index) => {
  const types = ['', 'success', 'warning', 'danger', 'info']
  return types[index % types.length]
}

// 移除单个词语
const removeWord = (index) => {
  words.value.splice(index, 1)
  inputText.value = words.value.join(' ')
}

// 清空输入
const clearInput = () => {
  inputText.value = ''
  words.value = []
}

// 提交
const handleSubmit = () => {
  if (words.value.length === 0) {
    ElMessage.warning('请输入要听写的词语')
    return
  }

  emit('submit', [...words.value])
}

// 暴露方法给父组件
defineExpose({
  clearInput,
  setWords: (wordList) => {
    words.value = wordList
    inputText.value = wordList.join(' ')
  }
})
</script>

<style lang="scss" scoped>
.word-input-card {
  border-radius: 20px;
  overflow: hidden;

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: bold;
    color: #303133;
  }
}

.word-textarea {
  :deep(.el-textarea__inner) {
    font-size: 18px;
    line-height: 1.8;
    border-radius: 12px;
    border: 2px solid #DCDFE6;
    transition: all 0.3s;

    &:focus {
      border-color: #409EFF;
      box-shadow: 0 0 10px rgba(64, 158, 255, 0.2);
    }
  }
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  flex-wrap: wrap;
  gap: 15px;

  .word-count {
    .el-tag {
      font-size: 16px;
      padding: 8px 16px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;

    .start-button {
      padding: 12px 30px;
      font-size: 18px;
      font-weight: 500;
    }
  }
}

.words-preview {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px dashed #EBEEF5;

  .preview-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 500;
    color: #606266;
  }

  .preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .word-tag {
      font-size: 16px;
      padding: 8px 16px;
      cursor: default;
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .input-actions {
    flex-direction: column;
    align-items: stretch;

    .word-count {
      text-align: center;
    }

    .action-buttons {
      justify-content: center;
    }
  }
}
</style>