import axios from 'axios'

// 统一 axios 实例:所有后端请求都走这里。
// baseURL = '/api',配合 vite 代理(/api → http://localhost:8082)实现开发期同源,规避跨域。
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  // 雪花 ID 精度保护:后端 Long(19 位)超出 JS Number 安全整数上限 2^53(约 9 千万亿,16 位),
  // JSON.parse 会把 1948453690123456789 解析成 1948453690123456800(末尾丢精度)——
  // 拿着"四舍五入过"的 orderId 去支付,查单必然 10701"订单不存在"。
  // 这里在 parse 前把 ≥16 位的整数用引号包起来,让它解析成字符串:字符串不丢精度,
  // 后端 Jackson 反序列化"字符串 → Long"完全兼容。只匹配 ≥16 位:金额(分)、
  // 数量、状态这些业务数字远没这么大,不受影响(例如 amount=579900 只有 6 位)。
  transformResponse: [
    (data) => {
      if (typeof data === 'string') {
        return JSON.parse(data.replace(/:(\d{16,})([,\}\]])/g, ':"$1"$2'))
      }
      return data
    }
  ]
})

// 请求拦截器:每个请求自动带上登录 token。
// 这里直接从 localStorage 读,而不是从 user store 读——store 引 api、api 再引 store 会循环引用。
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('insight_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
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
      // 网关 401:未登录 / token 过期 → 清掉本地登录态,送回登录页(带原地址,登录后跳回)
      if (error.response?.status === 401) {
        localStorage.removeItem('insight_token')
        localStorage.removeItem('insight_nickname')
        localStorage.removeItem('insight_user_id')
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href =
              '/login?redirect=' +
              encodeURIComponent(window.location.pathname + window.location.search)
        }
      }
      // 网络错误 / 超时 / 后端未启动等
      const msg = error.response?.data?.message || error.message || '网络异常'
      return Promise.reject(new Error(msg))
    }
)

export default request
