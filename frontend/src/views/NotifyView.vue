<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMessages } from '../api/notify'
import { useNotifyStore } from '../stores/notify'
import { formatTime } from '../utils/format'

const router = useRouter()
const notifyStore = useNotifyStore()

const PAGE_SIZE = 20
const messages = ref([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(true)
const error = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))

async function load() {
  loading.value = true
  error.value = ''
  try {
    // 后端分页三件套:{ total, records } + pageNum/pageSize 参数
    const page = await listMessages(pageNum.value, PAGE_SIZE)
    messages.value = page.records
    total.value = page.total
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

watch(pageNum, () => {
  load()
  window.scrollTo({ top: 0 })   // 换页回顶部,不然翻页后视角还停在页面中部
})

// 点未读消息 = 已读:先改本地(界面立即响应),再调后端。
// 后端是幂等接口,失败基本是网络抖动;本地已乐观更新,角标靠 store 与轮询对齐
async function readMessage(msg) {
  if (msg.isRead === 1) return
  msg.isRead = 1
  try {
    await notifyStore.markRead(msg.id)
  } catch (e) {
    // 静默:下次 loadUnread 会把角标拉回真实值
  }
}

function prevPage() {
  if (pageNum.value > 1) pageNum.value--
}
function nextPage() {
  if (pageNum.value < totalPages.value) pageNum.value++
}

onMounted(() => {
  load()
  notifyStore.loadUnread()   // 进页面顺便校准一次角标
})
</script>

<template>
  <div class="notify">
    <section class="hero container">
      <p class="hero-kicker">MESSAGE CENTER</p>
      <h1 class="hero-title">消息<span class="text-accent">中心</span></h1>
      <p class="hero-sub">
        下单、关单的站内信都在这里
        <span v-if="notifyStore.unread" class="hero-unread">· {{ notifyStore.unread }} 条未读</span>
      </p>
    </section>

    <section class="container list">
      <!-- 加载骨架 -->
      <div v-if="loading" class="skeletons">
        <div v-for="i in 5" :key="i" class="msg-skeleton skeleton"></div>
      </div>

      <!-- 加载失败 -->
      <div v-else-if="error" class="state">
        <p>{{ error }}</p>
        <button class="btn-ghost glass" @click="load">重新加载</button>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!messages.length" class="state">
        <svg class="state-bell" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <p>暂无消息,下一单就有了</p>
        <button class="btn-ghost glass" @click="router.push('/')">去逛逛</button>
      </div>

      <!-- 消息列表 + 分页 -->
      <template v-else>
        <article
          v-for="msg in messages"
          :key="msg.id"
          class="msg-card glass"
          :class="{ unread: msg.isRead === 0 }"
          @click="readMessage(msg)"
        >
          <div class="msg-head">
            <span class="msg-dot" :class="{ on: msg.isRead === 0 }"></span>
            <h3 class="msg-title">{{ msg.title }}</h3>
            <time class="msg-time">{{ formatTime(msg.createdAt) }}</time>
          </div>
          <p class="msg-content">{{ msg.content }}</p>
        </article>

        <footer v-if="totalPages > 1" class="pager">
          <button class="page-btn" :disabled="pageNum <= 1" @click="prevPage">上一页</button>
          <span class="page-info num">{{ pageNum }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="pageNum >= totalPages" @click="nextPage">下一页</button>
        </footer>
      </template>
    </section>
  </div>
</template>

<style scoped>
/* ===== 页头 ===== */
.notify { padding: var(--s-8) 0 var(--s-12); }
.hero { margin-bottom: var(--s-7); }
.hero-kicker { font-size: 12px; letter-spacing: 4px; color: var(--c-text-faint); margin-bottom: var(--s-3); }
.hero-title { font-size: 30px; font-weight: 300; letter-spacing: 2px; }
.hero-sub { margin-top: var(--s-2); font-size: 13px; color: var(--c-text-dim); }
.hero-unread { color: var(--c-accent-2); }

/* ===== 列表 ===== */
.list { display: flex; flex-direction: column; gap: var(--s-4); }
.skeletons { display: flex; flex-direction: column; gap: var(--s-4); }
.msg-skeleton { height: 96px; border-radius: var(--r-lg); }
.state {
  padding: var(--s-12) 0;
  text-align: center;
  color: var(--c-text-dim);
  display: flex; flex-direction: column; align-items: center; gap: var(--s-5);
}
.state-bell { width: 52px; height: 52px; color: var(--c-text-faint); }
.state p { margin-bottom: var(--s-2); }

/* ===== 消息卡片 ===== */
.msg-card {
  border-radius: var(--r-lg);
  padding: var(--s-5) var(--s-6);
  cursor: pointer;
  transition: border-color var(--t-fast) var(--ease-out);
}
.msg-card:hover { border-color: var(--c-border-strong); }
/* 未读卡片:青色描边 + 更实的底,一眼从已读堆里跳出来 */
.msg-card.unread { border-color: rgba(33, 212, 196, 0.35); background: var(--c-glass-strong); }
.msg-head { display: flex; align-items: center; gap: var(--s-3); }
.msg-dot { width: 8px; height: 8px; border-radius: 50%; background: transparent; flex-shrink: 0; }
.msg-dot.on { background: var(--c-accent-2); box-shadow: 0 0 8px rgba(33, 212, 196, 0.7); }
.msg-title { font-size: 15px; font-weight: 600; }
.msg-card.unread .msg-title { color: var(--c-accent-2); }
.msg-time { margin-left: auto; font-size: 12px; color: var(--c-text-faint); }
.msg-content { margin-top: var(--s-2); font-size: 13px; color: var(--c-text-dim); line-height: 1.7; }

/* ===== 分页 ===== */
.pager {
  margin-top: var(--s-3);
  display: flex; align-items: center; justify-content: center; gap: var(--s-5);
}
.page-btn {
  padding: 9px 20px;
  font-size: 13px;
  color: var(--c-text-dim);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  transition: border-color var(--t-fast) var(--ease-out), color var(--t-fast) var(--ease-out);
}
.page-btn:hover:not(:disabled) { border-color: var(--c-accent); color: var(--c-text); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: 13px; color: var(--c-text-dim); }
</style>
