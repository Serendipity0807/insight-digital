<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { createPayment, mockCallback } from '../api/payment'
import { formatPrice } from '../utils/format'

const route = useRoute()

// 收银台三态:收银(选渠道)→ 待支付(拿到支付单)→ 已支付(回调成功)
const step = ref('cashier')
const channel = ref('ALIPAY')
const paymentNo = ref('')
const payAmount = ref(null) // 以 /pay 返回为准,不信任页面自己带的数字
const error = ref('')
const busy = ref(false)

const orderId = computed(() => route.params.orderId)

// 结算页放进 sessionStorage 的订单信息(金额/截止时间只做展示)
const pending = (() => {
  try {
    return JSON.parse(sessionStorage.getItem('insight_pending_order') || 'null')
  } catch {
    return null
  }
})()

const expireText = computed(() => {
  if (!pending?.expireTime) return ''
  return pending.expireTime.replace('T', ' ').slice(0, 16)
})

onMounted(() => {
  // 刷新页面丢了 sessionStorage 也没关系:再点一次"去支付",
  // 后端幂等地返回同一张支付单,金额以返回值为准
})

async function goPay() {
  error.value = ''
  busy.value = true
  try {
    const vo = await createPayment({ orderId: orderId.value, channel: channel.value })
    paymentNo.value = vo.paymentNo
    payAmount.value = vo.amount
    step.value = 'pending'
  } catch (e) {
    // 常见失败:订单已关单(10701)/ 订单状态不对
    error.value = e.message || '发起支付失败'
  } finally {
    busy.value = false
  }
}

/**
 * 模拟渠道回调:真实场景是支付宝/微信的服务器支付完成后
 * 直发 POST /callback(带验签),前端只轮询支付状态;
 * 学习项目用这个按钮模拟"渠道告诉我们钱到账了"。
 */
async function simulateCallback() {
  error.value = ''
  busy.value = true
  try {
    await mockCallback({
      paymentNo: paymentNo.value,
      tradeNo: 'MOCK' + Date.now() // 模拟渠道流水号
    })
    step.value = 'paid'
  } catch (e) {
    error.value = e.message || '回调失败'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div class="container pay-page">
    <h1 class="page-title">收银台</h1>

    <div class="card glass">
      <p v-if="error" class="error-tip">{{ error }}</p>

      <!-- ① 选择渠道 -->
      <template v-if="step === 'cashier'">
        <div class="amount-box">
          <span class="label">订单 {{ orderId }}</span>
          <div class="amount">
            <span class="symbol">¥</span>
            <span class="num num-big">{{ payAmount == null ? (pending ? formatPrice(pending.payAmount) : '—') : formatPrice(payAmount) }}</span>
          </div>
          <span v-if="expireText" class="expire">支付截止时间:{{ expireText }}</span>
          <span v-else class="expire">未支付订单将在 30 分钟后由系统自动关闭</span>
        </div>

        <div class="channels">
          <button class="channel" :class="{ active: channel === 'ALIPAY' }" @click="channel = 'ALIPAY'">
            支付宝
          </button>
          <button class="channel" :class="{ active: channel === 'WECHAT' }" @click="channel = 'WECHAT'">
            微信支付
          </button>
        </div>

        <button class="btn-primary pay-btn" :disabled="busy" @click="goPay">
          {{ busy ? '创建支付单…' : '去支付' }}
        </button>
      </template>

      <!-- ② 已创建支付单,模拟渠道侧 -->
      <template v-else-if="step === 'pending'">
        <div class="amount-box">
          <span class="label">应付金额</span>
          <div class="amount">
            <span class="symbol">¥</span>
            <span class="num num-big">{{ formatPrice(payAmount) }}</span>
          </div>
          <span class="expire">支付单号:{{ paymentNo }}</span>
        </div>

        <p class="hint">
          真实场景:这一步会跳转到支付宝/微信的收银页面,用户付款后由<strong>渠道服务器</strong>
          回调后端(签名验签),前端不参与。学习演示:点下面的按钮模拟渠道回调。
        </p>

        <button class="btn-primary pay-btn" :disabled="busy" @click="simulateCallback">
          {{ busy ? '回调处理中…' : '模拟支付成功(渠道回调)' }}
        </button>
      </template>

      <!-- ③ 支付成功 -->
      <template v-else>
        <div class="success-box">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9" /><path d="m8.5 12 2.5 2.5 5-5" /></svg>
          <h2>支付成功</h2>
          <p>订单状态已更新为「已支付」,锁定库存已扣减</p>
          <span class="payno">支付单号:{{ paymentNo }}</span>
        </div>

        <div class="actions">
          <RouterLink to="/" class="btn-primary">回首页</RouterLink>
          <RouterLink to="/cart" class="btn-ghost">继续逛逛</RouterLink>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.pay-page {
  padding: var(--s-8) 0 var(--s-12);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.page-title { font-size: 24px; font-weight: 300; letter-spacing: 4px; margin-bottom: var(--s-6); }

.card {
  width: min(520px, 100%);
  padding: var(--s-8);
  border-radius: var(--r-xl);
}

.error-tip {
  margin-bottom: var(--s-4);
  padding: var(--s-3) var(--s-5);
  color: var(--c-danger);
  border: 1px solid rgba(255, 92, 108, 0.35);
  border-radius: var(--r-md);
  font-size: 13px;
}

.amount-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--s-2);
  padding: var(--s-6) 0;
}
.label { font-size: 13px; color: var(--c-text-faint); letter-spacing: 1px; }
.amount { display: flex; align-items: baseline; gap: var(--s-2); }
.symbol { font-size: 20px; color: var(--c-accent); font-weight: 600; }
.num-big { font-size: 44px; font-weight: 600; }
.expire { font-size: 12px; color: var(--c-text-faint); }
.payno { font-size: 12px; color: var(--c-text-faint); margin-top: var(--s-3); }

.channels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-3);
  margin: var(--s-5) 0;
}
.channel {
  padding: 14px 0;
  font-size: 15px;
  letter-spacing: 2px;
  color: var(--c-text-dim);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
  transition:
    border-color var(--t-fast) var(--ease-out),
    color var(--t-fast) var(--ease-out);
}
.channel.active {
  color: #fff;
  border-color: transparent;
  background: var(--c-gradient);
}

.hint {
  margin: var(--s-4) 0 var(--s-5);
  font-size: 13px;
  line-height: 1.9;
  color: var(--c-text-dim);
}
.hint strong { color: var(--c-accent-2); font-weight: 500; }

.pay-btn { width: 100%; }

.success-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--s-3);
  padding: var(--s-6) 0;
}
.success-box svg { width: 64px; height: 64px; color: var(--c-success); }
.success-box h2 { font-size: 22px; font-weight: 300; letter-spacing: 2px; }
.success-box p { font-size: 13px; color: var(--c-text-dim); }

.actions { display: flex; gap: var(--s-4); justify-content: center; }
</style>
