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

/** 批量查 SKU 摘要(秒杀页等按 skuId 组装商品名/图片/原价)。
 *  GET /api/v1/product/sku/batch?ids=10041,10042 —— 逗号分隔,后端自动转 List */
export function getSkuBatch(skuIds) {
  return request.get('/v1/product/sku/batch', { params: { ids: skuIds.join(',') } })
}
