import axios from 'axios'

// 统一 axios 实例:所有后端请求都走这里。
// baseURL = '/api',配合 vite 代理(/api → http://localhost:8082)实现开发期同源,规避跨域。
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 响应拦截器:统一剥掉后端的 Result 壳 { code, message, data }。
// code === 200 时直接返回 data;否则抛错,由各视图 catch 后展示。
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 业务错误:比如 10201 商品不存在 / 10001 参数错误
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 网络错误 / 超时 / 后端未启动等
    const msg = error.response?.data?.message || error.message || '网络异常'
    return Promise.reject(new Error(msg))
  }
)

export default request
