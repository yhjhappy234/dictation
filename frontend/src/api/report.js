import request from './request'

// 获取日报表
export function getDailyReport(date) {
  return request({
    url: '/reports/daily',
    method: 'get',
    params: { date }
  })
}

// 获取周报表
export function getWeeklyReport(startDate, endDate) {
  return request({
    url: '/reports/weekly',
    method: 'get',
    params: { startDate, endDate }
  })
}

// 获取月报表
export function getMonthlyReport(year, month) {
  return request({
    url: '/reports/monthly',
    method: 'get',
    params: { year, month }
  })
}

// 获取趋势数据
export function getTrendData(params) {
  return request({
    url: '/reports/trend',
    method: 'get',
    params
  })
}

// 获取易错词语Top10
export function getTopErrors(limit = 10) {
  return request({
    url: '/reports/top-errors',
    method: 'get',
    params: { limit }
  })
}

// 获取统计数据概览
export function getStatisticsOverview() {
  return request({
    url: '/reports/overview',
    method: 'get'
  })
}