/**
 * reports.js - 报表页面逻辑模块
 * 使用ECharts实现图表展示和数据可视化
 */

const ReportsModule = (function() {
    // 状态
    let state = {
        currentType: 'daily',  // daily, weekly, monthly
        chartInstances: {},
        data: {
            daily: null,
            weekly: null,
            monthly: null
        }
    };

    // DOM元素缓存
    let elements = {
        container: null,
        tabButtons: null,
        overviewCards: null,
        accuracyChart: null,
        countChart: null,
        timeChart: null,
        trendChart: null
    };

    // 颜色配置
    const colors = {
        primary: '#4A90E2',
        success: '#7ED321',
        warning: '#F5A623',
        danger: '#E74C3C',
        info: '#9B59B6',
        gray: '#95A5A6'
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

        // 检查ECharts是否加载
        if (typeof echarts === 'undefined') {
            console.error('ECharts未加载，请确保引入ECharts库');
            return false;
        }

        // 初始化DOM元素引用
        initElements();

        // 绑定事件
        bindEvents();

        // 加载初始数据
        loadReportData(state.currentType);

        return true;
    }

    /**
     * 初始化DOM元素引用
     */
    function initElements() {
        elements.tabButtons = document.querySelectorAll('.report-tab-btn');
        elements.overviewCards = document.getElementById('overview-cards');
        elements.accuracyChart = document.getElementById('accuracy-chart');
        elements.countChart = document.getElementById('count-chart') || document.getElementById('weeklyChart');
        elements.timeChart = document.getElementById('time-chart');
        elements.trendChart = document.getElementById('trend-chart') || document.getElementById('monthlyChart');
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        // 标签切换
        elements.tabButtons?.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const type = e.target.dataset.type;
                switchReportType(type);
            });
        });

        // 监听Bootstrap Tab切换
        const tabs = document.querySelectorAll('a[data-bs-toggle="tab"]');
        tabs.forEach(tab => {
            tab.addEventListener('shown.bs.tab', function(e) {
                const target = e.target.getAttribute('href');
                if (target === '#weekly') {
                    switchReportType('weekly');
                } else if (target === '#monthly') {
                    switchReportType('monthly');
                } else if (target === '#daily') {
                    switchReportType('daily');
                }
            });
        });

        // 窗口大小改变时重绘图表
        window.addEventListener('resize', handleResize);
    }

    /**
     * 切换报表类型
     * @param {string} type - 类型：daily, weekly, monthly
     */
    function switchReportType(type) {
        if (type === state.currentType) return;

        state.currentType = type;

        // 更新标签状态
        elements.tabButtons?.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.type === type);
        });

        // 加载数据
        loadReportData(type);
    }

    /**
     * 加载报表数据
     * @param {string} type - 报表类型
     */
    async function loadReportData(type) {
        showLoading();

        try {
            const response = await API.getReports(type);
            state.data[type] = response;

            renderOverview(response.overview || response.data || response);
            renderCharts(response, type);
        } catch (error) {
            console.error('加载报表数据失败:', error);
            showToast(API.handleAPIError(error, '加载失败'), 'error');
            showEmptyState();
        } finally {
            hideLoading();
        }
    }

    /**
     * 渲染概览卡片
     * @param {Object} overview - 概览数据
     */
    function renderOverview(overview) {
        // 更新日报表统计数字
        const dailyWords = document.getElementById('dailyWords');
        const dailyAvgTime = document.getElementById('dailyAvgTime');
        const dailyRate = document.getElementById('dailyRate');

        if (dailyWords) dailyWords.textContent = overview.totalWords || 0;
        if (dailyAvgTime) dailyAvgTime.textContent = (overview.avgDuration || overview.avgDurationSeconds || 0) + 's';
        if (dailyRate) dailyRate.textContent = (overview.completionRate || overview.accuracy || 0) + '%';

        if (!elements.overviewCards) return;

        const cards = [
            {
                label: '听写次数',
                value: overview.totalSessions || overview.sessions || 0,
                icon: '📝',
                color: colors.primary
            },
            {
                label: '词语总数',
                value: overview.totalWords || overview.words || 0,
                icon: '📚',
                color: colors.success
            },
            {
                label: '平均正确率',
                value: `${overview.avgAccuracy || overview.accuracy || 0}%`,
                icon: '🎯',
                color: colors.warning
            },
            {
                label: '总用时',
                value: formatDuration(overview.totalDuration || overview.duration || 0),
                icon: '⏱️',
                color: colors.info
            }
        ];

        const html = cards.map(card => `
            <div class="overview-card">
                <div class="card-icon" style="background-color: ${card.color}20; color: ${card.color}">
                    ${card.icon}
                </div>
                <div class="card-content">
                    <div class="card-value">${card.value}</div>
                    <div class="card-label">${card.label}</div>
                </div>
            </div>
        `).join('');

        elements.overviewCards.innerHTML = html;
    }

    /**
     * 渲染图表
     * @param {Object} data - 数据
     * @param {string} type - 类型
     */
    function renderCharts(data, type) {
        // 销毁旧图表
        destroyCharts();

        const chartData = data.chartData || data.data?.chartData || [];
        const topErrors = data.topErrors || data.data?.topErrors || [];

        // 根据类型渲染不同图表
        if (type === 'weekly') {
            renderWeeklyChart(chartData);
            renderWeeklyErrors(topErrors);
        } else if (type === 'monthly') {
            renderMonthlyChart(chartData);
        } else {
            renderDailyChart(chartData);
        }
    }

    /**
     * 渲染周报表图表
     * @param {Array} chartData - 图表数据
     */
    function renderWeeklyChart(chartData) {
        const chartDom = elements.countChart;
        if (!chartDom) return;

        const chart = echarts.init(chartDom);
        state.chartInstances.weekly = chart;

        const dates = chartData.map(d => d.date);
        const counts = chartData.map(d => d.count || d.words || 0);

        const option = {
            title: {
                text: '本周听写趋势',
                left: 'center'
            },
            tooltip: {
                trigger: 'axis'
            },
            xAxis: {
                type: 'category',
                data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
            },
            yAxis: {
                type: 'value',
                name: '听写词语数'
            },
            series: [{
                name: '听写词语数',
                type: 'line',
                smooth: true,
                data: counts.length > 0 ? counts : [0, 0, 0, 0, 0, 0, 0],
                itemStyle: {
                    color: '#FF6B6B'
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: 'rgba(255,107,107,0.3)' },
                            { offset: 1, color: 'rgba(255,107,107,0.1)' }
                        ]
                    }
                }
            }]
        };

        chart.setOption(option);
    }

    /**
     * 渲染易错词语表格
     * @param {Array} topErrors - 易错词语列表
     */
    function renderWeeklyErrors(topErrors) {
        const tbody = document.getElementById('weeklyErrors');
        if (!tbody) return;

        if (topErrors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center">暂无数据</td></tr>';
            return;
        }

        let html = '';
        topErrors.forEach((item, index) => {
            html += `
                <tr>
                    <td>${index + 1}</td>
                    <td>${escapeHtml(item.wordText || item.word)}</td>
                    <td>${item.errorCount || item.wrongCount || 0}</td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    /**
     * 渲染月报表图表
     * @param {Array} chartData - 图表数据
     */
    function renderMonthlyChart(chartData) {
        const chartDom = elements.trendChart;
        if (!chartDom) return;

        const chart = echarts.init(chartDom);
        state.chartInstances.monthly = chart;

        const dates = chartData.map(d => d.date);
        const counts = chartData.map(d => d.count || d.words || 0);

        const option = {
            title: {
                text: '本月听写趋势',
                left: 'center'
            },
            tooltip: {
                trigger: 'axis'
            },
            xAxis: {
                type: 'category',
                data: dates,
                axisLabel: {
                    rotate: 45
                }
            },
            yAxis: {
                type: 'value',
                name: '听写词语数'
            },
            series: [{
                name: '听写词语数',
                type: 'bar',
                data: counts.length > 0 ? counts : [0],
                itemStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: '#FF6B6B' },
                            { offset: 1, color: '#4ECDC4' }
                        ]
                    }
                }
            }]
        };

        chart.setOption(option);
    }

    /**
     * 渲染日报表图表
     * @param {Array} chartData - 图表数据
     */
    function renderDailyChart(chartData) {
        // 日报表可以简单显示统计数字
        if (elements.countChart) {
            // 可以添加日报表的图表展示
        }
    }

    /**
     * 销毁图表
     */
    function destroyCharts() {
        Object.values(state.chartInstances).forEach(chart => {
            if (chart) {
                chart.dispose();
            }
        });
        state.chartInstances = {};
    }

    /**
     * 处理窗口大小改变
     */
    function handleResize() {
        Object.values(state.chartInstances).forEach(chart => {
            if (chart) {
                chart.resize();
            }
        });
    }

    /**
     * 显示加载状态
     */
    function showLoading() {
        if (elements.container) {
            elements.container.classList.add('loading');
        }
    }

    /**
     * 隐藏加载状态
     */
    function hideLoading() {
        if (elements.container) {
            elements.container.classList.remove('loading');
        }
    }

    /**
     * 显示空状态
     */
    function showEmptyState() {
        if (elements.overviewCards) {
            elements.overviewCards.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">📊</div>
                    <div class="empty-text">暂无数据</div>
                </div>
            `;
        }
    }

    /**
     * 格式化时长
     * @param {number} seconds - 秒数
     * @returns {string} 格式化后的时长
     */
    function formatDuration(seconds) {
        if (!seconds) return '0分钟';

        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;

        if (hours > 0) {
            return `${hours}小时${minutes}分钟`;
        } else if (minutes > 0) {
            return `${minutes}分钟`;
        } else {
            return `${secs}秒`;
        }
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
     * 刷新数据
     */
    function refresh() {
        loadReportData(state.currentType);
    }

    /**
     * 导出报表
     * @param {string} format - 格式：'csv', 'excel'
     */
    async function exportReport(format = 'excel') {
        try {
            const response = await API.exportReport(state.currentType, format);
            showToast('导出成功', 'success');
            // 如果返回的是文件URL，则下载
            if (response.url) {
                window.open(response.url, '_blank');
            }
        } catch (error) {
            console.error('导出失败:', error);
            showToast(API.handleAPIError(error, '导出失败'), 'error');
        }
    }

    /**
     * 销毁模块
     */
    function destroy() {
        destroyCharts();
        window.removeEventListener('resize', handleResize);
    }

    // 公开API
    return {
        init,
        switchReportType,
        refresh,
        exportReport,
        destroy
    };
})();

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ReportsModule;
} else {
    window.ReportsModule = ReportsModule;
}