/**
 * dictation.js - 听写流程控制模块
 * 管理听写的整个生命周期，包括词语索引、状态管理等
 */

const DictationController = (function() {
    // 状态变量
    let state = {
        currentBatch: null,          // 当前批次信息
        words: [],                   // 词语列表
        currentIndex: -1,            // 当前词语索引
        isRunning: false,            // 是否正在听写
        isPaused: false,             // 是否暂停
        startTime: null,             // 开始时间
        completedCount: 0,           // 已完成数量
        wordStartTime: null,         // 当前词语开始时间
        listeningMode: false         // 是否开启语音识别模式
    };

    // 回调函数
    let callbacks = {
        onWordChange: null,          // 词语变化回调
        onProgress: null,            // 进度更新回调
        onComplete: null,            // 完成回调
        onError: null,               // 错误回调
        onStatusChange: null         // 状态变化回调
    };

    // 配置
    const config = {
        speakInterval: 500,          // 重复播报间隔（毫秒）
        autoSpeakOnNext: true        // 切换词语时是否自动播报
    };

    /**
     * 初始化听写控制器
     * @param {Object} batchData - 批次数据
     * @param {Object} options - 配置选项
     * @returns {boolean} 是否初始化成功
     */
    function init(batchData, options = {}) {
        if (!batchData || !batchData.words || batchData.words.length === 0) {
            console.error('批次数据无效');
            return false;
        }

        // 重置状态
        state = {
            currentBatch: batchData.batch || batchData,
            words: batchData.words || [],
            currentIndex: -1,
            isRunning: false,
            isPaused: false,
            startTime: null,
            completedCount: 0,
            wordStartTime: null,
            listeningMode: options.listeningMode || false
        };

        // 合并配置
        Object.assign(config, options);

        // 触发状态变化回调
        triggerStatusChange('initialized');

        console.log('听写控制器初始化完成，共', state.words.length, '个词语');
        return true;
    }

    /**
     * 开始听写
     * @returns {Promise} 开始操作的Promise
     */
    async function start() {
        if (state.words.length === 0) {
            triggerError('没有可听写的词语');
            return false;
        }

        state.isRunning = true;
        state.isPaused = false;
        state.startTime = new Date();
        state.currentIndex = 0;

        triggerStatusChange('started');

        // 播报第一个词语
        await speakCurrentWord();

        // 开启语音识别
        if (state.listeningMode && SpeechModule) {
            startVoiceRecognition();
        }

        // 开始计时
        state.wordStartTime = new Date();

        return true;
    }

    /**
     * 暂停听写
     */
    function pause() {
        if (!state.isRunning) return;

        state.isPaused = true;
        SpeechModule.stopSpeaking();
        SpeechModule.stopListening();

        triggerStatusChange('paused');
    }

    /**
     * 继续听写
     */
    async function resume() {
        if (!state.isPaused) return;

        state.isPaused = false;
        triggerStatusChange('resumed');

        // 继续播报当前词语
        if (state.currentIndex >= 0) {
            await speakCurrentWord();
        }

        // 重启语音识别
        if (state.listeningMode && SpeechModule) {
            startVoiceRecognition();
        }
    }

    /**
     * 切换暂停/继续状态
     */
    async function togglePause() {
        if (state.isPaused) {
            await resume();
        } else {
            pause();
        }
    }

    /**
     * 下一个词语
     * @returns {Promise} 切换操作的Promise
     */
    async function nextWord() {
        if (!state.isRunning) return false;

        // 记录当前词语完成
        if (state.currentIndex >= 0) {
            await completeCurrentWord();
        }

        // 检查是否还有下一个
        if (state.currentIndex >= state.words.length - 1) {
            await finishDictation();
            return false;
        }

        state.currentIndex++;
        state.wordStartTime = new Date();

        // 触发词语变化回调
        triggerWordChange();

        // 自动播报
        if (config.autoSpeakOnNext) {
            await speakCurrentWord();
        }

        return true;
    }

    /**
     * 上一个词语
     * @returns {Promise} 切换操作的Promise
     */
    async function previousWord() {
        if (!state.isRunning || state.currentIndex <= 0) return false;

        state.currentIndex--;
        state.wordStartTime = new Date();

        // 触发词语变化回调
        triggerWordChange();

        // 自动播报
        if (config.autoSpeakOnNext) {
            await speakCurrentWord();
        }

        return true;
    }

    /**
     * 重复播放当前词语
     * @param {number} times - 重复次数
     */
    async function repeatCurrent(times = 1) {
        if (state.currentIndex < 0) return;

        for (let i = 0; i < times; i++) {
            await speakCurrentWord();
            if (i < times - 1) {
                await delay(config.speakInterval);
            }
        }
    }

    /**
     * 播报当前词语
     */
    async function speakCurrentWord() {
        if (state.currentIndex < 0 || state.currentIndex >= state.words.length) {
            return;
        }

        const word = getCurrentWord();
        if (word && SpeechModule) {
            await SpeechModule.speakWord(word.word || word);
        }
    }

    /**
     * 完成当前词语
     */
    async function completeCurrentWord() {
        const duration = calculateWordDuration();
        const word = getCurrentWord();

        state.completedCount++;

        // 触发进度更新回调
        triggerProgress(duration);

        // 调用API记录完成（如果API可用）
        if (window.API && word && word.id) {
            try {
                await API.completeWord(word.id, duration);
            } catch (e) {
                console.error('记录词语完成失败:', e);
            }
        }
    }

    /**
     * 结束听写
     */
    async function finishDictation() {
        state.isRunning = false;
        SpeechModule.stopSpeaking();
        SpeechModule.stopListening();

        const totalDuration = calculateTotalDuration();

        // 触发完成回调
        if (callbacks.onComplete) {
            callbacks.onComplete({
                totalWords: state.words.length,
                completedWords: state.completedCount,
                duration: totalDuration,
                batch: state.currentBatch
            });
        }

        // 调用API结束听写
        if (window.API && state.currentBatch && state.currentBatch.id) {
            try {
                await API.endDictation(state.currentBatch.id);
            } catch (e) {
                console.error('结束听写失败:', e);
            }
        }

        triggerStatusChange('completed');
    }

    /**
     * 开始语音识别
     */
    function startVoiceRecognition() {
        if (!SpeechModule) return;

        SpeechModule.startListening({
            onCommand: (command, transcript) => {
                handleVoiceCommand(command);
            },
            onTimeout: () => {
                // 超时提示
                SpeechModule.playNotificationSound('warning');
                console.log('语音识别超时，请继续说');
            },
            onError: (error) => {
                triggerError(error);
            }
        });
    }

    /**
     * 处理语音命令
     * @param {string} command - 命令类型
     */
    function handleVoiceCommand(command) {
        switch (command) {
            case 'done':
            case 'next':
                nextWord();
                break;
            case 'repeat':
                repeatCurrent(2);
                break;
            default:
                console.log('未知命令:', command);
        }
    }

    /**
     * 获取当前词语
     * @returns {Object|null} 当前词语对象
     */
    function getCurrentWord() {
        if (state.currentIndex < 0 || state.currentIndex >= state.words.length) {
            return null;
        }
        return state.words[state.currentIndex];
    }

    /**
     * 获取当前进度信息
     * @returns {Object} 进度信息
     */
    function getProgress() {
        return {
            current: state.currentIndex + 1,
            total: state.words.length,
            remaining: state.words.length - state.currentIndex - 1,
            completed: state.completedCount,
            percentage: Math.round((state.currentIndex + 1) / state.words.length * 100)
        };
    }

    /**
     * 获取当前状态
     * @returns {Object} 状态对象
     */
    function getState() {
        return { ...state };
    }

    /**
     * 计算当前词语用时
     * @returns {number} 秒数
     */
    function calculateWordDuration() {
        if (!state.wordStartTime) return 0;
        return Math.round((new Date() - state.wordStartTime) / 1000);
    }

    /**
     * 计算总用时
     * @returns {number} 秒数
     */
    function calculateTotalDuration() {
        if (!state.startTime) return 0;
        return Math.round((new Date() - state.startTime) / 1000);
    }

    /**
     * 设置回调函数
     * @param {string} event - 事件名称
     * @param {Function} callback - 回调函数
     */
    function on(event, callback) {
        if (callbacks.hasOwnProperty(event)) {
            callbacks[event] = callback;
        }
    }

    /**
     * 触发词语变化回调
     */
    function triggerWordChange() {
        if (callbacks.onWordChange) {
            callbacks.onWordChange(getCurrentWord(), state.currentIndex);
        }
    }

    /**
     * 触发进度更新回调
     */
    function triggerProgress(duration) {
        if (callbacks.onProgress) {
            callbacks.onProgress(getProgress(), duration);
        }
    }

    /**
     * 触发错误回调
     */
    function triggerError(message) {
        if (callbacks.onError) {
            callbacks.onError(message);
        }
    }

    /**
     * 触发状态变化回调
     */
    function triggerStatusChange(status) {
        if (callbacks.onStatusChange) {
            callbacks.onStatusChange(status, state);
        }
    }

    /**
     * 延迟函数
     * @param {number} ms - 毫秒数
     * @returns {Promise}
     */
    function delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * 重置控制器
     */
    function reset() {
        SpeechModule.stopSpeaking();
        SpeechModule.stopListening();

        state = {
            currentBatch: null,
            words: [],
            currentIndex: -1,
            isRunning: false,
            isPaused: false,
            startTime: null,
            completedCount: 0,
            wordStartTime: null,
            listeningMode: false
        };

        triggerStatusChange('reset');
    }

    /**
     * 设置语音识别模式
     * @param {boolean} enabled - 是否启用
     */
    function setListeningMode(enabled) {
        state.listeningMode = enabled;

        if (state.isRunning && !state.isPaused) {
            if (enabled) {
                startVoiceRecognition();
            } else {
                SpeechModule.stopListening();
            }
        }
    }

    // 公开API
    return {
        init,
        start,
        pause,
        resume,
        togglePause,
        nextWord,
        previousWord,
        repeatCurrent,
        getCurrentWord,
        getProgress,
        getState,
        on,
        reset,
        setListeningMode,
        finishDictation
    };
})();

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DictationController;
} else {
    window.DictationController = DictationController;
}