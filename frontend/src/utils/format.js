// 金额格式化:后端金额单位是"分"(数据库铁律),展示统一除以 100 + 千分位。
export function formatPrice(fen) {
  if (fen == null) return '—'
  return (fen / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}
