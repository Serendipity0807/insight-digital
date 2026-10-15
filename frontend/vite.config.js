import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 前端开发服务器:端口 5173
// proxy:把 /api 开头的请求转发给后端 mall-product(8082),
// 开发环境绕开跨域(CORS);将来 mall-gateway(8080)上线后,把 target 改成 8080。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
