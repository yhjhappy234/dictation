import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useSpeech } from './useSpeech'

export function useDictation() {
  // 状态
  const words = ref([])
  const currentIndex = ref(-1)
  const isStarted = ref(false)
  const isPaused = ref(false)
  const isCompleted = ref(false)
  const startTime = ref(null)
  const elapsedTime = ref(0)
  const wordTimings = ref({}) // 每个词语的耗时
  const currentWordStartTime = ref(null)

  // 定时器
  let timer = null

  // 计算属性
  const currentWord = computed(() => {
    if (currentIndex.value >= 0 && currentIndex.value < words.value.length) {
      return words.value[currentIndex.value]
    }
    return null
  })

  const progress = computed(() => {
    if (words.value.length === 0) return 0
    return Math.round(((currentIndex.value + 1) / words.value.length) * 100)
  })

  const remainingCount = computed(() => {
    return Math.max(0, words.value.length - currentIndex.value - 1)
  })

  const completedCount = computed(() => {
    return currentIndex.value + 1
  })

  const canGoPrevious = computed(() => currentIndex.value > 0)
  const canGoNext = computed(() => currentIndex.value < words.value.length - 1)

  // 使用语音功能
  const {
    isSpeaking,
    isListening,
    speak,
    speakWord,
    speakResult,
    startListening,
    stopListening,
    checkCompatibility
  } = useSpeech()

  // 开始计时
  const startTimer = () => {
    if (timer) clearInterval(timer)
    timer = setInterval(() => {
      if (!isPaused.value && startTime.value) {
        elapsedTime.value = Math.floor((Date.now() - startTime.value) / 1000)
      }
    }, 1000)
  }

  // 停止计时
  const stopTimer = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  // 格式化时间
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}分${secs}秒`
  }

  // 设置词语列表
  const setWords = (wordList) => {
    words.value = wordList.filter(w => w.trim())
    currentIndex.value = -1
    isStarted.value = false
    isPaused.value = false
    isCompleted.value = false
    startTime.value = null
    elapsedTime.value = 0
    wordTimings.value = {}
  }

  // 开始听写
  const start = async () => {
    if (words.value.length === 0) {
      ElMessage.warning('请先输入要听写的词语')
      return
    }

    const compatibility = checkCompatibility()
    if (!compatibility.isFullySupported) {
      ElMessage.warning('建议使用Chrome浏览器以获得最佳体验')
    }

    isStarted.value = true
    isPaused.value = false
    currentIndex.value = 0
    startTime.value = Date.now()
    currentWordStartTime.value = Date.now()
    startTimer()

    await speak('听写开始，请认真听题')
    await speakWord(currentWord.value)

    // 启动语音识别
    startListening({
      autoRestart: true,
      onNext: () => next(),
      onRepeat: () => repeat(),
      onPrevious: () => previous()
    })
  }

  // 下一个词语
  const next = async () => {
    if (!canGoNext.value) {
      // 听写完成
      await complete()
      return
    }

    // 记录当前词语耗时
    if (currentWordStartTime.value && currentWord.value) {
      wordTimings.value[currentWord.value] = Math.floor(
        (Date.now() - currentWordStartTime.value) / 1000
      )
    }

    currentIndex.value++
    currentWordStartTime.value = Date.now()
    await speakWord(currentWord.value)
  }

  // 上一个词语
  const previous = async () => {
    if (!canGoPrevious.value) return

    currentIndex.value--
    currentWordStartTime.value = Date.now()
    await speakWord(currentWord.value)
  }

  // 重复当前词语
  const repeat = async () => {
    if (currentWord.value) {
      await speakWord(currentWord.value)
    }
  }

  // 暂停
  const pause = async () => {
    isPaused.value = true
    stopListening()
    await speak('听写已暂停')
  }

  // 继续
  const resume = async () => {
    isPaused.value = false
    await speak('继续听写')
    await speakWord(currentWord.value)
    startListening({
      autoRestart: true,
      onNext: () => next(),
      onRepeat: () => repeat(),
      onPrevious: () => previous()
    })
  }

  // 完成听写
  const complete = async () => {
    stopTimer()
    stopListening()
    isCompleted.value = true
    isPaused.value = false

    // 记录最后一个词语的耗时
    if (currentWordStartTime.value && currentWord.value) {
      wordTimings.value[currentWord.value] = Math.floor(
        (Date.now() - currentWordStartTime.value) / 1000
      )
    }

    await speak('听写结束，辛苦了！')

    // 返回结果数据
    return {
      words: words.value,
      totalTime: elapsedTime.value,
      wordTimings: wordTimings.value,
      completedAt: new Date().toISOString()
    }
  }

  // 重置
  const reset = () => {
    stopTimer()
    stopListening()
    words.value = []
    currentIndex.value = -1
    isStarted.value = false
    isPaused.value = false
    isCompleted.value = false
    startTime.value = null
    elapsedTime.value = 0
    wordTimings.value = {}
    currentWordStartTime.value = null
  }

  // 跳转到指定词语
  const goTo = async (index) => {
    if (index >= 0 && index < words.value.length) {
      currentIndex.value = index
      currentWordStartTime.value = Date.now()
      if (isStarted.value && !isPaused.value) {
        await speakWord(currentWord.value)
      }
    }
  }

  return {
    // 状态
    words,
    currentIndex,
    currentWord,
    isStarted,
    isPaused,
    isCompleted,
    isSpeaking,
    isListening,
    elapsedTime,

    // 计算属性
    progress,
    remainingCount,
    completedCount,
    canGoPrevious,
    canGoNext,

    // 方法
    setWords,
    start,
    next,
    previous,
    repeat,
    pause,
    resume,
    complete,
    reset,
    goTo,
    formatTime,
    speak,
    speakWord,
    speakResult,
    startListening,
    stopListening,
    checkCompatibility
  }
}