/**
 * 生词本页面逻辑
 */

(function() {
    'use strict';

    let difficultWords = [];

    /**
     * 页面初始化
     */
    function init() {
        loadDifficultWords();
        bindEvents();
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        const btnExport = document.getElementById('btnExport');
        const btnPractice = document.getElementById('btnPractice');

        if (btnExport) {
            btnExport.addEventListener('click', exportWords);
        }
        if (btnPractice) {
            btnPractice.addEventListener('click', startPractice);
        }
    }

    /**
     * 加载生词列表
     */
    async function loadDifficultWords() {
        try {
            const result = await API.getDifficultWords();
            difficultWords = result.data || [];
            renderTable();
        } catch (error) {
            console.error('加载生词列表失败:', error);
        }
    }

    /**
     * 渲染表格
     */
    function renderTable() {
        const tbody = document.getElementById('difficultWordsTable');
        if (!tbody) return;

        if (difficultWords.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">暂无生词</td></tr>';
            return;
        }

        let html = '';
        difficultWords.forEach(word => {
            const stars = getStars(word.masteryLevel || 1);
            html += `
                <tr>
                    <td><strong>${word.wordText}</strong></td>
                    <td>${word.errorCount || 0}</td>
                    <td>${word.avgDurationSeconds || 0}秒</td>
                    <td><span class="star-rating">${stars}</span></td>
                    <td>${formatDate(word.lastPracticeDate)}</td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="deleteWord(${word.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    /**
     * 获取星级显示
     */
    function getStars(level) {
        let stars = '';
        for (let i = 0; i < 5; i++) {
            stars += i < level ? '★' : '☆';
        }
        return stars;
    }

    /**
     * 格式化日期
     */
    function formatDate(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-CN');
    }

    /**
     * 导出生词本
     */
    function exportWords() {
        if (difficultWords.length === 0) {
            alert('暂无生词可导出');
            return;
        }

        let text = '生词本\n\n';
        difficultWords.forEach((word, index) => {
            text += `${index + 1}. ${word.wordText}\n`;
            text += `   错误次数: ${word.errorCount || 0}\n`;
            text += `   掌握度: ${getStars(word.masteryLevel || 1)}\n\n`;
        });

        // 创建下载
        const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = '生词本_' + new Date().toLocaleDateString('zh-CN') + '.txt';
        a.click();
        URL.revokeObjectURL(url);
    }

    /**
     * 开始针对性练习
     */
    async function startPractice() {
        if (difficultWords.length === 0) {
            alert('暂无生词，请先进行听写练习');
            return;
        }

        const words = difficultWords.map(w => w.wordText);

        try {
            const result = await API.createBatch(words);
            alert('已创建针对性练习批次，请前往首页开始听写');
            window.location.href = '/';
        } catch (error) {
            console.error('创建练习批次失败:', error);
            alert('创建练习批次失败');
        }
    }

    /**
     * 删除生词
     */
    async function deleteWord(id) {
        if (!confirm('确定要删除这个生词吗？')) return;

        try {
            await API.deleteDifficultWord(id);
            loadDifficultWords();
        } catch (error) {
            console.error('删除生词失败:', error);
        }
    }

    // 页面加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // 导出全局方法
    window.deleteWord = deleteWord;
})();