<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { getHealth } from '@/api/healthApi'

const pending = ref(false)
const connected = ref(false)
const message = ref('尚未检查后端连接')
let request: AbortController | undefined

async function checkConnection() {
  if (pending.value) return
  pending.value = true
  connected.value = false
  message.value = '正在检查连接…'
  request = new AbortController()
  const timeout = window.setTimeout(() => request?.abort(), 5000)
  try {
    await getHealth(request.signal)
    connected.value = true
    message.value = '后端服务已连接'
  } catch (error) {
    message.value =
      error instanceof DOMException && error.name === 'AbortError'
        ? '连接超时，请确认后端已启动'
        : '暂时无法连接后端，请确认服务与代理配置'
  } finally {
    window.clearTimeout(timeout)
    pending.value = false
  }
}

onMounted(checkConnection)
onUnmounted(() => request?.abort())
</script>

<template>
  <main class="bootstrap">
    <p class="eyebrow">DEVELOPMENT</p>
    <h1>Personal Economy</h1>
    <p>这里用于检查 API 服务连接，不代表业务功能已经验收。</p>
    <section class="connection" aria-labelledby="connection-title" :aria-busy="pending">
      <h2 id="connection-title">服务连接</h2>
      <p role="status" aria-live="polite" :class="{ connected, error: !connected && !pending }">
        {{ message }}
      </p>
      <button :disabled="pending" @click="checkConnection">
        {{ pending ? '检查中…' : '重新检查' }}
      </button>
    </section>
  </main>
</template>
