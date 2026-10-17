import request from './request'

// 订单接口:与后端 mall-order 一一对应。

/**
 * 创建订单。POST /api/v1/order/create
 * body = { items: [{ skuId, quantity }], address: { name, phone, detail }, remark }
 * 注意:body 里没有价格字段——金额永远由服务端算,前端传价格就是改价攻击。
 * 返回 { orderId, payAmount(分), expireTime(支付截止时间) }
 */
export function createOrder(data) {
  return request.post('/v1/order/create', data)
}
