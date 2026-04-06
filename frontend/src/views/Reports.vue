<template>
  <div class="reports-page">
    <!-- 时间范围选择器 -->
    <el-card class="time-selector-card" shadow="hover">
      <div class="time-selector">
        <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
          <el-radio-button value="day">今日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="custom">自定义</el-radio-button>
        </el-radio-group>

        <el-date-picker
          v-if="timeRange === 'custom'"
          v-model="customRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleCustomRangeChange"
        />
      </div>
    </el-card>

    <!-- 日报表 -->
    <el-card class="report-card daily-card" shadow="hover" v-if="timeRange === 'day'">
      <template #header>
        <div class="card-header">
          <el-icon :size="24" color="#409EFF"><Calendar /></el-icon>
          <span>今日听写报表</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :xs="12" :sm="4">
          <div class="daily-stat">
            <div class="stat-value">{{ dailyReport.wordCount }}</div>
            <div class="stat-label">听写词语数</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="4">
          <div class="daily-stat">
            <div class="stat-value">{{ dailyReport.avgTime }}秒</div>
            <div class="stat-label">平均耗时</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="4">
          <div class="daily-stat">
            <div class="stat-value">{{ dailyReport.accuracy }}%</div>
            <div class="stat-label">正确率</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="4">
          <div class="daily-stat">
            <div class="stat-value">{{ dailyReport.completedBatches }}</div>
            <div class="stat-label">完成批次</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="4">
          <div class="daily-stat">
            <div class="stat-value">{{ dailyReport.totalTime }}</div>
            <div class="stat-label">总耗时</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="4">
          <div class="daily-stat">
            <div class="stat-value">{{ dailyReport.newDifficult }}</div>
            <div class="stat-label">新增生词</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 周报表 -->
    <el-card class="report-card weekly-card" shadow="hover" v-if="timeRange === 'week'">
      <template #header>
        <div class="card-header">
          <el-icon :size="24" color="#67C23A"><Date /></el-icon>
          <span>本周听写报表</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="16">
          <div class="chart-container">
            <div class="chart-title">每日听写量趋势</div>
            <div ref="weeklyChartRef" class="chart-area"></div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="weekly-summary">
            <div class="summary-item">
              <el-statistic title="本周累计词语" :value="weeklyReport.totalWords" />
            </div>
            <div class="summary-item">
              <el-statistic title="本周累计批次" :value="weeklyReport.totalBatches" />
            </div>
            <div class="summary-item">
              <el-statistic title="本周总耗时" :value="formatMinutes(weeklyReport.totalMinutes)" />
            </div>
            <div class="summary-item">
              <el-statistic title="平均正确率" :value="weeklyReport.avgAccuracy" suffix="%" />
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 易错词语Top10 -->
      <el-divider>易错词语 Top10</el-divider>
      <div class="top-errors">
        <el-tag
          v-for="(word, index) in weeklyReport.topErrors"
          :key="index"
          class="error-tag"
          :type="getErrorTagType(index)"
        >
          <span class="error-word">{{ word.word }}</span>
          <span class="error-count">{{ word.count }}次错误</span>
        </el-tag>
      </div>
    </el-card>

    <!-- 月报表 -->
    <el-card class="report-card monthly-card" shadow="hover" v-if="timeRange === 'month'">
      <template #header>
        <div class="card-header">
          <el-icon :size="24" color="#E6A23C"><DataAnalysis /></el-icon>
          <span>月度趋势报表</span>
        </div>
      </template>

      <div class="chart-container full-width">
        <div class="chart-title">月度听写趋势</div>
        <div ref="monthlyChartRef" class="chart-area large"></div>
      </div>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <div class="monthly-stat">
            <el-progress
              type="dashboard"
              :percentage="monthlyReport.masteryRate"
              :color="progressColors"
            >
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">掌握率</span>
              </template>
            </el-progress>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="monthly-stat">
            <el-progress
              type="dashboard"
              :percentage="monthlyReport.practiceRate"
              :color="progressColors"
            >
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">练习完成率</span>
              </template>
            </el-progress>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="monthly-stat">
            <el-progress
              type="dashboard"
              :percentage="monthlyReport.improvementRate"
              :color="progressColors"
            >
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">进步幅度</span>
              </template>
            </el-progress>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 自定义时间范围报表 -->
    <el-card class="report-card custom-card" shadow="hover" v-if="timeRange === 'custom'">
      <template #header>
        <div class="card-header">
          <el-icon :size="24" color="#F56C6C"><Timer /></el-icon>
          <span>自定义时间范围报表</span>
          <span class="range-text" v-if="customRange">
            {{ customRange[0] }} 至 {{ customRange[1] }}
          </span>
        </div>
      </template>

      <el-empty v-if="!customRange" description="请选择时间范围" />

      <div v-else>
        <div ref="customChartRef" class="chart-area large"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDailyReport, getWeeklyReport, getMonthlyReport, getTrendData } from '@/api/report'

// 时间范围
const timeRange = ref('day')
const customRange = ref(null)

// 图表引用
const weeklyChartRef = ref(null)
const monthlyChartRef = ref(null)
const customChartRef = ref(null)

// 图表实例
let weeklyChart = null
let monthlyChart = null
let customChart = null

// 进度条颜色
const progressColors = [
  { color: '#F56C6C', percentage: 20 },
  { color: '#E6A23C', percentage: 40 },
  { color: '#409EFF', percentage: 60 },
  { color: '#67C23A', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
]

// 日报表数据
const dailyReport = ref({
  wordCount: 48,
  avgTime: 25,
  accuracy: 85,
  completedBatches: 3,
  totalTime: '20分钟',
  newDifficult: 2
})

// 周报表数据
const weeklyReport = ref({
  totalWords: 156,
  totalBatches: 12,
  totalMinutes: 180,
  avgAccuracy: 78,
  topErrors: [
    { word: '葡萄', count: 5 },
    { word: '樱桃', count: 4 },
    { word: '草莓', count: 3 },
    { word: '长颈鹿', count: 3 },
    { word: '熊猫', count: 2 },
    { word: '橘子', count: 2 },
    { word: '香蕉', count: 1 },
    { word: '西瓜', count: 1 },
    { word: '苹果', count: 1 },
    { word: '斑马', count: 1 }
  ]
})

// 月报表数据
const monthlyReport = ref({
  masteryRate: 65,
  practiceRate: 80,
  improvementRate: 45,
  dailyData: []
})

// 时间范围变化
const handleTimeRangeChange = async () => {
  await fetchReportData()
  await nextTick()
  initCharts()
}

// 自定义时间范围变化
const handleCustomRangeChange = async () => {
  if (customRange.value) {
    await fetchReportData()
    await nextTick()
    initCustomChart()
  }
}

// 获取报表数据
const fetchReportData = async () => {
  try {
    // 实际项目中调用API
    if (timeRange.value === 'day') {
      // const res = await getDailyReport()
      // dailyReport.value = res.data
    } else if (timeRange.value === 'week') {
      // const res = await getWeeklyReport()
      // weeklyReport.value = res.data
    } else if (timeRange.value === 'month') {
      // const res = await getMonthlyReport()
      // monthlyReport.value = res.data

      // 模拟数据
      monthlyReport.value.dailyData = generateMonthlyData()
    }
  } catch (error) {
    console.error('获取报表数据失败:', error)
  }
}

// 生成月度模拟数据
const generateMonthlyData = () => {
  const data = []
  for (let i = 1; i <= 30; i++) {
    data.push({
      day: i,
      words: Math.floor(Math.random() * 30) + 10,
      accuracy: Math.floor(Math.random() * 30) + 70,
      time: Math.floor(Math.random() * 20) + 10
    })
  }
  return data
}

// 格式化分钟数
const formatMinutes = (minutes) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return hours > 0 ? `${hours}小时${mins}分钟` : `${mins}分钟`
}

// 获取错误标签类型
const getErrorTagType = (index) => {
  if (index < 3) return 'danger'
  if (index < 6) return 'warning'
  return 'info'
}

// 初始化图表
const initCharts = () => {
  initWeeklyChart()
  initMonthlyChart()
}

// 初始化周图表
const initWeeklyChart = () => {
  if (!weeklyChartRef.value) return

  if (weeklyChart) {
    weeklyChart.dispose()
  }

  weeklyChart = echarts.init(weeklyChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLabel: {
        fontSize: 14
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 14
      }
    },
    series: [
      {
        name: '词语数',
        type: 'bar',
        data: [22, 18, 25, 30, 28, 35, 18],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        barWidth: '40%'
      }
    ]
  }

  weeklyChart.setOption(option)
}

// 初始化月图表
const initMonthlyChart = () => {
  if (!monthlyChartRef.value) return

  if (monthlyChart) {
    monthlyChart.dispose()
  }

  monthlyChart = echarts.init(monthlyChartRef.value)

  const data = monthlyReport.value.dailyData
  const days = data.map(d => `${d.day}日`)
  const words = data.map(d => d.words)
  const accuracy = data.map(d => d.accuracy)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['词语数', '正确率'],
      textStyle: {
        fontSize: 14
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days,
      axisLabel: {
        fontSize: 12,
        interval: 2
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '词语数',
        position: 'left',
        axisLabel: {
          fontSize: 12
        }
      },
      {
        type: 'value',
        name: '正确率',
        position: 'right',
        max: 100,
        axisLabel: {
          fontSize: 12,
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '词语数',
        type: 'line',
        smooth: true,
        data: words,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      },
      {
        name: '正确率',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: accuracy,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }

  monthlyChart.setOption(option)
}

// 初始化自定义图表
const initCustomChart = () => {
  if (!customChartRef.value || !customRange.value) return

  if (customChart) {
    customChart.dispose()
  }

  customChart = echarts.init(customChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['词语数', '正确率', '耗时']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['第1天', '第2天', '第3天', '第4天', '第5天', '第6天', '第7天']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '词语数',
        type: 'line',
        data: [20, 25, 30, 35, 28, 32, 40]
      },
      {
        name: '正确率',
        type: 'line',
        data: [75, 80, 82, 85, 88, 90, 92]
      },
      {
        name: '耗时',
        type: 'line',
        data: [15, 18, 20, 22, 19, 21, 25]
      }
    ]
  }

  customChart.setOption(option)
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  weeklyChart?.resize()
  monthlyChart?.resize()
  customChart?.resize()
}

// 监听时间范围变化
watch(timeRange, async () => {
  await nextTick()
  initCharts()
})

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchReportData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  weeklyChart?.dispose()
  monthlyChart?.dispose()
  customChart?.dispose()
})
</script>

<style lang="scss" scoped>
.reports-page {
  .time-selector-card {
    margin-bottom: 24px;
    border-radius: 16px;

    .time-selector {
      display: flex;
      align-items: center;
      gap: 20px;
      flex-wrap: wrap;
    }
  }
}

.report-card {
  border-radius: 16px;
  margin-bottom: 24px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 18px;
    font-weight: bold;

    .range-text {
      font-size: 14px;
      color: #909399;
      margin-left: 10px;
    }
  }
}

.daily-card {
  .daily-stat {
    text-align: center;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
    border-radius: 12px;
    margin-bottom: 10px;

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #409EFF;
    }

    .stat-label {
      font-size: 14px;
      color: #606266;
      margin-top: 8px;
    }
  }
}

.weekly-card {
  .chart-container {
    .chart-title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 16px;
    }

    .chart-area {
      height: 250px;
    }
  }

  .weekly-summary {
    .summary-item {
      padding: 16px;
      text-align: center;

      :deep(.el-statistic__head) {
        font-size: 14px;
        color: #909399;
      }

      :deep(.el-statistic__content) {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
    }
  }

  .top-errors {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .error-tag {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      font-size: 16px;

      .error-word {
        font-weight: 500;
      }

      .error-count {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
}

.monthly-card {
  .chart-container.full-width {
    .chart-area.large {
      height: 350px;
    }
  }

  .monthly-stat {
    display: flex;
    justify-content: center;
    padding: 20px;

    .percentage-value {
      font-size: 24px;
      font-weight: bold;
    }

    .percentage-label {
      font-size: 14px;
      color: #909399;
      display: block;
      margin-top: 4px;
    }
  }
}

.custom-card {
  .chart-area.large {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .time-selector {
    flex-direction: column;

    :deep(.el-radio-group) {
      width: 100%;
      display: flex;

      .el-radio-button {
        flex: 1;

        .el-radio-button__inner {
          width: 100%;
        }
      }
    }
  }

  .weekly-card {
    .weekly-summary {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .summary-item {
        flex: 1;
        min-width: 120px;
      }
    }
  }
}
</style>