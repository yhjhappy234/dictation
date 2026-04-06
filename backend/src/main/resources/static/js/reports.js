/**
 * 报表页面逻辑
 */

(function() {
    'use strict';

    let weeklyChart = null;
    let monthlyChart = null;

    /**
     * 页面初始化
     */
    function init() {
        loadDailyReport();
        bindEvents();
    }

    /**
     * 绑定事件
     */
    function bindEvents() {
        // 监听Tab切换
        const tabs = document.querySelectorAll('a[data-bs-toggle="tab"]');
        tabs.forEach(tab => {
            tab.addEventListener('shown.bs.tab', function(e) {
                const target = e.target.getAttribute('href');
                if (target === '#weekly') {
                    loadWeeklyReport();
                } else if (target === '#monthly') {
                    loadMonthlyReport();
                }
            });
        });
    }

    /**
     * 加载日报表
     */
    async function loadDailyReport() {
        try {
            const result = await API.getDailyReport();
            const data = result.data || {};

            document.getElementById('dailyWords').textContent = data.totalWords || 0;
            document.getElementById('dailyAvgTime').textContent = (data.avgDuration || 0) + 's';
            document.getElementById('dailyRate').textContent = (data.completionRate || 0) + '%';
        } catch (error) {
            console.error('加载日报表失败:', error);
        }
    }

    /**
     * 加载周报表
     */
    async function loadWeeklyReport() {
        try {
            const result = await API.getWeeklyReport();
            const data = result.data || {};

            // 渲染图表
            renderWeeklyChart(data.chartData || []);

            // 渲染易错词语表格
            renderWeeklyErrors(data.topErrors || []);
        } catch (error) {
            console.error('加载周报表失败:', error);
        }
    }

    /**
     * 渲染周报表图表
     */
    function renderWeeklyChart(chartData) {
        const chartDom = document.getElementById('weeklyChart');
        if (!chartDom) return;

        if (!weeklyChart) {
            weeklyChart = echarts.init(chartDom);
        }

        const dates = chartData.map(d => d.date);
        const counts = chartData.map(d => d.count);

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
                data: dates
            },
            yAxis: {
                type: 'value',
                name: '听写词语数'
            },
            series: [{
                name: '听写词语数',
                type: 'line',
                smooth: true,
                data: counts,
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

        weeklyChart.setOption(option);
    }

    /**
     * 渲染易错词语表格
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
                    <td>${item.wordText}</td>
                    <td>${item.errorCount}</td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    /**
     * 加载月报表
     */
    async function loadMonthlyReport() {
        try {
            const result = await API.getMonthlyReport();
            const data = result.data || {};

            renderMonthlyChart(data.chartData || []);
        } catch (error) {
            console.error('加载月报表失败:', error);
        }
    }

    /**
     * 渲染月报表图表
     */
    function renderMonthlyChart(chartData) {
        const chartDom = document.getElementById('monthlyChart');
        if (!chartDom) return;

        if (!monthlyChart) {
            monthlyChart = echarts.init(chartDom);
        }

        const dates = chartData.map(d => d.date);
        const counts = chartData.map(d => d.count);

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
                data: counts,
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

        monthlyChart.setOption(option);
    }

    // 页面加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();