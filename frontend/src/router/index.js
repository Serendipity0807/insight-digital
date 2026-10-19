import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

// 路由表:与后端进度同步,开通一个挂一个
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    // 登录/注册:购物车、下单、支付的前置
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    // 详情页:参数可视化图表、慧眼点评、信任徽章、慧眼对比入口
    {
      path: '/product/:id',
      name: 'product-detail',
      // 懒加载:详情页代码在用户真正点进去时才下载,首屏更快
      component: () => import('../views/ProductDetailView.vue')
    },
    {
      path: '/compare',
      name: 'compare',
      component: () => import('../views/CompareView.vue')
    },
    // 秒杀页:浏览公开(不加 meta.auth),点抢购时由页面自己引导登录
    {
      path: '/seckill',
      name: 'seckill',
      component: () => import('../views/SeckillView.vue')
    },
    // meta.auth:需要登录的页面——没 token 由全局守卫送回登录页
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue'),
      meta: { auth: true }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('../views/CheckoutView.vue'),
      meta: { auth: true }
    },
    {
      path: '/pay/:orderId',
      name: 'pay',
      component: () => import('../views/PayView.vue'),
      meta: { auth: true }
    },
    // 消息中心:未登录由全局守卫送回登录页
    {
      path: '/notify',
      name: 'notify',
      component: () => import('../views/NotifyView.vue'),
      meta: { auth: true }
    }
  ],
  // 滚动行为:换页面回顶部;返回上一页恢复原位置;带锚点(#featured)滚到锚点。
// 修掉两个体验问题:①从首页中部点进详情页,视角还停在中间 ②"全部商品"锚点在别的页面失效
scrollBehavior(to, from, savedPosition) {
  if (to.hash) {
    return { el: to.hash, behavior: 'smooth' }
  }
  if (savedPosition) {
    return savedPosition
  }
  return { top: 0 }
}
})

// 全局前置守卫:meta.auth 的路由必须已登录。
// 拦截后带上 redirect 参数——登录页成功后跳回"被拦下来之前想去的页面"。
router.beforeEach((to) => {
  const token = localStorage.getItem('insight_token')
  if (to.meta.auth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
