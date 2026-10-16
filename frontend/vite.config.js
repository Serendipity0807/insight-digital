import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 前端开发服务器:端口 5173
// proxy:把 /api 开头的请求转发给网关 mall-gateway(8080)。
// 阶段③起「前端只认网关」:8080 → Nacos 查到 mall-product → 转发 8082。
// 开发环境绕开跨域(CORS)。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
