/**
 * difficult-words.js - 生词本页面逻辑模块
 * 管理生词显示、星级评分、导出和练习创建
 */

const DifficultWordsModule = (function() {
    // 状态
    let state = {
        words: [],
        currentPage: 1,
        pageSize: 20,
        totalCount: 0,
        selectedIds: new Set(),
        sortField: 'stars',
        sortOrder: 'desc',
        filters: {
            minStars: 0,
            keyword: ''
        }
    };

    // DOM元素缓存
    let elements = {
        container: null,
        tableBody: null,
        pagination: null,
        searchInput: null,
        starFilter: null,
        searchBtn: null,
        selectAll: null,
        exportBtn: null,
        practiceBtn: null,
        deleteBtn: null
    };

    /**
     * 初始化模块
     * @param {string} containerId - 容器元素ID
     * @param {Object} options - 配置选项
     */
    function init(containerId, options = {}) {
        elements.container = document.getElementById(containerId);
        if (!elements.container) {
            console.error('找不到容器元素:', containerId);
            return false;
        }

        // 初始化DOM元素引用
        initElements();

        // 绑定事件
        bindEvents();

        // 加载数据
        loadDifficultWords();

        return true;
    }

    /**
     * 初始化DOM元素引用
     */
    function initElements() {
        elements.tableBody = document.getElementById('difficult-words-body') ||
                             document.getElementById('difficultWordsTable');
        elements.pagination = document.getElementById('words-pagination');
        elements.searchInput = document.getElementById('word-search-input');
        elements.starFilter = document.getElementById('star-filter');
        elements.searchBtn = document.getElementById('search-words-btn');
        elements.selectAll = document.getElementById('select-all-words');
        elements.exportBtn = document.getElementById('export-btn') ||
                             document.getElementById('btnExport');
        elements.practiceBtn = document.getElementById('create-practice-btn') ||
                               document.getElementById('btnPractice');
        elements.deleteBtn = document.getElementById('delete-selected-btn');
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        // 搜索按钮
        if (elements.searchBtn) {
            elements.searchBtn.addEventListener('click', handleSearch);
        }

        // 搜索输入框回车
        if (elements.searchInput) {
            elements.searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    handleSearch();
                }
            });
        }

        // 星级筛选
        if (elements.starFilter) {
            elements.starFilter.addEventListener('change', handleSearch);
        }

        // 全选
        if (elements.selectAll) {
            elements.selectAll.addEventListener('change', handleSelectAll);
        }

        // 导出按钮
        if (elements.exportBtn) {
            elements.exportBtn.addEventListener('click', handleExport);
        }

        // 创建练习按钮
        if (elements.practiceBtn) {
            elements.practiceBtn.addEventListener('click', handleCreatePractice);
        }

        // 删除选中按钮
        if (elements.deleteBtn) {
            elements.deleteBtn.addEventListener('click', handleDeleteSelected);
        }
    }

    /**
     * 加载生词列表
     * @param {number} page - 页码
     */
    async function loadDifficultWords(page = 1) {
        state.currentPage = page;

        try {
            const params = {
                page: state.currentPage,
                size: state.pageSize,
                minStars: state.filters.minStars,
                keyword: state.filters.keyword,
                sortField: state.sortField,
                sortOrder: state.sortOrder
            };

            // 移除空值参数
            Object.keys(params).forEach(key => {
                if (!params[key] && params[key] !== 0) delete params[key];
            });

            const response = await API.getDifficultWords(params);

            state.words = response.data || response.list || [];
            state.totalCount = response.total || response.totalCount || 0;

            renderWordList();
            renderPagination();
            updateButtonStates();
        } catch (error) {
            console.error('加载生词列表失败:', error);
            showToast(API.handleAPIError(error, '加载失败'), 'error');
        }
    }

    /**
     * 渲染生词列表
     */
    function renderWordList() {
        if (!elements.tableBody) return;

        if (state.words.length === 0) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        <div class="empty-icon">📚</div>
                        <div class="empty-text">生词本为空</div>
                        <div class="empty-hint">听写中的错误词语会自动添加到这里</div>
                    </td>
                </tr>
            `;
            return;
        }

        const html = state.words.map((word, index) => {
            const stars = word.stars || word.masteryLevel || 3;
            const starsHtml = renderStars(stars);
            const lastPractice = formatDate(word.lastPracticeTime || word.lastPracticeDate || word.updatedAt);
            const isSelected = state.selectedIds.has(word.id);

            return `
                <tr class="word-row ${isSelected ? 'selected' : ''}" data-id="${word.id}">
                    <td class="col-checkbox">
                        <input type="checkbox"
                               class="word-checkbox"
                               data-id="${word.id}"
                               ${isSelected ? 'checked' : ''}
                               onchange="DifficultWordsModule.toggleSelect(${word.id})">
                    </td>
                    <td class="col-index">${(state.currentPage - 1) * state.pageSize + index + 1}</td>
                    <td class="col-word">
                        <span class="word-text">${escapeHtml(word.word || word.wordText)}</span>
                        ${word.note ? `<span class="word-note" title="${escapeHtml(word.note)}">📝</span>` : ''}
                    </td>
                    <td class="col-stars">
                        <div class="star-rating" data-id="${word.id}">
                            ${starsHtml}
                        </div>
                    </td>
                    <td class="col-count">
                        <span class="wrong-count">${word.wrongCount || word.errorCount || 0}</span>次错误
                    </td>
                    <td class="col-time">${lastPractice}</td>
                    <td class="col-actions">
                        <button class="btn btn-sm btn-speak"
                                onclick="DifficultWordsModule.speakWord('${escapeHtml(word.word || word.wordText)}')"
                                title="朗读">
                            🔊
                        </button>
                        <button class="btn btn-sm btn-edit"
                                onclick="DifficultWordsModule.editWord(${word.id})"
                                title="编辑">
                            ✏️
                        </button>
                        <button class="btn btn-sm btn-delete"
                                onclick="DifficultWordsModule.deleteWord(${word.id})"
                                title="删除">
                            🗑️
                        </button>
                    </td>
                </tr>
            `;
        }).join('');

        elements.tableBody.innerHTML = html;

        // 绑定星级评分事件
        bindStarRatingEvents();
    }

    /**
     * 渲染星级
     * @param {number} stars - 星级数
     * @returns {string} HTML字符串
     */
    function renderStars(stars) {
        let html = '';
        for (let i = 1; i <= 5; i++) {
            html += `<span class="star ${i <= stars ? 'active' : ''}" data-star="${i}">★</span>`;
        }
        return html;
    }

    /**
     * 绑定星级评分事件
     */
    function bindStarRatingEvents() {
        const starContainers = document.querySelectorAll('.star-rating');
        starContainers.forEach(container => {
            const stars = container.querySelectorAll('.star');
            stars.forEach(star => {
                star.addEventListener('click', async (e) => {
                    e.stopPropagation();
                    const wordId = parseInt(container.dataset.id);
                    const starValue = parseInt(star.dataset.star);
                    await updateStarRating(wordId, starValue);
                });
            });
        });
    }

    /**
     * 更新星级评分
     * @param {number} wordId - 生词ID
     * @param {number} stars - 新星级
     */
    async function updateStarRating(wordId, stars) {
        try {
            await API.updateDifficultWord(wordId, { stars });

            // 更新本地状态
            const word = state.words.find(w => w.id === wordId);
            if (word) {
                word.stars = stars;
                renderWordList();
            }

            showToast('评分更新成功', 'success');
        } catch (error) {
            console.error('更新评分失败:', error);
            showToast(API.handleAPIError(error, '更新失败'), 'error');
        }
    }

    /**
     * 渲染分页
     */
    function renderPagination() {
        if (!elements.pagination) return;

        const totalPages = Math.ceil(state.totalCount / state.pageSize);

        if (totalPages <= 1) {
            elements.pagination.innerHTML = '';
            return;
        }

        let html = `<div class="pagination-info">共 ${state.totalCount} 条</div>`;

        // 上一页
        html += `
            <button class="page-btn prev ${state.currentPage <= 1 ? 'disabled' : ''}"
                    onclick="DifficultWordsModule.loadDifficultWords(${state.currentPage - 1})"
                    ${state.currentPage <= 1 ? 'disabled' : ''}>
                ‹
            </button>
        `;

        // 页码
        const startPage = Math.max(1, state.currentPage - 2);
        const endPage = Math.min(totalPages, state.currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <button class="page-btn ${i === state.currentPage ? 'active' : ''}"
                        onclick="DifficultWordsModule.loadDifficultWords(${i})">
                    ${i}
                </button>
            `;
        }

        // 下一页
        html += `
            <button class="page-btn next ${state.currentPage >= totalPages ? 'disabled' : ''}"
                    onclick="DifficultWordsModule.loadDifficultWords(${state.currentPage + 1})"
                    ${state.currentPage >= totalPages ? 'disabled' : ''}>
                ›
            </button>
        `;

        elements.pagination.innerHTML = html;
    }

    /**
     * 切换选择
     * @param {number} wordId - 生词ID
     */
    function toggleSelect(wordId) {
        if (state.selectedIds.has(wordId)) {
            state.selectedIds.delete(wordId);
        } else {
            state.selectedIds.add(wordId);
        }
        updateButtonStates();

        // 更新行样式
        const row = document.querySelector(`tr[data-id="${wordId}"]`);
        if (row) {
            row.classList.toggle('selected', state.selectedIds.has(wordId));
        }
    }

    /**
     * 全选/取消全选
     */
    function handleSelectAll() {
        const isChecked = elements.selectAll?.checked;

        if (isChecked) {
            state.words.forEach(word => state.selectedIds.add(word.id));
        } else {
            state.selectedIds.clear();
        }

        // 更新复选框状态
        document.querySelectorAll('.word-checkbox').forEach(checkbox => {
            checkbox.checked = isChecked;
        });

        // 更新行样式
        document.querySelectorAll('.word-row').forEach(row => {
            row.classList.toggle('selected', isChecked);
        });

        updateButtonStates();
    }

    /**
     * 更新按钮状态
     */
    function updateButtonStates() {
        const hasSelection = state.selectedIds.size > 0;

        if (elements.practiceBtn) {
            elements.practiceBtn.disabled = !hasSelection;
        }
        if (elements.deleteBtn) {
            elements.deleteBtn.disabled = !hasSelection;
        }
    }

    /**
     * 处理搜索
     */
    function handleSearch() {
        state.filters = {
            minStars: parseInt(elements.starFilter?.value) || 0,
            keyword: elements.searchInput?.value?.trim() || ''
        };
        state.selectedIds.clear();
        loadDifficultWords(1);
    }

    /**
     * 朗读词语
     * @param {string} word - 词语
     */
    async function speakWord(word) {
        if (SpeechModule) {
            await SpeechModule.speakWord(word);
        }
    }

    /**
     * 编辑生词
     * @param {number} wordId - 生词ID
     */
    function editWord(wordId) {
        const word = state.words.find(w => w.id === wordId);
        if (!word) return;

        // 使用简单的prompt或模态框编辑
        const newNote = prompt('编辑备注:', word.note || '');
        if (newNote === null) return; // 用户取消

        updateWordNote(wordId, newNote);
    }

    /**
     * 更新生词备注
     * @param {number} wordId - 生词ID
     * @param {string} note - 新备注
     */
    async function updateWordNote(wordId, note) {
        try {
            await API.updateDifficultWord(wordId, { note });

            const word = state.words.find(w => w.id === wordId);
            if (word) {
                word.note = note;
                renderWordList();
            }

            showToast('更新成功', 'success');
        } catch (error) {
            console.error('更新失败:', error);
            showToast(API.handleAPIError(error, '更新失败'), 'error');
        }
    }

    /**
     * 删除生词
     * @param {number} wordId - 生词ID
     */
    async function deleteWord(wordId) {
        if (!confirm('确定要从生词本中删除这个词吗？')) {
            return;
        }

        try {
            await API.deleteDifficultWord(wordId);

            // 从本地列表移除
            state.words = state.words.filter(w => w.id !== wordId);
            state.selectedIds.delete(wordId);

            renderWordList();
            renderPagination();
            showToast('删除成功', 'success');
        } catch (error) {
            console.error('删除失败:', error);
            showToast(API.handleAPIError(error, '删除失败'), 'error');
        }
    }

    /**
     * 删除选中的生词
     */
    async function handleDeleteSelected() {
        if (state.selectedIds.size === 0) {
            showToast('请先选择要删除的词语', 'warning');
            return;
        }

        if (!confirm(`确定要删除选中的 ${state.selectedIds.size} 个词语吗？`)) {
            return;
        }

        try {
            const deletePromises = Array.from(state.selectedIds).map(id =>
                API.deleteDifficultWord(id)
            );

            await Promise.all(deletePromises);

            state.selectedIds.clear();
            loadDifficultWords(state.currentPage);
            showToast('删除成功', 'success');
        } catch (error) {
            console.error('批量删除失败:', error);
            showToast(API.handleAPIError(error, '删除失败'), 'error');
        }
    }

    /**
     * 导出生词
     */
    async function handleExport() {
        try {
            const params = {
                minStars: state.filters.minStars,
                keyword: state.filters.keyword
            };

            const response = await API.getDifficultWords({ ...params, size: 1000 });
            const words = response.data || response.list || [];

            if (words.length === 0) {
                showToast('没有可导出的生词', 'warning');
                return;
            }

            // 生成CSV内容
            const csvContent = generateCSV(words);

            // 下载文件
            downloadFile(csvContent, '生词本导出.csv', 'text/csv;charset=utf-8');

            showToast(`成功导出 ${words.length} 个生词`, 'success');
        } catch (error) {
            console.error('导出失败:', error);
            showToast(API.handleAPIError(error, '导出失败'), 'error');
        }
    }

    /**
     * 生成CSV内容
     * @param {Array} words - 生词列表
     * @returns {string} CSV内容
     */
    function generateCSV(words) {
        const headers = ['词语', '星级', '错误次数', '最后练习时间', '备注'];
        const rows = words.map(word => [
            word.word || word.wordText,
            word.stars || word.masteryLevel || 3,
            word.wrongCount || word.errorCount || 0,
            formatDate(word.lastPracticeTime || word.lastPracticeDate || word.updatedAt),
            word.note || ''
        ]);

        // 添加BOM以支持中文
        const BOM = '\uFEFF';
        const csvRows = [headers, ...rows].map(row =>
            row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')
        );

        return BOM + csvRows.join('\n');
    }

    /**
     * 下载文件
     * @param {string} content - 文件内容
     * @param {string} filename - 文件名
     * @param {string} mimeType - MIME类型
     */
    function downloadFile(content, filename, mimeType) {
        const blob = new Blob([content], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    }

    /**
     * 创建针对性练习
     */
    async function handleCreatePractice() {
        if (state.selectedIds.size === 0) {
            showToast('请先选择要练习的词语', 'warning');
            return;
        }

        try {
            const wordIds = Array.from(state.selectedIds);
            const response = await API.createPracticeBatch(wordIds);

            showToast('练习批次创建成功', 'success');

            // 跳转到听写页面
            if (response.batchId || response.data?.id) {
                window.location.href = `/dictation.html?batchId=${response.batchId || response.data.id}`;
            }
        } catch (error) {
            console.error('创建练习失败:', error);
            showToast(API.handleAPIError(error, '创建练习失败'), 'error');
        }
    }

    /**
     * 格式化日期
     * @param {string|Date} date - 日期
     * @returns {string} 格式化后的日期
     */
    function formatDate(date) {
        if (!date) return '-';
        const d = new Date(date);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
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
     * 显示提示消息
     * @param {string} message - 消息内容
     * @param {string} type - 类型
     */
    function showToast(message, type = 'info') {
        if (window.ToastModule) {
            window.ToastModule.show(message, type);
        } else {
            console.log(`[${type}] ${message}`);
        }
    }

    /**
     * 刷新列表
     */
    function refresh() {
        state.selectedIds.clear();
        loadDifficultWords(state.currentPage);
    }

    /**
     * 获取选中的词语ID
     * @returns {Array} 选中的ID列表
     */
    function getSelectedIds() {
        return Array.from(state.selectedIds);
    }

    // 公开API
    return {
        init,
        loadDifficultWords,
        toggleSelect,
        speakWord,
        editWord,
        deleteWord,
        refresh,
        getSelectedIds
    };
})();

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DifficultWordsModule;
} else {
    window.DifficultWordsModule = DifficultWordsModule;
}