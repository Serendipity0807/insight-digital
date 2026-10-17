<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { createOrder } from '../api/order'
import { formatPrice } from '../utils/format'

const router = useRouter()
const cart = useCartStore()

// 结算页只认购物车里勾选的条目;勾选状态在购物车页操作,这里只读
const items = computed(() => cart.checkedItems)
const total = computed(() => cart.checkedTotal)

// 收货地址(下单时快照进订单,和订单里的 address_snapshot 对应)
const address = reactive({ name: '', phone: '', detail: '' })
const remark = ref('')
const submitting = ref(false)
const error = ref('')

onMounted(async () => {
  // 没有勾选条目(刷新页面 / 直接敲 URL 进来)→ 送回购物车
  if (!cart.items.length) {
    try {
      await cart.load()
    } catch (e) {
      error.value = e.message || '加载失败'
    }
  }
})

watch(items, (list) => {
  if (!submitting.value && !list.length && cart.items.length) {
    router.replace('/cart') // 勾选被清空了,结算没有意义
  }
})

function validate() {
  if (!address.name.trim()) return '请填写收货人'
  if (!/^1[3-9]\d{9}$/.test(address.phone.trim())) return '手机号格式不正确'
  if (!address.detail.trim()) return '请填写收货地址'
  return ''
}

async function submit() {
  error.value = ''
  const msg = validate()
  if (msg) {
    error.value = msg
    return
  }

  submitting.value = true
  try {
    // 下单请求体:只有 skuId + 数量 + 地址,没有价格——
    // 金额由订单服务重新算(防改价),前端合计只做展示
    const vo = await createOrder({
      items: items.value.map((i) => ({ skuId: i.skuId, quantity: i.quantity })),
      address: {
        name: address.name.trim(),
        phone: address.phone.trim(),
        detail: address.detail.trim()
      },
      remark: remark.value.trim() || null
    })

    // 下单成功:已购条目移出购物车(后端暂无"清空已选"接口,逐条删除顶替;
    // 接口文档里的 DELETE /api/v1/cart 是后续优化,删除失败不影响已下单事实)
    await Promise.allSettled(items.value.map((i) => cart.remove(i.skuId)))

    // 订单信息放进 sessionStorage 给收银台用(刷新不丢,关标签页即清)
    sessionStorage.setItem('insight_pending_order', JSON.stringify(vo))

    router.push(`/pay/${vo.orderId}`)
  } catch (e) {
    error.value = e.message || '下单失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="container checkout">
    <h1 class="page-title">确认订单</h1>

    <p v-if="error" class="error-tip">{{ error }}</p>

    <div class="grid">
      <!-- 左:收货地址 -->
      <section class="panel glass">
        <h2>收货地址</h2>
        <form class="form" @submit.prevent="submit">
          <div class="field-row">
            <label class="field">
              <span>收货人</span>
              <input v-model.trim="address.name" type="text" maxlength="20" placeholder="姓名" />
            </label>
            <label class="field">
              <span>手机号</span>
              <input v-model.trim="address.phone" type="tel" maxlength="11" placeholder="11 位手机号" />
            </label>
          </div>
          <label class="field">
            <span>收货地址</span>
            <input v-model.trim="address.detail" type="text" maxlength="200" placeholder="省市区 + 详细地址" />
          </label>
          <label class="field">
            <span>订单备注(选填)</span>
            <input v-model="remark" type="text" maxlength="200" placeholder="如:工作日送达" />
          </label>
        </form>
      </section>

      <!-- 右:订单明细 -->
      <section class="panel glass">
        <h2>订单明细</h2>
        <div v-if="!items.length" class="empty">
          <p>没有待结算的商品</p>
          <RouterLink to="/cart" class="btn-ghost">回购物车勾选</RouterLink>
        </div>
        <template v-else>
          <ul class="list">
            <li v-for="item in items" :key="item.skuId" class="line">
              <div class="line-info">
                <span class="line-name">{{ item.skuName }}</span>
                <span v-if="item.spec" class="line-spec">{{ item.spec }}</span>
              </div>
              <span class="line-qty num">× {{ item.quantity }}</span>
              <span class="line-price num">¥{{ formatPrice(item.price * item.quantity) }}</span>
            </li>
          </ul>

          <div class="total">
            <span>共 {{ cart.checkedCount }} 件,合计</span>
            <strong class="num">¥{{ formatPrice(total) }}</strong>
            <small>(支付时以服务端计算为准)</small>
          </div>

          <button class="btn-primary submit-btn" :disabled="submitting || !items.length" @click="submit">
            {{ submitting ? '提交中…' : '提交订单' }}
          </button>
        </template>
      </section>
    </div>
  </div>
</template>

<style scoped>
.checkout { padding: var(--s-8) 0 var(--s-12); }
.page-title { font-size: 24px; font-weight: 300; letter-spacing: 4px; margin-bottom: var(--s-6); }

.error-tip {
  margin-bottom: var(--s-4);
  padding: var(--s-3) var(--s-5);
  color: var(--c-danger);
  border: 1px solid rgba(255, 92, 108, 0.35);
  border-radius: var(--r-md);
  font-size: 13px;
}

.grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: var(--s-6);
  align-items: start;
}

.panel { padding: var(--s-7); border-radius: var(--r-xl); }
.panel h2 {
  font-size: 16px;
  font-weight: 400;
  letter-spacing: 2px;
  margin-bottom: var(--s-5);
}

.form { display: flex; flex-direction: column; gap: var(--s-4); }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-4); }
.field { display: flex; flex-direction: column; gap: var(--s-2); }
.field span { font-size: 12px; color: var(--c-text-faint); letter-spacing: 1px; }
.field input {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
  padding: 12px 16px;
  font-size: 14px;
  color: var(--c-text);
  outline: none;
  transition: border-color var(--t-fast) var(--ease-out);
}
.field input:focus { border-color: var(--c-accent); }
.field input::placeholder { color: var(--c-text-faint); }

.empty { text-align: center; color: var(--c-text-dim); padding: var(--s-6) 0; }
.empty p { margin-bottom: var(--s-4); }

.list { list-style: none; display: flex; flex-direction: column; gap: var(--s-3); }
.line {
  display: flex;
  align-items: center;
  gap: var(--s-3);
  padding: var(--s-3) var(--s-4);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
}
.line-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.line-name {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.line-spec {
  align-self: flex-start;
  font-size: 11px;
  color: var(--c-text-faint);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 1px 8px;
}
.line-qty { margin-left: auto; font-size: 13px; color: var(--c-text-dim); }
.line-price { font-size: 14px; min-width: 90px; text-align: right; }

.total {
  margin-top: var(--s-5);
  padding-top: var(--s-5);
  border-top: 1px solid var(--c-border);
  display: flex;
  align-items: baseline;
  gap: var(--s-2);
  font-size: 14px;
  color: var(--c-text-dim);
}
.total strong { margin-left: auto; font-size: 24px; color: var(--c-accent); }
.total small { font-size: 11px; color: var(--c-text-faint); }

.submit-btn { margin-top: var(--s-5); width: 100%; }

@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
  .field-row { grid-template-columns: 1fr; }
}
</style>
