import request from './request'

// 商品接口:与后端 mall-product 一一对应。
// baseURL 是 /api,所以这里只写 /v1/... 即可。

/** 分页查询商品列表。GET /api/v1/product/page?page=1&size=12 */
export function getProductPage(params) {
  return request.get('/v1/product/page', { params })
}

/** 商品详情。GET /api/v1/product/{id} */
export function getProductDetail(id) {
  return request.get(`/v1/product/${id}`)
}
