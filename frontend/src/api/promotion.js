import request from './request'

// 秒杀接口:与后端 mall-promotion 一一对应。

/** 秒杀场次列表。GET /api/v1/promotion/seckill/session/list */
export function listSeckillSessions() {
  return request.get('/v1/promotion/seckill/session/list')
}

/** 场次商品列表(stock 是实时剩余)。GET /api/v1/promotion/seckill/{sessionId}/goods */
export function listSeckillGoods(sessionId) {
  return request.get(`/v1/promotion/seckill/${sessionId}/goods`)
}

/** 秒杀下单:立即返回"排队中"+ requestId(取号单),真结果靠轮询。
 *  POST /api/v1/promotion/seckill/{sessionId}/order,body = { itemId } */
export function createSeckillOrder(sessionId, itemId) {
  return request.post(`/v1/promotion/seckill/${sessionId}/order`, { itemId })
}

/** 结果轮询。GET /api/v1/promotion/seckill/{sessionId}/result?requestId=xxx */
export function getSeckillResult(sessionId, requestId) {
  return request.get(`/v1/promotion/seckill/${sessionId}/result`, { params: { requestId } })
}
