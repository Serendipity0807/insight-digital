import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as cartApi from '../api/cart'

/**
 * 购物车状态:全局唯一一份,导航栏角标 / 购物车页 / 结算页共用。
 * 数据源是后端(Redis),store 只是它的前端镜像——
 * 每次增删改都先调接口,成功后再改本地,保证两边一致。
 */
export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  /** 角标数:所有条目数量之和 */
  const count = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))

  /** 已勾选的条目:结算页只认这些 */
  const checkedItems = computed(() => items.value.filter((i) => i.checked))

  /** 已勾选件数(结算按钮旁展示) */
  const checkedCount = computed(() =>
    checkedItems.value.reduce((sum, i) => sum + i.quantity, 0)
  )

  /** 合计金额(分):购物车页展示用;下单时服务端会重算,前端数字仅供参考 */
  const checkedTotal = computed(() =>
    checkedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0)
  )

  const allChecked = computed(
    () => items.value.length > 0 && items.value.every((i) => i.checked)
  )

  async function load() {
    loading.value = true
    try {
      items.value = await cartApi.getCartList()
    } finally {
      loading.value = false
    }
  }

  /** 加购:成功后整表刷新(后端是权威,别手算) */
  async function add(skuId, quantity) {
    await cartApi.addCartItem({ skuId, quantity })
    await load()
  }

  /** 改数量:先改本地(界面立即响应),接口失败再回滚 */
  async function updateQuantity(skuId, quantity) {
    const item = items.value.find((i) => i.skuId === skuId)
    if (!item) return
    const old = item.quantity
    item.quantity = quantity
    try {
      await cartApi.updateQuantity(skuId, { quantity })
    } catch (e) {
      item.quantity = old
      throw e
    }
  }

  async function toggleChecked(skuId, checked) {
    const item = items.value.find((i) => i.skuId === skuId)
    if (!item) return
    const old = item.checked
    item.checked = checked
    try {
      await cartApi.updateChecked(skuId, { checked })
    } catch (e) {
      item.checked = old
      throw e
    }
  }

  /** 全选/全不选:逐条调接口(购物车量级小,简单可靠优先) */
  async function setAllChecked(checked) {
    await Promise.all(
      items.value.map((i) => cartApi.updateChecked(i.skuId, { checked }))
    )
    items.value.forEach((i) => (i.checked = checked))
  }

  async function remove(skuId) {
    await cartApi.removeItem(skuId)
    items.value = items.value.filter((i) => i.skuId !== skuId)
  }

  /** 退出登录时清空前端镜像(后端 Redis 里的数据还在,重新登录后 load 回来) */
  function clear() {
    items.value = []
  }

  return {
    items,
    loading,
    count,
    checkedItems,
    checkedCount,
    checkedTotal,
    allChecked,
    load,
    add,
    updateQuantity,
    toggleChecked,
    setAllChecked,
    remove,
    clear
  }
})
