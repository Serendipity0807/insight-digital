<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { formatPrice } from '../utils/format'

const router = useRouter()
const cart = useCartStore()

const error = ref('')
const busySku = ref(null) // 正在请求中的条目(禁用其按钮,防止连点)

onMounted(async () => {
  try {
    await cart.load()
  } catch (e) {
    error.value = e.message || '加载失败'
  }
})

// 数量步进:夹在 1~99 之间(和后端 @Min/@Max 一致)
async function step(skuId, delta) {
  const item = cart.items.find((i) => i.skuId === skuId)
  if (!item) return
  const next = item.quantity + delta
  if (next < 1 || next > 99) return
  busySku.value = skuId
  try {
    await cart.updateQuantity(skuId, next)
  } catch (e) {
    error.value = e.message || '修改数量失败'
  } finally {
    busySku.value = null
  }
}

async function toggle(item) {
  busySku.value = item.skuId
  try {
    await cart.toggleChecked(item.skuId, !item.checked)
  } catch (e) {
    error.value = e.message || '操作失败'
  } finally {
    busySku.value = null
  }
}

async function remove(item) {
  if (!confirm(`把「${item.skuName}」移出购物车?`)) return
  busySku.value = item.skuId
  try {
    await cart.remove(item.skuId)
  } catch (e) {
    error.value = e.message || '删除失败'
  } finally {
    busySku.value = null
  }
}

const hue = (id) => id % 360
const subtotal = (item) => formatPrice(item.price * item.quantity)
</script>

<template>
  <div class="container cart-page">
    <h1 class="page-title">购物车</h1>

    <!-- 加载骨架 -->
    <div v-if="cart.loading" class="rows">
      <div v-for="n in 3" :key="n" class="row skeleton" style="height: 96px"></div>
    </div>

    <!-- 失败态 -->
    <div v-else-if="error && !cart.items.length" class="state">
      <p>{{ error }}</p>
      <button class="btn-ghost glass" @click="cart.load()">重新加载</button>
    </div>

    <!-- 空态 -->
    <div v-else-if="!cart.items.length" class="state empty">
      <p>购物车还是空的</p>
      <RouterLink to="/" class="btn-primary">去逛逛</RouterLink>
    </div>

    <template v-else>
      <p v-if="error" class="state">{{ error }}</p>

      <!-- 条目列表 -->
      <div class="rows">
        <div v-for="item in cart.items" :key="item.skuId" class="row glass">
          <!-- 勾选 -->
          <button
            class="check"
            :class="{ on: item.checked }"
            :disabled="busySku === item.skuId"
            @click="toggle(item)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7" /></svg>
          </button>

          <!-- 图:无真实图,首字占位(详情页同款) -->
          <div class="thumb" :style="{ '--hue': hue(item.skuId) }">
            {{ item.skuName?.charAt(0) || '?' }}
          </div>

          <!-- 信息 -->
          <div class="info">
            <RouterLink :to="`/product/${item.spuId}`" class="name">{{ item.skuName }}</RouterLink>
            <span v-if="item.spec" class="spec">{{ item.spec }}</span>
          </div>

          <!-- 单价 -->
          <div class="price num">¥{{ formatPrice(item.price) }}</div>

          <!-- 数量步进 -->
          <div class="stepper">
            <button :disabled="busySku === item.skuId || item.quantity <= 1" @click="step(item.skuId, -1)">−</button>
            <span class="num">{{ item.quantity }}</span>
            <button :disabled="busySku === item.skuId || item.quantity >= 99" @click="step(item.skuId, 1)">+</button>
          </div>

          <!-- 小计 -->
          <div class="subtotal num">¥{{ subtotal(item) }}</div>

          <!-- 删除 -->
          <button class="remove" :disabled="busySku === item.skuId" @click="remove(item)" title="移出购物车">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
          </button>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="summary glass">
        <button class="check all" :class="{ on: cart.allChecked }" @click="cart.setAllChecked(!cart.allChecked)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7" /></svg>
          <span>全选</span>
        </button>
        <div class="total">
          <span>已选 <em class="num">{{ cart.checkedCount }}</em> 件,合计</span>
          <strong class="num">¥{{ formatPrice(cart.checkedTotal) }}</strong>
        </div>
        <button
          class="btn-primary checkout-btn"
          :disabled="!cart.checkedCount"
          @click="router.push('/checkout')"
        >
          去结算
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-page { padding: var(--s-8) 0 var(--s-12); }
.page-title { font-size: 24px; font-weight: 300; letter-spacing: 4px; margin-bottom: var(--s-6); }

.state { padding: var(--s-8) 0; text-align: center; color: var(--c-text-dim); }
.state .btn-primary { display: inline-block; margin-top: var(--s-5); }
.empty p { margin-bottom: var(--s-5); }

.rows { display: flex; flex-direction: column; gap: var(--s-3); }

/* —— 单行条目 —— */
.row {
  display: grid;
  grid-template-columns: 32px 72px 1fr 110px 140px 130px 36px;
  align-items: center;
  gap: var(--s-4);
  padding: var(--s-4) var(--s-5);
  border-radius: var(--r-lg);
  transition: border-color var(--t-fast) var(--ease-out);
}
.row:hover { border-color: var(--c-border-strong); }

/* 勾选按钮:方块圆角,选中变渐变 */
.check {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  background: transparent;
  border: 1px solid var(--c-border-strong);
  border-radius: 6px;
  color: transparent;
  transition:
    background var(--t-fast) var(--ease-out),
    color var(--t-fast) var(--ease-out);
}
.check svg { width: 14px; height: 14px; }
.check.on { background: var(--c-gradient); border-color: transparent; color: #fff; }
.check:disabled { opacity: 0.5; cursor: not-allowed; }

.thumb {
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  font-size: 30px;
  font-weight: 200;
  color: hsla(var(--hue), 70%, 75%, 0.4);
  border-radius: var(--r-md);
  background:
    radial-gradient(70% 70% at 50% 40%, hsla(var(--hue), 80%, 55%, 0.18), transparent 75%),
    rgba(255, 255, 255, 0.03);
  user-select: none;
}

.info { display: flex; flex-direction: column; gap: var(--s-2); }
.name { font-size: 15px; color: var(--c-text); transition: color var(--t-fast) var(--ease-out); }
.name:hover { color: var(--c-accent-2); }
.spec {
  align-self: flex-start;
  font-size: 12px;
  color: var(--c-text-faint);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 2px 10px;
}

.price, .subtotal { font-size: 15px; }
.subtotal { color: var(--c-accent); font-weight: 600; }

/* 数量步进 */
.stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  overflow: hidden;
}
.stepper button {
  width: 32px;
  height: 32px;
  background: var(--c-glass);
  border: none;
  color: var(--c-text-dim);
  font-size: 16px;
  transition: color var(--t-fast) var(--ease-out);
}
.stepper button:hover:not(:disabled) { color: var(--c-accent-2); }
.stepper button:disabled { opacity: 0.35; cursor: not-allowed; }
.stepper span { min-width: 36px; text-align: center; font-size: 14px; }

.remove {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  background: transparent;
  border: none;
  color: var(--c-text-faint);
  transition: color var(--t-fast) var(--ease-out);
}
.remove:hover:not(:disabled) { color: var(--c-danger); }
.remove svg { width: 16px; height: 16px; }

/* —— 结算栏(吸底) —— */
.summary {
  margin-top: var(--s-6);
  display: flex;
  align-items: center;
  gap: var(--s-6);
  padding: var(--s-4) var(--s-6);
  border-radius: var(--r-lg);
}
.check.all {
  width: auto;
  display: flex;
  align-items: center;
  gap: var(--s-2);
  padding: 0;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--c-text-dim);
}
.total { margin-left: auto; font-size: 14px; color: var(--c-text-dim); }
.total em { font-style: normal; color: var(--c-text); }
.total strong { margin-left: var(--s-2); font-size: 22px; color: var(--c-accent); }
.checkout-btn:disabled { opacity: 0.45; cursor: not-allowed; }

@media (max-width: 860px) {
  .row { grid-template-columns: 28px 60px 1fr 32px; }
  .thumb { width: 60px; height: 60px; }
  .price, .stepper, .subtotal { display: none; }
}
</style>
