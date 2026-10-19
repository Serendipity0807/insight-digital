<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useCartStore } from '../../stores/cart'
import { getHotWords, suggest } from '../../api/search'

const router = useRouter()
// 登录态 + 购物车角标:都从 store 读,全站唯一
const userStore = useUserStore()
const cartStore = useCartStore()

// 搜索:回车后跳首页带 ?kw=,由首页按关键字重新拉列表
const searchKw = ref('')
function doSearch() {
  const kw = searchKw.value.trim()
  showPanel.value = false
  router.push({ path: '/', query: kw ? { kw } : {} })
}

// —— 搜索面板:输入为空给"热门搜索",有输入给"联想" ——
const hotWords = ref([])
const suggestions = ref([])
const showPanel = ref(false)

// 防抖:停止输入 250ms 才发联想请求,不每敲一个字打一次后端
let suggestTimer = null
function refreshPanel() {
  const kw = searchKw.value.trim()
  clearTimeout(suggestTimer)
  if (!kw) {
    loadHotWords()
    return
  }
  suggestTimer = setTimeout(async () => {
    try {
      suggestions.value = await suggest(kw)
    } catch (e) {
      suggestions.value = []
    }
  }, 250)
}

async function loadHotWords() {
  try {
    hotWords.value = await getHotWords(8)
  } catch (e) {
    hotWords.value = []
  }
}

function onFocus() {
  showPanel.value = true
  refreshPanel()
}

// 失焦延迟关闭,给"点选"留出时间(否则 blur 先关面板,点击落空)
let blurTimer = null
function onBlur() {
  blurTimer = setTimeout(() => (showPanel.value = false), 150)
}

// 点选热词/联想词:填入输入框并直接搜索
function pick(word) {
  searchKw.value = word
  doSearch()
}

// 面板开着时,输入变化就刷新(热词 ↔ 联想自动切换)
watch(searchKw, () => {
  if (showPanel.value) refreshPanel()
})

// 登录成功 → 拉一次购物车,角标立即有数
watch(
  () => userStore.isLoggedIn,
  (on) => {
    if (on) cartStore.load()
  }
)
onMounted(() => {
  if (userStore.isLoggedIn) cartStore.load()
})
onBeforeUnmount(() => {
  clearTimeout(suggestTimer)
  clearTimeout(blurTimer)
})

function handleLogout() {
  userStore.logout()
  cartStore.clear()
}
</script>

<template>
  <header class="header">
    <div class="container header-inner">
      <RouterLink to="/" class="logo">
        <svg class="logo-mark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <!-- "慧眼":一只镜头光圈组成的眼睛 -->
          <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
        <span class="logo-text">
          <strong>慧眼数码</strong>
          <small>INSIGHT DIGITAL</small>
        </span>
      </RouterLink>

      <nav class="nav">
        <RouterLink to="/" class="nav-link">首页</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#featured' }" class="nav-link">全部商品</RouterLink>
        <RouterLink to="/compare" class="nav-link">慧眼对比</RouterLink>
      </nav>

      <div class="actions">
        <div class="search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" /></svg>
          <input v-model.trim="searchKw" type="text" placeholder="搜索商品,回车"
                 @keyup.enter="doSearch" @focus="onFocus" @blur="onBlur" />
          <!-- 热词 / 联想面板 -->
          <div v-if="showPanel && (hotWords.length || suggestions.length)" class="search-panel">
            <p class="panel-title">{{ searchKw.trim() ? '联想' : '热门搜索' }}</p>
            <div v-if="searchKw.trim()" class="panel-suggests">
              <button v-for="s in suggestions" :key="s" class="panel-item" @mousedown.prevent="pick(s)">{{ s }}</button>
            </div>
            <div v-else class="panel-hots">
              <button v-for="h in hotWords" :key="h.word" class="hot-chip" @mousedown.prevent="pick(h.word)">{{ h.word }}</button>
            </div>
          </div>
        </div>
        <RouterLink to="/cart" class="cart-btn" title="购物车">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" /><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" /></svg>
          <span v-if="cartStore.count" class="badge">{{ cartStore.count }}</span>
        </RouterLink>

        <!-- 用户区:登录前给入口,登录后给昵称 + 退出 -->
        <div class="user-area">
          <template v-if="userStore.isLoggedIn">
            <span class="nickname">{{ userStore.nickname || '用户' }}</span>
            <button class="logout-btn" @click="handleLogout">退出</button>
          </template>
          <RouterLink v-else to="/login" class="login-link">登录</RouterLink>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-h);
  z-index: 100;
  background: rgba(11, 14, 18, 0.65);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border-bottom: 1px solid var(--c-border);
}
.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--s-8);
}

/* —— Logo —— */
.logo { display: flex; align-items: center; gap: var(--s-3); }
.logo-mark { width: 26px; height: 26px; color: var(--c-accent-2); }
.logo-text { display: flex; flex-direction: column; line-height: 1.15; }
.logo-text strong { font-size: 17px; letter-spacing: 2px; }
.logo-text small { font-size: 10px; color: var(--c-text-faint); letter-spacing: 2.5px; }

/* —— 导航 —— */
.nav { display: flex; gap: var(--s-7); margin-left: auto; }
.nav-link {
  font-size: 14px;
  color: var(--c-text-dim);
  transition: color var(--t-fast) var(--ease-out);
}
.nav-link:hover { color: var(--c-text); }
.router-link-active { color: var(--c-text); }

/* —— 右侧:搜索 / 购物车 / 用户 —— */
.actions { display: flex; align-items: center; gap: var(--s-3); }
.search {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--s-2);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 8px 16px;
  transition: border-color var(--t-fast) var(--ease-out);
}
.search:focus-within { border-color: var(--c-accent); }
.search svg { width: 16px; height: 16px; color: var(--c-text-faint); }
.search input {
  background: none;
  border: none;
  outline: none;
  color: var(--c-text);
  font-size: 13px;
  width: 160px;
}
.search input::placeholder { color: var(--c-text-faint); }

/* —— 搜索面板:热门搜索 / 联想 —— */
.search-panel {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  min-width: 280px;
  background: var(--c-bg-raised);
  border: 1px solid var(--c-border-strong);
  border-radius: var(--r-md);
  padding: var(--s-4);
  box-shadow: var(--glow-card);
  z-index: 130;
}
.panel-title {
  font-size: 11px;
  letter-spacing: 2px;
  color: var(--c-text-faint);
  margin-bottom: var(--s-3);
}
.panel-suggests { display: flex; flex-direction: column; gap: 2px; }
.panel-item {
  text-align: left;
  padding: 8px 10px;
  font-size: 13px;
  color: var(--c-text-dim);
  background: none;
  border: none;
  border-radius: var(--r-sm);
  transition: background var(--t-fast) var(--ease-out), color var(--t-fast) var(--ease-out);
}
.panel-item:hover { background: var(--c-glass-strong); color: var(--c-text); }
.panel-hots { display: flex; flex-wrap: wrap; gap: var(--s-2); }
.hot-chip {
  font-size: 12px;
  color: var(--c-text-dim);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 5px 12px;
  transition: color var(--t-fast) var(--ease-out), border-color var(--t-fast) var(--ease-out);
}
.hot-chip:hover { color: var(--c-accent-2); border-color: rgba(33, 212, 196, 0.5); }

.cart-btn {
  position: relative;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  color: var(--c-text);
  transition: border-color var(--t-fast) var(--ease-out);
}
.cart-btn:hover { border-color: var(--c-accent); }
.cart-btn svg { width: 18px; height: 18px; }
.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: var(--c-accent);
  border-radius: var(--r-pill);
  box-shadow: var(--glow-accent);
}

/* —— 用户区 —— */
.user-area {
  display: flex;
  align-items: center;
  gap: var(--s-3);
  padding-left: var(--s-2);
}
.login-link {
  font-size: 14px;
  color: var(--c-text-dim);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 8px 18px;
  transition:
    border-color var(--t-fast) var(--ease-out),
    color var(--t-fast) var(--ease-out);
}
.login-link:hover { border-color: var(--c-accent); color: var(--c-text); }
.nickname { font-size: 13px; color: var(--c-accent-2); }
.logout-btn {
  font-size: 12px;
  color: var(--c-text-faint);
  background: transparent;
  border: none;
  transition: color var(--t-fast) var(--ease-out);
}
.logout-btn:hover { color: var(--c-danger); }

@media (max-width: 860px) {
  .nav { display: none; }
  .search input { width: 100px; }
}
</style>
