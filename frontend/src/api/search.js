import request from './request'

// 搜索接口:与后端 mall-search 一一对应。
// baseURL 是 /api,所以这里只写 /v1/search...。

/** 主搜索。GET /api/v1/search?keyword=&sort=&minPrice=&maxPrice=&page=&size= */
export function searchProducts(params) {
  return request.get('/v1/search', { params })
}

/** 热门搜索词。GET /api/v1/search/hot?size=8 */
export function getHotWords(size = 8) {
  return request.get('/v1/search/hot', { params: { size } })
}

/** 输入联想。GET /api/v1/search/suggest?prefix=xx */
export function suggest(prefix) {
  return request.get('/v1/search/suggest', { params: { prefix } })
}
