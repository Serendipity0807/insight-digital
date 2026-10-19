import request from './request'

// 通知接口:与后端 mall-notify 一一对应。

/** 我的站内信(分页,新的在前)。GET /api/v1/notify/message?pageNum=1&pageSize=20 */
export function listMessages(pageNum = 1, pageSize = 20) {
  return request.get('/v1/notify/message', { params: { pageNum, pageSize } })
}

/** 未读数(导航栏铃铛角标)。GET /api/v1/notify/unread-count */
export function getUnreadCount() {
  return request.get('/v1/notify/unread-count')
}

/** 标记已读(幂等:重复点不报错)。PUT /api/v1/notify/message/{id}/read */
export function markRead(id) {
  return request.put(`/v1/notify/message/${id}/read`)
}
