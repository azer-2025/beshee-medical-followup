import request from '@/utils/request'

// 查询随访计划列表
export function listPlan(query) {
  return request({
    url: '/followup/plan/list',
    method: 'get',
    params: query
  })
}

// 查询随访计划详细
export function getPlan(planId) {
  return request({
    url: '/followup/plan/' + planId,
    method: 'get'
  })
}

// 新增随访计划
export function addPlan(data) {
  return request({
    url: '/followup/plan',
    method: 'post',
    data: data
  })
}

// 修改随访计划
export function updatePlan(data) {
  return request({
    url: '/followup/plan',
    method: 'put',
    data: data
  })
}

// 删除随访计划
export function delPlan(planId) {
  return request({
    url: '/followup/plan/' + planId,
    method: 'delete'
  })
}
