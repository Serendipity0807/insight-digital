<template>
  <!-- 详情页占位:第 2 步填充。
       落地内容:参数可视化图表(续航柱状图/跑分对比)、SKU 选择、
       慧眼点评标签、信任徽章、加入购物车、慧眼对比入口 -->
  <div class="container stub">
    <div class="stub-panel glass">
      <p class="stub-kicker">第 2 步 · 建设中</p>
      <h2>商品详情页</h2>
      <p>参数可视化图表、慧眼点评、信任徽章、加入购物车,都将在这里落地。</p>
      <RouterLink to="/" class="btn-ghost glass">← 返回首页</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.stub {
  min-height: calc(100vh - var(--header-h) - 200px);
  display: grid;
  place-items: center;
  padding: var(--s-10) 0;
}
.stub-panel {
  max-width: 480px;
  text-align: center;
  padding: var(--s-10) var(--s-8);
  border-radius: var(--r-xl);
}
.stub-kicker {
  font-size: 12px;
  letter-spacing: 3px;
  color: var(--c-accent-2);
  margin-bottom: var(--s-4);
}
.stub-panel h2 { font-size: 26px; font-weight: 300; letter-spacing: 2px; }
.stub-panel p {
  margin: var(--s-4) 0 var(--s-7);
  font-size: 13px;
  color: var(--c-text-dim);
  line-height: 1.8;
}
</style>
