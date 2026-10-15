// ============================================
// 商品假数据 —— 第 1 步专用占位
// 字段与后端 mall-product 保持一致(见 ProductDetailVO):
//   id / name / mainImage / description / sales / skus[]
// 金额单位:分(数据库铁律),前端展示时除以 100。
// hue:占位图的聚光灯色相(0-360),第 2 步换成真实主图后删除。
// 第 2 步接入 GET /api/v1/product/{id} 后,整个文件删除。
// ============================================
export const products = [
  {
    id: 1001,
    name: 'iPhone 15 Pro 256G 原色钛金属',
    tagline: 'A17 Pro 芯片 · 钛金属机身 · 三摄',
    tag: '影像旗舰',
    hue: 210,
    description: 'A17 Pro 芯片,钛金属机身。',
    sales: 12800,
    skus: [
      { id: 10011, spec: '原色钛金属 256G', price: 699900, originalPrice: 799900 },
      { id: 10012, spec: '原色钛金属 512G', price: 799900, originalPrice: 899900 }
    ]
  },
  {
    id: 1002,
    name: '小米 14 Pro 16+512G 白色',
    tagline: '骁龙 8 Gen3 · 徕卡光学 · 2K 屏',
    tag: '性价比之王',
    hue: 265,
    description: '骁龙 8 Gen3,徕卡影像。',
    sales: 23600,
    skus: [
      { id: 10021, spec: '白色 16+512G', price: 499900, originalPrice: 549900 },
      { id: 10022, spec: '黑色 16+512G', price: 499900, originalPrice: 549900 }
    ]
  },
  {
    id: 1003,
    name: '索尼 A7M4 全画幅微单 单机身',
    tagline: '3300 万像素 · 4K 60P · 五轴防抖',
    tag: '创作利器',
    hue: 190,
    description: '新一代全画幅基准,拍照视频双修。',
    sales: 5400,
    skus: [
      { id: 10031, spec: '单机身', price: 1699900, originalPrice: 1799900 },
      { id: 10032, spec: '28-70mm 套机', price: 1899900, originalPrice: 1999900 }
    ]
  },
  {
    id: 1004,
    name: '索尼 WH-1000XM5 降噪耳机 黑色',
    tagline: '行业标杆降噪 · 30 小时续航',
    tag: '影音旗舰',
    hue: 160,
    description: '更轻,更安静,更动听。',
    sales: 18900,
    skus: [
      { id: 10041, spec: '黑色', price: 239900, originalPrice: 289900 },
      { id: 10042, spec: '铂金银', price: 239900, originalPrice: 289900 }
    ]
  },
  {
    id: 1005,
    name: 'MacBook Air 13 M3 16+512G 午夜色',
    tagline: 'M3 芯片 · 18 小时续航 · 1.24kg',
    tag: '适合办公',
    hue: 225,
    description: '轻薄本标杆,安静又持久。',
    sales: 8600,
    skus: [
      { id: 10051, spec: '午夜色 16+512G', price: 949900, originalPrice: 999900 },
      { id: 10052, spec: '星光色 16+512G', price: 949900, originalPrice: 999900 }
    ]
  },
  {
    id: 1006,
    name: 'iPad Pro 11 M4 256G 深空灰',
    tagline: 'M4 芯片 · Ultra Retina XDR',
    tag: '随行灵感',
    hue: 240,
    description: '极致轻薄,性能无界。',
    sales: 11200,
    skus: [
      { id: 10061, spec: '深空灰 256G', price: 899900, originalPrice: 949900 },
      { id: 10062, spec: '银色 256G', price: 899900, originalPrice: 949900 }
    ]
  }
]
