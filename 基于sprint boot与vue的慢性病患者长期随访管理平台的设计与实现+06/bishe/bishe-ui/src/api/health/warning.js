import request from '@/utils/request'

// 查询健康预警列表
export function listWarning(query) {
  return request({
    url: '/health/warning/list',
    method: 'get',
    params: query
  })
}

// 查询健康预警详细
export function getWarning(warningId) {
  return request({
    url: '/health/warning/' + warningId,
    method: 'get'
  })
}

// 新增健康预警
export function addWarning(data) {
  return request({
    url: '/health/warning',
    method: 'post',
    data: data
  })
}

// 修改健康预警
export function updateWarning(data) {
  return request({
    url: '/health/warning',
    method: 'put',
    data: data
  })
}

// 修改预警已读状态
export function changeWarningReadStatus(warningId, isRead) {
  const data = {
    warningId,
    isRead
  }
  return request({
    url: '/health/warning/read',
    method: 'put',
    data: data
  })
}

// 删除健康预警
export function delWarning(warningId) {
  return request({
    url: '/health/warning/' + warningId,
    method: 'delete'
  })
}
