/**
 * 听写流程控制模块
 * 管理听写状态和流程
 */

const DictationModule = (function() {
    // 状态变量
    let state = {
        batchId: null,
        words: [],
        currentIndex: -1,
        isRunning: false,
        startTime: null,
        wordStartTime: null,
        repeatCount: 0,
        completedCount: 0
    };

    // 回调函数
    let callbacks = {
        onWordChange: null,
        onProgressUpdate: null,
        onComplete: null,
        onError: null
    };

    /**
     * 初始化听写模块
     * @param {object} options - 配置选项
     */
    function init(options = {}) {
        if (options.onWordChange) callbacks.onWordChange = options.onWordChange;
        if (options.onProgressUpdate) callbacks.onProgressUpdate = options.onProgressUpdate;
        if (options.onComplete) callbacks.onComplete = options.onComplete;
        if (options.onError) callbacks.onError = options.onError;
    }

    /**
     * 开始听写
     * @param {number} batchId - 批次ID
     */
    async function start(batchId) {
        try {
            // 加载批次数据
            const batchData = await API.getBatchDetail(batchId);
            state.batchId = batchId;
            state.words = batchData.words || [];
            state.currentIndex = 0;
            state.isRunning = true;
            state.startTime = new Date();
            state.completedCount = 0;

            // 调用开始听写API
            await API.startDictation(batchId);

            // 播放第一个词语
            playCurrentWord();

            // 更新UI
            updateProgress();

        } catch (error) {
            console.error('开始听写失败:', error);
            if (callbacks.onError) callbacks.onError(error);
        }
    }

    /**
     * 播放当前词语
     */
    function playCurrentWord() {
        if (state.currentIndex < 0 || state.currentIndex >= state.words.length) {
            return;
        }

        const word = state.words[state.currentIndex];
        state.wordStartTime = new Date();
        state.repeatCount = 0;

        // 更新当前词语显示
        if (callbacks.onWordChange) {
            callbacks.onWordChange(word.wordText, state.currentIndex);
        }

        // 语音播报
        SpeechModule.speak(word.wordText, function() {
            // 播报完成后开启语音识别
            startListening();
        });
    }

    /**
     * 开始语音识别监听
     */
    function startListening() {
        SpeechModule.startListening({
            onNext: function() {
                next();
            },
            onRepeat: function() {
                repeat();
            },
            onTimeout: function() {
                // 5秒超时，自动播放下一个
                const nextWord = state.words[state.currentIndex + 1];
                if (nextWord) {
                    SpeechModule.speak('下一个听写词语：' + nextWord.wordText, function() {
                        next();
                    });
                }
            },
            onResult: function(transcript) {
                console.log('识别到:', transcript);
            }
        });
    }

    /**
     * 下一个词语
     */
    async function next() {
        if (!state.isRunning) return;

        // 记录当前词语完成
        await recordCurrentWord();

        state.currentIndex++;

        if (state.currentIndex >= state.words.length) {
            // 所有词语完成
            await complete();
        } else {
            // 播放下一个词语
            playCurrentWord();
            updateProgress();
        }
    }

    /**
     * 上一个词语
     */
    function previous() {
        if (!state.isRunning) return;
        if (state.currentIndex <= 0) return;

        state.currentIndex--;
        playCurrentWord();
        updateProgress();
    }

    /**
     * 重复播放当前词语
     */
    function repeat() {
        if (!state.isRunning) return;

        state.repeatCount++;

        const word = state.words[state.currentIndex];
        SpeechModule.speak(word.wordText, function() {
            startListening();
        });
    }

    /**
     * 记录当前词语听写结果
     */
    async function recordCurrentWord() {
        if (state.currentIndex < 0 || state.currentIndex >= state.words.length) {
            return;
        }

        const word = state.words[state.currentIndex];
        const duration = Math.floor((new Date() - state.wordStartTime) / 1000);

        try {
            await API.completeWord(word.id, duration, state.repeatCount);
            state.completedCount++;
        } catch (error) {
            console.error('记录听写结果失败:', error);
        }
    }

    /**
     * 完成听写
     */
    async function complete() {
        state.isRunning = false;

        SpeechModule.speak('所有听写均已完成！', function() {
            if (callbacks.onComplete) {
                callbacks.onComplete({
                    batchId: state.batchId,
                    totalWords: state.words.length,
                    completedWords: state.completedCount,
                    totalTime: Math.floor((new Date() - state.startTime) / 1000)
                });
            }
        });

        // 调用结束听写API
        try {
            await API.endDictation(state.batchId);
        } catch (error) {
            console.error('结束听写失败:', error);
        }
    }

    /**
     * 更新进度
     */
    function updateProgress() {
        if (callbacks.onProgressUpdate) {
            callbacks.onProgressUpdate({
                current: state.currentIndex + 1,
                total: state.words.length,
                completed: state.completedCount,
                remaining: state.words.length - state.currentIndex - 1,
                progress: ((state.currentIndex + 1) / state.words.length * 100).toFixed(1)
            });
        }
    }

    /**
     * 获取当前状态
     */
    function getState() {
        return { ...state };
    }

    /**
     * 是否正在运行
     */
    function isRunning() {
        return state.isRunning;
    }

    /**
     * 停止听写
     */
    function stop() {
        state.isRunning = false;
        SpeechModule.stopListening();
    }

    /**
     * 重置状态
     */
    function reset() {
        state = {
            batchId: null,
            words: [],
            currentIndex: -1,
            isRunning: false,
            startTime: null,
            wordStartTime: null,
            repeatCount: 0,
            completedCount: 0
        };
    }

    return {
        init: init,
        start: start,
        next: next,
        previous: previous,
        repeat: repeat,
        stop: stop,
        reset: reset,
        getState: getState,
        isRunning: isRunning
    };
})();

// 导出模块
window.DictationModule = DictationModule;