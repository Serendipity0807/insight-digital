<script setup>
// ============ 阶段⑦ 预留:AI 统一助手入口 ============
// 未来的"慧眼助手" = 智能客服 / 导购 / 数据问答(统一助手 + Multi-Agent)。
// 届时:点击后在这里展开对话面板(组件化挂载到 App.vue),
// 对话请求走 agent 服务(Python :9000),经 mall-gateway 转发。
// 视觉上先占住右下角这块黄金位置,避免后期改动整体布局。
const comingSoon = () => {
  alert('慧眼助手即将上线(阶段⑦,与后端 Agent 一起落地)')
}
</script>

<template>
  <button class="ai-btn" @click="comingSoon" title="慧眼助手 · AI 导购/客服(阶段⑦ 上线)">
    <span class="ai-pulse"></span>
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M9.9 2.6 11.6 7.2l4.6 1.7-4.6 1.7-1.7 4.6-1.7-4.6-4.6-1.7 4.6-1.7Z" />
      <path d="M19 14.5l.9 2.6 2.6.9-2.6.9-.9 2.6-.9-2.6-2.6-.9 2.6-.9Z" />
    </svg>
  </button>
</template>

<style scoped>
.ai-btn {
  position: fixed;
  right: var(--s-6);
  bottom: var(--s-6);
  z-index: 200;
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  color: var(--c-text);
  background: rgba(18, 22, 29, 0.75);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid var(--c-border-strong);
  border-radius: var(--r-pill);
  box-shadow: var(--glow-card);
  transition:
    transform var(--t-fast) var(--ease-out),
    border-color var(--t-fast) var(--ease-out),
    box-shadow var(--t-fast) var(--ease-out);
}
.ai-btn:hover {
  transform: translateY(-3px);
  border-color: var(--c-accent-2);
  box-shadow: var(--glow-card), var(--glow-cyan);
}
.ai-btn svg { width: 24px; height: 24px; color: var(--c-accent-2); }

/* 呼吸光环:暗示"这是活的入口",等 Agent 上线后点亮 */
.ai-pulse {
  position: absolute;
  inset: -4px;
  border-radius: var(--r-pill);
  border: 1px solid rgba(33, 212, 196, 0.35);
  animation: ai-pulse 2.4s var(--ease-out) infinite;
}
@keyframes ai-pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.12); opacity: 0; }
}
</style>
