import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let loadingInstance = null

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 显示加载动画
    if (config.showLoading !== false) {
      loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(255, 255, 255, 0.7)'
      })
    }

    // 从本地存储获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    if (loadingInstance) {
      loadingInstance.close()
    }
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    if (loadingInstance) {
      loadingInstance.close()
    }

    const res = response.data

    // 根据后端返回的状态码处理
    if (res.code && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    if (loadingInstance) {
      loadingInstance.close()
    }

    // 处理HTTP错误状态
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络错误，请检查网络连接'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request