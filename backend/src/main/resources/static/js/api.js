/**
 * API调用封装模块
 * 使用Fetch API调用后端REST接口
 */

const API = (function() {
    const BASE_URL = '/api';

    // 通用请求方法
    async function request(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const finalOptions = { ...defaultOptions, ...options };

        try {
            const response = await fetch(BASE_URL + url, finalOptions);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('API请求错误:', error);
            throw error;
        }
    }

    // GET请求
    async function get(url) {
        return request(url, { method: 'GET' });
    }

    // POST请求
    async function post(url, data) {
        return request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    // PUT请求
    async function put(url, data) {
        return request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    // DELETE请求
    async function del(url) {
        return request(url, { method: 'DELETE' });
    }

    // ========== 批次管理 ==========

    // 创建批次
    async function createBatch(words) {
        return post('/batches', { words: words });
    }

    // 获取批次列表
    async function getBatches(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return get(`/batches?${queryString}`);
    }

    // 获取批次详情
    async function getBatchDetail(id) {
        return get(`/batches/${id}`);
    }

    // 删除批次
    async function deleteBatch(id) {
        return del(`/batches/${id}`);
    }

    // ========== 词语管理 ==========

    // 获取批次词语列表
    async function getWords(batchId) {
        return get(`/words/batch/${batchId}`);
    }

    // 更新词语状态
    async function updateWordStatus(wordId, status) {
        return put(`/words/${wordId}/status`, { status: status });
    }

    // ========== 听写操作 ==========

    // 开始听写
    async function startDictation(batchId) {
        return post(`/records/start/${batchId}`);
    }

    // 下一个词语
    async function nextWord(batchId) {
        return post(`/records/next/${batchId}`);
    }

    // 上一个词语
    async function previousWord(batchId) {
        return post(`/records/previous/${batchId}`);
    }

    // 完成词语听写
    async function completeWord(wordId, duration, repeatCount = 0) {
        return post(`/records/complete/${wordId}`, {
            duration: duration,
            repeatCount: repeatCount
        });
    }

    // 结束听写
    async function endDictation(batchId) {
        return post(`/records/end/${batchId}`);
    }

    // ========== 生词本管理 ==========

    // 获取生词本列表
    async function getDifficultWords() {
        return get('/difficult-words');
    }

    // 添加生词
    async function addDifficultWord(wordId) {
        return post('/difficult-words', { wordId: wordId });
    }

    // 删除生词
    async function deleteDifficultWord(id) {
        return del(`/difficult-words/${id}`);
    }

    // ========== 报表统计 ==========

    // 获取日报表
    async function getDailyReport() {
        return get('/reports/daily');
    }

    // 获取周报表
    async function getWeeklyReport() {
        return get('/reports/weekly');
    }

    // 获取月报表
    async function getMonthlyReport() {
        return get('/reports/monthly');
    }

    // ========== 建议系统 ==========

    // 获取听写建议
    async function getSuggestions() {
        return get('/suggestions');
    }

    // 导出API方法
    return {
        // 批次
        createBatch,
        getBatches,
        getBatchDetail,
        deleteBatch,
        // 词语
        getWords,
        updateWordStatus,
        // 听写
        startDictation,
        nextWord,
        previousWord,
        completeWord,
        endDictation,
        // 生词本
        getDifficultWords,
        addDifficultWord,
        deleteDifficultWord,
        // 报表
        getDailyReport,
        getWeeklyReport,
        getMonthlyReport,
        // 建议
        getSuggestions
    };
})();

// 导出模块
window.API = API;