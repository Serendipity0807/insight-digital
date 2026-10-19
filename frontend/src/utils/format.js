// 金额格式化:后端金额单位是"分"(数据库铁律),展示统一除以 100 + 千分位。
export function formatPrice(fen) {
  if (fen == null) return '—'
  return (fen / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 时间格式化:后端 LocalDateTime 序列化成 "2026-10-19T20:00:00"(无时区后缀,即后端本地时间)。
// 展示截取 "MM-DD HH:mm"——零依赖,不为这一个场景引 dayjs。
export function formatTime(str) {
  if (!str) return ''
  return str.slice(5, 16).replace('T', ' ')
}
