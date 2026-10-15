import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发配置:5173 端口 + 把 /api 开头的请求代理到后端 8000,解决跨域
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true,
      },
    },
  },
})
