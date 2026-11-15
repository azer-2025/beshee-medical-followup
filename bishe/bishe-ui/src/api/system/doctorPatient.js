import request from '@/utils/request'

// 查询医生名下的患者列表
export function listMyPatients(query) {
  return request({
    url: '/system/doctorPatient/list',
    method: 'get',
    params: query
  })
}

// 查询未绑定的患者列表
export function listUnboundPatients(query) {
  return request({
    url: '/system/doctorPatient/unboundList',
    method: 'get',
    params: query
  })
}

// 绑定患者
export function bindPatient(data) {
  return request({
    url: '/system/doctorPatient/bind',
    method: 'post',
    data: data
  })
}

// 解绑患者
export function unbindPatient(patientId, doctorId) {
  return request({
    url: '/system/doctorPatient/unbind/' + patientId,
    method: 'delete',
    params: { doctorId: doctorId }
  })
}

// 新增患者
export function addPatient(data) {
  return request({
    url: '/system/doctorPatient/add',
    method: 'post',
    data: data
  })
}

// 修改患者
export function updatePatient(data) {
  return request({
    url: '/system/doctorPatient/edit',
    method: 'put',
    data: data
  })
}

// 获取医生列表 (供患者选择)
export function listDoctors(query) {
  return request({
    url: '/system/doctorPatient/listDoctors',
    method: 'get',
    params: query
  })
}

// 获取我的主治医生
export function getMyDoctor() {
  return request({
    url: '/system/doctorPatient/myDoctor',
    method: 'get'
  })
}

// 患者选择主治医生
export function selectDoctor(data) {
  return request({
    url: '/system/doctorPatient/selectDoctor',
    method: 'post',
    data: data
  })
}
