import request from './request'

// 开始听写会话
export function startDictation(batchId) {
  return request({
    url: `/dictations/start/${batchId}`,
    method: 'post'
  })
}

// 记录词语听写结果
export function recordWordResult(data) {
  return request({
    url: '/dictations/word-result',
    method: 'post',
    data
  })
}

// 结束听写会话
export function endDictation(dictationId, data) {
  return request({
    url: `/dictations/${dictationId}/end`,
    method: 'post',
    data
  })
}

// 获取听写记录
export function getDictationHistory(params) {
  return request({
    url: '/dictations/history',
    method: 'get',
    params
  })
}

// 获取听写详情
export function getDictationDetail(id) {
  return request({
    url: `/dictations/${id}`,
    method: 'get'
  })
}

// 暂停听写
export function pauseDictation(dictationId) {
  return request({
    url: `/dictations/${dictationId}/pause`,
    method: 'post'
  })
}

// 继续听写
export function resumeDictation(dictationId) {
  return request({
    url: `/dictations/${dictationId}/resume`,
    method: 'post'
  })
}