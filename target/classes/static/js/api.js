/**
 * api.js - API调用封装模块
 * 封装所有后端接口调用，提供统一的错误处理和loading状态管理
 */

const APIModule = (function() {
    // API基础路径
    const BASE_URL = '/api';

    // 请求配置
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    };

    // Loading状态管理
    let loadingCount = 0;
    let loadingCallbacks = {
        onShow: null,
        onHide: null
    };

    /**
     * 显示Loading
     */
    function showLoading() {
        loadingCount++;
        if (loadingCallbacks.onShow) {
            loadingCallbacks.onShow();
        }
    }

    /**
     * 隐藏Loading
     */
    function hideLoading() {
        loadingCount--;
        if (loadingCount <= 0) {
            loadingCount = 0;
        }
        if (loadingCallbacks.onHide) {
            loadingCallbacks.onHide();
        }
    }

    /**
     * 设置Loading回调
     * @param {Object} callbacks - 回调对象
     */
    function setLoadingCallbacks(callbacks) {
        Object.assign(loadingCallbacks, callbacks);
    }

    /**
     * 统一请求方法
     * @param {string} url - 请求URL
     * @param {Object} options - 请求选项
     * @returns {Promise} 请求Promise
     */
    async function request(url, options = {}) {
        const fullUrl = url.startsWith('http') ? url : BASE_URL + url;
        const mergedOptions = { ...defaultOptions, ...options };

        // 如果有body且是对象，转换为JSON
        if (mergedOptions.body && typeof mergedOptions.body === 'object') {
            mergedOptions.body = JSON.stringify(mergedOptions.body);
        }

        showLoading();

        try {
            const response = await fetch(fullUrl, mergedOptions);

            // 检查响应状态
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new APIError(
                    errorData.message || `请求失败: ${response.status}`,
                    response.status,
                    errorData
                );
            }

            const data = await response.json();
            return data;
        } catch (error) {
            // 网络错误或其他错误
            if (error instanceof APIError) {
                throw error;
            }
            throw new APIError(error.message || '网络请求失败', 0, {});
        } finally {
            hideLoading();
        }
    }

    /**
     * GET请求
     * @param {string} url - 请求URL
     * @param {Object} params - 查询参数
     * @returns {Promise}
     */
    function get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;

        return request(fullUrl, {
            method: 'GET'
        });
    }

    /**
     * POST请求
     * @param {string} url - 请求URL
     * @param {Object} data - 请求数据
     * @returns {Promise}
     */
    function post(url, data = {}) {
        return request(url, {
            method: 'POST',
            body: data
        });
    }

    /**
     * PUT请求
     * @param {string} url - 请求URL
     * @param {Object} data - 请求数据
     * @returns {Promise}
     */
    function put(url, data = {}) {
        return request(url, {
            method: 'PUT',
            body: data
        });
    }

    /**
     * DELETE请求
     * @param {string} url - 请求URL
     * @returns {Promise}
     */
    function del(url) {
        return request(url, {
            method: 'DELETE'
        });
    }

    // ==================== 批次相关API ====================

    /**
     * 创建批次
     * @param {Array} words - 词语列表
     * @param {string} name - 批次名称
     * @returns {Promise}
     */
    function createBatch(words, name = '') {
        return post('/batches', {
            words: words,
            name: name
        });
    }

    /**
     * 获取批次列表
     * @param {Object} params - 查询参数
     * @param {number} params.page - 页码
     * @param {number} params.size - 每页数量
     * @param {string} params.status - 状态筛选
     * @returns {Promise}
     */
    function getBatches(params = {}) {
        return get('/batches', params);
    }

    /**
     * 获取批次详情
     * @param {number|string} id - 批次ID
     * @returns {Promise}
     */
    function getBatchDetail(id) {
        return get(`/batches/${id}`);
    }

    /**
     * 删除批次
     * @param {number|string} id - 批次ID
     * @returns {Promise}
     */
    function deleteBatch(id) {
        return del(`/batches/${id}`);
    }

    /**
     * 更新批次
     * @param {number|string} id - 批次ID
     * @param {Object} data - 更新数据
     * @returns {Promise}
     */
    function updateBatch(id, data) {
        return put(`/batches/${id}`, data);
    }

    // ==================== 听写相关API ====================

    /**
     * 开始听写
     * @param {number|string} batchId - 批次ID
     * @returns {Promise}
     */
    function startDictation(batchId) {
        return post(`/records/start/${batchId}`);
    }

    /**
     * 下一个词语
     * @param {number|string} batchId - 批次ID
     * @returns {Promise}
     */
    function nextWord(batchId) {
        return post(`/records/next/${batchId}`);
    }

    /**
     * 上一个词语
     * @param {number|string} batchId - 批次ID
     * @returns {Promise}
     */
    function previousWord(batchId) {
        return post(`/records/previous/${batchId}`);
    }

    /**
     * 完成词语
     * @param {number|string} wordId - 词语ID
     * @param {number} duration - 用时（秒）
     * @param {boolean} isCorrect - 是否正确
     * @returns {Promise}
     */
    function completeWord(wordId, duration, isCorrect = true) {
        return post(`/records/complete/${wordId}`, {
            duration: duration,
            isCorrect: isCorrect
        });
    }

    /**
     * 标记词语错误
     * @param {number|string} wordId - 词语ID
     * @returns {Promise}
     */
    function markWordIncorrect(wordId) {
        return post(`/records/word/${wordId}/incorrect`);
    }

    /**
     * 结束听写
     * @param {number|string} batchId - 批次ID
     * @returns {Promise}
     */
    function endDictation(batchId) {
        return post(`/records/end/${batchId}`);
    }

    /**
     * 获取听写状态
     * @param {number|string} batchId - 批次ID
     * @returns {Promise}
     */
    function getDictationStatus(batchId) {
        return get(`/records/${batchId}/status`);
    }

    // ==================== 生词本相关API ====================

    /**
     * 获取生词本列表
     * @param {Object} params - 查询参数
     * @param {number} params.page - 页码
     * @param {number} params.size - 每页数量
     * @param {number} params.minStars - 最小星级
     * @returns {Promise}
     */
    function getDifficultWords(params = {}) {
        return get('/difficult-words', params);
    }

    /**
     * 添加生词
     * @param {string} word - 词语
     * @param {number} stars - 星级（1-5）
     * @param {string} note - 备注
     * @returns {Promise}
     */
    function addDifficultWord(word, stars = 3, note = '') {
        return post('/difficult-words', {
            word: word,
            stars: stars,
            note: note
        });
    }

    /**
     * 更新生词
     * @param {number|string} id - 生词ID
     * @param {Object} data - 更新数据
     * @returns {Promise}
     */
    function updateDifficultWord(id, data) {
        return put(`/difficult-words/${id}`, data);
    }

    /**
     * 删除生词
     * @param {number|string} id - 生词ID
     * @returns {Promise}
     */
    function deleteDifficultWord(id) {
        return del(`/difficult-words/${id}`);
    }

    /**
     * 创建针对性练习批次
     * @param {Array} wordIds - 生词ID列表
     * @returns {Promise}
     */
    function createPracticeBatch(wordIds) {
        return post('/difficult-words/practice', {
            wordIds: wordIds
        });
    }

    // ==================== 报表相关API ====================

    /**
     * 获取报表数据
     * @param {string} type - 报表类型：'daily', 'weekly', 'monthly'
     * @param {Object} params - 额外参数
     * @returns {Promise}
     */
    function getReports(type, params = {}) {
        return get(`/reports/${type}`, params);
    }

    /**
     * 获取统计数据
     * @returns {Promise}
     */
    function getStatistics() {
        return get('/reports/statistics');
    }

    /**
     * 导出报表
     * @param {string} type - 报表类型
     * @param {string} format - 导出格式：'csv', 'excel'
     * @returns {Promise}
     */
    function exportReport(type, format = 'excel') {
        return get('/reports/export', { type, format });
    }

    // ==================== 用户设置相关API ====================

    /**
     * 获取用户设置
     * @returns {Promise}
     */
    function getSettings() {
        return get('/settings');
    }

    /**
     * 更新用户设置
     * @param {Object} settings - 设置对象
     * @returns {Promise}
     */
    function updateSettings(settings) {
        return put('/settings', settings);
    }

    // ==================== 辅助方法 ====================

    /**
     * 处理API错误
     * @param {Error} error - 错误对象
     * @param {string} defaultMessage - 默认错误消息
     * @returns {string} 用户友好的错误消息
     */
    function handleAPIError(error, defaultMessage = '操作失败') {
        if (error instanceof APIError) {
            switch (error.status) {
                case 401:
                    return '请先登录';
                case 403:
                    return '没有权限执行此操作';
                case 404:
                    return '请求的资源不存在';
                case 500:
                    return '服务器错误，请稍后重试';
                default:
                    return error.message || defaultMessage;
            }
        }
        return error.message || defaultMessage;
    }

    // 自定义错误类
    class APIError extends Error {
        constructor(message, status, data) {
            super(message);
            this.name = 'APIError';
            this.status = status;
            this.data = data;
        }
    }

    // 公开API
    return {
        // 通用方法
        request,
        get,
        post,
        put,
        del,
        setLoadingCallbacks,
        handleAPIError,
        APIError,

        // 批次相关
        createBatch,
        getBatches,
        getBatchDetail,
        deleteBatch,
        updateBatch,

        // 听写相关
        startDictation,
        nextWord,
        previousWord,
        completeWord,
        markWordIncorrect,
        endDictation,
        getDictationStatus,

        // 生词本相关
        getDifficultWords,
        addDifficultWord,
        updateDifficultWord,
        deleteDifficultWord,
        createPracticeBatch,

        // 报表相关
        getReports,
        getStatistics,
        exportReport,

        // 设置相关
        getSettings,
        updateSettings
    };
})();

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = APIModule;
} else {
    window.APIModule = APIModule;
    // 简短别名
    window.API = APIModule;
}