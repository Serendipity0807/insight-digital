<script setup>
import ProductCard from '../components/product/ProductCard.vue'
import { products } from '../mock/products'

const scrollToFeatured = () => {
  document.getElementById('featured')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<template>
  <div class="home">
    <!-- ===== 首屏:海报式 Hero ===== -->
    <section class="hero">
      <div class="hero-grid container">
        <div class="hero-copy">
          <p class="hero-kicker">INSIGHT DIGITAL · 慧眼数码</p>
          <h1 class="hero-title">
            见微知著<br />
            <span class="text-accent">慧眼识机</span>
          </h1>
          <p class="hero-sub">复杂参数,一眼看懂。每一次选购,都是一次被尊重的体验。</p>
          <div class="hero-cta">
            <button class="btn-primary" @click="scrollToFeatured">立即选购</button>
            <button class="btn-ghost glass">了解慧眼</button>
          </div>
          <div class="hero-stats">
            <div><strong class="num">2,000+</strong><span>甄选好物</span></div>
            <div><strong class="num">100%</strong><span>正品保障</span></div>
            <div><strong class="num">7×24</strong><span>慧眼助手在线</span></div>
          </div>
        </div>

        <div class="hero-visual">
          <!-- 镜头特写:珠宝式聚光展示(暂代真实产品图,含星芒) -->
          <svg class="lens" viewBox="0 0 420 420" fill="none" aria-label="产品特写占位图">
            <defs>
              <linearGradient id="lensRing" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stop-color="#2e6bff" />
                <stop offset="1" stop-color="#21d4c4" />
              </linearGradient>
              <radialGradient id="lensBody" cx="0.35" cy="0.3" r="1">
                <stop offset="0" stop-color="#2b3542" />
                <stop offset="0.6" stop-color="#141a22" />
                <stop offset="1" stop-color="#0c1015" />
              </radialGradient>
              <radialGradient id="lensGlow" cx="0.5" cy="0.5" r="0.5">
                <stop offset="0" stop-color="rgba(46,107,255,0.45)" />
                <stop offset="1" stop-color="rgba(46,107,255,0)" />
              </radialGradient>
              <filter id="flareBlur" x="-100%" y="-100%" width="300%" height="300%">
                <feGaussianBlur stdDeviation="6" />
              </filter>
            </defs>

            <!-- 星芒 -->
            <g filter="url(#flareBlur)" opacity="0.55">
              <line x1="210" y1="20" x2="210" y2="400" stroke="url(#lensRing)" stroke-width="2" />
              <line x1="20" y1="210" x2="400" y2="210" stroke="url(#lensRing)" stroke-width="2" />
            </g>

            <!-- 外圈金属环 + 镜身 -->
            <circle cx="210" cy="210" r="180" stroke="url(#lensRing)" stroke-width="1.5" opacity="0.55" />
            <circle cx="210" cy="210" r="168" stroke="rgba(255,255,255,0.10)" stroke-width="8" />
            <circle cx="210" cy="210" r="160" fill="url(#lensBody)" stroke="rgba(255,255,255,0.12)" stroke-width="1.5" />

            <!-- 光圈叶片 -->
            <polygon points="210,130 279,170 279,250 210,290 141,250 141,170" fill="#0a0e14" stroke="rgba(255,255,255,0.10)" stroke-width="1.5" />

            <!-- 镜片:蓝色玻璃辉光 -->
            <circle cx="210" cy="210" r="118" fill="url(#lensGlow)" stroke="rgba(255,255,255,0.08)" stroke-width="1.5" />
            <circle cx="210" cy="210" r="96" fill="#0b1017" stroke="url(#lensRing)" stroke-width="1.5" opacity="0.9" />
            <circle cx="210" cy="210" r="70" fill="none" stroke="rgba(33,212,196,0.35)" stroke-width="1" />
            <circle cx="210" cy="210" r="46" fill="url(#lensRing)" opacity="0.16" />

            <!-- 高光弧 -->
            <path d="M 150 132 A 118 118 0 0 1 270 132" stroke="rgba(255,255,255,0.65)" stroke-width="6" stroke-linecap="round" />

            <!-- 漂浮参数芯片(第 2 步替换为真实参数) -->
            <g class="chip" transform="translate(300,70)">
              <rect width="84" height="36" rx="18" fill="rgba(255,255,255,0.06)" stroke="rgba(255,255,255,0.14)" />
              <text x="42" y="23" text-anchor="middle" fill="#eef2f7" font-size="14">ƒ/1.78</text>
            </g>
            <g class="chip chip-2" transform="translate(36,300)">
              <rect width="96" height="36" rx="18" fill="rgba(255,255,255,0.06)" stroke="rgba(255,255,255,0.14)" />
              <text x="48" y="23" text-anchor="middle" fill="#eef2f7" font-size="14">48MP</text>
            </g>
          </svg>
        </div>
      </div>
      <div class="hero-scroll-hint">向下探索 ↓</div>
    </section>

    <!-- ===== 信任条:正品保障 / 七天无理由 / 极速退款 ===== -->
    <section class="trust container">
      <div class="trust-item glass">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1 1 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z" /><path d="m9 12 2 2 4-4" /></svg>
        <div><strong>正品保障</strong><span>官方货源 · 假一赔十</span></div>
      </div>
      <div class="trust-item glass">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" /><path d="M3 3v5h5" /></svg>
        <div><strong>七天无理由</strong><span>不满意 · 轻松退</span></div>
      </div>
      <div class="trust-item glass">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2 3 14h9l-1 8 10-12h-9l1-8z" /></svg>
        <div><strong>极速退款</strong><span>最快 2 小时到账</span></div>
      </div>
    </section>

    <!-- ===== 慧眼精选:画册式商品卡片 ===== -->
    <section id="featured" class="featured container">
      <header class="section-head">
        <div>
          <h2>慧眼精选</h2>
          <p>像翻阅画册一样,发现值得入手的数码好物</p>
        </div>
        <a class="btn-ghost glass" href="#">查看全部</a>
      </header>
      <div class="grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
    </section>

    <!-- ===== 慧眼对比预告(第 3 步上线,先占住视觉位置) ===== -->
    <section class="compare-teaser container">
      <div class="teaser glass">
        <div>
          <h2>慧眼对比</h2>
          <p>勾选两款,左右分屏。差异,一眼看清。</p>
        </div>
        <button class="btn-primary" disabled title="第 3 步上线">开始对比 · 即将上线</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ===== Hero ===== */
.hero {
  position: relative;
  min-height: calc(100vh - var(--header-h));
  display: flex;
  align-items: center;
  overflow: hidden;
  /* 细点阵科技纹理(上半部渐隐) */
  background-image: radial-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  -webkit-mask-image: radial-gradient(ellipse 90% 70% at 50% 25%, #000 30%, transparent 75%);
  mask-image: radial-gradient(ellipse 90% 70% at 50% 25%, #000 30%, transparent 75%);
}
.hero::before {
  /* 两盏环境射灯 */
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(900px 500px at 75% 20%, rgba(46, 107, 255, 0.14), transparent 60%),
    radial-gradient(700px 420px at 20% 60%, rgba(33, 212, 196, 0.07), transparent 55%);
  pointer-events: none;
}
.hero-grid {
  position: relative;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  align-items: center;
  gap: var(--s-10);
  padding: var(--s-10) 0;
}
.hero-kicker {
  font-size: 12px;
  letter-spacing: 4px;
  color: var(--c-text-faint);
  margin-bottom: var(--s-6);
}
.hero-title {
  font-size: clamp(44px, 6.5vw, 80px);
  font-weight: 200; /* 细字重 = 高级感 */
  line-height: 1.08;
  letter-spacing: 2px;
}
.hero-sub {
  margin-top: var(--s-6);
  font-size: 16px;
  color: var(--c-text-dim);
  max-width: 420px;
  line-height: 1.8;
}
.hero-cta { margin-top: var(--s-8); display: flex; gap: var(--s-4); }
.hero-stats { margin-top: var(--s-10); display: flex; gap: var(--s-8); }
.hero-stats div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-right: var(--s-8);
  border-right: 1px solid var(--c-border);
}
.hero-stats div:last-child { border-right: none; }
.hero-stats strong {
  font-size: 22px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}
.hero-stats span { font-size: 12px; color: var(--c-text-faint); }

.hero-visual { display: grid; place-items: center; }
.lens {
  width: min(440px, 100%);
  filter: drop-shadow(0 0 60px rgba(46, 107, 255, 0.15));
}
.chip { animation: chip-float 5s var(--ease-out) infinite; }
.chip-2 { animation-delay: 2.5s; }
@keyframes chip-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.hero-scroll-hint {
  position: absolute;
  bottom: var(--s-6);
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: var(--c-text-faint);
  letter-spacing: 2px;
  animation: hint-bob 2s var(--ease-out) infinite;
}
@keyframes hint-bob {
  0%, 100% { transform: translate(-50%, 0); }
  50% { transform: translate(-50%, 6px); }
}

/* ===== 信任条 ===== */
.trust {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--s-4);
  margin-top: var(--s-9);
}
.trust-item {
  display: flex;
  align-items: center;
  gap: var(--s-4);
  padding: var(--s-5) var(--s-6);
  border-radius: var(--r-lg);
}
.trust-item svg { width: 22px; height: 22px; color: var(--c-accent-2); flex-shrink: 0; }
.trust-item strong { font-size: 14px; display: block; }
.trust-item span { font-size: 12px; color: var(--c-text-faint); }

/* ===== 慧眼精选 ===== */
.featured { margin-top: var(--s-12); }
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--s-7);
}
.section-head h2 { font-size: 30px; font-weight: 300; letter-spacing: 2px; }
.section-head p { margin-top: var(--s-2); font-size: 13px; color: var(--c-text-faint); }
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--s-5);
}

/* ===== 对比预告 ===== */
.compare-teaser { margin-top: var(--s-12); }
.teaser {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-6);
  padding: var(--s-8);
  border-radius: var(--r-xl);
  background:
    linear-gradient(120deg, rgba(46, 107, 255, 0.10), rgba(33, 212, 196, 0.06)),
    var(--c-glass);
  border: 1px solid var(--c-border);
  flex-wrap: wrap;
}
.teaser h2 { font-size: 24px; font-weight: 300; letter-spacing: 2px; }
.teaser p { margin-top: var(--s-2); font-size: 13px; color: var(--c-text-dim); }

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .hero-grid { grid-template-columns: 1fr; }
  .hero-visual { display: none; }
  .trust { grid-template-columns: 1fr; }
}
</style>
