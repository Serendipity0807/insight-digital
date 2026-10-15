import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

// 路由表:与后端进度同步,开通一个挂一个
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    // 详情页:第 2 步填充(参数可视化图表、慧眼点评、信任徽章、慧眼对比入口)
    {
      path: '/product/:id',
      name: 'product-detail',
      // 懒加载:详情页代码在用户真正点进去时才下载,首屏更快
      component: () => import('../views/ProductDetailView.vue')
    }
    // 预留:/compare 慧眼对比(第 3 步)、/cart 购物车(第 4 步)、/checkout 结算(第 5 步)
  ]
})

export default router
