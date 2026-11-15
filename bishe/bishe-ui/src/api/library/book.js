import request from '@/utils/request'

export function listBook(query) {
  return request({
    url: '/library/book/list',
    method: 'get',
    params: query
  })
}

export function listPortalBook(query) {
  return request({
    url: '/library/book/portal/list',
    method: 'get',
    params: query
  })
}

export function getBook(bookId) {
  return request({
    url: '/library/book/' + bookId,
    method: 'get'
  })
}

export function addBook(data) {
  return request({
    url: '/library/book',
    method: 'post',
    data: data
  })
}

export function updateBook(data) {
  return request({
    url: '/library/book',
    method: 'put',
    data: data
  })
}

export function delBook(bookId) {
  return request({
    url: '/library/book/' + bookId,
    method: 'delete'
  })
}
