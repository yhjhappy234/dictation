/**
 * app.js - 主应用入口模块
 * 负责页面初始化、事件绑定和全局状态管理
 */

const App = (function() {
    // 全局状态
    const globalState = {
        currentPage: '',           // 当前页面
        user: null,                // 用户信息
        settings: {                // 用户设置
            speechRate: 0.8,
            listeningMode: true,
            autoPlay: true
        },
        isLoading: false
    };

    // 页面模块映射
    const pageModules = {
        'index': null,
        'dictation': DictationController,
        'history': HistoryModule,
        'difficult-words': DifficultWordsModule,
        'reports': ReportsModule
    };

    /**
     * 初始化应用
     */
    function init() {
        console.log('应用初始化开始...');

        // 检测当前页面
        detectCurrentPage();

        // 检测浏览器兼容性
        checkBrowserCompatibility();

        // 初始化全局事件
        initGlobalEvents();

        // 加载用户设置
        loadUserSettings();

        // 初始化页面特定模块
        initPageModule();

        // 显示加载完成
        hideGlobalLoading();

        console.log('应用初始化完成');
    }

    /**
     * 检测当前页面
     */
    function detectCurrentPage() {
        const path = window.location.pathname;
        const pageName = path.split('/').pop().replace('.html', '') || 'index';
        globalState.currentPage = pageName;
        console.log('当前页面:', pageName);
    }

    /**
     * 检测浏览器兼容性
     */
    function checkBrowserCompatibility() {
        if (!SpeechModule) {
            console.warn('SpeechModule未加载');
            return;
        }

        const speechCompat = SpeechModule.checkCompatibility();

        if (!speechCompat.speechSynthesis || !speechCompat.speechRecognition) {
            showCompatibilityWarning(speechCompat.message);
        }

        // 检测其他特性
        if (!window.fetch) {
            showCompatibilityWarning('您的浏览器不支持现代网络特性，请升级浏览器。');
        }

        // 检测Web Audio API
        if (!window.AudioContext && !window.webkitAudioContext) {
            console.warn('浏览器不支持Web Audio API');
        }
    }

    /**
     * 显示兼容性警告
     * @param {string} message - 警告信息
     */
    function showCompatibilityWarning(message) {
        const warning = document.createElement('div');
        warning.className = 'compatibility-warning';
        warning.innerHTML = `
            <div class="warning-content">
                <span class="warning-icon">⚠️</span>
                <span class="warning-text">${message}</span>
                <button class="warning-close" onclick="this.parentElement.parentElement.remove()">×</button>
            </div>
        `;
        document.body.insertBefore(warning, document.body.firstChild);
    }

    /**
     * 初始化全局事件
     */
    function initGlobalEvents() {
        // DOM加载完成
        document.addEventListener('DOMContentLoaded', handleDOMContentLoaded);

        // 页面卸载前保存状态
        window.addEventListener('beforeunload', handleBeforeUnload);

        // 页面可见性变化
        document.addEventListener('visibilitychange', handleVisibilityChange);

        // 网络状态
        window.addEventListener('online', handleOnline);
        window.addEventListener('offline', handleOffline);

        // 全局错误处理
        window.addEventListener('error', handleGlobalError);
        window.addEventListener('unhandledrejection', handleUnhandledRejection);
    }

    /**
     * DOM加载完成处理
     */
    function handleDOMContentLoaded() {
        // 初始化导航
        initNavigation();

        // 初始化主题
        initTheme();

        // 初始化Toast组件
        initToast();

        // 初始化模态框
        initModals();

        // 绑定全局按钮事件
        bindGlobalButtons();
    }

    /**
     * 初始化导航
     */
    function initNavigation() {
        const navItems = document.querySelectorAll('.nav-item');
        const currentPage = globalState.currentPage;

        navItems.forEach(item => {
            const href = item.getAttribute('href');
            if (href) {
                const itemPage = href.replace('.html', '').replace('/', '');
                if (itemPage === currentPage || (currentPage === '' && itemPage === 'index')) {
                    item.classList.add('active');
                }
            }
        });

        // 移动端菜单切换
        const menuToggle = document.querySelector('.menu-toggle');
        const nav = document.querySelector('.nav');

        if (menuToggle && nav) {
            menuToggle.addEventListener('click', () => {
                nav.classList.toggle('open');
            });
        }
    }

    /**
     * 初始化主题
     */
    function initTheme() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        setTheme(savedTheme);
    }

    /**
     * 设置主题
     * @param {string} theme - 主题名称
     */
    function setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    }

    /**
     * 切换主题
     */
    function toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
    }

    /**
     * 初始化Toast组件
     */
    function initToast() {
        // 创建Toast容器
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        // 定义全局Toast模块
        window.ToastModule = {
            show: function(message, type = 'info', duration = 3000) {
                const container = document.getElementById('toast-container');
                if (!container) return;

                const toast = document.createElement('div');
                toast.className = `toast toast-${type}`;
                toast.innerHTML = `
                    <span class="toast-icon">${getToastIcon(type)}</span>
                    <span class="toast-message">${escapeHtml(message)}</span>
                `;

                container.appendChild(toast);

                // 动画显示
                setTimeout(() => toast.classList.add('show'), 10);

                // 自动关闭
                setTimeout(() => {
                    toast.classList.remove('show');
                    setTimeout(() => toast.remove(), 300);
                }, duration);
            }
        };
    }

    /**
     * 获取Toast图标
     * @param {string} type - 类型
     * @returns {string} 图标
     */
    function getToastIcon(type) {
        const icons = {
            success: '✓',
            error: '✗',
            warning: '⚠',
            info: 'ℹ'
        };
        return icons[type] || icons.info;
    }

    /**
     * 初始化模态框
     */
    function initModals() {
        // 全局模态框方法
        window.ModalModule = {
            show: function(content, options = {}) {
                let modal = document.getElementById('global-modal');

                if (!modal) {
                    modal = document.createElement('div');
                    modal.id = 'global-modal';
                    modal.className = 'modal';
                    modal.innerHTML = `
                        <div class="modal-overlay" onclick="ModalModule.hide()"></div>
                        <div class="modal-content">
                            <div class="modal-header">
                                <h3 class="modal-title"></h3>
                                <button class="modal-close" onclick="ModalModule.hide()">×</button>
                            </div>
                            <div class="modal-body"></div>
                            <div class="modal-footer"></div>
                        </div>
                    `;
                    document.body.appendChild(modal);
                }

                const titleEl = modal.querySelector('.modal-title');
                const bodyEl = modal.querySelector('.modal-body');
                const footerEl = modal.querySelector('.modal-footer');

                if (options.title) {
                    titleEl.textContent = options.title;
                    titleEl.style.display = '';
                } else {
                    titleEl.style.display = 'none';
                }

                if (typeof content === 'string') {
                    bodyEl.innerHTML = content;
                } else {
                    bodyEl.innerHTML = '';
                    bodyEl.appendChild(content);
                }

                if (options.buttons && options.buttons.length > 0) {
                    footerEl.innerHTML = options.buttons.map(btn =>
                        `<button class="btn ${btn.class || 'btn-secondary'}" onclick="${btn.onclick}">${btn.text}</button>`
                    ).join('');
                } else {
                    footerEl.innerHTML = '';
                }

                modal.classList.add('show');
                document.body.style.overflow = 'hidden';
            },
            hide: function() {
                const modal = document.getElementById('global-modal');
                if (modal) {
                    modal.classList.remove('show');
                    document.body.style.overflow = '';
                }
            },
            confirm: function(message, onConfirm, onCancel) {
                this.show(`<p>${escapeHtml(message)}</p>`, {
                    title: '确认',
                    buttons: [
                        {
                            text: '取消',
                            class: 'btn-secondary',
                            onclick: 'ModalModule.hide(); ' + (onCancel ? onCancel + '()' : '')
                        },
                        {
                            text: '确定',
                            class: 'btn-primary',
                            onclick: 'ModalModule.hide(); ' + (onConfirm ? onConfirm + '()' : '')
                        }
                    ]
                });
            }
        };
    }

    /**
     * 绑定全局按钮事件
     */
    function bindGlobalButtons() {
        // 主题切换按钮
        const themeToggle = document.querySelector('.theme-toggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', toggleTheme);
        }

        // 语音测试按钮
        const speechTestBtn = document.querySelector('.speech-test-btn');
        if (speechTestBtn) {
            speechTestBtn.addEventListener('click', testSpeech);
        }

        // 快捷键
        document.addEventListener('keydown', handleGlobalKeyDown);
    }

    /**
     * 处理全局快捷键
     * @param {KeyboardEvent} e - 键盘事件
     */
    function handleGlobalKeyDown(e) {
        // ESC关闭模态框
        if (e.key === 'Escape') {
            if (window.ModalModule) {
                ModalModule.hide();
            }
        }

        // 页面特定快捷键
        if (globalState.currentPage === 'dictation' && !e.ctrlKey && !e.metaKey) {
            // 听写页面快捷键
            if (e.key === ' ' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                // 空格键：播放/暂停
                if (DictationController) {
                    DictationController.togglePause();
                }
            }
        }
    }

    /**
     * 加载用户设置
     */
    async function loadUserSettings() {
        try {
            // 从本地存储加载
            const savedSettings = localStorage.getItem('userSettings');
            if (savedSettings) {
                Object.assign(globalState.settings, JSON.parse(savedSettings));
            }

            // 从服务器加载
            if (API) {
                const settings = await API.getSettings();
                Object.assign(globalState.settings, settings);
                // 保存到本地
                localStorage.setItem('userSettings', JSON.stringify(globalState.settings));
            }
        } catch (error) {
            console.log('加载用户设置失败，使用默认设置');
        }
    }

    /**
     * 保存用户设置
     * @param {Object} settings - 设置对象
     */
    async function saveUserSettings(settings) {
        try {
            Object.assign(globalState.settings, settings);
            localStorage.setItem('userSettings', JSON.stringify(globalState.settings));

            if (API) {
                await API.updateSettings(settings);
            }
        } catch (error) {
            console.error('保存设置失败:', error);
        }
    }

    /**
     * 初始化页面特定模块
     */
    function initPageModule() {
        const page = globalState.currentPage;

        // 首页/听写页面特殊处理
        if (page === 'index' || page === '') {
            initDictationPage();
            return;
        }

        const module = pageModules[page];

        if (module && typeof module.init === 'function') {
            // 根据页面类型初始化
            switch (page) {
                case 'history':
                    module.init('history-container') || module.init('batchList');
                    break;
                case 'difficult-words':
                    module.init('difficult-words-container') || module.init('difficultWordsTable');
                    break;
                case 'reports':
                    module.init('reports-container') || module.init('weeklyChart');
                    break;
                default:
                    if (module) {
                        module.init();
                    }
            }
        }
    }

    /**
     * 初始化听写页面
     */
    function initDictationPage() {
        // 获取URL参数
        const urlParams = new URLSearchParams(window.location.search);
        const batchId = urlParams.get('batchId');

        // 获取DOM元素
        const elements = {
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
        if (SpeechModule) {
            const compatibility = SpeechModule.checkCompatibility();
            if (!compatibility.isFullySupported) {
                showWarning('建议使用Chrome浏览器以获得最佳语音交互体验');
            }
        }

        // 初始化听写模块
        if (DictationModule) {
            DictationModule.init({
                onWordChange: (word, index) => {
                    if (elements.currentWord) elements.currentWord.textContent = word;
                    if (elements.wordDisplay) elements.wordDisplay.textContent = word;
                },
                onProgressUpdate: (progress) => {
                    if (elements.completedCount) elements.completedCount.textContent = progress.completed;
                    if (elements.remainingCount) elements.remainingCount.textContent = progress.remaining;
                    if (elements.progressBar) elements.progressBar.style.width = progress.progress + '%';
                },
                onComplete: (result) => {
                    showSuccess(`听写完成！共听写 ${result.totalWords} 个词语，用时 ${result.totalTime} 秒`);
                    resetUI();
                },
                onError: (error) => {
                    showError('听写出错: ' + error.message);
                }
            });
        }

        // 绑定事件
        bindDictationEvents(elements);

        // 如果有batchId参数，直接开始听写
        if (batchId && DictationModule) {
            startDictationWithBatch(batchId, elements);
        }
    }

    /**
     * 绑定听写页面事件
     */
    function bindDictationEvents(elements) {
        // 开始听写按钮
        if (elements.btnStart) {
            elements.btnStart.addEventListener('click', onStartClick);
        }

        // 再次读取按钮
        if (elements.btnRepeat) {
            elements.btnRepeat.addEventListener('click', () => {
                if (DictationModule) DictationModule.repeat();
                if (DictationController) DictationController.repeatCurrent();
            });
        }

        // 上一个按钮
        if (elements.btnPrevious) {
            elements.btnPrevious.addEventListener('click', () => {
                if (DictationModule) DictationModule.previous();
                if (DictationController) DictationController.previousWord();
            });
        }

        // 下一个按钮
        if (elements.btnNext) {
            elements.btnNext.addEventListener('click', () => {
                if (DictationModule) DictationModule.next();
                if (DictationController) DictationController.nextWord();
            });
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
     * 当前批次ID
     */
    let currentBatchId = null;

    /**
     * 开始听写按钮点击
     */
    function onStartClick() {
        if (!currentBatchId) {
            showWarning('请先录入词语并创建批次');
            return;
        }

        // 更新UI状态
        const elements = getDictationElements();
        if (elements.btnStart) elements.btnStart.style.display = 'none';
        if (elements.btnEnd) elements.btnEnd.style.display = 'inline-block';
        if (elements.btnRepeat) elements.btnRepeat.disabled = false;
        if (elements.btnPrevious) elements.btnPrevious.disabled = false;
        if (elements.btnNext) elements.btnNext.disabled = false;
        if (elements.progressContainer) elements.progressContainer.style.display = 'block';

        // 开始听写
        if (DictationModule) {
            DictationModule.start(currentBatchId);
        }
    }

    /**
     * 结束听写按钮点击
     */
    function onEndClick() {
        if (DictationModule) {
            DictationModule.stop();
        }
        if (DictationController) {
            DictationController.finishDictation();
        }
        resetUI();
    }

    /**
     * 创建批次按钮点击
     */
    async function onAddBatchClick() {
        const elements = getDictationElements();
        const wordsText = elements.wordInput?.value?.trim();
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
            if (elements.btnAddBatch) {
                elements.btnAddBatch.disabled = true;
                elements.btnAddBatch.innerHTML = '<i class="bi bi-spinner-border me-2"></i>创建中...';
            }

            const result = await API.createBatch(words);
            currentBatchId = result.data?.id || result.id;

            showSuccess(`成功创建批次，共 ${words.length} 个词语`);

            // 更新统计显示
            if (elements.remainingCount) elements.remainingCount.textContent = words.length;

            // 启用开始按钮
            if (elements.btnStart) elements.btnStart.disabled = false;

        } catch (error) {
            showError('创建批次失败: ' + error.message);
        } finally {
            if (elements.btnAddBatch) {
                elements.btnAddBatch.disabled = false;
                elements.btnAddBatch.innerHTML = '<i class="bi bi-plus-circle me-2"></i>创建批次';
            }
        }
    }

    /**
     * 使用批次ID开始听写
     */
    async function startDictationWithBatch(batchId, elements) {
        currentBatchId = batchId;

        try {
            if (DictationModule) {
                await DictationModule.start(batchId);
            }
            if (DictationController) {
                const batchData = await API.getBatchDetail(batchId);
                DictationController.init(batchData);
                await DictationController.start();
            }

            // 更新UI状态
            if (elements.btnStart) elements.btnStart.style.display = 'none';
            if (elements.btnEnd) elements.btnEnd.style.display = 'inline-block';
            if (elements.btnRepeat) elements.btnRepeat.disabled = false;
            if (elements.btnPrevious) elements.btnPrevious.disabled = false;
            if (elements.btnNext) elements.btnNext.disabled = false;
            if (elements.progressContainer) elements.progressContainer.style.display = 'block';

        } catch (error) {
            showError('开始听写失败: ' + error.message);
        }
    }

    /**
     * 获取听写页面DOM元素
     */
    function getDictationElements() {
        return {
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
    }

    /**
     * 重置UI状态
     */
    function resetUI() {
        const elements = getDictationElements();
        if (elements.btnStart) elements.btnStart.style.display = 'inline-block';
        if (elements.btnEnd) elements.btnEnd.style.display = 'none';
        if (elements.btnRepeat) elements.btnRepeat.disabled = true;
        if (elements.btnPrevious) elements.btnPrevious.disabled = true;
        if (elements.btnNext) elements.btnNext.disabled = true;
        if (elements.progressContainer) elements.progressContainer.style.display = 'none';
        if (elements.progressBar) elements.progressBar.style.width = '0%';
        if (elements.currentWord) elements.currentWord.textContent = '-';
        if (elements.completedCount) elements.completedCount.textContent = '0';
        if (elements.remainingCount) elements.remainingCount.textContent = '0';
        if (elements.wordDisplay) elements.wordDisplay.textContent = '点击下方按钮开始听写';
    }

    /**
     * 测试语音功能
     */
    async function testSpeech() {
        try {
            showToast('正在测试语音功能...', 'info');
            if (SpeechModule) {
                await SpeechModule.speakWord('测试');
            }
            showToast('语音测试成功', 'success');
        } catch (error) {
            showToast('语音测试失败: ' + error.message, 'error');
        }
    }

    /**
     * 页面卸载前处理
     */
    function handleBeforeUnload() {
        // 保存状态
        if (globalState.currentPage === 'dictation') {
            const state = DictationController?.getState() || DictationModule?.getState();
            if (state && state.isRunning && !state.isPaused) {
                // 提示用户
                return '听写正在进行中，确定要离开吗？';
            }
        }
    }

    /**
     * 页面可见性变化处理
     */
    function handleVisibilityChange() {
        if (document.hidden) {
            // 页面隐藏时暂停
            if (globalState.currentPage === 'dictation') {
                const state = DictationController?.getState() || DictationModule?.getState?.();
                if (state && state.isRunning && !state.isPaused) {
                    if (DictationController) DictationController.pause();
                    if (DictationModule) DictationModule.stop();
                    showToast('页面已隐藏，听写已暂停', 'warning');
                }
            }
        }
    }

    /**
     * 网络连接处理
     */
    function handleOnline() {
        showToast('网络已连接', 'success');
    }

    /**
     * 网络断开处理
     */
    function handleOffline() {
        showToast('网络已断开，部分功能可能无法使用', 'warning');
    }

    /**
     * 全局错误处理
     * @param {Event} event - 错误事件
     */
    function handleGlobalError(event) {
        console.error('全局错误:', event.error);
        showToast('发生错误，请刷新页面重试', 'error');
    }

    /**
     * 未处理的Promise拒绝
     * @param {PromiseRejectionEvent} event - 事件
     */
    function handleUnhandledRejection(event) {
        console.error('未处理的Promise拒绝:', event.reason);
        showToast('发生错误，请刷新页面重试', 'error');
    }

    /**
     * 显示全局加载状态
     */
    function showLoading() {
        globalState.isLoading = true;
        const loader = document.getElementById('global-loader');
        if (loader) {
            loader.classList.add('show');
        }
    }

    /**
     * 隐藏全局加载状态
     */
    function hideLoading() {
        globalState.isLoading = false;
        const loader = document.getElementById('global-loader');
        if (loader) {
            loader.classList.remove('show');
        }
    }

    /**
     * 隐藏初始加载动画
     */
    function hideGlobalLoading() {
        const loading = document.getElementById('initial-loading');
        if (loading) {
            loading.classList.add('fade-out');
            setTimeout(() => loading.remove(), 300);
        }
    }

    /**
     * 显示Toast消息
     * @param {string} message - 消息
     * @param {string} type - 类型
     */
    function showToast(message, type = 'info') {
        if (window.ToastModule) {
            ToastModule.show(message, type);
        } else {
            console.log(`[${type}] ${message}`);
        }
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
     * HTML转义
     * @param {string} text - 原始文本
     * @returns {string} 转义后的文本
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text || '';
        return div.innerHTML;
    }

    /**
     * 获取全局状态
     * @returns {Object} 状态对象
     */
    function getState() {
        return { ...globalState };
    }

    /**
     * 获取设置
     * @returns {Object} 设置对象
     */
    function getSettings() {
        return { ...globalState.settings };
    }

    // 页面加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // 公开API
    return {
        init,
        getState,
        getSettings,
        saveUserSettings,
        showLoading,
        hideLoading,
        showToast,
        showSuccess,
        showWarning,
        showError,
        toggleTheme
    };
})();

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = App;
} else {
    window.App = App;
}