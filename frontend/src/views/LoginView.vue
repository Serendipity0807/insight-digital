<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 登录 / 注册 双模式,一张表单复用
const mode = ref('login')
const form = reactive({ phone: '', password: '', nickname: '' })
const submitting = ref(false)
const error = ref('')
const tip = ref('')

const isLogin = () => mode.value === 'login'

function switchMode(m) {
  mode.value = m
  error.value = ''
  tip.value = ''
}

// 手机号前端预校验:和后端 @Pattern 同款正则,拦下明显非法输入,省一次请求
const phoneValid = () => /^1[3-9]\d{9}$/.test(form.phone)

async function submit() {
  error.value = ''
  tip.value = ''
  if (!phoneValid()) {
    error.value = '手机号格式不正确'
    return
  }
  if (form.password.length < 6 || form.password.length > 20) {
    error.value = '密码长度需在 6~20 位'
    return
  }

  submitting.value = true
  try {
    if (isLogin()) {
      await userStore.login(form.phone, form.password)
      // 登录成功后跳回"被拦下来之前想去的页面"(路由守卫放进 redirect 参数)
      router.push(route.query.redirect || '/')
    } else {
      await userStore.register({ ...form })
      tip.value = '注册成功,请登录'
      mode.value = 'login'
      form.password = ''
    }
  } catch (e) {
    error.value = e.message || '操作失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="card glass">
      <div class="brand">
        <h1>慧眼数码</h1>
        <p>见微知著 · 慧眼识机</p>
      </div>

      <!-- 登录/注册切换 -->
      <div class="tabs">
        <button
          class="tab"
          :class="{ active: isLogin() }"
          @click="switchMode('login')"
        >登录</button>
        <button
          class="tab"
          :class="{ active: !isLogin() }"
          @click="switchMode('register')"
        >注册</button>
      </div>

      <form class="form" @submit.prevent="submit">
        <label class="field">
          <span>手机号</span>
          <input
            v-model.trim="form.phone"
            type="tel"
            maxlength="11"
            placeholder="11 位手机号"
          />
        </label>

        <label v-if="!isLogin()" class="field">
          <span>昵称(选填)</span>
          <input
            v-model.trim="form.nickname"
            type="text"
            maxlength="50"
            placeholder="怎么称呼你"
          />
        </label>

        <label class="field">
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            maxlength="20"
            :placeholder="isLogin() ? '6~20 位密码' : '设置 6~20 位密码'"
          />
        </label>

        <p v-if="tip" class="msg success">{{ tip }}</p>
        <p v-if="error" class="msg danger">{{ error }}</p>

        <button class="btn-primary submit" type="submit" :disabled="submitting">
          {{ submitting ? '提交中…' : isLogin() ? '登录' : '注册' }}
        </button>
      </form>

      <p class="foot-note">
        未登录只能浏览商品;购物车 / 下单 / 支付都从这里出发
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  min-height: calc(100vh - var(--header-h) - 200px);
  display: grid;
  place-items: center;
  padding: var(--s-8) 0 var(--s-10);
}

.card {
  width: min(420px, 100%);
  padding: var(--s-8) var(--s-7);
  border-radius: var(--r-xl);
}

.brand { text-align: center; }
.brand h1 { font-size: 26px; font-weight: 300; letter-spacing: 6px; }
.brand p {
  margin-top: var(--s-2);
  font-size: 12px;
  letter-spacing: 3px;
  color: var(--c-text-faint);
}

.tabs {
  margin-top: var(--s-7);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-2);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 4px;
}
.tab {
  border: none;
  background: transparent;
  color: var(--c-text-dim);
  font-size: 14px;
  letter-spacing: 2px;
  padding: 10px 0;
  border-radius: var(--r-pill);
  transition:
    background var(--t-fast) var(--ease-out),
    color var(--t-fast) var(--ease-out);
}
.tab.active {
  background: var(--c-gradient);
  color: #fff;
}

.form { margin-top: var(--s-6); display: flex; flex-direction: column; gap: var(--s-4); }
.field { display: flex; flex-direction: column; gap: var(--s-2); }
.field span { font-size: 12px; color: var(--c-text-faint); letter-spacing: 1px; }
.field input {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
  padding: 13px 16px;
  font-size: 14px;
  color: var(--c-text);
  outline: none;
  transition: border-color var(--t-fast) var(--ease-out);
}
.field input:focus { border-color: var(--c-accent); }
.field input::placeholder { color: var(--c-text-faint); }

.msg { font-size: 13px; }
.success { color: var(--c-success); }
.danger { color: var(--c-danger); }

.submit { margin-top: var(--s-2); width: 100%; }

.foot-note {
  margin-top: var(--s-5);
  text-align: center;
  font-size: 12px;
  color: var(--c-text-faint);
}
</style>
