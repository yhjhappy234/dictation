import request from './request'

// 创建听写批次
export function createBatch(data) {
  return request({
    url: '/batches',
    method: 'post',
    data
  })
}

// 获取批次列表
export function getBatches(params) {
  return request({
    url: '/batches',
    method: 'get',
    params
  })
}

// 获取批次详情
export function getBatchById(id) {
  return request({
    url: `/batches/${id}`,
    method: 'get'
  })
}

// 更新批次
export function updateBatch(id, data) {
  return request({
    url: `/batches/${id}`,
    method: 'put',
    data
  })
}

// 删除批次
export function deleteBatch(id) {
  return request({
    url: `/batches/${id}`,
    method: 'delete'
  })
}

// 完成批次
export function completeBatch(id, data) {
  return request({
    url: `/batches/${id}/complete`,
    method: 'post',
    data
  })
}