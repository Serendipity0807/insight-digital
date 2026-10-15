<script setup>
// 顶部导航:固定 + 毛玻璃。
// 购物车角标数量:第 4 步接入 Pinia 后从 store 读,现在写死 0。
// 搜索框:第 2 步开放(接商品搜索接口)。
</script>

<template>
  <header class="header">
    <div class="container header-inner">
      <RouterLink to="/" class="logo">
        <svg class="logo-mark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <!-- "慧眼":一只镜头光圈组成的眼睛 -->
          <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
        <span class="logo-text">
          <strong>慧眼数码</strong>
          <small>INSIGHT DIGITAL</small>
        </span>
      </RouterLink>

      <nav class="nav">
        <RouterLink to="/" class="nav-link">首页</RouterLink>
        <a class="nav-link" href="#featured">全部商品</a>
        <span class="nav-link soon" title="第 3 步上线">
          慧眼对比<em class="soon-tag">即将上线</em>
        </span>
      </nav>

      <div class="actions">
        <label class="search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" /></svg>
          <input type="text" placeholder="搜索商品、品牌…" title="第 2 步开放" />
        </label>
        <button class="cart-btn" title="购物车(第 4 步开放)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" /><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" /></svg>
          <span class="badge">0</span>
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-h);
  z-index: 100;
  background: rgba(11, 14, 18, 0.65);
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border-bottom: 1px solid var(--c-border);
}
.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--s-8);
}

/* —— Logo —— */
.logo { display: flex; align-items: center; gap: var(--s-3); }
.logo-mark { width: 26px; height: 26px; color: var(--c-accent-2); }
.logo-text { display: flex; flex-direction: column; line-height: 1.15; }
.logo-text strong { font-size: 17px; letter-spacing: 2px; }
.logo-text small { font-size: 10px; color: var(--c-text-faint); letter-spacing: 2.5px; }

/* —— 导航 —— */
.nav { display: flex; gap: var(--s-7); margin-left: auto; }
.nav-link {
  font-size: 14px;
  color: var(--c-text-dim);
  transition: color var(--t-fast) var(--ease-out);
}
.nav-link:hover { color: var(--c-text); }
.router-link-active { color: var(--c-text); }
.soon {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  cursor: not-allowed;
}
.soon-tag {
  font-style: normal;
  font-size: 10px;
  color: var(--c-accent-2);
  border: 1px solid rgba(33, 212, 196, 0.4);
  border-radius: var(--r-pill);
  padding: 1px 8px;
}

/* —— 右侧:搜索 / 购物车 —— */
.actions { display: flex; align-items: center; gap: var(--s-3); }
.search {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  padding: 8px 16px;
  transition: border-color var(--t-fast) var(--ease-out);
}
.search:focus-within { border-color: var(--c-accent); }
.search svg { width: 16px; height: 16px; color: var(--c-text-faint); }
.search input {
  background: none;
  border: none;
  outline: none;
  color: var(--c-text);
  font-size: 13px;
  width: 160px;
}
.search input::placeholder { color: var(--c-text-faint); }

.cart-btn {
  position: relative;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  background: var(--c-glass);
  border: 1px solid var(--c-border);
  border-radius: var(--r-pill);
  color: var(--c-text);
  transition: border-color var(--t-fast) var(--ease-out);
}
.cart-btn:hover { border-color: var(--c-accent); }
.cart-btn svg { width: 18px; height: 18px; }
.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: var(--c-accent);
  border-radius: var(--r-pill);
  box-shadow: var(--glow-accent);
}

@media (max-width: 860px) {
  .nav { display: none; }
  .search input { width: 100px; }
}
</style>
