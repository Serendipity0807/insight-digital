import request from './request'

// 购物车接口:与后端 mall-cart 一一对应(存 Redis,需要登录态)。

/** 购物车列表。GET /api/v1/cart */
export function getCartList() {
  return request.get('/v1/cart')
}

/** 加入购物车。POST /api/v1/cart/items,body = { skuId, quantity } */
export function addCartItem(data) {
  return request.post('/v1/cart/items', data)
}

/** 修改数量。PUT /api/v1/cart/items/{skuId},body = { quantity } */
export function updateQuantity(skuId, data) {
  return request.put(`/v1/cart/items/${skuId}`, data)
}

/** 勾选/取消勾选。PUT /api/v1/cart/items/{skuId}/checked,body = { checked } */
export function updateChecked(skuId, data) {
  return request.put(`/v1/cart/items/${skuId}/checked`, data)
}

/** 删除条目。DELETE /api/v1/cart/items/{skuId} */
export function removeItem(skuId) {
  return request.delete(`/v1/cart/items/${skuId}`)
}
