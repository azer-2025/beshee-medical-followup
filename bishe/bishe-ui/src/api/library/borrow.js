import request from '@/utils/request'

export function listBorrow(query) {
  return request({
    url: '/library/borrow/list',
    method: 'get',
    params: query
  })
}

export function getBorrow(recordId) {
  return request({
    url: '/library/borrow/' + recordId,
    method: 'get'
  })
}

export function addBorrow(data) {
  return request({
    url: '/library/borrow',
    method: 'post',
    data: data
  })
}

export function updateBorrow(data) {
  return request({
    url: '/library/borrow',
    method: 'put',
    data: data
  })
}

export function returnBorrow(recordId) {
  return request({
    url: '/library/borrow/return/' + recordId,
    method: 'post'
  })
}

export function renewBorrow(recordId) {
  return request({
    url: '/library/borrow/renew/' + recordId,
    method: 'post'
  })
}

export function remindBorrow(recordId) {
  return request({
    url: '/library/borrow/remind/' + recordId,
    method: 'post'
  })
}

export function delBorrow(recordId) {
  return request({
    url: '/library/borrow/' + recordId,
    method: 'delete'
  })
}

export function listMyBorrow(query) {
  return request({
    url: '/library/borrow/my/list',
    method: 'get',
    params: query
  })
}

export function borrowBook(bookId) {
  return request({
    url: '/library/borrow/my/borrow/' + bookId,
    method: 'post'
  })
}

export function returnMyBorrow(recordId) {
  return request({
    url: '/library/borrow/my/return/' + recordId,
    method: 'post'
  })
}

export function renewMyBorrow(recordId) {
  return request({
    url: '/library/borrow/my/renew/' + recordId,
    method: 'post'
  })
}
