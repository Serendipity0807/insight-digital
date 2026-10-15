<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCompareStore } from '../stores/compare'
import { getProductDetail } from '../api/product'
import { formatPrice } from '../utils/format'

const store = useCompareStore()
const router = useRouter()

const details = ref([])
const loading = ref(true)
const error = ref('')

const minOfSkus = (d) =>
  d.skus?.length ? Math.min(...d.skus.map((s) => s.price)) : null

// 对比页需要 specParams(详情接口才有),所以按 id 分别拉详情
const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const ids = store.items.map((x) => x.id)
    const list = await Promise.all(ids.map((id) => getProductDetail(id)))
    details.value = list.map((d) => ({ ...d, minPrice: minOfSkus(d) }))
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}
onMounted(load)

// 参数表(防御性解析)
const parsed = computed(() =>
  details.value.map((d) => {
    try {
      const arr = JSON.parse(d.specParams || '[]')
      return Array.isArray(arr) ? arr : []
    } catch {
      return []
    }
  })
)

const findValue = (list, label) => list.find((p) => p.label === label)?.value ?? null

// 文本参数行:取两款 label 并集,顺序以第一款为主(缺的显示 "—")
const textRows = computed(() => {
  const [a = [], b = []] = parsed.value
  const labels = [...new Set([...a, ...b].filter((p) => p.type !== 'meter').map((p) => p.label))]
  return labels.map((label) => ({ label, a: findValue(a, label), b: findValue(b, label) }))
})

// 数值参数行(进度条):同样取并集
const meterRows = computed(() => {
  const [a = [], b = []] = parsed.value
  const labels = [...new Set([...a, ...b].filter((p) => p.type === 'meter').map((p) => p.label))]
  return labels.map((label) => ({
    label,
    a: a.find((p) => p.label === label) ?? null,
    b: b.find((p) => p.label === label) ?? null
  }))
})

const meterPct = (p) => Math.min(100, Math.round(((p?.value || 0) / (p?.max || 100)) * 100))
</script>

<template>
  <div class="container compare">
    <header class="page-head">
      <h1 class="page-title">慧眼对比</h1>
      <button class="btn-ghost glass" @click="router.push('/')">返回挑选</button>
    </header>

    <!-- 不足两款:提示 -->
    <div v-if="store.items.length < 2" class="state">
      <p>对比需要两款商品。先去首页勾选商品卡片右上角的「对比」按钮。</p>
      <button class="btn-primary" @click="router.push('/')">去挑选商品</button>
    </div>

    <!-- 骨架屏 -->
    <div v-else-if="loading" class="compare-skeleton">
      <div class="skeleton sk-head"></div>
      <div v-for="i in 6" :key="i" class="skeleton sk-row"></div>
    </div>

    <!-- 失败态 -->
    <div v-else-if="error" class="state">
      <p>{{ error }}</p>
      <button class="btn-ghost glass" @click="load">重新加载</button>
    </div>

    <!-- 对比主体 -->
    <template v-else>
      <!-- 商品头部:左右分屏(第一格空出,与下面参数表的 label 列对齐) -->
      <div class="compare-head">
        <div class="head-spacer"></div>
        <div v-for="p in details" :key="p.id" class="head-col">
          <div class="head-media" :style="{ '--hue': p.id % 360 }">
            <div class="spotlight"></div>
            <span class="glyph">{{ p.name.charAt(0) }}</span>
            <span v-if="p.tag" class="tag-chip">{{ p.tag }}</span>
          </div>
          <h2 class="head-name">{{ p.name }}</h2>
          <p class="head-tagline">{{ p.tagline }}</p>
          <div class="head-price">
            <span class="price-symbol">¥</span>
            <span class="price-num num">{{ formatPrice(p.minPrice) }}</span>
            <span class="price-from">起</span>
          </div>
        </div>
      </div>

      <!-- 文本参数对比 -->
      <div v-if="textRows.length" class="compare-table glass">
        <h3>参数对比</h3>
        <div v-for="row in textRows" :key="row.label" class="row">
          <span class="row-label">{{ row.label }}</span>
          <span class="row-value">{{ row.a ?? '—' }}</span>
          <span class="row-value">{{ row.b ?? '—' }}</span>
        </div>
      </div>

      <!-- 数值参数对比:进度条并排,长短一眼看清 -->
      <div v-if="meterRows.length" class="compare-table glass">
        <h3>性能对比</h3>
        <div v-for="row in meterRows" :key="row.label" class="meter-row">
          <span class="row-label">{{ row.label }}</span>
          <div class="meter-cell">
            <div class="meter-track"><div class="meter-fill" :style="{ width: meterPct(row.a) + '%' }"></div></div>
            <span class="meter-num">{{ row.a?.value ?? '—' }}<em>{{ row.a?.unit || '' }}</em></span>
          </div>
          <div class="meter-cell">
            <div class="meter-track"><div class="meter-fill" :style="{ width: meterPct(row.b) + '%' }"></div></div>
            <span class="meter-num">{{ row.b?.value ?? '—' }}<em>{{ row.b?.unit || '' }}</em></span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.compare { padding: var(--s-8) 0 var(--s-12); }

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-7);
}
.page-title { font-size: 30px; font-weight: 300; letter-spacing: 2px; }

/* —— 失败/空态 —— */
.state {
  padding: var(--s-12) 0;
  text-align: center;
  color: var(--c-text-dim);
}
.state p { margin-bottom: var(--s-5); }

/* —— 骨架屏 —— */
.compare-skeleton { display: flex; flex-direction: column; gap: var(--s-3); }
.sk-head { height: 260px; border-radius: var(--r-xl); }
.sk-row { height: 44px; border-radius: var(--r-md); }

/* —— 三列对齐:label 列 + 两个商品列 —— */
.compare-head,
.compare-table .row,
.compare-table .meter-row {
  display: grid;
  grid-template-columns: 120px 1fr 1fr;
  gap: var(--s-4);
  align-items: center;
}
.compare-head { align-items: start; margin-bottom: var(--s-8); }

/* 商品头部卡片 */
.head-col {
  display: flex;
  flex-direction: column;
  padding: var(--s-5);
  border-radius: var(--r-xl);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
}
.head-media {
  position: relative;
  aspect-ratio: 1 / 1;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: var(--r-lg);
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
  font-size: 96px;
  font-weight: 200;
  color: hsla(var(--hue), 70%, 75%, 0.35);
  user-select: none;
}
.tag-chip {
  position: absolute;
  top: var(--s-4);
  left: var(--s-4);
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--c-accent-2);
  background: rgba(11, 14, 18, 0.6);
  border: 1px solid rgba(33, 212, 196, 0.35);
  border-radius: var(--r-pill);
  padding: 4px 10px;
}
.head-name {
  margin-top: var(--s-4);
  font-size: 17px;
  font-weight: 500;
  line-height: 1.4;
}
.head-tagline {
  margin-top: var(--s-2);
  font-size: 12px;
  color: var(--c-text-dim);
}
.head-price {
  margin-top: var(--s-3);
  display: flex;
  align-items: baseline;
  gap: 2px;
}
.price-symbol { font-size: 14px; color: var(--c-accent); font-weight: 600; }
.price-num { font-size: 22px; font-weight: 600; font-variant-numeric: tabular-nums; }
.price-from { font-size: 11px; color: var(--c-text-faint); margin-left: 2px; }

/* —— 参数对比表 —— */
.compare-table {
  margin-top: var(--s-6);
  padding: var(--s-6) var(--s-7);
  border-radius: var(--r-xl);
}
.compare-table h3 {
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
  margin-bottom: var(--s-4);
  color: var(--c-text-dim);
}
.row { padding: var(--s-3) 0; border-bottom: 1px solid var(--c-border); }
.row:last-child { border-bottom: none; }
.row-label {
  font-size: 13px;
  color: var(--c-text-faint);
  letter-spacing: 1px;
}
.row-value { font-size: 14px; }

/* —— 性能对比(进度条) —— */
.meter-row { padding: var(--s-3) 0; border-bottom: 1px solid var(--c-border); }
.meter-row:last-child { border-bottom: none; }
.meter-cell { display: flex; flex-direction: column; gap: var(--s-2); }
.meter-track {
  height: 8px;
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
.meter-num {
  font-size: 14px;
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

@media (max-width: 760px) {
  .compare-head,
  .compare-table .row,
  .compare-table .meter-row { grid-template-columns: 1fr 1fr; }
  .head-spacer { display: none; }
  .row-label { grid-column: 1 / -1; }
}
</style>
