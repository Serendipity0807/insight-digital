<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  product: { type: Object, required: true }
})

const router = useRouter()

// 起售价 = 所有 SKU 的最低价(分 → 元,千分位)
const minPrice = computed(() => {
  const min = Math.min(...props.product.skus.map((s) => s.price))
  return (min / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
})

const goDetail = () => router.push(`/product/${props.product.id}`)

// 第 4 步接入购物车(Pinia + mall-cart 服务);现在先占位
const addToCart = () => {}
</script>

<template>
  <article class="card" @click="goDetail">
    <!-- 商品图区:暂无真实图片,先用"聚光灯 + 品名首字"占位;
         第 2 步换成真实主图,并加骨架屏加载态 -->
    <div class="card-media" :style="{ '--hue': product.hue }">
      <div class="spotlight"></div>
      <span class="glyph">{{ product.name.charAt(0) }}</span>
      <span class="tag-chip">{{ product.tag }}</span>
    </div>

    <div class="card-body">
      <h3 class="card-name">{{ product.name }}</h3>
      <p class="card-tagline">{{ product.tagline }}</p>
      <div class="card-foot">
        <div class="price">
          <span class="price-symbol">¥</span>
          <span class="price-num num">{{ minPrice }}</span>
          <span class="price-from">起</span>
        </div>
        <button class="add-btn" @click.stop="addToCart" title="加入购物车(第 4 步开放)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14" /></svg>
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.card {
  border-radius: var(--r-lg);
  background: var(--c-glass);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid var(--c-border);
  overflow: hidden;
  cursor: pointer;
  transition:
    transform var(--t-base) var(--ease-out),
    border-color var(--t-base) var(--ease-out),
    box-shadow var(--t-base) var(--ease-out);
}
.card:hover {
  transform: translateY(-4px);
  border-color: var(--c-border-strong);
  box-shadow: var(--glow-card);
}

/* —— 画册式图区:正方形,聚光灯随商品色相变化 —— */
.card-media {
  position: relative;
  aspect-ratio: 1 / 1;
  display: grid;
  place-items: center;
  overflow: hidden;
  background:
    radial-gradient(60% 60% at 50% 35%, hsla(var(--hue), 80%, 55%, 0.22), transparent 70%),
    linear-gradient(160deg, #161c25 0%, #0d1117 100%);
}
.spotlight {
  position: absolute;
  inset: 0;
  background: radial-gradient(45% 45% at 50% 40%, hsla(var(--hue), 90%, 60%, 0.14), transparent 70%);
  opacity: 0.7;
  transition:
    opacity var(--t-base) var(--ease-out),
    transform var(--t-slow) var(--ease-out);
}
.card:hover .spotlight { opacity: 1; transform: scale(1.15); }
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
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* —— 信息区:名字 + 一句话卖点 + 价格 —— */
.card-body { padding: var(--s-5); }
.card-name {
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-tagline {
  margin-top: var(--s-2);
  font-size: 12px;
  color: var(--c-text-dim);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-foot {
  margin-top: var(--s-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price { display: flex; align-items: baseline; gap: 2px; }
.price-symbol { font-size: 14px; color: var(--c-accent); font-weight: 600; }
.price-num {
  font-size: 22px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.price-from { font-size: 11px; color: var(--c-text-faint); margin-left: 2px; }

/* 加入购物车:悬停浮现(第 4 步加"飞入购物车"微动效) */
.add-btn {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  color: #fff;
  background: var(--c-accent);
  border: none;
  border-radius: var(--r-pill);
  opacity: 0;
  transform: translateY(4px);
  transition:
    opacity var(--t-fast) var(--ease-out),
    transform var(--t-fast) var(--ease-out),
    box-shadow var(--t-fast) var(--ease-out);
}
.card:hover .add-btn { opacity: 1; transform: translateY(0); }
.add-btn:hover { box-shadow: var(--glow-accent); }
</style>
