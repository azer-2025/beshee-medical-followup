import request from '@/utils/request'

export function listReader(query) {
  return request({
    url: '/library/reader/list',
    method: 'get',
    params: query
  })
}

export function getReader(readerId) {
  return request({
    url: '/library/reader/' + readerId,
    method: 'get'
  })
}

export function addReader(data) {
  return request({
    url: '/library/reader',
    method: 'post',
    data: data
  })
}

export function updateReader(data) {
  return request({
    url: '/library/reader',
    method: 'put',
    data: data
  })
}

export function delReader(readerId) {
  return request({
    url: '/library/reader/' + readerId,
    method: 'delete'
  })
}

export function getReaderProfile() {
  return request({
    url: '/library/reader/profile',
    method: 'get'
  })
}
