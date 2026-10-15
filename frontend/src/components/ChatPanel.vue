<script setup lang="ts">
import { nextTick, ref } from 'vue'

// 一条聊天消息:谁说的 + 内容
interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
}

const messages = ref<ChatMessage[]>([])
const input = ref('')
const loading = ref(false) // 是否正在等后端回答
const error = ref('') // 请求失败时的提示
const listEl = ref<HTMLElement | null>(null)

let nextId = 1

// 空状态下的示例问题(点击即发送)
const suggestions = [
  '一线城市住宿费标准是多少?',
  '法定节假日加班费是几倍?',
  '差旅报销需要哪些材料?',
]

// 每次有新消息后,把滚动条滑到底部
async function scrollToBottom() {
  await nextTick()
  listEl.value?.scrollTo({ top: listEl.value.scrollHeight, behavior: 'smooth' })
}

// 发送一条消息:先展示用户问题 → 请求后端 → 展示回答
async function sendMessage(text: string) {
  const question = text.trim()
  if (!question || loading.value) return

  messages.value.push({ id: nextId++, role: 'user', content: question })
  input.value = ''
  error.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const res = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: question }),
    })
    if (!res.ok) throw new Error(`请求失败(HTTP ${res.status})`)
    const data = (await res.json()) as { reply: string }
    messages.value.push({ id: nextId++, role: 'assistant', content: data.reply })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '未知错误'
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// 回车发送;输入法选字时的回车不发送(isComposing)
function onKeydown(e: KeyboardEvent) {
  if (e.isComposing) return
  sendMessage(input.value)
}
</script>

<template>
  <div class="chat-app">
    <!-- 顶部栏:毛玻璃效果 -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand">
          <span class="logo">🧠</span>
          <div>
            <h1>OrgMind</h1>
            <p class="subtitle">公司知识助手</p>
          </div>
        </div>
        <span class="status"><i class="dot"></i>已连接知识库</span>
      </div>
    </header>

    <!-- 消息区 -->
    <main class="messages" ref="listEl">
      <div class="messages-inner">
        <!-- 空状态:欢迎语 + 示例问题 -->
        <section v-if="messages.length === 0 && !loading" class="empty">
          <div class="empty-icon">💬</div>
          <h2>你好,我是 OrgMind</h2>
          <p>我可以根据公司资料回答制度、流程类问题,试试下面几个:</p>
          <div class="chips">
            <button v-for="s in suggestions" :key="s" class="chip" @click="sendMessage(s)">
              {{ s }}
            </button>
          </div>
        </section>

        <!-- 消息气泡 -->
        <article v-for="m in messages" :key="m.id" class="row" :class="m.role">
          <div class="bubble">{{ m.content }}</div>
        </article>

        <!-- 正在思考:三个跳动的点 -->
        <article v-if="loading" class="row assistant">
          <div class="bubble typing"><span></span><span></span><span></span></div>
        </article>

        <!-- 出错提示 -->
        <p v-if="error" class="error">⚠️ {{ error }}(请确认后端服务已启动)</p>
      </div>
    </main>

    <!-- 底部输入区 -->
    <footer class="composer">
      <div class="composer-inner">
        <input
          v-model="input"
          class="composer-input"
          type="text"
          placeholder="输入问题,回车发送"
          :disabled="loading"
          @keydown.enter="onKeydown"
        />
        <button
          class="send-btn"
          :disabled="loading || !input.trim()"
          aria-label="发送"
          @click="sendMessage(input)"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M12 19V5M5 12l7-7 7 7"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>
      <p class="hint">回答基于已导入的公司文档生成,仅供参考</p>
    </footer>
  </div>
</template>

<style scoped>
.chat-app {
  height: 100dvh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

/* ===== 顶部栏:毛玻璃 ===== */
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.72);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
.topbar-inner {
  max-width: 760px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  font-size: 22px;
}
.brand h1 {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
}
.subtitle {
  font-size: 12px;
  color: #86868b;
}
.status {
  font-size: 12px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #34c759;
}

/* ===== 消息区 ===== */
.messages {
  flex: 1;
  overflow-y: auto;
}
.messages-inner {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.messages::-webkit-scrollbar {
  width: 6px;
}
.messages::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}
.messages::-webkit-scrollbar-track {
  background: transparent;
}

/* ===== 气泡(iMessage 风格:大圆角 + 小尾巴角)===== */
.row {
  display: flex;
}
.row.user {
  justify-content: flex-end;
}
.bubble {
  max-width: 72%;
  padding: 10px 15px;
  font-size: 15px;
  line-height: 1.55;
  border-radius: 18px;
  white-space: pre-wrap; /* 保留回答里的换行 */
  animation: pop 0.28s cubic-bezier(0.2, 0.9, 0.3, 1);
}
.row.user .bubble {
  background: #0a84ff;
  color: #ffffff;
  border-bottom-right-radius: 5px;
}
.row.assistant .bubble {
  background: #e9e9eb;
  color: #1d1d1f;
  border-bottom-left-radius: 5px;
}
@keyframes pop {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 正在思考:三个跳动的点 ===== */
.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 16px;
}
.typing span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #86868b;
  animation: bounce 1.2s infinite;
}
.typing span:nth-child(2) {
  animation-delay: 0.15s;
}
.typing span:nth-child(3) {
  animation-delay: 0.3s;
}
@keyframes bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

/* ===== 空状态:欢迎语 + 示例问题 ===== */
.empty {
  text-align: center;
  padding: 56px 0 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.empty-icon {
  font-size: 44px;
}
.empty h2 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
}
.empty p {
  color: #86868b;
  font-size: 14px;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}
.chip {
  border: 1px solid #d2d2d7;
  background: #ffffff;
  color: #0071e3;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
}
.chip:hover {
  background: #f5f5f7;
  border-color: #0071e3;
}

/* ===== 出错提示 ===== */
.error {
  color: #ff3b30;
  font-size: 13px;
  text-align: center;
}

/* ===== 底部输入区:毛玻璃 ===== */
.composer {
  background: rgba(255, 255, 255, 0.85);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding: 12px 24px 10px;
}
.composer-inner {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 10px;
}
.composer-input {
  flex: 1;
  border: 1px solid #d2d2d7;
  border-radius: 999px;
  padding: 11px 18px;
  font-size: 15px;
  background: #f5f5f7;
  outline: none;
  transition: all 0.2s;
}
.composer-input:focus {
  background: #ffffff;
  border-color: #0071e3;
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15);
}
.send-btn {
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 50%;
  background: #0071e3;
  color: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}
.send-btn:hover:not(:disabled) {
  background: #0077ed;
  transform: scale(1.05);
}
.send-btn:disabled {
  opacity: 0.4;
  cursor: default;
}
.hint {
  max-width: 760px;
  margin: 8px auto 0;
  font-size: 11px;
  color: #aeaeb2;
  text-align: center;
}
</style>
