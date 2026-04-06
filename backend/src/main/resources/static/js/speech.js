/**
 * 语音播报和识别模块
 * 使用Web Speech API实现语音交互
 */

const SpeechModule = (function() {
    // 检查浏览器兼容性
    const isSpeechSynthesisSupported = 'speechSynthesis' in window;
    const isSpeechRecognitionSupported = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window;

    let recognition = null;
    let isListening = false;
    let timeoutId = null;

    /**
     * 语音播报
     * @param {string} text - 要播报的文本
     * @param {function} onEnd - 播报结束回调
     */
    function speak(text, onEnd) {
        if (!isSpeechSynthesisSupported) {
            console.warn('当前浏览器不支持语音播报');
            alert('当前浏览器不支持语音播报，建议使用Chrome浏览器');
            if (onEnd) onEnd();
            return;
        }

        // 停止之前的播报
        speechSynthesis.cancel();

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'zh-CN';
        utterance.rate = 0.8; // 慢速播放，适合小学生
        utterance.pitch = 1;
        utterance.volume = 1;

        utterance.onend = function() {
            if (onEnd) onEnd();
        };

        utterance.onerror = function(e) {
            console.error('语音播报错误:', e);
            if (onEnd) onEnd();
        };

        speechSynthesis.speak(utterance);
    }

    /**
     * 开始语音识别监听
     * @param {object} callbacks - 回调函数对象
     * @param {function} callbacks.onNext - 识别到"下一个"时回调
     * @param {function} callbacks.onRepeat - 识别到"重复"时回调
     * @param {function} callbacks.onTimeout - 超时回调
     * @param {function} callbacks.onResult - 识别结果回调
     */
    function startListening(callbacks) {
        if (!isSpeechRecognitionSupported) {
            console.warn('当前浏览器不支持语音识别');
            // 如果不支持语音识别，直接启动超时机制
            startTimeout(callbacks);
            return;
        }

        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        recognition = new SpeechRecognition();
        recognition.continuous = true;
        recognition.interimResults = true;
        recognition.lang = 'zh-CN';

        recognition.onresult = function(event) {
            const result = event.results[event.results.length - 1];
            const transcript = result[0].transcript.toLowerCase();

            console.log('识别结果:', transcript);

            if (callbacks.onResult) {
                callbacks.onResult(transcript);
            }

            // 检测关键词
            if (result.isFinal) {
                if (transcript.includes('好了') || transcript.includes('下一个') || transcript.includes('下一题')) {
                    stopListening();
                    clearTimeout(timeoutId);
                    if (callbacks.onNext) callbacks.onNext();
                } else if (transcript.includes('重复') || transcript.includes('再说一遍') || transcript.includes('再读一遍')) {
                    stopListening();
                    clearTimeout(timeoutId);
                    if (callbacks.onRepeat) callbacks.onRepeat();
                }
            }
        };

        recognition.onerror = function(e) {
            console.error('语音识别错误:', e);
            if (e.error === 'no-speech') {
                // 无语音输入，继续监听
            }
        };

        recognition.onend = function() {
            isListening = false;
        };

        recognition.start();
        isListening = true;

        // 启动5秒超时
        startTimeout(callbacks);
    }

    /**
     * 启动超时机制
     */
    function startTimeout(callbacks) {
        timeoutId = setTimeout(function() {
            if (isListening) {
                stopListening();
            }
            if (callbacks.onTimeout) {
                callbacks.onTimeout();
            }
        }, 5000);
    }

    /**
     * 停止语音识别
     */
    function stopListening() {
        if (recognition && isListening) {
            recognition.stop();
            isListening = false;
        }
        if (timeoutId) {
            clearTimeout(timeoutId);
            timeoutId = null;
        }
    }

    /**
     * 检查浏览器兼容性
     */
    function checkCompatibility() {
        return {
            speechSynthesis: isSpeechSynthesisSupported,
            speechRecognition: isSpeechRecognitionSupported,
            isFullySupported: isSpeechSynthesisSupported && isSpeechRecognitionSupported
        };
    }

    return {
        speak: speak,
        startListening: startListening,
        stopListening: stopListening,
        checkCompatibility: checkCompatibility,
        isSupported: isSpeechSynthesisSupported && isSpeechRecognitionSupported
    };
})();

// 导出模块
window.SpeechModule = SpeechModule;