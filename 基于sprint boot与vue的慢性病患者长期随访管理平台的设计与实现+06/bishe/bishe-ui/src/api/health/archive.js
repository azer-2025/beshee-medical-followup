import request from '@/utils/request'

// 查询电子健康档案列表
export function listArchive(query) {
  return request({
    url: '/health/archive/list',
    method: 'get',
    params: query
  })
}

// 查询电子健康档案详细
export function getArchive(archiveId) {
  return request({
    url: '/health/archive/' + archiveId,
    method: 'get'
  })
}

// 新增电子健康档案
export function addArchive(data) {
  return request({
    url: '/health/archive',
    method: 'post',
    data: data
  })
}

// 修改电子健康档案
export function updateArchive(data) {
  return request({
    url: '/health/archive',
    method: 'put',
    data: data
  })
}

// 删除电子健康档案
export function delArchive(archiveId) {
  return request({
    url: '/health/archive/' + archiveId,
    method: 'delete'
  })
}
