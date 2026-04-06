/**
 * 历史记录页面逻辑
 */

(function() {
    'use strict';

    let batches = [];

    /**
     * 页面初始化
     */
    function init() {
        loadBatches();
        bindEvents();
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        const btnFilter = document.getElementById('btnFilter');
        if (btnFilter) {
            btnFilter.addEventListener('click', loadBatches);
        }
    }

    /**
     * 加载批次列表
     */
    async function loadBatches() {
        try {
            const filterDate = document.getElementById('filterDate')?.value;
            const filterStatus = document.getElementById('filterStatus')?.value;

            const params = {};
            if (filterDate) params.date = filterDate;
            if (filterStatus) params.status = filterStatus;

            const result = await API.getBatches(params);
            batches = result.data || [];
            renderBatches();
        } catch (error) {
            console.error('加载批次列表失败:', error);
        }
    }

    /**
     * 渲染批次列表
     */
    function renderBatches() {
        const container = document.getElementById('batchList');
        if (!container) return;

        if (batches.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-5">暂无听写记录</div>';
            return;
        }

        let html = '<div class="row">';
        batches.forEach(batch => {
            const statusBadge = getStatusBadge(batch.status);
            const progress = batch.totalWords > 0
                ? Math.round(batch.completedWords / batch.totalWords * 100)
                : 0;

            html += `
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="card history-card" onclick="showDetail(${batch.id})">
                        <div class="card-body">
                            <h5 class="card-title">${batch.batchName || '批次 #' + batch.id}</h5>
                            <p class="card-text text-muted">
                                <small>${formatDate(batch.createdAt)}</small>
                            </p>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span>${statusBadge}</span>
                                <span>${batch.completedWords}/${batch.totalWords}</span>
                            </div>
                            <div class="progress" style="height: 10px;">
                                <div class="progress-bar" style="width: ${progress}%"></div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        container.innerHTML = html;
    }

    /**
     * 显示批次详情
     */
    async function showDetail(batchId) {
        try {
            const result = await API.getBatchDetail(batchId);
            const batch = result.data;

            let html = `
                <div>
                    <h5>${batch.batchName || '批次 #' + batch.id}</h5>
                    <p class="text-muted">${formatDate(batch.createdAt)}</p>
                    <table class="table table-sm">
                        <thead><tr><th>词语</th><th>状态</th><th>耗时</th></tr></thead>
                        <tbody>
            `;

            if (batch.words) {
                batch.words.forEach(word => {
                    html += `
                        <tr>
                            <td>${word.wordText}</td>
                            <td>${getStatusBadge(word.status)}</td>
                            <td>${word.duration || '-'}秒</td>
                        </tr>
                    `;
                });
            }

            html += '</tbody></table></div>';

            document.getElementById('detailContent').innerHTML = html;

            const modal = new bootstrap.Modal(document.getElementById('detailModal'));
            modal.show();
        } catch (error) {
            console.error('加载批次详情失败:', error);
        }
    }

    /**
     * 获取状态徽章
     */
    function getStatusBadge(status) {
        const badges = {
            'DRAFT': '<span class="badge bg-secondary badge-status">草稿</span>',
            'IN_PROGRESS': '<span class="badge bg-warning badge-status">进行中</span>',
            'COMPLETED': '<span class="badge bg-success badge-status">已完成</span>',
            'PENDING': '<span class="badge bg-secondary badge-status">待听写</span>',
            'CURRENT': '<span class="badge bg-primary badge-status">当前</span>'
        };
        return badges[status] || '<span class="badge bg-secondary badge-status">' + status + '</span>';
    }

    /**
     * 格式化日期
     */
    function formatDate(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN');
    }

    // 页面加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // 导出全局方法
    window.showDetail = showDetail;
})();