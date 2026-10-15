import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 慧眼对比状态:全局记住用户勾选的商品(跨页面)。
 * 上限 2 款——"左右分屏"只放得下两个;满员后再勾第 3 款会被忽略。
 */
export const useCompareStore = defineStore('compare', () => {
  const items = ref([])
  const MAX = 2

  const isFull = computed(() => items.value.length >= MAX)
  const ready = computed(() => items.value.length === MAX)

  // 只保留对比需要的字段,别把整个商品对象塞进状态(数据干净、可预测)
  function pick(product) {
    return {
      id: product.id,
      name: product.name,
      mainImage: product.mainImage,
      tag: product.tag,
      tagline: product.tagline,
      minPrice: product.minPrice
    }
  }

  function toggle(product) {
    const i = items.value.findIndex((x) => x.id === product.id)
    if (i >= 0) {
      items.value.splice(i, 1) // 已在对比 → 移除
    } else if (items.value.length < MAX) {
      items.value.push(pick(product)) // 未满 → 加入
    }
    // 满员且点第 3 款:忽略(后续可加 toast 提示)
  }

  function remove(id) {
    items.value = items.value.filter((x) => x.id !== id)
  }

  function clear() {
    items.value = []
  }

  return { items, isFull, ready, toggle, remove, clear }
})
