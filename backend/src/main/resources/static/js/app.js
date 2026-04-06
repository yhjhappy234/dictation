/**
 * 主应用入口
 * 初始化页面和事件绑定
 */

(function() {
    'use strict';

    // DOM元素引用
    let elements = {};

    // 当前批次ID
    let currentBatchId = null;

    /**
     * 页面初始化
     */
    function init() {
        // 获取DOM元素
        elements = {
            currentWord: document.getElementById('currentWord'),
            completedCount: document.getElementById('completedCount'),
            remainingCount: document.getElementById('remainingCount'),
            wordDisplay: document.getElementById('wordDisplay'),
            progressBar: document.getElementById('progressBar'),
            progressContainer: document.getElementById('progressContainer'),
            btnRepeat: document.getElementById('btnRepeat'),
            btnPrevious: document.getElementById('btnPrevious'),
            btnNext: document.getElementById('btnNext'),
            btnStart: document.getElementById('btnStart'),
            btnEnd: document.getElementById('btnEnd'),
            btnAddBatch: document.getElementById('btnAddBatch'),
            wordInput: document.getElementById('wordInput')
        };

        // 检查语音兼容性
        const compatibility = SpeechModule.checkCompatibility();
        if (!compatibility.isFullySupported) {
            showWarning('建议使用Chrome浏览器以获得最佳语音交互体验');
        }

        // 初始化听写模块
        DictationModule.init({
            onWordChange: onWordChange,
            onProgressUpdate: onProgressUpdate,
            onComplete: onComplete,
            onError: onError
        });

        // 绑定事件
        bindEvents();
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        // 开始听写按钮
        if (elements.btnStart) {
            elements.btnStart.addEventListener('click', onStartClick);
        }

        // 再次读取按钮
        if (elements.btnRepeat) {
            elements.btnRepeat.addEventListener('click', onRepeatClick);
        }

        // 上一个按钮
        if (elements.btnPrevious) {
            elements.btnPrevious.addEventListener('click', onPreviousClick);
        }

        // 下一个按钮
        if (elements.btnNext) {
            elements.btnNext.addEventListener('click', onNextClick);
        }

        // 结束听写按钮
        if (elements.btnEnd) {
            elements.btnEnd.addEventListener('click', onEndClick);
        }

        // 创建批次按钮
        if (elements.btnAddBatch) {
            elements.btnAddBatch.addEventListener('click', onAddBatchClick);
        }
    }

    /**
     * 开始听写按钮点击
     */
    function onStartClick() {
        if (!currentBatchId) {
            showWarning('请先录入词语并创建批次');
            return;
        }

        // 更新UI状态
        elements.btnStart.style.display = 'none';
        elements.btnEnd.style.display = 'inline-block';
        elements.btnRepeat.disabled = false;
        elements.btnPrevious.disabled = false;
        elements.btnNext.disabled = false;
        elements.progressContainer.style.display = 'block';

        // 开始听写
        DictationModule.start(currentBatchId);
    }

    /**
     * 再次读取按钮点击
     */
    function onRepeatClick() {
        DictationModule.repeat();
    }

    /**
     * 上一个按钮点击
     */
    function onPreviousClick() {
        DictationModule.previous();
    }

    /**
     * 下一个按钮点击
     */
    function onNextClick() {
        DictationModule.next();
    }

    /**
     * 结束听写按钮点击
     */
    function onEndClick() {
        DictationModule.stop();
        resetUI();
    }

    /**
     * 创建批次按钮点击
     */
    async function onAddBatchClick() {
        const wordsText = elements.wordInput.value.trim();
        if (!wordsText) {
            showWarning('请输入要听写的词语');
            return;
        }

        // 按空格分割词语
        const words = wordsText.split(/\s+/).filter(w => w.length > 0);

        if (words.length === 0) {
            showWarning('请输入有效的词语');
            return;
        }

        try {
            elements.btnAddBatch.disabled = true;
            elements.btnAddBatch.innerHTML = '<i class="bi bi-spinner-border me-2"></i>创建中...';

            const result = await API.createBatch(words);
            currentBatchId = result.data.id;

            showSuccess(`成功创建批次，共 ${words.length} 个词语`);

            // 更新统计显示
            elements.remainingCount.textContent = words.length;

            // 启用开始按钮
            elements.btnStart.disabled = false;

        } catch (error) {
            showError('创建批次失败: ' + error.message);
        } finally {
            elements.btnAddBatch.disabled = false;
            elements.btnAddBatch.innerHTML = '<i class="bi bi-plus-circle me-2"></i>创建批次';
        }
    }

    /**
     * 词语变化回调
     */
    function onWordChange(word, index) {
        elements.currentWord.textContent = word;
        elements.wordDisplay.textContent = word;
    }

    /**
     * 进度更新回调
     */
    function onProgressUpdate(progress) {
        elements.completedCount.textContent = progress.completed;
        elements.remainingCount.textContent = progress.remaining;
        elements.progressBar.style.width = progress.progress + '%';
    }

    /**
     * 完成回调
     */
    function onComplete(result) {
        showSuccess(`听写完成！共听写 ${result.totalWords} 个词语，用时 ${result.totalTime} 秒`);
        resetUI();
    }

    /**
     * 错误回调
     */
    function onError(error) {
        showError('听写出错: ' + error.message);
    }

    /**
     * 重置UI状态
     */
    function resetUI() {
        elements.btnStart.style.display = 'inline-block';
        elements.btnEnd.style.display = 'none';
        elements.btnRepeat.disabled = true;
        elements.btnPrevious.disabled = true;
        elements.btnNext.disabled = true;
        elements.progressContainer.style.display = 'none';
        elements.progressBar.style.width = '0%';
        elements.currentWord.textContent = '-';
        elements.completedCount.textContent = '0';
        elements.remainingCount.textContent = '0';
        elements.wordDisplay.textContent = '点击下方按钮开始听写';
    }

    /**
     * 显示成功消息
     */
    function showSuccess(message) {
        showToast(message, 'success');
    }

    /**
     * 显示警告消息
     */
    function showWarning(message) {
        showToast(message, 'warning');
    }

    /**
     * 显示错误消息
     */
    function showError(message) {
        showToast(message, 'error');
    }

    /**
     * 显示Toast消息
     */
    function showToast(message, type) {
        // 使用Bootstrap的Toast组件或简单alert
        alert(message);
    }

    // 页面加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();