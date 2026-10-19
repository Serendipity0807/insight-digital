<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { listSeckillSessions, listSeckillGoods, createSeckillOrder, getSeckillResult } from '../api/promotion'
import { getSkuBatch } from '../api/product'
import { formatPrice } from '../utils/format'

const router = useRouter()

// —— 场次 + 商品 ——
const sessions = ref([])
const activeSessionId = ref(null)
const goods = ref([])
const loading = ref(true)
const error = ref('')

const activeSession = computed(
  () => sessions.value.find((s) => s.id === activeSessionId.value) || null
)

// —— 倒计时:一个每秒推进的 now,驱动所有时间相关展示 ——
const now = ref(Date.now())
let ticker = null

// 场次阶段:后端只给开始/结束时间,状态由前端算。
// 客户端时钟不准没关系——真正下单时后端还会再校验一次时间窗口,这里只是"展示层"。
const phase = computed(() => {
  const s = activeSession.value
  if (!s) return { state: 'idle', label: '', target: 0 }
  // 后端 LocalDateTime 序列化成 "2026-10-18T20:00:00"(无时区后缀),
  // JS 按本地时区解析——与后端同机部署天然一致;跨时区要改成带时区的格式。
  const start = new Date(s.startTime).getTime()
  const end = new Date(s.endTime).getTime()
  if (now.value < start) return { state: 'upcoming', label: '距开始', target: start }
  if (now.value <= end) return { state: 'ongoing', label: '距结束', target: end }
  return { state: 'ended', label: '已结束', target: 0 }
})

const countdown = computed(() => {
  const p = phase.value
  if (p.state === 'ended') return ['00', '00', '00']
  const diff = Math.max(0, p.target - now.value)
  const pad = (n) => String(n).padStart(2, '0')
  return [
    pad(Math.floor(diff / 3600000)),
    pad(Math.floor((diff / 60000) % 60)),
    pad(Math.floor((diff / 1000) % 60))
  ]
})

async function loadSessions() {
  loading.value = true
  error.value = ''
  try {
    sessions.value = await listSeckillSessions()
    if (sessions.value.length) {
      activeSessionId.value = sessions.value[0].id
      await loadGoods()
    }
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function loadGoods() {
  loading.value = true
  error.value = ''
  try {
    // 秒杀接口只给 skuId/秒杀价/剩余库存,名称图片原价在 product 服务——
    // 一次批量接口拿全再组装,不逐个查(N+1 教训的前端版)
    const list = await listSeckillGoods(activeSessionId.value)
    const skuIds = list.map((g) => g.skuId)
    const skus = skuIds.length ? await getSkuBatch(skuIds) : []
    const skuMap = new Map(skus.map((s) => [s.id, s]))
    goods.value = list.map((g) => {
      const sku = skuMap.get(g.skuId) || {}
      return {
        ...g,
        name: sku.spuName || '神秘商品',
        spec: sku.spec || '',
        image: sku.image || '',
        originalPrice: sku.originalPrice
      }
    })
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function selectSession(id) {
  if (id === activeSessionId.value) return
  activeSessionId.value = id
  loadGoods()
}

// —— 抢购 + 轮询 ——
const grabbing = ref(false)      // 防连点:请求没回来前按钮全部失效
const dialogVisible = ref(false)
const pollResult = ref(null)     // 弹层内容:QUEUING 中 / SUCCESS / SOLD_OUT / FAILED

async function grab(g) {
  if (phase.value.state !== 'ongoing' || grabbing.value) return
  // 未登录先登录:带上 redirect,登录成功后跳回本页(与路由守卫同一套路)
  if (!localStorage.getItem('insight_token')) {
    router.push({ path: '/login', query: { redirect: '/seckill' } })
    return
  }
  grabbing.value = true
  try {
    // 下单秒返回"取号单",真正结果要靠轮询——这就是削峰的体验形态
    const { requestId } = await createSeckillOrder(activeSessionId.value, g.id)
    pollResult.value = { status: 'QUEUING' }
    dialogVisible.value = true
    poll(requestId, g)
  } catch (e) {
    // 同步侧拒绝的业务错误(如 10904 已抢过):后端文案直接展示
    pollResult.value = { status: 'FAILED', reason: e.message || '抢购失败' }
    dialogVisible.value = true
  } finally {
    grabbing.value = false
  }
}

// 轮询:每秒问一次结果,拿到终态就停;60 次封顶防死循环
let pollTimer = null
function poll(requestId, g) {
  let tries = 0
  clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    tries++
    try {
      const res = await getSeckillResult(activeSessionId.value, requestId)
      if (res.status !== 'QUEUING') {
        clearInterval(pollTimer)
        pollResult.value = res
        if (res.status === 'SUCCESS') {
          g.grabbed = true
          g.remain = Math.max(0, g.remain - 1) // 本地库存同步减一,不等刷新
        }
        return
      }
    } catch (e) {
      // 单次轮询失败(网络抖动)不终止,交给超时兜底
    }
    if (tries >= 60) {
      clearInterval(pollTimer)
      pollResult.value = { status: 'FAILED', reason: '等待结果超时,请刷新页面查看' }
    }
  }, 1000)
}

function btnText(g) {
  if (g.grabbed) return '已抢到'
  if (g.remain <= 0) return '已售罄'
  if (phase.value.state === 'upcoming') return '未开始'
  if (phase.value.state === 'ended') return '已结束'
  return '立即抢购'
}

function btnDisabled(g) {
  return (
    grabbing.value ||
    g.grabbed ||
    g.remain <= 0 ||
    phase.value.state !== 'ongoing'
  )
}

onMounted(() => {
  loadSessions()
  ticker = setInterval(() => (now.value = Date.now()), 1000)
})
onBeforeUnmount(() => {
  clearInterval(ticker)
  clearInterval(pollTimer)
})
</script>

<template>
  <div class="seckill">
    <!-- 页头:标题 + 场次倒计时 -->
    <section class="hero container">
      <div>
        <p class="hero-kicker">FLASH SALE · 限时秒杀</p>
        <h1 class="hero-title">手慢无,<span class="text-accent">拼手速</span>的时候到了</h1>
        <p class="hero-sub">下单即排队,公平抢购,每人每件限抢一次</p>
      </div>
      <div v-if="phase.state !== 'idle'" class="countdown glass">
        <p class="cd-label">{{ phase.label }}</p>
        <div class="cd-digits">
          <template v-for="(d, i) in countdown" :key="i">
            <span class="cd-digit num">{{ d }}</span>
            <span v-if="i < 2" class="cd-colon">:</span>
          </template>
        </div>
      </div>
    </section>

    <!-- 场次切换 -->
    <section class="container sessions">
      <button
        v-for="s in sessions"
        :key="s.id"
        class="session-chip"
        :class="{ active: s.id === activeSessionId }"
        @click="selectSession(s.id)"
      >
        <strong>{{ s.name }}</strong>
        <small>{{ s.startTime.slice(5, 10) }} ~ {{ s.endTime.slice(5, 10) }}</small>
      </button>
    </section>

    <!-- 商品网格 -->
    <section class="container goods-section">
      <div v-if="loading" class="grid">
        <div v-for="i in 4" :key="i" class="card-skeleton skeleton"></div>
      </div>

      <div v-else-if="error" class="state">
        <p>{{ error }}</p>
        <button class="btn-ghost glass" @click="loadSessions">重新加载</button>
      </div>

      <div v-else-if="!goods.length" class="state">
        <p>本场暂无秒杀商品</p>
      </div>

      <div v-else class="grid">
        <article v-for="g in goods" :key="g.id" class="goods-card glass">
          <div class="goods-img">
            <img v-if="g.image" :src="g.image" :alt="g.name" />
            <div v-else class="img-ph">限时秒杀</div>
            <span v-if="g.remain <= 0" class="img-mask">已售罄</span>
            <span v-else-if="phase.state === 'ended'" class="img-mask">已结束</span>
            <span v-else-if="phase.state === 'upcoming'" class="img-mask">未开始</span>
            <span v-else-if="g.grabbed" class="img-mask got">已抢到</span>
          </div>

          <div class="goods-body">
            <h3 class="goods-name">{{ g.name }}</h3>
            <p v-if="g.spec" class="goods-spec">{{ g.spec }}</p>

            <div class="price-row">
              <span class="sk-price">¥{{ formatPrice(g.seckillPrice) }}</span>
              <span v-if="g.originalPrice" class="sk-origin">¥{{ formatPrice(g.originalPrice) }}</span>
            </div>

            <div class="remain-row">
              <span class="remain" :class="{ empty: g.remain <= 0 }">
                {{ g.remain > 0 ? `仅剩 ${g.remain} 件` : '已抢空' }}
              </span>
            </div>

            <button class="grab-btn" :disabled="btnDisabled(g)" @click="grab(g)">
              {{ btnText(g) }}
            </button>
          </div>
        </article>
      </div>
    </section>

    <!-- 抢购结果弹层:Teleport 挂到 body,盖住一切 -->
    <Teleport to="body">
      <div v-if="dialogVisible" class="dialog-mask" @click.self="dialogVisible = false">
        <div class="dialog glass">
          <template v-if="pollResult">
            <div v-if="pollResult.status === 'QUEUING'" class="dialog-body">
              <div class="spinner"></div>
              <h3>排队中…</h3>
              <p>请求已进入秒杀队列,正在为你抢购,稍安勿躁</p>
            </div>
            <div v-else-if="pollResult.status === 'SUCCESS'" class="dialog-body">
              <div class="dialog-icon">🎉</div>
              <h3>抢到了!</h3>
              <p>秒杀订单号<br /><span class="order-id num">{{ pollResult.orderId }}</span></p>
            </div>
            <div v-else class="dialog-body">
              <div class="dialog-icon">😅</div>
              <h3>{{ pollResult.status === 'SOLD_OUT' ? '手慢了' : '抢购失败' }}</h3>
              <p>{{ pollResult.status === 'SOLD_OUT' ? '这件商品已被抢空,看看别的吧' : pollResult.reason }}</p>
            </div>
          </template>
          <button class="dialog-btn" @click="dialogVisible = false">
            {{ pollResult?.status === 'SUCCESS' ? '好的' : '知道了' }}
          </button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* ===== 页头 ===== */
.hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--s-8);
  flex-wrap: wrap;
  padding: var(--s-10) 0 var(--s-7);
}
.hero-kicker { font-size: 12px; letter-spacing: 4px; color: var(--c-text-faint); margin-bottom: var(--s-4); }
.hero-title { font-size: 34px; font-weight: 300; letter-spacing: 2px; }
.hero-sub { margin-top: var(--s-2); font-size: 13px; color: var(--c-text-dim); }

/* ===== 倒计时 ===== */
.countdown { padding: var(--s-5) var(--s-7); border-radius: var(--r-lg); text-align: center; }
.cd-label { font-size: 11px; letter-spacing: 3px; color: var(--c-text-faint); margin-bottom: var(--s-2); }
.cd-digits { display: flex; align-items: center; gap: var(--s-2); }
.cd-digit {
  min-width: 56px;
  padding: 10px 0;
  font-size: 26px;
  font-variant-numeric: tabular-nums; /* 等宽数字:秒跳时不抖动 */
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.08);
  border: 1px solid rgba(255, 107, 107, 0.35);
  border-radius: var(--r-md);
}
.cd-colon { font-size: 22px; color: #ff6b6b; }

/* ===== 场次切换 ===== */
.sessions { display: flex; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-6); }
.session-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 10px 18px;
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  color: var(--c-text-dim);
  transition: border-color var(--t-fast) var(--ease-out), color var(--t-fast) var(--ease-out);
}
.session-chip strong { font-size: 13px; }
.session-chip small { font-size: 11px; color: var(--c-text-faint); }
.session-chip:hover { color: var(--c-text); border-color: rgba(255, 107, 107, 0.5); }
.session-chip.active {
  color: #ff6b6b;
  border-color: rgba(255, 107, 107, 0.6);
  background: rgba(255, 107, 107, 0.08);
}

/* ===== 商品网格 ===== */
.goods-section { margin-bottom: var(--s-12); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: var(--s-5); }
.card-skeleton { height: 380px; border-radius: var(--r-lg); }
.state { padding: var(--s-10) 0; text-align: center; color: var(--c-text-dim); }
.state p { margin-bottom: var(--s-5); }

.goods-card { border-radius: var(--r-lg); overflow: hidden; display: flex; flex-direction: column; }
.goods-img { position: relative; aspect-ratio: 16 / 10; background: var(--c-bg-raised); }
.goods-img img { width: 100%; height: 100%; object-fit: cover; }
.img-ph {
  display: grid; place-items: center; height: 100%;
  color: var(--c-text-faint); font-size: 13px; letter-spacing: 3px;
}
.img-mask {
  position: absolute; inset: 0;
  display: grid; place-items: center;
  background: rgba(8, 10, 14, 0.65);
  backdrop-filter: blur(2px);
  color: var(--c-text); font-size: 14px; letter-spacing: 2px;
}
.img-mask.got { color: #ffd76b; }

.goods-body { padding: var(--s-5); display: flex; flex-direction: column; gap: var(--s-2); flex: 1; }
.goods-name { font-size: 15px; font-weight: 500; }
.goods-spec { font-size: 12px; color: var(--c-text-faint); }

.price-row { display: flex; align-items: baseline; gap: var(--s-3); }
.sk-price {
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(120deg, #ff6b6b, #ffa94d);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.sk-origin { font-size: 13px; color: var(--c-text-faint); text-decoration: line-through; }

.remain { font-size: 12px; color: #ff6b6b; }
.remain.empty { color: var(--c-text-faint); }

.grab-btn {
  margin-top: var(--s-3);
  padding: 11px 0;
  font-size: 14px;
  letter-spacing: 2px;
  color: #fff;
  background: linear-gradient(120deg, #ff6b6b, #ff8e53);
  border: none;
  border-radius: var(--r-pill);
  transition: filter var(--t-fast) var(--ease-out), transform var(--t-fast) var(--ease-out);
}
.grab-btn:hover:not(:disabled) { filter: brightness(1.1); }
.grab-btn:active:not(:disabled) { transform: scale(0.97); }
.grab-btn:disabled { background: var(--c-glass); color: var(--c-text-faint); }

/* ===== 结果弹层 ===== */
.dialog-mask {
  position: fixed; inset: 0; z-index: 200;
  display: grid; place-items: center;
  background: rgba(6, 8, 12, 0.7);
  backdrop-filter: blur(6px);
}
.dialog {
  width: min(380px, calc(100vw - 48px));
  border-radius: var(--r-xl);
  padding: var(--s-8) var(--s-7);
  text-align: center;
  display: flex; flex-direction: column; gap: var(--s-5);
}
.dialog-body h3 { font-size: 20px; font-weight: 500; margin-bottom: var(--s-2); }
.dialog-body p { font-size: 13px; color: var(--c-text-dim); line-height: 1.7; }
.dialog-icon { font-size: 44px; }
.order-id { color: #ffd76b; font-size: 13px; word-break: break-all; }
.dialog-btn {
  margin-top: var(--s-2);
  padding: 11px 0;
  font-size: 14px;
  color: #fff;
  background: var(--c-accent);
  border: none;
  border-radius: var(--r-pill);
  transition: filter var(--t-fast) var(--ease-out);
}
.dialog-btn:hover { filter: brightness(1.1); }

/* 排队中转圈:border 一侧上色 + 旋转 */
.spinner {
  width: 40px; height: 40px;
  margin: 0 auto var(--s-4);
  border: 3px solid var(--c-border);
  border-top-color: var(--c-accent);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
