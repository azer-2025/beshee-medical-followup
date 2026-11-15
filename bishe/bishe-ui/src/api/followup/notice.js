import request from '@/utils/request'

export function listNotice(query) {
  return request({
    url: '/followup/notice/list',
    method: 'get',
    params: query
  })
}

export function getNotice(noticeId) {
  return request({
    url: '/followup/notice/' + noticeId,
    method: 'get'
  })
}

export function addNotice(data) {
  return request({
    url: '/followup/notice',
    method: 'post',
    data: data
  })
}

export function updateNotice(data) {
  return request({
    url: '/followup/notice',
    method: 'put',
    data: data
  })
}

export function delNotice(noticeId) {
  return request({
    url: '/followup/notice/' + noticeId,
    method: 'delete'
  })
}
