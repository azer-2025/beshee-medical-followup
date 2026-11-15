import request from '@/utils/request'

// 查询随访记录列表
export function listRecord(query) {
  return request({
    url: '/followup/record/list',
    method: 'get',
    params: query
  })
}

// 查询随访记录详细
export function getRecord(recordId) {
  return request({
    url: '/followup/record/' + recordId,
    method: 'get'
  })
}

// 新增随访记录
export function addRecord(data) {
  return request({
    url: '/followup/record',
    method: 'post',
    data: data
  })
}

// 修改随访记录
export function updateRecord(data) {
  return request({
    url: '/followup/record',
    method: 'put',
    data: data
  })
}

// 删除随访记录
export function delRecord(recordId) {
  return request({
    url: '/followup/record/' + recordId,
    method: 'delete'
  })
}
