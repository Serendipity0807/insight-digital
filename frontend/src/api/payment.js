import request from './request'

// 支付接口:与后端 mall-payment 一一对应。

/**
 * 发起支付。POST /api/v1/payment/pay,body = { orderId, channel: ALIPAY|WECHAT }
 * 返回 { paymentNo, amount(分), channel }——金额来自后端,前端只负责展示。
 * 幂等:重复发起返回同一张支付单,不产生新单。
 */
export function createPayment(data) {
  return request.post('/v1/payment/pay', data)
}

/**
 * 渠道支付成功回调(mock)。POST /api/v1/payment/callback
 * body = { paymentNo, tradeNo }
 * 真实场景这个请求由支付宝/微信的服务器直发后端(带签名验签),前端永远不发;
 * 这里是学习演示:用按钮模拟"渠道那边通知我们支付成功了"。
 */
export function mockCallback(data) {
  return request.post('/v1/payment/callback', data)
}
