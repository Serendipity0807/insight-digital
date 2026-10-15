<script setup>
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'
import AiAssistantButton from './components/common/AiAssistantButton.vue'
</script>

<template>
  <div class="app-shell">
    <AppHeader />
    <main class="app-main">
      <RouterView />
    </main>
    <AppFooter />

    <!-- 阶段⑦ 预留:AI 统一助手(智能客服 / 导购 / 数据问答)入口。
         届时点击后展开对话面板,面板本体挂载在这里,数据走 agent 服务(:9000) -->
    <AiAssistantButton />
  </div>
</template>

<style scoped>
.app-main {
  /* 固定头部让位 */
  padding-top: var(--header-h);
}
</style>
