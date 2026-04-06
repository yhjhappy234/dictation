import request from './request'

// 获取词语列表
export function getWords(params) {
  return request({
    url: '/words',
    method: 'get',
    params
  })
}

// 添加词语
export function addWord(data) {
  return request({
    url: '/words',
    method: 'post',
    data
  })
}

// 批量添加词语
export function addWords(words) {
  return request({
    url: '/words/batch',
    method: 'post',
    data: { words }
  })
}

// 更新词语
export function updateWord(id, data) {
  return request({
    url: `/words/${id}`,
    method: 'put',
    data
  })
}

// 删除词语
export function deleteWord(id) {
  return request({
    url: `/words/${id}`,
    method: 'delete'
  })
}

// 搜索词语
export function searchWords(keyword) {
  return request({
    url: '/words/search',
    method: 'get',
    params: { keyword }
  })
}