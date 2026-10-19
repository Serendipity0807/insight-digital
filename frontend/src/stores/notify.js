import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount, markRead as markReadApi } from '../api/notify'

/**
 * 未读数:全局唯一一份,导航栏铃铛角标和消息中心页共用。
 * 角标不是核心链路:拉取失败静默保持原值,下次轮询自动纠偏。
 */
export const useNotifyStore = defineStore('notify', () => {
  const unread = ref(0)

  /** 拉一次未读数(登录后 + 每 30 秒轮询调用) */
  async function loadUnread() {
    try {
      unread.value = await getUnreadCount()
    } catch (e) {
      // 静默失败:接口抖动时角标保持原值,不打扰用户
    }
  }

  /** 标记已读:后端成功后本地角标减一;失败往上抛,由消息页决定怎么处理 */
  async function markRead(id) {
    await markReadApi(id)
    unread.value = Math.max(0, unread.value - 1)
  }

  /** 退出登录时清零(后端数据还在,重新登录后 loadUnread 拉回) */
  function clear() {
    unread.value = 0
  }

  return { unread, loadUnread, markRead, clear }
})
