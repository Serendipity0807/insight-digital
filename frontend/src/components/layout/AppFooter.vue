<template>
  <footer class="footer">
    <div class="container footer-top">
      <div class="brand">
        <strong>慧眼数码</strong>
        <p>让复杂的数码选购,变得简单、透明、有温度。</p>
      </div>
      <div class="cols">
        <div class="col">
          <h4>购物指南</h4>
          <a href="#">购物流程</a>
          <a href="#">会员等级</a>
          <a href="#">常见问题</a>
        </div>
        <div class="col">
          <h4>配送与售后</h4>
          <a href="#">配送说明</a>
          <a href="#">退款政策</a>
          <a href="#">以旧换新</a>
        </div>
        <div class="col">
          <h4>关于慧眼</h4>
          <a href="#">品牌故事</a>
          <a href="#">联系我们</a>
          <a href="#">加入我们</a>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <div class="container">© 2026 慧眼数码 INSIGHT DIGITAL · 学习项目(前端阶段第 1 步)</div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  margin-top: var(--s-12);
  border-top: 1px solid var(--c-border);
  background: rgba(255, 255, 255, 0.02);
}
.footer-top {
  display: flex;
  justify-content: space-between;
  gap: var(--s-10);
  padding: var(--s-10) 0 var(--s-8);
  flex-wrap: wrap;
}
.brand strong { font-size: 18px; letter-spacing: 2px; }
.brand p { margin-top: var(--s-3); color: var(--c-text-dim); font-size: 13px; }
.cols { display: flex; gap: var(--s-10); flex-wrap: wrap; }
.col h4 {
  font-size: 13px;
  color: var(--c-text-faint);
  font-weight: 500;
  letter-spacing: 1px;
  margin-bottom: var(--s-4);
}
.col a {
  display: block;
  font-size: 13px;
  color: var(--c-text-dim);
  margin-bottom: var(--s-3);
  transition: color var(--t-fast) var(--ease-out);
}
.col a:hover { color: var(--c-text); }
.footer-bottom {
  border-top: 1px solid var(--c-border);
  padding: var(--s-5) 0;
  color: var(--c-text-faint);
  font-size: 12px;
}
</style>
