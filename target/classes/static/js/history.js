/**
 * history.js - 历史记录页面逻辑模块
 * 管理批次列表显示、筛选和详情查看
 */

const HistoryModule = (function() {
    // 状态
    let state = {
        batches: [],
        currentPage: 1,
        pageSize: 10,
        totalCount: 0,
        filters: {
            status: '',
            keyword: '',
            startDate: '',
            endDate: ''
        },
        currentDetailId: null
    };

    // DOM元素缓存
    let elements = {
        container: null,
        filterForm: null,
        statusFilter: null,
        keywordFilter: null,
        startDateFilter: null,
        endDateFilter: null,
        searchBtn: null,
        resetBtn: null,
        tableBody: null,
        pagination: null,
        modal: null,
        modalContent: null,
        modalClose: null
    };

    // 回调
    let callbacks = {
        onStartDictation: null,
        onDeleteBatch: null,
        onViewDetail: null
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

        // 合并配置
        Object.assign(state, options.initialState || {});
        Object.assign(callbacks, options.callbacks || {});

        // 初始化DOM元素引用
        initElements();

        // 绑定事件
        bindEvents();

        // 加载数据
        loadBatches();

        return true;
    }

    /**
     * 初始化DOM元素引用
     */
    function initElements() {
        elements.filterForm = document.getElementById('history-filter-form');
        elements.statusFilter = document.getElementById('status-filter');
        elements.keywordFilter = document.getElementById('keyword-filter');
        elements.startDateFilter = document.getElementById('start-date-filter');
        elements.endDateFilter = document.getElementById('end-date-filter');
        elements.searchBtn = document.getElementById('search-btn');
        elements.resetBtn = document.getElementById('reset-btn');
        elements.tableBody = document.getElementById('history-table-body');
        elements.pagination = document.getElementById('history-pagination');
        elements.modal = document.getElementById('detail-modal');
        elements.modalContent = document.getElementById('modal-content');
        elements.modalClose = document.getElementById('modal-close');
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        // 搜索按钮
        if (elements.searchBtn) {
            elements.searchBtn.addEventListener('click', handleSearch);
        }

        // 重置按钮
        if (elements.resetBtn) {
            elements.resetBtn.addEventListener('click', handleReset);
        }

        // 筛选器回车搜索
        if (elements.keywordFilter) {
            elements.keywordFilter.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    handleSearch();
                }
            });
        }

        // 模态框关闭
        if (elements.modalClose) {
            elements.modalClose.addEventListener('click', closeDetailModal);
        }

        // 点击模态框外部关闭
        if (elements.modal) {
            elements.modal.addEventListener('click', (e) => {
                if (e.target === elements.modal) {
                    closeDetailModal();
                }
            });
        }

        // ESC关闭模态框
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && elements.modal &&
                elements.modal.style.display !== 'none') {
                closeDetailModal();
            }
        });
    }

    /**
     * 加载批次列表
     * @param {number} page - 页码
     */
    async function loadBatches(page = 1) {
        state.currentPage = page;

        try {
            const params = {
                page: state.currentPage,
                size: state.pageSize,
                ...state.filters
            };

            // 移除空值参数
            Object.keys(params).forEach(key => {
                if (!params[key]) delete params[key];
            });

            const response = await API.getBatches(params);

            state.batches = response.data || response.list || [];
            state.totalCount = response.total || response.totalCount || 0;

            renderBatchList();
            renderPagination();
        } catch (error) {
            console.error('加载批次列表失败:', error);
            showToast(API.handleAPIError(error, '加载失败'), 'error');
        }
    }

    /**
     * 渲染批次列表
     */
    function renderBatchList() {
        if (!elements.tableBody) return;

        if (state.batches.length === 0) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        <div class="empty-icon">📝</div>
                        <div class="empty-text">暂无历史记录</div>
                    </td>
                </tr>
            `;
            return;
        }

        const html = state.batches.map((batch, index) => {
            const statusClass = getStatusClass(batch.status);
            const statusText = getStatusText(batch.status);
            const createTime = formatDate(batch.createTime || batch.createdAt);
            const duration = formatDuration(batch.duration);
            const accuracy = batch.accuracy ? `${batch.accuracy}%` : '-';

            return `
                <tr class="batch-row" data-id="${batch.id}">
                    <td class="col-index">${(state.currentPage - 1) * state.pageSize + index + 1}</td>
                    <td class="col-name">
                        <span class="batch-name">${escapeHtml(batch.name || '未命名批次')}</span>
                        <span class="word-count">(${batch.wordCount || 0}词)</span>
                    </td>
                    <td class="col-status">
                        <span class="status-badge ${statusClass}">${statusText}</span>
                    </td>
                    <td class="col-accuracy">${accuracy}</td>
                    <td class="col-time">
                        <div class="create-time">${createTime}</div>
                        <div class="duration">用时: ${duration}</div>
                    </td>
                    <td class="col-actions">
                        <button class="btn btn-view" onclick="HistoryModule.viewDetail(${batch.id})">
                            查看详情
                        </button>
                        ${batch.status === 'completed' ? `
                            <button class="btn btn-retry" onclick="HistoryModule.retryBatch(${batch.id})">
                                重听
                            </button>
                        ` : ''}
                        ${batch.status !== 'in_progress' ? `
                            <button class="btn btn-delete" onclick="HistoryModule.deleteBatch(${batch.id})">
                                删除
                            </button>
                        ` : ''}
                    </td>
                </tr>
            `;
        }).join('');

        elements.tableBody.innerHTML = html;
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

        let html = '';

        // 上一页
        html += `
            <button class="page-btn prev ${state.currentPage <= 1 ? 'disabled' : ''}"
                    onclick="HistoryModule.loadBatches(${state.currentPage - 1})"
                    ${state.currentPage <= 1 ? 'disabled' : ''}>
                上一页
            </button>
        `;

        // 页码
        const startPage = Math.max(1, state.currentPage - 2);
        const endPage = Math.min(totalPages, state.currentPage + 2);

        if (startPage > 1) {
            html += `<button class="page-btn" onclick="HistoryModule.loadBatches(1)">1</button>`;
            if (startPage > 2) {
                html += `<span class="page-ellipsis">...</span>`;
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <button class="page-btn ${i === state.currentPage ? 'active' : ''}"
                        onclick="HistoryModule.loadBatches(${i})">
                    ${i}
                </button>
            `;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<span class="page-ellipsis">...</span>`;
            }
            html += `<button class="page-btn" onclick="HistoryModule.loadBatches(${totalPages})">${totalPages}</button>`;
        }

        // 下一页
        html += `
            <button class="page-btn next ${state.currentPage >= totalPages ? 'disabled' : ''}"
                    onclick="HistoryModule.loadBatches(${state.currentPage + 1})"
                    ${state.currentPage >= totalPages ? 'disabled' : ''}>
                下一页
            </button>
        `;

        elements.pagination.innerHTML = html;
    }

    /**
     * 查看详情
     * @param {number} batchId - 批次ID
     */
    async function viewDetail(batchId) {
        state.currentDetailId = batchId;

        try {
            const detail = await API.getBatchDetail(batchId);
            showDetailModal(detail);
        } catch (error) {
            console.error('获取详情失败:', error);
            showToast(API.handleAPIError(error, '获取详情失败'), 'error');
        }
    }

    /**
     * 显示详情模态框
     * @param {Object} detail - 批次详情数据
     */
    function showDetailModal(detail) {
        if (!elements.modal || !elements.modalContent) return;

        const wordsHtml = (detail.words || []).map(word => {
            const statusClass = word.isCorrect ? 'correct' : (word.isCorrect === false ? 'incorrect' : 'pending');
            const statusIcon = word.isCorrect ? '✓' : (word.isCorrect === false ? '✗' : '-');

            return `
                <tr class="word-row ${statusClass}">
                    <td class="word-text">${escapeHtml(word.word || word.wordText)}</td>
                    <td class="word-status">
                        <span class="status-icon">${statusIcon}</span>
                    </td>
                    <td class="word-duration">${word.duration || '-'}秒</td>
                    <td class="word-attempts">${word.attempts || '-'}</td>
                </tr>
            `;
        }).join('');

        const html = `
            <div class="detail-header">
                <h3>${escapeHtml(detail.name || '批次详情')}</h3>
                <button class="btn-close" onclick="HistoryModule.closeDetailModal()">&times;</button>
            </div>
            <div class="detail-summary">
                <div class="summary-item">
                    <span class="label">状态:</span>
                    <span class="value">${getStatusText(detail.status)}</span>
                </div>
                <div class="summary-item">
                    <span class="label">总词数:</span>
                    <span class="value">${detail.wordCount || detail.words?.length || 0}</span>
                </div>
                <div class="summary-item">
                    <span class="label">正确率:</span>
                    <span class="value">${detail.accuracy || 0}%</span>
                </div>
                <div class="summary-item">
                    <span class="label">总用时:</span>
                    <span class="value">${formatDuration(detail.duration)}</span>
                </div>
            </div>
            <div class="detail-words">
                <table class="words-table">
                    <thead>
                        <tr>
                            <th>词语</th>
                            <th>状态</th>
                            <th>用时</th>
                            <th>尝试次数</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${wordsHtml || '<tr><td colspan="4" class="empty">暂无词语</td></tr>'}
                    </tbody>
                </table>
            </div>
            <div class="detail-actions">
                ${detail.status === 'completed' ? `
                    <button class="btn btn-primary" onclick="HistoryModule.retryBatch(${detail.id})">
                        重新听写
                    </button>
                ` : ''}
                <button class="btn btn-secondary" onclick="HistoryModule.closeDetailModal()">
                    关闭
                </button>
            </div>
        `;

        elements.modalContent.innerHTML = html;
        elements.modal.style.display = 'flex';
    }

    /**
     * 关闭详情模态框
     */
    function closeDetailModal() {
        if (elements.modal) {
            elements.modal.style.display = 'none';
        }
        state.currentDetailId = null;
    }

    /**
     * 重听批次
     * @param {number} batchId - 批次ID
     */
    async function retryBatch(batchId) {
        if (callbacks.onStartDictation) {
            callbacks.onStartDictation(batchId);
        } else {
            // 默认行为：跳转到听写页面
            window.location.href = `/dictation.html?batchId=${batchId}`;
        }
    }

    /**
     * 删除批次
     * @param {number} batchId - 批次ID
     */
    async function deleteBatch(batchId) {
        if (!confirm('确定要删除这条记录吗？此操作不可恢复。')) {
            return;
        }

        try {
            await API.deleteBatch(batchId);
            showToast('删除成功', 'success');
            loadBatches(state.currentPage);
        } catch (error) {
            console.error('删除失败:', error);
            showToast(API.handleAPIError(error, '删除失败'), 'error');
        }
    }

    /**
     * 处理搜索
     */
    function handleSearch() {
        state.filters = {
            status: elements.statusFilter?.value || '',
            keyword: elements.keywordFilter?.value || '',
            startDate: elements.startDateFilter?.value || '',
            endDate: elements.endDateFilter?.value || ''
        };
        loadBatches(1);
    }

    /**
     * 处理重置
     */
    function handleReset() {
        if (elements.statusFilter) elements.statusFilter.value = '';
        if (elements.keywordFilter) elements.keywordFilter.value = '';
        if (elements.startDateFilter) elements.startDateFilter.value = '';
        if (elements.endDateFilter) elements.endDateFilter.value = '';

        state.filters = {
            status: '',
            keyword: '',
            startDate: '',
            endDate: ''
        };
        loadBatches(1);
    }

    /**
     * 获取状态样式类
     * @param {string} status - 状态
     * @returns {string} 样式类名
     */
    function getStatusClass(status) {
        const classMap = {
            'pending': 'status-pending',
            'in_progress': 'status-progress',
            'completed': 'status-completed',
            'cancelled': 'status-cancelled',
            'DRAFT': 'status-pending',
            'IN_PROGRESS': 'status-progress',
            'COMPLETED': 'status-completed'
        };
        return classMap[status] || 'status-pending';
    }

    /**
     * 获取状态文本
     * @param {string} status - 状态
     * @returns {string} 状态文本
     */
    function getStatusText(status) {
        const textMap = {
            'pending': '待开始',
            'in_progress': '进行中',
            'completed': '已完成',
            'cancelled': '已取消',
            'DRAFT': '草稿',
            'IN_PROGRESS': '进行中',
            'COMPLETED': '已完成'
        };
        return textMap[status] || '未知';
    }

    /**
     * 格式化日期
     * @param {string|Date} date - 日期
     * @returns {string} 格式化后的日期
     */
    function formatDate(date) {
        if (!date) return '-';
        const d = new Date(date);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }

    /**
     * 格式化时长
     * @param {number} seconds - 秒数
     * @returns {string} 格式化后的时长
     */
    function formatDuration(seconds) {
        if (!seconds) return '-';
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}分${secs}秒`;
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
     * @param {string} type - 类型：'success', 'error', 'warning'
     */
    function showToast(message, type = 'info') {
        // 如果有全局Toast组件则使用，否则使用alert
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
        loadBatches(state.currentPage);
    }

    // 公开API
    return {
        init,
        loadBatches,
        viewDetail,
        closeDetailModal,
        retryBatch,
        deleteBatch,
        refresh
    };
})();

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = HistoryModule;
} else {
    window.HistoryModule = HistoryModule;
}