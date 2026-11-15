import request from '@/utils/request'

// 查询健康数据采集列表
export function listData(query) {
  return request({
    url: '/health/data/list',
    method: 'get',
    params: query
  })
}

// 查询健康数据采集详细
export function getData(dataId) {
  return request({
    url: '/health/data/' + dataId,
    method: 'get'
  })
}

// 新增健康数据采集
export function addData(data) {
  return request({
    url: '/health/data',
    method: 'post',
    data: data
  })
}

// 修改健康数据采集
export function updateData(data) {
  return request({
    url: '/health/data',
    method: 'put',
    data: data
  })
}

// 删除健康数据采集
export function delData(dataId) {
  return request({
    url: '/health/data/' + dataId,
    method: 'delete'
  })
}
