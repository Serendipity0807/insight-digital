import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '../api/user'

// 本地存储键:加前缀防止和别的项目/插件冲突
const TOKEN_KEY = 'insight_token'
const NICKNAME_KEY = 'insight_nickname'
const USER_ID_KEY = 'insight_user_id'

/**
 * 登录态:token 是整个前端的"钥匙"。
 * 注意分工:request.js 的请求拦截器从 localStorage 读 token(避免 api/store 循环引用),
 * 本 store 是 token 唯一的"写入者"——登录/退出都从这里走,状态和本地存储保持一致。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const nickname = ref(localStorage.getItem(NICKNAME_KEY) || '')
  const userId = ref(localStorage.getItem(USER_ID_KEY) || '')

  const isLoggedIn = computed(() => !!token.value)

  /** 登录成功后落盘:token 必须持久化,否则刷新页面就"被登出"了 */
  function persist(data) {
    token.value = data.token
    nickname.value = data.nickname || ''
    userId.value = String(data.userId ?? '')
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(NICKNAME_KEY, nickname.value)
    localStorage.setItem(USER_ID_KEY, userId.value)
  }

  async function login(phone, password) {
    persist(await loginApi({ phone, password }))
  }

  /** 注册只拿 userId,不自动登录——用户切回登录页输入密码,流程更接近真实电商 */
  async function register(payload) {
    return registerApi(payload)
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    userId.value = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(NICKNAME_KEY)
    localStorage.removeItem(USER_ID_KEY)
  }

  return { token, nickname, userId, isLoggedIn, login, register, logout }
})
