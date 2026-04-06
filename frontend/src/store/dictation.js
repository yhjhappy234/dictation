import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useDictationStore = defineStore('dictation', () => {
  // 用户偏好设置
  const preferences = ref({
    speechRate: 0.8,
    autoListen: true,
    showPinyin: false,
    voiceVolume: 1
  })

  // 当前批次信息
  const currentBatch = ref(null)

  // 听写状态
  const isDictating = ref(false)
  const currentWordIndex = ref(-1)
  const dictationWords = ref([])
  const wordResults = ref([])
  const startTime = ref(null)
  const elapsedTime = ref(0)

  // 计算属性
  const currentWord = computed(() => {
    if (currentWordIndex.value >= 0 && currentWordIndex.value < dictationWords.value.length) {
      return dictationWords.value[currentWordIndex.value]
    }
    return null
  })

  const progress = computed(() => {
    if (dictationWords.value.length === 0) return 0
    return Math.round(((currentWordIndex.value + 1) / dictationWords.value.length) * 100)
  })

  // 设置词语
  const setWords = (words) => {
    dictationWords.value = words.filter(w => w.trim())
    currentWordIndex.value = -1
    wordResults.value = []
  }

  // 开始听写
  const startDictation = () => {
    if (dictationWords.value.length === 0) return false
    currentWordIndex.value = 0
    isDictating.value = true
    startTime.value = Date.now()
    return true
  }

  // 下一个词语
  const nextWord = () => {
    if (currentWordIndex.value < dictationWords.value.length - 1) {
      currentWordIndex.value++
      return true
    }
    return false
  }

  // 上一个词语
  const previousWord = () => {
    if (currentWordIndex.value > 0) {
      currentWordIndex.value--
      return true
    }
    return false
  }

  // 结束听写
  const endDictation = () => {
    isDictating.value = false
    elapsedTime.value = startTime.value ? Math.floor((Date.now() - startTime.value) / 1000) : 0
    return {
      words: dictationWords.value,
      results: wordResults.value,
      totalTime: elapsedTime.value
    }
  }

  // 重置
  const reset = () => {
    currentWordIndex.value = -1
    isDictating.value = false
    dictationWords.value = []
    wordResults.value = []
    startTime.value = null
    elapsedTime.value = 0
    currentBatch.value = null
  }

  // 更新偏好设置
  const updatePreferences = (newPrefs) => {
    preferences.value = { ...preferences.value, ...newPrefs }
    // 保存到本地存储
    localStorage.setItem('dictation-preferences', JSON.stringify(preferences.value))
  }

  // 从本地存储加载偏好设置
  const loadPreferences = () => {
    const saved = localStorage.getItem('dictation-preferences')
    if (saved) {
      try {
        preferences.value = JSON.parse(saved)
      } catch (e) {
        console.error('Failed to load preferences:', e)
      }
    }
  }

  // 设置当前批次
  const setCurrentBatch = (batch) => {
    currentBatch.value = batch
  }

  // 记录词语结果
  const recordWordResult = (word, result) => {
    wordResults.value.push({
      word,
      ...result,
      timestamp: Date.now()
    })
  }

  return {
    // 状态
    preferences,
    currentBatch,
    isDictating,
    currentWordIndex,
    dictationWords,
    wordResults,
    startTime,
    elapsedTime,

    // 计算属性
    currentWord,
    progress,

    // 方法
    setWords,
    startDictation,
    nextWord,
    previousWord,
    endDictation,
    reset,
    updatePreferences,
    loadPreferences,
    setCurrentBatch,
    recordWordResult
  }
})