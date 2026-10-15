<script setup>
import { useRouter } from 'vue-router'
import { useCompareStore } from '../../stores/compare'

const store = useCompareStore()
const router = useRouter()

const goCompare = () => router.push('/compare')
</script>

<template>
  <!-- 悬浮条:勾选至少 1 款后浮现,凑满 2 款才能开始对比 -->
  <transition name="bar">
    <div v-if="store.items.length" class="compare-bar">
      <span class="bar-label">慧眼对比</span>

      <div class="bar-slots">
        <div v-for="p in store.items" :key="p.id" class="bar-item">
          <span class="thumb" :style="{ '--hue': p.id % 360 }">{{ p.name.charAt(0) }}</span>
          <span class="bar-name">{{ p.name }}</span>
          <button class="bar-remove" title="移除" @click="store.remove(p.id)">×</button>
        </div>
        <span v-if="!store.isFull" class="bar-empty">再选 1 款</span>
      </div>

      <button class="btn-primary bar-go" :disabled="!store.ready" @click="goCompare">
        开始对比<span v-if="!store.ready">({{ store.items.length }}/2)</span>
      </button>
      <button class="bar-clear" @click="store.clear">清空</button>
    </div>
  </transition>
</template>

<style scoped>
.compare-bar {
  position: fixed;
  bottom: var(--s-6);
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: var(--s-4);
  padding: var(--s-3) var(--s-4);
  border-radius: var(--r-lg);
  background: rgba(18, 22, 29, 0.85);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid var(--c-border-strong);
  box-shadow: var(--glow-card);
}
.bar-label {
  font-size: 12px;
  letter-spacing: 2px;
  color: var(--c-accent-2);
  white-space: nowrap;
}
.bar-slots {
  display: flex;
  align-items: center;
  gap: var(--s-3);
}
.bar-item {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  padding: 4px var(--s-3);
  border-radius: var(--r-pill);
  background: rgba(255, 255, 255, 0.05);
}
.thumb {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  font-size: 14px;
  color: hsla(var(--hue), 70%, 75%, 0.9);
  background: hsla(var(--hue), 80%, 55%, 0.18);
  border-radius: var(--r-sm);
}
.bar-name {
  max-width: 140px;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bar-remove {
  border: none;
  background: none;
  color: var(--c-text-faint);
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  padding: 0 2px;
}
.bar-remove:hover { color: var(--c-danger); }
.bar-empty {
  font-size: 12px;
  color: var(--c-text-faint);
  white-space: nowrap;
}
.bar-go { white-space: nowrap; }
.bar-go:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.bar-clear {
  border: none;
  background: none;
  color: var(--c-text-faint);
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}
.bar-clear:hover { color: var(--c-text-dim); }

/* 悬浮条进出场:底部滑入 */
.bar-enter-active,
.bar-leave-active {
  transition:
    transform var(--t-base) var(--ease-out),
    opacity var(--t-base) var(--ease-out);
}
.bar-enter-from,
.bar-leave-to {
  transform: translateX(-50%) translateY(16px);
  opacity: 0;
}
</style>
