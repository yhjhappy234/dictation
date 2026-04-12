/**
 * speech.js - 语音播报和识别模块
 * 使用Web Speech API实现语音合成和语音识别功能
 */

const SpeechModule = (function() {
    // 私有变量
    let recognition = null;
    let isListening = false;
    let timeoutId = null;
    let onCommandCallback = null;
    let onTimeoutCallback = null;

    // 配置
    const config = {
        speakRate: 0.8,          // 播报速度
        timeout: 5000,           // 识别超时时间（毫秒）
        keywords: {
            done: '好了',
            next: '下一个',
            repeat: '重复'
        }
    };

    /**
     * 检测浏览器兼容性
     * @returns {Object} 兼容性检测结果
     */
    function checkCompatibility() {
        const result = {
            speechSynthesis: 'speechSynthesis' in window,
            speechRecognition: false,
            message: ''
        };

        // 检测语音识别API（不同浏览器前缀）
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        result.speechRecognition = !!SpeechRecognition;

        // 生成提示信息
        if (!result.speechSynthesis) {
            result.message = '您的浏览器不支持语音合成功能，请使用Chrome、Edge或Safari浏览器。';
        } else if (!result.speechRecognition) {
            result.message = '您的浏览器不支持语音识别功能，请使用Chrome或Edge浏览器。';
        } else {
            result.message = '浏览器支持所有语音功能。';
        }

        return result;
    }

    /**
     * 播报词语
     * @param {string} word - 要播报的词语
     * @param {Object} options - 可选配置
     * @param {number} options.rate - 播报速度
     * @param {Function} options.onEnd - 播报结束回调
     * @param {Function} options.onStart - 播报开始回调
     * @returns {Promise} 播报完成的Promise
     */
    function speakWord(word, options = {}) {
        return new Promise((resolve, reject) => {
            if (!window.speechSynthesis) {
                reject(new Error('浏览器不支持语音合成'));
                return;
            }

            // 取消之前的播报
            window.speechSynthesis.cancel();

            const utterance = new SpeechSynthesisUtterance(word);
            utterance.lang = 'zh-CN';
            utterance.rate = options.rate || config.speakRate;
            utterance.pitch = 1;
            utterance.volume = 1;

            // 选择中文语音
            const voices = window.speechSynthesis.getVoices();
            const chineseVoice = voices.find(voice => voice.lang.includes('zh'));
            if (chineseVoice) {
                utterance.voice = chineseVoice;
            }

            utterance.onstart = () => {
                if (options.onStart) {
                    options.onStart();
                }
            };

            utterance.onend = () => {
                if (options.onEnd) {
                    options.onEnd();
                }
                resolve();
            };

            utterance.onerror = (event) => {
                reject(new Error(`语音合成错误: ${event.error}`));
            };

            window.speechSynthesis.speak(utterance);
        });
    }

    /**
     * 停止播报
     */
    function stopSpeaking() {
        if (window.speechSynthesis) {
            window.speechSynthesis.cancel();
        }
    }

    /**
     * 初始化语音识别
     */
    function initRecognition() {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

        if (!SpeechRecognition) {
            console.error('浏览器不支持语音识别');
            return false;
        }

        recognition = new SpeechRecognition();
        recognition.continuous = true;
        recognition.interimResults = false;
        recognition.lang = 'zh-CN';

        recognition.onresult = (event) => {
            // 清除超时计时器
            if (timeoutId) {
                clearTimeout(timeoutId);
                timeoutId = null;
            }

            const last = event.results.length - 1;
            const transcript = event.results[last][0].transcript.trim();

            console.log('识别结果:', transcript);

            // 检测关键词
            if (transcript.includes(config.keywords.done)) {
                if (onCommandCallback) {
                    onCommandCallback('done', transcript);
                }
            } else if (transcript.includes(config.keywords.next)) {
                if (onCommandCallback) {
                    onCommandCallback('next', transcript);
                }
            } else if (transcript.includes(config.keywords.repeat)) {
                if (onCommandCallback) {
                    onCommandCallback('repeat', transcript);
                }
            }

            // 重新设置超时
            resetTimeout();
        };

        recognition.onerror = (event) => {
            console.error('语音识别错误:', event.error);

            if (event.error === 'no-speech') {
                // 无语音输入，继续监听
                resetTimeout();
            } else if (event.error === 'audio-capture') {
                console.error('无法捕获音频，请检查麦克风');
            } else if (event.error === 'not-allowed') {
                console.error('麦克风权限被拒绝');
            }
        };

        recognition.onend = () => {
            // 如果还在监听状态，自动重启识别
            if (isListening && recognition) {
                try {
                    recognition.start();
                } catch (e) {
                    console.log('重启识别失败:', e);
                }
            }
        };

        return true;
    }

    /**
     * 重置超时计时器
     */
    function resetTimeout() {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            if (onTimeoutCallback) {
                onTimeoutCallback();
            }
        }, config.timeout);
    }

    /**
     * 开启麦克风识别
     * @param {Object} callbacks - 回调函数对象
     * @param {Function} callbacks.onCommand - 命令识别回调
     * @param {Function} callbacks.onTimeout - 超时回调
     * @param {Function} callbacks.onError - 错误回调
     * @returns {boolean} 是否成功启动
     */
    function startListening(callbacks = {}) {
        // 检测兼容性
        const compat = checkCompatibility();
        if (!compat.speechRecognition) {
            if (callbacks.onError) {
                callbacks.onError(compat.message);
            }
            return false;
        }

        // 初始化识别器
        if (!recognition) {
            if (!initRecognition()) {
                return false;
            }
        }

        // 设置回调
        onCommandCallback = callbacks.onCommand || null;
        onTimeoutCallback = callbacks.onTimeout || null;

        try {
            recognition.start();
            isListening = true;
            resetTimeout();
            console.log('语音识别已启动');
            return true;
        } catch (e) {
            console.error('启动语音识别失败:', e);
            if (callbacks.onError) {
                callbacks.onError('启动语音识别失败，请检查麦克风权限');
            }
            return false;
        }
    }

    /**
     * 停止语音识别
     */
    function stopListening() {
        isListening = false;

        if (timeoutId) {
            clearTimeout(timeoutId);
            timeoutId = null;
        }

        if (recognition) {
            try {
                recognition.stop();
            } catch (e) {
                console.log('停止识别失败:', e);
            }
        }

        console.log('语音识别已停止');
    }

    /**
     * 获取当前监听状态
     * @returns {boolean} 是否正在监听
     */
    function getListeningState() {
        return isListening;
    }

    /**
     * 设置超时时间
     * @param {number} ms - 超时毫秒数
     */
    function setTimeout(ms) {
        config.timeout = ms;
    }

    /**
     * 设置关键词
     * @param {Object} keywords - 关键词对象
     */
    function setKeywords(keywords) {
        Object.assign(config.keywords, keywords);
    }

    /**
     * 播放提示音
     * @param {string} type - 提示类型 ('success', 'error', 'warning')
     */
    function playNotificationSound(type = 'success') {
        // 使用Web Audio API生成简单的提示音
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            switch (type) {
                case 'success':
                    oscillator.frequency.value = 800;
                    break;
                case 'error':
                    oscillator.frequency.value = 300;
                    break;
                case 'warning':
                    oscillator.frequency.value = 500;
                    break;
                default:
                    oscillator.frequency.value = 600;
            }

            oscillator.type = 'sine';
            gainNode.gain.value = 0.3;

            oscillator.start();
            setTimeout(() => oscillator.stop(), 150);
        } catch (e) {
            console.log('播放提示音失败:', e);
        }
    }

    // 公开API
    return {
        checkCompatibility,
        speakWord,
        stopSpeaking,
        startListening,
        stopListening,
        getListeningState,
        setTimeout,
        setKeywords,
        playNotificationSound
    };
})();

// 导出模块（支持ES6模块和全局变量）
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SpeechModule;
} else {
    window.SpeechModule = SpeechModule;
}