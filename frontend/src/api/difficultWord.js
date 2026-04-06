import request from './request'

// 获取生词列表
export function getDifficultWords(params) {
  return request({
    url: '/difficult-words',
    method: 'get',
    params
  })
}

// 添加生词
export function addDifficultWord(data) {
  return request({
    url: '/difficult-words',
    method: 'post',
    data
  })
}

// 更新生词信息
export function updateDifficultWord(id, data) {
  return request({
    url: `/difficult-words/${id}`,
    method: 'put',
    data
  })
}

// 删除生词
export function deleteDifficultWord(id) {
  return request({
    url: `/difficult-words/${id}`,
    method: 'delete'
  })
}

// 从生词本创建批次
export function createBatchFromDifficult(wordIds) {
  return request({
    url: '/difficult-words/create-batch',
    method: 'post',
    data: { wordIds }
  })
}

// 导出生词本
export function exportDifficultWords(format = 'txt') {
  return request({
    url: '/difficult-words/export',
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

// 更新掌握度
export function updateMastery(id, mastery) {
  return request({
    url: `/difficult-words/${id}/mastery`,
    method: 'put',
    data: { mastery }
  })
}

// 获取生词统计
export function getDifficultWordsStats() {
  return request({
    url: '/difficult-words/stats',
    method: 'get'
  })
}