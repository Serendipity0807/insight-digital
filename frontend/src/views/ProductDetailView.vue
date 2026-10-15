<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getProductDetail } from '../api/product'
import { formatPrice } from '../utils/format'

const route = useRoute()

const detail = ref(null)
const loading = ref(true)
const error = ref('')
const selectedSkuId = ref(null)

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    detail.value = await getProductDetail(route.params.id)
    selectedSkuId.value = detail.value.skus?.[0]?.id ?? null
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}
load()

const selectedSku = computed(
  () => detail.value?.skus?.find((s) => s.id === selectedSkuId.value) ?? null
)
const price = computed(() => formatPrice(selectedSku.value?.price))
const originalPrice = computed(() => formatPrice(selectedSku.value?.originalPrice))
const sales = computed(() => (detail.value?.sales ?? 0).toLocaleString('zh-CN'))
const hue = computed(() => (detail.value?.id ?? 0) % 360)

// 参数表:后端 specParams 是 JSON 字符串,前端做防御性解析(脏数据不崩页面)
const params = computed(() => {
  const raw = detail.value?.specParams
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})
// 文本参数 → 色块;数值参数(type=meter) → 进度条
const textParams = computed(() => params.value.filter((p) => p.type !== 'meter'))
const meterParams = computed(() => params.value.filter((p) => p.type === 'meter'))
// 进度条百分比:value / max,夹在 0~100 之间,防脏数据溢出
const meterPct = (p) => Math.min(100, Math.round(((p.value || 0) / (p.max || 100)) * 100))

// 第 4 步:接入购物车 / 下单流程
const addToCart = () => {}
const buyNow = () => {}
</script>

<template>
  <div class="container detail">
    <!-- 骨架屏 -->
    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton sk-media"></div>
      <div class="sk-info">
        <div class="skeleton sk-line w60"></div>
        <div class="skeleton sk-line w40"></div>
        <div class="skeleton sk-line w80"></div>
        <div class="skeleton sk-line w30"></div>
        <div class="skeleton sk-btn"></div>
      </div>
    </div>

    <!-- 失败态 -->
    <div v-else-if="error" class="state">
      <p>{{ error }}</p>
      <button class="btn-ghost glass" @click="load">重新加载</button>
    </div>

    <!-- 详情 -->
    <template v-else-if="detail">
      <div class="detail-grid">
        <!-- 左:主图区(暂无真实图,聚光灯 + 首字占位) -->
        <div class="media glass" :style="{ '--hue': hue }">
          <div class="spotlight"></div>
          <span class="glyph">{{ detail.name.charAt(0) }}</span>
          <span v-if="detail.tag" class="tag-chip">{{ detail.tag }}</span>
        </div>

        <!-- 右:信息区 -->
        <div class="info">
          <h1 class="name">{{ detail.name }}</h1>
          <p class="tagline">{{ detail.tagline }}</p>

          <!-- 慧眼点评 -->
          <div v-if="detail.tag" class="review">
            <span class="review-label">慧眼点评</span>
            <span class="review-value">{{ detail.tag }}</span>
          </div>

          <p class="desc">{{ detail.description }}</p>

          <!-- SKU 选择 -->
          <div class="sku-group">
            <span class="label">规格</span>
            <div class="sku-list">
              <button
                v-for="s in detail.skus"
                :key="s.id"
                class="sku-item"
                :class="{ active: s.id === selectedSkuId }"
                @click="selectedSkuId = s.id"
              >
                {{ s.spec }}
              </button>
            </div>
          </div>

          <!-- 价格 -->
          <div class="price-box">
            <span class="price-symbol">¥</span>
            <span class="price-num num">{{ price }}</span>
            <span v-if="selectedSku?.originalPrice" class="original">{{ originalPrice }}</span>
            <span class="sales">已售 {{ sales }}</span>
          </div>

          <!-- CTA -->
          <div class="cta">
            <button class="btn-primary" @click="buyNow">立即购买</button>
            <button class="btn-ghost glass" @click="addToCart">加入购物车</button>
          </div>

          <!-- 信任徽章 -->
          <div class="badges">
            <span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1 1 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z" /><path d="m9 12 2 2 4-4" /></svg>
              正品保障
            </span>
            <span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" /><path d="M3 3v5h5" /></svg>
              七天无理由
            </span>
            <span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2 3 14h9l-1 8 10-12h-9l1-8z" /></svg>
              极速退款
            </span>
          </div>
        </div>
      </div>

      <!-- 参数可视化:色块(text)+ 计量条(meter),数据来自后端 specParams -->
      <div v-if="params.length" class="params glass">
        <h2>参数可视化</h2>

        <!-- 文本参数:色块 -->
        <div class="param-grid">
          <div v-for="(p, i) in textParams" :key="'t' + i" class="param-chip">
            <span class="param-label">{{ p.label }}</span>
            <span class="param-value">{{ p.value }}</span>
          </div>
        </div>

        <!-- 数值参数:进度条 -->
        <div v-for="(p, i) in meterParams" :key="'m' + i" class="param-meter">
          <div class="meter-head">
            <span class="param-label">{{ p.label }}</span>
            <span class="meter-num">{{ p.value }}<em>{{ p.unit }}</em></span>
          </div>
          <div class="meter-track">
            <div class="meter-fill" :style="{ width: meterPct(p) + '%' }"></div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail {
  padding: var(--s-8) 0 var(--s-12);
}

/* —— 骨架屏 —— */
.detail-skeleton {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-10);
  padding-top: var(--s-6);
}
.sk-media {
  aspect-ratio: 1 / 1;
  border-radius: var(--r-xl);
}
.sk-info { display: flex; flex-direction: column; gap: var(--s-5); }
.sk-line { height: 20px; border-radius: var(--r-sm); }
.w60 { width: 60%; }
.w40 { width: 40%; }
.w80 { width: 80%; }
.w30 { width: 30%; }
.sk-btn { height: 52px; width: 220px; border-radius: var(--r-pill); }

/* —— 失败态 —— */
.state {
  padding: var(--s-12) 0;
  text-align: center;
  color: var(--c-text-dim);
}
.state p { margin-bottom: var(--s-5); }

/* —— 详情主体 —— */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-10);
  align-items: start;
}

.media {
  position: relative;
  aspect-ratio: 1 / 1;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: var(--r-xl);
  background:
    radial-gradient(60% 60% at 50% 35%, hsla(var(--hue), 80%, 55%, 0.22), transparent 70%),
    linear-gradient(160deg, #161c25 0%, #0d1117 100%);
}
.spotlight {
  position: absolute;
  inset: 0;
  background: radial-gradient(45% 45% at 50% 40%, hsla(var(--hue), 90%, 60%, 0.14), transparent 70%);
}
.glyph {
  font-size: 160px;
  font-weight: 200;
  color: hsla(var(--hue), 70%, 75%, 0.35);
  user-select: none;
}
.tag-chip {
  position: absolute;
  top: var(--s-5);
  left: var(--s-5);
  font-size: 13px;
  letter-spacing: 1px;
  color: var(--c-accent-2);
  background: rgba(11, 14, 18, 0.6);
  border: 1px solid rgba(33, 212, 196, 0.35);
  border-radius: var(--r-pill);
  padding: 6px 14px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.info { display: flex; flex-direction: column; }
.name { font-size: 30px; font-weight: 300; letter-spacing: 1px; line-height: 1.3; }
.tagline { margin-top: var(--s-3); font-size: 14px; color: var(--c-text-dim); }

.review {
  margin-top: var(--s-5);
  display: flex;
  align-items: center;
  gap: var(--s-3);
}
.review-label {
  font-size: 12px;
  color: var(--c-text-faint);
  letter-spacing: 1px;
}
.review-value {
  font-size: 13px;
  color: var(--c-accent-2);
  background: rgba(33, 212, 196, 0.10);
  border: 1px solid rgba(33, 212, 196, 0.35);
  border-radius: var(--r-pill);
  padding: 4px 12px;
}

.desc { margin-top: var(--s-5); font-size: 14px; color: var(--c-text-dim); line-height: 1.8; }

.sku-group { margin-top: var(--s-7); }
.label { font-size: 13px; color: var(--c-text-faint); letter-spacing: 1px; }
.sku-list { margin-top: var(--s-3); display: flex; flex-wrap: wrap; gap: var(--s-3); }
.sku-item {
  padding: 10px 18px;
  font-size: 13px;
  color: var(--c-text-dim);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  transition:
    border-color var(--t-fast) var(--ease-out),
    color var(--t-fast) var(--ease-out);
}
.sku-item:hover { border-color: var(--c-border-strong); color: var(--c-text); }
.sku-item.active {
  color: #fff;
  border-color: transparent;
  background: var(--c-gradient);
}

.price-box {
  margin-top: var(--s-7);
  display: flex;
  align-items: baseline;
  gap: var(--s-3);
}
.price-symbol { font-size: 20px; color: var(--c-accent); font-weight: 600; }
.price-num { font-size: 40px; font-weight: 600; font-variant-numeric: tabular-nums; }
.original {
  font-size: 16px;
  color: var(--c-text-faint);
  text-decoration: line-through;
}
.sales { font-size: 13px; color: var(--c-text-faint); }

.cta { margin-top: var(--s-7); display: flex; gap: var(--s-4); }

.badges {
  margin-top: var(--s-7);
  display: flex;
  gap: var(--s-6);
  flex-wrap: wrap;
  padding-top: var(--s-5);
  border-top: 1px solid var(--c-border);
}
.badges span {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  font-size: 12px;
  color: var(--c-text-faint);
}
.badges svg { width: 16px; height: 16px; color: var(--c-accent-2); }

/* —— 参数可视化 —— */
.params {
  margin-top: var(--s-10);
  padding: var(--s-8);
  border-radius: var(--r-xl);
}
.params h2 {
  font-size: 20px;
  font-weight: 300;
  letter-spacing: 2px;
  margin-bottom: var(--s-6);
}

/* 文本参数:色块 */
.param-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--s-3);
}
.param-chip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: var(--s-4) var(--s-5);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
}
.param-label { font-size: 12px; color: var(--c-text-faint); letter-spacing: 1px; }
.param-value { font-size: 15px; color: var(--c-text); }

/* 数值参数:进度条 */
.param-meter { margin-top: var(--s-5); }
.meter-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--s-3);
}
.meter-num {
  font-size: 18px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--c-accent-2);
}
.meter-num em {
  font-size: 12px;
  font-style: normal;
  color: var(--c-text-faint);
  margin-left: 4px;
}
.meter-track {
  height: 6px;
  border-radius: var(--r-pill);
  background: rgba(255, 255, 255, 0.06);
  overflow: hidden;
}
.meter-fill {
  height: 100%;
  border-radius: var(--r-pill);
  background: var(--c-gradient);
  transition: width var(--t-slow) var(--ease-out);
}

@media (max-width: 900px) {
  .detail-grid { grid-template-columns: 1fr; }
  .detail-skeleton { grid-template-columns: 1fr; }
}
</style>
