import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

export function useSpeech() {
  const isSpeaking = ref(false)
  const isListening = ref(false)
  const recognition = ref(null)
  const lastRecognizedText = ref('')

  // 浏览器兼容性检查
  const checkCompatibility = () => {
    const hasSpeechSynthesis = 'speechSynthesis' in window
    const hasSpeechRecognition = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window

    return {
      canSpeak: hasSpeechSynthesis,
      canListen: hasSpeechRecognition,
      isFullySupported: hasSpeechSynthesis && hasSpeechRecognition
    }
  }

  // 语音播报
  const speak = (text, options = {}) => {
    return new Promise((resolve, reject) => {
      if (!('speechSynthesis' in window)) {
        ElMessage.error('您的浏览器不支持语音播报功能')
        reject(new Error('Speech synthesis not supported'))
        return
      }

      // 取消之前的语音
      window.speechSynthesis.cancel()

      const utterance = new SpeechSynthesisUtterance(text)
      utterance.lang = options.lang || 'zh-CN'
      utterance.rate = options.rate || 0.8 // 慢速，适合小学生
      utterance.pitch = options.pitch || 1
      utterance.volume = options.volume || 1

      utterance.onstart = () => {
        isSpeaking.value = true
      }

      utterance.onend = () => {
        isSpeaking.value = false
        resolve()
      }

      utterance.onerror = (event) => {
        isSpeaking.value = false
        console.error('Speech error:', event.error)
        reject(event)
      }

      window.speechSynthesis.speak(utterance)
    })
  }

  // 播报词语（带提示）
  const speakWord = async (word, withHint = true) => {
    const text = withHint ? `请听写：${word}` : word
    await speak(text, { rate: 0.7 })
  }

  // 播报结果
  const speakResult = async (isCorrect, word) => {
    if (isCorrect) {
      await speak('回答正确！', { rate: 1 })
    } else {
      await speak(`回答错误，正确答案是：${word}`, { rate: 0.9 })
    }
  }

  // 开始语音识别
  const startListening = (callbacks = {}) => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition

    if (!SpeechRecognition) {
      ElMessage.warning('语音识别功能需要使用Chrome浏览器')
      if (callbacks.onError) {
        callbacks.onError(new Error('Speech recognition not supported'))
      }
      return
    }

    if (recognition.value) {
      recognition.value.stop()
    }

    recognition.value = new SpeechRecognition()
    recognition.value.continuous = true
    recognition.value.interimResults = true
    recognition.value.lang = 'zh-CN'

    recognition.value.onstart = () => {
      isListening.value = true
      if (callbacks.onStart) callbacks.onStart()
    }

    recognition.value.onresult = (event) => {
      const result = event.results[event.results.length - 1]
      const transcript = result[0].transcript.trim()
      lastRecognizedText.value = transcript

      if (callbacks.onResult) {
        callbacks.onResult(transcript, result.isFinal)
      }

      // 识别特定命令
      if (result.isFinal) {
        if (transcript.includes('好了') || transcript.includes('下一个') || transcript.includes('继续')) {
          if (callbacks.onNext) callbacks.onNext()
        } else if (transcript.includes('重复') || transcript.includes('再说一遍') || transcript.includes('重读')) {
          if (callbacks.onRepeat) callbacks.onRepeat()
        } else if (transcript.includes('上一个') || transcript.includes('退回')) {
          if (callbacks.onPrevious) callbacks.onPrevious()
        }
      }
    }

    recognition.value.onerror = (event) => {
      console.error('Recognition error:', event.error)
      isListening.value = false
      if (callbacks.onError) {
        callbacks.onError(event)
      }

      // 自动重连（除了用户主动停止的情况）
      if (event.error !== 'aborted' && event.error !== 'no-speech') {
        setTimeout(() => {
          if (!isListening.value && callbacks.autoRestart) {
            startListening(callbacks)
          }
        }, 1000)
      }
    }

    recognition.value.onend = () => {
      isListening.value = false
      if (callbacks.onEnd) callbacks.onEnd()

      // 自动重启
      if (callbacks.autoRestart && !callbacks.manualStop) {
        setTimeout(() => {
          startListening(callbacks)
        }, 100)
      }
    }

    try {
      recognition.value.start()
    } catch (error) {
      console.error('Failed to start recognition:', error)
    }
  }

  // 停止语音识别
  const stopListening = () => {
    if (recognition.value) {
      recognition.value.stop()
      recognition.value = null
    }
    isListening.value = false
  }

  // 组件卸载时清理
  onUnmounted(() => {
    stopListening()
    if (window.speechSynthesis) {
      window.speechSynthesis.cancel()
    }
  })

  return {
    isSpeaking,
    isListening,
    lastRecognizedText,
    checkCompatibility,
    speak,
    speakWord,
    speakResult,
    startListening,
    stopListening
  }
}