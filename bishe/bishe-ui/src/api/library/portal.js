import request from '@/utils/request'

export function getPortalSummary() {
  return request({
    url: '/library/portal/summary',
    method: 'get'
  })
}
